package dbstresstest.logic.runnable.work;

import dbstresstest.data.objects.SQLSet;
import dbstresstest.data.objects.ValueSet;
import dbstresstest.logic.variables.Var;
import dbstresstest.plugins.DbPool;
import dbstresstest.logic.runnable.TaskLogger;
import dbstresstest.util.exceptions.DataTypeNotRecognizedException;
import dbstresstest.util.exceptions.FunctionEvaluationException;
import dbstresstest.util.exceptions.InvalidFunctionException;
import dbstresstest.util.exceptions.VarNotInitializedException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Lukas Hanusek
 * 
 * 
 * This set will be executed as single PreparedStatement but with different variables (for each line of variables once)
 * 
 */
public class ParametrizedQuery implements ExecutableQuery {
    
    private SQLSet set;
    
    private String sql;
    
    private int timeout = -1;
    
    private boolean isCall;
            
    private ExecutorService timeoutService;
    
    private CopyOnWriteArraySet<CopyOnWriteArraySet<Var>> varSets; //contains lines of argumens, must be accessed via iterator to keep the order in which variables were added
    
    
    public ParametrizedQuery(SQLSet set) throws InvalidFunctionException {
        this.set = set;
        this.sql = set.getSql();
        this.timeout = set.getTimeout();
        this.isCall = set.isCall();
        prepareVariables();
    }
    
    private void prepareVariables() throws InvalidFunctionException {
        if (set.getValues() != null) {
            varSets = new CopyOnWriteArraySet<CopyOnWriteArraySet<Var>>();
            for (ValueSet val : set.getValues()) {
                CopyOnWriteArraySet<Var> varSet = new CopyOnWriteArraySet<Var>();
                for (String param : val.getParams()) {
                    varSet.add(new Var(param));
                }
                varSets.add(varSet);
            }
        }
        if (this.timeout > 0) timeoutService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void execute(DbPool pool, TaskLogger logger) throws VarNotInitializedException, DataTypeNotRecognizedException, FunctionEvaluationException {
        try (Connection con = pool.getConnection(); PreparedStatement st = (this.isCall ? con.prepareCall(this.sql) : con.prepareStatement(this.sql))) {
            if (this.timeout > 0) con.setNetworkTimeout(timeoutService, this.timeout);
         
            //if sql comes without variables just execute it
            if (varSets == null || varSets.isEmpty()) {
                execute(st, logger);
            } else {
                //for all varibles (each line of variables defined)
                for (CopyOnWriteArraySet<Var> varSet : varSets) {
                    int i = 1;
                    for (Var v : varSet) {
                        st.setObject(i, v.getValue()); //setObject method automatically detects data type
                        i++;
                    }
                    execute(st, logger);
                }
            }
        } catch (Exception e) {
            logger.logQuery();
            logger.addTotalQueryTime(set.getName(), 0); //we need to add time here as well so the number of line is the same for all queries
            logger.logError(set.getName(), e);
        }
    }

    private void execute(PreparedStatement st, TaskLogger logger) throws SQLException {
        long start = System.currentTimeMillis();
        st.execute();
        long time = System.currentTimeMillis() - start;
        logger.addTotalQueryTime(set.getName(), time);
        logger.log("Query " + this.sql + " took " + time + "ms");
        logger.logQuery();
    }

    public void close() {
        if (timeoutService != null) timeoutService.shutdownNow();
    }
    
    
}

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
    
    private int timeout = 0;
    
    private CopyOnWriteArraySet<CopyOnWriteArraySet<Var>> varSets; //contains lines of argumens, must be accessed via iterator to keep the order in which variables were added
    
    
    public ParametrizedQuery(SQLSet set) throws InvalidFunctionException {
        this.set = set;
        this.sql = set.getSql();
        this.timeout = set.getTimeout();
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
    }

    @Override
    public void execute(DbPool pool, TaskLogger logger) throws VarNotInitializedException, DataTypeNotRecognizedException, FunctionEvaluationException {
        PreparedStatement st = null;
        Connection con = null;
        try {
            con = pool.getConnection();
            if (this.timeout > 0) con.setNetworkTimeout(Executors.newFixedThreadPool(1), this.timeout);
            
            //if this sql set is a CALL we need to create a CallableStatement
            if (set.isCall()) {
                st = con.prepareCall(this.sql);
            } else {
                st = con.prepareStatement(this.sql);
            }
            
            //if sql comes without variables just execute it
            if (varSets == null || varSets.isEmpty()) {
                executeStatement(st, logger);
                return;
            }
            
            //for all varibles (each line of variables defined)
            for (CopyOnWriteArraySet<Var> varSet : varSets) {
                int i = 1;
                for (Var v : varSet) {
                    st.setObject(i, v.getValue()); //setObject method automatically detects data type
                    i++;
                }
                executeStatement(st, logger);
            }
        } catch (Exception e) {
            logger.logQuery();
            logger.addTotalQueryTime(set.getName(), 0); //we need to add time here as well so the number of line is the same for all queries
            logger.logError(set.getName(), e);
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (Exception e) {
            } //statement might be closed already by bad query
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
            } //statement might be closed already by bad query
        }
    }
    
    private void executeStatement(PreparedStatement st, TaskLogger logger) throws SQLException {
        long start = System.currentTimeMillis();
        /* 
        Official Java docs:
        Executes the SQL statement in this PreparedStatement object, which may be any kind of SQL statement. 
        Some prepared statements return multiple results; the execute method handles these complex statements 
        as well as the simpler form of statements handled by the methods executeQuery and executeUpdate.
        */
        st.execute();
        long time = System.currentTimeMillis() - start;
        logger.addTotalQueryTime(set.getName(), time);
        logger.log("Query " + this.sql + " took " + time + "ms");
        logger.logQuery();
    }
    
}

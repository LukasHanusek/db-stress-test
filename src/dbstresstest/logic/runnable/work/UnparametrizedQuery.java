/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.runnable.work;

import dbstresstest.data.objects.SQLSet;
import dbstresstest.data.objects.ValueSet;
import dbstresstest.logic.variables.Var;
import dbstresstest.logic.variables.VarType;
import dbstresstest.plugins.DbPool;
import dbstresstest.logic.runnable.TaskLogger;
import dbstresstest.util.StringUtil;
import dbstresstest.util.exceptions.DataTypeNotRecognizedException;
import dbstresstest.util.exceptions.FunctionEvaluationException;
import dbstresstest.util.exceptions.InvalidFunctionException;
import dbstresstest.util.exceptions.VarNotInitializedException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Dakado
 */
public class UnparametrizedQuery implements ExecutableQuery {
    
    private SQLSet set;
    
    private String sql;
    
    private int timeout = -1;
    
    private ExecutorService timeoutService;
    
    private CopyOnWriteArraySet<CopyOnWriteArraySet<Var>> varSets; //contains lines of argumens, must be accessed via iterator to keep the order in which variables were added
    
    
    public UnparametrizedQuery(SQLSet set) throws InvalidFunctionException {
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
        if (this.timeout > 0) timeoutService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void execute(DbPool pool, TaskLogger logger) throws VarNotInitializedException, DataTypeNotRecognizedException, FunctionEvaluationException {
        try (Connection con = pool.getConnection(); Statement st = con.createStatement();) {
            if (this.timeout > 0) con.setNetworkTimeout(timeoutService, this.timeout);

                //if sql comes without variables just execute it
                if (varSets == null || varSets.isEmpty()) {
                    long start = System.currentTimeMillis();
                    st.execute(sql);
                    long time = System.currentTimeMillis() - start;
                    logger.addTotalQueryTime(set.getName(), time);
                    logger.log("Query " + sql + " took " + time + "ms");
                    logger.logQuery();
                    return;
                }
                
                //for all varibles (each line of variables defined)
                for (CopyOnWriteArraySet<Var> varSet : varSets) {
                    String sql = this.sql;
                    for (Var v : varSet) {
                        if (v.getType() == VarType.DOUBLE) sql = StringUtil.replaceFirst('?', sql, v.getValue().toString());
                        if (v.getType() == VarType.INT) sql = StringUtil.replaceFirst('?', sql, v.getValue().toString());
                        if (v.getType() == VarType.LONG) sql = StringUtil.replaceFirst('?', sql, v.getValue().toString());
                        if (v.getType() == VarType.STRING) sql = StringUtil.replaceFirst('?', sql, "'" + v.getValue().toString() + "'");
                        if (v.getType() == VarType.OBJECT) sql = StringUtil.replaceFirst('?', "'" + sql, v.getValue().toString() + "'");
                        if (v.getType() == VarType.FUNCTION) sql = StringUtil.replaceFirst('?', sql, v.getFunctionValueAsString());
                    }
                    long start = System.currentTimeMillis();
                    st.execute(sql);
                    long time = System.currentTimeMillis() - start;
                    logger.addTotalQueryTime(set.getName(), time);
                    logger.log("Query " + sql + " took " + time + "ms");
                    logger.logQuery();
                }
        } catch (Exception e) {
            logger.logQuery();
            logger.addTotalQueryTime(set.getName(), 0); //we need to add time here as well so the number of line is the same for all queries
            logger.logError(set.getName(), e);
        }
    }
    
    public void close() {
        if (timeoutService != null) timeoutService.shutdownNow();
    }
    
}

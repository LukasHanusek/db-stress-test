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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;

/**
 *
 * @author Dakado
 */
public class UnparametrizedQuery implements ExecutableQuery {
    
    private SQLSet set;
    
    private CopyOnWriteArraySet<CopyOnWriteArraySet<Var>> varSets; //contains lines of argumens, must be accessed via iterator to keep the order in which variables were added
    
    
    public UnparametrizedQuery(SQLSet set) throws InvalidFunctionException {
        this.set = set;
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
    
    private void tryGetConnection(DbPool pool) {
        
    }

    @Override
    public void execute(DbPool pool, TaskLogger logger) throws VarNotInitializedException, DataTypeNotRecognizedException, FunctionEvaluationException {
        Statement st = null;
        Connection con = null;
        try {
            con = pool.getConnection();
            if (set.getTimeout() > 0) con.setNetworkTimeout(Executors.newFixedThreadPool(1), set.getTimeout());

                st = con.createStatement();
                
                //if sql comes without variables just execute it
                if (varSets == null || varSets.isEmpty()) {
                    executeStatement(st, set.getSql(), logger);
                    return;
                }
                
                //for all varibles (each line of variables defined)
                for (CopyOnWriteArraySet<Var> varSet : varSets) {
                    String sql = set.getSql();
                    for (Var v : varSet) {
                        if (v.getType() == VarType.DOUBLE) sql = StringUtil.replaceFirst('?', sql, v.getValue().toString());
                        if (v.getType() == VarType.INT) sql = StringUtil.replaceFirst('?', sql, v.getValue().toString());
                        if (v.getType() == VarType.LONG) sql = StringUtil.replaceFirst('?', sql, v.getValue().toString());
                        if (v.getType() == VarType.STRING) sql = StringUtil.replaceFirst('?', sql, "'" + v.getValue().toString() + "'");
                        if (v.getType() == VarType.OBJECT) sql = StringUtil.replaceFirst('?', "'" + sql, v.getValue().toString() + "'");
                        if (v.getType() == VarType.FUNCTION) sql = StringUtil.replaceFirst('?', sql, v.getFunctionValueAsString());
                    }
                    executeStatement(st, sql, logger);
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
    
    private void executeStatement(Statement st, String sql, TaskLogger logger) throws SQLException {
        long start = System.currentTimeMillis();
        /* 
        Official Java docs:
        Executes the SQL statement in this PreparedStatement object, which may be any kind of SQL statement. 
        Some prepared statements return multiple results; the execute method handles these complex statements 
        as well as the simpler form of statements handled by the methods executeQuery and executeUpdate.
        */
        st.execute(sql);
        long time = System.currentTimeMillis() - start;
        logger.addTotalQueryTime(set.getName(), time);
        logger.log("Query " + sql + " took " + time + "ms");
        logger.logQuery();
    }
    
}

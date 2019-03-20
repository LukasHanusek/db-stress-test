/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.runnable.work;

import dbstresstest.plugins.DbPool;
import dbstresstest.logic.runnable.TaskLogger;

/**
 *
 * @author Lukas Hanusek
 */
public interface ExecutableQuery {
    
    public void execute(DbPool pool, TaskLogger tl) throws Exception;
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.functions;

import dbstresstest.logic.functions.flist.RandomCharEnum;
import dbstresstest.logic.functions.flist.RandomInt;
import dbstresstest.util.exceptions.InvalidFunctionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Lukas Hanusek
 */
public class FunctionManager {
    
    //Singleton
    private static FunctionManager manager;

    public static FunctionManager getInstance() {
        if (manager == null) manager = new FunctionManager();
        return manager;
    }
    
    //Manager Implementation
    
    private ConcurrentHashMap<String, Class> functions;
    
    private FunctionManager() {
        functions = new ConcurrentHashMap<String, Class>();
        
        //register implemented function classes
        functions.put(Functions.randomInt, RandomInt.class);
        functions.put(Functions.randomCharEnum, RandomCharEnum.class);
    }
    
    /**
     * Get function by name
     * @param name
     * @return
     * @throws InvalidFunctionException 
     */
    public Class getFunctionByName(String name) throws InvalidFunctionException {
        if (functions.containsKey(name)) {
            return functions.get(name);
        }
        throw new InvalidFunctionException("SQL contains invalid user-defined function: '" + name + "'");
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.variables;

import dbstresstest.logic.functions.Function;
import dbstresstest.logic.functions.FunctionManager;
import dbstresstest.logic.functions.FunctionUtil;
import dbstresstest.util.exceptions.DataTypeNotRecognizedException;
import dbstresstest.util.exceptions.FunctionEvaluationException;
import dbstresstest.util.exceptions.InvalidFunctionException;
import dbstresstest.util.exceptions.VarNotInitializedException;


/**
 *
 * @author Lukas Hanusek
 */
public class Var {
    
    int intValue;
    long longValue;
    double doubleValue;
    String stringValue;
    Object objectValue;
    
    VarType type;
    
    Function functionValue;
    
    
    public Var(String var) throws InvalidFunctionException {
        //STRING TYPE
        if ( (var.startsWith("'") && var.endsWith("'")) ||  (var.startsWith("\"") && var.endsWith("\""))) {
            type = VarType.STRING;
            stringValue = var.substring(1, var.length()-1); //remove '' quotes
            return;
        }
        //NUMBER TYPES
        if (var.matches("\\d+")) {
            long l = Long.valueOf(var);
            if (l < Integer.MAX_VALUE) {
                type = VarType.INT;
                intValue = Integer.valueOf(var);
            } else {
                type = VarType.LONG;
                longValue = l;
            }
            return;
        }
        //double type
        if (var.matches("[-+]?[0-9]*\\.?[0-9]+")) {
            type = VarType.DOUBLE;
            doubleValue = Double.valueOf(var);
            return;
        }
        //function type
        if (FunctionUtil.isFunction(var)) {
            String fname = FunctionUtil.extractFunctionName(var);
            type = VarType.FUNCTION;
            Class function = FunctionManager.getInstance().getFunctionByName(fname);
            try {
                Function f = (Function)function.newInstance();
                f.parse(var);
                functionValue = f;
                return;
            } catch (InstantiationException ex) {
                throw new InvalidFunctionException("Could not create a new function instance!");
            } catch (IllegalAccessException ex) {
                throw new InvalidFunctionException("Illegal function access exception!");
            }
        }
        //OBEJCT type - data type not identified use default object - this must be the last option
        type = VarType.OBJECT;
        objectValue = var;
        
        return;
    }

    public VarType getType() {
        return type;
    }
    
    public String getFunctionValueAsString() throws FunctionEvaluationException {
        if (type == VarType.FUNCTION) return functionValue.evaluateAsString();
        throw new FunctionEvaluationException("This variable is not a function!");
    }
    
    public Object getValue() throws VarNotInitializedException, DataTypeNotRecognizedException, FunctionEvaluationException {
        if (type == null) throw new VarNotInitializedException("Variable not initilized!");
        if (type == VarType.LONG) return longValue;
        if (type == VarType.INT) return intValue;
        if (type == VarType.DOUBLE) return doubleValue;
        if (type == VarType.STRING) return stringValue;
        if (type == VarType.OBJECT) return objectValue;
        if (type == VarType.FUNCTION) return functionValue.evaluate();
        throw new DataTypeNotRecognizedException();
    }
        
}

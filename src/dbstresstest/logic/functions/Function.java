/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.functions;

import dbstresstest.util.exceptions.FunctionEvaluationException;
import dbstresstest.util.exceptions.InvalidFunctionException;

/**
 *
 * @author Lukas Hanusek
 */
public interface Function {
    
    public static final String NAME = null;
    
    /**
     * Evaluate the function and return the result
     * @return 
     */
    public Object evaluate() throws FunctionEvaluationException;
    
    /**
     * Evaluate the function and return the result in the string format for unparametrized queries so it should include '' quotes if requiered
     * @return 
     */
    public String evaluateAsString() throws FunctionEvaluationException;
    
    /**
     * Get name of this function
     * @return 
     */
    public String getFunctionName();
    
    /**
     * Parse the function in the text format
     * @param def 
     */
    public void parse(String def) throws InvalidFunctionException;
    
}

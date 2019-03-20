/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.functions.flist;

import dbstresstest.logic.functions.Function;
import dbstresstest.logic.functions.FunctionUtil;
import dbstresstest.logic.functions.Functions;
import dbstresstest.util.exceptions.FunctionEvaluationException;
import dbstresstest.util.exceptions.InvalidFunctionException;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Lukas Hanusek
 */
public class RandomInt implements Function {
    
    public static final String NAME = Functions.randomInt;
    
    private int min, max;

    @Override
    public Object evaluate() {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    
    @Override
    public String evaluateAsString() throws FunctionEvaluationException {
        return this.evaluate().toString();
    }

    @Override
    public String getFunctionName() {
        return NAME;
    }

    @Override
    public void parse(String def) throws InvalidFunctionException {
        try {
            this.min = Integer.valueOf(FunctionUtil.getArgument(def, 0));
            this.max = Integer.valueOf(FunctionUtil.getArgument(def, 1));
        } catch (Exception e) {
            throw new InvalidFunctionException(e.getMessage());
        }
    }
    
}

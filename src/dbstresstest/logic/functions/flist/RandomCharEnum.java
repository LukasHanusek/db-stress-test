/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.functions.flist;

import dbstresstest.data.objects.CharEnum;
import dbstresstest.data.objects.managers.CharEnumManager;
import dbstresstest.logic.functions.Function;
import dbstresstest.logic.functions.FunctionUtil;
import dbstresstest.logic.functions.Functions;
import dbstresstest.util.exceptions.FunctionEvaluationException;
import dbstresstest.util.exceptions.InvalidFunctionException;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Dakado
 */
public class RandomCharEnum implements Function {
    
    public static final String NAME = Functions.randomCharEnum;
    
    private String charEnum;

    @Override
    public Object evaluate() throws FunctionEvaluationException {
        try {
            CharEnum ch = CharEnumManager.getInstance().getCharEnumByName(charEnum);
            StringBuilder random = new StringBuilder();
            for (int i = 0; i < ch.getChars().length; i++) {
                random.append(ch.getChars()[ThreadLocalRandom.current().nextInt(0, ch.getChars().length)]);
            }
            return random.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new FunctionEvaluationException(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
    
    @Override
    public String evaluateAsString() throws FunctionEvaluationException {
        return "'" + this.evaluate().toString() + "'";
    }

    @Override
    public String getFunctionName() {
        return NAME;
    }

    @Override
    public void parse(String def) throws InvalidFunctionException {
        try {
            this.charEnum = FunctionUtil.getArgument(def, 0);
            CharEnumManager.getInstance().getCharEnumByName(charEnum);
        } catch (Exception e) {
            throw new InvalidFunctionException(e.getMessage());
        }
    }
    
}

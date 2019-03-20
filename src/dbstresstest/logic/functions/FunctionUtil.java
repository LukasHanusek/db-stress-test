/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.functions;

/**
 *
 * @author Lukas Hanusek
 */
public class FunctionUtil {
    
    /**
     * Is the given string a function ? 
     * @param f
     * @return 
     */
    public static boolean isFunction(String f) {
        f = f.replaceAll("\\s+", "");
        return f.startsWith("#{") && f.endsWith("}");
    }
    
    /**
     * Extract just the function name from entire definition
     * @param def
     * @return 
     */ 
    public static String extractFunctionName(String def) {
        def = def.replaceAll("\\s+", "");
        def = def.replace("#{", "");
        return def.split("\\(")[0];
    }
    
    /**
     * Get function argument
     * @param def
     * @param place
     * @return
     * @throws IndexOutOfBoundsException 
     */
    public static String getArgument(String def, int place) throws IndexOutOfBoundsException {
        def = def.replaceAll("\\s+", "");
        String args = def.split(extractFunctionName(def) + "\\(")[1].split("\\)")[0];
        int totalargs = args.split(",").length;
        if (place > totalargs) throw new IndexOutOfBoundsException();
        if (totalargs == 1) {
            return args;
        } else {
            return args.split(",")[place];
        }
    }
    
}

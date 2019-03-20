/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.util;

/**
 *
 * @author Dakado
 */
public class StringUtil {
    
    public static String replaceFirst(char what, String where, String with) {
        char array[] = where.toCharArray();
        int matchAt = 0;
        int i = 0;
        for (char c : array) {
            if (c == '?') {
                matchAt = i;
                break;
            }
            i++;
        }
        String before = where.substring(0, matchAt);
        String replaced = with;
        String after = where.substring(matchAt + 1, where.length());
        return before + replaced + after;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.data.objects;

/**
 *
 * @author Dakado
 */
public class CharEnum {
    
    private char[] chars;
    
    private String name;
    
    public CharEnum(String name, String data, String delimiter) {
        this.name = name;
        chars = data.replaceAll(delimiter, "").toCharArray();
    }

    public char[] getChars() {
        return chars;
    }

    public String getName() {
        return name;
    }

}

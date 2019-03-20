/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.variables;

import dbstresstest.data.objects.SQLSet;
import dbstresstest.data.objects.ValueSet;
import dbstresstest.util.exceptions.VariableException;
import java.util.LinkedList;

/**
 *
 * @author Lukas Hanusek
 */
public class VariableParser {
    
    /**
     * Parses variables into editable text format
     * @param set
     * @return 
     */
    public static String getVariablesTextFormat(SQLSet set) {
        if (set.getValues() != null) {
            StringBuilder sb = new StringBuilder();
            for (ValueSet val : set.getValues()) { //lines
                boolean first = true;
                for (String param : val.getParams()) { //params in line
                    if (first) {
                        sb.append(param);
                    } else {
                        sb.append(set.getParamDelimiter() + param);
                    }
                    first = false;
                }
                sb.append(set.getLineDelimiter() + System.lineSeparator()); //end the line
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    
    /**
     * Parses text area into XML objects
     * @param variablesTextArea
     * @return 
     */
    public static LinkedList<ValueSet> parseValues(String variablesText, char lineDelimiter, char valueDelimtier) throws VariableException {
        
        //remove all system separators:
        String clearvariablesTextArea = variablesText.replaceAll(System.lineSeparator(), "");
        clearvariablesTextArea = clearvariablesTextArea.replaceAll("\n", "");
        clearvariablesTextArea = clearvariablesTextArea.replaceAll("\r", "");
        clearvariablesTextArea = clearvariablesTextArea.replaceAll("\n\r", "");
        
        LinkedList<ValueSet> valuesList = new LinkedList();
        int firstLineNumOfParams = -1;
        int currLine = 0;
        for (String line : clearvariablesTextArea.split(lineDelimiter + "")) {
            if (line.length() < 2) {
                continue; //ignore empty lines
            }
            ValueSet v = new ValueSet();
            LinkedList<String> params = new LinkedList();
            
            //special char-to-char parsing to allow variable delimtiers inside strings and function definistions
            boolean opened = false; //is parsing opened ?
            char endingChar = valueDelimtier;
            StringBuilder param = new StringBuilder(); // store chars of 1 param here
            for (char c : line.toCharArray()) {
                if (!opened) { //if reading new param define encloasing char to search
                    if (c == '#') endingChar = '}'; //is function
                    else if (c == '\'') endingChar = '\''; //is string
                    else if (c == '\"') endingChar = '\"'; //is string 
                    else endingChar = valueDelimtier; //data type not specified use default delimiter
                    if (c == valueDelimtier) continue; //after parsing function or string the next char will be value delimiter, we do not want to include that, so skip it
                    opened = true;
                    param.append(c);
                } else {
                    if (c == endingChar) {
                        if (c != valueDelimtier) param.append(c); //append last char only if it is not a delimiter
                        safeAdd(param.toString(), params);
                        param = new StringBuilder();
                        opened = false;
                        continue;
                    }
                    param.append(c);
                }
            }
            //add last param because is is splitted by ; we cannot actually reach the end properly reading the last line edding delimiter
            safeAdd(param.toString(), params);
            
            
            if (firstLineNumOfParams == -1) {
                firstLineNumOfParams = params.size();
            } else if (params.size() != firstLineNumOfParams) {
                throw new VariableException("First (zero) line contains " + firstLineNumOfParams + " arguments but line " + currLine + " contains " + params.size() + " arguments! \nPlease make sure you have have correct argument count and that you have defined suitable delimiter characters.");
            }
            v.setParams(params);
            valuesList.add(v);
            currLine++;
        }
        return valuesList;
    }
    
    
    public static void safeAdd(String param, LinkedList<String> params) {
        if (param.length() == 0) return;
        if (param == " ") return;
        if (param == "\\s+") return;
        params.add(param);
    }
    

}

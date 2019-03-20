/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.logic.variables;

/**
 *
 * @author Lukas Hanusek
 */
public enum VarType {
    
    INT, LONG, DOUBLE, STRING, //detectable types
    OBJECT, FUNCTION //undetectable type + function type
    
}

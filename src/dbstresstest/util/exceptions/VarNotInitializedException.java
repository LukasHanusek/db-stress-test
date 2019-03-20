/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbstresstest.util.exceptions;

/**
 *
 * @author Lukas Hanusek
 */
public class VarNotInitializedException extends Exception {
    
    public VarNotInitializedException() {
        super();
    }
    
    public VarNotInitializedException(String message) {
        super(message);
    }
    
}

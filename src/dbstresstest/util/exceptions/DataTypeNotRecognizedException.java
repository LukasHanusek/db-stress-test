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
public class DataTypeNotRecognizedException extends Exception {

    public DataTypeNotRecognizedException() {
        super();
    }

    public DataTypeNotRecognizedException(String message) {
        super(message);
    }

}

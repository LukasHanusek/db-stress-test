package dbstresstest.util.exceptions;

/**
 *
 * @author Lukas Hanusek
 */
public class SetNameNotUniqueException extends Exception {
    
    public SetNameNotUniqueException() {
        
    }
    
    public SetNameNotUniqueException(String message) {
        super(message);
    }
    
}

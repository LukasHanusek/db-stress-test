package dbstresstest.util.exceptions;

/**
 *
 * @author Lukas Hanusek
 */
public class ConnectionNameNotUniqueException extends Exception {
    
    public ConnectionNameNotUniqueException() {
        
    }
    
    public ConnectionNameNotUniqueException(String message) {
        super(message);
    }
    
}

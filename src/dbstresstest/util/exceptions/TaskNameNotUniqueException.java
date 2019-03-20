package dbstresstest.util.exceptions;

/**
 *
 * @author Lukas Hanusek
 */
public class TaskNameNotUniqueException extends Exception {
    
    public TaskNameNotUniqueException() {
        
    }
    
    public TaskNameNotUniqueException(String message) {
        super(message);
    }
    
}

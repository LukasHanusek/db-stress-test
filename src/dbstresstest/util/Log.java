package dbstresstest.util;

/**
 *
 * @author Lukas Hanusek
 */
public class Log {
    
    
    private String dispatcher;
    
    /**
     * 
     * @param dispacher 
     */
    public Log(String dispacher) {
        this.dispatcher = dispacher;
    }
    
    /**
     * 
     * @param message 
     */
    public void logMessage(String message) {
        Log.log("[" + TimeUtil.getSimpleDateFormat(System.currentTimeMillis()) + "][" + this.dispatcher + "]: " + message);
    }
    
    /**
     * 
     * @param s 
     */
    public static void log(String s) {
        System.out.println(s);
    }
    
}

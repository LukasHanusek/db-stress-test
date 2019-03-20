package dbstresstest.plugins;

/**
 *
 * @author Lukas Hanusek
 */
public class DatabasePlugin {
    
    /**
     * Empty constructor for reflection
     */
    public DatabasePlugin() {
        
    }
    
    /**
     * Get connection pool class
     * @return 
     */
    public DbPool getPool() {
        return null;
    }
    
    /**
     * Plugin name
     * @return 
     */
    public String getName() {
        return "UnknownPlugin";
    }
    
    /**
     * Get driver class path for this plugin
     * @return
     * @throws ClassNotFoundException 
     */
    public String getDriver() throws ClassNotFoundException {
        return "com.mysql.jdbc.Driver";
    }
    
    /**
     * Example: jdbc:mysql://%ADDRESS%:%PORT%/%DB_NAME%
     * Variables:
     *  %ADDRESS% - database address
     *  %PORT% - database port
     *  %DB_NAME% - schema or database name (depending on db)
     * @return 
     */
    public String getURIFormat() {
        return "jdbc:mysql://%ADDRESS%:%PORT%/%DB_NAME%?allowMultiQueries=true";
    }
    
    /**
     * Parse the given SQL, in case of error returns ParseError clase with info about the error, otherwise returns null
     * @param sql
     * @return 
     */
    public ParseError parse(String sql) {
        return null;
    }
    
}

package dbstresstest.plugins;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Lukas Hanusek
 */
public interface DbPool {
    
    /**
     * During the object creation the connection pool is created, if fails
     * exception is thrown and obejct is nto created
     *
     * @param uri
     * @param user
     * @param password
     * @param maxConnections
     * @throws Exception
     */
    public DbPool setup(String driver, String uri, String user, String password, int maxConnections) throws Exception;
    
    /**
     * Close the pool
     * @throws Exception 
     */
    public void close() throws Exception;

    /**
     * Maximum amount of concurrently active db connections
     *
     * @return
     */
    public int getPoolMaxActive();

    /**
     * Current amount of active conenctions
     *
     * @return
     */
    public int getPoolActive();

    /**
     * Connections that have been used previously (or reused several times) but
     * are currently in sleep mode
     *
     * @return
     */
    public int getPoolIdle();
    
    /**
     * Get connection from pool
     * @return
     * @throws SQLException 
     */
    public Connection getConnection() throws SQLException;
    
}

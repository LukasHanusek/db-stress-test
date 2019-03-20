package dbstresstest.plugins;

import java.sql.Connection;

public interface ConnectionFactory {

    public Connection createNew();
    
}

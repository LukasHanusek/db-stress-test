package dbstresstest.logic.runnable;

import dbstresstest.data.objects.DbCon;
import dbstresstest.data.objects.Task;
import dbstresstest.data.objects.managers.ConManager;
import dbstresstest.gui.utils.GUI;
import dbstresstest.plugins.DatabasePlugin;
import dbstresstest.plugins.DbPool;
import dbstresstest.plugins.PluginLoader;
import dbstresstest.util.exceptions.ConnectionNotFoundException;
import dbstresstest.util.exceptions.ConnectionPoolSetupException;
import dbstresstest.util.exceptions.InvalidFunctionException;
import java.util.LinkedList;

/**
 *
 * @author Lukas Hanusek
 */
public class RunnableTaskCreator {
    
    public static final String dbPlaceholder = "%ADDRESS%";
    public static final String portPlaceholder = "%PORT%";
    public static final String dbNamePlaceholder = "%DB_NAME%";
    
    /**
     * Creates a new runnable task list from saved task
     * @param task
     * @return
     * @throws ConnectionPoolSetupException 
     */
    public static LinkedList<RunnableTask> createRunnable(Task task) throws ConnectionPoolSetupException, InvalidFunctionException {
        if (task.getDatabases() == null || task.getDatabases().isEmpty()) throw new ConnectionPoolSetupException("Could not locate database connection data for this task.");
        LinkedList<RunnableTask> runnables = new LinkedList<RunnableTask>();
        for (long conid : task.getDatabases()) {
            try {
                DbCon con = ConManager.getInstance().getConById(conid);
                RunnableTask rt = new RunnableTask(task, con, con.getCustomName());
                runnables.add(rt);
            } catch (ConnectionNotFoundException ex) {
                GUI.getInstance().openExceptionWindow(ex);
            }
        }
        return runnables;
    }

   
    public static DbPool createPool(DbCon con, int size) throws ConnectionPoolSetupException, ClassNotFoundException {
        DatabasePlugin plugin = PluginLoader.getInstance().getPluginByName(con.getDatabaseType());
        if (plugin == null) {
            throw new ConnectionPoolSetupException("Could not find database plugin suitable for this task.");
        }

        String uri = plugin.getURIFormat();
        uri = uri.replaceAll(dbPlaceholder, con.getAddress());
        if (con.getPort() <= 0) {
            uri = uri.replaceAll(":" + portPlaceholder, ""); //remove port from con string to use the default one if user defined port 0 or below
        } else {
            uri = uri.replaceAll(portPlaceholder, con.getPort() + "");
        }
        uri = uri.replaceAll(dbNamePlaceholder, con.getDatabaseName());

        DbPool pool;
        try {
            pool = plugin.getPool().setup(plugin.getDriver(), uri, con.getUser(), con.getDecodedPassword(), size);
        } catch (Exception ex) {
            throw new ConnectionPoolSetupException(ex.getClass().getName() + ": " + ex.getMessage());
        }
        return pool;
    }

}

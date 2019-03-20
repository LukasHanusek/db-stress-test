package dbstresstest.plugins;

import dbstresstest.util.Log;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lukas Hanusek
 */
public class PluginLoader {
    
    private List<DatabasePlugin> plugins;
    private ExtensionLoader<DatabasePlugin> loader; 
    
    private Log log;
    
    private static PluginLoader instance;
    
    public static PluginLoader getInstance() {
        if (instance == null) instance = new PluginLoader();
        return instance;
    }
    
    public PluginLoader() {
        log = new Log("PluginLoader");
        plugins = new ArrayList();
        loader = new ExtensionLoader<DatabasePlugin>(); 
    }

    /**
     * Get loaded plugins
     * @return 
     */
    public List<DatabasePlugin> getPlugins() {
        return plugins;
    }
    
    public DatabasePlugin getPluginByName(String name) {
        for (DatabasePlugin pl : this.plugins) {
            if (name.equalsIgnoreCase(pl.getName())) return pl;
        }
        return null;
    }
    
    /**
     * 
     * @throws MalformedURLException 
     */
    public void loadPlugins() throws MalformedURLException {
        File pluginsDir = new File("plugins");
        for (File jar : pluginsDir.listFiles()) {
            if (jar.getName().toLowerCase().endsWith(".jar")) {
                DatabasePlugin plugin; 
                try {
                    plugin = loader.LoadClass(jar.getAbsolutePath(), removeJarExtension(jar.getName()), DatabasePlugin.class);
                    log.logMessage("Loaded " + plugin.getName() + " plugin!");
                    this.plugins.add(plugin);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        }
        log.logMessage("Loaded " + this.plugins.size() + " plugins!");
    }
    
    private String removeJarExtension(String jar) {
        return jar.substring(0, jar.length()-4);
    }

}

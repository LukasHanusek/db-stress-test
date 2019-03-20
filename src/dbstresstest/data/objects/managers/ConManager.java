package dbstresstest.data.objects.managers;

import dbstresstest.data.objects.DbCon;
import dbstresstest.util.Log;
import dbstresstest.util.exceptions.ConnectionNameNotUniqueException;
import dbstresstest.util.exceptions.ConnectionNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 * Singleton - during the first getInstance call XML data are loaded, then once save method is called, data will be overrriden
 * @author Lukas Hanusek
 */

@XmlRootElement (name = "connections")
public class ConManager {
    
    @XmlTransient
    private List<ManagerListener<DbCon>> listeners = new ArrayList();
    
    @XmlTransient
    private static ConManager instance = null;
    private LinkedList<DbCon> connections;

    /**
     * 
     * @return 
     */
    public static ConManager getInstance() { 
        if (instance == null) instance = load();
        return instance;
    } 
    
    public static void reload() {
        instance = load();
    }
    
    /**
     * 
     * @param lis 
     */
    public void addListener(ManagerListener lis) {
        listeners.add(lis);
    }
    
    /**
     * 
     * @param lis 
     */
    public void removeListener(ManagerListener lis) {
        listeners.remove(lis);
    }
    
    @XmlElement(name = "connection")
    public LinkedList<DbCon> getConnections() {
        return this.connections;
    }
    
    public void callListChanged() {
        for (ManagerListener lis : listeners) {
            lis.onListChanged(connections);
        }
    }
    
    public void setConnections(LinkedList<DbCon> connections) {
        this.connections = connections;
        callListChanged();
    }
    
    /**
     * 
     * @param con
     * @throws ConnectionNameNotUniqueException 
     */
    public void addConnection(DbCon con) throws ConnectionNameNotUniqueException {
        if (isNameUnique(con.getCustomName())) {
            if (con.getId() == 0) con.setId(System.currentTimeMillis());
            this.connections.add(con);
            callListChanged();
            save();
        } else {
            throw new ConnectionNameNotUniqueException();
        }
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    private boolean isNameUnique(String name) {
        for (DbCon con : this.connections) {
            if (name.equalsIgnoreCase(con.getCustomName())) return false;
        }
        return true;
    }
    
    /**
     * 
     * @param con 
     */
    public void removeConnection(DbCon con) {
        this.connections.remove(con);
        callListChanged();
        save();
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    public DbCon getConByName(String name) {
        for (DbCon con : connections) {
            if (con.getCustomName().equalsIgnoreCase(name)) {
                return con;
            }
        }
        return null;
    }
    
    /**
     * Get connection by index
     * @param index
     * @return 
     */
    public DbCon getConByIndex(int index) {
        return connections.get(index);
    }

    /**
     * Get connection by id
     * @param id
     * @return 
     */
    public DbCon getConById(long id) throws ConnectionNotFoundException {
        for (DbCon con : connections) {
            if (con.getId() == id) {
                return con;
            }
        }
        throw new ConnectionNotFoundException();
    }

    /**
     * Saves current ConManager instance to XML
     */
    public void save() {
        Log log = new Log("ConManager");
        try {
            File folder = new File("data");
            if (folder == null || !folder.exists()) folder.mkdir();
            File file = new File("data/connections.xml");
            if (file == null || !file.exists()) {
                file.createNewFile();
            }
            log.logMessage("Saving " + this.connections.size() + " connections to " + file.getAbsolutePath());
            JAXBContext jaxbContext = JAXBContext.newInstance(ConManager.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(this, file);
            for (DbCon con : this.connections) {
                con.modified = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Loads ConManager instance from XML using JAXB
     * @return 
     */
    private static ConManager load() {
        Log log = new Log("ConManager");
        try {
            File file = new File("data/connections.xml");
            ConManager manager;
            if (file == null || !file.exists()) {
                log.logMessage("Could not load saved connections, file (data/connections.xml) does not exist!");
                manager = new ConManager();
                manager.setConnections(new LinkedList<DbCon>());
            } else {
                log.logMessage("Loading saved connections from " + file.getAbsolutePath());
                JAXBContext jaxbContext = JAXBContext.newInstance(ConManager.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                manager = (ConManager) jaxbUnmarshaller.unmarshal(file);
                if (manager.connections == null) manager.setConnections(new LinkedList<DbCon>()); //if no elements are saved, JAXB loads the list as null, we want to prevent that
            }
            return manager;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}

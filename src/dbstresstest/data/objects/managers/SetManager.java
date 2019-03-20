package dbstresstest.data.objects.managers;

import dbstresstest.data.objects.SQLSet;
import dbstresstest.util.Log;
import dbstresstest.util.exceptions.SetNameNotUniqueException;
import dbstresstest.util.exceptions.SetNotFoundException;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class SetManager {

    private List<ManagerListener<SQLSet>> listeners = new ArrayList();
    
    private static SetManager instance = null;
    
    private LinkedList<SQLSet> sets;

    public static SetManager getInstance() {
        if (instance == null) {
            instance = new SetManager();
            instance.load();
        }
        return instance;
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

    /**
     * 
     * @return 
     */
    public LinkedList<SQLSet> getSets() {
        return this.sets;
    }
    
    public void callListChanged() {
        for (ManagerListener lis : listeners) {
            lis.onListChanged(sets);
        }
    }
    
    /**
     * 
     * @param set
     * @throws SetNameNotUniqueException 
     */
    public void addSet(SQLSet set) throws SetNameNotUniqueException {
        if (isNameUnique(set.getName())) {
            if (set.getId() == 0) set.setId(System.currentTimeMillis());
            this.sets.add(set);
            callListChanged();
            save();
        } else {
            throw new SetNameNotUniqueException();
        }
    }
    
    /**
     * 
     * @param set
     * @return 
     */
    public boolean removeSet(SQLSet set) {
        File file = new File("data/sql/" + set.getId() + ".xml");
        if (file.exists()) {
            file.delete();
            this.sets.remove(set);
            callListChanged();
            save();
            return true;
        }
        return false;
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    public SQLSet getSetByName(String name) throws SetNotFoundException {
        for (SQLSet set : sets) {
            if (set.getName().equalsIgnoreCase(name)) {
                return set;
            }
        }
        throw new SetNotFoundException("Set does not exist or is not loaded properly!");
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    public SQLSet getSetById(long id) throws SetNotFoundException {
        for (SQLSet set : sets) {
            if (set.getId() == id) {
                return set;
            }
        }
        throw new SetNotFoundException("Set does not exist or is not loaded properly!");
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    private boolean isNameUnique(String name) {
        for (SQLSet set : this.sets) {
            if (name.equalsIgnoreCase(set.getName())) return false;
        }
        return true;
    }
    
    /**
     * Save currently loaded sets to disk
     */
    public void save() {
        Log log = new Log("SetManager");
        try {
            File folder = new File("data/sql");
            if (folder == null || !folder.exists()) folder.mkdir();
            
            for (SQLSet set : sets) {
                if (!set.modified) continue;
                File file = new File("data/sql/" + set.getId() + ".xml");
                if (file == null || !file.exists()) {
                    file.createNewFile();
                }
                log.logMessage("Saving set " + set.getName() + " to " + file.getAbsolutePath());
                JAXBContext jaxbContext = JAXBContext.newInstance(SQLSet.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                jaxbMarshaller.marshal(set, file);
                set.modified = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all sets saved on disk
     */
    private void load() {
        Log log = new Log("SetManager");
        sets = new LinkedList();
        try {
            File folder = new File("data/sql");
            if (folder == null || !folder.exists()) {
                folder.mkdir();
            }
            for (File file : folder.listFiles()) {
                log.logMessage("Loading saved set " + file.getAbsolutePath());
                JAXBContext jaxbContext = JAXBContext.newInstance(SQLSet.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                SQLSet set = (SQLSet) jaxbUnmarshaller.unmarshal(file);
                this.sets.add(set);
            }
            log.logMessage("Loaded " + sets.size() + " SQL sets!");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}

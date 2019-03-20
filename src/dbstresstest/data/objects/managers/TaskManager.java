package dbstresstest.data.objects.managers;

import dbstresstest.data.objects.Task;
import dbstresstest.util.Log;
import dbstresstest.util.exceptions.SetNameNotUniqueException;
import dbstresstest.util.exceptions.TaskNameNotUniqueException;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Lukas Hanusek
 */
public class TaskManager {
    
    private List<ManagerListener<Task>> listeners = new ArrayList();
    
    private static TaskManager instance = null;
    
    private LinkedList<Task> tasks;

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
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
    public LinkedList<Task> getTasks() {
        return this.tasks;
    }
    
    public void callListChanged() {
        for (ManagerListener lis : listeners) {
            lis.onListChanged(tasks); 
        }
    }
    
    /**
     * 
     * @param set
     * @throws SetNameNotUniqueException 
     */
    public void addTask(Task task) throws TaskNameNotUniqueException {
        if (isNameUnique(task.getTaskName())) {
            if (task.getId() == 0) task.setId(System.currentTimeMillis());
            this.tasks.add(task);
            callListChanged();
            save();
        } else {
            throw new TaskNameNotUniqueException();
        }
    }
    
    /**
     * 
     * @param set
     * @return 
     */
    public boolean removeTask(Task task) {
        File file = new File("data/tasks/" + task.getId() + ".xml");
        if (file.exists()) {
            file.delete();
            this.tasks.remove(task);
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
    public Task getTaskByName(String name) {
        for (Task task : tasks) {
            if (task.getTaskName().equalsIgnoreCase(name)) {
                return task;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param name
     * @return 
     */
    private boolean isNameUnique(String name) {
        for (Task task : this.tasks) {
            if (name.equalsIgnoreCase(task.getTaskName())) return false;
        }
        return true;
    }
    
    public boolean isNameUnique(String name, long currentID) {
        for (Task task : this.tasks) {
            if (name.equalsIgnoreCase(task.getTaskName()) && task.getId() != currentID) return false;
        }
        return true;
    }
    
    /**
     * Save currently loaded sets to disk
     */
    public void save() {
        Log log = new Log("TaskManager");
        try {
            File folder = new File("data/tasks");
            if (folder == null || !folder.exists()) folder.mkdir();
            
            for (Task task : this.tasks) {
                if (!task.modified) continue;
                File file = new File("data/tasks/" + task.getId() + ".xml");
                if (file == null || !file.exists()) {
                    file.createNewFile();
                }
                log.logMessage("Saving task " + task.getTaskName() + " to " + file.getAbsolutePath());
                JAXBContext jaxbContext = JAXBContext.newInstance(Task.class);
                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                jaxbMarshaller.marshal(task, file);
                task.modified = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all Tasks saved on disk
     */
    private void load() {
        Log log = new Log("TaskManager");
        this.tasks = new LinkedList();
        try {
            File folder = new File("data/tasks");
            if (folder == null || !folder.exists()) {
                folder.mkdir();
            }
            for (File file : folder.listFiles()) {
                log.logMessage("Loading task " + file.getAbsolutePath());
                JAXBContext jaxbContext = JAXBContext.newInstance(Task.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Task task = (Task) jaxbUnmarshaller.unmarshal(file);
                this.tasks.add(task);
            }
            log.logMessage("Loaded " + tasks.size() + " runnable tasks (workloads)!");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    
}

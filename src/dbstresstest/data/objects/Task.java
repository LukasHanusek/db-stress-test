package dbstresstest.data.objects;

import java.util.LinkedList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement (name = "workload")
public class Task {
    
    private long id;
    
    private LinkedList<Long> sets;
    private long interval;
    private int poolSize; 
    private int repeatCount;
    private String taskName;
    
    private LinkedList<Long> databases; //connection list on which this task will run on
    
    @XmlTransient
    public boolean modified = false;

    public long getId() {
        return id;
    }

    public LinkedList<Long> getDatabases() {
        if (databases == null) this.databases = new LinkedList();
        return databases;
    }

    @XmlElementWrapper (name="databases")
    @XmlElement (name = "database")
    public void setDatabases(LinkedList<Long> databases) {
        this.databases = databases;
    }

    @XmlAttribute
    public void setId(long id) {
        this.id = id;
    }

    public LinkedList<Long> getSets() {
        if (sets == null) this.sets = new LinkedList();
        return sets;
    }

    @XmlElement (name = "sql")
    public void setSets(LinkedList<Long> sets) {
        this.sets = sets;
    }

    public long getInterval() {
        return interval;
    }

    @XmlAttribute
    public void setInterval(long interval) {
        this.interval = interval;
    }

    public int getPoolSize() {
        return poolSize;
    }

    @XmlAttribute
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getTaskName() {
        return taskName;
    }

    @XmlAttribute
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    @XmlAttribute
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }
    
}

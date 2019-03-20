package dbstresstest.data.objects;

import java.util.LinkedList;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement (name = "sql")
public class SQLSet {
    
    private long id;
    
    private String name;
    private String sql;
    private LinkedList<ValueSet> values; //list of variable lines (1 item here = 1 line in editor)
    
    private String databaseType; //database plugin unique name
    
    private boolean parametrized;
    
    //query type
    private boolean query;
    private boolean update;
    private boolean call; 
    
    private char lineDelimiter;
    private char paramDelimiter;
    
    private int timeout;
    
    @XmlTransient
    public boolean modified = false;

    //empty contrctuor for XML parsing
    public SQLSet() {
        
    }
    
    public SQLSet(String name, String sql, LinkedList<ValueSet> variables) {
        this.name = name;
        this.sql = sql;
        this.values = variables;
    }

    public long getId() {
        return id;
    }

    @XmlAttribute(name = "id")
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        return sql;
    }

    @XmlAttribute(name = "text")
    public void setSql(String sql) {
        this.sql = sql;
    }

    public LinkedList<ValueSet> getValues() {
        return values;
    }

    @XmlElement (name = "values")
    public void setValues(LinkedList<ValueSet> values) {
        this.values = values;
    }

    public boolean isParametrized() {
        return parametrized;
    }
    
    @XmlAttribute(name = "parametrized")
    public void setParametrized(boolean parametrized) {
        this.parametrized = parametrized;
    }

    public boolean isQuery() {
        return query;
    }

    @XmlAttribute(name = "query")
    public void setQuery(boolean query) {
        this.query = query;
    }

    public boolean isUpdate() {
        return update;
    }

    @XmlAttribute(name = "update")
    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isCall() {
        return call;
    }

    @XmlAttribute(name = "call")
    public void setCall(boolean call) {
        this.call = call;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    @XmlAttribute(name = "type")
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public char getLineDelimiter() {
        return lineDelimiter;
    }

    @XmlAttribute(name = "lineDelimiter")
    public void setLineDelimiter(char lineDelimited) {
        this.lineDelimiter = lineDelimited;
    }

    public char getParamDelimiter() {
        return paramDelimiter;
    }

    @XmlAttribute(name = "paramDelimiter")
    public void setParamDelimiter(char paramDelimiter) {
        this.paramDelimiter = paramDelimiter;
    }

    public int getTimeout() {
        return timeout;
    }

    @XmlAttribute(name = "timeout")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}

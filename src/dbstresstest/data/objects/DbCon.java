package dbstresstest.data.objects;

import java.util.Base64;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class DbCon {
    
    private long id;
    
    private String customName;
    private String databaseName;
    private String databaseType;
    private String address;
    private String user;
    private String password;
    private int port;
    
    @XmlTransient
    public boolean modified = false;
    
    //empty contrctuor for XML parsing
    public DbCon() {
        
    }

    public long getId() {
        return id;
    }

    @XmlAttribute
    public void setId(long id) {
        this.id = id;
    }

    public String getCustomName() {
        return customName;
    }

    @XmlElement
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    @XmlElement
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getAddress() {
        return address;
    }

    @XmlElement
    public void setAddress(String address) {
        this.address = address;
    }

    public String getUser() {
        return user;
    }

    @XmlElement
    public void setUser(String user) {
        this.user = user;
    }
    
    /**
     * BE CAREFUL THIS IS ENCODED VERSION OF PASSWORD
     * If you need usable password use getDecodedPassword() method, this is only for XML JABX parsing, not for manual use!
     * @return 
     */
    public String getPassword() {
        return new String(Base64.getEncoder().encode(password.getBytes()));
    }
    /**
     * Get real decoded password
     * @return 
     */
    public String getDecodedPassword() {
        return this.password;
    }
    
    /**
     * This method is used when password is changed during the runtime, (e.g. con editing)
     */
    @XmlTransient
    public void setDecodedPassword(String password) {
        this.password = password;
    }

    /**
     * This is only for XML loading in JABX, do not use this manually, it will not work
     * @param password 
     */
    @XmlElement
    public void setPassword(String password) {
        String decoded = new String(Base64.getDecoder().decode(password.getBytes()));
        this.password = decoded;
    }
    
    public int getPort() {
        return port;
    }

    @XmlElement
    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }
    
    @XmlElement
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    
}

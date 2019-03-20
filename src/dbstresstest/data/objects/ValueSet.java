package dbstresstest.data.objects;

import java.util.LinkedList;
import javax.xml.bind.annotation.XmlElement;

/**
 * This object represents one line of Variables for 1 SQL Set
 * @author Lukas Hanusek
 */
public class ValueSet {
    
    private LinkedList<String> params;
    
    //empty contrctuor for XML parsing
    public ValueSet() {
        
    }

    public LinkedList<String> getParams() {
        return params;
    }

    @XmlElement (name = "p")
    public void setParams(LinkedList<String> params) {
        this.params = params;
    }

}

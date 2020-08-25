package ODTLibrary;

import java.util.Map;

/**
 * Defines basic properties of a Markup-Language tag; used to parse markup
 * languages
 */
public class MlElement{
    private String name;
    private Map<String,String> properties;
    private boolean selfTerminating;

    public String getName(){
        return name;
    }
    public void setName(String name){
        name = this.name;
    }

    public Map<String, String> getProperties(){
        return properties;
    }
    public void setProperties(Map<String, String> properties){
        properties = this.properties;
    }

    public boolean IsSelfTerminating(){
        return selfTerminating;
    }
    public void setSelfTerminating(Boolean selfTerminating){
        selfTerminating = this.selfTerminating;
    }

}

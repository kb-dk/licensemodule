package dk.statsbiblioteket.doms.licensemodule.util;

import java.util.ArrayList;

import dk.kb.license.storage.Attribute;
import dk.kb.license.storage.AttributeGroup;
import dk.kb.license.storage.AttributeValue;
import dk.kb.license.storage.License;
import dk.kb.license.storage.LicenseContent;
import dk.kb.license.storage.Presentation;

public class ChangeDifferenceText {

    
  
    
    private String before;
    private String after;
    
    
    public ChangeDifferenceText(String before, String after) {
        this.before=before;
        this.after=after;
                
    }


    public String getBefore() {
        return before;
    }


    public String getAfter() {
        return after;
    }
    
        
}

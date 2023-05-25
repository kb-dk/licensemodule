package dk.statsbiblioteket.doms.licensemodule.util;

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

package dk.statsbiblioteket.doms.licensemodule.util;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.persistence.Attribute;
import dk.statsbiblioteket.doms.licensemodule.persistence.AttributeGroup;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicenseGroupType;
import dk.statsbiblioteket.doms.licensemodule.persistence.ConfiguredDomLicensePresentationType;
import dk.statsbiblioteket.doms.licensemodule.persistence.License;
import dk.statsbiblioteket.doms.licensemodule.persistence.LicenseContent;


/**
 * This class will generate changetext when a persistent object i updated
 * 
 * 
 * 
 * @author teg
 *
 */
public class LicenseChangelogGenerator {

    private static String NEWLINE = "\n";
    private static final Logger log = LoggerFactory.getLogger(LicenseChangelogGenerator.class);
    /*
    public static void main(String[] args) {
    License l1 = createTestLicenseWithAssociations(1);
    License l2 = createTestLicenseWithAssociations(2);
    l2.setLicenseName("DK changed");
    l2.setDescription_dk("DK changed desc");
    l2.setValidTo("01-01-2222");

    AttributeGroup ag2 = l2.getAttributeGroups().get(0);
    Attribute a2 = ag2.getAttributes().get(0);
    a2.setAttributeName("test changed");

    LicenseContent lc2 = l2.getLicenseContents().get(0);
    Presentation presentation = lc2.getPresentations().remove(0);

    ChangeDifferenceText changes = getChanges(l1, l2);
     System.out.println("old:");
     System.out.println(changes.getBefore());
     System.out.println("after:");
     System.out.println(changes.getAfter());   
    }
     */

    /**
     * Will generate a changeText. 
     * Will only add lines where the license has been changed. 
     * For a new created license licenseOld must be null
     * When deleting license set the licenseNew to null
     * The lines are name, valid dates, description, attributegroup(multiline) and licensecontent(multiline)
     * 
     */
    public static ChangeDifferenceText getLicenseChanges(License licenseOld, License licenseNew) {

        if (licenseOld == null) {
            return getNewLicenseChanges(licenseNew);
        }
        if (licenseNew == null) {
            return getDeleteLicenseChanges(licenseOld);
        }

        StringBuilder oldBuilder = new StringBuilder();
        StringBuilder newBuilder = new StringBuilder();

        String nameOld = getNameText(licenseOld);
        String nameNew = getNameText(licenseNew);        
        addToBuildersIfDifferent(nameOld, nameNew, oldBuilder, newBuilder);

        String dateOld = getValidDateText(licenseOld);
        String dateNew = getValidDateText(licenseNew);        
        addToBuildersIfDifferent(dateOld, dateNew, oldBuilder, newBuilder);


        String descriptionOld = getDescriptionText(licenseOld);
        String descriptionNew = getDescriptionText(licenseNew);
        addToBuildersIfDifferent(descriptionOld , descriptionNew, oldBuilder, newBuilder);


        String attributeGroupsOld = getAttributeGroupsText(licenseOld.getAttributeGroups());
        String attributeGroupsNew = getAttributeGroupsText(licenseNew.getAttributeGroups());
        addToBuildersIfDifferent(attributeGroupsOld , attributeGroupsNew ,oldBuilder, newBuilder);

        String licenseContentsOld = getLicenseContentsText(licenseOld.getLicenseContents());
        String licenseContentsNew = getLicenseContentsText(licenseNew.getLicenseContents());
        addToBuildersIfDifferent(licenseContentsOld , licenseContentsNew ,oldBuilder, newBuilder);


        ChangeDifferenceText changes = new ChangeDifferenceText(oldBuilder.toString(),newBuilder.toString());
        return changes;
    }


    /**
     * Will generate a changeText.  
     * For a new created presentationtype  before must be null
     * When deleting prensentationtype set after to null
     */
    public static ChangeDifferenceText getPresentationTypeChanges(ConfiguredDomLicensePresentationType before,ConfiguredDomLicensePresentationType after) {   

        if (before== null) {
            return getNewPresentationTypeChanges(after);
        }
        if (after == null) {
            return getDeletePresentationTypeChanges(before);
        }        
        
        StringBuilder oldBuilder = new StringBuilder();
        StringBuilder newBuilder = new StringBuilder();


        oldBuilder.append(before.getKey());
        newBuilder.append(after.getKey());

        addToBuildersIfDifferent(getDescriptionText(before), getDescriptionText(after), oldBuilder, newBuilder);
        ChangeDifferenceText changes = new ChangeDifferenceText(oldBuilder.toString(),newBuilder.toString());
        return changes;
    }

    
    /**
     * Will generate a changeText.  
     * For a new created  groupType   before must be null
     * When deleting a grouptype set after to null
     */
    public static ChangeDifferenceText getGroupTypeChanges(ConfiguredDomLicenseGroupType before,ConfiguredDomLicenseGroupType after) {   

        if (before== null) {
            return getNewGroupTypeChanges(after);
        }
        if (after == null) {
            return getDeleteGroupTypeChanges(before);
        }        
        
        StringBuilder oldBuilder = new StringBuilder();
        StringBuilder newBuilder = new StringBuilder();

        addToBuildersIfDifferent(before.toPresentationtString(),after.toPresentationtString(), oldBuilder, newBuilder);
        ChangeDifferenceText changes = new ChangeDifferenceText(oldBuilder.toString(),newBuilder.toString());
        return changes;
    }
    
    public static String getGroupTypeText(ConfiguredDomLicenseGroupType g) {
        StringBuilder b = new StringBuilder();
        b.append("value DK/En:"+g.getValue_dk() +" / "+ g.getValue_en()+NEWLINE);
        b.append("Description dk:"+g.getDescription_dk() +NEWLINE);
        b.append("Description en:"+g.getDescription_en() +NEWLINE);
        b.append("Must group:"+g.isMustGroup() +NEWLINE);
        b.append("Query:"+g.getQuery() +NEWLINE);          
        return b.toString();
    }
        
    /**
     * Full changetext of a license. Used when creating a new license
     * 
     */
    private static ChangeDifferenceText getNewLicenseChanges(License license) {   
        StringBuilder builder = new StringBuilder();

        String name = getNameText(license);
        builder.append(name);

        String date = getValidDateText(license);
        builder.append(date);

        String description = getDescriptionText(license);
        builder.append(description);

        String attributeGroups = getAttributeGroupsText(license.getAttributeGroups());
        builder.append(attributeGroups);

        String licenseContents = getLicenseContentsText(license.getLicenseContents());
        builder.append(licenseContents);

        ChangeDifferenceText changes = new ChangeDifferenceText("",builder.toString());
        return changes;
    }
    
    
    private static ChangeDifferenceText getNewPresentationTypeChanges(ConfiguredDomLicensePresentationType type) {   
        StringBuilder builder = new StringBuilder();
        builder.append(getDescriptionText(type));
        ChangeDifferenceText changes = new ChangeDifferenceText("",builder.toString());
        return changes;        
    }

    private static ChangeDifferenceText getDeletePresentationTypeChanges(ConfiguredDomLicensePresentationType type) {   
        //Just switch change text from nrew
        ChangeDifferenceText change = getNewPresentationTypeChanges(type);
        String after    =change.getAfter();
        String before = change.getBefore();       
        return new ChangeDifferenceText(after, before);
        
    }
        
    private static ChangeDifferenceText getNewGroupTypeChanges(ConfiguredDomLicenseGroupType type) {   
        StringBuilder builder = new StringBuilder();
        builder.append(type.toPresentationtString());
        ChangeDifferenceText changes = new ChangeDifferenceText("",builder.toString());
        return changes;        
    }

    private static ChangeDifferenceText getDeleteGroupTypeChanges(ConfiguredDomLicenseGroupType type) {   
        //Just switch change text from nrew
        ChangeDifferenceText change = getNewGroupTypeChanges(type);
        String after    =change.getAfter();
        String before = change.getBefore();       
        return new ChangeDifferenceText(after, before);        
    }
    
    
  
    private static ChangeDifferenceText getDeleteLicenseChanges(License license) {    
        //Just switch change text from nrew
        ChangeDifferenceText change = getNewLicenseChanges(license);
        String after    =change.getAfter();
        String before = change.getBefore();       
        return new ChangeDifferenceText(after, before);

    }

    private static void addToBuildersIfDifferent(String oldText,String newText, StringBuilder oldBuilder,  StringBuilder newBuilder ) {
        if(!oldText.equals(newText)) {
            oldBuilder.append(oldText);
            newBuilder.append(newText);
        }          

    }



    private static String getValidDateText(License license) {
        return "Valid:"+license.getValidFrom() +" to "+license.getValidTo()+NEWLINE;
    }

    private static String getNameText(License license) {
        return "License name DK/En: "+license.getLicenseName() +" / "+ license.getLicenseName_en()+NEWLINE;    
    }

    public static String getDescriptionText(ConfiguredDomLicensePresentationType type) {
        return "PresentationType value DK/En:"+type.getValue_dk() +" / "+ type.getValue_en() +NEWLINE;

    }
    
    private static String getDescriptionText(License license) {
        return "License description DK/En:"+license.getDescription_dk() +" / "+ license.getDescription_en() +NEWLINE;
    }



    private static String getAttributeGroupsText(ArrayList<AttributeGroup> groups) {
        StringBuilder b = new StringBuilder();
        b.append("Attributes:"+NEWLINE);
        for (AttributeGroup g: groups) {
            b.append("Attribute Group:"+NEWLINE);
            b.append(getAttributeGroupText(g));           
        }

        return b.toString();
    }

    private static String getAttributeGroupText(AttributeGroup g) {
        StringBuilder b = new StringBuilder();

        for (Attribute a : g.getAttributes()) {
            //Generate comma seperated string of values
            String values= a.getValues().stream().map(Object::toString).collect(Collectors.joining(", "));            
            b.append("Attribute:"+a.getAttributeName() +"=["+values +"]");
            b.append(NEWLINE);
        }        

        return b.toString();
    }

    
    private static String  getLicenseContentsText(ArrayList<LicenseContent> lcs) {
        StringBuilder b = new StringBuilder();
        b.append("LicenseContents:"+NEWLINE);
        for (LicenseContent lc:lcs) {            
            b.append(getLicenseContentText(lc));           
        }         
        return b.toString();
    }


    private static String getLicenseContentText(LicenseContent lc) {
        StringBuilder b = new StringBuilder();                
        //Generate comma seperated string of values
        String presentations= lc.getPresentations().stream().map(Object::toString).collect(Collectors.joining(", "));            
        b.append("LicenseContent:"+lc.getName() +"=["+presentations +"]");
        b.append(NEWLINE);

        return b.toString();
    }


}

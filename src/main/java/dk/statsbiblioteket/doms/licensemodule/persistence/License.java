package dk.statsbiblioteket.doms.licensemodule.persistence;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.doms.licensemodule.Util;


public class License extends Persistent{

	//Default to empty string for quick fix so GUI does not show 'null' when creating new license 	
	private String description_dk="";
	private String licenseName="";
	private String description_en="";
	private String licenseName_en="";
	private String validFrom="";
	private String validTo="";
	
	private ArrayList<AttributeGroup> attributeGroups = new  ArrayList<AttributeGroup>();	
	private ArrayList<LicenseContent> licenseContents = new  ArrayList<LicenseContent>();	
	private static final Logger log = LoggerFactory.getLogger(License.class);
	
	public License(){
		
	}
	
	public String toString(){
		return licenseName;
	}
	
	public boolean validateMainFields() {
    
		//Main fields
		if ( licenseName== null || licenseName.length() <3){
        	log.info("License validation error for name:"+licenseName);
            return false;
        }
		
        if ( description_dk == null || description_dk.length() <5){
        	log.info("License validation error for description:"+description_dk);
            return false;
        }
    
             
        boolean validFromOk = Util.validateDateFormat(validFrom);
        if (!validFromOk ){
        	return false;
        }
        boolean validToOk = Util.validateDateFormat(validTo);
        if (!validToOk ){
        	return false;
        }                
 	                      
        return true;
	}
	
	public boolean validateAttributesAndValuesNotNull(){
		//Test all attributes and values are not null/empty
	    
        for (AttributeGroup currentGroup : attributeGroups){        	
        	for (Attribute currentAttribute : currentGroup.getAttributes()){
        		if (!StringUtils.isNotBlank(currentAttribute.getAttributeName())){
        			return false;
        		}
        		for (AttributeValue currentValue : currentAttribute.getValues()){
        			if (!StringUtils.isNotBlank(currentValue.getValue())){
            			return false;
            		}	
        		}        		        		
        	}
        }
		return true;
	}
		
	public String getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(String validFrom) {
		this.validFrom = validFrom;
	}

	public String getValidTo() {
		return validTo;
	}

	public void setValidTo(String validTo) {
		this.validTo = validTo;
	}

	public String getDescription_dk() {
		return description_dk;
	}

	public void setDescription_dk(String description_dk) {
		this.description_dk = description_dk;
	}

	public String getLicenseName() {
		return licenseName;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}
	
	public String getDescription_en() {
		return description_en;
	}

	public void setDescription_en(String description_en) {
		this.description_en = description_en;
	}

	public String getLicenseName_en() {
		return licenseName_en;
	}

	public void setLicenseName_en(String licenseName_en) {
		this.licenseName_en = licenseName_en;
	}

	public ArrayList<AttributeGroup> getAttributeGroups() {
		return attributeGroups;
	}

	public void setAttributeGroups(ArrayList<AttributeGroup> attributeGroups) {
		this.attributeGroups = attributeGroups;
	}

	public ArrayList<LicenseContent> getLicenseContents() {
		return licenseContents;
	}

	public void setLicenseContents(ArrayList<LicenseContent> licenseContents) {
		this.licenseContents = licenseContents;
	}	
}

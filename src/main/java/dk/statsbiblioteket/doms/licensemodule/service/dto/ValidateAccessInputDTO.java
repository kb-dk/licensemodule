package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ValidateAccessInputDTO {
	
		
/* Serialization example
	 
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <validateAccessInputDTO>
      <attributes>
         <attribute>wayf.schacHomeOrganization</attribute>
         <values>sb.dk</values>
         <values>test.dk</values>
      </attributes>
      <groups>Individuelt forbud</groups>
      <groups>Klausuleret</groups>
      <presentationType>images</presentationType>
   </validateAccessInputDTO> 

*/
	
	
    private ArrayList<String> groups = new ArrayList<String>();
    private String presentationType;
	
    public  ValidateAccessInputDTO(){    	
    }
    
    private ArrayList<UserObjAttributeDTO> attributes = new ArrayList<UserObjAttributeDTO>();

	public ArrayList<UserObjAttributeDTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<UserObjAttributeDTO> attributes) {
		this.attributes = attributes;
	}
   
	public ArrayList<String> getGroups() {
		return groups;
	}
	public void setGroups(ArrayList<String> groups) {
		this.groups = groups;
	}
	public String getPresentationType() {
		return presentationType;
	}
	public void setPresentationType(String presentationType) {
		this.presentationType = presentationType;
	}
    
	
	
}

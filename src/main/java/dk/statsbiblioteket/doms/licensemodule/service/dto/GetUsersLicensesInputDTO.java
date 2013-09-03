package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

/*
Serialization exapmple
XML
<getUsersLicensesInputDTO>
    <attributes>
        <attribute>attribut_store.MediestreamFullAccess</attribute>
        <values>yes</values>
    </attributes>
    <english>false</english>
</getUsersLicensesInputDTO>
 
JSON:
{"attributes":[{"attribute":"attribut_store.MediestreamFullAccess","values":["true"]}],"locale":"da"}

 */


@XmlRootElement
public class GetUsersLicensesInputDTO {

	private ArrayList<UserObjAttributeDTO> attributes = new ArrayList<UserObjAttributeDTO>();	
	private String locale;
		
	public GetUsersLicensesInputDTO(){	
	}
	
    public GetUsersLicensesInputDTO( ArrayList<UserObjAttributeDTO>  attributes , String locale){    	
	   this.attributes=attributes; 		
	   this.locale=locale;
	}
	
	public ArrayList<UserObjAttributeDTO> getAttributes() {
		return attributes;
	}
	public void setAttributes( ArrayList<UserObjAttributeDTO> attributes) {
		this.attributes = attributes;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	
		
}

package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

/*
Serialization example
<getUserQueryInputDTO>
    <attributes>
        <attribute>attribut_store.MediestreamFullAccess</attribute>
        <values>yes</values>
    </attributes>
    <presentationType>images</presentationType>
</getUserQueryInputDTO>
 */


@XmlRootElement
public class GetUserQueryInputDTO {

	private ArrayList<UserObjAttributeDTO> attributes = new ArrayList<UserObjAttributeDTO>();
	private String presentationType;
	
	public GetUserQueryInputDTO(){
		
	}
	
    public GetUserQueryInputDTO( ArrayList<UserObjAttributeDTO>  attributes, String presentationType){
	   this.attributes=attributes; 		
       this.presentationType= presentationType;
    }
	
	public ArrayList<UserObjAttributeDTO> getAttributes() {
		return attributes;
	}
	public void setAttributes( ArrayList<UserObjAttributeDTO> attributes) {
		this.attributes = attributes;
	}

	public String getPresentationType() {
		return presentationType;
	}

	public void setPresentationType(String presentationType) {
		this.presentationType = presentationType;
	}

	
	
}

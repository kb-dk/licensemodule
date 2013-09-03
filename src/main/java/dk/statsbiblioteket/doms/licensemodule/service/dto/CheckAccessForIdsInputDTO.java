package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class CheckAccessForIdsInputDTO {

	private ArrayList<UserObjAttributeDTO> attributes = new ArrayList<UserObjAttributeDTO>();
	private String presentationType;
    private ArrayList<String> ids;
	
	public CheckAccessForIdsInputDTO(){

	}
	
	public ArrayList<UserObjAttributeDTO> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<UserObjAttributeDTO> attributes) {
		this.attributes = attributes;
	}

	public String getPresentationType() {
		return presentationType;
	}

	public void setPresentationType(String presentationType) {
		this.presentationType = presentationType;
	}

	public ArrayList<String> getIds() {
		return ids;
	}

	public void setIds(ArrayList<String> ids) {
		this.ids = ids;
	}

}

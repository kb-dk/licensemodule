package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserGroupDTO {

	private String groupName;
	private ArrayList<String> presentationTypes;
	
	public UserGroupDTO(){}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public ArrayList<String> getPresentationTypes() {
		return presentationTypes;
	}

	public void setPresentationTypes(ArrayList<String> presentationTypes) {
		this.presentationTypes = presentationTypes;
	}

	@Override
	public String toString() {
		return "UserGroupDTO [groupName=" + groupName + ", presentationTypes=" + presentationTypes + "]";
	}	

}


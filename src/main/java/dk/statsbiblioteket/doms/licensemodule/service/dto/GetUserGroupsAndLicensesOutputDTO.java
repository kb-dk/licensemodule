package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetUserGroupsAndLicensesOutputDTO {

	ArrayList<UserGroupDTO> groups = new ArrayList<UserGroupDTO>(); 
	private ArrayList<LicenseOverviewDTO> licenses = null;
	private ArrayList<String> allPresentationTypes = new ArrayList<String>();
	private ArrayList<String> allGroups = new ArrayList<String>();
	
	public GetUserGroupsAndLicensesOutputDTO(){		
	}
	
	public ArrayList<LicenseOverviewDTO> getLicenses() {
		return licenses;
	}

	public void setLicenses(ArrayList<LicenseOverviewDTO> licenses) {
		this.licenses = licenses;
	}
		
	public ArrayList<UserGroupDTO> getGroups() {
		return groups;
	}

	public void setGroups(ArrayList<UserGroupDTO> groups) {
		this.groups = groups;
	}

	public ArrayList<String> getAllPresentationTypes() {
		return allPresentationTypes;
	}

	public void setAllPresentationTypes(ArrayList<String> allPresentationTypes) {
		this.allPresentationTypes = allPresentationTypes;
	}

	public ArrayList<String> getAllGroups() {
		return allGroups;
	}

	public void setAllGroups(ArrayList<String> allGroups) {
		this.allGroups = allGroups;
	}		
}

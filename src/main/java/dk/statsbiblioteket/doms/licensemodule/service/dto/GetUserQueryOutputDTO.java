package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetUserQueryOutputDTO {

	
	private ArrayList<String> userLicenseGroups = new ArrayList<String>();
	private ArrayList<String> userNotInMustGroups = new ArrayList<String>();
	private String query;
	
	public GetUserQueryOutputDTO(){		
	}

	public ArrayList<String> getUserLicenseGroups() {
		return userLicenseGroups;
	}

	public void setUserLicenseGroups(ArrayList<String> userLicenseGroups) {
		this.userLicenseGroups = userLicenseGroups;
	}

	public ArrayList<String> getUserNotInMustGroups() {
		return userNotInMustGroups;
	}

	public void setUserNotInMustGroups(ArrayList<String> userNotInMustGroups) {
		this.userNotInMustGroups = userNotInMustGroups;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
	
}

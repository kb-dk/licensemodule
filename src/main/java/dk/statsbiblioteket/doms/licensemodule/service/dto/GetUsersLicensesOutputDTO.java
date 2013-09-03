package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GetUsersLicensesOutputDTO  {

	private ArrayList<LicenseOverviewDTO> licenses = null;

	public GetUsersLicensesOutputDTO(){		
	}
	
	public ArrayList<LicenseOverviewDTO> getLicenses() {
		return licenses;
	}

	public void setLicenses(ArrayList<LicenseOverviewDTO> licenses) {
		this.licenses = licenses;
	}
	
	
	
	
}

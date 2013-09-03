package dk.statsbiblioteket.doms.licensemodule.service.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LicenseOverviewDTO {

	private String name;
	private String description;
	private String validFrom;
	private String validTo;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	@Override
	public String toString() {
		return "LicenseOverviewDTO [name=" + name + ", description=" + description + ", validFrom=" + validFrom + ", validTo=" + validTo + "]";
	}
	
}

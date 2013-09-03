package dk.statsbiblioteket.doms.licensemodule.service.dto;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ValidateAccessOutputDTO {

	private boolean access;
	
	public ValidateAccessOutputDTO(){		
	}

	public ValidateAccessOutputDTO(boolean access){
		this.access=access;
	}
	
	public boolean isAccess() {
		return access;
	}

	public void setAccess(boolean access) {
		this.access = access;
	}
		
}

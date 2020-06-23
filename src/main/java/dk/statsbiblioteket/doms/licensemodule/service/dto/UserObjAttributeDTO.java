package dk.statsbiblioteket.doms.licensemodule.service.dto;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserObjAttributeDTO {
	
	private String attribute;
	private ArrayList<String> values = new  ArrayList<String>();

	public UserObjAttributeDTO(){		
	}
	
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public ArrayList<String> getValues() {
		return values;
	}
	public void setValues(ArrayList<String> values) {
		this.values = values;
	}

  @Override
  public String toString() {
    return "UserObjAttributeDTO [attribute=" + attribute + ", values=" + values + "]";
  }
	
	
}

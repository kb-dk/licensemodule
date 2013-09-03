package dk.statsbiblioteket.doms.licensemodule.persistence;

import java.util.ArrayList;

public class LicenseContent extends Persistent{

	private String name;
	
	private ArrayList<Presentation> presentations = new ArrayList<Presentation>();
	
	public LicenseContent(){
		
	}

	public LicenseContent(String name){
		this.name=name;
	}
	
	public ArrayList<Presentation> getPresentations() {
		return presentations;
	}

	public void setPresentations(ArrayList<Presentation> presentations) {
		this.presentations = presentations;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
	
}

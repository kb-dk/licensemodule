package dk.statsbiblioteket.doms.licensemodule.persistence;

public class Presentation extends Persistent{

	private String key;
	
	
	public Presentation() {
	
	}
	
	public Presentation(String key) {
		super();
		this.key = key;
	}
	
	
	public String getKey() {
		return key;
	}

	public void setKey(String name) {
		this.key = name;
	}

	
}

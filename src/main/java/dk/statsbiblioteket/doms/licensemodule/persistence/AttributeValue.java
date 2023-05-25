package dk.statsbiblioteket.doms.licensemodule.persistence;

public class AttributeValue extends Persistent {

	private String value;  

	public AttributeValue(String value){
		this.value=value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	  @Override
	    public String toString() {
	        return value;
	    }

}

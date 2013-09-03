package dk.statsbiblioteket.doms.licensemodule.persistence;

import java.util.ArrayList;

public class AttributeGroup extends Persistent{

	private int number;	

	private ArrayList<Attribute> attributes = new  ArrayList<Attribute>();

	public AttributeGroup(int number){
		this.number=number;

	}

	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<Attribute> attributes) {
		this.attributes = attributes;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

}

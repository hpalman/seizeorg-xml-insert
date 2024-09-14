package com.example.model.record;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class COLUMN {
	private String name;
	@XmlAttribute(name="NAME")
    public String getName() {
        return name;
    }
	public void setName(String name) {
        this.name = name;
    }

	private String value;
	@XmlValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}


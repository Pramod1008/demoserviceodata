package com.exampleodata.demo.model;

public class PropertyList {
    public String PropertiesName;
    public String PropertiesType;

    public String getPropertiesName() {
        return PropertiesName;
    }

    public void setPropertiesName(String propertiesName) {
        PropertiesName = propertiesName;
    }

    public String getPropertiesType() {
        return PropertiesType;
    }

    public void setPropertiesType(String propertiesType) {
        PropertiesType = propertiesType;
    }

    public PropertyList() {
    }

    public PropertyList(String propertiesName, String propertiesType) {
        PropertiesName = propertiesName;
        PropertiesType = propertiesType;
    }
}

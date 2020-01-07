package com.exampleodata.demo.model;

import java.util.List;

public class EntityListClass {
   public String EntityTypeName;
   public String EntitySet;
   //public List<PropertyList> propertyList;
    public String PropertiesName;
    public String PropertiesType;

    public EntityListClass(String entityTypeName, String entitySet, String propertiesName, String propertiesType, String navigationProperty) {
        EntityTypeName = entityTypeName;
        EntitySet = entitySet;
        PropertiesName = propertiesName;
        PropertiesType = propertiesType;
        NavigationProperty = navigationProperty;
    }

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

    public String NavigationProperty ;

    public String getEntityTypeName() {
        return EntityTypeName;
    }

    public String getNavigationProperty() {
        return NavigationProperty;
    }

    public void setNavigationProperty(String navigationProperty) {
        NavigationProperty = navigationProperty;
    }

    public void setEntityTypeName(String entityTypeName) {
        EntityTypeName = entityTypeName;
    }

    public String getEntitySet() {
        return EntitySet;
    }

    public void setEntitySet(String entitySet) {
        EntitySet = entitySet;
    }

    public EntityListClass() {
    }

}

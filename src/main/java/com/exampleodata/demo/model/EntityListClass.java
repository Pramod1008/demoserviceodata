package com.exampleodata.demo.model;

import java.util.List;

public class EntityListClass {
   public String EntityTypeName;
   public String EntitySet;
   public List<PropertyList> propertyList;
   public String NavigationProperty ;

    public String getEntityTypeName() {
        return EntityTypeName;
    }

    public EntityListClass(String entityTypeName, String entitySet, List<PropertyList> propertyList, String navigationProperty) {
        EntityTypeName = entityTypeName;
        EntitySet = entitySet;
        this.propertyList = propertyList;
        NavigationProperty = navigationProperty;
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

    public List<PropertyList> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<PropertyList> propertyList) {
        this.propertyList = propertyList;
    }

    public EntityListClass() {
    }

}

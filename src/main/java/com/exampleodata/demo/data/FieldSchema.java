package com.exampleodata.demo.data;

public class FieldSchema {
    private final String name;
    private final String type;
    public FieldSchema(String name,String type){
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}

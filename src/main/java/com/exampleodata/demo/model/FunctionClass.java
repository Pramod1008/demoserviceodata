package com.exampleodata.demo.model;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

public class FunctionClass {
    public String FUNCTION_NAME;
    public FullQualifiedName FUNCTION_NAME_FQN;
    // Function Parameters
    public String PARAMETER_AMOUNT;

    //Query
    public String QUERY ;

    //Return Type
    public FullQualifiedName SET_RETURN_TYPE ;

    //set collection
    public boolean SET_COLLECTION;

    //set Bound
    public boolean SET_BOUND;

    public String getFUNCTION_NAME() {
        return FUNCTION_NAME;
    }

    public void setFUNCTION_NAME(String FUNCTION_NAME) {
        this.FUNCTION_NAME = FUNCTION_NAME;
    }

    public FullQualifiedName getFUNCTION_NAME_FQN() {
        return FUNCTION_NAME_FQN;
    }

    public void setFUNCTION_NAME_FQN(FullQualifiedName FUNCTION_NAME_FQN) {
        this.FUNCTION_NAME_FQN = FUNCTION_NAME_FQN;
    }

    public String getPARAMETER_AMOUNT() {
        return PARAMETER_AMOUNT;
    }

    public void setPARAMETER_AMOUNT(String PARAMETER_AMOUNT) {
        this.PARAMETER_AMOUNT = PARAMETER_AMOUNT;
    }

    public String getQUERY() {
        return QUERY;
    }

    public void setQUERY(String QUERY) {
        this.QUERY = QUERY;
    }

    public FullQualifiedName getSET_RETURN_TYPE() {
        return SET_RETURN_TYPE;
    }

    public void setSET_RETURN_TYPE(FullQualifiedName SET_RETURN_TYPE) {
        this.SET_RETURN_TYPE = SET_RETURN_TYPE;
    }

    public boolean isSET_COLLECTION() {
        return SET_COLLECTION;
    }

    public void setSET_COLLECTION(boolean SET_COLLECTION) {
        this.SET_COLLECTION = SET_COLLECTION;
    }

    public boolean isSET_BOUND() {
        return SET_BOUND;
    }

    public void setSET_BOUND(boolean SET_BOUND) {
        this.SET_BOUND = SET_BOUND;
    }

    public FunctionClass() {
    }

    public FunctionClass(String FUNCTION_NAME, FullQualifiedName FUNCTION_NAME_FQN, String PARAMETER_AMOUNT, String QUERY, FullQualifiedName SET_RETURN_TYPE, boolean SET_COLLECTION, boolean SET_BOUND) {
        this.FUNCTION_NAME = FUNCTION_NAME;
        this.FUNCTION_NAME_FQN = FUNCTION_NAME_FQN;
        this.PARAMETER_AMOUNT = PARAMETER_AMOUNT;
        this.QUERY = QUERY;
        this.SET_RETURN_TYPE = SET_RETURN_TYPE;
        this.SET_COLLECTION = SET_COLLECTION;
        this.SET_BOUND = SET_BOUND;
    }
}

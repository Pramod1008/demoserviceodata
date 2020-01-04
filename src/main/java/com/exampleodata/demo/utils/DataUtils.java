package com.exampleodata.demo.utils;


import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class DataUtils {
    private static final Logger logger = LoggerFactory.getLogger(DataUtils.class);
    @SuppressWarnings("serial")
    private static final Map<String, FullQualifiedName> dataTypeObj = new HashMap<>();

    public static Map<String,FullQualifiedName> getDataTypeObj(){
        if(dataTypeObj.isEmpty()){
            initDataTypeObj();
        }
        return dataTypeObj;
    }

    private static void initDataTypeObj() {
        dataTypeObj.put("integer", EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        dataTypeObj.put("int", EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        dataTypeObj.put("string", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("long", EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
        //Here decimal as showed as string because hive decimal doesn't get cast in EdmPrimitiveTypeKind.Decimal
        dataTypeObj.put("bigdecimal", EdmPrimitiveTypeKind.Double.getFullQualifiedName());
        dataTypeObj.put("decimal", EdmPrimitiveTypeKind.Double.getFullQualifiedName());
        dataTypeObj.put("timestamp", EdmPrimitiveTypeKind.Date.getFullQualifiedName());
        dataTypeObj.put("date", EdmPrimitiveTypeKind.Date.getFullQualifiedName());
        dataTypeObj.put("datetime", EdmPrimitiveTypeKind.Date.getFullQualifiedName());
        dataTypeObj.put("mediumtext", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("double", EdmPrimitiveTypeKind.Double.getFullQualifiedName());
        dataTypeObj.put("longtext", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("bigint", EdmPrimitiveTypeKind.Int64.getFullQualifiedName());
        dataTypeObj.put("char", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("varchar", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("varchar2", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("text", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("mediumblob", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("longblob", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("clob", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("tinytext", EdmPrimitiveTypeKind.String.getFullQualifiedName());
        dataTypeObj.put("set", EdmPrimitiveTypeKind.Stream.getFullQualifiedName());
        dataTypeObj.put("float", EdmPrimitiveTypeKind.Decimal.getFullQualifiedName());
        dataTypeObj.put("mediumint", EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
        dataTypeObj.put("boolean", EdmPrimitiveTypeKind.Boolean.getFullQualifiedName());
    }

    public static URI createId(String entitySetName, Object id) {
        try {
            logger.info("Creating id");
            return new URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }
}

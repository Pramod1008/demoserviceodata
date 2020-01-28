package com.exampleodata.demo.model;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;


import com.exampleodata.demo.utils.DataUtils;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DemoEdmProviderForAllForAction extends CsdlAbstractEdmProvider {
 private static final Logger LOG = LoggerFactory.getLogger(DemoEdmProviderForAllForAction.class);
    // Service Namespace
    public static final String NAMESPACE = "OData.Demo";

    // EDM Container
    public static final String CONTAINER_NAME = "Container";
    public static final FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    public String ET_NAME = null;
    public FullQualifiedName ET_FQN =null;

    public String NV_NAME=null;
    public FullQualifiedName NV_NAME_FQN=null;
    public String NV_BIND_TARGET=null;

    public String ES_NAME = null;

    HashMap<String, FunctionClass> functionLinkedList=null;
    LinkedList getEntityList=null;

    public DemoEdmProviderForAllForAction() throws IOException, ParseException {
        getEntityList=getEntityList();
    }

    public LinkedList getEntityList() throws IOException, ParseException {
        JSONParser jsonParser=new JSONParser();
        Object obj= jsonParser.parse(new FileReader("D:\\Pramod\\Document\\git\\demoserviceodata\\src\\main\\java\\com\\exampleodata\\demo\\data\\EntitySet.json"));

        JSONArray jsonArray=(JSONArray) obj;

        int length = jsonArray.size();

        LinkedList entityList = new LinkedList();
        HashMap<String,EntityListClass> entityListMap = new HashMap<String,EntityListClass>();

        for (int i =0; i< length; i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            Set s = jsonObject.entrySet();
            Iterator iter = s.iterator();

            HashMap hm = new HashMap();
            EntityListClass entityListClass=new EntityListClass();
            PropertyList propertyList=new PropertyList();
            while(iter.hasNext()){
                Map.Entry me = (Map.Entry) iter.next();
                hm.put(me.getKey(), me.getValue());

                switch (me.getKey().toString()){
                    case "EntityType":entityListClass.setEntityTypeName(me.getValue().toString());
                        break;
                    case "EntitySet":entityListClass.setEntitySet(me.getValue().toString());
                        break;
//                    case "Properties":entityListClass.setEntityTypeName(hm.get("PropertiesType").toString());
//                    entityListClass.setPropertiesName(hm.get("PropertiesName").toString());
//                      break;
                    case "NavigationProperty":entityListClass.setNavigationProperty(me.getValue().toString());
                                            break;
                }

            }
            entityList.add(hm);
        }
        return entityList;
    }

    public HashMap<String, FunctionClass> getFunctionList() throws IOException, ParseException {
        JSONParser jsonParser=new JSONParser();
        Object obj= jsonParser.parse(new FileReader("D:\\Pramod\\Document\\git\\demoserviceodata\\src\\main\\java\\com\\exampleodata\\demo\\data\\Example.json"));

        JSONArray jsonArray=(JSONArray) obj;

        int length = jsonArray.size();

        HashMap<String,FunctionClass> functionMap = new HashMap<String,FunctionClass>();

        for (int i =0; i< length; i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            Set s = jsonObject.entrySet();
            Iterator iter = s.iterator();

            //HashMap hm = new HashMap();

            FunctionClass functionClass=new FunctionClass();

            while(iter.hasNext()){
                Map.Entry me = (Map.Entry) iter.next();
                //hm.put(me.getKey(), me.getValue());

                switch (me.getKey().toString()){
                    case "FUNCTION_NAME":functionClass.setFUNCTION_NAME(me.getValue().toString());
                                        functionClass.setFUNCTION_NAME_FQN(new FullQualifiedName(NAMESPACE,me.getValue().toString()));
                        break;
                    case "PARAMETER_AMOUNT":functionClass.setPARAMETER_AMOUNT(me.getValue().toString());
                        break;
                    case "QUERY":functionClass.setQUERY(me.getValue().toString());
                        break;
                    case "SET_RETURN_TYPE" :functionClass.setSET_RETURN_TYPE(new FullQualifiedName(NAMESPACE, me.getValue().toString()));
                        break;
                    case "SET_COLLECTION":functionClass.setSET_COLLECTION((boolean)me.getValue());
                        break;
                    case "SET_BOUND" :functionClass.setSET_BOUND((boolean)me.getValue());
                        break;
                }
            }
            //functionList.add(hm);

            functionMap.put(functionClass.getFUNCTION_NAME().toString(),functionClass);
        }
        //return functionList;
        return functionMap;
    }

    @Override
    public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) {
        final List<CsdlFunction> functions = new ArrayList<CsdlFunction>();

        for (Map.Entry<String, FunctionClass> functionEntry : functionLinkedList.entrySet()) {
            if (functionName.equals(functionEntry.getValue().getFUNCTION_NAME_FQN())){
                // Create the parameter for the function
                final List<CsdlParameter> parameters = new ArrayList<CsdlParameter>();
                if(!functionEntry.getValue().getPARAMETER_AMOUNT().trim().isEmpty()) {

                    CsdlParameter parameter = new CsdlParameter();
                    parameter.setName(functionEntry.getValue().getPARAMETER_AMOUNT());
                    parameter.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
                    parameters.add(parameter);
                }
                // Create the return type of the function
                final CsdlReturnType returnType = new CsdlReturnType();
                returnType.setCollection(functionEntry.getValue().isSET_COLLECTION()).setType(functionEntry.getValue().getSET_RETURN_TYPE());

                // Create the function
                final CsdlFunction function = new CsdlFunction();
                function.setName(functionEntry.getKey())
                        .setParameters(parameters)
                        .setBound(functionEntry.getValue().isSET_BOUND()).setReturnType(returnType);
                functions.add(function);

                return functions;
            }
        }

        return null;
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) {
        if(entityContainer.equals(CONTAINER)) {

            for (Map.Entry<String, FunctionClass> functionEntry : functionLinkedList.entrySet()) {
                if (functionImportName.equals(functionEntry.getKey()))
                {
                    for(int j=0;j<getEntityList.size();j++) {
                        HashMap hmES = (HashMap) getEntityList.get(j);
                        if(functionEntry.getValue().getSET_RETURN_TYPE().getName().equals(hmES.get("EntityType").toString()))
                        {
                            if(hmES.containsKey("EntitySet")){
                                ES_NAME= (String) hmES.get("EntitySet");

                                return new CsdlFunctionImport()
                                        .setName(functionImportName)
                                        .setFunction(functionEntry.getValue().getFUNCTION_NAME_FQN())
                                        .setEntitySet(ES_NAME)
                                        .setIncludeInServiceDocument(true);
                            }else {
                                return new CsdlFunctionImport()
                                        .setName(functionImportName)
                                        .setFunction(functionEntry.getValue().getFUNCTION_NAME_FQN())
                                        .setIncludeInServiceDocument(true);
                            }
                        }
                    }
                }

            }
        }
        return null;
    }

    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {

        // this method is called for each EntityType that are configured in the Schema
        CsdlEntityType entityType = null;
        List<CsdlProperty> csdlPropertyList =null;
        int localSet=0;

        for(int i=0;i<getEntityList.size();i++) {
            HashMap hm = (HashMap) getEntityList.get(i);
            if(hm.containsKey("EntityType") && entityTypeName.getName().equals(hm.get("EntityType"))){
                LOG.info("Create EntityType"+entityTypeName);
                csdlPropertyList=new ArrayList<>();
                if(hm.containsKey("Properties"))
                {
                    JSONArray jsonArray= (JSONArray) hm.get("Properties");
                    int length = jsonArray.size();

                    CsdlProperty csdlProperty =null;
                    // create PropertyRef for Key element
                    CsdlPropertyRef propertyRef = null;

                    for (int j =0; j< length; j++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                        Set s = jsonObject.entrySet();
                        Iterator iter = s.iterator();
                        csdlProperty=new CsdlProperty();
                        while(iter.hasNext()){
                            Map.Entry me = (Map.Entry) iter.next();

                           if( me.getKey().equals("PropertiesType"))
                           {
                               for (Map.Entry<String,FullQualifiedName> entry: DataUtils.getDataTypeObj().entrySet()) {
                                   if(me.getValue().toString().toLowerCase().contains(entry.getKey())){
                                       csdlProperty.setType(entry.getValue());
                                   }
                               }
                           }
                            if( me.getKey().equals("PropertiesName"))
                            {
                                if(me.getValue().toString().equalsIgnoreCase("ID"))
                                {
                                    localSet=1;
                                }
                                csdlProperty.setName(me.getValue().toString());
                            }
                        }
                        csdlPropertyList.add(csdlProperty);
                    }
                    propertyRef = new CsdlPropertyRef();
                        if(localSet==1)
                        {
                               propertyRef.setName("ID");
                        }else
                        {
                              propertyRef.setName("0");
                        }


                    List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
                    if(hm.containsKey("NavigationProperty"))
                    {
                        NV_NAME= (String) hm.get("NavigationProperty");
                        NV_NAME_FQN=new FullQualifiedName(NAMESPACE,NV_NAME);

                        LOG.info("Entity Name "+entityTypeName+" NV Name "+NV_NAME+" Set Partner "+entityTypeName.getName());

                        // navigation property: many-to-one, null not allowed (product must have a category)
                        CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName(NV_NAME)
                                .setType(NV_NAME_FQN).setNullable(true)
                                .setPartner(entityTypeName.getName());
                        navPropList.add(navProp);

                    }

                    entityType = new CsdlEntityType();
                    entityType.setName(entityTypeName.getName());
                    entityType.setProperties(csdlPropertyList);

                    if(!propertyRef.getName().equalsIgnoreCase("0"))
                    entityType.setKey(Arrays.asList(propertyRef));

                    if(navPropList.size()!=0)
                    entityType.setNavigationProperties(navPropList);

                    return entityType;
                }
            }
            }

        return null;
    }

    public CsdlEntityType getNewEntityType(FullQualifiedName entityTypeName,EntityCollection entityCollection) {

        // this method is called for each EntityType that are configured in the Schema
        CsdlEntityType entityType = null;
        List<CsdlProperty> csdlPropertyList =null;

        List<String> listProperties=new ArrayList<>();
        int len=entityCollection.getEntities().get(0).getProperties().size();
        for(int k=0 ;k<len;k++)
        {
            listProperties.add(entityCollection.getEntities().get(0).getProperties().get(k).getName());
        }
        LOG.info(listProperties.toString());

        int localSet=0;

        for(int i=0;i<getEntityList.size();i++) {
            HashMap hm = (HashMap) getEntityList.get(i);
            if(hm.containsKey("EntityType") && entityTypeName.getName().equals(hm.get("EntityType"))){
                LOG.info("Create EntityType"+entityTypeName);
                csdlPropertyList=new ArrayList<>();
                if(hm.containsKey("Properties"))
                {
                    JSONArray jsonArray= (JSONArray) hm.get("Properties");
                    int length = jsonArray.size();

                    CsdlProperty csdlProperty =null;
                    // create PropertyRef for Key element
                    CsdlPropertyRef propertyRef = null;

                    for (int j =0; j< length; j++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(j);
                        Set s = jsonObject.entrySet();
                        Iterator iterMap = s.iterator();
                        csdlProperty=new CsdlProperty();
                        HashMap hmList = new HashMap();
                        while(iterMap.hasNext()) {
                            Map.Entry me = (Map.Entry) iterMap.next();
                            hmList.put(me.getKey(), me.getValue());
                            LOG.info(hmList.keySet().toString());
                            LOG.info(hmList.values().toString());
                            LOG.info(me.getKey().equals("PropertiesName") +" " +listProperties.contains(hmList.get("PropertiesName")));
                            if (me.getKey().equals("PropertiesName") && listProperties.contains(hmList.get("PropertiesName")))
                            {
                                if(me.getValue().toString().equalsIgnoreCase("ID"))
                                {
                                    localSet=1;
                                }
                                csdlProperty.setName(me.getValue().toString());
                                LOG.info(hmList.get("PropertiesType").toString());
                                 for (Map.Entry<String,FullQualifiedName> entry: DataUtils.getDataTypeObj().entrySet()) {
                                    if(hmList.get("PropertiesType").toString().toLowerCase().contains(entry.getKey())){
                                        csdlProperty.setType(entry.getValue());
                                    }
                                }
                                csdlPropertyList.add(csdlProperty);
                            }
                        }

                    }
                    propertyRef = new CsdlPropertyRef();
                    if(localSet==1)
                    {
                        propertyRef.setName("ID");
                    }else
                    {
                        propertyRef.setName("0");
                    }


                    List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
                    if(hm.containsKey("NavigationProperty"))
                    {
                        NV_NAME= (String) hm.get("NavigationProperty");
                        NV_NAME_FQN=new FullQualifiedName(NAMESPACE,NV_NAME);

                        LOG.info("Entity Name "+entityTypeName+" NV Name "+NV_NAME+" Set Partner "+entityTypeName.getName());

                        // navigation property: many-to-one, null not allowed (product must have a category)
                        CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName(NV_NAME)
                                .setType(NV_NAME_FQN).setNullable(true)
                                .setPartner(entityTypeName.getName());
                        navPropList.add(navProp);

                    }

                    entityType = new CsdlEntityType();
                    entityType.setName(entityTypeName.getName());
                    entityType.setProperties(csdlPropertyList);

                    if(!propertyRef.getName().equalsIgnoreCase("0"))
                        entityType.setKey(Arrays.asList(propertyRef));

                    if(navPropList.size()!=0)
                        entityType.setNavigationProperties(navPropList);

                    return entityType;
                }
            }
        }

        return null;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

        CsdlEntitySet entitySet = null;

        if (entityContainer.equals(CONTAINER)) {

            for(int i=0;i<getEntityList.size();i++) {
                HashMap hm = (HashMap) getEntityList.get(i);
                if(hm.containsKey("EntitySet") && entitySetName.equalsIgnoreCase(hm.get("EntitySet").toString())){
                    ES_NAME= (String) hm.get("EntitySet");
                    ET_NAME= (String) hm.get("EntityType");
                    ET_FQN= new FullQualifiedName(NAMESPACE, ET_NAME);

                    NV_NAME= (String) hm.get("NavigationProperty");
                    NV_NAME_FQN=new FullQualifiedName(NAMESPACE,NV_NAME);

                    entitySet = new CsdlEntitySet();
                    entitySet.setName(ES_NAME);
                    entitySet.setType(ET_FQN);

                    if(NV_NAME!=null)
                    {
                        for(int j=0;j<getEntityList.size();j++) {
                            HashMap hmNVBindTarget = (HashMap) getEntityList.get(j);
                            if(NV_NAME.equalsIgnoreCase(hmNVBindTarget.get("EntityType").toString())){
                                NV_BIND_TARGET=hmNVBindTarget.get("EntitySet").toString();
                            }
                        }

                        LOG.info("Target Entity Set "+NV_BIND_TARGET+" Entity Type "+NV_NAME);
                        // navigation
                        CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                        navPropBinding.setTarget(NV_BIND_TARGET); // the target entity set, where the navigation property points to
                        navPropBinding.setPath(NV_NAME); // the path from entity type to navigation property
                        List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                        navPropBindingList.add(navPropBinding);
                        entitySet.setNavigationPropertyBindings(navPropBindingList);
                    }
                    return entitySet;
                }
            }
        }

        return null;

    }

     @Override
    public List<CsdlSchema> getSchemas() {
        try {
            // create Schema
            CsdlSchema schema = new CsdlSchema();
            schema.setNamespace(NAMESPACE);

            // add EntityTypes
            List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();

            for (int i = 0; i < getEntityList.size(); i++) {
                HashMap hm = (HashMap) getEntityList.get(i);
                if (hm.containsKey("EntityType")) {
                    ET_NAME = (String) hm.get("EntityType");
                    ET_FQN = new FullQualifiedName(NAMESPACE, ET_NAME);

                    entityTypes.add(getEntityType(ET_FQN));
                }
            }
            schema.setEntityTypes(entityTypes);


            List<CsdlFunction> functions = new ArrayList<CsdlFunction>();
            functionLinkedList = getFunctionList();
            for (Map.Entry<String, FunctionClass> functionEntry : functionLinkedList.entrySet()) {
                functions.addAll(getFunctions(functionEntry.getValue().getFUNCTION_NAME_FQN()));
            }
            schema.setFunctions(functions);

            // add EntityContainer
            schema.setEntityContainer(getEntityContainer());

            // finally
            List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
            schemas.add(schema);

            return schemas;
        }catch (Exception e){
            LOG.error(e.getMessage());
        }
        return null;
    }

    public CsdlEntityContainer getEntityContainer() {
        try{
        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();

        for(int i=0;i<getEntityList.size();i++) {
            HashMap hm = (HashMap) getEntityList.get(i);
            if(hm.containsKey("EntitySet")){
                ES_NAME= (String) hm.get("EntitySet");
                entitySets.add(getEntitySet(CONTAINER, ES_NAME));
            }
        }

        // Create function imports
        List<CsdlFunctionImport> functionImports = new ArrayList<CsdlFunctionImport>();

        functionLinkedList = getFunctionList();
        for (Map.Entry<String, FunctionClass> functionEntry : functionLinkedList.entrySet()) {
            functionImports.add(getFunctionImport(CONTAINER, functionEntry.getKey()));
        }
        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);
        entityContainer.setFunctionImports(functionImports);
        //entityContainer.setActionImports(actionImports);

        return entityContainer;
    }catch (Exception e){
        LOG.error(e.getMessage());
    }
        return null;
    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName) {

        // This method is invoked when displaying the service document at
        // e.g. http://localhost:8080/DemoService/DemoService.svc
        if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
            CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
            entityContainerInfo.setContainerName(CONTAINER);
            return entityContainerInfo;
        }
        return null;
    }
}

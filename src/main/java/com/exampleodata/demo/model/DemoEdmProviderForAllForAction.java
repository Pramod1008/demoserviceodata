package com.exampleodata.demo.model;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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
import org.apache.olingo.server.api.uri.UriResourceFunction;
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

    // Entity Types Names
    public static final String ET_PRODUCT_NAME = "Product";
    public static final FullQualifiedName ET_PRODUCT_FQN = new FullQualifiedName(NAMESPACE, ET_PRODUCT_NAME);

    public static final String ET_CATEGORY_NAME = "Category";
    public static final FullQualifiedName ET_CATEGORY_FQN = new FullQualifiedName(NAMESPACE, ET_CATEGORY_NAME);

    // Entity Set Names
    public static final String ES_PRODUCTS_NAME = "Products";
    public static final String ES_CATEGORIES_NAME = "Categories";

    public static final String ET_CATEGORY_COUNT = "CategoryCount";
    public static final FullQualifiedName ET_CategoryCount_FQN = new FullQualifiedName(NAMESPACE, ET_CATEGORY_COUNT);

    public String FUNCTION_NAME=null;
    public FullQualifiedName FUNCTION_NAME_FQN=null;
    // Function Parameters
    public String PARAMETER_AMOUNT = null;

    //Query
    public String QUERY =null;

    //Return Type
    public FullQualifiedName SET_RETURN_TYPE =null;

    //set collection
    public boolean SET_COLLECTION=false;

    //set Bound
    public boolean SET_BOUND=false;

    LinkedList functionLinkedList=null;

    public DemoEdmProviderForAllForAction() throws IOException, ParseException {
        functionLinkedList=getFunctionList();
    }

    public LinkedList getFunctionList() throws IOException, ParseException {
        final UriResourceFunction uriResourceFunction=null;
        JSONParser jsonParser=new JSONParser();
        Object obj= jsonParser.parse(new FileReader("E:\\Document\\git\\demoserviceodata\\src\\main\\java\\com\\exampleodata\\demo\\data\\Example.json"));

        JSONArray jsonArray=(JSONArray) obj;

        int length = jsonArray.size();

        LinkedList functionList = new LinkedList();

        for (int i =0; i< length; i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            Set s = jsonObject.entrySet();
            Iterator iter = s.iterator();

            HashMap hm = new HashMap();

            while(iter.hasNext()){
                Map.Entry me = (Map.Entry) iter.next();
                hm.put(me.getKey(), me.getValue());
            }
            functionList.add(hm);
        }
        return functionList;
    }

    @Override
    public List<CsdlFunction> getFunctions(final FullQualifiedName functionName) {
        final List<CsdlFunction> functions = new ArrayList<CsdlFunction>();
              if (functionName.equals(FUNCTION_NAME_FQN)) {
            //for(int i=0;i<functionLinkedList.size();i++) {
                //HashMap hm = (HashMap) functionLinkedList.get(i);
                functionLinkedList.contains("FUNCTION_NAME");
//                if(hm.containsKey("FUNCTION_NAME")){
//                    FUNCTION_NAME = (String) hm.get("FUNCTION_NAME");//(String) jsonObject.get("FUNCTION_NAME");//"GetTopProducts";
//                    FUNCTION_NAME_FQN
//                            = new FullQualifiedName(NAMESPACE, FUNCTION_NAME);
//                    PARAMETER_AMOUNT=(String) hm.get("PARAMETER_AMOUNT");
//                    QUERY=(String) hm.get("QUERY");
//                    SET_RETURN_TYPE = new FullQualifiedName(NAMESPACE, (String) hm.get("SET_RETURN_TYPE"));
//                    SET_COLLECTION= (boolean) hm.get("SET_COLLECTION");
//                    SET_BOUND = (boolean) hm.get("SET_BOUND");
//                }
            //}

            // Create the parameter for the function
            final List<CsdlParameter> parameters = new ArrayList<CsdlParameter>();
            if(!PARAMETER_AMOUNT.trim().isEmpty()) {

                CsdlParameter parameter = new CsdlParameter();
                parameter.setName(PARAMETER_AMOUNT);
                parameter.setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
                parameters.add(parameter);
            }
            // Create the return type of the function
            final CsdlReturnType returnType = new CsdlReturnType();
            returnType.setCollection(SET_COLLECTION).setType(SET_RETURN_TYPE);

            // Create the function
            final CsdlFunction function = new CsdlFunction();
            function.setName(FUNCTION_NAME_FQN.getName())
                    .setParameters(parameters)
                    .setBound(SET_BOUND).setReturnType(returnType);
            functions.add(function);

            return functions;

        }
        return null;
    }

    @Override
    public CsdlFunctionImport getFunctionImport(FullQualifiedName entityContainer, String functionImportName) {
        if(entityContainer.equals(CONTAINER)) {
         if(functionImportName.equals(FUNCTION_NAME_FQN.getName())){
                return new CsdlFunctionImport()
                        .setName(functionImportName)
                        .setFunction(FUNCTION_NAME_FQN)
                        .setEntitySet(ES_PRODUCTS_NAME)
                        .setIncludeInServiceDocument(true);
            }
        }

        return null;
    }

    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) {

        // this method is called for each EntityType that are configured in the Schema
        CsdlEntityType entityType = null;

        if (entityTypeName.equals(ET_PRODUCT_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty description = new CsdlProperty().setName("Description")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
            CsdlProperty price = new CsdlProperty().setName("Price")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: many-to-one, null not allowed (product must have a category)
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("Category")
                    .setType(ET_CATEGORY_FQN).setNullable(true)
                    .setPartner("Products");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_PRODUCT_NAME);
            entityType.setProperties(Arrays.asList(id, name, description, price));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);

        } else if (entityTypeName.equals(ET_CATEGORY_FQN)) {
            // create EntityType properties
            CsdlProperty id = new CsdlProperty().setName("ID")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
            CsdlProperty name = new CsdlProperty().setName("Name")
                    .setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

            // create PropertyRef for Key element
            CsdlPropertyRef propertyRef = new CsdlPropertyRef();
            propertyRef.setName("ID");

            // navigation property: one-to-many
            CsdlNavigationProperty navProp = new CsdlNavigationProperty().setName("Products")
                    .setType(ET_PRODUCT_FQN).setCollection(true)
                    .setPartner("Category");
            List<CsdlNavigationProperty> navPropList = new ArrayList<CsdlNavigationProperty>();
            navPropList.add(navProp);

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_CATEGORY_NAME);
            entityType.setProperties(Arrays.asList(id, name));
            entityType.setKey(Arrays.asList(propertyRef));
            entityType.setNavigationProperties(navPropList);
        }
        else if (entityTypeName.equals(ET_CategoryCount_FQN)) {
            // create EntityType properties
            CsdlProperty getCount = new CsdlProperty().setName("CategoryCount")
                    .setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());

            // configure EntityType
            entityType = new CsdlEntityType();
            entityType.setName(ET_CATEGORY_COUNT);
            entityType.setProperties(Arrays.asList(getCount));
        }

        return entityType;
    }

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName) {

        CsdlEntitySet entitySet = null;

        if (entityContainer.equals(CONTAINER)) {

            if (entitySetName.equals(ES_PRODUCTS_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_PRODUCTS_NAME);
                entitySet.setType(ET_PRODUCT_FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Categories"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("Category"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);

            } else if (entitySetName.equals(ES_CATEGORIES_NAME)) {

                entitySet = new CsdlEntitySet();
                entitySet.setName(ES_CATEGORIES_NAME);
                entitySet.setType(ET_CATEGORY_FQN);

                // navigation
                CsdlNavigationPropertyBinding navPropBinding = new CsdlNavigationPropertyBinding();
                navPropBinding.setTarget("Products"); // the target entity set, where the navigation property points to
                navPropBinding.setPath("Products"); // the path from entity type to navigation property
                List<CsdlNavigationPropertyBinding> navPropBindingList = new ArrayList<CsdlNavigationPropertyBinding>();
                navPropBindingList.add(navPropBinding);
                entitySet.setNavigationPropertyBindings(navPropBindingList);
            }
        }

        return entitySet;

    }

    @Override
    public List<CsdlSchema> getSchemas() {

        // create Schema
        CsdlSchema schema = new CsdlSchema();
        schema.setNamespace(NAMESPACE);

        // add EntityTypes
        List<CsdlEntityType> entityTypes = new ArrayList<CsdlEntityType>();
        entityTypes.add(getEntityType(ET_PRODUCT_FQN));
        entityTypes.add(getEntityType(ET_CATEGORY_FQN));
        entityTypes.add(getEntityType(ET_CategoryCount_FQN));
        schema.setEntityTypes(entityTypes);


        List<CsdlFunction> functions = new ArrayList<CsdlFunction>();

        for(int i=0;i<functionLinkedList.size();i++) {
            HashMap hm = (HashMap) functionLinkedList.get(i);
            if(hm.containsKey("FUNCTION_NAME")){
                FUNCTION_NAME = (String) hm.get("FUNCTION_NAME");//(String) jsonObject.get("FUNCTION_NAME");//"GetTopProducts";
                FUNCTION_NAME_FQN
                        = new FullQualifiedName(NAMESPACE, FUNCTION_NAME);
                functions.addAll(getFunctions(FUNCTION_NAME_FQN));
            }
        }

        schema.setFunctions(functions);

        // add EntityContainer
        schema.setEntityContainer(getEntityContainer());

        // finally
        List<CsdlSchema> schemas = new ArrayList<CsdlSchema>();
        schemas.add(schema);

        return schemas;
    }

    public CsdlEntityContainer getEntityContainer() {
        // create EntitySets
        List<CsdlEntitySet> entitySets = new ArrayList<CsdlEntitySet>();
        entitySets.add(getEntitySet(CONTAINER, ES_PRODUCTS_NAME));
        entitySets.add(getEntitySet(CONTAINER, ES_CATEGORIES_NAME));

        // Create function imports
        List<CsdlFunctionImport> functionImports = new ArrayList<CsdlFunctionImport>();
        for(int i=0;i<functionLinkedList.size();i++) {
            HashMap hm = (HashMap) functionLinkedList.get(i);
            if(hm.containsKey("FUNCTION_NAME"))
            {
                FUNCTION_NAME = (String) hm.get("FUNCTION_NAME");//(String) jsonObject.get("FUNCTION_NAME");//"GetTopProducts";
                FUNCTION_NAME_FQN
                        = new FullQualifiedName(NAMESPACE, FUNCTION_NAME);

                functionImports.add(getFunctionImport(CONTAINER, FUNCTION_NAME));
            }
        }

        // create EntityContainer
        CsdlEntityContainer entityContainer = new CsdlEntityContainer();
        entityContainer.setName(CONTAINER_NAME);
        entityContainer.setEntitySets(entitySets);
        entityContainer.setFunctionImports(functionImports);
        //entityContainer.setActionImports(actionImports);

        return entityContainer;

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

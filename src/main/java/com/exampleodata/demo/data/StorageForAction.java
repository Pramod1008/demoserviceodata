package com.exampleodata.demo.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.List;

import com.exampleodata.demo.model.DemoEdmProviderForAllForAction;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StorageForAction {

    private List<Entity> productList;
    private List<Entity> categoryList;
    public static final String ACTION_PROVIDE_DISCOUNT = "DiscountProducts";
    public static final String ACTION_PROVIDE_DISCOUNT_FOR_PRODUCT = "DiscountProduct";
    public static final String AMOUNT_PROPERTY = "Amount";
    public static final String PRICE_PROPERTY= "Price";
    private static final Logger LOG = LoggerFactory.getLogger(StorageForAction.class);
    public StorageForAction() {

        productList = new ArrayList<Entity>();
        categoryList = new ArrayList<Entity>();

        //initProductSampleData();
        //initCategorySampleData();
    }

    /* PUBLIC FACADE */


    public Entity readFunctionImportEntity(final UriResourceFunction uriResourceFunction,
                                           final ServiceMetadata serviceMetadata) throws ODataApplicationException, IOException, ParseException {

        final EntityCollection entityCollection = readFunctionImportCollection(uriResourceFunction, serviceMetadata);
        final EdmEntityType edmEntityType = (EdmEntityType) uriResourceFunction.getFunction().getReturnType().getType();

        return UtilForAction.findEntity(edmEntityType, entityCollection, uriResourceFunction.getKeyPredicates());
    }

    public EntityCollection readFunctionImportCollection(final UriResourceFunction uriResourceFunction,
                                                         final ServiceMetadata serviceMetadata) throws ODataApplicationException, IOException, ParseException {
        DemoEdmProviderForAllForAction demoEdmProviderForAllForAction=new DemoEdmProviderForAllForAction();
        if(DemoEdmProviderForAllForAction.FUNCTION_COUNT_CATEGORIES.equals(uriResourceFunction.getFunctionImport().getName())) {
            final List<Entity> resultEntityList = new ArrayList<Entity>();
            Entity entity=getCategoryCount();
            resultEntityList.add(entity);

            final EntityCollection resultCollection = new EntityCollection();
            resultCollection.getEntities().addAll(resultEntityList);
            return resultCollection;
        }else if(DemoEdmProviderForAllForAction.FUNCTION_PROVIDE_DISCOUNT_FOR_PRODUCT.equals(uriResourceFunction.getFunctionImport().getName())) {
            final UriParameter parameterDiscount=uriResourceFunction.getParameters().get(0);
            try
            {
                for(final Entity price:productList){
                    LOG.info("PriceList",price);
                }
            }catch (Exception e){
                throw new ODataApplicationException("Exception ",HttpStatusCode.BAD_REQUEST.getStatusCode(),Locale.ENGLISH);
            }
            return null;
        }else if(DemoEdmProviderForAllForAction.FUNCTION_GET_ALL_PRODUCTS.equals(uriResourceFunction.getFunctionImport().getName())) {
           // final UriParameter parameterDiscount=uriResourceFunction.getParameters().get(0);
            try
            {
                // EntityCollection entityCollectionget=AllProducts();
//                EntityCollection entityCollection = readEntitySetData(DemoEdmProviderForAllForAction.ET_PRODUCT_NAME);
//                return entityCollection;
                String query="SELECT * FROM studentapplication.product";
                EntityCollection entityCollectionget=AllProducts(query);
                return  entityCollectionget;
            }catch (Exception e){
                throw new ODataApplicationException("Exception ",HttpStatusCode.BAD_REQUEST.getStatusCode(),Locale.ENGLISH);
            }
        } else if(demoEdmProviderForAllForAction.FUNCTION_NAME.equals(uriResourceFunction.getFunctionImport().getName())) {
            String query=null;
            int limit;
            try
            {
                if(uriResourceFunction.getParameters().size()==0){
                    query=demoEdmProviderForAllForAction.QUERY;
                }else{
                    final UriParameter parameterTop=uriResourceFunction.getParameters().get(0);
                    limit=Integer.parseInt(parameterTop.getText());
                    query=demoEdmProviderForAllForAction.QUERY+" "+demoEdmProviderForAllForAction.PARAMETER_AMOUNT+" "+limit;
                }
                EntityCollection entityCollectionget=AllProducts(query);
                return  entityCollectionget;

            }catch (Exception e){
                throw new ODataApplicationException("Exception ",HttpStatusCode.BAD_REQUEST.getStatusCode(),Locale.ENGLISH);
            }
        }else {
            throw new ODataApplicationException("Function not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                    Locale.ROOT);
        }
    }

    private Entity getCategoryCount() {
        //EntityCollection getCountEntityCollection = new EntityCollection();
        // Sql Connection
        try
        {
            // create our mysql database connection
            String myDriver ="com.mysql.cj.jdbc.Driver";
            String myUrl = "jdbc:mysql://localhost:3306/studentapplication";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "root", "root");

            // our SQL SELECT query.
            // if you only need a few columns, specify them by name instead of using "*"
            String query = "Select Count(*) as CategoryCount from category";

            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                int CategoryCount = rs.getInt("CategoryCount");

                Entity entity = new Entity();
                entity.addProperty(new Property(null,"CategoryCount",ValueType.PRIMITIVE,CategoryCount));
                return entity;
            }
            st.close();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
        return null;
        //End Connection
    }

    public void resetDataSet() {
        resetDataSet(Integer.MAX_VALUE);
    }

    public void resetDataSet(final int amount) {
        // Replace the old lists with empty ones
        productList = new ArrayList<Entity>();
        categoryList = new ArrayList<Entity>();

        // Create new sample data
        initProductSampleData();
        initCategorySampleData();

        // Truncate the lists
        if(amount < productList.size()) {
            productList = productList.subList(0, amount);
            // Products 0, 1 are linked to category 0
            // Products 2, 3 are linked to category 1
            // Products 4, 5 are linked to category 2
            categoryList = categoryList.subList(0, (amount / 2) + 1);
        }
    }

    public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet) throws ODataApplicationException {

        if (edmEntitySet.getName().equals(DemoEdmProviderForAllForAction.ES_PRODUCTS_NAME)) {
            productList=initProductSampleData();
            return getEntityCollection(productList);
        } else if(edmEntitySet.getName().equals(DemoEdmProviderForAllForAction.ES_CATEGORIES_NAME)) {
            return getEntityCollection(categoryList);
        }

        return null;
    }

    public EntityCollection readEntitySetData(String edmEntityTypeName) throws ODataApplicationException {

        if (edmEntityTypeName.equals(DemoEdmProviderForAllForAction.ET_PRODUCT_NAME)) {
            productList=initProductSampleData();
            return getEntityCollection(productList);
        } else if(edmEntityTypeName.equals(DemoEdmProviderForAllForAction.ET_CATEGORY_NAME)) {
            return getEntityCollection(categoryList);
        }

        return null;
    }


    public Entity readEntityData(String entityTypeName) {

        if (entityTypeName.equals(DemoEdmProviderForAllForAction.ET_PRODUCT_NAME)) {
            return  getEntityCollection(productList).getEntities().get(0);
        } else if(entityTypeName.equals(DemoEdmProviderForAllForAction.ET_CATEGORY_NAME)) {
            return getEntityCollection(categoryList).getEntities().get(0);
        }

        return null;

    }

    public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams)
            throws ODataApplicationException {

        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        if (edmEntitySet.getName().equals(DemoEdmProviderForAllForAction.ES_PRODUCTS_NAME)) {
            return getEntity(edmEntityType, keyParams, productList);
        } else if(edmEntitySet.getName().equals(DemoEdmProviderForAllForAction.ES_CATEGORIES_NAME)) {
            return getEntity(edmEntityType, keyParams, categoryList);
        }

        return null;
    }

    // Navigation
    public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType) {
        EntityCollection collection = getRelatedEntityCollection(entity, relatedEntityType);
        if (collection.getEntities().isEmpty()) {
            return null;
        }
        return collection.getEntities().get(0);
    }

    public Entity getRelatedEntity(Entity entity, EdmEntityType relatedEntityType, List<UriParameter> keyPredicates)
            throws ODataApplicationException {

        EntityCollection relatedEntities = getRelatedEntityCollection(entity, relatedEntityType);
        return UtilForAction.findEntity(relatedEntityType, relatedEntities, keyPredicates);
    }

    public EntityCollection getRelatedEntityCollection(Entity sourceEntity, EdmEntityType targetEntityType) {
        EntityCollection navigationTargetEntityCollection = new EntityCollection();

        FullQualifiedName relatedEntityFqn = targetEntityType.getFullQualifiedName();
        String sourceEntityFqn = sourceEntity.getType();

        if (sourceEntityFqn.equals(DemoEdmProviderForAllForAction.ET_PRODUCT_FQN.getFullQualifiedNameAsString())
                && relatedEntityFqn.equals(DemoEdmProviderForAllForAction.ET_CATEGORY_FQN)) {
            // relation Products->Category (result all categories)
            int productID = (Integer) sourceEntity.getProperty("ID").getValue();
            if (productID == 0 || productID == 1) {
                navigationTargetEntityCollection.getEntities().add(categoryList.get(0));
            } else if (productID == 2 || productID == 3) {
                navigationTargetEntityCollection.getEntities().add(categoryList.get(1));
            } else if (productID == 4 || productID == 5 || productID == 6) {
                navigationTargetEntityCollection.getEntities().add(categoryList.get(2));
            }
        } else if (sourceEntityFqn.equals(DemoEdmProviderForAllForAction.ET_CATEGORY_FQN.getFullQualifiedNameAsString())
                && relatedEntityFqn.equals(DemoEdmProviderForAllForAction.ET_PRODUCT_FQN)) {
            // relation Category->Products (result all products)
            int categoryID = (Integer) sourceEntity.getProperty("ID").getValue();
            if (categoryID == 0) {
                // the first 2 products are notebooks
                navigationTargetEntityCollection.getEntities().addAll(productList.subList(0, 3));
            } else if (categoryID == 1) {
                // the next 2 products are organizers
                navigationTargetEntityCollection.getEntities().addAll(productList.subList(1, 4));
            } else if (categoryID == 2) {
                // the first 2 products are monitors
                navigationTargetEntityCollection.getEntities().addAll(productList.subList(2, 6));
            }
        }

        return navigationTargetEntityCollection;
    }

    /* INTERNAL */

    private EntityCollection getEntityCollection(final List<Entity> entityList) {

        EntityCollection retEntitySet = new EntityCollection();
        retEntitySet.getEntities().addAll(entityList);

        return retEntitySet;
    }

    private Entity getEntity(EdmEntityType edmEntityType, List<UriParameter> keyParams, List<Entity> entityList)
            throws ODataApplicationException {

        // the list of entities at runtime
        EntityCollection entitySet = getEntityCollection(entityList);

        /* generic approach to find the requested entity */
        Entity requestedEntity = UtilForAction.findEntity(edmEntityType, entitySet, keyParams);

        if (requestedEntity == null) {
            // this variable is null if our data doesn't contain an entity for the requested key
            // Throw suitable exception
            throw new ODataApplicationException("Entity for requested key doesn't exist",
                    HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        return requestedEntity;
    }


    private boolean entityIdExists(int id, List<Entity> entityList) {

        for (Entity entity : entityList) {
            Integer existingID = (Integer) entity.getProperty("ID").getValue();
            if (existingID.intValue() == id) {
                return true;
            }
        }

        return false;
    }

    private List<Entity> initProductSampleData(String query) {


        // Sql Connection
        try
        {
            // create our mysql database connection
            String myDriver ="com.mysql.cj.jdbc.Driver";
            String myUrl = "jdbc:mysql://localhost:3306/studentapplication";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "root", "root");

            // our SQL SELECT query.
            // if you only need a few columns, specify them by name instead of using "*"
           // String query = "SELECT * FROM studentapplication.product";

            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                String Name = rs.getString("Name");
                String Description = rs.getString("Description");
                int Price = rs.getInt("Price");
                int Id = rs.getInt("ID");

                Entity entity = new Entity();

                entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE,Id));
                entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, Name));
                entity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, Description));
                entity.addProperty(new Property(null,"Price",ValueType.PRIMITIVE,Price));


                entity.setType(DemoEdmProviderForAllForAction.ET_PRODUCT_FQN.getFullQualifiedNameAsString());
                entity.setId(createId(entity, "ID"));

                productList.add(entity);

            }

            st.close();
            return productList;
        }
        catch (Exception e)
        {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }

        //End Connection
        return null;
    }

       private List<Entity> initProductSampleData() {


        // Sql Connection
        try
        {
            // create our mysql database connection
            String myDriver ="com.mysql.cj.jdbc.Driver";
            String myUrl = "jdbc:mysql://localhost:3306/studentapplication";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "root", "root");

            // our SQL SELECT query.
            // if you only need a few columns, specify them by name instead of using "*"
            String query = "SELECT * FROM studentapplication.product";

            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {

                String Name = rs.getString("Name");
                String Description = rs.getString("Description");
                int Price = rs.getInt("Price");
                int Id = rs.getInt("ID");

                Entity entity = new Entity();

                entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE,Id));
                entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, Name));
                entity.addProperty(new Property(null, "Description", ValueType.PRIMITIVE, Description));
                entity.addProperty(new Property(null,"Price",ValueType.PRIMITIVE,Price));


                entity.setType(DemoEdmProviderForAllForAction.ET_PRODUCT_FQN.getFullQualifiedNameAsString());
                entity.setId(createId(entity, "ID"));

                productList.add(entity);

            }

            st.close();
            return productList;
        }
        catch (Exception e)
        {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }

        //End Connection
           return null;
       }



    private void initCategorySampleData() {


        // Sql Connection
        try
        {
            // create our mysql database connection
             String myDriver ="com.mysql.cj.jdbc.Driver";
            String myUrl = "jdbc:mysql://localhost:3306/studentapplication";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "root", "root");

            // our SQL SELECT query.
            // if you only need a few columns, specify them by name instead of using "*"
            String query = "SELECT * FROM studentapplication.category";

            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next())
            {
                Entity entity = new Entity();

                String Name = rs.getString("Name");
                int ID = rs.getInt("ID");

                entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE,ID));
                entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, Name));


                entity.setType(DemoEdmProviderForAllForAction.ET_CATEGORY_FQN.getFullQualifiedNameAsString());
                entity.setId(createId(entity, "ID"));

                categoryList.add(entity);
            }
            st.close();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }

        //End Connection
    }

    private URI createId(Entity entity, String idPropertyName) {
        return createId(entity, idPropertyName, null);
    }

    private URI createId(Entity entity, String idPropertyName, String navigationName) {
        try {
            StringBuilder sb = new StringBuilder(getEntitySetName(entity)).append("(");
            final Property property = entity.getProperty(idPropertyName);
            sb.append(property.asPrimitive()).append(")");
            if(navigationName != null) {
                sb.append("/").append(navigationName);
            }
            return new URI(sb.toString());
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create (Atom) id for entity: " + entity, e);
        }
    }

    private String getEntitySetName(Entity entity) {
        if(DemoEdmProviderForAllForAction.ET_CATEGORY_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
            return DemoEdmProviderForAllForAction.ES_CATEGORIES_NAME;
        } else if(DemoEdmProviderForAllForAction.ET_PRODUCT_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
            return DemoEdmProviderForAllForAction.ES_PRODUCTS_NAME;
        }
        return entity.getType();
    }

    public EntityCollection processBoundActionEntityCollection(EdmAction action, Map<String, Parameter> parameters) {
        EntityCollection collection = new EntityCollection();
        if (ACTION_PROVIDE_DISCOUNT.equals(action.getName())) {
            for (Entity entity : categoryList) {
                Entity en = getRelatedEntity(entity, (EdmEntityType) action.getReturnType().getType());
                Integer currentValue = (Integer)en.getProperty(PRICE_PROPERTY).asPrimitive();
                Integer newValue = currentValue - (Integer)parameters.get(AMOUNT_PROPERTY).asPrimitive();
                en.getProperty(PRICE_PROPERTY).setValue(ValueType.PRIMITIVE, newValue);
                collection.getEntities().add(en);
            }
        }
        return collection;
    }

    public DemoEntityActionResult processBoundActionEntity(EdmAction action, Map<String, Parameter> parameters,
                                                           List<UriParameter> keyParams) throws ODataApplicationException {
        DemoEntityActionResult result = new DemoEntityActionResult();
        if (ACTION_PROVIDE_DISCOUNT_FOR_PRODUCT.equals(action.getName())) {
            for (Entity entity : categoryList) {
                Entity en = getRelatedEntity(entity, (EdmEntityType) action.getReturnType().getType(), keyParams);
                Integer currentValue = (Integer)en.getProperty(PRICE_PROPERTY).asPrimitive();
                Integer newValue = currentValue - (Integer)parameters.get(AMOUNT_PROPERTY).asPrimitive();
                en.getProperty(PRICE_PROPERTY).setValue(ValueType.PRIMITIVE, newValue);
                result.setEntity(en);
                result.setCreated(true);
                return result;
            }
        }
        return null;
    }


    public EntityCollection getBoundFunctionEntityCollection(EdmFunction function, Integer amount) {
        EntityCollection collection = new EntityCollection();
        if ("GetDiscountProducts".equals(function.getName())) {
            for (Entity entity : categoryList) {
                    Entity en = getRelatedEntity(entity, (EdmEntityType) function.getReturnType().getType());
                if(en!=null) {
                    if (amount >= (Integer) en.getProperty("Price").getValue()) {
                        collection.getEntities().add(en);
                    }
                }
                //LOG.info(entity.toString()+" "+entity.getProperty("Price")+" type "+entity.getProperty("Price").getType());
            }
        }
        return collection;
    }

    public Entity getBoundFunctionEntity(EdmFunction function, Integer amount,List<UriParameter> keyParams) throws ODataApplicationException {
        if ("GetDiscountProduct".equals(function.getName())) {
            for (Entity entity : categoryList) {
                //if(amount== entity.getProperty("amount").asCollection().size()){
                    return getRelatedEntity(entity, (EdmEntityType) function.getReturnType().getType(), keyParams);
                //}
                //LOG.info(entity.toString());
            }
        }
        return null;
    }

    private boolean isKey(EdmEntityType edmEntityType, String propertyName) {

        List<EdmKeyPropertyRef> keyPropertyRefs = edmEntityType.getKeyPropertyRefs();
        for (EdmKeyPropertyRef propRef : keyPropertyRefs) {
            String keyPropertyName = propRef.getName();
            if (keyPropertyName.equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    public EntityCollection AllProducts(String query) {
        if(!productList.isEmpty())
        {
            productList.clear();
        }
        productList=initProductSampleData(query);
        return getEntityCollection(productList);
    }
}

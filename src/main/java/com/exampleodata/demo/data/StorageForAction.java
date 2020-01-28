package com.exampleodata.demo.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;

import com.exampleodata.demo.model.DemoEdmProviderForAllForAction;
import com.exampleodata.demo.model.FunctionClass;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.*;
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

    private List<Entity> entityResultList;

    private static final Logger LOG = LoggerFactory.getLogger(StorageForAction.class);
    public StorageForAction() {
        entityResultList = new ArrayList<Entity>();
      }

    public Entity readFunctionImportEntity(final UriResourceFunction uriResourceFunction,
                                           final ServiceMetadata serviceMetadata) throws ODataApplicationException, IOException, ParseException {

        final EntityCollection entityCollection = readFunctionImportCollection(uriResourceFunction, serviceMetadata);
        final EdmEntityType edmEntityType = (EdmEntityType) uriResourceFunction.getFunction().getReturnType().getType();

         return UtilForAction.findEntity(edmEntityType, entityCollection, uriResourceFunction.getKeyPredicates());
    }

    public EntityCollection readFunctionImportCollection(final UriResourceFunction uriResourceFunction,
                                                         final ServiceMetadata serviceMetadata) throws ODataApplicationException, IOException, ParseException {
        HashMap<String, FunctionClass> functionLinkedList=null;
        DemoEdmProviderForAllForAction demoEdmProviderForAllForAction=new DemoEdmProviderForAllForAction();
        functionLinkedList=demoEdmProviderForAllForAction.getFunctionList();
        String queryAppend="Select  " ;

        for (Map.Entry<String, FunctionClass> functionEntry : functionLinkedList.entrySet()) {
            if(functionEntry.getKey().equals(uriResourceFunction.getFunctionImport().getName())) {
                try
                    {
                        String query=null;
                        EntityCollection entityCollectionget=null;
                         int limit;
                        if(uriResourceFunction.getParameters().size()==0){
                            query=queryAppend+functionEntry.getValue().getQUERY();
                        }else{
                            final UriParameter parameterTop=uriResourceFunction.getParameters().get(0);
                            limit=Integer.parseInt(parameterTop.getText());
                            query=functionEntry.getValue().getQUERY()+" "+functionEntry.getValue().getPARAMETER_AMOUNT()+" "+limit;
                        }
                            FullQualifiedName et_Name= functionEntry.getValue().getSET_RETURN_TYPE();
                            entityCollectionget = AllProducts(query, et_Name);

                        return  entityCollectionget;

                    }catch (Exception e){
                        throw new ODataApplicationException("Exception ",HttpStatusCode.BAD_REQUEST.getStatusCode(),Locale.ENGLISH);
                    }
            }
        }
        return null;
    }
    public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet) throws ODataApplicationException {

       return null;
    }

    public EntityCollection readEntitySetData(String edmEntityTypeName) throws ODataApplicationException {

        return null;
    }


    public Entity readEntityData(String entityTypeName) {

//        if (entityTypeName.equals(DemoEdmProviderForAllForAction.ET_PRODUCT_NAME)) {
//            return  getEntityCollection(productList).getEntities().get(0);
//        } else if(entityTypeName.equals(DemoEdmProviderForAllForAction.ET_CATEGORY_NAME)) {
//            return getEntityCollection(categoryList).getEntities().get(0);
//        }

        return null;

    }

    public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams)
            throws ODataApplicationException {

        EdmEntityType edmEntityType = edmEntitySet.getEntityType();
//
//        if (edmEntitySet.getName().equals(DemoEdmProviderForAllForAction.ES_PRODUCTS_NAME)) {
//            return getEntity(edmEntityType, keyParams, productList);
//        } else if(edmEntitySet.getName().equals(DemoEdmProviderForAllForAction.ES_CATEGORIES_NAME)) {
//            return getEntity(edmEntityType, keyParams, categoryList);
//        }

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

        List<Entity> catList=new ArrayList<Entity>();
        Entity catEntity=new Entity();
        catEntity.addProperty(new Property(null,"ID", ValueType.PRIMITIVE,"1"));
        catEntity.addProperty(new Property(null,"Name", ValueType.PRIMITIVE,"New Category"));
        catList.add(catEntity);

        navigationTargetEntityCollection.getEntities().add(catList.get(0));

//        if (sourceEntityFqn.equals(DemoEdmProviderForAllForAction.ET_PRODUCT_FQN.getFullQualifiedNameAsString())
//                && relatedEntityFqn.equals(DemoEdmProviderForAllForAction.ET_CATEGORY_FQN)) {
//            // relation Products->Category (result all categories)
//            int productID = (Integer) sourceEntity.getProperty("ID").getValue();
//            if (productID == 0 || productID == 1) {
//                navigationTargetEntityCollection.getEntities().add(categoryList.get(0));
//            } else if (productID == 2 || productID == 3) {
//                navigationTargetEntityCollection.getEntities().add(categoryList.get(1));
//            } else if (productID == 4 || productID == 5 || productID == 6) {
//                navigationTargetEntityCollection.getEntities().add(categoryList.get(2));
//            }
//        } else if (sourceEntityFqn.equals(DemoEdmProviderForAllForAction.ET_CATEGORY_FQN.getFullQualifiedNameAsString())
//                && relatedEntityFqn.equals(DemoEdmProviderForAllForAction.ET_PRODUCT_FQN)) {
//            // relation Category->Products (result all products)
//            int categoryID = (Integer) sourceEntity.getProperty("ID").getValue();
//            if (categoryID == 0) {
//                // the first 2 products are notebooks
//                navigationTargetEntityCollection.getEntities().addAll(productList.subList(0, 3));
//            } else if (categoryID == 1) {
//                // the next 2 products are organizers
//                navigationTargetEntityCollection.getEntities().addAll(productList.subList(1, 4));
//            } else if (categoryID == 2) {
//                // the first 2 products are monitors
//                navigationTargetEntityCollection.getEntities().addAll(productList.subList(2, 6));
//            }
//        }

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

    private List<Entity> initProductSampleData(String query,FullQualifiedName et_Name) {

        // Sql Connection
        try
        {
            // create our mysql database connection
            String myDriver ="com.mysql.cj.jdbc.Driver";
            String myUrl = "jdbc:mysql://localhost:3306/studentapplication";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "root", "admin");

            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            ResultSet rs = st.executeQuery(query);

            List<Map<String,Object>> rows=new ArrayList<Map<String,Object>>();

            ResultSetMetaData metaData=rs.getMetaData();
            int columnCount=metaData.getColumnCount();

            // iterate through the java resultset
            while (rs.next())
            {
                Map<String,Object> columns=new LinkedHashMap<String, Object>();
                Entity entity = new Entity();

                for (int i=1;i<=columnCount;i++){
                columns.put(metaData.getColumnLabel(i),rs.getObject(i));

                    if(!metaData.getColumnLabel(1).toLowerCase().contains("count")){
                        entity.addProperty(new Property(null, metaData.getColumnLabel(i), ValueType.PRIMITIVE,rs.getObject(i)));
                    }else
                    {
                        entity.addProperty(new Property(null, "Count", ValueType.PRIMITIVE,rs.getObject(i)));
                    }

                }

                Link link = new Link();
                String navPropName="Category";
                link.setTitle(navPropName);
                link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
                link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);

                EntityCollection navigationTargetEntityCollection = new EntityCollection();
                //List<Entity> catList=new ArrayList<Entity>();
                Entity catEntity=new Entity();
                int Id = 1;
                catEntity.addProperty(new Property(null,"ID", ValueType.PRIMITIVE,Id));
                catEntity.addProperty(new Property(null,"Name", ValueType.PRIMITIVE,"New Category"));

                catEntity.setType("OData.Demo.Category");
                catEntity.setId(createId(catEntity, "ID"));

               // catList.add(catEntity);
                navigationTargetEntityCollection.getEntities().add(catEntity);
                navigationTargetEntityCollection.setId(catEntity.getId());

                Entity expandEntity =navigationTargetEntityCollection.getEntities().get(0);

                link.setInlineEntity(expandEntity);
                link.setHref(expandEntity.getId().toASCIIString());
                entity.getNavigationLinks().add(link);

                if(!metaData.getColumnLabel(1).toLowerCase().contains("count")) {
                    entity.setType(et_Name.getFullQualifiedNameAsString());
                    entity.setId(createId(entity, "ID"));
                }
                entityResultList.add(entity);
            }
            st.close();
            return entityResultList;
        }
        catch (Exception e)
        {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }

        //End Connection
        return null;
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

    private String getEntitySetName(Entity entity){
        try
        {
        LinkedList entityList=null;
        DemoEdmProviderForAllForAction demoEdmProviderForAllForAction=new DemoEdmProviderForAllForAction();
            entityList=demoEdmProviderForAllForAction.getEntityList();
            for(int i=0;i<entityList.size();i++) {
                HashMap hm = (HashMap) entityList.get(i);
                if(hm.containsKey("EntityType") && entity.getType().contains(hm.get("EntityType").toString())){
                String entitySet= hm.get("EntitySet").toString();
                 return entitySet;
                }
            }

        }catch (Exception e)
        {
            throw new ODataRuntimeException("Unable to create (Atom) id for entity: " + entity, e);
        }



//        if(DemoEdmProviderForAllForAction.ET_CATEGORY_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
//            return DemoEdmProviderForAllForAction.ES_CATEGORIES_NAME;
//        } else if(DemoEdmProviderForAllForAction.ET_PRODUCT_FQN.getFullQualifiedNameAsString().equals(entity.getType())) {
//            return DemoEdmProviderForAllForAction.ES_PRODUCTS_NAME;
//        }
        return entity.getType();
    }

    public EntityCollection getBoundFunctionEntityCollection(EdmFunction function, Integer amount) {
        EntityCollection collection = new EntityCollection();
//        if ("GetDiscountProducts".equals(function.getName())) {
//            for (Entity entity : categoryList) {
//                    Entity en = getRelatedEntity(entity, (EdmEntityType) function.getReturnType().getType());
//                if(en!=null) {
//                    if (amount >= (Integer) en.getProperty("Price").getValue()) {
//                        collection.getEntities().add(en);
//                    }
//                }
//                //LOG.info(entity.toString()+" "+entity.getProperty("Price")+" type "+entity.getProperty("Price").getType());
//            }
//        }
        return collection;
    }

    public Entity getBoundFunctionEntity(EdmFunction function, Integer amount,List<UriParameter> keyParams) throws ODataApplicationException {
       // if ("GetDiscountProduct".equals(function.getName())) {
//            for (Entity entity : categoryList) {
//                //if(amount== entity.getProperty("amount").asCollection().size()){
//                    return getRelatedEntity(entity, (EdmEntityType) function.getReturnType().getType(), keyParams);
//                //}
//                //LOG.info(entity.toString());
//            }
        //}
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

    public EntityCollection AllProducts(String query, FullQualifiedName et_Name) {
        if(!entityResultList.isEmpty())
        {
            entityResultList.clear();
        }
        entityResultList =initProductSampleData(query,et_Name);
        return getEntityCollection(entityResultList);
    }
}

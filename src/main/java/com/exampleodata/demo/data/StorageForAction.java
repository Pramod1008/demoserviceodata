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
        String queryAppend="Select * " ;

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
                        query=queryAppend+functionEntry.getValue().getQUERY()+" "+functionEntry.getValue().getPARAMETER_AMOUNT()+" "+limit;
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
    public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams)
            throws ODataApplicationException {;
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
        return navigationTargetEntityCollection;
    }

    /* INTERNAL */

    private EntityCollection getEntityCollection(final List<Entity> entityList) {

        EntityCollection retEntitySet = new EntityCollection();
        retEntitySet.getEntities().addAll(entityList);

        return retEntitySet;
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

            LinkedList entityList=null;
            HashMap<String, Set> hashSet= new HashMap<>();
            DemoEdmProviderForAllForAction demoEdmProviderForAllForAction=new DemoEdmProviderForAllForAction();
            entityList=demoEdmProviderForAllForAction.getEntityList();
            for(int i=0;i<entityList.size();i++) {
                HashMap hm = (HashMap) entityList.get(i);
                if(hm.containsKey("EntityType") && query.toLowerCase().contains(hm.get("EntityType").toString().toLowerCase())){
                    String entitySet= hm.get("EntitySet").toString();
                    hashSet.put(hm.get("EntityType").toString(),hm.entrySet());
                }
            }

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
                HashSet alreadyAdded=new HashSet();
                Entity catEntity=new Entity();
                String NavName=null;
                Link link = new Link();
                for (int i=1;i<=columnCount;i++){
                    columns.put(metaData.getColumnLabel(i),rs.getObject(i));
                    LOG.info(metaData.getTableName(i));
                    for (String entitySet:hashSet.keySet()) {
                        LOG.info(alreadyAdded.toString());
                        if(metaData.getTableName(i).toLowerCase().equals(entitySet.toLowerCase())){
                            alreadyAdded.add(entitySet);
                            if(alreadyAdded.size()==1)
                            {
                                if(!metaData.getColumnLabel(1).toLowerCase().contains("count")){
                                    entity.addProperty(new Property(null, metaData.getColumnLabel(i), ValueType.PRIMITIVE,rs.getObject(i)));
                                }else
                                {
                                    entity.addProperty(new Property(null, "Count", ValueType.PRIMITIVE,rs.getObject(i)));
                                }
                            } else if(metaData.getTableName(i).toLowerCase().equals(entitySet.toLowerCase())){
                                NavName=entitySet;
                                catEntity.addProperty(new Property(null, metaData.getColumnLabel(i), ValueType.PRIMITIVE,rs.getObject(i)));
                                }
                        }
                    }
                }

                if(NavName!=null) {
                    String navPropName = NavName;
                    link.setTitle(navPropName);
                    link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
                    link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);

                    EntityCollection navigationTargetEntityCollection = new EntityCollection();
                    catEntity.setType("OData.Demo." + navPropName);
                    catEntity.setId(createId(catEntity, "ID"));

                    // catList.add(catEntity);
                    navigationTargetEntityCollection.getEntities().add(catEntity);
                    navigationTargetEntityCollection.setId(catEntity.getId());

                    Entity expandEntity = navigationTargetEntityCollection.getEntities().get(0);

                    link.setInlineEntity(expandEntity);
                    link.setHref(expandEntity.getId().toASCIIString());
                    entity.getNavigationLinks().add(link);
                }

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
        return entity.getType();
    }

    public EntityCollection getBoundFunctionEntityCollection(EdmFunction function, Integer amount) {
        EntityCollection collection = new EntityCollection();
        return collection;
    }

    public Entity getBoundFunctionEntity(EdmFunction function, Integer amount,List<UriParameter> keyParams) throws ODataApplicationException {
        return null;
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

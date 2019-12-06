package com.exampleodata.demo.model;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.exampleodata.demo.data.StorageForAction;
import com.exampleodata.demo.data.UtilForAction;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.*;
import org.apache.olingo.server.api.uri.queryoption.*;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.core.uri.UriResourceSingletonImpl;


public class DemoEntityCollectionProcessorForAction implements EntityCollectionProcessor {


    private OData odata;
    private ServiceMetadata serviceMetadata;
    // our database-mock
    private StorageForAction storage;

    public DemoEntityCollectionProcessorForAction(StorageForAction storage) {
        this.storage = storage;
    }

    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    public void readEntityCollection(ODataRequest request, ODataResponse response,
                                     UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, SerializerException {

        ExpandOption expandOption = uriInfo.getExpandOption();

        final UriResource firstResourceSegment = uriInfo.getUriResourceParts().get(0);

        if(firstResourceSegment instanceof UriResourceEntitySet) {
            if (expandOption != null)
            {
                readExpand(expandOption,uriInfo);
            }
            readEntityCollectionInternal(request, response, uriInfo, responseFormat);
       } else if(firstResourceSegment instanceof UriResourceFunction) {
           readFunctionImportCollection(request, response, uriInfo, responseFormat);
       }else {
            throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                    Locale.ENGLISH);
        }


    }

    private void readExpand( ExpandOption expandOption, UriInfo uriInfo) throws ODataApplicationException  {


        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();


        EntityCollection entityCollection = storage.readEntitySetData(edmEntitySet);

        EdmNavigationProperty edmNavigationProperty = null;
        ExpandItem expandItem = expandOption.getExpandItems().get(0);
        if(expandItem.isStar()) {
            List<EdmNavigationPropertyBinding> bindings = edmEntitySet.getNavigationPropertyBindings();
            // we know that there are navigation bindings
            // however normally in this case a check if navigation bindings exists is done
            if(!bindings.isEmpty()) {
                // can in our case only be 'Category' or 'Products', so we can take the first
                EdmNavigationPropertyBinding binding = bindings.get(0);
                EdmElement property = edmEntitySet.getEntityType().getProperty(binding.getPath());
                // we don't need to handle error cases, as it is done in the Olingo library
                if(property instanceof EdmNavigationProperty) {
                    edmNavigationProperty = (EdmNavigationProperty) property;
                }
            }
        } else {

            UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);

            if(uriResource instanceof UriResourceNavigation) {
                edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
            }
        }

        if(edmNavigationProperty != null) {
            String navPropName = edmNavigationProperty.getName();
            EdmEntityType expandEdmEntityType = edmNavigationProperty.getType();

            List<Entity> entityList = entityCollection.getEntities();
            for (Entity entity : entityList) {
                Link link = new Link();
                link.setTitle(navPropName);
                link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
                link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);

                if (edmNavigationProperty.isCollection()) {
                    EntityCollection expandEntityCollection = storage.getRelatedEntityCollection(entity, expandEdmEntityType);
                    link.setInlineEntitySet(expandEntityCollection);
                    link.setHref(expandEntityCollection.getId().toASCIIString());
                } else {
                    Entity expandEntity = storage.getRelatedEntity(entity, expandEdmEntityType);
                    link.setInlineEntity(expandEntity);
                    link.setHref(expandEntity.getId().toASCIIString());
                }

                // set the link - containing the expanded data - to the current entity
                entity.getNavigationLinks().add(link);
            }
        }
    }

    private void readFunctionImportCollection(final ODataRequest request, final ODataResponse response,
                                              final UriInfo uriInfo, final ContentType responseFormat) throws ODataApplicationException, SerializerException {


        final UriResource firstSegment = uriInfo.getUriResourceParts().get(0);

        if(!(firstSegment instanceof UriResourceFunction)) {
            throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                    Locale.ENGLISH);
        }

        final UriResourceFunction uriResourceFunction = (UriResourceFunction) firstSegment;
        final EntityCollection entityCol = storage.readFunctionImportCollection(uriResourceFunction, serviceMetadata);


        final EdmEntityType edmEntityType = (EdmEntityType) uriResourceFunction.getFunction().getReturnType().getType();
        final ContextURL contextURL = ContextURL.with().asCollection().type(edmEntityType).build();
        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().contextURL(contextURL).build();
        final ODataSerializer serializer = odata.createSerializer(responseFormat);
        final SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entityCol,
                opts);


        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    private void readEntityCollectionInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                                              ContentType responseFormat) throws ODataApplicationException, SerializerException {

        EdmEntitySet responseEdmEntitySet = null; // we'll need this to build the ContextURL
        EntityCollection responseEntityCollection = null; // we'll need this to set the response body

        // 1st retrieve the requested EntitySet from the uriInfo (representation of the parsed URI)
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        int segmentCount = resourceParts.size();

        UriResource uriResource = resourceParts.get(0); // in our example, the first segment is the EntitySet
        if (!(uriResource instanceof UriResourceEntitySet)) {
            throw new ODataApplicationException("Only EntitySet is supported",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
        EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();

        if (segmentCount == 1) { // this is the case for: DemoService/DemoService.svc/Categories
            responseEdmEntitySet = startEdmEntitySet; // the response body is built from the first (and only) entitySet

            // 2nd: fetch the data from backend for this requested EntitySetName and deliver as EntitySet
            responseEntityCollection = storage.readEntitySetData(startEdmEntitySet);
        } else if (segmentCount == 2) { // in case of navigation: DemoService.svc/Categories(3)/Products

            UriResource lastSegment = resourceParts.get(1); // in our example we don't support more complex URIs
            if (lastSegment instanceof UriResourceNavigation) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) lastSegment;
                EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
                EdmEntityType targetEntityType = edmNavigationProperty.getType();
                // from Categories(1) to Products
                responseEdmEntitySet = UtilForAction.getNavigationTargetEntitySet(startEdmEntitySet, edmNavigationProperty);

                // 2nd: fetch the data from backend
                // first fetch the entity where the first segment of the URI points to
                List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
                // e.g. for Categories(3)/Products we have to find the single entity: Category with ID 3
                Entity sourceEntity = storage.readEntityData(startEdmEntitySet, keyPredicates);
                // error handling for e.g. DemoService.svc/Categories(99)/Products
                if (sourceEntity == null) {
                    throw new ODataApplicationException("Entity not found.",
                            HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
                }
                // then fetch the entity collection where the entity navigates to
                // note: we don't need to check uriResourceNavigation.isCollection(),
                // because we are the EntityCollectionProcessor
                responseEntityCollection = storage.getRelatedEntityCollection(sourceEntity, targetEntityType);
            } else if (lastSegment instanceof UriResourceFunction) {// For bound function
                UriResourceFunction uriResourceFunction = (UriResourceFunction) lastSegment;
                // 2nd: fetch the data from backend
                // first fetch the target entity type
                String targetEntityType = uriResourceFunction.getFunction().getReturnType().getType().getName();
                // contextURL displays the last segment
                for(EdmEntitySet entitySet : serviceMetadata.getEdm().getEntityContainer().getEntitySets()){
                    if(targetEntityType.equals(entitySet.getEntityType().getName())){
                        responseEdmEntitySet = entitySet;
                        break;
                    }
                }

                // error handling for null entities
                if (targetEntityType == null || responseEdmEntitySet == null) {
                    throw new ODataApplicationException("Entity not found.",
                            HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
                }
                EdmFunction edmFunction=uriResourceFunction.getFunction();
                Integer amount = Integer.parseInt(uriResourceFunction.getParameters().get(0).getText());
                // then fetch the entity collection for the target type
               // responseEntityCollection = storage.readEntitySetData(targetEntityType);
                responseEntityCollection = storage.getBoundFunctionEntityCollection(edmFunction,amount);
            }
        } else { // this would be the case for e.g. Products(1)/Category/Products
            throw new ODataApplicationException("Not supported",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        List<Entity> entityList = responseEntityCollection.getEntities();
        EntityCollection returnEntityCollection = new EntityCollection();

        // handle $top
        TopOption topOption = uriInfo.getTopOption();
        if (topOption != null) {
            int topNumber = topOption.getValue();
            if (topNumber >= 0) {
                if(topNumber <= entityList.size()) {
                    entityList = entityList.subList(0, topNumber);
                }  // else the client has requested more entities than available => return what we have
            } else {
                throw new ODataApplicationException("Invalid value for $top", HttpStatusCode.BAD_REQUEST.getStatusCode(),
                        Locale.ROOT);
            }
        }

        // after applying the system query options, create the EntityCollection based on the reduced list
        for (Entity entity : entityList) {
            returnEntityCollection.getEntities().add(entity);
        }

        CountOption countOption = uriInfo.getCountOption();
        if (countOption != null) {
            boolean isCount = countOption.getValue();
            if(isCount){
                returnEntityCollection.setCount(entityList.size());
            }
        }

        List<Entity> entityList1 = returnEntityCollection.getEntities();


        // 3rd apply $orderby
        OrderByOption orderByOption = uriInfo.getOrderByOption();
        if (orderByOption != null) {
            List<OrderByItem> orderItemList = orderByOption.getOrders();
            final OrderByItem orderByItem = orderItemList.get(0); // in our example we support only one
            Expression expression = orderByItem.getExpression();
            if(expression instanceof Member){
                UriInfoResource resourcePath = ((Member)expression).getResourcePath();
                UriResource uriResource1 = resourcePath.getUriResourceParts().get(0);
                if (uriResource1 instanceof UriResourcePrimitiveProperty) {
                    EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResource1).getProperty();
                    final String sortPropertyName = edmProperty.getName();

                    // do the sorting for the list of entities
                    Collections.sort(entityList1, new Comparator<Entity>() {

                        // we delegate the sorting to the native sorter of Integer and String
                        public int compare(Entity entity1, Entity entity2) {
                            int compareResult = 0;

                            if(sortPropertyName.equals("ID") || sortPropertyName.equals("Price")  ){
                                Integer integer1 = (Integer) entity1.getProperty(sortPropertyName).getValue();
                                Integer integer2 = (Integer) entity2.getProperty(sortPropertyName).getValue();

                                compareResult = integer1.compareTo(integer2);
                            }else{
                                String propertyValue1 = (String) entity1.getProperty(sortPropertyName).getValue();
                                String propertyValue2 = (String) entity2.getProperty(sortPropertyName).getValue();

                                compareResult = propertyValue1.compareTo(propertyValue2);
                            }

                            // if 'desc' is specified in the URI, change the order of the list
                            if(orderByItem.isDescending()){
                                return - compareResult; // just convert the result to negative value to change the order
                            }

                            return compareResult;
                        }
                    });
                }
            }
        }


        EdmEntityType edmEntityType = responseEdmEntitySet.getEntityType();
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();

        // we need the property names of the $select, in order to build the context URL
        String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);
        ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).selectList(selectList).build();

        // adding the selectOption to the serializerOpts will actually tell the lib to do the job
        final String id = request.getRawBaseUri() + "/" + responseEdmEntitySet.getName();
        EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
                .contextURL(contextUrl)
                .select(selectOption)
                .expand(expandOption)
                .id(id)
                .count(countOption)
                .build();

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType,
                returnEntityCollection, opts);

        // 4th: configure the response object: set the body, headers and status code
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }
}

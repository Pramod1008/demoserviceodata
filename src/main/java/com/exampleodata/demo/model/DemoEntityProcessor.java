package com.exampleodata.demo.model;


import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import com.exampleodata.demo.data.StorageForAction;
import com.exampleodata.demo.data.UtilForAction;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.edm.*;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.*;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.*;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
;

public class DemoEntityProcessor implements EntityProcessor {

    private OData odata;
    private StorageForAction storage;
    private ServiceMetadata serviceMetadata;

    public DemoEntityProcessor(StorageForAction storage) {
        this.storage = storage;
    }

    public void init(OData odata, ServiceMetadata serviceMetadata) {
        this.odata = odata;
        this.serviceMetadata = serviceMetadata;
    }

    public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
            throws ODataApplicationException, SerializerException {

        // The sample service supports only functions imports and entity sets.
        // We do not care about bound functions and composable functions.
        ExpandOption expandOption = uriInfo.getExpandOption();

        UriResource uriResource = uriInfo.getUriResourceParts().get(0);

        if(uriResource instanceof UriResourceEntitySet) {
            if (expandOption != null)
            {
                readExpand(expandOption,uriInfo);
            }
            readEntityInternal(request, response, uriInfo, responseFormat);
        } else if(uriResource instanceof UriResourceFunction) {
            readFunctionImportInternal(request, response, uriInfo, responseFormat);
        } else {
            throw new ODataApplicationException("Only EntitySet is supported",
                    HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
        }

    }

    @Override
    public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    }

    @Override
    public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {

    }

    private void readExpand(ExpandOption expandOption, UriInfo uriInfo) throws ODataApplicationException {

        // 1. retrieve the Entity Type
        List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
        // Note: only in our example we can assume that the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
        EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

        // 2. retrieve the data from backend
        List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
        Entity entity = storage.readEntityData(edmEntitySet, keyPredicates);

        // retrieve the EdmNavigationProperty from the expand expression
        // Note: in our example, we have only one NavigationProperty, so we can directly access it
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
            // can be 'Category' or 'Products', no path supported
            UriResource uriResource = expandItem.getResourcePath().getUriResourceParts().get(0);
            // we don't need to handle error cases, as it is done in the Olingo library
            if(uriResource instanceof UriResourceNavigation) {
                edmNavigationProperty = ((UriResourceNavigation) uriResource).getProperty();
            }
        }

        // can be 'Category' or 'Products', no path supported
        // we don't need to handle error cases, as it is done in the Olingo library
        if(edmNavigationProperty != null) {
            EdmEntityType expandEdmEntityType = edmNavigationProperty.getType();
            String navPropName = edmNavigationProperty.getName();

            // build the inline data
            Link link = new Link();
            link.setTitle(navPropName);
            link.setType(Constants.ENTITY_NAVIGATION_LINK_TYPE);
            link.setRel(Constants.NS_ASSOCIATION_LINK_REL + navPropName);

            if(edmNavigationProperty.isCollection()){ // in case of Categories(1)/$expand=Products
                // fetch the data for the $expand (to-many navigation) from backend
                // here we get the data for the expand
                EntityCollection expandEntityCollection = storage.getRelatedEntityCollection(entity, expandEdmEntityType);
                link.setInlineEntitySet(expandEntityCollection);
                link.setHref(expandEntityCollection.getId().toASCIIString());
            } else {  // in case of Products(1)?$expand=Category
                // fetch the data for the $expand (to-one navigation) from backend
                // here we get the data for the expand
                Entity expandEntity = storage.getRelatedEntity(entity, expandEdmEntityType);
                link.setInlineEntity(expandEntity);
                link.setHref(expandEntity.getId().toASCIIString());
            }

            // set the link - containing the expanded data - to the current entity
            entity.getNavigationLinks().add(link);
        }
    }

    private void readFunctionImportInternal(final ODataRequest request, final ODataResponse response,
                                            final UriInfo uriInfo, final ContentType responseFormat) throws ODataApplicationException, SerializerException {

        // 1st step: Analyze the URI and fetch the entity returned by the function import
        // Function Imports are always the first segment of the resource path
        final UriResource firstSegment = uriInfo.getUriResourceParts().get(0);

        if(!(firstSegment instanceof UriResourceFunction)) {
            throw new ODataApplicationException("Not implemented", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
                    Locale.ENGLISH);
        }

        final UriResourceFunction uriResourceFunction = (UriResourceFunction) firstSegment;
        final Entity entity = storage.readFunctionImportEntity(uriResourceFunction, serviceMetadata);

        if(entity == null) {
            throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }

        // 2nd step: Serialize the response entity
        final EdmEntityType edmEntityType = (EdmEntityType) uriResourceFunction.getFunction().getReturnType().getType();
        final ContextURL contextURL = ContextURL.with().type(edmEntityType).build();
        final EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextURL).build();
        final ODataSerializer serializer = odata.createSerializer(responseFormat);
        final SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, opts);

        // 3rd configure the response object
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

    private void readEntityInternal(ODataRequest request, ODataResponse response, UriInfo uriInfo,
                                    ContentType responseFormat) throws ODataApplicationException, SerializerException {

        EdmEntityType responseEdmEntityType = null; // we'll need this to build the ContextURL
        Entity responseEntity = null; // required for serialization of the response body
        EdmEntitySet responseEdmEntitySet = null; // we need this for building the contextUrl

        // 1st step: retrieve the requested Entity: can be "normal" read operation, or navigation (to-one)
        List<UriResource> resourceParts = uriInfo.getUriResourceParts();
        int segmentCount = resourceParts.size();

        UriResource uriResource = resourceParts.get(0); // in our example, the first segment is the EntitySet
        UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) uriResource;
        EdmEntitySet startEdmEntitySet = uriResourceEntitySet.getEntitySet();

        // Analyze the URI segments
        if (segmentCount == 1) { // no navigation
            responseEdmEntityType = startEdmEntitySet.getEntityType();
            responseEdmEntitySet = startEdmEntitySet; // since we have only one segment

            // 2. step: retrieve the data from backend
            List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
            responseEntity = storage.readEntityData(startEdmEntitySet, keyPredicates);
        } else if (segmentCount == 2) { // navigation
            UriResource segment = resourceParts.get(1); // in our example we don't support more complex URIs
            if (segment instanceof UriResourceNavigation) {
                UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) segment;
                EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
                responseEdmEntityType = edmNavigationProperty.getType();
                // contextURL displays the last segment
                responseEdmEntitySet = UtilForAction.getNavigationTargetEntitySet(startEdmEntitySet, edmNavigationProperty);

                // 2nd: fetch the data from backend.
                // e.g. for the URI: Products(1)/Category we have to find the correct Category entity
                List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
                // e.g. for Products(1)/Category we have to find first the Products(1)
                Entity sourceEntity = storage.readEntityData(startEdmEntitySet, keyPredicates);

                // now we have to check if the navigation is
                // a) to-one: e.g. Products(1)/Category
                // b) to-many with key: e.g. Categories(3)/Products(5)
                // the key for nav is used in this case: Categories(3)/Products(5)
                List<UriParameter> navKeyPredicates = uriResourceNavigation.getKeyPredicates();

                if (navKeyPredicates.isEmpty()) { // e.g. DemoService.svc/Products(1)/Category
                    responseEntity = storage.getRelatedEntity(sourceEntity, responseEdmEntityType);
                } else { // e.g. DemoService.svc/Categories(3)/Products(5)
                    responseEntity = storage.getRelatedEntity(sourceEntity, responseEdmEntityType, navKeyPredicates);
                }
            }else if (segment instanceof UriResourceFunction) {
                UriResourceFunction uriResourceFunction = (UriResourceFunction) segment;

                // 2nd: fetch the data from backend.
                // first fetch the target entity type
                String targetEntityType = uriResourceFunction.getFunction().getReturnType().getType().getName();

                // contextURL displays the last segment
                for(EdmEntitySet entitySet : serviceMetadata.getEdm().getEntityContainer().getEntitySets()){
                    if(targetEntityType.equals(entitySet.getEntityType().getName())){
                        responseEdmEntityType = entitySet.getEntityType();
                        responseEdmEntitySet = entitySet;
                        break;
                    }
                }

                // error handling for null entities
                if (targetEntityType == null || responseEdmEntitySet == null) {
                    throw new ODataApplicationException("Entity not found.",
                            HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
                }

                // then fetch the entity collection for the target type
                responseEntity = storage.readEntityData(targetEntityType);
            }
        } else {
            // this would be the case for e.g. Products(1)/Category/Products(1)/Category
            throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
        }

        if (responseEntity == null) {
            // this is the case for e.g. DemoService.svc/Categories(4) or DemoService.svc/Categories(3)/Products(999)
            throw new ODataApplicationException("Nothing found.", HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ROOT);
        }

        EdmEntityType edmEntityType = responseEdmEntitySet.getEntityType();
        SelectOption selectOption = uriInfo.getSelectOption();
        ExpandOption expandOption = uriInfo.getExpandOption();

        String selectList = odata.createUriHelper().buildContextURLSelectList(edmEntityType, expandOption, selectOption);
        // 3. serialize
        ContextURL contextUrl = ContextURL.with().entitySet(responseEdmEntitySet)
                .selectList(selectList)
                .suffix(Suffix.ENTITY)
                .build();
        //EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextUrl).build();

        // adding the selectOption to the serializerOpts will actually tell the lib to do the job
        final String id = request.getRawBaseUri() + "/" + responseEdmEntitySet.getName();
        EntitySerializerOptions opts = EntitySerializerOptions.with()
                .contextURL(contextUrl)
                .select(selectOption)
                .expand(expandOption)
                .build();

        ODataSerializer serializer = odata.createSerializer(responseFormat);
        SerializerResult serializerResult = serializer.entity(serviceMetadata,
                responseEdmEntityType, responseEntity, opts);

        // 4. configure the response object
        response.setContent(serializerResult.getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    }

}

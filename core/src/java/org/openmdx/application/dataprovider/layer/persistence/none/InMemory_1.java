/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: InMemory_1 class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.dataprovider.layer.persistence.none;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.layer.persistence.common.AbstractPersistence_1;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.application.dataprovider.spi.OperationAwareLayer_1;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.SparseArray;

/**
 * An in-memory data store
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class InMemory_1 extends AbstractPersistence_1 {

    /**
     * Constructor 
     *
     */
    public InMemory_1(
    ) {
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.spi.OperationAwareLayer_1#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new InMemoryLayerInteraction(connection);
    }

    /**
     * Lookup a feature's meta-data
     * 
     * @param objectClass
     * 
     * @param featureName
     * @return the feature's meta-data
     * @throws ServiceException 
     */
    ModelElement_1_0 getFeatureDef(
        String objectClass,
        String featureName
    ) throws ServiceException{
        return (ModelElement_1_0)getModel().getElement(objectClass).objGetMap("allFeature").get(featureName);
    }
    
    /**
     * InMemoryIterator
     *
     */
    private static class InMemoryIterator implements Serializable {

        private static final long serialVersionUID = 3905236814703769655L;
        private final AttributeSpecifier[] attributeSpecifier;
        private final FilterProperty[] attributeFilter;

        InMemoryIterator(
          FilterProperty[] attributeFilter,
          AttributeSpecifier[] attributeSpecifier
        ) { 
          this.attributeFilter = attributeFilter;
          this.attributeSpecifier = attributeSpecifier;
        }
       
          /**
           */
          FilterProperty[] getAttributeFilter() {
              return attributeFilter;
          }
          
          /**
           */
          AttributeSpecifier[] getAttributeSpecifier() {
              return attributeSpecifier;
          }

      }
    
    /**
     * Large object holders
     */
    static interface Lob extends Serializable {

        /**
         * Retrieve the large object's value
         * @return
         */
        Object getValue(
        );

    }

    /**
     * This class represents a binary large object
     */
    static final class Blob implements Lob {

        /**
         * 
         */
        private static final long serialVersionUID = 3763099665276547641L;
        /**
         * @serial
         */
        private byte[] value;

        /**
         * Constructor
         * 
         * @param source
         * @throws IOException
         */
        Blob(
            InputStream source
        ) throws IOException {
            ByteArrayOutputStream target = new ByteArrayOutputStream(source.available());
            byte[] buffer = new byte[DEFAULT_CHUNK_SIZE];
            for(
                    int i = source.read(buffer);
                    i > 0;
                    i = source.read(buffer)
            ) target.write(buffer, 0, i);
            this.value = target.toByteArray();
        }

        /**
         * Retrieve the BLOB's content
         * 
         * @return
         */
        public Object getValue(
        ){
            return new ByteArrayInputStream(this.value);
        }

    }

    /**
     * This class represents a character large object
     */
    static final class Clob implements Lob {


        /**
         * 
         */
        private static final long serialVersionUID = 3688510986948457785L;
        /**
         * @serial
         */
        private char[] value;

        /**
         * Constructor
         * 
         * @param source
         * @throws IOException
         */
        Clob(
            Reader source
        ) throws IOException {
            CharArrayWriter target = new CharArrayWriter();
            char[] buffer = new char[DEFAULT_CHUNK_SIZE];
            for(
                    int i = source.read(buffer);
                    i > 0;
                    i = source.read(buffer)
            ) target.write(buffer, 0, i);
            this.value = target.toCharArray();
        }

        /**
         * Retrieve the BLOB's content
         * 
         * @return
         */
        public Object getValue(
        ){
            return new CharArrayReader(this.value);
        }

    }

    /**
     * Get an object's reference.
     *
     * @param path
     * @param referenceMustExist Defines whether an exception should be thrown if the
     *        reference doesn't exist
     * @param isPreferringNotFoundException
     * @return

     * @exception ServiceException BAD_PARAMETER
     *        if the path refers to an object, not a reference
     * @exception ServiceException NOT_FOUND
     *        if referenceMustExist is true while the reference 
     *        does not exist
     */
    protected Map<String,MappedRecord> getReference(
        Path path,
        boolean referenceMustExist,
        boolean isPreferringNotFoundException
    ) throws ServiceException {
        if(path.size() % 2 == 0) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Path refers to a reference, not an object",
            new BasicException.Parameter("path",path)
        );
        Map<String,MappedRecord> result = this.referenceMap.get(path.getParent());
        if(referenceMustExist && result == null) {
            if(isPreferringNotFoundException) {            
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "Reference \u00ab" + path.getParent() + "\u00bb not found",
                    new BasicException.Parameter("object", path),
                    new BasicException.Parameter("reference", path.getParent())
                );
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.common.AbstractPersistence_1#activate(short, org.openmdx.application.configuration.Configuration, org.openmdx.application.dataprovider.spi.Layer_1)
     */
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        if(!configuration.values(SharedConfigurationEntries.BATCH_SIZE).isEmpty()) {
            this.batchSize = ((Number)configuration.values(SharedConfigurationEntries.BATCH_SIZE).get(Integer.valueOf(0))).intValue();
        }
        String namespaceId = configuration.getFirstValue(
            SharedConfigurationEntries.NAMESPACE_ID
        );
        if(!configuration.values(LayerConfigurationEntries.CLONE_REPLY).isEmpty()) {
            this.cloneReply = ((Boolean)(configuration.values(LayerConfigurationEntries.CLONE_REPLY).get(Integer.valueOf(0)))).booleanValue();
        }
        synchronized(InMemory_1.referenceMaps) {
            this.referenceMap = (Map)InMemory_1.referenceMaps.get(namespaceId);
            if (this.referenceMap == null) {
                SysLog.detail("Create namespace", namespaceId);
                InMemory_1.referenceMaps.put(
                    namespaceId,
                    this.referenceMap = new HashMap(INITIAL_REFERENCE_MAP_CAPACITY)
                );
            } else {
                SysLog.detail("Attach to namespace", namespaceId);
            }
        }
    }

    /**
     * Prepare the reply object.
     * <p>
     * 
     * 
     *
     * @param source
     *      the original object.
     * @param attributeSelector
     *      specifies the set of attributes to be returned.
     * @param attributeSpecifiers
     *      specifies specific attributes to be returned.
     *
     * @return  a copy of the object containing the requested attributes.
     */
    protected MappedRecord replyObject(
        MappedRecord source,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifiers
    ) throws ServiceException {
        switch (attributeSelector) {
		    case AttributeSelectors.NO_ATTRIBUTES:
		        return Facades.newObject(
		            Object_2Facade.getPath(source)
		        ).getDelegate();
		    case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
		    case AttributeSelectors.ALL_ATTRIBUTES: 
		        return this.cloneReply
		            ? Object_2Facade.cloneObject(source)
		            : source;
		    case AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES: {
		        MappedRecord target = Object_2Facade.cloneObject(source);
		        Set attributes = new HashSet();
		        for(AttributeSpecifier attributeSpecifier : attributeSpecifiers){
		            attributes.add(attributeSpecifier.name());
		        }
		        attributes.add(SystemAttributes.CREATED_AT);
		        attributes.add(SystemAttributes.CREATED_BY);
		        attributes.add(SystemAttributes.MODIFIED_AT);
		        attributes.add(SystemAttributes.MODIFIED_BY);
		        Object_2Facade.getValue(target).keySet().retainAll(attributes);
		        return target;
		    }
		    default:
		        throw new ServiceException(
		            BasicException.Code.DEFAULT_DOMAIN,
		            BasicException.Code.BAD_PARAMETER,
		            "Unsupported attributeSelector", 
		            new BasicException.Parameter(
		                "attributeSelector",
		                AttributeSelectors.toString(attributeSelector)
		            )
		        );    
		}
    }

    /**
     * Compare the value of an attribute of two objects.
     * <p>
     * If a value is not present or null, it is treated as larger than any 
     * existing and equal to null
     * 
     * @param a  first object to compare
     * @param b  second object to compare
     * @param attribute  attribute by which the compare takes place
     * @param index      index of the value to compare 
     * 
     * @return -1 if a.attribute < b.attribute; 
     *           0 if a.attribute == b.attribute;
     *           1 if a.attribute > b.attribute
     */
    protected int compare(
        MappedRecord a,
        MappedRecord b,
        String attribute,
        int index
    ) throws ServiceException {
        int aLarger = 1;
        int bLarger = -1;
        int equal = 0;
        int result = 0;
        if(SystemAttributes.OBJECT_IDENTITY.equals(attribute)) {
            return Object_2Facade.getPath(a).compareTo(Object_2Facade.getPath(b));
        }
        else if(SystemAttributes.OBJECT_CLASS.equals(attribute)) {
            return Object_2Facade.getObjectClass(a).compareTo(Object_2Facade.getObjectClass(b));
        }
        Object_2Facade aFacade = Facades.asObject(a);
        Object_2Facade bFacade = Facades.asObject(b);
        Object aValues = aFacade.getAttributeValues(attribute);
        int aSize = aValues instanceof SparseArray ?
            ((SparseArray)aValues).size() :
                ((List)aValues).size();
        Object bValues = bFacade.getAttributeValues(attribute);                
        int bSize = bValues instanceof SparseArray ?
            ((SparseArray)bValues).size() :
                ((List)bValues).size();
        // a
        if(
            (aValues == null) || 
            (aSize <= index)
        ) {
            result = aLarger;
        }
        // b
        if(
            (bValues == null) || 
            (bSize <= index)
        ) {
            if(result == aLarger) {
                return equal;
            }
            return bLarger;
        }
        if(result == aLarger) {
            return aLarger;
        }
        Object aValue = aValues instanceof SparseArray ?
            ((SparseArray)aValues).get(Integer.valueOf(index)) :
                ((List)aValues).get(index);
        Object bValue = bValues instanceof SparseArray ?
            ((SparseArray)bValues).get(Integer.valueOf(index)) :
                ((List)bValues).get(index);
        return ((Comparable)aValue).compareTo(bValue);
    }

    /**
     * InMemoryLayerInteraction
     *
     */
    public class InMemoryLayerInteraction extends OperationAwareLayer_1.LayerInteraction {
        
        public InMemoryLayerInteraction(
            RestConnection connection
        ) throws ResourceException {
            super(connection);
        }
            
        /**
         * Get the object specified by the requests's path 
         *
         * @param request   the request, an in out parameter
         *
         * @exception ServiceException  on failure
         */
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            synchronized(InMemory_1.this.referenceMap){
                Path path = input.getPath();
                Map<String, MappedRecord> reference = InMemory_1.this.getReference(
                    path, 
                    true, // referenceMustExist
                    input.isPreferringNotFoundException()
                );
                MappedRecord source = reference == null ? null : reference.get(
                    path.getLastSegment().toClassicRepresentation()
                );
                if(source == null) {
                    if(input.isPreferringNotFoundException()) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            "Object \u00ab" + path + "\u00bb not found",
                            new BasicException.Parameter("path",path)
                        );
                    }
                } else {
                    MappedRecord object = InMemory_1.this.replyObject(
    				    source,
    				    request.attributeSelector(),
    				    request.attributeSpecifier()
    				);
    				reply.getResult().add(object);
                }
            }
            return true;
        }

        /**
         * Get the objects specified by the references and filter properties
         *
         * @param request   the request, an in out parameter
         *
         * @exception ServiceException  on failure
         */
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            
            synchronized(InMemory_1.this.referenceMap){
                InMemoryIterator specification = new InMemoryIterator(
                    request.attributeFilter(),
                    request.attributeSpecifier()
                );
                final List sortersList = new ArrayList();
                for (int i=0; i<specification.getAttributeSpecifier().length; i++) {
                    AttributeSpecifier specifier = specification.getAttributeSpecifier()[i];
                    if (specifier.order() != SortOrder.UNSORTED.code() ) {
                        sortersList.add(specifier);
                    }
                }
                final AttributeSpecifier[] sorters = (AttributeSpecifier[])
                sortersList.toArray(new AttributeSpecifier[sortersList.size()]);
                MappedRecordFilter filter;
                List<FilterProperty> attributeFilter = new ArrayList<FilterProperty>(
                    Arrays.asList(specification.getAttributeFilter())
                );
                FilterProperty objectClassFilterProperty = null;
                for(Iterator<FilterProperty> i = attributeFilter.iterator(); i.hasNext(); ) {
                    FilterProperty p = i.next();
                    if(SystemAttributes.OBJECT_INSTANCE_OF.equals(p.name()) && !p.values().isEmpty()) {
                        Set<String> allSubtypes = InMemory_1.this.getAllSubtypes((String)p.getValue(0));
                        if(allSubtypes != null) {
                            objectClassFilterProperty = new FilterProperty(
                                p.quantor(),
                                SystemAttributes.OBJECT_CLASS,
                                p.operator(),
                                allSubtypes.toArray()
                            );
                        }
                        i.remove();
                    }
                }
                if(objectClassFilterProperty != null) {
                    attributeFilter.add(objectClassFilterProperty);
                }
                filter = new MappedRecordFilter(
                    attributeFilter.toArray(new FilterProperty[attributeFilter.size()])
                ){

                    /* (non-Javadoc)
                     * @see org.openmdx.application.dataprovider.layer.persistence.none.MappedRecordFilter#getValuesIterator(java.lang.Object, java.lang.String)
                     */
                    @SuppressWarnings("synthetic-access")
                    @Override
                    protected Iterator<?> getValuesIterator(
                        Object candidate,
                        String attribute
                    ) throws ServiceException {
                        if(SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute)) {
                            final Object_2Facade object = Facades.asObject((MappedRecord) candidate);
                            final List<?> superTypes = getModel().getElement(object.getObjectClass()).objGetList("allSupertype");
                            return new Iterator<String>(){
                                
                                private final Iterator<?> delegate = superTypes.iterator();

//                              @Override
                                public boolean hasNext() {
                                    return delegate.hasNext();
                                }
                                

//                              @Override
                                public String next() {
                                    Path superType = (Path) this.delegate.next();
                                    return superType.getLastSegment().toClassicRepresentation();
                                }

//                              @Override
                                public void remove() {
                                    throw new UnsupportedOperationException();
                                }
                                
                            };
                        } else {
                            return super.getValuesIterator(candidate, attribute);
                        }
                    }

                    @Override
                    protected Iterator<?> getObjectIterator(
                        final Object candidate,
                        final String attribute
                    ) throws Exception {
                        Object_2Facade object = Facades.asObject((MappedRecord) candidate);
                        ModelElement_1_0 featureDef = getFeatureDef(object.getObjectClass(), attribute);
                        if(featureDef.getModel().referenceIsStoredAsAttribute(featureDef)) {
                            return new Iterator<MappedRecord>(){

                                private final Iterator<?> delegate = getValuesIterator(candidate, attribute);
                                
//                              @Override
                                public boolean hasNext() {
                                    return delegate.hasNext();
                                }

//                              @Override
                                public MappedRecord next() {
                                    Path path = (Path) this.delegate.next();
                                    Map<String, MappedRecord> container = InMemory_1.this.referenceMap.get(
                                        path.getParent()
                                    );
                                    MappedRecord object = container == null ? null : container.get(path.getLastSegment().toClassicRepresentation());
                                    if(object == null) {
                                        throw new RuntimeServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.NOT_FOUND,
                                            "Referenced object not found",
                                            new BasicException.Parameter("xri", path)
                                        );
                                    } else {
                                        return object;
                                    }
                                }

//                              @Override
                                public void remove() {
                                    throw new UnsupportedOperationException();
                                }
                                
                            };
                        } else {
                            Map<String, MappedRecord> container = InMemory_1.this.referenceMap.get(
                                object.getPath().getChild(attribute)
                            );
                            return (container == null ? Collections.emptyMap() : container).values().iterator();
                        }
                    }
                    
                };
                List<MappedRecord> objects = new ArrayList<MappedRecord>();
                List<MappedRecord> sortedObjects = new ArrayList<MappedRecord>();
                Collection unfiltered = null;
                boolean isExtent = (request.path().size() == 6) && "extent".equals(request.path().getLastSegment().toClassicRepresentation());
                if(isExtent) {
                    unfiltered = new ArrayList();
                    for(Iterator i = referenceMap.values().iterator(); i.hasNext(); ) {
                        unfiltered.addAll(
                            ((Map)i.next()).values()
                        );
                    }
                }
                else {
                    Map<String,MappedRecord> unfilteredMap = InMemory_1.this.referenceMap.get(
                        request.path()
                    );
                    unfiltered = unfilteredMap == null
                    ? null
                        : unfilteredMap.values();
                }
                int replySize = java.lang.Math.min(
                    request.size(),
                    InMemory_1.this.batchSize
                );
                int replyPosition = request.position();
                if(request.direction() == SortOrder.DESCENDING.code()) {
                    if(replySize > replyPosition) replySize = replyPosition + 1;
                    replyPosition = replyPosition + 1 - replySize;
                }
                int position = 0;
                if(unfiltered != null) {
                    for (
                        Iterator<MappedRecord> objectIterator = unfiltered.iterator();
                        objectIterator.hasNext();
                    ){
                        MappedRecord object = objectIterator.next();
                        if(filter.accept(object)) {
                            position++;
                            if (sorters.length == 0) {
                                if(
                                    (position > replyPosition) &&
                                    (objects.size() < replySize)
                                ) {
                                    objects.add(
                                        InMemory_1.this.replyObject(
                                            object,
                                            request.attributeSelector(),
                                            specification.getAttributeSpecifier()
                                        )
                                    );
                                }
                            } 
                            else {
                                // with sorters must first find all then get relevant part
                                boolean added = false;
                                // add sorted
                                for (int pos = 0; pos < sortedObjects.size() && !added; pos++) {
                                    int diff = 0;
                                    for (int i=0; i<sorters.length && diff == 0 && !added; i++) {
                                        diff = InMemory_1.this.compare(sortedObjects.get(pos), object, sorters[i].name(), sorters[i].position());
                                        if ((diff > 0 && sorters[i].order() == SortOrder.ASCENDING.code())
                                                ||
                                                (diff < 0 && sorters[i].order() == SortOrder.DESCENDING.code())
                                        ) {
                                            sortedObjects.add(pos, object); // shifts existing objects
                                            added = true;
                                        }
                                        // if (diff == 0) just try next sorter 
                                    }
                                    if (!added && diff == 0) {
                                        // sequence is not defined, just add before current pos
                                        sortedObjects.add(pos, object);
                                        added = true;
                                    }
                                }
                                if (!added) {
                                    sortedObjects.add(object);
                                }
                            }        
                        }
                    }
                }
                if (sorters.length > 0) {
                    for(
                        Iterator<MappedRecord> o = sortedObjects.listIterator(replyPosition);
                        o.hasNext() && objects.size() < replySize;
                    ) {
                        objects.add(
                            InMemory_1.this.replyObject(
                                o.next(),
                                request.attributeSelector(),
                                specification.getAttributeSpecifier()
                            )
                        );
                    }
                }
                // reply
                reply.getResult().addAll(objects);
                boolean hasMore = request.position() + objects.size() < position;
                if(!hasMore) {
                    reply.setTotal(position);
                }
                reply.setHasMore(
                    hasMore
                );
                return true;
            }
        }
    
        /**
         * Create a new object
         *
         * @param request   the request, an in out parameter
         *
         * @exception ServiceException  in case of failure
         */
        @Override
        public boolean create(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            synchronized(InMemory_1.this.referenceMap){
                Path path = request.path();
                Map<String,MappedRecord> container = InMemory_1.this.getReference(
                    path, 
                    false, // referenceMustExist
                    false // isPreferringNotFoundException
                );
                if(container == null) {
                    InMemory_1.this.referenceMap.put(
                        path.getParent(),
                        container = new HashMap(INITIAL_REFERENCE_MAP_CAPACITY)
                    ); // createReferencesOnDemand
                }
                String objectId = path.getLastSegment().toClassicRepresentation();
                if(container.containsKey(objectId)) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.DUPLICATE,
                        "Object already exists",
                        new BasicException.Parameter("xri",path)
                    );
                }
                try {
                    container.put(
                        objectId,
                        Object_2Facade.cloneObject(request.object())
                    );
                    if(reply.getResult() != null) {
                        reply.getResult().add(
                            InMemory_1.this.replyObject(
                                request.object(),
                                request.attributeSelector(),
                                request.attributeSpecifier()
                            )
                        );
                    }
                    return true;
                } 
                catch (Exception exception) {
                    Error assertionFailure = BasicException.initHolder(
                        new Error(
                            "Compression/decompression failure",
                            BasicException.newEmbeddedExceptionStack(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN, 
                                BasicException.Code.ASSERTION_FAILURE
                            )
                        )
                    );
                    SysLog.error(
                        assertionFailure.getMessage(), 
                        assertionFailure.getCause()
                    );
                    throw assertionFailure;
                }     
            }
        }   
    
        /**
         * Removes an object including its descendents
         *
         * @param request   the request, an in out parameter
         *
         * @exception ServiceException  in case of failure
         */
        @Override
        public boolean delete(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            synchronized(InMemory_1.this.referenceMap){
                DataproviderRequest getRequest = new DataproviderRequest(
                    input.getDelegate(),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.NO_ATTRIBUTES,
                    null
                );
                Path path = request.path();     
                this.get(
				    getRequest.getInteractionSpec(),
				    Facades.newQuery(path),
				    super.newDataproviderReply().getResult()
				);
                Map<String,MappedRecord> reference = InMemory_1.this.getReference(
                    path, 
                    false, // referenceMustExist 
                    false // isPreferringNotFoundException
                );
                MappedRecord source = reference == null 
                    ? null 
                    : reference.remove(path.getLastSegment().toClassicRepresentation());
                for(
                    Iterator iterator = referenceMap.keySet().iterator();
                    iterator.hasNext();
                ) {
                    Path r = (Path)iterator.next();
                    if(r.startsWith(path)) {
                        SysLog.trace("removing reference", "" + r + " containing " + ((Map)referenceMap.get(r)).size() + " objects");
                        iterator.remove();
                    }
                    else {
                        SysLog.trace("not removing", r);
                    }
                } 
                if(source != null && output != null) {
                    reply.getResult().add(
                        InMemory_1.this.replyObject(
                            source,
                            request.attributeSelector(),
                            request.attributeSpecifier()
                        )
                    );
                }
                return true;
            }
        }
    
        /* (non-Javadoc)
         * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#put(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Object_2Facade, javax.resource.cci.IndexedRecord)
         */
        @Override
        public boolean put(
            RestInteractionSpec ispec,
            Object_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            DataproviderRequest request = this.newDataproviderRequest(ispec, input);
            DataproviderReply reply = this.newDataproviderReply(output);
            
            synchronized(InMemory_1.this.referenceMap){
                Object_2Facade source = Facades.asObject(request.object());            
                Path path = source.getPath();
                if(path == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Object not found",
                        new BasicException.Parameter("path")
                    );
                }
                Map<String,MappedRecord> container = InMemory_1.this.getReference(
                    path, 
                    true, // referenceMustExist 
                    true // isPreferringNotFoundException
                );
                Object_2Facade target = Facades.asObject(container.get(path.getLastSegment().toClassicRepresentation()));
                if(target == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Object not found",
                        new BasicException.Parameter("xri", path)
                    );
                }
                target.getValue().putAll(source.getValue());
                MappedRecord object = InMemory_1.this.replyObject(
                    target.getDelegate(),
                    request.attributeSelector(),
                    request.attributeSpecifier()
                );
                if(output != null) {
                    reply.getResult().add(
                        object
                    ); 
                }
                return true;
            }
        }
        
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    protected static int INITIAL_REFERENCE_MAP_CAPACITY = 213;
    protected static final Map referenceMaps = new HashMap();
    protected Map<Path,Map<String,MappedRecord>> referenceMap = null;
    
    /**
     * @deprecated the dataprovider 2 stack will support the batch size where
     * required by the plug-in implementation
     */
    protected int batchSize = Integer.MAX_VALUE;
    
    /**
     * @deprecated the dataprovider 2 stack will support unmodifiable records
     * eliminating the need to clone them
     */
    protected boolean cloneReply = true;

}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InMemory_1.java,v 1.3 2009/06/02 13:19:06 wfro Exp $
 * Description: InMemory_1 class
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/02 13:19:06 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderOperations;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.layer.persistence.common.AbstractIterator;
import org.openmdx.application.dataprovider.layer.persistence.common.AbstractPersistence_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.Orders;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * An in-memory data store
 */
@SuppressWarnings("unchecked")
public class InMemory_1
extends AbstractPersistence_1 {

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

    //-------------------------------------------------------------------------
    /**
     * Get an object's reference
     *
     * @param   path  
     *        the reference's path
     * @param   referenceMustExist
     *        Defines whether an exception should be thrown if the
     *        reference doesn't exist
     *
     * @exception ServiceException BAD_PARAMETER
     *        if the path refers to an object, not a reference
     * @exception ServiceException NOT_FOUND
     *        if referenceMustExist is true while the reference 
     *        does not exist
     */
    private Map<String,MappedRecord> getReference(
        Path path,
        boolean referenceMustExist
    ) throws ServiceException {
        if(path.size() % 2 == 0) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Path refers to a reference, not an object",
            new BasicException.Parameter("path",path)
        );
        Map<String,MappedRecord> result = this.referenceMap.get(path.getParent());
        if(referenceMustExist && result == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_FOUND,
            "Reference \u00ab" + path.getParent() + "\u00bb not found",
            new BasicException.Parameter("object", path),
            new BasicException.Parameter("reference", path.getParent())
        );
        return result;
    }

    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        if(!configuration.values(SharedConfigurationEntries.BATCH_SIZE).isEmpty()) {
            this.batchSize = ((Number)configuration.values(SharedConfigurationEntries.BATCH_SIZE).get(0)).intValue();
        }
        String namespaceId = configuration.getFirstValue(
            SharedConfigurationEntries.NAMESPACE_ID
        );
        synchronized(InMemory_1.referenceMaps) {
            this.referenceMap = (Map)InMemory_1.referenceMaps.get(namespaceId);
            if (this.referenceMap == null) {
                List storageFolder = configuration.values(LayerConfigurationEntries.STORAGE_FOLDER);
                if(storageFolder.size() == 0) {        
                    SysLog.detail("Create namespace", namespaceId);
                    InMemory_1.referenceMaps.put(
                        namespaceId,
                        this.referenceMap = new HashMap(INITIAL_REFERENCE_MAP_CAPACITY)
                    );
                }
                else {
                    InMemory_1.referenceMaps.put(
                        namespaceId,
                        this.referenceMap = new HashMap(INITIAL_REFERENCE_MAP_CAPACITY)
                    );
                }
            } 
            else {
                SysLog.detail("Attach to namespace", namespaceId);
            }
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Deactivates a dataprovider layer
     * <p>
     * Subclasses overriding this method have to apply the following pattern:
     * <pre>
     *  public void deactivate(
     *  ) throws Exception, ServiceException {
     *    // local activation code
     *    super.deactivate();
     *  }
     * </pre>   
     */
    public void deactivate(
    ) throws Exception, ServiceException {
    }

    //-------------------------------------------------------------------------
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
     * @param attributeSpecifier
     *      specifies specific attributes to be returned.
     *
     * @return  a copy of the object containing the requested attributes.
     */
    private MappedRecord replyObject(
        MappedRecord source,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        switch (attributeSelector) {
            case AttributeSelectors.NO_ATTRIBUTES:
                try {
                    return ObjectHolder_2Facade.newInstance(
                        ObjectHolder_2Facade.getPath(source)
                    ).getDelegate();
                }
                catch(Exception e) {
                    throw new ServiceException(e);
                }
            case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
            case AttributeSelectors.ALL_ATTRIBUTES: {
                MappedRecord target;
                try {
                    target = ObjectHolder_2Facade.cloneObject(source);
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
                return target;
            }
            case AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES: {
                MappedRecord target;
                try {
                    target = ObjectHolder_2Facade.cloneObject(source);
                } 
                catch (ResourceException e) {
                    throw new ServiceException(e);
                }
                Set attributes = new HashSet();
                for(
                    int index = 0;
                    index < attributeSpecifier.length;
                    index++
                ) {
                    attributes.add(attributeSpecifier[index].name());
                }
                attributes.add(SystemAttributes.CREATED_AT);
                attributes.add(SystemAttributes.CREATED_BY);
                attributes.add(SystemAttributes.MODIFIED_AT);
                attributes.add(SystemAttributes.MODIFIED_BY);
                ObjectHolder_2Facade.getValue(target).keySet().retainAll(attributes);
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

    //-------------------------------------------------------------------------
    /**
     * Get the object specified by the requests's path 
     *
     * @param request   the request, an in out parameter
     *
     * @exception ServiceException  on failure
     */
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        synchronized(this.referenceMap){
            Path path = ObjectHolder_2Facade.getPath(request.object());;
            MappedRecord source = this.getReference(
                path,
                true
            ).get(
                path.getBase()
            );
            if(source == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "Object \u00ab" + path + "\u00bb not found",
                    new BasicException.Parameter("path",path)
                );
            }
            try {
                return new DataproviderReply(
                    this.replyObject(
                        ObjectHolder_2Facade.cloneObject(source),
                        request.attributeSelector(),
                        request.attributeSpecifier()
                    )
                );
            }
            catch(Exception e) {
                throw new ServiceException(e);
            }
        }
    }

    //-------------------------------------------------------------------------
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
    private int compare(
        MappedRecord a,
        MappedRecord b,
        String attribute,
        int index
    ) throws ServiceException {
        try {
            int aLarger = 1;
            int bLarger = -1;
            int equal = 0;
            int result = 0;
            if(SystemAttributes.OBJECT_IDENTITY.equals(attribute)) {
                return ObjectHolder_2Facade.getPath(a).compareTo(ObjectHolder_2Facade.getPath(b));
            }
            else if(SystemAttributes.OBJECT_CLASS.equals(attribute)) {
                return ObjectHolder_2Facade.getObjectClass(a).compareTo(ObjectHolder_2Facade.getObjectClass(b));
            }
            ObjectHolder_2Facade aFacade = ObjectHolder_2Facade.newInstance(a);
            ObjectHolder_2Facade bFacade = ObjectHolder_2Facade.newInstance(b);
            SparseList aValue = aFacade.getAttributeValues(attribute);
            SparseList bValue = bFacade.getAttributeValues(attribute);
            // a
            if(
                (aValue == null) || 
                (aValue.size() <= index)
            ) {
                result = aLarger;
            }
            // b
            if(
                (bValue == null) || 
                (bValue.size() <= index)
            ) {
                if(result == aLarger) {
                    return equal;
                }
                return bLarger;
            }
            if(result == aLarger) {
                return aLarger;
            }
            if(aValue.get(0) instanceof Comparable){
                return ((Comparable)aValue.get(index)).compareTo(
                    bValue.get(index)
                );
            }
            else {
                return -1; // not sorted
            }
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Get the objects specified by the references and filter properties
     *
     * @param request   the request, an in out parameter
     *
     * @exception ServiceException  on failure
     */
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        synchronized(this.referenceMap){
            InMemoryIterator specification = 
                request.operation() == DataproviderOperations.ITERATION_START ?
                    new InMemoryIterator(
                        request.attributeFilter(),
                        request.attributeSpecifier()
                    ) :
                    (InMemoryIterator)AbstractIterator.deserialize(
                        (byte[])request.context(DataproviderReplyContexts.ITERATOR).get(0)
                    );
                final List sortersList = new ArrayList();
                for (int i=0; i<specification.getAttributeSpecifier().length; i++) {
                    AttributeSpecifier specifier = specification.getAttributeSpecifier()[i];
                    if (specifier.order() != Orders.ANY ) {
                        sortersList.add(specifier);
                    }
                }
                final AttributeSpecifier[] sorters = (AttributeSpecifier[])
                sortersList.toArray(new AttributeSpecifier[sortersList.size()]);
                MappedRecordFilter filter;
                try {
                    filter = new MappedRecordFilter(specification.getAttributeFilter());
                }   
                catch (RuntimeException exception){
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Invalid attribute filter",
                        new BasicException.Parameter("attributeFilter", (Object[])specification.getAttributeFilter())
                    );
                }    
                List<MappedRecord> objects = new ArrayList<MappedRecord>();
                List<MappedRecord> sortedObjects = new ArrayList<MappedRecord>();
                Collection unfiltered = null;
                boolean isExtent = (request.path().size() == 6) && "extent".equals(request.path().getBase());
                if(isExtent) {
                    unfiltered = new ArrayList();
                    for(Iterator i = referenceMap.values().iterator(); i.hasNext(); ) {
                        unfiltered.addAll(
                            ((Map)i.next()).values()
                        );
                    }
                }
                else {
                    Map<String,MappedRecord> unfilteredMap = this.referenceMap.get(
                        request.path()
                    );
                    unfiltered = unfilteredMap == null
                    ? null
                        : unfilteredMap.values();
                }
                int replySize = java.lang.Math.min(
                    request.size(),
                    this.batchSize
                );
                int replyPosition = request.position();
                if(request.direction() == Directions.DESCENDING) {
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
                                        this.replyObject(
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
                                        diff = this.compare(sortedObjects.get(pos), object, sorters[i].name(), sorters[i].position());
                                        if ((diff > 0 && sorters[i].order() == Directions.ASCENDING)
                                                ||
                                                (diff < 0 && sorters[i].order() == Directions.DESCENDING)
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
                            this.replyObject(
                                o.next(),
                                request.attributeSelector(),
                                specification.getAttributeSpecifier()
                            )
                        );
                    }
                }
                // reply
                DataproviderReply reply = new DataproviderReply(objects);
                boolean hasMore = request.position() + objects.size() < position;
                if(request.operation() == DataproviderOperations.ITERATION_START){
                    reply.context(DataproviderReplyContexts.ITERATOR).set(
                        0,
                        AbstractIterator.serialize(specification)
                    );
                    reply.context(DataproviderReplyContexts.ATTRIBUTE_SELECTOR).set(
                        0,
                        new Short(request.attributeSelector())
                    );
                    if(!hasMore) {
                        reply.context(DataproviderReplyContexts.TOTAL).set(
                            0,
                            new Integer(position)
                        );
                    }
                }
                reply.context(DataproviderReplyContexts.HAS_MORE).set(
                    0,
                    hasMore
                );
                return reply;     
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Create a new object
     *
     * @param request   the request, an in out parameter
     *
     * @exception ServiceException  in case of failure
     */
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        synchronized(this.referenceMap){
            Path path = request.path();
            Map<String,MappedRecord> container = this.getReference(path, false);
            if(container == null) {
                this.referenceMap.put(
                    path.getParent(),
                    container = new HashMap(INITIAL_REFERENCE_MAP_CAPACITY)
                ); // createReferencesOnDemand
            }
            String objectId = path.getBase();
            if(container.containsKey(objectId)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DUPLICATE,
                    "Object \u00ab" + path + "\u00bb already exists",
                    new BasicException.Parameter("path",path)
                );
            }
            try {
                container.put(
                    objectId,
                    ObjectHolder_2Facade.cloneObject(request.object())
                );
                return new DataproviderReply(
                    this.replyObject(
                        request.object(),
                        request.attributeSelector(),
                        request.attributeSpecifier()
                    )
                );
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

    //-------------------------------------------------------------------------
    /**
     * Removes an object including its descendents
     *
     * @param request   the request, an in out parameter
     *
     * @exception ServiceException  in case of failure
     */
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        synchronized(this.referenceMap){
            this.get(
                header,
                request
            ); // to throw NOT_FOUND if necessary
            Path path = request.path();     
            Map<String,MappedRecord> reference = getReference(
                path, 
                false
            );
            MappedRecord source = reference == null ? null :
                reference.remove(path.getBase());
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
            return source == null ?
                new DataproviderReply() :
                    new DataproviderReply(
                        this.replyObject(
                            source,
                            request.attributeSelector(),
                            request.attributeSpecifier()
                        )
                    ); 
        }
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#set(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        synchronized(this.referenceMap){
            Path path = request.path();
            Map reference = this.getReference(path, false);
            boolean isNew = reference == null ? 
                true : 
                reference.get(path.getBase()) == null ? 
                    true : 
                    false;
            if(isNew) {
                try {
                    ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance(request.object());
                    facade.attributeValues(SystemAttributes.CREATED_AT).addAll(
                        facade.attributeValues(SystemAttributes.MODIFIED_AT)
                    );
                    return this.create(
                        header, 
                        request
                    );
                }
                catch(Exception e) {
                    throw new ServiceException(e);
                }
            }
            else {
                return this.replace(
                    header, 
                    request
                );
            }
        }
    }

    //-------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        synchronized(this.referenceMap){
            MappedRecord source = request.object();            
            Path path = ObjectHolder_2Facade.getPath(source);
            Map<String,MappedRecord> container = this.getReference(
                path, 
                true
            );
            MappedRecord target = container.get(path.getBase());
            if(target == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "Object \u00ab" + path + "\u00bb not found",
                    new BasicException.Parameter("path",path)
                );
            }
            container.put(
                path.getBase(),
                source
            );
            return new DataproviderReply(
                this.replyObject(
                    target,
                    request.attributeSelector(),
                    request.attributeSpecifier()
                )
            ); 
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Modifies some of an object's attributes' values leaving the others
     * unchanged.
     *
     * @param request   the request, an in out parameter
     *
     * @exception ServiceException  in case of failure
     */
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        return this.replace(
            header, 
            request
        );
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static int INITIAL_REFERENCE_MAP_CAPACITY = 213;
    private static final Map referenceMaps = new HashMap();
    private Map<Path,Map<String,MappedRecord>> referenceMap = null;
    private int batchSize = Integer.MAX_VALUE;

}

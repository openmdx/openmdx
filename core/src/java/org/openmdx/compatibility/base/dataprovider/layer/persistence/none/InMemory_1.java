/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InMemory_1.java,v 1.27 2008/11/14 13:01:04 hburger Exp $
 * Description: InMemory_1 class
 * Revision:    $Revision: 1.27 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/14 13:01:04 $
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
package org.openmdx.compatibility.base.dataprovider.layer.persistence.none;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObjectFilter;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.common.AbstractIterator;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.common.AbstractPersistence_1;
import org.openmdx.compatibility.base.dataprovider.layer.persistence.common.Sequences;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
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

    /**
     * Helper class which allows to store DataproviderObject's in a memory
     * friendly way. Single-valued attributes are stored as object instead
     * of OffsetArrayList with one entry.
     */
    static class CompressedObject
    implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 3544673979526099512L;
        //-----------------------------------------------------------------------
        // create a compressed object from a DataproviderObject_1_0
        public CompressedObject(
            DataproviderObject_1_0 source
        ) throws ServiceException {
            this.toCompressed(source);
        }

        //-----------------------------------------------------------------------
        private void setAttributeValue(
            int attributeIndex,
            List values
        ) throws ServiceException{        
            try {
                this.attributeValues[attributeIndex] = compress(values);
            } catch (IOException e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.GENERIC,
                    "Saving of large object failed"
                );
            }
        }

        /**
         * Convert dataprovider values ot compressed values
         * 
         * @param values dataprovider values
         * 
         * @return compressed values
         * @throws IOException
         */
        private final static Object compress(
            List values
        ) throws IOException{
            if(values.isEmpty()) return null;
            Object value = values.get(0);
            return value instanceof InputStream ?
                new Blob((InputStream)value) :
                    value instanceof Reader ?
                        new Clob((Reader)value) :
                            values.size() == 1 ?
                                value :
                                    values;
        }

        //-----------------------------------------------------------------------
        Object getAttributeValue(
            int attributeIndex,
            int valueIndex
        ) {
            Object source = this.attributeValues[attributeIndex];
            return source == null ?
                null :
                    source instanceof List ?
                        ((List)source).get(valueIndex) :  
                            source instanceof Lob ?
                                ((Lob)source).getValue() : 
                                    valueIndex == 0 ?
                                        source :
                                            null;
        }

        //-----------------------------------------------------------------------
        int getAttributeIndex(
            String attribute
        ) {
            int index = -1;
            for(int i = 0; i < this.attributeNames.length; i++) {
                if(attribute.equals(this.attributeNames[i])) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        //-----------------------------------------------------------------------
        int getAttributeValuesSize(
            int attributeIndex
        ) {
            Object values = this.attributeValues[attributeIndex];
            return values == null
            ? 0
                : values instanceof List
                ? ((List)values).size()
                    : 1;
        }

        //-----------------------------------------------------------------------
        private void toCompressed(
            DataproviderObject_1_0 source
        ) throws ServiceException {
            this.path = new Path(source.path());
            int size = source.attributeNames().size();
            this.attributeNames = new String[size];
            this.attributeValues = new Object[size];
            int ii = 0;
            for(
                    Iterator i = source.attributeNames().iterator();
                    i.hasNext();
                    ii++
            ) {
                String attributeName = (String)i.next();
                List values = source.values(attributeName);
                this.attributeNames[ii] = attributeName;
                this.setAttributeValue(
                    ii,
                    values
                );
            }
        }

        //-----------------------------------------------------------------------
        public Path getPath(
        ) {
            return this.path;
        }

        //-----------------------------------------------------------------------
        public DataproviderObject getDataproviderObject(
        ) {
            DataproviderObject target = new DataproviderObject(
                new Path(this.path)
            );
            for(
                    int i = 0;
                    i < this.attributeNames.length;
                    i++
            ) {
                Object compressedValues = this.attributeValues[i];
                SparseList dataproviderValues = target.values(this.attributeNames[i]); 
                if(compressedValues instanceof List) {
                    dataproviderValues.addAll((List)compressedValues);
                } else if (compressedValues instanceof Lob) {
                    dataproviderValues.add(((Lob)compressedValues).getValue());
                } else if(compressedValues != null) {
                    dataproviderValues.add(compressedValues);
                }
            }
            return target;
        }

        //------------------------------------------------------------------------
        public void replace(
            DataproviderObject source
        ) throws ServiceException {
            DataproviderObject target = this.getDataproviderObject();
            target.attributeNames().removeAll(source.attributeNames());
            target.addClones(source,true);
            this.toCompressed(target);
        }

        //------------------------------------------------------------------------
        public void modify(
            DataproviderObject source
        ) throws ServiceException {
            DataproviderObject target = this.getDataproviderObject();
            for(
                    Iterator iterator = source.attributeNames().iterator();
                    iterator.hasNext();
            ) {
                String attributeName = (String)iterator.next();
                final SparseList sourceValues = source.values(attributeName);
                final SparseList targetValues = target.values(attributeName);
                for(
                        ListIterator population = sourceValues.populationIterator();
                        population.hasNext();
                ) targetValues.set(
                    population.nextIndex(),
                    population.next()
                );
            }
            this.toCompressed(target);
        }

        //------------------------------------------------------------------------
        // Implements Serializable
        //------------------------------------------------------------------------

        //------------------------------------------------------------------------
        private synchronized void writeObject(
            java.io.ObjectOutputStream stream
        ) throws java.io.IOException {
            stream.writeObject(this.path);
            stream.writeObject(this.attributeNames);
            for(
                    int i = 0; i < this.attributeNames.length;
                    i++
            ) {
                stream.writeObject(this.attributeValues[i]);
            }
        }

        //------------------------------------------------------------------------
        private synchronized void readObject(
            java.io.ObjectInputStream stream
        ) throws java.io.IOException, ClassNotFoundException {
            this.path = (Path)stream.readObject();
            this.attributeNames = (String[])stream.readObject();
            this.attributeValues = new Object[attributeNames.length];
            for(
                    int i = 0; 
                    i < attributeNames.length;
                    i++
            ) {
                this.attributeValues[i] = stream.readObject();
            }
        }

        //-----------------------------------------------------------------------
        // Variables
        //-----------------------------------------------------------------------
        private transient Path path;
        transient String[] attributeNames;
        private transient Object[] attributeValues;
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
    private Map getReference(
        Path path,
        boolean referenceMustExist
    ) throws ServiceException {
        if(path.size() % 2 == 0) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Path refers to a reference, not an object",
            new BasicException.Parameter("path",path)
        );
        final Map result = (Map)referenceMap.get(path.getParent());
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
    ) throws Exception, ServiceException {
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
                    this.storageFile = new File((String)storageFolder.get(0), namespaceId + ".db");
                    if(this.storageFile.exists()) {
                        try {
                            SysLog.detail("Load namespace", storageFile.getAbsolutePath());
                            ObjectInputStream stream = new ObjectInputStream(
                                new FileInputStream(this.storageFile)
                            );
                            InMemory_1.referenceMaps.put(
                                namespaceId,
                                this.referenceMap = this.readNestedMap(stream)
                            );
                        }
                        catch(Exception e) {
                            throw new ServiceException(e);
                        }
                    }
                    else {
                        SysLog.detail("Storage file not found. Create namespace", storageFile.getAbsolutePath());
                        InMemory_1.referenceMaps.put(
                            namespaceId,
                            this.referenceMap = new HashMap(INITIAL_REFERENCE_MAP_CAPACITY)
                        );
                    }
                }
            } else {
                SysLog.detail("Attach to namespace", namespaceId);
            }
        }
        if(isSequenceSupported()) synchronized(InMemory_1.referenceMaps){
            this.sequencesMap = (Map)InMemory_1.sequencesMaps.get(namespaceId);
            if (this.sequencesMap == null) {
                InMemory_1.sequencesMaps.put(
                    namespaceId,
                    this.sequencesMap = new HashMap(INITIAL_REFERENCE_MAP_CAPACITY)
                );
                for(
                        Iterator i = this.referenceMap.entrySet().iterator();
                        i.hasNext();                
                ){
                    Map.Entry referenceMapEntry = (Entry) i.next();
                    Path referencePath = (Path) referenceMapEntry.getKey();
                    if(referencePath.size() > 1) {
                        Sequences sequences = getSequences(referencePath.getParent());
                        String name = referencePath.getBase();
                        for(
                                Iterator j = ((Map) referenceMapEntry.getValue()).keySet().iterator();
                                j.hasNext();
                        ) sequences.update(name, (String)j.next());
                    }
                }
            }
        }
    }

    /**
     * Return an object's sequences
     * 
     * @param parent
     * 
     * @return an existing or newly created sequences object; 
     * never <clode>null</code>.
     */
    private Sequences getSequences (
        Path parent
    ){
        Sequences sequences = (Sequences) this.sequencesMap.get(parent);
        if(sequences == null) this.sequencesMap.put(
            parent,
            sequences = new Sequences()
        );
        return sequences;
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
        if(this.storageFile != null) synchronized(this.referenceMap){
            try {
                if(this.referenceMapIsDirty) {
                    SysLog.detail("Save namespace", storageFile.getAbsolutePath());
                    ObjectOutputStream stream = new ObjectOutputStream(
                        new FileOutputStream(storageFile)
                    );
                    this.writeNestedMap(
                        stream,
                        this.referenceMap
                    );
                    stream.close();
                }
            }
            catch(Exception e) {
                throw new ServiceException(e);
            }
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Serialization of Map must be implemented for .NET support
     */
    private void writeNestedMap(
        ObjectOutputStream stream,
        Map map
    ) throws IOException, ServiceException {
        stream.writeInt(map.size());
        int ii = 0;
        for(
                Iterator i = map.entrySet().iterator(); 
                i.hasNext(); 
        ) {
            Map.Entry entry = (Map.Entry)i.next();
            Object value = entry.getValue();
            stream.writeBoolean(value instanceof Map);
            if(value instanceof Map) {
                stream.writeObject(entry.getKey());
                this.writeNestedMap(
                    stream,
                    (Map)value
                );              
            }
            else {
                stream.writeObject(value);
                // reset is possible because DataproviderObjects
                // only contain primitive types. resetting the stream
                // improves the performance dramatically in case there
                // is a large number of objects to serialize
                if(ii % 8 == 0) {
                    stream.reset();
                } 
            }
            ii++;
        }
    }

    //-------------------------------------------------------------------------
    private Map readNestedMap(
        ObjectInputStream stream
    ) throws IOException, ClassNotFoundException, ServiceException {
        int size = stream.readInt();
        Map map = new HashMap(size);
        for(int i = 0; i < size; i++) {
            if(stream.readBoolean()) {
                Object key = stream.readObject();
                map.put(
                    key,
                    this.readNestedMap(
                        stream
                    )
                );
            }
            else {
                CompressedObject obj = (CompressedObject)stream.readObject();
                map.put(
                    obj.getPath().getBase(),
                    obj
                );
            }
        }
        return map;
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
    private DataproviderObject replyObject(
        CompressedObject source,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifier
    ) throws ServiceException {
        switch (attributeSelector) {

            case AttributeSelectors.NO_ATTRIBUTES:
                return new DataproviderObject(new Path(source.getPath()));

            case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
            case AttributeSelectors.ALL_ATTRIBUTES: {
                DataproviderObject target = source.getDataproviderObject();
                if(isSequenceSupported()){
                    Sequences sequences = (Sequences) this.sequencesMap.get(source.getPath());
                    (sequences == null ? EMPTY_SEQUENCES : sequences).toContext(target);
                }
                return target;
            }

            case AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES: {
                DataproviderObject target = source.getDataproviderObject();
                Set attributes = new HashSet();
                for(
                        int index = 0;
                        index < attributeSpecifier.length;
                        index++
                ) attributes.add(attributeSpecifier[index].name());
                attributes.add(SystemAttributes.OBJECT_CLASS);
                attributes.add(SystemAttributes.CREATED_AT);
                attributes.add(SystemAttributes.CREATED_BY);
                attributes.add(SystemAttributes.MODIFIED_AT);
                attributes.add(SystemAttributes.MODIFIED_BY);
                target.attributeNames().retainAll(attributes);
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
            Path path = request.object().path();
            CompressedObject source = (CompressedObject)getReference(
                path,
                true
            ).get(
                path.getBase()
            );
            if(source == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "Object \u00ab" + path + "\u00bb not found",
                new BasicException.Parameter("path",path)
            );
            return new DataproviderReply(
                this.replyObject(
                    source,
                    request.attributeSelector(),
                    request.attributeSpecifier()
                )
            );
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
        CompressedObject a,
        CompressedObject b,
        String attribute,
        int index
    ) {
        int aLarger = 1;
        int bLarger = -1;
        int equal = 0;
        int result = 0;

        // identity
        int aIndex = a.getAttributeIndex(attribute);
        int bIndex = b.getAttributeIndex(attribute);
        if(
                aIndex < 0 && bIndex < 0 && // test for stored attributes
                index == 0 && SystemAttributes.OBJECT_IDENTITY.equals(attribute)
        ) {
            return a.getPath().compareTo(b.getPath());
        }

        // a
        if(
                (aIndex < 0) || 
                (a.getAttributeValuesSize(aIndex) <= index)
        ) {
            result = aLarger;
        }

        // b
        if(
                (bIndex < 0) || 
                (b.getAttributeValuesSize(bIndex) <= index)
        ) {
            if(result == aLarger) {
                return equal;
            }
            return bLarger;
        }
        if(result == aLarger) {
            return aLarger;
        }

        if(a.getAttributeValue(aIndex, 0) instanceof Comparable){
            return ((Comparable)a.getAttributeValue(aIndex, index)).compareTo(
                b.getAttributeValue(bIndex, index)
            );
        }
        else {
            return -1; // not sorted
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
                        DataproviderObjectFilter filter;
                        try {
                            filter = new DataproviderObjectFilter(specification.getAttributeFilter());
                        }   catch (RuntimeException exception){
                            throw new ServiceException(
                                exception,
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                "Invalid attribute filter",
                                new BasicException.Parameter("attributeFilter", (Object[])specification.getAttributeFilter())
                            );
                        }    
                        List objects = new ArrayList();
                        List sortedObjects = new ArrayList();
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
                            Map unfilteredMap = (Map)referenceMap.get(
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
                                    Iterator objectIterator = unfiltered.iterator();
                                    objectIterator.hasNext();
                            ){
                                CompressedObject object = (CompressedObject)objectIterator.next();
                                if(filter.accept(object.getDataproviderObject())) {
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
                                    } else {
                                        // with sorters must first find all then get relevant part

                                        boolean added = false;
                                        // add sorted
                                        for (int pos = 0; pos < sortedObjects.size() && !added; pos++) {
                                            int diff = 0;
                                            for (int i=0; i<sorters.length && diff == 0 && !added; i++) {
                                                diff = this.compare((CompressedObject)sortedObjects.get(pos), object, sorters[i].name(), sorters[i].position());
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
                            for (Iterator o = sortedObjects.listIterator(replyPosition);
                            o.hasNext() && objects.size() < replySize;
                            ) {
                                objects.add(
                                    this.replyObject(
                                        (CompressedObject)o.next(),
                                        request.attributeSelector(),
                                        specification.getAttributeSpecifier()
                                    )
                                );
                            }
                        }

                        // reply
                        DataproviderReply reply = new DataproviderReply(objects);
                        if(request.operation() == DataproviderOperations.ITERATION_START){
                            reply.context(DataproviderReplyContexts.ITERATOR).set(
                                0,
                                AbstractIterator.serialize(specification)
                            );
                            reply.context(DataproviderReplyContexts.ATTRIBUTE_SELECTOR).set(
                                0,
                                new Short(request.attributeSelector())
                            );
                            reply.context(DataproviderReplyContexts.TOTAL).set(
                                0,
                                new Integer(position)
                            );
                        }
                        reply.context(DataproviderReplyContexts.HAS_MORE).set(
                            0,
                            new Boolean(request.position() + objects.size() < position)
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
            final Path path = request.path();
            Map reference = this.getReference(path, false);
            if(reference == null) referenceMap.put(
                path.getParent(),
                reference = new HashMap(INITIAL_REFERENCE_MAP_CAPACITY)
            ); // createReferencesOnDemand
            final String objectId = path.getBase();
            if(reference.containsKey(objectId)) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.DUPLICATE,
                "Object \u00ab" + path + "\u00bb already exists",
                new BasicException.Parameter("path",path)
            );
            if(isSequenceSupported() && path.size() > 2) {
                int split = path.size() - 2;
                getSequences(path.getPrefix(split)).update(path.get(split), objectId);
            }
            try {
                CompressedObject compressed = new CompressedObject(request.object());
                reference.put(
                    objectId,
                    compressed
                );
                this.referenceMapIsDirty = true;
                return new DataproviderReply(
                    this.replyObject(
                        compressed,
                        request.attributeSelector(),
                        request.attributeSpecifier()
                    )
                );
            } catch (Exception exception) {
                BasicException assertionFailure = new BasicException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ASSERTION_FAILURE,
                    "Compression/decompression failure"
                );
                SysLog.error(
                    assertionFailure.getMessage(), 
                    assertionFailure.getCause()
                );
                throw new Error(assertionFailure.getMessage());
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
            Map reference = getReference(
                path, 
                false
            );
            CompressedObject source = reference == null ? null :
                (CompressedObject)reference.remove(path.getBase());
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
            this.referenceMapIsDirty = true;
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
            boolean isNew = reference == null 
            ? true 
                : reference.get(path.getBase()) == null
                ? true
                    : false;
            this.referenceMapIsDirty = true;
            if(isNew) {    
                request.object().clearValues(
                    SystemAttributes.CREATED_AT
                ).addAll(
                    request.object().values(SystemAttributes.MODIFIED_AT)
                );
                return this.create(
                    header, 
                    request
                );
            }
            else {
                return this.replace(
                    header, 
                    request
                );
            }
        }
    }

    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        synchronized(this.referenceMap){
            final DataproviderObject source = request.object();
            final Path path = source.path();
            final CompressedObject target = (CompressedObject)getReference(
                path, 
                true
            ).get(
                path.getBase()
            );
            if(target == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "Object \u00ab" + path + "\u00bb not found",
                new BasicException.Parameter("path",path)
            );
            target.replace(source);
            this.referenceMapIsDirty = true;
            return new DataproviderReply(
                this.replyObject(
                    target,
                    request.attributeSelector(),
                    request.attributeSpecifier()
                )
            ); 
        }
    }


    /**
     * Modifes some of an object's attributes' values leaving the others
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
        synchronized(this.referenceMap){
            final DataproviderObject source = request.object();
            final Path path = source.path();
            final CompressedObject target = (CompressedObject)getReference(
                path, 
                true
            ).get(
                path.getBase()
            );
            if(target == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "Object \u00ab" + path + "\u00bb not found",
                new BasicException.Parameter("path",path)
            );
            target.modify(source);
            this.referenceMapIsDirty = true;
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
    // Variables
    //-------------------------------------------------------------------------
    private static final Sequences EMPTY_SEQUENCES = new Sequences(0);  
    private static int INITIAL_REFERENCE_MAP_CAPACITY = 213;
    private static final Map referenceMaps = new HashMap();
    private static final Map sequencesMaps = new HashMap();
    private Map referenceMap = null;
    private int batchSize = Integer.MAX_VALUE;
    private File storageFile = null;
    private boolean referenceMapIsDirty = false;
    private Map sequencesMap;

}

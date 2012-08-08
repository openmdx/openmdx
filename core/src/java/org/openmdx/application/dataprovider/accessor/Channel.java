/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Channel.java,v 1.5 2009/03/09 17:11:21 hburger Exp $
 * Description: Dataprovider Adapter: Provider
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/09 17:11:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.accessor;

import static org.openmdx.base.naming.SpecialResourceIdentifiers.EXTENT_REFERENCES;
import static org.openmdx.base.naming.SpecialResourceIdentifiers.EXTENT_OBJECTS;

import java.io.InputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;

import javax.resource.cci.MappedRecord;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObjectMarshaller_1_0;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.Directions;
import org.openmdx.application.dataprovider.cci.IterationProcessor;
import org.openmdx.application.dataprovider.cci.MarshallingRequestedList;
import org.openmdx.application.dataprovider.cci.Orders;
import org.openmdx.application.dataprovider.cci.PathOnlyMarshaller;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.RequestedObject;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.cci2.SparseArray;

//---------------------------------------------------------------------------
class Channel implements Synchronization_1_0, Serializable {

    @SuppressWarnings("unchecked")
    static class DataproviderObjectRecord implements MappedRecord {

        /**
         * 
         */
        private static final long serialVersionUID = 3257567312931731768L;


        /**
         * @param arg0
         */
        public DataproviderObjectRecord(DataproviderObject_1_0 source) {
            this.dataproviderObject = source;
            this.recordName = (String) source.values(SystemAttributes.OBJECT_CLASS).get(0);
            this.keys = source.attributeNames();
            this.keys.remove(SystemAttributes.OBJECT_CLASS);
            byte[] digest = source.getDigest();
            if(digest != null){
                source.clearValues(
                    SystemAttributes.OBJECT_LOCK_PREFIX + SystemAttributes.OBJECT_CLASS
                ).set(0, SystemAttributes.OPTIMISTIC_LOCK_CLASS);
                source.clearValues(
                    SystemAttributes.OBJECT_LOCK_PREFIX + SystemAttributes.OBJECT_DIGEST
                ).set(0, digest);
            }
        }


        /* (non-Javadoc)
         * @see javax.resource.cci.Record#getRecordName()
         */
        public String getRecordName() {
            return this.recordName;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Record#setRecordName(java.lang.String)
         */
        public void setRecordName(String recordName) {
            this.recordName = recordName;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
         */
        public void setRecordShortDescription(String recordShortDescription) {
            this.recordShortDescription = recordShortDescription;
        }

        /* (non-Javadoc)
         * @see javax.resource.cci.Record#getRecordShortDescription()
         */
        public String getRecordShortDescription() {
            return this.recordShortDescription;
        }


        /* (non-Javadoc)
         * @see java.util.Map#size()
         */
        public int size(
        ){
            return this.keys.size();
        }


        /* (non-Javadoc)
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty() {
            return this.keys.isEmpty();
        }


        /* (non-Javadoc)
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key) {
            return this.keys.contains(key);
        }


        /* (non-Javadoc)
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            return values().contains(value);
        }


        /* (non-Javadoc)
         * @see java.util.Map#get(java.lang.Object)
         */
        public Object get(Object key) {
            return key instanceof String ?
                this.dataproviderObject.getValues((String)key) :
                null;
        }


        /* (non-Javadoc)
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public Object put(Object key, Object value) {
            this.values = null;
            if(value instanceof Collection){
                this.dataproviderObject.clearValues((String) key).addAll((Collection) value);
            } else {
                this.dataproviderObject.clearValues((String) key).add(value);
            }
            return null;
        }


        /* (non-Javadoc)
         * @see java.util.Map#remove(java.lang.Object)
         */
        public Object remove(Object key) {
            this.values = null;
            Object value = key instanceof String ?
                this.dataproviderObject.getValues((String) key) :
                null;
            this.keys.remove(key);
            return value;
        }


        /* (non-Javadoc)
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(Map map) {
            for(
                Iterator i = map.entrySet().iterator();
                i.hasNext();
            ){
                Map.Entry e = (Entry) i.next();
                put(e.getKey(),e.getValue());
            }
        }


        /* (non-Javadoc)
         * @see java.util.Map#clear()
         */
        public void clear() {
            this.values = null;
            this.keys.clear();
        }


        /* (non-Javadoc)
         * @see java.util.Map#keySet()
         */
        public Set keySet() {
            return this.keys;
        }


        /* (non-Javadoc)
         * @see java.util.Map#values()
         */
        public Collection values() {
            if(this.values == null){
                this.values = new ArrayList();
                for(
                    Iterator i=this.keys.iterator();
                    i.hasNext();
                ) this.values.add(this.dataproviderObject.values((String) i.next()));
            }
            return this.values;
        }


        /* (non-Javadoc)
         * @see java.util.Map#entrySet()
         */
        public Set entrySet() {
            if(this.entries == null) this.entries = new EntrySet();
            return this.entries;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        public Object clone() throws CloneNotSupportedException {
            return new DataproviderObjectRecord(this.dataproviderObject);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return IndentingFormatter.toString(this);
        }


        //------------------------------------------------------------------------
        // Instance Members
        //------------------------------------------------------------------------

        /**
         * 
         */
        private String recordName = null;

        /**
         * 
         */
        private String recordShortDescription = null;
        
        /**
         * 
         */
        protected final DataproviderObject_1_0 dataproviderObject;

        /**
         * 
         */
        protected Set keys = null;

        /**
         * 
         */
        private transient Collection values = null;

        /**
         * 
         */
        private transient Set entries = null;


        //------------------------------------------------------------------------
        // Class EntrySet
        //------------------------------------------------------------------------

        class EntrySet extends AbstractSet {

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#iterator()
             */
            public Iterator iterator() {
                return new EntryIterator(keys.iterator());
            }

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#size()
             */
            public int size() {
                return keys.size();
            }   
            
        }


        //------------------------------------------------------------------------
        // Class EntryIterator
        //------------------------------------------------------------------------

        class EntryIterator implements Iterator {

            EntryIterator(
                Iterator keyIterator
            ){
                this.keyIterator = keyIterator;
            }
            
            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                return this.keyIterator.hasNext();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public Object next() {
                return new Entry((String)this.keyIterator.next());
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                this.keyIterator.remove();
            }   
            
            private final Iterator keyIterator;
            
        }


        //------------------------------------------------------------------------
        // Class Entry
        //------------------------------------------------------------------------

        class Entry implements Map.Entry {  
            
            Entry(
                String key
            ){
                this.key = key;
            }

            /* (non-Javadoc)
             * @see java.util.Map.Entry#getKey()
             */
            public Object getKey() {
                return this.key;
            }

            /* (non-Javadoc)
             * @see java.util.Map.Entry#getValue()
             */
            public Object getValue() {
                return dataproviderObject.values(key);
            }

            /* (non-Javadoc)
             * @see java.util.Map.Entry#setValue(java.lang.Object)
             */
            public Object setValue(Object value) {
                return put(this.key, value);
            }
                    
            private String key;

        }
        
    }

    static class RemoveResult extends RequestedObject {

    /**
     * 
     */
    private static final long serialVersionUID = 3256999951979459120L;

    /**
     * Constructor
     *
     * @param   path
     *          The path to be updated upon reply
     */
    RemoveResult(
        Path path,
        Connection_1 objectFactory
    ){
        this.path = path;
        this.objectFactory = objectFactory;
    }
    
    //------------------------------------------------------------------------
    // Extends RequestedObject
    //------------------------------------------------------------------------
        
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyListener#onException(org.openmdx.base.exception.ServiceException)
     */
    public void onException(ServiceException exception) {
        if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) try {
            this.objectFactory.invalidate(this.path, true);
        } catch (ServiceException invaidationException) {
            invaidationException.log();
        }
        super.onException(exception);
    }
            

    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * The path to be updated upon reply
     */     
    protected final Path path;

    /**
     * 
     * @author hburger
     *
     * To change the template for this generated type comment go to
     * Window>Preferences>Java>Code Generation>Code and Comments
     */
    protected final Connection_1 objectFactory;
    
}
    
    @SuppressWarnings("unchecked")
    static class Marshaller {

        private Marshaller(
        ){
            // Avoid instantiation
        }
        
      //------------------------------------------------------------------------
        /**
         * Retrieves a DataproviderObject_1_0's values as map.
         */
        static MappedRecord toMappedRecord(
            DataproviderObject_1_0 source
        )throws ServiceException{
            return new DataproviderObjectRecord(source);
        }
        
      static MappedRecord toOperationResult(
        DataproviderObject_1_0 source
      ){
        return new OperationResult(source);
      }

      //------------------------------------------------------------------------
        /**
         * Creates a DataproviderObject_1_0's based on the values in a map.
         */
        static DataproviderObject toDataproviderObject(
            Path path,
            MappedRecord values
        ){
            DataproviderObject result = new DataproviderObject(path);
            result.values(
                SystemAttributes.OBJECT_CLASS
            ).add(
                values.getRecordName()
            );
            for(
                Iterator i = values.entrySet().iterator();
                i.hasNext();
            ){
                Map.Entry entry = (Map.Entry)i.next();
                Object source = entry.getValue();
                String name = (String)entry.getKey();
                if(name.startsWith(SystemAttributes.OBJECT_LOCK_PREFIX)) {
                    if(name.endsWith(SystemAttributes.OBJECT_DIGEST)){
                        Object digest = source instanceof SparseList ? 
                            ((SparseList)source).get(0) :
                            source;
                        result.setDigest((byte[])digest);
                    }
                } else {
                    SparseList target = result.values(name);
                    if(source instanceof SortedMap){
                        for(
                            Iterator j = ((SortedMap)source).entrySet().iterator();
                            j.hasNext();
                        ){
                            Map.Entry k = (Entry)j.next();
                            target.set(
                                ((Integer)k.getKey()).intValue(),
                                k.getValue()
                            ); 
                        }
                    } else if(source instanceof SparseArray){
                        for(
                            ListIterator j = (
                                (SparseArray)source
                            ).populationIterator();
                            j.hasNext();
                        ) target.set(j.nextIndex(), j.next());
                    } else if(source instanceof Collection){
                        target.addAll((Collection)source);
                    } else {
                        target.set(0, source);
                    }   
                }
            }
            return result;    
        }
        
    }
    
    @SuppressWarnings("unchecked")    
    static class OperationResult extends AbstractMap implements MappedRecord {

    /**
     * 
     */
    private static final long serialVersionUID = 3258134665209590321L;

    OperationResult(
        DataproviderObject_1_0 source
    ){
        this.source = source;
    }

    public void setRecordName(
        String recordName
    ){
        source.clearValues(SystemAttributes.OBJECT_CLASS).set(
            0,
            recordName
        );
    }
    
    public String getRecordName(
    ){
        return (String)source.values(
            SystemAttributes.OBJECT_CLASS
        ).get(0);
    }
    
    public void setRecordShortDescription(
        String recordShortDescription
    ){
        this.recordShortDescription = recordShortDescription;
    }
    
    public String getRecordShortDescription(
    ){
        return this.recordShortDescription;
    }
    
    public Object clone(
    ){
        return new OperationResult(this.source);
    }

    public int size(
    ){
        return getMapping().size();
    }
    
    public Set entrySet(
    ){
        return getMapping().entrySet();
    }

    private Map getMapping(
    ){
        if(this.mapping == null){
            this.mapping = new HashMap();
            for(
                Iterator i = source.attributeNames().iterator();
                i.hasNext();
            ){
                String key = (String)i.next();
                if(
                    !SystemAttributes.OBJECT_CLASS.equals(key)
                )this.mapping.put(
                    key,
                    source.getValues(key)
                );
            }
        }
        return this.mapping;
    }
            
    private final DataproviderObject_1_0 source;
    
    private String recordShortDescription = null;

    private Map mapping = null;
        
}
    
    static class FindResult extends MarshallingRequestedList {

        /**
         * 
         */
        private static final long serialVersionUID = 3546920264634873912L;

        /**
         * Constructor
         * 
         * @param iterationProcessor
         * @param marshaller
         */
        FindResult(
            IterationProcessor iterationProcessor, 
            Path referenceFilter,
            DataproviderObjectMarshaller_1_0 marshaller,
            Connection_1 objectFactory
        ) {
            super(iterationProcessor, referenceFilter, marshaller);
            this.objectFactory = objectFactory;
        }

        //------------------------------------------------------------------------
        // Implements Reconstructable
        //------------------------------------------------------------------------

        /**
         * Constructor
         */ 
        FindResult(
            IterationProcessor iterationProcessor,
            Path referenceFilter,
            DataproviderObjectMarshaller_1_0 marshaller,
            Connection_1 connection,
            InputStream stream
        ) throws ServiceException {
            super(iterationProcessor,referenceFilter,marshaller,stream);
            this.objectFactory = connection;
        }


        //------------------------------------------------------------------------
        // Extends RequestedList
        //------------------------------------------------------------------------

        /**
         * Called if the work unit has been processed successfully
         */
        protected void onReplyInterceptor(
            DataproviderReply reply
        ){
            DataproviderObject_1_0[] fetched = reply.getObjects();
            if(
                this.objectFactory != null
            ) for(
                int i = 0;
                i < fetched.length;
                i++ 
            ) try {
                DataproviderObject_1_0 source = fetched[i];
                if(source.path().isLike(EXTENT_OBJECTS)) source.path().setTo(
                    new Path(source.path().getBase())
                );
                objectFactory.fetched(
                    source.path(), 
                    Marshaller.toMappedRecord(source)
                );
            } catch (ServiceException exception) {
                exception.log();
            }
            super.onReplyInterceptor(reply);
        }
        
            
        //------------------------------------------------------------------------
        // Instance members
        //------------------------------------------------------------------------

        /**
         * 
         */
        final protected Connection_1 objectFactory;

    }
    
    static class GetResult extends RequestedObject {

    /**
     * 
     */
    private static final long serialVersionUID = 3257285816412747060L;

    /**
     * Constructor
     *
     * @param   path
     *          The original access path
     */
    GetResult(
        Path accessPath,
        Connection_1 objectFactory
    ){
        this.accessPath = accessPath;
        this.objectFactory = objectFactory;
    }
    

    //------------------------------------------------------------------------
    // Extends RequestedObject
    //------------------------------------------------------------------------
        
    /**
     * Called if the work unit has been processed successfully
     */
    public void onReply(
        DataproviderReply reply
    ){
        DataproviderObject_1_0[] fetched = reply.getObjects();
        if(
            this.objectFactory != null
        ) for(
            int i = 1;
            i < fetched.length;
            i++ 
        ) try {
            objectFactory.fetched(
                fetched[i].path(), 
                Marshaller.toMappedRecord(fetched[i])
            );
        } catch (ServiceException exception) {
            exception.log();
        }
        super.onReply(reply);
    }
            
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyListener#onException(org.openmdx.base.exception.ServiceException)
     */
    public void onException(ServiceException exception) {
        if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) try {
            this.objectFactory.invalidate(this.accessPath, true);
        } catch (ServiceException invalidationException) {
            invalidationException.log();
        }
        super.onException(exception);
    }


    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * The path to be updated upon reply
     */     
    protected final Path accessPath;

    protected final Connection_1 objectFactory;
    
}
    
    static class CreateResult extends GetResult {

    /**
     * 
     */
    private static final long serialVersionUID = 3689346637176649527L;

    /**
     * Constructor
     *
     * @param   path
     *          The path to be updated upon reply
     */
    CreateResult(
        Path accessPath,
        Connection_1 objectFactory
    ){
        super(accessPath, objectFactory);
    }
    

    //------------------------------------------------------------------------
    // Extends RequestedObject
    //------------------------------------------------------------------------
        
    /**
     * Called if the work unit has been processed successfully
     */
    public void onReply(
        DataproviderReply reply
    ){
        super.onReply(reply);
        Path replyPath = path();
        if(!accessPath.equals(replyPath)){
            objectFactory.move(super.accessPath, replyPath);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyListener#onException(org.openmdx.base.exception.ServiceException)
     */
    public void onException(ServiceException exception) {
        if(exception.getExceptionCode() == BasicException.Code.DUPLICATE) try {
            this.objectFactory.invalidate(this.accessPath, false);
        } catch (ServiceException invalidationException) {
            invalidationException.log();
        }
        super.onException(exception);
    }

}
    
    /**
     * Constructs a new provider.
     * 
     * @param delegateChannel SPICE/2 RequestCollection which is used as interaction channel.
     * @param transactionPolicyIsNew true, if the Provider (and associated RequestCollection)
     *         has the transaction policy 'isNew'.
     */
    public Channel(
        RequestCollection delegateChannel,
        boolean transactionPolicyIsNew,
        boolean persistentNewObjectBecomeTransientUponRollback
    ) throws ServiceException {
        this.unitOfWorkChannel = delegateChannel;
        this.shareableChannel = (RequestCollection)delegateChannel.clone();
        this.transactionPolicyIsNew = transactionPolicyIsNew;
        this.persistentNewObjectsBecomeTransientUponRollback = persistentNewObjectBecomeTransientUponRollback;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3906645319247737140L;

    /**
     * 
     */
    private RequestCollection unitOfWorkChannel, shareableChannel;  

    /**
     * 
     */
    private final boolean transactionPolicyIsNew;

    /**
     * 
     */
    private final boolean persistentNewObjectsBecomeTransientUponRollback;

    /**
     * 
     */
    private boolean completing;

    /**
     * 
     */
    private Map<List<Object>,Map<Path,FindResult>> extents = 
        new HashMap<List<Object>,Map<Path,FindResult>>();

    private static final String[] FINAL_CLASSES = {
        "org:openmdx:base:Authority",
        "org:openmdx:base:Provider"
    };

    private RequestCollection getDelegateChannel(
    ){
        return this.completing ? 
            this.unitOfWorkChannel : 
            this.shareableChannel; 
    }

    //------------------------------------------------------------------------
    // Life cycle management
    //------------------------------------------------------------------------

    /**
     * Close the channel.
     * <p>
     * After the close method completes, all methods on the Provider_1_0
     * instance except isClosed throw a ILLEGAL_STATE RuntimeServiceException.
     */
    public void close(
    ){
        this.shareableChannel = this.unitOfWorkChannel = null;
    }

    //------------------------------------------------------------------------
    // Query
    //------------------------------------------------------------------------

    /**
     * Retrieve a list containing the identities of all objects for which the
     * <code>referenceFilter</code> as well as the <code>attributeFilter</code>
     * evaluate to true.
     *
     * @param       referenceFilter
     *              an object may be included into the result sets only if it
     *              is accessable through the path passed as
     *              <code>referenceFilter</code>
     * @param       attributeFilter
     *              an object may be included into the result sets only if all
     *              the filter properties evaluate to true if applied to it; 
     *              this argument may be <code>null</code>.
     * @param       attributeSpecifier
     *              An array of attribute specifiers
     * @param       objectFactory
     *              object factory for pre-fetch support
     * 
     * @return  a list of paths
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given reference filter is reachable.
     */
    public List<Object> find(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        AttributeSpecifier[] attributeSpecifier,
        Connection_1 objectFactory
    ) throws ServiceException {
        List<Object> keys = null;
        boolean useExtent = referenceFilter.isLike(EXTENT_REFERENCES);
        if(useExtent) { 
            if(
                attributeFilter == null || 
                attributeFilter.length == 0
            ) {
                return Collections.emptyList();
            }
            keys = getKey(attributeFilter, attributeSpecifier, 1);
        } else {
            keys = getKey(attributeFilter, attributeSpecifier, 0);
            Path identities = referenceFilter.getChild(":*");
            Map<Path,FindResult> mappings = keys == null ? null : this.extents.get(keys);
            if(mappings != null) {
                for (Map.Entry<Path,FindResult> e : mappings.entrySet()) {
                    if(identities.isLike(e.getKey())){
                        List<Object> result = new ArrayList<Object> ();
                        for(Object j : e.getValue()) {
                            Path candidate = (Path)j;
                            if(candidate.isLike(identities)) {
                                result.add(candidate);
                            }
                        }
                        SysLog.detail("Return cached paths", identities);
                        return result;
                    }
                }
            }
        }
        FindResult findResult = new FindResult(
            getDelegateChannel(), 
            referenceFilter,
            PathOnlyMarshaller.getInstance(),
            objectFactory
        );
        getDelegateChannel().addFindRequest(
            new Path(referenceFilter),
            attributeFilter,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            attributeSpecifier,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING,
            findResult          
        );
        if(useExtent){
            if(keys == null) return findResult;
            List<?> identities = (List<?>)keys.remove(0);
            Map<Path,FindResult> mappings = this.extents.get(keys);
            if(mappings == null) this.extents.put(
                keys,
                mappings = new HashMap<Path,FindResult>()
            );
            for(Object identity : identities) {
                mappings.put(
                    new Path(identity.toString()), 
                    findResult
                );
            }
        }
        return findResult;
    }

    /**
     * 
     * @param referenceFilter
     * @param attributeFilter
     * @param attributeSpecifier
     */
    private List<Object> getKey(
        FilterProperty[] attributeFilter,
        AttributeSpecifier[] attributeSpecifier,
        int identityCount 
    ){
        List<Object> key = new ArrayList<Object>();
        Set<FilterProperty> filterProperties = new HashSet<FilterProperty>();
        if(attributeFilter != null) for(
                int i = 0; 
                i < attributeFilter.length; 
                i++
        ){
            if(SystemAttributes.OBJECT_IDENTITY.equals(attributeFilter[i].name())){
                if(
                        attributeFilter[i].quantor() != Quantors.THERE_EXISTS ||
                        attributeFilter[i].operator() != FilterOperators.IS_LIKE 
                ) return null;
                key.add(attributeFilter[i].values());
            } else {
                filterProperties.add(attributeFilter[i]);
            } 
        }
        if(key.size() != identityCount) return null;
        key.add(filterProperties);
        if(attributeSpecifier != null) for(
                int i = 0; 
                i < attributeSpecifier.length;
                i++
        ) if (
                attributeSpecifier[i].order() != Orders.ANY
        ) key.add(attributeSpecifier[i]);
        return key;
    }

    /**
     * Reconstruct the list.
     *
     * @param   referenceFilter
     *          an object may be included into the result sets only if it
     *          is accessable through the path passed as
     *          <code>referenceFilter</code>
     * @param   criteria
     *          Criteria to be used to  reconstruct the list
     * @param   attributeSpecifier
     *          An array of attribute specifiers
     * 
     * @return  a list of paths
     *
     * @exception   ServiceException  NOT_SUPPORTED
     *              if no provider for the given reference filter is reachable.
     */
    public List<Object> reconstruct(
        Path referenceFilter,
        Connection_1 objectFactory,
        InputStream criteria
    ) throws ServiceException {
        return new FindResult(
            this.shareableChannel, 
            referenceFilter,
            PathOnlyMarshaller.getInstance(),
            objectFactory,
            criteria
        );
    }


    /**
     * Retrieve an object.
     * 
     * @param   accessPath
     *          the object's access path
     * @param   requiredSet
     * @param   objectFactory
     *          object factory for pre-fetch support
     *
     * @return  a map containing the object's default fetch group;
     *          or null if the object doesn't exist.
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    public MappedRecord getDefaultFetchGroup(
        Path accessPath,
        Set<String> requiredSet,
        Connection_1 objectFactory
    ) throws ServiceException {
        Path path = new Path(accessPath);
        int level = path.size() / 2;
        //
        // final classes
        //
        if(level < FINAL_CLASSES.length){
            DataproviderObject_1_0 object=new DataproviderObject(
                path
            );
            object.values(
                SystemAttributes.OBJECT_CLASS
            ).set(
                0,
                FINAL_CLASSES[level]
            );
            return Marshaller.toMappedRecord(
                object
            );
        } else {
            GetResult object=new GetResult(
                accessPath,
                objectFactory
            );
            AttributeSpecifier[] attributeSpecifiers = null;
            if(requiredSet != null) {
                attributeSpecifiers = new AttributeSpecifier[
                                                             requiredSet.size()
                                                             ];
                int j = 0;
                for(String attributeName : requiredSet) {
                    attributeSpecifiers[j++]=new AttributeSpecifier(attributeName);
                }
            }
            getDelegateChannel().addGetRequest(
                path,
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                attributeSpecifiers,
                object
            );
            return Marshaller.toMappedRecord(object);
        }
    }

    /**
     * Retrieve an attribute.
     *
     * @param   accessPath
     *          the object's identity
     * @param   name
     *          the attribute name
     * @param   target
     *          where the atttibute should be stored
     * @param   objectFactory
     *          object factory to deliver pre-fetched objects
     * 
     * @return  the specified feature
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    @SuppressWarnings("unchecked")
    public void getAttribute(
        Path accessPath,
        String name,
        Map target,
        Connection_1 objectFactory
    ) throws ServiceException {
        Path path = new Path(accessPath);
        GetResult source = new GetResult(
            accessPath,
            objectFactory
        );
        getDelegateChannel().addGetRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            new AttributeSpecifier[]{
                new AttributeSpecifier(name)
            },
            source
        );
        target.put(
            name,
            source.getValues(name)
        );
        for(String attributeName : source.attributeNames()) {
            if(
                !target.containsKey(attributeName) &&
                !SystemAttributes.OBJECT_CLASS.equals(attributeName)
            ) {
                target.put(
                    attributeName, 
                    source.getValues(attributeName) 
                );
            }
        }
    }

    /**
     * Invokes an operation on the specified object.
     *
     * @param   accessPath
     *          the object's 
     * @param   name
     *          the operation name
     * @param   arguments
     *          the operation arguments
     *
     * @return  the operation's return values
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    public MappedRecord invokeOperation(
        Path accessPath,
        String name,
        MappedRecord arguments
    ) throws ServiceException {
        return Marshaller.toOperationResult(
            getDelegateChannel().addOperationRequest(
                Marshaller.toDataproviderObject(
                    accessPath.getChild(name),
                    arguments
                )
            )
        );
    }

    //------------------------------------------------------------------------
    // Unit of work
    //------------------------------------------------------------------------

    /**
     * Create an object.
     *
     * @param accessPath
     *        the  of the object
     * @param attributes
     *        the object's attributes
     * @param   objectFactory
     *          object factory for pre-fetch support
     */
    public void createObject(
        Path accessPath,
        MappedRecord attributes,
        Connection_1 objectFactory
    ) throws ServiceException {
        Path path = new Path(accessPath);
        CreateResult target = new CreateResult(
            accessPath,
            objectFactory
        );
        this.unitOfWorkChannel.addCreateRequest(
            Marshaller.toDataproviderObject(
                path, 
                attributes
            ),
            AttributeSelectors.NO_ATTRIBUTES, //.... Copy back
            null,
            target
        );
    }  

    /**
     * Modify an object.
     *
     * @param accessPath
     *        the  of the object
     * @param attributes
     *        the attributes to be replaced
     * @param   objectFactory
     *          object factory for pre-fetch support
     */
    public void editObject(
        Path accessPath,
        MappedRecord attributes,
        Connection_1 objectFactory
    ) throws ServiceException {
        Path path = new Path(accessPath);
        GetResult target = new GetResult(
            accessPath,
            objectFactory
        );
        this.unitOfWorkChannel.addReplaceRequest(
            Marshaller.toDataproviderObject(
                path, 
                attributes
            ),
            AttributeSelectors.NO_ATTRIBUTES, //.... Copy back
            null,
            target
        );
    }  

    /**
     * Remove an object.
     *
     * @param accessPath
     *        the identity of the object
     * @param   objectFactory
     *          object factory to invalidate not found objects
     */
    public void removeObject(
        Path accessPath,
        Connection_1 objectFactory
    ) throws ServiceException {
        Path path = new Path(accessPath);
        RemoveResult target = new RemoveResult(
            path,
            objectFactory
        );
        this.unitOfWorkChannel.addRemoveRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null,
            target
        );
    }  

    //------------------------------------------------------------------------
    // Implements Provider_1_1
    //------------------------------------------------------------------------

    /**
     * Tells whether persistent-new objects become transient.
     * on rollback.
     * 
     * @return <code>true<code> if persistent-new objects become transient
     * on rollback.
     */
    public boolean doPeristentNewObjectsBecomeTransientUponRollback(
    ) {
        return this.persistentNewObjectsBecomeTransientUponRollback;
    }

    
    //------------------------------------------------------------------------
    // Implements Provider_1_3
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_3#getPrincipalChain()
     */
    public List<String> getPrincipalChain(
    ) {
        return this.unitOfWorkChannel.getPrincipalChain();
    }


    //------------------------------------------------------------------------
    // Implements Synchronization_1_0
    //------------------------------------------------------------------------

    /**
     * The afterBegin method notifies a provider or unit-of-work that a new
     * unit of work has started, and that the subsequent business methods on
     * the instance will be invoked in the context of the transaction. 
     */
    public void afterBegin(
    ) throws ServiceException {
        this.unitOfWorkChannel.clear();
        this.extents.clear();
    } 

    /**
     * The beforeCompletion method notifies a provider or plug-in that a
     * unit of work is about to be committed 
     */
    public void beforeCompletion(
    ) {
        try {
            this.unitOfWorkChannel.beginUnitOfWork(this.transactionPolicyIsNew);
            this.completing = true;
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    } 

    public void afterCompletion(
        int status
    ) {
        if(status == javax.transaction.Status.STATUS_COMMITTED){
            try {
                this.unitOfWorkChannel.endUnitOfWork();
            } 
            catch (Exception exception) {
                throw transactionPolicyIsNew ? 
                    new RuntimeServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ROLLBACK,
                        "Transactional unit of work rolled back") : 
                    new RuntimeServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ABORT,
                        "Non-transactional or container-managed unit of work aborted"
                    );
            } 
            finally {
                this.completing = false;
            }
        } 
        else {
            this.unitOfWorkChannel.clear();
            this.completing = false;
        }
    }

}

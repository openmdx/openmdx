/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Managed Aspect
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.accessor.rest;

import java.io.Flushable;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.TransactionalSegment;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.kernel.log.SysLog;

import java.util.function.Consumer; 

/**
 * Managed Aspect
 */
final class ManagedAspect
	extends AbstractPersistenceCapableCollection
    implements Container_1_0, Flushable
{

	private final DataObject_1 owner;

	ManagedAspect(
        DataObject_1 dataObject_1, 
        String aspectClass
    ){
        this.owner = dataObject_1;
		this.aspectClass = aspectClass;
        this.cachedAspect = this.owner.isTransientOrNew() || this.owner.objIsInaccessible() ? new HashMap<String, DataObject_1_0>() : null;
    }

    private final String aspectClass;        
    private Map<String,DataObject_1_0> transientAspect;
    
    private transient Container_1_0 delegate;
    private transient Map<String,DataObject_1_0> cachedAspect;
    private transient Set<Map.Entry<String,DataObject_1_0>> entries;
    private transient Collection<DataObject_1_0> values;
    private transient Set<String> keys;
    
    protected boolean isCache(
        Map<String,DataObject_1_0> aspect
    ){
        return this.cachedAspect == aspect;
    }
    
    /**
     * Retrieve the transient or standard delegate.
     * 
     * @param toRead
     * @param cacheRetrieved cache the values if the delegate has been retrieved
     * 
     * @return the transient or standard delegate
     */
    Map<String,DataObject_1_0> getDelegate(
        boolean toRead, 
        boolean cacheRetrieved            
    ){
        if(toRead && this.cachedAspect != null) {
            return this.cachedAspect;
        }
    	try {
			Container_1_0 aspects = this.owner.objIsContained() ? this.owner.getAspects() : null;
			if(aspects != null) {
				if(this.delegate == null || aspects.container() != this.delegate.container()) {
					this.delegate = aspects.subMap(
				        new Filter(
				            new IsInstanceOfCondition(this.aspectClass)
				        )
				    );
				}
				if(cacheRetrieved && this.delegate.isRetrieved()) {
				    return this.cachedAspect = new HashMap<String, DataObject_1_0>(this.delegate);
				} else {
        			return this.delegate;
				}
			} else {
	        	if(this.transientAspect == null) {
	    			this.transientAspect = new HashMap<String, DataObject_1_0>();
	        	}				
	        	return this.transientAspect;		        	
			}
		} catch (ServiceException e) {
			throw new RuntimeServiceException(e);
		}
    }

    void move(){
        final Map<String, DataObject_1_0> transientAspect = this.transientAspect;
		if(transientAspect != null) {
			this.transientAspect = null;
			putAll(transientAspect);
			if(this.cachedAspect == null) {
			    this.cachedAspect = transientAspect;				    
			}
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.AbstractMap#clear()
     */
    @Override
    public void clear() {
        this.getDelegate(false, false).clear();
        if(this.cachedAspect == null){
            this.cachedAspect = new HashMap<String, DataObject_1_0>();
        } else {
            this.cachedAspect.clear();
        }
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(
        Object key
    ) {
        return this.getDelegate(true, false).containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(
        Object value
    ) {
        return this.getDelegate(true, false).containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet(
    ) {
        if(this.entries == null) {
            this.entries = new Entries();
        }
        return this.entries;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#equals(java.lang.Object)
     */
    @Override
    public boolean equals(
        Object that
    ) {
        return this == that;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#get(java.lang.Object)
     */
    @Override
    public DataObject_1_0 get(
        Object key
    ) {
        return this.getDelegate(true, false).get(key);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#hashCode()
     */
    @Override
    public int hashCode(
    ) {
        return System.identityHashCode(this);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#isEmpty()
     */
    @Override
    public boolean isEmpty(
    ) {
        return this.getDelegate(true, false).isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#keySet()
     */
    @Override
    public Set<String> keySet(
    ) {
        if(this.keys == null) {
            this.keys = new Keys();
        }
        return this.keys;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public DataObject_1_0 put(
        String key, 
        DataObject_1_0 value
    ) {
        try {
            if(this.owner.jdoIsPersistent()) {
                this.owner.objMakeTransactional();
            }
            //
            // Move the aspect's containers to the core object
            //
            for(
                Iterator<Map.Entry<String,Flushable>> i = ((DataObject_1)value).flushableValues.entrySet().iterator();
                i.hasNext();
            ){
                Map.Entry<String,Flushable> flushableEntry = i.next();
                Flushable flushable = flushableEntry.getValue();
                if(flushable instanceof Container_1_0) {
                    Container_1_0 target = this.owner.objGetContainer(flushableEntry.getKey());
                    Container_1_0 source = (Container_1_0) flushable;
                    for(Map.Entry<String, DataObject_1_0> objectEntry : source.entrySet()) {
                        objectEntry.getValue().objMove(target, objectEntry.getKey());
                    }
                    i.remove();
                }
            }
            //
            // Save the aspect
            //
            final boolean qualifiedQualifier = this.owner.getAspects() != null && this.owner.isQualified();
            final String qualifier = qualifiedQualifier ? this.toObjectId(this.owner.getQualifier(), key) : key;
            if(this.cachedAspect != null) {
                if(qualifiedQualifier) {
                    //
                    // TODO BEFORE JRE 8
                    final DataObject_1_0 actualValue = this.cachedAspect.remove(key);
                    if(actualValue != null && actualValue != value) {
                        this.cachedAspect.put(key, actualValue); // undo
                        SysLog.log(
                            Level.WARNING,
                            "Sys|Unexpected aspect cache content when applying the object qualifier|" +
                            "The unqalified qualifier '{0}' and the qualified qualifier '{1}' " +
                            "refer to different objects '{2}' and '{3}', respectively",
                            key, qualifier, actualValue, value
                        );
                    }
                    // END BEFORE JRE 8
                    //
                    // TODO SINCE JRE 8
                    // this.cachedAspect.remove(key, value);
                    // END SINCE JRE 8
                }
                this.cachedAspect.put(qualifier, value);
            }
            return this.getDelegate(false, false).put(qualifier, value);
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#remove(java.lang.Object)
     */
    @Override
    public DataObject_1_0 remove(
        Object key
    ) {
        if(this.cachedAspect != null) {
            this.cachedAspect.remove(key);
        }
        return this.getDelegate(false, false).remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#size()
     */
    @Override
    public int size(
    ) {
        return this.getDelegate(true, true).size();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#values()
     */
    @Override
    public Collection<DataObject_1_0> values(
    ) {
        if(this.values == null) {
            this.values = new Values();
        }
        return this.values;
    }

    /* (non-Javadoc)
     * @see java.io.Flushable#flush()
     */
    @Override
    public void flush(
    ) throws IOException {
        // Nothing to do at the moment
    }
    
    private String toObjectId(
        String coreId,
        String aspectId
    ){
        return
            aspectId.startsWith(":") ? (":" + coreId + aspectId) :
            aspectId.startsWith("!") ? (coreId + aspectId) :
            (coreId + '*' + aspectId);    
    }
    
    void evict() {
    	this.delegate = null;
    	if(!this.owner.isTransientOrNew()) {
    		this.cachedAspect = null;
    	}
    }

    /* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends DataObject_1_0> t) {
		for(java.util.Map.Entry<? extends String, ? extends DataObject_1_0> e : t.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}
	
	@Override
	public PersistenceManager openmdxjdoGetDataObjectManager() {
		return this.owner.jdoGetPersistenceManager();
	}
	
	@Override
	public void openmdxjdoEvict(boolean allMembers, boolean allSubSets) {
        this.owner.aspects().openmdxjdoEvict(allMembers, allSubSets);	
	}
	
	@Override
	public void openmdxjdoRefresh() {
		this.owner.aspects().openmdxjdoRefresh();
	}
	
	@Override
	public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
		this.owner.aspects().openmdxjdoRetrieve(fetchPlan);			
	}
	
	@Override
	public PersistenceManager jdoGetPersistenceManager() {
		return this.owner.jdoGetPersistenceManager();
	}
	
	@Override
	public Object jdoGetObjectId() {
        throw new UnsupportedOperationException(
            "Query XRIs not yet supported"
        );		
	}
	
	@Override
	public Object jdoGetTransactionalObjectId() {
        throw new UnsupportedOperationException(
            "Query XRIs not yet supported"      
        );		                                
	}
	
	@Override
	public boolean jdoIsPersistent() {
		return this.owner.jdoIsPersistent();
	}
	
	@Override
	public Container_1_0 container() {
		return this.owner.aspects().container();
	}
	
	/**
	 * Doesn't use getDelegate(boolean, boolean) yet
	 * 
	 * @see #getDelegate(boolean, boolean)
	 */
	@Override
	public Container_1_0 subMap(QueryFilterRecord filter) {
		return this.owner.aspects().subMap(filter);
	}
	
	@Override
	public List<DataObject_1_0> values(FetchPlan fetchPlan, FeatureOrderRecord... criteria) {
		return fetchPlan == null ?
			new OrderedValues(DataObjectComparator.getInstance(criteria)) :
			this.owner.aspects().values(fetchPlan, criteria);
	}

    @Override
    public void processAll(
        FetchPlan fetchPlan,
        FeatureOrderRecord[] criteria,
        Consumer<DataObject_1_0> consumer
   ) {
        for(DataObject_1_0 value : values(fetchPlan, criteria)) {
            consumer.accept(value);
        } 
    }

    @Override
	public boolean isRetrieved() {
		return this.owner.aspects().isRetrieved();
	}
	
	Iterator<Map.Entry<String, DataObject_1_0>> entryIterator(){
		return new EntryIterator(getDelegate(true, true));
	}	
	
	/**
	 * Ordered Values
	 */
	class OrderedValues extends AbstractSequentialList<DataObject_1_0> {
		
		OrderedValues(Comparator<DataObject_1_0> comparator) {
			this.comparator = comparator;
		}

		private final Comparator<DataObject_1_0> comparator;
		
		@Override
		public int size() {
			return values().size();
		}

		@Override
		public ListIterator<DataObject_1_0> listIterator(int index) {
			final List<DataObject_1_0> values = new ArrayList<DataObject_1_0>();
			for(DataObject_1_0 value : values()) {
				values.add(value);
			}
			if(comparator != null) {
				Collections.sort(values, comparator);
			}
			return values.listIterator(index);
		} 
		
	}
	
	/**
	 * Entry Iterator
	 */
	class EntryIterator implements Iterator<Map.Entry<String, DataObject_1_0>> {

		EntryIterator(
			final Map<String, DataObject_1_0> aspect
		){
			this.aspect = aspect;
			this.delegate = aspect.entrySet().iterator();
		}
		
	    private final Map<String, DataObject_1_0> aspect;
	    private final Iterator<Map.Entry<String, DataObject_1_0>> delegate;
	    private Map.Entry<String, DataObject_1_0> next;
	    private String current;
	    
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
        	while(this.next == null && delegate.hasNext()) {
        		final java.util.Map.Entry<String, DataObject_1_0> candidate = delegate.next();
        		final DataObject_1_0 value = candidate.getValue();
				if(value.objIsInaccessible() || value.jdoIsDeleted()) {
                    if(isCache(this.aspect)) {
                    	delegate.remove();
                    }
        		} else {
        			this.next = candidate;
        		}
        	}
            return this.next != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public java.util.Map.Entry<String, DataObject_1_0> next() {
        	if(hasNext()) {
                final java.util.Map.Entry<String, DataObject_1_0> entry = next;
                this.next = null;
                this.current = entry.getKey();
                return entry;
        	} else {
        		throw new NoSuchElementException();
        	}
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            if(isCache(this.aspect)) {
                getDelegate(false, false).remove(this.current);
            }
            delegate.remove();
        }
	    
	}

	/**
	 * Value Iterator
	 */
    static class ValueIterator implements Iterator<DataObject_1_0> {

    	ValueIterator(
			final Iterator<Map.Entry<String, DataObject_1_0>> delegate
    	){
    		this.delegate = delegate;
    	}
    	
        final Iterator<Map.Entry<String, DataObject_1_0>> delegate;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public DataObject_1_0 next() {
            return delegate.next().getValue();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            delegate.remove();
        }
        
    }
	
    /**
     * Key Iterator
     */
    static class KeyIterator implements Iterator<String> {

    	KeyIterator(
			Iterator<Map.Entry<String, DataObject_1_0>> delegate
    	){
    		this.delegate = delegate;
    	}
    	
        final Iterator<Map.Entry<String, DataObject_1_0>> delegate;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        @Override
        public String next() {
            return delegate.next().getKey();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            delegate.remove();
        }
        
    }
    
    /**
     * Values
     */
    class Values extends AbstractCollection<DataObject_1_0> {

        /**
         * @param value
         * 
         * @return <code>true</code> if the collection had to be modified
         * 
         * @see java.util.Collection#add(java.lang.Object)
         */
    	@Override
        public boolean add(
            DataObject_1_0 value
        ) {
            boolean modify = !ManagedAspect.this.containsValue(value);
            if(modify) {
                String key = TransactionalSegment.getClassicRepresentationOfNewInstance();
                ManagedAspect.this.put(key, value);
            }
            return modify;
        }

        /**
         * 
         * @see java.util.Collection#clear()
         */
    	@Override
        public void clear() {
            ManagedAspect.this.clear();
        }

        /**
         * @param o
         * @return
         * @see java.util.Collection#contains(java.lang.Object)
         */
    	@Override
        public boolean contains(Object o) {
            return ManagedAspect.this.containsValue(o);
        }

        /**
         * @return
         * @see java.util.Collection#isEmpty()
         */
    	@Override
        public boolean isEmpty() {
            return ManagedAspect.this.isEmpty();
        }

        /**
         * @return
         * @see java.util.Collection#iterator()
         */
    	@Override
        public Iterator<DataObject_1_0> iterator() {
            return new ValueIterator(entryIterator());
        }

        /**
         * @return
         * @see java.util.Collection#size()
         */
    	@Override
        public int size() {
            return ManagedAspect.this.size();
        }

    }

    /**
     * Keys
     */
    class Keys extends AbstractSet<String> {

        /**
         * 
         * @see java.util.Collection#clear()
         */
    	@Override
        public void clear() {
            ManagedAspect.this.clear();
        }

        /**
         * @param o
         * @return
         * @see java.util.Collection#contains(java.lang.Object)
         */
    	@Override
        public boolean contains(Object o) {
            return ManagedAspect.this.containsKey(o);
        }

        /**
         * @return
         * @see java.util.Collection#isEmpty()
         */
    	@Override
        public boolean isEmpty() {
            return ManagedAspect.this.isEmpty();
        }

        /**
         * @return
         * @see java.util.Collection#iterator()
         */
    	@Override
        public Iterator<String> iterator() {
            return new KeyIterator(entryIterator());
        }

        /**
         * @return
         * @see java.util.Collection#size()
         */
    	@Override
        public int size() {
            return ManagedAspect.this.size();
        }

    }

    /**
     * Entries
     */
    class Entries extends AbstractSet<Map.Entry<String,DataObject_1_0>> {

        /**
         * 
         * @see java.util.Collection#clear()
         */
    	@Override
        public void clear() {
            ManagedAspect.this.clear();
        }

        /**
         * @return
         * @see java.util.Collection#isEmpty()
         */
    	@Override
        public boolean isEmpty() {
            return ManagedAspect.this.isEmpty();
        }

        /**
         * @return
         * @see java.util.Collection#iterator()
         */
    	@Override
        public Iterator<Map.Entry<String,DataObject_1_0>> iterator() {
            return entryIterator();
        }

        /**
         * @return
         * @see java.util.Collection#size()
         */
    	@Override
       public int size() {
            return ManagedAspect.this.size();
        }

    }
            
}

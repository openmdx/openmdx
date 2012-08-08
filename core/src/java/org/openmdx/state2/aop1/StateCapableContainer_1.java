/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateCapableContainer_1.java,v 1.18 2010/12/18 18:42:06 hburger Exp $
 * Description: State Object Container
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/12/18 18:42:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2010, OMEX AG, Switzerland
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
package org.openmdx.state2.aop1;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.resource.cci.InteractionSpec;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.ViewKind;
import org.openmdx.state2.spi.Configuration;

/**
 * State Object Container
 */
public class StateCapableContainer_1 
    implements Serializable, Container_1_0, Delegating_1_0<Container_1_0> 
{

    /**
     * Constructor 
     *
     * @param parent
     * @param container
     */
    protected StateCapableContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container 
    ) throws ServiceException {
        this(
            parent,
            container,
            StateCapableContainer_1.getFilter(parent, PersistenceHelper.getContainerId(container))
        );
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param selection
     * @param criteria
     */
    private StateCapableContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container,
        Filter criteria
    ){
        this.parent = parent;
        this.container = container;
        this.selection = container.subMap(criteria);
    }
           
    protected final ObjectView_1_0 parent;
 
    protected final Container_1_0 container;
    
    protected final Container_1_0 selection;
    
    private transient Set<Map.Entry<String, DataObject_1_0>> entries = null;
    
    private transient Collection<DataObject_1_0> values = null;

    private transient Set<String> keys = null;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 1312568818384216689L;

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Container_1_0#superSet()
     */
//  @Override
    public Container_1_0 container(
    ) {
        return this.container.container();
    }     

//  @Override
    public Path openmdxjdoGetContainerId() {
        return this.container.openmdxjdoGetContainerId();
    }

//  @Override
    public TransientContainerId openmdxjdoGetTransientContainerId() {
        return this.container.openmdxjdoGetTransientContainerId();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
//  @Override
    public PersistenceManager openmdxjdoGetPersistenceManager(){
    	return this.parent.jdoGetPersistenceManager();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
//  @Override
    public PersistenceManager openmdxjdoGetDataObjectManager() {
        return this.container.openmdxjdoGetDataObjectManager();
    }

//  @Override
    public boolean openmdxjdoIsPersistent() {
        return this.container.openmdxjdoIsPersistent();
    }

//  @Override
    public final Container_1_0 objGetDelegate(){
        return this.selection;
    }
    
    /**
     * Derive the filter from the state context
     * 
     * @param interactionSpec
     * 
     * @return the corresponding filter
     * @throws ServiceException 
     */
    protected static Filter getFilter(
        ObjectView_1_0 parent,
        Path containerId
    ) throws ServiceException{
        InteractionSpec interactionSpec = parent.getInteractionSpec();
        if(interactionSpec instanceof DateStateContext) {
            DateStateContext stateContext = (DateStateContext) interactionSpec;
        	List<Condition> filter = new ArrayList<Condition>();
        	filter.add(
	            new IsInstanceOfCondition(
	                "org:openmdx:state2:DateState"
	            )
	        );
        	if(
        	    containerId == null ||
        	    !SharedObjects.getPlugInObject(parent.jdoGetPersistenceManager(), Configuration.class).isValidTimeUnique(containerId)
        	){
                switch(stateContext.getViewKind()) {
                    case TIME_RANGE_VIEW:
                    	XMLGregorianCalendar validTo = stateContext.getValidTo();
                    	if(validTo != null) {
                    		filter.add(
                                new IsGreaterCondition(
                                    Quantifier.FOR_ALL,
                                    "stateValidFrom", // LESS_THAN_OR_EQUAL
                                    false,
                                    validTo
                                )
                    		);
                    	}
                    	XMLGregorianCalendar validFrom = stateContext.getValidFrom();
                    	if(validFrom != null) {
                    		filter.add(
                                new IsGreaterOrEqualCondition(
                                    Quantifier.FOR_ALL,
                                    "stateValidTo",
                                    true,
                                    validFrom
                                )
                    		);
                    	}
                		filter.add(
                            new IsInCondition(
                                Quantifier.FOR_ALL,
                                SystemAttributes.REMOVED_AT,
                                true
                            )
                        );
                		break;
                    case TIME_POINT_VIEW:
                    	XMLGregorianCalendar validAt = stateContext.getValidAt();
                    	if(validAt != null) {
                    		filter.add(
                                new IsGreaterCondition(
                                    Quantifier.FOR_ALL,
                                    "stateValidFrom",
                                    false, // IS_LESS_OR_EQUAL
                                    validAt
                                )
                            );
                    		filter.add(
                                new IsGreaterOrEqualCondition(
                                    Quantifier.FOR_ALL,
                                    "stateValidTo",
                                    true, // IS_GREATER_OR_EQUAL
                                    validAt
                                )
                            );
                    	}
                    	Date existsAt = stateContext.getExistsAt();
                    	if(existsAt == null) {
                    		filter.add(
                                new IsInCondition(
                                    Quantifier.FOR_ALL,
                                    SystemAttributes.REMOVED_AT,
                                    true // IS_IN
                                )
    						);
                    	} else {
                    		filter.add(
                                new IsGreaterCondition(
                                    Quantifier.FOR_ALL,
                                    SystemAttributes.CREATED_AT,
                                    false, // IS_LESS_OR_EQUAL,
                                    existsAt
                                )
                    		);
                    		filter.add(
                                new IsGreaterOrEqualCondition(
                                    Quantifier.FOR_ALL,
                                    SystemAttributes.REMOVED_AT,
                                    true, // IS_GREATER_OR_EQUAL
                                    existsAt
                                )
                			);
                    	}
                    	break;
                }
        	}
            return new Filter(filter, null, null);
        } else {
	        return null;
        }
    }
        
    private static boolean isInstanceOfCondition(
        Condition condition
    ){
        if(condition instanceof IsInstanceOfCondition) {
            return true;
        } else {
            String name = condition.getName();
            return SystemAttributes.OBJECT_CLASS.equals(name) || SystemAttributes.OBJECT_INSTANCE_OF.equals(name);
        }
    }
    
    private static boolean isStateFilter(
        Condition condition
    ){
        Model_1_0 model = Model_1Factory.getModel();
        for(Object value : condition.getValue()) {
            try {
                if(model.isSubtypeOf(value, "org:openmdx:state2:BasicState")) {
                    return true;
                }
            } catch (ServiceException exception) {
                exception.log();
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
     */
//  @Override
    public Container_1_0 subMap(Filter filter) {
        if(this.isInTimeRangeView()){
            Conditions: for(Condition condition : filter.getCondition()) {
                if(isInstanceOfCondition(condition)) {
                    if(isStateFilter(condition)) {
                        throw new UnsupportedOperationException(
                            "Inappropriate context for state queries: " +
                            this.parent.getInteractionSpec()
                        );
                    }
                    break Conditions;
                }
            }
        }
        return filter == null ? this : new StateCapableContainer_1(
            this.parent,
            this.selection,
            filter
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
     */
//  @Override
    public List<DataObject_1_0> values(
        FetchPlan fetchPlan, OrderSpecifier... criteria
    ) {
        return this.selection.values(fetchPlan, criteria);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
//  @Override
    public void clear() {
        this.entrySet().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
//  @Override
    public boolean containsKey(Object key) {
        return this.keySet().contains(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
//  @Override
    public boolean containsValue(
        Object value
    ) {
        return this.values().contains(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
//  @Override
    public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet() {
        if(this.entries == null) {
            this.entries = new AbstractSet<Map.Entry<String,DataObject_1_0>>()  {

                @Override
                public Iterator<Map.Entry<String,DataObject_1_0>> iterator() {
                    return new EntryIterator();
                }

                @Override
                public int size() {
                    return StateCapableContainer_1.this.size();
                }

                /* (non-Javadoc)
                 * @see java.util.AbstractCollection#isEmpty()
                 */
                @Override
                public boolean isEmpty() {
                    return StateCapableContainer_1.this.isEmpty();
                }
                
            };
        }    
        return this.entries;
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
//  @Override
    public DataObject_1_0 get(Object key) {
        return this.container.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
//  @Override
    public boolean isEmpty() {
        return this.selection.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
//  @Override
    public Set<String> keySet() {
        return this.keys == null ? 
            this.keys = new Keys(this.selection.keySet()) :
            this.keys;
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
//  @Override
    public DataObject_1_0 put(String key, DataObject_1_0 value) {
        return this.selection.put(key, value);            
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
//  @Override
    public void putAll(Map<? extends String, ? extends DataObject_1_0> t) {
        throw new UnsupportedOperationException();
    }

    /**
     * Tells whether the container is in a time range view
     * 
     * @return <code>true</code> if the container is in a time range view
     */
    private boolean isInTimeRangeView(){
        InteractionSpec interactionSpec = this.parent.getInteractionSpec();
        return 
            interactionSpec instanceof DateStateContext &&
            ((DateStateContext)interactionSpec).getViewKind() == ViewKind.TIME_RANGE_VIEW;
    }
    
    
    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
//  @Override
    public DataObject_1_0 remove(
        Object key
    ) {
    	DataObject_1_0 value = this.get(key);
    	if(value != null) try {
			if(!this.isInTimeRangeView()){
			    throw new UnsupportedOperationException(
			        "Inappropriate context for state view modification: " +
			        this.parent.getInteractionSpec()
			    );
			}
			Container_1_0 viewContainer = this.parent.objGetContainer(
				this.container.openmdxjdoGetTransientContainerId().getFeature()
			);
	        this.parent.jdoGetPersistenceManager().deletePersistent(viewContainer.get(key));
    	} catch (ServiceException exception) {
    		throw new RuntimeServiceException(exception);
    	}
        return value;
    }

    
    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
//  @Override
    public int size() {
        return this.keySet().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
//  @Override
    public Collection<DataObject_1_0> values() {
        if(this.values == null) {
            this.values = new AbstractCollection<DataObject_1_0>() {

                @Override
                public Iterator<DataObject_1_0> iterator(
                ){
                    return new ValueIterator();
                }

                @Override
                public int size() {
                    return StateCapableContainer_1.this.size();
                }

                /* (non-Javadoc)
                 * @see java.util.AbstractCollection#isEmpty()
                 */
                @Override
                public boolean isEmpty() {
                    return StateCapableContainer_1.this.isEmpty();
                }
                
            };
        }
        return this.values;
    }


    //-------------------------------------------------------------------------
    // Implements Container_1_0
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#retrieve()
     */
//  @Override
    public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
        this.selection.openmdxjdoRetrieve(fetchPlan);
    }

//  @Override
    public boolean isRetrieved() {
        return this.selection.isRetrieved();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoEvict()
     */
//  @Override
    public void openmdxjdoEvict(boolean allMembers, boolean allSubSets) {
        this.selection.openmdxjdoEvict(allMembers, allSubSets);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#refreshAll()
     */
//  @Override
    public void openmdxjdoRefresh() {
        this.selection.openmdxjdoRefresh();
    }

    
    //-------------------------------------------------------------------------
    // Extends Object
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return this.selection.toString();
    }
    
    
    //-------------------------------------------------------------------------
    // Class Keys
    //-------------------------------------------------------------------------
    
    /**
     * Keys
     */
    class Keys extends AbstractSet<String> {        
        
        /**
         * Constructor 
         *
         * @param delegate
         */
        Keys(
            Set<String> delegate
        ){
            this.delegate = delegate;
        }

        /**
         * objGetDelegate()'s entry set
         */
        private final Set<String> delegate;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<String> iterator(
        ){
            return new KeyIterator(this.snapshot().iterator());
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.snapshot().size();
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return this.snapshot().isEmpty();
        }

        private Collection<String> snapshot(){
            if(StateCapableContainer_1.this.container.isEmpty()) {
                return Collections.emptyList();
            } else {
                Set<String> keys = new LinkedHashSet<String>();
                for(String candidate : this.delegate){
                    keys.add(this.toKey(candidate));
                }
                return keys;
            }
        }
        
        /**
         * Derive the key from the qualifier
         * 
         * @param qualifier
         * 
         * @return the derived key
         */
        private String toKey(
            String qualifier
        ){
            PathComponent component = new PathComponent(qualifier);
            if(component.isPlaceHolder() && component.size() == 3) {
                return component.get(1);
            } else if (component.isPrivate()) {
                return component.getPrefix(component.size() - 2).toString();
            } else {
                // TODO support aspect specific PathComponent patterns
                return qualifier;
            }
        }
        
    }
    

    //-------------------------------------------------------------------------
    // Class KeyIterator
    //-------------------------------------------------------------------------

    /**
     * Key Iterator
     */
    class KeyIterator implements Iterator<String> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        KeyIterator(
            Iterator<String> delegate
        ){
            this.delegate = delegate;
        }
        
        /**
         * 
         */
        private final Iterator<String> delegate;
        
        /**
         * 
         */
        private String current = null;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
    //  @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
    //  @Override
        public String next() {
            return this.current = this.delegate.next();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
    //  @Override
        public void remove() {
        	this.delegate.remove();
            StateCapableContainer_1.this.remove(this.current);
            this.current = null;
        }
        
    }
    
    
    //------------------------------------------------------------------------
    // Class ValueIterator
    //------------------------------------------------------------------------

    /**
     * Value Iterator
     */
    class ValueIterator implements Iterator<DataObject_1_0> {

        /**
         * 
         */
        private final Iterator<String> delegate = StateCapableContainer_1.this.keySet().iterator();

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
    //  @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
    //  @Override
        public DataObject_1_0 next() {
            return StateCapableContainer_1.this.get(
                this.delegate.next()
            );
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
    //  @Override
        public void remove() {
            this.delegate.remove();
        }

    }
    
    
    //-------------------------------------------------------------------------
    // Class EntryIterator
    //-------------------------------------------------------------------------

    /**
     * Entry Iterator
     */
    class EntryIterator implements Iterator<Map.Entry<String,DataObject_1_0>>  {

        /**
         * 
         */
        private final Iterator<String> delegate = StateCapableContainer_1.this.keySet().iterator();

        /**
         * 
         */
        String current = null;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
    //  @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
    //  @Override
        public Map.Entry<String,DataObject_1_0> next() {
            this.current = this.delegate.next(); 
            return new Map.Entry<String, DataObject_1_0>(){

                public String getKey() {
                    return EntryIterator.this.current;
                }

                public DataObject_1_0 getValue() {
                    return StateCapableContainer_1.this.get(EntryIterator.this.current);
                }

                public DataObject_1_0 setValue(DataObject_1_0 value) {
                    throw new UnsupportedOperationException();
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
    //  @Override
        public void remove() {
            this.delegate.remove();
        }
 
    }

}

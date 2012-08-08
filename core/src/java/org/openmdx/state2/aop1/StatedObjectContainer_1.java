/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StatedObjectContainer_1.java,v 1.10 2009/02/12 18:34:42 hburger Exp $
 * Description: State Object Container
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/12 18:34:42 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.resource.cci.InteractionSpec;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.state2.cci.DateStateContext;
import org.openmdx.state2.cci.ViewKind;

/**
 * State Object Container
 */
public class StatedObjectContainer_1 
    implements Serializable, Container_1_0, Delegating_1_0 
{

    /**
     * Constructor 
     *
     * @param parent
     * @param container
     */
    public StatedObjectContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container
    ) throws ServiceException {
        this(
            parent,
            container,
            "org:openmdx:state2:DateState"
        );
    }

    
    /**
     * Constructor 
     *
     * @param parent
     * @param container
     * @param instanceOf
     */
    protected StatedObjectContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container,
        String instanceOf 
    ) throws ServiceException {
        this(
            parent,
            container,
            getFilter(
                parent.getInteractionSpec(),
                instanceOf
            )
        );
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param selection
     * @param criteria
     */
    protected StatedObjectContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 container,
        Object criteria
    ){
        this.parent = parent;
        this.container = container;
        this.selection = (Container_1_0)container.subMap(criteria);
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Container_1_0#superSet()
     */
    public Container_1_0 superSet(
    ) {
        return this.container.superSet();
    }     

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Container_1_0#getObjectId()
     */
    public Object getContainerId() {
        return this.container.getContainerId();
    }
    
    protected final ObjectView_1_0 parent;
 
    protected final Container_1_0 container;
    
    protected final Container_1_0 selection;
    
    private transient Set<Map.Entry<String, DataObject_1_0>> entries = null;
    
    private transient Collection<DataObject_1_0> values = null;

    private transient Set<String> keys = null;
    
    public final FilterableMap<String,DataObject_1_0> objGetDelegate(){
        return this.selection;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 1312568818384216689L;

    /**
     * Derive the filter from the state context
     * 
     * @param interactionSpec
     * @param instanceOf 
     * 
     * @return the corresponding filter
     */
    protected static FilterProperty[] getFilter(
        InteractionSpec interactionSpec, 
        String instanceOf
    ){
        if(interactionSpec instanceof DateStateContext) {
            DateStateContext stateContext = (DateStateContext) interactionSpec;
            switch(stateContext.getViewKind()) {
                case TIME_RANGE_VIEW:
                    return new FilterProperty[]{
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            SystemAttributes.OBJECT_INSTANCE_OF,
                            FilterOperators.IS_IN,
                            instanceOf
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            "stateValidFrom",
                            FilterOperators.IS_LESS_OR_EQUAL,
                            stateContext.getValidTo()
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            "stateValidTo",
                            FilterOperators.IS_GREATER_OR_EQUAL,
                            stateContext.getValidFrom()
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            SystemAttributes.REMOVED_AT,
                            FilterOperators.IS_IN
                        )
                    };
                case TIME_POINT_VIEW:
                    return stateContext.getExistsAt() == null ? new FilterProperty[]{
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            SystemAttributes.OBJECT_INSTANCE_OF,
                            FilterOperators.IS_IN,
                            instanceOf
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            "stateValidFrom",
                            FilterOperators.IS_LESS_OR_EQUAL,
                            stateContext.getValidAt()
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            "stateValidTo",
                            FilterOperators.IS_GREATER_OR_EQUAL,
                            stateContext.getValidAt()
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            SystemAttributes.REMOVED_AT,
                            FilterOperators.IS_IN
                        )
                    } : new FilterProperty[]{
                        new FilterProperty(
                            Quantors.THERE_EXISTS,
                            SystemAttributes.OBJECT_INSTANCE_OF,
                            FilterOperators.IS_IN,
                            instanceOf
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            "stateValidFrom",
                            FilterOperators.IS_LESS_OR_EQUAL,
                            stateContext.getValidAt()
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            "stateValidTo",
                            FilterOperators.IS_GREATER_OR_EQUAL,
                            stateContext.getValidAt()
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            SystemAttributes.CREATED_AT,
                            FilterOperators.IS_LESS_OR_EQUAL,
                            stateContext.getExistsAt()
                        ),
                        new FilterProperty(
                            Quantors.FOR_ALL,
                            SystemAttributes.REMOVED_AT,
                            FilterOperators.IS_GREATER_OR_EQUAL,
                            stateContext.getExistsAt()
                        )
                    };
            }
        }
        return null;
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
     */
    public FilterableMap<String, DataObject_1_0> subMap(Object filter) {
        return filter == null ? this : new StatedObjectContainer_1(
            this.parent,
            this.selection,
            filter
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
     */
    public List<DataObject_1_0> values(Object criteria) {
        return this.selection.values(criteria);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        entrySet().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return keySet().contains(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(
        Object value
    ) {
        return values().contains(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet() {
        if(this.entries == null) {
            this.entries = new AbstractSet<Map.Entry<String,DataObject_1_0>>()  {

                @Override
                public Iterator<Map.Entry<String,DataObject_1_0>> iterator() {
                    return new EntryIterator();
                }

                @Override
                public int size() {
                    return StatedObjectContainer_1.this.size();
                }
                
            };
        }    
        return this.entries;
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public DataObject_1_0 get(Object key) {
        return this.container.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return this.selection.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return this.keys == null ? 
            this.keys = new Keys(this.selection) :
            this.keys;
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public DataObject_1_0 put(String key, DataObject_1_0 value) {
        return this.selection.put(key, value);            
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends String, ? extends DataObject_1_0> t) {
        throw new UnsupportedOperationException();
    }

    private void assertModifiability(
    ) throws ServiceException {
        InteractionSpec interactionSpec = this.parent.getInteractionSpec();
        if(
            !(interactionSpec instanceof DateStateContext) ||
            ((DateStateContext)interactionSpec).getViewKind() != ViewKind.TIME_RANGE_VIEW
        ) {
            throw new UnsupportedOperationException(
                "Inappropriate context for state view modification: " +
                interactionSpec
            );
        }
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public DataObject_1_0 remove(
        Object key
    ) {
        try {
            assertModifiability();
            DataObject_1_0 value = get(key);
            if(value.jdoIsPersistent()) {
                JDOHelper.getPersistenceManager(value).deletePersistent(value);
            } 
            else {
                this.selection.remove(value);
            }
            return value;
        } 
        catch(Exception exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    
    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return keySet().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
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
                    return StatedObjectContainer_1.this.size();
                }
                
            };
        }
        return this.values;
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
            Map<String,DataObject_1_0> delegate
        ){
            this.delegate = delegate.entrySet();
        }

        /**
         * objGetDelegate()'s entry set
         */
        private final Set<Map.Entry<String,DataObject_1_0>> delegate;
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<String> iterator(
        ){
            return snapshot().iterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return snapshot().size();
        }
        
        private Collection<String> snapshot(){
            if(StatedObjectContainer_1.this.container.isEmpty()) {
                return Collections.emptyList();
            } else {
                List<String> keys = new ArrayList<String>();
                for(Map.Entry<String,DataObject_1_0> candidate : this.delegate){
                    String key = toKey(candidate.getKey());
                    if(!keys.contains(key)) {
                        keys.add(key);
                    }
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
            Set<String> delegate
        ){
            this.delegate = delegate.iterator();
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
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public String next() {
            return this.current = this.delegate.next();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            if(this.current == null) {
                throw new IllegalStateException("No current element");
            }
            StatedObjectContainer_1.this.remove(this.current);
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
        private final Iterator<String> delegate = keySet().iterator();

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public DataObject_1_0 next() {
            return StatedObjectContainer_1.this.get(
                this.delegate.next()
            );
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
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
        private final Iterator<String> delegate = keySet().iterator();

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Map.Entry<String,DataObject_1_0> next() {
            final String key = this.delegate.next(); 
            return new Map.Entry<String, DataObject_1_0>(){

                public String getKey() {
                    return key;
                }

                public DataObject_1_0 getValue() {
                    return StatedObjectContainer_1.this.get(key);
                }

                public DataObject_1_0 setValue(DataObject_1_0 value) {
                    throw new UnsupportedOperationException();
                }
                
            };
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            this.delegate.remove();
        }
 
    }

}

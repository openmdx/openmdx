/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateMap_1.java,v 1.1 2008/10/20 22:03:56 hburger Exp $
 * Description: StateMap_1 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/10/20 22:03:56 $
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
package org.openmdx.state2.plugin.generic;

import java.io.Flushable;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.kernel.exception.BasicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StateMap_1
 */
class StateMap_1
    extends AbstractMap<String,Object_1_0>
    implements FilterableMap<String,Object_1_0>, Flushable
{

    /**
     * Constructor 
     */
    StateMap_1(
        Object_1_0 core,
        FilterableMap<String,Object_1_0> persistent
    ) {
        this.core = core;
        this.persistent = persistent;
        this.entries = new EntrySet(
            persistent.entrySet(),
            null
        );
    }

    /**
     * Implement these methods as soon as they are really needed
     */
    private final static String NOT_YET_IMPLEMENTED = 
        "This operation is not yet implemented for state maps";
    
    /**
     * 
     */
    protected final Set<Map.Entry<String,Object_1_0>> entries;
    
    /**
     * 
     */
    private final Object_1_0 core;
    
    /**
     * 
     */
    private final FilterableMap<String,Object_1_0> persistent;
    
    /**
     * 
     */
    private static Logger logger = LoggerFactory.getLogger(StateMap_1.class);
    
    
    //------------------------------------------------------------------------
    // Implements Flushable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.io.Flushable#flush()
     */
    public void flush(
    ) throws IOException {
        int lastStateId = -1;
        List<Object_1_0> pending = new ArrayList<Object_1_0>();
        for(Map.Entry<String, Object_1_0> e : this.persistent.entrySet()) {
            String qualifier = e.getKey();
            if(new PathComponent(qualifier).isPlaceHolder()) {
                pending.add(e.getValue());
            } else {
                int stateId = getStateId(qualifier);
                if(stateId > lastStateId) {
                    lastStateId = stateId;
                }
            }
        }
        try {
            String prefix = this.core.objGetPath().getBase() + '*';
            for(Object_1_0 state : pending) {
                
                
            }
        } catch (ServiceException exception) {
            throw new ExtendedIOException(exception);
        }
        
    }

    /**
     * Fetch the state id from the state qualifier
     * 
     * @param qualifier
     * 
     * @return the last star sub-segment as int value
     */
    private static int getStateId(
        String qualifier
    ){
        int star = qualifier.lastIndexOf('*');
        if(star < 0) {
            logger.warn("State qualifier '{}' has no star segment", qualifier);
            return -1;
        } else {
            String stateId = qualifier.substring(star + 1);
            try {
                return Integer.valueOf(stateId);
            } catch (NumberFormatException exception) {
                logger.warn(
                    "State qualifier has no valid state id", 
                    new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "The state id should be an integer value",
                        new BasicException.Parameter("qualifier", qualifier),
                        new BasicException.Parameter("stateId", stateId)
                    )
                );
                return -1;
            }
        }
    }
    

    //------------------------------------------------------------------------
    // Implements Map
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.AbstractMap#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, Object_1_0>> entrySet() {
        return this.entries;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object_1_0 put(
        String key, 
        Object_1_0 value
    ) {
        try {
            value.objSetValue("core", this.core);
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
        return this.persistent.put(
            key == null ? PathComponent.createPlaceHolder().toString() : key, 
            value
        );
    }

    
    //------------------------------------------------------------------------
    // Implements FilterableMap
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
     */
    public FilterableMap<String, Object_1_0> subMap(Object filter) {
        throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
     */
    public List<Object_1_0> values(Object criteria) {
        if(criteria == null) {
            return new AbstractSequentialList<Object_1_0>(){

                @Override
                public ListIterator<Object_1_0> listIterator(
                    int index
                ) {
                    return new ValueIterator(
                        new ArrayList<Object_1_0>(
                            new TreeMap<String, Object_1_0>(StateMap_1.this).values()
                        ).listIterator(
                            index
                        )
                    );
                }

                @Override
                public int size() {
                    return StateMap_1.this.entries.size();
                }
                
            };
        } else {
            throw new UnsupportedOperationException(NOT_YET_IMPLEMENTED);
        }
    }

    
    //------------------------------------------------------------------------
    // Class EntrySet
    //------------------------------------------------------------------------

    /**
     * Entry Set
     */
    static class EntrySet extends AbstractSet<Map.Entry<String,Object_1_0>> {

        /**
         * Constructor 
         *
         * @param persistent
         * @param pending
         */
        EntrySet(
            Set<Map.Entry<String,Object_1_0>> persistent,
            Set<Map.Entry<String,Object_1_0>> pending
        ){
            this.persistent = persistent;
            this.pending = pending;
        }
        
        /**
         * Entries for the persistent states
         */
        private final Set<Map.Entry<String,Object_1_0>> persistent;
        
        /**
         * Entries for the pending states
         */
        private final Set<Map.Entry<String,Object_1_0>> pending;
                
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @SuppressWarnings("unchecked")
        @Override
        public Iterator<java.util.Map.Entry<String, Object_1_0>> iterator() {
            return null; // new EntryIterator(
//                this.persistent.iterator(),
//                this.pending.iterator()
//            );
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return this.persistent.size() + this.pending.size();
        }
        
    }


    //------------------------------------------------------------------------
    // Class EntryIterator
    //------------------------------------------------------------------------

    /**
     * Entry Iterator
     */
    class EntryIterator implements Iterator<Map.Entry<String,Object_1_0>> {

        /**
         * Iterator for the persistent states
         */
        private final Iterator<Map.Entry<String,Object_1_0>> persistent = 
            StateMap_1.this.persistent.entrySet().iterator();

        
        
        /**
         * Current iterator
         */
        private int delegateIndex = 0;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
//            while(this.delegateIndex < this.delegate.length) {
//                if(this.delegate[delegateIndex].hasNext()) {
//                    return true;
//                }
//                delegateIndex++;
//            }
            return false; 
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public java.util.Map.Entry<String, Object_1_0> next() {
//            if(hasNext()) {
//                return (this.current = this.delegate[this.delegateIndex]).next();
//            } else {
                throw new NoSuchElementException("No next element");
//            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
//            if(this.current == null) {
                throw new IllegalStateException("No current element");
//            } else {
//                this.current.remove();
//            }
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class ValueIterator
    //------------------------------------------------------------------------

    /**
     * Value Iterator
     */
    static class ValueIterator implements ListIterator<Object_1_0> {

        /**
         * Constructor 
         *
         * @param delegate
         */
        ValueIterator(
            ListIterator<Object_1_0> delegate
        ){
            this.delegate = delegate;
        }
        
        /**
         * The delegate
         */
        private final ListIterator<Object_1_0> delegate;

        /**
         * The last returned object
         */
        private Object_1_0 current = null;
        
        /* (non-Javadoc)
         * @see java.util.ListIterator#add(java.lang.Object)
         */
        public void add(Object_1_0 o) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasNext()
         */
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#hasPrevious()
         */
        public boolean hasPrevious() {
            return this.delegate.hasPrevious();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#next()
         */
        public Object_1_0 next() {
            return this.current = this.delegate.next();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#nextIndex()
         */
        public int nextIndex() {
            return this.delegate.nextIndex();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previous()
         */
        public Object_1_0 previous() {
            return this.current = this.delegate.previous();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#previousIndex()
         */
        public int previousIndex() {
            return this.delegate.previousIndex();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#set(java.lang.Object)
         */
        public void set(Object_1_0 o) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see java.util.ListIterator#remove()
         */
        public void remove() {
            if(this.current == null) {
                throw new IllegalStateException("No current element");
            } else try {
                    this.current.objRemove();
                    this.current = null;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }            
        }        
        
    }

}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractContainer.java,v 1.2 2009/06/09 12:45:18 hburger Exp $
 * Description: Abstract Container
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:18 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.accessor.rest;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.query.AbstractFilter;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.ModelAwareFilter;
import org.openmdx.base.query.Selector;
import org.openmdx.compatibility.state1.spi.StateCapables;

/**
 * Abstract Container
 */
@SuppressWarnings("deprecation")
abstract class AbstractContainer<E>
    extends AbstractCollection<E>
    implements org.openmdx.base.collection.Container<E>, Serializable
{

    private static final long serialVersionUID = 7238551247775407671L;

    /**
     * Constructor
     *  
     * @param attributeFilter
     *
     */
    protected AbstractContainer(
        Model_1_0 model,
        Selector attributeFilter
    ) {
        this.model = model;
        this.attributeFilter = attributeFilter;
    }

    /**
     * The model repository
     */
    private final Model_1_0 model;
    
    /**
     * The container's attribute filter
     */
    private Selector attributeFilter;
    
    /**
     * The current sequence state has to be requested from the subclass.
     */
    private static final long SEQUENCE_INITIALIZATION_PENDING = -2L;

    /**
     * The subclass has requested to use UUIDs instead of sequence values.
     */
    protected static final long SEQUENCE_NOT_SUPPORTED = -1L;

    /**
     * If sequences are supported by either the application or persistence 
     * layer.
     */
    protected static final long SEQUENCE_MIN_VALUE = 0L;

    /**
     * The next qualifier.
     */
    private long nextQualifier = SEQUENCE_INITIALIZATION_PENDING;

    abstract Object getContainerId();
    
    /**
     * Retrieve the attribute filter
     * 
     * @return the attribute filter
     */
    protected final Selector getSelector(){
        return this.attributeFilter;
    }
    
    /**
     * Initial qualifier value callback method.
     * 
     * @return the initial qualifier value; or -1L for UUIDs
     * 
     * @exception ServiceException
     */
    protected abstract long initialQualifier(
    ) throws ServiceException;

    /**
     * Get the next qualifier
     * 
     * @return the next qualifier,
     * or null if a UID shoid be used
     */
    synchronized String nextQualifier(
    ){
        if(this.nextQualifier == SEQUENCE_INITIALIZATION_PENDING) try {
            this.nextQualifier = initialQualifier();
        } catch (ServiceException exception) {
            // this.nextQualifier remains < 0L
        } finally {
            if(this.nextQualifier < 0L) this.nextQualifier = SEQUENCE_NOT_SUPPORTED;
        }
        return this.nextQualifier == SEQUENCE_NOT_SUPPORTED ?
            null :
            String.valueOf(this.nextQualifier++);
    }

    /**
     * Evict the object
     */
    protected void evict(
    ){
        this.nextQualifier = SEQUENCE_INITIALIZATION_PENDING;
    }
    
    /**
     * Retrieve the model
     * 
     * @return the model
     */
    protected Model_1_0 getModel(){
        return this.model;
    }

    /**
     * Get the selector by ignoring the current attribute filter.
     * 
     * @param filter
     * 
     * @return the derived selector; or <code>null</code> if the
     * filter is <code>null</code>
     */
    protected Selector getSelector(
        Object filter
    ){
        return filter == null ? null : new ObjectFilter_1(
            getModel(),
            (FilterProperty[])filter
        ); 
    }
    
    /**
     * Combine the current attribute filter with the requested one.
     * 
     * @param filter
     * 
     * @return the combined filter; or <code>null</code> if the
     * actual filter includes the requested one.
     */
    protected Selector combineWith(
        Object filter
    ){
        if(filter == null) {
            return null;
        } else {
            if(this.attributeFilter == null) {
                return new ObjectFilter_1(
                    getModel(),
                    (FilterProperty[])filter
                );
            } else {
                Set<FilterProperty> target = new LinkedHashSet<FilterProperty>();
                for(FilterProperty e : ((AbstractFilter)this.attributeFilter).getDelegate()) {
                    target.add(e);
                }
                boolean changed = false;
                for(FilterProperty e : (FilterProperty[])filter) {
                    changed |= target.add(e);
                }
                if(changed) {
                    return new ObjectFilter_1(
                        getModel(),
                        target.toArray(
                            new FilterProperty[target.size()]
                        )
                    );
                } else {
                    return null;
                }
            }
        }
        
    }

    /**
     * Object Filter
     */
    private static class ObjectFilter_1 extends ModelAwareFilter {

        /**
         * Constructor for a sub-class aware filter
         * 
         * @param model
         * @param filter
         * 
         * @exception   IllegalArgumentException
         *              in case of an invalid filter property set
         * @exception   NullPointerException
         *              if the filter is <code>null</code>
         */
        public ObjectFilter_1(
            Model_1_0 model,
            FilterProperty[] filter
        ){
            super(model, filter);
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 875014812028655977L;

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.query.AbstractFilter#getValues(java.lang.Object, java.lang.String)
         */
        protected Iterator<?> getValues(
            Object candidate, 
            String attribute
        ){
            try {
                DataObject_1 object = (DataObject_1)candidate;
                return SystemAttributes.OBJECT_INSTANCE_OF.equals(attribute) ?
                    newInstanceOfIterator(object.objGetClass()) :
                    newCanonicalizingIterator(object.objGetIterable(attribute));
            } catch (Exception exception){
                new RuntimeServiceException(exception).log();
                return null;
            }
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.query.AbstractFilter#accept(java.lang.Object)
         */
        public boolean accept(Object candidate) {
            return !JDOHelper.isDeleted(candidate) && super.accept(candidate);
        }

        protected Iterator<?> newCanonicalizingIterator(
            final Iterable<?> iterable
        ){
            return new Iterator<Object>(){

                private final Iterator<?> delegate = iterable.iterator();
                
                public boolean hasNext() {
                    return this.delegate.hasNext();
                }

                public Object next() {
                    Object value = this.delegate.next();
                    return value instanceof DataObject_1_0 ? StateCapables.getResourceIdentifier (
                        ((DataObject_1_0)value).jdoGetObjectId()
                    ) : value;
                }

                public void remove() {
                    this.delegate.remove();
                }
                
            };
        }
        
    }
    
}

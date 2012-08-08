/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ModelAwareFilter.java,v 1.6 2008/11/10 17:23:45 hburger Exp $
 * Description: Model Aware Filter
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/10 17:23:45 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.query;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * Model Aware Filter
 */
public abstract class ModelAwareFilter 
    extends AbstractFilter
    implements ModelHolder_1_0
{

    /**
     * Constructor 
     * 
     * @param filter
     * 
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    protected ModelAwareFilter(
        FilterProperty[] filter
    ){
        this(null, filter);
    }

    /**
     * Constructor 
     * 
     * @param model 
     * @param filter
     * 
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    protected ModelAwareFilter(
        Model_1_0 model, 
        FilterProperty[] filter
    ){
        super(filter);
        this.model = model;
    }

    /**
     * 
     */
    protected transient Model_1_0 model;
    
    /**
     * 
     * @param objectClass
     * @return
     */
    protected Iterator<String> newInstanceOfIterator(
        String objectClass
    ){
        return new InstanceOfIterator(
            objectClass
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0#getModel()
     */
    public Model_1_0 getModel() {
        return this.model;
    }

    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0#setModel(org.openmdx.model1.accessor.basic.cci.Model_1_0)
     */
    public void setModel(Model_1_0 model) {
        this.model = model;
    }

    /**
     * InstanceOfIterator
     */
    private class InstanceOfIterator implements Iterator<String> {

        /**
         * Constructor 
         *
         * @param objectClass
         */
        InstanceOfIterator(
            String objectClass
        ){
            this.objectClass = objectClass;
            try {
                this.delegate = ModelAwareFilter.this.model == null ? (Collection<?>)
                    Collections.EMPTY_SET : 
                    ModelAwareFilter.this.model.getElement(
                        this.objectClass
                    ).values(
                        "allSupertype"
                    );
            } catch (Exception exception){
                throw new RuntimeServiceException(exception);
            }        
        }

        /**
         * 
         */
        private final String objectClass;

        /**
         * 
         */
        private final Collection<?> delegate;

        /**
         * 
         */
        private Iterator<?> iterator = null;
        
        /**
         * 
         */
        private boolean first = true;

        /**
         * Retrieve the super-types iterator
         * 
         * @return the super-types iterator
         */
        private Iterator<?> getSupertypeIterator(
        ){
            if(this.iterator == null) {
                this.iterator = this.delegate.iterator();
            }
            return this.iterator;
        }
        
        /**
         * Returns <tt>true</tt> if the iteration has more elements. (In other
         * words, returns <tt>true</tt> if <tt>next</tt> would return an element
         * rather than throwing an exception.)
         *
         * @return <tt>true</tt> if the iterator has more elements.
         * 
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return this.first || getSupertypeIterator().hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @exception NoSuchElementException iteration has no more elements.
         * 
         * @see java.util.Iterator#next()
         */
        public String next() {
            if(first){
                this.first = false;
                return this.objectClass;
            } else {
                return ((Path)getSupertypeIterator().next()).getBase();
            }
        }

        /**
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation).  This method can be called only once per
         * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
         * the underlying collection is modified while the iteration is in
         * progress in any way other than by calling this method.
         *
         * @exception UnsupportedOperationException if the <tt>remove</tt>
         *        operation is not supported by this Iterator.
         *        
         * @exception IllegalStateException if the <tt>next</tt> method has not
         *        yet been called, or the <tt>remove</tt> method has already
         *        been called after the last call to the <tt>next</tt>
         *        method.
         * 
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
            
}

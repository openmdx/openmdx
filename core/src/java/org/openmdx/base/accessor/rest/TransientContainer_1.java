/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TransientContainer_1.java,v 1.3 2009/06/09 12:45:18 hburger Exp $
 * Description: Transient Container
 * Revision:    $Revision: 1.3 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.rest;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.query.AbstractFilter;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Selector;


/**
 * Transient Container
 */
@SuppressWarnings("unchecked") 
class TransientContainer_1
    extends AbstractContainer<DataObject_1_0>
{

    /**
     * Constructor
     * 
     * @param modelHolder 
     */   
    TransientContainer_1(
        Model_1_0 model
    ){
        this(
            model,
            null, // attributeFilter
            new TreeMap() // reference
        );
    }
    
    /**
     * Constructor
     * @param modelHolder TODO
     */   
    private TransientContainer_1(
        Model_1_0 model,
        Selector attributeFilter,
        Map reference
    ){
        super(model,attributeFilter);
        this.reference = reference;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3257007657234609973L;

    /**
    *
    */
   final Map<String,DataObject_1_0> reference;  

    Set<Map.Entry<String, DataObject_1_0>> getEntrySet(
    ){
        return this.reference.entrySet();
    }
    
    
    //--------------------------------------------------------------------------
    // Extends AbstractContainer
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.delegation.AbstractContainer#initialQualifier()
     */
    protected long initialQualifier(
    ){
        return 0L;
    }
    
    
    //--------------------------------------------------------------------------
    // Implements Collection
    //--------------------------------------------------------------------------

    /**
     * Returns the number of elements in this collection. If the collection 
     * contains more than Integer.MAX_VALUE elements or the number of elements
     * is unknown, returns Integer.MAX_VALUE.
     *
     * @eturn   the number of elements in this collection
     */
    public int size(
    ) {
        return this.toList(null).size();
    }

    /**
     * Returns an iterator over the elements contained in this collection.
     * 
     * @return    an iterator over the elements contained in this collection.
     */
    public Iterator iterator(
    ){
        return toList(null).iterator();
    }

    /**
     * Returns true if this collection contains the specified element. 
     *
     * @param   element
     *          element whose presence in this collection is to be tested.
     *
     * @return  true if this collection contains the specified element
     */
    public boolean contains(
        Object element
    ){
        if(this.reference.containsValue(element)) {
            return 
               getSelector() == null ||
               getSelector().accept(element);
        } else {
            return false;
        }
    }

    /**
     * Removes a single instance of the specified element from this
     * container, if it is present 
     *
     * @param   element
     *          element to be removed from this container, if present.
     */
    public void remove(
        DataObject_1 element
    ){
        this.reference.values().remove(element);
    }


    //--------------------------------------------------------------------------
    // Implements Container
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.Container#retrieveAll(boolean)
     */
    public void retrieveAll(
        boolean useFetchPlan
    ) {
        // nothing to do
    }
    
    /**
     * Selects objects matching the filter.
     * <p>
     * The semantics of the collection returned by this method become
     * undefined if the backing collection (i.e., this list) is structurally
     * modified in any way other than via the returned collection. (Structural
     * modifications are those that change the size of this list, or otherwise
     * perturb it in such a fashion that iterations in progress may yield
     * incorrect results.) 
     * <p>
     * This method returns a Collection as opposed to a Set because it 
     * behaves as set in respect to object id equality, not element equality.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * Container implementation.
     *
     * @param     filter
     *            The filter to be applied to objects of this container
     *
     * @return    A subset of this container containing the objects
     *            matching the filter.
     * 
     * @exception ClassCastException
     *            if the class of the specified filter prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this filter prevents it from being
     *            applied to this container. 
     */
    @SuppressWarnings("deprecation")
    public org.openmdx.base.collection.Container subSet(
        Object filter
    ){
        Selector selector = super.combineWith(filter);
        return selector == null ? this : new TransientContainer_1(
            getModel(),
            selector,
            this.reference
        );
    }

    /**
     * Select an object matching the filter.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * Container implementation.
     *
     * @param     filter
     *            The filter to be applied to objects of this container
     *
     * @return    the object matching the filter;
     *            or null if no object matches the filter.
     * 
     * @exception ClassCastException
     *            if the class of the specified filter prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this filter prevents it from being
     *            applied to this container or if more than one object 
     *            matches the filter. 
     */
    public DataObject_1_0 get(
        Object filter
    ){
        DataObject_1_0 candidate = this.reference.get(filter);
        return getSelector() == null || getSelector().accept(candidate) ? candidate : null; 
    }

    /**
     * Add an object using specific criteria.
     * <p>
     * The acceptable criteria object classes must be specified by the 
     * container implementation.
     * <p>
     * <code>add(null, element)</code> is equivalent to
     * <code>add(element)</code>.
     *
     * @param     criteria
     *            The criteria to be used to add the element.
     * @param     element
     *            the object to be added
     *
     * @exception ClassCastException
     *            if the class of the specified criteria or element prevents it
     *            from being added to this container.
     * @exception IllegalArgumentException
     *            if some aspect of the specified criteria or element prevents 
     *            it from being added to this container.
     */
    void add(
        String criteria,
        DataObject_1_0 element
    ){
        this.reference.put(
            criteria == null ? PathComponent.createPlaceHolder().toString() : criteria,
            element
        );
    }
                
    /**
     * Returns an unmodifiable list backed up by the underlaying container.
     * <p>
     * The acceptable order object classes must be specified by the container 
     * implementation.
     *
     * @param     order
     *            The order to be applied to objects of this container;
     *            or null for the collection's default order.
     *
     * @return    A list containing the objects of this container
     *            sorted according to the given order.
     * 
     * @exception ClassCastException
     *            if the class of the specified order prevents it from
     *            being applied to this container.
     * @exception IllegalArgumentException
     *            if some aspect of this order prevents it from being
     *            applied to this container. 
     */
    public List toList(
        Object order
    ){
        return new TransientList(
            order == null ? null : new ObjectComparator_1((AttributeSpecifier[])order)
        );
    }

    
    //------------------------------------------------------------------------
    // Implements RefBaseObject
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMofId()
     */
    public Object getContainerId() {
        return null; // Transient container's don't have a MOF id yet
    }
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    public String toString(
    ){
        StringBuilder text = new StringBuilder(
            getClass().getName()
        );
        try {
            text.append(
                ": (transient, "
            ).append(
                getSelector() == null ? 0 : ((AbstractFilter)getSelector()).size()
            ).append(
                " filter properties)"
            ).toString();
        } catch (Exception exception) {
            text.append(
                "// "
            ).append(
                exception.getMessage()
            );
        }
        return text.toString();
    }

    
    //------------------------------------------------------------------------
    // Class TransientList
    //------------------------------------------------------------------------

    /**
     * Transient List
     */
    final class TransientList 
        extends AbstractList
    {
        
        /**
         * Constructor 
         *
         * @param comparator
         */
        TransientList(
            Comparator comparator
        ){
            Selector selector = getSelector();
            for(DataObject_1_0 candidate : reference.values()) {
                if(selector == null || selector.accept(candidate)) {
                    if(comparator == null) {
                        this.list.add(candidate);
                    } else {
                        int i = 0;
                        while(
                            i < list.size() &&
                            comparator.compare(candidate, list.get(i)) > 0
                        ){
                            i++;
                        }
                        if(i < this.list.size()) {
                            this.list.add(i, candidate);
                        } else {
                            this.list.add(candidate);
                        }
                    }
                }
            }
        }

        /**
         * 
         */
        private final List<DataObject_1_0> list = new ArrayList();
        
        public int size(
        ){
            return this.list.size();
        }

        public Object get(
            int index
        ){
            return this.list.get(index);
        }
                
        public Object remove(
            int index
        ){
            try{
                DataObject_1 object=(DataObject_1)this.list.remove(index);
                reference.values().remove(object);
                object.objMove(null, null);
                return object;
            } catch (ServiceException exception){
                throw new UnsupportedOperationException(exception.getMessage());
            }
        }

    }

}

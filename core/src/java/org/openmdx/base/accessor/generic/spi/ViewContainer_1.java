/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ViewContainer_1.java,v 1.39 2008/09/10 08:55:21 hburger Exp $
 * Description: ViewContainer_1 
 * Revision:    $Revision: 1.39 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:21 $
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
package org.openmdx.base.accessor.generic.spi;

import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.AbstractFilter;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.ModelAwareFilter;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.compatibility.state1.view.DateStateContext;
import org.openmdx.compatibility.state1.view.DateStateContexts;
import org.openmdx.kernel.exception.BasicException;

/**
 * ViewContainer_1
 */
@SuppressWarnings("unchecked")
class ViewContainer_1
extends AbstractMap
implements ViewContainer_1_0, FilterableMap
{

    /**
     * Constructor 
     *
     * @param marshaller
     * @param parent
     * @param feature
     */
    ViewContainer_1(
        ViewConnection_1 marshaller, 
        ViewObject_1 parent,
        String feature
    ){
        this.dateStateContainer = null;
        this.marshaller = marshaller;
        this.attributeFilter = EMPTY_FILTER; 
        this.parent = parent;
        this.feature = feature;
        this.dateStateRequest = false;
        this.transientObjects = parent.objIsPersistent() ? null : new HashMap();
    }

    /**
     * Constructor 
     *
     * @param marshaller
     * @param parent
     * @param feature
     * @param dateStateContainer
     * @param attributeFilter
     * @param dateStateRequest
     * @param transientObjects
     */
    private ViewContainer_1(
        ViewConnection_1 marshaller, 
        ViewObject_1 parent,
        String feature, 
        Boolean dateStateContainer,
        FilterProperty[] attributeFilter,
        boolean dateStateRequest,
        Map transientObjects
    ){
        this.marshaller = marshaller;
        this.attributeFilter = attributeFilter; 
        this.parent = parent;
        this.feature = feature;
        this.dateStateContainer = dateStateContainer;
        this.dateStateRequest = dateStateRequest;
        this.transientObjects = transientObjects;
    }

    final ViewConnection_1 marshaller;

    final ViewObject_1 parent;

    private final String feature;

    final FilterProperty[] attributeFilter;

    private final boolean dateStateRequest;

    Map transientObjects;

    private Boolean dateStateContainer;

    private transient FilterableMap persistentObjects = null;

    private transient ModelAwareFilter filter = null;

    private transient Set entries = null;

    private static final FilterProperty[] EMPTY_FILTER = new FilterProperty[] {};

    private static final List<String> VALIDITY_ATTRIBUTES = Arrays.asList(
        SystemAttributes.CREATED_AT,
        State_1_Attributes.INVALIDATED_AT,
        State_1_Attributes.STATE_VALID_FROM,
        State_1_Attributes.STATE_VALID_TO
    );

    /**
     * Do not throw an exception when removing non-existent objects
     */
    private static final Object NULL = new Object();

    /**
     * Tests whether this container contains instances of DateState
     * 
     * @return <code>true</code> if this container contains instances of DateState
     */
    boolean isDateStateContainer(
    ){
        if(this.dateStateContainer == null) try {
            this.dateStateContainer = Boolean.valueOf(
                this.parent.containsInstancesOfDateState(this.feature)
            );
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
        return this.dateStateContainer.booleanValue();
    }

    /**
     * Tests whether it is a request for states or objects
     * 
     * @return <code>true</code> if it is a request for states
     */
    boolean isDateStateRequest(){
        return this.dateStateRequest;
    }

    /**
     * Test whether a given object is involved in the view's context
     * 
     * @param sinkObject
     * @param defaultValue the default value in case the object is not cached
     * @return
     * @throws ServiceException
     */
    boolean isInvolved (
        SinkObject_1 sinkObject, 
        boolean defaultValue
    ) throws ServiceException {
        DateStateContext context = marshaller.getContext();
        return context.isWritable() ? sinkObject.isInvolved(
            context.getValidFrom(), 
            context.getValidTo(), 
            defaultValue
        ) : sinkObject.isInvolved(
            context.getValidFor(), 
            context.getValidAt(), 
            defaultValue 
        );
    }

    boolean isPersistent(){
        return this.parent.objIsPersistent();
    }

    Path getPath(
    ) throws ServiceException{
        return this.parent.objGetPath().getChild(this.feature);
    }

    FilterableMap getSinkContainer(
    ){
        if(this.persistentObjects == null) try {
            this.persistentObjects = this.parent.getSinkContainer(this.feature);
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
        return this.persistentObjects;
    }

    ViewObject_1 getParent(){
        return this.parent;
    }

    Collection getSinkObjects(
        boolean fetch
    ){
        try {
            return this.parent.getSinkObjects(this.feature, fetch);
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    SparseList getBeforeImage(
    ){
        try {
            Object_1_0 sourceObject = (Object_1_0) this.parent.objGetDelegate();
            SparseList beforeImage = sourceObject == null ? null : (SparseList) sourceObject.objGetValue(
                "!" + this.feature // retrieve persistent value
            );
            if(beforeImage != null) {
                this.marshaller.marshalPaths(beforeImage);
            }
            return beforeImage;
        } catch (ServiceException exception) {
            return null;
        }
    }


    //------------------------------------------------------------------------
    // Implements ViewContainer_1_0
    //------------------------------------------------------------------------

    /**
     * Retrieve all states of an object
     * 
     * @param qualifier the object's qualifier
     * @param invalidated tells whether one looks for valid or invalid states 
     * @param deleted tells whether one non-deleted or deleted states
     * 
     * @return all states of an object
     * 
     * @throws ServiceException 
     */
    public Collection allStates(
        String qualifier,
        Boolean invalidated, 
        Boolean deleted
    ) throws ServiceException{
        ViewObject_1_0 object = (ViewObject_1_0) get(qualifier);
        return object.allStates(invalidated, deleted);
    }


    //------------------------------------------------------------------------
    // Extends FilterableMap
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractMap#entrySet()
     */
    public Set entrySet() {
        if(this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
     */
    public FilterableMap subMap(
        Object filter
    ) {
        if(filter instanceof FilterProperty[]) {
            FilterProperty[] source = (FilterProperty[]) filter;
            int i = State_1_Attributes.indexOfStatedObject(source);
            boolean dateStateRequest = i >= 0;
            FilterProperty[] target = new FilterProperty[
                                                         this.attributeFilter.length + source.length - (dateStateRequest ? 1 : 0)
                                                         ];
            System.arraycopy(this.attributeFilter, 0, target, 0, this.attributeFilter.length);
            if(dateStateRequest) {
                if(i > 0) {
                    System.arraycopy(source, 0, target, this.attributeFilter.length, i);
                }
                System.arraycopy(source, i + 1, target, this.attributeFilter.length + i, source.length - i - 1);
            } else {
                System.arraycopy(source, 0, target, this.attributeFilter.length, source.length);
            }
            return new ViewContainer_1(
                this.marshaller, 
                this.parent,
                this.feature, 
                this.dateStateContainer,
                target,
                this.dateStateRequest || dateStateRequest,
                this.transientObjects
            );
        } else throw new IllegalArgumentException(
            "View containers support only filters of type FilterProperty[]s"
        );
    }

    /**
     * Tests whether the attribute specifier requires ordering
     * 
     * @return <code>true</code> if the attribute specifier requires ordering
     */
    boolean order (
        AttributeSpecifier[] attributeSpecifiers
    ) {
        if(attributeSpecifiers != null) {
            for(
                    int i = 0;
                    i < attributeSpecifiers.length;
                    i++
            ){
                if(attributeSpecifiers[i].order() != Orders.ANY) {
                    return true;
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
     */
    public List values(
        Object criteria
    ) {
        try {
            return new ValueList((AttributeSpecifier[]) criteria);
        } catch (ClassCastException exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The criteria argument should be an instance of AttributeSpecifier[]",
                new BasicException.Parameter(
                    "criteria-class",
                    criteria.getClass().getName()
                )
            ); 
        }
    }

    /**
     * To support cascading delete requests
     * 
     * @param parentHasNoMoreValidStates
     */
    void clear(
        boolean parentHasNoMoreValidStates
    ){
        if(parentHasNoMoreValidStates || isDateStateContainer()) {
            clear();
        }
    }

    private Object_1_0 get(
        String qualifier
    ) throws ServiceException {
        return this.parent.toViewValue(
            this.parent.objGetPath().getDescendant(
                this.feature, qualifier
            ), 
            isDateStateContainer(), false
        );
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
     */
    public Object put(
        Object key, 
        Object value
    ) {
        try {
            if(isPersistent()) {
                ((Object_1_0)value).objMove(this, (String)key);
            } else {
                if(this.transientObjects.containsKey(key)) {
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.DUPLICATE,
                        "Transient container contains already an object with the given qualifier",
                        new BasicException.Parameter("qualifer", key)
                    );
                }
                this.transientObjects.put(key, value);
            }
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#get(java.lang.Object)
     */
    public Object get(Object key) {
        if(isPersistent()) {
            try {
                Object_1_0 candidate = get((String)key);
                //
                // Validate candidate
                //
                if(isDateStateContainer()) {
                    ViewObject_1_0 object = (ViewObject_1_0) candidate; 
                    return object.exists() ? object : null;
                } else {
                    candidate.objGetClass(); // Assert Accessibility 
                    return candidate;
                }
            } catch (ServiceException exception) {
                if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                    //
                    // Object is inaccessible
                    //
                    return null;
                } else {
                    throw new RuntimeServiceException(exception);
                }
            }
        } else {
            return this.transientObjects.get(key);
        }
    }

    /* (non-Javadoc)
     * @see java.util.AbstractMap#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        Object_1_0 object = (Object_1_0) get(key);
        if(object != null) try {
            object.objRemove();
        } catch (ServiceException exception) {
            if(exception.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                throw new RuntimeServiceException(exception);
            }
        }
        return object == null ? NULL : object;
    }

    FilterProperty[] getStateFilter(){
        for(
                int i = 0;
                i < this.attributeFilter.length;
                i++
        ){
            if(VALIDITY_ATTRIBUTES.contains(this.attributeFilter[i].name())) {
                return this.attributeFilter;
            }
        }
        List stateFilter = new ArrayList(
            Arrays.asList(ViewContainer_1.this.attributeFilter)
        );
        DateStateContext context = marshaller.getContext();
        if(context.isWritable()) {
            stateFilter.add(
                new FilterProperty(
                    Quantors.FOR_ALL,
                    State_1_Attributes.INVALIDATED_AT,
                    FilterOperators.IS_IN
                )
            );
            if(context.getValidTo() != null) {
                stateFilter.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        State_1_Attributes.STATE_VALID_FROM,
                        FilterOperators.IS_LESS_OR_EQUAL,
                        DateStateContexts.toBasicFormat(context.getValidTo())
                    )
                );
            }
            if(context.getValidFrom() != null) {
                stateFilter.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        State_1_Attributes.STATE_VALID_TO,
                        FilterOperators.IS_GREATER_OR_EQUAL,
                        DateStateContexts.toBasicFormat(context.getValidFrom())
                    )
                );
            }
        } else {
            if(context.getValidFor() != null) {
                String validFor = DateStateContexts.toBasicFormat(context.getValidFor()); 
                stateFilter.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        State_1_Attributes.STATE_VALID_FROM,
                        FilterOperators.IS_LESS_OR_EQUAL,
                        validFor
                    )
                );
                stateFilter.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        State_1_Attributes.STATE_VALID_TO,
                        FilterOperators.IS_GREATER_OR_EQUAL,
                        validFor
                    )
                );
            }
            if(context.getValidAt() == null) {
                stateFilter.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        State_1_Attributes.INVALIDATED_AT,
                        FilterOperators.IS_IN
                    )
                );
            } else {
                String validAt = DateFormat.getInstance().format(context.getValidAt()); 
                stateFilter.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        SystemAttributes.CREATED_AT,
                        FilterOperators.IS_LESS_OR_EQUAL,
                        validAt
                    )
                );
                stateFilter.add(
                    new FilterProperty(
                        Quantors.FOR_ALL,
                        State_1_Attributes.INVALIDATED_AT,
                        FilterOperators.IS_GREATER,
                        validAt
                    )
                );
            }
        }
        return (FilterProperty[]) stateFilter.toArray(
            new FilterProperty[stateFilter.size()]
        );
    }

    AbstractFilter getFilter(
    ){
        if(this.filter == null) {
            this.filter = new ObjectFilter_1(
                this.marshaller.getModel(),
                this.dateStateRequest ? getStateFilter() : this.attributeFilter 
            );
        }
        return this.filter;
    }

    Map fetchTransientObjects (){
        Map transientObjects = this.transientObjects;
        this.transientObjects = null;
        return transientObjects;
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
                ": ("
            ).append(
                this.isPersistent() ? getPath().toXri() : "transient" 
            ).append(
                ", "
            ).append(
                this.attributeFilter.length
            ).append(
                " filter properties, requesting "
            ).append(
                this.dateStateRequest ? "states" : "objects"
            );
        } catch (Exception exception) {
            text.append(
                "// "
            ).append(
                exception.getClass().getName()
            );
            if(exception.getMessage() != null){
                text.append(
                    ": "
                ).append(
                    exception.getMessage()
                );
            }
        }
        return text.append(
            ')'
        ).toString();
    }


    //------------------------------------------------------------------------
    // Extends AbstractMap
    //------------------------------------------------------------------------

    /**
     * Persistent Date State Entries
     */
    class EntrySet extends AbstractSet {

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        public Iterator iterator(
        ) {
            return 
            !isPersistent() ? new TransientIterator() : 
                !isDateStateContainer() ? new NonDateStateViewIterator() :
                    !isDateStateRequest() ? new DateStateViewIterator() :
                        (Iterator) new StateIterator(); 
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        public int size() {
            int s = 0;
            for(
                    Iterator i = iterator();
                    i.hasNext();
            ){
                i.next();
                s++;
            }
            return s;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        public boolean isEmpty() {
            return !iterator().hasNext();
        }



    }

    /**
     * TransientIterator
     */
    private class TransientIterator implements Iterator {

        TransientIterator(
        ){
            this.delegate = ViewContainer_1.this.transientObjects.entrySet().iterator();
            this.filter = getFilter();
        }

        private final Iterator delegate ;

        private final AbstractFilter filter;

        private Map.Entry prefetched = null;

        private Map.Entry entry = null;


        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            while(
                    this.prefetched == null &&
                    this.delegate.hasNext()
            ){
                Map.Entry prefetched = (Map.Entry) this.delegate.next();
                Object_1_0 object = (Object_1_0) prefetched.getValue();
                if(this.filter.accept(object)) {
                    this.prefetched = prefetched;
                }
            }
            this.entry = null;
            return this.prefetched != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            } else {
                this.entry = this.prefetched;
                this.prefetched = null;
                return this.entry;
            }
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove(
        ) {
            if(this.entry == null) {
                throw new IllegalStateException(
                    "remove() on transient objects may be called after next() and before hasNext() only"
                );
            } else {
                Object_1_0 object = (Object_1_0) this.entry.getValue();
                try {
                    object.objRemove();
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
                this.entry = null;
            }
        }

    }

    /**
     * DateState View Iterator
     */
    private class DateStateViewIterator implements Iterator {

        DateStateViewIterator(
        ){
            this.filter = getFilter();
            this.beforeImage = getBeforeImage();
            this.delegate = getSinkObjects(
                this.beforeImage == null
            ).iterator();
        }

        private final Collection beforeImage;

        private final AbstractFilter filter;

        private final Iterator delegate ;

        private Object_1_0 prefetched = null;

        private Map.Entry entry = null;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            try {
                while(
                        this.prefetched == null &&
                        this.delegate.hasNext()
                ){
                    SinkObject_1 sinkObject = (SinkObject_1) this.delegate.next();
                    Path path = sinkObject.objGetPath();
                    boolean involved = isInvolved(
                        sinkObject,
                        this.beforeImage != null && this.beforeImage.contains(path)
                    );
                    if (involved)  {
                        Object_1_0 viewObject = parent.toViewValue(
                            path, 
                            true, // dateStateInstance
                            false
                        ); 
                        if(
                                this.filter.accept(viewObject)
                        ){
                            this.prefetched = viewObject;
                        }
                    }
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            return this.prefetched != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            } else try {
                this.entry = new ViewEntry(
                    this.prefetched.objGetPath().getBase(),
                    this.prefetched
                );
                this.prefetched = null;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            return this.entry;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove(
        ) {
            if(this.entry == null) {
                throw new IllegalStateException();
            } else try {
                ((Object_1_0)this.entry.getValue()).objRemove();
                this.entry = null;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

    }

    /**
     * Non-DateState View Iterator
     */
    private class NonDateStateViewIterator implements Iterator {

        /**
         * Constructor 
         */
        NonDateStateViewIterator(
        ){
            this.beforeImage = getBeforeImage();
            if(this.beforeImage == null) {
                this.delegate = getSinkContainer().subMap(ViewContainer_1.this.attributeFilter).values().iterator();
                this.filter = null;
            } else {
                this.delegate = getSinkObjects(false).iterator(); 
                this.filter = getFilter();
            }
        }

        private final AbstractFilter filter;

        private final Collection beforeImage;

        private final Iterator delegate;

        private Path prefetched = null;

        private Map.Entry entry = null;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            try {
                while(
                        this.prefetched == null &&
                        this.delegate.hasNext()
                ){
                    if(this.beforeImage == null){
                        Object_1_0 sinkDelegate = (Object_1_0) this.delegate.next();
                        if(!SinkObject_1.isInaccessable(sinkDelegate)) {
                            this.prefetched = sinkDelegate.objGetPath();
                        }
                    } else {
                        SinkObject_1 sinkObject = (SinkObject_1) this.delegate.next();
                        Object_1_0 sinkDelegate = sinkObject.getDelegate();
                        if(
                                (!sinkObject.isDirty() || !sinkDelegate.objIsDeleted()) &&
                                this.filter.accept(sinkDelegate)
                        ) {
                            this.prefetched = sinkObject.objGetPath();
                        }
                    }
                }
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            return this.prefetched != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            } else try {
                this.entry = new ViewEntry(
                    this.prefetched.getBase(),
                    parent.toViewValue(
                        this.prefetched, 
                        false, // dateStateInstance
                        this.beforeImage == null // initializeCacheWithDelegate
                    )
                );
                this.prefetched = null;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            return this.entry;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove(
        ) {
            if(this.entry == null) {
                throw new IllegalStateException();
            } else try {
                ((Object_1_0)this.entry.getValue()).objRemove();
                this.entry = null;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

    }

    /**
     * EntryIterator
     */
    private class StateIterator implements Iterator {

        StateIterator(
        ){
            this.sinkObjects = getSinkObjects(
                true // fetch
            ).iterator();
            this.filter = getFilter();
        }

        private final AbstractFilter filter;

        private final Iterator sinkObjects;

        private Iterator states = Collections.EMPTY_SET.iterator();

        SinkObject_1 current = null; 

        Object_1_0 prefetched = null;

        Map.Entry entry = null;

        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            while(
                    this.prefetched == null && 
                    (this.states.hasNext() || this.sinkObjects.hasNext()) 
            ){
                if(this.states.hasNext()) {
                    Object_1_0 object = (Object_1_0) this.states.next();
                    if(this.filter.accept(object)) {
                        this.prefetched = object;
                    }
                } else {
                    this.current = (SinkObject_1) this.sinkObjects.next();
                    try {
                        this.states = this.current.allStates(null, Boolean.FALSE).iterator();
                    } catch (ServiceException exception) {
                        throw new RuntimeServiceException(exception);
                    }
                }
            }
            return this.prefetched != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Object next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            } else try {
                this.entry = new ViewEntry(
                    this.prefetched.objGetPath().getBase(),
                    ViewContainer_1.this.marshaller.getSink().getViewConnection(
                        DateStateContexts.newDateStateContext(this.prefetched)
                    ).getObject(
                        this.current.objGetPath()
                    )
                );
                this.prefetched = null;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
            return this.entry;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove(
        ) {
            if(this.entry == null) {
                throw new IllegalStateException();
            } else try {
                ((Object_1_0)this.entry.getValue()).objRemove();
                this.entry = null;
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

    }

    /**
     * ViewEntry
     */
    private static class ViewEntry implements Map.Entry {

        ViewEntry(
            String key,
            Object_1_0 value
        ){
            this.key = key;
            this.value = value;
        }

        final private String key;

        final private Object_1_0 value;

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
            return this.value;
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        public Object setValue(Object value) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * ValueList
     */
    private class ValueList extends AbstractSequentialList {

        /**
         * Constructor 
         *
         * @param attributeSpecifiers
         */
        ValueList(
            AttributeSpecifier[] attributeSpecifiers
        ){
            this.comparator = order(attributeSpecifiers) ?
                new ViewComparator_1(attributeSpecifiers) :
                    null;
        }

        /**
         * 
         */
        private final Comparator comparator;

        /* (non-Javadoc)
         * @see java.util.AbstractSequentialList#listIterator(int)
         */
        public ListIterator listIterator(
            int index
        ) {
            List result = new ArrayList();
            result.addAll(values());
            if(this.comparator != null) {
                Collections.sort(
                    result, 
                    this.comparator
                );
            }
            return result.listIterator(index);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        public int size() {
            return ViewContainer_1.this.size();
        }

    }

}
/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateContainer_1.java,v 1.6 2009/01/17 02:37:21 hburger Exp $
 * Description: State Object Container
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/17 02:37:21 $
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
package org.openmdx.compatibility.state1.aop1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.kernel.exception.BasicException;

/**
 * State Object Container
 */
public class StateContainer_1 
    implements Serializable, Container_1_0, Delegating_1_0 
{

    /**
     * Constructor 
     *
     * @param parent
     * @param feature
     * @throws ServiceException
     */
    public StateContainer_1(
        ObjectView_1_0 parent,
        String feature
    ) throws ServiceException {
        this(
            parent,
            parent.objGetDelegate().objGetContainer(feature),
            ALL, 
            feature
        );
    }

    /**
     * Constructor 
     *
     * @param parent
     * @param original
     * @param feature
     */
    public StateContainer_1(
        ObjectView_1_0 parent,
        Container_1_0 original
    ){
        this(
            parent,
            original,
            ALL, 
            "extent"
        );
    }
    
    /**
     * Constructor 
     *
     * @param parent
     * @param original
     * @param feature 
     * @param criteria
     */
    private StateContainer_1(
        ObjectView_1_0 parent,
        FilterableMap<String, DataObject_1_0> original,
        FilterProperty[] filter, 
        String feature
    ){
        this.parent = parent;
        this.feature = feature;
        this.original = original;
        this.filter = filter;
        this.amendet = amend(filter) ? original.subMap(VALID_ONLY) : original;
    }
     
    private final String feature;
    
    private final ObjectView_1_0 parent;
    
    private final FilterProperty[] filter;
    
    private final FilterableMap<String,DataObject_1_0> original;

    private final FilterableMap<String,DataObject_1_0> amendet;
    
    public final FilterableMap<String,DataObject_1_0> objGetDelegate(){
        return this.amendet;
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 1312568818384216689L;

    private static final FilterProperty[] ALL = {};
    
    private static final FilterProperty[] VALID_ONLY = {
        new FilterProperty(
            Quantors.FOR_ALL,
            SystemAttributes.REMOVED_AT,
            FilterOperators.IS_IN
        )
    };
    
    private static FilterProperty[] concatenate(
        FilterProperty[] superFilter,
        FilterProperty[] refinement
        
    ){
        FilterProperty[] concatenated = new FilterProperty[
            superFilter.length + refinement.length
        ];
        System.arraycopy(superFilter, 0, concatenated, 0, superFilter.length);
        System.arraycopy(refinement, 0, concatenated, superFilter.length, refinement.length);
        return concatenated;
    }
    
    //------------------------------------------------------------------------
    // Implements FilterableMap
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Container_1_0#superSet()
     */
    public Container_1_0 superSet() {
        throw new UnsupportedOperationException("Operation not supported by StateContainer_1");
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Container_1_0#getObjectId()
     */
    public Object getContainerId() {
        Path parent = (Path) this.parent.jdoGetObjectId();
        return parent.getChild(feature);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
     */
    public FilterableMap<String, DataObject_1_0> subMap(Object criteria) {
        if(criteria instanceof FilterProperty[]){
            FilterProperty[] filter = (FilterProperty[]) criteria;
            return new StateContainer_1(
                this.parent,
                this.original.subMap(normalize(filter)),
                concatenate(this.filter, filter), feature
            );
        } else {
            return this.amendet.subMap(criteria);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
     */
    public List<DataObject_1_0> values(Object criteria) {
        return this.amendet.values(criteria);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        this.amendet.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return this.amendet.containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(
        Object value
    ) {
        return this.amendet.containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet() {
        return this.amendet.entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public DataObject_1_0 get(Object key) {
        return this.amendet.get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return this.amendet.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        return this.amendet.keySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public DataObject_1_0 put(String key, DataObject_1_0 value) {
        // TODO assertMovability
        return this.amendet.put(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends String, ? extends DataObject_1_0> t) {
        throw new UnsupportedOperationException();
    }

    private void assertModifiability(){
        // Do not delete states
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public DataObject_1_0 remove(Object key) {
        this.assertModifiability();
        DataObject_1_0 value = get(key);
        try {
            if(value.jdoIsPersistent()) {
                JDOHelper.getPersistenceManager(value).deletePersistent(value);
            } else {
                this.amendet.remove(value);
            }
        } 
        catch (Exception exception) {
            throw new RuntimeServiceException(exception);
        }
        return value;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return this.amendet.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<DataObject_1_0> values() {
        return this.amendet.values();
    }

    
    //------------------------------------------------------------------------
    // Filter Mapping
    //------------------------------------------------------------------------
    
    /**
     * Normalize the given filter
     * 
     * @param source the original filter
     * 
     * @param the normalized filter
     */
    public static FilterProperty[] normalize(
        FilterProperty[] source
    ){
        List<FilterProperty> target = new ArrayList<FilterProperty>();
        boolean touched = false;
        for(FilterProperty f : source) {
            String feature = f.name();
            if(State_1_Attributes.STATED_OBJECT.equals(feature)) {
                touched = true;
                if(
                    f.operator() != FilterOperators.IS_NOT_IN ||
                    !f.values().isEmpty()
                ){
                    throw new RuntimeServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_IMPLEMENTED,
                        "Filter property not yet handled properly",
                        new BasicException.Parameter("quantor", Quantors.toString(f.quantor())),
                        new BasicException.Parameter("name", f.name()),
                        new BasicException.Parameter("operator", FilterOperators.toString(f.operator())),
                        new BasicException.Parameter("values", f.values())
                    );
                }
            } else if (State_1_Attributes.CREATED_AT_ALIAS.equals(feature)) {
                target.add(
                    new FilterProperty(
                        f.quantor(),
                        SystemAttributes.CREATED_AT,
                        f.operator(),
                        f.getValues()
                    )
                );
            } else if (State_1_Attributes.REMOVED_AT_ALIAS.equals(feature)) {
                target.add(
                    new FilterProperty(
                        f.quantor(),
                        SystemAttributes.REMOVED_AT,
                        f.operator(),
                        f.getValues()
                    )
                );
            } else {
                target.add(f);
            }
        }
        return touched ? target.toArray(
            new FilterProperty[target.size()]
        ) : source;
    }
    
    /**
     * Test whether an amendment is required
     * 
     * @param the original filter
     * 
     * @return <code>true</code> if an amendment is reqired
     */
    public static boolean amend(
        FilterProperty[] original
    ){
        boolean compatibility = false; 
        boolean restricted = false; 
        for(FilterProperty f : original) {
            String name = f.name();
            if(State_1_Attributes.STATED_OBJECT.equals(name)) {
                compatibility = true;
            } else if (
                SystemAttributes.CREATED_AT.equals(name) ||
                SystemAttributes.REMOVED_AT.equals(name) ||
                "stateValidFrom".equals(name) ||
                "stateValidTo".equals(name) ||
                State_1_Attributes.REMOVED_AT_ALIAS.equals(name) || 
                State_1_Attributes.CREATED_AT_ALIAS.equals(name) 
            ){
                restricted = true;
            }
        }
        return compatibility && !restricted;
    }
        
    
    //-------------------------------------------------------------------------
    // Extends Object
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return this.amendet.toString();
    }

}

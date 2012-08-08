/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Extent_1.java,v 1.4 2009/02/10 22:31:08 hburger Exp $
 * Description: State Object Container
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/10 22:31:08 $
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
package org.openmdx.base.aop1;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;
import org.openmdx.compatibility.state1.aop1.StateContainer_1;
import org.openmdx.compatibility.state1.aop1.StatedObjectContainer_1;

/**
 * State Object Container
 */
public class Extent_1 
    implements Serializable, Container_1_0, Delegating_1_0 
{

    /**
     * Constructor 
     *
     * @param parent
     * @param container
     */
    public Extent_1(
        ObjectView_1_0 parent,
        Container_1_0 container
    ) throws ServiceException {
        this.parent = parent;
        this.container = container;
    }
        
    protected final ObjectView_1_0 parent;
    
    protected final Container_1_0 container;
    
    private transient Container_1_0 stateContainer;

    private transient Container_1_0 statedObjectContainer;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 5143844081800565000L;

    protected Model_1_6 getModel(){
        return this.parent.getModel();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#getContainerId()
     */
    public Object getContainerId() {
        return objGetDelegate().getContainerId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#superSet()
     */
    public Container_1_0 superSet() {
        return objGetDelegate();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
     */
    public FilterableMap<String, DataObject_1_0> subMap(Object filter) {
        if(filter instanceof FilterProperty[]){
            FilterProperty[] filterProperties =(FilterProperty[])filter;
            for(FilterProperty filterProperty : filterProperties){
                if(
                    Quantors.THERE_EXISTS == filterProperty.quantor() &&
                    SystemAttributes.OBJECT_INSTANCE_OF.equals(filterProperty.name()) &&
                    FilterOperators.IS_IN == filterProperty.operator() &&
                    filterProperty.values().size() == 1
                ) try {
                    if(getModel().isSubtypeOf(filterProperty.getValue(0), "org:openmdx:state2:BasicState")){
                        return getStateContainer(
                            State_1_Attributes.indexOfStatedObject(filterProperties) < 0
                        ).subMap(
                            filter
                        );
                    }
                } catch (ServiceException exception) {
                    throw new RuntimeServiceException(exception);
                }
            }
        }
        return objGetDelegate().subMap(filter);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
     */
    public List<DataObject_1_0> values(Object criteria) {
        return objGetDelegate().values(criteria);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return objGetDelegate().containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        if(value instanceof DataObject_1_0){
            DataObject_1_0 object = (DataObject_1_0) value;
            try {
                if(getModel().isInstanceof(object, "org:openmdx:base:ExtentCapable")){
                    Path candidate = (Path) object.jdoGetObjectId();
                    Path parent = (Path) this.parent.jdoGetObjectId();
                    return 
                        candidate != null &&
                        parent != null &&
                        candidate.startsWith(parent);
                }
            } catch (ServiceException ignored) {
                return false;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public DataObject_1_0 get(Object key) {
        return objGetDelegate().get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set<String> keySet() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public DataObject_1_0 put(String key, DataObject_1_0 value) {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map<? extends String, ? extends DataObject_1_0> m) {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public DataObject_1_0 remove(Object key) {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection<DataObject_1_0> values() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.Delegating_1_0#objGetDelegate()
     */
    public Container_1_0 objGetDelegate(
    ){
        return this.container;
    }
    
    public Container_1_0 getStateContainer(
        boolean object
    ) throws ServiceException{
        if(object) {
            if(this.statedObjectContainer == null) {
                this.statedObjectContainer = new StatedObjectContainer_1(
                    this.parent,
                    this.container
                );
            }
            return this.statedObjectContainer;
        } else {
            if(this.stateContainer == null) {
                this.stateContainer = new StateContainer_1(
                    this.parent,
                    this.container
                );
            }
            return this.stateContainer;
        }
    }

}

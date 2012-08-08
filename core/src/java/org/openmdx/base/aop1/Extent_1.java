/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Extent_1.java,v 1.26 2010/12/18 18:42:06 hburger Exp $
 * Description: Extent
 * Revision:    $Revision: 1.26 $
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
package org.openmdx.base.aop1;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.OrderSpecifier;

/**
 * org::openmdx::base aware extent
 */
public class Extent_1 
    implements Serializable, Container_1_0, Delegating_1_0<Container_1_0> 
{

    /**
     * Constructor 
     *
     * @param parent
     * @param container
     */
    protected Extent_1(
        ObjectView_1_0 parent,
        Container_1_0 container
    ) throws ServiceException {
        this.parent = parent;
        this.container = container;
    }
        
    protected final ObjectView_1_0 parent;
    
    protected final Container_1_0 container;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1452567179504985867L;

    protected Model_1_0 getModel(){
        return this.parent.getModel();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
//  @Override
    public PersistenceManager openmdxjdoGetPersistenceManager(){
    	return this.parent.jdoGetPersistenceManager();
    }
    
//  @Override
    public Path openmdxjdoGetContainerId() {
        return this.objGetDelegate().openmdxjdoGetContainerId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#getContainerId()
     */
//  @Override
    public TransientContainerId openmdxjdoGetTransientContainerId() {
        return this.objGetDelegate().openmdxjdoGetTransientContainerId();
    }
    
/* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
//  @Override
    public PersistenceManager openmdxjdoGetDataObjectManager() {
        return this.objGetDelegate().openmdxjdoGetDataObjectManager();
    }

//  @Override
    public boolean openmdxjdoIsPersistent() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#retrieve()
     */
//  @Override
    public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#superSet()
     */
//  @Override
    public Container_1_0 container() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#subMap(java.lang.Object)
     */
//  @Override
    public Container_1_0 subMap(Filter filter) {
        return this.objGetDelegate().subMap(filter);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.FilterableMap#values(java.lang.Object)
     */
//  @Override
    public List<DataObject_1_0> values(
        FetchPlan fetchPlan, 
        OrderSpecifier... criteria
    ) {
        return this.objGetDelegate().values(fetchPlan, criteria);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
//  @Override
    public void clear() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
//  @Override
    public boolean containsKey(Object key) {
        return this.objGetDelegate().containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
//  @Override
    public boolean containsValue(Object value) {
        if(value instanceof DataObject_1_0) try {
            DataObject_1_0 object = (DataObject_1_0) value;
            if(this.getModel().isInstanceof(object, "org:openmdx:base:ExtentCapable")){
                Path candidate = object.jdoGetObjectId();
                Path parent = this.parent.jdoGetObjectId();
                return 
                    candidate != null &&
                    parent != null &&
                    candidate.startsWith(parent);
            }
        } catch (ServiceException ignored) {
            return false;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
//  @Override
    public Set<java.util.Map.Entry<String, DataObject_1_0>> entrySet() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
//  @Override
    public DataObject_1_0 get(Object key) {
        return this.objGetDelegate().get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
//  @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
//  @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
//  @Override
    public DataObject_1_0 put(String key, DataObject_1_0 value) {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
//  @Override
    public void putAll(Map<? extends String, ? extends DataObject_1_0> m) {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
//  @Override
    public DataObject_1_0 remove(Object key) {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
//  @Override
    public int size() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
//  @Override
    public Collection<DataObject_1_0> values() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoEvict()
     */
//  @Override
    public void openmdxjdoEvict(
        boolean allMembers, boolean allSubSets
    ) {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#refreshAll()
     */
//  @Override
    public void openmdxjdoRefresh() {
        throw new UnsupportedOperationException("This operation must not be applied to an extent");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.Delegating_1_0#objGetDelegate()
     */
//  @Override
    public Container_1_0 objGetDelegate(
    ){
        return this.container;
    }
    
//  @Override
    public boolean isRetrieved() {
        return false;
    }

}

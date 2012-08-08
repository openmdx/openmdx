/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefContainer_1.java,v 1.55 2010/08/30 15:41:41 wfro Exp $
 * Description: RefContainer_1 class
 * Revision:    $Revision: 1.55 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/30 15:41:41 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.AnyTypePredicate;

/**
 * RefContainer_1
 */
public class RefContainer_1
    extends AbstractCollection<RefObject_1_0>
    implements Serializable, RefContainer<RefObject_1_0>, PersistenceCapableCollection
{

    /**
     * Constructor
     *
     * @param   the marshaller to be applied to the elements, filter and order
     *           objects.
     * @param   The delegate contains unmarshalled elements
     */
    public RefContainer_1(
        Marshaller marshaller,
        Container_1_0 container
    ) {
        this.marshaller = marshaller;
        this.container = container;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 1211709415207799992L;

    private Marshaller marshaller;

    private Container_1_0 container;
    
    //-------------------------------------------------------------------------
    public Container_1_0 refDelegate(
    ) {
        return this.container;
    }
    
    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
     */
//  @Override
    public RefPackage refImmediatePackage() {
        throw new UnsupportedOperationException(
            "refImmediatePackage() is not supported, while refOutermostPackage() is"
        );
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
     */
//  @Override
    public RefObject refMetaObject(
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefContainer_1");
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMofId()
     */
//  @Override
    public String refMofId(
    ) {
        Path containerId = this.openmdxjdoGetContainerId();
        return containerId == null ? null : containerId.toXRI();
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
     */
//  @Override
    public Collection<?> refVerifyConstraints(
        boolean deepVerify
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefContainer_1");
    }

    //  -------------------------------------------------------------------------
//  @Override
    public RefPackage refOutermostPackage(
    ) {
        return (RefPackage) this.marshaller;
    }

    /**
     * 
     * @param qualifier
     * @param value
     */
    private void add(
        String qualifier,
        RefObject_1_0 value
    ) {
        try {
            ObjectView_1_0 objectView = (ObjectView_1_0) this.marshaller.unmarshal(value);
            objectView.objMove(
                this.container,
                qualifier
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<RefObject_1_0> iterator() {
        return new MarshallingSet<RefObject_1_0>(
                this.marshaller,
                this.container.values()
        ).iterator();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return this.container.size();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
        this.container.clear();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return this.container.isEmpty();
    }

    /**
     * Retrieve a member
     * 
     * @param filter
     * 
     * @return the requested member
     */
    private RefObject_1_0 get(String filter) {
        try {
            return (RefObject_1_0) this.marshaller.marshal(
                this.container.get(filter)
            );
        } catch (ServiceException exception){
            if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                return null;
            } else {
                throw new RuntimeServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TRANSFORMATION_FAILURE,
                    "Marshal failure"
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(RefObject_1_0 o) {
        this.add(null, o);
        return true;
    }


    //------------------------------------------------------------------------
    // Implements Container
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Container#openmdxjdoGetContainerId()
     */
//  @Override
    public Path openmdxjdoGetContainerId() {
        return this.container.openmdxjdoGetContainerId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Container#openmdxjdoGetTransientContainerId()
     */
//  @Override
    public TransientContainerId openmdxjdoGetTransientContainerId() {
        return this.container.openmdxjdoGetTransientContainerId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
//  @Override
    public PersistenceManager openmdxjdoGetPersistenceManager() {
        return this.container.openmdxjdoGetPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoIsPersistent()
     */
//  @Override
    public boolean openmdxjdoIsPersistent() {
        return this.container.openmdxjdoIsPersistent();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoEvict()
     */
//  @Override
    public void openmdxjdoEvict(
        boolean allMembers, boolean allSubSets
    ) {
        this.container.openmdxjdoEvict(allMembers, allSubSets);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoRefreshAll()
     */
//  @Override
    public void openmdxjdoRefresh() {
        this.container.openmdxjdoRefresh();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoRetrieve(javax.jdo.FetchPlan)
     */
//  @Override
    public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
        this.container.openmdxjdoRetrieve(fetchPlan);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#getAll(org.w3c.cci2.AnyTypePredicate)
     */
//  @Override
    public List<RefObject_1_0> getAll(AnyTypePredicate predicate) {
        return this.refGetAll(predicate);
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.Container#removeAll(org.w3c.cci2.AnyTypePredicate)
     */
    @Override
    public void removeAll(AnyTypePredicate predicate) {
        this.refRemoveAll(predicate);
    }

    
    //------------------------------------------------------------------------
    // Implements RefContainer
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refAdd(java.lang.Object[])
     */
//  @Override
    public void refAdd(Object... arguments) {
        int objectIndex = arguments.length - 1;
        add(
            RefContainer_1.toQualifier(objectIndex, arguments),
            (RefObject_1_0) arguments[objectIndex]
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGet(java.lang.Object[])
     */
//  @Override
    public RefObject_1_0 refGet(Object... arguments) {
        return get(
            RefContainer_1.toQualifier(arguments.length, arguments)
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGetAll(java.lang.Object)
     */
//  @Override
    public List<RefObject_1_0> refGetAll(Object query) {
        Container_1_0 source = this.container;
        OrderSpecifier[] order = null;
        if(query instanceof Object[]) {
            Object[] args = (Object[]) query;
            if(args.length == 1 && args[0] instanceof RefQuery_1_0) {
                query = args[0];
            }
        }
        if(query instanceof Condition[]) {
            source = this.container.subMap(
                new Filter(
                    Arrays.asList((Condition[])query),
                    null,
                    null
                )
            );
            order = null;            
        } else if(query instanceof OrderSpecifier[]) {
            order = (OrderSpecifier[]) query;
        } else if(query instanceof Filter) {
            Filter filter = (Filter)query;
            List<OrderSpecifier> orderSpecifier = filter.getOrderSpecifier();
            source = this.container.subMap(filter);
            order = orderSpecifier.toArray(new OrderSpecifier[orderSpecifier.size()]);
        } else if(query instanceof RefQuery_1_0) {
            Filter filter = ((RefQuery_1_0)query).refGetFilter();
            List<OrderSpecifier> orderSpecifier = filter.getOrderSpecifier();
            source = this.container.subMap(filter);
            order = orderSpecifier.toArray(new OrderSpecifier[orderSpecifier.size()]);
        }
        return new MarshallingSequentialList<RefObject_1_0>(
            this.marshaller, 
            source.values(order)
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refRemove(java.lang.Object[])
     */
//  @Override
    public void refRemove(Object... arguments) {
        RefObject_1_0 object = this.get(RefContainer_1.toQualifier(arguments.length, arguments));
        if(object != null) {
            object.refDelete();
        }
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refRemoveAll(java.lang.Object)
     */
//  @Override
    public long refRemoveAll(Object query) {
        long removed = 0;
        for(RefObject_1_0 refObject : this.refGetAll(query)) {
            refObject.refDelete();
            removed++;
        }
        return removed;
    }

    /**
     * Create a qualifier from its sub-segment specification array
     * 
     * @param size
     * @param arguments
     * 
     * @return the corresponding qualifier
     */
    static String toQualifier(
        int size,
        Object[] arguments
    ){
        switch(size) {
            case 0: 
                return null;
            case 1: 
                return String.valueOf(arguments[0]);
            default: {
                if(size % 2 == 1) throw new IllegalArgumentException(
                    "The ref-method was invoked with an odd number of arguments greater than one: " + arguments.length
                );
                StringBuilder qualifier = new StringBuilder(
                    arguments[0] == PERSISTENT ? "!" : ""
                ).append(
                    arguments[1] == null ?
                        PathComponent.createPlaceHolder().toString() :
                            String.valueOf(arguments[1])
                );
                for(
                    int i = 2;
                    i < size;
                    i++
                ){
                    qualifier.append(
                        arguments[i] == PERSISTENT ? '!' : '*'
                    ).append(
                        arguments[++i]
                    );
                }
                return qualifier.toString();
            }
        }        
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractCollection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        String id = this.refMofId();
        return this.getClass().getName() + ": " + (id == null ? "transient" : id);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO test for manager, container id and filter
        return super.equals(obj);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // TODO mingle persistence manager id, container id and filter
        return super.hashCode();
    }

}

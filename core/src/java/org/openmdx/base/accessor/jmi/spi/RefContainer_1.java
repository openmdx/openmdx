/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefContainer_1.java,v 1.48 2010/01/26 15:38:36 hburger Exp $
 * Description: RefContainer_1 class
 * Revision:    $Revision: 1.48 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/26 15:38:36 $
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.spi.Container;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.kernel.exception.BasicException;

/**
 * RefContainer_1
 */
public class RefContainer_1
    extends AbstractCollection<RefObject_1_0>
    implements Serializable, RefContainer, Container
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 1211709415207799992L;

    private Marshaller marshaller;

    private Container_1_0 container;

    //-------------------------------------------------------------------------
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

    //-------------------------------------------------------------------------
    public Container_1_0 refDelegate(
    ) {
        return this.container;
    }
    
    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
     */
    public RefPackage refImmediatePackage() {
        throw new UnsupportedOperationException(
            "refImmediatePackage() is not supported, while refOutermostPackage() is"
        );
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
     */
    public RefObject refMetaObject(
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefContainer_1");
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refMofId()
     */
    public String refMofId(
    ) {
        Path containerId = openmdxjdoGetContainerId();
        return containerId == null ? null : containerId.toXRI();
    }

    /* (non-Javadoc)
     * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
     */
    public Collection<?> refVerifyConstraints(
        boolean deepVerify
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefContainer_1");
    }

    //  -------------------------------------------------------------------------
    public RefPackage refOutermostPackage(
    ) {
        return (RefPackage) this.marshaller;
    }

    //-------------------------------------------------------------------------
    public void refAddValue(
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
    public Iterator<RefObject_1_0> iterator() {
        return new MarshallingSet<RefObject_1_0>(
                this.marshaller,
                this.container.values()
        ).iterator();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
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

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.collection.Container#get(java.lang.Object)
     */
    public RefObject_1_0 get(Object filter) {
        if(filter instanceof String) {
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
        } else {
            List<RefObject_1_0> selection = refGetAll(filter);
            switch(selection.size()){
                case 0: return null;
                case 1: return selection.get(0);
                default: throw BasicException.initHolder( 
                    new IllegalArgumentException(
                        "More than one object matches the give criteria",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CARDINALITY,
                            new BasicException.Parameter("expected", 0, 1),
                            new BasicException.Parameter("actual", selection.size())
                        )
                    )
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(RefObject_1_0 o) {
        this.refAddValue(null, o);
        return true;
    }


    //------------------------------------------------------------------------
    // Implements Container
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Container#openmdxjdoGetContainerId()
     */
    public Path openmdxjdoGetContainerId() {
        return PersistenceHelper.getContainerId(this.container);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Container#openmdxjdoGetTransientContainerId()
     */
    public TransientContainerId openmdxjdoGetTransientContainerId() {
        return PersistenceHelper.getTransientContainerId(this.container);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Container#openmdxjdoIsPersistent()
     */
    public boolean openmdxjdoIsPersistent() {
        return PersistenceHelper.isPersistent(this.container);
    }

    
    //------------------------------------------------------------------------
    // Implements RefContainer
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refAdd(java.lang.Object[])
     */
    public void refAdd(Object... arguments) {
        int objectIndex = arguments.length - 1;
        refAddValue(
            RefContainer_1.toQualifier(objectIndex, arguments),
            (RefObject_1_0) arguments[objectIndex]
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGet(java.lang.Object[])
     */
    public Object refGet(Object... arguments) {
        return get(
            RefContainer_1.toQualifier(arguments.length, arguments)
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refGetAll(java.lang.Object)
     */
    public List<RefObject_1_0> refGetAll(Object query) {
        Container_1_0 source = this.container;
        if(query instanceof Object[]) {
            Object[] args = (Object[]) query;
            if(args.length == 1 && args[0] instanceof RefQuery_1_0) {
                query = args[0];
            }
        }
        if(query instanceof FilterProperty[]) {
            source = this.container.subMap(query);
            query = null;            
        } else if(query instanceof AttributeSpecifier[]) {
            source = this.container.subMap(null);
        } else if(
            (query instanceof Object[]) && 
            (((Object[])query).length == 2) &&
            (((Object[])query)[0] instanceof FilterProperty[]) && 
            (((Object[])query)[1] instanceof AttributeSpecifier[]) 
        ) {
            source = this.container.subMap(((Object[])query)[0]);
            query = ((Object[])query)[1];
        } else if(
            (query instanceof RefQuery_1_0) ||
            (query instanceof Filter)
        ) {
            Filter filter = query instanceof RefQuery_1_0 ?
                ((RefQuery_1_0)query).refGetFilter() :
                    (Filter)query;
            FilterProperty[] conditions = new FilterProperty[filter.getCondition().length];
            for(int i = 0; i < conditions.length; i++) {
                Condition condition = filter.getCondition()[i];
                conditions[i] = new FilterProperty(
                    condition.getQuantor(),
                    condition.getFeature(),
                    FilterOperators.fromString(condition.getName()),
                    condition.getValue()
                );
            }
            source = this.container.subMap(conditions);
            AttributeSpecifier[] orderSpecifiers = new AttributeSpecifier[filter.getOrderSpecifier().length];
            for(int i = 0; i < orderSpecifiers.length; i++) {
                OrderSpecifier specifier = filter.getOrderSpecifier()[i];
                orderSpecifiers[i] = new AttributeSpecifier(
                    specifier.getFeature(),
                    0,
                    specifier.getOrder()
                );
            }
            query = orderSpecifiers;
        }
        return new MarshallingSequentialList<RefObject_1_0>(
            this.marshaller, 
            source.values(query)
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refRemove(java.lang.Object[])
     */
    public void refRemove(Object... arguments) {
        RefObject_1_0 object = get(RefContainer_1.toQualifier(arguments.length, arguments));
        if(object != null) {
            object.refDelete();
        }
    }

    /* (non-Javadoc)
     * @see org.oasisopen.jmi1.RefContainer#refRemoveAll(java.lang.Object)
     */
    public long refRemoveAll(Object query) {
        long removed = 0;
        for(RefObject_1_0 refObject : refGetAll(query)) {
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
            case 0: return null;
            case 1: return String.valueOf(arguments[0]);
            case 2: if(arguments[0] == REASSIGNABLE) {
                return String.valueOf(arguments[1]);
            } // else fall through
            default:
                if(size % 2 == 1) throw new IllegalArgumentException(
                    "The ref-method was invoked with an odd number of arguments greater than one: " + arguments.length
                );
            StringBuilder qualifier = new StringBuilder(
                arguments[0] == PERSISTENT ? "!" : ""
            ).append(
                arguments[1]
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

    
    //------------------------------------------------------------------------
    // Extends AbstractCollection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        String id = this.refMofId();
        return getClass().getName() + ": " + (id == null ? "transient" : id);
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

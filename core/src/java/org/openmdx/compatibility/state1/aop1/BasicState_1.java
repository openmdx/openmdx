/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BasicState_1.java,v 1.18 2009/05/29 17:04:10 hburger Exp $
 * Description: Compatibility State
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/29 17:04:10 $
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


import static org.openmdx.base.accessor.cci.SystemAttributes.CONTEXT_CAPABLE_CONTEXT;
import static org.openmdx.base.accessor.cci.SystemAttributes.CREATED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_BY;
import static org.openmdx.base.accessor.cci.SystemAttributes.VERSION;
import static org.openmdx.base.aop1.State_1_Attributes.CREATED_AT_ALIAS;
import static org.openmdx.base.aop1.State_1_Attributes.REMOVED_AT_ALIAS;
import static org.openmdx.base.aop1.State_1_Attributes.REMOVED_BY_ALIAS;
import static org.openmdx.base.aop1.State_1_Attributes.STATED_OBJECT;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jmi.reflect.RefBaseObject;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.accessor.view.PlugIn_1;
import org.openmdx.base.aop1.ContextCapable_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.kernel.exception.BasicException;

/**
 * org::openmdx::compatibility:state1::BasicState Aspect
 */
public class BasicState_1 extends org.openmdx.base.aop1.Aspect_1 {
    
    /**
     * Constructor  
     *
     * @param self
     * @param next
     * 
     * @throws ServiceException
     */
    public BasicState_1(
        ObjectView_1_0 self, 
        PlugIn_1 next
    ) throws ServiceException {
        super(self, next);
    }

    /**
     * Try to avoid multi-value round-trips
     */
    private transient Set<Object> removedBy;
    
    /**
     * Create a criteria dependent place holder
     * 
     * @param criteria
     * 
     * @return a newly created place holder
     */
    protected String newPlaceHolder(
        String criteria
    ){
        return PathComponent.createPlaceHolder().add(1, criteria).toString();
    }

    @Override
    protected DataObject_1_0 getCore() throws ServiceException{
        return self.objGetDelegate();
    }

    /**
     * Validate the core value to be set
     * 
     * @param newValue
     * 
     * @throws ServiceException
     */
    @Override
    protected void validateCore(
        DataObject_1_0 newValue
    ) throws ServiceException{
        DataObject_1_0 oldValue = getCore(false);
        if(
            oldValue != null &&
            !StateCapables.isCoreObject(oldValue.jdoGetObjectId()) &&
            getModel().isInstanceof(oldValue, "org:openmdx:compatibility:state1:StateCapable")
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An aspect's core can't be replaced",
                new BasicException.Parameter("class", newValue.objGetClass())
            );
        }
        if(
            newValue != null && 
            !getModel().isInstanceof(newValue, "org:openmdx:compatibility:state1:StateCapable")
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The core object must be an instance of AspectCapable",
                new BasicException.Parameter("class", newValue.objGetClass())
            );
        }
    }    
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.Aspect_1#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void objMove(
        Container_1_0 there, 
        String criteria
    ) throws ServiceException {
        if(there == null || criteria == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "objMove's arguments must not be null",
            new BasicException.Parameter("there", there == null ? "<null>" : ((RefBaseObject)there).refMofId()),
            new BasicException.Parameter("criteria", criteria == null ? "<null>" : criteria)
        );
        try {
            DataObjectManager_1_0 persistenceManager = (DataObjectManager_1_0)JDOHelper.getPersistenceManager(
                super.self.objGetDelegate()
            );
            Container_1_0 container = ((Delegating_1_0<Container_1_0>)there).objGetDelegate().superSet(); 
            Path containerId = (Path) container.getContainerId();
            DataObject_1_0 coreObject = null;            
            boolean validTimeUnique = false;
            boolean transactionTimeUnique = false;
            if(containerId != null) {
                boolean keep = StateCapables.isTransientObject(containerId.getParent()); 
                coreObject = keep ? super.getCore() : (DataObject_1_0) persistenceManager.getObjectById(
                    StateCapables.getStateCapable(
                        StateCapables.getStateCapable(
                            containerId.getChild(criteria)
                        )
                    )
                );
                if(coreObject != null) {
                    validTimeUnique = (Boolean)coreObject.objGetValue("validTimeUnique");
                    transactionTimeUnique = (Boolean)coreObject.objGetValue("transactionTimeUnique");
                }
                if(validTimeUnique) {
                    if(!keep){
                        super.setCore(coreObject);
                    }
                    super.objMove(container, criteria);
                    return;
                }
            }
            coreObject = container.isEmpty() ? null : container.get(criteria);
            if(coreObject != null) try {
                coreObject.objGetClass();
            } catch (ServiceException exception) {
                if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) {
                    coreObject = null;
                } else {
                    throw exception;
                }
            }
            if(coreObject == null) {
                coreObject = persistenceManager.newInstance(
                    "org:openmdx:compatibility:state1:StateCapable"
                );
                coreObject.objSetValue(
                    VERSION, 
                    Integer.valueOf(0)
                );
                coreObject.objSetValue(
                    "transactionTimeUnique",
                    Boolean.valueOf(transactionTimeUnique)
                );
                coreObject.objSetValue(
                    "validTimeUnique",
                    Boolean.valueOf(validTimeUnique)
                );
                coreObject.objMove(
                    container, 
                    criteria
                );
            }
            super.setCore(coreObject);
        } catch (RuntimeException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Adding the object to the given collection failed",
                new BasicException.Parameter("referenceId", there == null ? null : there.getContainerId()),
                new BasicException.Parameter("criteria", criteria)
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.Aspect_1#getCoreClass()
     */
    @Override
    protected String getCoreClass(
    ) throws ServiceException {
        return "org:openmdx:compatibility:state1:StateCapable";
    }
    
    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
     */
    @Override
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        if(CONTEXT_CAPABLE_CONTEXT.equals(feature)) {
            DataObject_1_0 delegate = getDelegate();
            if(delegate instanceof ContextCapable_1) {
                return delegate.objGetContainer(feature);
            }
        }
        DataObject_1_0 dataObject = super.self.objGetDelegate();
        return (
            dataObject.jdoIsPersistent() ? 
                (DataObject_1_0)JDOHelper.getPersistenceManager(dataObject).getObjectById(super.self.jdoGetObjectId()) : 
                dataObject
            ).objGetContainer(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return "underlyingState".equals(feature) ? null : super.objGetValue(
            STATED_OBJECT.equals(feature) ? "core" :
            REMOVED_AT_ALIAS.equals(feature) ? REMOVED_AT : 
            CREATED_AT_ALIAS.equals(feature) ? CREATED_AT :
            feature
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.Aspect_1#objGetSet(java.lang.String)
     */
    @Override
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return REMOVED_BY.equals(feature) || REMOVED_BY_ALIAS.equals(feature) ? (
            this.removedBy == null ? this.removedBy = new RemovedBy() : this.removedBy
        ) : super.objGetSet(
            feature
        );
    }

    //------------------------------------------------------------------------
    // Class RemovedBy
    //------------------------------------------------------------------------
    
    /**
     * Optimizing Removed By Implementation
     */
    class RemovedBy extends AbstractSet<Object> {

        /**
         * 
         */
        private transient Set<Object> delegate;

        /**
         * 
         * @return
         */
        @SuppressWarnings("synthetic-access")
        private Set<Object> getDelegate(){
            try {
                return 
                    BasicState_1.super.objGetValue(REMOVED_AT) == null ? Collections.emptySet() :
                    this.delegate == null ? this.delegate = BasicState_1.super.objGetSet(REMOVED_BY) :
                    this.delegate;    
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }
        
        /* (non-Javadoc)
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<Object> iterator() {
            return getDelegate().iterator();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size() {
            return getDelegate().size();
        }
        
    }

}
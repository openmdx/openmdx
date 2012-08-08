/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1.java,v 1.10 2010/07/01 15:57:11 hburger Exp $
 * Description: Unit Of Work Interceptor
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/07/01 15:57:11 $
 * ====================================================================
 *
 * This software is published under the BSD license Unit Of Work Interceptoras listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

package org.openmdx.compatibility.audit1.aop1;

import java.util.HashMap;
import java.util.List;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.spi.ManagedConnectionCache_2_0;
import org.openmdx.base.accessor.rest.spi.ManagedConnectionCache_2_0.Mode;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.SharedObjects;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.rest.spi.Object_2Facade;

/** 
 * Unit Of Work Interceptor
 */
public class UnitOfWork_1 extends org.openmdx.audit2.aop1.UnitOfWork_1 {

    /**
     * Constructor 
     *
     * @param self
     * @param next
     */
    public UnitOfWork_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) {
        super(self, next);
    }

    /**
     * Involvement is derived
     */
    private transient Container_1_0 involvement = null;
    
    /**
     * Retrieve the involvement.
     * 
     * @return the involvement
     * 
     * @throws ServiceException 
     */
    private Container_1_0 getInvolvement(
    ) throws ServiceException {
        if(this.involvement == null) try {
            Container_1_0 involvement = new UnitOfWorkInvolvesObject();
            List<?> delegates = self.objGetDelegate().objGetList("involved");
            for(Object delegate : delegates) {
                Path viewId = (Path) JDOHelper.getObjectId(delegate);
                Path objectId = viewId.getPrefix(viewId.size() - 2);
                Path involvementId = self.jdoGetObjectId().getDescendant("involvement", objectId.toString());
                SharedObjects.getPlugInObject(self.jdoGetPersistenceManager(), ManagedConnectionCache_2_0.class).put(
                    Mode.BASIC,
                    Object_2Facade.newInstance(involvementId, "org:openmdx:audit2:Involvement").getDelegate()
                );
                involvement.put(
                    involvementId.getBase(),
                    (DataObject_1_0) self.jdoGetPersistenceManager().getObjectById(involvementId)
                );
            }
            if(this.involvement == null) {
                this.involvement = involvement;
            }
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
        return this.involvement;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objGetContainer(java.lang.String)
     */
    @Override
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        return "involvement".equals(feature) ? this.getInvolvement() : super.objGetContainer(feature);
    }
    
    
    //------------------------------------------------------------------------
    // Class UnitOfWorkInvolvesObject
    //------------------------------------------------------------------------
    
    /**
     * Unit Of Work Involves Object
     */
    class UnitOfWorkInvolvesObject extends HashMap<String,DataObject_1_0> implements Container_1_0 {

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = -6572392280693133245L;
        
        private final TransientContainerId transientContainerId = new TransientContainerId(
            UnitOfWork_1.this.jdoGetTransactionalObjectId(),
            "involvement"
        );

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#container()
         */
    //  @Override
        public Container_1_0 container() {
            return this;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#isRetrieved()
         */
    //  @Override
        public boolean isRetrieved() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#isPersistent()
         */
        @Override
        public boolean openmdxjdoIsPersistent() {
            return UnitOfWork_1.this.jdoIsPersistent();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetContainerId()
         */
        @Override
        public Path openmdxjdoGetContainerId() {
            Path xri = UnitOfWork_1.this.jdoGetObjectId();
            return xri == null ? null : xri.getChild("involvement");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetTransientContainerId()
         */
        @Override
        public TransientContainerId openmdxjdoGetTransientContainerId() {
            return this.transientContainerId;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#jdoGetPersistenceManager()
         */
        @Override
        public PersistenceManager openmdxjdoGetPersistenceManager() {
            return UnitOfWork_1.this.jdoGetPersistenceManager();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoEvict()
         */
        @Override
        public void openmdxjdoEvict(boolean allMembers, boolean allSubSets) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#refreshAll()
         */
    //  @Override
        public void openmdxjdoRefresh() {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#retrieveAll(javax.jdo.FetchPlan)
         */
    //  @Override
        public void openmdxjdoRetrieve(
            FetchPlan fetchPlan
        ) {
            // nothing to do
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#subMap(java.lang.Object)
         */
    //  @Override
        public Container_1_0 subMap(
            Filter filter
        ) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#values(java.lang.Object)
         */
        public List<DataObject_1_0> values(
            OrderSpecifier... criteria
        ) {
            throw new UnsupportedOperationException();
        }

    }
    
}

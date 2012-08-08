/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: UnitOfWork_1.java,v 1.3 2009/12/09 17:17:05 hburger Exp $
 * Description: Unit Of Work Interceptor
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/12/09 17:17:05 $
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
import javax.resource.ResourceException;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.spi.VirtualObjects_2_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.SharedObjects;

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
        if(this.involvement == null){
            Container_1_0 involvement = new UnitOfWorkInvolvesObject();
            List<?> delegates = self.objGetDelegate().objGetList("involved");
            for(Object delegate : delegates) {
                Path viewId = (Path) JDOHelper.getObjectId(delegate);
                Path objectId = viewId.getPrefix(viewId.size() - 2);
                Path involvementId = self.jdoGetObjectId().getDescendant("involvement", objectId.toString());
                try {
                    SharedObjects.getPlugInObject(self.jdoGetPersistenceManager(), VirtualObjects_2_0.class).putSeed(
                        involvementId,
                        "org:openmdx:audit2:Involvement"
                    );
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
                involvement.put(
                    involvementId.getBase(),
                    (DataObject_1_0) self.jdoGetPersistenceManager().getObjectById(involvementId)
                );
            }
            if(this.involvement == null) {
                this.involvement = involvement;
            }
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
        return "involvement".equals(feature) ? getInvolvement() : super.objGetContainer(feature);
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

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#container()
         */
        public Container_1_0 container() {
            return this;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#isRetrieved()
         */
        public boolean isRetrieved() {
            return true;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#refreshAll()
         */
        public void refreshAll() {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#retrieveAll(javax.jdo.FetchPlan)
         */
        public void retrieveAll(FetchPlan fetchPlan) {
            // nothing to do
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#subMap(java.lang.Object)
         */
        public Container_1_0 subMap(Object filter) {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.cci.Container_1_0#values(java.lang.Object)
         */
        public List<DataObject_1_0> values(Object criteria) {
            throw new UnsupportedOperationException();
        }

    }
    
}

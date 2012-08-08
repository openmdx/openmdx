/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Removable_1.java,v 1.7 2009/11/04 15:59:44 hburger Exp $
 * Description: Removable_1 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/04 15:59:44 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.aop1;

import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_AT;
import static org.openmdx.base.accessor.cci.SystemAttributes.REMOVED_BY;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Removable
 */
public class Removable_1 extends Interceptor_1 {

    /**
     * Constructor 
     *
     * @param self
     * @param next
     */
    public Removable_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) {
        super(self, next);
    }

    /**
     * The future place holder has actually the value <code>10000-01-01T00:00:00.000Z</code>.
     */
    public static final Date IN_THE_FUTURE = new Date(253402300800000l);

    /**
     * The cached <code<removedBy</code> value
     */
    private transient Set<Object> removedBy;    

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.view.PlugIn_1#objDelete()
     */
    @Override
    public void objDelete(
    ) throws ServiceException {
        if(super.jdoIsNew()){
            super.objDelete();
        } else {
            super.objSetValue(
                REMOVED_AT,
                IN_THE_FUTURE
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objSetValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException {
        if(REMOVED_AT.equals(feature) || REMOVED_BY.equals(feature)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "A Removables's removal attributes are not modifiable",
                new BasicException.Parameter("id", jdoGetObjectId()),
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("to", to)
            );
        }
        super.objSetValue(feature, to);
    } 

    @Override
    public boolean objIsRemoved(
    ) throws ServiceException { 
        DataObject_1_0 dataObject = this.self.objGetDelegate();
        return !dataObject.jdoIsDeleted() && IN_THE_FUTURE.equals(
            dataObject.objGetValue(SystemAttributes.REMOVED_AT)
        );   
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.Aspect_1#objGetSet(java.lang.String)
     */
    @Override
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return REMOVED_BY.equals(feature) ? (
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
         * The data object's <code>removedBy</code> set
         */
        private transient Set<Object> delegate;

        /**
         * 
         * @return
         */
        @SuppressWarnings("synthetic-access")
        private Set<Object> getDelegate(
        ){
            try {
                return 
                    Removable_1.super.objGetValue(REMOVED_AT) == null ? Collections.emptySet() :
                    this.delegate == null ? this.delegate = Removable_1.super.objGetSet(REMOVED_BY) :
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

        /* (non-Javadoc)
         * @see java.util.AbstractCollection#isEmpty()
         */
        @Override
        public boolean isEmpty() {
            return getDelegate().isEmpty();
        }

    }

}

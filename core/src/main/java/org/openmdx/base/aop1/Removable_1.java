/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Removable_1 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2011, OMEX AG, Switzerland
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

import java.util.AbstractList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
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
    private transient List<Object> removedBy;    

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

    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.Aspect_1#objGetList(java.lang.String)
     */
    @Override
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        return REMOVED_BY.equals(feature) ? (
            this.removedBy == null ? this.removedBy = new RemovedBy() : this.removedBy
        ) : super.objGetList(
            feature
        );
    }


    //------------------------------------------------------------------------
    // Class RemovedBy
    //------------------------------------------------------------------------

    /**
     * Optimizing Removed By Implementation
     */
    class RemovedBy extends AbstractList<Object> {

        /**
         * The data object's <code>removedBy</code> set
         */
        private transient List<Object> delegate;

        private List<Object> getDelegate(
        ){
            try {
                objGetValue(REMOVED_AT);
                return 
                    Removable_1.super.objGetValue(REMOVED_AT) == null ? Collections.emptyList() :
                    this.delegate == null ? this.delegate = Removable_1.super.objGetList(REMOVED_BY) :
                    this.delegate;    
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
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

        /* (non-Javadoc)
         * @see java.util.AbstractList#get(int)
         */
        @Override
        public Object get(int index) {
            return getDelegate().get(index);
        }

    }

}

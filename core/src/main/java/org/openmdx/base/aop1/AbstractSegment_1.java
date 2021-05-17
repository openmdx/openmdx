/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Removable_1 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;

/**
 * Segment
 */
public abstract class AbstractSegment_1 extends Interceptor_1 {

    /**
     * Constructor 
     *
     * @param self
     * @param next
     */
    protected AbstractSegment_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) {
        super(self, next);
    }

    /**
     * Keep an aspect aware extent implementation
     */
    private transient Container_1_0 extent = null;

    /**
     * Create an aspect aware extent implementation
     *  
     * @param parent
     * @param container
     * 
     * @return a new aspect aware extent implementation
     * @throws ServiceException
     */
    protected abstract Container_1_0 newExtent(
        ObjectView_1_0 parent,
        Container_1_0 container
    ) throws ServiceException;
    
    /**
     * Retrieve the org::openmdx::base aware extent implementation
     * 
     * @return the org::openmdx::base aware extent implementation
     * 
     * @throws ServiceException 
     */
    private Container_1_0 getExtent() throws ServiceException{
        if(this.extent == null) {
            this.extent = newExtent(
                this.self,
                this.self.objGetDelegate().objGetContainer("extent")
            );
        }
        return this.extent;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objGetContainer(java.lang.String)
     */
    @Override
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        return "extent".equals(feature) ? getExtent() : super.objGetContainer(feature);
    }

}

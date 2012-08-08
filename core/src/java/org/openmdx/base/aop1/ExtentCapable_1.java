/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ExtentCapable_1.java,v 1.6 2009/11/04 15:59:44 hburger Exp $
 * Description: Extent Capable
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/04 15:59:44 $
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

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * Extent Capable Plug-In
 */
public class ExtentCapable_1 extends Interceptor_1 {

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * @param next the next plug-in
     * 
     * @throws ServiceException
     */
    public ExtentCapable_1(
        ObjectView_1_0 self,
        Interceptor_1 next
    ) throws ServiceException {
        super(self, next);
        this.aspect = self.getModel().isInstanceof(
            self.objGetDelegate(), 
            "org:openmdx:base:Aspect"
        );
    }
    
    /**
     * <code>true</code> in case of an <code>org::openmdx::base::Aspect</code> instance
     */
    private boolean aspect;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        if(SystemAttributes.OBJECT_IDENTITY.equals(feature)) {
            Object resourceIdentifier;
            if(this.aspect) {
                DataObject_1_0 core = (DataObject_1_0) this.self.objGetDelegate().objGetValue("core");
                resourceIdentifier = core == null ? null : core.jdoGetObjectId();
            } else {
                resourceIdentifier = this.jdoGetObjectId();
            }
            return resourceIdentifier == null ? null : ( 
                resourceIdentifier instanceof Path ? ((Path)resourceIdentifier) : new Path(resourceIdentifier.toString())
            ).toXRI();
        } else {
            return super.objGetValue(feature);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objSetValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException {
        if(SystemAttributes.OBJECT_IDENTITY.equals(feature)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "An object's identity is read-only",
                new BasicException.Parameter("feature", feature)
            );
        }
        super.objSetValue(feature, to);
    }
    
}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: InactiveObject_1.java,v 1.4 2007/11/15 11:21:34 hburger Exp $
 * Description: InvalidatedObject_1 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/15 11:21:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.spi;

import java.util.Date;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.layer.model.State_1_Attributes;

/**
 * InvalidatedObject_1
 */
class InactiveObject_1
    extends DelegatingObject_1
{

    /**
     * Constructor 
     *
     * @param object
     * @param deletePending
     */
    InactiveObject_1(
        Object_1_0 object, 
        boolean deletePending
    ) {
        super(object);
        this.deletePending = deletePending;
        this.invalidatedAt = deletePending ? null : DateFormat.getInstance().format(new Date());
    }
    
    private final String invalidatedAt;

    private final boolean deletePending;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.DelegatingObject_1#objGetValue(java.lang.String)
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return (
            State_1_Attributes.INVALIDATED_AT.equals(feature) ||
            ('?' + State_1_Attributes.INVALIDATED_AT).equals(feature)
        ) ? this.invalidatedAt : super.objGetValue(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.DelegatingObject_1#objIsDeleted()
     */
    public boolean objIsDeleted(
    ) throws ServiceException {
        return this.deletePending || super.objIsDeleted();
    }
    
}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Object_1_6.java,v 1.3 2008/12/15 03:15:36 hburger Exp $
 * Description: Object 1.6 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:36 $
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
package org.openmdx.base.accessor.generic.spi;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_1;
import org.openmdx.base.accessor.generic.cci.Object_1_2;
import org.openmdx.base.accessor.generic.cci.Object_1_3;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0;

/**
 * Object 1.6 
 */
public interface Object_1_6
    extends Delegating_1_0, Object_1_1, Object_1_2, Object_1_3, Object_1_5, ModelHolder_1_0
{
    
    /**
     * Retrieve the interaction specification associated with this object
     * 
     * @return <code>null</code> or the interaction specification associated with this object
     */
    InteractionSpec getInteractionSpec(
    );
    
    /**
     * Create an instance's clone
     * 
     * @param original an instance to be cloned 
     * @param identity the identity of the new object if a persistent-new 
     * instance should be returned, <code>null</code> if a transient instance 
     * should be returned
     * 
     * @return a clone
     * 
     * @exception ServiceException if case of failure
     */
    Object_1_0 cloneDelegate(
        Object_1_0 original, 
        Path identity
    ) throws ServiceException;
    
    /**
     * Retrieve the delegate
     * 
     * @return the delegate
     */
    Object_1_5 objGetDelegate();

    /**
     * Set the delegate 
     * 
     * @param delegate the new delegate
     */
    void objSetDelegate(
        Object_1_5 delegate
    );
    
}

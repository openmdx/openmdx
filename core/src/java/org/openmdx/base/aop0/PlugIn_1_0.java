/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: PlugIn_1_0.java,v 1.5 2009/11/23 17:27:53 hburger Exp $
 * Description: PlugIn_1_0 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/23 17:27:53 $
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
package org.openmdx.base.aop0;

import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.exception.ServiceException;


/**
 * Plug-In 1.0
 * <p>
 * A <code>PlugIn_1_0</code> implementation may implement any of the 
 * <code>InstanceLifecycleListener</code> interfaces in order to be
 * notified about life cycle events.
 * 
 * @see javax.jdo.listener.InstanceLifecycleListener
 */
public interface PlugIn_1_0 {
    
    /**
     * New qualifier callback
     * 
     * @param object
     * @param qualifier
     * 
     * @return the last XRI segment of the new object id, or the given
     * qualifier if it is not to be modified by the current plug-in
     */
    String getQualifier(
        DataObject_1 object,
        String qualifier
    ) throws ServiceException;

    /**
     * Post-set-"core" callback
     * 
     * @param target
     * @param core
     */
    void postSetCore(
        DataObject_1 target,
        DataObject_1 core
    ) throws ServiceException;

    /**
     * Called when the unit of work is about to be completed
     * 
     * @param unitOfWork
     */
    void beforeCompletion(
        UnitOfWork_1 unitOfWork
    );
    
    /**
     * A plug-in may optionally provide user objects
     * 
     * @param key 
     * 
     * @return the user object for the given key, or <code>null</code> if no 
     * such user object is provided by this plug-in
     */
    Object getUserObject(
        Object key
    );    
    
}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InstanceCallbackListener.java,v 1.4 2004/04/02 16:59:00 wfro Exp $
 * Description: openMDX: Instance Callback Listener
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:00 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.event;

import java.util.EventListener;

import org.openmdx.base.exception.ServiceException;


/**
 * openMDX
 * Instance Callback Listener
 */
public interface InstanceCallbackListener extends EventListener {

    /**
     * Called after the values are loaded from the data store into this 
     * instance. 
     * <p>
     * Derived fields should be initialized in this method. The context in which this 
     * call is made does not allow access to other persistent Object_1_0 instances.
     * 
     * @see InstanceCallbackEvent#POST_LOAD
     * 
     * @param   event
     *          the instance callback event
     * 
     * @exception   ServiceException    
     *              in case of failure
     */
    public void postLoad(
        InstanceCallbackEvent event
    ) throws ServiceException;

    /**
     * Called before the values are stored from this instance. 
     * <p>
     * Fields that might have been affected by modified non-persistent fields should be 
     * updated in this method.
     * <p>
     * The context in which this call is made allows access to the ObjectFactory_1_0 
     * instance and other persistent Object_1_0 instances. 
     * 
     * @see InstanceCallbackEvent#PRE_STORE
     * 
     * @param   event
     *          the instance callback event
     * 
     * @exception   ServiceException    
     *              in case of failure or veto
     */
    public void preStore(
        InstanceCallbackEvent event
    ) throws ServiceException;

    /**
     * Called before the values in the instance are cleared. 
     * <p>
     * Transient fields should be cleared in this method. Associations between this 
     * instance and others in the runtime environment should be cleared. 
     * 
     * @param   event
     *          the instance callback event
     * 
     * @see InstanceCallbackEvent#PRE_CLEAR
     * 
     * @exception   ServiceException    
     *              in case of failure
     */
    public void preClear(
        InstanceCallbackEvent event
    ) throws ServiceException;

    /**
     * Called before the instance is deleted. This method is called before the state 
     * transition to persistent-deleted or persistent-new-deleted. Access to field 
     * values within this call are valid. Access to field values after this call 
     * are disallowed. 
     * 
     * @param   event
     *          the instance callback event
     * 
     * @see InstanceCallbackEvent#PRE_DELETE
     * 
     * @exception   ServiceException    
     *              in case of failure or veto
     */
    public void preDelete(
        InstanceCallbackEvent event
    ) throws ServiceException;

}

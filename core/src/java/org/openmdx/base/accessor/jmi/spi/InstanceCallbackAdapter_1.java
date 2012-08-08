/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InstanceCallbackAdapter_1.java,v 1.4 2009/05/23 10:14:15 wfro Exp $
 * Description: Instance Callback Adapter
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/23 10:14:15 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;

import org.openmdx.base.event.InstanceCallbackEvent;
import org.openmdx.base.event.InstanceCallbackListener;
import org.openmdx.base.exception.ServiceException;

/**
 * Instance Callback Adapter
 */
class InstanceCallbackAdapter_1 implements InstanceCallbackListener {

	/**
	 * Constructor 
	 * 
	 * @param instance
	 */
    private InstanceCallbackAdapter_1(
        Object instance
    ){
        this.instance = instance;
    }
    
    /**
     * Factory 
     * 
     * @param instance
     * 
     * @return a new <code>InstanceCallbackListener</code> or <code>Null</code>
     */
    static InstanceCallbackListener newInstance(
    	Object instance
    ){
    	return new InstanceCallbackAdapter_1(instance);
    }

    /**
     * The instance implementing one or more callbacks
     */
    private final Object instance;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#postLoad(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void postLoad(InstanceCallbackEvent instanceCallback) throws ServiceException {
        if(instance instanceof LoadCallback)((LoadCallback)this.instance).jdoPostLoad();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preStore(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preStore(InstanceCallbackEvent instanceCallback) throws ServiceException {
        if(instance instanceof StoreCallback)((StoreCallback)this.instance).jdoPreStore();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preClear(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preClear(InstanceCallbackEvent instanceCallback) throws ServiceException {
        if(instance instanceof ClearCallback)((ClearCallback)this.instance).jdoPreClear();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preDelete(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preDelete(InstanceCallbackEvent instanceCallback) throws ServiceException {
        if(instance instanceof DeleteCallback)((DeleteCallback)this.instance).jdoPreDelete();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#postCreate(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void postCreate(
        InstanceCallbackEvent event
    ) throws ServiceException {
        // There is no CreateCallback
    }

    Object getInstance(
    ) {
        return this.instance;
    }
    
}

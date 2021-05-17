/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Instance Callback Adapter
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2009, OMEX AG, Switzerland
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
import javax.jdo.listener.ClearLifecycleListener;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreCallback;
import javax.jdo.listener.StoreLifecycleListener;

/**
 * Instance Callback Adapter
 */
class InstanceCallbackAdapter_1 
    implements LoadLifecycleListener, StoreLifecycleListener, ClearLifecycleListener, DeleteLifecycleListener
{

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
    static InstanceLifecycleListener newInstance(
    	Object instance
    ){
    	return new InstanceCallbackAdapter_1(instance);
    }

    /**
     * The instance implementing one or more callbacks
     */
    private final Object instance;
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.LoadLifecycleListener#postLoad(javax.jdo.listener.InstanceLifecycleEvent)
     */
    public void postLoad(InstanceLifecycleEvent event) {
        if(instance instanceof LoadCallback)((LoadCallback)this.instance).jdoPostLoad();
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#postStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
    public void postStore(InstanceLifecycleEvent event) {
        // There is no post-store callback
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#preStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
    public void preStore(InstanceLifecycleEvent event) {
        if(instance instanceof StoreCallback)((StoreCallback)this.instance).jdoPreStore();
    }


    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearLifecycleListener#postClear(javax.jdo.listener.InstanceLifecycleEvent)
     */
    public void postClear(InstanceLifecycleEvent event) {
        // There is no post-clear callback
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearLifecycleListener#preClear(javax.jdo.listener.InstanceLifecycleEvent)
     */
    public void preClear(InstanceLifecycleEvent event) {
        if(instance instanceof ClearCallback)((ClearCallback)this.instance).jdoPreClear();
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteLifecycleListener#postDelete(javax.jdo.listener.InstanceLifecycleEvent)
     */
    public void postDelete(InstanceLifecycleEvent event) {
        // There is no post-delete callback
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteLifecycleListener#preDelete(javax.jdo.listener.InstanceLifecycleEvent)
     */
    public void preDelete(InstanceLifecycleEvent event) {
        if(instance instanceof DeleteCallback)((DeleteCallback)this.instance).jdoPreDelete();
    }

    Object getInstance(
    ) {
        return this.instance;
    }
    
}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: MarshallingInstanceLifecycleListener.java,v 1.4 2010/04/19 11:27:09 hburger Exp $
 * Description: Dispatching Instance Life-Cycle Listener
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/19 11:27:09 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.spi;

import java.lang.ref.WeakReference;

import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;

import org.openmdx.base.collection.Registry;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.kernel.log.SysLog;

/**
 * Dispatching Instance Life-Cycle Listener
 * <p>
 * Dispatches<ol>
 * <li>to its registered children
 * <li>to the marshalled persistent instances
 * </ol>
 */
public class MarshallingInstanceLifecycleListener extends InstanceLifecycleListenerRegistry {

    /**
     * Constructor 
     *
     * @param registry the mandatory registry
     * @param marshaller the optional marshaller
     */
    public MarshallingInstanceLifecycleListener(
        Registry<?,?> registry,
        Marshaller marshaller
    ) {
        this.registry = new WeakReference<Registry<?,?>>(registry);
        this.marshaller = marshaller == null ? null : new WeakReference<Marshaller>(marshaller);
    }

    /**
     * A weak reference to the registry
     */
    private final WeakReference<Registry<?,?>> registry;

    /**
     * A weak reference to the marshaller
     */
    private final WeakReference<Marshaller> marshaller;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.InstanceLifecycleListenerRegistry#close()
     */
    @Override
    public void close() {
        this.registry.clear();
        if(this.marshaller != null) {
            this.marshaller.clear();
        }
        super.close();
    }

    
    /**
     * Retrieve an event's persistent capable object
     * 
     * @param source event created by another manager
     * @param reluctant avoid marshalling if <code>true</code>
     * 
     * @return the persistence capable object to which the event belongs
     */
    @SuppressWarnings("unchecked")
    private Object getPersistenceCapable(
        InstanceLifecycleEvent event, 
        boolean reluctant
    ){
        Object source = event.getPersistentInstance();
        if(source == null) {
            return null;
        } else if(reluctant) {
            try {
                return ((Registry) this.registry.get()).get(source);
            } catch (NullPointerException exception) {
                SysLog.warning("The event listener's registry has already been evicted by the garbage collector");
                return null;
            }
        } else {
            try {
                return this.marshaller.get().marshal(source);
            } catch (NullPointerException exception) {
                SysLog.warning("The event listener's marshaller has already been evicted by the garbage collector");
                return null;
            } catch (ServiceException exception) {
                SysLog.warning("Could not marshal the event's persistent instance", exception);
                return null;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearLifecycleListener#postClear(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void postClear(InstanceLifecycleEvent event) {
        if(!isEmpty()) {
            Object pc = getPersistenceCapable(event, true);
            if(pc != null) {
                super.postClear(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.CLEAR));
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearLifecycleListener#preClear(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void preClear(InstanceLifecycleEvent event) {
        Object pc = getPersistenceCapable(event, true);
        if(pc != null) {
            super.preClear(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.CLEAR));
            if(pc instanceof ClearCallback) {
                ((ClearCallback)pc).jdoPreClear();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.CreateLifecycleListener#postCreate(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void postCreate(InstanceLifecycleEvent event) {
        if(!isEmpty()) {
            Object pc = getPersistenceCapable(event, this.marshaller == null);
            if(pc != null) {
                super.postCreate(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.CREATE));
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteLifecycleListener#postDelete(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void postDelete(InstanceLifecycleEvent event) {
        if(!isEmpty()) {
            Object pc = getPersistenceCapable(event, this.marshaller == null);
            if(pc != null) {
                super.postDelete(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DELETE));
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteLifecycleListener#preDelete(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void preDelete(InstanceLifecycleEvent event) {
        Object pc = getPersistenceCapable(event, this.marshaller == null);
        if(pc != null) {
            super.preDelete(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DELETE));
            if(pc instanceof DeleteCallback) {
                ((DeleteCallback)pc).jdoPreDelete();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DirtyLifecycleListener#postDirty(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void postDirty(InstanceLifecycleEvent event) {
        if(!isEmpty()) {
            Object pc = getPersistenceCapable(event, this.marshaller == null);
            if(pc != null) {
                super.postDirty(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DIRTY));
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DirtyLifecycleListener#preDirty(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void preDirty(InstanceLifecycleEvent event) {
        if(!isEmpty()) {
            Object pc = getPersistenceCapable(event, this.marshaller == null);
            if(pc != null) {
                super.preDirty(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DIRTY));
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.LoadLifecycleListener#postLoad(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void postLoad(InstanceLifecycleEvent event) {
        Object pc = getPersistenceCapable(event, this.marshaller == null);
        if(pc != null) {
            super.postLoad(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.LOAD));
            if(pc instanceof LoadCallback) {
                ((LoadCallback)pc).jdoPostLoad();
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#postStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void postStore(InstanceLifecycleEvent event) {
        if(!isEmpty()) {
            Object pc = getPersistenceCapable(event, this.marshaller == null);
            if(pc != null) {
                super.postStore(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.STORE));
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#preStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
    @Override
    public void preStore(InstanceLifecycleEvent event) {
        Object pc = getPersistenceCapable(event, this.marshaller == null);
        if(pc != null) {
            super.preStore(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.STORE));
            if(pc instanceof StoreCallback) {
                ((StoreCallback)pc).jdoPreStore();
            }
        }
    }
        
}

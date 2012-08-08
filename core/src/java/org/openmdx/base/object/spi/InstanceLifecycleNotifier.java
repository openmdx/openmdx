/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: InstanceLifecycleNotifier.java,v 1.6 2008/03/04 14:21:11 hburger Exp $
 * Description: Instance Lifecycle Notifier
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/04 14:21:11 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
package org.openmdx.base.object.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.ClearLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.DetachLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreCallback;
import javax.jdo.listener.StoreLifecycleListener;

/**
 * Instance Lifecycle Notifier
 *
 * @since openMDX 2.0
 */
public final class InstanceLifecycleNotifier {

    /**
     * Constructor 
     */
    public InstanceLifecycleNotifier() {
        this.registry = new HashMap<InstanceLifecycleListener,Class<?>[]>();
    }
    
    /**
     * Constructor 
     *
     * @param that
     */
    public InstanceLifecycleNotifier(
        InstanceLifecycleNotifier that
    ) {
        this.registry = new HashMap<InstanceLifecycleListener,Class<?>[]>(that.registry);
    }
    
    /**
     * <code>InstanceLifecycleListener</code> registry
     */
    private final Map<InstanceLifecycleListener,Class<?>[]> registry;
    
    private final static Class<?>[] ALL = new Class[]{
        Object.class
    };
    
    /**
     * Close the instance.
     */
    public void close(){
        this.registry.clear();
    }
    
    /**
     * Adds the listener instance to the list of lifecycle event
     * listeners. The <code>classes</code> parameter identifies all
     * of the classes of interest. If the <code>classes</code>
     * parameter is specified as <code>null</code>, events for all
     * persistent classes and interfaces will be sent to
     * <code>listenerInstance</code>.
     * <p>The listenerInstance will be called for each event for which it
     * implements the corresponding listenerInstance interface.</p>
     * @param listener the lifecycle listener
     * @param classes the classes of interest to the listener
     */
    public synchronized void addInstanceLifecycleListener (
        InstanceLifecycleListener listener,
        Class<?>[] classes
    ){
        if(classes == null) {
            this.registry.put(listener, ALL);
        } else {
            Class<?>[] former = this.registry.put(listener, classes);
            if(former != null) {
                Set<Class<?>> combined = new HashSet<Class<?>>();
                combined.addAll(Arrays.asList(former));
                combined.addAll(Arrays.asList(classes));
                this.registry.put(
                    listener,
                    combined.toArray(new Class[combined.size()])
                );
            }
        }
    }

    /**
     * Removes the listener instance from the list of lifecycle event listeners.
     * @param listener the listener instance to be removed
     */
    public synchronized void removeInstanceLifecycleListener (
        InstanceLifecycleListener listener
    ){
        this.registry.remove(listener);
    }

    /**
     * Determine the set of matching listeners
     * 
     * @param listenerClass
     * @param sourceClass
     * 
     * @return the set of matching listeners
     */
    <T extends InstanceLifecycleListener> Collection<T> getListeners(
        Class<T> listenerClass,
        Object source
    ){
        if(this.registry.isEmpty()) return null;
        List<T> listeners = new ArrayList<T>();
        for(Map.Entry<InstanceLifecycleListener,Class<?>[]> e : this.registry.entrySet()) {
            InstanceLifecycleListener l = e.getKey();
            if(listenerClass.isInstance(l)) {
                Class<?>[] c = e.getValue();
                for(
                    int j = 0;
                    j < c.length;
                    j++
                ) if(
                    c[j].isInstance(source)
                ) {
                    listeners.add(listenerClass.cast(l));    
                    break;
                }
            }
        }
        return listeners.isEmpty() ? null : listeners;
    }

    
    //------------------------------------------------------------------------
    // Attach
    //------------------------------------------------------------------------

    /**
     * Get a specific attach Notifier
     * 
     * @param detached
     * 
     * @return the corresponding attach Notifier
     */
    public InstanceLifecycleNotifier.AttachNotifier getAttachNotifier(
        Object detached
    ){
        return new AttachNotifier(
            getListeners(AttachLifecycleListener.class, detached),
            detached
        );
    }

    /**
     * Attach Notifier
     */
    public static final class AttachNotifier {        

        /**
         * Constructor 
         *
         * @param listeners
         * @param detached
         */
        AttachNotifier (
            Collection<AttachLifecycleListener> listeners,
            Object detached
        ){
            this.detached = detached;
            if(listeners != null) {
                this.listeners = listeners.toArray(
                    new AttachLifecycleListener[listeners.size()]
                 );
            } else {
                this.listeners = null;
            }
        }

        /**
         * The matching listeners
         */
        AttachLifecycleListener[] listeners;

        /**
         * The detached object
         */
        Object detached;
        
        /**
         * Process pre-attach callback and event
         */
        public void preAttach() {
            if(this.listeners != null) {
                InstanceLifecycleEvent event = new InstanceLifecycleEvent(
                    this.detached,
                    InstanceLifecycleEvent.ATTACH
                );
                for(
                    int i = 0;
                    i < this.listeners.length;
                    i++
                ) this.listeners[i].preAttach(event);
            }
            if(this.detached instanceof AttachCallback) {
                ((AttachCallback)this.detached).jdoPreAttach();
            }
        }

        /**
         * Process post-attach callback and event
         */
        public void postAttach(
            Object persistent
        ) {
            if(persistent instanceof AttachCallback) {
                ((AttachCallback)persistent).jdoPostAttach(this.detached);
            }
            if(this.listeners != null) {
                InstanceLifecycleEvent event = new InstanceLifecycleEvent(
                    persistent,
                    InstanceLifecycleEvent.ATTACH,
                    this.detached
                );
                for(
                    int i = 0;
                    i < this.listeners.length;
                    i++
                ) this.listeners[i].postAttach(event);
            }
            close();
        }
        
        /**
         * This method is implicitely called by postAttach
         */
        public void close(){
            this.listeners = null;
            this.detached = null;
        }

    }
    
    
    //------------------------------------------------------------------------
    // Clear
    //------------------------------------------------------------------------

    /**
     * Get a specific clear Notifier
     * 
     * @param persistent
     * 
     * @return the corresponding clear Notifier
     */
    public InstanceLifecycleNotifier.ClearNotifier getClearNotifier(
        Object persistent
    ){
        return new ClearNotifier(
            getListeners(ClearLifecycleListener.class, persistent),
            persistent
        );
    }

    /**
     * Clear Notifier
     */
    public static final class ClearNotifier {        

        /**
         * Constructor 
         *
         * @param listeners
         * @param persistent
         */
        ClearNotifier (
            Collection<ClearLifecycleListener> listeners,
            Object persistent
        ){
            this.persistent = persistent;
            if(listeners != null) {
                this.listeners = listeners.toArray(
                    new ClearLifecycleListener[listeners.size()]
                 );
                this.event = new InstanceLifecycleEvent(
                    persistent,
                    InstanceLifecycleEvent.CLEAR
                );
            } else {
                this.listeners = null;
                this.event = null;
            }
        }

        /**
         * The matching listeners
         */
        ClearLifecycleListener[] listeners;

        /**
         * The detached object
         */
        Object persistent;

        /**
         * The event
         */
        InstanceLifecycleEvent event;
        
        /**
         * Process pre-clear callback and event
         */
        public void preClear() {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].preClear(this.event);
            if(this.persistent instanceof ClearCallback) {
                ((ClearCallback)this.persistent).jdoPreClear();
            }
        }

        /**
         * Process post-clear event
         */
        public void postClear(
        ) {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].postClear(this.event);
            close();
        }
        
        /**
         * This method is implicitely called by postAttach
         */
        public void close(){
            this.listeners = null;
            this.persistent = null;
            this.event = null;
        }

    }

    
    //------------------------------------------------------------------------
    // Create
    //------------------------------------------------------------------------

    /**
     * Get a specific create Notifier
     * 
     * @param persistent
     * 
     * @return the corresponding create Notifier
     */
    public InstanceLifecycleNotifier.CreateNotifier getCreateNotifier(
        Object persistent
    ){
        return new CreateNotifier(
            getListeners(CreateLifecycleListener.class, persistent),
            persistent
        );
    }

    /**
     * Clear Notifier
     */
    public static final class CreateNotifier {        

        /**
         * Constructor 
         *
         * @param listeners
         * @param persistent
         */
        CreateNotifier (
            Collection<CreateLifecycleListener> listeners,
            Object persistent
        ){
            this.persistent = persistent;
            if(listeners != null) {
                this.listeners = listeners.toArray(
                    new CreateLifecycleListener[listeners.size()]
                 );
                this.event = new InstanceLifecycleEvent(
                    persistent,
                    InstanceLifecycleEvent.CREATE
                );
            } else {
                this.listeners = null;
                this.event = null;
            }
        }
        
        /**
         * The matching listeners
         */
        CreateLifecycleListener[] listeners;

        /**
         * The detached object
         */
        Object persistent;

        /**
         * The event
         */
        InstanceLifecycleEvent event;
        
        /**
         * Process post-create event
         */
        public void postCreate(
        ) {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].postCreate(this.event);
            close();
        }
        
        /**
         * This method is implicitely called by postAttach
         */
        public void close(){
            this.listeners = null;
            this.persistent = null;
            this.event = null;
        }

    }

    
    //------------------------------------------------------------------------
    // Delete
    //------------------------------------------------------------------------

    /**
     * Get a specific delete Notifier
     * 
     * @param persistent
     * 
     * @return the corresponding delete Notifier
     */
    public InstanceLifecycleNotifier.DeleteNotifier getDeleteNotifier(
        Object persistent
    ){
        return new DeleteNotifier(
            getListeners(DeleteLifecycleListener.class, persistent),
            persistent
        );
    }

    /**
     * Clear Notifier
     */
    public static final class DeleteNotifier {        

        /**
         * Constructor 
         *
         * @param listeners
         * @param persistent
         */
        DeleteNotifier (
            Collection<DeleteLifecycleListener> listeners,
            Object persistent
        ){
            this.persistent = persistent;
            if(listeners != null) {
                this.listeners = listeners.toArray(
                    new DeleteLifecycleListener[listeners.size()]
                 );
                this.event = new InstanceLifecycleEvent(
                    persistent,
                    InstanceLifecycleEvent.DELETE
                );
            } else {
                this.listeners = null;
                this.event = null;
            }
        }

        /**
         * The matching listeners
         */
        DeleteLifecycleListener[] listeners;

        /**
         * The detached object
         */
        Object persistent;

        /**
         * The event
         */
        InstanceLifecycleEvent event;
        
        /**
         * Process pre-delete callback and event
         */
        public void preDelete() {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].preDelete(this.event);
            if(this.persistent instanceof ClearCallback) {
                ((DeleteCallback)this.persistent).jdoPreDelete();
            }
        }

        /**
         * Process post-delete event
         */
        public void postDelete(
        ) {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].postDelete(this.event);
            close();
        }
        
        /**
         * This method is implicitely called by postDelete
         */
        public void close(){
            this.listeners = null;
            this.persistent = null;
            this.event = null;
        }

    }


    //------------------------------------------------------------------------
    // Detach
    //------------------------------------------------------------------------

    /**
     * Get a specific detach Notifier
     * 
     * @param persistent
     * 
     * @return the corresponding detach Notifier
     */
    public InstanceLifecycleNotifier.DetachNotifier getDetachNotifier(
        Object persistent
    ){
        return new DetachNotifier(
            getListeners(DetachLifecycleListener.class, persistent),
            persistent
        );
    }

    /**
     * Detach Notifier
     */
    public static final class DetachNotifier {        

        /**
         * Constructor 
         *
         * @param listeners
         * @param persistent
         */
        DetachNotifier (
            Collection<DetachLifecycleListener> listeners,
            Object persistent
        ){
            this.persistent = persistent;
            if(listeners != null) {
                this.listeners = listeners.toArray(
                    new DetachLifecycleListener[listeners.size()]
                 );
            } else {
                this.listeners = null;
            }
        }

        /**
         * The matching listeners
         */
        DetachLifecycleListener[] listeners;

        /**
         * The detached object
         */
        Object persistent;
        
        /**
         * Process pre-detach callback and event
         */
        public void preDetach() {
            if(this.listeners != null) {
                InstanceLifecycleEvent event = new InstanceLifecycleEvent(
                    persistent,
                    InstanceLifecycleEvent.DETACH
                );
                for(
                    int i = 0;
                    i < this.listeners.length;
                    i++
                ) this.listeners[i].preDetach(event);
            }
            if(this.persistent instanceof DetachCallback) {
                ((DetachCallback)this.persistent).jdoPreDetach();
            }
        }

        /**
         * Process post-attach callback and event
         */
        public void postAttach(
            Object detached
        ) {
            if(detached instanceof DetachCallback) {
                ((DetachCallback)detached).jdoPostDetach(this.persistent);
            }
            if(this.listeners != null) {
                InstanceLifecycleEvent event = new InstanceLifecycleEvent(
                    detached,
                    InstanceLifecycleEvent.ATTACH,
                    this.persistent
                );
                for(
                    int i = 0;
                    i < this.listeners.length;
                    i++
                ) this.listeners[i].postDetach(event);
            }
            close();
        }
        
        /**
         * This method is implicitely called by postAttach
         */
        public void close(){
            this.listeners = null;
            this.persistent = null;
        }
        
    }

    
    //------------------------------------------------------------------------
    // Dirty
    //------------------------------------------------------------------------

    /**
     * Get a specific dirty Notifier
     * 
     * @param persistent
     * 
     * @return the corresponding dirty Notifier
     */
    public InstanceLifecycleNotifier.DirtyNotifier getDirtyNotifier(
        Object persistent
    ){
        return new DirtyNotifier(
            getListeners(DirtyLifecycleListener.class, persistent),
            persistent
        );
    }

    /**
     * Dirty Notifier
     */
    public static final class DirtyNotifier {        

        /**
         * Constructor 
         *
         * @param listeners
         * @param persistent
         */
        DirtyNotifier (
            Collection<DirtyLifecycleListener> listeners,
            Object persistent
        ){
            this.persistent = persistent;
            if(listeners != null) {
                this.listeners = listeners.toArray(
                    new DirtyLifecycleListener[listeners.size()]
                 );
                this.event = new InstanceLifecycleEvent(
                    persistent,
                    InstanceLifecycleEvent.DIRTY
                );
            } else {
                this.listeners = null;
                this.event = null;
            }
        }

        /**
         * The matching listeners
         */
        DirtyLifecycleListener[] listeners;

        /**
         * The detached object
         */
        Object persistent;

        /**
         * The event
         */
        InstanceLifecycleEvent event;
        
        /**
         * Process pre-dirty callback
         */
        public void preClear() {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].preDirty(this.event);
        }

        /**
         * Process post-dirty event
         */
        public void postDirty(
        ) {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].postDirty(this.event);
            close();
        }
        
        /**
         * This method is implicitely called by postDirty
         */
        public void close(){
            this.listeners = null;
            this.persistent = null;
            this.event = null;
        }
        
    }

    
    //------------------------------------------------------------------------
    // Load
    //------------------------------------------------------------------------

    /**
     * Get a specific load Notifier
     * 
     * @param persistent
     * 
     * @return the corresponding load Notifier
     */
    public InstanceLifecycleNotifier.LoadNotifier getLoadNotifier(
        Object persistent
    ){
        return new LoadNotifier(
            getListeners(LoadLifecycleListener.class, persistent),
            persistent
        );
    }

    /**
     * Detach Notifier
     */
    public static final class LoadNotifier {        

        /**
         * Constructor 
         *
         * @param listeners
         * @param persistent
         */
        LoadNotifier (
            Collection<LoadLifecycleListener> listeners,
            Object persistent
        ){
            this.persistent = persistent;
            if(listeners != null) {
                this.listeners = listeners.toArray(
                    new LoadLifecycleListener[listeners.size()]
                 );
                this.event = new InstanceLifecycleEvent(
                    persistent,
                    InstanceLifecycleEvent.LOAD
                );
            } else {
                this.listeners = null;
                this.event = null;
            }
        }

        /**
         * The matching listeners
         */
        LoadLifecycleListener[] listeners;

        /**
         * The detached object
         */
        Object persistent;

        /**
         * The event
         */
        InstanceLifecycleEvent event;
        
        /**
         * Process post-load callback and event
         */
        public void postLoad() {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].postLoad(this.event);
            if(this.persistent instanceof LoadCallback) {
                ((LoadCallback)this.persistent).jdoPostLoad();
            }
            close();
        }

        /**
         * This method is implicitely called by postLoad
         */
        public void close(){
            this.listeners = null;
            this.persistent = null;
            this.event = null;
        }

        
    }

    
    //------------------------------------------------------------------------
    // Store
    //------------------------------------------------------------------------

    /**
     * Get a specific store Notifier
     * 
     * @param persistent
     * 
     * @return the corresponding store Notifier
     */
    public InstanceLifecycleNotifier.StoreNotifier getStoreNotifier(
        Object persistent
    ){
        return new StoreNotifier(
            getListeners(StoreLifecycleListener.class, persistent),
            persistent
        );
    }

    /**
     * Store Notifier
     */
    public static final class StoreNotifier {        

        /**
         * Constructor 
         *
         * @param listeners
         * @param persistent
         */
        StoreNotifier (
            Collection<StoreLifecycleListener> listeners,
            Object persistent
        ){
            this.persistent = persistent;
            if(listeners != null) {
                this.listeners = listeners.toArray(
                    new StoreLifecycleListener[listeners.size()]
                 );
                this.event = new InstanceLifecycleEvent(
                    persistent,
                    InstanceLifecycleEvent.STORE
                );
            } else {
                this.listeners = null;
                this.event = null;
            }
       
            
        }

        /**
         * The matching listeners
         */
        StoreLifecycleListener[] listeners;

        /**
         * The detached object
         */
        Object persistent;

        /**
         * The event
         */
        InstanceLifecycleEvent event;
        
        /**
         * Process pre-store callback and event
         */
        public void preStore() {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].preStore(this.event);
            if(this.persistent instanceof StoreCallback) {
                ((StoreCallback)this.persistent).jdoPreStore();
            }
        }

        /**
         * Process post-store event
         */
        public void postStore(
        ) {
            if(this.listeners != null) for(
                int i = 0;
                i < this.listeners.length;
                i++
            ) this.listeners[i].postStore(this.event);
            close();
        }
        
        /**
         * This method is implicitely called by postStore
         */
        public void close(){
            this.listeners = null;
            this.persistent = null;
            this.event = null;
        }

        
    }

}
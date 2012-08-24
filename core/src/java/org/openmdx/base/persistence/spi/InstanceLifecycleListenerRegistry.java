/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Instance Life-Cycle Listener Registry
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.listener.ClearLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreLifecycleListener;


/**
 * Instance Life-Cycle Listener Registry
 */
public class InstanceLifecycleListenerRegistry implements 
//      AttachLifecycleListener, DetachLifecycleListener, 
        ClearLifecycleListener, CreateLifecycleListener, 
        DeleteLifecycleListener, DirtyLifecycleListener,
        LoadLifecycleListener, StoreLifecycleListener
{
    
    /**
     * 
     */
    private List<Entry> entries = new ArrayList<Entry>();
    
    /**
     * Close the registry
     */
    public void close(){
        if(this.entries != null) {
            this.entries.clear();
        }
        this.entries = null;
    }
    
    /**
     * Tells whether there are delegates or not
     * 
     * @return <code>true</code> if there are no delegates
     */
    protected boolean isEmpty(
    ){
        for(Entry entry : this.entries) {
            if(
                !(entry.listener instanceof InstanceLifecycleListenerRegistry) ||
                !((InstanceLifecycleListenerRegistry)entry.listener).isEmpty()
            ){
                return false;
            }
        }
        return true;
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
        Class<?>... classes
    ){
        this.entries.add(new Entry(listener, classes));
    }

    /**
     * Removes the listener instance from the list of lifecycle event listeners.
     * 
     * @param listener the listener instance to be removed
     */
    public synchronized void removeInstanceLifecycleListener (
        InstanceLifecycleListener listener
    ){
        for(
            Iterator<Entry> i = this.entries.iterator();
            i.hasNext();
        ){
            Entry entry = i.next();
            if(entry.listener == listener) {
                i.remove();
                return;
            }
        }
    }        
     
    /**
     * Tests whether a listener is interested in a given event
     * 
     * @param event
     * @param classes
     * 
     * @return <code>true</code> if a listener is interested in a given event
     */
    private boolean isInterested(
        InstanceLifecycleEvent event,
        Class<?>[] classes
    ){
        if(classes == null) {
            return true;
        } else {
            Object persistentInstance = event.getPersistentInstance();
            for(Class<?> candidate : classes) {
                if(candidate.isInstance(persistentInstance)) {
                    return true;
                }
            }
            return false;
        }        
    }

    /**
     * Propagate the registered listeners to a persistence manager
     * 
     * @param persistenceManager
     */
    public void propagateTo(
        PersistenceManager persistenceManager
    ){
        for(Entry entry : this.entries) {
            persistenceManager.addInstanceLifecycleListener(entry.listener, entry.classes);
        }
    }
    
        
    //------------------------------------------------------------------------
    // Implements CreateLifecycleListener
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.CreateLifecycleListener#postCreate(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void postCreate(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof CreateLifecycleListener && isInterested(event, entry.classes)) {
                ((CreateLifecycleListener)entry.listener).postCreate(event);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.LoadLifecycleListener#postLoad(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void postLoad(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof LoadLifecycleListener && isInterested(event, entry.classes)) {
                ((LoadLifecycleListener)entry.listener).postLoad(event);
            }
        }
    }

    
    //------------------------------------------------------------------------
    // Implements StoreLifecycleListener
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#postStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void postStore(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof StoreLifecycleListener && isInterested(event, entry.classes)) {
                ((StoreLifecycleListener)entry.listener).postStore(event);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreLifecycleListener#preStore(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void preStore(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof StoreLifecycleListener && isInterested(event, entry.classes)) {
                ((StoreLifecycleListener)entry.listener).preStore(event);
            }
        }
    }

    
    //------------------------------------------------------------------------
    // Implements ClearLifecycleListener
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearLifecycleListener#postClear(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void postClear(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof ClearLifecycleListener && isInterested(event, entry.classes)) {
                ((ClearLifecycleListener)entry.listener).postClear(event);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearLifecycleListener#preClear(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void preClear(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof ClearLifecycleListener && isInterested(event, entry.classes)) {
                ((ClearLifecycleListener)entry.listener).preClear(event);
            }
        }
    }

    
    //------------------------------------------------------------------------
    // Implements DeleteLifecycleListener
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteLifecycleListener#postDelete(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void postDelete(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof DeleteLifecycleListener && isInterested(event, entry.classes)) {
                ((DeleteLifecycleListener)entry.listener).postDelete(event);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteLifecycleListener#preDelete(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void preDelete(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof DeleteLifecycleListener && isInterested(event, entry.classes)) {
                ((DeleteLifecycleListener)entry.listener).preDelete(event);
            }
        }
    }

    //------------------------------------------------------------------------
    // Implements AttachLifecycleListener
    //------------------------------------------------------------------------
    
//  /* (non-Javadoc)
//   * @see javax.jdo.listener.AttachLifecycleListener#postAttach(javax.jdo.listener.InstanceLifecycleEvent)
//   */
//  @Override
//  public void postAttach(InstanceLifecycleEvent event) {
//  for(Entry entry : this.entries) {
//          if(entry.listener instanceof AttachLifecycleListener && isInterested(event, entry.classes)) {
//              ((AttachLifecycleListener)entry.listener).postAttach(event);
//          }
//      }
//  }
//
//  /* (non-Javadoc)
//   * @see javax.jdo.listener.AttachLifecycleListener#preAttach(javax.jdo.listener.InstanceLifecycleEvent)
//   */
//  @Override
//  public void preAttach(InstanceLifecycleEvent event) {
//      for(Entry entry : this.entries) {
//          if(entry.listener instanceof AttachLifecycleListener && isInterested(event, entry.classes)) {
//              ((AttachLifecycleListener)entry.listener).preAttach(event);
//          }
//      }
//  }

    
    //------------------------------------------------------------------------
    // Implements DirtyLifecycleListener
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.jdo.listener.DirtyLifecycleListener#postDirty(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void postDirty(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof DirtyLifecycleListener && isInterested(event, entry.classes)) {
                ((DirtyLifecycleListener)entry.listener).postDirty(event);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DirtyLifecycleListener#preDirty(javax.jdo.listener.InstanceLifecycleEvent)
     */
//  @Override
    public void preDirty(InstanceLifecycleEvent event) {
        for(Entry entry : this.entries) {
            if(entry.listener instanceof DirtyLifecycleListener && isInterested(event, entry.classes)) {
                ((DirtyLifecycleListener)entry.listener).preDirty(event);
            }
        }
    }

    
    //------------------------------------------------------------------------
    // Implements DetachLifecycleListener
    //------------------------------------------------------------------------
    
//  /* (non-Javadoc)
//   * @see javax.jdo.listener.DetachLifecycleListener#postDetach(javax.jdo.listener.InstanceLifecycleEvent)
//   */
//  @Override
//  public void postDetach(InstanceLifecycleEvent event) {
//      for(Entry entry : this.entries) {
//          if(entry.listener instanceof DetachLifecycleListener && isInterested(event, entry.classes)) {
//              ((DetachLifecycleListener)entry.listener).postDetach(event);
//          }
//      }
//  }
//
//  /* (non-Javadoc)
//   * @see javax.jdo.listener.DetachLifecycleListener#preDetach(javax.jdo.listener.InstanceLifecycleEvent)
//   */
//  @Override
//  public void preDetach(InstanceLifecycleEvent event) {
//      for(Entry entry : this.entries) {
//          if(entry.listener instanceof DetachLifecycleListener && isInterested(event, entry.classes)) {
//              ((DetachLifecycleListener)entry.listener).preDetach(event);
//          }
//      }
//  }

    
    //------------------------------------------------------------------------
    // Class Entry
    //------------------------------------------------------------------------

    /**
     * The entry is plain structure
     */
    static class Entry {

        /**
         * Constructor 
         *
         * @param listener
         * @param classes the classes the listener is interested in 
         */
        Entry(
            InstanceLifecycleListener listener,
            Class<?>[] classes
        ) {
            this.listener = listener;
            this.classes = classes == null || classes.length == 0 ? null : classes;
        }

        /**
         * The listener
         */
        final InstanceLifecycleListener listener;

        /**
         * An empty classes array is normalized to <code>null</code>
         */
        final Class<?>[] classes;
        
    }
    
}

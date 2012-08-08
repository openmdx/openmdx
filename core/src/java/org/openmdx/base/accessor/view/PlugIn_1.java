/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PlugIn_1.java,v 1.13 2009/06/09 12:45:17 hburger Exp $
 * Description: Object 1 Plug-In
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
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
package org.openmdx.base.accessor.view;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.StoreCallback;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.DelegatingObject_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Object 1 Plug-In
 */
public class PlugIn_1
    extends DelegatingObject_1 
    implements ClearCallback, DeleteCallback, StoreCallback
{

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * @param next the next plug-in
     * 
     * @throws ServiceException
     */
    protected PlugIn_1(
        ObjectView_1_0 self,
        PlugIn_1 next
    ) {
        super(next);
        this.self = self;
    }
    
    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * 
     * @throws ServiceException
     */
    protected PlugIn_1(
        ObjectView_1_0 self
    ) throws ServiceException {
        super(self.objGetDelegate());
        this.self = self;
    }
    
    /**
     * The plug-in holder
     */
    protected final ObjectView_1_0 self;

    /**
     * Retrieve the next plug-in
     * 
     * @return the next plug-in, or <code>null</code> if this is the terminal plug-in
     */
    private PlugIn_1 getNext(){
        try {
            DataObject_1_0 delegate = this.getDelegate();
            return delegate instanceof PlugIn_1 ? (PlugIn_1)delegate : null;
        } catch (ServiceException exception) {
            return null;
        }
    }

    /**
     * Retrieve the model repository
     * 
     * @return the model repository
     */
    protected Model_1_0 getModel(){
        return this.self.getModel();
    }
    
    public PersistenceManager jdoGetPersistenceManager(
    ) {
        return this.self.jdoGetPersistenceManager();
    }

    public void objDelete(
    ) throws ServiceException {
        DataObject_1_0 delegate = getDelegate();
        if(delegate instanceof PlugIn_1) {
            ((PlugIn_1)delegate).objDelete();
        } else {
            JDOHelper.getPersistenceManager(delegate).deletePersistent(delegate);
        }
    }
    
    public void objRefresh(
    ) throws ServiceException {
        DataObject_1_0 delegate = getDelegate();
        if(delegate instanceof PlugIn_1) {
            ((PlugIn_1)delegate).objRefresh();
        } else {
            JDOHelper.getPersistenceManager(delegate).refresh(delegate);
        }
    }
    
    public boolean objIsRemoved(
    ) throws ServiceException { 
        DataObject_1_0 delegate = getDelegate();
        return delegate instanceof PlugIn_1 && ((PlugIn_1)delegate).objIsRemoved();
    }

    
    //------------------------------------------------------------------------
    // Implements InstanceCallbacks
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.ClearCallback#jdoPreClear()
     */
    public void jdoPreClear() {
        PlugIn_1 next = getNext();
        if(next != null) {
            next.jdoPreClear();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteCallback#jdoPreDelete()
     */
    public void jdoPreDelete() {
        PlugIn_1 next = getNext();
        if(next != null) {
            next.jdoPreDelete();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreCallback#jdoPreStore()
     */
    public void jdoPreStore() {
        PlugIn_1 next = getNext();
        if(next != null) {
            next.jdoPreStore();
        }
    }
    
}

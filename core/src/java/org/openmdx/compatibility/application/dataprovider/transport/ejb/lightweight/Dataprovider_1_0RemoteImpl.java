/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1_0RemoteImpl.java,v 1.8 2008/01/25 00:58:53 hburger Exp $
 * Description: Dataprovider_1_0RemoteImpl
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 00:58:53 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.application.dataprovider.transport.ejb.lightweight;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;

import javax.ejb.Handle;

import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Remote;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.kernel.application.container.spi.ejb.AbstractEJBObject;

/**
 * Dataprovider_1_0RemoteImpl
 */
class Dataprovider_1_0RemoteImpl 
    extends AbstractEJBObject
    implements Dataprovider_1_0Remote 
{

    /**
     * Constructor
     * 
     * @param home the EJB's home
     */
    Dataprovider_1_0RemoteImpl(
        Dataprovider_1HomeImpl home
    ) throws RemoteException {
        super(home);
        this.action = super.getAction(
            "process",
            new Class[]{
                ServiceHeader.class,
                UnitOfWorkRequest[].class                
            }
        ); 
    }
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3833470608427464246L;

    /**
     * The process() method's action
     */
    private final Action action;

    
    //------------------------------------------------------------------------
    // Implements EJBObject
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.ejb.EJBObject#getHandle()
     */
    public Handle getHandle() throws RemoteException {
        return new Dataprovider_1_0RemoteHandle(getEJBHome().getHomeHandle());
    }

    
    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0Remote
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Remote#process(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest[])
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
    ) throws RemoteException {
        try {
            return (UnitOfWorkReply[]) this.action.invoke(
                new Object[]{header, workingUnits}
            );
        } catch (InvocationTargetException exception) {
            throw new UndeclaredThrowableException(exception);
        }
    }
    
}

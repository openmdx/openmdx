/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ObjectHandle.java,v 1.1 2008/01/25 00:58:53 hburger Exp $
 * Description: ObjectHandle 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 00:58:53 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.kernel.application.container.spi.ejb;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;

/**
 * ObjectHandle
 */
public class ObjectHandle
    implements Handle
{

    /**
     * Implements <code>Srializable</code>.
     */
    private static final long serialVersionUID = 6121659734057135692L;

    /**
     * Constructor 
     *
     */
    protected ObjectHandle(
        HomeHandle homeHandle
    ) {
        this.homeHandle = homeHandle;
    }

    /**
     * 
     */
    private final HomeHandle homeHandle;

    /**
     * 
     */
    private transient EJBObject ejbObject;
    
    /* (non-Javadoc)
     * @see javax.ejb.Handle#getEJBObject()
     */
    public EJBObject getEJBObject(
    ) throws RemoteException {
        EJBHome home = homeHandle.getEJBHome();
        if(this.ejbObject == null) try {
            this.ejbObject = (EJBObject) home.getClass().getMethod("create").invoke(home);
        } catch (InvocationTargetException exception) {
            Throwable throwable = exception.getCause();
            throw throwable instanceof RemoteException ?
                 (RemoteException)throwable :
                 new RemoteException(
                     "Could not re-create EJBObject from its EJBHome instance",
                     throwable
                 );
        } catch (Exception exception) {
            new RemoteException(
                "Could invoke the EJBHome's create() method",
                exception
            );
        }
        return this.ejbObject;
    }

}

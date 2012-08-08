/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LightweightSessionContext.java,v 1.7 2008/01/25 00:58:54 hburger Exp $
 * Description: Session Context Implementation
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 00:58:54 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.container.lightweight;

import java.security.Principal;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.naming.Reference;
import javax.naming.spi.NamingManager;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;


/**
 * Session Context Implementation
 */
class LightweightSessionContext implements SessionContext {

        /**
	 * 
	 */
        Reference localReference = null;

        /**
	 * 
	 */
        Reference remoteReference = null;

        /**
	 *
	 */
        private final UserTransaction userTransaction;

        /**
	 *
	 */
        private final TransactionManager transactionManager;

        /**
	 * Constructor
	 * 
	 * @param transactionManager the TransactionManager instance or 
	 * <code>null</code> in case of a bean managed transaction.
	 * @param userTransaction the UserTransaction instance or 
	 * <code>null</code> in case of a container managed transaction.
	 */
        public LightweightSessionContext(
            TransactionManager transactionManager,
            UserTransaction userTransaction
        ) {
            this.transactionManager = transactionManager;
            this.userTransaction = userTransaction;
        }

    /* (non-Javadoc)
     * @see javax.ejb.SessionContext#getEJBLocalObject()
	 */
    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        EJBLocalHome localHome = getEJBLocalHome();
        try {
            return (EJBLocalObject) localHome.getClass().getMethod(
                "create",
                (Class[])null
             ).invoke(
                 localHome,
                 (Object[])null
             );
        } catch (Exception e) {
            throw new IllegalStateException("EJB Local Object creation failed " + e);
        }
    }

    /* (non-Javadoc)
	 * @see javax.ejb.SessionContext#getEJBObject()
	 */
    public EJBObject getEJBObject() throws IllegalStateException {
        EJBHome home = getEJBHome();
        try {
            return (EJBObject) home.getClass().getMethod(
                "create",
                (Class[])null
             ).invoke(
                 home,
                 (Object[])null
             );
        } catch (Exception e) {
            throw new IllegalStateException("EJB Object creation failed " + e);
        }
    }

        /* (non-Javadoc)
	 * @see javax.ejb.EJBContext#getEJBHome()
	 */
        public EJBHome getEJBHome() {
                if(this.remoteReference == null) {
                    throw new IllegalStateException(
                            "No remote interface available for this EJB"
                    ); 
                } else try {
                    return (EJBHome) NamingManager.getObjectInstance(
                        this.remoteReference, 
                        null, // name
                        null, // nameCtx
                        null // environment
                    );
                } catch (Exception exception) {
                     throw (IllegalStateException) new IllegalStateException(
                         "EJB Home acquisition failed"
                     ).initCause(
                         exception
                     );
                }

        }

        /* (non-Javadoc)
	 * @see javax.ejb.EJBContext#getEJBLocalHome()
	 */
        public EJBLocalHome getEJBLocalHome() {
                if(this.localReference == null){ 
                    throw new IllegalStateException(
                        "No local interface available for this EJB"
                    );
                } else  try {
                    return (EJBLocalHome) NamingManager.getObjectInstance(
                        this.localReference, 
                        null, // name
                        null, // nameCtx
                        null // environment
                    );
                } catch (Exception exception) {
                     throw (IllegalStateException) new IllegalStateException(
                         "EJB Local Home acquisition failed"
                     ).initCause(
                         exception
                     );
                }
        }

        /* (non-Javadoc)
	 * @see javax.ejb.EJBContext#getEnvironment()
	 */
        public Properties getEnvironment() {
                return null;
        }

        /**
	 * @deprecated
	 */
        public java.security.Identity getCallerIdentity() {
                return null;
        }

        /* (non-Javadoc)
	 * @see javax.ejb.EJBContext#getCallerPrincipal()
	 */
        public Principal getCallerPrincipal() {
                return anonymous;
        }

        /**
	 * @deprecated
	 */
        public boolean isCallerInRole(java.security.Identity identity) {
                return isCallerInRole(identity.getName());
        }

        /* (non-Javadoc)
	 * @see javax.ejb.EJBContext#isCallerInRole(java.lang.String)
	 */
        public boolean isCallerInRole(String role) {
                return anonymous.getName().equals(role);
        }

        /* (non-Javadoc)
	 * @see javax.ejb.EJBContext#getUserTransaction()
	 */
        public UserTransaction getUserTransaction() throws IllegalStateException {
            if(this.userTransaction == null) throw new IllegalStateException(
                "Container-managed transaction"
            );
                return this.userTransaction;
        }

        /* (non-Javadoc)
	 * @see javax.ejb.EJBContext#setRollbackOnly()
	 */
        public void setRollbackOnly() throws IllegalStateException {
            if(this.transactionManager == null) throw new IllegalStateException(
                "Bean-managed transaction"
            );
        try {
            this.transactionManager.setRollbackOnly();
        } catch (SystemException exception) {
            throw new RuntimeException(exception.getMessage());
        }
        }

        /* (non-Javadoc)
	 * @see javax.ejb.EJBContext#getRollbackOnly()
	 */
        public boolean getRollbackOnly() throws IllegalStateException {
            if(this.transactionManager == null) throw new IllegalStateException(
                "Bean-managed transaction"
            );
        try {
            return this.transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException exception) {
            throw new RuntimeException(exception.getMessage());
        }
        }

        /**
	 * No security support
	 */
        private final static Principal anonymous = new Anonymous();



    //------------------------------------------------------------------------
    // Since JRE 1.4
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.ejb.EJBContext#getTimerService()
     */
    public TimerService getTimerService() throws IllegalStateException {
                throw new IllegalStateException("TimerService not yet supported");
    }

    /* (non-Javadoc)
     * @see javax.ejb.SessionContext#getMessageContext()
     */
    public MessageContext getMessageContext() throws IllegalStateException {
        throw new IllegalStateException("EJB has not been invoked through a WebService endpoint");
    }

    /* (non-Javadoc)
     * @see javax.ejb.SessionContext#getBusinessObject(java.lang.Class)
     */
    public Object getBusinessObject(Class arg0)
        throws IllegalStateException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.ejb.SessionContext#getInvokedBusinessInterface()
     */
    public Class getInvokedBusinessInterface()
        throws IllegalStateException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.ejb.EJBContext#lookup(java.lang.String)
     */
    public Object lookup(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}

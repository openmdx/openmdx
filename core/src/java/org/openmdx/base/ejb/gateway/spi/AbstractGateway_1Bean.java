/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractGateway_1Bean.java,v 1.8 2008/02/10 01:21:52 hburger Exp $
 * Description: Gateway_1 Session Bean
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/10 01:21:52 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.ejb.gateway.spi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.rmi.PortableRemoteObject;

import org.openmdx.base.exception.NamingExceptions;
import org.openmdx.base.exception.RemoteExceptions;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.io.Resettable;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.security.ExecutionContext;

/**
 * Gateway_1 Session Bean
 */
public abstract class AbstractGateway_1Bean implements SessionBean {

    /**
     * The EJB's <code>SessionContext</code> object.
     */
    private SessionContext sessionContext;

    /**
     * Retrieve the sessionContext.
     * 
     * @return the <code>sessionContext</code>'s value
     */
    protected final SessionContext getSessionContext() {
        return sessionContext;
    }

    /**
     * Retrieve the gateways's execution context
     */
    protected abstract ExecutionContext getExecutionContext();

    /**
     * Retrieve the executionEnvironment.
     * 
     * @return the <code>executionEnvironment</code>'s value
     */
    protected abstract Hashtable<?,?> getExecutionEnvironment();
    
    /**
     * Create a new <code>InitialContext</code>
     * 
     * @return the newly created <code>InitialContext</code>
     * 
     * @throws NamingException 
     */
    protected Context newInitialContext() throws NamingException {
        ExecutionContext executionContext = getExecutionContext();
        Hashtable<?,?> executionEnvironment = getExecutionEnvironment();
        return executionContext instanceof InitialContextFactory ?
            ((InitialContextFactory)executionContext).getInitialContext(executionEnvironment) :
            new InitialContext(executionEnvironment);
    }
    
    
    //------------------------------------------------------------------------
    // Implements SessionBean
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(
        SessionContext sessionContext
    ) throws EJBException, RemoteException {
        this.sessionContext = sessionContext;
    }

    /**
     * This method corresponds to the create method in the home interface.
     * The parameter sets of the two methods are identical. When the client calls
     * <code>create()</code>, the container allocates an instance of
     * the EJBean and calls <code>ejbCreate()</code>.
     *
     * @exception               javax.ejb.CreateException if there is
     *                          a problem creating the bean
     */
    public void ejbCreate(
    ) throws CreateException {
        try {
            Context initialContext = new InitialContext();
            try {
                Context beanContext = (Context) initialContext.lookup("java:comp/env");
                try {
                    create(beanContext);
                } finally {                    
                    beanContext.close();
                } 
            } catch (NamingException exception) {
                throw (CreateException) new CreateException(
                    "Component context failure"
                ).initCause(exception);
            } finally {
                initialContext.close();
            } 
        } catch (NamingException exception) {
            throw (CreateException) new CreateException(
                "Initial context failure"
            ).initCause(exception);
        }
    }

    /**
     * This method may be overriden by subclasses.
     * 
     * @param beanContext
     * 
     * @exception               javax.ejb.CreateException if there is
     *                          a problem creating the bean
     */
    protected void create(
        Context beanContext
    ) throws CreateException {
        //
    }
    
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() throws EJBException, RemoteException {
        this.sessionContext = null;
    }

    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() throws EJBException, RemoteException {
        //
    }

    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() throws EJBException, RemoteException {
        //
    }

    /**
     * This method defines the class' or sub-class' retry policy.
     * 
     * @return <code>true</code> if the request should be retried once.
     */
    protected boolean isRetriable(
        PrivilegedActionException privilegedActionException
    ){
        Exception exception = privilegedActionException.getException();
        return (
            exception instanceof RemoteException &&
            RemoteExceptions.isRetriable((RemoteException) exception)
        ) || (
            exception instanceof NamingException &&
            NamingExceptions.isRetriable((NamingException) exception)
        );
    }
    
    
    //------------------------------------------------------------------------
    // Implements Gateway_1_0
    //------------------------------------------------------------------------

    /**
     * Internal session bean accessor factory
     */
    protected Object newStatelessSessionBeanAccessor(
        final Gateway_1_0Internal gateway,
        final Class<?> homeInterface, 
        final Class<?> remoteInterface, 
        final String id
    ) throws ServiceException {
        attempts: for(
            int 
                attempt = TRY_WITH_CURRENT_CONTEXT, 
                attemptLimit = TRY_WITH_CURRENT_CONTEXT;;
        ) try {
            Object[] bean = (Object[]) getExecutionContext().execute(

                new PrivilegedExceptionAction<Object>() {
   
                   public Object run(
                   ) throws Exception {
                       Context initialContext = newInitialContext();
                       try {
                           EJBObject bean = (EJBObject) homeInterface.getDeclaredMethod(
                              "create", 
                              (Class[])null
                           ).invoke(
                              PortableRemoteObject.narrow(
                                   initialContext.lookup(id),
                                   homeInterface
                               ), 
                               (Object[])null
                           );
                           return new Object[]{
                               bean,
                               bean.getHandle()
                           };
                       } catch (InvocationTargetException exception) {
                           Throwable throwable = exception.getTargetException();
                           throw throwable instanceof Exception ? 
                               (Exception) throwable :
                               new UndeclaredThrowableException(throwable);
                       } finally {
                           initialContext.close();
                       }
                   }                 
                    
                }
                
            );
            return Classes.newProxyInstance(
                new RemoteInvocationHandler(
                    gateway,
                    (EJBObject) bean[0],
                    (Handle) bean[1],
                    getResetToken()                    
                ),
                remoteInterface                 
            );
        } catch (PrivilegedActionException exception) {
            int failedAttempt = attempt++;
        	if(isRetriable(exception)) {
                switch(failedAttempt) {
	                case TRY_WITH_CURRENT_CONTEXT:
	                    ExecutionContext executionContext = getExecutionContext();
	                    if(executionContext instanceof Resettable) {
	                        ((Resettable)executionContext).reset();
	                        attempt = TRY_WITH_RESET_CONTEXT;
	                        attemptLimit = RETRY_WITH_RESET_CONTEXT;
	                    } else {
	                        attemptLimit = RETRY_WITH_CURRENT_CONTEXT;
	                    }
                }
        		if(attempt <= attemptLimit) {
                    SysLog.detail(RETRY_MESSAGE[failedAttempt], exception);
                    continue attempts;
                }
        	}        	
            throw new ServiceException(
                exception.getException(),
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.COMMUNICATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("id", id),
                    new BasicException.Parameter("home", homeInterface.getName()),
                    new BasicException.Parameter("remote", remoteInterface.getName())
                },
                ABORT_MESSAGE[failedAttempt]
            );
        } catch (RuntimeException exception) {
            new RuntimeServiceException(exception).log();
            throw exception; // Destroy the EJB
        }
    }

    /**
     * Create a proxy for a stateless session bean
     * 
     * @param homeInterface 
     *        the interface to lookup the delegate object
     * @param remoteInterface 
     *        the interface to be implemented by the proxy instance
     * @param id The stateless session bean's id
     * 
     * @return a proxy for the requested stateless session bean
     * 
     * @throws ServiceException if accessor creation fails
     */
    public Object newStatelessSessionBeanAccessor(
        final Class<?> homeInterface, 
        final Class<?> remoteInterface, 
        final String id
    ) throws ServiceException {
        return newStatelessSessionBeanAccessor(
            (Gateway_1_0Internal) getSessionContext().getEJBLocalObject(),
            homeInterface,
            remoteInterface,
            id
        );
    }
    

    //------------------------------------------------------------------------
    // Implements Gateway_1_0Internal
    //------------------------------------------------------------------------

    /**
     * Invocation
     * <p>
     * Attempts:<ol>
     * <li>Use current connection and EJB object
     * <li>Retrieve a new EJB object from its handle
     * <li>Reset the execution context and retrieve a new EJB object from its 
     *     handle
     * </ol>
     */
    public Object invoke(
        final Object proxy, 
        final Method method, 
        final Object[] args
    ) throws PrivilegedActionException {
        final RemoteInvocationHandler handler = (RemoteInvocationHandler) Proxy.getInvocationHandler(
            proxy
        );
        attempts: for(
            int 
                attempt = TRY_WITH_CURRENT_CONTEXT,
                attemptLimit = this.getResetToken() == handler.getResetToken() ? 
                    RETRY_WITH_CURRENT_CONTEXT : 
                    TRY_WITH_CURRENT_CONTEXT;;
        ) try {
            return getExecutionContext().execute(

                new PrivilegedExceptionAction<Object>() {
   
                    public Object run() throws Exception {
                        try {
                            return method.invoke(
                                handler.getTargetObject(getResetToken()),
                                args
                            );
                        } catch (InvocationTargetException exception) {
                            Throwable throwable = exception.getTargetException();
                            throw throwable instanceof Exception ? 
                                (Exception) throwable :
                                new UndeclaredThrowableException(throwable);
                        }                        
                    }
                }

            );
        } catch (SecurityException exception) {
            throw new PrivilegedActionException(
                 new ConnectException(
                     "Connection could not be established",
                     exception
                 ) // a RemoteExeption
            );
        } catch (PrivilegedActionException exception) {
            int failedAttempt = attempt++;
            if(isRetriable(exception)) {
                switch (failedAttempt) {
                    case TRY_WITH_CURRENT_CONTEXT:
                    case RETRY_WITH_CURRENT_CONTEXT:
	                    if(attempt <= attemptLimit) {
	                        handler.reset();
	                    } else {
		                    ExecutionContext executionContext = getExecutionContext();
		                    if(executionContext instanceof Resettable) {
		                        ((Resettable)executionContext).reset();
		                        attempt = TRY_WITH_RESET_CONTEXT;
		                        attemptLimit = RETRY_WITH_RESET_CONTEXT;
		                    }
	                    }
                }
                if(attempt <= attemptLimit) {
                    SysLog.detail(RETRY_MESSAGE[failedAttempt], exception);
                    continue attempts;
                }
            }           
            throw exception;
        } catch (RuntimeException exception) {
            new RuntimeServiceException(exception).log();
            throw exception; // Destroy the EJB
        }

    }

    /**
     * Retrieve the reset token
     * 
     * @return the reset token; or <code>-1</code> if the 
     * <code>ExecutuionContex</code> is not resettable.
     */
    long getResetToken(){
        ExecutionContext executionContext = getExecutionContext();
        return executionContext instanceof Resettable ?
            ((Resettable)executionContext).getResetToken() :
            -1L;    
    }

    /**
     * Retry the same method invocation with the current context
     */
    protected static final int TRY_WITH_CURRENT_CONTEXT = 0;

    /**
     * Retry the same method invocation with the current context
     */
    protected static final int RETRY_WITH_CURRENT_CONTEXT = 1;

    /**
     * Try the same method invocation with a reset context
     */
    protected static final int TRY_WITH_RESET_CONTEXT = 2;

    /**
     * Retry the same method invocation with a reset context
     */
    protected static final int RETRY_WITH_RESET_CONTEXT = 3;

    /**
     * Failure lof messages
     */
    private final static String[] RETRY_MESSAGE = new String[]{
        "Trying the method invocation with the current context failed, going to retry the same method invocation with the current context",
        "Retrying the same method invocation with the current context failed, going to try the same method invocation with a reset context",
        "Trying the same method invocation with a reset context failed, going to retry the same method invocation with this reset context",
        null
    };

    /**
     * Failure lof messages
     */
    private final static String[] ABORT_MESSAGE = new String[]{
        null,
        "Retrying to retrieve the remote EJB object failed with the current context which is not resettable",
        null,
        "Retrying to retrieve the remote EJB object failed with a reset context"
    };

}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SessionBean_1.java,v 1.16 2008/09/10 08:55:25 hburger Exp $
 * Description: SessionBean_1 class 
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
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
package org.openmdx.compatibility.base.application.j2ee;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.ApplicationContext_1_0;
import org.openmdx.compatibility.base.application.cci.CommandOptions_1_0;
import org.openmdx.compatibility.base.application.cci.ConfigurationProvider_1_0;
import org.openmdx.compatibility.base.application.cci.ConfigurationSpecifier;
import org.openmdx.compatibility.base.application.cci.Manageable_1_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.application.spi.AbstractApplicationContext_1;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean_1 is a SessionBean according to EJB 1.1 specification.
 */
abstract public class SessionBean_1 
implements SessionBean, Manageable_1_0, 
CommandOptions_1_0, ConfigurationProvider_1_0
{

    /**
     * Identification
     */
    private String instanceId;

    /**
     * J-Engine access 
     */
    private ApplicationContext_1_0 applicationContext;

    /**
     * 
     */
    private Context configurationContext;

    /**
     * J-Engine access management
     * 
     * @return      WebLogic J-Engine access
     */
    protected final ApplicationContext_1_0 getApplicationContext (
    ) throws ServiceException {
        return this.applicationContext;
    }

    protected Context getConfigurationContext(){
        return this.configurationContext;
    }

    /**
     * To replace the deprecated UIDFactory.create() calls
     * 
     * @return a UUID as strong
     */
    protected final String uuidAsString(
    ){
        return this.uuidGenerator.next().toString();
    }

    /**
     * 
     */
    protected Logger logger;


    //------------------------------------------------------------------------
    // Home interface
    //------------------------------------------------------------------------

    /**
     * This method corresponds to the create method in the home interface.
     * The parameter sets of the two methods are identical. When the client calls
     * <code>create()</code>, the container allocates an instance of
     * the EJBean and calls <code>ejbCreate()</code>.
     *
     * @exception               javax.ejb.CreateException if there is
     *                          a problem creating the bean
     */
    public void ejbCreate() throws CreateException {
        this.logger = LoggerFactory.getLogger(getClass());
        try {
            try {
                Context initialContext = new InitialContext(); 
                this.configurationContext = (Context)initialContext.lookup(BEAN_ENVIRONMENT);
                this.options = getConfiguration(
                    null,
                    configurationSpecification()
                );
            } catch (Exception exception) {
                throw (CreateException) Throwables.initCause(
                    new CreateException("Error retrieving session bean options"),
                    exception, 
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ACTIVATION_FAILURE, 
                    null,
                    new BasicException.Parameter("class", getClass().getName()),
                    new BasicException.Parameter("context", BEAN_ENVIRONMENT)
                );
            }
            try {
                this.logger.trace("Creating session bean {}: {}", this.instanceId, this.options);
                activate();
                this.logger.info("Session bean {} created: {}", this.instanceId, this.options);
            } catch (Exception exception) {
                throw (CreateException) Throwables.initCause(
                    new CreateException("Creation of session bean " + this.instanceId + " failed"),
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ACTIVATION_FAILURE, 
                    null,
                    this.options.toExceptionParameters()
                );
            }
        } catch (CreateException exception) {
            this.logger.error(
                exception.getMessage(), 
                exception.getCause()
            );
            throw exception;
        } catch (Error error) {
            BasicException assertionFailure = new BasicException(
                error,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ASSERTION_FAILURE,
                "Creation of session bean " + this.instanceId + " failed"
            );
            this.logger.error(
                assertionFailure.getMessage(), 
                assertionFailure.getCause()
            );
            throw error;
        }
    }


    //------------------------------------------------------------------------
    // SessionBean interface
    //------------------------------------------------------------------------

    /**
     * 
     */
    private  UUIDGenerator uuidGenerator = null;

    /**
     * Session context
     */
    private SessionContext ctx;

    /**
     * Set the associated session context. The container calls this method
     * after the instance creation. 
     * <p>
     * The enterprise Bean instance should store the reference to the context
     * object in an instance variable. 
     * <p>
     * This method is called with no transaction context.
     *
     * @param ctx   A SessionContext interface for the instance.
     */
    public void setSessionContext(SessionContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Get the associated session context.  
     *
     * @return      The SessionContext interface for the instance.
     */
    protected SessionContext getSessionContext(
    ) {
        return this.ctx;
    }

    /**
     * The activate method is called when the instance is activated from its
     * "passive" state. The instance should acquire any resource that it has
     * released earlier in the ejbPassivate() method. 
     * <p>
     * This method is called with no transaction context.
     * 
     * @exception   EJBException    Thrown by the method to indicate a
     *                              failure caused by a system-level error.
     */
    public void ejbActivate() {
        //
    }

    /**
     * The passivate method is called before the instance enters the
     * "passive" state. The instance should release any resources that it
     * can re-acquire later in the ejbActivate() method. 
     * <p>
     * After the passivate method completes, the instance must be in a state
     * that allows the container to use the Java Serialization protocol to
     * externalize and store away the instance's state.
     * <p>
     * This method is called with no transaction context.
     * 
     * @exception   EJBException    Thrown by the method to indicate a
     *                              failure caused by a system-level error.
     */
    public void ejbPassivate() {
        //
    }

    /**
     * A container invokes this method before it ends the life of the
     * session object. This happens as a result of a client's invoking
     * a remove operation, or when a container decides to terminate the
     * session object after a timeout. 
     * <p>
     * This method is called with no transaction context.
     * 
     * @exception   EJBException    Thrown by the method to indicate a
     *                              failure caused by a system-level error.
     */
    public void ejbRemove() {
        try {
            this.logger.trace(
                "Removing session bean {}: {}",
                this.instanceId,
                this.options
            );
            deactivate();
            this.configurationContext.close();
            this.logger.info(
                "Session bean {} removed: {}",
                this.instanceId,
                this.options
            );
        } catch (Exception exception) {
            new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.DEACTIVATION_FAILURE, 
                "Removal of session bean " + this.instanceId + " failed",
                this.options.toExceptionParameters()
            ).log();
        }
    }


    //------------------------------------------------------------------------
    // Manageable_1_0 interface
    //------------------------------------------------------------------------

    /**
     * The activate method is called upon the the bean's creation
     * and can be overriden by a subclass.
     * <p>
     * An activate() implementation of a subclass must be
     * of the form:
     * <pre>
     *   {
     *     super.activate();
     *     \u00ablocal activation code\u00bb
     *   }
     */
    public void activate(
    ) throws Exception {
        this.uuidGenerator = UUIDs.getGenerator();
        this.instanceId = uuidAsString();
        this.applicationContext = AbstractApplicationContext_1.getInstance();
    }

    /**
     * The deactivate method is called upon the the bean's removal
     * and can be overriden by a subclass.
     * <p>
     * A deactivate() implementation of a subclass must be
     * of the form:
     * <pre>
     *   {
     *     \u00ablocal deactivation code\u00bb
     *     super.deactivate();
     *   }
     * </pre>
     */
    public void deactivate(
    ) throws Exception {
        //
    }


    //------------------------------------------------------------------------
    // Implements CommandOptions_1_0
    //------------------------------------------------------------------------

    /**
     * The JNDI location of the bean's context
     */
    final static public String BEAN_ENVIRONMENT = "java:comp/env"; 

    /**
     * SessionBeans_1 instances have no parameters
     */
    final static private String[] BEAN_PARAMETERS = {}; 


    /**
     * Command options buffer
     */
    private Configuration options = null;

    /**
     * Get the command options.
     * 
     * @return  A property set containing the command options.
     */
    public Configuration getOptions (
    ) {
        return this.options;
    }

    /**
     * Get the parameters.
     * 
     * @return  An array containing the parameters.
     */
    public String [] getParameters () {
        return BEAN_PARAMETERS;
    }

    /**
     * Define whether the applications task should be executed or not.
     * 
     * @return  False if for example a help request has been processed,
     *          true otherwise. 
     */
    public boolean isExecutable () {
        return true;
    }


    //------------------------------------------------------------------------
    // Implements ConfigurationProvider_1_0
    //------------------------------------------------------------------------

    /**
     * Get a specific configuration
     *
     * @param       section
     *              the section to be parsed, my be null
     * @param       specification
     *              a map of id/ConfigurationSpecifier entries, may be null
     * 
     * @return      the requested configuration
     *
     * @exception   ServiceException
     *              if the actual configuration does not match the 
     *              specification
     */
    @SuppressWarnings("unchecked")
    public Configuration getConfiguration(
        String[] section,
        Map<String,ConfigurationSpecifier> specification
    ) throws ServiceException {
        return new EjbConfiguration(
            getConfigurationContext(),
            section,
            specification
        );      
    }

    /**
     * Define a default implementation of configurationSpecifier
     *
     * @return      a map of id/ConfigurationSpecifier entries
     */
    protected Map<String,ConfigurationSpecifier> configurationSpecification() {
        return new HashMap<String,ConfigurationSpecifier>();
    }


    //------------------------------------------------------------------------
    // Prerequisits
    //------------------------------------------------------------------------

    static {
        AbstractApplicationContext_1.getInstance();
    }  	

}

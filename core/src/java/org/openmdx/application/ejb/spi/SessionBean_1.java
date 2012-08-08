/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SessionBean_1.java,v 1.4 2009/06/09 12:45:19 hburger Exp $
 * Description: Abstract Session Bean 1 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:19 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.ejb.spi;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.openmdx.application.cci.ConfigurationProvider_1_0;
import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.LoggerFactory;

/**
 * SessionBean_1 is a SessionBean according to EJB 1.1 specification.
 */
public abstract class SessionBean_1 
    implements SessionBean, ConfigurationProvider_1_0
{

    /**
     * Constructor 
     */
    protected SessionBean_1() {
        super();
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 8493439702909430139L;

    /**
     * Identification
     */
    private String instanceId;

    /**
     * the configuration context
     */
    private Context configurationContext;

    /**
     * The instance specific logger
     */
    private Logger logger;

    /**
     * The JNDI location of the bean's context
     */
    final static public String BEAN_ENVIRONMENT = "java:comp/env"; 

    /**
     * Command options buffer
     */
    private Configuration options = null;

    /**
     * Session context
     */
    private SessionContext ctx;

    /**
     * The MOF repository proxy
     */
    private transient Model_1_0 model = null;
    
    /**
     * Retrieve the configuration context
     * 
     * @return the configuration context
     */
    protected Context getConfigurationContext(){
        return this.configurationContext;
    }

    /**
     * Retrieve logger.
     *
     * @return Returns the logger.
     */
    protected Logger getLogger() {
        return this.logger;
    }


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
        this.logger = LoggerFactory.getLogger();
        try {
            try {
                Context initialContext = new InitialContext(); 
                this.configurationContext = (Context)initialContext.lookup(BEAN_ENVIRONMENT);
                this.options = getConfiguration(
                    null,
                    configurationSpecification()
                );
            } catch (Exception exception) {
                throw Throwables.initCause(
                    new CreateException("Error retrieving session bean options"),
                    exception, 
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ACTIVATION_FAILURE, 
                    new BasicException.Parameter("class", getClass().getName()),
                    new BasicException.Parameter("context", BEAN_ENVIRONMENT)
                );
            }
            try {
                Object[] logParameters = new Object[]{this.instanceId, this.options}; 
                this.logger.log(
                    Level.FINEST, 
                    "Creating session bean {0}: {1}", 
                    logParameters
                );
                this.activate();
                this.logger.log(
                    Level.INFO, 
                    "Session bean {0} created: {1}", 
                    logParameters
                );
            } catch (Exception exception) {
                throw Throwables.initCause(
                    new CreateException("Creation of session bean " + this.instanceId + " failed"),
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ACTIVATION_FAILURE, 
                    this.options.toExceptionParameters()
                );
            }
        } catch (CreateException exception) {
            this.logger.log(
                Level.SEVERE,
                exception.getMessage(), 
                exception.getCause()
            );
            throw exception;
        } catch (Error error) {
            BasicException assertionFailure = BasicException.newStandAloneExceptionStack(
                error, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Creation of session bean " + this.instanceId + " failed",
                new BasicException.Parameter("instanceId", instanceId)
            );
            this.logger.log(
                Level.SEVERE,
                assertionFailure.getMessage(), 
                assertionFailure
            );
            throw error;
        }
    }


    //------------------------------------------------------------------------
    // SessionBean interface
    //------------------------------------------------------------------------

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
            Object[] logParameters = new Object[]{this.instanceId, this.options};
            this.logger.log(
                Level.FINEST,
                "Removing session bean {0}: {1}",
                logParameters
            );
            this.deactivate();
            this.configurationContext.close();
            this.logger.log(
                Level.INFO,
                "Session bean {0} removed: {1}",
                logParameters
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
    // 
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
    protected void activate(
    ) throws Exception {
        this.instanceId = UUID.randomUUID().toString();
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
    protected void deactivate(
    ) throws Exception {
        //
    }


    //------------------------------------------------------------------------
    // Implements ConfigurationProvider_1_0
    //------------------------------------------------------------------------

    /**
     * Get a specific configuration
     *
     * @param       section
     *              the section to be parsed
     * 
     * @return      the requested configuration
     */
    protected Configuration getConfiguration(
        String...  section
    ) throws ServiceException {
        return getConfiguration(section, null);
    }
    
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
    public Configuration getConfiguration(
        String[] section,
        Map<String,ConfigurationSpecifier> specification
    ) throws ServiceException {
        Configuration configuration = new EnvironmentConfiguration(
            getConfigurationContext(),
            section,
            specification
        );   
        if((section != null) && (section.length > 0)) {
            try {
                String name = "";
                for(String n: section) {
                    name += n + ".";
                }
                for(
                    Enumeration<URL> resources = Classes.getResources("META-INF/" + name + "properties");
                    resources.hasMoreElements();
                ) {
                    URL resource = resources.nextElement();
                    Properties properties = new Properties();
                    try {
                        properties.load(resource.openStream());
                        for(
                            Enumeration<?> propertyNames = properties.propertyNames();
                            propertyNames.hasMoreElements();
                        ) {
                            Object key = propertyNames.nextElement();
                            if(key instanceof String) {
                                String propertyName = (String)key;
                                configuration.setValue(
                                    propertyName,
                                    properties.getProperty(propertyName),
                                    true
                                );
                            }
                        }
                    }
                    catch(Exception e) {}
                }
            }
            catch(Exception e) {}
        }
        return configuration;
    }

    /**
     * Define a default implementation of configurationSpecifier
     *
     * @return      a map of id/ConfigurationSpecifier entries
     */
    protected Map<String,ConfigurationSpecifier> configurationSpecification() {
        return new HashMap<String,ConfigurationSpecifier>();
    }
    
    /**
     * Retrieve indexed env entries
     * 
     * @param context
     * @param name
     * 
     * @return the indexd env entry set
     */
    protected Iterable<Map.Entry<Integer,String>> envEntries(
        final String context,
        final String name
    ){
        return new Iterable<Map.Entry<Integer,String>>(){

            @SuppressWarnings("unchecked")
            public Iterator<Map.Entry<Integer, String>> iterator() {
                try {
                    return new EnvironmentIterator(context, name);
                } catch (NamingException exception) {
                    return Collections.EMPTY_SET.iterator();
                }
            }
            
        };
    }

    /**
     * Retrieve a MOF repository proxy
     * 
     * @return a MOF repository proxy
     */
    protected Model_1_0 getModel(){
        return this.model == null ? this.model = Model_1Factory.getModel() : this.model;
    }
        
    
    //------------------------------------------------------------------------
    // Class EnvironmentEntries
    //------------------------------------------------------------------------

    /**
     * Environment Iterator
     */
    class EnvironmentIterator implements Iterator<Map.Entry<Integer,String>> {

        /**
         * Constructor 
         *
         * @param context
         * @param name
         * @throws NamingException
         */
        EnvironmentIterator(
            final String context,
            final String name
        ) throws NamingException{
            this.pattern = Pattern.compile(name + "\\[(\\d)\\]");
            this.delegate = getConfigurationContext().list(context);
            this.prefix = BEAN_ENVIRONMENT + '/' + context + '/';
        }

        /**
         * 
         */
        private final Enumeration<NameClassPair> delegate;
        
        /**
         * 
         */
        private final Pattern pattern;
        
        /**
         * 
         */
        private final String prefix;
        
        /**
         * Prefetched element
         */
        private Map.Entry<Integer, String> next = null;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext(
        ) {
            while(this.next == null && this.delegate.hasMoreElements()) {
                NameClassPair ncp = this.delegate.nextElement();
                String n = ncp.getName();
                if(
                    SharedConfigurationEntries.WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS || 
                    !ncp.isRelative()
                ) {
                    n = n.substring(n.lastIndexOf('/') + 1);
                }
                final String value = this.prefix + n;
                Matcher m = this.pattern.matcher(n);
                if(m.matches()) {
                    final Integer key = Integer.valueOf(m.group(1));
                    this.next = new Map.Entry<Integer, String>(){

                        public Integer getKey() {
                            return key;
                        }

                        public String getValue() {
                            return value;
                            
                        }

                        public String setValue(String value) {
                            throw new UnsupportedOperationException(
                                "Env entries can't be modified"
                            );
                        }
                        
                    };
                    return true;
                }
            }
            return this.next != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public Map.Entry<Integer, String> next() {
            if(hasNext()) {
                Map.Entry<Integer, String> next = this.next;
                this.next = null;
                return next;
            } else throw new NoSuchElementException(
                "There are no more matching environment entries left"
            );
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException(
                "Env entries can't be modified"
            );
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class EnvironmentConfiguration
    //------------------------------------------------------------------------
    
    /**
     * Command option parser
     */
    static class EnvironmentConfiguration extends Configuration { 

        /**
         * Constructor
         *
         * @param       source
         *              the ejb context
         * @param       section
         *              the section to be parsed, my be null
         * @param       specification
         *              the configurations specification, may be null
         */
        @SuppressWarnings("unchecked")
        EnvironmentConfiguration(
            Context context,
            String[] section,
            Map specification
        ) throws ServiceException {
            super();
            try {
                Context base = context;
                if(section != null) for(
                        int i = 0, iLimit = section.length - 1;
                        i < iLimit;
                        i++
                ){
                    base = (Context) base.lookup(section[i]);
                }
                String sectionName =
                    section == null ? "" : section[section.length - 1];
                for(
                        NamingEnumeration bindings = base.listBindings(sectionName);
                        bindings.hasMore();
                ) try {
                    Binding binding = (Binding)bindings.next();
                    Object value = binding.getObject();
                    String name = binding.getName();
                    if(
                        SharedConfigurationEntries.WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS || 
                        !binding.isRelative()
                    ) {
                        name = name.substring(name.lastIndexOf('/') + 1);
                    }
                    if (
                        value instanceof String ||
                        value instanceof Boolean ||
                        value instanceof Integer ||
                        value instanceof Long ||
                        value instanceof Short
                    ) {
                        this.setValue(name,value, false);
                    }
                } catch (NamingException exception) {
                    throw new ServiceException (
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.ACTIVATION_FAILURE, 
                        "Naming exception in section",
                        new BasicException.Parameter("section", Arrays.toString(section))
                    );
                }
            } catch (NamingException exception) {
                // The section has no entries
            }
        }

    }
    
}

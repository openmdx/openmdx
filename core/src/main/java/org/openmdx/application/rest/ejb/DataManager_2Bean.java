/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Manager 2 Bean 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.application.rest.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
#if JAVA_8
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.Interaction;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;
#else
import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionSpec;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.InteractionSpec;
import jakarta.resource.cci.Record;
#endif
import org.openmdx.application.rest.adapter.InboundConnectionFactory_2;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * Data Manager 2 Bean
 */
public class DataManager_2Bean implements SessionBean {
    
    /**
     * Implements {@link Serializable}
     */
    private static final long serialVersionUID = -1593128027032912087L;

    /**
     * A reference to the shared connection factory
     */
    private ConnectionFactory connectionFactory;
    
    /**
     * The connection factory is shared among the bean instances
     */
    private static final ConcurrentMap<String,ConnectionFactory> connectionFactories = new ConcurrentHashMap<String, ConnectionFactory>();
    
    /**
     * A connection is established when the bean is created
     */
    private transient Connection connection = null;
    
    /**
     * The EJB session context
     */
//  private SessionContext sessionContext;

    /**
     * The REST connection spec
     */
    private RestConnectionSpec connectionSpec;

    /**
     * Connect to an entity manager
     * 
     * @throws EJBException
     */
    private void connect(
    ) throws EJBException {
        if(this.connection == null) {
            try {
                this.connection = connectionFactory.getConnection(
                    this.connectionSpec
                );
            } catch (ResourceException exception) {
                throw Throwables.initCause(
                    new EJBException("Entity manager acquisition failure"),
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ACTIVATION_FAILURE
                );
            }
        }
    }

    /**
     * Disconnect from the entity manager
     */
    private void disconnect(
    ) throws EJBException {
        if(this.connection != null) {
            try {
                this.connection.close();
            } catch (ResourceException exception) {
                throw Throwables.initCause(
                    new EJBException("Entity manager disposal failure"),
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.DEACTIVATION_FAILURE
                );
            } finally {
                this.connection = null;
            }
        }
    }

    /**
     * A {@link Connection_2Home#create()} invocation leads here
     * 
     * @param         connectionSpec the REST {@code {@code ConnectionSpec} 
     * 
     * @throws CreateException
     */
    public void ejbCreate(
        ConnectionSpec connectionSpec
    ) throws CreateException {
        String name = "jdo:EntityManagerFactory";        
        Map<String,Object> overrides = new HashMap<String, Object>();
        try {
            Bindings: for(
                NamingEnumeration<Binding> e = new InitialContext().listBindings("java:comp/env");
                e.hasMore();
             ){
                Binding b = e.nextElement();
                for(ConfigurableProperty p : ConfigurableProperty.values()){
                    if(p.name().equals(b.getName())) {
                        if(p != ConfigurableProperty.Name) {
                            overrides.put(p.qualifiedName(), b.getObject());
                        } else if(DataManager_2Bean.connectionFactories.containsKey(name = (String)b.getObject())) {
                            break Bindings;
                        }
                        continue Bindings;
                    }
                }
             }
        } catch (NamingException exception) {
            throw Throwables.initCause(
                new CreateException("Unable to retrieve the entity manager configuration"),
                exception, // cause
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                new BasicException.Parameter(
                    "context", 
                    "java:comp/env"
                )
            );
        }
        this.connectionFactory = DataManager_2Bean.connectionFactories.get(name);
        if(this.connectionFactory == null) {
            this.connectionFactory = Maps.putUnlessPresent(
                DataManager_2Bean.connectionFactories,
                name,
                InboundConnectionFactory_2.newInstance(name, overrides)
            );
        }
        try {
            this.connectionSpec = (RestConnectionSpec) connectionSpec;
        } catch (ClassCastException exception) {
            throw Throwables.initCause(
                new CreateException("Invalid ConnectionSpec"),
                exception, // cause
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter(
                    "expected", 
                    RestConnectionSpec.class.getName()
                ),
                new BasicException.Parameter(
                    "actual", 
                    connectionSpec == null ? null : connectionSpec.getClass().getName()
                )
            );
        }
        connect();
    }

    
    //------------------------------------------------------------------------
    // Implements SessionBean
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    @Override
    public void ejbActivate(
    ) throws EJBException, RemoteException {
        // TODO restore unit of work
        connect();
    }

    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    @Override
    public void ejbPassivate(
    ) throws EJBException, RemoteException {
        // TODO save unit of work
        disconnect();
    }

    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    @Override
    public void ejbRemove(
    ) throws EJBException, RemoteException {
        disconnect();
    }

    /* (non-Javadoc)
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    @Override
    public void setSessionContext(
        SessionContext sessionContext
    ) throws EJBException, RemoteException {
//      this.sessionContext = sessionContext;
    }

    
    //------------------------------------------------------------------------
    // Provides Connection 2.0
    //------------------------------------------------------------------------
    
    /**
     * Connection 2.0's execute method
     * 
     * @param ispec
     * @param input
     * 
     *  @return  output Record if execution of the EIS function has been 
     *           successful; null otherwise
     * 
     * @throws ResourceException
     */
    public Record execute(
        InteractionSpec ispec, 
        Record input
    ) throws ResourceException {
        Interaction interaction = this.connection.createInteraction();
        try {
            return interaction.execute(ispec, input);
        }  finally {
            interaction.close();
        }
    }
    
}

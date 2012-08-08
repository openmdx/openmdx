/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceManagerFactory_1.java,v 1.11 2008/02/29 18:02:03 hburger Exp $
 * Description: Persistence Manager Factory 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 18:02:03 $
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.JDOConnection;

import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.accessor.jmi.cci.RefPackageFactory_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.object.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.object.spi.InstanceLifecycleNotifier;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.LateBindingConnection_1;
import org.openmdx.compatibility.base.dataprovider.cci.QualityOfService;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.kernel.exception.BasicException;

/**
 * Persistence Manager Factory
 *
 * @since openMDX 1.13
 */
public class PersistenceManagerFactory_1 
    extends AbstractPersistenceManagerFactory 
    implements JDOConnection
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6593277236725994669L;

    /**
     * Constructor 
     *
     * @param configuration
     */
    private PersistenceManagerFactory_1(
        Map<String,Object> configuration
    ) {
        super(configuration);
        this.refPackageFactory = null;
    }

    /**
     * Constructor 
     *
     * @param refPackageFactory
     */
    PersistenceManagerFactory_1(
        RefPackageFactory_1_0 refPackageFactory        
    ) {
        super(EMPTY_CONFIGURATION);
        this.refPackageFactory = refPackageFactory;
        freeze();
    }

    /**
     * This constant avoids type casting
     */
    private static final Map<String,Object> EMPTY_CONFIGURATION = Collections.emptyMap();
    
    /**
     * 
     */
    private RefPackageFactory_1_0 refPackageFactory;

    /**
     * 
     */
    private transient Dataprovider_1_1Connection connection;

    /**
     * Get instance
     * 
     * @param properties
     * 
     * @return a new instance
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory(
        Map<String,Object> properties
    ){
        PersistenceManagerFactory_1 persistenceManagerFactory = new PersistenceManagerFactory_1(
           properties
        );
        if(properties.containsKey(Dataprovider_1ConnectionFactory.class.getName())) {
            Dataprovider_1ConnectionFactory connectionFactory = (Dataprovider_1ConnectionFactory)properties.get(
                Dataprovider_1ConnectionFactory.class.getName()
            );
            persistenceManagerFactory.setConnectionFactory(connectionFactory);
        }
        persistenceManagerFactory.freeze();
        return persistenceManagerFactory;
    }

    
    //------------------------------------------------------------------------
    // Implements JDOConnection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.AbstractPersistenceManagerFactory#close()
     */
    public synchronized void close() {
        if(this.connection != null) this.connection.close();
        super.close();
    }

    /* (non-Javadoc)
     * @see javax.jdo.datastore.JDOConnection#getNativeConnection()
     */
    public Object getNativeConnection() {
        return this.connection;
    }
    
    
    //------------------------------------------------------------------------
    // Extends AbstractPersistenceManagerFactory
    //------------------------------------------------------------------------

    /**
     * 
     */
    private synchronized Dataprovider_1_1Connection getConnection(
    ) throws ServiceException{
        if(this.connection == null) {
            Dataprovider_1ConnectionFactory connectionFactory = (Dataprovider_1ConnectionFactory) this.getConnectionFactory();
            if(connectionFactory == null) {
                String jndiName = this.getConnectionFactoryName();
                if(jndiName == null) throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE,
                    null,
                    "Neither the connection factory nor the connection factory name has been set"
                );
                this.connection = new LateBindingConnection_1(
                    jndiName
                );
            } else {
                this.connection = connectionFactory.createConnection();
            }
        }
        return this.connection;
    }
    
    /**
     * Create a new persistence manager
     * <p>
     * The parameters may be kept by the instance.
     * 
     * @param notifier
     * @param connectionUsername
     * @param connectionPassword
     * 
     * @return a new persistence manager
     */
    protected synchronized PersistenceManager newPersistenceManager(
        InstanceLifecycleNotifier notifier
    ){
        if(this.refPackageFactory == null) {
            try {
                boolean containerManagedUnitOfWork = !this.getOptimistic();
                boolean transactionPolicyIsNew = !containerManagedUnitOfWork;
                return new RefRootPackage_1(
                    new Manager_1(
                        new Connection_1(
                            new Provider_1(
                                new RequestCollection(
                                    new ServiceHeader(),
                                    getConnection()
                                ),
                                transactionPolicyIsNew
                              ),
                              containerManagedUnitOfWork
                          )
                      ),
                      this,
                      this.getDefaultImplPackageSuffix()
                 ).refPersistenceManager();  
            } catch (ServiceException exception) {
                throw new JDOFatalUserException(
                    "Persistence manager establishment failed",
                    exception
                );
            }
        } else {
            try {
                return this.refPackageFactory.createRefPackage().refPersistenceManager();
            } catch (RuntimeException exception) {
                throw new JDOFatalInternalException(
                    "RefPackage could not be cloned",
                    exception
                );
            }
        }
    }
    
    /**
     * Create a new persistence manager
     * <p>
     * The parameters may be kept by the instance.
     * 
     * @param notifier
     * @param connectionUsername the principal chain
     * @param connectionPassword the correlation id
     * 
     * @return a new persistence manager
     */
    protected synchronized PersistenceManager newPersistenceManager(
        InstanceLifecycleNotifier notifier,
        String connectionUsername,
        String connectionPassword
    ){
        if(this.refPackageFactory == null) {
            try {
                boolean containerManagedUnitOfWork = !this.getOptimistic();
                boolean transactionPolicyIsNew = !containerManagedUnitOfWork;
                return new RefRootPackage_1(
                    new Manager_1(
                        new Connection_1(
                            new Provider_1(
                                new RequestCollection(
                                    new ServiceHeader(
                                        getPrincipalChain(connectionUsername),
                                        connectionPassword, // correlationId,
                                        false, // traceRequest
                                        new QualityOfService(),
                                        null, // requestedAt,
                                        null // requestedFor
                                    ),
                                    getConnection()
                                ),
                                transactionPolicyIsNew
                              ),
                              containerManagedUnitOfWork
                          )
                      ),
                      this,
                      this.getDefaultImplPackageSuffix()
                 ).refPersistenceManager();  
            } catch (ServiceException exception) {
                throw new JDOFatalUserException(
                    "Persistence manager establishment failed",
                    exception
                );
            }
        } else throw new JDOFatalUserException(
            "This factory does not support service header replacement"
        );
    }

    /**
     * Create a service header populated with a principal list encoded as 
     * user name.
     * 
     * @param connectionUsername a principal or the stringified principal list 
     * 
     * @return a principal array
     */
    public static ServiceHeader newServiceHeader(
        String connectionUsername
    ){
        return  new ServiceHeader(
            getPrincipalChain(connectionUsername),
            null, // correlationId,
            false, // traceRequest
            new QualityOfService(),
            null, // requestedAt,
            null // requestedFor
        );
    }
    
    /**
     * Convert the stringified principal chain into an array
     * 
     * @param connectionUsername a principal or the stringified principal list 
     * 
     * @return a principal array
     */
    public static String[] getPrincipalChain(
        String connectionUsername
    ){
        if(
            connectionUsername == null || 
            "".equals(connectionUsername)
        ) {
            return new String[]{};
        } else if (
            connectionUsername.startsWith("[") &&
            connectionUsername.endsWith("]")
        ) {
            List<String> principalChain = new ArrayList<String>();
            for(
                int j = 0, i = 1, iLimit = connectionUsername.length() - 1;
                i < iLimit;
                i = j + 2
            ){
                j = connectionUsername.indexOf(", ", i);
                if(j < 0) j = iLimit;
                principalChain.add(connectionUsername.substring(i, j));
            }
            return principalChain.toArray(
                new String[principalChain.size()]
            );
        } else {
            return new String[]{connectionUsername};
        }
    }

}

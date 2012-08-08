/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Bean.java,v 1.28 2008/02/29 18:24:24 hburger Exp $
 * Description: A Dataprovider Service
 * Revision:    $Revision: 1.28 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 18:24:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.server;

import java.util.Arrays;
import java.util.Enumeration;

import javax.jdo.PersistenceManager;
import javax.naming.NameClassPair;
import javax.naming.NamingException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.object.spi.PersistenceManagerFactory_2_0;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1RemoteConnection;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1_0Remote;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.LateBindingConnection_1;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.PersistenceManagerFactory_1_0;
import org.openmdx.compatibility.base.application.cci.DbConnectionManager_1_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.application.j2ee.SessionBean_1;
import org.openmdx.compatibility.base.application.spi.DbConnectionManagerPool_1;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.kernel.Dataprovider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_2Connection;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.RMIMapper;
import org.openmdx.kernel.log.SysLog;

/**
 * The dataprovider server
 */
public class Dataprovider_1Bean
  extends SessionBean_1
  implements Dataprovider_1_0, PersistenceManagerFactory_1_0, PersistenceManagerFactory_2_0
{

    //------------------------------------------------------------------------
    // Implements Manageable_1_0
    // -----------------------------------------------------------------------
  
    /**
     * 
     */
    private static final long serialVersionUID = 3256720688944854580L;

    /**
     * Activates the EJB
     * 
     * @param configuration
     */
    protected void activate(
        Configuration configuration
    ) throws Exception {
        //
        // Get the server id
        //
        configuration.values(
            SharedConfigurationEntries.SERVER_ID
        ).add(
            this.serverId = getApplicationContext().getContainerId()
        );      
        //
        // Get dataprovider connections
        //
        getDataproviderConnections(configuration);
        //
        // Get datasources
        //
        getDataSources(configuration);
        //
        // Acquire kernel
        //
        this.kernel = new Dataprovider_1(
          configuration,
          this, 
          getSelf()
        );
    }
    
    /**
     * Activates the dataprovider Java server.
     */
    public void activate(
    ) throws Exception {
      super.activate();
      activate(new Configuration());
    }

    /**
     * Get the EJB's self reference
     * 
     * @return
     */
    protected Dataprovider_1_0 getSelf(
    ){
        try {
            return (Dataprovider_1_0) getSessionContext().getEJBLocalObject();
        } catch (Exception ejbLocalObjectException) {
            try {
                return new Dataprovider_1RemoteConnection<Dataprovider_1_0Remote>(
                    (Dataprovider_1_0Remote) getSessionContext().getEJBObject()
                );
            } catch (Exception ejbObjectException) {
                return null;
            }
        }
    }

    /**
     * Get dataprovider connections
     * 
     * @param dataproviderConfiguration
     * 
     * @throws ServiceException
     */
    protected void getDataproviderConnections(
        Configuration dataproviderConfiguration
    ) throws ServiceException{
        try {
            SparseList<Dataprovider_1_0> dataproviders = dataproviderConfiguration.values(
                SharedConfigurationEntries.DATAPROVIDER_CONNECTION
            );
            for(
                Enumeration<NameClassPair> e = getConfigurationContext().list(
                    DATAPROVIDER_NAME_CONTEXT
                );
                e.hasMoreElements();
            ){
                NameClassPair ncp = e.nextElement();
                String n = ncp.getName();
                if(
                    SharedConfigurationEntries.WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS || 
                    !ncp.isRelative()
                ) n = n.substring(n.lastIndexOf('/') + 1);
                if(
                    n.startsWith(DATAPROVIDER_NAME_PATTERN) &&
                    n.endsWith("]")
                ){
                    int i = Integer.parseInt(
                        n.substring(
                            DATAPROVIDER_NAME_PATTERN.length(),
                            n.length() - 1
                        )
                    );
                    dataproviders.set(i, newDataproviderConnection(n));
                }
            }
        } catch (NamingException exception) {
            // Ignore silently
        }
    }

    /**
     * Create a dataprovider for a given JNDI name
     * 
     * @param jndiEntry
     */
    protected Dataprovider_1_1Connection newDataproviderConnection(
        String jndiEntry
    ){
        return new LateBindingConnection_1(
            BEAN_ENVIRONMENT + '/' + DATAPROVIDER_NAME_CONTEXT + '/' + jndiEntry
        );
    }
    
    /**
     * Get the Data Sources
     * 
     * @param dataproviderConfiguration
     * 
     * @throws ServiceException
     */
    protected void getDataSources(
        Configuration dataproviderConfiguration
    ) throws ServiceException{
        try {
  	      SparseList<DbConnectionManager_1_0> datasources = dataproviderConfiguration.values(
  	          SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY
  	      );
  	      for(
  	          Enumeration<NameClassPair> e = getConfigurationContext().list(
  	              DATABASE_NAME_CONTEXT
  	          );
  	          e.hasMoreElements();
  	      ){
  	          NameClassPair ncp = e.nextElement();
  	          String n = ncp.getName();
              if(
                  SharedConfigurationEntries.WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS || 
                  !ncp.isRelative()
              ) n = n.substring(n.lastIndexOf('/') + 1);
  	          if(
  	              n.startsWith(DATABASE_NAME_PATTERN) &&
  	              n.endsWith("]")
  	           ){
  	              int i = Integer.parseInt(
  	                  n.substring(
  	                      DATABASE_NAME_PATTERN.length(),
  	                      n.length() - 1
  	                  )
  	              );
  	              datasources.set(
  	                  i,
  	                  getDatabaseConnectionManager(
  	                      BEAN_ENVIRONMENT + 
  	                      '/' + DATABASE_NAME_CONTEXT +
  	                      '/' + n
  	                  )
  	              );
  	           }
  	      }
        } catch (NamingException exception) {
            // Ignore silently
        }
    }

    /**
     * Database Connection Manager Factory
     * 
     * @param jndiName the connection managers JNDI name
     * 
     * @return a DB connection Manager
     * 
     * @throws ServiceException
     */
    protected DbConnectionManager_1_0 getDatabaseConnectionManager(
        String jndiName
    ) throws ServiceException{
        return new DbConnectionManagerPool_1(jndiName);
    }
        
    /**
     * Deactivates the dataprovider Java server.
     */
    public void deactivate(
    ) throws Exception {
      this.kernel.close();
      super.deactivate();      
    }


    //------------------------------------------------------------------------
    // PersistenceManagerFactory_2_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManagerFactory_2_0#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() throws ServiceException {
        return this.kernel.getPersistenceManager();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.object.spi.PersistenceManagerFactory_2_1#getPersistenceManager(java.lang.String, java.lang.String)
     */
    public PersistenceManager getPersistenceManager(
        String userid,
        String password
    ) throws ServiceException {
        return this.kernel.getPersistenceManager(userid, password);
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManagerFactory_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.PersistenceManagerFactory_1_0#getPersistenceManager(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader)
     */
    public PersistenceManager getPersistenceManager(
        ServiceHeader serviceHeader
    ) throws ServiceException {
        return this.kernel.getPersistenceManager(serviceHeader);
    }

    
    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
       * Process a set of working units
       *
       * @param   header          the service header
       * @param   workingUnits    a collection of working units
       *
       * @return  a collection of working unit replies
       */
      public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
      ) {      
        try {
          SysLog.detail(this.serverId + ": header", header);
          SysLog.detail(this.serverId + ": requests", Arrays.asList(workingUnits));
          UnitOfWorkReply[] replies = RMIMapper.marshal(
            this.kernel.process(
              header,
              RMIMapper.unmarshal(workingUnits)
            )
          );
          SysLog.detail(this.serverId + ": replies", Arrays.asList(replies));
          return replies;
        } catch (RuntimeException exception) {
            new ServiceException(exception).log();
            throw exception;    
        }
      }

      /**
       * 
       */    
      protected Dataprovider_1_2Connection kernel;
    
      /**
       * 
       */
      protected String serverId;

      /**
       * 
       */
      private static final String DATAPROVIDER_NAME_PATTERN = SharedConfigurationEntries.DATAPROVIDER_CONNECTION + '[';

      /**
       * 
       */
      protected static final String DATAPROVIDER_NAME_CONTEXT = "ejb";
      
      /**
       * 
       */
      private static final String DATABASE_NAME_PATTERN = SharedConfigurationEntries.DATABASE_CONNECTION_FACTORY + '[';

      /**
       * 
       */
      private static final String DATABASE_NAME_CONTEXT = "jdbc";

}  

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1ConnectionFactoryImpl.java,v 1.5 2009/05/26 13:43:51 wfro Exp $
 * Description: sDataprovider_1ConnectionFactoryImpl class
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 13:43:51 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.application.dataprovider.transport.ejb.cci;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_3Connection;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

public class Dataprovider_1ConnectionFactoryImpl
    implements Dataprovider_1ConnectionFactory
{

    /**
     * Constructor
     *
     * @param       registryContext
     *              the registries' context
     *
     * @exception   ServiceException    ACTIVATION_FALURE
     *              if the factory's initialization fails
     */
    public Dataprovider_1ConnectionFactoryImpl(
        Context registryContext
    ) throws ServiceException {
        this(
            registryContext,
            DEFAULT_REGISTRATION_ID,
            DEFAULT_REGISTRY_NAMES
        );
    }   

    /**
     * Constructor
     *
     * @param       registryContext
     *              the registries' context
     * @param       defaultRegistrationId
     *              the default dataprovider's registration id
     *
     * @exception   ServiceException    ACTIVATION_FALURE    
     *              if the factory's initialization fails
     */
    public Dataprovider_1ConnectionFactoryImpl(
        Context registryContext,
        String defaultRegistrationId
    ) throws ServiceException {
        this(
            registryContext,
            defaultRegistrationId,
            DEFAULT_REGISTRY_NAMES
        );
    }   


    /**
     * Constructor
     *
     * @param       registryContext
     *              the registries' context
     * @param       defaultRegistrationId
     *              the default dataprovider's registration id
     * @param       registryNames
     *              the registries' names
     *
     * @exception   ServiceException    ACTIVATION_FALURE    
     *              if the factory's initialization fails
     */
    public Dataprovider_1ConnectionFactoryImpl(
        Context registryContext,
        String defaultRegistrationId,
        String[] registryNames
    ) throws ServiceException {
        this.defaultRegistrationId = defaultRegistrationId;
        List<Context> registries = new ArrayList<Context>();

        for(
                int index = 0;
                index < registryNames.length;
                index++
        ) try {
            registries.add(
                (Context)registryContext.lookup(
                    registryNames[index]
                ) 
            );
        } catch (Exception exception) {
            // Ignore non-existent JNDI entries
        }
        this.registries = registries.toArray(
            new Context[registries.size()]
        );
        this.registryNames = new String[this.registries.length];

        for(
                int index = 0;
                index < this.registryNames.length;
                index++
        ) try {
            this.registryNames[index] = 
                this.registries[index].getNameInNamespace();
        } catch (Exception exception) {
            this.registryNames[index] = this.registries[index].toString();
        }
        SysLog.detail(
            "Dataprovider factory registries", 
            Arrays.asList(this.registryNames)
        );
    }

    /**
     * Creates a standard dataprovider connection
     *
     * @param       connectionFactory
     *              The dataprovider connection factory interface
     *
     * @return      a new dataprovider connection
     */
    public static Dataprovider_1_1Connection createStandardConnection(
        Object connectionFactory
    ) throws Exception {
        return ((Dataprovider_1ConnectionFactory)
                connectionFactory
        ).createConnection();
    }

    /**
     * Creates a remote dataprovider connection
     *
     * @param       ejbHome
     *              The dataprovider EJB's home interface
     *
     * @return      a new dataprovider connection
     * 
     * @deprecated
     */
    public static Dataprovider_1_1Connection createRemoteConnection(
        Object ejbHome
    ) throws Exception {
        throw new UnsupportedOperationException("Remote connections to dataproviders are not supported");
    }

    /**
     * Creates a local dataprovider connection
     *
     * @param       home
     *              The dataprovider EJB's local home interface
     *
     * @return      a new dataprovider connection
     * 
     * @throws CreateException 
     * @throws ServiceException 
     */
    public static Dataprovider_1_3Connection createLocalConnection(
        Object home
    ) throws ServiceException, CreateException {
        Dataprovider_1_0Local dataprovider = ((Dataprovider_1LocalHome)home).create();
            return new Dataprovider_1LocalConnection(dataprovider);
    }

    /**
     * Creates a local or remote dataprovider connection
     *
     * @param       home
     *              The dataprovider EJB's home or local home interface
     *
     * @return      a new dataprovider connection
     */
    public static Dataprovider_1_1Connection createGenericConnection(
        Object home
    ) throws Exception {
        return home instanceof Dataprovider_1ConnectionFactory ?
            createStandardConnection(home) :
        home instanceof Dataprovider_1LocalHome ?
            createLocalConnection(home) :
            createRemoteConnection(home);
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1ConnectionFactory
    //------------------------------------------------------------------------

    /**
     * Creates a dataprovider connection with the default registration id and
     * transaction policy. 
     *
     * @return      a dataprovider connection
     *
     * @exception   ServiceException 
     *              if the connection can't be created
     */
    public Dataprovider_1_1Connection createConnection(
    ) throws ServiceException {
        return createConnection(defaultRegistrationId);
    }


    /**
     * Creates a dataprovider connection with the specified registration id
     * and a default transaction policy.
     *
     * @param       registrationId
     *              The dataprovider's registration id 
     *
     * @return      a dataprovider connection
     *
     * @exception   ServiceException 
     *              if the connection can't be created
     */
    private Dataprovider_1_1Connection createConnection(
        String registrationId
    ) throws ServiceException {
        for (
                int index = 0;
                index < this.registries.length;
                index++
        ) try {
            return createGenericConnection(
                this.registries[index].lookup(registrationId)
            );
        } catch (NamingException exception) {
            // Continue 
        } catch (Exception exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Dataprovider connection \"" + registrationId + 
                "\" could not be created",
                new BasicException.Parameter("registrationId", registrationId),
                new BasicException.Parameter("registryName", this.registryNames[index])
            );
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_FOUND,
            "Dataprovider connection \"" + registrationId + 
            "\" could not be created",
            new BasicException.Parameter("registrationId", registrationId),
            new BasicException.Parameter("registryNames", (Object[])this.registryNames)
        );
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    protected static final String[] DEFAULT_REGISTRY_NAMES = {
        "java:comp/env",
        "ch/omex/dataprovider-1/NoOrNewTransaction"
    };

    protected static final String DEFAULT_REGISTRATION_ID = "access";


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     *
     */     
    protected final Context[] registries;

    /**
     *
     */     
    protected final String[] registryNames;

    /**
     *
     */     
    protected final String defaultRegistrationId;

}

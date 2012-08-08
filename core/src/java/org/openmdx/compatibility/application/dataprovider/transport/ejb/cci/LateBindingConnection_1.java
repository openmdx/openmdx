/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LateBindingConnection_1.java,v 1.14 2008/01/21 17:05:46 hburger Exp $
 * Description: Dataprovider_1Bean class
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/21 17:05:46 $
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
package org.openmdx.compatibility.application.dataprovider.transport.ejb.cci;

import java.io.Serializable;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Late Binding Connection
 */
public class LateBindingConnection_1 
    implements Dataprovider_1_1Connection, Serializable 
{

    /**
     * Constructor
     * 
     * @param dataproviderName the dataprovider connection factory's jndi name
     */
    public LateBindingConnection_1(
        String dataproviderName
    ) {
        this(dataproviderName, null);
    }

    /**
     * Constructor
     * 
     * @param dataproviderName the dataprovider connection factory's jndi name
     * @param environment the initial context's environment
     */
    public LateBindingConnection_1(
        String dataproviderName,
        Hashtable<?,?> environment
    ) {
        this.dataproviderName = dataproviderName;
        this.environment = environment == null ? null : new Hashtable<Object,Object>(environment);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3977017336140085043L;

    /**
     * The delegate
     */
    private transient Dataprovider_1_0 dataprovider = null;

    /**
     * @serial The dataprovider connection factory's JNDI name
     */
    protected final String dataproviderName;
        
    /**
     * @serial The initial context's environment
     */
    private final Hashtable<Object,Object> environment;

    /**
     * Create a dataprovider connection if necessary and return a dataprovider 
     * proxy.
     * 
     * @return a dataprovider proxy
     * 
     * @exception   RuntimeServiceException COMMUNICATION_FAILURE
     */
    private Dataprovider_1_0 getDelegate(
    ){
        if(this.dataprovider == null) try {
            this.dataprovider = createConnection();
        } catch (Exception exception) {
            throw new RuntimeServiceException(
                exception,
                StackedException.DEFAULT_DOMAIN,
                StackedException.COMMUNICATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("name",this.dataproviderName),
                    new BasicException.Parameter("environment", this.environment)
                },
                "Could not establish connection to dataprovider"
            );
        }
        return this.dataprovider;
    }

    /**
     * 
     * @return
     * @throws NamingException
     */
    protected Context getInitialContext(
    ) throws NamingException{
        return new InitialContext(this.environment);
    }
    
    /**
     * This method may be overridden by a subclass
     * 
     * @return
     * @throws Exception
     */
    protected Dataprovider_1_1Connection createConnection(
    ) throws Exception{
        Context initialContext = getInitialContext();
        try {
            return Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                initialContext.lookup(this.dataproviderName)
            );
        } finally {
            initialContext.close();
        }
    }
    

    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0Connection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     */
    public void remove() throws ServiceException {
        //
        // If late binding is required, then the garbage collector should
        // cleann up in return.
        //
        close();
    }

    /**
     * Close the connection.
     * <p>
     * This method does not remove the object it connects to.
     */
    public void close(
    ){
        this.dataprovider = null;
    }

    /* (non-Javadoc)
     * 
     * @exception   RuntimeServiceException COMMUNICATION_FAILURE
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] workingUnits
    ) {
        return getDelegate().process(header, workingUnits);
    }

}

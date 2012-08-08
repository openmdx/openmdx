/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LateBindingWrappedConnection_1.java,v 1.7 2008/09/10 08:55:24 hburger Exp $
 * Description: Late Binding Wrapped Connection
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:24 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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

import javax.naming.Context;
import javax.naming.InitialContext;

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
 * Late Binding Wrapped Connection
 */
public class LateBindingWrappedConnection_1 
implements Dataprovider_1_1Connection 
{

    /**
     * 
     */
    protected static final String DEFAULT_WRAPPER_NAME = "ejb/dataproviderRequiresNew";

    /**
     * Constructor
     * 
     * @param dataproviderName the dataprovider connection factory's jndi name
     */
    public LateBindingWrappedConnection_1(
        String dataproviderName
    ) {
        this(
            new LateBindingConnection_1(dataproviderName)
        );
    }

    /**
     * Constructor
     * 
     * @param dataprovider
     */
    public LateBindingWrappedConnection_1(
        Dataprovider_1_0 dataprovider
    ) {
        this(
            dataprovider, 
            DEFAULT_WRAPPER_NAME
        );
    }

    /**
     * Constructor
     * 
     * @param dataporviderName the dataprovider connection factory's jndi name
     * @param environment the initial context's environment
     */
    public LateBindingWrappedConnection_1(
        String dataproviderName,
        String wrapperName
    ) {
        this(
            new LateBindingConnection_1(dataproviderName),
            wrapperName
        );
    }

    /**
     * Constructor
     * 
     * @param dataprovider
     * @param wrapperName
     */
    public LateBindingWrappedConnection_1(
        Dataprovider_1_0 dataprovider,
        String wrapperName
    ) {
        this.processor = dataprovider;
        this.wrapperName = wrapperName;
    }

    /**
     * The processor
     */
    protected Dataprovider_1_0 processor;

    /**
     * The dataprovider wrapper factory's JNDI name
     */
    protected final String wrapperName;

    /**
     * The delegate
     */
    private transient Dataprovider_1_1Connection dataprovider = null;

    /**
     * Create a dataprovider connection if necessary and return a dataprovider 
     * proxy.
     * 
     * @return a dataprovider proxy
     * @throws Exception 
     * 
     * @exception   RuntimeServiceException COMMUNICATION_FAILURE
     */
    protected Dataprovider_1_1Connection createConnection(
    ) throws Exception{
        Context initialContext = new InitialContext();
        try {
            DataproviderWrapper_1_0Local wrapper = (
                    (DataproviderWrapper_1LocalHome) initialContext.lookup(this.wrapperName)
            ).create();            
            return new Dataprovider_1_0WrappedConnection(
                wrapper,
                this.dataprovider
            );
        } finally {
            initialContext.close();
        }
    }

    /**
     * Create a dataprovider connection if necessary and return a dataprovider 
     * proxy.
     * 
     * @return a dataprovider proxy
     * 
     * @exception   RuntimeServiceException COMMUNICATION_FAILURE
     */
    private synchronized Dataprovider_1_0 getDelegate(
    ){
        if(this.dataprovider == null) try {
            this.dataprovider = createConnection();
        } catch (Exception exception) {
            throw new RuntimeServiceException(
                exception,
                StackedException.DEFAULT_DOMAIN,
                StackedException.COMMUNICATION_FAILURE,
                "Could not establish connection to dataprovider",
                new BasicException.Parameter("wrapper",this.wrapperName)
            );
        }
        return this.dataprovider;
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

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.LateBindingConnection_1#close()
     */
    public synchronized void close() {
        if(this.dataprovider != null) {
            this.dataprovider.close();
            this.dataprovider = null;
        }
        this.processor = null;
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

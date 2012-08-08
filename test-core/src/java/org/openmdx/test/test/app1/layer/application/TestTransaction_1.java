/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestTransaction_1.java,v 1.2 2005/04/15 11:35:09 hburger Exp $
 * Description: TestTransaction_1
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/04/15 11:35:09 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.test.test.app1.layer.application;

import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.layer.application.ProvidingUid_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.test.kernel.application.container.ejb.cci.Runnable_1LocalHome;


/**
 * TestTransaction_1
 * <p>
 * This plugin rejects empty strings as description
 */
public class TestTransaction_1
        extends ProvidingUid_1 {


    /**
     * Constructor
     * 
     */
    public TestTransaction_1() {
        super();
    }

    /**
     * Its value is evaluated during prolog and used during epilog 
     */
    private List failures;
    
    /**
     * 
     */
    private Runnable rollbackOnly;
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id, Configuration configuration, 
        Layer_1_0 delegation
    ) throws Exception, ServiceException {
        super.activate(id, configuration, delegation);
        this.rollbackOnly = ((Runnable_1LocalHome)
            new InitialContext().lookup("java:comp/env/ejb/RollbackOnly")
        ).create();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[])
     */
    public void prolog(ServiceHeader header, DataproviderRequest[] requests) throws ServiceException {
        this.failures = new ArrayList();
        for(
            int i = 0;
            i < requests.length;
            i++
        ){
            switch(requests[i].operation()){
                case DataproviderOperations.OBJECT_CREATION:
                case DataproviderOperations.OBJECT_MODIFICATION:
                case DataproviderOperations.OBJECT_REPLACEMENT:
                case DataproviderOperations.OBJECT_SETTING:
                    SparseList description = requests[i].object().getValues("description");
                    if(
                        description != null && 
                        "".equals(description.get(0))
                    ) this.failures.add(
                        requests[i].path()
                    );                    
            }
        }
        super.prolog(header, requests);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[], org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply[])
     */
    public void epilog(
        ServiceHeader header, 
        DataproviderRequest[] requests, 
        DataproviderReply[] replies
    ) throws ServiceException {
        super.epilog(header, requests, replies);
        if(!this.failures.isEmpty()){
            if("non-transactional".equals(header.getCorrelationId())) {
                this.rollbackOnly.run();
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("path", this.failures)
                },
                TestTransaction_1.class.getName() + " rejects empty strings as description values"
           );    
        }
    }

}

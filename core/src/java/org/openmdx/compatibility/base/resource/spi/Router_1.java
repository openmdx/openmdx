/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Router_1.java,v 1.8 2005/04/08 14:21:04 hburger Exp $
 * Description: Router 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/04/08 14:21:04 $
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
package org.openmdx.compatibility.base.resource.spi;

import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.resource.cci.ConnectionFactory_1_0;
import org.openmdx.compatibility.base.resource.cci.Connection_1_0;
import org.openmdx.kernel.exception.BasicException;

/**
 * Router
 */
public class Router_1 implements ConnectionFactory_1_0 {

    /**
     * Constructor
     * 
     * @param factories
     * 
     * @throws ServiceException
     */
    public Router_1(
        ConnectionFactory_1_0[] factories
    ) throws ServiceException {
        if(factories == null || factories.length != 1) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_IMPLEMENTED,
            new BasicException.Parameter[]{
                new BasicException.Parameter("factories",factories.length),
                new BasicException.Parameter("minimum",1),
                new BasicException.Parameter("maximum",1)
            },
            "Number of factories must be one at the moment"
        );
        this.factories = factories;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.resource.cci.ConnectionFactory_1_0#getConnection()
     */
    public Connection_1_0 getConnection() throws ServiceException {
        return factories[0].getConnection();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.resource.cci.ConnectionFactory_1_0#getMetaData()
     */
    public Structure_1_0 getMetaData() throws ServiceException {
        return factories[0].getMetaData();
    }

    /**
     * 
     */
    private final ConnectionFactory_1_0[] factories;

}

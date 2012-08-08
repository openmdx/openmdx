/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1RemoteConnectionFactory.java,v 1.1 2009/01/05 13:44:51 wfro Exp $
 * Description: Dataprovider_1RemoteConnectionFactory class
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:51 $
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
package org.openmdx.application.dataprovider.transport.ejb.cci;

import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1ConnectionFactory;
import org.openmdx.application.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Dataprovider_1RemoteConnectionFactory
 */
public class Dataprovider_1RemoteConnectionFactory
implements Dataprovider_1ConnectionFactory
{   
    /**
     * Constructor
     */
    public Dataprovider_1RemoteConnectionFactory(
        Dataprovider_1Home home
    ){
        this.home = home;
    }

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
        try {
            return new Dataprovider_1RemoteConnection<Dataprovider_1_0Remote>(
                    this.home.create()
            );
        } catch (Exception exception) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Could not create EJB dataprovider object"
            ).log();
        }
    }

    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * The dataprovider home.
     */
    private final Dataprovider_1Home home;

}

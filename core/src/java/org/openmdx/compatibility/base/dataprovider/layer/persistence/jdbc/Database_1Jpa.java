/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Database_1Jpa.java,v 1.1 2008/12/26 21:49:47 wfro Exp $
 * Description: Database_1Jpa 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/26 21:49:47 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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

package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;

/**
 * Database_1Jpa
 *
 */
public class Database_1Jpa
    extends Database_1 {

    //-----------------------------------------------------------------------
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        this.entityManagerFactories = new ArrayList<EntityManagerFactory>();
        for(DataSource dataSource: this.dataSources) {
            Map<String,Object> config = new HashMap<String,Object>();
            config.put(
                "openjpa.ConnectionFactory", 
                dataSource
            );
            this.entityManagerFactories.add(
                Persistence.createEntityManagerFactory(
                    null, 
                    config
                )
            );
        }
    }

    //-----------------------------------------------------------------------
    protected EntityManager getEntityManager(
        DataproviderRequest request
    ) throws ServiceException {
        return this.entityManagerFactories.get(0).createEntityManager();
    }
        
    //-----------------------------------------------------------------------
    @Override
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {        
        super.epilog(
            header, 
            requests, 
            replies
        );
    }

    //-----------------------------------------------------------------------
    @Override
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        EntityManager em = this.getEntityManager(request);
        return super.get(header, request);
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected List<EntityManagerFactory> entityManagerFactories = null;


}

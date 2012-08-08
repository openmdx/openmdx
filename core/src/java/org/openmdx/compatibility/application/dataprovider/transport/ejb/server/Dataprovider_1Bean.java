/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Bean.java,v 1.35 2008/11/27 16:46:56 hburger Exp $
 * Description: A Dataprovider Service
 * Revision:    $Revision: 1.35 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/27 16:46:56 $
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.spi.AbstractDataprovider_1Bean;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.kernel.Dataprovider_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.transport.rmi.RMIMapper;

/**
 * The dataprovider server
 */
public class Dataprovider_1Bean 
    extends AbstractDataprovider_1Bean 
    implements Dataprovider_1_0
{

    /**
     * 
     */    
    protected Dataprovider_1_1Connection kernel;
  
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3256720688944854580L;

    
    //------------------------------------------------------------------------
    // Implements Manageable_1_0
    // -----------------------------------------------------------------------
  
    /**
     * Activates the EJB
     * 
     * @param configuration
     */
    protected void activate(
        Configuration configuration
    ) throws Exception {
        super.activate(configuration);
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
     * Deactivates the dataprovider Java server.
     */
    public void deactivate(
    ) throws Exception {
      this.kernel.close();
      super.deactivate();      
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
        UnitOfWorkRequest... workingUnits
    ) {      
        try {
            logger.debug("Requests: server={}, header={}, {}", this.serverId, header, workingUnits);
            UnitOfWorkReply[] replies = RMIMapper.marshal(
                this.kernel.process(
                    header,
                    RMIMapper.unmarshal(workingUnits)
                )
            );
            logger.debug("Replies: serverId={}, header={}, {}", this.serverId, header, replies);
            return replies;
       } catch (RuntimeException exception) {
            new ServiceException(exception).log();
            throw exception;    
        }
    }

}  

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractLayer_1.java,v 1.1 2009/01/05 13:44:51 wfro Exp $
 * Description: Abstract Layer_1_0 implementation
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:51 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.spi;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;


import java.util.UUID;




/**
 * A transparent implementation of Layer_1_0, i.e. all calls are delegated.
 */
abstract class AbstractLayer_1
    implements Layer_1_0
{

    /**
     * Constructor
     */
        protected AbstractLayer_1(){
                super();
        }

    /**
     * To replace the deprecated UIDFactory.create() calls
     * 
     * @return a UID as string
     */
    protected final String uidAsString(
    ){
        if(this.uuidGenerator == null) this.uuidGenerator = UUIDs.getGenerator();
        UUID uuid = this.uuidGenerator.next();
        return this.compressUID ?
            UUIDConversion.toUID(uuid) :
            uuid.toString();
    }


    //------------------------------------------------------------------------
    // Implements Layer_1_0
    //------------------------------------------------------------------------

    /**
     * Activates a dataprovider layer
     * 
     * @param   id              the dataprovider layer's id
     * @param   configuration   the dataprovider'a configuration
     *  <dl>
     *      <dt>namespaceId</dt>            <dd>String</dd>
     *      <dt>exposedPath</dt>            <dd>Path[]</dd>
     *  </dl>
     * @param       delegation
     *              the layer to delegate to;
     *              or null if "persistenceLayer".equals(id)
     *
     * @exception   Exception
     *              unexpected exceptions
     * @exception   ServiceException
     *              expected exceptions
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException{
        this.compressUID = configuration.isOn(
            SharedConfigurationEntries.COMPRESS_UID
        );
    }

    /**
     * Deactivates a dataprovider layer
     * <p>
     * Subclasses overriding this method have to apply the following pattern:
     * <pre>
     *  public void deactivate(
     *  ) throws Exception, ServiceException {
     *      // local deactivation code
     *      super.deactivate();
     *  }
     * </pre>       
     *
     * @exception   Exception
     *              unexpected exceptions
     * @exception   ServiceException
     *              expected exceptions
     */
    public void deactivate(
    ) throws Exception, ServiceException{
        //
    }

    /**
     * 
     */
    private transient UUIDGenerator uuidGenerator = null;

    /**
     * Defines whether the UID's should be in compressed or UUID format.
     */
    private boolean compressUID;

}

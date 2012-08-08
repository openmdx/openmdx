/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractLayer_0.java,v 1.1 2008/11/27 16:46:55 hburger Exp $
 * Description: Abstract Delegating Layer
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/27 16:46:55 $
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
package org.openmdx.compatibility.base.dataprovider.spi;

import java.util.Collections;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.ConfigurationSpecifier;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderLayers;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Abstract Delegating Layer
 */
class AbstractLayer_0 implements Layer_1_0 {

    /**
     * Constructor 
     */
    protected AbstractLayer_0() {
        // Instantiation requires sub-classing
    }

    /**
     * The layer's id
     */
    private short id;

    /**
     *
     */
    private Dataprovider_1_0 delegation;

    /**
     * Retrieve id.
     *
     * @return Returns the id.
     */
    protected final String getId() {
        return DataproviderLayers.toString(this.id);
    }
    
    
    //------------------------------------------------------------------------
    // Implements Layer_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#configurationSpecification()
     */
    public Map<String, ConfigurationSpecifier> configurationSpecification() {
        return Collections.emptyMap();
    }

    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
        this.id = id;
        if (id == DataproviderLayers.PERSISTENCE) new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            "An instance of " + AbstractLayer_0.class.getSimpleName() + " is used as PERSISTENCE layer!",
            new BasicException.Parameter(
                "class", 
                getClass().getName()
            ),
            new BasicException.Parameter(
                "namespace",
                configuration.getFirstValue(SharedConfigurationEntries.NAMESPACE_ID)
            )
        ).log();
        this.delegation = delegation;
        SysLog.detail(
            "Activating " + DataproviderLayers.toString(id) + " layer ",
            configuration
        );
    }

    public void deactivate(
    ) throws Exception {
        //
    }

    /**
     * Create a <code>NOT_SUPPORTED</code> ServiceException
     * 
     * @return a newly created <code>NOT_SUPPORTED</code> ServiceException
     */
    private ServiceException newNotSupportedException(
    ){
        return new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "Only Datatprovider_1_0 processing methods are supported",
            new BasicException.Parameter("layer-class", getClass()),
            new BasicException.Parameter("layer-id", getId())
        );
    }

    /**
     * Get the object specified by the requests's path 
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public final DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * Get the objects specified by the references and filter properties.
     *
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public final DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * Request to start publishing the data stream specified by the requests's path 
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public final DataproviderReply startPublishing(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * Create a new object
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public final DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * Modifies some of an object's attributes leaving the others unchanged.
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public final DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * Modifies all changeable attributes of an object.
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public final DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * Creates an object or modifies all its changeable attributes if it
     * already exists.
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public final DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * Removes an object including its descendents
     *
     * @param       header
     *              request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              on failure
     */
    public final DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * This method allows the dataprovider layers to verify the integrity of a 
     * collection of requests as a whole before the actual processing of the 
     * individual requests starts.
     *
     * @param       header
     *              the requests' service header
     * @param       requests
     *              the request list
     *
     * @exception   ServiceException
     *              on failure
     */
    public final void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException {
        throw newNotSupportedException();
    }

    /**
     * This method allows the dataprovider layers postprocessing of a 
     * collection of requests as a whole after the actual processing of the 
     * individual requests has been done.
     *
     * @param       header
     *              the requests' service header
     * @param       requests
     *              the request collection
     * @param       replies
     *              the reply collection
     *
     * @exception   ServiceException
     *              must not be thrown unless the request collection is to be 
     *              treated as an atomic processing unit
     */
    public final void epilog(
        ServiceHeader header, 
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        throw newNotSupportedException();
    }


    //--------------------------------------------------------------------------
    // Implements Operation_1_0
    //--------------------------------------------------------------------------

    /**
     * Execute an operation
     *
     * @param       header
     *              the request header
     * @param       request
     *              the request
     *
     * @return      the reply
     *
     * @exception   ServiceException
     *              in case of failure
     */
    public final DataproviderReply operation(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        throw newNotSupportedException();
    }


    //--------------------------------------------------------------------------
    // Implements Operation_1_0
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0#process(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest[])
     */
    public UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest... workingUnits
    ) {
        return this.delegation.process(header, workingUnits);
    }

}

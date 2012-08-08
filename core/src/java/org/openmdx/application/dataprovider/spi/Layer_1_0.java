/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Layer_1_0.java,v 1.1 2009/01/05 13:44:51 wfro Exp $
 * Description: spice dataprovider layer interface
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:51 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.application.dataprovider.spi;

import java.util.Map;

import org.openmdx.application.cci.ConfigurationSpecifier;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.base.exception.ServiceException;


/**
 * This interface provides a standardized interface for query and update
 * operations throughout the dataprovider layers.  
 */
public interface Layer_1_0  
    extends Dataprovider_1_0, Operation_1_0
{

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
     * @exception   ServiceException
     *              expected exceptions
     * @exception   Exception
     *              unexpected exceptions
     *
   */
    void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception;

    /**
     * Deactivates a dataprovider layer
     *
     * @exception   ServiceException
     *              expected exceptions
     * @exception   Exception
     *              unexpected exceptions
     */
    void deactivate(
    ) throws Exception;

    /**
     * This layer's specific configuration specifiers.
     *
     * @return  a map with id/ConfigurationSpecifier entries
     */
    Map<String,ConfigurationSpecifier> configurationSpecification();


    //------------------------------------------------------------------------
    // Request processing
    //------------------------------------------------------------------------
        
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
    DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException;

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
    DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException;

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
    DataproviderReply startPublishing(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException;

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
    DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException;

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
    DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException;

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
    DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException;

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
    DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException;

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
    DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException;

    
    //------------------------------------------------------------------------
    // Work unit processing
    //------------------------------------------------------------------------
        
    /**
     * This method allows the dataprovider layers to verify the integrity of a 
     * set of requests as a whole before the actual processing of the 
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
    void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException;
    
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
    void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException;
    
}

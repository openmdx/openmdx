/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateCapable_1.java,v 1.11 2009/02/17 10:06:22 hburger Exp $
 * Description: State Capable Layer
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/17 10:06:22 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.compatibility.state1.layer.type;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.application.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.application.dataprovider.spi.DelegatingLayer_1;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.state1.spi.StateCapables;

/**
 * State Capable Layer
 */
public class StateCapable_1 extends DelegatingLayer_1 {

    /**
     * The identity prefix for objects with unique transaction time
     */
    private Collection<Path> transactionTimeUnique;
    
    /**
     * The identity prefix for objects with unique valid time
     */
    private Collection<Path> validTimeUnique;

    /**
     * Convert configuration values to a path set
     * 
     * @param values the paths string representations
     * 
     * @return the corresponding path set
     */
    protected static Set<Path> toPaths(
        SparseList<?> values
    ){
        Set<Path> paths = new HashSet<Path>();
        for(
            Iterator<?> i = values.populationIterator();
            i.hasNext();
        ){
            paths.add(
                new Path((String)i.next())
            );
        }
        return paths;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        this.transactionTimeUnique = toPaths(
            configuration.values(LayerConfigurationEntries.TRANSACTION_TIME_UNIQUE)
        );
        this.validTimeUnique = toPaths(
            configuration.values(LayerConfigurationEntries.VALID_TIME_UNIQUE)
        );
        super.activate(id, configuration, delegation);
    }

    /**
     * Tells whether the given identity belongs to an object with unique transaction time
     * 
     * @param identity
     * @return <code>true</code> if the given identity belongs to an object with unique transaction time
     */
    private Boolean isTransactionTimeUnique(
        Path identity
    ){
        for(Path pattern : this.transactionTimeUnique) {
            if(identity.isLike(pattern)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * Tells whether the given identity belongs to an object with unique transaction time
     * 
     * @param identity
     * @return <code>true</code> if the given identity belongs to an object with unique transaction time
     */
    private Boolean isValidTimeUnique(
        Path identity
    ){
        for(Path pattern : this.validTimeUnique) {
            if(identity.isLike(pattern)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
    
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.spi.DelegatingLayer_1#create(org.openmdx.application.dataprovider.cci.ServiceHeader, org.openmdx.application.dataprovider.cci.DataproviderRequest)
     */
    @Override
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        DataproviderObject object = request.object();
        if(getModel().isSubtypeOf(object.values(SystemAttributes.OBJECT_CLASS).get(0), "org:openmdx:compatibility:state1:BasicState")) {
            SparseList<Object> coreValues = object.getValues("core");
            if(coreValues != null) {
                Path coreValue = (Path) coreValues.get(0); 
                if(coreValue != null && StateCapables.isCoreObject(coreValue)) {
                    if(StateCapables.getResourceIdentifier(coreValue).getLastComponent().isPlaceHolder()) {
                        coreValue.setTo(StateCapables.getStateCapable(request.path()));
                    }
                }
            }
        }
        return super.create(header, request);
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
    @Override
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        Path requestPath = request.path();
        if(StateCapables.CORE_SEGMENT.equals(requestPath)) {
            DataproviderObject segment = new DataproviderObject(requestPath);
            segment.values(
                SystemAttributes.OBJECT_CLASS
            ).set(
                0, 
                "org:openmdx:compatibility:state1:Segment"
            );
            return new DataproviderReply(segment);            
        } else if (StateCapables.isCoreObject(requestPath)) {
            DataproviderObject stateCapable = new DataproviderObject(requestPath);
            stateCapable.values(
                SystemAttributes.OBJECT_CLASS
            ).set(
                0, 
                "org:openmdx:compatibility:state1:StateCapable"
            );
            stateCapable.values(
                "openmdxjdoVersion"
            ).set(
                0,
                Integer.valueOf(0)
            );
            completeStateCapable(stateCapable, new Path(requestPath.getBase()));
            return new DataproviderReply(stateCapable);            
        }
        return super.get(
            header, 
            request
        );
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.spi.DelegatingLayer_1#process(org.openmdx.application.dataprovider.cci.ServiceHeader, org.openmdx.application.dataprovider.cci.UnitOfWorkRequest)
     */
    @Override
    protected UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWorkRequest
    ) {
        UnitOfWorkReply unitOfWorkReply = super.process(header, unitOfWorkRequest);
        if(!unitOfWorkReply.failure()) {
            for(DataproviderReply dataproviderReply : unitOfWorkReply.getReplies()) {
                for(DataproviderObject dataproviderObject : dataproviderReply.getObjects()) {
                    Object objectClass = dataproviderObject.values(SystemAttributes.OBJECT_CLASS).get(0);
                    if("org:openmdx:compatibility:state1:StateCapable".equals(objectClass)) {
                        completeStateCapable(dataproviderObject, dataproviderObject.path());
                    }
                }
            }
        }
        return unitOfWorkReply;
    }

    /**
     * Add the derived attributes
     * 
     * @param stateCapable
     */
    private void completeStateCapable(
        DataproviderObject stateCapable,
        Path identity
    ){
        if(stateCapable.getValues("transactionTimeUnique") == null) {
            stateCapable.values(
                "transactionTimeUnique"
            ).set(
                0,
                isTransactionTimeUnique(
                    identity
                )
            );
        }
        if(stateCapable.getValues("validTimeUnique") == null) {
            stateCapable.values(
                "validTimeUnique"
            ).set(
                0,
                isValidTimeUnique(
                    identity
                )
            );
        }
    }

}

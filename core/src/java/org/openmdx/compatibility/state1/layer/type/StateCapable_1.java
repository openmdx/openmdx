/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateCapable_1.java,v 1.2 2008/12/15 03:15:37 hburger Exp $
 * Description: State Capable Layer
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:37 $
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

import static org.openmdx.base.aop2.core.Aspect_1.CORE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.DelegatingLayer_1;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.state1.aop2.core.DateState_1;
import org.openmdx.compatibility.state1.spi.StateCapables;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * State Capable Layer
 */
public class StateCapable_1 extends DelegatingLayer_1 {

    /**
     * The path's to be handled by this class
     */
    private Collection<Path> stateCapablePattern;
    
    /**
     * The identity prefix for objects with unique transaction time
     */
    private Collection<Path> transactionTimeUnique;
    
    /**
     * The identity prefix for objects with unique valid time
     */
    private Collection<Path> validTimeUnique;
    
    /**
     * The model repository accessor
     */
    private Model_1_0 model;

    private Path dateState;
    
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
    ) throws Exception {
        this.stateCapablePattern = toPaths(
            configuration.values(LayerConfigurationEntries.STATE_CAPABLE)
        );
        this.transactionTimeUnique = toPaths(
            configuration.values(LayerConfigurationEntries.TRANSACTION_TIME_UNIQUE)
        );
        this.validTimeUnique = toPaths(
            configuration.values(LayerConfigurationEntries.VALID_TIME_UNIQUE)
        );
        this.model = (Model_1_0) configuration.values(
            SharedConfigurationEntries.MODEL
        ).get(
            0
        );
        ModelElement_1_0 dateState = this.model == null ? null : this.model.findElement(
            DateState_1.CLASS
        );
        this.dateState = dateState == null ? null : dateState.path();
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
        for(Path stateCapablePattern : this.stateCapablePattern) {
            if(requestPath.isLike(stateCapablePattern)) {
                Path corePath = requestPath;
                DataproviderObject core = new DataproviderObject(corePath);
                core.values(
                    SystemAttributes.OBJECT_CLASS
                ).set(
                    0, 
                    org.openmdx.compatibility.state1.aop2.core.StateCapable_1.CLASS
                );
                Path resourceIdentifier = new Path(corePath.getBase());
                core.values(LayerConfigurationEntries.TRANSACTION_TIME_UNIQUE).set(0, isTransactionTimeUnique(resourceIdentifier)); 
                core.values(LayerConfigurationEntries.VALID_TIME_UNIQUE).set(0, isValidTimeUnique(resourceIdentifier)); 
//              core.values(STATE).set(0, resourceIdentifier);
                DataproviderObject view = new DataproviderObject(resourceIdentifier);
                view.values(
                    SystemAttributes.OBJECT_CLASS
                ).set(
                    0, 
                    DateState_1.CLASS
                );
                view.values(CORE).set(0, corePath);
                return new DataproviderReply(
                    Arrays.asList(core, view)
                );
            }
        }
        if(requestPath.getBase().indexOf('!') < 0) {
            ModelElement_1_0[] ends = this.model == null ? null : this.model.getTypes(requestPath);
            if(ends != null && ends[2].values("allSupertype").contains(this.dateState)) {
                Path resourceIdentifier = requestPath;
                Path stateCapable = StateCapables.getStateCapable(resourceIdentifier);
                DataproviderObject core = new DataproviderObject(stateCapable);
                core.values(
                    SystemAttributes.OBJECT_CLASS
                ).set(
                    0, 
                    org.openmdx.compatibility.state1.aop2.core.StateCapable_1.CLASS
                );
                core.values(LayerConfigurationEntries.TRANSACTION_TIME_UNIQUE).set(0, isTransactionTimeUnique(resourceIdentifier)); 
                core.values(LayerConfigurationEntries.VALID_TIME_UNIQUE).set(0, isValidTimeUnique(resourceIdentifier)); 
//              core.values(STATE).set(0, resourceIdentifier);
                DataproviderObject view = new DataproviderObject(resourceIdentifier);
                view.values(
                    SystemAttributes.OBJECT_CLASS
                ).set(
                    0, 
                    DateState_1.CLASS
                );
                view.values(CORE).set(0, stateCapable);
                return new DataproviderReply(
                    Arrays.asList(view, core)
                );
            }
        }
        return super.get(
            header, 
            request
        );
    }

}

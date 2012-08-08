/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: State_1.java,v 1.9 2010/01/06 17:16:35 wfro Exp $
 * Description: Strict_1 class performing type checking of DataproviderRequest/DataproviderReply
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/06 17:16:35 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.type;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.spi.Object_2Facade;

/** 
 * Apply org::openmdx::state2 configuration
 */
public class State_1 extends Strict_1 {

    //-----------------------------------------------------------------------
    public State_1(
    ) {
    }
    
    // --------------------------------------------------------------------------
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new StateLayerInteraction(connection);
    }
            
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.type.Strict_1#activate(short, org.openmdx.application.configuration.Configuration, org.openmdx.application.dataprovider.spi.Layer_1_0)
     */
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1 delegation
    ) throws ServiceException {
        super.activate(id, configuration, delegation);
        this.validTimeUnique = configuration.isOn(
            LayerConfigurationEntries.VALID_TIME_UNIQUE
        );
        this.transactionTimeUnique = configuration.isOn(
            LayerConfigurationEntries.TRANSACTION_TIME_UNIQUE
        );
    }

    // --------------------------------------------------------------------------
    public class StateLayerInteraction extends Strict_1.LayerInteraction {
        
        public StateLayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
        }
    }
    
    //-----------------------------------------------------------------------
    /**
     * Tells whether states are disabled or not
     */
    private boolean validTimeUnique;

    /**
     * Tells whether invalidated states are deleted or kept.
     */
    private boolean transactionTimeUnique;
    
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.type.Strict_1#completeAndVerifyReplyObject(javax.resource.cci.MappedRecord, short, org.openmdx.base.query.AttributeSpecifier[])
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void completeAndVerifyReplyObject(
        MappedRecord object, 
        short attributeSelector
    ) throws ServiceException {
        super.completeAndVerifyReplyObject(
            object, 
            attributeSelector
        );
        if(
            attributeSelector != AttributeSelectors.NO_ATTRIBUTES &&
            getModel().objectIsSubtypeOf(object, "org:openmdx:state2:StateCapable")
        ) {
            MappedRecord value = Object_2Facade.getValue(object);
            value.put("validTimeUnique", Boolean.valueOf(validTimeUnique));
            value.put("transactionTimeUnique", Boolean.valueOf(this.transactionTimeUnique));
        }
    }

}

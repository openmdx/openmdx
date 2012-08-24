/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Model layer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.model;

import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.spi.Object_2Facade;

/**
 * State plug-in
 */
public class State_2 extends Standard_1 {
	
	/**
	 * Tells whether the transaction time is unique for object hosted by this
	 * provider.
	 */
	private boolean transactionTimeUnique = false;
    
    /**
     * Retrieve transactionTimeUnique.
     *
     * @return Returns the transactionTimeUnique.
     */
    public boolean isTransactionTimeUnique() {
        return this.transactionTimeUnique;
    }
    
    /**
     * Set transactionTimeUnique.
     * 
     * @param transactionTimeUnique The transactionTimeUnique to set.
     */
    public void setTransactionTimeUnique(boolean transactionTimeUnique) {
        this.transactionTimeUnique = transactionTimeUnique;
    }

    /* (non-Javadoc)
	 * @see org.openmdx.application.dataprovider.spi.Layer_1#activate(short, org.openmdx.application.configuration.Configuration, org.openmdx.application.dataprovider.spi.Layer_1)
	 */
	@Override
	public void activate(
		short id, 
		Configuration configuration,
		Layer_1 delegation
	) throws ServiceException {
		super.activate(id, configuration, delegation);
		if(configuration.containsEntry(LayerConfigurationEntries.TRANSACTION_TIME_UNIQUE)) {
    		setTransactionTimeUnique(configuration.isOn(LayerConfigurationEntries.TRANSACTION_TIME_UNIQUE));
		}
	}

	/**
	 * Complete a StateCapable's values
	 * 
	 * @param request
	 * @param object
	 * 
	 * @throws ServiceException
	 */
    @SuppressWarnings("unchecked")
	protected void completeStateCapable(
        DataproviderRequest request,
        MappedRecord object
	) throws ServiceException {
        Object_2Facade.getValue(object).put("transactionTimeUnique", Boolean.valueOf(this.transactionTimeUnique));
	}
	
	/**
     * Set known derived features
     * 
     * @param request
     * @param object
     * 
     * @throws ServiceException
     */
	@Override
    protected void completeObject(
        DataproviderRequest request,
        MappedRecord object
    ) throws ServiceException {
    	super.completeObject(request, object);
		if(getModel().objectIsSubtypeOf(object, "org:openmdx:state2:StateCapable")) {
		    completeStateCapable(request, object);
    	}
    }

}

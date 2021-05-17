/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XML Importer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2011, OMEX AG, Switzerland
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
package org.openmdx.application.xml.spi;

import javax.resource.ResourceException;

import org.openmdx.base.dataprovider.cci.Channel;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Object_2Facade;

/**
 * Dataprovider Target
 */
public class Dataprovider_2Target implements ImportTarget {

    /**
     * Constructor
     * 
     * @param target
     */
    public Dataprovider_2Target(
        Channel target
    ) {
        this.target = target;
        this.target2 = (Channel)target.clone();
    }

    /**
     * The delegate
     */
    private final Channel target;
    private final Channel target2; // for retrieval

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmdx.application.xml.ImportTarget#importObject(org.openmdx
     * .application.xml.ImportTarget.Mode, javax.resource.cci.MappedRecord)
     */
    @Override
    public void importObject(
        ImportMode mode, 
        ObjectRecord objectHolder
    ) throws ServiceException {
    	try {
	        switch (mode) {
	            case UPDATE:
	            	objectHolder.setVersion(
	                    this.target2.addGetRequest(
	                        Object_2Facade.getPath(objectHolder)
	                    ).getVersion()
	                );
	                this.target.addUpdateRequest(
	                    objectHolder
	                );
	                break;
	            case SET:
	                ObjectRecord result = this.target2.addGetRequest(
	                    Object_2Facade.getPath(objectHolder)
	                );
	                if(result == null) {
	                    this.target.addCreateRequest(
	                        objectHolder
	                    );
	                } else {
	                    this.target.addUpdateRequest(
	                        objectHolder
	                    );
	                }
	                break;
	            case CREATE:
	                this.target.addCreateRequest(
	                    objectHolder
	                );
	                break;
	        }
    	} catch (ResourceException exception) {
    		throw new ServiceException(exception);
    	}
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importEpilog()
     */
    @Override
    public void importEpilog(
        boolean successful
    ) throws ServiceException {
    	try {
	        if(successful) {
				this.target.endBatch();
	        } else {
	            this.target.forgetBatch();
	        }
    	} catch (ResourceException e) {
    		throw new ServiceException(e);
    	}
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ImportTarget#importProlog()
     */
    @Override
    public void importProlog(
    ) throws ServiceException {
        try {
			this.target.beginBatch();
		} catch (ResourceException e) {
    		throw new ServiceException(e);
		}
    }

}
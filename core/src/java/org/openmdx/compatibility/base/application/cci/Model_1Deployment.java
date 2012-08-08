/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Model_1Deployment.java,v 1.3 2008/04/10 18:16:59 hburger Exp $
 * Description: Model_1Deployment
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/10 18:16:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.compatibility.base.application.cci;

import java.util.Arrays;

import javax.naming.Context;

import org.openmdx.base.application.deploy.Deployment;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.spi.Model_1;


/**
 * Model_1Deployment
 */
public class Model_1Deployment implements Deployment {

    /**
     * Constructor
     * 
     */
    public Model_1Deployment(
        String... models
    ) {
        this.pending = models;
    }

    /**
     * Not yet deployed model elements
     */
    private String[] pending;

    /**
     * The exception in case of failure
     */
    private ServiceException exception = null;
    
    
    //------------------------------------------------------------------------
    // Implements Deployment
    //------------------------------------------------------------------------    

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.client.Deployment#getStatus()
     */
    public synchronized ServiceException getStatus() {
        return this.exception;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.deploy.Deployment#newInitialContext()
     */
    public Context context() throws ServiceException {
        if(this.pending != null) try {
            Model_1 model = new Model_1();
            model.addModels(Arrays.asList(this.pending));
        } catch (ServiceException exception) {
            this.exception = exception;            
        } finally {
            this.pending = null;
        }
        if(this.exception == null){
            return null;
        } else {
            throw this.exception;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.client.Deployment#destroy()
     */
    public void destroy() {
        //
    }

}

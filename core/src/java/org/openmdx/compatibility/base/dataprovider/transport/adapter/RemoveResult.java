/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RemoveResult.java,v 1.7 2005/02/21 13:10:11 hburger Exp $
 * Description: Dataprovider Adapter: Get Result
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/21 13:10:11 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.compatibility.base.dataprovider.transport.adapter;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.RequestedObject;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 *  
 */
class RemoveResult 
    extends RequestedObject
{

    /**
     * 
     */
    private static final long serialVersionUID = 3256999951979459120L;

    /**
     * Constructor
     *
     * @param   path
     *          The path to be updated upon reply
     */
    RemoveResult(
        Path path,
        Manager_1_0 manager
    ){
        this.path = path;
        this.manager = manager;
    }
    

    //------------------------------------------------------------------------
    // Extends RequestedObject
    //------------------------------------------------------------------------
        
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyListener#onException(org.openmdx.base.exception.ServiceException)
     */
    public void onException(ServiceException exception) {
        if(exception.getExceptionCode() == BasicException.Code.NOT_FOUND) try {
            this.manager.invalidate(this.path, true);
        } catch (ServiceException invaidationException) {
            invaidationException.log();
        }
        super.onException(exception);
    }
            

    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * The path to be updated upon reply
     */     
    protected final Path path;

    /**
     * 
     * @author hburger
     *
     * To change the template for this generated type comment go to
     * Window>Preferences>Java>Code Generation>Code and Comments
     */
    protected final Manager_1_0 manager;
    
}

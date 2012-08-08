/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: OperationAwarePlugin_1.java,v 1.11 2009/03/02 13:38:15 wfro Exp $
 * Description: OperationAwarePlugin_1
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/02 13:38:15 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.dispatching;

import java.util.StringTokenizer;

import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

//-----------------------------------------------------------------------------
/**
 * openMDX 1 compatibility plugin which dispatches Layer_1_0 operation requests to 
 * ObjectFactory_1_0 object requests.
 */
abstract public class OperationAwarePlugin_1
    extends AbstractPlugin_1 {

    // ---------------------------------------------------------------------------
    // Layer_1
    // ---------------------------------------------------------------------------
    /*
     * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#otherOperation(org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest,
     *      java.lang.String, org.openmdx.compatibility.base.naming.Path)
     */
    protected DataproviderObject otherOperation(
        ServiceHeader header,
        DataproviderRequest request,
        String operation,
        Path replyPath
    ) throws ServiceException {
        DataObject_1_0 target = this.retrieveObject(request.path().getPrefix(
            request.path().size() - 2)
        );
        String featureName = this.getFeatureName(request);
        //
        // handle namespaces
        //
        if (featureName.indexOf(':') >= 0) {
            StringTokenizer tokens = new StringTokenizer(featureName, ":");
            String containerName = tokens.nextToken();
            String objectName = tokens.nextToken();
            featureName = tokens.nextToken();
            target = target.objGetContainer(containerName).get(
                objectName
            );
        }
        //
        // invoke operation on target
        //
        return DataproviderObjectMarshaller.toDataproviderObject(
            replyPath,
            target.objInvokeOperation(
                featureName, 
                DataproviderObjectMarshaller.toStructure(
                    request.object(),
                    this.objectCache,
                    this.objectFactory,
                    getModel()
                )
            )
        );
    }

    // -------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------

}

//--- End of File -----------------------------------------------------------

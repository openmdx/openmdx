/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ProvidingId_1.java,v 1.1 2005/03/15 15:18:08 hburger Exp $
 * Description: ProvidingId_1
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/03/15 15:18:08 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.document1.layer.application;

import java.util.Date;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.layer.application.ProvidingUid_1;
import org.openmdx.compatibility.base.naming.Path;


/**
 * ProvidingId_1
 */
public class ProvidingId_1
    extends ProvidingUid_1 
{
    private static final String WILDCARD = ":*";
    protected static final Path REVISION_REFERENCE_PATTERN = new Path(
        new String[]{
            "org:openmdx:document1",
            "provider",WILDCARD,
            "segment",WILDCARD,
            "cabinet",WILDCARD,
            "node",WILDCARD,
            "revision"
        }
    );
    protected static final Path REVISION_OBJECT_PATTERN = REVISION_REFERENCE_PATTERN.getChild(WILDCARD);

    /**
     * Create a new object
     *
     * @param   request     the request, an in out parameter
     *
     * @exception   ServiceException    in case of failure
     */
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        final Path path = request.path();
        if(path.isLike(REVISION_REFERENCE_PATTERN)) {
            path.add(createRevisionId());
        } else if (
            path.isLike(REVISION_OBJECT_PATTERN) &&
            path.getLastComponent().isPlaceHolder()
        ){
            path.remove(path.size()-1);
            path.add(createRevisionId());
        }
        return super.create(header,request);
    }

    /**
     * Get a time stamp
     * 
     * @return a time stamp
     */
    protected String createRevisionId(
    ){
       return DateFormat.getInstance().format(new Date()); 
    }

}

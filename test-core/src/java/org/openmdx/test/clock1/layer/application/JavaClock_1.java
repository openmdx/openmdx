/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JavaClock_1.java,v 1.2 2008/03/07 22:22:07 hburger Exp $
 * Description: Java Clock Provider
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/07 22:22:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.test.clock1.layer.application;

import java.util.Date;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Java Clock Provider
 */
public class JavaClock_1 extends Layer_1 {
	
	
	/* (non-Javadoc)
	 * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
	 */
	public DataproviderReply get(
		ServiceHeader header,
		DataproviderRequest request
	) throws ServiceException {
		if(!request.path().isLike(A_SEGMENT_PATH)) return super.get(header, request);
		DataproviderObject segment = new DataproviderObject(request.path());
	    segment.values(
	      SystemAttributes.OBJECT_CLASS
	    ).set(0, "org:openmdx:test:clock1:Segment");
	    return new DataproviderReply(segment);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.compatibility.base.dataprovider.spi.Operation_1_0#operation(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
	 */
	public DataproviderReply operation(
		ServiceHeader header,
		DataproviderRequest request
	) throws ServiceException {
	    String operationName = request.path().get(
	      request.path().size()-2
	    );
	    Path accessPath = request.path().getPrefix(
	      request.path().size()-2
	    );
		if(
			!accessPath.isLike(A_SEGMENT_PATH) ||
			!"currentDateAndTime".equals(operationName)
		) return super.operation(header, request);
		DataproviderObject time = new DataproviderObject(
			request.path().getDescendant(new String[]{"reply","-"})
		);
	    time.values(
	      SystemAttributes.OBJECT_CLASS
	    ).set(0, "org:openmdx:test:clock1:Time");
	    time.values(
  	      "utc"
  	    ).set(0, DateFormat.getInstance().format(new Date()));
	    return new DataproviderReply(time);
	}

	private final static Path A_SEGMENT_PATH = new Path(
		"xri:@openmdx:org.openmdx.test.clock1/provider/:*/segment/:*"
	);

}

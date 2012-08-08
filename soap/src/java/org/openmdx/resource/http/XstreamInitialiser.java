/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: XstreamInitialiser.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/22 15:32:52 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland; France Telecom, France
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
 * 
 */
package org.openmdx.resource.http;

import javax.resource.ResourceException;
import javax.resource.cci.Record;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.Records;
import org.openmdx.base.transport.jca.DeletePersistentInteractionSpec;
import org.openmdx.base.transport.jca.FlushInteractionSpec;
import org.openmdx.base.transport.jca.MakePersistentInteractionSpec;
import org.openmdx.base.transport.jca.OpenMdxInteractionSpec;
import org.openmdx.base.transport.jca.OperationInteractionSpec;
import org.openmdx.base.transport.jca.QueryInteractionSpec;
import org.openmdx.base.transport.jca.RetrieveInteractionSpec;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public final class XstreamInitialiser {
    
    private static Record indexedRecordTemplate = null;
    private static Record mappedRecordTemplate = null;
    
	private XstreamInitialiser() {

	}

	private static Class getIndexedRecordClass(
    ) {
        if(indexedRecordTemplate == null) {
            try {
                indexedRecordTemplate = Records.getRecordFactory().createIndexedRecord(null);
            }
            catch(ResourceException e) {
                new ServiceException(e).log();
            }
        }
        return indexedRecordTemplate.getClass();
    }
    
    private static Class getMappedRecordClass(
    ) {
        if(mappedRecordTemplate == null) {
            try {
                mappedRecordTemplate = Records.getRecordFactory().createMappedRecord(null);
            }
            catch(ResourceException e) {
                new ServiceException(e).log();
            }
        }
        return mappedRecordTemplate.getClass();
    }
        
	public static XStream getXstream() {
		XStream xstream = new XStream(new StaxDriver());
		xstream.alias(BeginRequest.class.getName(), BeginRequest.class);
		xstream.alias(EndRequest.class.getName(), EndRequest.class);
		xstream.alias(OpenMdxInteractionSpec.class.getName(),
				DeletePersistentInteractionSpec.class);
		xstream.alias(OpenMdxInteractionSpec.class.getName(),
				FlushInteractionSpec.class);
		xstream.alias(OpenMdxInteractionSpec.class.getName(),
				MakePersistentInteractionSpec.class);
		xstream.alias(OpenMdxInteractionSpec.class.getName(),
				OperationInteractionSpec.class);
		xstream.alias(OpenMdxInteractionSpec.class.getName(),
				QueryInteractionSpec.class);
		xstream.alias(OpenMdxInteractionSpec.class.getName(),
				RetrieveInteractionSpec.class);
		xstream.alias(OpenMdxInteractionSpec.class.getName(),
				OpenMdxInteractionSpec.class);
		xstream.alias("VSIR", getIndexedRecordClass());
		xstream.alias("VSMR", getMappedRecordClass());
		xstream.setMode(XStream.NO_REFERENCES);
		return xstream;
	}

}

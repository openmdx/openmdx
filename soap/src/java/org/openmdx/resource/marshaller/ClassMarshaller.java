/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: ClassMarshaller.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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
package org.openmdx.resource.marshaller;

import javax.resource.ResourceException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * This is the interface for all object marshaller. A marshaller is able to
 * serialize and deserialize an object in xml.
 * 
 */
public interface ClassMarshaller {
	/**
	 * Used to know the name of the element in xml that is handled by this
	 * marshaller. This method is used when reading an xml stream.
	 * 
	 * @return the element name.
	 */
	String getHandledElementName();

	/**
	 * Used to know which class is handled by this marshaller. This method is
	 * used when writing an object into a stream.
	 * 
	 * @return the name of the class.
	 */
	String getHandledClassName();

	/**
	 * When reading an xml stream, this method tries to unmarshall the object.
	 * 
	 * @param reader
	 *            the xml reader
	 * @return the new object deserialized from the stream.
	 * @throws ResourceException
	 *             if there was a problem when reading the stream.
	 */
	Object unmarshal(XMLStreamReader reader) throws ResourceException;

	/**
	 * When writing an object into a stream.
	 * 
	 * @param toMarshal
	 *            the object to marshal. Must be of the correct type handled by
	 *            this marshaller.
	 * @param writer
	 *            the stream to write on.
	 * @throws ResourceException
	 *             if there was a problem when marshalling the object.
	 */
	void marshal(Object toMarshal, XMLStreamWriter writer)
			throws ResourceException;
}

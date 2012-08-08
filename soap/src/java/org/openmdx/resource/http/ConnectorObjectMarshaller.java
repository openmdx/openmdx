/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: ConnectorObjectMarshaller.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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

import java.io.InputStream;
import java.io.OutputStream;

import javax.resource.ResourceException;

/**
 * This Interface is used by the connector to marshall and unmarshall object.
 * Implementations of this interface should be found through factories.
 */
public interface ConnectorObjectMarshaller {
	/**
	 * Init the marshaller to write on the given output stream.
	 * 
	 * @param output
	 *            the output stream to write on.
	 * @throws ResourceException
	 *             If there was a problem during initialisation.
	 */
	void initWrite(OutputStream output) throws ResourceException;

	/**
	 * Init the marshaller to read on the given InputStream.
	 * 
	 * @param input
	 *            the inputstream
	 * @param manager
	 *            to handle StreamedRecords.
	 * @throws ResourceException
	 *             a pb during marshaller initialisation.
	 */
	void initRead(InputStream input, OutputManager manager)
			throws ResourceException;

	/**
	 * Writes an object on the stream
	 * 
	 * @param toSend
	 *            the object to marshall.
	 * @throws ResourceException
	 */
	void writeObject(Object toSend) throws ResourceException;

	/**
	 * Ends the writing part.
	 * 
	 * @throws ResourceException
	 */
	void flush() throws ResourceException;

	/**
	 * Read an object from the InputStream
	 * 
	 * @return the object or null if there is no more objects.
	 * @throws ResourceException
	 */
	Object readObject() throws ResourceException;
}

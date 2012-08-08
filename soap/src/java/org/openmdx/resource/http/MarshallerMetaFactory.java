/*
 * ====================================================================
 * Project:     openMDX/SOAP, http://www.openmdx.org/
 * Name:        $Id: MarshallerMetaFactory.java,v 1.1 2007/03/22 15:32:52 wfro Exp $
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

import org.openmdx.kernel.exception.BasicException;

/**
 * This meta factory uses Reflection to instanciate a new
 * ConnectorObjectMarshallerFactory.
 * 
 */
public class MarshallerMetaFactory {
	private MarshallerMetaFactory() {

	}

	/**
	 * Creates a new ConnectorObjectMarshallerFactory using the name given as
	 * the Factory class name.
	 * 
	 * @param name
	 *            the class name of the factory to create
	 * @return the new Factory
	 * @throws ResourceException
	 *             If there is a problem during the instanciation of the
	 *             factory.
	 */
	public static ConnectorObjectMarshallerFactory getFacory(String name)
			throws ResourceException {
		try {
			return (ConnectorObjectMarshallerFactory) Class.forName(name)
					.newInstance();
		} catch (InstantiationException e) {
			throw new ResourceException(
					new BasicException(
							e,
							BasicException.Code.DEFAULT_DOMAIN,
							BasicException.Code.COMMUNICATION_FAILURE,
							null,
							"Error during the resource adapter initialisation :"
									+ " the factory you tried to use may not have a default Constructor"));
		} catch (IllegalAccessException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.COMMUNICATION_FAILURE, null,
					"Error during the resource adapter initialisation : "
							+ "You have no access to the factory Class"));
		} catch (ClassNotFoundException e) {
			throw new ResourceException(new BasicException(e,
					BasicException.Code.DEFAULT_DOMAIN,
					BasicException.Code.COMMUNICATION_FAILURE, null,
					"Error during the resource adapter initialisation :"
							+ "the factory class is not in the classpath"));
		}

	}
}

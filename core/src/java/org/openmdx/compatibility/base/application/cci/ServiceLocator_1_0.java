/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ServiceLocator_1_0.java,v 1.4 2007/12/13 18:19:20 hburger Exp $
 * Description: The ServiceLocator interface
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/13 18:19:20 $
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
package org.openmdx.compatibility.base.application.cci;

import java.util.Enumeration;

import org.openmdx.base.exception.ServiceException;

/**
 * The ServiceLocator interface
 *
 * @deprecated in favour of {@link javax.naming.InitialContext
 * Standard JNDI access}
 */
public interface ServiceLocator_1_0 {

	/**
	 * Lookup a service
	 *
	 * @param	registrationId
	 *			A colon-separated list of naming elements
	 *
	 * @return	An object, usually the service's connection factory
	 */
	Object lookup(
		String registrationId
	) throws ServiceException;

	/**
	 * Bind a service to the registry 
	 *
	 * @param		registrationId
	 *				A colon-separated list of naming elements
	 * @param		object
	 *				The object to be bound, usually the service's connection
	 *				factory
	 */
	void bind(
		String registrationId,
		Object object
	) throws ServiceException;
			
	/**
	 * Removes a service from the registry
	 *
	 * @param		registrationId
	 *				A colon-separated list of naming elements
	 */
	void unbind(
		String registrationId
	) throws ServiceException;

	/**
	 *
	 */
	Enumeration listBindings(
		String name
	) throws ServiceException;

}

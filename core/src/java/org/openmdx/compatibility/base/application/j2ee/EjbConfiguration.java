/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: EjbConfiguration.java,v 1.8 2005/05/18 16:10:59 hburger Exp $
 * Description: EjbConfiguration class 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/05/18 16:10:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.application.j2ee;

import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.kernel.exception.BasicException;


/**
 * Command option parser
 */
public class EjbConfiguration 
	extends Configuration
{ 

	/**
	 * Constructor
	 *
	 * @param		source
	 *				the ejb context
	 * @param		section
	 *				the section to be parsed, my be null
	 * @param		specification
	 *				the configurations specification, may be null
	 */
	public EjbConfiguration(
		Context context,
		String[] section,
		Map specification
	) throws ServiceException {
		super();
		try {
			String sectionName =
				section == null ? "" : section[0];
			for(
				NamingEnumeration bindings = context.listBindings(sectionName);
				bindings.hasMore();
			) try {
				Binding binding = (Binding)bindings.next();
				Object value = binding.getObject();
                String name = binding.getName();
                if(
                    SharedConfigurationEntries.WORK_AROUND_SUN_APPLICATION_SERVER_BINDINGS || 
                    !binding.isRelative()
                ) name = name.substring(name.lastIndexOf('/') + 1);
				if (
					value instanceof String ||
					value instanceof Boolean ||
					value instanceof Integer ||
					value instanceof Long ||
					value instanceof Short
				) setValue(name,value);
			} catch (NamingException exception) {
				throw new ServiceException (
					exception,
					BasicException.Code.DEFAULT_DOMAIN, 
					BasicException.Code.ACTIVATION_FAILURE, 
					new BasicException.Parameter[] {
						new BasicException.Parameter("section", section)
					},
					"Naming exception in section"
				);
			}
		} catch (NamingException exception) {
			// The section has no entries
		}
	}
    
}

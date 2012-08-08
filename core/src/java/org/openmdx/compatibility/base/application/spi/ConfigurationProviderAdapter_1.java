/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ConfigurationProviderAdapter_1.java,v 1.3 2008/03/21 18:45:21 hburger Exp $
 * Description: Application Framework 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:45:21 $
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
package org.openmdx.compatibility.base.application.spi;

import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.ConfigurationProvider_1_0;
import org.openmdx.compatibility.base.application.cci.Configuration_1_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;

/**
 * To access the components or subcomponents configuration
 *
 * @obsolete	Configuration_1_0 superseeded by ConfigurationProvider_1_0
 */
public class ConfigurationProviderAdapter_1 implements ConfigurationProvider_1_0 { 
	
	final private Configuration_1_0 configurationProvider;
	
	/**
 	 * @obsolete	Configuration_1_0 superseeded by ConfigurationProvider_1_0
 	 */
	public ConfigurationProviderAdapter_1(
		Configuration_1_0 configurationProvider
	){
		this.configurationProvider = configurationProvider;
	}
		
	/**
	 * Get a specific configuration
	 *
	 * @param		section
	 *				the section to be parsed, my be null
	 * @param		specification
	 *				a map of id/ConfigurationSpecifier entries, may be null
	 * 
	 * @return		the requested configuration
	 *
	 * @exception	ServiceException
	 *				if the actual configuration does not match the 
	 *				specification
	 */
	@SuppressWarnings("unchecked")
    public Configuration getConfiguration(
		String[] section,
		Map specification
	) throws ServiceException {
		if(section.length == 0) return this.configurationProvider.getConfiguration(
			null,
			specification
		);
		StringBuilder target = new StringBuilder();
		for (
			int index = 0;
			index < section.length;
			index++
		){
			if(index > 0) target.append('/');
            target.append(section[index]);
		}
		return this.configurationProvider.getConfiguration(
			target.toString(),
			specification
		);
	}
	
}

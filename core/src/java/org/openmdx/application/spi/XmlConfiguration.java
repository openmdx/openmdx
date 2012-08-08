/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XmlConfiguration.java,v 1.1 2009/01/05 13:44:50 wfro Exp $
 * Description: Application Framework 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:50 $
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
package org.openmdx.application.spi;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;



/**
 * Command option parser
 */
public class XmlConfiguration 
	extends Configuration
{ 

	/**
	 * Constructor
	 *
	 * @param		source
	 *				the xml source
	 * @param		section
	 *				the section to be parsed, my be null
	 * @param		specification
	 *				a map of id/ConfigurationSpecifier entries, may be null
	 */
	@SuppressWarnings("unchecked")
    public XmlConfiguration(
		URL source,
		String[] section,
		Map specification
	) throws ServiceException {
		super();
		if(source.getProtocol().equalsIgnoreCase("file")) {
			// only "file"-s are supported for the moment
			String configuration = "";
			try {
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(source.openStream())
				);
				String line;
				while((line=reader.readLine()) != null) configuration += line;
			} catch (FileNotFoundException fEx) {
				throw new ServiceException (
					fEx,
					BasicException.Code.DEFAULT_DOMAIN, 
					BasicException.Code.ACTIVATION_FAILURE, 
					"Failed to open the file",
					new BasicException.Parameter [] {
						new BasicException.Parameter("FileName", source.getFile())
					}
				);
			} catch (IOException ioEx) {
				throw new ServiceException (
					ioEx,
					BasicException.Code.DEFAULT_DOMAIN, 
					BasicException.Code.ACTIVATION_FAILURE, 
					"Error reading a character from the file",
					new BasicException.Parameter [] {
						new BasicException.Parameter("FileName", source.getFile())
					}
				);
			}
			
			// Parse and load the stringified configuration
			parseAndLoadConfiguration(configuration, section);
			
		} else {
			throw new ServiceException (
				BasicException.Code.DEFAULT_DOMAIN,
				BasicException.Code.ACTIVATION_FAILURE, 
				"The URL type " + source.getProtocol() + " is not supported", 
				new BasicException.Parameter [] {
					new BasicException.Parameter("URL", source)
				}
			); 
		}
	}
	
	/**
	 * Parses and loads the stringified configuration
	 */
	protected void parseAndLoadConfiguration(
		String configuration,
		String[] section
	){
		int fromIndex = 0;
		while(configuration.indexOf("<env-entry>", fromIndex) != -1) {
			String envEntryName = configuration.substring(
				configuration.indexOf("<env-entry-name>", fromIndex) + 16,
				configuration.indexOf("</env-entry-name>", fromIndex)
			);
			
			// Consider only the cases when:
			// - the section is null and envEntryName doesn't contain "/" sign,
			// - the section is set and envEntryName contains "/" sign.
			if(section == null && envEntryName.indexOf("/") == -1 ||
				section != null && envEntryName.indexOf("/") != -1) {
				
				String prefix = (section == null) ? "" : section[0] + "/";
				
				if(envEntryName.indexOf(prefix) != -1) {
					
					envEntryName = envEntryName.substring(
						envEntryName.indexOf(prefix) + prefix.length(),
						envEntryName.length()
					);
					String envEntryType = configuration.substring(
						configuration.indexOf("<env-entry-type>", fromIndex) + 16,
						configuration.indexOf("</env-entry-type>", fromIndex)
					);
					String envEntryValue = configuration.substring(
						configuration.indexOf("<env-entry-value>", fromIndex) + 17,
						configuration.indexOf("</env-entry-value>", fromIndex)
					);
					
					// Find out the index
					String name = envEntryName;
					int index = 0;
					if(envEntryName.endsWith("]")) {
						name = envEntryName.substring(
							0,
							envEntryName.indexOf("[")
						);
						index = Integer.parseInt(
							envEntryName.substring(
								envEntryName.indexOf("[") + 1,
								envEntryName.indexOf("]")
							)
						);
					}
					
					// Set the value
					if(envEntryType.equals("java.lang.String")) {
						values(name).set(
							index,
							envEntryValue
						);
					} else if(envEntryType.equals("java.lang.Boolean")) {
						values(name).set(
							index,
							Boolean.valueOf(envEntryValue)
						);
					} else if(envEntryType.equals("java.lang.Integer")) {
						values(name).set(
							index,
							Integer.valueOf(envEntryValue)
						);
					} else if(envEntryType.equals("java.lang.Long")) {
						values(name).set(
							index,
							Long.valueOf(envEntryValue)
						);
					} else if(envEntryType.equals("java.lang.Short")) {
						values(name).set(
							index,
							Short.valueOf(envEntryValue)
						);
					}
				}
			}
			
			// Update fromIndex
			fromIndex = configuration.indexOf("</env-entry>", fromIndex + 12);
		}
	}
}

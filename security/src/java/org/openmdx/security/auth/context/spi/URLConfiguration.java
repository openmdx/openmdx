/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: URLConfiguration.java,v 1.4 2008/09/11 10:47:30 hburger Exp $
 * Description: URL Configuration
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/11 10:47:30 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.security.auth.context.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.openmdx.base.exception.ExtendedIOException;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.DelegatingMappedRecord;
import org.openmdx.base.text.conversion.URLReader;
import org.openmdx.kernel.environment.SystemProperties;
import org.openmdx.kernel.exception.BasicException;

/**
 * This class represents an implementation for
 * <code>javax.security.auth.login.Configuration</code>.
 *
 * <p>This object provides the runtime login configuration acquired
 * from a single <code>java.net.URL</code>.
 * 
 * <p>The configuration syntax supported by this implementation
 * is exactly that syntax specified in the
 * <code>javax.security.auth.login.Configuration</code> class.
 * 
 * @see javax.security.auth.login.LoginContext
 */
@SuppressWarnings("unchecked")
public class URLConfiguration extends Configuration {

	/**
	 * Constructor
     * 
     * @param source the <code>Confuguration</code>'s source
	 * @param options the options shared by all <code>LoginModule</code>s
	 * 
	 * @throws IOException 
	 */
	public URLConfiguration(
		URL source, 
        Map options
	) throws IOException{
		this.url = source;
        this.options = options;
		load();
	}
	
	/**
	 * The configuration source
	 */
	private final URL url;
	
    /**
     * The options shared by all <code>LoginModule</code>s
     */
    private final Map options;

    /**
	 * The tokenizer variable is used by the load() method.
	 */
	private transient StreamTokenizer tokenizer;

	/**
	 * The currentToken variable is used by the load() method.
	 */
    private transient int currentToken;
	
	/**
	 * The currentLine variable is used by the load() method.
	 */
    private transient int currentLine;
	
	/**
	 * The configuration variable is set by the load() method.
	 */
    private Map configuration;
	
	/**
	 * Defines whether system properties should be expanded
	 */
	protected static final boolean EXPAND_PROPERTIES = !"false".equals(
		System.getProperty("policy.expandProperties")
	);

	/**
     * Retrieve an entry from the Configuration using an application name
     * as an index.
     *
     * <p>
     *
     * @param applicationName the name used to index the Configuration.
     * @return an array of AppConfigurationEntries which correspond to
     *		the stacked configuration of LoginModules for this
     *		application, or null if this application has no configured
     *		LoginModules.
     */
    public synchronized AppConfigurationEntry[] getAppConfigurationEntry (
		String applicationName
	){
		if(this.configuration == null) refresh();
		if(this.configuration == null) return null;		
		List entries = (List) this.configuration.get(
			this.configuration.containsKey(applicationName) ? applicationName : "other"
	    );
		return entries == null || entries.isEmpty() ?
			null :
			(AppConfigurationEntry[]) entries.toArray(
				new AppConfigurationEntry[entries.size()]
			);
    }

	/**
	 * Load the configuration from the given URL
	 * 
	 * @throws IOException in case of failure
	 * @throws ResourceException 
	 */
	private void load (
	) throws IOException {
		Map configuration = new LinkedHashMap();
		newStreamTokenizer();
		
		newToken();
		while (this.currentToken != StreamTokenizer.TT_EOF) {
			
			String applicationName = this.tokenizer.sval;
			String moduleClass;
			AppConfigurationEntry.LoginModuleControlFlag controlFlag;
			LinkedList configEntries = new LinkedList();
			
			// application name
			newToken();
			read('{');
			
			// get the modules
			while (this.currentToken != '}') {
				// get the module class name
				moduleClass = read("module class name", false);
				
				// controlFlag (required, optional, etc)
				controlFlag = toControlFlag(
					read("control flag", false)
				);
				
				// get the args
				HashMap options = new HashMap(this.options);
				while (this.currentToken != ';') {
					String key = read("option key", false);
					read('=');
					String value = read("option value", URLConfiguration.EXPAND_PROPERTIES);
					options.put(key, value);
				}
				
				newToken();
				
				// create the new element
				configEntries.add(
					new AppConfigurationEntry(
						moduleClass,
						controlFlag,
						options
					)
				);
			}
			
			read('}');
			read(';');
			
			// add this configuration entry
			if (
				configuration.containsKey(applicationName)
			) throw new ExtendedIOException(
			    new BasicException(
				    BasicException.Code.DEFAULT_DOMAIN,
				    BasicException.Code.INVALID_CONFIGURATION,
				    "Can not specify multiple entries for application name",
						new BasicException.Parameter("url", this.url),
						new BasicException.Parameter("applicationName", applicationName)
				)
			);
			configuration.put(applicationName, configEntries);
		}
		this.configuration = configuration;
	}
	
	/**
	 * Retrieve the control flag for a given value
	 * 
	 * @param value the control flag's string representation
	 * 
	 * @return the control flag for the given value
	 * 
	 * @throws ExtendedIOException
	 */
	private AppConfigurationEntry.LoginModuleControlFlag toControlFlag(
		String value
	) throws ExtendedIOException{
		if (value.equalsIgnoreCase("REQUIRED")) {
			return AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
		} else if (value.equalsIgnoreCase("REQUISITE")) {
			return AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
		} else if (value.equalsIgnoreCase("SUFFICIENT")) {
			return AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
		} else if (value.equalsIgnoreCase("OPTIONAL")) {
			return AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
		} else throw new ExtendedIOException(
		    new BasicException(
			    BasicException.Code.DEFAULT_DOMAIN,
			    BasicException.Code.INVALID_CONFIGURATION,
			    "Invalid Control Flag",
					new BasicException.Parameter("url", this.url),
					new BasicException.Parameter("line", this.currentLine),
					new BasicException.Parameter("controlFlag", value)
			)
		);
	}
	
    /**
     * Refresh and reload the Configuration by re-reading all of the
     * login configurations.
     *
     * @exception SecurityException if the caller does not have permission
     *				to refresh the Configuration.
     */
    public synchronized void refresh(
	) {		
		try {
			load();
		} catch (Exception exception) {
			this.configuration = null;
		}
    }	
		
	/**
	 * Create a new <code>StreamTokenizer</code>
	 * 
	 * @return an initialized <code>StreamTokenizer</code>
	 * 
	 * @throws IOException
	 */
	private void newStreamTokenizer(
	) throws IOException{
		StreamTokenizer streamTokenizer = new StreamTokenizer(
			new BufferedReader(
				new URLReader(this.url)
			)
		);
		streamTokenizer.quoteChar('"');
		streamTokenizer.wordChars('$', '$');
		streamTokenizer.wordChars('_', '_');
		streamTokenizer.wordChars('-', '-');
		streamTokenizer.lowerCaseMode(false);
		streamTokenizer.slashSlashComments(true);
		streamTokenizer.slashStarComments(true);
		streamTokenizer.eolIsSignificant(true);
		this.tokenizer = streamTokenizer;
	}
	
	/**
	 * Consume a given token
	 * 
	 * @param expected the expected token
	 * 
	 * @throws IOException if the current token is not the expected one.
	 */
	private void read(
		char expected
	) throws IOException {
		switch(this.currentToken) {
			case StreamTokenizer.TT_EOF:
				throw new ExtendedIOException(
				    new BasicException(
					    BasicException.Code.DEFAULT_DOMAIN,
					    BasicException.Code.INVALID_CONFIGURATION,
					    "Unexpected end of file",
							new BasicException.Parameter("url", this.url),
							new BasicException.Parameter("expected", expected)
					)
				);				
			case '{':
			case ';':
			case '}':
			case '=':
				if (this.currentToken == expected) {
					newToken();
				} else {
					throw new ExtendedIOException(
					    new BasicException(
						    BasicException.Code.DEFAULT_DOMAIN,
						    BasicException.Code.INVALID_CONFIGURATION,
						    "Unexpected value",
								new BasicException.Parameter(
									"url", 
									this.url
								),
								new BasicException.Parameter(
									"line", 
									this.currentLine
								),
								new BasicException.Parameter(
									"expected",
									expected
								),
								new BasicException.Parameter(
									"found", 
									this.tokenizer.sval
								)
						)
					);
				}
				break;
				
			default:
				throw new ExtendedIOException(
				    new BasicException(
					    BasicException.Code.DEFAULT_DOMAIN,
					    BasicException.Code.INVALID_CONFIGURATION,
					    "Unexpected value",
							new BasicException.Parameter(
								"url", 
								this.url
							),
							new BasicException.Parameter(
								"line", 
								this.currentLine
							),
							new BasicException.Parameter(
								"expected",
								expected
							),
							new BasicException.Parameter(
								"found", 
								this.tokenizer.sval
							)
					)
				);
		}
	}

	/**
	 * Read a value
	 * @param expected description of the expected value
	 * @param expand tells whether system properties hsould be expanded
	 * 
	 * @throws IOException if the current token is not the expected one.
	 */
	private String read(
		String expected, 
		boolean expand
	) throws IOException {
		
		switch(this.currentToken) {
			case StreamTokenizer.TT_EOF:
				throw new ExtendedIOException(
				    new BasicException(
					    BasicException.Code.DEFAULT_DOMAIN,
					    BasicException.Code.INVALID_CONFIGURATION,
					    "Unexpected end of file",
							new BasicException.Parameter("url", this.url),								
							new BasicException.Parameter("expected", expected)
					)
				);				
			case '"':
			case StreamTokenizer.TT_WORD:
				String value = this.tokenizer.sval;
				newToken();
                return SystemProperties.expand(expand, value);
			default:
				throw new ExtendedIOException(
				    new BasicException(
					    BasicException.Code.DEFAULT_DOMAIN,
					    BasicException.Code.INVALID_CONFIGURATION,
					    "Unexpected value",
							new BasicException.Parameter(
								"url", 
								this.url
							),
							new BasicException.Parameter(
								"line", 
								this.currentLine
							),
							new BasicException.Parameter(
								"expected", 
								expected
							),
							new BasicException.Parameter(
								"found", 
								this.tokenizer.sval
							)
					)
				);
		}
	}
	
	/**
	 * Read ahead the next token and keep track of the line number
	 * 
	 * @throws IOException
	 */
	private void newToken(
	) throws IOException {
		while (
			(this.currentToken = this.tokenizer.nextToken()) == StreamTokenizer.TT_EOL
		) this.currentLine++;
	}
	

	//------------------------------------------------------------------------
	// Extends Object
	//------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		try {
			MappedRecord reply = Records.getRecordFactory().createMappedRecord(
				getClass().getName(),
				"URL: " + this.url
			);
			for(
				Iterator i = this.configuration.entrySet().iterator();
				i.hasNext();
			){
				Map.Entry e = (Entry) i.next();
				List v = new ArrayList();
				for(
					Iterator j = ((List)e.getValue()).iterator();
					j.hasNext();
				){
					AppConfigurationEntry m = (AppConfigurationEntry) j.next();
					v.add(
						new DelegatingMappedRecord(
							m.getLoginModuleName(),
							m.getControlFlag().toString(),
							m.getOptions()				
						)
					);
				}
				reply.put(
					e.getKey(),
					v
				);
			}
			return reply.toString();
		} catch (Exception e) {
			return super.toString();
		}
	}

}

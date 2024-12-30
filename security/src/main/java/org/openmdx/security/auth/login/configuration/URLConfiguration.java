/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: URL Configuration
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.security.auth.login.configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.MappedRecord;
#endif
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.DelegatingMappedRecord;
import org.openmdx.base.text.conversion.URLReader;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * This class represents an implementation for
 * {@code javax.security.auth.login.Configuration}.
 *
 * <p>This object provides the runtime login configuration acquired
 * from a single {@code java.net.URL}.
 * 
 * <p>The configuration syntax supported by this implementation
 * is exactly that syntax specified in the
 * {@code javax.security.auth.login.Configuration} class.
 * 
 * @see javax.security.auth.login.LoginContext
 */
public class URLConfiguration extends Configuration {

	/**
	 * Constructor
     * 
     * @param source the {@code Confuguration}'s source
	 * @param options the options shared by all {@code LoginModule}s
	 * 
	 * @throws IOException 
	 */
	public URLConfiguration(
		URL source, 
        Map<String,?> options
	) throws IOException{
		this.url = source;
        this.options = options;
		this.load();
	}
	
	/**
	 * The configuration source
	 */
	private final URL url;
	
    /**
     * The options shared by all {@code LoginModule}s
     */
    private final Map<String,?> options;

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
    private Map<String,List<AppConfigurationEntry>> configuration;
	
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
    @Override
    public synchronized AppConfigurationEntry[] getAppConfigurationEntry (
		String applicationName
	){
		if(this.configuration == null) this.refresh();
		if(this.configuration == null) return null;		
		List<AppConfigurationEntry> entries = this.configuration.get(
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
		Map<String,List<AppConfigurationEntry>> configuration = new LinkedHashMap<String,List<AppConfigurationEntry>>();
		this.newStreamTokenizer();
		
		this.newToken();
		while (this.currentToken != StreamTokenizer.TT_EOF) {
			
			String applicationName = this.tokenizer.sval;
			String moduleClass;
			AppConfigurationEntry.LoginModuleControlFlag controlFlag;
			List<AppConfigurationEntry> configEntries = new LinkedList<AppConfigurationEntry>();
			
			// application name
			this.newToken();
			this.read('{');
			
			// get the modules
			while (this.currentToken != '}') {
				// get the module class name
				moduleClass = this.read("module class name", false);
				
				// controlFlag (required, optional, etc)
				controlFlag = this.toControlFlag(
					this.read("control flag", false)
				);
				
				// get the args
				Map<String,Object> options = new HashMap<String,Object>(this.options);
				while (this.currentToken != ';') {
					String key = this.read("option key", false);
					this.read('=');
					String value = this.read("option value", EXPAND_PROPERTIES);
					options.put(key, value);
				}
				
				this.newToken();
				
				// create the new element
				configEntries.add(
					new AppConfigurationEntry(
						moduleClass,
						controlFlag,
						options
					)
				);
			}
			
			this.read('}');
			this.read(';');
			
			// add this configuration entry
			if (
				configuration.containsKey(applicationName)
			) throw Throwables.initCause(
				new IOException("Can not specify multiple entries for application name"),
				null, // cause
			    BasicException.Code.DEFAULT_DOMAIN,
			    BasicException.Code.INVALID_CONFIGURATION,
				new BasicException.Parameter("url", this.url),
				new BasicException.Parameter("applicationName", applicationName)
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
	 * @throws IOException
	 */
	private AppConfigurationEntry.LoginModuleControlFlag toControlFlag(
		String value
	) throws IOException{
		if (value.equalsIgnoreCase("REQUIRED")) {
			return AppConfigurationEntry.LoginModuleControlFlag.REQUIRED;
		} 
		else if (value.equalsIgnoreCase("REQUISITE")) {
			return AppConfigurationEntry.LoginModuleControlFlag.REQUISITE;
		} 
		else if (value.equalsIgnoreCase("SUFFICIENT")) {
			return AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT;
		} 
		else if (value.equalsIgnoreCase("OPTIONAL")) {
			return AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL;
		} 
		else throw Throwables.initCause(
			new IOException("Invalid Control Flag"),
			null, // cause
		    BasicException.Code.DEFAULT_DOMAIN,
		    BasicException.Code.INVALID_CONFIGURATION,
			new BasicException.Parameter("url", this.url),
			new BasicException.Parameter("line", this.currentLine),
			new BasicException.Parameter("controlFlag", value)
		);
	}
	
    /**
     * Refresh and reload the Configuration by re-reading all of the
     * login configurations.
     *
     * @exception SecurityException if the caller does not have permission
     *				to refresh the Configuration.
     */
    @Override
    public synchronized void refresh(
	) {		
		try {
			this.load();
		} 
		catch (Exception exception) {
			this.configuration = null;
		}
    }	
		
	/**
	 * Create a new {@code StreamTokenizer}
	 * 
	 * @return an initialized {@code StreamTokenizer}
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
				throw Throwables.initCause(
					new IOException("Unexpected end of file"),
					null, // cause
				    BasicException.Code.DEFAULT_DOMAIN,
				    BasicException.Code.INVALID_CONFIGURATION,
					new BasicException.Parameter("url", this.url),
					new BasicException.Parameter("expected", expected)
				);				
			case '{':
			case ';':
			case '}':
			case '=':
				if (this.currentToken == expected) {
					this.newToken();
				} 
				else {
					throw Throwables.initCause(
						new IOException("Unexpected value"),
						null, // cause
					    BasicException.Code.DEFAULT_DOMAIN,
					    BasicException.Code.INVALID_CONFIGURATION,
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
					);
				}
				break;
				
			default:
				throw Throwables.initCause(
					new IOException("Unexpected value"),
					null, // cause
				    BasicException.Code.DEFAULT_DOMAIN,
				    BasicException.Code.INVALID_CONFIGURATION,
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
				throw Throwables.initCause(
					new IOException("Unexpected end of file"),
					null, // cause
				    BasicException.Code.DEFAULT_DOMAIN,
				    BasicException.Code.INVALID_CONFIGURATION,
					new BasicException.Parameter("url", this.url),								
					new BasicException.Parameter("expected", expected)
				);				
			case '"':
			case StreamTokenizer.TT_WORD:
				String value = this.tokenizer.sval;
				this.newToken();
                return SystemProperties.expand(expand, value);
			default:
				throw Throwables.initCause(
					new IOException("Unexpected value"),
					null, // cause
				    BasicException.Code.DEFAULT_DOMAIN,
				    BasicException.Code.INVALID_CONFIGURATION,
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
		) { this.currentLine++; }
	}
	

	//------------------------------------------------------------------------
	// Extends Object
	//------------------------------------------------------------------------
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
    @SuppressWarnings("unchecked")
    public String toString() {
		try {
			MappedRecord reply = Records.getRecordFactory().createMappedRecord(
				this.getClass().getName()
			);
			reply.setRecordShortDescription("URL: " + this.url);
			for(Map.Entry<String,List<AppConfigurationEntry>> e : this.configuration.entrySet()){
				List<DelegatingMappedRecord> v = new ArrayList<DelegatingMappedRecord>();
				for(AppConfigurationEntry m : e.getValue()){
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
		} 
		catch (Exception e) {
			return super.toString();
		}
	}

}

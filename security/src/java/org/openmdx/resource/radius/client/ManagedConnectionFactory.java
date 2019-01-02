/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Managed RADIUS Connection Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.resource.radius.client;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.logging.ClassicFormatter;
import org.openmdx.resource.spi.AbstractManagedConnectionFactory;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusClient;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;

/**
 * Managed RADIUS Connection Factory
 * <p>
 * <em>Note:<br>
 * The accounting ports are not yet configurable.</em>
 */
public class ManagedConnectionFactory extends AbstractManagedConnectionFactory {

	/**
	 * Constructor
	 * 
	 * @param logger
	 */
	protected ManagedConnectionFactory(
		Logger logger
	){
		if(this.logToAdapter = logger == null) {
			this.logger = Logger.getLogger(
	        	ManagedConnectionFactory.class.getName()
	        ); 
	    	this.logger.addHandler(
	    		new LogHandler()
	    	);
		} else {
			this.logger = logger;
		}
	}
	
	/**
     * Constructor
     */
    public ManagedConnectionFactory() {
    	this(null);
    }

	/**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 6915930278489524854L;

    /**
     * <code>true</code> if the managed connection factory is created without logger
     */
    private final boolean logToAdapter;
    
    /**
     * 
     */
    private final Logger logger;
    
	/**
	 * The RADIUS hosts
	 */
    private String[] hosts;
	
	/**
	 * The authentication ports
	 */
	private int[] authenticationPorts;
	
	/**
	 * The accounting ports
	 */
	private int[] accountingPorts;	

	/**
	 * The socket timeout (in seconds)
	 */
	private BigDecimal socketTimeout = null;

	/**
	 * The NAS address
	 */
	private InetAddress nasAddress = null;
	
	/**
	 * This flag defines whether tracing is enabled or not
	 */
	private boolean trace = false;
	
	/* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#isManagedConnectionShareable()
     */
    @Override
    protected boolean isManagedConnectionShareable() {
	    return false;
    }

	/**
     * @return the trace
     */
    public boolean isTrace() {
    	return trace;
    }

	/**
     * @param trace the trace to set
     */
    public void setTrace(
    	boolean trace
    ) {
    	this.trace = trace;
    }

	/* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#setLogWriter(java.io.PrintWriter)
     */
    @Override
    public void setLogWriter(
    	PrintWriter logWriter
    ){
	    super.setLogWriter(logWriter);
	    if(this.logToAdapter) {
	    	this.logger.setUseParentHandlers(logWriter == null);
	    }
    }

	/* (non-Javadoc)
     * @see org.openmdx.resource.spi.ManagedURLConnectionFactory#setConnectionURL(java.lang.String)
     */
    @Override
    public void setConnectionURL(
    	String connectionURL
    ) {
    	super.setConnectionURL(connectionURL);
    	String[] urls = connectionURL.split("\\s+");
    	this.hosts = new String[urls.length];
    	this.authenticationPorts = new int[urls.length];
    	this.accountingPorts = new int[urls.length];
    	for(int i = 0; i < urls.length; i++) try {
    		String url = urls[i];
    		int s = url.indexOf(';');
    		URI uri = new URI(s < 0 ? url : url.substring(0, s));
    		this.hosts[i] = uri.getHost();
    		int port = uri.getPort();
    		this.authenticationPorts[i] = port < 0 ? 1812 : port;
    		this.accountingPorts[i] = 1813;
    	} catch (URISyntaxException exception) {
    		throw BasicException.initHolder(
    			new IllegalArgumentException(
    				"Invalid RADIUS connection URL configuration. " +
    				"The expected syntax is a blank separated list of \u00AB" +
    				"aaa://\u2039radius-host\u203A:\u2039authentication-port\u203A" +
    				";transport=udp;protocol=radius\u00BB entries.",
    				BasicException.newEmbeddedExceptionStack(
    					exception,
    					BasicException.Code.DEFAULT_DOMAIN,
    					BasicException.Code.INVALID_CONFIGURATION,
    					new BasicException.Parameter("index", i),
    					new BasicException.Parameter("url", urls[i])
    				)
    			)
    		);
    	}
    }

	/**
     * @return the socketTimeout
     */
    public String getSocketTimeout() {
    	return this.socketTimeout == null ? null : this.socketTimeout.toString();
    }

	/**
     * @param socketTimeout the socketTimeout to set
     */
    public void setSocketTimeout(
    	String socketTimeout
    ) {
    	this.socketTimeout = socketTimeout == null ? null : new BigDecimal(socketTimeout);
    }

	/**
     * @return the nasAddress
     */
    public String getNasAddress() {
    	return this.nasAddress.getHostAddress();
    }

	/**
     * @param nasAddress the nasAddress to set
     */
    public void setNasAddress(String nasAddress) {
    	try {
	        this.nasAddress = InetAddress.getByName(nasAddress);
        } catch (UnknownHostException exception) {
	        throw BasicException.initHolder(
    			new IllegalArgumentException(
    				"Invalid Address",
    				BasicException.newEmbeddedExceptionStack(
    					exception,
    					BasicException.Code.DEFAULT_DOMAIN,
    					BasicException.Code.INVALID_CONFIGURATION,
    					new BasicException.Parameter("nasAddress", nasAddress)
    				)
    			)
    		);
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnectionFactory#createConnectionFactory(javax.resource.spi.ConnectionManager)
     */
//	Override
    public Object createConnectionFactory(
        ConnectionManager connectionManager
    ) throws ResourceException {
        return new ConnectionFactory(
        	this,
        	connectionManager
        );
    }


	@Override
    protected ManagedConnection newManagedConnection(
    	Subject subject, 
    	ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
		PasswordCredential credential = getPasswordCredential(subject);
	    try {
	        return new ManagedConnection(
	        	this,
	        	credential, connectionRequestInfo, 
	        	new RadiusClient(
	                this.hosts, 
	                this.authenticationPorts, 
	                this.accountingPorts, 
	                credential == null || credential.getPassword() == null ? this.getPassword() : new String(credential.getPassword()),
	                this.socketTimeout, 
	                this.logger,
	                this.trace, 
	                this.nasAddress
	        	)
	        );
        } catch (RadiusException exception) {
        	throw ResourceExceptions.initHolder(
        		new ResourceException(
	        		"RADIUS client could not be created",
	                BasicException.newEmbeddedExceptionStack(
	                	exception,
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.CREATION_FAILURE
	                )
	            )
        	);
        } catch (InvalidParameterException exception) {
        	throw ResourceExceptions.initHolder(
        		new ResourceException(
	        		"Invalid RADIUS client configuration",
	                BasicException.newEmbeddedExceptionStack(
	                	exception,
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.INVALID_CONFIGURATION
	                )
	            )
        	);
        }
    }

	
    //------------------------------------------------------------------------
    // Extends AbstractManagedConnectionFactory
    //------------------------------------------------------------------------
    
    /**
     * Overriding required for Oracle WebLogic
     * 
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#equals(java.lang.Object)
     */
    @Override
    public boolean equals(
    	Object that
    ) {
        return super.equals(that);
    }

	/**
     * Overriding required for Oracle WebLogic
     * 
     * @see org.openmdx.resource.spi.AbstractManagedConnectionFactory#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    
    
    //------------------------------------------------------------------------
    // Class LogHandler
    //------------------------------------------------------------------------
    
    /**
     * Log handler
     */
    class LogHandler extends Handler {

    	/**
    	 * Constructor
    	 */
    	LogHandler(){
    		if(super.getFormatter() == null) {
    			super.setFormatter(new ClassicFormatter());
    		}
    	}
    	
		@Override
        public void close() throws SecurityException {
			// nothing to do
        }

		@Override
        public void flush() {
			PrintWriter logWriter = ManagedConnectionFactory.this.getLogWriter();
			if(logWriter != null) {
				logWriter.flush();
			}
        }

		@Override
        public void publish(
        	LogRecord record
        ) {
			PrintWriter logWriter = ManagedConnectionFactory.this.getLogWriter();
			if(logWriter != null) {
				logWriter.println(
					this.getFormatter().format(record)
				);
			}
        }
    	
    }
    
}

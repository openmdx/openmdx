/*
 * ====================================================================
 * Project:     openMDX/OpenEJB, http://www.openmdx.org/
 * Name:        $Id: ExtendedService.java,v 1.2 2009/11/13 16:51:20 wfro Exp $
 * Description: ExtendedService
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/13 16:51:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.catalina.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class ExtendedService extends StandardService {

    //-----------------------------------------------------------------------
	static class CommandListener implements Runnable {
		
		public CommandListener(
			int port,
			ExtendedService service
	    ) {
			this.port = port;
			this.service = service;
		}
		
	    public void run(
	    ) {
	        // Set up a server socket to wait on
	        ServerSocket serverSocket = null;
	        try {
	            serverSocket = new ServerSocket(this.port, 1, InetAddress.getByName("localhost"));
	        } catch (IOException e) {
	            log.error("Tomcat/ExtendedService.await: create[" + this.port + "]: ", e);
	        }
	        // Loop waiting for a connection and a valid command
	        while (true) {
	            // Wait for the next connection
	            Socket socket = null;
	            InputStream stream = null;
	            try {
	                socket = serverSocket.accept();
	                socket.setSoTimeout(10 * 1000);  // Ten seconds
	                stream = socket.getInputStream();
	            } catch (AccessControlException ace) {
	                log.warn("Tomcat/ExtendedService.accept security exception: " + ace.getMessage(), ace);
	                continue;
	            } catch (IOException e) {
	                log.error("Tomcat/ExtendedService.await: accept: ", e);
	            }
	            // Read a set of characters from the socket
	            StringBuffer command = new StringBuffer();
	            int expected = 1024;
	            while (expected > 0) {
	                int ch = -1;
	                try {
	                    ch = stream.read();
	                } catch (IOException e) {
	                    log.warn("Tomcat/ExtendedService.await: read: ", e);
	                    ch = -1;
	                }
	                if (ch < 32)  // Control character or EOF terminates loop
	                    break;
	                command.append((char) ch);
	                expected--;
	            }
	            // Close the socket now that we are done with it
	            try {
	                socket.close();
	            } catch (IOException e) {
	                ;
	            }
	            // Match against our command string
	            if("stopconnectors".equalsIgnoreCase(command.toString())) {
	            	try {
		    			this.service.pauseConnectors();
		    			this.service.stopConnectors();
		            }
	    			catch(Exception e) {
		                log.warn("Tomcat/ExtendedService.await: Error when stopping connectors", e);        				
	    			}
	            }
	            else if("startconnectors".equalsIgnoreCase(command.toString())) {
	    			try {
	        			this.service.startConnectors();
	    			}
	    			catch(Exception e) {
		                log.warn("Tomcat/ExtendedService.await: Error when stopping connectors", e);        				
	    			}
	            }
	            else {
	            	log.warn("Tomcat/ExtendedService.await: Invalid command '" + command.toString() + "' received");
	            }
	        }
	    }
	    
	    private final int port;
	    private final ExtendedService service;
	}
	
    //-----------------------------------------------------------------------
    public void setPort(
    	String port
    ) {
    	this.port = port;
    }
    
    //-----------------------------------------------------------------------
    public String getPort(
    ) {
    	return this.port;
    }
    
    //-----------------------------------------------------------------------
    protected boolean startConnectors(
    ) {
    	boolean success = true;
        // Start our defined Connectors second
        synchronized(this.connectors) {
            for (int i = 0; i < this.connectors.length; i++) {
                if (this.connectors[i] instanceof Lifecycle) {
                	try {
                		((Lifecycle)this.connectors[i]).start();                		
                	}
            		// Ignore. Connectors can be started later with command 'startConnectors'                	
                	catch(Exception e) {
                		success = false;
                	}
                }
            }
        }           	
        return success;
    }
    
    //-----------------------------------------------------------------------
    protected void pauseConnectors(
    ) throws LifecycleException {
        // Stop our defined Connectors first
        synchronized (this.connectors) {
            for (int i = 0; i < this.connectors.length; i++) {
            	this.connectors[i].pause();
            }
        }
        // Heuristic: Sleep for a while to ensure pause of the connector
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore
        }    	
    }
    
    //-----------------------------------------------------------------------
    protected void stopConnectors(
    ) throws LifecycleException {
        synchronized (this.connectors) {
            for (int i = 0; i < this.connectors.length; i++) {
                if (this.connectors[i] instanceof Lifecycle) {
                	try {
                		((Lifecycle)this.connectors[i]).stop();
                	}
                   	catch(Exception e) {}
                }
            }
        }
    }
    
    //-----------------------------------------------------------------------
    protected void initializeConnectors(
    ) {
        synchronized(this.connectors) {
            for (int i = 0; i < this.connectors.length; i++) {
                if (this.connectors[i] instanceof Lifecycle) {
                	try {
                		this.connectors[i].initialize();
                	} 
            		// Ignore. Connectors can be started later with command 'startConnectors'                	
                	catch(Exception e) {}
                }
            }
        }
    }
    
    //-----------------------------------------------------------------------
    @Override
	public void start(
	) throws LifecycleException {
    	// Do not let super.start() start the connectors
		Connector[] connectors = super.connectors;
		super.connectors = new Connector[]{};
		super.start();
		super.connectors = connectors;
		// Now start the connectors if property autostartConnectors is not set or set to true. 
		// In case one of the connectors can not be started stop all. They can be started 
		// explicitly with the command 'startConnectors'
		String autostartConnectors = System.getProperty(ExtendedService.class.getName() + ".autostartConnectors");
		if(autostartConnectors == null || Boolean.valueOf(autostartConnectors).booleanValue()) {
			if(!this.startConnectors()) {
				this.stopConnectors();
			}
		}
		new Thread(
			new CommandListener(
				Integer.valueOf(this.port), 
				this
			)
		).start();
	}

    //-----------------------------------------------------------------------
	@Override
	public void initialize(
	) throws LifecycleException {
		// Do not let super.initialize() initialize the connectors.
		Connector[] connectors = super.connectors;
		super.connectors = new Connector[]{};
		super.initialize();
		super.connectors = connectors;
		// Now initialize the connectors
		this.initializeConnectors();
	}

	//-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static Log log = LogFactory.getLog(ExtendedService.class);
    
    protected String port;
    
}

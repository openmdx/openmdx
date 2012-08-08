/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SocketLoggingMechanism.java,v 1.1 2008/03/21 18:22:02 hburger Exp $
 * Description: Socket Logging Mechanism
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:22:02 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.log.impl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.openmdx.compatibility.kernel.log.LogEvent;
import org.openmdx.compatibility.kernel.log.LogFormatter;
import org.openmdx.compatibility.kernel.log.SysLog;


/**
 * This logging mechanism logs events to a socket stream.
 */
public class SocketLoggingMechanism 
	extends AbstractLoggingMechanism
{

	private static SocketLoggingMechanism singleton =
		new SocketLoggingMechanism();


	protected SocketLoggingMechanism() {
	    super();
	}
	


	/**
	 * Returns the mechanism object. The mechanism is shared so it returns a 
	 * singleton
	 * 
	 * @return the mechanism singleton
	 */
	public static AbstractLoggingMechanism getInstance()
	{
		return SocketLoggingMechanism.singleton;
	}


	/**
	 * This method opens the particular logging mechanism so that messages
	 * can be output.
	 * 
	 * @param log  A Logger
	 */
	synchronized 
	protected void open(
		Log log)
	{
		// A shared mechanism must be configured using SysLog properties!
		LogProperties logProperties = 
			isSharedLog() 
				? SysLog.getLogger().getLogProperties()
				: log.getLogProperties();
        

		// read the log configuration
		String host = logProperties.getProperty(
							log.getName(),
							"SocketLoggingMechamism.host",
							DEFAULT_HOST);

		String port = logProperties.getProperty(
							log.getName(),
							"SocketLoggingMechamism.port",
							String.valueOf(DEFAULT_PORT));

		String format = logProperties.getProperty(
							log.getName(),
							"LogFormat",
							null);

   		 // replace formatter
		this.formatter = new LogFormatterStandard(format);

		try {
			this.port = Integer.parseInt(port, 10);
		}catch(NumberFormatException e) {
			// error parsing port number
			LogLog.criticalError(
				this.getClass(),
				"open",
		        "Bad port number log property  'SocketLoggingMechamism.port=" 
		        + port + "'. Using default port " + DEFAULT_PORT,
		        "");

			// try default port
			this.port = DEFAULT_PORT;
		}

		try {
			this.address = InetAddress.getByName(host);
		}
		catch(Exception ex1) {
			// Could not find address of <host>
			LogLog.criticalError(
				this.getClass(),
				"open",
		        "Bad host name log property 'SocketLoggingMechamism.host=" 
		        + port + "'. Using localhost",
		        ex1);

			try {
				// Try localhost
				this.address = InetAddress.getLocalHost();
			} catch(Exception ex2) { this.address = null; }
		}

		establishConnection(this.address, this.port);

		super.open(log); // The mechanism is now open
	}


	/**
	 * This method closes the particular logging mechanism so that messages
	 * no longer get logged to the mechanism.
	 */
	synchronized 
	protected void close()
	{
		super.close(); // The mechanism is now closed

		dropConnection();
	}


	protected void notifyLogOpened(Log log, int loggingLevel) {
	    // Do not handle any kind of notification events
	}
	protected void notifyLogClosed(Log log) {
	    // Do not handle any kind of notification events
	}
	protected void notifyLoggingLevelChange(Log log, int loggingLevel) {
	    // Do not handle any kind of notification events
	}
	protected void notifyLoggingPerformanceChange(Log log, boolean state) {
	    // Do not handle any kind of notification events
	}
	protected void notifyLoggingStatisticsChange(Log log, boolean state) {
	    // Do not handle any kind of notification events
	}


	/**
	 * Logs a log event.
	 * 
	 * @param log A logger
	 * @param event A log event
	 */
	protected void logEvent(
		Log       log, 
		LogEvent  event)
	{
		if (!isOpen()) return;
		
		synchronized(this) {
			if (this.writer != null) {
				try {
					this.writer.write(format(event));
					this.writer.flush();
					return;
				}catch(IOException e) {
				    // ignore
				}
			}
	
			// try to reestablish the connection
			long  now = System.currentTimeMillis();
			if ((now - this.lastConnectionAttempt) > RECONNECTION_DELAY) {
				dropConnection();
				establishConnection(this.address, this.port);
			}else{
				return;
			}
	
			// try a 2nd time after the reestablish attempt
			if (this.writer != null) {
				try {
					this.writer.write(format(event));
					this.writer.flush();
				}catch(IOException e) {
				    // ignore
				}
			}
		}
	}


	/** 
	 * This mechanism accepts statistics logs 
	 * 
	 * @return true to indicate that the mechanism accepts statistics
	 */
    public boolean acceptsStatistics() { return true; }


	/** 
	 * Returns the name of the mechanism. 
	 * 
	 * @return The mechanism name
	 */
	public String getName() { return "SocketLoggingMechanism"; }


	/**
	 * Return a debug string.
	 */
	public String toString()
	{
		if (this.address != null) {
		    return getName() + " is logging to socket: host=" +
		                       this.address.getHostName() + ", port=" + this.port;
		}else{
		    return getName() + " is not connected";
		}
	}


	/**
	 * Drop the current connection
	 */
	private void dropConnection()
	{
		try {
			if (this.bufferedStream != null) {
				this.bufferedStream.close();
				this.bufferedStream = null;
			}
		}catch(IOException e) {
		 // ignore
		}

		try {
			if (this.rawStream != null) {
				this.rawStream.close();
				this.rawStream = null;
			}
		}catch(IOException e) {
		 // ignore
		}

		try {
			if (this.socket != null) {
				this.socket.close();
				this.socket = null;
			}
		}catch(IOException e) {
		 // ignore
		}

		this.port = DEFAULT_PORT;
		this.address = null;
	}


	/**
	 * Establish a new connection
	 * 
	 * @param address
	 * @param port
	 */
	private void establishConnection(
		InetAddress  address, 
		int          port)
	{
		this.lastConnectionAttempt = System.currentTimeMillis();

		while (true) {
			if (address == null) break;

			try {
				this.socket = new Socket(address, port);
			}catch(IOException e) {
				this.socket = null;
				break;
			}

			try {
				this.rawStream = this.socket.getOutputStream();
			}catch(UnknownHostException e) {
			 // ignore
			}catch(IOException e) {
				try {
					this.socket.close();
				}catch(IOException es) {
				 // ignore
				}
				this.socket = null;
				break;
			}
			this.bufferedStream = new BufferedOutputStream(this.rawStream);

			try {
				this.writer = new OutputStreamWriter(this.bufferedStream, "ASCII");

				LogLog.trace(
					this.getClass(),
					"establishConnection",
				    "SocketLoggingMechamism: connection to host=" 
				    + this.address.getHostName() + ", port=" + this.port 
				    + " established",
				    "");
				}
			catch(Exception ex) {
				try {
					this.bufferedStream.close();
					this.socket.close();
				}catch(IOException es) {
				 // ignore
				}

				this.bufferedStream = null;
				this.socket = null;
				break;
			}
		}

		LogLog.criticalError(
			this.getClass(),
			"establishConnection",
	        "SocketLoggingMechamism: establishing connection to  host=" 
	        + this.address.getHostName() + ", port=" + this.port + " failed",
	        "");
	}


	/** 
	 * A simple formatter
	 * 
	 * @param event A log event
	 */
	private String  format(
		LogEvent  event)
	{
        return new StringBuilder(
        ).append(
            "<LogEvent>"
        ).append(
            this.formatter.format(event)
        ).append(
            "</LogEvent>"
        ).toString();
	}




	final static private String  DEFAULT_HOST = "localhost";

	final static private int     DEFAULT_PORT = 1560;

	final static int              RECONNECTION_DELAY = 30000; // 30 secs



	private int                port = DEFAULT_PORT;

	private InetAddress        address;

	private Socket             socket;

	private OutputStreamWriter writer;

	private OutputStream       rawStream;

	private OutputStream       bufferedStream;

	private LogFormatter       formatter = new LogFormatterStandard(null);

	private long               lastConnectionAttempt = 0;

}


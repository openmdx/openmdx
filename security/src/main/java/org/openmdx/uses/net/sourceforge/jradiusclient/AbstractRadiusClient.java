/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: AbstractRadiusClient
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
package org.openmdx.uses.net.sourceforge.jradiusclient;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract Radius CLient
 */
public class AbstractRadiusClient {

	/**
	 * Constructor
	 * 
	 * @param logging
	 * @param trace
	 */
	protected AbstractRadiusClient(
		Logger logger,
		boolean trace
	){
		this.logger = logger;
		this.trace = trace;
		synchronized(AbstractRadiusClient.class) {
			this.id = Integer.valueOf(AbstractRadiusClient.nextId++);
		}
	}

	/**
	 * The instance id
	 */
	private final Integer id;

	/**
	 * The next instance id
	 */
	private static int nextId;

	/**
	 * 
	 */
	private final Logger logger;

	/**
	 * 
	 */
	private final boolean trace;

	/**
	 * The JRadius client default logger name
	 */
	protected static final String DEFAULT_LOGGER_NAME = "org.openmdx.uses.net.sourceforge.jradiusclient";

	/**
	 * Log at warn level
	 * 
	 * @param format
	 * @param id
	 * @param argument2
	 * @param argument3
	 */
	protected final void logWarning(
		String format,
		byte id,
		Object argument2,
		Object argument3
	){
		if(this.logger.isLoggable(Level.WARNING)) {
			this.logger.warning(
				MessageFormat.format(
					format,
					new Object[]{
						this.id,
						valueOf(id),
						argument2,
						argument3
					}
				)
			);
		}
	}

	/**
	 * Log at debug level
	 * 
	 * @param format
	 * @param id
	 * @param argument2
	 */
	protected final void logDebug(
		String format,
		int id,
		Object argument2
	){
		if(
			this.logger != null && 
			this.trace &&
			this.logger.isLoggable(Level.FINE)
		){
			this.logger.fine(
				MessageFormat.format(
					format,
					new Object[]{
						this.id,
						Integer.valueOf(id),
						argument2
					}
				)
			);
		}
	}

	/**
	 * Log at debug  level
	 * 
	 * @param format
	 * @param argument1
	 */
	protected final void logDebug(
		String format,
		Object argument1
	){
		if(
			this.logger != null && 
			this.trace &&
			this.logger.isLoggable(Level.FINE)
		){
			this.logger.fine(
				MessageFormat.format(
					format,
					new Object[]{
						this.id,
						argument1
					}
				)
			);
		}
	}

	/**
	 * Log at info level
	 * 
	 * @param format
	 */
	protected final void logInfo(
		String format
	){
		if(
			this.logger != null && 
			this.logger.isLoggable(Level.INFO)
		){
			this.logger.info(
				MessageFormat.format(
					format,
					new Object[]{
						this.id
					}
				)
			);
		}
	}

	/**
	 * Log at warn level
	 * 
	 * @param message
	 */
	protected final void logWarning(
		String format,
		Object argument1
	){
		if(
			this.logger != null &&
			this.logger.isLoggable(Level.WARNING)
		){
			this.logger.warning(
				MessageFormat.format(
					format,
					new Object[]{
						this.id,
						argument1
					}
				)
			);
		}
	}

	/**
	 * Log at severe level
	 * 
	 * @param message
	 */
	protected final void logSevere(
		String format
	){
		if(
			this.logger != null &&
			this.logger.isLoggable(Level.SEVERE)
		){
			this.logger.severe(
				MessageFormat.format(
					format,
					new Object[]{
						this.id,
					}
				)
			);
		}
	}

	/**
	 * Convert a number value to a number object
	 * 
	 * @param value the byte value 
	 * 
	 * @return the Byte object
	 */
	protected final static Byte valueOf(
		byte value
	){
		return Byte.valueOf(value);
	}

}

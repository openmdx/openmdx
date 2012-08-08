/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PoseidonLoggingMechanism.java,v 1.3 2004/06/30 07:34:47 rbruder Exp $
 * Description: lab client
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/06/30 07:34:47 $
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
package org.openmdx.model1.poseidon.plugin;

import org.openmdx.kernel.log.LogEvent;
import org.openmdx.kernel.log.LogLevel;
import org.openmdx.kernel.log.impl.AbstractFileLoggingMechanism;
import org.openmdx.kernel.log.impl.Log;

import com.gentleware.services.Services;


/**
 * This logging mechanism is used to redirect all openMDX log messages to 
 * Poseidon's own log service which writes to the file Poseidon.log.
 */
public class PoseidonLoggingMechanism
  extends AbstractFileLoggingMechanism 
{

    protected static PoseidonLoggingMechanism singleton =
      new PoseidonLoggingMechanism();


    protected PoseidonLoggingMechanism() {}
  
  
    /**
     * Returns the mechanism object. The mechanism is shared so it returns a 
     * singleton
     * 
     * @return the mechanism singleton
     */
    public static PoseidonLoggingMechanism getInstance()
    {
      return PoseidonLoggingMechanism.singleton;
    }


    /** 
     * This file mechanism is not dated
     * 
     * @return false to indicate that the mechanism is not dated
     */
      protected boolean isDatedLog() { return false; }


    /**
     * Shared logging needs an additional qualifier on each message
     * logged, prefix the message with the log name.
     */
    String loggingPrefix(
      Log log
    ) {
      return ( log.getName() );
    }


    /** 
     * Returns the name of the mechanism. 
     * 
     * @return The mechanism name
     */
    public String getName() { return "PoseidonLoggingMechanism"; }


    protected void logEvent(
      Log log, 
      LogEvent event
    ) {
      // map openMDX log events to Poseidon log
      switch (event.getLoggingLevel())
      {
        case LogLevel.LOG_LEVEL_CRITICAL_ERROR:
          Services.logFatal(getFormatter().format(event));
          break;
        case LogLevel.LOG_LEVEL_ERROR:
          Services.logError(getFormatter().format(event));
          break;
        case LogLevel.LOG_LEVEL_WARNING:
          Services.logWarn(getFormatter().format(event));
          break;
        case LogLevel.LOG_LEVEL_INFO:
          Services.logInfo(getFormatter().format(event));
          break;
        case LogLevel.LOG_LEVEL_DETAIL:
          // due to an error in Poseidon (Poseidon crashes if the log level
          // DEBUG is used), openMDX detail messages are logged with Poseidon
          // log level info 
          // Services.logDebug(getFormatter().format(event));
          Services.logInfo(getFormatter().format(event));
          break;
        case LogLevel.LOG_LEVEL_TRACE:
          // due to an error in Poseidon (Poseidon crashes if the log level
          // DEBUG is used), openMDX trace messages are logged with Poseidon
          // log level info 
          // Services.logDebug(getFormatter().format(event));
          Services.logInfo(getFormatter().format(event));
          break;
        default:
          Services.logInfo(getFormatter().format(event));
          break;
      }
    }
  }



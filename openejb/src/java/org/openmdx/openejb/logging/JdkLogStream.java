/*
 * ====================================================================
 * Project:     openMDX/OpenEJB, http://www.openmdx.org/
 * Name:        $Id: JdkLogStream.java,v 1.1 2009/03/31 18:07:33 wfro Exp $
 * Description: Slf4jLogStream
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/31 18:07:33 $
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
package org.openmdx.openejb.logging;

import java.util.logging.Level;

import org.apache.openejb.util.LogCategory;

public class JdkLogStream 
    implements org.apache.openejb.util.LogStream{
  
    public JdkLogStream(
        LogCategory logCategory            
    ) {
        this.logCategory = logCategory;
        this.logger = java.util.logging.Logger.getLogger(logCategory.getName());
    }

    public void debug(String message) {
        this.logger.log(Level.FINEST, message);
    }

    public void debug(String message, Throwable t) {
        this.logger.log(Level.FINEST, message, t);
    }

    public void error(String message) {
        this.logger.log(Level.SEVERE, message);
    }

    public void error(String message, Throwable t) {
        this.logger.log(Level.SEVERE, message, t);
    }

    public void fatal(String message) {
        this.logger.log(Level.SEVERE, message);
    }

    public void fatal(String message, Throwable t) {
        this.logger.log(Level.SEVERE, message, t);
    }

    public void info(String message) {
        this.logger.log(Level.INFO, message);
    }

    public void info(String message, Throwable t) {
        this.logger.log(Level.INFO, message, t);
    }

    public boolean isDebugEnabled() {
        return this.logger.isLoggable(Level.FINEST);
    }

    public boolean isErrorEnabled() {
        return this.logger.isLoggable(Level.SEVERE);
    }

    public boolean isFatalEnabled() {
        return this.logger.isLoggable(Level.SEVERE);
    }

    public boolean isInfoEnabled() {
        return this.logCategory == LogCategory.TRANSACTION ?
            false :
            this.logger.isLoggable(Level.INFO);
    }

    public boolean isWarnEnabled() {
        return this.logger.isLoggable(Level.WARNING);
    }

    public void warn(String message) {
        this.logger.log(Level.WARNING, message);
    }

    public void warn(String message, Throwable t) {
        this.logger.log(Level.WARNING, message, t);
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final java.util.logging.Logger logger;
    private final LogCategory logCategory;
}

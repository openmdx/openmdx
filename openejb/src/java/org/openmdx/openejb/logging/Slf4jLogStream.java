/*
 * ====================================================================
 * Project:     openMDX/OpenEJB, http://www.openmdx.org/
 * Name:        $Id: Slf4jLogStream.java,v 1.3 2009/01/23 17:27:58 wfro Exp $
 * Description: Slf4jLogStream
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/23 17:27:58 $
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

import org.apache.openejb.util.LogCategory;

public class Slf4jLogStream 
    implements org.apache.openejb.util.LogStream{
  
    public Slf4jLogStream(
        LogCategory logCategory            
    ) {
        this.logCategory = logCategory;
        this.logger = org.slf4j.LoggerFactory.getLogger(logCategory.getName());
    }

    public void debug(String message) {
        this.logger.debug(message);
    }

    public void debug(String message, Throwable t) {
        this.logger.debug(message, t);
    }

    public void error(String message) {
        this.logger.error(message);
    }

    public void error(String message, Throwable t) {
        this.logger.error(message, t);
    }

    public void fatal(String message) {
        this.logger.error(message);
    }

    public void fatal(String message, Throwable t) {
        this.logger.error(message, t);
    }

    public void info(String message) {
        this.logger.info(message);
    }

    public void info(String message, Throwable t) {
        this.logger.info(message, t);
    }

    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return this.logger.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return this.logCategory == LogCategory.TRANSACTION ?
            false :
            this.logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    public void warn(String message) {
        this.logger.warn(message);
    }

    public void warn(String message, Throwable t) {
        this.logger.warn(message, t);
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final org.slf4j.Logger logger;
    private final LogCategory logCategory;
}

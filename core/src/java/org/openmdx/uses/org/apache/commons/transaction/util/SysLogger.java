/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SysLogger.java,v 1.1 2005/03/24 14:07:51 hburger Exp $
 * Description: SysLogger
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/03/24 14:07:51 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.uses.org.apache.commons.transaction.util;

import org.openmdx.kernel.log.LogLevel;
import org.openmdx.kernel.log.SysLog;


/**
 * SysLogger
 */
public class SysLogger
        implements LoggerFacade {

    /**
     * Constructor
     */
    public SysLogger() {
        super();
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#createLogger(java.lang.String)
     */
    public LoggerFacade createLogger(String name) {
        return this;
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#logInfo(java.lang.String)
     */
    public void logInfo(String message) {
        SysLog.info(message, null, 1);
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#logFine(java.lang.String)
     */
    public void logFine(String message) {
        SysLog.detail(message, null, 1);
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#isFineEnabled()
     */
    public boolean isFineEnabled() {
        return LogLevel.LOG_LEVEL_DETAIL <= SysLog.getLogger().getLoggingLevel();
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#logFiner(java.lang.String)
     */
    public void logFiner(String message) {
        SysLog.detail(message, null, 1);
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#isFinerEnabled()
     */
    public boolean isFinerEnabled() {
        return LogLevel.LOG_LEVEL_DETAIL <= SysLog.getLogger().getLoggingLevel();
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#logFinest(java.lang.String)
     */
    public void logFinest(String message) {
        SysLog.trace(message, null, 1);
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#isFinestEnabled()
     */
    public boolean isFinestEnabled() {
        return SysLog.isTraceOn();
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#logWarning(java.lang.String)
     */
    public void logWarning(String message) {
        SysLog.warning(message, null, 1);
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#logWarning(java.lang.String, java.lang.Throwable)
     */
    public void logWarning(String message, Throwable t) {
        SysLog.warning(message, t, 1);
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#logSevere(java.lang.String)
     */
    public void logSevere(String message) {
        SysLog.criticalError(message, null, 1);
    }

    /* (non-Javadoc)
     * @see org.openmdx.uses.org.apache.commons.transaction.util.LoggerFacade#logSevere(java.lang.String, java.lang.Throwable)
     */
    public void logSevere(String message, Throwable t) {
        SysLog.criticalError(message, t, 1);
    }

}

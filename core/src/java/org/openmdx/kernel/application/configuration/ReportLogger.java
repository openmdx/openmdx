/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ReportLogger.java,v 1.1 2008/09/08 11:45:37 hburger Exp $
 * Description: ReportLogger 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/08 11:45:37 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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

package org.openmdx.kernel.application.configuration;

import org.openmdx.kernel.exception.BasicException;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * ReportLogger
 *
 */
public class ReportLogger
    extends MarkerIgnoringBase
{
    
    /**
     * Constructor 
     *
     * @param report
     */
    public ReportLogger(Report report) {
        this.report = report;
    }
    
    private final Report report;

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String)
     */
    public void debug(String msg) {
        // Debug is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object)
     */
    public void debug(String format, Object arg) {
        // Debug is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void debug(String format, Object arg1, Object arg2) {
        // Debug is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Object[])
     */
    public void debug(String format, Object... argArray) {
        // Debug is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug(String msg, Throwable t) {
        // Debug is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String)
     */
    public void error(String msg) {
        this.report.addError(msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object)
     */
    public void error(String format, Object arg) {
        this.error(
            MessageFormatter.format(format, arg)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void error(String format, Object arg1, Object arg2) {
        this.error(
            MessageFormatter.format(format, arg1, arg2)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Object[])
     */
    public void error(String format, Object... argArray) {
        this.error(
            MessageFormatter.format(format, argArray)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#error(java.lang.String, java.lang.Throwable)
     */
    public void error(String msg, Throwable t) {
        this.error(msg, t);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#getName()
     */
    public String getName() {
        return this.report.getName();
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String)
     */
    public void info(String msg) {
        this.report.addInfo(msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object)
     */
    public void info(String format, Object arg) {
        this.report.addInfo(
            MessageFormatter.format(format, arg)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void info(String format, Object arg1, Object arg2) {
        this.report.addInfo(
            MessageFormatter.format(format, arg1, arg2)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Object[])
     */
    public void info(String format, Object... argArray) {
        this.report.addInfo(
            MessageFormatter.format(format, argArray)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#info(java.lang.String, java.lang.Throwable)
     */
    public void info(String msg, Throwable t) {
        this.report.addInfo(msg + ": " + BasicException.toStackedException(t));
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String)
     */
    public void trace(String msg) {
        // Trace is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object)
     */
    public void trace(String format, Object arg) {
        // Trace is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void trace(String format, Object arg1, Object arg2) {
        // Trace is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Object[])
     */
    public void trace(String format, Object... argArray) {
        // Trace is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#trace(java.lang.String, java.lang.Throwable)
     */
    public void trace(String msg, Throwable t) {
        // Trace is disabled
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String)
     */
    public void warn(String msg) {
        this.report.addWarning(msg);
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object)
     */
    public void warn(String format, Object arg) {
        this.report.addWarning(
            MessageFormatter.format(format, arg)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object[])
     */
    public void warn(String format, Object... argArray) {
        this.report.addWarning(
            MessageFormatter.format(format, argArray)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public void warn(String format, Object arg1, Object arg2) {
        this.report.addWarning(
            MessageFormatter.format(format, arg1, arg2)
        );
    }

    /* (non-Javadoc)
     * @see org.slf4j.Logger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn(String msg, Throwable t) {
        this.report.addWarning(msg, t);
    }


}

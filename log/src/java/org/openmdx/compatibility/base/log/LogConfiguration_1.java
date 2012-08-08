/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LogConfiguration_1.java,v 1.2 2009/01/09 13:52:33 wfro Exp $
 * Description: LogConfiguration_1 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/09 13:52:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.compatibility.base.log;

import org.openmdx.application.control.LogConfiguration_1_0;
import org.openmdx.compatibility.kernel.log.LogLevel;
import org.openmdx.compatibility.kernel.log.SysLog;

/**
 * LogConfiguration_1
 */
public class LogConfiguration_1
    implements LogConfiguration_1_0
{

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#enablePerformanceLog()
     */
    public void enablePerformanceLog() {
        SysLog.getLogConfig().enablePerformanceLog(true);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#enableStatisticsLog()
     */
    public void enableStatisticsLog() {
        SysLog.getLogConfig().enableStatisticsLog(true);
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#setLogLevelCriticalError()
     */
    public void setLogLevelCriticalError() {
        SysLog.getLogConfig().setLogLevel(LogLevel.LOG_LEVEL_CRITICAL_ERROR);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#setLogLevelDetail()
     */
    public void setLogLevelDetail() {
        SysLog.getLogConfig().setLogLevel(LogLevel.LOG_LEVEL_DETAIL);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#setLogLevelError()
     */
    public void setLogLevelError() {
        SysLog.getLogConfig().setLogLevel(LogLevel.LOG_LEVEL_ERROR);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#setLogLevelInfo()
     */
    public void setLogLevelInfo() {
        SysLog.getLogConfig().setLogLevel(LogLevel.LOG_LEVEL_INFO);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#setLogLevelTrace()
     */
    public void setLogLevelTrace() {
        SysLog.getLogConfig().setLogLevel(LogLevel.LOG_LEVEL_TRACE);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#setLogLevelWarning()
     */
    public void setLogLevelWarning() {
        SysLog.getLogConfig().setLogLevel(LogLevel.LOG_LEVEL_WARNING);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return SysLog.getLogConfig().dumpLogProperties();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#setConfigName(java.lang.String)
     */
    public void setConfigName(String configName) {
        SysLog.setConfigName(configName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.LogConfiguration_1_0#setLogSOurce(java.lang.String)
     */
    public void setLogSource(String logSource) {
        SysLog.setLogSource(logSource);
    }
    
}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Foreign Log Record 
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
package org.openmdx.kernel.log;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Foreign Log Record
 */
public class ForeignLogRecord
    extends LogRecord 
{

    /**
     * Constructor 
     * 
     * @param loggerName The logger's name
     * @param level
     * @param msg
     */
    public ForeignLogRecord(
        String loggerName,
        String loggerClass, 
        Level level, 
        String msg
    ) {
        super(level, msg);
        super.setLoggerName(loggerName);
        this.loggerClass = loggerClass;
    }

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 2754042576241756966L;

    /**
     * The logger class
     */
    private transient String loggerClass;

    /* (non-Javadoc)
     * @see java.util.logging.LogRecord#getSourceClassName()
     */
    @Override
    public String getSourceClassName(
    ){
        if(this.loggerClass != null) inferCaller();
        return super.getSourceClassName();
    }

    /* (non-Javadoc)
     * @see java.util.logging.LogRecord#getSourceMethodName()
     */
    @Override
    public String getSourceMethodName() {
        if(this.loggerClass != null) inferCaller();
        return super.getSourceMethodName();
    }

    /* (non-Javadoc)
     * @see java.util.logging.LogRecord#setSourceClassName(java.lang.String)
     */
    @Override
    public void setSourceClassName(String sourceClassName) {
        this.loggerClass = null; // no more need to infer caller
        super.setSourceClassName(sourceClassName);
    }

    /* (non-Javadoc)
     * @see java.util.logging.LogRecord#setSourceMethodName(java.lang.String)
     */
    @Override
    public void setSourceMethodName(
        String sourceMethodName
    ){ 
        this.loggerClass = null; // no more need to infer caller
        super.setSourceMethodName(sourceMethodName);
    }

    /**
     * Method to infer the caller's class and method names
     */
    private void inferCaller(
    ) {
        if(this.loggerClass == null) return;
        //
        // Get the stack trace.
        //
        StackTraceElement stack[] = (new Throwable()).getStackTrace();
        //
        // First, search back to a method in the Logger class.
        //
        int ix = 0;
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix++];
            String cname = frame.getClassName();
            if (cname.equals(loggerClass)) {
                break;
            }
        }
        //
        // Now search for the first frame before the "Logger" class.
        //
        while (ix < stack.length) {
            StackTraceElement frame = stack[ix++];
            String cname = frame.getClassName();
            if (!cname.equals(loggerClass)) {
                //
                // We've found the relevant frame.
                //
                super.setSourceClassName(cname);
                super.setSourceMethodName(frame.getMethodName());
                break;
            }
        }
        //
        // no more need to infer caller
        //
        this.loggerClass = null;
    }

}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StackTrace.java,v 1.1 2008/03/21 18:22:02 hburger Exp $
 * Description: Stack Trace
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:22:02 $
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
package org.openmdx.kernel.log.impl;



/**
 * This class encapsulates the functionality to parse a string
 * containing a stack trace and produce the individual attributes of
 * the stack trace.
 */
class StackTrace 
	implements LogLocation
{
    
    final private StackTraceElement stackTraceElement;

    /**
     * Parses for the trace attributes like classname, methodname, linenr, ...
     * when there is no exception at hand. Actually this constructor can be used
     * to get the trace attributes needed for tracing and logging. Any
     * stack trace line can be used for parsing.
     */
    StackTrace(int numLevelsUp)
    {
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        int index = numLevelsUp + 2;
        this.stackTraceElement = index < stackTraceElements.length ? stackTraceElements[index]: null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.log.impl.LogLocation#getClassname()
     */
    public String getClassname() {
        return this.stackTraceElement == null ? "" : this.stackTraceElement.getClassName();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.log.impl.LogLocation#getMethod()
     */
    public String getMethod() {
        return this.stackTraceElement == null ? "" : this.stackTraceElement.getMethodName();
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.log.impl.LogLocation#getLineNumber()
     */
    public int getLineNumber() {
        return this.stackTraceElement == null ? -1 : this.stackTraceElement.getLineNumber();
    }

}



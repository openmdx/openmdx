/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractStackTrace.java,v 1.7 2006/08/11 09:24:13 hburger Exp $
 * Description: Stack Trace baseclass
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/11 09:24:13 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.log.impl;


import java.io.PrintWriter;
import java.io.StringWriter;

import org.openmdx.kernel.text.StringBuilders;

/**
 * This the base class for all stack tracers and provides the generic
 * functionality
 */
abstract public class AbstractStackTrace
        implements LogLocation
{

    protected AbstractStackTrace()
    {
        clear();
    }


    /**
     * This returns the a specific line in the given throwable (stack
     * trace) that corresponds to level.  This raw line can then be
     * parsed to find the position of a caller in the running program.
     *
     * @param throwable a Throwable
     *
     * @param level a stack trace level we're interested in
     */
        abstract
    protected String currentStackLine(
                Throwable throwable,
                int level);


    /**
     * Sets the desired depth of in a stack trace that properly
     * indicates the callers level in the stack trace. This level
     * will be parsed and the parsed information will be available
     * through the class' getters.
     *
     * @param numLevelsUp the stack trace level we're interested in
     */
        abstract
    protected void setLevel(
        int numLevelsUp);

        /**
	 * Breaks the given line into a class name, line number, function,
	 * etc.  Presumably, the line corresponds to a line from a stack
	 * trace.
	 *
	 * @param raw a line from the stack trace
	 */
        abstract
        protected void parse(
                String raw);


    /**
     * Returns the trace information for a given exception if available.
     *
     * @param ex a java standard exception. No ServiceException allowed.
     * @return a String
     */
        static
    public String getTraceInfo(
        Throwable ex)
    {
        StringWriter sWriter = new StringWriter();

                ex.printStackTrace(
                        new PrintWriter(sWriter));

                return sWriter.toString();
    }


    /**
     * Resets the members
     */
    protected void clear()
    {
                setClassname("");
                setMethod("");
                setFilename("");
                setLineNumber(0);
    }



        /**
	 * Returns the full class name including the class' package.
	 *
	 * @return a String
     */
    public String getClassname()
    {
        return classname;
    }


        /**
	 * Returns the short class name without the class' package.
	 *
	 * @return a String
     */
    public String getShortClassname()
    {
                try {
                    return this.classname.substring(
                                                        this.classname.lastIndexOf('.') + 1,
                                                        this.classname.length());
                }
                catch (Exception e) {
                    return this.classname;
                }
    }


        /**
	 * Returns the method name.
	 *
	 * @return a String
     */
    public String getMethod()
    {
        return this.method;
    }


        /**
	 * Returns the file name.
	 *
	 * @return a String
     */
    public String getFilename()
    {
        return this.filename;
    }


        /**
	 * Returns the line number.
	 *
	 * @return a String
     */
    public int getLineNumber()
    {
        return this.lineNumber;
    }


        /**
	 * Returns the line number as string.
	 *
	 * @return   a String
     */
        public String getLineNumberString()
    {
                return (getLineNumber() >= 0) ? String.valueOf(getLineNumber()) : "";
    }


        /**
	 * Sets the classname
	 *
	 * @return classname a class name
	 */
        public void setClassname(String classname)
        {
                this.classname = classname;
        }


        /**
	 * Sets the method name.
	 *
	 * @return method a method name
	 */
        public void setMethod(String method)
        {
                this.method = method;
        }


        /**
	 * Sets the file name.
	 *
	 * @return filename  a file name
	 */
        public void setFilename(String filename)
        {
                this.filename = filename;
        }


        /**
	 * Sets the line number.
	 *
	 * @param lineNr  a line number
	 */
        public void setLineNumber(int lineNr)
        {
                this.lineNumber = lineNr;
        }


        /**
	 * Returns a StackTrace as a String.
	 *
	 * @return a String
     */
    public String toString()
    {
        return StringBuilders.newStringBuilder(
        ).append(
            "class="
        ).append(
            getClassname()
        ).append(
            " method="
        ).append(
            getMethod()
        ).append(
            " file="
        ).append(
            getFilename()
        ).append(
            " line="
        ).append(
            getLineNumberString()
        ).toString();
    }


    private String classname;
    private String method;
    private String filename;
    private int lineNumber;
}

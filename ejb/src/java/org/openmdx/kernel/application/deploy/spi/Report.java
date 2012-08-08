/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Report.java,v 1.3 2010/06/04 22:45:00 hburger Exp $
 * Description: Configuration result
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:45:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.deploy.spi;


import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.openmdx.kernel.exception.BasicException;

/**
 * This class holds the results from a configuration verification/validation.
 */
public class Report
{

    /**
     * Creates a new validation report object. It does not contain any reports
     * and the 'valid' flag is set to true.
     * 
     * @param name            The name of this configuration
     * @param context         The name of the context
     * @param majorVersion    The major version of the configuration [0,1,2,..]
     * @param minorVersion    The minor version of the configuration [0,1,2,..]
     */
    public Report(
        String name,
        VersionNumber version,
        String context
    ) {
        this.name = name;
        this.version = version;
        this.context = context;
    }


    /**
     * Returns validation result.
     *
     * @return  true unless addError has been called at least once.
     * 
     * @see org.openmdx.kernel.application.deploy.spi.Report#addError(java.lang.String)
     * @see org.openmdx.kernel.application.deploy.spi.Report#addError(java.lang.String,java.lang.Throwable)
     */
    public boolean isSuccess()
    {
        return this.success;
    }

    /**
     * Returns validation result.
     *
     * @return  false unless addWarning has been called at least once.
     * 
     * @see org.openmdx.kernel.application.deploy.spi.Report#addWarning(java.lang.String)
     * @see org.openmdx.kernel.application.deploy.spi.Report#addWarning(java.lang.String,java.lang.Throwable)
     */
    public boolean hasWarning()
    {
        return this.warning;
    }

    /**
     * Report an error. A ConfigurationValidationReport may have any number
     * of reports.
     * <p>
     * E.g.:
     * <ul>
     * <li> Entry 'port' is of invalid type. Type 'Integer' expected.
     * <li> Entry 'ports' cardinality violation. Valid range is [1..6].
     * <ul>
     */
    public void addError(String message)
    {
        this.reports.add("Error: " + message);
        this.success = false;
    }

    /**
     * Report an error including its cause.
     * 
     * @param report
     * @param cause
     */
    public void addError(
        String report,
                Throwable cause
    ) {
        addError(toEntry(report, cause));
    }

    /**
     * Report a warning.
     */
    public void addWarning(String message)
    {
        this.reports.add("Warning: " + message);
        this.warning = true;
    }

    /**
     * Fromat an exception entry
     * 
     * @param message
     * @param cause
     * 
     * @return the stringified exception entry
     */
    private static String toEntry(
        String message,
        Throwable cause
    ){
        CharArrayWriter exceptionStack = new CharArrayWriter();
        exceptionStack.append(message).append(": ");
        BasicException.toExceptionStack(
            cause
        ).printStackTrace(
            new PrintWriter(exceptionStack)
        );
        return exceptionStack.toString();
    }
        
    /**
     * Report a warning including its cause.
     * 
     * @param message
     * @param cause
     */
    public void addWarning(
        String message,
                Throwable cause
    ) {
        addWarning(toEntry(message, cause));
    }

    /**
     * Report an information.
     */
    public void addInfo(String message)
    {
        this.reports.add("Information: " + message);
    }

    /**
     * Include the contents of another report.
     * 
     * @param report
     */
    public void addAll(Report report)
    {
        this.reports.addAll(report.reports);
    }

    /**
     * Returns the configuration name.
     *
     * @return A configuration name string
     */
    public String getName()
    {
        return this.name;
    }


    /**
     * Returns the configuration context.
     *
     * @return A configuration context string
     */
    public String getContext()
    {
        return this.context;
    }


    /**
     * Returns the configuration's version.
     *
     * @return the configuration version
     */
    public VersionNumber getVersion()
    {
        return this.version;
    }

    /**
     * Returns a string representation of the report.
     *
     * @return  a stringified report
     */
    @Override
	public String toString()
    {
        StringBuilder buf = new StringBuilder(
            "Name   : ").append(this.name
        ).append(
            "\nContext: ").append(this.context
        ).append(
            "\nVersion: ").append(this.version
        ).append(
            "\nResult : ").append(this.success ? "succeeded" : "failed"
        ).append(
            '\n'
        );
        for(String report : reports) {
            buf.append('\n').append(report);
        }
        return buf.toString();
    }



    /** The configuration name */
    private final String name;

    /** The configuration context */
    private final String context;

    /** validation/verification state */
    protected boolean success = true;

    /** validation/verification state */
    protected boolean warning = false;

    /** verification reports */
    protected final List<String> reports = new ArrayList<String>();

    /** The configuration version */
    private final VersionNumber version;

}

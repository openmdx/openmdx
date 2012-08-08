/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ConfigurationValidationReport.java,v 1.10 2006/08/11 09:24:07 hburger Exp $
 * Description: Configuration result
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/08/11 09:24:07 $
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
package org.openmdx.base.application.configuration;


import java.util.Iterator;
import java.util.List;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.environment.cci.VersionNumber;

import org.openmdx.kernel.text.StringBuilders;

/**
 * This class holds the results from a configuration verification/validation.
 * <p>
 * @deprecated in favour of {@link 
 * org.openmdx.kernel.application.configuration.Report Report}.
 */
public class ConfigurationValidationReport extends Report
{

    /**
     * Creates a new validation report object. It does not contain any reports
     * and the 'valid' flag is set to true.
     *
     * @param name            The name of this configuration
     * @param majorVersion    The major version of the configuration [0,1,2,..]
     * @param minorVersion    The minor version of the configuration [0,1,2,..]
     */
    public ConfigurationValidationReport(
        String name,
        int majorVersion,
        int minorVersion
        ){
        super(
                name,
                        new VersionNumber(new int[]{majorVersion,minorVersion}),
                        null
            );
    }

    /**
     * Sets the configuration result to succeeded or failed.
     *
     * @param succeeded  The configuration result
     */
    public void setSucceeded(boolean succeeded)
    {
        super.success = succeeded;
    }

    /**
     * Add an error report. A ConfigurationValidationReport may have any number
     * of reports.
     *
     * <p>
     * E.g.:
     * <ul>
     * <li> Entry 'port' is of invalid type. Type 'Integer' expected.
     * <li> Entry 'ports' cardinality violation. Valid range is [1..6].
     * <ul>
     */
    public void addErrorReport(String report)
    {
        super.reports.add("Error: " + report);
    }

    /**
     * Add an info report. A ConfigurationValidationReport may have any number
     * of reports.
     */
    public void addInfoReport(String report)
    {
        super.reports.add("Context: " + report);
    }

        /* (non-Javadoc)
	 * @see org.openmdx.kernel.application.configuration.Report#addWarningReport(java.lang.String)
	 */
        public void addWarningReport(String report) {
                super.addWarning(report);
        }

        /* (non-Javadoc)
	 * @see org.openmdx.kernel.application.configuration.Report#addReport(java.util.List)
	 */
        public void addReport(List reports) {
                super.reports.addAll(reports);
        }

        /* (non-Javadoc)
	 * @see org.openmdx.kernel.application.configuration.Report#getReports()
	 */
        public List getReports() {
                return super.reports;
        }

        /**
     * Returns the configuration's major version.
     *
     * @return A configuration major version
     */
    public int getMajorVersion()
    {
        return super.getVersion().get(0);
    }

    /**
     * Returns the configuration's minor version.
     *
     * @return A configuration minor version
     */
    public int getMinorVersion()
    {
        return super.getVersion().get(1);
    }

    /**
     * Returns a string representation of the report.
     *
     * @return  a stringified report
     */
    public String toString()
    {
        CharSequence buf = StringBuilders.newStringBuilder().
            append("Name   : ").append(getName()).
            append("\nVersion: ").append(getVersion()).
            append("\nResult : ").append(isSuccess()?"succeeded":"failed").append('\n');

        Iterator iter = reports.iterator();
        while(iter.hasNext()) {
            StringBuilders.asStringBuilder(buf).append('\n').append(iter.next());
        }

        return buf.toString();
    }

}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Platform_1_0.java,v 1.3 2004/04/02 16:59:03 wfro Exp $
 * Description: openMDX Platform Description Interface 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:03 $
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
package org.openmdx.kernel.environment.cci;


/**
 *
 */
public interface Platform_1_0 {

    /**
     * Return the name of the platform not including its version.
     *
     * @return The platform's name, e.g. "jboss".
     */
    public String getName();
    
    /**
     * Return the title of the specification that this application server implements.
     * @return the specification title, null is returned if it is not known.
     */
    String getSpecificationTitle();

    /**
     * Returns the version number of the specification
     * that this application server implements.
     * This version string must be a sequence of positive decimal
     * integers separated by "."'s and may have leading zeros.
     * When version strings are compared the most significant
     * numbers are compared.
     * @return the specification version, null is returned if it is not known.
     */
    String getSpecificationVersion();

    /**
     * Return the name of the organization, vendor,
     * or company that owns and maintains the specification
     * of the classes that implement this application server.
     * @return the specification vendor, null is returned if it is not known.
     */
    String getSpecificationVendor();

    /**
     * Return the title of this application server.
     * @return the title of the implementation, null is returned if it is not known.
     */
    String getImplementationTitle();

    /**
     * Return the version of this implementation. It consists of any string
     * assigned by the vendor of this implementation and does
     * not have any particular syntax specified or expected by the Java
     * runtime. It may be compared for equality with other
     * application server version strings used for this implementation
     * by this vendor for this application server.
     * @return the version of the implementation, null is returned if it is not known.
     */
    String getImplementationVersion();

    /**
     * Returns the name of the organization,
     * vendor or company that provided this implementation.
     * @return the vendor that implemented this application server.
     */
    String getImplementationVendor();

    /**
     * Get the implementation mapping.
     * <p>
     * <code>getImplementationMapping(
     *   new VersionNumber("1.3"),
     *   2,
     *   new VersionNumber("1.4")
     * )</code> for example returns the following values depending in the 
     * Java Runtime Environment<ul>
     * <li>1.1.4 -> jre.before1_3
     * <li>1.2.2 -> jre.before1_3
     * <li>1.3.1 -> jre.only1_3
     * <li>1.4.1 -> jre.since1_4
     * </ul>
     * 
     * @param before
     *        Versions up to but not including <code>before</code> are mapped 
     *        to <code>family + ".before" + before</code> unless 
     *        <code>before</code> is <code>null</code>
     * @param exact
     *        Version from <code>before</code> to but not including 
     *        <code>since</code> are mapped to
     *        <code>family + ".only" + partialVersion</code> where partialVersion
     *        includes the number of components specified by the argument exact.
     * @param since
     *        Versions from <code>since</code> on are mapped to
     *        <code>family + ".since" + since</code> unless 
     *        <code>since</code> is <code>null</code>
     * 
     * @return the implementation mappings 
     */
    String getMapping(
        VersionNumber before,
        int exactComponents,
        VersionNumber since
    );

}

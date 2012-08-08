/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Dependencies.java,v 1.7 2009/02/19 17:44:36 hburger Exp $
 * Description: Compliance
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/19 17:44:36 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application;

import javax.management.RuntimeErrorException;

import org.openmdx.kernel.environment.cci.VersionNumber;

/**
 * Used to check the dependencies
 */
public class Dependencies {

    /**
     * Avoid instantiation 
     */
    private Dependencies(
    ){
        super();
    }

    /**
     * Check the version of required components
     * 
     * @exception   VersionMismatchException
     *              if a components version does not match
     */
    public static void checkDependencies (
    ){
        if(Dependencies.actualVersion == null){
            org.openmdx.base.Dependencies.checkCompliance(Version.getSpecificationVersion());
            //... Add additional dependency checks if required
            Dependencies.actualVersion = new VersionNumber(Version.getSpecificationVersion());
        }
    }

    /**
     * Check the version of this and required components
     * 
     * @exception   VersionMismatchException
     *              if a components version does not match
     * @exception   IllegalArgumentException
     *              if the version string can't be parsed.
     */
    public static void checkCompliance (
        String expectedVersion
    ){
        checkDependencies();
        if(
                ! actualVersion.isCompliantWith(new VersionNumber(expectedVersion))
        ) {
            throw new RuntimeErrorException(
                new AssertionError(
                    Version.class.getName() + ' ' + Version.getImplementationVersion() + " is not compliant with " + expectedVersion
                ),
                "Library version mismatch"
            );
        }
    }

    /**
     * The version number is set as soon as the dependencies are checked.
     */
    private static VersionNumber actualVersion = null;

}

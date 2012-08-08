/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Configuration.java,v 1.1 2004/06/17 13:02:46 hburger Exp $
 * Description: Configuration
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/06/17 13:02:46 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.configuration;


/**
 * Configuration
 */
public interface Configuration {
    
    /**
     * Verifies the configuration. All configuration entries are verified with
     * the configuration entry descriptions. A verification requires that the
     * configuration entry descriptions have been set.
     *
     * <p> The verification process verifies that:
     * <ul>
     * <li>each entry has an entry description
     * <li>multi-valued entries do not have value holes
     * <li>the value cardinality is valid
     * <li>the value types are valid
     * <li>each entry from entry description list is present
     * </ul>
     *
     * @return  A validation report
     */
    Report verify(
    );

    /**
     * Validates the configuration.
     *
     * A concrete configuration validates the configuration entries. Validation
     * goes beyond simple verification:
     * <ul>
     * <li>Detects missing entries and adds its defaults
     * <li>Rejects unsupported entries
     * <li>Checks the version and possibly upgrades the configuration
     *     to the most current version
     * </ul>
     * To accomplish this the validator needs more information than is required
     * for just a simple verification.
     *
     * @return  A validation report
     * 
     * @see #verify()
     */
    Report validate();

}

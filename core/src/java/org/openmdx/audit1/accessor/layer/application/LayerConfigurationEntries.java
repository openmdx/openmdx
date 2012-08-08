/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: LayerConfigurationEntries.java,v 1.2 2007/08/01 16:45:09 wfro Exp $
 * Description: LayerConfigurationEntries 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/01 16:45:09 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.openmdx.audit1.accessor.layer.application;

/**
 * LayerConfigurationEntries
 *
 */
public class LayerConfigurationEntries {

    /**
     * The audit plugin requires the configuration of two delegate 
     * dataproviders:
     * <ul>
     *   <li>dataprovider[0] must refer to a dataprovider which is responsible
     *       to store the before images. dataprovider[0] must implement the 
     *       same interface as the configured provider.
     *   <li>dataprovider[1] must refer to a dataprovider implementing the
     *       audit1 interface. 
     */
    public static final String DATAPROVIDER = "dataprovider";
    
    /**
     * The identity of objects to be audited, i.e. before images, is mapped 
     * according to the audit mapping. 
     */
    public static final String AUDIT_MAPPING = "auditMapping";
    
    /**
     * An object retrieval of a involved object returns by the default the
     * requested object or throws a NOT_FOUND exception. When the option is 
     * set to true all objects involved in a unit of work are returned in 
     * the same fetch set.
     */
    public static final String RETURN_ALL_INVOLVED = "returnAllInvolved";
    
}

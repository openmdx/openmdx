/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ShareableConnectionManager.java,v 1.1 2009/01/12 12:49:23 wfro Exp $
 * Description: Shareable Connection Manager
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.container.lightweight;

import java.util.HashSet;
import java.util.Set;

import javax.resource.ResourceException;

/**
 * Shareable Connection Manager
 */
@SuppressWarnings("unchecked")
public class ShareableConnectionManager extends AbstractConnectionManager {

    /**
     * Constructor
     * 
     * @param credentials
     */
    public ShareableConnectionManager(
        Set credentials
    ) {
        super(credentials);
    }

    /**
     * Constructor
     * 
     * @param credentials
     * @param connectionClass
     */
    public ShareableConnectionManager(
        Set credentials, 
        Class connectionClass
    ) {
        super(credentials, connectionClass);
    }

    /**
     * Constructor
     * 
     * @param credentials
     * @param connectionClass
     * 
     * @throws ResourceException
     */
    public ShareableConnectionManager(
        Set credentials, 
        String connectionClass
    ) throws ResourceException {
        super(credentials, connectionClass);
    }

    /**
     * Implements <code>Serializable</code>.
     */
    private static final long serialVersionUID = 2446478778635712446L;

    /**
     * The managed connections to be shared
     */
    private final Set sharedConnections = new HashSet();

    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.lightweight.AbstractConnectionManager#getManagedConnections()
     */
    protected Set getManagedConnections() {
        return this.sharedConnections;
    }

}

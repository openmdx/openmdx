/*
 * ====================================================================
 * Project:     OMEX/Security, http://www.omex.ch/
 * Name:        $Id: WebLogicUser.java,v 1.2 2005/07/11 12:41:57 hburger Exp $
 * Description: WebLogic Server User
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/11 12:41:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
package org.openmdx.weblogic.security.realm;

import weblogic.security.spi.WLSUser;


/**
 * WebLogic Server User
 * <p>
 * The WebLogicGroup is a class that implements a WLSUser principal whose name 
 * field will be signed by the PrincipalValidatorImpl class. To use this class, 
 * you should make the PrincipalValidatorImpl class the runtime class for your 
 * Principal Validation provider, or extend that class and make the extended 
 * class your Principal Validation provider.

 */
public class WebLogicUser 
	extends WebLogicPrincipal 
	implements WLSUser 
{

    /**
     * <code>serialVersionUID</code> to implement <code>Serializable</code>.
     */
    private static final long serialVersionUID = 3545234713393247284L;

    /**
     * Implements Serializable
     */
    protected WebLogicUser(){
        super();
    }

    /**
     * Constructor
     * 
     * @param xri
     *        the user's XRI
     * @param name
     *        the user's name
     */
    public WebLogicUser(
        String xri, 
        String name
    ) {
        super(xri, name);
    }
    
}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: GenericPrincipals.java,v 1.3 2007/10/10 16:06:08 hburger Exp $
 * Description: Principal Types
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:08 $
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

package org.openmdx.kernel.security.authentication.spi;

import java.security.Principal;

/**
 * The main principal types used in openMDX/Security tokens
 */
public class GenericPrincipals {

    private GenericPrincipals() {
        // Avoid instantiation
    }

    /**
     * The principal constants' prefix
     */
    protected static final String PREFIX = "openMDX."; // "org.openmdx.security.authentication.";

    /**
     * Denotes an authenticated user
     */
    public static final String USER = PREFIX + "User";
    
    /**
     * Test whether a given principal is a generic <code>USER</code> principal.
     * 
     * @return true if the given principal is a generic <code>USER</code> principal.
     */
    public static boolean isGenericUser(
        Principal principal
    ){
        return 
            principal instanceof GenericPrincipal &&
            USER.equals(((GenericPrincipal)principal).getType());            
    }

    /**
     * Denotes a group an authenticated user belongs to
     */
    public static final String GROUP = PREFIX + "Group";

    /**
     * Test whether a given principal is a generic <code>USER</code> principal.
     * 
     * @return true if the given principal is a generic <code>USER</code> principal.
     */
    public static boolean isGenericGroup(
        Principal principal
    ){
        return 
            principal instanceof GenericPrincipal &&
            GROUP.equals(((GenericPrincipal)principal).getType());            
    }

    /**
     * The token containing an array of generic principals
     */
    public static final String TOKEN = PREFIX + "Principals";

}

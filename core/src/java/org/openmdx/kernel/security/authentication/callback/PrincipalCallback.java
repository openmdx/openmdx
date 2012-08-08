/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org
 * Name:        $Id: PrincipalCallback.java,v 1.1 2005/07/09 19:45:26 hburger Exp $
 * Description: Principal Callback
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/09 19:45:26 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.kernel.security.authentication.callback;

import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.callback.Callback;

import org.openmdx.kernel.security.authentication.spi.GenericPrincipal;


/**
 * Principal Callback
 */
public class PrincipalCallback 
	implements Callback, Serializable 
{

    /**
     * Construct an <code>PrincipalCallback</code> with a prompt. 
     * 
     * @param prompt the prompt used to request the principals
     */
    public PrincipalCallback(
        String prompt
    ){
        this.prompt = prompt;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3618985568139163190L;

    /**
     * @serial
     */
    private final String prompt;
    
    /**
     * @serial
     */
    private Principal[] principals;
    
    /**
     * Get the prompt.
     * 
     * @return the prompt.
     */
    public String getPrompt(
    ){
        return this.prompt;        
    }

    /**
     * Retrieve the principals.
     * 
     * @return the <code>principals</code>'s value
     */
    public Set getPrincipals() {
        return new HashSet(Arrays.asList(this.principals));
    }

    /**
     * Retrieve the <code>Principal</code>s<ul>
     * <li>whose class name equals the given type
     * <li>which are an instance of <code>GenericPrincipal</code> with the given type
     * </ul>
     * 
     * @param the <code>Principal</code>s
     * 
     * @return the corresponding <code>Set</code> of <code>Principal</code>s
     */
    public Set getPrincipals(
        String type
    ) {
        Set reply = new HashSet();
        for(
            int i = 0;
            i < this.principals.length;
            i++
        ) if (
            type.equals(this.principals[i].getClass().getName()) ||
            this.principals[i] instanceof GenericPrincipal &&
            type.equals(((GenericPrincipal)this.principals[i]).getType())
        ) reply.add(
            this.principals[i]
        );
        return reply;
    }
    
    /**
     * Retrieve the principals.
     * 
     * @return the <code>principals</code>'s value
     */
    public Set getPrincipals(
        Class type
    ) {
        Set reply = new HashSet();
        for(
            int i = 0;
            i < this.principals.length;
            i++
        ) if (
            type.isInstance(this.principals[i])
        ) reply.add(
            this.principals[i]
        );
        return reply;
    }

    /**
     * Set the principals
     *
     * @param principals The <code>principals</code>'s value
     */
    public void setPrincipals(Principal[] principals) {
        this.principals = principals;
    }
    
}

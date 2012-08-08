/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org
 * Name:        $Id: AddressCallback.java,v 1.1 2010/03/05 13:26:09 hburger Exp $
 * Description: Address Callback
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/05 13:26:09 $
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
package org.openmdx.security.auth.callback;

import java.io.Serializable;
import java.net.InetAddress;

import javax.security.auth.callback.Callback;


/**
 * Address Callback
 */
public class AddressCallback 
	implements Callback, Serializable {

	/**
     * Construct an AddressCallback with a prompt. 
     * 
     * @param prompt the prompt used to request the name
     */
    public AddressCallback(
        String prompt
    ){
        this.prompt = prompt;
    }

    /**
     * <code>serialVersionUID</code> to implement <code>Serializable</code>
     */
	private static final long serialVersionUID = 3257569486201894963L;

    /**
     * @serial
     */
    private final String prompt;
    
    /**
     * @serial
     */
    private InetAddress address;
    
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
     * @return Returns the address.
     */
    public InetAddress getAddress() {
        return this.address;
    }
    /**
     * @param address The address to set.
     */
    public void setAddress(
        InetAddress address
    ) {
        this.address = address;
    }
    
}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: BasicAuthentication.java,v 1.1 2010/10/28 15:24:05 hburger Exp $
 * Description: Basic Authentication 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/10/28 15:24:05 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.application.rest.http;

import java.io.UnsupportedEncodingException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;


/**
 * Basic Authentication according to RFC 2617
 */
public class BasicAuthentication {

    /**
     * Constructor 
     *
     * @param userName
     * @param password
     */
    public BasicAuthentication(
        String userName,
        String password
    ){
        try {
            this.authorization = "Basic " + Base64.encode(
                (userName + ":" + (password == null ? "" : password)).getBytes(CHARACTER_ENCODING)
            );
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unable to encode the BASIC authorization string",
                new BasicException.Parameter("character-encoding", CHARACTER_ENCODING)
            );
        }
    }

    /**
     * Constructor 
     *
     * @param userName
     * @param password
     */
    public BasicAuthentication(
        String userName,
        char[] password
    ){
        this(userName, password == null ? null : new String(password));
    }

    /**
     * Use UTF-8 encoding
     */
    private final static String CHARACTER_ENCODING = "UTF-8";
    
    /**
     * 
     */
    private final String authorization;

    /**
     * Retrieve the authorization field
     * 
     * @return the authorization field
     */
    public String getAuthorization(){
        return this.authorization;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Authorization: " + this.authorization;
    }
    
}

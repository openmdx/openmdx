/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: StandardCallbackPrompts.java,v 1.1 2007/11/26 14:04:34 hburger Exp $
 * Description: Standard Callback Prompts
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/26 14:04:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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


/**
 * Standard Callback Constants
 * <p>
 * It is recommended to use these definitions in order to simplify
 * localization and callback identification.
 */
public class StandardCallbackPrompts {

    /**
     * Avoid instantiation
     */
    protected StandardCallbackPrompts(
    ){
    }


    //------------------------------------------------------------------------
    // Username
    //------------------------------------------------------------------------

    /**
     * The username callback prompt
     */
    public final static String USERNAME = "username";

    
    //------------------------------------------------------------------------
    // Password
    //------------------------------------------------------------------------

    /**
     * The password callback prompt
     */
    public final static String PASSWORD = "password";

    
    //------------------------------------------------------------------------
    // Passcode
    //------------------------------------------------------------------------

    /**
     * The passcode callback prompt
     */
    public final static String PASSCODE = "passcode";

	/**
	 * The passcode's PIN part
	 */
	public final static String PIN = "PIN";
	
	/**
	 * The passcode's tokecode part
	 */
	public final static String TOKENCODE = "tokencode";

	
    //------------------------------------------------------------------------
    // Address
    //------------------------------------------------------------------------

    /**
     * The address callback prompt
     */
    public final static String CLIENT = "client";


    //------------------------------------------------------------------------
    // Context
    //------------------------------------------------------------------------

    /**
     * Callback prompt for the context.
     */
    public final static String CONTEXT = "context";
   
}

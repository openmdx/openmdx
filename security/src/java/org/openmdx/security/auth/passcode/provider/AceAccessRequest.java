/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: AceAccessRequest.java,v 1.2 2007/11/26 16:22:16 hburger Exp $
 * Description: ACE Access Request
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/26 16:22:16 $
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
package org.openmdx.security.auth.passcode.provider;

import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.packets.PapAccessRequest;


/**
 * ACE Access Request
 */
public class AceAccessRequest extends PapAccessRequest {

    /**
     * Async Port Type
     */
    protected static final byte[] ASYNC = new byte[]{0,0,0,0};

    /**
     * Constructor
     * 
     * @param userName
     * @param passcode
     * @param context
     * @param tokenCodeLength
     * 
     * @throws InvalidParameterException
     */
    AceAccessRequest(
        String userName, 
        String passcode,
        String context,
        int tokenCodeLength
    ) throws InvalidParameterException {
    	this(
            userName, 
            getPassword(passcode, SecurIDState.getTag(context), tokenCodeLength),
            context
        );        
    }

    /**
     * Constructor
     * 
     * @param userName
     * @param pin
     * @param tokenCode
     * @param context
     * 
     * @throws InvalidParameterException
     */
    AceAccessRequest(
        String userName, 
        char[] pin,
        String tokenCode,
        String context
    ) throws InvalidParameterException {
    	this(
            userName, 
            getPassword(pin, tokenCode, SecurIDState.getTag(context)),
            context
        );        
    }
    
    /**
     * Constructor
     * 
     * @param userName
     * @param password
     * @param context
     * 
     * @throws InvalidParameterException
     */
    private AceAccessRequest(
        String userName, 
        String password,
        String context
    ) throws InvalidParameterException {
        super(
            userName, 
            password
        );        
		if(context != null){
		    setAttribute(
				new RadiusAttribute(
					RadiusAttributeValues.STATE,
					SecurIDState.getState(context)
				)
			);
		    setSocketIndex(
		        SecurIDState.getProvider(context)
		    );
		}
        setAttribute(
            new RadiusAttribute(
                RadiusAttributeValues.NAS_PORT_TYPE, 
                ASYNC
            )
        );
    }
    
    /**
     * Return the &quot;password&quot; to be sent to the ACE Radius Server
     * 
     * @param passcode
     * @param state
     * @param tokenCodeLength
     * 
     * @return the pass code, the PIN or the token code
     */
    private static final String getPassword(
        String passcode,
        short state,
        int tokenCodeLength
    ){
        if(passcode == null) return EMPTYSTRING;
        try {
	        switch(state) {
	            case SecurIDState.SECURID_NEXT:
	                return passcode.substring(passcode.length() - tokenCodeLength);
	            case SecurIDState.SECURID_NPIN:
	                return passcode.substring(0, passcode.length() - tokenCodeLength);
		        default: 
		            return passcode;
	        }
        } catch (IndexOutOfBoundsException exception) {
            return EMPTYSTRING;
        }
    }

    /**
     * Return the &quot;password&quot; to be sent to the ACE Radius Server
     * 
     * @param pin
     * @param tokenCode
     * @param state
     * 
     * @return the pass code, the PIN or the token code
     */
    private static final String getPassword(
        char[] pin,
        String tokenCode,
        short state
    ){
    	String tokenCodeValue = tokenCode == null ? EMPTYSTRING : tokenCode;
    	String pinValue = pin == null ? EMPTYSTRING : String.valueOf(pin);
        switch(state) {
	        case SecurIDState.SECURID_NEXT:
	            return tokenCodeValue;
	        case SecurIDState.SECURID_NPIN:
	            return pinValue;
	        default: 
	            return pinValue + tokenCodeValue;
	    }
    }

}

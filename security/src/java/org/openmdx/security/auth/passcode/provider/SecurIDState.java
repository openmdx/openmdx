/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: SecurIDState.java,v 1.3 2009/03/08 18:52:18 wfro Exp $
 * Description: SecurID State
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:18 $
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

import java.io.UnsupportedEncodingException;
import java.security.ProviderException;
import java.util.Iterator;

import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttribute;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusClient;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;

/**
 * SecurID State 
 */
final class SecurIDState {

    /**
     * Avod instantiation
     */
    private SecurIDState() {
    }

    /**
     * Unknown SecurID State
     */
    final static short UNKNOWN = -1;
    
    /**
     * No SecurID State
     */
    final static short NULL = 0;

    /**
     * Wait for token to change, then enter the new tokencode. 
     */
    final static short SECURID_NEXT = 1;
    
    /**
     * Enter a new PIN.
     */
    final static short SECURID_NPIN = 2;
    
    /**
     * Wait for the token code to change, then enter the new passcode.
     */
    final static short SECURID_WAIT = 3;
    
    /**
     * Parse a SecurID reply's state
     *  
     * @param context
     * 
     * @return the embedded tag
     */
    final static short getTag(
        String context
    ){
    	return 
    		context == null ? SecurIDState.NULL :
    		context.startsWith("SECURID_NEXT|") ? SecurIDState.SECURID_NEXT :
			context.startsWith("SECURID_NPIN|") ? SecurIDState.SECURID_NPIN :
			context.startsWith("SECURID_WAIT|") ? SecurIDState.SECURID_WAIT :
				SecurIDState.UNKNOWN;
    }

    /**
     * Convert a context to a state
     * 
     * @param context
     * 
     * @return the corresponding state
     */
    final static byte[] getState(
        String context
    ){
        try {
            return context == null ? 
                null :
                context.substring(0, context.length() - 1).getBytes(RadiusClient.ENCODING);
        } 
        catch (UnsupportedEncodingException exception) {
            throw new ProviderException(
                "The RadiusClient's \"" + RadiusClient.ENCODING + "\" encoding is not supported",
                exception
            );
        }
    }
    
    /**
     * Convert a context to a state
     * 
     * @param context
     * 
     * @return the corresponding state
     */
    final static int getProvider(
        String context
    ){
        return context == null ? 
            -1 :
            Character.digit(context.charAt(context.length() - 1), Character.MAX_RADIX);
    }

    /**
     * Convert a state to a context
     * 
     * @param state the state
     * 
     * @return the corresponding context
     */
    final static String getContext(
        RadiusPacket reply
    ){
        byte[] state = SecurIDState.getState(reply);
        try {
            return state == null ? 
                null : 
                new String(state, RadiusClient.ENCODING) + Character.forDigit(reply.getSocketIndex(), Character.MAX_RADIX);
        } 
        catch (UnsupportedEncodingException exception) {
            throw new ProviderException(
                "The RadiusClient's \"" + RadiusClient.ENCODING + "\" encoding is not supported",
                exception
            );
        }
    }
    
	/**
    *
    */
	@SuppressWarnings("unchecked")
	protected static byte[] getState(
		RadiusPacket packet
	){
		for(
			Iterator i = packet.getAttributes().iterator();
			i.hasNext();
		) {
			RadiusAttribute attribute = (RadiusAttribute) i.next();
			if(
				attribute.getType() == RadiusAttributeValues.STATE
			) return attribute.getValue();
		}
		return null;
	}

}

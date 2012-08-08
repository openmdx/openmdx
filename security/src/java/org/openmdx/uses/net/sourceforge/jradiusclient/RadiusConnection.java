/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Name:        $Id: RadiusConnection.java,v 1.2 2010/08/03 14:27:23 hburger Exp $
 * Description: RADIUS Connection 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/03 14:27:23 $
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
package org.openmdx.uses.net.sourceforge.jradiusclient;

import java.io.Closeable;

import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;

/**
 * RADIUS Connection 
 */
public interface RadiusConnection extends Closeable {

	/**
	 * This method performs the job of sending accounting information for the
	 * current user to the radius accounting server.
	 * 
	 * @param requestPacket Any  request attributes to add to the accounting packet.
	 * 
	 * @return RadiusPacket a packet containing the response from the Radius server
	 * 
	 * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException
	 * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException
	 */
	RadiusPacket account(
		RadiusPacket requestPacket
	) throws InvalidParameterException, RadiusException;

    /**
     * This method performs the job of authenticating the given <code>RadiusPacket</code> against
     * the radius server.
     *
     * @param RadiusPacket containing all of the <code>RadiusAttributes</code> for this request. This
     * <code>RadiusPacket</code> must include the USER_NAME attribute and be of type ACCEES_REQUEST.
     * If the USER_PASSWORD attribute is set it must contain the plaintext bytes, we will encode the
     * plaintext to send to the server with a REVERSIBLE algorithm. We will set the NAS_IDENTIFIER
     * Attribute, so even if it is set in the RadiusPacket we will overwrite it
     * @param int retries must be zero or greater, if it is zero default value of 3 will be used
     *
     * @return RadiusPacket containing the response attributes for this request
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException
     * @exception org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException
     */
    public RadiusPacket authenticate(
    	RadiusPacket accessRequest
    ) throws RadiusException, InvalidParameterException;

}

/*
 * ====================================================================
 * Project:     openMDX/Tomcat, http://www.openmdx.org/
 * Description: ExtendedJDBCRealm
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package org.openmdx.catalina.realm;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.realm.DataSourceRealm;
import org.apache.catalina.util.Base64;

public class ExtendedDataSourceRealm extends DataSourceRealm {

    //-----------------------------------------------------------------------
    /**
     * Returns the digest format.
     *
     * @return The format
     */
    public String getDigestFormat() {
        return this.digestFormat;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the digest format. Supported formats are <code>hex</code>, <code>base64</code>.
     *
     * @param digest format.
     */
    public void setDigestFormat(String format) {
        this.digestFormat = format;
    }

    //-----------------------------------------------------------------------
    /**
     * Digest the password using the specified algorithm and
     * convert the result to a corresponding formatted string.
     * If exception, the plain credentials string is returned.
     *
     * @param credentials Password or other credentials to use in
     *        authenticating this username
     */
    @Override
	protected String digest(
        String credentials
    )  {
        super.getContainer().getRealm();
        
        String digest = super.digest(credentials);
        if("hex".equalsIgnoreCase(this.digestFormat)) {
            return digest;
        }
        else if("base64".equalsIgnoreCase(this.digestFormat)) {
            String formattedDigest = Base64.encode(HexUtils.convert(digest));
            containerLog.debug("formatted digest " + formattedDigest);
            return formattedDigest;
        }
        else {
            containerLog.error("Illegal digestFormat: " + getDigestFormat());
            throw new IllegalArgumentException("Illegal digestFormat: " + getDigestFormat());
        }

    }

    protected String normalizeUserName(
    	String username
    ) {
    	if(username == null) return null;
    	// Domain
    	if(username.indexOf("@") >= 0) {
    		username = username.substring(0, username.indexOf("@"));
    	}
    	if(username.indexOf("\\") >= 0) {
    		username = username.substring(username.indexOf("\\") + 1);
    	}
    	// Principal chain
    	if(
	        (username.startsWith("[") && username.endsWith("]")) ||
	        (username.startsWith("{") && username.endsWith("}"))
	    ) {
            List<String> principalChain = new ArrayList<String>();
            for(String principal: username.substring(1, username.length() - 1).split(",")) {
                principal = principal.trim();
                if(!principal.isEmpty()) {
                    principalChain.add(principal);
                }
            }    		
	        username = principalChain.isEmpty() ? username : principalChain.get(0);
    	}
        return username;
    }

    // -----------------------------------------------------------------------
    @Override
    protected String getPassword(
    	Connection dbConnection,
    	String username
    ) {
    	return super.getPassword(
    		dbConnection,
    		this.normalizeUserName(username)
    	);
    }

    // -----------------------------------------------------------------------
    @Override
    protected ArrayList<String> getRoles(
    	Connection dbConnection,
    	String username
    ) {
    	return super.getRoles(
    		dbConnection,
    		this.normalizeUserName(username)
    	);
    }

	//-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    /**
     * The format for the digest (hex, base64)
     */
    protected String digestFormat = "hex";

}

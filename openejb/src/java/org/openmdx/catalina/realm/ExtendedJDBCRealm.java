package org.openmdx.catalina.realm;
/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ExtendedJDBCRealm.java,v 1.2 2009/11/13 17:05:34 wfro Exp $
 * Description: Extended Context Bindings
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/13 17:05:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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


import java.security.Principal;

import org.apache.catalina.Realm;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.catalina.util.Base64;
import org.apache.catalina.util.HexUtils;

public class ExtendedJDBCRealm extends JDBCRealm {

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
     * Returns the flag.
     *
     * @return The flag
     */
    public String getFlag() {
        return this.flag;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the flag. Supported values are <code>sufficient</code>, <code>required</code>.
     *
     * @param flag flag.
     */
    public void setFlag(String flag) {
        this.flag = flag;
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
    protected String digest(
        String credentials
    )  {
        super.getContainer().getRealm();
        
        String digest = super.digest(credentials);
        if("hex".equalsIgnoreCase(this.digestFormat)) {
            return digest;
        }
        else if("base64".equalsIgnoreCase(this.digestFormat)) {
            try {
                String formattedDigest = new String(Base64.encode(HexUtils.convert(digest)), "US-ASCII");
                containerLog.debug("formatted digest " + formattedDigest);
                return formattedDigest;
            }
            catch(Exception e) {
                String formattedDigest = new String(Base64.encode(HexUtils.convert(digest)));
                containerLog.debug("formatted digest " + formattedDigest);
                return formattedDigest;
            }
        }
        else {
            containerLog.error("Illegal digestFormat: " + getDigestFormat());
            throw new IllegalArgumentException("Illegal digestFormat: " + getDigestFormat());
        }

    }

    //-----------------------------------------------------------------------
    @Override
    public synchronized Principal authenticate(
        String username,
        String credentials
    ) {
        Principal principal = super.authenticate(username, credentials);
        if(
            "required".equals(this.flag) ||
            (principal != null)
        ) {
            System.out.println("ExtendedJDBCRealm: authentication OK for " + principal);
            return principal;
        }
        else if("sufficient".equals(this.flag)) {
            Realm realm = this.getContainer().getParent() == null
                ? this.getContainer().getRealm()
                : this.getContainer().getParent().getRealm();
            if(this == realm) {
                System.out.println("ExtendedJDBCRealm: already at parent level. authentication FAILED");
                return null;
            }
            else {
                return realm.authenticate(
                    username, 
                    credentials
                );
            }
        }
        else {
            containerLog.error("Illegal flag: " + getFlag());
            throw new IllegalArgumentException("Illegal flag: " + getFlag());            
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    /**
     * The format for the digest (hex, base64)
     */
    protected String digestFormat = "hex";
    /**
     * flag which allows to set whether an authentication with this 
     * realm is sufficient or required.
     */
    protected String flag = "sufficient";
    
}

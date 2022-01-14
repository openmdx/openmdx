/*
 * ====================================================================
 * Project:     openMDX/Tomcat, http://www.openmdx.org/
 * Description: ExtendedDatasourceCRealm
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

/**
 * ExtendedDataSourceRealm
 *
 */
public class ExtendedDataSourceRealm extends DataSourceRealm {

    /**
     * Normalize user name. Handle @, [] and {}
     * 
     * @param username
     * @return
     */
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

    /* (non-Javadoc)
     * @see org.apache.catalina.realm.DataSourceRealm#getPassword(java.sql.Connection, java.lang.String)
     */
    @Override
    protected String getPassword(
    	Connection dbConnection,
    	String username
    ) {
    	return "{MD5}" + super.getPassword(
    		dbConnection,
    		this.normalizeUserName(username)
    	);
    }

    /* (non-Javadoc)
     * @see org.apache.catalina.realm.DataSourceRealm#getRoles(java.sql.Connection, java.lang.String)
     */
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

}

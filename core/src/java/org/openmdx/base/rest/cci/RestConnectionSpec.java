/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RestConnectionSpec.java,v 1.5 2010/10/24 21:46:37 hburger Exp $
 * Description: REST Connection Specification
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/10/24 21:46:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2010, OMEX AG, Switzerland
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
package org.openmdx.base.rest.cci;

import java.io.Serializable;

import javax.resource.cci.ConnectionSpec;

/**
 * REST Connection Specification
 */
public class RestConnectionSpec implements ConnectionSpec, Serializable {

    /**
     * Constructor 
     */
    public RestConnectionSpec(
    ){
    	this(null, null, null);
    }
    
    /**
     * Constructor 
     *
     * @param user
     * @param password
     */
    public RestConnectionSpec(
        String user,
        String password
    ){
    	this(user, password, null);
    }

    /**
     * Constructor 
     *
     * @param user
     * @param password
     */
    public RestConnectionSpec(
        String user,
        String password,
        Object tenant
    ){
        this.userName = user;
        this.password = password;
        this.tenant = tenant;
    }
    
    /**
     * @serial JCA's standard property 'UserName'
     */
    private String userName;
    
    /**
     * @serial JCA's standard property 'Password'
     */
    private String password;

    /**
     * @serial The 'Tenant' property
     */
    private Object tenant;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -7736215086099158839L;

    /**
     * Set userName.
     * 
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Retrieve user.
     *
     * @return Returns the user.
     */
    public String getUserName() {
        return this.userName;
    }
    
    /**
     * Set password.
     * 
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieve password.
     *
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }

    
    /**
     * Retrieve the tenant
     * 
     * @return the tenant
     */
    public Object getTenant() {
		return tenant;
	}

    /**
     * Set the tenant
     * 
     * @param tenant
     */
	public void setTenant(Object tenant) {
		this.tenant = tenant;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return 
            that instanceof RestConnectionSpec && 
            this.userName.equals(((RestConnectionSpec)that).userName);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.userName.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        return this.userName;
    }
    
}
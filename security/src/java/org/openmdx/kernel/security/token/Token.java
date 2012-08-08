/*
 * ====================================================================
 * Project:     OMEX/Security, http://www.omex.ch/
 * Name:        $Id: Token.java,v 1.3 2009/03/08 18:52:21 wfro Exp $
 * Description: Token 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/08 18:52:21 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.security.token;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Date;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Token
 */
final class Token 
	implements MultiLineStringRepresentation, Externalizable {
	
	/**
	 * Constructor
	 * 
	 * @param expiration
	 * @param principals
	 */
	Token(
		long timeout,
		Serializable value
	) {
		this.expiration = timeout > 0L ? System.currentTimeMillis() + timeout : -1L;
		this.value = value;
	}
	
	/**
	 * <code>serialVersionUID</code> to implement <code>Serializable</code>.
	 */
	private static final long serialVersionUID = 3256726173651515703L;

	/**
	 * The version tag is used as Zip entry name
	 */
	final static String VERSION_TAG = "1";
	
	/**
	 * The authenticated principals
	 */
	private Object value;
	
	/**
	 * The expiration 
	 */
	private long expiration;
	
	
	//------------------------------------------------------------------------
	// Implements Externalizable
	//------------------------------------------------------------------------
	
	/**
	 * Constructor to implement <code>Externalizable</code>
	 */
	public Token() {
	    super();
	}
	
	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(
		ObjectInput in
	) throws IOException, ClassNotFoundException {
		this.expiration = in.readLong();
		this.value = in.readObject();
	}
	
	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(
		ObjectOutput out
	) throws IOException {
		out.writeLong(this.expiration);
		out.writeObject(this.value);
	}
	
	/**
	 * Retrieve the token's value
	 * 
	 * @return
	 */
	Object getValue(){
		return this.value;
	}

	/**
	 * Test whether the token has not yet expired
	 * @throws TokenException  
	 * 
	 * @throws ServiceException ILLEGAL_STATE
	 * if the token has expired
	 */
	void verify(
    ) throws TokenException {
		if(
			this.expiration > 0 && 
			this.expiration < System.currentTimeMillis()
		) {
			throw new TokenException(
    			BasicException.Code.DEFAULT_DOMAIN,
    			BasicException.Code.ILLEGAL_STATE,
    			"The token has expired",
				new BasicException.Parameter(
					"expiration", 
					new Date(this.expiration)
				)
			);
		}
	}

	
	//------------------------------------------------------------------------
	// Extends Object
	//------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
        return IndentingFormatter.toString(
            this.getClass().getName() + 
            " (exires " + new Date(this.expiration) + "): " + 
            this.value
        );
	}

}
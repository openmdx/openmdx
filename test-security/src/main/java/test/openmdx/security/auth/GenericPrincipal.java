/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org
 * Description: Generic Principal
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package test.openmdx.security.auth;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.Principal;

/**
 * Generic Principal
 */
public final class GenericPrincipal 
	implements Externalizable, Principal {

	/**
     * Implements <code>Externalizable</code>
	 */
    public GenericPrincipal() {
		super();
	}

	/**
     * Constructor
     * 
     * @param type
     *        the principal's class name
     * @param identity
     *        the principal's identity
     * @param name
     *        the principal's name
     *        
     * @param NullPointerException if any of the arguments is <code>null</code>       
     */
    public GenericPrincipal(
        String type,
        String identity,
        String name
    ) {
        this.type = type;
        this.identity = identity;
        this.name = name;
    }
    
    /**
     * <code>serialVersionUID</code> to implement <code>Serializable</code>.
     */
	private static final long serialVersionUID = 3256728368329602610L;

    /**
     * the Principal's Identity
     */
    private String identity;

    /**
     * the Principal's name
     */
    private String name;

    /**
     * the Principal's type
     */
    private String type;

    /**
     * Get the principal's type
     * 
     * @return the principal's class name
     */
    public String getType(
    ){
        return this.type;
    }

    /**
     * Retrieve the identity.
     * 
     * @return the <code>identity</code>'s value
     */
    public String getIdentity() {
        return this.identity;
    }

	
	//------------------------------------------------------------------------
	// Implements Externalizable
	//------------------------------------------------------------------------
	
    /* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.type = (String) in.readObject();
		this.identity = (String) in.readObject();
		this.name = (String) in.readObject();
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(this.type);
		out.writeObject(this.identity);
		out.writeObject(this.name);
	}

	
	//------------------------------------------------------------------------
	// Implements Principal
	//------------------------------------------------------------------------

	/* (non-Javadoc)
     * @see java.security.Principal#getName()
     */
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString() {
        return 
            this.identity != null ? this.identity :
            this.name != null ? this.name : 
            super.toString();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object object) {
		if(object == this) return true;
        if(!(object instanceof GenericPrincipal)) return false;
        GenericPrincipal that = (GenericPrincipal) object;
        return 
        	(this.type == null ? that.type == null : this.type.equals(that.type)) &&
        	(this.identity == null ? that.identity == null : this.identity.equals(that.identity)) &&
        	(this.name == null ? that.name == null : this.name.equals(that.name));
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
    	return
    		this.identity != null ? this.identity.hashCode() : 
			this.name != null ? this.name.hashCode() :
			this.type != null ? this.type.hashCode() :
			0;
    }

}

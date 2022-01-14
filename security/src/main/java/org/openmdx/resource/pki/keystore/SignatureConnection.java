/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Key Connection 
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
package org.openmdx.resource.pki.keystore;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.SignatureException;

import org.openmdx.resource.pki.cci.SignatureProvider;

/**
 * Key Connection
 */
public class SignatureConnection
    extends KeyStoreConnection
    implements SignatureProvider
{

/* (non-Javadoc)
     * @see org.openmdx.resource.pki.cci.SignatureProvider#getSigner()
     */
    @Override
    public Signer getSigner(
    ) throws GeneralSecurityException {
	    return new SignerImpl(
	    	getDelegate().getSignature(ConnectionType.SIGNATURE_PROVIDER)
	    );
    }

	/* (non-Javadoc)
     * @see org.openmdx.resource.pki.cci.SignatureProvider#getValidator()
     */
    @Override
    public Verifier getVerifier(
    ) throws GeneralSecurityException {
	    return new VerifierImpl(
	    	getDelegate().getSignature(ConnectionType.SIGNATURE_VERIFIER)
	    );
    }

    /**
     * Update Implementation
     */
    static class UpdateImpl implements Updatable {
    	
    	/**
         * @param signature
         */
        protected UpdateImpl(Signature signature) {
	        this.signature = signature;
        }

		/**
    	 * 
    	 */
    	protected final Signature signature;
    	
		@Override
        public void update(byte[] data) throws SignatureException {
			this.signature.update(data);
        }

		@Override
        public void update(
        	byte[] data, 
        	int off, 
        	int len
        ) throws SignatureException {
			this.signature.update(data, off, len);
        }
    	
    }

    /**
     * Signer Implementation
     */
    static class SignerImpl extends UpdateImpl implements Signer {

    	/**
    	 * Constructor
    	 * 
    	 * @param signature
    	 */
		SignerImpl(Signature signature) {
	        super(signature);
        }

		@Override
        public byte[] sign() throws SignatureException {
			return this.signature.sign();
        }
    	
    }

    /**
     * Validator Implementation
     */
    static class VerifierImpl extends UpdateImpl implements Verifier {

    	/**
    	 * Constructor
    	 * 
    	 * @param signature
    	 */
    	VerifierImpl(Signature signature) {
	        super(signature);
        }

		/* (non-Javadoc)
         * @see org.openmdx.resource.pki.cci.SignatureProvider.Verifier#verify(byte[])
         */
		@Override
        public boolean verify(
        	byte[] signature
        ) throws SignatureException {
			return this.signature.verify(signature);
        }

		/* (non-Javadoc)
         * @see org.openmdx.resource.pki.cci.SignatureProvider.Verifier#verify(byte[], int, int)
         */
        @Override
        public boolean verify(
        	byte[] signature, 
        	int offset, 
        	int length
        ) throws SignatureException {
			return this.signature.verify(signature, offset, length);
        }
    	
    }
}

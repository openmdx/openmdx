/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Signature Provider
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.resource.pki.cci;

import java.io.Closeable;
import java.security.GeneralSecurityException;
import java.security.SignatureException;

/**
 * {@code Signature} Provider
 */
public interface SignatureProvider extends Closeable {

	/**
	 * Retrieve a signer 
	 * 
	 * @return a signer
	 * 
	 * @exception GeneralSecurityException
	 */
	Signer getSigner(
	) throws GeneralSecurityException;
	
	/**
	 * Retrieve a validator
	 * 
	 * @return a validator
	 * 
	 * @exception GeneralSecurityException
	 */
	Verifier getVerifier(
	) throws GeneralSecurityException;
	
	
	//------------------------------------------------------------------------
	// Interface Update
	//------------------------------------------------------------------------
	
	/**
	 * Update
	 */
    interface Updatable {

        void update(
        	byte[] data
        ) throws SignatureException;

        void update(
        	byte[] data, 
        	int off, 
        	int len
        ) throws SignatureException;

    }

    
	//------------------------------------------------------------------------
	// Interface Signer
	//------------------------------------------------------------------------
	
    /**
	 * Signer
	 */
    interface Signer extends Updatable {

    	/**
    	 * Calculate the signature
    	 * 
    	 * @return the signature
    	 * 
    	 * @throws SignatureException
    	 */
    	byte[] sign(
        ) throws SignatureException;
    	
    }

	//------------------------------------------------------------------------
	// Interface Validator
	//------------------------------------------------------------------------
	
    /**
     * Validator
     */
    interface Verifier extends Updatable {

    	/**
    	 * Verify the given signature
    	 * 
    	 * @param signature
    	 * 
    	 * @return {@code true} if the signature is valid
    	 * 
    	 * @throws SignatureException
    	 */
        boolean verify(
        	byte[] signature
        ) throws SignatureException;
    	
        /**
    	 * Verify the given signature
         * 
         * @param signature
         * @param offset
         * @param length
         * 
    	 * @return {@code true} if the signature is valid
    	 * 
         * @throws SignatureException
         */
        boolean verify(
        	byte[] signature,
        	int offset,
        	int length
        ) throws SignatureException;

    }
    
}

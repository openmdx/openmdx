/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Token Validator
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package test.openmdx.security.auth;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignedObject;
import java.util.zip.ZipInputStream;

import org.openmdx.kernel.exception.BasicException;

/**
 * Token Validator
 */
public class TokenValidator {

	/**
	 * Constructor
	 * 
	 * @param privateKey
	 * @param algorithm
	 */
	public TokenValidator(
		PublicKey publicKey
	){
		this.publicKey = publicKey;
	}
	
	/**
	 * The tokens are signed with this private key
	 */
	private final PublicKey publicKey;

	/**
	 * Validates a token and returns its value
	 * 
	 * @param token
	 * 
	 * @return the token's value
	 * 
	 * @throws TokenException 
	 */
	public Object getValue (
		byte[] token
	) throws TokenException {
		try {
			ByteArrayInputStream byteStream = new ByteArrayInputStream(token);	
			ZipInputStream compressedStream = new ZipInputStream(byteStream);
			String versionTag = compressedStream.getNextEntry().getName();
			if(!Token.VERSION_TAG.equals(versionTag)) throw new IllegalArgumentException(
				this.getClass().getName() + " supports version tag '" + Token.VERSION_TAG + "' but not '" + versionTag + "'"
			);
			ObjectInputStream objectStream = new ObjectInputStream(compressedStream);			
			SignedObject signedObject = (SignedObject) objectStream.readObject();
			if(
				! signedObject.verify(
					this.publicKey, 
					Signature.getInstance(signedObject.getAlgorithm())
				)
			) throw new SignatureException(
				"Signature verification failed"
			);
			Token decodedToken = (Token)signedObject.getObject();
			decodedToken.verify();
			return decodedToken.getValue();		
		} 
		catch (TokenException exception) {
            throw exception;
        } 
		catch (Exception exception) {
            throw new TokenException(
            	exception,
            	BasicException.Code.DEFAULT_DOMAIN,
            	BasicException.Code.GENERIC,
            	"Token value retrieval failed"
            );
		}
	}
	
}

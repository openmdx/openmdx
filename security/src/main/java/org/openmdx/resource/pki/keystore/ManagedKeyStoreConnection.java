/*
 * ====================================================================
 * Project:     opeMDX/Security, http://www.openmdx.org/
 * Description: Managed Key Store Connection 
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
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.PKIXParameters;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.security.PasswordCredential;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.security.PasswordCredential;
#endif
import javax.security.auth.Subject;

import org.openmdx.resource.spi.AbstractManagedConnection;

/**
 * Managed Key Store Connection 
 */
class ManagedKeyStoreConnection extends AbstractManagedConnection<ManagedConnectionFactory> {

	/**
	 * Constructor
	 */
	private ManagedKeyStoreConnection(
    	ManagedConnectionFactory factory,
        ConnectionType connectionType, 
        PasswordCredential credential, 
        ConnectionRequestInfo connectionRequestInfo
    ){
    	super(factory,"KeyStore","1.0", credential, connectionRequestInfo);
		this.connectionType = connectionType;
	}
		
	/**
     * Constructor
     */
    ManagedKeyStoreConnection(
        ManagedConnectionFactory factory,
    	ConnectionType connectionType,
        PasswordCredential credential,
        String alias,
        Certificate certificate, 
        Key key, 
        String algorithm
    ) {
    	this(factory, connectionType, credential, null);
    	this.alias = alias; 
        this.certificate = certificate;
        this.key = key;
        this.algorithm = algorithm;
    }

	/**
     * Constructor 
     *
     * @throws NoSuchAlgorithmException 
     */
    ManagedKeyStoreConnection(
        ManagedConnectionFactory factory,
    	ConnectionType connectionType,
        PasswordCredential credential,
        PKIXParameters parameters, 
        String algorithm
    ) throws NoSuchAlgorithmException {
    	this(factory, connectionType, credential, null);
        this.alias = null;
        this.certificate = null;
        this.key = null;
        this.parameters = parameters;
        this.validator = CertPathValidator.getInstance(algorithm);
    }

    /**
     * The connection type to be provided
     */
    private final ConnectionType connectionType;
    
    /**
     * {@code null} unless connectionType is CERTIFICATE or PRIVATE_KEY
     */
    private Certificate certificate;

    /**
     * {@code null} unless connectionType is PRIVATE_KEY
     */
    private Key key;
    
    /**
     * {@code null} unless connectionType is VALIDATOR
     */
    private PKIXParameters parameters;
    
    /**
     * {@code null} unless connectionType is VALIDATOR
     */
    private CertPathValidator validator;
    
    /**
     * The algorithm to be used
     */
    private String algorithm;
    
    /**
     * 
     */
    private String alias;
    

    /**
     * @return the certificate
     */
    Certificate getCertificate() {
    	return this.certificate;
    }

	/**
	 * Retrieve the signature
	 * 
     * @return the initialized signature
     * 
	 * @throws GeneralSecurityException
     */
    Signature getSignature(
    	ConnectionType type
    ) throws GeneralSecurityException {
    	Signature signature = Signature.getInstance(this.algorithm);
    	switch(type) {
	    	case SIGNATURE_PROVIDER:
	    		if(this.connectionType != ConnectionType.SIGNATURE_PROVIDER) {
	    			throw new SignatureException(
	    				"The signatures provided by this key store connection can be used for verification only"
	    			);
	    		}
	        	signature.initSign((PrivateKey) this.key);
	        	return signature;
	    	case SIGNATURE_VERIFIER:
	    		signature.initVerify(this.certificate);
	        	return signature;
	        default:
	        	throw new IllegalArgumentException("Signature cnnection type expected: " + type);
    	}
    }

	/**
     * @return the alias
     */
    String getAlias() {
    	return this.alias;
    }

    /**
     * @return the algorithm
     */
    String getAlgorithm() {
    	return this.algorithm;
    }

	/**
     * Validate a certification path
     * 
     * @param certificationPath
     * 
     * @return the certification validation result
     * 
     * @throws InvalidAlgorithmParameterException 
     * @throws CertPathValidatorException 
     */
    CertPathValidatorResult validate(
    	CertPath certificationPath
    ) throws CertPathValidatorException, InvalidAlgorithmParameterException {
    	return this.validator.validate(certificationPath, this.parameters);
    }

	/* (non-Javadoc)
     * @see javax.resource.spi.ManagedConnection#destroy()
     */
    @Override
    public void destroy(
    ) throws ResourceException {
        this.certificate = null;
        this.key = null;
    	super.destroy();
    }

    
    /* (non-Javadoc)
     * @see org.openmdx.resource.spi.AbstractManagedConnection#newConnection()
     */
    @Override
    protected Object newConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo
    ) throws ResourceException {
    	switch(this.connectionType) {
	    	case CERTIFICATE_PROVIDER: 
	    		return new CertificateConnection();
	    	case SIGNATURE_PROVIDER:
	    	case SIGNATURE_VERIFIER:
	    		return new SignatureConnection();
	    	case CERTIFICATE_VALIDATOR: 
	    		return new ValidatorConnection();
	    	default : 
	    		return null;
    	}
    }

    @Override
    protected boolean matches(
        Object credential,
        ConnectionRequestInfo connectionRequestInfo
    ) {
        // TODO Auto-generated method stub
        return false;
    }
    
}

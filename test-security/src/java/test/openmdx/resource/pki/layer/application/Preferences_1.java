/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Preferences_1 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2009, OMEX AG, Switzerland
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
package test.openmdx.resource.pki.layer.application;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.spi.Layer_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.resource.cci.ConnectionFactory;
import org.openmdx.resource.pki.cci.CertificateProvider;
import org.openmdx.resource.pki.cci.CertificateValidator;
import org.openmdx.resource.pki.cci.SignatureProvider;

/**
 * Preferences_1
 */
public class Preferences_1 extends Layer_1 {

    /**
     * Constructor 
     */
    public Preferences_1() {
    }

    private Context pkiContext;
    private Map<String,Object> pkiProviders;
    
    protected static final Path PKI_SEGMENT_PATTERN = new Path(
        "xri:@openmdx*org:openmdx:preferences1/provider/PKI/segment/:*"
    ).lock();

    protected static final Path PKI_PREFERENCES_PATTERN = new Path(
        "xri:@openmdx*org:openmdx:preferences1/provider/PKI/segment/:*/preferences/:*"
    ).lock();

    /* (non-Javadoc)
	 * @see org.openmdx.application.dataprovider.spi.Layer_1#getInteraction(javax.resource.cci.Connection)
	 */
	@Override
	public Interaction getInteraction(
		Connection connection
	) throws ResourceException {
		return new LayerInteraction(connection);
	}

	/* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    @Override
	public void activate(
        short id, 
        Configuration configuration, 
        Layer_1 delegation
    ) throws ServiceException {
        super.activate(id, configuration, delegation);
        try {
            this.pkiContext = (Context) new InitialContext().lookup("java:comp/env/pki");
        } catch (NamingException exception) {
            throw new ServiceException(exception);
        }
        this.pkiProviders = new HashMap<String,Object>();
    }
    
    @SuppressWarnings("unchecked")
	private final ConnectionFactory<?,GeneralSecurityException> getConnectionFactory(
        String segment
    ) throws ServiceException {
        Object connectionFactory;
        if(this.pkiProviders.containsKey(segment)) {
            connectionFactory = this.pkiProviders.get(segment);
        } else try {
            this.pkiProviders.put(
                segment, 
                connectionFactory = pkiContext.lookup(segment)
            );
        } catch (NamingException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "Key store connection factory acquisition failed",
                new BasicException.Parameter("jndiName", "java:comp/env/pki/" + segment)                    
            ).log();
        }
        return (ConnectionFactory<?,GeneralSecurityException>) connectionFactory;
    }

    
    //------------------------------------------------------------------------
    // Class LayerInteraction
    //------------------------------------------------------------------------
    
    /**
     * Layer Interaction
     */
    protected class LayerInteraction extends Layer_1.LayerInteraction {
        
        public LayerInteraction(
            Connection connection
        ) throws ResourceException {
            super(connection);
        }

		/* (non-Javadoc)
		 * @see org.openmdx.application.dataprovider.spi.Layer_1.LayerInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean get(
			RestInteractionSpec ispec, 
			Query_2Facade input,
			IndexedRecord output
		) throws ServiceException {        
			Path resourceIdentifier = input.getPath();
			if(resourceIdentifier.isLike(PKI_SEGMENT_PATTERN)) {
	            try {
					Object connection = getConnectionFactory(resourceIdentifier.getBase()).getConnection();
	                Object_2Facade facade = Object_2Facade.newInstance(
	                	resourceIdentifier, 
	                	"org:openmdx:preferences1:Segment"
	                );
	                facade.getValue().put(
	                	"description", 
	                	connection.getClass().getName()
	                );
	                return output.add(facade.getDelegate());
	            } catch (GeneralSecurityException exception) {
	                throw new ServiceException(
	                    exception,
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.MEDIA_ACCESS_FAILURE,
	                    "Key store connection factory acquisition failed",
	                    new BasicException.Parameter("jndiName", "java:comp/env/pki/" + resourceIdentifier.getBase())                    
	                ).log();
	            } catch (ResourceException exception) {
	            	throw new ServiceException(
	            		exception,
	            		BasicException.Code.DEFAULT_DOMAIN,
	            		BasicException.Code.SYSTEM_EXCEPTION,
	            		"Key store adapter failure"
	            	);
				}
	        } else if (resourceIdentifier.isLike(PKI_PREFERENCES_PATTERN)) {
	        	String jndiName = "java:comp/env/pki/" + resourceIdentifier.get(4);
	        	try {
	        		Object connection = getConnectionFactory(resourceIdentifier.get(4)).getConnection();
	        		Object_2Facade facade = Object_2Facade.newInstance(
        				resourceIdentifier,
	        			"org:openmdx:preferences1:Preferences"
	        		);
	        		MappedRecord preferences = facade.getValue();
	        		String type = resourceIdentifier.getBase();
	        		if("certificate".equals(type)) {
	        			Certificate certificate = ((CertificateProvider)connection).getCertificate();
	                    preferences.put("description",certificate.toString());
	                    preferences.put("absolutePath",((CertificateProvider)connection).getAlias() + "/certificate");
	                } else if ("key".equals(type)) {
	                    SignatureProvider signatureProvider = (SignatureProvider)connection;
	                    preferences.put("absolutePath", signatureProvider.getClass().getSimpleName() + "/signature");
	                } else if ("validator".equals(type)) {
	                	CertificateFactory factory = CertificateFactory.getInstance("X.509");
	                	URL url = new URL(
	               			"xri://+resource/test/openmdx/resource/pki/UserCert.pem"
	               		);
	                	Certificate certificate = factory.generateCertificate(url.openStream());
	                	CertPathValidatorResult result = ((CertificateValidator)connection).validate(
                			factory.generateCertPath(
                				Arrays.asList(certificate)
                			)
	                	);
	                    preferences.put("description",result.toString());
	                    preferences.put("absolutePath","TestTrust/validator");
	                } else {
	    	        	return super.get(ispec, input, output);
	                }
	                return output.add(facade.getDelegate());
	        	} catch (CertPathValidatorException exception) {
		        	throw new ServiceException(
	                    exception,
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.VALIDATION_FAILURE,
	                    "Certificate validation failure",
	                    new BasicException.Parameter("jndiName", jndiName)                    
	                ).log();
	        	} catch (InvalidAlgorithmParameterException exception) {
		        	throw new ServiceException(
	                    exception,
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.NOT_SUPPORTED,
	                    "Key store connection factory acquisition failed",
	                    new BasicException.Parameter("jndiName", jndiName)                    
	                ).log();
	        	} catch (GeneralSecurityException exception) {
		        	throw new ServiceException(
	                    exception,
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.MEDIA_ACCESS_FAILURE,
	                    "Key store connection factory acquisition failed",
	                    new BasicException.Parameter("jndiName", jndiName)                    
	                ).log();
	            } catch (ResourceException exception) {
	            	throw new ServiceException(
	            		exception,
	            		BasicException.Code.DEFAULT_DOMAIN,
	            		BasicException.Code.SYSTEM_EXCEPTION,
	            		"Key store adapter failure"
	            	);
				} catch (IOException exception) {
	            	throw new ServiceException(
	            		exception,
	            		BasicException.Code.DEFAULT_DOMAIN,
	            		BasicException.Code.NOT_FOUND,
	            		"User certificate Retrieval failure"
	            	);
				}
	        } else {
	        	return super.get(ispec, input, output);
	        }
		}

    }
    
}

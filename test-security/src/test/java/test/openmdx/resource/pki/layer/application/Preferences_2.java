/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Preferences 
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
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.AbstractRestPort;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.resource.cci.ConnectionFactory;
import org.openmdx.resource.pki.cci.CertificateProvider;
import org.openmdx.resource.pki.cci.CertificateValidator;
import org.openmdx.resource.pki.cci.SignatureProvider;

/**
 * Preferences
 */
public class Preferences_2 extends AbstractRestPort {

	/**
	 * Constructor
	 */
	public Preferences_2() {
		super();
	}

	private Context pkiContext;
	private final Map<String, Object> pkiProviders = new HashMap<String, Object>();

	protected static final Path PKI_SEGMENT_PATTERN = new Path(
			"xri:@openmdx*org:openmdx:preferences1/provider/PKI/segment/:*");

	protected static final Path PKI_PREFERENCES_PATTERN = new Path(
			"xri:@openmdx*org:openmdx:preferences1/provider/PKI/segment/:*/preferences/:*");

	@SuppressWarnings("unchecked")
	protected final ConnectionFactory<?, GeneralSecurityException> getConnectionFactory(String segment)
			throws ServiceException {
		Object connectionFactory;
		if (this.pkiProviders.containsKey(segment)) {
			connectionFactory = this.pkiProviders.get(segment);
		} else
			try {
				if (this.pkiContext == null) {
					this.pkiContext = (Context) new InitialContext().lookup("java:comp/env/pki");
				}
				this.pkiProviders.put(segment, connectionFactory = pkiContext.lookup(segment));
			} catch (NamingException exception) {
				throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.NOT_FOUND,
						"Key store connection factory acquisition failed",
						new BasicException.Parameter("jndiName", "java:comp/env/pki/" + segment)).log();
			}
		return (ConnectionFactory<?, GeneralSecurityException>) connectionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.
	 * Connection)
	 */
	@Override
	public Interaction getInteraction(RestConnection connection) throws ResourceException {
		return new RestInteraction(connection, newDelegateInteraction(connection));
	}

	/**
	 * Intercepting Interaction
	 */
	protected class RestInteraction extends AbstractRestInteraction {

		/**
		 * Constructor
		 */
		protected RestInteraction(RestConnection connection, Interaction delegate) throws ResourceException {
			super(connection, delegate);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.
		 * base.resource.spi.RestInteractionSpec,
		 * org.openmdx.base.rest.cci.QueryRecord,
		 * org.openmdx.base.rest.cci.ResultRecord)
		 */
		@SuppressWarnings("unchecked")
		@Override
		protected boolean get(RestInteractionSpec ispec, QueryRecord input, ResultRecord output)
				throws ResourceException {
			try {
				final Path resourceIdentifier = input.getResourceIdentifier();
				if (resourceIdentifier.isLike(PKI_SEGMENT_PATTERN)) {
					try {
						Object connection = getConnectionFactory(
								resourceIdentifier.getLastSegment().toClassicRepresentation()).getConnection();
						Object_2Facade facade = Object_2Facade.newInstance(resourceIdentifier,
								"org:openmdx:preferences1:Segment");
						facade.getValue().put("description", connection.getClass().getName());
						return output.add(facade.getDelegate());
					} catch (GeneralSecurityException exception) {
						throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.MEDIA_ACCESS_FAILURE,
								"Key store connection factory acquisition failed",
								new BasicException.Parameter("jndiName", "java:comp/env/pki/"
										+ resourceIdentifier.getLastSegment().toClassicRepresentation())).log();
					} catch (ResourceException exception) {
						throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.SYSTEM_EXCEPTION, "Key store adapter failure");
					}
				} else if (resourceIdentifier.isLike(PKI_PREFERENCES_PATTERN)) {
					String jndiName = "java:comp/env/pki/" + resourceIdentifier.getSegment(4).toClassicRepresentation();
					try {
						Object connection = getConnectionFactory(
								resourceIdentifier.getSegment(4).toClassicRepresentation()).getConnection();
						Object_2Facade facade = Object_2Facade.newInstance(resourceIdentifier,
								"org:openmdx:preferences1:Preferences");
						MappedRecord preferences = facade.getValue();
						String type = resourceIdentifier.getLastSegment().toClassicRepresentation();
						if ("certificate".equals(type)) {
							Certificate certificate = ((CertificateProvider) connection).getCertificate();
							preferences.put("description", certificate.toString());
							preferences.put("absolutePath",
									((CertificateProvider) connection).getAlias() + "/certificate");
						} else if ("key".equals(type)) {
							@SuppressWarnings("resource")
							SignatureProvider signatureProvider = (SignatureProvider) connection;
							preferences.put("absolutePath",
									signatureProvider.getClass().getSimpleName() + "/signature");
						} else if ("validator".equals(type)) {
							CertificateFactory factory = CertificateFactory.getInstance("X.509");
							URL url = new URL("xri://+resource/test/openmdx/resource/pki/UserCert.pem");
							Certificate certificate = factory.generateCertificate(url.openStream());
							CertPathValidatorResult result = ((CertificateValidator) connection)
									.validate(factory.generateCertPath(Arrays.asList(certificate)));
							preferences.put("description", result.toString());
							preferences.put("absolutePath", "TestTrust/validator");
						} else {
							return super.get(ispec, input, output);
						}
						return output.add(facade.getDelegate());
					} catch (CertPathValidatorException exception) {
						throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.VALIDATION_FAILURE, "Certificate validation failure",
								new BasicException.Parameter("jndiName", jndiName)).log();
					} catch (InvalidAlgorithmParameterException exception) {
						throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.NOT_SUPPORTED, "Key store connection factory acquisition failed",
								new BasicException.Parameter("jndiName", jndiName)).log();
					} catch (GeneralSecurityException exception) {
						throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.MEDIA_ACCESS_FAILURE,
								"Key store connection factory acquisition failed",
								new BasicException.Parameter("jndiName", jndiName)).log();
					} catch (ResourceException exception) {
						throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.SYSTEM_EXCEPTION, "Key store adapter failure");
					} catch (IOException exception) {
						throw new ServiceException(exception, BasicException.Code.DEFAULT_DOMAIN,
								BasicException.Code.NOT_FOUND, "User certificate Retrieval failure");
					}
				} else {
					return super.get(ispec, input, output);
				}
			} catch (ServiceException exception) {
				throw ResourceExceptions.toResourceException(exception);
			}
		}

	}

}

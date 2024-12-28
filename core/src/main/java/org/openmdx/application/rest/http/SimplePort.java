/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Simple Port 
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
package org.openmdx.application.rest.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.Interaction;
import javax.resource.spi.CommException;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.Interaction;
import jakarta.resource.spi.CommException;
#endif

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.net.CookieManager;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.kernel.exception.BasicException;

/**
 * The Simple Port handles BASIC authentication and cookies
 */
public class SimplePort extends HttpPort {

    /**
     * Constructor 
     */
    public SimplePort(
    ) {
    }

    /**
     * The BASIC authentication user name
     */
    private String userName = null;
    
    /**
     * The BASIC authentication user name    
     */
    private String password = null;
    
    /**
     * The authorization value
     */
    private BasicAuthentication authorization = null;
    
    /**
     * Use "text/xml" as pretty printing default value.
     * 
     * @return the default MIME type
     */
    @Override
    protected String getDefaultMimeType() {
        return "text/xml";
    }

    /**
     * Retrieve userName.
     *
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }
    
    /**
     * Set userName.
     * 
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
        this.authorization = null; // setting the user name invalidates the authorization property
    }
    
    /**
     * Retrieve password.
     *
     * @return Returns the password.
     */
    public String getPassword() {
        throw new UnsupportedOperationException("The password is a write-only property");
    }
    
    /**
     * Set password.
     * 
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
        this.authorization = null; // setting the password invalidates the authorization property
    }

    
    @Override
    public Interaction getInteraction(
        RestConnection connection
    ) throws ResourceException {
        return new SimpleInteraction(
            connection, 
            this, 
            this.newCookieHandler()
        );
    }

    /**
     * Retrieve the BASIC authorization value
     * 
     * @return the BASIC authorization value
     * 
     * @throws UnsupportedEncodingException
     */
    protected String getAuthorization(
    ) throws UnsupportedEncodingException {
        if(this.authorization == null && this.userName != null && !"".equals(this.userName)) {
            this.authorization = new BasicAuthentication(getUserName(), this.password);
        }
        return this.authorization == null ? null : this.authorization.getAuthorization();
    }

    /**
     * Provide the cookie handler
     * 
     * @return the appropriate cookie handler, or {@code null} if no cookie handling is desired
     */
    protected CookieHandler newCookieHandler(){
        return new CookieManager();
    }
    
    
    //------------------------------------------------------------------------
    // Class SimpleInteraction
    //------------------------------------------------------------------------

    /**
     * Simple Interaction does BASIC authentication cookie handling
     */
    class SimpleInteraction extends PlainVanillaInteraction {

        /**
         * Constructor 
         * 
         * @throws ResourceException 
         */
        protected SimpleInteraction(
            RestConnection connection,
            HttpContext httpContext,
            CookieHandler cookieHandler
        ) throws ResourceException {
            super(connection, httpContext);
            this.cookieHandler = cookieHandler;
            try {
                this.cookieURI = new URI(contextURL);
            } catch (URISyntaxException exception) {
                throw new ResourceException(exception);
            }
        }

        /**
         * The cookie URI
         */
        protected final URI cookieURI;
        
        /**
         * The cookie handler
         */
        protected final CookieHandler cookieHandler;

        
        /* (non-Javadoc)
         * @see org.openmdx.application.rest.http.HttpPort.PlainVanillaInteraction#getResponseCode(java.net.HttpURLConnection)
         */
        @Override
        public int getStatus(
            HttpURLConnection urlConnection
        ) throws IOException {
            int status = super.getStatus(urlConnection);
            this.cookieHandler.put(
                SimpleInteraction.this.cookieURI,
                urlConnection.getHeaderFields()
            );
            return status;
        }


        /**
         * Create connection for the given URL
         * 
         * @param uri
         * 
         * @throws ServiceException
         */
        @Override
        protected HttpURLConnection newConnection(
            URL url,
            RestInteractionSpec interactionSpec
        ) throws ResourceException {
            HttpURLConnection urlConnection = super.newConnection(url, interactionSpec);
            try {
                //
                // BASIC Authentication
                //
                String authorization = getAuthorization();
                if(authorization != null) {
                    urlConnection.setRequestProperty("Authorization", authorization);
                }
                //
                // Cookies
                //
                Map<String, List<String>> cookies = SimpleInteraction.this.cookieHandler.get(
                    SimpleInteraction.this.cookieURI, 
                    urlConnection.getRequestProperties()
                );
                for(Map.Entry<String, List<String>> entry : cookies.entrySet()) {
                    String key = entry.getKey();
                    if("Cookie2".equals(key)) {
                        StringBuilder values = new StringBuilder();
                        String separator = "";
                        for(String value : entry.getValue()) {
                            values.append(separator).append(value);
                            separator = "; ";
                        }
                        urlConnection.setRequestProperty(key, values.toString());
                    } else {
                        for(String value : entry.getValue()) {
                            urlConnection.setRequestProperty(key, value);
                        }
                    }
                }
            } catch (IOException exception) {
                throw new CommException(
                    "Can't open a connection for the given URL",
                    BasicException.newEmbeddedExceptionStack(
                		exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("url", url),
                        new BasicException.Parameter("function", interactionSpec.getFunctionName())
                    )
                );
            }
            return urlConnection;
        }
                
    }

}

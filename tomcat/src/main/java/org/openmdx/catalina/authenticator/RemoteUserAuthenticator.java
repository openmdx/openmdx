/*
 * ====================================================================
 * Project:     openMDX/Tomcat, http://www.openmdx.org/
 * Description: RemoteUserAuthenticator
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
package org.openmdx.catalina.authenticator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.CombinedRealm;
import org.apache.catalina.realm.RealmBase;
import org.apache.catalina.valves.ValveBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of authentication
 * that utilizes the pre-authenticated REMOTE_USER to identify client users.
 * The REMOTE_USER is mapped to an authenticated principal and registered with
 * the request.
 * 
 * In addition, the Authorization header is checked if the option jwtSubjectField
 * is set. If it contains a Bearer token and the payload of the token contains the 
 * jwtSubjectField (e.g. "sub"), then the value of the "sub" field is used as remote
 * user. It is is mapped to an authenticated principal and registered with the 
 * request. WARNING: use this option only if Tomcat is fronted with a web server
 * validating the JWT token. 
 *
 * The authenticator is tested in combination with the Apache mod_jk 
 * and the AJP connector with option tomcatAuthentication=false but should also
 * work when fronted with Microsoft IIS.
 * <p>
 * Example:
 * <p>
 * <b>Tomcat server.xml:</b>
 * <pre>
 *   &lt;Connector port="8009" protocol="AJP/1.3" redirectPort="8443" URIEncoding="UTF-8" tomcatAuthentication="false" /&gt;
 * </pre>
 * <p>
 * <b>MyWebApp context.xml:</b>
 * <pre>
 *   &lt;Context path="/MyWebApp" docBase="${catalina.home}/webapps/MyWebApp.war"&gt;
 *     &lt;Valve className="org.openmdx.catalina.authenticator.RemoteUserAuthenticator" /&gt;
 *   <&lt;Context&gt;
 * </pre>
 * <p>
 * <b>Apache httpd.conf:</b>
 * <pre>
 *   &lt;VirtualHost *:80&gt;
 *        JkMount /MyWebapp/* ajp13_worker
 *        &lt;Location /MyWebApp&gt;
 *                AuthType basic
 *                AuthName myrealm
 *                AuthUserFile /etc/apache2/conf/.htpasswd
 *                require valid-user
 *        &lt;/Location&gt;
 *   &lt;/VirtualHost&gt;
 * </pre>
 * 
 */
public class RemoteUserAuthenticator extends ValveBase {
	
    private static Log LOG = LogFactory.getLog(RemoteUserAuthenticator.class);
    private final Method realmBaseGetPrincipalMethod;
    private final Field combinedRealmRealmsField;
    private String jwtSubjectField = null;
    
	/**
     * Constructor.
     * 
     */
    public RemoteUserAuthenticator(
    ) {
        super();
        try {
            this.realmBaseGetPrincipalMethod = RealmBase.class.getDeclaredMethod("getPrincipal", String.class);
            this.realmBaseGetPrincipalMethod.setAccessible(true);
        } catch (Exception e) {
            String msg = "Unable to get method RealmBase.getPrincipal!";
            LOG.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
        try {
        	this.combinedRealmRealmsField = CombinedRealm.class.getDeclaredField("realms");
        	this.combinedRealmRealmsField.setAccessible(true);
        } catch (Exception e) {
            String msg = "";
            LOG.error(msg, e);
            throw new IllegalStateException(msg, e);
        }        
    }

	/**
	 * @return the jwtSubjectField
	 */
	public String getJwtSubjectField() {
		return jwtSubjectField;
	}

	/**
	 * @param jwtSubjectField the jwtSubjectField to set
	 */
	public void setJwtSubjectField(String jwtSubjectField) {
		this.jwtSubjectField = jwtSubjectField;
	}

    /* (non-Javadoc)
     * @see org.apache.catalina.valves.ValveBase#invoke(org.apache.catalina.connector.Request, org.apache.catalina.connector.Response)
     */
    @Override
    public void invoke(
    	Request request, 
    	Response response
    ) throws IOException, ServletException {
    	String remoteUser = null;
		String authorization = request.getHeader("Authorization");
    	// JWT authentication
    	if(
    		this.getJwtSubjectField() != null && 
    		authorization != null &&
    		authorization.startsWith("Bearer ")
    	) {
    		String[] jwtToken = null;
			try {
    			jwtToken = authorization.substring(7).split("\\.");
    			String payload = new String(Base64.getDecoder().decode(jwtToken[1]), "UTF-8");
    			int subStart = payload.indexOf("\"" + this.getJwtSubjectField() + "\":");
    			if(subStart < 0) {
    				subStart = payload.indexOf(this.getJwtSubjectField() + ":");
    			}
    			if(subStart > 0) {
    				int subEnd = payload.indexOf(",", subStart);
    				String[] sub = payload.substring(subStart, subEnd).split(":");
    				remoteUser = sub[1];
    				if(remoteUser.startsWith("\"")) {
    					remoteUser = remoteUser.substring(1);
    				}
    				if(remoteUser.endsWith("\"")) {
    					remoteUser = remoteUser.substring(0, remoteUser.length() - 1);
    				}
    			}
			} catch(Exception ignore) {}
            if(LOG.isDebugEnabled()) {
            	LOG.debug("Authorization: " + authorization);
            	if(jwtToken != null) {
                	LOG.debug("JWT token: " + Arrays.asList(jwtToken));            		
            	}
    			if(remoteUser != null) {
                	LOG.debug("Remote user: " + remoteUser + " [authType:" + request.getAuthType() + "]");
    			}
            }
    	}
    	// Remote user authentication (by AJP)
    	if(request.getUserPrincipal() != null) {
            remoteUser = request.getUserPrincipal().getName();
        }
        if(remoteUser != null) {
            // Remove Windows domain information
            if(remoteUser.contains("\\")) {
                remoteUser = remoteUser.substring( remoteUser.lastIndexOf("\\") + 1);
            }
            if(LOG.isDebugEnabled()) {
            	LOG.debug("Remote user: " + remoteUser + " [authType:" + request.getAuthType() + "]");
            }
            Principal principal = null;
            Session session = request.getSessionInternal(false);
            if(session != null) {
                if (LOG.isDebugEnabled()) {
                	LOG.debug("Current user already has an session [" + session.getId() + "].");
                }
                principal = session.getPrincipal();
                if (principal != null) {
                    if (LOG.isDebugEnabled()) {
                    	LOG.debug("User was already authenticated, reuse principal from session. " + principal);
                    }
                    request.setUserPrincipal(principal);
                    getNext().invoke(request, response);
                    return;
                }
            }
            if(principal == null) {
            	principal = this.getPrincipal(
            		request.getContext().getRealm(), 
            		remoteUser
            	);
            }
            this.register(
            	request, 
            	response, 
            	principal, 
            	request.getAuthType(), 
            	remoteUser
            );
        }
        getNext().invoke(request, response);
    }
    
    /**
     * Register an authenticated Principal and authentication type in our
     * request, in the current session (if there is one). NOTE: this method 
     * is derived from AuthenticatorBase.register().
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are generating
     * @param principal The authenticated Principal to be registered
     * @param authType The authentication type to be registered
     * @param username Username used to authenticate (if any)
     */
    public void register(
    	Request request, 
    	HttpServletResponse response,
    	Principal principal, 
    	String authType,
        String username
    ) {
        if(LOG.isDebugEnabled()) {
            String name = (principal == null) ? "none" : principal.getName(); 
            LOG.debug("Authenticated '" + name + "' with type '" + authType +
                    "'");
        }
        request.setAuthType(authType);
        request.setUserPrincipal(principal);
        Session session = request.getSessionInternal(false);
        if(session != null) {
            Manager manager = request.getContext().getManager();
            manager.changeSessionId(session);
            request.changeSessionId(session.getId());
        } 
        if(session != null) {
            session.setAuthType(authType);
            session.setPrincipal(principal);
            if (username != null) {
                session.setNote(Constants.SESS_USERNAME_NOTE, username);
            } else {
                session.removeNote(Constants.SESS_USERNAME_NOTE);
            }
        }
    }

    /**
     * Get principal in realm for given user name.
     * 
     * @param realm
     * @param username
     * @return
     */
    protected Principal getPrincipal(
    	Realm realm, 
    	String username
    ) {
        if (LOG.isDebugEnabled()) {
        	LOG.debug("Try to find user " + username + " based on container configured realms");
        }
        if (realm instanceof RealmBase) {
            if(realm instanceof CombinedRealm) {
                if (LOG.isDebugEnabled()) {
                	LOG.debug("application uses a combined realm. try to retrieve realm list.");
                }
                try {
                    @SuppressWarnings("unchecked")
					List<Realm> realmList = (List<Realm>)this.combinedRealmRealmsField.get(realm);
                    if (realmList != null) {
                        for (Realm r : realmList) {
                        	Principal p = this.getPrincipal(r, username);
                            if (p != null) {
                                if (LOG.isDebugEnabled()) {
                                	LOG.debug("Sucessfully found principal for given username. " + p);
                                }
                                return p;
                            }
                        }
                    }
                } catch (Throwable t) {
                    final String msg = "Unable to find principal for user " + username + " [" + t.getMessage() + "]";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(msg, t);
                    } else {
                        LOG.error(msg);
                    }
                }
            } else {
                try {
                    return (Principal)this.realmBaseGetPrincipalMethod.invoke(realm, username);
                } catch (Exception e) {
                    final String msg = "Unable to invoke getPrincipal(..) for user " + username + " [" + e.getMessage() + "]";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(msg, e);
                    } else {
                        LOG.error(msg);
                    }
                }
            }
        }
        return null;
    }

}

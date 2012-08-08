/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RadiusLoginModule.java,v 1.7 2010/07/14 13:49:46 hburger Exp $
 * Description: Java Radius Client Derivate
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/07/14 13:49:46 $
 * ====================================================================
 *
 * Copyright (C) 2004  OMEX AG
 *
 * * This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 * * This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Lesser General Public License for more details.
 *
 * * You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * 
 * This library BASED on Java Radius Client 2.0.0
 * (http://http://jradius-client.sourceforge.net/),
 * but it's namespace and content has been MODIFIED by OMEX AG
 * in order to integrate it into the openMDX framework.
 */
package org.openmdx.uses.net.sourceforge.jradiusclient.jaas;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusAttributeValues;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusClient;
import org.openmdx.uses.net.sourceforge.jradiusclient.RadiusPacket;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.InvalidParameterException;
import org.openmdx.uses.net.sourceforge.jradiusclient.exception.RadiusException;
import org.openmdx.uses.net.sourceforge.jradiusclient.packets.PapAccessRequest;


/**
 * This is an implementation of javax.security.auth.spi.LoginModule specific to
 * using a RADIUS Server for authentication.
 * @author <a href="mailto:bloihl@users.sourceforge.net">Robert J. Loihl</a>
 */
public class RadiusLoginModule implements LoginModule {

    public static final int MAX_CHALLENGE_ATTEMPTS = 3;
    //initial state variables
    protected Subject radiusSubject;
    protected CallbackHandler callbackHandler;
    protected Map<String,?> sharedState;
    protected Map<String,?> moduleOptions;
    //private state of the authentication attempt
    private boolean authenticationSucceeded = false;
    private boolean authenticationCommitted = false;
    //state variables for this auth attempt
    private String userName;
    private RadiusPrincipal userPrincipal;
    private int challengedAttempts = 0;
    private RadiusClient radiusClient;
    /**
     * Method to abort the authentication process (phase 2). This method gets
     * called if the LoginContext's overall authentication process failed
     * (i.e. one of the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * <code>LoginModules</code> did not succeed). It also cleans up any
     * internal state saved by the login method.
     * @return boolean true if this method succeeds false if this
     *                      <code>LoginModule</code> should be ignored
     * @exception LoginException If the abort fails
     */
    public boolean abort() throws LoginException{
        if(!this.authenticationSucceeded){
            return false;
        }else if(this.authenticationSucceeded && !this.authenticationCommitted){
            //Radius authentication succeeded but overall authentication failed
            this.authenticationSucceeded = false;
            this.userName = null;
            this.radiusClient = null;
            this.userPrincipal = null;
            this.challengedAttempts = 0;
        }else{
            //overall authentication succeeded and our commit succeeded,
            //but someone else's commit failed
            this.logout();
        }
        return true;
    }
    /**
     * Method to commit the authentication process (phase 2). This method gets
     * called if the LoginContext's overall authentication process succeeded
     * (i.e. all of the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL
     * <code>LoginModules</code> succeeded).
     * If this LoginModule's own authentication attempt succeeded (checked by
     * retrieving the private state saved by the login method), then this
     * method associates relevant Principals and Credentials with the Subject
     * located in the LoginModule. If this LoginModule's own authentication
     * attempt failed, then this method cleans up any internal state saved by
     * the login method. ( poss. improvement: perform
     * a RADIUS accounting request to notify RADIUS server of login time.)
     * @return boolean true if this method succeeds false if this
     *                      <code>LoginModule</code> should be ignored
     * @exception LoginException If the commit action fails
     */
    public boolean commit() throws LoginException{
        if(!this.authenticationSucceeded){
            return false;
        } else {
            //add a principal to the subject
            this.userPrincipal = new RadiusPrincipal(this.userName);
            if(!this.radiusSubject.getPrincipals().contains(this.userPrincipal)){
                this.radiusSubject.getPrincipals().add(this.userPrincipal);
            }
            //now clean out state
            this.userName = null;
            this.radiusClient = null;
            this.challengedAttempts = 0;
            this.authenticationCommitted = true;
        }
        return true;
    }
    /**
     * Initialize this <code>LoginModule</code>.
     * This method is called by the LoginContext after this LoginModule has
     * been instantiated. The purpose of this method is to initialize this
     * LoginModule with the relevant information. If this LoginModule does not
     * understand any of the data stored in sharedState or options parameters,
     * they can be ignored. There MUST be the following parameters specified in
     * the options:<br>
     * <ul>
     * <li>hostname - the fully qualified name or IP address of the RADIUS Server</li>
     * <li>shared secret - the secret shared between us and the RADIUS Server</li>
     * </ul>
     * The following parameters MAY be specified, but they must be supplied together:<br>
     * <ul>
     * <li>Authenication Port - The port the RADIUS Server is listening on for
     * authentication</li>
     * <li>Accounting Port - The port the RADIUS Server is listening on for
     * accounting requests</li>
     * </ul>
     * @param subject javax.security.auth.Subject
     * @param callbackHandler javax.security.auth.callback.CallbackHandler
     * @param sharedState java.util.Map
     * @param options java.util.Map
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String,?> sharedState, Map<String,?> options) {
        this.radiusSubject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.moduleOptions = options;
    }
    /**
     * Authenticates this Subject against a RADIUS Server (phase 1). It uses
     * the callbacks to request a UserName and a Password, and possibly requests
     * a response to a challenge recieved from the RADIUS server.
     * @return boolean True if this <code>LoginModule</code> succeeds, False if
     *                      this <code>LoginModule</code> should be ignored
     * @exception FailedLoginException if the login fails
     * @exception LoginException If this <code>LoginModule</code> can't perform
     *                           the requested authentication
     */
    public boolean login() throws LoginException{
        //perform callbacks
        if (this.callbackHandler == null) {
            throw new LoginException("Error: No callback handler installed to gather username and password.");
        }

        // create callbacks
        NameCallback nameCallback = new NameCallback("User Name: ");
        PasswordCallback passwordCallback = new PasswordCallback("Password: ",true);//turn on password echo(?)
        RadiusCallback radiusCallback = new RadiusCallback();

        Callback[] callbacks = new Callback[3];
        callbacks[0] = nameCallback;
        callbacks[1] = passwordCallback;
        callbacks[2] = radiusCallback;

        try {
            // send callbacks to callback handler
            this.callbackHandler.handle(callbacks);
        } catch(IOException ioex) {
            throw new LoginException(ioex.getMessage());
        } catch(UnsupportedCallbackException uscbex) {
            StringBuffer sb = new StringBuffer("Error: callback ");
            sb.append(uscbex.getCallback().toString());
            sb.append(" not supported.");
            throw new LoginException(sb.toString());
        }

        this.userName = nameCallback.getName();
        char[] userPassword = passwordCallback.getPassword();
        if(userPassword == null){
            //treat a null password as a zero length password
            userPassword = new char[0];
        }
        //finally clear the password
        passwordCallback.clearPassword();

        //now authenticate
        try{
            this.radiusClient = new RadiusClient(radiusCallback.getHostName(),
                                                 radiusCallback.getAuthPort(),
                                                 radiusCallback.getAcctPort(),
                                                 radiusCallback.getSharedSecret());
            RadiusPacket accessRequest = new PapAccessRequest(this.userName,String.valueOf(userPassword));
            this.authenticate(accessRequest, radiusCallback.getNumRetries() );
        }catch(InvalidParameterException ivpex){
            StringBuffer sb1 = new StringBuffer("Configuration of the RADIUS client is incorrect. ");
            sb1.append(ivpex.getMessage());
            throw new LoginException(sb1.toString());
        }catch(RadiusException rex){
            StringBuffer sb2 = new StringBuffer("Configuration of the RADIUS client is incorrect. ");
            sb2.append(rex.getMessage());
            throw new LoginException(sb2.toString());
        }
        //finally clear the password in memory
        for(int i = 0; i < userPassword.length;i++){
            userPassword[i] = ' ';
        }
        userPassword = null;
        this.authenticationSucceeded = true;
        //everything went well, return true
        return true;
    }
    /**
     * Authenticates this Subject against a RADIUS Server (phase 1). It may request
     * a response to a challenge recieved from the RADIUS server. It will do this
     * using a single PasswordCallback with the Challenge message as the prompt.
     * @exception FailedLoginException if the login fails
     * @exception LoginException If this <code>LoginModule</code> can't perform
     *                           the requested authentication
     */
    private void authenticate( final RadiusPacket accessRequest, final int numRetries) throws LoginException {
        try {
            //requestAttributes can be null, the radiusClient.authenticate method checks for this and handles it fine - BL
            RadiusPacket accessResponse = this.radiusClient.authenticate(accessRequest, numRetries);
            switch (accessResponse.getPacketType()) {
                case RadiusPacket.ACCESS_ACCEPT:
                    //SUCCESS!!!!
                    break;
                case RadiusPacket.ACCESS_REJECT:
                    throw new CredentialExpiredException("Incorrect User Name or Password.");
                case RadiusPacket.ACCESS_CHALLENGE:
                    if (this.challengedAttempts > RadiusLoginModule.MAX_CHALLENGE_ATTEMPTS) {
                        this.challengedAttempts = 0;
                        throw new LoginException("Maximum number of challenge retries exceeded.");
                    }
                    Callback[] callbacks = new Callback[1];
                    String password = null;
                    callbacks[0] = new PasswordCallback(String.valueOf(accessResponse.getAttribute(RadiusAttributeValues.REPLY_MESSAGE).getValue()),true);
                    try {
                        this.callbackHandler.handle(callbacks);
                        password = String.valueOf(((PasswordCallback)callbacks[0]).getPassword());
                        if (password == null) {
                            //treat a null password as a zero length password
                            password = new String("");
                        }
                        //finally clear the password
                        ((PasswordCallback)callbacks[0]).clearPassword();
                    } catch(IOException ioex) {
                        throw new LoginException(ioex.getMessage());
                    } catch(UnsupportedCallbackException uscbex) {
                        StringBuffer sb = new StringBuffer("Error: callback ");
                        sb.append(uscbex.getCallback().toString());
                        sb.append(" not supported.");
                        throw new LoginException(sb.toString());
                    }
                    //do this first so that we are actually incrementing the BEFORE
                    //we get recursive
                    this.challengedAttempts++;
                    RadiusPacket challengeResponse = new PapAccessRequest(this.userName,String.valueOf(password));
                    this.authenticate(challengeResponse, 1);
                    break;
                default:
                    throw new LoginException("Received an Invalid response from the RADIUS Server.");
            }
        } catch(InvalidParameterException ivpex) {
            throw new LoginException(ivpex.getMessage());
        } catch(RadiusException rex) {
            throw new LoginException(rex.getMessage());
        }
    }

    /**
     * This method logs out a Subject (Poss. Improvement: perform
     * a RADIUS accounting request to notify RADIUS server of logout time.)
     * @return boolean return true if the logout was successful, False if
     *                      this <code>LoginModule</code> should be ignored
     * @exception LoginException if the logout fails.
     */
    public boolean logout() throws LoginException {
        this.radiusSubject.getPrincipals().remove(this.userPrincipal);
        this.authenticationCommitted = false;
        this.authenticationSucceeded = false;
        this.userName = null;
        this.radiusClient = null;
        this.userPrincipal = null;
        this.challengedAttempts = 0;
        return true;
    }
}
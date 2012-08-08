/*
 * ====================================================================
 * Project:     OMEX/Security, http://www.omex.ch/
 * Name:        $Id: RemoteLoginModule.java,v 1.14 2008/04/04 17:55:32 hburger Exp $
 * Description: Remote Login Module
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 17:55:32 $
 * ====================================================================
 *
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
 * All rights reserved.
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
 */
package org.openmdx.security.auth.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.jmi.reflect.RefException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.cci.Authority;
import org.openmdx.base.cci.Provider;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipal;
import org.openmdx.kernel.security.authentication.spi.GenericPrincipals;
import org.openmdx.security.authentication1.cci.AuthenticationContext;
import org.openmdx.security.authentication1.jmi.Authentication1Package;
import org.openmdx.security.realm1.cci.Credential;
import org.openmdx.security.realm1.cci.Group;
import org.openmdx.security.realm1.cci.Principal;
import org.openmdx.security.realm1.cci.Realm;
import org.openmdx.security.realm1.cci.ValidationResult;
import org.openmdx.security.realm1.jmi.Realm1Package;
import org.openmdx.security.realm1.query.PrincipalQuery;

/**
 * The remote authenticator's login module implementation.
 */
public class RemoteLoginModule implements LoginModule {
    
    /**
     * the subject for this login
     */
    private Subject subject;
    
    /**
     * where to get user names, passwords, ... for this login
     */
    private CallbackHandler callbackHandler;
    
    /** 
     * Did we add principals or credentials to the subject?
     */
  	private boolean subjectModified;
  	
  	/** 
  	 * if so, what principals did we add to the subject
  	 * (so we can remove the principals we added if the login is aborted)
  	 */
  	private Set<java.security.Principal> principalsForSubject; 

    /** 
     * if so, what credentials did we add to the subject
     * (so we can remove the credentials we added if the login is aborted)
     */
    private Set<Object> credentialsForSubject; 

    /**
     * The realm's object id is passed as an option
     */
    private Path realmId;
    
    /**
     * The persistence manager factory is passed as option
     */
    private PersistenceManagerFactory persistenceManagerFactory;

    /**
     * Which credential request codes ask for visible input
     */
    private Object echoOn;
    
    /**
     * Name Callback Prompt
     */
    private String namePrompt;

    /**
     * Password Callback Prompt
     */
    private String passwordPrompt;

    /**
     * Text Output Callback Realm Prompt
     */
    private String realmInformation;

    /**
     * Initialize a login attempt.
     *
     * @param subject the Subject this login attempt will populate.
     *
     * @param callbackhandler the CallbackHandler that can be used to
     * get the user name, and in authentication mode, the user's password
     *
     * @param sharedState A Map containing data shared between login
     * modules when there are multiple authenticators configured.<br>
     * <code>RemoteLoginModule</code> does not use this parameter.
     *
     * @param options A Map containing options that the authenticator's
     * authentication provider impl wants to pass to its login module impl.<ul>
     * <li><b><code>"org.openmdx.security.realm1.cci.Realm"</code>:</b>
     *     <em>(mandatory)</em>
     *     The realm's object id, i.e. 
     *     "xri://@openmdx*org.openmdx.secuyrity.realm1/provider/</code>&lsaquo;provider name&rsaquo;<code>/segment/</code>&lsaquo;sement name&rsaquo;<code>/realm/</code>&lsaquo;realm name&rsaquo;<code>
     * <li><b><code>"javax.jdo.PersistenceManagerFactory"</code>:</b> 
     *     <em>(mandatory)</em>
     *     A JDO persistence manager factory instance.
     * <li><b><code>"javax.security.auth.callback.NameCallback.prompt"</code>:</b>
     *     <em>(optional)</em>
     *     The prompt to be used by the
     *     <code>javax.security.auth.callback.NameCallback</code> 
     * <li><b><code>"javax.security.auth.callback.PasswordCallback.prompt"</code>:</b>
     *     <em>(optional)</em>
     *     The prompt to be used by the
     *     <code>javax.security.auth.callback.PasswordCallback</code> supporting the 
     *     following placeholders<ul>
     *     <li><code>${name}</code> The <code>NameCallback</code>'s name
     *     <li><code>${challenge}</code> The credential's challenge
     *     </ul> 
     * <li><b><code>"javax.security.auth.callback.TextOutputCallback.realm"</code>:</b>
     *     <em>(optional)</em>
     *     The prompt to be used by the
     *     <code>javax.security.auth.callback.TextOutputCallback</code> 
     *     for realm information supporting the following placeholders<ul>
     *     <li><code>${realm.id}</code> The realm path's base name
     *     <li><code>${realm.xri}</code> The realm path's XRI
     *     </ul>
     * <li><b><code>"javax.security.auth.callback.PasswordCallback.echoOn"</code>:</b> 
     *     <em>(optional)</em>
     *     one of<ul>
     *     <li>a <code>java.lang.Boolean</code>
     *          telling whether the
     *          <code>javax.security.auth.callback.PasswordCallback</code>'s
     *          echo is to be switched on or not
     *     <li>a <code>java.utilSet</code> of
     *          <code>org.openmdx.security.realm1.cci.ValidationResult</code> 
     *          codes requiring their 
     *          <code>javax.security.auth.callback.PasswordCallback</code>'s
     *          echo to be switched on
     *     </ul> 
     * </ul>
     * 
     * @exception <code>NullPointerException</code> if one of the following options is missing<ul>
     * <li><code>"org.openmdx.security.realm1.cci.Realm"</code>
     * <li><code>"javax.jdo.PersistenceManagerFactory"</code>
     * </ul>
     */
  	public void initialize(
      	Subject         subject,
      	CallbackHandler callbackHandler,
      	Map<String,?>   sharedState,
      	Map<String,?>   options
  	){
      	//
      	// only called (once!) after the constructor and before login
      	//
    	this.subject = subject;
    	this.callbackHandler = callbackHandler;
        String realmId = options.get(Realm.class.getName()).toString();
        if(realmId == null || realmId.length() == 0) throw new NullPointerException(
            "The option " + Realm.class.getName() + " is missing or empty"
        );
        this.realmId = new Path(realmId);
        this.persistenceManagerFactory = (PersistenceManagerFactory) options.get(
            PersistenceManagerFactory.class.getName()
        );        
        if(this.persistenceManagerFactory == null) throw new NullPointerException(
            "The option " + PersistenceManagerFactory.class.getName() + " is missing"
        );
        this.echoOn = options.get(PasswordCallback.class.getName() + ".echoOn");
        this.namePrompt = options.get(NameCallback.class.getName() + ".prompt").toString();
        if(this.namePrompt == null) this.namePrompt = "Name";
        this.passwordPrompt = options.get(PasswordCallback.class.getName() + ".prompt").toString();
        if(this.passwordPrompt == null) this.passwordPrompt = "${challenge}";
        this.realmInformation = options.get(TextOutputCallback.class.getName() + ".realm").toString();
        if(this.realmInformation == null) this.realmInformation = "Realm ${realm}";
    	this.principalsForSubject = new HashSet<java.security.Principal>();
        this.credentialsForSubject = new HashSet<Object>();
        this.subjectModified = false;
   	}

    /**
   	* Attempt to login.
   	*
   	* @return A boolean indicating whether or not the login for
   	* this login module succeeded.
   	*/
  	public boolean login(
    ) throws LoginException {
        try {
            PersistenceManager persistenceManager = this.persistenceManagerFactory.getPersistenceManager();
            Transaction unitOfWork = persistenceManager.currentTransaction();
            Authority realmAuthority = (Authority) persistenceManager.getObjectById(
                Realm1Package.AUTHORITY_XRI
            );
            Authority authenticationAuthority = (Authority) persistenceManager.getObjectById(
                Authentication1Package.AUTHORITY_XRI
            );
            Provider realmProvider = realmAuthority.getProvider(this.realmId.get(2));
            Provider authenticationProvider = authenticationAuthority.getProvider(this.realmId.get(2));
            org.openmdx.security.realm1.cci.Segment realmSegment = (org.openmdx.security.realm1.cci.Segment) realmProvider.getSegment(
                this.realmId.get(4)
            );
            org.openmdx.security.authentication1.cci.Segment authenticationSegment = (org.openmdx.security.authentication1.cci.Segment) authenticationProvider.getSegment(
                this.realmId.get(4)
            );
            Authentication1Package authenticationPackage = (Authentication1Package) authenticationSegment.refImmediatePackage();
            Realm realm = realmSegment.getRealm(this.realmId.get(6));
            Realm1Package realm1Package = (Realm1Package) realm.refImmediatePackage();
            NameCallback nameCallback;
            PasswordCallback[] passwordCallbacks;
            Principal principal;
            Credential[] authenticationCredentials;
            ValidationResult[] validationResult;
            AuthenticationContext authenticationContext = null;
            try {
                nameCallback = new NameCallback(this.namePrompt);
                String realmInformation = format(
                    this.realmInformation,
                    "realm.xri",
                    this.realmId.toXRI()
                );
                realmInformation = format(
                    realmInformation,
                    "realm.id",
                    this.realmId.getBase()
                );
                this.callbackHandler.handle(
                    new Callback[]{
                        new TextOutputCallback(
                            TextOutputCallback.INFORMATION,
                            realmInformation
                        ),
                        nameCallback
                    }
                );
                String name = nameCallback.getName();
                if(name == null || "".equals(name)) throw new LoginException(
                    this.namePrompt + ": missing"
                );
                PrincipalQuery principalQuery = realm1Package.createPrincipalQuery();
                principalQuery.name().equalTo(name);
                principalQuery.disabled().isFalse();
                List<?> principals = realm.getPrincipal(principalQuery);
                switch (principals.size()) {
                    case 0: throw new FailedLoginException(
                        "Found no enabled principal named " + name
                    );
                    case 1: break;
                    default: throw new FailedLoginException(
                        "Found " + principals.size() +
                        " enabled principals named " + name
                    );
                }
                principal = (Principal) principals.get(0);
                Collection<?> authCredentials = principal.getAuthCredential();
                authenticationCredentials = authCredentials.toArray(
                    new Credential[]{}
                );
                passwordCallbacks = new PasswordCallback[authenticationCredentials.length];
                validationResult = new ValidationResult[authenticationCredentials.length];
                unitOfWork.begin();
                for(
                    int i = 0;
                    i < authenticationCredentials.length;
                    i++
                ) {
                    org.openmdx.security.realm1.cci.CredentialRequestParams params =  realm1Package.createCredentialRequestParams(authenticationContext);               
                    validationResult[i] = authenticationCredentials[i].request(params);
                }
                unitOfWork.commit();
                for(
                    int i = 0;
                    i < validationResult.length;
                    i++
                ) if(validationResult[i].isAccepted()) {
                    String passwordPrompt = format(
                        this.passwordPrompt,
                        "name",
                        name
                    );
                    passwordPrompt = format(
                        passwordPrompt,
                        "challenge",
                        validationResult[i].getChallenge()
                    );
                    passwordCallbacks[i] = new PasswordCallback(
                        passwordPrompt,
                        isPasswordEchoOn(validationResult[i].getCode())
                    );
                } else throw new FailedLoginException(
                    principal.getName() + " can't be authenticated: " +
                    validationResult[i].getChallenge()
                );
                this.callbackHandler.handle(passwordCallbacks);
            } catch (LoginException exception) {
                throw exception;
            } catch (Exception exception) {
                throw (LoginException) new LoginException(
                    "Callback processing failed"
                ).initCause(
                    exception
                );
            }
            boolean preparing = true;
            try {
                unitOfWork.begin();
//              persistenceManager.makeTransactional(principal);
                authenticationContext = authenticationPackage.getAuthenticationContext(
                ).createAuthenticationContext(realm);
                authenticationContext.setSubject(principal.getSubject());
                authenticationSegment.addAuthenticationContext(authenticationContext);
                for(
                    int i = 0;
                    i < validationResult.length;
                    i++
                ) {
                    char[] password = passwordCallbacks[i].getPassword();
                    org.openmdx.security.realm1.cci.CredentialValidateParams params = realm1Package.createCredentialValidateParams(
                        authenticationContext, 
                        validationResult[i].getState(), 
                        UnicodeTransformation.toByteArray(password, 0, password.length)
                    );
                    validationResult[i] = authenticationCredentials[i].validate(params);
                }
                preparing = false;
                unitOfWork.commit();
            } catch (JmiServiceException exception){
                if(preparing) unitOfWork.rollback();
                throw exception;
            } catch (RefException exception) {
                if(preparing) unitOfWork.rollback();
                throw new RuntimeServiceException(exception);
            }
            for(
                int i = 0;
                i < validationResult.length;
                i++
            ) if(
                !validationResult[i].isAccepted()
            ) throw new FailedLoginException(
                "Login for principal '" + principal.getName() + "' failed: " +
                principal.refMofId()
            );
            List<Principal> principals = new ArrayList<Principal>();
            principals.add(principal);
            for(Principal p : principals) {
                for(Object g : p.getIsMemberOf()) { 
                    if(!principals.contains(g)) principals.add((Group)g);
                }
            }
            for(Principal p : principals) {
                this.principalsForSubject.add(
                    new GenericPrincipal (
                        p == principal ? 
                            GenericPrincipals.USER :
                        p instanceof Group ?
                            GenericPrincipals.GROUP :
                            p.getClass().getName(),
                        p.refMofId(),
                        p.getName()
                    )
                );
            }
        } catch (RuntimeException exception) {
            new ServiceException(exception).log();
            throw (LoginException) new LoginException(
                "Login failed"
            ).initCause(
                exception
            );
        }
        return true;
  	}

   /**
   	* Completes the login by adding the user and the user's groups
   	* to the subject.
   	*
   	* @return A boolean indicating whether or not the commit succeeded.
   	*/
  	public boolean commit(
    ) throws LoginException {
      	//
      	// only called (once!) after login
      	//
      	// put the user and the user's groups (computed during the
      	// login method and stored in the principalsForSubject object)
      	// into the subject.
      	//
        this.principalsForSubject.removeAll(this.subject.getPrincipals());
        this.subject.getPrincipals().addAll(this.principalsForSubject);
        this.subject.getPublicCredentials().addAll(this.credentialsForSubject);
  	  	return this.subjectModified = true;
  	}

   /**
   	* Aborts the login attempt.  Remove any principals we put
   	* into the subject during the commit method from the subject.
   	*
   	* @return A boolean indicating whether or not the abort succeeded.
   	*/
  	public boolean abort(
    ) throws LoginException {
  	    //
  	    // only called (once!) after login or commit
  	    // or may be? called (n times) after abort
  	    //
  	    if (this.subjectModified) {
  	        subject.getPrincipals().removeAll(this.principalsForSubject);
            subject.getPublicCredentials().removeAll(this.credentialsForSubject);
  	        this.subjectModified = false;
        }
  	    return true;
  	}

  	/**
  	 * Logout.  This should never be called.
  	 *
  	 * @return A boolean indicating whether or not the logout succeeded.
  	 */
  	public boolean logout(
  	) throws LoginException {
  	    //
  	    // should never be called
  	    //
        if (this.subjectModified) {
            subject.getPrincipals().removeAll(this.principalsForSubject);
            subject.getPublicCredentials().removeAll(this.credentialsForSubject);
            this.subjectModified = false;
        }
    	return true;
  	}
    
    /**
     * Tells whether the <code>PasswordCallback</code>'s echo should be switched on or not.
     * 
     * @param code the <ValidationResult</code>'s code
     * 
     * @return <code>true</code> if the <code>PasswordCallback</code>'s echo should be switched on
     */
    @SuppressWarnings("unchecked")
	private boolean isPasswordEchoOn(
        Short code
    ){
        return 
            this.echoOn instanceof Boolean ? ((Boolean)this.echoOn).booleanValue() :
            this.echoOn instanceof Set ? ((Set<?>)this.echoOn).contains(code) :
            false;
    }

    /**
     * Replace a placeholder by its value
     * 
     * @param raw
     * @param field
     * @param value
     * 
     * @return the fomratted string
     */
    private String format(
        String raw,
        String field,
        Object value
    ){
        String placeholder = "${" + field + '}';
        int i = raw.indexOf(placeholder);
        return i < 0 ?
            raw :
            raw.substring(0, i) + value + raw.substring(i + placeholder.length());
    }

}

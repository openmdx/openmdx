/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ServiceHeader.java,v 1.10 2011/11/26 01:34:59 hburger Exp $
 * Description: Service Header
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.cci;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOFatalUserException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.openmdx.base.persistence.spi.PersistenceManagers;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.text.MultiLineStringRepresentation;

/**
 * This class represents a service header
 */
public final class ServiceHeader
    implements MultiLineStringRepresentation, Serializable, Cloneable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3546359530901812277L;

    /**
     * Constructor
     *
     * @param       principal
     *              The request is processed on behalf of principal.
     * @param       correlationId
     * @param       trace
     *              Define, whether the request should be traced
     *              over all the tiers.
     * @param       qualityOfService
     *              The requested quality of service
     */ 
    public ServiceHeader(
        String principal
    ){
        this(
            principal, 
            null // requestedAt
        );
    }

    /**
     * Constructor
     *
     * @param       principal
     *              The request is processed on behalf of principal.
     * @param       correlationId
     * @param       trace
     *              Define, whether the request should be traced
     *              over all the tiers.
     * @param       qualityOfService
     *              The requested quality of service
     * @param       requestedAt
     *              Defines the time point for queries of stateful objects
     * @param       requestedFor
     *              Defines the time point for the data of stateful objects
     */ 
    public ServiceHeader(
        String principal,
        String requestedAt
    ){
        this(
            principal == null ? 
                NO_PRINCIPALS : 
                Collections.singletonList(principal), 
            requestedAt 
        );
    }

    /**
     * Constructor for marshallers.
     *
     * @param       principalChain
     *              The request is processed on behalf of principalChain.
     * @param       correlationId
     * @param       trace
     *              Define, whether the request should be traced
     *              over all the tiers.
     * @param       qualityOfService
     *              The requested quality of service
     * @param       requestedAt
     *              Defines the time point for queries of stateful objects
     * @param       requestedFor
     *              Defines the time point for the data of stateful objects
     */ 
    private ServiceHeader(
        List<String> principalChain,
        String requestedAt
    ){
        this.principalChain = new ArrayList<String>(principalChain);
        this.requestedAt = requestedAt;
    }
    
    /**
     * Default constructor
     */
    public ServiceHeader(
    ){
        this(
            NO_PRINCIPALS, // principalChain
            null // requestedAt
        );
    }

    /**
     * The request is processed on behalf of principalChain.
     */
    private final List<String> principalChain;

    /**
     * 
     */
    private final static List<String> NO_PRINCIPALS = Collections.emptyList();
    
    /**
     * For toString
     */
    private final static String[] TO_STRING_CONTENT = {
        "principalChain",
        "requestedAt",
    };

    public List<String> getPrincipalChain(){
        return Collections.unmodifiableList(this.principalChain);
    }
    
    /**
     * Replaces the principal list by one containing the given principal
     */
    public void setPrincipal(
        String principal
    ){
        this.principalChain.clear();
        addPrincipal(principal);
    }
        
    /**
     * Adds a principal to the principal chain
     */
    public void addPrincipal(
        String principal
    ){
        if(principal != null) this.principalChain.add(principal);
    }

    private final String requestedAt;
    public String getRequestedAt(
    ){
        return this.requestedAt;
    }

    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone(
    ) throws CloneNotSupportedException {        
        return new ServiceHeader(
             this.principalChain,
             this.requestedAt
        );
    }

    /**
     * Convert the stringified principal chain into an array
     * 
     * @param connectionUsername a principal or the stringified principal list 
     * 
     * @return a principal array
     */
    private static String[] toPrincipalArray(
        String connectionUsername
    ){
        Collection<String> principalNames = PersistenceManagers.toPrincipalChain(connectionUsername);
        return principalNames.toArray(
            new String[principalNames.size()]
        );
    }
    
    /**
     * Retrieve a subject's password credential
     * 
     * @param subject
     * 
     * @return the subject's password credential
     */
    private static PasswordCredential getCredential(
        Subject subject
    ){
        Set<PasswordCredential> credentials = subject.getPrivateCredentials(PasswordCredential.class);
        switch(credentials.size()) {
            case 0: 
                return null;
            case 1: 
                return credentials.iterator().next();
            default: 
                throw new JDOFatalUserException(
                    "The subject should contain exactly one password credential: " + credentials.size()
                );
        }
    }

    /**
     * Create a service header populated with a principal list encoded as 
     * Subject embedded PasswordCredential. 
     * 
     * @param subject
     * 
     * @return a new service header
     */
    public static ServiceHeader toServiceHeader(
        Subject subject
    ){
        PasswordCredential credential = getCredential(subject);
        return credential == null ? new ServiceHeader() : toServiceHeader(
            credential.getUserName(), 
            new String(credential.getPassword())
        );            
    }

    /**
     * Create a service header populated with a principal list encoded as 
     * user name.
     * 
     * @param connectionUsername a principal or the stringified principal list 
     * @param connectionPassword the correlation id
     * 
     * @return a principal array
     */
    public static ServiceHeader toServiceHeader(
        String connectionUsername, 
        String connectionPassword
    ){
        return new ServiceHeader(
            Arrays.asList(toPrincipalArray(connectionUsername)),
            null // requestedAt
        );
    }
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ){
        return Records.getRecordFactory().asMappedRecord(
		    ServiceHeader.class.getName(),
		    null, // short description
		    TO_STRING_CONTENT,
		    new Object[]{
		        this.principalChain,
		        this.requestedAt
		    }
		).toString();
    }

}

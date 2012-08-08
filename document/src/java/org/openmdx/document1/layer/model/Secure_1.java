/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Secure_1.java,v 1.3 2005/03/18 12:40:00 hburger Exp $
 * Description: Secure Model Layer for org::openmdx::document1
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/03/18 12:40:00 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.document1.layer.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;


/**
 * Secure Model Layer for org::openmdx::document1
 * <p>
 * This plug-in relies on the fact that Strict_1 will remove all attributes in 
 * case of a NO_ATTRIBUTES request.
 */
public class Secure_1
    extends Insecure_1 
{
    
    /**
     * Retrieve the access tokens.
     * <p>
     * This method and its way to define the access tokens may be overridden 
     * by a subclass. Security can be disabled by returning <code>null</code>, 
     * e.g for administrators.
     * @param serviceHeader the request's service header
     * 
     * @return the access token or null to disable security
     * @throws ServiceException 
     */
    protected Collection getAccessTokens(
        ServiceHeader serviceHeader
    ) throws ServiceException{
        Collection principalChain = serviceHeader.getPrincipalChain();
        if(principalChain == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ASSERTION_FAILURE,
            new BasicException.Parameter[]{
                new BasicException.Parameter("principalChain", principalChain)
            },
            "The principal chain may be empty but must never be null"
        );
        return principalChain;
    }
   
    /**
     * Calculate the intersection of two sets.
     * <p>
     * Do not make this method public due to ist <code>null</ccode> handling.
     * 
     * @param left 
     * @param right
     * 
     * @return the intersection of the two sets;
     *         or an empty set if either of them iis null.
     */
    protected static final Set intersection(
        Collection left, Collection right
    ){
        if(
            left == null ||
            right == null
        ) return Collections.EMPTY_SET;
        Set set = new HashSet(left);
        set.retainAll(right);
        return set;
    }

    /**
     * This method adds a permission filter to a request.
     * <p>
     * I may be overrridden by a subclass, e.g. to avoid permission 
     * checks for administrators.
     * 
     * @param header
     * @param request
     * @param grantName
     * @param accessTokens
     * 
     * @throws ServiceException 
     */
    protected void filterByPermission(
        ServiceHeader header,
        DataproviderRequest request,
        String grantName
    ) throws ServiceException{
        Collection accessTokens = getAccessTokens(header);
        request.addAttributeFilterProperty(
            new FilterProperty(
                Quantors.THERE_EXISTS,
                grantName,
                FilterOperators.IS_IN,
                accessTokens.toArray()
            )
        );
    }           

    /**
     * Assert a specific permission
     * <p>
     * This method may be overrridden by a subclass,
     * e.g. to avoid permission checks for administrators.
     * @param object
     * @param message
     * @param grantName
     * 
     * @throws ServiceException AUTHORIZATION_FAILURE if permission is denied
     */
    protected void assertPermission(
        ServiceHeader header,
        DataproviderObject_1_0 object,
        String[] grantNames,
        String message
    ) throws ServiceException{
        Collection accessTokens = getAccessTokens(header);
        //
        // Return if access is granted
        //
        for(
            int i = 0;
            i < grantNames.length;
            i++
        ) if(
            !intersection(accessTokens, object.getValues(grantNames[i])).isEmpty()
        ) return;
        //
        // Throw an exception if access is denied
        //
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.AUTHORIZATION_FAILURE,
            new BasicException.Parameter[]{
                new BasicException.Parameter("path", object.path()),
                new BasicException.Parameter("grantNames", grantNames),
                new BasicException.Parameter("accessTokens", accessTokens)
            },
            message
        );
    }
    
    /**
     * Assert a specific permission
     * 
     * @param header
     * @param path
     * @param grantNames
     * @param message
     * @throws ServiceException
     */
    private void assertPermission(
        ServiceHeader header,   
        Path path,
        String[] grantNames,
        String message
    ) throws ServiceException{
        assertPermission(  
            header,
            getDelegation().get(
                header,
                new DataproviderRequest(
                    new DataproviderObject(path),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                    null
                )
            ).getObject(),
            grantNames,
            message
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.document1.layer.model.Unsecure_1#unmarshal(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest, org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply)
     */
    protected DataproviderReply unmarshal(
        ServiceHeader header, 
        DataproviderRequest request, 
        DataproviderReply reply
    ) throws ServiceException {
        DataproviderObject[] objects = super.unmarshal(header, request, reply).getObjects();
        //
        // This plug-in relies on the fact that Strict_1 will remove all 
        // attributes in case of a NO_ATTRIBUTES request.
        //
        if(request.attributeSelector() != AttributeSelectors.NO_ATTRIBUTES) {
            for(
                int i = 0;
                i < objects.length;
                i++
            ){
                Path path = objects[i].path();
                if(
                    path.isLike(NODE_OBJECT_PATTERN) || 
                    path.isLike(CABINET_OBJECT_PATTERN) || 
                    path.isLike(SEGMENT_OBJECT_PATTERN)
                ) assertPermission(
                    header,
                    objects[i],
                    READ_PERMISSION, 
                    "All requests selecting some attributes require read permission to the given object"
                );                        
            }
        }
        return reply;
    }

    /**
     * This method assumes that all ExtentCapable objects of this segment are
     * sub-classes of DocumentObject.
     */
    public DataproviderReply find(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        Path path = request.path();
        if(                
            path.isLike(NODE_REFERENCE_PATTERN) ||
            path.isLike(CABINET_REFERENCE_PATTERN) || 
            path.isLike(SEGMENT_REFERENCE_PATTERN) ||
            path.isLike(EXTENT_REFERENCE_PATTERN)
        ){  //  
            // DocumentObject access
            //
            filterByPermission(
               header, 
               request,
               "readGrantedTo"
            );
        } else if (
            path.isLike(REVISION_REFERENCE_PATTERN)
        ){  //
            // Test Revision access
            //
            assertPermission(
                header,
                path.getParent(),
                READ_PERMISSION,
                "Accessing a Revision requires read permission for its Resource"
            );
        }
        return super.find(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.document1.layer.model.Insecure_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply get(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        Path path = request.path();
        if (
            path.isLike(REVISION_OBJECT_PATTERN)
        ){  //
            // Test Revision access
            //
            assertPermission(
                header,
                path.getPrefix(path.size()-2),
                READ_PERMISSION,
                "Accessing a Revision requires read permission for its Resource"
            );
        }
        return super.get(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.document1.layer.model.Insecure_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply create(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        Path path = request.path();
        if(                
            path.isLike(REVISION_OBJECT_PATTERN) ||
            (path.isLike(NODE_OBJECT_PATTERN) && request.object().values("uri").isEmpty()) ||
            path.isLike(CABINET_OBJECT_PATTERN)
        ){
            assertPermission(
                header,
                path.getPrefix(path.size()-2),
                MODIFY_PERMISSION,
                "Adding an object requires modification permission for its parent object"
            );
        } else if (path.isLike(NODE_OBJECT_PATTERN)) {
            assertPermission(  
                header,
                path.getPrefix(path.size()-2),
                CREATE_PERMISSION,
                "Adding a Node requires create or modification permission for the Cabinet"
            );
        }    
        return super.create(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.document1.layer.model.Insecure_1#modify(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply modify(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        Path path = request.path();
        if(                
            path.isLike(NODE_OBJECT_PATTERN) ||
            path.isLike(CABINET_OBJECT_PATTERN) || 
            path.isLike(SEGMENT_OBJECT_PATTERN) 
        ) assertPermission(  
            header,
            getBeforeImage(header, request),
            MODIFY_PERMISSION,
            "Object modification permission denied"
        );
        return super.modify(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.document1.layer.model.Insecure_1#remove(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply remove(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        Path path = request.path();
        if(                
            path.isLike(NODE_OBJECT_PATTERN) ||
            path.isLike(CABINET_OBJECT_PATTERN) || 
            path.isLike(SEGMENT_OBJECT_PATTERN) 
        ) assertPermission(  
            header,
            getBeforeImage(header, request),
            DELETE_PERMISSION,
            "Object modification permission denied"
        );
        return super.remove(header, request);
    }

    /* (non-Javadoc)
     * @see org.openmdx.document1.layer.model.Insecure_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply replace(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        Path path = request.path();
        if(                
            path.isLike(NODE_OBJECT_PATTERN) ||
            path.isLike(CABINET_OBJECT_PATTERN) || 
            path.isLike(SEGMENT_OBJECT_PATTERN) 
        ) assertPermission(  
            header,
            getBeforeImage(header, request),
            MODIFY_PERMISSION,
            "Object modification permission denied"
        );
        return super.replace(header, request);
    }

    private static final String[] DELETE_PERMISSION = new String[]{"deleteGrantedTo"};
    private static final String[] READ_PERMISSION = new String[]{"readGrantedTo"};
    private static final String[] MODIFY_PERMISSION = new String[]{"modifyGrantedTo"};
    private static final String[] LINK_PERMISSION = new String[]{"linkGrantedTo"};
    private static final String[] CREATE_PERMISSION = new String[]{"modifyGrantedTo", "createGrantedTo"};
    
}

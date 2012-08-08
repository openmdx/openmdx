/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Insecure_1.java,v 1.5 2005/03/18 00:09:34 hburger Exp $
 * Description: Unsecure Model Layer for org::openmdx::document1
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/03/18 00:09:34 $
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

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.OffsetArrayList;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.layer.model.Standard_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;


/**
 * Insecure Model Layer for org::openmdx::document1
 */
public class Insecure_1
    extends Standard_1 
{

    private static final String WILDCARD = ":*";
    protected static final Path SEGMENT_REFERENCE_PATTERN = new Path(
        new String[]{"org:openmdx:document1","provider",WILDCARD,"segment"}
    );
    protected static final Path SEGMENT_OBJECT_PATTERN = SEGMENT_REFERENCE_PATTERN.getChild(WILDCARD);
    protected static final Path CABINET_REFERENCE_PATTERN = SEGMENT_OBJECT_PATTERN.getChild("cabinet");    
    protected static final Path CABINET_OBJECT_PATTERN = CABINET_REFERENCE_PATTERN.getChild(WILDCARD);
    protected static final Path NODE_REFERENCE_PATTERN = CABINET_OBJECT_PATTERN.getChild("node");
    protected static final Path NODE_OBJECT_PATTERN = NODE_REFERENCE_PATTERN.getChild(WILDCARD);
    protected static final Path REVISION_REFERENCE_PATTERN = NODE_OBJECT_PATTERN.getChild("revision");
    protected static final Path REVISION_OBJECT_PATTERN = REVISION_REFERENCE_PATTERN.getChild(WILDCARD);
    protected static final Path EXTENT_REFERENCE_PATTERN = SEGMENT_OBJECT_PATTERN.getChild("extent");
    
    /**
     * A large object's length is passed as the feature's second value.
     */
    private static final int LARGE_OBJECT_LENGTH_INDEX = 1;
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#create(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply create(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        if(request.path().isLike(REVISION_OBJECT_PATTERN)){
            DataproviderObject resource = getDelegation().get(
                header,
                new DataproviderRequest(
                    new DataproviderObject(request.path().getPrefix(NODE_OBJECT_PATTERN.size())),
                    DataproviderOperations.OBJECT_RETRIEVAL,
                    AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES,
                    null
                )
            ).getObject();
            resource.clearValues("headRevision").add(request.path());
            getDelegation().replace(
                header,
                new DataproviderRequest(
                    resource,
                    DataproviderOperations.OBJECT_REPLACEMENT,
                    AttributeSelectors.NO_ATTRIBUTES,
                    null                            
                )
            );
        }
        return unmarshal(header, request, super.create(header, marshal(header, request)));
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#remove(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply remove(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        return unmarshal(header, request, super.remove(header, marshal(header, request)));
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#find(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply find(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        return unmarshal(header, request, super.find(header, marshal(header, request)));
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply get(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        return unmarshal(header, request, super.get(header, marshal(header, request)));
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#modify(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply modify(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        return unmarshal(header, request, super.modify(header, marshal(header, request)));
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#replace(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply replace(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        return unmarshal(header, request, super.replace(header, marshal(header, request)));
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#set(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply set(ServiceHeader header, DataproviderRequest request) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            new BasicException.Parameter[]{
                new BasicException.Parameter("path", request.path())
            },             
            "Set operations should have been intercepted by a higher layer"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#startPublishing(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    public DataproviderReply startPublishing(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            new BasicException.Parameter[]{
                new BasicException.Parameter("path", request.path())
            },             
            "Object monitoring not supported by this dataprovider"
        );
    }

    /**
     * Unmarshal the reply
     * 
     * @param header 
     * @param request 
     * @param reply
     * 
     * @return the model conform reply
     * @throws ServiceException 
     */
    protected DataproviderReply unmarshal(
        ServiceHeader header, 
        DataproviderRequest request, 
        DataproviderReply reply
    ) throws ServiceException{
        DataproviderObject[] objects = reply.getObjects(); 
        for(
            int i = 0;
            i < objects.length;
            i++
        ){
            DataproviderObject object = objects[i];
            //
            // Derived attributes, etc.
            //
            if(object.path().isLike(REVISION_OBJECT_PATTERN)) {
                //
                // Process Revision objects
                //
                SparseList length = object.getValues("value");
                if(length != null) {
                    object.attributeNames().remove("value");
                    object.values("length").set(0, length.get(LARGE_OBJECT_LENGTH_INDEX));
                }
            }
        }
        return reply;
    }

    /**
     * To allow a sub-class to intercept the parent folder retrieval
     * 
     * @param header
     * @param request
     * 
     * @return the parent folders
     *  
     * @throws ServiceException
     */
    protected DataproviderObject[] getParentFolders(
         ServiceHeader header,
         DataproviderRequest request
    ) throws ServiceException{
         return super.find(header, request).getObjects();
    }
    
    /**
     * Marshal the request
     * 
     * @param header 
     * @param request
     * 
     * @return the marshalled request
     * 
     * @throws ServiceException 
     */
    protected DataproviderRequest marshal(
        ServiceHeader header, 
        DataproviderRequest request
    ) throws ServiceException{
        Path path = request.path();
        switch(request.operation()) {
            case DataproviderOperations.OBJECT_RETRIEVAL:
            case DataproviderOperations.ITERATION_START:
            case DataproviderOperations.ITERATION_CONTINUATION:
                //
                // Derived attributes, etc.
                //
                AttributeSpecifier[] attributeSpecifiers = null;
                FilterProperty[] attributeFilter = null;
                if(
                    path.isLike(REVISION_REFERENCE_PATTERN) ||
                    path.isLike(REVISION_OBJECT_PATTERN)
                ){
                    //
                    // Process Revision objects
                    //
                    if(request.attributeSpecifier("value") != null) throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("path", path),
                            new BasicException.Parameter("feature", "value")
                        },
                        "Large objects must be retrieved into an OutputStream or a Writer"
                    );
                    length: switch(request.attributeSelector()){
                        case AttributeSelectors.NO_ATTRIBUTES:
                            if(request.attributeSpecifier().length != 0){
                                attributeSpecifiers = new AttributeSpecifier[]{};
                            }
                            break length;
                        case AttributeSelectors.ALL_ATTRIBUTES:
                        case AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES:
                            if(request.attributeSpecifier("length") == null){
                                attributeSpecifiers = new AttributeSpecifier[
                                    request.attributeSpecifier().length + 1                                             
                                ];
                                System.arraycopy(
                                    request.attributeSpecifier(), 0, 
                                    attributeSpecifiers, 0, 
                                    attributeSpecifiers.length - 1
                                );
                                attributeSpecifiers[attributeSpecifiers.length - 1] = new AttributeSpecifier(
                                    "value",
                                    LARGE_OBJECT_LENGTH_INDEX,
                                    1,
                                    Directions.ASCENDING
                                );
                                break length;
                            }    
                        case AttributeSelectors.SPECIFIED_AND_SYSTEM_ATTRIBUTES:
                            if(request.attributeSpecifier("length") != null) {
                                attributeSpecifiers = new AttributeSpecifier[request.attributeSpecifier().length];
                                for(
                                    int i = 0;
                                    i < attributeSpecifiers.length;
                                    i++
                                ) attributeSpecifiers[i] = "length".equals(attributeSpecifiers[i].name()) ?
                                    new AttributeSpecifier(
                                        "value",
                                        LARGE_OBJECT_LENGTH_INDEX,
                                        1,
                                        Directions.ASCENDING
                                    ) :
                                    request.attributeSpecifier()[i];
                            }
                            break length;
                    }
                }
                return attributeFilter == null && attributeSpecifiers == null ? request : new DataproviderRequest(
                    request,
                    request.object(),
                    request.operation(),
                    attributeFilter == null ? request.attributeFilter() : attributeFilter,
                    request.position(),
                    request.size(),
                    request.direction(),
                    AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                    attributeSpecifiers == null ? request.attributeSpecifier() : attributeSpecifiers
                );
            case DataproviderOperations.OBJECT_REPLACEMENT:
            case DataproviderOperations.OBJECT_MODIFICATION:
                //
                // Protect unmodifiable objects
                //
                if (path.isLike(REVISION_OBJECT_PATTERN)){
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("path", path),
                        },
                        "A revision can't be modified"
                    ); 
                }
            case DataproviderOperations.OBJECT_CREATION:
                //
                // Frozen attributes, etc.
                //
                if(path.isLike(NODE_OBJECT_PATTERN)){
                    //
                    // Process Node Requests
                    //
                    DataproviderObject node = request.object();
                    SparseList uriFeature = node.getValues("uri");
                    if(uriFeature != null) {
                        //
                        // Derive parent from uri
                        //
                        List uriCollection = uriFeature.population();
                        if(
                            uriCollection.size() > new HashSet(uriCollection).size()
                        ) throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("path", path),
                                new BasicException.Parameter("uri", uriFeature)
                            },
                            "Duplicate uri entry"
                        );
                        DataproviderObject[] candidates = super.find(
                            header, 
                            new DataproviderRequest(
                                new DataproviderObject(node.path().getParent()),
                                DataproviderOperations.ITERATION_START,
                                new FilterProperty[]{
                                    new FilterProperty(
                                        Quantors.THERE_EXISTS,
                                        "uri",
                                        FilterOperators.IS_IN,
                                        uriCollection.toArray()
                                    )
                                },
                                0,
                                Integer.MAX_VALUE,
                                Directions.ASCENDING,
                                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                                null
                            )
                        ).getObjects();
                        for(
                           int i = 0;
                           i < candidates.length;
                           i++
                        ) if (
                            !path.equals(candidates[i].path())
                        ) throw new ServiceException (
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter[]{
                                new BasicException.Parameter("path", path),
                                new BasicException.Parameter("uri", uriFeature),
                                new BasicException.Parameter("conflictingPath", candidates[i].path()),
                                new BasicException.Parameter("conflictingUri", candidates[i].getValues("uri"))
                            },
                            "Conflict between the given uri set and existing ones"
                        );              
                        SparseList parentCollection = new OffsetArrayList();
                        for(
                            ListIterator i = uriFeature.populationIterator();
                            i.hasNext();
                        ){
                            int j = i.nextIndex();                        
                            String uri = i.next().toString();
                            int k = uri.lastIndexOf('/');
                            if(k >= 0) parentCollection.set(j, uri.substring(0, k));
                        }
                        DataproviderRequest parentRequest = new DataproviderRequest(
                            new DataproviderObject(node.path().getParent()),
                            DataproviderOperations.ITERATION_START,
                            new FilterProperty[]{
                                new FilterProperty(
                                    Quantors.THERE_EXISTS,
                                    "uri",
                                    FilterOperators.IS_IN,
                                    new HashSet(parentCollection.population()).toArray()
                                )
                            },
                            0,
                            Integer.MAX_VALUE,
                            Directions.ASCENDING,
                            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                            null
                        );
                        filterByPermission(
                            header,
                            parentRequest,
                            "linkGrantedTo"
                        );
                        DataproviderObject[] parents = super.find(
                            header, 
                            parentRequest
                        ).getObjects();
                        SparseList parentFeature = node.clearValues("parent");
                        parents: for(
                            ListIterator i = parentCollection.populationIterator();
                            i.hasNext();
                        ){
                            int j = i.nextIndex();
                            Object parent = i.next();
                            for(
                                int k = 0;
                                k < parents.length;
                                k++
                            ) if (parents[k].values("uri").contains(parent)) {
                                parentFeature.set(j, parents[k].path());
                                continue parents;
                            }
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter[]{
                                    new BasicException.Parameter("path", path),
                                    new BasicException.Parameter("uri", uriFeature.get(j)),
                                    new BasicException.Parameter("parent", parent)
                                },
                                "Parent node not found"
                            );
                        }
                    }
                }
                return request;
            case DataproviderOperations.OBJECT_REMOVAL:
                if(path.isLike(NODE_OBJECT_PATTERN)){
                    //
                    // Assert referential integrity
                    //
                    if(
                        super.find(
                            header, 
                            new DataproviderRequest(
                                new DataproviderObject(path.getParent()),
                                DataproviderOperations.ITERATION_START,
                                new FilterProperty[]{
                                    new FilterProperty(
                                        Quantors.THERE_EXISTS,
                                        "parent",
                                        FilterOperators.IS_IN,
                                        new Path[]{path}
                                    )
                                },
                                0,
                                Integer.MAX_VALUE,
                                Directions.ASCENDING,
                                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                                null
                            )
                        ).getObjects().length > 0
                    ) throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CARDINALITY,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("path", path),
                        },
                        "A node can't be deleted as long as it has children"
                    );
                } else if (path.isLike(REVISION_OBJECT_PATTERN)){
                    //
                    // Protect unmodifiable objects
                    //
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("path", path),
                        },
                        "A revision can't be removed"
                    ); 
                }
                return request;
            default:
                return request;
        }
    }

    /**
     * This method adds a permission filter to a request.
     * <p>
     * It is overrridden by a subclass to add security.
     * 
     * @param header TODO
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
    }           

    /**
     * Assert a specific permission
     * <p>
     * It is overrridden by a subclass to add security.
     * @param header TODO
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
    }

}

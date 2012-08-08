/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Provider_1.java,v 1.30 2008/12/15 03:15:37 hburger Exp $
 * Description: Dataprovider Adapter: Provider
 * Revision:    $Revision: 1.30 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.adapter;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.PathOnlyMarshaller;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_3;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_3;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Dataprovider Adapter
 * Provider_1_0 Implementation
 */
//---------------------------------------------------------------------------
public class Provider_1
    implements Provider_1_3, Serializable 
{

    /**
     * Constructs a new provider.
     * 
     * @param channel SPICE/2 RequestCollection which is used as interaction channel.
     * @param transactionPolicyIsNew true, if the Provider (and associated RequestCollection)
     *         has the transaction policy 'isNew'.
     */
    public Provider_1(
        RequestCollection channel,
        boolean transactionPolicyIsNew
    ) throws ServiceException {
        this(
            channel, 
            transactionPolicyIsNew, // transactionPolicyIsNew
            transactionPolicyIsNew // persistentNewObjectBecomeTransientUponRollback
        );
    }

    /**
     * Constructs a new provider.
     * 
     * @param channel SPICE/2 RequestCollection which is used as interaction channel.
     * @param transactionPolicyIsNew true, if the Provider (and associated RequestCollection)
     *         has the transaction policy 'isNew'.
     */
    public Provider_1(
        RequestCollection channel,
        boolean transactionPolicyIsNew,
        boolean persistentNewObjectBecomeTransientUponRollback
    ) throws ServiceException {
        this.unitOfWorkChannel = channel;
        this.shareableChannel = (RequestCollection)channel.clone();
        this.transactionPolicyIsNew = transactionPolicyIsNew;
        this.persistentNewObjectsBecomeTransientUponRollback = persistentNewObjectBecomeTransientUponRollback;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3906645319247737140L;

    /**
     * 
     */
    private RequestCollection unitOfWorkChannel, shareableChannel;  

    /**
     * 
     */
    private final boolean transactionPolicyIsNew;

    /**
     * 
     */
    private final boolean persistentNewObjectsBecomeTransientUponRollback;

    /**
     * 
     */
    private boolean completing;

    /**
     * 
     */
    private Map<List<Object>,Map<Path,FindResult>> extents = 
        new HashMap<List<Object>,Map<Path,FindResult>>();

    /**
     * The fallback provider forwards all requests to the delegate
     */
    private static final Path[] UNIVERSE = new Path[]{
        new Path("")
    };

    private static final String[] FINAL_CLASSES = {
        "org:openmdx:base:Authority",
        "org:openmdx:base:Provider"
    };

    /**
     * 
     */
    static final Path EXTENT_PATTERN = new Path(
        new String[]{":*","provider",":*","segment",":*","extent"}
    );

    /**
     * A provider's exposed paths define which objects are provided by it.
     *
     * @return  the provider's exposed paths
     */
    public Path[] getExposedPaths(
    ) throws ServiceException {
        return UNIVERSE;
    }

    /**
     *
     */
    public boolean isTransactionPolicyIsNew(
    ){
        return this.transactionPolicyIsNew;
    }

    private RequestCollection getChannel(
    ){
        return this.completing ? 
            this.unitOfWorkChannel : 
                this.shareableChannel; 
    }


    //------------------------------------------------------------------------
    // Life cycle management
    //------------------------------------------------------------------------

    /**
     * Close the connection.
     * <p>
     * After the close method completes, all methods on the Provider_1_0
     * instance except isClosed throw a ILLEGAL_STATE RuntimeServiceException.
     */
    public void close(
    ){
        this.shareableChannel = this.unitOfWorkChannel = null;
    }

    /**
     * This method tells whether the manager is closed.
     * <p> 
     * The isClosed method returns false upon construction of the Provider_1_0
     * instance. It returns true only after the close method completes 
     * successfully. 
     */
    public boolean isClosed(
    ){
        return getChannel() == null;
    }


    //------------------------------------------------------------------------
    // Query
    //------------------------------------------------------------------------

    /**
     * Retrieve a list containing the identities of all objects for which the
     * <code>referenceFilter</code> as well as the <code>attributeFilter</code>
     * evaluate to true.
     *
     * @param       referenceFilter
     *              an object may be included into the result sets only if it
     *              is accessable through the path passed as
     *              <code>referenceFilter</code>
     * @param       attributeFilter
     *              an object may be included into the result sets only if all
     *              the filter properties evaluate to true if applied to it; 
     *              this argument may be <code>null</code>.
     * @param       attributeSpecifier
     *              An array of attribute specifiers
     * @param       manager
     *              manager for pre-fetch support
     * 
     * @return  a list of paths
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given reference filter is reachable.
     */
    public List<Object> find(
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        AttributeSpecifier[] attributeSpecifier,
        Manager_1_0 manager
    ) throws ServiceException {
        List<Object> keys = null;
        boolean useExtent = referenceFilter.isLike(EXTENT_PATTERN);
        if(useExtent) { 
            if(
                attributeFilter == null || 
                attributeFilter.length == 0
            ) {
                return Collections.emptyList();
            }
            keys = getKey(attributeFilter, attributeSpecifier, 1);
        } else {
            keys = getKey(attributeFilter, attributeSpecifier, 0);
            Path identities = referenceFilter.getChild(":*");
            Map<Path,FindResult> mappings = keys == null ? null : this.extents.get(keys);
            if(mappings != null) {
                for (Map.Entry<Path,FindResult> e : mappings.entrySet()) {
                    if(identities.isLike(e.getKey())){
                        List<Object> result = new ArrayList<Object> ();
                        for(Object j : e.getValue()) {
                            Path candidate = (Path)j;
                            if(candidate.isLike(identities)) {
                                result.add(candidate);
                            }
                        }
                        SysLog.detail("Return cached paths", identities);
                        return result;
                    }
                }
            }
        }
        FindResult findResult = new FindResult(
            getChannel(), 
            referenceFilter,
            PathOnlyMarshaller.getInstance(),
            manager
        );
        getChannel().addFindRequest(
            new Path(referenceFilter),
            attributeFilter,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            attributeSpecifier,
            0,
            Integer.MAX_VALUE,
            Directions.ASCENDING,
            findResult          
        );
        if(useExtent){
            if(keys == null) return findResult;
            List<?> identities = (List<?>)keys.remove(0);
            Map<Path,FindResult> mappings = this.extents.get(keys);
            if(mappings == null) this.extents.put(
                keys,
                mappings = new HashMap<Path,FindResult>()
            );
            for(Object identity : identities) {
                mappings.put(
                    new Path(identity.toString()), 
                    findResult
                );
            }
        }
        return findResult;
    }

    /**
     * 
     * @param referenceFilter
     * @param attributeFilter
     * @param attributeSpecifier
     */
    private List<Object> getKey(
        FilterProperty[] attributeFilter,
        AttributeSpecifier[] attributeSpecifier,
        int identityCount 
    ){
        List<Object> key = new ArrayList<Object>();
        Set<FilterProperty> filterProperties = new HashSet<FilterProperty>();
        if(attributeFilter != null) for(
                int i = 0; 
                i < attributeFilter.length; 
                i++
        ){
            if(SystemAttributes.OBJECT_IDENTITY.equals(attributeFilter[i].name())){
                if(
                        attributeFilter[i].quantor() != Quantors.THERE_EXISTS ||
                        attributeFilter[i].operator() != FilterOperators.IS_LIKE 
                ) return null;
                key.add(attributeFilter[i].values());
            } else {
                filterProperties.add(attributeFilter[i]);
            } 
        }
        if(key.size() != identityCount) return null;
        key.add(filterProperties);
        if(attributeSpecifier != null) for(
                int i = 0; 
                i < attributeSpecifier.length;
                i++
        ) if (
                attributeSpecifier[i].order() != Orders.ANY
        ) key.add(attributeSpecifier[i]);
        return key;
    }

    /**
     * Reconstruct the list.
     *
     * @param   referenceFilter
     *          an object may be included into the result sets only if it
     *          is accessable through the path passed as
     *          <code>referenceFilter</code>
     * @param   criteria
     *          Criteria to be used to  reconstruct the list
     * @param   attributeSpecifier
     *          An array of attribute specifiers
     * 
     * @return  a list of paths
     *
     * @exception   ServiceException  NOT_SUPPORTED
     *              if no provider for the given reference filter is reachable.
     */
    public List<Object> reconstruct(
        Path referenceFilter,
        Manager_1_0 manager,
        InputStream criteria
    ) throws ServiceException {
        return new FindResult(
            this.shareableChannel, 
            referenceFilter,
            PathOnlyMarshaller.getInstance(),
            manager,
            criteria
        );
    }


    /**
     * Retrieve an object.
     * 
     * @param   accessPath
     *          the object's access path
     * @param   requiredSet
     * @param   manager
     *          manager for pre-fetch support
     *
     * @return  a map containing the object's default fetch group;
     *          or null if the object doesn't exist.
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    public MappedRecord getDefaultFetchGroup(
        Path accessPath,
        Set<String> requiredSet,
        Manager_1_0 manager
    ) throws ServiceException {
        Path path = new Path(accessPath);
        int level = path.size() / 2;
        //
        // final classes
        //
        if(level < FINAL_CLASSES.length){
            DataproviderObject_1_0 object=new DataproviderObject(
                path
            );
            object.values(
                SystemAttributes.OBJECT_CLASS
            ).set(
                0,
                FINAL_CLASSES[level]
            );
            return Marshaller.toMappedRecord(
                object
            );
        } else {
            GetResult object=new GetResult(
                accessPath,
                manager
            );
            AttributeSpecifier[] attributeSpecifiers = null;
            if(requiredSet != null) {
                attributeSpecifiers = new AttributeSpecifier[
                                                             requiredSet.size()
                                                             ];
                int j = 0;
                for(String attributeName : requiredSet) {
                    attributeSpecifiers[j++]=new AttributeSpecifier(attributeName);
                }
            }
            getChannel().addGetRequest(
                path,
                AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
                attributeSpecifiers,
                object
            );
            return Marshaller.toMappedRecord(object);
        }
    }

    /**
     * Retrieve an attribute.
     *
     * @param   accessPath
     *          the object's identity
     * @param   name
     *          the attribute name
     * @param   target
     *          where the atttibute should be stored
     * @param   manager
     *          Manager to deliver pre-fetched objects
     * 
     * @return  the specified feature
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    @SuppressWarnings("unchecked")
    public void getAttribute(
        Path accessPath,
        String name,
        Map target,
        Manager_1_0 manager
    ) throws ServiceException {
        Path path = new Path(accessPath);
        GetResult source=new GetResult(
            accessPath,
            manager
        );
        getChannel().addGetRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            new AttributeSpecifier[]{
                new AttributeSpecifier(name)
            },
            source
        );
        target.put(
            name,
            source.getValues(name)
        );
        for(String attributeName : source.attributeNames()) {
            if(
                !target.containsKey(attributeName) &&
                !SystemAttributes.OBJECT_CLASS.equals(attributeName)
            ) {
                target.put(
                    attributeName, 
                    source.getValues(attributeName) 
                );
            }
        }
    }

    /**
     * Retrieve part of an attribute
     *
     * @param   accessPath
     *          the object's identity
     * @param   name
     *          the attribute name
     * @param   position
     *          start position of the part to be retrieved
     * @param   capacity
     *          maximal number of elements to be returned
     * 
     * @return  a partof the attribute
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     *            
     * @deprecated as optional objects can't be accepted
     */
    public Object getAttributePart(
        Path accessPath,
        String name,
        int position,
        int capacity
    ) throws ServiceException {
        Path path = new Path(accessPath);
        DataproviderObject_1_0 source = this.shareableChannel.addGetRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            new AttributeSpecifier[]{
                new AttributeSpecifier(
                    name,
                    position,
                    capacity,
                    Directions.ASCENDING
                )
            }
        );
        return source.values(name).get(position);
    }

    /**
     * Invokes an operation on the specified object.
     *
     * @param   accessPath
     *          the object's 
     * @param   name
     *          the operation name
     * @param   arguments
     *          the operation arguments
     *
     * @return  the operation's return values
     *
     * @exception ServiceException  NOT_SUPPORTED
     *            if no provider for the given path is reachable.
     */
    public MappedRecord invokeOperation(
        Path accessPath,
        String name,
        MappedRecord arguments
    ) throws ServiceException {
        return Marshaller.toOperationResult(
            getChannel().addOperationRequest(
                Marshaller.toDataproviderObject(
                    accessPath.getChild(name),
                    arguments
                )
            )
        );
    }

    //------------------------------------------------------------------------
    // Unit of work
    //------------------------------------------------------------------------

    /**
     * Create an object.
     *
     * @param accessPath
     *        the  of the object
     * @param attributes
     *        the object's attributes
     * @param   manager
     *          manager for pre-fetch support
     */
    public void createObject(
        Path accessPath,
        MappedRecord attributes,
        Manager_1_0 manager
    ) throws ServiceException {
        Path path = new Path(accessPath);
        CreateResult target = new CreateResult(
            accessPath,
            manager
        );
        this.unitOfWorkChannel.addCreateRequest(
            Marshaller.toDataproviderObject(
                path, 
                attributes
            ),
            AttributeSelectors.NO_ATTRIBUTES, //.... Copy back
            null,
            target
        );
    }  

    /**
     * Modify an object.
     *
     * @param accessPath
     *        the  of the object
     * @param attributes
     *        the attributes to be replaced
     * @param   manager
     *          manager for pre-fetch support
     */
    public void editObject(
        Path accessPath,
        MappedRecord attributes,
        Manager_1_0 manager
    ) throws ServiceException {
        Path path = new Path(accessPath);
        GetResult target = new GetResult(
            accessPath,
            manager
        );
        this.unitOfWorkChannel.addReplaceRequest(
            Marshaller.toDataproviderObject(
                path, 
                attributes
            ),
            AttributeSelectors.NO_ATTRIBUTES, //.... Copy back
            null,
            target
        );
    }  

    /**
     * Remove an object.
     *
     * @param accessPath
     *        the identity of the object
     * @param   manager
     *          Manager to invalidate not found objects
     */
    public void removeObject(
        Path accessPath,
        Manager_1_0 manager
    ) throws ServiceException {
        Path path = new Path(accessPath);
        RemoveResult target = new RemoveResult(
            path,
            manager
        );
        this.unitOfWorkChannel.addRemoveRequest(
            path,
            AttributeSelectors.SPECIFIED_AND_TYPICAL_ATTRIBUTES,
            null,
            target
        );
    }  


    //------------------------------------------------------------------------
    // Implements Provider_1_1
    //------------------------------------------------------------------------

    /**
     * Tells whether persistent-new objects become transient.
     * on rollback.
     * 
     * @return <code>true<code> if persistent-new objects become transient
     * on rollback.
     */
    public boolean doPeristentNewObjectsBecomeTransientUponRollback(
    ) {
        return this.persistentNewObjectsBecomeTransientUponRollback;
    }

    
    //------------------------------------------------------------------------
    // Implements Provider_1_3
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_3#getPrincipalChain()
     */
    public List<String> getPrincipalChain(
    ) {
        return this.unitOfWorkChannel.getPrincipalChain();
    }


    //------------------------------------------------------------------------
    // Implements Synchronization_1_0
    //------------------------------------------------------------------------

    /**
     * The afterBegin method notifies a provider or unit-of-work that a new
     * unit of work has started, and that the subsequent business methods on
     * the instance will be invoked in the context of the transaction. 
     */
    public void afterBegin(
    ) throws ServiceException {
        this.unitOfWorkChannel.clear();
        this.extents.clear();
    } 

    /**
     * The beforeCompletion method notifies a provider or plug-in that a
     * unit of work is about to be committed 
     */
    public void beforeCompletion(
    ) throws ServiceException {
        this.unitOfWorkChannel.beginUnitOfWork(this.transactionPolicyIsNew);
        this.completing = true;
    } 

    /**
     * TO DO: Replace by a more appropriate method name
     * <p>
     * The afterCompletion method notifies a provider that a unit of
     * work is complete.
     *
     * @param   preparing
     *          defines whether status of the unit of work should stay
     *          PREPARING or change to ROLLING_BACK.
     */
    public void afterCompletion(
        boolean preparing
    ) throws ServiceException {
        if(preparing){
            try {
                this.unitOfWorkChannel.endUnitOfWork();
            } catch (Exception exception) {
                throw transactionPolicyIsNew ? new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ROLLBACK,
                    "Transactional unit of work rolled back"
                ) : new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ABORT,
                    "Non-transactional or container-managed unit of work aborted"
                );
            } finally {
                this.completing = false;
            }
        } else {
            this.unitOfWorkChannel.clear();
            this.completing = false;
        }
    }


    //------------------------------------------------------------------------
    // Implements Connection_1Factory
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1Factory#getConnection()
     */
    public Connection_1_3 getConnection(
        boolean containerManagedUnitOfWork
    ) throws ServiceException {
        return new Connection_1(
            new Provider_1(
                (RequestCollection)this.shareableChannel.clone(),
                this.transactionPolicyIsNew
            ),
            containerManagedUnitOfWork
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1Factory#getConnection(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader)
     */
    public Connection_1_3 getConnection(
        boolean containerManagedUnitOfWork,
        boolean transactionPolicyIsNew, 
        ServiceHeader serviceHeader
    ) throws ServiceException {
        return new Connection_1(
            new Provider_1(
                this.shareableChannel.createRequestCollection(serviceHeader),
                transactionPolicyIsNew
            ),
            containerManagedUnitOfWork
        );
    }


}

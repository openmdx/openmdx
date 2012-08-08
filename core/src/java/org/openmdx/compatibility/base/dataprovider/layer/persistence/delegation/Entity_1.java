/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Entity_1.java,v 1.13 2008/09/10 08:55:25 hburger Exp $
 * Description: Entity_1 
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.layer.persistence.delegation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.resource.ResourceException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.Reconstructable;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.ManagerFactory_2_0;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderOperations;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.layer.application.CommonConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;

/**
 * Entity_1
 */
public class Entity_1
extends StreamOperationAwareLayer_1
{

    //---------------------------------------------------------------------------
    protected Set<Path> getDirectAccessPaths(
    ) throws ServiceException {
        return new HashSet<Path>(
                DEFAULT_DIRECT_ACCESS_PATHS
        );
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.dispatching.AbstractPlugin_1#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    @Override
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception, ServiceException {
        super.activate(id, configuration, delegation);
        this.objectCache = new HashMap<Path, RefObject_1_0>();
        this.objectIsNew = new HashMap<Path, Boolean>();
        this.classCache = new HashMap<String, Class<?>>();
        //
        // Keep local references
        //
        this.entityManagerFactory = (ManagerFactory_2_0) configuration.values(
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION
        ).get(
            0
        );
        this.model = (Model_1_0) configuration.values(
            SharedConfigurationEntries.MODEL
        ).get(
            0
        );
        //
        // retrievalSize
        //
        if(configuration.values(CommonConfigurationEntries.RETRIEVAL_SIZE).size() > 0) {
            this.retrievalSize = ((Number)configuration.values(
                CommonConfigurationEntries.RETRIEVAL_SIZE).get(0)
            ).intValue();
        }
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#deactivate()
     */
    @Override
    public void deactivate(
    ) throws Exception, ServiceException {
        this.entityManagerFactory = null;
        super.deactivate();
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[], org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply[])
     */
    @Override
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        super.epilog(header, requests, replies);
        this.objectCache.clear();
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public void prolog(
        ServiceHeader header, 
        DataproviderRequest[] requests
    ) throws ServiceException {
        super.prolog(header, requests);
        this.objectCache.clear();
        this.objectIsNew.clear();
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#epilog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply)
     */
    @Override
    public void epilog(
        ServiceHeader header,
        UnitOfWorkRequest request,
        UnitOfWorkReply reply
    ) throws ServiceException {
        if(request.isTransactionalUnit()) {
            this.entityManager.currentTransaction().commit();
        }
        //
        // Dissociate entity manager
        //
        this.entityManager.close();
        this.entityManager = null;
        //
        // Delegate to super class
        //
        super.epilog(header, request, reply);
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#prolog(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void prolog(
        ServiceHeader header, 
        UnitOfWorkRequest request
    ) throws ServiceException {
        super.prolog(header, request);
        //
        // Associate entity manager
        //
        List<String> principals = header.getPrincipalChain();
        Subject subject = principals.isEmpty() ? DEFAULT_SUBJECT : new Subject(
            true,
            Collections.EMPTY_SET,
            Collections.EMPTY_SET,
            Collections.singleton(
                new PasswordCredential(
                    principals.toString(),
                    NO_PASSWORD
                )
            )
        );
        try {
            this.entityManager = this.entityManagerFactory.createManager(subject);
        } catch (ResourceException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                "Persistence manager acquisiion failure"
            );
        }

        if(request.isTransactionalUnit()) {
            this.entityManager.currentTransaction().begin();
        }
    }

    //---------------------------------------------------------------------------
    protected RefObject_1_0 getObject(
        Path id,
        boolean mandatory
    ) throws ServiceException {
        try {
            return (RefObject_1_0) this.entityManager.getObjectById(id);
        } catch(JDOObjectNotFoundException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "Object not found",
                new BasicException.Parameter("path",id)
            );
        } catch(RuntimeException exception) {
            throw new ServiceException(exception);
        }            
    }

    //---------------------------------------------------------------------------
    protected Object[] toSelector(
        String qualifier
    ){
        return qualifier.startsWith("!") ?
            new Object[]{RefContainer.PERSISTENT, qualifier.substring(1)} :
                new Object[]{RefContainer.REASSIGNABLE, qualifier};
    }

    //---------------------------------------------------------------------------
    protected RefContainer getContainer(
        RefObject_1_0 _object,
        String _featureName
    ){
        RefObject_1_0 object = _object;
        String featureName = _featureName;
        if(featureName.indexOf(':') >= 0){
            // Namespace
            StringTokenizer tokens = new StringTokenizer(featureName,":");
            String containerName = tokens.nextToken();
            String objectName = tokens.nextToken();
            featureName = tokens.nextToken();
            RefContainer container = (RefContainer) object.refGetValue(containerName);
            object = (RefObject_1_0) container.refGet(toSelector(objectName));
        }
        return (RefContainer) object.refGetValue(featureName);
    }

    //---------------------------------------------------------------------------
    /**
     * Retrieve object with given id. The object is retrieved by navigating
     * the object hierarchy except the objects configured with getDirectAccessPaths() 
     */
    protected RefObject_1_0 retrieveObject(
        Path identity,
        boolean mandatory
    ) throws ServiceException {
        if(this.directAccessPaths == null) {
            this.directAccessPaths = this.getDirectAccessPaths();
        }
        // startFrom
        int startFrom = 0;
        for(Path path : this.directAccessPaths){
            if(identity.size() >= path.size()) {
                if(identity.getPrefix(path.size()).isLike(path)) {
                    startFrom = java.lang.Math.max(startFrom, path.size());
                }
            }
        }
        // getting objects starting from 'startFrom'
        RefObject_1_0 object = this.getObject(
            identity.getPrefix(startFrom),
            mandatory
        );
        for(
                int i = startFrom;
                object != null && i < identity.size(); 
                i+=2
        ) { 
            object = (RefObject_1_0)this.getContainer(
                object,
                identity.get(i)
            ).refGet(
                toSelector(identity.get(i+1))
            );
        }
        if(!mandatory || object != null) {
            return object;
        } 
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_FOUND,
            "Object not found",
            new BasicException.Parameter("accessPath", identity)
        );
    }

    //---------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1#get(org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader, org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest)
     */
    @SuppressWarnings("unchecked")
    @Override
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(this.retrievalSize > 1) {
            List<Path> batch = new ArrayList<Path>();
            batch.add(request.path());
            List<DataproviderObject> result = new ArrayList<DataproviderObject>();
            result.add(
                DataproviderObjectMarshaller.toDataproviderObject(
                    request.path(),        
                    this.retrieveObject(request.path(), true),
                    request,
                    this.model,
                    batch, 
                    this.retrievalSize
                )        
            );           
            for(
                    int i = 1;
                    i < this.retrievalSize && i < batch.size();
                    i++
            ) {
                Path path = batch.get(i);
                try {
                    result.add(
                        DataproviderObjectMarshaller.toDataproviderObject(
                            path,        
                            this.retrieveObject(path, true),
                            Collections.EMPTY_SET,
                            this.model,
                            batch, 
                            this.retrievalSize
                        )        
                    );   
                    SysLog.trace("Referenced object retrieved", path);
                } 
                catch (Exception exception) {
                    SysLog.warning("Retrieval of referenced object failed", Arrays.asList(request.path(), path));
                }
            }
            return new DataproviderReply(result);
        } 
        else {
            return new DataproviderReply(
                DataproviderObjectMarshaller.toDataproviderObject(
                    request.path(),        
                    retrieveObject(request.path(), true),
                    request,
                    this.model,
                    null, 
                    this.retrievalSize
                )
            );
        }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        List paths = null;
        RefContainer container = this.getContainer(
            this.retrieveObject(
                request.path().getParent(),
                true
            ),
            request.path().getBase()
        );
        switch(request.operation()) {
            case DataproviderOperations.ITERATION_START:
                paths = container.refGetAll(
                    new Object[]{
                        request.attributeFilter(),
                        request.attributeSpecifier()
                    }
                ); 
                break;
            case DataproviderOperations.ITERATION_CONTINUATION:
                paths = container.refGetAll(
                    new ByteArrayInputStream(
                        (byte[])request.context(DataproviderReplyContexts.ITERATOR).get(0)
                    )
                );        
                break;
            default:
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Unexpected operation",
                    new BasicException.Parameter(
                        "operation",
                        DataproviderOperations.toString(request.operation())
                    )
                );
        }

        // Complete and return as DataproviderObject
        List result = new ArrayList();
        int replySize = paths instanceof FetchSize 
        ? ((FetchSize)paths).getFetchSize() 
            : FetchSize.DEFAULT_FETCH_SIZE; 
        if(replySize <= 0) replySize = this.retrievalSize;
        if(replySize > request.size()) replySize = request.size();
        int replyPosition = request.position();
        if(request.direction() == Directions.DESCENDING) {
            if(replySize > replyPosition) replySize = replyPosition + 1;
            replyPosition = replyPosition + 1 - replySize;
        }
        int ii = 0;
        ListIterator iterator = null;
        for(
                iterator = paths.listIterator(replyPosition);
                iterator.hasNext() && ii < replySize;
                ii++
        ) {
            Object element = iterator.next();
            Path path;
            RefObject_1_0 source;
            if(element instanceof RefObject_1_0) {
                source = (RefObject_1_0)element;
                path = source.refGetPath();
            } 
            else {
                path = (Path)element;
                source = retrieveObject(path, true);
            }
            result.add(
                DataproviderObjectMarshaller.toDataproviderObject(
                    path,
                    source,
                    request,
                    this.model
                )
            );
        }
        this.entityManager.evictAll();
        DataproviderReply reply = new DataproviderReply(result);
        /**
         * Try to set context. If collection does not support the features
         * do not set the context
         */
        // HAS_MORE
        boolean hasMore = iterator.hasNext();
        try {
            reply.context(DataproviderReplyContexts.HAS_MORE).set(
                0,
                Boolean.valueOf(hasMore)
            );
        } 
        catch(Exception e) {
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "Reply context population failure",
                new BasicException.Parameter []{
                    new BasicException.Parameter("path", request.path()),
                    new BasicException.Parameter("context", DataproviderReplyContexts.HAS_MORE)                    
                }
            ).log();
        }
        // TOTAL
        try {
            reply.context(DataproviderReplyContexts.TOTAL).set(
                0,
                hasMore ? UNKNOWN_TOTAL : Integer.valueOf(replyPosition + result.size())     
            );
        } 
        catch(Exception e) {
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "Reply context population failure",
                new BasicException.Parameter []{
                    new BasicException.Parameter("path", request.path()),
                    new BasicException.Parameter("context", DataproviderReplyContexts.TOTAL)                    
                }
            ).log();
        }
        /**
         * ITERATOR. collection must be serializable in case an iteration
         * is required. If not throw an exception.
         */
        if(hasMore) {
            if(paths instanceof Reconstructable) {
                try {
                    ByteArrayOutputStream stream=new ByteArrayOutputStream();
                    ((Reconstructable)paths).write(stream);
                    reply.context(DataproviderReplyContexts.ITERATOR).add(
                        stream.toByteArray()
                    );
                } 
                catch (ServiceException exception){
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TOO_LARGE_RESULT_SET,
                        "A list exceeding the given limit " +
                        "could not be prepared for reconstruction",
                        new BasicException.Parameter("size", paths.size()),
                        new BasicException.Parameter("limit", String.valueOf(replySize))
                    );
                }
            } 
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.TOO_LARGE_RESULT_SET,
                    "A non-reconstructable list's size exceeds the given limit",
                    new BasicException.Parameter("size", paths.size()),
                    new BasicException.Parameter("limit",String.valueOf(replySize)),
                    new BasicException.Parameter("class",paths.getClass().getName())
                );
            }
        }
        // ATTRIBUTE_SELECTOR
        reply.context(DataproviderReplyContexts.ATTRIBUTE_SELECTOR).set(
            0,
            new Short(request.attributeSelector())
        );
        return reply;
    }

    //---------------------------------------------------------------------------
    protected Class<?> classForId(
        String mofId
    ) throws ServiceException{
        StringBuilder javaClass = new StringBuilder();
        List<String> modelClass = Arrays.asList(mofId.split(":"));
        int iLimit = modelClass.size() - 1;
        for(
                int i = 0;
                i < iLimit;
                i++
        ){
            javaClass.append(
                Identifier.PACKAGE_NAME.toIdentifier(modelClass.get(i))
            ).append(
                '.'
            );
        }
        try {
            Class<?> jmi1Class = Classes.getApplicationClass(
                javaClass.append(
                    Names.CCI2_PACKAGE_SUFFIX
                ).append(
                    '.'
                ).append(
                    Identifier.CLASS_PROXY_NAME.toIdentifier(modelClass.get(iLimit))
                ).toString(
                )
            );
            return jmi1Class;

        } catch (ClassNotFoundException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Class not found",
                new BasicException.Parameter("mofClass", mofId),
                new BasicException.Parameter("jmi1Class", javaClass)
            );
        }
    }

    //---------------------------------------------------------------------------
    protected RefObject_1_0 createObject(
        DataproviderRequest request
    ) throws ServiceException {
        Path identity = request.path();
        RefObject_1_0 object = (RefObject_1_0) this.entityManager.newInstance(
            classForId(
                (String)request.object().values(SystemAttributes.OBJECT_CLASS).get(0)
            )
        );
        if(identity.size() % 2 == 0){
            identity.add(PathComponent.createPlaceHolder());
            this.objectCache.put(identity, object);
        } 
        else {
            this.objectCache.put(new Path(identity), object);
        }
        return object;
    }

    //---------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        Path identity = request.path();
        RefObject_1_0 target = this.objectCache.get(identity);
        if(target == null) target = createObject(request);
        Path parentPath = identity.getPrefix( identity.size() - 2);
        RefObject_1_0 parent = this.objectCache.get(parentPath);
        if(parent==null) parent = this.retrieveObject(parentPath, true);
        DataproviderObjectMarshaller.toObject(    
            null,
            request.object(),
            target,
            this.objectCache,
            this.entityManager,
            this.model, true
        );
        RefContainer container = (RefContainer) parent.refGetValue(identity.get(parentPath.size()));
        Object[] selector = toSelector(identity.get(parentPath.size() + 1));
        container.refAdd(
            selector[0],
            selector[1],
            target
        );
        identity.setTo(target.refGetPath());
        return new DataproviderReply(
            request.object()
        );
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest    request
    ) throws ServiceException {
        RefObject_1_0 target = this.objectCache.get(request.path());
        if(target == null) target = this.retrieveObject(request.path(), true);
        DataproviderObjectMarshaller.toObject(
            null,
            request.object(),
            target,
            this.objectCache,
            this.entityManager,
            this.model, 
            false
        );
        if(request.attributeSelector() == AttributeSelectors.NO_ATTRIBUTES) {
            DataproviderObject reply = new DataproviderObject(
                request.path()
            );
            reply.values(SystemAttributes.OBJECT_CLASS).add(
                target.refClass().refMofId()
            );
            return new DataproviderReply(reply);          
        }
        else {
            return this.get(
                header,
                request
            );
        }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest    request
    ) throws ServiceException {
        RefObject_1_0 target = this.objectCache.get(request.path());
        if(target == null) target = this.retrieveObject(request.path(), true);
        DataproviderObjectMarshaller.toObject(
            null,
            request.object(),
            target,
            this.objectCache,
            this.entityManager,
            this.model, 
            true
        );        
        if(request.attributeSelector() == AttributeSelectors.NO_ATTRIBUTES) {
            DataproviderObject reply = new DataproviderObject(
                request.path()
            );
            reply.values(SystemAttributes.OBJECT_CLASS).add(
                target.refClass().refMofId()
            );
            return new DataproviderReply(reply);        
        }
        else {
            return new DataproviderReply(
                DataproviderObjectMarshaller.toDataproviderObject(
                    request.path(),
                    target,
                    request,
                    this.model
                )
            );
        }
    }

    //---------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        if(
                Boolean.TRUE.equals(this.objectIsNew.get(request.path())) ||
                this.retrieveObject(request.path(), false) == null
        ){
            DataproviderObject_1_0 object = request.object();
            // exclude Authority, Provider, Segment
            if(object.path().size() > 5) {
                object.clearValues(SystemAttributes.CREATED_BY).addAll(
                    object.values(SystemAttributes.MODIFIED_BY)
                );
                object.clearValues(SystemAttributes.CREATED_AT).addAll(
                    object.values(SystemAttributes.MODIFIED_AT)
                );
            }
            return this.create(header,request); 
        } 
        else {
            return this.replace(header, request);
        }
    }

    //---------------------------------------------------------------------------
    public DataproviderReply remove(
        ServiceHeader header,
        DataproviderRequest    request
    ) throws ServiceException {
        DataproviderReply reply = this.get(
            header,
            request
        );
        RefObject_1_0 target = this.retrieveObject(
            request.path(),
            true
        );
        target.refDelete();
        return reply;
    }

    //---------------------------------------------------------------------------
    protected String getFeatureName(
        DataproviderRequest request
    ) {
        Path path = request.path();    
        return path.size() % 2 == 0 
        ? (String)path.get(path.size()-1) 
            : (String)path.get(path.size()-2);
    }

    //---------------------------------------------------------------------------
    protected DataproviderObject otherOperation(
        ServiceHeader header,
        DataproviderRequest request,
        String operation,
        Path replyPath
    ) throws ServiceException {
        RefObject_1_0 target = this.retrieveObject(
            request.path().getPrefix(request.path().size() - 2),
            true
        );
        String featureName = this.getFeatureName(request);
        // Namespaces
        if (featureName.indexOf(':') >= 0) {
            StringTokenizer tokens = new StringTokenizer(featureName, ":");
            String containerName = tokens.nextToken();
            String objectName = tokens.nextToken();
            RefContainer container = (RefContainer) target.refGetValue(containerName);
            target = (RefObject_1_0)container.refGet(toSelector(objectName));
        }
        try {
            return DataproviderObjectMarshaller.toDataproviderObject(
                replyPath,
                (RefStruct_1_0)target.refInvokeOperation(
                    featureName,
                    DataproviderObjectMarshaller.toStructureValues(
                        request.object(),
                        this.objectCache,
                        this.entityManager,
                        this.model
                    )
                ),
                new ArrayList<Path>()
            );
        }
        catch(Exception e) {
            throw new ServiceException(e); 
        }
    }

    //---------------------------------------------------------------------------  
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#getStreamOperation(org.openmdx.compatibility.base.naming.Path, java.lang.String, java.io.OutputStream, long)
     */
    @Override
    protected DataproviderObject getStreamOperation(
        ServiceHeader header,
        Path objectPath,
        String feature,
        OutputStream value, 
        long position, 
        Path replyPath
    ) throws ServiceException {
        return null;
    }

    //---------------------------------------------------------------------------  
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1#getStreamOperation(org.openmdx.compatibility.base.naming.Path, java.lang.String, java.io.Writer, long)
     */
    @Override
    protected DataproviderObject getStreamOperation(
        ServiceHeader header,
        Path objectPath,
        String feature,
        Writer value, 
        long position, 
        Path replyPath
    ) throws ServiceException {
        return null;
    }

    //---------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------
    private int retrievalSize = 1;
    protected Set<Path> directAccessPaths = null;
    private ManagerFactory_2_0 entityManagerFactory;
    private Model_1_0 model;
    protected Map<Path,Boolean> objectIsNew = null;
    protected Map<Path,RefObject_1_0> objectCache = null;
    protected Map<String,Class<?>> classCache = null;
    @SuppressWarnings("unchecked")
    private static Subject DEFAULT_SUBJECT = new Subject(
        true,
        Collections.EMPTY_SET,
        Collections.EMPTY_SET,
        Collections.EMPTY_SET
    );    
    private static final char[] NO_PASSWORD = new char[]{};    
    private static final Collection<Path> DEFAULT_DIRECT_ACCESS_PATHS = Arrays.asList(
        new Path("xri:@openmdx:*"),
        new Path("xri:@openmdx:*/provider/:*"),
        new Path("xri:@openmdx:*/provider/:*/segment/:*")
    );

    /**
     * This value means that potentially expensive counting has been avoided.
     */
    protected final static Integer UNKNOWN_TOTAL = new Integer(Integer.MAX_VALUE);

    /**
     * An entity manager is kept for a collection of units of work
     */
    private PersistenceManager entityManager;

}

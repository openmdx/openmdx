/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: AbstractPlugin_1.java,v 1.35 2009/03/02 13:38:15 wfro Exp $
 * Description: AbstractPlugin_1
 * Revision:    $Revision: 1.35 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/02 13:38:15 $
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.dispatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.accessor.Connection_1;
import org.openmdx.application.dataprovider.accessor.Switch_1;
import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderRequest;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.dataprovider.spi.OperationAwareLayer_1;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.PersistenceManager_1_0;
import org.openmdx.base.accessor.view.Manager_1;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.compatibility.base.dataprovider.layer.application.CommonConfigurationEntries;
import org.openmdx.kernel.exception.BasicException;

//-----------------------------------------------------------------------------
/**
 * Abstract class which provides the infrastructure to dispatch openMDX 1
 * compatibility Layer_1_0 requests to ObjectFactory_1_0 object requests.
 * 
 */
@SuppressWarnings("unchecked")
abstract public class AbstractPlugin_1
extends OperationAwareLayer_1 
{

    //---------------------------------------------------------------------------
    /**
     * Retrieve object with given id. The object is retrieved by navigating
     * the object hierarchy except the objects configured with getDirectAccessPaths() 
     */
    protected DataObject_1_0 retrieveObject(
        Path identity
    ) throws ServiceException {
        return this.retrieveObject(identity, true);
    }

    //---------------------------------------------------------------------------
    protected FilterableMap getContainer(
        DataObject_1_0 _object,
        String _featureName
    ) throws ServiceException {
        DataObject_1_0 object = _object;
        String featureName = _featureName;
        // handle namespaces
        if(featureName.indexOf(':') >= 0){
            StringTokenizer tokens = new StringTokenizer(featureName,":");
            String containerName = tokens.nextToken();
            String objectName = tokens.nextToken();
            featureName = tokens.nextToken();
            object = object.objGetContainer(containerName).get(objectName);
        }
        return object.objGetContainer(featureName);
    }

    //---------------------------------------------------------------------------
    /**
     * Retrieve object with given id. The object is retrieved by navigating
     * the object hierarchy except the objects configured with getDirectAccessPaths() 
     */
    protected DataObject_1_0 retrieveObject(
        Path identity,
        boolean mandatory
    ) throws ServiceException {
        if(this.directAccessPaths == null) {
            this.directAccessPaths = this.getDirectAccessPaths();
        }
        // startFrom
        int startFrom = 0;
        for(
                Iterator i = this.directAccessPaths.iterator();
                i.hasNext();
        ) {
            Path path = (Path)i.next();
            if(identity.size() >= path.size()) {
                if(identity.getPrefix(path.size()).isLike(path)) {
                    startFrom = java.lang.Math.max(startFrom, path.size());
                }
            }
        }
        // getting objects starting from 'startFrom'
        DataObject_1_0 object = this.getObject(
            identity.getPrefix(startFrom),
            mandatory
        );
        for(
                int i = startFrom;
                object != null && i < identity.size(); 
                i+=2
        ) { 
            try {
                object = (DataObject_1_0)this.getContainer(
                    object,
                    identity.get(i)
                ).get(identity.get(i+1));
            } 
            catch (ServiceException exception) {
                if(
                        exception.getExceptionCode() != BasicException.Code.NOT_FOUND
                ) throw exception;
                object = null;  
            }
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
    protected DataObject_1_0 retrieveObject(
        DataproviderRequest request,
        boolean mandatory
    ) throws ServiceException {
        Path identity = request.path();
        DataObject_1_0 object = this.retrieveObject(
            identity, 
            mandatory
        );
        if(object != null) {
            try {
                object.objGetClass(); // Verify existence
                this.objectCache.put(identity, object);
            } 
            catch (ServiceException exception) {
                if(mandatory) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "Object not found",
                        new BasicException.Parameter("accessPath", identity)
                    );
                }
                object = null;
            }
        }
        this.objectIsNew.put(
            identity,
            Boolean.valueOf(object == null)
        );
        return object;    
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
    // Layer_1_0
    //---------------------------------------------------------------------------
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        //
        // Default  Qualifier Type
        //
        this.defaultQualifierType = configuration.isOn(SharedConfigurationEntries.SEQUENCE_SUPPORTED) 
        ? "SEQUENCE" 
            : configuration.isOn(SharedConfigurationEntries.COMPRESS_UID) 
            ? "UID" 
                : "UUID";
        //
        // batchSize
        //
        if(configuration.values(SharedConfigurationEntries.BATCH_SIZE).size() > 0) {
            this.batchSize = ((Number)configuration.values(
                SharedConfigurationEntries.BATCH_SIZE).get(0)
            ).intValue();
        }
        //
        // retrievalSize
        //
        if(configuration.values(CommonConfigurationEntries.RETRIEVAL_SIZE).size() > 0) {
            this.retrievalSize = ((Number)configuration.values(
                CommonConfigurationEntries.RETRIEVAL_SIZE).get(0)
            ).intValue();
        }
        //
        // packageImpl
        //
        if(configuration.values(SharedConfigurationEntries.MODEL_PACKAGE).size() > 0) {
            this.packageImpls = new HashMap();
            for(
                    ListIterator i = configuration.values(SharedConfigurationEntries.MODEL_PACKAGE).listIterator();
                    i.hasNext();
            ) {
                String packageImpl = (String)configuration.values(SharedConfigurationEntries.PACKAGE_IMPL).get(
                    i.nextIndex()
                );
                this.packageImpls.put(
                    i.next(),
                    packageImpl
                );
            }
        }
        //
        // Switch configuration
        //
        SparseList dataproviderSource = configuration.values(
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION
        );
        SparseList delegationPathSource = configuration.values(
            SharedConfigurationEntries.DELEGATION_PATH
        );
        if(delegationPathSource.isEmpty()){
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "The org:openmdx:compatibility:runtime1 model allowing to explore other dataproviders is no longer supported. " +
                "No " + SharedConfigurationEntries.DATAPROVIDER_CONNECTION +
                " entries must be specified without their corresponding " + SharedConfigurationEntries.DELEGATION_PATH
            );
        } 
        else {
            List dataproviderTarget = new ArrayList();
            List delegationPathTarget = new ArrayList();
            for(
                    ListIterator dpi = delegationPathSource.populationIterator();
                    dpi.hasNext();
            ){
                int i = dpi.nextIndex();
                Object dp = dataproviderSource.get(i);
                if(dp == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        "The delegation path at the given index has no corresponding dataprovider counterpart",
                        new BasicException.Parameter(
                            SharedConfigurationEntries.DELEGATION_PATH,
                            delegationPathSource
                        ),
                        new BasicException.Parameter(
                            "index",
                            i
                        ),
                        new BasicException.Parameter(
                            SharedConfigurationEntries.DATAPROVIDER_CONNECTION,
                            dataproviderSource
                        )
                    );
                }
                Object delegationPath = dpi.next();
                delegationPathTarget.add(
                    delegationPath instanceof String
                    ? new Path((String)delegationPath)
                    : delegationPath
                );
                dataproviderTarget.add(dp);
            }
            this.router = new Switch_1(
                (Dataprovider_1_0[]) dataproviderTarget.toArray(
                    new Dataprovider_1_0[dataproviderTarget.size()]
                ),
                Path.toPathArray(delegationPathTarget),
                this.getDelegation()
            );
        }                

        this.objectCache = new HashMap();
        this.objectIsNew = new HashMap();
    }

    //-------------------------------------------------------------------------
    protected DataObject_1_0 createObject(
        DataproviderRequest request
    ) throws ServiceException {
        Path identity = request.path();
        DataObject_1_0 object = this.objectFactory.newInstance(
            (String)request.object().values(SystemAttributes.OBJECT_CLASS).get(0)
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

    //-------------------------------------------------------------------------
    public void prolog(
        ServiceHeader header,
        DataproviderRequest[] requests
    ) throws ServiceException {  
        super.prolog(
            header, 
            requests
        );
        this.header = header;
        Dataprovider_1_0 delegation = this.router == null ? 
            this.getDelegation() : 
                this.router;
            if(delegation == null) {
                this.delegation = null;
                this.manager = null;
            } else {
                this.delegation = new RequestCollection(
                    header,
                    delegation
                );
                PersistenceManager_1_0 interaction = new Connection_1(
                    this.delegation,
                    false, // transactionPolicyIsNew
                    true, // containerManagedUnitOfWork
                    this.defaultQualifierType
                ); 
                this.manager = new Manager_1(interaction);
            }
            this.objectFactory = this.getObjectFactory();
            ((Synchronization_1_0)this.objectFactory.currentTransaction().getSynchronization()).afterBegin();
            this.objectCache.clear();
            this.objectIsNew.clear();
    }

    //-------------------------------------------------------------------------
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        this.objectFactory.currentTransaction().getSynchronization().beforeCompletion();
        this.objectCache.clear();
        super.epilog(
            header, 
            requests, 
            replies
        );
    }  

    //---------------------------------------------------------------------------
    // (optionally) to be implemented by subclass of Plugin_1
    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
    abstract protected PersistenceManager_1_0 getObjectFactory(
    ) throws ServiceException;

    //---------------------------------------------------------------------------
    /**
     * By default, Plugin_1 retrieves an object starting at the root object.
     * An object with path 'xri:@openmdx:<a-id>/provider/<p-id>/segment/<s-id>/ref/<o-id>'
     * is retrieved as follows:
     * <p>
     * <ul>
     *   <li>provider = objectFactory.getObject("xri:@openmdx:<a-id>/provider/<p-id>");</li>
     *   <li>segment = provider.objGetContainer("segment").get("s-id");</li>
     *   <li>obj = segment.objGetContainer("ref").get("o-id");</li>
     *   <li>return obj;</li>
     * </ul>
     * <p>
     * This multi-step retrieval is required because the features 'segment' and 'ref'
     * could be intercepted by user-defined application-logic (e.g. in case of 
     * derived references).
     * <p>
     * Plugin_1 allows to optimize the access by specificing a list of so called
     * direct access path patterns. If in the example above a direct access path
     * pattern 'xri:@openmdx:<a-id>/:* /segment/:* /ref/:*' is configured, Plugin_1
     * retrieves objects matching the pattern in one step:
     * <p>
     * <ul>
     *   <li>obj = objectFactory.getObject("xri:@openmdx:<a-id>/provider/<p-id>/segment/<s-id>/ref/<o-id>");</li>
     *   <li>return obj;</li>
     * </ul> 
     * <p>
     * The following direct access paths are preconfigured:
     * <ul>
     *   <li>xri:@openmdx:*</li>
     *   <li>xri:@openmdx:* /provider/:*</li>
     *   <li>xri:@openmdx:* /provider/:* /segment/:*</li>
     * </ul>
     * <p>
     */
    protected Set getDirectAccessPaths(
    ) throws ServiceException {
        Set paths = new HashSet();
        paths.add(new Path("xri:@openmdx:*"));
        paths.add(new Path("xri:@openmdx:*/provider/:*"));
        paths.add(new Path("xri:@openmdx:*/provider/:*/segment/:*"));
        return paths;
    }

    //---------------------------------------------------------------------------
    /**
     * Return default manager.
     */
    public PersistenceManager_1_0 getManager(
    ) {
        return this.manager;
    }

    //---------------------------------------------------------------------------
    /**
     * Create a new manager with specified ServiceHeader
     */
    public PersistenceManager_1_0 getManager(
        ServiceHeader header
    ) throws ServiceException {
        return new Manager_1(
            this.getConnection(header)
        );
    }

    /**
     * Create a new provider with specified ServiceHeader
     */
    public PersistenceManager_1_0 getConnection(
        ServiceHeader header
    ) throws ServiceException {
        return new Connection_1(
            new RequestCollection(
                header,
                this.router
            ),
            false, // transactionPolicyIsNew
            true, // containerManagedUnitOfWork
            this.defaultQualifierType
        );
    }

    //---------------------------------------------------------------------------
    public ServiceHeader getServiceHeader(
    ) {
        return this.header;
    }

    //---------------------------------------------------------------------------
    protected Map getPackageImpls(
    ) {
        return this.packageImpls;
    }

    //---------------------------------------------------------------------------
    protected DataObject_1_0 getObject(
        Path id,
        boolean mandatory
    ) throws ServiceException {
        DataObject_1_0 object = null;
        ServiceException cause = null;
        try {
            object = (DataObject_1_0)this.objectFactory.getObjectById(id);
        } 
        catch(Exception e) {
            ServiceException e0 = new ServiceException(e);
            if(e0.getExceptionCode() != BasicException.Code.NOT_FOUND) throw e0;
            cause = e0;
        }
        if(mandatory && object == null) {
            throw new ServiceException(
                cause,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "Object not found",
                new BasicException.Parameter("path",id)
            );
        }
        return object;
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    protected int retrievalSize = 1;
    protected int batchSize = Integer.MAX_VALUE;
    protected Dataprovider_1_0 router = null;
    protected PersistenceManager_1_0 objectFactory = null;
    protected Map packageImpls = null;
    protected RequestCollection delegation = null;    
    protected Manager_1 manager = null;
    protected Set directAccessPaths = null;
    protected ServiceHeader header = null;
    protected Map objectCache = null;
    protected Map objectIsNew = null;
    protected String defaultQualifierType = null;

}

//--- End of File -----------------------------------------------------------

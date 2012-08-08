/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: AbstractPlugin_1.java,v 1.4 2008/02/29 18:05:19 hburger Exp $
 * Description: AbstractPlugin_1
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/29 18:05:19 $
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

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_1;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.dataprovider.layer.application.CommonConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.spi.StreamOperationAwareLayer_1;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Switch_1;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.naming.PathComponent;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;

//-----------------------------------------------------------------------------
/**
 * Abstract class which provides the infrastructure to dispatch openMDX 1
 * compatibility Layer_1_0 requests to ObjectFactory_1_0 object requests.
 * 
 */
@SuppressWarnings("unchecked")
abstract public class AbstractPlugin_1
  extends StreamOperationAwareLayer_1 {
  
    //---------------------------------------------------------------------------
    /**
     * Retrieve object with given id. The object is retrieved by navigating
     * the object hierarchy except the objects configured with getDirectAccessPaths() 
     */
    protected Object_1_0 retrieveObject(
        Path identity
    ) throws ServiceException {
        return this.retrieveObject(identity, true);
    }

    //---------------------------------------------------------------------------
    protected FilterableMap getContainer(
        Object_1_0 _object,
        String _featureName
    ) throws ServiceException {
        Object_1_0 object = _object;
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
    protected Object_1_0 retrieveObject(
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
        Object_1_0 object = this.getObject(
            identity.getPrefix(startFrom),
            mandatory
        );
        for(
            int i = startFrom;
            object != null && i < identity.size(); 
            i+=2
        ) { 
            try {
                object = (Object_1_0)this.getContainer(
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
            new BasicException.Parameter[]{
                new BasicException.Parameter("accessPath", identity)
            },
            "Object not found"
        );
    }

    //---------------------------------------------------------------------------
    protected Object_1_0 retrieveObject(
        DataproviderRequest request,
        boolean mandatory
    ) throws ServiceException {
        Path identity = request.path();
        Object_1_0 object = this.retrieveObject(
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
                        new BasicException.Parameter[]{
                            new BasicException.Parameter("accessPath", identity)
                        },
                        "Object not found"
                    );
                }
                object = null;
            }
        }
        this.objectIsNew.put(
            identity,
            new Boolean(object == null)
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
    @SuppressWarnings("deprecation")
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception, ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        //
        // Default  Qualifier Type
        //
        this.defaultQualifierType = configuration.isOn(CommonConfigurationEntries.SEQUENCE_SUPPORTED) 
            ? "SEQUENCE" 
            : configuration.isOn(CommonConfigurationEntries.COMPRESS_UID) 
                ? "UID" 
                : "UUID";
        //
        // batchSize
        //
        if(configuration.values(CommonConfigurationEntries.BATCH_SIZE).size() > 0) {
            this.batchSize = ((Number)configuration.values(
                CommonConfigurationEntries.BATCH_SIZE).get(0)
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
        if(configuration.values(CommonConfigurationEntries.MODEL_PACKAGE).size() > 0) {
            this.packageImpls = new HashMap();
            for(
                ListIterator i = configuration.values(CommonConfigurationEntries.MODEL_PACKAGE).listIterator();
                i.hasNext();
            ) {
                String packageImpl = (String)configuration.values(CommonConfigurationEntries.PACKAGE_IMPL).get(
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
            CommonConfigurationEntries.DATAPROVIDER_CONNECTION
        );
        SparseList delegationPathSource = configuration.values(
            CommonConfigurationEntries.DELEGATION_PATH
        );
        if(delegationPathSource.isEmpty()){
            if(dataproviderSource.isEmpty()) {
                this.router = this.getDelegation();
            } 
            else {
                SysLog.info(
                    "The org:openmdx:compatibility:runtime1 model allowing to explore other dataproviders is deprecated",
                    "Specifying some " + CommonConfigurationEntries.DATAPROVIDER_CONNECTION +
                    " entries without their corresponding " + CommonConfigurationEntries.DELEGATION_PATH +
                    " is deprecated"
                );
                List dataproviderTarget = dataproviderSource.population(); 
                this.router = new Switch_1(
                    (Dataprovider_1_0[]) dataproviderTarget.toArray(
                        new Dataprovider_1_0[dataproviderTarget.size()]
                    ),
                    this.getDelegation()
                );   
            }
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
                        new BasicException.Parameter[]{
                            new BasicException.Parameter(
                                CommonConfigurationEntries.DELEGATION_PATH,
                                delegationPathSource
                            ),
                            new BasicException.Parameter(
                                "index",
                                i
                            ),
                            new BasicException.Parameter(
                                CommonConfigurationEntries.DATAPROVIDER_CONNECTION,
                                dataproviderSource
                            )
                        },
                        "The delegation path at the given index has no corresponding dataprovider counterpart"
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
        //
        // get model
        //
        List models = configuration.values(CommonConfigurationEntries.MODEL);
        if (models.isEmpty()) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                null,
                "A model must be configured with options 'modelPackage' and 'packageImpl'"
            );
        } 
        else {
            this.model = (Model_1_3) models.get(0);
        }
        this.objectCache = new HashMap();
        this.objectIsNew = new HashMap();
    }

    //-------------------------------------------------------------------------
    protected Object_1_0 createObject(
        DataproviderRequest request
    ) throws ServiceException {
        Path identity = request.path();
        Object_1_0 object = this.objectFactory.createObject(
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
        this.delegation = new RequestCollection(
            header,
            this.router == null
                ? this.getDelegation()
                : this.router
        );
        Provider_1_0 provider = new Provider_1(
            this.delegation,
            false // transactionPolicyIsNew
        );
        Connection_1_5 interaction = new Connection_1(
            provider,
            true, // containerManagedUnitOfWork
            this.defaultQualifierType
        ); 
        interaction.setModel(this.model);
        this.manager = new Manager_1(interaction);
        this.objectFactory = this.getObjectFactory();
        this.objectFactory.getUnitOfWork().afterBegin();
        this.objectCache.clear();
        this.objectIsNew.clear();
    }

    //-------------------------------------------------------------------------
    public void epilog(
        ServiceHeader header,
        DataproviderRequest[] requests,
        DataproviderReply[] replies
    ) throws ServiceException {
        this.objectFactory.getUnitOfWork().beforeCompletion();
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
    abstract protected ObjectFactory_1_0 getObjectFactory(
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
    public Model_1_0 getModel(
    ) {
        return this.model;
    }

    //---------------------------------------------------------------------------
    /**
     * Return default manager.
     */
    public ObjectFactory_1_1 getManager(
    ) {
        return this.manager;
    }

    //---------------------------------------------------------------------------
    /**
     * Create a new manager with specified ServiceHeader
     */
    public ObjectFactory_1_0 getManager(
        ServiceHeader header
    ) throws ServiceException {
        Provider_1_0 provider = new Provider_1(
            new RequestCollection(
                header,
                this.router
            ),
            false
        );
        return new Manager_1(
            new Connection_1(
                provider,
                true,
                this.defaultQualifierType
            )
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
    protected Object_1_0 getObject(
        Path id,
        boolean mandatory
    ) throws ServiceException {
        Object_1_0 object = null;
        ServiceException cause = null;
        try {
            object = this.objectFactory.getObject(id);
        } 
        catch(ServiceException e) {
            if(e.getExceptionCode() != BasicException.Code.NOT_FOUND) throw e;
            cause = e;
        }
        if(mandatory && object == null) {
            throw new ServiceException(
                cause,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("path",id)
                },
                "Object not found"
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
    protected ObjectFactory_1_0 objectFactory = null;
    protected Model_1_3 model = null;
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

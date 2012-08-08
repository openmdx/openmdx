/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ObjectView_1.java,v 1.7 2008/09/10 08:55:21 hburger Exp $
 * Description: ObjectView_1 class
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:21 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
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
 */
package org.openmdx.compatibility.base.dataprovider.layer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.view.Manager_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderReply;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequestContexts;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Provider_1;
import org.openmdx.compatibility.base.dataprovider.transport.adapter.Switch_1;
import org.openmdx.compatibility.base.dataprovider.transport.delegation.Connection_1;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * @author wfro
 */
@SuppressWarnings("unchecked")
public abstract class ObjectView_1
extends Standard_1 {

    //------------------------------------------------------------------------
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

        // Default  Qualifier Type
        this.defaultQualifierType = configuration.isOn(
            SharedConfigurationEntries.COMPRESS_UID
        ) ? "UID" : "UUID";

        // Switch configuration
        SparseList dataproviderSource = configuration.values(
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION
        );
        SparseList delegationPathSource = configuration.values(
            SharedConfigurationEntries.DELEGATION_PATH
        );
        if(delegationPathSource.isEmpty()){
            if(dataproviderSource.isEmpty()) {
                this.router = this.getDelegation();
            } else {
                SysLog.info(
                    "The org:openmdx:compatibility:runtime1 model allowing to explore other dataproviders is deprecated",
                    "Specifying some " + SharedConfigurationEntries.DATAPROVIDER_CONNECTION +
                    " entries without their corresponding " + SharedConfigurationEntries.DELEGATION_PATH +
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
        } else {
            List dataproviderTarget = new ArrayList();
            List delegationPathTarget = new ArrayList();
            for(
                    ListIterator dpi = delegationPathSource.populationIterator();
                    dpi.hasNext();
            ){
                int i = dpi.nextIndex();
                Object dp = dataproviderSource.get(i);
                if(dp == null) throw new ServiceException(
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
                delegationPathTarget.add(dpi.next());
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
    }

    //------------------------------------------------------------------------
    private void prepareObjectFactories(
        ServiceHeader header, 
        boolean reset
    ) throws ServiceException {
        //
        // volatile object factory
        //
        if(reset || this.volatileObjectFactory == null) this.volatileObjectFactory = new Manager_1(
            new Connection_1(
                new Provider_1(
                    new RequestCollection(
                        header,
                        this.getDelegation()
                    ),
                    false
                ),
                false,
                this.defaultQualifierType
            )
        );
        //
        // persistent object factory
        // 
        if(reset || this.persistentObjectFactory == null) this.persistentObjectFactory = new Manager_1(
            new Connection_1(
                new Provider_1(
                    new RequestCollection(
                        header,
                        this.router
                    ),
                    false
                ),
                true,
                this.defaultQualifierType
            )
        );
    }

    //------------------------------------------------------------------------
    // implements Layer_1_0
    //------------------------------------------------------------------------

    /**
     * Set the unit of work id and test whether it has changed or is null.
     * 
     * @return true if the unit of work id has changed or is null.  
     */
    private boolean setUnitOfWorkId(
        String unitOfWorkId
    ){
        boolean changed = unitOfWorkId == null || unitOfWorkId != this.currentUnitOfWorkId;
        this.currentUnitOfWorkId = unitOfWorkId;
        return changed;
    }

    //------------------------------------------------------------------------
    public void prolog(
        ServiceHeader header, 
        DataproviderRequest[] requests
    ) throws ServiceException {
//      super.prolog(header,requests);
        prepareObjectFactories(
            header,
            setUnitOfWorkId(
                requests.length > 0 ? 
                    (String) requests[0].context(DataproviderRequestContexts.UNIT_OF_WORK_ID).get(0) :
                        null
            )
        );    
    }


    //------------------------------------------------------------------------
    public DataproviderReply get(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.prepareObjectFactories(header, false);
        this.volatileObjectFactory.getUnitOfWork().begin();
        this.prepareObject(
            request.path()
        );
        this.volatileObjectFactory.getUnitOfWork().commit();
        return super.get(
            header,
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply find(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.prepareObjectFactories(header, false);
        this.volatileObjectFactory.getUnitOfWork().begin();
        this.prepareContainer(
            request.path()
        );
        this.volatileObjectFactory.getUnitOfWork().commit();
        return super.find(
            header,
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply set(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.prepareObjectFactories(header, false);
        try {
            super.get(
                header,
                request
            );
        } catch(ServiceException e) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "ObjectView does not support object creation",
                new BasicException.Parameter("object", request.object())
            );    
        }
        // object is already prepared and is replaced
        return super.set(
            header,
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply create(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "ObjectView does not support object creation",
            new BasicException.Parameter("object", request.object())
        );    
    }

    //------------------------------------------------------------------------
    public DataproviderReply replace(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.prepareObjectFactories(header, false);
        super.get(
            header,
            request
        );
        return super.replace(
            header,
            request
        );
    }

    //------------------------------------------------------------------------
    public DataproviderReply modify(
        ServiceHeader header,
        DataproviderRequest request
    ) throws ServiceException {
        this.prepareObjectFactories(header, false);
        super.get(
            header,
            request
        );
        return super.modify(
            header,
            request
        );
    }

    //------------------------------------------------------------------------
    // ObjectView
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    /**
     * This method is no longer invoked.
     * <p>
     * The clearStorage() invocations have been replaced by object factory
     * replacements.
     * 
     * @deprecated since openMDX 1.5.3
     */
    protected abstract void clearStorage(
    ) throws ServiceException;

    //------------------------------------------------------------------------
    /**
     * Prepare objects of the specified container. The objects must be prepared
     * using the volatile factory. prepareContainer() is called within a
     * unit of work.
     */
    protected abstract void prepareContainer(
        Path identity
    ) throws ServiceException;

    //------------------------------------------------------------------------
    /**
     * Prepare specified object. The object must be prepared using the volatile 
     * factory. prepareContainer() is called within a unit of work.
     */
    protected abstract void prepareObject(
        Path identity
    ) throws ServiceException;

    //------------------------------------------------------------------------
    protected ObjectFactory_1_0 getVolatileObjectFactory(
    ) throws ServiceException {
        return this.volatileObjectFactory;
    }

    //------------------------------------------------------------------------
    /**
     * Object factory used to retrieve objects from other (remote) providers.
     * ObjectView does not perform any unit of work handling on this factory.
     */
    protected ObjectFactory_1_0 getPersistentObjectFactory(
    ) throws ServiceException {
        return this.persistentObjectFactory;
    }

    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------
    private String currentUnitOfWorkId = null;
    private Dataprovider_1_0 router = null;
    private ObjectFactory_1_0 volatileObjectFactory = null;
    private ObjectFactory_1_0 persistentObjectFactory = null;
    private String defaultQualifierType = null;

}

//---End of File -----------------------------------------------------------

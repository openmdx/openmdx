/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractExplorer_1.java,v 1.7 2008/09/10 08:55:20 hburger Exp $
 * Description: Abstract Explorer Plug-In
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:20 $
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
package org.openmdx.compatibility.runtime1.layer.application;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.application.configuration.Configuration;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSelectors;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderRequest;
import org.openmdx.compatibility.base.dataprovider.cci.Directions;
import org.openmdx.compatibility.base.dataprovider.cci.RequestCollection;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.spi.DelegatingLayer_0;
import org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Abstract Explorer Plug-In
 */
@SuppressWarnings("unchecked")
public abstract class AbstractExplorer_1 extends DelegatingLayer_0 {

    /**
     * 
     */
    private Path[] scopes;

    /**
     * 
     */
    private Integer[] mapping;

    /**
     * 
     */
    private Path domainReference;

    /**
     * 
     */
    private SparseList dataproviders;

    /**
     * 
     */
    private SparseList ignoreSuffix;

    /**
     * 
     */
    private SparseList ignorePrefix;


    //------------------------------------------------------------------------
    // Implements Layer_1_0
    //------------------------------------------------------------------------

    /**
     * 
     */
    @SuppressWarnings("deprecation")
    public void activate(
        short id,
        Configuration configuration,
        Layer_1_0 delegation
    ) throws Exception {
        SparseList jndiContext = configuration.values(
            LayerConfigurationEntries.JNDI_CONTEXT
        );
        SparseList serviceLocatorContext = configuration.values(
            LayerConfigurationEntries.SERVICE_LOCATOR_CONTEXT
        );
        this.ignoreSuffix = configuration.values(
            LayerConfigurationEntries.IGNORE_SUFFIX
        );
        this.ignorePrefix = configuration.values(
            LayerConfigurationEntries.IGNORE_PREFIX
        );
        this.dataproviders = configuration.values(
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION
        );
        if(
                this.dataproviders.isEmpty() == 
                    (jndiContext.isEmpty() && serviceLocatorContext.isEmpty())
        ) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.INVALID_CONFIGURATION,
            "Either " + SharedConfigurationEntries.DATAPROVIDER_CONNECTION + 
            " or " + LayerConfigurationEntries.JNDI_CONTEXT + " and " +
            LayerConfigurationEntries.SERVICE_LOCATOR_CONTEXT + 
            " must be empty, but not all of them",
            new BasicException.Parameter(
                SharedConfigurationEntries.DATAPROVIDER_CONNECTION,
                this.dataproviders
            ),
            new BasicException.Parameter(
                LayerConfigurationEntries.JNDI_CONTEXT,
                jndiContext
            ),
            new BasicException.Parameter(
                LayerConfigurationEntries.SERVICE_LOCATOR_CONTEXT,
                serviceLocatorContext
            )
        );
        if (! this.dataproviders.isEmpty()) {
            List scopes = configuration.values(
                SharedConfigurationEntries.EXPOSED_PATH
            );
            this.scopes = new Path[scopes.size()];
            for(
                    int i = 0;
                    i < this.scopes.length;
                    i++
            ){ 
                Object exposedPath = scopes.get(i);
                this.scopes[i] = exposedPath instanceof Path ?
                    (Path)exposedPath : 
                        new Path(exposedPath.toString())
                    ;
            }
            this.mapping = new Integer[this.scopes.length];
            for(
                    int i = 0;
                    i < this.scopes.length;
                    i++
            ) this.mapping[i] = new Integer(i);
        } else {
            SysLog.detail("jndiContext", jndiContext);
            SysLog.detail("serviceLocatorContext", serviceLocatorContext);
            Path runtimeSegment = toPath(
                configuration.values(
                    LayerConfigurationEntries.RUNTIME_SEGMENT
                ).get(
                    0
                )
            );
            if(runtimeSegment == null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                LayerConfigurationEntries.RUNTIME_SEGMENT + 
                " configuration missing"
            );

            ServiceHeader header = new ServiceHeader();
            List scopes = new ArrayList();
            List mapping = new ArrayList();
            this.domainReference = runtimeSegment.getChild("domain");

            if(!jndiContext.isEmpty()){// Lookup in JNDI tree
                Context initialContext = new InitialContext();
                String context = null;
                for( 
                        Iterator i = jndiContext.populationIterator();
                        i.hasNext();
                ) try {
                    context = (String)i.next();
                    processBindings(
                        header,
                        initialContext.listBindings(context),
                        scopes, mapping
                    );
                } catch (Exception exception) {
                    // Silently ignore empty registries
                }
            }

            if(!serviceLocatorContext.isEmpty()){// Lookup in ServiceLocator
                org.openmdx.compatibility.base.application.cci.ServiceLocator_1_0 serviceLocator = org.openmdx.compatibility.base.application.j2ee.StandardServiceLocator.getInstance();
                for( 
                        Iterator i = serviceLocatorContext.populationIterator();
                        i.hasNext();
                ) processBindings(
                    header,
                    serviceLocator.listBindings((String)i.next()),
                    scopes, mapping
                );
            }

            this.scopes = new Path[scopes.size()];
            for(
                    int i = 0;
                    i < this.scopes.length;
                    i++
            ){ 
                Object exposedPath = scopes.get(i);
                this.scopes[i] = exposedPath instanceof Path ?
                    (Path)exposedPath : 
                        new Path(exposedPath.toString())
                    ;
            }
            this.mapping = (Integer[])mapping.toArray(
                new Integer[mapping.size()]
            );

            SysLog.detail(
                "Exposed paths", 
                Arrays.asList(this.scopes)
            );
            SysLog.detail(
                "Dataprovider mapping", 
                Arrays.asList(this.mapping)
            );
        }
        super.activate(
            id,
            configuration,
            delegation
        );
    }

    /**
     * 
     * @param binding
     * @return
     */
    private boolean isToBeIgnored(
        Binding binding
    ){
        String name = binding.getName();
        String pattern = null;
        for(
                Iterator i = this.ignorePrefix.populationIterator();
                i.hasNext();
        ) if(name.startsWith(pattern = (String)i.next())){
            SysLog.detail(
                "Ignore binding starting with '" + pattern + "'", 
                binding
            );
            return true;
        }
        for(
                Iterator i = this.ignoreSuffix.populationIterator();
                i.hasNext();
        ) if(name.endsWith(pattern = (String)i.next())){
            SysLog.detail(
                "Ignore binding ending with '" + pattern + "'", 
                binding
            );
            return true;
        }
        return false;   
    }

    /**
     * 
     * @param header
     * @param bindings
     * @param scopes
     * @param mapping
     */
    private void processBindings(
        ServiceHeader header,
        Enumeration bindings,
        List scopes, List mapping
    ){
        while(bindings.hasMoreElements()){
            Binding binding = (Binding)bindings.nextElement();
            if(!isToBeIgnored(binding)) try {
                SysLog.trace("Binding accepted", binding);
                Dataprovider_1_1Connection connection = 
                    Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                        binding.getObject()
                    );
                RequestCollection requests = new RequestCollection(
                    header,
                    connection
                );
                List domains = requests.addFindRequest(
                    this.domainReference,
                    null
                );
                DataproviderObject_1_0 domain = ((DataproviderObject_1_0)
                        domains.get(0)
                );
                List namespaces = requests.addFindRequest(
                    domain.path().getChild("namespace"),
                    null,
                    AttributeSelectors.ALL_ATTRIBUTES, null,
                    0, Integer.MAX_VALUE,
                    Directions.ASCENDING
                );
                DataproviderObject_1_0 namespace = ((DataproviderObject_1_0)
                        namespaces.get(0)
                );
                SysLog.info(
                    binding.getName(), 
                    "Provides namespace '" + namespace + "'"
                );
                Integer index = new Integer(this.dataproviders.size());
                this.dataproviders.add(connection);
                for(
                        Iterator k = namespace.values(
                            SharedConfigurationEntries.EXPOSED_PATH
                        ).populationIterator();
                        k.hasNext();
                ){
                    scopes.add(k.next());
                    mapping.add(index);
                }                                       
            } catch (Exception exception) {
                ServiceException serviceException = new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Getting a dataprovider's configuration failed",
                    new BasicException.Parameter("binding", binding)
                );
                SysLog.criticalError(
                    serviceException.getMessage(),
                    serviceException.getExceptionStack()
                );
            }
        }
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------

    /**
     * Process a set of working units
     *
     * @param   header          the service header
     * @param   unitsOfWork a collection of working units
     *
     * @return  a collection of working unit replies
     */
    public final UnitOfWorkReply[] process(
        ServiceHeader header,
        UnitOfWorkRequest[] unitsOfWork
    ){
        UnitOfWorkReply[] replies = new UnitOfWorkReply[unitsOfWork.length];
        for(
                int i=0;
                i < unitsOfWork.length;
                i++
        ) replies[i] = process(header, unitsOfWork[i]);
        return replies;
    }

    /**
     * Process a single unit of work
     *
     * @param   header          the service header
     * @param   unitOfWork      a working unit
     *
     * @return  a collection of working unit replies
     */
    abstract protected UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWork
    );

    /**
     * Process a sub-request 
     * 
     * @param header
     * @param unitOfWorkRequest
     * @param unitOfWorkReply
     * @param position
     * @param length
     * @param mapping
     * @return
     */
    protected UnitOfWorkReply process(
        ServiceHeader header,
        UnitOfWorkRequest unitOfWorkRequest,
        UnitOfWorkReply unitOfWorkReply,
        int[] positions,
        Integer mapping
    ){
        SysLog.trace("Request order", ArraysExtension.asList(positions));
        DataproviderRequest[] requests = new DataproviderRequest[positions.length];
        for(
                int i = 0;
                i < positions.length;
                i++
        ) requests[i] = unitOfWorkRequest.getRequests()[positions[i]];
        UnitOfWorkRequest subunitOfWorkRequest = new UnitOfWorkRequest(
            unitOfWorkRequest.isTransactionalUnit(),
            requests
        );
        subunitOfWorkRequest.context(
            SharedConfigurationEntries.DATAPROVIDER_CONNECTION
        ).set(
            0,
            mapping
        );
        UnitOfWorkReply subunitOfWorkReply = super.process(
            header,
            new UnitOfWorkRequest[]{subunitOfWorkRequest}
        )[0];
        if (subunitOfWorkReply.failure()) return subunitOfWorkReply;
        for(
                int i = 0;
                i < positions.length;
                i++
        ) unitOfWorkReply.getReplies()[positions[i]] = subunitOfWorkReply.getReplies()[i];
        return unitOfWorkReply;
    }

    /**
     * 
     * @param request
     * @return
     * @throws ServiceException
     */
    protected Integer getMapping(
        DataproviderRequest request
    ) throws ServiceException {
        Path path = request.path();
        for (
                int i = 0;
                i < this.scopes.length;
                i++
        ) if (path.startsWith(this.scopes[i])) return this.mapping[i];
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "No dataprovider supporting \"" + path + "\" found",
            new BasicException.Parameter("path",path)
        );
    }

    /**
     * The toPath method accepts any input class
     */
    protected final static Path toPath(
        Object path
    ){
        return
        path == null ? 
            null : 
                path instanceof Path ? 
                    (Path)path : 
                        new Path(path.toString());
    }

}

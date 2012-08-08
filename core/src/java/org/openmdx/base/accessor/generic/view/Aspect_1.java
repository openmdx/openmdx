/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Aspect_1.java,v 1.5 2008/09/18 12:46:43 hburger Exp $
 * Description: Aspect_1 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/18 12:46:43 $
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

package org.openmdx.base.accessor.generic.view;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Aspect_1
 */
abstract class Aspect_1
    extends ContextCapable_1
    implements Object_1_0
{

    /**
     * Constructor for retrieval
     *
     * @param aspect
     * @param marshaller
     * @throws ServiceException
     */
    protected Aspect_1(
        Object_1_0 aspect,
        Manager_1 marshaller
    ) throws ServiceException {
        super(
            aspect,
            marshaller
        );
    }
    
    /**
     * org::openmdx::base::Aspect's MOF id
     */
    private final static String ASPECT = "org:openmdx:base:Aspect";

    /**
     * org::openmdx::base::AspectCapable's MOF id
     */
    private final static String ASPECT_CAPABLE = "org:openmdx:base:AspectCapable";

    /**
     * 
     */
    private transient Object_1_0 core;
    
    /**
     *  
     */
    private transient Map<?,?> coreFeatures;

    /**
     * Defines whether the object is an Aspect instance
     */
    private transient Boolean aspect;
    
    /**
     * @throws ServiceException 
     * 
     */
    protected final boolean isAspect(
    ) throws ServiceException{
        if(this.aspect == null) {
            boolean aspect = getMarshaller().getModel().isInstanceof(this, ASPECT);
            this.aspect = Boolean.valueOf(aspect);
            return aspect;
        } else {
            return this.aspect.booleanValue();
        }
    }
    
    /**
     * Tells whether a given feature is a core or an aspect feature
     * 
     * @param feature the fature's name
     * 
     * @return <code>true</code> if the given feature is a core feature
     * @throws ServiceException 
     */
    private boolean isCoreFeature(
        String feature
    ) throws ServiceException{
        if(!isAspect() || "core".equals(feature)) {
            return false;
        }
        if(coreFeatures == null) {
            this.coreFeatures = (Map<?,?>)super.marshaller.getModel(
            ).getElement(
                getCore().objGetClass()
            ).values(
                "allFeature"
            ).get(
                0
            );
        }
        return this.coreFeatures.containsKey(feature);
    }
       
    /**
     * Retrieve the core delegate
     * @throws ServiceException 
     */
    private Object_1_0 getCore(
    ) throws ServiceException{
        if(this.core == null) {
            this.core = (Object_1_0) super.objGetValue("core");
        }
        if(this.core == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An aspect is inaccessible until the core object is set"
            );
        }
        return this.core; 
    }
    
    /**
     * Replace the core delegate
     * 
     * @param core
     * 
     * @throws ServiceException
     */
    private void setCore(
        Object_1_0 core
    ) throws ServiceException{
        if(core != null) {
            if(!getMarshaller().getModel().isInstanceof(core, ASPECT_CAPABLE)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "The aspectCapable argument is not an isntance of AspectCapable",
                    new BasicException.Parameter("class", core.objGetClass())
                );
            }
        }
        super.objSetValue(
            "core", 
            this.core = core
        );
    }
    
    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddToUnitOfWork()
     */
    public void objAddToUnitOfWork(
    ) throws ServiceException {
        if(isAspect()) {
            getCore().objAddToUnitOfWork();
        }
        super.objAddToUnitOfWork();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMakeVolatile()
     */
    public void objMakeVolatile(
    ) throws ServiceException {
        if(isAspect()) {
            getCore().objMakeVolatile();
        }
        super.objMakeVolatile();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRefresh()
     */
    public void objRefresh(
    )throws ServiceException {
        if(isAspect()) {
            getCore().objRefresh();
        }
        super.objRefresh();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveFromUnitOfWork()
     */
    public void objRemoveFromUnitOfWork(
    ) throws ServiceException {
        if(isAspect()) {
            getCore().objRemoveFromUnitOfWork();
        }
        super.objRemoveFromUnitOfWork();
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException {
        Set<String> features = super.objDefaultFetchGroup();
        if(isAspect()) {
            features.addAll(getCore().objDefaultFetchGroup());
        }
        return features;
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objFlush()
     */
    public boolean objFlush(
    ) throws ServiceException {
        return (!isAspect() || getCore().objFlush()) & super.objFlush();
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDirty()
     */
    public boolean objIsDirty(
    ) throws ServiceException {
        return super.objIsDirty() || (isAspect() && getCore().objIsDirty());
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsInUnitOfWork()
     */
    public boolean objIsInUnitOfWork(
    ) throws ServiceException {
        return super.objIsInUnitOfWork() || (isAspect() && getCore().objIsInUnitOfWork());
    }

    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
     */
    public FilterableMap<String, Object_1_0> objGetContainer(
        String feature
    ) throws ServiceException {
        if(isAspect()) {
            if(isCoreFeature(feature)) {
                return getCore().objGetContainer(feature);
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Containment is supported for core objects only at the moment",
                new BasicException.Parameter("feature", feature)
            );
        }
        return super.objGetContainer(feature);
    }

    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetLargeObject(java.lang.String)
     */
    public LargeObject_1_0 objGetLargeObject(
        String feature
    ) throws ServiceException {
        return isCoreFeature(feature) ? 
            getCore().objGetLargeObject(feature) :
            super.objGetLargeObject(feature);
    }

    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetList(java.lang.String)
     */
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        return isCoreFeature(feature) ? 
            getCore().objGetList(feature) :
            super.objGetList(feature);
    }

    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetSet(java.lang.String)
     */
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return isCoreFeature(feature) ? 
            getCore().objGetSet(feature) :
            super.objGetSet(feature);
    }

    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetSparseArray(java.lang.String)
     */
    public SortedMap<Integer, Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        return isCoreFeature(feature) ? 
            getCore().objGetSparseArray(feature) :
            super.objGetSparseArray(feature);
    }

    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetValue(java.lang.String)
     */
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return 
            isAspect() && "core".equals(feature) ? getCore() :
            isCoreFeature(feature) ? getCore().objGetValue(feature) :
            super.objGetValue(feature);
    }

    /**
     * @param operation
     * @param arguments
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        return isCoreFeature(operation) ? 
            getCore().objInvokeOperation(operation, arguments) :
            super.objInvokeOperation(operation, arguments);
    }

    /**
     * @param operation
     * @param arguments
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        return isCoreFeature(operation) ? 
            getCore().objInvokeOperationInUnitOfWork(operation, arguments) :
            super.objInvokeOperationInUnitOfWork(operation, arguments);
    }

    /**
     * @param feature
     * @param to
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objSetValue(java.lang.String, java.lang.Object)
     */
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException {
        if(isAspect() && "core".equals(feature)) {
            setCore((Object_1_0) to);
        } else if(isCoreFeature(feature)) {
            getCore().objSetValue(feature, to);
        } else {
            super.objSetValue(feature, to);
        }
    }
   
}

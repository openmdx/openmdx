/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Aspect_1.java,v 1.3 2008/12/15 03:15:37 hburger Exp $
 * Description: Aspect_1 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:37 $
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

package org.openmdx.base.aop2.core;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.generic.spi.Object_1_5;
import org.openmdx.base.accessor.generic.spi.Object_1_6;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * Aspect_1
 */
public class Aspect_1 extends PlugIn_1 {

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * @param next the next plug-in
     * @throws ServiceException
     */
    public Aspect_1(
        Object_1_6 self,
        Object_1_0 next
    ) throws ServiceException {
        super(self, next);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1129453356997731102L;

    /**
     * org::openmdx::base::Aspect's MOF id
     */
    public final static String CLASS = "org:openmdx:base:Aspect";

    /**
     * Reference to the aspect's core object
     */
    public final static String CORE = "core";

    /**
     * 
     */
    private Object_1_5 core = null;

    /**
     *  
     */
    private transient Map<?,?> coreFeatures;

    /**
     * Retrieve the core object's class in order to know which features have to be delegated to it. 
     * 
     * @return the core object's class
     * 
     * @throws ServiceException
     */
    protected String getCoreClass(
    ) throws ServiceException{
        return getCore(true).objGetClass();
    }
    
    private final Map<?,?> getCoreFeatures(
    ) throws ServiceException{
        if(this.coreFeatures == null) {
            this.coreFeatures = (Map<?,?>)self.getModel().getElement(
                getCoreClass()
            ).values(
                "allFeature"
            ).get(
                0
            );
        }
        return this.coreFeatures;
    };
    
    /**
     * Retrieve the delegate
     * 
     * @param feature the fature's name
     * 
     * @return the core or aspect instance depending on the feature
     * 
     * @throws ServiceException 
     */
    private final Object_1_0 getDelegate(
        String feature
    ) throws ServiceException{
        return getCoreFeatures().containsKey(feature) ? 
            getCore(true) : 
            getDelegate();
    }
    
    protected Object_1_5 getCore() throws ServiceException{
        return (Object_1_5) this.self.objGetDelegate().objGetValue(CORE);
    }
    
    /**
     * Retrieve the core delegate
     * @param required 
     * @throws ServiceException 
     */
    private final Object_1_5 getCore(
        boolean required
    ) throws ServiceException{
        if(this.core == null) { 
            this.core = getCore();
        }
        if(required && this.core == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "This invocation is only allowed after the aspect's core is defined"
            );
        }
        return this.core;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.StaticallyDelegatingObject_1#objGetResourceIdentifier()
     */
    @Override
    public Object objGetResourceIdentifier() {
        try {
            Object_1_5 core = getCore(false); 
            return core == null ? null : core.objGetResourceIdentifier();
        } catch (ServiceException exception) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.StaticallyDelegatingObject_1#objGetPath()
     */
    @Override
    public Path objGetPath(
    ) throws ServiceException {
        Object_1_5 core = getCore(false); 
        return core == null ? null : core.objGetPath();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.StaticallyDelegatingObject_1#objIsPersistent()
     */
    @Override
    public boolean objIsPersistent(
    ) throws ServiceException {
        Object_1_5 core = getCore(false); 
        return core != null && core.objIsPersistent();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#getAspect(java.lang.String)
     */
    @Override
    public Map<String, Object_1_0> getAspect(
        String aspectClass
    ) throws ServiceException {
        return getCore(true).getAspect(aspectClass);
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddToUnitOfWork()
     */
    public void objAddToUnitOfWork(
    ) throws ServiceException {
        getCore(true).objAddToUnitOfWork();
        super.objAddToUnitOfWork();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMakeVolatile()
     */
    public void objMakeVolatile(
    ) throws ServiceException {
        getCore(true).objMakeVolatile();
        super.objMakeVolatile();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRefresh()
     */
    public void objRefresh(
    )throws ServiceException {
        getCore(true).objRefresh();
        super.objRefresh();
    }

    /**
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveFromUnitOfWork()
     */
    public void objRemoveFromUnitOfWork(
    ) throws ServiceException {
        getCore(true).objRemoveFromUnitOfWork();
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
        Object_1_0 core = getCore(false);
        if(core != null) {
            features.addAll(core.objDefaultFetchGroup());
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
        return super.objFlush() & getCore(true).objFlush();
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsDirty()
     */
    public boolean objIsDirty(
    ) throws ServiceException {
        if(super.objIsDirty()) {
            return true;
        } else {
            Object_1_5 core = getCore(false);
            return core != null && core.objIsDirty();
        }
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objIsInUnitOfWork()
     */
    public boolean objIsInUnitOfWork(
    ) throws ServiceException {
        if(super.objIsInUnitOfWork()) {
            return true;
        } else {
            Object_1_5 core = getCore(false);
            return core != null && core.objIsInUnitOfWork();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objIsDeleted()
     */
    @Override
    public boolean objIsDeleted(
    ) throws ServiceException {
        if(super.objIsDeleted()) {
            return true;
        } else {
            Object_1_5 core = getCore(false);
            return core != null && core.objIsDeleted();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objIsNew()
     */
    @Override
    public boolean objIsNew(
    ) throws ServiceException {
        if(super.objIsNew()) {
            return true;
        } else {
            Object_1_5 core = getCore(false);
            return core != null && core.objIsNew();
        }
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
        return getCore(true).objGetContainer(feature);
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
        return getDelegate(feature).objGetLargeObject(feature);
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
        return getDelegate(feature).objGetList(feature);
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
        return getDelegate(feature).objGetSet(feature);
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
        return getDelegate(feature).objGetSparseArray(feature);
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
        return getDelegate(operation).objInvokeOperation(operation, arguments);
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
        return getDelegate(operation).objInvokeOperationInUnitOfWork(operation, arguments);
    }

    /**
     * Set the core object reference
     * 
     * @param to
     * 
     * @throws ServiceException
     */
    protected void setCore(
        Object_1_5 to
    ) throws ServiceException{
        Object_1_5 oldValue = getCore(false);
        if(oldValue != null && to != oldValue) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An aspect's core can't be replaced",
                new BasicException.Parameter("class", to.objGetClass())
            );
        }
        if(to != null && !self.getModel().isInstanceof(to, AspectCapable_1.CLASS)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The core object must be an instance of AspectCapable",
                new BasicException.Parameter("class", to.objGetClass())
            );
        }
        super.objSetValue(CORE, to);
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
        if(CORE.equals(feature)) {
            setCore((Object_1_5) to);
        } else {
            getDelegate(feature).objSetValue(feature, to);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    @Override
    public void objMove(
        FilterableMap<String, Object_1_0> there, 
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "An aspect must not be moved, one sets its core instead",
            new BasicException.Parameter("criteria", criteria)
        );
    }

}

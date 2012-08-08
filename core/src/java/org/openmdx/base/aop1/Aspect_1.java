/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Aspect_1.java,v 1.12 2009/04/22 16:20:58 hburger Exp $
 * Description: Aspect_1 
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/22 16:20:58 $
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

package org.openmdx.base.aop1;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOUserException;
import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.LargeObject_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.accessor.view.PlugIn_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.MethodInvocationSpec;
import org.openmdx.compatibility.state1.spi.StateCapables;
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
        ObjectView_1_0 self,
        PlugIn_1 next
    ) throws ServiceException {
        super(self, next);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1129453356997731102L;

    /**
     * 
     */
    private DataObject_1_0 core = null;

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
            this.coreFeatures = (Map<?,?>)getModel().getElement(
                getCoreClass()
            ).objGetValue("allFeature");
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
    private final DataObject_1_0 getDelegate(
        String feature
    ) throws ServiceException{
        return getCoreFeatures().containsKey(feature) ? 
            getCore(true) : 
            getDelegate();
    }
    
    private DataObject_1_0 getCoreOrDelegate(
    ){
        DataObject_1_0 core = getCore(false);
        if(core != null && core.jdoIsPersistent()) {
            Path coreId = core.jdoGetObjectId();
            if(StateCapables.isCoreObject(coreId)) try {
                return super.getDelegate();
            } catch (ServiceException exception) {
                return null; // there is no core object
            }
        }
        return core;
    }
    
    protected DataObject_1_0 getCore() throws ServiceException{
        return (DataObject_1_0) this.self.objGetDelegate().objGetValue("core");
    }
    
    /**
     * Retrieve the core delegate
     * @param required 
     * @throws ServiceException 
     */
    protected final DataObject_1_0 getCore(
        boolean required
    ) {
        try {
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
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get core object",
                e,
                this
            );
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.StaticallyDelegatingObject_1#objGetPath()
     */
    @Override
    public Path jdoGetObjectId(
    ) {
        DataObject_1_0 core = getCoreOrDelegate(); 
        return core == null ? null : core.jdoGetObjectId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop2.core.StaticallyDelegatingObject_1#objIsPersistent()
     */
    @Override
    public boolean jdoIsPersistent(
    ) {
        DataObject_1_0 core = getCoreOrDelegate(); 
        return core != null && core.jdoIsPersistent();
    }

    @Override
    public Path jdoGetTransactionalObjectId() {
        DataObject_1_0 core = getCoreOrDelegate(); 
        return core == null ? null : core.jdoGetTransactionalObjectId();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#getAspect(java.lang.String)
     */
    @Override
    public Map<String, DataObject_1_0> getAspect(
        String aspectClass
    ) throws ServiceException {
        return getCore(true).getAspect(aspectClass);
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup(
    ) throws ServiceException {
        Set<String> features = super.objDefaultFetchGroup();
        DataObject_1_0 core = getCore(false);
        if(core != null) {
            features.addAll(core.objDefaultFetchGroup());
        }
        return features;
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#jdoIsDirty()
     */
    public boolean jdoIsDirty(
    ) {
        if(super.jdoIsDirty()) {
            return true;
        } else {
            DataObject_1_0 core = getCoreOrDelegate();
            return core != null && core.jdoIsDirty();
        }
    }

    /**
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.PersistenceCapable_1_0#jdoIsTransactional()
     */
    public boolean jdoIsTransactional(
    ) {
        if(super.jdoIsTransactional()) {
            return true;
        } else {
            DataObject_1_0 core = getCoreOrDelegate();
            return core != null && core.jdoIsTransactional();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objIsDeleted()
     */
    @Override
    public boolean jdoIsDeleted(
    ) {
        if(super.jdoIsDeleted()) {
            return true;
        } else {
            DataObject_1_0 core = getCoreOrDelegate();
            return core != null && core.jdoIsDeleted();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.StaticallyDelegatingObject_1#objIsNew()
     */
    @Override
    public boolean jdoIsNew(
    ) {
        if(super.jdoIsNew()) {
            return true;
        } else {
            DataObject_1_0 core = getCoreOrDelegate();
            return core != null && core.jdoIsNew();
        }
    }

    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
     */
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        return getCore(true).objGetContainer(feature);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objGetValue(java.lang.String)
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return getDelegate(feature).objGetValue(feature);
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

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    @Override
    public boolean execute(
        InteractionSpec ispec, 
        Record input, 
        Record output
    ) throws ResourceException {
        if(ispec instanceof MethodInvocationSpec) try {
            return getDelegate(((MethodInvocationSpec)ispec).getFunctionName()).execute(ispec, input, output);
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new ResourceException(
                    "Method invocation failure",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
            );
        } else throw BasicException.initHolder(
            new ResourceException(
                "Method invocation failure",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("interactionSpec", ispec == null ? "<null>" : ispec.getClass().getName())
                )
            )
        );
    }

    /**
     * Validate the core value to be set
     * 
     * @param newValue
     * 
     * @throws ServiceException
     */
    protected void validateCore(
        DataObject_1_0 newValue
    ) throws ServiceException{
        DataObject_1_0 oldValue = getCore(false);
        if(oldValue != null && newValue != oldValue) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An aspect's core can't be replaced",
                new BasicException.Parameter("class", newValue.objGetClass())
            );
        }
        if(
            newValue != null && 
            !getModel().isInstanceof(newValue, "org:openmdx:base:AspectCapable")
        ) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The core object must be an instance of AspectCapable",
                new BasicException.Parameter("class", newValue.objGetClass())
            );
        }
    }
    
    /**
     * Set the core object reference
     * 
     * @param to
     * 
     * @throws ServiceException
     */
    protected void setCore(
        DataObject_1_0 to
    ) throws ServiceException{
        validateCore(to);
        this.core = to;
        super.objSetValue("core", to);
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
        if("core".equals(feature)) {
            setCore((DataObject_1_0) to);
        } else {
            getDelegate(feature).objSetValue(feature, to);
        }
    }

}

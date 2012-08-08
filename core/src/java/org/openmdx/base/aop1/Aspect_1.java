/*
 * ====================================================================
 * Name:        $Id: Aspect_1.java,v 1.22 2010/06/02 13:44:09 hburger Exp $
 * Description: org::openmdx::base::Aspect plug-in
 * Revision:    $Revision: 1.22 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:44:09 $
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

import javax.resource.ResourceException;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.Record;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.resource.spi.MethodInvocationSpec;
import org.openmdx.kernel.exception.BasicException;

/**
 * org::openmdx::base::Aspect plug-in
 */
public class Aspect_1 extends Interceptor_1 {

    /**
     * Constructor 
     *
     * @param self the plug-in holder
     * @param next the next plug-in
     * @throws ServiceException
     */
    public Aspect_1(
        ObjectView_1_0 self,
        Interceptor_1 next
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
    private transient DataObject_1_0 core = null;

    /**
     *  
     */
    private transient Map<?,?> coreFeatures;

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
    ) throws ServiceException {
        if(this.coreFeatures == null && getCore(false) != null) {
            this.coreFeatures = (Map<?,?>)getModel().getElement(
                getCore(true).objGetClass()
            ).objGetValue("allFeature");
        }
        return this.coreFeatures != null && this.coreFeatures.containsKey(feature) ? 
            getCore(true) : 
            getDelegate();
    }
    
    /**
     * Retrieve the core object
     * 
     * @throws ServiceException 
     */
    private DataObject_1_0 getCore(
        boolean required
    ) throws ServiceException {
        if(this.core == null) { 
            this.core = (DataObject_1_0) this.self.objGetDelegate().objGetValue("core");
        }
        if(required && this.core == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "This invocation is only allowed after the aspect's core is defined"
            );
        } else {
            return this.core;
        }
    }
    
    /**
     * @param feature
     * @return
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetContainer(java.lang.String)
     */
    @Override
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        DataObject_1_0 core = getCore(false);
        return core == null ? super.objGetContainer(feature) : core.objGetContainer(feature);
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
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objGetList(java.lang.String)
     */
    @Override
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
    @Override
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
    @Override
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
        DataObject_1_0 oldValue = (DataObject_1_0) this.self.objGetDelegate().objGetValue("core");
        if(newValue != oldValue) {
            if(oldValue != null) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ILLEGAL_STATE,
                "An aspect's core can't be replaced",
                ExceptionHelper.newObjectIdParameter("aspect", this),
                ExceptionHelper.newObjectIdParameter("newCore", newValue),
                ExceptionHelper.newObjectIdParameter("oldCore", oldValue)
            );
            Model_1_0 model = getModel();
            if(!model.isInstanceof(newValue, "org:openmdx:base:AspectCapable")) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The core object must be an instance of AspectCapable",
                ExceptionHelper.newObjectIdParameter("aspect", this),
                ExceptionHelper.newObjectIdParameter("newCore", newValue),
                new BasicException.Parameter("class", newValue.objGetClass())
            );
            if(!model.isSubtypeOf(objGetClass(), newValue.objGetClass())) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "The core object must be a super-class of the aspect",
                ExceptionHelper.newObjectIdParameter("aspect", this),
                ExceptionHelper.newObjectIdParameter("newCore", newValue),
                new BasicException.Parameter("class", newValue.objGetClass())
            );
        }
    }
    
    /**
     * @param feature
     * @param to
     * @throws ServiceException
     * @see org.openmdx.base.accessor.generic.cci.Featured_1_0#objSetValue(java.lang.String, java.lang.Object)
     */
    @Override
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException {
        if("core".equals(feature)) {
            DataObject_1_0 core = (DataObject_1_0) to;
            validateCore(core);
            super.objSetValue("core", this.core = core);
        } else {
            getDelegate(feature).objSetValue(feature, to);
        }
    }

}

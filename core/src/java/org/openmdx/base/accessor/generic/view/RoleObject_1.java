/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RoleObject_1.java,v 1.7 2008/03/19 17:18:51 hburger Exp $
 * Description: Embedded Role Object
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/19 17:18:51 $
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
package org.openmdx.base.accessor.generic.view;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.generic.spi.AbstractObject_1;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;

/**
 * Embedded Role Object
 */
@SuppressWarnings("unchecked")
class RoleObject_1 extends EmbeddedObject_1 {

    /**
     * 
     */
    private static final long serialVersionUID = 4049080427786678327L;


    /**
     * Constructor
     * 
     * @param object
     * @param objectClass
     * @param prefix
     * @param qualifier
     * @throws ServiceException
     */
    RoleObject_1(
        Object_1 object,
        String objectClass,
        String prefix, 
        String qualifier
    ) throws ServiceException{
        super(object.getDelegate(), objectClass, prefix);
        this.suffix = SystemAttributes.ROLE_CAPABLE_ROLE + '=' + qualifier;
        int i = qualifier.lastIndexOf('.');
        this.core = (Object_1_0) (
           i < 0 ? 
           object : 
           object.objGetContainer(
               SystemAttributes.ROLE_CAPABLE_ROLE).get(qualifier.substring(0,i)
           )
        );
    }
    
    /**
     * 
     */
    private final String suffix;

    /**
     * 
     */
    private final Object_1_0 core;

    
    //--------------------------------------------------------------------------
    // Implements Object_1_0
    //--------------------------------------------------------------------------

    /**
     * Tests whether a feature is a core or role feature.
     */
    private boolean isCoreFeature(
        String feature
    ) throws ServiceException{
        return this.core.objDefaultFetchGroup().contains(feature);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetPath()
     */
    public Path objGetPath() throws ServiceException {
        Path objectPath = getDelegate().objGetPath();
        if(objectPath == null) return null;
        String[] components = objectPath.getSuffix(0);
        components[components.length-1] += ';' + this.suffix;
        return new Path(components);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objDefaultFetchGroup()
     */
    public Set objDefaultFetchGroup() throws ServiceException {
        Set defaultFetchGroup = super.objDefaultFetchGroup();
        defaultFetchGroup.addAll(core.objDefaultFetchGroup());
        return defaultFetchGroup;
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetContainer(java.lang.String)
     */
    public FilterableMap objGetContainer(
        String feature
    ) throws ServiceException {
        return isCoreFeature(feature) ?
            core.objGetContainer(feature) :
            super.objGetContainer(feature);
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetLargeObject(java.lang.String)
     */
    public LargeObject_1_0 objGetLargeObject(
        String feature
    ) throws ServiceException {
        return isCoreFeature(feature) ?
            core.objGetLargeObject(feature) :
            super.objGetLargeObject(feature);
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetList(java.lang.String)
     */
    public List objGetList(String feature) throws ServiceException {
        return isCoreFeature(feature) ?
             core.objGetList(feature) :
             super.objGetList(feature);
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSet(java.lang.String)
     */
    public Set objGetSet(String feature) throws ServiceException {
        return isCoreFeature(feature) ?
            core.objGetSet(feature) :
            super.objGetSet(feature);
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSparseArray(java.lang.String)
     */
    public SortedMap objGetSparseArray(String feature) throws ServiceException {
        return isCoreFeature(feature) ?
            core.objGetSparseArray(feature) :
            super.objGetSparseArray(feature);
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetValue(java.lang.String)
     */
    public Object objGetValue(String feature) throws ServiceException {
        return isCoreFeature(feature) ?
            core.objGetValue(feature) :
            super.objGetValue(feature);
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        return isCoreFeature(operation) ?
            core.objInvokeOperation(operation, arguments) :
            super.objInvokeOperation(operation, arguments);
    }
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        return isCoreFeature(operation) ?
            core.objInvokeOperationInUnitOfWork(operation, arguments) :
            super.objInvokeOperationInUnitOfWork(operation, arguments);
    }

    
    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /**
     * 
     */
    public String toString(
    ){
        return AbstractObject_1.toString(this, this.objGetClass(), this.suffix);
    }

}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EmbeddedObject_1.java,v 1.10 2008/09/11 18:01:06 hburger Exp $
 * Description: Embedded Object
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/11 18:01:06 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.generic.view;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.generic.spi.AbstractObject_1;
import org.openmdx.base.accessor.generic.spi.DelegatingObject_1;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * Embedded Object
 */
@SuppressWarnings("unchecked")
abstract class EmbeddedObject_1 
    extends DelegatingObject_1 
    implements Serializable
{

    /**
     * Constructor
     * 
     * @param object
     * @param objectClass
     * @param prefix
     * @throws ServiceException
     */
    protected EmbeddedObject_1(
        Object_1_0 object,
        String objectClass,
        String prefix
    ) throws ServiceException{
        super(object);
        this.prefix = prefix;
        this.objectClass = objectClass;
    }

    /**
     * 
     */
    protected final String prefix;

    /**
     * 
     */
    protected final String objectClass;


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetPath()
     */
    public abstract Path objGetPath(
    ) throws ServiceException;

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objDefaultFetchGroup()
     */
    public Set objDefaultFetchGroup() throws ServiceException {
        Set defaultFetchGroup = new HashSet();
        for(
                Iterator i = super.objDefaultFetchGroup().iterator();
                i.hasNext();
        ){
            String feature = (String)i.next();
            if(
                    feature.startsWith(prefix)
            ) defaultFetchGroup.add(
                feature.substring(prefix.length())
            );
        }
        defaultFetchGroup.remove(SystemAttributes.OBJECT_CLASS);
        return defaultFetchGroup;
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
     */
    public String objGetClass() {
        return this.objectClass;
    }

    public Object_1_0 objCopy(
        FilterableMap there, 
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_IMPLEMENTED,
            "Method not implemented yet"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public void objMove(
        FilterableMap there, 
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "An embedded object can't be moved",
            new BasicException.Parameter("path",objGetPath()),
            new BasicException.Parameter("prefix",prefix),
            new BasicException.Parameter("criteria",criteria)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objSetValue(java.lang.String, java.lang.Object)
     */
    public void objSetValue(
        String feature, 
        Object to
    ) throws ServiceException{
        super.objSetValue((this.prefix + feature), to);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetValue(java.lang.String)
     */
    public Object objGetValue(String feature) throws ServiceException {
        return super.objGetValue((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetList(java.lang.String)
     */
    public List objGetList(String feature) throws ServiceException {
        return super.objGetList((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSet(java.lang.String)
     */
    public Set objGetSet(String feature) throws ServiceException {
        return super.objGetSet((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSparseArray(java.lang.String)
     */
    public SortedMap objGetSparseArray(
        String feature
    ) throws ServiceException {
        return super.objGetSparseArray((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetContainer(java.lang.String)
     */
    public FilterableMap objGetContainer(String feature) throws ServiceException {
        return super.objGetContainer((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperation(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        return super.objInvokeOperation(
            (this.prefix + operation),
            arguments
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        return super.objInvokeOperationInUnitOfWork(
            (this.prefix + operation),
            arguments
        );
    }


    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /**
     * 
     */
    public String toString(
    ){
        return AbstractObject_1.toString(this, "prefix=" + prefix);
    }

}

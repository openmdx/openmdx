/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EmbeddedObjectView_1.java,v 1.7 2009/02/10 16:36:37 hburger Exp $
 * Description: Embedded Object
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/10 16:36:37 $
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
package org.openmdx.base.accessor.view;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.StoreCallback;
import javax.resource.cci.InteractionSpec;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.accessor.spi.AbstractDataObject_1;
import org.openmdx.base.accessor.spi.DelegatingObject_1;
import org.openmdx.base.accessor.spi.Delegating_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.kernel.exception.BasicException;

/**
 * Embedded Object
 */
public abstract class EmbeddedObjectView_1 
    extends DelegatingObject_1 
    implements ObjectView_1_0, Delegating_1_0, LoadCallback, StoreCallback, DeleteCallback, Serializable {

    /**
     * Constructor
     * 
     * @param object
     * @param objectClass
     * @param prefix
     * @throws ServiceException
     */
    protected EmbeddedObjectView_1(
        DataObject_1_0 object,
        String objectClass,
        String prefix
    ) throws ServiceException{
        super(object);
        this.prefix = prefix;
        this.objectClass = objectClass;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetPath()
     */
    public abstract Object jdoGetObjectId(
    );

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objDefaultFetchGroup()
     */
    public Set<String> objDefaultFetchGroup() throws ServiceException {
        Set<String> defaultFetchGroup = new HashSet<String>();
        for(String feature : super.objDefaultFetchGroup()) {
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

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objMove(org.openmdx.base.collection.FilterableMap, java.lang.String)
     */
    public void objMove(
        FilterableMap<String, DataObject_1_0> there, 
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "An embedded object can't be moved",
            new BasicException.Parameter("path",jdoGetObjectId()),
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
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return super.objGetValue((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetList(java.lang.String)
     */
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        return super.objGetList((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSet(java.lang.String)
     */
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return super.objGetSet((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetSparseArray(java.lang.String)
     */
    public SortedMap<Integer,Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        return super.objGetSparseArray((this.prefix + feature));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetContainer(java.lang.String)
     */
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
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

    //------------------------------------------------------------------------
    // Implements ObjectView_1_0
    //------------------------------------------------------------------------    

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#getInteractionSpec()
     */
    public InteractionSpec getInteractionSpec(
    ) throws ServiceException {
        DataObject_1_0 delegate = this.getDelegate();
        return delegate instanceof ObjectView_1_0 ?
            ((ObjectView_1_0)delegate).getInteractionSpec() :
            null;        
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.view.ObjectView_1_0#getModel()
     */
    public Model_1_6 getModel() {
        DataObject_1_0 delegate;
        try {
            delegate = this.getDelegate();
            if(delegate instanceof ObjectView_1_0) {
                return ((ObjectView_1_0)delegate).getModel();
            }
        } catch (ServiceException ignorable) {
        }
        return Model_1Factory.getModel();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#objDelete()
     */
    public void objDelete(
    ) throws ServiceException {
        DataObject_1_0 delegate = this.getDelegate();
        JDOHelper.getPersistenceManager(delegate).deletePersistent(delegate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#objDelete()
     */
    public void objRefresh(
    ) throws ServiceException {
        DataObject_1_0 delegate = this.getDelegate();
        JDOHelper.getPersistenceManager(delegate).refresh(delegate);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#getMarshaller()
     */
    public Marshaller getMarshaller() {
        throw new UnsupportedOperationException("Operation not supported by StaticallyDelegationgObject_1");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Delegating_1_0#objGetDelegate()
     */
    public DataObject_1_0 objGetDelegate(
    ) throws ServiceException {
        return this.getDelegate();
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.view.ObjectView_1_0#objSetDelegate(org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public void objSetDelegate(DataObject_1_0 delegate) {
        throw new UnsupportedOperationException("Operation not supported by StaticallyDelegationgObject_1");
    }
    
    //--------------------------------------------------------------------------
    // Implements PersistenceCapable
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.spi.Object_1_5#getFactory()
     */
    public PersistenceManager jdoGetPersistenceManager(
    ) {
        try {
            DataObject_1_0 delegate = this.getDelegate();
            return JDOHelper.getPersistenceManager(delegate);
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get persistence manager",
                e,
                this
            );
        }
    }
    
    //--------------------------------------------------------------------------
    // Implements StoreCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.StoreCallback#jdoPreStore()
     */
    public void jdoPreStore(
    ) {
        try {
            DataObject_1_0 delegate = this.getDelegate();
            if(delegate instanceof StoreCallback) {
                ((StoreCallback)delegate).jdoPreStore();
            }
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get persistence manager",
                e,
                this
            );
        }
    }

    //--------------------------------------------------------------------------
    // Implements DeleteCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.DeleteCallback#jdoPreDelete()
     */
    public void jdoPreDelete(
    ) {
        try {
            DataObject_1_0 delegate = this.getDelegate();
            if(delegate instanceof DeleteCallback) {
                ((DeleteCallback)delegate).jdoPreDelete();
            }
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get persistence manager",
                e,
                this
            );
        }
    }

    //--------------------------------------------------------------------------
    // Implements LoadCallback
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.jdo.listener.LoadCallback#jdoPostLoad()
     */
    public void jdoPostLoad(
    ) {
        try {
            DataObject_1_0 delegate = this.getDelegate();
            if(delegate instanceof LoadCallback) {
                ((LoadCallback)delegate).jdoPostLoad();
            }
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get persistence manager",
                e,
                this
            );
        }
    }
    
    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    public String toString(
    ){
        return AbstractDataObject_1.toString(this, "prefix=" + prefix);
    }

    //--------------------------------------------------------------------------
    // Members
    //--------------------------------------------------------------------------
    private static final long serialVersionUID = 5939885690325545597L;

    protected final String prefix;
    protected final String objectClass;
    
}

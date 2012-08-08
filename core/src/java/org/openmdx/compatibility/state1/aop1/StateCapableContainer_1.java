/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateCapableContainer_1.java,v 1.9 2009/06/02 16:38:41 hburger Exp $
 * Description: State Object Container
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/02 16:38:41 $
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
package org.openmdx.compatibility.state1.aop1;

import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingFilterableMap;
import org.openmdx.base.exception.ServiceException;

/**
 * State Object Container
 */
@SuppressWarnings("unchecked")
public class StateCapableContainer_1 
    extends MarshallingFilterableMap
    implements Container_1_0
{

    /**
     * Constructor 
     *
     * @param delegate
     * @throws ServiceException 
     */
    public StateCapableContainer_1(
        ObjectView_1_0 parent
    ) throws ServiceException{
        super(
            parent.getMarshaller(),
            null
        );
        this.factory = parent.jdoGetPersistenceManager();
    }
       
    /**
     * 
     */
    private final PersistenceManager factory;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 1312568818384216689L;

    
    //------------------------------------------------------------------------
    // Implements Container_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Container_1_0#superSet()
     */
    public Container_1_0 superSet(
    ) {
        throw new UnsupportedOperationException("Operation not supported by StateCapableContainer");
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.Container_1_0#retrieve()
     */
    public void retrieveAll(boolean useFetchPlan) {
        throw new UnsupportedOperationException("Operation not supported by StateCapableContainer");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Container_1_0#getObjectId()
     */
    public Object getContainerId() {
        throw new UnsupportedOperationException("Operation not supported by StateCapableContainer");
    }

    
    //------------------------------------------------------------------------
    // Extends MarshallingFilterableMap
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.MarshallingFilterableMap#getDelegate()
     */
    @Override
    protected FilterableMap<String, DataObject_1_0> getDelegate() {
        throw new UnsupportedOperationException(
            "There is very restricted support for org::openmdx::compatobility::state1::StateCapable objects"
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.MarshallingMap#get(java.lang.Object)
     */
    @Override
    public DataObject_1_0 get(Object key) {
        try {
            return (DataObject_1_0) this.marshaller.marshal(
                ((DataObjectManager_1_0)this.factory).getObjectById(key)
            );
        } catch (Exception exception) {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.MarshallingMap#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        try {
            DataObject_1_0 object = (DataObject_1_0)this.factory.getObjectById(key);
            return 
                object != null &&
                "org:openmdx:compatibility:state1:StateCapable".equals(object.objGetClass());
        } catch (ServiceException exception) {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.collection.MarshallingMap#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        if(value instanceof ObjectView_1_0) try {
            ObjectView_1_0 candidate = (ObjectView_1_0) value;
            return 
                candidate.jdoIsPersistent() && 
                "org:openmdx:compatibility:state1:StateCapable".equals(candidate.objGetClass());
        } catch (ServiceException exception) {
            return false;
        } else {
            return false;
        }
    }

}

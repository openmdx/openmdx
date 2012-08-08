/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MarshallingObject_1.java,v 1.25 2010/12/07 23:07:37 hburger Exp $
 * Description: MarshallingObject_1 class
 * Revision:    $Revision: 1.25 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/12/07 23:07:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.spi;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.ExceptionListenerMarshaller;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.kernel.exception.BasicException;

/**
 * A marshalling object
 */
public abstract class MarshallingObject_1<M extends Marshaller>
    extends DelegatingObject_1 
{

    /**
     * Constructor
     */
    protected MarshallingObject_1(
        DataObject_1_0 object,
        M marshaller
    ){
        super(object);
        this.marshaller = marshaller;
    }

    /**
     * Deserializer
     */
    protected MarshallingObject_1(
    ){    
        super();
    }

    /**
     * @serial
     */
    protected M marshaller;

    /**
     * Retrieve the marshaller
     * 
     * @return the marshaller
     */
    public M getMarshaller(){
        return this.marshaller;
    }

    /**
     * In order to replace the given marshaller
     * 
     * @param marshaller
     */
    protected void setMarshaller(
        M marshaller
    ){
        this.marshaller = marshaller;
    }
    
    
    //------------------------------------------------------------------------
    // Implements Object_1_0
    //------------------------------------------------------------------------

    /**
     * Set an attribute's value.
     * <p>
     * This method returns a <code>BAD_PARAMETER</code> exception unless the 
     * feature is single valued or a stream. 
     *
     * @param       feature
     *              the attribute's name
     * @param       to
     *              the object.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is write protected or the feature is a
     *        stream modified in the current unit of work.
     * @exception   ServiceException BAD_PARAMETER
     *              if the feature is multi-valued
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    @Override
    public void objSetValue(
        String feature,
        Object to
    ) throws ServiceException {
        this.getDelegate().objSetValue(
            feature, 
            getMarshaller().unmarshal(to)
        );
    }

    /**
     * Get a feature.
     *
     * @param       feature
     *              the feature's name
     *
     * @return      the object representing the feature;
     *              or null if the feature's value hasn't been set yet.
     *
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ServiceException 
     *              if the object is not accessible
     */
    @Override
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return this.getMarshaller().marshal(
            getDelegate().objGetValue(feature)
        );
    }

    /**
     * Get a List attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a list
     */
    @Override
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        return new MarshallingList<Object>(
            this.getMarshaller(),
            this.getDelegate().objGetList(feature)
        );
    }

    /**
     * Get a Set attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     * @exception   ClassCastException
     *              if the feature's value is not a set
     */
    @Override
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return new MarshallingSet<Object>(
            this.getMarshaller(),
            this.getDelegate().objGetSet(feature)
        );
    }

    /**
     * Get a SparseArray attribute.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature's value is not a sparse array
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    @Override
    public SortedMap<Integer,Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        return new MarshallingSortedMap(
            this.getMarshaller(),
            this.getDelegate().objGetSparseArray(feature)
        );
    }

    /**
     * Get a reference feature.
     * <p> 
     * This method never returns <code>null</code> as an instance of the
     * requested class is created on demand if it hasn't been set yet.
     *
     * @param       feature
     *              The feature's name.
     *
     * @return      a collection which may be empty but never null.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if the object is deleted
     * @exception   ClassCastException
     *              if the feature is not a reference
     * @exception   ServiceException NOT_SUPPORTED
     *              if the object has no such feature
     */
    @Override
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        return new MarshallingContainer(
            this.jdoGetPersistenceManager(),
            this.getMarshaller(),
            this.getContainer(feature)
        );
    }

    /**
     * 
     * @param feature
     * 
     * @return the container to be marshalled
     * 
     * @throws ServiceException
     */
    protected Container_1_0 getContainer(
        String feature
    ) throws ServiceException {
        return this.getDelegate().objGetContainer(feature);
    }
    
    //--------------------------------------------------------------------------
    // Life Cycle Operations
    //--------------------------------------------------------------------------

    /**
     * Create an instance's clone
     * @param identity the identity of the new object if a persistent-new 
     * instance should be returned, <code>null</code> if a transient instance 
     * should be returned
     * 
     * @return a clone
     * 
     * @exception ServiceException if case of failure
     */
    @Override
    public DataObject_1_0 openmdxjdoClone(String... exclude
    ) {
        try {
            return (DataObject_1_0)this.getMarshaller().marshal(
                this.getDelegate().openmdxjdoClone()
            );
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to clone object",
                e,
                this
            );
        }
    }
                
    /**
     * The move operation moves the object to the scope of the container passed
     * as the first parameter. The object remains valid after move has
     * successfully executed.
     *
     * @param     there
     *            the object's new container.
     * @param     criteria
     *            The criteria is used to move the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException  ILLEGAL_STATE
     *            if the object is persistent.
     * @exception ServiceException BAD_PARAMETER
     *            if <code>there</code> is <code>null</code>.
     * @exception ServiceException  
     *            if the move operation fails.
     */
    @Override
    public void objMove(
        Container_1_0 there,
        String criteria
    ) throws ServiceException {
        Container_1_0 to = there == null ? 
            null : 
            ((MarshallingContainer)there).getDelegate(this.getMarshaller()); 
        this.getDelegate().objMove(
            to,
            criteria
        );
    } 


    //------------------------------------------------------------------------
    // Class MarshallingContainer
    //------------------------------------------------------------------------

    /**
     * Marshalling Container
     */
    protected static class MarshallingContainer 
        extends MarshallingContainer_1
    {

        /**
         * Constructor
         * 
         * @param marshaller
         * @param container
         */
        protected MarshallingContainer(
            PersistenceManager persistenceManager,
            Marshaller marshaller,
            Container_1_0 container
        ) throws ServiceException {
            super(persistenceManager, marshaller, container);
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3257009873437996080L;

        /**
         * Get the delegate and verifies the marshaller
         * 
         * @param requestedMarshaller
         * 
         * @return the delegate map
         * 
         * @exception ServiceException BAD_PARAMETER
         *            If the request specifies a different marshaller
         *            
         */
        Container_1_0 getDelegate(
            Marshaller requestedMarshaller
        ) throws ServiceException{
            if(!(super.marshaller instanceof ExceptionListenerMarshaller)) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unexpected marshaller",
                new BasicException.Parameter("expected", ExceptionListenerMarshaller.class.getName()),
                new BasicException.Parameter("actual", super.marshaller.getClass().getName())
            );
            Marshaller actualMarshaller = ((ExceptionListenerMarshaller)super.marshaller).getDelegate();
            MarshallingContainer container;
            if(actualMarshaller == requestedMarshaller) {
                //
                // Identical manager
                //
                container = this;
            } else {  
                //
                // Manager has changed, e.g. in case of context switch
                //
                TransientContainerId containerId = PersistenceHelper.getTransientContainerId(getDelegate());
                container = (MarshallingContainer)((DataObject_1_0)((DataObjectManager_1_0)actualMarshaller).getObjectById(
                    containerId.getParent()
                )).objGetContainer(
                    containerId.getFeature()
                );
            }
            return container.getDelegate();
        }

    }

}

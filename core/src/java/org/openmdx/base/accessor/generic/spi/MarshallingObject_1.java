/*
 * ====================================================================
 * Project:     openMEX, http://www.openmdx.org/
 * Name:        $Id: MarshallingObject_1.java,v 1.21 2008/12/15 03:15:36 hburger Exp $
 * Description: MarshallingObject_1 class
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:36 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.generic.spi;

import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingFilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller_1_0;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;

/**
 * A marshalling object
 */
public abstract class MarshallingObject_1<M extends CachingMarshaller_1_0>
    extends DynamicallyDelegatingObject_1
{

    /**
     * Constructor
     */
    protected MarshallingObject_1(
        Object_1_0 object,
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
    protected M getMarshaller(){
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
    public void objSetValue(
        String feature,
        Object to
    ) throws ServiceException {
        getDelegate().objSetValue(feature, getMarshaller().unmarshal(to));
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
    public Object objGetValue(
        String feature
    ) throws ServiceException {
        return getMarshaller().marshal(getDelegate().objGetValue(feature));
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
    public List<Object> objGetList(
        String feature
    ) throws ServiceException {
        return new MarshallingList<Object>(
            getMarshaller(),
            getDelegate().objGetList(feature)
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
    public Set<Object> objGetSet(
        String feature
    ) throws ServiceException {
        return new MarshallingSet<Object>(
            getMarshaller(),
            getDelegate().objGetSet(feature)
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
    public SortedMap<Integer,Object> objGetSparseArray(
        String feature
    ) throws ServiceException {
        return new MarshallingSortedMap(
            getMarshaller(),
            getDelegate().objGetSparseArray(feature)
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
    public FilterableMap<String, Object_1_0> objGetContainer(
        String feature
    ) throws ServiceException {
        if(getDelegate() == null) {
            return null;
        }
        return new MarshallingContainer(
            getMarshaller(),
            getDelegate().objGetContainer(feature)
        );
    }


    //--------------------------------------------------------------------------
    // Life Cycle Operations
    //--------------------------------------------------------------------------

    /**
     * The copy operation makes a copy of the object. The copy is located in the
     * scope of the container passed as the first parameter and includes the
     * object's default fetch set.
     *
     * @return    an object initialized from the existing object.
     * 
     * @param     there
     *            the new object's container or <code>null</code>, in which case
     *            the object will not belong to any container until it is moved
     *            to a container.
     * @param     criteria
     *            The criteria is used to add the object to the container or 
     *            <code>null</null>, in which case it is up to the
     *            implementation to define the criteria.
     *
     * @exception ServiceException
     *            if the copy operation fails.
     */
    public Object_1_0 objCopy(
        FilterableMap<String, Object_1_0> there,
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "This Object_1_0 implementation does not support objCopy()",
            new BasicException.Parameter(
                "class",
                getClass().getName()
            )
        );
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
    public void objMove(
        FilterableMap<String, Object_1_0> there,
        String criteria
    ) throws ServiceException {
        FilterableMap<String,Object_1_0> to = there == null ? 
            null : 
            ((MarshallingContainer)there).getDelegate(getMarshaller()); 
        getDelegate().objMove(
            to,
            criteria
        );
    } 


    //--------------------------------------------------------------------------
    // Operations
    //--------------------------------------------------------------------------

    /**
     * Invokes an operation asynchronously.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments object. 
     *
     * @return      a structure with the result's values if the accessor is
     *              going to populate it after the unit of work has committed
     *              or null if the operation's return value(s) will never be
     *              available to the accessor.
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if no unit of work is in progress
     * @exception   ServiceException NOT_SUPPORTED
     *              if either asynchronous calls are not supported by the 
     *              manager or the requested operation is not supportd by the
     *              object.
     * @exception   ServiceException 
     *              if the invocation fails for another reason
     */
    public Structure_1_0 objInvokeOperationInUnitOfWork(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        M marshaller = getMarshaller();
        return (Structure_1_0)marshaller.marshal(
            getDelegate().objInvokeOperationInUnitOfWork(
                operation,
                (Structure_1_0)marshaller.unmarshal(arguments)
            )
        );
    }

    /**
     * Invokes an operation synchronously.
     * <p>
     * Only query operations can be invoked synchronously unless the unit of
     * work is non-optimistic or committing.
     *
     * @param       operation
     *              The operation name
     * @param       arguments
     *              The operation's arguments object. 
     *
     * @return      the operation's return object
     *
     * @exception   ServiceException ILLEGAL_STATE
     *              if a non-query operation is called in an inappropriate
     *        state of the unit of work.
     * @exception   ServiceException NOT_SUPPORTED
     *              if either synchronous calls are not supported by the 
     *        manager or the requested operation is not supportd by the
     *        object.
     * @exception   ServiceException 
     *        if a checked exception is thrown by the implementation or
     *        the invocation fails for another reason.
     */
    public Structure_1_0 objInvokeOperation(
        String operation,
        Structure_1_0 arguments
    ) throws ServiceException {
        Marshaller marshaller = getMarshaller();
        return (Structure_1_0)marshaller.marshal(
            getDelegate().objInvokeOperation(
                operation,
                (Structure_1_0)marshaller.unmarshal(arguments)
            )
        );
    }


    //------------------------------------------------------------------------
    // Event Handling
    //------------------------------------------------------------------------

    /**
     * Add an event listener.
     * 
     * @param feature
     *        restrict the listener to this feature;
     *        or null if the listener is interested in all features
     * @param listener
     *        the event listener to be added
     */
    public void objAddEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException{
        super.objAddEventListener(feature, listener); //... Event marshalling to be added
    }

    /**
     * Remove an event listener.
     * 
     * @param feature
     *        the name of the feature that was listened on,
     *        or null if the listener is interested in all features
     * @param listener
     *        the event listener to be removed
     */
    public void objRemoveEventListener(
        String feature,
        EventListener listener
    ) throws ServiceException{
        super.objRemoveEventListener(feature, listener); // TODO add event marshalling
    }

    /**
     * Add an event listener.
     * 
     * @param feature
     *        the name of the feature that was listened on,
     *        or null if the listener is interested in all features
     * @param listenerType
     *        the type of the event listeners to be returned
     * 
     * @return an array of listenerType containing the matching event
     *         listeners
     */
    public <T extends EventListener> T[] objGetEventListeners(
        String feature,
        Class<T> listenerType
    ) throws ServiceException{
        return super.objGetEventListeners(feature, listenerType); // TODO add event marshalling
    }

    
    //------------------------------------------------------------------------
    // Class MarshallingContainer
    //------------------------------------------------------------------------

    /**
     * Marshalling Container
     */
    protected static class MarshallingContainer 
        extends MarshallingFilterableMap
        implements RefBaseObject
    {

        /**
         * Constructor
         * 
         * @param marshaller
         * @param container
         */
        protected MarshallingContainer(
            Marshaller marshaller,
            FilterableMap<String,Object_1_0> container
        ) throws ServiceException {
            super(marshaller, container);
        }

        /**
         * Implements <code>Serializable</code>
         */
        private static final long serialVersionUID = 3257009873437996080L;

        /**
         * Get the delegate and verifies the marshaller
         * 
         * @param marshaller
         * 
         * @return the delegate map
         * 
         * @exception ServiceException BAD_PARAMETER
         *            If the request specifies a different marshaller
         *            
         */
        FilterableMap<String,Object_1_0> getDelegate(
            Marshaller marshaller
        ) throws ServiceException{
            if(!(super.marshaller instanceof CollectionMarshallerAdapter)) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unexpected marshaller",
                new BasicException.Parameter("expected",CollectionMarshallerAdapter.class.getName()),
                new BasicException.Parameter("actual",super.marshaller.getClass().getName())
            );
            Marshaller delegate = ((CollectionMarshallerAdapter)super.marshaller).getDelegate();
            if(delegate != marshaller) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Delegate marshaller mismatch",
                new BasicException.Parameter("expected",marshaller.getClass().getName()),
                new BasicException.Parameter("actual",delegate.getClass().getName())
            );
            return getDelegate();
        }

        /**
         * Retrieve the delegate in case of a RefContainer instance
         * 
         * @return the delegate in case of a RefContainer instance
         * 
         * @throws UnsupportedOperationException unless the delegate is an instance of RefContainer
         */
        private RefBaseObject refBaseObject(){
            FilterableMap<String,Object_1_0> delegate = getDelegate();
            if(delegate instanceof RefBaseObject) {
                return (RefBaseObject)delegate;
            }
            throw new UnsupportedOperationException(
                "The delegate is not an instance of " + RefBaseObject.class.getName() + ": " + (
                    delegate == null ? "null" : delegate.getClass().getName()
                )
            );
        }
        
        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refImmediatePackage()
         */
        public RefPackage refImmediatePackage() {
            return refBaseObject().refImmediatePackage();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMetaObject()
         */
        public RefObject refMetaObject() {
            return refBaseObject().refMetaObject();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refMofId()
         */
        public String refMofId() {
            return refBaseObject().refMofId();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refOutermostPackage()
         */
        public RefPackage refOutermostPackage() {
            return refBaseObject().refOutermostPackage();
        }

        /* (non-Javadoc)
         * @see javax.jmi.reflect.RefBaseObject#refVerifyConstraints(boolean)
         */
        public Collection<?> refVerifyConstraints(boolean deepVerify) {
            return refBaseObject().refVerifyConstraints(deepVerify);
        }

    }

}

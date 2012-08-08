/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MarshallingObject_1.java,v 1.11 2008/04/21 17:04:25 hburger Exp $
 * Description: MarshallingObject_1 class
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/21 17:04:25 $
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
package org.openmdx.base.accessor.generic.spi;

import java.io.Serializable;
import java.util.EventListener;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingFilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;



/**
 * An abstract delegating object
 */
@SuppressWarnings("unchecked")
public class MarshallingObject_1
    extends DelegatingObject_1 
    implements Serializable
{

    /**
     * Marshalling Container
     */
    static class MarshallingContainer<K,V,M extends FilterableMap<K,?>> 
        extends MarshallingFilterableMap<K,V,M>
    {
    
        /**
         * Constructor
         * 
         * @param marshaller
         * @param container
         */
        public MarshallingContainer(
            Marshaller marshaller,
            M container
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
        M getDelegate(
            Marshaller marshaller
        ) throws ServiceException{
            if(!(super.marshaller instanceof CollectionMarshallerAdapter)) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("expected",CollectionMarshallerAdapter.class.getName()),
                    new BasicException.Parameter("actual",super.marshaller.getClass().getName())
                },
                "Unexpected marshaller"
            );
    		Marshaller delegate = ((CollectionMarshallerAdapter)super.marshaller).getDelegate();
            if(delegate != marshaller) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("expected",marshaller.getClass().getName()),
                    new BasicException.Parameter("actual",delegate.getClass().getName())
                },
                "Delegate marshaller mismatch"
            );
            return super.getDelegate();
        }
        
    }

    /**
     * 
     */
    private static final long serialVersionUID = 3257565092349491254L;

    /**
     * Constructor
     */
    public MarshallingObject_1(
        Object_1_0 object,
        Marshaller marshaller
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
        getDelegate().objSetValue(feature, this.marshaller.unmarshal(to));
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
        return this.marshaller.marshal(getDelegate().objGetValue(feature));
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
    public List objGetList(
        String feature
    ) throws ServiceException {
        return new MarshallingList(
            this.marshaller,
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
    public Set objGetSet(
        String feature
    ) throws ServiceException {
        return new MarshallingSet(
            this.marshaller,
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
    public SortedMap objGetSparseArray(
        String feature
    ) throws ServiceException {
        return new MarshallingSortedMap(
            this.marshaller,
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
    public FilterableMap objGetContainer(
        String feature
    ) throws ServiceException {
        if(getDelegate() == null) {
            return null;
        }
        return new MarshallingContainer(
            this.marshaller,
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
        FilterableMap there,
        String criteria
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            new BasicException.Parameter[]{
                new BasicException.Parameter(
                    "class",
                   getClass().getName()
                )
            },
            "This Object_1_0 implementation does not support objCopy()"
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
        FilterableMap there,
        String criteria
    ) throws ServiceException {        
        getDelegate().objMove(
            there == null ? null : ((MarshallingContainer)there).getDelegate(this.marshaller),
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
        return (Structure_1_0)this.marshaller.marshal(
            getDelegate().objInvokeOperationInUnitOfWork(
                operation,
                (Structure_1_0)this.marshaller.unmarshal(arguments)
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
        return (Structure_1_0)this.marshaller.marshal(
            getDelegate().objInvokeOperation(
                operation,
                (Structure_1_0)this.marshaller.unmarshal(arguments)
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
        super.objRemoveEventListener(feature, listener); //... Event marshalling to be added
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
    public EventListener[] objGetEventListeners(
        String feature,
        Class listenerType
    ) throws ServiceException{
        return super.objGetEventListeners(feature, listenerType); //... Event marshalling to be added
    }
    
    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * @serial
     */
    protected Marshaller marshaller;
            
}

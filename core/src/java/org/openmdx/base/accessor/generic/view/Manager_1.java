/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Manager_1.java,v 1.35 2008/09/18 12:46:43 hburger Exp $
 * Description: SPICE Object Layer: Manager implementation
 * Revision:    $Revision: 1.35 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/18 12:46:43 $
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
package org.openmdx.base.accessor.generic.view;

import java.io.Serializable;
import java.util.List;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_4;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_2;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.generic.spi.MarshallingStructure_1;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1Factory;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_1;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_2;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_4;
import org.openmdx.compatibility.base.event.InstanceCallbackEvent;
import org.openmdx.compatibility.base.event.InstanceCallbackListener;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.resource.cci.Connection_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * Manager_1 implementation.
 * <p>
 * The manager returns the same object for a given object id as long as it is not 
 * garbage collected.
 */
public class Manager_1 
    extends CachingMarshaller
    implements InstanceCallbackListener, Serializable, ObjectFactory_1_4 
{

    /**
     * Constructs a Manager.
     *
     * @param   interaction
     *          the interaction object to be used by this manager
     */
    public Manager_1(
        Connection_1_1 interaction
    ) throws ServiceException{
        this.connection = interaction;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4121130329538180151L;

    /**
     * Aspects replace former view and role patterns
     */
    private static final String ASPECTS_ONLY = "Support for " +
        "org::openmdx::compatibility::view1 and " +
        "org::openmdx::compatibility::role1 has been removed"; 

    /**
     *  
     */
    private Connection_1_1 connection;

    /**
     * Return connection assigned to this manager.
     */
    public Connection_1_0 getConnection(
    ) {
        return this.connection;
    }


    //------------------------------------------------------------------------
    // Implements ModelHolder_1_0
    //------------------------------------------------------------------------

    /**
     * Set model.
     * 
     * @param model The model to set.
     */
    public final void setModel(Model_1_0 model) {
        this.connection.setModel(model);
    }    

    /* (non-Javadoc)
     * @see org.openmdx.model1.accessor.basic.cci.ModelHolder_1_0#getModel()
     */
    public Model_1_0 getModel() {
        return this.connection.getModel();
    }


    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_2
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_2#getConnectionFactory()
     */
    public Connection_1Factory getConnectionFactory() {
        return this.connection instanceof Connection_1_2 ?
            ((Connection_1_2)this.connection).getConnectionFactory() :
                null;
    }

    /**
     * Container managed units of work are either non transactional or part of
     * a bigger unit of work.
     *
     * @return the value of the ContainerManaged property.
     */
    public Boolean hasContainerManagedUnitOfWork(
    ){
        return this.connection instanceof Connection_1_2 ? Boolean.valueOf(
            ((Connection_1_2)this.connection).hasContainerManagedUnitOfWork()
        ) : null;
    }


    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_1
    //------------------------------------------------------------------------

    /**
     * This method is deprecated and will throw a NOT_SUPPORTED exception
     * 
     * @deprecated
     * 
     * @exception   ServiceException    NOT_SUPPORTED
     */
    public Object_1_0 createObject(
      String roleClass,
      String roleId,
      Object_1_0 roleCapable
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            ASPECTS_ONLY
        );
    }

    
    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_0
    //------------------------------------------------------------------------

    /**
     * This method is deprecated and will throw a NOT_SUPPORTED exception
     * 
     * @deprecated
     * 
     * @exception   ServiceException    NOT_SUPPORTED
     */
    public Object_1_0 createObject(
        String objectClass, 
        Object_1_0 initialValues
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            ASPECTS_ONLY
        );
    }

    /**
     * Close the basic accessor.
     * <p>
     * After the close method completes, all methods on the ObjectFactory_1_0
     * instance except isClosed throw a ILLEGAL_STATE RuntimeServiceException.
     */
    public void close(
    ) throws ServiceException {
        if (isClosed()) return;
        this.connection.close();
        this.connection = null;
        super.clear();
    }

    /**
     * Tells whether the object factory has been closed.
     * 
     * @return <code>true</code> if the object factory has been closed
     */
    public boolean isClosed(
    ){
        return this.connection == null;
    }

    /**
     * 
     * @throws ServiceException
     */
    private void validateState(
    ) throws ServiceException{
        if(isClosed()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "The manager is closed"
        ); 
    }

    /**
     * Return the unit of work associated with the current basic accessor.
     *
     * @return  the unit of work
     * @throws ServiceException 
     */
    public UnitOfWork_1_0 getUnitOfWork(
    ) throws ServiceException{
        validateState();
        return this.connection.getUnitOfWork();
    }

    /**
     * Get an object from the object factory.
     * <p>
     * If an object with the given id is already in the cache it is returned,
     * otherwise a new object is returned.
     *
     * @param       accessPath
     *              Access path of object to be retrieved.
     *
     * @return      A persistent object
     */
    private Object_1_0 getObject(
        Path accessPath
    ) throws ServiceException{
        validateState();
        return (Object_1_0)(
                accessPath.size() > 2 && "view".equals(accessPath.get(accessPath.size()-2)) ? 
                    getObject(
                        accessPath.getPrefix(accessPath.size()-2)
                    ).objGetContainer("view").get(accessPath.getBase()) :
                        marshal(this.connection.getObject(accessPath))
        );
    }

    /**
     * Get an object from the object factory.
     * <p>
     * If an object with the given access path is already in the cache it is
     * returned, otherwise a new object is returned.
     *
     * @param       accessPath
     *              Access path of object to be retrieved.
     *
     * @return      A persistent object
     *
     * @exception   ServiceException    ILLEGAL_STATE
     *              if the object factory is closed
     */
    public Object_1_0 getObject(
        Object accessPath
    ) throws ServiceException {
        return accessPath == null ?
            null :
                accessPath instanceof Path ?
                    getObject((Path)accessPath) :
                        getObject(new Path(accessPath.toString()));
    }

    /**
     * Create an object
     *
     * @param       objectClass
     *              The model class of the object to be created
     *
     * @return      an object
     */
    public Object_1_0 createObject(
        String objectClass
    ) throws ServiceException{
        validateState();
        return (Object_1_0)marshal(
            this.connection.createObject(objectClass)
        );
    }

    /**
     * Create a structure
     *
     * @param       type
     *              The type of the structure to be created
     * @param       fieldNames
     *              The names of the structure's fields
     * @param       fieldValues
     *              The structure's field values
     *
     * @return      a structure
     */
    public Structure_1_0 createStructure(
        String type,
        List<String> fieldNames,
        List<?> fieldValues
    ) throws ServiceException {
        validateState();
        return new MarshallingStructure_1(
            this.connection.createStructure(type, fieldNames, fieldValues),
            this
        );
    }

    /**
     * Test whether there is no layer mismatch.
     * 
     * @param initialValues
     * 
     * @return the initialValues' delegate
     */
    Object_1_0 getDelegate(
        Object_1_0 initialValues
    ) throws ServiceException {
        try {
            return ((AbstractObject_1) initialValues).getDelegate();
        } catch (ClassCastException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED, 
                "object extension requires an object instanceof '" +
                AbstractObject_1.class.getName() + 
                "'. This problem is likely to occur in JMI plugins when using extend<X>(refObject).",
                new BasicException.Parameter(
                    "class", 
                    initialValues == null ? null : initialValues.getClass().getName()
                )
            );
        }
    }

    /**
     * This method clones an object
     */
    public Object_1_0 cloneObject(
        Path target, 
        Object_1_0 initialValues, 
        boolean completelyDirty
    ) throws ServiceException {
        validateState();
        return this.connection instanceof Connection_1_4 ? (Object_1_0)marshal(
            ((Connection_1_4)this.connection).cloneObject(
                target, 
                getDelegate(initialValues), 
                completelyDirty
            )
        ) : null;
    }

    /**
     * Clears the cache 
     */
    public void clear(
    ){
        super.clear();
        if(this.connection instanceof Connection_1_4) {
            ((Connection_1_4)this.connection).clear();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3#evict()
     */
    public void evict() {
        if(this.connection instanceof Connection_1_4) {
            ((Connection_1_4)this.connection).evict();
        }
    }


    //------------------------------------------------------------------------
    // Extends CachingMarshaller
    //------------------------------------------------------------------------

    /**
     * Marshals path objects to Object_1_0 objects.
     *
     * @param  source    The object to be marshalled
     * 
     * @return           The marshalled object
     * 
     * @exception        RuntimeServiceException
     *                   DATA_CONVERSION: Object can't be marshalled
     */
    protected Object createMarshalledObject (
        Object source
    ) throws ServiceException{
        validateState();
        return new StandardObject_1(
            (Object_1_0) source,
            this
        );
    }

    /**
     * Marshals an object
     *
     * @param  source    The object to be marshalled
     * 
     * @return           The marshalled object
     * 
     * @exception        ServiceException 
     *                   Object can't be marshalled
     */
    public Object marshal (
        Object source
    ) throws ServiceException{
        validateState();
        return source instanceof Object_1_0 ? super.marshal(source) : source;
    }

    /**
     * Unmarshals an object
     *
     * @param  source   The marshalled object
     * 
     * @return          The unmarshalled object
     * 
     * @exception       ServiceException
     *                  Object can't be unmarshalled
     */
    public Object unmarshal (
        Object source
    ) throws ServiceException{
        validateState();
        return source instanceof AbstractObject_1 ? ((AbstractObject_1)source).getDelegate() : source;
    }


    //------------------------------------------------------------------------
    // Implements InstanceCallbackListener
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.event.InstanceCallbackListener#postCreate(org.openmdx.compatibility.base.event.InstanceCallbackEvent)
     */
    public void postCreate(
        InstanceCallbackEvent event
    ) throws ServiceException {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#postLoad(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void postLoad(
        InstanceCallbackEvent event
    ) throws ServiceException {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preClear(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preClear(
        InstanceCallbackEvent event
    ) throws ServiceException {
        Object key = event.getSource();        
        if(key instanceof Object_1_2) {
            Object_1_2 delegate = (Object_1_2) key;
            if(delegate.objIsInaccessable()) {
                Object value = super.mapping.remove(key);
                if(value instanceof AbstractObject_1) {
                    ((AbstractObject_1)value).setInaccessabilityReason(
                        delegate.getInaccessabilityReason()
                    );
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preDelete(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preDelete(
        InstanceCallbackEvent event
    )throws ServiceException {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.event.InstanceCallbackListener#preStore(org.openmdx.base.event.InstanceCallbackEvent)
     */
    public void preStore(
        InstanceCallbackEvent event
    ) throws ServiceException {
        //
    }

    public void evict(
        Path path
    ){
        super.mapping.remove(path);
    }


}

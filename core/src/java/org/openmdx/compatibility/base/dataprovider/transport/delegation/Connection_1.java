/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Connection_1.java,v 1.46 2008/12/15 03:15:29 hburger Exp $
 * Description: SPICE Object Layer: Manager implementation
 * Revision:    $Revision: 1.46 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/15 03:15:29 $
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
 *   * Redistributions in binary form must reproduce the above copyright
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
package org.openmdx.compatibility.base.dataprovider.transport.delegation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.cci.MappedRecord;
import javax.transaction.UserTransaction;

import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.persistence.spi.OptimisticTransaction_2_0;
import org.openmdx.base.transaction.Synchronization_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_0;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Provider_1_3;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1Factory;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * Connection_1 implementation.
 * <p>
 * A connection can be constructed with the following transaction management policies:
 * <p>
 * <pre>
 *                             transaction    containerManaged    optimistic
 * 
 *  a) optimistic server-side Tx   null           false              true
 *     (e.g. EJB managed) 
 *  b) optimistic client-side Tx  !null           false              true
 *  c) non-optimistic             !null           false              false
 * 
 *  d) external control*           null           true               false 
 * 
 * </pre>
 * <p> 
 * *external control. Transaction management is NOT under the control of the connection. 
 * As a consequence begin() and commit() must not be called on the unit of work, i.e. 
 * are not supported. The synchronization points of externally controlled transactions 
 * are afterBegin() and beforeCompletion().
 * <p>  
 * A non-optimistic connection coordinates user transactions. As a consequence it requires 
 * a provider with an transactionPolicyIsNew=false interaction policy, i.e. a Provider which
 * is not itself transaction coordinator.
 */
public class Connection_1
    extends CachingMarshaller
    implements Connection_1_5
{

    /**
     * Constructor 
     * 
     * @param provider 
     * @param userTransaction 
     * @param optimisticTransaction 
     * @param multithreaded 
     * @param defaultQualifierType UID, UUID, URN, SEQUENCE
     * @param containerManagedUnitOfWork
     * @param transactional 
     * @param optimistic 
     * @throws ServiceException
     */
    private Connection_1(
        Provider_1_0 provider, 
        UserTransaction userTransaction, 
        OptimisticTransaction_2_0 optimisticTransaction, 
        boolean multithreaded, 
        String defaultQualifierType, 
        boolean containerManagedUnitOfWork, 
        boolean transactional, 
        boolean optimistic
    ) throws ServiceException{
        this.multithreaded = multithreaded;
        this.provider = provider;
        this.defaultQualifierType = defaultQualifierType;
        this.containerManagedUnitOfWork = containerManagedUnitOfWork;
        this.transactional = transactional;
        this.optimistic = optimistic;
        this.userTransaction = userTransaction;
        this.optimisticTransaction = optimisticTransaction;
        if(!optimistic && transactional && userTransaction == null) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            new BasicException.Parameter[]{
                new BasicException.Parameter("transactional", transactional),
                new BasicException.Parameter("containerManaged", containerManagedUnitOfWork),
                new BasicException.Parameter("optimistic", optimistic)
            },
            "Transactional non-optimistic units of work require a user transaction"
        );
        if(multithreaded) {
            this.unitOfWork = null;
            this.unitsOfWork = new ConcurrentHashMap<Thread,UnitOfWork_1>();
        } else {
            this.unitOfWork = newUnitOfWork();
            this.unitsOfWork = null;
        }
    }

    /**
     * Constructor
     *
     * @param   providers
     *           the provider connected to this router
     * @param    containerManagedUnitOfWork
     *               defines whether the unit of work is container managed
     * @param    transactionalUnitOfWork
     *               defines whether the unit of work is transactional
     * @param    optimisticUnitOfWork
     *               defines whether the unit of work is optimistic
     * 
     * @throws ServiceException
     */
    public Connection_1(
        Provider_1_0 provider,
        boolean containerManagedUnitOfWork,
        boolean transactionalUnitOfWork,
        boolean optimisticUnitOfWork,
        UserTransaction userTransaction
    ) throws ServiceException{
        this(
            provider, 
            null, // userTransaction
            null, // multithreaded
            true, 
            "UUID", 
            containerManagedUnitOfWork, 
            transactionalUnitOfWork, optimisticUnitOfWork
        );
    }

    /**
     * Constructor
     *
     * @param   providers
     *           the provider connected to this router
     * @param    containerManagedUnitOfWork
     *               defines whether the unit of work is container managed
     * @param defaultQualifierType UID, UUID, URN, SEQUENCE
     * 
     * @throws ServiceException
     */
    public Connection_1(
        Provider_1_0 provider,
        boolean containerManagedUnitOfWork,
        String defaultQualifierType
    ) throws ServiceException{
        this(
            provider, 
            null, // userTransaction
            null, // optimisticTransaction
            true, // multithreaded
            defaultQualifierType, 
            containerManagedUnitOfWork, 
            provider.isTransactionPolicyIsNew(), // transactional
            !containerManagedUnitOfWork // optimistic
        );
    }

    /**
     * Constructor
     *
     * @param   providers
     *           the provider connected to this router
     * @param    containerManagedUnitOfWork
     *               defines whether the unit of work is container managed
     * @param defaultQualifierType UID, UUID, URN, SEQUENCE
     * 
     * @throws ServiceException
     */
    public Connection_1(
        Provider_1_0 provider,
        OptimisticTransaction_2_0 optimisticTransaction,
        String defaultQualifierType
    ) throws ServiceException{
        this(
            provider, 
            null, // userTransaction
            optimisticTransaction,
            true, // multithreaded 
            defaultQualifierType,  
            false, // containerManagedUnitOfWork,
            false, // transactional (false at provider level)
            true // optimistic
        );
    }

    
    /**
     * Constructor 
     *
     * @param provider
     * @param userTransaction
     * @param containerManaged 
     * @param optimistic
     * @param defaultQualifierType UID, UUID, URN, SEQUENCE
     * @throws ServiceException
     */
    public Connection_1(
        Provider_1_0 provider,
        UserTransaction userTransaction,
        boolean containerManaged, 
        boolean optimistic, 
        String defaultQualifierType
    ) throws ServiceException{
        this(
            provider, 
            userTransaction, 
            null, // optimisticTransaction
            true, // multithreaded
            defaultQualifierType, 
            containerManaged, 
            provider.isTransactionPolicyIsNew(), // transactional 
            optimistic
        );
    }


    /**
     * Constructs a Manager.
     *
     * @param   providers
     *           the provider connected to this router
     * @param    containerManagedUnitOfWork
     *               defines whether the unit of work is container managed
     * 
     * @throws ServiceException
     */
    public Connection_1(
        Provider_1_0 provider,
        boolean containerManagedUnitOfWork
    ) throws ServiceException{
        this(
            provider, 
            null, // userTransaction
            null, // optimistictRANSACTION
            true, // multithreaded
            "UUID", // defaultQualifierType 
            containerManagedUnitOfWork, // transactional
            provider.isTransactionPolicyIsNew(),
            !containerManagedUnitOfWork // optimistic
        );
    }

    /**
     * Constructs a non-optimistic Manager
     *
     * @param provider
     * @param userTransaction
     * @param optimistic
     * 
     * @throws ServiceException
     */
    public Connection_1(
        Provider_1_0 provider,
        UserTransaction userTransaction,
        boolean optimistic
    ) throws ServiceException{
        this(
            provider, 
            userTransaction, 
            null, // optimisticTransaction
            true, // multithreaded
            "UUID", // containerManagedUnitOfWork
            false, // transactional
            provider.isTransactionPolicyIsNew(), 
            optimistic
        );
    }

    /**
     * Tells whether the connection is multi-threaded.
     */
    private final boolean multithreaded;
    
    /**
     * Aspects replace former view and role patterns
     */
    private static final String ASPECTS_ONLY = "Support for " +
        "org::openmdx::compatibility::view1 and " +
        "org::openmdx::compatibility::role1 has been removed"; 

    /**
     * Retrieves the defaultQualifierType.
     * 
     * @return the defaultQualifierType
     */
    public String getDefaultQualifierType() {
        return this.defaultQualifierType;
    }

    /**
     * Unit of work factory method
     * 
     * @return a new unit of work
     */
    protected UnitOfWork_1 newUnitOfWork(
    ){
        return new UnitOfWork_1(
            this.provider, 
            this.transactional, 
            this.containerManagedUnitOfWork, 
            this.optimistic, 
            this.userTransaction, 
            this.optimisticTransaction
        );
    }

    //------------------------------------------------------------------------
    // Implements Connection_1_5
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_5#isMultithreaded()
     */
    public boolean isMultithreaded() {
        return this.multithreaded;
    }

    /**
     * Retrieve model.
     *
     * @return Returns the model.
     */
    public final Model_1_0 getModel() {
        return this.model;
    }

    /**
     * Set model.
     * 
     * @param model The model to set.
     */
    public final void setModel(Model_1_0 model) {
        this.model = model;
    }    

    
    //------------------------------------------------------------------------
    // Implements Connection_1_2
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_3#getPrincipalChain()
     */
    public List<String> getPrincipalChain() {
        return this.provider instanceof Provider_1_3 ?
            ((Provider_1_3)this.provider).getPrincipalChain() :
            null;
    }

    
    //------------------------------------------------------------------------
    // Implements Connection_1_2
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1_2#getConnectionFactory()
     */
    public Connection_1Factory getConnectionFactory() {
        return this.provider instanceof Connection_1Factory ?
            (Connection_1Factory) this.provider : 
                null;
    }

    /**
     * Container managed units of work are either non transactional or part of
     * a bigger unit of work.
     *
     * @return the value of the ContainerManaged property.
     */
    public final boolean hasContainerManagedUnitOfWork(
    ){
        return this.containerManagedUnitOfWork;
    }

    //------------------------------------------------------------------------
    // Implements Connection_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.resource.cci.Connection_1_0#getMetaData()
     */
    public Structure_1_0 getMetaData() throws ServiceException {
        return null; //... Return connection meta data
    }

    //------------------------------------------------------------------------
    // Implements Connection_1_4
    //------------------------------------------------------------------------

    /**
     * This method clones an object
     * @param original
     * @return the clone
     * 
     * @throws ServiceException
     */
    public Object_1_0 cloneObject(
        Path target, 
        Object_1_0 original, 
        boolean completelyDirty
    ) throws ServiceException {
        validateState();
        if(original instanceof Object_1) {
            Object_1 that = (Object_1) original;
            if(target != null && that.objIsPersistent() && !that.objIsNew()) {
                Object_1 clone = getObjectById(target);
                clone.propagate(
                    that,
                    completelyDirty
                );
                return clone;
            } else {
                return new Object_1(
                    target,
                    that,
                    completelyDirty
                );
            }
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("object class", original == null ? null : original.getClass().getName())
                },
                "object extension requires an object instanceof '" + Object_1.class.getName() + "'. " + 
                "This problem is likely to occur in JMI plugins when using refCloneObject(refObject)"
            );      
        }
    }

    public void evict(
    ){
        for(
                Iterator<?> i = this.mapping.values().iterator();
                i.hasNext();
        ){
            Object o = i.next();
            if(o instanceof Evictable) {
                ((Evictable)o).evict();
            }
        }
    }

    public void clear(
    ){
        if(this.multithreaded) {
            this.unitsOfWork.clear();
        }
        super.clear();
    }

    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_0
    //------------------------------------------------------------------------

    /**
     * Close the basic accessor.
     * <p>
     * After the close method completes, all methods on the ObjectFactory_1_0
     * instance except isClosed throw an ILLEGAL_STATE ServiceException.
     */
    public void close(
    ) throws ServiceException{
        if(!isClosed()) {
            this.provider.close();
            this.provider = null;
            clear();      
        }
    }

    /**
     * 
     */
    public boolean isClosed(
    ){  
        return this.provider == null;
    }

    private final void validateState(
    ) throws ServiceException{
        if(isClosed()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            null,
            "Connection is closed"
        );  
    }

    /**
     * Return the unit of work associated with the current basic accessor.
     *
     * @return  the unit of work
     * @throws ServiceException 
     */
    public UnitOfWork_1 getUnitOfWork(
    ) throws ServiceException{
        validateState();
        if(this.multithreaded) {
            Thread thread = Thread.currentThread();
            UnitOfWork_1 unitOfWork = this.unitsOfWork.get(thread);
            if(unitOfWork == null) {
                this.unitsOfWork.put(
                    thread,
                    unitOfWork = newUnitOfWork()
                );
            }
            return unitOfWork;
        } else {
            return this.unitOfWork;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.resource.cci.Connection_1_0#getSynchronization()
     */
    public Synchronization_1_0 getSynchronization() throws ServiceException {
        validateState();
        return this.provider;
    }

    /**
     * Get an object from the basic accessor.
     * <p>
     * If an object with the given id is already in the cache it is returned,
     * otherwise a new object is returned.
     *
     * @param       objectId
     *              Identity of object to be retrieved.
     *
     * @return      A managed object
     */
    private Object_1 getObjectById(
        Path objectId
    ) throws ServiceException{
        validateState();
        return (Object_1)marshal(objectId);
    }

    /**
     * Get an object from the object factory.
     * <p>
     * If an object with the given acess path is already in the cache it is
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
                    getObjectById((Path)accessPath) :
                        getObjectById(new Path(accessPath.toString()));
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
        return new Object_1(objectClass, this);
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
    ) throws ServiceException{
        validateState();
        return new ListStructure_1(type,fieldNames,fieldValues);
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
        String[] fieldNames,
        Object[] fieldValues
    ) throws ServiceException{
        validateState();
        return new ListStructure_1(type,fieldNames,fieldValues);
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
    // Implements Manager_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#fetched(org.openmdx.compatibility.base.naming.Path, javax.resource.cci.MappedRecord)
     */
    public void fetched(
        Path accessPath, 
        MappedRecord attributes
    ) throws ServiceException {
        if(isClosed()) return;
        ((Object_1)this.marshal(accessPath)).fetched(attributes);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#updated(org.openmdx.compatibility.base.naming.Path, javax.resource.cci.MappedRecord)
     */
    public void updated(
        Path accessPath, 
        MappedRecord attributes
    ) throws ServiceException {
        if(isClosed()) return;
        if(this.containsKey(accessPath))((Object_1)this.marshal(accessPath)).updated(attributes);
    } 

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#invalidate(org.openmdx.compatibility.base.naming.Path, boolean)
     */
    public void invalidate(
        Path accessPath, 
        boolean makeInaccessable
    ) throws ServiceException {
        if(isClosed()) return;
        getObjectById(
            accessPath.getPrefix(accessPath.size() - 2)
        ).setExistence(
            accessPath, 
            false
        );
        for (
            Iterator<Entry<Object, Object>> i = super.mapping.entrySet().iterator();
            i.hasNext();
        ){
            Map.Entry<?,?> e = i.next();
            if (((Path)e.getKey()).startsWith(accessPath)){
                ((Object_1)e.getValue()).invalidate(makeInaccessable);
                i.remove();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.transport.spi.Manager_1_0#move(org.openmdx.compatibility.base.naming.Path, org.openmdx.compatibility.base.naming.Path)
     */
    public void move(
        Path path,
        Path newValue
    ){
        if(isClosed()){
            path.setTo(newValue);   
        } else {
            Object object = super.mapping.remove(path);
            path.setTo(newValue);
            if(object != null) cache(path, object);
        }
    }

    public boolean containsKey(
        Path path
    ){
        return mapping.containsKey(path);
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
     * @exception        ServiceException
     *                   Object can't be marshalled
     */
    protected Object createMarshalledObject (
        Object source
    ) throws ServiceException {
        return new Object_1(
            (Path)source,
            this,
            this.provider
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
     *                   DATA_CONVERSION: Object can't be marshalled
     */
    public Object marshal (
        Object source
    ) throws ServiceException{
        validateState();
        return source instanceof Path ? super.marshal(source) : source;
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
        return source instanceof Object_1_0 ? 
            ((Object_1_0)source).objGetPath() : 
                source;
    }


    //------------------------------------------------------------------------
    // Class members
    //------------------------------------------------------------------------

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3977865059621680436L;


    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     *  
     */
    private Model_1_0 model = null;

    /**
     *
     */ 
    private UnitOfWork_1 unitOfWork;

    /**
     *
     */ 
    private ConcurrentMap<Thread,UnitOfWork_1> unitsOfWork; 

    /**
     *
     */
    private Provider_1_0 provider;

    /**
     * 
     */
    private final boolean transactional;

    /**
     * 
     */
    private final boolean optimistic;

    /**
     * 
     */
    private final UserTransaction userTransaction;

    /**
     * 
     */
    private final OptimisticTransaction_2_0 optimisticTransaction;

    
    /**
     * UID, UUID, URN, SEQUENCE
     */
    private String defaultQualifierType;

    /**
     * Container managed units of work are either non transactional or part of
     * a bigger unit of work.
     */
    private final boolean containerManagedUnitOfWork;

}

/**********************************************************************
Copyright (c) 2002 Kelly Grizzle and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 

Contributors:
2003 Erik Bengtson - removed exist() operation
2003 Andy Jefferson - added localiser
2003 Erik Bengtson - added new constructor for App ID
2003 Erik Bengtson - fixed loadDefaultFetchGroup to call jdoPostLoad
2003 Erik Bengtson - fixed evict to call jdoPreClear
2004 Andy Jefferson - converted to use Logger
2004 Andy Jefferson - reordered methods to put in categories, split String utilities across to StringUtils.
2004 Andy Jefferson - added Lifecycle Listener callbacks
2004 Andy Jefferson - removed JDK 1.4 methods so that we support 1.3 also
2005 Martin Taal - Contrib of detach() method for "detachOnClose" functionality.
2007 Xuan Baldauf - Contrib of initialiseForHollowPreConstructed(), notifyMadePersistentClean() (needed by DB4O plugin). Contribution of some assertions and analysis-comments. 
2007 Xuan Baldauf - Contrib of internalAreAllFieldsLoaded(), internalIsAtLeastOneFieldDirty(), internalIsAtLeastOneFieldDirtyVerify(), createDirtyFieldsBitmapCopy() (needed by DB4O plugin). 
2007 Xuan Baldauf - remove the fields "jdoLoadedFields" and "jdoModifiedFields".  
2007 Xuan Baldauf - remove the fields "retrievingDetachedState" and "resettingDetachedState".
2007 Xuan Baldauf - remove the field "updatingEmbeddedFieldsWithOwner"
    ...
 **********************************************************************/
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdo;

import javax.jdo.spi.StateManager;

/**
 * Implementation of the StateManager.
 * Implemented here as one StateManager per Object so adds on functionality particular 
 * to each object. All PersistenceCapable objects will have a StateManager when they 
 * have had communication with the PersistenceManager. They will typically always have
 * an identity also. The exception to that is for embedded/serialised objects.
 * 
 * <H3>Embedded/Serialised Objects</H3>
 * An object that is being embedded/serialised in an owning object will NOT have an identity
 * unless the object is subject to a makePersistent() call also. When an object
 * is embedded/serialised and a field is changed, the field will NOT be marked as dirty (unless
 * it is also an object in its own right with an identity). When a field is changed
 * any owning objects are updated so that they can update their tables accordingly.
 *
 * <H3>Performance and Memory</H3>
 * StateManagers are very performance-critical, because for each PersistentCapable object made persistent,
 * there will be one StateManager instance, adding up to the total memory footprint of that object.
 * In heap profiling analysis, JDOStateManagerImpls showed to consume bytes 169 per StateManager by itself
 * and about 500 bytes per StateManager when taking PC-individual child-object (like the OID) referred
 * by the StateManager into account. With small Java objects this can mean a substantial memory overhead and
 * for applications using such small objects can be critical. For this reason the StateManager should always
 * be minimal in memory consumption.
 *  
 * @version $Revision: 1.1 $
 */
abstract class JDOStateManagerImpl extends AbstractStateManager implements StateManager
{
//    private static final JDOImplHelper HELPER;
//
//    private static final SingleTypeFieldManager HOLLOWFIELDMANAGER = new SingleTypeFieldManager();
//
//    /** Flag for {@link #miscFlags} whether we are retrieving detached state from the detached object. */
//    private static final byte MISC_RETRIEVING_DETACHED_STATE = (byte) (1<<7);
//
//    /** Flag for {@link #miscFlags} whether we are resetting the detached state. */
//    private static final byte MISC_RESETTING_DETACHED_STATE  = (byte) (1<<6);
//
//    /** Flag whether this SM is updating the ownership of its embedded/serialised field(s). */
//    private static final byte MISC_UPDATING_EMBEDDING_FIELDS_WITH_OWNER  = (byte) (1<<5);
//
//    /** Bit-packed flags that have been separate booleans (effectively separate bytes) before. */
//    private byte miscFlags;
//
//    /** Flags for object state stored with the object. */
//    private byte jdoFlags;
//
//    /** Image of the PersistenceCapable instance when the instance is enlisted in the transaction. */
//    private PersistenceCapable savedImage = null;
//
//    /** Flags of the PersistenceCapable instance when the instance is enlisted in the transaction. */
//    private byte savedFlags;
//
//    /** Loaded fields of the PersistenceCapable instance when the instance is enlisted in the transaction. */
//    private boolean[] savedLoadedFields = null;
//
//    /** if the PersistenceCapable instance is new and was flushed to the datastore. */
//    private boolean flushedNew = false;
//
//    /** state for transitions of activities. */
//    private ActivityState activity = ActivityState.NONE;
//
//    private boolean changingState = false;
//
//    private boolean postLoadPending = false;
//
//    private boolean flushing = false;
//
//    private boolean disconnecting = false;
//
//    /** Flag to indicate if the object is in the process of being detached. */
//    private boolean detaching = false;
//
//    /** PC object that we are in the process of detaching. */
//    private PersistenceCapable detachingPC = null;
//
//    /** Whether the object is being attached. */
//    private boolean attaching = false;
//
//    /** Attached PC object, when this SM refers to a detached PC (SM is temporarily connected to get the values) */
//    private PersistenceCapable attachedPC = null;
//
//    /** List of StateManagers that we must notify when we have completed inserting our record. */
//    private List insertionNotifyList = null;
//
//    /** Fields of this object that we must update when notified of the insertion of the related objects. */
//    private Map fieldsToBeUpdatedAfterObjectInsertion = null;
//
//    /** List of owners when embedded. */
//    private List embeddedOwners = null;
//
//    /**
//     * Map of external field values (added to our object table where we dont have relations to them - unidirectional).
//     * This will include FK and order mappings for 1-N uni.
//     */
//    private HashMap externalFieldValuesByMapping = null;
//
//    static
//    {
//        HELPER = (JDOImplHelper) AccessController.doPrivileged(new PrivilegedAction()
//        {
//            public Object run()
//            {
//                try
//                {
//                    return JDOImplHelper.getInstance();
//                }
//                catch (SecurityException e)
//                {
//                    throw new JDOFatalUserException(LOCALISER.msg("StateManager.SecurityProblem"), e);
//                }
//            }
//        });
//    }
//
//    /**
//     * Basic constructor. Delegates to the superclass.
//     * @param om The ObjectManager
//     * @param cmd the metadata for the class.
//     */
//    public JDOStateManagerImpl(ObjectManager om, AbstractClassMetaData cmd)
//    {
//        super(om, cmd);
//    }
//
//    /**
//     * Initialises a state manager to manage a hollow instance having the given object ID and the given
//     * (optional) field values. This constructor is used for creating new instances of existing persistent
//     * objects, and consequently shouldnt be used when the StoreManager controls the creation of such objects
//     * (such as in an ODBMS).
//     * @param id the JDO identity of the object.
//     * @param fv the initial field values of the object (optional)
//     * @param pcClass Class of the object that this will manage the state for
//     */
//    public void initialiseForHollow(Object id, FieldValues fv, Class pcClass)
//    {
//        myID = id;
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW);
//        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
//        if (id instanceof OID || id == null)
//        {
//            // Create new PC
//            myPC = HELPER.newInstance(pcClass, this);
//        }
//        else
//        {
//            // Create new PC, and copy the key class to fields
//            myPC = HELPER.newInstance(pcClass, this, myID);
//        }
//
//        if (fv != null)
//        {
//            loadFieldValues(fv);
//        }
//    }
//
//    /**
//     * Initialises a state manager to manage a HOLLOW / P_CLEAN instance having the given FieldValues.
//     * This constructor is used for creating new instances of existing persistent objects using application 
//     * identity, and consequently shouldnt be used when the StoreManager controls the creation of such objects
//     * (such as in an ODBMS).
//     * @param fv the initial field values of the object.
//     * @param pcClass Class of the object that this will manage the state for
//     */
//    public void initialiseForHollowAppId(FieldValues fv, Class pcClass)
//    {
//        if (cmd.getIdentityType() != IdentityType.APPLICATION)
//        {
//            throw new JDOFatalUserException("This constructor is only for objects using application identity.");
//        }
//
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW);
//        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
//        myPC = HELPER.newInstance(pcClass, this); // Create new PC
//        if (myPC == null)
//        {
//            if (!HELPER.getRegisteredClasses().contains(pcClass))
//            {
//                // probably never will get here, as JDOImplHelper.newInstance() internally already throws
//                // JDOFatalUserException when class is not registered 
//                throw new JDOFatalUserException(LOCALISER.msg("StateManager.ClassNotRegistered", pcClass.getName()));
//            }
//            else
//            {
//                // Provide advisory information since we can't create an instance of this class, so maybe they
//                // have an error in their data ?
//                throw new JDOFatalUserException(LOCALISER.msg("StateManager.ClassNotConstructable", pcClass.getName()));
//            }
//        }
//
//        loadFieldValues(fv); // as a minimum the PK fields are loaded here
//
//        // Create the ID now that we have the PK fields loaded
//        myID = myPC.jdoNewObjectIdInstance();
//        if (!cmd.usesSingleFieldIdentityClass())
//        {
//            myPC.jdoCopyKeyFieldsToObjectId(myID);
//        }
//    }
//
//    /**
//     * Initialises a state manager to manage the given hollow instance having the given object ID.
//     * Unlike the {@link #initialiseForHollow} method, this method does not create a new instance and instead 
//     * takes a pre-constructed instance.
//     * @param id the identity of the object.
//     * @param pc the object to be managed.
//     */
//    public void initialiseForHollowPreConstructed(Object id, PersistenceCapable pc)
//    {
//        myID = id;
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW);
//        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
//        myPC = pc;
//
//        replaceStateManager(this); // Assign this StateManager to the PC
//        myPC.jdoReplaceFlags();
//
//        // TODO Add to the cache
//    }
//
//    /**
//     * Initialises a state manager to manage the passed persistent instance having the given object ID.
//     * Used where we have retrieved a PC object from a datastore directly (not field-by-field), for example on
//     * an object datastore. This initialiser will not add StateManagers to all related PCs. This must be done by
//     * any calling process. This simply adds the StateManager to the specified object and records the id, setting
//     * all fields of the object as loaded.
//     * @param id the identity of the object.
//     * @param pc The object to be managed
//     */
//    public void initialiseForPersistentClean(Object id, PersistenceCapable pc)
//    {
//        myID = id;
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_CLEAN);
//        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
//        myPC = pc;
//
//        replaceStateManager(this); // Assign this StateManager to the PC
//        myPC.jdoReplaceFlags();
//
//        // Mark all fields as loaded
//        for (int i=0; i<loadedFields.length; ++i)
//        {
//            loadedFields[i] = true;
//        }
//
//        // Add the object to the cache
//        myOM.putObjectIntoCache(this, true, true);
//    }
//
//    /**
//     * Initialises a state manager to manage a PersistenceCapable instance that will be EMBEDDED/SERIALISED 
//     * into another PersistenceCapable object. The instance will not be assigned an identity in the process 
//     * since it is a SCO.
//     * @param pc The PersistenceCapable to manage (see copyPc also)
//     * @param copyPc Whether the SM should manage a copy of the passed PC or that one
//     */
//    public void initialiseForEmbedded(PersistenceCapable pc, boolean copyPc)
//    {
//        pcObjectType = EMBEDDED_PC; // Default to an embedded PC object
//        myID = null; // It is embedded at this point so dont need an ID since we're not persisting it
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NEW);
//        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
//
//        myPC = pc;
//        replaceStateManager(this); // Set SM for embedded PC to be this
//        if (copyPc)
//        {
//            // Create a new PC with the same field values
//            PersistenceCapable pcCopy = myPC.jdoNewInstance(this);
//            pcCopy.jdoCopyFields(myPC, getAllFieldNumbers());
//
//            // Swap the managed PC to be the copy and not the input
//            pcCopy.jdoReplaceStateManager(this);
//            myPC = pcCopy;
//            disconnectClone(pc);
//        }
//
//        // Mark all fields as loaded since we are using the passed PersistenceCapable
//        for (int i=0;i<loadedFields.length;i++)
//        {
//            loadedFields[i] = true;
//        }
//    }
//
//    /**
//     * Initialises a state manager to manage a transient instance that is becoming newly persistent.
//     * A new object ID for the instance is obtained from the store manager and the object is inserted
//     * in the data store.
//     * <p>This constructor is used for assigning state managers to existing
//     * instances that are transitioning to a persistent state.
//     * @param pc the instance being make persistent.
//     * @param preInsertChanges Any changes to make before inserting
//     */
//    public void initialiseForPersistentNew(PersistenceCapable pc, FieldValues preInsertChanges)
//    {
//        myPC = pc;
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NEW);
//        jdoFlags = PersistenceCapable.READ_OK;
//        for (int i=0; i<loadedFields.length; ++i)
//        {
//            loadedFields[i] = true;
//        }
//
//        replaceStateManager(this); // Assign this StateManager to the PC
//        myPC.jdoReplaceFlags();
//
//        saveFields();
//
//        // Populate all fields that have "value-strategy" and are not datastore populated
//        populateStrategyFields();
//
//        if (preInsertChanges != null)
//        {
//            // Apply any pre-insert field updates
//            preInsertChanges.fetchFields(this);
//        }
//
//        if (cmd.getIdentityType() == IdentityType.APPLICATION)
//        {
//            //load key fields from Application Id instance to PC instance
//
//            //if a primary key field is of type PersistenceCapable, it must first be persistent
//            for (int fieldNumber = 0; fieldNumber < getAllFieldNumbers().length; fieldNumber++)
//            {
//                AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(fieldNumber);
//                if (fmd.isPrimaryKey())
//                {
//                    if (myOM.getMetaDataManager().getMetaDataForClass(fmd.getType(), getObjectManager().getClassLoaderResolver()) != null)
//                    {
//                        synchronized (currFMmonitor)
//                        {
//                            FieldManager prevFM = currFM;
//                            try
//                            {
//                                currFM = new SingleValueFieldManager();
//                                myPC.jdoProvideField(fieldNumber);
//                                PersistenceCapable pkFieldPC = (PersistenceCapable) ((SingleValueFieldManager) currFM).fetchObjectField(fieldNumber);
//                                if (pkFieldPC == null)
//                                {
//                                    throw new JDOUserException(LOCALISER.msg("StateManager.PrimaryKeyFieldIsNull",fmd.getFullFieldName()));
//                                }
//                                if (!myOM.getApiAdapter().isPersistent(pkFieldPC))
//                                {
//                                    // Note that this can cause the insert of our object being managed by this SM via flush() when bidir relation
//                                    myOM.persistObjectInternal(pkFieldPC, null);
//                                }
//                            }
//                            finally
//                            {
//                                currFM = prevFM;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        /* Set the identity
//         * This must come after the above block, because in identifying relationships
//         * the PK FK associations must be persisted before, otherwise we
//         * don't have an id assigned to the PK FK associations
//         */        
//        setIdentity();
//
//        if (this.getObjectManager().getTransaction().isActive())
//        {
//            myOM.enlistInTransaction(this);
//        }
//
//        // Now in PERSISTENT_NEW so call any callbacks/listeners
//        getCallbackHandler().postCreate(myPC);
//    }
//
//    /**
//     * Initialises a state manager to manage a Transactional Transient instance.
//     * A new object ID for the instance is obtained from the store manager and the object is inserted in the data store.
//     * <p>
//     * This constructor is used for assigning state managers to Transient
//     * instances that are transitioning to a transient clean state.
//     * @param pc the instance being make persistent.
//     */
//    public void initialiseForTransactionalTransient(PersistenceCapable pc)
//    {
//        myPC = pc;
//        myLC = null;
//        jdoFlags = PersistenceCapable.READ_OK;
//        for (int i=0; i<loadedFields.length; ++i)
//        {
//            loadedFields[i] = true;
//        }
//        myPC.jdoReplaceFlags();
//
//        // Populate all fields that have "value-strategy" and are not datastore populated
//        populateStrategyFields();
//
//        // Set the identity
//        setIdentity();
//
//        // for non transactional read, tx might be not active
//        // TODO add verification if is non transactional read = true
//        if (myOM.getTransaction().isActive())
//        {
//            myOM.enlistInTransaction(this);
//        }
//    }
//
//    /**
//     * Initialises the StateManager to manage a PersistenceCapable object in detached state.
//     * @param pc the detach object.
//     * @param id the identity of the object.
//     * @param version the detached version
//     * @since 1.1
//     */
//    public void initialiseForDetached(PersistenceCapable pc, Object id, Object version)
//    {
//        this.myID = id;
//        this.myPC = pc;
//        setVersion(version);
//
//        // This lifecycle state is not always correct. It is certainly "detached"
//        // but we dont know if it is CLEAN or DIRTY. We need this setting here since all objects
//        // have a lifecycle state and other methods e.g isPersistent() depend on it.
//        this.myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.DETACHED_CLEAN);
//
//        this.myFP = null;
//
//        this.myPC.jdoReplaceFlags();
//    }
//
//    /**
//     * Initialise to create a StateManager for a PersistenceCapable object, assigning the specified id to the object. 
//     * This is used when getting objects out of the L2 Cache, where they have no StateManager assigned, and returning 
//     * them as associated with a particular PM.
//     * @param pc The PersistenceCapable object
//     * @param id Id to assign to the PersistenceCapable object
//     * @param loaded The list of loaded fields (when put in the cache)
//     * @param pcClass Class of the object that this will manage the state for
//     */
//    public void initialiseForCachedPC(PersistenceCapable pc, Object id, boolean loaded[],Class pcClass)
//    {
//        // Create a new copy of the input object type, performing the majority of the initialisation
//        initialiseForHollow(id, null, pcClass);
//
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_CLEAN);
//        jdoFlags = PersistenceCapable.READ_OK;
//
//        synchronized(pc)
//        {
//            // Synchronise the L2 cached object while we grab its fields.
//            // Copy the fields from the input object to our copy that will be returned to the user
//            pc.jdoReplaceStateManager(this);
//            myPC.jdoCopyFields(pc, getAllFieldNumbers());
//
//            // Reinstate the original loadedFields list
//            for (int i=0; i<loadedFields.length; i++)
//            {
//                loadedFields[i] = loaded[i];
//            }
//
//            // Mark all relationships as not yet loaded since we can't maintain those whilst cached.
//            for (int i=0; i<cmd.getPersistenceCapableFieldNumbers().length; i++)
//            {
//                loadedFields[cmd.getPersistenceCapableFieldNumbers()[i]] = false;
//            }
//            
//            int[] secondClassMutableFieldNumbers = getSecondClassMutableFieldNumbers();
//            for (int i=0; i<secondClassMutableFieldNumbers.length; i++)
//            {
//                loadedFields[secondClassMutableFieldNumbers[i]] = false;
//            }
//
//            // Disconnect the input object
//            disconnectClone(pc);
//        }
//
//        if (isFetchPlanLoaded())
//        {
//            // Should we call postLoad when getting the object out of the L2 cache ? Seems incorrect IMHO
//            postLoad();
//        }
//    }
//
//    /**
//     * Look to the database to determine which class this object is. This parameter is a hint. Set false, if it's
//     * already determined the correct pcClass for this pc "object" in a certain
//     * level in the hierarchy. Set to true and it will look to the database.
//     * @param fv the initial field values of the object.
//     */
//    public void checkInheritance(FieldValues fv)
//    {
//        // Inheritance case, check the level of the instance
//        ClassLoaderResolver clr = myOM.getClassLoaderResolver();
//        String className = getStoreManager().getClassNameForObjectID(myID, clr, myOM);
//        if (className == null)
//        {
//            // className is null when id class exists, and object has been validated and doesn't exist.
//            throw new JPOXObjectNotFoundException(LOCALISER.msg("StateManager.ObjectDoesntExist"), myID);
//        }
//        else if (!cmd.getFullClassName().equals(className))
//        {
//            Class pcClass;
//            try
//            {
//                //load the class and make sure the class is initialized
//                pcClass = getObjectManager().getClassLoaderResolver().classForName(className, myID.getClass().getClassLoader(), true);
//                cmd = myOM.getMetaDataManager().getMetaDataForClass(pcClass, getObjectManager().getClassLoaderResolver());
//            }
//            catch (ClassNotResolvedException e)
//            {
//                JPOXLogger.JDO.warn(LOCALISER.msg("StateManager.GetObjectByIdClassNotFound", myID));
//                throw new JDOUserException(LOCALISER.msg("StateManager.GetObjectByIdClassNotFound", myID), e);
//            }
//            if (cmd == null)
//            {
//                throw new JDOFatalUserException(LOCALISER.msg("StateManager.NotPersistableClassError", pcClass));
//            }
//            if (cmd.getIdentityType() != IdentityType.APPLICATION)
//            {
//                throw new JDOFatalUserException("This method should only be used for objects using application identity.");
//            }
//            myFP = myOM.getFetchPlan().manageFetchPlanForClass(cmd);
//
//            initialiseFieldInformation();
//
//            // Create new PC at right inheritance level
//            myPC = HELPER.newInstance(pcClass, this);
//            if (myPC == null)
//            {
//                throw new JDOFatalUserException(LOCALISER.msg("StateManager.ClassNotRegistered", cmd.getFullClassName()));
//            }
//
//            // Note that this will mean the fields are loaded twice (loaded earlier in this method)
//            // and also that postLoad will be called twice
//            loadFieldValues(fv);
//
//            // Create the id for the new PC
//            myID = myPC.jdoNewObjectIdInstance();
//            if (!cmd.usesSingleFieldIdentityClass())
//            {
//                myPC.jdoCopyKeyFieldsToObjectId(myID);
//            }
//        }
//    }
//
//    /**
//     * Convenience method to populate all fields in the PC object that have "value-strategy" specified
//     * and that aren't datastore attributed. This applies not just to PK fields (where it is most
//     * useful to use value-strategy) but also to any other field. Fields are populated only if they are null
//     * This is called once on a PC object, when makePersistent is called.
//     */
//    private void populateStrategyFields()
//    {
//        int totalFieldCount = cmd.getNoOfInheritedManagedFields() + cmd.getNoOfManagedFields();
//        DatastoreClass table = null;
//        if (!cmd.isEmbeddedOnly() && getStoreManager().usesDatastoreClass())
//        {
//            table = getStoreManager().getDatastoreClass(cmd.getFullClassName(), myOM.getClassLoaderResolver());
//        }
//
//        for (int fieldNumber =0; fieldNumber < totalFieldCount; fieldNumber++)
//        {
//            AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(fieldNumber);
//            IdentityStrategy strategy = fmd.getValueStrategy();
//
//            // Check for the strategy, and if it is a datastore attributed strategy
//            if (strategy != null && !getStoreManager().isStrategyDatastoreAttributed(strategy, false))
//            {
//                // Assign the strategy value where required.
//                // Default JDO2 behaviour is to always provide a strategy value when it is marked as using a strategy
//                boolean applyStrategy = true;
//                if (!fmd.getType().isPrimitive() && strategy != null &&
//                    fmd.hasExtension("strategy-when-notnull") &&
//                    fmd.getValueForExtension("strategy-when-notnull").equalsIgnoreCase("false") &&
//                    this.provideField(fieldNumber) != null)
//                {
//                    // JPOX allows an extension to only provide a value-strategy value where the field is null at persistence.
//                    applyStrategy = false;
//                }
//
//                if (applyStrategy)
//                {
//                    // Apply a strategy value for this field
//                    DatastoreClass fieldTable = null;
//                    if (getStoreManager().usesDatastoreClass())
//                    {
//                        fieldTable = table.getBaseDatastoreClassWithField(fmd);
//                    }
//                    Object obj = getStoreManager().getStrategyValue(myOM, fieldTable, cmd, fieldNumber);
//                    this.replaceField(fieldNumber, obj, true);
//                }
//            }
//        }
//    }
//
//    /**
//     * Convenience method to load the passed field values.
//     * Loads the fields using any required fetch plan and calls jdoPostLoad() as appropriate.
//     * @param fv Field Values to load (including any fetch plan to use when loading)
//     */
//    public void loadFieldValues(FieldValues fv)
//    {
//        // Fetch the required fields using any defined fetch plan
//        FetchPlan.FetchPlanForClass origFetchPlan = myFP;
//        FetchPlan loadFetchPlan = fv.getFetchPlanForLoading();
//        if (loadFetchPlan != null)
//        {
//            myFP = loadFetchPlan.manageFetchPlanForClass(cmd);
//        }
//
//        boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
//
//        fv.fetchFields(this);
//
//        // TODO This only applies to JDO (call postLoad even after we have the fields loaded)
//        // We should have a JPAStateManagerImpl that has this difference.
//        if (callPostLoad && isFetchPlanLoaded() && myOM.getOMFContext().getApiAdapter() instanceof JDOAdapter)
//        {
//            postLoad();
//        }
//
//        // Reinstate the original (PM) fetch plan
//        myFP = origFetchPlan;
//    }
//
//    /**
//     * Utility to set the identity for the PersistenceCapable object.
//     * Will only create the id instance if it is not attributed (in some way) by the datastore.
//     * So, for example, autoassign cases will not gain their identity here. In such cases the identity
//     * is only created when they are inserted into the datastore.
//     */
//    private void setIdentity()
//    {
//        if (cmd.getIdentityType() == IdentityType.DATASTORE)
//        {
//            if (cmd.getIdentityMetaData() == null || !getStoreManager().isStrategyDatastoreAttributed(cmd.getIdentityMetaData().getValueStrategy(), true))
//            {
//                myID = getStoreManager().newObjectID(myOM, cmd.getFullClassName(), myPC);
//            }
//        }
//        else if (cmd.getIdentityType() == IdentityType.APPLICATION)
//        {
//            boolean isObjectIDDatastoreAttributed = false;
//            int totalFieldCount = cmd.getNoOfInheritedManagedFields() + cmd.getNoOfManagedFields();
//            for (int fieldNumber =0; fieldNumber < totalFieldCount; fieldNumber++)
//            {
//                AbstractPropertyMetaData fmd=cmd.getManagedFieldAbsolute(fieldNumber);
//                if (fmd.isPrimaryKey())
//                {
//                    if (getStoreManager().isStrategyDatastoreAttributed(fmd.getValueStrategy(), false))
//                    {
//                        isObjectIDDatastoreAttributed = true;
//                        break;
//                    }
//                    else if (cmd.usesSingleFieldIdentityClass())
//                    {
//                        if (this.provideField(fieldNumber) == null)
//                        {
//                            // SingleFieldIdentity field has not had its value set (by user, or by value-strategy)
//                            throw new JDOFatalUserException(LOCALISER.msg("StateManager.SingleFieldIdentityPKFieldIsNull", 
//                                cmd.getFullClassName(), fmd.getName()));
//                        }
//                    }
//                }
//            }
//
//            if (!isObjectIDDatastoreAttributed)
//            {
//                // Not generating the identity in the datastore so set it now
//                myID = getStoreManager().newObjectID(myOM, cmd.getFullClassName(), myPC);
//            }
//        }
//
//        internalNotifyIDChanged();
//    }
//
//    /**
//     * Javadocs ?????????
//     */
//    protected void internalNotifyIDChanged()
//    {
//        if (myInternalID != myID && myID != null)
//        {
//            /*
//                FIXME: PERFORMANCE: This is a hot spot. Try to get rid of
//                the separation between myInternalID and myID if possible.
//                For example, prove that myInternalID and myID are always
//                equal or code can be changed that they are always equal.
//                
//                Then we would loose this call and 4 bytes of myInternalID
//                per StateManager as well (on 32 bit machines).
//            */
//            // Update the id with the PM if it is changing
//            myOM.replaceObjectId(myPC, myInternalID, myID);
//        }
//    }
//
//    /**
//     * Convenience method for datastores that retrieve objects directly and so need to have a degree of control
//     * over object state, with this method migrating thge managed object to P_CLEAN state.
//     */
//    public void migrateToPersistentClean()
//    {
//        // TODO Rewrite this so that it checks on the object state at calling and prevents inappropriate changes
//        // e.g if already deleted, and requested to be P_CLEAN
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_CLEAN);
//
//        // Mark all fields as loaded
//        for (int i=0; i<loadedFields.length; ++i)
//        {
//            loadedFields[i] = true;
//        }
//        
//        // FIXME: Should we add this object into the cache as above?
//    }
//
//    /**
//     * Accessor for a L2-Cacheable form of this PersistenceCapable object.
//     * @return The L2 cacheable object
//     */
//    public CachedPC getL2CacheableObject()
//    {
//        PersistenceCapable pcCopy = myPC.jdoNewInstance(this, myPC.jdoGetObjectId());
//
//        // Make a copy of the field values from the original object - basic fields + PC fields + SCO fields (omit SCO containers)
//        int[] allFieldNumbers = getAllFieldNumbers(); 
//        int[] containerFieldNumbers = cmd.getSecondClassContainerFieldNumbers();
//        int[] noncontainerFieldNumbers = new int[allFieldNumbers.length - containerFieldNumbers.length];
//        boolean[] l2loadedFields = new boolean[allFieldNumbers.length];
//        int nonNum = 0;
//        for (int i=0;i<allFieldNumbers.length;i++)
//        {
//            boolean noncontainer = true;
//            for (int j=0;j<containerFieldNumbers.length;j++)
//            {
//                if (containerFieldNumbers[j] == allFieldNumbers[i])
//                {
//                    noncontainer = false;
//                    break;
//                }
//            }
//            if (noncontainer)
//            {
//                noncontainerFieldNumbers[nonNum++] = allFieldNumbers[i];
//                l2loadedFields[i] = (noncontainer && loadedFields[i]);
//            }
//            else
//            {
//                l2loadedFields[i] = false;
//            }
//        }
//        pcCopy.jdoCopyFields(myPC, noncontainerFieldNumbers);
//
//        // Reset jdoFlags in the copy to PersistenceCapable.READ_WRITE_OK and clear its state manager.
//        pcCopy.jdoReplaceFlags();
//        pcCopy.jdoReplaceStateManager(null);
//
//        return new CachedPC(pcCopy, l2loadedFields);
//    }
//
//    /**
//     * Method that replaces the PC managed by this StateManager to be the supplied object.
//     * This happens when we want to get an object for an id and create a Hollow object, and then validate
//     * against the datastore. This validation can pull in a new object graph from the datastore (e.g for DB4O)
//     * @param pc The PersistenceCapable to use
//     */
//    public void replaceManagedPC(PersistenceCapable pc)
//    {
//        if (pc == null)
//        {
//            return;
//        }
//
//        // Assign the StateManager for the new object
//        pc.jdoReplaceStateManager(this);
//
//        // Set all fields to be loaded
//        for (int i=0;i<loadedFields.length;i++)
//        {
//            loadedFields[i] = true;
//        }
//
//        // Unassign from the old object
//        myPC.jdoReplaceStateManager(null);
//
//        // Swap our object
//        myPC = pc;
//
//        // Put it in the cache in case the previous object was stored
//        myOM.putObjectIntoCache(this, true, true);
//    }
//
//    /**
//     * Convenience method to update our object with the field values from the passed object.
//     * Objects need to be of the same type, and the other object should not have a StateManager.
//     * @param pc The object that we should copy fields from
//     */
//    public void copyFieldsFromObject(PersistenceCapable pc, int[] fieldNumbers)
//    {
//        if (pc == null)
//        {
//            return;
//        }
//        if (!pc.getClass().getName().equals(myPC.getClass().getName()))
//        {
//            return;
//        }
//
//        // Assign the new object to this StateManager temporarily so that we can copy its fields
//        pc.jdoReplaceStateManager(this);
//        myPC.jdoCopyFields(pc, fieldNumbers);
//
//        // Remove the StateManager from the other object
//        pc.jdoReplaceStateManager(null);
//
//        // Set the loaded flags now that we have copied
//        for (int i=0;i<fieldNumbers.length;i++)
//        {
//            loadedFields[fieldNumbers[i]] = true;
//        }
//    }
//
//    /**
//     * Finalise method to make sure that we close correctly.
//     * @see java.lang.Object#finalize()
//     */
//    protected void finalize() throws Throwable
//    {
//        try
//        {
//            // Make sure all is flushed
//            flush();
//        }
//        finally
//        {
//            super.finalize();
//        }
//    }
//
//    /**
//     * Utility to update our object to use a different state manager.
//     * @param sm The new state manager.
//     **/
//    private void replaceStateManager(StateManager sm)
//    {
//        try
//        {
//            myPC.jdoReplaceStateManager(sm);
//        }
//        catch (SecurityException e)
//        {
//            throw new JDOFatalUserException(LOCALISER.msg("StateManager.SecurityProblem"), e);
//        }
//    }
//
//    /**
//     * Method to enlist the managed object in the current transaction.
//     */
//    public void enlistInTransaction()
//    {
//        if (!getObjectManager().getTransaction().isActive())
//        {
//            return;
//        }
//        myOM.enlistInTransaction(this);
//
//        if (jdoFlags == PersistenceCapable.LOAD_REQUIRED && isFetchPlanLoaded())
//        {
//            /*
//             * A transactional object whose DFG fields are loaded does not need
//             * to contact us in order to read those fields.
//             */
//            jdoFlags = PersistenceCapable.READ_OK;
//            myPC.jdoReplaceFlags();
//        }
//    }
//
//    /**
//     * Method to evict the managed object from the current transaction.
//     */
//    public void evictFromTransaction()
//    {
//        myOM.evictFromTransaction(this);
//
//        /*
//         * A non-transactional object needs to contact us on any field read no
//         * matter what fields are loaded.
//         */
//        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
//        myPC.jdoReplaceFlags();
//    }
//
//    /**
//     * Method to save all fields of the object.
//     */
//    public void saveFields()
//    {
//        savedImage = myPC.jdoNewInstance(this);
//        savedImage.jdoCopyFields(myPC, getAllFieldNumbers());
//        savedFlags = jdoFlags;
//        savedLoadedFields = (boolean[])loadedFields.clone();
//    }
//
//    /**
//     * Method to restore all fields of the object.
//     */
//    public void restoreFields()
//    {
//        if (savedImage != null)
//        {
//            loadedFields = savedLoadedFields;
//            jdoFlags = savedFlags;
//            myPC.jdoReplaceFlags();
//            myPC.jdoCopyFields(savedImage, getAllFieldNumbers());
//
//            clearDirtyFlags();
//            clearSavedFields();
//        }
//    }
//
//    /**
//     * Method to clear all fields of the object.
//     */
//    public void clearFields()
//    {
//        clearFieldsByNumbers(getAllFieldNumbers());
//    }
//
//    /**
//     * Method to clear all fields that are not part of the primary key of the object.
//     */
//    public void clearNonPrimaryKeyFields()
//    {
//        clearFieldsByNumbers(getNonPrimaryKeyFieldNumbers());
//    }
//    
//    private void clearFieldsByNumbers(int[] fieldNumbers)
//    {
//        try
//        {
//            getCallbackHandler().preClear(myPC);
//        }
//        finally
//        {
//            replaceFields(fieldNumbers, HOLLOWFIELDMANAGER);
//            clearLoadedFlags();
//            clearDirtyFlags();
//            getCallbackHandler().postClear(myPC);
//        }
//    }
//
//    /**
//     * Method to clear all saved fields on the object.
//     */
//    public void clearSavedFields()
//    {
//        savedImage = null;
//        savedFlags = 0;
//        savedLoadedFields = null;
//    }
//
//    /**
//     * Method to clear all loaded flags on the object.
//     * Note that the contract of this method implies, especially for object database backends, that the memory form
//     * of the object is outdated.
//     * Thus, for features like implicit saving of dirty object subgraphs should be switched off for this PC, even if the 
//     * object actually looks like being dirty (because it is being changed to null values).
//     */
//    public void clearLoadedFlags()
//    {
//        getStoreManager().notifyObjectIsOutdated(this);
//
//        jdoFlags = PersistenceCapable.LOAD_REQUIRED;
//        myPC.jdoReplaceFlags();
//        clearFlags(loadedFields);
//    }
//
//    /**
//     * Marks the given field dirty.
//     * @param field The no of field to mark as dirty. 
//     */
//    public void makeDirty(int field)
//    {
//        if (activity != ActivityState.DELETING)
//        {
//            // Mark dirty unless in the process of being deleted
//            boolean wasDirty = preWriteField(field);
//            postWriteField(wasDirty);
//        }
//    }
//
//    /**
//     * Mark the associated PersistenceCapable field dirty.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param fieldName the name of the field
//     */
//    public void makeDirty(PersistenceCapable pc, String fieldName)
//    {
//        if (!disconnectClone(pc))
//        {
//            int fieldNumber = cmd.getFieldNumberAbsolute(fieldName);
//
//            if (fieldNumber == -1)
//            {
//                throw new JDOUserException(LOCALISER.msg("StateManager.InvalidFieldForClass", fieldName, cmd.getFullClassName()));
//            }
//            
//            makeDirty(fieldNumber);
//        }
//    }
//
//    // -------------------------- Accessor Methods -----------------------------
//
//    /**
//     * Accessor for the PersistenceManager that owns this instance.
//     * @param pc The PersistenceCapable instance
//     * @return The PersistenceManager that owns this instance
//     */
//    public javax.jdo.PersistenceManager getPersistenceManager(PersistenceCapable pc)
//    {
//        //in identifying relationships, jdoCopyKeyFieldsFromId will call
//        //this method, and at this moment, myPC in statemanager is null
//        // Currently AbstractPersistenceManager.java putObjectInCache prevents any identifying relation object being put in L2
//
//        //if not identifying relationship, do the default check of disconnectClone:
//        //"this.disconnectClone(pc)"
//        if (myPC != null && this.disconnectClone(pc))
//        {
//            return null;
//        }
//        else if (myOM == null)
//        {
//            return null;
//        }
//        else
//        {
//            myOM.hereIsStateManager(this, myPC);
//            return (PersistenceManager) myOM.getOwner();
//        }
//    }
//
//    /**
//     * Return the object representing the JDO identity of the calling instance.
//     *
//     * According to the JDO specification, if the JDO identity is being changed
//     * in the current transaction, this method returns the JDO identify as of
//     * the beginning of the transaction.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @return the object representing the JDO identity of the calling instance
//     */
//    public Object getObjectId(PersistenceCapable pc)
//    {
//        if (disconnectClone(pc))
//        {
//            return null;
//        }
//        else
//        {
//            return getExternalObjectId(pc);
//        }
//    }
//
//    /**
//     * If the id is obtained after inserting the object into the database, set
//     * new a new id for persistent classes (for example, increment).
//     * @param id the id received from the datastore
//     */
//    public void setPostStoreNewObjectId(Object id)
//    {
//        if (cmd.getIdentityType() == IdentityType.DATASTORE)
//        {
//            if (id instanceof OID)
//            {
//                // Provided an OID direct
//                this.myID = id;
//            }
//            else
//            {
//                // OID "key" value provided
//                myID = OIDFactory.getInstance(myOM, cmd.getFullClassName(), id);
//            }
//        }
//        else if (cmd.getIdentityType() == IdentityType.APPLICATION)
//        {
//            try
//            {
//                myID = null;
//
//                int fieldCount = getHighestFieldNumber();
//                for (int fieldNumber = 0; fieldNumber < fieldCount; fieldNumber++)
//                {
//                    AbstractPropertyMetaData fmd=cmd.getManagedFieldAbsolute(fieldNumber);
//                    if (fmd.isPrimaryKey() && getStoreManager().isStrategyDatastoreAttributed(fmd.getValueStrategy(), false))
//                    {
//                        //replace the value of the id, but before convert the value to the field type if needed
//                        replaceField(fieldNumber, TypeConversionHelper.convertTo(id, fmd.getType()), true);
//                    }
//                }
//            }
//            catch (Exception e)
//            {
//                JPOXLogger.JDO.error(e);
//            }
//            finally
//            {
//                myID = myOM.getApiAdapter().getNewApplicationIdentityObjectId(getObject(), cmd);
//            }
//        }
//
//        internalNotifyIDChanged();
//    }
//    
//    /**
//     * Return an object id that the user can use.
//     * @param pc the PersistenceCapable object
//     * @return the object id
//     */
//    public Object getExternalObjectId(PersistenceCapable pc)
//    {
//        if (cmd.getIdentityType() == IdentityType.DATASTORE)
//        {
//            if (!flushing)
//            {
//                // Flush any datastore changes so that myID is set by the time we return
//                if (!flushedNew &&
//                    activity != ActivityState.INSERTING && activity != ActivityState.INSERTING_CALLBACKS &&
//                    myLC.stateType() == LifeCycleState.P_NEW)
//                {
//                    if (getStoreManager().isStrategyDatastoreAttributed(cmd.getIdentityMetaData().getValueStrategy(), true))
//                    {
//                        flush();
//                    }
//                }
//            }
//        }
//        else if (cmd.getIdentityType() == IdentityType.APPLICATION)
//        {
//            // Note that we always create a new application identity since it is mutable and we can't allow
//            // the user to change it. The only drawback of this is that we *must* have the relevant fields
//            // set when this method is called, so that the identity can be generated.
//            if (!flushing)
//            {
//                // Flush any datastore changes so that we have all necessary fields populated
//                // only if the datastore generates the field numbers
//                if (!flushedNew &&
//                    activity != ActivityState.INSERTING && activity != ActivityState.INSERTING_CALLBACKS &&
//                    myLC.stateType() == LifeCycleState.P_NEW)
//                {
//                    int[] pkFieldNumbers = cmd.getPrimaryKeyFieldNumbers();
//                    for (int i = 0; i < pkFieldNumbers.length; i++)
//                    {
//                        AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(i);
//                        if (getStoreManager().isStrategyDatastoreAttributed(fmd.getValueStrategy(), false))
//                        {
//                            flush();
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (cmd.usesSingleFieldIdentityClass())
//            {
//                //SingleFieldIdentity classes are immutable.
//                //Note, the instances of SingleFieldIdentity can be changed by the user using reflection,
//                //but this is not allowed by the JDO spec
//                return myID;
//            }
//            return myOM.getApiAdapter().getNewApplicationIdentityObjectId(myPC, cmd);
//        }
//
//        return myID;
//    }
//
//    /**
//     * Replace the current value of jdoStateManager.
//     *
//     * <P>This method is called by the PersistenceCapable whenever
//     * jdoReplaceStateManager is called and there is already
//     * an owning StateManager.  This is a security precaution
//     * to ensure that the owning StateManager is the only
//     * source of any change to its reference in the PersistenceCapable.</p>
//     *
//     * @return the new value for the jdoStateManager
//     * @param pc the calling PersistenceCapable instance
//     * @param sm the proposed new value for the jdoStateManager
//     */
//    public StateManager replacingStateManager(PersistenceCapable pc, StateManager sm)
//    {
//        if (myLC == null)
//        {
//            throw new JDOFatalInternalException("Null LifeCycleState");
//        }
//
//        if (myLC.stateType() == LifeCycleState.DETACHED_CLEAN)
//        {
//            return sm;
//        }
//
//        if (pc == myPC)
//        {
//            //TODO check if we are really in transition to a transient instance
//            if (sm == null)
//            {
//                return null;
//            }
//            if (sm == this)
//            {
//                return this;
//            }
//
//            if (this.myOM == ((AbstractStateManager) sm).myOM)
//            {
//                // This is a race condition when makePersistent or
//                // makeTransactional is called on the same PC instance for the
//                // same PM. It has been already set to this SM - just 
//                // disconnect the other one. Return this SM so it won't be
//                // replaced.
//                ((JDOStateManagerImpl) sm).disconnect();
//                return this;
//            }
//
//            if (sm != null)
//            {
//                throw new JDOUserException(LOCALISER.msg("StateManager.StateManagerChangeError"));
//            }
//            if (!disconnecting)
//            {
//                throw new JDOUserException(LOCALISER.msg("StateManager.StateManagerDisconnectError"));
//            }
//
//            if (JPOXLogger.JDO.isDebugEnabled())
//            {
//                JPOXLogger.JDO.debug(LOCALISER.msg("StateManager.ClearingStateManager", StringUtils.toJVMIDString(pc)));
//            }
//
//            return null;
//        }
//        else if (pc == savedImage)
//        {
//            return null;
//        }
//        else
//        {
//            return sm;
//        }
//    }
//
//    /**
//     * Return the object representing the JDO identity
//     * of the calling instance.  If the JDO identity is being changed in
//     * the current transaction, this method returns the current identity as
//     * changed in the transaction.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @return the object representing the JDO identity of the calling instance
//     */
//    public Object getTransactionalObjectId(PersistenceCapable pc)
//    {
//        return getObjectId(pc);
//    }
//
//    // --------------------------- Load Field Methods --------------------------
//
//    /**
//     * Fetchs from the database all SCO fields that are not containers that aren't already loaded.
//     */
//    private void loadSCONonContainerFields()
//    {
//        int[] noncontainerFieldNumbers = cmd.getSecondClassNonContainerFieldNumbers();
//        int[] fieldNumbers = getFlagsSetTo(loadedFields, noncontainerFieldNumbers, false);
//        if (fieldNumbers != null && fieldNumbers.length > 0)
//        {
//            getStoreManager().fetchObject(this, fieldNumbers);
//
//            // We currently dont call postLoad here since this is only called as part of attaching an object
//            // and consequently we just read to get the current (attached) values. Could add a flag on input to allow postLoad
//
//            // Update the L2 cache to have this object (more loaded fields)
//            myOM.putObjectIntoCache(this, false, true);
//        }
//    }
//
//    /**
//     * Fetch the specified fields from the database.
//     * Do NOT call with a FetchPlan field.
//     * @param fieldNumbers the numbers of the field(s) to fetch.
//     */
//    private void loadSpecifiedFields(int[] fieldNumbers)
//    {
//        if (myOM.getApiAdapter().isDetached(myPC))
//        {
//            // Nothing to do since we're detached
//            return;
//        }
//
//        if (isEmbedded())
//        {
//            // Should never happen since embedded will always retrieve all fields in one go.
//        }
//        else
//        {
//            getStoreManager().fetchObject(this, fieldNumbers);
//        }
//
//        // Update the L2 cache to have this object (more loaded fields)
//        myOM.putObjectIntoCache(this, false, true);
//    }
//
//    /**
//     * Fetch from the database all fields that are not currently loaded regardless of whether
//     * they are in the current fetch group or not. Called by lifecycle transitions.
//     * @since 1.1
//     */
//    public void loadUnloadedFields()
//    {
//        int[] fieldNumbers = getFlagsSetTo(loadedFields, getAllFieldNumbers(), false);
//        if (fieldNumbers != null && fieldNumbers.length > 0)
//        {
//            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
//            getStoreManager().fetchObject(this, fieldNumbers);
//
//            int[] secondClassMutableFieldNumbers = getSecondClassMutableFieldNumbers();
//
//            // Make sure all SCO lazy-loaded fields have been loaded
//            for (int i=0;i<secondClassMutableFieldNumbers.length;i++)
//            {
//                SingleValueFieldManager sfv = new SingleValueFieldManager();
//                provideFields(new int[]{secondClassMutableFieldNumbers[i]}, sfv);
//                Object value = sfv.fetchObjectField(i);
//                if (value instanceof SCOContainer)
//                {
//                    ((SCOContainer)value).load();
//                }
//            }
//
//            if (callPostLoad)
//            {
//                postLoad();
//            }
//
//            // Update the L2 cache to have this object (more loaded fields)
//            myOM.putObjectIntoCache(this, false, true);
//        }
//    }
//
//    boolean loadingFieldsInFetchPlan = false;
//
//    /**
//     * Method to load all unloaded fields in the FetchPlan.
//     * Recurses through the FetchPlan objects and loads fields of sub-objects where needed.
//     * Used as a precursor to detaching objects at commit since fields can't be loaded during
//     * the postCommit phase when the detach actually happens.
//     * @param state The FetchPlan state
//     */
//    public void loadFieldsInFetchPlan(FetchPlanState state)
//    {
//        if (loadingFieldsInFetchPlan)
//        {
//            // Already in the process of loading fields in this class so skip
//            return;
//        }
//
//        // Load unloaded FetchPlan fields of this object
//        loadingFieldsInFetchPlan = true;
//        loadUnloadedFieldsInFetchPlan();
//
//        // Recurse through all fields and do the same
//        int[] fieldNumbers = getFlagsSetTo(loadedFields, getAllFieldNumbers(), true);
//        if (fieldNumbers != null && fieldNumbers.length > 0)
//        {
//            // TODO Fix this to just access the fields of the FieldManager yet this actually does a replaceField
//            replaceFields(fieldNumbers, new LoadFieldManager(this, getSecondClassMutableFields(), myFP, state));
//        }
//
//        loadingFieldsInFetchPlan = false;
//    }
//
//    /**
//     * Fetchs from the database all fields that are not currently loaded and that are in the current
//     * fetch group. Called by lifecycle transitions.
//     * @since 1.1
//     */
//    public void loadUnloadedFieldsInFetchPlan()
//    {
//        int[] fieldNumbers = getFlagsSetTo(loadedFields, myFP.getFieldsInActualFetchPlan(), false);
//        if (fieldNumbers != null && fieldNumbers.length > 0)
//        {
//            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
//            getStoreManager().fetchObject(this, fieldNumbers);
//            if (callPostLoad)
//            {
//                postLoad();
//            }
//
//            // Update the L2 cache to have this object (more loaded fields)
//            myOM.putObjectIntoCache(this, false, true);
//        }
//    }
//
//    /**
//     * Fetchs from the database all fields in the actual fetch plan.
//     * Called by life-cycle transitions.
//     * @since 1.1
//     */
//    public void loadUnloadedFieldsOfClassInFetchPlan(FetchPlan fetchPlan)
//    {
//        FetchPlanForClass fpc = fetchPlan.manageFetchPlanForClass(this.cmd);
//        int[] fieldNumbers = getFlagsSetTo(loadedFields, fpc.getFieldsInActualFetchPlan(), false);
//        if (fieldNumbers != null && fieldNumbers.length > 0)
//        {
//            boolean callPostLoad = fpc.isToCallPostLoadFetchPlan(this.loadedFields);
//            getStoreManager().fetchObject(this, fieldNumbers);
//            if (callPostLoad)
//            {
//                postLoad();
//            }
//
//            // Update the L2 cache to have this object (more loaded fields)
//            myOM.putObjectIntoCache(this, false, true);
//        }
//    }
//
//    /**
//     * Refreshes from the database all fields in fetch plan.
//     * Called by life-cycle transitions when the object undergoes a "transitionRefresh".
//     */
//    public void refreshFieldsInFetchPlan()
//    {
//        int[] fieldNumbers = myFP.getFieldsInActualFetchPlan();
//        if (fieldNumbers != null && fieldNumbers.length > 0)
//        {
//            clearDirtyFlags(fieldNumbers);
//            clearFlags(loadedFields, fieldNumbers);
//
//            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
//
//            // Refresh the fetch plan fields in this object
//            setTransactionalVersion(null); // Make sure that the version is reset upon fetch
//            getStoreManager().fetchObject(this, fieldNumbers);
//
//            if (cmd.hasRelations(myOM.getClassLoaderResolver()))
//            {
//                // Check for cascade refreshes to related objects
//                for (int i=0;i<fieldNumbers.length;i++)
//                {
//                    AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(fieldNumbers[i]);
//                    int relationType = fmd.getRelationType(myOM.getClassLoaderResolver());
//                    if (relationType != Relation.NONE && fmd.isCascadeRefresh())
//                    {
//                        // Need to refresh the related field object(s)
//                        Object value = provideField(fieldNumbers[i]);
//                        if (value != null)
//                        {
//                            if (value instanceof Collection)
//                            {
//                                // Refresh any PC elements in the collection
//                                // TODO This should replace the SCO wrapper with a new one, or reload the wrapper
//                                SCOUtils.refreshFetchPlanFieldsForCollection(this, ((Collection)value).toArray());
//                            }
//                            else if (value instanceof Map)
//                            {
//                                // Refresh any PC keys/values in the map
//                                // TODO This should replace the SCO wrapper with a new one, or reload the wrapper
//                                SCOUtils.refreshFetchPlanFieldsForMap(this, ((Map)value).entrySet());
//                            }
//                            else if (value instanceof PersistenceCapable)
//                            {
//                                // Refresh any PC fields
//                                myOM.refreshObject(value);
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (callPostLoad)
//            {
//                postLoad();
//            }
//
//            getCallbackHandler().postRefresh(myPC);
//
//            // Update the L2 cache to have this object (more loaded fields)
//            myOM.putObjectIntoCache(this, false, true);
//        }
//    }
//    
//    /**
//     * Refreshes from the database all fields currently loaded.
//     * Called by life-cycle transitions when making transactional or reading fields.
//     */
//    public void refreshLoadedFields()
//    {
//        int[] fieldNumbers = getFlagsSetTo(loadedFields, myFP.getFieldsInActualFetchPlan(), true);
//
//        if (fieldNumbers != null && fieldNumbers.length > 0)
//        {
//            clearDirtyFlags();
//            clearFlags(loadedFields);
//
//            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
//            getStoreManager().fetchObject(this, fieldNumbers);
//            if (callPostLoad)
//            {
//                postLoad();
//            }
//
//            // Update the L2 cache to have this object (more loaded fields)
//            myOM.putObjectIntoCache(this, false, true);
//        }
//    }
//
//    /**
//     * Method that will unload all fields that are not in the FetchPlan.
//     * This is typically for use when the instance is being refreshed.
//     * @since 1.2
//     */
//    public void unloadNonFetchPlanFields()
//    {
//        int[] fpFieldNumbers = myFP.getFieldsInActualFetchPlan();
//        int[] nonfpFieldNumbers = null;
//        if (fpFieldNumbers == null || fpFieldNumbers.length == 0)
//        {
//            nonfpFieldNumbers = getAllFieldNumbers();
//        }
//        else
//        {
//            int fieldCount = getHighestFieldNumber();
//            if (fieldCount == fpFieldNumbers.length)
//            {
//                // No fields that arent in FetchPlan
//                return;
//            }
//
//            nonfpFieldNumbers = new int[fieldCount - fpFieldNumbers.length];
//            int currentFPFieldIndex = 0;
//            int j = 0;
//            for (int i=0;i<fieldCount; i++)
//            {
//                if (currentFPFieldIndex >= fpFieldNumbers.length)
//                {
//                    // Past end of FetchPlan fields
//                    nonfpFieldNumbers[j++] = i;
//                }
//                else
//                {
//                    if (fpFieldNumbers[currentFPFieldIndex] == i)
//                    {
//                        // FetchPlan field so move to next
//                        currentFPFieldIndex++;
//                    }
//                    else
//                    {
//                        nonfpFieldNumbers[j++] = i;
//                    }
//                }
//            }
//        }
//
//        // Mark all non-FetchPlan fields as unloaded
//        for (int i=0;i<nonfpFieldNumbers.length;i++)
//        {
//            loadedFields[nonfpFieldNumbers[i]] = false;
//            unloadedFields[nonfpFieldNumbers[i]] = true;
//        }
//    }
//
//    /**
//     * Convenience method to unload a field.
//     * @param fieldName Name of the field
//     */
//    public void unloadField(String fieldName)
//    {
//        FieldMetaData fmd = (FieldMetaData) getClassMetaData().getField(fieldName);
//        int fieldNumber = fmd.getAbsoluteFieldNumber();
//        if (pcObjectType == PC)
//        {
//            loadedFields[fieldNumber] = false;
//            unloadedFields[fieldNumber] = true;
//        }
//        else
//        {
//            throw new JDOUserException("Cannot unload field of embedded object");
//        }
//    }
//
//    /**
//     * Convenience method to load a field from the datastore.
//     * Used in attaching fields and checking their old values (so we dont
//     * want any postLoad method being called).
//     * TODO Merge this with one of the loadXXXFields methods.
//     * @param fieldNumber The field number.
//     */
//    public void loadFieldFromDatastore(int fieldNumber)
//    {
//        getStoreManager().fetchObject(this, new int[]{fieldNumber});
//    }
//
//    /**
//     * Return true if the field is cached in the calling instance.
//     * <P>
//     * In the JPOX implementation of this method, isLoaded() will always
//     * return true. If the field is not loaded, it will be loaded as a side
//     * effect of the call to this method. If it is in the default fetch group,
//     * the default fetch group, including this field, will be loaded.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the absolute field number
//     * @return always returns true (this implementation)
//     */
//    public boolean isLoaded(PersistenceCapable pc, int field)
//    {
//        try
//        {
//            if (disconnectClone(pc))
//            {
//                return true;
//            }
//            else
//            {
//                // Permit loading of fields whilst marked as deleted so we can do reachability
//                if (activity != ActivityState.DELETING)
//                {
//                    transitionReadField(loadedFields[field]);
//                }
//                if (!loadedFields[field])
//                {
//                    if (!unloadedFields[field])
//                    {
//                        Object value = provideField(field);
//                        if (value != null)
//                        {
//                            if (getSecondClassMutableFields()[field])
//                            {
//                                // When we (L2) cache objects we retain the values but don't restore the SCO fields directly
//                                // Try to get SCO field from its current value
//                                if (value instanceof SCO)
//                                {
//                                    if (!(value instanceof SCOCollection) && !(value instanceof SCOMap))
//                                    {
//                                        SCO newValue = (SCO) newSCOEmptyInstance(field);
//                                        newValue.setValueFrom(value, true);
//                                        loadedFields[field] = true;
//                                        return true;
//                                    }
//                                }
//                            }
//                            else if (isPCField(field))
//                            {
//                                // When we (L2) cache objects we retain the values but don't restore the SCO fields directly
//                                // Try to load PC field from the cache (if cached)
//                                if (value instanceof PersistenceCapable && loadPCFieldFromCache(field, (PersistenceCapable) value))
//                                {
//                                    return true;
//                                }
//                            }
//                        }
//                    }
//                    else 
//                    {
//                        // ignore what our PC has as value for this field, (re-)load it anyway
//                        // because it has explicitly been unloaded
//                    }
//
//                    if (pcObjectType != PC)
//                    {
//                        // Embedded object so we assume that all was loaded before (when it was read)
//                        return true;
//                    }
//
//                    if (myFP.isFieldInActualFetchPlan(field))
//                    {
//                        loadUnloadedFieldsInFetchPlan();
//                    }
//                    else
//                    {
//                        loadSpecifiedFields(new int[] {field});
//                    }
//                    unloadedFields[field] = false;
//                }
//
//                return true;
//            }
//        }
//        catch (JPOXException jpe)
//        {
//            // Convert into a JDOException since this is called from a user update of a field
//            throw JPOXJDOHelper.getJDOExceptionForJPOXException(jpe);
//        }
//    }
//
//    /**
//     * Convenience method to return if a field number is a PC field
//     * @param fieldNumber Number of field
//     * @return Whether it is PC
//     */
//    private boolean isPCField(int fieldNumber)
//    {
//        int[] pcFields = cmd.getPersistenceCapableFieldNumbers();
//        for (int i = 0; i < pcFields.length; i++)
//        {
//            if (pcFields[i] == fieldNumber)
//            {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Method to try to retrieve a PC field from the L1/L2 caches.
//     * Note that this only works for application identity PC objects.
//     * @param fieldNumber Number of field to load
//     * @param pc The PC object to load
//     * @return Whether it was loaded from cache.
//     */
//    private boolean loadPCFieldFromCache(int fieldNumber, PersistenceCapable pc)
//    {
//        AbstractClassMetaData fieldCmd = myOM.getMetaDataManager().getMetaDataForClass(pc.getClass(), myOM.getClassLoaderResolver());
//        if (fieldCmd.getIdentityType() == IdentityType.APPLICATION)
//        {
//            Object externalID = myOM.getApiAdapter().getNewApplicationIdentityObjectId(pc, fieldCmd);
//            Object cachedPC = myOM.getObjectFromCache(externalID);
//            if (cachedPC != null)
//            {
//                replaceField(fieldNumber, cachedPC, true);
//                loadedFields[fieldNumber] = true;
//                myOM.putObjectIntoCache(myOM.findStateManager(cachedPC), true, true);
//                return true;
//            }
//        }
//        return false;
//    }
//
//    /**
//     * Returns whether all fields are loaded.
//     * @return Returns true if all fields are loaded.
//     */
//    public boolean getAllFieldsLoaded()
//    {
//        for (int i = 0;i<loadedFields.length;i++)
//        {
//            if (!loadedFields[i])
//            {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Returns whether the object being managed is dirty.
//     * @return whether at least one field is dirty by checking the dirty flag.
//     */
//    public boolean isDirty()
//    {
//        return dirty;
//    }
//
//    /**
//     * Creates a copy of the {@link #dirtyFields} bitmap.
//     * @return a copy of the {@link #dirtyFields} bitmap.
//     */
//    public boolean[] getDirtyFields()
//    {
//        boolean[] copy = new boolean[dirtyFields.length];
//        System.arraycopy(dirtyFields,0,copy,0,dirtyFields.length);
//        return copy;
//    }
//
//    // ---------------------- Field Accessor/Mutator Methods -------------------
//
//    /**
//     * Called by the various setXXXField() methods once it's established that
//     * the field really deserves to be written.
//     * Makes the state transition and replaces the field value in the PC
//     * instance.
//     */
//    private void writeField(int field, Object newValue)
//    {
//        replaceField(field, newValue, true);
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setBooleanField(PersistenceCapable pc, int field, boolean currentValue, boolean newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, newValue ? Boolean.TRUE : Boolean.FALSE, true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || currentValue != newValue)
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, newValue ? Boolean.TRUE : Boolean.FALSE);
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, newValue ? Boolean.TRUE : Boolean.FALSE, true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setByteField(PersistenceCapable pc, int field, byte currentValue, byte newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, new Byte(newValue), true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || currentValue != newValue)
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, new Byte(newValue));
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, new Byte(newValue), true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setCharField(PersistenceCapable pc, int field, char currentValue, char newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, new Character(newValue), true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || currentValue != newValue)
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, new Character(newValue));
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, new Character(newValue), true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setDoubleField(PersistenceCapable pc, int field, double currentValue, double newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, new Double(newValue), true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || currentValue != newValue)
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, new Double(newValue));
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, new Double(newValue), true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setFloatField(PersistenceCapable pc, int field, float currentValue, float newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, new Float(newValue), true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || currentValue != newValue)
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, new Float(newValue));
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, new Float(newValue), true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setIntField(PersistenceCapable pc, int field, int currentValue, int newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, new Integer(newValue), true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || currentValue != newValue)
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, new Integer(newValue));
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, new Integer(newValue), true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setLongField(PersistenceCapable pc, int field, long currentValue, long newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, new Long(newValue), true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || currentValue != newValue)
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, new Long(newValue));
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, new Long(newValue), true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setShortField(PersistenceCapable pc, int field, short currentValue, short newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, new Short(newValue), true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || currentValue != newValue)
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, new Short(newValue));
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, new Short(newValue), true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setStringField(PersistenceCapable pc, int field, String currentValue, String newValue)
//    {
//        if (pc != myPC)
//        {
//            replaceField(pc, field, newValue, true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            if (!loadedFields[field] || !equals(currentValue, newValue))
//            {
//                boolean wasDirty = preWriteField(field);
//                writeField(field, newValue);
//                postWriteField(wasDirty);
//            }
//        }
//        else
//        {
//            replaceField(field, newValue, true);
//        }
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable when the
//     * corresponding mutator method (setXXX()) is called on the
//     * PersistenceCapable.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @param currentValue the current value of the field
//     * @param newValue the new value for the field
//     */
//    public void setObjectField(PersistenceCapable pc, int field, Object currentValue, Object newValue)
//    {
//        if (currentValue != null && currentValue != newValue && currentValue instanceof PersistenceCapable)
//        {
//            // Where the object is embedded, remove the owner from its old value since it is no longer managed by this StateManager
//            JDOStateManagerImpl currentSM = (JDOStateManagerImpl)myOM.findStateManager(currentValue);
//            if (currentSM != null && currentSM.isEmbedded())
//            {
//                currentSM.removeEmbeddedOwner(this, field);
//            }
//        }
//
//        if (pc != myPC)
//        {
//            replaceField(pc, field, newValue, true);
//            disconnectClone(pc);
//        }
//        else if (myLC != null)
//        {
//            boolean loadedOldValue = false;
//            Object oldValue = currentValue;
//            AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(field);
//            if (fmd.isDependent() && currentValue == null && newValue == null)
//            {
//                // Retrieve the old value (when not loaded) for any PC fields that are being nulled
//                if (!loadedFields[field])
//                {
//                    loadSpecifiedFields(new int[] {field});
//                    loadedOldValue = true;
//                    oldValue = provideField(field);
//                }
//            }
//
//            // Check equality of old and new values
//            boolean equal = false;
//            if (currentValue == null && newValue == null)
//            {
//                equal = true;
//            }
//            else if (currentValue != null && newValue != null)
//            {
//                if (currentValue instanceof PersistenceCapable)
//                {
//                    // PC object field so compare object equality
//                    // See JDO2 [5.4] "The JDO implementation must not use the application's hashCode and equals methods 
//                    // from the persistence-capable classes except as needed to implement the Collections Framework" 
//                    if (currentValue == newValue)
//                    {
//                        equal = true;
//                    }
//                }
//                else
//                {
//                    // Non-PC object field so compare using equals()
//                    if (currentValue.equals(newValue))
//                    {
//                        equal = true;
//                    }
//                }
//            }
//
//            /*
//                JDO 2.0 spec section 6.3 explicitly states:
//                In order for changes to arrays to be tracked, the application must explicitly notify the owning 
//                FCO of the change to the Array [...] by replacing the field value with its current value.
//                Thus, even if the old and new values are equal, at least in the array case, the consequences should be 
//                the same as if the old and new values are not equal.
//            */
//            if (!loadedFields[field] || !equal || fmd.hasArray())
//            {
//                boolean wasDirty = preWriteField(field);
//                if (currentValue instanceof SCO)
//                {
//                    ((SCO) currentValue).unsetOwner();
//                }
//
//                if (newValue instanceof SCO)
//                {
//                    SCO sco = (SCO) newValue;
//                    Object owner = sco.getOwner();
//
//                    if (owner != null)
//                    {
//                        throw new JDOUserException(LOCALISER.msg("StateManager.SecondClassObjectShareError", sco.getFieldName(), owner));
//                    }
//                }
//
//                writeField(field, newValue);
//                postWriteField(wasDirty);
//            }
//            else if (loadedOldValue)
//            {
//                // We've updated the value with the old value (when retrieving it above), so put the new value back again
//                boolean wasDirty = preWriteField(field);
//                writeField(field, newValue);
//                postWriteField(wasDirty);
//            }
//
//            if (fmd.isDependent() && oldValue != null && newValue == null && oldValue instanceof PersistenceCapable)
//            {
//                // Delete any dependent PC fields that have been nulled
//                flush(); // Make sure that any null reference is set first (to avoid any FK constraint failures)
//                JPOXLogger.JDO.debug(LOCALISER.msg("StateManager.DeleteDependentNulledField", oldValue, fmd.getFullFieldName()));
//                myOM.deleteObjectInternal(oldValue);
//            }
//        }
//        else
//        {
//            replaceField(field, newValue, true);
//        }
//    }
//
//    /**
//     * The StateManager uses this method to supply the value of jdoFlags to the
//     * associated PersistenceCapable instance.
//     * @param pc the calling PersistenceCapable instance
//     * @return the value of jdoFlags to be stored in the PersistenceCapable instance
//     */
//    public byte replacingFlags(PersistenceCapable pc)
//    {
//        // If this is a clone, return READ_WRITE_OK.
//        if (pc != myPC)
//        {
//            return PersistenceCapable.READ_WRITE_OK;
//        }
//        else
//        {
//            return jdoFlags;
//        }
//    }
//
//    /**
//     * Method to return the current value of a particular field.
//     * @param fieldNumber Number of field
//     * @return The value of the field
//     */
//    public Object provideField(int fieldNumber)
//    {
//        if (!unloadedFields[fieldNumber])
//        {
//            return provideField(myPC, fieldNumber);
//        }
//        else
//        {
//            return null;
//        }
//    }
//
//    /**
//     * Method to change the value of a particular field.
//     * @param fieldNumber Number of field
//     * @param value New value
//     * @param makeDirty Whether to make the field dirty when replacing it
//     */
//    public void replaceField(int fieldNumber, Object value, boolean makeDirty)
//    {
//        replaceField(myPC, fieldNumber, value, makeDirty);
//    }
//
//    /**
//     * Method to retrieve the value of a field from the PC object.
//     * Assumes that it is loaded.
//     * @param pc The PC object
//     * @param fieldNumber Number of field
//     * @return The value of the field
//     */
//    private Object provideField(PersistenceCapable pc, int fieldNumber)
//    {
//        Object obj;
//        synchronized (currFMmonitor)
//        {
//            FieldManager prevFM = currFM;
//            currFM = new SingleValueFieldManager();
//            try
//            {
//                pc.jdoProvideField(fieldNumber);
//                obj = currFM.fetchObjectField(fieldNumber);
//            }
//            finally
//            {
//                currFM = prevFM;
//            }
//        }
//
//        return obj;
//    }
//
//    /**
//     * Method to change the value of a field in the PC object.
//     * @param pc The PC object
//     * @param fieldNumber Number of field
//     * @param value The new value of the field
//     * @param makeDirty Whether to make the field dirty while replacing its value
//     */
//    private void replaceField(PersistenceCapable pc, int fieldNumber, Object value, boolean makeDirty)
//    {
//        if (embeddedOwners != null)
//        {
//            // Notify any owners that embed this object that it has just changed
//            // We do this before we actually change the object so we can compare with the old value
//            Iterator ownerIter = embeddedOwners.iterator();
//            while (ownerIter.hasNext())
//            {
//                EmbeddedOwnerRelation owner = (EmbeddedOwnerRelation)ownerIter.next();
//                JDOStateManagerImpl ownerSM = (JDOStateManagerImpl)owner.sm;
//
//                if (ownerSM == null || ownerSM.cmd == null)
//                {
//                    //for some reason these are null... raised when running JPA TCK
//                    continue;
//                }
//                AbstractPropertyMetaData ownerFmd = ownerSM.cmd.getManagedFieldAbsolute(owner.fieldNumber);
//                if (ownerFmd.getCollection() != null)
//                {
//                    // PC Object embedded in collection
//                    Object ownerField = ownerSM.provideField(owner.fieldNumber);
//                    if (ownerField instanceof SCOCollection)
//                    {
//                        ((SCOCollection)ownerField).updateEmbeddedElement(myPC, fieldNumber, value);
//                    }
//                }
//                else if (ownerFmd.getMap() != null)
//                {
//                    // PC Object embedded in map
//                    Object ownerField = ownerSM.provideField(owner.fieldNumber);
//                    if (ownerField instanceof SCOMap)
//                    {
//                        if (pcObjectType == EMBEDDED_MAP_KEY_PC)
//                        {
//                            ((SCOMap)ownerField).updateEmbeddedKey(myPC, fieldNumber, value);
//                        }
//                        if (pcObjectType == EMBEDDED_MAP_VALUE_PC)
//                        {
//                            ((SCOMap)ownerField).updateEmbeddedValue(myPC, fieldNumber, value);
//                        }
//                    }
//                }
//                else
//                {
//                    // PC Object embedded in PC object
//                    if ((ownerSM.miscFlags&MISC_UPDATING_EMBEDDING_FIELDS_WITH_OWNER)==0)
//                    {
//                        // Update the owner when one of our fields have changed, EXCEPT when they have just
//                        // notified us of our owner field!
//                        if (ownerSM.isEmbedded())
//                        {
//                            // Owner is embedded so just update its field
//                            ownerSM.replaceField(owner.fieldNumber, pc, makeDirty);
//                        }
//                        else
//                        {
//                            if (makeDirty)
//                            {
//                                // Owner is not embedded so mark the field as dirty too
//                                boolean wasDirty = ownerSM.preWriteField(owner.fieldNumber);
//                                ownerSM.replaceField(owner.fieldNumber, pc, true);
//                                ownerSM.postWriteField(wasDirty);
//                            }
//                            else
//                            {
//                                ownerSM.replaceField(owner.fieldNumber, pc, false);
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Update the field in our PC object
//        synchronized (currFMmonitor)
//        {
//            FieldManager prevFM = currFM;
//            currFM = new SingleValueFieldManager();
//
//            try
//            {
//                currFM.storeObjectField(fieldNumber, value);
//                pc.jdoReplaceField(fieldNumber);
//            }
//            finally
//            {
//                currFM = prevFM;
//            }
//        }
//    }
//
//    /**
//     * Called from the StoreManager after StoreManager.update() is called to
//     * obtain updated values from the PersistenceCapable associated with this
//     * StateManager.
//     *
//     * @param fieldNumbers An array of field numbers to be updated by the Store
//     * @param fm The updated values are stored in this object. This object is only valid
//     *   for the duration of this call.
//     */
//    public void provideFields(int fieldNumbers[], FieldManager fm)
//    {
//        synchronized (currFMmonitor)
//        {
//            FieldManager prevFM = currFM;
//            currFM = fm;
//
//            try
//            {
//                // This will respond by calling this.providedXXXFields() with the value of the field
//                myPC.jdoProvideFields(fieldNumbers);
//            }
//            finally
//            {
//                currFM = prevFM;
//            }
//        }
//    }
//
//    /**
//     * Called from the StoreManager to refresh data in the PersistenceCapable
//     * object associated with this StateManager.
//     * @param fieldNumbers An array of field numbers to be refreshed by the Store
//     * @param fm The updated values are stored in this object. This object is only valid
//     *   for the duration of this call.
//     * @param replaceWhenDirty Whether to replace the fields when they are dirty here
//     */
//    public void replaceFields(int fieldNumbers[], FieldManager fm, boolean replaceWhenDirty)
//    {
//        synchronized (currFMmonitor)
//        {
//            FieldManager prevFM = currFM;
//            currFM = fm;
//
//            try
//            {
//                int[] fieldsToReplace = fieldNumbers;
//                if (!replaceWhenDirty)
//                {
//                    int numberToReplace = fieldNumbers.length;
//                    for (int i=0;i<fieldNumbers.length;i++)
//                    {
//                        if (dirtyFields[fieldNumbers[i]])
//                        {
//                            numberToReplace--;
//                        }
//                    }
//                    if (numberToReplace > 0 && numberToReplace != fieldNumbers.length)
//                    {
//                        fieldsToReplace = new int[numberToReplace];
//                        int n = 0;
//                        for (int i=0;i<fieldNumbers.length;i++)
//                        {
//                            if (!dirtyFields[fieldNumbers[i]])
//                            {
//                                fieldsToReplace[n++] = fieldNumbers[i];
//                            }
//                        }
//                    }
//                    else if (numberToReplace == 0)
//                    {
//                        fieldsToReplace = null;
//                    }
//                }
//
//                if (fieldsToReplace != null)
//                {
//                    myPC.jdoReplaceFields(fieldsToReplace);
//                }
//            }
//            finally
//            {
//                currFM = prevFM;
//            }
//        }
//    }
//
//    /**
//     * Called from the StoreManager to refresh data in the PersistenceCapable
//     * object associated with this StateManager.
//     * @param fieldNumbers An array of field numbers to be refreshed by the Store
//     * @param fm The updated values are stored in this object. This object is only valid
//     *   for the duration of this call.
//     */
//    public void replaceFields(int fieldNumbers[], FieldManager fm)
//    {
//        replaceFields(fieldNumbers, fm, true);
//    }
//
//    /**
//     * Called from the StoreManager to refresh data in the PersistenceCapable
//     * object associated with this StateManager. Only not loaded fields are refreshed
//     *
//     * @param fieldNumbers An array of field numbers to be refreshed by the Store
//     * @param fm The updated values are stored in this object. This object is only valid
//     *   for the duration of this call.
//     */
//    public void replaceNonLoadedFields(int fieldNumbers[], FieldManager fm)
//    {
//        synchronized (currFMmonitor)
//        {
//            FieldManager prevFM = currFM;
//            currFM = fm;
//
//            boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
//            try
//            {
//                int[] fieldsToReplace = getFlagsSetTo(loadedFields, fieldNumbers, false);
//                if (fieldsToReplace != null && fieldsToReplace.length > 0)
//                {
//                    myPC.jdoReplaceFields(fieldsToReplace);
//                }
//            }
//            finally
//            {
//                currFM = prevFM;
//            }
//            if (callPostLoad && isFetchPlanLoaded())
//            {
//                // The fetch plan is now loaded so fire off any necessary post load
//                postLoad();
//            }
//        }
//    }
//
//    /**
//     * Method to register an owner StateManager with this embedded/serialised object.
//     * @param ownerSM The owning State Manager.
//     * @param ownerFieldNumber The field number in the owner that the embedded/serialised object is stored as
//     */
//    public void addEmbeddedOwner(org.jpox.StateManager ownerSM, int ownerFieldNumber)
//    {
//        if (ownerSM == null)
//        {
//            return;
//        }
//
//        if (embeddedOwners == null)
//        {
//            embeddedOwners = new ArrayList();
//        }
//        embeddedOwners.add(new EmbeddedOwnerRelation(ownerSM, ownerFieldNumber));
//    }
//
//    /**
//     * Method to remove an owner StateManager from this embedded/serialised objects owners list.
//     * @param ownerSM The owner to remove
//     * @param ownerFieldNumber The field in the owner where this object is stored
//     */
//    public void removeEmbeddedOwner(StateManager ownerSM, int ownerFieldNumber)
//    {
//        if (embeddedOwners != null)
//        {
//            Iterator iter = embeddedOwners.iterator();
//            while (iter.hasNext())
//            {
//                EmbeddedOwnerRelation relation = (EmbeddedOwnerRelation)iter.next();
//                if (relation.sm == ownerSM && relation.fieldNumber == ownerFieldNumber)
//                {
//                    iter.remove();
//                    break;
//                }
//            }
//        }
//    }
//
//    /**
//     * Accessor for the owning StateManagers for the managed object when stored embedded.
//     * Should really only have a single owner but users could, in principle, assign it to multiple.
//     * @return StateManagers owning this embedded object.
//     */
//    public org.jpox.StateManager[] getEmbeddedOwners()
//    {
//        if (embeddedOwners == null)
//        {
//            return null;
//        }
//        org.jpox.StateManager[] owners = new org.jpox.StateManager[embeddedOwners.size()];
//        for (int i=0;i<owners.length;i++)
//        {
//            EmbeddedOwnerRelation relation = (EmbeddedOwnerRelation)embeddedOwners.get(i);
//            owners[i] = relation.sm;
//        }
//        return owners;
//    }
//
//    /**
//     * Wrapper class storing the owning state manager, and the field of the
//     * PC managed by the owning state manager where this object is embedded/serialised.
//     */
//    private class EmbeddedOwnerRelation
//    {
//        private org.jpox.StateManager sm;
//        private int fieldNumber;
//
//        /**
//         * 
//         * @param ownerSM the owner StateManager
//         * @param ownerFieldNumber the absolute owner field number
//         */
//        public EmbeddedOwnerRelation(org.jpox.StateManager ownerSM, int ownerFieldNumber)
//        {
//            this.sm = ownerSM;
//            this.fieldNumber = ownerFieldNumber;
//        }
//    }
//
//    /**
//     * Method to store a new SCO field at the specified field number.
//     * @param fieldNumber The field number
//     * @param value The value
//     * @param forUpdate Whether this SCO needs updating in the datastore
//     * @return the new SCO instance
//     */
//    public Object newSCOInstance(int fieldNumber, Object value, boolean forUpdate)
//    {
//        if (value instanceof PersistenceCapable)
//        {
//            AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(fieldNumber);
//            if (fmd.getEmbeddedMetaData() != null &&
//                fmd.getEmbeddedMetaData().getOwnerField() != null)
//            {
//                // Assign the embedded/serialised object "owner-field" if specified
//                JDOStateManagerImpl subSM = (JDOStateManagerImpl)myOM.findStateManager(value);
//                int ownerAbsFieldNum = subSM.cmd.getFieldNumberAbsolute(fmd.getEmbeddedMetaData().getOwnerField());
//                if (ownerAbsFieldNum >= 0)
//                {
//                    miscFlags |= MISC_UPDATING_EMBEDDING_FIELDS_WITH_OWNER;
//                    subSM.replaceField(ownerAbsFieldNum, myPC, true);
//                    miscFlags &=~MISC_UPDATING_EMBEDDING_FIELDS_WITH_OWNER;
//                }
//            }
//        }
//        if (getSecondClassMutableFields()[fieldNumber] && value != null)
//        {
//            if (!(value instanceof SCO) || myPC != ((SCO)value).getOwner())
//            {
//                AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(fieldNumber);
//                if (JPOXLogger.JDO.isDebugEnabled())
//                {
//                    JPOXLogger.JDO.debug(LOCALISER.msg("StateManager.ReplacingSCO", fmd.getFullFieldName()));
//                }
//                SCO newValue = SCOUtils.newSCOInstance(fmd.getType(), this, fmd, value, forUpdate);
//
//                value = newValue;
//            }
//        }
//
//        return value;
//    }
//
//    /**
//     * Method to replace a second-class object field value.
//     * @param fieldNumber Number of field
//     * @param value New value of the field
//     * @param makeDirty Whether to make the field dirty when replacing with its SCO
//     * @return The new SCO object representing the new value
//     */
//    public Object replaceSCOFieldWithWrapper(int fieldNumber, Object value, boolean makeDirty)
//    {
//        Object newValue = newSCOInstance(fieldNumber, value, makeDirty);
//        if (newValue != value)
//        {
//            replaceField(fieldNumber, newValue, makeDirty);
//        }
//        return newValue;
//    }
//
//    /**
//     * Method to create a new SCO proxy wraper object for a field.
//     * The wrapper will be empty. e.g this could be a SCO collection
//     * and will be a org.jpox.sco.{class} type with no elements initially.
//     * @param fieldNumber Number of field
//     * @return The new (empty) SCO object
//     */
//    public Object newSCOEmptyInstance(int fieldNumber)
//    {
//        AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(fieldNumber);
//        if (JPOXLogger.JDO.isDebugEnabled())
//        {
//            JPOXLogger.JDO.debug(LOCALISER.msg("StateManager.ReplacingSCO", fmd.getFullFieldName()));
//        }
//        SCO newValue = SCOUtils.newSCOInstance(fmd.getType(), this, fmd, null, false);
//
//        return newValue;
//    }
//
//    /**
//     * Method to change the object state to read-field.
//     * @param isLoaded if the field was previously loaded
//     */
//    private void transitionReadField(boolean isLoaded)
//    {
//        if (myLC == null)
//        {
//            return;
//        }
//        synchronized(myPC)
//        {
//            preStateChange();
//            try
//            {
//                myLC = myLC.transitionReadField(this, isLoaded);
//            }
//            finally
//            {
//                postStateChange();
//            }
//        }
//    }
//
//    /**
//     * Method to change the object state to write-field.
//     */
//    private void transitionWriteField()
//    {
//        synchronized(myPC)
//        {
//            preStateChange();
//            try
//            {
//                myLC = myLC.transitionWriteField(this);
//            }
//            finally
//            {
//                postStateChange();
//            }
//        }
//    }
//
//    // ------------------------- Lifecycle Methods -----------------------------
//
//    /**
//     * Method to mark an object for reachability.
//     * Provides the basis for "persistence-by-reachability", but run at commit time only.
//     * The reachability algorithm is also run at makePersistent, but directly via InsertRequest.
//     * @param reachables List of object ids currently logged as reachable
//     */
//    public void runReachability(Set reachables)
//    {
//        if (reachables == null)
//        {
//            return;
//        }
//        if (!reachables.contains(getInternalObjectId()))
//        {
//            // Make sure all changes are persisted
//            flush();
//
//            if (isDeleted(myPC))
//            {
//                // This object is deleted so nothing further will be reachable
//                return;
//            }
//
//            // This object was enlisted so make sure all of its fields are loaded before continuing
//            if (getObjectManager().isEnlistedInTransaction(getInternalObjectId()))
//            {
//                loadUnloadedFields();
//            }
//
//            if (JPOXLogger.REACHABILITY.isDebugEnabled())
//            {
//                JPOXLogger.REACHABILITY.debug(LOCALISER.msg("Reachability.Commit.AddingInstanceToReachables", 
//                    StringUtils.toJVMIDString(myPC), getObjectId(myPC), myLC));
//            }
//            // Add this object id since not yet reached
//            reachables.add(getInternalObjectId());
//
//            // Go through all (loaded FetchPlan) fields for reachability using ReachabilityFieldManager
//            int[] loadedFieldNumbers = getFlagsSetTo(loadedFields, getAllFieldNumbers(), true);
//            if (loadedFieldNumbers != null && loadedFieldNumbers.length > 0)
//            {
//                provideFields(loadedFieldNumbers, new ReachabilityFieldManager(this, reachables));
//            }
//        }
//    }
//
//    /**
//     * Method to make the object persistent.
//     */
//    public void makePersistent()
//    {
//        if (myLC.isDeleted() && myOM.getOMFContext().getApiAdapter() instanceof JDOAdapter)
//        {
//            // JDO doesnt allow repersist of deleted objects
//            // TODO Move this into a JDO-specific StateManager
//            return;
//        }
//
//        if (dirty && !myLC.isDeleted && myLC.isTransactional && myOM.isDelayDatastoreOperationsEnabled())
//        {
//            // Already provisionally persistent, but delaying til commit so just re-run reachability
//            // to bring in any new objects that are now reachable
//            provideFields(cmd.getAllFieldNumbers(), new PersistFieldManager(this, false));
//            return;
//        }
//
//        getCallbackHandler().prePersist(myPC);
//
//        if (flushedNew)
//        {
//            // With CompoundIdentity bidir relations when the SM is created for this object ("initialiseForPersistentNew") the persist
//            // of the PK PC fields can cause the flush of this object, and so it is already persisted by the time we ge here
//            registerTransactional();
//            return;
//        }
//
//        if (cmd.isEmbeddedOnly())
//        {
//            // Cant persist an object of this type since can only be embedded
//            return;
//        }
//
//        // If this is an embedded/serialised object becoming persistent in its own right, assign an identity.
//        if (myID == null)
//        {
//            setIdentity();
//        }
//
//        dirty = true;
//
//        if (myOM.isDelayDatastoreOperationsEnabled())
//        {
//            // Delaying datastore flush til later
//            myOM.markDirty(this);
//            if (JPOXLogger.PERSISTENCE.isDebugEnabled())
//            {
//                JPOXLogger.PERSISTENCE.debug(LOCALISER.msg("StateManager.PersistenceDelayed", StringUtils.toJVMIDString(myPC)));
//            }
//            registerTransactional();
//
//            if (myLC.isTransactional && myLC.isDeleted())
//            {
//                // Re-persist of a previously deleted object
//                myLC = myLC.transitionMakePersistent(this);
//            }
//
//            // Run reachability on all fields of this PC - JDO2 [12.6.7]
//            provideFields(cmd.getAllFieldNumbers(), new PersistFieldManager(this, false));
//        }
//        else
//        {
//            // Persist the object and all reachables
//            internalMakePersistent();
//            registerTransactional();
//        }
//    }
//
//    /**
//     * Method to persist the object to the datastore.
//     */
//    private void internalMakePersistent()
//    {
//        activity = ActivityState.INSERTING;
//        boolean[] tmpDirtyFields = dirtyFields;
//        try
//        {
//            getCallbackHandler().preStore(myPC); // This comes after setting the INSERTING flag so we know we are inserting it now
//
//            //in InstanceLifecycleEvents this object could get dirty if a field is changed in preStore or
//            //postCreate, we clear dirty flags to make sure this object will not be flushed again
//            clearDirtyFlags();
//
//            getStoreManager().insertObject(this);
//            flushedNew = true;
//
//            getCallbackHandler().postStore(myPC);
//        }
//        catch (NotYetFlushedException ex)
//        {
//            //happening on cyclic relationships
//            //if not yet flushed error, we rollback dirty fields, so we can retry inserting
//            dirtyFields = tmpDirtyFields;
//            myOM.markDirty(this);
//            dirty = true;
//            //we throw exception, so the owning relationship will mark it's foreign key to update later
//            throw ex;
//        }
//        finally
//        {
//            activity = ActivityState.NONE;
//        }
//    }
//
//    /**
//     * Tests whether this object is being inserted.
//     * @return true if this instance is inserting.
//     */
//    public boolean isInserting()
//    {
//        return (activity == ActivityState.INSERTING);
//    }
//
//    /**
//     * Tests whether this object is being deleted.
//     * @return true if this instance is being deleted.
//     */
//    public boolean isDeleting()
//    {
//        return (activity == ActivityState.DELETING);
//    }
//
//    /**
//     * Accessor for whether the instance is newly persistent yet hasnt yet been flushed to the datastore.
//     * @return Whether not yet flushed to the datastore
//     */
//    public boolean isWaitingToBeFlushedToDatastore()
//    {
//        // Return true if object is new and not yet flushed to datastore
//        return myLC.stateType() == LifeCycleState.P_NEW && !flushedNew;
//    }
//
//    /**
//     * Returns whether the field of this object is inserted in the datastore.
//     * Only applies during the makePersistent process.
//     * @param fieldNumber Number of the field
//     * @return Whether it is inserted to the level of this field
//     */
//    public boolean isInserted(int fieldNumber)
//    {
//        if (!isInserting())
//        {
//            return true;
//        }
//        if (latestInsertedDatastoreClass == null)
//        {
//            // Not yet inserted anything
//            return false;
//        }
//        AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(fieldNumber);
//        if (fmd == null)
//        {
//            return false;
//        }
//        return latestInsertedDatastoreClass.managesClass(fmd.getClassName());
//    }
//
//    /**
//     * Latest datastore class used during insertion. Only used during makePersistent.
//     * Only the most recent datastore class is stored since with inheritance the tables are inserted
//     * in order, starting at the root.
//     */
//    private DatastoreClass latestInsertedDatastoreClass = null;
//
//    /**
//     * Change the activity state.
//     * @param activityState the new state
//     * @param table Datastore class that has just been processed
//     */
//    public void changeActivityState(ActivityState activityState, DatastoreClass table)
//    {
//        if (activityState == ActivityState.INSERTING_CALLBACKS)
//        {
//            latestInsertedDatastoreClass = table;
//            if (table.managesClass(cmd.getFullClassName()))
//            {
//                // Full insertion has just completed so notify all interested StateManagers
//                activity = activityState;
//                if (insertionNotifyList != null)
//                {
//                    synchronized(insertionNotifyList)
//                    {
//                        for (int i=0; i<insertionNotifyList.size(); i++)
//                        {
//                            JDOStateManagerImpl notifySM = (JDOStateManagerImpl)insertionNotifyList.get(i);
//                            notifySM.insertionCompleted(this);
//                        }
//                        insertionNotifyList.clear();
//                        insertionNotifyList = null;
//                    }
//                }
//            }
//            else
//            {
//                // Not yet inserted fully so keep going
//            }
//        }
//        else
//        {
//            activity = activityState;
//        }
//    }
//
//    /**
//     * Method to add a notifier that we must contact when we have finished our insertion.
//     * @param sm the state manager
//     * @param activityState the ActivityState (unused)
//     */
//    public void addInsertionNotifier(StateManager sm, ActivityState activityState)
//    {
//        // TODO Use the second param to add the StateManager to other lists for other events
//        if (insertionNotifyList == null)
//        {
//            insertionNotifyList = new ArrayList();
//        }
//        insertionNotifyList.add(sm);
//    }
//
//    /**
//     * Marks the given field as being required to be updated when the specified object has been inserted.
//     * @param pc The Persistable object
//     * @param fieldNumber Number of the field.
//     */
//    public void updateFieldAfterInsert(Object pc, int fieldNumber)
//    {
//        JDOStateManagerImpl otherSM = (JDOStateManagerImpl) myOM.findStateManager(pc);
//
//        // Register the other SM to update us when it is inserted
//        otherSM.addInsertionNotifier(this, ActivityState.INSERTING_CALLBACKS);
//
//        // Register that we should update this field when the other SM informs us
//        if (fieldsToBeUpdatedAfterObjectInsertion == null)
//        {
//            fieldsToBeUpdatedAfterObjectInsertion = new HashMap();
//        }
//        FieldContainer cont = (FieldContainer)fieldsToBeUpdatedAfterObjectInsertion.get(otherSM);
//        if (cont == null)
//        {
//            cont = new FieldContainer(fieldNumber);
//        }
//        else
//        {
//            cont.set(fieldNumber);
//        }
//        fieldsToBeUpdatedAfterObjectInsertion.put(otherSM, cont);
//
//        if (JPOXLogger.JDO.isDebugEnabled())
//        {
//            JPOXLogger.JDO.debug(LOCALISER.msg("StateManager.FieldRegisteredForUpdateAfterInsertion", 
//                cmd.getManagedFieldAbsolute(fieldNumber).getFullFieldName(), StringUtils.toJVMIDString(myPC)));
//        }
//    }
//
//    /**
//     * Method called by another StateManager when this object has registered that it needs to know
//     * when the other object has been inserted.
//     * @param sm State Manager of the other object that has just been inserted
//     */
//    void insertionCompleted(JDOStateManagerImpl sm)
//    {
//        if (fieldsToBeUpdatedAfterObjectInsertion == null)
//        {
//            return;
//        }
//
//        // Go through our insertion update list and mark all required fields as dirty
//        FieldContainer fldCont = (FieldContainer)fieldsToBeUpdatedAfterObjectInsertion.get(sm);
//        if (fldCont != null)
//        {
//            dirty = true;
//            int[] fieldsToUpdate = fldCont.getFields();
//            for (int i=0;i<fieldsToUpdate.length;i++)
//            {
//                if (JPOXLogger.JDO.isDebugEnabled())
//                {
//                    JPOXLogger.JDO.debug(LOCALISER.msg("StateManager.FieldBeingUpdatedNowValueInserted", 
//                        cmd.getManagedFieldAbsolute(fieldsToUpdate[i]).getName(), myID));
//                }
//                dirtyFields[fieldsToUpdate[i]] = true;
//            }
//            fieldsToBeUpdatedAfterObjectInsertion.remove(sm);
//            if (fieldsToBeUpdatedAfterObjectInsertion.size() == 0)
//            {
//                fieldsToBeUpdatedAfterObjectInsertion = null;
//            }
//
//            // Perform our update
//            flush();
//        }
//    }
//
//    /**
//     * Method to set the value for an external field stored against this object
//     * @param mapping The mapping for the (external) field
//     * @param value The value that this field has
//     */
//    public void setExternalFieldValueForMapping(JavaTypeMapping mapping, Object value)
//    {
//        if (externalFieldValuesByMapping == null)
//        {
//            externalFieldValuesByMapping = new HashMap();
//        }
//        externalFieldValuesByMapping.put(mapping, value);
//    }
//
//    /**
//     * Accessor for the value of an external field.
//     * This is used when inserting this object so that we can insert the external field values too.
//     * @param mapping The external field mapping
//     * @return The value for this mapping
//     */
//    public Object getValueForExternalField(JavaTypeMapping mapping)
//    {
//        if (externalFieldValuesByMapping == null)
//        {
//            return null;
//        }
//        return externalFieldValuesByMapping.get(mapping);
//    }
//
//    /** Private class storing the fields to be updated for a StateManager, when it is inserted */
//    private class FieldContainer
//    {
//        boolean[] fieldsToUpdate = new boolean[getAllFieldNumbers().length];
//        /**
//         * Constructor
//         * @param fieldNumber the absolute field number to flag true
//         */
//        public FieldContainer(int fieldNumber)
//        {
//            fieldsToUpdate[fieldNumber] = true;
//        }
//        /**
//         * Flag to true the <code>fieldNumber</code>
//         * @param fieldNumber the absolute field number to flag true
//         */
//        public void set(int fieldNumber)
//        {
//            fieldsToUpdate[fieldNumber] = true;
//        }
//        /**
//         * Array with absolute field numbers with true flag
//         * @return array of absolute field numbers
//         */
//        public int[] getFields()
//        {
//            return getFlagsSetTo(fieldsToUpdate,true);
//        }
//    }
//
//    /**
//     * Makes Transactional Transient instances persistent.
//     */
//    public void makePersistentTransactionalTransient()
//    {
//        preStateChange();
//        try
//        {
//            if (myLC.isTransactional && !myLC.isPersistent)
//            {
//                // make the transient instance persistent in the datastore, if is transactional and !persistent 
//                makePersistent();
//                myLC = myLC.transitionMakePersistent(this);
//            }
//        }
//        finally
//        {
//            postStateChange();
//        }
//
//    }
//
//    /**
//     * Method to change the object state to transactional.
//     */
//    public void makeTransactional()
//    {
//        preStateChange();
//        try
//        {
//            if (myLC == null)
//            {
//                initializeSM(LifeCycleState.T_CLEAN);
//                setRestoreValues(true);
//            }
//            else
//            {
//                myLC = myLC.transitionMakeTransactional(this);
//            }
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /**
//     * Method to change the object state to nontransactional.
//     */
//    public void makeNontransactional()
//    {
//        preStateChange();
//        try
//        {
//            myLC = myLC.transitionMakeNontransactional(this);
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /**
//     * Method to change the object state to transient.
//     * @param state Object containing the state of any fetchplan processing
//     */
//    public void makeTransient(FetchPlanState state)
//    {
//        if (state == null)
//        {
//            // No FetchPlan in use so just unset the owner of all loaded SCO fields
//            int[] fieldNumbers = getFlagsSetTo(loadedFields, getSecondClassMutableFieldNumbers(), true);
//            if (fieldNumbers != null && fieldNumbers.length > 0)
//            {
//                provideFields(fieldNumbers, new UnsetOwners());
//            }
//        }
//        else
//        {
//            // Make all loaded SCO fields transient appropriate to this fetch plan
//            loadUnloadedFieldsInFetchPlan();
//            int[] fieldNumbers = getFlagsSetTo(loadedFields, getSecondClassMutableFieldNumbers(), true);
//            if (fieldNumbers != null && fieldNumbers.length > 0)
//            {
//                // TODO Fix this to just access the fields of the FieldManager yet this actually does a replaceField
//                replaceFields(fieldNumbers, new MakeTransientFieldManager(this, getSecondClassMutableFields(), myFP, state));
//            }
//        }
//
//        preStateChange();
//        try
//        {
//            myLC = myLC.transitionMakeTransient(this,state!=null, myOM.getDetachAllOnCommit());
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /**
//     * Method to detach this object.
//     * If the object is detachable then it will be migrated to DETACHED state, otherwise will migrate
//     * to TRANSIENT. Used by "DetachAllOnCommit".
//     * @param state State for the detachment process
//     */
//    public void detach(FetchPlanState state)
//    {
//        if (myLC.isDeleted() || myOM.getApiAdapter().isDetached(myPC) || detaching)
//        {
//            // Already deleted, detached or being detached
//            return;
//        }
//
//        // Check if detachable ... if so then we detach a copy, otherwise we return a transient copy
//        boolean detachable = myOM.getApiAdapter().isDetachable(myPC);
//
//        if (detachable)
//        {
//            if (JPOXLogger.PERSISTENCE.isDebugEnabled())
//            {
//                JPOXLogger.PERSISTENCE.debug(LOCALISER.msg("OM.Detach", StringUtils.toJVMIDString(myPC), 
//                    "" + state.getCurrentFetchDepth()));
//            }
//
//            // Call any "pre-detach" listeners
//            getCallbackHandler().preDetach(myPC);
//        }
//
//        try
//        {
//            detaching = true;
//
//            // Handle any field loading/unloading before the detach
//            if ((myOM.getFetchPlan().getDetachmentOptions() & FetchPlan.DETACH_LOAD_FIELDS) != 0)
//            {
//                // Load any unloaded fetch-plan fields
//                loadUnloadedFieldsInFetchPlan();
//            }
//
//            // Detach all (loaded) fields in the FetchPlan
//            FieldManager detachFieldManager = new DetachFieldManager(this, getSecondClassMutableFields(), myFP, 
//                state, false);
//            for (int i = 0; i < loadedFields.length; i++)
//            {
//                if (loadedFields[i])
//                {
//                    try
//                    {
//                        // Just fetch the field since we are usually called in postCommit() so dont want to update it
//                        detachFieldManager.fetchObjectField(i);
//                    }
//                    catch (EndOfFetchPlanGraphException eofpge)
//                    {
//                        // Do nothing
//                    }
//                }
//            }
//
//            if (detachable)
//            {
//                // Migrate the lifecycle state to DETACHED_CLEAN
//                myLC = myLC.transitionDetach(this);
//
//                // Update the object with its detached state
//                myPC.jdoReplaceFlags();
//                ((Detachable)myPC).jdoReplaceDetachedState();
//
//                // Call any "post-detach" listeners
//                getCallbackHandler().postDetach(myPC, myPC); // there is no copy, so give the same object
//
//                PersistenceCapable toCheckPC = myPC;
//                Object toCheckID = myID;
//                disconnect();
//
//                if (!toCheckPC.jdoIsDetached())
//                {
//                    // Sanity check on the objects detached state
//                    throw new JDOUserException(LOCALISER.msg("StateManager.DetachOfObjectFailed", toCheckPC.getClass().getName(), toCheckID));
//                }
//            }
//            else
//            {
//                // Make the object transient
//                makeTransient(null);
//            }
//        }
//        finally
//        {
//            detaching = false;
//        }
//    }
//
//    /**
//     * Return an array of field numbers that must be included in the detached object
//     * @return the field numbers array
//     */
//    private int[] getFieldsNumbersToDetach()
//    {
//        // This will cause the detach of any other fields in the FetchPlan.
//        int[] fieldsToDetach = myFP.getFieldsInActualFetchPlan();
//        if ((myOM.getFetchPlan().getDetachmentOptions() & FetchPlan.DETACH_UNLOAD_FIELDS) == 0)
//        {
//            // Detach fetch-plan fields plus any other loaded fields
//            int[] allFieldNumbers = getAllFieldNumbers();
//            int[] loadedFieldNumbers = getFlagsSetTo(loadedFields, allFieldNumbers, true);
//            if (loadedFieldNumbers != null && loadedFieldNumbers.length > 0)
//            {
//                boolean[] flds = new boolean[allFieldNumbers.length];
//                for (int i=0;i<fieldsToDetach.length;i++)
//                {
//                    flds[fieldsToDetach[i]] = true;
//                }
//                for (int i=0;i<loadedFieldNumbers.length;i++)
//                {
//                    flds[loadedFieldNumbers[i]] = true;
//                }
//                fieldsToDetach = getFlagsSetTo(flds,true);
//            }
//        }
//        return fieldsToDetach;
//    }
//    
//    /**
//     * Method to make detached copy of this instance
//     * If the object is detachable then the copy will be migrated to DETACHED state, otherwise will migrate
//     * the copy to TRANSIENT. Used by "ObjectManager.detachObjectCopy()".
//     * @param state State for the detachment process
//     * @return the detached PersistenceCapable instance
//     * @since 1.1
//     */
//    public Object detachCopy(FetchPlanState state)
//    {
//        if (myLC.isDeleted())
//        {
//            throw new JDOUserException(LOCALISER.msg("StateManager.DetachOfDeletedObjectNotPossible", myPC.getClass().getName(), myID));
//        }
//        if (myOM.getApiAdapter().isDetached(myPC))
//        {
//            throw new JDOUserException(LOCALISER.msg("StateManager.DetachOfAlreadyDetachedObjectNotPossible", myPC.getClass().getName(), myID));
//        }
//        if (dirty)
//        {
//            myOM.flush(false);
//        }
//        if (detaching)
//        {
//            // Object in the process of detaching (recursive) so return the object which will be the detached object
//            return detachingPC;
//        }
//
//        PersistenceCapable detachedPC = myPC.jdoNewInstance(this);
//        detachingPC = detachedPC;
//
//        // Check if detachable ... if so then we detach a copy, otherwise we return a transient copy
//        boolean detachable = myOM.getApiAdapter().isDetachable(myPC);
//
//        // make sure a detaching PC is not read by another thread while we are detaching
//        synchronized (detachingPC)
//        {
//            if (detachable)
//            {
//                if (JPOXLogger.PERSISTENCE.isDebugEnabled())
//                {
//                    JPOXLogger.PERSISTENCE.debug(LOCALISER.msg("OM.DetachCopy", StringUtils.toJVMIDString(myPC), 
//                        "" + state.getCurrentFetchDepth()));
//                }
//
//                // Call any "pre-detach" listeners
//                getCallbackHandler().preDetach(myPC);
//            }
//            try
//            {
//                detaching = true;
//
//                // Handle any field loading/unloading before the detach
//                if ((myOM.getFetchPlan().getDetachmentOptions() & FetchPlan.DETACH_LOAD_FIELDS) != 0)
//                {
//                    // Load any unloaded fetch-plan fields
//                    loadUnloadedFieldsInFetchPlan();
//                }
//
//                if (myLC == myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW) ||
//                    myLC == myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NONTRANS))
//                {
//                    // Migrate any HOLLOW/P_NONTRANS to P_CLEAN etc
//                    myLC = myLC.transitionReadField(this, true);
//                }
//
//                // Create a SM for our copy object
//                JDOStateManagerImpl smDetachedPC = new JDOStateManagerImpl(myOM, cmd);
//                smDetachedPC.initialiseForDetached(detachedPC, getExternalObjectId(myPC), myVersion);
//                detachedPC.jdoReplaceStateManager(smDetachedPC);
//
//                smDetachedPC.replaceFields(getFieldsNumbersToDetach(), new DetachFieldManager(this, getSecondClassMutableFields(), 
//                    myFP, state, true));
//
//                if (detachable)
//                {
//                    // Update the object with its detached state - not to be confused with the "state" object above
//                    detachedPC.jdoReplaceFlags();
//                    ((Detachable)detachedPC).jdoReplaceDetachedState();
//                }
//                else
//                {
//                    smDetachedPC.makeTransient(null);
//                }
//
//                // Remove its StateManager since now detached or transient
//                detachedPC.jdoReplaceStateManager(null);
//            }
//            catch (Exception e)
//            {
//                // What could possible be thrown here ?
//                JPOXLogger.JDO.debug("DETACH ERROR : Error thrown while detaching " +
//                    StringUtils.toJVMIDString(myPC) + " (id=" + myID + ")", e);
//            }
//            finally
//            {
//                detaching = false;
//                detachingPC = null;
//            }
//
//            if (detachable && !myOM.getApiAdapter().isDetached(detachedPC))
//            {
//                // Sanity check on the objects detached state
//                throw new JDOUserException(LOCALISER.msg("StateManager.DetachOfObjectFailed", detachedPC.getClass().getName(), myID));
//            }
//
//            if (detachable)
//            {
//                // Call any "post-detach" listeners
//                getCallbackHandler().postDetach(myPC, detachedPC);
//            }
//        }
//        return detachedPC;
//    }
//
//    /**
//     * Accessor for the PC object that this detached object is being attached as.
//     * @return The attached version of this object
//     */
//    public Object getAttachedPC()
//    {
//        return attachedPC;
//    }
//
//    /**
//     * Method to attach the detached persistable instance
//     * @param obj the detached persistable instance to be attached
//     * @param embedded Whether the object is stored embedded/serialised in another object
//     * @return The attached copy
//     * @since 1.1
//     */
//    public Object attachCopy(Object obj, boolean embedded)
//    {
//        if (attaching)
//        {
//            return myPC;
//        }
//        attaching = true;
//
//        PersistenceCapable detachedPC = (PersistenceCapable)obj;
//        try
//        {
//            // Check if the object is already persisted
//            boolean persistent = false;
//            if (embedded)
//            {
//                persistent = true;
//            }
//            else
//            {
//                if (!myOM.getOMFContext().getPersistenceConfiguration().getAttachSameDatastore())
//                {
//                    // We cant assume that this object was detached from this datastore so we check it
//                    try
//                    {
//                        getStoreManager().locateObject(this);
//                        persistent = true;
//                    }
//                    catch (JPOXObjectNotFoundException onfe)
//                    {
//                        // Not currently present!
//                    }
//                }
//                else
//                {
//                    // Assumed detached from this datastore
//                    persistent = true;
//                }
//            }
//
//            // Call any "pre-attach" listeners
//            getCallbackHandler().preAttach(detachedPC);
//
//            if (myOM.getApiAdapter().isDeleted(detachedPC))
//            {
//                // The detached object has been deleted
//                myLC = myLC.transitionDeletePersistent(this);
//            }
//
//            if (myLC == myOM.getApiAdapter().getLifeCycleState(LifeCycleState.HOLLOW) ||
//                myLC == myOM.getApiAdapter().getLifeCycleState(LifeCycleState.P_NONTRANS))
//            {
//                // Make the attached object transactional
//                // There may be other states that we come in here with that need making transactional
//                myLC = myLC.transitionMakeTransactional(this);
//            }
//
//            if (persistent)
//            {
//                // Make sure that all non-container SCO fields are loaded so we can make valid dirty checks
//                // for whether these fields have been updated whilst detached. The detached object doesnt know if the contents
//                // have been changed.
//                loadSCONonContainerFields();
//            }
//
//            // Add a state manager to the detached PC so that we can retrieve its detached state
//            JDOStateManagerImpl smDetachedPC = new JDOStateManagerImpl(myOM, cmd);
//            smDetachedPC.initialiseForDetached(detachedPC, getExternalObjectId(detachedPC), null);
//            detachedPC.jdoReplaceStateManager(smDetachedPC);
//
//            // Mark the temporary StateManager with the attached PC object in case it is accessed during attach
//            smDetachedPC.attachedPC = myPC;
//
//            // Retrieve the updated values from the detached object
//            retrieveDetachState(smDetachedPC);
//
//            if (!persistent)
//            {
//                // Object is not yet persisted! so make it persistent
//
//                // Make sure all field values in the attach object are ready for inserts (but dont trigger any cascade attaches)
//                internalAttachCopy(this, smDetachedPC, smDetachedPC.loadedFields, smDetachedPC.dirtyFields, persistent, smDetachedPC.myVersion, false);
//
//                makePersistent();
//            }
//
//            // Go through all related fields and attach them (including relationships)
//            internalAttachCopy(this, smDetachedPC, smDetachedPC.loadedFields, smDetachedPC.dirtyFields, persistent, smDetachedPC.myVersion, true);
//
//            // Remove the state manager from the detached PC
//            detachedPC.jdoReplaceStateManager(null);
//            smDetachedPC.attachedPC = null;
//
//            // Call any "post-attach" listeners
//            getCallbackHandler().postAttach(myPC,detachedPC);
//        }
//        finally
//        {
//            attaching = false;
//        }
//        return myPC;
//    }
//
//    /**
//     * Attach the fields of a persistent object.
//     * @param sm StateManager for the attached object.
//     * @param smDetached StateManager for the detached object.
//     * @param loadedFields Fields that were detached with the object
//     * @param dirtyFields Fields that have been modified while detached
//     * @param persistent whether the object is already persistent
//     * @param version the version
//     * @param cascade Whether to cascade the attach to related fields
//     * @since 1.1
//     */
//    private void internalAttachCopy(org.jpox.StateManager sm,
//                                   org.jpox.StateManager smDetached,
//                                   boolean[] loadedFields,
//                                   boolean[] dirtyFields,
//                                   boolean persistent,
//                                   Object version,
//                                   boolean cascade)
//    {
//        // Need to take all loaded fields plus all modified fields (maybe some werent detached but have been modified) and attach them
//        int[] attachFieldNumbers = null;
//        int numberOfAttachFields = 0;
//        for (int i=0;i<loadedFields.length;i++)
//        {
//            if (loadedFields[i] || dirtyFields[i])
//            {
//                numberOfAttachFields++;
//            }
//        }
//        attachFieldNumbers = new int[numberOfAttachFields];
//        int n=0;
//        for (int i=0;i<loadedFields.length;i++)
//        {
//            if (loadedFields[i] || dirtyFields[i])
//            {
//                attachFieldNumbers[n++] = getAllFieldNumbers()[i];
//            }
//        }
//        sm.setVersion(version);
//        if (attachFieldNumbers != null)
//        {
//            // Only update the fields that were detached, and only update them if there are any to update
//            smDetached.provideFields(attachFieldNumbers, 
//                new AttachFieldManager(sm, getSecondClassMutableFields(), dirtyFields, persistent, cascade));
//        }
//    }
//
//    /**
//     * Method to delete the object from persistence.
//     */
//    public void deletePersistent()
//    {
//        if (!myLC.isDeleted())
//        {
//            // Call any lifecycle listeners waiting for this event.
//            getCallbackHandler().preDelete(myPC);
//
//            // Update lifecycle state
//            dirty = true;
//            preStateChange();
//            try
//            {
//                myLC = myLC.transitionDeletePersistent(this);
//            }
//            finally
//            {
//                postStateChange();
//            }
//
//            if (myOM.isDelayDatastoreOperationsEnabled())
//            {
//                // Delay deletion until flush/commit so run reachability now to tag all reachable instances as necessary
//                myOM.markDirty(this);
//
//                if (myLC.stateType() == LifeCycleState.P_DELETED)
//                {
//                    // Make sure all fields are loaded so we can perform reachability
//                    loadUnloadedFields();
//                }
//
//                // Reachability
//                provideFields(getAllFieldNumbers(), new DeleteFieldManager(this));
//            }
//            else
//            {
//                // Delete the object from the datastore (includes reachability)
//                internalDeletePersistent();
//
//                // Call any lifecycle listeners waiting for this event.
//                getCallbackHandler().postDelete(myPC);
//            }
//        }
//    }
//
//    /**
//     * Method to delete the object from the datastore.
//     */
//    private void internalDeletePersistent()
//    {
//        if (isDeleting())
//        {
//            throw new JDOUserException(LOCALISER.msg("StateManager.DeleteRecursiveError"));
//        }
//
//        activity = ActivityState.DELETING;
//        try
//        {
//            if (dirty)
//            {
//                clearDirtyFlags();
//                /*
//                 * Clear the PM's knowledge of our being dirty. This will
//                 * call our flush() method, which will do nothing.
//                 */
//                myOM.flush(false);
//            }
//
//            getStoreManager().deleteObject(this);
//        }
//        finally
//        {
//            activity = ActivityState.NONE;
//        }
//    }
//
//    /**
//     * Locate the object in the datastore.
//     * @throws JPOXObjectNotFoundException if the object doesnt exist.
//     */
//    public void locate()
//    {
//        // Validate the object existence
//        getStoreManager().locateObject(this);
//    }
//
//    /**
//     * Nullify fields with reference to PersistenceCapable or SCO instances 
//     */
//    public void nullifyFields()
//    {
//        if (!myLC.isDeleted() && !myOM.getApiAdapter().isDetached(myPC))
//        {
//            // Update any relationships for fields of this object that aren't dependent
//            replaceFields(getNonPrimaryKeyFieldNumbers(), new NullifyRelationFieldManager(this));
//            flush();
//        }
//    }
//
//    /**
//     * Validates whether the persistence capable instance exists in the datastore.
//     * If the instance doesn't exist in the datastore, this method will fail raising a JPOXObjectNotFoundException.
//     * If the object is transactional then does nothing.
//     * If the object has unloaded (non-SCO, non-PK) fetch plan fields then fetches them.
//     * Else it checks the existence of the object in the datastore.
//     */
//    public void validate()
//    {
//        if (!myLC.isTransactional)
//        {
//            // Find all FetchPlan fields that are not PK, not SCO and still not loaded
//            int[] fieldNumbers = getFlagsSetTo(loadedFields, myFP.getFieldsInActualFetchPlan(), false);
//            if (fieldNumbers != null && fieldNumbers.length > 0)
//            {
//                fieldNumbers = getFlagsSetTo(getNonPrimaryKeyFields(), fieldNumbers, true);
//            }
//            if (fieldNumbers != null && fieldNumbers.length > 0)
//            {
//                fieldNumbers = getFlagsSetTo(getSecondClassMutableFields(), fieldNumbers, false);
//            }
//            if (fieldNumbers != null && fieldNumbers.length > 0)
//            {
//                // Some fetch plan fields are not loaded so try to load them, and this by itself validates the existence
//                // Load the fields in the current FetchPlan (JDO2 spec 12.6.5)
//                transitionReadField(false);
//
//                fieldNumbers = myFP.getFieldsInActualFetchPlan();
//                if (fieldNumbers != null)
//                {
//                    boolean callPostLoad = myFP.isToCallPostLoadFetchPlan(this.loadedFields);
//                    setTransactionalVersion(null); // Make sure we get the latest version
//                    getStoreManager().fetchObject(this, fieldNumbers);
//                    if (callPostLoad)
//                    {
//                        postLoad();
//                    }
//
//                    // Update the L2 cache to have this object (more loaded fields)
//                    myOM.putObjectIntoCache(this, false, true);
//                }
//            }
//            else
//            {
//                // Validate the object existence
//                getStoreManager().locateObject(this);
//                transitionReadField(false);
//            }
//        }
//    }
//
//    // ------------------------- Object State Methods --------------------------
//
//    /**
//     * Method to change the object state to evicted.
//     */
//    public void evict()
//    {
//        if (myLC != myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_CLEAN) &&
//            myLC != myOM.getOMFContext().getApiAdapter().getLifeCycleState(LifeCycleState.P_NONTRANS))
//        {
//            return;
//        }
//
//        preStateChange();
//        try
//        {
//            try
//            {
//                getCallbackHandler().preClear(myPC);
//
//                getCallbackHandler().postClear(myPC);
//            }
//            finally
//            {
//                myLC = myLC.transitionEvict(this);
//            }
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /**
//     * Method to refresh the object.
//     */
//    public void refresh()
//    {
//        preStateChange();
//        try
//        {
//            myLC = myLC.transitionRefresh(this);
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /**
//     * Method to retrieve the object.
//     * @param fgOnly Only load the current fetch group fields
//     */
//    public void retrieve(boolean fgOnly)
//    {
//        preStateChange();
//        try
//        {
//            myLC = myLC.transitionRetrieve(this, fgOnly);
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /**
//     * Method to retrieve the object.
//     * @param fetchPlan the fetch plan to load fields
//     */
//    public void retrieve(FetchPlan fetchPlan)
//    {
//        preStateChange();
//        try
//        {
//            myLC = myLC.transitionRetrieve(this, fetchPlan);
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    // --------------------------- Process Methods -----------------------------
//
//    /**
//     * Method called before a change in state.
//     */
//    private void preStateChange()
//    {
//        changingState = true;
//    }
//
//    /**
//     * Method called after a change in state.
//     */
//    private void postStateChange()
//    {
//        changingState = false;
//        if (postLoadPending && isFetchPlanLoaded())
//        {
//            // Only call postLoad when all FetchPlan fields are loaded
//            postLoadPending = false;
//            postLoad();
//        }
//    }
//
//    /**
//     * Method called before a write of the specified field.
//     * @param field The field to write
//     * @return true if the field was already dirty before
//     */
//    private boolean preWriteField(int field)
//    {
//        boolean wasDirty = dirty;
//        /*
//         * If we're writing a field in the process of inserting it must be due 
//         * to jdoPreStore().  We haven't actually done the INSERT yet so we 
//         * don't want to mark anything as dirty, which would make us want to do 
//         * an UPDATE later. 
//         */
//        if (activity != ActivityState.INSERTING && activity != ActivityState.INSERTING_CALLBACKS)
//        {
//            //TODO dirty already??? this is not correct, only gets dirty after state transition
////            dirty = true;
//            if (!wasDirty) // (only do it for first dirty event).
//            {
//                // Call any lifecycle listeners waiting for this event
//                getCallbackHandler().preDirty(myPC);
//            }
//
//            transitionWriteField();
//
//            dirty = true;
//            dirtyFields[field] = true;
//            loadedFields[field] = true;
//        }
//        return wasDirty;
//    }
//
//    /**
//     * Method called after the write of a field.
//     * @param wasDirty whether before writing this field the pc was dirty
//     */
//    private void postWriteField(boolean wasDirty)
//    {
//        if (dirty && !wasDirty) // (only do it for first dirty event).
//        {
//            // Call any lifecycle listeners waiting for this event
//            getCallbackHandler().postDirty(myPC);
//        }
//        
//        /*
//         * If we're writing a field in the middle of flushing or inserting it
//         * must be due to jdoPreStore(). To avoid an infinite recursion we don't
//         * notify the PM or call flush(). if not transactional transient
//         */
//        if (activity == ActivityState.NONE && !flushing && !(myLC.isTransactional && !myLC.isPersistent))
//        {
//            myOM.markDirty(this);
//        }
//    }
//
//    /**
//     * Called whenever the default fetch group fields have all been loaded.
//     * Updates jdoFlags and calls jdoPostLoad() as appropriate.
//     * <p>
//     * If it's called in the midst of a life-cycle transition both actions will
//     * be deferred until the transition is complete.
//     * <em>This deferral is important</em>. Without it, we could enter user
//     * code (jdoPostLoad()) while still making a state transition, and that way
//     * lies madness.
//     * <p>
//     * As an example, consider a jdoPostLoad() that calls other enhanced methods
//     * that read fields (jdoPostLoad() itself is not enhanced). A P_NONTRANS
//     * object accessed within a transaction would produce the following infinite
//     * loop:
//     * <p>
//     * <blockquote>
//     * 
//     * <pre>
//     * 
//     *  isLoaded()
//     *  transitionReadField()
//     *  refreshLoadedFields()
//     *  jdoPostLoad()
//     *  isLoaded()
//     *  ...
//     *  
//     * </pre>
//     * 
//     * </blockquote>
//     * <p>
//     * because the transition from P_NONTRANS to P_CLEAN can never be completed.
//     */
//    private void postLoad()
//    {
//        if (changingState)
//        {
//            postLoadPending = true;
//        }
//        else
//        {
//            /*
//             * A transactional object whose DFG fields are loaded does not need
//             * to contact us in order to read those fields, so we can safely set
//             * READ_OK.
//             *
//             * A non-transactional object needs to notify us on all field reads
//             * so that we can decide whether or not any transition should occur,
//             * so we leave the flags at LOAD_REQUIRED.
//             */
//            if (jdoFlags == PersistenceCapable.LOAD_REQUIRED && myLC.isTransactional())
//            {
//                jdoFlags = PersistenceCapable.READ_OK;
//                myPC.jdoReplaceFlags();
//            }
//
//            getCallbackHandler().postLoad(myPC);
//        }
//    }
//
//    /**
//     * Method invoked just before a transaction starts for the ObjectManager managing us.
//     * @param tx The transaction
//     */
//    public void preBegin(org.jpox.Transaction tx)
//    {
//        preStateChange();
//        try
//        {
//            myLC = myLC.transitionBegin(this, tx);
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /**
//     * This method is invoked just after a commit is performed in a Transaction
//     * involving the PersistenceCapable managed by this StateManager
//     * @param tx The Transaction
//     */
//    public void postCommit(org.jpox.Transaction tx)
//    {
//        preStateChange();
//        try
//        {
//            myLC = myLC.transitionCommit(this, tx);
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /**
//     * This method is invoked just before a rollback is performed in a Transaction
//     * involving the PersistenceCapable managed by this StateManager.
//     * @param tx The Transaction been rolled back
//     */
//    public void preRollback(org.jpox.Transaction tx)
//    {
//        preStateChange();
//        try
//        {
//            myOM.clearDirty(this);
//            myLC = myLC.transitionRollback(this, tx);
//        }
//        finally
//        {
//            postStateChange();
//        }
//    }
//
//    /** Flag to signify that we are currently storing the PC object, so we dont detach it on any serialisation. */
//    private boolean storingPC = false;
//
//    /**
//     * Method to set the storing PC flag.
//     */
//    public void setStoringPC()
//    {
//        storingPC = true;
//    }
//
//    /**
//     * Method to unset the storing PC flag.
//     */
//    public void unsetStoringPC()
//    {
//        storingPC = false;
//    }
//
//    /**
//     * Guarantee that the serializable transactional and persistent fields
//     * are loaded into the instance.  This method is called by the generated
//     * jdoPreSerialize method prior to serialization of the instance.
//     *
//     * @param pc the calling PersistenceCapable instance
//     */
//    public void preSerialize(PersistenceCapable pc)
//    {
//        if (disconnectClone(pc))
//        {
//            return;
//        }
//
//        // Retrieve all fields prior to serialisation
//        retrieve(false);
//
//        myLC = myLC.transitionSerialize(this);
//
//        if (!storingPC && pc instanceof Detachable)
//        {
//            if (!myLC.isDeleted && myLC.isPersistent)
//            {
//                if (myLC.isDirty)
//                {
//                    flush();
//                }
//
//                // Normal PC Detachable object being serialised so load up the detached state into the instance
//                // JDO2 spec "For Detachable classes, the jdoPreSerialize method must also initialize the jdoDetachedState
//                // instance so that the detached state is serialized along with the instance."
//                ((Detachable)pc).jdoReplaceDetachedState();
//            }
//        }
//    }
//
//    /**
//     * Flushes any outstanding changes to the object to the datastore. 
//     * This will process :-
//     * <ul>
//     * <li>Any objects that have been marked as provisionally persistent yet havent been flushed to the datastore.</li>
//     * <li>Any objects that have been marked as provisionally deleted yet havent been flushed to the datastore.</li>
//     * <li>Any fields that have been updated.</li>
//     * </ul>
//     */
//    public void flush()
//    {
//        if (dirty)
//        {
//            if (flushing)
//            {
//                // In the case of persisting a new object using autoincrement id within an optimistic
//                // transaction, flush() will initially be called at the point of recognising that the
//                // id is generated in the datastore, and will then be called again at the point of doing
//                // the InsertRequest for the object itself. Just return since we are flushing right now
//                return;
//            }
//
//            flushing = true;
//            try
//            {
//                if (myLC.stateType() == LifeCycleState.P_NEW && !flushedNew)
//                {
//                    // Newly persisted object but not yet flushed to datastore (e.g optimistic transactions)
//                    if (!isEmbedded())
//                    {
//                        // internalMakePersistent does preStore, postStore
//                        internalMakePersistent();
//                    }
//                    else
//                    {
//                        getCallbackHandler().preStore(myPC);
//
//                        getCallbackHandler().postStore(myPC);
//                    }
//                    dirty = false;
//                }
//                else if (myLC.stateType() == LifeCycleState.P_DELETED)
//                {
//                    // Object marked as deleted but not yet deleted from datastore
//                    getCallbackHandler().preDelete(myPC);
//                    if (!isEmbedded())
//                    {
//                        internalDeletePersistent();
//                    }
//                    getCallbackHandler().postDelete(myPC);
//                }
//                else if (myLC.stateType() == LifeCycleState.P_NEW_DELETED)
//                {
//                    // Newly persisted object marked as deleted but not yet deleted from datastore
//                    if (flushedNew)
//                    {
//                        // Only delete it if it was actually persisted into the datastore
//                        getCallbackHandler().preDelete(myPC);
//                        if (!isEmbedded())
//                        {
//                            internalDeletePersistent();
//                        }
//                        flushedNew = false; // No longer newly persisted flushed object since has been deleted
//                        getCallbackHandler().postDelete(myPC);
//                    }
//                    else
//                    {
//                        // Was never persisted to the datastore so nothing to do
//                        dirty = false;
//                    }
//                }
//                else
//                {
//                    // Updated object with changes to flush to datastore
//                    if (!isDeleting())
//                    {
//                        getCallbackHandler().preStore(myPC);
//                    }
//
//                    int[] dirtyFieldNumbers = getFlagsSetTo(dirtyFields, true);
//                    if (dirtyFieldNumbers == null)
//                    {
//                        throw new JDOFatalInternalException(LOCALISER.msg("StateManager.InconsistentDirtyStateError"));
//                    }
//                    if (!isEmbedded())
//                    {
//                        getStoreManager().updateObject(this, dirtyFieldNumbers);
//
//                        // Update the object in the cache(s)
//                        myOM.putObjectIntoCache(this, true, true);
//                    }
//
//                    clearDirtyFlags();
//
//                    getCallbackHandler().postStore(myPC);
//                }
//            }
//            finally
//            {
//                flushing = false;
//            }
//        }
//    }
//
//    /**
//     * Initialize SM reference in PC and Oid
//     * @param newState The new StateManager state 
//     **/
//    private void initializeSM(int newState)
//    {
//        final JDOStateManagerImpl thisSM = this;
//        myLC = myOM.getOMFContext().getApiAdapter().getLifeCycleState(newState);
//
//        try
//        {
//            if (myLC.isPersistent())
//            {
//                myOM.addStateManager(this);
//            }
//
//            // Everything OK so far. Now we can set SM reference in PC 
//            // It can be done only after myLC is set to deligate validation
//            // to the LC and objectId verified for uniqueness
//            AccessController.doPrivileged(
//            // Need to have privileges to perform jdoReplaceStateManager.
//                    new PrivilegedAction()
//                    {
//                        public Object run()
//                        {
//                            try
//                            {
//                                myPC.jdoReplaceStateManager(thisSM);
//                                return null;
//                            }
//                            catch (SecurityException e)
//                            {
//                                throw new JDOFatalUserException("EXC_CannotSetStateManager", e);
//                            }
//                        }
//                    });
//
//        }
//        catch (SecurityException e)
//        {
//            throw new JDOUserException(e.getMessage());
//
//        }
//        catch (JDOException jdoException)
//        {
//            if (myOM.getStateManagerById(myID) == this)
//            {
//                myOM.removeStateManager(this);
//            }
//            throw jdoException;
//        }
//    }
//
//    /**
//     * Method to disconnect any cloned persistence capable objects from their
//     * StateManager.
//     * @param pc The PersistenceCapable object
//     * @return Whether the object was disconnected.
//     **/
//    protected boolean disconnectClone(PersistenceCapable pc)
//    {
//        if (detaching)
//        {
//            return false;
//        }
//        if (pc != myPC)
//        {
//            if (JPOXLogger.JDO.isDebugEnabled())
//            {
//                JPOXLogger.JDO.debug(LOCALISER.msg("StateManager.DisconnectClone", StringUtils.toJVMIDString(pc), this));
//            }
//
//            // Reset jdoFlags in the clone to PersistenceCapable.READ_WRITE_OK 
//            // and clear its state manager.
//            pc.jdoReplaceFlags();
//            pc.jdoReplaceStateManager(null);
//            return true;
//        }
//        else
//        {
//            return false;
//        }
//    }
//
//    /**
//     * Convenience method to unset the owners of all SCO fields in the PC object.
//     */
//    private void unsetOwnerInSCOFields()
//    {
//        // Call unsetOwner() on all loaded SCO fields.
//        int[] fieldNumbers = getFlagsSetTo(loadedFields, getSecondClassMutableFieldNumbers(), true);
//        if (fieldNumbers != null && fieldNumbers.length > 0)
//        {
//            provideFields(fieldNumbers, new UnsetOwners());
//        }        
//    }
//
//    /**
//     * Disconnect the StateManager from the PersistenceManager and PC object.
//     */
//    public void disconnect()
//    {
//        if (JPOXLogger.JDO.isDebugEnabled())
//        {
//            JPOXLogger.JDO.debug(LOCALISER.msg("StateManager.Disconnect", StringUtils.toJVMIDString(myPC), this));
//        }
//
//        //we are transitioning to TRANSIENT state, so if any postLoad
//        //action is pending we do it before. This usually happens when
//        //we make transient instances using the fetch plan and some
//        //fields were loaded during this action which triggered a
//        //jdoPostLoad event
//        if (postLoadPending)
//        {
//            changingState = false; //hack to make sure postLoad does not return without processing
//            postLoadPending = false;
//            postLoad();
//        }
//        
//        unsetOwnerInSCOFields();
//
//        myOM.removeStateManager(this);
//        jdoFlags = PersistenceCapable.READ_WRITE_OK;
//        myPC.jdoReplaceFlags();
//
//        disconnecting = true;
//        try
//        {
//            replaceStateManager(null);
//        }
//        finally
//        {
//            disconnecting = false;
//        }
//
//        clearSavedFields();
//        myOM = null;
//        myFP = null;
//        myPC = null;
//        myID = null;
//        myLC = null;
//        cmd = null;
////      srm = null;
//    }
//
//    /**
//     * Registers the pc class in the cache
//     */
//    public void registerTransactional()
//    {
//        myOM.addStateManager(this);
//    }
//
//    // ------------------------------ Detach Methods ---------------------------
//
//    /**
//     * Convenience method to retrieve the detach state from the passed State Manager's object
//     * @param sm The State Manager
//     */
//    public void retrieveDetachState(org.jpox.StateManager sm)
//    {
//        if (sm.getObject() instanceof Detachable)
//        {
//            ((JDOStateManagerImpl)sm).miscFlags |= MISC_RETRIEVING_DETACHED_STATE;
//            ((Detachable)sm.getObject()).jdoReplaceDetachedState();
//            ((JDOStateManagerImpl)sm).miscFlags &=~MISC_RETRIEVING_DETACHED_STATE;
//        }
//    }
//
//    /**
//     * Convenience method to reset the detached state in the current object.
//     */
//    public void resetDetachState()
//    {
//        if (getObject() instanceof Detachable)
//        {
//            miscFlags |= MISC_RESETTING_DETACHED_STATE;
//            try
//            {
//                ((Detachable)getObject()).jdoReplaceDetachedState();
//            }
//            finally
//            {
//                miscFlags &=~MISC_RESETTING_DETACHED_STATE;
//            }
//        }
//    }
//
//    /**
//     * Method to update the "detached state" in the detached object
//     * to obtain the "detached state" from the detached object, or
//     * to reset it (to null).
//     * @param pc The PersistenceCapable beind updated
//     * @param currentState The current state values
//     * @return The detached state to assign to the object
//     */
//    public Object[] replacingDetachedState(Detachable pc, Object[] currentState)
//    {
//        if ((miscFlags&MISC_RESETTING_DETACHED_STATE)!=0)
//        {
//            return null;
//        }
//        else if ((miscFlags&MISC_RETRIEVING_DETACHED_STATE)!=0)
//        {
//            // Retrieving the detached state from the detached object
//            // Don't need the id or version since they can't change
//            BitSet jdoLoadedFields = (BitSet)currentState[2];
//            for (int i = 0; i < this.loadedFields.length; i++)
//            {
//                this.loadedFields[i] = jdoLoadedFields.get(i);
//            }
//
//            BitSet jdoModifiedFields = (BitSet)currentState[3];
//            for (int i = 0; i < dirtyFields.length; i++)
//            {
//                dirtyFields[i] = jdoModifiedFields.get(i);
//            }
//            setVersion(currentState[1]);
//            return currentState;
//        }
//        else
//        {
//            // Updating the detached state in the detached object with our state
//            Object[] state = new Object[4];
//            state[0] = myID;
//            state[1] = myVersion;
//
//            // Loaded fields
//            if (currentState == null || currentState[2] == null)
//            {
//                BitSet loadedState = new BitSet();
//                for (int i = 0; i < loadedFields.length; i++)
//                {
//                    if (loadedFields[i])
//                    {
//                        loadedState.set(i);
//                    }
//                    else
//                    {
//                        loadedState.clear(i);
//                    }
//                }
//                state[2] = loadedState;
//            }
//            else
//            {
//                // TODO Why are we just passing the same state back ?
//                state[2] = currentState[2];
//            }
//
//            // Modified fields
//            if (currentState == null || currentState[3] == null)
//            {
//                BitSet modifiedState = new BitSet();
//                for (int i = 0; i < dirtyFields.length; i++)
//                {
//                    if (dirtyFields[i])
//                    {
//                        modifiedState.set(i);
//                    }
//                    else
//                    {
//                        modifiedState.clear(i);
//                    }
//                }
//                state[3] = modifiedState;
//            }
//            else
//            {
//                // TODO Why are we just passing the same state back ?
//                state[3] = currentState[3];
//            }
//            return state;
//        }
//    }
//
//    // ------------------------------ Helper Methods ---------------------------
//
//    /**
//     * Method to dump a PersistenceCapable object to the specified
//     * PrintWriter.
//     * @param pc The PersistenceCapable object
//     * @param out The PrintWriter
//     **/
//    private static void dumpPC(PersistenceCapable pc, PrintWriter out)
//    {
//        out.println(StringUtils.toJVMIDString(pc));
//
//        if (pc == null)
//        {
//            return;
//        }
//
//        out.print("jdoStateManager = " + peekField(pc, "jdoStateManager"));
//        out.print("jdoFlags = ");
//        Object flagsObj = peekField(pc, "jdoFlags");
//        if (flagsObj instanceof Byte)
//        {
//            out.println(jdoFlagsToString(((Byte) flagsObj).byteValue()));
//        }
//        else
//        {
//            out.println(flagsObj);
//        }
//
//        Class c = pc.getClass();
//
//        do
//        {
//            String[] fieldNames = HELPER.getFieldNames(c);
//
//            for (int i = 0; i < fieldNames.length; ++i)
//            {
//                out.print(fieldNames[i]);
//                out.print(" = ");
//                out.println(peekField(pc, fieldNames[i]));
//            }
//
//            c = c.getSuperclass();
//        }
//        while (c != null && PersistenceCapable.class.isAssignableFrom(c));
//    }
//
//    /**
//     * Utility to dump the contents of the StateManager.
//     *
//     * @param out PrintWriter to dump to
//     **/
//    public void dump(PrintWriter out)
//    {
//        out.println("myPM = " + myOM);
//        out.println("myID = " + myID);
//        out.println("myLC = " + myLC);
//        out.println("cmd = " + cmd);
//        out.println("srm = " + getStoreManager());
//        out.println("fieldCount = " + getHighestFieldNumber());
//        out.println("dirty = " + dirty);
//        out.println("flushing = " + flushing);
//        out.println("changingState = " + changingState);
//        out.println("postLoadPending = " + postLoadPending);
//        out.println("disconnecting = " + disconnecting);
//        out.println("dirtyFields = " + StringUtils.booleanArrayToString(dirtyFields));
//        out.println("getSecondClassMutableFields() = " + StringUtils.booleanArrayToString(getSecondClassMutableFields()));
//        out.println("getAllFieldNumbers() = " + StringUtils.intArrayToString(getAllFieldNumbers()));
//        out.println("secondClassMutableFieldNumbers = " + StringUtils.intArrayToString(getSecondClassMutableFieldNumbers()));
//
//        out.println();
//        out.println("jdoFlags = " + jdoFlagsToString(jdoFlags));
//        out.println("loadedFields = " + StringUtils.booleanArrayToString(loadedFields));
//        out.print("myPC = ");
//        dumpPC(myPC, out);
//
//        out.println();
//        out.println("savedFlags = " + jdoFlagsToString(savedFlags));
//        out.println("savedLoadedFields = " + StringUtils.booleanArrayToString(savedLoadedFields));
//
//        out.print("savedImage = ");
//        dumpPC(savedImage, out);
//    }
//
//
//    /**
//     * Utility to convert JDO specific flags to a String.
//     * @param flags The JDO flags
//     * @return String version 
//     **/
//    private static String jdoFlagsToString(byte flags)
//    {
//        switch (flags)
//        {
//            case PersistenceCapable.LOAD_REQUIRED:
//                return "LOAD_REQUIRED";
//            case PersistenceCapable.READ_OK:
//                return "READ_OK";
//            case PersistenceCapable.READ_WRITE_OK:
//                return "READ_WRITE_OK";
//            default:
//                return "???";
//        }
//    }
//
//    /**
//     * Stringifier method.
//     * @return String form of the StateManager
//     */
//    public String toString()
//    {
//        return "JDOStateManagerImpl[pc=" + StringUtils.toJVMIDString(myPC) + ", lifecycle=" + myLC + "]";
//    }
}
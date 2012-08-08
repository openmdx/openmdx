/**********************************************************************
Copyright (c) 2006 Andy Jefferson and others. All rights reserved.
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
2007 Xuan Baldauf - use IdentityReference instead of super.toString() as a unique identifier.
2007 Xuan Baldauf - moved initialization of AbstractStateManager to Initialization.
2007 Xuan Baldauf - remove the field "srm".
2007 Xuan Baldauf - remove the field "secondClassMutableFieldNumbers".
2007 Xuan Baldauf - remove the field "allNonPrimaryKeyFieldNumbers".
2007 Xuan Baldauf - remove the field "allFieldNumbers".
2007 Xuan Baldauf - remove the field "nonPrimaryKeyFields".
2007 Xuan Baldauf - remove the field "secondClassMutableFields".
2007 Xuan Baldauf - move the field "fieldCount" to AbstractClassMetaData.
2007 Xuan Baldauf - remove the field "callback".  
    ...
**********************************************************************/
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdo;


/**
 * Abstract representation of a StateManager.
 * Provides some of the basic StateManager methods that do very little.
 * @version $Revision: 1.1 $
 */
public abstract class AbstractStateManager implements StateManager
{
//    /** Localiser for messages. */
//    protected static final Localiser LOCALISER = Localiser.getInstance("org.jpox.Localisation");
//
//    /** the Object Manager for this StateManager */
//    protected ObjectManager myOM;
//
//    /** The PersistenceCapable instance managed by this StateManager */
//    protected PersistenceCapable myPC;
//
//    /** the metadata for the class. */
//    protected AbstractClassMetaData cmd;
//
//    /** The object identity in the JVM. Will be "myID" (if set) or otherwise a temporary id based on this StateManager. */
//    protected Object myInternalID;
//
//    /** The object identity in the datastore */
//    protected Object myID;
//
//    /** The actual LifeCycleState for the PersistenceCapable instance */
//    protected LifeCycleState myLC;
//
//    /** version field for optimistic transactions */
//    protected Object myVersion;
//
//    /** version field for optimistic transactions, after a insert/update but not yet committed. */
//    protected Object transactionalVersion;
//
//    /** Fetch plan for the class of the managed object. */
//    protected FetchPlan.FetchPlanForClass myFP;
//
//    /**
//     * Indicator for whether the PersistenceCapable instance is dirty.
//     * Note that "dirty" in this case is not equated to being in the P_DIRTY state.
//     * The P_DIRTY state means that at least one field in the object has been written by the user during 
//     * the current transaction, whereas for this parameter, a field is "dirty" if it's been written by the 
//     * user but not yet updated in the data store.  The difference is, it's possible for an object's state
//     * to be P_DIRTY, yet have no "dirty" fields because flush() has been called at least once during the transaction.
//     */
//    protected boolean dirty = false;
//
//    /** indicators for which fields are currently dirty in the PersistenceCapable instance. */
//    protected boolean[] dirtyFields;
//
//    /** indicators for which fields are currently loaded in the PersistenceCapable instance. */
//    protected boolean[] loadedFields;
//
//    /** indicators for which fields are currently unloaded in the PersistenceCapable instance. */
//    protected boolean[] unloadedFields;
//
//    /** Whether to restore values at StateManager. If true, overwrites the restore values at tx level. */
//    protected boolean restoreValues = false;
//
//    /** Current FieldManager. */
//    protected FieldManager currFM = null;
//
//    /** monitor to synchronize execution when replacing/providing fields **/
//    protected Object currFMmonitor = new Object();
//
//    /** The type of the managed object (0 = PC, 1 = embedded PC, 2 = embedded element, 3 = embedded key, 4 = embedded value. */
//    protected int pcObjectType = 0;
//
//    /**
//     * Constructor.
//     * @param om ObjectManager
//     * @param cmd the metadata for the class.
//     */
//    public AbstractStateManager(ObjectManager om, AbstractClassMetaData cmd)
//    {
//        myOM = om;
//        this.cmd = cmd;
//
//        // Set up the field arrays
//        initialiseFieldInformation();
//
//        myFP = myOM.getFetchPlan().manageFetchPlanForClass(cmd);
//    }
//
//    /**
//     * Convenience method to initialise the field information.
//     **/
//    protected void initialiseFieldInformation()
//    {
//        int fieldCount = getHighestFieldNumber();
//
//        dirtyFields = new boolean[fieldCount];
//        loadedFields = new boolean[fieldCount];
//        unloadedFields = new boolean[fieldCount];
//    }
//    
//    /**
//     * returns the handler for callback events.
//     * @return the handler for callback events.
//     */
//    protected CallbackHandler getCallbackHandler()
//    {
//        return myOM.getCallbackHandler();
//    }
//    
//    /**
//     * returns indicators for which fields are second-class mutable.
//     * @return indicators for which fields are second-class mutable.
//     */
//    protected boolean[] getSecondClassMutableFields()
//    {
//        return cmd.getSecondClassMutableFieldFlags();
//    }
//    
//    /**
//     * returns indicators for which fields are non-primary key fields.
//     * @return indicators for which fields are non-primary key fields.
//     */
//    protected boolean[] getNonPrimaryKeyFields()
//    {
//        return cmd.getNonPrimaryKeyFieldFlags();
//    }
//    
//    /**
//     * returns field numbers of all fields.
//     * @return field numbers of all fields.
//     */
//    protected int[] getAllFieldNumbers()
//    {
//        return cmd.getAllFieldNumbers();
//    }
//    
//    /**
//     * returns field numbers of all non-primary-key fields.
//     * @return field numbers of all non-primary-key fields.
//     */
//    protected int[] getNonPrimaryKeyFieldNumbers()
//    {
//        return cmd.getNonPrimaryKeyFieldNumbers();
//    }
//    
//    /**
//     * returns field numbers of all second class mutable fields.
//     * @return field numbers of all second class mutable fields.
//     */
//    protected int[] getSecondClassMutableFieldNumbers()
//    {
//        return cmd.getSecondClassMutableFieldNumbers();
//    }
//
//    /**
//     * Accessor for the StoreManager used for this object.
//     * @return The StoreManager.
//     **/
//    public StoreManager getStoreManager()
//    {
//        return myOM.getStoreManager();
//    }
//
//    /**
//     * Accessor for the ClassMetaData for this object.
//     * @return The ClassMetaData.
//     **/
//    public AbstractClassMetaData getClassMetaData()
//    {
//        return cmd;
//    }
//
//    /**
//     * Accessor for the MetaDataManager to use for this object.
//     * Simply a wrapper accessor method. 
//     * @return The MetaDataManager.
//     **/
//    public MetaDataManager getMetaDataManager()
//    {
//        return myOM.getMetaDataManager();
//    }
//
//    /**
//     * Accessor for the ObjectManager for this object.
//     * @return The Object Manager.
//     **/
//    public ObjectManager getObjectManager()
//    {
//        return myOM;
//    }
//
//    /**
//     * Accessor for the Persistent Capable object.
//     * @return The PersistentCapable object
//     **/
//    public PersistenceCapable getObject()
//    {
//        return myPC;
//    }
//
//    /**
//     * Accessor for the LifeCycleState
//     * @return the LifeCycleState
//     */
//    public LifeCycleState getLifecycleState()
//    {
//        return myLC;
//    }
//
//    /**
//     * Accessor for the Restore Values flag 
//     * @return Whether to restore values
//     */
//    public boolean isRestoreValues()
//    {
//        return restoreValues;
//    }
//
//    /**
//     * Mutator for the Restore Values flag 
//     * @param restore_values Whether to restore values
//     */
//    protected void setRestoreValues(boolean restore_values)
//    {
//        restoreValues = restore_values;
//    }
//
//    /**
//     * Accessor for the internal object id of the object we are managing.
//     * This will return the "id" if it has been set, otherwise a temporary id based on this StateManager.
//     * @return The internal object id
//     */
//    public Object getInternalObjectId()
//    {
//        if (myID != null)
//        {
//            return myID;
//        }
//        else if (myInternalID == null)
//        {
//            // Assign a temporary internal "id" based on the object itself until our real identity is assigned
////          myInternalID = super.toString();
//            myInternalID = new IdentityReference(this);
//            return myInternalID;
//        }
//        else
//        {
//            return myInternalID;
//        }
//    }
//
//    // -------------------------- Lifecycle Methods ---------------------------
//
//    /**
//     * Method to disconnect any cloned persistence capable objects from their
//     * StateManager.
//     * @param pc The PersistenceCapable object
//     * @return Whether the object was disconnected.
//     **/
//    protected abstract boolean disconnectClone(PersistenceCapable pc);
//
//    /**
//     * Tests whether this object is dirty.
//     *
//     * Instances that have been modified, deleted, or newly
//     * made persistent in the current transaction return true.
//     *
//     * <P>Transient nontransactional instances return false (JDO spec), but the
//     * JPOX implementation does not currently support the transient
//     * transactional state.
//     *
//     * @see PersistenceCapable#jdoMakeDirty(String fieldName)
//     * @param pc the calling PersistenceCapable instance
//     * @return true if this instance has been modified in current transaction.
//     */
//    public boolean isDirty(PersistenceCapable pc)
//    {
//        if (disconnectClone(pc))
//        {
//            return false;
//        }
//        else
//        {
//            return myLC.isDirty();
//        }
//    }
//
//    /**
//     * Tests whether this object is transactional.
//     *
//     * Instances that respect transaction boundaries return true.  These
//     * instances include transient instances made transactional as a result of
//     * being the target of a makeTransactional method call; newly made
//     * persistent or deleted persistent instances; persistent instances read
//     * in data store transactions; and persistent instances modified in
//     * optimistic transactions.
//     * <P>
//     * Transient nontransactional instances return false.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @return true if this instance is transactional.
//     */
//    public boolean isTransactional(PersistenceCapable pc)
//    {
//        if (disconnectClone(pc))
//        {
//            return false;
//        }
//        else
//        {
//            return myLC.isTransactional();
//        }
//    }
//
//    /**
//     * Tests whether this object is persistent.
//     * Instances whose state is stored in the data store return true.
//     * Transient instances return false.
//     * @see ObjectManager#persistObject(Object pc)
//     * @param pc the calling PersistenceCapable instance
//     * @return true if this instance is persistent.
//     */
//    public boolean isPersistent(PersistenceCapable pc)
//    {
//        if (disconnectClone(pc))
//        {
//            return false;
//        }
//        else
//        {
//            return myLC.isPersistent();
//        }
//    }
//
//    /**
//     * Tests whether this object has been newly made persistent.
//     * Instances that have been made persistent in the current transaction
//     * return true.
//     * <P>
//     * Transient instances return false.
//     *
//     * @see ObjectManager#persistObject(Object pc)
//     * @param pc the calling PersistenceCapable instance
//     * @return true if this instance was made persistent
//     * in the current transaction.
//     */
//    public boolean isNew(PersistenceCapable pc)
//    {
//        if (disconnectClone(pc))
//        {
//            return false;
//        }
//        else
//        {
//            return myLC.isNew();
//        }
//    }
//
//    /**
//     * Tests whether this object has been deleted.
//     * Instances that have been deleted in the current transaction return true.
//     *
//     * <P>Transient instances return false.
//     *
//     * @see ObjectManager#deleteObject(Object pc)
//     * @param pc the calling PersistenceCapable instance
//     * @return true if this instance was deleted
//     * in the current transaction.
//     */
//    public boolean isDeleted(PersistenceCapable pc)
//    {
//        if (disconnectClone(pc))
//        {
//            return false;
//        }
//        else
//        {
//            return myLC.isDeleted();
//        }
//    }
//
//    // -------------------------- Version handling ----------------------------
//
//    /** 
//     * Return the object representing the version of the calling instance.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @return the object representing the version of the calling instance
//     * @since JDO 2.0
//     */    
//    public Object getVersion(PersistenceCapable pc)
//    {
//        if (pc == myPC)
//        {
//            // JIRA-2993 This used to return myVersion but now we use transactionalVersion
//            return transactionalVersion;
//        }
//        else
//        {
//            return null;
//        }
//    }
//
//    /**
//     * Sets the value for the version column in a transaction not yet committed
//     * @param version The version
//     */
//    public void setTransactionalVersion(Object version)
//    {
//        this.transactionalVersion = version;
//    }
//
//    /** Return the object representing the transactional version 
//     * of the calling instance.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @return the object representing the version of the calling instance
//     * @since 1.1.1
//     */    
//    public Object getTransactionalVersion(PersistenceCapable pc)
//    {
//        return this.transactionalVersion;
//    }
//
//    /**
//     * Sets the value for the version column in the datastore
//     * @param version The version
//     */
//    public void setVersion(Object version)
//    {
//        this.myVersion = version;
//        this.transactionalVersion = version;
//    }
//
//    /**
//     * Convenience accessor for whether this StateManager manages an embedded/serialised object.
//     * @return Whether the managed object is embedded/serialised.
//     */
//    public boolean isEmbedded()
//    {
//        return pcObjectType > 0;
//    }
//
//    /**
//     * Method to set this StateManager as managing an embedded/serialised object.
//     * @param embeddedType The type of object being managed
//     */
//    public void setPcObjectType(int embeddedType)
//    {
//        this.pcObjectType = embeddedType;
//    }
//
//    /**
//     * Accessor for the PC object type (whether it is PC, embedded PC, etc).
//     * @return PC Object Type
//     */
//    public int getPcObjectType()
//    {
//        return pcObjectType;
//    }
//
//    // -------------------------- Field Handling Methods ------------------------------
//
//    /**
//     * Accessor for the highest field number in this class
//     * @return The highest field number
//     */
//    public int getHighestFieldNumber()
//    {
//        return cmd.getFieldCount();
//    }
//
//    /**
//     * Accessor for whether the current fetch plan fields are loaded.
//     * @return Whether the fetch plan fields are all loaded.
//     */
//    protected boolean isFetchPlanLoaded()
//    {
//        int[] fpFields = myFP.getFieldsInActualFetchPlan();
//        for (int i=0; i<fpFields.length; ++i)
//        {
//            if (!loadedFields[fpFields[i]])
//            {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * Accessor for the names of the fields that are dirty.
//     * @return Names of the dirty fields
//     */
//    public String[] getDirtyFieldNames()
//    {
//        int[] dirtyFieldNumbers = getFlagsSetTo(dirtyFields, true);
//        if (dirtyFieldNumbers != null && dirtyFieldNumbers.length > 0)
//        {
//            String[] dirtyFieldNames = new String[dirtyFieldNumbers.length];
//            for (int i=0;i<dirtyFieldNumbers.length;i++)
//            {
//                dirtyFieldNames[i] = cmd.getManagedFieldAbsolute(dirtyFieldNumbers[i]).getName();
//            }
//            return dirtyFieldNames;
//        }
//        return null;
//    }
//
//    /**
//     * Accessor for the names of the fields that are loaded.
//     * @return Names of the loaded fields
//     */
//    public String[] getLoadedFieldNames()
//    {
//        int[] loadedFieldNumbers = getFlagsSetTo(loadedFields, true);
//        if (loadedFieldNumbers != null && loadedFieldNumbers.length > 0)
//        {
//            String[] loadedFieldNames = new String[loadedFieldNumbers.length];
//            for (int i=0;i<loadedFieldNumbers.length;i++)
//            {
//                loadedFieldNames[i] = cmd.getManagedFieldAbsolute(loadedFieldNumbers[i]).getName();
//            }
//            return loadedFieldNames;
//        }
//        return null;
//    }
//
//    /**
//     * Method to clear all dirty flags on the object.
//     */
//    protected void clearDirtyFlags()
//    {
//        dirty = false;
//        clearFlags(dirtyFields);
//    }
//    
//    /**
//     * Method to clear all dirty flags on the object.
//     * @param fields the fields to clear
//     */
//    protected void clearDirtyFlags(int[] fields)
//    {
//        dirty = false;
//        clearFlags(dirtyFields,fields);
//    }
//
//    // -------------------------- providedXXXField Methods ----------------------------
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedBooleanField(PersistenceCapable pc, int field, boolean currentValue)
//    {
//        currFM.storeBooleanField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedByteField(PersistenceCapable pc, int field, byte currentValue)
//    {
//        currFM.storeByteField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedCharField(PersistenceCapable pc, int field, char currentValue)
//    {
//        currFM.storeCharField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedDoubleField(PersistenceCapable pc, int field, double currentValue)
//    {
//        currFM.storeDoubleField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedFloatField(PersistenceCapable pc, int field, float currentValue)
//    {
//        currFM.storeFloatField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedIntField(PersistenceCapable pc, int field, int currentValue)
//    {
//        currFM.storeIntField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedLongField(PersistenceCapable pc, int field, long currentValue)
//    {
//        currFM.storeLongField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedShortField(PersistenceCapable pc, int field, short currentValue)
//    {
//        currFM.storeShortField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc   the calling PersistenceCapable instance
//     * @param field   the field number
//     * @param currentValue   the current value of the field
//     */
//    public void providedStringField(PersistenceCapable pc, int field, String currentValue)
//    {
//        currFM.storeStringField(field, currentValue);
//    }
//
//    /**
//     * This method is called from the associated PersistenceCapable when its
//     * PersistenceCapable.jdoProvideFields() method is invoked. Its purpose is
//     * to provide the value of the specified field to the StateManager.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param fieldNumber the field number
//     * @param currentValue the current value of the field
//     */
//    public void providedObjectField(PersistenceCapable pc, int fieldNumber, Object currentValue)
//    {
//        currFM.storeObjectField(fieldNumber, currentValue);
//    }
//
//    // -------------------------- replacingXXXField Methods ----------------------------
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a boolean field.
//     *
//     * @param pc the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public boolean replacingBooleanField(PersistenceCapable pc, int field)
//    {
//        boolean value = currFM.fetchBooleanField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a byte field.
//     *
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public byte replacingByteField(PersistenceCapable obj, int field)
//    {
//        byte value = currFM.fetchByteField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a char field.
//     *
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public char replacingCharField(PersistenceCapable obj, int field)
//    {
//        char value = currFM.fetchCharField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a double field.
//     *
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public double replacingDoubleField(PersistenceCapable obj, int field)
//    {
//        double value = currFM.fetchDoubleField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a float field.
//     *
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public float replacingFloatField(PersistenceCapable obj, int field)
//    {
//        float value = currFM.fetchFloatField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a int field.
//     *
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public int replacingIntField(PersistenceCapable obj, int field)
//    {
//        int value = currFM.fetchIntField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a long field.
//     *
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public long replacingLongField(PersistenceCapable obj, int field)
//    {
//        long value = currFM.fetchLongField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a short field.
//     *
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public short replacingShortField(PersistenceCapable obj, int field)
//    {
//        short value = currFM.fetchShortField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of a String field.
//     *
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public String replacingStringField(PersistenceCapable obj, int field)
//    {
//        String value = currFM.fetchStringField(field);
//        loadedFields[field] = true;
//    
//        return value;
//    }
//
//    /**
//     * This method is invoked by the PersistenceCapable object's
//     * jdoReplaceField() method to refresh the value of an Object field.
//     * @param obj the calling PersistenceCapable instance
//     * @param field the field number
//     * @return the new value for the field
//     */
//    public Object replacingObjectField(PersistenceCapable obj, int field)
//    {
//        try
//        {
//            Object value = currFM.fetchObjectField(field);
//            loadedFields[field] = true;
//            return value;
//        }
//        catch (EndOfFetchPlanGraphException eodge)
//        {
//            // Beyond the scope of the fetch-depth when detaching
//            return null;
//        }
//    }
//
//    // -------------------------- getXXXField Methods ----------------------------
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public boolean getBooleanField(PersistenceCapable pc, int field, boolean currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public byte getByteField(PersistenceCapable pc, int field, byte currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public char getCharField(PersistenceCapable pc, int field, char currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public double getDoubleField(PersistenceCapable pc, int field, double currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public float getFloatField(PersistenceCapable pc, int field, float currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public int getIntField(PersistenceCapable pc, int field, int currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public long getLongField(PersistenceCapable pc, int field, long currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public short getShortField(PersistenceCapable pc, int field, short currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public String getStringField(PersistenceCapable pc, int field, String currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    /**
//     * This method is called by the associated PersistenceCapable if the
//     * value for the specified field is not cached (StateManager.isLoaded()
//     * fails). In this implementation of the StateManager, isLoaded() has a
//     * side effect of loading unloaded information and will always return true.
//     * As such, this method should never be called.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @param field the field number 
//     * @param currentValue the current value of the field
//     * @return the new value for the field
//     */
//    public Object getObjectField(PersistenceCapable pc, int field, Object currentValue)
//    {
//        throw new JPOXException(LOCALISER.msg("StateManager.MethodNotSupported"));
//    }
//
//    // ------------------------------ Helper Methods ---------------------------
//
//    /**
//     * Compares two objects for equality, where one or both of the object
//     * references may be null.
//     *
//     * @return  <code>true</code> if the objects are both <code>null</code> or
//     *          compare equal according to their equals() method,
//     *          <code>false</code> otherwise.
//     */
//    protected static boolean equals(Object o1, Object o2)
//    {
//        return o1 == null ? (o2 == null) : o1.equals(o2);
//    }
//
//    /**
//     * Utility to clear the supplied flags.
//     * @param flags
//     */
//    protected static void clearFlags(boolean[] flags)
//    {
//        for (int i = 0; i < flags.length; i++)
//        {
//            flags[i] = false;
//        }
//    }
//
//    /**
//     * Utility to clear the supplied flags.
//     * @param flags
//     * @param fields fields numbers where the flags will be cleared
//     */
//    protected static void clearFlags(boolean[] flags, int[] fields)
//    {
//        for (int i = 0; i < fields.length; i++)
//        {
//            flags[fields[i]] = false;
//        }
//    }
//    
//    /**
//     * Returns an array of integers containing the indices of all elements in
//     * <tt>flags</tt> that are in the <tt>state</tt> passed as argument.
//     * @param flags Array of flags (true or false)
//     * @param state The state to search (true or false)
//     * @return The settings of the flags
//     */
//    public static int[] getFlagsSetTo(boolean[] flags, boolean state)
//    {
//        int[] temp = new int[flags.length];
//        int j = 0;
//
//        for (int i = 0; i < flags.length; i++)
//        {
//            if (flags[i] == state)
//            {
//                temp[j++] = i;
//            }
//        }
//
//        if (j != 0)
//        {
//            int[] fieldNumbers = new int[j];
//            System.arraycopy(temp, 0, fieldNumbers, 0, j);
//
//            return fieldNumbers;
//        }
//        else
//        {
//            return null;
//        }
//    }
//
//    /**
//     * Returns an array of integers containing the indices of all elements in
//     * <tt>flags</tt> whose index occurs in <tt>indices</tt> and whose value is
//     * <tt>state</tt>.
//     */
//    protected static int[] getFlagsSetTo(boolean[] flags, int[] indices, boolean state)
//    {
//        int[] temp = new int[indices.length];
//        int j = 0;
//
//        for (int i = 0; i < indices.length; i++)
//        {
//            if (flags[indices[i]] == state)
//            {
//                temp[j++] = indices[i];
//            }
//        }
//
//        if (j != 0)
//        {
//            int[] fieldNumbers = new int[j];
//            System.arraycopy(temp, 0, fieldNumbers, 0, j);
//
//            return fieldNumbers;
//        }
//        else
//        {
//            return null;
//        }
//    }
//    
//    /**
//     * Utility to take a peek at a field in the PersistenceCapable object.
//     * @param obj The PersistenceCapable object
//     * @param fieldName The field to peek at
//     * @return The value of the field.
//     */
//    protected static Object peekField(Object obj, String fieldName)
//    {
//        try
//        {
//            /*
//             * This doesn't work due to security problems but you get the idea.
//             * I'm trying to get field values directly without going through
//             * the provideField machinery.
//             */
//            Object value = obj.getClass().getDeclaredField(fieldName).get(obj);
//            if (value instanceof PersistenceCapable)
//            {
//                return StringUtils.toJVMIDString(value);
//            }
//            else
//            {
//                return value;
//            }
//        }
//        catch (Exception e)
//        {
//            return e.toString();
//        }
//    }
}

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
2002 Mike Martin - unknown changes
2003 Erik Bengtson - removed exist() operation
2004 Andy Jefferson - added getHighestFieldNumber()
2005 Andy Jefferson - javadocs
2007 Xuan Baldauf - Contrib of notifyMadePersistentClean() (needed by DB4O plugin).
2007 Xuan Baldauf - Contrib of internalAreAllFieldsLoaded(), internalIsAtLeastOneFieldDirty(), internalIsAtLeastOneFieldDirtyVerify(), createDirtyFieldsBitmapCopy() (needed by DB4O plugin). 
    ...
**********************************************************************/
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdo;


/**
 * Extension to SUN's JDO StateManager class. This makes the assumption that a
 * StateManager corresponds to ONE PersistenceCapable object. As a result of
 * this assumption various methods are added to the basic JDO definition.
 * TODO Remove the dependence on JDO
 *
 * @version $Revision: 1.1 $
 **/
public interface StateManager
{
//    // Types of PC objects that can be managed by this StateManager
//    /** PC **/
//    public static int PC = 0;
//    /** Embedded (or serialised) PC **/
//    public static int EMBEDDED_PC = 1;
//    /** Embedded (or serialised) Collection Element PC **/
//    public static int EMBEDDED_COLLECTION_ELEMENT_PC = 2;
//    /** Embedded (or serialised) Map Key PC **/
//    public static int EMBEDDED_MAP_KEY_PC = 3;
//    /** Embedded (or serialised) Map Value PC **/
//    public static int EMBEDDED_MAP_VALUE_PC = 4;
//    
//    /**
//     * Accessor for the object managed by this StateManager.
//     * @return The object
//     */
//    PersistenceCapable getObject();
//
//    /**
//     * Accessor for the id of the object managed by this StateManager.
//     * @return The identity of the object
//     */
//    Object getInternalObjectId();
//    
//    /**
//     * return a copy from the object Id
//     * @param pc the PersistenceCapable object
//     * @return the object id
//     */
//    Object getExternalObjectId(PersistenceCapable pc);
//
//    /**
//      * Returns the ObjectManager that owns the StateManager instance
//      * @return The ObjectManager
//     */
//    ObjectManager getObjectManager();
//
//    /**
//     * Accessor for the manager for the store.
//     * @return Store Manager
//     */
//    StoreManager getStoreManager();
//
//    /**
//     * Accessor for the manager for MetaData.
//     * @return MetaData manager
//     */
//    MetaDataManager getMetaDataManager();
//
//    /**
//     * Method to mark the specified (absolute) field number as dirty.
//     * @param fieldNumber The (absolute) field number of the field
//     */
//    void makeDirty(int fieldNumber);
//
//    /**
//     * Accessor for the names of all dirty fields.
//     * @return Names of all dirty fields
//     */
//    String[] getDirtyFieldNames();
//
//    /**
//     * Accessor for the names of all loaded fields.
//     * @return Names of all loaded fields
//     */
//    String[] getLoadedFieldNames();
//
//    /**
//     * Marks the given field dirty for issuing an update after the insert.
//     * @param pc The Persistable object
//     * @param fieldNumber The no of field to mark as dirty. 
//     */
//    void updateFieldAfterInsert(Object pc, int fieldNumber);
//
//    /**
//     * Update the acitvity state.
//     * @param activityState the activity state
//     * @param table Datastore class that has just been processed
//     */
//    void changeActivityState(ActivityState activityState, DatastoreClass table);
//
//    /**
//     * Method to run reachability from this StateManager.
//     * @param reachables List of reachable StateManagers so far
//     */
//    void runReachability(Set reachables);
//
//    /**
//     * Method to make the managed object transactional.
//     */
//    void makeTransactional();
//
//    /**
//     * Method to make the managed object nontransactional.
//     */
//    void makeNontransactional();
//
//    /**
//     * Method to make the managed object transient.
//     * @param state Object containing the state of any fetch plan processing
//     */
//    void makeTransient(FetchPlanState state);
//
//    /**
//     * Method to make the managed object persistent.
//     */
//    void makePersistent();
//    
//    /**
//     * Method to make Transactional Transient instances persistent
//     */
//    void makePersistentTransactionalTransient();
//
//    /**
//     * Method to delete the object from persistence.
//     */
//    void deletePersistent();
//
//    /**
//     * Method to attach to this the detached persistable instance
//     * @param detachedPC the detached persistable instance to be attached
//     * @param embedded Whether it is embedded
//     * @return The attached copy
//     * @since 1.1
//     */
//    Object attachCopy(Object detachedPC, boolean embedded);
//
//    /**
//     * Method to make detached copy of this instance
//     * @param state State for the detachment process
//     * @return the detached PersistenceCapable instance
//     * @since 1.1
//     */
//    Object detachCopy(FetchPlanState state);
//
//    /**
//     * Method to detach the PersistenceCapable object.
//     * @param state State for the detachment process
//     */
//    void detach(FetchPlanState state);
//
//    /**
//     * Validates whether the persistence capable instance exists in the
//     * datastore. If the instance does not exist in the datastore, this method
//     * will fail raising a JPOXObjectNotFoundException.
//     */
//    void validate();
//
//    /**
//     * Method to change the object state to evicted.
//     */
//    void evict();
//
//    /**
//     * Method to refresh the values of the currently loaded fields in the managed object.
//     */
//    void refresh();
//
//    /**
//     * Method to retrieve the fields for this object.
//     * @param fgOnly Whether to retrieve just the current fetch plan fields
//     */
//    void retrieve(boolean fgOnly);
//
//    /**
//     * Method to retrieve the object.
//     * @param fetchPlan the fetch plan to load fields
//     **/
//    void retrieve(FetchPlan fetchPlan);
//
//    /**
//     * Convenience interceptor to allow operations to be performed before the begin is performed
//     * @param tx The transaction
//     */
//    void preBegin(Transaction tx);
//    
//    /**
//     * Convenience interceptor to allow operations to be performed after the commit is performed
//     * but before returning control to the application.
//     * @param tx The transaction
//     */
//    void postCommit(Transaction tx);
//
//    /**
//     * Convenience interceptor to allow operations to be performed before any rollback is
//     * performed.
//     * @param tx The transaction
//     */
//    void preRollback(Transaction tx);
//
//    /**
//     * Method to flush all changes to the datastore.
//     */
//    void flush();
//
//    /**
//     * Method to return the current value of the specified field.
//     * @param fieldNumber (absolute) field number of the field
//     * @return The current value
//     */
//    Object provideField(int fieldNumber);
//
//    /**
//     * Method to obtain updated field values from the passed FieldManager.
//     * @param fieldNumbers The numbers of the fields
//     * @param fm The fieldManager
//     */
//    void provideFields(int fieldNumbers[], FieldManager fm);
//
//    /**
//     * Method to change the value of the specified field.
//     * @param fieldNumber (absolute) field number of the field
//     * @param value The new value.
//     * @param makeDirty Whether to make the field dirty when replacing it
//     */
//    void replaceField(int fieldNumber, Object value, boolean makeDirty);
//
//    /**
//     * Method to update the data in the object with the values from the passed FieldManager
//     * @param fieldNumbers (absolute) field numbers of the fields to update
//     * @param fm The FieldManager
//     * @param replaceWhenDirty Whether to replace these fields if the field is dirty
//     */
//    void replaceFields(int fieldNumbers[], FieldManager fm, boolean replaceWhenDirty);
//
//    /**
//     * Method to update the data in the object with the values from the passed FieldManager
//     * @param fieldNumbers (absolute) field numbers of the fields to update
//     * @param fm The FieldManager
//     */
//    void replaceFields(int fieldNumbers[], FieldManager fm);
//
//    /**
//     * Method to update the data in the object with the values from the passed
//     * FieldManager. Only non loaded fields are updated
//     * @param fieldNumbers (absolute) field numbers of the fields to update
//     * @param fm The FieldManager
//     */
//    void replaceNonLoadedFields(int fieldNumbers[], FieldManager fm);
//
//    /**
//     * Method to create a new SCO wrapper for a second class field.
//     * @param fieldNumber Number of the field
//     * @param value The value to give it
//     * @param forUpdate Whether it needs updating in the datastore
//     * @return The SCO wrapper
//     */
//    Object newSCOInstance(int fieldNumber, Object value, boolean forUpdate);
//
//    /**
//     * Wrap a field value with a SCO instance.
//     * @param fieldNumber
//     * @param value the value to be wrapped
//     * @param makeDirty Whether to make the field dirty when replacing it with its SCO wrapper
//     * @return the new SCO instance
//     */
//    Object replaceSCOFieldWithWrapper(int fieldNumber, Object value, boolean makeDirty);
//
//    /**
//     * Method to return an empty SCO wrapper for the specified field.
//     * @param fieldNumber (absolute) field number of the SCO field to wrap
//     * @return The (empty) SCO wrapper
//     */
//    Object newSCOEmptyInstance(int fieldNumber);
//
//    /**
//     * When an object is being attached it will temporarily have a StateManager connected, and during this time
//     * this method gives access to the attached variant of the object.
//     * @return The attached PC object (or null if not temporarily connected detached object)
//     */
//    Object getAttachedPC();
//
//    /**
//     * Tests whether this object is new yet waiting to be flushed to the datastore.
//     * @return true if this instance is waiting to be flushed
//     */
//    boolean isWaitingToBeFlushedToDatastore();
//
//    /**
//     * Method to allow the setting of the id of the PC object. This is used when it is obtained after persisting
//     * the object to the datastore. In the case of RDBMS, this may be via auto-increment, or in the case of ODBMS
//     * this may be an accessor for the id after storing.
//     * @param id the id received from the datastore. May be an OID, or the key value for an OID, or an application id.
//     */
//    void setPostStoreNewObjectId(Object id);
//
//    /**
//     * Sets the value for the version column in the datastore. Update the transactional version too
//     * @param version The version
//     */
//    void setVersion(Object version);
//
//    /** Return the object representing the transactional version 
//     * of the calling instance.
//     * @param pc the calling <code>PersistenceCapable</code> instance
//     * @return the object representing the version of the calling instance
//     * @since 1.1.1
//     */    
//    Object getTransactionalVersion (PersistenceCapable pc);  
//    
//    /**
//     * Sets the value for the version column in a transaction not yet committed
//     * @param optimisticTransactionalVersion
//     */
//    void setTransactionalVersion(Object optimisticTransactionalVersion);
//
//    /**
//     * Accessor for the highest field number
//     * @return Highest field number
//     */
//    int getHighestFieldNumber();
//
//    /**
//     * Accessor for an L2 cacheable form of this object.
//     * @return The L2 cacheable object
//     */
//    CachedPC getL2CacheableObject();
//
//    /**
//     * Diagnostic method to dump the current state to the provided PrintWriter.
//     * @param out The PrintWriter
//     */
//    void dump(PrintWriter out);
//    
//    /**
//     * Accessor for the ClassMetaData for this object.
//     * @return The ClassMetaData.
//     **/
//    AbstractClassMetaData getClassMetaData();
//    
//    /**
//     * Nullify fields with reference to PersistenceCapable or SCO instances 
//     */
//    void nullifyFields();
//
//    /**
//     * Fetchs from the database all fields that are not currently loaded and that are in the current
//     * fetch group. Called by lifecycle transitions.
//     * @since 1.1
//     */
//    void loadUnloadedFieldsInFetchPlan();
//
//    /**
//     * Method to load all unloaded fields in the FetchPlan.
//     * Recurses through the FetchPlan objects and loads fields of sub-objects where needed.
//     * @param state The FetchPlan state
//     */
//    void loadFieldsInFetchPlan(FetchPlanState state);
//
//    /**
//     * Loads all unloaded fields of the managed class that are in the current FetchPlan.
//     * Called by life-cycle transitions.
//     * @param fetchPlan The FetchPlan
//     * @since 1.1
//     */
//    void loadUnloadedFieldsOfClassInFetchPlan(FetchPlan fetchPlan);
//
//    /**
//     * Fetch from the database all fields that are not currently loaded regardless of whether
//     * they are in the current fetch group or not. Called by lifecycle transitions.
//     * @since 1.1
//     */
//    void loadUnloadedFields();
//
//    /**
//     * Drop any loaded state for the given field, so upon next access it will get reloaded.
//     * @param fieldName
//     */
//    void unloadField(String fieldName);
//
//    /**
//     * Method that will unload all fields that are not in the FetchPlan.
//     * @since 1.2
//     */
//    void unloadNonFetchPlanFields();
//
//    /**
//     * Convenience method to reset the detached state in the current object.
//     */
//    void resetDetachState();
//
//    /**
//     * Disconnect the StateManager from the PersistenceManager and PC object.
//     */
//    void disconnect();
//    
//    //called by lifecycle ops
//    void evictFromTransaction();
//
//    void enlistInTransaction();
//    
//    /**
//     * Refreshes from the database all fields currently loaded.
//     * Called by life-cycle transitions.
//     */
//    void refreshLoadedFields();
//
//    /**
//     * Method to clear all saved fields on the object.
//     **/
//    void clearSavedFields();
//    
//    /**
//     * Refreshes from the database all fields in fetch plan.
//     * Called by life-cycle transitions.
//     */
//    void refreshFieldsInFetchPlan();
//    
//    /**
//     * Method to clear all fields that are not part of the primary key of the object.
//     **/
//    void clearNonPrimaryKeyFields();
//    
//    /**
//     * Method to restore all fields of the object.
//     **/
//    void restoreFields();
//
//    /**
//     * Method to save all fields of the object.
//     **/
//    void saveFields();
//    
//    /**
//     * Method to clear all fields of the object.
//     **/
//    void clearFields();
//    
//    /**
//     * Registers the pc class in the cache
//     */
//    void registerTransactional();
//
//    /**
//     * Accessor for the Restore Values flag 
//     * @return Whether to restore values
//     */
//    boolean isRestoreValues();
//    
//    /**
//     * Method to clear all loaded flags on the object.
//     **/
//    void clearLoadedFlags();
//
//    //used by SCO classes
//    /**
//     * Method to register an owner StateManager with this embedded/serialised object.
//     * @param ownerSM The owning State Manager.
//     * @param ownerFieldNumber The field number in the owner that the embedded/serialised object is stored as
//     */
//    void addEmbeddedOwner(StateManager ownerSM, int ownerFieldNumber);
//    
//    //used by PM classes
//    /**
//     * Convenience method to load the passed field values.
//     * Loads the fields using any required fetch plan and calls jdoPostLoad() as appropriate.
//     * @param fv Field Values to load (including any fetch plan to use when loading)
//     */
//    void loadFieldValues(FieldValues fv);
//
//    /**
//     * Look to the database to determine which
//     * class this object is. This parameter is a hint. Set false, if it's
//     * already determined the correct pcClass for this pc "object" in a certain
//     * level in the hierarchy. Set to true and it will look to the database.
//     * @param fv the initial field values of the object.
//     */
//    void checkInheritance(FieldValues fv);
//    
//    //used by JPOXHelper
//    /**
//     * Convenience method to retrieve the detach state from the passed State Manager's object
//     * @param sm The State Manager
//     */
//    void retrieveDetachState(org.jpox.StateManager sm);
//
//    //used by embedded mappings
//    /**
//     * Method to set this StateManager as managing an embedded/serialised object.
//     * @param embeddedType The type of object being managed
//     */
//    void setPcObjectType(int embeddedType);
//
//    /**
//     * Accessor for the PC object type.
//     * @return PC Object type (PC, embedded PC, etc)
//     */
//    int getPcObjectType();
//
//    /**
//     * Accessor for the overall owner StateManagers of the managed object when embedded.
//     * @return Owning StateManagers when embedded (if any)
//     */
//    StateManager[] getEmbeddedOwners();
//
//    //used by container mappings
//    /**
//     * Method to set the storing PC flag.
//     */
//    void setStoringPC();
//
//    /**
//     * Method to unset the storing PC flag.
//     */
//    void unsetStoringPC();
// 
//    //used by scostore
//    /**
//     * Convenience accessor for whether this StateManager manages an embedded/serialised object.
//     * @return Whether the managed object is embedded/serialised.
//     */
//    boolean isEmbedded();
//
//    /**
//     * Method to set the value for an external field stored against this object
//     * @param mapping The mapping for the (external) field
//     * @param value The value that this field has
//     */
//    void setExternalFieldValueForMapping(JavaTypeMapping mapping, Object value);
//
//    /**
//     * Accessor for the value of an external field.
//     * This is used when inserting this object so that we can insert the external field values too.
//     * @param mapping The external field mapping
//     * @return The value for this mapping
//     */
//    Object getValueForExternalField(JavaTypeMapping mapping);
//
//    /**
//     * Convenience method to load a field from the datastore.
//     * Used in attaching fields and checking their old values (so we dont
//     * want any postLoad method being called).
//     * TODO Merge this with one of the loadXXXFields methods.
//     * @param fieldNumber The field number.
//     */
//    void loadFieldFromDatastore(int fieldNumber);
//
//    /**
//     * Method that replaces the PC managed by this StateManager to be the supplied object.
//     * This happens when we want to get an object for an id and create a Hollow object, and then validate
//     * against the datastore. This validation can pull in a new object graph from the datastore (e.g for DB4O)
//     * @param pc The PersistenceCapable to use
//     */
//    void replaceManagedPC(PersistenceCapable pc);
//    
//    Object getVersion(PersistenceCapable pc);
//    
//    boolean isLoaded(PersistenceCapable pc, int fieldNumber);
//    
//    void setObjectField(PersistenceCapable pc, int fieldNumber, Object oldValue, Object newValue);
//
//    /**
//     * Tests whether this object is being inserted.
//     * @return true if this instance is inserting.
//     */
//    boolean isInserting();
//
//    /**
//     * Returns whether the specified field of this object is inserted in the datastore.
//     * Only applies during the makePersistent process.
//     * @param fieldNumber Number of the field
//     * @return Whether it is inserted to the level of this field
//     */
//    boolean isInserted(int fieldNumber);
//
//    /**
//     * Tests whether this object is in the process of being deleted.
//     * @return true if this instance is being deleted.
//     */
//    boolean isDeleting();
//
//    /**
//     * Returns whether the object being managed is dirty.
//     * @return whether at least one field is dirty by checking the dirty flag.
//     */
//    public boolean isDirty();
//
//    /**
//     * Accessor for whether the managed object is deleted.
//     * @param pc The PC object
//     * @return Whether it is deleted
//     */
//    boolean isDeleted(PersistenceCapable pc);
//
//    /**
//     * Accessor for whether the managed object is new.
//     * @param pc The PC object
//     * @return Whether it is new
//     */
//    boolean isNew(PersistenceCapable pc);
//
//    /**
//     * Accessor for the object id.
//     * @param pc The PC object
//     * @return The (external) id
//     */
//    Object getObjectId(PersistenceCapable pc);
//    
//    /**
//     * Locate this object in the datastore.
//     * @throws JPOXObjectNotFoundException if the object doesnt exist.
//     */
//    void locate();
//
//    /**
//     * Returns whether all fields are loaded.
//     * @return Returns true if all fields are loaded.
//     */
//    public boolean getAllFieldsLoaded();
//
//    /**
//     * Creates a copy of the internal dirtyFields array.
//     * TODO Change this to return an array of the field numbers that are dirty
//     * @return a copy of the internal dirtyFields array.
//     */
//    public boolean[] getDirtyFields();
//    
//    /**
//     * Convenience method for datastores that retrieve objects directly and so need to have a degree of control
//     * over object state, with this method migrating thge managed object to P_CLEAN state.
//     */
//    public void migrateToPersistentClean();
}
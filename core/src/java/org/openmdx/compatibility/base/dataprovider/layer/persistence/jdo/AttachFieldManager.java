/**********************************************************************
Copyright (c) 2004 Erik Bengtson and others. All rights reserved. 
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
2005 Andy Jefferson - cater for object not being persistent
    ...
**********************************************************************/
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdo;

/**
 * Utility class to handle the attach of fields.
 * The attachment process has 2 distinct cases to cater for.
 * <OL>
 * <LI>The object was detached, has been updated, and needs reattaching.</LI>
 * <LI>The object was detached from a different datastore, and is being attached here.</LI>
 * </OL>
 * In the first case, the fields which are dirty have their values (and dirty flags) updated.
 * In the second case, all fields have their fields (and dirty flags) updated.
 * 
 * @version $Revision: 1.1 $
 */
public class AttachFieldManager extends AbstractFieldManager
{
//    /** Localiser for internationalisation. */
//    protected static final Localiser LOCALISER = Localiser.getInstance("org.jpox.Localisation");
//
//    /** the attached instance */
//    private final StateManager smAttached;
//
//    /** The second class mutable fields. */
//    private final boolean[] secondClassMutableFields;
//
//    /** The dirty fields. */
//    private final boolean dirtyFields[];
//
//    /** Whether the attached instance is persistent yet. */
//    private final boolean persistent;
//
//    /** Whether to cascade the attach to related fields. */
//    private final boolean cascadeAttach;
//
//    /**
//     * Constructor.
//     * @param smAttached the attached instance
//     * @param secondClassMutableFields second class mutable field flags
//     * @param dirtyFields dirty field flags
//     * @param persistent whether the object is persistent
//     * @param cascadeAttach Whether to cascade any attach calls to related fields
//     */
//    public AttachFieldManager(StateManager smAttached, 
//                              boolean secondClassMutableFields[], 
//                              boolean dirtyFields[],
//                              boolean persistent,
//                              boolean cascadeAttach)
//    {
//        this.smAttached = smAttached;
//        this.secondClassMutableFields = secondClassMutableFields;
//        this.dirtyFields = dirtyFields;
//        this.persistent = persistent;
//        this.cascadeAttach = cascadeAttach;
//    }
//
//    /**
//     * Method to store an object field into the attached instance
//     * @param fieldNumber Number of the field to store
//     * @param value the value in the detached instance
//     */
//    public void storeObjectField(int fieldNumber, Object value)
//    {
//        ApiAdapter api = smAttached.getObjectManager().getApiAdapter();
//        if (value == null)
//        {
//            Object oldValue = null;
//            AbstractClassMetaData cmd = smAttached.getClassMetaData();
//            AbstractPropertyMetaData fmd = cmd.getManagedFieldAbsolute(fieldNumber);
//            if (fmd.isDependent())
//            {
//                try
//                {
//                    // Get any old value of this field so we can do cascade-delete if being nulled
//                    smAttached.loadFieldFromDatastore(fieldNumber);
//                }
//                catch (Exception e)
//                {
//                    // Error loading the field so didnt exist before attaching anyway
//                }
//                oldValue = smAttached.provideField(fieldNumber);
//            }
//
//            if (dirtyFields[fieldNumber] || !persistent)
//            {
//                smAttached.makeDirty(fieldNumber);
//                smAttached.replaceField(fieldNumber, null, true);
//            }
//
//            if (fmd.isDependent() && !fmd.isEmbedded() &&
//                oldValue != null && value == null && api.isPersistable(oldValue))
//            {
//                // Check for a field storing a PC where it is being nulled and the other object is dependent
//                smAttached.flush(); // Flush the nulling of the field
//                JPOXLogger.PERSISTENCE.debug(LOCALISER.msg("StateManager.DeleteDependentNulledField", oldValue, fmd.getFullFieldName()));
//                smAttached.getObjectManager().deleteObjectInternal(oldValue);
//            }
//        }
//        else if (secondClassMutableFields[fieldNumber])
//        {
//            AbstractPropertyMetaData fmd = smAttached.getClassMetaData().getManagedFieldAbsolute(fieldNumber);
//            if (fmd.isSerialized())
//            {
//                // SCO Field is serialised so just update the column with this new value - dont do comparisons at the moment
//                smAttached.replaceField(fieldNumber, value, true);
//                smAttached.makeDirty(fieldNumber);
//            }
//            else
//            {
//                if (dirtyFields[fieldNumber] || !persistent)
//                {
//                    smAttached.makeDirty(fieldNumber);
//                }
//
//                // We have just created a new object of this class. A SCO mutable field may have been initialised at creation.
//                Object oldValue = smAttached.provideField(fieldNumber);
//                SCO sco;
//                if (oldValue == null || (oldValue != null && !(oldValue instanceof SCO)))
//                {
//                    // The field wasn't initialised at creation, so create an empty SCO wrapper and copy the new values to it
//                    sco = (SCO) smAttached.newSCOEmptyInstance(fieldNumber);
//                    if (sco instanceof SCOContainer)
//                    {
//                        // Load any containers to avoid update issues
//                        ((SCOContainer)sco).load();
//                    }
//                    smAttached.replaceField(fieldNumber, sco, true);
//                }
//                else
//                {
//                    // The field is already a SCO wrapper, so just copy the new values to it
//                    sco = (SCO) oldValue;
//                }
//                if (cascadeAttach)
//                {
//                    // Only trigger the cascade when required
//                    sco.attachCopy(value);
//                }
//            }
//        }
//        else if (api.isPersistable(value))
//        {
//            // PC field or reference field containing PC object
//            if (dirtyFields[fieldNumber] || !persistent)
//            {
//                smAttached.makeDirty(fieldNumber);
//            }
//
//            if (cascadeAttach)
//            {
//                // Field is "second-class" (no identity) if has "embedded" info or serialised
//                AbstractPropertyMetaData fmd = smAttached.getClassMetaData().getManagedFieldAbsolute(fieldNumber);
//                boolean sco = (fmd.getEmbeddedMetaData() != null || fmd.isSerialized() || fmd.isEmbedded());
//
//                // Only trigger the cascade attach when required
//                Object pcObj = smAttached.getObjectManager().attachObjectCopy(value, sco);
//
//                // Reinstate the dirty field since attach of the other end of an inverse may reset the flag
//                if (dirtyFields[fieldNumber] || !persistent)
//                {
//                    smAttached.makeDirty(fieldNumber);
//                }
//                else if (sco && value != null && smAttached.getObjectManager().getApiAdapter().isDirty(value))
//                {
//                    // Related PC is dirty, but the field in this class isn't, yet is embedded/serialised
//                    // so make it dirty to get the update
//                    smAttached.makeDirty(fieldNumber);
//                }
//                smAttached.replaceField(fieldNumber, pcObj, true);
//            }
//        }
//        else
//        {
//            if (dirtyFields[fieldNumber] || !persistent)
//            {
//                smAttached.makeDirty(fieldNumber);
//                smAttached.replaceField(fieldNumber, value, true);
//            }
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeBooleanField(int, boolean)
//     */
//    public void storeBooleanField(int fieldNumber, boolean value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeBooleanField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeByteField(int, byte)
//     */
//    public void storeByteField(int fieldNumber, byte value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeByteField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeCharField(int, char)
//     */
//    public void storeCharField(int fieldNumber, char value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeCharField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeDoubleField(int, double)
//     */
//    public void storeDoubleField(int fieldNumber, double value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeDoubleField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeFloatField(int, float)
//     */
//    public void storeFloatField(int fieldNumber, float value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeFloatField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeIntField(int, int)
//     */
//    public void storeIntField(int fieldNumber, int value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeIntField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeLongField(int, long)
//     */
//    public void storeLongField(int fieldNumber, long value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeLongField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeShortField(int, short)
//     */
//    public void storeShortField(int fieldNumber, short value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeShortField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * @see FieldConsumer#storeStringField(int, java.lang.String)
//     */
//    public void storeStringField(int fieldNumber, String value)
//    {
//        if (dirtyFields[fieldNumber] || !persistent)
//        {
//            smAttached.makeDirty(fieldNumber);
//            SingleValueFieldManager sfv = new SingleValueFieldManager();
//            sfv.storeStringField(fieldNumber, value);
//            smAttached.replaceFields(new int[]{fieldNumber}, sfv);
//        }
//    }
}
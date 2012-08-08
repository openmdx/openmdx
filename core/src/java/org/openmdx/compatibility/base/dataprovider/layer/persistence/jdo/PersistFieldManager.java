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
    ...
**********************************************************************/
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdo;


/**
 * Field manager that perists all unpersisted PC objects referenced from the source object.
 * If any collection/map fields are not currently using SCO wrappers they will be converted to do so.
 * Effectively provides "persistence-by-reachability" (at persist).
 * 
 * @version $Revision: 1.1 $
 **/
public class PersistFieldManager extends AbstractFieldManager
{
//    /** StateManager for the owning object. */
//    private final StateManager sm;
//
//    /** Whether this manager will replace any SCO fields with SCO wrappers. */
//    private final boolean replaceSCOsWithWrappers;
//
//    /**
//     * Constructor.
//     * @param sm The state manager for the object.
//     * @param replaceSCOsWithWrappers Whether to swap any SCO field objects for SCO wrappers
//     **/
//    public PersistFieldManager(StateManager sm, boolean replaceSCOsWithWrappers)
//    {
//        this.sm = sm;
//        this.replaceSCOsWithWrappers = replaceSCOsWithWrappers;
//    }
//
//    /**
//     * Utility method to process the passed persistable object.
//     * @param pc The PC object
//     */
//    protected void processPersistable(Object pc)
//    {
//        // TODO Consider adding more of the functionality in SCOUtils.validateObjectForWriting
//        ApiAdapter adapter = sm.getObjectManager().getApiAdapter();
//        if (!adapter.isPersistent(pc) ||
//            (adapter.isPersistent(pc) && adapter.isDeleted(pc)))
//        {
//            // Object is TRANSIENT/DETACHED and being persisted, or P_NEW_DELETED and being re-persisted
//            sm.getObjectManager().persistObjectInternal(pc, null);
//        }
//    }
//
//    /**
//     * Method to store an object field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeObjectField(int fieldNumber, Object value)
//    {
//        if (value != null)
//        {
//            AbstractPropertyMetaData fmd = sm.getClassMetaData().getManagedFieldAbsolute(fieldNumber);
//            boolean persistCascade = fmd.isCascadePersist();
//            ApiAdapter api = sm.getObjectManager().getApiAdapter();
//
//            if (replaceSCOsWithWrappers)
//            {
//                // Replace any SCO field that isnt already a wrapper, with its wrapper object
//                boolean[] secondClassMutableFieldFlags = sm.getClassMetaData().getSecondClassMutableFieldFlags();
//                if (secondClassMutableFieldFlags[fieldNumber] && !(value instanceof SCO))
//                {
//                    // Replace the field with a SCO wrapper
//                    sm.replaceSCOFieldWithWrapper(fieldNumber, value, true);
//                }
//            }
//
//            if (persistCascade)
//            {
//                if (api.isPersistable(value))
//                {
//                    // Process PC fields
//                    processPersistable(value);
//                }
//                else if (value instanceof Collection)
//                {
//                    // Process all elements of the Collection that are PC
//                    Collection coll = (Collection)value;
//                    Iterator iter = coll.iterator();
//                    while (iter.hasNext())
//                    {
//                        Object element = iter.next();
//                        if (api.isPersistable(element))
//                        {
//                            processPersistable(element);
//                        }
//                    }
//                }
//                else if (value instanceof Map)
//                {
//                    // Process all keys, values of the Map that are PC
//                    Map map = (Map)value;
//
//                    // Process any keys that are PersistenceCapable
//                    Set keys = map.keySet();
//                    Iterator iter = keys.iterator();
//                    while (iter.hasNext())
//                    {
//                        Object mapKey = iter.next();
//                        if (api.isPersistable(mapKey))
//                        {
//                            processPersistable(mapKey);
//                        }
//                    }
//
//                    // Process any values that are PersistenceCapable
//                    Collection values = map.values();
//                    iter = values.iterator();
//                    while (iter.hasNext())
//                    {
//                        Object mapValue = iter.next();
//                        if (api.isPersistable(mapValue))
//                        {
//                            processPersistable(mapValue);
//                        }
//                    }
//                }
//                else if (value instanceof Object[])
//                {
//                    Object[] array = (Object[]) value;
//
//                    for (int i=0;i<array.length;i++)
//                    {
//                        Object element = array[i];
//                        if (api.isPersistable(element))
//                        {
//                            processPersistable(element);
//                        }
//                    }
//                }
//                else
//                {
//                    // Primitive, or primitive array, or some unsupported container type
//                }
//            }
//        }
//    }
//
//    /**
//     * Method to store a boolean field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeBooleanField(int fieldNumber, boolean value)
//    {
//        // Do nothing
//    }
//
//    /**
//     * Method to store a byte field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeByteField(int fieldNumber, byte value)
//    {
//        // Do nothing
//    }
//
//    /**
//     * Method to store a char field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeCharField(int fieldNumber, char value)
//    {
//        // Do nothing
//    }
//
//    /**
//     * Method to store a double field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeDoubleField(int fieldNumber, double value)
//    {
//        // Do nothing
//    }
//
//    /**
//     * Method to store a float field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeFloatField(int fieldNumber, float value)
//    {
//        // Do nothing
//    }
//
//    /**
//     * Method to store an int field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeIntField(int fieldNumber, int value)
//    {
//        // Do nothing
//    }
//
//    /**
//     * Method to store a long field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeLongField(int fieldNumber, long value)
//    {
//        // Do nothing
//    }
//
//    /**
//     * Method to store a short field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeShortField(int fieldNumber, short value)
//    {
//        // Do nothing
//    }
//
//    /**
//     * Method to store a string field.
//     * @param fieldNumber Number of the field (absolute)
//     * @param value Value of the field
//     */
//    public void storeStringField(int fieldNumber, String value)
//    {
//        // Do nothing
//    }
}
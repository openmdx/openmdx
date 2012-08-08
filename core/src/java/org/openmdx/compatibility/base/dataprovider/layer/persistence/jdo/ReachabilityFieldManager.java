/**********************************************************************
Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
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
 * Field manager that runs reachability on all PC objects referenced from the source object.
 * Whenever a PC object is encountered "runReachability" is performed on the StateManager of the object.
 *
 * @version $Revision: 1.1 $
 */
public class ReachabilityFieldManager extends AbstractFieldManager
{
//    /** Localiser for messages. */
//    protected static final Localiser LOCALISER = Localiser.getInstance("org.jpox.Localisation");
//
//    /** StateManager for the owning object. */
//    private final StateManager sm;
//
//    /** Set of reachables up to this point. */
//    private Set reachables = null;
//
//    /**
//     * Constructor.
//     * @param sm The state manager for the object.
//     * @param reachables Reachables up to this point
//     */
//    public ReachabilityFieldManager(StateManager sm, Set reachables)
//    {
//        this.sm = sm;
//        this.reachables = reachables;
//    }
//
//    /**
//     * Utility method to process the passed persistable object.
//     * @param obj The persistable object
//     * @param fmd MetaData for the field storing this object
//     */
//    protected void processPersistable(Object obj, AbstractPropertyMetaData fmd)
//    {
//        // TODO Remove this reference to PersistenceCabable
//        ApiAdapter api = sm.getObjectManager().getApiAdapter();
//        org.jpox.StateManager sm = this.sm.getObjectManager().findStateManager(obj);
//        if (sm != null)
//        {
//            sm.runReachability(reachables);
//        }
//        else
//        {
//            if (JPOXLogger.REACHABILITY.isDebugEnabled())
//            {
//                JPOXLogger.REACHABILITY.debug(LOCALISER.msg("Reachability.Commit.FailedToFindStateManager", 
//                    api.getIdForObject(obj), fmd.getFullFieldName()));
//            }
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
//        AbstractPropertyMetaData fmd = sm.getClassMetaData().getManagedFieldAbsolute(fieldNumber);
//        if (value != null)
//        {
//            boolean persistCascade = fmd.isCascadePersist();
//            ApiAdapter api = sm.getObjectManager().getApiAdapter();
//            if (persistCascade)
//            {
//                if (api.isPersistable(value))
//                {
//                    // Process PC fields
//                    if (JPOXLogger.REACHABILITY.isDebugEnabled())
//                    {
//                        JPOXLogger.REACHABILITY.debug(LOCALISER.msg("Reachability.Commit.ProcessingPCField", 
//                            fmd.getFullFieldName()));
//                    }
//                    processPersistable(value, fmd);
//                }
//                else if (value instanceof Collection)
//                {
//                    // Process all elements of the Collection that are PC
//                    if (JPOXLogger.REACHABILITY.isDebugEnabled())
//                    {
//                        JPOXLogger.REACHABILITY.debug(LOCALISER.msg("Reachability.Commit.ProcessingSCOContainer", 
//                            fmd.getFullFieldName()));
//                    }
//                    Collection coll = (Collection)value;
//                    Iterator iter = coll.iterator();
//                    while (iter.hasNext())
//                    {
//                        Object element = iter.next();
//                        if (api.isPersistable(element))
//                        {
//                            processPersistable(element, fmd);
//                        }
//                    }
//                }
//                else if (value instanceof Map)
//                {
//                    // Process all keys, values of the Map that are PC
//                    Map map = (Map)value;
//
//                    // Process any keys that are PersistenceCapable
//                    if (JPOXLogger.REACHABILITY.isDebugEnabled())
//                    {
//                        JPOXLogger.REACHABILITY.debug(LOCALISER.msg("Reachability.Commit.ProcessingSCOContainer", 
//                            fmd.getFullFieldName()));
//                    }
//                    Set keys = map.keySet();
//                    Iterator iter = keys.iterator();
//                    while (iter.hasNext())
//                    {
//                        Object mapKey = iter.next();
//                        if (api.isPersistable(mapKey))
//                        {
//                            processPersistable(mapKey, fmd);
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
//                            processPersistable(mapValue, fmd);
//                        }
//                    }
//                }
//                else if (value instanceof Object[])
//                {
//                    // Process all array elements that are PC
//                    if (JPOXLogger.REACHABILITY.isDebugEnabled())
//                    {
//                        JPOXLogger.REACHABILITY.debug(LOCALISER.msg("Reachability.Commit.ProcessingArrayField", 
//                            fmd.getFullFieldName()));
//                    }
//                    Object[] array = (Object[]) value;
//                    for (int i=0;i<array.length;i++)
//                    {
//                        Object element = array[i];
//                        if (api.isPersistable(element))
//                        {
//                            processPersistable(element, fmd);
//                        }
//                    }
//                }
//                else
//                {
//                    // Primitive, or primitive array, or some unsupported container type
//                }
//            }
//        }
//        else
//        {
//            if (JPOXLogger.REACHABILITY.isDebugEnabled())
//            {
//                JPOXLogger.REACHABILITY.debug(LOCALISER.msg("Reachability.Commit.ProcessingNullField", 
//                    fmd.getFullFieldName()));
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
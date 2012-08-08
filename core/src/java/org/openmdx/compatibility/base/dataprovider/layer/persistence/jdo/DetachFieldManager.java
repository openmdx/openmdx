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
2005 Jorg von Frantzius - updates for fetch-depth
2007 Andy Jefferson - rewritten to process all fields detecting persistable objects at runtime
    ...
**********************************************************************/
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdo;


/**
 * FieldManager to handle the detachment of fields with persistable objects.
 *
 * @version $Revision: 1.1 $
 */
public class DetachFieldManager extends AbstractFetchFieldManager
{
//    /** Whether we should create detached copies, or detach in situ. */
//    boolean copy = true;
//
//    /**
//     * Constructor for a field manager for detachment.
//     * @param sm the StateManager of the instance being detached. An instance in Persistent or Transactional state
//     * @param secondClassMutableFields The second class mutable fields for the class of this object
//     * @param fpClass Fetch Plan for the class of this instance
//     * @param state State object to hold any pertinent controls for the detachment process
//     * @param copy Whether to create detached COPIES or just detach in situ
//     */
//    public DetachFieldManager(StateManager sm, boolean[] secondClassMutableFields, FetchPlanForClass fpClass, FetchPlanState state, 
//            boolean copy)
//    {
//        super(sm, secondClassMutableFields, fpClass, state);
//        this.copy = copy;
//    }
//
//    /**
//     * Utility method to process the passed persistable object.
//     * @param pc The PC object
//     */
//    protected Object processPersistable(Object pc)
//    {
//        if (pc == null)
//        {
//            return null;
//        }
//
//        ApiAdapter api = sm.getObjectManager().getApiAdapter();
//        if (!api.isPersistable(pc))
//        {
//            return pc;
//        }
//
//        if (!api.isDetached(pc))
//        {
//            if (api.isPersistent(pc))
//            {
//                // Persistent object that is not yet detached so detach it
//                if (copy)
//                {
//                    // Detach a copy and return the copy
//                    return sm.getObjectManager().detachObjectCopy(pc, state);
//                }
//                else
//                {
//                    // Detach the object
//                    sm.getObjectManager().detachObject(pc, state);
//                }
//            }
//        }
//        return pc;
//    }
//
//    /**
//     * Method to fetch an object field whether it is collection/map, PC, or whatever for the detachment process.
//     * @param fieldNumber Number of the field
//     * @return The object
//     */
//    protected Object internalFetchObjectField(int fieldNumber)
//    {
//        SingleValueFieldManager sfv = new SingleValueFieldManager();
//        sm.provideFields(new int[]{fieldNumber}, sfv);
//        Object value = sfv.fetchObjectField(fieldNumber);
//        ApiAdapter api = sm.getObjectManager().getApiAdapter();
//
//        if (value == null)
//        {
//            return null;
//        }
//        else
//        {
//            AbstractPropertyMetaData fmd = sm.getClassMetaData().getManagedFieldAbsolute(fieldNumber);
//            if (api.isPersistable(value))
//            {
//                // Process PC fields
//                return processPersistable(value);
//            }
//            else if (value instanceof Collection || value instanceof Map)
//            {
//                // Process all elements of Collections/Maps that are PC
//                if (!(value instanceof SCO))
//                {
//                    // Replace with SCO so we can work with it
//                    value = sm.replaceSCOFieldWithWrapper(fieldNumber, value, false);
//                }
//                SCO sco = (SCO)value;
//
//                if (copy)
//                {
//                    return sco.detachCopy(state);
//                }
//
//                if (sco instanceof Collection)
//                {
//                    // Detach all PC elements of the collection
//                    SCOUtils.detachForCollection(sm, ((Collection)sco).toArray(), state);
//                    sco.unsetOwner();
//                }
//                else if (sco instanceof Map)
//                {
//                    // Detach all PC keys/values of the map
//                    SCOUtils.detachForMap(sm, ((Map)sco).entrySet(), state);
//                    sco.unsetOwner();
//                }
//                return sco;
//            }
//            else if (value instanceof Object[])
//            {
//                // Process object array
//                if (!api.isPersistable(fmd.getType().getComponentType()))
//                {
//                    // Array element type is not persistable so just return
//                    return value;
//                }
//
//                Object[] arrValue = (Object[])value;
//                Object[] arrDetached = (Object[])Array.newInstance(fmd.getType().getComponentType(), arrValue.length);
//                for (int j=0;j<arrValue.length;j++)
//                {
//                    // Detach elements as appropriate
//                    arrDetached[j] = processPersistable(arrValue[j]);
//                }
//                return arrDetached;
//            }
//            else if (secondClassMutableFields[fieldNumber])
//            {
//                // Other SCO - what to do here ? unset owner?
//                SCO sco;
//                if (value instanceof SCO)
//                {
//                    sco = (SCO) value;
//                }
//                else
//                {
//                    // Replace with a SCO wrapper so that we can detach it
//                    sco = (SCO) sm.replaceSCOFieldWithWrapper(fieldNumber, value, false);
//                }
//                if (copy)
//                {
//                    return sco.detachCopy(state);
//                }
//
//                return sco;
//            }
//            else
//            {
//                // Primitive, primitive array, or other non-PC field
//            }
//            return value;
//        }
//    }
//
//    /**
//     * Method to throw and EndOfFetchPlanGraphException since we're at the end of a branch in the tree.
//     * @param fieldNumber Number of the field
//     * @return Object to return
//     */
//    protected Object endOfGraphOperation(int fieldNumber)
//    {
//        // check if the object here is PC and is in the detached cache anyway
//        SingleValueFieldManager sfv = new SingleValueFieldManager();
//        sm.provideFields(new int[]{fieldNumber}, sfv);
//        Object value = sfv.fetchObjectField(fieldNumber);
//        ApiAdapter api = sm.getObjectManager().getApiAdapter();
//
//        if (api.isPersistable(value))
//        {
//            Object detached = null;
//            if (copy)
//            {
//                detached = ((DetachState)state).getDetachedCopyObject(value);
//            }
//            if (detached != null)
//            {
//                // While we are at the end of a branch and this would go beyond the depth limits, 
//                // the object here *is* already detached so just return it
//                return detached;
//            }
//            if (!copy && sm.getObjectManager().getApiAdapter().isDetached(value))
//            {
//                return value;
//            }
//        }
//
//        // we reached a leaf of the object graph to detach
//        throw new EndOfFetchPlanGraphException();
//    }
}
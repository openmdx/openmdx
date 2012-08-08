/**********************************************************************
Copyright (c) 2006 Erik Bengtson and others. All rights reserved. 
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
 * Manager that nullifies any Collection/Map/PC fields of the object.
 *
 * @version $Revision: 1.1 $
 */
public class NullifyRelationFieldManager extends AbstractFieldManager
{
//    /** State Manager for the object. */
//    private final StateManager sm;
//
//    /**
//     * Constructor.
//     * @param sm the StateManager
//     */
//    public NullifyRelationFieldManager(StateManager sm)
//    {
//        this.sm = sm; 
//    }
//
//    /**
//     * Accessor for object field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public Object fetchObjectField(int fieldNumber)
//    {
//        Object value = sm.provideField(fieldNumber);
//        ApiAdapter api = sm.getObjectManager().getApiAdapter();
//        if (value == null)
//        {
//            return null;
//        }
//        else if (api.isPersistable(value))
//        {
//            // Process PC fields
//            sm.makeDirty(fieldNumber);
//            return null;
//        }
//        else if (value instanceof Collection)
//        {
//            // Process Collection fields
//            sm.makeDirty(fieldNumber);
//            ((Collection)value).clear();
//            return value;
//        }
//        else if (value instanceof Map)
//        {
//            // Process Map fields
//            sm.makeDirty(fieldNumber);
//            ((Map)value).clear();
//            return value;
//        }
//        else if (value.getClass().isArray() && Object.class.isAssignableFrom(value.getClass().getComponentType()))
//        {
//            // Process object array fields
//            // TODO Check if the array element is PC and nullify
//        }
//
//        //do not need to nullify fields that are not references and resides embedded in this object
//        return value;
//    }
//
//    /**
//     * Accessor for boolean field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public boolean fetchBooleanField(int fieldNumber)
//    {
//        return true;
//    }
//
//    /**
//     * Accessor for char field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public char fetchCharField(int fieldNumber)
//    {
//        return '0';
//    }
//
//    /**
//     * Accessor for byte field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public byte fetchByteField(int fieldNumber)
//    {
//        return (byte)0;
//    }
//
//    /**
//     * Accessor for double field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public double fetchDoubleField(int fieldNumber)
//    {
//        return 0;
//    }
//
//    /**
//     * Accessor for float field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public float fetchFloatField(int fieldNumber)
//    {
//        return 0;
//    }
//
//    /**
//     * Accessor for int field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public int fetchIntField(int fieldNumber)
//    {
//        return 0;
//    }
//
//    /**
//     * Accessor for long field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public long fetchLongField(int fieldNumber)
//    {
//        return 0;
//    }
//
//    /**
//     * Accessor for short field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public short fetchShortField(int fieldNumber)
//    {
//        return 0;
//    }
//
//    /**
//     * Accessor for String field.
//     * @param fieldNumber Number of field 
//     * @return Object value
//     */
//    public String fetchStringField(int fieldNumber)
//    {
//        return "";
//    }
}
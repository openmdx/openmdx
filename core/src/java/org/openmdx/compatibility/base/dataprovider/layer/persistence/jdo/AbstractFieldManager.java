/**********************************************************************
Copyright (c) 2002 Mike Martin (TJDO) and others. All rights reserved.
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
    Andy Jefferson - coding standards
    ...
**********************************************************************/
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdo;


/**
 * Abstract representation of a field manager.
 *
 * @version $Revision: 1.1 $
 **/
public abstract class AbstractFieldManager implements FieldManager
{
    /**
     * Default constructor
     */
    public AbstractFieldManager()
    {
        //default constructor
    }

    private String failureMessage(String method)
    {
        return "Somehow " + getClass().getName() + "." + method + "() was called, which should have been impossible";
    }

    public void storeBooleanField(int fieldNumber, boolean value)
    {
        throw new JPOXException(failureMessage("storeBooleanField")).setFatal();
    }

    public boolean fetchBooleanField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchBooleanField")).setFatal();
    }

    public void storeCharField(int fieldNumber, char value)
    {
        throw new JPOXException(failureMessage("storeCharField")).setFatal();
    }

    public char fetchCharField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchCharField")).setFatal();
    }

    public void storeByteField(int fieldNumber, byte value)
    {
        throw new JPOXException(failureMessage("storeByteField")).setFatal();
    }

    public byte fetchByteField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchByteField")).setFatal();
    }

    public void storeShortField(int fieldNumber, short value)
    {
        throw new JPOXException(failureMessage("storeShortField")).setFatal();
    }

    public short fetchShortField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchShortField")).setFatal();
    }

    public void storeIntField(int fieldNumber, int value)
    {
        throw new JPOXException(failureMessage("storeIntField")).setFatal();
    }

    public int fetchIntField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchIntField")).setFatal();
    }

    public void storeLongField(int fieldNumber, long value)
    {
        throw new JPOXException(failureMessage("storeLongField")).setFatal();
    }

    public long fetchLongField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchLongField")).setFatal();
    }

    public void storeFloatField(int fieldNumber, float value)
    {
        throw new JPOXException(failureMessage("storeFloatField")).setFatal();
    }

    public float fetchFloatField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchFloatField")).setFatal();
    }

    public void storeDoubleField(int fieldNumber, double value)
    {
        throw new JPOXException(failureMessage("storeDoubleField")).setFatal();
    }

    public double fetchDoubleField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchDoubleField")).setFatal();
    }

    public void storeStringField(int fieldNumber, String value)
    {
        throw new JPOXException(failureMessage("storeStringField")).setFatal();
    }

    public String fetchStringField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchStringField")).setFatal();
    }

    public void storeObjectField(int fieldNumber, Object value)
    {
        throw new JPOXException(failureMessage("storeObjectField")).setFatal();
    }

    public Object fetchObjectField(int fieldNumber)
    {
        throw new JPOXException(failureMessage("fetchObjectField")).setFatal();
    }
}

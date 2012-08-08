/*
 * Copyright 2005 OpenXRI Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.openxri;


/*
********************************************************************************
* Class: XRIParseException
********************************************************************************
*/ /**
* This class is used to indicate a parsing failure of an XRI syntax element.
* @author =chetan
*/
public class XRIParseException
    extends java.lang.RuntimeException
{
    private Exception moEx = null;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *Constructs a XRIParseException with a default message
    */
    public XRIParseException()
    {
        super("Invalid XRI");

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *Constructs a XRIParseException with the provided message
    */
    public XRIParseException(String sMsg)
    {
        super(sMsg);

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *Constructs a XRIParseException with the provided message and
    *based off of the provided Exception
    */
    public XRIParseException(String sMsg, Exception oEx)
    {
        super(sMsg);
        moEx = oEx;

    } // Constructor()

    /*
    ****************************************************************************
    * dump()
    ****************************************************************************
    */ /**
    * Prints the stack trace for the exception and the exception it is based
    * upon to standard out.
    */
    public void dump()
    {
        if (moEx != null)
        {
            moEx.printStackTrace();
        }

        printStackTrace();

    } // dump()

    /*
    ****************************************************************************
    * toString()
    ****************************************************************************
    */ /**
    * Provides String representation of the exception
    */
    public String toString()
    {
        return super.toString();

    } // toString()

} // Class: XRIParseException

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
* Class: ParseStream
********************************************************************************
*/ /**
* This class is a utility class for parsing a String into Parsable objects
*
* @author =chetan
*/
public class ParseStream
{
    String msData = null;
    int mnConsumed = 0;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs and input stream from a String
    */
    ParseStream(String sString)
    {
        msData = sString;

    } // Constructor()

    /*
    ****************************************************************************
    * begin()
    ****************************************************************************
    */ /**
    * Constructs a new ParseStream object for consuming data based on the
    * current object
    */
    ParseStream begin()
    {
        return new ParseStream(msData);

    } // begin()

    /*
    ****************************************************************************
    * end()
    ****************************************************************************
    */ /**
    * Consumes all or part of the current string based on the passed in
    * stream.  The passed in stream MUST have been created as a result of
    * a call to begin()
    * @param oRef The stream containing the data that has yet to be consumed
    */
    void end(ParseStream oRef)
    {
        msData = oRef.msData;
        mnConsumed += oRef.mnConsumed;

    } // end()

    /*
    ****************************************************************************
    * getConsumed()
    ****************************************************************************
    */ /**
    * Returns the String that has been consumed by the passed in stream.  The
    * passed in stream MUST have been created as a result of a call to begin()
    * @param oRef The stream containing the data that has yet to be consumed
    * @return String The string that was consumed by the passed in stream.
    */
    String getConsumed(ParseStream oRef)
    {
        return (oRef.mnConsumed > 0) ? msData.substring(0, oRef.mnConsumed) : null;

    } // getConsumed()

    /*
    ****************************************************************************
    * empty()
    ****************************************************************************
    */ /**
    * Returns whether or not the stream is empty
    * @return boolean Returns true if stream is empty
    */
    boolean empty()
    {
        return msData.length() == 0;

    } // empty()

    /*
    ****************************************************************************
    * consume()
    ****************************************************************************
    */ /**
    * Consumes a given number of characters
    * @param nSize The amount of characters to consume
    */
    void consume(int nSize)
    {
        if (nSize > 0)
        {
            mnConsumed += nSize;
            msData = msData.substring(nSize);
        }

    } // consume()

    /*
    ****************************************************************************
    * toString()
    ****************************************************************************
    */ /**
    * Returns the characters yet to be consumed
    *
    * @return String The String representation of the characters yet to be
    *         consumed
    */
    public String toString()
    {
        return msData;

    } // toString()

    /*
    ****************************************************************************
    * getData()
    ****************************************************************************
    */ /**
    * Returns the characters yet to be consumed
    *
    * @return String The String representation of the characters yet to be
    *         consumed
    */
    String getData()
    {
        return msData;

    } // getData()

} // Class: ParseStream

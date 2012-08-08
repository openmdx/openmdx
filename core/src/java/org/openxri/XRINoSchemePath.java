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
* Class: XRINoSchemePath
********************************************************************************
*/ /**
* This class provides a strong typing for a XRINoSchemePath.  Any
* object of this class that appears outside of the package is a valid
* XRINoSchemePath.
*
* @author =chetan
*/
public class XRINoSchemePath
    extends XRIPath
{
    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs RelativePath from a String
    */
    public XRINoSchemePath(String sXRI)
    {
        super(sXRI);
        mbAllowColon = false;
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *  Protected Constructor used by package only
    */
    XRINoSchemePath()
    {
        mbAllowColon = false;

    } // Constructor()

    /*
    ****************************************************************************
    * doScan()
    ****************************************************************************
    */ /**
    * Parses the input stream into the object
    * @param oStream The input stream to scan from
    * @return  boolean True if part of the Stream was consumed into the object
    */
    boolean doScan(ParseStream oStream)
    {
        // NOTE: A RelativePath can be empty
        if (oStream.empty())
        {
            return true;
        }

        // doesn't matter if this works or not, will consume what it needs to
        scanXRISegments(oStream);

        return true;

    } // doScan()

} // Class: XRINoSchemePath

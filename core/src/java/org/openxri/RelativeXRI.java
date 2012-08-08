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
* Class: RelativeXRI
********************************************************************************
*/ /**
* This class provides a strong typing for a Relative XRI.  Any
* object of this class that appears outside of the package is a valid
* Relative XRI.
*
* @author =chetan
*/
public class RelativeXRI
    extends Parsable
    implements XRIReference
{
    private XRIPath moXRIPath;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Protected Constructor used by package only
    */
    RelativeXRI() {} // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs XRI from String
    */
    public RelativeXRI(String sXRI)
    {
        super(sXRI);
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs an XRI from the provided RelativePath
    */
    public RelativeXRI(XRIPath oPath)
    {
        moXRIPath = oPath;
        setParsedValue(oPath.toString());

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
        moXRIPath = scanXRIPath(oStream);
        return true;

    } // doScan()

    /*
    ****************************************************************************
    * scanXRIPath()
    ****************************************************************************
    */ /**
    *
    */
    static XRIPath scanXRIPath(ParseStream oStream)
    {
        // check for a local path regardless of scanAuthority outcome
        XRIAbsolutePath oPath = new XRIAbsolutePath();
        if (oPath.scan(oStream))
        {
            return oPath;
        }
        else
        {
            XRINoSchemePath oRelativePath = new XRINoSchemePath();
            if (oRelativePath.scan(oStream))
            {
                return oRelativePath;
            }
        }

        return null;

    } // scanXRIPath()

    /*
    ****************************************************************************
    * getAuthorityPath()
    ****************************************************************************
    */ /**
    *
    */
    public AuthorityPath getAuthorityPath()
    {
        return null;

    } // getAuthorityPath()

    /*
    ****************************************************************************
    * getXRIPath()
    ****************************************************************************
    */ /**
    *
    */
    public XRIPath getXRIPath()
    {
        return moXRIPath;

    } // getXRIPath()

} // Class: RelativeXRI

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
* Class: AuthorityPath
********************************************************************************
*/ /**
* This class provides a base class for all types of AuthorityPath elements
*
* @author =chetan
*/
public abstract class AuthorityPath
    extends Parsable
{
    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Protected Constructor used by package only
    */
    AuthorityPath()
    {
        super();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs AuthorityPath from a String
    */
    AuthorityPath(String sPath)
    {
        super(sPath);

    } // Constructor()

    /*
    ****************************************************************************
    * buildAuthorityPath()
    ****************************************************************************
    */ /**
    * Static method to build the correct AuthorityPath object from a String
    */
    public static AuthorityPath buildAuthorityPath(String sPath)
    {
        ParseStream oStream = new ParseStream(sPath);
        AuthorityPath oPath = scanAuthority(oStream);

        // only return the path if the entire stream was consumed
        return (oStream.getData().length() == 0) ? oPath : null;

    } // buildAuthorityPath()

    /*
    ****************************************************************************
    * scanAuthority()
    ****************************************************************************
    */ /**
    *
    */
    static AuthorityPath scanAuthority(ParseStream oParseStream)
    {
        GCSAuthority oGCSAuthority = new GCSAuthority();
        if (oGCSAuthority.scan(oParseStream))
        {
            return oGCSAuthority;
        }

        XRefAuthority oXRefAuthority = new XRefAuthority();
        if (oXRefAuthority.scan(oParseStream))
        {
            return oXRefAuthority;
        }

        IRIAuthority oIRIAuthority = new IRIAuthority();
        if (oIRIAuthority.scan(oParseStream))
        {
            return oIRIAuthority;
        }

        return null;

    } // scanAuthority()

    /**
     *  Serializes the authority into IRI-normal form
     */
    public abstract String toIRINormalForm();

    /**
     *  Serializes the authority into URI-normal form
     */
    public abstract String toURINormalForm();

}

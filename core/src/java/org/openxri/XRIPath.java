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

import java.util.Iterator;
import java.util.Vector;


/*
********************************************************************************
* Class: XRIPath
********************************************************************************
*/ /**
* This base class provides a strong typing for a XRI Path.  Any
* object of this class that appears outside of the package is a valid
* XRI Path.
*
* @author =chetan
*/
public abstract class XRIPath
    extends Parsable
{
    Vector moSegments = new Vector();
    boolean mbAllowColon = true;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *
    */
    XRIPath() {
     // Constructor()
    } 

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *
    */
    XRIPath(String sVal)
    {
        super(sVal);

    } // Constructor()

    /*
    ****************************************************************************
    * getNumSegments()
    ****************************************************************************
    */ /**
    *  returns The number of XRISegmentVals for this relative path
    * @return int The number of XRISegmentVals for this relative path
    */
    public int getNumSegments()
    {
        parse();
        return (moSegments == null) ? 0 : moSegments.size();

    } // getNumSegments()

    /*
    ****************************************************************************
    * getSegmentIterator()
    ****************************************************************************
    */ /**
    *  returns an Iterator for the XRISegmentVals for this relative path
    * @return Iterator Iterator for the XRISegmentVals for this relative path
    */
    public Iterator getSegmentIterator()
    {
        parse();
        return (moSegments == null) ? null : moSegments.iterator();

    }

    /*
    ****************************************************************************
    * getSegmentAt()
    ****************************************************************************
    */ /**
    *Returns the XRISegmentVal at the given index
    * @param nIndex The index of the XRISegmentVal to return
    * @return XRISegmentVal The XRISegmentVal at the specified index
    */
    public XRISegment getSegmentAt(int nIndex)
    {
        parse();
        if ((moSegments == null) || (nIndex >= moSegments.size()))
        {
            return null;
        }

        return (XRISegment) moSegments.elementAt(nIndex);

    } // getSegmentAt()

    /**
     * Parses the input stream into XRISegmentVals
     * @param oStream The input stream to scan from
     */
    void scanXRISegments(ParseStream oPathStream)
    {
        // sets whether colons are allowed
        boolean bAllowColon = mbAllowColon;

        // loop through the XRI segments as long as we are consuming something
        boolean bConsumed = true;
        while (!oPathStream.empty() && bConsumed)
        {
            bConsumed = false;
            ParseStream oStream = oPathStream.begin();
            boolean bStartsWithSlash = (oStream.getData().charAt(0) == '/');

            // if this is the first segment, it must not start with slash
            if ((bStartsWithSlash) && (moSegments.size() == 0))
            {
                break;
            }

            // if this is not the first segment, we expect a slash
            if ((!bStartsWithSlash) && (moSegments.size() > 0))
            {
                break;
            }

            // consume the slash if necessary
            if (bStartsWithSlash)
            {
                bConsumed = true;
                oStream.consume(1);
            }

            // if there is actually a segment, add it to the list
            XRISegment oSegment = new XRISegment(true, bAllowColon, true);
            if (oSegment.scan(oStream))
            {
                bConsumed = true;
                moSegments.add(oSegment);
            }

            // consume whatever we used (even if the segment was empty)
            oPathStream.end(oStream);

            // after the first segment, colons are allowed
            bAllowColon = true;
        }

    } // scanXRISegments()


    /**
     * Serialzes Relative Path into IRI normal from
     * @return The IRI normal form of the Relative Path
     */
    public String toIRINormalForm()
    {
        StringBuffer sValue = new StringBuffer();

        Iterator oIt = moSegments.iterator();
        if (oIt.hasNext())
        {
            sValue.append(((XRISegment) oIt.next()).toIRINormalForm());
        }

        while (oIt.hasNext())
        {
            sValue.append("/");
            sValue.append((((XRISegment) oIt.next()).toIRINormalForm()));
        }

        return sValue.toString();

    }


    /**
     * Serialzes Relative Path into URI normal from
     * @return The URI normal form of the Relative Path
     */
    public String toURINormalForm()
    {
    	return IRIUtils.IRItoURI(toIRINormalForm());
    }

}

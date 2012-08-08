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
* Class: XRIAbsolutePath
********************************************************************************
*/ /**
* This class provides a strong typing for a XRI Absolute Path.  Any
* object of this class that appears outside of the package is a valid
* XRI Absolute Path.
*
* @author =chetan
*/
public class XRIAbsolutePath
    extends XRIPath
{
    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs LocalPath from a String
    */
    public XRIAbsolutePath(String sPath)
    {
        super(sPath);
        mbAllowColon = true;
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *  Protected Constructor used by package only
    */
    XRIAbsolutePath()
    {
        mbAllowColon = true;

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *
    */
    XRIAbsolutePath(XRINoSchemePath oPath)
    {
        moSegments = oPath.moSegments;
        mbAllowColon = true;

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
        // make sure we start with a slash
        if (oStream.empty() || (oStream.getData().charAt(0) != '/'))
        {
            return false;
        }

        // consume the slash
        oStream.consume(1);

        // now scan the XRI segments as we are supposed to
        super.scanXRISegments(oStream);

        // return true no matter what, we got the slash
        return true;

    } // doScan()

    
    /**
     * Serialzes Local Path into IRI normal from
     * @return The IRI normal form of the Local Path
     */
    public String toIRINormalForm()
    {
        return "/" + super.toIRINormalForm();
    }
    
    /**
     * Tests to see if this absolute path is a subsegment-wise prefix of
     * the given <code>path</code>.
     * @param path
     * @return true if <code>path</code> is prefixed with or exactly the same as this XRIAbsolutePath object.
     */
    public boolean isPrefixOf(XRIAbsolutePath path)
    {
    	int n = this.getNumSegments();
    	// first, if we have more segments than the given path, return false
    	if (n > path.getNumSegments())
    		return false;
    	
    	// compare segment by segment
    	for (int i = 0; i < n; i++) {
    		XRISegment seg = getSegmentAt(i);
    		if (i == n - 1) {
    			// final segment, can be a suffix
    			if (!seg.isPrefixOf(path.getSegmentAt(i)))
    				return false;
    		}
    		else if (!seg.equalsIgnoreCase(path.getSegmentAt(i)))
    			// not final segment, must be equal
    			return false;
    	}
    	return true;
    }
}

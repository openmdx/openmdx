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
* Class: XRefAuthority
********************************************************************************
*/ /**
* This class provides a strong typing for a XRef Authority.  Any
* object of this class that appears outside of the package is a valid
* XRef Authority.
*
* @author =chetan
*/
public class XRefAuthority
    extends XRIAuthority
{
    XRef moXRoot;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs a cross-reference authority from a string
    */
    public XRefAuthority(String sPath)
    {
        super(sPath);
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *
    */
    XRefAuthority() {
       // Constructor()  
    } 

    /*
    ****************************************************************************
    * doScan()
    ****************************************************************************
    */ /**
    *
    */
    boolean doScan(ParseStream oStream)
    {
        if (oStream.empty())
        {
            return false;
        }

        ParseStream oTempStream = oStream.begin();

        // make sure we have a valid XRI Value
        XRef oXRef = new XRef();
        if (!oXRef.scan(oTempStream))
        {
            return false;
        }

        // at this point, we know we have enough for a valid xref
        oStream.end(oTempStream);
        moXRoot = oXRef;

        // the cross-reference MAY be followed by an XRI Segment
        // where the star cannot be assumed
        XRISegment oSegment = new XRISegment(false, true, true);
        if (oSegment.scan(oStream))
        {
            moSegment = oSegment;
        }

        return true;

    } // doScan()

    /*
    ****************************************************************************
    * getXRoot()
    ****************************************************************************
    */ /**
    *Returns the Cross Reference Root
    * @return  XRef The Cross Reference Root Authority
    */
    public XRef getXRoot()
    {
        parse();
        return moXRoot;

    }

    
    /**
     * Serialzes the XRIAuthority into IRI normal from
     * @return The IRI normal form of the XRIAuthority
     */
    public String toIRINormalForm()
    {
        String sValue = getXRoot().toIRINormalForm();
        if (moSegment != null)
        {
            sValue += moSegment.toIRINormalForm();
        }

        return sValue;
    }

    
    /**
     * Serialzes the XRefAuthority into URI normal from
     * @return The URI normal form of the XRefAuthority
     */
    public String toURINormalForm()
    {
    	return IRIUtils.IRItoURI(toIRINormalForm());
    }

    /*
    ****************************************************************************
    * getRootAuthority()
    ****************************************************************************
    */ /**
    *Returns the root XRI Authority as a String
    * @return  String The Root XRI Authority
    */
    public String getRootAuthority()
    {
        return getXRoot().toString();

    } // getRootAuthority()

    /*
    ****************************************************************************
    * getParent()
    ****************************************************************************
    */ /**
    *Returns the parent XRIAuthority for this object.  Equivalent to all but
    *the last SubSegment.
    * @return XRIAuthority The parent XRIAuthority of this object
    */
    public XRIAuthority getParent()
    {
        parse();

        // return null if there is no XRISegment
        if (this.moSegment == null)
        {
            return null;
        }

        // otherwise, we are good to go
        XRefAuthority oParent = new XRefAuthority();
        oParent.moXRoot = this.moXRoot;
        oParent.moSegment = this.moSegment.getParent();
        oParent.msValue = moXRoot.toString() + oParent.moSegment.toString();
        oParent.mbParsed = true;
        oParent.mbParseResult = this.mbParseResult;

        return oParent;

    } // getParent()

} // Class: XRefAuthority

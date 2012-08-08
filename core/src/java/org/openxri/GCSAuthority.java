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
* Class: GCSAuthority
********************************************************************************
*/ /**
* This class provides a strong typing for a GCS Authority.  Any
* object of this class that appears outside of the package is a valid
* GCS Authority.
*
* @author =chetan
*/
public class GCSAuthority
    extends XRIAuthority
{
    private String msGCSRoot;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs GCSAuthority from a String
    */
    public GCSAuthority(String sPath)
    {
        super(sPath);
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Protected Constructor used by package only
    */
    GCSAuthority()
    {
        super();

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
        if (!scanGCSChar(oStream))
        {
            return false;
        }

        // read in a segment, but bail if it isn't persistent in the ! namespace
        XRISegment oSegment =
            new XRISegment(true, true, !msGCSRoot.equals("!"));
        if (oSegment.scan(oStream))
        {
            moSegment = oSegment;
        }

        return true;

    } // doScan()

    /*
    ****************************************************************************
    * scanGCSChar()
    ****************************************************************************
    */ /**
    * Parses the input stream into the GCS Character String
    * @param oParseStream The input stream to scan from
    * @return  boolean True if part of the Stream was consumed
    */
    private boolean scanGCSChar(ParseStream oParseStream)
    {
        if (oParseStream.empty())
        {
            return false;
        }

        switch (oParseStream.getData().charAt(0))
        {
            case '+':
            case '=':
            case '@':
            case '$':
            case '!':
            {
                // this way provides a clean copy, whereas substring does not                
                msGCSRoot = Character.toString(oParseStream.getData().charAt(0));
                oParseStream.consume(1);
                return true;
            }
        }

        return false;

    } // scanGCSChar()

    /*
    ****************************************************************************
    * getGCSRootAsChar()
    ****************************************************************************
    */ /**
    *Returns the GCS root
    * @return  char The GCS Root Authority
    */
    public char getGCSRootAsChar()
    {
        parse();
        return msGCSRoot.charAt(0);

    } // getGCSRootAsChar()

    /*
    ****************************************************************************
    * getGCSRoot()
    ****************************************************************************
    */ /**
    *Returns the GCS root
    * @return  String The GCS Root Authority
    */
    public String getGCSRoot()
    {
        parse();
        return msGCSRoot;

    } // getGCSRoot()

    
    /**
     * Serialzes the XRIAuthority into IRI-normal from
     * @return The IRI normal form of the XRIAuthority
     */
    public String toIRINormalForm()
    {
        String sValue = msGCSRoot;
        if (moSegment != null) {
            sValue += moSegment.toIRINormalForm();
        }
        return sValue;
    }

    
    /**
     * Serialzes the XRIAuthority into URI normal from
     * @return The URI normal form of the XRIAuthority
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
        return getGCSRoot();

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
        GCSAuthority oParent = new GCSAuthority();
        oParent.msGCSRoot = this.msGCSRoot;
        oParent.moSegment = this.moSegment.getParent();
        oParent.msValue = msGCSRoot + (oParent.moSegment != null ? oParent.moSegment.toString() : "");
        oParent.mbParsed = true;
        oParent.mbParseResult = this.mbParseResult;

        return oParent;

    } // getParent()

} // Class: GCSAuthority

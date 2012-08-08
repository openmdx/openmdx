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

import com.ibm.icu.text.UTF16;


/*
********************************************************************************
* Class: SubSegment
********************************************************************************
*/ /**
* This class provides a strong typing for a XRI subsegment.  Any
* object of this class that appears outside of the package is a valid
* subsegment.
*
* @author =chetan
*/
public class XRISubSegment
    extends Parsable
{
    boolean mbPersistant = false;
    XRef moXRef = null;
    boolean mbAllowColon = false;
    boolean mbAllowImpliedDelimiter = false;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Protected Constructor used by package only
    */
    XRISubSegment(boolean bAllowImpliedDelimiter, boolean bAllowColon)
    {
        mbAllowImpliedDelimiter = bAllowImpliedDelimiter;
        mbAllowColon = bAllowColon;

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs SubSegment from String
    */
    public XRISubSegment(String sXRI)
    {
        super(sXRI);
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs SubSegment from String
    */
    public XRISubSegment(String sXRI, boolean bAllowColon)
    {
        super(sXRI);
        mbAllowColon = bAllowColon;
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * isPersistant()
    ****************************************************************************
    */ /**
    * Indicates whether Subsegment is persistent or not
    * @return boolean Indicates whether Subsegment is persistent or not
    */
    public boolean isPersistant()
    {
        return mbPersistant;

    } // isPersistant()

    /*
    ****************************************************************************
    * getXRef()
    ****************************************************************************
    */ /**
    * Returns the cross-reference of this object
    * @return XRef the cross-reference of this object
    */
    public XRef getXRef()
    {
        parse();
        return moXRef;

    } // getXRef()

    /*
    ****************************************************************************
    * toString()
    ****************************************************************************
    */ /**
    * String representation of the object.
    * @return String the String form of the SubSegment with its persistent indicator.
    */
    public String toString()
    {
    	return toString(true);
    }

    
    public String toString(boolean wantOptionalDelim)
    {
        parse();

        // add the dot if necessary
        String sRetval = super.toString();
        if ((sRetval.length() > 0) &&
            (sRetval.charAt(0) != XRI.RDELIM) &&
            (sRetval.charAt(0) != XRI.PDELIM) &&
            wantOptionalDelim)
        {
            sRetval = XRI.RDELIM_S + sRetval;
        }

        return sRetval;

    }
    
    
    public boolean equals(XRISubSegment subseg)
    {
    	return toString(true).equals(subseg.toString(true));
    }

    
    public boolean equalsIgnoreCase(XRISubSegment subseg)
    {
    	return toString(true).equalsIgnoreCase(subseg.toString(true));
    }


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
        if (oStream.getData().charAt(0) == XRI.PDELIM)
        {
            this.mbPersistant = true;
            oStream.consume(1);
        }
        else if (oStream.getData().charAt(0) == XRI.RDELIM)
        {
            oStream.consume(1);
        }
        else if (!mbAllowImpliedDelimiter)
        {
            return false;
        }

        // if there is a cross-reference, it has priority in scanning
        XRef oXRef = new XRef();
        if (oXRef.scan(oStream))
        {
            moXRef = oXRef;
            return true;
        }

        // read the characters, it is ok if they are empty
        int n = scanPChars(oStream.getData());
        oStream.consume(n);

        return true;

    } // doScan()

    /*
    ****************************************************************************
    * scanPChars()
    ****************************************************************************
    */ /**
    * Reads xri-pchars from the String
    * @param s The String to scan from
    * @return int The number of characters read in
    */
    private int scanPChars(String s)
    {
		int c;
		for (int i = 0; i < s.length(); i += UTF16.getCharCount(c)) {
			c = UTF16.charAt(s, i);

			if (Characters.isPChar(c))
            {
				// pchar includes colon, but our configuration might not allow it
				if (c != ':')
					continue;
				
				// it's a colon, only let through if our configuration allows it
				if (mbAllowColon)
					continue;
            }

            //
            // escaped
            //
            if (Characters.isEscaped(c, s, i))
            {
                i += 2;
                continue;
            }

            return i;
        }

        return s.length();
    }

    
    /**
     * Serialzes SubSegment into IRI normal from
     * @return The IRI normal form of the SubSegment
     */
    public String toIRINormalForm()
    {
    	return toIRINormalForm(true);
    }


    public String toIRINormalForm(boolean wantOptionalDelim)
    {
        if (moXRef != null) {
        	String sValue;
        	if (isPersistant())
        		sValue = XRI.PDELIM_S;
        	else
        		sValue = wantOptionalDelim? XRI.RDELIM_S : "";
            return sValue + moXRef.toIRINormalForm();
        }

        return IRIUtils.XRItoIRI(toString(wantOptionalDelim), false);

    }

    
    /**
     * Serialzes SubSegment into URI normal from
     * @return The URI normal form of the SubSegment
     */
    public String toURINormalForm()
    {
    	return toURINormalForm(true);
    }


    public String toURINormalForm(boolean wantOptionalDelim)
    {
    	return IRIUtils.IRItoURI(toIRINormalForm(wantOptionalDelim));
    }
}

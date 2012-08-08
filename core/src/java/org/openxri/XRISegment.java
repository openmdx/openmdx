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
* Class: XRISegment
********************************************************************************
*/ /**
* This class provides a strong typing for a XRI Segment.  Any
* object of this class that appears outside of the package is a valid
* XRI Segment with at least one subsegment.
*
* @author =chetan
*/
@SuppressWarnings("unchecked")
public class XRISegment
    extends Parsable
{
    private Vector moSubSegments = null;
    private boolean mbAllowImpliedDelimiter = true;
    private boolean mbAllowColon = true;
    private boolean mbAllowReassignable = true;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Protected Constructor used by package only
    */
    XRISegment(
        boolean bAllowImpliedDelimiter, boolean bAllowColon,
        boolean bAllowReassignable)
    {
        mbAllowImpliedDelimiter = bAllowImpliedDelimiter;
        mbAllowColon = bAllowColon;
        mbAllowReassignable = bAllowReassignable;

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs XRISegment from String
    */
    public XRISegment(String sVal)
    {
        super(sVal);
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs XRISegment from String
    */
    public XRISegment(
        String sVal, boolean bAllowImpliedDelimiter, boolean bAllowColon)
    {
        super(sVal);
        mbAllowImpliedDelimiter = bAllowImpliedDelimiter;
        mbAllowColon = bAllowColon;
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * getNumSubSegments()
    ****************************************************************************
    */ /**
    *Returns the number of subsegments in the XRI segment
    * @return int number of subsegments
    */
    public int getNumSubSegments()
    {
        parse();
        return (moSubSegments == null) ? 0 : moSubSegments.size();

    } // getNumSubSegments()

    /*
    ****************************************************************************
    * getSubSegmentIterator()
    ****************************************************************************
    */ /**
    *provides an Iterator for the subsegments
    * @return Iterator provides an Iterator for the subsegments
    */
    public Iterator getSubSegmentIterator()
    {
        parse();
        return (moSubSegments == null) ? null : moSubSegments.iterator();

    } // getSubSegmentIterator()

    /*
    ****************************************************************************
    * getSubSegmentAt()
    ****************************************************************************
    */ /**
    *Returns the subsegment at the given index
    * @param nIndex The index of the subsegment to return
    * @return SubSegment The subsegment at the specified location
    */
    public XRISubSegment getSubSegmentAt(int nIndex)
    {
        parse();
        if ((moSubSegments == null) || (nIndex >= moSubSegments.size()))
        {
            return null;
        }

        return (XRISubSegment) moSubSegments.elementAt(nIndex);

    } // getSubSegmentAt()

    /*
    ****************************************************************************
    * getParent()
    ****************************************************************************
    */ /**
    *Returns the parent XRISegment for this object.  Equivalent to all but
    *the last SubSegment.
    * @return XRISegment The parent XRISegment of this object
    */
    public XRISegment getParent()
    {
        parse();

        // return null if there isn't atleast 2 subsegments
        if ((this.moSubSegments == null) || (this.moSubSegments.size() <= 1))
        {
            return null;
        }

        // return a XRISegment with the first n-1 subsegments
        XRISegment oParent =
            new XRISegment(
                mbAllowImpliedDelimiter, mbAllowColon, mbAllowReassignable);
        oParent.msValue = "";
        oParent.moSubSegments = new Vector();
        oParent.mbParsed = true;
        oParent.mbParseResult = this.mbParseResult;
        for (int i = 0; i < (this.moSubSegments.size() - 1); i++)
        {
            XRISubSegment oSubseg = (XRISubSegment) this.moSubSegments.elementAt(i);
            oParent.moSubSegments.add(oSubseg);
            oParent.msValue += oSubseg.toString();
        }

        return oParent;

    } // getParent()

    /*
    ****************************************************************************
    * getRemainder()
    ****************************************************************************
    */ /**
    *Returns the last part of the XRI segment.  Skips over the specified number
    *of subsegments and returns the remainder as a XRISegment.
    *For example: This XRI Segment is "!a!b!c!d"
    *             getSegmentRemaider(0) => !a!b!c!d
    *             getSegmentRemaider(1) => !b!c!d
    *             getSegmentRemaider(2) => !c!d
    *             getSegmentRemaider(3) => !d
    *             getSegmentRemaider(4) => null
    *@param nSkip The number of subsegments to skip.
    */
    public XRISegment getRemainder(int nSkip)
    {
        parse();

        // return null if there isn't atleast nSkip subsegments
        if (
            (this.moSubSegments == null) ||
            (this.moSubSegments.size() <= nSkip))
        {
            return null;
        }

        // return a XRISegment without the first few subsegments
        XRISegment oRemainder =
            new XRISegment(false, mbAllowColon, mbAllowReassignable);
        oRemainder.msValue = "";
        oRemainder.moSubSegments = new Vector();
        oRemainder.mbParsed = true;
        oRemainder.mbParseResult = this.mbParseResult;
        for (int i = nSkip; i < this.moSubSegments.size(); i++)
        {
            XRISubSegment oSubseg = (XRISubSegment) this.moSubSegments.elementAt(i);
            oRemainder.moSubSegments.add(oSubseg);
            oRemainder.msValue += oSubseg.toString();
        }

        return oRemainder;

    } // getRemainder()

    /*
    ****************************************************************************
    * toString()
    ****************************************************************************
    */ /**
    * String representation of the object.
    * @return String the String form of the XRI with its persistent indicator.
    */
    public String toString()
    {
        parse();

        StringBuffer sRetval = new StringBuffer();
        for (int i = 0; i < moSubSegments.size(); i++)
        {
            sRetval.append(
                ((XRISubSegment) moSubSegments.elementAt(i)).toString(i > 0));	// don't output the star at the beginning; hope that's correct; =peacekeeper
        }

        return sRetval.toString();

    } // toString()

    /*
    ****************************************************************************
    * doScan()
    ****************************************************************************
    */ /**
    * Parses the input stream into the object
    * @param oStream The input stream to scan from
    * @return  boolean True if part of the Stream was consumed into the object
    */
    boolean doScan(ParseStream oXRISegStream)
    {
        moSubSegments = new Vector();
        boolean bAllowImpliedDelimiter = mbAllowImpliedDelimiter;
        boolean bAllowReassignable = mbAllowReassignable;

        // loop through the stream, but don't consume the real string unless
        // we are successful
        while (!oXRISegStream.empty())
        {
            // determine if we have a delimiter for the next subsegment
            char c = oXRISegStream.getData().charAt(0);

            // break out if the first character has to be persistent and isn't
            if ((!bAllowReassignable) && (c != XRI.PDELIM))
            {
                break;
            }

            // check if we have a valid non-null subsegment
            XRISubSegment oSubSegment =
                new XRISubSegment(bAllowImpliedDelimiter, mbAllowColon);
            if (oSubSegment.scan(oXRISegStream))
            {
                // if we had a valid sub-segment, consume it and add it to the list
                moSubSegments.add(oSubSegment);
            }
            else
            {
                break;
            }

            bAllowImpliedDelimiter = false;
            bAllowReassignable = true;
        }

        // if we have subsegments, we are good.  Otherwise, it is an error
        if (moSubSegments.size() > 0)
        {
            return true;
        }

        moSubSegments = null;
        return false;

    } // doScan()

    /**
     * Serialzes XRISegment into IRI normal from
     * @return The IRI normal form of the XRISegment
     */
    public String toIRINormalForm(boolean wantOptionalDelim)
    {
        String sValue = "";
        boolean first = true;

        Iterator oIt = moSubSegments.iterator();
        while (oIt.hasNext())
        {
            sValue += ((XRISubSegment) oIt.next()).toIRINormalForm(wantOptionalDelim || !first);
            first = false;
        }

        return sValue;

    }

    /**
     * Serialzes XRISegment into IRI normal from
     * @return The IRI normal form of the XRISegment
     */
    public String toIRINormalForm()
    {
    	return toIRINormalForm(false);
    }

    /**
     * Serialzes XRISegment into URI normal from
     * @return The URI normal form of the XRISegment
     */
    public String toURINormalForm(boolean wantOptionalDelim)
    {
    	return IRIUtils.IRItoURI(toIRINormalForm(wantOptionalDelim));
    }

    /**
     * Serialzes XRISegment into URI normal from
     * @return The URI normal form of the XRISegment
     */
    public String toURINormalForm()
    {
    	return toURINormalForm(false);
    }

    
    public boolean equals(XRISegment segment)
    {
    	return toString().equals(segment.toString());
    }
    
    
    public boolean equalsIgnoreCase(XRISegment segment)
    {
    	return toString().equalsIgnoreCase(segment.toString());
    }
    
    
    public boolean isPrefixOf(XRISegment segment)
    {
    	int n = this.getNumSubSegments();
    	
    	// first, return false if this segment has more subsegments than the given segment
    	if (n > segment.getNumSubSegments())
    		return false;
    	
    	for (int i = 0; i < n; i++) {
    		XRISubSegment subseg = this.getSubSegmentAt(i);
    		if (!subseg.equalsIgnoreCase(segment.getSubSegmentAt(i)))
    			return false;
    	}
    	return true;
    }
}

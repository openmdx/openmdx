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
* Class: XRIAuthority
********************************************************************************
*/ /**
* This class provides a base class for all types of XRIAuthority elements.
*
* @author =chetan
*/
public abstract class XRIAuthority
    extends AuthorityPath
{
    XRISegment moSegment;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Protected Constructor used by package only
    */
    XRIAuthority()
    {
        super();

    } // Constructor()

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs XRIAuthority from a String
    */
    XRIAuthority(String sPath)
    {
        super(sPath);
        parse();

    } // Constructor()

    /*
    ****************************************************************************
    * getXRISegment()
    ****************************************************************************
    */ /**
    *Returns the XRI Segment for this Authority Path
    * @return  XRISegment The XRI Segment
    */
    public XRISegment getXRISegment()
    {
        parse();
        return moSegment;

    } // getXRISegment()

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
        if (moSegment != null)
        {
            return moSegment.getNumSubSegments();
        }

        return 0;

    } // getNumSubSegments()

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
        if (moSegment != null)
        {
            return moSegment.getSubSegmentAt(nIndex);
        }

        return null;

    } // getSubSegmentAt()

    /*
    ****************************************************************************
    * getLastSubSegment()
    ****************************************************************************
    */ /**
    *Returns the last subsegment in the XRI segment
    * @return SubSegment The last subsegment
    */
    public XRISubSegment getLastSubSegment()
    {
        parse();

        if (moSegment != null)
        {
            int nSize = moSegment.getNumSubSegments();
            if (nSize >= 1)
            {
                return moSegment.getSubSegmentAt(nSize - 1);
            }
        }

        return null;

    } // getLastSubSegment()

    /*
    ****************************************************************************
    * getRootAuthority()
    ****************************************************************************
    */ /**
    *Returns the root XRI Authority as a String
    * @return  String The Root XRI Authority
    */
    public abstract String getRootAuthority();

    /*
    ****************************************************************************
    * getParent()
    ****************************************************************************
    */ /**
    *Returns the parent XRIAuthority for this object.  Equivalent to all but
    *the last SubSegment.
    * @return XRIAuthority The parent XRIAuthority of this object
    */
    public abstract XRIAuthority getParent();

    /*
    ****************************************************************************
    * getParentAsXRI()
    ****************************************************************************
    */ /**
    *Returns the parent XRefAuthority for this object.  Equivalent to all but
    *the last SubSegment.
    * @return XRI The parent XRefAuthority of this object as an XRI
    */
    public XRI getParentAsXRI()
    {
        AuthorityPath oParent = getParent();
        return (oParent == null) ? null : new XRI(oParent);

    } // getParentAsXRI()

} // Class: XRIAuthority

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
* Class: XRIFragment
********************************************************************************
*/ /**
* This class provides a strong typing for a XRI Fragment.  Any
* object of this class that appears outside of the package is a valid
* XRI Fragment.
*
* @author =chetan
*/
public class XRIFragment
    extends Parsable
{
    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *
    */
    XRIFragment() {
     // Constructor()
    } 

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs a XRIFragment from a string. Not implemented.
    */
    public XRIFragment(String sFrag)
    {
        super(sFrag);
        parse();

    } // Constructor()

    
    
    // this is called by Parsable.scan after calling our doScan with the consumed string.
    // we override this method to remove the leading '#'
    void setParsedValue(String sValue)
    {
        if (sValue != null)
        {
        	if (sValue.length() > 0 && sValue.charAt(0) == '#')
        		msValue = sValue.substring(1);
        	else
        		msValue = sValue;
        }
        else
        {
            msValue = "";
        }

        mbParsed = true;
        mbParseResult = true;

    }
    
    

    boolean doScan(ParseStream oStream)
    {
    	if (oStream.empty() || oStream.getData().charAt(0) != '#')
    		return false;

    	oStream.consume(1);
        
        // read the characters, it is ok if they are empty
        int n = scanIFragmentChars(oStream.getData());
        oStream.consume(n);

        return true;
    } // doScan()

    
    private int scanIFragmentChars(String s)
    {
    	
		int c;
		for (int i = 0; i < s.length(); i += UTF16.getCharCount(c)) {
			c = UTF16.charAt(s, i);

            // TODO - ucschar
            //
            // 
            // XXX This is really 'ipchar' according to xri-syntax-v2.0-cs but it eats up ')' and other characters
            // so we're going to be more restrictive here.
            // if (Characters.isIPChar(c))
            if (Characters.isPChar(c))
                continue;

            //
            // escaped
            //
            if (Characters.isEscaped(c, s, i)) {
                i += 2;
                continue;
            }
            
            //
            // "/" or "?"
            if (c == '/' || c == '?')
            	continue;
            return i;
        }

        return s.length();

    }


    public String toIRINormalForm()
    {
    	return IRIUtils.XRItoIRI(toString(), false);
    }

} // Class: XRIFragment

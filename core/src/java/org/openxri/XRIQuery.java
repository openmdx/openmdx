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

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.UTF16;


/*
********************************************************************************
* Class: XRIQuery
********************************************************************************
*/ /**
* This class provides a strong typing for a XRI Query.  Any
* object of this class that appears outside of the package is a valid
* XRI Query.
*
* @author =chetan
*
*/
public class XRIQuery
    extends Parsable
{
	protected String query = null;
	
    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *
    */
    XRIQuery() {
        // Constructor()
    } 

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs a XRIQuery from a string. Not implemented.
    */
    public XRIQuery(String sQuery)
    {
        super(sQuery);
        parse();

    } // Constructor()

    
    // this is called by Parsable.scan after calling our doScan with the consumed string.
    // we override this method to remove the leading '?'
    void setParsedValue(String sValue)
    {
        if (sValue != null)
        {
        	if (sValue.length() > 0 && sValue.charAt(0) == '?')
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
    
    
    /*
    ****************************************************************************
    * doScan()
    ****************************************************************************
    */ /**
    *
    */
    boolean doScan(ParseStream oStream)
    {
    	if (oStream.empty() || oStream.getData().charAt(0) != '?')
    		return false;

    	oStream.consume(1);
        
        // read the characters, it is ok if they are empty
        int n = scanIQueryChars(oStream.getData());
        oStream.consume(n);

        return true;
    } // doScan()

    
    private int scanIQueryChars(String s)
    {
		int c;
		for (int i = 0; i < s.length(); i += UTF16.getCharCount(c)) {
			c = UTF16.charAt(s, i);

            // pchar's
            if (Characters.isIPChar(c))
                continue;

            //
            // escaped
            //
            if (Characters.isEscaped(c, s, i)) {
                i += 2;
                continue;
            }

            //
            // private
            //
            if (UCharacter.getType(c) == UCharacter.PRIVATE_USE)
            	continue;
            
            //
            // "/" or "?"
            if (c == '/' || c == '?')
            	continue;
            return i;
        }

        return s.length();

    } // scanPChars()

    
    public String toIRINormalForm()
    {
    	return IRIUtils.XRItoIRI(toString(), false);
    }
    
} // Class: XRIQuery

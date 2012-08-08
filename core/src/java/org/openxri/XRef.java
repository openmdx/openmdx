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

import java.net.URI;
import java.net.URISyntaxException;


/*
********************************************************************************
* Class: XRef
********************************************************************************
*/ /**
* This class provides a strong typing for a XRI cross reference.  Any
* object of this class that appears outside of the package is a valid
* cross reference.
*
* @author =chetan
*/

// TBD: support URI in xref
public class XRef
    extends Parsable
{
    XRIReference moXRIRef;
    String msIRI;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    * Constructs a cross-reference from a string
    */
    public XRef(String sPath)
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
    XRef()
    {
        super();

    } // Constructor()

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

        if (oStream.getData().charAt(0) != '(')
        {
            return false;
        }

        ParseStream oTempStream = oStream.begin();
        oTempStream.consume(1);

        String sIRI = null;
        // make sure we have a valid XRI reference
        XRIReference oRef = scanXRIReference(oTempStream);
        if (oRef == null || oTempStream.empty() || (oTempStream.getData().charAt(0) != ')'))
        {
           	// if we got a reference, but the resulting temp stream is empty or does not begin with ')' 
        	// it got parsed wrongly (happens if the XRef is an IRI). Retry parsing with an IRI
            if(oRef != null) {
                oTempStream = oStream.begin();
                oTempStream.consume(1);
            }
            // if there is no XRI Reference, see if it is an IRI
            sIRI = scanIRI(oTempStream);
            if (sIRI == null)
            {
                return false;
            }
        }

        // make sure we have the trailing ')'
        if (oTempStream.empty() || (oTempStream.getData().charAt(0) != ')'))
        {
            return false;
        }

        // at this point, complete consumption and return true
        oTempStream.consume(1);
        oStream.end(oTempStream);
        moXRIRef = oRef;
        msIRI = sIRI;

        return true;

    } // doScan()

    /*
    ****************************************************************************
    * scanXRIReference()
    ****************************************************************************
    */ /**
    * Returns a non-null XRIReference if an XRIReference is consumed from the 
    * stream
    */
    static XRIReference scanXRIReference(ParseStream oStream)
    {
        // make sure we have a valid XRI Value
        XRI oXRI = new XRI();
        if (oXRI.scan(oStream))
        {
            return oXRI;
        }

        // try parsing it as a relative XRI
        RelativeXRI oRelXRI = new RelativeXRI();
        if (oRelXRI.scan(oStream)) {
            return oRelXRI;
        }

        return null;

    } // scanXRIReference()
    

	/**
	 * Returns a non-null String if an IRI is consumed from the stream
	 */
     static String scanIRI(ParseStream oStream)
     {
         int n = scanIRIChars(oStream.getData());
         String data = oStream.getData().substring(0, n);
         
         try {
        	 // try parsing to check validity, Java's URI parser may not be IRI compliant so
        	 // this is a TODO
        	 URI u = new URI(data);
             if(!u.isAbsolute()) {
                 return null;
             }
         }
         catch (URISyntaxException e) {
        	 return null;
         }

         oStream.consume(n);
         return data;
     }

     
     /**
      * 
      * @param s
      * @return
      */
     static int scanIRIChars(String s)
     {
         for (int i = 0; i < s.length(); i++)
         {
             char c = s.charAt(i);

             // assume that ')' has been escaped out
             if (c == ')')
            	 return i;
         }
         return s.length();
     }
     
     
    /*
    ****************************************************************************
    * getXRIReference()
    ****************************************************************************
    */ /**
    * @return XRIReference
    */
    public XRIReference getXRIReference()
    {
        parse();
        return moXRIRef;

    } // getXRIReference()
    
    /*
     ****************************************************************************
     * getIRI()
     ****************************************************************************
     */ /**
     * @return XRIValue
     */
     public String getIRI()
     {
         parse();
         return msIRI;

     } // getXRIReference()


     /**
     * Serialzes XRef into IRI normal from
     * @return The IRI normal form of the XRef
     */
    public String toIRINormalForm()
    {
        return IRIUtils.XRItoIRI(toString(), true);
    }
    
    
    public String toURINormalForm()
    {
    	return IRIUtils.IRItoURI(toIRINormalForm());
    }

}

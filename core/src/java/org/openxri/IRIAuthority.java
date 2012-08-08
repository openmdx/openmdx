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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;


/*
********************************************************************************
* Class: IRIAuthority
********************************************************************************
*/ /**
* This class provides a strong typing for a IRI Authority.  Any
* object of this class that appears outside of the package is a valid
* IRI Authority.  It currently only accepts IRI Authorities that serve as IP
* Addresses or appear to be valid host names
*
* @author =chetan
*/
public class IRIAuthority
    extends AuthorityPath
{
    private URI moURI = null;

    /*
    ****************************************************************************
    * Constructor()
    ****************************************************************************
    */ /**
    *
    */
    public IRIAuthority(String sPath)
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
    IRIAuthority()
    {
        super();

    } // Constructor()

    /*
    ****************************************************************************
    * getIUserInfo()
    ****************************************************************************
    */ /**
    * Returns the  userinfo portion of the IRI Authority
    */
    public String getIUserInfo()
    {
        return moURI.getUserInfo();

    } // getIUserInfo()

    /*
    ****************************************************************************
    * getIHost()
    ****************************************************************************
    */ /**
    * Returns the host portion of the IRI Authority
    */
    public String getIHost()
    {
        return moURI.getHost();

    } // getIHost()

    /*
    ****************************************************************************
    * getPort()
    ****************************************************************************
    */ /**
    * Returns the port portion of the IRI Authority
    */
    public int getPort()
    {
        return moURI.getPort();

    } // getPort()

    /*
    ****************************************************************************
    * doScan()
    ****************************************************************************
    */ /**
    * Scans the Stream for a valid IRI-Authority
    */
    boolean doScan(ParseStream oStream)
    {
        boolean bVal = false;
        int n = scanChars(oStream.getData());
        String sData = oStream.getData().substring(0, n);
        try
        {
            moURI = new URI("http", sData, null, null, null);
            String sHost = moURI.getHost();
            if ((sHost != null) && (sHost.length() > 0))
            {
                char cFirst = sHost.charAt(0);
                boolean bCheckIP = Character.isDigit(cFirst) ||
                    (cFirst == '[');
                bVal = bCheckIP ? verifyIP(sHost) : verifyDNS(sHost);
            }
        }
        catch (URISyntaxException e) {}

        // consume and return true if valid
        if (bVal)
        {
            oStream.consume(n);
            return true;
        }

        return false;

    } // doScan()

    /*
    ****************************************************************************
    * verifyDNS()
    ****************************************************************************
    */ /**
    *
    * @param host
    * @return
    */
    private boolean verifyDNS(String sHost)
    {
        // TODO Auto-generated method stub
        return true;

    } // verifyDNS()

    /*
    ****************************************************************************
    * verifyIP()
    ****************************************************************************
    */ /**
    *
    */
    private boolean verifyIP(String sIP)
    {
        try
        {
            InetAddress oAddr = InetAddress.getByName(sIP);
            return oAddr != null;
        }
        catch (UnknownHostException e) {}
        return false;

    } // verifyIP()

    /*
    ****************************************************************************
    * scanChars()
    ****************************************************************************
    */ /**
    *
    * @param data
    * @return
    */
    private int scanChars(String s)
    {
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);

            // not exactly spec compliant, but does the right thing
            switch (c)
            {
                case '/':
                case ')':
                case '#':
                case '?':
                    break;
                default:
                    continue;
            }

            return i;
        }

        return s.length();

    }

    
    /**
     * Serialzes the IRIAuthority into IRI normal from
     * @return The IRI normal form of the IRIAuthority
     */
    public String toIRINormalForm()
    {
        return IRIUtils.XRItoIRI(toString(), false);
    }

    
    /**
     * Serialzes the IRIAuthority into URI normal from
     * @return The URI normal form of the IRIAuthority
     */
    public String toURINormalForm()
    {
        return IRIUtils.IRItoURI(toIRINormalForm());
    }

}

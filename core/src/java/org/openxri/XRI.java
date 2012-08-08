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

import java.io.UnsupportedEncodingException;


/**
 * This class provides a strong typing for a XRI.  Any
 * object of this class that appears outside of the package is a valid
 * XRI.  THERE ARE INTENTIONALLY NO SET METHODS.  Use this class like
 * java.lang.String or java.net.URI
 *
 * @author =chetan
 */
public class XRI
    extends Parsable
    implements XRIReference
{
    public static final String PDELIM_S = "!";
    public static final String RDELIM_S = "*";
    public static final char PDELIM = '!';
    public static final char RDELIM = '*';
    public static final String XRI_SCHEME = "xri://";
    public static final int XRI_SCHEME_LENGTH = XRI_SCHEME.length();
    
    AuthorityPath moAuthorityPath = null;
    XRIAbsolutePath moAbsolutePath = null;
    XRIQuery query = null;
    XRIFragment fragment = null;


    /**
     * Protected Constructor used by package only
     */
    XRI()
    {
     // Constructor()
    } 


    /**
     * Constructs an XRI from the provided XRI
     */
    public XRI(XRI oXRI)
    {
        moAuthorityPath = oXRI.getAuthorityPath();
        moAbsolutePath = oXRI.getXRIAbsolutePath();
        query = oXRI.getQuery();
        fragment = oXRI.getFragment();
        setParsedXRI();

    }

    
    /**
     * 
     * @return absolute path
     */
    public XRIAbsolutePath getXRIAbsolutePath()
    {
        return moAbsolutePath;

    }


    /**
     * Constructs XRI from String
     */
    public XRI(String sXRI)
    {
        super(sXRI);
        parse();

    }


    /**
     * Constructs an XRI from the provided AuthorityPath
     */
    public XRI(AuthorityPath oAuthority)
    {
        moAuthorityPath = oAuthority;
        setParsedXRI();

    }


    /**
     * Constructs an XRI from the provided AuthorityPath and LocalPath
     * @param oAuthority
     * @param oPath
     */
    public XRI(AuthorityPath oAuthority, XRIPath oPath)
    {
        if (oAuthority == null)
        {
            throw new XRIParseException();
        }

        moAuthorityPath = oAuthority;
        if (oPath != null)
        {
            if (oPath instanceof XRINoSchemePath)
            {
                moAbsolutePath = new XRIAbsolutePath((XRINoSchemePath) oPath);
            }
            else if (oPath instanceof XRIAbsolutePath)
            {
                moAbsolutePath = (XRIAbsolutePath) oPath;
            }
        }
        setParsedXRI();

    }

    
    /**
     * Constructs an XRI from the provided AuthorityPath, LocalPath, Query and Fragment
     * @param oAuthority
     * @param oPath
     * @param query
     * @param fragment
     */
    public XRI(AuthorityPath oAuthority, XRIPath oPath, XRIQuery query, XRIFragment fragment)
    {
        if (oAuthority == null)
        {
            throw new XRIParseException();
        }

        moAuthorityPath = oAuthority;
        if (oPath != null)
        {
            if (oPath instanceof XRINoSchemePath)
            {
                moAbsolutePath = new XRIAbsolutePath((XRINoSchemePath) oPath);
            }
            else if (oPath instanceof XRIAbsolutePath)
            {
                moAbsolutePath = (XRIAbsolutePath) oPath;
            }
        }
        
        this.query = query;
        this.fragment = fragment;
        
        setParsedXRI();

    }

    
    /**
     * Constructs an XRI from the provided XRI reference in IRI Normal Form
     * @param iri
     * @return
     */
    public static XRI fromIRINormalForm(String iri)
    {
    	String xriNF = IRIUtils.IRItoXRI(iri);
    	return new XRI(xriNF);
    }

    
    /**
     * Constructs an XRI from the provided XRI reference in URI Normal Form
     * @param iri
     * @return
     */
    public static XRI fromURINormalForm(String uri)
    {
    	String iriNF;
		try {
			iriNF = IRIUtils.URItoIRI(uri);
		}
		catch (UnsupportedEncodingException e) {
			// we're only using UTF-8 which should really be there in every JVM
			throw new XRIParseException("UTF-8 encoding not supported: " + e.getMessage());
		}

		String xriNF = IRIUtils.IRItoXRI(iriNF);
    	return new XRI(xriNF);
    }


    /**
     * This is used by constructors that need to set the parsed value
     * without actually parsing the XRI.
     */
    void setParsedXRI()
    {
        String sValue = XRI_SCHEME + moAuthorityPath.toString();

        // add the local path and relative path as necessary
        if (moAbsolutePath != null)
        {
            sValue += moAbsolutePath.toString();
        }

        if (query != null) {
        	sValue += query.toString();
        }
        
        if (fragment != null) {
        	sValue += query.toString();
        }
        
        setParsedValue(sValue);
    }


    /**
     *  returns returns true if the XRI is absolute
     * @return boolean returns true if the XRI is absolute
     */
    public boolean isAbsolute()
    {
        parse();
        return (moAuthorityPath != null);

    }
    
    /**
     *  returns returns true if the XRI is relative
     * @return boolean returns true if the XRI is relative
     */
    public boolean isRelative()
    {
        return !isAbsolute();

    }

    
    /**
     * Parses the input stream into an Authority Path
     * @param oStream The input stream to scan from
     */
    static AuthorityPath scanSchemeAuthority(ParseStream oParseStream)
    {
        if (oParseStream.empty())
        {
            return null;
        }

        ParseStream oAuthStream = oParseStream.begin();

        // The xri:// is optional
        if ((oParseStream.getData().length() >= XRI_SCHEME_LENGTH))
        {
            String sScheme =
                oAuthStream.getData().substring(0, XRI_SCHEME_LENGTH);
            if ((sScheme != null) && sScheme.equalsIgnoreCase(XRI_SCHEME))
            {
                oAuthStream.consume(XRI_SCHEME_LENGTH);
            }
        }

        // see if we get an authority
        AuthorityPath oAuthorityPath = AuthorityPath.scanAuthority(oAuthStream);

        // if we found one, consume the entire auth stream, including 
        // the scheme
        if (oAuthorityPath != null)
        {
            oParseStream.end(oAuthStream);
        }

        return oAuthorityPath;

    }

    
    public String toIRINormalForm()
    {
        String iri = "";

        // add the authority path if it is there
        if (moAuthorityPath != null)
        {
            iri = XRI_SCHEME + moAuthorityPath.toIRINormalForm();
        }

        // add the local path and relative path as necessary
        if (moAbsolutePath != null)
        {
            iri += moAbsolutePath.toIRINormalForm();
        }

        if (query != null)
        	iri += "?" + query.toIRINormalForm();
        
        if (fragment != null)
        	iri += "#" + fragment.toIRINormalForm();
        
        return iri;
    }
    
    
	/**
	 * Serialzes the XRI into IRI normal from
	 * @return The IRI normal form of the XRI
	 */
    public String toURINormalForm()
    {
    	String iri = toIRINormalForm();
    	return IRIUtils.IRItoURI(iri);
    }

    
    /**
     * Parses the input stream into the object
     * @param oStream The input stream to scan from
     * @return  boolean True if part of the Stream was consumed into the object
     */
    boolean doScan(ParseStream oStream)
    {
        moAuthorityPath = scanSchemeAuthority(oStream);
        if (moAuthorityPath == null)
        {
            return false;
        }

        XRIAbsolutePath oPath = new XRIAbsolutePath();
        if (oPath.scan(oStream))
        {
            moAbsolutePath = oPath;
        }

        XRIQuery query = new XRIQuery();
        if (query.scan(oStream)) {
        	this.query = query;
        }
        
        XRIFragment fragment = new XRIFragment();
        if (fragment.scan(oStream)) {
        	this.fragment = fragment;
        }
        
        return true;

    }

    
    public AuthorityPath getAuthorityPath()
    {
        return moAuthorityPath;

    }


    public XRIPath getXRIPath()
    {
        return moAbsolutePath;
    }

    
	/**
	 * @return Returns the query.
	 */
	public XRIQuery getQuery()
	{
		return query;
	}


	/**
	 * @return Returns the fragment.
	 */
	public XRIFragment getFragment()
	{
		return fragment;
	}

}

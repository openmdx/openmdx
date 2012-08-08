/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: LightweightClassLoader.java,v 1.7 2008/01/15 17:28:31 hburger Exp $
 * Description: Lightweight Class Loader
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/15 17:28:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.application.deploy.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.environment.SystemProperties;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.kernel.url.protocol.XriProtocols;
import org.openmdx.kernel.url.protocol.xri.ZipURLConnection;

/**
 * Lightweight Class Loader
 */
public class LightweightClassLoader extends URLClassLoader 
	implements MultiLineStringRepresentation
{

    /**
     * Constructor
     * 
     * @param type 
     * @param shortDescription 
     * @param parent
     * @param urls
     */
    public LightweightClassLoader(
        String type, 
        String shortDescription, 
        ClassLoader parent
    ) {
        super(EMPTY_SET, parent);
        this.type = type;
        this.shortDescription = shortDescription;
    }

    /**
     * Start with an empty set of <code>URL</code>'s
     */
    private static final URL[] EMPTY_SET = new URL[]{};

	/**
     * 
     */
    private final String type;

	/**
     * 
     */
    private final String shortDescription;

    /**
     * Add a list of URLs to the lightweight class loader's search path,
     * including libraries referred to by manifest class-path entries.
     * 
     * @param urls
     * @param report 
     * 
     * @throws IOException
     */
    public void addURLs(
        URL[] urls, 
        Report report 
    ){
    	Set<URL> currentUrls = new HashSet<URL>(Arrays.asList(getURLs()));
    	List<URL> newUrls = new ArrayList<URL>(Arrays.asList(urls));
    	while(!newUrls.isEmpty()) {
    	    URL newURL = newUrls.remove(0);
    	    try {
        		URL url = toCanonicalForm(newURL);
        		if(currentUrls.add(url)){
        			newUrls.addAll(Arrays.asList(getManifestClassPath(url)));
        			URL implementationURL = getImplementationURL(url); 
                    try {
                        //
                        // Validate URL
                        //
            			implementationURL.openStream().close(); 
                        addURL(implementationURL);
                    } catch (IOException exception) {
                        report.addWarning(
                            this.type + " classloader " + this.shortDescription + " can't verify URL " + implementationURL,
                            exception
                        );
                    }
        		}
        	} catch (IOException exception) {
                report.addWarning(
                    this.type + " classloader " + this.shortDescription + " can't validate URL " + newURL,
                    exception
                );
        	}
    	}
    }

	/**
     * Converts file URL to their canaonical form
     * 
     * @param url
     * @return
     */
    protected static URL toCanonicalForm(
    	URL url
    ) throws IOException {
    	File file = toFile(url);
    	return file == null ?
    		url :
    		file.toURL();
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return 
			type + " Class Loader (" + shortDescription +
			"): URLs=" + new IndentingFormatter(getURLs()) +
			",\nparent=" + getParent();
	}

    /**
     * Tests whether an URL is a "file" url.
     * 
     * @param url the URL to be tested
     * @return the corresponding File if the URL is a "file" URL;
     *         null otherwise
     * @throws IOException
     */
    static private File toFile(
        URL url
    ) throws IOException{
    	String uri = url.toExternalForm();
      	if(!"file".equals(url.getProtocol().toLowerCase())) return null;
      	return new File(uri.substring(5)).getCanonicalFile();
    }

    /**
     * The Manifest is optional.
     * 
     * @param module
     * @return module's manifest; or null
     * @throws IOException
     */
    static public Manifest getManifest(
        File module
    ) throws IOException {
    	if(module.isDirectory()) {
    		File manifestFile = new File(module, JarFile.MANIFEST_NAME);
    		if(manifestFile.isFile()) return new Manifest(
    			new FileInputStream(manifestFile)
    		);
    	} else if(module.isFile()) return getManifest(
    		module.toURL().toExternalForm()
    	);
    	return null;
    }

    /**
     * The Manifest is optional.
     * 
     * @param module
     * @return module's manifest; or null
     * @throws IOException
     */
    static protected Manifest getManifest(
        String module
    ) throws IOException {
      	try {
    	    return ((ZipURLConnection)
    	    	new URL(
    	    		XriProtocols.ZIP_PREFIX + module + XriProtocols.ZIP_SEPARATOR
    	    	).openConnection()
    		).getManifest();
      	} catch (IOException exception){
      		throw new IOException(
      			"Accessing '" + module + "' as JAR archive failed: " + exception
    		);
      	}
    }

    /**
     * The Manifest is optional.
     * 
     * @param module
     * @return module's manifest; or null
     * @throws IOException
     */
    static public Manifest getManifest(
        URL module
    ) throws IOException {
    	File file = toFile(module);
    	return file == null ?
    		getManifest(module.toExternalForm()) :
    		getManifest(file);
    }

    /**
     * The Manifest is optional as well as its Class-Path attribute.
     * Return an empty array if either is missing.
     * 
     * @param url
     * @return the manifest class path; never null.
     * @throws IOException
     */
    static public URL[] getManifestClassPath(
        URL url
    ) throws IOException {
      	Manifest manifest = getManifest(url);
      	String archiveString = url.toExternalForm();
    	List<URL> classPath = new ArrayList<URL>();
      	if(manifest != null){  		
      		String attributeValue = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
      		if(attributeValue != null) {
          		URL context = archiveString.endsWith("/") ? new URL(url, "..") : url; 
      			for(
	      		    StringTokenizer tokens = new StringTokenizer(attributeValue);
	      		    tokens.hasMoreTokens();
	      		) classPath.add(
	      		    new URL(context, tokens.nextToken())
	      		); 
      		}
      	}
    	SysLog.detail(
    		archiveString + " has " + (
    			manifest == null ? "no" : "a"
    		) + " Manifest leading to the following Class-Path", 
    		classPath
    	);
      	return classPath.toArray(new URL[classPath.size()]);
    }

    /**
     * The Manifest is optional as well as its Implementation-URL attribute.
     * Return the original URL of either is missing
     * 
     * @param url
     * @return the manifest class path; never null.
     * @throws IOException
     */
    static protected URL getImplementationURL(
        URL url
    ) throws IOException {
      	Manifest manifest = getManifest(url);
      	String archiveString = url.toExternalForm();
    	URL implementationURL = null;
      	if(manifest != null){  		
      		String attributeValue = SystemProperties.expand(
                manifest.getMainAttributes().getValue(
                    Attributes.Name.IMPLEMENTATION_URL
                )
      		);
      		if(attributeValue != null) {
          		URL context = archiveString.endsWith("/") ? new URL(url, "..") : url; 
      			implementationURL = new URL(context, attributeValue); 
      		}
      	}
      	if(implementationURL == null) {
        	SysLog.detail(
        		archiveString + " has no Implementation-URL Manifest entry", 
        		url
        	);
        	return url;
      	} else {
        	SysLog.detail(
        		archiveString + " has an Implementation-URL Manifest entry leading to the following location", 
        		implementationURL
        	);
        	return implementationURL;
      	}
    }

    /**
     * Factory method
     * 
     * @param type 
     * @param shortDescription 
     * @param parent
     * @param urls
     * @param report
     * 
     * @throws IOException 
     */
    public static LightweightClassLoader newInstance(
        String type, 
        String shortDescription, 
        ClassLoader parent, 
        URL[] urls,
        Report report
    ) throws IOException {
        LightweightClassLoader instance = new LightweightClassLoader(
            type, 
            shortDescription, 
            parent
        );
        instance.addURLs(urls, report);
        return instance;
    }
    
}

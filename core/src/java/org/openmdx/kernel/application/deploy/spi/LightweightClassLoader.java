/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightClassLoader.java,v 1.10 2008/09/08 11:45:37 hburger Exp $
 * Description: Lightweight Class Loader
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/08 11:45:37 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.application.deploy.spi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.configuration.ReportLogger;
import org.openmdx.kernel.environment.SystemProperties;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
import org.openmdx.kernel.url.protocol.xri.ZipURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Keep track of missing classes
     */
    private final ConcurrentMap<String,ClassNotFoundException> missingClasses = new ConcurrentHashMap<String,ClassNotFoundException>();

    /**
     * The 
     */
    private static final URL NO_RESOURCE = noResource();
    
    /**
     * Keep track of missing resources
     */
    private final ConcurrentMap<String,URL> resources = NO_RESOURCE == null ? 
        null : // no tracking will take place
        new ConcurrentHashMap<String,URL>();
    
    /* (non-Javadoc)
     * @see java.net.URLClassLoader#findResource(java.lang.String)
     */
    @Override
    public URL findResource(
        String name
    ) {
        if(this.resources == null) {
            return super.findResource(name);
        } else {
            URL resource = this.resources.get(name);
            if(resource == null) {
                resource = super.findResource(name);
                this.resources.putIfAbsent(
                    name, 
                    resource == null ? NO_RESOURCE : resource
                );
                return resource;
            } else {
                return resource == NO_RESOURCE ? null : resource;
            }
        }
    }

    /* (non-Javadoc)
     * @see java.net.URLClassLoader#findClass(java.lang.String)
     */
    @Override
    protected Class<?> findClass(
        String name
    ) throws ClassNotFoundException {
        ClassNotFoundException notFound = this.missingClasses.get(name);
        if(notFound == null) {
            try {
                return super.findClass(name);
            } catch (ClassNotFoundException exception) {
                this.missingClasses.putIfAbsent(
                    name,
                    notFound = exception
                );
            }
        }
        throw notFound;
    }

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
    	boolean added = false;
    	Logger logger = new ReportLogger(report);
    	while(!newUrls.isEmpty()) {
    	    URL newURL = newUrls.remove(0);
    	    try {
        		URL url = toCanonicalForm(newURL);
        		if(currentUrls.add(url)){
        		    added = true;
        			newUrls.addAll(Arrays.asList(getManifestClassPath(url, logger)));
        			URL implementationURL = getImplementationURL(url, logger); 
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
    	if(added){
    	    this.missingClasses.clear();
            this.resources.clear();
    	}
    }

    /**
     * Provide the NO_RESOURCE place holder object
     * @return
     */
    private static URL noResource(
    ){
        try {
            return new URL("file","","");
        } catch (MalformedURLException exception) {
            LoggerFactory.getLogger(
                LightweightClassLoader.class
            ).error(
                "NO_RESOURCE place holder URL \"file:\" can't created, no resource tracking will take place",
                exception
            );
            return null;
        }
    }
    
	/**
     * Converts file URL to their canonical form
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
    	    		XRI_2Protocols.ZIP_PREFIX + module + XRI_2Protocols.ZIP_SEPARATOR
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
     * @param logger
     *  
     * @return the manifest class path; never <code>null</code>.
     * 
     * @throws IOException
     */
    static public URL[] getManifestClassPath(
        URL url, 
        Logger logger
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
        logger.info(
            "{} has {} Manifest leading to the following Class-Path: {}",
            archiveString,
            manifest == null ? "no" : "a",
            classPath
        );
        return classPath.toArray(new URL[classPath.size()]);
    }
    
    /**
     * The Manifest is optional as well as its Implementation-URL attribute.
     * Return the original URL of either is missing
     * 
     * @param url
     * @param logger 
     * 
     * @return the manifest class path; never <code>null</code>.
     * 
     * @throws IOException
     */
    static protected URL getImplementationURL(
        URL url, 
        Logger logger
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
      	    logger.info(
      	        "{} has no Implementation-URL Manifest entry: {}",
      	        archiveString, 
        		url
        	);
        	return url;
      	} else {
            logger.info(
                "{} has an Implementation-URL Manifest entry leading to the following location: ",
                archiveString, 
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

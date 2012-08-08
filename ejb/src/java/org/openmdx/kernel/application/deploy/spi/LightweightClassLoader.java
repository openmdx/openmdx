/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightClassLoader.java,v 1.6 2010/06/04 22:45:00 hburger Exp $
 * Description: Lightweight Class Loader
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:45:00 $
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
import java.text.MessageFormat;
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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.openmdx.kernel.log.LoggerFactory;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.openmdx.kernel.url.protocol.XRI_2Protocols;
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
    	LogSink logger = new ReportSink(report);
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
            LoggerFactory.getLogger().log(
            	Level.SEVERE,
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
    		file.toURI().toURL();
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
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
    		module.toURI().toURL().toExternalForm()
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
    	return getManifestClassPath(url, new LoggerSink(logger));
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
    private static URL[] getManifestClassPath(
        URL url, 
        LogSink logger
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
        LogRecord logRecord = new LogRecord(
        	Level.INFO,
        	manifest == null ? "{0} has no Manifest leading to the following Class-Path: {1}" : "{0} has a Manifest leading to the following Class-Path: {1}"
        );
        logRecord.setParameters(
        	new Object[]{
                archiveString,
                classPath
            }
        );
        logger.log(logRecord);
        return classPath.toArray(new URL[classPath.size()]);
    }
    
    /**
     * The Manifest is optional as well as its Implementation-URL attribute.
     * Return the original URL of either is missing
     * 
     * @param url
     * @param logSink 
     * 
     * @return the manifest class path; never <code>null</code>.
     * 
     * @throws IOException
     */
    static protected URL getImplementationURL(
        URL url, 
        LogSink logSink
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
      		LogRecord logRecord = new LogRecord(
      			Level.INFO, 
      			"{0} has no Implementation-URL Manifest entry: {1}"
      		);
      		logRecord.setParameters(
      			new Object[]{archiveString,url}
        	);
      		logSink.log(logRecord);
        	return url;
      	} else {
      		LogRecord logRecord = new LogRecord(
      			Level.INFO, 
      			"{0} has an Implementation-URL Manifest entry leading to the following location: {1}"
      		);
      		logRecord.setParameters(
      			new Object[]{archiveString,implementationURL}
        	);
      		logSink.log(logRecord);
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

    
    //------------------------------------------------------------------------
    // Interface Logger
    //------------------------------------------------------------------------

    /**
     * Log Sink
     */
    public interface LogSink {
    	
    	void log (LogRecord logRecord);
    	
    }

    
    //------------------------------------------------------------------------
    // Class ReportSink
    //------------------------------------------------------------------------

    /**
     * Report Sink
     */
    static class ReportSink implements LogSink {
        
        /**
         * Constructor 
         *
         * @param sink
         */
        ReportSink(Report sink) {
            this.sink = sink;
        }

        /**
         * 
         */
        private final Report sink;

        /**
         * 
         */
		public void log(LogRecord logRecord) {
			int level = logRecord.getLevel().intValue();
			Object[] parameters = logRecord.getParameters();
			Throwable thrown = logRecord.getThrown();
			try {
			String message = logRecord.getMessage();
			if(parameters != null && parameters.length != 0) try {
				message = MessageFormat.format(message, parameters);
			} catch (IllegalArgumentException exception) {
				// Use the pattern as message
			}
			if(level >= Level.SEVERE.intValue()) {
				if(thrown == null) {
					this.sink.addError(message);
				} else {
					this.sink.addError(message, thrown);
				}
			} else if (level >= Level.WARNING.intValue()) {
				if(thrown == null) {
					this.sink.addWarning(message);
				} else {
					this.sink.addWarning(message, thrown);
				}
			} else {
				this.sink.addInfo(message);
			}
			} catch (RuntimeException e) {
				throw e;
			}
		} 

    }

    //------------------------------------------------------------------------
    // Class LoggerSink
    //------------------------------------------------------------------------

    /**
     * Report Logger
     */
    static class LoggerSink implements LogSink {
        
        /**
         * Constructor 
         *
         * @param sink
         */
    	LoggerSink(Logger sink) {
            this.sink = sink;
        }

        /**
         * 
         */
        private final Logger sink;

        /**
         * 
         */
		public void log(LogRecord logRecord) {
			this.sink.log(logRecord);
		}
    }

}

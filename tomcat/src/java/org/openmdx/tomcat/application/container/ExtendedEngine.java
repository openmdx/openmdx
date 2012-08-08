/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ExtendedEngine.java,v 1.10 2008/06/06 17:44:57 wfro Exp $
 * Description: Extended Engine
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/06 17:44:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
package org.openmdx.tomcat.application.container;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardEngine;
import org.apache.juli.logging.Log;
import org.openmdx.kernel.Version;
import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher;

/**
 * Extended Engine
 */
public class ExtendedEngine extends StandardEngine {

	/**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -7761462319055378832L;

	/**
	 * The deployment unit directory
	 */
	protected String deploymentDirectory = "deployment-units";

	/**
	 * 
	 */
	private transient File deploymentDir = null;
	
	/**
	 * The staging area
	 */
	protected String stagingDirectory = "staging-area";

	/**
	 * 
	 */
	private transient File stagingDir = null;
	
    /**
     * 
     */
    private final ApplicationContextFactory applicationContextFactory = new ApplicationContextFactory();
    
    /**
     * 
     */
    private final ComponentContextFactory componentContextFactory = new ComponentContextFactory();

    /**
     * 
     */
    private final ContextSwitcher threadContextSwitcher = new ThreadContextSwitcher();
    
    /**
     * Retrieve the ApplicationContextFactory
     * 
     * @return the ApplicationContextFactory
     */
    protected ApplicationContextFactory getApplicationContextFactory(){
    	return this.applicationContextFactory;
    }
    
	/**
	 * @return the deploymentDirectory
	 */
	public final String getDeploymentDirectory() {
		return deploymentDirectory;
	}

	/**
	 * @param deploymentDirectory the deploymentDirectory to set
	 */
	public final void setDeploymentDirectory(String deploymentDirectory) {
		this.deploymentDirectory = deploymentDirectory;
	}

	/**
	 * @return the stagingDirectory
	 */
	public final String getStagingDirectory() {
		return this.stagingDirectory;
	}

	/**
	 * @param stagingDirectory the stagingDirectory to set
	 */
	public final void setStagingDirectory(String stagingDirectory) {
		this.stagingDirectory = stagingDirectory;
	}

	/**
	 * Provide the deployment directory
	 * 
	 * @return the deployment directory
	 */
	protected File getDeploymentDir(){
		return this.deploymentDir == null ? this.deploymentDir = new File(
			new File(getBaseDir()), 
			getDeploymentDirectory()
		) : this.deploymentDir;
	}

	/**
	 * Provide the staging directory
	 * 
	 * @return the staging directory
	 */
	protected File getStagingDir(){
		return this.stagingDir == null ? this.stagingDir = new File(
			new File(getBaseDir()), 
			getStagingDirectory()
		) : this.stagingDir;
	}
		
	protected File[] getResourceAdapterFiles(boolean staging){
		return getDeploymentUnit(staging, RAR_FILTER);
	}

	protected File[] getEnterpriseApplicationFiles(boolean staging){
		return getDeploymentUnit(staging, EAR_FILTER);
	}

	private File[] getDeploymentUnit(
		boolean staging,
		FilenameFilter filter
	){
		File[] files = (
			staging ? getStagingDir() : getDeploymentDir()
		).listFiles(
			filter
		);
		return files == null ? new File[0] : files;
	}
	
	/**
	 * Test whether a given application is deployed
	 * 
	 * @param enterpriseApplication
	 * 
	 * @return <code>true</code> if the given application is deployed
	 */
	protected boolean isRunning(
		String enterpriseApplication
	){
		return this.applicationContextFactory.getClassLoader(enterpriseApplication) != null;
	}
	
	private boolean deleteWebApplication(
		String uri
	){
		File webApplicationDirectory = new File(
			this.stagingDir,
			uri
		);
		boolean summary = true;
		if(webApplicationDirectory.isDirectory()) {
			for(File webApplicationFile: webApplicationDirectory.listFiles()) {
				boolean detail = webApplicationFile.delete();
				summary &= detail;
				logger.debug(
					"Web Application File " +(detail ? "removed" : "removal failed") + ": " + webApplicationFile
				);
			}
			if(summary) {
				summary = webApplicationDirectory.delete(); 
				logger.debug(
					"Web Application staging directory " + (summary ? "removed" : "removal failed") + ": " + webApplicationDirectory
				);
			}
		}
		return summary;
	}
	
	private boolean move(
		File from,
		File to
	){
		if(!from.renameTo(to)) try {
			InputStream source = new FileInputStream(from);
			OutputStream target = new FileOutputStream(to);
			byte[] buffer = new byte[0x10000];
			for(
				int i = 0;
				i >= 0;
			){
				i = source.read(buffer);
				target.write(buffer, 0, i);
			}
			target.flush();
			target.close();
			source.close();
			if(!from.delete()) {
				getLogger().warn("Unable to remove staged archive '" + from + "' after deployment");
			}
		} catch (Exception exception) {
			getLogger().debug("Could not move staged archive from '" + from + "' to '" + to + "'");
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.catalina.core.StandardEngine#start()
	 */
	@Override
	public void start() throws LifecycleException {
        if(!started ) {
        	Log log = getLogger();
        	try {        		
	        	log.info(
	    			"Starting embedded EJB container for engine '" + getName() + "': openMDX Lightweight Container/" + 
	    			Version.getImplementationVersion()
	        	);
	    		LightweightContainer embeddedContainer = LightweightContainer.getInstance(
    				this.applicationContextFactory,
	    			this.componentContextFactory, 
	    			this.threadContextSwitcher
	    		);
	    		if(getDeploymentDir().isDirectory()) {
	        		//
	        		// Pending Deployments
	        		// 
		    		File[] resourceAdapterFiles = getResourceAdapterFiles(true);
	    			log.info(
    					"Process " + resourceAdapterFiles.length + " pending resource adapter deployments from '" + stagingDir + "'"
	    			);
	    			for(File newArchive : resourceAdapterFiles){
	    				File oldArchive = new File(getDeploymentDir(), newArchive.getName());
	    				if(oldArchive.isDirectory()) {
	    					log.warn("Expanded resource adapter has to be undeployed manually: " + oldArchive);
	    				} else if (!oldArchive.delete()) {
	    					log.warn("Resource adapter archive could not be undeployed: " + oldArchive);
	    				} else if(newArchive.length() == 0) {
	    					if(newArchive.delete()) {
		    					log.info("Resource adapter archive successfully undeployed: " + oldArchive);
	    					} else {
		    					log.warn("Resource adapter undeployment marker file could not be deleted: " + newArchive);
	    					}
	    				} else if(move(newArchive, oldArchive)) {
	    					log.info("Resource adapter archive successfully deployed from " + newArchive + " to " + oldArchive);
	    				} else {
	    					log.warn("Resource adapter archive could not be deployed from " + newArchive + " to " + oldArchive);
	    				}
	    			}
		    		File[] enterpriseApplicationFiles = getEnterpriseApplicationFiles(true);
	    			log.info(
    					"Process " + enterpriseApplicationFiles.length + " pending enterprise applications deplpyments from '" + stagingDir + "'"
	    			);
	    			for(File newArchive : enterpriseApplicationFiles) {
	    				File oldArchive = new File(getDeploymentDir(), newArchive.getName());
	    				if(oldArchive.isDirectory()) {
	    					log.warn("Expanded enterprise application has to be undeployed manually: " + oldArchive);
	    				} else if (oldArchive.exists() && !oldArchive.delete()) {
	    					log.warn("Enterprise application archive could not be undeployed: " + oldArchive);
	    				} else if (!deleteWebApplication(EAR_FILTER.getName(oldArchive))){
	    					log.warn("An EARs web application staging directory could not be cleared: " + EAR_FILTER.getName(oldArchive));
	    				} else if(newArchive.length() == 0) {
	    					if(newArchive.delete()) {
		    					log.info("Enterprise application archive successfully undeployed: " + oldArchive);
	    					} else {
		    					log.warn("Enterprise application undeployment marker file could not be deleted: " + newArchive);
	    					}
	    				} else if(move(newArchive, oldArchive)) {
	    					log.info("Enterprise application archive successfully deployed from " + newArchive + " to " + oldArchive);
	    				} else {
	    					log.warn("Enterprise application archive could not be deployed from " + newArchive + " to " + oldArchive);
	    				}
	    			}
	    			//
	        		// Start
	        		//
		    		resourceAdapterFiles = getResourceAdapterFiles(false);
	    			log.info(
    					"Start " + resourceAdapterFiles.length + " resource adapters from '" + deploymentDir + "'"
	    			);
	    			for(File file : resourceAdapterFiles){
	    				deploy(embeddedContainer, file);
	    			}
		    		enterpriseApplicationFiles = getEnterpriseApplicationFiles(false);
	    			log.info(
    					"Start " + enterpriseApplicationFiles.length + " enterprise applications from '" + deploymentDir + "'"
	    			);
	    			for(File file : enterpriseApplicationFiles) {
	    				deploy(embeddedContainer, file);
	    			}
	    		} else {
	    			log.warn(
	    				"The deployment directory '" + deploymentDir + "' does not exist"
	    			);
	    		}
        	} catch (RuntimeException exception) {
        		log.fatal(
        			"Embedded EJB container could not be started", 
        			exception
        		);
        	}
        }
    	super.start();
	}
	
	boolean deploy(
		LightweightContainer target,
		File file
	){
		try {
			URL url = file.toURL();
			if(RAR_FILTER.accept(file)){
				Report report = target.deployConnector(url);
				log("Resource adapter", url, report);
				return report.isSuccess();
			} else if (EAR_FILTER.accept(file)){
				Report[] reports = target.deployApplication(
					url, 
					new File(
						this.stagingDir,
						EAR_FILTER.getName(file)
					)
				);
				log("Enterprise application", url, reports);
				return reports[0].isSuccess();
			} else {
				return false;
			}
		} catch (Exception exception) {
			getLogger().error(
				"Start of '" + file + "' failed",
				exception
			);
			return false;
		}
	}
	
    /**
     * Log a given deployment
     * 
     * @param type deployment unit type
     * @param url the deployment unit's url
     * @param report the deployment report
     */
    private void log(
        String type,
        URL url,
        Report... reports
    ){
    	Log log = getLogger();
        if(reports[0].isSuccess()) {
            if(log.isInfoEnabled()) {
                log.info(type + " '" + url + "' was successfully deployed");
            }
        } else {
            if(log.isErrorEnabled()) {
                log.error(type + " '" + url + "' could not be doployed successfully");
            }
        }
        for(Report report : reports) {
            if(report.isSuccess()) {
                if(report.hasWarning()) {
                    if(log.isWarnEnabled()) {
                        log.warn(report);
                    }
                } else {
                    if(log.isDebugEnabled()) {
                        log.debug(report);
                    }
                }
            } else {
                if(log.isErrorEnabled()) {
                    log.error(report); 
                }
            }
        }
    }
    
    static SuffixFilter RAR_FILTER = new SuffixFilter("rar");
    static SuffixFilter EAR_FILTER = new SuffixFilter("ear");
    static SuffixFilter XML_FILTER = new SuffixFilter("xml");
    static TypeFilter DIRECTORY_FILTER = new TypeFilter(true);
    
	/**
	 * Enterprise Archive Filter
	 */
	static final class SuffixFilter implements FilenameFilter {

		/**
		 * Constructor
		 * 
		 * @param extension
		 */
		SuffixFilter(
			String extension
		) {
			this.suffix = "." + extension; 
		}
		
		/**
		 * 
		 */
		private final String suffix;
		
		/* (non-Javadoc)
		 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
		 */
		public boolean accept(
			File dir, 
			String name
		) {
			return name.toLowerCase().endsWith(this.suffix);
		}		

		boolean accept(
			File file
		){
			return accept(null, file.getName());
		}
		
		String getSuffix(){
			return this.suffix;
		}
		
		String getName(
			File file
		){
			if(accept(file)) {
				String name = file.getName();
				return name.substring(
					0, 
					name.length() - this.suffix.length()
				).replace(
					'/',
					'#'
				);
			} else {
				return null;
			}
		}
		
		String getPath(
			File file
		){
			if(accept(file)) {
				String name = file.getName();
				return '/' + name.substring(
					0, 
					name.length() - this.suffix.length()
				).replace(
					'#',
					'/'
				);
			} else {
				return null;
			}
		}
		
		File getFile(
			File directory,
			String name
		){
			return new File(
				directory,
				name + this.suffix
			);
		}
		
	}

	/**
	 * Directory Filter
	 */
	static class TypeFilter implements FileFilter {

		TypeFilter(
			boolean directory
		){
			this.directory = directory;
		}
		
		private final boolean directory;
		
		public boolean accept(File pathname) {
			return directory ? pathname.isDirectory() : pathname.isFile();
		}
		
	}
	
}

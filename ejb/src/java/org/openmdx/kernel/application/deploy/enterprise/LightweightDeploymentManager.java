/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LightweightDeploymentManager.java,v 1.7 2011/06/21 22:55:06 hburger Exp $
 * Description: Lightweight Deployment Manager
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/06/21 22:55:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.deploy.enterprise;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;

import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.DConfigBeanVersionType;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.DConfigBeanVersionUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.exceptions.TargetException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;

import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.application.deploy.spi.Report;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.url.URLInputStream;

/**
 * Lightweight Deployment Manager
 */
public class LightweightDeploymentManager implements DeploymentManager {

	/**
	 * Constructor
	 * 
	 * @param mode 
	 */
	LightweightDeploymentManager(
		LightweightContainer.Mode mode
	) {
		this.locale = null;
		this.mode = mode;
		this.targets = new Target[]{new Type()};
	}

	private Locale locale;

	final LightweightContainer.Mode mode;
	
	private final Target[] targets; 
		
	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#createConfiguration(javax.enterprise.deploy.model.DeployableObject)
	 */
	public DeploymentConfiguration createConfiguration(
		DeployableObject obj
	) throws InvalidModuleException {
		/// Nothing done yet
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], java.io.File, java.io.File)
	 */
	public ProgressObject distribute(
		Target[] targetList, 
		File moduleArchive,
		File deploymentPlan
	) throws IllegalStateException {
		try {
			URL moduleURL = moduleArchive.toURI().toURL();
			ModuleType type = getType(moduleArchive);
			return distribute(
				targetList,
				type,
				moduleURL,
				null // deploymentPlan not yet supported
			);
		} catch (MalformedURLException exception) {
			throw new UnsupportedOperationException(
				"The module archive can't be reference as URL",
				exception
			);
		}
	}

	/**
	 * Derives the module type from the archive file's ending
	 * 
	 * @param archiveFile
	 * 
	 * @return the archive's module type
	 */
	protected ModuleType getType(
		File archiveFile
	){
		String archiveName = archiveFile.getName().toLowerCase();
		for(int i = 0; i < 5; i++) {
			ModuleType candidate = ModuleType.getModuleType(i);
			if(archiveName.endsWith(candidate.getModuleExtension())) {
				return candidate;
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], java.io.InputStream, java.io.InputStream)
	 */
	public ProgressObject distribute(
		Target[] targetList,
		InputStream moduleArchive,
		InputStream deploymentPlan
	) throws IllegalStateException {
		throw new UnsupportedOperationException(
			"This deprecated method is not supported"
		);
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], javax.enterprise.deploy.shared.ModuleType, java.io.InputStream, java.io.InputStream)
	 */
	public ProgressObject distribute(
		final Target[] targetList, 
		final ModuleType type,
		final InputStream moduleArchive,
		final InputStream deploymentPlan
	) throws IllegalStateException {
		if(moduleArchive instanceof URLInputStream) {
			URL moduleURL = ((URLInputStream)moduleArchive).getURL(); 
			return distribute(
				targetList,
				type,
				moduleURL,
				null // deploymentPlan not yet supported
			);
		} else {
			throw new UnsupportedOperationException(
				"Upload not yet supported by, the module archive should be of type " + 
				URLInputStream.class.getName() + " instead of "+ 
				moduleArchive.getClass().getName()
			);
		}
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#distribute(javax.enterprise.deploy.spi.Target[], javax.enterprise.deploy.shared.ModuleType, java.io.InputStream, java.io.InputStream)
	 */
	protected ProgressObject distribute(
		final Target[] targetList, 
		final ModuleType type,
		final URL moduleArchive,
		final URL deploymentPlan
	) throws IllegalStateException {
		TargetModuleID[] targetModuleIDs = new TargetModuleID[targetList.length];
		for(int i = 0; i < targetModuleIDs.length; i++){
			targetModuleIDs[i] = new Unit(
				targetList[i],
				type,
				moduleArchive
			);
		}
		return new Progress(
			ActionType.EXECUTE,
			CommandType.DISTRIBUTE,
			StateType.COMPLETED,
			targetModuleIDs
		);
	}
	
	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#getAvailableModules(javax.enterprise.deploy.shared.ModuleType, javax.enterprise.deploy.spi.Target[])
	 */
	public TargetModuleID[] getAvailableModules(
		ModuleType moduleType,
		Target[] targetList
	) throws TargetException, IllegalStateException {
		/// Nothing done yet
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#getCurrentLocale()
	 */
	public Locale getCurrentLocale() {
		return this.locale == null ? getDefaultLocale() : this.locale;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#getDConfigBeanVersion()
	 */
	public DConfigBeanVersionType getDConfigBeanVersion(
	) {
		/// Nothing done yet
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#getDefaultLocale()
	 */
	public Locale getDefaultLocale() {
		return Locale.getDefault();
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#getNonRunningModules(javax.enterprise.deploy.shared.ModuleType, javax.enterprise.deploy.spi.Target[])
	 */
	public TargetModuleID[] getNonRunningModules(
		ModuleType moduleType,
		Target[] targetList
	) throws TargetException, IllegalStateException {
		/// Nothing done yet
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#getRunningModules(javax.enterprise.deploy.shared.ModuleType, javax.enterprise.deploy.spi.Target[])
	 */
	public TargetModuleID[] getRunningModules(
		ModuleType moduleType,
		Target[] targetList
	) throws TargetException, IllegalStateException {
		/// Nothing done yet
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#getSupportedLocales()
	 */
	public Locale[] getSupportedLocales() {
		return Locale.getAvailableLocales();
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#getTargets()
	 */
	public Target[] getTargets() throws IllegalStateException {
		return this.targets;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#isDConfigBeanVersionSupported(javax.enterprise.deploy.shared.DConfigBeanVersionType)
	 */
	public boolean isDConfigBeanVersionSupported(DConfigBeanVersionType version) {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#isLocaleSupported(java.util.Locale)
	 */
	public boolean isLocaleSupported(Locale locale) {
		for(Locale candidate : Locale.getAvailableLocales()) {
			if(candidate.equals(locale)) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#isRedeploySupported()
	 */
	public boolean isRedeploySupported() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#redeploy(javax.enterprise.deploy.spi.TargetModuleID[], java.io.File, java.io.File)
	 */
	public ProgressObject redeploy(
		TargetModuleID[] moduleIDList,
		File moduleArchive, 
		File deploymentPlan
	) throws UnsupportedOperationException, IllegalStateException {
		/// Nothing done yet
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#redeploy(javax.enterprise.deploy.spi.TargetModuleID[], java.io.InputStream, java.io.InputStream)
	 */
	public ProgressObject redeploy(
		TargetModuleID[] moduleIDList,
		InputStream moduleArchive, 
		InputStream deploymentPlan
	) throws UnsupportedOperationException, IllegalStateException {
		/// Nothing done yet
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#release()
	 */
	public void release() {
		/// Nothing done yet
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#setDConfigBeanVersion(javax.enterprise.deploy.shared.DConfigBeanVersionType)
	 */
	public void setDConfigBeanVersion(
		DConfigBeanVersionType version
	) throws DConfigBeanVersionUnsupportedException {
		/// Nothing done yet
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#setLocale(java.util.Locale)
	 */
	public void setLocale(
		Locale locale
	) throws UnsupportedOperationException {
		this.locale = locale;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#start(javax.enterprise.deploy.spi.TargetModuleID[])
	 */
	public ProgressObject start(
		TargetModuleID[] moduleIDList
	) throws IllegalStateException {
		Type target = null;
		SortedMap<Integer,URL> applications = new TreeMap<Integer,URL>();
		SortedMap<Integer,URL> resourceAdapters = new TreeMap<Integer,URL>();
		for(int i = 0; i < moduleIDList.length; i++) {
		    if( moduleIDList[i] instanceof Unit) {
		        Unit targetModuleID = (Unit)moduleIDList[i];
    			if(target == null) {
    				Target candidate = targetModuleID.getTarget();
    				if(candidate instanceof Type) {
    					target = (Type) candidate;
    				} else throw new IllegalArgumentException(
    					"Unsuppoarted target: " + target	
    				);
    			} else if(!target.equals(targetModuleID.getTarget())) throw new IllegalArgumentException(
                    "Deployment to different targets at once is not supported"
                );
				ModuleType type = targetModuleID.getType(); 
				URL url = targetModuleID.getURL();
				if(ModuleType.EAR == type) {
					applications.put(i,url);
				} else if (ModuleType.RAR == type) {
					resourceAdapters.put(i,url);
				} else throw new IllegalArgumentException(
					"Unsupported lightweight target module id type:" + type
				);
			} else throw new IllegalArgumentException(
				"Unsupported lightweight target module id: " + moduleIDList[i]
			);
		}
		if(target == null) {
		    return new Progress(
                ActionType.EXECUTE,
                CommandType.START,
                StateType.COMPLETED,
                new TargetModuleID[]{}
            );
		}
		switch(target.getMode()) {
			case ENTERPRISE_APPLICATION_CONTAINER: try {
				List<TargetModuleID> completed = new ArrayList<TargetModuleID>();
				LightweightContainer lightweightContainer = LightweightContainer.getInstance(
					LightweightContainer.Mode.ENTERPRISE_APPLICATION_CONTAINER
				);
				for(Map.Entry<Integer,URL> resourceAdapter : resourceAdapters.entrySet()){
					Report report = lightweightContainer.deployConnector(resourceAdapter.getValue());
					Object[] logParameters = new Object[]{resourceAdapter.getKey(),report};
					if(report.isSuccess()){
						completed.add(moduleIDList[resourceAdapter.getKey()]);
						SysLog.log(
							Level.INFO,
							"Deployment of resource adapter {0} succeeded: {1}",
							logParameters
						);
					} else {
						SysLog.log(
							Level.INFO,
							"Deployment of resource adapter {0} failed: {1}",
							logParameters
						);
					}
				}
				for(Map.Entry<Integer,URL> application : applications.entrySet()){
					Report[] reports = lightweightContainer.deployApplication(application.getValue());
					Object[] logParameters = new Object[]{application.getKey(),reports[0]}; 
					if(reports[0].isSuccess()){
						SysLog.log(
							Level.INFO,
							"Deployment of enterprise application {0} succeeded: {1}",
							logParameters
							
						);
					} else {
						SysLog.log(
							Level.WARNING,
							"Deployment of enterprise application {0} failed: {1}",
							logParameters
						);
					}
					for(int i = 1; i < reports.length; i++) {
						SysLog.log(Level.INFO,"Module {0}: {1}", new Object[]{i, reports[i]});
					}
				}
				return new Progress(
					ActionType.EXECUTE,
					CommandType.START,
					StateType.COMPLETED,
					completed.toArray(new TargetModuleID[completed.size()])							
				);
			} catch (Exception exception) {
                SysLog.log(
                    Level.WARNING,
                    "Could not start target",
                    BasicException.newStandAloneExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ACTIVATION_FAILURE,
                        "Could not start target",
                        new BasicException.Parameter("name", target.getName()),
                        new BasicException.Parameter("description", target.getDescription()),
                        new BasicException.Parameter("mode", target.getMode())
                    )
                );
				return new Progress(
					ActionType.EXECUTE,
					CommandType.START,
					StateType.FAILED,
					new TargetModuleID[]{}
				);
			}
			default: throw new IllegalArgumentException(
				"Unsupported target mode: " + target.getMode()
			);
				
		}
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#stop(javax.enterprise.deploy.spi.TargetModuleID[])
	 */
	public ProgressObject stop(
		TargetModuleID[] moduleIDList
	) throws IllegalStateException {
		/// Nothing done yet
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.enterprise.deploy.spi.DeploymentManager#undeploy(javax.enterprise.deploy.spi.TargetModuleID[])
	 */
	public ProgressObject undeploy(
		TargetModuleID[] moduleIDList
	) throws IllegalStateException {
		/// Nothing done yet
		return null;
	}
	
	
	//------------------------------------------------------------------------
	// Class Type
	//------------------------------------------------------------------------
	
	/**
	 * Lightweight Target
	 */
	class Type implements Target {

		LightweightContainer.Mode getMode(){
			return LightweightDeploymentManager.this.mode;
		}
		
		public String getDescription() {
			switch(getMode()) {
				case ENTERPRISE_APPLICATION_CONTAINER:
					//
					// Provides Initial Context Factory for local JNDI access
					//
					return "In-process Lightweight Container";
				case ENTERPRISE_JAVA_BEAN_SERVER:
					//
					// Provides Initial Context Factory for remote JNDI access
					//
					return LightweightContainer.newProviderURL();
				default:
					return toString();
			}
		}

		public String getName() {
			return getMode().name();
		}

	}

	//------------------------------------------------------------------------
	// Class Status
	//------------------------------------------------------------------------
	
	/**
	 * Lightweight Deployment Status
	 */
	static class Status implements DeploymentStatus {

		Status(
			ActionType action,
			CommandType command,
			StateType initialState
		){
			this.action = action;
			this.command = command;
			this.state = initialState;
		}

		private final ActionType action;
		
		private final CommandType command;
		
		private StateType state;
		
		public ActionType getAction() {
			return this.action;
		}

		public CommandType getCommand() {
			return this.command;
		}

		public String getMessage() {
			return this.action + " " + this.command + " (" + this.state + ")";
		}

		public StateType getState() {
			return this.state;
		}

		void setState(
			StateType state
		){
			this.state = state;
		}
		
		public boolean isCompleted() {
			return StateType.COMPLETED == this.state;
		}

		public boolean isFailed() {
			return StateType.FAILED == this.state;
		}

		public boolean isRunning() {
			return StateType.RUNNING == this.state;
		}

	}

	
	//------------------------------------------------------------------------
	// Class Progress
	//------------------------------------------------------------------------
	
	/**
	 * Lightweight Progress Object
	 */
	static class Progress implements ProgressObject {
		
		/**
		 * Constructor
		 * 
		 * @param action
		 * @param command
		 * @param initialState
		 */
		Progress(
			ActionType action,
			CommandType command,
			StateType initialState,
			TargetModuleID[] targetModuleIDs
		){
			this.status = new Status(
				action,
				command,
				initialState
			);
			this.targetModuleIDs = targetModuleIDs;
		}

		final private DeploymentStatus status;

		final private TargetModuleID[] targetModuleIDs;
		
		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#addProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
		 */
		public void addProgressListener(ProgressListener pol) {
			// Nothing done yet
		}

		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#cancel()
		 */
		public void cancel() throws OperationUnsupportedException {
			// Nothing done yet
		}

		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#getClientConfiguration(javax.enterprise.deploy.spi.TargetModuleID)
		 */
		public ClientConfiguration getClientConfiguration(TargetModuleID id) {
			// Nothing done yet
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#getDeploymentStatus()
		 */
		public DeploymentStatus getDeploymentStatus() {
			return this.status;
		}

		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#getResultTargetModuleIDs()
		 */
		public TargetModuleID[] getResultTargetModuleIDs() {
			return this.targetModuleIDs;
		}

		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#isCancelSupported()
		 */
		public boolean isCancelSupported() {
			return true;
		}

		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#isStopSupported()
		 */
		public boolean isStopSupported() {
			return false;
		}

		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#removeProgressListener(javax.enterprise.deploy.spi.status.ProgressListener)
		 */
		public void removeProgressListener(ProgressListener pol) {
			// Nothing done yet
		}

		/* (non-Javadoc)
		 * @see javax.enterprise.deploy.spi.status.ProgressObject#stop()
		 */
		public void stop() throws OperationUnsupportedException {
			// Nothing done yet
		}

	}

	
	//------------------------------------------------------------------------
	// Class Unit
	//------------------------------------------------------------------------
	
	/**
	 * 
	 */
	static class Unit implements TargetModuleID {

		/**
		 * Constructor
		 * 
		 * @param target
		 * @param url
		 */
		Unit(
			Target target,
			ModuleType type,
			URL url
		){
			this.target = target;
			this.type = type;
			this.url = url;
		}
		
		private final Target target;
		
		private final ModuleType type;
		
		private final URL url;
		
		
		public TargetModuleID[] getChildTargetModuleID(
		) {
			// Nothing done yet
			return null;
		}

		public String getModuleID() {
			return this.url.toString();
		}

		public TargetModuleID getParentTargetModuleID(
		) {
			// Nothing done yet
			return null;
		}

		public Target getTarget(
		) {
			return this.target;
		}

		public String getWebURL(
		) {
			return null;
		}

		URL getURL(){
			return this.url;
		}
		
		ModuleType getType(){
			return this.type;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.url + " (" + this.type + ")";
		}
		
	}

}

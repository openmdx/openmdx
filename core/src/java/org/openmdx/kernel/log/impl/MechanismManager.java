/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: MechanismManager.java,v 1.6 2008/02/05 13:41:39 hburger Exp $
 * Description: Manageable
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/05 13:41:39 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.log.impl;


import java.util.ArrayList;
import java.util.Arrays;

import org.openmdx.kernel.log.slf4j.SLF4JLoggingMechanism;

public class MechanismManager {


	public MechanismManager(
		Log  aLogger)
	{
		this.logger = aLogger;
	}
	
	
	/**
	 * Returns a list with the currently active mechanisms. This list MUST be
	 * considered as read-only. Due to J# compatibility problems it's not
	 * possible to return a read-only collection.
	 * 
	 * @return An array of mechanims (never returns a null array)
	 */
	public AbstractLoggingMechanism[] getMechanisms()
	{
		return this.liveMechanisms;
	}


	/**
	 * Checks if a mechanims given by its name is active.
	 * 
	 * @return true if the mechanism is active
	 */
	public boolean isMechanismActive(String mechName)
	{
		if (mechName == null) {
			return false;
		}
		
		AbstractLoggingMechanism[] mechs = getMechanisms();
		
		for(int ii=0; ii<mechs.length; ii++) {
			if (mechName.equals(mechs[ii].getName())) {
				return true;
			}
		}
		
		return false;
	}
	

	/**
	 * Checks if a mechanism is configured for this logger 
	 *
	 * @param mechName a mechanism name
	 * @return  true if configured
	 */
	public boolean isMechanismConfigured(
		String  mechName)
	{
		String[] configuredMechanisms = 
			this.logger.getLogProperties().getLoggingMechanismNames(
				this.logger.getName());


		// For all attached pluggable mechanism
		for(int ii=0; ii<configuredMechanisms.length; ii++) {
			// Has the mechanism been configured?
			if (configuredMechanisms[ii].equals(mechName)) {
				return true;
			}
		}

		return false;
	}

	
	/**
	 * Adds a Logging Mechanism to the list of mechanisms..
	 *
	 * @param name The name of the Mechanism.
	 */
    public void addMechanisms(
		String[] mechs)
	{
		for(int ii=0; ii<mechs.length; ii++){
			addMechanism(mechs[ii]);
		}
	}

	
	/**
	 * Adds a Logging Mechanism to the list of mechanisms..
	 *
	 * @param name The name of the Mechanism.
	 */
    public void addMechanism(
		String mechanismName
    ) {
		AbstractLoggingMechanism newLoggingMechanism = 
            "SLF4JLoggingMechanism".equals(mechanismName) ? SLF4JLoggingMechanism.getInstance() :
            "StandardErrLoggingMechanism".equals(mechanismName) ? StandardErrLoggingMechanism.getInstance() :
            "SharedFileLoggingMechanism".equals(mechanismName) ? SharedFileLoggingMechanism.getInstance() :
            "SharedDatedFileLoggingMechanism".equals(mechanismName) ? SharedDatedFileLoggingMechanism.getInstance() :
            "UniqueFileLoggingMechanism".equals(mechanismName) ? UniqueFileLoggingMechanism.getInstance() :
            "UniqueDatedFileLoggingMechanism".equals(mechanismName) ? UniqueDatedFileLoggingMechanism.getInstance() :
            "SharedFileStatisticsMechanism".equals(mechanismName) ? SharedFileStatisticsMechanism.getInstance() :
            "SharedDatedFileStatisticsMechanism".equals(mechanismName) ? SharedDatedFileStatisticsMechanism.getInstance() :
            "SocketLoggingMechanism".equals(mechanismName) ? SocketLoggingMechanism.getInstance() : null;
        if(newLoggingMechanism == null) {
			LogLog.warning(
				this.getClass(), 
				"addMechanism",
				"The mechanism '" + mechanismName + "' is not known to the"
				+ " logger " + this.logger.getName(), 
				"");
		} else {
        	addMechanism(newLoggingMechanism);
        }
	}

	
	/**
	 * Adds a Logging Mechanism to the list of mechanisms that this
	 * log writes to.  Opens the mechanism and registers it also.
	 *
	 * @param newMech The name of the Logging Mechanism.
	 */
	synchronized
	public void addMechanism(
		AbstractLoggingMechanism newMech)
	{
		if (newMech == null) return;
		
		if (!isMechanismConfigured(newMech.getName())) {
			LogLog.trace(
				this.getClass(), 
				"addMechanism",
				"The mechanism '" + newMech.getName() + "' is not configured"
				+ " with the logger " + this.logger.getName() + " ->"
				+ " skipping it", 
				"");
			return;
		}
		
		// Try to find a duplicate. If found log an error and return
		if (getShadowMechanism(newMech.getName()) != null) {
			LogLog.error(
				this.getClass(), 
				"addMechanism",
				"Tried to add the already added mechanism " 
				+ newMech.getName() + " to the logger " + this.logger.getName(), 
				"");
			return;
		}


		this.shadowMechanisms.add(newMech);

		updateLiveLoggingMechanismList();

		fireAddMechanismEvent(newMech);
	}


	/**
	 * Removes an active Logging Mechanism from the list of mechanisms that this
	 * log writes to. Closes the mechanism and deregisters it also.
	 *
	 * @param mechName The name of the Logging Mechanism to remove.
	 *
	 */
	synchronized
	public void removeMechanism(
		String mechName)
	{
		AbstractLoggingMechanism mech = getShadowMechanism(mechName);

		if (mech == null) {
			// Mechanism is not or no more registered with this logger
			return;
		}

		this.shadowMechanisms.remove(mech);
		
		updateLiveLoggingMechanismList();

		fireRemoveMechanismEvent(mech);
	}


	/**
	 * Removes all Logging Mechanisms from the list of mechanisms that this
	 * log writes to.  Closes the mechanisms and deregisters them also.
	 */
	synchronized
	public void removeAllMechanisms()
	{
		AbstractLoggingMechanism mech = null;

		// Do not use an iterator => J#
		for(int ii=0; ii<this.shadowMechanisms.size(); ii++) {
			mech = this.liveMechanisms[ii];

			fireRemoveMechanismEvent(mech);		
		}

		this.shadowMechanisms.clear();

		updateLiveLoggingMechanismList();
	}


	/**
	 * Register a listener
	 * 
	 * @param listener
	 */
	synchronized
	public void register(
		MechanismManagerListener  listener)
	{
		this.listeners.add(listener);
	}


	public String toString(
    ){
        return "live=" + Arrays.asList(getMechanisms());
	}


	/**
	 * Fire the 'mechanismAdded' event for all registered listeners
	 * 
	 * @param mech
	 */
	synchronized
	private void fireAddMechanismEvent(
		AbstractLoggingMechanism mech)
	{
		// Do not use an iterator => J#
		for(int ii=0; ii<this.listeners.size(); ii++) {
			this.listeners.get(ii).mechanismAddedEvent(mech);
		}
	}

	/**
	 * Fire the 'mechanismRemoved' event for all registered listeners
	 * 
	 * @param mech
	 */
	synchronized
	private void fireRemoveMechanismEvent(
		AbstractLoggingMechanism mech)
	{
		// Do not use an iterator => J#
		for(int ii=0; ii<this.listeners.size(); ii++) {
			this.listeners.get(ii).mechanismRemovedEvent(mech);
		}
	}


	/**
	 * Update the live logging mechanism list.
	 */
	synchronized
	private void updateLiveLoggingMechanismList()
	{
		this.liveMechanisms = this.shadowMechanisms.toArray(
			new AbstractLoggingMechanism[shadowMechanisms.size()]
		);
	}


	/**
	 * Returns the named logging mechanism
	 *
	 * @param mechName  Name of the logging mechanism to search for..
	 *
	 * @return The concrete instance of an <code>AbstractLoggingMechanism</code>
	 *          or null if not found.
	 */
	synchronized 
	private AbstractLoggingMechanism getShadowMechanism(
		String mechName)
	{
		AbstractLoggingMechanism currMech = null;

		for(int ii=0; ii<this.shadowMechanisms.size(); ii++) {
			currMech = this.liveMechanisms[ii];
			if (currMech.getName().equals(mechName)) {
				return currMech;
			}
		}

		// Not found -> return null
		return null;
	}





	/** The logger we belong to */
	private final Log  logger;
	

	/**
	 * The mechanism shadow list is synchronized and used for modification.
	 * The shadow list is 'copied' to the live list.
	 */
	private ArrayList<AbstractLoggingMechanism> shadowMechanisms = new ArrayList<AbstractLoggingMechanism>();


	/** 
	 * The mechanism live list is non synchronized list for fast read only 
	 * access. 
	 * The live list is updated from the synchronized shadow list. No
	 * modifications are allowed on the live list, except a complete exchange
	 * of the list!
	 */
	private AbstractLoggingMechanism[] liveMechanisms = 
												new AbstractLoggingMechanism[0];


	/** 
	 * A list of registered listeners
	 */
	private ArrayList<MechanismManagerListener>  listeners = new ArrayList<MechanismManagerListener>();
	
}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JDOTask.java,v 1.1 2007/04/24 16:41:50 hburger Exp $
 * Description: Abstract JDO Task
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/04/24 16:41:50 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.tools.ant.taskdefs;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Abstract JDO Task
 */
public abstract class JDOTask extends Fork {

	/**
	 * Construtor
	 */
	JDOTask(
	) {
	}

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.taskdefs.Java#execute()
	 */
	public void execute() throws BuildException {
		String className = getClassName();
		if(className == null) {
			throw new BuildException(
				getClass().getName() + " has no class specified for vendor " + this.jdoVendor,
				getLocation()
			);
		} else {
			super.setClassname(className);
		}
		if(this.verbose) {
			super.createArg().setValue("-v");
		}
		super.execute();
	}

	/**
	 * Set the JDO vendor the task delegate to
	 * 
	 * @param jdoVendor
	 */
	public void setJdovendor(
		JDOVendor jdoVendor
	){
		this.jdoVendor = jdoVendor; 
	}
	
	/**
	 * @return the jdoVendor
	 */
	protected JDOVendor getJdovendor() {
		return this.jdoVendor;
	}

    /**
     * set verbose
     * 
     * @param verbose Whether to give verbose output
     */
    public void setVerbose(
    	boolean verbose
    ){
    	this.verbose = verbose;
        if (verbose) {
            log("JDO Task verbose: " + verbose, Project.MSG_VERBOSE);
        }
    }

    /**
	 * Provide the class name
	 * 
	 * @return the vendor specific class name
	 */
	protected abstract String getClassName();
	
	/** 
	 * The JDO vendor ot delegate to
	 */
	private JDOVendor jdoVendor = new JDOVendor();
	
	/**
	 * Defines whether the <code>-v</code> flag is switched on or off
	 */
	private boolean verbose = false;

}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Database.java,v 1.1 2007/04/24 16:41:50 hburger Exp $
 * Description: Enhance Task
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

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;

/**
 * Schema Task
 */
public class Database extends JDOTask {

	/**
	 * Construtor
	 */
	public Database(
	) {
	}

	/* (non-Javadoc)
	 * @see org.openmdx.tools.ant.taskdefs.Spawn#executeJava()
	 */
	public int executeJava() throws BuildException {
		String command = this.command.getValue(getJdovendor());
		if(command == null) {
			throw new BuildException(
				getClass().getName() + " lacks vendor sepcific command",
				getLocation()
			);
		} else {
			super.createArg().setValue(command);
		}
		return super.executeJava();
	}


	/* (non-Javadoc)
	 * @see org.openmdx.tools.ant.taskdefs.JDOTask#getClassName()
	 */
	protected String getClassName() {
		return (String) schemaTool.get(getJdovendor().getValue());
	}

	/**
	 * Set the databse command
	 * 
	 * @param command
	 */
	public void setCommand(
		DatabaseCommand command
	){
		this.command = command;
	}
	
	/**
	 * The databse command to be executed
	 */
	private DatabaseCommand command = new DatabaseCommand();
	
	/**
	 * The schema tool classes
	 */
	private static final Map schemaTool = new HashMap();
	
	static {
		schemaTool.put(JDOVendor.JPOX, "org.jpox.SchemaTool");
	}

}
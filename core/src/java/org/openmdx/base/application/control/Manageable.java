/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Manageable.java,v 1.5 2008/03/21 18:28:07 hburger Exp $
 * Description: A manageable application
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:28:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.base.application.control;


import java.util.ArrayList;
import java.util.List;




/**
 * Manageables are similiar to applications. But they not have a run method and
 * may not implement the EventListener or CmdLineLineListener interface.
 */
public abstract class Manageable 
{
	/**
	 * Initializes the manageable. May be overloaded by a concrete manageable
	 * If init() fails, init() has to cleanup all it's allocated resources.
	 * 
	 * <p>Manageables are initialized in the order they have been registered
	 * with application controller.
	 *
	 * @return    true if successful
	 */
	protected abstract void init()
		throws Exception;


	/**
	 * The default manageable release. May be overloaded
	 * by a concrete manageable
	 * 
	 * <p>Manageables are released in the reverse order they have been registered
	 * with application controller.
	 *
	 * release() is called only if init() was successful.
	 */
	protected abstract void release()
		throws Exception;


	/**
	 * Requests the command line options specification. A manageable overrides
	 * this method to provide command line options.
	 *
	 * @return List. A list of cmd line options (objects of class CmdLineOption)
	 */
	@SuppressWarnings("unchecked")
    public List getCmdLineOptions()
	{
		return new ArrayList();
	}


	/**
	 * Returns the command line arguments as processed by the command line
	 * processor
	 * 
	 * @return CmdLineArgs
	 */
	public CmdLineArgs getCmdLineArgs()
	{
		return getController().getCmdLineArgs();
	}


	/**
	 * Returns the command line arguments as received from the VM's main
	 * 
	 * @return String[]
	 */
	public String[] getRawArgs()
	{
		return getController().getRawArgs();
	}

	
	/**
	 * Returns the associated controller. The application object must be 
	 * registered with controller to have a controller object.
	 * 
	 * @return ApplicationController_1_0
	 */
	public ApplicationController getController()
	{
		return this.controller;
	}

	/**
	 * Sets a controller. This is done while registering the Application with
	 * the application controller.
	 * 
	 * @param controller
	 */
	public void setController(ApplicationController controller)
	{
		this.controller = controller;
	}

	/** The controller with which the application is registered */
	private ApplicationController  controller;
	
}

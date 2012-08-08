/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Application.java,v 1.1 2009/01/13 23:51:09 wfro Exp $
 * Description: Base Application
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 23:51:09 $
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
package org.openmdx.application.shell;


import java.util.ArrayList;
import java.util.List;

import org.openmdx.kernel.log.SysLog;



/**
 * The application class represents an executable Java application.
 *
 *
 * <p><bold>Simple application</bold>
 * <p>The most simple application looks like:
 *
 * <pre>
 *      import org.openmdx.base.application.Application;
 *      import org.openmdx.base.application.ApplicationController_1_0;
 *
 *      public class SimpApp extends Application
 *      {
 *        public SimpApp()
 *        {
 *          super("SimpApp", "1.0", "Simple App", null);
 *        }
 *
 *        protected void run()
 *          throws Exception
 *        {
 *          System.out.println("Hello World");
 *        }
 *
 *        public static void main(String args[])
 *        {
 *          ApplicationController_1_0  controller;
 *
 *          controller = new ApplicationController_1_0(args);
 *
 *          controller.registerApplication(new SimpApp());
 *          controller.run();
 *        }
 *      }
 * </pre>
 *
 *
 * <p><bold>Elaborated application</bold>
 * <p>A full featured application using
 * <ul>
 *   <li>  Application name and version
 *   <li>  Command line parameters
 *   <li>  Help text
 *   <li>  Logging initialisation
 *   <li>  Exception handler
 *   <li>  CommandLine event handler
 *   <li>  Exit codes
 * </ul>
 * Both handlers shown here are similiar to the default handlers.
 * <p>
 *
 * <pre>
 *      import org.openmdx.base.application.CmdLineOption;
 *      import org.openmdx.base.application.Application;
 *      import org.openmdx.base.application.ApplicationController_1_0;
 *      import org.openmdx.base.application.ExceptionEvent;
 *      import org.openmdx.base.application.ExceptionListener;
 *      import org.openmdx.base.log.generic.AppLog;
 *      import org.openmdx.base.exception.StackedException;
 *      import myPackage.MyException;
 *
 *
 *      public class SimpApp extends Application
 *        implements ExceptionListener
 *      {
 *        public SimpApp()
 *        {
 *          super("SimpApp", "1.0",
 *                "A sample to demonstrate the Application features",
 *                createCmdLineOptions());
 *        }
 *
 *
 *        protected void init()
 *          throws Exception
 *        {
 *          this.msg   = getCmdLineArgs().getFirstValue("msg"));
 *          this.print = getCmdLineArgs().hasArg("print"));
 *        }
 *
 *        protected void run()
 *          throws Exception
 *        {
 *          if (this.print) System.out.println(this.msg);
 *        }
 *
 *        protected void release()
 *          throws Exception
 *        {
 *          AppLog.trace("Done");
 *          setExitCode(1);
 *        }
 *
 *        // ExceptionEvent listener
 *        public void exception(ExceptionEvent e)
 *        {
 *          // handle your own exceptions, that org.openmdx.base is not
 *          // aware of
 *          Throwable  th = e.getException();
 *          if (ex instanceof StackedException) {
 *            AppLog.error("",((StackedException)ex).toString());
 *          }else if (ex instanceof MyException) {
 *            AppLog.error("",((MyException)ex).toString());
 *          }else{
 *            AppLog.error("", ex);
 *          }
 *        }
 *
 *        // CmdLineEvent listener
 *        public void cmdLineBadArgs(CmdLineEvent e)
 *        {
 *          System.out.println("Command line parsing failed!");
 *          System.out.println();
 *          System.out.println(e.getInfo());
 *          exit(Application.EXIT_CODE_FAILED);
 *        }
 *
 *        // CmdLineEvent listener
 *        public void cmdLineHelpRequest(CmdLineEvent e)
 *        {
 *          System.out.println(e.getInfo());
 *          exit(Application.EXIT_CODE_FAILED);
 *        }
 *
 *        // CmdLineEvent listener
 *        public void cmdLineVersionRequest(CmdLineEvent e)
 *        {
 *          System.out.println(e.getInfo());
 *          exit(Application.EXIT_CODE_OK);
 *        }
 *
 *        // CmdLineEvent listener
 *        public void cmdLineTrace(CmdLineEvent e)
 *        {
 *          System.out.println(e.getInfo());
 *        }
 *
 *        private static ArrayList createCmdLineOptions()
 *        {
 *          ArrayList  options = new ArrayList();
 *
 *          options.add(new CmdLineOption("msg"  , "A message to be printed", 1, 1));
 *          options.add(new CmdLineOption("print", "Enable message printing"));
 *          return options;
 *        }
 *
 *        public static void main(String args[])
 *        {
 *          ApplicationController_1_0  controller;
 *
 *          controller = new ApplicationController_1_0(args);
 *
 *          controller.initLogging("simpapp", "SimpApp");
 *          controller.registerApplication(new SimpApp());
 *          controller.run();
 *        }
 *
 *        private boolean print;
 *        private String  msg;
 *      }
 * </pre>
 */
@SuppressWarnings("unchecked")
public class Application
	extends Manageable
{


    /**
     * Creates an <code>Application</code> object. The application name
     * is the class name and the application version is set to 1.0
     *
     * <p><bold> NOTE:  NEVER PUT DIRECT OR INDIRECT APPLICATION LOGIC INTO THE
     * CONSTRUCTOR!</bold>
     */
    public Application()
    {
        this(null, "1.0", "No help available", null, null);


        if (Application.theApp != null) {
        	throw new RuntimeException(
				"An Application() object must only be instantiated once!");
        }

		Application.theApp = this;
    }


	/**
	 * Creates an <code>Application</code> object.
	 *
	 * <p>A note on help text:
	 *
	 * <p>Requests the application overview help text. Pass a short text that
	 * describes what the application is doing. The application name and the
	 * version are implicitely added.
	 *
	 * <p>Do not format the text using control characters as ('\r', '\n', ...)
	 * nor pass any command option description here. The Help text is
	 * appropriately formatted when outputted.
	 *
	 * <p><bold> NOTE:  NEVER PUT DIRECT OR INDIRECT APPLICATION LOGIC INTO THE
	 * CONSTRUCTOR!</bold>
	 *
	 *
	 * @param name  The application's name
	 * @param version The application's version
	 * @param helpText The application's help
	 * @param cmdLineOptions The application's command line options. A list of
	 * <code>CmdLineOption</code> objects
	 */
	public Application(
		String name,
		String version,
		String helpText,
		List   cmdLineOptions)
	{
		this(name, version, helpText, cmdLineOptions, null);
	}

    /**
     * Creates an <code>Application</code> object.
     *
     * <p>A note on help text:
     *
     * <p>Requests the application overview help text. Pass a short text that
	 * describes what the application is doing. The application name and the
	 * version are implicitely added.
	 *
	 * <p>Do not format the text using control characters as ('\r', '\n', ...)
	 * nor pass any command option description here. The Help text is
	 * appropriately formatted when outputted.
     *
     * <p><bold> NOTE:  NEVER PUT DIRECT OR INDIRECT APPLICATION LOGIC INTO THE
     * CONSTRUCTOR!</bold>
     *
     *
     * @param name  The application's name
     * @param version The application's version
     * @param helpText The application's help
     * @param cmdLineOptions The application's command line options. A list of
     * <code>CmdLineOption</code> objects
     * @param cmdLineFreeArgOption The application's command line free arg option
     */
    public Application(
    	String name,
    	String version,
    	String helpText,
    	List   cmdLineOptions,
		CmdLineFreeArgOption cmdLineFreeArgOption)
    {
        if ((name==null) || (name.length()==0)) {
			this.appName = this.getClass().getName();
        }else{
			this.appName = name;
        }

		this.appVersion = (version == null) ? "1.0" : version;

		this.appHelpText = (helpText == null) ? "No help available" : helpText;

		this.appCmdLineOptions = (cmdLineOptions == null) ? new ArrayList() : cmdLineOptions;

		this.appCmdLineFreeArgOptions = (cmdLineFreeArgOption == null) ?
										new CmdLineFreeArgOption("")
										:
										cmdLineFreeArgOption;
    }


	/**
	 * Returns the application's name
	 *
	 * @return String
	 */
	public final String getName()
	{
		return this.appName;
	}


	/**
	 * Returns the application's version
	 *
	 * @return String
	 */
	public final String getVersion()
	{
		return this.appVersion;
	}


	/**
	 * Returns the application's version
	 *
	 * @return String
	 */
	public final String getHelpText()
	{
		return this.appHelpText;
	}


	/**
	 * Requests the command line options specification
	 *
	 * @return    a list of cmd line options (objects of class CmdLineOption)
	 */
	public final List getCmdLineOptions()
	{
		return this.appCmdLineOptions;
	}


	/**
	 * Requests the command line free argument option specification
	 *
	 * @return    A string
	 */
	public final CmdLineFreeArgOption getCmdLineFreeArgOptions()
	{
		return this.appCmdLineFreeArgOptions;
	}


	/**
	 * Returns the application's exit code
	 */
	public final int getExitCode()
	{
		return this.m_exitCode;
	}


    /**
     * Terminates the currently running application.
     *
     * Uses the previously set exit code.
     *
     * This method calls the exit method in class System. This method never
     * returns normally.
     */
    public final void exit()
    {
        exit(m_exitCode);
    }


    /**
     * Terminates the currently running application.
     *
     * The argument serves as a status code; by convention, a nonzero status
     * code indicates abnormal termination.
     *
     * This method calls the exit method in class System. This method never
     * returns normally.
     */
    public final void exit(int exitCode)
    {
        //prevent switching back to OK
        setExitCode(exitCode);

        SysLog.info("Application exits with exit code " + m_exitCode);
        System.exit(m_exitCode);
    }


    /**
     * Checks if the application has failed. An application failure is
     * indicated by an exit code not equal to EXIT_CODE_OK. The exit code
     * can be set at any time during execution.
     */
    public final boolean hasFailed()
    {
        return (m_exitCode != Application.EXIT_CODE_OK);
    }


    /**
     * Returns the application object
     *
     * @return   the application object
     */
    public static Application getApp()
    {
    	return Application.theApp;
    }


    /**
     * Initializes the application. May be overloaded by a concrete application
     * If init() fails, init() has to cleanup all it's allocated resources.
     */
    protected void init()
        throws Exception
    {
        //
    }


    /**
     * The default application run. May be overloaded by a concrete application.
     * run() is called only if init() was successful.
     */
    protected void run()
        throws Exception
    {
        //
    }


    /**
     * The default application release. May be overloaded
     * by a concrete application
     *
     * release() is called only if init() was successful.
     */
    protected void release()
        throws Exception
    {
        //
    }


    /**
     * Sets the applications exit code. Once an exit code other than EXIT_CODE_OK
     * has been set, it not possible to switch back to EXIT_CODE_OK.
     *
     * The default exit code set is EXIT_CODE_OK.
     */
    protected final void setExitCode(int exitCode)
    {
        //prevent switching back to OK
        if (exitCode != Application.EXIT_CODE_OK) {
            m_exitCode = exitCode;
        }
    }


	/**
	 * Merges all command line options from <code>opst2</code> into
	 * <code>opts1<opts1>
	 *
	 * @param opts1
	 * @param opts2
	 * @return List  The merged option list
	 */
	protected static List mergeCmdLineOptions(List opts1, List opts2)
	{
		opts1.addAll(opts2);
		return opts1;
	}




    /** Predefined application exit codes */
    public final static int EXIT_CODE_OK     = 0;
    public final static int EXIT_CODE_FAILED = 255;


    /** The application's exit code */
    private int m_exitCode = EXIT_CODE_OK;

	/** The application's name */
	private final String appName;

	/** The application's name */
	private final String appHelpText;

    /** The application's version */
    private final String appVersion;

    /** The application's command line options */
    private final List appCmdLineOptions;

	/** The application's command line free arg options */
    private final CmdLineFreeArgOption appCmdLineFreeArgOptions;

    /** The one and only application */
    private static Application  theApp;

}

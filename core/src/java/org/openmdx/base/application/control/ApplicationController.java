/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ApplicationController.java,v 1.15 2008/03/21 18:27:19 hburger Exp $
 * Description: Base Application
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:27:19 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.application.control;


import java.beans.ExceptionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmdx.compatibility.base.application.control.LogConfiguration_1_0;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;


/**
 * ApplicationController
 */
@SuppressWarnings("unchecked")
public class ApplicationController
    implements CmdLineListener, ExceptionListener
{
    /**
     * Creates an <code>ApplicationController</code> object.
     *
     * @param args The command line arguments as obtained from the main()
     */
    public ApplicationController(String args[])
    {
        this.cmdLineProcessor = new CmdLineProcessor();
        this.manageables = new ArrayList();
        this.rawArgs = (args == null) ? new String[0] : args;
    }


    /**
     * Runs the controller.
     *
     * @param args The command line arguments as obtained from the main
     */
    public void run()
    {
        if (this.application == null) {
            throw new IllegalStateException(
                    "An 'Application_1_0' object must be registered with the"
                    + " application controller before the controller's run"
                    + " method may be called");
        }

        // Check if the command line arguments have been passed
        if (this.rawArgs == null) {
            try {
                Exception ex = new Exception(
                                     "The command line arguments must be set"
                                     + " prior to calling"
                                     + " ApplicationController.run()");

                handleExceptionEvent(ex);
            } catch(Throwable e) {
                // ignore
            }

            exit(Application.EXIT_CODE_FAILED);
        }


        boolean succeeded = false;

        // Handle all possible exceptions to prevent abnormal program
        // terminations due to uncaught exceptions.
        try {
            // Some command line options must be processed early
            processRawCmdLineOptions();

            // Log registered application
            SysLog.detail("Registered application: " + this.application.getClass().getName());

            // Log registered manageables
            Iterator iter = this.manageables.iterator();
            while(iter.hasNext()) {
                Manageable m = (Manageable)iter.next();
                SysLog.detail("Registered manageable: " + m.getClass().getName());
            }

            succeeded = execute();

        }catch (Exception ex) {
            handleExceptionEvent(ex);
        }

        if (!succeeded) {
            SysLog.error("Application failed");
            exit(Application.EXIT_CODE_FAILED);
        }
    }



    /**
     * Register an application with the controller.
     *
     * @param app The application
     */
    public void registerApplication(
            Application app)
    {
        this.application = app;
        app.setController(this);
    }


    /**
     * Register a manageable with the controller.
     *
     * @param app The application
     */
    public void registerManageable(
            Manageable manageable)
    {
        if (manageable instanceof Application) {
            throw new IllegalArgumentException(
                    "An object of type 'Application' must not be registered"
                    + " as 'Manageable'");
        }

        this.manageables.add(manageable);
    }


  /*
     * CmdLineEvent listener.
     *
   * @see org.openmdx.base.application.control.CmdLineListener#cmdLineBadArgs(org.openmdx.base.application.control.CmdLineEvent)
     */
    public void cmdLineBadArgs(CmdLineEvent e)
    {
        System.out.println("Command line parsing failed!");
        System.out.println();
        System.out.println(e.getInfo());
        System.exit(Application.EXIT_CODE_FAILED);
    }

  /**
     * CmdLineEvent listener.
     *
   * @see org.openmdx.base.application.control.CmdLineListener#cmdLineHelpRequest(org.openmdx.base.application.control.CmdLineEvent)
     */
    public void cmdLineHelpRequest(CmdLineEvent e)
    {
        System.out.println(e.getInfo());
        System.exit(Application.EXIT_CODE_FAILED);
    }

  /**
     * CmdLineEvent listener.
     *
   * @see org.openmdx.base.application.control.CmdLineListener#cmdLineVersionRequest(org.openmdx.base.application.control.CmdLineEvent)
     */
    public void cmdLineVersionRequest(CmdLineEvent e)
    {
        System.out.println(e.getInfo());
        System.exit(Application.EXIT_CODE_OK);
    }

  /**
     * CmdLineEvent listener.
     *
   * @see org.openmdx.base.application.control.CmdLineListener#cmdLineTrace(org.openmdx.base.application.control.CmdLineEvent)
     */
    public void cmdLineTrace(CmdLineEvent e)
    {
        System.out.println(e.getInfo());
    }

    /**
     * ExceptionEvent listener.
     *
     * @see org.openmdx.compatibility.base.application.control.ExceptionListener#exception(org.openmdx.compatibility.base.application.control.ExceptionEvent)
     */
    public void exceptionThrown(Exception e){
         SysLog.criticalError("", e instanceof BasicException || e instanceof BasicException.Wrapper ? (Object)e.toString() : e);
    }

  /**
     * Terminates the currently running application using the applications exit
     * code to exit.
     *
     * The argument serves as a status code; by convention, a nonzero status
     * code indicates abnormal termination.
     *
     * This method calls the exit method in class System. This method never
     * returns normally.
     */
    public void exit()
    {
        exit(this.application.getExitCode());
    }

 /**
     * Returns the application object.
     *
     * @return   the application object
     */
    public Application getApplication()
    {
        return this.application;
    }

    /**
     * Initializes the logging system.
     *
     * @param configName   A configuration name
     * @param logSource    A log source
     */
    public void initLogging(String configName, String logSource)
    {
        getLogConfiguration().setConfigName(configName);
        getLogConfiguration().setLogSource(logSource);

//      AppLog.setConfigName(configName);
//      AppLog.setLogSource(logSource);
    }


    /**
     * Returns the command line arguments as processed by the command line
     * processor.
     *
     * @return CmdLineArgs
     */
    public CmdLineArgs getCmdLineArgs()
    {
        return cmdLineProcessor.getCmdLineArgs();
    }


    /**
     * Returns the command line arguments as received from the VM's main.
     *
     * @return String[]
     */
    public String[] getRawArgs()
    {
        return this.rawArgs;
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
    private void exit(int exitCode)
    {
        SysLog.info("Application exits with exit code " + exitCode);
        System.exit(exitCode);
    }


    /**
     * Controls the application's execution flow.
     *
     * @param  args  the command line args from main()
     * @return false on any failure
     */
    private boolean execute()
      throws Exception
    {
        boolean initOK = false;
        boolean runOK = false;
        boolean releaseOK = false;


        // Process the command line
        processCmdLine();

        SysLog.detail(
                "Starting application " + this.application.getName()
                + " " + this.application.getVersion());

        SysLog.trace("Command line options successfully parsed");

        logCommandLineOptions();

        // INIT
        initOK = doInit();

        // RUN
        if (initOK) {
            SysLog.detail("Successfully initialized");

            doRun();

            runOK = !this.application.hasFailed();

            if (runOK) {
                SysLog.info("Successfully run");
            }else{
                SysLog.info("Run failed");
            }
        }

        // RELEASE
        if (initOK) {
            if (!runOK) {
                SysLog.info("Run failed -> calling release() for cleaning up!");
            }

            // If init() was OK, (doesn't matter wether run() was successful)
            // call release()
            doRelease();

            releaseOK = !this.application.hasFailed();

            if (runOK) {
                SysLog.info((releaseOK ? "Successfully released" : "Release failed"));
            }
        }

        return !this.application.hasFailed();
    }


    /**
     * Initialises the application.
     *
     * @return    true if successful
     */
    private boolean doInit()
        throws Exception
    {
        try {
            SysLog.trace("enter");

            long start = System.currentTimeMillis();

            // Initialise the manageables first
            for(int ii=0; ii<this.manageables.size(); ii++) {
                Manageable m = (Manageable)manageables.get(ii);
                SysLog.detail("Initialising manageable " + m.getClass().getName());
                m.init();
            }

            // Initialise the application
            SysLog.detail("Initialising application " + this.application.getClass().getName());
            this.application.init();

            SysLog.performance("Elapsed time init()", System.currentTimeMillis() - start);
            SysLog.trace("exit");
            return true;
        }
        catch (Exception ex) {
                handleExceptionEvent(ex);
                if (!this.application.hasFailed()) {
                    this.application.setExitCode(Application.EXIT_CODE_FAILED);
                }
        }

        return false;
    }


    /**
     * Runs the application.
     */
    private void doRun()
        throws Exception
    {
        try {
            SysLog.trace("enter");
            long start = System.currentTimeMillis();

            this.application.run();

            SysLog.performance("Elapsed time run()", System.currentTimeMillis() - start);
            SysLog.trace("exit");
            return;
        }
        catch (Exception ex) {
            handleExceptionEvent(ex);
            if (!this.application.hasFailed()) {
                this.application.setExitCode(Application.EXIT_CODE_FAILED);
            }
        }
    }


    /**
     * Releases the application.
     */
    private void doRelease()
        throws Exception
    {
        SysLog.trace("enter");
        long start = System.currentTimeMillis();


        //Release the application first
        try {
            SysLog.detail("Releasing application " + this.application.getClass().getName());
            this.application.release();
        }
        catch (Exception ex) {
            handleExceptionEvent(ex);
            if (!this.application.hasFailed()) {
                this.application.setExitCode(Application.EXIT_CODE_FAILED);
            }
        }


        //Release the manageables in reverse order
        for(int ii=this.manageables.size()-1; ii>= 0; ii--) {
            try {
                Manageable m = (Manageable)manageables.get(ii);
                SysLog.detail("Releasing manageable " + m.getClass().getName());

                m.release();
            }
            catch (Exception ex) {
                handleExceptionEvent(ex);
                if (!this.application.hasFailed()) {
                    this.application.setExitCode(Application.EXIT_CODE_FAILED);
                }
            }
        }

        SysLog.performance("Elapsed time release()", System.currentTimeMillis() - start);
        SysLog.trace("exit");
    }


    /**
     * Process the command line arguments.
     *
     * The application may immediately terminate here if
     * <ul>
     *   <li> the command line options do not match the passed arguments
     *   <li> the argument -h, --help or --version has been passed
     * </ul>
     */
    private void processCmdLine()
    {
        CmdLineFreeArgOption freeArgOption;
        List options = new ArrayList();
        List opts;
        int parserState;
        Iterator iter;


        // Merge the application options
        opts = this.application.getCmdLineOptions();
        if (opts != null) options.addAll(opts);

        // Merge the manageable options
        for(int ii=0; ii<this.manageables.size(); ii++) {
            Manageable m = (Manageable)manageables.get(ii);
            opts = m.getCmdLineOptions();
            if (opts != null) options.addAll(opts);
        }

        // Merge the system options
        opts = getSystemCmdLineOptions();
        if (opts != null) options.addAll(opts);

        // Get the applications free argument command line option specification
        freeArgOption = this.application.getCmdLineFreeArgOptions();

        // Attach the command line args
        cmdLineProcessor.setRawArgs(this.rawArgs);

        // Attach the command line options
        cmdLineProcessor.setOptions(this.application.getHelpText(), options);

        // Attach the command line free argument options
        cmdLineProcessor.setFreeArgsOption(freeArgOption);

        // Attach application name/version
        cmdLineProcessor.setApplicationName(this.application.getName());
        cmdLineProcessor.setApplicationVersion(this.application.getVersion());

        // Invoke the command line parser
        ArrayList cmdLineTraces = new ArrayList();
        parserState = cmdLineProcessor.parse(false, cmdLineTraces);

        if (this.cmdLineParserTrace) {
            iter = cmdLineTraces.iterator();
            while(iter.hasNext()) {
                handleCmdLineEvent(
                    new CmdLineEvent(
                        this, CmdLineEvent.CMDLINE_TRACE, (String)iter.next()));
            }
        }

        // Call command line handler
        switch (parserState) {
            case CmdLineProcessor.PARSER_FAILED:
                handleCmdLineEvent(
                    new CmdLineEvent(
                        this,
                        CmdLineEvent.CMDLINE_BAD_ARGS,
                        cmdLineProcessor.getUsage()));
                exit(Application.EXIT_CODE_FAILED);
                break;

            case CmdLineProcessor.PARSER_NO_ARGS:
                handleExceptionEvent(
                    new Exception("Internal ERROR: Command line arguments have not been supplied to the CommandLineProcessor")
                                );
                exit(Application.EXIT_CODE_FAILED);
                break;

            case CmdLineProcessor.PARSER_HELP:
                handleCmdLineEvent(
                    new CmdLineEvent(
                        this,
                        CmdLineEvent.CMDLINE_HELP_REQUEST,
                        cmdLineProcessor.getHelp()));
                exit(Application.EXIT_CODE_FAILED);
                break;

            case CmdLineProcessor.PARSER_SUCCESS:
                break;
        }
    }


    /**
     * Processes the raw command line options.
     */
    private void processRawCmdLineOptions()
        throws Exception
    {
        for(int ii=0; ii<this.rawArgs.length; ii++) {
            if (this.rawArgs[ii].equals("--trace-cmdline-parser")) {
                this.cmdLineParserTrace = true;
            }else if (this.rawArgs[ii].equals("--version")) {
                handleCmdLineEvent(
                    new CmdLineEvent(
                        this,
                        CmdLineEvent.CMDLINE_VERSION_REQUEST,
                        this.application.getName()
                        + " V" + this.application.getVersion()));
            }else if (this.rawArgs[ii].equals("--log-level")) {
                if (ii<(this.rawArgs.length-1)) {
                    String level = this.rawArgs[ii+1];
                    if (level.equals("critical")) {
                        getLogConfiguration().setLogLevelCriticalError();
                    }else if (level.equals("error")) {
                        getLogConfiguration().setLogLevelError();
                    }else if (level.equals("warning")) {
                        getLogConfiguration().setLogLevelWarning();
                    }else if (level.equals("info")) {
                        getLogConfiguration().setLogLevelInfo();
                    }else if (level.equals("detail")) {
                        getLogConfiguration().setLogLevelDetail();
                    }else if (level.equals("trace")) {
                        getLogConfiguration().setLogLevelTrace();
                    }else{
                        throw new Exception(
                                    "The log-level '" + level + "' is not supported."
                                    + " Check the program's help.");
                    }

                }
            }else if (this.rawArgs[ii].equals("--log-performance")) {
                getLogConfiguration().enablePerformanceLog();
            }else if (this.rawArgs[ii].equals("--log-statistics")) {
                getLogConfiguration().enableStatisticsLog();
            }else if (this.rawArgs[ii].equals("--dump-log-properties")) {
                System.out.println(getLogConfiguration());
                exit(Application.EXIT_CODE_OK);
            }
        }
    }

    /**
     * Retrieve the log configuration proxy from an optional library
     * 
     * @return the log configuration proxy
     */
    private LogConfiguration_1_0 getLogConfiguration(){
        if(this.logConfiguration == null) try {
            this.logConfiguration = (LogConfiguration_1_0) Classes.getApplicationClass(
                "org.openmdx.compatibility.base.log.LogConfiguration_1"
            ).newInstance();
        } catch (Exception exception) {
            SysLog.error(
                "Log configuration proxy unavailable, slf4j-openmdx1.jar is probalbly missing",
                exception
            );
            exit(Application.EXIT_CODE_OK);
        }
        return this.logConfiguration;
    }
    
    /**
     * Requests the system command line option specification.
     *
     * @return    A lis of cmd line options (objects of class CmdLineOption)
     */
    private ArrayList getSystemCmdLineOptions()
    {
        ArrayList options = new ArrayList();

        options.add(
            new CmdLineOption(
                "version",
                "Prints the version and exits"));

        options.add(
            new CmdLineOption(
                "log-level",
                "Activates a log level. Overrules the log level setting from the"
                + " log property file."
                + " Log levels: {critical, error, warning, info, detail, trace}",
                0, 1));

        options.add(
            new CmdLineOption(
                "log-performance",
                "Enables performnace logs. Overrules the log performance setting"
                + " from the log property file."));

        options.add(
            new CmdLineOption(
                "log-statistics",
                "Enables statistics logs. Overrules the log statistics setting"
                + " from the log property file."));

        options.add(
            new CmdLineOption(
                "trace-cmdline-parser",
                "Enables the tracing of the command line parser."));

        options.add(
            new CmdLineOption(
                "dump-log-properties",
                "Dumps the log properties and exits."));

        return options;
    }


    /**
     * Log all command line options. Print "****" for options that are secret.
     *
     * and therefore must not be logged.
     */
    private void logCommandLineOptions()
    {
        CmdLineOption option;
        List appOpts = this.application.getCmdLineOptions();
        List sysOpts = getSystemCmdLineOptions();

        List opts = new ArrayList();
        if (appOpts != null) opts.addAll(appOpts);
        if (sysOpts != null) opts.addAll(sysOpts);

        Iterator iter = opts.iterator();
        while(iter.hasNext()) {
            option = (CmdLineOption)iter.next();
            if (option.isSwitch()) {
                if (getCmdLineArgs().hasArg(option.getId())) {
                    SysLog.detail("CmdLine Switch: --" + option.getId());
                }
            }else{
                List values = getCmdLineArgs().getValues(option.getId());
                Iterator iVal = values.iterator();
                boolean multiValue = (values.size() > 1);
                int ii=0;
                while(iVal.hasNext()) {
                    String value = (String)iVal.next();
                    if (option.isSecret()) value = "******";
                    if (multiValue) {
                        SysLog.detail("CmdLine Option: --" + option.getId()
                                     + "[" + ii + "]=\"" + value + "\"");
                    }else{
                        SysLog.detail("CmdLine Option: --" + option.getId()
                                     + "=\"" + value + "\"");
                    }
                    ii++;
                }
            }
        }
    }


    /**
     * Handle a command line event by delegating it to the appropriate listener.
     *
     * @param event  A command line event
     */
    private void handleCmdLineEvent(CmdLineEvent event)
    {
        try {
            if (this.application instanceof CmdLineListener) {
                try {
                    // delegate
                    String methodName = "";

                    switch(event.getType()) {
                        case CmdLineEvent.CMDLINE_BAD_ARGS:
                            methodName = "cmdLineBadArgs";
                            break;
                        case CmdLineEvent.CMDLINE_HELP_REQUEST:
                            methodName = "cmdLineHelpRequest";
                            break;
                        case CmdLineEvent.CMDLINE_VERSION_REQUEST:
                            methodName = "cmdLineVersionRequest";
                            break;
                        case CmdLineEvent.CMDLINE_TRACE:
                            methodName = "cmdLineTrace";
                            break;
                    }

                    Method meth = ExceptionListener.class.getMethod(
                                    methodName, new Class[]{CmdLineEvent.class});

                    meth.invoke(this.application, new Object[]{event});
                    return;

                }catch(Throwable tx) { 
                    // ignore
                }
            }

            switch(event.getType()) {
                case CmdLineEvent.CMDLINE_BAD_ARGS:
                    cmdLineBadArgs(event);
                    break;
                case CmdLineEvent.CMDLINE_HELP_REQUEST:
                    cmdLineHelpRequest(event);
                    break;
                case CmdLineEvent.CMDLINE_VERSION_REQUEST:
                    cmdLineVersionRequest(event);
                    break;
                case CmdLineEvent.CMDLINE_TRACE:
                    cmdLineTrace(event);
                    break;
            }
        } catch(Throwable e) {
            // ignore
        }
    }

    /**
     * Handle an exception event by delegating it to the appropriate listener.
     *
     * @param exception  An exception
     */
    protected void handleExceptionEvent(Exception exception) {
        try {
             (
                 this.application instanceof ExceptionListener ? (ExceptionListener)this.application : this
             ).exceptionThrown(exception);
        } catch(Exception handlerException) {
            try {
                SysLog.error("The exception handler caused an exception", handlerException);
            } catch (Exception logException){
                // This method must never return with an exception
            }
        }
    }


    /** Trace command line parsing */
    private boolean cmdLineParserTrace = false;

    /** The command line processor object */
    private CmdLineProcessor cmdLineProcessor;

    /** The application */
    protected Application application;

    /** Additional manageables */
    private List manageables;

    /** Command line raw args */
    private final String[] rawArgs;

    /**
     * org.openmdx.compatibility.kernel.log.SysLog configuration proxy
     */
    LogConfiguration_1_0 logConfiguration = null;
    
}

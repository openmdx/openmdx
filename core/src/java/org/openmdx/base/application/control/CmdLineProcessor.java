/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CmdLineProcessor.java,v 1.7 2008/03/21 18:28:06 hburger Exp $
 * Description: Application Framework 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:28:06 $
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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openmdx.uses.gnu.getopt.Getopt;
import org.openmdx.uses.gnu.getopt.LongOpt;





/**
 * The CmdLineProcessor parses command line arguments
 *
 * <p>Definitions:
 * <pre>   foo   -a  -b 100 --noLog --port=8080  file1  file2  file3</pre>
 *
 * <pre>
 *
 *     Executable            :  foo
 *
 *     Command line options  :   -a   -b   --noLog   --port
 *
 *     Command line arguments:   '100' is an argument for the short option '-b'
 *                               '8080' is an argument for the long option '--port'
 *
 *     Free arguments        :   arguments that do not belong to an option
 *                               'file1'  'file2'  'file3'
 * </pre>
 *
 * A sample application using the command line processor:
 * <pre>
 * 
 * public class Application
 * {
 *   public int main(String args[])
 *   {
 * 	   ArrayList   	         options; 
 *	   CmdLineProcessor		 cmdLineProcessor(args);
 *
 *
 *     // Command line option definition
 *	   options.add(new CmdLineOption("delete", "Option --delete")); 
 *
 *	   // Attach the command line options 
 *	   cmdLineProcessor.setOptions("An overiew help text", options);
 *
 *     // Process the command line parameters and let the command line processor
 *     // handle any error conditions.
 *	   cmdLineProcessor.parse(true);	
 *
 *	   // Get the parsed command line arguments
 *	   CmdLineArgs  args = cmdLineProcessor.getCmdLineArgs();
 *	
 *     // Lookup
 *	   if ( args.hasArg("delete") ) {
 *	     ... any action ...
 *	   }
 *
 *     return 0;
 *   }
 * }
 * </pre>
 *
 */
@SuppressWarnings("unchecked")
public class CmdLineProcessor 
{

	/** Command line parsing failed */
	public final static int PARSER_FAILED  = 0; 
	
	/** No raw arguments set for the parser */ 
    public final static int PARSER_NO_ARGS = 1;     
	
	/** Command line parsing was successful */
    public final static int PARSER_SUCCESS = 2;		
	
	/** Parser recognized a help request */
    public final static int PARSER_HELP    = 3;        
	

	
	/**
	 * The Command Line Processor
	 */
	public CmdLineProcessor()
	{
		init();
	}
	

	/**
	 * The Command Line Processor
	 * 
	 * @param	args		The cmd line arguments from main()
	 */
	public CmdLineProcessor(String args[])
	{
		init();
		
		m_args.setRawArgs(args);
	}


    /**
     * Set an application name
     * 
     * @param name An application name
     */
    public void setApplicationName(String name)
    {
        m_applicationName = name;
    }

    /**
     * Set an application version
     * 
     * @param version An application version
     */
    public void setApplicationVersion(String version)
    {
        m_applicationVersion = version;
    }

	/**
	 * Set the command line arguments
	 * 
	 * @param	args		The cmd line arguments from main()
	 */
	public void setRawArgs(String args[])
	{
		m_args.setRawArgs(args);
	}
	
	
	/**
	 * Set the desired command line options and passes a help overview string
	 * that describes the applications functionality.
	 * 
	 * @param	pgmHelpOverview	 The program's help overview string
	 * @param	options			 A list of cmd line options (class CmdLineOption)
	 */
	public void setOptions(
			String		helpOverview,
			List   		options)
	{
		m_pgmHelpOverview = helpOverview;
		m_optionList      = options;
	}
	

	/**
	 * Set the desired free argument options and passes a usage string
	 * that describes the free arguments.
	 * 
	 * @param	freeArgOption	 The free argument option
	 */
	public void setFreeArgsOption(
		CmdLineFreeArgOption	freeArgOption)
	{
		m_freeArgOption = freeArgOption;
	}
	

	
	/**
	 * Parse the command line.
	 *
	 * Preconditions:
	 * <ul>
	 *   <li> The raw command line arguments (argc, argv) must have be set
	 *   <li> The command line options must have been set optionally using
	 *        a call to setOptions("help overview", options)
	 *   <li> The free args options must have been set optionally using
	 *        a call to setFreeArgsOption("usage", minArgs, maxArgs)
	 * </ul>
	 *     
	 * <p>If 'handleErrors' is set to true, the command line processor handles
	 * any command line parsing errors on behalf of the application and exits
	 * the application with the exitCode 255 on any error.
	 *
	 * <p>If an application must handle the parsing state itself it can use the
	 * snippet listed below (actually this snippet is used when 'handleErrors'
	 * is set to 'true'):
	 *
	 * <pre>
	 * 
	 * 	parserState = cmdLineProcessor.parse(false);
	 * 	switch (parserState) {
	 * 		case CmdLineProcessor.PARSER_FAILED:
	 * 			System.out.println("Command line parsing failed!");
	 * 			System.out.println("Usage:");
	 * 			System.out.println(cmdLineProcessor.getUsage());
	 * 			System.exit(1);	
	 * 			break;
	 * 		case CmdLineProcessor.PARSER_NO_ARGS:
	 *			System.out.println(
	 *				"Internal ERROR: Command line arguments have not been " +
	 *				"supplied to the CommandLineProcessor");
	 * 			System.exit(1);	
	 * 			break;
	 * 		case CmdLineProcessor.PARSER_HELP:
	 * 			System.out.println(cmdLineProcessor.getHelp());
	 * 			System.exit(1);	
	 * 			break;
	 * 		case CmdLineProcessor.PARSER_SUCCESS:
	 * 			break;
	 * 	}
	 * </pre>
	 * 
	 * @see #parse(boolean, List)
	 *
	 * @param handleErrors if true the parser handles the errors itself
	 * @return	The parser's result code
	 */
    public int parse(boolean handleErrors)
    {
        return parse(handleErrors, null);
    }	
	
	/**
	 * Parse the command line.
	 *
	 * @see #parse(boolean)
	 *
	 * @param handleErrors if true the parser handles the errors itself
	 * @param traceMessages if not null filled up with parse trace messages 
	 * (strings)
	 * @return	The parser's result code
	 */
	public int parse(boolean handleErrors, List traceMessages)
	{
        m_traceMessages.clear();
        
		m_parse_state = PARSER_SUCCESS;
	
		parseProlog();
	
		parseArgumentFile();
			
		if (m_parse_state == PARSER_SUCCESS) {
			parseAnalyze();
		}
		
		if (m_parse_state == PARSER_SUCCESS) {
			m_args.setParsedArgs(m_parse_optArgs, m_parse_freeArgs);
		
			parseVerify();
		
			parseEpilog();
		}
		
		// handle errors right here?
		if (handleErrors) {
			parseHandleErrors();
		}
		
		if (traceMessages != null) {
			traceMessages.addAll(m_traceMessages);
		}
		return m_parse_state;
	}
	

	
	/**
	 * Returns the combined usage string for all application options
	 * 
	 * <p>
	 * E.g
	 * <pre>
	 * NAME
	 * Application 1.0
	 *
	 * OPTIONS
	 *   --aaa arg [1..2]       -  Option --aaa
	 *   --bbb arg [0..2]       -  Option --bbb
	 *   -h                     -  print a help text. 
	 *   --help                 -  print a help text. 
	 *  
	 *   Free args [1..4]       -  Files to convert
	 * <pre>
	 *
	 * @return	A string
	 */
	public String  getUsage()
	{
		return "NAME" + EOL
				+ " " + m_applicationName + " " + m_applicationVersion + EOL + EOL
				+ "OPTIONS" + EOL
		        + getUsage(false, true);
	}
	


	/**
	 * Returns the help overview string provided by the application and a help 
	 * string for all application options.
	 * 
	 * <p>
	 * E.g
	 * <pre>
	 * NAME
	 * Application 1.0
	 *
	 * DESCRIPTION
	 * The help overview text provided by the application    
	 *
	 * OPTIONS
	 *   --aaa arg [1..2]       -  Option --aaa
	 *   --bbb arg [0..2]       -  Option --bbb
	 *   -h                     -  print a help text. 
	 *   --help                 -  print a help text. 
	 * 
	 *   Free args [1..4]       -  Files to convert
	 * <pre>
	 * 
	 * @return	A string
	 */
	public String  getHelp()
	{
	    StringBuilder	help = new StringBuilder();
        ArrayList  		overview = formatText(m_pgmHelpOverview, OVERVIEW_TEXT_WIDTH);
		
		for(int ii=0; ii<overview.size(); ii++) {
			help.append(
                "  "
            ).append(
                overview.get(ii)
            ).append(
                EOL
            );
		}
		
        return "NAME" + EOL
                + "  " + m_applicationName + " " + m_applicationVersion + EOL + EOL
        		+ "DESCRIPTION" + EOL
                + help.toString() + EOL
                + "OPTIONS" + EOL
                + getUsage(false, true);
	}
	

	/**
	 * Get the parsed command lin arguments
	 * 
	 * @return	The cmd line args
	 */
	public CmdLineArgs  getCmdLineArgs()
	{
		return m_args;
	}
	
	
	/**
	 * Initializes a CmdLineProcessor object
	 */
	private void init()
	{
		m_additionalOptions = new ArrayList();
		m_pgmHelpOverview   = "";
		m_optionList        = new ArrayList();
		m_freeArgOption     = null;
		m_args              = new CmdLineArgs();
		m_parse_state       = PARSER_NO_ARGS;
		m_parse_optionList  = new ArrayList();
		m_parse_freeArgs    = new ArrayList(); 
		m_parse_optArgs     = new ArrayList();

		
        setAdditionalOptions();
	}
	
	private void parseProlog()
	{
		List	args = 	m_args.getRawArgs();
	
		
		m_parse_optionList.clear();	// clear the parser active option list
		m_parse_freeArgs.clear();
		m_parse_optArgs.clear();
	
		// no parsed args yet (passed the cleared containers)
		m_args.setParsedArgs(m_parse_optArgs, m_parse_freeArgs);
	
		m_parse_optionList.addAll(m_optionList);
		m_parse_optionList.addAll(m_additionalOptions);
	
		if (args == null) {
			m_parse_state = PARSER_NO_ARGS;
			return;
		}
		
		if ( isHelpRequested() ) {
	        m_parse_state = PARSER_HELP;
			return;
		}
	}
	
	private void parseArgumentFile()
	{
		String		argumentFile;
		boolean     bOK;
	
	
		// check for argument file
		argumentFile = getArgumentFile();
		if (argumentFile != null) {
           	trace("[setup] Reading arg file: " + argumentFile);
			bOK = m_args.readArgumentFile(argumentFile);
			if (!bOK) {
				trace("[error] Reading arg file '" + argumentFile + "' failed");
				m_parse_state = PARSER_FAILED;
			}	
		}
	}
	
    private void parseAnalyze()
	{
        StringBuilder    shortopts = new StringBuilder();
		ArrayList   	longopts  = new ArrayList();
		LongOpt   		gnu_longopts[];
		CmdLineOption 	opt;
		CmdLineArg      cmdArg;
		String			arg, name;
		LongOpt         lopt;
		int             c;
		
		
		for(int ii=0; ii<m_parse_optionList.size(); ii++) {
			opt = (CmdLineOption)m_parse_optionList.get(ii);

            trace("[setup] " + opt.toString());
			
			if (opt.getId().length() == 1) {
				// Short option
                shortopts.append(opt.getId());
				if (!opt.isSwitch()) shortopts.append(":");
			}else{
				// Long option
				if (opt.isSwitch()) {
					longopts.add(
						new LongOpt(
							opt.getId(), LongOpt.NO_ARGUMENT, null, 0));
				}else{
					longopts.add(
						new LongOpt(
							opt.getId(), LongOpt.REQUIRED_ARGUMENT, null, 0));
				}
			}
		}
		
        trace("[setup] Short = " + shortopts);

		gnu_longopts = (LongOpt[])longopts.toArray (new LongOpt[longopts.size()]);
								
		List argRawList = m_args.getRawArgs();
		String  argRaw[] = (String[])argRawList.toArray (new String[argRawList.size()]);
		Getopt g = new Getopt("testprog", argRaw, shortopts.toString(), gnu_longopts);

		g.setOpterr(false); // We'll do our own error handling

		while((c = g.getopt()) != -1) {
			switch(c) {
        		case 0:
        			// LONG option
          			arg  = g.getOptarg();
          			lopt = gnu_longopts[g.getLongind()];
					name = lopt.getName();
					if (arg != null) {
						cmdArg = new CmdLineArg(name, arg);
					}else{
						cmdArg = new CmdLineArg(name);
					}
					m_parse_optArgs.add(cmdArg);
                	trace("[analyze] Long Option: " + cmdArg.toString());
          			break;
          			
 				case ':':
					m_parse_state = PARSER_FAILED;
                	trace("[error] You need an argument for option '" + 
						   (char)g.getOptopt());
          			break;
          			
 	       		case '?':
					m_parse_state = PARSER_FAILED;
                	trace("[error] The option '" +  (char)g.getOptopt() +  
                          "' is not valid");
          			break;
          			
		        default:
		        	// SHORT option
					name = String.valueOf ((char) c);
          			arg = g.getOptarg();
					if (arg != null) {
						cmdArg = new CmdLineArg(name, arg);
					}else{
						cmdArg = new CmdLineArg(name);
					}
					m_parse_optArgs.add(cmdArg);
                	trace("[analyze] Short Option: " + cmdArg.toString());
         			break;
         	}
		}

		// Free args
	  	if (m_parse_state == PARSER_SUCCESS) {
		 	for(int kk = g.getOptind(); kk < argRaw.length ; kk++) {
				m_parse_freeArgs.add(argRaw[kk]);
		 	}
		}
	}

	
	private void parseVerify()
	{
	  	if (m_parse_state == PARSER_SUCCESS) {
	
	        if (m_freeArgOption != null) {
				// check min free args
				if (m_parse_freeArgs.size() < m_freeArgOption.getMinimum()) {
					m_parse_state = PARSER_FAILED;
					trace("[error] Not enough free arguments (min = " +
						  m_freeArgOption.getMinimum() + ").");
					return;
				}
		
				// check max free args
				if (m_parse_freeArgs.size() > m_freeArgOption.getMaximum()) {
					m_parse_state = PARSER_FAILED;
                    trace("[error] Too many free arguments (max = " +
						  m_freeArgOption.getMaximum() + ").");
					return;
				}
			}
			 		
			// check for required options
			for(int ii=0; ii<m_optionList.size(); ii++) {
				CmdLineOption  opt = (CmdLineOption)m_optionList.get(ii);
				if (opt.isSwitch()) continue;
						
				// check minimum and maximum
				int  count = m_args.getValues(opt.getId()).size();
	
				if (count<opt.getMinimum() || count>opt.getMaximum()) {
				    StringBuilder  tmp = new StringBuilder(
                        "[error] Bad number of occurrences ("
                    );
					if (count<opt.getMinimum()) {
						tmp.append(
                            "min = "
                        ).append(
                            opt.getMinimum()
                        );
					}
					if (count>opt.getMaximum()) {
                        tmp.append(
                            " max = "
                        ).append(
                            opt.getMaximum()
                        );
					}
                    tmp.append(
                        ") for option '"
                    ).append(
                        opt.getId().length() == 1 ? "-" : "--"
                    ).append(
                        opt.getId()
                    ).append(
                        "'. "
                    );
                    trace(tmp.toString());

					m_parse_state = PARSER_FAILED;
					return;
				}
			}		
		}	
	}
	
	private void parseEpilog()
	{
	  	if (m_parse_state == PARSER_SUCCESS) {
            trace("Command Line Option Dump: "); 
            trace(getCmdLineArgs().toString());
		}
	}
	
	private void parseHandleErrors()
	{
	    // print trace messages
	    Iterator iter = m_traceMessages.iterator();
	    while(iter.hasNext()) {
	        System.out.println(iter.next());
	    }
	    
		switch (m_parse_state) {
			case PARSER_FAILED:
				System.out.println("Command line parsing failed!");
				System.out.println("Usage:");
				System.out.println(getUsage());
				System.exit(255);
				break;
					
			case PARSER_NO_ARGS:
	 			System.out.println(
	 				"Internal ERROR: Command line arguments have not been " +
	 				"supplied to the CommandLineProcessor");
				System.out.println();
				System.exit(255);
	            break;

			case PARSER_HELP:
				System.out.println(getHelp());
				System.exit(255);	
	            break;

			case PARSER_SUCCESS:
				break;
		}	
	}
	
	
	/**
	 * Returns the usage string for single command line option
	 * 
	 * @param   option - A command line option
	 * @return	A String
	 */
	private String  getUsage(
		boolean addDebugOptions, 
		boolean addHelpOptions)
	{
		int           ii;
		StringBuilder  usage = new StringBuilder();
	
		
		// The application's options
		for(ii=0; ii<m_optionList.size(); ii++) {
			usage.append(
                getUsage((CmdLineOption)m_optionList.get(ii))
            ).append(
                EOL
            );
		}
	
		// Add the additional options
        usage.append(
            EOL
        );
		for(ii=0; ii<m_additionalOptions.size(); ii++) {
            usage.append(
                getUsage((CmdLineOption)m_additionalOptions.get(ii))
            ).append(
                EOL
            );
		}
		
		// free args
		if (m_freeArgOption == null) {
            usage.append(
                EOL
            ).append(
                getArgPrefix()
            ).append(
                "-No free arguments required-"
            ).append(
                EOL
            );
		}else{
		    StringBuilder  arg      = new StringBuilder();
		    StringBuilder  modifier = new StringBuilder();
			if (m_freeArgOption.getMaximum() == Integer.MAX_VALUE) {
				modifier.append(
                    "["
                ).append(
                    m_freeArgOption.getMinimum()
                ).append(
                    "..]"
                );  
			}else{
                modifier.append(
                    "["
                ).append(
                    m_freeArgOption.getMinimum()
                ).append(
                    ".."
                ).append(
                    m_freeArgOption.getMaximum()
                ).append(
                    "]"
                );  
			}
            arg.append(
                "Free args "
            ).append(
                modifier
            );
	
			int padding = LEN_ARG_DESCR - arg.length();
			padding = (padding < 0) ? 0 : padding;
	
            usage.append(
                EOL
            ).append(
                getArgPrefix()
            ).append(
                arg
            );
			for(int kk=0; kk<padding; kk++) usage.append(
                ' '
            );
            usage.append(
                USAGE_DELIM
            ).append(
                m_freeArgOption.getUsage()
            ).append(
                EOL
            );
		}
	
		return usage.toString();
	}
	

	/**
	 * Returns the combined usage string for all application options
	 *
	 * @param	option  command line option
	 * @return	A string
	 */
	private String  getUsage(CmdLineOption  option)
	{
		int            ii;
		String      	endl     = System.getProperty("line.separator");		
		StringBuilder	usage    = new StringBuilder();
		StringBuilder	arg      = new StringBuilder();
		StringBuilder	modifier = new StringBuilder();
		ArrayList		argUsage;
		int				padding;
		
	
		argUsage = formatText(option.getUsage(), ARG_USAGE_WIDTH);
		if (argUsage.size() == 0) return "";
		
		if (option.isSwitch()) {
		    arg.append(
                option.getId().length() == 1 ? "-" : "--"
            ).append(
                option.getId()
            );
		}else{
		    modifier.append(
                " arg  "
            );
			if (option.getMaximum() == Integer.MAX_VALUE) {
                modifier.append(
                    "["
                ).append(
                    option.getMinimum()
                ).append(
                    "..]"
                );  
			}else{
                modifier.append(
                    "["
                ).append(
                    option.getMinimum()
                ).append(
                    ".."
                ).append(
                    option.getMaximum()
                ).append(
                    "]"
                );  
			}
            arg.append(
                option.getId().length() == 1 ? "-" : "--"
            ).append(
                option.getId()
             ).append(
                 modifier
             );
		}
		
		padding = LEN_ARG_DESCR - arg.length();
		padding = (padding < 0) ? 0 : padding;
        usage.append(
            getArgPrefix()
        ).append(
            arg
        );
		for(ii=0; ii<padding; ii++) usage.append(
            ' '
        );
        usage.append(
            USAGE_DELIM
        ).append(
            argUsage.get(0)
        );
		
		for(ii=1; ii<argUsage.size(); ii++) {
            usage.append(
                endl
            );
			padding = LEN_ARG_PREFIX + LEN_ARG_DESCR + USAGE_DELIM.length();
			for(int kk=0; kk<padding; kk++) usage.append(
                ' '
            );
            usage.append(
                argUsage.get(ii)
            );
		}
		
		return usage.toString();
	}
	
	
	/**
	 * Sets the help options
	 */
	private void  setAdditionalOptions()
	{
        m_additionalOptions.clear();
		
        m_additionalOptions.add(
            new CmdLineOption("argFile", 
        		"The optional argument file holds all or a part of the"
                + " command line arguments. One argument per line." + EOL
                + "Examples: " + EOL
                + "# This is a comment line" + EOL
                + "-t" + EOL
                + "--port 8080" + EOL,
            	0, 1)); 

        m_additionalOptions.add(
			new CmdLineOption(
				"h", 
			    "print a help text." + EOL
			    + " E.g.:  program -h [any other args]")); 
	
        m_additionalOptions.add(
			new CmdLineOption(
				"help", 
			    "print a help text." + EOL
			    + " E.g.:  program --help [any other args]")); 
	}
	

	/**
	 * Checks if command line help is requested. Meaning one of the help 
	 * switches is passed as first command line argument.
	 * 
	 * See also setHelpOptions()
	 *
	 * @return  true if help is requested
	 */
	private boolean  isHelpRequested()
	{
		List		args = m_args.getRawArgs();
		String      tmp;
	
	    // Check for help
	    if (args.size() >= 1) {
	    	tmp = (String)args.get(0); 
			return (tmp.equals("-h") || tmp.equals("--help"));
		}
		
		return false;
	}
	

	/**
	 * Checks if an argument file has been passed on the command line
	 * (--argFile file)
	 * 
	 * @return  the file or null if no argFile has been passed
	 */
	private String  getArgumentFile()
	{
		List	 args = m_args.getRawArgs();
		String   tmp;
		
	    // Check for --argFile option
	    for(int ii=0; ii < (args.size()-1); ii++) {
	    	tmp = (String)args.get(ii); 
			if (tmp.equals("--argFile")) {
				return (String)args.get(ii+1);
			}
		}
		
		return null;
	}
	


//	/**
//	 * Build the gnu option list
//	 *
//	 * @param   trace     Turn trace on/off
//	 * @param   options   The options to parse (objects of class CmdLineOption)
//	 * @param   longopts  The long options
//	 * @return  A string  The short options
//	 */
//	private void  buildGnuOptionList(
//			boolean		trace,
//			ArrayList	options,
//			ArrayList	longopts) 
//	{
//		longopts.clear();
//		
//	}

	/**
	 * Formats a text string to a paragraph width lines less than 'lineWidth'
	 * characters.
	 * 
	 * @param  text        a text line
	 * @param  lineWidth   length of the formatted text lines
	 * @return the formatted lines (a list of objects of class String)
	 */
	private ArrayList  	formatText(
			String 		_text, 
			int 		lineWidth)
	{
		ArrayList	    paragraph   = new ArrayList();
		StringBuilder	lineBuf     = new StringBuilder(lineWidth);
		String          line, token;

		
		String text = _text.replace('\t', ' ');
		
		StringTokenizer st = new StringTokenizer(text, " \r\n", true);
		while (st.hasMoreTokens()) {
			token = st.nextToken();
			if (token.equals("\r")) continue;
			
			if (token.equals("\n")) {
				line = lineBuf.toString().trim();
				paragraph.add(line);
				lineBuf = new StringBuilder(token);			
			}else if (token.equals(" ")) {
				if (lineBuf.length() > 0) lineBuf.append(" "); // no leading ' '
			}else{
				if ((lineBuf.length() + token.length()) >= lineWidth) {
					line = lineBuf.toString().trim();
					if (line.length() > 0) {
						paragraph.add(line); // no empty line
						lineBuf = new StringBuilder(token);			
					}
				}else{
                    lineBuf.append(token);
				}
			}
		}
		
		line = lineBuf.toString().trim();
		if (line.length() > 0) paragraph.add(line);
			
		return paragraph;
	}


	/**
	 * Returns the argument prefix
	 * 
	 * @return a String
	 */
	private String getArgPrefix()
	{
	    StringBuilder    buf = new StringBuilder();
		
		for (int ii=0; ii<LEN_ARG_PREFIX; ii++) buf.append(' ');	
		
		return buf.toString();
	}


	/**
	 * Add a trace message to the trace message list
	 * 
	 * @param msg  A trace message
	 */
	private void trace(String msg)
	{
       	m_traceMessages.add("CmdLine processor: " + msg);
	}


	private static final int   		LEN_ARG_PREFIX      =  2;
	private static final int   		LEN_ARG_DESCR       = 30;
	private static final int   		ARG_USAGE_WIDTH     = 45;
	private static final int   		OVERVIEW_TEXT_WIDTH = 80;
	private static final String   	USAGE_DELIM         = " -  ";
	


    private final static String  EOL = System.getProperty("line.separator");		

    /** An application name */
    private String  m_applicationName = "Application";
    
    /** An application version */
    private String  m_applicationVersion = "";

	/** The additional options */
	private ArrayList  m_additionalOptions;

	/** The help overview */
	private String  m_pgmHelpOverview;

	/** The complete option list (objects of class CmdLineOption) */
	private List  m_optionList;

	/** The free arg options */
	private CmdLineFreeArgOption  m_freeArgOption;

	/** The command line args */
	private CmdLineArgs  m_args;
	
	/** The parser state */
	private int  m_parse_state;
		
	/** The parsed options (objects of class CmdLineOption) */
	private ArrayList  m_parse_optionList;
	
	/** The parsed free args (objects of class String)*/
	private ArrayList  m_parse_freeArgs; 
	
	/** The opt args (objects of class CmdLineArg) */
	private ArrayList  m_parse_optArgs;
	
	private ArrayList m_traceMessages = new ArrayList();

}



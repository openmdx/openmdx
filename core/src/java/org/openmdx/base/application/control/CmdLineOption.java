/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CmdLineOption.java,v 1.3 2004/04/02 16:59:00 wfro Exp $
 * Description: Application Framework 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:00 $
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



/**
 * The CmdLineOption describes represents a single command line option
 *
 * Sample 1
 *   -d -z --port 80 --logFile ../tst.log 
 */

	
public class CmdLineOption 
{
	/**
	 * Option declaration for switches. 
	 *
	 * Switches are always optional. They may or may not be given on the command
	 * line. This is in contrast to options where a minimun and a maximum value 
	 * for repetitions must be specified.
	 *
	 * !! Restrictions !!
	 * The switches -h, --help and --helphelp are reserved for help 
	 * functionality and may not be specified by applications!
	 * 
	 * E.g:                        id        
	 *   [-a]                      "a" 
	 *   [--noLog]                 "noLog" 
	 * 
	 * @param	id			The (long or short) switch's id.
	 * @param	usage		The part of the usage string associated with
	 *						this specific switch.
	 */
	public CmdLineOption(
		String   	id,
		String		usage)
	{
		m_id       = id;
		m_usage    = usage;
		m_minimum  = 0;
		m_maximum  = Integer.MAX_VALUE;
		m_bSwitch  = true;
        m_isSecret = false;
	}
	



	/**
	 * Option declaration
	 * 
	 * !!Restrictions!!
	 * The specification for optional arguments as the POSIX standard states is 
	 * not implemented. Arguments are always required (option) or not required 
	 * (switch). Therefore only two CmdLineOption constructors are provided.
	 * Default arguments will be supported in the near future through an 
	 * additional constructor.
	 *
	 * E.g:                        id        minimum  maximum
	 *   --host foo.com            "host"       1        1 
	 *   --port 80 --port 81       "port"       1        2
	 *   [--host foo.com]          "host"       0        1 
	 *   [--port 80] [--port 81]   "port"       0        2
	 * 
	 * @param	id			The (long or short) option's id.
	 * @param	usage		The part of the usage string associated with
	 *						this specific option.
	 * @param	minimum		Defines the minimal number of repetitions for
	 *						this option.
	 * @param	maximum		Defines the maximal number of repetitions for
	 *						this option. 
	 */
	public CmdLineOption(
		String  id,
		String  usage,
		int     minimum,
		int     maximum)
	{
		m_id       = id;
		m_usage    = usage;
		m_minimum  = minimum;
		m_maximum  = maximum;
		m_bSwitch  = false;
        m_isSecret = false;
	}

    /**
     * Option declaration
     * 
     * !!Restrictions!!
     * The specification for optional arguments as the POSIX standard states is 
     * not implemented. Arguments are always required (option) or not required 
     * (switch). Therefore only two CmdLineOption constructors are provided.
     * Default arguments will be supported in the near future through an 
     * additional constructor.
     *
     * E.g:                        id        minimum  maximum
     *   --host foo.com            "host"       1        1 
     *   --port 80 --port 81       "port"       1        2
     *   [--host foo.com]          "host"       0        1 
     *   [--port 80] [--port 81]   "port"       0        2
     * 
     * @param	id			The (long or short) option's id.
     * @param	usage		The part of the usage string associated with
     *						this specific option.
     * @param	minimum		Defines the minimal number of repetitions for
     *						this option.
     * @param	maximum		Defines the maximal number of repetitions for
     *						this option. 
     * @param	secret		The option is secret. Meaning that its argument
     *                      must not be logged or made in other ways public.
     */
    public CmdLineOption(
        String  id,
        String  usage,
        int     minimum,
        int     maximum,
        boolean secret)
    {
        m_id       = id;
        m_usage    = usage;
        m_minimum  = minimum;
        m_maximum  = maximum;
        m_bSwitch  = false;
        m_isSecret = secret;
    }
	

	/**
	 * Option declaration
	 * 
	 * !!Restrictions!!
	 * The specification for optional arguments as the POSIX standard states is 
	 * not implemented. Arguments are always required (option) or not required 
	 * (switch). Therefore only two CmdLineOption constructors are provided.
	 * Default arguments will be supported in the near future through an 
	 * additional constructor.
	 *
	 * E.g:                        id        minimum  
	 *   --host foo.com            "host"       1      
	 *   --port 80 --port 81       "port"       1     
	 *   [--host foo.com]          "host"       0      
	 *   [--port 80] [--port 81]   "port"       0     
	 * 
	 * @param	id			The (long or short) option's id.
	 * @param	usage		The part of the usage string associated with
	 *						this specific option.
	 * @param	minimum		Defines the minimal number of repetitions for
	 *						this option.
	 */
	public CmdLineOption(
		String  id,
		String  usage,
		int    minimum)
	{
		m_id       = id;
		m_usage    = usage;
		m_minimum  = minimum;
		m_maximum  = Integer.MAX_VALUE;
		m_bSwitch  = false;
        m_isSecret = false;
	}


    /**
     * Returns the id 
     * @return  a String
     */
	public String getId()
	{
		return m_id;
	}
	

    /**
     * Returns the usage string 
     *
     * @return  a String
     */
	public String getUsage()
	{
		return m_usage;
	}


    /**
     * Tests if the option is a switch 
     * @return  true if a switch
     */
	public boolean isSwitch()
	{
		return m_bSwitch;
	}
	

    /**
     * Returns the minimal number of arguments for this option 
     * @return  a number
     */
	public int getMinimum()
	{
		return m_minimum;
	}
	

    /**
     * Returns the maximum number of arguments for this option 
     * @return  a number
     */
	public int getMaximum()
	{
		return m_maximum;
	}

    /**
     * Returns true if the arguments for this option are secret (password, ...).
     * 
     * @return  true if logging permitted
     */
    public boolean isSecret()
    {
    	return m_isSecret;
    }

    /**
     * Returns a string representation 
     * @return  a string
     */
	public String toString()
	{
		if (m_bSwitch) {
			return "Switch=" + m_id;
		}else{
			if (m_id.length() == 1) {
				return "Short=" + m_id + ", min=" + m_minimum + ", max=" + m_maximum;
			}else{
				return "Long=" + m_id + ", min=" + m_minimum + ", max=" + m_maximum;
			}
		}
	}
	
	
	private String     m_id;
	private String     m_usage;	
	private boolean   m_bSwitch;
	private int        m_minimum;
	private int        m_maximum;
	private boolean    m_isSecret;
}



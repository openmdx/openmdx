/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CmdLineArg.java,v 1.3 2004/04/02 16:59:00 wfro Exp $
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
 * CmdLineArg holds a parsed command line argument.
 */
public class CmdLineArg 
{
	/**
	 * A Command line argument (an option with a value that can be empty)
	 */
	public CmdLineArg(String  name,   String  value)
	{
		m_name    = name;
		m_value   = value;
		m_bSwitch = false;
	}
	

	/**
	 * A Command line argument (a switch)
	 */
	public CmdLineArg(String  name)
	{
		m_name    = name;
		m_value   = "";
		m_bSwitch = true;
	}
	

	
	/**
	 * Return the argument name
	 */
	public String   getName()
	{
		return m_name;
	}
	

	/**
	 * Return the argument value
	 */
	public String   getValue()
	{
		return m_value;
	}
	

	/**
	 * Check if the argument is a switch
	 */
	boolean   isSwitch()
	{
		return m_bSwitch;
	}
	
		
	/**
	 * Return a string representation
	 */
	public String   toString()
	{
		String prefix = (m_name.length() > 1) ? "--" : "-";

		if (m_bSwitch) {
			return prefix + m_name;
		}else{
			if (m_value.length() != 0) {
				return prefix + m_name + "=" + "\"" + m_value + "\"";
			}else{
				return prefix + m_name + "=" + "\"\"";
			}
		}
	}
	

	
	private String			m_name;
	private String			m_value;
	private boolean		m_bSwitch;
}


/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: CmdLineFreeArgOption.java,v 1.3 2004/04/02 16:59:00 wfro Exp $
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
 * The CmdLineFreeArgOption describes the free arguments for a command line
 */

	
public class CmdLineFreeArgOption 
{
	/**
	 * Free argument declaration.
	 * No minimal and maximal number for free arguemnts
	 *
	 * @param	usage		Describes the free arguments 
	 */
	public CmdLineFreeArgOption(
		String		usage)
	{
		m_usage   = usage;
		m_minimum = 0;
		m_maximum = Integer.MAX_VALUE;
	}

	/**
	 * Free argument declaration
	 * 
	 * @param	usage		Describes the free arguments
	 * @param	minimum		Defines the minimal number of free arguments.
	 */
	public CmdLineFreeArgOption(
		String		usage,
		int			minimum)
	{
		m_usage   = usage;
		m_minimum = minimum;
		m_maximum = Integer.MAX_VALUE;
	}

	/**
	 * Free argument declaration
	 * 
	 * @param	usage		Describes the free arguments
	 * @param	minimum		Defines the minimal number of free arguments.
	 * @param	maximum		Defines the maximal number of arguments. 
	 */
	public CmdLineFreeArgOption(
		String		usage,
		int			minimum,
		int			maximum)
	{
		m_usage   = usage;
		m_minimum = minimum;
		m_maximum = maximum;
	}
	

    /**
     * Returns the usage string 
     * @return  a String
     */
	public String getUsage()
	{
		return m_usage;
	}
	
	
    /**
     * Returns the minimal number of free arguments
     * @return  a number
     */
	public int getMinimum()
	{
		return m_minimum;
	}
	

    /**
     * Returns the maximum number of free arguments
     * @return  a number
     */
	public int getMaximum()
	{
		return m_maximum;
	}
	

	private String		m_usage;	
	private int 		m_minimum;
	private int 		m_maximum;
}



/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LogEntity.java,v 1.1 2008/03/21 18:21:54 hburger Exp $
 * Description: Log Entity
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:54 $
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
package org.openmdx.compatibility.kernel.log;


public class LogEntity implements Comparable<LogEntity>
{
	public LogEntity(String name, String mechanism)
	{
		this.name = name;
		this.mechanism = mechanism;
	}

	public String getName() 
	{ 
		return this.name; 
	}
	
	public String getMechanism() 
	{ 
		return this.mechanism; 
	}
	
	public void  setName(String value) 
	{ 
		this.name = value; 
	}
	
	public void setMechanism(String value) 
	{ 
		this.mechanism = value; 
	}
	
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		
		if (obj != null && getClass() == obj.getClass()) {
			return (mechanism.equals(((LogEntity)obj).getMechanism()) &&
			         name.equals(((LogEntity)obj).getName()));
		}
		
		return false;
	}
	
	public int hashCode()
	{
		return (mechanism + name).hashCode();
	}
	
	public int compareTo(LogEntity o) 
	{
		return name.compareTo(((LogEntity)o).name);
	}

	private String  mechanism;
	private String  name;
}

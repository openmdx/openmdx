/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ConfigurationSpecifier.java,v 1.2 2012/01/05 23:20:20 hburger Exp $
 * Description: Application Framework 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2012/01/05 23:20:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-3023, OMEX AG, Switzerland
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
package org.openmdx.application.cci;


/**
 * Class to declare the application's options
 * 
 * @deprecated The activation pattern has been replaced by the Java Bean 
 * pattern
 */
@Deprecated
public class ConfigurationSpecifier { 

    public final String usage;	
    public final boolean required;
    public final int minimum;
	public final int maximum;
    public final Object[] defaults;
	public final boolean sensitive;
		
	/**
	 * ConfigurationSpecifier declaration
	 * 
	 * @param	usage		The part of the usage string associated with
	 *						this specific option.
	 * @param	required	Defines whether the option is required.
	 * @param	minimum		Defines the minimal number of arguments for
	 *						this option.
	 * @param	maximum		Defines the maximal number of arguments for
	 *						this option. 
	 *						Integer.MAX_VALUE indicates no limit.
	 * @param	sensitive	Sensitive options are not logged by the
	 *						application framework.
	 */
	public ConfigurationSpecifier (
		String usage,
		boolean required,
		int minimum,
		int maximum,
		boolean sensitive
	) {
		this.usage		= usage;
		this.required	= required;
		this.minimum	= minimum;
		this.maximum	= maximum;
		this.defaults	= null;
		this.sensitive	= sensitive;
	}
		
	/**
	 * ConfigurationSpecifier declaration
	 * 
	 * @param	usage		The part of the usage string associated with
	 *						this specific option.
	 * @param	required	Defines whether the option is required.
	 * @param	minimum		Defines the minimal number of arguments for
	 *						this option.
	 * @param	maximum		Defines the maximal number of arguments for
	 *						this option. 
	 *						Integer.MAX_VALUE indicates no limit.
	 */
	public ConfigurationSpecifier (
		String usage,
		boolean required,
		int minimum,
		int maximum
	) {
		this(usage,required,minimum,maximum,false);
	}
	
	/**
	 * ConfigurationSpecifier declaration
	 * 
	 * @param	usage		The part of the usage string associated with
	 *						this specific option.
	 * @param	defaults	The default argument values in case the option
	 *						is not explicitely given.
	 * @param	minimum		Defines the minimal number of arguments for
	 *						this option.
	 * @param	maximum		Defines the maximal number of arguments for
	 *						this option. 
	 *						Integer.MAX_VALUE indicates no limit.
	 * @param	sensitive	Sensitive options are not logged by the
	 *						application framework.
	 */
	public ConfigurationSpecifier (
		String usage,
		Object[] defaults,
		int minimum,
		int maximum,
		boolean sensitive
	) {
		this.usage		= usage;
		this.required	= true;
		this.minimum	= minimum;
		this.maximum	= maximum;
		this.defaults	= defaults;
		this.sensitive	= sensitive;
	}

	/**
	 * ConfigurationSpecifier declaration
	 * 
	 * @param	usage		The part of the usage string associated with
	 *						this specific option.
	 * @param	defaults	The default argument values in case the option
	 *						is not explicitely given.
	 * @param	minimum		Defines the minimal number of arguments for
	 *						this option.
	 * @param	maximum		Defines the maximal number of arguments for
	 *						this option. 
	 *						Integer.MAX_VALUE indicates no limit.
	 */
	public ConfigurationSpecifier (
		String usage,
		Object[] defaults,
		int minimum,
		int maximum
	) {
		this(usage,defaults,minimum,maximum,false);
	}

	/**
	 * Flag declaration
	 *  
	 * @param	usage		The part of the usage string associated with
	 *						this specific option.
	 */
	public ConfigurationSpecifier (
		String usage
	){
		this (usage, false, 0, 1);
	}

}

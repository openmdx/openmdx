/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Multiplicity
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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
package org.openmdx.base.mof.cci;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * The multiplicities
 */
public enum Multiplicity {

	/**
	 * Cardinality 0 or 1
	 */
	OPTIONAL("0..1"),
	
	/**
	 * Cardinality 1
	 */
	SINGLE_VALUE("1..1"),
	
	/**
	 * An unbounded list
	 */
	LIST("list"),
	
	/**
	 * An unbounded set
	 */
	SET("set"),
	
	/**
	 * An unbounded sparse array
	 */
	SPARSEARRAY("sparsearray"),
	
	/**
	 * An unbounded map
	 */
	MAP("map"),

	/**
	 * A stream
	 */
	STREAM("stream");
	
	/**
	 * Constructor
	 * 
	 * @param value
	 */
	private Multiplicity(
		String value
	){
		this.code = value;
	}
  
	/**
	 * The external representation
	 */
	private final String code;

	/**
	 * Parse the multiplicity 
	 * 
	 * @param multiplicity the value's String representation
	 * 
	 * @return the corresponding enumeration value
	 * 
	 * @throws NullPointerException if value is <code>null</value>
	 * @throws IllegalArgumentException if the value does not match any of Multiplicity's <code>String</code> representations
	 */
	public static Multiplicity parse(
		String multiplicity
	){
		for(Multiplicity candidate : values()) {
			if(candidate.code.equals(multiplicity)){
				return candidate;
			}
		}
		throw Throwables.initCause(
			new IllegalArgumentException("Unknown multiplicity"),
			null,
			BasicException.Code.DEFAULT_DOMAIN,
			BasicException.Code.BAD_PARAMETER,
			new BasicException.Parameter("value", multiplicity)
		);
	}
	
	/**
	 * Tells whether the Multiplicity is one of<ul>
	 * <li>{@link #OPTIONAL}
	 * <li>{@link #SINGLE_VALUE}
	 * </ul>
	 * @return <code>true</code> if the multiplicity is single-valued
	 */
	public boolean isSingleValued(){
		return this == OPTIONAL || this == SINGLE_VALUE;
	}

	/**
	 * Tells whether the Multiplicity is one of<ul>
	 * <li>{@link #LIST}
	 * <li>{@link #SET}
	 * <li>{@link #SPARSEARRAY}
	 * <li>{@link #MAP}
	 * </ul>
	 * @return <code>true</code> if the multiplicity is multi-valued
	 */
	public boolean isMultiValued(){
		return this == LIST || this == SET || this == SPARSEARRAY || this == MAP;
	}
	
	/**
	 * Tells whether the Multiplicity is<ul>
	 * <li>{@link #STREAM}
	 * </ul>
	 * @return <code>true</code> if the multiplicity is stream-valued
	 */
	public boolean isStreamValued(){
		return this == Multiplicity.STREAM;
	}
	
	/**
	 * Retrieve the multiplicity's representation
	 * 
	 * @return the multiplicity's representation
	 */
	public String code(){
		return this.code;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return code();
	}
	
}

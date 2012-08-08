/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: FixedSizeIndexedRecord.java,v 1.12 2008/02/28 13:59:08 hburger Exp $
 * Description: JCA: IndexedRecord backed-up by a primitive type array
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/28 13:59:08 $
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
 * This product includes software developed by other organizations 
 * as listed in the NOTICE file.
 */
package org.openmdx.base.resource.spi;

import org.openmdx.base.resource.cci.ArrayBasedIndexedRecord;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Java Connector Architecture:
 * A fixed-size IndexedRecord implementation backed-up by an array.
 */
@SuppressWarnings("unchecked")
public class FixedSizeIndexedRecord 
	extends ArraysExtension.AsList
	implements ArrayBasedIndexedRecord, MultiLineStringRepresentation
{

	/**
     * 
     */
    private static final long serialVersionUID = 3978138833738936625L;

    /**
	 * Creates an IndexedRecord with the specified name and the given content.  
	 * <p>
	 * This constructor does not declare any exceptions as it assumes that the
	 * necessary checks are made by the record factory.
	 *
	 * @param     recordName
	 *            The name of the record acts as a pointer to the meta 
	 *            information (stored in the metadata repository) for a
	 *            specific record type. 
	 * @param     recordShortDescription
	 *            The short description of the Record; or null.
	 * @param     values
	 *            The values of the indexed record.
	 */
	public FixedSizeIndexedRecord(
		String recordName,
		String recordShortDescription,
		Object values
	){
		super(values);
		this.name = recordName;
		this.shortDescription = recordShortDescription;
	}


	/**
	 * Get the values backing-up the FixedSizeIndexedRecord.
	 * <p>
	 * Changes to the values are reflected by the IndexedRecord and vice
	 * versa.
	 *
	 * @return  the IndexedRecord's values
	 */
	public Object getValues(
	){
		return super.getDelegate();
	}


	//------------------------------------------------------------------------
	// Implements Serializable
	//------------------------------------------------------------------------

	/**
	 * Serial Version UID
	 */
	// static final long serialVersionUID = 3275321657666356693L;

	/**
	 * Deserialization
	 */
	protected FixedSizeIndexedRecord(
	){		
	    super();
	}
	
	
	//------------------------------------------------------------------------
	// Implements Record
	//------------------------------------------------------------------------
		 
	/**
	 * Gets the name of the Record. 
	 *
	 * @return  String representing name of the Record
	 */
	public final String getRecordName(
	){
		return this.name;
	}

	/**
	 * Sets the name of the Record. 
	 *
	 * @param recordName
	 *        Name of the Record
	 */
	public final void setRecordName(
		String recordName
	){
		this.name = recordName;
	}

	/**
	 * Gets a short description string for the Record.
	 * This property is used primarily by application development tools. 
	 *
	 * @return   String representing a short description of the Record
	 */
	public final String getRecordShortDescription(
	){
		return this.shortDescription;
	}

	/**
	 * Sets a short description string for the Record.
	 * This property is used primarily by application development tools. 
	 *
	 * @param recordShortDescription
	 *        Description of the Record
	 */
	public final void setRecordShortDescription(
		String recordShortDescription
	){
		this.shortDescription = recordShortDescription;
	}


	//------------------------------------------------------------------------
	// Extends Object
	//------------------------------------------------------------------------

	/**
	 * Returns a multi-line string representation of this IndexedRecord.
	 * <p>
	 * The string representation consists of the record name, follwed by the
	 * optional short description enclosed in parenthesis (" (...)"), followed 
	 * by a colon and the values enclosed in square brackets (": [...]"). Each
	 * value is written on a separate line and indented while embedded lines
	 * are indented as well.
	 *
	 * @return   a multi-line String representation of this Record.
	 */
	public String toString(
	){
		return IndentingFormatter.toString(this);
	}
	

	//------------------------------------------------------------------------
	// Instance members
	//------------------------------------------------------------------------
	
	/**
	 * The record's name
	 */
	private String name;    

	/**
	 * The record's short description
	 */
	private String shortDescription;

}
/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JCA: IndexedRecord backed-up by an optional object
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2019, OMEX AG, Switzerland
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

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Java Connector Architecture:
 * An IndexedRecord of size 0 or 1 backed-up by an optional value
 */
public final class SetRecordFacade 
    extends CollectionRecordFacade 
    implements org.openmdx.base.resource.cci.SetRecord 
{

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
	 */
	SetRecordFacade(
	    Supplier<Object> getter,
	    Consumer<Object> setter
	){
	    super(getter, setter);
	}


	//------------------------------------------------------------------------
	// Implements Serializable
	//------------------------------------------------------------------------

	/**
	 * Serial Version UID
	 */
    private static final long serialVersionUID = -3535615139292529654L;


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
		return NAME;
	}

	
    //------------------------------------------------------------------------
    // Implements {@code SetRecord}
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.util.AbstractList#add(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean add(Object e) {
        return !contains(e) && super.add(e);
    }

}

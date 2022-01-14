/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JCA: Fixed-size MappedRecord implementation
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.resource.spi;

import javax.resource.cci.MappedRecord;

import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Java Connector Architecture:
 * A fixed-size MappedRecord implementation.
 */
final class FixedSizeMappedRecord 
    extends ArraysExtension.AsMap
    implements MappedRecord, MultiLineStringRepresentation
{

    /**
     * Creates an MappedRecord with the specified name and the given content.  
     * <p>
     * This constructor does not declare any exceptions as it assumes that the
     * necessary checks are made by the record factory: The arguments keys
     * and values for example must have the same length.
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     * @param     recordShortDescription
     *            The short description of the Record; or null.
     * @param     keys
     *            The keys of the mapped record.
     * @param     values
     *            The values of the mapped record.
     */
    FixedSizeMappedRecord(
        String recordName,
        String recordShortDescription,
        Object[] keys,
        Object[] values
    ){
    	super(keys, values);
        this.name = recordName;
        this.shortDescription = recordShortDescription;
    }

    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------

    /**
     * Constructor
     */
    protected FixedSizeMappedRecord(
    ){      
        // for de-serialization
    }
    
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -2541643071249561421L;

    
    //--------------------------------------------------------------------------
    // Implements Record
    //--------------------------------------------------------------------------

    /**
     * 
     */
    private String name;    

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
     *
     */
    private String shortDescription;        

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
    
    /**
     * Returns a multi-line string representation of this MappedRecord.
     * <p>
     * The string representation consists of the record name, follwed by the
     * optional short description enclosed in parenthesis (" (...)"), followed 
     * by a colon and the mappings enclosed in braces (": {...}"). Each
     * key-value mapping is rendered as the key followed by an equals sign ("=")
     * followed by the associated value written on a separate line and indented
     * while embedded lines are indented as well.
     *
     * @return   a multi-line String representation of this Record.
     */
    @Override
    public String toString(
    ){
        return IndentingFormatter.toString(this);
    }
    
}

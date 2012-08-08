/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: VariableSizeIndexedRecord.java,v 1.12 2010/03/19 12:32:54 hburger Exp $
 * Description: JCA: variable-size IndexedRecord implementation
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/19 12:32:54 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.resource.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.resource.cci.IndexedRecord;

import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Java Connector Architecture:
 * A variable-size IndxedRecord implementation.
 */
@SuppressWarnings("unchecked")
public class VariableSizeIndexedRecord 
    extends ArrayList
    implements IndexedRecord, MultiLineStringRepresentation
{

    /**
     * Creates an IndexedRecord with the specified name and the given content.  
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     * @param     recordShortDescription
     *            The short description of the Record; or null.
     */
    VariableSizeIndexedRecord(
        String recordName,
        String recordShortDescription
    ){
        super();
        this.recordName = recordName;
        this.description = recordShortDescription;
    }

    /**
     * Creates an IndexedRecord with the specified name and the given content.  
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     */
    protected VariableSizeIndexedRecord(
        String recordName
    ){
        this(recordName,null);
    }

    /**
     * Creates an IndexedRecord with the specified name and the given content.  
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     * @param     description
     *            The description of the Record; or null.
     * @param     initialContent
     *            The list's initial content
     */
    VariableSizeIndexedRecord(
        String recordName,
        String description,
        Collection<?> initialContent
    ){
        super(initialContent);
        this.recordName = recordName;
        this.description = description;
    }

    /**
     * The record name
     */
    private String recordName;    

    /**
     * The record short description
     */
    private String description;

    
    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------

    /**
     * Constructor
     */
    protected VariableSizeIndexedRecord(){
        // for de-serialization
    }
    
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = 8633465662532489653L;


    //--------------------------------------------------------------------------
    // Implements Record
    //--------------------------------------------------------------------------

    /**
     * Gets the name of the Record. 
     *
     * @return  String representing name of the Record
     */
    @Override
    public final String getRecordName(
    ){
        return this.recordName;
    }

    /**
     * Sets the name of the Record. 
     *
     * @param name
     *        Name of the Record
     */
    @Override
    public final void setRecordName(
        String name
    ){
        this.recordName = name;
    }

    /**
     * Gets a short description string for the Record.
     * This property is used primarily by application development tools. 
     *
     * @return   String representing a short description of the Record
     */
    @Override
    public final String getRecordShortDescription(
    ){
        return this.description;
    }

    /**
     * Sets a short description string for the Record.
     * This property is used primarily by application development tools. 
     *
     * @param description
     *        Description of the Record
     */
    @Override
    public final void setRecordShortDescription(
        String description
    ){
        this.description = description;
    }

  
    //--------------------------------------------------------------------------
    // Implements List
    //--------------------------------------------------------------------------

    /**
     * Check if this instance has the same content as another List.
     * <p>
     * The Record's name and short description are ignored.
     *
     * @return  true if two instances are equal
     */
    @Override
    public boolean equals(
        Object other
    ){
        if (other == this) return true;
        if (!(other instanceof List)) return false;
        ListIterator e1 = listIterator();
        ListIterator e2 = ((List) other).listIterator();
        while(e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : o1.equals(o2))) return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    /**
     * Returns the hash code for the Record instance. 
     *
     * @return hash code
     */
    @Override
    public int hashCode(
    ){
        int hashCode = 1;
        for(Object member : this){
            hashCode *= 31;
            if(member != null) hashCode += member.hashCode();
        }
        return hashCode;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return  a copy of this IndexedRecord instance
     */
    @Override
    public Object clone(
    ){
        return new VariableSizeIndexedRecord(
            this.recordName,
            this.description,
            this
        );
    }

    /**
     * Returns a multi-line string representation of this IndexedRecord.
     * <p>
     * The string representation consists of the record name, follwed by the
     * optional short description enclosed in parenthesis (" (...)"), followed 
     * by a colon and the values enclosed in square brackets (": [...]"). Each
     * value is written on a separate line and indented while embedded lines are
     * indented as well.
     *
     * @return   a multi-line String representation of this Record.
     */
    @Override
    public String toString(
    ){
        return IndentingFormatter.toString(this);
    }

}

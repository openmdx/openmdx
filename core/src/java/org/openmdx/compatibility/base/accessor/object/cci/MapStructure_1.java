/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MapStructure_1.java,v 1.8 2008/09/10 08:55:27 hburger Exp $
 * Description: SPICE Structure_1_0 standard implementation
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:27 $
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
package org.openmdx.compatibility.base.accessor.object.cci;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

@SuppressWarnings("unchecked")
public class MapStructure_1
implements Structure_1_0 
{

    /**
     * Constructor
     *
     * @param       structureClass
     *              The model class of the structure to be created
     * @param       fieldMappings
     *              maps field names to field values
     */
    public MapStructure_1(
        MappedRecord record
    ){
        this.record = record;
    }


    //------------------------------------------------------------------------
    // Implements Structure_1_0
    //------------------------------------------------------------------------

    /**
     * Returns the structure's model class.
     *
     * @return  the structure's model class
     */
    public String objGetType(
    ){
        return this.record.getRecordName();
    }

    /**
     * Return the field names in this structure.
     *
     * @return  the (String) field names contained in this structure
     */
    public List objFieldNames(
    ){
        if(this.fieldNames==null)this.fieldNames = Collections.unmodifiableList(
            new ArrayList(this.record.keySet())
        );
        return this.fieldNames;
    }

    /**
     * Get a field.
     *
     * @param       field
     *              the fields's name
     *
     * @return      the fields value which may be null.
     *
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the structure has no such field
     */
    public Object objGetValue(
        String fieldName
    ) throws ServiceException {
        Object value = this.record.get(fieldName);
        if(
                value == null &&
                !this.record.containsKey(fieldName)
        ) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.BAD_MEMBER_NAME,
            "This structure has no such field",
            new BasicException.Parameter("fieldName", fieldName)
        );
        return value;
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    public String toString(
    ){
        return this.record.toString();
    }


    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * The structure's content
     */
    protected final MappedRecord record;

    /**
     * Field
     */  
    private transient List fieldNames = null;

}

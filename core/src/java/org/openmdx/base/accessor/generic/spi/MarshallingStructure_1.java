/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MarshallingStructure_1.java,v 1.7 2008/02/18 13:34:06 hburger Exp $
 * Description: SPICE Basic Accessor Object interface
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/18 13:34:06 $
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
package org.openmdx.base.accessor.generic.spi;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.MarshallingSparseArray;
import org.openmdx.compatibility.base.collection.SparseArray;


/**
 * The Structure_1_0 interface.
 */
public class MarshallingStructure_1 
    extends DelegatingStructure_1
    implements Serializable
{

    /**
     * 
     */
    private static final long serialVersionUID = 3258411716403738167L;

    /**
     * Constructor
     *
     * @param   the marshaller to be applied to the elements, filter and order
     *          objects.
     * @param   The delegate contains unmarshalled elements
     */   
    public MarshallingStructure_1(
        Structure_1_0 structure,
        ObjectFactory_1_0 marshaller
    ) {
        super(structure);
        this.marshaller = marshaller;
    }

    
    //------------------------------------------------------------------------
    // Implements Structure_1_0
    //------------------------------------------------------------------------
        
    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     */
    public String objGetType(
    ){
        return getDelegate().objGetType();
    }

    /**
     * Return the field names in this structure.
     *
     * @return  the (String) field names contained in this structure
     */
    public List objFieldNames(
    ){
        return getDelegate().objFieldNames();
    }
     
    /**
     * Get a field.
     *
     * @param       fieldName
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
    Object value = getDelegate().objGetValue(fieldName);
    if(value instanceof List) {
      return new MarshallingList(
        this.marshaller,
        (List)value
      );
    }
    else if(value instanceof Set) {
      return new MarshallingSet(
        this.marshaller,
        (Set)value
      );
    }
    else if(value instanceof SparseArray) {
      return new MarshallingSparseArray(
        this.marshaller,
        (SparseArray)value
      );
    }
    else if(value instanceof SortedMap) {
      return new MarshallingSortedMap(
        this.marshaller,
        (SortedMap)value
      );
    }
    else {
      return this.marshaller.marshal(value);
    }
    }

    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     *
     */
    protected ObjectFactory_1_0 marshaller;

}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Property.java,v 1.7 2008/03/21 20:15:15 hburger Exp $
 * Description: Property 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 20:15:15 $
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
package org.openmdx.compatibility.base.exception;

import java.io.Serializable;
import java.util.Map;

import org.openmdx.kernel.exception.BasicException;


/**
 * The Property class represents a multivalue property given by a property name 
 * and multiple property values. Property values are always of type string.
 *
 * @author   J. Lang
 *
 * @deprecated use BaseException.Parameter
 */
public final class Property 
  extends BasicException.Parameter
  implements Serializable 
{ 

    /**
     * 
     */
    private static final long serialVersionUID = 3257571719500739123L;

    /**
     * Creates a <code>Property</code> object.
     *
     * @param  name    The property name
     * @param  values  A non-null object containing property values
     *
     * @deprecated use BasicException.Parameter#Parameter(String,Object)
     */
    public Property(
        String  name,
        Object  values
    ){
        super(name,values);
    }

    /**
     * Creates a <code>Property</code> object.
     *
     * @param  mapEntry A map entry
     *
     * @deprecated use BasicException.Parameter#Parameter(String,Object)
     */
    @SuppressWarnings("unchecked")
    public Property(
        Map.Entry mapEntry      
    ) {
        super(mapEntry.getKey().toString(),mapEntry.getValue());
    }

    /**
     * Creates a <code>Property</code> object.
     *
     * @param   name       
     *          the property's name.
     * @param   value
     *          the property's value
     *
     * @deprecated use BasicException.Parameter#Parameter(String,boolean)
     */
    public Property(
        String  name,
        boolean value
    ){
        super(name, value);
    }

    /**
     * Creates a <code>Property</code> object.
     *
     * @param   name       
     *          the property's name.
     * @param   value
     *          the property's value
     *
     * @deprecated use BasicException.Parameter#Parameter(String,byte)
     */
    public Property(
        String  name,
        byte    value
    ){
        super(name,value);
    }

    /**
     * Creates a <code>Property</code> object.
     *
     * @param   name       
     *          the property's name.
     * @param   value
     *          the property's value
     *
     * @deprecated use BasicException.Parameter#Parameter(String,short)
     */
    public Property(
        String  name,
        short   value
    ){
        super(name,value);
    }

    /**
     * Creates a <code>Property</code> object.
     *
     * @param   name       
     *          the property's name.
     * @param   value
     *          the property's value
     *
     * @deprecated use BasicException.Parameter#Parameter(String,int)
     */
    public Property(
        String  name,
        int value
    ){
        super(name,value);
    }

    /**
     * Creates a <code>Property</code> object.
     *
     * @param   name       
     *          the property's name.
     * @param   value
     *          the property's value
     *
     * @deprecated use BasicException.Parameter#Parameter(String,long)
     */
    public Property(
        String  name,
        long    value
    ){
        super(name,value);
    }

    /**
     * Returns the property's first value. 
     *
     * @return  The property's first value as a string; or null if it is empty
     *
     * @deprecated use BasicException.Parameter#getValue()
     */
    public final String getFirstValue() {
        return super.getValue();
    }

    /**
     * Returns the string representation of the property's value.
     * <p>
     * The returned array must not be modifed by the caller.
     *
     * @return  The property's values
     *
     * @deprecated use BasicException.Parameter#getValue()
     */
    public final String[] getValues(
    ){
        return getValue() == null ? null : new String[]{getValue()};
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param       index
     *              index of element to return.
     *
     * @return      the element at the specified position in this list.
     *
     * @exception   IndexOutOfBoundsException
     *              if the given index is out of range (index < 0 || index >=
     *              size()).
     *
     * @deprecated without replacement
     */
    public Object get(
        int index
    ){
        return getValues()[index];
    }

    /**
     * Returns the number of elements in this list. If this list contains more
     * than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     *
     * @return      the number of elements in this list.
     *
     * @deprecated without replacement
     */
    public int size(
    ){
        return getValues().length;
    }

}


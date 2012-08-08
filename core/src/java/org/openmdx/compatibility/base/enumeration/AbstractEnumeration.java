/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractEnumeration.java,v 1.5 2008/09/09 14:49:28 hburger Exp $
 * Description: SPICE Collections: Abstract Enumeration 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 14:49:28 $
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
package org.openmdx.compatibility.base.enumeration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * The unit of work status interface.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractEnumeration 
    implements Cloneable
{

    /**
     * Constructor
     *
     * @param   enumerationClass
     *          the non-abstract enumeration class
     * @param   value
     *          the enumeration value as short
     *
     * @exception   IllegalArgumentException
     *              if no short constant is declared for the specified value.
     */
    protected AbstractEnumeration(
        Class enumerationClass,
        short value
    ){
        this.value=value;
        if(
            ! AbstractEnumeration.lenient &&
            getMapping(getClass())[ENUMERATION_TO_STRING].get(this)==null
        ) throw new IllegalArgumentException(
            enumerationClass.getName() + 
            "contains no Short constant with value " + value
        );  
    }

    /**
     * Assuming the specified String represents a specififc enumeration, 
     * constructs AbstractEnumeration object initialized to that value. Throws
     * an exception if the String cannot be parsed as the specific
     * enumeration.
     *
     * @param   enumerationClass
     *          the non-abstract enumeration class
     * @param   value
     *          the enumeration value as String
     *
     * @exception   NullPointerException
     *              If the String does not contain a parsable non-abstract
     *              enumeration value.
     */
    protected AbstractEnumeration(
        Class enumerationClass,
        String value
    ){
        Object object = getMapping(enumerationClass)[
            STRING_TO_ENUMERATION
        ].get(value);
        if(object==null) throw new IllegalArgumentException(
            enumerationClass.getName() + 
            "contains no Short constant named " + value
        );
        this.value=((AbstractEnumeration)object).value;
    }   


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Creates and returns a copy of this object
     */
    public Object clone(
    ) throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     */
    public boolean equals(
        Object that
    ){
        return this.getClass()==that.getClass() &&
        this.value==((AbstractEnumeration)that).value;
    } 

    /**
     * Returns a hash code value for the object.
     */
    public int hashCode(
    ){
        return this.value;
    } 

    /**
     * Returns a string representation of the object.
     */           
    public String toString(
    ){
        return(String)getMapping(getClass())[ENUMERATION_TO_STRING].get(this);
    } 
            

    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * Get the status value.
     *
     * @return  the status value as short.
     */
    public final short shortValue(
    ){
        return this.value;
    }
    
    /**
     * The status value
     */
    private final short value;


    //------------------------------------------------------------------------
    // Class members
    //------------------------------------------------------------------------

    /** 
     *
     */
    private static int ENUMERATION_TO_STRING = 0;

    /** 
     *
     */
    private static int STRING_TO_ENUMERATION = 1;

    /** 
     *
     */
    private static Map mappings=new HashMap();

    /** 
     *
     */
    private static boolean lenient = false;
    
    /**
     * Returns a hash map array containing the enumeration to String and String
     * to enumeration mappings.
     */
    private static synchronized Map[] getMapping(
        Class enumeration
    ){
        Map[] mappings = (Map[])AbstractEnumeration.mappings.get(enumeration);
        if(mappings==null){
            lenient = true;
            try {
                Field[] fields = enumeration.getFields();
                mappings = new Map[]{new HashMap(),new HashMap()};
                Constructor constructor=enumeration.getDeclaredConstructor(
                    new Class[]{short.class}
                );
                for(
                    int i=0; 
                    i<fields.length;
                    i++
                ){
                    Field field=fields[i];
                    if(
                        field.getType() == short.class &&
                        Modifier.isStatic(field.getModifiers()) &&
                        Modifier.isFinal(field.getModifiers())
                    ){
                        Object enumerationValue = constructor.newInstance(
                            field.get(null)
                        );
                        mappings[ENUMERATION_TO_STRING].put(
                            enumerationValue,
                            field.getName()
                        );  
                        mappings[STRING_TO_ENUMERATION].put(
                            field.getName(),
                            enumerationValue
                        );  
                    }
                }
                AbstractEnumeration.mappings.put(enumeration,mappings);
            } catch (Exception exception) {
                mappings = null;
            }
            lenient = false;
        }
        return mappings;
    }
            
}

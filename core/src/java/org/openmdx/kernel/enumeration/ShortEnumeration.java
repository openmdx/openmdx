/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ShortEnumeration.java,v 1.4 2007/10/10 16:06:06 hburger Exp $
 * Description: Short Enumeration
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:06 $
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
package org.openmdx.kernel.enumeration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Short enumeration class
 * 
 * @deprecated use a Java native enum.
 */
public abstract class ShortEnumeration {            

    protected ShortEnumeration(
    ){
        // Avoid instantiation
    }
    
    /**
     * 
     */
    public interface Mapper {

        /**
         * Returns a string representation of a specific enumeration value
         */
        public String toString(
            short value
        );
                
    }
    
    /**
     * 
     */
    protected static class ReflectiveMapper 
        implements Mapper
    {

        /**
         * 
         * @param parent
         * @param current
         */        
        public ReflectiveMapper(
            ReflectiveMapper parent,
            Class current
        ){
            this.parent = parent;
            this.current = current;
            if(current == null) return;
            Field[]  fields = current.getDeclaredFields();
            for(
                int i=0; i<fields.length; i++
           ) try {
               Field field=fields[i];
               if(
                   field.getType() == short.class &&
                   Modifier.isStatic(field.getModifiers()) &&
                   Modifier.isFinal(field.getModifiers())
               ) this.mapping.put(
                   field.get(null),
                   field.getName()
               );
            } catch(Exception exception) {
                // Ignore security exceptions
            }
        }
        
        /**
         * @param value
         */
        private String toString(
            Short value
        ){
            String result = (String)this.mapping.get(value);
            return result != null ? result : 
                parent != null ? parent.toString(value) :
                value.toString(); 
        }

        /**
         * 
         */
        public String toString(
            short value
        ){
            return toString(new Short(value));
        }

        /**
         * @param source
         * 
         * @exception   IllegalArgumentException
         *              if no such voue is found
         */
        private Short findValue(
            String source
        ){
            for(
                Iterator i = this.mapping.entrySet().iterator();
                i.hasNext();
            ){
                Map.Entry e = (Entry)i.next();
                if(e.getValue().equals(source)) return (Short)e.getKey();
            }
            return this.parent != null ? this.parent.findValue(source) : null;
        }

        /**
         * 
         * @param source
         * @return
         * 
         * @exception   IllegalArgumentException
         *              if no such voue is found
         */
        public short toValue(
            String source
        ){
            Short value = findValue(source);
            if(value == null) throw new IllegalArgumentException(
                this.current.getName() + " has no value " + source
            );
            return value.shortValue();
        }

        /**
         * 
         */
        private final ReflectiveMapper parent;

        /**
         * 
         */
        private final Class current;
                
        /**
         * 
         */
        private final Map mapping = new HashMap();
        
    }

    /**
     * 
     */
    protected static ReflectiveMapper mapper = new ReflectiveMapper(null, null);
        
}

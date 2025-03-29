/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Date 
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
package org.w3c.spi2;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openmdx.kernel.text.spi.Parser;
import org.w3c.spi.PrimitiveTypeParsers;
import org.w3c.time.DateTimeConstants;

/**
 * Date
 */
public class Datatypes {

    public static final Class<#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif> DATE_TIME_CLASS
            = #if CLASSIC_CHRONO_TYPES java.util.Date.class #else java.time.Instant.class #endif;

    public static final Class<#if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate #endif> DATE_CLASS
            = #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar.class #else java.time.LocalDate.class #endif;

    public static final Class<#if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration> DURATION_CLASS
            = #if CLASSIC_CHRONO_TYPES javax.xml.datatype.Duration.class #else java.time.Duration.class #endif;

    public static final org.w3c.spi.#if CLASSIC_CHRONO_TYPES ImmutableDatatypeFactory #else ContemporaryChronoDatatypeFactory #endif DATATYPE_FACTORY
            = org.w3c.spi.DatatypeFactories.#if CLASSIC_CHRONO_TYPES immutableDatatypeFactory() #else contemporaryChronoDatatypeFactory() #endif;

    /**
     * Constructor 
     */
    private Datatypes() {
        // Avoid instantiation
    }
    
    /**
     * Create a value from its string representation 
     * 
     * @param valueClass the value's class
     * @param string the values string representation, which can be {@code null}
     * 
     * @return the value, or {@code null} if the value's string was {@code null}
     * 
     * @exception IllegalArgumentException if the string can't be parsed according to the
     * requested type
     */
    public static <V> V create(
        Class<V> valueClass,
        String string
    ){
    	final Parser primitiveTypeParser = PrimitiveTypeParsers.getExtendedParser();
    	return primitiveTypeParser.handles(valueClass) ? primitiveTypeParser.parse(
    		valueClass,
    		string
    	) : create(
    		valueClass, 
    		(Object)string
    	);
    }

    /**
     * Create a structure proxy
     * 
     * @param structureInterface the structure's interface
     * @param values the structure's members
     * 
     * @return a new structure
     */
    public static <S> S create(
        Class<S> structureInterface,
        Object... values
    ){
        return Structures.create(structureInterface, values);
    }        
    
    /**
     * Create a structure proxy
     * 
     * @param structureInterface the structure's interface
     * @param members the structure's members
     * 
     * @return
     */
    public static <S> S create(
        Class<S> structureInterface,
        Structures.Member<?>... members
    ){
        return Structures.create(structureInterface, members);
    }    

    /**
     * Associate a member name with its value
     * 
     * @param name the member's name
     * @param value the members possibly {@code null} value
     * 
     * @return a name/value pair
     */
    public static <T extends Enum<T>> Structures.Member<T> member(
        T name,
        Object value
    ){
        return new Structures.Member<T>(name, value);
    }
        
    /**
     * Create a qualified type name
     * 
     * @param components the qualified type name's components
     * 
     * @return the qualified type name
     */
    public static List<String> typeName(
        String... components
    ){
        return Collections.unmodifiableList(
            Arrays.asList(
                components
            )
        );  
    }
    
}

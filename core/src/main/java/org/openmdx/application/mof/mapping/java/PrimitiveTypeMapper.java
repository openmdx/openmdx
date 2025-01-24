/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: PrimitiveTypeMapper 
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
package org.openmdx.application.mof.mapping.java;

import org.openmdx.base.exception.ServiceException;

/**
 * Primitive Type Mapper
 */
public interface PrimitiveTypeMapper {

    /**
     * Determines the Java equivalent for a given primitive type
     *
     * @param format,            e.g. CCI2, JMI1 etc.
     * @param qualifiedTypeName  the qualified model class name
     * @param asObject           tells whether the object or scalar variant of a Java built-in types is required
     * @param classicChronoTypes tells whether XML or java.time chrono types shall be used
     * @return the Java equivalent for the given primitive type
     */
    String getFeatureType(
        String qualifiedTypeName, 
        JavaExportFormat format,
        boolean asObject,
        boolean classicChronoTypes
    ) throws ServiceException;
    
    /**
     * Determines the Java predicate for a given primitive type
     *
     * @param qualifiedTypeName  the qualified model class name
     * @param classicChronoTypes tells whether XML datatypes or jmi.time shall be used
     * @return the Java predicate for the given primitive type
     */
    String getPredicateType(
        String qualifiedTypeName,
        boolean classicChronoTypes
    ) throws ServiceException;

    /**
     * Provide the Java pattern to parse a given expression represented by the EXPRESSION_PLACE_HOLDER.
     *
     * @param format,            e.g. CCI2, JMI1 etc.
     * @param qualifiedTypeName  the qualified model class name
     * @param asObject           tells whether the object or scalar variant of a Java built-in types is required
     * @param classicChronoTypes tells whether XMI datatype or java.time chrono types are to be used
     * @return the Java pattern to parse a given expression, e.g. "Boolean.valueOf({})"
     * @see #EXPRESSION_PLACEHOLDER
     */
    String getParsePattern(
        String qualifiedTypeName, 
        JavaExportFormat format,
        boolean asObject,
        boolean classicChronoTypes
    ) throws ServiceException;

    /**
     * Maps between the type of the JPA3 property value and the type of the CCI2 property value
     * 
     * @param qualifiedTypeName the qualified model name
     * @param from, either CCI2 or JPA3
     * @param to, either CCI2 or JPA3
     * @return the pattern to map the value from the {@code from} format to the {@code to} format
     * 
     * @throws ServiceException in case of an unsupported combination of arguments
     * 
     * @see #EXPRESSION_PLACEHOLDER
     */
    String getMappingPattern(
        String qualifiedTypeName, 
        JavaExportFormat from,
        JavaExportFormat to
    ) throws ServiceException;
    
    /**
     * The placeholder for the expression inside a pattern
     */
    String EXPRESSION_PLACEHOLDER = "{}";

}
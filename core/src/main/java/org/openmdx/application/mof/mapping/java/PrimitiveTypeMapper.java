/*
 * ==================================================================== 
 * Project: openMDX/Core, http://www.openmdx.org/
 * Name: Primitive Type Mapper
 * Owner: OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as listed in
 * the NOTICE file.
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
     * @param qualifiedTypeName the qualified model class name
     * @param format, e.g. CCI2, JMI1 etc.
     * @param asObject tells whether the object or scalar variant of a Java built-in types is required
     * @return the Java equivalent for the given primitive type
     * 
     * @throws ServiceException
     */
    String getFeatureType(
        String qualifiedTypeName, 
        Format format, 
        boolean asObject
    ) throws ServiceException;
    
    /**
     * Determines the Java predicate for a given primitive type
     * 
     * @param qualifiedTypeName the qualified model class name
     * 
     * @return the Java predicate for the given primitive type
     * 
     * @throws ServiceException
     */
    String getPredicateType(
        String qualifiedTypeName
    ) throws ServiceException;

    /**
     * Provide the Java pattern to parse a given expression represented by the EXPRESSION_PLACE_HOLDER.
     *  
     * @param qualifiedTypeName the qualified model class name
     * @param format, e.g. CCI2, JMI1 etc.
     * @param asObject tells whether the object or scalar variant of a Java built-in types is required
     * @return the Java pattern to parse a given expression, e.g. "Boolean.valueOf({})"
     * 
     * @throws ServiceException
     * 
     * @see {@link #EXPRESSION_PLACEHOLDER}
     */
    String getParsePattern(
        String qualifiedTypeName, 
        Format format, 
        boolean asObject
    ) throws ServiceException;

    /**
     * Maps between the type of the JPA3 property value and the type of the CCI2 property value
     * 
     * @param qualifiedTypeName
     * @param from, either CCI2 or JPA3
     * @param to, either CCI2 or JPA3
     * @return the pattern to map the value from the <code>from</code> format to the <code>to</code> format
     * 
     * @throws ServiceException in case of an unsupported combination of arguments
     * 
     * @see {@link #EXPRESSION_PLACEHOLDER}
     */
    String getMappingPattern(
        String qualifiedTypeName, 
        Format from, 
        Format to
    ) throws ServiceException;
    
    /**
     * The placeholder for the expression inside a pattern
     */
    final String EXPRESSION_PLACEHOLDER = "{}";

}
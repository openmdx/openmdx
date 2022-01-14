/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Mapping_1_0 
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
package org.openmdx.base.accessor.jmi.spi;

import java.lang.reflect.Constructor;

import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
import javax.resource.cci.Record;

import org.openmdx.base.exception.ServiceException;

/**
 * Mapping 1.0
 */
public interface Mapping_1_0 {

    /**
     * Create a package proxy
     * 
     * @param qualifiedName
     * 
     * @return a new package proxy
     * 
     * @throws ServiceException
     */
    RefPackage newPackage(
        Jmi1Package_1_0 outermostPackage,
        String qualifiedName
    ) throws ServiceException;

    /**
     * Create a struct proxy
     * 
     * @param outermostPackage
     * @param qualifiedName
     * 
     * @return a new struct proxy
     * 
     * @throws ServiceException
     */
    RefStruct newStruct(
        Jmi1Package_1_0 outermostPackage,
        Record delegate
    ) throws ServiceException;
    
    /**
     * Retrieve the per RefClass mapping
     * 
     * @param qualifiedClassName
     * 
     * @return
     */
    ClassMapping_1_0 getClassMapping(
        String qualifiedClassName
    ) throws ServiceException;
    
    /**
     * Retrieve the interfaces's MOF class name
     * 
     * @param javaInterface a Java interface
     * 
     * @return the interface's MOF class
     * 
     * @throws ServiceException
     */
    String getModelClassName(
        Class<?> javaInterface
    ) throws ServiceException;

    /**
     * Retrieve instance interface
     * 
     * @param the JPA class
     * 
     * @return the instance interface
     */
    Class<? extends RefObject> getInstanceInterface(
        Class<?> javaClass
    ) throws ServiceException;
    
    /**
     * Retrieve the feature mapper
     * 
     * @param qualifiedClassName
     * @param type
     * 
     * @return the feature mapper
     * 
     * @throws ServiceException
     */
    FeatureMapper getFeatureMapper(
        String qualifiedClassName,
        FeatureMapper.Type type
    ) throws ServiceException;

    /**
     * Retrieve the exception constructor
     * 
     * @param qualifiedExceptionName the exception's id
     * @param qualifiedPackageName the exception's name space
     * 
     * @return the exception constructor
     */
    Constructor<? extends RefException> getExceptionConstructor(
        String qualifiedExceptionName,
        String qualifiedPackageName
    );    
    
}

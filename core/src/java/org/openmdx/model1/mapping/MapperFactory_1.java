/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MapperFactory_1.java,v 1.18 2008/03/21 18:40:16 hburger Exp $
 * Description: PackageExternalizerFactory_1
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:40:16 $
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

/**
 * @author wfro
 */ 
package org.openmdx.model1.mapping;

import java.lang.reflect.InvocationTargetException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.kernel.application.cci.Classes;

/**
 * MapperFactory_1
 */
@SuppressWarnings("unchecked")
public class MapperFactory_1 {

    /**
     * Create a mapper instance
     * 
     * @param format the format, either predefined or as class name
     * 
     * @return the mapper instance specified by <code>format</code>
     * 
     * @throws ServiceException
     */
    @SuppressWarnings("deprecation")
    public static Mapper_1_0 create(
        String format
    ) throws ServiceException {
        try {
            //
            // dynamic loading decouples factory and managed classes. 
            //
            if(MappingTypes.JMI_OPENMDX_1.equals(format)) {
                return (Mapper_1_0) Classes.getApplicationClass(
                    "org.openmdx.compatibility.model1.mapping.java.JMIMapper_1"
                ).getConstructor(
                    MAPPING_FORMAT__PACKAGE_SUFFFIX__FILE_EXTENSION
                ).newInstance(
                    new Object[]{
                        MappingTypes.JMI_OPENMDX_1,
                        "cci",
                        "java"            
                    }
                );
            } else if(
                MappingTypes.CCI2.equals(format) ||
                MappingTypes.JMI1.equals(format) ||
                MappingTypes.JDO2.equals(format)
            ) {
                return (Mapper_1_0) Classes.getApplicationClass(
                    "org.openmdx.model1.mapping.java.Mapper_1"
                ).getConstructor(
                    MAPPING_FORMAT__PACKAGE_SUFFFIX__FILE_EXTENSION
                ).newInstance(
                    new Object[]{
                        format,
                        format,
                        "java"            
                    }
                );
            } else if(
                format.startsWith(MappingTypes.JDO2 + ':')
            ) {
                return (Mapper_1_0) Classes.getApplicationClass(
                    "org.openmdx.model1.mapping.java.Mapper_1"
                ).getConstructor(
                    MAPPING_FORMAT__PACKAGE_SUFFFIX__FILE_EXTENSION
                ).newInstance(
                    new Object[]{
                        format,
                        MappingTypes.JDO2,
                        "java"            
                    }
                );
            } else {
                return (Mapper_1_0) Classes.getApplicationClass(
                    MappingTypes.XMI1.equals(format) || MappingTypes.XMI_OPENMDX_1.equals(format) ? "org.openmdx.model1.mapping.xmi.XMIMapper_1" :
                    MappingTypes.UML_OPENMDX_1.equals(format) ? "org.openmdx.model1.mapping.xmi.Uml1Mapper_1" :
                    MappingTypes.UML2_OPENMDX_1.equals(format) ? "org.openmdx.model1.mapping.xmi.Uml2Mapper_1" :
                    MappingTypes.TOGETHER_OPENMDX_1.equals(format) ? "org.openmdx.model1.mapping.together.TogetherMapper_1" :
                    format
                ).getConstructor(
                    NO_ARGUMENTS
                ).newInstance(
                    (Object[])null
                );
            }
        } catch(ClassNotFoundException e) {
            throw new ServiceException(e);
        } catch(NoSuchMethodException e) {
            throw new ServiceException(e);
        } catch(InvocationTargetException e) {
            throw e.getTargetException() instanceof ServiceException ?
                (ServiceException)e.getTargetException() :
                new ServiceException(e);
        } catch(IllegalAccessException e) {
            throw new ServiceException(e);
        } catch(InstantiationException e) {
            throw new ServiceException(e);
        }
    }
  
    final static private Class[] MAPPING_FORMAT__PACKAGE_SUFFFIX__FILE_EXTENSION = new Class[] {
        String.class, // mappingFormat
        String.class, // packageSuffix,
        String.class // fileExtension
    };
  
    final static private Class[] NO_ARGUMENTS = new Class[]{};

}

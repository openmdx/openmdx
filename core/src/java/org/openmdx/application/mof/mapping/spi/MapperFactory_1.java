/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MapperFactory_1.java,v 1.4 2010/04/16 09:48:32 hburger Exp $
 * Description: MapperFactory_1
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 09:48:32 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.application.mof.mapping.spi;

import java.lang.reflect.InvocationTargetException;

import org.openmdx.application.mof.mapping.cci.Mapper_1_0;
import org.openmdx.application.mof.mapping.cci.MappingTypes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.loading.Classes;

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
            if(
                MappingTypes.CCI2.equals(format) ||
                MappingTypes.JMI1.equals(format) ||
                MappingTypes.JPA3.equals(format)
            ) {
                return Classes.<Mapper_1_0>getApplicationClass(
                    org.openmdx.application.mof.mapping.java.Mapper_1.class.getName()
                ).getConstructor(
                    MAPPING_FORMAT__PACKAGE_SUFFFIX__FILE_EXTENSION
                ).newInstance(
                    format,
                    format,
                    "java"            
                );
            } else if(
                format.startsWith(MappingTypes.JPA3 + ':')
            ) {
                return Classes.<Mapper_1_0>getApplicationClass(
                    org.openmdx.application.mof.mapping.java.Mapper_1.class.getName()
                ).getConstructor(
                    MAPPING_FORMAT__PACKAGE_SUFFFIX__FILE_EXTENSION
                ).newInstance(
                    format,
                    MappingTypes.JPA3,
                    "java"            
                );
            } else {
                return Classes.<Mapper_1_0>getApplicationClass(
                    MappingTypes.XMI1.equals(format) || MappingTypes.XMI_OPENMDX_1.equals(format) ? org.openmdx.application.mof.mapping.xmi.XMIMapper_1.class.getName() :
                    MappingTypes.UML_OPENMDX_1.equals(format) ? org.openmdx.application.mof.mapping.xmi.Uml1Mapper_1.class.getName() :
                    MappingTypes.UML2_OPENMDX_1.equals(format) ? org.openmdx.application.mof.mapping.xmi.Uml2Mapper_1.class.getName() :
                    MappingTypes.TOGETHER_OPENMDX_1.equals(format) ? org.openmdx.application.mof.mapping.together.TogetherMapper_1.class.getName() :
                    format
                ).getConstructor(
                ).newInstance();
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
  
}

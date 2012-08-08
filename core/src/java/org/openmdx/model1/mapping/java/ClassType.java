/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ClassType.java,v 1.6 2007/12/20 18:57:06 wfro Exp $
 * Description: Class Type 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/20 18:57:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
 * All rights reserved.
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
package org.openmdx.model1.mapping.java;

import java.util.Iterator;
import java.util.List;

import org.openmdx.model1.code.Stereotypes;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.Names;

/**
 * Class Type
 */
public enum ClassType {
    
    /**
     * The model class is represented by a CCI interface and a JDO class
     */
    OBJECT {

        public String getParameterType(
            ClassDef classDef,
            Format format
        ){
            if(format == Format.JMI1) { 
                return getType(
                    classDef.getQualifiedName(), 
                    Names.JMI1_PACKAGE_SUFFIX
                );
            }
            else {
                return getType(
                    classDef.getQualifiedName(), 
                    Names.CCI2_PACKAGE_SUFFIX
                );
            }
        }
        
        public String getResultType(
            ClassDef classDef,
            Format format
        ){
            if(format == Format.JMI1) { 
                return getType(
                    classDef.getQualifiedName(), 
                    Names.JMI1_PACKAGE_SUFFIX
                );
            }
            else {
                return getType(
                    classDef.getQualifiedName(), 
                    Names.CCI2_PACKAGE_SUFFIX
                );
            }
        }
        
        public String getMemberType(
            ClassDef classDef,
            Format format
        ){
            if(format == Format.JDO2) {
                return getType(
                    classDef.getQualifiedName(), 
                    Names.JDO2_PACKAGE_SUFFIX
                );
            }
            else if(format == Format.JMI1) { 
                return getType(
                    classDef.getQualifiedName(), 
                    Names.JMI1_PACKAGE_SUFFIX
                );
            }
            else {
                return getType(
                    classDef.getQualifiedName(), 
                    Names.CCI2_PACKAGE_SUFFIX
                );
            }
        }

        public String getInterfaceType(
            ClassDef classDef,
            Format format
        ) {
            return getType(
                classDef.getQualifiedName(), 
                Names.CCI2_PACKAGE_SUFFIX
            );
        }
                
    },
    
    /**
     * The model class is representetd by a CCI interface and has a 
     * super-class represented by a JDO class.
     */
    EXTENSION {

        public String getParameterType(
            ClassDef classDef,
            Format format
        ){
            if(format == Format.JMI1) { 
                return getType(
                    classDef.getQualifiedName(), 
                    Names.JMI1_PACKAGE_SUFFIX
                );
            }
            else {
                return getType(
                    classDef.getQualifiedName(), 
                    Names.CCI2_PACKAGE_SUFFIX
                );
            }
        }
        
        public String getResultType(
            ClassDef classDef,
            Format format
        ){
            if(format == Format.JMI1) { 
                return getType(
                    classDef.getQualifiedName(), 
                    Names.JMI1_PACKAGE_SUFFIX
                );
            }
            else {
                return getType(
                    classDef.getQualifiedName(), 
                    Names.CCI2_PACKAGE_SUFFIX
                );
            }
        }
        
        public String getMemberType(
            ClassDef classDef,
            Format format
        ){
            ClassDef current = classDef;
            ClassDef next;
            while(
                current.isAbstract() &&
                (next = getSuperClassDef(current)) != null
            ){
                current = next;
             }
             return getType(
                 current.getQualifiedName(), 
                 Names.JDO2_PACKAGE_SUFFIX
             );
        }
        
        public String getInterfaceType(
            ClassDef classDef,
            Format format
        ) {
            return getType(
                classDef.getQualifiedName(), 
                Names.CCI2_PACKAGE_SUFFIX
            );            
        }
                
    },
    
    /**
     * The model class is representetd by a CCI interface and has no
     * super-class represented by a JDO class.
     */
    MIXIN {

        public String getParameterType(
            ClassDef classDef,
            Format format
        ){
            if(format == Format.JMI1) { 
                return getType(
                    classDef.getQualifiedName(), 
                    Names.JMI1_PACKAGE_SUFFIX
                );
            }
            else {
                return getType(
                    classDef.getQualifiedName(), 
                    Names.CCI2_PACKAGE_SUFFIX
                );
            }
        }
        
        public String getResultType(
            ClassDef classDef,
            Format format            
        ){
            if(format == Format.JMI1) { 
                return getType(
                    classDef.getQualifiedName(), 
                    Names.JMI1_PACKAGE_SUFFIX
                );
            }
            else {
                return getType(
                    classDef.getQualifiedName(), 
                    Names.CCI2_PACKAGE_SUFFIX
                );
            }
        }
        
        public String getMemberType(
            ClassDef classDef,
            Format format
        ){
            return getType(
                classDef.getQualifiedName(), 
                Names.CCI2_PACKAGE_SUFFIX
            );
        }

        public String getInterfaceType(
            ClassDef classDef,
            Format format
        ) {
            return getType(
                classDef.getQualifiedName(), 
                Names.CCI2_PACKAGE_SUFFIX
            );            
        }
        
    };

    /**
     * Retrieve the qualified member type
     * 
     * @param classDef
     * 
     * @return the qualified member type
     */
    public abstract String getMemberType(
        ClassDef classDef,
        Format format
    );
    
    /**
     * Retrieve the qualified parameter type
     * 
     * @param classDef
     * 
     * @return the qualified parameter type
     */
    public abstract String getParameterType(
        ClassDef classDef,
        Format format
    );
    
    /**
     * Retrieve the qualified result type
     * 
     * @param classDef
     * 
     * @return the qualified result type
     */
    public abstract String getResultType(
        ClassDef classDef,
        Format format
    );
    
    /**
     * Retrieve the qualified interface type
     * 
     * @param classDef
     * 
     * @return the qualified result type
     */
    public abstract String getInterfaceType(
        ClassDef classDef,
        Format format
    );
    
    /**
     * 
     * @param classDef
     * @return
     */
    private static ClassDef getSuperClassDef(
        ClassDef classDef
    ){
        for(
            Iterator i = classDef.getSupertypes().iterator();
            i.hasNext();
         ){
             ClassDef c = (ClassDef) i.next();
             if(!c.getStereotype().contains(Stereotypes.ROOT)) {
                 return c;
             }
        }
        return null;
    }
    
    /**
     * 
     * @param qualifiedName
     * @param packageSuffix
     * @return
     */
    static String getType(
        String qualifiedName,
        String packageSuffix
    ){
        List nameComponents = MapperUtils.getNameComponents(qualifiedName);
        return AbstractMapper.getNamespace(
            MapperUtils.getNameComponents(
                MapperUtils.getPackageName(qualifiedName)
            ), 
            packageSuffix
        ) + '.' + Identifier.CLASS_PROXY_NAME.toIdentifier( 
            (String) nameComponents.get(nameComponents.size()-1)
        );
    }
    
}

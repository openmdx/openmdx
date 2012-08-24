/*
 * ==================================================================== 
 * Project:      openMDX, http://www.openmdx.org
 * Description:  JMIClassLevelTemplate Revision: $Revision: 1.8 $ 
 * Owner:        OMEX AG, Switzerland, http://www.omex.ch 
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.java;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.omg.mof.spi.Identifier;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.metadata.ClassMetaData;
import org.openmdx.application.mof.mapping.java.metadata.FieldMetaData;
import org.openmdx.application.mof.mapping.java.metadata.Visibility;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

public class AbstractClassMapper
    extends AbstractMapper {

    //-----------------------------------------------------------------------
    protected AbstractClassMapper(
        ModelElement_1_0 classDef,        
        Writer writer, 
        Model_1_0 model,
        Format format, 
        String packageSuffix, 
        MetaData_1_0 metaData
    ) throws ServiceException {
        super(
            writer, 
            model,
            format, 
            packageSuffix,
            metaData
        );
        this.classDef = new ClassDef(classDef, model, metaData);
        this.mixIn = this.isRoot(classDef);
        this.className = Identifier.CLASS_PROXY_NAME.toIdentifier(this.classDef.getName());
        this.extendsClassDef = this.classDef.getSuperClassDef(format != Format.JPA3);
        this.classMetaData = (ClassMetaData) this.classDef.getClassMetaData();
        this.spiFeatures = this.classMetaData.getFieldMetaData(Visibility.SPI);
        boolean requiresSlice = this.classMetaData == null || this.classMetaData.isRequiresSlices();
        this.qualifiedClassName = Arrays.asList(this.classDef.getQualifiedName().split(":"));
        if(this.isBaseClass()) {            
            this.baseClassDef = this.classDef;
            ModelElement_1_0 compositeReference = model.getCompositeReference(classDef);            
            this.compositeReference = this.directCompositeReference = compositeReference == null ? 
                null : 
                new ReferenceDef(
                    compositeReference, 
                    model
                );
            this.sliceHolder = requiresSlice; 
        } 
        else {
            this.baseClassDef = this.classDef.getBaseClassDef();
            ClassMetaData superClassMetaData = (ClassMetaData) this.extendsClassDef.getClassMetaData();
            this.directCompositeReference = null;
            ModelElement_1_0 compositeReference = model.getCompositeReference(
                model.getElement(this.baseClassDef.getQualifiedName())
            );
            this.compositeReference = compositeReference == null ? 
                null : 
                new ReferenceDef(
                    compositeReference, 
                    model
                );
            this.sliceHolder = requiresSlice && !(
                superClassMetaData == null || superClassMetaData.isRequiresSlices()
            );
        }
    }
    
    /**
     * Checks whether this class is a sub-class of the one specified by qualifiedName.
     * 
     * @param qualifiedName a qualified class name
     * 
     * @return <code>true</code> if this class is a sub-class of the one specified by qualifiedName
     */
    protected final boolean isInstanceOf(
        String qualifiedName
    ){
        return this.classDef.isInstanceOf(qualifiedName);
    }
    
    /**
     * 
     * @return
     */
    protected final boolean isBaseClass(
    ){
        return this.extendsClassDef == null;        
    }

    /**
     * 
     * @return
     */
    protected final boolean hasContainer(
    ){
        return this.directCompositeReference != null;        
    }

    /**
     * 
     * @return
     */
    protected final boolean isSliceHolder(
    ){
        return this.sliceHolder;        
    }
    
    /**
     * 
     * @param referenceDef
     * @return
     * @throws ServiceException
     */
    protected boolean isTransient(
        ReferenceDef referenceDef
    ) throws ServiceException {
        String qualifiedQualifierTypeName = referenceDef.getQualifiedQualifierTypeName();
        return (
            qualifiedQualifierTypeName != null && 
            !this.model.isPrimitiveType(qualifiedQualifierTypeName)
        ) || TRANSIENT_REFERENCES.contains(referenceDef.getQualifiedTypeName());
    }

    /**
     * Test whether the reference refers to a mix-in class or not.
     * 
     * @param referenceDef
     * 
     * @return <code>true</code> if the reference refers to a mix-in class
     * 
     * @throws ServiceException
     */
    protected boolean isMixIn(
        ReferenceDef referenceDef
    ) throws ServiceException {
        return getClassType(getClassDef(referenceDef.getQualifiedTypeName())) == ClassType.MIXIN;
    }
    
    /**
     * 
     */
    @Override
    protected ClassDef getClassDef(
        String qualifiedName
    ) throws ServiceException {
        return new ClassDef(
            this.model.getElement(qualifiedName), 
            this.model,
            this.metaData
        );
    }

    protected ClassDef getClassDef(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        String qualifiedName = featureDef.getQualifiedName();
        int i = qualifiedName.lastIndexOf(':');
        String className = qualifiedName.substring(0, i);
        return this.getClassDef(className);
    }

    /**
     * 
     * @param qualifiedName
     * @return
     * @throws ServiceException
     */
    protected FieldMetaData getFieldMetaData(
        String qualifiedName
    ) throws ServiceException {
        int i = qualifiedName.lastIndexOf(':');
        String fieldName = qualifiedName.substring(i + 1);
        String className = qualifiedName.substring(0, i);
        ClassMetaData classMetaData = (ClassMetaData) getClassDef(className).getClassMetaData();
        return classMetaData == null ? null : classMetaData.getFieldMetaData(fieldName);
    }

    /**
     * Retrieve className.
     *
     * @return Returns the className.
     */
     public String getClassName() {
         return this.className;
     }

    /**
     * Tells whether the current class a mix-in class
     * 
     * @return <code>true</code> if the current class has the 
     * <code>&laquo;root&raquo;</code> stereotype
     */
    protected boolean isMixIn(){
        return this.mixIn;
    }
    
    /**
     * 
     */
    private final boolean mixIn;
    
    /**
     * 
     */
    protected final ClassDef classDef;
    
    /**
     * 
     */
    protected final String className;
    
    /**
     * 
     */
    protected final ClassMetaData classMetaData;
    
    /**
     * 
     */
    protected final Collection<FieldMetaData> spiFeatures;
    
    /**
     * 
     */
    protected final ClassDef extendsClassDef;

    /**
     * 
     */
    protected final ClassDef baseClassDef;

    /**
     * 
     */
    private final boolean sliceHolder;

    /**
     * Direct composite reference
     */
    protected final ReferenceDef directCompositeReference;

    /**
     * Direct or indirect comppsite reference
     */
    protected final ReferenceDef compositeReference;

    /**
     * 
     */
    protected final List<String> qualifiedClassName;
    
    /**
     * References to the following object types are considered to be transient
     */
    private static final Set<String> TRANSIENT_REFERENCES = Collections.singleton(
        "org:openmdx:base:ExtentCapable"
    );

}

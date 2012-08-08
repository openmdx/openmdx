/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractMapper.java,v 1.44 2008/02/16 19:40:00 hburger Exp $
 * Description: JMITemplate 
 * Revision:    $Revision: 1.44 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/16 19:40:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2006, OMEX AG, Switzerland
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
package org.openmdx.model1.mapping.java;

import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.code.Stereotypes;
import org.openmdx.model1.importer.metadata.Visibility;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.ElementDef;
import org.openmdx.model1.mapping.MapperTemplate;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MetaData_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.OperationDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

// ---------------------------------------------------------------------------
public abstract class AbstractMapper
    extends MapperTemplate {
    
    // -----------------------------------------------------------------------
    protected AbstractMapper(
        Writer writer,
        Model_1_3 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData
    ) {
        super(
            writer,
            model
        );
        this.metaData = metaData;
        this.format = format;
        this.packageSuffix = packageSuffix;
    }

    // -----------------------------------------------------------------------
    /**
     * 
     */
    protected Model_1_3 getModel(){
        return (Model_1_3) super.model;
    }
    
    // -----------------------------------------------------------------------
    protected Format getFormat(){
        return this.format;
    }

    // -----------------------------------------------------------------------
    protected String getReturnType(
        OperationDef featureDef
    ) throws ServiceException {
        ClassDef resultTypeDef = new ClassDef(
            this.model.getElement(featureDef.getQualifiedReturnTypeName()),
            this.model
        );
        ClassType resultType = this.getClassType(resultTypeDef);
        return resultType.getResultType(resultTypeDef, this.getFormat());
    }
    
    // -----------------------------------------------------------------------
    protected String getParameterType(
        OperationDef featureDef
    ) throws ServiceException {
        ClassDef parameterTypeDef = new ClassDef(
            this.model.getElement(featureDef.getQualifiedInParameterTypeName()),
            this.model
        );
        ClassType parameterType = this.getClassType(parameterTypeDef);
        return parameterType.getParameterType(parameterTypeDef, this.getFormat());
    }
    
    // -----------------------------------------------------------------------
    protected String getType(
        StructuralFeatureDef featureDef, 
        String collectionClass, 
        Boolean returnValue
    ) throws ServiceException {
        return this.getType(
            featureDef.getQualifiedTypeName(),
            collectionClass,
            returnValue
        );
    }

    // -----------------------------------------------------------------------
    protected String printAnnotationAndReturnCast(
        StructuralFeatureDef featureDef, 
        String collectionClass
    ) throws ServiceException {
        String qualifiedTypeName = featureDef.getQualifiedTypeName();
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            return "";
        } else { 
            if(getFormat() == Format.JDO2) {
                this.pw.println("  @SuppressWarnings({\"unchecked\",\"cast\"})");
            } 
            return '(' + (
                collectionClass == null ? "T" : collectionClass + "<T>"
            ) + ')';
        }
    }

    // -----------------------------------------------------------------------
    private String getType(
        String qualifiedTypeName, 
        String collectionClass, 
        Boolean returnValue
    ) throws ServiceException {
        boolean multiValued = collectionClass != null;
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            String javaType = this.getObjectType(qualifiedTypeName);
            return (
               getFormat() == Format.JDO2 && Boolean.TRUE.equals(returnValue) ? "final " : ""
            ) + (
               multiValued ? collectionClass + '<' + javaType + '>' : javaType
            );
        } else if(returnValue == null) {
            ClassDef classDef = this.getClassDef(qualifiedTypeName);
            Format format = this.getFormat();
            String javaType = this.getClassType(classDef).getMemberType(
                classDef, 
                getFormat() == Format.JDO2 && this.model.isStructureType(qualifiedTypeName) ? Format.CCI2 : format
            ); 
            return multiValued ? collectionClass + '<' + javaType + '>' : javaType;
        } else {
            String javaType = this.interfaceType(
                qualifiedTypeName, 
                org.openmdx.model1.importer.metadata.Visibility.CCI, 
                returnValue
            );
            if(returnValue) {
                return "<T extends " + javaType + "> " + (
                    multiValued ? collectionClass + "<T>" : "T"
                );
            } 
            else {
                return multiValued 
                    ? collectionClass + "<? extends " + javaType + '>' 
                    : javaType; 
            }
        }
    }
    
    // -----------------------------------------------------------------------
    protected String printAnnotationAndReturnMapCast(
        StructuralFeatureDef featureDef, 
        Class<?> keyClass
    ) throws ServiceException {
        String qualifiedTypeName = featureDef.getQualifiedTypeName();
        if(this.model.isPrimitiveType(qualifiedTypeName)){
            return "";
        } else { 
            if(getFormat() == Format.JDO2) {
                this.pw.println("  @SuppressWarnings(\"unchecked\")");
            } 
            return "(java.util.Map<" + keyClass.getName() + ", T>)";
        }
    }

    // -----------------------------------------------------------------------
    protected String getMapType(
        StructuralFeatureDef featureDef, 
        Class<?> keyClass, 
        Boolean returnValue
    ) throws ServiceException {
        String qualifiedTypeName = featureDef.getQualifiedTypeName();
        String elementTypeName = this.getObjectType(qualifiedTypeName);
        String keyTypeName = keyClass.getName();
        if(
            returnValue != null && 
            !this.model.isPrimitiveType(qualifiedTypeName)
        ) {
            if(returnValue) {
                return "<T extends " + elementTypeName + "> " + "java.util.Map<" + keyTypeName +",T>"; 
            } else {
                return "java.util.Map<" + keyTypeName +",? extends " + elementTypeName + '>'; 
            }
        } else {
            return "java.util.Map<" + keyTypeName +',' + elementTypeName + '>';
        }
    }
    
    // -----------------------------------------------------------------------
    protected String getFeatureType(
        StructuralFeatureDef featureDef, 
        Boolean returnType
    ) throws ServiceException {
        String multiplicity = featureDef.getMultiplicity();
        String type = featureDef.getQualifiedTypeName();
        return 
            Multiplicities.OPTIONAL_VALUE.equals(multiplicity) ? (
                this.model.isPrimitiveType(type) ? this.getObjectType(type) : getType(type, null, returnType) 
            ) :
            Multiplicities.SINGLE_VALUE.equals(multiplicity) ? ( 
                this.model.isPrimitiveType(type) ? this.getType(type) : getType(type, null, returnType) 
            ) :
            Multiplicities.LIST.equals(multiplicity) ? this.getType(featureDef, "java.util.List", returnType) :
            Multiplicities.SET.equals(multiplicity) ? this.getType(featureDef, "java.util.Set", returnType) :
            Multiplicities.SPARSEARRAY.equals(multiplicity) ? this.getType(featureDef, "org.w3c.cci2.SparseArray", returnType) :
            Multiplicities.STREAM.equals(multiplicity) ? (
                PrimitiveTypes.BINARY.equals(type) ? "java.io.InputStream" :
                PrimitiveTypes.STRING.equals(type) ? "java.io.Reader" :
                "java.io.DataInput" 
            ) : this.getType(featureDef, "java.util.List", returnType);
    }
    
    // -----------------------------------------------------------------------
    protected void mapParameter(
        String prefix,
        StructuralFeatureDef featureDef, 
        String suffix
    ) throws ServiceException {
        this.pw.println(
            prefix +
            getFeatureType(featureDef, Boolean.FALSE) +
            ' ' +
            this.getFeatureName(featureDef) +
            suffix
        );
    }
    
    // -----------------------------------------------------------------------
    protected void mapGetMember(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.pw.println("  " + accessModifier + ' ' + this.getFeatureType(featureDef, true) + ' ' + this.getMethodName(featureDef.getBeanGetterName()) + "(");
        this.pw.println("  ) {");
        this.pw.println("    return this." + this.getFeatureName(featureDef) + ';');
        this.pw.println("  }");
        this.pw.println();        
    }

    // -----------------------------------------------------------------------
    protected void mapInitializeMember(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        String name = this.getFeatureName(featureDef);
        this.pw.println("    this." + name + " = " + name + ';');
    }
    
    // -----------------------------------------------------------------------
    protected void mapInitializeMember(
        StructuralFeatureDef featureDef, 
        String source
    ) throws ServiceException {
        this.pw.println(
            "    this." + this.getFeatureName(featureDef) + " = " + 
            source + '.' + this.getMethodName(featureDef.getBeanGetterName()) + "();"
        );
    }
    
    // -----------------------------------------------------------------------
    public void mapGetFeatureUsingObjectType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        if (Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + ' ' + this.getObjectType(featureDef.getQualifiedTypeName()) + ' ' + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getObjectType(featureDef.getQualifiedTypeName()) +  ")this.refGetValue(\"" + featureDef.getName() + "\", 0);");
            this.pw.println("  }");
        } 
        else if (Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef.getQualifiedTypeName()) );
            this.pw.println(featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getObjectType(featureDef.getQualifiedTypeName()) +  ")this.refGetValue(\"" + featureDef.getName() + "\", 0);");
            this.pw.println("  }");
        } else if (Multiplicities.LIST.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef, "java.util.List", Boolean.TRUE) + ' ' + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getType(featureDef, "java.util.List", Boolean.FALSE) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        } else if (Multiplicities.SET.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef, "java.util.Set", Boolean.TRUE) + ' ' + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getType(featureDef, "java.util.Set", Boolean.FALSE) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        } else if (Multiplicities.MAP.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + ' ' + this.getMapType(featureDef, java.lang.String.class, true) + ' ' + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getMapType(featureDef, java.lang.String.class, false) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        } else if (Multiplicities.SPARSEARRAY.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef, "org.w3c.cci2.SparseArray", Boolean.TRUE) + ' ' + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getType(featureDef, "org.w3c.cci2.SparseArray", Boolean.FALSE) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        } else if (Multiplicities.STREAM.equals(featureDef.getMultiplicity())) {
            if (PrimitiveTypes.BINARY.equals(featureDef.getQualifiedTypeName())) {
                this.pw.println("  " + accessModifier + " java.io.InputStream " + featureDef.getBeanGetterName() + " (");
                this.pw.println("  ) {");
                this.pw.println("    return (java.io.InputStream)this.refGetValue(\"" + featureDef.getName() + "\", 0);");
                this.pw.println("  }");
            } else if (PrimitiveTypes.STRING.equals(featureDef.getQualifiedTypeName())) {
                this.pw.println("  " + accessModifier + " java.io.Reader " + featureDef.getBeanGetterName() + " (");
                this.pw.println("  ) {");
                this.pw.println("    return (java.io.Reader)this.refGetValue(\"" + featureDef.getName() + "\", 0);");
                this.pw.println("  }");
            } else {
                this.pw.println("  " + accessModifier + " java.io.DataInput " + featureDef.getBeanGetterName() + " (");
                this.pw.println("  ) {");
                this.pw.println("    return (java.io.DataInput)this.refGetValue(\"" + featureDef.getName() + "\", 0);");
                this.pw.println("  }");
            }
        } else {
            this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef, "java.util.List", Boolean.TRUE) + ' ' + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getType(featureDef, "java.util.List", Boolean.FALSE) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        }
    }
    
    // -----------------------------------------------------------------------
    public void mapGetFeatureIndexedUsingNativeType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef.getQualifiedTypeName()) + ' ' + this.getMethodName(featureDef.getBeanGetterName()) + "(");
        this.pw.println("    int index");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(featureDef.getQualifiedTypeName())) {
            this.pw.println("    return ((java.lang.Boolean)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index)).booleanValue();");
        } else if (PrimitiveTypes.DATETIME.equals(featureDef
            .getQualifiedTypeName())) {
            this.pw.println("    return (java.util.Date)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index);");
        } else if (
            PrimitiveTypes.DATE.equals(featureDef.getQualifiedTypeName()) ||
            PrimitiveTypes.DURATION.equals(featureDef.getQualifiedTypeName()) ||
            PrimitiveTypes.ANYURI.equals(featureDef.getQualifiedTypeName())             
        ) {
            this.pw.println(
                "    return (" +
                getType(featureDef.getQualifiedTypeName()) +
                ")this.refGetValue(\"" + 
                featureDef.getQualifiedName() + 
                "\", index);"
            );
        } else if (
            PrimitiveTypes.INTEGER.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.SHORT.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.LONG.equals(featureDef.getQualifiedTypeName())
        ) {
            this.pw.println("    return ((java.lang.Number)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index))."
                + this.getType(featureDef.getQualifiedTypeName())
                + "Value();");
        } else {
            this.pw.println("    return ("
                + this.getType(featureDef.getQualifiedTypeName())
                + ")this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index);");
        }
        this.pw.println("  }");
        this.pw.println("");
    }
     
    // -----------------------------------------------------------------------
    public void mapGetFeatureKeyedUsingNativeType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef.getQualifiedTypeName()) + ' ' + this.getMethodName(featureDef.getBeanGetterName()) + "(");
        this.pw.println("    String key");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(featureDef.getQualifiedTypeName())) {
            this.pw
                .println("    return ((java.lang.Boolean)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key)).booleanValue();");
        } else if (PrimitiveTypes.DATETIME.equals(featureDef
            .getQualifiedTypeName())) {
            this.pw
                .println("    return (java.util.Date)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key);");
        } else if (
            PrimitiveTypes.DATE.equals(featureDef.getQualifiedTypeName()) ||
            PrimitiveTypes.DURATION.equals(featureDef.getQualifiedTypeName()) ||
            PrimitiveTypes.ANYURI.equals(featureDef.getQualifiedTypeName())             
        ) {
            this.pw.println(
                "    return (" +
                getType(featureDef.getQualifiedTypeName()) +
                ")this.refGetValue(\"" + 
                featureDef.getQualifiedName() + 
                "\", key);"
            );
        } else if (
            PrimitiveTypes.INTEGER.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.SHORT.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.LONG.equals(featureDef.getQualifiedTypeName())
        ) {
            this.pw.println(
                "    return ((java.lang.Number)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key))." +
                this.getType(featureDef.getQualifiedTypeName()) +
                "Value();"
            );
        } else {
            this.pw.println("    return ("
                + this.getType(featureDef.getQualifiedTypeName())
                + ")this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key);");
        }
        this.pw.println("  }");
        this.pw.println("");        
    }

    // -----------------------------------------------------------------------
    /**
     * Maps a list of namespace name components to a namespace name in the
     * destination language.
     */
    public String getNamespace(
        List<String> namespaceNameComponents
    ) {
        return getNamespace(
            namespaceNameComponents,
            this.packageSuffix
        );
    }
    
    /**
     * Maps a list of namespace name components to a namespace name in the
     * destination language.
     */
    public static String getNamespace(
        List<String> namespaceNameComponents,
        String packageSuffix
    ) {
        StringBuffer namespace = new StringBuffer();
        for(
            int i = 0, iLimit = namespaceNameComponents.size(); 
            i < iLimit; 
            i++
        ) Names.openmdx2NamespaceElement(
            i == 0 ? namespace : namespace.append('.'),
            namespaceNameComponents.get(i)
        );
        return namespace.append(
            '.'
        ).append(
            packageSuffix
        ).toString();
    }
    
    // -----------------------------------------------------------------------
    /**
     * Maps a primitive type to a primitive type of the target platform
     */
    protected String getObjectType(
        String qualifiedTypeName
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            if(PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                return "java.lang.Boolean";
            }
            if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                return "java.lang.Short";
            }
            if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                return "java.lang.Long";
            }
            if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                return "java.lang.Integer";
            }
            else {
                return this.getType(qualifiedTypeName);
            }
        }
        else {
            return this.getType(qualifiedTypeName);
        }
    }

    // -----------------------------------------------------------------------
    protected String interfaceType(
        String qualifiedTypeName, 
        Visibility visibility,
        boolean returnValue
    ) throws ServiceException {
        ClassDef interfaceTypeDef = new ClassDef(
            this.model.getElement(qualifiedTypeName), 
            this.model
        );
        ClassType interfaceType = this.getClassType(interfaceTypeDef);
        return returnValue
            ? interfaceType.getResultType(interfaceTypeDef, this.getFormat())
            : interfaceType.getInterfaceType(interfaceTypeDef, this.getFormat());
    }

    // -----------------------------------------------------------------------
    protected String interfaceType(
        ElementDef elementDef, 
        Visibility visibility,
        boolean returnValue
    ) throws ServiceException {
        return this.interfaceType(
            elementDef.getQualifiedName(), 
            visibility,
            returnValue
        );
    }
    
    // -----------------------------------------------------------------------
    protected String getPackageSuffix(
        Visibility visbility
    ){
        switch(visbility) {
            case CCI : return Names.CCI2_PACKAGE_SUFFIX;
            case SPI : return Names.SPI2_PACKAGE_SUFFIX;
            default: return null;
        }
    }
    
    // -----------------------------------------------------------------------
    protected String getType(
        OperationDef operationDef
    ) throws ServiceException {
        return "org:openmdx:base:Void".equals(operationDef.getQualifiedReturnTypeName()) ?
            "void" :
            getType(operationDef.getQualifiedReturnTypeName());
    }
    
    // -----------------------------------------------------------------------
    protected String cciType(
        OperationDef operationDef
    ) throws ServiceException {
        return "org:openmdx:base:Void".equals(operationDef.getQualifiedReturnTypeName()) ?
            "void" :
            interfaceType(operationDef.getQualifiedReturnTypeName(), org.openmdx.model1.importer.metadata.Visibility.CCI, false);
    }
    
    // -----------------------------------------------------------------------
    protected String getQueryType(
        String qualifiedTypeName, 
        String targetNamspace
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            return PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.BooleanTypePredicate"
            ) : PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.lang.Short>"
            ) : PrimitiveTypes.LONG.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.lang.Long>" 
            ) : PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? ( 
                "org.w3c.cci2.ComparableTypePredicate<java.lang.Integer>" 
            ) : PrimitiveTypes.STRING.equals(qualifiedTypeName) ? (                      
                "org.w3c.cci2.StringTypePredicate" 
            ) : (
                PrimitiveTypes.ANYURI.equals(qualifiedTypeName) 
            ) ? ( 
                "org.w3c.cci2.ResourceIdentifierTypePredicate<java.net.URI>" 
            ) : (
                PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName) ||
                PrimitiveTypes.XRI.equals(qualifiedTypeName) 
            ) ? ( 
                "org.w3c.cci2.StringTypePredicate" 
            ) : PrimitiveTypes.DATETIME.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.util.Date>"
            ) : PrimitiveTypes.DATE.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.PartiallyOrderedTypePredicate<javax.xml.datatype.XMLGregorianCalendar>"
            ) : PrimitiveTypes.DURATION.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.PartiallyOrderedTypePredicate<javax.xml.datatype.Duration>"
            ) : PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.math.BigDecimal>"
            ) : "org.w3c.cci2.AnyTypePredicate";   
        } else {
            List<String> nameComponents = MapperUtils.getNameComponents(qualifiedTypeName);
            String namespace = getNamespace(
                MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedTypeName)),
                Names.CCI2_PACKAGE_SUFFIX
            );
            return (
                namespace.equals(targetNamspace) ? "" : namespace + '.'
            ) + Identifier.CLASS_PROXY_NAME.toIdentifier( 
                nameComponents.get(nameComponents.size()-1), // modelElementName
                null, // removablePrefix, 
                null, // prependablePrefix
                null
, "Query" // appendableSuffix
            );
        }
    }

    protected String getParseExpression(
        String qualifiedTypeName, 
        String multiplicity,
        String expression
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                return PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? (
                    expression + " == null ? null : java.lang.Boolean.valueOf(" + expression + ")"
                ) : PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? (
                    expression + " == null ? null : java.lang.Short.valueOf(" + expression + ")"
                ) : PrimitiveTypes.LONG.equals(qualifiedTypeName) ? (
                    expression + " == null ? null : java.lang.Long.valueOf(" + expression + ")"
                ) : PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? ( 
                    expression + " == null ? null : java.lang.Integer.valueOf(" + expression + ")"
                ) : PrimitiveTypes.STRING.equals(qualifiedTypeName) ? (                      
                    expression
                ) : PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? ( 
                    expression + " == null ? null : java.net.URI.create(" + expression + ")"
                ) : PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? (
                    expression + " == null ? null : new java.math.BigDecimal(" + expression + ")"    
                ) : "null // TODO support parsing of type " + qualifiedTypeName; 
            } else if (Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
                return PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? (
                    "java.lang.Boolean.parseBoolean(" + expression + ")"
                ) : PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? (
                    "java.lang.Short.parseShort(" + expression + ")"
                ) : PrimitiveTypes.LONG.equals(qualifiedTypeName) ? (
                    "java.lang.Long.parseLong(" + expression + ")"
                ) : PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? ( 
                    "java.lang.Integer.parseInt(" + expression + ")"
                ) : PrimitiveTypes.STRING.equals(qualifiedTypeName) ? (                      
                    expression
                ) : PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? ( 
                    "java.net.URI.create(" + expression + ")"
                ) : PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? (
                    "new java.math.BigDecimal(" + expression + ")"    
                ) : "null // TODO support parsing of of type " + qualifiedTypeName; 
            } else {
                return "null // TODO support parsing of multi-valued arguments";
            }
        } else {
            return "null // TODO support parsing of non-primitive arguments";
        }
    }
    
    // -----------------------------------------------------------------------

    protected String getType(
        String qualifiedTypeName
    ) throws ServiceException {        
        return getType(
            qualifiedTypeName,
            this.model.isStructureType(qualifiedTypeName) && getFormat() == Format.JDO2
        );
    }

    // -----------------------------------------------------------------------
    
    protected String getType(
        String qualifiedTypeName,
        boolean cci
    ) throws ServiceException {        
        return 
            this.model.isPrimitiveType(qualifiedTypeName) ? 
                getPrimitiveType(qualifiedTypeName) :
            cci ? 
               this.interfaceType(qualifiedTypeName, org.openmdx.model1.importer.metadata.Visibility.CCI, false) : 
               this.getModelType(qualifiedTypeName);
    }

    // -----------------------------------------------------------------------

    protected String getObjectType(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return getObjectType(featureDef.getQualifiedTypeName());
    }
    
    // -----------------------------------------------------------------------
    protected static String getPrimitiveType(
        String qualifiedTypeName
    ){
        return 
            PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? "boolean" :
            PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? "short" :
            PrimitiveTypes.LONG.equals(qualifiedTypeName) ? "long" :
            PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? "int" :
            PrimitiveTypes.BINARY.equals(qualifiedTypeName) ? "byte[]" :
            PrimitiveTypes.STRING.equals(qualifiedTypeName) ? "java.lang.String" :
            PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName) ? "java.lang.Object" :
            PrimitiveTypes.DATETIME.equals(qualifiedTypeName) ? "java.util.Date" :
            PrimitiveTypes.DATE.equals(qualifiedTypeName) ? "javax.xml.datatype.XMLGregorianCalendar" :
            PrimitiveTypes.DURATION.equals(qualifiedTypeName) ? "javax.xml.datatype.Duration" :
            PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? "java.math.BigDecimal" :
            PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? "java.net.URI" :
            "java.lang.String";
    }
    
    // -----------------------------------------------------------------------
    protected String getModelType(
        String qualifiedTypeName
    ) throws ServiceException {
        List<String> nameComponents = MapperUtils.getNameComponents(qualifiedTypeName);
        return AbstractMapper.getNamespace(
            MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedTypeName)),
            this.format == Format.JDO2 && 
            getModel().getElement(qualifiedTypeName).values("stereotype").contains(Stereotypes.ROOT) ? 
                Names.CCI2_PACKAGE_SUFFIX : 
                this.packageSuffix
        ) + '.' + Identifier.CLASS_PROXY_NAME.toIdentifier( 
            nameComponents.get(nameComponents.size()-1)
        );
    }

    // -----------------------------------------------------------------------
    protected String getOrderType(
        StructuralFeatureDef featureDef
    ){
        return
            Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity()) ||
            Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity()) ?
            "org.w3c.cci2.SimpleTypeOrder" :
            "org.w3c.cci2.MultivaluedTypeOrder";
    }

    // -----------------------------------------------------------------------
    /**
     * Maps a method name to a method name that does not collide with an
     * operation name in the root object class in the destination language (e.g.
     * java.lang.Object in Java). This mapping is needed to avoid that generated
     * methods accidentally override methods inherited from the root object
     * class.
     */
    public String getMethodName(
        String methodName
    ) {
        return Identifier.OPERATION_NAME.toIdentifier(methodName);
    }
    
    // -----------------------------------------------------------------------
    /**
     * Maps a parameter name to a parameter name that does not collide with a
     * keyword name in the destination language
     */
    public String getFeatureName(
        StructuralFeatureDef featureDef
    ) {
        return getFeatureName(featureDef.getName());
    }

    // -----------------------------------------------------------------------
    /**
     * Maps a parameter name to a parameter name that does not collide with a
     * keyword name in the destination language
     */
    protected String getFeatureName(
        String featureName
    ) {
        return Identifier.ATTRIBUTE_NAME.toIdentifier(featureName);
    }

    // -----------------------------------------------------------------------
    /**
     * 
     */
    public String getPredicateName(
        String prefix,
        StructuralFeatureDef fieldDef
    ){
        return Identifier.OPERATION_NAME.toIdentifier(
            (prefix == null ? "" : prefix + ' ') + fieldDef.getBeanGenericName()
        );
    }
    
    // -----------------------------------------------------------------------
    public void arrayToList(
        StructuralFeatureDef featureDef,
        String listName,
        String varName
    ) throws ServiceException {
        this.pw.println("    java.util.List " + listName + " = new java.util.ArrayList();");
        this.pw.println("    for(int i = 0; i < " + varName + ".length; i++) {");
        this.pw.println("      " + listName + ".add(");
        if (
            PrimitiveTypes.INTEGER.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.SHORT.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.LONG.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.BOOLEAN.equals(featureDef.getQualifiedTypeName()) 
        ) {
            this.pw.println("        new " + this.getObjectType(featureDef.getQualifiedTypeName()) + "(" + varName + "[i])");
        }
        else {
            this.pw.println("        " + varName + "[i]");
        }
        this.pw.println("      );");
        this.pw.println("    }");
    }
    
    // -----------------------------------------------------------------------
    public void fileHeader(
    ) {
        this.pw.println("//////////////////////////////////////////////////////////////////////////////");
        this.pw.println("//");
        this.pw.println("// Name: $Id: AbstractMapper.java,v 1.44 2008/02/16 19:40:00 hburger Exp $");
        this.pw.println("// Generated by: openMDX JMI Mapper");
        this.pw.println("// Date: " + new Date().toString());
        this.pw.println("//");
        this.pw.println("// GENERATED - DO NOT CHANGE MANUALLY");
        this.pw.println("//");
        this.pw.println("//////////////////////////////////////////////////////////////////////////////");
    }

    // -----------------------------------------------------------------------
    public void trace(
        String text
    ) {
        this.pw.println("// ----------------------------------------------------------------------------");
        this.pw.println("// " + text);
        this.pw.println("// ----------------------------------------------------------------------------");
    }

    // -----------------------------------------------------------------------
    protected ClassType getClassType(
        ClassDef classDef
    ) throws ServiceException{
        return 
            classDef.getStereotype().contains(Stereotypes.ROOT) ? ClassType.MIXIN :
            classDef.isAbstract() && classDef.getSuperClassDef(true) != null ? ClassType.EXTENSION :
            ClassType.OBJECT;
    }

    // -----------------------------------------------------------------------
    protected boolean isRoot(
        ModelElement_1_0 classDef
    ) throws ServiceException{
        return 
            classDef != null &&
            classDef.values("stereotype").contains(Stereotypes.ROOT);
    }

    // -----------------------------------------------------------------------
    protected boolean isRoot(
        String qualifiedTypeName
    ) throws ServiceException{
        return isRoot(
            this.model.findElement(qualifiedTypeName)
        );
    }

    // -----------------------------------------------------------------------
    protected ClassDef getClassDef(
        String qualifiedName
    ) throws ServiceException{
        return new ClassDef(
            this.model.getElement(qualifiedName),
            this.model,
            this.metaData
            
        ); 
    }
    
    // -----------------------------------------------------------------------
    protected static boolean isDerived(
        ModelElement_1_0 modelElement
    ){
        return Boolean.TRUE.equals(modelElement.values("isDerived").get(0));
    }
    
    // -----------------------------------------------------------------------
    
    /**
     * 
     */
    private final String packageSuffix;

    /**
     * 
     */
    private final Format format;
    
    /**
     * 
     */
    protected final MetaData_1_0 metaData;
    
}

// --- End of File -----------------------------------------------------------

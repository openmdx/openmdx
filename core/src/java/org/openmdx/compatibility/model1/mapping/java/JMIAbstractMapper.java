/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIAbstractMapper.java,v 1.14 2008/04/21 16:57:12 hburger Exp $
 * Description: JMITemplate 
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/21 16:57:12 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.compatibility.model1.mapping.java;

import java.io.Writer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmdx.base.Version;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.mapping.MapperTemplate;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MappingTypes;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.StructuralFeatureDef;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public abstract class JMIAbstractMapper
    extends MapperTemplate {
    
    /**
     * Mapper's id callback.
     * 
     * @return the mapper's id
     */
    protected abstract String mapperId();
    
    //-----------------------------------------------------------------------
    public JMIAbstractMapper(
        Writer writer,
        Model_1_3 model,
        String format, 
        String packageSuffix
    ) {
        super(
            writer,
            model
        );
        this.format = format;
        this.packageSuffix = packageSuffix;
        this.generics = !format.equals(MappingTypes.JMI_OPENMDX_1);    
    }

    //-----------------------------------------------------------------------
    protected String getSetType(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return "java.util.Set";
    }
    
    //-----------------------------------------------------------------------
    protected String getListType(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return "java.util.List";
    }
    
    //-----------------------------------------------------------------------
    protected String getCollectionType(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return "java.util.Collection";
    }
    
    //-----------------------------------------------------------------------
    protected String getSparseArrayType(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return "java.util.SortedMap";
    }
    
    //-----------------------------------------------------------------------
    protected String getMapType(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return "java.util.Map";
    }
    
    //-----------------------------------------------------------------------
    protected String getContainerType(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return "org.oasisopen.jmi1.RefContainer";
    }
    
    //-----------------------------------------------------------------------
    protected void mapParameter(
        String separator,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        if (Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println(separator + this.getObjectType(featureDef.getQualifiedTypeName()) + " " + this.getParamName(featureDef.getName()) + "");
        } else if (Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println(separator + this.getType(featureDef.getQualifiedTypeName()) + " " + this.getParamName(featureDef.getName()) + "");
        } else if (Multiplicities.LIST.equals(featureDef.getMultiplicity())) {
            this.pw.println(separator + this.getListType(featureDef) + " " + this.getParamName(featureDef.getName()) + "");
        } else if (Multiplicities.SET.equals(featureDef.getMultiplicity())) {
            this.pw.println(separator + this.getSetType(featureDef) + " " + this.getParamName(featureDef.getName()) + "");
        } else if (Multiplicities.SPARSEARRAY.equals(featureDef.getMultiplicity())) {
            this.pw.println(separator + this.getCollectionType(featureDef) + " " + this.getParamName(featureDef.getName()) + "");
        } else if (Multiplicities.STREAM.equals(featureDef.getMultiplicity())) {
            if (PrimitiveTypes.BINARY.equals(featureDef.getQualifiedTypeName())) {
                this.pw.println(separator + "java.io.InputStream " + this.getParamName(featureDef.getName()) + "");
            } else if (PrimitiveTypes.STRING.equals(featureDef.getQualifiedTypeName())) {
                this.pw.println(separator + "java.io.Reader " + this.getParamName(featureDef.getName()) + "");
            } else {
                this.pw.println(separator + "java.io.DataInput " + this.getParamName(featureDef.getName()) + "");
            }
            this.pw.println("");
        } else {
            this.pw.println(separator + this.getListType(featureDef) + " " + this.getParamName(featureDef.getName()) + "");
        }        
    }
    
    //-----------------------------------------------------------------------
    public void mapImplGetFeatureUsingObjectType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        if (Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + " " + this.getObjectType(featureDef.getQualifiedTypeName()) + " " + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getObjectType(featureDef.getQualifiedTypeName()) +  ")this.refGetValue(\"" + featureDef.getName() + "\", 0);");
            this.pw.println("  }");
        } 
        else if (Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + " " + this.getType(featureDef.getQualifiedTypeName()) );
            this.pw.println(featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getObjectType(featureDef.getQualifiedTypeName()) +  ")this.refGetValue(\"" + featureDef.getName() + "\", 0);");
            this.pw.println("  }");
        } else if (Multiplicities.LIST.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + " " + this.getListType(featureDef) + " " + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getListType(featureDef) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        } else if (Multiplicities.SET.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + " " + this.getSetType(featureDef) + " " + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getSetType(featureDef) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        } else if (Multiplicities.MAP.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + " " + this.getMapType(featureDef) + " " + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getMapType(featureDef) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        } else if (Multiplicities.SPARSEARRAY.equals(featureDef.getMultiplicity())) {
            this.pw.println("  " + accessModifier + " " + this.getSparseArrayType(featureDef) + " " + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getSparseArrayType(featureDef) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
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
            this.pw.println("  " + accessModifier + " " + this.getListType(featureDef) + " " + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getListType(featureDef) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
        }
    }
    
    //-----------------------------------------------------------------------
    public void mapImplGetFeatureIndexedUsingNativeType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.pw.println("  " + accessModifier + " " + this.getType(featureDef.getQualifiedTypeName()) + " " + this.getMethodName(featureDef.getBeanGetterName()) + "(");
        this.pw.println("    int index");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(featureDef.getQualifiedTypeName())) {
            this.pw.println("    return ((Boolean)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index)).booleanValue();");
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
     
    //-----------------------------------------------------------------------
    public void mapImplGetFeatureKeyedUsingNativeType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.pw.println("  " + accessModifier + " " + this.getType(featureDef.getQualifiedTypeName()) + " " + this.getMethodName(featureDef.getBeanGetterName()) + "(");
        this.pw.println("    String key");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(featureDef.getQualifiedTypeName())) {
            this.pw
                .println("    return ((Boolean)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key)).booleanValue();");
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
    
    //-----------------------------------------------------------------------
    /**
     * Maps a list of namespace name components to a namespace name in the
     * destination language.
     */
    public String getNamespace(
        List namespaceNameComponents
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
    protected String getNamespace(
        List namespaceNameComponents,
        String packageSuffix
    ) {
        StringBuffer namespace = new StringBuffer();
        for(
            int i = 0, iLimit = namespaceNameComponents.size(); 
            i < iLimit; 
            i++
        ) Names.openmdx1NamespaceElement(
            i == 0 ? namespace : namespace.append('.'),
            (String)namespaceNameComponents.get(i)
        );
        return namespace.append(
            '.'
        ).append(
            packageSuffix
        ).toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Maps a primitive type to a primitive type of the target platform
     */
    protected String getObjectType(
        String qualifiedTypeName
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            if(PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                return "Boolean";
            }
            if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                return "Short";
            }
            if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                return "Long";
            }
            if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                return "Integer";
            }
            else {
                return this.getType(qualifiedTypeName);
            }
        }
        else {
            return this.getType(qualifiedTypeName);
        }
    }

    //-----------------------------------------------------------------------
    protected String getType(
        String qualifiedTypeName
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            if(PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                return "boolean";
            }
            if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                return "short";
            }
            if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                return "long";
            }
            if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                return "int";
            }
            if(PrimitiveTypes.BINARY.equals(qualifiedTypeName)) {
                return "byte[]";
            }
            if(PrimitiveTypes.STRING.equals(qualifiedTypeName)) {
                return "String";
            }
            if(PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)) {
                return "java.lang.Object";
            }
            if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) {
                return "java.util.Date";
            }
            if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) {
                return "javax.xml.datatype.XMLGregorianCalendar";
            }
            if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) {
                return "javax.xml.datatype.Duration";
            }
            if(PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) {
                return "java.math.BigDecimal";
            }
            if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) {
                return MappingTypes.JMI_OPENMDX_1.equals(format) ?
                    "String" :
                    "java.net.URI";
            }
            else {
                return "String";
            }
        }
        else {
            List nameComponents = MapperUtils.getNameComponents(qualifiedTypeName);
            return this.getNamespace(
                MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedTypeName))
            ) + "." + nameComponents.get(nameComponents.size()-1);
        }
    }

    //-----------------------------------------------------------------------
    protected String getQueryType(
        String qualifiedTypeName
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            return PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.BooleanTypePredicate"
            ) : PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate" + (this.generics ? "<org.w3c.Short>" : "") 
            ) : PrimitiveTypes.LONG.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate" + (this.generics ? "<org.w3c.Long>" : "") 
            ) : PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? ( 
                "org.w3c.cci2.ComparableTypePredicate" + (this.generics ? "<org.w3c.Integer>" : "") 
            ) : PrimitiveTypes.STRING.equals(qualifiedTypeName) ? (                      
                "org.w3c.cci2.StringTypePredicate" 
            ) : (
                PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName) ||
                PrimitiveTypes.ANYURI.equals(qualifiedTypeName) 
            ) ? ( 
                "org.w3c.cci2.ResourceIdentifierTypePredicate" + (this.generics ? "<java.net.URI>" : "") 
            ) : PrimitiveTypes.DATETIME.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate" + (this.generics ? "<java.util.Date>" : "")
            ) : PrimitiveTypes.DATE.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.PartiallyOrderedTypePredicate" + (this.generics ? "<javax.xml.datatype.XMLGregorianCalendar>" : "") 
            ) : PrimitiveTypes.DURATION.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.PartiallyOrderedTypePredicate" + (this.generics ? "<javax.xml.datatype.Duration>" : "")
            ) : PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate" + (this.generics ? "<java.math.BigDecimal>" : "")
            ) : "org.w3c.cci2.AnyTypePredicate";   
        } else {
            List nameComponents = MapperUtils.getNameComponents(qualifiedTypeName);
            return this.getNamespace(
                MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedTypeName)),
                "query"
            ) + "." + nameComponents.get(nameComponents.size()-1) + "Query";
        }
    }

    //-----------------------------------------------------------------------
    protected String getOrderType(
        StructuralFeatureDef featureDef
    ){
        return
            Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity()) ||
            Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity()) ?
            "org.w3c.cci2.SimpleTypeOrder" :
            "org.w3c.cci2.MultivaluedTypeOrder";
    }
    
    //-----------------------------------------------------------------------
    /**
     * Maps a method name to a method name that does not collide with an operation
     * name in the root object class in the destination language (e.g. 
     * java.lang.Object in Java). This mapping is needed to avoid that generated
     * methods accidentally override methods inherited from the root object class.
     */
    public String getMethodName(
        String methodName
    ) {
        String name = methodName.startsWith("get") || methodName.startsWith("set")
            ? methodName.substring(3)
            : methodName;
        name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        return RESERVED_WORDS.contains(name)
            ?  methodName + "_"
            : methodName;
    }
    
    //-----------------------------------------------------------------------
    /**
     * Maps a parameter name to a parameter name that does not collide with a
     * keyword name in the destination language
     */
    public String getParamName(
        String name
    ) {
        return RESERVED_WORDS.contains(name)
            ? name + "_"
            : name;
    }

    //-----------------------------------------------------------------------
    /**
     * 
     */
    public String getPredicateName(
        String prefix,
        StructuralFeatureDef fieldDef
    ){
        String prefixedName = fieldDef.getBeanGenericName();
        String standardName = 
            Character.toLowerCase(prefixedName.charAt(0)) + 
            prefixedName.substring(1);        
        return (
            prefix == null ? standardName : prefix + prefixedName
        ) + ( 
            RESERVED_WORDS.contains(standardName) ? "_" : ""
        );
    }
    
    //-----------------------------------------------------------------------
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
    
    //-----------------------------------------------------------------------
    public void fileHeader(
    ) {
        this.pw.println("//////////////////////////////////////////////////////////////////////////////");
        this.pw.println("//");
        this.pw.println("// Generated by openMDX version: " + Version.getImplementationVersion());
        this.pw.println("// Generated by class: " + mapperId());
        this.pw.println("// Generated at: " + new Date().toString());
        this.pw.println("//");
        this.pw.println("// GENERATED - DO NOT CHANGE MANUALLY");
        this.pw.println("//");
        this.pw.println("//////////////////////////////////////////////////////////////////////////////");
    }

    //-----------------------------------------------------------------------
    public void trace(
        String text
    ) {
        this.pw.println("// ----------------------------------------------------------------------------");
        this.pw.println("// " + text);
        this.pw.println("// ----------------------------------------------------------------------------");
    }

    //-----------------------------------------------------------------------
    private static final Set RESERVED_WORDS = new HashSet(
        Arrays.asList(
            new String[]{
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", 
                "char", "class", "const", "continue", "default", "do", "double", 
                "else", "extends", "false", "final", "finally", "float", "for", 
                "goto", "if", "implements", "import", "instanceof", "int", "interface", 
                "long", "native", "new", "null", "package", "private", "protected", 
                "public", "return", "short", "static", "strictfp", "super", "switch", 
                "synchronized", "this", "throw", "throws", "transient", "true", "try", 
                "void", "volatile", "while", "hashCode", "notify", "toString", "wait",
                "notifyAll", "finalize", "clone"
            }
        )
    );
    
    private final String format;
    protected final String packageSuffix;
    protected final boolean generics;
    
}

//--- End of File -----------------------------------------------------------

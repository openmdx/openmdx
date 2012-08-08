/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIInstanceImplMapper.java,v 1.8 2008/02/18 09:18:21 hburger Exp $
 * Description: JMIInstanceTemplate 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/18 09:18:21 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.ExceptionDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.OperationDef;
import org.openmdx.model1.mapping.ReferenceDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

public class JMIInstanceImplMapper
    extends JMIAbstractMapper {

    //-----------------------------------------------------------------------
    public JMIInstanceImplMapper(
        ModelElement_1_0 classDef,
        Writer writer,
        Model_1_0 model,
        String format, String packageSuffix
    ) throws ServiceException {
        super(
            writer,
            model,
            format, 
            packageSuffix
        );
        this.classDef = new ClassDef(classDef, model);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return "$Id: JMIInstanceImplMapper.java,v 1.8 2008/02/18 09:18:21 hburger Exp $";
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceSetWithQualifier(
        ReferenceDef referenceDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplReferenceSetWithQualifier");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(referenceDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getListType(referenceDef) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(\"" + referenceDef.getQualifiedName() + "\", newValue);");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(referenceDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "[] newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(\"" + referenceDef.getQualifiedName() + "\", newValue);");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceSetNoQualifier(
        ReferenceDef referenceDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplReferenceSetNoQualifier");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(referenceDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        this.pw.println("      newValue");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceRemoveWithQualifier(
        ReferenceDef referenceDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplReferenceRemoveWithQualifier");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void remove" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + "");
        this.pw.println("  ) {");
        this.pw.println("    refRemoveValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        if (
            PrimitiveTypes.BOOLEAN.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.INTEGER.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.SHORT.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.LONG.equals(referenceDef.getQualifiedQualifierTypeName())
        ) {
            this.pw.println("      new " + this.getObjectType(referenceDef.getQualifiedQualifierTypeName()) + "(" + referenceDef.getQualifierName() + ")");
        } else {
            this.pw.println("      " + referenceDef.getQualifierName() + "");
        }
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceRemoveOptional(
        ReferenceDef referenceDef,
        boolean isReadOnly
    ) {
        this.trace("Instance/ImplReferenceRemoveOptional");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void remove" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("  ) {");
        this.pw.println("    refRemoveValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\"");
        this.pw.println("    );");
        this.pw.println("  }");
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceGet1_1WithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        this.trace("Instance/ImplReferenceGet1_1WithQualifier");
        this.pw.println("  public " + this.getType(referenceDef.getQualifiedTypeName()) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + "");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getType(referenceDef.getQualifiedTypeName()) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        if (
            PrimitiveTypes.BOOLEAN.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.INTEGER.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.SHORT.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.LONG.equals(referenceDef.getQualifiedQualifierTypeName())
        ) {
            this.pw.println("      new " + this.getObjectType(referenceDef.getQualifiedQualifierTypeName()) + "(" + referenceDef.getQualifierName() + ")");
        } else {
            this.pw.println("      " + referenceDef.getQualifierName() + "");
        }
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");                
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceGet1_1NoQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/ImplReferenceGet1_1NoQualifier");
        this.pw.println("  public " + this.getType(referenceDef.getQualifiedTypeName()) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getType(referenceDef.getQualifiedTypeName()) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\"");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceGet0_nWithQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/ImplReferenceGet0_nWithQualifier");
        this.pw.println("  public " + this.getCollectionType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + "");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getCollectionType(referenceDef) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        this.pw.println("      " + referenceDef.getQualifierName() + "");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public " + this.getListType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + ",");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "Filter filter");
        this.pw.println("  ) {");
        this.pw.println("    return ((" + this.getContainerType(referenceDef) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        this.pw.println("      " + referenceDef.getQualifierName() + "");
        this.pw.println("    )).toList(filter);");
        this.pw.println("  }");
        this.pw.println();
        String queryName = MapperUtils.getElementName(referenceDef.getQualifiedTypeName()) + "Query";
        String queryPackage = this.getNamespace(
            MapperUtils.getNameComponents(MapperUtils.getPackageName(referenceDef.getQualifiedTypeName())), 
            "query"
        );
        this.pw.println("  public " + this.getListType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + ",");
        this.pw.println("    " + queryPackage + '.' + queryName + " query");
        this.pw.println("  ) {");
        this.pw.println("    return ((" + this.getContainerType(referenceDef) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        this.pw.println("      " + referenceDef.getQualifierName() + "");
        this.pw.println("    )).toList(query);");
        this.pw.println("  }");
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceGet0_nWithFilter(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/ImplReferenceGet0_nWithFilter");
        String queryName = MapperUtils.getElementName(referenceDef.getQualifiedTypeName()) + "Query";
        String queryPackage = this.getNamespace(
            MapperUtils.getNameComponents(MapperUtils.getPackageName(referenceDef.getQualifiedTypeName())), 
            "query"
        );
        this.pw.println("  public " + this.getCollectionType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getCollectionType(referenceDef) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\"");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public " + this.getListType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "Filter filter");
        this.pw.println("  ) {");
        this.pw.println("    return ((" + this.getContainerType(referenceDef) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\"");
        this.pw.println("    )).toList(filter);");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public " + this.getListType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + queryPackage + '.' + queryName + " query");
        this.pw.println("  ) {");
        this.pw.println("    return ((" + this.getContainerType(referenceDef) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\"");
        this.pw.println("    )).toList(query);");
        this.pw.println("  }");
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceGet0_1WithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        this.trace("Instance/ImplReferenceGet0_1WithQualifier");
        this.pw.println("  public " + this.getType(referenceDef.getQualifiedTypeName()) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + "");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getType(referenceDef.getQualifiedTypeName()) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        if(
            PrimitiveTypes.BOOLEAN.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.INTEGER.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.SHORT.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.LONG.equals(referenceDef.getQualifiedQualifierTypeName())
        ) {
            this.pw.println("      new " + this.getObjectType(referenceDef.getQualifiedQualifierTypeName()) + "(" + referenceDef.getQualifierName() + ")");
        } else {
            this.pw.println("      " + referenceDef.getQualifierName() + "");
        }
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
    }
        
    // -----------------------------------------------------------------------
    public void mapImplReferenceGet0_nNoFilter(
        ReferenceDef referenceDef
    ) throws ServiceException {
        this.trace("Instance/ImplReferenceGet0_nNoFilter");
        // as list
        this.pw.println("  public " + this.getCollectionType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getCollectionType(referenceDef) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\"");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();        
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceGet0_1NoQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/ImplReferenceGet0_1NoQualifier");
        this.pw.println("  public " + this.getType(referenceDef.getQualifiedTypeName()) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getType(referenceDef.getQualifiedTypeName()) + ")refGetValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\"");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceAddWithQualifier(
        ReferenceDef referenceDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplReferenceAddWithQualifier");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void add" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + ",");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        if (
            PrimitiveTypes.BOOLEAN.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.INTEGER.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.SHORT.equals(referenceDef.getQualifiedQualifierTypeName()) || 
            PrimitiveTypes.LONG.equals(referenceDef.getQualifiedQualifierTypeName())
        ) {
            this.pw.println("      new " + this.getObjectType(referenceDef.getQualifiedQualifierTypeName()) + "(" + referenceDef.getQualifierName() + "),");
        } else {
            this.pw.println("      " + referenceDef.getQualifierName() + ",");
        }
        this.pw.println("      newValue");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplReferenceAddWithoutQualifier(
        ReferenceDef referenceDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplReferenceAddWithoutQualifier");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void add" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    refAddValue(");
        this.pw.println("      \"" + referenceDef.getQualifiedName() + "\",");
        this.pw.println("      newValue");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapImplOperation(
        OperationDef operationDef
    ) throws ServiceException {
        this.trace("Instance/ImplOperation");
        this.pw.println("  public " + this.getType(operationDef.getQualifiedReturnTypeName()) + " " + this.getMethodName(operationDef.getName()) + "(");
        this.pw.println("      " + this.getType(operationDef.getQualifiedInParameterTypeName()) + " params");
        this.pw.print("  ) throws javax.jmi.reflect.RefException");
        for (Iterator<ExceptionDef> i = operationDef.getExceptions().iterator(); i.hasNext();) {
            ExceptionDef exceptionDef = i.next();
            this.pw.print(", ");
            this.pw.print(this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(exceptionDef.getQualifiedName(), 2))) + "." + exceptionDef.getName());
        }
        this.pw.println("  {");
        this.pw.println("    java.util.List args = new java.util.ArrayList();");
        this.pw.println("    args.add(params);");
        this.pw.println("    return (" + this.getType(operationDef.getQualifiedReturnTypeName()) + ")refInvokeOperation(");
        this.pw.println("      \"" + operationDef.getQualifiedName() + "\",");
        this.pw.println("      args");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public " + this.getType(operationDef.getQualifiedReturnTypeName()) + " " + this.getMethodName(operationDef.getName()) + "(");
        int ii = 0;
        for (Iterator<StructuralFeatureDef> i = operationDef.getParameters().iterator(); i.hasNext(); ii++) {
            StructuralFeatureDef param = i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                param
            );
        }
        this.pw.print("  ) throws javax.jmi.reflect.RefException");
        for (Iterator<ExceptionDef> i = operationDef.getExceptions().iterator(); i.hasNext();) {
            ExceptionDef exceptionDef = i.next();
            this.pw.print("    , ");
            this.pw.print(this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(exceptionDef.getQualifiedName(), 2))) + "." + exceptionDef.getName());
        }
        this.pw.println("  {");
        this.pw.println("    return " + this.getMethodName(operationDef.getName()) + "(");
        this.pw.println("      (("
            + this.getNamespace(MapperUtils
                .getNameComponents(MapperUtils.getPackageName(operationDef
                    .getQualifiedInParameterTypeName())))
            + "."
            + MapperUtils
                .getElementName(MapperUtils.getPackageName(operationDef
                    .getQualifiedInParameterTypeName()))
            + "Package)refOutermostPackage().refPackage(\""
            + MapperUtils.getPackageName(operationDef
                .getQualifiedInParameterTypeName())
            + "\")).create"
            + MapperUtils.getElementName(operationDef
                .getQualifiedInParameterTypeName()) + "(");
        this.pw.print("        ");
        ii = 0;
        for (Iterator<StructuralFeatureDef> i = operationDef.getParameters().iterator(); i.hasNext(); ii++) {
            StructuralFeatureDef param = i.next();
            if (ii > 0) {
                this.pw.print("    , ");
            }
            this.pw.println(this.getParamName(param.getName()) + "");
        }
        this.pw.println("      )");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplEnd() {
        this.trace("Instance/ImplEnd");
        this.pw.println("}");
    }

    // -----------------------------------------------------------------------
    public void mapImplBegin() {
        this.trace("Instance/ImplBegin");
        this.fileHeader();
        this.pw.println("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.classDef.getQualifiedName()))) + ";");
        this.pw.println("");
        this.pw.println("@SuppressWarnings({\"serial\",\"unchecked\"})");
        this.pw.println("public class " + this.classDef.getName() + "Impl");
        this.pw.println("  extends org.openmdx.base.accessor.jmi.spi.RefObject_1");
        this.pw.println("  implements " + this.classDef.getName() + " {");
        this.pw.println("");
        this.pw.println("  public " + this.classDef.getName() + "Impl(");
        this.pw.println("    org.openmdx.base.accessor.generic.cci.Object_1_0 object,");
        this.pw.println("    javax.jmi.reflect.RefClass refClass");
        this.pw.println("  ) {");
        this.pw.println("    super(object, refClass);");
        this.pw.println("  }");
        this.pw.println();
        if("org:openmdx:base:Authority".equals(this.classDef.getQualifiedName())) {
            this.pw.println("  public javax.jmi.reflect.RefPackage getPackage(){");
            this.pw.println("    return refOutermostPackage().refPackage(refGetPath().get(0));");
            this.pw.println("  }");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeSetStream(
        AttributeDef attributeDef,
        boolean isReadOnly
    ) {
        this.trace("Instance/ImplAttributeSetStream");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("");
        if (PrimitiveTypes.BINARY.equals(attributeDef.getQualifiedTypeName())) {
            this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    java.io.InputStream newValue");
            this.pw.println("  ) {");
            this.pw.println("    this.refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue);");
            this.pw.println("  }");
            this.pw.println("");
            this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    java.io.InputStream newValue,");
            this.pw.println("    long length");
            this.pw.println("  ) {");
            this.pw.println("    this.refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue, length);");
            this.pw.println("  }");
        } else if (PrimitiveTypes.STRING.equals(attributeDef.getQualifiedTypeName())) {
            this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    java.io.Reader newValue");
            this.pw.println("  ) {");
            this.pw.println("    this.refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue);");
            this.pw.println("  }");
            this.pw.println("");
            this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    java.io.Reader newValue,");
            this.pw.println("    long length");
            this.pw.println("  ) {");
            this.pw.println("    this.refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue, length);");
            this.pw.println("  }");
        }
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeSetSet(
        AttributeDef attributeDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeSetSet");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getSetType(attributeDef) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue);");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "[] newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue);");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeSetMap(
        AttributeDef attributeDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeSetMap");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getMapType(attributeDef) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue);");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeSetList(
        AttributeDef attributeDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeSetList");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getListType(attributeDef) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue);");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "[] newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(\"" + attributeDef.getQualifiedName() + "\", newValue);");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeSet1_1(
        AttributeDef attributeDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeSet1_1");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    " + this.getMethodName(attributeDef.getBeanSetterName()) + "(0, newValue);");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  protected void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    int index,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  ) {");
        if (
            PrimitiveTypes.BOOLEAN.equals(attributeDef.getQualifiedTypeName()) || 
            PrimitiveTypes.INTEGER.equals(attributeDef.getQualifiedTypeName()) || 
            PrimitiveTypes.SHORT.equals(attributeDef.getQualifiedTypeName()) || 
            PrimitiveTypes.LONG.equals(attributeDef.getQualifiedTypeName())
        ) {
            this.pw.println("    refSetValue(\"" + attributeDef.getQualifiedName() + "\", index, new " + this.getObjectType(attributeDef.getQualifiedTypeName()) + "(newValue));");
        } else {
            this.pw.println("    refSetValue(\"" + attributeDef.getQualifiedName() + "\", index, newValue);");
        }
        this.pw.println("  }");
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeSet0_1(
        AttributeDef attributeDef,
        boolean isReadOnly
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeSet0_1");
        String accessModifier = isReadOnly ? "protected" : "public";
        this.pw.println("  " + accessModifier + " void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getObjectType(attributeDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  ) {");
        this.pw.println("    refSetValue(\"" + attributeDef.getQualifiedName() + "\", 0, newValue);");
        this.pw.println("  }");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeGetStream(
        AttributeDef attributeDef
    ) {
        this.trace("Instance/ImplAttributeGetStream");
        this.pw.println("");
        if (PrimitiveTypes.BINARY.equals(attributeDef.getQualifiedTypeName())) {
            this.pw.println("  public java.io.InputStream " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("  ) {");
            this.pw.println("    return (java.io.InputStream)this.refGetValue(\"" + attributeDef.getQualifiedName() + "\");");
            this.pw.println("  }");
            this.pw.println("");
            this.pw.println("  public long " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("    java.io.OutputStream stream,");
            this.pw.println("    long position");
            this.pw.println("  ) {");
            this.pw.println("    return this.refGetValue(\"" + attributeDef.getQualifiedName() + "\", stream, position);");
            this.pw.println("  }");
        } else if (PrimitiveTypes.STRING.equals(attributeDef.getQualifiedTypeName())) {
            this.pw.println("  public java.io.Reader " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("  ) {");
            this.pw.println("    return (java.io.Reader)this.refGetValue(\"" + attributeDef.getQualifiedName() + "\");");
            this.pw.println("  }");
            this.pw.println("");
            this.pw.println("  public long " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("    java.io.Writer stream,");
            this.pw.println("    long position");
            this.pw.println("  ) {");
            this.pw.println("    return this.refGetValue(\"" + attributeDef.getQualifiedName() + "\", stream, position);");
            this.pw.println("  }");
        }
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeGetSparseArray(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeGetSparseArray");
        
        this.mapImplGetFeatureUsingObjectType(
            "public", 
            attributeDef
        );        
        this.mapImplGetFeatureIndexedUsingNativeType(
            "public",
            attributeDef
        );
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeGetSet(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeGetSet");
        
        this.mapImplGetFeatureUsingObjectType(
            "public", 
            attributeDef
        );
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeGetMap(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeGetMap");
        
        this.mapImplGetFeatureUsingObjectType(
            "public", 
            attributeDef
        );
        this.mapImplGetFeatureKeyedUsingNativeType(
            "public",
            attributeDef
        );
    }

    // -----------------------------------------------------------------------
    public void mapImplAttributeGetList(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeGetList");
        
        this.mapImplGetFeatureUsingObjectType(
            "public", 
            attributeDef
        );
        this.mapImplGetFeatureIndexedUsingNativeType(
            "public", 
            attributeDef
        );
    }

    //-----------------------------------------------------------------------
    public void mapImplAttributeGet1_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeGet1_1");

        this.pw.println("  public " + this.getType(attributeDef.getQualifiedTypeName()) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("  ) {");
        this.pw.println("    return " + this.getMethodName(attributeDef.getBeanGetterName()) + "(0);");
        this.pw.println("  }");
        this.pw.println("");
        
        this.mapImplGetFeatureIndexedUsingNativeType(
            "protected", 
            attributeDef
        );
    }

    //-----------------------------------------------------------------------
    public void mapImplAttributeGet0_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/ImplAttributeGet0_1");

        this.mapImplGetFeatureUsingObjectType(
            "public", 
            attributeDef
        );
    }

    //-----------------------------------------------------------------------
    private final ClassDef classDef;
        
}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIStructImplMapper.java,v 1.5 2008/04/02 17:39:09 wfro Exp $
 * Description: JMIStructureTemplate 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/02 17:39:09 $
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.StructDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

public class JMIStructImplMapper
    extends JMIAbstractMapper {
    
    //-----------------------------------------------------------------------
    public JMIStructImplMapper(
        ModelElement_1_0 structDef,
        Writer writer,
        Model_1_3 model,
        String format, 
        String packageSuffix
    ) throws ServiceException {
        super(
            writer,
            model,
            format, 
            packageSuffix
        );
        this.structDef = new StructDef(
            structDef, 
            model, 
            true // openmdx1
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return "$Id: JMIStructImplMapper.java,v 1.5 2008/04/02 17:39:09 wfro Exp $";
    }

    //-----------------------------------------------------------------------
    public void mapImplFieldGetStream(
        StructuralFeatureDef structureFieldDef) {
        this.trace("StructureType/ImplFieldGetStream");
        this.pw.println("");
        if (PrimitiveTypes.BINARY.equals(structureFieldDef
            .getQualifiedTypeName())) {
            this.pw.println("  public java.io.InputStream " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  ) {");
            this.pw.println("    return (java.io.InputStream)this.refGetValue(\"" + structureFieldDef.getQualifiedName() + "\");");
            this.pw.println("  }");
        } else if (PrimitiveTypes.STRING.equals(structureFieldDef
            .getQualifiedTypeName())) {
            this.pw.println("  public java.io.Reader " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  ) {");
            this.pw.println("    return (java.io.Reader)this.refGetValue(\"" + structureFieldDef.getQualifiedName() + "\");");
            this.pw.println("  }");
        } else {
            this.pw.println("  public java.util.Collection " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  ) {");
            this.pw.println("    return (java.util.Collection)this.refGetValue(\"" + structureFieldDef.getQualifiedName() + "\");");
            this.pw.println("  }");
        }
    }

    //-----------------------------------------------------------------------
    public void mapImplFieldGetSparseArray(
        StructuralFeatureDef structureFieldDef) throws ServiceException {
        this.trace("StructureType/ImplFieldGetSparseArray");
        
        this.mapImplGetFeatureUsingObjectType(
            "public",
            structureFieldDef
        );

        // Contains
        this.pw.println("  public boolean contains" + structureFieldDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(structureFieldDef.getQualifiedTypeName()) + " value");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(structureFieldDef
            .getQualifiedTypeName())
            || PrimitiveTypes.INTEGER.equals(structureFieldDef
                .getQualifiedTypeName())
            || PrimitiveTypes.SHORT.equals(structureFieldDef
                .getQualifiedTypeName())
            || PrimitiveTypes.LONG.equals(structureFieldDef
                .getQualifiedTypeName())) {
            this.pw.println("    return refContainsValue(\"" + structureFieldDef.getQualifiedName() + "\", new " + this.getObjectType(structureFieldDef.getQualifiedTypeName()) + "(value));");
        } else {
            this.pw.println("    return refContainsValue(\"" + structureFieldDef.getQualifiedName() + "\", value);");
        }
        this.pw.println("  }");
        this.pw.println("");
        
        this.mapImplGetFeatureIndexedUsingNativeType(
            "public",
            structureFieldDef
        );
    }

    //-----------------------------------------------------------------------
    public void mapImplFieldGetSet(
        StructuralFeatureDef structureFieldDef) throws ServiceException {
        this.trace("StructureType/ImplFieldGetSet");
        
        this.mapImplGetFeatureUsingObjectType(
            "public",
            structureFieldDef
        );
        
        // contains
        this.pw.println("  public boolean contains" + structureFieldDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(structureFieldDef.getQualifiedTypeName()) + " value");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(structureFieldDef.getQualifiedTypeName())
            || PrimitiveTypes.INTEGER.equals(structureFieldDef.getQualifiedTypeName())
            || PrimitiveTypes.SHORT.equals(structureFieldDef.getQualifiedTypeName())
            || PrimitiveTypes.LONG.equals(structureFieldDef.getQualifiedTypeName())
        ) {
            this.pw.println("    return refContainsValue(\"" + structureFieldDef.getQualifiedName() + "\", new " + this.getObjectType(structureFieldDef.getQualifiedTypeName()) + "(value));");
        } else {
            this.pw.println("    return refContainsValue(\"" + structureFieldDef.getQualifiedName() + "\", value);");
        }
        this.pw.println("  }");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapImplFieldGetList(
        StructuralFeatureDef structureFieldDef) throws ServiceException {
        this.trace("StructureType/ImplFieldGetList");
        
        this.mapImplGetFeatureUsingObjectType(
            "public",
            structureFieldDef
        );
        
        // contains
        this.pw.println("  public boolean contains" + structureFieldDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(structureFieldDef.getQualifiedTypeName()) + " value");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(structureFieldDef.getQualifiedTypeName())
            || PrimitiveTypes.INTEGER.equals(structureFieldDef.getQualifiedTypeName())
            || PrimitiveTypes.SHORT.equals(structureFieldDef.getQualifiedTypeName())
            || PrimitiveTypes.LONG.equals(structureFieldDef.getQualifiedTypeName())
        ) {
            this.pw.println("    return refContainsValue(\"" + structureFieldDef.getQualifiedName() + "\", new " + this.getObjectType(structureFieldDef.getQualifiedTypeName()) + "(value));");
        } else {
            this.pw.println("    return refContainsValue(\"" + structureFieldDef.getQualifiedName() + "\", value);");
        }
        this.pw.println("  }");
        this.pw.println("");
        
        this.mapImplGetFeatureIndexedUsingNativeType(
            "public",
            structureFieldDef
        );
    }

    //-----------------------------------------------------------------------
    public void mapImplFieldGet1_1(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        this.trace("StructureType/ImplFieldGet1_1");
        
        this.pw.println("  public " + this.getType(structureFieldDef.getQualifiedTypeName()) + " " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
        this.pw.println("  ) {");
        this.pw.println("    return " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(0);");
        this.pw.println("  }");
        this.pw.println("");
        
        this.mapImplGetFeatureIndexedUsingNativeType(
            "private",
            structureFieldDef
        );
    }

    //-----------------------------------------------------------------------
    public void mapImplFieldGet0_1(
        StructuralFeatureDef structureFieldDef
   ) throws ServiceException {
        this.trace("StructureType/ImplFieldGet0_1");
        
        this.mapImplGetFeatureUsingObjectType(
            "public",
            structureFieldDef
        );
        
    }

    //-----------------------------------------------------------------------
    public void mapImplEnd() {
        this.trace("StructureType/ImplEnd");
        this.pw.println("}");
    }

    //-----------------------------------------------------------------------
    public void mapImplBegin() {
        this.trace("StructureType/ImplBegin");
        this.fileHeader();
        this.pw.println("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.structDef.getQualifiedName()))) + ";");
        this.pw.println("");
        this.pw.println("@SuppressWarnings({\"serial\",\"unchecked\"})");
        this.pw.println("public class " + this.structDef.getName() + "Impl");
        this.pw.println("  extends org.openmdx.base.accessor.jmi.spi.RefStruct_1");
        this.pw.println("  implements " + this.structDef.getName() + " {");
        this.pw.println("");
        this.pw.println("  public " + this.structDef.getName() + "Impl(");
        this.pw.println("    org.openmdx.base.accessor.jmi.cci.RefPackage_1_0 refPackage,");
        this.pw.println("    java.util.List args");
        this.pw.println("  ) {");
        this.pw.println("    super(refPackage, args);");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public " + this.structDef.getName() + "Impl(");
        this.pw.println("    org.openmdx.base.accessor.jmi.cci.RefPackage_1_0 refPackage,");
        this.pw.println("    java.lang.Object arg");
        this.pw.println("  ) {");
        this.pw.println("    super(refPackage, arg);");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public String refQualifiedTypeName(");
        this.pw.println("  ) {");
        this.pw.println("    return \"" + this.structDef.getQualifiedName() + "\";");
        this.pw.println("  }");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    private final StructDef structDef;

}

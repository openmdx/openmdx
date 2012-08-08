/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIPackageImplMapper.java,v 1.9 2008/06/28 00:21:36 hburger Exp $
 * Description: JMIPackageTemplate 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:36 $
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
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.mapping.AbstractNames;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.ClassifierDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.StructDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

public class JMIPackageImplMapper
    extends JMIAbstractMapper {

    //-----------------------------------------------------------------------
    public JMIPackageImplMapper(
        Writer writer,
        Model_1_3 model,
        String format, 
        String packageSuffix
    ) {
        super(
            writer,
            model,
            format, 
            packageSuffix
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return "$Id: JMIPackageImplMapper.java,v 1.9 2008/06/28 00:21:36 hburger Exp $";
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapImplStructCreator(
        StructDef structDef
    ) throws ServiceException {
        this.trace("Package/ImplStructCreator");
        this.pw
            .println("  public " + this.getType(structDef.getQualifiedName()) + " create" + MapperUtils.getElementName(structDef.getQualifiedName()) + "(");
        int ii = 0;
        for (Iterator<StructuralFeatureDef> i = structDef.getFields().iterator(); i.hasNext(); ii++) {
            StructuralFeatureDef fieldDef = i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                fieldDef
            );
        }
        this.pw.println("  ) {");
        this.pw.println("    java.util.List args = new java.util.ArrayList();");
        String addOp = "add";
        for (Iterator<StructuralFeatureDef> i = structDef.getFields().iterator(); i.hasNext();) {
            StructuralFeatureDef fieldDef = i
                .next();
            if ((Multiplicities.SINGLE_VALUE.equals(fieldDef.getMultiplicity()))
                && (PrimitiveTypes.BOOLEAN.equals(fieldDef
                    .getQualifiedTypeName())
                    || PrimitiveTypes.INTEGER.equals(fieldDef
                        .getQualifiedTypeName())
                    || PrimitiveTypes.SHORT.equals(fieldDef
                        .getQualifiedTypeName()) || PrimitiveTypes.LONG
                    .equals(fieldDef.getQualifiedTypeName()))) {
                this.pw.println("    args." + addOp + "(new " + this.getObjectType(fieldDef.getQualifiedTypeName()) + "(" + fieldDef.getName() + "));");
            } else {
                this.pw.println("    args." + addOp + "(" + fieldDef.getName() + ");");
            }
        }
        this.pw.println("    return (" + this.getType(structDef.getQualifiedName()) + ")this.refCreateStruct(");
        this.pw.println("      \"" + structDef.getQualifiedName() + "\",");
        this.pw.println("      args");
        this.pw.println("    );");
        this.pw.println("  }");        
    }
    
    //-----------------------------------------------------------------------
    public void mapImplFilterCreator(
        ClassifierDef classifierDef
    ) throws ServiceException {
        this.trace("Package/ImplFilterCreator");
        String queryName = MapperUtils.getElementName(classifierDef.getQualifiedName()) + "Query";
        String queryPackage = this.getNamespace(
            MapperUtils.getNameComponents(MapperUtils.getPackageName(classifierDef.getQualifiedName())), 
            "query"
        );
        this.pw.println("  public " + queryPackage + '.' + queryName + " create" + queryName + '(');
        this.pw.println("  ) {");
        this.pw.println("    return (" + queryPackage + '.' + queryName + ")this.refCreateFilter(");
        this.pw.println("      \"" + classifierDef.getQualifiedName() + "\",");
        this.pw.println("      null,");
        this.pw.println("      null");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public " + this.getType(classifierDef.getQualifiedName()) + "Filter create" + MapperUtils.getElementName(classifierDef.getQualifiedName()) + "Filter(");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getType(classifierDef.getQualifiedName()) + "Filter)this.refCreateFilter(");
        this.pw.println("      \"" + classifierDef.getQualifiedName() + "\",");
        this.pw.println("      null,");
        this.pw.println("      null");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public " + this.getType(classifierDef.getQualifiedName()) + "Filter create" + MapperUtils.getElementName(classifierDef.getQualifiedName()) + "Filter(");
        this.pw.println("    org.openmdx.compatibility.base.query.FilterProperty[] filterProperties,");
        this.pw.println("    org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier[] attributeSpecifiers");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getType(classifierDef.getQualifiedName()) + "Filter)this.refCreateFilter(");
        this.pw.println("      \"" + classifierDef.getQualifiedName() + "\",");
        this.pw.println("      filterProperties,");
        this.pw.println("      attributeSpecifiers");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();
        
    }
    
    //-----------------------------------------------------------------------
    public void mapImplEnd(
        String qualifiedPackageName
    ) {
        this.trace("Package/ImplEnd");
        this.pw.println("");
        this.pw.println("  public String refMofId(");
        this.pw.println("  ) {");
        this.pw.println("    return \"" + qualifiedPackageName + "\";");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("}");        
    }
    
    //-----------------------------------------------------------------------
    public void mapImplClassAccessor(
        ClassDef classDef
    ) throws ServiceException {
        this.trace("Package/ImplClassAccessor");
        String className = MapperUtils.getElementName(classDef.getQualifiedName());
        String openmdx1AccessorName = getMethodName("get" + className + "Class");
        String openmdx2AccessorName = getMethodName("get" + className);
        this.pw.println("  /**");
        this.pw.println("   * openMDX 1 specific <code>" + className + "</code> class accessor");
        this.pw.println("   * <p>");
        this.pw.println("   * @return " + className + " class");
        this.pw.println("   * @deprecated in favour of " + openmdx2AccessorName + "()");
        this.pw.println("   * @see #" + openmdx2AccessorName + "()");
        this.pw.println("   */");
        this.pw.println(
            "  public " + this.getType(classDef.getQualifiedName()) + "Class " + openmdx1AccessorName + "(");
        this.pw.println("  ) {");
        this.pw.println("    return " + openmdx2AccessorName + "();");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  /**");
        this.pw.println("   * JMI 1.0 compliant <code>" + className + "</code> class accessor");
        this.pw.println("   * <p>");
        this.pw.println("   * @return " + className + " class");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(classDef.getQualifiedName()) + "Class " + openmdx2AccessorName + "(");
        this.pw.println("  ) {");
        this.pw.println("    return (" + this.getType(classDef.getQualifiedName()) + "Class)refClass(");
        this.pw.println("      \"" + classDef.getQualifiedName() + "\"");
        this.pw.println("    );");
        this.pw.println("  }");
        this.pw.println();        
    }
    
    //-----------------------------------------------------------------------
    public void mapImplBegin(
        String qualifiedPackageName
    ) {
        List<String> namespaceComponents = MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedPackageName));
        StringBuffer buffer = Names.openmdx1PackageName(
            new StringBuffer(),
            MapperUtils.getElementName(qualifiedPackageName)
        );
        String packageIntfName = buffer.toString();
        String packageImplName = buffer.append("Impl").toString();
        buffer.setLength(0);
        String packageCompatibilityName = AbstractNames.openmdx2PackageName(
            buffer,
            MapperUtils.getElementName(qualifiedPackageName)
        ).toString();
        this.trace("Package/ImplBegin");
        this.fileHeader();
        this.pw.println("package " + this.getNamespace(namespaceComponents) + ";");
        this.pw.println();
        this.pw.println("@SuppressWarnings({\"serial\",\"unchecked\"})");
        this.pw.println("public class " + packageImplName);
        this.pw.println("  extends org.openmdx.base.accessor.jmi.spi.RefPackage_1");        
        this.pw.println("  implements " + packageIntfName + ", " + this.getNamespace(namespaceComponents, "jmi") + '.' + packageCompatibilityName +  " {");
        this.pw.println();
        this.pw.println("  public " + packageImplName + '(');
        this.pw.println("    javax.jmi.reflect.RefPackage outermostPackage,");
        this.pw.println("    javax.jmi.reflect.RefPackage immediatePackage");
        this.pw.println("  ) {");
        this.pw.println("    super(outermostPackage, immediatePackage);");
        this.pw.println("  }");
        this.pw.println();        
    }
    
}

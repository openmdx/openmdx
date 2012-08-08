/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: PackageMapper.java,v 1.17 2008/06/28 00:21:26 hburger Exp $
 * Description: JMI Package Template 
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:26 $
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
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.mapping.AbstractNames;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.ClassifierDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MetaData_1_0;
import org.openmdx.model1.mapping.StructDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

/**
 * JMI Package template
 */
@SuppressWarnings("unchecked")
public class PackageMapper
    extends AbstractMapper {

    //-----------------------------------------------------------------------
    public PackageMapper(
        Writer writer,
        Model_1_3 model,
        Format format, 
        String packageSuffix, MetaData_1_0 metaData
    ) {
        super(
            writer,
            model,
            format, 
            packageSuffix,
            metaData
        );
    }

    //-----------------------------------------------------------------------
    public void mapQueryCreator(
        ClassifierDef classifierDef
    ) throws ServiceException {
        this.trace("Package/QueryCreator");
        String candidateType = this.interfaceType(
            classifierDef, 
            org.openmdx.model1.importer.metadata.Visibility.CCI,
            false
        ); 
        this.pw.println("  public " + candidateType + "Query " + getMethodName(
            "create" + MapperUtils.getElementName(classifierDef.getQualifiedName()) + "Query"
        ) + "();");
        this.pw.println();
    }
    
    //-----------------------------------------------------------------------
    public void mapEnd(
    ) {
        this.trace("Package/End.vm");
        this.pw.println("}");        
    }
    
    //-----------------------------------------------------------------------
    public void mapClassAccessor(
        ClassDef classDef
    ) throws ServiceException {
        this.trace("Package/ClassAccessor");
        this.pw.println("  public " + this.getType(classDef.getQualifiedName()) + "Class " + getMethodName(
            "get" + MapperUtils.getElementName(classDef.getQualifiedName()) 
        ) + "();");
        this.pw.println();        
    }
  
    //-----------------------------------------------------------------------
    @SuppressWarnings("static-access")
    public void mapBegin(
        String qualifiedPackageName
    ) throws ServiceException {
        this.trace("Package/Begin");
        this.fileHeader();
        List nameComponents = MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedPackageName));
        StringBuffer buffer = AbstractNames.openmdx2PackageName(
            new StringBuffer(),
            MapperUtils.getElementName(qualifiedPackageName)
        );
        String packageType = buffer.toString();
        buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
        String packageField = buffer.toString();
        buffer.setLength(buffer.length() - 7);
        String authorityField = buffer.append("Authority").toString();
        String authorityType = this.getType("org:openmdx:base:Authority");
        String xri = MapperUtils.getAuthorityId(nameComponents);
        this.pw.println(" /**");
        this.pw.println(
            MapperUtils.wrapText(
                "  * ",
                "The <code>AUTHORITY_XRI</code> <em>\"" + xri + 
                "\"</em> may be used to look up the Authority the package <code>" + 
                qualifiedPackageName + "</code> belongs to:"
            )
        );
        this.pw.println("  * <p>");
        this.pw.println("  * <pre>");
        this.pw.println("  *   " + authorityType + ' ' + authorityField + " = (" + authorityType + ")persistenceManager.getObjectById(");
        this.pw.println("  *     " + authorityType + ".class,");
        this.pw.println("  *     " + packageType + ".AUTHORITY_XRI");
        this.pw.println("  *   );");
        this.pw.println("  *   " + packageType + " " + packageField + " = (" + packageType + ')' + authorityField + ".getPackage();");
        this.pw.println("  * </pre>");
        this.pw.println("  * <p>");
        this.pw.println("  *");
        this.pw.println("  * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class,java.lang.Object)");
        this.pw.println("  */");
        this.pw.println("package " + this.getNamespace(nameComponents) + ";");
        this.pw.println();
        this.pw.println("public interface " + packageType + "  extends javax.jmi.reflect.RefPackage {");  
        this.pw.println();
        this.pw.println(" /**");
        this.pw.println(
            MapperUtils.wrapText(
                "  * ",
                "The <code>AUTHORITY_XRI</code> <em>\"" + xri + 
                "\"</em> may be used to look up the Authority the package <code>" + 
                qualifiedPackageName + "</code> belongs to:"
            )
        );
        this.pw.println("   * <p>");
        MapperUtils.wrapText(
            "   * ",
            "<em>Note: This is an extension to the JMI 1 standard.</em>"
        );
        this.pw.println("  */");
        this.pw.println("  java.lang.String AUTHORITY_XRI = \"" + xri + "\";");   
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    public void mapStructCreator(
        StructDef structDef
    ) throws ServiceException {
        this.trace("Package/StructCreator");
        String methodName = Identifier.OPERATION_NAME.toIdentifier(
            structDef.getName(), 
            null, // removablePrefix
            "create", // prependablePrefix
            null, // removableSuffix
            null // appendableSuffix
        );
        this.pw.println("  public " + this.getType(structDef.getQualifiedName()) + " " + methodName + "(");
        int ii = 0;
        for (Iterator i = structDef.getFields().iterator(); i.hasNext(); ii++) {
            StructuralFeatureDef fieldDef = (StructuralFeatureDef) i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                fieldDef, ""
            );
        }
        this.pw.println("  );");        
    }
        
}

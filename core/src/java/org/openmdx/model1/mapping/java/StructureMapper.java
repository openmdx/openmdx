/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StructureMapper.java,v 1.19 2008/06/28 00:21:26 hburger Exp $
 * Description: Structure Template 
 * Revision:    $Revision: 1.19 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:26 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
import java.util.ArrayList;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.mapping.AbstractNames;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MetaData_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.StructDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

/**
 * StructureMapper
 */
public class StructureMapper
    extends AbstractMapper {
    
    //-----------------------------------------------------------------------
    public StructureMapper(
        ModelElement_1_0 structDef,
        Writer writer,
        Model_1_3 model,
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
        this.structDef = new StructDef(
            structDef, 
            model, 
            false // openmdx1
        );
        this.structName = Identifier.CLASS_PROXY_NAME.toIdentifier(
            this.structDef.getName()
        );
    }

    //-----------------------------------------------------------------------
    public void mapFieldGetSparseArray(
        StructuralFeatureDef fieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGetSparseArray");
        this.members.add(fieldDef.getBeanGenericName());
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves a SparseArray containing all the elements for the structure field <code>" + fieldDef.getName() + "</code>."
        ));
        if (fieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            MapperUtils.wrapText("   * ", fieldDef.getAnnotation());
        }
        this.pw.println("   * @return A SparseArray containing all elements for this structure field.");
        this.pw.println("   */");
        String memberType = this.getType(fieldDef, "org.w3c.cci2.SparseArray", Boolean.TRUE, TypeMode.MEMBER);
        this.pw.println("  public " + memberType + " " + this.getMethodName(fieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    public void mapFieldGetSet(
        StructuralFeatureDef fieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGetSet");
        this.members.add(fieldDef.getBeanGenericName());
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a set containing all the elements for the structure field <code>" + fieldDef.getName() + "</code>."));
        if (fieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", fieldDef.getAnnotation()));
        }
        this.pw.println("   * @return A set containing all elements for this structure field.");
        this.pw.println("   */");
        String memberType = this.getType(fieldDef, "java.util.Set", Boolean.TRUE, TypeMode.MEMBER);
        this.pw.println("  public " + memberType + " " + this.getMethodName(fieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");        
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    public void mapFieldGetList(
        StructuralFeatureDef fieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGetList");
        this.members.add(fieldDef.getBeanGenericName());
        if(getFormat() != Format.JMI1) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves a list containing all the elements for the structure field <code>" + fieldDef.getName() + "</code>."));
            if (fieldDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", fieldDef.getAnnotation()));
            }
            this.pw.println("   * @return A list containing all elements for this structure field.");
            this.pw.println("   */");
            String memberType = this.getType(fieldDef, "java.util.List", Boolean.TRUE, TypeMode.MEMBER);
            this.pw.println("  public " + memberType + " " + this.getMethodName(fieldDef.getBeanGetterName()) + "(");
            this.pw.println("  );");        
            this.pw.println();
        }
    }

    //-----------------------------------------------------------------------
    public void mapFieldGet1_1(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGet1_1");
        this.members.add(structureFieldDef.getBeanGenericName());
        this.pw.println("  /**");
        this.pw.println("   * Retrieves the value for the structure field <code>" + structureFieldDef.getName() + "</code>.");
        if (structureFieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", structureFieldDef.getAnnotation()));
        }
        this.pw.println("   * @return The non-null value for structure field <code>" + structureFieldDef.getName() + "</code>.");
        this.pw.println("   */");
        String memberType = this.getType(structureFieldDef.getQualifiedTypeName());
        this.pw.println("  public " + memberType + " " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");        
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    public void mapFieldGet0_1(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGet0_1");
        this.members.add(structureFieldDef.getBeanGenericName());
        this.pw.println("  /**");
        this.pw.println("   * Retrieves the value for the optional structure field <code>" + structureFieldDef.getName() + "</code>.");
        if (structureFieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils
                .wrapText("   * ", structureFieldDef.getAnnotation()));
        }
        this.pw.println("   * @return The possibly null value for structure field <code>" + structureFieldDef.getName() + "</code>.");
        this.pw.println("   */");
        String memberType = this.getObjectType(structureFieldDef.getQualifiedTypeName());
        this.pw.println("  public " + memberType + " " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");        
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    public void mapFieldGetStream(
        StructuralFeatureDef structureFieldDef
    ) {
        this.trace("StructureType/FieldGetStream.vm");
        this.members.add(structureFieldDef.getBeanGenericName());
        this.pw.println("");
        if (PrimitiveTypes.BINARY.equals(structureFieldDef.getQualifiedTypeName())) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves the value as java.io.InputStream for the binary structure field <code>" + structureFieldDef.getName() + "</code>."));
            if (structureFieldDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", structureFieldDef.getAnnotation()));
            }
            this.pw.println("   * @return A InputStream containing the binary value as stream for this structure field.");
            this.pw.println("   */");
            this.pw.println("  public java.io.InputStream " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
            this.pw.println("");
        } 
        else if (PrimitiveTypes.STRING.equals(structureFieldDef.getQualifiedTypeName())) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves the value as java.io.Reader for the string structure field <code>" + structureFieldDef.getName() + "</code>."));
            if (structureFieldDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", structureFieldDef.getAnnotation()));
            }
            this.pw.println("   * @return A Reader containing the string value as stream for this structure field.");
            this.pw.println("   */");
            this.pw.println("  public java.io.Reader " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
            this.pw.println();
        } 
        else {
            this.pw.println();
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves the value as streaming Collection for the structure field <code>" + structureFieldDef.getName() + "</code>."));
            if (structureFieldDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", structureFieldDef.getAnnotation()));
            }
            this.pw.println("   * @return A Collection containing the value as stream for this structure field.");
            this.pw.println("   */");
            this.pw.println("  public java.io.DataInput " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
            this.pw.println();
        }
    }

    //-----------------------------------------------------------------------
    public void mapEnd(
    ) throws ServiceException {
        this.pw.println();
        if(getFormat() == Format.CCI2) {
            this.trace("StructureType/Member");
            this.pw.println();
            this.pw.println("  /**");
            this.pw.println("   * The structure's members");
            this.pw.println("   */");
            this.pw.println("  enum Member {");
            String delimiter = "    ";
            for(String member : this.members) {
                this.pw.println(delimiter + AbstractNames.uncapitalize(member));
                delimiter = "  , ";
            }
            this.pw.println("  }");
            this.pw.println();
        }
        this.trace("StructureType/End");
        this.pw.println("}");
    }

    //-----------------------------------------------------------------------
    public void mapBegin(
    ) throws ServiceException {
        this.trace("StructureType/Begin");
        this.fileHeader();
        List<String> nameComponents = MapperUtils.getNameComponents(MapperUtils.getPackageName(this.structDef.getQualifiedName()));
        this.pw.println("package " + this.getNamespace(nameComponents) + ";");
        this.pw.println();
        this.pw.println("public interface " + this.structName);
        if(getFormat() == Format.JMI1) {            
            this.pw.print("  extends " + REF_STRUCT_INTERFACE_NAME + ", ");
        }
        if(getFormat() != Format.CCI2) {
            this.pw.println(
                getNamespace(nameComponents, Names.CCI2_PACKAGE_SUFFIX) +    
                '.' +
                this.structName
            );
        }
        this.pw.println("{");
        this.members = new ArrayList<String>();
    }

    //-----------------------------------------------------------------------
    static final String REF_STRUCT_INTERFACE_NAME = "org.openmdx.base.accessor.jmi.cci.RefStruct_1_0";
    
    private final StructDef structDef;
    private final String structName;
    private List<String> members;
    
}

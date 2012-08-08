/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIStructIntfMapper.java,v 1.6 2008/04/02 17:39:09 wfro Exp $
 * Description: JMIStructureTemplate 
 * Revision:    $Revision: 1.6 $
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

public class JMIStructIntfMapper
    extends JMIAbstractMapper {
    
    //-----------------------------------------------------------------------
    public JMIStructIntfMapper(
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
        return "$Id: JMIStructIntfMapper.java,v 1.6 2008/04/02 17:39:09 wfro Exp $";
    }

    //-----------------------------------------------------------------------
    public void mapIntfFieldGetSparseArray(
        StructuralFeatureDef fieldDef
    ) throws ServiceException {
        this.trace("StructureType/IntfFieldGetSparseArray");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a SparseArray containing all the elements for the structure field <code>" + fieldDef.getName() + "</code>."));
        if (fieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", fieldDef.getAnnotation()));
        }
        this.pw.println("   * @return A SparseArray containing all elements for this structure field.");
        this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getSparseArrayType(fieldDef) + " " + this.getMethodName(fieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Returns true if the SparseArray of all the values for the structure field <code>" + fieldDef.getName() + "</code> contains the specified element."));
        if (fieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", fieldDef.getAnnotation()));
        }
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@param value An element whose presence in the SparseArray of all the values for this structure field is to be tested."));
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@return <code>true</code> if the SparseArray of all the values for this structure field contains the specified element."));
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@exception JmiException If the containment of the value cannot be checked for some reason."));
        this.pw.println("   */");
        this.pw.println("  public boolean contains" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(fieldDef.getQualifiedTypeName()) + " value");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the element at the specified position in the SparseArray of all the values for the structure field <code>" + fieldDef.getName() + "</code>."));
        if (fieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", fieldDef.getAnnotation()));
        }
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "@param index The index of the element to return."));
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@return The element at the specified position in the SparseArray of all values for this structure field."));
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@exception JmiException If the value cannot be retrieved for some reason."));
        this.pw.println("   */");
        this.pw
            .println("  public " + this.getType(fieldDef.getQualifiedTypeName()) + " " + this.getMethodName(fieldDef.getBeanGetterName()) + "(");
        this.pw.println("    int index");
        this.pw.println("  );");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapIntfFieldGetSet(
        StructuralFeatureDef fieldDef
    ) throws ServiceException {
        this.trace("StructureType/IntfFieldGetSet");
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
        this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getSetType(fieldDef) + " " + this.getMethodName(fieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Returns true if the set of all the values for the structure field <code>" + fieldDef.getName() + "</code> contains the specified element."));
        if (fieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", fieldDef.getAnnotation()));
        }
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@param value An element whose presence in the set of all the values for this structure field is to be tested."));
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@return <code>true</code> if the set of all the values for this structure field contains the specified element."));
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@exception JmiException If the containment of the value cannot be checked for some reason."));
        this.pw.println("   */");
        this.pw.println("  public boolean contains" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(fieldDef.getQualifiedTypeName()) + " value");
        this.pw.println("  );");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapIntfFieldGetList(
        StructuralFeatureDef fieldDef
    ) throws ServiceException {
        this.trace("StructureType/IntfFieldGetList");
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
        this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getListType(fieldDef) + " " + this.getMethodName(fieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Returns true if the list of all the values for the structure field <code>" + fieldDef.getName() + "</code> contains the specified element."));
        if (fieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", fieldDef.getAnnotation()));
        }
        this.pw.println("   * @param value An element whose presence in the list of all the values for this structure field is to be tested.");
        this.pw.println("   * @return <code>true</code> if the list of all the values for this structure field contains the specified element.");
        this.pw.println("   * @exception JmiException If the containment of the value cannot be checked for some reason.");
        this.pw.println("   */");
        this.pw.println("  public boolean contains" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(fieldDef.getQualifiedTypeName()) + " value");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the element at the specified position in the list of all the values for the structure field <code>" + fieldDef.getName() + "</code>."));
        if (fieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", fieldDef.getAnnotation()));
        }
        this.pw.println("   * @param index The index of the element to return.");
        this.pw.println("   * @return The element at the specified position in the list of all values for this structure field.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(fieldDef.getQualifiedTypeName()) + " " + this.getMethodName(fieldDef.getBeanGetterName()) + "(");
        this.pw.println("    int index");
        this.pw.println("  );");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapIntfFieldGet1_1(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        this.trace("StructureType/IntfFieldGet1_1");
        this.pw.println("  /**");
        this.pw.println("   * Retrieves the value for the structure field <code>" + structureFieldDef.getName() + "</code>.");
        if (structureFieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", structureFieldDef.getAnnotation()));
        }
        this.pw.println("   * @return The non-null value for structure field <code>" + structureFieldDef.getName() + "</code>.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(structureFieldDef.getQualifiedTypeName()) + " " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapIntfFieldGet0_1(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        this.trace("StructureType/IntfFieldGet0_1");
        this.pw.println("  /**");
        this.pw.println("   * Retrieves the value for the optional structure field <code>" + structureFieldDef.getName() + "</code>.");
        if (structureFieldDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils
                .wrapText("   * ", structureFieldDef.getAnnotation()));
        }
        this.pw.println("   * @return The possibly null value for structure field <code>" + structureFieldDef.getName() + "</code>.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getObjectType(structureFieldDef.getQualifiedTypeName()) + " " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapIntfEnd() {
        this.trace("StructureType/IntfEnd");
        this.pw.println("}");
    }

    //-----------------------------------------------------------------------
    public void mapIntfBegin() {
        this.trace("StructureType/IntfBegin");
        this.fileHeader();
        this.pw.println("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.structDef.getQualifiedName()))) + ";");
        this.pw.println("");
        this.pw.println("@SuppressWarnings(\"unchecked\")");
        this.pw.println("public interface " + this.structDef.getName() + "");
        this.pw.println("  extends org.openmdx.base.accessor.jmi.cci.RefStruct_1_0 {");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapIntfFieldGetStream(
        StructuralFeatureDef structureFieldDef
    ) {
        this.trace("StructureType/IntfFieldGetStream.vm");
        this.pw.println("");
        if (PrimitiveTypes.BINARY.equals(structureFieldDef
            .getQualifiedTypeName())) {
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
            this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
            this.pw.println("   */");
            this.pw.println("  public java.io.InputStream " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
            this.pw.println("");
        } else if (PrimitiveTypes.STRING.equals(structureFieldDef.getQualifiedTypeName())) {
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
            this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
            this.pw.println("   */");
            this.pw.println("  public java.io.Reader " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
            this.pw.println("");
        } else {
            this.pw.println("");
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
            this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
            this.pw.println("   */");
            this.pw.println("  public java.util.Collection " + this.getMethodName(structureFieldDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
            this.pw.println("");
        }
    }
    
    //-----------------------------------------------------------------------
    private final StructDef structDef;
        
}

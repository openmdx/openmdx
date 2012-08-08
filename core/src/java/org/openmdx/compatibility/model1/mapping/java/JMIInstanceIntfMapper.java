/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIInstanceIntfMapper.java,v 1.9 2008/02/18 09:18:20 hburger Exp $
 * Description: JMIInstanceTemplate 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/18 09:18:20 $
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

public class JMIInstanceIntfMapper
    extends JMIAbstractMapper {

    //-----------------------------------------------------------------------
    public JMIInstanceIntfMapper(
        ModelElement_1_0 classDef,
        Writer writer,
        Model_1_0 model,
        String format, 
        String packageSuffix
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
        return "$Id: JMIInstanceIntfMapper.java,v 1.9 2008/02/18 09:18:20 hburger Exp $";
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceSetNoQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceSetNoQualifier");
        this.pw.println("  /**");
        this.pw.println("   * Sets a new value for the reference <code>"
            + referenceDef.getName() + "</code>.");
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw
            .println("   * @param newValue The non-null new value for this reference.");
        this.pw
            .println("   * @exception JmiException If the value cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void "
            + this.getMethodName(referenceDef.getBeanSetterName()) + "(");
        this.pw.println("    "
            + this.getType(referenceDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceRemoveWithQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceRemoveWithQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Removes the qualified (by means of the specified qualifier attribute value) element from the list of all the values for the reference <code>"
                    + referenceDef.getName() + "</code>."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@param "
                    + referenceDef.getQualifierName()
                    + " The qualifier attribute value that qualifies the reference to get the element to be removed."));
        this.pw
            .println("   * @exception JmiException If the value cannot be removed for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void remove"
            + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    "
            + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " "
            + referenceDef.getQualifierName() + "");
        this.pw.println("  );");
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceRemoveOptional(
        ReferenceDef referenceDef) {
        this.trace("Instance/IntfReferenceRemoveOptional");
        this.pw.println("  /**");
        this.pw
            .println("   * Removes the value for the optional reference <code>"
                + referenceDef.getName() + "</code>.");
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw
            .println("   * @exception JmiException If the value cannot be removed for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void remove"
            + referenceDef.getBeanGenericName() + " (");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceGet1_1WithQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceGet1_1WithQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves the value for the optional reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param " + referenceDef.getQualifierName() + " The value for the qualifier attribute that qualifies this reference.");
        this.pw.println("   * @return The non-null value for this reference.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(referenceDef.getQualifiedTypeName()) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + "");
        this.pw.println("  );");
        this.pw.println();        
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceGet1_1NoQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceGet1_1NoQualifier");
        this.pw.println("  /**");
        this.pw.println("   * Retrieves the value for the reference <code>"
            + referenceDef.getName() + "</code>.");
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @return The non-null value for this reference.");
        this.pw
            .println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public "
            + this.getType(referenceDef.getQualifiedTypeName()) + " "
            + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceGet0_nWithQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceGet0_nWithQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param " + referenceDef.getQualifierName() + " The value for the qualifier attribute that qualifies this reference.");
        this.pw.println("   * @return The collection of referenced objects.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getCollectionType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + "");
        this.pw.println("  );");
        this.pw.println();
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value and filter."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param " + referenceDef.getQualifierName() + " The value for the qualifier attribute that qualifies this reference.");
        this.pw.println("   * @param filter filter which is applied to the set of referenced objects.");
        this.pw.println("   * @return The filtered collection of referenced objects.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getListType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + ",");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "Filter filter");
        this.pw.println("  );");
        this.pw.println();
        String queryName = MapperUtils.getElementName(referenceDef.getQualifiedTypeName()) + "Query";
        String queryPackage = this.getNamespace(
            MapperUtils.getNameComponents(MapperUtils.getPackageName(referenceDef.getQualifiedTypeName())), 
            "query"
        );
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value and filter."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param " + referenceDef.getQualifierName() + " The value for the qualifier attribute that qualifies this reference.");
        this.pw.println("   * @param query query object which is applied to the set of referenced objects.");
        this.pw.println("   * @return The filtered collection of referenced objects.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getListType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + ",");
        this.pw.println("    " + queryPackage + '.' + queryName + " query");
        this.pw.println("  );");
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceGet0_nWithFilter(
        ReferenceDef referenceDef
    ) throws ServiceException {
        this.trace("Instance/IntfReferenceGet0_nWithFilter");
        String queryName = MapperUtils.getElementName(referenceDef.getQualifiedTypeName()) + "Query";
        String queryPackage = this.getNamespace(
            MapperUtils.getNameComponents(MapperUtils.getPackageName(referenceDef.getQualifiedTypeName())), 
            "query"
        );
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves a collection containing all the elements for the reference <code>" + referenceDef.getName() + "</code>."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @return A collection containing all the elements for this reference.");
        this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getCollectionType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println();
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified filter."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param filter filter which is applied to the set of referenced objects.");
        this.pw.println("   * @return The filtered collection of referenced objects.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getListType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "Filter filter");
        this.pw.println("  );");
        this.pw.println();
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified query."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param predicate predicate which is applied to the set of referenced objects.");
        this.pw.println("   * @return The filtered collection of referenced objects.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getListType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + queryPackage + '.' + queryName + " query");
        this.pw.println("  );");
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceGet0_1WithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        this.trace("Instance/IntfReferenceGet0_1WithQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param " + referenceDef.getQualifierName() + " The value for the qualifier attribute that qualifies this reference.");
        this.pw.println("   * @return The possibly null value for this reference.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(referenceDef.getQualifiedTypeName()) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + "");
        this.pw.println("  );");
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceGet0_nNoFilter(
        ReferenceDef referenceDef
    ) throws ServiceException {
        this.trace("Instance/IntfReferenceGet0_nNoFilter");
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves the objects referenced by <code>" + referenceDef.getName() + "</code>."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @return The collection of referenced objects.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getCollectionType(referenceDef) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println();
    }
    
    // -----------------------------------------------------------------------
    public void mapIntfReferenceGet0_1NoQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceGet0_1NoQualifier");
        this.pw.println("  /**");
        this.pw.println("   * Retrieves the value for the optional reference <code>" + referenceDef.getName() + "</code>.");
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @return The possibly null value for this reference.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(referenceDef.getQualifiedTypeName()) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceAddWithQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceAddWithQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Appends the specified element to the list of all the values for the reference <code>" + referenceDef.getName() + "</code> for a specified qualifier attribute value."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param " + referenceDef.getQualifierName() + " The qualifier attribute value that qualifies the reference to get the element to be appended.");
        this.pw.println("   * @param newValue The element to be appended.");
        this.pw.println("   * @exception JmiException If the value cannot be appended for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void add" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + ",");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfReferenceAddWithoutQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceAddWithoutQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Appends the specified element to the list of all the values for the reference <code>" + referenceDef.getName() + "</code>."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue The element to be appended.");
        this.pw.println("   * @exception JmiException If the value cannot be appended for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void add" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapIntfOperation(OperationDef operationDef)
        throws ServiceException {
        this.trace("Instance/IntfOperation");
        this.pw.println("");
        if (operationDef.getAnnotation() != null) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils.wrapText("   * ", operationDef.getAnnotation()));
            this.pw.println("  */");
        }
        this.pw.println("  public " + this.getType(operationDef.getQualifiedReturnTypeName()) + " " + this.getMethodName(operationDef.getName()) + "(");
        this.pw.println("      " + this.getType(operationDef.getQualifiedInParameterTypeName()) + " params");
        this.pw.print("  ) throws javax.jmi.reflect.RefException");
        for (Iterator<ExceptionDef> i = operationDef.getExceptions().iterator(); i.hasNext();) {
            ExceptionDef exceptionDef = i.next();
            this.pw.print(", ");
            this.pw.print(this.getNamespace(MapperUtils
                .getNameComponents(MapperUtils.getPackageName(exceptionDef
                    .getQualifiedName(), 2)))
                + "." + exceptionDef.getName());
        }
        this.pw.println("  ;");
        this.pw.println("");
        this.pw.println("  /**");
        if (operationDef.getAnnotation() != null) {
            this.pw.println(MapperUtils.wrapText("   * ", operationDef.getAnnotation()));
        }
        for (Iterator<StructuralFeatureDef> i = operationDef.getParameters().iterator(); i.hasNext();) {
            StructuralFeatureDef param = i.next();
            if (param.getAnnotation() != null) {
                this.pw.println("   * @param " + param.getName() + " " + param.getAnnotation() + "");
            }
        }
        this.pw.println("   */");
        this.pw.println("  public "
            + this.getType(operationDef.getQualifiedReturnTypeName()) + " "
            + this.getMethodName(operationDef.getName()) + "(");
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
            this.pw.print(", ");
            this.pw.print(this.getNamespace(MapperUtils
                .getNameComponents(MapperUtils.getPackageName(exceptionDef
                    .getQualifiedName(), 2)))
                + "." + exceptionDef.getName());
        }
        this.pw.println("  ;");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfEnd() {
        this.trace("Instance/IntfEnd.vm");
        this.pw.println("}");
    }

    // -----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapIntfBegin()
        throws ServiceException {
        this.trace("Instance/IntfBegin");
        this.fileHeader();
        this.pw.println("package "
            + this.getNamespace(MapperUtils.getNameComponents(MapperUtils
                .getPackageName(this.classDef.getQualifiedName()))) + ";");
        this.pw.println("");
        if (this.classDef.getAnnotation() != null) {
            this.pw.println("/**");
            this.pw.println(MapperUtils.wrapText(" * ", this.classDef.getAnnotation()));
            this.pw.println(" */");
        }
        this.pw.println("@SuppressWarnings(\"unchecked\")");
        this.pw.println("public interface " + this.classDef.getName() + "");
        if (this.classDef.getSupertypes().isEmpty()) {
            if("org:openmdx:base:Authority".equals(this.classDef.getQualifiedName())) {
                this.pw.println("  extends org.openmdx.base.accessor.jmi.cci.RefAuthority_1_0");
            } else {
                this.pw.println("  extends org.openmdx.base.accessor.jmi.cci.RefObject_1_0");
            }
        } else {         
            this.pw.println("  extends");
            int ii = 0;
            for (Iterator<ClassDef> i = this.classDef.getSupertypes().iterator(); i.hasNext(); ii++) {
                ClassDef supertype = i.next();
                if (ii > 0) {
                    this.pw.println(",");
                }
                this.pw.print(" " + this.getType(supertype.getQualifiedName()));
            }
        }
        this.pw.println(" {");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeSetStream(AttributeDef attributeDef) {
        this.trace("Instance/IntfAttributeSetStream");
        this.pw.println("");
        if (PrimitiveTypes.BINARY.equals(attributeDef.getQualifiedTypeName())) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Sets a new java.io.InputStream containing the binary value for the attribute <code>" + attributeDef.getName() + "</code>."));
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param newValue A java.io.InputStream containing the binary value for this attribute.");
            this.pw.println("   * @exception JmiException If the value cannot be set for some reason.");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    java.io.InputStream newValue");
            this.pw.println("  );");
            this.pw.println("");
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Sets a new java.io.InputStream containing the binary value for the attribute <code>" + attributeDef.getName() + "</code>. This method allows to specify the stream length which typically results in better performance."));
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param newValue A java.io.InputStream containing the binary value for this attribute.");
            this.pw.println("   * @param length length of the stream in bytes.");
            this.pw.println("   * @exception JmiException If the value cannot be set for some reason.");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    java.io.InputStream newValue,");
            this.pw.println("    long length");
            this.pw.println("  );");
            this.pw.println("");
        } else if (PrimitiveTypes.STRING.equals(attributeDef
            .getQualifiedTypeName())) {
            this.pw.println("  /**");
            this.pw.println("   * Sets a new java.io.Reader containing the string value for the attribute <code>" + attributeDef.getName() + "</code>.");
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param newValue A java.io.Reader containing all the string value for this attribute.");
            this.pw.println("   * @exception JmiException If the values cannot be set for some reason.");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("     java.io.Reader newValue");
            this.pw.println("  );");
            this.pw.println("");
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Sets a new java.io.Reader containing the string value for the attribute <code>" + attributeDef.getName() + "</code>.  This method allows to specify the stream length which typically results in better performance."));
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param newValue A java.io.Reader containing all the string value for this attribute.");
            this.pw.println("   * @param length length of the stream in characters.");
            this.pw.println("   * @exception JmiException If the values cannot be set for some reason.");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("     java.io.Reader newValue,");
            this.pw.println("     long length");
            this.pw.println("  );");
        }
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeSetSet(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeSetSet");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Sets a new set containing all the new elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue A set containing all the new elements for this attribute.");
        this.pw.println("   * @exception JmiException If the values cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getSetType(attributeDef) + " newValue");
        this.pw.println("  );");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeSetMap(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeSetMap");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Sets a new map containing all the new elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue A map containing all the new elements for this attribute.");
        this.pw.println("   * @exception JmiException If the values cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getMapType(attributeDef) + " newValue");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeSetList(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeSetList");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Sets a new list containing all the new elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue A list containing all the new elements for this attribute.");
        this.pw.println("   * @exception JmiException If the values cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getListType(attributeDef) + " newValue");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Sets a new list containing all the new elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue An array containing all the new elements for this attribute.");
        this.pw.println("   * @exception JmiException If the values cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "[] newValue");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeSet1_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeSet1_1");
        this.pw.println("  /**");
        this.pw.println("   * Sets a new value for the attribute <code>" + attributeDef.getName() + "</code>.");
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue The non-null new value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   * @exception JmiException If the value cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeSet0_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeSet0_1");
        this.pw.println("");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println("   * Sets a new value for the attribute <code>" + attributeDef.getName() + "</code>.");
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue The possibly null new value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   * @exception JmiException If the value cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getObjectType(attributeDef.getQualifiedTypeName()) + " newValue");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeGetStream(AttributeDef attributeDef) {
        this.trace("Instance/IntfAttributeGetStream.vm");
        this.pw.println("");
        if (PrimitiveTypes.BINARY.equals(attributeDef.getQualifiedTypeName())) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves the value as java.io.InputStream for the binary attribute <code>" + attributeDef.getName() + "</code>."));
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @return A java.io.InputStream containing the binary value as stream for this attribute.");
            this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
            this.pw.println("   * @deprecated");
            this.pw.println("   */");
            this.pw.println("  public java.io.InputStream " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
            this.pw.println("");
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves the value as java.io.OutputStream for the binary attribute <code>" + attributeDef.getName() + "</code>."));
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param stream A java.io.OutputStream containing the binary value as output stream.");
            this.pw.println("   * @param position start streaming at given position.");
            this.pw.println("   * @return length of the stream.");
            this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
            this.pw.println("   */");
            this.pw.println("  public long " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("    java.io.OutputStream stream,");
            this.pw.println("    long position");
            this.pw.println("  );");
            this.pw.println("");
        } else if (PrimitiveTypes.STRING.equals(attributeDef.getQualifiedTypeName())) {
            this.pw.println("  /**");
            this.pw.println("   * Retrieves the value as java.io.Reader for the string attribute <code>" + attributeDef.getName() + "</code>.");
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @return A java.io.Reader containing the string value as stream for this attribute.");
            this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
            this.pw.println("   * @deprecated");
            this.pw.println("   */");
            this.pw.println("  public java.io.Reader " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
            this.pw.println("");
            this.pw.println("  /**");
            this.pw.println("   * Retrieves the value as java.io.Writer for the string attribute <code>" + attributeDef.getName() + "</code>.");
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param stream A java.io.Writer containing the string value as output stream.");
            this.pw.println("   * @param position start streaming at given position.");
            this.pw.println("   * @return length of the stream.");
            this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
            this.pw.println("   */");
            this.pw.println("  public long " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("    java.io.Writer stream,");
            this.pw.println("    long position");
            this.pw.println("  );");
        }
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeGetSparseArray(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeGetSparseArray");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a SparseArray containing all the elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return A SparseArray containing all elements for this attribute.");
        this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getSparseArrayType(attributeDef) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the element at the specified position in the SparseArray of all the values for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param index The index of the element to return.");
        this.pw.println("   * @return The element at the specified position in the SparseArray of all values for this attribute.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(attributeDef.getQualifiedTypeName()) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("    int index");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeGetSet(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeGetSet");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a set containing all the elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return A set containing all elements for this attribute.");
        this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getSetType(attributeDef) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeGetMap(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeGetMap");
        this.pw.println("  /**");
        MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a map containing all the elements for the attribute <code>" + attributeDef.getName() + "</code>.");
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return A map containing all elements for this attribute.");
        this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getMapType(attributeDef) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the element at the specified key in the map for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param key The key of the element to return.");
        this.pw.println("   * @return The element at the specified key in the map of all values for this attribute.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(attributeDef.getQualifiedTypeName()) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("    String key");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeGetList(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeGetList");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a list containing all the elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return A list containing all elements for this attribute.");
        this.pw.println("   * @exception JmiException If the values cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getListType(attributeDef) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the element at the specified position in the list of all the values for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param index The index of the element to return.");
        this.pw.println("   * @return The element at the specified position in the list of all values for this attribute.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(attributeDef.getQualifiedTypeName()) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("    int index");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeGet1_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeGet1_1");
        this.pw.println("  /**");
        this.pw.println("   * Retrieves the value for the attribute <code>" + attributeDef.getName() + "</code>.");
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return The non-null value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(attributeDef.getQualifiedTypeName()) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
    }

    // -----------------------------------------------------------------------
    public void mapIntfAttributeGet0_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/IntfAttributeGet0_1");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the possibly null value for the optional attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return The possibly null value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   * @exception JmiException If the value cannot be retrieved for some reason.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getObjectType(attributeDef.getQualifiedTypeName()) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        this.pw.println("  );");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapIntfReferenceSetWithQualifier(
        ReferenceDef referenceDef)
        throws ServiceException {
        this.trace("Instance/IntfReferenceSetWithQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Sets a list containing all the new elements for the reference <code>" + referenceDef.getName() + "</code>."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue A list containing all the new elements for this reference.");
        this.pw.println("   * @exception JmiException If the values cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(referenceDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getListType(referenceDef) + " newValue");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Sets an array containing all the new elements for the reference <code>" + referenceDef.getName() + "</code>."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue An array containing all the new elements for this reference.");
        this.pw.println("   * @exception JmiException If the values cannot be set for some reason.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(referenceDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "[] newValue");
        this.pw.println("  );");
        this.pw.println("");
    }
    
    //-----------------------------------------------------------------------
    private final ClassDef classDef;
        
}

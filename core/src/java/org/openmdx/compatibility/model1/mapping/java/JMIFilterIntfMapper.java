/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIFilterIntfMapper.java,v 1.8 2008/04/02 17:39:09 wfro Exp $
 * Description: JMIFilterTemplate 
 * Revision:    $Revision: 1.8 $
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
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassifierDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.ReferenceDef;
import org.openmdx.model1.mapping.StructDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

public class JMIFilterIntfMapper
    extends JMIAbstractMapper {

    //-----------------------------------------------------------------------
    public JMIFilterIntfMapper(
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
        return "$Id: JMIFilterIntfMapper.java,v 1.8 2008/04/02 17:39:09 wfro Exp $";
    }

    //-----------------------------------------------------------------------
    public void mapFilterInterfaceForAllQuantorComment(
        ClassifierDef classifierDef,
        StructuralFeatureDef featureDef
    ) {
        this.pw.println("  /**");
        if (
            Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity()) || 
            Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity())
        ) {
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "Adds a constraint for the attribute <code>" + featureDef.getName() + "</code> to the filter. An instance of class <code>" + classifierDef.getName() + "</code> is excluded from the result set unless its value of attribute <code>" + featureDef.getName() + "</code> satisfies the given condition."
            ));
        } else {
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "Adds a constraint for the attribute <code>" + featureDef.getName() + "</code> to the filter. An instance of class <code>" + classifierDef.getName() + "</code> is excluded from the result set unless all its values of attribute <code>" + featureDef.getName() + "</code> satisfy the given condition."
            ));
        }
        if (Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<p>Since the multiplicity for this attribute is 0..1, the attribute can have no value in which case the filter condition is met!"
            ));
        } else if (Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<p>Since the multiplicity for this attribute is 1..1, there is no difference between the filter methods <code>forAll" + featureDef.getBeanGenericName() + "</code> and <code>thereExists" + featureDef.getBeanGenericName() + "</code> for the same arguments."
            ));
        } else {
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<p>Since the multiplicity for this attribute is 0..n, the attribute can have no values in which case the filter condition is met!"
            ));            
        }
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "<p>You can set at most one <code>thereExists" + featureDef.getBeanGenericName() + "</code> or <code>forAll" + featureDef.getBeanGenericName() + "</code> constraint for this attribute."
        ));
        this.pw.println("   * @param operator The operator for this filter.");
        this.pw.println("   * @see org.openmdx.compatibility.base.query.FilterOperators");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "@param filterValues The values you want the attribute <code>" + featureDef.getName() + "</code> to be compared to."
        ));
        this.pw.println("   */");
    }

    //-----------------------------------------------------------------------
    public void mapFilterInterfaceThereExistsQuantorComment(
        ClassifierDef classifierDef,
        StructuralFeatureDef featureDef
    ) {
        this.pw.println("  /**");
        if (
            Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity()) || 
            Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity())
        ) {
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Adds a constraint for the attribute <code>" + featureDef.getName() + "</code> to the filter. An instance of class <code>" + classifierDef.getName() + "</code> is excluded from the result set unless its value of attribute <code>" + featureDef.getName() + "</code> satisfies the given condition."));
        } else {
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Adds a constraint for the attribute <code>" + featureDef.getName() + "</code> to the filter. An instance of class <code>" + classifierDef.getName() + "</code> is excluded from the result set unless at least one of its values of attribute <code>" + featureDef.getName() + "</code> satisfies the given condition."));
        }
        if (Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity())) {
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "<p>Since the multiplicity for this attribute is 0..1, the attribute can have no value in which case the filter condition is not met!"));
        } else if (Multiplicities.SINGLE_VALUE.equals(featureDef
            .getMultiplicity())) {
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "<p>Since the multiplicity for this attribute is 1..1, there is no difference between the filter methods <code>forAll" + featureDef.getBeanGenericName() + "</code> and <code>thereExists" + featureDef.getBeanGenericName() + "</code> for the same arguments."));
        } else {
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "<p>Since the multiplicity for this attribute is 0..n, the attribute can have no values in which case the filter condition is not met!"));
        }
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "<p>You can set at most one <code>thereExists" + featureDef.getBeanGenericName() + "</code> or <code>forAll" + featureDef.getBeanGenericName() + "</code> constraint for this attribute."));
        this.pw.println("   * @param operator The operator for this filter.");
        this.pw.println("   * @see org.openmdx.compatibility.base.query.FilterOperators");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "@param filterValues The values you want the attribute <code>" + featureDef.getName() + "</code> to be compared to."));
        this.pw.println("   */");
    }
    
    //-----------------------------------------------------------------------
    public void mapIntfStructureFieldIsNotStruct(
        ClassifierDef structDef,
        AttributeDef fieldDef
    ) throws ServiceException {
        this.trace("Filter/IntfStructureFieldIsNotStruct");
        this.mapFilterInterfaceForAllQuantorComment( structDef, fieldDef );
        this.pw.println("  public void forAll" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType( fieldDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceThereExistsQuantorComment( structDef, fieldDef );
        this.pw.println("  public void thereExists" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(fieldDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceForAllQuantorComment( structDef, fieldDef );
        this.pw.println("  public void forAll" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(fieldDef) + " filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceThereExistsQuantorComment( structDef, fieldDef );
        this.pw.println("  public void thereExists" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(fieldDef) + " filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println("   * Specifies the sort order of all the instances that match the filter criteria.");
        this.pw.println("   * @param order The sort order for this filter.");
        this.pw.println("   * @see org.openmdx.compatibility.base.dataprovider.cci.Directions");
        this.pw.println("   */");
        this.pw.println("  public void orderBy" + fieldDef.getBeanGenericName() + " (");
        this.pw.println("    short order");
        this.pw.println("  );");
        this.pw.println("    ");
    }
    
    //-----------------------------------------------------------------------
    public void mapIntfReference(
        ClassifierDef classDef,
        ReferenceDef referenceDef
    ) throws ServiceException {
        this.trace("Filter/IntfReference");
        this.mapFilterInterfaceForAllQuantorComment( classDef, referenceDef );
        this.pw.println("  public void forAll" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceThereExistsQuantorComment( classDef, referenceDef );
        this.pw.println("  public void thereExists" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceForAllQuantorComment( classDef, referenceDef );
        this.pw.println("  public void forAll" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(referenceDef) + " filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceThereExistsQuantorComment( classDef, referenceDef );
        this.pw.println("  public void thereExists" + referenceDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(referenceDef) + " filterValues");
        this.pw.println("  );");
        this.pw.println("       "); 
    }
    
    //-----------------------------------------------------------------------
    public void mapIntfEnd(
    ) {
        this.trace("Filter/IntfEnd");
        this.pw.println("}        ");
    }
    
    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapIntfBegin(
        ClassifierDef classifierDef
    ) throws ServiceException {
        this.trace("Filter/IntfBegin");
        this.fileHeader();
        String typeName = MapperUtils.getElementName(classifierDef.getQualifiedName());
        this.pw.println("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(classifierDef.getQualifiedName()))) + ";");
        this.pw.println("");
        this.pw.println("/**");
        this.pw.println(MapperUtils
            .wrapText(
                " * ",
                "A <code>" + classifierDef.getName() + "Filter</code> selects a set of instances of class <code>" + classifierDef.getName() + "</code> based on conditions to be met by their attributes. For each attribute there can be set at most one constraint using either its thereExists or forAll clause. An instance must meet all constraints to be member of this set."));
        this.pw.println(" */");
        this.pw.println("@SuppressWarnings(\"unchecked\")");
        this.pw.println("public interface " + typeName + "Filter");
        if(classifierDef.getSupertypes().isEmpty()) {
            this.pw.print("  extends org.openmdx.base.accessor.jmi.cci.RefFilter_1_0");
        } else {
            String prefix = "  extends ";
            for (
                Iterator<ClassifierDef> i = classifierDef.getSupertypes().iterator(); 
                i.hasNext();
                prefix = ",\n    "
            ) {
                ClassifierDef supertypeDef = i.next();
                this.pw.print(prefix + this.getType(supertypeDef.getQualifiedName()) + "Filter");
            }
        }
        this.pw.println();
        this.pw.println("{");
        this.pw.println();
    }
    
    //-----------------------------------------------------------------------
    public void mapIntfAttributeIsStruct(
        ClassifierDef classDef,
        AttributeDef attributeDef
    ) throws ServiceException {       
        this.trace("Filter/IntfAttributeIsStruct");
        this.mapFilterInterfaceForAllQuantorComment( classDef, attributeDef );
        this.pw.println("  public void forAll" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "Filter filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceThereExistsQuantorComment( classDef, attributeDef );
        this.pw.println("  public void thereExists" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "Filter filterValues");
        this.pw.println("  );");
        this.pw.println(" ");
    }
    
    //-----------------------------------------------------------------------
    public void mapIntfAttributeIsNotStruct(
        ClassifierDef classDef,
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Filter/IntfAttributeIsNotStruct");
        this.mapFilterInterfaceForAllQuantorComment( classDef, attributeDef );
        this.pw.println("  public void forAll" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceThereExistsQuantorComment( classDef, attributeDef );
        this.pw.println("  public void thereExists" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getType(attributeDef.getQualifiedTypeName()) + "[] filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceForAllQuantorComment( classDef, attributeDef );
        this.pw.println("  public void forAll" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(attributeDef) + " filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceThereExistsQuantorComment( classDef, attributeDef );
        this.pw.println("  public void thereExists" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + this.getCollectionType(attributeDef) + " filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println("   * Specifies the sort order of all the instances that match the filter criteria.");
        this.pw.println("   * @param order The sort order for this filter.");
        this.pw.println("   * @see org.openmdx.compatibility.base.dataprovider.cci.Directions");
        this.pw.println("   */");
        this.pw.println("  public void orderBy" + attributeDef.getBeanGenericName() + " (");
        this.pw.println("    short order");
        this.pw.println("  );");
        this.pw.println("       "); 
    }
    
    //-----------------------------------------------------------------------
    public void mapIntfStructureFieldIsStruct(
        StructDef structDef,
        AttributeDef structureFieldDef
    ) throws ServiceException {
        this.trace("Filter/IntfStructureFieldIsStruct");
        this.mapFilterInterfaceForAllQuantorComment( structDef, structureFieldDef );
        this.pw.println("  public void forAll" + structureFieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + getType(structureFieldDef.getQualifiedTypeName()) + "Filter filterValues");
        this.pw.println("  );");
        this.pw.println("");
        this.mapFilterInterfaceThereExistsQuantorComment( structDef, structureFieldDef );
        this.pw.println("  public void thereExists" + structureFieldDef.getBeanGenericName() + " (");
        this.pw.println("    short operator,");
        this.pw.println("    " + getType(structureFieldDef.getQualifiedTypeName()) + "Filter filterValues");
        this.pw.println("  );");
        this.pw.println("       "); 
    }
      
    //-----------------------------------------------------------------------
    
}

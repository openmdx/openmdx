/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIQueryMapper.java,v 1.12 2008/04/02 17:39:09 wfro Exp $
 * Description: JMI Query Mapper
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/02 17:39:09 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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
import org.openmdx.model1.mapping.ClassifierDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.ReferenceDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

/**
 * JMI Query Mapper
 */
public class JMIQueryMapper
    extends JMIAbstractMapper {

    /**
     * Constructor 
     *
     * @param writer
     * @param model
     * @param format
     * @param packageSuffix
     */
    public JMIQueryMapper(
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
        return "$Id: JMIQueryMapper.java,v 1.12 2008/04/02 17:39:09 wfro Exp $";
    }

    /**
     * @param classDef
     * @param featureDef
     * @throws ServiceException
     */
    public void mapStructurealFeature(
        ClassifierDef classDef,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.trace("Query/Feature");
        String queryType = getQueryType(featureDef.getQualifiedTypeName()); 
        boolean qualifiedReference =
            featureDef instanceof ReferenceDef &&
            ((ReferenceDef)featureDef).getQualifiedQualifierTypeName() != null;
        if(
            Multiplicities.SINGLE_VALUE.equals(featureDef.getMultiplicity()) &&
            !qualifiedReference
        ) {
            //
            // single-valued mandatory feature
            //
            this.pw.println("  /**");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "Adds a constraint for the feature <code>" + featureDef.getName() + "</code> to the predicate. The predicate for <code>" + 
                classDef.getName() + "</code> evaluates <code>true</code> if its value of feature <code>" + featureDef.getName() + 
                "</code> satisfies the selected condition."
            ));
            this.pw.println("   */");
            this.pw.println("  public " + queryType + ' ' + getPredicateName(null, featureDef) + '(');
            this.pw.println("  );");
            this.pw.println();
        } else {
            //
            // optional or multi-valued feature
            //
            if(
                Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity()) &&
                !qualifiedReference
            ) {
                //
                // is null?
                // 
                this.pw.println("  /**");
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "Adds a constraint to the predicate for <code>" + classDef.getName() + 
                        "</code> testing whether the value of the feature <code>" + featureDef.getName() + 
                        "</code> is <code>null</code> or not."
                    )
                );
                this.pw.println("   */");
                this.pw.println("  public org.w3c.cci2.OptionalFeaturePredicate " + getPredicateName(null, featureDef) + '(');
                this.pw.println("  );");
                this.pw.println();
            } else {
                //
                // is empty?
                // 
                this.pw.println("  /**");
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "Adds a constraint to the predicate for <code>" + classDef.getName() + 
                        "</code> testing whether the feature <code>" + featureDef.getName() + 
                        "</code> has values or not."
                    )
                );
                this.pw.println("   */");
                this.pw.println("  public org.w3c.cci2.MultivaluedFeaturePredicate " + getPredicateName(null, featureDef) + '(');
                this.pw.println("  );");
                this.pw.println();
            }
            //
            // there exists all
            //
            this.pw.println("  /**");
            if (
                Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity())
            ) {
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "Adds a constraint for the feature <code>" + featureDef.getName() + "</code> to the predicate. The predicate for <code>" + 
                        classDef.getName() + "</code> evaluates <code>true</code> if its value of feature <code>" + featureDef.getName() + 
                        "</code> satisfies the selected condition."
                    )
                );
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "<p>Since the multiplicity for this attribute is 0..1, the attribute can have no value in which case the predicate evaluates to <code>false</code>!"
                    )
                );
            } else {
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "Adds a constraint for the feature <code>" + featureDef.getName() + 
                        "</code> to the predicate. The predicate for <code>" + classDef.getName() + 
                        "</code> evaluates <code>true</code> if at least one of its values of feature <code>" + featureDef.getName() + 
                        "</code> satisfies the given condition."
                    )
                );
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "<p>Since the multiplicity for this attribute is 0..n, the attribute can have no values in which case the predicate evaluates to <code>false</code>!"
                    )
                );
            }
            this.pw.println("   */");
            this.pw.println("  public " + queryType + ' ' + getPredicateName("thereExists", featureDef) + '(');
            this.pw.println("  );");
            this.pw.println();
            //
            // for all
            //
            this.pw.println("  /**");
            if (
                Multiplicities.OPTIONAL_VALUE.equals(featureDef.getMultiplicity())
            ) {
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "Adds a constraint for the feature <code>" + featureDef.getName() + "</code> to the predicate. The predicate for <code>" + 
                        classDef.getName() + "</code> evaluates <code>true</code> if its value of feature <code>" + featureDef.getName() + 
                        "</code> satisfies the selected condition."
                    )
                );
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "<p>Since the multiplicity for this attribute is 0..1, the attribute can have no value in which case the predicate evaluates to <code>true</code>!"
                    )
                );
            } else {
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "Adds a constraint for the feature <code>" + featureDef.getName() + 
                        "</code> to the predicate. The predicate for <code>" + classDef.getName() + 
                        "</code> evaluates <code>true</code> if all of its values of feature <code>" + featureDef.getName() + 
                        "</code> satisfy the selected condition."
                    )
                );
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "<p>Since the multiplicity for this attribute is 0..n, the attribute can have no values in which case the predicate evaluates to <code>true</code>!"
                    )
                );
            }
            this.pw.println("   */");
            this.pw.println("  public " + queryType + ' ' + getPredicateName("forAll", featureDef) + '(');
            this.pw.println("  );");
            this.pw.println();
        }
        if(this.model.isPrimitiveType(featureDef.getQualifiedTypeName())) {
            //
            // order by
            // 
            this.pw.println("  /**");
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "Allows to adds a sort instruction for <code>" + classDef.getName() + 
                    "</code> depending the feature <code>" + featureDef.getName() + "</code>"
                )
            );
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "Note: <em>The order in which </em><code>orderBy&hellip;</code><em> instructions are given is relevant!</em>"
                )
            );
            this.pw.println("   */");
            this.pw.println("    public " + getOrderType(featureDef) + ' ' + getPredicateName("orderBy", featureDef) + '(');
            this.pw.println("  );");
            this.pw.println();
        }
    }

    /**
     * 
     */
    public void mapEnd(
    ) {
        this.trace("Query/End");
        this.pw.println("}        ");
    }

    /**
     * 
     * @param classifierDef
     * @throws ServiceException
     */
    @SuppressWarnings("unchecked")
    public void mapBegin(
        ClassifierDef classifierDef
    ) throws ServiceException {
        this.trace("Query/Begin");
        this.fileHeader();
        String qualifiedName = classifierDef.getQualifiedName();
        this.pw.println(
            "package " + this.getNamespace(
                MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedName)), 
                "query"
            ) + ";"
        );
        this.pw.println();
        this.pw.println("/**");
        this.pw.println(MapperUtils.wrapText(
            " * ",
            "A <code>" + classifierDef.getName() + "Query</code> selects a set of instances of class <code>" + classifierDef.getName() + 
              "</code> based on conditions to be met by their attributes. "
        ));
        this.pw.println(" */");
        this.pw.println("@SuppressWarnings(\"unchecked\")");
        this.pw.println("public interface " + MapperUtils.getElementName(qualifiedName) + "Query");
        this.pw.print("  extends ");
        if(classifierDef.getSupertypes().isEmpty()) {
            this.pw.println("org.w3c.cci2.AnyTypePredicate");
        } else {
            String prefix = "";
            for (
                Iterator<ClassifierDef> i = classifierDef.getSupertypes().iterator(); 
                i.hasNext(); 
                prefix = ",\n    "
            ) {
                ClassifierDef supertypeDef = i.next();
                this.pw.print(
                    prefix + getNamespace(
                        MapperUtils.getNameComponents(MapperUtils.getPackageName(supertypeDef.getQualifiedName())), 
                        "query"
                    ) + '.' + supertypeDef.getName() + "Query"
                );
            }
            this.pw.println();
        }
        this.pw.println("{");
        this.pw.println();
    }
        
}

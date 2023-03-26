/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: JMI Query Mapper
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.application.mof.mapping.java;

import java.io.Writer;
import java.util.Iterator;

import org.omg.mof.spi.Identifier;
import org.openmdx.application.mof.mapping.cci.ClassifierDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;

/**
 * JMI Query Mapper
 */
public class QueryMapper
    extends AbstractMapper {

    /**
     * Constructor 
     *
     * @param writer
     * @param model
     * @param format 
     * @param packageSuffix
     * @param metaData 
     * @param markdown TODO
     * @param primitiveTypeMapper 
     */
    public QueryMapper(
        Writer writer,
        Model_1_0 model,
        Format format, 
        String packageSuffix, 
        MetaData_1_0 metaData, 
        boolean markdown, 
        																																																															PrimitiveTypeMapper primitiveTypeMapper
    ) {
        super(
            writer,
            model,
            format, 
            packageSuffix,
            metaData, 
            markdown, 
            primitiveTypeMapper
        );
    }
    
    /**
     * @param classDef
     * @param featureDef
     * @throws ServiceException
     */
    public void mapStructuralFeature(
        ClassifierDef classDef,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.trace("Query/Feature");
        String queryType = getQueryType(featureDef.getQualifiedTypeName(), null); 
        boolean qualifiedReference =
            featureDef instanceof ReferenceDef &&
            ((ReferenceDef)featureDef).getQualifiedQualifierTypeName() != null;
        if(
            Multiplicity.SINGLE_VALUE.toString().equals(featureDef.getMultiplicity()) &&
            !qualifiedReference
        ) {
            //
            // single-valued mandatory feature
            //
            printLine("  /**");
            MapperUtils.wrapText(
                "   * ",
                "Adds a constraint for the feature {@code " + featureDef.getName() + "} to the predicate. The predicate for {@code " + 
                classDef.getName() + "} evaluates {@code true} if its value of feature {@code " + featureDef.getName() + 
                "} satisfies the selected condition.", this::printLine
            );
            printLine("   */");
            printLine("  public " + queryType + ' ' + getPredicateName(null, featureDef) + '(');
            printLine("  );");
            newLine();
        } else {
            //
            // optional or multi-valued feature
            //
            if(
                Multiplicity.OPTIONAL.toString().equals(featureDef.getMultiplicity()) &&
                !qualifiedReference
             ) {
                //
                // is null?
                // 
                printLine("  /**");
                MapperUtils.wrapText(
                    "   * ",
                    "Adds a constraint to the predicate for {@code " + classDef.getName() + 
                    "} testing whether the value of the feature {@code " + featureDef.getName() + 
                    "} is {@code null} or not.", this::printLine
                );
                printLine("   */");
                printLine("  public org.w3c.cci2.OptionalFeaturePredicate " + getPredicateName(null, featureDef) + '(');
                printLine("  );");
                newLine();
            } else {
                //
                // is empty?
                // 
                printLine("  /**");
                MapperUtils.wrapText(
                    "   * ",
                    "Adds a constraint to the predicate for {@code " + classDef.getName() + 
                    "} testing whether the feature {@code " + featureDef.getName() + 
                    "} has values or not.", this::printLine
                );
                printLine("   */");
                printLine("  public org.w3c.cci2.MultivaluedFeaturePredicate " + getPredicateName(null, featureDef) + '(');
                printLine("  );");
                newLine();
            }
            //
            // there exists
            //
            printLine("  /**");
            if (
                Multiplicity.OPTIONAL.code().equals(featureDef.getMultiplicity())
            ) {
                MapperUtils.wrapText(
                    "   * ",
                    "Adds a condition for the feature {@code " + featureDef.getName() + "} to the predicate for {@code " + 
                    classDef.getName() + "}, which evaluates to {@code false} unless the value of the feature {@code " + 
                    featureDef.getName() + "} satisfies the given condition.", this::printLine
                );
                MapperUtils.wrapText(
                    "   * ",
                    "<p>Since the attribute is optional its value  may be {@code null}, " +
                    "in which case the condition is <em>not satisfied</em>", this::printLine
                );
            } else {
                MapperUtils.wrapText(
                    "   * ",
                    "Adds a condition for the feature {@code " + featureDef.getName() + "} to the predicate for {@code " + 
                    classDef.getName() + "}, which evaluates to {@code false} unless the values of the feature {@code " + 
                    featureDef.getName() + "} satisfy the given condition.", this::printLine
                );
                MapperUtils.wrapText(
                    "   * ",
                    "<p>Since the multiplicity for this attribute is 0..n, the attribute may have no values. " +
                    "in which case the condition is <em>not satisfied</em>", this::printLine
                );
            }
            printLine("   */");
            printLine("  public " + queryType + ' ' + getPredicateName("thereExists", featureDef) + '(');
            printLine("  );");
            newLine();
            //
            // for all
            //
            printLine("  /**");
            if (
                Multiplicity.OPTIONAL.code().equals(featureDef.getMultiplicity())
            ) {
                MapperUtils.wrapText(
                    "   * ",
                    "Adds a condition for the feature {@code " + featureDef.getName() + "} to the predicate for {@code " + 
                    classDef.getName() + "}, which evaluates to {@code false} unless the value of the feature {@code " + 
                    featureDef.getName() + "} satisfies the given condition.", this::printLine
                );
                MapperUtils.wrapText(
                    "   * ",
                    "<p>Since the attribute is optional its value  may be {@code null}, " +
                    "in which case the condition is <em>satisfied</em>", this::printLine
                );
            } else {
                MapperUtils.wrapText(
                    "   * ",
                    "Adds a condition for the feature {@code " + featureDef.getName() + "} to the predicate for {@code " + 
                    classDef.getName() + "}, which evaluates to {@code false} unless the values of the feature {@code " + 
                    featureDef.getName() + "} satisfy the given condition.", this::printLine
                );
                MapperUtils.wrapText(
                    "   * ",
                    "<p>Since the multiplicity for this attribute is 0..n, the attribute may have no values. " +
                    "in which case the condition is <em>satisfied</em>", this::printLine
                );
            }
            printLine("   */");
            printLine("  public " + queryType + ' ' + getPredicateName("forAll", featureDef) + '(');
            printLine("  );");
            newLine();
        }
        if(this.model.isPrimitiveType(featureDef.getQualifiedTypeName())) {
            String orderType = getOrderType(featureDef);
            if(orderType != null) {
                //
                // order by
                // 
                printLine("  /**");
                MapperUtils.wrapText(
                    "   * ",
                    "Allows to adds a sort instruction for {@code " + classDef.getName() + 
                    "} depending the feature {@code " + featureDef.getName() + "}", this::printLine
                );
                MapperUtils.wrapText(
                    "   * ",
                    "Note: <em>The order in which </em>{@code orderBy&hellip;}<em> instructions are given is relevant!</em>", this::printLine
                );
                printLine("   */");
                printLine("    public " + orderType + ' ' + getPredicateName("orderBy", featureDef) + '(');
                printLine("  );");
                newLine();
            }
        }
    }

    /**
     * 
     */
    public void mapEnd(
    ) {
        this.trace("Query/End");
        printLine("}        ");
    }

    /**
     * 
     * @param classifierDef
     * @throws ServiceException
     */
    public void mapBegin(
        ClassifierDef classifierDef
    ) throws ServiceException {
        this.trace("Query/Begin");
        this.fileHeader();
        String qualifiedName = classifierDef.getQualifiedName();
        printLine(
            "package " + this.getNamespace(
                MapperUtils.getNameComponents(
                    MapperUtils.getPackageName(qualifiedName)
                )
            ) + ';'
        );
        newLine();
        printLine("/**");
        MapperUtils.wrapText(
            " * ",
            "A {@code " + classifierDef.getName() + "Query} selects a set of instances of class {@code " + classifierDef.getName() + 
              "} based on conditions to be met by their attributes. ", this::printLine
        );
        printLine(" */");
        printLine("public interface " + Identifier.CLASS_PROXY_NAME.toIdentifier(MapperUtils.getElementName(qualifiedName), null, null,null, "Query"));
        print("  extends ");
        if(classifierDef.getSupertypes().isEmpty()) {
            printLine("org.w3c.cci2.AnyTypePredicate");
        } else {
            String prefix = "";
            for (
                Iterator<?> i = classifierDef.getSupertypes().iterator(); 
                i.hasNext(); 
                prefix = ",\n    "
            ) {
                ClassifierDef supertypeDef = (ClassifierDef) i.next();
                print(
                    prefix + getNamespace(
                        MapperUtils.getNameComponents(MapperUtils.getPackageName(supertypeDef.getQualifiedName())) 
                    ) + '.' + Identifier.CLASS_PROXY_NAME.toIdentifier(supertypeDef.getName(), null, null, null, "Query")
                );
            }
            newLine();
        }
        printLine("{");
        newLine();
    }
        
}

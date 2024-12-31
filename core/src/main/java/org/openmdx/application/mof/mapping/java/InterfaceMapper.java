/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Interface Mapper
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import org.omg.mof.spi.Identifier;
import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.externalizer.spi.JMIFlavour;
import org.openmdx.application.mof.externalizer.spi.JakartaFlavour;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.java.metadata.FieldMetaData;
import org.openmdx.application.mof.mapping.java.metadata.Visibility;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.kernel.exception.BasicException;

/**
 * Interface Mapper for SPI
 */
public class InterfaceMapper
extends AbstractClassMapper {

    /**
     * Constructor 
     * @param markdown TODO
     */
    public InterfaceMapper(
        ModelElement_1_0 classDef,
        Writer writer,
        Model_1_0 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData, 
        AnnotationFlavour annotationFlavour, 
        JakartaFlavour jakartaFlavour,
        JMIFlavour jmiFlavour,
        PrimitiveTypeMapper primitiveTypeMapper
    ) throws ServiceException {
        super(
            classDef,
            writer,
            model,
            format, 
            packageSuffix, 
            metaData, 
            annotationFlavour, 
            jakartaFlavour, 
            jmiFlavour, 
            primitiveTypeMapper
        );
    }

    /**
     * 
     * @throws ServiceException
     */
    public void mapEnd(
    ) throws ServiceException {
        this.trace("Interface/End.vm");
        newLine();
        mapPrivateFields();
        printLine("}");
    }

    /**
     * 
     * @throws ServiceException
     */
    public void mapBegin(
    ) throws ServiceException {
        this.trace("Interface/Begin");
        printLine(
            "package ",
            this.getNamespace(
                MapperUtils.getNameComponents(
                    MapperUtils.getPackageName(
                        this.classDef.getQualifiedName()
                    )
                )
            ),
            ";"
        );
        newLine();
        printLine("/**");
        print(" * Service Provider Interface {@code " + this.classDef.getName() + "}"); 
        mapAnnotation(" * ", this.classDef);
        printLine(" */");
        this.mapGeneratedAnnotation();
        printLine("public interface ", this.className, " extends ", this.interfaceType(this.classDef, Visibility.CCI, false), " {"); 
    }

    /**
     * 
     *
     */
    private void mapPrivateFields(
    ) throws ServiceException {
        for(FieldMetaData field : this.spiFeatures){
            this.trace("Interface/PrivateFeature");
            boolean singlevalued = !isMultivalued(field);
            boolean flag = PrimitiveTypes.BOOLEAN.equals(getType(field));
            //
            // Accessor
            //
            printLine("  /**");
            MapperUtils.wrapText(
                "   * ",
                "Retrieves the " + (
                        singlevalued ? "value" : "values"
                ) + "for the attribute {@code " + field.getName() + "}.", this::printLine
            );
            printLine("   * @return The value for attribute {@code " + field.getName() + "}.");
            printLine("   */");
            print("   " + field.getFieldType() + " ");
            print(
                Identifier.OPERATION_NAME.toIdentifier(
                    field.getName(), 
                    flag ? "is" : null, // removablePrefix 
                        flag && singlevalued ? "is" : "get", // prependablePrefix
                            null
                            , null // appendableSuffix
                )
            );
            printLine("(");
            printLine("  );");
            newLine();
            //
            // Mutator
            //
            printLine("  /**");
            MapperUtils.wrapText(
                "   * ",
                "Sets the " + (
                        singlevalued ? "value" : "values"
                ) + "for the attribute {@code " + field.getName() + "}.", this::printLine
            );
            printLine("   * @param " + field.getName() + " The new value for attribute {@code " + field.getName() + "}");
            printLine("   */");
            print("  void ");
            print(
                Identifier.OPERATION_NAME.toIdentifier(
                    field.getName(), 
                    flag ? "is" : null, // removablePrefix 
                        "set", // prependablePrefix
                        null
                        , null // appendableSuffix
                )
            );
            printLine("(");
            printLine(
                "    " + getType(field) + (
                        singlevalued ? " " : "... "
                ) + field.getName()
            );
            printLine("  );");
            newLine();
        }
    }

    /**
     * 
     * @param fieldMetaData
     * @return
     */
    private boolean isMultivalued(
        FieldMetaData fieldMetaData
    ){
        String fieldType = fieldMetaData.getFieldType();
        return 
        fieldType.startsWith("java.util.Set<") ||
        fieldType.startsWith("java.util.List<");
    }

    /**
     * 
     * @param fieldMetaData
     * @return
     */
    private String getType(
        FieldMetaData fieldMetaData
    ) throws ServiceException {
        String fieldType = fieldMetaData.getFieldType();
        if(fieldType == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE, 
                "private field has unknown type. Type of private fields must be declared in meta data file (.openmdxjdo)",
                new BasicException.Parameter("interface", this.classDef.getQualifiedName()),
                new BasicException.Parameter("field", fieldMetaData.getName())
            );
        }
        int i = fieldType.indexOf('<');
        return i < 0 ? fieldType : fieldType.substring(i + 1, fieldType.length() - 1);
    }

}

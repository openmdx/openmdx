/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InterfaceMapper.java,v 1.5 2008/09/10 08:55:26 hburger Exp $
 * Description: Interface Mapper
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:26 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.importer.metadata.FieldMetaData;
import org.openmdx.model1.importer.metadata.Visibility;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MetaData_1_0;

/**
 * Interface Mapper for SPI
 */
public class InterfaceMapper
extends AbstractClassMapper {

    /**
     * Constructor 
     *
     * @param classDef
     * @param writer
     * @param model
     * @param format
     * @param packageSuffix
     * @param metaData
     * @throws ServiceException
     */
    public InterfaceMapper(
        ModelElement_1_0 classDef,
        Writer writer,
        Model_1_3 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData
    ) throws ServiceException {
        super(
            classDef,
            writer,
            model,
            format, 
            packageSuffix, 
            metaData
        );
    }

    /**
     * 
     * @throws ServiceException
     */
    public void mapEnd(
    ) throws ServiceException {
        this.trace("Interface/End.vm");
        this.pw.println();
        mapPrivateFields();
        this.pw.println("}");
    }

    /**
     * 
     * @throws ServiceException
     */
    public void mapBegin(
    ) throws ServiceException {
        this.trace("Interface/Begin");
        this.fileHeader();
        this.pw.println(
            "package " + this.getNamespace(
                MapperUtils.getNameComponents(
                    MapperUtils.getPackageName(
                        this.classDef.getQualifiedName()
                    )
                )
            ) + ';'
        );
        this.pw.println();
        this.pw.println("/**");
        this.pw.print(" * Service Provider Interface <code>" + this.classDef.getName() + "</code>"); 
        if (this.classDef.getAnnotation() != null) {
            this.pw.println(" *<p>");
            this.pw.println(MapperUtils.wrapText(" * ", this.classDef.getAnnotation()));
        }
        this.pw.println(" */");
        this.pw.println(
            "public interface " + this.className + " extends " +
            this.interfaceType(this.classDef, Visibility.CCI, false) + " {"
        ); 
    }

    /**
     * 
     *
     */
    private void mapPrivateFields(
    ) throws ServiceException {
        for(FieldMetaData field : this.spiFeatures){
            this.trace("Interface/PrivateFeature");
            String fieldType = getType(field);
            boolean singlevalued = !isMultivalued(field);
            boolean flag = PrimitiveTypes.BOOLEAN.equals(fieldType);
            //
            // Accessor
            //
            this.pw.println("  /**");
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "Retrieves the " + (
                            singlevalued ? "value" : "values"
                    ) + "for the attribute <code>" + field.getName() + "</code>."
                )
            );
            this.pw.println("   * @return The value for attribute <code>" + field.getName() + "</code>.");
            this.pw.println("   */");
            this.pw.print("   " + field.getFieldType() + " ");
            this.pw.print(
                Identifier.OPERATION_NAME.toIdentifier(
                    field.getName(), 
                    flag ? "is" : null, // removablePrefix 
                        flag && singlevalued ? "is" : "get", // prependablePrefix
                            null
                            , null // appendableSuffix
                )
            );
            this.pw.println("(");
            this.pw.println("  );");
            this.pw.println();
            //
            // Mutator
            //
            this.pw.println("  /**");
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "Sets the " + (
                            singlevalued ? "value" : "values"
                    ) + "for the attribute <code>" + field.getName() + "</code>."
                )
            );
            this.pw.println("   * @param " + field.getName() + " The new value for attribute <code>" + field.getName() + "</code>");
            this.pw.println("   */");
            this.pw.print("  void ");
            this.pw.print(
                Identifier.OPERATION_NAME.toIdentifier(
                    field.getName(), 
                    flag ? "is" : null, // removablePrefix 
                        "set", // prependablePrefix
                        null
                        , null // appendableSuffix
                )
            );
            this.pw.println("(");
            this.pw.println(
                "    " + fieldType + (
                        singlevalued ? " " : "... "
                ) + field.getName()
            );
            this.pw.println("  );");
            this.pw.println();
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

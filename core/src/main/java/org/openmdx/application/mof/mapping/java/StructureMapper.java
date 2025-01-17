/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Structure Template 
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
import java.util.ArrayList;
import java.util.List;

import org.omg.mof.spi.AbstractNames;
import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.externalizer.spi.JMIFlavour;
import org.openmdx.application.mof.externalizer.spi.JakartaFlavour;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.StructDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;

/**
 * Structure Mapper
 */
public class StructureMapper extends AbstractMapper {
    
    /**
     * Constructor 
     */
    public StructureMapper(
        ModelElement_1_0 structDef,
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
        this.structDef = new StructDef(
            structDef, 
            model
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
        printLine("  /**");
        MapperUtils.wrapText(
            "   * ",
            "Retrieves a SparseArray containing all the elements for the structure field {@code " + fieldDef.getName() + "}.", this::printLine
        );
        mapAnnotation("   * ", fieldDef);
        printLine("   * @return A SparseArray containing all elements for this structure field.");
        printLine("   */");
        String memberType = this.getType(fieldDef, "org.w3c.cci2.SparseArray", Boolean.TRUE, TypeMode.MEMBER, null);
        printLine("  public ", memberType, " ", this.getMethodName(fieldDef.getBeanGetterName()), "(");
        printLine("  );");
        newLine();
    }

    //-----------------------------------------------------------------------
    public void mapFieldGetSet(
        StructuralFeatureDef fieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGetSet");
        this.members.add(fieldDef.getBeanGenericName());
        printLine("  /**");
        MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a set containing all the elements for the structure field {@code " + fieldDef.getName() + "}.", this::printLine);
        mapAnnotation("   * ", fieldDef);
        printLine("   * @return A set containing all elements for this structure field.");
        printLine("   */");
        String memberType = this.getType(fieldDef, "java.util.Set", Boolean.TRUE, TypeMode.MEMBER, null);
        printLine("  public ", memberType, " ", this.getMethodName(fieldDef.getBeanGetterName()), "(");
        printLine("  );");        
        newLine();
    }

    //-----------------------------------------------------------------------
    public void mapFieldGetList(
        StructuralFeatureDef fieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGetList");
        this.members.add(fieldDef.getBeanGenericName());
        if(getFormat() != Format.JMI1) {
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves a list containing all the elements for the structure field {@code " + fieldDef.getName() + "}.", this::printLine);
            mapAnnotation("   * ", fieldDef);
            printLine("   * @return A list containing all elements for this structure field.");
            printLine("   */");
            String memberType = this.getType(fieldDef, "java.util.List", Boolean.TRUE, TypeMode.MEMBER, null);
            printLine("  public ", memberType, " ", this.getMethodName(fieldDef.getBeanGetterName()), "(");
            printLine("  );");        
            newLine();
        }
    }

    //-----------------------------------------------------------------------
    public void mapFieldGet1_1(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGet1_1");
        this.members.add(structureFieldDef.getBeanGenericName());
        printLine("  /**");
        printLine("   * Retrieves the value for the structure field {@code ", structureFieldDef.getName(), "}.");
        mapAnnotation("   * ", structureFieldDef);
        printLine("   * @return The non-null value for structure field {@code ", structureFieldDef.getName(), "}.");
        printLine("   */");
        String memberType = this.getType(structureFieldDef.getQualifiedTypeName(), getFormat(), false);
        printLine("  public ", memberType, " ", this.getMethodName(structureFieldDef.getBeanGetterName()), "(");
        printLine("  );");        
        newLine();
    }

    //-----------------------------------------------------------------------
    public void mapFieldGet0_1(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        this.trace("StructureType/FieldGet0_1");
        this.members.add(structureFieldDef.getBeanGenericName());
        printLine("  /**");
        printLine("   * Retrieves the value for the optional structure field {@code ", structureFieldDef.getName(), "}.");
        mapAnnotation("   * ", structureFieldDef);
        printLine("   * @return The possibly null value for structure field {@code ", structureFieldDef.getName(), "}.");
        printLine("   */");
        String memberType = this.getType(structureFieldDef.getQualifiedTypeName(), getFormat(), true);
        printLine("  public ", memberType, " ", this.getMethodName(structureFieldDef.getBeanGetterName()), "(");
        printLine("  );");        
        newLine();
    }

    //-----------------------------------------------------------------------
    public void mapFieldGetStream(
        StructuralFeatureDef structureFieldDef
    ) {
        this.trace("StructureType/FieldGetStream.vm");
        this.members.add(structureFieldDef.getBeanGenericName());
        newLine();
        if (PrimitiveTypes.BINARY.equals(structureFieldDef.getQualifiedTypeName())) {
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves the value as java.io.InputStream for the binary structure field {@code " + structureFieldDef.getName() + "}.", this::printLine);
            mapAnnotation("   * ", structureFieldDef);
            printLine("   * @return A InputStream containing the binary value as stream for this structure field.");
            printLine("   */");
            printLine("  public java.io.InputStream ", this.getMethodName(structureFieldDef.getBeanGetterName()), "(");
            printLine("  );");
            newLine();
        } 
        else if (PrimitiveTypes.STRING.equals(structureFieldDef.getQualifiedTypeName())) {
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves the value as java.io.Reader for the string structure field {@code " + structureFieldDef.getName() + "}.", this::printLine);
            mapAnnotation("   * ", structureFieldDef);
            printLine("   * @return A Reader containing the string value as stream for this structure field.");
            printLine("   */");
            printLine("  public java.io.Reader ", this.getMethodName(structureFieldDef.getBeanGetterName()), "(");
            printLine("  );");
            newLine();
        } 
        else {
            newLine();
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Retrieves the value as streaming Collection for the structure field {@code " + structureFieldDef.getName() + "].", this::printLine);
            mapAnnotation("   * ", structureFieldDef);
            printLine("   * @return A Collection containing the value as stream for this structure field.");
            printLine("   */");
            printLine("  public java.io.DataInput ", this.getMethodName(structureFieldDef.getBeanGetterName()), "(");
            printLine("  );");
            newLine();
        }
    }

    //-----------------------------------------------------------------------
    public void mapEnd(
    ) throws ServiceException {
        newLine();
        if(getFormat() == Format.CCI2) {
            this.trace("StructureType/Member");
            newLine();
            printLine("  /**");
            printLine("   * The structure's members");
            printLine("   */");
            printLine("  enum Member {");
            String delimiter = "    ";
            if(this.members.isEmpty()) {
                printLine(delimiter, "// No members");
            } else {
                for(String member : this.members) {
                    printLine(delimiter, AbstractNames.uncapitalize(member));
                    delimiter = "  , ";
                }
            }
            printLine("  }");
            newLine();
        }
        this.trace("StructureType/End");
        printLine("}");
    }

    //-----------------------------------------------------------------------
    public void mapBegin(
    ) throws ServiceException {
        this.trace("StructureType/Begin");
        List<String> nameComponents = MapperUtils.getNameComponents(MapperUtils.getPackageName(this.structDef.getQualifiedName()));
        printLine("package ", this.getNamespace(nameComponents), ";");
        newLine();
        this.mapGeneratedAnnotation();
        printLine("public interface ", this.structName);
        if(getFormat() == Format.JMI1) {            
            print("  extends " + REF_STRUCT_INTERFACE_NAME + ", ");
        }
        if(getFormat() != Format.CCI2) {
            printLine(
                getNamespace(nameComponents, Names.CCI2_PACKAGE_SUFFIX),    
                ".",
                this.structName
            );
        }
        printLine("{");
        this.members = new ArrayList<String>();
    }

    //-----------------------------------------------------------------------
    static final String REF_STRUCT_INTERFACE_NAME = "org.openmdx.base.accessor.jmi.cci.RefStruct_1_0";
    
    private final StructDef structDef;
    private final String structName;
    private List<String> members;
    
}

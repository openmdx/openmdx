/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Struct Features Mapper 
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
package org.openmdx.application.mof.mapping.java.mof;

import java.io.Writer;

import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.StructDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.Format;
import org.openmdx.application.mof.mapping.java.StandardPrimitiveTypeMapper;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.Version;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Struct Features Mapper
 */
public class StructFeaturesMapper extends FeaturesMapper {

    public StructFeaturesMapper(
        ModelElement_1_0 structDef,
        Writer writer,
        Model_1_0 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData, 
        boolean markdown
    ) throws ServiceException {
        super(
    		structDef,
            writer,
            model,
            format, 
            packageSuffix,
            metaData, 
            markdown, 
            new StandardPrimitiveTypeMapper()
        );
        this.structDef = new StructDef(
        	structDef, 
        	model
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return Version.getImplementationVersion();
    }
    
    /**
     * Map field
     * 
     * @param structureFieldDef
     * 
     * @throws ServiceException
     */
    public void mapField(
        StructuralFeatureDef structureFieldDef
    ) throws ServiceException {
        printLine("  /**");
        MapperUtils.wrapText(
            "   * ",
            "Struct field {@code " + structureFieldDef.getName() + "}.", 
            this::printLine
        );
        printLine("   */");
        print("    java.lang.String ");
        print(getConstantName(structureFieldDef.getName()));
        print(" = \"");
        print(structureFieldDef.getName());
        printLine("\";");
        newLine();
    }

    public void mapEnd() {
        printLine("}");
    }

    public void mapBegin() {
        this.fileHeader();
        printLine("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.structDef.getQualifiedName()))) + ";");
        newLine();
        printLine("public interface " + this.structDef.getName() + FEATURES_INTERFACE_SUFFIX);
        printLine("  extends org.openmdx.base.accessor.jmi.cci.RefStruct_1_0 {");
        newLine();
    }

    /**
     * 
     */
    private final StructDef structDef;
        
}

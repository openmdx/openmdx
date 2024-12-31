/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Class Features Mapper
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
import java.util.Iterator;

import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.externalizer.spi.JMIFlavour;
import org.openmdx.application.mof.externalizer.spi.JakartaFlavour;
import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.OperationDef;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.java.Format;
import org.openmdx.application.mof.mapping.java.StandardPrimitiveTypeMapper;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.Version;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * Class Features Mapper
 */
public class InstanceFeaturesMapper extends FeaturesMapper {

    public InstanceFeaturesMapper(
        ModelElement_1_0 classDef,        
        Writer writer, 
        Model_1_0 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData, 
        AnnotationFlavour annotationFlavour, 
        JakartaFlavour jakartaFlavour, 
        JMIFlavour jmiFlavour
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
            new StandardPrimitiveTypeMapper()
        );
        this.classDef = new ClassDef(classDef, model);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return Version.getImplementationVersion();
    }

    /**
     * Map reference name
     * 
     * @param referenceDef
     * 
     * @throws ServiceException
     */
    public void mapReference(
        ReferenceDef referenceDef
    ) throws ServiceException {
        printLine("   /**");
        MapperUtils.wrapText(
            "    * ",
            "Reference feature {@code " + referenceDef.getName() + "}.",
            this::printLine
        );
        printLine("    */");
        print("    java.lang.String ");
        print(getConstantName(referenceDef.getName()));
        print(" = \"");
        print(referenceDef.getName());
        printLine("\";");
        newLine();
    }

    /**
     * Map operation name
     * 
     * @param operationDef
     * 
     * @throws ServiceException
     */
    public void mapOperation(
        OperationDef operationDef
    ) throws ServiceException {
        printLine("   /**");
        MapperUtils.wrapText(
            "    * ",
            "Behavioural feature {@code " + operationDef.getName() + "}.",
            this::printLine
        );
        printLine("    */");
        print("    java.lang.String ");
        print(getConstantName(operationDef.getName()));
        print(" = \"");
        print(operationDef.getName());
        printLine("\";");
        newLine();
    }

    /**
     * 
     *
     */
    public void mapEnd() {
//      this.trace("ClassFeatures/End");
        printLine("}");
    }

    /**
     * 
     * @throws ServiceException
     */
    public void mapBegin()
        throws ServiceException {
        printLine(
        	"package ",
            this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.classDef.getQualifiedName()))),
            ";"
        );
        newLine();
        printLine("/**");
        MapperUtils.wrapText(" * ", "Features of class " + this.classDef.getName(), this::printLine);
        printLine(" */");
        this.mapGeneratedAnnotation();
        print("public interface " + this.classDef.getName() + FEATURES_INTERFACE_SUFFIX);
        if (!this.classDef.getSupertypes().isEmpty()) {
            String separator = " extends "; 
            for (
                Iterator<?> i = this.classDef.getSupertypes().iterator(); 
                i.hasNext(); 
                separator = ", "
            ) {
                ClassDef supertype = (ClassDef) i.next();
                print(separator);
                print(this.getModelType(supertype.getQualifiedName()) + FEATURES_INTERFACE_SUFFIX);
            }
        }
        printLine(" {");
        newLine();
    }

    /**
     * Map operation name
     * 
     * @param attributeDef
     * @throws ServiceException
     */    
    public void mapAttribute(
        AttributeDef attributeDef
    ) throws ServiceException {
        printLine("   /**");
        MapperUtils.wrapText(
            "    * ",
            "Structural feature {@code " + attributeDef.getName() + "}.",
            this::printLine
        );
        printLine("    */");
        print("    java.lang.String ");
        print(getConstantName(attributeDef.getName()));
        print(" = \"");
        print(attributeDef.getName());
        printLine("\";");
        newLine();
    }

    /**
     * 
     */
    private final ClassDef classDef;
        
}

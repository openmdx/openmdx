/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: JMI Package Template 
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
import java.util.Iterator;
import java.util.List;

import org.omg.mof.spi.AbstractNames;
import org.omg.mof.spi.Identifier;
import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.externalizer.spi.JMIFlavour;
import org.openmdx.application.mof.externalizer.spi.JakartaFlavour;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.ClassifierDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.StructDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * JMI Package template
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class PackageMapper extends AbstractMapper {

    public PackageMapper(
        Writer writer,
        Model_1_0 model,
        Format format, 
        String packageSuffix, 
        MetaData_1_0 metaData, 
        AnnotationFlavour annotationFlavour, 
        JakartaFlavour jakartaFlavour, 
        JMIFlavour jmiFlavour, 
        PrimitiveTypeMapper primitiveTypeMapper
    ) {
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
    }

    //-----------------------------------------------------------------------
    public void mapQueryCreator(
        ClassifierDef classifierDef
    ) throws ServiceException {
        this.trace("Package/QueryCreator");
        String candidateType = this.interfaceType(
            classifierDef, 
            org.openmdx.application.mof.mapping.java.metadata.Visibility.CCI,
            false
        ); 
        printLine(
        	"  public ", 
        	candidateType,
        	"Query ",
        	getMethodName("create", MapperUtils.getElementName(classifierDef.getQualifiedName()), "Query"),
        	"();"
        );
        newLine();
    }
    
    //-----------------------------------------------------------------------
    public void mapEnd(
    ) {
        this.trace("Package/End.vm");
        printLine("}");        
    }
    
    //-----------------------------------------------------------------------
    public void mapClassAccessor(
        ClassDef classDef
    ) throws ServiceException {
        this.trace("Package/ClassAccessor");
        printLine(
        	"  public ",
        	this.getType(classDef.getQualifiedName(), getFormat(), false),
        	"Class ",
        	getMethodName("get" + MapperUtils.getElementName(classDef.getQualifiedName())),
        	"();"
        );
        newLine();        
    }
  
    //-----------------------------------------------------------------------
    public void mapBegin(
        String qualifiedPackageName
    ) throws ServiceException {
        this.trace("Package/Begin");
        List nameComponents = MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedPackageName));
        StringBuffer buffer = AbstractNames.openmdx2PackageName(
            new StringBuffer(),
            MapperUtils.getElementName(qualifiedPackageName)
        );
        String packageType = buffer.toString();
        buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
        String packageField = buffer.toString();
        buffer.setLength(buffer.length() - 7);
        String authorityField = buffer.append("Authority").toString();
        String authorityType = this.getType("org:openmdx:base:Authority", getFormat(), false);
        String xri = MapperUtils.getAuthorityId(nameComponents);
        printLine(" /**");
        MapperUtils.wrapText(
            "  * ",
            "The {@code AUTHORITY_XRI} <em>\"" + xri + 
            "\"</em> may be used to look up the Authority the package {@code " + 
            qualifiedPackageName + "} belongs to:", this::printLine
        );
        printLine("  * <p>");
        printLine("  * <pre>");
        printLine("  *   ", authorityType, " ", authorityField, " = (", authorityType, ")persistenceManager.getObjectById(");
        printLine("  *     ", authorityType, ".class,");
        printLine("  *     ", packageType, ".AUTHORITY_XRI");
        printLine("  *   );");
        printLine("  *   ", packageType, " ", packageField, " = (", packageType, ")", authorityField, ".getPackage();");
        printLine("  * </pre>");
        printLine("  * <p>");
        printLine("  *");
        printLine("  * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class,java.lang.Object)");
        printLine("  */");
        printLine("package ", this.getNamespace(nameComponents), ";");
        newLine();
        this.mapGeneratedAnnotation();
        printLine("public interface ", packageType, "  extends javax.jmi.reflect.RefPackage {");  
        newLine();
        printLine(" /**");
        MapperUtils.wrapText(
            "  * ",
            "The {@code AUTHORITY_XRI} <em>\"" + xri + 
            "\"</em> may be used to look up the Authority the package {@code " + 
            qualifiedPackageName + "} belongs to:", this::printLine
        );
        printLine("   * <p>");
        MapperUtils.wrapText(
            "   * ",
            "<em>Note: This is an extension to the JMI 1 standard.</em>", this::printLine
        );
        printLine("  */");
        printLine("  java.lang.String AUTHORITY_XRI = \"", xri, "\";");   
        newLine();
    }

    //-----------------------------------------------------------------------
    public void mapStructCreator(
        StructDef structDef
    ) throws ServiceException {
        this.trace("Package/StructCreator");
        String methodName = Identifier.OPERATION_NAME.toIdentifier(
            structDef.getName(), 
            null, // removablePrefix
            "create", // prependablePrefix
            null, // removableSuffix
            null // appendableSuffix
        );
        printLine("  public ", this.getType(structDef.getQualifiedName(), getFormat(), false), " ", methodName, "(");
        int ii = 0;
        for (Iterator<?> i = structDef.getFields().iterator(); i.hasNext(); ii++) {
            StructuralFeatureDef fieldDef = (StructuralFeatureDef) i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                fieldDef, ""
            );
        }
        printLine("  );");        
    }
        
}

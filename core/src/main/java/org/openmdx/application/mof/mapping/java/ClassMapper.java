/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ClassMapper 
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.java;

import java.io.Writer;
import java.util.List;

import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * ClassMapper
 */
@SuppressWarnings({"rawtypes"})
public class ClassMapper extends AbstractClassMapper {

    public ClassMapper(
        ModelElement_1_0 classDef,        
        Writer writer, 
        Model_1_0 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData, 
        boolean markdown, 
        PrimitiveTypeMapper primitiveTypeMapper
    ) throws ServiceException {
        super(
            classDef,
            writer, 
            model,
            format, 
            packageSuffix, 
            metaData, 
            markdown, 
            primitiveTypeMapper
        );
    }
    
    //-----------------------------------------------------------------------
    public void mapInstanceExtenderRequiredAttributes(
        ClassDef superclassDef,
        List requiredAttributes
    ) throws ServiceException {
        // Nothing to do
    }

    //-----------------------------------------------------------------------
    public void mapIntfInstanceExtenderAllAttributes(
        ClassDef superclassDef,
        List attributes
    ) throws ServiceException {
        // Nothing to do
    }

    //-----------------------------------------------------------------------
    public void mapInstanceCreatorRequiredAttributes(
        List requiredAttributes
    ) throws ServiceException {
        // Nothing to do
    }

    //-----------------------------------------------------------------------
    public void mapIntfInstanceCreatorAllAttributes(
        List attributes
    ) throws ServiceException {
        // Nothing to do
    }

    //-----------------------------------------------------------------------
    public void mapEnd() {
        this.trace("ClassProxy/End.vm");
        printLine("}");
    }

    //-----------------------------------------------------------------------
    public void mapBegin(
    ) {
        this.trace("ClassProxy/Begin");
        printLine(
        	"package ",
        	this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.classDef.getQualifiedName()))),
        	";"
        );
        newLine();
        this.mapGeneratedAnnotation();
        printLine("public interface ", this.className, "Class ");
        if(getFormat() == Format.JMI1) printLine("  extends javax.jmi.reflect.RefClass");
        printLine("{");
        newLine();
        printLine("  /**");
        MapperUtils.wrapText("   * ", "Creates an instance of class {@code " + this.className + "}.", this::printLine);
        MapperUtils.wrapText(
            "   * ",
            "This is a factory operation used to create instance objects of class {@code " + this.className + "}.", 
            this::printLine
        );
        printLine("   */");
        printLine("  public ", this.className, " create", this.className, "(");
        printLine("  );");
        newLine();
        printLine("  /**");
        MapperUtils.wrapText(
            "   * ",
            "Creates an instance of class {@code " + this.className + "} based on the specified Object instance.", 
            this::printLine
        );
        MapperUtils.wrapText(
            "   * ",
            "This is a factory operation used to create instance objects of class {@code " + this.className + "}.", 
            this::printLine
        );
        MapperUtils.wrapText(
            "   * ",
            "@param object The Object instance this class is based on.", 
            this::printLine
        );
        printLine("   */");
        printLine("  public ", this.className, " get", this.className, "(");
        printLine("    Object object");
        printLine("  );");
    }

}

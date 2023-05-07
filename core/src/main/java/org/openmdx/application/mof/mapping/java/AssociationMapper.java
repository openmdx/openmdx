/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: AssociationMapper 
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

import org.omg.mof.spi.Identifier;
import org.openmdx.application.mof.mapping.cci.AssociationDef;
import org.openmdx.application.mof.mapping.cci.AssociationEndDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;

/**
 * AssociationMapper
 */
public class AssociationMapper
    extends AbstractMapper
{

    public AssociationMapper(
        ModelElement_1_0 element,
        Writer writer,
        Model_1_0 model,
        Format format,
        String packageSuffix,
        MetaData_1_0 metaData, 
        boolean markdown, 
        PrimitiveTypeMapper primitiveTypeMapper
    ) throws ServiceException {
        super(writer, model, format, packageSuffix, metaData, markdown, primitiveTypeMapper);
        this.associationName = Identifier.CLASS_PROXY_NAME.toIdentifier(
            element.getName(),
            null, // removablePrefix
            null, // prependablePrefix
            null, // removableSuffix
            null //appendableSuffix
        );
        this.associationDef = new AssociationDef(
            element,
            model
        );
    }

    final String associationName;
    
    final AssociationDef associationDef;

    static final String QUALIFIED_CONTAINER_CLASS_NAME = "org.w3c.cci2.Container";
    static final String QUALIFIED_COLLECTION_CLASS_NAME = "java.util.Collection";
    
    /**
     * Begin
     * 
     * @throws ServiceException
     */
    protected void mapBegin(
    ) throws ServiceException {
        this.trace("Association/Begin");
        printLine(
            "package ",
            this.getNamespace(
                MapperUtils.getNameComponents(
                    MapperUtils.getPackageName(
                        this.associationDef.getQualifiedName()
                    )
                )
            ),
            ";"
        );
        newLine();
        printLine("/**");
        printLine(" * Association Interface {@code ", this.associationDef.getName(), "}"); 
        mapAnnotation(" * ", this.associationDef);
        printLine(" */");
        this.mapGeneratedAnnotation();
        printLine("public interface ", this.associationName, " {"); 
    }

    /**
     * End
     * 
     * @throws ServiceException
     */
    protected void mapEnd(
    ) throws ServiceException {
        printLine("}"); 
        this.trace("Association/End");
    }
    
    /**
     * End
     * 
     * @throws ServiceException
     */
    protected void mapAssociationEnd(
        AssociationEndDef associationEnd
    ) throws ServiceException {
        this.trace("AssociationEnd/Begin");
        String name = Identifier.CLASS_PROXY_NAME.toIdentifier(associationEnd.getName());            
        String qualifierValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(associationEnd.getQualifierName());
        String qualifierTypeName = qualifierValueName + InstanceMapper.QUALIFIER_TYPE_SUFFIX;
        String qualifierValueType = getType(associationEnd.getQualifierType(), getFormat(), false);
        String objectValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(associationEnd.getName());
        if(objectValueName.equals(qualifierValueName)) {
            objectValueName = '_' + objectValueName;
        }
        newLine();
        printLine("  /**");
        printLine("   * Association End Interface {@code ", associationEnd.getName(), "}"); 
        mapAnnotation(" * ", associationEnd);
        printLine("   */");
        printLine("  interface ", name, "<E> extends ", QUALIFIED_CONTAINER_CLASS_NAME, "<E> {"); 
        newLine();            
        printLine("     E get(");
        printLine("       ", InstanceMapper.QUALIFIER_TYPE_CLASS_NAME, " ", qualifierTypeName, ",");
        printLine("       ", qualifierValueType, " ", qualifierValueName);
        printLine("     );");
        newLine();
        ReferenceDef referenceDef = associationEnd.getReference(); 
        if(referenceDef != null && referenceDef.isChangeable()) {
            printLine("     void add(");
            printLine("       ", InstanceMapper.QUALIFIER_TYPE_CLASS_NAME, " ", qualifierTypeName, ",");
            printLine("       ", qualifierValueType, " ", qualifierValueName, ",");
            printLine("       E ", objectValueName);
            printLine("     );");
            newLine();            
            printLine("     void remove(");
            printLine("       ", InstanceMapper.QUALIFIER_TYPE_CLASS_NAME, " ", qualifierTypeName, ",");
            printLine("       ", qualifierValueType, " ", qualifierValueName);
            printLine("     );");
            newLine();            
        }
        printLine("  }"); 
        newLine();                        
        this.trace("AssociationEnd/End");
    }
    
    /**
     * Map Association
     * 
     * @throws ServiceException 
     */
    public boolean mapAssociation(
    ) throws ServiceException {
        for(AssociationEndDef associationEnd : this.associationDef.getEnds()) {
            if(!AggregationKind.NONE.equals(associationEnd.getAggregation())) {
                mapBegin();
                mapAssociationEnd(associationEnd);
                mapEnd();
                return true;
            }
        }
        return false;
    }
    
}

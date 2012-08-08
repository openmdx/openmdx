/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIPackageIntfMapper.java,v 1.10 2008/04/02 17:39:09 wfro Exp $
 * Description: JMIPackageTemplate 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/02 17:39:09 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.ClassifierDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.StructDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

public class JMIPackageIntfMapper
    extends JMIAbstractMapper {

    //-----------------------------------------------------------------------
    public JMIPackageIntfMapper(
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
        return "$Id: JMIPackageIntfMapper.java,v 1.10 2008/04/02 17:39:09 wfro Exp $";
    }

    //-----------------------------------------------------------------------
    public void mapIntfFilterCreator(
        ClassifierDef classifierDef
    ) throws ServiceException {
        this.trace("Package/IntfFilterCreator");
        this.pw.println("  public " + this.getType(classifierDef.getQualifiedName()) + "Filter create" + MapperUtils.getElementName(classifierDef.getQualifiedName()) + "Filter(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  public " + this.getType(classifierDef.getQualifiedName()) + "Filter create" + MapperUtils.getElementName(classifierDef.getQualifiedName()) + "Filter(");
        this.pw.println("    org.openmdx.compatibility.base.query.FilterProperty[] filterProperties,");
        this.pw.println("    org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier[] attributeSpecifiers");
        this.pw.println("  );");
        this.pw.println("");        
    }
    
    //-----------------------------------------------------------------------
    public void mapIntfEnd(
    ) {
        this.trace("Package/IntfEnd.vm");
        this.pw.println("}");        
    }
    
    //-----------------------------------------------------------------------
    public void mapIntfClassAccessor(
        ClassDef classDef
    ) throws ServiceException {
        this.trace("Package/IntfClassAccessor");
        this.pw.println(
            "  public " + this.getType(classDef.getQualifiedName()) + "Class " + getMethodName(
                "get" + MapperUtils.getElementName(classDef.getQualifiedName()) + "Class"
            ) + "();"
        );
        this.pw.println("");        
    }
  
    //-----------------------------------------------------------------------
    public void mapIntfBegin(
        String qualifiedPackageName
    ) throws ServiceException {
        this.trace("Package/IntfBegin");
        this.fileHeader();
        List<String> nameComponents = MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedPackageName));
        String packageType = Names.openmdx1PackageName(
            new StringBuffer(),
            MapperUtils.getElementName(qualifiedPackageName)
        ).toString();
        this.pw.println("package " + this.getNamespace(nameComponents) + ";");
        this.pw.println();
        this.pw.println("@SuppressWarnings(\"unchecked\")");
        this.pw.print("public interface " + packageType + " extends org.openmdx.base.accessor.jmi.cci.RefPackage_1_1 {");
        this.pw.println();
    }
    
    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapIntfStructCreator(
        StructDef structDef
    ) throws ServiceException {
        this.trace("Package/IntfStructCreator");
        this.pw
            .println("  public " + this.getType(structDef.getQualifiedName()) + " create" + MapperUtils.getElementName(structDef.getQualifiedName()) + "(");
        int ii = 0;
        for (Iterator<StructuralFeatureDef> i = structDef.getFields().iterator(); i.hasNext(); ii++) {
            StructuralFeatureDef fieldDef = i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                fieldDef
            );
        }
        this.pw.println("  );");        
    }
        
}

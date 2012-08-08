/*
 * ==================================================================== 
 * Project: openmdx, http://www.openmdx.org
 * Name: $Id: JMIClassImplMapper.java,v 1.5 2008/04/02 17:39:09 wfro Exp $ 
 * Description: JMIClassLevelTemplate Revision: $Revision: 1.5 $ 
 * Owner: OMEX AG, Switzerland, http://www.omex.ch 
 * Date: $Date: 2008/04/02 17:39:09 $
 * ====================================================================
 * 
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of the openMDX team nor the names
 * of its contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
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
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.MapperUtils;

public class JMIClassImplMapper
    extends JMIAbstractMapper 
{

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return "$Id: JMIClassImplMapper.java,v 1.5 2008/04/02 17:39:09 wfro Exp $";
    }

    //-----------------------------------------------------------------------
    public JMIClassImplMapper(
        ModelElement_1_0 classDef,        
        Writer writer, 
        Model_1_3 model,
        String format, 
        String packageSuffix
    ) throws ServiceException {
        super(
            writer, 
            model,
            format, packageSuffix
        );
        this.classDef = new ClassDef(classDef, model);
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapImplInstanceExtenderRequiredAttributes(
        ClassDef superclassDef,
        List requiredAttributes
    ) throws ServiceException {
        this.trace("ClassProxy/ImplInstanceExtenderRequiredAttributes");
        this.pw.println("  public " + this.classDef.getName() + " extend" + superclassDef.getName() + "(");
        this.pw.println("      " + this.getType(superclassDef.getQualifiedName()) + " _base");
        for (Iterator<AttributeDef> i = requiredAttributes.iterator(); i.hasNext();) {
            AttributeDef attribute = i.next();
            this.mapParameter(
                "    , ",
                attribute
            );
        }
        this.pw.println("  ) {");
        this.pw.println("    " + this.classDef.getName() + "Impl _object = (" + this.classDef.getName() + "Impl)get" + this.classDef.getName() + "(_base);");
        for (Iterator<AttributeDef> i = requiredAttributes.iterator(); i.hasNext();) {
            AttributeDef attribute = i.next();
            this.pw.println("    _object." + this.getMethodName(attribute.getBeanSetterName()) + "(" + this.getParamName(attribute.getName()) + ");");
        }
        this.pw.println("    return _object;");
        this.pw.println("  }");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapImplInstanceExtenderAllAttributes(
        ClassDef superclassDef,
        List attributes
    ) throws ServiceException {
        this.trace("ClassProxy/ImplInstanceExtenderAllAttributes");
        this.pw.println("  public " + this.classDef.getName() + " extend" + superclassDef.getName() + "(");
        this.pw.println("      " + this.getType(superclassDef.getQualifiedName()) + " _base");
        for (Iterator<AttributeDef> i = attributes.iterator(); i.hasNext();) {
            AttributeDef attribute = i.next();
            this.mapParameter(
                "    , ",
                attribute
            );
        }
        this.pw.println("  ) {");
        this.pw.println("    " + this.classDef.getName() + "Impl _object = (" + this.classDef.getName() + "Impl)get" + this.classDef.getName() + "(_base);");
        for (Iterator<AttributeDef> i = attributes.iterator(); i.hasNext();) {
            AttributeDef attribute = i.next();
            this.pw.println("    _object." + this.getMethodName(attribute.getBeanSetterName()) + "(" + this.getParamName(attribute.getName()) + ");");
        }
        this.pw.println("    return _object;");
        this.pw.println("  }");
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapImplInstanceCreatorRequiredAttributes(
        List requiredAttributes
    ) throws ServiceException {
        this.trace("ClassProxy/ImplInstanceCreatorRequiredAttributes");
        this.pw.println("  public " + this.classDef.getName() + " create" + this.classDef.getName() + "(");
        int ii = 0;
        for (Iterator<AttributeDef> i = requiredAttributes.iterator(); i.hasNext(); ii++) {
            AttributeDef attribute = i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                attribute
            );
        }
        this.pw.println("  ) {");
        this.pw.println("    " + this.classDef.getName() + "Impl _object = (" + this.classDef.getName() + "Impl)create" + this.classDef.getName() + "();");
        for (Iterator<AttributeDef> i = requiredAttributes.iterator(); i.hasNext();) {
            AttributeDef attribute = i.next();
            this.pw.println("    _object." + this.getMethodName(attribute.getBeanSetterName()) + "(" + this.getParamName(attribute.getName()) + ");");
        }
        this.pw.println("    return _object;");
        this.pw.println("  }");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapImplInstanceCreatorAllAttributes(
        List attributes
    ) throws ServiceException {
        this.trace("ClassProxy/ImplInstanceCreatorAllAttributes");
        this.pw.println("  public " + this.classDef.getName() + " create" + this.classDef.getName() + "(");
        int ii = 0;
        for (Iterator<AttributeDef> i = attributes.iterator(); i.hasNext(); ii++) {
            AttributeDef attribute = i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                attribute
            );
        }
        this.pw.println("  ) {");
        this.pw.println("    " + this.classDef.getName() + "Impl _object = (" + this.classDef.getName() + "Impl)create" + this.classDef.getName() + "();");
        for (Iterator<AttributeDef> i = attributes.iterator(); i.hasNext();) {
            AttributeDef attribute = i.next();
            this.pw.println("    _object." + this.getMethodName(attribute.getBeanSetterName()) + "(" + this.getParamName(attribute.getName()) + ");");
        }
        this.pw.println("    return _object;");
        this.pw.println("  }");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapImplEnd() {
        this.trace("ClassProxy/ImplEnd.vm");
        this.pw.println("}");
    }

    //-----------------------------------------------------------------------
    public void mapImplBegin() {
        this.trace("ClassProxy/ImplBegin");
        
        this.fileHeader();
        this.pw.println("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.classDef.getQualifiedName()))) + ";");
        this.pw.println("");
        this.pw.println("@SuppressWarnings({\"unchecked\",\"serial\"})");
        this.pw.println("public class " + this.classDef.getName() + "ClassImpl");
        this.pw.println("  extends org.openmdx.base.accessor.jmi.spi.RefClass_1");
        this.pw.println("  implements " + this.classDef.getName() + "Class {");
        this.pw.println("");
        this.pw.println("  public " + this.classDef.getName() + "ClassImpl(");
        this.pw.println("    org.openmdx.base.accessor.jmi.cci.RefPackage_1_0 refPackage");
        this.pw.println("  ) {");
        this.pw.println("    super(refPackage);");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public String refMofId(");
        this.pw.println("  ) {");
        this.pw.println("    return \"" + this.classDef.getQualifiedName() + "\";");
        this.pw.println("  }");
        this.pw.println("");
        this.pw.println("  public " + this.classDef.getName() + " get" + this.classDef.getName() + "(");
        this.pw.println("    Object object");
        this.pw.println("  ) {");        
        this.pw.println("    try {");
        this.pw.println("      if(object instanceof org.openmdx.base.accessor.jmi.cci.RefObject_1_0) {");
        this.pw.println("        java.util.List args = new java.util.ArrayList();");
        this.pw.println("        args.add(object);");
        this.pw.println("        " + this.classDef.getName() + " target = (" + this.classDef.getName() + ")refCreateInstance(");
        this.pw.println("          args");
        this.pw.println("        );");
        this.pw.println("        return target;");
        this.pw.println("      } else {");
        this.pw.println("        return (" + this.classDef.getName() + ")((org.openmdx.base.accessor.jmi.cci.RefPackage_1_0)this.refOutermostPackage()).refObject(");
        this.pw.println("          object instanceof org.openmdx.compatibility.base.naming.Path");
        this.pw.println("            ? ((org.openmdx.compatibility.base.naming.Path)object).toXri()");
        this.pw.println("            : ((org.openmdx.base.accessor.generic.cci.Object_1_0)object).objGetPath().toXri()");
        this.pw.println("        );");
        this.pw.println("      }");
        this.pw.println("    }");
        this.pw.println("    catch(org.openmdx.base.exception.ServiceException e) {");
        this.pw.println("        throw new org.openmdx.base.accessor.jmi.cci.JmiServiceException(e);");
        this.pw.println("    }");
        this.pw.println("  }");
        this.pw.println();
        this.pw.println("  public " + this.classDef.getName() + " create" + this.classDef.getName() + "(");
        this.pw.println("  ) {");
        this.pw.println("    " + this.classDef.getName() + " target = (" + this.classDef.getName() + ")refCreateInstance(");
        this.pw.println("      null");
        this.pw.println("    );");
        this.pw.println("    return target;");
        this.pw.println("  }");
    }

    //-----------------------------------------------------------------------
    protected final ClassDef classDef; 
    
}

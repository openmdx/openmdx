/*
 * ==================================================================== 
 * Project: openmdx, http://www.openmdx.org
 * Name: $Id: JMIClassIntfMapper.java,v 1.2 2007/01/06 16:29:27 hburger Exp $ 
 * Description: JMIClassLevelTemplate Revision: $Revision: 1.2 $ 
 * Owner: OMEX AG, Switzerland, http://www.omex.ch 
 * Date: $Date: 2007/01/06 16:29:27 $
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
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.MapperUtils;

public class JMIClassIntfMapper
    extends JMIAbstractMapper {

    //-----------------------------------------------------------------------
    public JMIClassIntfMapper(
        ModelElement_1_0 classDef,        
        Writer writer, 
        Model_1_0 model,
        String format, 
        String packageSuffix
    ) throws ServiceException {
        super(
            writer, 
            model,
            format, 
            packageSuffix
        );
        this.classDef = new ClassDef(classDef, model);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.model1.mapping.java.JMIAbstractMapper#getId()
     */
    protected String mapperId() {
        return "$Id: JMIClassIntfMapper.java,v 1.2 2007/01/06 16:29:27 hburger Exp $";
    }

    //-----------------------------------------------------------------------
    public void mapIntfInstanceExtenderRequiredAttributes(
        ClassDef superclassDef,
        List requiredAttributes
    ) throws ServiceException {
        this.trace("ClassProxy/IntfInstanceExtenderRequiredAttributes");
        this.pw.println("  /**");
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "Creates an instance of class <code>" + this.classDef.getName() + "</code> based on the superclass <code>" + superclassDef.getName() + "</code> and all required attributes not included in this superclass."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "This is a factory operation used to create instance objects of class <code>" + this.classDef.getName() + "</code>."
            )
        );
        for (Iterator i = requiredAttributes.iterator(); i.hasNext();) {
            AttributeDef attribute = (AttributeDef)i.next();
            if (attribute.getAnnotation() != null) {
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "@param " + this.getParamName(attribute.getName()) + " attribute.getAnnotation()"
                    )
                );
            }
        }
        this.pw.println("   */");
        this.pw.println("  public " + this.classDef.getName() + " extend" + superclassDef.getName() + "(");
        this.pw.println("      " + this.getType(superclassDef.getQualifiedName()) + " _base");
        for(Iterator i = requiredAttributes.iterator(); i.hasNext();) {
            AttributeDef attribute = (AttributeDef)i.next();
            this.mapParameter(
                "    , ",
                attribute                
            );
        }
        this.pw.println("  );");
    }

    //-----------------------------------------------------------------------
    public void mapIntfInstanceExtenderAllAttributes(
        ClassDef superclassDef,
        List attributes
    ) throws ServiceException {
        this.trace("ClassProxy/IntfInstanceExtenderAllAttributes");
        this.pw.println("  /**");
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "Creates an instance of class <code>" + this.classDef.getName() + "</code> based on the superclass <code>" + superclassDef.getName() + "</code> and all attributes not included in this superclass."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "This is a factory operation used to create instance objects of class <code>" + this.classDef.getName() + "</code>."
            )
       );
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            AttributeDef attribute = (AttributeDef)i.next();
            if (attribute.getAnnotation() != null) {
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "@param " + this.getParamName(attribute.getName()) + " attribute.getAnnotation()"
                    )
                );
            }
        }
        this.pw.println("   */");
        this.pw.println("  public " + this.classDef.getName() + " extend" + superclassDef.getName() + "(");
        this.pw.println("      " + this.getType(superclassDef.getQualifiedName()) + " _base");
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            AttributeDef attribute = (AttributeDef)i.next();
            this.mapParameter(
                "    , ",
                attribute
            );
        }
        this.pw.println("  );");
    }

    //-----------------------------------------------------------------------
    public void mapIntfInstanceCreatorRequiredAttributes(
        List requiredAttributes
    ) throws ServiceException {
        this.trace("ClassProxy/IntfInstanceCreatorRequiredAttributes");
        this.pw.println("  /**");
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "Creates an instance of class <code>" + this.classDef.getName() + "</code> based on all required attributes."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "This is a factory operation used to create instance objects of class <code>" + this.classDef.getName() + "</code>."
            )
        );
        for (Iterator i = requiredAttributes.iterator(); i.hasNext();) {
            AttributeDef attribute = (AttributeDef)i.next();
            if (attribute.getAnnotation() != null) {
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "@param " + this.getParamName(attribute.getName()) + " attribute.getAnnotation()"
                    )
                );
            }
        }
        this.pw.println("   */");
        this.pw.println("  public " + this.classDef.getName() + " create" + this.classDef.getName() + "(");
        int ii = 0;
        for (Iterator i = requiredAttributes.iterator(); i.hasNext(); ii++) {
            AttributeDef attribute = (AttributeDef)i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                attribute
            );
        }
        this.pw.println("  );");
    }

    //-----------------------------------------------------------------------
    public void mapIntfInstanceCreatorAllAttributes(
        List attributes
    ) throws ServiceException {
        this.trace("ClassProxy/IntfInstanceCreatorAllAttributes");
        this.pw.println("  /**");
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "Creates an instance of class <code>" + this.classDef.getName() + "</code> based on all attributes."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "This is a factory operation used to create instance objects of class <code>" + this.classDef.getName() + "</code>."
            )
        );
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            AttributeDef attribute = (AttributeDef)i.next();
            if (attribute.getAnnotation() != null) {
                this.pw.println(
                    MapperUtils.wrapText(
                        "   * ",
                        "@param " + this.getParamName(attribute.getName()) + " attribute.getAnnotation()"
                    )
                );
            }
        }
        this.pw.println("   */");
        this.pw.println("  public " + this.classDef.getName() + " create" + this.classDef.getName() + "(");
        int ii = 0;
        for (Iterator i = attributes.iterator(); i.hasNext(); ii++) {
            AttributeDef attribute = (AttributeDef)i.next();
            String separator = ii == 0
                ? "      "
                : "    , ";
            this.mapParameter(
                separator,
                attribute
            );
        }
        this.pw.println("  );");
        this.pw.println("");
    }

    //-----------------------------------------------------------------------
    public void mapIntfEnd() {
        this.trace("ClassProxy/IntfEnd.vm");
        this.pw.println("}");
    }

    //-----------------------------------------------------------------------
    public void mapIntfBegin(
    ) {
        this.trace("ClassProxy/IntfBegin");
        this.fileHeader();
        this.pw.println("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.classDef.getQualifiedName()))) + ";");
        this.pw.println("");
        this.pw.println("public interface " + this.classDef.getName() + "Class");
        this.pw.println("  extends org.openmdx.base.accessor.jmi.cci.RefClass_1_0 {");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "Creates an instance of class <code>" + this.classDef.getName() + "</code>."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "This is a factory operation used to create instance objects of class <code>" + this.classDef.getName() + "</code>."
            )
        );
        this.pw.println("   */");
        this.pw.println("  public " + this.classDef.getName() + " create" + this.classDef.getName() + "(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "Creates an instance of class <code>" + this.classDef.getName() + "</code> based on the specified Object instance."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "This is a factory operation used to create instance objects of class <code>" + this.classDef.getName() + "</code>."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "@param object The Object instance this class is based on. Object must be instanceof RefObject, Object_1_0 or Path."
            )
        );
        this.pw.println("   */");
        this.pw.println("  public " + this.classDef.getName() + " get" + this.classDef.getName() + "(");
        this.pw.println("    Object object");
        this.pw.println("  );");
    }

    //-----------------------------------------------------------------------
    protected final ClassDef classDef;    
}

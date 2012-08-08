/*
 * ==================================================================== 
 * Project: openmdx, http://www.openmdx.org
 * Name: $Id: ClassMapper.java,v 1.11 2007/12/14 11:52:05 wfro Exp $ 
 * Description: JMIClassLevelTemplate Revision: $Revision: 1.11 $ 
 * Owner: OMEX AG, Switzerland, http://www.omex.ch 
 * Date: $Date: 2007/12/14 11:52:05 $
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

package org.openmdx.model1.mapping.java;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MetaData_1_0;

/**
 * ClassMapper
 */
public class ClassMapper extends AbstractClassMapper {

    //-----------------------------------------------------------------------
    public ClassMapper(
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
    
    //-----------------------------------------------------------------------
    public void mapInstanceExtenderRequiredAttributes(
        ClassDef superclassDef,
        List requiredAttributes
    ) throws ServiceException {
        if(false) {
            this.trace("ClassProxy/InstanceExtenderRequiredAttributes");
            String superclassName = Identifier.CLASS_PROXY_NAME.toIdentifier(superclassDef.getName());
            this.pw.println("  /**");
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "Creates an instance of class <code>" + this.className + "</code> based on the superclass <code>" + superclassName + "</code> and all required attributes not included in this superclass."
                )
            );
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "This is a factory operation used to create instance objects of class <code>" + this.className + "</code>."
                )
            );
            for (Iterator i = requiredAttributes.iterator(); i.hasNext();) {
                AttributeDef attribute = (AttributeDef)i.next();
                if (attribute.getAnnotation() != null) {
                    this.pw.println(
                        MapperUtils.wrapText(
                            "   * ",
                            "@param " + this.getFeatureName(attribute) + " attribute.getAnnotation()"
                        )
                    );
                }
            }
            this.pw.println("   */");
            this.pw.println("  public " + this.className + " extend" + superclassName + "(");
            this.pw.println("      " + this.getType(superclassDef.getQualifiedName()) + " _base");
            for(Iterator i = requiredAttributes.iterator(); i.hasNext();) {
                AttributeDef attribute = (AttributeDef)i.next();
                this.mapParameter(
                    "    , ",
                    attribute, ""                
                );
            }
            this.pw.println("  );");
        }
    }

    //-----------------------------------------------------------------------
    public void mapIntfInstanceExtenderAllAttributes(
        ClassDef superclassDef,
        List attributes
    ) throws ServiceException {
        if(false) {
            this.trace("ClassProxy/InstanceExtenderAllAttributes");
            String superclassName = Identifier.CLASS_PROXY_NAME.toIdentifier(superclassDef.getName());
            this.pw.println("  /**");
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "Creates an instance of class <code>" + this.className + "</code> based on the superclass <code>" + superclassName + "</code> and all attributes not included in this superclass."
                )
            );
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "This is a factory operation used to create instance objects of class <code>" + this.className + "</code>."
                )
           );
            for (Iterator i = attributes.iterator(); i.hasNext();) {
                AttributeDef attribute = (AttributeDef)i.next();
                if (attribute.getAnnotation() != null) {
                    this.pw.println(
                        MapperUtils.wrapText(
                            "   * ",
                            "@param " + this.getFeatureName(attribute) + " attribute.getAnnotation()"
                        )
                    );
                }
            }
            this.pw.println("   */");
            this.pw.println("  public " + this.className + " extend" + superclassName + "(");
            this.pw.println("      " + this.getType(superclassDef.getQualifiedName()) + " _base");
            for (Iterator i = attributes.iterator(); i.hasNext();) {
                AttributeDef attribute = (AttributeDef)i.next();
                this.mapParameter(
                    "    , ",
                    attribute, ""
                );
            }
            this.pw.println("  );");
        }
    }

    //-----------------------------------------------------------------------
    public void mapInstanceCreatorRequiredAttributes(
        List requiredAttributes
    ) throws ServiceException {
        if(false) {
            this.trace("ClassProxy/InstanceCreatorRequiredAttributes");
            this.pw.println("  /**");
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "Creates an instance of class <code>" + this.className + "</code> based on all required attributes."
                )
            );
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "This is a factory operation used to create instance objects of class <code>" + this.className + "</code>."
                )
            );
            for (Iterator i = requiredAttributes.iterator(); i.hasNext();) {
                AttributeDef attribute = (AttributeDef)i.next();
                if (attribute.getAnnotation() != null) {
                    this.pw.println(
                        MapperUtils.wrapText(
                            "   * ",
                            "@param " + this.getFeatureName(attribute) + " attribute.getAnnotation()"
                        )
                    );
                }
            }
            this.pw.println("   */");
            this.pw.println("  public " + this.className + " create" + this.className + "(");
            int ii = 0;
            for (Iterator i = requiredAttributes.iterator(); i.hasNext(); ii++) {
                AttributeDef attribute = (AttributeDef)i.next();
                String separator = ii == 0
                    ? "      "
                    : "    , ";
                this.mapParameter(
                    separator,
                    attribute, ""
                );
            }
            this.pw.println("  );");
        }
    }

    //-----------------------------------------------------------------------
    public void mapIntfInstanceCreatorAllAttributes(
        List attributes
    ) throws ServiceException {
        if(false) {
            this.trace("ClassProxy/InstanceCreatorAllAttributes");
            this.pw.println("  /**");
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "Creates an instance of class <code>" + this.className + "</code> based on all attributes."
                )
            );
            this.pw.println(
                MapperUtils.wrapText(
                    "   * ",
                    "This is a factory operation used to create instance objects of class <code>" + this.className + "</code>."
                )
            );
            for (Iterator i = attributes.iterator(); i.hasNext();) {
                AttributeDef attribute = (AttributeDef)i.next();
                if (attribute.getAnnotation() != null) {
                    this.pw.println(
                        MapperUtils.wrapText(
                            "   * ",
                            "@param " + this.getFeatureName(attribute) + " attribute.getAnnotation()"
                        )
                    );
                }
            }
            this.pw.println("   */");
            this.pw.println("  public " + this.className + " create" + this.className + "(");
            int ii = 0;
            for (Iterator i = attributes.iterator(); i.hasNext(); ii++) {
                AttributeDef attribute = (AttributeDef)i.next();
                String separator = ii == 0
                    ? "      "
                    : "    , ";
                this.mapParameter(
                    separator,
                    attribute, ""
                );
            }
            this.pw.println("  );");
            this.pw.println();
        }
    }

    //-----------------------------------------------------------------------
    public void mapEnd() {
        this.trace("ClassProxy/End.vm");
        this.pw.println("}");
    }

    //-----------------------------------------------------------------------
    public void mapBegin(
    ) {
        this.trace("ClassProxy/Begin");
        this.fileHeader();
        this.pw.println("package " + this.getNamespace(MapperUtils.getNameComponents(MapperUtils.getPackageName(this.classDef.getQualifiedName()))) + ";");
        this.pw.println("");
        this.pw.println("public interface " + this.className + "Class ");
        if(getFormat() == Format.JMI1) this.pw.println("  extends javax.jmi.reflect.RefClass");
        this.pw.println("{");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "Creates an instance of class <code>" + this.className + "</code>."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "This is a factory operation used to create instance objects of class <code>" + this.className + "</code>."
            )
        );
        this.pw.println("   */");
        this.pw.println("  public " + this.className + " create" + this.className + "(");
        this.pw.println("  );");
        this.pw.println("");
        this.pw.println("  /**");
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "Creates an instance of class <code>" + this.className + "</code> based on the specified Object instance."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "This is a factory operation used to create instance objects of class <code>" + this.className + "</code>."
            )
        );
        this.pw.println(
            MapperUtils.wrapText(
                "   * ",
                "@param object The Object instance this class is based on."
            )
        );
        this.pw.println("   */");
        this.pw.println("  public " + this.className + " get" + this.className + "(");
        this.pw.println("    Object object");
        this.pw.println("  );");
    }

}
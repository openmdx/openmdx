/*
 * ====================================================================
 * Name:        $Id: InstanceMapper.java,v 1.123 2008/11/18 18:18:50 hburger Exp $
 * Description: Instance Mapper 
 * Revision:    $Revision: 1.123 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/18 18:18:50 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
package org.openmdx.model1.mapping.java;

import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.importer.metadata.ClassMetaData;
import org.openmdx.model1.importer.metadata.FieldMetaData;
import org.openmdx.model1.importer.metadata.Visibility;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.ExceptionDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MetaData_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.OperationDef;
import org.openmdx.model1.mapping.ReferenceDef;
import org.openmdx.model1.mapping.StructuralFeatureDef;

/**
 * InstanceMapper
 */
public class InstanceMapper
extends AbstractClassMapper {

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public InstanceMapper(
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
        this.localFeatures = new HashMap<String,ModelElement_1_0>(
                (Map<String,ModelElement_1_0>)classDef.values("allFeature").get(0)
        );
        if(isBaseClass()) {            
            this.superFeatures = Collections.emptyMap();
        } else {
            this.superFeatures = (Map<String,ModelElement_1_0>)model.getElement(
                this.extendsClassDef.getQualifiedName()
            ).values("allFeature").get(0); 
            this.localFeatures.keySet().removeAll(superFeatures.keySet());                
        }
    }

    // -----------------------------------------------------------------------
    public void mapReferenceAddWithoutQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        if(this.getFormat() == Format.JMI1) return;
        this.trace("Instance/ReferenceAddWithoutQualifier");
        ClassDef classDef = getClassDef(referenceDef.getQualifiedTypeName());
        String referenceType = getClassType(classDef).getType(classDef, this.getFormat(), TypeMode.MEMBER);
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Appends the specified element to the list of all the values for the reference <code>" + referenceDef.getName() + "</code>."));
        this.pw.println("   * <p>");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "<em>Note: This is an extension to the JMI 1 standard.<br>" +
            "In order to remain standard compliant you should substitute this method with <code>get" + 
            referenceDef.getBeanGenericName() + 
            "().add(" + 
            referenceType + 
            ")</code></em>."
        ));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue The element to be appended.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName("add" + referenceDef.getBeanGenericName()) + " (");
        this.pw.println("    " + referenceType + " newValue");
        this.pw.println("  );");
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapReferenceAddWithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        if(
                referenceDef.isChangeable() && 
                (referenceDef.isComposition() || referenceDef.isShared())
        ){
            Format format = getFormat();
            if(format == Format.JMI1) { // || format == Format.JDO2
                this.trace("Instance/ReferenceAddWithQualifier");
                ClassDef classDef = getClassDef(referenceDef.getQualifiedTypeName());
                ClassType classType = getClassType(classDef);
                String referenceType = classType.getType(classDef, format, TypeMode.PARAMETER);
                String valueHolder = referenceDef.getName();
                if(valueHolder.equals(referenceDef.getQualifierName())) valueHolder = '_' + valueHolder;
                this.pw.println("  /**");
                this.pw.println(MapperUtils.wrapText(
                    "   * ",
                    "Adds the specified element to the set of the values for the reference " +
                    "<code>" + referenceDef.getName() + "</code>."
                ));
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText(
                    "   * ",
                    "<em>Note: This is an extension to the JMI 1 standard.</em>"
                ));
                if (referenceDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                }
                this.pw.println("   * @param " + referenceDef.getQualifierName() + PERSISTENCY_SUFFIX + " <code>true</code> if <code>" + referenceDef.getQualifierName() + "</code> is persistent");
                this.pw.println("   * @param " + referenceDef.getQualifierName() + " The qualifier attribute value that qualifies the reference to get the element to be appended.");
                this.pw.println("   * @param " + valueHolder + " The element to be appended.");
                this.pw.println("   */");
                this.pw.println("  public void " + this.getMethodName("add" + referenceDef.getBeanGenericName()) + " (");
                this.pw.println("    boolean " + referenceDef.getQualifierName() + PERSISTENCY_SUFFIX + ",");            
                this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + ",");
                this.pw.println("    " + referenceType + " " + valueHolder);        
//              if(format == Format.JDO2) {
//                  String beanSetterName = Identifier.OPERATION_NAME.toIdentifier(
//                      getFeatureName(referenceDef.getExposedEndName()),
//                      null, // removablePrefix
//                      "addTo", // prependablePrefix
//                      null, // removableSuffix
//                      null // appendableSuffix
//                  );
//                  this.pw.println("  ){");
//                  this.pw.println("    ((" + accessorType + ")" + valueHolder + ")." + beanSetterName + "(");
//                  this.pw.println("       this,");
//                  this.pw.println("       " + referenceDef.getQualifierName() + PERSISTENCY_SUFFIX + ",");
//                  this.pw.println("       " + referenceDef.getQualifierName());
//                  this.pw.println("    );");
//                  this.pw.println("  }");
//              } else {
                    this.pw.println("  );");
                    this.pw.println();
                    this.pw.println("  /**");
                    this.pw.println(MapperUtils.wrapText(
                        "   * ",
                        "Adds the specified element to the set of the values for the reference " +
                        "<code>" + referenceDef.getName() + "</code> using a reassignable qualifier."
                    ));
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText(
                        "   * ",
                        "<em>Note: This is an extension to the JMI 1 standard.</em>"
                    ));
                    if (referenceDef.getAnnotation() != null) {
                        this.pw.println("   * <p>");
                        this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                    }
                    this.pw.println("   * @param " + referenceDef.getQualifierName() + " The qualifier attribute value that qualifies the reference to get the element to be appended.");
                    this.pw.println("   * @param " + valueHolder + " The element to be appended.");
                    this.pw.println("   */");
                    this.pw.println("  public void " + this.getMethodName("add" + referenceDef.getBeanGenericName()) + " (");
                    this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName() + ",");
                    this.pw.println("    " + referenceType + " " + valueHolder);        
                    this.pw.println("  );");
//              }
                this.pw.println();
            }
        }
    }

    // -----------------------------------------------------------------------
    public void mapReferenceRemoveOptional(
        ReferenceDef referenceDef) throws ServiceException {
        if(false) {
            this.trace("Instance/ReferenceRemoveOptional");
            this.pw.println("  /**");
            this.pw
            .println("   * Removes the value for the optional reference <code>"
                + referenceDef.getName() + "</code>.");
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "To remain standard compliant you should substitute this method with <code>set" + 
                referenceDef.getBeanGenericName() + "(null)</code>.</em>"
            ));
            if (referenceDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
            }
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName("remove" + referenceDef.getBeanGenericName()) + " (");
            this.pw.println("  );");
            this.pw.println();
        }
    }

    // -----------------------------------------------------------------------
    public void mapReferenceRemoveWithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        if(false && getFormat() == Format.JMI1) {
            this.trace("Instance/ReferenceRemoveWithQualifier");
            this.pw.println("  /**");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "Removes the qualified (by means of the specified qualifier attribute value) " +
                "element from the list of all the values for the reference <code>" +
                referenceDef.getName() + 
                "</code>."
            ));
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "To remain standard compliant you should use " +
                "<code>javax.jdo.Query.deletePersistentAll()</code>.</em>"
            ));
            if (referenceDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
            }
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "@param "
                    + referenceDef.getQualifierName()
                    + " The qualifier attribute value that qualifies the reference to get the element to be removed."));
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName("remove" + referenceDef.getBeanGenericName()) + " (");
            this.pw.println("    "
                + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " "
                + referenceDef.getQualifierName() + "");
            this.pw.println("  );");
        }
    }

    // -----------------------------------------------------------------------
    public void mapReferenceSetNoQualifier(
        ReferenceDef referenceDef, 
        boolean referencedEnd
    ) throws ServiceException {
        String referenceName;
        String qualifiedTypeName;
        String beanSetterName;
        String argumentType;
        if(referencedEnd){
            referenceName = getFeatureName(referenceDef);
            qualifiedTypeName = referenceDef.getQualifiedTypeName();
            beanSetterName = referenceDef.getBeanSetterName();
            argumentType = this.interfaceType(
                qualifiedTypeName, 
                org.openmdx.model1.importer.metadata.Visibility.CCI,
                false
            );
        } else {
            referenceName = getFeatureName(referenceDef.getExposedEndName());
            qualifiedTypeName = referenceDef.getExposedEndQualifiedTypeName();
            beanSetterName = Identifier.OPERATION_NAME.toIdentifier(
                referenceName,
                null, // removablePrefix
                "addTo", // prependablePrefix
                null, // removableSuffix
                null // appendableSuffix
            );
            ClassDef classDef = getClassDef(qualifiedTypeName);
            argumentType = getClassType(classDef).getType(classDef, this.getFormat(), TypeMode.MEMBER);
        }
        this.trace("Instance/ReferenceSetNoQualifier");
        this.pw.println("  /**");
        this.pw.println("   * Sets a new value for the reference <code>"
            + referenceName + "</code>.");
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw
        .println("   * @param " + referenceName + " The non-null new value for this reference.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(beanSetterName) + "(");
        this.pw.println("    " + argumentType + ' ' + referenceName);
        if(getFormat() == Format.JDO2) {
            ClassDef classDef = getClassDef(qualifiedTypeName);
            ClassType classType = getClassType(classDef);
            String memberType = classType.getType(classDef, this.getFormat(), TypeMode.MEMBER);
            this.pw.println("  ){");            
            boolean mixin = classType == ClassType.MIXIN; 
            if(mixin) {
                this.pw.println("    this." + referenceName + INSTANCE_SUFFIX + " = " + referenceName + ';');
                this.pw.println("    this." + referenceName + " = openmdxjdoGetObjectId(" + referenceName + ");");
            } else {
                if(referencedEnd) {
                    this.pw.println("    this." + referenceName + " = (" + memberType + ')' + referenceName + ';');
                } else {
                    this.pw.println("    this." + referenceName + " = " + referenceName + ';');
                }
            }
            if(!referencedEnd) {
                String qualifiedQualifierType = this.directCompositeReference.getQualifiedQualifierTypeName();
                String qualifierArgumentType = getType(qualifiedQualifierType);
                this.pw.println("    this." + JDO_IDENTITY_MEMBER + " = openmdxjdoNewObjectId(");
                this.pw.println("      java.lang.Boolean." + (mixin ? "TRUE" : "FALSE") + ", // mix-in");
                this.pw.println("      " + referenceName + ", // parent");
                this.pw.println("      \"" + getFeatureName(referenceDef) + "\", // referenceName");
                this.pw.println("      java.util.Collections.singletonList(" + qualifierArgumentType + ".class), // qualifierClass"); 
                this.pw.println("      " + CLASS_ACCESSOR + "()");
                this.pw.println("    );");
            }
            this.pw.println("  }");
        } else {
            this.pw.println("  );");            
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapReferenceGetx_1NoQualifier(
        ReferenceDef referenceDef, 
        boolean optional, 
        boolean referencedEnd
    ) throws ServiceException {
        String referenceName = referencedEnd ?
            getFeatureName(referenceDef) :
                getFeatureName(referenceDef.getExposedEndName());
            String qualifiedTypeName = referencedEnd ?
                referenceDef.getQualifiedTypeName() :
                    referenceDef.getExposedEndQualifiedTypeName();
                ClassDef classDef = getClassDef(qualifiedTypeName);
                ClassType classType = getClassType(classDef);
                if(getFormat() == Format.JDO2) {
                    mapDeclareReference("  ", qualifiedTypeName, referenceName);
                }
                if(referencedEnd) {
                    this.trace("Instance/ReferenceGetx_1NoQualifier");
                    this.pw.println("  /**");
                    this.pw.println("   * Retrieves the value for the reference <code>"
                        + referenceName + "</code>.");
                    if (referenceDef.getAnnotation() != null) {
                        this.pw.println("   * <p>");
                        this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                    }
                    this.pw.print("   * @return The ");
                    this.pw.print(optional ? "&ndash; possibly <code>null</code> &ndash;" : "non-<code>null</code>");
                    this.pw.println(" value for this reference.");
                    this.pw.println("   */");
                    String accessorType = classType.getType(classDef, this.getFormat(), TypeMode.RESULT); 
                    this.pw.println(
                        "  public " + accessorType + ' '
                        + this.getMethodName(referenceDef.getBeanGetterName()) + '('
                    );
                    if(getFormat() == Format.JDO2) {
                        this.pw.println("  ){");
                        switch (classType) {
                            case MIXIN:
                                this.pw.println("    return this." + referenceName + INSTANCE_SUFFIX + " == null ? this." + referenceName + INSTANCE_SUFFIX + " = openmdxjdoGetObject(");
                                this.pw.println("       " + accessorType + ".class,");
                                this.pw.println("       this." + referenceName);
                                this.pw.println("    ) : this." + referenceName + INSTANCE_SUFFIX + ';');
                                break;
                            case EXTENSION:
                                this.pw.println("    return (" + accessorType + ")this." + referenceName + ";");
                                break;
                            case OBJECT:
                                this.pw.println("    return this." + referenceName + ";");
                                break;
                        }
                        this.pw.println("  }");
                    } else {
                        this.pw.println("  );");
                    }
                    this.pw.println();
                }
    }

    // -----------------------------------------------------------------------
    public void mapReferenceGet0_nWithQualifier(
        ReferenceDef referenceDef, 
        boolean delegate
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        this.trace("Instance/ReferenceGet0_nWithQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value."));
        this.pw.println("   * <p>");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "<em>Note: This is an extension to the JMI 1 standard.<br>" +
            "In order to remain standard compliant you should substitute this method with <code>java.jdo.Query</code></em>"
        ));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param " + referenceDef.getQualifierName() + " The value for the qualifier attribute that qualifies this reference.");
        this.pw.println("   * @return The collection of referenced objects.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(referenceDef, "java.util.Collection", Boolean.TRUE, TypeMode.MEMBER) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + getType(referenceDef, null, Boolean.FALSE, TypeMode.MEMBER) + " " + referenceDef.getQualifierName());
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");
            this.pw.println("    throw new java.lang.UnsupportedOperationException(\"Not yet implemented\");"); // TODO
            this.pw.println("  }");
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
//      this.pw.println("  /**");
//      MapperUtils
//      .wrapText(
//      "   * ",
//      "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value and query.");
//      if (referenceDef.getAnnotation() != null) {
//      this.pw.println("   * <p>");
//      this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
//      }
//      this.pw.println("   * @param query query which is applied to the set of referenced objects.");
//      this.pw.println("   * @return The filtered collection of referenced objects.");
//      this.pw.println("   */");
//      this.pw.println("  public " + getType(referenceDef, "java.util.List", Boolean.TRUE) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
//      this.pw.println("    " + this.getType(referenceDef, null, Boolean.FALSE) + " " + referenceDef.getQualifierName() + ",");
//      this.pw.println("    " + this.cciType(referenceDef.getQualifiedTypeName()) + "Query query");
//      if(getFormat() == Format.JDO2) {
//      this.pw.println("  ){");
//      this.pw.println("    throw new java.lang.UnsupportedOperationException(\"Not yet implemented\");"); 
//      this.pw.println("  }");
//      } else {
//      this.pw.println("  );");
//      }
//      this.pw.println();
    }

    public void mapReferenceGet0_nWithQuery(
        ReferenceDef referenceDef
    ) throws ServiceException {
        String referenceName = getFeatureName(referenceDef);
        String collectionType = referenceDef.isComposition() || referenceDef.isShared() ? interfaceType(
            referenceDef.getQualifiedAssociationName(),
            null,
            false
        ) + '.' + Identifier.CLASS_PROXY_NAME.toIdentifier(
            referenceDef.getName()
        ) : "java.util.Set";
            Boolean mixIn = null;
            ClassMetaData classMetaData;

            if(getFormat() == Format.JDO2) {
                classMetaData = (ClassMetaData) getClassDef(referenceDef.getQualifiedTypeName()).getClassMetaData();
                if(classMetaData.isRequiresExtent()) {
                    this.trace("Instance/ReferenceDeclaration");
                    this.pw.println();
                    this.pw.println("  /**");
                    this.pw.println("   * Reference <code>" + referenceName + "</code>.");
                    this.pw.println("   */");
                    if(mixIn = isMixIn(referenceDef)) {
                        this.pw.print("  private java.util.Set<java.lang.String> " + referenceName + ';');
                        this.pw.println();
                        this.pw.println("  /**");
                        this.pw.println("   * Reference <code>" + referenceName + "</code> instances.");
                        this.pw.println("   */");
                        this.pw.print("  private transient ");
                        this.pw.println(getType(referenceDef, "java.util.Set", null, TypeMode.MEMBER) + ' ' + referenceName + INSTANCE_SUFFIX + ';');
                        this.pw.println();
                    } else {
                        if (referenceDef.isComposition()) {
                            this.pw.println("@SuppressWarnings(\"unused\")");
                            this.pw.print("  private transient ");
                            this.pw.println(getType(referenceDef, collectionType, null, TypeMode.MEMBER) + ' ' + referenceName + ';');
                        } else {
                            this.pw.print("  private ");
                            this.pw.println(getType(referenceDef, "java.util.Set", null, TypeMode.MEMBER) + ' ' + referenceName + ';');
                        }
                        this.pw.println();
                    }
                }
            } else {
                classMetaData = null;
            }
            this.trace("Instance/ReferenceGet0_nWithQuery");
            if(getFormat() == Format.JMI1) {
                String qualifiedQueryName = getQueryType(
                    referenceDef.getQualifiedTypeName(), 
                    getNamespace(
                        MapperUtils.getNameComponents(
                            MapperUtils.getPackageName(
                                referenceDef.getQualifiedTypeName()
                            )
                        )
                    )
                );
                this.pw.println("  /**");
                this.pw.println(MapperUtils.wrapText(
                    "   * ",
                    "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified query."));
                this.pw.println(MapperUtils.wrapText(
                    "   * ",
                    "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                    "In order to remain standard compliant you should substitute this method with <code>java.jdo.Query</code></em>"
                ));
                if (referenceDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                }
                this.pw.println("   * @param query predicate which is applied to the set of referenced objects.");
                this.pw.println("   * @return The objects for which the predicate evaluates to <code>true</code>.");
                this.pw.println("   */");
                this.pw.println("  public <T extends " + this.getType(referenceDef.getQualifiedTypeName()) + "> java.util.List<T> " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
                this.pw.println("    " + qualifiedQueryName + " query");
                this.pw.println("  );");
                this.pw.println();
            } else {
                this.pw.println("  /**");
                this.pw.println(MapperUtils.wrapText(
                    "   * ",
                    "Retrieves a set containing all the elements for the reference <code>" + referenceDef.getName() + "</code>."));
                if (referenceDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                }
                this.pw.println("   * @return A set containing all the elements for this reference.");
                this.pw.println("   */");
                String cast = this.printAnnotationAndReturnCast(referenceDef, collectionType);
                String methodName = this.getMethodName(referenceDef.getBeanGetterName());
                this.pw.println("  public " + getType(referenceDef, collectionType, Boolean.TRUE, TypeMode.MEMBER) + ' ' + methodName + "(");
                if(getFormat() == Format.JDO2) {
                    this.pw.println("  ){");
                    if(referenceDef.isComposition()) {
                        if(classMetaData.isRequiresExtent()) {
                            this.pw.println("    return " + cast + " null; // TODO");
                        } else {
                            this.pw.println("    throw new javax.jdo.JDOUserException(");
                            this.pw.println("      \"Extent not managed for '" + referenceDef.getQualifiedTypeName().replace(":","::") + "'\"");
                            this.pw.println("    );");
                        }
                    } else if(mixIn) {
                        this.pw.println("    if(this." + referenceName + INSTANCE_SUFFIX + " == null){");
                        this.pw.println("      this." + referenceName + INSTANCE_SUFFIX + " = openmdxjdoGetObjectSet(");
                        this.pw.println("        " + getType(referenceDef, null, null, TypeMode.MEMBER) + ".class,");
                        this.pw.println("        this." + referenceName);
                        this.pw.println("      );");
                        this.pw.println("    }");
                        this.pw.println("    return (" + collectionType + "<T>)this." + referenceName + INSTANCE_SUFFIX + ';');
                    } else if (referenceDef.isShared()) {
                        this.pw.println("    Object tmp = this." + referenceName + ';');
                        this.pw.println("    return " + cast + "tmp;");
                    } else {
                        this.pw.println(
                            "    " + collectionType + "<?> " + referenceName + " = this." + referenceName + ';'
                        );
                        this.pw.println("    return " + cast + referenceName + ';');
                    }                
                    this.pw.println("  }");
                } else {
                    this.pw.println("  );");
                }
            }
            this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapReferenceGet0_nNoQuery(
        ReferenceDef referenceDef, 
        boolean delegate
    ) throws ServiceException {
        if(this.getFormat() == Format.JMI1) return;
        String referenceName = getFeatureName(referenceDef);
        String qualifierType = referenceDef.getQualifiedQualifierTypeName();
        if(qualifierType != null) qualifierType = qualifierType.intern();
        if(qualifierType == null || qualifierType == PrimitiveTypes.INTEGER) {
            String collectionType = qualifierType == null ? 
                "java.util.Set" :
                    "java.util.List";
            int attributeField = -1;
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            if(getFormat() == Format.JDO2 && !delegate) {
                attributeField = this.sliced.size();
                this.trace("Instance/ReferenceDeclaration0_n");
                this.pw.println();
                this.pw.println("  /**");
                this.pw.println("   * Reference <code>" + referenceName + "</code> as <code>" + collectionType + "</code>");
                this.pw.println("   */");
                this.pw.println("  private transient " + getType(referenceDef, collectionType, null, TypeMode.MEMBER) + ' ' + referenceName + ';');
                this.pw.println();
                this.sliced.put(referenceName, referenceDef.getQualifiedTypeName());
            }
            this.trace("Instance/ReferenceGet0_nNoQuery");
            this.pw.println("  /**");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "Retrieves the <code>Collection</code> of objects referenced by <code>" + referenceDef.getName() + "</code>."));
            if (referenceDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
            }
            this.pw.println("   * @return The <code>Collection</code> of referenced objects.");
            this.pw.println("   */");
            String cast = this.printAnnotationAndReturnCast(referenceDef, collectionType);
            this.pw.println("  public " + this.getType(referenceDef, collectionType, Boolean.TRUE, TypeMode.MEMBER) + " " + methodName + "(");
            if(getFormat() == Format.JDO2) {
                String openmdxjdoMethodName = OPENMDX_JDO_PREFIX + "Get" + collectionType.substring("java.util.".length());
                this.pw.println("  ){");
                if(delegate) {
                    this.pw.println("    return super." + methodName + "();");
                } else {
                    this.pw.println("    " + collectionType + ' ' + referenceName + " = this." + referenceName + " == null ?");
                    this.pw.println("      this." + referenceName + " = " + openmdxjdoMethodName + "(" + SLICE_CLASS_NAME + '.' + FIELD_OFFSET_MEMBER + " + " + attributeField + ") : ");
                    this.pw.println("      this." + referenceName + ';');
                    this.pw.println("    return " + cast + referenceName + ';');
                }
                this.pw.println("  }");
            } 
            else {
                this.pw.println("  );");
            }
            this.pw.println();
        } else if(PrimitiveTypes.STRING == qualifierType) {
            ClassDef classDef = getClassDef(referenceDef.getQualifiedTypeName());
            String referenceType = getClassType(classDef).getType(classDef, getFormat(), TypeMode.INTERFACE);
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            if(getFormat() == Format.JDO2 && !delegate) {
                this.trace("Instance/ReferenceDeclarationMap");
                this.pw.println();
                this.pw.println("  /**");
                this.pw.println("   * Reference <code>" + referenceName + "</code> as <code>java.util.Map</code>");
                this.pw.println("   */");
                this.pw.println("  private transient java.util.Map<java.lang.String," + referenceType + "> " + referenceName + ';');
                this.pw.println();
            }
            this.trace("Instance/ReferenceGetMap");
            this.pw.println("  /**");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "Retrieves the <code>Mao</code> of objects referenced by <code>" + referenceDef.getName() + "</code>."));
            if (referenceDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
            }
            this.pw.println("   * @return The <code>Map</code> of referenced objects.");
            this.pw.println("   */");
            String cast = printAnnotationAndReturnMapCast(referenceDef, java.lang.String.class);
            this.pw.println("  public " + this.getMapType(referenceDef, java.lang.String.class, Boolean.TRUE) + " " + methodName + "(");
            if(getFormat() == Format.JDO2) {
                this.pw.println("  ){");
                if(delegate) {
                    this.pw.println("    return super." + methodName + "();");
                } else {
                    this.pw.println("    if(this." + referenceName + " == null){");
                    this.pw.println("      this." + referenceName + " = openmdxjdoNewMap();");
                    this.pw.println("    }");
                    this.pw.println("    return " + cast + "this." + referenceName + ';');
                }
                this.pw.println("  }");
            } else {
                this.pw.println("  );");
            }
            this.pw.println();
        }
    }

    // -----------------------------------------------------------------------
    public void mapReferenceGet0_1WithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        Format format = getFormat();
        if(format == Format.JMI1) {
            ClassDef classDef = getClassDef(referenceDef.getQualifiedTypeName());
            String accessorType = getClassType(classDef).getType(classDef, format, TypeMode.RESULT); 
            String qualifierPersistencyArgumentName = referenceDef.getQualifierName() + PERSISTENCY_SUFFIX;
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            this.trace("Instance/IntfReferenceGet0_1WithQualifier");
            for(
                    int i = 0;
                    i < 2;
                    i++
            ){
                this.pw.println("  /**");
                this.pw.println(MapperUtils.wrapText(
                    "   * ",
                    "Retrieves the value for the reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value."));
                if(i == 1) {
                    this.pw.println("   * <p>");
                    this.pw.println(
                        MapperUtils.wrapText(
                            "   * ",
                            "This method is equivalent to the preferred invocation <code>" + 
                            methodName + "(false," + referenceDef.getQualifierName() + ")</code>."
                        )
                    );
                }
                this.pw.println("   * <p>");
                if (referenceDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                }
                if(i == 0) {
                    this.pw.println("   * @param " + qualifierPersistencyArgumentName + " Defines whether value for the qualifier is persistent or not");
                }
                this.pw.println("   * @param " + referenceDef.getQualifierName() + " The value for the qualifier attribute that qualifies this reference.");
                this.pw.println("   * @return The possibly null value for this qualifier");
                this.pw.println("   */");
                this.pw.println("  public " + accessorType + ' ' + methodName + '(');
                if(i == 0) {
                    this.pw.println("    boolean " + qualifierPersistencyArgumentName + ",");
                }
                this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName());
                if(format == Format.JDO2) {
                    this.pw.println("  ){");
                    this.pw.println("    throw new java.lang.UnsupportedOperationException(\"Not yet implemented\");"); // TODO
                    this.pw.println("  }");
                } else {
                    this.pw.println("  );");
                }
                this.pw.println();
            }
        } 
    }

    // -----------------------------------------------------------------------
    public void mapReferenceGet1_1WithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        if(this.hasContainer()) {
            this.trace("Instance/IntfReferenceGet1_1WithQualifier");
            this.pw.println("  /**");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "Retrieves the value for the optional reference <code>" + referenceDef.getName() + "</code> for the specified qualifier attribute value."));
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "In order to remain standard compliant you should substitute this method with <code>java.jdo.Query</code></em>"
            ));
            if (referenceDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
            }
            this.pw.println("   * @param " + referenceDef.getQualifierName() + " The value for the qualifier attribute that qualifies this reference.");
            this.pw.println("   * @return The non-null value for this reference.");
            this.pw.println("   */");
            this.pw.println("  public " + this.getType(referenceDef, null, Boolean.TRUE, TypeMode.MEMBER) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
            this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + referenceDef.getQualifierName());
            if(getFormat() == Format.JDO2) {
                this.pw.println("  ){");
                this.pw.println("    throw new java.lang.UnsupportedOperationException(\"Qualified object retrieval not yet supported by persistence layer\");");
                this.pw.println("  }");
            } else {
                this.pw.println("  );");
            }
            this.pw.println();       
        }
    }

    // -----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapOperation(
        OperationDef operationDef
    ) throws ServiceException {
        this.trace("Instance/Operation");
        this.pw.println("  /**");
        if (operationDef.getAnnotation() != null) {
            this.pw.println(MapperUtils.wrapText("   * ", operationDef.getAnnotation()));
        }
        for(StructuralFeatureDef param: operationDef.getParameters()) {
            if(!"org:openmdx:base:Void".equals(param.getQualifiedTypeName())) {
                if (param.getAnnotation() != null) {
                    this.pw.println("   * @param " + param.getName() + " " + param.getAnnotation() + "");
                }
            }
        }
        this.pw.println("   */");
        this.pw.println("  public " + this.getReturnType(operationDef) + " " + this.getMethodName(operationDef.getName()) + "(");
        int ii = 0;
        for(StructuralFeatureDef param: operationDef.getParameters()) {
            if(!"org:openmdx:base:Void".equals(param.getQualifiedTypeName())) {
                String separator = ii == 0
                ? "      "
                    : "    , ";
                this.mapParameter(
                    separator,
                    param, 
                    ""
                );
                ii++;
            }
        }
        if(getFormat() == Format.JDO2) { 
            this.pw.println(" ){");
            this.pw.println("    throw new javax.jdo.JDOFatalUserException(");
            this.pw.println("      \"Behavioural features not handled by by data object\",");
            this.pw.println("      new UnsupportedOperationException(\"Behavioural feature\"),");
            this.pw.println("      this");
            this.pw.println("    );");
            this.pw.println("  }");
        } 
        else {
            List<ExceptionDef> exceptions = operationDef.getExceptions();
            this.pw.print("  )");
            String separator = " throws ";
            for(ExceptionDef exceptionDef : exceptions) {
                String namespace = this.getNamespace(
                    MapperUtils.getNameComponents(
                        MapperUtils.getPackageName(
                            exceptionDef.getQualifiedName(),
                            2
                        )
                    )
                );
                this.pw.print(
                    separator +
                    namespace +
                    "." + 
                    exceptionDef.getName()
                );
                separator = ", ";
            }
            this.pw.println(";");            
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapEnd(
    ) throws ServiceException {
        this.trace("Instance/End.vm");
        this.pw.println();
        if(getFormat() == Format.JDO2) { 
            mapSingleValuedFields();
            mapMultivaluedFields();
            mapClass();
        }
        if(isBaseClass()) {
            if(hasContainer()) {
                if(!this.localFeatures.containsKey(this.directCompositeReference.getExposedEndName())){
                    mapReferenceGetx_1NoQualifier(
                        this.directCompositeReference, 
                        false, // optional
                        false // referencedEnd
                    );
                }
                if(this.getFormat() == Format.JDO2) {        
                    mapReferenceSetNoQualifier(
                        this.directCompositeReference, 
                        false // referencedEnd
                    );
                }
                mapContainment(); 
                mapIdentity();
            } else if (isAuthority()){
                mapAuthority();
            }
        } else {
            mapIdentity();
        }
        this.pw.println("}");
    }

    // -----------------------------------------------------------------------
    private void mapAuthority(
    ) throws ServiceException {
        switch(getFormat()) {
            case JDO2: {
                this.trace("Instance/Authority");
                String qualifierArgumentType = "java.lang.String";
                String qualifierValueName = "authority";
                String qualifierTypeArgumentName = qualifierValueName + QUALIFIER_TYPE_SUFFIX;
                String qualifierTypeAccessorName = "getAuthorityType";
                String qualifierAccessorName = "getAuthority";
                this.pw.println("  /**");
                this.pw.println("   * The object's application identity");
                this.pw.println("   */");
                this.pw.println("  public static class " + OBJECT_IDENTITY_CLASS_NAME);
                this.pw.println("    extends " + QUALIFIED_ABSTRACT_OBJECT_IDENTITY_CLASS_NAME);
                this.pw.println("    implements " + this.interfaceType(this.classDef, Visibility.CCI, false) + "." + OBJECT_IDENTITY_CLASS_NAME);
                this.pw.println("  {");
                this.pw.println();
                this.pw.println("    public " + OBJECT_IDENTITY_CLASS_NAME + "(");
                this.pw.println("      " + QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeArgumentName + ",");
                this.pw.println("      " + qualifierArgumentType + " " + qualifierValueName);
                this.pw.println("    ){");
                this.pw.println("      this(");
                this.pw.println("        " + qualifierTypeArgumentName + ","); 
                this.pw.println("        " + qualifierValueName + ",");
                this.pw.println("        BASE_CLASS // The authority is final");
                this.pw.println("      );");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    protected " + OBJECT_IDENTITY_CLASS_NAME + "(");
                this.pw.println("      " + QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeArgumentName + ",");
                this.pw.println("      " + qualifierArgumentType + " " + qualifierValueName + ",");
                this.pw.println("      java.util.List<String> objectClass");
                this.pw.println("    ){");
                this.pw.println("      super(");
                this.pw.println("        java.util.Collections.singletonList(" + qualifierTypeArgumentName + "),"); 
                this.pw.println("        java.util.Collections.singletonList(" + qualifierValueName + "),");
                this.pw.println("        objectClass");
                this.pw.println("      );");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    protected java.lang.String referenceName(");
                this.pw.println("    ){");
                this.pw.println("      return null;");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * The authority has no parent.");
                this.pw.println("     * @return <code>null</code>");
                this.pw.println("     */");
                this.pw.println("    protected " + QUALIFIED_IDENTITY_CLASS_NAME + " parent(");
                this.pw.println("    ){");
                this.pw.println("      return null;");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * Tells whether the <code>" + qualifierValueName + "</code> value is persistent or reassignable.");
                this.pw.println("     * @return <code>PERSISTENT</code> or <code>REASSIGNABLE</code>");
                this.pw.println("     */");
                this.pw.println("    public " + QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeAccessorName + "(");
                this.pw.println("    ){");
                this.pw.println("      return identifierType(0);");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * The <code>" + qualifierValueName + "</code> value");
                this.pw.println("     * @return the <code>" + qualifierValueName + "</code> value");
                this.pw.println("     */");
                this.pw.println("    public " + qualifierArgumentType + " " + qualifierAccessorName + "(");
                this.pw.println("    ){");
                this.pw.println("      return qualifier(" + qualifierArgumentType + ".class, 0);");
                this.pw.println("    }");
                this.pw.println();            
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  public static " + OBJECT_IDENTITY_CLASS_NAME + " newIdentity(");
                this.pw.println("      " + QUALIFIER_TYPE_CLASS_NAME +" " + qualifierTypeArgumentName + ",");
                this.pw.println("      " + qualifierArgumentType + " " + qualifierValueName);
                this.pw.println("  ){");
                this.pw.println("    return new " + OBJECT_IDENTITY_CLASS_NAME + "(");
                this.pw.println("        " + qualifierTypeArgumentName + ","); 
                this.pw.println("        " + qualifierValueName + ",");
                this.pw.println("        CLASS");
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  public static " + OBJECT_IDENTITY_CLASS_NAME + " openmdxjdoToIdentity(");
                this.pw.println("      " + QUALIFIED_OBJECT_ID_CLASS_NAME + " objectId");
                this.pw.println("  ){");
                this.pw.println("    return new " + OBJECT_IDENTITY_CLASS_NAME + "(");
                this.pw.println("        " + QUALIFIER_TYPE_CLASS_NAME + ".valueOf(objectId.isQualifierPersistent(0)),"); 
                this.pw.println("        objectId.getQualifier(" + qualifierArgumentType + ".class, 0),"); 
                this.pw.println("        objectId.getTargetClass()"); 
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
            } break;
            case CCI2: {
                this.pw.println("  /**");
                this.pw.println("   * Object Identity");
                this.pw.println("   */");
                this.pw.println("  public interface " + InstanceMapper.OBJECT_IDENTITY_CLASS_NAME + " extends " + QUALIFIED_IDENTITY_CLASS_NAME + " {");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * Retrieve the whole <code>authority</code>.");
                this.pw.println("     * @return the whole <code>authority</code> value");
                this.pw.println("     */");
                this.pw.println("    public java.lang.String getAuthority();");
                this.pw.println();            
                this.pw.println("  }");
                this.pw.println();            
            } break;
        }
    }

    // -----------------------------------------------------------------------
    private void mapClass(){
        this.pw.println("  /**");
        for(
                int i = 0;
                i < this.qualifiedClassName.size();
                i++
        ){
            this.pw.print(i == 0 ? "   * Define the model class <code>" : "::");
            this.pw.print(this.qualifiedClassName.get(i));
        }
        this.pw.println("</code>");
        this.pw.println("   */");
        this.pw.println("  final static public " + CLASS_TYPE + " " + CLASS_MEMBER + " = openmdxjdoClassName(");
        for(
                int i = 0;
                i < this.qualifiedClassName.size();
                i++
        ){
            this.pw.print(i == 0 ? "    \"" : ", \"");
            this.pw.print(this.qualifiedClassName.get(i));
            this.pw.print("\"");
        }
        this.pw.println("\n  );");
        this.pw.println();
        this.pw.println("  /**");
        this.pw.println("   * Retrieve the model class");
        this.pw.println("   * @return the model class");
        this.pw.println("   * @see " + this.className + "." + CLASS_MEMBER);
        this.pw.println("   */");
        this.pw.println("  protected " + CLASS_TYPE + " " + CLASS_ACCESSOR + " (");
        this.pw.println("  ){");
        this.pw.println("    return " + CLASS_MEMBER +";");
        this.pw.println("  }");
        this.pw.println();
        if(isBaseClass()) {
            this.pw.println("  /**");
            this.pw.println("   * Define this class as base class");
            this.pw.println("   * @see " + this.className + "." + CLASS_MEMBER);
            this.pw.println("   */");
            this.pw.print("  final static public " + CLASS_TYPE + " " + BASE_CLASS_MEMBER + " = ");
            this.pw.print(this.className);
            this.pw.println("." + CLASS_MEMBER + ";");
            this.pw.println();
        }
    }

    // -----------------------------------------------------------------------
    private void mapContainment(
    ) throws ServiceException{

        this.trace("Instance/Containment");
        //
        // Names
        // 
        String referenceName = this.directCompositeReference.getExposedEndName();
        String qualifierName = this.directCompositeReference.getQualifierName();
        String objectName = this.directCompositeReference.getName();
        //
        // Types
        // 
        String qualifiedReferenceType = this.directCompositeReference.getExposedEndQualifiedTypeName();
        ClassDef classDef = getClassDef(qualifiedReferenceType);
        ClassType classType = getClassType(classDef);
        String referenceMemberType = classType.getType(classDef, this.getFormat(), TypeMode.MEMBER);
        String qualifiedQualifierType = this.directCompositeReference.getQualifiedQualifierTypeName();
        String qualifierArgumentType = getType(qualifiedQualifierType);
        String referenceValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(referenceName);
        String qualifierValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(qualifierName);
        String objectValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(objectName);
        if(objectValueName.equals(qualifierValueName)) {
            objectValueName = '_' + objectValueName;
        }
        Boolean mixin = null;
        switch(getFormat()) {
            case JDO2:
                mixin = Boolean.valueOf(
                    getClassType(getClassDef(qualifiedReferenceType)) == ClassType.MIXIN
                );
                //
                // Mutator
                //
                String associationMutatorName = Identifier.OPERATION_NAME.toIdentifier(
                    referenceName,
                    null, // removablePrefix
                    "addTo", // prependablePrefix
                    null, // removableSuffix
                    null // appendableSuffix
                );
                String qualifierPersistencyArgumentName = qualifierValueName + PERSISTENCY_SUFFIX;
                this.pw.println("  /**");
                this.pw.println("   * Set the object's composite association");
                this.pw.println("   * <code>" + this.directCompositeReference.getQualifiedAssociationName() + "</code>.");
                if(this.directCompositeReference.getAnnotation() != null) {
                    this.pw.println("  * <p>");
                    this.pw.println(MapperUtils.wrapText(" * ", this.directCompositeReference.getAnnotation()));
                }
                this.pw.println("   * @param " + referenceValueName);
                this.pw.println("   * The non-null new value for this object's composite owner.");
                this.pw.println("   * @param " + qualifierPersistencyArgumentName);
                this.pw.println("   * Defines whether the <code>" + qualifierValueName + "</code> is persistent.");
                this.pw.println("   * @param " + qualifierValueName);
                this.pw.println("   * The non-null new value for this object's qualifier.");
                this.pw.println("   */");
                this.pw.println("  public void " + associationMutatorName + "(");
                this.pw.println("    " + referenceMemberType + ' ' + referenceValueName + ',');
                this.pw.println("    boolean " + qualifierPersistencyArgumentName + ',');
                this.pw.println("    " + qualifierArgumentType + ' ' + qualifierValueName);
                this.pw.println("  ){");
                if(mixin) {
                    this.pw.println("    this." + referenceValueName + INSTANCE_SUFFIX + " = " + referenceValueName + ';');
                    this.pw.println("    this." + referenceValueName + " = openmdxjdoGetObjectId(" + referenceValueName + ");");
                } else {
                    this.pw.println("    this." + referenceValueName + " = " + referenceValueName + ';');
                }            
                this.pw.println("    this." + JDO_IDENTITY_MEMBER + " = openmdxjdoNewObjectId(");
                this.pw.println("      java.lang.Boolean." + (mixin ? "TRUE" : "FALSE") + ", // mix-in");
                this.pw.println("      " + referenceValueName + ", // parent");
                this.pw.println("      \"" + getFeatureName(this.directCompositeReference) + "\", // referenceName");
                this.pw.println("      java.util.Collections.singletonList(" + qualifierPersistencyArgumentName + "),");
                this.pw.println("      java.util.Collections.singletonList(" + qualifierValueName + "),");
                this.pw.println("      BASE_CLASS,");
                this.pw.println("      " + CLASS_ACCESSOR + "()");
                this.pw.println("    );");
                this.pw.println("  }");
                break;
            case CCI2: 
                String qualifierTypeAccessorName = Identifier.OPERATION_NAME.toIdentifier(
                    qualifierName, 
                    null, // removablePrefix
                    "get", // prependablePrefix
                    null, // removableSuffix
                    QUALIFIER_TYPE_WORD // appendableSuffix
                );
                String qualifierAccessorName = Identifier.OPERATION_NAME.toIdentifier(
                    qualifierName, 
                    null, // removablePrefix
                    "get", // prependablePrefix
                    null, // removableSuffix
                    null // appendableSuffix
                );
                String referenceAccessorName = Identifier.OPERATION_NAME.toIdentifier(
                    referenceName, 
                    null, // removablePrefix
                    "get", // prependablePrefix
                    null, // removableSuffix
                    null // appendableSuffix
                );
                ClassDef parentClassDef = getClassDef(qualifiedReferenceType);
                String parentIdentityType;
                if(parentClassDef.isMixIn()) {
                    parentIdentityType = QUALIFIED_IDENTITY_CLASS_NAME;
                } else {
                    parentClassDef = parentClassDef.getBaseClassDef();
                    String cci2Class = ClassType.getType(
                        parentClassDef.getQualifiedName(), 
                        Names.CCI2_PACKAGE_SUFFIX
                    );
                    parentIdentityType = cci2Class + "." + InstanceMapper.OBJECT_IDENTITY_CLASS_NAME;
                }
                this.pw.println("  /**");
                this.pw.println("   * Object Identity");
                this.pw.println("   */");
                this.pw.println("  public interface " + InstanceMapper.OBJECT_IDENTITY_CLASS_NAME + " extends " + QUALIFIED_IDENTITY_CLASS_NAME + " {");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * Retrieve the <code>" + classDef.getName() + "</code>'s identity");
                this.pw.println("     * @return the parent object's identity");
                this.pw.println("     */");
                this.pw.println("    public " + parentIdentityType + " " + referenceAccessorName + "();");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * Tells whether the <code>" + qualifierValueName + "</code> value is persistent or reassignable.");
                this.pw.println("     * @return <code>PERSISTENT</code> or <code>REASSIGNABLE</code>");
                this.pw.println("     */");
                this.pw.println("    public " + QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeAccessorName + "();");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * The <code>" + qualifierValueName + "</code> value");
                this.pw.println("     * @return the <code>" + qualifierValueName + "</code> value");
                this.pw.println("     */");
                this.pw.println("    public " + qualifierArgumentType + " " + qualifierAccessorName + "();");
                this.pw.println();            
                this.pw.println("  }");
                break;
        }                
        this.pw.println();            
    }

    // -----------------------------------------------------------------------
    private void mapIdentity(
    ) throws ServiceException{
        this.trace("Instance/Identity");
        if(getFormat() == Format.JDO2) {
            if(this.compositeReference != null) {
                String referenceName = this.compositeReference.getExposedEndName();
                String qualifierName = this.compositeReference.getQualifierName();
                String qualifiedReferenceType = this.compositeReference.getExposedEndQualifiedTypeName();
                String qualifiedQualifierType = this.compositeReference.getQualifiedQualifierTypeName();
                String qualifierArgumentType = getType(qualifiedQualifierType);
                String referenceValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(referenceName);
                String qualifierValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(qualifierName);
                String qualifierTypeArgumentName = qualifierValueName + QUALIFIER_TYPE_SUFFIX;
                ClassDef parentClassDef = getClassDef(qualifiedReferenceType);
                String parentIdentityType;
                String parentIdentityBuilder;
                String parentObjectClass;
                if(parentClassDef.isMixIn()) {
                    parentIdentityType = QUALIFIED_IDENTITY_CLASS_NAME;
                    parentIdentityBuilder = null;
                    parentObjectClass = null;
                } else {
                    parentClassDef = parentClassDef.getBaseClassDef();
                    String cci2Class = ClassType.getType(
                        parentClassDef.getQualifiedName(), 
                        Names.CCI2_PACKAGE_SUFFIX
                    );
                    String jdo2Class = ClassType.getType(
                        parentClassDef.getQualifiedName(), 
                        Names.JDO2_PACKAGE_SUFFIX
                    );
                    parentIdentityType = cci2Class + "." + InstanceMapper.OBJECT_IDENTITY_CLASS_NAME;
                    parentIdentityBuilder = jdo2Class + ".openmdxjdoToIdentity";
                    parentObjectClass = jdo2Class + ".CLASS";
                }
                String identityClassName;
                if(isBaseClass())  {
                    String qualifierTypeAccessorName = Identifier.OPERATION_NAME.toIdentifier(
                        qualifierName, 
                        null, // removablePrefix
                        "get", // prependablePrefix
                        null, // removableSuffix
                        QUALIFIER_TYPE_WORD // appendableSuffix
                    );
                    String qualifierAccessorName = Identifier.OPERATION_NAME.toIdentifier(
                        qualifierName, 
                        null, // removablePrefix
                        "get", // prependablePrefix
                        null, // removableSuffix
                        null // appendableSuffix
                    );
                    String referenceAccessorName = Identifier.OPERATION_NAME.toIdentifier(
                        referenceName, 
                        null, // removablePrefix
                        "get", // prependablePrefix
                        null, // removableSuffix
                        null // appendableSuffix
                    );
                    this.pw.println("  /**");
                    this.pw.println("   * The object's application identity");
                    this.pw.println("   */");
                    this.pw.println("  public static class " + OBJECT_IDENTITY_CLASS_NAME);
                    this.pw.println("    extends " + QUALIFIED_ABSTRACT_OBJECT_IDENTITY_CLASS_NAME);
                    this.pw.println("    implements " + interfaceType(this.classDef, Visibility.CCI, false) + "." + OBJECT_IDENTITY_CLASS_NAME);
                    this.pw.println("  {");
                    this.pw.println();
                    this.pw.println("    public " + OBJECT_IDENTITY_CLASS_NAME + "(");
                    this.pw.println("      " + parentIdentityType + " " + referenceValueName + ",");
                    this.pw.println("      " + QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeArgumentName + ",");
                    this.pw.println("      " + qualifierArgumentType + " " + qualifierValueName);
                    this.pw.println("    ){");
                    this.pw.println("      this(");
                    this.pw.println("        " + referenceValueName + ",");
                    this.pw.println("        " + qualifierTypeArgumentName + ","); 
                    this.pw.println("        " + qualifierValueName + ",");
                    this.pw.println("        null // objectClass");
                    this.pw.println("      );");
                    this.pw.println("    }");
                    this.pw.println();
                    this.pw.println("    public " + OBJECT_IDENTITY_CLASS_NAME + "(");
                    this.pw.println("      " + parentIdentityType + " " + referenceValueName + ",");
                    this.pw.println("      " + QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeArgumentName + ",");
                    this.pw.println("      " + qualifierArgumentType + " " + qualifierValueName + ",");
                    this.pw.println("      java.util.List<String> objectClass");
                    this.pw.println("    ){");
                    this.pw.println("      super(");
                    this.pw.println("        java.util.Collections.singletonList(" + qualifierTypeArgumentName + "),"); 
                    this.pw.println("        java.util.Collections.singletonList(" + qualifierValueName + "),");
                    this.pw.println("        objectClass");
                    this.pw.println("      );");
                    this.pw.println("      this." + referenceValueName + " = " + referenceValueName + ";");
                    this.pw.println("    }");
                    this.pw.println();
                    this.pw.println("    private final " + parentIdentityType + " " + referenceValueName + ";");
                    this.pw.println();
                    this.pw.println("    protected java.lang.String referenceName(");
                    this.pw.println("    ){");
                    if(this.compositeReference == null) {
                        this.pw.println("      return null;");
                    } else {
                        this.pw.println("      return \"" + this.directCompositeReference.getName() + "\";");
                    }
                    this.pw.println("    }");
                    this.pw.println();
                    this.pw.println("    /**");
                    this.pw.println("     * Retrieve the <code>" + parentClassDef.getName() + "</code>'s identity");
                    this.pw.println("     * @return the parent object's identity");
                    this.pw.println("     */");
                    this.pw.println("    public " + parentIdentityType + " " + referenceAccessorName + "(");
                    this.pw.println("    ){");
                    this.pw.println("      return this." + referenceValueName + ";");
                    this.pw.println("    }");
                    this.pw.println();
                    this.pw.println("    protected " + QUALIFIED_IDENTITY_CLASS_NAME + " parent("); 
                    this.pw.println("    ){");
                    this.pw.println("      return " + referenceAccessorName + "();");
                    this.pw.println("    }");
                    this.pw.println();
                    this.pw.println("    /**");
                    this.pw.println("     * Tells whether the <code>" + qualifierValueName + "</code> value is persistent.");
                    this.pw.println("     * @return <code>true</code> if the <code>" + qualifierValueName + "</code> value is persistent.");
                    this.pw.println("     */");
                    this.pw.println("    public " + QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeAccessorName + "(");
                    this.pw.println("    ){");
                    this.pw.println("      return identifierType(0);");
                    this.pw.println("    }");
                    this.pw.println();
                    this.pw.println("    /**");
                    this.pw.println("     * The <code>" + qualifierValueName + "</code> value");
                    this.pw.println("     * @return the <code>" + qualifierValueName + "</code> value");
                    this.pw.println("     */");
                    this.pw.println("    public " + qualifierArgumentType + " " + qualifierAccessorName + "(");
                    this.pw.println("    ){");
                    this.pw.println("      return qualifier(" + qualifierArgumentType + ".class, 0);");
                    this.pw.println("    }");
                    this.pw.println();            
                    this.pw.println("  }");
                    this.pw.println();
                    identityClassName = OBJECT_IDENTITY_CLASS_NAME;
                } else {
                    identityClassName = ClassType.getType(
                        this.baseClassDef.getQualifiedName(), 
                        Names.JDO2_PACKAGE_SUFFIX
                    ) + "." + OBJECT_IDENTITY_CLASS_NAME;
                }
                this.pw.println("  public static " + identityClassName + " newIdentity(");
                this.pw.println("      " + parentIdentityType + " " + referenceValueName + ",");
                this.pw.println("      " + QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeArgumentName + ",");
                this.pw.println("      " + qualifierArgumentType + " " + qualifierValueName);
                this.pw.println("  ){");
                this.pw.println("    return new " + identityClassName + "(");
                this.pw.println("        " + referenceValueName + ",");
                this.pw.println("        " + qualifierTypeArgumentName + ","); 
                this.pw.println("        " + qualifierValueName + ",");
                this.pw.println("        CLASS");
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  public static " + identityClassName + " openmdxjdoToIdentity(");
                this.pw.println("      " + QUALIFIED_OBJECT_ID_CLASS_NAME + " objectId");
                this.pw.println("  ){");
                this.pw.println("    return new " + identityClassName + "(");
                if(parentIdentityBuilder == null) {
                    this.pw.println("        " + TO_IDENTITY_METHOD + "(objectId.getParentObjectId(null)),");
                } else {
                    this.pw.println("        " + parentIdentityBuilder + "(");
                    this.pw.println("          objectId.getParentObjectId(" + parentObjectClass + ")");
                    this.pw.println("        ),");
                }
                this.pw.println("        " + QUALIFIER_TYPE_CLASS_NAME + ".valueOf(objectId.isQualifierPersistent(0)),"); 
                this.pw.println("        objectId.getQualifier(" + qualifierArgumentType + ".class, 0),"); 
                this.pw.println("        objectId.getTargetClass()"); 
                this.pw.println("    );");
                this.pw.println("  }");
                this.pw.println();
            }
        }
    }

    // -----------------------------------------------------------------------
    protected String toQualifierAccessor(
        String qualifiedTypeName
    ){
        return PrimitiveTypes.STRING.equals(qualifiedTypeName) ?
            "identityParser.nextString()" :
                PrimitiveTypes.LONG.equals(qualifiedTypeName) ?
                    "identityParser.nextNumber().longValue()" :
                        PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ?
                            "identityParser.nextNumber().intValue()" :
                                PrimitiveTypes.SHORT.equals(qualifiedTypeName) ?
                                    "identityParser.nextNumber().shortValue()" :
                                        PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? 
                                            "(java.math.BigDecimal)identityParser.nextNumber()" :
                                                PrimitiveTypes.UUID.equals(qualifiedTypeName) ?
                                                    "identityParser.nextUUID()" :
                                                        PrimitiveTypes.OID.equals(qualifiedTypeName) ?
                                                            "identityParser.nextOID()" :
                                                                PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ?
                                                                    "identityParser.nextIRI()" :
                                                                        "identityParser.nextString()";
    }

    // -----------------------------------------------------------------------
    protected String getQualifierMutator(
        String qualifiedTypeName
    ){
        return PrimitiveTypes.STRING.equals(qualifiedTypeName) ?
            "appendString" :
                PrimitiveTypes.LONG.equals(qualifiedTypeName) ?
                    "appendNumber" :
                        PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ?
                            "appendNumber" :
                                PrimitiveTypes.SHORT.equals(qualifiedTypeName) ?
                                    "appendNumber" :
                                        PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? 
                                            "appendNumber" :
                                                PrimitiveTypes.UUID.equals(qualifiedTypeName) ?
                                                    "appendUUID" :
                                                        PrimitiveTypes.OID.equals(qualifiedTypeName) ?
                                                            "appendOID" :
                                                                PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ?
                                                                    "appendIRI" :
                                                                        "appendString";
    }

    // -----------------------------------------------------------------------
    boolean isAuthority(){
        return 
        "org:openmdx:base:Authority".equals(this.classDef.getQualifiedName());
    }

    // -----------------------------------------------------------------------
    boolean hasSPI(){
        return this.spiFeatures != null;
    }

    // -----------------------------------------------------------------------
    boolean isProvider(){
        return 
        "org:openmdx:base:Provider".equals(this.classDef.getQualifiedName());
    }

    // -----------------------------------------------------------------------
    boolean hasSlices(){
        return this.extendsClassDef != null || !this.sliced.isEmpty();
//      return !this.sliced.isEmpty();
    }

    // -----------------------------------------------------------------------
    public void mapSingleValuedFields(
    ) throws ServiceException{
        if(hasSlices()) {
            for(Map.Entry<String,String> e : this.sliced.entrySet()){
                this.mapDeclareSize("    ", e.getKey());
            }
            this.pw.println();
            this.pw.println("  protected int openmdxjdoGetSize(int field){");
            this.pw.println("    switch(field - " + SLICE_CLASS_NAME + '.' + FIELD_OFFSET_MEMBER + "){");
            int j = 0;
            for(String attributeName : this.sliced.keySet()){
                this.pw.println("      case " + j++ + ": return this." + attributeName + SIZE_SUFFIX + ";");
            }
            this.pw.println("      default: return super.openmdxjdoGetSize(field);");
            this.pw.println("    }");
            this.pw.println("  }");
            this.pw.println();
            this.pw.println("  protected void openmdxjdoSetSize(int field, int size){");
            this.pw.println("    switch(field - " + SLICE_CLASS_NAME + '.' + FIELD_OFFSET_MEMBER + "){");
            j = 0;
            for(String attributeName : this.sliced.keySet()){
                this.pw.println("      case " + j++ + ": this." + attributeName + SIZE_SUFFIX + " = size; break;");
            }
            this.pw.println("      default: super.openmdxjdoSetSize(field, size);");
            this.pw.println("    }");
            this.pw.println("  }");
            this.pw.println();
        }
    }

    public void mapMultivaluedFields(
    ) throws ServiceException{
        if(isSliceHolder() || hasSlices()) {
            String superClassName = isSliceHolder() ? 
                QUALIFIED_SLICE_CLASS_NAME :
                    this.getType(this.extendsClassDef.getQualifiedName()) + '.' + SLICE_CLASS_NAME;
            this.pw.println();
            this.pw.println("  /**");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<code>" + SLICE_CLASS_NAME + "</code> object hold the <code>" + 
                classDef.getName() + "</code>'s multivalued attributes"
            ));
            this.pw.println("   */");
            this.pw.println("  public static class " + SLICE_CLASS_NAME + " extends " + superClassName + "{");
            this.pw.println();
            for(Map.Entry<String,String> e : this.sliced.entrySet()){
                String qualifiedName = e.getValue();
                if(
                        this.model.isPrimitiveType(qualifiedName) ||
                        this.model.isStructureType(qualifiedName)
                ){
                    String typeName = getValueType(qualifiedName, true);
                    this.mapDeclareValue("    ", typeName, e.getKey());
                } else {
                    this.mapDeclareReference("    ", qualifiedName, e.getKey());
                }
            }
            this.pw.println();
            this.pw.println("    /**");
            this.pw.println("     * Number of fields in the superclasses");
            this.pw.println("     */");
            this.pw.println("    final static int " + FIELD_OFFSET_MEMBER + " = " + superClassName + "." + FIELD_COUNT_ACCESSOR + "();"); 
            this.pw.println();
            this.pw.println("    /**");
            this.pw.println("     * Retrieve the number of fields in the class and its superclasses");
            this.pw.println("     *");
            this.pw.println("     * @return the umber of fields in the class and its superclasses");
            this.pw.println("     */");
            this.pw.println("    protected static int " + FIELD_COUNT_ACCESSOR + "(");  
            this.pw.println("    ){");  
            this.pw.println("      return " + FIELD_OFFSET_MEMBER + " + " + this.sliced.size() + ";");  
            this.pw.println("    }");
            this.pw.println();
            if(isSliceHolder()) {
                this.pw.println("    /**");
                this.pw.println("     * Constructor");
                this.pw.println("     */");
                this.pw.println("    @SuppressWarnings(\"unused\")");
                this.pw.println("    private " + SLICE_CLASS_NAME + '('); 
                this.pw.println("    ){");
                this.pw.println("      // Implements Serializable");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * Constructor");
                this.pw.println("     */");
                this.pw.println("    protected " + SLICE_CLASS_NAME + '(');
                this.pw.println("      " + this.className + " object,");
                this.pw.println("      int index");
                this.pw.println("    ){");
                this.pw.println("      this." + JDO_IDENTITY_MEMBER + " = object;");
                this.pw.println("      this." + INDEX_MEMBER + " = index;");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * The slice's index");
                this.pw.println("     */");
                this.pw.println("    @SuppressWarnings(\"unused\")");
                this.pw.println("    private int " + INDEX_MEMBER + ";");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * The slice's owner");
                this.pw.println("     */");
                this.pw.println("    @SuppressWarnings(\"unused\")");
                this.pw.println("    private " + this.className + " " + JDO_IDENTITY_MEMBER + ";");
                this.pw.println();
                this.pw.println("    /**");
                this.pw.println("     * The slices' compound identity class");
                this.pw.println("     */");
                this.pw.println("    public static class " + SLICE_ID_CLASS_NAME + " extends " + QUALIFIED_ABSTRACT_SLICE_ID_CLASS_NAME + " {");
                this.pw.println();
                this.pw.println("      /**");
                this.pw.println("       * Constructor");
                this.pw.println("       */");
                this.pw.println("      public " + SLICE_ID_CLASS_NAME + "(){");
                this.pw.println("      }");
                this.pw.println();
                this.pw.println("      /**");
                this.pw.println("       * Constructor");
                this.pw.println("       */");
                this.pw.println("      public " + SLICE_ID_CLASS_NAME + "(java.lang.String sliceIdentity){");
                this.pw.println("        this.openmdxjdoIdentity = openmdxjdoIdentity(" + this.className + ".class, sliceIdentity);");
                this.pw.println("        this.openmdxjdoIndex = openmdxjdoIndex(sliceIdentity);");
                this.pw.println("      }");
                this.pw.println();
                this.pw.println("      public javax.jdo.identity.StringIdentity openmdxjdoIdentity;");
                this.pw.println("      protected javax.jdo.identity.StringIdentity openmdxjdoIdentity(){");
                this.pw.println("        return this.openmdxjdoIdentity;");
                this.pw.println("      }");
                this.pw.println();
                this.pw.println("      public int openmdxjdoIndex;");
                this.pw.println("      protected int openmdxjdoIndex(){");
                this.pw.println("        return this.openmdxjdoIndex;");
                this.pw.println("      }");
                this.pw.println();
                this.pw.println("    }");
                this.pw.println();
            } else {
                this.pw.println("    /**");
                this.pw.println("     * Constructor");
                this.pw.println("     */");
                this.pw.println("    protected " + SLICE_CLASS_NAME + '(');
                this.pw.println("      " + this.className + " object,");
                this.pw.println("      int index");
                this.pw.println("    ){");
                this.pw.println("      super(object, index);");
                this.pw.println("    }");
            }
            this.pw.println();
            if(!hasSlices()){
                this.pw.println("  }");
                this.pw.println();
            } else {
                this.pw.println("    protected java.lang.Object getValue(int field){");
                this.pw.println("      switch(field - " + FIELD_OFFSET_MEMBER + "){");
                int j = 0;
                for(Map.Entry<String,String> e : this.sliced.entrySet()){
                    this.pw.print("        case " + j++ + ": return ");
                    if(this.mapValueType(e.getValue())) {
                        this.pw.print(getModelType(e.getValue()) + ".toCCI(this." + e.getKey() + ')');                    
                    } else {
                        this.pw.print("this." + e.getKey());                    
                    }
                    this.pw.println(';');
                }
                this.pw.println("        default: return super.getValue(field);");
                this.pw.println("      }");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    protected void setValue(int field, java.lang.Object value){");
                this.pw.println("      switch(field - " + FIELD_OFFSET_MEMBER + "){");
                j = 0;
                for(Map.Entry<String,String> e : this.sliced.entrySet()){
                    this.pw.println("        case " + j++ + ":");
                    this.pw.print("          this." + e.getKey() + " = ");
                    if(
                            this.model.isPrimitiveType(e.getValue()) ||
                            this.model.isStructureType(e.getValue())                        
                    ) {
                        String source = '(' + getObjectType(e.getValue()) + ")value";
                        if(this.mapValueType(e.getValue())) {
                            this.pw.println(getModelType(e.getValue()) + ".toJDO(" + source + ");");
                        } else {
                            this.pw.println(source + ';');
                        }
                    } else {
                        ClassDef classDef = getClassDef(e.getValue());
                        ClassType classType = getClassType(classDef);
                        String memberType = classType.getType(classDef, this.getFormat(), TypeMode.MEMBER);
                        if(classType == ClassType.MIXIN) {
                            this.pw.println(JDO_IDENTITY_ACCESSOR + "(value);");
                            this.pw.print("          this." + e.getKey() + INSTANCE_SUFFIX + " = ");
                        }
                        this.pw.println('(' + memberType + ")value;");
                    }
                    this.pw.println("        break;");
                }
                this.pw.println("        default: super.setValue(field, value);");
                this.pw.println("      }");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  protected " + QUALIFIED_SLICE_CLASS_NAME + " new" + SLICE_CLASS_NAME + "(");
                this.pw.println("    int index");
                this.pw.println("  ){");
                this.pw.println("    return new " + SLICE_CLASS_NAME + "(this, index);");
                this.pw.println("  }");
            }
            this.pw.println();
        }
    }

    // -----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void mapBegin(
    ) throws ServiceException {
        this.trace("Instance/Begin");
        this.fileHeader();
        this.pw.println(
            "package " + this.getNamespace(
                MapperUtils.getNameComponents(
                    MapperUtils.getPackageName(
                        this.classDef.getQualifiedName()
                    )
                )
            ) + ';'
        );
        this.pw.println();
        this.pw.println("/**");
        this.pw.print(" * ");
        this.pw.print(
            this.classDef.isAbstract() ? "Abstract class" : "Class"
        );
        this.pw.println(" <code>" + this.classDef.getName() + "</code>"); 
        if (this.classDef.getAnnotation() != null) {
            this.pw.println(" *<p>");
            this.pw.println(MapperUtils.wrapText(" * ", this.classDef.getAnnotation()));
        }
        this.pw.println(" */");
        if(getFormat() == Format.JDO2) {
            this.pw.println("@SuppressWarnings(\"serial\")");
            this.pw.print("public class " + this.className); 
            this.pw.println(
                "  extends " + (
                        isBaseClass() 
                        ? QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME 
                            : this.getType(this.extendsClassDef.getQualifiedName())
                )
            );
            this.pw.print(" implements ");
            this.pw.print(
                interfaceType(
                    this.classDef, 
                    hasSPI() ? Visibility.SPI : Visibility.CCI,
                        false
                )
            );
//          if(isBaseClass()) {
//          this.pw.print(",\n    javax.jdo.listener.StoreCallback");
//          this.pw.print(",\n    javax.jdo.listener.LoadCallback");
//          this.pw.print(",\n    javax.jdo.listener.ClearCallback");
//          }
            this.pw.println();
        } 
        else {
            this.pw.println("public interface " + this.className);
            String separator = "  extends ";
            if(getFormat() == Format.JMI1) {
                this.pw.print(
                    separator + this.interfaceType(
                        this.classDef, 
                        Visibility.CCI,
                        false
                    )
                );
                separator = ",\n    ";
            }
            if (this.classDef.getSupertypes().isEmpty()) {
                if(getFormat() == Format.JMI1) {
                    this.pw.print(separator + REF_OBJECT_INTERFACE_NAME);
                    separator = ",\n    ";
                }
            } else {
                for (
                        Iterator<ClassDef> i = this.classDef.getSupertypes().iterator(); 
                        i.hasNext(); 
                        separator = ",\n    "
                ){
                    this.pw.print(separator + this.getType(i.next().getQualifiedName()));
                }
            }
        }
        this.pw.println("{");
        this.pw.println();
        if(getFormat() == Format.JDO2) {
            if(isBaseClass()) {
                this.pw.println("  /**");
                this.pw.println("   * The the object's JDO identity key");
                this.pw.println("   */");
                this.pw.print(hasContainer() ? "   private" : "   public");
                this.pw.println("  java.lang.String " + JDO_IDENTITY_MEMBER + ';');
                this.pw.println();
                this.pw.println("  /**");
                this.pw.println("   * Retrieve the object's JDO identity key");
                this.pw.println("   * @return the value of the object's JDO identity key");
                this.pw.println("   */");
                this.pw.println("  protected java.lang.String openmdxjdoGetObjectId(");
                this.pw.println("  ){");
                this.pw.println("     return this." + JDO_IDENTITY_MEMBER + ';');
                this.pw.println("  }");
                this.pw.println();
            }
            if(isSliceHolder()) {
                this.pw.println("  /**");
                this.pw.println("   * Slice holder");
                this.pw.println("   */");
                this.pw.println(
                    "   private java.util.TreeMap<java.lang.Integer," + SLICE_CLASS_NAME + "> " + 
                    SLICES_MEMBER + " = new java.util.TreeMap<java.lang.Integer," + SLICE_CLASS_NAME + ">();"
                );
                this.pw.println();
                this.pw.println("   @SuppressWarnings(\"unchecked\")");
                this.pw.println("   protected final  <E extends " + QUALIFIED_SLICE_CLASS_NAME + "> java.util.SortedMap<java.lang.Integer,E> openmdxjdoGetSlices(");
                this.pw.println("   ){");
                this.pw.println("      return (java.util.SortedMap<java.lang.Integer,E>)this.openmdxjdoSlices;");
                this.pw.println("   }");
                this.pw.println();
            }
        }
    }

    // -----------------------------------------------------------------------
    public void mapAttributeSetStream(AttributeDef attributeDef) {
        this.trace("Instance/AttributeSetStream");
        this.pw.println();
        String newValue = getFeatureName(attributeDef);
        String modelType = attributeDef.getQualifiedTypeName();
        if (PrimitiveTypes.BINARY.equals(modelType)) {
            if(getFormat() == Format.JMI1) {
                this.pw.println("  /**");
                this.pw.println(MapperUtils
                    .wrapText(
                        "   * ",
                        "Sets a new binary value for the attribute <code>" + attributeDef.getName() + "</code>."));
                if (attributeDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
                }
                this.pw.println("   * @param " + newValue + " A <code>BinaryLargeObject</code> containing the binary value for this attribute.");
                this.pw.println("   */");
                this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
                this.pw.println("    org.w3c.cci2.BinaryLargeObject " + newValue);
                this.pw.println("  );");
                this.pw.println();
            } else {
                this.pw.println("  /**");
                this.pw.println("   * Sets a new  binary value for the attribute <code>" + attributeDef.getName() + "</code>");
                if (attributeDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
                }
                this.pw.println("   * @param " + newValue + " A <code>BinaryLargeObject</code> containing the value for this attribute.");
                this.pw.println("   */");
                this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
                this.pw.println("    org.w3c.cci2.BinaryLargeObject " + newValue);
                if(getFormat() == Format.JDO2) {
                    this.pw.println("  ){");
                    this.pw.println("    this." + newValue + " = openmdxjdoToArray(" + newValue + ");");
                    this.pw.println("  }");
                } else {
                    this.pw.println("  );");
                }
                this.pw.println();
            }
        } else if (PrimitiveTypes.STRING.equals(modelType)) {
            if(getFormat() == Format.JMI1) {
                this.pw.println("  /**");
                this.pw.println("   * Sets a new character large object value for the attribute <code>" + attributeDef.getName() + "</code>.");
                if (attributeDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
                }
                this.pw.println("   * @param " + newValue + " A <code>CharacterLargeObject</code> containing the value for this attribute.");
                this.pw.println("   */");
                this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
                this.pw.println("     org.w3c.cci2.CharacterLargeObject " + newValue);
                this.pw.println("  );");
                this.pw.println();
            } else {
                this.pw.println("  /**");
                this.pw.println("   * Sets a new character large object value for the attribute <code>" + attributeDef.getName() + "</code>");
                if (attributeDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
                }
                this.pw.println("   * @param " + newValue + " A <code>CharacterLargeObject</code> containing the value for this attribute.");
                this.pw.println("   */");
                this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
                this.pw.println("    org.w3c.cci2.CharacterLargeObject " + newValue);
                if(getFormat() == Format.JDO2) {
                    this.pw.println("  ){");
                    this.pw.println("    this." + newValue + " = openmdxjdoToArray(" + newValue + ");");
                    this.pw.println("  }");
                } else {
                    this.pw.println("  );");
                }
            }
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeSet1_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        this.trace("Instance/AttributeSet1_1");
        String attributeName = getFeatureName(attributeDef);
        String modelType = attributeDef.getQualifiedTypeName();
        String attributeType = this.getType(modelType);        
        this.pw.println("  /**");
        this.pw.println("   * Sets a new value for the attribute <code>" + attributeDef.getName() + "</code>.");
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param " + attributeName + " The non-null new value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + attributeType + ' ' + attributeName);
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");
            this.pw.print("    this." + attributeName + " = ");
            if(this.mapValueType(modelType)) {
                String source = '(' + getObjectType(modelType) + ')' + attributeName;
                this.pw.print(getModelType(modelType) + ".toJDO(" + source + ')');
            } else if (this.model.isPrimitiveType(modelType)){
                this.pw.print(attributeName);
            } else {
                String source = '(' + getObjectType(modelType) + ')' + attributeName;
                this.pw.print(source);
            }
            this.pw.println(';');
            this.pw.println("  }");
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeSet0_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        this.trace("Instance/AttributeSet0_1");
        String attributeName = getFeatureName(attributeDef);
        String modelType = attributeDef.getQualifiedTypeName();
        String attributeType = this.getObjectType(modelType);
        this.pw.println();
        this.pw.println("  /**");
        this.pw.println("   * Sets a new value for the attribute <code>" + attributeDef.getName() + "</code>.");
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param " + attributeName + " The possibly null new value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + attributeType + ' ' + attributeName);
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");            
            this.pw.print("    this." + attributeName + " = ");
            if(this.mapValueType(modelType)) {
                String source = '(' + getObjectType(modelType) + ')' + attributeName;
                this.pw.print(getModelType(modelType) + ".toJDO(" + source + ')');
            } else if (this.model.isPrimitiveType(modelType)){
                this.pw.print(attributeName);
            } else {
                String source = '(' + getObjectType(modelType) + ')' + attributeName;
                this.pw.print(source);
            }
            this.pw.println(';');
            this.pw.println("  }");
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeGetStream(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/AttributeGetStream.vm");
        this.pw.println();
        String newValue = getFeatureName(attributeDef);

        if (PrimitiveTypes.BINARY.equals(attributeDef.getQualifiedTypeName())) {
            if(getFormat() != Format.JMI1) {
                this.pw.println("  /**");
                this.pw.println("   * Retrieves a binary large object value for the attribute <code>" + attributeDef.getName() + "</code>.");
                if (attributeDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
                }
                this.pw.println("   */");
                this.pw.println("  public org.w3c.cci2.BinaryLargeObject " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
                if(getFormat() == Format.JDO2) {
                    this.pw.println("  ){");
                    this.pw.println("    return org.w3c.cci2.BinaryLargeObjects.valueOf(this." + newValue + ");");
                    this.pw.println("  }");
                    mapDeclareValue("  ", "byte[]", newValue);
                } else {
                    this.pw.println("  );");
                }
                this.pw.println();
            }
        } else if (PrimitiveTypes.STRING.equals(attributeDef.getQualifiedTypeName())) {
            if(getFormat() != Format.JMI1) {
                this.pw.println("  /**");
                this.pw.println("   * Retrieves a character large object value for the attribute <code>" + attributeDef.getName() + "</code>.");
                if (attributeDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
                }
                this.pw.println("   */");
                this.pw.println("  public org.w3c.cci2.CharacterLargeObject " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
                if(getFormat() == Format.JDO2) {
                    this.pw.println("  ){");
                    this.pw.println("    return org.w3c.cci2.CharacterLargeObjects.valueOf(this." + newValue + ");");
                    this.pw.println("  }");
                    mapDeclareValue("  ", "char[]", newValue);
                } else {
                    this.pw.println("  );");
                }
            }
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeGetSparseArray(
        AttributeDef attributeDef
    ) throws ServiceException {
        String attributeName = getFeatureName(attributeDef);
        int attributeField;
        if(getFormat() == Format.JDO2) {
            attributeField = this.sliced.size();
            this.trace("Instance/AttributeDeclarationSparseArray");
            this.pw.println();
            this.pw.println("  /**");
            this.pw.println("   * Attribute <code>" + attributeName + "</code>.");
            this.pw.println("   */");
            this.pw.println("  private transient " + getType(attributeDef, "org.w3c.cci2.SparseArray", null, TypeMode.MEMBER) + ' ' + attributeName + ';');
            this.pw.println();
            this.sliced.put(attributeName, attributeDef.getQualifiedTypeName());
        } else {
            attributeField = -1;
        }
        this.trace("Instance/AttributeGetSparseArray");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a SparseArray containing all the elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return A SparseArray containing all elements for this attribute.");
        this.pw.println("   */");
        String cast = printAnnotationAndReturnCast(attributeDef, "org.w3c.cci2.SparseArray");
        this.pw.println("  public " + getType(attributeDef, "org.w3c.cci2.SparseArray", Boolean.TRUE, TypeMode.MEMBER) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");
            this.pw.println("    return " + cast + "(this." + attributeName + " == null ?");
            this.pw.println("       this." + attributeName + " = openmdxjdoGetSparseArray(" + SLICE_CLASS_NAME + '.' + FIELD_OFFSET_MEMBER + " + " + attributeField + ") : ");
            this.pw.println("       this." + attributeName);
            this.pw.println("    );");
            this.pw.println("  }");
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------

    public void mapAttributeGetSet(
        AttributeDef attributeDef
    ) throws ServiceException {
        String attributeName = getFeatureName(attributeDef);
        int attributeField;
        Integer embedded = null;
        String embeddedSet = null;
        if(getFormat() == Format.JDO2) {
            attributeField = this.sliced.size();
            this.trace("Instance/AttributeDeclarationSet");
            this.pw.println();
            this.pw.println("  /**");
            this.pw.println("   * Attribute <code>" + attributeName + "</code>.");
            this.pw.println("   */");
            this.pw.println("  private transient " + getType(attributeDef, "java.util.Set", null, TypeMode.MEMBER) + ' ' + attributeName + ';');
            this.pw.println();
            FieldMetaData fieldMetaData = getFieldMetaData(attributeDef.getQualifiedName()); 
            if(fieldMetaData != null) {
                embedded = fieldMetaData.getEmbedded();
            }
            if(embedded == null) {
                this.sliced.put(attributeName, attributeDef.getQualifiedTypeName());
            } else {
                String fieldType = getObjectType(attributeDef.getQualifiedTypeName());
                for(
                        int i = 0;
                        i < embedded.intValue();
                        i++
                ){
                    this.pw.println("  private " + fieldType + " " + attributeName + SUFFIX_SEPARATOR + i + ";");
                }
            }
        } else {
            attributeField = -1;
        }
        this.trace("Instance/AttributeGetSet");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a set containing all the elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return A set containing all elements for this attribute.");
        this.pw.println("   */");
        String cast = printAnnotationAndReturnCast(attributeDef, "java.util.Set");
        this.pw.println("  public " + getType(attributeDef, "java.util.Set", Boolean.TRUE, TypeMode.MEMBER) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");
            this.pw.println("    return " + cast + "(this." + attributeName + " == null ?");
            this.pw.print("       this." + attributeName + " = ");
            if(embedded == null) {
                this.pw.println("openmdxjdoGetSet(" + SLICE_CLASS_NAME + '.' + FIELD_OFFSET_MEMBER + " + " + attributeField + ") :");
            } else {
                embeddedSet = Identifier.CLASS_PROXY_NAME.toIdentifier(attributeDef.getName());
                this.pw.println("new " + embeddedSet + "(" + embedded + ") :");
            }                
            this.pw.println("       this." + attributeName);
            this.pw.println("     );");
            this.pw.println("  }");
            if(embedded != null) {
                String elementType = getObjectType(attributeDef);
                this.pw.println();
                this.pw.println("  private class " + embeddedSet + " extends EmbeddedSet<" + elementType + ">{");
                this.pw.println();
                this.pw.println("    " + embeddedSet + "(int capacity){");
                this.pw.println("      super(capacity);");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    protected final " + elementType + " openmdxjdoGet(int index){");
                this.pw.println("      switch(index){");
                for(
                        int i = 0;
                        i < embedded;
                        i++
                ){
                    this.pw.println("         case " + i + ": return " + attributeName + SUFFIX_SEPARATOR + i + ";");
                }
                this.pw.println("         default: throw new IndexOutOfBoundsException(\"Index \" + index + \" is not in [0.." + (embedded - 1) + "]\");");
                this.pw.println("      }");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    protected final void openmdxjdoSet(int index, " + elementType + " element){");
                this.pw.println("      switch(index){");
                for(
                        int i = 0;
                        i < embedded;
                        i++
                ){
                    this.pw.println("         case " + i + ": " + attributeName + SUFFIX_SEPARATOR + i + " = element;");
                }
                this.pw.println("         default: throw new IndexOutOfBoundsException(\"Index \" + index + \" is not in [0.." + (embedded - 1) + "]\");");
                this.pw.println("      }");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("  }");
                this.pw.println();
            }
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------

    /**
     * 
     */
    public void mapAttributeGetMap(
        AttributeDef attributeDef
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        String attributeName = getFeatureName(attributeDef);
        if(getFormat() == Format.JDO2) {
            this.trace("Instance/AttributeDeclarationMap");
            this.pw.println();
            this.pw.println("  /**");
            this.pw.println("   * Attribute <code>" + attributeName + "</code>.");
            this.pw.println("   */");
            this.pw.println("  private transient " + this.getMapType(attributeDef, java.lang.String.class, null) + ' ' + attributeName + ';');
            this.pw.println();
        }        
        this.trace("Instance/AttributeGetMap");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a map containing all the elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return A map containing all elements for this attribute.");
        this.pw.println("   */");
        String cast = printAnnotationAndReturnMapCast(attributeDef, java.lang.String.class);
        this.pw.println("  public " + this.getMapType(attributeDef, java.lang.String.class, Boolean.TRUE) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");
            this.pw.println("    return " + cast + "this." + attributeName + ';');
            this.pw.println("  }");
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------

    public void mapAttributeSetList(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/AttributeSetList");
        String attributeName = getFeatureName(attributeDef);
        if(getFormat() == Format.JMI1) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Clears <code>" + attributeDef.getName() + "</code> and adds the members of the given List."));
            this.pw.println("   * <p>");
            this.pw.println("   * This method is equivalent to<pre>");
            this.pw.println("   *   list.clear();");
            this.pw.println("   *   list.addAll(" + attributeName + ");");
            this.pw.println("   * </pre>");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "In order to remain standard compliant you should use the equivalent code.</em>"
            ));
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param " + attributeName + " collection to be copied.");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    " + this.getType(attributeDef, "java.util.List", Boolean.FALSE, TypeMode.MEMBER) + ' ' + attributeName);
            this.pw.println("  );");
            this.pw.println();            
        } else {
            this.pw.println("  /**");
            String elementType = this.getType(attributeDef.getQualifiedTypeName(), true);
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Clears <code>" + attributeDef.getName() + "</code> and adds the given value(s)."));
            this.pw.println("   * <p>");
            this.pw.println("   * This method is equivalent to<pre>");
            this.pw.println("   *   list.clear();");
            this.pw.println("   *   for(" + elementType + " e : attributeName){");
            this.pw.println("   *     list.add(e);");
            this.pw.println("   *   }");
            this.pw.println("   * </pre>");
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param " + attributeName + " value(s) to be added to <code>" + attributeDef.getName() + "</code>");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    " + elementType + "... " + attributeName);
            if(getFormat() == Format.JDO2) {
                this.pw.println("  ){");
                this.pw.println("    openmdxjdoSetCollection(");
                this.pw.println("      " + this.getMethodName(attributeDef.getBeanGetterName()) + "(),");
                this.pw.println("      " + attributeName);
                this.pw.println("    );");
                this.pw.println("  }");
            } else {
                this.pw.println("  );");
            }
            this.pw.println();
        }
    }

    //-----------------------------------------------------------------------

    public void mapAttributeSetSet(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/AttributeSetSet");
        String attributeName = getFeatureName(attributeDef);
        if(getFormat() == Format.JMI1) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Clears <code>" + attributeDef.getName() + "</code> and adds the members of the given Set."));
            this.pw.println("   * <p>");
            this.pw.println("   * This method is equivalent to<pre>");
            this.pw.println("   *   set.clear();");
            this.pw.println("   *   set.addAll(" + attributeName + ");");
            this.pw.println("   * </pre>");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "In order to remain standard compliant you should use the equivalent code.</em>"
            ));
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param " + attributeName + " collection to be copied.");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    " + this.getType(attributeDef, "java.util.Set", Boolean.FALSE, TypeMode.MEMBER) + ' ' + attributeName);
            this.pw.println("  );");
            this.pw.println();
        } else {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Clears <code>" + attributeDef.getName() + "</code> and adds the given value(s)."));
            this.pw.println("   * <p>");
            this.pw.println("   * This method is equivalent to<pre>");
            this.pw.println("   *   set.clear();");
            this.pw.println("   *   set.addAll(Arrays.asList(" + attributeName + "));");
            this.pw.println("   * </pre>");
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param " + attributeName + " value(s) to be added to <code>" + attributeDef.getName() + "</code>");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    " + this.getType(attributeDef, null, Boolean.FALSE, TypeMode.MEMBER) + "... " + attributeName);
            if(getFormat() == Format.JDO2) {
                this.pw.println("  ){");
                this.pw.println("    openmdxjdoSetCollection(");
                this.pw.println("      " + this.getMethodName(attributeDef.getBeanGetterName()) + "(),");
                this.pw.println("      " + attributeName);
                this.pw.println("    );");
                this.pw.println("  }");
            } else {
                this.pw.println("  );");
            }
            this.pw.println();
        }
    }

    //-----------------------------------------------------------------------

    public void mapAttributeSetSparseArray(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/AttributeSetSparseArray");
        String attributeName = getFeatureName(attributeDef);
        if(getFormat() != Format.JMI1) {
            this.pw.println("  /**");
            this.pw.println(MapperUtils
                .wrapText(
                    "   * ",
                    "Clears <code>" + attributeDef.getName() + "</code> and adds the given value(s)."));
            this.pw.println("   * <p>");
            this.pw.println("   * This method is equivalent to<pre>");
            this.pw.println("   *   array.clear();");
            this.pw.println("   *   array.putAll(" + attributeName + ");");
            this.pw.println("   * </pre>");
            if (attributeDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
            }
            this.pw.println("   * @param " + attributeName + " value(s) to be added to <code>" + attributeDef.getName() + "</code>");
            this.pw.println("   */");
            this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
            this.pw.println("    " + this.getMapType(attributeDef, Integer.class, Boolean.FALSE) + ' ' + attributeName);
            if(getFormat() == Format.JDO2) {
                this.pw.println("  ){");
                this.pw.println("    openmdxjdoSetArray(");
                this.pw.println("      " + this.getMethodName(attributeDef.getBeanGetterName()) + "(),");
                this.pw.println("      " + attributeName);
                this.pw.println("    );");
                this.pw.println("  }");
            } else {
                this.pw.println("  );");
            }
            this.pw.println();
        }
    }

    //-----------------------------------------------------------------------

    public void mapReferenceSetWithQualifier(
        ReferenceDef referenceDef)
    throws ServiceException {
        this.trace("Instance/ReferenceSetWithQualifier");
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Sets a list containing all the new elements for the reference <code>" + referenceDef.getName() + "</code>."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue A list containing all the new elements for this reference.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(referenceDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef, "org.w3c.cci2.SparseArray", Boolean.TRUE, TypeMode.MEMBER) + " newValue");
        this.pw.println("  );");
        this.pw.println();
        this.pw.println("  /**");
        this.pw.println(MapperUtils.wrapText(
            "   * ",
            "Sets an array containing all the new elements for the reference <code>" + referenceDef.getName() + "</code>."));
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        this.pw.println("   * @param newValue An array containing all the new elements for this reference.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(referenceDef.getBeanSetterName()) + "(");
        this.pw.println("    " + this.getType(referenceDef.getQualifiedTypeName()) + "[] newValue");
        this.pw.println("  );");
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeGetList(
        AttributeDef attributeDef
    ) throws ServiceException {
        String attributeName = getFeatureName(attributeDef);
        Integer embedded = null;
        String embeddedList = null;
        int attributeField;
        if(getFormat() == Format.JDO2) {
            attributeField = this.sliced.size();
            this.trace("Instance/AttributeDeclarationList");
            this.pw.println();
            this.pw.println("  /**");
            this.pw.println("   * Attribute <code>" + attributeName + "</code>.");
            this.pw.println("   */");
            this.pw.println("  private transient " + this.getType(attributeDef, "java.util.List", null, TypeMode.MEMBER) + ' ' + attributeName + ';');
            FieldMetaData fieldMetaData = getFieldMetaData(attributeDef.getQualifiedName()); 
            if(fieldMetaData != null) {
                embedded = fieldMetaData.getEmbedded();
            }
            if(embedded == null) {
                this.sliced.put(attributeName, attributeDef.getQualifiedTypeName());
            } else {
                String fieldType = getObjectType(attributeDef.getQualifiedTypeName());
                for(
                        int i = 0;
                        i < embedded.intValue();
                        i++
                ){
                    this.pw.println("  private " + fieldType + " " + attributeName + SUFFIX_SEPARATOR + i + ";");
                }
            }
            this.pw.println();
        } else {
            attributeField = -1;
        }
        this.trace("Instance/AttributeGetList");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a list containing all the elements for the attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return A list containing all elements for this attribute.");
        this.pw.println("   */");
        String cast = printAnnotationAndReturnCast(attributeDef, "java.util.List");
        this.pw.println("  public " + this.getType(attributeDef, "java.util.List", Boolean.TRUE, TypeMode.MEMBER) + ' ' + this.getMethodName(attributeDef.getBeanGetterName()) + '(');
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");
            this.pw.println("    return " + cast + "(this." + attributeName + " == null ?");
            this.pw.print("       this." + attributeName + " = ");
            if(embedded == null) {
                this.pw.println("openmdxjdoGetList(" + SLICE_CLASS_NAME + '.' + FIELD_OFFSET_MEMBER + " + " + attributeField + ") :");
            } else {
                embeddedList = Identifier.CLASS_PROXY_NAME.toIdentifier(attributeDef.getName());
                this.pw.println("new " + embeddedList + "(" + embedded + ") :");
            }                
            this.pw.println("       this." + attributeName);
            this.pw.println("     );");
            this.pw.println("  }");
            if(embedded != null) {
                String elementType = getObjectType(attributeDef);
                this.pw.println();
                this.pw.println("  private class " + embeddedList + " extends EmbeddedList<" + elementType + ">{");
                this.pw.println();
                this.pw.println("    " + embeddedList + "(int capacity){");
                this.pw.println("      super(capacity);");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    protected final " + elementType + " openmdxjdoGet(int index){");
                this.pw.println("      switch(index){");
                for(
                        int i = 0;
                        i < embedded;
                        i++
                ){
                    this.pw.println("         case " + i + ": return " + attributeName + SUFFIX_SEPARATOR + i + ";");
                }
                this.pw.println("         default: throw new IndexOutOfBoundsException(\"Index \" + index + \" is not in [0.." +  (embedded - 1)  + "]\");");
                this.pw.println("      }");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("    protected final void openmdxjdoSet(int index, " + elementType + " element){");
                this.pw.println("      switch(index){");
                for(
                        int i = 0;
                        i < embedded;
                        i++
                ){
                    this.pw.println("         case " + i + ": " + attributeName + SUFFIX_SEPARATOR + i + " = element;");
                }
                this.pw.println("         default: throw new IndexOutOfBoundsException(\"Index \" + index + \" is not in [0.." +  (embedded - 1)  + "]\");");
                this.pw.println("      }");
                this.pw.println("    }");
                this.pw.println();
                this.pw.println("  }");
                this.pw.println();
            }
        } 
        else {
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeGet1_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        String attributeName = getFeatureName(attributeDef);
        boolean objectIdentity = SystemAttributes.OBJECT_IDENTITY.equals(attributeName);
        String featureType = objectIdentity ?
            QUALIFIED_IDENTITY_FEATURE_CLASS_NAME :
                this.getFeatureType(attributeDef, Boolean.TRUE);        
        String modelType = attributeDef.getQualifiedTypeName();
        if(getFormat() == Format.JDO2) {
            mapDeclareValue("  ", this.getValueType(modelType, false), attributeName);
        }
        this.trace("Instance/AttributeGet1_1");
        this.pw.println("  /**");
        this.pw.println("   * Retrieves the value for the attribute <code>" + attributeDef.getName() + "</code>.");
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return The non-null value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   */");
        String cast =  printAnnotationAndReturnCast(attributeDef, null);
        this.pw.println("  public " + featureType + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");
            this.pw.print("    return ");
            if(this.mapValueType(modelType)) {
                this.pw.print(getModelType(modelType) + ".toCCI(this." + attributeName + ')');                    
            } else {
                this.pw.print(cast + "this." + attributeName);                    
            }
            this.pw.println(';');
            this.pw.println("  }");
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeGet0_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        String attributeName = getFeatureName(attributeDef);
        String modelType = attributeDef.getQualifiedTypeName();
        String featureType = this.getType(attributeDef, null, Boolean.TRUE, TypeMode.MEMBER);        
        if(getFormat() == Format.JDO2) {
            mapDeclareValue("  ", this.getValueType(modelType, true), attributeName);
        }
        this.trace("Instance/AttributeGet0_1");
        this.pw.println("  /**");
        this.pw.println(MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the possibly null value for the optional attribute <code>" + attributeDef.getName() + "</code>."));
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @return The possibly null value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   */");
        String cast =  printAnnotationAndReturnCast(attributeDef, null);
        this.pw.println("  public " + featureType + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
        if(getFormat() == Format.JDO2) {
            this.pw.println("  ){");
            this.pw.print("    return ");
            if(this.mapValueType(modelType)) {
                this.pw.print(getModelType(modelType) + ".toCCI(this." + attributeName + ')');                    
            } else {
                this.pw.print(cast + "this." + attributeName);                    
            }
            this.pw.println(';');
            this.pw.println("  }");
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    protected String getValueType(
        String qualifiedTypeName, 
        boolean optional
    ) throws ServiceException{
        return
        PrimitiveTypes.DATE.equals(qualifiedTypeName) ? "java.sql.Date" :
            PrimitiveTypes.DATETIME.equals(qualifiedTypeName) ? "java.sql.Timestamp" :
                optional ? getObjectType(qualifiedTypeName) :
                    getType(qualifiedTypeName);
    }

    //-----------------------------------------------------------------------
    protected boolean mapValueType(
        String qualifiedTypeName
    ) {
        return 
        PrimitiveTypes.DATE.equals(qualifiedTypeName) ||
        PrimitiveTypes.DATETIME.equals(qualifiedTypeName);
    }

    //-----------------------------------------------------------------------
    protected void mapDeclareValue(
        String indentation,
        String attributeType, 
        String attributeName
    ) throws ServiceException{
        this.trace("Instance/DeclareValue");
        this.pw.println();
        this.pw.println(indentation + "/**");
        this.pw.println(indentation + " * Attribute <code>" + attributeName + "</code>.");
        this.pw.println(indentation + " */");
        this.pw.println(indentation + "private " + attributeType + ' ' + attributeName + ';');
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    protected void mapDeclareReference(
        String indentation,
        String qualifiedTypeName, 
        String referenceName
    ) throws ServiceException{
        this.trace("Instance/ReferenceDeclaration");
        ClassDef classDef = getClassDef(qualifiedTypeName);
        ClassType classType = getClassType(classDef);
        String referenceType = classType.getType(classDef, this.getFormat(), TypeMode.MEMBER);
        if(classType == ClassType.MIXIN){
            this.pw.println("  /**");
            this.pw.println("   * JDO Object id of the instance referenced by <code>" + referenceName + "</code>.");
            this.pw.println("   */");
            this.pw.println("   @SuppressWarnings(\"unused\")");            
            this.pw.println("  private java.lang.String " + referenceName + ';');
            this.pw.println();
            this.pw.println("  /**");
            this.pw.println("   * Instance referenced by <code>" + referenceName + "</code>.");
            this.pw.println("   */");
            this.pw.println("   @SuppressWarnings(\"unused\")");            
            this.pw.println("  private transient " + referenceType + ' ' + referenceName + INSTANCE_SUFFIX + ';');
        } else {
            this.pw.println("  /**");
            this.pw.println("   * Instance referenced by <code>" + referenceName + "</code>.");
            this.pw.println("   */");
            this.pw.println("   @SuppressWarnings(\"unused\")");            
            this.pw.println("  private " + referenceType + ' ' + referenceName + ';');
        }
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    protected void mapDeclareSize(
        String indentation,
        String attributeName
    ) throws ServiceException{
        this.trace("Instance/DeclareSize");
        this.pw.println();
        this.pw.println(indentation + "/**");
        this.pw.println(indentation + " * Number of elements of attribute <code>" + attributeName + "</code>");
        this.pw.println(indentation + " */");
        this.pw.println(indentation + "private int " + attributeName + SIZE_SUFFIX + ';');
        this.pw.println();
    }

    //-----------------------------------------------------------------------
    /**
     * Retrieve not inherited features
     * @param inherited
     *
     * @return Returns the implementsClassDef.
     */
    protected Map<String,ModelElement_1_0> getFeatures(boolean inherited) {
        return inherited ? this.superFeatures : this.localFeatures;
    }    

    //-----------------------------------------------------------------------
    private final Map<String,ModelElement_1_0> superFeatures;
    private final Map<String,ModelElement_1_0> localFeatures;
    private final Map<String,String> sliced = new LinkedHashMap<String,String>();   

    static final String OPENMDX_JDO_PREFIX = "openmdxjdo";
    static final String SLICE_CLASS_NAME = "Slice";
    static final String SLICE_ID_CLASS_NAME = "ObjectId";
    static final String OBJECT_IDENTITY_CLASS_NAME = "Identity";
    static final String BASE_CLASS_MEMBER = "BASE_CLASS";
    static final String CLASS_MEMBER = "CLASS";
    static final String CLASS_TYPE = "java.util.List<java.lang.String>";
    static final String CLASS_ACCESSOR = OPENMDX_JDO_PREFIX + "GetClass";
    static final String FIELD_OFFSET_MEMBER = OPENMDX_JDO_PREFIX + "FieldOffset";
    static final String FIELD_COUNT_ACCESSOR = OPENMDX_JDO_PREFIX + "FieldCount";
    static final String JDO_IDENTITY_MEMBER = OPENMDX_JDO_PREFIX + "Identity";
    static final String JDO_IDENTITY_ACCESSOR = "org.oasisopen.jdo2.Identifiable.openmdxjdoGetObjectId";
    static final String TO_IDENTITY_METHOD = "org.oasisopen.jdo2.Identifiable.openmdxjdoToIdentity";
    static final String XRI_IDENTITY_ACCESSOR = "getIdentity";
    static final String QUALIFIER_MEMBER = OPENMDX_JDO_PREFIX + "Qualifier";
    static final String IDENTITY_HANDLER_MEMBER = OPENMDX_JDO_PREFIX + "IdentityHandler";
    static final String INDEX_MEMBER = OPENMDX_JDO_PREFIX + "Index";
    static final String SLICES_MEMBER = OPENMDX_JDO_PREFIX + "Slices";
    static final String QUALIFIED_IDENTITY_FEATURE_CLASS_NAME = "java.lang.String";
    static final String QUALIFIED_IDENTITY_CLASS_NAME = "org.oasisopen.cci2." + OBJECT_IDENTITY_CLASS_NAME;
    static final String QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME = "org.w3c.jdo2.AbstractObject";
    static final String QUALIFIED_SLICE_CLASS_NAME = QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".Slice";
    static final String QUALIFIED_ABSTRACT_OBJECT_IDENTITY_CLASS_NAME = "org.oasisopen.jdo2.Abstract" + OBJECT_IDENTITY_CLASS_NAME;
    static final String QUALIFIED_ABSTRACT_SLICE_ID_CLASS_NAME = QUALIFIED_SLICE_CLASS_NAME + ".Abstract" + SLICE_ID_CLASS_NAME;
    static final String QUALIFIED_OBJECT_ID_CLASS_NAME = "org.oasisopen.spi2.ObjectId";
    static final String SUFFIX_SEPARATOR = "_";
    static final String SIZE_SUFFIX = SUFFIX_SEPARATOR + "size";
    static final String QUALIFIER_TYPE_WORD = "type";
    static final String QUALIFIER_TYPE_SUFFIX = "Type";
    static final String QUALIFIER_TYPE_CLASS_NAME = "org.oasisopen.cci2.QualifierType";
    static final String PERSISTENCY_SUFFIX = "IsPersistent";
    static final String INSTANCE_SUFFIX = SUFFIX_SEPARATOR + "instance";
    static final String UNORDERED_ASSOCIATION_TYPE = "java.util.Collection";
    static final String REF_OBJECT_INTERFACE_NAME = "org.openmdx.base.accessor.jmi.cci.RefObject_1_0"; 
}

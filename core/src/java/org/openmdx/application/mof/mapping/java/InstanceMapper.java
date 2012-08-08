/*
 * ====================================================================
 * Name:        $Id: InstanceMapper.java,v 1.17 2011/01/12 17:07:09 hburger Exp $
 * Description: Instance Mapper 
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/01/12 17:07:09 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2011, OMEX AG, Switzerland
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
package org.openmdx.application.mof.mapping.java;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.omg.mof.spi.AbstractNames;
import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.ExceptionDef;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.OperationDef;
import org.openmdx.application.mof.mapping.cci.ReferenceDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.metadata.ClassMetaData;
import org.openmdx.application.mof.mapping.java.metadata.FieldMetaData;
import org.openmdx.application.mof.mapping.java.metadata.Visibility;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.kernel.exception.BasicException;

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
        Writer writerJdoSlice,
        Model_1_0 model,
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
        this.pwSlice = writerJdoSlice == null ? null : new PrintWriter(writerJdoSlice);
        this.localFeatures = new HashMap<String,ModelElement_1_0>(
            (Map<String,ModelElement_1_0>)classDef.objGetValue("allFeature")
        );
        if(isBaseClass()) {            
            this.superFeatures = Collections.emptyMap();
        } 
        else {
            this.superFeatures = (Map<String,ModelElement_1_0>)model.getElement(
                this.extendsClassDef.getQualifiedName()
            ).objGetValue("allFeature"); 
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
            if(format == Format.JMI1) {
                this.trace("Instance/ReferenceAddWithQualifier");
                ClassDef classDef = getClassDef(referenceDef.getQualifiedTypeName());
                ClassType classType = getClassType(classDef);
                String referenceType = classType.getType(classDef, format, TypeMode.PARAMETER);
                String valueHolder = Identifier.ATTRIBUTE_NAME.toIdentifier(referenceDef.getName());
                String qualifierName = Identifier.ATTRIBUTE_NAME.toIdentifier(referenceDef.getQualifierName());
                if(valueHolder.equals(qualifierName)) valueHolder = '_' + valueHolder;
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
                if(referenceDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                }
                this.pw.println("   * @param " + qualifierName + PERSISTENCY_SUFFIX + " <code>true</code> if <code>" + qualifierName + "</code> is persistent");
                this.pw.println("   * @param " + qualifierName + " The qualifier attribute value that qualifies the reference to get the element to be appended.");
                this.pw.println("   * @param " + valueHolder + " The element to be appended.");
                this.pw.println("   */");
                this.pw.println("  public void " + this.getMethodName("add" + referenceDef.getBeanGenericName()) + " (");
                this.pw.println("    boolean " + qualifierName + PERSISTENCY_SUFFIX + ",");            
                this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + qualifierName + ",");
                this.pw.println("    " + referenceType + " " + valueHolder);        
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
                if(referenceDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                }
                this.pw.println("   * @param " + qualifierName + " The qualifier attribute value that qualifies the reference to get the element to be appended.");
                this.pw.println("   * @param " + valueHolder + " The element to be appended.");
                this.pw.println("   */");
                this.pw.println("  public void " + this.getMethodName("add" + referenceDef.getBeanGenericName()) + " (");
                this.pw.println("    " + this.getType(referenceDef.getQualifiedQualifierTypeName()) + " " + qualifierName + ",");
                this.pw.println("    " + referenceType + " " + valueHolder);        
                this.pw.println("  );");
                this.pw.println();
                this.pw.println("  /**");
                this.pw.println(MapperUtils.wrapText(
                    "   * ",
                    "Adds the specified element to the set of the values for the reference " +
                    "<code>" + referenceDef.getName() + "</code> using an implementation-specific, reassignable qualifier."
                ));
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText(
                    "   * ",
                    "<em>Note: This is an extension to the JMI 1 standard.</em>"
                ));
                if(referenceDef.getAnnotation() != null) {
                    this.pw.println("   * <p>");
                    this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
                }
                this.pw.println("   * @param " + valueHolder + " The element to be appended.");
                this.pw.println("   */");
                this.pw.println("  public void " + this.getMethodName("add" + referenceDef.getBeanGenericName()) + " (");
                this.pw.println("    " + referenceType + " " + valueHolder);        
                this.pw.println("  );");
                this.pw.println();
            }
        }
    }

    // -----------------------------------------------------------------------
    public void mapReferenceRemoveOptional(
        ReferenceDef referenceDef) throws ServiceException {
    }

    // -----------------------------------------------------------------------
    public void mapReferenceRemoveWithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
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
                org.openmdx.application.mof.mapping.java.metadata.Visibility.CCI,
                false
            );
        } 
        else {
            referenceName = getFeatureName(referenceDef.getExposedEndName());
            qualifiedTypeName = referenceDef.getExposedEndQualifiedTypeName();
            beanSetterName = Identifier.OPERATION_NAME.toIdentifier(
                referenceName,
                null, // removablePrefix
                "set", // prependablePrefix
                null, // removableSuffix
                null // appendableSuffix
            );
            ClassDef classDef = getClassDef(qualifiedTypeName);
            argumentType = getClassType(classDef).getType(classDef, this.getFormat(), TypeMode.MEMBER);
        }
        this.trace("Instance/ReferenceSetNoQualifier");
        this.pw.println("  /**");
        this.pw.println("   * Sets a new value for the reference <code>" + referenceName + "</code>.");
        if (referenceDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
        }
        String methodName = this.getMethodName(beanSetterName);
        this.pw.println("   * @param " + referenceName + " The non-null new value for this reference.");
        this.pw.println("   */");
        this.pw.println("  public void " + methodName + "(");
        this.pw.println("    " + argumentType + ' ' + referenceName);
        if(getFormat() == Format.JPA3) {
            this.pw.println("  ){");                
            this.pw.println("    throw new javax.jdo.JDOFatalUserException(");
            this.pw.println("      \"Typed set not handled by data object\",");
            this.pw.println("      new UnsupportedOperationException(\"Use " + methodName + InstanceMapper.ID_SUFFIX + "() instead.\"),");
            this.pw.println("      this");
            this.pw.println("    );");            
            this.pw.println("  }");
            this.pw.println();
            this.pw.println("  public void " + methodName + InstanceMapper.ID_SUFFIX + '(');
            this.pw.println("    java.lang.String " + referenceName);            
            this.pw.println("  ) {");
            this.pw.println("    super.openmdxjdoMakeDirty();");            
            this.pw.println("    this." + referenceName + " = " + referenceName + ";");                            
            this.pw.println("  }");            
        } 
        else {
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
        if(getFormat() == Format.JPA3) {
            mapDeclareReference("  ", qualifiedTypeName, referenceName, referencedEnd);
        }
        if(referencedEnd) {
            this.trace("Instance/ReferenceGetx_1NoQualifier");
            this.pw.println("  /**");
            this.pw.println("   * Retrieves the value for the reference <code>" + referenceName + "</code>.");
            if (referenceDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
            }
            this.pw.print("   * @return The ");
            this.pw.print(optional ? "&ndash; possibly <code>null</code> &ndash;" : "non-<code>null</code>");
            this.pw.println(" value for this reference.");
            this.pw.println("   */");
            String accessorType = classType.getType(classDef, this.getFormat(), TypeMode.RESULT); 
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            this.pw.println("  public " + accessorType + ' ' + methodName + '(');
            if(getFormat() == Format.JPA3) {
                this.pw.println("  ) {");
                this.pw.println("    throw new javax.jdo.JDOFatalUserException(");
                this.pw.println("      \"This signature is not handled by data object\",");
                this.pw.println("      new UnsupportedOperationException(\"This signature is not handled by data object. Use " + methodName + InstanceMapper.ID_SUFFIX + "().\"),");
                this.pw.println("      this");
                this.pw.println("    );");                                    
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  public java.lang.String " + methodName + InstanceMapper.ID_SUFFIX + '(');
                this.pw.println("  ) {");
                this.pw.println("    return this." + referenceName + ";");                            
                this.pw.println("  }");
            } 
            else {
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
            "Retrieves the members of the given container referencing this object via <code>" + referenceDef.getName() + "</code>."));
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
        this.pw.println("   * @param " + referenceDef.getQualifierName() + " The container of the objects to be retrieved.");
        this.pw.println("   * @return The members referencing ths object via <code>" + referenceDef.getName() + "</code>.");
        this.pw.println("   */");
        this.pw.println("  public " + this.getType(referenceDef, "java.util.Collection", Boolean.TRUE, TypeMode.MEMBER) + " " + this.getMethodName(referenceDef.getBeanGetterName()) + "(");
        this.pw.println("    " + getType(referenceDef.getQualifiedQualifierTypeName(), true) + " " + referenceDef.getQualifierName());
        if(getFormat() == Format.JPA3) {
            this.pw.println("  ){");
            this.pw.println("    throw new java.lang.UnsupportedOperationException(\"Not yet implemented\");"); // TODO
            this.pw.println("  }");
        } else {
            this.pw.println("  );");
        }
        this.pw.println();
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
        ClassMetaData classMetaData;

        if(getFormat() == Format.JPA3) {
            classMetaData = (ClassMetaData) getClassDef(referenceDef.getQualifiedTypeName()).getClassMetaData();
            if(classMetaData.isRequiresExtent()) {
                if (referenceDef.isComposition()) {
                    // No member for composites. Composites are retrieved by query
                }
                else {
                    this.trace("Instance/ReferenceDeclaration");
                    this.pw.println();
                    this.pw.println("  /**");
                    this.pw.println("   * Reference <code>" + referenceName + "</code>.");
                    this.pw.println("   */");
                    this.pw.println("  @SuppressWarnings(\"unused\")");                    
                    this.pw.print("  private transient ");
                    this.pw.println(getType(referenceDef, "java.util.Set", null, TypeMode.MEMBER) + ' ' + referenceName + ';');
                    this.pw.println();
                }
            }
        } 
        else {
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
        } 
        else {
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
            if(getFormat() == Format.JPA3) {
                this.pw.println("  ){");
                if(referenceDef.isComposition()) {
                    this.pw.println("    throw new javax.jdo.JDOFatalUserException(");
                    this.pw.println("      \"This signature is not handled by data object\",");
                    this.pw.println("      new UnsupportedOperationException(\"This signature is not handled by data object. Use query on composites.\"),");
                    this.pw.println("      this");
                    this.pw.println("    );");                                                        
                } 
                else if (referenceDef.isShared()) {
                    this.pw.println("    throw new javax.jdo.JDOFatalUserException(");
                    this.pw.println("      \"This signature is not handled by data object\",");
                    this.pw.println("      new UnsupportedOperationException(\"This signature is not handled by data object. Use query on member '" + referenceName + "'.\"),");
                    this.pw.println("      this");
                    this.pw.println("    );");                                                                            
                } 
                else {
                    this.pw.println(
                        "    " + collectionType + "<?> " + referenceName + " = this." + referenceName + ';'
                    );
                    this.pw.println("    return " + cast + referenceName + ';');
                }                
                this.pw.println("  }");
            } 
            else {
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
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            if(getFormat() == Format.JPA3 && !delegate) {
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
            this.pw.println("  public " + this.getType(referenceDef, collectionType, Boolean.TRUE, TypeMode.MEMBER) + " " + methodName + "(");
            if(getFormat() == Format.JPA3) {
                this.pw.println("  ){");
                this.pw.println("    throw new javax.jdo.JDOFatalUserException(");
                this.pw.println("      \"This signature is not handled by data object\",");
                this.pw.println("      new UnsupportedOperationException(\"This signature is not handled by data object. Use " + methodName + InstanceMapper.ID_SUFFIX + "().\"),");
                this.pw.println("      this");
                this.pw.println("    );");                                                                                            
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  public " + collectionType + "<java.lang.String> " + methodName + InstanceMapper.ID_SUFFIX + "(");
                this.pw.println("  ){");
                if(delegate) {
                    this.pw.println("    return super." + methodName + InstanceMapper.ID_SUFFIX + "();");
                } else {
                    this.pw.println("    java.util.SortedMap<java.lang.Integer," + this.className + SLICE_CLASS_NAME + "> slices = openmdxjdoGetSlices();");
                    this.pw.print("    return ");
                    mapSlicedClass("      ", referenceDef, QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".Sliced" + (qualifierType == null ? "Set" : "List"));
                    this.pw.println("    };");
                    
                }
                this.pw.println("  }");
            } 
            else {
                this.pw.println("  );");
            }
            this.pw.println();
        } 
        else if(PrimitiveTypes.STRING == qualifierType) {
            ClassDef classDef = getClassDef(referenceDef.getQualifiedTypeName());
            String referenceType = getClassType(classDef).getType(classDef, getFormat(), TypeMode.INTERFACE);
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            if(getFormat() == Format.JPA3 && !delegate) {
                this.trace("Instance/ReferenceDeclarationMap");
                this.pw.println();
                this.pw.println("  /**");
                this.pw.println("   * Reference <code>" + referenceName + "</code> as <code>java.util.Map</code>");
                this.pw.println("   */");
                this.pw.println("  @SuppressWarnings(\"unused\")");                    
                this.pw.println("  private transient java.util.Map<java.lang.String," + referenceType + "> " + referenceName + ';');
                this.pw.println();
                this.sliced.put(referenceName, referenceDef.getQualifiedTypeName());                
            }
            this.trace("Instance/ReferenceGetMap");
            this.pw.println("  /**");
            this.pw.println(MapperUtils.wrapText(
                "   * ",
                "Retrieves the <code>Map</code> of objects referenced by <code>" + referenceDef.getName() + "</code>."));
            if (referenceDef.getAnnotation() != null) {
                this.pw.println("   * <p>");
                this.pw.println(MapperUtils.wrapText("   * ", referenceDef.getAnnotation()));
            }
            this.pw.println("   * @return The <code>Map</code> of referenced objects.");
            this.pw.println("   */");
            this.pw.println("  public " + this.getMapType(referenceDef, java.lang.String.class, Boolean.TRUE) + " " + methodName + "(");
            if(getFormat() == Format.JPA3) {
                this.pw.println("  ){");
                this.pw.println("    throw new javax.jdo.JDOFatalUserException(");
                this.pw.println("      \"References of type map not handled by data object\",");
                this.pw.println("      new UnsupportedOperationException(\"References of type map not handled by data object. Use " + methodName + InstanceMapper.ID_SUFFIX + "().\"),");
                this.pw.println("      this");
                this.pw.println("    );");                                                                                                            
                this.pw.println("  }");
            } 
            else {
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
                if(format == Format.JPA3) {
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
            if(getFormat() == Format.JPA3) {
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
        if(getFormat() == Format.JPA3) { 
            this.pw.println(" ){");
            this.pw.println("    throw new javax.jdo.JDOFatalUserException(");
            this.pw.println("      \"Behavioural features not handled by data object\",");
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
        this.trace("Instance/End");
        this.pw.println();
        if(getFormat() == Format.JPA3) { 
            this.mapSingleValuedFields();
            this.mapMultivaluedFields();
        }
        if(this.isBaseClass()) {
            if(this.hasContainer()) {
                if(!this.localFeatures.containsKey(this.directCompositeReference.getExposedEndName())){
                    this.mapReferenceGetx_1NoQualifier(
                        this.directCompositeReference, 
                        false, // optional
                        false // referencedEnd
                    );
                }
                if(this.getFormat() == Format.JPA3) {        
                    this.mapReferenceSetNoQualifier(
                        this.directCompositeReference, 
                        false // referencedEnd
                    );
                }
                this.mapContainment(); 
            } 
            else if(this.isAuthority()){
                this.mapAuthority();
            }
        } 
        else {
            // nothing to do
        }
        this.pw.println("}");
    }

    // -----------------------------------------------------------------------
    private void mapAuthority(
    ) throws ServiceException {
        switch(getFormat()) {
            case JPA3:
                break;
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
        String qualifiedQualifierType = this.directCompositeReference.getQualifiedQualifierTypeName();
        String qualifierArgumentType = getType(qualifiedQualifierType);
        String qualifierValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(qualifierName);
        String objectValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(objectName);
        if(objectValueName.equals(qualifierValueName)) {
            objectValueName = '_' + objectValueName;
        }
        switch(getFormat()) {
            case JPA3:
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
    boolean hasSlices(
    ){
        return 
            this.extendsClassDef != null ||
            (( this.classMetaData.getBaseClass() != null) && (this.classMetaData.getBaseClass() != null)) ||
            !this.sliced.isEmpty();
    }

    // -----------------------------------------------------------------------
    public void mapSingleValuedFields(
    ) throws ServiceException{
        if(this.hasSlices()) {
            for(Map.Entry<String,String> e : this.sliced.entrySet()){
                this.mapDeclareSize("    ", e.getKey());
            }
            this.pw.println();
        }
    }

    // -----------------------------------------------------------------------
    public void mapMultivaluedFields(
    ) throws ServiceException{
        if(this.isSliceHolder() || this.hasSlices()) {
            this.trace("Instance/Begin");
            this.fileHeader();
            this.pwSlice.println(
                "package " + this.getNamespace(
                    MapperUtils.getNameComponents(
                        MapperUtils.getPackageName(
                            this.classDef.getQualifiedName()
                        )
                    )
                ) + ';'
            );
            this.pwSlice.println();
            this.pwSlice.println("/**");
            this.pwSlice.println(MapperUtils.wrapText(
                " * ",
                "<code>" + this.className + SLICE_CLASS_NAME + "</code> object hold the <code>" + 
                classDef.getName() + "</code>'s multivalued attributes"
            ));
            this.pwSlice.println(" */");
            String superClassName = this.isSliceHolder() || this.extendsClassDef == null ? (
                this.classMetaData != null && this.classMetaData.getBaseClass() != null ? this.classMetaData.getBaseClass() + SLICE_CLASS_NAME : null 
            ) : this.getType(this.extendsClassDef.getQualifiedName()) + SLICE_CLASS_NAME;
            this.pwSlice.println("@SuppressWarnings(\"serial\")");            
            this.pwSlice.print("public class " + this.className + SLICE_CLASS_NAME + " ");
            this.pwSlice.println(superClassName == null ? "implements java.io.Serializable {" : "extends " + superClassName + " {");
            this.pwSlice.println();
            for(Map.Entry<String,String> e : this.sliced.entrySet()){
                String qualifiedName = e.getValue();
                if(
                    this.model.isPrimitiveType(qualifiedName) ||
                    this.model.isStructureType(qualifiedName)
                ) {
                    String typeName = getValueType(qualifiedName, true);
                    this.mapDeclareValue(
                        this.pwSlice, 
                        "  ", 
                        typeName, 
                        e.getKey(),
                        null, // visibility
                        true
                    );
                } 
                else {
                    this.mapDeclareReference(
                        this.pwSlice, 
                        "  ", 
                        qualifiedName, 
                        e.getKey(), 
                        false, // unused
                        true // accessors
                    );
                }
            }
            this.pwSlice.println();
            this.pwSlice.println("  /**");
            this.pwSlice.println("   * Constructor");
            this.pwSlice.println("   */");
            this.pwSlice.println("  public " + this.className + SLICE_CLASS_NAME + '('); 
            this.pwSlice.println("  ){");
            this.pwSlice.println("    // Implements Serializable");
            this.pwSlice.println("  }");
            this.pwSlice.println();
            if(
                (this.isSliceHolder() || (this.extendsClassDef == null)) &&
                (this.classMetaData.getBaseClass() == null)
            ) {
                this.pwSlice.println("  /**");
                this.pwSlice.println("   * Constructor");
                this.pwSlice.println("   */");
                this.pwSlice.println("  protected " + this.className + SLICE_CLASS_NAME + '(');
                this.pwSlice.println("    " + this.className + " object,");
                this.pwSlice.println("    int index");
                this.pwSlice.println("  ){");
                this.pwSlice.println("    this." + JDO_IDENTITY_MEMBER + " = object;");
                this.pwSlice.println("    this." + INDEX_MEMBER + " = index;");
                this.pwSlice.println("  }");
                this.pwSlice.println();
                this.pwSlice.println("  /**");
                this.pwSlice.println("   * The slice's index");
                this.pwSlice.println("   */");
                this.pwSlice.println("  @SuppressWarnings(\"unused\")");
                this.pwSlice.println("  private int " + INDEX_MEMBER + ";");
                this.pwSlice.println();
                this.pwSlice.println("  /**");
                this.pwSlice.println("   * The slice's owner");
                this.pwSlice.println("   */");
                this.pwSlice.println("  @SuppressWarnings(\"unused\")");
                this.pwSlice.println("  private " + this.className + " " + JDO_IDENTITY_MEMBER + ";");
                this.pwSlice.println();
                this.trace(this.pwSlice, "Instance/SliceId");
                this.pwSlice.println("  /**");
                this.pwSlice.println("   * The slices' compound identity class");
                this.pwSlice.println("   */");
                this.pwSlice.println("  public static class " + SLICE_ID_CLASS_NAME + " implements java.io.Serializable {");
                this.pwSlice.println();
                this.pwSlice.println("    /**");
                this.pwSlice.println("     * The parent's object id");
                this.pwSlice.println("     */");
                this.pwSlice.println("    public java.lang.String " + JDO_IDENTITY_MEMBER + ";");
                this.pwSlice.println();
                this.pwSlice.println("    /**");
                this.pwSlice.println("     * The slice's index");
                this.pwSlice.println("     */");
                this.pwSlice.println("    public int " + INDEX_MEMBER + ";");
                this.pwSlice.println();
                this.pwSlice.println("    /**");
                this.pwSlice.println("     * Test for equality.");
                this.pwSlice.println("     * <p>");
                this.pwSlice.println("     * This method is required by JPA.");
                this.pwSlice.println("     * @param that the object to be compared");
                this.pwSlice.println("     * @return <code>true</code> if the two ids refer to the same slice object");
                this.pwSlice.println("     */");
                this.pwSlice.println("    @Override");
                this.pwSlice.println("    public boolean equals(java.lang.Object that) {");
                this.pwSlice.println("      return this == that || (");
                this.pwSlice.println("        that instanceof " + SLICE_ID_CLASS_NAME + " &&");
                this.pwSlice.println("        this." + INDEX_MEMBER + " == ((" + SLICE_ID_CLASS_NAME + ")that)." + INDEX_MEMBER + " &&");
                this.pwSlice.println("        (this." + JDO_IDENTITY_MEMBER + " == null ? ((" + SLICE_ID_CLASS_NAME + ")that)." + JDO_IDENTITY_MEMBER + 
                                              " == null : this." + JDO_IDENTITY_MEMBER + ".equals(((" + SLICE_ID_CLASS_NAME + ")that)." + JDO_IDENTITY_MEMBER + "))");
                this.pwSlice.println("      );");
                this.pwSlice.println("    }");
                this.pwSlice.println();
                this.pwSlice.println("    /**");
                this.pwSlice.println("     * Calculate the slice id's hash code.");
                this.pwSlice.println("     * <p>");
                this.pwSlice.println("     * This method is should be overridden together with {@link #equals(java.lang.Object)}.");
                this.pwSlice.println("     * @return the slice id's hash code");
                this.pwSlice.println("     */");
                this.pwSlice.println("    @Override");
                this.pwSlice.println("    public int hashCode() {");
                this.pwSlice.println("      return this." + INDEX_MEMBER + " + (this." + JDO_IDENTITY_MEMBER + " == null ? 0 : this." + JDO_IDENTITY_MEMBER + ".hashCode());");
                this.pwSlice.println("    }");
                this.pwSlice.println();
                this.pwSlice.println("    /**");
                this.pwSlice.println("     * Provide the slice id's string representation");
                this.pwSlice.println("     * @return the slice id's string representation");
                this.pwSlice.println("     */");
                this.pwSlice.println("    @Override");
                this.pwSlice.println("    public java.lang.String toString() {");
                this.pwSlice.println("      return new java.lang.StringBuilder().append(this." + JDO_IDENTITY_MEMBER + ").append('#').append(this." + INDEX_MEMBER + ").toString();");
                this.pwSlice.println("    }");
                this.pwSlice.println();
                this.pwSlice.println("  }");
                this.pwSlice.println();
            } 
            else {
                this.pwSlice.println("  /**");
                this.pwSlice.println("   * Constructor");
                this.pwSlice.println("   */");
                this.pwSlice.println("  protected " + this.className + SLICE_CLASS_NAME + '(');
                this.pwSlice.println("    " + this.className + " object,");
                this.pwSlice.println("    int index");
                this.pwSlice.println("  ){");
                this.pwSlice.println("    super(object, index);");
                this.pwSlice.println("  }");
            }
            this.pwSlice.println();
            this.pwSlice.println("}");
            this.pwSlice.println();
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
        this.pw.print(this.classDef.isAbstract() ? "Abstract class" : "Class");
        this.pw.println(" <code>" + this.classDef.getName() + "</code>"); 
        if (this.classDef.getAnnotation() != null) {
            this.pw.println(" *<p>");
            this.pw.println(MapperUtils.wrapText(" * ", this.classDef.getAnnotation()));
        }
        this.pw.println(" */");
        if(getFormat() == Format.JPA3) {
            String superClassName = this.isBaseClass() ? 
                this.classMetaData.getBaseClass() != null ?
                    this.classMetaData.getBaseClass() :                
                    QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME :
                this.getType(this.extendsClassDef.getQualifiedName());
            this.pw.println("@SuppressWarnings(\"serial\")");
            this.pw.print("public class " + this.className); 
            this.pw.println("  extends " + superClassName);
            this.pw.print(" implements ");
            this.pw.print(
                interfaceType(
                    this.classDef, 
                    hasSPI() ? Visibility.SPI : Visibility.CCI,
                        false
                )
            );
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
        if(getFormat() == Format.JPA3) {
            this.pw.println("  /**");
            this.pw.println("   * Constructor");
            this.pw.println("   */");
            this.pw.println("  public " + this.className + '('); 
            this.pw.println("  ){");
            this.pw.println("    // Implements Serializable");
            this.pw.println("  }");
            this.pw.println();            
            if(this.isBaseClass() && (this.classMetaData.getBaseClass() == null)) {
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
                this.pw.println("  @Override");
                this.pw.println("  protected java.lang.String getOpenmdxjdoIdentity(");
                this.pw.println("  ){");
                this.pw.println("     return this." + JDO_IDENTITY_MEMBER + ';');
                this.pw.println("  }");
                this.pw.println();
                this.pw.println("  /**");
                this.pw.println("   * Set the object's JDO identity key");
                this.pw.println("   */");
                this.pw.println("  @Override");
                this.pw.println("  protected void setOpenmdxjdoIdentity(");
                this.pw.println("    String value");
                this.pw.println("  ){");
                this.pw.println("     this." + JDO_IDENTITY_MEMBER + " = value;");
                this.pw.println("  }");
                this.pw.println();
            }
            if(this.isSliceHolder()) {
                this.pw.println("  /**");
                this.pw.println("   * Slice holder");
                this.pw.println("   */");
                this.pw.println(
                    "   private java.util.TreeMap<java.lang.Integer, " + this.className + SLICE_CLASS_NAME + "> " + 
                    SLICES_MEMBER + " = new java.util.TreeMap<java.lang.Integer, "+ this.className + SLICE_CLASS_NAME +">();"
                );
                this.pw.println();
                this.pw.println("   @SuppressWarnings(\"unchecked\")");
                this.pw.println("   protected final  <E extends " + this.className + SLICE_CLASS_NAME + "> java.util.SortedMap<java.lang.Integer,E> openmdxjdoGetSlices(");
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
                if(getFormat() == Format.JPA3) {
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
                if(getFormat() == Format.JPA3) {
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
        if (!attributeDef.isChangeable()) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", "This attribute is not changeable, i.e. its value can be set as long as the object is <em>TRANSIENT</em> or <em>NEW</em>"));
        }
        if (attributeDef.getAnnotation() != null) {
            this.pw.println("   * <p>");
            this.pw.println(MapperUtils.wrapText("   * ", attributeDef.getAnnotation()));
        }
        this.pw.println("   * @param " + attributeName + " The non-null new value for attribute <code>" + attributeDef.getName() + "</code>.");
        this.pw.println("   */");
        this.pw.println("  public void " + this.getMethodName(attributeDef.getBeanSetterName()) + "(");
        this.pw.println("    " + attributeType + ' ' + attributeName);
        if(getFormat() == Format.JPA3) {
            this.pw.println("  ){");
            this.pw.println("    super.openmdxjdoMakeDirty();");                        
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
        if(getFormat() == Format.JPA3) {
            this.pw.println("  ){");           
            this.pw.println("    super.openmdxjdoMakeDirty();");                        
            this.pw.println("    this." + attributeName + " = ");
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
        this.trace("Instance/AttributeGetStream");
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
                if(getFormat() == Format.JPA3) {
                    this.pw.println("  ){");
                    this.pw.println("    return org.w3c.cci2.BinaryLargeObjects.valueOf(this." + newValue + ");");
                    this.pw.println("  }");
                    mapDeclareValue("  ", "byte[]", newValue, attributeDef.isDerived() ? "public" : null);
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
                if(getFormat() == Format.JPA3) {
                    this.pw.println("  ){");
                    this.pw.println("    return org.w3c.cci2.CharacterLargeObjects.valueOf(this." + newValue + ");");
                    this.pw.println("  }");
                    mapDeclareValue("  ", "char[]", newValue, attributeDef.isDerived() ? "public" : null);
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
        if(getFormat() == Format.JPA3) {
            this.sliced.put(attributeName, attributeDef.getQualifiedTypeName());
        } else {
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
        if(getFormat() == Format.JPA3) {
            this.pw.println("  public " + getType(attributeDef, "org.w3c.cci2.SparseArray", null, TypeMode.MEMBER) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("  ){");
            this.pw.println("    java.util.SortedMap<java.lang.Integer," + this.className + SLICE_CLASS_NAME + "> slices = openmdxjdoGetSlices();");
            this.pw.println("    return org.w3c.cci2.SortedMaps.asSparseArray(");
            this.pw.print("      ");
            mapSlicedClass("        ", attributeDef, QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".SlicedMap");
            this.pw.println("      }");
            this.pw.println("    );");
            this.pw.println("  }");
        } 
        else {
            this.pw.println("  public " + getType(attributeDef, "org.w3c.cci2.SparseArray", Boolean.TRUE, TypeMode.MEMBER) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeGetSet(
        AttributeDef attributeDef
    ) throws ServiceException {
        String attributeName = getFeatureName(attributeDef);
        Integer embedded = null;
        String embeddedSet = null;
        if(getFormat() == Format.JPA3) {
            FieldMetaData fieldMetaData = getFieldMetaData(attributeDef.getQualifiedName()); 
            if(fieldMetaData != null) {
                embedded = fieldMetaData.getEmbedded();
            }
            if(embedded == null) {
                this.sliced.put(attributeName, attributeDef.getQualifiedTypeName());
            } 
            else {
                String fieldType = getObjectType(attributeDef.getQualifiedTypeName());
                for(
                    int i = 0;
                    i < embedded.intValue();
                    i++
                ){
                    this.pw.println("  private " + fieldType + " " + attributeName + SUFFIX_SEPARATOR + i + ";");
                }
            }
        } 
        else {
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
        if(getFormat() == Format.JPA3) {
            this.pw.println("  public " + getType(attributeDef, "java.util.Set", null, TypeMode.MEMBER) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("  ){");
            if(embedded == null) {
                this.pw.println("    java.util.SortedMap<java.lang.Integer," + this.className + SLICE_CLASS_NAME + "> slices = openmdxjdoGetSlices();");
                this.pw.print("    return ");
                mapSlicedClass("      ", attributeDef, QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".SlicedSet");
                this.pw.println("    };");
            } 
            else {
                this.pw.println("    return");
                embeddedSet = Identifier.CLASS_PROXY_NAME.toIdentifier(attributeDef.getName());
                this.pw.println("new " + embeddedSet + "(" + embedded + ");");
            }                
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
            this.pw.println("  public " + getType(attributeDef, "java.util.Set", Boolean.TRUE, TypeMode.MEMBER) + " " + this.getMethodName(attributeDef.getBeanGetterName()) + "(");
            this.pw.println("  );");
        }
        this.pw.println();
    }

    // -----------------------------------------------------------------------
    public void mapAttributeGetMap(
        AttributeDef attributeDef
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        String attributeName = getFeatureName(attributeDef);
        if(getFormat() == Format.JPA3) {
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
        if(getFormat() == Format.JPA3) {
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
            if(getFormat() == Format.JPA3) {
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
            if(getFormat() == Format.JPA3) {
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
            if(getFormat() == Format.JPA3) {
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

    private void mapSlicedClass(
        String prefix,
        StructuralFeatureDef featureDef,
        String sliceClass
    ) throws ServiceException{
        String featureName = getFeatureName(featureDef);
        String modelClass = featureDef.getQualifiedTypeName();
        boolean mapValueType = this.mapValueType(modelClass);
        String valueClass;
        String mapType;
        if(featureDef instanceof ReferenceDef){
            valueClass = "java.lang.String";
            mapType = sliceClass + "<java.lang.String," + this.className + SLICE_CLASS_NAME + '>';
        } else {
            valueClass = getObjectType(modelClass);
            mapType = getMapType(featureDef, sliceClass, Boolean.FALSE, TypeMode.MEMBER, this.className + SLICE_CLASS_NAME);
            if(mapType.indexOf('?') > 0){
                System.err.println(featureDef);
                throw new ServiceException(BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.ASSERTION_FAILURE, "?");
            }            
        }
        this.pw.println("new " + mapType + "(slices) {");
        this.pw.println(prefix + "@Override");
        this.pw.println(prefix + "protected " + valueClass + " getValue(" + this.className + SLICE_CLASS_NAME + " slice) {");
        this.pw.println(prefix + " return " + (mapValueType ? getModelType(modelClass) + ".toCCI(slice." + featureDef.getBeanGetterName() + "())" : "slice." + featureDef.getBeanGetterName() + "()") + ";");
        this.pw.println(prefix + "}");
        this.pw.println(prefix + "@Override");
        this.pw.println(prefix + "protected void setValue(" + this.className + SLICE_CLASS_NAME + " slice, " + valueClass + " value) {");
        this.pw.println(prefix + "  openmdxjdoMakeDirty();");
        this.pw.println(prefix + "  slice." + featureDef.getBeanSetterName() + "(" + (mapValueType ? getModelType(modelClass) + ".toJDO(value)" : "value") + ");");
        this.pw.println(prefix + "}");
        this.pw.println(prefix + "@Override");
        this.pw.println(prefix + "protected " + this.className + SLICE_CLASS_NAME + " newSlice(int index) {");
        this.pw.println(prefix + "  return new " + this.className + SLICE_CLASS_NAME + "(" + this.className + ".this, index);");
        this.pw.println(prefix + "}");
        this.pw.println(prefix + "@Override");
        this.pw.println(prefix + "protected void setSize(int size) {");
        this.pw.println(prefix + "  openmdxjdoMakeDirty();");
        this.pw.println(prefix + "  " + featureName + SIZE_SUFFIX + " = size;");
        this.pw.println(prefix + "}");
        this.pw.println(prefix + "@Override");
        this.pw.println(prefix + "public int size() {");
        this.pw.println(prefix + "  return " + featureName + SIZE_SUFFIX + ";");
        this.pw.println(prefix + "}");
    }
    
    // -----------------------------------------------------------------------
    public void mapAttributeGetList(
        AttributeDef attributeDef
    ) throws ServiceException {
        String attributeName = getFeatureName(attributeDef);
        Integer embedded = null;
        String embeddedList = null;
        if(getFormat() == Format.JPA3) {
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
        } 
        else {
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
        if(getFormat() == Format.JPA3) {
            this.pw.println("  public " + this.getType(attributeDef, "java.util.List", null, TypeMode.MEMBER) + ' ' + this.getMethodName(attributeDef.getBeanGetterName()) + '(');
            this.pw.println("  ){");
            if(embedded == null) {
                this.pw.println("    java.util.SortedMap<java.lang.Integer," + this.className + SLICE_CLASS_NAME + "> slices = openmdxjdoGetSlices();");
                this.pw.print("    return ");
                mapSlicedClass("      ", attributeDef, QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".SlicedList");
                this.pw.println("    };");
            } 
            else {
                this.pw.print("    return ");
                embeddedList = Identifier.CLASS_PROXY_NAME.toIdentifier(attributeDef.getName());
                this.pw.println("new " + embeddedList + "(" + embedded + ");");
            }                
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
            this.pw.println("  public " + this.getType(attributeDef, "java.util.List", Boolean.TRUE, TypeMode.MEMBER) + ' ' + this.getMethodName(attributeDef.getBeanGetterName()) + '(');
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
        if(getFormat() == Format.JPA3) {
            mapDeclareValue(
                "  ", 
                this.getValueType(modelType, false), 
                attributeName, 
                attributeDef.isDerived() ? "public" : null
            );
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
        if(getFormat() == Format.JPA3) {
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
        if(getFormat() == Format.JPA3) {
            mapDeclareValue(
                "  ", 
                this.getValueType(modelType, true), 
                attributeName, 
                attributeDef.isDerived() ? "public" : null
            );
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
        if(getFormat() == Format.JPA3) {
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
        PrintWriter pw,
        String indentation,
        String attributeType, 
        String attributeName,
        String visibility,
        boolean settersAndGetters
    ) throws ServiceException{
        this.trace(pw, "Instance/DeclareValue");
        pw.println();
        pw.println(indentation + "/**");
        pw.println(indentation + " * Attribute <code>" + attributeName + "</code>.");
        pw.println(indentation + " */");
        pw.println(indentation + (visibility == null ? "" : visibility + " ") + attributeType + ' ' + attributeName + ';');
        pw.println();
        if(settersAndGetters) {
            String getterName = AbstractNames.openmdx2AccessorName(
                attributeName,
                true, // forQuery
                false, // forBoolean
                true // singleValued
            ); 
            String setterName = AbstractNames.openmdx2AccessorName(
                attributeName,
                false, // forQuery
                false, // forBoolean
                true // singleValued
            );            
            pw.println(indentation + "public " + attributeType + " " + getterName + "(");
            pw.println(indentation + "){");
            pw.println(indentation + "  return this." + attributeName + ";");
            pw.println(indentation + "}");
            pw.println();
            pw.println(indentation + "public void " + setterName + "(");
            pw.println(indentation + "  " + attributeType + " value");
            pw.println(indentation + "){");
            pw.println(indentation + "  this." + attributeName + " = value;");
            pw.println(indentation + "}");
            pw.println();
        }
    }

    //-----------------------------------------------------------------------
    protected void mapDeclareValue(
        String indentation,
        String attributeType, 
        String attributeName,
        String visibility
    ) throws ServiceException{
        this.mapDeclareValue(
            this.pw, 
            indentation, 
            attributeType, 
            attributeName,
            visibility,
            false
        );
    }
    
    //-----------------------------------------------------------------------
    protected void mapDeclareReference(
        PrintWriter pw,
        String indentation,
        String qualifiedTypeName, 
        String referenceName, 
        boolean unused,
        boolean accessors
    ) throws ServiceException{
        this.trace(pw, "Instance/ReferenceDeclaration");
        if(unused){
            this.pw.println("  @SuppressWarnings(\"unused\")");
        }
        pw.println("  /**");
        pw.println("   * Instance referenced by <code>" + referenceName + "</code>.");
        pw.println("   */");
        pw.println("  java.lang.String " + referenceName + ';');
        pw.println();
        if(accessors) {
            String getterName = AbstractNames.openmdx2AccessorName(
                referenceName,
                true, // forQuery
                false, // forBoolean
                true // singleValued
            ); 
            String setterName = AbstractNames.openmdx2AccessorName(
                referenceName,
                false, // forQuery
                false, // forBoolean
                true // singleValued
            );                        
            pw.println(indentation + "public java.lang.String " + getterName + "(");
            pw.println(indentation + "){");
            pw.println(indentation + "  return this." + referenceName + ";");
            pw.println(indentation + "}");
            pw.println();
            pw.println(indentation + "public void " + setterName + "(");
            pw.println(indentation + "  java.lang.String value");
            pw.println(indentation + "){");
            pw.println(indentation + "  this." + referenceName + " = value;");
            pw.println(indentation + "}");
            pw.println();
        }        
    }

    //-----------------------------------------------------------------------
    protected void mapDeclareReference(
        String indentation,
        String qualifiedTypeName, 
        String referenceName, 
        boolean referencedEnd
    ) throws ServiceException{
        this.mapDeclareReference(
            this.pw,
            indentation, 
            qualifiedTypeName, 
            referenceName, 
            false, // !referencedEnd
            false
        );
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
        this.pw.println(indentation + "int " + attributeName + SIZE_SUFFIX + ';');
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
    private final PrintWriter pwSlice;

    static final String OPENMDX_JDO_PREFIX = "openmdxjdo";
    static final String SLICE_CLASS_NAME = "$Slice";
    static final String SLICE_ID_CLASS_NAME = "SliceId";
    static final String OBJECT_IDENTITY_CLASS_NAME = "Identity";
    static final String JDO_IDENTITY_MEMBER = OPENMDX_JDO_PREFIX + "Identity";
    static final String INDEX_MEMBER = OPENMDX_JDO_PREFIX + "Index";
    static final String SLICES_MEMBER = OPENMDX_JDO_PREFIX + "Slices";
    static final String QUALIFIED_IDENTITY_FEATURE_CLASS_NAME = "java.lang.String";
    static final String QUALIFIED_IDENTITY_CLASS_NAME = "org.oasisopen.cci2." + OBJECT_IDENTITY_CLASS_NAME;
    static final String QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME = "org.w3c.jpa3.AbstractObject";
    static final String QUALIFIED_ABSTRACT_OBJECT_IDENTITY_CLASS_NAME = "org.oasisopen.jdo2.Abstract" + OBJECT_IDENTITY_CLASS_NAME;
    static final String QUALIFIED_ABSTRACT_SLICE_ID_CLASS_NAME = QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".AbstractSliceId";
    static final String SUFFIX_SEPARATOR = "_";
    static final String SIZE_SUFFIX = SUFFIX_SEPARATOR + "size";
    static final String QUALIFIER_TYPE_WORD = "type";
    static final String QUALIFIER_TYPE_SUFFIX = "Type";
    static final String QUALIFIER_TYPE_CLASS_NAME = "org.oasisopen.cci2.QualifierType";
    static final String PERSISTENCY_SUFFIX = "IsPersistent";
    static final String ID_SUFFIX = SUFFIX_SEPARATOR + "Id";
    static final String REF_OBJECT_INTERFACE_NAME = "org.openmdx.base.accessor.jmi.cci.RefObject_1_0"; 
}

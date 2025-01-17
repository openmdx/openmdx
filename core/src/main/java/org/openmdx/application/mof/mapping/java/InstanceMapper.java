/*
 * ====================================================================
 * Description: Instance Mapper 
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
import org.openmdx.application.mof.externalizer.spi.AnnotationFlavour;
import org.openmdx.application.mof.externalizer.spi.ChronoFlavour;
import org.openmdx.application.mof.externalizer.spi.JakartaFlavour;
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
import org.openmdx.base.rest.cci.VoidRecord;
import org.openmdx.kernel.exception.BasicException;

/**
 * InstanceMapper
 */
public class InstanceMapper
extends AbstractClassMapper {

    /**
     * Constructor 
     */
    public InstanceMapper(
        ModelElement_1_0 classDef,
        Writer writer,
        Writer writerJdoSlice,
        Model_1_0 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData, 
        AnnotationFlavour annotationFlavour,
        JakartaFlavour jakartaFlavour,
        ChronoFlavour chronoFlavour,
        PrimitiveTypeMapper primitiveTypeMapper
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
            chronoFlavour,
            primitiveTypeMapper
        );
        this.pwSlice = writerJdoSlice == null ? null : new PrintWriter(writerJdoSlice);
        this.localFeatures = new HashMap<>(classDef.objGetMap("allFeature"));
        if(isBaseClass()) {            
            this.superFeatures = Collections.emptyMap();
        } 
        else {
            this.superFeatures = model.getElement(
                this.extendsClassDef.getQualifiedName()
            ).objGetMap("allFeature"); 
            this.localFeatures.keySet().removeAll(superFeatures.keySet());                
        }
    }

    public void mapReferenceAddWithoutQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        if(this.getFormat() == Format.JMI1) return;
        this.trace("Instance/ReferenceAddWithoutQualifier");
        ClassDef classDef = getClassDef(referenceDef.getQualifiedTypeName());
        String referenceType = getClassType(classDef).getType(classDef, this.getFormat(), TypeMode.MEMBER);
        printLine("  /**");
        MapperUtils
            .wrapText(
                "   * ",
                "Appends the specified element to the list of all the values for the reference {@code " + referenceDef.getName() + "}.", this::printLine);
        printLine("   * <p>");
        MapperUtils.wrapText(
            "   * ",
            "<em>Note: This is an extension to the JMI 1 standard.<br>" +
            "In order to remain standard compliant you should substitute this method with {@code get" + 
            referenceDef.getBeanGenericName() + 
            "().add(" + 
            referenceType + 
            ")}</em>.", this::printLine
        );
        if (referenceDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
        }
        printLine("   * @param newValue The element to be appended.");
        printLine("   */");
        printLine("  public void ", this.getMethodName("add" + referenceDef.getBeanGenericName()), " (");
        printLine("    ", referenceType, " newValue");
        printLine("  );");
        newLine();
    }

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
                printLine("  /**");
                MapperUtils.wrapText(
                    "   * ",
                    "Adds the specified element to the set of the values for the reference " +
                    "{@code " + referenceDef.getName() + "}.", this::printLine
                );
                printLine("   * <p>");
                MapperUtils.wrapText(
                    "   * ",
                    "<em>Note: This is an extension to the JMI 1 standard.</em>", this::printLine
                );
                if(referenceDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
                }
                printLine("   * @param ", qualifierName, PERSISTENCY_SUFFIX, " {@code true} if {@code ", qualifierName, "} is persistent");
                printLine("   * @param ", qualifierName, " The qualifier attribute value that qualifies the reference to get the element to be appended.");
                printLine("   * @param ", valueHolder, " The element to be appended.");
                printLine("   */");
                printLine("  public void ", this.getMethodName("add" + referenceDef.getBeanGenericName()), " (");
                printLine("    boolean ", qualifierName, PERSISTENCY_SUFFIX, ",");            
                printLine("    ", this.getType(referenceDef.getQualifiedQualifierTypeName(), getFormat(), false), " ", qualifierName, ",");
                printLine("    ", referenceType, " ", valueHolder);        
                printLine("  );");
                newLine();
                printLine("  /**");
                MapperUtils.wrapText(
                    "   * ",
                    "Adds the specified element to the set of the values for the reference " +
                    "{@code " + referenceDef.getName() + "} using a reassignable qualifier.", this::printLine
                );
                printLine("   * <p>");
                MapperUtils.wrapText(
                    "   * ",
                    "<em>Note: This is an extension to the JMI 1 standard.</em>", this::printLine
                );
                if(referenceDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
                }
                printLine("   * @param ", qualifierName, " The qualifier attribute value that qualifies the reference to get the element to be appended.");
                printLine("   * @param ", valueHolder, " The element to be appended.");
                printLine("   */");
                printLine("  public void ", this.getMethodName("add" + referenceDef.getBeanGenericName()), " (");
                printLine("    ", this.getType(referenceDef.getQualifiedQualifierTypeName(), getFormat(), false), " ", qualifierName, ",");
                printLine("    ", referenceType, " ", valueHolder);        
                printLine("  );");
                newLine();
                printLine("  /**");
                MapperUtils.wrapText(
                    "   * ",
                    "Adds the specified element to the set of the values for the reference " +
                    "{@code " + referenceDef.getName() + "} using an implementation-specific, reassignable qualifier.", this::printLine
                );
                printLine("   * <p>");
                MapperUtils.wrapText(
                    "   * ",
                    "<em>Note: This is an extension to the JMI 1 standard.</em>", this::printLine
                );
                if(referenceDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
                }
                printLine("   * @param ", valueHolder, " The element to be appended.");
                printLine("   */");
                printLine("  public void ", this.getMethodName("add" + referenceDef.getBeanGenericName()), " (");
                printLine("    ", referenceType, " ", valueHolder);        
                printLine("  );");
                newLine();
            }
        }
    }

    public void mapReferenceRemoveOptional(
        ReferenceDef referenceDef
    ) {
        // Nothing to do 
    }

    public void mapReferenceRemoveWithQualifier(
        ReferenceDef referenceDef
    ) {
        // Nothing to do 
    }

    public void mapReferenceSetNoQualifier(
        ReferenceDef referenceDef, 
        boolean optional, 
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
        } else {
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
        printLine("  /**");
        printLine("   * Sets a new value for the reference {@code ", referenceName, "}.");
        if (referenceDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
        }
        String methodName = this.getMethodName(beanSetterName);
        printLine(
            "   * @param ",
            referenceName,
            " The new",
            optional ? "&mdash;possibly {@code null}&mdash;" : " non-{@code null} ",
            "value for this reference."
        );
        printLine("   */");
        printLine("  public void ", methodName, "(");
        printLine("    ", argumentType, " ", referenceName);
        if(getFormat() == Format.JPA3) {
            printLine("  ){");                
            printLine("    throw new javax.jdo.JDOFatalUserException(");
            printLine("      \"Typed set not handled by data object\",");
            printLine("      new UnsupportedOperationException(\"Use ", methodName, InstanceMapper.ID_SUFFIX, "() instead.\"),");
            printLine("      this");
            printLine("    );");            
            printLine("  }");
            newLine();
            printLine("  public void ", methodName, InstanceMapper.ID_SUFFIX, "(");
            printLine("    java.lang.String ", referenceName);            
            printLine("  ) {");
            printLine("    super.openmdxjdoMakeDirty();");            
            printLine("    this.", referenceName, " = ", referenceName, ";");                            
            printLine("  }");            
        } 
        else {
            printLine("  );");            
        }
        newLine();
    }

    public void mapReferenceGetx_1NoQualifier(
        ReferenceDef referenceDef, 
        boolean optional, 
        boolean referencedEnd
    ) throws ServiceException {
        String referenceName = referencedEnd ? getFeatureName(referenceDef) : getFeatureName(referenceDef.getExposedEndName());
        String qualifiedTypeName = referencedEnd ? referenceDef.getQualifiedTypeName() : referenceDef.getExposedEndQualifiedTypeName();
        ClassDef classDef = getClassDef(qualifiedTypeName);
        ClassType classType = getClassType(classDef);
        if(getFormat() == Format.JPA3) {
            mapDeclareReference("  ", qualifiedTypeName, referenceName, referencedEnd);
        }
        if(referencedEnd) {
            this.trace("Instance/ReferenceGetx_1NoQualifier");
            printLine("  /**");
            printLine("   * Retrieves the value for the reference {@code ", referenceName, "}.");
            if (referenceDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
            }
            print("   * @return The ");
            print(optional ? "&mdash;possibly {@code null}&mdash;" : " non-{@code null} ");
            printLine(" value for this reference.");
            printLine("   */");
            String accessorType = classType.getType(classDef, this.getFormat(), TypeMode.RESULT); 
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            printLine("  public ", accessorType, " ", methodName, "(");
            if(getFormat() == Format.JPA3) {
                printLine("  ) {");
                printLine("    throw new javax.jdo.JDOFatalUserException(");
                printLine("      \"This signature is not handled by data object\",");
                printLine("      new UnsupportedOperationException(\"This signature is not handled by data object. Use ", methodName, InstanceMapper.ID_SUFFIX, "().\"),");
                printLine("      this");
                printLine("    );");                                    
                printLine("  }");
                newLine();
                printLine("  public java.lang.String ", methodName, InstanceMapper.ID_SUFFIX, "(");
                printLine("  ) {");
                printLine("    return this.", referenceName, ";");                            
                printLine("  }");
            } 
            else {
                printLine("  );");
            }
            newLine();
        }
    }

    public void mapReferenceGet0_nWithQualifier(
        ReferenceDef referenceDef, 
        boolean delegate
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        this.trace("Instance/ReferenceGet0_nWithQualifier");
        printLine("  /**");
        MapperUtils.wrapText(
            "   * ",
            "Retrieves the members of the given container referencing this object via {@code " + referenceDef.getName() + "}.", this::printLine);
        printLine("   * <p>");
        MapperUtils.wrapText(
            "   * ",
            "<em>Note: This is an extension to the JMI 1 standard.<br>" +
            "In order to remain standard compliant you should substitute this method with {@code javax.jdo.Query}</em>", this::printLine
        );
        if (referenceDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
        }
        printLine("   * @param ", referenceDef.getQualifierName(), " The container of the objects to be retrieved.");
        printLine("   * @return The members referencing ths object via {@code ", referenceDef.getName(), "}.");
        printLine("   */");
        printLine(
        	"  public ", 
        	this.getType(referenceDef, "java.util.Collection", Boolean.TRUE, TypeMode.MEMBER, null), 
        	" ", 
        	this.getMethodName(referenceDef.getBeanGetterName()),
        	"("
        );
        printLine("    ", getInterfaceType(referenceDef.getQualifiedQualifierTypeName()), " ", referenceDef.getQualifierName());
        if(getFormat() == Format.JPA3) {
            printLine("  ){");
            printLine("    throw new java.lang.UnsupportedOperationException(\"Not yet implemented\");"); // TODO
            printLine("  }");
        } else {
            printLine("  );");
        }
        newLine();
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

        final ClassMetaData classMetaData;
        if(getFormat() == Format.JPA3) {
            classMetaData = (ClassMetaData) getClassDef(referenceDef.getQualifiedTypeName()).getClassMetaData();
            if(classMetaData.isRequiresExtent()) {
                if (referenceDef.isComposition()) {
                    // No member for composites. Composites are retrieved by query
                } else {
                    this.trace("Instance/ReferenceDeclaration");
                    newLine();
                    printLine("  /**");
                    printLine("   * Reference {@code ", referenceName, "}.");
                    printLine("   */");
                    printLine("  @SuppressWarnings(\"unused\")");                    
                    printLine(
                    	"  private transient ",
                    	getType(referenceDef, "java.util.Set", null, TypeMode.MEMBER, null),
                    	" ",
                    	referenceName,
                    	";"
                    );
                    newLine();
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
            printLine("  /**");
            MapperUtils.wrapText(
                "   * ",
                "Retrieves the value for the reference {@code " + referenceDef.getName() + "} for the specified query.", this::printLine);
            MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "In order to remain standard compliant you should substitute this method with {@code javax.jdo.Query}</em>", this::printLine
            );
            if (referenceDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
            }
            printLine("   * @param query predicate which is applied to the set of referenced objects.");
            printLine("   * @return The objects for which the predicate evaluates to {@code true}.");
            printLine("   */");
            printLine(
            	"  public <T extends ",
            	this.getType(referenceDef.getQualifiedTypeName(), getFormat(), false),
            	"> java.util.List<T> ",
            	this.getMethodName(referenceDef.getBeanGetterName()),
            	"("
            );
            printLine("    ", qualifiedQueryName, " query");
            printLine("  );");
            newLine();
        } 
        else {
            printLine("  /**");
            MapperUtils.wrapText(
                "   * ",
                "Retrieves a set containing all the elements for the reference {@code " + referenceDef.getName() + "}.", this::printLine);
            if (referenceDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
            }
            printLine("   * @return A set containing all the elements for this reference.");
            printLine("   */");
            String cast = this.printAnnotationAndReturnCast(referenceDef, collectionType);
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            printLine("  public ", getType(referenceDef, collectionType, Boolean.TRUE, TypeMode.MEMBER, null), " ", methodName, "(");
            if(getFormat() == Format.JPA3) {
                printLine("  ){");
                if(referenceDef.isComposition()) {
                    printLine("    throw new javax.jdo.JDOFatalUserException(");
                    printLine("      \"This signature is not handled by data object\",");
                    printLine("      new UnsupportedOperationException(\"This signature is not handled by data object. Use query on composites.\"),");
                    printLine("      this");
                    printLine("    );");                                                        
                } 
                else if (referenceDef.isShared()) {
                    printLine("    throw new javax.jdo.JDOFatalUserException(");
                    printLine("      \"This signature is not handled by data object\",");
                    printLine("      new UnsupportedOperationException(\"This signature is not handled by data object. Use query on member '" + referenceName + "'.\"),");
                    printLine("      this");
                    printLine("    );");                                                                            
                } 
                else {
                    printLine(
                        "    ",
                        collectionType,
                        "<?> ",
                        referenceName,
                        " = this.",
                        referenceName,
                        ";"
                    );
                    printLine("    return ", cast, referenceName, ";");
                }                
                printLine("  }");
            } 
            else {
                printLine("  );");
            }
        }
        newLine();
    }

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
            printLine("  /**");
            MapperUtils.wrapText(
                "   * ",
                "Retrieves the {@code Collection} of objects referenced by {@code " + referenceDef.getName() + "}.", this::printLine);
            if (referenceDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
            }
            printLine("   * @return The {@code Collection} of referenced objects.");
            printLine("   */");
            printLine("  public ", this.getType(referenceDef, collectionType, Boolean.TRUE, TypeMode.MEMBER, null), " ", methodName, "(");
            if(getFormat() == Format.JPA3) {
                printLine("  ){");
                printLine("    throw new javax.jdo.JDOFatalUserException(");
                printLine("      \"This signature is not handled by data object\",");
                printLine("      new UnsupportedOperationException(\"This signature is not handled by data object. Use ", methodName, InstanceMapper.ID_SUFFIX, "().\"),");
                printLine("      this");
                printLine("    );");                                                                                            
                printLine("  }");
                newLine();
                printLine("  public ", collectionType, "<java.lang.String> ", methodName, InstanceMapper.ID_SUFFIX, "(");
                printLine("  ){");
                if(delegate) {
                    printLine("    return super.", methodName, InstanceMapper.ID_SUFFIX, "();");
                } else {
                    printLine("    java.util.SortedMap<java.lang.Integer,", this.className, SLICE_CLASS_NAME, "> slices = openmdxjdoGetSlices();");
                    print("    return ");
                    mapSlicedClass("      ", referenceDef, QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".Sliced" + (qualifierType == null ? "Set" : "List"));
                    printLine("    };");
                    
                }
                printLine("  }");
            } 
            else {
                printLine("  );");
            }
            newLine();
        } 
        else if(PrimitiveTypes.STRING == qualifierType) {
            ClassDef classDef = getClassDef(referenceDef.getQualifiedTypeName());
            String referenceType = getClassType(classDef).getType(classDef, getFormat(), TypeMode.INTERFACE);
            String methodName = this.getMethodName(referenceDef.getBeanGetterName());
            if(getFormat() == Format.JPA3 && !delegate) {
                this.trace("Instance/ReferenceDeclarationMap");
                newLine();
                printLine("  /**");
                printLine("   * Reference {@code ", referenceName, "} as {@code java.util.Map}");
                printLine("   */");
                printLine("  @SuppressWarnings(\"unused\")");                    
                printLine("  private transient java.util.Map<java.lang.String,", referenceType, "> ", referenceName, ";");
                newLine();
                this.sliced.put(referenceName, referenceDef.getQualifiedTypeName());                
            }
            this.trace("Instance/ReferenceGetMap");
            printLine("  /**");
            MapperUtils.wrapText(
                "   * ",
                "Retrieves the {@code Map} of objects referenced by {@code " + referenceDef.getName() + "}.", this::printLine);
            if (referenceDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
            }
            printLine("   * @return The {@code Map} of referenced objects.");
            printLine("   */");
            printLine("  public ", this.getMapType(referenceDef, java.lang.String.class, Boolean.TRUE), " ", methodName, "(");
            if(getFormat() == Format.JPA3) {
                printLine("  ){");
                printLine("    throw new javax.jdo.JDOFatalUserException(");
                printLine("      \"References of type map not handled by data object\",");
                printLine("      new UnsupportedOperationException(\"References of type map not handled by data object. Use ", methodName, InstanceMapper.ID_SUFFIX, "().\"),");
                printLine("      this");
                printLine("    );");                                                                                                            
                printLine("  }");
            } 
            else {
                printLine("  );");
            }
            newLine();
        }
    }

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
                printLine("  /**");
                MapperUtils.wrapText(
                    "   * ",
                    "Retrieves the value for the reference {@code " + referenceDef.getName() + "} for the specified qualifier attribute value.", this::printLine);
                if(i == 1) {
                    printLine("   * <p>");
                    MapperUtils.wrapText(
                        "   * ",
                        "This method is equivalent to the preferred invocation {@code " + 
                        methodName + "(false," + referenceDef.getQualifierName() + ")}.", this::printLine
                    );
            }
                printLine("   * <p>");
                if (referenceDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
                }
                if(i == 0) {
                    printLine("   * @param ", qualifierPersistencyArgumentName, " Defines whether value for the qualifier is persistent or not");
                }
                printLine("   * @param ", referenceDef.getQualifierName(), " The value for the qualifier attribute that qualifies this reference.");
                printLine("   * @return The possibly null value for this qualifier");
                printLine("   */");
                printLine("  public ", accessorType, " ", methodName, "(");
                if(i == 0) {
                    printLine("    boolean ", qualifierPersistencyArgumentName, ",");
                }
                printLine("    ", this.getType(referenceDef.getQualifiedQualifierTypeName(), getFormat(), false), " ", referenceDef.getQualifierName());
                printLine("  );");
                newLine();
            }
        } 
    }

    public void mapReferenceGet1_1WithQualifier(
        ReferenceDef referenceDef
    ) throws ServiceException {
        if(this.hasContainer()) {
            this.trace("Instance/IntfReferenceGet1_1WithQualifier");
            printLine("  /**");
            MapperUtils.wrapText(
                "   * ",
                "Retrieves the value for the optional reference {@code " + referenceDef.getName() + "} for the specified qualifier attribute value.", this::printLine);
            printLine("   * <p>");
            MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "In order to remain standard compliant you should substitute this method with {@code java.jdo.Query}</em>", this::printLine
            );
            if (referenceDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
            }
            printLine("   * @param ", referenceDef.getQualifierName(), " The value for the qualifier attribute that qualifies this reference.");
            printLine("   * @return The non-null value for this reference.");
            printLine("   */");
            printLine(
            	"  public ",
            	this.getType(referenceDef, null, Boolean.TRUE, TypeMode.MEMBER, null),
            	" ",
            	this.getMethodName(referenceDef.getBeanGetterName()),
            	"("
            );
            printLine(
            	"    ",
            	this.getType(referenceDef.getQualifiedQualifierTypeName(), getFormat(), false),
            	" ",
            	referenceDef.getQualifierName()
            );
            if(getFormat() == Format.JPA3) {
                printLine("  ){");
                printLine("    throw new java.lang.UnsupportedOperationException(\"Qualified object retrieval not yet supported by persistence layer\");");
                printLine("  }");
            } else {
                printLine("  );");
            }
            newLine();       
        }
    }

    @SuppressWarnings("unchecked")
    public void mapOperation(
        OperationDef operationDef
    ) throws ServiceException {
        this.trace("Instance/Operation");
        printLine("  /**");
        if (operationDef.getAnnotation() != null) {
            MapperUtils.wrapText("   * ", operationDef.getAnnotation(), this::printLine);
        }
        for(StructuralFeatureDef param: operationDef.getParameters()) {
            if(!VoidRecord.NAME.equals(param.getQualifiedTypeName())) {
                if (param.getAnnotation() != null) {
                    printLine("   * @param ", param.getName(), " ", param.getAnnotation(), "");
                }
            }
        }
        printLine("   */");
        printLine("  public ", this.getReturnType(operationDef), " ", this.getMethodName(operationDef.getName()), "(");
        int ii = 0;
        for(StructuralFeatureDef param: operationDef.getParameters()) {
            if(!VoidRecord.NAME.equals(param.getQualifiedTypeName())) {
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
            printLine(" ){");
            printLine("    throw new javax.jdo.JDOFatalUserException(");
            printLine("      \"Behavioural features not handled by data object\",");
            printLine("      new UnsupportedOperationException(\"Behavioural feature\"),");
            printLine("      this");
            printLine("    );");
            printLine("  }");
        } 
        else {
            List<ExceptionDef> exceptions = operationDef.getExceptions();
            print("  )");
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
                print(
                    separator +
                    namespace +
                    "." + 
                    exceptionDef.getName()
                );
                separator = ", ";
            }
            printLine(";");            
        }
        newLine();
    }

    public void mapEnd(
    ) throws ServiceException {
        this.trace("Instance/End");
        newLine();
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
                        false, // optional
                        false // referencedEnd
                    );
                }
                this.mapContainment(); 
            } 
            else if(this.isAuthority()){
                this.mapAuthority();
            }
        } 
        printLine("}");
    }

    private void mapAuthority(
    ){
        switch(getFormat()) {
            case CCI2: {
                printLine("  /**");
                printLine("   * Object Identity");
                printLine("   */");
                printLine("  public interface ", InstanceMapper.OBJECT_IDENTITY_CLASS_NAME, " extends ", QUALIFIED_IDENTITY_CLASS_NAME, " {");
                newLine();
                printLine("    /**");
                printLine("     * Retrieve the whole {@code authority}.");
                printLine("     * @return the whole {@code authority} value");
                printLine("     */");
                printLine("    public java.lang.String getAuthority();");
                newLine();            
                printLine("  }");
                newLine();            
            } break;
            default:
                break;
        }
    }

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
        String qualifierArgumentType = getType(qualifiedQualifierType, getFormat(), false);
        String qualifierValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(qualifierName);
        String objectValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(objectName);
        if(objectValueName.equals(qualifierValueName)) {
            objectValueName = '_' + objectValueName;
        }
        switch(getFormat()) {
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
                printLine("  /**");
                printLine("   * Object Identity");
                printLine("   */");
                printLine("  public interface ", InstanceMapper.OBJECT_IDENTITY_CLASS_NAME, " extends ", QUALIFIED_IDENTITY_CLASS_NAME, " {");
                newLine();
                printLine("    /**");
                printLine("     * Retrieve the {@code ", classDef.getName(), "}'s identity");
                printLine("     * @return the parent object's identity");
                printLine("     */");
                printLine("    public ", parentIdentityType, " ", referenceAccessorName, "();");
                newLine();
                printLine("    /**");
                printLine("     * Tells whether the {@code ", qualifierValueName, "} value is persistent or reassignable.");
                printLine("     * @return {@code PERSISTENT} or {@code REASSIGNABLE}");
                printLine("     */");
                printLine("    public ", QUALIFIER_TYPE_CLASS_NAME, " ", qualifierTypeAccessorName, "();");
                newLine();
                printLine("    /**");
                printLine("     * The {@code ", qualifierValueName, "} value");
                printLine("     * @return the {@code ", qualifierValueName, "} value");
                printLine("     */");
                printLine("    public ", qualifierArgumentType, " ", qualifierAccessorName, "();");
                newLine();            
                printLine("  }");
                break;
            default:
                break;
        }                
        newLine();            
    }

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

    boolean isAuthority(){
        return 
        "org:openmdx:base:Authority".equals(this.classDef.getQualifiedName());
    }

    boolean hasSPI(){
        return this.spiFeatures != null;
    }

    boolean isProvider(){
        return 
        "org:openmdx:base:Provider".equals(this.classDef.getQualifiedName());
    }

    boolean hasSlices(
    ){
        return 
            this.extendsClassDef != null ||
            this.classMetaData.getBaseClass() != null ||
            !this.sliced.isEmpty();
    }

    public void mapSingleValuedFields(
    ) {
        if(this.hasSlices()) {
            for(Map.Entry<String,String> e : this.sliced.entrySet()){
                this.mapDeclareSize("    ", e.getKey());
            }
            newLine();
        }
    }

    public void mapMultivaluedFields(
    ) throws ServiceException{
        if(this.isSliceHolder() || this.hasSlices()) {
            this.trace(pwSlice, "Instance/Begin");
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
            MapperUtils.wrapText(
                " * ",
                "{@code " + this.className + SLICE_CLASS_NAME + "} object hold the {@code " + 
                classDef.getName() + "}'s multivalued attributes", this.pwSlice::println
            );
            this.pwSlice.println(" */");
            String superClassName = this.isSliceHolder() || this.extendsClassDef == null ? (
                this.classMetaData != null && this.classMetaData.getBaseClass() != null ? this.classMetaData.getBaseClass() + SLICE_CLASS_NAME : null 
            ) : this.getType(this.extendsClassDef.getQualifiedName(), getFormat(), false) + SLICE_CLASS_NAME;
            mapGeneratedAnnotation(pwSlice::println);
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
                    String typeName = getType(qualifiedName, getFormat(), true);
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
                this.pwSlice.println("     * @return {@code true} if the two ids refer to the same slice object");
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

    @SuppressWarnings("unchecked")
    public void mapBegin(
    ) throws ServiceException {
        this.trace("Instance/Begin");
        printLine(
            "package ",
            this.getNamespace(
                MapperUtils.getNameComponents(
                    MapperUtils.getPackageName(
                        this.classDef.getQualifiedName()
                    )
                )
            ),
            ";"
        );
        newLine();
        printLine("/**");
        print(" * ");
        print(this.classDef.isAbstract() ? "Abstract class" : "Class");
        printLine(" {@code ", this.classDef.getName(), "}"); 
        if (this.classDef.getAnnotation() != null) {
            printLine(" *<p>");
            MapperUtils.wrapText(" * ", this.classDef.getAnnotation(), this::printLine);
        }
        printLine(" */");
        this.mapGeneratedAnnotation();
        if(getFormat() == Format.JPA3) {
            String superClassName = this.isBaseClass() ? 
                this.classMetaData.getBaseClass() != null ?
                    this.classMetaData.getBaseClass() :                
                    QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME :
                this.getType(this.extendsClassDef.getQualifiedName(), getFormat(), false);
            printLine("@SuppressWarnings(\"serial\")");
            print("public class " + this.className); 
            printLine("  extends ", superClassName);
            print(" implements ");
            print(
                interfaceType(
                    this.classDef, 
                    hasSPI() ? Visibility.SPI : Visibility.CCI,
                    false
                )
            );
            newLine();
        } else {
            printLine("public interface ", this.className);
            String separator = "  extends ";
            if(getFormat() == Format.JMI1) {
                print(
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
                    print(separator + REF_OBJECT_INTERFACE_NAME);
                    separator = ",\n    ";
                }
            } else {
                for (
                    Iterator<ClassDef> i = this.classDef.getSupertypes().iterator(); 
                    i.hasNext(); 
                    separator = ",\n    "
                ){
                    print(separator + this.getType(i.next().getQualifiedName(), getFormat(), false));
                }
            }
        }
        printLine("{");
        newLine();
        if(getFormat() == Format.JPA3) {
            printLine("  /**");
            printLine("   * Constructor");
            printLine("   */");
            printLine("  public ", this.className, "("); 
            printLine("  ){");
            printLine("    // Implements Serializable");
            printLine("  }");
            newLine();            
            if(this.isBaseClass() && (this.classMetaData.getBaseClass() == null)) {
                printLine("  /**");
                printLine("   * The the object's JDO identity key");
                printLine("   */");
                print(hasContainer() ? "   private" : "   public");
                printLine("  java.lang.String ", JDO_IDENTITY_MEMBER, ";");
                newLine();
                printLine("  /**");
                printLine("   * Retrieve the object's JDO identity key");
                printLine("   * @return the value of the object's JDO identity key");
                printLine("   */");
                printLine("  @Override");
                printLine("  protected java.lang.String getOpenmdxjdoIdentity(");
                printLine("  ){");
                printLine("     return this.", JDO_IDENTITY_MEMBER, ";");
                printLine("  }");
                newLine();
                printLine("  /**");
                printLine("   * Set the object's JDO identity key");
                printLine("   */");
                printLine("  @Override");
                printLine("  protected void setOpenmdxjdoIdentity(");
                printLine("    String value");
                printLine("  ){");
                printLine("     this.", JDO_IDENTITY_MEMBER, " = value;");
                printLine("  }");
                newLine();
            }
            if(this.isSliceHolder()) {
                printLine("  /**");
                printLine("   * Slice holder");
                printLine("   */");
                printLine(
                    "   private java.util.TreeMap<java.lang.Integer, ", 
                    this.className, 
                    SLICE_CLASS_NAME,
                    "> ", 
                    SLICES_MEMBER,
                    " = new java.util.TreeMap<java.lang.Integer, ",
                    this.className,
                    SLICE_CLASS_NAME,
                    ">();"
                );
                newLine();
                printLine("   @SuppressWarnings(\"unchecked\")");
                printLine(
                	"   protected final  <E extends ",
                	this.className,
                	SLICE_CLASS_NAME,
                	"> java.util.SortedMap<java.lang.Integer,E> openmdxjdoGetSlices("
                );
                printLine("   ){");
                printLine("      return (java.util.SortedMap<java.lang.Integer,E>)this.openmdxjdoSlices;");
                printLine("   }");
                newLine();
            }
        }
    }

    public void mapAttributeSetStream(AttributeDef attributeDef) {
        this.trace("Instance/AttributeSetStream");
        newLine();
        String newValue = getFeatureName(attributeDef);
        String modelType = attributeDef.getQualifiedTypeName();
        if (PrimitiveTypes.BINARY.equals(modelType)) {
            if(getFormat() == Format.JMI1) {
                printLine("  /**");
                MapperUtils
                    .wrapText(
                        "   * ",
                        "Sets a new binary value for the attribute {@code " + attributeDef.getName() + "}.", this::printLine);
                if (attributeDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
                }
                printLine("   * @param ", newValue, " A {@code BinaryLargeObject} containing the binary value for this attribute.");
                printLine("   */");
                printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
                printLine("    org.w3c.cci2.BinaryLargeObject ", newValue);
                printLine("  );");
                newLine();
            } else {
                printLine("  /**");
                printLine("   * Sets a new  binary value for the attribute {@code ", attributeDef.getName(), "}");
                if (attributeDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
                }
                printLine("   * @param ", newValue, " A {@code BinaryLargeObject} containing the value for this attribute.");
                printLine("   */");
                printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
                printLine("    org.w3c.cci2.BinaryLargeObject ", newValue);
                if(getFormat() == Format.JPA3) {
                    printLine("  ){");
                    printLine("    this.", newValue, " = openmdxjdoToArray(", newValue, ");");
                    printLine("  }");
                } else {
                    printLine("  );");
                }
                newLine();
            }
        } else if (PrimitiveTypes.STRING.equals(modelType)) {
            if(getFormat() == Format.JMI1) {
                printLine("  /**");
                printLine("   * Sets a new character large object value for the attribute {@code ", attributeDef.getName(), "}.");
                if (attributeDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
                }
                printLine("   * @param ", newValue, " A {@code CharacterLargeObject} containing the value for this attribute.");
                printLine("   */");
                printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
                printLine("     org.w3c.cci2.CharacterLargeObject ", newValue);
                printLine("  );");
                newLine();
            } else {
                printLine("  /**");
                printLine("   * Sets a new character large object value for the attribute {@code ", attributeDef.getName(), "}");
                if (attributeDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
                }
                printLine("   * @param ", newValue, " A {@code CharacterLargeObject} containing the value for this attribute.");
                printLine("   */");
                printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
                printLine("    org.w3c.cci2.CharacterLargeObject ", newValue);
                if(getFormat() == Format.JPA3) {
                    printLine("  ){");
                    printLine("    this.", newValue, " = openmdxjdoToArray(", newValue, ");");
                    printLine("  }");
                } else {
                    printLine("  );");
                }
            }
        }
        newLine();
    }

    public void mapAttributeSet1_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        Format format = getFormat();
        if(format == Format.JMI1) return;
        this.trace("Instance/AttributeSet1_1");
        String attributeName = getFeatureName(attributeDef);
        String modelType = attributeDef.getQualifiedTypeName();
        boolean primitiveType = this.model.isPrimitiveType(modelType);
        String attributeType = this.getType(modelType, format == Format.JPA3 && primitiveType ? Format.CCI2 : format, false);
        printLine("  /**");
        printLine("   * Sets a new value for the attribute {@code ", attributeDef.getName(), "}.");
        if (!attributeDef.isChangeable()) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", "This attribute is not changeable, i.e. its value can be set as long as the object is <em>TRANSIENT</em> or <em>NEW</em>", this::printLine);
        }
        if (attributeDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
        }
        printLine("   * @param ", attributeName, " The non-{@code null} new value for attribute {@code ", attributeDef.getName(), "}.");
        printLine("   */");
        printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
        printLine("    " + attributeType + ' ' + attributeName);
        if(format == Format.JPA3) {
            printLine("  ){");
            printLine("    super.openmdxjdoMakeDirty();");                        
            print("    this." + attributeName + " = ");
            if(this.mapValueType(modelType)) {
                String source = primitiveType ? attributeName : '(' + getType(modelType, format, true) + ')' + attributeName;
                print(getMappingExpression(modelType, Format.CCI2, Format.JPA3, source));
            } else if (primitiveType){
                print(attributeName);
            } else {
                String source = '(' + getType(modelType, format, true) + ')' + attributeName;
                print(source);
            }
            printLine(";");
            printLine("  }");
        } else {
            printLine("  );");
        }
        newLine();
    }

    public void mapAttributeSet0_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        Format format = getFormat();
        if(format == Format.JMI1) return;
        this.trace("Instance/AttributeSet0_1");
        String attributeName = getFeatureName(attributeDef);
        String modelType = attributeDef.getQualifiedTypeName();
        boolean primitiveType = this.model.isPrimitiveType(modelType);
        String attributeType = this.getType(modelType, format == Format.JPA3 && primitiveType ? Format.CCI2 : format, true);
        newLine();
        printLine("  /**");
        printLine("   * Sets a new value for the attribute {@code ", attributeDef.getName(), "}.");
        if (attributeDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
        }
        printLine("   * @param ", attributeName, " The possibly null new value for attribute {@code ", attributeDef.getName(), "}.");
        printLine("   */");
        printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
        printLine("    ", attributeType, " ", attributeName);
        if(format == Format.JPA3) {
            printLine("  ){");           
            printLine("    super.openmdxjdoMakeDirty();");                        
            printLine("    this.", attributeName, " = ");
            if(this.mapValueType(modelType)) {
                String source = primitiveType ? attributeName : '(' + getType(modelType, format, true) + ')' + attributeName;
                print(getMappingExpression(modelType, Format.CCI2, Format.JPA3, source));
            } else if (primitiveType){
                print(attributeName);
            } else {
                String source = '(' + getType(modelType, format, true) + ')' + attributeName;
                print(source);
            }
            printLine(";");
            printLine("  }");
        } else {
            printLine("  );");
        }
        newLine();
    }

    public void mapAttributeGetStream(
        AttributeDef attributeDef
    ) {
        this.trace("Instance/AttributeGetStream");
        newLine();
        String newValue = getFeatureName(attributeDef);

        if (PrimitiveTypes.BINARY.equals(attributeDef.getQualifiedTypeName())) {
            if(getFormat() != Format.JMI1) {
                printLine("  /**");
                printLine("   * Retrieves a binary large object value for the attribute {@code ", attributeDef.getName(), "}.");
                if (attributeDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
                }
                printLine("   */");
                printLine("  public org.w3c.cci2.BinaryLargeObject ", this.getMethodName(attributeDef.getBeanGetterName()), "(");
                if(getFormat() == Format.JPA3) {
                    printLine("  ){");
                    printLine("    return org.w3c.cci2.BinaryLargeObjects.valueOf(this.", newValue, ");");
                    printLine("  }");
                    mapDeclareValue("  ", "byte[]", newValue, attributeDef.isDerived() ? "public" : null);
                } else {
                    printLine("  );");
                }
                newLine();
            }
        } else if (PrimitiveTypes.STRING.equals(attributeDef.getQualifiedTypeName())) {
            if(getFormat() != Format.JMI1) {
                printLine("  /**");
                printLine("   * Retrieves a character large object value for the attribute {@code ", attributeDef.getName(), "}.");
                if (attributeDef.getAnnotation() != null) {
                    printLine("   * <p>");
                    MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
                }
                printLine("   */");
                printLine("  public org.w3c.cci2.CharacterLargeObject ", this.getMethodName(attributeDef.getBeanGetterName()), "(");
                if(getFormat() == Format.JPA3) {
                    printLine("  ){");
                    printLine("    return org.w3c.cci2.CharacterLargeObjects.valueOf(this.", newValue, ");");
                    printLine("  }");
                    mapDeclareValue("  ", "char[]", newValue, attributeDef.isDerived() ? "public" : null);
                } else {
                    printLine("  );");
                }
            }
        }
        newLine();
    }

    public void mapAttributeGetSparseArray(
        AttributeDef attributeDef
    ) throws ServiceException {
        String attributeName = getFeatureName(attributeDef);
        if(getFormat() == Format.JPA3) {
            this.sliced.put(attributeName, attributeDef.getQualifiedTypeName());
        }
        this.trace("Instance/AttributeGetSparseArray");
        printLine("  /**");
        MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a SparseArray containing all the elements for the attribute {@code " + attributeDef.getName() + "}.", this::printLine);
        if (attributeDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
        }
        printLine("   * @return A SparseArray containing all elements for this attribute.");
        printLine("   */");
        if(getFormat() == Format.JPA3) {
            printLine(
            	"  public ",
            	getType(attributeDef, "org.w3c.cci2.SparseArray", null, TypeMode.MEMBER, null),
            	" ",
            	this.getMethodName(attributeDef.getBeanGetterName()),
            	"("
            );
            printLine("  ){");
            printLine("    java.util.SortedMap<java.lang.Integer,", this.className, SLICE_CLASS_NAME, "> slices = openmdxjdoGetSlices();");
            printLine("    return org.w3c.cci2.SortedMaps.asSparseArray(");
            print("      ");
            mapSlicedClass("        ", attributeDef, QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".SlicedMap");
            printLine("      }");
            printLine("    );");
            printLine("  }");
        } 
        else {
            printLine(
            	"  public ",
            	getType(attributeDef, "org.w3c.cci2.SparseArray", Boolean.TRUE, TypeMode.MEMBER, null),
            	" ",
            	this.getMethodName(attributeDef.getBeanGetterName()),
            	"("
            );
            printLine("  );");
        }
        newLine();
    }

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
                String fieldType = getType(attributeDef.getQualifiedTypeName(), getFormat(), true);
                for(
                    int i = 0;
                    i < embedded;
                    i++
                ){
                    printLine("  private ", fieldType, " ", attributeName, SUFFIX_SEPARATOR, Integer.toString(i), ";");
                }
            }
        } 
        this.trace("Instance/AttributeGetSet");
        printLine("  /**");
        MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a set containing all the elements for the attribute {@code " + attributeDef.getName() + "}.", this::printLine);
        if (attributeDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
        }
        printLine("   * @return A set containing all elements for this attribute.");
        printLine("   */");
        if(getFormat() == Format.JPA3) {
            printLine(
            	"  public ",
            	getType(attributeDef, "java.util.Set", null, TypeMode.MEMBER, null),
            	" ",
            	this.getMethodName(attributeDef.getBeanGetterName()),
            	"("
            );
            printLine("  ){");
            if(embedded == null) {
                printLine(
                	"    java.util.SortedMap<java.lang.Integer,",
                	this.className + SLICE_CLASS_NAME,
                	"> slices = openmdxjdoGetSlices();"
                );
                print("    return ");
                mapSlicedClass("      ", attributeDef, QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".SlicedSet");
                printLine("    };");
            } 
            else {
                printLine("    return");
                embeddedSet = Identifier.CLASS_PROXY_NAME.toIdentifier(attributeDef.getName());
                printLine("new ", embeddedSet, "(", Integer.toString(embedded), ");");
            }                
            printLine("  }");
            if(embedded != null) {
                String elementType = getObjectType(attributeDef);
                newLine();
                printLine("  private class ", embeddedSet, " extends EmbeddedSet<", elementType, ">{");
                newLine();
                printLine("    ", embeddedSet, "(int capacity){");
                printLine("      super(capacity);");
                printLine("    }");
                newLine();
                printLine("    protected final ", elementType, " openmdxjdoGet(int index){");
                printLine("      switch(index){");
                for(
                    int i = 0;
                    i < embedded;
                    i++
                ){
                    printLine("         case ", Integer.toString(i), ": return ", attributeName, SUFFIX_SEPARATOR,Integer.toString(i), ";");
                }
                printLine(
                	"         default: throw new IndexOutOfBoundsException(\"Index \", + index + \" is not in [0..",
                	Integer.toString(embedded - 1),
                	"]\");"
                );
                printLine("      }");
                printLine("    }");
                newLine();
                printLine("    protected final void openmdxjdoSet(int index, ", elementType, " element){");
                printLine("      switch(index){");
                for(
                    int i = 0;
                    i < embedded;
                    i++
                ){
                    printLine(
                    	"         case ", 
                    	Integer.toString(i), 
                    	": ", 
                    	attributeName, 
                    	SUFFIX_SEPARATOR, 
                    	Integer.toString(i), 
                    	" = element;"
                    );
                }
                printLine(
                	"         default: throw new IndexOutOfBoundsException(\"Index \" + index + \" is not in [0..", 
                	Integer.toString(embedded - 1),
                	"]\");"
                );
                printLine("      }");
                printLine("    }");
                newLine();
                printLine("  }");
                newLine();
            }
        } else {
            printLine(
            	"  public ",
            	getType(attributeDef, "java.util.Set", Boolean.TRUE, TypeMode.MEMBER, null),
            	" ",
            	this.getMethodName(attributeDef.getBeanGetterName()),
            	"("
            );
            printLine("  );");
        }
        newLine();
    }

    public void mapAttributeGetMap(
        AttributeDef attributeDef
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        String attributeName = getFeatureName(attributeDef);
        if(getFormat() == Format.JPA3) {
            this.trace("Instance/AttributeDeclarationMap");
            newLine();
            printLine("  /**");
            printLine("   * Attribute {@code " + attributeName + "}.");
            printLine("   */");
            printLine("  private transient ", this.getMapType(attributeDef, java.lang.String.class, null), " ", attributeName, ";");
            newLine();
        }        
        this.trace("Instance/AttributeGetMap");
        printLine("  /**");
        MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a map containing all the elements for the attribute {@code " + attributeDef.getName() + "}.", this::printLine);
        if (attributeDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
        }
        printLine("   * @return A map containing all elements for this attribute.");
        printLine("   */");
        String cast = printAnnotationAndReturnMapCast(attributeDef, java.lang.String.class);
        printLine(
        	"  public ",
        	this.getMapType(attributeDef, java.lang.String.class, Boolean.TRUE),
        	" ",
        	this.getMethodName(attributeDef.getBeanGetterName()),
        	"("
        );
        if(getFormat() == Format.JPA3) {
            printLine("  ){");
            printLine("    return ", cast, "this.", attributeName, ";");
            printLine("  }");
        } else {
            printLine("  );");
        }
        newLine();
    }

    public void mapAttributeSetList(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/AttributeSetList");
        String attributeName = getFeatureName(attributeDef);
        Format format = getFormat();
        if(format == Format.JMI1) {
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Clears {@code " + attributeDef.getName() + "} and adds the members of the given List.", this::printLine);
            printLine("   * <p>");
            printLine("   * This method is equivalent to<pre>");
            printLine("   *   list.clear();");
            printLine("   *   list.addAll(", attributeName, ");");
            printLine("   * </pre>");
            MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "In order to remain standard compliant you should use the equivalent code.</em>", this::printLine
            );
            if (attributeDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
            }
            printLine("   * @param ", attributeName, " collection to be copied.");
            printLine("   */");
            printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
            printLine(
            	"    ", 
            	this.getType(attributeDef, "java.util.List", Boolean.FALSE, TypeMode.MEMBER, null),
            	" ",
            	attributeName
            );
            printLine("  );");
            newLine();            
        } else {
            String qualifiedTypeName = attributeDef.getQualifiedTypeName();
            String elementType = this.getType(qualifiedTypeName, format == Format.JPA3 && this.model.isPrimitiveType(qualifiedTypeName) ? Format.CCI2 : format, false);
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Clears {@code " + attributeDef.getName() + "} and adds the given value(s).", this::printLine);
            printLine("   * <p>");
            printLine("   * This method is equivalent to<pre>");
            printLine("   *   list.clear();");
            printLine("   *   for(", elementType, " e : ", attributeName, "){");
            printLine("   *     list.add(e);");
            printLine("   *   }");
            printLine("   * </pre>");
            if (attributeDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
            }
            printLine("   * @param ", attributeName, " value(s) to be added to {@code ", attributeDef.getName(), "}");
            printLine("   */");
            printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
            printLine("    ", elementType, "... ", attributeName);
            if(format == Format.JPA3) {
                printLine("  ){");
                printLine("    openmdxjdoSetCollection(");
                printLine("      ", this.getMethodName(attributeDef.getBeanGetterName()), "(),");
                printLine("      ", attributeName);
                printLine("    );");
                printLine("  }");
            } else {
                printLine("  );");
            }
            newLine();
        }
    }

    public void mapAttributeSetSet(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/AttributeSetSet");
        String attributeName = getFeatureName(attributeDef);
        Format format = getFormat();
        if(format == Format.JMI1) {
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Clears {@code " + attributeDef.getName() + "} and adds the members of the given Set.", this::printLine);
            printLine("   * <p>");
            printLine("   * This method is equivalent to<pre>");
            printLine("   *   set.clear();");
            printLine("   *   set.addAll(", attributeName, ");");
            printLine("   * </pre>");
            MapperUtils.wrapText(
                "   * ",
                "<em>Note: This is an extension to the JMI 1 standard.<br>" +
                "In order to remain standard compliant you should use the equivalent code.</em>", this::printLine
            );
            if (attributeDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
            }
            printLine("   * @param ", attributeName, " collection to be copied.");
            printLine("   */");
            printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
            printLine("    ", this.getType(attributeDef, "java.util.Set", Boolean.FALSE, TypeMode.MEMBER, null), " ", attributeName);
            printLine("  );");
            newLine();
        } else {
            String qualifiedTypeName = attributeDef.getQualifiedTypeName();
            String elementType = this.getType(qualifiedTypeName, format == Format.JPA3 && this.model.isPrimitiveType(qualifiedTypeName) ? Format.CCI2 : format, false);
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Clears {@code " + attributeDef.getName() + "} and adds the given value(s).", this::printLine);
            printLine("   * <p>");
            printLine("   * This method is equivalent to<pre>");
            printLine("   *   set.clear();");
            printLine("   *   for(", elementType, " e : ", attributeName, "){");
            printLine("   *     set.add(e);");
            printLine("   *   }");
            printLine("   * </pre>");
            if (attributeDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
            }
            printLine("   * @param ", attributeName, " value(s) to be added to {@code ", attributeDef.getName(), "}");
            printLine("   */");
            printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
            printLine("    ", elementType, "... ", attributeName);
            if(format == Format.JPA3) {
                printLine("  ){");
                printLine("    openmdxjdoSetCollection(");
                printLine("      ", this.getMethodName(attributeDef.getBeanGetterName()), "(),");
                printLine("      ", attributeName);
                printLine("    );");
                printLine("  }");
            } else {
                printLine("  );");
            }
            newLine();
        }
    }

    public void mapAttributeSetSparseArray(
        AttributeDef attributeDef
    ) throws ServiceException {
        this.trace("Instance/AttributeSetSparseArray");
        String attributeName = getFeatureName(attributeDef);
        if(getFormat() != Format.JMI1) {
            printLine("  /**");
            MapperUtils
                .wrapText(
                    "   * ",
                    "Clears {@code " + attributeDef.getName() + "} and adds the given value(s).", this::printLine);
            printLine("   * <p>");
            printLine("   * This method is equivalent to<pre>");
            printLine("   *   array.clear();");
            printLine("   *   array.putAll(", attributeName, ");");
            printLine("   * </pre>");
            if (attributeDef.getAnnotation() != null) {
                printLine("   * <p>");
                MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
            }
            printLine("   * @param ", attributeName, " value(s) to be added to {@code ", attributeDef.getName(), "}");
            printLine("   */");
            printLine("  public void ", this.getMethodName(attributeDef.getBeanSetterName()), "(");
            printLine("    ", this.getMapType(attributeDef, Integer.class, Boolean.FALSE), " ", attributeName);
            if(getFormat() == Format.JPA3) {
                printLine("  ){");
                printLine("    openmdxjdoSetArray(");
                printLine("      ", this.getMethodName(attributeDef.getBeanGetterName()), "(),");
                printLine("      ", attributeName);
                printLine("    );");
                printLine("  }");
            } else {
                printLine("  );");
            }
            newLine();
        }
    }

    public void mapReferenceSetWithQualifier(
        ReferenceDef referenceDef)
    throws ServiceException {
        this.trace("Instance/ReferenceSetWithQualifier");
        printLine("  /**");
        MapperUtils.wrapText(
            "   * ",
            "Sets a list containing all the new elements for the reference {@code " + referenceDef.getName() + "}.", this::printLine);
        if (referenceDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
        }
        printLine("   * @param newValue A list containing all the new elements for this reference.");
        printLine("   */");
        printLine("  public void ", this.getMethodName(referenceDef.getBeanSetterName()), "(");
        printLine("    ", this.getType(referenceDef, "org.w3c.cci2.SparseArray", Boolean.TRUE, TypeMode.MEMBER, null), " newValue");
        printLine("  );");
        newLine();
        printLine("  /**");
        MapperUtils.wrapText(
            "   * ",
            "Sets an array containing all the new elements for the reference {@code " + referenceDef.getName() + "}.", this::printLine);
        if (referenceDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", referenceDef.getAnnotation(), this::printLine);
        }
        printLine("   * @param newValue An array containing all the new elements for this reference.");
        printLine("   */");
        printLine("  public void ", this.getMethodName(referenceDef.getBeanSetterName()), "(");
        printLine("    ", this.getType(referenceDef.getQualifiedTypeName(), getFormat(), false), "[] newValue");
        printLine("  );");
        newLine();
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
            Format format = getFormat();
            valueClass = getType(modelClass, format == Format.JPA3 && this.model.isPrimitiveType(modelClass) ? Format.CCI2 : format, true);
            mapType = getMapType(featureDef, sliceClass, Boolean.FALSE, TypeMode.MEMBER, this.className + SLICE_CLASS_NAME);
            if(mapType.indexOf('?') > 0){
                System.err.println(featureDef);
                throw new ServiceException(BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.ASSERTION_FAILURE, "?");
            }            
        }
        printLine("new ", mapType, "(slices) {");
        printLine(prefix, "@Override");
        printLine(prefix, "protected ", valueClass, " getValue(", this.className, SLICE_CLASS_NAME, " slice) {");
        printLine(
        	prefix, 
        	" return ", 
        	mapValueType ? getMappingExpression(modelClass, Format.JPA3, Format.CCI2, "slice." + featureDef.getBeanGetterName() + "()") : "slice." + featureDef.getBeanGetterName() + "()",
        	";"
        );
        printLine(prefix + "}");
        printLine(prefix + "@Override");
        printLine(prefix + "protected void setValue(" + this.className + SLICE_CLASS_NAME + " slice, " + valueClass + " value) {");
        printLine(prefix + "  openmdxjdoMakeDirty();");
        printLine(prefix + "  slice." + featureDef.getBeanSetterName() + "(" + (mapValueType ? getMappingExpression(modelClass, Format.CCI2, Format.JPA3, "value") : "value") + ");");
        printLine(prefix + "}");
        printLine(prefix + "@Override");
        printLine(prefix + "protected " + this.className + SLICE_CLASS_NAME + " newSlice(int index) {");
        printLine(prefix + "  return new " + this.className + SLICE_CLASS_NAME + "(" + this.className + ".this, index);");
        printLine(prefix + "}");
        printLine(prefix + "@Override");
        printLine(prefix + "protected void setSize(int size) {");
        printLine(prefix + "  openmdxjdoMakeDirty();");
        printLine(prefix + "  " + featureName + SIZE_SUFFIX + " = size;");
        printLine(prefix + "}");
        printLine(prefix + "@Override");
        printLine(prefix + "public int size() {");
        printLine(prefix + "  return " + featureName + SIZE_SUFFIX + ";");
        printLine(prefix + "}");
    }
    
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
                String fieldType = getType(attributeDef.getQualifiedTypeName(), getFormat(), true);
                for(
                    int i = 0;
                    i < embedded;
                    i++
                ){
                    printLine("  private ", fieldType, " ", attributeName, SUFFIX_SEPARATOR, Integer.toString(i), ";");
                }
            }
            newLine();
        } 
        this.trace("Instance/AttributeGetList");
        printLine("  /**");
        MapperUtils
            .wrapText(
                "   * ",
                "Retrieves a list containing all the elements for the attribute {@code " + attributeDef.getName() + "}.", this::printLine);
        if (attributeDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
        }
        printLine("   * @return A list containing all elements for this attribute.");
        printLine("   */");
        if(getFormat() == Format.JPA3) {
            printLine(
            	"  public ", 
            	this.getType(attributeDef, "java.util.List", null, TypeMode.MEMBER, null),
            	" ",
            	this.getMethodName(attributeDef.getBeanGetterName()),
            	"("
            );
            printLine("  ){");
            if(embedded == null) {
                printLine("    java.util.SortedMap<java.lang.Integer,", this.className, SLICE_CLASS_NAME, "> slices = openmdxjdoGetSlices();");
                print("    return ");
                mapSlicedClass("      ", attributeDef, QUALIFIED_ABSTRACT_OBJECT_CLASS_NAME + ".SlicedList");
                printLine("    };");
            } 
            else {
                print("    return ");
                embeddedList = Identifier.CLASS_PROXY_NAME.toIdentifier(attributeDef.getName());
                printLine("new ", embeddedList, "(", embedded.toString(), ");");
            }                
            printLine("  }");
            if(embedded != null) {
                String elementType = getObjectType(attributeDef);
                newLine();
                printLine("  private class ", embeddedList, " extends EmbeddedList<", elementType, ">{");
                newLine();
                printLine("    ", embeddedList, "(int capacity){");
                printLine("      super(capacity);");
                printLine("    }");
                newLine();
                printLine("    protected final ", elementType, " openmdxjdoGet(int index){");
                printLine("      switch(index){");
                for(
                    int i = 0;
                    i < embedded;
                    i++
                ){
                    printLine("         case ", Integer.toString(i), ": return ", attributeName, SUFFIX_SEPARATOR, Integer.toString(i), ";");
                }
                printLine("         default: throw new IndexOutOfBoundsException(\"Index \" + index + \" is not in [0..", Integer.toString(embedded - 1), "]\");");
                printLine("      }");
                printLine("    }");
                newLine();
                printLine("    protected final void openmdxjdoSet(int index, ", elementType, " element){");
                printLine("      switch(index){");
                for(
                    int i = 0;
                    i < embedded;
                    i++
                ){
                    printLine("         case ", Integer.toString(i), ": ", attributeName + SUFFIX_SEPARATOR, Integer.toString(i), " = element;");
                }
                printLine(
                	"         default: throw new IndexOutOfBoundsException(\"Index \" + index + \" is not in [0..", 
                	Integer.toString(embedded - 1),
                	"]\");"
                );
                printLine("      }");
                printLine("    }");
                newLine();
                printLine("  }");
                newLine();
            }
        } 
        else {
            printLine(
            	"  public ", this.getType(attributeDef, "java.util.List", Boolean.TRUE, TypeMode.MEMBER, null),
            	" ",
            	this.getMethodName(attributeDef.getBeanGetterName()),
            	"("
            );
            printLine("  );");
        }
        newLine();
    }

    public void mapAttributeGet1_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        Format format = getFormat();
        if(format == Format.JMI1) return;
        String attributeName = getFeatureName(attributeDef);
        boolean objectIdentity = SystemAttributes.OBJECT_IDENTITY.equals(attributeName);
        String modelType = attributeDef.getQualifiedTypeName();
        if(format == Format.JPA3) {
            mapDeclareValue(
                "  ", 
                this.getType(modelType, format, false),
                attributeName, 
                attributeDef.isDerived() ? "public" : null
            );
        }
        this.trace("Instance/AttributeGet1_1");
        printLine("  /**");
        printLine("   * Retrieves the value for the attribute {@code ", attributeDef.getName(), "}.");
        if (attributeDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
        }
        printLine("   * @return The non-null value for attribute {@code ", attributeDef.getName(), "}.");
        printLine("   */");
        String cast =  printAnnotationAndReturnCast(attributeDef, null);
        String featureType = objectIdentity ?
            QUALIFIED_IDENTITY_FEATURE_CLASS_NAME :
            this.getType(attributeDef, null, Boolean.TRUE, TypeMode.MEMBER, Boolean.FALSE);        
        printLine("  public ", featureType, " ", this.getMethodName(attributeDef.getBeanGetterName()), "(");
        if(format == Format.JPA3) {
            printLine("  ){");
            print("    return ");
            if(this.mapValueType(modelType)) {
                print(getMappingExpression(modelType, Format.JPA3, Format.CCI2, "this." + attributeName));                    
            } else {
                print(cast + "this." + attributeName);                    
            }
            printLine(";");
            printLine("  }");
        } else {
            printLine("  );");
        }
        newLine();
    }

    public void mapAttributeGet0_1(
        AttributeDef attributeDef
    ) throws ServiceException {
        if(getFormat() == Format.JMI1) return;
        String attributeName = getFeatureName(attributeDef);
        String modelType = attributeDef.getQualifiedTypeName();
        if(getFormat() == Format.JPA3) {
            mapDeclareValue(
                "  ", 
                this.getType(modelType, this.getFormat(), true),
                attributeName, 
                attributeDef.isDerived() ? "public" : null
            );
        }
        this.trace("Instance/AttributeGet0_1");
        printLine("  /**");
        MapperUtils
            .wrapText(
                "   * ",
                "Retrieves the possibly null value for the optional attribute {@code " + attributeDef.getName() + "}.", this::printLine);
        if (attributeDef.getAnnotation() != null) {
            printLine("   * <p>");
            MapperUtils.wrapText("   * ", attributeDef.getAnnotation(), this::printLine);
        }
        printLine("   * @return The possibly null value for attribute {@code ", attributeDef.getName(), "}.");
        printLine("   */");
        String cast =  printAnnotationAndReturnCast(attributeDef, null);
        String featureType = this.getType(attributeDef, null, Boolean.TRUE, TypeMode.MEMBER, Boolean.TRUE);        
        printLine("  public ", featureType, " ", this.getMethodName(attributeDef.getBeanGetterName()), "(");
        if(getFormat() == Format.JPA3) {
            printLine("  ){");
            print("    return ");
            if(this.mapValueType(modelType)) {
                print(getMappingExpression(modelType, Format.JPA3, Format.CCI2, "this." + attributeName));                    
            } else {
                print(cast + "this." + attributeName);                    
            }
            printLine(";");
            printLine("  }");
        } else {
            printLine("  );");
        }
        newLine();
    }

    protected boolean mapValueType(
        String qualifiedTypeName
    ) throws ServiceException {
        String cci2Type = this.primitiveTypeMapper.getFeatureType(qualifiedTypeName, Format.CCI2, false, this.chronoFlavour.isClassic());
        String jpa3Type = this.primitiveTypeMapper.getFeatureType(qualifiedTypeName, Format.JPA3, false, this.chronoFlavour.isClassic());
        return !cci2Type.equals(jpa3Type);
    }

    protected void mapDeclareValue(
        PrintWriter pw,
        String indentation,
        String attributeType, 
        String attributeName,
        String visibility,
        boolean settersAndGetters
    ) {
        this.trace(pw, "Instance/DeclareValue");
        pw.println();
        pw.println(indentation + "/**");
        pw.println(indentation + " * Attribute {@code " + attributeName + "}.");
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

    protected void mapDeclareValue(
        String indentation,
        String attributeType, 
        String attributeName,
        String visibility
    ) {
        this.mapDeclareValue(
            this.pw, 
            indentation, 
            attributeType, 
            attributeName,
            visibility,
            false
        );
    }
    
    protected void mapDeclareReference(
        PrintWriter pw,
        String indentation,
        String qualifiedTypeName, 
        String referenceName, 
        boolean unused,
        boolean accessors
    ) {
        this.trace(pw, "Instance/ReferenceDeclaration");
        if(unused){
            printLine("  @SuppressWarnings(\"unused\")");
        }
        pw.println("  /**");
        pw.println("   * Instance referenced by {@code " + referenceName + "}.");
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

    protected void mapDeclareReference(
        String indentation,
        String qualifiedTypeName, 
        String referenceName, 
        boolean referencedEnd
    ) {
        this.mapDeclareReference(
            this.pw,
            indentation, 
            qualifiedTypeName, 
            referenceName, 
            false, // !referencedEnd
            false
        );
    }
    
    protected void mapDeclareSize(
        String indentation,
        String attributeName
    )  {
        this.trace("Instance/DeclareSize");
        newLine();
        printLine(indentation + "/**");
        printLine(indentation + " * Number of elements of attribute {@code ", attributeName, "}");
        printLine(indentation + " */");
        printLine(indentation + "int ", attributeName, SIZE_SUFFIX, ";");
        newLine();
    }

    /**
     * Retrieve not inherited features
     *
     * @return Returns the implementsClassDef.
     */
    protected Map<String,ModelElement_1_0> getFeatures(boolean inherited) {
        return inherited ? this.superFeatures : this.localFeatures;
    }    

    //-----------------------------------------------------------------------
    private final Map<String,ModelElement_1_0> superFeatures;
    private final Map<String,ModelElement_1_0> localFeatures;
    private final Map<String,String> sliced = new LinkedHashMap<>();
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
    static final String SUFFIX_SEPARATOR = "_";
    static final String SIZE_SUFFIX = SUFFIX_SEPARATOR + "size";
    static final String QUALIFIER_TYPE_WORD = "type";
    static final String QUALIFIER_TYPE_SUFFIX = "Type";
    static final String QUALIFIER_TYPE_CLASS_NAME = "org.oasisopen.cci2.QualifierType";
    static final String PERSISTENCY_SUFFIX = "IsPersistent";
    static final String ID_SUFFIX = SUFFIX_SEPARATOR + "Id";
    static final String REF_OBJECT_INTERFACE_NAME = "org.openmdx.base.accessor.jmi.cci.RefObject_1_0"; 

}

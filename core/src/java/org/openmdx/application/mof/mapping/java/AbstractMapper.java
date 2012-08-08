/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractMapper.java,v 1.10 2011/07/08 13:20:51 wfro Exp $
 * Description: JMITemplate 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/07/08 13:20:51 $
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
package org.openmdx.application.mof.mapping.java;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;

import org.omg.mof.spi.AbstractNames;
import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.ElementDef;
import org.openmdx.application.mof.mapping.cci.MapperTemplate;
import org.openmdx.application.mof.mapping.cci.MetaData_1_0;
import org.openmdx.application.mof.mapping.cci.OperationDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.java.metadata.Visibility;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.cci.Stereotypes;
import org.openmdx.kernel.exception.BasicException;

// ---------------------------------------------------------------------------
public abstract class AbstractMapper
    extends MapperTemplate {
    
    // -----------------------------------------------------------------------
    protected AbstractMapper(
        Writer writer,
        Model_1_0 model,
        Format format, 
        String packageSuffix,
        MetaData_1_0 metaData
    ) {
        super(
            writer,
            model
        );
        this.metaData = metaData;
        this.format = format;
        this.packageSuffix = packageSuffix;
    }

    // -----------------------------------------------------------------------
    /**
     * 
     */
    protected Model_1_0 getModel(){
        return super.model;
    }
    
    // -----------------------------------------------------------------------
    protected Format getFormat(){
        return this.format;
    }

    // -----------------------------------------------------------------------
    protected String getReturnType(
        OperationDef featureDef
    ) throws ServiceException {
        ClassDef resultTypeDef = new ClassDef(
            this.model.getElement(featureDef.getQualifiedReturnTypeName()),
            this.model
        );
        ClassType resultType = this.getClassType(resultTypeDef);
        return resultType.getType(resultTypeDef, this.getFormat(), TypeMode.RESULT);
    }
    
    // -----------------------------------------------------------------------
    protected String getType(
        StructuralFeatureDef featureDef, 
        String collectionClass, 
        Boolean returnValue,
        TypeMode featureUsage
    ) throws ServiceException {
        return this.getFeatureType(
            featureDef.getQualifiedTypeName(),
            collectionClass,
            returnValue,
            featureUsage, 
            ""
        );
    }
    
    // -----------------------------------------------------------------------
    protected String getMapType(
        StructuralFeatureDef featureDef, 
        String collectionClass, 
        Boolean returnValue,
        TypeMode featureUsage,
        String elementType
    ) throws ServiceException {
        return this.getFeatureType(
            featureDef.getQualifiedTypeName(),
            collectionClass,
            returnValue,
            featureUsage, 
            "," + elementType
        );
    }

    // -----------------------------------------------------------------------
    protected String getFeatureType(
        StructuralFeatureDef featureDef,
        Boolean returnValue,
        TypeMode featureUsage
    ) throws ServiceException {
    	Multiplicity multiplicity = ModelHelper.toMultiplicity(featureDef.getMultiplicity());
    	if(multiplicity == null) {
        	return this.getType(featureDef, "java.util.List", returnValue, featureUsage); // TODO verify whether this branch is really necessary
        } else {
	        switch(multiplicity) { 
		        case OPTIONAL: {
		            String type = featureDef.getQualifiedTypeName();
		        	return this.model.isPrimitiveType(type) ? this.getObjectType(type) : getFeatureType(type, null, returnValue, featureUsage, "");
		        }
		        case SINGLE_VALUE: {
		            String type = featureDef.getQualifiedTypeName();
		        	return this.model.isPrimitiveType(type) ? this.getType(type) : getFeatureType(type, null, returnValue, featureUsage, "") ;
		        }
		        case LIST:
		        	return this.getType(featureDef, "java.util.List", returnValue, featureUsage);
		        case SET:
		        	return this.getType(featureDef, "java.util.Set", returnValue, featureUsage);
		        case SPARSEARRAY:
		        	return this.getType(featureDef, "org.w3c.cci2.SparseArray", returnValue, featureUsage);
		        case STREAM: {
		            String type = featureDef.getQualifiedTypeName();
		        	return 
		        		PrimitiveTypes.BINARY.equals(type) ? "java.io.InputStream" :
		                PrimitiveTypes.STRING.equals(type) ? "java.io.Reader" :
		                "java.io.DataInput";
		        }
		        default:
		        	throw new ServiceException(
		        		BasicException.Code.DEFAULT_DOMAIN,
		        		BasicException.Code.NOT_SUPPORTED,
		        		"Unsupported multiplicity",
		        		new BasicException.Parameter("feature", featureDef.getQualifiedName()),
		        		new BasicException.Parameter("type", featureDef.getQualifiedTypeName()),
		        		new BasicException.Parameter("multiplicity", featureDef.getMultiplicity())
		        	);
	        }
        }
    }
        
    // -----------------------------------------------------------------------
    private String getFeatureType(
        String qualifiedTypeName, 
        String collectionClass, 
        Boolean returnValue,
        TypeMode featureUsage, 
        String amendment
    ) throws ServiceException {
        boolean multiValued = collectionClass != null;
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            String javaType = this.getObjectType(qualifiedTypeName);
            return (
               getFormat() == Format.JPA3 && Boolean.TRUE.equals(returnValue) ? "final " : ""
            ) + (
               multiValued ? collectionClass + '<' + javaType + amendment + '>' : javaType
            );
        } else if(returnValue == null) {
            ClassDef classDef = this.getClassDef(qualifiedTypeName);
            Format format = this.getFormat();
            String javaType = this.getClassType(classDef).getType(
                classDef, 
                getFormat() == Format.JPA3 && this.model.isStructureType(qualifiedTypeName) ? Format.CCI2 : format,
                featureUsage
            ); 
            return multiValued ? qualified(collectionClass,qualifiedTypeName,false) + '<' + javaType + amendment + '>' : javaType;
        } else {
            String javaType = this.interfaceType(
                qualifiedTypeName, 
                org.openmdx.application.mof.mapping.java.metadata.Visibility.CCI, 
                multiValued ? false : returnValue
            );
            if(returnValue.booleanValue()) {
                return "<T extends " + javaType + "> " + (
                    multiValued ? qualified(collectionClass,qualifiedTypeName,true) + "<T>" : "T"
                ); 
            } else if (multiValued) {
                return "".equals(amendment) ? 
                    qualified(collectionClass,qualifiedTypeName,false) + "<? extends " + javaType + '>' :
                    qualified(collectionClass,qualifiedTypeName,false) + "<" + javaType + amendment + '>';
            } else {
                return javaType;
            }
        }
    }
    
    /**
     * Add a scope to collectionClass if it is unqualified
     * 
     * @param collectionClass
     * @param javaType
     * 
     * @return the qualifiedCollection class
     * 
     * @throws ServiceException
     */
    private String qualified(
        String collectionClass,
        String qualifiedTypeName,
        boolean returnValue
    ) throws ServiceException {
        if(collectionClass.indexOf('.') < 0) {
            return this.interfaceType(
                qualifiedTypeName, 
                org.openmdx.application.mof.mapping.java.metadata.Visibility.CCI, 
                returnValue
            ) + '.' + collectionClass;
        } else {
            return collectionClass;
        }
    }
    
    // -----------------------------------------------------------------------
    protected String getType(
        String qualifiedTypeName
    ) throws ServiceException {        
        return getType(
            qualifiedTypeName,
            this.model.isStructureType(qualifiedTypeName) && getFormat() == Format.JPA3
        );
    }
    
    // -----------------------------------------------------------------------
    protected String getType(
        String qualifiedTypeName,
        boolean cci
    ) throws ServiceException {        
        return 
            this.model.isPrimitiveType(qualifiedTypeName) ? 
                getPrimitiveType(qualifiedTypeName) :
            cci ? 
               this.interfaceType(qualifiedTypeName, org.openmdx.application.mof.mapping.java.metadata.Visibility.CCI, false) : 
               this.getModelType(qualifiedTypeName);
    }
    
    // -----------------------------------------------------------------------
    protected String printAnnotationAndReturnCast(
        StructuralFeatureDef featureDef, 
        String collectionClass
    ) throws ServiceException {
        String qualifiedTypeName = featureDef.getQualifiedTypeName();
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            return "";
        } else { 
            return '(' + (
                collectionClass == null ? "T" : collectionClass + "<T>"
            ) + ')';
        }
    }

    // -----------------------------------------------------------------------
    protected String printAnnotationAndReturnMapCast(
        StructuralFeatureDef featureDef, 
        Class<?> keyClass
    ) throws ServiceException {
        String qualifiedTypeName = featureDef.getQualifiedTypeName();
        if(this.model.isPrimitiveType(qualifiedTypeName)){
            return "";
        } else { 
            if(getFormat() == Format.JPA3) {
                this.pw.println("  @SuppressWarnings(\"unchecked\")");
            } 
            return "(java.util.Map<" + keyClass.getName() + ", T>)";
        }
    }

    // -----------------------------------------------------------------------
    protected String getMapType(
        StructuralFeatureDef featureDef, 
        Class<?> keyClass, 
        Boolean returnValue
    ) throws ServiceException {
        String qualifiedTypeName = featureDef.getQualifiedTypeName();
        boolean primitive = this.model.isPrimitiveType(qualifiedTypeName);
        String baseTypeName = primitive ? getObjectType(
            qualifiedTypeName
        ) : interfaceType(
            qualifiedTypeName, 
            Visibility.CCI, 
            Boolean.TRUE.equals(returnValue)
        ); 
        String keyTypeName = keyClass.getName();
        if(returnValue != null && !primitive) {
            if(returnValue) {
                return "<T extends " + baseTypeName + "> java.util.Map<" + keyTypeName +",T>"; 
            } else {
                return "java.util.Map<" + keyTypeName +",? extends " + baseTypeName + '>'; 
            }
        } else {
            return "java.util.Map<" + keyTypeName +',' + baseTypeName + '>';
        }
    }
    
    // -----------------------------------------------------------------------
    protected String getFeatureType(
        StructuralFeatureDef featureDef,
        Boolean returnValue
    ) throws ServiceException {
        return this.getFeatureType(
            featureDef,
            returnValue,
            TypeMode.MEMBER            
        );
    }
    
    // -----------------------------------------------------------------------
    protected String getParameterType(
        StructuralFeatureDef featureDef 
    ) throws ServiceException {
        return this.getFeatureType(
            featureDef,
            null,
            TypeMode.PARAMETER            
        );
    }
    
    // -----------------------------------------------------------------------
    protected void mapParameter(
        String prefix,
        StructuralFeatureDef featureDef, 
        String suffix
    ) throws ServiceException {
        this.pw.println(
            prefix +
            this.getParameterType(featureDef) +
            ' ' +
            this.getFeatureName(featureDef) +
            suffix
        );
    }
    
    // -----------------------------------------------------------------------
    protected void mapGetMember(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.pw.println("  " + accessModifier + ' ' + this.getFeatureType(featureDef, true) + ' ' + this.getMethodName(featureDef.getBeanGetterName()) + "(");
        this.pw.println("  ) {");
        this.pw.println("    return this." + this.getFeatureName(featureDef) + ';');
        this.pw.println("  }");
        this.pw.println();        
    }

    // -----------------------------------------------------------------------
    protected void mapInitializeMember(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        String name = this.getFeatureName(featureDef);
        this.pw.println("    this." + name + " = " + name + ';');
    }
    
    // -----------------------------------------------------------------------
    protected void mapInitializeMember(
        StructuralFeatureDef featureDef, 
        String source
    ) throws ServiceException {
        this.pw.println(
            "    this." + this.getFeatureName(featureDef) + " = " + 
            source + '.' + this.getMethodName(featureDef.getBeanGetterName()) + "();"
        );
    }
    
    // -----------------------------------------------------------------------
    public void mapGetFeatureUsingObjectType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
    	Multiplicity multiplicity = ModelHelper.toMultiplicity(featureDef.getMultiplicity());
    	if(multiplicity == null) { // TODO verify whether this branch is really necessary
            this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef, "java.util.List", Boolean.TRUE, TypeMode.MEMBER) + ' ' + featureDef.getBeanGetterName() + " (");
            this.pw.println("  ) {");
            this.pw.println("    return (" + this.getType(featureDef, "java.util.List", Boolean.FALSE, TypeMode.MEMBER) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
            this.pw.println("  }");
    	} else {
	    	switch(multiplicity){
	    		case OPTIONAL:
	                this.pw.println("  " + accessModifier + ' ' + this.getObjectType(featureDef.getQualifiedTypeName()) + ' ' + featureDef.getBeanGetterName() + " (");
	                this.pw.println("  ) {");
	                this.pw.println("    return (" + this.getObjectType(featureDef.getQualifiedTypeName()) +  ")this.refGetValue(\"" + featureDef.getName() + "\", 0);");
	                this.pw.println("  }");
	                break;
	    		case SINGLE_VALUE:
	                this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef.getQualifiedTypeName()) );
	                this.pw.println(featureDef.getBeanGetterName() + " (");
	                this.pw.println("  ) {");
	                this.pw.println("    return (" + this.getObjectType(featureDef.getQualifiedTypeName()) +  ")this.refGetValue(\"" + featureDef.getName() + "\", 0);");
	                this.pw.println("  }");
	                break;
	    		case LIST:
	                this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef, "java.util.List", Boolean.TRUE, TypeMode.MEMBER) + ' ' + featureDef.getBeanGetterName() + " (");
	                this.pw.println("  ) {");
	                this.pw.println("    return (" + this.getType(featureDef, "java.util.List", Boolean.FALSE, TypeMode.MEMBER) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
	                this.pw.println("  }");
	                break;
	    		case SET:
	                this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef, "java.util.Set", Boolean.TRUE, TypeMode.MEMBER) + ' ' + featureDef.getBeanGetterName() + " (");
	                this.pw.println("  ) {");
	                this.pw.println("    return (" + this.getType(featureDef, "java.util.Set", Boolean.FALSE, TypeMode.MEMBER) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
	                this.pw.println("  }");
	                break;
	    		case MAP:
	                this.pw.println("  " + accessModifier + ' ' + this.getMapType(featureDef, java.lang.String.class, Boolean.TRUE) + ' ' + featureDef.getBeanGetterName() + " (");
	                this.pw.println("  ) {");
	                this.pw.println("    return (" + this.getMapType(featureDef, java.lang.String.class, Boolean.FALSE) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
	                this.pw.println("  }");
	                break;
	    		case SPARSEARRAY:
	                this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef, "org.w3c.cci2.SparseArray", Boolean.TRUE, TypeMode.MEMBER) + ' ' + featureDef.getBeanGetterName() + " (");
	                this.pw.println("  ) {");
	                this.pw.println("    return (" + this.getType(featureDef, "org.w3c.cci2.SparseArray", Boolean.FALSE, TypeMode.MEMBER) + ")this.refGetValue(\"" + featureDef.getName() + "\");");
	                this.pw.println("  }");
	                break;
	    		case STREAM:
	                if (PrimitiveTypes.BINARY.equals(featureDef.getQualifiedTypeName())) {
	                    this.pw.println("  " + accessModifier + " java.io.InputStream " + featureDef.getBeanGetterName() + " (");
	                    this.pw.println("  ) {");
	                    this.pw.println("    return (java.io.InputStream)this.refGetValue(\"" + featureDef.getName() + "\", 0);");
	                    this.pw.println("  }");
	                } else if (PrimitiveTypes.STRING.equals(featureDef.getQualifiedTypeName())) {
	                    this.pw.println("  " + accessModifier + " java.io.Reader " + featureDef.getBeanGetterName() + " (");
	                    this.pw.println("  ) {");
	                    this.pw.println("    return (java.io.Reader)this.refGetValue(\"" + featureDef.getName() + "\", 0);");
	                    this.pw.println("  }");
	                } else {
	                    this.pw.println("  " + accessModifier + " java.io.DataInput " + featureDef.getBeanGetterName() + " (");
	                    this.pw.println("  ) {");
	                    this.pw.println("    return (java.io.DataInput)this.refGetValue(\"" + featureDef.getName() + "\", 0);");
	                    this.pw.println("  }");
	                }
	                break;
    		}
    	}
    }
    
    // -----------------------------------------------------------------------
    public void mapGetFeatureIndexedUsingNativeType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef.getQualifiedTypeName()) + ' ' + this.getMethodName(featureDef.getBeanGetterName()) + "(");
        this.pw.println("    int index");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(featureDef.getQualifiedTypeName())) {
            this.pw.println("    return ((java.lang.Boolean)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index)).booleanValue();");
        } else if (PrimitiveTypes.DATETIME.equals(featureDef
            .getQualifiedTypeName())) {
            this.pw.println("    return (java.util.Date)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index);");
        } else if (
            PrimitiveTypes.DATE.equals(featureDef.getQualifiedTypeName()) ||
            PrimitiveTypes.DURATION.equals(featureDef.getQualifiedTypeName()) ||
            PrimitiveTypes.ANYURI.equals(featureDef.getQualifiedTypeName())             
        ) {
            this.pw.println(
                "    return (" +
                getType(featureDef.getQualifiedTypeName()) +
                ")this.refGetValue(\"" + 
                featureDef.getQualifiedName() + 
                "\", index);"
            );
        } else if (
            PrimitiveTypes.INTEGER.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.SHORT.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.LONG.equals(featureDef.getQualifiedTypeName())
        ) {
            this.pw.println("    return ((java.lang.Number)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index))."
                + this.getType(featureDef.getQualifiedTypeName())
                + "Value();");
        } else {
            this.pw.println("    return ("
                + this.getType(featureDef.getQualifiedTypeName())
                + ")this.refGetValue(\"" + featureDef.getQualifiedName() + "\", index);");
        }
        this.pw.println("  }");
        this.pw.println("");
    }
     
    // -----------------------------------------------------------------------
    public void mapGetFeatureKeyedUsingNativeType(
        String accessModifier,
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        this.pw.println("  " + accessModifier + ' ' + this.getType(featureDef.getQualifiedTypeName()) + ' ' + this.getMethodName(featureDef.getBeanGetterName()) + "(");
        this.pw.println("    String key");
        this.pw.println("  ) {");
        if (PrimitiveTypes.BOOLEAN.equals(featureDef.getQualifiedTypeName())) {
            this.pw
                .println("    return ((java.lang.Boolean)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key)).booleanValue();");
        } else if (PrimitiveTypes.DATETIME.equals(featureDef
            .getQualifiedTypeName())) {
            this.pw
                .println("    return (java.util.Date)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key);");
        } else if (
            PrimitiveTypes.DATE.equals(featureDef.getQualifiedTypeName()) ||
            PrimitiveTypes.DURATION.equals(featureDef.getQualifiedTypeName()) ||
            PrimitiveTypes.ANYURI.equals(featureDef.getQualifiedTypeName())             
        ) {
            this.pw.println(
                "    return (" +
                getType(featureDef.getQualifiedTypeName()) +
                ")this.refGetValue(\"" + 
                featureDef.getQualifiedName() + 
                "\", key);"
            );
        } else if (
            PrimitiveTypes.INTEGER.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.SHORT.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.LONG.equals(featureDef.getQualifiedTypeName())
        ) {
            this.pw.println(
                "    return ((java.lang.Number)this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key))." +
                this.getType(featureDef.getQualifiedTypeName()) +
                "Value();"
            );
        } else {
            this.pw.println("    return ("
                + this.getType(featureDef.getQualifiedTypeName())
                + ")this.refGetValue(\"" + featureDef.getQualifiedName() + "\", key);");
        }
        this.pw.println("  }");
        this.pw.println("");        
    }

    // -----------------------------------------------------------------------
    /**
     * Maps a list of namespace name components to a namespace name in the
     * destination language.
     */
    public String getNamespace(
        List<String> namespaceNameComponents
    ) {
        return getNamespace(
            namespaceNameComponents,
            this.packageSuffix
        );
    }
    
    /**
     * Maps a list of namespace name components to a namespace name in the
     * destination language.
     */
    public static String getNamespace(
        List<String> namespaceNameComponents,
        String packageSuffix
    ) {
        StringBuffer namespace = new StringBuffer();
        for(
            int i = 0, iLimit = namespaceNameComponents.size(); 
            i < iLimit; 
            i++
        ) AbstractNames.openmdx2NamespaceElement(
            i == 0 ? namespace : namespace.append('.'),
            namespaceNameComponents.get(i)
        );
        return namespace.append(
            '.'
        ).append(
            packageSuffix
        ).toString();
    }
    
    // -----------------------------------------------------------------------
    /**
     * Maps a primitive type to a primitive type of the target platform
     */
    protected String getObjectType(
        String qualifiedTypeName
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            if(PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) {
                return "java.lang.Boolean";
            }
            if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) {
                return "java.lang.Short";
            }
            if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) {
                return "java.lang.Long";
            }
            if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) {
                return "java.lang.Integer";
            }
            else {
                return this.getType(qualifiedTypeName);
            }
        }
        else {
            return this.getType(qualifiedTypeName);
        }
    }

    // -----------------------------------------------------------------------
    protected String interfaceType(
        String qualifiedTypeName, 
        Visibility visibility,
        boolean returnValue
    ) throws ServiceException {
        ClassDef interfaceTypeDef = new ClassDef(
            this.model.getElement(qualifiedTypeName), 
            this.model
        );
        ClassType interfaceType = this.getClassType(interfaceTypeDef);
        return returnValue
            ? interfaceType.getType(interfaceTypeDef, this.getFormat(), TypeMode.RESULT)
            : interfaceType.getType(interfaceTypeDef, this.getFormat(), TypeMode.INTERFACE);
    }

    // -----------------------------------------------------------------------
    protected String interfaceType(
        ElementDef elementDef, 
        Visibility visibility,
        boolean returnValue
    ) throws ServiceException {
        return this.interfaceType(
            elementDef.getQualifiedName(), 
            visibility,
            returnValue
        );
    }
    
    // -----------------------------------------------------------------------
    protected String getPackageSuffix(
        Visibility visbility
    ){
        switch(visbility) {
            case CCI : return Names.CCI2_PACKAGE_SUFFIX;
            case SPI : return Names.SPI2_PACKAGE_SUFFIX;
            default: return null;
        }
    }
    
    // -----------------------------------------------------------------------
    protected String cciType(
        OperationDef operationDef
    ) throws ServiceException {
        return "org:openmdx:base:Void".equals(operationDef.getQualifiedReturnTypeName()) ?
            "void" :
            interfaceType(operationDef.getQualifiedReturnTypeName(), org.openmdx.application.mof.mapping.java.metadata.Visibility.CCI, false);
    }
    
    // -----------------------------------------------------------------------
    protected String getQueryType(
        String qualifiedTypeName, 
        String targetNamspace
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            return PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.BooleanTypePredicate"
            ) : PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.lang.Short>"
            ) : PrimitiveTypes.LONG.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.lang.Long>" 
            ) : PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? ( 
                "org.w3c.cci2.ComparableTypePredicate<java.lang.Integer>" 
            ) : PrimitiveTypes.STRING.equals(qualifiedTypeName) ? (                      
                "org.w3c.cci2.StringTypePredicate" 
            ) : (
                PrimitiveTypes.ANYURI.equals(qualifiedTypeName) 
            ) ? ( 
                "org.w3c.cci2.ResourceIdentifierTypePredicate<java.net.URI>" 
            ) : (
                PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName) ||
                PrimitiveTypes.XRI.equals(qualifiedTypeName) 
            ) ? ( 
                "org.w3c.cci2.StringTypePredicate" 
            ) : PrimitiveTypes.DATETIME.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.util.Date>"
            ) : PrimitiveTypes.DATE.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.PartiallyOrderedTypePredicate<javax.xml.datatype.XMLGregorianCalendar>"
            ) : PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.net.URI>"
            ) : PrimitiveTypes.DURATION.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.PartiallyOrderedTypePredicate<javax.xml.datatype.Duration>"
            ) : PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? (
                "org.w3c.cci2.ComparableTypePredicate<java.math.BigDecimal>"
            ) : "org.w3c.cci2.AnyTypePredicate";   
        } else {
            List<String> nameComponents = MapperUtils.getNameComponents(qualifiedTypeName);
            String namespace = getNamespace(
                MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedTypeName)),
                Names.CCI2_PACKAGE_SUFFIX
            );
            return (
                namespace.equals(targetNamspace) ? "" : namespace + '.'
            ) + Identifier.CLASS_PROXY_NAME.toIdentifier( 
                nameComponents.get(nameComponents.size()-1), // modelElementName
                null, // removablePrefix, 
                null, // prependablePrefix
                null,
                "Query" // appendableSuffix
            );
        }
    }

    protected String getParseExpression(
        String qualifiedTypeName, 
        Multiplicity multiplicity,
        String expression
    ) throws ServiceException {
        if(this.model.isPrimitiveType(qualifiedTypeName)) {
            if(multiplicity != null) {
                switch(multiplicity) {
                    case OPTIONAL: 
                        return PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? (
                            expression + " == null ? null : java.lang.Boolean.valueOf(" + expression + ")"
                        ) : PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? (
                            expression + " == null ? null : java.lang.Short.valueOf(" + expression + ")"
                        ) : PrimitiveTypes.LONG.equals(qualifiedTypeName) ? (
                            expression + " == null ? null : java.lang.Long.valueOf(" + expression + ")"
                        ) : PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? ( 
                            expression + " == null ? null : java.lang.Integer.valueOf(" + expression + ")"
                        ) : PrimitiveTypes.STRING.equals(qualifiedTypeName) ? (                      
                            expression
                        ) : PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? ( 
                            expression + " == null ? null : java.net.URI.create(" + expression + ")"
                        ) : PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? (
                            expression + " == null ? null : new java.math.BigDecimal(" + expression + ")"    
                        ) : (
                            expression + " // type " + qualifiedTypeName + " is implicitely mapped to java.lang.String"
                        ); 
                    case SINGLE_VALUE:
                        return PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? (
                            "java.lang.Boolean.parseBoolean(" + expression + ")"
                        ) : PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? (
                            "java.lang.Short.parseShort(" + expression + ")"
                        ) : PrimitiveTypes.LONG.equals(qualifiedTypeName) ? (
                            "java.lang.Long.parseLong(" + expression + ")"
                        ) : PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? ( 
                            "java.lang.Integer.parseInt(" + expression + ")"
                        ) : PrimitiveTypes.STRING.equals(qualifiedTypeName) ? (                      
                            expression
                        ) : PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? ( 
                            "java.net.URI.create(" + expression + ")"
                        ) : PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? (
                            "new java.math.BigDecimal(" + expression + ")"    
                        ) : (
                            expression + " // type " + qualifiedTypeName + " is implicitely mapped to java.lang.String"
                        ); 
                }
            }
            return "null // TODO support parsing of multi-valued arguments";
        } else {
            return "null // TODO support parsing of non-primitive arguments";
        }
    }
    
    // -----------------------------------------------------------------------

    protected String getObjectType(
        StructuralFeatureDef featureDef
    ) throws ServiceException {
        return getObjectType(featureDef.getQualifiedTypeName());
    }
    
    // -----------------------------------------------------------------------
    protected static String getPrimitiveType(
        String qualifiedTypeName
    ){
        return 
            PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName) ? "boolean" :
            PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? "short" :
            PrimitiveTypes.LONG.equals(qualifiedTypeName) ? "long" :
            PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? "int" :
            PrimitiveTypes.BINARY.equals(qualifiedTypeName) ? "byte[]" :
            PrimitiveTypes.STRING.equals(qualifiedTypeName) ? "java.lang.String" :
            PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName) ? "java.lang.Object" :
            PrimitiveTypes.DATETIME.equals(qualifiedTypeName) ? "java.util.Date" :
            PrimitiveTypes.DATE.equals(qualifiedTypeName) ? "javax.xml.datatype.XMLGregorianCalendar" :
            PrimitiveTypes.DURATION.equals(qualifiedTypeName) ? "javax.xml.datatype.Duration" :
            PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? "java.math.BigDecimal" :
            PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? "java.net.URI" :
            "java.lang.String";
    }
    
    // -----------------------------------------------------------------------
    protected String getModelType(
        String qualifiedTypeName
    ) throws ServiceException {
        List<String> nameComponents = MapperUtils.getNameComponents(qualifiedTypeName);
        return AbstractMapper.getNamespace(
            MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedTypeName)),
            this.format == Format.JPA3 && 
            getModel().getElement(qualifiedTypeName).objGetList("stereotype").contains(Stereotypes.ROOT) ? 
                Names.CCI2_PACKAGE_SUFFIX : 
                this.packageSuffix
        ) + '.' + Identifier.CLASS_PROXY_NAME.toIdentifier( 
            nameComponents.get(nameComponents.size()-1)
        );
    }

    // -----------------------------------------------------------------------
    protected String getOrderType(
        StructuralFeatureDef featureDef
    ){
        return
            Multiplicity.SINGLE_VALUE.toString().equals(featureDef.getMultiplicity()) ||
            Multiplicity.OPTIONAL.toString().equals(featureDef.getMultiplicity()) ?
            "org.w3c.cci2.SimpleTypeOrder" :
            null; // "org.w3c.cci2.MultivaluedTypeOrder" no longer supported
    }

    // -----------------------------------------------------------------------
    /**
     * Maps a method name to a method name that does not collide with an
     * operation name in the root object class in the destination language (e.g.
     * java.lang.Object in Java). This mapping is needed to avoid that generated
     * methods accidentally override methods inherited from the root object
     * class.
     */
    public String getMethodName(
        String methodName
    ) {
        return Identifier.OPERATION_NAME.toIdentifier(methodName);
    }

    // -----------------------------------------------------------------------
    /**
     * Compose a method name
     * 
     * @param prependablePrefix
     * @param methodName
     * @param appendableSuffix
     * 
     * @return the resulting method name
     */
    public String getMethodName(
        String prependablePrefix, 
        String methodName,
        String appendableSuffix
    ) {
        return Identifier.OPERATION_NAME.toIdentifier(
            methodName,
            null, // removablePrefix
            prependablePrefix, 
            null, // removableSuffix 
            appendableSuffix            
        );
    }
    
    // -----------------------------------------------------------------------
    /**
     * Maps a parameter name to a parameter name that does not collide with a
     * keyword name in the destination language
     */
    public String getFeatureName(
        StructuralFeatureDef featureDef
    ) {
        return getFeatureName(featureDef.getName());
    }

    // -----------------------------------------------------------------------
    /**
     * Maps a parameter name to a parameter name that does not collide with a
     * keyword name in the destination language
     */
    protected String getFeatureName(
        String featureName
    ) {
        return Identifier.ATTRIBUTE_NAME.toIdentifier(featureName);
    }

    // -----------------------------------------------------------------------
    /**
     * 
     */
    public String getPredicateName(
        String prefix,
        StructuralFeatureDef fieldDef
    ){
        return Identifier.OPERATION_NAME.toIdentifier(
            (prefix == null ? "" : prefix + ' ') + fieldDef.getBeanGenericName()
        );
    }
    
    // -----------------------------------------------------------------------
    public void arrayToList(
        StructuralFeatureDef featureDef,
        String listName,
        String varName
    ) throws ServiceException {
        this.pw.println("    java.util.List " + listName + " = new java.util.ArrayList();");
        this.pw.println("    for(int i = 0; i < " + varName + ".length; i++) {");
        this.pw.println("      " + listName + ".add(");
        if (
            PrimitiveTypes.INTEGER.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.SHORT.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.LONG.equals(featureDef.getQualifiedTypeName()) || 
            PrimitiveTypes.BOOLEAN.equals(featureDef.getQualifiedTypeName()) 
        ) {
            this.pw.println("        new " + this.getObjectType(featureDef.getQualifiedTypeName()) + "(" + varName + "[i])");
        }
        else {
            this.pw.println("        " + varName + "[i]");
        }
        this.pw.println("      );");
        this.pw.println("    }");
    }
    
    // -----------------------------------------------------------------------
    public void fileHeader(
    ) {
        this.pw.println("//////////////////////////////////////////////////////////////////////////////");
        this.pw.println("//");
        this.pw.println("// Name: $Id: AbstractMapper.java,v 1.10 2011/07/08 13:20:51 wfro Exp $");
        this.pw.println("// Generated by: openMDX Java Mapper");
        this.pw.println("// Date: " + new Date().toString());
        this.pw.println("//");
        this.pw.println("// GENERATED - DO NOT CHANGE MANUALLY");
        this.pw.println("//");
        this.pw.println("//////////////////////////////////////////////////////////////////////////////");
    }

    // -----------------------------------------------------------------------
    public void trace(
        PrintWriter pw,
        String text
    ) {
        pw.println("// ----------------------------------------------------------------------------");
        pw.println("// " + text);
        pw.println("// ----------------------------------------------------------------------------");
    }

    // -----------------------------------------------------------------------
    public void trace(
        String text
    ) {
        this.trace(
            this.pw, 
            text
        );
    }
    
    // -----------------------------------------------------------------------
    protected ClassType getClassType(
        ClassDef classDef
    ) throws ServiceException{
        return 
            classDef.getStereotype().contains(Stereotypes.ROOT) ? ClassType.MIXIN :
            classDef.isAbstract() && classDef.getSuperClassDef(true) != null ? ClassType.EXTENSION :
            ClassType.OBJECT;
    }

    // -----------------------------------------------------------------------
    protected boolean isRoot(
        ModelElement_1_0 classDef
    ) throws ServiceException{
        return 
            classDef != null &&
            classDef.objGetList("stereotype").contains(Stereotypes.ROOT);
    }

    // -----------------------------------------------------------------------
    protected boolean isRoot(
        String qualifiedTypeName
    ) throws ServiceException{
        return isRoot(
            this.model.findElement(qualifiedTypeName)
        );
    }

    // -----------------------------------------------------------------------
    protected ClassDef getClassDef(
        String qualifiedName
    ) throws ServiceException{
        return new ClassDef(
            this.model.getElement(qualifiedName),
            this.model,
            this.metaData
            
        ); 
    }
    
    // -----------------------------------------------------------------------
    protected static boolean isDerived(
        ModelElement_1_0 modelElement
    ) throws ServiceException {
        return Boolean.TRUE.equals(modelElement.objGetValue("isDerived"));
    }
    
    // -----------------------------------------------------------------------
    private final String packageSuffix;
    private final Format format;    
    protected final MetaData_1_0 metaData;

}

// --- End of File -----------------------------------------------------------

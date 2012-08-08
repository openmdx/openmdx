/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TogetherImporter_1.java,v 1.2 2004/07/11 20:56:15 hburger Exp $
 * Description: model.importer.TogetherImporter
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/07/11 20:56:15 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.model1.together.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.omg.model1.code.DirectionKind;
import org.omg.model1.code.ScopeKind;
import org.omg.model1.code.VisibilityKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Stereotypes;
import org.openmdx.model1.code.TogetherImporterErrorCodes;
import org.openmdx.model1.importer.spi.ModelImporter_1;

import com.togethersoft.openapi.ide.message.IdeMessageManagerAccess;
import com.togethersoft.openapi.ide.message.IdeMessageType;
import com.togethersoft.openapi.rwi.RwiDiagram;
import com.togethersoft.openapi.rwi.RwiMember;
import com.togethersoft.openapi.rwi.RwiModel;
import com.togethersoft.openapi.rwi.RwiModelAccess;
import com.togethersoft.openapi.rwi.RwiNode;
import com.togethersoft.openapi.rwi.RwiPackage;
import com.togethersoft.openapi.rwi.RwiProperty;
import com.togethersoft.openapi.sci.SciAttribute;
import com.togethersoft.openapi.sci.SciClass;
import com.togethersoft.openapi.sci.SciCodeBlock;
import com.togethersoft.openapi.sci.SciElement;
import com.togethersoft.openapi.sci.SciLanguage;
import com.togethersoft.openapi.sci.SciModel;
import com.togethersoft.openapi.sci.SciModelAccess;
import com.togethersoft.openapi.sci.SciModelPart;
import com.togethersoft.openapi.sci.SciOperation;
import com.togethersoft.openapi.sci.SciPackage;
import com.togethersoft.openapi.sci.SciParameter;
import com.togethersoft.openapi.sci.SciProperty;
import com.togethersoft.openapi.sci.enum.SciAttributeEnumeration;
import com.togethersoft.openapi.sci.enum.SciClassEnumeration;
import com.togethersoft.openapi.sci.enum.SciInheritanceEnumeration;
import com.togethersoft.openapi.sci.enum.SciOperationEnumeration;
import com.togethersoft.openapi.sci.enum.SciPackageEnumeration;
import com.togethersoft.openapi.sci.enum.SciParameterEnumeration;

public class TogetherImporter_1
  extends ModelImporter_1 {

  //------------------------------------------------------------------------

  /**
   * Constructs an importer for Together ControlCenter 5.5.
   *
   * @param header dataprovider service header.
   *
   * @param target dataprovider used to store objects. The created model1 objects
   *        are stored using the addSetRequest() operation.
   *
   * @param providerName to root path where the objects are stored is 
   *        'org::omg/model1/<providerName>.
   *
   */

  //------------------------------------------------------------------------
  public TogetherImporter_1(
    boolean autoCorrectionOn,
    boolean showWarnings
  ) throws ServiceException {
    this.autoCorrectionOn = autoCorrectionOn;
    this.showWarnings = showWarnings;
  }

  //---------------------------------------------------------------------------
  private Path toPath(
    String scope,
    String name
  ) throws ServiceException {
    SysLog.trace("scope=" + scope);
    SysLog.trace("name=" + name);
    if (scope.equals("")) {
  		SysLog.error("missing scope for element with name <" + name + ">");
      throw new ServiceException(
				BasicException.Code.DEFAULT_DOMAIN,
				TogetherImporterErrorCodes.MISSING_SCOPE, 
				new BasicException.Parameter[]{
					new BasicException.Parameter("name", name)
				},
				"found a reference to an element for which the scope is missing (name=" + name + ")" 
			);
    }
    String elementName = nameToPathComponent(name);
    return toElementPath(
      nameToPathComponent(scope),
      elementName.substring(elementName.lastIndexOf(":") + 1)
    );
  }

  //---------------------------------------------------------------------------
  private void processTogetherPackage(
    SciPackage aPackage
  ) throws ServiceException {

    SysLog.trace("Processing package " + aPackage.getQualifiedName());
    
    String qualifiedPackageName = javaToQualifiedName(aPackage.getQualifiedName());
    DataproviderObject modelPackage = new DataproviderObject(
      toPath(
        qualifiedPackageName, 
        qualifiedPackageName
      )
    );

    modelPackage.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PACKAGE);
    modelPackage.values("isAbstract").add(new Boolean(false));
    modelPackage.values("visibility").add(VisibilityKind.PUBLIC_VIS);
    String annotation = getPackageAnnotation(aPackage);
    if (annotation != null) { modelPackage.values("annotation").add(annotation); }

    createModelElement( 
      null,
      modelPackage
    );
  }

  //---------------------------------------------------------------------------
  private void addWorkaroundPrimitiveTypes(
  ) throws ServiceException {

    // workaround for primitive type org::w3c::short
    primitiveTypes.add(toPrimitiveType("org::w3c", "short"));
    DataproviderObject primitiveTypeDefShort = new DataproviderObject(
      new Path(toPrimitiveType("org::w3c", "short"))
    );
    primitiveTypeDefShort.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PRIMITIVE_TYPE);
    primitiveTypeDefShort.values("container").add(
      toPath(
        "org::w3c", 
        "org::w3c"
      )
    );
    primitiveTypeDefShort.values("isAbstract").add(new Boolean(false));
    primitiveTypeDefShort.values("visibility").add(VisibilityKind.PUBLIC_VIS);    
    this.createModelElement(null, primitiveTypeDefShort);

    // workaround for primitive type org::w3c::long
    primitiveTypes.add(toPrimitiveType("org::w3c", "long"));
    DataproviderObject modelPrimitiveTypeLong = new DataproviderObject(
      new Path(toPrimitiveType("org::w3c", "long"))
    );
    modelPrimitiveTypeLong.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PRIMITIVE_TYPE);
    modelPrimitiveTypeLong.values("container").add(
      toPath(
        "org::w3c", 
        "org::w3c"
      )
    );
    modelPrimitiveTypeLong.values("isAbstract").add(new Boolean(false));
    modelPrimitiveTypeLong.values("visibility").add(VisibilityKind.PUBLIC_VIS);    
    this.createModelElement(null, modelPrimitiveTypeLong);
  }
   
  //---------------------------------------------------------------------------
  /**
   * NOTE: a PrimitiveType does not have supertypes
   */
  private void processTogetherPrimitiveType(
    SciClass aClass
  ) throws ServiceException {

    SysLog.trace("Processing primitive type " + getQualifiedName(aClass));

    primitiveTypes.add(toClassType(aClass));

    // ModelPrimitiveType object
    DataproviderObject primitiveTypeDef = new DataproviderObject(
      toClassType(aClass)
    );

    // object_class
    primitiveTypeDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PRIMITIVE_TYPE);
    
    // container
    primitiveTypeDef.values("container").add(
      toPath(
        javaToQualifiedName(aClass.getContainingPackage().getQualifiedName()),
        javaToQualifiedName(aClass.getContainingPackage().getQualifiedName())
      )
    );

    /**
     * skip stereotype because its value 'Primitive'
     * was marked to note the difference between
     * ordinary classes and primitive types
     */

    // annotation
    String annotation = getClassAnnotation(aClass);
    if(annotation != null) { 
      primitiveTypeDef.values("annotation").add(annotation); 
    }

    // isAbstract attribute
    primitiveTypeDef.values("isAbstract").add(
      new Boolean(isAbstractClass(aClass))
    );
    
    // visibility attribute
    primitiveTypeDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    createModelElement(null, primitiveTypeDef);
  }

  //---------------------------------------------------------------------------
  private void processTogetherAliasType(
    SciClass aClass
  ) throws ServiceException {

    SysLog.trace("Processing alias type " + getQualifiedName(aClass));

    // ModelClass object
    DataproviderObject aliasTypeDef = new DataproviderObject(
      new Path(toClassType(aClass))
    );

    // object_class
    aliasTypeDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.ALIAS_TYPE
    );
    
    // container
    aliasTypeDef.values("container").add(
      toPath(
        javaToQualifiedName(aClass.getContainingPackage().getQualifiedName()),
        javaToQualifiedName(aClass.getContainingPackage().getQualifiedName())
      )
    );

    // annotation
    String annotation = getClassAnnotation(aClass);
    if(annotation != null) { 
      aliasTypeDef.values("annotation").add(annotation); 
    }

    // isAbstract attribute
    aliasTypeDef.values("isAbstract").add(
      new Boolean(isAbstractClass(aClass))
    );
    
    // visibility attribute
    aliasTypeDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    // isSingleton attribute
    aliasTypeDef.values("isSingleton").add(
      new Boolean(false)
    );

    // type
    SciAttributeEnumeration attributes = aClass.attributes();
    SciAttribute referencedType = attributes.nextSciAttribute();
    aliasTypeDef.values("type").add(
      new Path(toType(referencedType.getType().getCanonicalText()))
    );
    
    // create element
    this.createModelElement(
      null, 
      aliasTypeDef
    );

  }

  //---------------------------------------------------------------------------
  private void processTogetherStructureType(
    SciClass aClass
  ) throws ServiceException {

    SysLog.trace("Processing structure type " + getQualifiedName(aClass));

    // ModelClass object
    DataproviderObject structureTypeDef = new DataproviderObject(
      new Path(toClassType(aClass))
    );

    // object_class
    structureTypeDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.STRUCTURE_TYPE
    );
    
    // container
    structureTypeDef.values("container").add(
      toPath(
        javaToQualifiedName(aClass.getContainingPackage().getQualifiedName()),
        javaToQualifiedName(aClass.getContainingPackage().getQualifiedName())
      )
    );

    /**
     * skip stereotype because its value 'Struct'
     * was marked to note the difference between
     * ordinary classes and structure types
     */

    // annotation
    String annotation = getClassAnnotation(aClass);
    if(annotation != null) { 
      structureTypeDef.values("annotation").add(annotation); 
    }

    // supertype
    SortedSet superTypePaths = new TreeSet();
    Iterator superClassesIterator = getSuperClasses(aClass).iterator();
    while (superClassesIterator.hasNext()) {
      SciClass aSuperClass = (SciClass)superClassesIterator.next();
      superTypePaths.add(
        new Path(toClassType(aSuperClass))
      );
    }
    // add supertypes in sorted order
    for(
      Iterator it = superTypePaths.iterator();
      it.hasNext();
    ) {
      structureTypeDef.values("supertype").add(
        it.next()
      );
    }
        
    // isAbstract attribute
    structureTypeDef.values("isAbstract").add(
      new Boolean(isAbstractClass(aClass))
    );
    
    // visibility attribute
    structureTypeDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    // isSingleton attribute
    structureTypeDef.values("isSingleton").add(
      new Boolean(false)
    );

    this.createModelElement(
      null, 
      structureTypeDef
    );

    SciAttributeEnumeration attributes = aClass.attributes();
    while(attributes.hasMoreElements()) {
      this.processTogetherStructureField(
        attributes.nextSciAttribute(), 
        structureTypeDef
      );
    }        
  }

  //---------------------------------------------------------------------------
  private void processTogetherStructureField(
     SciAttribute anAttribute,
     DataproviderObject aContainer
  ) throws ServiceException {

    SysLog.trace("Processing structure field " + anAttribute.getName());

    DataproviderObject structureFieldDef = new DataproviderObject(
      new Path(aContainer.path().toString() + "::" + anAttribute.getName())
    );

    // object_class
    structureFieldDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.STRUCTURE_FIELD
    );
    
    // container
    structureFieldDef.values("container").add(aContainer.path());

    // maxLength attribute
    structureFieldDef.values("maxLength").add(
      new Integer(getAttributeMaxLength(anAttribute))
    );
    
    // multiplicity attribute
    structureFieldDef.values("multiplicity").add(
      getAttributeCardinality(anAttribute)
    );

    // annotation
    String annotation = getAttributeAnnotation(anAttribute);
    if(annotation != null) { 
      structureFieldDef.values("annotation").add(annotation); 
    }

    try {
      structureFieldDef.values("type").add(
        new Path(toType(anAttribute.getType().getCanonicalText()))
      );
    } catch (ServiceException ex) {
      throw new ServiceException(
      	new BasicException(
	        ex,
	        BasicException.Code.DEFAULT_DOMAIN,
	        ex.getExceptionCode(),
	        BasicException.Parameter.add(
	          ex.getExceptionStack().getParameters(),
	          new BasicException.Parameter[]{
	            new BasicException.Parameter("referenceType", "structure field"),
	            new BasicException.Parameter("where", structureFieldDef.path().toString())
	          }
	        ),
	        ex.getExceptionStack().getDescription()
	  	)
      );
    }
    this.createModelElement(
      null, 
      structureFieldDef
    );
  }
  
  //---------------------------------------------------------------------------
  private void processTogetherClass(
    SciClass aClass
  ) throws ServiceException {

    SysLog.trace("Processing class " + getQualifiedName(aClass));

    verifyCorrectnessOfNamespaceProperty(aClass);

    // ModelClass object
    DataproviderObject classDef = new DataproviderObject(
        new Path(toClassType(aClass))
    );

    // object_class
    classDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.CLASS);
    
    // container
    classDef.values("container").add(
      toPath(
        javaToQualifiedName(aClass.getContainingPackage().getQualifiedName()),
        javaToQualifiedName(aClass.getContainingPackage().getQualifiedName())
      )
    );

    // stereotype
    classDef.values("stereotype").addAll(
      this.getClassStereotypes(aClass)
    );

    // annotation
    String annotation = getClassAnnotation(aClass);
    if(annotation != null) { 
      classDef.values("annotation").add(annotation); 
    }

    // supertype
    SortedSet superTypePaths = new TreeSet();
    Iterator superClassesIterator = getSuperClasses(aClass).iterator();
    while (superClassesIterator.hasNext()) {
      SciClass aSuperClass = (SciClass)superClassesIterator.next();
      superTypePaths.add(new Path(toClassType(aSuperClass)));
    }
    // add supertypes in sorted order
    for(
      Iterator it = superTypePaths.iterator();
      it.hasNext();
    ) {
      classDef.values("supertype").add(
        it.next()
      );
    }
        
    // isAbstract attribute
    classDef.values("isAbstract").add(
      new Boolean(isAbstractClass(aClass))
    );
    
    // visibility attribute
    classDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    // isSingleton attribute
    classDef.values("isSingleton").add(
      new Boolean(false)
    );

    createModelElement(null, classDef);

    // operations
    SciOperationEnumeration operations = aClass.operations();
    while(operations.hasMoreElements()) {
      SciOperation anOperation = operations.nextSciOperation();
      processTogetherOperation(anOperation, classDef);
    }

    /**
     * attributes and associations
     * Note: inherited attributes and associations are omitted
     * Note: in Together 5.5 associations are implemented as ordinary attributes
     *       that start with the prefix "lnk"
     */
    SciAttributeEnumeration attributes = aClass.attributes();
    while(attributes.hasMoreElements()) {
      SciAttribute anAttribute = attributes.nextSciAttribute();
      if(anAttribute.getName().startsWith("lnk")) {
        // association
        processTogetherAssociation(anAttribute, aClass, classDef);
      } 
      else {
        // attribute
        processTogetherAttribute(anAttribute, classDef);
      }
    }        
  }

  //---------------------------------------------------------------------------
  private void processTogetherAttribute(
     SciAttribute anAttribute,
     DataproviderObject aContainer
  ) throws ServiceException {

    SysLog.trace("Processing attribute " + anAttribute.getName());

    DataproviderObject attributeDef = new DataproviderObject(
      new Path(aContainer.path().toString() + "::" + anAttribute.getName())
    );

    attributeDef.values("visibility").add(getAttributeVisibility(anAttribute));
    attributeDef.values("container").add(aContainer.path());
    attributeDef.values("maxLength").add(new Integer(getAttributeMaxLength(anAttribute)));
    attributeDef.values("isLanguageNeutral").add(new Boolean(isLanguageNeutralAttribute(anAttribute)));
    attributeDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ATTRIBUTE);

    boolean isChangeable = isChangeableAttribute(anAttribute);
    boolean isDerived = isDerivedAttribute(anAttribute);
    if(isDerived && isChangeable) { 
      SysLog.warning("attribute <" + attributeDef.path().toString() + "> is derived AND changeable (derived attributes are NEVER changeable)");
      warning("attribute <" + attributeDef.path().toString() + "> is derived AND changeable (derived attributes are NEVER changeable)");
    } 

		try {
	    attributeDef.values("type").add(
	      new Path(toType(anAttribute.getType().getCanonicalText()))
	    );
	  } catch (ServiceException ex) {
	  	throw new ServiceException(
	  	  new BasicException(
	  		ex,
				BasicException.Code.DEFAULT_DOMAIN,
				ex.getExceptionCode(),
        		BasicException.Parameter.add(
					ex.getExceptionStack().getParameters(),
					new BasicException.Parameter[]{
					  new BasicException.Parameter("referenceType", "attribute"),
					  new BasicException.Parameter("where", attributeDef.path().toString())
					}
				),
				ex.getExceptionStack().getDescription()
		  )
	  	);
	  }
	  
    attributeDef.values("uniqueValues").add(new Boolean(isUniqueAttribute(anAttribute)));
    attributeDef.values("multiplicity").add(getAttributeCardinality(anAttribute));
    attributeDef.values("scope").add(ScopeKind.INSTANCE_LEVEL);
    attributeDef.values("isDerived").add(new Boolean(isDerived));
    attributeDef.values("isChangeable").add(new Boolean(isChangeable));

    String annotation = getAttributeAnnotation(anAttribute);
    if(annotation != null) { 
      attributeDef.values("annotation").add(annotation); 
    }
    createModelElement(null, attributeDef);
  }
  
  //---------------------------------------------------------------------------
  private void processTogetherOperation(
    SciOperation anOperation,
    DataproviderObject aContainer
  ) throws ServiceException {

    SysLog.trace("Processing operation " + anOperation.getName());

    String operationName = anOperation.getName();
    DataproviderObject operationDef = new DataproviderObject(
      new Path(aContainer.path().toString() + "::" + anOperation.getName()) 
    );

    // object_class
    operationDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.OPERATION
    );

    // container
    operationDef.values("container").add(
      aContainer.path()
    );

    // stereotype
    Set stereotypes = this.getOperationStereotypes(anOperation);
    boolean isException = false;
    if(stereotypes.size() > 0) { 
      operationDef.values("stereotype").addAll(
        stereotypes
      ); 

      // well-known stereotype <<exception>>
      if(operationDef.values("stereotype").contains(Stereotypes.EXCEPTION)) {
		    operationDef.clearValues(SystemAttributes.OBJECT_CLASS).add(
		      ModelAttributes.EXCEPTION
		    );
        operationDef.clearValues("stereotype");
        isException = true;
      }      
    }

    // annotation
    String annotation = getOperationAnnotation(anOperation);
    if(annotation != null) { 
      operationDef.values("annotation").add(annotation); 
    }

    // visibility
    operationDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    // scope
    operationDef.values("scope").add(ScopeKind.INSTANCE_LEVEL);

    // isQuery
    operationDef.values("isQuery").add(
      new Boolean(isQueryOperation(anOperation))
    );

    // semantics
    String semantics = getOperationSemantics(anOperation);
    if(!semantics.equals("")) { 
      operationDef.values("semantics").add(semantics); 
    }

    // parameters
    SciParameterEnumeration parameters = anOperation.getParameterList().parameters();
    
    if(parameters.hasMoreElements()) {

      /**
       * In openMDX all operations have exactly one parameter with name 'in'. The importer
       * supports two forms how parameters may be specified:
       * 1) p0:t0, p1:t1, ..., pn:tn. In this case a class with stereotype <parameter> is created
       *    and p0, ..., pn are added as class attributes. Finally, a parameter with name 'in'
       *    is created with the created parameter type.
       * 2) in:t. In this case the the parameter with name 'in' is created with the specified type.
       */
      
      /**
       * Create the parameter type class. We need this class only in case 1. Because we only know
       * at the end whether we really need it, create it anyway but do not add it to the repository.
       */
	    String capOperationName = 
	      operationName.substring(0,1).toUpperCase() +
	      operationName.substring(1);
	      
	    DataproviderObject parameterType = new DataproviderObject(
	      new Path(
	        aContainer.path().toString() + capOperationName + "Params"
	      )
	    );
	    parameterType.values(SystemAttributes.OBJECT_CLASS).add(
	      ModelAttributes.STRUCTURE_TYPE
	    );  
	    parameterType.values("visibility").add(VisibilityKind.PUBLIC_VIS);
	    parameterType.values("isAbstract").add(new Boolean(false));
	    parameterType.values("container").addAll(
	      aContainer.values("container")
	    );

      /**
       * Create parameters either as STRUCTURE_FIELD of parameterType (case 1) 
       * or as PARAMETER of modelOperation (case 2)
       */
      boolean createParameterType = true;
      boolean parametersCreated = false;
      
	    while(parameters.hasMoreElements()) {
	      SciParameter aParameter = parameters.nextSciParameter();
	      
	      DataproviderObject parameterDef = this.processTogetherParameter(
	        aParameter, 
	        parameterType
	      );

        /**
         * Case 2: Parameter with name 'in'. Create object as PARAMETER.
         */
        String fullQualifiedParameterName = parameterDef.path().getBase();
        if("in".equals(fullQualifiedParameterName.substring(fullQualifiedParameterName.lastIndexOf(':') + 1))) {
        	
        	// 'in' is the only allowed parameter
        	if(parametersCreated) {
	          throw new ServiceException(
	            BasicException.Code.DEFAULT_DOMAIN,
	            BasicException.Code.ASSERTION_FAILURE, 
	            new BasicException.Parameter[]{
	            	new BasicException.Parameter("operation", operationDef)
	            },
	            "Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + ModelAttributes.STRUCTURE_TYPE
	          );        		
        	}
        	parameterType = new DataproviderObject(
        	  (Path)parameterDef.values("type").get(0)
        	);
        	createParameterType = false;
        }
        
        /**
         * Case 1: Parameter is attribute of parameter type. Create object as ATTRIBUTE.
         */
        else {
        	
        	// 'in' is the only allowed parameter
        	if(!createParameterType) {
	          throw new ServiceException(
	            BasicException.Code.DEFAULT_DOMAIN,
	            BasicException.Code.ASSERTION_FAILURE, 
	            new BasicException.Parameter[]{
	            	new BasicException.Parameter("operation", operationDef)
	            },
	            "Parameter format must be [p0:T0, ... ,pn:Tn | in:T], where T must be a class with stereotype " + ModelAttributes.STRUCTURE_TYPE
	          );        		
        	}
          this.createModelElement(
            null,
            parameterDef
        	);
        	parametersCreated = true;
        }
	      
	    }

      /**
       * Case 1: parameter type must be created
       */
      if(createParameterType) {
	      this.createModelElement(
	        null,
	        parameterType
	      );
      }

	    // in-parameter
      DataproviderObject inParameterDef = new DataproviderObject(
        new Path(
          operationDef.path().toString() + "::in"
        ) 
      );
      inParameterDef.values(SystemAttributes.OBJECT_CLASS).add(
        ModelAttributes.PARAMETER
      );  
      inParameterDef.values("container").add(
        operationDef.path()
      );  
      inParameterDef.values("direction").add(
        DirectionKind.IN_DIR
      );
      inParameterDef.values("multiplicity").add(
        "1..1"
      );
      inParameterDef.values("type").add(
        parameterType.path()
      );
      this.createModelElement(
        null, 
        inParameterDef
      );
    }

    // void in-parameter
    else {
      DataproviderObject inParameterDef = new DataproviderObject(
        new Path(
          operationDef.path().toString() + "::in"
        ) 
      );
      inParameterDef.values(SystemAttributes.OBJECT_CLASS).add(
        ModelAttributes.PARAMETER
      );  
      inParameterDef.values("container").add(
        operationDef.path()
      );  
      inParameterDef.values("direction").add(
        DirectionKind.IN_DIR
      );
      inParameterDef.values("multiplicity").add(
        "1..1"
      );
      inParameterDef.values("type").add(
        this.toPath(
          "org::openmdx::base",
          "Void"
        )
      );
      this.createModelElement(
        null, 
        inParameterDef
      );
    }

    // result
    DataproviderObject resultDef = new DataproviderObject(
      new Path(
        operationDef.path().toString() + "::result"
      ) 
    );
    resultDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.PARAMETER
    );  
    resultDef.values("container").add(
      operationDef.path()
    );  
    resultDef.values("direction").add(
      DirectionKind.RETURN_DIR
    );
    resultDef.values("multiplicity").add(
      "1..1"
    );

		try {
	    resultDef.values("type").add(
	      this.toType(
	        anOperation.getReturnType().getCanonicalText()
	      )
	    );
 	  } 
 	  catch(ServiceException ex) {
      resultDef.values("type").add(
        this.toPath(
          "org::openmdx::base",
          "Void"
        )
      );
		}
    
    if(!isException) {    	    
	    this.createModelElement(
	      null, 
	      resultDef
	    );
    }
    
    // exceptions
    StringTokenizer exceptions = new StringTokenizer(anOperation.getThrowList().getText(), ", ");
    while(exceptions.hasMoreTokens()) {
    	String qualifiedExceptionName = exceptions.nextToken();
    	String qualifiedClassName = qualifiedExceptionName.substring(0, qualifiedExceptionName.lastIndexOf(':'));
    	operationDef.values("exception").add(
    	  new Path(
	  	    this.toPath(
	  	      qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':')),
	  	      qualifiedClassName.substring(qualifiedClassName.lastIndexOf(':') + 1)
	  	    ).toString() + 
	        "::" + 
	        qualifiedExceptionName.substring(qualifiedExceptionName.lastIndexOf(':') + 1)
	      )
    	);
    }
    
    // operation
    this.createModelElement(
      null, 
      operationDef
    );

  }
  
  //---------------------------------------------------------------------------
  private DataproviderObject processTogetherParameter(
    SciParameter aParameter,
    DataproviderObject parameterType
  ) throws ServiceException {

    SysLog.trace("Processing parameter " + aParameter.getName());

    DataproviderObject parameterDef = new DataproviderObject(
      new Path(
        parameterType.path().toString() + "::" + aParameter.getName()
      ) 
    );
    
    // object_class
    parameterDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.STRUCTURE_FIELD
    );
    
    // container
    parameterDef.values("container").add(
      parameterType.path()
    );

    parameterDef.values("maxLength").add(new Integer(1000000));

    // type, multiplicity
    // type name is of the form: TypeName [ "[n..m]" ]
    String typeNameWithMultiplicity = aParameter.getType().getCanonicalText();
    String typeName = typeNameWithMultiplicity;
    String multiplicity = "1..1";
    int posOfMultiplicityQualifier = typeNameWithMultiplicity.indexOf('[');
    if(posOfMultiplicityQualifier >= 0) {
      typeName = typeNameWithMultiplicity.substring(
        0, 
        posOfMultiplicityQualifier
      );
      multiplicity = typeNameWithMultiplicity.substring(
        posOfMultiplicityQualifier + 1,
        typeNameWithMultiplicity.lastIndexOf(']')
      );
    }
    
    try {
	    parameterDef.values("type").add(
	      this.toType(typeName)
	    );
	  } catch (ServiceException ex) {
	  	throw new ServiceException(
	  	  new BasicException(
	  		ex,
				BasicException.Code.DEFAULT_DOMAIN,
				ex.getExceptionCode(), 
        		BasicException.Parameter.add(
					ex.getExceptionStack().getParameters(),
					new BasicException.Parameter[]{
					  new BasicException.Parameter("referenceType", "parameter"),
					  new BasicException.Parameter("where", parameterDef.path().toString())
					}
				),
				ex.getExceptionStack().getDescription()
			)
	  	);
		}

    parameterDef.values("multiplicity").add(multiplicity);

    return parameterDef;

  }

  //---------------------------------------------------------------------------
  private void processTogetherAssociation(
    SciAttribute anAttribute,
    SciClass aClientClass,
    DataproviderObject aContainer
  ) throws ServiceException {

    short associationType = getAssociationType(anAttribute);
    String associationName = getAssociationName(anAttribute);
    if(associationType == ASSOCIATION) {
      SysLog.trace("Processing association " + associationName + " (ASSOCIATION)");
    } 
    else if(associationType == COMPOSITION) {
      SysLog.trace("Processing association " + associationName + " (COMPOSITION)");
    } 
    else if(associationType == AGGREGATION) {
      SysLog.trace("Processing association " + associationName + " (AGGREGATION)");
    } 
    else {
      SysLog.trace("Processing association " + associationName + " (unknown association type!!!!!!)");
    }

    /** 
     * o create ModelAssocation
     * o create two ModelAssociationEnds
     * o create two ModelReferences
     * 
     * Note: In Together 5.5/6.0 associations are no first class entities, they
     *       are implemented as special attributes of a class. Therefore it
     *       is assumed that the association belongs to the same package as
     *       the corresponding client class (the class which owns the association
     *       attribute).
     */
		String clientScope = javaToQualifiedName(aClientClass.getContainingPackage().getQualifiedName());

		// check whether association name (label) is null
    if(associationName.length() == 0) {
      SysLog.error("the name of an aggregation or a composition cannot be null; please specify a name");
			throw new ServiceException(
				BasicException.Code.DEFAULT_DOMAIN,
				TogetherImporterErrorCodes.MISSING_ASSOCIATION_PROPERTY, 
				new BasicException.Parameter[]{
				  new BasicException.Parameter("missing BasicException.Parameter", "name"),
				  new BasicException.Parameter("client class", aClientClass.getName()),
				  new BasicException.Parameter("client role", getAssociationClientRoleName(anAttribute)),
				  new BasicException.Parameter("supplier class", getAssociationSupplierClass(anAttribute).getName()),
				  new BasicException.Parameter("supplier role", getAssociationSupplierRoleName(anAttribute))
				},
				"the name of an aggregation or a composition cannot be null; please specify a name"
			);
    }

		// check whether the client role is null
    if(this.getAssociationClientRoleName(anAttribute).length() == 0) {
      SysLog.error("the client role of an aggregation or a composition cannot be null; please specify a name");
			throw new ServiceException(
				BasicException.Code.DEFAULT_DOMAIN,
				TogetherImporterErrorCodes.MISSING_ASSOCIATION_PROPERTY, 
				new BasicException.Parameter[]{
				  new BasicException.Parameter("missing BasicException.Parameter", "client role"),
				  new BasicException.Parameter("association name", associationName),
				  new BasicException.Parameter("client class", aClientClass.getName()),
				  new BasicException.Parameter("client role", getAssociationClientRoleName(anAttribute)),
				  new BasicException.Parameter("supplier class", getAssociationSupplierClass(anAttribute).getName()),
				  new BasicException.Parameter("supplier role", getAssociationSupplierRoleName(anAttribute))
				},
				"the client role of an aggregation or a composition cannot be null; please specify a name"
			);
    }

		// check whether the supplier role is null
    if(this.getAssociationSupplierRoleName(anAttribute).length() == 0) {
      SysLog.error("the supplier role of an aggregation or a composition cannot be null; please specify a name");
			throw new ServiceException(
				BasicException.Code.DEFAULT_DOMAIN,
				TogetherImporterErrorCodes.MISSING_ASSOCIATION_PROPERTY, 
				new BasicException.Parameter[]{
				  new BasicException.Parameter("missing BasicException.Parameter", "supplier role"),
				  new BasicException.Parameter("association name", associationName),
				  new BasicException.Parameter("client class", aClientClass.getName()),
				  new BasicException.Parameter("client role", getAssociationClientRoleName(anAttribute)),
				  new BasicException.Parameter("supplier class", getAssociationSupplierClass(anAttribute).getName()),
				  new BasicException.Parameter("supplier role", getAssociationSupplierRoleName(anAttribute))
				},
				"the supplier role of an aggregation or a composition cannot be null; please specify a name"
			);
    }

    // ModelAssociation object
    DataproviderObject associationDef = new DataproviderObject(
      toPath(
        clientScope, 
        associationName
      )
    );

    // object_class
    associationDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ASSOCIATION);

    // container
    associationDef.values("container").add(
      toPath(
        javaToQualifiedName(aClientClass.getContainingPackage().getQualifiedName()),
        javaToQualifiedName(aClientClass.getContainingPackage().getQualifiedName())
      )
    );

    // annotation
    String annotation = getAssociationAnnotation(anAttribute);
    if(annotation != null) { 
      associationDef.values("annotation").add(annotation); 
    }
    
    // stereotype
    String stereotype = getAssociationStereotype(anAttribute);
    if(!stereotype.equals("")) { 
      associationDef.values("stereotype").addAll(
        this.parseStereotype(stereotype)
      ); 
    }

    associationDef.values("isAbstract").add(new Boolean(false));
    associationDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);
    associationDef.values("isDerived").add(
        new Boolean(isDerivedAssociation(anAttribute))
    );

    createModelElement(null, associationDef);

    // ModelAssociationEnds
    DataproviderObject associationEndDefClientSide = new DataproviderObject(
      new Path(associationDef.path().toString() + "::" + getAssociationClientRoleName(anAttribute)) 
    );
    DataproviderObject associationEndDefSupplierSide = new DataproviderObject(
      new Path(associationDef.path().toString() + "::" + getAssociationSupplierRoleName(anAttribute)) 
    );

    // object_class
    associationEndDefClientSide.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.ASSOCIATION_END
    );
    associationEndDefSupplierSide.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.ASSOCIATION_END
    );

    // add client and supplier constraints if necessary
    this.processTogetherConstraints(
      associationEndDefClientSide,
      getAssociationClientConstraints(anAttribute)
    );
    this.processTogetherConstraints(
      associationEndDefSupplierSide,
      getAssociationSupplierConstraints(anAttribute)
    );

    // container, isChangeable, name
    associationEndDefClientSide.values("container").add(
      associationDef.path()
    );
 	  associationEndDefClientSide.values("isChangeable").add(
      new Boolean(
        !getAssociationClientConstraints(anAttribute).contains("isFrozen")
      )
	  );
    associationEndDefClientSide.values("name").add(
      getAssociationClientRoleName(anAttribute)
    );

    associationEndDefSupplierSide.values("container").add(
      associationDef.path()
    );
    associationEndDefSupplierSide.values("isChangeable").add(
      new Boolean(
        !getAssociationSupplierConstraints(anAttribute).contains("isFrozen")
      )
    );
    associationEndDefSupplierSide.values("name").add(
      getAssociationSupplierRoleName(anAttribute)
    );
      
    /**
     * Qualifiers
     * NOTE:
     * To comply with RoseImporter we change qualifier assignments
     * client qualifier attributes now belong to the supplier side and
     * supplier qualifier attributes now belong to the client side
     */
		try {
      Map qualifierAttributes = parseAssociationQualifierAttributes(
        getAssociationSupplierQualifier(anAttribute)
      );
      Iterator qualifierIterator = qualifierAttributes.entrySet().iterator();
      while (qualifierIterator.hasNext()) {
        Map.Entry qualifier = (Map.Entry)qualifierIterator.next();
        associationEndDefClientSide.values("qualifierName").add((String)qualifier.getKey());
        associationEndDefClientSide.values("qualifierType").add(
          new Path(toType((String)qualifier.getValue()))
        );
      }
      
      qualifierAttributes = parseAssociationQualifierAttributes(
        getAssociationClientQualifier(anAttribute)
      );
      qualifierIterator = qualifierAttributes.entrySet().iterator();
      while(qualifierIterator.hasNext()) {
        Map.Entry qualifier = (Map.Entry)qualifierIterator.next();
        associationEndDefSupplierSide.values("qualifierName").add((String)qualifier.getKey());
        associationEndDefSupplierSide.values("qualifierType").add(
            new Path(toType((String)qualifier.getValue()))
        );
      }
 	  } catch (ServiceException ex) {
	  	throw new ServiceException(
	  	  new BasicException(
	  		ex,
			BasicException.Code.DEFAULT_DOMAIN,
			ex.getExceptionCode(), 
	        BasicException.Parameter.add(
				ex.getExceptionStack().getParameters(),
				new BasicException.Parameter[]{
				  new BasicException.Parameter("referenceType", "qualifier type in association"),
				  new BasicException.Parameter("where", associationDef.path().toString())
				}
			),
			ex.getExceptionStack().getDescription()
		  )
  		);
	}

    // multiplicity
    associationEndDefClientSide.values("multiplicity").add(
      getAssociationClientCardinality(anAttribute)
    );
    associationEndDefSupplierSide.values("multiplicity").add(
      getAssociationSupplierCardinality(anAttribute)
    );
    
    // type
    associationEndDefClientSide.values("type").add(
      new Path(toClassType(aClientClass))
    );
    associationEndDefSupplierSide.values("type").add(
      new Path(toClassType(getAssociationSupplierClass(anAttribute)))
    );
    
    associationEndDefClientSide.values("isNavigable").add(
      new Boolean(!isDirectedAssociation(anAttribute))
    );
    associationEndDefSupplierSide.values("isNavigable").add(
      new Boolean(isDirectedAssociation(anAttribute) || isUndirectedAssociation(anAttribute))
    );

    if(associationType == COMPOSITION) {
      associationEndDefSupplierSide.values("aggregation").add(AggregationKind.COMPOSITE);
    } else if(associationType == AGGREGATION) {
      associationEndDefSupplierSide.values("aggregation").add(AggregationKind.SHARED);
    } else {
      associationEndDefSupplierSide.values("aggregation").add(AggregationKind.NONE);
    }
    
    // The aggregation type for the association end on the client side is always
    // set to AggregationKind.NONE. This is due to the fact that in Together 
    // 5.5/6.0 associations are no first class entities, they are implemented as
    // special attributes of a class (the client side class).
    associationEndDefClientSide.values("aggregation").add(AggregationKind.NONE);

    this.verifyAndCompleteAssociationEnds(
      associationEndDefClientSide,
      associationEndDefSupplierSide
    );
    this.exportAssociationEndAsReference(
      associationEndDefClientSide,
      associationEndDefSupplierSide,
      associationDef,
      null
    );
    this.exportAssociationEndAsReference(
      associationEndDefSupplierSide,
      associationEndDefClientSide,
      associationDef,
      null
    );
    this.createModelElement(null, associationEndDefClientSide);
    this.createModelElement(null, associationEndDefSupplierSide);      
  }
  
  //---------------------------------------------------------------------------
  private void processTogetherConstraints(
    DataproviderObject associationEndDef,
    Collection constraints
  ) throws ServiceException {
    for(
      Iterator i = constraints.iterator();
      i.hasNext();
    ) {

      // add new constraint
      DataproviderObject associationEndDefConstraint = new DataproviderObject(
        new Path(
          associationEndDef.path().toString() + (String)i.next()
        ) 
      );

      // object_class
      associationEndDefConstraint.values(SystemAttributes.OBJECT_CLASS).add(
        ModelAttributes.CONSTRAINT
      );

      // container
      associationEndDefConstraint.values("container").add(
        associationEndDef.path()
      );
         
      this.createModelElement(
        null,
        associationEndDefConstraint
      );
    }
  }
  
  //---------------------------------------------------------------------------
  private Set extractClassesFromPackages(
    SciPackageEnumeration packages
  ) {
    HashSet set = new HashSet();

    while (packages.hasMoreElements()) {
      SciPackage nextPackage = packages.nextSciPackage();
      SciClassEnumeration classes = nextPackage.classes();
      while (classes.hasMoreElements()) {
        SciClass cl = classes.nextSciClass();
        set.add(cl);
      }
      // extract classes from nested subpackages
      set.addAll(extractClassesFromPackages(nextPackage.subpackages()));
    }
    return set;
  }

  //---------------------------------------------------------------------------
  /**
   * returns the full qualified class name in Java syntax,
   * i.e. [ pkg { "." pkg } "." ] Class
   */
  private String getQualifiedName(
    SciClass aClass
  ) {
    SciPackage pkg = aClass.getContainingPackage();

    // class is in root package => return class name directly
    if(pkg.getName().equals("<default>")) {
      return (aClass.getName());
    } 
    else {
      String qualifiedName = pkg.getName();
      while(pkg.getParentPackage() != null) {

        // omit name of root package
        pkg = pkg.getParentPackage();
        if(!pkg.getName().equals("<default>")) {
          qualifiedName = pkg.getName() + "." + qualifiedName;
        }
      }
      return (qualifiedName + "." + aClass.getName());
    }
  }

  //---------------------------------------------------------------------------
  /**
   * ensure that multiplicity/cardinality ranges conform to syntax convention,
   * e.g. "1..1", "0..n"
   * Note: Together syntax "1" or "0..*" is converted
   */
  private String convertCardinality(
    String range
  ) {
    if(range.equals("1")) { 
      return "1..1"; 
    }
    else {
      return(range.replace('*', 'n'));
    }
  }
  
  //---------------------------------------------------------------------------
  /**
   * takes a class and returns the associated openMDX class name
   * e.g. org::omg::model1/provider/Together/segment/org::omg::model1/element/org::omg::model1::ModelOperation
   */
  private Path toClassType(
    SciClass aClass
  ) throws ServiceException {
    String scope = javaToQualifiedName(aClass.getContainingPackage().getQualifiedName());
    String className = aClass.getName();
    return toPath(scope, className);
  }

  //---------------------------------------------------------------------------
  /**
   * takes a class and returns the associated openMDX class name
   * e.g. org::omg::model1/provider/<providerName>/segment/org::omg::model1/element/org::omg::model1::ModelOperation
   */
  private Path toPrimitiveType(
    String scope, 
    String unqualifiedPrimitiveTypeName
  ) throws ServiceException {
    return toPath(
      scope, 
      unqualifiedPrimitiveTypeName
    );
  }

  //---------------------------------------------------------------------------
  /**
   * takes the name of a primitive type (in fully qualified MOF syntax)
   * e.g. org::w3c::string
   * or an unqualified or qualified (in MOF syntax) class name
   * e.g. ModelOperation, org::omg::model1::ModelOperation
   * and converts it to a openMDX classifier name
   * e.g. org::omg::model1/provider/<providerName>/segment/org::w3c/element/org::w3c::string
   */
  private Path toType(
   String classifier
  ) throws ServiceException {

    // check whether the classifier denotes a class
    SciClass aClass = getClass(classifier);
    if((aClass != null) && !(this.getClassStereotypes(aClass).contains(Stereotypes.PRIMITIVE))) {
      return this.toClassType(aClass);
    }
    else {
      // classifier does not denote a class, therefore it must be a primitive type

	    // extract unqualified classifier
	    String unqualifiedClassifier = new String();
	    int i = classifier.length()-1;
	    while((i >= 0) && (classifier.charAt(i) != ':')) {
	      unqualifiedClassifier = classifier.charAt(i) + unqualifiedClassifier;
	      i--;
	    }

      // extract scope
      String scope = new String("");
      if (i > 0) {
        scope = classifier.substring(0, i-1); 
      }

      // classifier is a valid primitive type
      if(primitiveTypes.contains(toPrimitiveType(scope, unqualifiedClassifier))) {  
        return toPrimitiveType(
          scope, 
          unqualifiedClassifier
        );
      } 
      else {
        SysLog.error("the classifier <" + classifier + "> is neither a valid class name nor a valid primitive type");
        throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN,
					TogetherImporterErrorCodes.UNKNOWN_CLASSIFIER, 
					new BasicException.Parameter[]{
					  new BasicException.Parameter("unknownClassifier", classifier)
					},
					"the classifier <" + classifier + "> is neither a valid class name nor a valid primitive type"
				);
      } 
    }
  }

  //---------------------------------------------------------------------------
  /**
   * convert qualified names for classes, packages, ... in MOF syntax
   * (e.g. org::openmdx::example::MyClass) into Java syntax (e.g. org.openmdx.example.MyClass)
   */
  private String qualifiedNameToJava(
    String qualifiedName
  ) {
    SysLog.trace("call of qualifiedNameToJava for value <" + qualifiedName + ">");
    String javaQualifiedName = new String();
    for(
      int i = 0; 
      i < qualifiedName.length(); 
      i++
    ) {
      char ch = qualifiedName.charAt(i);
      if(ch == ':') {
        if((i+1 < qualifiedName.length()) && (qualifiedName.charAt(i+1) == ':')) { 
          javaQualifiedName = javaQualifiedName + '.';
          i++;
        } 
       else {
          javaQualifiedName = javaQualifiedName + ':';
        }
      }
      else {
        javaQualifiedName = javaQualifiedName + ch; 
      }
    }
    return javaQualifiedName;
  }
  
  //---------------------------------------------------------------------------
  /**
   * convert qualified names for classes, packages, ... in MOF syntax
   * (e.g. org::openmdx::example::MyClass) into Java syntax (e.g. org.openmdx.example.MyClass)
   */
  private String javaToQualifiedName(
    String javaQualifiedName
  ) {
    SysLog.trace("call of javaToQualifiedName for value <" + javaQualifiedName + ">");
    String qualifiedName = new String();
    for(
      int i = 0; 
      i < javaQualifiedName.length(); 
      i++
    ) {
      char ch = javaQualifiedName.charAt(i);
      if(ch == '.') { 
        qualifiedName = qualifiedName + "::"; 
      }
      else { 
        qualifiedName = qualifiedName + ch; 
      }
    }
    return qualifiedName;
  }

  //---------------------------------------------------------------------------
  /**
   * try to find the desired class based on its name and answer null if
   * the desired class does not exist
   * Note: in Together this is project language specific 8-(
   */
  private SciClass getClass(
    String className
  ) {

    // try to find the class if it was modeled bases on project language Java
    SciClass theClass = SciModelAccess.getModel().findClass(SciLanguage.JAVA, className);

    // no success so far, try to find the class if it was modeled based on project language C++
    if(theClass == null) {
      theClass = SciModelAccess.getModel().findClass(SciLanguage.CPP, className);
    }

    // no success so far, try to find the class if it was modeled based on project language CORBA IDL
    if(theClass == null) {
      theClass = SciModelAccess.getModel().findClass(SciLanguage.IDL, className);
    }

    // no success so far, try to find the class if it was modeled based on project language C#
    if(theClass == null) {
      theClass = SciModelAccess.getModel().findClass(SciLanguage.CSHARP, className);
    }

    // no success so far, try to find the class if it was modeled based on project language Visual Basic
    if(theClass == null) {
      theClass = SciModelAccess.getModel().findClass(SciLanguage.VBASIC, className);
    }

    // could not find the desired super class
    return theClass;
  }

  //---------------------------------------------------------------------------
  /**
   * skip root packages, 
   * Note: a root package can only contain other packages and
   * nothing else (no classes, ...)
   */
  private Set getAllPackages(
  ) {
    SciModel model = SciModelAccess.getModel();
    SciPackageEnumeration rootPackages = model.rootPackages(SciModelPart.MODEL);
    
    HashSet set = new HashSet();
    while(rootPackages.hasMoreElements()) {
      SciPackage nextPackage = rootPackages.nextSciPackage();
      // traverse nested subpackages
      set.addAll(traversePackages(nextPackage.subpackages()));
    }
    return set;
  }

  //---------------------------------------------------------------------------
  private Set traversePackages(
    SciPackageEnumeration packages
  ) {
    HashSet set = new HashSet();
    while(packages.hasMoreElements()) {
      SciPackage nextPackage = packages.nextSciPackage();
      set.add(nextPackage);

      // traverse nested subpackages
      set.addAll(traversePackages(nextPackage.subpackages()));
    }
    return set;
  }

  //---------------------------------------------------------------------------
  // get all classes in all packages
  private Set getAllClasses(
  ) {
    SciModel model = SciModelAccess.getModel();
    SciPackageEnumeration rootPackages = model.rootPackages(SciModelPart.MODEL);

    Set set = extractClassesFromPackages(rootPackages);
    return set;
  }

  //---------------------------------------------------------------------------
  private Set getSuperClasses(
    SciClass aClass
  ) {
    HashSet set = new HashSet(1,1);
    SciInheritanceEnumeration inhes = aClass.inheritances();

    while(inhes.hasMoreElements()) {
      // try to find the desired super class based on its name
      SciClass superClass = getClass(inhes.nextSciInheritance().getName());
      if (superClass != null) { 
        set.add(superClass); 
      }
    }
    return set;
  }

  //---------------------------------------------------------------------------
//  /**
//   * really ugly!!!!!! 
//   * since it is not possible in Together to ask a class for its subclasses
//   * we have to use this inefficient workaround to determine the subclasses
//   * for a certain class
//   */
//  private Set getSubClasses(
//    SciClass aClass
//  ) {
//    Set subClasses = new HashSet();
//    Set allClasses = getAllClasses();
//    Iterator it = allClasses.iterator();
//    while (it.hasNext()) {
//      SciClass currentClass = (SciClass)it.next();
//      Set superClasses = getSuperClasses(currentClass);
//      Iterator it2 = superClasses.iterator();
//      while (it2.hasNext()) {
//          SciClass currentSuperClass = (SciClass)it2.next();
//          if (currentSuperClass == aClass) { subClasses.add(currentClass); }
//      }
//    }
//    return subClasses;
//  }

  //---------------------------------------------------------------------------
//  private Set getRootClasses(
//    SciClass aClass
//  ) {
//    HashSet set = new HashSet(1,1);
//
//    Set superClasses = getSuperClasses(aClass);
//    Iterator it = superClasses.iterator();
//    if (!it.hasNext()) {
//        // this class has no super classes at all, the class is root class itself
//        set.add(aClass);
//    }
//    while (it.hasNext()) {
//        SciClass aSuperClass = (SciClass)it.next();
//        set.addAll(getRootClasses(aSuperClass));
//    }
//    return set;
//  }

  //---------------------------------------------------------------------------
  private void autoCorrectMissingTags(
    SciElement element, 
    String tagName, 
    String tagValue
  ) {
    if(autoCorrectionOn) {
      element.getTagList().setTagValue(tagName, tagValue);
      SysLog.trace("auto correction: added missing tag <" + tagName + "> with value <" + tagValue + ">");
      info("auto correction: added missing tag <" + tagName + "> with value <" + tagValue + ">");
    }
  }

  //---------------------------------------------------------------------------
  /**
   * derived attributes can be marked by the user and detected by this
   * Model Exporter module only if the following properties are set in the
   * Together Inspector. This can be done by manually adding the following
   * two lines to the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.Attribute.item.openmdx.item.isDerived = ChoiceField( { values := { "false", "true" } } )
   * inspector.node.element.*.Attribute.item.openmdx.item.isDerived.name = isDerived
   *
   * If you want to have a graphical representation (i.e. "/") of this (this
   * is not a Together standard feature), you can add the following two
   * lines to the file %TGH%/config/view.config
   *
   * \  derived = getProperty("isDerived");
   * \  if (derived == "true", name = "/ " + name);
   *
   * Please note that these two lines have to be added directly below the
   * following line
   * \  name=formatAttributeName(name);
   * in the view.config file
   */
  private boolean isDerivedAttribute(
    SciAttribute att
  ) {
    String tagValue = att.getTagList().getTagValue("isDerived");
    if(tagValue == null) {
      SysLog.warning("the BasicException.Parameter 'isDerived' for attribute <" + att.getName() + "> is not set (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isDerived' for attribute <" + att.getName() + "> is not set (using value \"false\" as default value)");
      autoCorrectMissingTags(att, "isDerived", "false");
      return false;
    }
    else if(tagValue.equals("true")) { 
      return true; 
    }
    else if(tagValue.equals("false")) { 
      return false; 
    }
    else {
      SysLog.warning("the BasicException.Parameter 'isDerived' for attribute <" + att.getName() + "> uses an unknown value (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isDerived' for attribute <" + att.getName() + "> uses an unknown value (using value \"false\" as default value)");
      autoCorrectMissingTags(att, "isDerived", "false");
      return false;
    }
  }

  //---------------------------------------------------------------------------
  /**
   * changeable attributes can be marked by the user and detected by this
   * Model Exporter module only if the following properties are set in the
   * Together Inspector. This can be done by manually adding the following
   * two lines to the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.Attribute.item.openmdx.item.isChangeable = ChoiceField( { values := { "false", "true" } } )
   * inspector.node.element.*.Attribute.item.openmdx.item.isChangeable.name = isChangeable
   */
  private boolean isChangeableAttribute(
    SciAttribute att
  ) {
    String tagValue = att.getTagList().getTagValue("isChangeable");
    if(tagValue == null) {
      SysLog.warning("the BasicException.Parameter 'isChangeable' for attribute <" + att.getName() + "> is not set (using value \"true\" as default value)");
      warning("the BasicException.Parameter 'isChangeable' for attribute <" + att.getName() + "> is not set (using value \"true\" as default value)");
      autoCorrectMissingTags(att, "isChangeable", "true");
      return true;
    }
    else if(tagValue.equals("true")) { 
      return true; 
    }
    else if(tagValue.equals("false")) { 
      return false; 
    }
    else {
      SysLog.warning("the BasicException.Parameter 'isChangeable' for attribute <" + att.getName() + "> uses an unknown value (using value \"true\" as default value)");
      warning("the BasicException.Parameter 'isChangeable' for attribute <" + att.getName() + "> uses an unknown value (using value \"true\" as default value)");
      autoCorrectMissingTags(att, "isChangeable", "true");
      return true;
    }
  }

  //---------------------------------------------------------------------------
  /**
   * language neutral attributes can be marked by the user and detected by this
   * Model Exporter module only if the following properties are set in the
   * Together Inspector. This can be done by manually adding the following
   * two lines to the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.Attribute.item.openmdx.item.isLanguageNeutral = ChoiceField( { values := { "false", "true" } } )
   * inspector.node.element.*.Attribute.item.openmdx.item.isLanguageNeutral.name = isLanguageNeutral
   */
  private boolean isLanguageNeutralAttribute(
    SciAttribute att
  ) {
    String tagValue = att.getTagList().getTagValue("isLanguageNeutral");
    if(tagValue == null) {
      SysLog.warning("the BasicException.Parameter 'isLanguageNeutral' for attribute <" + att.getName() + "> is not set (using value \"true\" as default value)");
      warning("the BasicException.Parameter 'isLanguageNeutral' for attribute <" + att.getName() + "> is not set (using value \"true\" as default value)");
      autoCorrectMissingTags(att, "isLanguageNeutral", "true");
      return true;
    }
    else if(tagValue.equals("true")) { 
      return true; 
    }
    else if(tagValue.equals("false")) { 
      return false; 
    }
    else {
      SysLog.warning("the BasicException.Parameter 'isLanguageNeutral' for attribute <" + att.getName() + "> uses an unknown value (using value \"true\" as default value)");
      warning("the BasicException.Parameter 'isLanguageNeutral' for attribute <" + att.getName() + "> uses an unknown value (using value \"true\" as default value)");
      autoCorrectMissingTags(att, "isLanguageNeutral", "true");
      return true;
    }
  }

  //---------------------------------------------------------------------------
  /**
   * unique attributes can be marked by the user and detected by this
   * Model Exporter module only if the following properties are set in the
   * Together Inspector. This can be done by manually adding the following
   * two lines to the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.Attribute.item.openmdx.item.isUnique = ChoiceField( { values := { "false", "true" } } )
   * inspector.node.element.*.Attribute.item.openmdx.item.isUnique.name = isUnique
   */
  private boolean isUniqueAttribute(
    SciAttribute att
  ) {
    String tagValue = att.getTagList().getTagValue("isUnique");
    if(tagValue == null) {
      SysLog.warning("the BasicException.Parameter 'isUnique' for attribute <" + att.getName() + "> is not set (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isUnique' for attribute <" + att.getName() + "> is not set (using value \"false\" as default value)");
      autoCorrectMissingTags(att, "isUnique", "false");
      return false;
    }
    else if(tagValue.equals("true")) { 
      return true; 
    }
    else if(tagValue.equals("false")) { 
      return false; 
    }
    else {
      SysLog.warning("the BasicException.Parameter 'isUnique' for attribute <" + att.getName() + "> uses an unknown value (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isUnique' for attribute <" + att.getName() + "> uses an unknown value (using value \"false\" as default value)");
      autoCorrectMissingTags(att, "isUnique", "false");
      return false;
    }
  }

  //---------------------------------------------------------------------------
  /*
   * MaxLength attributes can be marked by the user and detected by this
   * Model Exporter module only if the following properties are set in the
   * Together Inspector. This can be done by manually adding the following
   * two lines to the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.Attribute.item.openmdx.item.maxLength = StringField
   * inspector.node.element.*.Attribute.item.openmdx.item.maxLength.name = maxLength
   */
  private int getAttributeMaxLength(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("maxLength");
    if(val == null) { 
      SysLog.warning("the BasicException.Parameter 'maxLength' for attribute <" + att.getName() + "> is not set (using value \"" + DEFAULT_ATTRIBUTE_MAX_LENGTH + "\" as default value)");
      warning("the BasicException.Parameter 'maxLength' for attribute <" + att.getName() + "> is not set (using value \"" + DEFAULT_ATTRIBUTE_MAX_LENGTH + "\" as default value)");
      autoCorrectMissingTags(att, "maxLength", Integer.toString(DEFAULT_ATTRIBUTE_MAX_LENGTH));
      return DEFAULT_ATTRIBUTE_MAX_LENGTH; 
    }
    else {
      try {
        return Integer.parseInt(val);
      } catch (NumberFormatException ex) {
        SysLog.warning("the value for the BasicException.Parameter 'maxLength' for attribute <" + att.getName() + "> is not set correctly (using value \"" + DEFAULT_ATTRIBUTE_MAX_LENGTH + "\" as default value)");
        warning("the value for the BasicException.Parameter 'maxLength' for attribute <" + att.getName() + "> is not set correctly (using value \"" + DEFAULT_ATTRIBUTE_MAX_LENGTH + "\" as default value)");
        return DEFAULT_ATTRIBUTE_MAX_LENGTH;
      }
    }
  }

  //---------------------------------------------------------------------------
  private String getAttributeCardinality(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("stereotype");
    if(val == null) {
      return (convertCardinality("1..1"));
    }
    else { 
      return (convertCardinality(val)); 
    }
  }

  //---------------------------------------------------------------------------
  private String getAttributeVisibility(
    SciAttribute att
  ) {
    SysLog.trace("attribute.hasProperty(PRIVATE)=" + att.hasProperty(SciProperty.PRIVATE));
    SysLog.trace("attribute.hasProperty(PUBLIC)=" + att.hasProperty(SciProperty.PUBLIC));
    if(att.hasProperty(SciProperty.PRIVATE)) {
      return VisibilityKind.PRIVATE_VIS;
    }
    if(att.hasProperty(SciProperty.PUBLIC)) {
      return VisibilityKind.PUBLIC_VIS;
    }
    return VisibilityKind.PUBLIC_VIS;
  }

  //---------------------------------------------------------------------------
  private String getAssociationName(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("label");
    if (val == null) { 
      return ""; 
    }
    else { 
      return (val); 
    }
  }

  //---------------------------------------------------------------------------
  private short getAssociationType(
    SciAttribute att
  ) {
    String link = att.getTagList().getTagValue("link");
    if(link == null) { 
      return ASSOCIATION; 
    }
    else if(link.equals("aggregation")) { 
      return AGGREGATION; 
    }
    else if(link.equals("aggregationByValue")) { 
      return COMPOSITION;  
    }
    else { 
      return ASSOCIATION; 
    }
  }

  //---------------------------------------------------------------------------
  private SciClass getAssociationSupplierClass(
    SciAttribute att
  ) throws ServiceException {
		SciClass referencedClass = (SciClass) att.getType().getReferencedElement();
    
    if (referencedClass == null) {
      SysLog.error("invalid class specified for association supplier class (hint: verify class for lnk... BasicException.Parameter)");
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        TogetherImporterErrorCodes.INVALID_SUPPLIER_CLASS, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("attribute", att.getName()),
          new BasicException.Parameter("class", att.getContainingClass().getQualifiedName())
        },
        "invalid class specified for association supplier class (hint: verify class for lnk... BasicException.Parameter)"
      );
    }
    
    return referencedClass;
  }

  //---------------------------------------------------------------------------
  private String getAssociationClientRoleName(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("clientRole");
    if(val == null) {
      return ""; 
    }
    else { 
      return(val); 
    }
  }

  //---------------------------------------------------------------------------
  private String getAssociationSupplierRoleName(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("supplierRole");
    if(val == null) { 
      return ""; 
    }
    else { 
      return (val); 
    }
  }

  //---------------------------------------------------------------------------
  private String getAssociationClientCardinality(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("clientCardinality");
    if(val == null) { 
      return convertCardinality("1..1"); 
    }
    else { 
      return convertCardinality(val); 
    }
  }

  //---------------------------------------------------------------------------
  private String getAssociationSupplierCardinality(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("supplierCardinality");
    if(val == null) { 
      return convertCardinality("1..1"); 
    }
    else { 
      return convertCardinality(val); 
    }
  }

  //---------------------------------------------------------------------------
  private String getAssociationClientQualifier(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("clientQualifier");
    if(val == null) { 
      return ""; 
    }
    else { 
      return (val); 
    }
  }

  //---------------------------------------------------------------------------
  private String getAssociationSupplierQualifier(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("supplierQualifier");
    if(val == null) { 
      return ""; 
    }
    else { 
      return (val); 
    }
  }

  //---------------------------------------------------------------------------
  /**
   * NOTE
   * client constraints for an association can be marked by the user and 
   * detected only if the following properties are set in the Together 
   * Inspector. This can be done by manually adding the following two lines to
   * the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.AssociationLink.item.Properties.item.clientConstraints = StringField
   * inspector.node.element.*.AssociationLink.item.Properties.item.clientConstraints.name = client constraints (openMDX)
   *
   */
  private Collection getAssociationClientConstraints(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("clientConstraints");
    if(val == null) { 
      return new ArrayList(); 
    }
    else {
      ArrayList list = new ArrayList();
      StringTokenizer tokenizer = new StringTokenizer(val, ", \t");
      while (tokenizer.hasMoreTokens()) {
        list.add(tokenizer.nextToken());
      } 
      return list; 
    }
  }

  //---------------------------------------------------------------------------
  /**
   * NOTE
   * supplier constraints for an association can be marked by the user and 
   * detected only if the following properties are set in the Together 
   * Inspector. This can be done by manually adding the following two lines to
   * the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.AssociationLink.item.Properties.item.supplierConstraints = StringField
   * inspector.node.element.*.AssociationLink.item.Properties.item.supplierConstraints.name = supplier constraints (openMDX)
   *
   */
  private Collection getAssociationSupplierConstraints(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("supplierConstraints");
    if(val == null) { 
      return new ArrayList(); 
    }
    else {
      ArrayList list = new ArrayList();
      StringTokenizer tokenizer = new StringTokenizer(val, ", \t");
      while (tokenizer.hasMoreTokens()) {
        list.add(tokenizer.nextToken());
      } 
      return list; 
    }
  }

  //---------------------------------------------------------------------------
  private boolean isDirectedAssociation(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("directed");
    return (val != null);
  }

  //---------------------------------------------------------------------------
  private boolean isUndirectedAssociation(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("undirected");
    return (val != null);
  }

  //---------------------------------------------------------------------------
  /**
   * NOTE
   * derived attributes can be marked by the user and detected only if 
   * the following properties are set in the Together Inspector. This 
   * can be done by manually adding the following two lines to the 
   * file %TGH%\config\changes.config
   *
   * inspector.node.element.*.AssociationLink.item.openmdx.item.isDerived = ChoiceField( { values := { "false", "true" } } ) </li>
   * inspector.node.element.*.AssociationLink.item.openmdx.item.isDerived.name = isDerived </li>
   */
  private boolean isDerivedAssociation(SciAttribute att) {
    String tagValue = att.getTagList().getTagValue("isDerived");
    if (tagValue == null) {
      SysLog.warning("the BasicException.Parameter 'isDerived' for association <" + att.getName() + "> is not set (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isDerived' for association <" + att.getName() + "> is not set (using value \"false\" as default value)");
      autoCorrectMissingTags(att, "isDerived", "false");
      return false;
    }
    else if (tagValue.equals("true")) { return true; }
    else if (tagValue.equals("false")) { return false; }
    else {
      SysLog.warning("the BasicException.Parameter 'isDerived' for association <" + att.getName() + "> uses an unknown value (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isDerived' for association <" + att.getName() + "> uses an unknown value (using value \"false\" as default value)");
      autoCorrectMissingTags(att, "isDerived", "false");
      return false;
    }
  }

  //---------------------------------------------------------------------------
  private Set getClassStereotypes(
    SciClass aClass
  ) {
    String val = aClass.getTagList().getTagValue("stereotype");
    if(val == null) { 
      return new HashSet(); 
    }
    else { 
      return this.parseStereotype(val); 
    }
  }

  //---------------------------------------------------------------------------
  /**
   * isQuery operations can be marked by the user and detected by this
   * Model Exporter module only if the following properties are set in the
   * Together Inspector. This can be done by manually adding the following
   * two lines to the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.Operation.item.Properties.item.isQuery = ChoiceField( { values := { "false", "true" } } )
   * inspector.node.element.*.Operation.item.Properties.item.isQuery.name = isQuery (openMDX)
   */
  private boolean isQueryOperation(
    SciOperation op
  ) {
    String tagValue = op.getTagList().getTagValue("isQuery");
    if(tagValue == null) {
      SysLog.warning("the BasicException.Parameter 'isQuery' for operation <" + op.getName() + "> is not set (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isQuery' for operation <" + op.getName() + "> is not set (using value \"false\" as default value)");
      autoCorrectMissingTags(op, "isQuery", "false");
      return false;
    }
    else if(tagValue.equals("true")) { 
      return true; 
    }
    else if(tagValue.equals("false")) { 
      return false; 
    }
    else {
      SysLog.warning("the openMDX BasicException.Parameter 'isQuery' for operation <" + op.getName() + "> uses an unknown value (using value \"false\" as default value)");
      warning("the openMDX BasicException.Parameter 'isQuery' for operation <" + op.getName() + "> uses an unknown value (using value \"false\" as default value)");
      autoCorrectMissingTags(op, "isQuery", "false");
      return false;
    }
  }

  //---------------------------------------------------------------------------
  private Set getOperationStereotypes(
    SciOperation op
  ) {
    String val = op.getTagList().getTagValue("stereotype");
    if(val == null) { 
      return new HashSet(); 
    }
    else { 
      return this.parseStereotype(val);
    }
  }

  //---------------------------------------------------------------------------
  private String getOperationSemantics(
    SciOperation op
  ) {
    SciCodeBlock val = op.getBody();
    if(val == null) { 
      return ""; 
    }
    else { 
      return (val.getText()); 
    }
  }

  //---------------------------------------------------------------------------
  private String getAssociationStereotype(
    SciAttribute att
  ) {
    String val = att.getTagList().getTagValue("stereotype");
    if(val == null) { 
      return ""; 
    }
    else { 
      return (val); 
    }
  }

  //---------------------------------------------------------------------------
  private String getPackageAnnotation(
    SciPackage sciPackage
  ) {
    RwiModel model = RwiModelAccess.getModel();
    RwiPackage rwiPackage = model.findPackage(sciPackage);
    if(rwiPackage != null) {
      RwiDiagram rwiDiagram = rwiPackage.getPhysicalDiagram();
      if(rwiDiagram != null) { 
        return (rwiDiagram.getProperty(RwiProperty.DOC)); 
      }
    }
    return null;
  }

  //---------------------------------------------------------------------------
  private String getClassAnnotation(
    SciClass sciClass
  ) {
    RwiModel model = RwiModelAccess.getModel();
    RwiNode rwiNode = model.findNode(sciClass);
    if(rwiNode != null) { 
      return (rwiNode.getProperty(RwiProperty.DOC));
    }
    return null;
  }

  //---------------------------------------------------------------------------
  private String getAttributeAnnotation(
    SciAttribute sciAttribute
  ) {
    RwiModel model = RwiModelAccess.getModel();
    RwiMember rwiMember = model.findMember(sciAttribute);
    if(rwiMember != null) { 
      return (rwiMember.getProperty(RwiProperty.DOC));
    }
    return null;
  }

  //---------------------------------------------------------------------------
  private String getOperationAnnotation(
    SciOperation sciOperation
  ) {
    RwiModel model = RwiModelAccess.getModel();
    RwiMember rwiMember = model.findMember(sciOperation);
    if(rwiMember != null) { 
      return (rwiMember.getProperty(RwiProperty.DOC));
    }
    return null;
  }

  //---------------------------------------------------------------------------
  private String getAssociationAnnotation(
    SciAttribute sciAttribute
  ) {
    return (getAttributeAnnotation(sciAttribute));
  }

  //---------------------------------------------------------------------------
  /*
   * abstract classes can be marked by the user and detected by this
   * Model Exporter module only if the following properties are set in the
   * Together Inspector. This can be done by manually adding the following
   * two lines to the file %TGH%\config\changes.config
   *
   * inspector.node.element.*.Class.item.openmdx.item.isAbstract = ChoiceField( { values := { "false", "true" } } )
   * inspector.node.element.*.Class.item.openmdx.item.isAbstract.name = isAbstract
   *
   * Please note that Together offers an 'abstract' BasicException.Parameter if you choose
   * a project language that supports abstract classes where abstract is
   * a class BasicException.Parameter. This is the case for Java, but not for C++. In C++
   * for example, abstract classes are classes that own at least one pure
   * virtual operation (and this is obviously NOT a class BasicException.Parameter!).
   *
   * If you want to have a graphical representation (i.e. class name in italic)
   * of this , you can add the following two lines to the file %TGH%/config/view.config
   *
   * \  if (getProperty("isAbstract") == "true",
   * \      nameLabel->setFont(%defaultFontName%, "BoldItalic", %defaultFontSize%),
   * \      nameLabel->setFont(%defaultFontName%, "Bold", %defaultFontSize%)
   * \    );
   *
   */
  private boolean isAbstractClass(
    SciClass cl
  ) {
    String tagValue = cl.getTagList().getTagValue("isAbstract");
    if(tagValue == null) {
      SysLog.warning("the BasicException.Parameter 'isAbstract' for class <" + cl.getName() + "> is not set (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isAbstract' for class <" + cl.getName() + "> is not set (using value \"false\" as default value)");
      autoCorrectMissingTags(cl, "isAbstract", "false");
      return false;
    }
    else if("true".equals(tagValue)) { 
      return true; 
    }
    else if("false".equals(tagValue)) { 
      return false; 
    }
    else {
      SysLog.warning("the BasicException.Parameter 'isAbstract' for class <" + cl.getName() + "> uses an unknown value (using value \"false\" as default value)");
      warning("the BasicException.Parameter 'isAbstract' for class <" + cl.getName() + "> uses an unknown value (using value \"false\" as default value)");
      autoCorrectMissingTags(cl, "isAbstract", "false");
      return false;
    }
  }
  
  //---------------------------------------------------------------------------
  private String getNextToken(
    StringTokenizer tokenizer, 
    String qualifierText
  ) throws ServiceException {
		String nextToken = new String();
    if(tokenizer.hasMoreTokens()) {
    	nextToken = tokenizer.nextToken();
      // skip spaces and tabs
      while (tokenizer.hasMoreTokens() && ( nextToken.equals(" ") || nextToken.equals("\t") )) {
        nextToken = tokenizer.nextToken();
      }
      if(nextToken.equals(" ") || nextToken.equals("\t")) {
        SysLog.error("syntax error in qualifier declaration <" + qualifierText + ">: unexpected end of expression");
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN,
					TogetherImporterErrorCodes.QUALIFIER_SYNTAX_ERROR, 
					new BasicException.Parameter[]{
					  new BasicException.Parameter("qualifier text", qualifierText)
					},
					"syntax error in qualifier declaration: unexpected end of expression"
				);
      }
    }
    return nextToken;
  }

  //---------------------------------------------------------------------------
  /**
   * parse strings with the following EBNF syntax 
   * [ qualifierAttribute ':' qualifierType ] { ';' qualifierAttribute ':' qualifierType }
   */
  private Map parseAssociationQualifierAttributes(
    String qualifierText
  ) throws ServiceException {
    Map qualifierAttributes = new HashMap();
    
    /**
     * qualifier types are in MOF syntax (with '::'). Since the standard
     * Java StringTokenizer cannot distinguish between ':' that separates
     * an attribute name from its attribute type and a '::' that is used 
     * to qualify attribute types, the input text is first converted from
     * MOF to Java syntax ('.' instead of '::'). This makes the lexical
     * analysis a lot easier and avoids having to write a dedicated tokenizer
     * that can handle the problem mentioned above.
     */
    qualifierText = qualifiedNameToJava(qualifierText);
    
    StringTokenizer tokenizer = new StringTokenizer(qualifierText.trim(), ":; \t", true);

    if(tokenizer.hasMoreTokens()) {
      String qualifierAttribute = getNextToken(tokenizer, qualifierText);
      String delim = getNextToken(tokenizer, qualifierText);
      if(!delim.equals(":")) { 
        SysLog.error("syntax error in qualifier declaration <" + qualifierText + ">: missing ':'"); 
				throw new ServiceException(
					BasicException.Code.DEFAULT_DOMAIN,
					TogetherImporterErrorCodes.QUALIFIER_SYNTAX_ERROR, 
					new BasicException.Parameter[]{
					  new BasicException.Parameter("qualifier text", qualifierText),
					  new BasicException.Parameter("error message", "missing ':'")
					},
					"syntax error in qualifier declaration: missing ':'"
				);
      }
      String qualifierType = javaToQualifiedName(getNextToken(tokenizer, qualifierText));
      qualifierAttributes.put(qualifierAttribute, qualifierType);

      if(tokenizer.hasMoreTokens()) {
        delim = getNextToken(tokenizer, qualifierText);
        if (!delim.equals(";")) { 
          SysLog.error("syntax error in qualifier declaration <" + qualifierText + ">: qualifier expressions must be separated by ';'");
					throw new ServiceException(
						BasicException.Code.DEFAULT_DOMAIN,
						TogetherImporterErrorCodes.QUALIFIER_SYNTAX_ERROR, 
						new BasicException.Parameter[]{
						  new BasicException.Parameter("qualifier text", qualifierText),
						  new BasicException.Parameter("error message", "qualifier expressions must be separated by ';'")
						},
						"syntax error in qualifier declaration: qualifier expressions must be separated by ';'"
					);
        }
        while (delim.equals(";")) {
          qualifierAttribute = getNextToken(tokenizer, qualifierText);
          delim = getNextToken(tokenizer, qualifierText);
          if (!delim.equals(":")) {
            SysLog.error("syntax error in qualifier declaration <" + qualifierText + ">: missing ':'");
						throw new ServiceException(
							BasicException.Code.DEFAULT_DOMAIN,
							TogetherImporterErrorCodes.QUALIFIER_SYNTAX_ERROR, 
							new BasicException.Parameter[]{
							  new BasicException.Parameter("qualifier text", qualifierText),
							  new BasicException.Parameter("error message", "missing ':'")
							},
							"syntax error in qualifier declaration: missing ':'"
						);
          }
          qualifierType = javaToQualifiedName(getNextToken(tokenizer, qualifierText));
          qualifierAttributes.put(qualifierAttribute, qualifierType);
          delim = getNextToken(tokenizer, qualifierText);
        }
      }
    }
    return qualifierAttributes;
  }

  //---------------------------------------------------------------------------
  private void verifyCorrectnessOfNamespaceProperty(
    SciClass aClass
  ) throws ServiceException {
    if (!aClass.getQualifiedName().equals(javaToQualifiedName(getQualifiedName(aClass)))) {
      SysLog.error("namespace BasicException.Parameter of class " + getQualifiedName(aClass) + " does NOT match package containment");
      throw new ServiceException(
        BasicException.Code.DEFAULT_DOMAIN,
        TogetherImporterErrorCodes.WRONG_NAMESPACE_PROPERTY, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("class", getQualifiedName(aClass))
        },
        "namespace BasicException.Parameter of class " + getQualifiedName(aClass) + " does NOT match package containment"
      );
    }
  }

  //---------------------------------------------------------------------------
  private void warning(
    String text
  ) {
    if (showWarnings) { 
      IdeMessageManagerAccess.printMessage(IdeMessageType.WARNING, "WARNING: " + text); 
    }
  }

  //---------------------------------------------------------------------------
  private void info(
    String text
  ) {
    IdeMessageManagerAccess.printMessage(IdeMessageType.INFORMATION, text); 
  }

  //---------------------------------------------------------------------------
  public void process(
    ServiceHeader header,
    Dataprovider_1_0 target,
    String providerName
  ) throws ServiceException {
      
    this.header = header;
    this.target = target;
    this.providerName = providerName;        

    this.beginImport();
    
    if(this.autoCorrectionOn) {
      SysLog.trace("auto correction of missing tags is ON"); 
    }
    else { 
      SysLog.trace("auto correction of missing tags is OFF"); 
    }
    
    if(this.showWarnings) { 
      SysLog.trace("show warnings is ON"); 
    }
    else {
      SysLog.trace("show warnings is OFF"); 
    }
    
    // make sure that all classes inherit from a openMDX class
    // DEACTIVATED in v3
    //if(autoCorrectionOn) { assertClassesInheritFromBasicObject(); }

    // determine all packages that depend from the package which was selected
    // by the user
    Set allPackages = getAllPackages();
    Iterator allPackagesInterator = allPackages.iterator();
    while (allPackagesInterator.hasNext()) {
      SciPackage nextPkg = (SciPackage)allPackagesInterator.next();
      this.processTogetherPackage(nextPkg);
    }

    // Together WORKAROUND: add all primitive types that cannot
    // be modelled as classes in Together due to keyword conflicts
    addWorkaroundPrimitiveTypes();

    // stereotype = <Primitive> => PrimitiveType
    // (all the primitive types must be known before Structs and Classes)
    Iterator allClassesIterator = getAllClasses().iterator();
    while (allClassesIterator.hasNext()) {
      SciClass currentClass = (SciClass)allClassesIterator.next();
      if(this.getClassStereotypes(currentClass).contains(Stereotypes.PRIMITIVE) ) {
        this.processTogetherPrimitiveType(currentClass);
      }
    }

    allClassesIterator = getAllClasses().iterator();
    while(allClassesIterator.hasNext()) {
      SciClass currentClass = (SciClass)allClassesIterator.next();

      if(this.getClassStereotypes(currentClass).contains(Stereotypes.STRUCT)) {
        this.processTogetherStructureType(currentClass);
      }
      else if(this.getClassStereotypes(currentClass).contains(Stereotypes.ALIAS)) {
        this.processTogetherAliasType(currentClass);
      }   
      else if("parameter".equals(this.getClassStereotypes(currentClass))) {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.NOT_SUPPORTED, 
          new BasicException.Parameter[]{
            new BasicException.Parameter("classifier", currentClass)
          },
          "Stereotype <<parameter>> is not supported anymore. Use stereotype <<" + Stereotypes.STRUCT + ">> instead"
        );
      }
      else if(
        !this.getClassStereotypes(currentClass).contains(Stereotypes.PRIMITIVE) 
      ) {
        this.processTogetherClass(currentClass);
      }
    }
    this.endImport();
  }

  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------
      
  // association types
  final private short ASSOCIATION = 0;
  final private short AGGREGATION = 1;
  final private short COMPOSITION = 2;

  // configuration flags
  boolean autoCorrectionOn = false;
  boolean showWarnings = true;

  // all primitive types
  Set primitiveTypes = new HashSet();
        
}

//--- End of File -----------------------------------------------------------

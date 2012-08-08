/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MofRepositoryImporter.java,v 1.21 2004/10/07 21:15:49 hburger Exp $
 * Description: lab client
 * Revision:    $Revision: 1.21 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/10/07 21:15:49 $
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
 
package org.openmdx.model1.poseidon.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.omg.model1.code.DirectionKind;
import org.omg.model1.code.ScopeKind;
import org.omg.model1.code.VisibilityKind;
import org.omg.uml.foundation.core.AssociationEnd;
import org.omg.uml.foundation.core.Attribute;
import org.omg.uml.foundation.core.DataType;
import org.omg.uml.foundation.core.Feature;
import org.omg.uml.foundation.core.Generalization;
import org.omg.uml.foundation.core.ModelElement;
import org.omg.uml.foundation.core.Namespace;
import org.omg.uml.foundation.core.Operation;
import org.omg.uml.foundation.core.Parameter;
import org.omg.uml.foundation.core.Stereotype;
import org.omg.uml.foundation.core.TagDefinition;
import org.omg.uml.foundation.core.TaggedValue;
import org.omg.uml.foundation.core.UmlAssociation;
import org.omg.uml.foundation.core.UmlClass;
import org.omg.uml.foundation.datatypes.MultiplicityRange;
import org.omg.uml.foundation.datatypes.ParameterDirectionKindEnum;
import org.omg.uml.modelmanagement.Model;
import org.omg.uml.modelmanagement.UmlPackage;
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
import org.openmdx.model1.code.ModelExceptions;
import org.openmdx.model1.code.Stereotypes;
import org.openmdx.model1.importer.spi.ModelImporter_1;
import org.openmdx.uses.java.lang.StringBuilder;

import com.gentleware.poseidon.openapi.PoseidonProjectConnector;


class MofRepositoryImporter
  extends ModelImporter_1
{

  public MofRepositoryImporter() {
    super();
  }

  //---------------------------------------------------------------------------
  public void process(
    ServiceHeader serviceHeader,
    Dataprovider_1_0 dataprovider,
    String provider
  ) throws ServiceException {
    this.header = serviceHeader;
    this.target = dataprovider;
    this.providerName = provider;        

    this.beginImport();
    
    this.nElements = 0;
    this.warnings = new ArrayList();

    // get current poseidon model and process its model elements
    Model model = (Model)PoseidonProjectConnector.getModel();
    this.processModelElements(model.getOwnedElement());
    
    this.endImport();
  }

  //---------------------------------------------------------------------------
  public int getNumberOfElements(
  ) {
    return nElements;
  }

  //---------------------------------------------------------------------------
  void processModelElements(
    Collection elements
  ) throws ServiceException {
    for(
      java.util.Iterator it = elements.iterator();
      it.hasNext();
    ) {
      Object next = it.next();

      if (next instanceof UmlPackage)
      {
        this.nElements++;
        this.processUMLPackage((UmlPackage)next);
      }
      else if (next instanceof UmlClass)
      {
        this.nElements++;
        UmlClass umlClass = (UmlClass)next;
        
        // depending on stereotype, the given class must be treated differently
        if (umlClass.getStereotype().isEmpty())
        {
          this.processUMLClass(umlClass);
        }
        else
        {
          List stereotypes = new ArrayList();
          for(
            Iterator it2 = umlClass.getStereotype().iterator();
            it2.hasNext();
          ) 
          {
            stereotypes.add(((Stereotype)it2.next()).getName());
          }
          if (stereotypes.contains(Stereotypes.STRUCT))
          {
            this.processStructureType(umlClass);
          }
          else if (stereotypes.contains(Stereotypes.ALIAS))
          {
            this.processAliasType(umlClass);
          }
          else if (stereotypes.contains(Stereotypes.PRIMITIVE))
          {
            this.addWarning(WARNING_INVALID_PRIMITIVE_TYPE_DECLARATION, new String[]{ this.getQualifiedName(umlClass, "::") });
          }
          else
          {
            this.processUMLClass(umlClass);
          }
        }
      }
      else if (next instanceof DataType)
      {
        this.nElements++;
        this.processUMLDataType((DataType)next);
      }
      else if (next instanceof UmlAssociation)
      {
        this.nElements++;
        this.processUMLAssociation((UmlAssociation)next);
      }
      else if (next instanceof Generalization)
      {
        this.nElements++;
        this.processUMLGeneralization((Generalization)next);
      }
      else if (next instanceof Stereotype)
      {
        this.nElements++;
        this.processUMLStereotype((Stereotype)next);
      }
      else if (next instanceof TagDefinition)
      {
        this.nElements++;
        this.processUMLTagDefinition((TagDefinition)next);
      }
      else
      {
        this.nElements++;
        SysLog.warning("*** unprocessed model element: " + next);
      }
    }
  }

  //---------------------------------------------------------------------------
  public void processUMLGeneralization(
    Generalization generalization
  ) {
  }
  
  //---------------------------------------------------------------------------
  public void processUMLStereotype(
    Stereotype stereotype
  ) {
  }
  
  //---------------------------------------------------------------------------
  public void processUMLTagDefinition(
    TagDefinition tagDefinition
  ) {
  }
  
  //---------------------------------------------------------------------------
  public void processUMLPackage(
    UmlPackage umlPackage
  ) throws ServiceException {
    String qualifiedName = this.getQualifiedName(umlPackage, "::");
    SysLog.info("processing UML package " + qualifiedName);

    DataproviderObject modelPackage = new DataproviderObject(
      toElementPath(
        nameToPathComponent(qualifiedName),
        umlPackage.getName()
      )
    );

    modelPackage.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PACKAGE);
    modelPackage.values("isAbstract").add(new Boolean(false));
    modelPackage.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    // annotation
    String annotation = this.getAnnotation(umlPackage);
    if (annotation.length() != 0)
    {
      modelPackage.values("annotation").add(annotation);
    }

    SysLog.info("package=" + modelPackage);
    this.createModelElement( 
      null,
      modelPackage
    );

    // process all model elements in this package
    this.processModelElements(umlPackage.getOwnedElement());
  }

  //---------------------------------------------------------------------------
  private void processStructureType(
    UmlClass umlClass
  ) throws ServiceException {

    if (this.isTopLevelModelElement(umlClass))
    {
      SysLog.error("structure type must be in a package");
      throw new ServiceException(
        ModelExceptions.MODEL_DOMAIN,
        ModelExceptions.MODEL_ELEMENT_NOT_IN_PACKAGE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("element", umlClass.getName()),
          new BasicException.Parameter("element type", "Structure Type")
        },
        "structure type must be in a package"
      );                  
    }

    String qualifiedName = this.getQualifiedName(umlClass, "::");
    SysLog.info("Processing structure type " + qualifiedName);

    // ModelClass object
    DataproviderObject structureTypeDef = new DataproviderObject(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        umlClass.getName()
      )
    );

    // object_class
    structureTypeDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.STRUCTURE_TYPE
    );
    
    // container
    structureTypeDef.values("container").add(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        getName(getScope(qualifiedName))
      )
    );

    /**
     * skip stereotype because its value 'Struct'
     * was marked to note the difference between
     * ordinary classes and structure types
     */

    // annotation
    String annotation = this.getAnnotation(umlClass);
    if (annotation.length() != 0)
    {
      structureTypeDef.values("annotation").add(annotation);
    }

    // supertype
    SortedSet superTypePaths = new TreeSet();
    for (
      Iterator it = umlClass.getGeneralization().iterator();
      it.hasNext(); 
    ) {
      String superclass = this.getQualifiedName(((Generalization)it.next()).getParent(), "::");
      superTypePaths.add(
        toElementPath(
          nameToPathComponent(getScope(superclass)),
          getName(superclass)
        )
      );
    }
    // add supertypes in sorted order
    for(
      Iterator it = superTypePaths.iterator();
      it.hasNext();
    ) {
      structureTypeDef.values("supertype").add(it.next());
    }
        
    // isAbstract attribute
    structureTypeDef.values("isAbstract").add(
      new Boolean(umlClass.isAbstract())
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

    for(
      Iterator it = getAttributes(umlClass).iterator();
      it.hasNext();
    ) {
      this.processStructureField(
        (Attribute)it.next(),
        structureTypeDef
      );
    }
  }

  //---------------------------------------------------------------------------
  private void processStructureField(
    Attribute umlAttribute,
    DataproviderObject structureTypeDef
  ) throws ServiceException {

    SysLog.info("Processing structure field " + umlAttribute.getName());

    DataproviderObject structureFieldDef = new DataproviderObject(
      new Path(structureTypeDef.path().toString() + "::" + umlAttribute.getName())
    );

    // object_class
    structureFieldDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.STRUCTURE_FIELD
    );
    
    // container
    structureFieldDef.values("container").add(structureTypeDef.path());

    // maxLength attribute
    structureFieldDef.values("maxLength").add(new Integer(this.getAttributeMaxLength(umlAttribute)));
    
    // multiplicity attribute
    // openMDX uses attribute stereotype to indicate multiplicity
    // this allows to use multiplicities like set, list, ...
    // if no multiplicity has been modeled, the default multiplicity is taken
    structureFieldDef.values("multiplicity").add(
      umlAttribute.getStereotype().isEmpty() ?
        DEFAULT_ATTRIBUTE_MULTIPLICITY :
        ((Stereotype)umlAttribute.getStereotype().iterator().next()).getName() 
    );

    // annotation
    String annotation = this.getAnnotation(umlAttribute);
    if (annotation.length() != 0)
    {
      structureFieldDef.values("annotation").add(annotation);
    }

    String qualifiedTypeName = this.getQualifiedName(umlAttribute.getType(), "::");
    structureFieldDef.values("type").add(
      toElementPath(
        nameToPathComponent(getScope(qualifiedTypeName)),
        getName(qualifiedTypeName)
      )
    );
    
    this.createModelElement(
      null, 
      structureFieldDef
    );
  }
  
  //---------------------------------------------------------------------------
  private void processAliasType(
    UmlClass umlClass
  ) throws ServiceException {

    if (this.isTopLevelModelElement(umlClass))
    {
      SysLog.error("alias type must be in a package");
      throw new ServiceException(
        ModelExceptions.MODEL_DOMAIN,
        ModelExceptions.MODEL_ELEMENT_NOT_IN_PACKAGE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("element", umlClass.getName()),
          new BasicException.Parameter("element type", "Alias Type")
        },
        "alias type must be in a package"
      );                  
    }
    
    String qualifiedName = this.getQualifiedName(umlClass, "::");
    SysLog.info("Processing alias type " + qualifiedName);

    // ModelClass object
    DataproviderObject aliasTypeDef = new DataproviderObject(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        umlClass.getName()
      )
    );

    // object_class
    aliasTypeDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.ALIAS_TYPE
    );
    
    // container
    aliasTypeDef.values("container").add(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        getName(getScope(qualifiedName))
      )
    );


    // annotation
    String annotation = this.getAnnotation(umlClass);
    if (annotation.length() != 0)
    {
      aliasTypeDef.values("annotation").add(annotation);
    }

    // isAbstract attribute
    aliasTypeDef.values("isAbstract").add(
      new Boolean(umlClass.isAbstract())
    );
    
    // visibility attribute
    aliasTypeDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    // isSingleton attribute
    aliasTypeDef.values("isSingleton").add(
      new Boolean(false)
    );

    // type
    List attributes = this.getAttributes(umlClass);
    this.verifyAliasAttributeNumber(aliasTypeDef, attributes.size());

    Attribute attribute = (Attribute)attributes.get(0);
    this.verifyAliasAttributeName(aliasTypeDef, attribute.getName());
    aliasTypeDef.values("type").add(
      toElementPath(
        nameToPathComponent(getScope(attribute.getName())),
        getName(attribute.getName())
      )
    );
    
    SysLog.info("alias type=" + aliasTypeDef);

    // create element
    this.createModelElement(
      null, 
      aliasTypeDef
    );
  }
  
  //---------------------------------------------------------------------------
  public void processUMLDataType(
    DataType umlDataType
  ) throws ServiceException {

    if (this.isTopLevelModelElement(umlDataType))
    {
      SysLog.error("datatype must be in a package");
      throw new ServiceException(
        ModelExceptions.MODEL_DOMAIN,
        ModelExceptions.MODEL_ELEMENT_NOT_IN_PACKAGE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("element", umlDataType.getName()),
          new BasicException.Parameter("element type", "DataType")
        },
        "datatype must be in a package"
      );                  
    }
    
    String qualifiedName = this.getQualifiedName(umlDataType, "::");
    SysLog.info("processing UML data type " + qualifiedName);

    // ModelPrimitiveType object
    DataproviderObject primitiveTypeDef = new DataproviderObject(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        umlDataType.getName()
      )
    );

    // object_class
    primitiveTypeDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.PRIMITIVE_TYPE);
    
    // container
    primitiveTypeDef.values("container").add(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        getName(getScope(qualifiedName))
      )
    );

    /**
     * skip stereotype because its value 'Primitive'
     * was marked to note the difference between
     * ordinary classes and primitive types
     */

    // annotation
    String annotation = this.getAnnotation(umlDataType);
    if (annotation.length() != 0)
    {
      primitiveTypeDef.values("annotation").add(annotation);
    }

    // isAbstract attribute
    primitiveTypeDef.values("isAbstract").add(
      new Boolean(umlDataType.isAbstract())
    );
    
    // isSingleton attribute
    primitiveTypeDef.values("isSingleton").add(
      new Boolean(false)
    );

    // visibility attribute
    primitiveTypeDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    SysLog.info("primitive type=" + primitiveTypeDef);
    createModelElement(null, primitiveTypeDef);
  }

  //---------------------------------------------------------------------------
  public void processUMLAssociation(
    UmlAssociation umlAssociation
  ) throws ServiceException {

    String qualifiedName = this.getQualifiedName(umlAssociation, "::");
    SysLog.info("processing UML association " + qualifiedName);
    
//    this.verifyAssociationName(umlAssociation.getName());
    if (umlAssociation.getName() == null || umlAssociation.getName().length() == 0)
    {
      SysLog.error("the name of an association cannot be empty");
      throw new ServiceException(
        ModelExceptions.MODEL_DOMAIN,
        ModelExceptions.ASSOCIATION_NAME_IS_EMPTY, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("class 1", ((AssociationEnd)umlAssociation.getConnection().get(0)).getParticipant().getName()),
          new BasicException.Parameter("class 2", ((AssociationEnd)umlAssociation.getConnection().get(1)).getParticipant().getName())
        },
        "the name of an association cannot be empty"
      );            
    }

    // ModelAssociation object
    DataproviderObject associationDef = new DataproviderObject(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        umlAssociation.getName()
      )
    );

    // object_class
    associationDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ASSOCIATION);

    // container
    associationDef.values("container").add(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        getName(getScope(qualifiedName))
      )
    );

    // annotation
    String annotation = this.getAnnotation(umlAssociation);
    if (annotation.length() != 0)
    {
      associationDef.values("annotation").add(annotation);
    }
    
    // stereotype
    for(Iterator it = umlAssociation.getStereotype().iterator(); it.hasNext();)
    {
      associationDef.values("stereotype").add(((Stereotype)it.next()).getName());
    }

    associationDef.values("isAbstract").add(new Boolean(false));
    associationDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);
    associationDef.values("isDerived").add(
        new Boolean(this.isAssociationDerived(umlAssociation))
    );

    createModelElement(null, associationDef);
    
    DataproviderObject associationEnd1Def = this.processUMLAssociationEnd(
      (AssociationEnd)umlAssociation.getConnection().get(0),
      associationDef
    );
    
    DataproviderObject associationEnd2Def = this.processUMLAssociationEnd(
      (AssociationEnd)umlAssociation.getConnection().get(1),
      associationDef
    );
    
    /**
     * Poseidon XMI/UML dialect
     * NOTE:
     * To comply with our MOF model implementation we change aggregation and 
     * qualifier assignments. Client aggregation and qualifier attributes 
     * now belong to the supplier side and supplier aggregation and qualifier 
     * attributes now belong to the client side.
     */

    // swap 'aggregation' to comply with our MOF model
    String temp = (String)associationEnd1Def.values("aggregation").get(0);
    associationEnd1Def.values("aggregation").set(
      0, 
      associationEnd2Def.values("aggregation").get(0)
    );
    associationEnd2Def.values("aggregation").set(0, temp);

    // swap 'qualifierName' to comply with our MOF model
    List tempQualifier = new ArrayList(associationEnd1Def.values("qualifierName"));
    associationEnd1Def.values("qualifierName").clear();
    associationEnd1Def.values("qualifierName").addAll(
      associationEnd2Def.values("qualifierName")
    );
    associationEnd2Def.values("qualifierName").clear();
    associationEnd2Def.values("qualifierName").addAll(tempQualifier);

    // swap 'qualifierType' to comply with our MOF model
    tempQualifier = new ArrayList(associationEnd1Def.values("qualifierType"));
    associationEnd1Def.values("qualifierType").clear();
    associationEnd1Def.values("qualifierType").addAll(
      associationEnd2Def.values("qualifierType")
    );
    associationEnd2Def.values("qualifierType").clear();
    associationEnd2Def.values("qualifierType").addAll(tempQualifier);

    this.verifyAndCompleteAssociationEnds(
      associationEnd1Def,
      associationEnd2Def
    );
    this.exportAssociationEndAsReference(
      associationEnd1Def,
      associationEnd2Def,
      associationDef,
      null
    );
    this.exportAssociationEndAsReference(
      associationEnd2Def,
      associationEnd1Def,
      associationDef,
      null
    );
    this.createModelElement(null, associationEnd1Def);
    this.createModelElement(null, associationEnd2Def);      
  }

  //---------------------------------------------------------------------------
  private DataproviderObject processUMLAssociationEnd(
    AssociationEnd umlAssociationEnd,
    DataproviderObject associationDef
  ) throws ServiceException {

    SysLog.info("processing UML association end " + umlAssociationEnd.getName());
    
    // Note: 
    // In the Poseidon XMI/UML dialect qualifiers are entered by misusing 
    // the association end name. The association end names consist of the
    // association end name and the qualifiers in square brackets (name and
    // qualfiers are separated by a new line). Therefore the real 
    // association end name and the qualifiers must be extracted.
  
    // extract/parse association end name and qualifiers
    List qualifiers = this.toAssociationEndQualifiers(umlAssociationEnd.getName());
    String associationEndName = this.toAssociationEndName(umlAssociationEnd.getName());

    this.verifyAssociationEndName(associationDef, associationEndName);

    DataproviderObject associationEndDef = new DataproviderObject(
      new Path(associationDef.path().toString() + "::" + associationEndName)
    );

    // name
    associationEndDef.values("name").add(associationEndName);

    // annotation
    String annotation = this.getAnnotation(umlAssociationEnd);
    if (annotation.length() != 0)
    {
      associationEndDef.values("annotation").add(annotation);
    }
    
    // object_class
    associationEndDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.ASSOCIATION_END
    );

    // type
    String qualifiedTypeName = this.getQualifiedName(umlAssociationEnd.getParticipant(), "::");
    associationEndDef.values("type").add(
      toElementPath(
        nameToPathComponent(getScope(qualifiedTypeName)),
        getName(qualifiedTypeName)
      )
    );

    // multiplicity
    associationEndDef.values("multiplicity").add(
      this.toMOFMultiplicity((MultiplicityRange)umlAssociationEnd.getMultiplicity().getRange().iterator().next())
    );

    // container
    associationEndDef.values("container").add(
      associationDef.path()
    );
    
    // isChangeable
    associationEndDef.values("isChangeable").add(
      new Boolean(
        this.toMOFChangeability(umlAssociationEnd.getChangeability())
      )
    );

    // aggregation
    associationEndDef.values("aggregation").add(
      this.toMOFAggregation(umlAssociationEnd.getAggregation())
    );

    // isNavigable
    associationEndDef.values("isNavigable").add(
      new Boolean(umlAssociationEnd.isNavigable())
    );

    // qualifiers
    for (
      Iterator it = qualifiers.iterator();
      it.hasNext();
    ) {
      Qualifier qualifier = (Qualifier)it.next();
      associationEndDef.values("qualifierName").add(qualifier.getName());
      associationEndDef.values("qualifierType").add(
        toElementPath(
          nameToPathComponent(getScope(qualifier.getType())),
          getName(qualifier.getType())
        )
      );
    }    

    return associationEndDef;
  }

  //---------------------------------------------------------------------------
  void processUMLClass(
    UmlClass umlClass
  ) throws ServiceException {

    if (this.isTopLevelModelElement(umlClass))
    {
      SysLog.error("class must be in a package");
      throw new ServiceException(
        ModelExceptions.MODEL_DOMAIN,
        ModelExceptions.MODEL_ELEMENT_NOT_IN_PACKAGE, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("element", umlClass.getName()),
          new BasicException.Parameter("element type", "Class")
        },
        "class must be in a package"
      );                  
    }
    
    String qualifiedName = this.getQualifiedName(umlClass, "::");
    SysLog.info("processing UML class " + qualifiedName);

    // ModelClass object
    DataproviderObject classDef = new DataproviderObject(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        umlClass.getName()
      )
    );

    // object_class
    classDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.CLASS);
    
    // container
    classDef.values("container").add(
      toElementPath(
        nameToPathComponent(getScope(qualifiedName)),
        getName(getScope(qualifiedName))
      )
    );

    // stereotype
    for(Iterator it = umlClass.getStereotype().iterator(); it.hasNext();)
    {
      classDef.values("stereotype").add(((Stereotype)it.next()).getName());
    }

    // annotation
    String annotation = this.getAnnotation(umlClass);
    if (annotation.length() != 0)
    {
      classDef.values("annotation").add(annotation);
    }

    // supertype
    SortedSet superTypePaths = new TreeSet();
    for (
      Iterator it = umlClass.getGeneralization().iterator();
      it.hasNext(); 
    ) {
      String superclass = this.getQualifiedName(((Generalization)it.next()).getParent(), "::");
      superTypePaths.add(
        toElementPath(
          nameToPathComponent(getScope(superclass)),
          getName(superclass)
        )
      );
    }
    // add supertypes in sorted order
    for(
      Iterator it = superTypePaths.iterator();
      it.hasNext();
    ) {
      classDef.values("supertype").add(it.next());
    }
        
    // isAbstract attribute
    classDef.values("isAbstract").add(
      new Boolean(umlClass.isAbstract())
    );
    
    // visibility attribute
    classDef.values("visibility").add(VisibilityKind.PUBLIC_VIS);

    // isSingleton attribute
    classDef.values("isSingleton").add(
      new Boolean(false)
    );

    SysLog.info("class=" + classDef);
    createModelElement(null, classDef);

    // process all model elements in this class
    for(
      Iterator it = umlClass.getFeature().iterator();
      it.hasNext();
    ) {
      Object next = it.next();
      if (next instanceof Attribute)
      {
        this.nElements++;
        this.processUMLAttribute((Attribute)next, classDef);
      }
      else if (next instanceof Operation)
      {
        this.nElements++;
        this.processUMLOperation((Operation)next, classDef);
      }
    }
  }

  //---------------------------------------------------------------------------
  void processUMLAttribute(
    Attribute umlAttribute,
    DataproviderObject classDef
  ) throws ServiceException {
    SysLog.info("processing UML attribute " + umlAttribute.getName());

    DataproviderObject attributeDef = new DataproviderObject(
      new Path(classDef.path().toString() + "::" + umlAttribute.getName())
    );

    attributeDef.values(SystemAttributes.OBJECT_CLASS).add(ModelAttributes.ATTRIBUTE);

    attributeDef.values("container").add(classDef.path());
    attributeDef.values("visibility").add(this.toMOFVisibility(umlAttribute.getVisibility()));

    if (!VisibilityKind.PUBLIC_VIS.equals(attributeDef.values("visibility").get(0)))
    {
      SysLog.warning("ignoring attribute " + getDisplayName(attributeDef) + " because its visibility is not public");
      this.addWarning(WARNING_INVISIBLE_ATTRIBUTE, new String[] { getDisplayName(attributeDef) });
    }

    attributeDef.values("uniqueValues").add(new Boolean(DEFAULT_ATTRIBUTE_IS_UNIQUE));
    attributeDef.values("isLanguageNeutral").add(new Boolean(DEFAULT_ATTRIBUTE_IS_LANGUAGE_NEUTRAL));
    attributeDef.values("maxLength").add(new Integer(this.getAttributeMaxLength(umlAttribute)));

    if (umlAttribute.getType() != null)
    {
      String qualifiedTypeName = this.getQualifiedName(umlAttribute.getType(), "::");
      if (qualifiedTypeName.indexOf("::") != -1)
      {
        attributeDef.values("type").add(
          toElementPath(
            nameToPathComponent(getScope(qualifiedTypeName)),
            getName(qualifiedTypeName)
          )
        );
      }
      else
      {
        SysLog.error("invalid attribute type was specified");
        throw new ServiceException(
          ModelExceptions.MODEL_DOMAIN,
          ModelExceptions.INVALID_ATTRIBUTE_TYPE, 
          new BasicException.Parameter[]{
            new BasicException.Parameter("attribute", attributeDef.path().toString())
          },
          "invalid attribute type was specified"
        );            
      }
    }
    else
    {
      SysLog.error("no attribute type was specified");
      throw new ServiceException(
        ModelExceptions.MODEL_DOMAIN,
        ModelExceptions.NO_ATTRIBUTE_TYPE_SPECIFIED, 
        new BasicException.Parameter[]{
          new BasicException.Parameter("attribute", attributeDef.path().toString())
        },
        "no attribute type was specified"
      );            
    }
    
    // openMDX uses attribute stereotype to indicate multiplicity
    // this allows to use multiplicities like set, list, ...
    // if no multiplicity has been modeled, the default multiplicity is taken
    attributeDef.values("multiplicity").add(
      umlAttribute.getStereotype().isEmpty() ?
        DEFAULT_ATTRIBUTE_MULTIPLICITY :
        ((Stereotype) umlAttribute.getStereotype().iterator().next()).getName()
    );
    attributeDef.values("scope").add(ScopeKind.INSTANCE_LEVEL);
    boolean isDerived = this.isAttributeDerived(umlAttribute);
    attributeDef.values("isDerived").add(new Boolean(isDerived));
    boolean isChangeable = this.toMOFChangeability(umlAttribute.getChangeability());
    attributeDef.values("isChangeable").add(new Boolean(isChangeable));
    if (isDerived && isChangeable)
    {
      SysLog.warning("derived attribute " + getDisplayName(attributeDef) + " must be set to final");
      this.addWarning(WARNING_DERIVED_AND_CHANGEABLE_ATTRIBUTE, new String[] { getDisplayName(attributeDef) });
    }

    // annotation
    String annotation = this.getAnnotation(umlAttribute);
    if (annotation.length() != 0)
    {
      attributeDef.values("annotation").add(annotation);
    }

    SysLog.info("attribute=" + attributeDef);
    createModelElement(null, attributeDef);
  }

  //---------------------------------------------------------------------------
  private void processUMLOperation(
    Operation umlOperation,
    DataproviderObject classDef
  ) throws ServiceException {
    SysLog.info("Processing UML operation " + umlOperation.getName());

    DataproviderObject operationDef = new DataproviderObject(
      new Path(classDef.path().toString() + "::" + umlOperation.getName())
    );

    // object_class
    operationDef.values(SystemAttributes.OBJECT_CLASS).add(
      ModelAttributes.OPERATION
    );

    // container
    operationDef.values("container").add(classDef.path());

    // stereotype
    boolean isException = false;
    if (!umlOperation.getStereotype().isEmpty())
    {
      for(Iterator it = umlOperation.getStereotype().iterator(); it.hasNext();)
      {
        operationDef.values("stereotype").add(((Stereotype)it.next()).getName());
      }

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
    String annotation = this.getAnnotation(umlOperation);
    if (annotation.length() != 0)
    {
      operationDef.values("annotation").add(annotation);
    }

    // visibility
    operationDef.values("visibility").add(this.toMOFVisibility(umlOperation.getVisibility()));

    if (!VisibilityKind.PUBLIC_VIS.equals(operationDef.values("visibility").get(0)))
    {
      SysLog.warning("ignoring operation " + getDisplayName(operationDef) + " because its visibility is not public");
      this.addWarning(WARNING_INVISIBLE_OPERATION, new String[] { getDisplayName(operationDef) });
    }

    // scope
    operationDef.values("scope").add(ScopeKind.INSTANCE_LEVEL);

    // isQuery
    operationDef.values("isQuery").add(
      new Boolean(umlOperation.isQuery())
    );

    // select the return parameter and all the other parameters
    List parameters = new ArrayList();
    Parameter returnParameter = null;
    for(
      Iterator it = umlOperation.getParameter().iterator();
      it.hasNext();
    ) {
      Parameter param = (Parameter)it.next();
      if (ParameterDirectionKindEnum.PDK_RETURN.equals(param.getKind()))
      {
        returnParameter = param;
      }
      else
      {
        parameters.add(param);
      }
    }

    if(parameters.size() > 0)
    {

      /**
       * In SPICE all operations have excatly one parameter with name 'in'. The importer
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
        umlOperation.getName().substring(0,1).toUpperCase() +
        umlOperation.getName().substring(1);
        
      DataproviderObject parameterType = new DataproviderObject(
        new Path(
          classDef.path().toString() + capOperationName + "Params"
        )
      );
      parameterType.values(SystemAttributes.OBJECT_CLASS).add(
        ModelAttributes.STRUCTURE_TYPE
      );  
      parameterType.values("visibility").add(VisibilityKind.PUBLIC_VIS);
      parameterType.values("isAbstract").add(new Boolean(false));
      parameterType.values("isSingleton").add(new Boolean(false));
      parameterType.values("container").addAll(
        classDef.values("container")
      );

      /**
       * Create parameters either as STRUCTURE_FIELD of parameterType (case 1) 
       * or as PARAMETER of modelOperation (case 2)
       */
      boolean createParameterType = true;
      boolean parametersCreated = false;
      
      for(
        Iterator it = parameters.iterator();
        it.hasNext();
      ) {
        Parameter aParameter = (Parameter)it.next();
        
        DataproviderObject parameterDef = this.processParameter(
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
            SysLog.error("Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + Stereotypes.STRUCT);
            throw new ServiceException(
              ModelExceptions.MODEL_DOMAIN,
              ModelExceptions.INVALID_PARAMETER_DECLARATION, 
              new BasicException.Parameter[]{
                new BasicException.Parameter("operation", operationDef.path().toString())
              },
              "Parameter format must be [p0:T0...pn:Tn | in:T], where T must be a class with stereotype " + Stereotypes.STRUCT
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
            SysLog.error("Parameter format must be [p0:T0, ... ,pn:Tn | in:T], where T must be a class with stereotype " + ModelAttributes.STRUCTURE_TYPE);
            throw new ServiceException(
              ModelExceptions.MODEL_DOMAIN,
              ModelExceptions.INVALID_PARAMETER_DECLARATION, 
              new BasicException.Parameter[]{
                new BasicException.Parameter("operation", operationDef.path().toString())
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
        toElementPath(
          nameToPathComponent("org::openmdx::base"),
          "Void"
        )
      );
      this.createModelElement(
        null, 
        inParameterDef
      );
    }

    // Note:
    // return parameter is ignored for exceptions (operations with stereotype
    // exception)
    if(!isException) {          
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

      String qualifiedTypeName = this.getQualifiedName(returnParameter.getType(), "::");
      resultDef.values("type").add(
        toElementPath(
          nameToPathComponent(getScope(qualifiedTypeName)),
          getName(qualifiedTypeName)
        )
      );
    
      this.createModelElement(
        null, 
        resultDef
      );
    }
    
    // exceptions
    String allExceptions = this.getOperationExceptions(umlOperation);
    if (allExceptions != null)
    {
      StringTokenizer exceptions = new StringTokenizer(allExceptions, ",; ");
      while(exceptions.hasMoreTokens()) {
        String qualifiedExceptionName = exceptions.nextToken();
        if (qualifiedExceptionName.indexOf("::") == -1)
        {
          SysLog.warning("Found invalid exception declaration <" + qualifiedExceptionName + "> for the operation " + getDisplayName(operationDef) + "; this exception is ignored unless a valid qualified exception name is specified.");
          this.addWarning(WARNING_INVALID_EXCEPTION_DECLARATION, new String[] { qualifiedExceptionName, getDisplayName(operationDef) });          
        }
        else
        {
          String qualifiedClassName = qualifiedExceptionName.substring(0, qualifiedExceptionName.lastIndexOf("::"));
          String scope = getScope(qualifiedClassName);
          String name = getName(qualifiedClassName);
          if (scope.length() == 0 || name.length() == 0)
          {
            SysLog.warning("Found invalid exception declaration <" + qualifiedExceptionName + "> for the operation " + getDisplayName(operationDef) + "; this exception is ignored unless a valid qualified exception name is specified.");          
            this.addWarning(WARNING_INVALID_EXCEPTION_DECLARATION, new String[] { qualifiedExceptionName, getDisplayName(operationDef) });          
          }
          else
          {
            operationDef.values("exception").add(
              new Path(
                toElementPath(nameToPathComponent(scope), name).toString() + "::" +
                getName(qualifiedExceptionName)
              )
            );
          }
        }
      }
    }
    
    // operation
    this.createModelElement(
      null, 
      operationDef
    );

  }

  //---------------------------------------------------------------------------
  private DataproviderObject processParameter(
    Parameter umlParameter,
    DataproviderObject parameterType
  ) throws ServiceException {
    SysLog.info("Processing parameter " + umlParameter.getName());

    DataproviderObject parameterDef = new DataproviderObject(
      new Path(
        parameterType.path().toString() + "::" + umlParameter.getName()
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

    parameterDef.values("maxLength").add(new Integer(DEFAULT_PARAMETER_MAX_LENGTH));


    String qualifiedTypeName = this.getQualifiedName(umlParameter.getType(), "::");
    parameterDef.values("type").add(
      toElementPath(
        nameToPathComponent(getScope(qualifiedTypeName)),
        getName(qualifiedTypeName)
      )
    );

    parameterDef.values("multiplicity").add(
      umlParameter.getStereotype().isEmpty() ?
        DEFAULT_PARAMETER_MULTIPLICITY :
        ((Stereotype) umlParameter.getStereotype().iterator().next()).getName()
    );

    return parameterDef;

  }
  

  //---------------------------------------------------------------------------
  private boolean isTopLevelModelElement(
    ModelElement modelElement
  ) {
    Namespace namespace = modelElement.getNamespace();
    return (namespace instanceof Model);
  }

  //---------------------------------------------------------------------------
  private String getQualifiedName(
    ModelElement modelElement,
    String namespaceSeparator
  ) {
    Namespace namespace = modelElement.getNamespace();
    if (namespace instanceof Model) { return modelElement.getName(); }

    StringBuffer sb = new StringBuffer(this.getQualifiedName(namespace, namespaceSeparator));
    sb.append(namespaceSeparator);
    sb.append(modelElement.getName() == null ? "" : modelElement.getName());
    return sb.toString();
  }

  //---------------------------------------------------------------------------
  private List getAttributes(
    UmlClass umlClass
  ) {
    List attributes = new ArrayList();
    for(
      Iterator it = umlClass.getFeature().iterator();
      it.hasNext();
    ) {
      Feature feature = (Feature)it.next();
      if (feature instanceof Attribute)
      {
        attributes.add(feature);
      }
    }
    return attributes;
  }
  
  //---------------------------------------------------------------------------
  private String getAnnotation(
    ModelElement modelElement
  ) {
    // the information about the annotation of a model element is stored as a
    // tagged value; the UML Profile for MOF defines:
    // annotation(MOF) <-> tagged value "documentation" (UML)
    for(
      Iterator it = modelElement.getTaggedValue().iterator();
      it.hasNext();
    ) {
      TaggedValue taggedValue = (TaggedValue)it.next();
      if ("documentation".equals(taggedValue.getType().getName()))
      {
        Collection dataValues = taggedValue.getDataValue();
        return dataValues.isEmpty() ? 
          new String() : 
          (String) dataValues.iterator().next();
      }
    }
    return new String();
  }
  
  //---------------------------------------------------------------------------
  private String toMOFMultiplicity(
    MultiplicityRange range
  ) {
    StringBuilder sb = new StringBuilder();
    sb.append(range.getLower());
    sb.append("..");
    sb.append(range.getUpper() == -1 ? "n" : String.valueOf(range.getUpper()));
    return sb.toString();
  }

  //---------------------------------------------------------------------------
  private String toMOFVisibility(
    org.omg.uml.foundation.datatypes.VisibilityKind umlVisibility
  ) {
    if(org.omg.uml.foundation.datatypes.VisibilityKindEnum.VK_PRIVATE.equals(umlVisibility)) {
      return VisibilityKind.PRIVATE_VIS;
    }
    else if(org.omg.uml.foundation.datatypes.VisibilityKindEnum.VK_PUBLIC.equals(umlVisibility)) {
      return VisibilityKind.PUBLIC_VIS;
    }
    else
    {
      return VisibilityKind.PUBLIC_VIS;
    }
  }

  //---------------------------------------------------------------------------
  private String toMOFAggregation(
    org.omg.uml.foundation.datatypes.AggregationKind umlAggregation
  ) {
    if(org.omg.uml.foundation.datatypes.AggregationKindEnum.AK_COMPOSITE.equals(umlAggregation)) {
      return AggregationKind.COMPOSITE;
    }
    else if(org.omg.uml.foundation.datatypes.AggregationKindEnum.AK_AGGREGATE.equals(umlAggregation)) {
      return AggregationKind.SHARED;
    }
    else
    {
      return AggregationKind.NONE;
    }
  }

  //---------------------------------------------------------------------------
  private boolean toMOFChangeability(
    org.omg.uml.foundation.datatypes.ChangeableKind umlChangeability
  ) {
    return org.omg.uml.foundation.datatypes.ChangeableKindEnum.CK_CHANGEABLE.equals(umlChangeability);
  }
  
  //---------------------------------------------------------------------------
  private String getOperationExceptions(
    Operation operation
  ) {
    // the information about the exceptions of an operation is stored as a
    // tagged value  (openMDX choice)
    //
    // Note: the Poseidon way of adding exceptions by means of adding exceptions
    // to the list of "raised signals" cannot be used because the names of all the 
    // exceptions that can be added must end with "Exception" which is not 
    // acceptable for us
    for(
      Iterator it = operation.getTaggedValue().iterator();
      it.hasNext();
    ) {
      TaggedValue taggedValue = (TaggedValue)it.next();
      if ("exceptions".equals(taggedValue.getType().getName()))
      {
        Collection dataValues = taggedValue.getDataValue();
        return dataValues.isEmpty() ? 
          new String() : 
          (String)dataValues.iterator().next();
      }
    }
    return new String();
  }
  
  //---------------------------------------------------------------------------
  private boolean isAttributeDerived(
    Attribute attribute
  ) {
    // the information whether an attribute is derived or not is stored as a
    // tagged value (this is automatically done by Poseidon for UML) 
    for(
      Iterator it = attribute.getTaggedValue().iterator();
      it.hasNext();
    ) {
      TaggedValue taggedValue = (TaggedValue)it.next();
      if ("derived".equals(taggedValue.getType().getName()))
      {
        Collection dataValues = taggedValue.getDataValue();
        return dataValues.isEmpty() ? 
          false : 
          "true".equals(dataValues.iterator().next());
      }
    }
    return false; 
  }
  
  //---------------------------------------------------------------------------
  private boolean isAssociationDerived(
    UmlAssociation association
  ) {
    // the information whether an attribute is derived or not is stored as a
    // tagged value (this is automatically done by Poseidon for UML) 
    for(
      Iterator it = association.getTaggedValue().iterator();
      it.hasNext();
    ) {
      TaggedValue taggedValue = (TaggedValue)it.next();
      if ("derived".equals(taggedValue.getType().getName()))
      {
        Collection dataValues = taggedValue.getDataValue();
        return dataValues.isEmpty() ? 
          false : 
          "true".equals(dataValues.iterator().next());
      }
    }
    return false; 
  }
  
  //---------------------------------------------------------------------------
  private int getAttributeMaxLength(
    Attribute attribute
  ) {
    // the information about the maxLength of an attribute is stored as a
    // tagged value  (openMDX choice)
    for(
      Iterator it = attribute.getTaggedValue().iterator();
      it.hasNext();
    ) {
      TaggedValue taggedValue = (TaggedValue)it.next();
      if ("maxLength".equals(taggedValue.getType().getName()))
      {
        Collection dataValues = taggedValue.getDataValue();
        return dataValues.isEmpty() ? 
          DEFAULT_ATTRIBUTE_MAX_LENGTH : 
          Integer.parseInt((String)dataValues.iterator().next());
      }
    }
    return DEFAULT_ATTRIBUTE_MAX_LENGTH;
  }
  
  //---------------------------------------------------------------------------
  private String toAssociationEndName(
    String assEndNameWithQualifier
  ) {
    if (assEndNameWithQualifier != null && assEndNameWithQualifier.indexOf((char)10) != -1)
    {
      return assEndNameWithQualifier.substring(
        0,
        assEndNameWithQualifier.indexOf((char)10)
      );
    }
    return assEndNameWithQualifier;
  }
  
  //---------------------------------------------------------------------------
  private List toAssociationEndQualifiers(
    String assEndNameWithQualifier
  ) throws ServiceException {
    List qualifiers = new ArrayList();
    if (assEndNameWithQualifier != null && assEndNameWithQualifier.indexOf('[') != -1)
    {
      String qualifierText = assEndNameWithQualifier.substring(
        assEndNameWithQualifier.indexOf('[') + 1,
        assEndNameWithQualifier.indexOf(']')
      );
      qualifiers = this.parseAssociationEndQualifierAttributes(qualifierText);
    }
    return qualifiers;
  }

  //---------------------------------------------------------------------------
  private String getDisplayName(
    DataproviderObject obj
  ) {
    return obj.path().getBase();
  }
  
  //---------------------------------------------------------------------------
  public List getWarnings(
  ) {
    return this.warnings;
  }

  //---------------------------------------------------------------------------
  private void addWarning(
    int warningCode,
    String[] params
  ) {
    this.warnings.add(new Warning(warningCode, params));
  }

  //---------------------------------------------------------------------------
  private int nElements;
  private List warnings;
  public static final int WARNING_INVISIBLE_ATTRIBUTE = 1;
  public static final int WARNING_INVISIBLE_OPERATION = 2;
  public static final int WARNING_DERIVED_AND_CHANGEABLE_ATTRIBUTE = 3;
  public static final int WARNING_INVALID_EXCEPTION_DECLARATION = 4;
  public static final int WARNING_INVALID_PRIMITIVE_TYPE_DECLARATION = 5;

  public class Warning
  {
    private int code;
    private String[] params;
    
    public Warning(int code, String[] params)
    {
      this.code = code;
      this.params = params;
    }
    
    public int getCode() { return this.code; }
    public String[] getParameters() { return this.params; }
  }
}

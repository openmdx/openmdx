/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TogetherMapper_1.java,v 1.6 2009/06/09 12:45:18 hburger Exp $
 * Description: TogetherCppExternalizer_1
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:18 $
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
package org.openmdx.application.mof.mapping.together;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.application.mof.mapping.cci.AliasDef;
import org.openmdx.application.mof.mapping.cci.AttributeDef;
import org.openmdx.application.mof.mapping.cci.ClassDef;
import org.openmdx.application.mof.mapping.cci.OperationDef;
import org.openmdx.application.mof.mapping.cci.StructDef;
import org.openmdx.application.mof.mapping.cci.StructuralFeatureDef;
import org.openmdx.application.mof.mapping.spi.AbstractMapper_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;

@SuppressWarnings("unchecked")
public class TogetherMapper_1 
  extends AbstractMapper_1 {

  //-------------------------------------------------------------------------
  public TogetherMapper_1(
  ) {
    super(
      "model.together"
    );
  }
  
  //---------------------------------------------------------------------------    
  private void classBegin(
    ModelElement_1_0 classDef,
    Set includeTypes,
    UmlMapper umlMapper
  ) throws ServiceException {
      ClassDef mClassDef = new ClassDef(classDef, this.model);
      umlMapper.mapClassBegin(
          mClassDef, 
          includeTypes
      );
  }

  //---------------------------------------------------------------------------    
  private void classEnd(
    ModelElement_1_0 classDef,
    UmlMapper umlMapper
  ) throws ServiceException {
      ClassDef mClassDef = new ClassDef(classDef, this.model);
      umlMapper.mapClassEnd(mClassDef);
  }

  //--------------------------------------------------------------------------------
  private void classAttribute(
    ModelElement_1_0 attributeDef,
    UmlMapper umlMapper
  ) throws ServiceException {
      AttributeDef mAttributeDef = new AttributeDef(
          attributeDef, 
          this.model 
      );
      umlMapper.mapClassAttribute(
          mAttributeDef,
          (Number)attributeDef.objGetValue("maxLength")          
      );
  }
  
  //--------------------------------------------------------------------------------
  private void classOperation(
    ModelElement_1_0 operationDef,
    UmlMapper umlMapper
  ) throws ServiceException {
    OperationDef mOperationDef = new OperationDef(
        operationDef,
        this.model 
    );
    umlMapper.mapClassOperation(mOperationDef);
  }
  
  //--------------------------------------------------------------------------------
  private void classReference(
    String forPackage,
    ModelElement_1_0 classDef,
    ModelElement_1_0 referenceDef,
    UmlMapper umlMapper
  ) throws ServiceException {
    ModelElement_1_0 referencedEnd = this.model.getElement(
      referenceDef.objGetValue("referencedEnd")
    );
    ModelElement_1_0 exposedEnd = this.model.getElement(
      referenceDef.objGetValue("exposedEnd")
    );
    ModelElement_1_0 association = this.model.getElement(
      exposedEnd.objGetValue("container")
    );

    SysLog.trace("----------------------------------------------------------------------------");
    SysLog.trace("found reference " + referenceDef.objGetValue("qualifiedName"));
    SysLog.trace("      referencedEnd " + referencedEnd.objGetValue("qualifiedName"));
    SysLog.trace("      referencedEnd is " + (((Boolean)referencedEnd.objGetValue("isNavigable")).booleanValue() ? "" : "NOT ") + "navigable");
    SysLog.trace("      referencedEnd.aggregation is " + referencedEnd.objGetValue("aggregation"));
    SysLog.trace("      exposedEnd " + exposedEnd.objGetValue("qualifiedName"));
    SysLog.trace("      exposedEnd is " + (((Boolean)exposedEnd.objGetValue("isNavigable")).booleanValue() ? "" : "NOT ") + "navigable");
    SysLog.trace("      exposedEnd.aggregation is " + exposedEnd.objGetValue("aggregation"));
    SysLog.trace("      reference belongs to association " + association.objGetValue("qualifiedName"));
    SysLog.trace("----------------------------------------------------------------------------");
    SysLog.trace("this reference belongs to ...");
  
    if (
      isAssociationPartOfThisClass(
        forPackage,
        association,
        referencedEnd,
        exposedEnd
      )
    ) {
      SysLog.trace("This class", classDef.objGetValue("qualifiedName"));
    
      String label = (String)association.objGetValue("name");
      String annotation = (String)association.objGetValue("annotation");
      Boolean isDerived = (Boolean)association.objGetValue("isDerived");
      String clientRole = (String)exposedEnd.objGetValue("name");
      String clientCardinality = ((String)exposedEnd.objGetValue("multiplicity")).replace('n', '*');
      String clientQualifierName = (String)referencedEnd.objGetValue("qualifierName");
      String clientQualifierType = null;
      if (referencedEnd.objGetValue("qualifierType") != null) {
          clientQualifierType = (String)this.model.getDereferencedType(referencedEnd.objGetValue("qualifierType")).objGetValue("qualifiedName");
      }    
      String clientConstraints = null;
      if(!((Boolean)exposedEnd.objGetValue("isChangeable")).booleanValue()) {
        clientConstraints = "isFrozen";
      }
      String supplierRole = (String)referencedEnd.objGetValue("name");
      String supplierCardinality = ((String)referencedEnd.objGetValue("multiplicity")).replace('n', '*');
      String supplierQualifiedTypeName = (String)this.model.getElementType(referenceDef).objGetValue("qualifiedName");
      String supplierQualifierName = (String)exposedEnd.objGetValue("qualifierName");
      
      String supplierQualifierType = null;
      if (exposedEnd.objGetValue("qualifierType") != null) {
          supplierQualifierType = (String)this.model.getDereferencedType(exposedEnd.objGetValue("qualifierType")).objGetValue("qualifiedName");
      }
      String supplierConstraints = null;
      if(!((Boolean)referencedEnd.objGetValue("isChangeable")).booleanValue()) {
          supplierConstraints = "isFrozen";
      }
      Boolean isDirected = new Boolean(!((Boolean)exposedEnd.objGetValue("isNavigable")).booleanValue());
      String aggregation = (String)referencedEnd.objGetValue("aggregation");
      try {
        umlMapper.mapClassReference(
            label,
            annotation,
            clientCardinality,
            clientQualifierName,
            clientQualifierType,
            clientConstraints,
            clientRole,
            supplierCardinality,
            supplierQualifierName,
            supplierQualifiedTypeName,
            supplierQualifierType,
            supplierConstraints,
            supplierRole,
            aggregation,
            isDirected,
            isDerived            
        );
      } catch(Exception ex) {
        throw new ServiceException(ex).log();
      }
    } else {
      SysLog.trace("      => other class");
    }
  
  }
  
  //---------------------------------------------------------------------------    
  private void primitiveType(
    ModelElement_1_0 classDef,
    UmlMapper umlMapper
  ) throws ServiceException {
    try {
        ClassDef mClassDef = new ClassDef(
            classDef, 
            this.model
        );
        umlMapper.mapPrimitiveType(mClassDef);
    } catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //---------------------------------------------------------------------------    
  private void aliasType(
      ModelElement_1_0 aliasDef,
      UmlMapper umlMapper
  ) throws ServiceException {
    try {
        AliasDef mAliasDef = new AliasDef(
            aliasDef,
            this.model
        );
        umlMapper.mapAliasType(
            mAliasDef
        );
    } catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //---------------------------------------------------------------------------    
  private void structBegin(
    ModelElement_1_0 structDef,
    Set includeTypes,
    UmlMapper umlMapper
  ) throws ServiceException {
      StructDef mStructDef = new StructDef(
          structDef,
          this.model, 
          true // openmdx1
      );
      umlMapper.mapStructBegin(
          mStructDef, 
          includeTypes
      );
  }

  //---------------------------------------------------------------------------    
  private void structEnd(
    ModelElement_1_0 structDef,
    UmlMapper umlMapper
  ) throws ServiceException {
      StructDef mStructDef = new StructDef(
          structDef,
          this.model, 
          true // openmdx1
      );
      umlMapper.mapStructEnd(
          mStructDef
      );
  }

  //--------------------------------------------------------------------------------
  private void structField(
    ModelElement_1_0 structDef,
    ModelElement_1_0 fieldDef,
    UmlMapper umlMapper
  ) throws ServiceException {
      StructuralFeatureDef mFieldDef = new AttributeDef(
          fieldDef,
          this.model 
      );
      umlMapper.mapStructField(
          mFieldDef,
          (Number)fieldDef.objGetValue("maxLength")
      );
  }
  
  //--------------------------------------------------------------------------------
  /**
   * Determines whether a given reference is part of a given class or not.
   */
  private boolean isAssociationPartOfThisClass(
    String forPackage,
    ModelElement_1_0 modelAssociation,
    ModelElement_1_0 modelRefAssociationEnd,
    ModelElement_1_0 modelExpAssociationEnd
  ) throws ServiceException {
    boolean refEndIsNavigable = ((Boolean)modelRefAssociationEnd.objGetValue("isNavigable")).booleanValue();
    boolean expEndIsNavigable = ((Boolean)modelExpAssociationEnd.objGetValue("isNavigable")).booleanValue();

    if (refEndIsNavigable && !expEndIsNavigable) { return true; }
    else if (!refEndIsNavigable && expEndIsNavigable) { return false; }
    else if (refEndIsNavigable && expEndIsNavigable) {
      // association is navigable in both directions
      if (
        modelRefAssociationEnd.objGetValue("aggregation") == AggregationKind.NONE 
      ) {
        // no composite or shared aggregation in both directions
        // Have to do some guess work!!!!
        SysLog.warning(
          "Have no indication to which class the association must be assigned; both ends are possible",
          Arrays.asList(
              "end1=" + modelRefAssociationEnd.objGetValue("qualifiedName") + "; " + 
              "end2=" + modelExpAssociationEnd.objGetValue("qualifiedName")
          )
        );
        if(this.model.isLocal(modelAssociation, forPackage)) {
          return (
            ((String)modelRefAssociationEnd.objGetValue("qualifiedName")).compareTo(
              (String)modelExpAssociationEnd.objGetValue("qualifiedName")
            ) > 0
          );
        } 
        else {
          SysLog.warning("The association does NOT belong to the current package and is therefore ignored (assume that association will be handled at owner package). Association", modelAssociation.objGetValue("qualifiedName"));
          return false;
        }
      } 
      // composite or shared aggregation in one direction
      else {  
        return (modelRefAssociationEnd.objGetValue("aggregation") != AggregationKind.NONE);
      }
    }
    // should not get here!
    return false;
  }

  //-------------------------------------------------------------------------
  public void externalize(
    String qualifiedPackageName,
    Model_1_0 model, 
    ZipOutputStream jar
  ) throws ServiceException {
  
    SysLog.trace("starting...");  
    this.model = model;
    
    try {
      ByteArrayOutputStream umlFile = new ByteArrayOutputStream();
      Writer umlWriter = new OutputStreamWriter(umlFile);
      UmlMapper umlMapper = new UmlMapper(umlWriter, model);

      List packagesToExport = this.getMatchingPackages(qualifiedPackageName);
  
      // export all matching packages
      for(
        Iterator pkgs = packagesToExport.iterator();
        pkgs.hasNext();
      ) {
        ModelElement_1_0 currentPackage = (ModelElement_1_0)pkgs.next();
        String currentPackageName = (String)currentPackage.objGetValue("qualifiedName");

        // process package content
        for(
          Iterator i = this.model.getContent().iterator(); 
          i.hasNext();
        ) {
    
          ModelElement_1_0 modelElement = (ModelElement_1_0)i.next();
          SysLog.trace("processing package element", modelElement.objGetValue("qualifiedName"));
    
          // org:omg:model1:Class
          if(modelElement.objGetClass().equals(ModelAttributes.CLASS)) {
    
            SysLog.trace("processing class", modelElement.objGetValue("qualifiedName"));
  
            // only generate for classes which are content of the modelPackage. 
            // Do not generate for imported model elements
            if(this.model.isLocal(modelElement, currentPackageName)) {
              umlFile.reset();  
              List attributes = new ArrayList();
              List references = new ArrayList();
              List operations = new ArrayList();
              Set includeTypes = new HashSet();
  
              ClassDef classDef = new ClassDef(modelElement, this.model);
  
              // add includes for all supertypes of this class
              for(
                Iterator j = classDef.getSupertypes().iterator();
                j.hasNext();
              ) {
                includeTypes.add(
                  ((ClassDef)j.next()).getQualifiedName()
                );
              }
  
              // get class features and add necessary inlcudes
              for(
                Iterator j = modelElement.objGetList("content").iterator();
                j.hasNext();
              ) {
                ModelElement_1_0 feature = this.model.getElement(j.next());
  
                if(feature.objGetClass().equals(ModelAttributes.ATTRIBUTE)) {
                  attributes.add(feature);
  
                  // add include for type of this attribute
                  ModelElement_1_0 featureType = this.model.getElement(feature.objGetValue("type"));
                  includeTypes.add(featureType.objGetValue("qualifiedName"));
                }
                else if(feature.objGetClass().equals(ModelAttributes.REFERENCE)) {
                  references.add(feature);
  
                  // add include for type of this reference
                  ModelElement_1_0 featureType = this.model.getElement(feature.objGetValue("type"));
                  includeTypes.add(featureType.objGetValue("qualifiedName"));
                }
                else if(feature.objGetClass().equals(ModelAttributes.OPERATION)) {
                  operations.add(feature);
                }
              }              
              this.classBegin(
                  modelElement,
                  includeTypes,
                  umlMapper
              );  
              for(
                Iterator j = attributes.iterator();
                j.hasNext();
              ) {
                this.classAttribute(
                  (ModelElement_1_0)j.next(),
                  umlMapper
                );
              }              
              for(
                Iterator j = references.iterator();
                j.hasNext();
              ) {
                this.classReference(
                  currentPackageName,
                  modelElement,
                  (ModelElement_1_0)j.next(),
                  umlMapper
                );
              }              
              for(
                Iterator j = operations.iterator();
                j.hasNext();
              ) {
                this.classOperation(
                  (ModelElement_1_0)j.next(),
                  umlMapper
                );
              }              
              this.classEnd(
                modelElement,
                umlMapper
              );          
              umlWriter.flush();
              addToZip(jar, umlFile, modelElement, ".h");
            }              
          }
  
          // org:omg:model1:PrimitiveType
          else if(modelElement.objGetClass().equals(ModelAttributes.PRIMITIVE_TYPE)) {
            SysLog.trace("processing primitive type", modelElement.objGetValue("qualifiedName"));
            // only generate for types which are content of the modelPackage. 
            if(this.model.isLocal(modelElement, currentPackageName)) {
              umlFile.reset();  
              this.primitiveType(
                  modelElement,
                  umlMapper
              );
              umlWriter.flush();
              this.addToZip(jar, umlFile, modelElement, ".h");
            }
          }
    
          // org:omg:model1:AliasType
          else if(modelElement.objGetClass().equals(ModelAttributes.ALIAS_TYPE)) {  
            SysLog.trace("processing alias type", modelElement.objGetValue("qualifiedName"));
            // only generate for types which are content of the modelPackage.
            // IMPORTANT: model.isLocal() does not work to test whether an ALIAS is
            // local because isLocal() dereferences alias types.
            ModelElement_1_0 aliasContainer = this.model.getElement(
              modelElement.objGetValue("container")
            );
            String elementPackageName = (String)aliasContainer.objGetValue("qualifiedName");
            if(currentPackageName.startsWith(elementPackageName)) {
              umlFile.reset();  
              this.aliasType(
                  modelElement,
                  umlMapper
              );
              umlWriter.flush();
              this.addToZip(jar, umlFile, modelElement, ".h", false);
            }
          }
  
          // org:omg:model1:StructureType
          else if(modelElement.objGetClass().equals(ModelAttributes.STRUCTURE_TYPE)) {
    
            SysLog.trace("processing structure type", modelElement.objGetValue("qualifiedName"));
  
            // only generate for structs which are content of the modelPackage. 
            if(this.model.isLocal(modelElement, currentPackageName)) {
              umlFile.reset();  
  
              List fields = new ArrayList();
              Set includeTypes = new HashSet();
  
              ClassDef classDef = new ClassDef(
                  modelElement, 
                  this.model
              );
  
              // add includes for all supertypes of this class
              for(
                Iterator j = classDef.getSupertypes().iterator();
                j.hasNext();
              ) {
                includeTypes.add(
                  ((ClassDef)j.next()).getQualifiedName()
                );
              }  
              // get class features and add necessary inlcudes
              for(
                Iterator j = modelElement.objGetList("content").iterator();
                j.hasNext();
              ) {
                ModelElement_1_0 feature = this.model.getElement(j.next());  
                if(feature.objGetClass().equals(ModelAttributes.STRUCTURE_FIELD)) {
                  fields.add(feature);  
                  // add include for type of this attribute
                  ModelElement_1_0 featureType = this.model.getElement(feature.objGetValue("type"));
                  includeTypes.add(featureType.objGetValue("qualifiedName"));
                }
              }             
              this.structBegin(
                modelElement,
                includeTypes,
                umlMapper
              );  
              for(
                Iterator j = fields.iterator();
                j.hasNext();
              ) {
                structField(
                  modelElement,
                  (ModelElement_1_0)j.next(),
                  umlMapper
                );
              }                          
              this.structEnd(
                modelElement,
                umlMapper
              );          
              umlWriter.flush();
              this.addToZip(jar, umlFile, modelElement, ".h");
            }
          }
        }
      }
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
    SysLog.trace("done");
  }

  //--------------------------------------------------------------------------------

}

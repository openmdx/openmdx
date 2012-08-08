/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JMIMapper_1.java,v 1.13 2008/06/28 00:21:35 hburger Exp $
 * Description: JMIExternalizer_1
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:35 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 
package org.openmdx.compatibility.model1.mapping.java;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.omg.model1.code.VisibilityKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.accessor.basic.spi.ModelElement_1;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.mapping.AbstractMapper_1;
import org.openmdx.model1.mapping.AbstractNames;
import org.openmdx.model1.mapping.AttributeDef;
import org.openmdx.model1.mapping.ClassDef;
import org.openmdx.model1.mapping.ClassifierDef;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.OperationDef;
import org.openmdx.model1.mapping.ReferenceDef;
import org.openmdx.model1.mapping.StructDef;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class JMIMapper_1
  extends AbstractMapper_1 {

  //---------------------------------------------------------------------------  
  /**
   * Constructor.
   * @param mappingFormat mapping format defined MapperFactory_1.
   * @param packageSuffix The suffix for the package to be generated in (without leading dot), e.g. 'cci'.
   * @param fileExtension The file extension (without leading point), e.g. 'java'.
   */
  public JMIMapper_1(
    String mappingFormat,
    String packageSuffix,
    String fileExtension
  ) {
    super(
        packageSuffix
    );    
    this.mappingFormat = mappingFormat;
    this.fileExtension = fileExtension;
  }

  //---------------------------------------------------------------------------  
  /**
   * Is called for all ModelAttribute features of a class including suerptyes.
   * This method must check wheter modelAttribute.container = modelClass and
   * behave accordingly. 
 * @param filterCompatibilityMapper TODO
   */
  void jmiAttribute(
      ModelElement_1_0 classDef,
      ModelElement_1_0 attributeDef,
      JMIFilterIntfMapper filterIntfMapper,
      JMIQueryMapper queryMapper,
      JMIFilterImplMapper filterImplMapper,
      JMIInstanceIntfMapper instanceIntfMapper, 
      JMIInstanceImplMapper instanceImplMapper
  ) throws ServiceException {

    SysLog.trace("attribute=" + attributeDef.path());

    String multiplicity = (String)attributeDef.values("multiplicity").get(0);
    boolean attributeTypeIsStruct = this.model.isStructureType(attributeDef.values("type").get(0));
    boolean isDerived = ((Boolean)attributeDef.values("isDerived").get(0)).booleanValue();
    boolean isChangeable = ((Boolean)attributeDef.values("isChangeable").get(0)).booleanValue();
    
    // required for ...Class.create...() operations
    this.processedAttributes.add(attributeDef);
    
    try {
      
      if(
        VisibilityKind.PUBLIC_VIS.equals(
          attributeDef.values("visibility").get(0)
        )
      ) {    
        
        AttributeDef mAttributeDef = new AttributeDef(
            attributeDef,
            this.model, 
            true // openmdx1
        );       
        ClassDef mClassDef = new ClassDef(
            classDef,
            this.model
        );
        
        // Attributes are read-only if they are not changeable or derived
        boolean isReadOnly = !isChangeable || isDerived;
    
        // setter/getter interface only if modelAttribute.container = modelClass. 
        // Otherwise inherit from super interfaces.
        if(attributeDef.values("container").get(0).equals(classDef.path())) {
  
          // filter interface
          if(filterIntfMapper != null) {
  
            // attribute is struct
            if(attributeTypeIsStruct) {
              filterIntfMapper.mapIntfAttributeIsStruct(mClassDef, mAttributeDef);
  
            }

            // attribute is non-struct
            else {
              filterIntfMapper.mapIntfAttributeIsNotStruct(mClassDef, mAttributeDef);
            }
          }    
          // filter interface
          if(queryMapper != null) {
  
            // attribute is struct
            if(attributeTypeIsStruct) {
                queryMapper.mapStructurealFeature(mClassDef, mAttributeDef);
  
            }

            // attribute is non-struct
            else {
                queryMapper.mapStructurealFeature(mClassDef, mAttributeDef);
            }
          }
          // Note:
          // Set operations in interfaces are generated only if the attribute
          // is changeable and is not derived.

          // set/get interface 0..1
          if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
            instanceIntfMapper.mapIntfAttributeGet0_1(mAttributeDef);

            if(!isReadOnly) {
              instanceIntfMapper.mapIntfAttributeSet0_1(mAttributeDef);
            }
          }
  
          // set/get interface 1..1
          else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
            instanceIntfMapper.mapIntfAttributeGet1_1(mAttributeDef);

            if(!isReadOnly) {
              instanceIntfMapper.mapIntfAttributeSet1_1(mAttributeDef);
            }
          }
  
          // set/get interface list
          else if(Multiplicities.LIST.equals(multiplicity)) {
            instanceIntfMapper.mapIntfAttributeGetList(mAttributeDef);

            if(!isReadOnly) {
              instanceIntfMapper.mapIntfAttributeSetList(mAttributeDef);
            }
          }  
  
          // set/get interface set
          else if(Multiplicities.SET.equals(multiplicity)) {
            instanceIntfMapper.mapIntfAttributeGetSet(mAttributeDef);

            if(!isReadOnly) {
              instanceIntfMapper.mapIntfAttributeSetSet(mAttributeDef);
            }
          }  
  
          // set/get interface sparsearray
          else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
            instanceIntfMapper.mapIntfAttributeGetSparseArray(mAttributeDef);
          }  
  
          // set/get interface map
          else if(Multiplicities.MAP.equals(multiplicity)) {
            instanceIntfMapper.mapIntfAttributeGetMap(mAttributeDef);

            if(!isReadOnly) {
              instanceIntfMapper.mapIntfAttributeSetMap(mAttributeDef);
            }
          }  
  
          // set/get interface stream
          else if(Multiplicities.STREAM.equals(multiplicity)) {
            instanceIntfMapper.mapIntfAttributeGetStream(mAttributeDef);

            if(!isReadOnly) {
              instanceIntfMapper.mapIntfAttributeSetStream(mAttributeDef);
            }
          }  

          // set/get interface with lower and upper bound, e.g. 0..n
          else {
            
            instanceIntfMapper.mapIntfAttributeGetList(mAttributeDef);

            if(!isReadOnly) {
              instanceIntfMapper.mapIntfAttributeSetList(mAttributeDef);
            }
          }  
        }
    
        // filter impl
        if(filterImplMapper != null) {
  
          // attribute is struct
          if(attributeTypeIsStruct) {
            filterImplMapper.mapImplAttributeIsStruct(mAttributeDef);
          }

          // attribute is non-struct
          else {
            filterImplMapper.mapImplAttributeIsNotStruct(mAttributeDef);
          }
        }
  
        // instance implementation for abstract and non-abstract classes
        // (required for JMI plugins)
               
        // set/get implementation 0..1
        if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
            
            // get
          instanceImplMapper.mapImplAttributeGet0_1(mAttributeDef);

          // set  
            instanceImplMapper.mapImplAttributeSet0_1(mAttributeDef, isReadOnly);

        }

        // set/get implementation 1..1
        else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {

            // get
          instanceImplMapper.mapImplAttributeGet1_1(mAttributeDef);

          // set
          instanceImplMapper.mapImplAttributeSet1_1(mAttributeDef, isReadOnly);

        }
        
        // set/get implementation list
        else if(Multiplicities.LIST.equals(multiplicity)) {
            
            // get
          instanceImplMapper.mapImplAttributeGetList(mAttributeDef);

          // set
          instanceImplMapper.mapImplAttributeSetList(mAttributeDef, isReadOnly);
        }
        
        // set/get implementation set
        else if(Multiplicities.SET.equals(multiplicity)) {
            
            // get
          instanceImplMapper.mapImplAttributeGetSet(mAttributeDef);

          // set
          instanceImplMapper.mapImplAttributeSetSet(mAttributeDef, isReadOnly);
        }
        
        // set/get implementation sparsearray
        else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
            
            // get
          instanceImplMapper.mapImplAttributeGetSparseArray(mAttributeDef);

        }
        
        // set/get implementation map
        else if(Multiplicities.MAP.equals(multiplicity)) {
            
            // get
          instanceImplMapper.mapImplAttributeGetMap(mAttributeDef);

          // set
          instanceImplMapper.mapImplAttributeSetMap(mAttributeDef, isReadOnly);
        }
        
        // set/get implementation stream
        else if(Multiplicities.STREAM.equals(multiplicity)) {
            
            // get
          instanceImplMapper.mapImplAttributeGetStream(mAttributeDef);

          // set
          instanceImplMapper.mapImplAttributeSetStream(mAttributeDef, isReadOnly);
        }

        // set/get implementation with lower and upper bound, e.g. 0..n
        else {
            
            // get
          instanceImplMapper.mapImplAttributeGetList(mAttributeDef);

          // set
          instanceImplMapper.mapImplAttributeSetList(mAttributeDef, isReadOnly);
        }
      }
    } 
    catch(Exception ex) {
      throw new ServiceException(ex);
    }
  }

  //---------------------------------------------------------------------------    
  void jmiReference(
      ModelElement_1_0 classDef,
      ModelElement_1_0 referenceDef,
      JMIFilterIntfMapper filterIntfMapper,
      JMIQueryMapper queryMapper,
      JMIFilterImplMapper filterImplMapper,
      JMIInstanceIntfMapper instanceIntfMapper, 
      JMIInstanceImplMapper instanceImplMapper, 
      Object newParam
  ) throws ServiceException {

    SysLog.trace("reference=" + referenceDef.path());

    ModelElement_1_0 referencedEnd = this.model.getElement(
      referenceDef.values("referencedEnd").get(0)
    );
    DataproviderObject association = (DataproviderObject)this.model.getElement(
      referencedEnd.values("container").get(0)
    );

    SysLog.trace("referencedEnd=", referencedEnd);
    SysLog.trace("association=", association);

    // setter/getter interface only if modelAttribute.container = modelClass. 
    // Otherwise inherit from super interfaces.
    boolean memberOfClass = referenceDef.values("container").get(0).equals(classDef.path());
    String multiplicity = (String)referenceDef.values("multiplicity").get(0);
    String visibility = (String)referenceDef.values("visibility").get(0);
    List qualifierNames = referencedEnd.values("qualifierName");
    List qualifierTypes = referencedEnd.values("qualifierType");
    boolean isChangeable = ((Boolean)referenceDef.values("isChangeable").get(0)).booleanValue();
    boolean isDerived = ((Boolean)association.values("isDerived").get(0)).booleanValue();

    // check whether this reference is stored as attribute
    // required for ...Class.create...() operations
    // Note:
    if(this.model.referenceIsStoredAsAttribute(referenceDef)) {
        SysLog.trace("the reference <" + referenceDef.values("qualifiedName").get(0) + "> is stored as attribute");
        DataproviderObject referenceAsAttribute = null;
        this.processedAttributes.add(
            referenceAsAttribute = new ModelElement_1(referenceDef)
        );
        if(qualifierNames.size() > 0) {
            // 0..n association        
            // set multiplicity to 0..n, this ensures that the instance creator uses
            // a multivalued parameter for this reference attribute
            referenceAsAttribute.clearValues("multiplicity").add(Multiplicities.MULTI_VALUE);
        }
        referenceAsAttribute.values("isDerived").add(
            association.values("isDerived").get(0)
        );
    }

    try {
      
      if(VisibilityKind.PUBLIC_VIS.equals(visibility)) {  
  
        ReferenceDef mReferenceDef = new ReferenceDef(
            referenceDef,
            this.model, 
            true // openmdx1
        );
        ClassDef mClassDef = new ClassDef(
            classDef,
            this.model
        );
        
        /**
         * References are read-only if they are not changeable. In addition they are also
         * read-only if the reference is derived and stored as attribute.
         */
        boolean isReadOnly = !isChangeable || (isDerived && this.model.referenceIsStoredAsAttribute(referenceDef));
  
        // filter for references stored as attributes
        if(this.model.referenceIsStoredAsAttribute(referenceDef)) {  
            if(memberOfClass) {
                if(filterIntfMapper != null) {
                    filterIntfMapper.mapIntfReference(mClassDef, mReferenceDef);
                } 
                if(queryMapper != null) {
                    queryMapper.mapStructurealFeature(mClassDef, mReferenceDef);
                }
            } 
            if(filterImplMapper != null) {
                filterImplMapper.mapImplReference(mReferenceDef);
            }
        }
        
        // no qualifier, multiplicity must be [0..1|1..1|0..n]
        if(qualifierNames.size() == 0) {
  
          // 0..1
          if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {

            // get
            if(memberOfClass) {
              instanceIntfMapper.mapIntfReferenceGet0_1NoQualifier(mReferenceDef);
            }

            // create impl for non-abstract and abstract classes (JMI plugins)
            instanceImplMapper.mapImplReferenceGet0_1NoQualifier(mReferenceDef);
            
            // set/remove (remove only for 0..1 references)
            if(!isReadOnly) {
              if(memberOfClass) {
                instanceIntfMapper.mapIntfReferenceSetNoQualifier(mReferenceDef);
                instanceIntfMapper.mapIntfReferenceRemoveOptional(mReferenceDef);
              }
            }
            
            // create impl for non-abstract and abstract classes (JMI plugins)              
            // set
            instanceImplMapper.mapImplReferenceSetNoQualifier(mReferenceDef, isReadOnly);

            // remove
            instanceImplMapper.mapImplReferenceRemoveOptional(mReferenceDef, isReadOnly);

          }
          
          // 1..1
          else if (Multiplicities.SINGLE_VALUE.equals(multiplicity)) {

            // get
            if(memberOfClass) {
              instanceIntfMapper.mapIntfReferenceGet1_1NoQualifier(mReferenceDef);
            }

            // create impl for non-abstract and abstract classes (JMI plugins)
            instanceImplMapper.mapImplReferenceGet1_1NoQualifier(mReferenceDef);

            // set
            if(!isReadOnly) {
              if(memberOfClass) {
                instanceIntfMapper.mapIntfReferenceSetNoQualifier(mReferenceDef);
              }
            }

            // create impl for non-abstract and abstract classes (JMI plugins)            
            instanceImplMapper.mapImplReferenceSetNoQualifier(mReferenceDef, isReadOnly);
          }
          
          // 0..n
          else {

            // get
            if(memberOfClass) {
              instanceIntfMapper.mapIntfReferenceGet0_nWithFilter(mReferenceDef);
            }
            // create impl for non-abstract and abstract classes (JMI plugins)
            instanceImplMapper.mapImplReferenceGet0_nWithFilter(mReferenceDef);
          }          
        }
  
        // 0..n association where qualifier qualifies 1..1, 0..1, 0..n
        else { 
  
          boolean qualifierTypeIsPrimitive = 
              this.model.isPrimitiveType(qualifierTypes.get(0));
          boolean qualifiesUniquely = 
              Multiplicities.OPTIONAL_VALUE.equals(multiplicity) || 
              Multiplicities.SINGLE_VALUE.equals(multiplicity);

          // create impl for non-abstract and abstract classes (JMI plugins)                
          instanceImplMapper.mapImplReferenceSetWithQualifier(mReferenceDef, isReadOnly);

          // qualifier is optional if not complex
          if(qualifiesUniquely) {

            if(memberOfClass) {
              if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                  instanceIntfMapper.mapIntfReferenceGet0_1WithQualifier(mReferenceDef);
              }
              else {
                  instanceIntfMapper.mapIntfReferenceGet1_1WithQualifier(mReferenceDef);
              }     
              if(this.model.referenceIsStoredAsAttribute(referenceDef)) {
                  instanceIntfMapper.mapIntfReferenceGet0_nNoFilter(mReferenceDef);                  
              }
              else {
                  instanceIntfMapper.mapIntfReferenceGet0_nWithFilter(mReferenceDef);
              }
            }
            // Create impl for non-abstract and abstract classes (JMI plugins)  
            if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
                instanceImplMapper.mapImplReferenceGet0_1WithQualifier(mReferenceDef);
            }
            else {
                instanceImplMapper.mapImplReferenceGet1_1WithQualifier(mReferenceDef);
            }
            if(this.model.referenceIsStoredAsAttribute(referenceDef)) {
                instanceImplMapper.mapImplReferenceGet0_nNoFilter(mReferenceDef);
            }
            else {
                instanceImplMapper.mapImplReferenceGet0_nWithFilter(mReferenceDef);                
            }
          }
      
          // !qualifiesUniquely. qualifier is required
          else {
  
            if(!this.model.referenceIsStoredAsAttribute(referenceDef)) {  
              if(memberOfClass) {
//              instanceIntfMapper.mapIntfReferenceGet0_nWithFilter(mReferenceDef);
                instanceIntfMapper.mapIntfReferenceGet0_nWithQualifier(mReferenceDef);
              }
              // create impl for non-abstract and abstract classes (JMI plugins)              
//            instanceImplMapper.mapImplReferenceGet0_nWithFilter(mReferenceDef);
              instanceImplMapper.mapImplReferenceGet0_nWithQualifier(mReferenceDef);
            }
            
            /**
             * It is NOT possible to have this situation. If a qualifier does
             * not qualify uniquely, then it must be a non-primitive type.
             * (If the qualifier type is primitive, then the multiplicity must
             * be 0..1 or 1..1, i.e. the qualifier qualifes uniquely [openMDX 
             * Constraint].)
             * Non-primitive, ambiguous qualifiers are used to indicate the 
             * owner of the associated element. The owner is only used if the
             * reference is NOT stored as attribute (otherwise you would know 
             * your associated elements).
             */
            else {              
              throw new ServiceException(
                  BasicException.Code.DEFAULT_DOMAIN,
                  BasicException.Code.ASSERTION_FAILURE,
                  new BasicException.Parameter[]{
                      new BasicException.Parameter("reference", referenceDef.path()),
                      new BasicException.Parameter("qualifier", qualifierNames.get(0))
                  },
                  "reference with non-primitive, ambiguous qualifier cannot be stored as attribute"
              );
            }
          }
  
          // add with qualifier
          if(qualifiesUniquely && qualifierTypeIsPrimitive) {

            if(!isReadOnly) {
              if(memberOfClass) {
                instanceIntfMapper.mapIntfReferenceAddWithQualifier(mReferenceDef);
              }
            }

            // create impl for non-abstract and abstract classes
            instanceImplMapper.mapImplReferenceAddWithQualifier(mReferenceDef, isReadOnly);
          }

          // add without qualifier
          if(!isReadOnly) {
            if(memberOfClass) {
              instanceIntfMapper.mapIntfReferenceAddWithoutQualifier(mReferenceDef);
            }
          }

          // create impl for non-abstract and abstract classes (JMI plugins)            
          instanceImplMapper.mapImplReferenceAddWithoutQualifier(mReferenceDef, isReadOnly);

          // remove with qualifier if qualifier qualifies uniquely
          if(qualifiesUniquely) {

            if(!isReadOnly) {
              if(memberOfClass) {
                instanceIntfMapper.mapIntfReferenceRemoveWithQualifier(mReferenceDef);
              }
            }

            // create impl for non-abstract and abstract classes (JMI plugins)
            instanceImplMapper.mapImplReferenceRemoveWithQualifier(mReferenceDef, isReadOnly);
          }

        }
      }
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //---------------------------------------------------------------------------    
  void jmiOperation(
      ModelElement_1_0 classDef,
      ModelElement_1_0 operationDef,
      JMIInstanceIntfMapper instanceIntfMapper,
      JMIInstanceImplMapper instanceImplMapper
  ) throws ServiceException {

    SysLog.trace("operation=" + operationDef.path());

    boolean isMemberOfClass = operationDef.values("container").get(0).equals(classDef.path());

    if(VisibilityKind.PUBLIC_VIS.equals(operationDef.values("visibility").get(0))) {

      try {
      
          OperationDef mOperationDef = new OperationDef(
              operationDef,
              this.model, 
              true // openmdx1
          );

          if(isMemberOfClass) {
              instanceIntfMapper.mapIntfOperation(mOperationDef);
          }
  
          // create impl for non-abstract and abstract classes (JMI plugins)
          instanceImplMapper.mapImplOperation(mOperationDef);

      } 
      catch(Exception ex) {
        throw new ServiceException(ex).log();
      }
    }
  }

  //---------------------------------------------------------------------------    
  void jmiException(
    ModelElement_1_0 exceptionDef,
    JMIExceptionImplMapper exceptionImplMapper
  ) throws ServiceException {

    SysLog.trace("exception=" + exceptionDef.path());
    
    if(VisibilityKind.PUBLIC_VIS.equals(exceptionDef.values("visibility").get(0))) {
      
      try {
          exceptionImplMapper.mapImplException();
      } 
      catch(Exception ex) {
          throw new ServiceException(ex).log();
      }
    }
  }

  //---------------------------------------------------------------------------    
  void jmiBeginClass(
      ModelElement_1_0 classDef,
      JMIClassIntfMapper classIntfMapper,
      JMIClassImplMapper classImplMapper,
      JMIInstanceIntfMapper instanceIntfMapper,
      JMIInstanceImplMapper instanceImplMapper
  ) throws ServiceException {

    SysLog.trace("class=" + classDef.path());

    boolean isAbstract = ((Boolean)classDef.values("isAbstract").get(0)).booleanValue();

    try {
      
      // only generate for non-abstract classes
      if(!isAbstract) {
    
        // class interface
        classIntfMapper.mapIntfBegin();
      
        // class implementation
        classImplMapper.mapImplBegin();
        
      }
    
      // object interface
      instanceIntfMapper.mapIntfBegin();
    
      // object implementation. Also generate for abstract classes which
      // is useful for the implementation of JMI plugins.
      instanceImplMapper.mapImplBegin();
  
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
    this.processedAttributes = new ArrayList();

  }

  //---------------------------------------------------------------------------    
  void jmiObjectCreator(
      ModelElement_1_0 classDef,
      ModelElement_1_0 supertypeDef,
      JMIClassIntfMapper classIntfMapper,
      JMIClassImplMapper classImplMapper
  ) throws ServiceException {

    SysLog.trace("class=" + classDef.path());
    
    // traverse all processed attributes (this includes all regular attributes and
    // those references that are stored as attributes) to find out which 
    // attributes are mandatory
    List allAttributes = new ArrayList();
    List requiredAttributes = new ArrayList();
    for(
      Iterator i = this.processedAttributes.iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 attributeDef = (ModelElement_1_0)i.next();

      // attribute member of class, non-derived and public
      if(
        ((supertypeDef == null) || !supertypeDef.values("feature").contains(attributeDef.path())) &&
        !((Boolean)attributeDef.values("isDerived").get(0)).booleanValue() &&
        VisibilityKind.PUBLIC_VIS.equals(attributeDef.values("visibility").get(0))
      ) {
        AttributeDef att = new AttributeDef(
          attributeDef,
          this.model, 
          true // openmdx1
        );
        allAttributes.add(att);
        
        // required attribute
        String multiplicity = (String)attributeDef.values("multiplicity").get(0);
        SysLog.trace("attribute", "" + attributeDef.values("qualifiedName").get(0) + "; multiplicity=" + multiplicity);
        if(Multiplicities.SINGLE_VALUE.equals(attributeDef.values("multiplicity").get(0))) {
          requiredAttributes.add(att);
        } 
        else {
          /* isAllAttributesRequired = false; */
        }
      }
    }

    try {
      // creators
      if(supertypeDef == null) {        

        // check whether all attributes are mandatory and whether there are some
        // mandatory attributes at all; if so, do not generate creator 
        // (otherwise we get a duplicate creator definition)
        if(/*!isAllAttributesRequired &&*/ requiredAttributes.size() > 0) {
          classIntfMapper.mapIntfInstanceCreatorRequiredAttributes(requiredAttributes);      
          classImplMapper.mapImplInstanceCreatorRequiredAttributes(requiredAttributes);
        }
      }
      
      // extenders
      else {
          ClassDef mSuperclassDef = new ClassDef(
              supertypeDef,
              this.model
          );
        
        // check whether all attributes are mandatory and whether there are some
        // mandatory attributes at all; if so, do not generate creator 
        // (otherwise we get a duplicate creator definition)
        if(/*!isAllAttributesRequired &&*/ requiredAttributes.size() > 0) {
          classIntfMapper.mapIntfInstanceExtenderRequiredAttributes(mSuperclassDef, requiredAttributes);    
          classImplMapper.mapImplInstanceExtenderRequiredAttributes(mSuperclassDef, requiredAttributes);
        }
      }
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //---------------------------------------------------------------------------    
  void jmiEndClass(
      ModelElement_1_0 classDef,
      JMIClassIntfMapper classIntfMapper,
      JMIClassImplMapper classImplMapper,
      JMIInstanceIntfMapper instanceIntfMapper,
      JMIInstanceImplMapper instanceImplMapper
  ) throws ServiceException {

    // add additional template references to context
    List structuralFeatures = new ArrayList();
    List operations = new ArrayList();
    for(
      Iterator i = classDef.values("feature").iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 feature = this.model.getElement(i.next());
      
      if(
        VisibilityKind.PUBLIC_VIS.equals(feature.values("visibility").get(0))
      ) {
        if(
          feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE)
        ) {
          structuralFeatures.add(
            new AttributeDef(
              feature, 
              this.model, 
              true // openmdx1
            )
          );
        }
        else if(
          feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)
        ) {
          ModelElement_1_0 referencedEnd = this.model.getElement(
            feature.values("referencedEnd").get(0)
          );
          List qualifierTypes = referencedEnd.values("qualifierType");
  
          // skip references for which a qualifier exists and the qualifier is
          // not a primitive type
          if(
            qualifierTypes.isEmpty() ||
            this.model.isPrimitiveType(qualifierTypes.get(0))
          ) {
            structuralFeatures.add(
              new ReferenceDef(
                feature,
                model, 
                true // openmdx1
              )
            );
          }
        }
        else if(
          feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION)
        ) {
          operations.add(
            new OperationDef(
              feature,
              this.model, 
              true // openmdx1
            )
          );
        }
      }
    }
    boolean classIsAbstract = ((Boolean)classDef.values("isAbstract").get(0)).booleanValue();

    try {
      if(!classIsAbstract) {
  
        // standard creators
        jmiObjectCreator(
          classDef,
          null,
          classIntfMapper,
          classImplMapper
        );
    
        // narrow creators
        SysLog.trace("creators for", classDef.path());
        SysLog.trace("supertypes", classDef.values("allSupertype"));
        for(
          Iterator i = classDef.values("allSupertype").iterator();
          i.hasNext();
        ) {
          ModelElement_1_0 supertype = this.model.getDereferencedType(i.next());
          if(!supertype.path().equals(classDef.path())) {
            SysLog.trace("creating", supertype.path());
            this.jmiObjectCreator(
              classDef,
              supertype,
              classIntfMapper,
              classImplMapper
            );
          }
          else {
            SysLog.trace("skipping", supertype.path());
          }
        }
        classIntfMapper.mapIntfEnd();  
        classImplMapper.mapImplEnd();
      }
      
      // impl for non-abstract and abstract classes (JMI plugins)
      instanceImplMapper.mapImplEnd();
      instanceIntfMapper.mapIntfEnd();
      
    } catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //---------------------------------------------------------------------------    
  void jmiBeginFilter(
      ClassifierDef mClassifierDef,
      JMIFilterIntfMapper filterIntfMapper,
      JMIQueryMapper queryMapper, 
      JMIFilterImplMapper filterImplMapper
  ) throws ServiceException {
    try {
        filterIntfMapper.mapIntfBegin(mClassifierDef);
        queryMapper.mapBegin(mClassifierDef);
        filterImplMapper.mapImplBegin(mClassifierDef);
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //---------------------------------------------------------------------------    
  void jmiEndFilter(
      JMIFilterIntfMapper filterIntfMapper,
      JMIQueryMapper queryMapper, 
      JMIFilterImplMapper filterImplMapper
  ) throws ServiceException {
    try {
        filterIntfMapper.mapIntfEnd();  
        queryMapper.mapEnd();  
        filterImplMapper.mapImplEnd();
    } 
    catch(Exception ex) {
        throw new ServiceException(ex).log();
    }
  }

  //---------------------------------------------------------------------------    
  void jmiBeginStruct(
    ModelElement_1_0 structDef,
    JMIStructIntfMapper structIntfMapper,
    JMIStructImplMapper structImplMapper
  ) throws ServiceException {

    SysLog.trace("struct=" + structDef.path());

    try {
      structIntfMapper.mapIntfBegin();  
      structImplMapper.mapImplBegin();
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
    this.processedAttributes = new ArrayList();

  }

    //--------------------------------------------------------------------------------
  /**
   * Is called for all ModelAttribute features of a class including suerptyes.
   * This method must check whether modelAttribute.container = modelClass and
   * behave accordingly. 
 * @param queryMapper TODO
   */
  void jmiStructureField(
      ModelElement_1_0 classDef,
      ModelElement_1_0 structureFieldDef,
      JMIFilterIntfMapper filterIntfMapper,
      JMIQueryMapper queryMapper,
      JMIFilterImplMapper filterImplMapper,
      JMIStructIntfMapper structIntfMapper, 
      JMIStructImplMapper structImplMapper
  ) throws ServiceException {

    SysLog.trace("structure field=" + structureFieldDef.path());

    boolean classIsAbstract = ((Boolean)classDef.values("isAbstract").get(0)).booleanValue();
    String multiplicity = (String)structureFieldDef.values("multiplicity").get(0);
    boolean fieldTypeIsStruct = this.model.isStructureType(structureFieldDef.values("type").get(0));
    // required for ...Class.create...() operations
    this.processedAttributes.add(structureFieldDef);
    
    try {
        AttributeDef mStructureFieldDef = new AttributeDef(
            structureFieldDef,
            this.model, 
            true // openmdx1
        );
        StructDef mStructDef = new StructDef(
            classDef,
            this.model, 
            true // openmdx1
        );
      // getter interface only if modelAttribute.container = modelClass. 
      // Otherwise inherit from super interfaces.
      if(structureFieldDef.values("container").get(0).equals(classDef.path())) {
  
        // filter interface
        if(filterIntfMapper != null) {
          SysLog.trace("generating filter interface ...");
  
          // field is struct
          if(fieldTypeIsStruct) {
            filterIntfMapper.mapIntfStructureFieldIsStruct(mStructDef, mStructureFieldDef);
  
          }

          // field is non-struct
          else {
            filterIntfMapper.mapIntfStructureFieldIsNotStruct(mStructDef, mStructureFieldDef);
          }
        }
        if(queryMapper != null) {
            SysLog.trace("generating query interface ...");
            queryMapper.mapStructurealFeature(mStructDef, mStructureFieldDef);
          }

        // get interface 0..1
        if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
          structIntfMapper.mapIntfFieldGet0_1(mStructureFieldDef);
        }
  
        // get interface 1..1
        else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
          structIntfMapper.mapIntfFieldGet1_1(mStructureFieldDef);
        }
  
        // get interface list
        else if(Multiplicities.LIST.equals(multiplicity)) {
          structIntfMapper.mapIntfFieldGetList(mStructureFieldDef);
        }
  
        // get interface set
        else if(Multiplicities.SET.equals(multiplicity)) {
          structIntfMapper.mapIntfFieldGetSet(mStructureFieldDef);
        }
  
        // get interface sparsearray
        else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
          structIntfMapper.mapIntfFieldGetSparseArray(mStructureFieldDef);
        }
  
        // get interface stream
        else if(Multiplicities.STREAM.equals(multiplicity)) {
          structIntfMapper.mapIntfFieldGetStream(mStructureFieldDef);
        }
  
        // get interface with lower and upper bound, e.g. 0..n
        else {
          structIntfMapper.mapIntfFieldGetList(mStructureFieldDef);
        }  
      }
  
      // filter impl
      if(filterImplMapper != null) {
        SysLog.trace("generating filter implementation ...");
  
        // field is struct
        if(fieldTypeIsStruct) {
          filterImplMapper.mapImplStructureFieldIsStruct(mStructureFieldDef);
        }

        // field is non-struct
        else {
          filterImplMapper.mapImplStructureFieldIsNotStruct(mStructureFieldDef);
        }
      }
  
      // instance implementation
      if(!classIsAbstract) {
    
        // get implementation 0..1
        if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
          structImplMapper.mapImplFieldGet0_1(mStructureFieldDef);
        }
  
        // get implementation 1..1
        else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
          structImplMapper.mapImplFieldGet1_1(mStructureFieldDef);
        }
  
        // get implementation list
        else if(Multiplicities.LIST.equals(multiplicity)) {
          structImplMapper.mapImplFieldGetList(mStructureFieldDef);
        }
  
        // get implementation set
        else if(Multiplicities.SET.equals(multiplicity)) {
          structImplMapper.mapImplFieldGetSet(mStructureFieldDef);
        }
  
        // get implementation sparsearray
        else if(Multiplicities.SPARSEARRAY.equals(multiplicity)) {
          structImplMapper.mapImplFieldGetSparseArray(mStructureFieldDef);
        }
  
        // get implementation stream
        else if(Multiplicities.STREAM.equals(multiplicity)) {
          structImplMapper.mapImplFieldGetStream(mStructureFieldDef);
        }
  
        // get implementation with lower and upper bound, e.g. 0..n
        else {
          structImplMapper.mapImplFieldGetList(mStructureFieldDef);
        }  
      }
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //---------------------------------------------------------------------------    
  void jmiEndStruct(
    JMIStructIntfMapper structIntfMapper,
    JMIStructImplMapper structImplMapper
  ) throws ServiceException {

    try {
      structIntfMapper.mapIntfEnd();  
      structImplMapper.mapImplEnd();
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //--------------------------------------------------------------------------------
  void jmiBeginPackage(
      String forPackage,
      JMIPackageIntfMapper pkgIntfMapper,
      JMIPackageForwardCompatibilityMapper pkgCompatibilityMapper, 
      JMIPackageImplMapper pkgImplMapper
  ) throws ServiceException {
    try {
        pkgIntfMapper.mapIntfBegin(forPackage);
        pkgCompatibilityMapper.mapIntfBegin(forPackage);
        pkgImplMapper.mapImplBegin(forPackage);
    } 
    catch(Exception ex) {
        throw new ServiceException(ex).log();
    }
  }

  //--------------------------------------------------------------------------------
  void jmiEndPackage(
      String forPackage,
      JMIPackageIntfMapper pkgIntfMapper,
      JMIPackageForwardCompatibilityMapper pkgCompatibilityMapper, 
      JMIPackageImplMapper pkgImplMapper
  ) throws ServiceException {
    try {
      pkgIntfMapper.mapIntfEnd();
      pkgCompatibilityMapper.mapIntfEnd();
      pkgImplMapper.mapImplEnd(forPackage);
  
    } catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }

  //--------------------------------------------------------------------------------
  void jmiObjectMarshaller(
      ModelElement_1_0 classDef,
      JMIPackageIntfMapper pkgIntfMapper,
      JMIPackageForwardCompatibilityMapper pkgCompatibilityMapper, 
      JMIPackageImplMapper pkgImplMapper
  ) throws ServiceException {
    try {
        ClassDef mClassDef = new ClassDef(
            classDef,
            this.model
        );
        pkgIntfMapper.mapIntfClassAccessor(mClassDef);  
        pkgCompatibilityMapper.mapIntfClassAccessor(mClassDef);  
        pkgImplMapper.mapImplClassAccessor(mClassDef);
    } catch(Exception ex) {
      throw new ServiceException(ex);
    }

  }
  
  //--------------------------------------------------------------------------------
  private void jmiStructCreator(
      ModelElement_1_0 structDef,
      JMIPackageIntfMapper pkgIntfMapper,
      JMIPackageForwardCompatibilityMapper pkgCompatibilityMapper, 
      JMIPackageImplMapper pkgImplMapper
  ) throws ServiceException {

    try {
        StructDef mStructDef = new StructDef(
            structDef,
            this.model, 
            true // openmdx1
        );
        pkgIntfMapper.mapIntfStructCreator(mStructDef);
        pkgCompatibilityMapper.mapIntfStructCreator(mStructDef);
        pkgImplMapper.mapImplStructCreator(mStructDef);
    } catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }
  
  //--------------------------------------------------------------------------------
  // Generates create filter operations for a struct or a class in the current 
  // package
  private void jmiFilterCreator(
      ClassifierDef mClassifierDef,
      JMIPackageIntfMapper pkgIntfMapper,
      JMIPackageForwardCompatibilityMapper pkgCompatibilityMapper, 
      JMIPackageImplMapper pkgImplMapper
  ) throws ServiceException {
    try {
        pkgIntfMapper.mapIntfFilterCreator(mClassifierDef);
        pkgCompatibilityMapper.mapIntfFilterCreator(mClassifierDef);
        pkgImplMapper.mapImplFilterCreator(mClassifierDef);
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
  }
  
  //---------------------------------------------------------------------------    
  public void externalize(
    String qualifiedPackageName,
    Model_1_3 model,
    ZipOutputStream zip
  ) throws ServiceException {
  
    SysLog.trace("starting...");
    
    this.model = model;

    List packagesToExport = this.getMatchingPackages(qualifiedPackageName);
    SysLog.detail("exporting packages " + packagesToExport);
    
    try {

      // allocate streams one time
      ByteArrayOutputStream pkgIntfFile = new ByteArrayOutputStream();
      ByteArrayOutputStream pkgCompatibilityFile = new ByteArrayOutputStream();
      ByteArrayOutputStream pkgImplFile = new ByteArrayOutputStream();
      ByteArrayOutputStream classIntfFile = new ByteArrayOutputStream();
      ByteArrayOutputStream classImplFile = new ByteArrayOutputStream();
      ByteArrayOutputStream instanceIntfFile = new ByteArrayOutputStream();
      ByteArrayOutputStream instanceImplFile = new ByteArrayOutputStream();
      ByteArrayOutputStream structIntfFile = new ByteArrayOutputStream();
      ByteArrayOutputStream structImplFile = new ByteArrayOutputStream();
      ByteArrayOutputStream filterIntfFile = new ByteArrayOutputStream();
      ByteArrayOutputStream queryFile = new ByteArrayOutputStream();
      ByteArrayOutputStream filterImplFile = new ByteArrayOutputStream();
      ByteArrayOutputStream exceptionImplFile = new ByteArrayOutputStream();

      Writer classIntfWriter = new OutputStreamWriter(classIntfFile);
      Writer classImplWriter = new OutputStreamWriter(classImplFile);
      Writer filterIntfWriter = new OutputStreamWriter(filterIntfFile);
      Writer queryWriter = new OutputStreamWriter(queryFile);
      Writer filterImplWriter = new OutputStreamWriter(filterImplFile);
      Writer instanceIntfWriter = new OutputStreamWriter(instanceIntfFile);
      Writer instanceImplWriter = new OutputStreamWriter(instanceImplFile);
      Writer structIntfWriter = new OutputStreamWriter(structIntfFile);
      Writer structImplWriter = new OutputStreamWriter(structImplFile);

      // export matching packages
      for(
        Iterator pkgs = packagesToExport.iterator(); 
        pkgs.hasNext();
      ) {
        ModelElement_1_0 currentPackage = (ModelElement_1_0)pkgs.next();
        String currentPackageName = (String)currentPackage.values("qualifiedName").get(0);

        // package files
        SysLog.trace("creating buffered file writer for package interface/implementation");
        pkgIntfFile.reset();
        Writer pkgIntfWriter = new OutputStreamWriter(pkgIntfFile);
        JMIPackageIntfMapper pkgIntfMapper = new JMIPackageIntfMapper(
            pkgIntfWriter, 
            this.model, 
            this.mappingFormat, 
            this.packageSuffix
        );
        pkgCompatibilityFile.reset();
        Writer pkgCompatibilityWriter = new OutputStreamWriter(pkgCompatibilityFile);
        JMIPackageForwardCompatibilityMapper pkgCompatibilityMapper = new JMIPackageForwardCompatibilityMapper(
            pkgCompatibilityWriter, 
            this.model, 
            this.mappingFormat, 
            this.packageSuffix
        );
        pkgImplFile.reset();
        Writer pkgImplWriter = new OutputStreamWriter(pkgImplFile);
        JMIPackageImplMapper pkgImplMapper = new JMIPackageImplMapper(
            pkgImplWriter, 
            this.model, 
            this.mappingFormat, 
            this.packageSuffix
        );
          
        // initialize package
        this.jmiBeginPackage(
            currentPackageName,
            pkgIntfMapper,
            pkgCompatibilityMapper, 
            pkgImplMapper
        );
    
        // process packageContent
        for(
          Iterator i = this.model.getContent().iterator(); 
          i.hasNext();
        ) {  
          ModelElement_1_0 element = (ModelElement_1_0)i.next();
          SysLog.trace("processing package element " + element.path());
    
          // org:omg:model1:Class
          if(
            this.model.isClassType(element) &&
            this.model.isLocal(element, currentPackageName)
          ) {
            SysLog.trace("processing class " + element.path());
            boolean isAbstract = ((Boolean)element.values("isAbstract").get(0)).booleanValue();  
    
            // object marshaller for non-abstract and abstract classes (JMI plugins)
            if(!isAbstract) {
                this.jmiObjectMarshaller(
                    element,
                    pkgIntfMapper,
                    pkgCompatibilityMapper, 
                    pkgImplMapper
                );
            }
    
            // generate create filter operations for the current class in package
            ClassifierDef mClassDef = new ClassDef(
                element,
                this.model
            );
            this.jmiFilterCreator(
                mClassDef,
                pkgIntfMapper,
                pkgCompatibilityMapper, 
                pkgImplMapper
            );
  
            classIntfFile.reset();
            classImplFile.reset();
            filterIntfFile.reset();
            queryFile.reset();
            filterImplFile.reset();
            instanceIntfFile.reset();
            instanceImplFile.reset();
  
            JMIClassIntfMapper classIntfMapper = new JMIClassIntfMapper(
                element, 
                classIntfWriter, 
                this.model, 
                this.mappingFormat, packageSuffix
            );
            JMIClassImplMapper classImplMapper = new JMIClassImplMapper(
                element, 
                classImplWriter, 
                this.model, 
                this.mappingFormat, packageSuffix
            );
            JMIInstanceIntfMapper instanceIntfMapper = new JMIInstanceIntfMapper(
                element, 
                instanceIntfWriter, 
                this.model, 
                this.mappingFormat, packageSuffix
            );
            JMIInstanceImplMapper instanceImplMapper = new JMIInstanceImplMapper(
                element, 
                instanceImplWriter, 
                this.model, 
                this.mappingFormat, packageSuffix
            );
            JMIFilterIntfMapper filterIntfMapper = new JMIFilterIntfMapper(
                filterIntfWriter, 
                this.model, 
                this.mappingFormat, packageSuffix
            );
            JMIQueryMapper queryMapper = new JMIQueryMapper(
                queryWriter, 
                this.model, 
                this.mappingFormat, packageSuffix
            );
            JMIFilterImplMapper filterImplMapper = new JMIFilterImplMapper(
                filterImplWriter, 
                this.model, 
                this.mappingFormat, packageSuffix
            );
            this.jmiBeginClass(
                element,
                classIntfMapper,
                classImplMapper,
                instanceIntfMapper,
                instanceImplMapper
            );
  
            this.jmiBeginFilter(
                mClassDef,
                filterIntfMapper,
                queryMapper, 
                filterImplMapper
            );
  
            // get class features
            for(
              Iterator j = element.values("feature").iterator();
              j.hasNext();
            ) {
              ModelElement_1_0 feature = this.model.getElement(j.next());
  
              SysLog.trace("processing class feature " + feature.path());
  
              if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE)) {
                this.jmiAttribute(
                  element,
                  feature,
                  filterIntfMapper,
                  queryMapper,
                  filterImplMapper,
                  instanceIntfMapper, instanceImplMapper
                );
              }
              else if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)) {
                this.jmiReference(
                  element,
                  feature,
                  filterIntfMapper,
                  queryMapper,
                  filterImplMapper,
                  instanceIntfMapper, instanceImplMapper, null
                );
              }
              else if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION)) {
                this.jmiOperation(
                  element,
                  feature,
                  instanceIntfMapper,
                  instanceImplMapper
                );
              }
            }
  
            this.jmiEndFilter(
              filterIntfMapper,
              queryMapper, filterImplMapper
            );
  
            this.jmiEndClass(
              element,
              classIntfMapper,
              classImplMapper,
              instanceIntfMapper,
              instanceImplMapper
            );
        
            instanceIntfWriter.flush();
            this.addToZip(zip, instanceIntfFile, element, "." + this.fileExtension);
            filterIntfWriter.flush();
            queryWriter.flush();
            filterImplWriter.flush();
            instanceImplWriter.flush();
            
            this.addToZip(zip, filterIntfFile, element, "Filter." + this.fileExtension);
            this.addToZip(
                zip, 
                queryFile, 
                element, 
                (String)element.values("name").get(0),
                "Query." + this.fileExtension,
                true,
                "query"
            );
            
            this.addToZip(zip, filterImplFile, element, "FilterImpl." + this.fileExtension);
            this.addToZip(zip, instanceImplFile, element, "Impl." + this.fileExtension);
            if(!isAbstract) {
              classIntfWriter.flush();
              classImplWriter.flush();
              this.addToZip(zip, classIntfFile, element, "Class." + this.fileExtension);
              this.addToZip(zip, classImplFile, element, "ClassImpl." + this.fileExtension);
            }            
          }
    
          // org:omg:model1:StructureType
          else if(
            this.model.isStructureType(element) &&
            this.model.isLocal(element, currentPackageName)
          ) {
            SysLog.trace("processing structure type " + element.path());
  
            this.jmiStructCreator(
              element,
              pkgIntfMapper,
              pkgCompatibilityMapper, 
              pkgImplMapper
            );
    
            StructDef mStructDef = new StructDef(
                element,
                this.model, 
                true // openmdx1                
            );
            this.jmiFilterCreator(
                mStructDef,
                pkgIntfMapper,
                pkgCompatibilityMapper, 
                pkgImplMapper
            );
  
            // only generate for structs which are content of the modelPackage. 
            // Do not generate for imported model elements
            if(this.model.isLocal(element, currentPackageName)) {
    
              structIntfFile.reset();
              structImplFile.reset();              
              filterIntfFile.reset();
              queryFile.reset();
              filterImplFile.reset();
  
              JMIStructIntfMapper structIntfMapper = new JMIStructIntfMapper(
                  element, 
                  structIntfWriter, 
                  this.model, 
                  this.mappingFormat, packageSuffix
              );
              JMIStructImplMapper structImplMapper = new JMIStructImplMapper(
                  element, 
                  structImplWriter, 
                  this.model, 
                  this.mappingFormat, packageSuffix
              );
              JMIFilterIntfMapper filterIntfMapper = new JMIFilterIntfMapper(
                  filterIntfWriter, 
                  this.model, 
                  this.mappingFormat, packageSuffix
              );
              JMIQueryMapper queryMapper = new JMIQueryMapper(
                  queryWriter, 
                  this.model, 
                  this.mappingFormat, 
                  packageSuffix
              );              
              JMIFilterImplMapper filterImplMapper = new JMIFilterImplMapper(
                  filterImplWriter, 
                  this.model, 
                  this.mappingFormat, packageSuffix
              );
              this.jmiBeginStruct(
                element,
                structIntfMapper,
                structImplMapper
              );
            
              this.jmiBeginFilter(
                mStructDef,
                filterIntfMapper,
                queryMapper, 
                filterImplMapper
              );
  
              // StructureFields
              for(
                Iterator j = element.values("content").iterator();
                j.hasNext();
              ) {
                ModelElement_1_0 feature = this.model.getElement(j.next());
  
                SysLog.trace("processing structure field " + feature.path());
  
                if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_FIELD)) {
                  this.jmiStructureField(
                    element,
                    feature,
                    filterIntfMapper,
                    queryMapper,
                    filterImplMapper,
                    structIntfMapper, structImplMapper
                  );
                }
              }
    
              this.jmiEndFilter(
                filterIntfMapper,
                queryMapper, 
                filterImplMapper
              );
  
              this.jmiEndStruct(
                structIntfMapper,
                structImplMapper
              );
    
              structIntfWriter.flush();
              structImplWriter.flush();
              filterIntfWriter.flush();
              queryWriter.flush();
              filterImplWriter.flush();
  
              this.addToZip(zip, structIntfFile, element, "." + this.fileExtension);
              this.addToZip(zip, structImplFile, element, "Impl." + this.fileExtension);
              this.addToZip(zip, filterIntfFile, element, "Filter." + this.fileExtension);
              this.addToZip(
                  zip, 
                  queryFile, 
                  element, 
                  (String)element.values("name").get(0),
                  "Query." + this.fileExtension,
                  true,
                  "query"
              );
              this.addToZip(zip, filterImplFile, element, "FilterImpl." + this.fileExtension);              
            }
          }
  
          // org:omg:model1:Exception
          else if(
            element.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.EXCEPTION) &&
            this.model.isLocal(element, currentPackageName)
          ) {   
            SysLog.trace("processing exception " + element.path());
  
            // only generate for structs which are content of the modelPackage. 
            // Do not generate for imported model elements
            if(this.model.isLocal(element, currentPackageName)) {  
              exceptionImplFile.reset();
              Writer exceptionImplWriter = new OutputStreamWriter(exceptionImplFile);
              JMIExceptionImplMapper exceptionImplMapper = new JMIExceptionImplMapper(
                  element,
                  exceptionImplWriter,
                  this.model,
                  this.mappingFormat, packageSuffix
              );
              this.jmiException(
                element,
                exceptionImplMapper
              );
              exceptionImplWriter.flush();
              this.addToZip(zip, exceptionImplFile, element, "." + this.fileExtension);
            }            
          }
        }
        
        // flush package
        this.jmiEndPackage(
          currentPackageName,
          pkgIntfMapper,
          pkgCompatibilityMapper, 
          pkgImplMapper
        );
    
        pkgIntfWriter.flush();
        pkgCompatibilityWriter.flush();
        pkgImplWriter.flush();
 
        String mofName = (String)currentPackage.values("name").get(0);
        this.addToZip(
            zip, 
            pkgIntfFile, 
            currentPackage, 
            Names.openmdx1PackageName(
                new StringBuffer(),
                mofName
            ).toString(),
            '.' + this.fileExtension
        );
        this.addToZip(
            zip, 
            pkgCompatibilityFile, 
            currentPackage, 
            AbstractNames.openmdx2PackageName(
                new StringBuffer(),
                mofName
            ).toString(),
            '.' + this.fileExtension,
            true,
            "jmi"
        );
        this.addToZip(
            zip, 
            pkgImplFile, 
            currentPackage, 
            Names.openmdx1PackageName(
                new StringBuffer(),
                mofName
            ).toString(),
            "Impl." + this.fileExtension
        );
      }
    } 
    catch(Exception ex) {
      throw new ServiceException(ex).log();
    }
    
    SysLog.trace("done");
  }
  
  //--------------------------------------------------------------------------------
  private final String fileExtension;  
  private final String mappingFormat;
  private List processedAttributes = null;
    
}

//--- End of File -----------------------------------------------------------

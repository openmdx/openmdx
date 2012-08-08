/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMIMapper_1.java,v 1.10 2008/04/02 17:38:40 wfro Exp $
 * Description: XMIExternalizer_1
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/02 17:38:40 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.model1.mapping.xmi;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.ZipOutputStream;

import org.omg.model1.code.VisibilityKind;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.mapping.AbstractMapper_1;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class XMIMapper_1
  extends AbstractMapper_1 {

  //---------------------------------------------------------------------------
  public XMIMapper_1(
  ) {
    super(
      XMINames.XMI_PACKAGE_SUFFIX
    );
  }
  
  //---------------------------------------------------------------------------
  public void externalize(
    String qualifiedPackageName,
    Model_1_3 model, 
    ZipOutputStream zip
  ) throws ServiceException {
    
    SysLog.trace("> externalize");
    
    this.model = model;
    List packagesToExport = this.getMatchingPackages(qualifiedPackageName);

    // export all matching packages
    for(
      Iterator pkgs = packagesToExport.iterator();
      pkgs.hasNext();
    ) {
      ModelElement_1_0 currentPackage = (ModelElement_1_0)pkgs.next();
      String currentPackageName = (String)currentPackage.values("qualifiedName").get(0);

      // model as XML with all model features
      ByteArrayOutputStream bs;
      PrintWriter pw =  new PrintWriter(bs = new ByteArrayOutputStream());
      this.createXMIModel(
        currentPackageName, 
        pw, 
        true
      );
      pw.flush();
      this.addToZip(
        zip, 
        bs, 
        this.model.getElement(currentPackageName), 
        ".xml"
      );
      
      // model as XML with edit features
      pw =  new PrintWriter(bs = new ByteArrayOutputStream());
      this.createXMIModel(
        currentPackageName, 
        pw, 
        false
      );
      pw.flush();
      this.addToZip(
        zip, 
        bs, 
        this.model.getElement(currentPackageName), 
        "_edit.xml"
      );
  
      // model as XSD with all features
      pw =  new PrintWriter(bs = new ByteArrayOutputStream());
      this.createXMISchema(
        currentPackageName, 
        pw, 
        true
      );
      pw.flush();
      this.addToZip(
        zip, 
        bs, 
        this.model.getElement(currentPackageName), 
        ".xsd"
      );
  
      // model as XSD with edit features
      pw =  new PrintWriter(bs = new ByteArrayOutputStream());
      this.createXMISchema(
        currentPackageName, 
        pw, 
        false
      );
      pw.flush();
      this.addToZip(
        zip, 
        bs, 
        this.model.getElement(currentPackageName), 
        "_edit.xsd"
      );
    }

  }

  //---------------------------------------------------------------------------
  private void createXMISchema(
    String packageName,
    PrintWriter outputStream,
    boolean allFeatures
  ) throws ServiceException {

    Map allCompositeAssociationEnds = new HashMap();

    for(
      Iterator i = this.model.getContent().iterator();
      i.hasNext();
    ) {
      DataproviderObject modelElement = (DataproviderObject)i.next();
        
      // Collect AssociationEnds of with containment='composite' for performance reasons.
      // The collection is used to determine the qualifiers of classes.
      if(modelElement.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ASSOCIATION_END)) {
        if(AggregationKind.COMPOSITE.equals(modelElement.values("aggregation").get(0))) {
          allCompositeAssociationEnds.put(
            modelElement.values("type").get(0), 
            modelElement
          );        
        }
      }
    }

    XMISchemaMapper XMISchemaWriter = new XMISchemaMapper(
      outputStream,
      this.model
    );
    XMISchemaWriter.writeSchemaHeader(true);

    // all model elements
    for(
      Iterator iterator = this.model.getContent().iterator();
      iterator.hasNext();
    ) {
      ModelElement_1_0 elementDef = (ModelElement_1_0)iterator.next();
      SysLog.trace("elementDef=" + elementDef.path());
      
      // PrimitiveType
      if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PRIMITIVE_TYPE)) {
        XMISchemaWriter.writePrimitiveType(elementDef);
      }

      // Class
      else if(
        elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.CLASS) ||
        elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_TYPE)
      ) {

        XMISchemaWriter.writeComplexTypeHeader(elementDef);
        
        int compositeReferenceCount = 0;
        /**
         * Write all class features
         * phase 0: attributes and references stored as attributes
         * phase 1: composite references
         */
        for(
          int featureType = 0;
          featureType < 2;
          featureType++
        ) {

          if(featureType == 0) {
            XMISchemaWriter.writeAttributeFeatureHeader(elementDef);
          }
            
          // iterate features
          for(
            Iterator i = elementDef.values("feature").iterator();
            i.hasNext();
          ) {
            ModelElement_1_0 feature = this.model.getElement(i.next());
            SysLog.trace("processing feature", "" + feature.path());
            boolean isPublic = VisibilityKind.PUBLIC_VIS.equals(feature.values("visibility").get(0));
  
            // Attribute
            if(this.model.isAttributeType(feature)) {  
              boolean isChangeable = ((Boolean)feature.values("isChangeable").get(0)).booleanValue();
              if(
                isPublic && 
                (isChangeable || allFeatures) 
              ) {
                if(featureType == 0) {
                  XMISchemaWriter.writeAttribute(
                    feature,
                    this.model.getDereferencedType(
                      feature.values("type").get(0)
                    ).values(
                      SystemAttributes.OBJECT_CLASS
                    ).contains(ModelAttributes.CLASS)
                  );
                }
              }
            }
  
            // Reference
            else if(this.model.isReferenceType(feature)) {  
              ModelElement_1_0 referencedEnd = this.model.getElement(feature.values("referencedEnd").get(0));
              boolean isNavigable = ((Boolean)referencedEnd.values("isNavigable").get(0)).booleanValue();
              boolean isChangeable = 
                  ((Boolean)feature.values("isChangeable").get(0)).booleanValue() ||
                  //
                  // CR0004024
                  //
                  "org:openmdx:base:AuthorityHasProvider:provider".equals(referencedEnd.path().getBase());
              if(isPublic && isNavigable && (isChangeable || allFeatures)) {
                if(this.model.referenceIsStoredAsAttribute(feature)) {
                  if(featureType == 0) {
                    XMISchemaWriter.writeReferenceStoredAsAttribute(
                      feature
                    );
                  }
                }
                else if(!AggregationKind.NONE.equals(referencedEnd.values("aggregation").get(0))) {
                  if(featureType == 1) {
                    if(compositeReferenceCount++ == 0) {
                      XMISchemaWriter.writeCompositeReferenceFeatureHeader(elementDef);
                    }
                    XMISchemaWriter.writeReference(
                      feature,
                      elementDef
                    );
                  }
                }
              }
            }
          }
          if(featureType == 0) {
            XMISchemaWriter.writeAttributeFeatureFooter(elementDef);
          }
        }
                
        XMISchemaWriter.writeCompositeReferenceFeatureFooter(elementDef, compositeReferenceCount);

        // Qualifiers        
        // Either the elementDef (which is a class) itself or one of its supertypes must 
        // have a composite AssocationEnd with a qualifier.
        for(
          Iterator i = elementDef.values("allSupertype").iterator();
          i.hasNext();
        ) {
          ModelElement_1_0 supertype = this.model.getDereferencedType(i.next());
          ModelElement_1_0 modelAssociationEnd = null;
          if((modelAssociationEnd = (ModelElement_1_0)allCompositeAssociationEnds.get(supertype.path())) != null) {
            for(
              int j = 0; 
              j < modelAssociationEnd.values("qualifierName").size(); 
              j++
            ) {
              String qualifierName = (String)modelAssociationEnd.values("qualifierName").get(j);
              ModelElement_1_0 qualifierType = this.model.getDereferencedType(modelAssociationEnd.values("qualifierType").get(j));
              XMISchemaWriter.writeQualifierAttributes(
                qualifierName, 
                (String)qualifierType.values("qualifiedName").get(0),
                this.model.isPrimitiveType(qualifierType)
              );    
            }
          }
        }
        XMISchemaWriter.writeComplexTypeFooter(elementDef);
      }

      // StructureType
      else if(
        elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_TYPE)
      ) {

        XMISchemaWriter.writeComplexTypeHeader(elementDef);
        XMISchemaWriter.writeStructureFieldHeader(elementDef);
        
        // all class features
        for(
          Iterator i = elementDef.values("feature").iterator();
          i.hasNext();
        ) {
          ModelElement_1_0 feature = this.model.getElement(i.next());

          // StructureField
          if(feature.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_FIELD)) {
            XMISchemaWriter.writeStructureField(
              feature,
              this.model.getDereferencedType(
                feature.values("type").get(0)
              ).values(
                SystemAttributes.OBJECT_CLASS
              ).contains(ModelAttributes.CLASS)
            );
          }
        }
        XMISchemaWriter.writeStructureFieldFooter(elementDef);
        XMISchemaWriter.writeComplexTypeFooter(elementDef);
      }
    }    
    XMISchemaWriter.writeSchemaFooter();
  }

  //---------------------------------------------------------------------------
  private static CharSequence getPathPrefix(
      String forPackage
  ){
      if(XMINames.XMI_PACKAGE_SUFFIX.length() == 4) {
          return "xri://+resource/";          
      } else {
          StringTokenizer tokenizer = new StringTokenizer(forPackage, ":");
          StringBuffer pathPrefix = new StringBuffer("../");
          for(int i = 0; i < tokenizer.countTokens()-1; i++) {
              pathPrefix.append("../");
          }
          return pathPrefix;
      }
  }
  
  //---------------------------------------------------------------------------
  private void createXMIModel(
    String forPackage,
    PrintWriter outputStream,
    boolean allFeatures
  ) throws ServiceException {

    SysLog.trace("> createXMIModel");
    XMIModelMapper XMIModelWriter = new XMIModelMapper(
      outputStream,
      allFeatures
    );

    XMIModelWriter.writeModelHeader(
      this.model.getElement(forPackage).path().get(2), 
      this.model.getElement(forPackage).path().get(4), 
      getPathPrefix(forPackage) + "org/omg/model1/" + XMINames.XMI_PACKAGE_SUFFIX + "/model1" + 
      (allFeatures ? ".xsd" : "_edit.xsd")
    );
    
    for(
      Iterator iterator = this.model.getContent().iterator();
      iterator.hasNext();
    ) {
      ModelElement_1_0 elementDef = (ModelElement_1_0)iterator.next();
      SysLog.trace("modelElement=" + elementDef.path());
      
      // only write model elements contained in the defining model (segment name)
      // @see CR0001066 
      if(forPackage.startsWith(elementDef.path().get(4))) {
        if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.CLASS)) {
          XMIModelWriter.writeClass(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE)) {
          XMIModelWriter.writeAttribute(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ASSOCIATION)) {
          XMIModelWriter.writeAssociation(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ASSOCIATION_END)) {
          XMIModelWriter.writeAssociationEnd(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.REFERENCE)) {
          XMIModelWriter.writeReference(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION)) {
          XMIModelWriter.writeOperation(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.EXCEPTION)) {
          XMIModelWriter.writeException(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PARAMETER)) {
          XMIModelWriter.writeParameter(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PACKAGE)) {
          XMIModelWriter.writePackage(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PRIMITIVE_TYPE)) {
          XMIModelWriter.writePrimitiveType(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_TYPE)) {
          XMIModelWriter.writeStructureType(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_FIELD)) {
          XMIModelWriter.writeStructureField(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ALIAS_TYPE)) {
          XMIModelWriter.writeAliasType(elementDef);
        }
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.CONSTRAINT)) {
          // not exported in this version
        }
        else {
          throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.ASSERTION_FAILURE, 
            new BasicException.Parameter[]{
              new BasicException.Parameter("element", elementDef)
            },
            "can not export model element. Unsupported type"
          );
        }
      }
    }    
    XMIModelWriter.writeModelFooter();
  }

  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------
  static final String ROLE_TYPE = "ch:omex:generic:Role";
  
}

//---------------------------------------------------------------------------

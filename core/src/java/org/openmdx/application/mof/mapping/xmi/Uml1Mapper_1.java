/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: XMIExternalizer_1
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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

package org.openmdx.application.mof.mapping.xmi;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.openmdx.application.mof.mapping.spi.AbstractMapper_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;

//---------------------------------------------------------------------------
@SuppressWarnings({"rawtypes","unchecked"})
public class Uml1Mapper_1
  extends AbstractMapper_1 {

  //---------------------------------------------------------------------------
  public Uml1Mapper_1(
  ) {
    super("xmi");
  }
  
  //---------------------------------------------------------------------------
  public void externalize(
    String qualifiedPackageName,
    Model_1_0 model, 
    ZipOutputStream zip
  ) throws ServiceException {
    
    this.model = model;

    long start = System.currentTimeMillis();

    Set topLevelPackageNames = this.determinePackageContainment();
    
    ByteArrayOutputStream bs = new ByteArrayOutputStream(1024*1024);
    PrintWriter pw =  new PrintWriter(bs);

    // create XMI stream (Poseidon for UML specific)
    this.writeUMLModel(
      topLevelPackageNames, 
      pw,
      Uml1ModelMapper.POSEIDON_XMI_DIALECT
    );
    pw.flush();

    this.addToZip(
      zip, 
      bs, 
      null, 
      "poseidon.xmi"
    );

    // create XMI stream (MagicDraw specific)
    bs.reset();
    this.writeUMLModel(
      topLevelPackageNames, 
      pw,
      Uml1ModelMapper.MAGICDRAW_XMI_DIALECT
    );
    pw.flush();

    this.addToZip(
      zip, 
      bs, 
      null, 
      "magicdraw.xmi"
    );
    
    long end = System.currentTimeMillis();
    SysLog.detail("time used to externalize XMI 1.2 (based on UML 1.4 metamodel) for MagicDraw AND Poseidon: " + (end-start) + " ms");

  }

  //---------------------------------------------------------------------------
  private Set determinePackageContainment(
  ) throws ServiceException {
    
    // Note:
    // The XMI/UML mapping requires that all packages are traversed in their
    // containment order. Since our MOF packages are all toplevel model
    // elements (i.e. not nested), this package hierarchy must be reconstructed
    // based on the qualified package names.

    // set of all toplevel package names, e.g. ch, org, net
    Set topLevelPackageNames = new HashSet();
    
    // maps qualified package names to the designated package model element
    packages = new HashMap();

    // lists the qualified name of all subpackages for a given package
    subpackages = new HashMap();
    
    for(
      Iterator iterator = this.model.getContent().iterator();
      iterator.hasNext();
    ) {
      ModelElement_1_0 elementDef = (ModelElement_1_0)iterator.next();

      if(elementDef.isPackageType())
      {
        String qualifiedName = elementDef.getQualifiedName().substring(
          0,
          elementDef.getQualifiedName().lastIndexOf(":")
        );
        packages.put(
          qualifiedName,
          elementDef
        );
        
        // check whether this package is a toplevel package
        if (qualifiedName.indexOf(":") == -1)
        {
          // found a toplevel package
          topLevelPackageNames.add(qualifiedName);
        }
        else
        {
          // found no toplevel package, get parent package for this package
          String parentPackageName = qualifiedName.substring(0, qualifiedName.lastIndexOf(":"));

          // add new subpackage for this parent package
          if (!subpackages.containsKey(parentPackageName))
          {
            subpackages.put(
              parentPackageName,
              new HashSet()
            );
          }
          ((Set)subpackages.get(parentPackageName)).add(qualifiedName);
        }
      }
    }
    return topLevelPackageNames;
  }

  //---------------------------------------------------------------------------
  private void writeUMLModel(
    Set topLevelPackageNames,
    PrintWriter outputStream,
    byte xmiDialect
  ) throws ServiceException {

    SysLog.trace("writeUMLModel");
    
    Uml1ModelMapper writer = new Uml1ModelMapper(
      outputStream,
      xmiDialect
    );

    // write XMI and UML:Model header
    writer.writeHeader();

    for(
      Iterator it = topLevelPackageNames.iterator();
      it.hasNext();
    ) {
      this.writePackage(writer, (String)it.next());
    }

    // write XMI and UML:Model footer
    writer.writeFooter();
  }

  //---------------------------------------------------------------------------
  private void writePackage(
    Uml1ModelMapper writer,
    String qualifiedPackageName
  ) throws ServiceException {

    SysLog.trace("writePackage ", qualifiedPackageName);

    // Note:
    // Packages are not nested, i.e. a package does NOT belong to the content
    // of its parent package. All packages are toplevel model elements.

    ModelElement_1_0 packageDef = (ModelElement_1_0)this.packages.get(qualifiedPackageName); 

    // write package header
    writer.writePackageHeader(packageDef);

    // write all model elements in this package
    for(
      Iterator iterator = packageDef.objGetList("content").iterator();
      iterator.hasNext();
    ) {
      this.writeModelElement(
        writer, 
        this.model.getElement(iterator.next())
      );
    }

    // write all subpackages within this package
    if (this.subpackages.containsKey(qualifiedPackageName))
    {
      for(
        Iterator it = ((Set)this.subpackages.get(qualifiedPackageName)).iterator();
        it.hasNext();
      ) {
        this.writePackage(writer, (String)it.next());
      }
    }

    // write package footer
    writer.writePackageFooter(packageDef);
  }

  //---------------------------------------------------------------------------
  private void writeModelElement(
    Uml1ModelMapper writer,
    ModelElement_1_0 elementDef
  ) throws ServiceException {
      
//    // only write model elements contained in the defining model (segment name)
//    // @see CR0001066 
//    if(packageName.startsWith(elementDef.path().get(4))) {
    if(elementDef.isPrimitiveType()) {
      writer.writePrimitiveType(elementDef);
    }
    else if(elementDef.isAliasType()) {
      ModelElement_1_0 typeDef = this.model.getElement(elementDef.getType());
      writer.writeAliasType(elementDef, typeDef, typeDef.isPrimitiveType());
    }
    else if(elementDef.isClassType()) {

      // determine the direct subtypes
      List subtypePaths = elementDef.objGetList("subtype");
      subtypePaths.remove(elementDef.jdoGetObjectId());
      List subtypes = new ArrayList();
      for(
        Iterator it = subtypePaths.iterator();
        it.hasNext();
      ) {
        subtypes.add(this.model.getElement(it.next()));
      }
      
      // write generalizations for all direct subtypes
      if (!subtypes.isEmpty())
      {
        writer.writeGeneralization(elementDef, subtypes);
      }

      // determine the direct supertypes
      List supertypePaths = elementDef.objGetList("supertype");
      List supertypes = new ArrayList();
      for(
        Iterator it = supertypePaths.iterator();
        it.hasNext();
      ) {
        supertypes.add(this.model.getElement(it.next()));
      }

      // check whether this class has structural features
      boolean hasStructuralFeatures = this.hasStructuralFeatures(elementDef.objGetList("content"));

      // write class header with direct supertypes
      writer.writeClassHeader(
        elementDef, 
        supertypes,
        hasStructuralFeatures
      );
      
      // write all features
      for(
        Iterator iterator = elementDef.objGetList("content").iterator();
        iterator.hasNext();
      ) {
        this.writeModelElement(
          writer, 
          this.model.getElement(iterator.next())
        );
      }

      writer.writeClassFooter(
        elementDef,
        hasStructuralFeatures
      );
    }
    else if(elementDef.isAttributeType()) {
      ModelElement_1_0 typeDef = this.model.getElement(elementDef.getType());
      boolean refTypeIsPrimitive = typeDef.isPrimitiveType();
      boolean isDerived = elementDef.isDerived().booleanValue();
      boolean isChangeable = elementDef.isChangeable().booleanValue();
      writer.writeAttribute(elementDef, isDerived, isChangeable, typeDef, refTypeIsPrimitive);
    }
    else if(elementDef.isAssociationType()) {
      writer.writeAssociationHeader(elementDef);
      ModelElement_1_0 associationEnd0Def = this.model.getElement(
        elementDef.objGetList("content").get(0)
      );
      ModelElement_1_0 associationEnd1Def = this.model.getElement(
        elementDef.objGetList("content").get(1)
      );
      
      ModelElement_1_0 associationEnd0TypeDef = this.model.getElement(
        associationEnd0Def.getType()
      );
      ModelElement_1_0 associationEnd1TypeDef = this.model.getElement(
        associationEnd1Def.getType()
      );
      
      List associationEnd0QualifierTypes = new ArrayList();
      for(
        Iterator it = associationEnd0Def.objGetList("qualifierType").iterator();
        it.hasNext();
      ) {
        associationEnd0QualifierTypes.add(
          this.model.getElement(it.next())
        );
      }
      List associationEnd1QualifierTypes = new ArrayList();
      for(
        Iterator it = associationEnd1Def.objGetList("qualifierType").iterator();
        it.hasNext();
      ) {
        associationEnd1QualifierTypes.add(
          this.model.getElement(it.next())
        );
      }      
      // write both association ends with their qualifiers
      writer.writeAssociationEnds(
        associationEnd0Def,
        associationEnd0TypeDef, 
        associationEnd0QualifierTypes,
        associationEnd1Def,
        associationEnd1TypeDef,
        associationEnd1QualifierTypes
      );      
      writer.writeAssociationFooter(elementDef);
    }
    else if(elementDef.isOperationType()) {
      List exceptions = new ArrayList();
      for(
        Iterator it = elementDef.objGetList("exception").iterator();
        it.hasNext();
      ) {
        exceptions.add(
          this.model.getElement(it.next())
        );
      }
      writer.writeOperationHeader(elementDef, exceptions);
      
      // write parameters
      for(
        Iterator iterator = elementDef.objGetList("content").iterator();
        iterator.hasNext();
      ) {
        this.writeModelElement(
          writer, 
          this.model.getElement(iterator.next())
        );
      }
      
      writer.writeOperationFooter(elementDef);
    }
    else if(elementDef.isExceptionType()) {
      writer.writeExceptionHeader(elementDef);
      for(
        Iterator iterator = elementDef.objGetList("content").iterator();
        iterator.hasNext();
      ) {
        this.writeModelElement(
          writer, 
          this.model.getElement(iterator.next())
        );
      }
      writer.writeExceptionFooter(elementDef);
    }
    else if(elementDef.isParameterType()) {
      ModelElement_1_0 typeDef = this.model.getElement(elementDef.getType());
      writer.writeParameter(elementDef, typeDef, typeDef.isPrimitiveType());
    }
    else if(elementDef.isStructureType()) {
      
      // according to MOF every structure type must have at least one structure
      // field; however in openMDX 'Void' has no structure field, therefore
      // to avoid problems with Poseidon XMI import (CR0002222), we must check
      // for the existence of structure fields
      boolean hasStructureFields = this.hasStructureFields(elementDef.objGetList("content"));
      
      writer.writeStructureTypeHeader(elementDef, hasStructureFields);
      
      // write structure fields
      for(
        Iterator iterator = elementDef.objGetList("content").iterator();
        iterator.hasNext();
      ) {
        this.writeModelElement(
          writer, 
          this.model.getElement(iterator.next())
        );
      }
      
      writer.writeStructureTypeFooter(elementDef, hasStructureFields);
    }
    else if(elementDef.isStructureFieldType()) {
      ModelElement_1_0 typeDef = this.model.getElement(elementDef.getType());
      writer.writeStructureField(elementDef, typeDef, typeDef.isPrimitiveType());
    }
  }
  
  //---------------------------------------------------------------------------
  private boolean hasStructureFields(
    List<Object> content
  ) throws ServiceException {
    if (!content.isEmpty())
    {
      for(
        Iterator iterator = content.iterator();
        iterator.hasNext();
      ) {
        ModelElement_1_0 elementDef = this.model.getElement(iterator.next());
        if(elementDef.isStructureFieldType()) {
          return true;
        }
      }
    }
    return false;
  }
  
  //---------------------------------------------------------------------------
  private boolean hasStructuralFeatures(
    List<Object> content
  ) throws ServiceException {
    if (!content.isEmpty())
    {
      for(
        Iterator iterator = content.iterator();
        iterator.hasNext();
      ) {
        ModelElement_1_0 elementDef = this.model.getElement(iterator.next());
        if(elementDef.isAttributeType()) {
          return true;
        }
        else if(elementDef.isOperationType()) {
          return true;
        }
        // in openMDX exceptions are modeled as operations and are therefore 
        // structural features as well in the broadest sense
        else if(elementDef.isExceptionType()) {
          return true;
        }
      }
    }
    return false;
  }
  
  //---------------------------------------------------------------------------
  // Variables
  //---------------------------------------------------------------------------
  private Map subpackages = null;
  private Map packages = null;
}

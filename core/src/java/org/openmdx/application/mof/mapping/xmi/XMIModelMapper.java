/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMIModelMapper.java,v 1.9 2010/01/03 15:02:03 wfro Exp $
 * Description: write model as org.omg.model1.xsd compliant XML file
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/03 15:02:03 $
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

/**
 * @author wfro
 */
package org.openmdx.application.mof.mapping.xmi;

import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.w3c.format.DateTimeFormat;

@SuppressWarnings("unchecked")
public class XMIModelMapper {

  //---------------------------------------------------------------------------
  public XMIModelMapper(
    PrintWriter os, 
    boolean allFeatures
  ) {
    this.pw = new PrintWriter(os);
    this.allFeatures = allFeatures;
  }

  //---------------------------------------------------------------------------
  String xmlEncodeValue(
    String value
  ) {
    StringBuffer encodedString = new StringBuffer(value);
    for(
      int i = value.length()-1; 
      i >= 0;
      i--
    ) {
      switch(value.charAt(i)) {
        case '<':
          encodedString.replace(i, i+1, "&lt;");
          break;
        case '>':
          encodedString.replace(i, i+1, "&gt;");
          break;
        case '&':
          encodedString.replace(i, i+1, "&amp;");
          break;
        case '"':
          encodedString.replace(i, i+1, "&quot;");
          break;
      }
    }
    return encodedString.toString();
  }
    
  //---------------------------------------------------------------------------
    /**
     * In case of a multi-valued element write it in the form 
     * <elementName>{<_item>value</_item>}</elementName>.
     */
  void writeElement(
    String elementName,
    List elementValues
  ) {
    if(elementValues.size() == 0) {
        return;
    }
    this.pw.println(spaces(44) + "<" + elementName +  ">");
    for(
      Iterator i = elementValues.iterator();
      i.hasNext();
    ) {
      java.lang.Object elementValue = i.next();
      if(elementValue instanceof Path) {
        this.pw.println(spaces(48) + "<_item>" + ((Path)elementValue).toXRI() + "</_item>");
      }
      else {
        this.pw.println(spaces(48) + "<_item>" + elementValue.toString() + "</_item>");
      }
    }    
    this.pw.println(spaces(44) + "</" + elementName +  ">");
  } 

  //---------------------------------------------------------------------------
    /**
     * Otherwise write it in the form <elementName>value</elementName>
     */
  void writeElement(
    String elementName,
    java.lang.Object elementValue
  ) {
    this.writeElement(
      44,
      elementName,
      elementValue
    );
  } 

  //---------------------------------------------------------------------------
  void writeElement(
    int pos,
    String elementName,
    java.lang.Object elementValue
  ) {
    if(elementValue != null) {    
      if(elementValue instanceof Path) {
        this.pw.println(spaces(pos) + "<" + elementName +  ">" + ((Path)elementValue).toXRI() + "</" + elementName + ">");
      }
      else {
        this.pw.println(spaces(pos) + "<" + elementName +  ">" + elementValue.toString() + "</" + elementName + ">");
      }
    }
  } 

  //---------------------------------------------------------------------------
  /**
   * Translate a string of the form 20020406T082623.930Z to a string of the
   * form 2002-04-06T08:26:23Z.
   */
  void writeElementAsDateTime(
    String elementName,
    Date elementValue
  ) {
    this.writeElementAsDateTime(
      44,
      elementName,
      elementValue
    );
  }  

  //---------------------------------------------------------------------------
  void writeElementAsDateTime(
    int pos,
    String elementName,
    Date elementValue
  ) {
    this.pw.println(
      spaces(pos) + "<" + elementName +  ">" + 
      DateTimeFormat.EXTENDED_UTC_FORMAT.format(elementValue) +
      "</" + elementName + ">"
    );
  }  

  //---------------------------------------------------------------------------
  void writeElementEncoded(
    String elementName,
    List elementValues
  ) {
    for(
      Iterator i = elementValues.iterator();
      i.hasNext();
    ) {
     this.pw.println(spaces(44) + "<" + elementName +  ">" + xmlEncodeValue((String)i.next()) + "</" + elementName + ">");
    }
  }  

  //---------------------------------------------------------------------------
  public void writePackage(
    ModelElement_1_0 packageDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.Package qualifiedName=\"" + packageDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", packageDef.jdoGetObjectId().toString());
    }
    writeElement("container", packageDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", packageDef.objGetValue("name"));
      writeElement("qualifiedName", packageDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", packageDef.objGetList("stereotype"));
    writeElement("isAbstract", packageDef.objGetValue("isAbstract"));
    if(this.allFeatures) {
      writeElement("content", packageDef.objGetList("content"));
      writeElement("allSupertype", packageDef.objGetList("allSupertype"));
      writeElement("subtype", packageDef.objGetList("allSubtype"));
    }
    writeElement("supertype", packageDef.objGetList("supertype"));
    writeElement("visibility", packageDef.objGetValue("visibility"));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Package>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writePrimitiveType(
    ModelElement_1_0 primitiveTypeDef
  ) throws ServiceException {
  
    this.pw.println(spaces(36) + "<org.omg.model1.PrimitiveType qualifiedName=\"" + primitiveTypeDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", primitiveTypeDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)primitiveTypeDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)primitiveTypeDef.objGetValue("modifiedAt"));
    }
    writeElement("container", primitiveTypeDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", primitiveTypeDef.objGetValue("name"));
      writeElement("qualifiedName", primitiveTypeDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", primitiveTypeDef.objGetList("stereotype"));
    writeElement("isAbstract", primitiveTypeDef.objGetValue("isAbstract"));
    if(this.allFeatures) {
      writeElement("allSupertype", primitiveTypeDef.objGetList("allSupertype"));
      writeElement("subtype", primitiveTypeDef.objGetList("allSubtype"));
    }
    writeElement("supertype", primitiveTypeDef.objGetList("supertype"));
    writeElement("visibility", primitiveTypeDef.objGetValue("visibility"));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.PrimitiveType>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writeAttribute(
    ModelElement_1_0 attributeDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.Attribute qualifiedName=\"" + attributeDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");
  
    if(this.allFeatures) {
      writeElement("identity", attributeDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)attributeDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)attributeDef.objGetValue("modifiedAt"));
    }
    writeElement("isDerived", attributeDef.objGetValue("isDerived"));
    writeElement("maxLength", attributeDef.objGetValue("maxLength"));
    writeElement("container", attributeDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", attributeDef.objGetValue("name"));
      writeElement("qualifiedName", attributeDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", attributeDef.objGetList("stereotype"));
    writeElement("scope", attributeDef.objGetValue("scope"));
    writeElement("visibility", attributeDef.objGetValue("visibility"));
    writeElement("isChangeable", attributeDef.objGetValue("isChangeable"));
    writeElement("multiplicity", attributeDef.objGetValue("multiplicity"));
    writeElement("type", attributeDef.objGetValue("type"));
  
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Attribute>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeStructureField(
    ModelElement_1_0 structureFieldDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.StructureField qualifiedName=\"" + structureFieldDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");
  
    if(this.allFeatures) {
      writeElement("identity", structureFieldDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)structureFieldDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)structureFieldDef.objGetValue("modifiedAt"));
    }
    writeElement("maxLength", structureFieldDef.objGetValue("maxLength"));
    writeElement("multiplicity", structureFieldDef.objGetValue("multiplicity"));
    writeElement("container", structureFieldDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", structureFieldDef.objGetValue("name"));
      writeElement("qualifiedName", structureFieldDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", structureFieldDef.objGetList("stereotype"));
    writeElement("type", structureFieldDef.objGetValue("type"));
  
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.StructureField>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeOperation(
    ModelElement_1_0 operationDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.Operation qualifiedName=\"" + operationDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", operationDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)operationDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)operationDef.objGetValue("modifiedAt"));
    }
    if(this.allFeatures) {
      writeElement("parameter", operationDef.objGetList("parameter"));
    }
    writeElement("container", operationDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", operationDef.objGetValue("name"));
      writeElement("qualifiedName", operationDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", operationDef.objGetList("stereotype"));
    writeElement("scope", operationDef.objGetValue("scope"));
    writeElement("visibility", operationDef.objGetValue("visibility"));
    writeElement("exception", operationDef.objGetList("exception"));
    writeElementEncoded("semantics", operationDef.objGetList("semantics"));
    writeElement("isQuery", operationDef.objGetValue("isQuery"));
    if(this.allFeatures) {
      writeElement("content", operationDef.objGetList("content"));
    }

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Operation>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writeException(
    ModelElement_1_0 exceptionDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.Exception qualifiedName=\"" + exceptionDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", exceptionDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)exceptionDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)exceptionDef.objGetValue("modifiedAt"));
    }
    if(this.allFeatures) {
      writeElement("parameter", exceptionDef.objGetList("parameter"));
    }
    writeElement("container", exceptionDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", exceptionDef.objGetValue("name"));
      writeElement("qualifiedName", exceptionDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", exceptionDef.objGetList("stereotype"));
    writeElement("scope", exceptionDef.objGetValue("scope"));
    writeElement("visibility", exceptionDef.objGetValue("visibility"));
    if(this.allFeatures) {
      writeElement("content", exceptionDef.objGetList("content"));
    }

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Exception>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writeParameter(
    ModelElement_1_0 parameterDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.Parameter qualifiedName=\"" + parameterDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", parameterDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)parameterDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)parameterDef.objGetValue("modifiedAt"));
    }
    writeElement("container", parameterDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", parameterDef.objGetValue("name"));
      writeElement("qualifiedName", parameterDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", parameterDef.objGetList("stereotype"));
    writeElement("direction", parameterDef.objGetValue("direction"));
    writeElement("multiplicity", parameterDef.objGetValue("multiplicity"));
    writeElement("type", parameterDef.objGetValue("type"));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Parameter>");  
    this.pw.flush();

  }
  
  //---------------------------------------------------------------------------
  public void writeAssociation(
    ModelElement_1_0 associationDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.Association qualifiedName=\"" + associationDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", associationDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)associationDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)associationDef.objGetValue("modifiedAt"));
    }
    writeElement("container", associationDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", associationDef.objGetValue("name"));
      writeElement("qualifiedName", associationDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", associationDef.objGetList("stereotype"));
    writeElement("isAbstract", associationDef.objGetValue("isAbstract"));
    if(this.allFeatures) {
      writeElement("allSupertype", associationDef.objGetList("allSupertype"));
      writeElement("subtype", associationDef.objGetList("allSubtype"));
      writeElement("content", associationDef.objGetList("content"));
    }
    writeElement("supertype", associationDef.objGetList("supertype"));
    writeElement("visibility", associationDef.objGetValue("visibility"));  
    writeElement("isDerived", associationDef.objGetValue("isDerived"));  

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Association>");
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeAssociationEnd(
    ModelElement_1_0 associationEndDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.AssociationEnd qualifiedName=\"" + associationEndDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", associationEndDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)associationEndDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)associationEndDef.objGetValue("modifiedAt"));
    }
    writeElement("aggregation", associationEndDef.objGetValue("aggregation"));
    writeElement("isChangeable", associationEndDef.objGetValue("isChangeable"));
    writeElement("isNavigable", associationEndDef.objGetValue("isNavigable"));
    writeElement("multiplicity", associationEndDef.objGetValue("multiplicity"));
    writeElement("qualifierName", associationEndDef.objGetList("qualifierName"));
    writeElement("qualifierType", associationEndDef.objGetList("qualifierType"));
    writeElement("container", associationEndDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", associationEndDef.objGetValue("name"));
      writeElement("qualifiedName", associationEndDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", associationEndDef.objGetList("stereotype"));
    writeElement("type", associationEndDef.objGetValue("type"));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.AssociationEnd>");  
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeReference(
    ModelElement_1_0 referenceDef
  ) throws ServiceException {

    this.pw.println(spaces(36) + "<org.omg.model1.Reference qualifiedName=\"" + referenceDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", referenceDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)referenceDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)referenceDef.objGetValue("modifiedAt"));
    }
    writeElement("container", referenceDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", referenceDef.objGetValue("name"));
      writeElement("qualifiedName", referenceDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", referenceDef.objGetList("stereotype"));
    writeElement("scope", referenceDef.objGetValue("scope"));
    writeElement("visibility", referenceDef.objGetValue("visibility"));
    writeElement("exposedEnd", referenceDef.objGetValue("exposedEnd"));
    writeElement("referencedEnd", referenceDef.objGetValue("referencedEnd"));
    if(this.allFeatures) {
      writeElement("referencedEndIsNavigable", referenceDef.objGetValue("referencedEndIsNavigable"));
    }
    writeElement("isChangeable", referenceDef.objGetValue("isChangeable"));
    writeElement("multiplicity", referenceDef.objGetValue("multiplicity"));
    writeElement("type", referenceDef.objGetValue("type"));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Reference>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writeClass(
    ModelElement_1_0 classDef
  ) throws ServiceException {
  
    this.pw.println(spaces(36) + "<org.omg.model1.Class qualifiedName=\"" + classDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", classDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)classDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)classDef.objGetValue("modifiedAt"));
    }
    writeElement("isSingleton", classDef.objGetValue("isSingleton"));
    if(this.allFeatures) {
      writeElement("feature", classDef.objGetList("feature"));
      writeElement("content", classDef.objGetList("content"));
    }
    writeElement("container", classDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", classDef.objGetValue("name"));
      writeElement("qualifiedName", classDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", classDef.objGetList("stereotype"));
    writeElement("isAbstract", classDef.objGetValue("isAbstract"));
    if(this.allFeatures) {
      writeElement("allSupertype", classDef.objGetList("allSupertype"));
      writeElement("subtype", classDef.objGetList("allSubtype"));
    }
    writeElement("supertype", classDef.objGetList("supertype"));
    writeElement("visibility", classDef.objGetValue("visibility"));  
    if(this.allFeatures) {
      writeElement("compositeReference", classDef.objGetValue("compositeReference"));  
    }
   
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Class>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeStructureType(
    ModelElement_1_0 structDef
  ) throws ServiceException {
  
    this.pw.println(spaces(36) + "<org.omg.model1.StructureType qualifiedName=\"" + structDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", structDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)structDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)structDef.objGetValue("modifiedAt"));
    }
    if(this.allFeatures) {
      writeElement("feature", structDef.objGetList("feature"));
      writeElement("content", structDef.objGetList("content"));
    }
    writeElement("container", structDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", structDef.objGetValue("name"));
      writeElement("qualifiedName", structDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", structDef.objGetList("stereotype"));
    writeElement("isAbstract", structDef.objGetValue("isAbstract"));
    if(this.allFeatures) {
      writeElement("allSupertype", structDef.objGetList("allSupertype"));
      writeElement("subtype", structDef.objGetList("allSubtype"));
    }
    writeElement("supertype", structDef.objGetList("supertype"));
    writeElement("visibility", structDef.objGetValue("visibility"));  
    writeElement("compositeReference", structDef.objGetValue("compositeReference"));  
   
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.StructureType>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeAliasType(
    ModelElement_1_0 aliasTypeDef
  ) throws ServiceException {
  
    this.pw.println(spaces(36) + "<org.omg.model1.AliasType qualifiedName=\"" + aliasTypeDef.objGetValue("qualifiedName") + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", aliasTypeDef.jdoGetObjectId().toString());
      writeElementAsDateTime("createdAt", (Date)aliasTypeDef.objGetValue("createdAt"));
      writeElementAsDateTime("modifiedAt", (Date)aliasTypeDef.objGetValue("modifiedAt"));
    }
    if(this.allFeatures) {
      writeElement("feature", aliasTypeDef.objGetList("feature"));
      writeElement("content", aliasTypeDef.objGetList("content"));
    }
    writeElement("container", aliasTypeDef.objGetValue("container"));
    if(this.allFeatures) {
      writeElement("name", aliasTypeDef.objGetValue("name"));
      writeElement("qualifiedName", aliasTypeDef.objGetValue("qualifiedName"));
    }
    writeElement("stereotype", aliasTypeDef.objGetList("stereotype"));
    writeElement("isAbstract", aliasTypeDef.objGetValue("isAbstract"));
    if(this.allFeatures) {
      writeElement("allSupertype", aliasTypeDef.objGetList("allSupertype"));
      writeElement("subtype", aliasTypeDef.objGetList("allSubtype"));
    }
    writeElement("supertype", aliasTypeDef.objGetList("supertype"));
    writeElement("visibility", aliasTypeDef.objGetValue("visibility"));  
    writeElement("type", aliasTypeDef.objGetValue("type"));
    writeElement("compositeReference", aliasTypeDef.objGetValue("compositeReference"));  
   
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.AliasType>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeModelHeader(
    String providerName,
    String segmentName, 
    String schemaFileName
  ) {
//  String currentDateTime = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
    this.pw.println("<?xml version=" + "\"1.0\"" + " encoding=" + "\"UTF-8\"" + "?>");
    this.pw.println("<!-- Generated by openMDX XMI Exporter -->");
    this.pw.println("<org.openmdx.base.Authority name=\"org:omg:model1\" xmlns:xsi=" + "\"http://www.w3.org/2001/XMLSchema-instance\"" + " xsi:noNamespaceSchemaLocation=\"" + schemaFileName + "\">");
    this.pw.println("    <_object></_object>");
    this.pw.println("    <_content>");
    this.pw.println("        <provider>");
    this.pw.println("            <org.openmdx.base.Provider qualifiedName=\"" + providerName + "\">");
    this.pw.println("                <_object></_object>");
    this.pw.println("                <_content>");
    this.pw.println("                    <segment>");
    this.pw.println("                        <org.omg.model1.Segment qualifiedName=" + "\"" + segmentName + "\">");
    this.pw.println("                            <_object/>");
    this.pw.println("                            <_content>");
    this.pw.println("                                <element>");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeModelFooter(
  ) {
    this.pw.println("                                </element>");
    this.pw.println("                             </_content>");
    this.pw.println("                        </org.omg.model1.Segment>");
    this.pw.println("                    </segment>");
    this.pw.println("                </_content>");
    this.pw.println("            </org.openmdx.base.Provider>");
    this.pw.println("        </provider>");
    this.pw.println("    </_content>");
    this.pw.println("</org.openmdx.base.Authority>");

    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  private String spaces(
    int number
  ) {
    return(SPACES.substring(0, number));
  }

  //---------------------------------------------------------------------------  
  // Variables
  //---------------------------------------------------------------------------  

  PrintWriter pw = null;
  boolean allFeatures = false;
  private static String SPACES = new String("                                                                                                     ");

}

//---------------------------------------------------------------------------

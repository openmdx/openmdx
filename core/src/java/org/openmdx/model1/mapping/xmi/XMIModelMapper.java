/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMIModelMapper.java,v 1.1 2005/12/11 23:52:21 wfro Exp $
 * Description: write model as org.omg.model1.xsd compliant XML file
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/12/11 23:52:21 $
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
package org.openmdx.model1.mapping.xmi;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;

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
        this.pw.println(spaces(48) + "<_item>" + ((Path)elementValue).toUri() + "</_item>");
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
        this.pw.println(spaces(pos) + "<" + elementName +  ">" + ((Path)elementValue).toUri() + "</" + elementName + ">");
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
    String elementValue
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
    String elementValue
  ) {
    this.pw.println(
      spaces(pos) + "<" + elementName +  ">" + 
      elementValue.substring(0,4) + "-" + 
      elementValue.substring(4,6) + "-" + 
      elementValue.substring(6,8) + "T" +
      elementValue.substring(9,11) + ":" + 
      elementValue.substring(11,13) + ":" + 
      elementValue.substring(13,15) + "Z" +
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
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.Package qualifiedName=\"" + packageDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", packageDef.path().toString());
      writeElementAsDateTime("createdAt", (String)packageDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)packageDef.values("modifiedAt").get(0));
    }
    writeElement("container", packageDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", packageDef.values("name").get(0));
      writeElement("qualifiedName", packageDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", packageDef.values("stereotype"));
    writeElement("isAbstract", packageDef.values("isAbstract").get(0));
    if(this.allFeatures) {
      writeElement("content", packageDef.values("content"));
      writeElement("allSupertype", packageDef.values("allSupertype"));
      writeElement("subtype", packageDef.values("allSubtype"));
    }
    writeElement("supertype", packageDef.values("supertype"));
    writeElement("visibility", packageDef.values("visibility").get(0));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Package>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writePrimitiveType(
    ModelElement_1_0 primitiveTypeDef
  ) {
  
    this.pw.println(spaces(36) + "<org.omg.model1.PrimitiveType qualifiedName=\"" + primitiveTypeDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", primitiveTypeDef.path().toString());
      writeElementAsDateTime("createdAt", (String)primitiveTypeDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)primitiveTypeDef.values("modifiedAt").get(0));
    }
    writeElement("container", primitiveTypeDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", primitiveTypeDef.values("name").get(0));
      writeElement("qualifiedName", primitiveTypeDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", primitiveTypeDef.values("stereotype"));
    writeElement("isAbstract", primitiveTypeDef.values("isAbstract").get(0));
    if(this.allFeatures) {
      writeElement("allSupertype", primitiveTypeDef.values("allSupertype"));
      writeElement("subtype", primitiveTypeDef.values("allSubtype"));
    }
    writeElement("supertype", primitiveTypeDef.values("supertype"));
    writeElement("visibility", primitiveTypeDef.values("visibility").get(0));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.PrimitiveType>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writeAttribute(
    ModelElement_1_0 attributeDef
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.Attribute qualifiedName=\"" + attributeDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");
  
    if(this.allFeatures) {
      writeElement("identity", attributeDef.path().toString());
      writeElementAsDateTime("createdAt", (String)attributeDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)attributeDef.values("modifiedAt").get(0));
    }
    writeElement("isDerived", attributeDef.values("isDerived").get(0));
    writeElement("maxLength", attributeDef.values("maxLength").get(0));
    writeElement("container", attributeDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", attributeDef.values("name").get(0));
      writeElement("qualifiedName", attributeDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", attributeDef.values("stereotype"));
    writeElement("scope", attributeDef.values("scope").get(0));
    writeElement("visibility", attributeDef.values("visibility").get(0));
    writeElement("isChangeable", attributeDef.values("isChangeable").get(0));
    writeElement("multiplicity", attributeDef.values("multiplicity").get(0));
    writeElement("type", attributeDef.values("type").get(0));
  
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Attribute>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeStructureField(
    ModelElement_1_0 structureFieldDef
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.StructureField qualifiedName=\"" + structureFieldDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");
  
    if(this.allFeatures) {
      writeElement("identity", structureFieldDef.path().toString());
      writeElementAsDateTime("createdAt", (String)structureFieldDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)structureFieldDef.values("modifiedAt").get(0));
    }
    writeElement("maxLength", structureFieldDef.values("maxLength").get(0));
    writeElement("multiplicity", structureFieldDef.values("multiplicity").get(0));
    writeElement("container", structureFieldDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", structureFieldDef.values("name").get(0));
      writeElement("qualifiedName", structureFieldDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", structureFieldDef.values("stereotype"));
    writeElement("type", structureFieldDef.values("type").get(0));
  
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.StructureField>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeOperation(
    ModelElement_1_0 operationDef
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.Operation qualifiedName=\"" + operationDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", operationDef.path().toString());
      writeElementAsDateTime("createdAt", (String)operationDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)operationDef.values("modifiedAt").get(0));
    }
    if(this.allFeatures) {
      writeElement("parameter", operationDef.values("parameter"));
    }
    writeElement("container", operationDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", operationDef.values("name").get(0));
      writeElement("qualifiedName", operationDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", operationDef.values("stereotype"));
    writeElement("scope", operationDef.values("scope").get(0));
    writeElement("visibility", operationDef.values("visibility").get(0));
    writeElement("exception", operationDef.values("exception"));
    writeElementEncoded("semantics", operationDef.values("semantics"));
    writeElement("isQuery", operationDef.values("isQuery").get(0));
    if(this.allFeatures) {
      writeElement("content", operationDef.values("content"));
    }

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Operation>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writeException(
    ModelElement_1_0 exceptionDef
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.Exception qualifiedName=\"" + exceptionDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", exceptionDef.path().toString());
      writeElementAsDateTime("createdAt", (String)exceptionDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)exceptionDef.values("modifiedAt").get(0));
    }
    if(this.allFeatures) {
      writeElement("parameter", exceptionDef.values("parameter"));
    }
    writeElement("container", exceptionDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", exceptionDef.values("name").get(0));
      writeElement("qualifiedName", exceptionDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", exceptionDef.values("stereotype"));
    writeElement("scope", exceptionDef.values("scope").get(0));
    writeElement("visibility", exceptionDef.values("visibility").get(0));
    if(this.allFeatures) {
      writeElement("content", exceptionDef.values("content"));
    }

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Exception>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writeParameter(
    ModelElement_1_0 parameterDef
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.Parameter qualifiedName=\"" + parameterDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", parameterDef.path().toString());
      writeElementAsDateTime("createdAt", (String)parameterDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)parameterDef.values("modifiedAt").get(0));
    }
    writeElement("container", parameterDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", parameterDef.values("name").get(0));
      writeElement("qualifiedName", parameterDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", parameterDef.values("stereotype"));
    writeElement("direction", parameterDef.values("direction").get(0));
    writeElement("multiplicity", parameterDef.values("multiplicity").get(0));
    writeElement("type", parameterDef.values("type").get(0));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Parameter>");  
    this.pw.flush();

  }
  
  //---------------------------------------------------------------------------
  public void writeAssociation(
    ModelElement_1_0 associationDef
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.Association qualifiedName=\"" + associationDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", associationDef.path().toString());
      writeElementAsDateTime("createdAt", (String)associationDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)associationDef.values("modifiedAt").get(0));
    }
    writeElement("container", associationDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", associationDef.values("name").get(0));
      writeElement("qualifiedName", associationDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", associationDef.values("stereotype"));
    writeElement("isAbstract", associationDef.values("isAbstract").get(0));
    if(this.allFeatures) {
      writeElement("allSupertype", associationDef.values("allSupertype"));
      writeElement("subtype", associationDef.values("allSubtype"));
      writeElement("content", associationDef.values("content"));
    }
    writeElement("supertype", associationDef.values("supertype"));
    writeElement("visibility", associationDef.values("visibility").get(0));  
    writeElement("isDerived", associationDef.values("isDerived").get(0));  

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Association>");
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeAssociationEnd(
    ModelElement_1_0 associationEndDef
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.AssociationEnd qualifiedName=\"" + associationEndDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", associationEndDef.path().toString());
      writeElementAsDateTime("createdAt", (String)associationEndDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)associationEndDef.values("modifiedAt").get(0));
    }
    writeElement("aggregation", associationEndDef.values("aggregation").get(0));
    writeElement("isChangeable", associationEndDef.values("isChangeable").get(0));
    writeElement("isNavigable", associationEndDef.values("isNavigable").get(0));
    writeElement("multiplicity", associationEndDef.values("multiplicity").get(0));
    writeElement("qualifierName", associationEndDef.values("qualifierName"));
    writeElement("qualifierType", associationEndDef.values("qualifierType"));
    writeElement("container", associationEndDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", associationEndDef.values("name").get(0));
      writeElement("qualifiedName", associationEndDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", associationEndDef.values("stereotype"));
    writeElement("type", associationEndDef.values("type").get(0));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.AssociationEnd>");  
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeReference(
    ModelElement_1_0 referenceDef
  ) {

    this.pw.println(spaces(36) + "<org.omg.model1.Reference qualifiedName=\"" + referenceDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", referenceDef.path().toString());
      writeElementAsDateTime("createdAt", (String)referenceDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)referenceDef.values("modifiedAt").get(0));
    }
    writeElement("container", referenceDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", referenceDef.values("name").get(0));
      writeElement("qualifiedName", referenceDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", referenceDef.values("stereotype"));
    writeElement("scope", referenceDef.values("scope").get(0));
    writeElement("visibility", referenceDef.values("visibility").get(0));
    writeElement("exposedEnd", referenceDef.values("exposedEnd").get(0));
    writeElement("referencedEnd", referenceDef.values("referencedEnd").get(0));
    if(this.allFeatures) {
      writeElement("referencedEndIsNavigable", referenceDef.values("referencedEndIsNavigable").get(0));
    }
    writeElement("isChangeable", referenceDef.values("isChangeable").get(0));
    writeElement("multiplicity", referenceDef.values("multiplicity").get(0));
    writeElement("type", referenceDef.values("type").get(0));

    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Reference>");  
    this.pw.flush();

  }

  //---------------------------------------------------------------------------
  public void writeClass(
    ModelElement_1_0 classDef
  ) {
  
    this.pw.println(spaces(36) + "<org.omg.model1.Class qualifiedName=\"" + classDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", classDef.path().toString());
      writeElementAsDateTime("createdAt", (String)classDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)classDef.values("modifiedAt").get(0));
    }
    writeElement("isSingleton", classDef.values("isSingleton").get(0));
    if(this.allFeatures) {
      writeElement("feature", classDef.values("feature"));
      writeElement("content", classDef.values("content"));
    }
    writeElement("container", classDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", classDef.values("name").get(0));
      writeElement("qualifiedName", classDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", classDef.values("stereotype"));
    writeElement("isAbstract", classDef.values("isAbstract").get(0));
    if(this.allFeatures) {
      writeElement("allSupertype", classDef.values("allSupertype"));
      writeElement("subtype", classDef.values("allSubtype"));
    }
    writeElement("supertype", classDef.values("supertype"));
    writeElement("visibility", classDef.values("visibility").get(0));  
    if(this.allFeatures) {
      writeElement("compositeReference", classDef.values("compositeReference").get(0));  
    }
   
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.Class>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeStructureType(
    ModelElement_1_0 structDef
  ) {
  
    this.pw.println(spaces(36) + "<org.omg.model1.StructureType qualifiedName=\"" + structDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", structDef.path().toString());
      writeElementAsDateTime("createdAt", (String)structDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)structDef.values("modifiedAt").get(0));
    }
    if(this.allFeatures) {
      writeElement("feature", structDef.values("feature"));
      writeElement("content", structDef.values("content"));
    }
    writeElement("container", structDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", structDef.values("name").get(0));
      writeElement("qualifiedName", structDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", structDef.values("stereotype"));
    writeElement("isAbstract", structDef.values("isAbstract").get(0));
    if(this.allFeatures) {
      writeElement("allSupertype", structDef.values("allSupertype"));
      writeElement("subtype", structDef.values("allSubtype"));
    }
    writeElement("supertype", structDef.values("supertype"));
    writeElement("visibility", structDef.values("visibility").get(0));  
    writeElement("compositeReference", structDef.values("compositeReference").get(0));  
   
    this.pw.println(spaces(40) + "</_object>");
    this.pw.println(spaces(40) + "<_content/>");
    this.pw.println(spaces(36) + "</org.omg.model1.StructureType>");  
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeAliasType(
    ModelElement_1_0 aliasTypeDef
  ) {
  
    this.pw.println(spaces(36) + "<org.omg.model1.AliasType qualifiedName=\"" + aliasTypeDef.values("qualifiedName").get(0) + "\">");
    this.pw.println(spaces(40) + "<_object>");

    if(this.allFeatures) {
      writeElement("identity", aliasTypeDef.path().toString());
      writeElementAsDateTime("createdAt", (String)aliasTypeDef.values("createdAt").get(0));
      writeElementAsDateTime("modifiedAt", (String)aliasTypeDef.values("modifiedAt").get(0));
    }
    if(this.allFeatures) {
      writeElement("feature", aliasTypeDef.values("feature"));
      writeElement("content", aliasTypeDef.values("content"));
    }
    writeElement("container", aliasTypeDef.values("container").get(0));
    if(this.allFeatures) {
      writeElement("name", aliasTypeDef.values("name").get(0));
      writeElement("qualifiedName", aliasTypeDef.values("qualifiedName").get(0));
    }
    writeElement("stereotype", aliasTypeDef.values("stereotype"));
    writeElement("isAbstract", aliasTypeDef.values("isAbstract").get(0));
    if(this.allFeatures) {
      writeElement("allSupertype", aliasTypeDef.values("allSupertype"));
      writeElement("subtype", aliasTypeDef.values("allSubtype"));
    }
    writeElement("supertype", aliasTypeDef.values("supertype"));
    writeElement("visibility", aliasTypeDef.values("visibility").get(0));  
    writeElement("type", aliasTypeDef.values("type").get(0));
    writeElement("compositeReference", aliasTypeDef.values("compositeReference").get(0));  
   
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

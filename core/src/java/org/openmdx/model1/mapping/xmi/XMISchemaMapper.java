/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMISchemaMapper.java,v 1.6 2008/03/21 18:40:22 hburger Exp $
 * Description: write XML schema (XSD)
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:40:22 $
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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.openmdx.model1.code.Stereotypes;

@SuppressWarnings("unchecked")
public class XMISchemaMapper {
  
  //---------------------------------------------------------------------------
  public XMISchemaMapper(
    PrintWriter os,
    org.openmdx.model1.accessor.basic.cci.Model_1_0 model
  ) {
    this.pw = new PrintWriter(os);
    this.model = model;
  }

  //---------------------------------------------------------------------------
  private String toXsdName(
    String qualifiedName
  ) {
    return qualifiedName.replace(':', '.');
  }

  //---------------------------------------------------------------------------
  private String toXsdTypeName(
    String qualifiedName
  ) {
    if(PrimitiveTypes.DURATION.equals(qualifiedName)) { return "xsd:duration"; }
    else if(PrimitiveTypes.DATETIME.equals(qualifiedName)) { return "xsd:dateTime"; }
    else if(PrimitiveTypes.STRING.equals(qualifiedName)) { return "xsd:string"; }
    else if(PrimitiveTypes.SHORT.equals(qualifiedName)) { return "xsd:short"; }
    else if(PrimitiveTypes.LONG.equals(qualifiedName)) { return "xsd:long"; }
    else if(PrimitiveTypes.INTEGER.equals(qualifiedName)) { return "xsd:integer"; }
    else if(PrimitiveTypes.BOOLEAN.equals(qualifiedName)) { return "xsd:boolean"; }
    else if(PrimitiveTypes.DECIMAL.equals(qualifiedName)) { return "xsd:decimal"; }
    else if(PrimitiveTypes.DATE.equals(qualifiedName)) { return "xsd:date"; }
    else if(PrimitiveTypes.ANYURI.equals(qualifiedName)) { return "xsd:anyURI"; }
    else { 
      return "xsd:string";
    }      
  }

  //---------------------------------------------------------------------------
  public void writeQualifierAttributes(
    String _qualifierName, 
    String qualifierTypeName,
    boolean typeIsPrimitive
  ) {
      String qualifierName = _qualifierName.equals("elementName")
          ? "name"
          : _qualifierName;
      this.pw.println(spaces(8) + "<xsd:attribute name=\"" + qualifierName +  "\" type=\"" + (typeIsPrimitive ? toXsdTypeName(qualifierTypeName) : "org.openmdx.base.ObjectId") + "\" use=\"required\"/>");
      this.pw.println(spaces(8) + "<xsd:attribute name=\"_qualifier\" type=\"xsd:string\" fixed=\"" + qualifierName + "\"/>");
      this.pw.println(spaces(8) + "<xsd:attribute name=\"_operation\" type=\"xsd:string\" use=\"optional\"/>");
      this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writePrimitiveType(
    ModelElement_1_0 primitiveTypeDef
  ) {
    String qualifiedName = (String)primitiveTypeDef.values("qualifiedName").get(0);
    this.pw.println(spaces(4) + "<xsd:simpleType name=\"" + toXsdName(qualifiedName) + "\">");
    this.pw.println(spaces(4) + "  <xsd:annotation>");
    this.pw.println(spaces(4) + "    <xsd:documentation>this is a " + qualifiedName + "</xsd:documentation>");
    this.pw.println(spaces(4) + "  </xsd:annotation>");
    this.pw.println(spaces(4) + "  <xsd:restriction base=\"" + toXsdTypeName(qualifiedName) + "\"/>");
    this.pw.println(spaces(4) + "</xsd:simpleType>");
  }

  //---------------------------------------------------------------------------    
  /**
   * Produce two forms of an attribute or structure field:
   * <ul>
   *   <li>single-valued: element</li>
   *   <li>Multi-valued: 0..unbounded repetions of item within element</li>
   * </ul>
   */
  private void writeField(
    String name,
    String multiplicity,
    String typeName
  ) throws ServiceException {
    if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
      this.pw.println(spaces(24) + "<xsd:element name=\"" + name + "\" type=\"" + typeName + "\" minOccurs=\"0\" maxOccurs=\"1\"/>");
    } 
    else if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
      this.pw.println(spaces(24) + "<xsd:element name=\"" + name + "\" type=\"" + typeName + "\"/>");
    } 
    else {
      String contentType = this.model.isPrimitiveType(typeName.replace('.',':')) ? "simple" : "complex";
      boolean isMap = Multiplicities.MAP.equals(multiplicity);
      this.pw.println(spaces(24) + "<xsd:element name=\"" + name + "\" minOccurs=\"0\" maxOccurs=\"1\">");
      this.pw.println(spaces(24) + "    <xsd:complexType>");
      this.pw.println(spaces(24) + "        <xsd:sequence minOccurs=\"0\" maxOccurs=\"unbounded\">");
      this.pw.println(spaces(24) + "            <xsd:element name=\"_item\">");
      this.pw.println(spaces(24) + "                <xsd:complexType>");
      this.pw.println(spaces(24) + "                    <xsd:" + contentType + "Content>");
      this.pw.println(spaces(24) + "                        <xsd:extension base=\"" + typeName + "\">");
      this.pw.println(spaces(24) + "                            <xsd:attribute name=\"_operation\" type=\"xsd:string\" use=\"optional\" default=\"\"/>");
      if(isMap) {
        this.pw.println(spaces(24) + "                            <xsd:attribute name=\"_key\" type=\"xsd:string\" use=\"optional\" default=\"-1\"/>");
      }
      else {
        this.pw.println(spaces(24) + "                            <xsd:attribute name=\"_position\" type=\"xsd:integer\" use=\"optional\" default=\"-1\"/>");
      }
      this.pw.println(spaces(24) + "                        </xsd:extension>");
      this.pw.println(spaces(24) + "                    </xsd:" + contentType + "Content>");
      this.pw.println(spaces(24) + "                </xsd:complexType>");
      this.pw.println(spaces(24) + "            </xsd:element>");
      this.pw.println(spaces(24) + "        </xsd:sequence>");
      if(!isMap) {
        this.pw.println(spaces(24) + "        <xsd:attribute name=\"_offset\" type=\"xsd:integer\" use=\"optional\" default=\"0\"/>");
      }
      this.pw.println(spaces(24) + "        <xsd:attribute name=\"_multiplicity\" type=\"xsd:string\" fixed=\"" + multiplicity + "\"/>");
      this.pw.println(spaces(24) + "    </xsd:complexType>");
      this.pw.println(spaces(24) + "</xsd:element>");      
    } 
  }

  //---------------------------------------------------------------------------
  public void writeAttribute(
    ModelElement_1_0 attributeDef,
    boolean isClass
  ) throws ServiceException {

    String attributeName = (String)attributeDef.values("name").get(0);
    String qualifiedTypeName = (String)this.model.getDereferencedType(attributeDef.values("type").get(0)).values("qualifiedName").get(0);
    String multiplicity = (String) attributeDef.values("multiplicity").get(0);
    String typeName = isClass
      ? "org.openmdx.base.ObjectId"
      : toXsdName(qualifiedTypeName);

    writeField(
      attributeName,
      multiplicity,
      typeName
    );
    
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeStructureField(
    ModelElement_1_0 structureFieldDef,
    boolean isClass
  ) throws ServiceException {

    String fieldName = (String)structureFieldDef.values("name").get(0);
    String qualifiedTypeName = (String)this.model.getDereferencedType(structureFieldDef.values("type").get(0)).values("qualifiedName").get(0);
    String multiplicity = (String) structureFieldDef.values("multiplicity").get(0);
    String typeName = isClass
      ? "org.openmdx.base.ObjectId"
      : toXsdName(qualifiedTypeName);

    writeField(
      fieldName,
      multiplicity,
      typeName
    );
    
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  private Set getCoreSubtypes(
    ModelElement_1_0 typeDef
  ) throws ServiceException {
    Set coreSubtypes = new TreeSet();
    for(
      Iterator i = typeDef.values("allSubtype").iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 subtype = this.model.getDereferencedType(i.next());
      if(
        subtype.values("supertype").contains(typeDef.path()) &&
        !subtype.values("stereotype").contains(Stereotypes.ROLE)
      ) {
        coreSubtypes.add(subtype.path());
        coreSubtypes.addAll(getCoreSubtypes(subtype));
      }
    }
    coreSubtypes.add(typeDef.path());
    return coreSubtypes;
  }

  //---------------------------------------------------------------------------
  private Set getRoleTypes(
    ModelElement_1_0 typeDef
  ) throws ServiceException {

    Set roleTypes = new TreeSet();
    
    /**
     * get role types from supertypes
     */
    for(
      Iterator i = typeDef.values("supertype").iterator();
      i.hasNext();
    ) {
      roleTypes.addAll(
        this.getRoleTypes(
          this.model.getDereferencedType(i.next())
        )
      );
    }
    
    /**
     * get direct role types from typeDef, i.e.
     * all direct subtypes with stereotype role
     */
    for(
      Iterator i = typeDef.values("allSubtype").iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 subtype = this.model.getDereferencedType(i.next());
      if(
        subtype.values("supertype").contains(typeDef.path()) &&
        subtype.values("stereotype").contains(Stereotypes.ROLE)
      ) {
        roleTypes.add(subtype.path());
        roleTypes.addAll(this.getCoreSubtypes(subtype));
      }
    }
    
    return roleTypes;
  }

  //---------------------------------------------------------------------------
  public void writeReferenceStoredAsAttribute(
    ModelElement_1_0 referenceDef
  ) throws ServiceException {
    String referenceName = (String)referenceDef.values("name").get(0);
    String multiplicity = this.model.getElement(referenceDef.values("referencedEnd").get(0)).values("qualifierName").size() > 0 
      ? "list"
      : (String)referenceDef.values("multiplicity").get(0);
    writeField(
      referenceName,
      multiplicity,
      "org.openmdx.base.ObjectId"
    );
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  /**
   * Write reference which is exposed by exposingType (possibly inherited from
   * super class).
   */
  public void writeReference(
    ModelElement_1_0 referenceDef,
    ModelElement_1_0 exposingType
  ) throws ServiceException {
    
    String referenceName = (String)referenceDef.values("name").get(0);
    ModelElement_1_0 referencedType = this.model.getDereferencedType(referenceDef.values("type").get(0));
    
    // in case of a recursive reference, only the subclasses of exposingClass
    // with stereotype <<role>> are added to the list of allowed types
    boolean isRecursiveReference = this.model.isSubtypeOf(exposingType, referencedType);

    this.pw.println(spaces(12) + "<xsd:element name=\"" + referenceName + "\" minOccurs=\"0\" maxOccurs=\"1\">");
    this.pw.println(spaces(16) + "<xsd:complexType>");
    this.pw.println(spaces(20) + "<xsd:choice minOccurs=\"0\" maxOccurs=\"unbounded\">");

    // references to all non-abstract subclasses of supplier class
    for(
      Iterator i = (isRecursiveReference ? this.getRoleTypes(exposingType) : this.getCoreSubtypes(referencedType)).iterator();
      i.hasNext();
    ) {
      ModelElement_1_0 subtype = this.model.getDereferencedType(i.next());
      if(!isAbstract(subtype)) {
        String qualifiedName = (String)subtype.values("qualifiedName").get(0);
        this.pw.println(spaces(24) + "<xsd:element name=\"" + toXsdName(qualifiedName) + "\" type=\"" + toXsdName(qualifiedName) + "\"/>");
      }

    }
    this.pw.println(spaces(20) + "</xsd:choice>");
    this.pw.println(spaces(16) + "</xsd:complexType>");
    this.pw.println(spaces(12) + "</xsd:element>");
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  public void writeAttributeFeatureHeader(
    ModelElement_1_0 classDef
  ) {
    this.pw.println(spaces(8)  + "<xsd:sequence>");
    this.pw.println(spaces(12) + "<xsd:element name=\"_object\">");
    this.pw.println(spaces(16) + "<xsd:complexType>");
    this.pw.print  (spaces(20) + "<xsd:choice minOccurs=\"0\" maxOccurs=\"unbounded\">");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeAttributeFeatureFooter(
    ModelElement_1_0 classDef
  ) {
    this.pw.println("</xsd:choice>");
    this.pw.println(spaces(16) + "</xsd:complexType>");    
    this.pw.println(spaces(12) + "</xsd:element>");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeStructureFieldHeader(
    ModelElement_1_0 structDef
  ) {
    this.pw.println(spaces(20) + "<xsd:all>");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeStructureFieldFooter(
    ModelElement_1_0 structDef
  ) {
    this.pw.println(spaces(20) + "</xsd:all>");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeCompositeReferenceFeatureHeader(
    ModelElement_1_0 classDef
  ) {
    this.pw.println(spaces(12) + "<xsd:element name=\"_content\">");
    this.pw.println(spaces(16) + "<xsd:complexType>");
    this.pw.print  (spaces(20) + "<xsd:all>");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeCompositeReferenceFeatureFooter(
    ModelElement_1_0 classDef,
    int compositeReferenceCount
  ) {
    if(compositeReferenceCount == 0) {
        this.pw.println(spaces(12) + "<xsd:element name=\"_content\">");
        this.pw.println(spaces(16) + "<xsd:complexType/>");    
    } else {
        this.pw.println("</xsd:all>");
        this.pw.println(spaces(16) + "</xsd:complexType>");    
    }
    this.pw.println(spaces(12) + "</xsd:element>");
    this.pw.println(spaces(8)  + "</xsd:sequence>");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeComplexTypeHeader(
    ModelElement_1_0 classDef
  ) {
    
//    String className = (String) classDef.values("name").get(0);
    String qualifiedClassName = (String) classDef.values("qualifiedName").get(0);
        
    this.pw.println(spaces(4) + "<xsd:complexType name=\"" + toXsdName(qualifiedClassName) + "\" abstract=\"" + isAbstract(classDef) + "\">");

    // class documentation
    this.pw.println(spaces(8) + "<xsd:annotation>");
    this.pw.println(spaces(12) + "<xsd:documentation>class " + qualifiedClassName + "</xsd:documentation>");
    this.pw.println(spaces(8) + "</xsd:annotation>");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeComplexTypeFooter(
    ModelElement_1_0 classDef
  ) {
    // the implicit qualifier of org:openmdx:base:Authority is 'name: org::w3c::string'. This qualifier is not
    // modeled. Therefore force generation.
    if("org:openmdx:base:Authority".equals(classDef.values("qualifiedName").get(0))) {
      this.pw.println(spaces(8) + "<xsd:attribute name=\"name\" type=\"org.w3c.string\" use=\"required\"/>");
      this.pw.println(spaces(8) + "<xsd:attribute name=\"_qualifier\" type=\"xsd:string\" fixed=\"name\"/>");
      this.pw.println(spaces(8) + "<xsd:attribute name=\"_operation\" type=\"xsd:string\" use=\"optional\"/>");
    }
    this.pw.println(spaces(4) + "</xsd:complexType>");
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeSchemaHeader(
    boolean objectIdDefinitionRequired
  ) {
    this.pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    this.pw.println("<!-- generated by openMDX XMI Schema Writer -->");
    this.pw.println("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">");
    this.pw.println(spaces(4) + "<xsd:element name=\"org.openmdx.base.Authority\" type=\"org.openmdx.base.Authority\">");
    this.pw.println(spaces(4) + "  <xsd:annotation>");
    this.pw.println(spaces(4) + "    <xsd:documentation>Root element</xsd:documentation>");
    this.pw.println(spaces(4) + "  </xsd:annotation>");
    this.pw.println(spaces(4) + "</xsd:element>");
    if(objectIdDefinitionRequired) {
      this.pw.println(spaces(4) + "<xsd:simpleType name=\"org.openmdx.base.ObjectId\">");                
      this.pw.println(spaces(4) + "  <xsd:annotation>");                                                        
      this.pw.println(spaces(4) + "    <xsd:documentation>this is a org:openmdx:base:ObjectId</xsd:documentation>");
      this.pw.println(spaces(4) + "  </xsd:annotation>");                                                       
      this.pw.println(spaces(4) + "  <xsd:restriction base=\"xsd:string\"/>");        
      this.pw.println(spaces(4) + "</xsd:simpleType>");
    }
    this.pw.flush();
  }
  
  //---------------------------------------------------------------------------
  public void writeSchemaFooter(
  ) {
    this.pw.println("</xsd:schema>");
    this.pw.flush();
  }

  //---------------------------------------------------------------------------
  private boolean isAbstract(
    ModelElement_1_0 modelClass
  ) {
    Boolean isAbstract = (Boolean)modelClass.values("isAbstract").get(0);
    return isAbstract.booleanValue();
  }

  //---------------------------------------------------------------------------
  private String spaces(
    int number
  ) {
      while(XMISchemaMapper.spaces.length() < number) {
          XMISchemaMapper.spaces.append(' ');
      }
      return XMISchemaMapper.spaces.substring(0, number);
  }

  //---------------------------------------------------------------------------  
  // Variables
  //---------------------------------------------------------------------------  
  private final PrintWriter pw;
  private final Model_1_0 model;
  private final static StringBuffer spaces = new StringBuffer();
 
}

//--- End of File -----------------------------------------------------------

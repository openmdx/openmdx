/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: write XML schema (XSD)
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */

package org.openmdx.application.mof.mapping.xmi;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.omg.mof.cci.VisibilityKind;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.Stereotypes;
import org.openmdx.base.text.conversion.XMLEncoder;

/**
 * This class writes MOF model elements to a given PrintStream using the OMG's
 * XML Metadata Interchange (XMI) format. The XMI format is based on the UML
 * mapping and follows the UML Profile for MOF as closely as possible for the
 * openMDX project. 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class Uml1ModelMapper {
  
  //---------------------------------------------------------------------------
  /**
   * Creates a new instance of a XMIUmlModelInterchangeWriter.
   * @param printWriter output is written to this printWriter
   * @param writePoseidonXMI defines whether the XMI/UML output is Poseidon 
   * specific (qualifiers in association end name, exceptions an operation can
   * throw as tagged value)
   */
  public Uml1ModelMapper(
    PrintWriter printWriter,
    byte xmiDialect
  ) {
    this.pw = new PrintWriter(printWriter);
    this.xmiDialect = xmiDialect; 
    this.stereotypeIds = new HashMap();
    this.datatypeIds = new HashMap();
    this.classIds = new HashMap();
    this.generalizationIds = new HashMap();
    this.tagDefinitionIds = new HashMap();
  }

  //---------------------------------------------------------------------------
  public void writeExceptionHeader(
    ModelElement_1_0 exceptionDef
  ) throws ServiceException {
    String name = (String)exceptionDef.getName();
    
    pw.write(indent, 0, nTabs); pw.write("<UML:Operation xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = 'public' isSpecification = 'false' ownerScope = 'instance'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisQuery = 'false' concurrency = 'sequential' isRoot = 'false' isLeaf = 'false'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = 'false'>\n");

    // annotation
    if(!exceptionDef.objGetList("annotation").isEmpty()) {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)exceptionDef.objGetValue("annotation")
        )
      );
    }   
    pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.stereotype>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Stereotype xmi.idref = '"); pw.write(this.getStereotypeId(Stereotypes.EXCEPTION, "Operation")); pw.write("'/>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.stereotype>\n");
    pw.write(indent, 0, nTabs); pw.write("\t<UML:BehavioralFeature.parameter>\n");
    nTabs = nTabs + 2;
  }
  
  //---------------------------------------------------------------------------
  public void writeExceptionFooter(
    ModelElement_1_0 exceptionDef
  ) {
    nTabs = nTabs - 2;
    pw.write(indent, 0, nTabs); pw.write("\t</UML:BehavioralFeature.parameter>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:Operation>\n");
    pw.flush();      
  }
  
  //---------------------------------------------------------------------------
  public void writeOperationHeader(
    ModelElement_1_0 operationDef,
    List exceptions
  ) throws ServiceException {
    String name = (String)operationDef.getName();
    boolean isQuery = ((Boolean)operationDef.objGetValue("isQuery")).booleanValue();
    String visibility = this.toXMIVisibility((String)operationDef.objGetValue("visibility"));

    pw.write(indent, 0, nTabs); pw.write("<UML:Operation xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' ownerScope = 'instance'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisQuery = '"); pw.print(isQuery); pw.write("' concurrency = 'sequential' isRoot = 'false' isLeaf = 'false'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = 'false'>\n");

    // annotation
    if (!operationDef.objGetList("annotation").isEmpty()) {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)operationDef.objGetValue("annotation")
        )
      );
    }
    
    // Due to the limited support of Poseidon to enter the exceptions an 
    // operation can throw, a tagged value "exceptions" is used to enter
    // them.
    if (!exceptions.isEmpty())
    {
      StringBuffer sb = new StringBuffer();
      int nExceptions = exceptions.size();
      for(
        int index = 0;
        index < nExceptions;
        index++
      ) {
        sb.append(this.toMOFSyntax((String)((ModelElement_1_0)exceptions.get(index)).getQualifiedName()));
        if (index < nExceptions-1)
        {
          sb.append("; ");
        }
      }
      
      this.writeTaggedValue(
        "exceptions",
        sb.toString()
      );
    }
    pw.write(indent, 0, nTabs); pw.write("\t<UML:BehavioralFeature.parameter>\n");
    nTabs = nTabs + 2;
  }

  //---------------------------------------------------------------------------
  public void writeOperationFooter(
    ModelElement_1_0 operationDef
  ) {
    nTabs = nTabs - 2;
    pw.write(indent, 0, nTabs); pw.write("\t</UML:BehavioralFeature.parameter>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:Operation>\n");
    pw.flush();      
  }
  
  //---------------------------------------------------------------------------
  public void writeParameter(
    ModelElement_1_0 parameterDef,
    ModelElement_1_0 parameterTypeDef,
    boolean parameterTypeIsPrimitive
  ) throws ServiceException {
    String name = (String)parameterDef.getName();
    String typeQualifiedName = (String)parameterTypeDef.getQualifiedName();
    String direction = this.toXMIParameterKind((String)parameterDef.objGetValue("direction"));
    String multiplicity = (String)parameterDef.getMultiplicity();

    pw.write(indent, 0, nTabs); pw.write("<UML:Parameter xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisSpecification = 'false' kind = '"); pw.write(direction); pw.write("'>\n");

    // annotation
    if (!parameterDef.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)parameterDef.objGetValue("annotation")
        )
      );
    }    
    if (!Multiplicity.SINGLE_VALUE.toString().equals(multiplicity))
    {
      pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.stereotype>\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Stereotype xmi.idref = '"); pw.write(this.getStereotypeId(multiplicity, "Parameter")); pw.write("'/>\n");
      pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.stereotype>\n");
    }
    pw.write(indent, 0, nTabs); pw.write("\t<UML:Parameter.type>\n");
    if (parameterTypeIsPrimitive)
    {
      // type is DataType
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:DataType xmi.idref = '"); pw.write(this.getDataTypeId(typeQualifiedName)); pw.write("'/>\n");
    }
    else
    {
      // type is Class
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Class xmi.idref = '"); pw.write(this.getClassId(typeQualifiedName)); pw.write("'/>\n");
    }
    pw.write(indent, 0, nTabs); pw.write("\t</UML:Parameter.type>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:Parameter>\n");
    pw.flush();      
  }
  
  //---------------------------------------------------------------------------
  public void writeAssociationHeader(
    ModelElement_1_0 associationDef
  ) throws ServiceException {
    String name = (String)associationDef.getName();
    boolean isAbstract = ((Boolean)associationDef.isAbstract()).booleanValue();
    boolean isDerived = ((Boolean)associationDef.isDerived()).booleanValue();
    
    pw.write(indent, 0, nTabs); pw.write("<UML:Association xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisSpecification = 'false' isRoot = 'false' isLeaf = 'false' isAbstract = '"); pw.print(isAbstract); pw.write("'>\n");

    // annotation
    if (!associationDef.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)associationDef.objGetValue("annotation")
        )
      );
    }
    
    if (isDerived)
    {
      this.writeTaggedValue("derived", "true");
    }
    pw.write(indent, 0, nTabs); pw.write("\t<UML:Association.connection>\n");
    nTabs = nTabs + 2;
  }
  
  //---------------------------------------------------------------------------
  public void writeAssociationFooter(
    ModelElement_1_0 associationDef
  ) {
    nTabs = nTabs - 2;
    pw.write(indent, 0, nTabs); pw.write("\t</UML:Association.connection>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:Association>\n");
    pw.flush();      
  }
  
  //---------------------------------------------------------------------------
  public void writeAssociationEnds(
    ModelElement_1_0 associationEnd1Def,
    ModelElement_1_0 associationEnd1TypeDef,
    List associationEnd1QualifierTypes,
    ModelElement_1_0 associationEnd2Def,
    ModelElement_1_0 associationEnd2TypeDef,
    List associationEnd2QualifierTypes
  ) throws ServiceException {
    boolean assEnd1IsNavigable = ((Boolean)associationEnd1Def.objGetValue("isNavigable")).booleanValue();
    boolean assEnd1IsChangeable = ((Boolean)associationEnd1Def.isChangeable()).booleanValue();
    String assEnd1TypeQualifiedName = (String)associationEnd1TypeDef.getQualifiedName();
    String assEnd1Aggregation = (String)associationEnd1Def.getAggregation();

    boolean assEnd2IsNavigable = ((Boolean)associationEnd2Def.objGetValue("isNavigable")).booleanValue();
    boolean assEnd2IsChangeable = ((Boolean)associationEnd2Def.isChangeable()).booleanValue();
    String assEnd2TypeQualifiedName = (String)associationEnd2TypeDef.getQualifiedName();
    String assEnd2Aggregation = (String)associationEnd2Def.getAggregation();
    
    // Note:
    // Aggregation and qualifier assignments must be changed to comply.
    
    String assEnd1Name;
    if (POSEIDON_XMI_DIALECT == this.xmiDialect)
    {
      // use Poseidon specific association end name (not XMI/UML compliant)
      // which includes qualfiers beside the association end name
      assEnd1Name = this.toPoseidonAssociationEndName(
        (String)associationEnd1Def.getName(),
        associationEnd2Def.objGetList("qualifierName"),
        associationEnd2QualifierTypes
      );
    }
    else
    {
      // use XMI/UML compliant association end name
      assEnd1Name = (String)associationEnd1Def.getName();
    }
    pw.write(indent, 0, nTabs); pw.write("<UML:AssociationEnd xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(assEnd1Name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = 'public' isSpecification = 'false' isNavigable = '"); pw.print(assEnd1IsNavigable); pw.write("' ordering = 'unordered'\n");
    pw.write(indent, 0, nTabs); pw.write("\taggregation = '"); pw.write(this.toXMIAggregation(assEnd2Aggregation)); pw.write("' targetScope = 'instance' changeability = '"); pw.write(this.toXMIChangeability(assEnd1IsChangeable)); pw.write("'>\n");

    // annotation
    if (!associationEnd1Def.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)associationEnd1Def.objGetValue("annotation")
        )
      );
    }
    
    pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.multiplicity>\n");
    nTabs = nTabs + 2;
    this.writeAssociationEndMultiplicity((String)associationEnd1Def.getMultiplicity());
    nTabs = nTabs - 2;
    pw.write(indent, 0, nTabs); pw.write("\t</UML:AssociationEnd.multiplicity>\n");

    int nAssEnd1Qualifiers = associationEnd1Def.objGetList("qualifierName").size();
    if (MAGICDRAW_XMI_DIALECT == this.xmiDialect && nAssEnd1Qualifiers > 0)
    {
      pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.qualifier>\n");
      for(
        int index = 0;
        index < nAssEnd1Qualifiers;
        index++
      ) {
        String qualifierName = (String)associationEnd1Def.objGetList("qualifierName").get(index);
        String qualifierTypeName = (String)((ModelElement_1_0)associationEnd1QualifierTypes.get(index)).getQualifiedName();
        pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Attribute xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(qualifierName); pw.write("'\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\tvisibility = 'public' isSpecification = 'false' ownerScope = 'instance'\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\tchangeability = 'changeable'>\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\t<UML:StructuralFeature.type>\n");
        if (((ModelElement_1_0)associationEnd1QualifierTypes.get(index)).objGetClass().equals(ModelAttributes.PRIMITIVE_TYPE))
        {
          // type is DataType
          pw.write(indent, 0, nTabs); pw.write("\t\t\t\t<UML:DataType xmi.idref = '"); pw.write(this.getDataTypeId(qualifierTypeName)); pw.write("'/>\n");
        }
        else
        {
          // type is Class
          pw.write(indent, 0, nTabs); pw.write("\t\t\t\t<UML:Class xmi.idref = '"); pw.write(this.getClassId(qualifierTypeName)); pw.write("'/>\n");
        }
        pw.write(indent, 0, nTabs); pw.write("\t\t\t</UML:StructuralFeature.type>\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t</UML:Attribute>\n");
      }
      pw.write(indent, 0, nTabs); pw.write("\t</UML:AssociationEnd.qualifier>\n");
    }

    pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.participant>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Class xmi.idref = '"); pw.write(this.getClassId(assEnd1TypeQualifiedName)); pw.write("'/>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:AssociationEnd.participant>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:AssociationEnd>\n");

    String assEnd2Name;
    if (POSEIDON_XMI_DIALECT == this.xmiDialect)
    {
      // use Poseidon specific association end name (not XMI/UML compliant)
      // which includes qualfiers beside the association end name
      assEnd2Name = this.toPoseidonAssociationEndName(
        (String)associationEnd2Def.getName(),
        associationEnd1Def.objGetList("qualifierName"),
        associationEnd1QualifierTypes
      );
    }
    else
    {
      // use XMI/UML compliant association end name
      assEnd2Name = (String)associationEnd2Def.getName();
    }
    pw.write(indent, 0, nTabs); pw.write("<UML:AssociationEnd xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(assEnd2Name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = 'public' isSpecification = 'false' isNavigable = '"); pw.print(assEnd2IsNavigable); pw.write("' ordering = 'unordered'\n");
    pw.write(indent, 0, nTabs); pw.write("\taggregation = '"); pw.write(this.toXMIAggregation(assEnd1Aggregation)); pw.write("' targetScope = 'instance' changeability = '"); pw.write(this.toXMIChangeability(assEnd2IsChangeable)); pw.write("'>\n");

    // annotation
    if (!associationEnd2Def.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)associationEnd2Def.objGetValue("annotation")
        )
      );
    }
    
    pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.multiplicity>\n");
    nTabs = nTabs + 2;
    this.writeAssociationEndMultiplicity((String)associationEnd2Def.getMultiplicity());
    nTabs = nTabs - 2;
    pw.write(indent, 0, nTabs); pw.write("\t</UML:AssociationEnd.multiplicity>\n");

    int nAssEnd2Qualifiers = associationEnd2Def.objGetList("qualifierName").size();
    if (MAGICDRAW_XMI_DIALECT == this.xmiDialect && nAssEnd2Qualifiers > 0)
    {
      pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.qualifier>\n");
      for(
        int index = 0;
        index < nAssEnd2Qualifiers;
        index++
      ) {
        String qualifierName = (String)associationEnd2Def.objGetList("qualifierName").get(index);
        String qualifierTypeName = (String)((ModelElement_1_0)associationEnd2QualifierTypes.get(index)).getQualifiedName();
        pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Attribute xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(qualifierName); pw.write("'\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\tvisibility = 'public' isSpecification = 'false' ownerScope = 'instance'\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\tchangeability = 'changeable'>\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\t<UML:StructuralFeature.type>\n");
        if (((ModelElement_1_0)associationEnd2QualifierTypes.get(index)).objGetClass().equals(ModelAttributes.PRIMITIVE_TYPE))
        {
          // type is DataType
          pw.write(indent, 0, nTabs); pw.write("\t\t\t\t<UML:DataType xmi.idref = '"); pw.write(this.getDataTypeId(qualifierTypeName)); pw.write("'/>\n");
        }
        else
        {
          // type is Class
          pw.write(indent, 0, nTabs); pw.write("\t\t\t\t<UML:Class xmi.idref = '"); pw.write(this.getClassId(qualifierTypeName)); pw.write("'/>\n");
        }
        pw.write(indent, 0, nTabs); pw.write("\t\t\t</UML:StructuralFeature.type>\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t</UML:Attribute>\n");
      }
      pw.write(indent, 0, nTabs); pw.write("\t</UML:AssociationEnd.qualifier>\n");
    }

    pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.participant>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Class xmi.idref = '"); pw.write(this.getClassId(assEnd2TypeQualifiedName)); pw.write("'/>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:AssociationEnd.participant>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:AssociationEnd>\n");
    pw.flush();      
  }
  
  //---------------------------------------------------------------------------
  public void writePrimitiveType(
    ModelElement_1_0 primitiveTypeDef
  ) throws ServiceException {
    String name = (String)primitiveTypeDef.getName();
    String visibility = this.toXMIVisibility((String)primitiveTypeDef.objGetValue("visibility"));
    boolean isAbstract = ((Boolean)primitiveTypeDef.isAbstract()).booleanValue();
    String typeId = this.getDataTypeId((String)primitiveTypeDef.getQualifiedName());

    pw.write(indent, 0, nTabs); pw.write("<UML:DataType xmi.id = '"); pw.write(typeId); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = '"); pw.print(isAbstract); pw.write("'>\n");

    // annotation
    if (!primitiveTypeDef.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)primitiveTypeDef.objGetValue("annotation")
        )
      );
    }
    pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.stereotype>\n");
    String primitiveStereotypeId = this.getStereotypeId(Stereotypes.PRIMITIVE, "DataType");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Stereotype xmi.idref = '"); pw.write(primitiveStereotypeId); pw.write("'/>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.stereotype>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:DataType>\n");
    pw.flush();      
  }

  //---------------------------------------------------------------------------
  public void writeAttribute(
    ModelElement_1_0 attributeDef,
    boolean isDerived,
    boolean isChangeable,
    ModelElement_1_0 typeDef,
    boolean referencedTypeIsPrimitive
  ) throws ServiceException {
    String name = (String)attributeDef.getName();
    String typeQualifiedName = (String)typeDef.getQualifiedName();
    String visibility = this.toXMIVisibility((String)attributeDef.objGetValue("visibility"));
    String changeability = this.toXMIChangeability(isChangeable);
    String multiplicity = (String)attributeDef.getMultiplicity();
    int maxLength = ((Number)attributeDef.objGetValue("maxLength")).intValue();
    
    pw.write(indent, 0, nTabs); pw.write("<UML:Attribute xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' ownerScope = 'instance'\n");
    pw.write(indent, 0, nTabs); pw.write("\tchangeability = '"); pw.write(changeability); pw.write("'>\n");
    
    // annotation
    if (!attributeDef.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)attributeDef.objGetValue("annotation")
        )
      );
    }
    
    this.writeTaggedValue(
      "maxLength",
      String.valueOf(maxLength)
    );

    if (isDerived)
    {    
      this.writeTaggedValue("derived", "true");
    }
    if (!Multiplicity.SINGLE_VALUE.toString().equals(multiplicity))
    {
      pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.stereotype>\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Stereotype xmi.idref = '"); pw.write(this.getStereotypeId(multiplicity, "Attribute")); pw.write("'/>\n");
      pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.stereotype>\n");
    }
    pw.write(indent, 0, nTabs); pw.write("\t<UML:StructuralFeature.type>\n");
    if (referencedTypeIsPrimitive)
    {
      // type is DataType
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:DataType xmi.idref = '"); pw.write(this.getDataTypeId(typeQualifiedName)); pw.write("'/>\n");
    }
    else
    {
      // type is Class
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Class xmi.idref = '"); pw.write(this.getClassId(typeQualifiedName)); pw.write("'/>\n");
    }
    pw.write(indent, 0, nTabs); pw.write("\t</UML:StructuralFeature.type>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:Attribute>\n");
    pw.flush();      
  }
  
  //---------------------------------------------------------------------------
  public void writeAliasType(
    ModelElement_1_0 aliasTypeDef,
    ModelElement_1_0 typeDef,
    boolean referencedTypeIsPrimitive
  ) throws ServiceException {
    String name = (String)aliasTypeDef.getName();
    String qualifiedName = (String)aliasTypeDef.getQualifiedName();
    String visibility = this.toXMIVisibility((String)aliasTypeDef.objGetValue("visibility"));
    boolean isAbstract = ((Boolean)aliasTypeDef.isAbstract()).booleanValue();
    String typeQualifiedName = (String)typeDef.getQualifiedName();
    String classId = this.getClassId(qualifiedName);

    pw.write(indent, 0, nTabs); pw.write("<UML:Class xmi.id = '"); pw.write(classId); pw.write("' name = '"); pw.write(name); pw.write("' visibility = '"); pw.write(visibility); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisSpecification = 'false' isRoot = 'false' isLeaf = 'false' isAbstract = '"); pw.print(isAbstract); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisActive = 'false'>\n");
    pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.stereotype>\n");
    String aliasStereotypeId = this.getStereotypeId(Stereotypes.ALIAS, "Class");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Stereotype xmi.idref = '"); pw.write(aliasStereotypeId); pw.write("'/>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.stereotype>\n");

    // annotation
    if (!aliasTypeDef.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)aliasTypeDef.objGetValue("annotation")
        )
      );
    }
    
    pw.write(indent, 0, nTabs); pw.write("\t<UML:Classifier.feature>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Attribute xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(this.toMOFSyntax(typeQualifiedName)); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t\tvisibility = 'public' isSpecification = 'false' ownerScope = 'instance'\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t\tchangeability = 'changeable'>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t\t<UML:StructuralFeature.type>\n");
    if (referencedTypeIsPrimitive)
    {
      // type is DataType
      pw.write(indent, 0, nTabs); pw.write("\t\t\t\t<UML:DataType xmi.idref = '"); pw.write(this.getDataTypeId(typeQualifiedName)); pw.write("'/>\n");
    }
    else
    {
      // type is Class
      pw.write(indent, 0, nTabs); pw.write("\t\t\t\t<UML:Class xmi.idref = '"); pw.write(this.getClassId(typeQualifiedName)); pw.write("'/>\n");
    }
    pw.write(indent, 0, nTabs); pw.write("\t\t\t</UML:StructuralFeature.type>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t</UML:Attribute>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:Classifier.feature>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:Class>\n");
    pw.flush();      
  }

  //---------------------------------------------------------------------------
  public void writePackageHeader(
    ModelElement_1_0 packageDef
  ) throws ServiceException {
    pw.write(indent, 0, nTabs); pw.write("<UML:Package xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write((String)packageDef.getName()); pw.write("' visibility = 'public'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisSpecification = 'false' isRoot = 'false' isLeaf = 'false' isAbstract = 'false'>\n");   
    pw.write(indent, 0, nTabs); pw.write("\t<UML:Namespace.ownedElement>\n");

    // annotation
    if (!packageDef.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)packageDef.objGetValue("annotation")
        )
      );
    }
    
    nTabs = nTabs + 2;
  }
  
  //---------------------------------------------------------------------------
  public void writePackageFooter(
    ModelElement_1_0 packageDef
  ) {
    nTabs = nTabs - 2;
    pw.write(indent, 0, nTabs); pw.write("\t</UML:Namespace.ownedElement>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:Package>\n");
    pw.flush();      
  }

  //---------------------------------------------------------------------------
  public void writeGeneralization(
    ModelElement_1_0 classDef,
    List subtypeDefs
  ) throws ServiceException {
    String parentTypeId = this.getClassId((String)classDef.getQualifiedName());
    for(
      Iterator it = subtypeDefs.iterator();
      it.hasNext();
    ) {
      ModelElement_1_0 childDef = (ModelElement_1_0)it.next();
      String childTypeId = this.getClassId((String)childDef.getQualifiedName());
      String generalizationId = this.getGeneralizationId(
        (String)classDef.getQualifiedName(),
        (String)childDef.getQualifiedName()
      );
      pw.write(indent, 0, nTabs); pw.write("<UML:Generalization xmi.id = '"); pw.write(generalizationId); pw.write("' isSpecification = 'false'>\n");
      pw.write(indent, 0, nTabs); pw.write("\t<UML:Generalization.child>\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Class xmi.idref = '"); pw.write(childTypeId); pw.write("'/>\n");
      pw.write(indent, 0, nTabs); pw.write("\t</UML:Generalization.child>\n");
      pw.write(indent, 0, nTabs); pw.write("\t<UML:Generalization.parent>\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Class xmi.idref = '"); pw.write(parentTypeId); pw.write("'/>\n");
      pw.write(indent, 0, nTabs); pw.write("\t</UML:Generalization.parent>\n");
      pw.write(indent, 0, nTabs); pw.write("</UML:Generalization>\n");
    }
  }
  
  //---------------------------------------------------------------------------
  public void writeStructureTypeHeader(
    ModelElement_1_0 structureTypeDef,
    boolean hasStructureFields
  ) throws ServiceException {
    String name = (String)structureTypeDef.getName();
    String classId = this.getClassId((String)structureTypeDef.getQualifiedName());
    String visibility = this.toXMIVisibility((String)structureTypeDef.objGetValue("visibility"));
    boolean isAbstract = ((Boolean)structureTypeDef.isAbstract()).booleanValue();
    
    pw.write(indent, 0, nTabs); pw.write("<UML:Class xmi.id = '"); pw.write(classId); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'\n"); 
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = '"); pw.print(isAbstract); pw.write("' isActive = 'false'>\n");

    // annotation
    if (!structureTypeDef.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)structureTypeDef.objGetValue("annotation")
        )
      );
    }
    
    pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.stereotype>\n");
    String structStereotypeId = this.getStereotypeId(Stereotypes.STRUCT, "Class");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Stereotype xmi.idref = '"); pw.write(structStereotypeId); pw.write("'/>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.stereotype>\n");
    pw.write(indent, 0, nTabs);
    
    // write an opening 'UML:Classifier.feature' tag only if the structure type
    // has structure fields, otherwise the Poseidon XMI import will crash
    // CR0002222
    if (hasStructureFields)
    {
      pw.write("\t<UML:Classifier.feature>\n");
    }
    else
    {
      pw.write("\t<UML:Classifier.feature/>\n");
    }
    nTabs = nTabs + 2;
  }
  
  //---------------------------------------------------------------------------
  public void writeStructureTypeFooter(
    ModelElement_1_0 structureTypeDef,
    boolean hasStructureFields
  ) {
    nTabs = nTabs - 2;
    if (hasStructureFields)
    {
      pw.write(indent, 0, nTabs); pw.write("\t</UML:Classifier.feature>\n");
    }
    pw.write(indent, 0, nTabs); pw.write("</UML:Class>\n");
    pw.flush();      
  }
  
  //---------------------------------------------------------------------------
  public void writeStructureField(
    ModelElement_1_0 attributeDef,
    ModelElement_1_0 typeDef,
    boolean referencedTypeIsPrimitive
  ) throws ServiceException {
    this.writeAttribute(attributeDef, false, false, typeDef, referencedTypeIsPrimitive);
  }
  
  //---------------------------------------------------------------------------
  public void writeClassHeader(
    ModelElement_1_0 classDef,
    List supertypeDefs,
    boolean hasStructuralFeatures
  ) throws ServiceException {
    String name = (String)classDef.getName();
    String classId = this.getClassId((String)classDef.getQualifiedName());
    String visibility = this.toXMIVisibility((String)classDef.objGetValue("visibility"));
    boolean isAbstract = ((Boolean)classDef.isAbstract()).booleanValue();

    pw.write(indent, 0, nTabs); pw.write("<UML:Class xmi.id = '"); pw.write(classId); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'\n"); 
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = '"); pw.print(isAbstract); pw.write("' isActive = 'false'>\n");

    // annotation
    if (!classDef.objGetList("annotation").isEmpty())
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)classDef.objGetValue("annotation")
        )
      );
    }
    
    for(
      Iterator it = classDef.objGetList("stereotype").iterator();
      it.hasNext();
    )
    {
      String stereotypeId = this.getStereotypeId((String)it.next(), "Class");
      pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.stereotype>\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Stereotype xmi.idref = '"); pw.write(stereotypeId); pw.write("'/>\n");
      pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.stereotype>\n");
    }

    for(
      Iterator it = supertypeDefs.iterator();
      it.hasNext();
    ) {
      ModelElement_1_0 parentDef = (ModelElement_1_0)it.next();
      String generalizationId = this.getGeneralizationId(
        (String)parentDef.getQualifiedName(),
        (String)classDef.getQualifiedName()
      );
      pw.write(indent, 0, nTabs); pw.write("\t<UML:GeneralizableElement.generalization>\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Generalization xmi.idref = '"); pw.write(generalizationId); pw.write("'/>\n");
      pw.write(indent, 0, nTabs); pw.write("\t</UML:GeneralizableElement.generalization>\n");
    }
    pw.write(indent, 0, nTabs);
    
    // write an opening 'UML:Classifier.feature' tag only if the class has 
    // structural features, otherwise the Poseidon XMI import will crash
    // CR0002222
    if (hasStructuralFeatures)
    {
      pw.write("\t<UML:Classifier.feature>\n");
    }
    else
    {
      pw.write("\t<UML:Classifier.feature/>\n");
    }
    nTabs = nTabs + 2;
  }
  
  //---------------------------------------------------------------------------
  public void writeClassFooter(
    ModelElement_1_0 classDef,
    boolean hasStructuralFeatures
  ) {
    nTabs = nTabs - 2;
    if (hasStructuralFeatures)
    {
      pw.write(indent, 0, nTabs); pw.write("\t</UML:Classifier.feature>\n");
    }
    pw.write(indent, 0, nTabs); pw.write("</UML:Class>\n");
    pw.flush();      
  }
  
  //---------------------------------------------------------------------------
  public void writeHeader(
  ) {
    pw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    pw.write("<!-- generated by openMDX XMI UML Model Interchange Writer -->\n");
    pw.write("<!--\n");
    pw.write(" Please note:\n");
    if (POSEIDON_XMI_DIALECT == this.xmiDialect)
    {
      pw.write(" This XMI contains extensions that are specific for Poseidon for UML:\n");
      pw.write(" - since Poseidon has no support for qualifiers, these are stored in the\n");
      pw.write("   association end name\n");
      pw.write("   (i.e. roleName [qualifier1: qualifierType1; qualifier2: qualifierType2])\n");
      pw.write(" - the tagged value 'exceptions' is used to list the exceptions that an\n");
      pw.write("   operation can throw\n");
    } else {
      pw.write(" This XMI contains extensions that are specific for MagicDraw:\n");
      pw.write(" - the tagged value 'exceptions' is used to list the exceptions that an\n");
      pw.write("   operation can throw\n");
    }
    pw.write("-->\n");
    pw.write("<XMI xmi.version = '1.2' xmlns:UML = 'org.omg.xmi.namespace.UML'>\n");
    pw.write("\t<XMI.header>\n");
    pw.write("\t\t<XMI.documentation>\n");
    pw.write("\t\t\t<XMI.exporter>openMDX XMI UML Model Interchange Writer</XMI.exporter>\n");
    pw.write("\t\t\t<XMI.exporterVersion>1.0</XMI.exporterVersion>\n");
    pw.write("\t\t</XMI.documentation>\n");
    pw.write("\t</XMI.header>\n");
    pw.write("\t<XMI.content>\n");
    pw.write("\t\t<UML:Model xmi.id = '"); pw.write(this.createId()); pw.write("' name = 'models' isSpecification = 'false'\n");
    pw.write("\t\t\tisRoot = 'false' isLeaf = 'false' isAbstract = 'false'>\n");
    pw.write("\t\t\t<UML:Namespace.ownedElement>\n");
    this.writeStereotype(Multiplicity.OPTIONAL.toString(), "Attribute");
    this.writeStereotype(Multiplicity.SINGLE_VALUE.toString(), "Attribute");
    this.writeStereotype(ModelHelper.UNBOUND, "Attribute");
    this.writeStereotype(Multiplicity.LIST.toString(), "Attribute");
    this.writeStereotype(Multiplicity.SET.toString(), "Attribute");
    this.writeStereotype(Multiplicity.SPARSEARRAY.toString(), "Attribute");
    this.writeStereotype(Multiplicity.STREAM.toString(), "Attribute");
    this.writeStereotype(Multiplicity.MAP.toString(), "Attribute");
    this.writeStereotype(Multiplicity.OPTIONAL.toString(), "Parameter");
    this.writeStereotype(Multiplicity.SINGLE_VALUE.toString(), "Parameter");
    this.writeStereotype(ModelHelper.UNBOUND, "Parameter");
    this.writeStereotype(Multiplicity.LIST.toString(), "Parameter");
    this.writeStereotype(Multiplicity.SET.toString(), "Parameter");
    this.writeStereotype(Multiplicity.SPARSEARRAY.toString(), "Parameter");
    this.writeStereotype(Multiplicity.STREAM.toString(), "Parameter");
    this.writeStereotype(Multiplicity.MAP.toString(), "Parameter");
    this.writeStereotype(Stereotypes.PRIMITIVE, "DataType");
    this.writeStereotype(Stereotypes.STRUCT, "Class");
    this.writeStereotype(Stereotypes.ALIAS, "Class");
    this.writeStereotype(Stereotypes.ROOT, "Class");
    this.writeStereotype(Stereotypes.ROLE, "Class");
    this.writeStereotype(Stereotypes.EXCEPTION, "Operation");
    this.writeTagDefinition("derived");
    this.writeTagDefinition("maxLength");
    this.writeTagDefinition("exceptions");
    this.writeTagDefinition("documentation");
  }
  
  //---------------------------------------------------------------------------
  public void writeFooter(
  ) {
    pw.write("\t\t\t</UML:Namespace.ownedElement>\n");
    pw.write("\t\t</UML:Model>\n");
    pw.write("\t</XMI.content>\n");
    pw.write("</XMI>\n");
    pw.flush();
  }

  //---------------------------------------------------------------------------
  private void writeStereotype(
    String stereotype,
    String baseClass
  ) {
    String id = this.getStereotypeId(stereotype, baseClass);
    pw.write("\t\t\t\t<UML:Stereotype xmi.id = '"); pw.write(id); pw.write("' name = '"); pw.write(stereotype); pw.write("'\n");
    pw.write("\t\t\t\t\t visibility = 'public' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'\n");
    pw.write("\t\t\t\t\tisAbstract = 'false'>\n");
    pw.write("\t\t\t\t\t<UML:Stereotype.baseClass>"); pw.write(baseClass); pw.write("</UML:Stereotype.baseClass>\n");
    pw.write("\t\t\t\t</UML:Stereotype>\n");
  }

  //---------------------------------------------------------------------------
  private void writeTagDefinition(
    String tag
  ) {
    pw.write("\t\t\t\t<UML:TagDefinition xmi.id = '"); pw.write(this.getTagDefinitionId(tag)); pw.write("' name = '"); pw.write(tag); pw.write("'\n");
    pw.write("\t\t\t\t\tisSpecification = 'false' tagType = 'String'>\n");
    pw.write("\t\t\t\t\t<UML:TagDefinition.multiplicity>\n");
    pw.write("\t\t\t\t\t\t<UML:Multiplicity xmi.id = '"); pw.write(this.createId()); pw.write("'>\n");
    pw.write("\t\t\t\t\t\t\t<UML:Multiplicity.range>\n");
    pw.write("\t\t\t\t\t\t\t\t<UML:MultiplicityRange xmi.id = '"); pw.write(this.createId()); pw.write("' lower = '1'\n");
    pw.write("\t\t\t\t\t\t\t\t\tupper = '1'/>\n");
    pw.write("\t\t\t\t\t\t\t</UML:Multiplicity.range>\n");
    pw.write("\t\t\t\t\t\t</UML:Multiplicity>\n");
    pw.write("\t\t\t\t\t</UML:TagDefinition.multiplicity>\n");
    pw.write("\t\t\t\t</UML:TagDefinition>\n");
  }

  //---------------------------------------------------------------------------
  private void writeTaggedValue(
    String tag,
    String dataValue
  ) {
    pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.taggedValue>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:TaggedValue xmi.id = '"); pw.write(this.createId()); pw.write("' isSpecification = 'false'>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t\t<UML:TaggedValue.dataValue>"); pw.write(dataValue); pw.write("</UML:TaggedValue.dataValue>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t\t<UML:TaggedValue.type>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t\t\t<UML:TagDefinition xmi.idref = '"); pw.write(this.getTagDefinitionId(tag)); pw.write("'/>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t\t</UML:TaggedValue.type>\n");
    pw.write(indent, 0, nTabs); pw.write("\t\t</UML:TaggedValue>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.taggedValue>\n");
  }
  
  //---------------------------------------------------------------------------
  private String toPoseidonAssociationEndName(
    String associationEndName,
    List qualifierNames,
    List qualifierTypes
  ) throws ServiceException {
    StringBuffer sb = new StringBuffer(associationEndName);
    if (!qualifierNames.isEmpty())
    {
      int nQualifiers = qualifierNames.size();
      sb.append(POSEIDON_LF);
      sb.append("[");
      for(
        int index = 0;
        index < nQualifiers;
        index++
      ) {
        String qualifierTypeName = (String)((ModelElement_1_0)qualifierTypes.get(index)).getQualifiedName();
        sb.append(qualifierNames.get(index));
        sb.append(":");
        sb.append(this.toMOFSyntax(qualifierTypeName));
        if (index < nQualifiers-1)
        {
          sb.append("; ");
        }
      }
      sb.append("]");
    }
    return sb.toString();  
  }
  
  //---------------------------------------------------------------------------
  private String toMOFSyntax(
    String qualifiedName
  ) {
    if(qualifiedName.length() == 0) {
      return new String();
    } else {
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < qualifiedName.length(); i++)
      {
        char ch = qualifiedName.charAt(i);
        if(':' == ch) { sb.append("::"); }
        else { sb.append(ch); }
      }
      return sb.toString();
    }
  }

  //---------------------------------------------------------------------------
  private void writeAssociationEndMultiplicity(
    String multiplicity
  ) {
    pw.write(indent, 0, nTabs); pw.write("<UML:Multiplicity xmi.id = '"); pw.write(this.createId()); pw.write("'>\n");
    pw.write(indent, 0, nTabs); pw.write("\t<UML:Multiplicity.range>\n");
    if ("0..1".equals(multiplicity))
    {
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:MultiplicityRange xmi.id = '"); pw.write(this.createId()); pw.write("' lower = '0'\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t\tupper = '1'/>\n");
    }
    else if ("1..1".equals(multiplicity))
    {
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:MultiplicityRange xmi.id = '"); pw.write(this.createId()); pw.write("' lower = '1'\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t\tupper = '1'/>\n");
    }
    else if ("1..n".equals(multiplicity))
    {
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:MultiplicityRange xmi.id = '"); pw.write(this.createId()); pw.write("' lower = '1'\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t\tupper = '-1'/>\n");
    }
    else
    {
      // 0..n
      pw.write(indent, 0, nTabs); pw.write("\t\t<UML:MultiplicityRange xmi.id = '"); pw.write(this.createId()); pw.write("' lower = '0'\n");
      pw.write(indent, 0, nTabs); pw.write("\t\t\tupper = '-1'/>\n");
    }
    pw.write(indent, 0, nTabs); pw.write("\t</UML:Multiplicity.range>\n");
    pw.write(indent, 0, nTabs); pw.write("</UML:Multiplicity>\n");
  }
  
  //---------------------------------------------------------------------------
  private String toHTMLString(
    String input
  ) {
      return XMLEncoder.encode(input);
  }

  //---------------------------------------------------------------------------
  private String toXMIVisibility(
    String mofVisibility
  ) {
    if (VisibilityKind.PRIVATE_VIS.equals(mofVisibility))
    {
      return "private";
    }
    else
    {
      return "public";
    }
  }

  //---------------------------------------------------------------------------
  private String toXMIAggregation(
    String mofAggregation
  ) {
    if (AggregationKind.COMPOSITE.equals(mofAggregation))
    {
      return "composite";
    }
    else if (AggregationKind.SHARED.equals(mofAggregation))
    {
      return "aggregate";
    }
    else
    {
      return "none";
    }
  }

  //---------------------------------------------------------------------------
  private String toXMIChangeability(
    boolean isChangeable
  ) {
    return (isChangeable ? "changeable" : "frozen"); 
  }
  
  //---------------------------------------------------------------------------
  private String toXMIParameterKind(
    String direction
  ) {
    if ("return_dir".equals(direction))
    {
      return "return";
    }
    else if ("out_dir".equals(direction))
    {
      return "out";
    }
    else if ("inout_dir".equals(direction))
    {
      return "inout";
    }
    else
    {
      return "in";
    }
  }
  
  //---------------------------------------------------------------------------
  private String createId(
  ) {
    return "_" + String.valueOf(id++);
  }

  //---------------------------------------------------------------------------
  private String getStereotypeId(
    String stereotype,
    String baseClass
  ) {
    String key = stereotype+baseClass;
    if (!this.stereotypeIds.containsKey(key))
    {
      this.stereotypeIds.put(
        key,
        this.createId()
      );
    }
    return (String)this.stereotypeIds.get(key);
  }

  //---------------------------------------------------------------------------
  private String getDataTypeId(
    String qualifiedName
  ) {
    if (!this.datatypeIds.containsKey(qualifiedName))
    {
      this.datatypeIds.put(
        qualifiedName,
        this.createId()
      );
    }
    return (String)this.datatypeIds.get(qualifiedName);
  }

  //---------------------------------------------------------------------------
  private String getClassId(
    String qualifiedName
  ) {
    if (!this.classIds.containsKey(qualifiedName))
    {
      this.classIds.put(
        qualifiedName,
        this.createId()
      );
    }
    return (String)this.classIds.get(qualifiedName);
  }

  //---------------------------------------------------------------------------
  private String getTagDefinitionId(
    String tag
  ) {
    if (!this.tagDefinitionIds.containsKey(tag))
    {
      this.tagDefinitionIds.put(
        tag,
        this.createId()
      );
    }
    return (String)this.tagDefinitionIds.get(tag);
  }

  //---------------------------------------------------------------------------
  private String getGeneralizationId(
    String parentQualifiedName,
    String childQualifiedName
  ) {
    String key = parentQualifiedName + "-" + childQualifiedName;
    if (!this.generalizationIds.containsKey(key))
    {
      this.generalizationIds.put(
        key,
        this.createId()
      );
    }
    return (String)this.generalizationIds.get(key);
  }

  //---------------------------------------------------------------------------  
  // Variables
  //---------------------------------------------------------------------------  
  private final PrintWriter pw;
  private final byte xmiDialect;
  private Map stereotypeIds = null;
  private Map datatypeIds = null;
  private Map classIds = null;
  private Map generalizationIds = null;
  private Map tagDefinitionIds = null;
  private int id = 0;
  private static int nTabs = 4;
  private static final String POSEIDON_LF = "&#10;";
  private static char[] indent = {'\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t'};

  public static final byte POSEIDON_XMI_DIALECT = 1;
  public static final byte MAGICDRAW_XMI_DIALECT = 2;
}

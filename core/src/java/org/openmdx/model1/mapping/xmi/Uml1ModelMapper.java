/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Uml1ModelMapper.java,v 1.3 2008/06/16 13:30:44 hburger Exp $
 * Description: write XML schema (XSD)
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/16 13:30:44 $
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

package org.openmdx.model1.mapping.xmi;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.omg.model1.code.VisibilityKind;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.Stereotypes;

/**
 * This class writes MOF model elements to a given PrintStream using the OMG's
 * XML Metadata Interchange (XMI) format. The XMI format is based on the UML
 * mapping and follows the UML Profile for MOF as closely as possible for the
 * openMDX project. 
 */
@SuppressWarnings("unchecked")
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
  ) {
    String name = (String)exceptionDef.values("name").get(0);
    
    pw.write(indent, 0, nTabs); pw.write("<UML:Operation xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = 'public' isSpecification = 'false' ownerScope = 'instance'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisQuery = 'false' concurrency = 'sequential' isRoot = 'false' isLeaf = 'false'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = 'false'>\n");

    // annotation
    if (exceptionDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)exceptionDef.values("annotation").get(0)
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
  ) {
    String name = (String)operationDef.values("name").get(0);
    boolean isQuery = ((Boolean)operationDef.values("isQuery").get(0)).booleanValue();
    String visibility = this.toXMIVisibility((String)operationDef.values("visibility").get(0));

    pw.write(indent, 0, nTabs); pw.write("<UML:Operation xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' ownerScope = 'instance'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisQuery = '"); pw.print(isQuery); pw.write("' concurrency = 'sequential' isRoot = 'false' isLeaf = 'false'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = 'false'>\n");

    // annotation
    if (operationDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)operationDef.values("annotation").get(0)
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
        sb.append(this.toMOFSyntax((String)((ModelElement_1_0)exceptions.get(index)).values("qualifiedName").get(0)));
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
  ) {
    String name = (String)parameterDef.values("name").get(0);
    String typeQualifiedName = (String)parameterTypeDef.values("qualifiedName").get(0);
    String direction = this.toXMIParameterKind((String)parameterDef.values("direction").get(0));
    String multiplicity = (String)parameterDef.values("multiplicity").get(0);

    pw.write(indent, 0, nTabs); pw.write("<UML:Parameter xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisSpecification = 'false' kind = '"); pw.write(direction); pw.write("'>\n");

    // annotation
    if (parameterDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)parameterDef.values("annotation").get(0)
        )
      );
    }
    
    if (!Multiplicities.SINGLE_VALUE.equals(multiplicity))
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
  ) {
    String name = (String)associationDef.values("name").get(0);
    boolean isAbstract = ((Boolean)associationDef.values("isAbstract").get(0)).booleanValue();
    boolean isDerived = ((Boolean)associationDef.values("isDerived").get(0)).booleanValue();
    
    pw.write(indent, 0, nTabs); pw.write("<UML:Association xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisSpecification = 'false' isRoot = 'false' isLeaf = 'false' isAbstract = '"); pw.print(isAbstract); pw.write("'>\n");

    // annotation
    if (associationDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)associationDef.values("annotation").get(0)
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
  ) {
    boolean assEnd1IsNavigable = ((Boolean)associationEnd1Def.values("isNavigable").get(0)).booleanValue();
    boolean assEnd1IsChangeable = ((Boolean)associationEnd1Def.values("isChangeable").get(0)).booleanValue();
    String assEnd1TypeQualifiedName = (String)associationEnd1TypeDef.values("qualifiedName").get(0);
    String assEnd1Aggregation = (String)associationEnd1Def.values("aggregation").get(0);

    boolean assEnd2IsNavigable = ((Boolean)associationEnd2Def.values("isNavigable").get(0)).booleanValue();
    boolean assEnd2IsChangeable = ((Boolean)associationEnd2Def.values("isChangeable").get(0)).booleanValue();
    String assEnd2TypeQualifiedName = (String)associationEnd2TypeDef.values("qualifiedName").get(0);
    String assEnd2Aggregation = (String)associationEnd2Def.values("aggregation").get(0);
    
    // Note:
    // Aggregation and qualifier assignments must be changed to comply.
    
    String assEnd1Name;
    if (POSEIDON_XMI_DIALECT == this.xmiDialect)
    {
      // use Poseidon specific association end name (not XMI/UML compliant)
      // which includes qualfiers beside the association end name
      assEnd1Name = this.toPoseidonAssociationEndName(
        (String)associationEnd1Def.values("name").get(0),
        associationEnd2Def.values("qualifierName"),
        associationEnd2QualifierTypes
      );
    }
    else
    {
      // use XMI/UML compliant association end name
      assEnd1Name = (String)associationEnd1Def.values("name").get(0);
    }
    pw.write(indent, 0, nTabs); pw.write("<UML:AssociationEnd xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(assEnd1Name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = 'public' isSpecification = 'false' isNavigable = '"); pw.print(assEnd1IsNavigable); pw.write("' ordering = 'unordered'\n");
    pw.write(indent, 0, nTabs); pw.write("\taggregation = '"); pw.write(this.toXMIAggregation(assEnd2Aggregation)); pw.write("' targetScope = 'instance' changeability = '"); pw.write(this.toXMIChangeability(assEnd1IsChangeable)); pw.write("'>\n");

    // annotation
    if (associationEnd1Def.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)associationEnd1Def.values("annotation").get(0)
        )
      );
    }
    
    pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.multiplicity>\n");
    nTabs = nTabs + 2;
    this.writeAssociationEndMultiplicity((String)associationEnd1Def.values("multiplicity").get(0));
    nTabs = nTabs - 2;
    pw.write(indent, 0, nTabs); pw.write("\t</UML:AssociationEnd.multiplicity>\n");

    int nAssEnd1Qualifiers = associationEnd1Def.values("qualifierName").size();
    if (MAGICDRAW_XMI_DIALECT == this.xmiDialect && nAssEnd1Qualifiers > 0)
    {
      pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.qualifier>\n");
      for(
        int index = 0;
        index < nAssEnd1Qualifiers;
        index++
      ) {
        String qualifierName = (String)associationEnd1Def.values("qualifierName").get(index);
        String qualifierTypeName = (String)((ModelElement_1_0)associationEnd1QualifierTypes.get(index)).values("qualifiedName").get(0);
        pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Attribute xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(qualifierName); pw.write("'\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\tvisibility = 'public' isSpecification = 'false' ownerScope = 'instance'\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\tchangeability = 'changeable'>\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\t<UML:StructuralFeature.type>\n");
        if (((ModelElement_1_0)associationEnd1QualifierTypes.get(index)).values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PRIMITIVE_TYPE))
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
        (String)associationEnd2Def.values("name").get(0),
        associationEnd1Def.values("qualifierName"),
        associationEnd1QualifierTypes
      );
    }
    else
    {
      // use XMI/UML compliant association end name
      assEnd2Name = (String)associationEnd2Def.values("name").get(0);
    }
    pw.write(indent, 0, nTabs); pw.write("<UML:AssociationEnd xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(assEnd2Name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = 'public' isSpecification = 'false' isNavigable = '"); pw.print(assEnd2IsNavigable); pw.write("' ordering = 'unordered'\n");
    pw.write(indent, 0, nTabs); pw.write("\taggregation = '"); pw.write(this.toXMIAggregation(assEnd1Aggregation)); pw.write("' targetScope = 'instance' changeability = '"); pw.write(this.toXMIChangeability(assEnd2IsChangeable)); pw.write("'>\n");

    // annotation
    if (associationEnd2Def.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)associationEnd2Def.values("annotation").get(0)
        )
      );
    }
    
    pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.multiplicity>\n");
    nTabs = nTabs + 2;
    this.writeAssociationEndMultiplicity((String)associationEnd2Def.values("multiplicity").get(0));
    nTabs = nTabs - 2;
    pw.write(indent, 0, nTabs); pw.write("\t</UML:AssociationEnd.multiplicity>\n");

    int nAssEnd2Qualifiers = associationEnd2Def.values("qualifierName").size();
    if (MAGICDRAW_XMI_DIALECT == this.xmiDialect && nAssEnd2Qualifiers > 0)
    {
      pw.write(indent, 0, nTabs); pw.write("\t<UML:AssociationEnd.qualifier>\n");
      for(
        int index = 0;
        index < nAssEnd2Qualifiers;
        index++
      ) {
        String qualifierName = (String)associationEnd2Def.values("qualifierName").get(index);
        String qualifierTypeName = (String)((ModelElement_1_0)associationEnd2QualifierTypes.get(index)).values("qualifiedName").get(0);
        pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Attribute xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(qualifierName); pw.write("'\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\tvisibility = 'public' isSpecification = 'false' ownerScope = 'instance'\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\tchangeability = 'changeable'>\n");
        pw.write(indent, 0, nTabs); pw.write("\t\t\t<UML:StructuralFeature.type>\n");
        if (((ModelElement_1_0)associationEnd2QualifierTypes.get(index)).values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PRIMITIVE_TYPE))
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
  ) {
    String name = (String)primitiveTypeDef.values("name").get(0);
    String visibility = this.toXMIVisibility((String)primitiveTypeDef.values("visibility").get(0));
    boolean isAbstract = ((Boolean)primitiveTypeDef.values("isAbstract").get(0)).booleanValue();
    String typeId = this.getDataTypeId((String)primitiveTypeDef.values("qualifiedName").get(0));

    pw.write(indent, 0, nTabs); pw.write("<UML:DataType xmi.id = '"); pw.write(typeId); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = '"); pw.print(isAbstract); pw.write("'>\n");

    // annotation
    if (primitiveTypeDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)primitiveTypeDef.values("annotation").get(0)
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
  ) {
    String name = (String)attributeDef.values("name").get(0);
    String typeQualifiedName = (String)typeDef.values("qualifiedName").get(0);
    String visibility = this.toXMIVisibility((String)attributeDef.values("visibility").get(0));
    String changeability = this.toXMIChangeability(isChangeable);
    String multiplicity = (String)attributeDef.values("multiplicity").get(0);
    int maxLength = ((Number)attributeDef.values("maxLength").get(0)).intValue();
    
    pw.write(indent, 0, nTabs); pw.write("<UML:Attribute xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' ownerScope = 'instance'\n");
    pw.write(indent, 0, nTabs); pw.write("\tchangeability = '"); pw.write(changeability); pw.write("'>\n");
    
    // annotation
    if (attributeDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)attributeDef.values("annotation").get(0)
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
    if (!Multiplicities.SINGLE_VALUE.equals(multiplicity))
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
  ) {
    String name = (String)aliasTypeDef.values("name").get(0);
    String qualifiedName = (String)aliasTypeDef.values("qualifiedName").get(0);
    String visibility = this.toXMIVisibility((String)aliasTypeDef.values("visibility").get(0));
    boolean isAbstract = ((Boolean)aliasTypeDef.values("isAbstract").get(0)).booleanValue();
    String typeQualifiedName = (String)typeDef.values("qualifiedName").get(0);
    String classId = this.getClassId(qualifiedName);

    pw.write(indent, 0, nTabs); pw.write("<UML:Class xmi.id = '"); pw.write(classId); pw.write("' name = '"); pw.write(name); pw.write("' visibility = '"); pw.write(visibility); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisSpecification = 'false' isRoot = 'false' isLeaf = 'false' isAbstract = '"); pw.print(isAbstract); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisActive = 'false'>\n");
    pw.write(indent, 0, nTabs); pw.write("\t<UML:ModelElement.stereotype>\n");
    String aliasStereotypeId = this.getStereotypeId(Stereotypes.ALIAS, "Class");
    pw.write(indent, 0, nTabs); pw.write("\t\t<UML:Stereotype xmi.idref = '"); pw.write(aliasStereotypeId); pw.write("'/>\n");
    pw.write(indent, 0, nTabs); pw.write("\t</UML:ModelElement.stereotype>\n");

    // annotation
    if (aliasTypeDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)aliasTypeDef.values("annotation").get(0)
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
  ) {
    pw.write(indent, 0, nTabs); pw.write("<UML:Package xmi.id = '"); pw.write(this.createId()); pw.write("' name = '"); pw.write((String)packageDef.values("name").get(0)); pw.write("' visibility = 'public'\n");
    pw.write(indent, 0, nTabs); pw.write("\tisSpecification = 'false' isRoot = 'false' isLeaf = 'false' isAbstract = 'false'>\n");   
    pw.write(indent, 0, nTabs); pw.write("\t<UML:Namespace.ownedElement>\n");

    // annotation
    if (packageDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)packageDef.values("annotation").get(0)
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
  ) {
    String parentTypeId = this.getClassId((String)classDef.values("qualifiedName").get(0));
    for(
      Iterator it = subtypeDefs.iterator();
      it.hasNext();
    ) {
      ModelElement_1_0 childDef = (ModelElement_1_0)it.next();
      String childTypeId = this.getClassId((String)childDef.values("qualifiedName").get(0));
      String generalizationId = this.getGeneralizationId(
        (String)classDef.values("qualifiedName").get(0),
        (String)childDef.values("qualifiedName").get(0)
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
  ) {
    String name = (String)structureTypeDef.values("name").get(0);
    String classId = this.getClassId((String)structureTypeDef.values("qualifiedName").get(0));
    String visibility = this.toXMIVisibility((String)structureTypeDef.values("visibility").get(0));
    boolean isAbstract = ((Boolean)structureTypeDef.values("isAbstract").get(0)).booleanValue();
    
    pw.write(indent, 0, nTabs); pw.write("<UML:Class xmi.id = '"); pw.write(classId); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'\n"); 
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = '"); pw.print(isAbstract); pw.write("' isActive = 'false'>\n");

    // annotation
    if (structureTypeDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)structureTypeDef.values("annotation").get(0)
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
  ) {
    this.writeAttribute(attributeDef, false, false, typeDef, referencedTypeIsPrimitive);
  }
  
  //---------------------------------------------------------------------------
  public void writeClassHeader(
    ModelElement_1_0 classDef,
    List supertypeDefs,
    boolean hasStructuralFeatures
  ) {
    String name = (String)classDef.values("name").get(0);
    String classId = this.getClassId((String)classDef.values("qualifiedName").get(0));
    String visibility = this.toXMIVisibility((String)classDef.values("visibility").get(0));
    boolean isAbstract = ((Boolean)classDef.values("isAbstract").get(0)).booleanValue();

    pw.write(indent, 0, nTabs); pw.write("<UML:Class xmi.id = '"); pw.write(classId); pw.write("' name = '"); pw.write(name); pw.write("'\n");
    pw.write(indent, 0, nTabs); pw.write("\tvisibility = '"); pw.write(visibility); pw.write("' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'\n"); 
    pw.write(indent, 0, nTabs); pw.write("\tisAbstract = '"); pw.print(isAbstract); pw.write("' isActive = 'false'>\n");

    // annotation
    if (classDef.values("annotation").size() != 0)
    {
      this.writeTaggedValue(
        "documentation",
        this.toHTMLString(
          (String)classDef.values("annotation").get(0)
        )
      );
    }
    
    for(
      Iterator it = classDef.values("stereotype").iterator();
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
        (String)parentDef.values("qualifiedName").get(0),
        (String)classDef.values("qualifiedName").get(0)
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
    this.writeStereotype(Multiplicities.OPTIONAL_VALUE, "Attribute");
    this.writeStereotype(Multiplicities.SINGLE_VALUE, "Attribute");
    this.writeStereotype(Multiplicities.MULTI_VALUE, "Attribute");
    this.writeStereotype(Multiplicities.LIST, "Attribute");
    this.writeStereotype(Multiplicities.SET, "Attribute");
    this.writeStereotype(Multiplicities.SPARSEARRAY, "Attribute");
    this.writeStereotype(Multiplicities.STREAM, "Attribute");
    this.writeStereotype(Multiplicities.MAP, "Attribute");
    this.writeStereotype(Multiplicities.OPTIONAL_VALUE, "Parameter");
    this.writeStereotype(Multiplicities.SINGLE_VALUE, "Parameter");
    this.writeStereotype(Multiplicities.MULTI_VALUE, "Parameter");
    this.writeStereotype(Multiplicities.LIST, "Parameter");
    this.writeStereotype(Multiplicities.SET, "Parameter");
    this.writeStereotype(Multiplicities.SPARSEARRAY, "Parameter");
    this.writeStereotype(Multiplicities.STREAM, "Parameter");
    this.writeStereotype(Multiplicities.MAP, "Parameter");
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
  ) {
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
        String qualifierTypeName = (String)((ModelElement_1_0)qualifierTypes.get(index)).values("qualifiedName").get(0);
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
    if(input.length() == 0) {
      return new String();
    } else {
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < input.length(); i++)
      {
        char ch = input.charAt(i);
        
        if('<' == ch) { sb.append("&lt;"); }
        else if('>' == ch) { sb.append("&gt;"); }
        else if('\'' == ch) { sb.append("&apos;"); }
        else if('"' == ch) { sb.append("&quot;"); }
        else if('&' == ch) { sb.append("&amp;"); }
        else if(8217 == ch) { sb.append("&#8217;"); }
        else if(228 == ch) { sb.append("&#228;"); }
        else if(246 == ch) { sb.append("&#246;"); }
        else if(252 == ch) { sb.append("&#252;"); }
        else if(196 == ch) { sb.append("&#196;"); }
        else if(214 == ch) { sb.append("&#214;"); }
        else if(220 == ch) { sb.append("&#220;"); }
        else if (ch >= 0x007F)
        {
        	// ignore non UTF-8 character because this character can cause 
        	// Poseidon to crash when importing an XMI file
        	SysLog.warning("ignoring non UTF-8 character &#" + (long)ch + "; in string \"" + input + "\"");
        }
        else { sb.append(ch); }
      }
      return sb.toString();
    }
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

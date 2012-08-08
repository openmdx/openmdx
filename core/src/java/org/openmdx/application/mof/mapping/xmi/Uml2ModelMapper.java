/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Uml2ModelMapper.java,v 1.4 2009/11/05 17:46:23 hburger Exp $
 * Description: Uml2ModelMapper
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/05 17:46:23 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.Stereotypes;
import org.openmdx.base.text.conversion.HtmlEncoder;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;

@SuppressWarnings("unchecked")
public class Uml2ModelMapper {

    //---------------------------------------------------------------------------
    public Uml2ModelMapper(
        PrintWriter printWriter,
        short xmiFormat
    ) {
        this.pw = new PrintWriter(printWriter);
        this.classifierIds = new HashMap();
        this.associationEndIds = new HashMap();
        this.associationIds = new HashMap();
    }

    //---------------------------------------------------------------------------
    public void mapOperationBegin(
        ModelElement_1_0 operationDef,
        ModelElement_1_0 returnType
    ) throws ServiceException {
        String name = (String)operationDef.objGetValue("name");
        Boolean isQuery = operationDef.objGetList("isQuery").isEmpty()
            ? null
            : (Boolean)operationDef.objGetValue("isQuery");
        String returnTypeQualifiedName = returnType == null
            ? null
            : (String)returnType.objGetValue("qualifiedName");
        
        pw.write(indent, 0, nTabs); 
        pw.print("<ownedOperation");
        this.printAttribute("xmi:id", this.createId()); 
        this.printAttribute("name", name); 
        if(returnTypeQualifiedName != null) {
            this.printAttribute("type", this.getClassifierId(returnTypeQualifiedName));
        }
        if(isQuery != null) {
            this.printAttribute("isQuery", isQuery.toString());
        }
        pw.println(">");
        
        // Annotation
        if(!operationDef.objGetList("annotation").isEmpty()) {            
            pw.write(indent, 0, nTabs);
            pw.print("\t<ownedComment");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("body", this.toHTMLString((String)operationDef.objGetValue("annotation")));
            pw.println("/>");
        }
        
        // Stereotype
        if(operationDef.objGetList("stereotype").isEmpty()) {
            pw.print("\t<eAnnotations");
            this.printAttribute("xmi:id", this.createId()); 
            this.printAttribute("source", "keywords"); 
            pw.println(">");
            pw.write(indent, 0, nTabs); 
            pw.print("\t\t<details");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("key", (String)operationDef.objGetValue("stereotype"));
            pw.println("/>");        
            pw.write(indent, 0, nTabs); 
            pw.println("\t</eAnnotations>");
        }
        
        nTabs += 1;
    }

    //---------------------------------------------------------------------------
    public void mapOperationEnd(
        ModelElement_1_0 operationDef
    ) {
        nTabs -= 1;
        pw.write(indent, 0, nTabs); 
        pw.println("</ownedOperation>");
        pw.flush();      
    }

    //---------------------------------------------------------------------------
    public void mapParameter(
        ModelElement_1_0 parameterDef,
        ModelElement_1_0 parameterTypeDef
    ) throws ServiceException {
        String name = (String)parameterDef.objGetValue("name");
        String typeQualifiedName = (String)parameterTypeDef.objGetValue("qualifiedName");
        String direction = this.toXMIParameterKind((String)parameterDef.objGetValue("direction"));

        if("return".equals(direction)) {
            pw.write(indent, 0, nTabs); 
            pw.print("<returnResult");
            this.printAttribute("xmi:id", this.createId()); 
            this.printAttribute("type", this.getClassifierId(typeQualifiedName));
            this.printAttribute("direction", direction);
            pw.println("/>");            
        }
        else {
            pw.write(indent, 0, nTabs); 
            pw.print("<ownedParameter");
            this.printAttribute("xmi:id", this.createId()); 
            this.printAttribute("name", name); 
            this.printAttribute("type", this.getClassifierId(typeQualifiedName));
            this.printAttribute("direction", direction);
            pw.println("/>");
        }
        
        pw.flush();      
    }

    //---------------------------------------------------------------------------
    public void mapAssociationBegin(
        ModelElement_1_0 associationDef,
        ModelElement_1_0 end1,
        ModelElement_1_0 end2
    ) throws ServiceException {
        String name = (String)associationDef.objGetValue("name");
        String qualifiedName = (String)associationDef.objGetValue("qualifiedName");
        boolean isDerived = ((Boolean)associationDef.objGetValue("isDerived")).booleanValue();
        String end1QualifiedName = (String)end1.objGetValue("qualifiedName");
        String end2QualifiedName = (String)end2.objGetValue("qualifiedName");
        String end1Id = this.getAssociationEndId(end1QualifiedName);
        String end2Id = this.getAssociationEndId(end2QualifiedName);
        
        pw.write(indent, 0, nTabs); 
        pw.print("<ownedMember");
        this.printAttribute("xmi:type", "uml:Association");
        this.printAttribute("xmi:id", this.getAssociationId(qualifiedName)); 
        this.printAttribute("name", name);
        this.printAttribute("isDerived", Boolean.toString(isDerived));
        this.printAttribute("memberEnd", end1Id + " " + end2Id);
        pw.println(">");

        // Annotation
        if(!associationDef.objGetList("annotation").isEmpty()) {            
            pw.write(indent, 0, nTabs);
            pw.print("\t<ownedComment");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("body", this.toHTMLString((String)associationDef.objGetValue("annotation")));
            pw.println("/>");
        }
        
        nTabs += 1;
    }

    //---------------------------------------------------------------------------
    public void mapAssociationEnd(
        ModelElement_1_0 associationDef
    ) {
        nTabs -= 1;
        pw.write(indent, 0, nTabs); 
        pw.println("</ownedMember>");
        pw.flush();      
    }

    //---------------------------------------------------------------------------
    public void mapAssociationEnd(
        ModelElement_1_0 associationDef,        
        ModelElement_1_0 associationEndDef,
        ModelElement_1_0 associationEndTypeDef,
        List qualifierTypes,
        boolean mapAsAttribute
    ) throws ServiceException {
        String name = (String)associationEndDef.objGetValue("name");
        String qualifiedName = (String)associationEndDef.objGetValue("qualifiedName");
        boolean isChangeable = ((Boolean)associationEndDef.objGetValue("isChangeable")).booleanValue();
        boolean isDerived = ((Boolean)associationDef.objGetValue("isDerived")).booleanValue();
        String multiplicity = (String)associationEndDef.objGetValue("multiplicity");        
        String qualifiedTypeName = (String)associationEndTypeDef.objGetValue("qualifiedName");
        String aggregation = (String)associationEndDef.objGetValue("aggregation");
        String associationQualifiedName = (String)associationDef.objGetValue("qualifiedName");        
        
        if(mapAsAttribute) {
            pw.write(indent, 0, nTabs); 
            pw.print("<ownedAttribute");
            this.printAttribute("xmi:id", this.getAssociationEndId(qualifiedName));
            this.printAttribute("name", name);
            this.printAttribute("type", this.getClassifierId(qualifiedTypeName));
            this.printAttribute("isUnique", Boolean.toString(false));
            this.printAttribute("isReadOnly", Boolean.toString(!isChangeable));
            this.printAttribute("isDerived", Boolean.toString(isDerived));
            this.printAttribute("association", this.getAssociationId(associationQualifiedName));
            if(!AggregationKind.NONE.equals(aggregation)) {
                this.printAttribute("aggregation", aggregation);
            }
            pw.println(">");
            
            // Multiplicity
            this.mapMultiplicity(multiplicity);
            
            // Qualifier
            if(!associationEndDef.objGetList("qualifierName").isEmpty()) {
                String qualifierName = (String)associationEndDef.objGetValue("qualifierName");
                ModelElement_1_0 qualifierType = (ModelElement_1_0)qualifierTypes.get(0);
                String qualifierTypeName = (String)qualifierType.objGetValue("qualifiedName");
                
                pw.write(indent, 0, nTabs);
                pw.print("\t<qualifier");
                this.printAttribute("xmi:id", this.createId());
                this.printAttribute("name", qualifierName);
                this.printAttribute("type", this.getClassifierId(qualifierTypeName));
                pw.println("/>");                                
            }
            pw.write(indent, 0, nTabs); 
            pw.println("</ownedAttribute>");            
        }
        else {
            pw.write(indent, 0, nTabs); 
            pw.print("<ownedEnd");
            this.printAttribute("xmi:id", this.getAssociationEndId(qualifiedName));
            this.printAttribute("name", name);
            this.printAttribute("type", this.getClassifierId(qualifiedTypeName));
            this.printAttribute("isUnique", Boolean.toString(false));
            this.printAttribute("association", this.getAssociationId(associationQualifiedName));
            pw.println(">");
            
            // Multiplicity
            this.mapMultiplicity(multiplicity);
            
            pw.write(indent, 0, nTabs); 
            pw.println("</ownedEnd>");
        }
        
        pw.flush();
    }

    //---------------------------------------------------------------------------
    public void mapPrimitiveType(
        ModelElement_1_0 primitiveTypeDef
    ) throws ServiceException {
        String name = (String)primitiveTypeDef.objGetValue("name");
        String qualifiedName = (String)primitiveTypeDef.objGetValue("qualifiedName");

        pw.write(indent, 0, nTabs);
        pw.print("<ownedMember");
        this.printAttribute("xmi:id", this.getClassifierId(qualifiedName));
        this.printAttribute("xmi:type", "uml:PrimitiveType");
        this.printAttribute("name", name);
        pw.println("/>");
        pw.flush();      
    }

    //---------------------------------------------------------------------------
    private void mapMultiplicity(
        String multiplicity
    ) {
        // Multiplicity
        if(Multiplicities.SINGLE_VALUE.equals(multiplicity)) {
            // Upper value
            pw.write(indent, 0, nTabs);
            pw.print("\t<upperValue");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("xmi:type", "uml:LiteralUnlimitedNatural");
            this.printAttribute("value", "1");
            pw.println("/>");
            
            // Lower value
            pw.write(indent, 0, nTabs);
            pw.print("\t<lowerValue");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("xmi:type", "uml:LiteralInteger");
            this.printAttribute("value", "1");
            pw.println("/>");                        
        }
        else if(Multiplicities.OPTIONAL_VALUE.equals(multiplicity)) {
            // Upper value
            pw.write(indent, 0, nTabs);
            pw.print("\t<upperValue");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("xmi:type", "uml:LiteralUnlimitedNatural");
            this.printAttribute("value", "1");
            pw.println("/>");
            
            // Lower value
            pw.write(indent, 0, nTabs);
            pw.print("\t<lowerValue");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("xmi:type", "uml:LiteralInteger");
            pw.println("/>");                        
        }
        else if(Multiplicities.MULTI_VALUE.equals(multiplicity)) {
            // Upper value
            pw.write(indent, 0, nTabs);
            pw.print("\t<upperValue");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("xmi:type", "uml:LiteralUnlimitedNatural");
            this.printAttribute("value", "-1");
            pw.println("/>");
            
            // Lower value
            pw.write(indent, 0, nTabs);
            pw.print("\t<lowerValue");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("xmi:type", "uml:LiteralInteger");
            pw.println("/>");                        
        }
        else {
            pw.write(indent, 0, nTabs); 
            pw.print("\t<eAnnotations");
            this.printAttribute("xmi:id", this.createId()); 
            this.printAttribute("source", "keywords"); 
            pw.println(">");
            pw.write(indent, 0, nTabs); 
            pw.print("\t\t<details");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("key", multiplicity);
            pw.println("/>");        
            pw.write(indent, 0, nTabs); 
            pw.println("\t</eAnnotations>");
        }                
    }
    
    //---------------------------------------------------------------------------
    public void mapAttribute(
        ModelElement_1_0 attributeDef,
        boolean isDerived,
        boolean isChangeable,
        ModelElement_1_0 typeDef,
        boolean referencedTypeIsPrimitive
    ) throws ServiceException {
        String name = (String)attributeDef.objGetValue("name");
        String typeQualifiedName = (String)typeDef.objGetValue("qualifiedName");
        String multiplicity = (String)attributeDef.objGetValue("multiplicity");

        pw.write(indent, 0, nTabs); 
        pw.print("<ownedAttribute");
        this.printAttribute("xmi:id", this.createId()); 
        this.printAttribute("name", name);
        this.printAttribute("type", this.getClassifierId(typeQualifiedName));
        this.printAttribute("isReadOnly", Boolean.toString(!isChangeable));
        this.printAttribute("isDerived", Boolean.toString(isDerived));
        pw.println(">");

        // Annotation
        if(!attributeDef.objGetList("annotation").isEmpty()) {            
            pw.write(indent, 0, nTabs);
            pw.print("\t<ownedComment");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("body", this.toHTMLString((String)attributeDef.objGetValue("annotation")));
            pw.println("/>");
        }
                
        // Multiplicity
        this.mapMultiplicity(multiplicity);
        
        pw.write(indent, 0, nTabs); 
        pw.println("</ownedAttribute>");
        pw.flush();      
    }

    //---------------------------------------------------------------------------
    public void mapAliasType(
        ModelElement_1_0 aliasTypeDef,
        ModelElement_1_0 typeDef,
        boolean referencedTypeIsPrimitive
    ) throws ServiceException {
        String name = (String)aliasTypeDef.objGetValue("name");
        String qualifiedName = (String)aliasTypeDef.objGetValue("qualifiedName");
        String typeQualifiedName = (String)typeDef.objGetValue("qualifiedName");

        pw.write(indent, 0, nTabs); 
        pw.print("<ownedMember");
        this.printAttribute("xmi:id", this.getClassifierId(qualifiedName));
        this.printAttribute("xmi:type", "uml:Class");
        this.printAttribute("name", name);
        pw.println(">");
        
        pw.write(indent, 0, nTabs); 
        pw.print("\t<eAnnotations");
        this.printAttribute("xmi:id", this.createId());
        this.printAttribute("source", "keywords");
        pw.println(">");
        
        pw.write(indent, 0, nTabs); 
        pw.print("\t\t<details");
        this.printAttribute("xmi:id", this.createId());
        this.printAttribute("key", "alias");
        pw.println("/>");
        
        pw.write(indent, 0, nTabs); 
        pw.println("\t</eAnnotations>");
        
        pw.write(indent, 0, nTabs); 
        pw.print("\t<ownedAttribute");
        this.printAttribute("xmi:id", this.createId());
        this.printAttribute("name", this.toMOFSyntax(typeQualifiedName));
        pw.println("/>");
        
        pw.write(indent, 0, nTabs); 
        pw.println("</ownedMember>");
        
        pw.flush();      
    }

    //---------------------------------------------------------------------------
    public void mapPackageBegin(
        ModelElement_1_0 packageDef
    ) throws ServiceException {
        pw.write(indent, 0, nTabs);
        pw.write("<ownedMember");
        this.printAttribute("xmi:type", "uml:Package");
        this.printAttribute("xmi:id", this.createId());
        this.printAttribute("name", (String)packageDef.objGetValue("name")); 
        pw.println(">");
        nTabs += 1;
    }

    //---------------------------------------------------------------------------
    public void mapPackageEnd(
        ModelElement_1_0 packageDef
    ) {
        nTabs -= 1;
        pw.write(indent, 0, nTabs); 
        pw.println("</ownedMember>");
        pw.flush();      
    }

    //---------------------------------------------------------------------------
    private void printAttribute(
        String attributeName,
        String attributeValue
    ) {
       pw.print(" " + attributeName + "=\"" + attributeValue + "\""); 
    }
    
    //---------------------------------------------------------------------------
    public void mapClassBegin(
        ModelElement_1_0 classDef,
        List supertypeDefs,
        boolean hasFeatures,
        boolean asStructureType
    ) throws ServiceException {
        String name = (String)classDef.objGetValue("name");
        String classId = this.getClassifierId((String)classDef.objGetValue("qualifiedName"));
        boolean isAbstract = ((Boolean)classDef.objGetValue("isAbstract")).booleanValue();

        pw.write(indent, 0, nTabs); 
        pw.print("<ownedMember");
        this.printAttribute("xmi:type", "uml:Class");
        this.printAttribute("xmi:id", classId);
        this.printAttribute("name", name);
        this.printAttribute("isAbstract", Boolean.toString(isAbstract));
        pw.println(">");

        // Annotation
        if(!classDef.objGetList("annotation").isEmpty()) {
            pw.write(indent, 0, nTabs);
            pw.print("\t<ownedComment");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("body", this.toHTMLString((String)classDef.objGetValue("annotation")));
            pw.println("/>");
        }
        // Stereotypes
        if(asStructureType || !classDef.objGetList("stereotype").isEmpty()) {

            pw.write(indent, 0, nTabs);
            pw.print("\t<eAnnotations");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("source", "keywords");
            pw.println(">");

            pw.write(indent, 0, nTabs);
            pw.print("\t\t<details");
            this.printAttribute("xmi:id", this.createId());
            this.printAttribute("key", asStructureType ? Stereotypes.STRUCT : (String)classDef.objGetValue("stereotype"));
            pw.println("/>");
            
            pw.write(indent, 0, nTabs);
            pw.println("\t</eAnnotations>");
        }
        // Generalization        
        for(
            Iterator it = supertypeDefs.iterator();
            it.hasNext();
        ) {
            pw.write(indent, 0, nTabs);
            pw.print("\t<generalization");
            this.printAttribute("xmi:id", this.createId());            
            ModelElement_1_0 parentClassDef = (ModelElement_1_0)it.next();
            String parentClassQualifiedName = (String)parentClassDef.objGetValue("qualifiedName");
            this.printAttribute("general", this.getClassifierId(parentClassQualifiedName));
            pw.println("/>");            
        }
        nTabs += 1;
    }

    //---------------------------------------------------------------------------
    public void mapClassEnd(
        ModelElement_1_0 classDef,
        boolean hasStructuralFeatures
    ) {
        nTabs -= 1;
        pw.write(indent, 0, nTabs); 
        pw.println("</ownedMember>");
        pw.flush();      
    }

    //---------------------------------------------------------------------------
    public void mapModelBegin(
    ) {
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");                
        pw.println("<uml:Model xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:Default_0=\"http:///Default_0.profile.uml2\" xmlns:notation=\"http://www.ibm.com/xtools/1.5.0/Notation\" xmlns:openMDX_0=\"http:///_O4ds_t64EdmrtKJSXC0_FQ.profile.uml2\" xmlns:uml=\"http://www.eclipse.org/uml2/1.0.0/UML\" xmlns:umlnotation=\"http://www.ibm.com/xtools/1.5.0/Umlnotation\" xsi:schemaLocation=\"http:///Default_0.profile.uml2 pathmap://UML2_MSL_PROFILES/Default.epx#_bA7Pc9WLEdiy4IqP8whjFA?Default/%3CEPackage%3E http:///_O4ds_t64EdmrtKJSXC0_FQ.profile.uml2 platform:/resource/openmdx-core_profiles/openMDX.epx#_O4ds_964EdmrtKJSXC0_FQ?openMDX/%3CEPackage%3E\" xmi:id=\"_kF5MkN67EdmkJO73lqONhA\" name=\"models\" appliedProfile=\"_kF5MlN67EdmkJO73lqONhA _kF5Ml967EdmkJO73lqONhA _kF5Mmt67EdmkJO73lqONhA _kF5Mnd67EdmkJO73lqONhA _kF5MoN67EdmkJO73lqONhA _kRKocN67EdmkJO73lqONhA\">");
        pw.println("  <eAnnotations xmi:id=\"_kF5Mkd67EdmkJO73lqONhA\" source=\"uml2.diagrams\"/>");
        pw.println("  <packageImport xmi:type=\"uml:ProfileApplication\" xmi:id=\"_kF5MlN67EdmkJO73lqONhA\">");
        pw.println("    <eAnnotations xmi:id=\"_kF5Mld67EdmkJO73lqONhA\" source=\"attributes\">");
        pw.println("      <details xmi:id=\"_kF5Mlt67EdmkJO73lqONhA\" key=\"version\" value=\"0\"/>");
        pw.println("    </eAnnotations>");
        pw.println("    <importedPackage xmi:type=\"uml:Profile\" href=\"pathmap://UML2_PROFILES/Basic.profile.uml2#_6mFRgK86Edih9-GG5afQ0g\"/>");
        pw.println("    <importedProfile href=\"pathmap://UML2_PROFILES/Basic.profile.uml2#_6mFRgK86Edih9-GG5afQ0g\"/>");
        pw.println("  </packageImport>");
        pw.println("  <packageImport xmi:type=\"uml:ProfileApplication\" xmi:id=\"_kF5Ml967EdmkJO73lqONhA\">");
        pw.println("    <eAnnotations xmi:id=\"_kF5MmN67EdmkJO73lqONhA\" source=\"attributes\">");
        pw.println("      <details xmi:id=\"_kF5Mmd67EdmkJO73lqONhA\" key=\"version\" value=\"0\"/>");
        pw.println("    </eAnnotations>");
        pw.println("    <importedPackage xmi:type=\"uml:Profile\" href=\"pathmap://UML2_PROFILES/Intermediate.profile.uml2#_Cz7csK87Edih9-GG5afQ0g\"/>");
        pw.println("    <importedProfile href=\"pathmap://UML2_PROFILES/Intermediate.profile.uml2#_Cz7csK87Edih9-GG5afQ0g\"/>");
        pw.println("  </packageImport>");
        pw.println("  <packageImport xmi:type=\"uml:ProfileApplication\" xmi:id=\"_kF5Mmt67EdmkJO73lqONhA\">");
        pw.println("    <eAnnotations xmi:id=\"_kF5Mm967EdmkJO73lqONhA\" source=\"attributes\">");
        pw.println("      <details xmi:id=\"_kF5MnN67EdmkJO73lqONhA\" key=\"version\" value=\"0\"/>");
        pw.println("    </eAnnotations>");
        pw.println("    <importedPackage xmi:type=\"uml:Profile\" href=\"pathmap://UML2_PROFILES/Complete.profile.uml2#_M7pTkK87Edih9-GG5afQ0g\"/>");
        pw.println("    <importedProfile href=\"pathmap://UML2_PROFILES/Complete.profile.uml2#_M7pTkK87Edih9-GG5afQ0g\"/>");
        pw.println("  </packageImport>");
        pw.println("  <packageImport xmi:type=\"uml:ProfileApplication\" xmi:id=\"_kF5Mnd67EdmkJO73lqONhA\">");
        pw.println("    <eAnnotations xmi:id=\"_kF5Mnt67EdmkJO73lqONhA\" source=\"attributes\">");
        pw.println("      <details xmi:id=\"_kF5Mn967EdmkJO73lqONhA\" key=\"version\" value=\"0\"/>");
        pw.println("    </eAnnotations>");
        pw.println("    <importedPackage xmi:type=\"uml:Profile\" href=\"pathmap://UML2_MSL_PROFILES/Default.epx#_a_S3wNWLEdiy4IqP8whjFA?Default\"/>");
        pw.println("    <importedProfile href=\"pathmap://UML2_MSL_PROFILES/Default.epx#_a_S3wNWLEdiy4IqP8whjFA?Default\"/>");
        pw.println("  </packageImport>");
        pw.println("  <packageImport xmi:type=\"uml:ProfileApplication\" xmi:id=\"_kF5MoN67EdmkJO73lqONhA\">");
        pw.println("    <eAnnotations xmi:id=\"_kF5Mod67EdmkJO73lqONhA\" source=\"attributes\">");
        pw.println("      <details xmi:id=\"_kF5Mot67EdmkJO73lqONhA\" key=\"version\" value=\"0\"/>");
        pw.println("    </eAnnotations>");
        pw.println("    <importedPackage xmi:type=\"uml:Profile\" href=\"pathmap://UML2_MSL_PROFILES/Deployment.epx#_vjbuwOvHEdiDX5bji0iVSA?Deployment\"/>");
        pw.println("    <importedProfile href=\"pathmap://UML2_MSL_PROFILES/Deployment.epx#_vjbuwOvHEdiDX5bji0iVSA?Deployment\"/>");
        pw.println("  </packageImport>");
        pw.println("  <packageImport xmi:id=\"_kF5Mo967EdmkJO73lqONhA\">");
        pw.println("    <importedPackage xmi:type=\"uml:Model\" href=\"pathmap://UML2_LIBRARIES/UML2PrimitiveTypes.library.uml2#_EfRZoK86EdieaYgxtVWN8Q\"/>");
        pw.println("  </packageImport>");
        pw.println("  <packageImport xmi:type=\"uml:ProfileApplication\" xmi:id=\"_kRKocN67EdmkJO73lqONhA\">");
        pw.println("    <eAnnotations xmi:id=\"_kRKocd67EdmkJO73lqONhA\" source=\"attributes\">");
        pw.println("      <details xmi:id=\"_kRKoct67EdmkJO73lqONhA\" key=\"version\" value=\"0\"/>");
        pw.println("    </eAnnotations>");
        pw.println("    <importedPackage xmi:type=\"uml:Profile\" href=\"platform:/resource/openmdx-core_profiles/openMDX.epx#_O4XmQN64EdmrtKJSXC0_FQ?openMDX\"/>");
        pw.println("    <importedProfile href=\"platform:/resource/openmdx-core_profiles/openMDX.epx#_O4XmQN64EdmrtKJSXC0_FQ?openMDX\"/>");
        pw.println("  </packageImport>");
    }

    //---------------------------------------------------------------------------
    public void mapModelEnd(
    ) {
        pw.println("</uml:Model>");
        pw.flush();
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
    private String toHTMLString(
        String input
    ) {
        return HtmlEncoder.encode(input, false);
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
        return "_" + UUIDConversion.toUID(UUIDs.newUUID());
    }

    //---------------------------------------------------------------------------
    private String getClassifierId(
        String qualifiedName
    ) {
        if (!this.classifierIds.containsKey(qualifiedName))
        {
            this.classifierIds.put(
                qualifiedName,
                this.createId()
            );
        }
        return (String)this.classifierIds.get(qualifiedName);
    }

    //---------------------------------------------------------------------------
    private String getAssociationEndId(
        String associationEnd
    ) {
        if (!this.associationEndIds.containsKey(associationEnd)) {
            this.associationEndIds.put(
                associationEnd,
                this.createId()
            );
        }
        return (String)this.associationEndIds.get(associationEnd);
    }

    //---------------------------------------------------------------------------
    private String getAssociationId(
        String association
    ) {
        if (!this.associationIds.containsKey(association)) {
            this.associationIds.put(
                association,
                this.createId()
            );
        }
        return (String)this.associationIds.get(association);
    }

    //---------------------------------------------------------------------------  
    // Variables
    //---------------------------------------------------------------------------  
    private final PrintWriter pw;
    private final Map classifierIds;
    private final Map associationIds;
    private final Map associationEndIds;
    private static int nTabs = 0;
    private static char[] indent = {'\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t','\t'};

    public static final short XMI_FORMAT_RSM6 = 1;
}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMI2Parser.java,v 1.18 2007/10/10 16:06:11 hburger Exp $
 * Description: XMI2 Parser
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:11 $
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

package org.openmdx.model1.importer.xmi;

import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.pattern.StringExpression;
import org.openmdx.model1.uml1.UML1AggregationKind;
import org.openmdx.model1.uml1.UML1Association;
import org.openmdx.model1.uml1.UML1AssociationEnd;
import org.openmdx.model1.uml1.UML1Attribute;
import org.openmdx.model1.uml1.UML1ChangeableKind;
import org.openmdx.model1.uml1.UML1Class;
import org.openmdx.model1.uml1.UML1DataType;
import org.openmdx.model1.uml1.UML1ModelElement;
import org.openmdx.model1.uml1.UML1MultiplicityRange;
import org.openmdx.model1.uml1.UML1Operation;
import org.openmdx.model1.uml1.UML1OrderingKind;
import org.openmdx.model1.uml1.UML1Package;
import org.openmdx.model1.uml1.UML1Parameter;
import org.openmdx.model1.uml1.UML1ParameterDirectionKind;
import org.openmdx.model1.uml1.UML1ScopeKind;
import org.openmdx.model1.uml1.UML1Stereotype;
import org.openmdx.model1.uml1.UML1StructuralFeature;
import org.openmdx.model1.uml1.UML1VisibilityKind;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;

import org.openmdx.kernel.text.StringBuilders;

public class XMI2Parser
    implements XMIParser {

    //---------------------------------------------------------------------------
    public XMI2Parser(
        PrintStream infos,
        PrintStream warnings,
        PrintStream errors
    ) {
        this.infos = infos;
        this.warnings = warnings;
        this.errors = errors;
    }

    //---------------------------------------------------------------------------
    private void error(
        String message
    ) {
        this.errors.println("ERROR:   " + message);
    }

    //---------------------------------------------------------------------------
    private void info(
        String message
    ) {
        this.infos.println("INFO:    " + message);
    }

    //---------------------------------------------------------------------------
    public void parse(
        String modelUri,
        XMIImporter_1 importer,
        XMIReferenceResolver resolver,
        Stack scope
    ) throws Exception {

        this.importer = importer;
        this.resolver = resolver;
        this.scope = scope;
        this.modelUri = modelUri;
        XMLReader reader = null;
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            reader = parser.getXMLReader();
        }
        catch (Exception ex) {
            ex.printStackTrace(this.errors);
            throw ex;
        }
        try {
            reader.setContentHandler(this);
            reader.parse(modelUri);
        }
        catch(Exception ex) {
            if(this.getLocator() != null) {
                this.error(
                    "Exception occurred while processing line " + this.getLocator().getLineNumber() +
                    " and column " + this.getLocator().getColumnNumber()
                );
            }
            ex.printStackTrace(this.errors);
            throw ex;
        }
    }

    //---------------------------------------------------------------------------
    public Locator getLocator(
    ) {
        return this.locator;
    }

    //---------------------------------------------------------------------------
    public void startDocument(
    ) {
        this.elementStack = new Stack();
        this.associationEnds = new HashMap();
    }

    //---------------------------------------------------------------------------
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts
    ) {
        // Scope
        if("ownedMember".equals(qName)) {
            scope.push(atts.getValue("name"));
        }

        // uml:Model
        if("uml:Model".equals(qName)) {
            this.info("XMI version=" + atts.getValue("xmi:version") + "; name=" + atts.getValue("name"));
        }
        // ownedAttribute
        else if("ownedAttribute".equals(qName)) {
            // attribute
            if(atts.getValue("association") == null) {
                UML1Attribute attributeDef =
                    new UML1Attribute(
                        atts.getValue("xmi:id"),
                        atts.getValue("name"),
                        this.getScopeAsQualifiedName() + "::" + atts.getValue("name"),
                        this.toUMLVisibilityKind(atts.getValue("visibility")),
                        false,
                        UML1ScopeKind.CLASSIFIER,
                        "true".equals(atts.getValue("isReadOnly"))
                            ? UML1ChangeableKind.FROZEN
                            : UML1ChangeableKind.CHANGEABLE,
                        UML1ScopeKind.CLASSIFIER,
                        "true".equals(atts.getValue("isOrdered"))
                            ? UML1OrderingKind.ORDERED
                            : UML1OrderingKind.UNORDERED,
                        Boolean.valueOf(atts.getValue("isDerived"))
                   );
                String type = resolver.lookupXMIId(atts.getValue("type"));
                if(type != null) {
                    attributeDef.setType(type);
                }
                attributeDef.setMultiplicityRange(
                    new UML1MultiplicityRange("1", "1")
                );
                elementStack.push(attributeDef);
            }
            // reference
            else {
                UML1AssociationEnd associationEndDef =
                    new UML1AssociationEnd(
                        atts.getValue("xmi:id"),
                        atts.getValue("name"),
                        this.getScopeAsQualifiedName() + "::" + atts.getValue("name"),
                        this.toUMLVisibilityKind(atts.getValue("visibility")),
                        false,
                        this.toUMLAggregationKind(atts.getValue("aggregation")),
                        "true".equals(atts.getValue("isReadOnly"))
                            ? UML1ChangeableKind.FROZEN
                            : UML1ChangeableKind.CHANGEABLE,
                        true
                    );
                String type = resolver.lookupXMIId(
                        atts.getValue("type")
                    );
                if(type != null) {
                    associationEndDef.setParticipant(type);
                }
                this.associationEnds.put(
                    atts.getValue("xmi:id"),
                    associationEndDef
                );
                elementStack.push(associationEndDef);
            }
        }
        // uml:Package
        else if("ownedMember".equals(qName) && "uml:Package".equals(atts.getValue("xmi:type"))) {
            UML1Package pkgDef = new UML1Package(
                atts.getValue("xmi:id"),
                atts.getValue("name"),
                this.getScopeAsQualifiedName(),
                this.toUMLVisibilityKind(atts.getValue("visibility")),
                false,
                Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
                Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
                Boolean.valueOf(atts.getValue("isAbstract")).booleanValue()
            );
            elementStack.push(pkgDef);
        }
        // uml:Class
        else if("ownedMember".equals(qName) && "uml:Class".equals(atts.getValue("xmi:type"))) {
            UML1Class classDef = new UML1Class(
                atts.getValue("xmi:id"),
                atts.getValue("name"),
                this.getScopeAsQualifiedName(),
                this.toUMLVisibilityKind(atts.getValue("packageableElement_visibility")),
                false,
                Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
                Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
                Boolean.valueOf(atts.getValue("isAbstract")).booleanValue(),
                Boolean.valueOf(atts.getValue("isActive")).booleanValue()
            );
            elementStack.push(classDef);
        }
        // uml:PrimitiveType
        else if("ownedMember".equals(qName) && "uml:PrimitiveType".equals(atts.getValue("xmi:type"))) {
            UML1DataType dataTypeDef = new UML1DataType(
                atts.getValue("xmi:id"),
                atts.getValue("name"),
                this.getScopeAsQualifiedName(),
                this.toUMLVisibilityKind(atts.getValue("packageableElement_visibility")),
                false,
                Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
                Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
                Boolean.valueOf(atts.getValue("isAbstract")).booleanValue()
            );
            elementStack.push(dataTypeDef);
        }
        // uml:Association
        else if("ownedMember".equals(qName) && "uml:Association".equals(atts.getValue("xmi:type"))) {
            UML1Association associationDef = new UML1Association(
                atts.getValue("xmi:id"),
                atts.getValue("name"),
                this.getScopeAsQualifiedName(),
                this.toUMLVisibilityKind(atts.getValue("visibility")),
                false,
                Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
                Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
                Boolean.valueOf(atts.getValue("isAbstract")).booleanValue(),
                Boolean.valueOf(atts.getValue("isDerived"))
            );
            String memberEnd = atts.getValue("memberEnd");
            associationDef.setExposedEndId(memberEnd.substring(0, memberEnd.indexOf(" ")));
            associationDef.setReferencedEndId(memberEnd.substring(memberEnd.indexOf(" ") + 1));
            elementStack.push(associationDef);
        }
        // ownedEnd
        else if("ownedEnd".equals(qName) && (atts.getValue("association") != null)) {
            UML1AssociationEnd associationEndDef =
                new UML1AssociationEnd(
                    atts.getValue("xmi:id"),
                    atts.getValue("name"),
                    this.getScopeAsQualifiedName(),
                    this.toUMLVisibilityKind(atts.getValue("visibility")),
                    false,
                    this.toUMLAggregationKind(atts.getValue("aggregation")),
                    "true".equals(atts.getValue("isReadOnly"))
                        ? UML1ChangeableKind.FROZEN
                        : UML1ChangeableKind.CHANGEABLE,
                    false // ownedEnd is never navigable. Navigable ends mapped to ownedAttribute
                );
            associationEndDef.setParticipant(
                resolver.lookupXMIId(
                    atts.getValue("type")
                )
            );
            this.associationEnds.put(
                atts.getValue("xmi:id"),
                associationEndDef
            );
            elementStack.push(associationEndDef);
        }
        // ownedOperation
        else if("ownedOperation".equals(qName)) {
            UML1Operation operationDef =
                new UML1Operation(
                    atts.getValue("xmi:id"),
                    atts.getValue("name"),
                    this.getScopeAsQualifiedName() + ":" + atts.getValue("name"),
                    this.toUMLVisibilityKind(atts.getValue("visibility")),
                    false,
                    UML1ScopeKind.CLASSIFIER,
                    "true".equals(atts.getValue("isQuery")),
                    Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
                    Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
                    Boolean.valueOf(atts.getValue("isAbstract")).booleanValue()
                );
            this.scope.push(atts.getValue("name"));
            elementStack.push(operationDef);
        }
        // returnResult
        else if("returnResult".equals(qName)) {
            if(this.elementStack.size() > 0) {
                UML1ModelElement operationDef = (UML1ModelElement)this.elementStack.peek();
                if(operationDef instanceof UML1Operation) {
                    UML1Parameter parameterDef =
                        new UML1Parameter(
                            atts.getValue("xmi:id"),
                            atts.getValue("name"),
                            operationDef.getQualifiedName() + ":result",
                            this.toUMLVisibilityKind(atts.getValue("visibility")),
                            false,
                            UML1ParameterDirectionKind.RETURN
                        );
                    String type = resolver.lookupXMIId(atts.getValue("type"));
                    if(type != null) {
                        parameterDef.setType(type);
                    }
                    ((UML1Operation)operationDef).getParameters().add(parameterDef);
                    this.elementStack.push(parameterDef);
                }
            }
        }
        // ownedParameter
        else if("ownedParameter".equals(qName)) {
            if(this.elementStack.size() > 0) {
                UML1ModelElement operationDef = (UML1ModelElement)this.elementStack.peek();
                if(operationDef instanceof UML1Operation) {
                    String name = atts.getValue("name");
                    Set stereotypes = new HashSet();
                    // openMDX compatibility. Allow stereotypes as prefix for parameter names
                    if((name != null) && (name.indexOf("<<") >= 0) && (name.indexOf(">>") >= 0)) {
                        StringTokenizer tokenizer = new StringTokenizer(name.substring(name.indexOf("<<") + 2, name.indexOf(">>")), " ,");
                        while(tokenizer.hasMoreTokens()) {
                            stereotypes.add(tokenizer.nextToken());
                        }
                        name = name.substring(name.indexOf(">>") + 2).trim();
                    }
                    UML1Parameter parameterDef =
                        new UML1Parameter(
                            atts.getValue("xmi:id"),
                            name,
                            operationDef.getQualifiedName() + ":" + atts.getValue("name"),
                            this.toUMLVisibilityKind(atts.getValue("visibility")),
                            false,
                            UML1ParameterDirectionKind.IN
                        );
                    String type = resolver.lookupXMIId(atts.getValue("type"));
                    if(type != null) {
                        parameterDef.setType(type);
                    }
                    parameterDef.getStereotypes().addAll(stereotypes);
                    ((UML1Operation)operationDef).getParameters().add(parameterDef);
                    this.elementStack.push(parameterDef);
                }
            }
        }
        // ownedComment
        else if("ownedComment".equals(qName)) {
            String body = atts.getValue("body");
            if(elementStack.size() > 0) {
                UML1ModelElement element = (UML1ModelElement)elementStack.peek();
                if(body != null) {
                    element.getComment().add(
                        body
                    );
                }
            }
        }
        // generalization
        else if("generalization".equals(qName)) {
            String supertype = resolver.lookupXMIId(atts.getValue("general"));
            if(this.elementStack.size() > 0) {
                UML1ModelElement element = (UML1ModelElement)elementStack.peek();
                if((supertype != null) && (element instanceof UML1Class)) {
                    ((UML1Class)element).getSuperclasses().add(supertype);
                }
            }
        }
        // generalization reference
        else if("general".equals(qName)) {
            HRef href = null;
            try {
                href = new HRef(atts.getValue("href"));
            }
            catch(ServiceException e) {
                this.error("reference is not a valid URI " + atts.getValue("href"));
            }
            if(href != null) {
                String id = href.getFragment();
                if(id.indexOf("?") >= 0) {
                    id = id.substring(0, id.indexOf("?"));
                }
                String supertype = resolver.lookupXMIId(id);
                if(this.elementStack.size() > 0) {
                    UML1ModelElement element = (UML1ModelElement)elementStack.peek();
                    if((supertype != null) && (element instanceof UML1Class)) {
                        ((UML1Class)element).getSuperclasses().add(supertype);
                    }
                }
            }
        }
        // eAnnotations, source=keywords
        else if("eAnnotations".equals(qName) && "keywords".equals(atts.getValue("source"))) {
            UML1Stereotype annotationDef = new UML1Stereotype(
                atts.getValue("xmi:id"),
                atts.getValue("name"),
                this.getScopeAsQualifiedName(),
                this.toUMLVisibilityKind(atts.getValue("visibility")),
                false
            );
            elementStack.push(annotationDef);
        }
        // details
        else if("details".equals(qName)) {
            if(this.elementStack.size() > 0) {
                UML1ModelElement element = (UML1ModelElement)elementStack.peek();
                // Stereotype
                if(element instanceof UML1Stereotype) {
                    ((UML1Stereotype)element).setDataValue(atts.getValue("key"));
                }
            }
        }
        // lowerValue
        else if("lowerValue".equals(qName)) {
            if(this.elementStack.size() > 0) {
                UML1ModelElement element = (UML1ModelElement)elementStack.peek();
                if(element instanceof UML1StructuralFeature) {
                    UML1StructuralFeature feature = (UML1StructuralFeature)element;
                    if(feature.getMultiplicityRange() == null) {
                        feature.setMultiplicityRange(new UML1MultiplicityRange("1", "1"));
                    }
                    String value = atts.getValue("value");
                    feature.getMultiplicityRange().setLower(
                        value == null ? "0" : value
                    );
                }
                else if(element instanceof UML1AssociationEnd) {
                    UML1AssociationEnd associationEndDef = (UML1AssociationEnd)element;
                    if(associationEndDef.getMultiplicityRange() == null) {
                        associationEndDef.setMultiplicityRange(new UML1MultiplicityRange("1", "1"));
                    }
                    String value = atts.getValue("value");
                    associationEndDef.getMultiplicityRange().setLower(
                        value == null ? "0" : value
                    );
                }
            }
        }
        // upperValue
        else if("upperValue".equals(qName)) {
            if(this.elementStack.size() > 0) {
                UML1ModelElement element = (UML1ModelElement)elementStack.peek();
                if(element instanceof UML1StructuralFeature) {
                    UML1StructuralFeature feature = (UML1StructuralFeature)element;
                    if(feature.getMultiplicityRange() == null) {
                        feature.setMultiplicityRange(new UML1MultiplicityRange("1", "1"));
                    }
                    String value = atts.getValue("value");
                    feature.getMultiplicityRange().setUpper(
                        value == null ? "1" : value
                    );
                }
                else if(element instanceof UML1AssociationEnd) {
                    UML1AssociationEnd associationEndDef = (UML1AssociationEnd)element;
                    if(associationEndDef.getMultiplicityRange() == null) {
                        associationEndDef.setMultiplicityRange(new UML1MultiplicityRange("1", "1"));
                    }
                    String value = atts.getValue("value");
                    associationEndDef.getMultiplicityRange().setUpper(
                        value == null ? "1" : value
                    );
                }
            }
        }
        // qualifier
        else if("qualifier".equals(qName)) {
            if(this.elementStack.size() > 0) {
                UML1ModelElement element = (UML1ModelElement)elementStack.peek();
                if(element instanceof UML1AssociationEnd) {
                    UML1AssociationEnd associationEndDef = (UML1AssociationEnd)element;
                    UML1Attribute qualifier =
                        new UML1Attribute(
                            atts.getValue("xmi:id"),
                            atts.getValue("name"),
                            this.getScopeAsQualifiedName() + "::" + atts.getValue("name"),
                            this.toUMLVisibilityKind(atts.getValue("visibility")),
                            false,
                            UML1ScopeKind.CLASSIFIER,
                            "true".equals(atts.getValue("isReadOnly"))
                                ? UML1ChangeableKind.FROZEN
                                : UML1ChangeableKind.CHANGEABLE,
                            UML1ScopeKind.CLASSIFIER,
                            "true".equals(atts.getValue("isOrdered"))
                                ? UML1OrderingKind.ORDERED
                                : UML1OrderingKind.UNORDERED,
                            Boolean.valueOf(atts.getValue("isDerived"))
                       );
                    String type = resolver.lookupXMIId(atts.getValue("type"));
                    if(type != null) {
                        qualifier.setType(type);
                    }
                    associationEndDef.getQualifier().add(qualifier);
                    this.elementStack.push(qualifier);
                }
            }
        }
        // package references
        else if(
            "references".equals(qName) &&
            ("uml:Package".equals(atts.getValue("xmi:type")) || "uml:Model".equals(atts.getValue("xmi:type"))) 
        ) {
            HRef href = null;
            try {
                href = new HRef(atts.getValue("href"));
            }
            catch(ServiceException e) {
                this.error("reference is not a valid URI " + atts.getValue("href"));
            }
            if(href != null) {
                try {
                    String scheme = href.getScheme();
                    String path = StringExpression.compile("%20").matcher(href.getPath()).replaceAll(" ");
                    String packageName = null;
                    if("platform".equals(scheme) && path.startsWith("/resource/")) {
                        path = path.substring("/resource/".length());
                        packageName = path.substring(path.indexOf("/") + 1);
                    }
                    else {
                        packageName = path;
                    }
                    String projectName = this.resolver.lookupProject(packageName);
                    if((projectName != null) && (this.importer.getPathMap().get(projectName) == null)) {
                        this.error("Referenced project " + projectName + " not defined as pathMapSymbol");
                    }
                    else {
                        URL packageURL = projectName == null
                            ? new URL(this.modelUri.substring(0, this.modelUri.lastIndexOf('/') + 1) + packageName)
                            : new URL(this.importer.getPathMap().get(projectName) + packageName);
                        XMIImporter_1 nestedImporter = new XMIImporter_1(
                            packageURL,
                            this.importer.getXMIFormat(),
                            this.importer.getPathMap(),
                            this.infos,
                            this.warnings,
                            this.errors
                        );
                        nestedImporter.processNested(
                            this.importer,
                            this.resolver,
                            this.scope
                        );
                    }
                }
                catch(Exception e) {
                    this.error("Can not process nested model " + atts.getValue("href") + ". Reason=" + e.getMessage());
                }
            }
        }
        // type reference
        else if("type".equals(qName)) {
            HRef href = null;
            try {
                href = new HRef(atts.getValue("href"));
            }
            catch(ServiceException e) {
                this.error("reference is not a valid URI " + atts.getValue("href"));
            }
            if(href != null) {
                String id = href.getFragment();
                if(id.indexOf("?") >= 0) {
                    id = id.substring(0, id.indexOf("?"));
                }
                if(this.elementStack.size() > 0) {
                    UML1ModelElement element = (UML1ModelElement)this.elementStack.peek();
                    if(element instanceof UML1StructuralFeature) {
                        ((UML1StructuralFeature)element).setType(
                            resolver.lookupXMIId(id)
                        );
                    }
                    else if(element instanceof UML1Parameter) {
                        ((UML1Parameter)element).setType(
                            resolver.lookupXMIId(id)
                        );
                    }
                    else if(element instanceof UML1AssociationEnd) {
                        ((UML1AssociationEnd)element).setParticipant(
                            resolver.lookupXMIId(id)
                        );
                    }
                }
            }
        }
    }

    //---------------------------------------------------------------------------
    public void endElement(
        String namespaceURI,
        String localName,
        String qName
    ) {
        if("ownedMember".equals(qName) || "ownedOperation".equals(qName)) {
            scope.pop();
        }
        try {
            // ownedAttribute
            if("ownedAttribute".equals(qName)) {
                if(this.elementStack.size() > 0) {
                    Object attributeDef = this.elementStack.pop();
                    if(this.elementStack.size() > 0) {
                        UML1ModelElement classDef = (UML1ModelElement)this.elementStack.peek();
                        if((classDef instanceof UML1Class) && (attributeDef instanceof UML1Attribute)) {
                            ((UML1Class)classDef).getFeature().add(attributeDef);
                        }
                    }
                }
            }
            // ownedMember
            else if("ownedMember".equals(qName)) {
                if(this.elementStack.size() > 0) {
                    UML1ModelElement element = (UML1ModelElement)this.elementStack.pop();
                    if(element instanceof UML1Package) {
                        this.importer.processUMLPackage((UML1Package)element);
                    }
                    else if(element instanceof UML1Class) {
                        this.importer.processUMLClass((UML1Class)element);
                    }
                    else if(element instanceof UML1DataType) {
                        this.importer.processUMLDataType((UML1DataType)element);
                    }
                    else if(element instanceof UML1Association) {
                        UML1Association associationDef = (UML1Association)element;
                        associationDef.getConnection().add(
                            this.associationEnds.get(associationDef.getExposedEndId())
                        );
                        associationDef.getConnection().add(
                            this.associationEnds.get(associationDef.getReferencedEndId())
                        );
                        this.importer.processUMLAssociation((UML1Association)element);
                    }
                }
            }
            // ownedOperation
            else if("ownedOperation".equals(qName)) {
                if(this.elementStack.size() > 0) {
                    UML1ModelElement operationDef = (UML1ModelElement)this.elementStack.pop();
                    if(operationDef instanceof UML1Operation) {
                        if(this.elementStack.size() > 0) {
                            UML1ModelElement classDef = (UML1ModelElement)this.elementStack.peek();
                            if(classDef instanceof UML1Class) {
                                ((UML1Class)classDef).getFeature().add(
                                    operationDef
                                );
                            }
                        }
                    }
                }
            }
            // returnResult
            else if("returnResult".equals(qName)) {
                if(this.elementStack.size() > 0) {
                    this.elementStack.pop();
                }
            }
            // ownedParameter
            else if("ownedParameter".equals(qName)) {
                if(this.elementStack.size() > 0) {
                    this.elementStack.pop();
                }
            }
            // qualifier
            else if("qualifier".equals(qName)) {
                if(this.elementStack.size() > 0) {
                    this.elementStack.pop();
                }
            }
            // eAnnotations
            else if("eAnnotations".equals(qName)) {
                if(this.elementStack.size() > 0) {
                    UML1ModelElement element = (UML1ModelElement)this.elementStack.peek();
                    // apply stereotype to element
                    if(element instanceof UML1Stereotype) {
                        if(this.elementStack.size() > 1) {
                            this.elementStack.pop();
                            ((UML1ModelElement)this.elementStack.peek()).getStereotypes().add(
                                ((UML1Stereotype)element).getDataValue()
                            );
                        }
                    }
                }
            }
            // ownedEnd
            else if("ownedEnd".equals(qName)) {
                if(this.elementStack.size() > 0) {
                    this.elementStack.pop();
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace(this.errors);
        }
    }

    //---------------------------------------------------------------------------
    public void characters(
        char[] ch,
        int start,
        int length
    ) {
        //
    }

    //---------------------------------------------------------------------------
    public void endDocument() {
        //
        }

    //---------------------------------------------------------------------------
    public void endPrefixMapping(String prefix) {
        //
        }

    //---------------------------------------------------------------------------
    public void ignorableWhitespace(char[] ch, int start, int length) {
        //
        }

    //---------------------------------------------------------------------------
    public void processingInstruction(String target, String data) {
        //
        }

    //---------------------------------------------------------------------------
    public void setDocumentLocator(Locator locator) {
        //
        }

    //---------------------------------------------------------------------------
    public void skippedEntity(String name) {
        //
    }

    //---------------------------------------------------------------------------
    public void startPrefixMapping(String prefix, String uri) {
        //
    }

    //---------------------------------------------------------------------------
    private String getScopeAsQualifiedName(
    ) {
        CharSequence sb = StringBuilders.newStringBuilder();
        for (int i = 0; i < this.scope.size(); i++) {
            if(i > 0) StringBuilders.asStringBuilder(sb).append("::");
            StringBuilders.asStringBuilder(sb).append(this.scope.get(i));
        }
        return sb.toString();
    }

    //---------------------------------------------------------------------------
    private UML1VisibilityKind toUMLVisibilityKind(
        String visibility
    ) {
        if ("private".equals(visibility))
        {
          return UML1VisibilityKind.PRIVATE;
        }
        else if ("protected".equals(visibility))
        {
          return UML1VisibilityKind.PROTECTED;
        }
        else if ("package".equals(visibility))
        {
          return UML1VisibilityKind.PACKAGE;
        }
        else
        {
          return UML1VisibilityKind.PUBLIC;
        }
    }

    //---------------------------------------------------------------------------
    private UML1AggregationKind toUMLAggregationKind(
        String aggregation
    ) {
        if("composite".equals(aggregation)) {
            return UML1AggregationKind.COMPOSITE;
        }
        else if("shared".equals(aggregation)) {
            return UML1AggregationKind.AGGREGATE;
        }
        else {
            return UML1AggregationKind.NONE;
        }
    }

    //-------------------------------------------------------------------------
    private String modelUri = null;
    private Locator locator = null;
    private Stack scope = null;
    private Stack elementStack = null;
    private Map associationEnds = null;
    private XMIImporter_1 importer = null;
    private XMIReferenceResolver resolver = null;
    private PrintStream infos = null;
    private PrintStream warnings = null;
    private PrintStream errors = null;

}

//--- End of File -----------------------------------------------------------

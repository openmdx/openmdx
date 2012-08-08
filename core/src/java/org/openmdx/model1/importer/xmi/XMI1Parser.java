/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMI1Parser.java,v 1.14 2008/04/03 09:49:25 wfro Exp $
 * Description: XMI1 Parser
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/03 09:49:25 $
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
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.model1.uml1.UML1AggregationKind;
import org.openmdx.model1.uml1.UML1Association;
import org.openmdx.model1.uml1.UML1AssociationEnd;
import org.openmdx.model1.uml1.UML1Attribute;
import org.openmdx.model1.uml1.UML1ChangeableKind;
import org.openmdx.model1.uml1.UML1Class;
import org.openmdx.model1.uml1.UML1DataType;
import org.openmdx.model1.uml1.UML1Generalization;
import org.openmdx.model1.uml1.UML1ModelElement;
import org.openmdx.model1.uml1.UML1MultiplicityRange;
import org.openmdx.model1.uml1.UML1Operation;
import org.openmdx.model1.uml1.UML1OrderingKind;
import org.openmdx.model1.uml1.UML1Package;
import org.openmdx.model1.uml1.UML1Parameter;
import org.openmdx.model1.uml1.UML1ParameterDirectionKind;
import org.openmdx.model1.uml1.UML1ScopeKind;
import org.openmdx.model1.uml1.UML1TagDefinition;
import org.openmdx.model1.uml1.UML1TaggedValue;
import org.openmdx.model1.uml1.UML1VisibilityKind;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;

@SuppressWarnings("unchecked")
public class XMI1Parser
  implements XMIParser {

  public XMI1Parser(
    PrintStream infos,
    PrintStream warnings,
    PrintStream errors
  ) {
    this.infos = infos;
    this.errors = errors;
    this.warnings = warnings;
  }

  //---------------------------------------------------------------------------
  public void parse(
    String uri,
    XMIImporter_1 consumer,
    XMIReferenceResolver resolver,
    Stack scope
  ) throws Exception {
    this.importer = consumer;
    this.resolver = resolver;
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
      reader.parse(uri);
    }
    catch (Exception ex) {
      if (this.getLocator() != null)
      {
        this.errors.println(
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
  public void startDocument()
  {
    scope = new ArrayList();
    this.classStack = new Stack();
    this.referenceStack = new Stack();
  }

  //---------------------------------------------------------------------------
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
  {
    if (XMI1Constants.XMI.equals(qName))
    {
      this.infos.println("XMI version '" + atts.getValue("xmi.version") + "' detected");
    }
    else if (XMI1Constants.MODEL.equals(qName))
    {
      this.infos.println("Parsing UML model '" + atts.getValue("name") + "' ...");
    }
    else if (XMI1Constants.GRAPHNODE.equals(qName) && !this.diagramInfoDetected)
    {
      this.diagramInfoDetected = true;
      this.warnings.println("Found diagram/layout information. Will be skipped.");
    }
    else if (XMI1Constants.ATTRIBUTE.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create attribute def and push on stack
        UML1Attribute attrDef = new UML1Attribute(
          "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            this.toUMLScopeKind(atts.getValue("ownerScope")),
            this.toUMLChangeableKind(atts.getValue("changeability")),
            this.toUMLScopeKind(atts.getValue("targetScope")),
            this.toUMLOrderingKind(atts.getValue("ordering")),
            null
        );
        // type maybe defined as XML attribute
        if (atts.getValue("type") != null)
        {
          attrDef.setType(
            resolver.lookupXMIId(
              atts.getValue("type")
            )
          );
        }
        // stereotype maybe defined as XML attribute
        if (atts.getValue("stereotype") != null)
        {
          attrDef.getStereotypes().add(
            resolver.lookupXMIId(
              atts.getValue("stereotype")
            )
          );
        }
        classStack.push(attrDef);
      }
    }
    else if (XMI1Constants.OPERATION.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create operation def and push on stack
        UML1Operation opDef = new UML1Operation(
            "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            this.toUMLScopeKind(atts.getValue("targetScope")),
            Boolean.valueOf(atts.getValue("isQuery")).booleanValue(),
            Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
            Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
            Boolean.valueOf(atts.getValue("isAbstract")).booleanValue()
        );
        // stereotype maybe defined as XML attribute
        if (atts.getValue("stereotype") != null)
        {
          opDef.getStereotypes().add(
            resolver.lookupXMIId(
              atts.getValue("stereotype")
            )
          );
        }
        classStack.push(opDef);
      }
    }
    else if (XMI1Constants.CLASS.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create class def and push on stack
        UML1Class classDef = new UML1Class(
            "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
            Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
            Boolean.valueOf(atts.getValue("isAbstract")).booleanValue(),
            Boolean.valueOf(atts.getValue("isActive")).booleanValue()
        );
        // stereotype maybe defined as XML attribute
        if (atts.getValue("stereotype") != null)
        {
          classDef.getStereotypes().add(
            resolver.lookupXMIId(
              atts.getValue("stereotype")
            )
          );
        }
        // generalization maybe defined as XML attribute
        if (atts.getValue("generalization") != null)
        {
          UML1Generalization generalization = this.resolver.lookupGeneralization(
            atts.getValue("generalization")
          );
          String parent = this.resolver.lookupXMIId(
            generalization.getParent()
          );
          classDef.getSuperclasses().add(parent);
        }
        classStack.push(classDef);

        // add new class scope
        scope.add(atts.getValue("name"));
      }
    }
    else if (XMI1Constants.ASSOCIATION_END.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create association end def and push on stack
        UML1AssociationEnd assEnd = new UML1AssociationEnd(
            "",
            atts.getValue("name"),
            this.toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            this.toUMLAggregationKind(atts.getValue("aggregation")),
            this.toUMLChangeableKind(atts.getValue("changeability")),
            Boolean.valueOf(atts.getValue("isNavigable")).booleanValue()
        );
        // participant maybe defined as XML attribute
        if (atts.getValue("participant") != null)
        {
          assEnd.setParticipant(
            resolver.lookupXMIId(
              atts.getValue("participant")
            )
          );
        }
        classStack.push(assEnd);
      }
    }
    else if (XMI1Constants.ASSOCIATION.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create association def and push on stack
        UML1Association ass = new UML1Association(
            "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
            Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
            Boolean.valueOf(atts.getValue("isAbstract")).booleanValue(),
            null // derive isDerived from tagged values
        );
        classStack.push(ass);

        // add new association scope
        scope.add(atts.getValue("name"));
      }
    }
    else if (XMI1Constants.GENERALIZATION.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference to generalization: resolve it and push it on stack
        classStack.push(
          resolver.lookupGeneralization(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create operation def and push on stack
        UML1Generalization genDef = new UML1Generalization(
            "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue()
        );
        classStack.push(genDef);
      }
    }
    else if (XMI1Constants.PACKAGE.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create class def and push on stack
        UML1Package pkgDef = new UML1Package(
            "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
            Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
            Boolean.valueOf(atts.getValue("isAbstract")).booleanValue()
        );
        classStack.push(pkgDef);

        // add new scope
        scope.add(atts.getValue("name"));
      }
    }
    else if (XMI1Constants.PARAMETER.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create parameter def and push on stack
        UML1Parameter paramDef = new UML1Parameter(
            "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            this.toUMLParameterDirectionKind(atts.getValue("kind"))
        );
        // type maybe defined as XML attribute
        if (atts.getValue("type") != null)
        {
          paramDef.setType(
            resolver.lookupXMIId(
              atts.getValue("type")
            )
          );
        }
        // stereotype maybe defined as XML attribute
        if (atts.getValue("stereotype") != null)
        {
          paramDef.getStereotypes().add(
            resolver.lookupXMIId(
              atts.getValue("stereotype")
            )
          );
        }
        classStack.push(paramDef);
      }
    }
    else if (XMI1Constants.STEREOTYPE.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: stereotype definition not used at the moment
        // push null value on stack
        classStack.push(null);
      }
    }
    else if (XMI1Constants.TAGGED_VALUE.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create tagged value def and push on stack
        UML1TaggedValue taggedValue = new UML1TaggedValue(
            "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue()
        );
        // type maybe defined as XML attribute
        if (atts.getValue("type") != null)
        {
          taggedValue.setType(
            resolver.lookupTagDefinition(
              atts.getValue("type")
            )
          );
        }
        classStack.push(taggedValue);
      }
    }
    else if (XMI1Constants.TAG_DEFINITION.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupTagDefinition(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: tag definition not used at the moment
        // push null value on stack
        classStack.push(null);
      }
    }
    else if (XMI1Constants.COMMENT.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupComment(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: comment not used at the moment
        // push null value on stack
        classStack.push(null);
      }
    }
    else if (XMI1Constants.DATATYPE.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create datatype definition push it on stack
        UML1DataType datatypeDef = new UML1DataType(
            "",
            atts.getValue("name"),
            toQualifiedName(scope, atts.getValue("name")),
            this.toUMLVisibilityKind(atts.getValue("visibility")),
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            Boolean.valueOf(atts.getValue("isRoot")).booleanValue(),
            Boolean.valueOf(atts.getValue("isLeaf")).booleanValue(),
            Boolean.valueOf(atts.getValue("isAbstract")).booleanValue()
        );
        classStack.push(datatypeDef);
      }
    }
    else if (XMI1Constants.MULTIPLICITY_RANGE.equals(qName))
    {
      if (atts.getValue("xmi.idref") != null)
      {
        // found reference: resolve it and push on stack
        classStack.push(
          resolver.lookupXMIId(atts.getValue("xmi.idref"))
        );
      }
      else
      {
        // found definition: create multiplicity range definition push it on stack
        UML1MultiplicityRange multipRange = new UML1MultiplicityRange(
          atts.getValue("lower"),
          atts.getValue("upper")
        );
        classStack.push(multipRange);
      }
    }
    else if (XMI1Constants.STRUCTURALFEATURE_TYPE.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.STRUCTURALFEATURE_TYPE);
    }
    else if (XMI1Constants.STRUCTURALFEATURE_MULTIPLICITY.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.STRUCTURALFEATURE_MULTIPLICITY);
    }
    else if (XMI1Constants.ASSOCIATION_END_MULTIPLICITY.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.ASSOCIATION_END_MULTIPLICITY);
    }
    else if (XMI1Constants.ASSOCIATION_END_PARTICIPANT.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.ASSOCIATION_END_PARTICIPANT);
    }
    else if (XMI1Constants.ASSOCIATION_END_QUALIFIER.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.ASSOCIATION_END_QUALIFIER);
    }
    else if (XMI1Constants.PARAMETER_TYPE.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.PARAMETER_TYPE);
    }
    else if (XMI1Constants.CLASSIFIER_FEATURE.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.CLASSIFIER_FEATURE);
    }
    else if (XMI1Constants.METHOD_SPECIFICATION.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.METHOD_SPECIFICATION);
    }
    else if (XMI1Constants.COMMENT_ANNOTATEDELEMENT.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.COMMENT_ANNOTATEDELEMENT);
    }
    else if (XMI1Constants.GENERALIZATION_PARENT.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.GENERALIZATION_PARENT);
    }
    else if (XMI1Constants.GENERALIZATION_CHILD.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.GENERALIZATION_CHILD);
    }
    else if (XMI1Constants.GENERALIZABLEELEMENT_GENERALIZATION.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.GENERALIZABLEELEMENT_GENERALIZATION);
    }
    else if (XMI1Constants.NAMESPACE_OWNEDELEMENT.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.NAMESPACE_OWNEDELEMENT);
    }
    else if (XMI1Constants.MODELELEMENT_STEREOTYPE.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.MODELELEMENT_STEREOTYPE);
    }
    else if (XMI1Constants.MODELELEMENT_TAGGEDVALUE.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.MODELELEMENT_TAGGEDVALUE);
    }
    else if (XMI1Constants.MODELELEMENT_COMMENT.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.MODELELEMENT_COMMENT);
    }
    else if (XMI1Constants.TAGGEDVALUE_TYPE.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.TAGGEDVALUE_TYPE);
    }
    else if (XMI1Constants.TAGGEDVALUE_DATAVALUE.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.TAGGEDVALUE_DATAVALUE);
    }
    else if (XMI1Constants.ASSOCIATION_CONNECTION.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.ASSOCIATION_CONNECTION);
    }
    else if (XMI1Constants.BEHAVIORALFEATURE_PARAMETER.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.BEHAVIORALFEATURE_PARAMETER);
    }
    else if (XMI1Constants.UML1SEMANTICMODELBRIDGE_ELEMENT.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.UML1SEMANTICMODELBRIDGE_ELEMENT);
    }
  }

  //---------------------------------------------------------------------------
  public void endElement(String namespaceURI, String localName, String qName)
  {
    try {
      if (XMI1Constants.PACKAGE.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.NAMESPACE_OWNEDELEMENT.equals(ref))
        {
          // close current scope
          scope.remove(scope.size()-1);

          this.importer.processUMLPackage((UML1Package)tmp);
        }
      }
      else if (XMI1Constants.CLASS.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.NAMESPACE_OWNEDELEMENT.equals(ref))
        {
          // close current class scope
          scope.remove(scope.size()-1);

          this.importer.processUMLClass((UML1Class)tmp);
        }
        else if (XMI1Constants.PARAMETER_TYPE.equals(ref))
        {
          ((UML1Parameter)this.classStack.peek()).setType((String)tmp);
        }
        else if (XMI1Constants.STRUCTURALFEATURE_TYPE.equals(ref))
        {
          ((UML1Attribute)classStack.peek()).setType((String)tmp);
        }
        else if (XMI1Constants.ASSOCIATION_END_PARTICIPANT.equals(ref))
        {
          ((UML1AssociationEnd)this.classStack.peek()).setParticipant((String)tmp);
        }
        else if (XMI1Constants.COMMENT_ANNOTATEDELEMENT.equals(ref))
        {
          // ignored
        }
        else if (XMI1Constants.GENERALIZATION_PARENT.equals(ref))
        {
          ((UML1Generalization)classStack.peek()).setParent((String)tmp);
        }
        else if (XMI1Constants.GENERALIZATION_CHILD.equals(ref))
        {
          ((UML1Generalization)classStack.peek()).setChild((String)tmp);
        }
      }
      else if (XMI1Constants.ATTRIBUTE.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.CLASSIFIER_FEATURE.equals(ref))
        {
          ((UML1Class)classStack.peek()).getFeature().add(tmp);
        }
        else if (XMI1Constants.ASSOCIATION_END_QUALIFIER.equals(ref))
        {
          ((UML1AssociationEnd)classStack.peek()).getQualifier().add(tmp);
        }
      }
      else if (XMI1Constants.PARAMETER.equals(qName))
      {
        Object tmp = this.classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.BEHAVIORALFEATURE_PARAMETER.equals(ref))
        {
          ((UML1Operation)classStack.peek()).getParameters().add(tmp);
        }
      }
      else if (XMI1Constants.OPERATION.equals(qName))
      {
        Object tmp = this.classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.CLASSIFIER_FEATURE.equals(ref))
        {
          ((UML1Class)classStack.peek()).getFeature().add(tmp);
        }
        else if (XMI1Constants.METHOD_SPECIFICATION.equals(ref))
        {
          // ignore method specifications
        }
      }
      else if (XMI1Constants.STEREOTYPE.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.MODELELEMENT_STEREOTYPE.equals(ref))
        {
          ((UML1ModelElement)classStack.peek()).getStereotypes().add(tmp);
        }
      }
      else if (XMI1Constants.GENERALIZATION.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.GENERALIZABLEELEMENT_GENERALIZATION.equals(ref))
        {
          String parent = this.resolver.lookupXMIId(
            ((UML1Generalization)tmp).getParent()
          );
          ((UML1Class)classStack.peek()).getSuperclasses().add(parent);
        }
        else if (XMI1Constants.NAMESPACE_OWNEDELEMENT.equals(ref))
        {
          this.importer.processUMLGeneralization((UML1Generalization)tmp);
        }
      }
      else if (XMI1Constants.TAGGED_VALUE.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.MODELELEMENT_TAGGEDVALUE.equals(ref))
        {
          ((UML1ModelElement)classStack.peek()).getTaggedValues().add(tmp);
        }
      }
      else if (XMI1Constants.TAG_DEFINITION.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.TAGGEDVALUE_TYPE.equals(ref))
        {
          ((UML1TaggedValue)classStack.peek()).setType((UML1TagDefinition)tmp);
        }
      }
      else if (XMI1Constants.COMMENT.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.MODELELEMENT_COMMENT.equals(ref))
        {
          ((UML1ModelElement)classStack.peek()).getComment().add((String)tmp);
        }
      }
      else if (XMI1Constants.DATATYPE.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.NAMESPACE_OWNEDELEMENT.equals(ref))
        {
          this.importer.processUMLDataType((UML1DataType)tmp);
        }
        else if (XMI1Constants.STRUCTURALFEATURE_TYPE.equals(ref))
        {
          ((UML1Attribute)classStack.peek()).setType((String)tmp);
        }
        else if (XMI1Constants.PARAMETER_TYPE.equals(ref))
        {
          ((UML1Parameter)classStack.peek()).setType((String)tmp);
        }
      }
      else if (XMI1Constants.MULTIPLICITY_RANGE.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.ASSOCIATION_END_MULTIPLICITY.equals(ref))
        {
          ((UML1AssociationEnd)classStack.peek()).setMultiplicityRange((UML1MultiplicityRange)tmp);
        }
        else if (XMI1Constants.STRUCTURALFEATURE_MULTIPLICITY.equals(ref))
        {
          ((UML1Attribute)classStack.peek()).setMultiplicityRange((UML1MultiplicityRange)tmp);
        }
      }
      else if (XMI1Constants.ASSOCIATION_END.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.ASSOCIATION_CONNECTION.equals(ref))
        {
          ((UML1Association)classStack.peek()).getConnection().add(tmp);
        }
      }
      else if (XMI1Constants.ASSOCIATION.equals(qName))
      {
        Object tmp = classStack.pop();
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.NAMESPACE_OWNEDELEMENT.equals(ref))
        {
          // close current association scope
          scope.remove(scope.size()-1);

          this.importer.processUMLAssociation((UML1Association)tmp);
        }
      }
      else if (XMI1Constants.STRUCTURALFEATURE_TYPE.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.STRUCTURALFEATURE_MULTIPLICITY.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.ASSOCIATION_END_MULTIPLICITY.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.ASSOCIATION_END_PARTICIPANT.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.ASSOCIATION_END_QUALIFIER.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.PARAMETER_TYPE.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.CLASSIFIER_FEATURE.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.METHOD_SPECIFICATION.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.COMMENT_ANNOTATEDELEMENT.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.GENERALIZATION_PARENT.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.GENERALIZATION_CHILD.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.NAMESPACE_OWNEDELEMENT.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.MODELELEMENT_STEREOTYPE.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.MODELELEMENT_TAGGEDVALUE.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.MODELELEMENT_COMMENT.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.TAGGEDVALUE_TYPE.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.TAGGEDVALUE_DATAVALUE.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.GENERALIZABLEELEMENT_GENERALIZATION.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.ASSOCIATION_CONNECTION.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.BEHAVIORALFEATURE_PARAMETER.equals(qName))
      {
        this.referenceStack.pop();
      }
      else if (XMI1Constants.UML1SEMANTICMODELBRIDGE_ELEMENT.equals(qName))
      {
        this.referenceStack.pop();
      }
    } catch(Exception ex) {
      ex.printStackTrace(this.errors);
    }
  }

  //---------------------------------------------------------------------------
  public void characters(char[] ch, int start, int length)
  {
    if (!this.referenceStack.isEmpty())
    {
      String ref = (String)this.referenceStack.peek();
      if (XMI1Constants.TAGGEDVALUE_DATAVALUE.equals(ref))
      {
        UML1TaggedValue taggedValue = (UML1TaggedValue)this.classStack.peek();

        if (taggedValue.getDataValue() != null) {
          // Because the characters callback may be called more than once between
          // calls to startElement and endElement, the incoming buffer must be
          // appended to an already existing value. 
          StringBuilder sb = new StringBuilder(
              taggedValue.getDataValue()
          ).append(
              String.copyValueOf(ch, start, length)
          );
          taggedValue.setDataValue(sb.toString());
        }
        else
        {
          // first characters callback for this tagged value
          taggedValue.setDataValue(String.copyValueOf(ch, start, length));
        }
      }
    }
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
  private String toQualifiedName(
    List scope,
    String name
  ) {
    StringBuilder sb = new StringBuilder();
    for (
      int i = 0;
      i < scope.size();
      i++
    ) sb.append(
        scope.get(i)
    ).append(
        "::"
    );
    return sb.append(name).toString();
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
  private UML1ScopeKind toUMLScopeKind(
    String scope
  ) {
    if ("classifier".equals(scope))
    {
      return UML1ScopeKind.CLASSIFIER;
    }
    else
    {
      return UML1ScopeKind.INSTANCE;
    }
  }

  //---------------------------------------------------------------------------
  private UML1OrderingKind toUMLOrderingKind(
    String ordering
  ) {
    if ("ordered".equals(ordering))
    {
      return UML1OrderingKind.ORDERED;
    }
    else
    {
      return UML1OrderingKind.UNORDERED;
    }
  }

  //---------------------------------------------------------------------------
  private UML1ChangeableKind toUMLChangeableKind(
    String changeability
  ) {
    if ("addOnly".equals(changeability))
    {
      return UML1ChangeableKind.ADDONLY;
    }
    else if ("frozen".equals(changeability))
    {
      return UML1ChangeableKind.FROZEN;
    }
    else
    {
      return UML1ChangeableKind.CHANGEABLE;
    }
  }

  //---------------------------------------------------------------------------
  private UML1AggregationKind toUMLAggregationKind(
    String aggregation
  ) {
    if ("composite".equals(aggregation))
    {
      return UML1AggregationKind.COMPOSITE;
    }
    else if ("aggregate".equals(aggregation))
    {
      return UML1AggregationKind.AGGREGATE;
    }
    else
    {
      return UML1AggregationKind.NONE;
    }
  }

  //---------------------------------------------------------------------------
  private UML1ParameterDirectionKind toUMLParameterDirectionKind(
    String kind
  ) {
    if ("inout".equals(kind))
    {
      return UML1ParameterDirectionKind.INOUT;
    }
    else if ("out".equals(kind))
    {
      return UML1ParameterDirectionKind.OUT;
    }
    else if ("return".equals(kind))
    {
      return UML1ParameterDirectionKind.RETURN;
    }
    else
    {
      return UML1ParameterDirectionKind.IN;
    }
  }

  //---------------------------------------------------------------------------
  private Locator locator = null;
  private List scope = null;
  private Stack classStack = null;
  private Stack referenceStack = null;
  private XMIReferenceResolver resolver = null;
  private XMIImporter_1 importer = null;
  private PrintStream infos = null;
  private PrintStream errors = null;
  private PrintStream warnings = null;
  private boolean diagramInfoDetected = false;
}

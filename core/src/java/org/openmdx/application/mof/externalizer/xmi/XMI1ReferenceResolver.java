/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMI1ReferenceResolver.java,v 1.1 2009/01/13 02:10:41 wfro Exp $
 * Description: XMI1 Reference Resolver
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:41 $
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
package org.openmdx.application.mof.externalizer.xmi;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Comment;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Generalization;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1TagDefinition;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1VisibilityKind;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

@SuppressWarnings("unchecked")
public class XMI1ReferenceResolver
  implements ContentHandler, XMIReferenceResolver {

  //---------------------------------------------------------------------------
  public XMI1ReferenceResolver(
      Map xmiReferences,
      PrintStream errors
  ) {
      this.xmiReferences = xmiReferences;
      this.errors = errors;
  }

  //---------------------------------------------------------------------------
  public String lookupXMIId(
    String xmiId
  ) {
    return (String)xmiReferences.get(xmiId);
  }

  //---------------------------------------------------------------------------
  public UML1Generalization lookupGeneralization(
    String xmiId
  ) {
    return (UML1Generalization)this.generalizations.get(xmiId);
  }

  //---------------------------------------------------------------------------
  public UML1Comment lookupComment(
    String xmiId
  ) {
    return (UML1Comment)this.comments.get(xmiId);
  }

  //---------------------------------------------------------------------------
  public UML1TagDefinition lookupTagDefinition(
    String xmiId
  ) {
    return (UML1TagDefinition)this.tagDefinitions.get(xmiId);
  }

  //---------------------------------------------------------------------------
  public String lookupProject(
      String packageName
  ) {
      return (String)this.projects.get(packageName);
  }

    //---------------------------------------------------------------------------
    private void error(
        String message
    ) {
        this.errors.println("ERROR: " + message);
        this.hasErrors = true;
    }

  //---------------------------------------------------------------------------
  public boolean hasErrors(
  ) {
      return this.hasErrors;
  }

  //---------------------------------------------------------------------------
  public void startDocument(
  ) throws SAXException {
    this.scope = new ArrayList();
    this.generalizations = new HashMap();
    this.tagDefinitions = new HashMap();
    this.comments = new HashMap();
    this.referenceStack = new Stack();
    this.projects = new HashMap();
    this.hasErrors = false;
  }

  //---------------------------------------------------------------------------
  public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
  {
    if (XMI1Constants.PACKAGE.equals(qName))
    {
      // add new scope
      scope.add(atts.getValue("name"));
    }
    else if (XMI1Constants.GENERALIZATION_PARENT.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.GENERALIZATION_PARENT);
    }
    else if (XMI1Constants.GENERALIZATION_CHILD.equals(qName))
    {
      this.referenceStack.push(XMI1Constants.GENERALIZATION_CHILD);
    }
    else if (XMI1Constants.GENERALIZATION.equals(qName))
    {
      if (atts.getValue("xmi.id") != null)
      {
        // found new definiton
        UML1Generalization generalization = new UML1Generalization(
          "",
          "",
          "",
          UML1VisibilityKind.PUBLIC,
          new Boolean(atts.getValue("isSpecification")).booleanValue()
        );
        // parent maybe defined as XML attribute
        if (atts.getValue("parent") != null)
        {
          generalization.setParent(atts.getValue("parent"));
        }
        // child maybe defined as XML attribute
        if (atts.getValue("child") != null)
        {
          generalization.setChild(atts.getValue("child"));
        }
        this.generalizations.put(
          atts.getValue("xmi.id"),
          generalization
        );
        this.lastGeneralizationId = atts.getValue("xmi.id");
      }
    }
    else if (XMI1Constants.TAG_DEFINITION.equals(qName))
    {
      if (atts.getValue("xmi.id") != null)
      {
        // found new definition
        this.tagDefinitions.put(
          atts.getValue("xmi.id"),
          new UML1TagDefinition(
            "",
            atts.getValue("name"),
            "",
            UML1VisibilityKind.PUBLIC,
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            atts.getValue("tagType")
          )
        );
      }
    }
    else if (XMI1Constants.COMMENT.equals(qName))
    {
      if (atts.getValue("xmi.id") != null)
      {
        // found new definition
        this.comments.put(
          atts.getValue("xmi.id"),
          new UML1Comment(
            "",
            "",
            "",
            UML1VisibilityKind.PUBLIC,
            Boolean.valueOf(atts.getValue("isSpecification")).booleanValue(),
            atts.getValue("body")
          )
        );
      }
    }
    else if (XMI1Constants.CLASS.equals(qName))
    {
      if (!this.referenceStack.empty()) {
        String ref = (String)this.referenceStack.peek();
        if (XMI1Constants.GENERALIZATION_PARENT.equals(ref))
        {
          ((UML1Generalization)this.generalizations.get(this.lastGeneralizationId)).setParent(atts.getValue("xmi.idref"));
        }
        else if (XMI1Constants.GENERALIZATION_CHILD.equals(ref))
        {
          ((UML1Generalization)this.generalizations.get(this.lastGeneralizationId)).setChild(atts.getValue("xmi.idref"));
        }
      }
    }

    if (atts.getValue("xmi.id") != null) {
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
      sb.append(atts.getValue("name"));
      xmiReferences.put(
        atts.getValue("xmi.id"),
        sb.toString()
      );

//      System.out.println("xmi.id=" + atts.getValue("xmi.id") + " maps to name=" + sb.toString());
    }
  }

  //---------------------------------------------------------------------------
  public void endElement(String namespaceURI, String localName, String qName)
  {
    if (XMI1Constants.PACKAGE.equals(qName))
    {
      // close current scope
      scope.remove(scope.size()-1);
    }
    else if (XMI1Constants.GENERALIZATION_PARENT.equals(qName))
    {
      this.referenceStack.pop();
    }
    else if (XMI1Constants.GENERALIZATION_CHILD.equals(qName))
    {
      this.referenceStack.pop();
    }
  }

  //---------------------------------------------------------------------------
  public Locator getLocator(
  ) {
    return this.locator;
  }

  //---------------------------------------------------------------------------
  public void setDocumentLocator(Locator arg0) {
    this.locator = arg0;
  }

  //---------------------------------------------------------------------------
  public void endDocument() throws SAXException {
      //
  }

  //---------------------------------------------------------------------------
  public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
      //
  }
  

  //---------------------------------------------------------------------------
  public void endPrefixMapping(String arg0) throws SAXException {
      //
  }

  //---------------------------------------------------------------------------
  public void ignorableWhitespace(char[] arg0, int arg1, int arg2)
    throws SAXException {
      //
  }

  //---------------------------------------------------------------------------
  public void processingInstruction(String arg0, String arg1)
    throws SAXException {
      //
  }

  //---------------------------------------------------------------------------
  public void skippedEntity(String arg0) throws SAXException {
      //
  }

  //---------------------------------------------------------------------------
  public void startPrefixMapping(String arg0, String arg1)
    throws SAXException {
      //
  }
  

    //---------------------------------------------------------------------------
    public void parse(
        String uri
    ) throws Exception {
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
    private final PrintStream errors;
    private final Map xmiReferences;
    private Locator locator = null;
    private List scope;
    private Map tagDefinitions = null;
    private Map generalizations = null;
    private Map comments = null;
    private String lastGeneralizationId = null;
    private Stack referenceStack = null;
    private Map projects = null;
    private boolean hasErrors = false;
}

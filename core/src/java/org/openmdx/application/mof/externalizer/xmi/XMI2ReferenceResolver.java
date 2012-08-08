/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMI2ReferenceResolver.java,v 1.3 2009/03/04 18:44:38 wfro Exp $
 * Description: XMI2 Reference Resolver
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/04 18:44:38 $
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
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Comment;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Generalization;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1TagDefinition;
import org.openmdx.base.exception.ServiceException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

@SuppressWarnings("unchecked")
public class XMI2ReferenceResolver
    implements ContentHandler, XMIReferenceResolver {

    //---------------------------------------------------------------------------
    public XMI2ReferenceResolver(
        Map xmiReferences,
        Stack scope,
        Map pathMap,
        PrintStream infos,
        PrintStream warnings,
        PrintStream errors
    ) {
        this.xmiReferences = xmiReferences;
        this.scope = scope;
        this.pathMap = pathMap;
        this.infos = infos;
        this.warnings = warnings;
        this.errors = errors;
        this.projects = new HashMap();
    }

    //---------------------------------------------------------------------------
    /**
     * Retrieves the fully qualified name of the model element identified by a 
     * given xmiId
     * @param xmiId the xmi.id that identifies desired model element
     * @return the fully qualified name of the model element
     */
    public String lookupXMIId(
        String xmiId
    ) {
        return (String)xmiReferences.get(xmiId);
    }

    //---------------------------------------------------------------------------
    public UML1Generalization lookupGeneralization(String xmiId) {
        return null;
    }

    //---------------------------------------------------------------------------
    public UML1Comment lookupComment(String xmiId) {
        return null;
    }

    //---------------------------------------------------------------------------
    public UML1TagDefinition lookupTagDefinition(String xmiId) {
        return null;
    }

    //---------------------------------------------------------------------------
    public String lookupProject(
        String packageName
    ) {
        return (String)this.projects.get(packageName);
    }

    //---------------------------------------------------------------------------
    public void startDocument(
    ) throws SAXException {
        this.hasErrors = false;
    }

    //---------------------------------------------------------------------------
    private void error(
        String message
    ) {
        this.errors.println("ERROR:  " + message);
        this.hasErrors = true;
    }

    //---------------------------------------------------------------------------
//    private void info(
//        String message
//    ) {
//        this.errors.println("INFO:   " + message);
//    }
//    
    //---------------------------------------------------------------------------
    public boolean hasErrors(
    ) {
        return this.hasErrors;
    }

    //---------------------------------------------------------------------------
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts
    ) {
        if(
            "ownedMember".equals(qName) ||
            "packagedElement".equals(qName)
        ) {
            scope.push(atts.getValue("name"));
            if(
                "uml:Class".equals(atts.getValue("xmi:type")) ||
                "uml:PrimitiveType".equals(atts.getValue("xmi:type"))
            ) {
                if (atts.getValue("xmi:id") != null) {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < scope.size(); i++) {
                        if(i > 0) sb.append("::");
                        sb.append(scope.get(i));
                    }
                    xmiReferences.put(
                        atts.getValue("xmi:id"),
                        sb.toString()
                    );
                }
            }
        }
        // UML package reference
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
                    String path = Pattern.compile("%20").matcher(href.getPath()).replaceAll(" ");
                    String packageName = null;
                    if("platform".equals(scheme) && path.startsWith("/resource/")) {
                        path = path.substring("/resource/".length());
                        String projectName = path.substring(0, path.indexOf("/"));
                        packageName = path.substring(path.indexOf("/") + 1);
                        // register with package/project mapping
                        this.projects.put(
                            packageName,
                            projectName
                        );
                    }
                    else {
                        packageName = path;
                    }
                    String projectName = this.lookupProject(packageName);
                    if((projectName != null) && (this.pathMap.get(projectName) == null)) {
                        this.error("Referenced project '" + projectName + "' not defined as pathMapSymbol");
                    }
                    else {
                        String packageURI = projectName == null
                            ? this.uri.substring(0, this.uri.lastIndexOf('/') + 1) + packageName
                            : this.pathMap.get(projectName) + packageName;
                        XMI2ReferenceResolver nestedReferenceResolver =
                            new XMI2ReferenceResolver(
                                this.xmiReferences,
                                this.scope,
                                this.pathMap,
                                this.infos,
                                this.warnings,
                                this.errors
                            );
                        nestedReferenceResolver.parse(packageURI);
                    }
                }
                catch(Exception e) {
                    this.error("Can not process nested model " + atts.getValue("href") + ". Reason=" + e.getMessage());
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
        if(
            "ownedMember".equals(qName) || 
            "packagedElement".equals(qName)
        ) {
            scope.pop();
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
        this.uri = uri;
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
    public String toString(
    ) {
        return
            "xmiReferences=" + xmiReferences + "\n" +
            "projects=" + projects;
    }

    //---------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------
    private final PrintStream infos;
    private final PrintStream warnings;
    private final PrintStream errors;
    private Locator locator = null;
    private String uri = null;
    private final Stack scope;
    private final Map xmiReferences;
    private final Map pathMap;
    private final Map projects;
    private boolean hasErrors = false;

}

//--- End of File -----------------------------------------------------------

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: XMI2 Reference Resolver
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
package org.openmdx.application.mof.externalizer.xmi;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.application.mof.externalizer.xmi.uml1.UML1AssociationEnd;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Comment;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1Generalization;
import org.openmdx.application.mof.externalizer.xmi.uml1.UML1TagDefinition;
import org.openmdx.base.exception.ServiceException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class XMI2ReferenceResolver
    implements ContentHandler, XMIReferenceResolver {

    //---------------------------------------------------------------------------
    public XMI2ReferenceResolver(
        Map<String,String> xmiReferences,
        Stack<String> scope,
        Map<String,String> pathMap,
        PrintStream infos,
        PrintStream warnings,
        PrintStream errors, 
        Map<String, UML1AssociationEnd> umlAssociationEnds
    ) {
        this.xmiReferences = xmiReferences;
        this.umlAssociationEnds = umlAssociationEnds;
        this.scope = scope;
        this.pathMap = pathMap;
        this.infos = infos;
        this.warnings = warnings;
        this.errors = errors;
        this.projects = new HashMap<String,String>();
    }

    /**
     * Get href as URI.
     * 
     * @param href
     * @return
     */
    public URI hrefToURI(
        String href
    ) {
        URI hrefURI = null;
        String schema = null;
        String value = null;
        try {
            value = href;
            value = value.replace(" ", "%20");
            // Convert relative paths to platform resource paths
            schema = "";
            while(value.startsWith("../") || value.startsWith("..\\")) {
                value = value.substring(3);
                schema = "platform:/resource/";
            }                 
            hrefURI = new URI(schema + value);
        } catch(URISyntaxException e) {
            this.error("Reference is not a valid URI >" + schema + value + "<");
        }
        return hrefURI;
    }

    /**
     * Retrieves the fully qualified name of the model element identified by a 
     * given xmiId.
     * 
     * @param xmiId the xmi.id that identifies desired model element
     * @return the fully qualified name of the model element
     */
    public String lookupXMIId(
        String xmiId
    ) {
        return this.xmiReferences.get(xmiId);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.mof.externalizer.xmi.XMIReferenceResolver#lookupGeneralization(java.lang.String)
     */
    @Override
    public UML1Generalization lookupGeneralization(
        String xmiId
      ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.mof.externalizer.xmi.XMIReferenceResolver#lookupComment(java.lang.String)
     */
    @Override
    public UML1Comment lookupComment(
        String xmiId
    ) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.mof.externalizer.xmi.XMIReferenceResolver#lookupTagDefinition(java.lang.String)
     */
    @Override
    public UML1TagDefinition lookupTagDefinition(
        String xmiId
    ) {
        return null;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.application.mof.externalizer.xmi.XMIReferenceResolver#lookupAssociationEnd(java.lang.String)
	 */
    @Override
	public UML1AssociationEnd lookupAssociationEnd(
	    String xmiId
	) {
		return this.umlAssociationEnds == null ? null : this.umlAssociationEnds.get(xmiId);
	}

    /* (non-Javadoc)
     * @see org.openmdx.application.mof.externalizer.xmi.XMIReferenceResolver#lookupProject(java.lang.String)
     */
    @Override
    public String lookupProject(
        String packageName
    ) {
        return this.projects.get(packageName);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    @Override
    public void startDocument(
    ) throws SAXException {
        this.hasErrors = false;
        this.elementStack = this.umlAssociationEnds == null ? null : new Stack<Object>();
    }

    /**
     * Write error.
     * 
     * @param message
     */
    private void error(
        String message
    ) {
        this.errors.println("ERROR:  " + message);
        this.hasErrors = true;
    }

    /**
     * Get current scope as qualified name.
     * 
     * @return
     */
    private String getScopeAsQualifiedName(
    ) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.scope.size(); i++) {
            if(i > 0) sb.append("::");
            sb.append(this.scope.get(i));
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.mof.externalizer.xmi.XMIReferenceResolver#hasErrors()
     */
    @Override
    public boolean hasErrors(
    ) {
        return this.hasErrors;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts
    ) {
    	Object element = DUMMY;
    	if(
    		("ownedEnd".equals(qName) || "ownedAttribute".equals(qName)) && 
    		(atts.getValue("association") != null || atts.getValue("aggregation") != null)
    	) {
    		if(this.umlAssociationEnds != null) {
    			UML1AssociationEnd umlAssociationEnd = new UML1AssociationEnd(
                    atts.getValue("xmi:id"),
                    atts.getValue("name"),
                    this.getScopeAsQualifiedName() + "::" + atts.getValue("name"),
                    XMI20Parser.toUMLVisibilityKind(atts.getValue("visibility")),
                    false,
                    atts.getValue("type")
                );
    			element = umlAssociationEnd;
    			this.umlAssociationEnds.put(umlAssociationEnd.getId(), umlAssociationEnd);
    		}
    	} else if("ownedAttribute".equals(qName)) { 
    		if(this.elementStack != null) {
	    		//
	    		// Might be a reference, might be a primitive type
	    		//
				element = new UML1AssociationEnd(
	                atts.getValue("xmi:id"),
	                atts.getValue("name"),
	                this.getScopeAsQualifiedName() + "::" + atts.getValue("name"),
	                XMI20Parser.toUMLVisibilityKind(atts.getValue("visibility")),
	                false,
	                atts.getValue("type")
	            );
    		}
    	} else if("type".equals(qName) && atts.getValue("href") != null) { 
    		if(this.elementStack != null) {
    			Object parent = this.elementStack.peek();
    			if(parent instanceof UML1AssociationEnd) {
    				UML1AssociationEnd umlAssociationEnd = (UML1AssociationEnd) parent;
    				String href = atts.getValue("href");
    				int h = href.indexOf('#');
    				int q = href.indexOf('?');
    				if(q > h && h >= 0) {
    					umlAssociationEnd.setParticipantId(
							href.substring(h + 1, q)
    					);
    				}
    			}
    		}
    	} else if("association".equals(qName)) {
    		if(this.elementStack != null) {
    			Object parent = this.elementStack.peek();
    			if(parent instanceof UML1AssociationEnd) {
    				UML1AssociationEnd umlAssociationEnd = (UML1AssociationEnd) parent;
        			this.umlAssociationEnds.put(umlAssociationEnd.getId(), umlAssociationEnd);
    			}
    		}
    	} else if("uml:Package".equals(qName)) {
            scope.push(atts.getValue("name"));
    	} else if(
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
        } else if(
        	((XMI2Configuration.importPackage() && "importedPackage".equals(qName)) || "importedElement".equals(qName) || "references".equals(qName)) &&
            ("uml:Package".equals(atts.getValue("xmi:type")) || "uml:Model".equals(atts.getValue("xmi:type"))) 
        ) {
            // UML package reference
            URI href = this.hrefToURI(atts.getValue("href"));
            if(href != null) {
                try {
                    String scheme = href.getScheme();
                    String path = href.getPath().replace("%20", " ");
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
                    } else {
                        packageName = path;
                    }
                    String projectName = this.lookupProject(packageName);
                    if((projectName != null) && (this.pathMap.get(projectName) == null)) {
                        this.error("Referenced project '" + projectName + "' not defined as pathMapSymbol");
                    } else {
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
                                this.errors, 
                                this.umlAssociationEnds
                            );
                        nestedReferenceResolver.parse(packageURI);
                    }
                } catch(Exception e) {
                    new ServiceException(e).log();
                    this.error("Can not process nested model " + atts.getValue("href") + ". Reason=" + e.getMessage());
                }
            }
        }
    	if(this.elementStack != null) {
    		this.elementStack.push(element);
    	}
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(
        String namespaceURI,
        String localName,
        String qName
    ) {
        if(
            "ownedMember".equals(qName) || 
            "packagedElement".equals(qName) ||
            "uml:Package".equals(qName)
        ) {
            scope.pop();
        }
        if(this.elementStack != null) {
        	this.elementStack.pop();
        }
    }

    /**
     * Get locator.
     * 
     * @return
     */
    public Locator getLocator(
    ) {
        return this.locator;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    @Override
    public void setDocumentLocator(
        Locator locator
    ) {
        this.locator = locator;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    @Override
    public void endDocument(
    ) throws SAXException {
        //
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(
        char[] arg0, 
        int arg1, 
        int arg2
    ) throws SAXException {
      //
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    @Override
    public void endPrefixMapping(
        String arg0
    ) throws SAXException {
        //
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    @Override
    public void ignorableWhitespace(
        char[] arg0, 
        int arg1, 
        int arg2
    ) throws SAXException {
        //
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public void processingInstruction(
        String arg0, 
        String arg1
    ) throws SAXException {
        //
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    @Override
    public void skippedEntity(
        String arg0
    ) throws SAXException {
        //
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(
        String arg0, 
        String arg1
    ) throws SAXException {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.mof.externalizer.xmi.XMIReferenceResolver#parse(java.lang.String)
     */
    @Override
    public void parse(
        String uri
    ) throws Exception {
        this.uri = uri;
        XMLReader reader = null;
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            reader = parser.getXMLReader();
        } catch (Exception ex) {
            ex.printStackTrace(this.errors);
            throw ex;
        }
        try {
            reader.setContentHandler(this);
            reader.parse(uri);
        } catch(Exception ex) {
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
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
    private final Stack<String> scope;
    private final Map<String,String> xmiReferences;
    private final Map<String, UML1AssociationEnd> umlAssociationEnds;
    private final Map<String,String> pathMap;
    private final Map<String,String> projects;
    private boolean hasErrors = false;
    private Stack<Object> elementStack = null;
    private final static Object DUMMY = new Object();
    
}

//--- End of File -----------------------------------------------------------

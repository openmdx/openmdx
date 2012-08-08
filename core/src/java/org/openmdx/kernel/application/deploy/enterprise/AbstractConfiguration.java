/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractConfiguration.java,v 1.1 2008/01/13 21:37:33 hburger Exp $
 * Description: Abstract Configuration
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/13 21:37:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.deploy.enterprise;

import java.util.ArrayList;
import java.util.Iterator;

import org.openmdx.kernel.application.configuration.Report;
import org.openmdx.kernel.environment.cci.VersionNumber;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract Configuration
 */
abstract class AbstractConfiguration {
    
  /**
   * Constructor 
   */
  protected AbstractConfiguration() {
    super();
  }

  public abstract void parseXml(Element element, Report report);

  public abstract void parseOpenMdxXml(Element element, Report report);

  public void verify(
    Report report
  ) {
      //
  }

  protected String createUniqueLocalApplicationContextLink(
    String moduleId,
    String componentId
  ) {
    return this.createUniqueApplicationContextLink(moduleId, componentId, "local");
  }

  protected String createUniqueRemoteApplicationContextLink(
    String moduleURI,
    String componentId
  ) {
    return this.createUniqueApplicationContextLink(moduleURI, componentId, "remote");
  }

  /**
   * Transforms the given XRI part to IRI-normal form. This method does not parse the given String;
   * it simply converts all '%' to '%25', and if <code>inXref</code> is true, also percent encodes
   * '#', '?' and '/'. 
   * @param s
   * @param inXref
   * @return
   */
  private static CharSequence toIRINormalForm(
      String s, 
      boolean inXref
  ) {
      StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '%') {
                sb.append("%25");
            } else if (inXref) {
                if (c == '#')
                    sb.append("%23");
                else if (c == '?')
                    sb.append("%3F");
                else if (c == '/')
                    sb.append("%2F");
                else
                    sb.append(c);
            } else {
                sb.append(c);
            }
        }
        return sb;
    }

  private String createUniqueApplicationContextLink(
    String moduleURI,
    String componentId,
    String linkId
  ) {
    return new StringBuilder(
    ).append(
        '('
    ).append(
        toIRINormalForm(moduleURI, true)
    ).append(
        ")/"
    ).append(
        toIRINormalForm(componentId, false)
    ).append(
        '/'
    ).append(
        toIRINormalForm(linkId, false)
    ).toString();
  }

  protected Element getUniqueChild(
    Element element,
    String tagName,
    Report report
  ) {
    Iterator<Element> children = getChildrenByTagName(element, tagName);

    if (children != null && children.hasNext()) {
      Element child = children.next();
      if (children.hasNext()) {
        report.addError("expected only one '" + tagName + "' tag");
      }
      return child;
    } else {
      report.addError("expected one '" + tagName + "' tag");
      return null;
    }
  }

  protected Element getOptionalChild(
    Element element,
    String tagName,
    Report report
  ) {
    Iterator<Element> goodChildren = getChildrenByTagName(element, tagName);

    if (goodChildren != null && goodChildren.hasNext()) {
      Element child = goodChildren.next();
      if (goodChildren.hasNext()) {
        report.addError("expected only one '" + tagName + "' tag");
      }
      return child;
    } else {
      return null;
    }
  }

  protected Iterator<Element> getChildrenByTagName(
    Element element,
    String tagName
  ) {
     if (element == null) return null;

     NodeList children = element.getChildNodes();
     ArrayList<Element> goodChildren = new ArrayList<Element>();
     for (int i=0; i<children.getLength(); i++) {
        Node currentChild = children.item(i);
        if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
            Element candidate = (Element)currentChild;
            if(candidate.getTagName().equals(tagName)) {
               goodChildren.add(candidate);
            }
        }
     }
     return goodChildren.iterator();
  }

  protected String getElementContent(
    Element element
  ) {
     if (element == null) {
       return null;
     } else {
         NodeList children = element.getChildNodes();
         StringBuilder result = new StringBuilder();
         for (
            int i = 0; 
            i < children.getLength(); 
            i++
         ) {
            if (
                children.item(i).getNodeType() == Node.TEXT_NODE ||
                children.item(i).getNodeType() == Node.CDATA_SECTION_NODE
            ){
                result.append(children.item(i).getNodeValue());
            } else if (
                children.item(i).getNodeType() == Node.COMMENT_NODE 
            ){
               // Ignore comment nodes
            } else {
                result.append(children.item(i).getFirstChild());
            }
         }
         return result.toString().trim();
     }
  }

  protected final static String REPORT_APPLICATION_NAME = "J2EE Application";
  protected final static VersionNumber REPORT_APPLICATION_VERSION = new VersionNumber("1.3");
  protected final static String REPORT_EJB_MODULE_NAME = "Enterprise JavaBeans (Module)";
  protected final static String REPORT_EJB_COMPONENT_NAME = "Enterprise JavaBeans (Component)";
  protected final static String REPORT_EJB_CLIENT_NAME = "Application Client";
  protected final static VersionNumber REPORT_EJB_VERSION = new VersionNumber("2.0");
  protected final static String REPORT_CONNECTOR_NAME = "Connector";
  protected final static VersionNumber REPORT_CONNECTOR_VERSION = new VersionNumber("1.0");
  protected final static String REPORT_WEB_APPLICATION_NAME = "Web Application";
  protected final static VersionNumber REPORT_WEB_APPLICATION_VERSION = new VersionNumber("2.3");
  
}

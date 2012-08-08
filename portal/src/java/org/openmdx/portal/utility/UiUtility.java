/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: UiUtility.java,v 1.16 2009/06/13 18:48:08 wfro Exp $
 * Description: UiUtility
 * Revision:    $Revision: 1.16 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2009/06/13 18:48:08 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of CRIXP Corp. nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
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
 * 
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.utility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.application.dataprovider.importer.XmlImporter;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.portal.text.conversion.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Command line utility which supports the management of openCRX ui configuration files:
 * <ul>
 *   <li>merge: merges locale separated ui files (en_US, de_DE) to one merged file.</li> 
 *   <li>split: splits a merged ui file to locale separated files.</li>
 * </ul>
 * <p>
 * 
 * @author wfro
 */
@SuppressWarnings("unchecked")
public class UiUtility {
    
  //-------------------------------------------------------------------------    
  public void exceptionThrown(
         Exception exception
  ){
     exception.printStackTrace();
  }

  //-------------------------------------------------------------------------  
  protected void init(
  ) throws Exception {
  }

  //-------------------------------------------------------------------------    
  private Map lookupElementDefinition(
      Document document,
      String elementDefinitionName
  ) {
      Map elementDefinition = new HashMap();
      NodeList elementDefinitionNodes = document.getElementsByTagName("ElementDefinition");
      for(int i = 0; i < elementDefinitionNodes.getLength();  i++) {
        org.w3c.dom.Node elementDefinitionNode = elementDefinitionNodes.item(i);
        org.w3c.dom.NamedNodeMap elementDefinitionNodeAttributes = elementDefinitionNode.getAttributes();
        org.w3c.dom.Attr elementDefinitionNodeName = (org.w3c.dom.Attr)elementDefinitionNodeAttributes.getNamedItem("name");
        if(
          (elementDefinitionNodeName != null) &&
          elementDefinitionName.equals(elementDefinitionNodeName.getValue())
        ) {
            org.w3c.dom.NodeList textNodes = elementDefinitionNode.getChildNodes();
            for(int j = 0; j < textNodes.getLength(); j++) {
                org.w3c.dom.Node textNode = textNodes.item(j);
                if(textNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    if("Text".equals(textNode.getNodeName())) {
                        org.w3c.dom.NamedNodeMap textNodeAttributes = textNode.getAttributes();
                        org.w3c.dom.Attr textNodeType = (org.w3c.dom.Attr)textNodeAttributes.getNamedItem("type");
                        if(
                            (textNodeType != null) &&
                            ("Label".equals(textNodeType.getValue()) || "ShortLabel".equals(textNodeType.getValue()) || "ToolTip".equals(textNodeType.getValue()))
                        ) {
                            org.w3c.dom.NodeList attributeNodes = textNode.getChildNodes();
                            for(int k = 0; k < attributeNodes.getLength(); k++) {
                                org.w3c.dom.Node attributeNode = attributeNodes.item(k);
                                if(attributeNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                    if(attributeNode.hasChildNodes()) {
                                        elementDefinition.put(
                                            attributeNode.getNodeName() + "_" + textNodeType.getValue(),
                                            attributeNode.getFirstChild().getNodeValue()
                                        );
                                    }
                                }
                            }
                        }
                    }
                    else {
                        System.out.println("WARNING: skipping node " + textNode.getNodeName());
                    }
                }
            }
        }
      }
      return elementDefinition;
  }
  
  //-------------------------------------------------------------------------    
  private Map lookupElementDefinitionByType(
      String type,
      Document document,
      String elementDefinitionName,
      String alternateId
  ) {
      Map alternateElementDefinition = new HashMap();
      NodeList alternateElementDefinitionNodes = document.getElementsByTagName(type);
      for(int i = 0; i < alternateElementDefinitionNodes.getLength();  i++) {
        org.w3c.dom.Node alternateElementDefinitionNode = alternateElementDefinitionNodes.item(i);
        org.w3c.dom.NamedNodeMap elementDefinitionNodeAttributes = alternateElementDefinitionNode.getAttributes();
        org.w3c.dom.Attr alternateElementDefinitionNodeName = (org.w3c.dom.Attr)elementDefinitionNodeAttributes.getNamedItem("name");
        org.w3c.dom.Attr alternateElementDefinitionNodeId = (org.w3c.dom.Attr)elementDefinitionNodeAttributes.getNamedItem("id");
        if(
          (alternateElementDefinitionNodeName != null) &&
          elementDefinitionName.equals(alternateElementDefinitionNodeName.getValue()) &&
          alternateId.equals(alternateElementDefinitionNodeId.getValue())
        ) {
            org.w3c.dom.NodeList textNodes = alternateElementDefinitionNode.getChildNodes();
            for(int j = 0; j < textNodes.getLength(); j++) {
                org.w3c.dom.Node textNode = textNodes.item(j);
                if(textNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    if("Text".equals(textNode.getNodeName())) {
                        org.w3c.dom.NamedNodeMap textNodeAttributes = textNode.getAttributes();
                        org.w3c.dom.Attr textNodeType = (org.w3c.dom.Attr)textNodeAttributes.getNamedItem("type");
                        if(
                            (textNodeType != null) &&
                            ("Label".equals(textNodeType.getValue()) || "ShortLabel".equals(textNodeType.getValue()) || "ToolTip".equals(textNodeType.getValue()))
                        ) {
                            org.w3c.dom.NodeList attributeNodes = textNode.getChildNodes();
                            for(int k = 0; k < attributeNodes.getLength(); k++) {
                                org.w3c.dom.Node attributeNode = attributeNodes.item(k);
                                if(attributeNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                    if(attributeNode.hasChildNodes()) {
                                        alternateElementDefinition.put(
                                            attributeNode.getNodeName() + "_" + textNodeType.getValue(),
                                            attributeNode.getFirstChild().getNodeValue()
                                        );
                                    }
                                }
                            }
                        }
                    }
                    else {
                        System.out.println("WARNING: skipping node " + textNode.getNodeName());
                    }
                }
            }
        }
      }
      return alternateElementDefinition;
  }
  
  //-------------------------------------------------------------------------    
  private void writeAsSchema(
      Writer w,
      Writer fw,
      Map<Path,MappedRecord> elementDefinitions,
      int localeIndex
  ) throws ServiceException, IOException {
      String providerName ="CRX";
      String segmentName = "Standard";
      if(!elementDefinitions.isEmpty()) {
          MappedRecord obj = elementDefinitions.values().iterator().next();
          Path objPath = ObjectHolder_2Facade.getPath(obj);
          providerName = objPath.get(2);
          segmentName = objPath.get(4);
      }
      String s = null;
      s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
          "<org.openmdx.base.Authority xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" name=\"org:openmdx:ui1\" xsi:noNamespaceSchemaLocation=\"xri:+resource/org/openmdx/ui1/xmi/ui1.xsd\">\n" +
          "  <_object/>\n" + 
          "  <_content>\n" +
          "    <provider>\n" +
          "      <org.openmdx.base.Provider qualifiedName=\"" + providerName + "\" _operation=\"null\">\n" +
          "        <_object/>\n" +
          "        <_content>\n" +
          "          <segment>\n" +
          "            <org.openmdx.ui1.Segment qualifiedName=\"" + segmentName + "\" _operation=\"null\">\n" +
          "              <_object/>\n" +
          "              <_content>\n" +
          "                <elementDefinition>\n";
      w.write(s, 0, s.length());
      for(
          Iterator<MappedRecord> i = elementDefinitions.values().iterator(); 
          i.hasNext(); 
      ) {
          MappedRecord element = i.next();
          ObjectHolder_2Facade elementFacade;
          try {
	          elementFacade = ObjectHolder_2Facade.newInstance(element);
          }
          catch (ResourceException e) {
        	  throw new ServiceException(e);
          }
          String elementDefinitionType = elementFacade.getObjectClass();
          if(
              "org:openmdx:ui1:ElementDefinition".equals(elementDefinitionType) ||
              "org:openmdx:ui1:AlternateElementDefinition".equals(elementDefinitionType) ||
              "org:openmdx:ui1:AdditionalElementDefinition".equals(elementDefinitionType)
          ) {
              boolean allLocales = localeIndex < 0;
              // write only if either label or toolTip for specified locale exists
              if(
                  allLocales || 
                  ((elementFacade.attributeValues("label").get(localeIndex) != null) && !"".equals(elementFacade.attributeValues("label").get(localeIndex))) ||
                  ((elementFacade.attributeValues("shortLabel").get(localeIndex) != null) && !"".equals(elementFacade.attributeValues("shortLabel").get(localeIndex))) ||
                  ((elementFacade.attributeValues("toolTip").get(localeIndex) != null) && !"".equals(elementFacade.attributeValues("toolTip").get(localeIndex)))
              ) {
                  boolean isNested = false;
                  if("org:openmdx:ui1:AlternateElementDefinition".equals(elementDefinitionType)) {
                      s = "                  <org.openmdx.ui1.ElementDefinition name=\"" + elementFacade.getPath().getParent().getParent().getBase() + "\" _operation=\"null\">\n" +
                          "                    <_object/>\n" +
                          "                    <_content>\n" +
                          "                      <alternateElementDefinition>\n" +
                          "                        <org.openmdx.ui1.AlternateElementDefinition id=\"" + elementFacade.getPath().getBase() + "\" _operation=\"create\">\n" +
                          "                          <_object>\n";
                      isNested = true;
                  }
                  else if("org:openmdx:ui1:AdditionalElementDefinition".equals(elementDefinitionType)) {
                      s = "                  <org.openmdx.ui1.ElementDefinition name=\"" + elementFacade.getPath().getParent().getParent().getBase() + "\" _operation=\"null\">\n" +
                          "                    <_object/>\n" +
                          "                    <_content>\n" +
                          "                      <additionalElementDefinition>\n" +
                          "                        <org.openmdx.ui1.AdditionalElementDefinition id=\"" + elementFacade.getPath().getBase() + "\" _operation=\"create\">\n" +
                          "                          <_object>\n";
                      isNested = true;
                  }
                  else {
                      s = "                  <org.openmdx.ui1.ElementDefinition name=\"" + elementFacade.getPath().getBase() + "\" _operation=\"create\">\n" +
                          "                    <_object>\n";
                      isNested = false;
                  }
                  w.write(s, 0, s.length());
    
                  // <label>
                  boolean tagWritten = false;
                  int startIndex = allLocales ? 0 : localeIndex;
                  int endIndex = allLocales ? this.locales.size()-1 : localeIndex;
                  for(
                      int j = startIndex;
                      j < endIndex + 1;
                      j++
                  ) {
                      String label = elementFacade.attributeValues("label").size() > j
                          ? (String)elementFacade.attributeValues("label").get(j)
                          : "";
                      if(!"".equals(label)) {
                          String indent = isNested ? "      " : "";        
                          // <label>
                          if(!tagWritten) {
                              s = indent + "                      <label>\n";
                              w.write(s, 0, s.length());
                              tagWritten = true;
                          }                          
                          // <item>
                          s = indent + "                        <_item>";
                          w.write(s, 0, s.length());
                          s = label;
                          fw.write(s, 0, s.length());
                          s = "</_item>\n";
                          w.write(s, 0, s.length());                          
                      }
                  }
                  // </label>
                  if(tagWritten) {
                      String indent = isNested ? "      " : "";
                      s = indent + "                      </label>\n";
                      w.write(s, 0, s.length());
                  }
                  // <shortLabel>
                  tagWritten = false;
                  for(
                      int j = startIndex;
                      j < endIndex + 1;
                      j++
                  ) {
                      String shortLabel = elementFacade.attributeValues("shortLabel").size() > j
                          ? (String)elementFacade.attributeValues("shortLabel").get(j)
                          : "";
                      if(!"".equals(shortLabel)) {
                          String indent = isNested ? "      " : "";
                          // <shortLabel>
                          if(!tagWritten) {
                              s = indent + "                      <shortLabel>\n";
                              w.write(s, 0, s.length());
                              tagWritten = true;
                          }                          
                          // <item>
                          s = indent + "                        <_item>";
                          w.write(s, 0, s.length());
                          s = shortLabel;
                          fw.write(s, 0, s.length());
                          s = "</_item>\n";
                          w.write(s, 0, s.length());                          
                      }
                  }
                  // </shortLabel>
                  if(tagWritten) {
                      String indent = isNested ? "      " : "";
                      s = indent + "                      </shortLabel>\n";
                      w.write(s, 0, s.length());
                  }                                    
                  // <toolTip>
                  tagWritten = false;
                  for(
                      int j = startIndex;
                      j < endIndex + 1;
                      j++
                  ) {
                      String toolTip = elementFacade.attributeValues("toolTip").size() > j
                          ? (String)elementFacade.attributeValues("toolTip").get(j)
                          : "";
                      if(!"".equals(toolTip)) {
                          String indent = isNested ? "      " : "";
        
                          // <toolTip>
                          if(!tagWritten) {
                              s = indent + "                      <toolTip>\n";
                              w.write(s, 0, s.length());
                              tagWritten = true;
                          }
                          
                          // item
                          s = indent + "                        <_item>";
                          w.write(s, 0, s.length());
                          s = toolTip;
                          fw.write(s, 0, s.length());
                          s = "</_item>\n";
                          w.write(s, 0, s.length());
                      }
                  }        
                  // </toolTip>
                  if(tagWritten) {
                      String indent = isNested ? "      " : "";
                      s = indent + "                      </toolTip>\n";
                      w.write(s, 0, s.length());
                  }
                  
                  if("org:openmdx:ui1:AlternateElementDefinition".equals(elementDefinitionType)) {
                      s = "                          </_object>\n" +
                          "                          <_content/>\n" +
                          "                        </org.openmdx.ui1.AlternateElementDefinition>\n" +
                          "                      </alternateElementDefinition>\n" +
                          "                    </_content>\n" +
                          "                  </org.openmdx.ui1.ElementDefinition>\n";
                  }
                  else if("org:openmdx:ui1:AdditionalElementDefinition".equals(elementDefinitionType)) {
                      s = "                          </_object>\n" +
                          "                          <_content/>\n" +
                          "                        </org.openmdx.ui1.AdditionalElementDefinition>\n" +
                          "                      </additionalElementDefinition>\n" +
                          "                    </_content>\n" +
                          "                  </org.openmdx.ui1.ElementDefinition>\n";
                  }
                  else {
                      s = "                    </_object>\n" +
                          "                    <_content/>\n" +
                          "                  </org.openmdx.ui1.ElementDefinition>\n";
                  }
                  w.write(s, 0, s.length());
              }
          }
      }
      s = "                </elementDefinition>\n" +
          "              </_content>\n" +
          "            </org.openmdx.ui1.Segment>\n" +
          "          </segment>\n" +
          "        </_content>\n" +
          "      </org.openmdx.base.Provider>\n" +
          "    </provider>\n" +
          "  </_content>\n" +
          "</org.openmdx.base.Authority>\n";
      w.write(s, 0, s.length());
  }

  //-------------------------------------------------------------------------
  /**
   * @param localIndex if < 0 ==> read all locales, else read elements of 
   *        specified locale
   */
  private void readAsSchema(
      File file,
      Map<Path,MappedRecord> mergedElementDefinitions,
      int localeIndex
  ) throws ServiceException {
      Map<Path,MappedRecord> elementDefinitions = new LinkedHashMap<Path,MappedRecord>();
      if(file.exists()) {
          XmlImporter importer = new XmlImporter(
            elementDefinitions,
            false
          );
          System.out.println("loading " + file);
          try {
            importer.process(
              new String[]{file.getAbsolutePath()}
            );
          }
          catch(ServiceException e) {
            e.log();
            System.out.println("STATUS: " + e.getMessage());
          }
          catch(Exception e) {
              new ServiceException(e).log();
              System.out.println("STATUS: " + e.getMessage());
          }
      }
      
      // merge entries
      Set<Path> keySet = localeIndex <= 0 ? elementDefinitions.keySet() : mergedElementDefinitions.keySet();
      try {
          for(Iterator j = keySet.iterator(); j.hasNext(); ) {
            Path key = (Path)j.next();
            // merge entry
            if(mergedElementDefinitions.get(key) != null) {
              MappedRecord mergedElementDefinition = mergedElementDefinitions.get(key);
              String mergedElementDefinitionType = ObjectHolder_2Facade.getObjectClass(mergedElementDefinition);
              if(
                "org:openmdx:ui1:ElementDefinition".equals(mergedElementDefinitionType) ||
                "org:openmdx:ui1:AlternateElementDefinition".equals(mergedElementDefinitionType) ||
                "org:openmdx:ui1:AdditionalElementDefinition".equals(mergedElementDefinitionType)
              ) {
                  MappedRecord elementDefinition = elementDefinitions.get(key);
                  ObjectHolder_2Facade elementDefinitionFacade = ObjectHolder_2Facade.newInstance(elementDefinition);
                  ObjectHolder_2Facade mergedElementDefinitionFacade = ObjectHolder_2Facade.newInstance(mergedElementDefinition);
                  if(elementDefinitionFacade.getAttributeValues("label") != null) {
                	  mergedElementDefinitionFacade.attributeValues("label").add(
                      (elementDefinitionFacade != null) && (elementDefinitionFacade.attributeValues("label").size() > 0)
                        ? elementDefinitionFacade.attributeValue("label")
                        : "" // empty string as default
                    );
                  }
                  if(mergedElementDefinitionFacade.getAttributeValues("shortLabel") != null) {
                	  mergedElementDefinitionFacade.attributeValues("shortLabel").add(
                      (elementDefinitionFacade != null) && (elementDefinitionFacade.attributeValues("shortLabel").size() > 0)
                        ? elementDefinitionFacade.attributeValue("shortLabel")
                        : "" // empty string as default
                    );
                  }
                  if(mergedElementDefinitionFacade.getAttributeValues("toolTip") != null) {
                	  mergedElementDefinitionFacade.attributeValues("toolTip").add(
                      (elementDefinitionFacade != null) && (elementDefinitionFacade.attributeValues("toolTip").size() > 0)
                        ? elementDefinitionFacade.attributeValue("toolTip")
                        : ""  // empty string as default
                    );
                  }
              }
            }
            // add if it does not exist. Only add for locale=0 (en_US)
            else if(localeIndex <= 0) {
                mergedElementDefinitions.put(
                    key,
                    elementDefinitions.get(key)
                );
            }
            // locale > 0 requires that locale=0 exists. Complain if it
            // does not
            else {
              System.err.println("entry " + key + " of locale " + locales.get(localeIndex) + " has no corresponding entry for locale " + locales.get(0) + ". Not loading");
            }
          }
      }
      catch(Exception e) {
          System.err.println("Can not import. Reason is " + e.getMessage());
      }
  }
  
  //-------------------------------------------------------------------------
  private void readAsTable(
      File file,
      File templateFile,
      Map<Path,MappedRecord> elementDefinitions
  ) throws ServiceException {          
      XmlImporter importer = new XmlImporter(
          elementDefinitions,
          false
      );
      System.out.println("loading " + templateFile);
      try {
          importer.process(
              new String[]{templateFile.getAbsolutePath()}
          );
      }
      catch(ServiceException e) {
          e.log();
          System.out.println("STATUS: " + e.getMessage());
      }
      catch(Exception e) {
          new ServiceException(e).log();
          System.out.println("STATUS: " + e.getMessage());
      }
      try {
          System.out.println("loading " + file.getAbsolutePath());
          org.w3c.dom.Document mergedElementDefinitions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
          for(Iterator<MappedRecord> i = elementDefinitions.values().iterator(); i.hasNext(); ) {
              MappedRecord elementDefinition = i.next();
              ObjectHolder_2Facade elementDefinitionFacade;
              try {
	              elementDefinitionFacade = ObjectHolder_2Facade.newInstance(elementDefinition);
              }
              catch (ResourceException e) {
            	  throw new ServiceException(e);
              }
              String elementDefinitionType = elementDefinitionFacade.getObjectClass();
              Map mergedElementDefinition = null;
              if("org:openmdx:ui1:ElementDefinition".equals(elementDefinitionType)) {
                  mergedElementDefinition = 
                      this.lookupElementDefinition(
                          mergedElementDefinitions, 
                          elementDefinitionFacade.getPath().getBase()
                      );
              }
              else if("org:openmdx:ui1:AlternateElementDefinition".equals(elementDefinitionType)) {
                  mergedElementDefinition = 
                      this.lookupElementDefinitionByType(
                          "AlternateElementDefinition",
                          mergedElementDefinitions, 
                          elementDefinitionFacade.getPath().getParent().getParent().getBase(), 
                          elementDefinitionFacade.getPath().getBase()
                      );
              }
              else if("org:openmdx:ui1:AdditionalElementDefinition".equals(elementDefinitionType)) {
                  mergedElementDefinition = 
                      this.lookupElementDefinitionByType(
                          "AdditionalElementDefinition",
                          mergedElementDefinitions, 
                          elementDefinitionFacade.getPath().getParent().getParent().getBase(), 
                          elementDefinitionFacade.getPath().getBase()
                      );
              }
              for(int j = 1; j < locales.size(); j++) { // skip en_US
                  String label = null;
                  String shortLabel = null;
                  String toolTip = null;
                  if(mergedElementDefinition != null) {
                      label = (String)mergedElementDefinition.get(locales.get(j) + "_Label");
                      shortLabel = (String)mergedElementDefinition.get(locales.get(j) + "_ShortLabel");
                      toolTip = (String)mergedElementDefinition.get(locales.get(j) + "_ToolTip");
                  }
                  elementDefinitionFacade.attributeValues("label").add(label == null ? "" : label);
                  elementDefinitionFacade.attributeValues("shortLabel").add(shortLabel == null ? "" : shortLabel);
                  elementDefinitionFacade.attributeValues("toolTip").add(toolTip == null ? "" : toolTip);
              }
          }
      }
      catch(ParserConfigurationException e) {
          System.err.println("ParserConfigurationException: can not load file " + file.getAbsolutePath());
      }
      catch(SAXException e) {
          System.err.println("SAXException: can not load file " + file.getAbsolutePath());
      }
      catch(IOException e) {
          System.err.println("IOException: can not load file " + file.getAbsolutePath());
      }      
  }
  
  //-------------------------------------------------------------------------    
  private void writeAsTable(
      Writer w,
      Writer fw,
      Map<Path,MappedRecord> elementDefinitions
  ) throws ServiceException, IOException {
      String s = null;   
      s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
      w.write(s, 0, s.length());
      s = "<ElementDefinitions>\n";
      w.write(s, 0, s.length());
      for(
          Iterator<MappedRecord> j = elementDefinitions.values().iterator(); 
          j.hasNext(); 
      ) {
          MappedRecord entry = j.next();
          ObjectHolder_2Facade entryFacade;
          try {
	          entryFacade = ObjectHolder_2Facade.newInstance(entry);
          }
          catch (ResourceException e) {
        	  throw new ServiceException(e);
          }
          String elementDefinitionType = entryFacade.getObjectClass();
          if("org:openmdx:ui1:ElementDefinition".equals(elementDefinitionType)) {
              s = "  <ElementDefinition name=\"" + entryFacade.getPath().getBase() + "\">\n";
          }
          else if("org:openmdx:ui1:AlternateElementDefinition".equals(elementDefinitionType)) {
              s = "  <AlternateElementDefinition name=\"" + entryFacade.getPath().getParent().getParent().getBase() + "\" id=\"" + entryFacade.getPath().getBase() + "\">\n";                      
          }
          else {
              s = "  <AdditionalElementDefinition name=\"" + entryFacade.getPath().getParent().getParent().getBase() + "\" id=\"" + entryFacade.getPath().getBase() + "\">\n";                      
          }
          w.write(s, 0, s.length());
          // label
          if(entryFacade.getAttributeValues("label") != null) {
              s = "    <Text type=\"Label\">\n";
              w.write(s, 0, s.length());
              for(int k = 0; k < locales.size(); k++) {
                  s = "      <" + locales.get(k) + ">";
                  w.write(s, 0, s.length());
                  s = (String)entryFacade.attributeValues("label").get(k);
                  if(s == null) s = "";
                  fw.write(s, 0, s.length());
                  s = "</" + locales.get(k) + ">\n";
                  w.write(s, 0, s.length());
              }
              s = "    </Text>\n";
              w.write(s, 0, s.length());
          }
          // shortLabel
          if(entryFacade.getAttributeValues("shortLabel") != null) {
              s = "    <Text type=\"ShortLabel\">\n";
              w.write(s, 0, s.length());
              for(int k = 0; k < locales.size(); k++) {
                  s = "      <" + locales.get(k) + ">";
                  w.write(s, 0, s.length());
                  s = (String)entryFacade.attributeValues("shortLabel").get(k);
                  if(s == null) s = "";
                  fw.write(s, 0, s.length());
                  s = "</" + locales.get(k) + ">\n";
                  w.write(s, 0, s.length());
              }
              s = "    </Text>\n";
              w.write(s, 0, s.length());
          }
          // toolTip
          if(entryFacade.getAttributeValues("toolTip") != null) {
              s = "    <Text type=\"ToolTip\">\n";
              w.write(s, 0, s.length());
              for(int k = 0; k < locales.size(); k++) {
                  s = "      <" + locales.get(k) + ">";
                  w.write(s, 0, s.length());
                  s = (String)entryFacade.attributeValues("toolTip").get(k);
                  if(s == null) s = "";
                  fw.write(s, 0, s.length());
                  s = "</" + locales.get(k) + ">\n";
                  w.write(s, 0, s.length());
              }
              s = "    </Text>\n";
              w.write(s, 0, s.length());
          }
          if("org:openmdx:ui1:ElementDefinition".equals(elementDefinitionType)) {
              s = "  </ElementDefinition>\n";
          }
          else if("org:openmdx:ui1:AlternateElementDefinition".equals(elementDefinitionType)) {
              s = "  </AlternateElementDefinition>\n";                      
          }
          else {
              s = "  </AdditionalElementDefinition>\n";                                    
          }
          w.write(s, 0, s.length());
      }
      s = "</ElementDefinitions>\n";         
      w.write(s, 0, s.length());      
  }
  
  //-------------------------------------------------------------------------    
  protected void split(
      List locales,
      File sourceDir,
      File targetDir,
      String format
  ) throws ServiceException {
      String en_US_Dir =  targetDir.getAbsolutePath() + File.separatorChar + "en_US";
      System.out.println("sourceDir=" + sourceDir.getAbsolutePath());      
      File[] en_US_files = new File(en_US_Dir).listFiles();
      if(en_US_files == null) {
          System.out.println("ERROR: directory not found: " + en_US_Dir);
          return;
      }
      
      // en_US files are leading. process all files
      for(int u = 0; u < en_US_files.length; u++) {
          Map elementDefinitions = new LinkedHashMap();          
          File file =  new File(sourceDir.getAbsolutePath() + File.separatorChar + en_US_files[u].getName());
          if("table".equals(this.format)) {
              this.readAsTable(
                  file,
                  en_US_files[u],
                  elementDefinitions
              );
          }
          else {
              this.readAsSchema(
                  file, 
                  elementDefinitions, 
                  -1
              );              
          }
         
          // split and store as XML
          for(int j = 1; j < locales.size(); j++) { // never write en_US                  
              String outFileName = targetDir.getAbsolutePath() + File.separatorChar + locales.get(j) + File.separatorChar + en_US_files[u].getName(); 
              try {
                  File outFile = new File(outFileName);
                  if(outFile.exists()) {
                      File renamed = new File(outFile.getParent() + File.separatorChar + ".#" + outFile.getName());
                      if(!outFile.renameTo(renamed)) {
                          System.out.println("WARNING: can not move file " + outFile.getAbsolutePath() + " to " + renamed.getAbsolutePath() + ". Skipping");
                          continue;
                      }
                  }
                  else if(!outFile.getParentFile().exists()) {
                      if(!outFile.getParentFile().mkdir()) {
                          System.out.println("WARNING: can not create directory " + outFile.getParentFile().getAbsolutePath() + ". Skipping");
                          continue;                      
                      }
                  }
                  System.out.println("writing file " + outFileName);
                  Writer w = new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8");
                  Writer fw = new XMLWriter(w);
                  this.writeAsSchema(
                      w, 
                      fw, 
                      elementDefinitions, 
                      j
                  );
                  w.close();
              }
              catch(FileNotFoundException e) {
                  System.err.println("can not create file " + outFileName);
              }
              catch(UnsupportedEncodingException e) {
                  System.err.println("can not create file with encoding UTF-8 " + outFileName);
              }
              catch(IOException e) {
                  System.err.println("error writing to file " + outFileName);
              }
          }
      }
  }

  //-------------------------------------------------------------------------    
  protected void merge(
      List locales,
      File sourceDir,
      File targetDir,
      String format
  ) throws ServiceException {

      String en_US_Dir =  sourceDir.getAbsolutePath() + File.separatorChar + "en_US";
      File[] en_US_files = new File(en_US_Dir).listFiles();
      System.out.println("sourceDir=" + sourceDir.getAbsolutePath());      
      if(en_US_files == null) {
          System.out.println("ERROR: directory not found: " + en_US_Dir);
          return;
      }
      
      // process all ui files
      for(int u = 0; u < en_US_files.length; u++) {

          // get all locale specific files for en_US_files[k]
          Map mergedElementDefinitions = new LinkedHashMap();
          for(int i = 0; i < locales.size(); i++) {
              File file =  new File(sourceDir.getAbsolutePath() + File.separatorChar + locales.get(i) + File.separatorChar + en_US_files[u].getName());
              this.readAsSchema(
                  file, 
                  mergedElementDefinitions, 
                  i
              );
          }
    
          // dump mergedElementDefinitions as UTF-8 encoded XML
          String outFileName = targetDir.getAbsolutePath() + File.separatorChar + en_US_files[u].getName();
          try {
              // rename existing file
              File outFile = new File(outFileName);
              if(outFile.exists()) {
                  File renamed = new File(outFile.getParent() + File.separatorChar + ".#" + outFile.getName());
                  if(!outFile.renameTo(renamed)) {
                      System.out.println("WARNING: can not move file " + outFile.getAbsolutePath() + " to " + renamed.getAbsolutePath() + ". Skipping");
                      continue;
                  }
              }
              else if(!outFile.getParentFile().exists()) {
                  if(!outFile.getParentFile().mkdir()) {
                      System.out.println("WARNING: can not create directory " + outFile.getParentFile().getAbsolutePath() + ". Skipping");
                      continue;                      
                  }
              }
              System.out.println("writing file " + outFileName);
              Writer w = new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8");
              Writer fw = new XMLWriter(w);
              if("table".equals(format)) {
                  this.writeAsTable(
                      w,
                      fw,
                      mergedElementDefinitions
                  );
              }
              else {
                  this.writeAsSchema(
                      w,
                      fw,
                      mergedElementDefinitions,
                      -1
                  );
              }
              w.close();
          }
          catch(FileNotFoundException e) {
              System.err.println("can not create file " + outFileName);
          }
          catch(UnsupportedEncodingException e) {
              System.err.println("can not create file with encoding UTF-8 " + outFileName);
          }
          catch(IOException e) {
              System.err.println("error writing to file " + outFileName);
          }
      }
  }
  
  //-------------------------------------------------------------------------    
  protected void run(
	  String[] args
  ) throws ServiceException {
      this.locales = new ArrayList();
      this.sourceDir = new File(".");
      this.targetDir = new File(".");
      this.format = "table";
      String command = "merge";
	  for(int i = 0; i < args.length; i++) {
	      // locales. get locale and assert en_US to be the first in the list
	      if("--locale".equals(args[i])) {
	    	  int j = i + 1;
	    	  while(j < args.length && !args[j].startsWith("--")) {
	    		  this.locales.add(args[j]);
	    		  j++;
	    	  }
	      }
	      // sourceDir
	      else if("--sourceDir".equals(args[i]) && (i + 1 < args.length)) {
	          this.sourceDir = new File(args[i+1]);
	      }
	      // targetDir
	      else if("--targetDir".equals(args[i]) && (i + 1 < args.length)) {
	          this.targetDir = new File(args[i+1]);
	      }
	      else if("--format".equals(args[i]) && (i + 1 < args.length)) {
	          this.format = args[i+1];	    	  
	      }
	      else if("--split".equals(args[i])) {
	    	  command = "split";
	      }
	      else if("--merge".equals(args[i])) {
	    	  command = "merge";
	      }
	  }
      if(this.locales.isEmpty() || !"en_US".equals(this.locales.get(0))) {
          this.locales.add(0, "en_US");
      }	  
	  
      if("merge".equals(command)){
          this.merge(
        	  this.locales, 
        	  this.sourceDir, 
        	  this.targetDir, 
        	  this.format
          );
      }
      else if("split".equals(command)){
          this.split(
        	  this.locales, 
        	  this.sourceDir, 
        	  this.targetDir, 
        	  this.format
          );
      }
  }

  //-------------------------------------------------------------------------    
  public static void main(
    String[] args
  ) throws ServiceException {
    new UiUtility().run(args);
  }
  
  //-------------------------------------------------------------------------
  // Variables    
  //-------------------------------------------------------------------------    
  private List locales = null;
  private File sourceDir = null;
  private File targetDir = null;
  private String format = null;
  
}

//--- End of File -----------------------------------------------------------

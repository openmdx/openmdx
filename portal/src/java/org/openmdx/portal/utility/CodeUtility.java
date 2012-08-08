/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: CodeUtility.java,v 1.9 2009/03/08 18:03:26 wfro Exp $
 * Description: CodeUtility
 * Revision:    $Revision: 1.9 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2009/03/08 18:03:26 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, CRIXP Corp., Switzerland
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.shell.Application;
import org.openmdx.application.shell.ApplicationController;
import org.openmdx.application.shell.CmdLineOption;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.compatibility.base.dataprovider.importer.xml.XmlImporter;
import org.openmdx.portal.text.conversion.XMLWriter;
import java.beans.ExceptionListener;
import org.openmdx.uses.org.apache.commons.collections.map.ListOrderedMap;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Command line utility which supports the management of openCRX code files:
 * <ul>
 *   <li>merge: merges locale separated code files (en_US, de_DE) to one merged file.</li> 
 *   <li>split: splits a merged code file to locale separated files.</li>
 * </ul>
 * <p>
 */
@SuppressWarnings("unchecked")
public class CodeUtility
  extends Application
  implements ExceptionListener {
    
  //-------------------------------------------------------------------------    
  public CodeUtility(
  ) {
    super(
      APP_NAME, 
      VERSION, 
      HELP_TEXT, 
      CodeUtility.createCmdLineOptions()
    );        
  }
        
  //-------------------------------------------------------------------------    
  public void exceptionThrown(
         Exception exception
  ){
     exception.printStackTrace();
  }

  //-------------------------------------------------------------------------  
  protected void init(
  ) throws Exception {
      // locales. get locale and assert en_US to be the first in the list
      this.locale = new ArrayList();
      if(this.getCmdLineArgs().hasArg("locale")) {
        this.locale.addAll(this.getCmdLineArgs().getValues("locale"));   
      }
      if((this.locale.size() == 0) || !"en_US".equals(this.locale.get(0))) {
          this.locale.add(0, "en_US");
      }
      // sourceDir
      this.sourceDir = null;
      if(this.getCmdLineArgs().hasArg("sourceDir")) {
          this.sourceDir = new File(this.getCmdLineArgs().getFirstValue("sourceDir"));
      }
      else {
          this.sourceDir = new File(".");
      }  
      // targetDir
      this.targetDir = null;
      if(this.getCmdLineArgs().hasArg("targetDir")) {
          this.targetDir = new File(this.getCmdLineArgs().getFirstValue("targetDir"));
      }
      else {
          this.targetDir = new File(".");
      }
  }

  //-------------------------------------------------------------------------    
  private Map lookupCode(
      Document document,
      String lookupContainerName,
      String lookupCode
  ) {
      Map codeEntry = new HashMap();
      NodeList containerNodes = document.getElementsByTagName("CodeValueContainer");
      for(int i = 0; i < containerNodes.getLength();  i++) {
        org.w3c.dom.Node containerNode = containerNodes.item(i);
        org.w3c.dom.NamedNodeMap containerNodeAttributes = containerNode.getAttributes();
        org.w3c.dom.Attr containerNodeName = (org.w3c.dom.Attr)containerNodeAttributes.getNamedItem("name");
        if(
          (containerNodeName != null) &&
          lookupContainerName.equals(containerNodeName.getValue())
        ) {
            org.w3c.dom.NodeList codeValueEntryNodes = containerNode.getChildNodes();
            for(int j = 0; j < codeValueEntryNodes.getLength(); j++) {
                org.w3c.dom.Node codeValueEntryNode = codeValueEntryNodes.item(j);
                if(codeValueEntryNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    if("CodeValueEntry".equals(codeValueEntryNode.getNodeName())) {
                        org.w3c.dom.NamedNodeMap codeValueEntryNodeAttributes = codeValueEntryNode.getAttributes();
                        org.w3c.dom.Attr codeValueEntryNodeName = (org.w3c.dom.Attr)codeValueEntryNodeAttributes.getNamedItem("code");
                        if(
                            (codeValueEntryNodeName != null) &&
                            (lookupCode.equals(codeValueEntryNodeName.getValue()))
                        ) {
                            org.w3c.dom.NodeList attributeNodes = codeValueEntryNode.getChildNodes();
                            for(int k = 0; k < attributeNodes.getLength(); k++) {
                                org.w3c.dom.Node attributeNode = attributeNodes.item(k);
                                if(attributeNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                    if(attributeNode.hasChildNodes()) {
                                        codeEntry.put(
                                            attributeNode.getNodeName(),
                                            attributeNode.getFirstChild().getNodeValue()
                                        );
                                    }
                                }
                            }
                        }
                    }
                    else {
                        System.out.println("WARNING: skipping node " + codeValueEntryNode.getNodeName());
                    }
                }
            }
        }
      }
      return codeEntry;
  }
  
  //-------------------------------------------------------------------------    
  protected void split(
      List locale,
      File sourceDir,
      File targetDir
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
          // read files containing locale-specific texts and add to en_US
          Map codes = ListOrderedMap.decorate(new HashMap());
          XmlImporter importer = new XmlImporter(
              codes,
              false
          );
          System.out.println("loading " + en_US_files[u]);
          try {
              importer.process(
                  new String[]{en_US_files[u].getAbsolutePath()}
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
          File file =  new File(sourceDir.getAbsolutePath() + File.separatorChar + en_US_files[u].getName());
          try {
              System.out.println("loading " + file.getAbsolutePath());
              org.w3c.dom.Document mergedCodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
              for(Iterator i = codes.values().iterator(); i.hasNext(); ) {
                  DataproviderObject code = (DataproviderObject)i.next();
                  Map mergedCode = 
                      this.lookupCode(
                          mergedCodes, 
                          code.path().getParent().getParent().getBase(), 
                          code.path().getBase()
                      );
                  for(int j = 1; j < locale.size(); j++) { // skip en_US
                      String shortText = (String)mergedCode.get(locale.get(j) + "_short");
                      String longText = (String)mergedCode.get(locale.get(j) + "_long");
                      code.values("shortText").add(shortText == null ? "" : shortText);
                      code.values("longText").add(longText == null ? "" : longText);
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
         
          // split and store codes as XML
          for(int j = 1; j < locale.size(); j++) { // never write base locale 0                  
              String outFileName = targetDir.getAbsolutePath() + File.separatorChar + locale.get(j) + File.separatorChar + en_US_files[u].getName(); 
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
                  String providerName ="CRX"; // default
                  String segmentName = "Standard"; // default
                  if(codes.size() > 0) {
                      DataproviderObject_1_0 obj = (DataproviderObject_1_0)codes.values().iterator().next();
                      providerName = obj.path().get(2);
                      segmentName = obj.path().get(4);
                  }
                  String s = null;
                  s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                      "<org.openmdx.base.Authority xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" name=\"org:opencrx:kernel:code1\" xsi:noNamespaceSchemaLocation=\"xri:+resource/org/opencrx/kernel/code1/xmi/code1.xsd\">\n" +
                      "  <_object/>\n" + 
                      "  <_content>\n" +
                      "    <provider>\n" +
                      "      <org.openmdx.base.Provider qualifiedName=\"" + providerName + "\" _operation=\"null\">\n" +
                      "        <_object/>\n" +
                      "        <_content>\n" +
                      "          <segment>\n" +
                      "            <org.opencrx.kernel.code1.Segment qualifiedName=\"" + segmentName + "\" _operation=\"null\">\n" +
                      "              <_object/>\n" +
                      "              <_content>\n" +
                      "                <valueContainer>\n";
                  w.write(s, 0, s.length());
                  boolean firstContainer = true;
                  for(Iterator i = codes.values().iterator(); i.hasNext(); ) {
                      DataproviderObject_1_0 element = (DataproviderObject_1_0)i.next();
                      if("org:opencrx:kernel:code1:CodeValueContainer".equals(element.values(SystemAttributes.OBJECT_CLASS).get(0))) {
                          if(!firstContainer) {
                              s = "                      </entry>\n" +
                                  "                    </_content>\n" +
                                  "                  </org.opencrx.kernel.code1.CodeValueContainer>\n";
                              w.write(s, 0, s.length());
                          }
                          s = "                  <org.opencrx.kernel.code1.CodeValueContainer name=\"" + element.path().getBase() + "\" _operation=\"create\">\n" +
                              "                    <_object/>\n" +
                              "                    <_content>\n" +
                              "                      <entry>\n";
                          w.write(s, 0, s.length());
                          firstContainer = false;
                      }
                      else if("org:opencrx:kernel:code1:CodeValueEntry".equals(element.values(SystemAttributes.OBJECT_CLASS).get(0))) {
                          // only write if there are texts
                          if(
                            ((element.values("shortText").size() > j) && (((String)element.values("shortText").get(j)).length() > 0)) ||
                            ((element.values("longText").size() > j) && (((String)element.values("longText").get(j)).length() > 0))
                          ) {                          
                              String shortText = element.values("shortText").size() > j
                                ? (String)element.values("shortText").get(j)
                                : "";
                              String longText = element.values("longText").size() > j
                                ? (String)element.values("longText").get(j)
                                : "";
                              s = "                        <org.opencrx.kernel.code1.CodeValueEntry code=\"" + element.path().getBase() + "\" _operation=\"create\">\n" +
                                  "                          <_object>\n";
                              w.write(s, 0, s.length());
                              if(shortText.length() > 0) {
                                  s = "                            <shortText>\n" +
                                      "                              <_item>";
                                  w.write(s, 0, s.length());
                                  s = shortText;
                                  fw.write(s, 0, s.length());
                                  s = "</_item>\n" +
                                      "                            </shortText>\n";
                                  w.write(s, 0, s.length());
                              }
                              if(longText.length() > 0) {
                                  s = "                            <longText>\n" +
                                      "                              <_item>";
                                  w.write(s, 0, s.length());
                                  s = longText;
                                  fw.write(s, 0, s.length());
                                  s = "</_item>\n" +
                                      "                            </longText>\n";
                                  w.write(s, 0, s.length());
                              }
                              s = "                          </_object>\n" +
                                  "                          <_content/>\n" +
                                  "                        </org.opencrx.kernel.code1.CodeValueEntry>\n";
                              w.write(s, 0, s.length());
                          }
                      }
                  }
                  if(!firstContainer) {
                      s = "                      </entry>\n" +
                          "                    </_content>\n" +
                          "                  </org.opencrx.kernel.code1.CodeValueContainer>\n";
                      w.write(s, 0, s.length());
                  }
                  s = "                </valueContainer>\n" +
                      "              </_content>\n" +
                      "            </org.opencrx.kernel.code1.Segment>\n" +
                      "          </segment>\n" +
                      "        </_content>\n" +
                      "      </org.openmdx.base.Provider>\n" +
                      "    </provider>\n" +
                      "  </_content>\n" +
                      "</org.openmdx.base.Authority>\n";
                  w.write(s, 0, s.length());
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
      List locale,
      File sourceDir,
      File targetDir
  ) throws ServiceException {

      String en_US_Dir =  sourceDir.getAbsolutePath() + File.separatorChar + "en_US";
      System.out.println("sourceDir=" + sourceDir.getAbsolutePath());      
      File[] en_US_files = new File(en_US_Dir).listFiles();
      if(en_US_files == null) {
          System.out.println("ERROR: directory not found: " + en_US_Dir);
          return;
      }
      
      // process all code files
      for(int u = 0; u < en_US_files.length; u++) {

          // get all locale specific files for en_US_files[k]
          Map mergedCodes = new TreeMap();
          Set codeValueContainers = new HashSet(); // collect CodeValueContainer which are required in next step
          for(int i = 0; i < locale.size(); i++) {
              
              // read entries
              File file =  new File(sourceDir.getAbsolutePath() + File.separatorChar + locale.get(i) + File.separatorChar + en_US_files[u].getName());
              Map codes = new HashMap();
              if(file.exists()) {
                  XmlImporter importer = new XmlImporter(
                    codes,
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
              Set keySet = i == 0 ? codes.keySet() : mergedCodes.keySet();
              try {
                  for(Iterator j = keySet.iterator(); j.hasNext(); ) {
                    Path key = (Path)j.next();
                    // merge entry
                    if(mergedCodes.get(key) != null) {
                      DataproviderObject mergedCodeEntry = (DataproviderObject)mergedCodes.get(key);
                      if("org:opencrx:kernel:code1:CodeValueEntry".equals(mergedCodeEntry.values(SystemAttributes.OBJECT_CLASS).get(0))) {
                          DataproviderObject codeEntry = (DataproviderObject)codes.get(key);
                          if(mergedCodeEntry.getValues("shortText") != null) {
                            mergedCodeEntry.values("shortText").add(
                              (codeEntry != null) && (codeEntry.values("shortText").size() > 0)
                                ? codeEntry.values("shortText").get(0)
                                : "" // empty string as default
                            );
                          }
                          if(mergedCodeEntry.getValues("longText") != null) {
                            mergedCodeEntry.values("longText").add(
                              (codeEntry != null) && (codeEntry.values("longText").size() > 0)
                                ? codeEntry.values("longText").get(0)
                                : ""  // empty string as default
                            );
                          }
                      }
                    }
                    // add if it does not exist. Only add for locale=0 (en_US)
                    else if(i == 0) {
                        DataproviderObject_1_0 entry = (DataproviderObject_1_0)codes.get(key);
                        if("org:opencrx:kernel:code1:CodeValueContainer".equals(entry.values(SystemAttributes.OBJECT_CLASS).get(0))) {
                            codeValueContainers.add(entry);
                        }
                        else {                         
                            mergedCodes.put(
                                key,
                                entry
                            );
                        }
                    }
                    // locale > 0 requires that locale=0 exists. Complain if it
                    // does not
                    else {
                      System.err.println("entry " + key + " of locale " + locale.get(i) + " has no corresponding entry for locale " + locale.get(0) + ". Not loading");
                    }
                  }
              }
              catch(Exception e) {
                  System.err.println("Can not import. Reason is " + e.getMessage());
              }
          }
    
          // try numeric sort of entries
          Map sortedCodes = new TreeMap();
          for(Iterator i = mergedCodes.entrySet().iterator(); i.hasNext(); ) {
              Entry entry = (Entry)i.next();
              String codeKey = ((Path)entry.getKey()).getBase();
              try {
                  sortedCodes.put(
                    new Integer(codeKey),
                    entry.getValue()
                  );
              }
              catch(NumberFormatException e) {
                  sortedCodes = mergedCodes;
                  break;
              }
          }
          
          // dump mergedCodes as UTF-8 encoded XML
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
              String s = null;   
              s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
              w.write(s, 0, s.length());
              s = "<CodeValueContainers>\n";
              w.write(s, 0, s.length());
              for(Iterator i = codeValueContainers.iterator(); i.hasNext(); ) {
                  DataproviderObject_1_0 codeValueContainer = (DataproviderObject_1_0)i.next();
                  s = "  <CodeValueContainer name=\"" + codeValueContainer.path().getBase() + "\">\n";
                  w.write(s, 0, s.length());
                  for(Iterator j = sortedCodes.values().iterator(); j.hasNext(); ) {
                      DataproviderObject_1_0 entry = (DataproviderObject_1_0)j.next();
                      if(codeValueContainer.path().getBase().equals(entry.path().getParent().getParent().getBase())) {
                          s = "    <CodeValueEntry code=\"" + entry.path().getBase() + "\">\n";
                          w.write(s, 0, s.length());
                          for(int k = 0; k < locale.size(); k++) {
                              // shortText
                              s = "      <" + locale.get(k) + "_short>";
                              w.write(s, 0, s.length());
                              s = (String)entry.values("shortText").get(k);
                              fw.write(s, 0, s.length());
                              s = "</" + locale.get(k) + "_short>\n";
                              w.write(s, 0, s.length());
                              // longText
                              s = "      <" + locale.get(k) + "_long>";
                              w.write(s, 0, s.length());
                              s = (String)entry.values("longText").get(k);
                              fw.write(s, 0, s.length());
                              s = "</" + locale.get(k) + "_long>\n";
                              w.write(s, 0, s.length());
                          }
                          s = "    </CodeValueEntry>\n";
                          w.write(s, 0, s.length());
                      }
                  }
                  s = "  </CodeValueContainer>\n";
                  w.write(s, 0, s.length());
              }
              s = "</CodeValueContainers>\n";         
              w.write(s, 0, s.length());
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
  ) throws Exception {
      String command = "merge";
      if (this.getCmdLineArgs().hasArg("merge")) {
          command = "merge";
      }
      if (this.getCmdLineArgs().hasArg("split")) {
          command = "split";
      }
      if("merge".equals(command)){
          this.merge(locale, sourceDir, targetDir);
      }
      else if("split".equals(command)){
          this.split(locale, sourceDir, targetDir);
      }
  }

  //-------------------------------------------------------------------------    
  protected void release(
  ) throws Exception {
    System.out.println("shutdown");
  }

  //-------------------------------------------------------------------------    
  public static void main(
    String[] args
  ) {
    ApplicationController controller = new ApplicationController(args);
    controller.initLogging(LOG_CONFIG_NAME, LOG_SOURCE);
    controller.registerApplication(new CodeUtility());
    controller.run();
  }
  
  //-------------------------------------------------------------------------        
  private static List createCmdLineOptions(
  ) {
    ArrayList options = new ArrayList();
    
    // merge command
    options.add(
      new CmdLineOption(
        "merge",
        "Merge locale separated files to one merged file"
      )
    );
    
    // merge command
    options.add(
      new CmdLineOption(
        "split",
        "Split merged file into locale separated files"
      )
    );
    
    // dir (split directory)
    options.add(
      new CmdLineOption(
        "sourceDir",
        "directory containing the source files",
        1,
        1
      )
    );    
    options.add(
      new CmdLineOption(
        "targetDir",
        "directory containing the target files",
        1,
        1
      )
    );    
    // locale
    options.add(
      new CmdLineOption(
        "locale",
        "list of locales to process",
        0,
        Integer.MAX_VALUE
      )
    );    
    return options;
  }
  
  //-------------------------------------------------------------------------
  // Variables    
  //-------------------------------------------------------------------------    

  // The version gets set by CVS
  private static final String VERSION = "$Revision: 1.9 $";
  
  // The application name
  private static final String APP_NAME = "CodeMapper";
  
  // The logging configuration name
  private static final String LOG_CONFIG_NAME = "CodeMapper";
  
  // The logging log source name
  private static final String LOG_SOURCE = CodeUtility.APP_NAME;
  
  // Application help
  private static final String HELP_TEXT = "CodeMapper splits and merges code tables";
  
  // command line options
  private List locale = null;
  private File sourceDir = null;
  private File targetDir = null;
  
}

//--- End of File -----------------------------------------------------------

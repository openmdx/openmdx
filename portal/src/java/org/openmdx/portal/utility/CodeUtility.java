/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: CodeUtility
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2012, OMEX AG, Switzerland
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.application.xml.Importer;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.Throwables;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Command line utility which supports the management of openMDX/Portal Code code files:
 * <ul>
 *   <li>merge: merges locale separated code files (en_US, de_DE) to one merged file.</li> 
 *   <li>split: splits a merged code file to locale separated files.</li>
 * </ul>
 * <p>
 */
@SuppressWarnings("unchecked")
public class CodeUtility {
    
	/**
	 * Lookup code.
	 * 
	 * @param document
	 * @param lookupContainerName
	 * @param lookupCode
	 * @return
	 */
	private Map<String,String> lookupCode(
		Document document,
		String lookupContainerName,
		String lookupCode
	) {
      Map<String,String> codeEntry = new HashMap<>();
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
  
	/**
	 * Split merged code files per locale.
	 * 
	 * @param locale
	 * @param sourceDir
	 * @param targetDir
	 * @throws ServiceException
	 */
	protected void split(
		List<String> locale,
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
          Map<Path,ObjectRecord> codes = new LinkedHashMap<Path,ObjectRecord>();
          System.out.println("Loading " + en_US_files[u]);
          try {
              Importer.importObjects(
            	  Importer.asTarget(codes),
            	  Importer.asSource(en_US_files[u])
              );
          }
          catch(ServiceException e) {
              e.log();
              System.out.println("STATUS: " + e.getMessage());
          }
          catch(Exception e) {
              Throwables.log(e);
              System.out.println("STATUS: " + e.getMessage());          
          }
          // Load merged file
          File file =  new File(sourceDir.getAbsolutePath() + File.separatorChar + en_US_files[u].getName());
          if(file.exists()) {
	          try {
	              System.out.println("Loading " + file.getAbsolutePath());
	              org.w3c.dom.Document mergedCodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
	              for(Iterator<ObjectRecord> i = codes.values().iterator(); i.hasNext(); ) {
	                  MappedRecord code = i.next();
	                  Object_2Facade codeFacade;
	                  try {
		                  codeFacade = Object_2Facade.newInstance(code);
	                  }
	                  catch (ResourceException e) {
	                	  throw new ServiceException(e);
	                  }
	                  Map<String,String> mergedCode = 
	                      this.lookupCode(
	                          mergedCodes, 
	                          codeFacade.getPath().getParent().getParent().getLastSegment().toClassicRepresentation(), 
	                          codeFacade.getPath().getLastSegment().toClassicRepresentation()
	                      );
	                  for(int j = 1; j < locale.size(); j++) { // skip en_US
	                      String shortText = (String)mergedCode.get(locale.get(j) + "_short");
	                      String longText = (String)mergedCode.get(locale.get(j) + "_long");
	                      codeFacade.attributeValuesAsList("shortText").add(shortText == null ? "" : shortText);
	                      codeFacade.attributeValuesAsList("longText").add(longText == null ? "" : longText);
	                  }
	              }
	          }
	          catch(ParserConfigurationException e) {
	              System.err.println("ParserConfigurationException: Can not load file " + file.getAbsolutePath());
	          }
	          catch(SAXException e) {
	              System.err.println("SAXException: Can not load file " + file.getAbsolutePath());
	          }
	          catch(IOException e) {
	              System.err.println("IOException: Can not load file " + file.getAbsolutePath());
	          }
	         
	          // split and store codes as XML
	          for(int j = 1; j < locale.size(); j++) { // never write base locale 0                  
	              String outFileName = targetDir.getAbsolutePath() + File.separatorChar + locale.get(j) + File.separatorChar + en_US_files[u].getName(); 
	              try {
	                  File outFile = new File(outFileName);
	                  if(outFile.exists()) {
	                      File renamed = new File(outFile.getParent() + File.separatorChar + ".#" + outFile.getName());
	                      if(!outFile.renameTo(renamed)) {
	                          System.out.println("WARNING: Can not move file " + outFile.getAbsolutePath() + " to " + renamed.getAbsolutePath() + ". Skipping");
	                          continue;
	                      }
	                  }
	                  else if(!outFile.getParentFile().exists()) {
	                      if(!outFile.getParentFile().mkdir()) {
	                          System.out.println("WARNING: Can not create directory " + outFile.getParentFile().getAbsolutePath() + ". Skipping");
	                          continue;                      
	                      }
	                  }
	                  System.out.println("writing file " + outFileName);
	                  try(
    	                  Writer w = new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8");
    	                  Writer fw = new XMLWriter(w);
	                  ){
    	                  String providerName ="CRX"; // default
    	                  String segmentName = "Standard"; // default
    	                  if(codes.size() > 0) {
    	                      MappedRecord obj = codes.values().iterator().next();
    	                      Path objPath = Object_2Facade.getPath(obj);
    	                      providerName = objPath.getSegment(2).toClassicRepresentation();
    	                      segmentName = objPath.getSegment(4).toClassicRepresentation();
    	                  }
    	                  String s = null;
    	                  s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    	                      "<org.openmdx.base.Authority xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" name=\"org:opencrx:kernel:code1\" xsi:noNamespaceSchemaLocation=\"xri://+resource/org/opencrx/kernel/code1/xmi1/code1.xsd\">\n" +
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
    	                  for(Iterator<ObjectRecord> i = codes.values().iterator(); i.hasNext(); ) {
    	                      MappedRecord element = i.next();
    	                      Object_2Facade elementFacade;
    	                      try {
    		                      elementFacade = Object_2Facade.newInstance(element);
    	                      }
    	                      catch (ResourceException e) {
    	                    	  throw new ServiceException(e);
    	                      }
    	                      if("org:opencrx:kernel:code1:CodeValueContainer".equals(elementFacade.getObjectClass())) {
    	                          if(!firstContainer) {
    	                              s = "                      </entry>\n" +
    	                                  "                    </_content>\n" +
    	                                  "                  </org.opencrx.kernel.code1.CodeValueContainer>\n";
    	                              w.write(s, 0, s.length());
    	                          }
    	                          s = "                  <org.opencrx.kernel.code1.CodeValueContainer name=\"" + elementFacade.getPath().getLastSegment().toClassicRepresentation() + "\" _operation=\"create\">\n" +
    	                              "                    <_object/>\n" +
    	                              "                    <_content>\n" +
    	                              "                      <entry>\n";
    	                          w.write(s, 0, s.length());
    	                          firstContainer = false;
    	                      }
    	                      else if("org:opencrx:kernel:code1:CodeValueEntry".equals(elementFacade.getObjectClass())) {
    	                          // only write if there are texts
    	                          if(
    	                            ((elementFacade.attributeValuesAsList("shortText").size() > j) && (((String)elementFacade.attributeValuesAsList("shortText").get(j)).length() > 0)) ||
    	                            ((elementFacade.attributeValuesAsList("longText").size() > j) && (((String)elementFacade.attributeValuesAsList("longText").get(j)).length() > 0))
    	                          ) {                          
    	                              String shortText = elementFacade.attributeValuesAsList("shortText").size() > j
    	                                ? (String)elementFacade.attributeValuesAsList("shortText").get(j)
    	                                : "";
    	                              String longText = elementFacade.attributeValuesAsList("longText").size() > j
    	                                ? (String)elementFacade.attributeValuesAsList("longText").get(j)
    	                                : "";
    	                              s = "                        <org.opencrx.kernel.code1.CodeValueEntry code=\"" + elementFacade.getPath().getLastSegment().toClassicRepresentation() + "\" _operation=\"create\">\n" +
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
	                  }
	              } catch(FileNotFoundException e) {
	                  System.err.println("Can not create file " + outFileName);
	              } catch(UnsupportedEncodingException e) {
	                  System.err.println("Can not create file with encoding UTF-8 " + outFileName);
	              } catch(IOException e) {
	                  System.err.println("Error writing to file " + outFileName);
	              }
	          }
          }
          // Skip if merged file does not exist
          else {
              System.out.println("File does not exist. Skipping " + file.getAbsolutePath());        	  
          }
      }
  }
  
	/**
	 * Merge code files.
	 * 
	 * @param locale
	 * @param sourceDir
	 * @param targetDir
	 * @throws ServiceException
	 */
	protected void merge(
		List<String> locale,
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
		// Process all code files
		for(int u = 0; u < en_US_files.length; u++) {
			// Get all locale specific files for en_US_files[k]
			Map<Path,MappedRecord> mergedCodes = new TreeMap<Path,MappedRecord>();
			Set<MappedRecord> codeValueContainers = new HashSet<>(); // collect CodeValueContainer which are required in next step
			for(int i = 0; i < locale.size(); i++) {              
				// Read entries
				File file =  new File(sourceDir.getAbsolutePath() + File.separatorChar + locale.get(i) + File.separatorChar + en_US_files[u].getName());
				Map<Path,ObjectRecord> codes = new HashMap<Path,ObjectRecord>();
				if(file.exists()) {
					System.out.println("loading " + file);
					try {
						Importer.importObjects(
							Importer.asTarget(codes),
							Importer.asSource(file)
						);
					} catch(ServiceException e) {
						e.log();
						System.out.println("STATUS: " + e.getMessage());
					} catch(Exception e) {
			            Throwables.log(e);
						System.out.println("STATUS: " + e.getMessage());
					}
				}
				// Merge entries
				Set<Path> keySet = i == 0 ? codes.keySet() : mergedCodes.keySet();
				try {
					for(Iterator<Path> j = keySet.iterator(); j.hasNext(); ) {
						Path key = (Path)j.next();
						// merge entry
						if(mergedCodes.get(key) != null) {
							MappedRecord mergedCodeEntry = mergedCodes.get(key);
							Object_2Facade mergedCodeEntryFacade = Object_2Facade.newInstance(mergedCodeEntry);
							if(mergedCodeEntryFacade.getObjectClass().endsWith("CodeValueEntry")) {
								MappedRecord codeEntry = codes.get(key);
								Object_2Facade codeEntryFacade = Object_2Facade.newInstance(codeEntry);
								mergedCodeEntryFacade.attributeValuesAsList("shortText").add(
									(codeEntryFacade != null && !codeEntryFacade.attributeValuesAsList("shortText").isEmpty()) 
										? codeEntryFacade.attributeValue("shortText") 
										: "" // empty string as default
									);
								mergedCodeEntryFacade.attributeValuesAsList("longText").add(
									(codeEntryFacade != null && !codeEntryFacade.attributeValuesAsList("longText").isEmpty()) 
										? codeEntryFacade.attributeValue("longText") 
										: ""  // empty string as default
									);
							}
						} else if(i == 0) {
							// add if it does not exist. Only add for locale=0 (en_US)
							MappedRecord entry = codes.get(key);
							if(Object_2Facade.getObjectClass(entry).endsWith("CodeValueContainer")) {
								codeValueContainers.add(entry);
							} else {                         
								mergedCodes.put(
									key,
									entry
								);
							}
						} else {
							// locale > 0 requires that locale=0 exists. Complain if it
							// does not
							System.err.println("entry " + key + " of locale " + locale.get(i) + " has no corresponding entry for locale " + locale.get(0) + ". Not loading");
						}
					}
				} catch(Exception e) {
					System.err.println("Can not import. Reason is " + e.getMessage());
				}
			}
			// Try numeric sort of entries
			Map<? super Comparable<?>,MappedRecord> sortedCodes = new TreeMap<>();
			for(Iterator<Map.Entry<Path,MappedRecord>> i = mergedCodes.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<Path,MappedRecord> entry = i.next();
				String codeKey = ((Path)entry.getKey()).getLastSegment().toClassicRepresentation();
				try {
					sortedCodes.put(
						Integer.valueOf(codeKey),
						entry.getValue()
					);
				} catch(NumberFormatException e) {
					sortedCodes = (Map)mergedCodes; // Dirty, but works for values() only access. Nevertheless assignment to a sortedCodesValues for both cases would be better
					break;
				}
			}
			// Dump mergedCodes as UTF-8 encoded XML
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
				} else if(!outFile.getParentFile().exists()) {
					if(!outFile.getParentFile().mkdir()) {
						System.out.println("WARNING: can not create directory " + outFile.getParentFile().getAbsolutePath() + ". Skipping");
						continue;                      
					}
				}
				System.out.println("writing file " + outFileName);
				try(
    				Writer w = new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8");
    				Writer fw = new XMLWriter(w);
				){
    				String s = null;   
    				s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    				w.write(s, 0, s.length());
    				s = "<CodeValueContainers>\n";
    				w.write(s, 0, s.length());
    				for(Iterator<MappedRecord> i = codeValueContainers.iterator(); i.hasNext(); ) {
    					MappedRecord codeValueContainer = i.next();
    					Path codeValueContainerPath = Object_2Facade.getPath(codeValueContainer);
    					s = "  <CodeValueContainer name=\"" + codeValueContainerPath.getLastSegment().toClassicRepresentation() + "\">\n";
    					w.write(s, 0, s.length());
    					for(Iterator<MappedRecord> j = sortedCodes.values().iterator(); j.hasNext(); ) {
    						MappedRecord entry = j.next();
    						Object_2Facade entryFacade;
    						try {
    							entryFacade = Object_2Facade.newInstance(entry);
    						}
    						catch (ResourceException e) {
    							throw new ServiceException(e);
    						}
    						if(codeValueContainerPath.getLastSegment().toClassicRepresentation().equals(entryFacade.getPath().getParent().getParent().getLastSegment().toClassicRepresentation())) {
    							s = "    <CodeValueEntry code=\"" + entryFacade.getPath().getLastSegment().toClassicRepresentation() + "\">\n";
    							w.write(s, 0, s.length());
    							for(int k = 0; k < locale.size(); k++) {
    								// shortText
    								s = "      <" + locale.get(k) + "_short>";
    								w.write(s, 0, s.length());
    								s = k < entryFacade.attributeValuesAsList("shortText").size() ?
    									(String)entryFacade.attributeValuesAsList("shortText").get(k) :
    										"";
    									fw.write(s, 0, s.length());
    									s = "</" + locale.get(k) + "_short>\n";
    									w.write(s, 0, s.length());
    									// longText
    									s = "      <" + locale.get(k) + "_long>";
    									w.write(s, 0, s.length());
    									s = k < entryFacade.attributeValuesAsList("longText").size() ?
    										(String)entryFacade.attributeValuesAsList("longText").get(k) :
    											"";
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
				}
			} catch(FileNotFoundException e) {
				System.err.println("can not create file " + outFileName);
			} catch(UnsupportedEncodingException e) {
				System.err.println("can not create file with encoding UTF-8 " + outFileName);
			} catch(IOException e) {
				System.err.println("error writing to file " + outFileName);
			}
		}
	}
  
	/**
	 * Run code utility.
	 * 
	 * @param args
	 * @throws ServiceException
	 */
	protected void run(
		String[] args
	) throws ServiceException {
      this.locales = new ArrayList<>();
      this.sourceDir = new File(".");
      this.targetDir = new File(".");
      String command = "merge";
	  for(int i = 0; i < args.length; i++) {
	      if("--locale".equals(args[i]) && (i + 1 < args.length)) {
	    	  String[] locales = args[i+1].split("/");
	    	  this.locales.addAll(Arrays.asList(locales));
	      }	      // sourceDir
	      else if("--sourceDir".equals(args[i]) && (i + 1 < args.length)) {
	          this.sourceDir = new File(args[i+1]);
	      }
	      // targetDir
	      else if("--targetDir".equals(args[i]) && (i + 1 < args.length)) {
	          this.targetDir = new File(args[i+1]);
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
        	  this.targetDir
          );
      }
      else if("split".equals(command)){
          this.split(
        	  this.locales, 
        	  this.sourceDir, 
        	  this.targetDir
          );
      }
	}

	/**
	 * Main code utility.
	 * 
	 * @param args
	 * @throws ServiceException
	 */
	public static void main(
		String[] args
	) throws ServiceException {
	  new CodeUtility().run(args);
	}
  
	//-------------------------------------------------------------------------
	// Members    
	//-------------------------------------------------------------------------    
	private List<String> locales = null;
	private File sourceDir = null;
	private File targetDir = null;
  
}

//--- End of File -----------------------------------------------------------

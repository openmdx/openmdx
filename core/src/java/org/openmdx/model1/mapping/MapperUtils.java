/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: MapperUtils.java,v 1.4 2008/02/15 17:24:06 hburger Exp $
 * Description: GMIUtils
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/15 17:24:06 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.model1.mapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class MapperUtils {

  public MapperUtils(
  ) {
      super();
  }

  //--------------------------------------------------------------------------------
  /**
   * Retrieves the element name for a given qualified name, e.g. for the 
   * qualified name 'org:omg:model1:Class' the element name would be 'Class'.
   * @param qualifiedName The qualified name in ':' notation.
   * @return The element name for the given qualified name.
   */
  public static String getElementName(
    String qualifiedName
  ) {
    return qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1);
  }

  //--------------------------------------------------------------------------------
  /**
   * Retrieves the package name for a given qualified name, e.g. for the 
   * qualified name 'org:omg:model1:Class' the package name would be 
   * 'org:omg:model1'.
   * @param qualifiedName The qualified name in ':' notation.
   * @return The package name for the given qualified name.
   */
  public static String getPackageName(
    String qualifiedName
  ) {
    return getPackageName(
      qualifiedName,
      1
    );
  }

  //--------------------------------------------------------------------------------
  public static String getPackageName(
    String qualifiedName,
    int nestingLevel
  ) {
    String packageName = qualifiedName;
    for(int i = 0; i < nestingLevel; i++) {
        packageName = packageName.substring(0, packageName.lastIndexOf(':'));
    }
    return packageName;
  }

  //--------------------------------------------------------------------------------
  /**
   * Converts a given qualified name into a list of name components, e.g. for 
   * the qualified name 'org:omg:model1:Class' the result would be an ordered 
   * list with the entries 'org', 'omg', 'model1', and 'Class'.
   * @param qualifiedName The qualified name in ':' notation.
   * @return A list of name components for the given qualified name.
   */
  public static List<String> getNameComponents(
    String qualifiedName
  ) {
    List<String> nameComponents = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(qualifiedName, ":");
    while(tokenizer.hasMoreTokens()) {
      nameComponents.add(tokenizer.nextToken());
    }
    return nameComponents;
  }

  //--------------------------------------------------------------------------------
  /**
   * Capitalizes the first character of a given string.
   * @param text The string to be capitalized.
   * @return The capitalized string.
   */
  public static String capitalize(
    String text
  ) {
    StringBuffer buf = new StringBuffer(text.substring(0,1).toUpperCase());
    if (text.length() > 1) {
      buf.append(text.substring(1));
    }
    return buf.toString();
  }

  //--------------------------------------------------------------------------------
  /**
   * Word wraps a given text and returns it as a string.
   * @param indent The indentation to be used for text output.
   * @param inText The input text to be word wrapped.
   * @return The word wrapped output text.
   */
  public static String wrapText(
    String indent,
    String inText
  ) {
    StringBuffer currentLine = new StringBuffer();
    StringBuffer outText = new StringBuffer();
    StringTokenizer tokenizer = new StringTokenizer(inText);
    while (tokenizer.hasMoreTokens()) {
      String nextToken = tokenizer.nextToken();
      if (
        indent.length() + currentLine.length() + nextToken.length() + 1 >= WRAP_AT_POSITION
      ) {
        outText.append(indent);
        outText.append(currentLine);
        outText.append(System.getProperty("line.separator"));
        currentLine = new StringBuffer();
      }
      currentLine.append(nextToken);
      currentLine.append(' ');
    }
    outText.append(indent);
    outText.append(currentLine);
    return outText.toString();
  }

  //--------------------------------------------------------------------------------

  /**
   * Evaluate t he XRI of a given model package
   * 
   * @param  name components of the MOF package
   *  
   * @return an XRI which may be used to look up the Authority the gven 
   *         package belongs to"
   */
  public static String getAuthorityId(
    List<String> nameComponents
  ){
    StringBuffer id = new StringBuffer("xri://@openmdx");
    char delimiter = '*';
    for(
        Iterator<String> i = nameComponents.iterator();
        i.hasNext();
        delimiter = '.'
    ) id.append(delimiter).append(i.next());
    return id.toString();
  }

  //--------------------------------------------------------------------------------
  private final static int WRAP_AT_POSITION = 78;

}
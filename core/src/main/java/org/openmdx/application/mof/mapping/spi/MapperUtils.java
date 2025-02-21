/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: GMIUtils
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.application.mof.mapping.spi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;

import org.openmdx.kernel.xri.XRI_2Protocols;

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
    List<String> nameComponents = new ArrayList<>();
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
    StringBuilder buf = new StringBuilder(text.substring(0,1).toUpperCase());
    if (text.length() > 1) {
      buf.append(text.substring(1));
    }
    return buf.toString();
  }

  //--------------------------------------------------------------------------------

   /**
     * A (possibly multiline) input text is split into lines and
     * send to the sink line by line with the given indent.
     * 
     * @param indent The indentation to be used for text output.
     * @param text The input text to be word wrapped.
     */
    public static void wrapText(
        String indent,
        String text, 
        Consumer<CharSequence> sink
    ) {
        final StringBuilder buffer = new StringBuilder();
        final StringTokenizer tokenizer = new StringTokenizer(text, "\n\r\f");
        while (tokenizer.hasMoreTokens()) {
            String nextToken = tokenizer.nextToken();
            buffer.append(indent);
            buffer.append(nextToken);
            sink.accept(buffer);
            buffer.setLength(0);
        }
    }

  //--------------------------------------------------------------------------------

  /**
   * Evaluate t he XRI of a given model package
   * 
   * @param  nameComponents Components of the MOF package
   *  
   * @return an XRI which may be used to look up the Authority the gven 
   *         package belongs to"
   */
  public static String getAuthorityId(
    List<String> nameComponents
  ){
    StringBuilder id = new StringBuilder(XRI_2Protocols.OPENMDX_PREFIX);
    char delimiter = '*';
    for(
        Iterator<String> i = nameComponents.iterator();
        i.hasNext();
        delimiter = '.'
    ) id.append(delimiter).append(i.next());
    return id.toString();
  }

}

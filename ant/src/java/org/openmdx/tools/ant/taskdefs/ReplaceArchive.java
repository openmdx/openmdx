/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ReplaceArchive.java,v 1.1 2005/08/18 15:53:10 hburger Exp $
 * Description: ReplaceArchive ant task
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/08/18 15:53:10 $
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
package org.openmdx.tools.ant.taskdefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * ReplaceArchive is an ant task which replaces a token by a value string in
 * an archive file. The task works recursively, i.e. it also processes
 * contained archives. For performance reasons multiple token/value pairs
 * can be specified.
 */
public class ReplaceArchive 
    extends Task {

    //-------------------------------------------------------------------------
  public ReplaceArchive(
  ) {
      super();
  }

  //-------------------------------------------------------------------------
  public void setToken(
      String token
  ) {
      this.tokens[0] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken1(
      String token
  ) {
      this.tokens[1] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken2(
      String token
  ) {
      this.tokens[2] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken3(
      String token
  ) {
      this.tokens[3] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken4(
      String token
  ) {
      this.tokens[4] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken5(
      String token
  ) {
      this.tokens[5] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken6(
      String token
  ) {
      this.tokens[6] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken7(
      String token
  ) {
      this.tokens[7] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken8(
      String token
  ) {
      this.tokens[8] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken9(
      String token
  ) {
      this.tokens[9] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken10(
      String token
  ) {
      this.tokens[10] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken11(
      String token
  ) {
      this.tokens[11] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken12(
      String token
  ) {
      this.tokens[12] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken13(
      String token
  ) {
      this.tokens[13] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken14(
      String token
  ) {
      this.tokens[14] = token;
  }

  //-------------------------------------------------------------------------
  public void setToken15(
      String token
  ) {
      this.tokens[15] = token;
  }

  //-------------------------------------------------------------------------
  public void setValue(
      String value
  ) {
      this.values[0] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue1(
      String value
  ) {
      this.values[1] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue2(
      String value
  ) {
      this.values[2] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue3(
      String value
  ) {
      this.values[3] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue4(
      String value
  ) {
      this.values[4] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue5(
      String value
  ) {
      this.values[5] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue6(
      String value
  ) {
      this.values[6] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue7(
      String value
  ) {
      this.values[7] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue8(
      String value
  ) {
      this.values[8] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue9(
      String value
  ) {
      this.values[9] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue10(
      String value
  ) {
      this.values[10] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue11(
      String value
  ) {
      this.values[11] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue12(
      String value
  ) {
      this.values[12] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue13(
      String value
  ) {
      this.values[13] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue14(
      String value
  ) {
      this.values[14] = value;
  }
  
  //-------------------------------------------------------------------------
  public void setValue15(
      String value
  ) {
      this.values[15] = value;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Sets the set of exclude patterns. Patterns may be separated by a comma
   * or a space; optional.
   *
   * @param excludes the string containing the exclude patterns
   */
  public void setExcludes(
      String excludes
  ) {
//    this.excludes = excludes;
  }

  //-------------------------------------------------------------------------
  /**
   * The source archive to use when replacing a token in all archive contents;
   * required.
   * @param srcArchive source archive
   */
  public void setSrcArchive(
      File srcArchive
  ) {
      this.srcArchive = srcArchive;
  }

  //-------------------------------------------------------------------------
  /**
   * The destination archive that results after the replacement (an already existing archive will be replaced);
   * required.
   * @param destArchive destination archive
   */
  public void setDestArchive(
      File destArchive
  ) {
      this.destArchive = destArchive;
  }

  //-------------------------------------------------------------------------
  /**
   * Set encoding of source and destination archive. 
   */
  public void setEncoding(
      String encoding
  ) {
      this.encoding = encoding;
  }
  
  //-------------------------------------------------------------------------
  /**
   * The comma or space separated list of archives that are excluded from the replacement;
   * optional.
   * @param excludeArchives comma or space separated list of excluded archives
   */
  public void setExcludeArchives(
      String excludeArchives
  ) {
      this.excludeArchives = new HashSet();
      StringTokenizer tokenizer = new StringTokenizer(excludeArchives, " ,");
      while (tokenizer.hasMoreTokens()) {
	      this.excludeArchives.add(tokenizer.nextToken());
      }
  }

  //-------------------------------------------------------------------------
  /**
   * The comma or space separated list of file types to include in replacement;
   * optional.
   * @param includeTypes comma or space separated list of included file types
   */
  public void setIncludeTypes(
      String includeTypes
  ) {
      this.includeTypes = new HashSet();
      StringTokenizer tokenizer = new StringTokenizer(includeTypes, " ,");
      while (tokenizer.hasMoreTokens()) {
	      this.includeTypes.add(tokenizer.nextToken());
      }
  }

  //-------------------------------------------------------------------------
  public void execute(
  ) throws BuildException {
      
    // verify args
    if (this.srcArchive == null) {
        throw new BuildException("property 'srcArchive' not set.");
    } 
    else if(!this.srcArchive.exists()) {
        throw new BuildException("srcArchive does not exist.");
    }
    if (this.destArchive == null) {
        throw new BuildException("property 'destArchive' not set.");
    }
    if(this.encoding == null) {
        this.encoding = "UTF-8";
    }
    
    log("Recursively replace", Project.MSG_INFO);
    log("     tokens=<" + Arrays.asList(this.tokens) + ">", Project.MSG_INFO);
    log("     values=<" + Arrays.asList(this.values) + ">", Project.MSG_INFO);
    log("     srcArchive=<" + this.srcArchive + ">", Project.MSG_INFO);
    log("     destArchive=<" + this.destArchive + ">", Project.MSG_INFO);
    log("     encoding=<" + this.encoding + ">", Project.MSG_INFO);
    log("     excludeArchives=<" + this.excludeArchives + ">", Project.MSG_INFO);
    log("     includeTypes=<" + this.includeTypes + ">", Project.MSG_INFO);
    
    // replace
    try {
	    ZipInputStream source = new ZipInputStream(new FileInputStream(this.srcArchive));
	    ZipOutputStream dest = new ZipOutputStream(new FileOutputStream(this.destArchive));
	    this.replaceInArchive("", source, dest);
	    source.close();
	    dest.close();
    }
    catch(IOException e) {
        throw new BuildException(e);
    }
  }
  
  //-------------------------------------------------------------------------
  private int writeAndReplace(
      InputStream source,
      OutputStream dest,
      boolean replace
  ) throws IOException {
      if(replace) {

          // get from source
	      ByteArrayOutputStream data = new ByteArrayOutputStream();
	      int b = 0;
	      while((b = source.read()) != -1) {
	          data.write(b);
	      }
	      String s = data.toString(this.encoding);
	      
	      // replace token -> value
	      for(int i = 0; i < MAX_TOKENS; i++) {
	          String token = this.tokens[i];
	          String value = this.values[i];
	          if(token != null) {
			      int pos = 0; 
			      while((pos = s.indexOf(token)) >= 0) {
			          s = s.substring(0, pos) + value + s.substring(pos + token.length());
			      }
	          }
	      }
	      // write to dest
	      byte[] encoded = s.getBytes(this.encoding);
	      dest.write(encoded, 0, encoded.length);
	      return encoded.length;
      }
      else {
	      byte[] data = new byte[1024];
	      int len = 0;
	      int total = 0;
	      while((len = source.read(data, 0, 1024)) != -1) {
	         dest.write(data, 0, len);
	         total += len;
	      }
          return total;
      }
  }

  //-------------------------------------------------------------------------
  private void replaceInArchive(
      String indent,
      ZipInputStream source,
      ZipOutputStream dest
  ) throws IOException {
      ZipEntry sourceEntry = source.getNextEntry();
      while(sourceEntry != null) {
          ZipEntry destEntry = new ZipEntry(sourceEntry.getName());
          dest.putNextEntry(destEntry);
          if(!sourceEntry.isDirectory()) {
              if(
                  this.isArchive(sourceEntry.getName()) && 
                  !this.excludeArchives.contains(sourceEntry.getName())
              ) {
                  log(indent + "processing nested archive " + sourceEntry.getName(), Project.MSG_INFO);
                  
                  // source nested
                  ByteArrayOutputStream sourceNested = new ByteArrayOutputStream();
                  this.writeAndReplace(source, sourceNested, false);
                  sourceNested.close();
                  
                  // dest nested
                  ByteArrayOutputStream destNestedOs = new ByteArrayOutputStream();
                  ZipOutputStream destNested = new ZipOutputStream(destNestedOs);
                  
                  // replace nested
                  this.replaceInArchive(
                      indent + "  ",
                      new ZipInputStream(new ByteArrayInputStream(sourceNested.toByteArray())),
                      destNested
                  );
                  destNested.close();
                  dest.write(destNestedOs.toByteArray());
              }
              else {
                  boolean include = false;
                  for(Iterator i = this.includeTypes.iterator(); i.hasNext(); ) {
                      if(include = sourceEntry.getName().endsWith((String)i.next())) break;
                  }
                  this.writeAndReplace(source, dest, include);
              }
          }
          dest.closeEntry();
          sourceEntry = source.getNextEntry();
      }
  }
  
  //-------------------------------------------------------------------------
  private boolean isArchive(
      String name
  ) {
      return 
          name.endsWith(".jar") || 
          name.endsWith(".war") || 
          name.endsWith(".rar") || 
          name.endsWith(".ear");
  }

  //-------------------------------------------------------------------------
  // Members
  //-------------------------------------------------------------------------
  private static final int MAX_TOKENS = 16;
  
  private String[] tokens = new String[MAX_TOKENS];
  private String[] values = new String[MAX_TOKENS];
  private String encoding = null;
//private String excludes = null;
  private File destArchive = null;
  private File srcArchive = null;
  private Set excludeArchives = new HashSet();
  private Set includeTypes = new HashSet();
}

//--- End of File -----------------------------------------------------------

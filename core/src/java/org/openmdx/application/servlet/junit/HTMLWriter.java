/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: HTMLWriter.java,v 1.1 2004/06/02 13:04:24 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/06/02 13:04:24 $
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
package org.openmdx.application.servlet.junit;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * A writer that scrubs certain ASCII characters into HTML entities.
 *
 * @author Michael T. Nygard
 * @author Tracie Karsjens
 */
public class HTMLWriter extends FilterWriter {
  private boolean passthrough = false;

  public HTMLWriter(OutputStream outStream) {
    super(new OutputStreamWriter(outStream));
  }

  public void write(char cbuf[], int off, int len) throws IOException {
    for(int i = 0; i < len; i++) {
      write(cbuf[i]);
    }
  }

  /**
  * Pass most characters directly through.  Treat certain sequences
  * specially
  */
  public void write(char c) throws IOException {
    if(passthrough) {
      out.write(c);
      return;
    }

    switch(c) {
      case '\n':  out.write("<br>"); break;
      case '<':   out.write("&lt;"); break;
      case '>':   out.write("&gt;"); break;
      case '"':   out.write("&quot;"); break;
      default:    out.write(c);
    }
  }

  public void write(String str, int off, int len) throws IOException {
    char[] chars = new char[len];
    str.getChars(off, off+len, chars, 0);
    for(int i = 0; i < len; i++) {
      write(chars[i]);
    }
  }

  public void setPassthrough(boolean b) {
    this.passthrough = b;
  }

  public boolean isPassthrough() {
    return this.passthrough;
  }
}

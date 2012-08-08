/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XmlContentHandler.java,v 1.10 2008/06/28 00:21:58 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/28 00:21:58 $
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
package org.openmdx.compatibility.base.dataprovider.exporter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.text.conversion.UnicodeTransformation;
import org.openmdx.kernel.exception.BasicException;


/**
 * ContentHandler for formatting XML output. This class roughly follows
 * org.xml.sax.ContentHandler, but allows for easy porting to .net and does not
 * provide all possibilities of the original.
 * 
 * @author anyff
 */
@SuppressWarnings("unchecked")
public class XmlContentHandler {
  
  /**
   * Helper class for converting non allowed XML characters to allowed ones.
   * <p>
   * Must be outermost "Writer", becuase it is used only for content data, 
   * not the xml formatting signs (<>...). The formatting signs are written to 
   * the underlying Writer directly.
   * 
   */
  static private class XmlStringConverter {
    public XmlStringConverter(Writer out) {
      _out = out;
    }
    
    public void write(int b) throws IOException {
      switch (b) {
        case '<' :
          _out.write(new char[] {'&', 'l', 't', ';'});
          break;
        case '>' :
          _out.write(new char[] {'&', 'g', 't', ';'});          
          break;
        case '\'' :
          _out.write(new char[] {'&', 'a', 'p', 'o', 's', ';'});
          break;
        case '"' :
          _out.write(new char[] {'&', 'q', 'u', 'o', 't', ';'});
          break;
        case '&' :
          _out.write(new char[] {'&', 'a', 'm', 'p', ';'});
          break;
        default :
          _out.write(b);
          break;
      }
    }
    
    public void write(String str) throws IOException {
      for (int i = 0; i < str.length(); i++) {
        this.write(str.charAt(i));
      }
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException {
      for (int i = off; i < off + len; i++) {
        this.write(cbuf[i]);
      }
    }
    
    Writer _out;
  }
  
  /**
   * 
   * 
   */
  static private class Attribute{
    Attribute(String qName, String value) {
      _qName = qName;
      _value = value;
    }
    
    /* private */ String _qName;
    /* private */ String _value;
  }
  
  
  /** 
   * Class for setting attributes to startElement. This class roughly follows
   * org.xml.sax.helpers.AttributesImpl, but provides only a minimal set of 
   * methods and supports only CDATA typed attributes.
   * 
   * @author anyff
   *
   * To change this generated comment go to 
   * Window>Preferences>Java>Code Generation>Code Template
   */
  static public class Attributes{
    
    public Attributes() {
      _attributes = new ArrayList();
    }
    
    
    /**
     * Add an attribute of type CDATA. CDATA is the only supported type of 
     * attributes. 
     * 
     * @param qName the qualified name
     * @param value the attribute value
     */
    public void addCDATA(
      String qName, 
      String value
    ) {
      _attributes.add(new Attribute(qName, value));
    }
    
    public String getQName(int index) {
      return ((Attribute)_attributes.get(index))._qName;
      
    }
    
    public String getValue(int index) {
      return ((Attribute)_attributes.get(index))._value;
    }
    
    public int getLength() {
      return _attributes.size();
    }
    
    private List _attributes; 
  }
  
  // ---------------------------------------------------------------------------
  /**
   * Setup an XmlContentHandler.
   * 
   * @param stream
   */
  public XmlContentHandler(PrintStream stream) {
    this.outStream = stream;
  }
  
  
  /**
   * Turn on or off the indentation.
   */
  public void setIndentation(boolean indent) {
    this.indent = indent;
  }
  
  
  /**
   * Get the current indentation setting.
   */
  public boolean getIndentation() {
    return this.indent;
  }
  
  
  /**
   * Set the number of spaces to use for indentation. To have any effect,
   * the indentation must be turned on with setIndentation.
   */
  public void setIndentationLength(int indentationLength) {
      StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < indentationLength; i++) {
      buffer.append(' ');
    }
    this.indentString = buffer.toString();
  }
  
  
  /**
   * Get the number of spaces currently set to be used for indentation.
   */
  public int getIndentationLength() {
    return this.indentString.length();
  }
  
  
  /**
   * Set the encoding to use for the output file. Default is UTF-8.
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }
  
  
  /**
   * Get the currently set encoding.
   */
  public String getEncoding() {
    return this.encoding;
  }
  
  
  /**
   * Set collation for empty elements
   * <p>
   * Define if empty elements should be closed in the same tag or if an opening 
   * and a separate closing tag is required.
   */
  public void setAutoCollation(boolean autoCollation) {
    this.autoCollate = autoCollation;
  }
  
  
  /**
   * Get the current setting of the collation for empty elements.
   */
  public boolean getAutoCollation() {
    return this.autoCollate;
  }
  
  
  /**
   * Start an element.
   * <p>
   * namespaceURI, localName are not currently supported.
   * 
   * @param namespaceURI not supported
   * @param localName not supported
   * @param qName qualified name of the element
   * @param atts attributes of the element, must not be null
   * @throws ServiceException
   */
  public void startElement(
    String namespaceURI,
    String localName,
    String qName,
    Attributes atts
  ) throws ServiceException {
    try {
      // terminate last tag
      if (this.lastElementStarted != null) {
        this.writer.write('>');
      }
      
      if (this.indent) {
        this.writer.write('\n');
      
        for (int i = 0; i < this.qNameStack.size(); i++) {
          this.writer.write(this.indentString);
        }
      }
      this.writer.write('<');
      this.writer.write(qName);
      
      if (atts.getLength() > 0) {
        for (int i = 0; i < atts.getLength(); i++) {
          this.writer.write(" ");
          this.converter.write(atts.getQName(i));
          this.writer.write('=');
          this.writer.write('"');
          this.converter.write(atts.getValue(i));
          this.writer.write('"');
        }
      }
      // don't terminate the tag for autoCollate if required.
      this.lastElementStarted = qName;
      
      this.allowIndent = true;
      this.qNameStack.push(qName);
    }
    catch(IOException ioe) {
      throw new ServiceException (
        ioe,
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.MEDIA_ACCESS_FAILURE,
        null,
        "IOException"
      );
    }
  }
  
  
  /**
   * End an element.
   * 
   * @param namespaceURI
   * @param localName
   * @param qName
   * @throws ServiceException
   */
  public void endElement(
    String namespaceURI,
    String localName,
    String qName
  ) throws ServiceException {
    try {
      String expectedQName = (String)this.qNameStack.pop();
      if (!expectedQName.equals(qName)) {
        throw new ServiceException(
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.ASSERTION_FAILURE,
          new BasicException.Parameter[] {
            new BasicException.Parameter("qName", qName),
            new BasicException.Parameter("expected qName", expectedQName)
          },
          "Non matching qName for XML tag."
         );
      }    
      
      boolean collated = false;
      // check if last tag must be ended or if it must be collated
      if (this.lastElementStarted != null) {
        if (this.lastElementStarted.equals(qName) && this.autoCollate) {
          this.writer.write("/>"); // collate empty element
          collated = true;
        }
        else {
          this.writer.write(">");  // end last tag
        
        }
        this.lastElementStarted = null;
      }
        
      if (!collated) {
        if (this.indent  && this.allowIndent) {
          this.writer.write('\n');
          for (int i = 0; i < this.qNameStack.size(); i++) {
            this.writer.write(this.indentString);
          }
        }

        this.writer.write("</");
        // write through this._converter 
        this.converter.write(qName);
        this.writer.write('>');
      }
      this.allowIndent = true;
    }
    catch(IOException ioe) {
      throw new ServiceException (
        ioe,
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.MEDIA_ACCESS_FAILURE,
        null,
        "IOException"
      );
    }
  }
  
  
  /** 
   * Write the characters of ch to the output stream; starting at start at 
   * most length characters.
   *  
   * @param ch
   * @param start
   * @param length
   * @throws ServiceException
   */
  public void characters(
    char[] ch,
    int start,
    int length
  ) throws ServiceException {
    try {
      
      if (this.lastElementStarted != null) {
        this.writer.write(">");  // end last tag
        this.lastElementStarted = null;
      }
      this.converter.write(ch, start, length);
      this.allowIndent = false;
    }
    catch(IOException ioe) {
      throw new ServiceException (
        ioe,
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.MEDIA_ACCESS_FAILURE,
        null,
        "IOException"
      );
    }
  }

  /**
   * StartDocument only after the encoding and indentation have been set.
   * 
   * @throws ServiceException
   */
  public void startDocument() 
  throws ServiceException {
    if (this.writer == null) {
      try {
        if (this.encoding.equals("UTF-8")) {
          this.writer = UnicodeTransformation.getTransformer().utf8Writer(this.outStream);
        }
        else {
          this.writer = new OutputStreamWriter(this.outStream, this.encoding);
        }
        this.qNameStack = new Stack();
        this.converter = new XmlStringConverter(this.writer);
      }
      catch (UnsupportedEncodingException ue) {
        throw new ServiceException(
          ue,
          BasicException.Code.DEFAULT_DOMAIN,
          BasicException.Code.INVALID_CONFIGURATION,
          new BasicException.Parameter[] {
            new BasicException.Parameter("encoding", this.encoding)
          },
          "Unsupported encoding."
        );
      }
    }
    else {
      throw new ServiceException(
         BasicException.Code.DEFAULT_DOMAIN,
         BasicException.Code.ASSERTION_FAILURE,
         null,
         "Document must be ended before started again."
      );
    }
    
    try {
      //<?xml version="1.0" encoding="UTF-8"?>
      this.writer.write("<?xml version=\"1.0\" encoding=\"");
      this.writer.write(this.encoding);
      this.writer.write("\"?>");
    }
    catch(IOException ioe) {
      throw new ServiceException (
        ioe,
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.MEDIA_ACCESS_FAILURE,
        null,
        "IOException"
      );
    }
  }
  
  /**
   * Write comment
   * @param comment comment string enclosed in comment tags <!-- -->
   * @throws ServiceException
   */
  public void comment(
      String comment
  ) throws ServiceException {
      try {
          this.writer.write("<!-- " + comment + " -->");
      }
      catch(IOException ioe) {
          throw new ServiceException (
            ioe,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.MEDIA_ACCESS_FAILURE,
            null,
            "IOException"
          );
      }
  }
  
  /**
   * End the document. 
   * <p>
   * Exception is thrown if there are open elements.
   * 
   * @throws ServiceException
   */
  public void endDocument(
      boolean closeWriter
  ) throws ServiceException {
    try {
        if(closeWriter) {
            this.writer.close();
            this.writer = null;
        }
        else {
            this.writer.flush();
        }
        if(this.qNameStack.size() != 0) {
            throw new ServiceException(
              BasicException.Code.DEFAULT_DOMAIN,
              BasicException.Code.ASSERTION_FAILURE,
              new BasicException.Parameter[] {
                new BasicException.Parameter("elements", this.qNameStack.toString())
              },
              "Open elements while endDocument()."
            );
        }
    }
    catch(IOException ioe) {
      throw new ServiceException (
        ioe,
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.MEDIA_ACCESS_FAILURE,
        null,
        "IOException"
      );
    }    
  }

  public void endDocument(
  ) throws ServiceException {
      this.endDocument(true);
  }
  
  /**
   * Add the processing instructions.
   */
  public void processingInstruction(
    String target,
    String data
  ) throws ServiceException {
    try {
      this.writer.write("\n<?");
      this.writer.write(target);
      this.writer.write(' ');
      this.writer.write(data);
      this.writer.write("?>");
    }
    catch(IOException ioe) {
      throw new ServiceException (
        ioe,
        BasicException.Code.DEFAULT_DOMAIN,
        BasicException.Code.MEDIA_ACCESS_FAILURE,
        null,
        "IOException"
      );
    }
  }
  
  
  /** outStream as provided by caller */
  private PrintStream outStream = null;
  
  /** Writer setup on the _outStream */
  private Writer writer = null;
  
  /** current encoding for output, default UTF-8 */
  private String encoding = "UTF-8";
  
  /** current indentation length, default 4 */
  private String indentString = "    ";
  
  /** current indentation setting, default true */
  private boolean indent = true;
  
  /** defines if empty elements use one or two tags */
  private boolean autoCollate = true;
  
  /** stack to control the opening and closing of tags */
  private Stack qNameStack;
  
  /** keep the qName of the last element started for auto collation  */
  private String lastElementStarted;
  
  /** allow or prohibit indentation based on method calls received last */
  private boolean allowIndent;
  
  XmlStringConverter converter;
}

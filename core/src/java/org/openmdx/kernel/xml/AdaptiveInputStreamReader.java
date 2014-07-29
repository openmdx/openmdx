/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Adaptive InputStream Reader
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license  as listed below.
 * 
 * Copyright (c) 2005-2013, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.xml;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Adaptive Input Stream Reader has the following encoding priorities<ol>
 * <li>The constructor's <code>encoding</code> argument
 * <li>A byte order mark
 * <li>An XML declaration's encoding attribute
 * <li>The platform's default encoding
 * </ol>
 */
public class AdaptiveInputStreamReader extends Reader {
	
	/**
	 * Constructor
	 * 
	 * @param in
     * @param encoding overrides the adaptive encoding unless it is <code>null</code>
	 * @param byteOrderMarkAware 
	 * @param xmlDeclarationAware 
	 * @param propagateClose tells whether a close request is propagated to the input stream
	 * @throws IOException 
	 */
	public AdaptiveInputStreamReader(
		InputStream in,
		String overriddenEncoding, 
		boolean byteOrderMarkAware, 
		boolean xmlDeclarationAware, 
		boolean propagateClose
	) throws IOException {
	    String encoding = overriddenEncoding;
		if(byteOrderMarkAware || xmlDeclarationAware){
			InputStream stream = new BufferedInputStream(in);
			if (byteOrderMarkAware){
				String byteOrderMark = ByteOrderMark.readByteOrderMark(stream);
				if(encoding == null) {
                    encoding = byteOrderMark;
                }
			}
			if(xmlDeclarationAware) {
				if(encoding == null) {
				    XMLDeclaration xmlDeclaration = XMLDeclaration.readXMLDeclaration(
						stream
					); 
					if(xmlDeclaration != null) {
                        encoding = xmlDeclaration.getEncoding();
                    }
					this.delegate = new BufferedReader(
						encoding == null ? new InputStreamReader(stream) : new InputStreamReader(stream, encoding)
					);
				} else {
					XMLDeclaration.readXMLDeclaration(
						this.delegate = new BufferedReader(
							new InputStreamReader(stream, encoding)
						)
					);
				}
			} else {
                this.delegate = new BufferedReader(
                    encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding)
                );
            }
		} else {
			this.delegate = new BufferedReader(
				encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding)
			);
		}
        this.propagateClose = propagateClose;
	}	
	
	/**
	 * The delegate, unless the stream is closed
	 */
	private Reader delegate;

	/**
	 * Tells whether close operations shall be propagated to the underlying stream
	 */
	private final boolean propagateClose;

	/**
	 * Ensure that the <code>Reader</code> is open
	 * 
	 * @throws IOException 
	 */
	private final Reader getDelegate(
	) throws IOException{
	    if(this.delegate == null) {
	        throw new IOException("The reader is already closed");
	    }
	    return this.delegate;
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#mark(int)
	 */
    @Override
	public void mark(int readAheadLimit) throws IOException {
	    getDelegate().mark(readAheadLimit);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#markSupported()
	 */
    @Override
	public boolean markSupported() {
        return this.delegate != null && this.delegate.markSupported();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read()
	 */
    @Override
	public int read() throws IOException {
		return getDelegate().read();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(char[], int, int)
	 */
    @Override
	public int read(char[] cbuf, int off, int len) throws IOException {
        return getDelegate().read(cbuf, off, len);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(char[])
	 */
    @Override
	public int read(char[] cbuf) throws IOException {
        return getDelegate().read(cbuf);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#ready()
	 */
    @Override
	public boolean ready() throws IOException {
		return getDelegate().ready();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#reset()
	 */
    @Override
	public void reset() throws IOException {
	    getDelegate().reset();
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#skip(long)
	 */
    @Override
	public long skip(long n) throws IOException {
		return getDelegate().skip(n);
	}

	/* (non-Javadoc)
	 * @see java.io.Reader#close()
	 */
    @Override
	public void close() throws IOException {
	    if(this.delegate != null) {
	        if(this.propagateClose) {
	            this.delegate.close();
	        }
	        this.delegate = null;
	    }
	}


	//------------------------------------------------------------------------
	// Class XMLDeclaration
    //------------------------------------------------------------------------
	
	/**
	 * XML Declaration
	 */
	protected static final class XMLDeclaration {

	    /**
	     * Constructor
	     */
	    protected XMLDeclaration(
	    ){
	        super();
	    }
	    
	    /**
	     * Constructor
	     */
	    protected XMLDeclaration(
	        String version,
	        String encoding,
	        String standalone
	    ) {
	        this.version = version;
	        this.encoding = encoding;
	        this.standalone = standalone;
	    }

	    /**
	     * Constructor
	     */
	    public XMLDeclaration(
	        XMLDeclaration that
	    ) {
	        this(
	            that.version,
	            that.encoding,
	            that.standalone
	        );
	    }

	    /**
	     * The mandatory XML version attribute
	     */
	    private String version;
	    
	    /**
	     * The optional encoding attribute
	     */
	    private String encoding;
	    
	    /**
	     * The optional standalone attribute
	     */
	    private String standalone;

	    /**
	     * Maximal number of characters to read ahead.
	     */
	    static final int READ_AHEAD_LIMIT = 100;
	    
	    /**
	     * 
	     */
	    private static final Pattern XML_DECLARATION_PATTERN;
	    
	    /**
	     * @return Returns the encoding.
	     */
	    public String getEncoding() {
	        return 
	            Encodings.ISO_8859_1.equals(this.encoding) ?
	                "ISO-8859-1" :
	            Encodings.UTF_16BE.equals(this.encoding) ||
	            Encodings.UTF_16BE_WITH_BOM.equals(this.encoding) ?
	                "UTF-16BE" :
	            Encodings.UTF_16LE.equals(this.encoding) ||
	            Encodings.UTF_16LE_WITH_BOM.equals(this.encoding) ?
	                "UTF-16LE" :
	            Encodings.UTF_8.equals(this.encoding) ?
	                "UTF-8" :
	            Encodings.WINDOWS_1252.equals(this.encoding) ?
	                "windows-1252" :
	                this.encoding;
	    }

	    /**
	     * @param encoding The encoding to set.
	     */
	    public void setQuotedEncoding(String encoding) {
	        this.encoding = unquote(encoding);
	    }

	    /**
	     * @return Returns the standalone.
	     */
	    public String getStandalone() {
	        return standalone;
	    }

	    /**
	     * @param standalone The standalone to set.
	     */
	    public void setQuotedStandalone(String standalone) {
	        this.standalone = unquote(standalone);
	    }

	    /**
	     * @param version The version to set.
	     */
	    public void setQuotedVersion(String version) {
	        this.version = unquote(version);
	    }
	    
	    /**
	     * @return Returns the version.
	     */
	    public String getVersion() {
	        return version;
	    }

	    /**
	     * Remove the suurounding &laquo;'&raquo; respectively &laquo;"&raquo; 
	     * characters.
	     *  
	     * @param quoted the embedded string; may be <code>null</code>
	     * @return the quoted string without its leading or trailing character;
	     * or <code>null</code> if quoted was <code>null</code>. 
	     */
	    private static final String unquote(
	        String quoted
	    ){
	        return quoted == null || quoted.length() < 2 ? 
	            null : 
	            quoted.substring(1, quoted.length() - 1);
	    }
	    
	    /**
	     * Consume the XML Declaration and return it or reset the stream otherwise.
	     * 
	     * @param in the stream
	     * @param regexpFactory 
	     * 
	     * @return the XML Declaration; or <code>null</code> if none has been 
	     * specified.
	     * 
	     * @throws IOException  
	     */
	    public static XMLDeclaration readXMLDeclaration(
	        InputStream in
	    ) throws IOException {
	        return readXMLDeclaration(
	            new ASCIIReader(in)
	        );
	    }

	    /**
	     * Consume the XML Declaration and return it or reset the stream otherwise.
	     * 
	     * @param in the stream
	     * @param regexpFactory TODO
	     * 
	     * @return the XML Declaration; or <code>null</code> if none has been 
	     * specified.
	     * 
	     * @throws IOException  
	     */
	    public static XMLDeclaration readXMLDeclaration(
	        Reader in
	    ) throws IOException {
	        in.mark(READ_AHEAD_LIMIT);
	        try {
	            if(
	                in.read() == '<' &&
	                in.read() == '?' &&
	                in.read() == 'x' &&
	                in.read() == 'm' &&
	                in.read() == 'l'
	            ){
	                StringBuffer b = new StringBuffer();
	                xmlDeclaration: for(
	                    int i = 5, c = in.read();
	                    c > 0 && i++ < READ_AHEAD_LIMIT;
	                    c = in.read()
	                ){
	                    if(c == '>'){
	                        Matcher matcher = XML_DECLARATION_PATTERN.matcher(b.toString());
	                        if(!matcher.matches()) break xmlDeclaration;
	                        XMLDeclaration reply = new XMLDeclaration();
	                        reply.setQuotedVersion(matcher.group(1));
	                        reply.setQuotedEncoding(matcher.group(3));
	                        reply.setQuotedStandalone(matcher.group(5));
	                        return reply;
	                    } else {
	                        b.append((char)c);
	                    }
	                }
	            }
	            in.reset();
	            return null;
	        } catch (IOException exception) {
	            in.reset();
	            throw exception;
	        } catch (RuntimeException exception) {
	            in.reset();
	            throw exception;
	        }
	    }
	    

	    //------------------------------------------------------------------------
	    // Extends Object
	    //------------------------------------------------------------------------
	    
	    /* (non-Javadoc)
	     * @see java.lang.Object#toString()
	     */
	    @Override
	    public String toString() {
	        StringBuffer b = new StringBuffer(
	            "<?xml version=\""
	        ).append(
	            getVersion()
	        ).append(
	            '"'
	        );
	        if(this.encoding != null) b.append(
	            " encoding=\""
	        ).append(
	            getEncoding()
	        ).append(
	            '"'
	        );
	        if(this.standalone != null) b.append(
	            " standalone=\""
	        ).append(
	            getStandalone()
	        ).append(
	            '"'
	        );
	        return b.append(
	            "?>"
	        ).toString();
	    }

	    static {
	        String whitespace = "[ \n\r\t]";
	        String optionalWhitespace = whitespace + '*';
	        String mandatoryWhitespace = whitespace + '+';
	        String value = optionalWhitespace + "=" + optionalWhitespace + 
	            "('[^']*'|\"[^\"]*\")";
	        XML_DECLARATION_PATTERN = Pattern.compile(
	            "^" + 
	            mandatoryWhitespace + "version" + value + "(" + 
	            mandatoryWhitespace + "encoding" + value + ")?(" + 
	            mandatoryWhitespace + "standalone" + value + ")?" + 
	            optionalWhitespace + "\\?$"
	        );
	    }

	}

	
    //------------------------------------------------------------------------
    // Class ASICCReader
    //------------------------------------------------------------------------
    
    /**
     * ASCII Reader
     * <p>
     * This <code>InputStream</code> <code>Reader</code> is able to read ASCII
     * characters encoded in any of  the following formats provided the stream 
     * does not contain a byte order mark or any other non-ASCII character up
     * the position it is read through the <code>InputStreamASCIIReader</code>.
     * <ul>
     * <li>US-ASCII
     * <li>ISO-8859-1
     * <li>UTF-8
     * <li>UTF-16
     * <li>UTF-32
     * </ul>
     * The <code>InputStreamASCIIReader</code> is designed not to read ahead.
     */
    protected static class ASCIIReader extends Reader {

        /**
         * Constructor
         * 
         * @param source
         */
        protected ASCIIReader(
            InputStream in
        ) {
            this.in = in;
        }

        /**
         * 
         */
        protected InputStream in;

        /**
         * 
         */
        private int prefix0 = -1;
        
        /**
         * 
         */
        private int suffix0 = -1;
        
        /**
         * Close disconnects the reader from the underlying 
         * <code>InputStream</code> rather than closing it.
         * 
         * @exception IOException
         */
        @Override
        public void close() throws IOException {
            this.in = null;
        }

        /* (non-Javadoc)
         * @see java.io.Reader#read(char[], int, int)
         */
        @Override
        public int read(
            char[] cbuf, 
            int off, 
            int len
        ) throws IOException {
            for(
                int i = 0;
                i < len;
                i++
            ){
                int c = read();
                if(c < 0) return i == 0 ? -1 : i;
                cbuf[i + off] = (char) c;
            }
            return len;
        }

        /* (non-Javadoc)
         * @see java.io.Reader#read()
         */
        @Override
        public int read(
        ) throws IOException {
            if(isEncodingKnown()){
                for(int i = prefix0; i > 0; i--) this.in.read();
                int c = this.in.read();
                for(int i = suffix0; i > 0; i--) this.in.read();
                return c > 127 ? 0 : c;
            } else if (prefix0 < 0) { // prefix0 unkown
                for(
                    int i = 0;
                    i < 4;
                    i++
                ){
                    int c = this.in.read();
                    if(c != 0) {
                        this.prefix0 = i;
                        if(i > 0) this.suffix0 = 0;
                        return c;
                    }
                }
            } else { // suffix0 unknown
                for(
                    int i = 0;
                    i < 4;
                    i++
                ){
                    int c = this.in.read();
                    if(c != 0) {
                        this.suffix0 = i;
                        return c;
                    }
                }
            }
            return 0;
        }

        /**
         * Tells whether the encoding is already konwn
         * 
         * @return true if the encoding is already konwn
         */
        private boolean isEncodingKnown(){
            return prefix0 >= 0 && suffix0 >= 0;
        }
        
        /**
         * Determine the maximal number of bytes per character
         * 
         * @return the maximal number of bytes per character
         */
        private int maxBytesPerCharacter(
        ){
            return isEncodingKnown() ? 1 + prefix0 + suffix0 : 4;
        }
        
        /* (non-Javadoc)
         * @see java.io.Reader#mark(int)
         */
        @Override
        public void mark(int readAheadLimit) throws IOException {
            this.in.mark(
                readAheadLimit * maxBytesPerCharacter()
            );
        }

        /* (non-Javadoc)
         * @see java.io.Reader#markSupported()
         */
        @Override
        public boolean markSupported() {
            return this.in.markSupported();
        }

        /* (non-Javadoc)
         * @see java.io.Reader#reset()
         */
        @Override
        public void reset() throws IOException {
            this.in.reset();
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class Encodings
    //------------------------------------------------------------------------
    
    /**
     * Canonical Encoding Names for <code>java.io</code> and 
     * <code>java.lang</code> API. 
     */
    protected static class Encodings {

        /**
         * Eight-bit UCS Transformation Format
         * 
         * @since JRE 1.2
         */
        protected final static String UTF_8 = "UTF8";

        /**
         * American Standard Code for Information Interchange
         * 
         * @since JRE 1.2
         */
        protected final static String US_ASCII = "ASCII";
        
        /**
         * ISO 8859-1, Latin Alphabet No. 1
         * 
         * @since JRE 1.2
         */
        protected final static String ISO_8859_1 = "ISO8859_1";
            
        /**
         * Sixteen-bit Unicode Transformation Format, big-endian byte order, with byte-order mark 
         * 
         * @since JRE 1.2
         */
        protected final static String UTF_16BE_WITH_BOM = "UnicodeBig";
        
        /**
         * Sixteen-bit Unicode Transformation Format, little-endian byte order, with byte-order mark 
         * 
         * @since JRE 1.2
         */
        protected final static String UTF_16LE_WITH_BOM = "UnicodeLittle";
        
        /**
         * Windows Latin-1
         * 
         * @since JRE 1.2
         */
        protected final static String WINDOWS_1252 = "Cp1252";
        /**
         * Sixteen-bit Unicode Transformation Format, big-endian byte order
         * 
         * @since JRE 1.3
         */
        protected final static String UTF_16BE = "UnicodeBigUnmarked";
        
        /**
         * Sixteen-bit Unicode Transformation Format, little-endian byte order
         * 
         * @since JRE 1.3
         */
        protected final static String UTF_16LE = "UnicodeLittleUnmarked";
        
        /**
         * Sixteen-bit UCS Transformation Format, byte order identified by<ul> 
         * <li>a mandatory initial byte-order mark 
         * @since JRE 1.3
         * </ul>
         * <ul>
         * <li>an optional byte-order mark 
         * @since JRE 1.4
         * </ul>
         */
        protected final static String UTF_16 = "UTF-16";
        
        /**
         * Thirtytwo-bit UCS Transformation Format, byte order identified by an optional byte-order mark.
         */
        protected final static String UTF_32 = "UTF-32";
        
        /**
         * Thirtytwo-bit UCS Transformation Format, big-endian byte order
         */
        protected final static String UTF_32BE = "UTF-32BE";

        /**
         * Thirtytwo-bit UCS Transformation Format, little-endian byte order
         */
        protected final static String UTF_32LE = "UTF-32LE";
        
    }
    
    
    //------------------------------------------------------------------------
    // Class ByteOrderMark
    //------------------------------------------------------------------------
    
    /**
     * Byte Order Mark
     * <p>
     *  The exact bytes 
     *  comprising the BOM will be whatever the Unicode character FEFF is 
     *  converted into by that transformation format. In that form, the BOM 
     *  serves to indicate both that it is a Unicode file, and which of the 
     *  formats it is in. Examples:</p>
     *  <div align="center">
     *    <center>
     *    <table border="1" cellpadding="2" cellspacing="0">
     *      <tr>
     *        <th width="50%">Bytes</th>
     *        <th width="50%">Encoding Form</th>
     *      </tr>
     *      <tr>
     *        <td width="50%">EF BB BF</td>
     *        <td width="50%">UTF-8</td>
     *      </tr>
     *      <tr>
     *        <td width="50%">00 00 FE FF</td>
     *        <td width="50%">UTF-32, big-endian</td>
     *      </tr>
     *      <tr>
     *        <td width="50%">FF FE 00 00</td>
     *        <td width="50%">UTF-32, little-endian</td>
     *      </tr>
     *      <tr>
     *        <td width="50%">FE FF</td>
     *        <td width="50%">UTF-16, big-endian</td>
     *      </tr>
     *      <tr>
     *        <td width="50%">FF FE</td>
     *        <td width="50%">UTF-16, little-endian</td>
     *      </tr>
     *    </table>
     *    </center>
     *  </div>
     */
    protected static class ByteOrderMark {

        /**
         * Constructor 
         */
        private ByteOrderMark() {
            // Avoid instantiation
        }

        /**
         * The Unicode character point used as byte order mark.
         */
        public final static char VALUE = 0xFEFF;
        
        /**
         * Each <code>ENCODINGS</code> entry corresponds to a
         * <code>REPRESENTATIONS</code> entry.
         */
        final public static String[] ENCODINGS = {
            Encodings.UTF_8, 
            Encodings.UTF_32BE,
            Encodings.UTF_32LE,
            Encodings.UTF_16BE,
            Encodings.UTF_16LE      
        };

        /**
         * Each <code>REPRESENTATIONS</code> entry corresponds to an 
         * <code>ENCODINGS</code> entry.
         */
        final public static byte[][] REPRESENTATIONS = new byte[][]{
            new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF},
            new byte[]{(byte)0x00, (byte)0x00, (byte)0xFE, (byte)0xFF},
            new byte[]{(byte)0xFF, (byte)0xFE, (byte)0x00, (byte)0x00},
            new byte[]{(byte)0xFE, (byte)0xFF},
            new byte[]{(byte)0xFF, (byte)0xFE}
        };

        /**
         * Consume the input stream's byte order mark if any and return the 
         * corresponding encoding or reset the input stream otherwise.
         * 
         * @param in the input stream
         * 
         * @return the byte order mark's encoding; or <code>null</code> in absence
         * of a byte order mark.
         * 
         * @throws IOException  
         */
        public static String readByteOrderMark(
            InputStream in
        ) throws IOException {
            in.mark(4);
            byte[] head = new byte[4];
            int limit = in.read(head);
            encodings: for(
                int encoding = 0;
                encoding < REPRESENTATIONS.length;
                encoding++
            ){
                byte[] bom = ByteOrderMark.REPRESENTATIONS[encoding];
                if(limit < bom.length) continue encodings;
                for(
                    int j = 0;
                    j < bom.length;
                    j++
                ) if(
                    bom[j] != head[j]
                ) continue encodings;
                in.reset();
                in.skip(bom.length);
                return ByteOrderMark.ENCODINGS[encoding];               
            }
            in.reset();
            return null;
        }

    }

}

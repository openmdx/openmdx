/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Validating Document Builder 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2013, OMEX AG, Switzerland
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

package org.openmdx.kernel.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmdx.kernel.log.SysLog;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validating Document Builder
 * <p><pre>
 * [ 1]    document ::= prolog element Misc* 
 * [22]    prolog ::= XMLDecl? Misc* (doctypedecl Misc*)? 
 * [23]    XMLDecl ::= '&lt;?xml' VersionInfo EncodingDecl? SDDecl? S? '?&gt;'
 * [24]    VersionInfo ::= S 'version' Eq ("'" VersionNum "'" | '"' VersionNum '"')
 * [25]    Eq ::= S? '=' S?
 * [26]    VersionNum ::= '1.0'
 * [27]    Misc ::= Comment | PI | S
 * </pre>  
 */
public class ValidatingDocumentBuilder {
    
    /**
     * Constructor 
     * 
     * @throws ParserConfigurationException 
     */
    protected ValidatingDocumentBuilder(
    ) throws ParserConfigurationException {
        this.dtdDocumentBuilderFactory = newDocumentBuilderFactory(null);
        this.xsdDocumentBuilderFactory = newDocumentBuilderFactory("http://www.w3.org/2001/XMLSchema");
    }

    /**
     * Create a validating document builder factory for DTD of schema based documents.
     * 
     * @param schemaBased whether the documents are DTD or schmea based
     * 
     * @return a new document builder factory
     * 
     * @throws ParserConfigurationException
     */
    private static DocumentBuilderFactory newDocumentBuilderFactory(
        String schemaLanguage
    ) throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(true);
        if(schemaLanguage != null) { 
            documentBuilderFactory.setAttribute(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage", 
                schemaLanguage
            );
            documentBuilderFactory.setNamespaceAware(true);
        }
        return documentBuilderFactory;
    }
    
    /**
     * Create a validating document builder instance.
     * 
     * @return a new validating document builder instance
     * 
     * @throws ParserConfigurationException
     */
    public static ValidatingDocumentBuilder newInstance(
    ) throws ParserConfigurationException{
        return new ValidatingDocumentBuilder();
    }
    
    /**
     * Parse a document
     * 
     * @param url the document's url
     * 
     * @return the document
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public Document parse(
        URL url
    ) throws ParserConfigurationException, SAXException, IOException {
        String namespace = xmlNamespace(url);
        boolean dtd = namespace == null;
        DocumentBuilder documentBuilder = (
            dtd ? dtdDocumentBuilderFactory : xsdDocumentBuilderFactory
        ).newDocumentBuilder();
        documentBuilder.setEntityResolver(EntityMapper.getInstance());
        documentBuilder.setErrorHandler(new DocumentErrorHandler(url));
        final Document document;
        try(InputStream stream = url.openStream()){
            document = dtd ? 
                documentBuilder.parse(stream) :
                documentBuilder.parse(stream, namespace + '/');
        }
        return document;
    }

    /**
     * Test whether a document is DTD or schema based
     * 
     * @param url the document's url
     *
     * @return the namespace in case of schema based documents,
     * <code>null</code> in case of dtd based documents
     * 
     * @throws IOException 
     */
    protected String xmlNamespace(
        URL url
    ) throws IOException {
        try (
            Reader in = new AdaptiveInputStreamReader(
                url.openStream(),
                null, // encoding 
                true, // byteOrderMarkAware 
                true, // xmlDeclarationAware
                true // popagateClode
            );
        ) {
            char[] charArray = new char[READ_AHEAD_LIMIT];
            int l = in.read(charArray);
            CharBuffer charBuffer = CharBuffer.wrap(charArray, 0, l);
            Matcher startMatcher = TRUNCATED_DOCUMENT.matcher(charBuffer);
            if(startMatcher.matches()) {
                if(startMatcher.group(1) == null) {
                    String start = startMatcher.group(2);
                    int i = start.indexOf("xmlns=\"");
                    if(i > 0) {
                        int j = start.indexOf('"', i + 7);
                        String namespace = start.substring(i + 7, j);
                        return namespace;
                    }
                }
            } else {
                SysLog.log(
                    Level.WARNING, 
                    "Don't know how to validate the document at URL {0}", 
                    url
                );
            }
            return null;
        }
    }
    
    /**
     * Schema Document Builder Factory 
     */
    private DocumentBuilderFactory xsdDocumentBuilderFactory;

    /**
     * DTD Document Builder Factory
     */
    private DocumentBuilderFactory dtdDocumentBuilderFactory;
    
    /**
     * How far to look for the element start.
     */
    private final static int READ_AHEAD_LIMIT = 10000;
    
    /**
     * In case of UTF-32
     */
    final static int MAXIMAL_NUMBER_OF_BYTES_PER_CHARACTER = 4;

    /**
     * 
     */
    private static final Pattern TRUNCATED_DOCUMENT;
    
    static {
        final String comment = "<!--(?:[^-]+-)+->"; 
        final String pi = "<\\?[^?>]+\\?>";
        final String misc = "(?:" + comment + "|" + pi + "|\\s)";
        final String doctypedecl = "<!DOCTYPE[^>]+>";
        final String elementStart = "<[^?!][^>]+>";
        TRUNCATED_DOCUMENT = Pattern.compile("(?s)" + misc + "*(" + doctypedecl + ")?" + misc + "*(" + elementStart + ").*");
    }
    
    
    /**
     * Class  DeploymentErrorHandler
     */
    static class DocumentErrorHandler implements ErrorHandler {

        /**
         * Constructor 
         *
         * @param documentURL
         */
        DocumentErrorHandler(
            URL documentURL
        ) {
            this.documentURL = documentURL;
        }

        /**
         * 
         */
        private final URL documentURL;
        
        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        public void error(
            SAXParseException exception
        ) throws SAXException {
            String message = newMessage(exception, "error");
            SysLog.log(Level.SEVERE, message);
            System.err.println(message);
        }

        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        public void fatalError(
            SAXParseException exception
        ) throws SAXException {
            String message = newMessage(exception, "fatal error");
            SysLog.log(Level.SEVERE, message);
            System.err.println(message);
        }
        
        /* (non-Javadoc)
         * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        public void warning(
            SAXParseException exception
        ) throws SAXException {
            String message = newMessage(exception, "warning");
            SysLog.log(Level.WARNING,message);
            System.err.println(message);
        }

        /**
         * Build the message
         * 
         * @param exception
         * @param level
         * 
         * @return the complete exception message
         */
        private String newMessage(
            SAXParseException exception,
            String level
        ){
            return new StringBuilder(
                "Parsing "
            ).append(
                level
            ).append(
                " in "
            ).append(
                documentURL.toExternalForm()
            ).append(
                " on line "
            ).append(
                exception.getLineNumber()
            ).append(
                ": "
            ).append(
                exception.getMessage()
            ).toString();
        }

    }
    
}

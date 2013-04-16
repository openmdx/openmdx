/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XML Importer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.application.xml.spi;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.wbxml.WBXMLReader;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * XML Importer
 */
public class ImportHelper {

    /**
     * Constructor 
     * <p>
     * Validation and schema validation are disabled
     */
	public ImportHelper(
	) {		
	    this(false);
	}

	/**
	 * Constructor 
	 *
	 * @param xmlValidation tells whether validation and schema validation are enabled or disabled
	 */
    public ImportHelper(
        boolean xmlValidation
    ) {     
        this.xmlValidation = xmlValidation;
    }
	
    /**
     * The schema validation flag
     */
    private boolean xmlValidation;
	
    /**
     * Create an XML reader
     * 
     * @param importHandler
     * @param errorHandler
     * 
     * @return a new <code>XMLReader</code>
     * 
     * @throws ServiceException
     */
    private XMLReader newReader(
        ImportHandler importHandler, 
        ErrorHandler errorHandler
    ) throws ServiceException {
        try {
            XMLReader reader;
            boolean xmlValidation;
            if(importHandler.isBinary()) {
                reader = new WBXMLReader();
                xmlValidation = false;
            } else {
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                SysLog.detail("SAX Parser", parser.getClass().getName());
                reader = parser.getXMLReader();
                xmlValidation = this.xmlValidation;
            }
            SysLog.detail("XML Reader", reader.getClass().getName());
            //
            // Features
            //
            setFeature(
                reader, 
                "http://xml.org/sax/features/namespaces", 
                true
            );
            setFeature(
                reader,
                "http://xml.org/sax/features/validation",
                xmlValidation
            );
            setFeature(
                reader,
                "http://apache.org/xml/features/validation/schema",
                xmlValidation
            );
            // 
            // Handlers
            //
            reader.setContentHandler(importHandler);
            reader.setDTDHandler(importHandler);
            reader.setErrorHandler(errorHandler == null ? importHandler : errorHandler);
            reader.setEntityResolver(importHandler);
            return reader;
        } catch (ParserConfigurationException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Unable to acquire a SAX Parser");
        } catch (SAXException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Unable to acquire a SAX Parser");
        }
    }

    /**
     * Set the value of a feature.
     * 
     * @param reader
     * @param feature
     * @param value
     */
    private static void setFeature(
        XMLReader reader,
        String feature,
        boolean value
    ) {
        try {
            reader.setFeature(feature, value);
        } catch (SAXException e) {
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Unable to set SAXReader feature",
                new BasicException.Parameter("feature", feature),
                new BasicException.Parameter("vaue", value));
        }
    }
    
    /**
     * Input source factory method
     * 
     * @param source the input source specification
     * 
     * @return the corresponding <code>InputSource Enumeration</code>
     */
    public static Iterable<InputSource> asSource(
        InputSource source
    ) {
        return new StandardSource(source);
    }

    /**
     * Import  
     * 
     * @param target the object sink
     * @param source the XML source
     * @param errorHandler <code>null</code> leads to standard error handling
     * 
     * @throws ServiceException  
     */
    public void importObjects (
        ImportTarget target,
        Iterable<InputSource> sources,
        ErrorHandler errorHandler
    ) throws ServiceException {
        target.importProlog();
        boolean success = false;
        try {
            for(InputSource source : sources) {
                try {
                    newReader(
                        new ImportHandler(
                            target, 
                            source
                        ), 
                        errorHandler
                    ).parse(
                        source
                    );
                } catch (SAXException exception) {
                    Exception cause = exception.getException();
                    throw new ServiceException(
                        cause != null ? cause : exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "exception while parsing",
                        new BasicException.Parameter("systemId", source.getSystemId()),
                        new BasicException.Parameter("publicId", source.getPublicId())
                    );
                } catch (RuntimeException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "unknown exception",
                        new BasicException.Parameter("systemId", source.getSystemId()),
                        new BasicException.Parameter("publicId", source.getPublicId())
                    );
                } catch (IOException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "unknown exception",
                        new BasicException.Parameter("systemId", source.getSystemId()),
                        new BasicException.Parameter("publicId", source.getPublicId())
                    );
                }
            }
            success = true;
        } catch (RuntimeServiceException exception) {
            throw new ServiceException(exception);
        } finally {
            target.importEpilog(success);
        }
    }

    
    //------------------------------------------------------------------------
    // Class StandardSource
    //------------------------------------------------------------------------
    
    /**
     * Allows to iterate once over the <code>InputSource</code> singleton
     */
    protected static class StandardSource implements Iterable<InputSource> {

        /**
         * Constructor 
         *
         * @param singleton
         */
        protected StandardSource(
            InputSource singleton
        ) {
            this.singleton = singleton;
            this.credit = singleton.getByteStream() == null && singleton.getCharacterStream() == null ? Integer.MAX_VALUE : 1; 
        }

        /**
         * The <code>InputSource</code> singleton
         */
        private final InputSource singleton;
        
        /**
         * Access count
         */
        private int credit; 
        
        /* (non-Javadoc)
         * @see java.lang.Iterable#iterator()
         */
        public Iterator<InputSource> iterator(
        ) {
            if(this.credit-- <= 0) throw new IllegalStateException(
                "A stream source is expcected to be accessed once only"
            );
            return Collections.singleton(this.singleton).iterator(); 
        }

        
    }
    
}

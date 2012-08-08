/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RestFormat.java,v 1.5 2010/04/08 17:30:37 wfro Exp $
 * Description: RecordFormat 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/08 17:30:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.base.rest.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.base.accessor.rest.spi.ControlObjects_2;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.HttpHeaderFieldContent;
import org.openmdx.base.io.HttpHeaderFieldValue;
import org.openmdx.base.io.ObjectInputStream;
import org.openmdx.base.io.ObjectOutputStream;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.wbxml.WBXMLReader;
import org.openmdx.base.xml.stream.CharacterWriter;
import org.openmdx.base.xml.stream.LargeObjectWriter;
import org.openmdx.base.xml.stream.XMLOutputFactories;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.format.DateTimeFormat;
import org.w3c.spi2.Datatypes;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * RecordFormat
 */
public class RestFormat {

    /**
     * Constructor
     */
    private RestFormat() {
        // Avoid instantiation
    }

    /**
     * 
     */
    private static final String ITEM_TAG = "_item";

    /**
     * Tells whether the stack trace shall be included
     */
    private static final boolean INCLUDE_STACK_TRACE = true;
    
    /**
     * The XML Readers
     */
    private static final ThreadLocal<XMLReader> xmlReaders = new ThreadLocal<XMLReader>() {

        protected XMLReader initialValue() {
            try {
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                XMLReader reader = parser.getXMLReader();
                reader.setFeature("http://xml.org/sax/features/namespaces", true);
                reader.setFeature("http://xml.org/sax/features/validation", false);
                return reader;
            } catch (Exception e) {
                throw new RuntimeServiceException(e);
            }
        }
    };

    /**
     * The XML Readers
     */
    private static final ThreadLocal<XMLReader> wbxmlReaders = new ThreadLocal<XMLReader>() {

        protected XMLReader initialValue() {
            try {
                return new WBXMLReader(new WBXMLPlugIn());
            } catch (Exception e) {
                throw new RuntimeServiceException(e);
            }
        }
    };

    /**
     * An <code>XMLOutputFactory</code> cache
     */
    private static final ConcurrentMap<String,XMLOutputFactory> xmlOutputFactories = new ConcurrentHashMap<String, XMLOutputFactory>();

    /**
     * Retrieve an XML Reader instance
     * 
     * @param wbxml
     * 
     * @return an XML Reader instance
     * @throws SAXNotSupportedException 
     * @throws SAXNotRecognizedException 
     */
    private static XMLReader getReader(
        Source source
    ) throws SAXNotRecognizedException, SAXNotSupportedException {
        if(source.isWBXML()) {
            XMLReader xmlReader = wbxmlReaders.get();
            xmlReader.setFeature(WBXMLReader.EXHAUST, source.isToBeExhausted());
            return xmlReader;
        } else {
            return xmlReaders.get();
        }
    }

    /**
     * Retrieve and cache MIME type dependent XML output factories
     * 
     * @param mimeType
     * 
     * @return a MIME type specific XML output factory
     */
    public static XMLOutputFactory getOutputFactory(
        String mimeType
    ){
        int pos;
        if((pos = mimeType.indexOf(";")) > 0) {
            mimeType = mimeType.substring(0, pos);
        }
        XMLOutputFactory xmlOutputFactory = xmlOutputFactories.get(mimeType);
        if(xmlOutputFactory == null) {
            XMLOutputFactory concurrent = xmlOutputFactories.putIfAbsent(
                mimeType, 
                xmlOutputFactory = XMLOutputFactories.newInstance(mimeType)
            );
            return concurrent == null ? xmlOutputFactory : concurrent;
        } else {
            return xmlOutputFactory;
        }
    }
    
    /**
     * Parse a record
     * 
     * @param source
     *            the source providing the record
     * 
     * @param xri
     *            optional request path. If null the path is derived from source
     * 
     * @return either a structure or an object record
     * 
     * @throws ServiceException
     */
    public static MappedRecord parseRequest(
        Source source, 
        Path xri
    ) throws ServiceException {
        try {
            StandardHandler handler = new StandardHandler(source);
            XMLReader reader = getReader(source);
            reader.setContentHandler(handler);
            reader.parse(source.getBody());
            return handler.getValue(xri);
        } catch (IOException exception) {
            throw new ServiceException(exception);
        } catch (SAXException exception) {
            throw new ServiceException(exception);
        } catch (RuntimeServiceException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Parse a collection
     * 
     * @param target
     * @param source
     * 
     * @throws ServiceException
     */
    public static void parseResponse(
        Record target, 
        Source source
    ) throws ServiceException {
        try {
            StandardHandler handler = new StandardHandler(target, source);
            XMLReader reader = getReader(source);
            reader.setContentHandler(handler);
            reader.parse(source.getBody());
        } catch (IOException exception) {
            throw new ServiceException(exception);
        } catch (SAXException exception) {
            throw new ServiceException(exception);
        } catch (RuntimeServiceException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Parse an exception stack
     * 
     * @param source
     *            the source providing the exception stack
     * 
     * @return a <code>BasicException</code>
     * 
     * @throws ServiceException
     */
    public static BasicException parseException(
        Source source
    ) throws ServiceException {
        try {
            ExceptionHandler handler = new ExceptionHandler();
            XMLReader reader = getReader(source);
            reader.setContentHandler(handler);
            reader.parse(source.getBody());
            return handler.getValue();
        } catch (IOException exception) {
            throw new ServiceException(exception);
        } catch (SAXException exception) {
            throw new ServiceException(exception);
        } catch (RuntimeServiceException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Retrieve the base URL
     * 
     * @return the base URL
     */
    public static String getBase(
        HttpServletRequest request
    ) {
        StringBuffer hrefPrefix = request.getRequestURL();
        hrefPrefix.setLength(hrefPrefix.indexOf(request.getServletPath()));
        return hrefPrefix.append('/').toString();
    }

    /**
     * Provide a <code>parse()</code> source
     * 
     * @param source
     *            the HTTP request
     * 
     * @return a <code>Source</code>
     * 
     * @throws ServiceException
     */
    public static Source asSource(
        HttpServletRequest request
    ) throws ServiceException {
        try {
            HttpHeaderFieldContent contentType = new HttpHeaderFieldValue(
                request.getHeaders("Content-Type")
            ).getPreferredContent(
                "application/xml;charset=UTF-8"
            );
            String mimeType = contentType.getValue();
            String encoding = contentType.getParameterValue("charset", null);
            InputSource inputSource = "application/vnd.openmdx.wbxml".equals(mimeType) ? new InputSource(
                request.getInputStream()
            ) : new InputSource (
                request.getReader()
            );
            inputSource.setEncoding(encoding);
            return new Source(
                getBase(request), 
                inputSource, 
                mimeType
            );
        } catch (IOException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Provide a <code>parse()</code> source
     * 
     * @param source the <code>ObjectInput</code>
     * 
     * @return a <code>Source</code>
     */
    public static Source asSource(
        ObjectInput input
    ){
        return new Source(
            new InputSource(
                input instanceof InputStream ? (InputStream) input : new ObjectInputStream(input)
            )
        );
    }
    
    /**
     * Provide a <code>format()</code> target
     * 
     * @param source the <code>ObjectOutput</code>
     * 
     * @return a <code>Target</code>
     */
    public static Target asTarget(
        final ObjectOutput output
    ){
        return new Target("./"){

            @Override
            protected XMLStreamWriter newWriter(
            ) throws XMLStreamException {
                return getOutputFactory(
                    "application/vnd.openmdx.wbxml"
                ).createXMLStreamWriter(
                    output instanceof OutputStream ? (OutputStream) output : new ObjectOutputStream(output)
                );
            }
            
        };
    }
    
    /**
     * Format Object
     * 
     * @param target
     * @param source
     * 
     * @throws ServiceException
     */
    public static void format(
        Target target, 
        Object_2Facade source
    ) throws ServiceException {
        Path resourceIdentifier = source.getPath();
        printRecord(
            target,
            0,
            resourceIdentifier,
            resourceIdentifier.getBase(),
            (byte[]) source.getVersion(),
            source.getValue()
        );
    }

    /**
     * Format Query
     * 
     * @param target
     * @param source
     * 
     * @throws ServiceException
     */
    public static void format(
        Target target, 
        Query_2Facade source
    ) throws ServiceException {
        printRecord(
            target, 
            0, 
            null, // XRI
            "query",
            null, // Version
            source.getDelegate()
        );
    }

    /**
     * Format Result Set
     * 
     * @param target
     * @param xri
     * @param source
     * 
     * @throws ServiceException
     */
    public static void format(
        Target target, 
        Path xri, 
        IndexedRecord source
    ) throws ServiceException {
        try {
            XMLStreamWriter writer = target.getWriter();
            writer.writeStartElement("org.openmdx.kernel.ResultSet");
            writer.writeAttribute("href", target.toURL(xri));
            if (source instanceof ResultRecord) {
                ResultRecord result = (ResultRecord) source;
                Boolean more = result.getHasMore();
                if (more != null) {
                    writer.writeAttribute("hasMore", more.toString());
                }
                Long total = result.getTotal();
                if (total != null) {
                    writer.writeAttribute("total", total.toString());
                }
            }
            for (Object record : source) {
                Object_2Facade object = Object_2Facade.newInstance((MappedRecord) record);
                printRecord(
                    target, 
                    1, 
                    object.getPath(), 
                    object.getPath().getBase(), 
                    (byte[]) object.getVersion(), 
                    object.getValue()
               );
            }
            writer.writeEndElement(); // "org.openmdx.kernel.ResultSet"
            writer.writeEndDocument();
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        } catch (XMLStreamException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Format Operation
     * 
     * @param target
     * @param source
     * 
     * @throws ServiceException
     */
    public static void format(
        Target target, 
        MessageRecord source
    ) throws ServiceException {
        printRecord(
            target, 
            0, 
            source.getPath(), 
            "result",
            null, // Version
            source.getBody()
        );
    }

    /**
     * Print Value
     * 
     * @param target
     * @param indent
     * @param tag
     * @param index
     * @param value
     * 
     * @throws ServiceException
     */
    private static void printValue(
        Target target,
        int indent,
        String tag,
        Object index,
        Object value)
    throws ServiceException {
        try {
            XMLStreamWriter writer = target.getWriter();
            if(value == null && index == null)  {
                writer.writeEmptyElement(tag);
            } else {
                writer.writeStartElement(tag);
                if (index != null) {
                    writer.writeAttribute("index", index.toString());
                }
                if (value == null) {
                    // wait for the tag to be closed
                } else if (value instanceof Path) {
                    Path xri = (Path) value;
                    writer.writeAttribute("href", target.toURL(xri));
                    writer.writeCharacters(xri.toXRI());
                } else if (value instanceof String) {
                    writer.writeCData((String) value);
                } else if (value instanceof Date) {
                    writer.writeCharacters(
                        DateTimeFormat.EXTENDED_UTC_FORMAT.format((Date) value)
                    );
                } else if (value instanceof char[]) {
                    char[] text = (char[]) value;
                    writer.writeCharacters(text, 0, text.length);
                } else if (writer instanceof LargeObjectWriter) {
                    if (value instanceof BinaryLargeObject) {
                        ((LargeObjectWriter) writer).writeBinaryData(
                            (BinaryLargeObject) value
                        );
                    } else if (value instanceof CharacterLargeObject) {
                        ((LargeObjectWriter) writer).writeCharacterData(
                            (CharacterLargeObject) value
                        );
                    } else if (value instanceof InputStream) {
                        ((LargeObjectWriter) writer).writeBinaryData(
                            BinaryLargeObjects.valueOf((InputStream) value)
                        );
                    } else if (value instanceof Reader) {
                        ((LargeObjectWriter) writer).writeCharacterData(
                            CharacterLargeObjects.valueOf((Reader) value)
                        );
                    } else if (value instanceof byte[]) {
                        ((LargeObjectWriter) writer).writeBinaryData(
                            BinaryLargeObjects.valueOf((byte[]) value)
                        );
                    } else {
                        //
                        // Data types other than large objects
                        //
                        writer.writeCharacters(value.toString());
                    }
                } else {
                    if (value instanceof BinaryLargeObject) {
                        Base64.encode(
                            ((BinaryLargeObject) value).getContent(),
                            new CharacterWriter(writer)
                        );
                    } else if (value instanceof CharacterLargeObject) {
                        CharacterLargeObject source = (CharacterLargeObject) value;
                        Long length = source.getLength();
                        StringWriter data = length == null ? new StringWriter(
                        ) : new StringWriter(
                            length.intValue()
                        );
                        CharacterLargeObjects.streamCopy(
                            source.getContent(),
                            0l,
                            data
                        );
                        writer.writeCData(data.toString());    

                    } else if (value instanceof InputStream) {
                        Base64.encode(
                            (InputStream) value, 
                            new CharacterWriter(writer)
                        );
                    } else if (value instanceof Reader) {
                        StringWriter data = new StringWriter();
                        CharacterLargeObjects.streamCopy((Reader) value, 0l, data);
                        writer.writeCData(data.toString());
                    } else if (value instanceof byte[]) {
                        writer.writeCharacters(Base64.encode((byte[]) value));
                    } else {
                        //
                        // Data types other than large objects
                        //
                        writer.writeCharacters(value.toString());
                    }
                }
                writer.writeEndElement(); // tag
            }
        } catch (IOException exception) {
            throw new ServiceException(exception);
        } catch (XMLStreamException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Print Exception
     * 
     * @param target
     * @param exception
     * @throws ServiceException
     */
    public static void format(
        Target target, 
        BasicException source
    ) {
        try {
            XMLStreamWriter writer = target.getWriter();
            writer.writeStartElement("org.openmdx.kernel.Exception");
            StackTraceElement[] lastTrace = null;
            for (BasicException entry : source.getExceptionStack()) {
                writer.writeStartElement("element");
                writer.writeAttribute("exceptionDomain", entry.getExceptionDomain());
                writer.writeAttribute("exceptionCode", String.valueOf(entry.getExceptionCode()));
                Date exceptionTime = entry.getTimestamp();
                if (exceptionTime != null) {
                    writer.writeAttribute(
                        "exceptionTime",
                        DateTimeFormat.EXTENDED_UTC_FORMAT.format(exceptionTime)
                    );
                }
                String exceptionClass = entry.getExceptionClass();
                if (exceptionClass != null) {
                    writer.writeAttribute("exceptionClass", exceptionClass);
                }
                String methodName = entry.getMethodName(!INCLUDE_STACK_TRACE);
                if (methodName != null) {
                    writer.writeAttribute("methodName", methodName);
                    Integer lineNumber = entry.getLineNr(!INCLUDE_STACK_TRACE);
                    if (lineNumber != null) {
                        writer.writeAttribute("lineNumber", String.valueOf(lineNumber));
                    }
                }
                String description = entry.getDescription();
                if (description == null) {
                    writer.writeEmptyElement("description");
                } else {
                    writer.writeStartElement("description");
                    writer.writeCData(entry.getDescription());
                    writer.writeEndElement(); // "description"
                }
                BasicException.Parameter[] parameters = entry.getParameters();
                if (parameters.length == 0) {
                    writer.writeEmptyElement("parameter");
                } else {
                    writer.writeStartElement("parameter");
                    for (BasicException.Parameter parameter : parameters) {
                        writer.writeStartElement(ITEM_TAG);
                        writer.writeAttribute("id", parameter.getName());
                        String value = parameter.getValue();
                        if (value != null) {
                            writer.writeCData(parameter.getValue());
                        }
                        writer.writeEndElement(); // ITEM_TAG
                    }
                    writer.writeEndElement(); // parameter
                }
                if(INCLUDE_STACK_TRACE) {
                    writer.writeStartElement("stackTraceElements");
                    StackTraceElement[] stackTrace = entry.getStackTrace();
                    int more = 0;
                    if(lastTrace != null){
                        for(
                            int l = lastTrace.length - 1, s = stackTrace.length - 1;
                            l >= 0 && s >= 0 && lastTrace[l].equals(stackTrace[s]);
                            l--,s--
                        ){
                            more++;
                        }
                        writer.writeAttribute("more", Integer.toString(more));
                    }
                    lastTrace = stackTrace;
                    for(
                        int s = 0, sLimit = stackTrace.length - more;
                        s < sLimit;
                        s++
                    ){
                        StackTraceElement element = stackTrace[s];
                        writer.writeStartElement(ITEM_TAG);
                        writer.writeAttribute("declaringClass", element.getClassName());
                        writer.writeAttribute("methodName", element.getMethodName());
                        String fileName = element.getFileName();
                        if(fileName != null) {
                            writer.writeAttribute("fileName", fileName);
                        }
                        writer.writeAttribute("lineNumber", Integer.toString(element.getLineNumber()));
                        writer.writeEndElement(); // ITEM_TAG
                    }
                    writer.writeEndElement(); // parameter
                } else {
                    writer.writeEmptyElement("stackTraceElements");
                }
                writer.writeEndElement(); // element
            }
            writer.writeEndElement(); // "org.openmdx.kernel.Exception"
        } catch (XMLStreamException internal) {
            Throwables.log(internal);
        }
    }

    /**
     * Print Record
     * 
     * @param target
     * @param indent
     * @param xri
     * @param id
     * @param version
     * @param record
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private static void printRecord(
        Target target,
        int indent,
        Path xri,
        String id,
        byte[] version,
        MappedRecord record)
    throws ServiceException {
        try {
            XMLStreamWriter writer = target.getWriter();
            String tag = record.getRecordName().replace(':', '.');
            writer.writeStartElement(tag);
            writer.writeAttribute("id", id);
            writer.writeAttribute("href", target.toURL(xri));
            if (version != null) {
                writer.writeAttribute("version", Base64.encode(version));
            }
            Set<Map.Entry<String, ?>> entries = record.entrySet();
            for (Map.Entry<String, ?> entry : entries) {
                String feature = entry.getKey();
                Object value = entry.getValue();
                try {
                    if (value instanceof Collection<?>) {
                        //
                        // "list" or "set"
                        //
                        Collection<?> values = (Collection<?>) value;
                        if (values.isEmpty()) {
                            writer.writeEmptyElement(feature);
                        } else {
                            writer.writeStartElement(feature);
                            for (Object v : values){
                                try {
                                    printValue(
                                        target,
                                        indent + 2,
                                        ITEM_TAG,
                                        null, // index
                                        v);
                                } catch (Exception exception) {
                                    if (SysLog.isTraceOn())SysLog.trace(
                                        "Collection element print failure",
                                        new ServiceException(
                                            exception,
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.PROCESSING_FAILURE,
                                            "Unable to retrieve feature value",
                                            new BasicException.Parameter(
                                                "hrefContext",
                                                target.getBase()
                                            ),
                                            new BasicException.Parameter(
                                                "xri",
                                                xri.toXRI()
                                            ),
                                            new BasicException.Parameter(
                                                "feature",
                                                feature
                                            )
                                        )
                                    );
                                }
                            }
                            writer.writeEndElement(); // feature
                        }
                    } else if (value instanceof Map) {
                        //
                        // "sparsearray"
                        //
                        Map<?, ?> values = (Map<?, ?>) value;
                        if (values.isEmpty()) {
                            writer.writeEmptyElement(feature);
                        } else {
                            writer.writeStartElement(feature);
                            for (Map.Entry<?, ?> e : values.entrySet()){
                                try {
                                    printValue(
                                        target,
                                        indent + 2,
                                        ITEM_TAG,
                                        e.getKey(),
                                        e.getValue()
                                    );
                                } catch (Exception exception) {
                                    if (SysLog.isTraceOn()) SysLog.trace(
                                        "Collection element print failure",
                                        new ServiceException(
                                            exception,
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.PROCESSING_FAILURE,
                                            "Unable to retrieve feature value",
                                            new BasicException.Parameter(
                                                "hrefContext",
                                                target.getBase()
                                            ),
                                            new BasicException.Parameter(
                                                "xri",
                                                xri.toXRI()
                                            ),
                                            new BasicException.Parameter(
                                                "feature",
                                                feature)
                                            )
                                     );
                                }
                            }
                            writer.writeEndElement(); // feature
                        }
                    } else {
                        printValue(
                            target, 
                            indent + 1, 
                            feature, 
                            null, // index
                            value
                        );
                    }
                } catch (Exception exception) {
                    if (SysLog.isTraceOn())SysLog.trace(
                        "Collection element print failure",
                        new ServiceException(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.PROCESSING_FAILURE,
                            "Unable to retrieve feature value",
                            new BasicException.Parameter(
                                "hrefContext",
                                target.getBase()),
                            new BasicException.Parameter("xri", xri
                                .toXRI()),
                            new BasicException.Parameter(
                                "feature",
                                feature
                            )
                        )
                    );
                }
            }
            writer.writeEndElement(); // tag
        } catch (XMLStreamException exception) {
            throw new ServiceException(exception);
        }
    }

    
    // ------------------------------------------------------------------------
    // Class Source
    // ------------------------------------------------------------------------

    /**
     * Source
     */
    public static class Source {

        /**
         * Constructor 
         *
         * @param body
         */
        Source(
            InputSource body
        ){
            this.base = "./";
            this.body = body;
            this.wbxml = true;
            this.exhaust = false;
        }
        
        /**
         * Constructor
         * 
         * @param uri
         * @param body
         * @param mimeType
         */
        public Source(
            String uri, 
            InputSource body, 
            String mimeType
        ) {
            this.base = uri;
            this.body = body;
            this.wbxml = "application/vnd.openmdx.wbxml".equals(mimeType);
            this.exhaust = true;
        }
        
        /**
         * The HREF prefix
         */
        protected final String base;

        /**
         * The input source
         */
        private final InputSource body;

        /**
         * The WBXML flag
         */
        private final boolean wbxml;
        
        /**
         * The exhaust flag
         */
        private final boolean exhaust;
        
        /**
         * Retrieve the resource identifier for a given href URL
         * 
         * @param url
         *            the href URL
         * 
         * @return the XRI for the given href URL
         */
        protected Path getXRI(
            String url
        ) throws ServiceException {
            if (url.startsWith(this.base)) {
                String xri = url.substring(this.base.length());
                return new Path((xri.startsWith("!") ? "xri://@openmdx"
                    : "xri://@openmdx*")
                    + xri
                );
            } else
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "The URL does not start with the expected base URL",
                    new BasicException.Parameter("base", this.base),
                    new BasicException.Parameter("url", url)
               );
        }

        /**
         * Set up and retrieve the <code>InputSource</code>
         * 
         * @return the <code>InputSource</code>
         * 
         * @throws ServiceException
         */
        protected InputSource getBody(
        ) throws ServiceException {
            return this.body;
        }

        /**
         * Tells whether XML or WBXML shall be used
         */
        protected boolean isWBXML() {
            return this.wbxml;
        }

        /**
         * Tells whether the source shall be exhausted
         * 
         * @return <code>true</code> if the source shall be exhausted
         */
        protected boolean isToBeExhausted() {
            return this.exhaust;
        }
    }
    

    // ------------------------------------------------------------------------
    // Class Target
    // ------------------------------------------------------------------------

    /**
     * Abstract Target
     */
    public static abstract class Target {

        /**
         * Constructor
         * 
         * @param uri
         */
        protected Target(String uri) {
            this.base = uri;
        }

        /**
         * Map a cause to an XMLStreamException
         * 
         * @param exception
         *            the exception to be mapped
         * 
         * @return an XMLStreamException
         */
        protected static XMLStreamException toXMLStreamException(
            Exception exception) {
            BasicException cause = BasicException.toExceptionStack(exception);
            return new XMLStreamException(cause.getMessage(), cause);
        }

        /**
         * The HREF prefix
         */
        private final String base;

        /**
         * The XML output stream
         */
        private XMLStreamWriter writer;

        /**
         * Retrieve the base URI
         * 
         * @return the HREF prefix
         */
        protected String getBase() {
            return this.base;
        }

        /**
         * Terminates and flushes the document
         */
        public void close()
        throws XMLStreamException {
            if (this.writer != null) {
                this.writer.writeEndDocument();
                this.writer.flush();
                this.writer = null;
            }
        }

        /**
         * Discard the writer
         */
        protected void reset(){
            this.writer = null;
        }
        
        /**
         * Create an XML stream writer
         * 
         * @return a new XML stream writer
         * 
         * @throws XMLStreamException
         */
        protected abstract XMLStreamWriter newWriter()
        throws XMLStreamException;

        /**
         * Retrieve an XML Stream Write
         * 
         * @return an XML Stream Writer for an open document
         * 
         * @throws XMLStreamException
         */
        protected XMLStreamWriter getWriter()
        throws XMLStreamException {
            if (this.writer == null) {
                this.writer = newWriter();
                this.writer.writeStartDocument();
            }
            return this.writer;
        }

        /**
         * Retrieve the href URL for a given resource identifier
         * 
         * @param xri
         *            the resource identifier
         * 
         * @return the href URL for the given XRI
         */
        protected String toURL(Path xri) {
            if (xri == null) {
                return this.base + '!' + UUIDs.newUUID();
            } else {
                String uri = xri.toXRI();
                return this.base
                + uri.substring(uri.charAt(14) == '!' ? 14 : 15);
            }
        }

    }


    // ------------------------------------------------------------------------
    // Class StandardHandler
    // ------------------------------------------------------------------------

    /**
     * Content handler which maps an XML encoded values to JCA records
     */
    static class StandardHandler extends AbstractHandler {

        /**
         * Constructor to parse an object or a record
         * 
         * @param source
         */
        StandardHandler(Source source) {
            this.target = null;
            this.source = source;
        }

        /**
         * Constructor to parse a collection
         * 
         * @param source
         * @param target
         */
        StandardHandler(Record target, Source source) {
            this.target = target;
            this.source = source;
        }

        /**
         * The model accessor
         */
        private final Model_1_0 model = Model_1Factory.getModel();

        /**
         * The record factory
         */
        private static final ExtendedRecordFactory recordFactory = Records.getRecordFactory();

        private final Source source;

        private final Record target;

        private MappedRecord values = null;

        private String featureName = null;

        private String previousEndElement = null;

        private String href = null;

        private String version = null;

        private String index = null;

        private Record value = null;

        private String multiplicity = null;

        private String featureType = null;

        /**
         * Retrieve the interaction's value record
         * 
         * @return the interaction's value record
         * 
         * @throws ServiceException
         */
        MappedRecord getValue(Path xri)
        throws ServiceException {
            String typeName = this.values.getRecordName();
            if (this.isQueryType(typeName)) {
                return this.getQuery(xri);
            } else if (this.values == null || this.isStructureType(typeName)) {
                return this.values;
            } else {
                return this.getObject(xri);
            }
        }

        /**
         * Retrieve the interaction's object record
         * 
         * @return the interaction's object record
         */
        MappedRecord getObject(Path xri)
        throws ServiceException {
            try {
                Object_2Facade object = Object_2Facade.newInstance();
                object.setValue(this.values);
                object.setPath(
                    xri == null ? this.source.getXRI(this.href) : xri
                );
                if (this.version != null) {
                    object.setVersion(Base64.decode(this.version));
                }
                return object.getDelegate();
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
        }

        /**
         * Retrieve the interaction's query record
         * 
         * @return the interaction's query record
         */
        MappedRecord getQuery(Path xri)
        throws ServiceException {
            QueryRecord query = (QueryRecord) this.values;
            if(xri != null) {
                query.setPath(xri);
            }
            return query;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void endElement(String uri, String localName, String name)
        throws SAXException {
            try {
                if ("org.openmdx.kernel.ResultSet".equals(name)) {
                    //
                    // Nothing to do
                    //
                } else if (name.indexOf('.') > 0) {
                    //
                    // Object
                    //
                    if (target instanceof IndexedRecord) {
                        ((IndexedRecord)this.target).add(this.getObject(null));
                        this.values = null;
                    } else if (this.target instanceof MessageRecord) {
                        ((MessageRecord)this.target).setPath(this.source.getXRI(this.href));
                        ((MessageRecord)this.target).setBody(this.values);
                        this.values = null;
                    }
                } else if (
                    ITEM_TAG.equals(name) || 
                    (name.equals(this.featureName) && !ITEM_TAG.equals(this.previousEndElement))
                ) {
                    if (hasData()) {
                        java.lang.Object data = getData();
                        if(data instanceof String) {
                            //
                            // Map value
                            //
                            String text = (String) data;
                            data =
                                PrimitiveTypes.STRING.equals(featureType) ? text : 
                                PrimitiveTypes.SHORT.equals(featureType) ? Datatypes.create(Short.class, text.trim()) :
                                PrimitiveTypes.LONG.equals(featureType) ? Datatypes.create(Long.class, text.trim()) : 
                                PrimitiveTypes.INTEGER.equals(featureType) ? Datatypes.create(Integer.class, text.trim()) : 
                                PrimitiveTypes.DECIMAL.equals(featureType) ? Datatypes.create(BigDecimal.class,text.trim()) : 
                                PrimitiveTypes.BOOLEAN.equals(featureType) ? Datatypes.create(Boolean.class, text.trim()) : 
                                PrimitiveTypes.OBJECT_ID.equals(featureType) ? text.trim() : 
                                PrimitiveTypes.DATETIME.equals(featureType) ? Datatypes.create(Date.class,text.trim()) : 
                                PrimitiveTypes.DATE.equals(featureType) ? Datatypes.create(XMLGregorianCalendar.class, text.trim()) : 
                                PrimitiveTypes.BINARY.equals(featureType) ? Base64.decode(text.trim()) : 
                                featureType != null && model.isClassType(featureType) ? new Path(text.trim()) : text;
                        }
                        if (
                            Multiplicities.SINGLE_VALUE.equals(this.multiplicity) || 
                            Multiplicities.OPTIONAL_VALUE.equals(this.multiplicity)
                        ) {
                            this.values.put(this.featureName, data);
                        } else if (data == null) {
                            SysLog.warning(
                                "Null feature for the given multiplicity ignored",
                                multiplicity
                            );
                        } else if (
                            Multiplicities.LIST.equals(multiplicity) || 
                            Multiplicities.SET.equals(multiplicity)
                        ) {
                            ((IndexedRecord) this.value).add(data);
                        } else if (Multiplicities.SPARSEARRAY.equals(multiplicity)) {
                            ((MappedRecord) this.value).put(
                                Integer.valueOf(this.index), 
                                data
                            );
                        } else if (Multiplicities.STREAM.equals(multiplicity)) {
                            this.values.put(
                                this.featureName,
                                PrimitiveTypes.BINARY.equals(featureType) ? BinaryLargeObjects.valueOf((byte[]) data) : 
                                PrimitiveTypes.STRING.equals(featureType) ? CharacterLargeObjects.valueOf((String) data) : 
                                data
                            );
                        } else {
                            SysLog.warning(
                                "Unsupported multiplicity, feature ignored",
                                multiplicity
                            );
                        }
                    }
                }
                this.previousEndElement = name;
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes)
        throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            try {
                if ("org.openmdx.kernel.ResultSet".equals(qName)) {
                    if (this.target instanceof ResultRecord) {
                        ResultRecord target = (ResultRecord) this.target;
                        String more = attributes.getValue("hasMore");
                        if (more != null) {
                            target.setHasMore(Boolean.parseBoolean(more));
                        }
                        String total = attributes.getValue("total");
                        if (total != null) {
                            target.setTotal(Long.parseLong(total));
                        }
                    }
                } else if (qName.indexOf('.') > 0) {
                    try {
                        // Begin object or struct
                        this.values = recordFactory.createMappedRecord(
                            qName.replace('.', ':')
                        );
                        this.href = attributes.getValue("href");
                        this.version = attributes.getValue("version");
                    } catch (ResourceException exception) {
                        throw new SAXException(exception);
                    }
                } else if (ITEM_TAG.equals(qName)) {
                    this.index = attributes.getValue("index");
                } else {
                    this.index = null;
                    this.featureName = localName;
                    ModelElement_1_0 featureDef = getFeatureDef(
                        this.values.getRecordName(),
                        this.featureName
                    );
                    this.featureType = getFeatureType(featureDef);
                    this.multiplicity = getMultiplicity(featureDef);
                    if (
                        Multiplicities.LIST.equals(this.multiplicity) || 
                        Multiplicities.SET.equals(this.multiplicity)
                    ) {
                        this.values.put(
                            this.featureName, 
                            this.value = recordFactory.createIndexedRecord(this.multiplicity)
                        );
                    } else if (Multiplicities.SPARSEARRAY.equals(this.multiplicity)) {
                        this.values.put(
                            this.featureName, 
                            this.value = recordFactory.createMappedRecord(this.multiplicity)
                        );
                    } else if (Multiplicities.OPTIONAL_VALUE.equals(this.multiplicity)) {
                        this.values.put(this.featureName, this.value = null);
                    } else {
                        this.value = null;
                    }
                }
            } catch (ServiceException exception) {
                throw new SAXException(exception);
            } catch (ResourceException exception) {
                throw new SAXException(exception);
            }
        }

        /**
         * Retrieve the feature definition
         * 
         * @param typeName
         * @param featureName
         * 
         * @return the feature definition
         * 
         * @throws ServiceException
         */
        protected ModelElement_1_0 getFeatureDef(
            String typeName,
            String featureName)
        throws ServiceException {
            return ControlObjects_2.isControlObjectType(typeName) ? null : model.getFeatureDef(
                model.getElement(typeName),
                featureName,
                false // includeSubtypes
            );
        }

        /**
         * Retrieve the feature type
         * 
         * @param featureDef
         *            feature definition
         * 
         * @return the feature type
         * 
         * @throws ServiceException
         */
        protected String getFeatureType(ModelElement_1_0 featureDef)
        throws ServiceException {
            return featureDef == null ? 
                PrimitiveTypes.STRING : 
                (String) model.getElementType(featureDef).objGetValue("qualifiedName");
        }

        /**
         * Retrieve the feature's multiplicity
         * 
         * @param featureDef
         *            feature definition
         * 
         * @return the feature's multiplicity
         * 
         * @throws ServiceException
         */
        protected String getMultiplicity(
            ModelElement_1_0 featureDef
        ) throws ServiceException {
            return 
                featureDef == null ? Multiplicities.OPTIONAL_VALUE :
                ModelUtils.getMultiplicity(featureDef);
        }

        /**
         * Tell whether a given record is a structure type
         * 
         * @param typeName
         * 
         * @return <code>true</code> in case of structure, <code>false</code> in
         *         case of object
         * 
         * @throws ServiceException
         */
        protected boolean isStructureType(
            String typeName
        ) throws ServiceException {
            return 
                !ControlObjects_2.isControlObjectType(typeName) && 
                model.isStructureType(typeName);
        }

        /**
         * Tell whether a given record is query type
         * 
         * @param typeName
         * 
         * @return <code>true</code> in case of query, <code>false</code>
         *         otherwise
         * 
         * @throws ServiceException
         */
        protected boolean isQueryType(String typeName)
        throws ServiceException {
            return QueryRecord.NAME.equals(typeName);
        }

    }

    
    // ------------------------------------------------------------------------
    // Class ExceptionHandler
    // ------------------------------------------------------------------------

    /**
     * Content handler which maps an XML encoded values to BasicExceptions
     */
    static class ExceptionHandler extends AbstractHandler {

        /**
         * Constructor to parse an object or a record
         * 
         * @param source
         */
        ExceptionHandler() {
        }

        private String exceptionDomain;

        private String exceptionCode;

        private String exceptionTime;

        private String exceptionClass;

        private String methodName;

        private String lineNumber;

        private String description;

        private String parameter_id;

        private String stackTraceElement_declaringClass;
        
        private String stackTraceElement_fileName;
        
        private String stackTraceElement_methodName;
        
        private String stackTraceElement_lineNumber;
        
        private String stackTraceElements_more;

        private List<StackTraceElement> stackTraceElements = new ArrayList<StackTraceElement>();
        
        private StackTraceElement[] stackTrace;
        
        private final Map<String, String> parameters = new HashMap<String, String>();

        private BasicException stack;

        /**
         * Retrieve the exception stack
         * 
         * @return the exception stack
         * 
         * @throws ServiceException
         */
        BasicException getValue()
        throws ServiceException {
            return this.stack;
        }

        @Override
        public void endElement(String uri, String localName, String name)
        throws SAXException {
            try {
                if (ITEM_TAG.equals(name)) {
                    if(this.parameter_id != null) {
                        this.parameters.put(
                            this.parameter_id, 
                            super.hasData() ? (String)getData() : null
                        );
                    } else if (this.stackTraceElement_declaringClass != null){
                        this.stackTraceElements.add(
                            new StackTraceElement(
                                this.stackTraceElement_declaringClass, 
                                this.stackTraceElement_methodName, 
                                this.stackTraceElement_fileName, 
                                Integer.parseInt(this.stackTraceElement_lineNumber)
                            )
                        );
                    }
                } else if ("description".equals(name)) {
                    this.description = (String) getData();
                } else if ("element".equals(name)) {
                    BasicException.Parameter[] parameters =
                        new BasicException.Parameter[this.parameters.size()];
                    int i = 0;
                    for (Map.Entry<String, String> parameter : this.parameters.entrySet()) {
                        parameters[i++] = new BasicException.Parameter(
                            parameter.getKey(),
                            parameter.getValue()
                        );
                    }
                    BasicException element = new BasicException(
                        this.exceptionDomain,
                        Integer.valueOf(this.exceptionCode),
                        this.exceptionClass,
                        this.exceptionTime == null ? null : DateTimeFormat.EXTENDED_UTC_FORMAT.parse(exceptionTime),
                        this.methodName,
                        this.lineNumber == null ? null : Integer.valueOf(this.lineNumber),
                        this.description,
                        parameters
                    );
                    element.setStackTrace(this.stackTrace);
                    if (this.stack == null) {
                        this.stack = element;
                    } else {
                        this.stack.getCause(null).initCause(element);
                    }
                } else if ("stackTraceElements".equals(name)) {
                    int stackTraceElementCount = this.stackTraceElements.size();
                    if(this.stackTraceElements_more == null || "0".equals(this.stackTraceElements_more)) {
                        this.stackTrace = this.stackTraceElements.toArray(new StackTraceElement[stackTraceElementCount]);
                    } else {
                        int more = Integer.parseInt(stackTraceElements_more);
                        StackTraceElement[] stackTrace = this.stackTraceElements.toArray(new StackTraceElement[stackTraceElementCount + more]);
                        System.arraycopy(this.stackTrace, this.stackTrace.length - more, stackTrace, stackTraceElementCount, more);
                        this.stackTrace = stackTrace;
                    }
                }
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        @Override
        public void startElement(
            String uri,
            String localName,
            String name,
            Attributes attributes)
        throws SAXException {
            super.startElement(uri, localName, name, attributes);
            if ("element".equals(name)) {
                this.exceptionDomain = attributes.getValue("exceptionDomain");
                this.exceptionCode = attributes.getValue("exceptionCode");
                this.exceptionTime = attributes.getValue("exceptionTime");
                this.exceptionClass = attributes.getValue("exceptionClass");
                this.methodName = attributes.getValue("methodName");
                this.lineNumber = attributes.getValue("lineNumber");
            } else if ("parameter".equals(name)) {
                this.parameters.clear();
            } else if ("stackTraceElements".equals(name)) {
                this.stackTraceElements.clear();
                this.stackTraceElements_more = attributes.getValue("more");
            } else if (ITEM_TAG.equals(name)) {
                this.parameter_id = attributes.getValue("id");
                this.stackTraceElement_declaringClass = attributes.getValue("declaringClass");
                if(this.stackTraceElement_declaringClass == null) {
                    this.stackTraceElement_methodName = null;
                    this.stackTraceElement_fileName = null;
                    this.stackTraceElement_lineNumber = null;
                } else {
                    this.stackTraceElement_methodName = attributes.getValue("methodName");
                    this.stackTraceElement_fileName = attributes.getValue("fileName");
                    this.stackTraceElement_lineNumber = attributes.getValue("lineNumber");
                }
            }
        }

    }

    
    // ------------------------------------------------------------------------
    // Class AbstractHandler
    // ------------------------------------------------------------------------

    /**
     * Error handler and content handler for character data
     */
    static class AbstractHandler extends DefaultHandler implements LargeObjectWriter {

        /**
         * Constructor
         */
        AbstractHandler() {
        }

        /**
         * The character data
         */
        private final StringBuilder characterData = new StringBuilder();

        /**
         * Tells whether some character data has been read.
         */
        private boolean hasCharacterData = false;

        /**
         * binary data
         */
        private byte[] binaryData = null;

        /**
         * Tells whether there is either character or binary content available
         * 
         * @return <code>true</code> if some content is available
         */
        protected boolean hasData(
        ){
            return this.hasCharacterData || binaryData != null;
        }
        
        /**
         * Retrieve the elements character data
         * 
         * @return the elements character data
         */
        protected Object getData() {
            return this.hasCharacterData ? characterData.toString() : this.binaryData;
        }

        @Override
        public void error(
            SAXParseException exception
        ) throws SAXException {
            StringBuilder locationString = new StringBuilder();
            String systemId = exception.getSystemId();
            if (systemId != null) {
                int index = systemId.lastIndexOf('/');
                if (index != -1) {
                    systemId = systemId.substring(index + 1);
                }
                locationString.append(systemId);
            }
            locationString.append(
                ':'
            ).append(
                exception.getLineNumber()
            ).append(
                ':'
            ).append(
                exception.getColumnNumber()
            );
            throw Throwables.log(
                BasicException.initHolder(
                    new SAXException(
                        "XML parse error",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.PROCESSING_FAILURE,
                            new BasicException.Parameter("message", exception.getMessage()),
                            new BasicException.Parameter("location", locationString),
                            new BasicException.Parameter("systemId", systemId),
                            new BasicException.Parameter("lineNumber", exception.getLineNumber()),
                            new BasicException.Parameter("columnNumber", exception.getColumnNumber())
                        )
                    )
                )
            );
        }

        @Override
        public void characters(
            char[] ch, 
            int start, 
            int length
        ) throws SAXException {
            this.hasCharacterData = true;
            this.characterData.append(ch, start, length);
        }

        @Override
        public void startElement(
            String uri,
            String localName,
            String name,
            Attributes attributes)
        throws SAXException {
            this.hasCharacterData = false;
            this.characterData.setLength(0);
            this.binaryData = null;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeBinaryData(byte[], int, int)
         */
        @Override
        public void writeBinaryData(
            byte[] data, 
            int offset, 
            int length
        ) throws XMLStreamException {
            this.binaryData = offset == 0 && length == data.length ? data : Arrays.copyOfRange(data, offset, offset + length);           
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeBinaryData(org.w3c.cci2.BinaryLargeObject)
         */
        @Override
        public void writeBinaryData(
            BinaryLargeObject data
        ) throws XMLStreamException {
            throw new UnsupportedOperationException("Large object streaming not yet implemented");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeCharacterData(org.w3c.cci2.CharacterLargeObject)
         */
        @Override
        public void writeCharacterData(
            CharacterLargeObject data
        ) throws XMLStreamException {
            throw new UnsupportedOperationException("Large object streaming not yet implemented");
        }

    }

}

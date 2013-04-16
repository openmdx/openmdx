/*
 * ====================================================================
 * Project:     openMDX/Dalvik, http://www.openmdx.org/
 * Description: REST Format
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2013, OMEX AG, Switzerland
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
package org.openmdx.dalvik.rest.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.rest.spi.ControlObjects_2;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.ObjectOutputStream;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLOutputFactory;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamException;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamWriter;
import org.openmdx.dalvik.xml.stream.CharacterWriter;
import org.openmdx.dalvik.xml.stream.LargeObjectWriter;
import org.openmdx.dalvik.xml.stream.XMLOutputFactories;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
import org.w3c.format.DateTimeFormat;

/**
 * REST Format
 */
public class RestFormatter {

    /**
     * Constructor
     */
    private RestFormatter() {
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
     * An <code>XMLOutputFactory</code> cache
     */
    private static final ConcurrentMap<String,XMLOutputFactory> xmlOutputFactories = new ConcurrentHashMap<String, XMLOutputFactory>();

    /**
     * Convert the path info into a resource identifier
     * 
     * @param pathInfo
     * 
     * @return the XRI corresponding to the path info
     */
    public static Path toResourceIdentifier(
        String pathInfo
    ){  
        return new Path(
            (pathInfo.startsWith("/@openmdx") ? "" : pathInfo.startsWith("/!") ? "xri://@openmdx" : "xri://@openmdx*") + 
            pathInfo.substring(1)
        );
    }
    
    /**
     * Retrieve and cache MIME type dependent XML output factories
     * 
     * @param mimeType
     * 
     * @return a MIME type specific XML output factory
     * 
     * @throws XMLStreamException if no factory can be provided for the given MIME type
     */
    public static XMLOutputFactory getOutputFactory(
        String mimeType
    ) throws XMLStreamException {
        XMLOutputFactory xmlOutputFactory = RestFormatter.xmlOutputFactories.get(mimeType);
        return xmlOutputFactory == null ? Maps.putUnlessPresent(
            RestFormatter.xmlOutputFactories,
            mimeType,
            XMLOutputFactories.newInstance(mimeType)
        ) : xmlOutputFactory;
    }
    
    /**
     * Tells whether the given MIME type requires a binary stream
     * 
     * @param mimeType
     * 
     * @return <code>true</code> if the given MIME type requires a binary stream
     */
    public static boolean isBinary(
        String mimeType
    ){
        return "application/vnd.openmdx.wbxml".equals(mimeType);
    }
    
    /**
     * Provide a <code>format()</code> target
     * 
     * @param source the <code>ObjectOutput</code>
     * 
     * @return a <code>Target</code>
     */
    public static RestTarget asTarget(
        final ObjectOutput output
    ){
        return new RestTarget("."){

            @Override
            protected XMLStreamWriter newWriter(
            ) throws XMLStreamException {
                return RestFormatter.getOutputFactory(
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
        RestTarget target, 
        Object_2Facade source
    ) throws ServiceException {
        Path resourceIdentifier = source.getPath();
        RestFormatter.printRecord(
            target,
            0,
            resourceIdentifier,
            null,
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
        RestTarget target, 
        Query_2Facade source
    ) throws ServiceException {
        RestFormatter.printRecord(
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
        RestTarget target, 
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
                Object_2Facade object = Facades.asObject((MappedRecord) record);
                RestFormatter.printRecord(
                    target, 
                    1, 
                    object.getPath(), 
                    null, 
                    (byte[]) object.getVersion(), 
                    object.getValue()
               );
            }
            writer.writeEndElement(); // "org.openmdx.kernel.ResultSet"
            writer.writeEndDocument();
        } catch (XMLStreamException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * Format Operation
     * 
     * @param target
     * @param id the (optional) id
     * @param source
     * @throws ServiceException
     */
    public static void format(
        RestTarget target, 
        String id, 
        MessageRecord source
    ) throws ServiceException {
        RestFormatter.printRecord(
            target, 
            0, 
            source.getPath(), 
            id,
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
        RestTarget target,
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
        RestTarget target, 
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
     * Tells whether we are processing a struct value or not
     * 
     * @param value
     * 
     * @return <code>true</code> if we are processing a struct value
     */
    private static boolean isStructureType(
        Map<?,?> value
    ){
        if(value instanceof MappedRecord) {
            String recordName = ((MappedRecord)value).getRecordName();
            return 
            	recordName != null && 
                !ControlObjects_2.isControlObjectType(recordName) && 
                recordName.indexOf(':') > 0;
        }
        return false;
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
        RestTarget target,
        int indent,
        Path xri,
        String id,
        byte[] version,
        MappedRecord record
    ) throws ServiceException {
        try {
            XMLStreamWriter writer = target.getWriter();
            String tag = record.getRecordName().replace(':', '.');
            writer.writeStartElement(tag);
            if(id != null) {
                target.getWriter().writeAttribute("id", id);
            }
            if(xri != null) {
                target.getWriter().writeAttribute("href", target.toURL(xri));
            }
            if (version != null) {
                target.getWriter().writeAttribute("version", Base64.encode(version));
            }
            Set<Map.Entry<String, ?>> entries = record.entrySet();
            for (Map.Entry<String, ?> entry : entries) {
                String feature = entry.getKey();
                Object value = entry.getValue();
                try {
                    printValue(target, indent, xri, feature, value);
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
            target.getWriter().writeEndElement(); // tag
        } catch (XMLStreamException exception) {
            throw new ServiceException(exception);
        }
    }

    /**
     * @param target
     * @param indent
     * @param xri
     * @param feature
     * @param value
     * @throws XMLStreamException
     *  
     * @throws ServiceException 
     */
    private static void printValue(
        RestTarget target,
        int indent,
        Path xri,
        String feature,
        Object value
    ) throws XMLStreamException, ServiceException {
        XMLStreamWriter writer = target.getWriter();
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
                            v
                        );
                    } catch (Exception exception) {
                        if (SysLog.isTraceOn()) { 
                        	SysLog.trace(
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
                                        xri == null ? null : xri.toXRI()
                                    ),
                                    new BasicException.Parameter(
                                        "feature",
                                        feature
                                    )
                                )
                            );
                        }
                    }
                }
                writer.writeEndElement(); // feature
            }
        } else if (value instanceof Map) {
            Map<?, ?> values = (Map<?, ?>) value;
            if(isStructureType(values)){
                //
                // Structure
                //
                writer.writeStartElement(feature);
                printRecord(
                    target,
                    indent + 2,
                    null, // xri
                    null, // id
                    null, // version
                    (MappedRecord)value // record
                );
                writer.writeEndElement(); // feature
            } else {
                //
                // "sparsearray"
                //
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
                                        xri == null ? null : xri.toXRI()
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
    }
}

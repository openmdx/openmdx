/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard REST Formatter
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
package org.openmdx.dalvik.rest.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;
import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;
import #if JAVA_8 javax.resource.cci.MappedRecord #else jakarta.resource.cci.MappedRecord #endif;
import #if JAVA_8 javax.resource.spi.ResourceAllocationException #else jakarta.resource.spi.ResourceAllocationException #endif;

import org.ietf.jgss.Oid;
import org.openmdx.base.accessor.rest.spi.ControlObjects_2;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.ObjectOutputStream;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.RestFormatter;
import org.openmdx.base.rest.spi.Target;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.xml.spi.LargeObjectWriter;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLOutputFactory;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamException;
import org.openmdx.dalvik.uses.javax.xml.stream.XMLStreamWriter;
import org.openmdx.dalvik.xml.stream.CharacterWriter;
import org.openmdx.dalvik.xml.stream.XMLOutputFactories;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.CharacterLargeObject;
import org.w3c.cci2.CharacterLargeObjects;
#if CLASSIC_CHRONO_TYPES
import org.w3c.format.DateTimeFormat;
import org.w3c.spi.ImmutableDatatypeFactory;
#endif
import org.w3c.spi2.Datatypes;

/**
 * Standard REST Formatter
 */
public class AlternateRestFormatter implements RestFormatter {

    /**
     * 
     */
    private static final String ITEM_TAG = "_item";

    /**
     * Tells whether the stack trace shall be included
     */
    private static final boolean INCLUDE_STACK_TRACE = true;
    
    /**
     * An {@code XMLOutputFactory} cache
     */
    private final ConcurrentMap<String,XMLOutputFactory> xmlOutputFactories = new ConcurrentHashMap<String, XMLOutputFactory>();

    /**
     * Retrieve and cache MIME type dependent XML output factories
     * 
     * @param mimeType
     * 
     * @return a MIME type specific XML output factory
     * @throws ServiceException  
     * 
     * @throws XMLStreamException if no factory can be provided for the given MIME type
     */
    public XMLOutputFactory getOutputFactory(
        String mimeType
    ) throws BasicException {
        XMLOutputFactory xmlOutputFactory = this.xmlOutputFactories.get(mimeType);
        return xmlOutputFactory == null ? Maps.putUnlessPresent(
            this.xmlOutputFactories,
            mimeType,
            XMLOutputFactories.newInstance(mimeType)
        ) : xmlOutputFactory;
    }
    
    /**
     * Provide a {@code format()} target
     * 
     * @param source the {@code ObjectOutput}
     * 
     * @return a {@code Target}
     */
    @Override
    public Target asTarget(
        final ObjectOutput output
    ){
        return new RestTarget("."){

            @Override
            protected XMLStreamWriter newWriter(
            ) throws XMLStreamException {
                try {
                    return getOutputFactory(
                        "application/vnd.openmdx.wbxml"
                    ).createXMLStreamWriter(
                        output instanceof OutputStream ? (OutputStream) output : new ObjectOutputStream(output)
                    );
                } catch (BasicException exception) {
                    throw new XMLStreamException(exception);
                }
            }
            
        };
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestFormatter#format(org.openmdx.base.rest.spi.Target, org.openmdx.base.rest.cci.ObjectRecord)
     */
    @Override
    public void format(
        Target target, 
        ObjectRecord source
    ) throws ResourceException {
    	this.format(target, source, true);
    }

   /**
     * Format Object
     * 
     * @param target
     * @param source
     * @param serializeNulls
     * 
     * @throws ServiceException
     */
    @Override
    public void format(
        Target target, 
        ObjectRecord source,
        boolean serializeNulls
    ) throws ResourceException {
        Path resourceIdentifier = source.getResourceIdentifier();
        final UUID transientObjectId = source.getTransientObjectId();
        formatRecord(
            (RestTarget)target,
            0,
            resourceIdentifier,
            transientObjectId == null ? null : transientObjectId.toString(),
            source.getVersion(),
            null, // index
            source.getValue(),
            serializeNulls
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
    @Override
    public void format(
        Target target, 
        QueryRecord source
    ) throws ResourceException {
    	formatRecord(
            (RestTarget)target, 
            0, 
            null, // XRI
            "query",
            null, // Version
            null, // index
            source,
            true // serializeNulls
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestFormatter#format(org.openmdx.base.rest.spi.Target, org.openmdx.base.naming.Path, javax.resource.cci.IndexedRecord)
     */
    @Override
    public void format(
        Target target, 
        Path xri, 
        IndexedRecord source
    ) throws ResourceException {
    	this.format(
    		target,
    		xri,
    		source,
    		true // serializeNulls
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
    @Override
    public void format(
        Target target, 
        Path xri, 
        IndexedRecord source,
        boolean serializeNulls
    ) throws ResourceException {
        try {
            RestTarget restTarget = (RestTarget)target;
            XMLStreamWriter writer = restTarget.getWriter();
            writer.writeStartElement("org.openmdx.kernel.ResultSet");
            writer.writeAttribute("href", restTarget.toURL(xri));
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
                ObjectRecord object = (ObjectRecord) record;
                printRecord(
                    restTarget, 
                    1, 
                    object.getResourceIdentifier(), 
                    null, 
                    object.getVersion(), 
                    null, // index
                    object.getValue(),
                    serializeNulls
               );
            }
            writer.writeEndElement(); // "org.openmdx.kernel.ResultSet"
            writer.writeEndDocument();
        } catch (XMLStreamException exception) {
        	throw ResourceExceptions.initHolder(
        		new ResourceException(
        			"Unable to write the REST request to the given stream",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE
                    )
                )
        	);
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
    @Override
    public void format(
        Target target, 
        String id, 
        MessageRecord source
    ) throws ResourceException {
    	formatRecord(
            (RestTarget)target, 
            0, 
            source.getResourceIdentifier(), 
            id,
            null, // Version
            null, // index
            source.getBody(),
            true // serializeNulls
        );
    }

    /**
     * Print Value
     * 
     * @param target
     * @param indent
     * @param index
     * @param value
     * @param anyType tells whether the value's type can't be derived from the model
     * @throws IOException 
     * @throws XMLStreamException 
     * 
     * @throws ServiceException
     */
    private static void printItem(
        RestTarget target,
        int indent,
        Object index,
        Object value, 
        boolean anyType,
        boolean serializeNulls
    ) throws XMLStreamException {
        if (value instanceof MappedRecord && isStructureType((MappedRecord)value)){
            printRecord(
                target,
                indent,
                null, // xri
                null, // id
                null, // version
                index,
                (MappedRecord)value, // record
                serializeNulls
            );
        } else {
            printValue(
                target,
                indent,
                ITEM_TAG,
                index,
                value, 
                anyType,
                serializeNulls
            );
        }
    }

    /**
     * Print Value
     * 
     * @param target
     * @param indent
     * @param tag
     * @param index
     * @param value
     * @param anyType tells whether the value's type can't be derived from the model
     * @throws XMLStreamException 
     * 
     * @throws ServiceException
     * @throws IOException 
     */
    private static void printValue(
        RestTarget target,
        int indent,
        String tag,
        Object index,
        Object value, 
        boolean anyType,
        boolean serializeNulls
    ) throws XMLStreamException {
        XMLStreamWriter writer = target.getWriter();
        try {
            if(value == null && index == null)  {
            	if(serializeNulls) {
            		writer.writeEmptyElement(tag);
            	}
            } else {
                writer.writeStartElement(tag);
                if (index != null) {
                    writer.writeAttribute("index", index.toString());
                }
                if (anyType) {
                    writer.writeAttribute("type", getPrimitiveType(value));
                }
                if (value == null) {
                    // wait for the tag to be closed
                } else {
                	if (value instanceof MappedRecord && isStructureType((MappedRecord)value)){
	                    printRecord(
	                        target,
	                        indent + 2,
	                        null, // xri
	                        null, // id
	                        null, // version
	                        index,
	                        (MappedRecord)value, // record
	                        serializeNulls
	                    );
	                } else if (value instanceof Path) {
	                    Path xri = (Path) value;
	                    writer.writeAttribute("href", target.toURL(xri));
	                    writer.writeCharacters(xri.toXRI());
	                } else if (value instanceof String) {
	                    writer.writeCData((String) value);
	                } else if (Datatypes.DATE_TIME_CLASS.isInstance(value)) {
                        writer.writeCharacters(
                            #if CLASSIC_CHRONO_TYPES DateTimeFormat.EXTENDED_UTC_FORMAT.format(Datatypes.DATE_TIME_CLASS.cast(value))
                            #else DateTimeConstants.DT_WITH_UTC_TZ_EXT_PATTERN.format(Datatypes.DATE_TIME_CLASS.cast(value))
                            #endif
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
	            }
                writer.writeEndElement(); // tag
            }
        } catch (ServiceException exception) {
        	throw new XMLStreamException(exception);
        } catch (IOException exception) {
        	throw new XMLStreamException(exception);
        }
    }

    private static String getPrimitiveType(Object value) {
    	return
    		value instanceof String ? PrimitiveTypes.STRING :
    		value instanceof Short ? PrimitiveTypes.SHORT :
    		value instanceof Long ? PrimitiveTypes.LONG :
    		value instanceof Integer ? PrimitiveTypes.INTEGER :
    		value instanceof BigDecimal ? PrimitiveTypes.DECIMAL :
    		value instanceof Boolean ? PrimitiveTypes.BOOLEAN :
    		value instanceof Path ? PrimitiveTypes.OBJECT_ID :
            Datatypes.DATE_TIME_CLASS.isInstance(value) ? PrimitiveTypes.DATETIME :
            Datatypes.DATE_CLASS.isInstance(value) ? PrimitiveTypes.DATE :
    		value instanceof URI ? PrimitiveTypes.ANYURI :
    		value instanceof byte[] ? PrimitiveTypes.BINARY :
    		value instanceof UUID ? PrimitiveTypes.UUID :
    		value instanceof Oid ? PrimitiveTypes.OID :
    		"org:w3c:anyType";
    }
    
    /**
     * Print Exception
     * 
     * @param target
     * @param exception
     * @throws ServiceException
     */
    @Override
    public void format(
        Target target, 
        BasicException source
    ) {
        try {
            XMLStreamWriter writer = ((RestTarget)target).getWriter();
            writer.writeStartElement("org.openmdx.kernel.Exception");
            StackTraceElement[] lastTrace = null;
            for (BasicException entry : source.getExceptionStack()) {
                writer.writeStartElement("element");
                writer.writeAttribute("exceptionDomain", entry.getExceptionDomain());
                writer.writeAttribute("exceptionCode", String.valueOf(entry.getExceptionCode()));
                #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif exceptionTime = entry.getTimestamp();
                if (exceptionTime != null) {
                    writer.writeAttribute(
                        "exceptionTime",
                        #if CLASSIC_CHRONO_TYPES DateTimeFormat.EXTENDED_UTC_FORMAT.format(exceptionTime)
                        #else DateTimeConstants.DT_WITH_UTC_TZ_EXT_PATTERN.format(exceptionTime)
                        #endif
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
     * @return {@code true} if we are processing a struct value
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
     * Format Record
     */
    private static void formatRecord(
        RestTarget target,
        int indent,
        Path xri,
        String id,
        byte[] version,
        Object index, 
        MappedRecord record,
        boolean serializeNulls
    ) throws ResourceException {
    	try {
			printRecord(
				target,
				indent,
				xri,
				id,
				version,
				index,
				record,
				serializeNulls
			);
		} catch (XMLStreamException exception) {
            throw ResourceExceptions.initHolder(
                new ResourceAllocationException(
                    "Unable to format the given record",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.TRANSFORMATION_FAILURE,
	                    new BasicException.Parameter(BasicException.Parameter.XRI, xri),
	                    new BasicException.Parameter("id", id),
	                    new BasicException.Parameter("type", record.getRecordName()),
	                    new BasicException.Parameter("index", index),
	                    new BasicException.Parameter("indentation", Integer.valueOf(indent))
                    )
                )
            );
		}
    }
    
    /**
     * Print Record
     */
    @SuppressWarnings("unchecked")
    private static void printRecord(
        RestTarget target,
        int indent,
        Path xri,
        String id,
        byte[] version,
        Object index, 
        MappedRecord record,
        boolean serializeNulls
    ) throws XMLStreamException{
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
        if (index != null) {
            writer.writeAttribute("index", index.toString());
        }
        Set<Map.Entry<String, ?>> entries = record.entrySet();
        for (Map.Entry<String, ?> entry : entries) {
            String feature = entry.getKey();
            Object value = entry.getValue();
            try {
                printValue(
                	target,
                	indent,
                	xri,
                	feature,
                	value,
                	isAnyType(record.getRecordName(), feature),
                	serializeNulls
                );
            } catch (Exception exception) {
                SysLog.warning(
                    "Collection element print failure",
                    new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "Unable to retrieve feature value",
                        new BasicException.Parameter("hrefContext", target.getBase()),
                        new BasicException.Parameter(BasicException.Parameter.XRI, xri),
                        new BasicException.Parameter("id", id),
                        new BasicException.Parameter("feature", feature)
                    )
                );
            }
        }
        target.getWriter().writeEndElement(); // tag
    }

	/**
	 * Determines whether value's type is modeled as {@code org::w3c::anyType}
	 * 
	 * @param recordName the value holder's model type
	 * @param feature the unqualified feature name
	 * 
	 * @return {@code true} if the value's type is modeled as {@code org::w3c::anyType}
	 */
	private static boolean isAnyType(String recordName, String feature) {
		return "value".equals(feature) && "org:openmdx:kernel:Condition".equals(recordName);
	}

	/**
     * @param target
     * @param indent
     * @param xri
     * @param feature
     * @param value
     * @param anyType tells whether the value's type can't be derived from the model
     * 
     * @throws XMLStreamException
     * @throws ServiceException 
	 * @throws IOException 
     */
    private static void printValue(
        RestTarget target,
        int indent,
        Path xri,
        String feature,
        Object value, 
        boolean anyType,
        boolean serializeNulls
    ) throws XMLStreamException {
        XMLStreamWriter writer = target.getWriter();
        if (value instanceof Collection<?>) {
            //
            // "list" or "set"
            //
            Collection<?> values = (Collection<?>) value;
            if (values.isEmpty()) {
            	if(serializeNulls) {
            		writer.writeEmptyElement(feature);
            	}
            } else {
                writer.writeStartElement(feature);
                int index = 0;
                for (Object v : values){
                    try {
                        printItem(
                            target,
                            indent + 2,
                            Integer.valueOf(index++), 
                            v, 
                            anyType,
                            serializeNulls
                        );
                    } catch (Exception exception) {
                        SysLog.warning(
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
                                new BasicException.Parameter(BasicException.Parameter.XRI, xri),
                                new BasicException.Parameter("feature", feature)
                            )
                        );
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
                    null, // index
                    (MappedRecord)value, // record
                    serializeNulls
                );
                writer.writeEndElement(); // feature
            } else {
                //
                // "sparsearray"
                //
                if (values.isEmpty()) {
                	if(serializeNulls) {
                		writer.writeEmptyElement(feature);
                	}
                } else {
                    writer.writeStartElement(feature);
                    for (Map.Entry<?, ?> e : values.entrySet()){
                        try {
                            printItem(
                                target,
                                indent + 2,
                                e.getKey(),
                                e.getValue(), 
                                anyType,
                                serializeNulls
                            );
                        } catch (Exception exception) {
                            SysLog.warning(
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
                                    new BasicException.Parameter(BasicException.Parameter.XRI, xri),
                                    new BasicException.Parameter("feature", feature)
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
                value, 
                anyType,
                serializeNulls
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.RestFormatter#format(org.openmdx.base.rest.spi.Target, java.lang.String, org.openmdx.base.rest.cci.MessageRecord, boolean)
     */
    @Override
    public void format(
        Target target,
        String id,
        MessageRecord source,
        boolean serializeNulls
    )
        throws ResourceException {
        formatRecord(
            (RestTarget)target, 
            0, 
            source.getResourceIdentifier(), 
            id,
            null, // Version
            null, // index
            source.getBody(),
            serializeNulls
        );
    }

}

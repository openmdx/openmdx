/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Parser
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
package org.openmdx.base.rest.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.Record;
#endif

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.base.accessor.rest.spi.ControlObjects_2;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.io.ObjectInputStream;
import org.openmdx.base.json.stream.JSONReader;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.RestSource.Format;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.base.wbxml.WBXMLReader;
import org.openmdx.base.xml.spi.LargeObjectWriter;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
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
 * REST Parser
 */
public class RestParser {

    /**
     * Constructor
     */
    private RestParser() {
        // Avoid instantiation
    }
    

    /**
     * 
     */
    private static final String ITEM_TAG = "_item";
    private static final String OBJECTS_TAG = "objects";
    
    /**
     * The XML Readers
     */
    private static final ThreadLocal<XMLReader> xmlReaders = new ThreadLocal<XMLReader>() {

        @Override
        protected XMLReader initialValue() {
            try {
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                XMLReader reader = parser.getXMLReader();
                try {
                    reader.setFeature("http://xml.org/sax/features/namespaces", true);
                } catch(SAXException e) {
                    SysLog.info("Unable to set SAXReader feature", e.getMessage());
                }
                try {
                    reader.setFeature("http://xml.org/sax/features/validation", false);
                } catch(SAXException e) {
                    SysLog.info("Unable to set SAXReader feature", e.getMessage());
                }
                try {
                    // Prevent XML eXternal Entity injection (XXE)
                    // See https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
                    reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                } catch(SAXException e) {
                    SysLog.info("Unable to set SAXReader feature", e.getMessage());
                }
                return reader;
            } catch (Exception e) {
                throw new RuntimeServiceException(e);
            }
        }
    };

    /**
     * The WBXML Readers
     */
    private static final ThreadLocal<XMLReader> wbxmlReaders = new ThreadLocal<XMLReader>() {

        @Override
        protected XMLReader initialValue() {
            try {
                return new WBXMLReader(new WBXMLPlugIn());
            } catch (Exception e) {
                throw new RuntimeServiceException(e);
            }
        }
    };
    
    /**
     * The JSON Readers
     */
    private static final ThreadLocal<XMLReader> jsonReaders = new ThreadLocal<XMLReader>() {

        @Override
        protected XMLReader initialValue() {
            try {
                return new JSONReader();
            } catch (Exception e) {
                throw new RuntimeServiceException(e);
            }
        }
    };
    
    /**
     * Retrieve an XML Reader instance
     * 
     * @param source
     * 
     * @return an XML Reader instance
     * 
     * @throws SAXNotSupportedException 
     * @throws SAXNotRecognizedException 
     */
    private static XMLReader getReader(
        RestSource source
    ) throws SAXNotRecognizedException, SAXNotSupportedException {
        final Format format = source.getFormat();
        switch(format) {
            case WBXML:
                XMLReader xmlReader = RestParser.wbxmlReaders.get();
                xmlReader.setFeature(WBXMLReader.EXHAUST, source.isToBeExhausted());
                return xmlReader;
            case XML:
                return RestParser.xmlReaders.get();
            case JSON:
                return RestParser.jsonReaders.get();
        }
        throw new SAXNotSupportedException("Unsupported input format: " + format);
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
    @SuppressWarnings("unused")
    public static MappedRecord parseRequest(
        RestSource source, 
        Path xri
    ) throws SAXException {
        try {
            StandardHandler handler = new StandardHandler(source);
            XMLReader reader = RestParser.getReader(source);
            reader.setContentHandler(handler);
            if(false) {
                Reader is = source.getBody().getCharacterStream();
                if(is != null) {
                    int c;
                    StringBuilder s = new StringBuilder();
                    while((c = is.read()) != -1) {
                        s.append((char)c);
                    }
                    SysLog.detail("Request", s);
                    source.getBody().setCharacterStream(new StringReader(s.toString()));
                }
            }
            reader.parse(source.getBody());
            source.close();
            return handler.getValue(xri);
        } catch (IOException exception) {
            throw new SAXException(exception);
        } catch (RuntimeException exception) {
            throw new SAXException(exception);
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
        RestSource source
    ) throws SAXException {
        try {
            StandardHandler handler = new StandardHandler(target, source);
            XMLReader reader = RestParser.getReader(source);
            reader.setContentHandler(handler);
            reader.parse(source.getBody());
            source.close();
        } catch (IOException|SAXException|RuntimeServiceException exception) {
            throw new SAXException(exception);
        }
    }

    /**
     * Parse an exception stack
     * 
     * @param source
     *            the source providing the exception stack
     * 
     * @return a {@code BasicException}
     * 
     * @throws SAXException in case of failure
     */
    public static BasicException parseException(
        RestSource source
    ) throws SAXException {
        try {
            ExceptionHandler handler = new ExceptionHandler();
            XMLReader reader = RestParser.getReader(source);
            reader.setContentHandler(handler);
            reader.parse(source.getBody());
            source.close();
            return handler.getValue();
        } catch (IOException|RuntimeException exception) {
            throw new SAXException(exception);
        }
    }

    /**
     * Tells whether the given MIME type requires a binary stream
     * 
     * @param mimeType
     * 
     * @return {@code true} if the given MIME type requires a binary stream
     */
    public static boolean isBinary(
        String mimeType
    ){
        return "application/vnd.openmdx.wbxml".equals(mimeType);
    }
    
    /**
     * Provide a {@code parse()} source
     * 
     * @param input the {@code ObjectInput}
     * 
     * @return a {@code Source}
     */
//  @SuppressWarnings("resource")
    public static RestSource asSource(
        ObjectInput input
    ){
        return new RestSource(
            new InputSource(
                input instanceof InputStream ? (InputStream) input : new ObjectInputStream(input)
            )
        );
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
        StandardHandler(RestSource source) {
            this.target = null;
            this.source = source;
        }

        /**
         * Constructor to parse a collection
         * 
         * @param source
         * @param target
         */
        StandardHandler(Record target, RestSource source) {
            this.target = target;
            this.source = source;
        }

        /**
         * The model accessor
         */
        private final Model_1_0 model = Model_1Factory.getModel();
        private static final ExtendedRecordFactory recordFactory = Records.getRecordFactory();
        private final RestSource source;
        private final Record target;
        private final Deque<Record> values = new ArrayDeque<Record>();
        private String featureName = null;
        private final Deque<String> featureNames = new ArrayDeque<String>();
        private final Deque<String> featureTypes = new ArrayDeque<String>();
        private String previousEndElement = null;
        private String href = null;
        private String version = null;
        private String id = null;
        private String index = null;
        private Record value = null;
        private Multiplicity multiplicity = null;
        private String featureType = null;
        private boolean nestedStructFieldStart = false;
        private boolean nestedStructEnd = false;

        /**
         * Retrieve the interaction's value record
         * 
         * @return the interaction's value record
         * 
         * @throws ServiceException
         */
        MappedRecord getValue(
            Path xri
        ) throws SAXException {
            String typeName = this.values.peek().getRecordName();
            if (this.isQueryType(typeName)) {
                return this.getQuery(xri);
            } else if (this.isStructureType(typeName)) {
                return (MappedRecord) this.values.peek();
            } else if (xri.isTransactionalObjectId()){
                return this.getObject(null);
            } else {
                return this.getObject(xri);
            }
        }

        /**
         * Retrieve the interaction's object record
         * 
         * @param xri 
         * 
         * @return the interaction's object record
         */
        ObjectRecord getObject(
            Path xri
        ) throws SAXException {
            final Class<ObjectRecord> recordInterface = ObjectRecord.class;
            ObjectRecord object = newMappedRecord(recordInterface);
            object.setResourceIdentifier(
                xri == null ? this.source.getXRI(this.href) : xri
            );
            object.setValue((MappedRecord) this.values.peek());
            if (this.version != null) {
                object.setVersion(Base64.decode(this.version));
            }
            if(UUIDConversion.isUUID(this.id)) {
                object.setTransientObjectId(UUIDConversion.fromString(this.id));
            }
            return object;
        }

        private <T extends MappedRecord> T newMappedRecord(
            final Class<T> recordInterface
        ) throws SAXException {
            try {
                return recordFactory.createMappedRecord(recordInterface);
            } catch (ResourceException exception) {
                throw new SAXException(exception);
            }
        }

        private MappedRecord newMappedRecord(
            String typeName
        ) throws SAXException {
            try {
                return recordFactory.createMappedRecord(typeName);
            } catch (ResourceException exception) {
                throw new SAXException(exception);
            }
        }

        private MappedRecord newMappedRecord(
            final Multiplicity multiplicity
        ) throws SAXException {
            return newMappedRecord(multiplicity.code());
        }
        
        private IndexedRecord newIndexedRecord(
            final Multiplicity multiplicity
        ) throws SAXException {
            try {
                return recordFactory.createIndexedRecord(multiplicity.code());
            } catch (ResourceException exception) {
                throw new SAXException(exception);
            }
        }
        
        /**
         * Retrieve the interaction's query record
         * 
         * @return the interaction's query record
         */
        MappedRecord getQuery(
            Path xri
        ){
            QueryRecord query = (QueryRecord) this.values.peek();
            if(xri != null) {
                query.setResourceIdentifier(xri);
            }
            return query;
        }

        @SuppressWarnings({
            "unchecked", "cast"
        })
        @Override
        public void endElement(
            String uri, 
            String localName, 
            String name
        ) throws SAXException {
            boolean nestedStructEnd = false;
            try {
                if ("org.openmdx.kernel.ResultSet".equals(name)) {
                    // Nothing to do
                } else if (name.indexOf('.') > 0) {
                    // Object or struct
                    if (this.target instanceof IndexedRecord) {
                        ((IndexedRecord)this.target).add((MappedRecord) this.getObject(null));
                        this.values.pop();
                    } else if (this.target instanceof MessageRecord) {
                        ((MessageRecord)this.target).setResourceIdentifier(this.source.getXRI(this.href));
                        ((MessageRecord)this.target).setBody((MappedRecord) this.values.peekLast());
                        nestedStructEnd = isStructEnding();
                        this.values.pop();
                    } else if(isStructEnding()) {
                        // pop struct
                        nestedStructEnd = true;
                        this.values.pop();
                    }
                } else if(ITEM_TAG.equals(name)) { 
                    propagateData();
                } else if(OBJECTS_TAG.equals(name)) {
                    // nothing to do
                } else {
                    if(this.peekMultivaluedMultiplicity() != null) {
                        this.values.pop();
                    } else if(name.equals(this.featureName) && !ITEM_TAG.equals(this.previousEndElement)) {
                        if(this.isStructureType(this.featureType)) {
                            if(!this.nestedStructEnd) {
                                this.values.pop();
                                // Reset struct in case of null-tag
                                if(this.nestedStructFieldStart && !this.values.isEmpty()) {
                                    MappedRecord holder = (MappedRecord)this.values.peek();
                                    Object featureValue = holder.get(this.featureName);
                                    if(featureValue instanceof IndexedRecord) {
                                        ((IndexedRecord)featureValue).clear();
                                    } else {
                                        holder.put(this.featureName, null);
                                    }
                                }
                            }
                        } else {
                            propagateData();
                        }
                    }
                    if(!this.featureNames.isEmpty()) {
                        this.featureName = this.featureNames.pop();
                        this.featureType = this.featureTypes.pop();
                    } else {
                        this.featureName = null;
                    }
                }
                this.previousEndElement = name;
            } catch (Exception e) {
                throw new SAXException(e);
            }
            this.nestedStructEnd = nestedStructEnd;
            this.nestedStructFieldStart = false;
        }
        
        /**
         * Tells whether struct has been added by startElement()
         * 
         * @return {@code true} if struct has been added by startElement()
         * @throws SAXException
         */
        private boolean isStructEnding(
        ) throws SAXException {
            return this.values.size() > 1 && isStructureType(this.values.peek().getRecordName());
        }

        /**
         * @throws ServiceException
         */
        @SuppressWarnings("unchecked")
        private void propagateData(
        ) throws SAXException {
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
                        PrimitiveTypes.OBJECT_ID.equals(featureType) ? Datatypes.create(Path.class, text.trim()) :
                        PrimitiveTypes.DATETIME.equals(featureType) ? Datatypes.create(Date.class,text.trim()) : 
                        PrimitiveTypes.DATE.equals(featureType) ? Datatypes.create(XMLGregorianCalendar.class, text.trim()) : 
                        PrimitiveTypes.ANYURI.equals(featureType) ? Datatypes.create(URI.class, text.trim()) : 
                        PrimitiveTypes.BINARY.equals(featureType) ? Base64.decode(text.trim()) : 
                        featureType != null && isClassType() ? new Path(text.trim()) : text;
                }
                if(data == null && this.multiplicity != Multiplicity.SINGLE_VALUE && this.multiplicity != Multiplicity.OPTIONAL) {
                    SysLog.warning(
                        "Null feature for the given multiplicity ignored",
                        multiplicity
                    );
                } else {
                    switch(this.multiplicity) {
                        case SINGLE_VALUE: case OPTIONAL:
                            ((MappedRecord) this.values.peek()).put(this.featureName, data);
                            break;
                        case LIST: case SET:  
                            ((IndexedRecord) this.value).add(data); // TODO honour index
                            break;
                        case SPARSEARRAY:
                            ((MappedRecord) this.value).put(
                                Integer.valueOf(this.index), 
                                data
                            );
                            break;
                        case STREAM:
                            ((MappedRecord) this.values.peek()).put(
                                this.featureName,
                                PrimitiveTypes.BINARY.equals(featureType) ? BinaryLargeObjects.valueOf((byte[]) data) : 
                                PrimitiveTypes.STRING.equals(featureType) ? CharacterLargeObjects.valueOf((String) data) : 
                                data
                            );
                            break;
                        default:
                            SysLog.warning(
                                "Unsupported multiplicity, feature ignored",
                                multiplicity
                            );
                    }
                }
            }
        }

        private boolean isClassType() throws SAXException {
            try {
                return model.isClassType(featureType);
            } catch (ServiceException exception) {
                throw new SAXException(exception);
            }
        }

        private Multiplicity peekMultivaluedMultiplicity(
        ){
            String type = this.values.peek().getRecordName();
            if(type.indexOf(':') < 0) {
                for(Multiplicity candidate : Multiplicity.values()) {
                    if(candidate.code().equalsIgnoreCase(type)){
                        return candidate;
                    }
                }
            }
            return null;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public void startElement(
            String uri,
            String localName,
            String qName,
            Attributes attributes
        ) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            boolean nestedStructFieldStart = false;
            try {
                final String typeName ;
                final ModelElement_1_0 featureDef;
                if(qName.indexOf('.') > 0) {
                    typeName = qName.replace('.', ':');
                    featureDef = null;
                } else if(!ITEM_TAG.equals(qName) && qName.indexOf('.') < 0 && !this.values.isEmpty()) {
                    final String recordName = this.values.peek().getRecordName();
                    featureDef = getFeatureDef(
                        recordName,
                        localName
                    );
                    typeName = getFeatureType(featureDef);
                } else {
                	featureDef = null;
                	typeName = null;
                }
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
                } else if("org:openmdx:kernel:Query".equals(typeName) && this.target instanceof QueryRecord) {
                    this.values.push(this.target);
                } else if(qName.indexOf('.') > 0) {
                    // Begin object or struct
                    MappedRecord mappedRecord = this.newMappedRecord(typeName);
                    if(!this.nestedStructFieldStart) {
                        if(this.isStructureType(typeName)) {
                            // nested struct
                            if(!this.values.isEmpty()) {
                                // multi-valued org:w3c:anyType
                                if(this.values.peek() instanceof IndexedRecord) {
                                    this.values.pop();
                                    this.featureType = typeName;
                                    this.value = mappedRecord;
                                }
                                Object values = ((MappedRecord)this.values.peek()).get(this.featureName);
                                if(values instanceof IndexedRecord) {
                                    IndexedRecord list = (IndexedRecord)values;
                                    if(attributes.getValue("index") != null) {
                                        this.index = attributes.getValue("index");
                                        int index = Integer.parseInt(this.index);
                                        if(index != list.size()) {
                                            throw new ServiceException(
                                                BasicException.Code.DEFAULT_DOMAIN,
                                                BasicException.Code.NOT_SUPPORTED,
                                                "List indices must be ascending and without holes",
                                                new BasicException.Parameter("multiplicity", this.multiplicity),
                                                new BasicException.Parameter("index", this.index),
                                                new BasicException.Parameter("item", mappedRecord)
                                            );
                                        }
                                    }
                                    list.add(mappedRecord);
                                } else {
                                    MappedRecord map = (MappedRecord)values;
                                    this.index = attributes.getValue("index");
                                    map.put(this.index, mappedRecord);                              
                                }
                            }
                        }
                        String href = attributes.getValue("href");
                        if(href != null) {
                             this.href = href;
                        }
                        String version = attributes.getValue("version");
                        if(version != null){
                            this.version = version;
                        }
                        String id = attributes.getValue("id");
                        if(id != null) {
                             this.id = id;
                        }
                        this.values.push(mappedRecord);
                    }
                } else if(ITEM_TAG.equals(qName)) {
                    this.index = attributes.getValue("index");
                    final String featureType = attributes.getValue("type");
                    if(featureType != null) {
                        this.featureType = featureType;
                    }
                } else if(OBJECTS_TAG.equals(qName)) {
                    // nothing to do
                } else {
                    if(this.featureName != null) {
                        this.featureNames.push(this.featureName);
                        this.featureTypes.push(this.featureType);
                    }
                    this.featureName = localName;
                    this.featureType = typeName;
                    this.multiplicity = getMultiplicity(featureDef);
                    if(this.isStructureType(typeName)) {
                        this.value = this.newMappedRecord(typeName);
                        nestedStructFieldStart = true;
                        switch(this.multiplicity) {
                            case OPTIONAL:
                            case SINGLE_VALUE: {                    
                                ((MappedRecord)this.values.peek()).put(
                                    this.featureName, 
                                    this.value
                                );
                                break;
                            }
                            case SET: {
                                IndexedRecord set = (IndexedRecord)((MappedRecord)this.values.peek()).get(this.featureName);
                                if(set == null) {
                                    ((MappedRecord)this.values.peek()).put(
                                        this.featureName, 
                                        set = newIndexedRecord(this.multiplicity) 
                                    );
                                }
                                set.add(this.value);
                                break;
                            }
                            case LIST: {
                                IndexedRecord list = (IndexedRecord)((MappedRecord)this.values.peek()).get(this.featureName);
                                if(list == null) {
                                    ((MappedRecord)this.values.peek()).put(
                                        this.featureName, 
                                        list = newIndexedRecord(this.multiplicity)
                                    );
                                }
                                list.add(this.value);
                                break;
                            }
                            case SPARSEARRAY: case MAP: {
                                MappedRecord map = (MappedRecord)((MappedRecord)this.values.peek()).get(this.featureName);
                                if(map == null) {
                                    ((MappedRecord)this.values.peek()).put(
                                        this.featureName, 
                                        map = newMappedRecord(this.featureType) 
                                    );
                                }
                                this.index = attributes.getValue("index");
                                map.put(this.index, this.value);                                                              
                                break;
                            }
                            default:
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE,
                                    "Unexpected multiplicity",
                                    new BasicException.Parameter("multiplicity", multiplicity)
                                );
                        }
                        this.values.push(this.value);
                    } else {
                        switch(this.multiplicity) {
                            case LIST: case SET: {
                                final MappedRecord holder = (MappedRecord) this.values.peek();
                                this.value = (Record) holder.get(this.featureName);
                                if(this.value == null) {
                                    final Multiplicity multiplicity2 = this.multiplicity;
                                    holder.put(
                                        this.featureName, 
                                        this.value = newIndexedRecord(multiplicity2)
                                    );
                                } else {
                                    ((IndexedRecord)this.value).clear();
                                }
                                this.values.push(this.value);
                                break;
                            }
                            case SPARSEARRAY: {
                                final MappedRecord holder = (MappedRecord) this.values.peek();
                                this.value = (Record) holder.get(this.featureName);
                                if(this.value == null) {
                                    holder.put(
                                        this.featureName, 
                                        this.value = newMappedRecord(this.multiplicity)
                                    );
                                } else {
                                    ((MappedRecord)this.value).clear();
                                }
                                this.values.push(this.value);
                                break;
                            }
                            case OPTIONAL: {
                                final MappedRecord holder = (MappedRecord) this.values.peek();
                                this.value = (Record) holder.get(this.featureName);
                                if(this.value == null && !holder.containsKey(this.featureName)) {
                                    holder.put(
                                        this.featureName, 
                                        this.value
                                    );
                                }
                                break;
                            }
                            default:
                                this.value = null;
                        }
                    }
                }
            } catch (Exception exception) {
                throw new SAXException(exception);
            }
            this.nestedStructFieldStart = nestedStructFieldStart;
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
                (String) model.getElementType(featureDef).getQualifiedName();
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
        protected Multiplicity getMultiplicity(
            ModelElement_1_0 featureDef
        ) throws ServiceException {
            return featureDef == null ? Multiplicity.OPTIONAL : ModelHelper.getMultiplicity(featureDef);
        }

        /**
         * Tell whether a given record is a structure type
         * 
         * @param typeName
         * 
         * @return {@code true} in case of structure, {@code false} in
         *         case of object
         *         
         * @throws SAXException
         */
        protected boolean isStructureType(
            String typeName
        ) throws SAXException{
            try {
                return 
                    !ControlObjects_2.isControlObjectType(typeName) &&  
                    model.isStructureType(typeName);
            } catch (ServiceException exception) {
                throw new SAXException(exception);
            }
        }

        /**
         * Tell whether a given record is query type
         * 
         * @param typeName
         * 
         * @return {@code true} in case of query, {@code false}
         *         otherwise
         * 
         * @throws ServiceException
         */
        protected boolean isQueryType(
            String typeName
        ){
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
        BasicException getValue(
        ){
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
                        Integer.parseInt(this.exceptionCode),
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
         * @return {@code true} if some content is available
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
        ){
            this.binaryData = offset == 0 && length == data.length ? data : ArraysExtension.copyOfRange(data, offset, offset + length);           
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeBinaryData(org.w3c.cci2.BinaryLargeObject)
         */
        @Override
        public void writeBinaryData(
            BinaryLargeObject data
        ){
            throw new UnsupportedOperationException("Large object streaming not yet implemented");
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.xml.stream.LargeObjectWriter#writeCharacterData(org.w3c.cci2.CharacterLargeObject)
         */
        @Override
        public void writeCharacterData(
            CharacterLargeObject data
        ){
            throw new UnsupportedOperationException("Large object streaming not yet implemented");
        }

    }

}

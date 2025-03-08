/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Import Handler
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
package org.openmdx.application.xml.spi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Level;

import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.loading.Resources;
import org.openmdx.kernel.log.SysLog;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.spi2.Datatypes;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Import Handler
 */
public class ImportHandler extends DefaultHandler {

	/**
     * Constructor
     *
     * @param target
     * @param source
     * @param defaultImportMode
     */
    public ImportHandler(
        ImportTarget target,
        InputSource source,
        ImportMode defaultImportMode
    ) {
        this.target = target;
        this.url = getDocumentURL(source);
        this.binary = isBinary(source);
        this.defaultImportMode = defaultImportMode;
    }

    private static final String MODEL1_SCHEMA = Resources.toResourceXRI("org/omg/model1/xmi1/model1.xsd");
    
    /**
     * Be aware, {@code SimpleDateFormat} instances are not thread safe.
     */
    private final SimpleDateFormat localSecondFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Be aware, {@code SimpleDateFormat} instances are not thread safe.
     */
    private final SimpleDateFormat localMillisecondFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    // stack of objects currently open
    private Stack<ObjectRecord> objectStack = null;

    // Path length of the request collection's first element
    private int pendingAt = -1;

    /**
     * set of XML elements which denote object elements, i.e. elements with a
     * qualifier This set is required to determine on endElement operations
     * whether an attribute/ struct/reference is closed or an object.
     */
    private Map<String, Map<String, String>> objectElements = null;

    // state/context of current object
    private ObjectRecord currentObject = null;

    /**
     * The current object's operation
     */
    private ImportMode currentObjectOperation;

    private Path currentPath = null;

    private StringBuilder currentAttributeValue = null;

    private String currentAttributeName = null;

    private String currentLocalpartObject = null;

    private int nextTemporaryId = -1;

    private int currentAttributeOffset = 0;

    private int currentAttributePosition = -1;

    private String currentAttributeKey = null;

    private Multiplicity currentAttributeMultiplicity = null;

    private String currentAttributeOperation = null;

    // true when the last call was endElement(). Is set to false by
    // startElement()
    private boolean previousElementEnded = true;

    private final ImportTarget target;

    private final URL url;

    private final ImportMode defaultImportMode;

    /**
     * {@code true} in case of a WBXML input
     */
    private final boolean binary;

    // cached type definitions (global in classloader)
    private static final Set<String> loadedSchemas = new HashSet<String>();

    // attributes types as (<qualified attribute name>, <qualified type name)
    // pair.
    private static final Map<String, String> attributeTypes = new HashMap<String, String>();

    // attribute multiplicities
    private static final Map<String, String> attributeMultiplicities = new HashMap<String, String>();

    // qualifier names of a complex schema type.
    private static final Map<String, String> qualifierNames = new HashMap<String, String>();

    private static final List<Path> cachedParentPaths = new ArrayList<Path>();

    /**
     * Suffix marking date/time values as UTC based
     */
    private static String[] UTC_IDS = {
        "Z", // canonical form
        "+00", "-00",
        "+00:00", "-00:00"
    };

    private static final String LEGACY_RESOURCE_PREFIX = "xri:+resource/";
    private static final String FALLBACK_ATTRIBUTE_TYPE = "org.w3c.string";

    /**
     * Determine the document URL
     * 
     * @param source
     *            the {@code InputSource}
     * 
     * @return the document {@code URL}
     */
    private static URL getDocumentURL(
        InputSource source
    ) {
        String uri = source.getSystemId();
        if (uri == null) {
            return null;
        } else {
            try {
                return new URL(uri);
            } catch (MalformedURLException exception) {
                try {
                    return new File(uri).toURI().toURL();
                } catch (MalformedURLException exception1) {
                    return null;
                }
            }
        }
    }

    /**
     * Determine the document is binary
     * 
     * @param source
     *            the {@code InputSource}
     * 
     * @return {@code true} in case of WBXML input
     */
    private static boolean isBinary(
        InputSource source
    ) {
        if (source.getCharacterStream() != null) {
            return false;
        }
        String uri = source.getSystemId();
        if (uri != null) {
            uri = uri.toLowerCase();
            if (uri.endsWith(".wbxml")) {
                return true;
            }
            if (uri.endsWith(".xml")) {
                return false;
            }
        }
        String encoding = source.getEncoding();
        return encoding == null;
    }

    /**
     * Tells whether we are reading an XML or a WBXML source
     * 
     * @return {@code true} in case of a WBXML source
     */
    public boolean isBinary() {
        return this.binary;
    }

    // -----------------------------------------------------------------------
    // Implements EntityResolver
    // -----------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(
        String publicId,
        String systemId
    )
        throws SAXException {
        return this.getSchemaInputSource(systemId);
    }

    /**
     * Initialize qualifierNames, attributeTypes, attributeMultiplicities
     * 
     * @param schemaDocument
     * 
     * @throws ServiceException
     */
    private void initTypes(
        Document schemaDocument
    )
        throws ServiceException {
        Element docElement = schemaDocument.getDocumentElement();
        NodeList complexTypeNodes = docElement.getElementsByTagName("xsd:complexType");
        // iterate all xsd:complexType
        int complexTypeNodesLength = complexTypeNodes.getLength();
        for (int i = 0; i < complexTypeNodesLength; i++) {
            Node complexType = complexTypeNodes.item(i);
            NamedNodeMap complexTypeAttributes = complexType.getAttributes();
            Attr complexTypeName = (Attr) complexTypeAttributes.getNamedItem("name");
            if (complexTypeName != null) {
                // get qualifierName of complex type
                NodeList attributeNodes = ((Element) complexType).getElementsByTagName("xsd:attribute");
                int attributeNodesLength = attributeNodes.getLength();
                for (int j = 0; j < attributeNodesLength; j++) {
                    Node attributeNode = attributeNodes.item(j);
                    if ("_qualifier".equals(attributeNode.getAttributes().getNamedItem("name").getNodeValue())) {
                        ImportHandler.qualifierNames.put(
                            complexTypeName
                                .getNodeValue(), attributeNode
                                    .getAttributes()
                                    .getNamedItem("fixed")
                                    .getNodeValue()
                        );
                    }
                }
                // get qualifierTypes, qualifierMultiplicities
                NodeList elementNodes = ((Element) complexType).getElementsByTagName("xsd:element");
                int elementNodesLength = elementNodes.getLength();
                for (int j = 0; j < elementNodesLength; j++) {
                    Node element = elementNodes.item(j);
                    NamedNodeMap elementAttributes = element.getAttributes();
                    Attr attributeName = (Attr) elementAttributes.getNamedItem("name");
                    Attr attributeType = (Attr) elementAttributes.getNamedItem("type");
                    // default multiplicity Multiplicities.SINGLE_VALUE
                    if (attributeName != null &&
                        !"_content".equals(attributeName.getNodeValue()) &&
                        !"_object".equals(attributeName.getNodeValue()) &&
                        !"_item".equals(attributeName.getNodeValue()) &&
                        attributeName.getNodeValue().indexOf('.') < 0) {
                        String attributeTypeName = null;
                        // multi-valued attribute defined as complexType
                        if (attributeType == null) {
                            // find xsd:attribute name=""_multiplicity"" of
                            // complexType
                            NodeList l0 = ((Element) element).getElementsByTagName("xsd:complexType");
                            for (int i0 = 0; i0 < l0.getLength(); i0++) {
                                Node n0 = l0.item(i0);
                                NodeList l1 = ((Element) n0).getElementsByTagName("xsd:attribute");
                                for (int i1 = 0; i1 < l1.getLength(); i1++) {
                                    Node n1 = l1.item(i1);
                                    if ("_multiplicity".equals(n1.getAttributes().getNamedItem("name").getNodeValue())) {
                                        ImportHandler.attributeMultiplicities.put(
                                            complexTypeName.getNodeValue() + ":" + attributeName.getNodeValue(),
                                            n1.getAttributes().getNamedItem("fixed").getNodeValue()
                                        );
                                    }
                                }
                            }
                            // xsd:extension base=
                            Node extension = element;
                            while (extension != null &&
                                !"xsd:extension".equals(extension.getNodeName())) {
                                NodeList children = extension.getChildNodes();
                                extension = null;
                                for (int k = 0; k < children.getLength(); k++) {
                                    if (children.item(k).getNodeType() == Node.ELEMENT_NODE) {
                                        extension = children.item(k);
                                        break;
                                    }
                                }
                            }
                            if (extension != null) {
                                NamedNodeMap extensionAttributes = extension.getAttributes();
                                Attr extensionBase = (Attr) extensionAttributes.getNamedItem("base");
                                if (extensionBase != null) {
                                    attributeTypeName = extensionBase.getValue();
                                }
                            }
                        } else {
                            //
                            // A simple type is defined as:
                            // 
                            // <pre>
                            // &lt;xsd:element name=&quot;org.omg.model1.Element.annotation&quot; type=&quot;org.w3c.string&quot; minOccurs=&quot;0&quot;/&gt;
                            // </pre>
                            //
                            attributeTypeName = attributeType.getValue();
                        }
                        if (attributeTypeName == null) {
                            // SysLog.warning("type for attribute " +
                            // attributeName +
                            // " not defined. Assuming org:w3c:string");
                            attributeTypeName = FALLBACK_ATTRIBUTE_TYPE;
                        }
                        ImportHandler.attributeTypes.put(
                            complexTypeName.getNodeValue().replace('.', ':') + ":" + attributeName.getNodeValue(),
                            attributeTypeName
                        );
                    }
                }
            }
        }
    }

    /**
     * Load the meta data
     * 
     * @param uriSchema
     * 
     * @throws ServiceException
     */
    private void loadMetaData(
        String uriSchema
    )
        throws ServiceException {
        if (MODEL1_SCHEMA.equals(uriSchema)) {
            Model1MetaData.amendAttributeTypes(attributeTypes);
            Model1MetaData.amendAttributeMultiplicities(attributeMultiplicities);
            Model1MetaData.amendQualifierNames(qualifierNames);
        } else
            try {
                Document schemaDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    this.getSchemaInputSource(uriSchema)
                );
                this.initTypes(schemaDocument);
            } catch (Exception exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Could not retrieve meta data from schema",
                    new BasicException.Parameter("schema", uriSchema)
                );
            }
        ImportHandler.loadedSchemas.add(uriSchema);
    }

    /**
     * Retrieve the metadata if necessary
     * 
     * @param uriSchema
     * @throws ServiceException
     */
    private void retrieveMetaData(
        String uriSchema
    )
        throws SAXException {
        try {
            if (uriSchema.indexOf("/xmi/") > 0)
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "\"/xmi/\" directory no longer supported",
                    new BasicException.Parameter("uri", uriSchema)
                );
            synchronized (ImportHandler.loadedSchemas) {
                if (!ImportHandler.loadedSchemas.contains(uriSchema)) {
                    loadMetaData(uriSchema);
                }
            }
        } catch (ServiceException exception) {
            throw new SAXException(exception.log());
        }
    }

    // ------------------------------------------------------------------------
    // Implements ContentHandler
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    @Override
    public void startDocument()
        throws SAXException {
        this.currentPath = new Path("");
        this.objectStack = new Stack<ObjectRecord>();
        this.objectElements = new HashMap<String, Map<String, String>>();
        this.currentObject = null;
        this.currentAttributeValue = null;
        this.previousElementEnded = true;
    }

    /**
     * Start an XML element. Start and end look like: <element> ... embedded
     * elements </element>
     */
    @Override
    public void startElement(
        String uri,
        String localpart,
        String rawname,
        Attributes attributes
    )
        throws SAXException {
        this.previousElementEnded = false;
        this.currentAttributeValue = null;
        // check whether to load schema
        for (int i = 0; i < attributes.getLength(); i++) {
            if ("noNamespaceSchemaLocation".equals(attributes.getLocalName(i))) {
                this.retrieveMetaData(attributes.getValue(i));
            }
        }
        List<String> element = parseElement(localpart);
        String qualifierName = ImportHandler.qualifierNames.get(localpart);
        final Multiplicity attributeMultiplicity = getCurrentAttributeMultiplicity(localpart);
        //
        // element has a qualifier --> new object
        //
        if (qualifierName != null) {
            try {

                this.currentLocalpartObject = localpart;
                this.objectStack.push(this.currentObject);
                //
                // Remember attribute values of element. These are: - localpart
                // - <qualifier> - "_qualifier" (fixed) - "_operation"
                // (optional)
                //
                Map<String, String> attributeValues = new HashMap<String, String>();
                attributeValues.put("_qualifier", qualifierName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attributeName = attributes.getLocalName(i);
                    if (!"".equals(attributeName)
                        && !"_qualifier".equals(attributeName)
                        && !"noNamespaceSchemaLocation".equals(attributeName)
                        && !"xsi".equals(attributeName)) {
                        attributeValues.put(
                            attributeName, attributes
                                .getValue(i)
                        );
                    }
                }
                if (this.currentPath.size() < 4) {
                    // "null" fix for Authority|Provider
                    attributeValues.put("_operation", "null");
                } else if (attributeValues.size() < 3 ||
                    "".equals(attributeValues.get("_operation"))) {
                    // "set" is the default operation
                    attributeValues.put("_operation", this.defaultImportMode.name().toLowerCase());
                }
                String qualifier = attributeValues.get(qualifierName);
                if (qualifier == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "required attribute " + qualifierName + " not found.",
                        new BasicException.Parameter("localpart", localpart),
                        new BasicException.Parameter(
                            "attributes",
                            attributeValues
                        )
                    );
                }
                this.objectElements.put(localpart, attributeValues);
                this.currentPath = this.currentPath.getChild(
                    "".equals(qualifier) ? ":" + this.nextTemporaryId-- : qualifier
                );
                this.currentObject = Facades.newObject(
                    this.currentPath,
                    this.toNameComponent(element)
                ).getDelegate();
                String operation = attributeValues.get("_operation");
                if (!"null".equals(operation)) {
                    Path objectId = Object_2Facade.getPath(this.currentObject);
                    // An object and its sub-objects are processed as a single
                    // unit of work
                    if (this.pendingAt == -1) {
                        this.pendingAt = objectId.size();
                    }
                    if ("create".equals(operation)) {
                        this.currentObjectOperation = ImportMode.CREATE;
                    } else if ("set".equals(operation)) {
                        this.currentObjectOperation = ImportMode.SET;
                    } else if ("update".equals(operation)) {
                        this.currentObjectOperation = ImportMode.UPDATE;
                    } else if ("operation".equals(operation) ||
                        "remove".equals(operation)) {
                        //
                        // unsupported request
                        //
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "No longer supported _operation argument",
                            new BasicException.Parameter(BasicException.Parameter.XRI, objectId),
                            new BasicException.Parameter("unsupported", "operation", "remove"),
                            new BasicException.Parameter("requested", operation)
                        );
                    } else {
                        //
                        // illegal request
                        //
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "Unsupported _operation argument",
                            new BasicException.Parameter(BasicException.Parameter.XRI, objectId),
                            new BasicException.Parameter("supported", "", "null", "set", "create", "update"),
                            new BasicException.Parameter("requested", operation)
                        );
                    }
                }
            } catch (ServiceException e) {
                throw new SAXException(e);
            }
        } else if ("_item".equals(localpart)) {
            //
            // <"_item"> start element of value of a multi-valued attribute
            //
            this.currentAttributeOperation = attributes.getValue("_operation");
            final String _position = attributes.getValue("_position");
            int position = _position == null ? 0 : Integer.parseInt(_position);
            this.currentAttributePosition = position == -1 ? this.currentAttributePosition + 1 : position;
            this.currentAttributeKey = attributes.getValue("_key");
        } else if (attributeMultiplicity != null && attributeMultiplicity.isMultiValued()) {
            // multi-valued attribute with attributes 'offset', 'multiplicity' last
            // component of the element is the unqualified element name
            this.currentAttributeName = localpart;
            this.currentAttributeMultiplicity = attributeMultiplicity;
            try {
                if (attributeMultiplicity.isCollection()) {
                    // LIST, SET, SPARSEARRAY
                    this.currentPath = this.currentPath.getChild(
                        element.get(element.size() - 1)
                    );
                    this.currentAttributeOffset = attributes.getValue("_offset") == null ? 0
                        : Integer.parseInt(
                            attributes.getValue("_offset")
                        );
                    this.currentAttributePosition = -1;
                    Facades.asObject(this.currentObject).clearAttributeValuesAsList(this.currentAttributeName);
                } else {
                    // MAP
                    String featureName = element.get(element.size() - 1);
                    this.currentPath = this.currentPath.getChild(featureName);
                    this.currentAttributeOffset = 0; // no offset for maps
                    this.currentAttributePosition = -1; // no position for maps
                    Facades.asObject(
                        this.currentObject
                    ).attributeValuesAsMap(
                        this.currentAttributeName
                    ).clear();
                }
            } catch (ServiceException exception) {
                throw new SAXException(exception);
            }
        } else {
            //
            // no attribute --> no qualifier -> element is
            // reference|attribute|struct last component of the element is the
            // unqualified element name
            //
            if ("_object".equals(localpart)) {
                this.currentAttributeName = null;
            } else if ("_content".equals(localpart)) {
                this.currentObject = null;
            } else {
                this.currentPath = currentPath.getChild(element.get(element.size() - 1));
                this.currentAttributeName = localpart;
                this.currentAttributeOffset = 0;
                this.currentAttributePosition = -1;
            }
        }
    }

    private Multiplicity getCurrentAttributeMultiplicity(String localpart) {
        final String multiplicityKey = this.currentLocalpartObject + ":" + localpart;
        final String attributeMultiplicity = ImportHandler.attributeMultiplicities.get(
            multiplicityKey
        );
        return attributeMultiplicity == null ? null : Multiplicity.parse(attributeMultiplicity);
    }

    /**
     * 
     */
    @Override
    public void characters(
        char[] ch,
        int offset,
        int length
    )
        throws SAXException {
        if (this.currentAttributeValue == null) {
            this.currentAttributeValue = new StringBuilder();
        }
        this.currentAttributeValue.append(ch, offset, length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(
        String uri,
        String localName,
        String qName
    )
        throws SAXException {
        try {
            //
            // The end element corresponds to an Object. Process the
            // currentObject and get next from stack
            //
            if ("_object".equals(localName) ||
                this.objectElements.get(localName) != null) {
                if (this.currentObject != null) {
                    if (this.currentObjectOperation != null) {
                        this.target.importObject(this.currentObjectOperation, this.currentObject);
                        this.currentObjectOperation = null;
                    }
                    //
                    // If object was already closed by <"_object"> it can not be
                    // closed again by the object end tag. Skip endObject() in
                    // this case
                    //
                    try {
                        if (this.pendingAt == Object_2Facade.getPath(this.currentObject).size()) {
                            this.pendingAt = -1;
                        }
                    } catch (RuntimeException e) {
                        throw new SAXException(e);
                    }
                    this.currentObject = this.objectStack.pop();
                }
                this.currentLocalpartObject = null;
                //
                // In v3 format there are never pending objects.
                //
                if ("_object".equals(localName) &&
                    (this.currentObject != null || !this.objectStack.isEmpty())) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "pending object found. There must be no pending objects in v3 schema format.",
                        new BasicException.Parameter("object", this.currentObject)
                    );
                }
            }
            // it is an end element of a Attribute|Reference|StructureType
            else if (this.currentObject != null) {
                /**
                 * store attribute value the attribute value between two closing
                 * elements is a sequence of blanks. Skip this value
                 */
                if (!this.previousElementEnded) {
                    Object_2Facade facade = Facades.asObject(this.currentObject);
                    // attribute name
                    String attributeName = this.currentPath.getLastSegment().toClassicRepresentation();
                    Object value = getCurrentAttributeValue();
                    //
                    // Set values according to multiplicity and operation
                    //
                    boolean supported = true;
                    int absolutePosition = this.currentAttributeOffset + this.currentAttributePosition;

                    if (this.currentAttributeMultiplicity == null) {
                        // SPARSEARRAY
                        // In case of v2 format the multiplicity is not set
                        if (this.currentAttributeOperation == null ||
                            "".equals(this.currentAttributeOperation) ||
                            "set".equals(this.currentAttributeOperation)) {
                            if (facade.isTypeSafe()) {
                                if (facade.isCollection(attributeName)) {
                                    if (absolutePosition < 0) {
                                        Collection<Object> values = facade.attributeValuesAsCollection(attributeName);
                                        values.add(value);
                                    } else {
                                        List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                                        values.set(absolutePosition, value);
                                    }
                                } else {
                                    facade.setAttributeValue(attributeName, value);
                                }
                            } else {
                                List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                                if (absolutePosition < 0) {
                                    values.add(value);
                                } else {
                                    values.set(absolutePosition, value);
                                }
                            }
                        } else if ("add".equals(this.currentAttributeOperation)) {
                            List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                            values.add(value);
                        } else if ("remove".equals(this.currentAttributeOperation)) {
                            List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                            values.remove(value);
                        } else {
                            supported = false;
                        }
                    } else {
                        switch (this.currentAttributeMultiplicity) {
                            case SET: {
                                final List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                                switch (getCurrentAttributeOperation(AttributeOperation.ADD)) {
                                    case ADD:
                                        if (value != null) {
                                            values.add(value);
                                        }
                                        break;
                                    case REMOVE:
                                        values.remove(value);
                                        break;
                                    default:
                                        supported = false;
                                }
                            }
                                break;
                            case LIST: {
                                List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                                switch (getCurrentAttributeOperation(AttributeOperation.ADD)) {
                                    case ADD:
                                        if (value != null) {
                                            values.add(value);
                                        }
                                        break;
                                    case REMOVE:
                                        values.remove(value);
                                        break;
                                    case SET:
                                        values.set(absolutePosition, value);
                                        break;
                                    default:
                                        supported = false;
                                }
                            }
                                break;
                            case SPARSEARRAY:
                                switch (getCurrentAttributeOperation(AttributeOperation.ADD)) {
                                    case SET:
                                        if (facade.isTypeSafe()) {
                                            if (facade.isCollection(attributeName)) {
                                                if (absolutePosition < 0) {
                                                    if (value != null) {
                                                        Collection<Object> values = facade.attributeValuesAsCollection(attributeName);
                                                        values.add(value);
                                                    }
                                                } else {
                                                    List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                                                    values.set(absolutePosition, value);
                                                }
                                            } else {
                                                facade.setAttributeValue(attributeName, value);
                                            }
                                        } else {
                                            List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                                            if (absolutePosition < 0) {
                                                if (value != null) {
                                                    values.add(value);
                                                }
                                            } else {
                                                values.set(absolutePosition, value);
                                            }
                                        }
                                        break;
                                    case ADD: {
                                        if (value != null) {
                                            List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                                            values.add(value);
                                        }
                                    }
                                        break;
                                    case REMOVE: {
                                        List<Object> values = facade.getAttributeValuesAsGuardedList(attributeName);
                                        if (value != null) {
                                            values.remove(value);
                                        } else if (absolutePosition >= 0) {
                                            values.remove(absolutePosition);
                                        } else {
                                            supported = false;
                                        }
                                    }
                                        break;
                                    default:
                                        supported = false;
                                }
                                break;
                            case MAP:
                                if (value != null) {
                                    final Map<String, Object> values = facade.attributeValuesAsMap(attributeName);
                                    if (this.currentAttributeKey == null) {
                                        throw new ServiceException(
                                            BasicException.Code.DEFAULT_DOMAIN,
                                            BasicException.Code.ASSERTION_FAILURE,
                                            "Map items need a _key",
                                            new BasicException.Parameter("object", this.currentObject),
                                            new BasicException.Parameter("_multiplicity", this.currentAttributeMultiplicity)
                                        );
                                    }
                                    switch (getCurrentAttributeOperation(AttributeOperation.SET)) {
                                        case SET:
                                            values.put(this.currentAttributeKey, value);
                                            break;
                                        case REMOVE:
                                            values.remove(this.currentAttributeKey, value);
                                            break;
                                        default:
                                            supported = false;
                                    }
                                }
                                break;
                            default:
                                supported = false;
                        }
                    }
                    if (!supported) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_SUPPORTED,
                            "attribute operation for this multiplicity not supported.",
                            new BasicException.Parameter(
                                "object",
                                this.currentObject
                            ),
                            new BasicException.Parameter(
                                "attribute",
                                this.currentAttributeName
                            ),
                            new BasicException.Parameter(
                                "operation",
                                this.currentAttributeOperation
                            ),
                            new BasicException.Parameter(
                                "multiplicity",
                                this.currentAttributeMultiplicity
                            ),
                            new BasicException.Parameter(
                                "value",
                                value
                            )
                        );
                    }
                    this.currentAttributeOperation = null;
                }
            }
            if (!"_item".equals(localName) &&
                !"_object".equals(localName) &&
                !"_content".equals(localName)) {
                // </item> is the end tag for a value of a multi-valued attribute.
                // Leave currentPath unchanged.
                this.currentPath = this.currentPath.getParent();
                this.currentAttributeMultiplicity = null;
            }
            this.currentAttributeValue = null;
            this.previousElementEnded = true;
        } catch (ServiceException exception) {
            throw Throwables.log(
                new SAXException(exception)
            );
        } catch (RuntimeException exception) {
            throw Throwables.log(
                new SAXException(exception)
            );
        }
    }

    private AttributeOperation getCurrentAttributeOperation(
        AttributeOperation defaultValue
    ) {
        return this.currentAttributeOperation == null || this.currentAttributeOperation.isEmpty() ? defaultValue
            : AttributeOperation.parse(this.currentAttributeOperation);
    }

    private Object getCurrentAttributeValue()
        throws ServiceException {
        // attribute type
        String attributeType = getCurrentAttributeType();
        if (this.currentAttributeValue != null) {
            if (FALLBACK_ATTRIBUTE_TYPE.equals(attributeType) || "string".equals(attributeType)) {
                return this.currentAttributeValue.toString();
            } else if ("org.w3c.short".equals(attributeType) || "short".equals(attributeType)) {
                String v = this.currentAttributeValue.toString().trim();
                if (v.length() > 0) {
                    return Short.valueOf(v);
                }
            } else if ("org.w3c.long".equals(attributeType) || "long".equals(attributeType)) {
                String v = this.currentAttributeValue.toString().trim();
                if (v.length() > 0) {
                    return Long.valueOf(v);
                }
            } else if ("org.w3c.integer".equals(attributeType) || "integer".equals(attributeType)) {
                String v = this.currentAttributeValue.toString().trim();
                if (v.length() > 0) {
                    return Integer.valueOf(v);
                }
            } else if ("org.w3c.decimal".equals(attributeType) || "decimal".equals(attributeType)) {
                String v = this.currentAttributeValue.toString().trim();
                if (v.length() > 0) {
                    return new BigDecimal(v);
                }
            } else if ("org.w3c.boolean".equals(attributeType) || "boolean".equals(attributeType)) {
                String v = this.currentAttributeValue.toString().trim();
                if (v.length() > 0) {
                    return Boolean.valueOf(v);
                }
            } else if ("org.w3c.dateTime".equals(attributeType) || "dateTime".equals(attributeType)) {
                String v = this.currentAttributeValue.toString().trim();
                if (v.length() > 0) {
                    return parseDateTime(v);
                }
            } else if ("org.w3c.date".equals(attributeType) || "date".equals(attributeType)) {
                String v = this.currentAttributeValue.toString().trim();
                if (v.length() > 0) {
                    try {
                        return Datatypes.create(
                            Datatypes.DATE_CLASS,
                            v
                        );
                    } catch (IllegalArgumentException e) {
                        throw new ServiceException(e);
                    }
                }
            } else if ("org.openmdx.base.duration".equals(attributeType)) {
                return Datatypes.create(Datatypes.DURATION_CLASS, this.currentAttributeValue.toString().trim());
            } else if ("org.openmdx.base.anyURI".equals(attributeType)) {
                return Datatypes.create(URI.class, this.currentAttributeValue.toString().trim());
            } else if ("org.openmdx.base.ObjectId".equals(attributeType)) {
                Path pathValue = new Path(
                    this.currentAttributeValue.toString().trim()
                );
                int index = cachedParentPaths.indexOf(pathValue.getParent());
                if (index >= 0) {
                    return cachedParentPaths.get(index).getChild(pathValue.getLastSegment());
                } else {
                    cachedParentPaths.add(pathValue.getParent());
                    return pathValue;
                }
            } else if ("org.w3c.binary".equals(attributeType)) {
                return Base64.decode(
                    this.currentAttributeValue.toString()
                );
            } else {
                ImportHandler.attributeTypes.put(
                    getQualifiedCurrentAttributeName(),
                    FALLBACK_ATTRIBUTE_TYPE
                );
                return this.currentAttributeValue.toString();
            }
        }
        return null;
    }

    private String getCurrentAttributeType() {
        String attributeType = ImportHandler.attributeTypes.get(getQualifiedCurrentAttributeName());
        return attributeType == null ? FALLBACK_ATTRIBUTE_TYPE : attributeType;
    }

    private String getQualifiedCurrentAttributeName() {
        final String qualifiedClassName = this.currentObject.getValue().getRecordName();
        return qualifiedClassName + ":" + this.currentAttributeName;
    }

    /**
     * timePoint is of the form 2001-09-29T15:45:21.798Z
     * 
     * @param source
     *            the time stamp to be parsed
     * 
     * @return the date/time value
     * 
     * @throws ServiceException
     */
    private #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif parseDateTime(
        String source
    )
        throws ServiceException {
        String v = source;
        try {
            //
            // Handle UTC date/time value
            // 
            for (String utcId : UTC_IDS) {
                if (v.endsWith(utcId)) {
                    return Datatypes.create(Datatypes.DATE_TIME_CLASS, v);
                }
            }
            //
            // Handle non-UTC date/time value
            //
            int timePosition = v.indexOf('T');
            int timeZonePosition = v.lastIndexOf('+', timePosition);
            if (timeZonePosition < 0) {
                timeZonePosition = v.lastIndexOf('-', timePosition);
            }
            if (timeZonePosition > timePosition &&
                !v.regionMatches(true, timeZonePosition - 3, "GMT", 0, 3)) {
                v = v.substring(0, timeZonePosition) + "GMT" + v.substring(timeZonePosition);
            }
            return (v.indexOf('.', timePosition) < 0 ? localSecondFormat : localMillisecondFormat).parse(v)#if !CLASSIC_CHRONO_TYPES .toInstant()#endif;
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(exception);
        } catch (ParseException e) {
            throw new ServiceException(e);
        }
    }

    // ------------------------------------------------------------------------
    // Implements errorHandler
    // ------------------------------------------------------------------------

    /**
     * Provide location information for the error handler
     */
    private String getLocationString(
        SAXParseException ex
    ) {
        StringBuilder str = new StringBuilder();
        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            str.append(systemId);
        }
        str.append(
            ':'
        ).append(
            ex.getLineNumber()
        ).append(
            ':'
        ).append(
            ex.getColumnNumber()
        );
        return str.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(
        SAXParseException e
    )
        throws SAXException {
        SysLog.warning(getLocationString(e), e.getMessage());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(
        SAXParseException e
    )
        throws SAXException {
        throw new SAXException(
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                "XML parse error",
                new BasicException.Parameter("message", e.getMessage()),
                new BasicException.Parameter("location", getLocationString(e))
            ).log()
        );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(
        SAXParseException e
    )
        throws SAXException {
        throw new SAXException(
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.PROCESSING_FAILURE,
                "fatal XML parse error",
                new BasicException.Parameter("message", e.getMessage()),
                new BasicException.Parameter("location", getLocationString(e))
            )
        );
    }

    // ------------------------------------------------------------------------
    // XmlImporter
    // ------------------------------------------------------------------------

    /**
     * Parse elementName which is of the format <className>.<propertyName>.
     * Translate into an List of strings.
     */
    private List<String> parseElement(String elementName) {
        List<String> element = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(elementName, ".");
        while (tokenizer.hasMoreTokens()) {
            element.add(tokenizer.nextToken());
        }
        return element;
    }

    /**
     * 
     * @param nameElements
     * @return
     */
    private String toNameComponent(
        List<String> nameElements
    ) {
        if (nameElements.size() == 0) {
            return "";
        }
        StringBuilder nameComponent = new StringBuilder(nameElements.get(0));
        for (int i = 1; i < nameElements.size(); i++) {
            nameComponent.append(':').append(nameElements.get(i));
        }
        return nameComponent.toString();
    }

    private InputSource getSchemaInputSource(
        String schemaUri
    ) throws SAXException {
        final InputStream schemaSource;
        if (Resources.isResourceXRI(schemaUri)) {
            schemaSource = getSchemaSource(Resources.fromURI(schemaUri));
        } else if (schemaUri.startsWith(LEGACY_RESOURCE_PREFIX)) {
            SysLog.warning(
                "Deprecated XRI 1 format '" + LEGACY_RESOURCE_PREFIX
                + "…', use 'xri://+resource/…'!",
                schemaUri
            );
            schemaSource = Resources.getResourceAsStream(schemaUri.substring(LEGACY_RESOURCE_PREFIX.length()));
        } else {
            schemaSource = getSchemaSource(schemaUri);
        }
        if (schemaSource == null) {
            throw BasicException.initHolder(
                new SAXException(
                    "Schema access failed. xsi:noNamespaceSchemaLocation must be an URL supported by openMDX, i.e. http:/, file:/, xri://+resource/…",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        new BasicException.Parameter("document", this.url),
                        new BasicException.Parameter("schema", schemaUri)
                    )
                )
            );
        }
        SysLog.log(Level.FINE, "Sys|Providing schema|Document={0}, Schema={1}", this.url, schemaUri);
        return new InputSource(schemaSource);
    }

    /**
     * @param schemaUri
     * 
     * @return the schema input stream, or {@code null} in case of failure
     */
    private InputStream getSchemaSource(String schemaUri) {
        try {
            return schemaUri == null ? null : getSchemaSource(new URL(getContextURL(), schemaUri));
        } catch (IOException exception) {
            return null;
        }
    }

	private InputStream getSchemaSource(final URL schemaUrl){
		try {
			return schemaUrl == null ? null : schemaUrl.openStream();
		} catch (IOException e) {
			return null;
		}
	}

    private URL getContextURL()
        throws MalformedURLException {
        final String uri = this.url.toString();
        if (this.url.toString().startsWith("file:")
            && !this.url.toString().startsWith("file:/")
            && !this.url.toString().startsWith("file:./")) {
            return new URL("file:./" + uri.substring(5));
        } else {
            return this.url;
        }
    }

}

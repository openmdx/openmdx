/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ImportHandler.java,v 1.17 2010/06/02 13:43:14 hburger Exp $
 * Description: Import Handler
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/02 13:43:14 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.resource.ResourceException;
import javax.resource.cci.MappedRecord;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.w3c.spi2.Datatypes;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Import Handler
 */
@SuppressWarnings("unchecked")
public class ImportHandler extends DefaultHandler {

    /**
     * Constructor 
     *
     * @param target
     * @param documentURL 
     */
    public ImportHandler(
         ImportTarget target, 
         URL documentURL
    ){
        this.target = target;
        this.url = documentURL;
    }

    /**
     * Be aware, <code>SimpleDateFormat</code> instances are not thread safe.
     */
    private final SimpleDateFormat localSecondFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Be aware, <code>SimpleDateFormat</code> instances are not thread safe.
     */
    private final SimpleDateFormat localMillisecondFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    // stack of objects currently open
    private Stack<MappedRecord> objectStack = null;

    // Path length of the request collection's first element
    private int pendingAt = -1;

    /**
     * set of XML elements which denote object elements, i.e. elements with a
     * qualifier This set is required to determine on endElement operations
     * whether an attribute/ struct/reference is closed or an object.
     */
    private Map objectElements = null;

    // state/context of current object
    private MappedRecord currentObject = null;

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

    private String currentAttributeMultiplicity = null;

    private String currentAttributeOperation = null;

    // true when the last call was endElement(). Is set to false by
    // startElement()
    private boolean previousElementEnded = true;

    private final ImportTarget target;

    private final URL url;

    // cached type definitions (global in classloader)
    private static final Set loadedSchemas = new HashSet();

    // attributes types as (<qualified attribute name>, <qualified type name)
    // pair.
    private static final Map attributeTypes = new HashMap();

    // attribute multiplicities
    private static final Map attributeMultiplicities = new HashMap();

    // qualifier names of a complex schema type.
    private static final Map qualifierNames = new HashMap();

    /**
     * Suffix marking date/time values as UTC based
     */
    private static String[] UTC_IDS = {
        "Z", // canonical form
        "+00", "-00",
        "+00:00", "-00:00"
    };
    
    
    // -----------------------------------------------------------------------
    // Implements EntityResolver
    // -----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(
        String publicId, 
        String systemId
    ) throws SAXException {
        try {
            return this.getSchemaInputSource(systemId);
        } catch (ServiceException e) {
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "Entity resolution failed",
                new BasicException.Parameter("publicId", publicId),
                new BasicException.Parameter("systemId", systemId));
            return null;
        }
    }

    /**
     * Initialize qualifierNames, attributeTypes, attributeMultiplicities
     * 
     * @param schemaDocument
     * 
     * @throws ServiceException
     */
    private void initTypes(
        org.w3c.dom.Document schemaDocument
    ) throws ServiceException {
        org.w3c.dom.Element docElement = schemaDocument.getDocumentElement();
        org.w3c.dom.NodeList complexTypeNodes = docElement.getElementsByTagName("xsd:complexType");
        // iterate all xsd:complexType
        int complexTypeNodesLength = complexTypeNodes.getLength();
        for (
            int i = 0; 
            i < complexTypeNodesLength; 
            i++
        ) {
            org.w3c.dom.Node complexType = complexTypeNodes.item(i);
            org.w3c.dom.NamedNodeMap complexTypeAttributes = complexType.getAttributes();
            org.w3c.dom.Attr complexTypeName = (org.w3c.dom.Attr) complexTypeAttributes.getNamedItem("name");
            if (complexTypeName != null) {
                // get qualifierName of complex type
                org.w3c.dom.NodeList attributeNodes = ((org.w3c.dom.Element) complexType).getElementsByTagName("xsd:attribute");
                int attributeNodesLength = attributeNodes.getLength();
                for (
                    int j = 0; 
                    j < attributeNodesLength; 
                    j++
                ) {
                    org.w3c.dom.Node attributeNode = attributeNodes.item(j);
                    if ("_qualifier".equals(attributeNode.getAttributes().getNamedItem("name").getNodeValue())) {
                        ImportHandler.qualifierNames.put(complexTypeName
                            .getNodeValue(), attributeNode
                            .getAttributes()
                            .getNamedItem("fixed")
                            .getNodeValue());
                    }
                }
                // get qualifierTypes, qualifierMultiplicities
                org.w3c.dom.NodeList elementNodes =((org.w3c.dom.Element) complexType).getElementsByTagName("xsd:element");
                int elementNodesLength = elementNodes.getLength();
                for (
                    int j = 0; 
                    j < elementNodesLength; 
                    j++
                ) {
                    org.w3c.dom.Node element = elementNodes.item(j);
                    org.w3c.dom.NamedNodeMap elementAttributes = element.getAttributes();
                    org.w3c.dom.Attr attributeName = (org.w3c.dom.Attr) elementAttributes.getNamedItem("name");
                    org.w3c.dom.Attr attributeType = (org.w3c.dom.Attr) elementAttributes.getNamedItem("type");
                    // default multiplicity Multiplicities.SINGLE_VALUE
                    if (
                        attributeName != null &&
                        !"_content".equals(attributeName.getNodeValue()) &&
                        !"_object".equals(attributeName.getNodeValue()) &&
                        !"_item".equals(attributeName.getNodeValue()) &&
                        attributeName.getNodeValue().indexOf('.') < 0
                     ) {
                        String attributeTypeName = null;
                        // multi-valued attribute defined as complexType
                        if (attributeType == null) {
                            // find xsd:attribute name=""_multiplicity"" of
                            // complexType
                            org.w3c.dom.NodeList l0 = ((org.w3c.dom.Element) element).getElementsByTagName("xsd:complexType");
                            for (
                                int i0 = 0; 
                                i0 < l0.getLength(); 
                                i0++
                            ) {
                                org.w3c.dom.Node n0 = l0.item(i0);
                                org.w3c.dom.NodeList l1 = ((org.w3c.dom.Element) n0).getElementsByTagName("xsd:attribute");
                                for (
                                    int i1 = 0; 
                                    i1 < l1.getLength(); 
                                    i1++
                                ) {
                                    org.w3c.dom.Node n1 = l1.item(i1);
                                    if ("_multiplicity".equals(n1.getAttributes().getNamedItem("name").getNodeValue())) {
                                        ImportHandler.attributeMultiplicities.put(
                                            complexTypeName.getNodeValue() + ":" + attributeName.getNodeValue(),
                                            n1.getAttributes().getNamedItem("fixed").getNodeValue()
                                        );
                                    }
                                }
                            }
                            // xsd:extension base=
                            org.w3c.dom.Node extension = element;
                            while (
                                extension != null &&
                                !"xsd:extension".equals(extension.getNodeName())
                            ) {
                                org.w3c.dom.NodeList children = extension.getChildNodes();
                                extension = null;
                                for (
                                    int k = 0; 
                                    k < children.getLength(); 
                                    k++
                                ) {
                                    if (children.item(k).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                        extension = children.item(k);
                                        break;
                                    }
                                }
                            }
                            if (extension != null) {
                                org.w3c.dom.NamedNodeMap extensionAttributes = extension.getAttributes();
                                org.w3c.dom.Attr extensionBase = (org.w3c.dom.Attr) extensionAttributes.getNamedItem("base");
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
                            attributeTypeName = "org.w3c.string";
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
     * 
     * @param _uriSchema
     * @throws ServiceException
     */
    private void loadSchema(
        String uriSchema
    ) throws ServiceException {
        if(uriSchema.indexOf("/xmi/") > 0) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            "\"/xmi/\" directory no longer supported",
            new BasicException.Parameter("uri", uriSchema)
        );
        if (!ImportHandler.loadedSchemas.contains(uriSchema)) try {
            org.w3c.dom.Document schemaDocument = DocumentBuilderFactory.newInstance(
            ).newDocumentBuilder(
            ).parse(
                this.getSchemaInputSource(uriSchema)
            );
            this.initTypes(schemaDocument);
            ImportHandler.loadedSchemas.add(uriSchema);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                null
            ).log();
        } catch (org.xml.sax.SAXException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                null
            ).log();
        } catch (java.io.IOException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                null
            ).log();
        }
    }

    // ------------------------------------------------------------------------
    // Implements ContentHandler
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    @Override
    public void startDocument(
    ) throws SAXException {
        this.currentPath = new Path("");
        this.objectStack = new Stack();
        this.objectElements = new HashMap();
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
    ) throws SAXException {
        this.previousElementEnded = false;
        this.currentAttributeValue = null;
        // check whether to load schema
        for (
            int i = 0; 
            i < attributes.getLength(); 
            i++
        ) {
            if ("noNamespaceSchemaLocation".equals(attributes.getLocalName(i))) {
                try {
                    this.loadSchema(attributes.getValue(i));
                } catch (ServiceException e) {
                    throw new SAXException(e.log());
                }
            }
        }
        List element = parseElement(localpart);
        String qualifierName = (String) ImportHandler.qualifierNames.get(localpart);
        String attributeMultiplicity = (String) ImportHandler.attributeMultiplicities.get(
            this.currentLocalpartObject + ":" + localpart
        );
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
                Map attributeValues = new HashMap();
                attributeValues.put("_qualifier", qualifierName);
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attributeName = attributes.getLocalName(i);
                    if (!"".equals(attributeName)
                        && !"_qualifier".equals(attributeName)
                        && !"noNamespaceSchemaLocation".equals(attributeName)
                        && !"xsi".equals(attributeName)) {
                        attributeValues.put(attributeName, attributes
                            .getValue(i));
                    }
                }
                if (this.currentPath.size() < 4) {
                    // "null" fix for Authority|Provider
                    attributeValues.put("_operation", "null"); 
                } else  if (
                    attributeValues.size() < 3 ||
                    "".equals(attributeValues.get("_operation"))
                ) {
                    // "set" is the default operation
                    attributeValues.put("_operation", "set");
                }
                String qualifier = (String) attributeValues.get(qualifierName);
                if (qualifier == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "required attribute " + qualifierName + " not found.",
                        new BasicException.Parameter("localpart", localpart),
                        new BasicException.Parameter(
                            "attributes",
                            attributeValues));
                }
                this.objectElements.put(localpart, attributeValues);
                this.currentPath = this.currentPath.getChild(
                    "".equals(qualifier) ? ":" + this.nextTemporaryId-- : qualifier
                );
                try {
                    this.currentObject = Object_2Facade.newInstance(
                        this.currentPath,
                        this.toNameComponent(element)
                    ).getDelegate(
                    );
                    String operation = (String) attributeValues.get("_operation");
                    if (!"null".equals(operation)) {
                        Path objectId = Object_2Facade.getPath(this.currentObject);
                        // An object and its sub-objects are processed as a single
                        // unit of work
                        if (this.pendingAt == -1) {
                            this.pendingAt = objectId.size();
                        }
                        if("create".equals(operation)) {
                            this.currentObjectOperation = ImportMode.CREATE;
                        } else if ("set".equals(operation)) {
                            this.currentObjectOperation = ImportMode.SET;
                        } else if ("update".equals(operation)) {
                            this.currentObjectOperation = ImportMode.UPDATE;
                        } else if (
                            "operation".equals(operation) || 
                            "remove".equals(operation)
                        ) {
                            //
                            // unsupported request
                            //
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_SUPPORTED,
                                "No longer supported _operation argument",
                                new BasicException.Parameter("xri", objectId.toXRI()),
                                new BasicException.Parameter("unsupported","operation","remove"),
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
                                new BasicException.Parameter("xri", objectId.toXRI()),
                                new BasicException.Parameter("supported", "", "null", "set", "create", "update"),
                                new BasicException.Parameter("requested", operation)
                            );
                        }
                    }
                } catch (ResourceException exception) {
                    throw new ServiceException(exception);
                }
            } catch (ServiceException e) {
                throw new SAXException(e);
            }
        } else if ("_item".equals(localpart)) {
            //
            // <"_item"> start element of value of a multi-valued attribute
            //
            this.currentAttributeOperation = attributes.getValue("_operation");
            int position = attributes.getValue("_position") == null ? 0 : Integer.parseInt(
                attributes.getValue("_position")
            );
            this.currentAttributePosition = position == -1 ? this.currentAttributePosition + 1 : position;
        } else if (
            Multiplicities.SET.equals(attributeMultiplicity) ||
            Multiplicities.LIST.equals(attributeMultiplicity) ||
            Multiplicities.SPARSEARRAY.equals(attributeMultiplicity) ||
            Multiplicities.MULTI_VALUE.equals(attributeMultiplicity)
        ) {
            try {
                // multi-valued attribute with attributes 'offset', 'multiplicity' last
                // component of the element is the unqualified element name
                this.currentPath = this.currentPath.getChild(
                    (String) element.get(element.size() - 1)
                );
                this.currentAttributeName = localpart;
                this.currentAttributeOffset = attributes.getValue("_offset") == null ? 0 :  Integer.parseInt(
                    attributes.getValue("_offset")
                );
                this.currentAttributeMultiplicity = attributeMultiplicity;
                this.currentAttributePosition = -1;
                Object_2Facade.newInstance(
                    this.currentObject
                ).attributeValuesAsList(
                    this.currentAttributeName
                ).clear();
            } catch (ResourceException exception) {
                throw new SAXException(exception);
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
                this.currentPath = currentPath.getChild((String) element.get(element.size() - 1));
                this.currentAttributeName = localpart;
                this.currentAttributeOffset = 0;
                this.currentAttributePosition = -1;
            }
        }
    }

    /**
     * 
     */
    @Override
    public void characters(
        char[] ch, 
        int offset, 
        int length
    ) throws SAXException {
        if (this.currentAttributeValue == null) {
            this.currentAttributeValue = new StringBuilder();
        }
        this.currentAttributeValue.append(ch, offset, length);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(
        String uri, 
        String localName, 
        String qName
    ) throws SAXException {
        try {
            //
            // The end element corresponds to an Object. Process the
            // currentObject and get next from stack
            //
            if (
                "_object".equals(localName) || 
                this.objectElements.get(localName) != null
            ) {
                if (this.currentObject != null) {
                    if(this.currentObjectOperation != null) {
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
                if (
                    "_object".equals(localName) && 
                    (this.currentObject != null || !this.objectStack.isEmpty())
                ) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.ASSERTION_FAILURE,
                        "pending object found. There must be no pending objects in v3 schema format.",
                        new BasicException.Parameter("object",this.currentObject)
                    );
                }
            }
            // it is an end element of a Attribute|Reference|StructureType
            else if (this.currentObject != null) {
                //
                // The default object to store the values is currentObject.
                // However, in the case of StructureTypes the objects are nested and the 
                // target  object has to be set to correct nested object.
                //
                //   MappedRecord targetObject = this.currentObject;
                //   for(
                //     int i = this.currentObject.path().size() + 1;
                //     i < this.currentPath.size() - 1;
                //     i++
                //   ) {
                //     String attributeName = this.currentPath.get(i);
                //     if(targetObject.getValues(attributeName) == null) {
                //       targetObject.values(attributeName).add(
                //         targetObject = new DataproviderObject(this.currentPath.getPrefix(i+1))
                //        );
                //     }
                //   }
                //
                /**
                 * store attribute value the attribute value between two closing
                 * elements is a sequence of blanks. Skip this value
                 */
                if (!this.previousElementEnded) {
                    if (this.currentAttributeValue != null) {
                        Object_2Facade facade = Object_2Facade.newInstance(this.currentObject);
                        // attribute name
                        String attributeName = this.currentPath.getBase();
                        String qualifiedClassName = facade.getValue().getRecordName();
                        // attribute type
                        String attributeType = (String) ImportHandler.attributeTypes.get(qualifiedClassName + ":" + this.currentAttributeName);
                        if (attributeType == null) {
                            attributeType = "org.w3c.string";
                        }
                        java.lang.Object value = null;
                        if ("org.w3c.string".equals(attributeType) || "string".equals(attributeType)) {
                            value = this.currentAttributeValue.toString();
                        } else if ("org.w3c.short".equals(attributeType) || "short".equals(attributeType)) {
                            String v = this.currentAttributeValue.toString().trim();
                            if (v.length() > 0) {
                                value = Short.valueOf(v);
                            }
                        } else if ("org.w3c.long".equals(attributeType) || "long".equals(attributeType)) {
                            String v = this.currentAttributeValue.toString().trim();
                            if (v.length() > 0) {
                                value = Long.valueOf(v);
                            }
                        } else if ("org.w3c.integer".equals(attributeType) || "integer".equals(attributeType)) {
                            String v = this.currentAttributeValue.toString().trim();
                            if (v.length() > 0) {
                                value = Integer.valueOf(v);
                            }
                        } else if ("org.w3c.decimal".equals(attributeType) || "decimal".equals(attributeType)) {
                            String v = this.currentAttributeValue.toString().trim();
                            if (v.length() > 0) {
                                value = new BigDecimal(v);
                            }
                        } else if ("org.w3c.boolean".equals(attributeType) || "boolean".equals(attributeType)) {
                            String v = this.currentAttributeValue.toString().trim();
                            if (v.length() > 0) {
                                value = Boolean.valueOf(v);
                            }
                        } else if ("org.w3c.dateTime".equals(attributeType) || "dateTime".equals(attributeType)) {
                            String v = this.currentAttributeValue.toString().trim();
                            if (v.length() > 0) {
                                value = newDateTime(v);
                            }
                        } else if ("org.w3c.date".equals(attributeType) || "date".equals(attributeType)) {
                            String v = this.currentAttributeValue.toString().trim();
                            if (v.length() > 0) {
                                try {
                                    value = Datatypes.create(
                                        XMLGregorianCalendar.class,
                                        v
                                    );
                                } catch (IllegalArgumentException e) {
                                    throw new ServiceException(e);
                                }
                            }
                        } else if ("org.openmdx.base.ObjectId".equals(attributeType)) {
                            // SysLog.trace("adding string value to " +
                            // localpart);
                            value = new Path(
                                this.currentAttributeValue.toString().trim()
                            );
                        } else if ("org.w3c.binary".equals(attributeType)) {
                            // SysLog.trace("adding string value to " +
                            // localpart);
                            value = Base64.decode(
                                this.currentAttributeValue.toString()
                            );
                        } else {
                            // SysLog.info("unknown type of attribute [name="
                            // + qualifiedClassName + ":" +
                            // this.currentAttributeName + ";type=" +
                            // attributeType +
                            // "]. Assuming org:w3c:string");
                            ImportHandler.attributeTypes.put(
                                qualifiedClassName + ":" + this.currentAttributeName,
                                "org.w3c.string"
                            );
                            value = this.currentAttributeValue.toString();
                        }
                        //
                        // Set values according to multiplicity and
                        // operation TBD: replace SparseList by proper
                        // Collection class when migrating to Object_1_0
                        //
                        boolean notSupported = false;
                        int absolutePosition = this.currentAttributeOffset + this.currentAttributePosition;
                        if (Multiplicities.SET.equalsIgnoreCase(this.currentAttributeMultiplicity)) {
                            // SET
                            List values = facade.attributeValuesAsList(attributeName);
                            if (
                                this.currentAttributeOperation == null || 
                                "".equals(this.currentAttributeOperation) || 
                                "add".equals(this.currentAttributeOperation)
                             ) {
                                values.add(value);
                            } else if ("remove".equals(this.currentAttributeOperation)) {
                                values.remove(value);
                            } else {
                                notSupported = true;
                            }
                        } else if (
                            Multiplicities.LIST.equalsIgnoreCase(this.currentAttributeMultiplicity) || 
                            Multiplicities.MULTI_VALUE.equals(this.currentAttributeMultiplicity)
                        ) {
                            // LIST
                            List values = facade.attributeValuesAsList(attributeName);
                            if (
                                this.currentAttributeOperation == null || 
                                "".equals(this.currentAttributeOperation) || 
                                "add".equals(this.currentAttributeOperation)
                            ) {
                                values.add(value);
                            } else if ("remove".equals(this.currentAttributeOperation)) {
                                values.remove(value);
                            } else if ("set".equals(this.currentAttributeOperation)) {
                                values.set(absolutePosition, value);
                            } else {
                                notSupported = true;
                            }
                        } else if (
                            this.currentAttributeMultiplicity == null || 
                            Multiplicities.SPARSEARRAY.equalsIgnoreCase(this.currentAttributeMultiplicity)
                        ) {
                            // SPARSEARRAY
                            // In case of v2 format the multiplicity is not set
                            List values = facade.attributeValuesAsList(attributeName);
                            if (
                                this.currentAttributeOperation == null || 
                                "".equals(this.currentAttributeOperation) || 
                                "set".equals(this.currentAttributeOperation)
                            ) {
                                if (absolutePosition < 0) {
                                    values.add(value);
                                } else {
                                    values.set(absolutePosition, value);
                                }
                            } else if ("add".equals(this.currentAttributeOperation)) {
                                values.add(value);
                            } else if ("remove".equals(this.currentAttributeOperation)) {
                                values.remove(value);
                            } else {
                                notSupported = true;
                            }
                        } else {
                            // unsupported multiplicity
                            notSupported = true;
                        }
                        if (notSupported) {
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
                                )
                            );
                        }
                        this.currentAttributeOperation = null;
                    } else {
                        SysLog.detail("no value for " + localName);
                    }
                }
            }
            if (
                !"_item".equals(localName) && 
                !"_object".equals(localName) && 
                !"_content".equals(localName)
            ) {
                // </item> is the end tag for a value of a multi-valued attribute.
                // Leave currentPath unchanged.
                this.currentPath = this.currentPath.getParent();
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
        } catch (ResourceException exception) {
            throw Throwables.log(
                new SAXException(exception)
            );
        }
    }

    /**
     * timePoint is of the form 2001-09-29T15:45:21.798Z
     * 
     * @param v
     * 
     * @return the date/time value
     * 
     * @throws ServiceException 
     */
    private Date newDateTime(
        String v
    ) throws ServiceException{
        try {
            //
            // Handle UTC date/time value
            // 
            for(String utcId : UTC_IDS) {
                if(v.endsWith(utcId)) {
                    return Datatypes.create(Date.class, v);
                }
            }
            //
            // Handle non-UTC date/time value
            //
            int timePosition = v.indexOf('T');
            int timeZonePosition = v.lastIndexOf('+', timePosition);
            if(timeZonePosition < 0) {
                timeZonePosition = v.lastIndexOf('-', timePosition);
            }
            if (
                timeZonePosition > timePosition && 
                !v.regionMatches(true, timeZonePosition - 3, "GMT", 0, 3)
            ){
                v = v.substring(0, timeZonePosition) + "GMT" + v.substring(timeZonePosition);
            }
            return (
                v.indexOf('.', timePosition) < 0 ? localSecondFormat : localMillisecondFormat
            ).parse(
                v
            );
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
    
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
     */
    @Override
    public void warning(
        SAXParseException e
    ) throws SAXException {
        SysLog.warning(getLocationString(e), e.getMessage());
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(
        SAXParseException e
    ) throws SAXException {
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

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
     */
    @Override
    public void fatalError(
        SAXParseException e
    ) throws SAXException {
        throw new SAXException(new ServiceException(
            e,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.PROCESSING_FAILURE,
            "fatal XML parse error",
            new BasicException.Parameter("message", e.getMessage()),
            new BasicException.Parameter("location", getLocationString(e)))
        );
    }

    
    // ------------------------------------------------------------------------
    // XmlImporter
    // ------------------------------------------------------------------------

    /**
     * Parse elementName which is of the format <className>.<propertyName>.
     * Translate into an List of strings.
     */
    private List parseElement(String elementName) {
        ArrayList element = new ArrayList();
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
        List nameElements
    ) {
        if (nameElements.size() == 0) {
            return "";
        }
        StringBuilder nameComponent =
            new StringBuilder((String) nameElements.get(0));
        for (int i = 1; i < nameElements.size(); i++) {
            nameComponent.append(':').append(nameElements.get(i));
        }
        return nameComponent.toString();
    }

    /**
     * 
     */
    private InputSource getSchemaInputSource(
        String schemaUri
    ) throws ServiceException {
        URL schemaUrl = null;
        try {
            if (this.url != null) {
                if (
                    this.url.toString().startsWith("file:") && 
                    !this.url.toString().startsWith("file:/") && 
                    !this.url.toString().startsWith("file:./")
                ) {
                    schemaUrl = new URL(
                        new URL("file:./" + this.url.toString().substring(5)), 
                        schemaUri
                    );
                } else {
                    schemaUrl = new URL(this.url, schemaUri);
                }
            }
            if (
                schemaUrl != null && 
                schemaUrl.toString().startsWith("resource:")
            ) {
                schemaUrl = new URL(
                    "xri://+resource/" + schemaUri.substring(schemaUri.lastIndexOf("../") + 3)
                );
                SysLog.warning(
                    "Deprecated URL schema 'resource', use 'xri://+resource/...'!",
                    "Schema URI '" + schemaUri + "' transformed to URL '" + schemaUrl
                );
            } else if (schemaUri.startsWith("xri:+resource/")) {
                schemaUrl = new URL("xri://+resource/" + schemaUri.substring(14));
                SysLog.warning(
                    "Deprecated XRI 1 format 'xri:+resource', use 'xri://+resource/...'!",
                    "Schema URI '" + schemaUri + "' transformed to URL '" + schemaUrl
                );
            } else if (schemaUri.startsWith("xri://+resource/")) {
                schemaUrl = new URL(schemaUri);
            } else if (schemaUrl == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "Schema access failed. xsi:noNamespaceSchemaLocation must be an URL supported by openMDX, i.e. http:/, file:/, xri://+resource/, ...",
                    new BasicException.Parameter("schema URI", schemaUri)
                );
            }
            SysLog.detail("Document URL", this.url);
            SysLog.detail("Schema URI", schemaUri);
            SysLog.detail("Schema URL", schemaUrl);
            return new InputSource(schemaUrl.openStream());
        } catch (IOException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Schema access failed",
                new BasicException.Parameter("document", this.url),
                new BasicException.Parameter("schema",schemaUrl)
            );
        }
    }

}

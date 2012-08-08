/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XmlImporter.java,v 1.42 2009/01/13 02:09:32 wfro Exp $
 * Description: Generic XML Importer
 * Revision:    $Revision: 1.42 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:09:32 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.dataprovider.importer.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.AttributeSelectors;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.application.dataprovider.cci.RequestCollection;
import org.openmdx.application.dataprovider.cci.ServiceHeader;
import org.openmdx.application.mof.cci.Multiplicities;
import org.openmdx.application.mof.mapping.xmi.XMINames;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads the XML-source defined by 'uriSource', maps them to DataproviderObjects
 * and stores them in target. If objects are already present in target they are 
 * replaced, if they are new they are created.
 */
@SuppressWarnings("unchecked")
public class XmlImporter
extends DefaultHandler
implements ContentHandler, DTDHandler, ErrorHandler, DeclHandler, LexicalHandler, EntityResolver {

    //------------------------------------------------------------------------
    public XmlImporter(
        ServiceHeader header,
        Dataprovider_1_0 target
    ) throws ServiceException {
        this(
            header,
            target,
            false,
            true
        );
    }

    //------------------------------------------------------------------------
    /**
     * Creates an XmlImporter which stores the imported objects to the target 
     * provider. In case splitUnitsOfWork==true the first object with a non
     * null operation begins a unit of work. All child objects are member of 
     * the same unit of work. The unit of work is committed when the next 
     * object with the same path size is reached. If splitUnitsOfWork=false
     * the import is performed within one unit of work.
     */
    public XmlImporter(
        ServiceHeader header,
        Dataprovider_1_0 target,
        boolean transactional,
        boolean splitUnitsOfWork
    ) throws ServiceException {
        this(
            new RequestCollection(header, target),
            new RequestCollection(header, target),
            transactional,
            splitUnitsOfWork
        );
    }

    //------------------------------------------------------------------------
    /**
     * Creates an XmlImporter which stores the imported objects to the target 
     * provider. 
     * <p>
     * Allow the "set" operation being controlled by the objects' modification 
     * dates.
     * @deprecated Use {@link #XmlImporter(ServiceHeader,Dataprovider_1_0,Date,boolean,boolean)} instead
     */
    public XmlImporter(
        ServiceHeader header,
        Dataprovider_1_0 target,
        Date modifiedSince, 
        boolean transactional
    ) throws ServiceException {
        this(
            header, 
            target, 
            modifiedSince, 
            transactional, 
            false // lenient
        );
    }

//  ------------------------------------------------------------------------
    /**
     * Creates an XmlImporter which stores the imported objects to the target 
     * provider. 
     * <p>
     * Allow the "set" operation being controlled by the objects' modification 
     * dates.
     */
    public XmlImporter(
        ServiceHeader header,
        Dataprovider_1_0 target,
        Date modifiedSince, 
        boolean transactional, 
        boolean lenient,
        boolean createInitialAndOtherStatesInSeparateUnitsOfWork
    ) throws ServiceException {
        this(
            new RequestCollection(header, target, lenient),
            new RequestCollection(header, target, lenient),
            transactional, 
            true // splitUnitsOfWork
        );
        setGlobalModifiedSince(
            modifiedSince
        );
        setGlobalCreateInitialAndOtherStatesInSeparateUnitsOfWork(
            createInitialAndOtherStatesInSeparateUnitsOfWork
        );
    }

    /**
     * Creates an XmlImporter which stores the imported objects to the target 
     * provider. 
     * <p>
     * Allow the "set" operation being controlled by the objects' modification 
     * dates.
     */
    public XmlImporter(
        ServiceHeader header,
        Dataprovider_1_0 target,
        Date modifiedSince, 
        boolean transactional, 
        boolean lenient
    ) throws ServiceException {
        this(
            header,
            target,
            modifiedSince,
            transactional,
            lenient,
            true // createInitialAndOtherStatesInSeparateUnitsOfWork
        );
    }

    //------------------------------------------------------------------------
    public XmlImporter(
        RequestCollection target,
        RequestCollection reader,
        boolean transactional,
        boolean splitUnitsOfWork
    ) throws ServiceException {
        this.transactional = transactional;
        this.splitUnitsOfWork = splitUnitsOfWork;
        this.target = target;
        this.reader = reader;
        this.schemaValidation = false;
    }

    //------------------------------------------------------------------------
    public XmlImporter(
        Map target,
        boolean schemaValidation
    ) throws ServiceException {
        this.transactional = false;
        this.splitUnitsOfWork = true;
        this.target = target;
        this.schemaValidation = schemaValidation;
    }

    //------------------------------------------------------------------------
    /**
     * Define the validFrom date which will be applied to each object.
     * <p>
     * NOTE: all objects for which the "_operation" attribute is not "operation"
     * must be stated to support the setting of object_validFrom.
     * <p>
     * Set to null to avoid further setting of the validFrom date.
     * 
     * @param validFrom
     */
    public void setGlobalValidFrom(
        Date validFrom
    ) {
        if (validFrom != null) {
            globalValidFrom = DateFormat.getInstance().format(validFrom);
        }
        else {
            globalValidFrom = null;
        }
    }

    //------------------------------------------------------------------------
    /** 
     * Get the currently set validFrom date, if any.
     */
    public Date getGlobalValidFrom() {
        if (globalValidFrom != null) {
            try {
                return millisecondFormat.parse(globalValidFrom);
            }
            catch(ParseException pe) {
                globalValidFrom = null;
                return null;
            }
        }
        else {
            return null;
        }
    }

    //------------------------------------------------------------------------
    /**
     * Define the validTo date which will be applied to each object.
     * <p>
     * NOTE: all objects for which the "_operation" attribute is not "operation"
     * must be stated to support the setting of object_validTo.
     * <p>
     * Set to null to avoid further setting of the validTo date.
     * @param validTo
     */
    public void setGlobalValidTo(
        Date validTo
    ) {
        if (validTo != null) {
            globalValidTo = DateFormat.getInstance().format(validTo);
        }
        else {
            globalValidTo = null;
        }  
    }

    //------------------------------------------------------------------------
    /**
     * Allow the "set" operation being controlled by the objects' modification 
     * dates.
     * <p>
     * @param modifiedSince the "set" operation is controlled by the objects' 
     * modification dates if this value is not <code>null</code>
     */
    public void setGlobalModifiedSince(
        Date modifiedSince
    ) {
        this.globalModifiedSince = modifiedSince == null ? 
            null :
                DateFormat.getInstance().format(modifiedSince);
    }

    //------------------------------------------------------------------------
    /** 
     * Get the currently set validFrom date, if any.
     */
    public Date getGlobalValidTo() {
        if (globalValidTo != null) {
            try {
                return millisecondFormat.parse(globalValidTo);
            }
            catch(ParseException pe) {
                globalValidTo = null;
                return null;
            }
        }
        else {
            return null;
        }
    }

    //------------------------------------------------------------------------

    /**
     * Retrieve globalCreateInitialAndOtherStatesInSeparateUnitsOfWork.
     *
     * @return Returns the globalCreateInitialAndOtherStatesInSeparateUnitsOfWork.
     */
    public boolean isGlobalCreateInitialAndOtherStatesInSeparateUnitsOfWork(
    ) {
        return this.globalCreateInitialAndOtherStatesInSeparateUnitsOfWork;
    }


    /**
     * Set globalCreateInitialAndOtherStatesInSeparateUnitsOfWork.
     * 
     * @param globalCreateInitialAndOtherStatesInSeparateUnitsOfWork The globalCreateInitialAndOtherStatesInSeparateUnitsOfWork to set.
     */
    public void setGlobalCreateInitialAndOtherStatesInSeparateUnitsOfWork(
        boolean globalCreateInitialAndOtherStatesInSeparateUnitsOfWork
    ) {
        this.globalCreateInitialAndOtherStatesInSeparateUnitsOfWork = globalCreateInitialAndOtherStatesInSeparateUnitsOfWork;
    }


    //------------------------------------------------------------------------
    // DOMEntityResolver
    //------------------------------------------------------------------------
    public InputSource resolveEntity(
        String publicId,
        String systemId
    ) throws 
    SAXException {
        try {
            return this.getSchemaInputSource(systemId);
        }
        catch (ServiceException e) {
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

    //------------------------------------------------------------------------
    /**
     * Initialize qualifierNames, attributeTypes, attributeMultiplicities, 
     */
    private void initTypes(
        org.w3c.dom.Document schemaDocument
    ) throws ServiceException {

        org.w3c.dom.Element docElement = schemaDocument.getDocumentElement();
        org.w3c.dom.NodeList complexTypeNodes = docElement.getElementsByTagName("xsd:complexType");

        // iterate all xsd:complexType
        int complexTypeNodesLength = complexTypeNodes.getLength(); 
        for(
                int i = 0; 
                i < complexTypeNodesLength;  
                i++
        ) {
            org.w3c.dom.Node complexType = complexTypeNodes.item(i);
            org.w3c.dom.NamedNodeMap complexTypeAttributes = complexType.getAttributes();
            org.w3c.dom.Attr complexTypeName = (org.w3c.dom.Attr)complexTypeAttributes.getNamedItem("name");

            if(complexTypeName != null) {

                // get qualifierName of complex type      
                org.w3c.dom.NodeList attributeNodes = ((org.w3c.dom.Element)complexType).getElementsByTagName("xsd:attribute");
                int attributeNodesLength = attributeNodes.getLength();
                for(
                        int j = 0;
                        j < attributeNodesLength;
                        j++
                ) {
                    org.w3c.dom.Node attributeNode = attributeNodes.item(j);
                    if("_qualifier".equals(attributeNode.getAttributes().getNamedItem("name").getNodeValue())) {
                        XmlImporter.qualifierNames.put(
                            complexTypeName.getNodeValue(),
                            attributeNode.getAttributes().getNamedItem("fixed").getNodeValue()
                        );
                    }
                }

                // get qualifierTypes, qualifierMultiplicities
                org.w3c.dom.NodeList elementNodes = ((org.w3c.dom.Element)complexType).getElementsByTagName("xsd:element");
                int elementNodesLength = elementNodes.getLength(); 
                for(
                        int j = 0; 
                        j < elementNodesLength;  
                        j++
                ) {
                    org.w3c.dom.Node element = elementNodes.item(j);
                    org.w3c.dom.NamedNodeMap elementAttributes = element.getAttributes();
                    org.w3c.dom.Attr attributeName = (org.w3c.dom.Attr)elementAttributes.getNamedItem("name");
                    org.w3c.dom.Attr attributeType = (org.w3c.dom.Attr)elementAttributes.getNamedItem("type");

                    // default multiplicity Multiplicities.SINGLE_VALUE
                    if(
                            (attributeName != null) &&
                            !"_content".equals(attributeName.getNodeValue()) &&
                            !"_object".equals(attributeName.getNodeValue()) &&
                            !"_item".equals(attributeName.getNodeValue()) &&
                            (attributeName.getNodeValue().indexOf('.') < 0)
                    ) {
                        String attributeTypeName = null;

                        // multi-valued attribute defined as complexType
                        if(attributeType == null) {

                            // find xsd:attribute name=""_multiplicity"" of complexType
                            org.w3c.dom.NodeList l0 = ((org.w3c.dom.Element)element).getElementsByTagName("xsd:complexType");
                            for(int i0 = 0; i0 < l0.getLength(); i0++) {
                                org.w3c.dom.Node n0 = l0.item(i0);                
                                org.w3c.dom.NodeList l1 = ((org.w3c.dom.Element)n0).getElementsByTagName("xsd:attribute");
                                for(int i1 = 0; i1 < l1.getLength(); i1++) {
                                    org.w3c.dom.Node n1 = l1.item(i1);
                                    if("_multiplicity".equals(n1.getAttributes().getNamedItem("name").getNodeValue())) {
                                        XmlImporter.attributeMultiplicities.put(
                                            complexTypeName.getNodeValue() + ":" + attributeName.getNodeValue(),
                                            n1.getAttributes().getNamedItem("fixed").getNodeValue()
                                        );
                                    }             
                                }
                            }

                            // xsd:extension base=
                            org.w3c.dom.Node extension = element;
                            while(
                                    (extension != null) && 
                                    !"xsd:extension".equals(extension.getNodeName())
                            ) {
                                org.w3c.dom.NodeList children = extension.getChildNodes();
                                extension = null;
                                for(int k = 0; k < children.getLength(); k++) {
                                    if(children.item(k).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                        extension = children.item(k);
                                        break;
                                    }
                                }
                            }
                            if(extension != null) {
                                org.w3c.dom.NamedNodeMap extensionAttributes = extension.getAttributes();
                                org.w3c.dom.Attr extensionBase = (org.w3c.dom.Attr)extensionAttributes.getNamedItem("base");
                                if(extensionBase != null) {
                                    attributeTypeName = extensionBase.getValue();
                                }
                            }
                        }

                        /**
                         * A simple type is defined as:
                         * <pre>
                         * <xsd:element name="org.omg.model1.Element.annotation" type="org.w3c.string" minOccurs="0"/>
                         * </pre>
                         */
                        else {
                            attributeTypeName = attributeType.getValue();
                        }
                        if(attributeTypeName == null) {
                            //SysLog.warning("type for attribute " + attributeName + " not defined. Assuming org:w3c:string");
                            attributeTypeName = "org.w3c.string";
                        }
                        XmlImporter.attributeTypes.put(
                            complexTypeName.getNodeValue().replace('.',':') + ":" + attributeName.getNodeValue(),
                            attributeTypeName
                        );
                    }
                }
            } 
        }
    }

    //------------------------------------------------------------------------
    private void loadSchema(
        String _uriSchema
    ) throws ServiceException {
        String uriSchema = _uriSchema;
        if(XMINames.XMI_PACKAGE_SUFFIX.length() == 4) {
            int i = uriSchema.indexOf("/xmi/");
            if(i > 0) {
                SysLog.info(
                    "Deprecated '/xmi/' will be replaced by '" + XMINames.XMI_PACKAGE_SUFFIX + "'", 
                    uriSchema
                );
                uriSchema = uriSchema.substring(0, i + 1) + XMINames.XMI_PACKAGE_SUFFIX + uriSchema.substring(i + 4);
            }
        }
        try {
            if(!XmlImporter.loadedSchemas.contains(uriSchema)) {
                org.w3c.dom.Document schemaDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    this.getSchemaInputSource(uriSchema)
                );
                this.initTypes(schemaDocument);
                XmlImporter.loadedSchemas.add(uriSchema);
            }
        }
        catch(javax.xml.parsers.ParserConfigurationException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                ""
            ).log();
        }
        catch(org.xml.sax.SAXException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                ""
            ).log();
        }
        catch(java.io.IOException ex) {
            throw new ServiceException(
                ex,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                ""
            ).log();
        }
    }

    //------------------------------------------------------------------------
    // ContentHandler
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    public void setDocumentLocator(
        Locator locator
    ) {
        //
    }

    //------------------------------------------------------------------------
    public void startDocument() 
    throws SAXException {
        this.currentPath = new Path("");
        this.objectStack = new Stack();
        this.objectElements = new HashMap();
        this.currentObject = null;
        this.currentAttributeValue = null;
        this.previousElementEnded = true;
        try {
            // import all objects in same unit of work
            if(!this.splitUnitsOfWork && this.target instanceof RequestCollection) {
                ((RequestCollection)this.target).beginUnitOfWork(
                    this.transactional
                );
            }
        }
        catch(ServiceException e) {
            throw new SAXException(e);
        }
    }

    //------------------------------------------------------------------------
    /**
     * Start an XML element. Start and end look like:
     * <element>
     *   ... embedded elements
     * </element>
     */
    public void startElement(
        String uri, 
        String localpart, 
        String rawname, 
        Attributes attributes
    ) throws SAXException {

//      this.currentLocalpart = localpart;
        this.previousElementEnded = false;
        this.currentAttributeValue = new StringBuilder();

        //SysLog.trace("<uri=" + uri + ", localpart=" + localpart + ", rawname=" + rawname + ">");
        // check whether to load schema
        for(
                int i = 0; 
                i < attributes.getLength(); 
                i++
        ) {
            //SysLog.trace("  " + attributes.getLocalName(i) + "=" + attributes.getValue(i));
            if("noNamespaceSchemaLocation".equals(attributes.getLocalName(i))) {
                try {
                    this.loadSchema(attributes.getValue(i));
                }
                catch(ServiceException e) {
                    throw new SAXException(e.log());
                }
            }
        }

        List element = parseElement(localpart);
        String qualifierName = (String)XmlImporter.qualifierNames.get(localpart);
        String attributeMultiplicity = (String)XmlImporter.attributeMultiplicities.get(
            this.currentLocalpartObject + ":" + localpart
        );

        /**
         * element has a qualifier --> new object
         */
        if(qualifierName != null) {

            try {

                this.currentLocalpartObject = localpart;
                this.objectStack.push(
                    this.currentObject
                );

                /**
                 * Remember attribute values of element. These are:
                 * - localpart
                 * - <qualifier>
                 * - "_qualifier" (fixed)
                 * - "_operation" (optional)
                 */
                Map attributeValues = new HashMap();
                attributeValues.put(
                    "_qualifier",
                    qualifierName
                );
                for(
                        int i = 0;
                        i < attributes.getLength();
                        i++
                ) {
                    String attributeName = attributes.getLocalName(i);
                    if(
                            !"".equals(attributeName) &&
                            !"_qualifier".equals(attributeName) &&
                            !"noNamespaceSchemaLocation".equals(attributeName) &&
                            !"xsi".equals(attributeName)
                    ) {
                        attributeValues.put(
                            attributeName,
                            attributes.getValue(i)
                        );
                    }
                }

                // no operation specified -> add "" operation
                if(attributeValues.size() < 3) {
                    attributeValues.put(
                        "_operation",
                        ""
                    );
                }
                if(this.currentPath.size() < 4) {
                    attributeValues.put(
                        "_operation",
                        "null"
                    ); // "null" fix for Authority|Provider
                } 
                else if("".equals(attributeValues.get("_operation"))) {
                    attributeValues.put(
                        "_operation", 
                        "set"
                    ); // "set" default for others
                }

                String qualifier = (String)attributeValues.get(qualifierName);
                if(qualifier == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.ASSERTION_FAILURE,
                        "required attribute " + qualifierName + " not found.",
                        new BasicException.Parameter("localpart", localpart),
                        new BasicException.Parameter("attributes", attributeValues)
                    );        
                }

                this.objectElements.put(
                    localpart,
                    attributeValues
                );

                this.currentPath = this.currentPath.getChild(
                    "".equals(qualifier) 
                    ? ":" + this.nextTemporaryId-- 
                        : qualifier
                );
                SysLog.trace("new object", this.currentPath);

                this.currentObject = new DataproviderObject(
                    this.currentPath
                );

                // the dot-separated element name is equivalent to the qualfiedName class name
                this.currentObject.values(SystemAttributes.OBJECT_CLASS).add(
                    this.toNameComponent(element)
                );        
                this.beginObject(
                    this.currentObject,
                    (String)attributeValues.get("_operation")
                );

            }
            catch(ServiceException e) {
                throw new SAXException(e);
            }
        }

        /**
         * <"_item"> start element of value of a multi-valued attribute
         */    
        else if("_item".equals(localpart)) {
            this.currentAttributeOperation = attributes.getValue("_operation");
            int position = attributes.getValue("_position") == null
            ? 0
                : new Integer(attributes.getValue("_position")).intValue();
            this.currentAttributePosition = position == -1
            ? this.currentAttributePosition + 1
                : position;
        }

        /**
         * multi-valued attribute with attributes 'offset', 'multiplicity'
         * last component of the element is the unqualified element name
         */    
        else if(
                Multiplicities.SET.equals(attributeMultiplicity) ||
                Multiplicities.LIST.equals(attributeMultiplicity) ||
                Multiplicities.SPARSEARRAY.equals(attributeMultiplicity) ||
                Multiplicities.MULTI_VALUE.equals(attributeMultiplicity)
        ) {
            this.currentPath = this.currentPath.getChild(
                (String)element.get(element.size() - 1)
            );
            this.currentAttributeName = localpart;
            this.currentAttributeOffset = attributes.getValue("_offset") == null
            ? 0
                : new Integer(attributes.getValue("_offset")).intValue();
            this.currentAttributeMultiplicity = attributeMultiplicity;
            this.currentAttributePosition = -1;
        }

        /**
         * no attribute --> no qualifier -> element is reference|attribute|struct
         * last component of the element is the unqualified element name
         */
        else {
            if("_object".equals(localpart)) {
                this.currentAttributeName = null;
            }
            else if("_content".equals(localpart)) {
                this.currentObject = null;
            }
            else {
                this.currentPath = currentPath.getChild(
                    (String)element.get(element.size()-1)
                );
                this.currentAttributeName = localpart;
                this.currentAttributeOffset = 0;
                this.currentAttributePosition = -1;
            }
        }

    }

    //---------------------------------------------------------------------------
    public void characters(
        char[] ch, 
        int offset, 
        int length
    ) throws SAXException {

        if(this.currentAttributeValue == null) {
            this.currentAttributeValue = new StringBuilder();
        }
        this.currentAttributeValue.append(
            ch, 
            offset, 
            length
        );
    }

    //------------------------------------------------------------------------
    public void ignorableWhitespace(
        char[] ch, 
        int offset, 
        int length
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void endElement(
        String uri, 
        String localpart, 
        String rawname
    ) throws SAXException {

        try {
            /**    
             * The end element corresponds to an Object. Process the currentObject
             * and get next from stack
             */
            if(
                    "_object".equals(localpart) ||
                    (this.objectElements.get(localpart) != null)
            ) {
                try {
                    Map attributeValues = "_object".equals(localpart)
                    ? (Map)this.objectElements.get(this.currentLocalpartObject)
                        : (Map)this.objectElements.get(localpart);

                    /**
                     * If object was already closed by <"_object"> it can not be
                     * closed again by the object end tag. Skip endObject() in this case 
                     */
                    if(this.currentObject != null) {
                        this.endObject(
                            this.currentObject,
                            (String)attributeValues.get("_operation")
                        );
                        this.currentObject = (DataproviderObject)this.objectStack.pop();
                    }
                }
                catch(ServiceException e) {
                    throw new SAXException(e);
                }

                this.currentLocalpartObject = null;

                /**
                 * In v3 format there are never pending objects.
                 */
                if(
                        "_object".equals(localpart) && 
                        ((this.currentObject != null) || (this.objectStack.size() > 0))
                ) { 
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.ASSERTION_FAILURE,
                        "pending object found. There must be no pending objects in v3 schema format.",
                        new BasicException.Parameter("object", this.currentObject)
                    );
                }
            }

            // it is an end element of a Attribute|Reference|StructureType    
            else if(this.currentObject != null) {

                // The default object to store the values is currentObject. However,
                // in the case of StructureTypes the objects are nested and the target
                // object has to be set to correct nested object.
                DataproviderObject targetObject = this.currentObject;
                for(
                        int i = this.currentObject.path().size() + 1;
                        i < this.currentPath.size() - 1;
                        i++
                ) {
                    String attributeName = this.currentPath.get(i);
                    if(targetObject.getValues(attributeName) == null) {
                        targetObject.values(attributeName).add(
                            targetObject = new DataproviderObject(this.currentPath.getPrefix(i+1))
                        );
                    }
                }

                /**
                 * store attribute value
                 * the attribute value between two closing elements is a sequence of blanks. Skip this value
                 */
                if(!this.previousElementEnded) {
                    if(this.currentAttributeValue != null) { 
                        //SysLog.trace("attribute value complete " + currentPath + "; value=" + this.currentAttributeValue);

                        // attribute name
                        String attributeName = currentPath.get(currentPath.size()-1);
                        String qualifiedClassName = (String)this.currentObject.values(SystemAttributes.OBJECT_CLASS).get(0);

                        // attribute type
                        String attributeType = (String)XmlImporter.attributeTypes.get(
                            qualifiedClassName + ":" + this.currentAttributeName 
                        );
                        if(attributeType == null) {
                            attributeType = "org.w3c.string";
                        }

                        if(SystemAttributes.MODIFIED_AT.equals(attributeName)) {
                            if(
                                    this.globalModifiedSince != null && (
                                            "org.w3c.dateTime".equals(attributeType) || 
                                            "dateTime".equals(attributeType)
                                    )
                            ){
                                // SysLog.trace("adding boolean value to " + localpart);
                                // timePoint is of the form 2001-09-29T15:45:21.798Z 
                                String v = this.currentAttributeValue.toString().trim();

                                // Convert time zone from ISO 8601 to SimpleDateFormat 
                                int timePosition = v.indexOf('T');
                                if(v.endsWith("Z")){ 
                                    //
                                    // SimpleDateFormat can't handle 'Z' time zone designator
                                    //
                                    v=v.substring(0,v.length()-1)+"GMT+00:00";
                                } else {
                                    int timeZonePosition = v.lastIndexOf('-');
                                    if(timeZonePosition < timePosition) timeZonePosition = v.lastIndexOf('+');
                                    if(
                                            timeZonePosition > timePosition &&
                                            ! v.regionMatches(true, timeZonePosition-3, "GMT", 0, 3)
                                    ) v=v.substring(
                                        0, 
                                        timeZonePosition
                                    ) + "GMT" + v.substring(
                                        timeZonePosition
                                    );
                                }        
                                int timeLength = v.length() - timePosition - 1;
                                try {
                                    this.currentObject.setDigest(
                                        DateFormat.getInstance().format(
                                            v.indexOf('.', timePosition) == -1 ? 
                                                (timeLength == 8 ? localSecondFormat.parse(v) : secondFormat.parse(v)) : 
                                                    (timeLength == 12 ? localMillisecondFormat.parse(v) : millisecondFormat.parse(v))
                                        ).getBytes(
                                            PRIVATE_DIGEST_ENCODING
                                        )
                                    );
                                } catch (ParseException exception) {
                                    throw new ServiceException(exception);
                                } catch (UnsupportedEncodingException exception) {
                                    throw new ServiceException(exception);
                                }
                            }
                        } else if(
                                //
                                // do not import system attributes
                                //
                                !SystemAttributes.OBJECT_CLASS.equals(attributeName) &&
                                !SystemAttributes.CREATED_AT.equals(attributeName) &&
                                !SystemAttributes.CREATED_BY.equals(attributeName) &&
                                !SystemAttributes.MODIFIED_BY.equals(attributeName)
                        ) {

                            java.lang.Object value = null;

                            if("org.w3c.string".equals(attributeType) || "string".equals(attributeType)) {
                                // SysLog.trace("adding string value to " + localpart);
                                value = this.currentAttributeValue.toString();
                            }
                            else if("org.w3c.short".equals(attributeType) || "short".equals(attributeType)) {
                                // SysLog.trace("adding numeric value to " + localpart);
                                value = new Short(this.currentAttributeValue.toString().trim());
                            }
                            else if("org.w3c.long".equals(attributeType) || "long".equals(attributeType)) {
                                // SysLog.trace("adding numeric value to " + localpart);
                                value = new Long(this.currentAttributeValue.toString().trim());
                            }
                            else if("org.w3c.integer".equals(attributeType) || "integer".equals(attributeType)) {
                                // SysLog.trace("adding numeric value to " + localpart);
                                value = new Integer(this.currentAttributeValue.toString().trim());
                            }
                            else if("org.w3c.decimal".equals(attributeType) || "decimal".equals(attributeType)) {
                                // SysLog.trace("adding numeric value to " + localpart);
                                value = new BigDecimal(this.currentAttributeValue.toString().trim());
                            }
                            else if("org.w3c.boolean".equals(attributeType) || "boolean".equals(attributeType)) {
                                // SysLog.trace("adding boolean value to " + localpart);
                                value = new Boolean(this.currentAttributeValue.toString().trim());
                            }
                            else if("org.w3c.dateTime".equals(attributeType) || "dateTime".equals(attributeType)) {
                                // SysLog.trace("adding boolean value to " + localpart);
                                // timePoint is of the form 2001-09-29T15:45:21.798Z 
                                String v = this.currentAttributeValue.toString().trim();

                                // Convert time zone from ISO 8601 to SimpleDateFormat 
                                int timePosition = v.indexOf('T');
                                if(v.endsWith("Z")){ 
                                    //
                                    // SimpleDateFormat can't handle 'Z' time zone designator
                                    //
                                    v=v.substring(0,v.length()-1)+"GMT+00:00";
                                } else {
                                    int timeZonePosition = v.lastIndexOf('-');
                                    if(timeZonePosition < timePosition) timeZonePosition = v.lastIndexOf('+');
                                    if(
                                            timeZonePosition > timePosition &&
                                            ! v.regionMatches(true, timeZonePosition-3, "GMT", 0, 3)
                                    ) v=v.substring(
                                        0, 
                                        timeZonePosition
                                    ) + "GMT" + v.substring(
                                        timeZonePosition
                                    );
                                }        
                                int timeLength = v.length() - timePosition - 1;
                                try {
                                    value = DateFormat.getInstance().format(
                                        v.indexOf('.', timePosition) == -1 ? 
                                            (timeLength == 8 ? localSecondFormat.parse(v) : secondFormat.parse(v)) : 
                                                (timeLength == 12 ? localMillisecondFormat.parse(v) : millisecondFormat.parse(v))
                                    );
                                } catch (ParseException e) {
                                    throw new ServiceException(e);
                                }
                            }
                            else if (("org.w3c.date").equals(attributeType) || "date".equals(attributeType)) {
                                String v = this.currentAttributeValue.toString().trim();

                                try {
                                    value = DateFormat.getInstance().format(dateFormat.parse(v)).substring(0, 8);
                                } catch (ParseException e) {
                                    throw new ServiceException(e);
                                }

                            }
                            else if(("org.openmdx.base.ObjectId").equals(attributeType)) {
                                // SysLog.trace("adding string value to " + localpart);
                                value = new Path(this.currentAttributeValue.toString().trim());
                            }
                            else if(("org.w3c.binary").equals(attributeType)) {
                                // SysLog.trace("adding string value to " + localpart);
                                value = Base64.decode(this.currentAttributeValue.toString());
                            }              
                            else {
                                //SysLog.info("unknown type of attribute [name=" + qualifiedClassName + ":" + this.currentAttributeName + ";type=" + attributeType + "]. Assuming org:w3c:string");
                                XmlImporter.attributeTypes.put(
                                    qualifiedClassName + ":" + this.currentAttributeName,
                                    "org.w3c.string"
                                );
                                value = this.currentAttributeValue.toString();
                            }

                            /**
                             * Set values according to multiplicity and operation
                             * TBD: replace SparseList by proper Collection class when migrating
                             * to Object_1_0
                             */
                            boolean notSupported = false;
                            int absolutePosition = this.currentAttributeOffset + this.currentAttributePosition;

                            // SET
                            if(Multiplicities.SET.equalsIgnoreCase(this.currentAttributeMultiplicity)) {
                                SparseList values = this.currentObject.values(attributeName);
                                if(
                                        (this.currentAttributeOperation == null) ||
                                        "".equals(this.currentAttributeOperation) || 
                                        "add".equals(this.currentAttributeOperation)
                                ) {
                                    values.add(value);
                                }
                                else if("remove".equals(this.currentAttributeOperation)) {
                                    values.remove(value);
                                }
                                else {
                                    notSupported = true;
                                }
                            }

                            // LIST
                            else if(
                                    Multiplicities.LIST.equalsIgnoreCase(this.currentAttributeMultiplicity) ||
                                    Multiplicities.MULTI_VALUE.equals(this.currentAttributeMultiplicity)
                            ) {
                                List values = this.currentObject.values(attributeName);
                                if(
                                        (this.currentAttributeOperation == null) ||
                                        "".equals(this.currentAttributeOperation) || 
                                        "add".equals(this.currentAttributeOperation)
                                ) {
                                    values.add(value);
                                }
                                else if("remove".equals(this.currentAttributeOperation)) {
                                    values.remove(value);
                                }
                                else if("set".equals(this.currentAttributeOperation)) {
                                    values.set(
                                        absolutePosition, 
                                        value
                                    );
                                }
                                else {
                                    notSupported = true;
                                }
                            }

                            /**
                             * SPARSEARRAY
                             * In case of v2 format the multiplicity is not set
                             */
                            else if(
                                    (this.currentAttributeMultiplicity == null) ||
                                    Multiplicities.SPARSEARRAY.equalsIgnoreCase(this.currentAttributeMultiplicity)
                            ) {                
                                SparseList values = this.currentObject.values(attributeName);
                                if(
                                        (this.currentAttributeOperation == null) ||
                                        "".equals(this.currentAttributeOperation) ||
                                        "set".equals(this.currentAttributeOperation)
                                ) {
                                    if(absolutePosition < 0) {
                                        values.add(value);
                                    }
                                    else {
                                        values.set(
                                            absolutePosition, 
                                            value
                                        );
                                    }
                                }                
                                else if("add".equals(this.currentAttributeOperation)) {
                                    values.add(value);
                                }
                                else if("remove".equals(this.currentAttributeOperation)) {
                                    values.remove(value);
                                }
                                else {
                                    notSupported = true;
                                }
                            }

                            // unsupported multiplicity
                            else {
                                notSupported = true;
                            }

                            if(notSupported) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN, 
                                    BasicException.Code.NOT_SUPPORTED,
                                    "attribute operation for this multiplicity not supported.",
                                    new BasicException.Parameter("object", this.currentObject),
                                    new BasicException.Parameter("attribute", this.currentAttributeName),
                                    new BasicException.Parameter("operation", this.currentAttributeOperation),
                                    new BasicException.Parameter("multiplicity", this.currentAttributeMultiplicity)
                                );              
                            }
                            this.currentAttributeOperation = null;
                        }
                    }
                    else {
                        SysLog.warning("no value for " + localpart);
                    }
                }
            }

            /**
             * </item> is the end tag for a value of a multi-valued attribute.
             * Leave currentPath unchanged.
             */
            if(
                    !"_item".equals(localpart) &&
                    !"_object".equals(localpart) &&
                    !"_content".equals(localpart)
            ) {
                this.currentPath = this.currentPath.getParent();
            }
            this.currentAttributeValue = null;
            this.previousElementEnded = true;
        }
        catch(ServiceException e) {
            throw new SAXException(e.log());
        }
    }

    //------------------------------------------------------------------------
    public void endPrefixMapping(
        String prefix
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void skippedEntity(
        String name
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void endDocument(
    ) throws SAXException {
        try {
            // import all objects in same unit of work
            if(!this.splitUnitsOfWork && this.target instanceof RequestCollection) {
                ((RequestCollection)this.target).endUnitOfWork();
            }
        }
        catch(ServiceException e) {
            throw new SAXException(e);
        }
    }

    //------------------------------------------------------------------------
    // XmlImporter
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    public void endObject(
        DataproviderObject object,
        String operation
    ) throws ServiceException {
        // set valid period if the "_operation" attribute is not "operation"
        // and the object is not empty (to handle the empty ""_object"" tags)
        if (!"operation".equals(operation) && object.attributeNames().size() > 1) {
            if(globalValidFrom != null && object.values("object_validFrom").size() == 0) {
                object.values("object_validFrom").set(0, globalValidFrom);
            }
            if (globalValidTo != null && object.values("object_validTo").size() == 0) {
                object.values("object_validTo").set(0, globalValidTo);
            }
            if("set".equals(operation)) {
                if(isState(object)) {
                    Path statedObject = object.path();
                    String qualifier = statedObject.remove(object.path().size() - 1);
                    statedObject.add(qualifier.substring(0, qualifier.indexOf(";state=")));
                    if(this.states == null) {
                        this.states = new ArrayList();
                    } else {
                        Path actualObject = ((DataproviderObject)this.states.get(0)).path();
                        if(!statedObject.equals(actualObject)){
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.TRANSFORMATION_FAILURE,
                                "The state instance does not match the actual object",
                                new BasicException.Parameter("actual object", actualObject),
                                new BasicException.Parameter("actual state", statedObject)
                            );
                        }                  
                    }
                    this.states.add(object);
                } else if (this.globalModifiedSince != null) {
                    String modifiedAt = getModifiedAt(object);
                    if(
                            modifiedAt == null ||
                            this.globalModifiedSince.compareTo(modifiedAt) <= 0
                    ) {
                        ((RequestCollection)this.target).addSetRequest(
                            object,
                            AttributeSelectors.NO_ATTRIBUTES,
                            null
                        );
                    }
                }
            }
        }
        SysLog.trace(operation, object);
        if(this.pendingAt == object.path().size()) {
            pendingAt = -1;
            if(this.splitUnitsOfWork) {
                ((RequestCollection)target).endUnitOfWork();
            }
        }
    }

    //------------------------------------------------------------------------
    public void beginObject(
        DataproviderObject object,
        String operation
    ) throws ServiceException {
        SysLog.trace(operation, object.path());
        if("null".equals(operation)) {
            // No processing of current object
        }
        else {
            // target is dataprovider  
            if(this.target instanceof RequestCollection) {
                // An object and its sub-objects are processed as a single unit of work
                if(this.pendingAt == -1 && !isState(object)) {
                    this.pendingAt = object.path().size();
                    if(this.splitUnitsOfWork) {
                        ((RequestCollection) target).beginUnitOfWork(this.transactional);
                    } 
                }
                //
                // create request
                //
                if("create".equals(operation)) {
                    if(object.path().size() % 2 == 0) {
                        this.currentPath = ((RequestCollection)this.target).addCreateRequest(
                            object, 
                            AttributeSelectors.NO_ATTRIBUTES, null
                        ).path();
                    }
                    else {
                        ((RequestCollection)this.target).addCreateRequest(
                            object, 
                            AttributeSelectors.NO_ATTRIBUTES, null
                        );
                    }
                }
                //
                // set request
                //
                else if("set".equals(operation)) {
                    if(!isState(object)) {
                        if(this.states != null) {
                            Path statedObject = ((DataproviderObject)this.states.get(0)).path();
                            if(!object.path().equals(statedObject)) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.TRANSFORMATION_FAILURE,
                                    "The state instances do not match the actual object",
                                    new BasicException.Parameter("actual object", object.path()),
                                    new BasicException.Parameter("actual states", statedObject)
                                );
                            }              
                            boolean deleted = true;
                            boolean modified = this.globalModifiedSince == null;
                            for(
                                    Iterator i = this.states.iterator();
                                    i.hasNext();               
                            ){
                                DataproviderObject_1_0 state = (DataproviderObject_1_0) i.next();
                                String invalidatedAt = getInvalidatedAt(state);                  
                                if(invalidatedAt == null) {
                                    deleted = false;
                                } else {
                                    if(!modified) {
                                        modified = this.globalModifiedSince.compareTo(invalidatedAt) <= 0;
                                    }
                                    i.remove();
                                }
                                if(!modified) {
                                    String modifiedAt = getModifiedAt(state);
                                    modified = 
                                        modifiedAt == null || 
                                        this.globalModifiedSince.compareTo(modifiedAt) <= 0;
                                }
                            }
                            if(modified) {
                                List existing = ((RequestCollection)this.target).addFindRequest(
                                    object.path().getParent(),
                                    new FilterProperty[]{
                                        new FilterProperty(
                                            Quantors.THERE_EXISTS,
                                            "statedObject",
                                            FilterOperators.IS_IN,
                                            object.path()
                                        ),
                                        new FilterProperty(
                                            Quantors.FOR_ALL,
                                            "object_invalidatedAt",
                                            FilterOperators.IS_IN
                                        ),
                                    }
                                );
                                ((RequestCollection)target).endUnitOfWork();
                                ((RequestCollection)target).beginUnitOfWork(this.transactional);
                                if(!existing.isEmpty()) {
                                    ((RequestCollection)this.target).addRemoveRequest(object.path());
                                }
                                if(!deleted) {
                                    ((RequestCollection)target).endUnitOfWork();
                                    ((RequestCollection)target).beginUnitOfWork(this.transactional);
                                    for(
                                            int i = 0;
                                            i < this.states.size();
                                            i++
                                    ){
                                        DataproviderObject state = (DataproviderObject)this.states.get(i);
                                        state.path().add(
                                            new StringBuilder(
                                                state.path().remove(state.path().size() - 1)
                                            ).append(
                                                ";validFrom="
                                            ).append(
                                                getValidity(state, "stateValidFrom", "object_validFrom")
                                            ).append(
                                                ";validTo="
                                            ).append(
                                                getValidity(state, "stateValidTo", "object_validTo")
                                            ).toString()
                                        );
                                        if(i == 0) {
                                            ((RequestCollection)target).addCreateRequest(state);
                                            if(this.states.size() > 1){
                                                ((RequestCollection)target).endUnitOfWork();
                                                ((RequestCollection)target).beginUnitOfWork(this.transactional);
                                            }
                                        } else {
                                            ((RequestCollection)target).addReplaceRequest(state);
                                        }
                                    }
                                }
                            }
                            this.states = null;
                        } else if (this.globalModifiedSince == null) {
                            ((RequestCollection)this.target).addSetRequest(
                                object,
                                AttributeSelectors.NO_ATTRIBUTES,
                                null
                            );
                        }
                    }
                }
                //
                // update request
                //
                else if("update".equals(operation)) {
                    DataproviderObject retrieved = new DataproviderObject(
                        this.reader.addGetRequest(
                            object.path()
                        )
                    );
                    object.setDigest(retrieved.getDigest());
                    ((RequestCollection)this.target).addReplaceRequest(
                        object,
                        AttributeSelectors.NO_ATTRIBUTES,
                        null
                    );
                }
                //
                // operation request
                //
                else if("operation".equals(operation)) {
                    ((RequestCollection)this.target).addOperationRequest(object);
                }
                //
                // remove request
                //
                else if("remove".equals(operation)) {
                    ((RequestCollection)this.target).addRemoveRequest(object.path());
                }
                //
                // illegal request
                //
                else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        "Supported operations are [\"\"|\"null\"|\"set\"|\"create\"|\"update\"|\"operation\"|\"remove\"]",
                        new BasicException.Parameter("operation", operation)
                    );
                }
            }
            // target is Map
            else if (target instanceof Map) {
                DataproviderObject existing = (DataproviderObject)((Map)this.target).get(object.path());
                if("create".equals(operation)) {
                    if(existing != null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.DUPLICATE,
                            "can not create object. Object already exists.",
                            new BasicException.Parameter("object", object)
                        );
                    }
                    else {
                        ((Map) this.target).put(object.path(), object);                
                    }
                }
                else if("set".equals(operation) || "update".equals(operation)) {
                    if(existing == null) {
                        ((Map) this.target).put(object.path(), object);                
                    }
                    else {
                        existing.addClones(
                            object,
                            true
                        );
                    }
                }
                else {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        "Supported operations are [\"\"|\"null\"|\"set\"|\"create\"]",
                        new BasicException.Parameter("operation", operation)
                    );
                }
            }
            // not supported target
            else {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "target type not supported. Supported are [Map|RequestCollection]",
                    new BasicException.Parameter("target type", target.getClass().getName())
                );
            }
        }
    }

    //------------------------------------------------------------------------
    // DTDHandler
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    public void notationDecl(
        String name, 
        String publicId, 
        String systemId
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void unparsedEntityDecl(
        String name, 
        String publicId, 
        String systemId, 
        String notationName
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    // LexicalHandler
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    public void startDTD(
        String name, 
        String publicId, 
        String systemId
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void startEntity(
        String name
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void startCDATA(
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void endCDATA(
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void comment(
        char[] ch, 
        int offset, 
        int length
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void endEntity(
        String name
    ) throws SAXException {
        //
    }
    //------------------------------------------------------------------------
    public void endDTD(
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    // DeclHandler
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    public void elementDecl(
        String name, 
        String contentModel
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void attributeDecl(
        String elementName, 
        String attributeName, 
        String type, 
        String defaultValue, 
        String defaultType
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void internalEntityDecl(
        String name, 
        String text
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    public void externalEntityDecl(
        String name, 
        String publicId, 
        String systemId
    ) throws SAXException {
        //
    }

    //------------------------------------------------------------------------
    // ErrorHandler
    //------------------------------------------------------------------------

    public void warning( 
        SAXParseException e
    ) {
        SysLog.warning(getLocationString(e), e.getMessage());
    }

    //------------------------------------------------------------------------
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

    //------------------------------------------------------------------------
    public void fatalError(
        SAXParseException e
    ) throws SAXException {
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

    //------------------------------------------------------------------------
    // XmlImporter
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    protected String getLocationString(
        SAXParseException ex
    ) {
        StringBuilder str = new StringBuilder();
        String systemId = ex.getSystemId();
        if(systemId != null) {
            int index = systemId.lastIndexOf('/');
            if(index != -1) 
                systemId = systemId.substring(index + 1);
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

    //------------------------------------------------------------------------
    /**
     * Parse elementName which is of the format <className>.<propertyName>.
     * Translate into an List of strings.
     */
    private List parseElement(
        String elementName
    ) {
        ArrayList element = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(elementName, ".");
        while(tokenizer.hasMoreTokens()) {
            element.add(tokenizer.nextToken());
        }
        return element;
    }

    //------------------------------------------------------------------------
    private String toNameComponent(
        List nameElements
    ) {
        if(nameElements.size() == 0) {
            return "";
        }
        StringBuilder nameComponent = new StringBuilder((String)nameElements.get(0));
        for(
                int i = 1;
                i < nameElements.size();
                i++
        ) {
            nameComponent.append(":" + nameElements.get(i));
        }
        return nameComponent.toString();
    }

    //------------------------------------------------------------------------
    private InputSource getSchemaInputSource(
        String schemaUri
    ) throws ServiceException {
        URL schemaUrl = null;
        try {
            if(this.url != null) {
                if(
                        this.url.toString().startsWith("file:") &&
                        !this.url.toString().startsWith("file:/") &&
                        !this.url.toString().startsWith("file:./")
                ) {
                    schemaUrl = new URL(
                        new URL(
                            "file:./" +
                            this.url.toString().substring(5)
                        ),
                        schemaUri
                    );
                } 
                else {
                    schemaUrl = new URL(this.url, schemaUri);
                }
            }
            if((schemaUrl != null) && schemaUrl.toString().startsWith("resource:")) {
                schemaUrl = new URL(
                    "xri:+resource/" + 
                    schemaUri.substring(schemaUri.lastIndexOf("../") + 3)
                );
            }
            else if(schemaUri.startsWith("xri:+resource/")) {
                schemaUrl = new URL(
                    "xri://+resource/" + 
                    schemaUri.substring(14)
                );
            }
            else if(schemaUri.startsWith("xri://+resource/")) {
                schemaUrl = new URL(schemaUri);
            }
            if(schemaUrl == null) {
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
        }
        catch (IOException exception) {
            throw new ServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Schema access failed",
                new BasicException.Parameter("document", this.url),
                new BasicException.Parameter("schema", schemaUrl == null ? schemaUri : schemaUrl.toString())
            );
        }
    }

    //------------------------------------------------------------------------
    private XMLReader getReader(
    ) throws ServiceException {
        // construct parser and set features
        XMLReader reader;
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            SysLog.detail("SAX Parser", parser.getClass().getName());
            reader = parser.getXMLReader();
            SysLog.detail("XML Reader", reader.getClass().getName());
        } 
        catch (Exception exception) {
            throw new ServiceException(
                exception, 
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.INVALID_CONFIGURATION,
                "unable to acquire a SAX Parser"
            );
        }

        // namespaces  
        try {
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
        }
        catch(SAXException e) {
            new ServiceException(
                e, 
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.INVALID_CONFIGURATION,
                "unable to set feature",
                new BasicException.Parameter("feature","http://xml.org/sax/features/namespaces")
            );
        }

        // validation
        try {
            reader.setFeature("http://xml.org/sax/features/validation", this.schemaValidation);
        }
        catch(SAXException e) {
            new ServiceException(
                e, 
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.INVALID_CONFIGURATION,
                "unable to set feature",
                new BasicException.Parameter("feature","http://xml.org/sax/features/validation")
            );
        }
        try {
            reader.setFeature("http://apache.org/xml/features/validation/schema", this.schemaValidation);
        }
        catch(SAXException e) {
            new ServiceException(
                e, 
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.INVALID_CONFIGURATION,
                "unable to set feature",
                new BasicException.Parameter("feature","http://apache.org/xml/features/validation/schema")
            );
        }

        // set handlers
        reader.setContentHandler(this);
        reader.setDTDHandler(this);
        reader.setErrorHandler(this);
        reader.setEntityResolver(this);

        // declaration-handler
        try {
            reader.setProperty("http://xml.org/sax/properties/declaration-handler", this);
        }
        catch(SAXException e) {
            new ServiceException(
                e, 
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.INVALID_CONFIGURATION,
                "unable to set feature",
                new BasicException.Parameter("feature","http://xml.org/sax/properties/declaration-handler")
            );
        }

        // lexical-handler
        try {
            reader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
        }
        catch(SAXException e) {
            new ServiceException(
                e,
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.INVALID_CONFIGURATION,
                "unable to set feature",
                new BasicException.Parameter("feature","http://xml.org/sax/properties/lexical-handler")
            );
        }
        return reader;
    }

    //------------------------------------------------------------------------
    /**
     * Read all XML files specified by uris, interpret the data as dataprovider
     * objects and store the objects to target.
     */
    public void process(
        String... uris
    ) throws ServiceException {

        // reset unit of work
        try {
            if(this.target instanceof RequestCollection) {
                ((RequestCollection)this.target).endUnitOfWork();
            }
        } catch(Exception e) {
            // ignore
        }

        // construct parser and set features
        XMLReader reader = this.getReader();

        // parse files
        for (int i = 0; i < uris.length; i++) {
            try {
                try {
                    this.url = new URL(uris[i]);
                }
                catch (MalformedURLException urlException) {
                    try {
                        this.url = new File(uris[i]).toURL();
                    }
                    catch (MalformedURLException fileException) {
                        throw new ServiceException(
                            urlException,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.PROCESSING_FAILURE,
                            "URI can be parsed neither as URL nor as file",
                            new BasicException.Parameter("uri", uris[i])
                        );
                    }
                }
                // reader.parse(new InputSource(this.url.openStream()));
                reader.parse(uris[i]);
            }
            catch (SAXException e) {
                Exception ex = e.getException();
                throw new ServiceException(
                    ex != null ? ex : e,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "exception while parsing",
                        new BasicException.Parameter("uri", uris[i]),
                        new BasicException.Parameter("index", i)
                );
            }
            catch (Exception e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.PROCESSING_FAILURE,
                    "unknown exception"
                );
            }
        }
    }

    //------------------------------------------------------------------------
    /**
     * Read all XML files specified by uris, interpret the data as dataprovider
     * objects and store the objects to target.
     */
    public void process(
        InputStream[] is
    ) throws ServiceException {

        // reset unit of work
        try {
            if(this.target instanceof RequestCollection) {
                ((RequestCollection)this.target).endUnitOfWork();
            }
        } catch(Exception e) {
            // ignore
        }

        // construct parser and set features
        XMLReader reader = this.getReader();

        // parse files
        for (int i = 0; i < is.length; i++) {
            try {
                // reader.parse(new InputSource(this.url.openStream()));
                reader.parse(new InputSource(is[i]));
            }
            catch (SAXException e) {
                Exception ex = e.getException();
                throw new ServiceException(
                    ex != null ? ex : e,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "exception while parsing",
                        new BasicException.Parameter("input stream", i)
                );
            }
            catch (Exception e) {
                throw new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.PROCESSING_FAILURE,
                    "unknown exception"
                );
            }
        }
    }

    //------------------------------------------------------------------------
    private static String getInvalidatedAt(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        SparseList invalidatedAt = object.getValues("object_invalidatedAt");
        return (String) (invalidatedAt == null ? null : invalidatedAt.get(0));
    }

    //------------------------------------------------------------------------
    private static String getValidity(
        DataproviderObject_1_0 object,
        String attribute0,
        String attribute1    
    ) throws ServiceException {
        Object value = null;
        SparseList values = object.getValues(attribute0);
        if(values != null) value = values.get(0);
        if(value == null) {
            values = object.getValues(attribute1);
            if(values != null) value = values.get(0);
        }
        return value == null ? "ever" : (String)value;
    }

    //------------------------------------------------------------------------
    private static String getModifiedAt(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        byte[] digest = object == null ? null : object.getDigest();
        try {
            return digest == null ? null : new String(
                object.getDigest(),
                PRIVATE_DIGEST_ENCODING
            );
        } catch (UnsupportedEncodingException exception) {
            throw new ServiceException(exception);
        }
    }

    //------------------------------------------------------------------------
    private static boolean isState (
        DataproviderObject_1_0 object
    ) throws ServiceException {
        return object.path().getBase().indexOf(";state=") > 0;
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------


    private final static DateFormat secondFormat = DateFormat.getInstance(
        "yyyy-MM-dd'T'HH:mm:ssz"
    );
    private final static DateFormat millisecondFormat = DateFormat.getInstance(
        "yyyy-MM-dd'T'HH:mm:ss.SSSz"
    );
    private final static DateFormat dateFormat = DateFormat.getInstance(
        "yyyy-MM-dd"
    ); 

    /**
     * Be aware, <code>SimpleDateFormat</code> instances are not thread safe.
     */
    private final SimpleDateFormat localSecondFormat = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss"
    );

    /**
     * Be aware, <code>SimpleDateFormat</code> instances are not thread safe.
     */
    private final SimpleDateFormat localMillisecondFormat = new SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSS"
    );

    public final int _OFFSET_INDEX = 0;
    public final int _MULTIPLICITY_INDEX = 1;

    // defines, whether XML file is validated against schema
    private final boolean schemaValidation;

    // defines whether the operations are transactional or not
    private final boolean transactional;

    // if false the import is performed in one unit of work
    private final boolean splitUnitsOfWork;

    // stack of objects currently open
    Stack objectStack = null;

    // Path length of the request collection's first element
    private int pendingAt = -1;

    /**
     * set of XML elements which denote object elements, i.e. elements with a qualifier
     * This set is required to determine on endElement operations whether an attribute/
     * struct/reference is closed or an object.
     */
    private Map objectElements = null;

    // state/context of current object
    private DataproviderObject currentObject = null;
    private Path currentPath = null;
    private StringBuilder currentAttributeValue = null;
    private String currentAttributeName = null;
    private String currentLocalpartObject = null;
    private int nextTemporaryId = -1;
    private int currentAttributeOffset = 0;
    private int currentAttributePosition = -1;
    private String currentAttributeMultiplicity = null;
    private String currentAttributeOperation = null;

    // true when the last call was endElement(). Is set to false by startElement()
    private boolean previousElementEnded = true;

    private List states = null;
    protected Object target = null;
    protected RequestCollection reader = null;

    private URL url = null;

    // define a valid period to apply to all treated objects.
    private String globalValidFrom = null;
    private String globalValidTo = null;
    private boolean globalCreateInitialAndOtherStatesInSeparateUnitsOfWork = true;


    /**
     * The "set" operation is controlled by the objects' modification dates if
     * <code>globalModifiedSince</code> is not <code>null</code>.
     */
    private String globalModifiedSince = null;

    // cached type definitions (global in classloader)
    private static final Set loadedSchemas = new HashSet();  
    // attributes types as (<qualified attribute name>, <qualified type name) pair.
    private static final Map attributeTypes = new HashMap();
    // attribute multiplicities
    private static final Map attributeMultiplicities = new HashMap();
    // qualifier names of a complex schema type.
    private static final Map qualifierNames = new HashMap();

    private final static String PRIVATE_DIGEST_ENCODING = "UTF-8";

}

//--- End of File -----------------------------------------------------------

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: XMLExportHandler.java,v 1.19 2009/01/13 17:34:50 wfro Exp $
 * Description: Export handler for synchronizer producing XML output
 * Revision:    $Revision: 1.19 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 17:34:50 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.exporter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.openmdx.application.cci.SystemAttributes;
import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.application.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.application.mof.cci.Multiplicities;
import org.openmdx.base.collection.SparseList;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.conversion.Base64;

/**
 * Class to export the callback data as an XML file.
 */
@SuppressWarnings("unchecked")
public class XMLExportHandler implements TraversalHandler {

    /**
     * Create an XMLExportHandler.
     * <p>
     * schemaInstance and schameLocation are written to the generated xml file
     * as specified here. They are not used for verification of the data 
     * provided. No verification takes place at all.
     * 
     * @param model           model of the data to export
     * @param schemaInstance  schema instance for the xml file
     * @param schemaLocation  schema location for the xml file
     */
    public XMLExportHandler(
        Model_1_0 model,
        String schemaInstance,
        String schemaLocation
    ) {
        this.model = model;
        this.schemaInstance = schemaInstance;
        this.schemaLocation = schemaLocation;
    }

    // the XMLReader methods
    public void setContentHandler(
        XmlContentHandler handler
    ) {
        if (handler == null) {
            throw new java.lang.NullPointerException("ContentHandler is null");
        }
        this.contentHandler = handler;
    }

    public XmlContentHandler getContentHandler() {
        return contentHandler;
    }


    public boolean startReference(
        String reference
    ) throws ServiceException {

        contentHandler.startElement(
            "",
            "",
            toSimpleQualifiedName(reference),
            new XmlContentHandler.Attributes());

        return true;
    }

    public void endReference(
        String reference
    ) throws ServiceException {

        this.contentHandler.endElement(
            "",
            "",
            toSimpleQualifiedName(reference)
        );
    }

    public boolean startObject(
        Path parentPath,
        String qualifiedName,
        String qualifierName,
        String id,
        short operation
    ) throws ServiceException {
        XmlContentHandler.Attributes atts = new XmlContentHandler.Attributes();
        atts.addCDATA(qualifierName, id);

        if (qualifiedName.equals("org:openmdx:base:Authority")) {
            atts.addCDATA(
                "xmlns:xsi",
                this.schemaInstance);
            atts.addCDATA(
                "xsi:noNamespaceSchemaLocation",
                this.schemaLocation);
        }
        else if (operation == TraversalHandler.NULL_OP) {
            atts.addCDATA(
                "_operation",
                "null");
        }
        
        String test = toXML(qualifiedName);
        this.contentHandler.startElement("", "", test, atts);
        return true;

    }

    public void endObject(
        String qualifiedName
    ) throws ServiceException {
        this.contentHandler.endElement("", "", "_content");
        String endElem = toXML(qualifiedName);
        this.contentHandler.endElement("", "", endElem);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a map of attribute-specific tags. Tags are written
     * to the XML stream as comment. 
     */
    public Map getAttributeTags(
        DataproviderObject_1_0 object
    ) throws ServiceException {
        return null;
    }
    
    //-----------------------------------------------------------------------
    public boolean featureComplete(
        Path reference,
        DataproviderObject_1_0 object
    ) throws ServiceException {
        Map tags = this.getAttributeTags(object);        
        ModelElement_1_0 objectClass = null;
        if (object.values(SystemAttributes.OBJECT_CLASS).get(0) != null) {
            objectClass = this.model.getDereferencedType(
                object.getValues(SystemAttributes.OBJECT_CLASS).get(0)
            );  
        }
        this.contentHandler.startElement(
            "",
            "",
            "_object",
            new XmlContentHandler.Attributes()
        );

        for (Iterator attr = object.attributeNames().iterator();
            attr.hasNext();
        ) {
            String attributeName = (String) attr.next();

            if (objectClass != null) {

                Map modelAttributes =
                    (Map) objectClass.objGetValue("attribute");
                DataproviderObject attributeType =
                    (DataproviderObject) modelAttributes.get(attributeName);

                if (attributeType == null  
                    || object.getValues(attributeName) == null 
                    || object.getValues(attributeName).size() == 0
                ) {
                    // System.out.println("unkonwn attribute type for: "+ attribute);
                    continue;
                }
                String multiplicity = (String) attributeType.values("multiplicity").get(0);
                boolean isMultiValued = 
                    (multiplicity.equals(Multiplicities.MULTI_VALUE)
                    || multiplicity.equals(Multiplicities.SET)
                    || multiplicity.equals(Multiplicities.LIST)
                    || multiplicity.equals(Multiplicities.SPARSEARRAY)
                ); 
                boolean needsPosition = multiplicity.equals(Multiplicities.SPARSEARRAY);                
                SparseList attributeValues = object.values(attributeName);
                String elementTag = this.toSimpleQualifiedName(
                    (String) attributeType.getValues(
                        "qualifiedName").get(0)
                    );
                this.contentHandler.startElement(
                    "",
                    "",
                    elementTag,
                    new XmlContentHandler.Attributes()
                );

                for(ListIterator i = attributeValues.populationIterator();
                    i.hasNext();
                ) {
                    Object value = i.next();
                    int valueIndex = i.nextIndex();
                    
                    String stringValue = null;
                    String typeName = 
                        ((Path) attributeType.getValues("type").get(0)).getBase();
                    
                    if (typeName.equals("org:w3c:dateTime")) {
                        String v = (String) value;
                        //System.out.println("dateTime=" + v);
                        String t =
                            v.substring(0, 4)
                                + "-"
                                + v.substring(4, 6)
                                + "-"
                                + v.substring(6, 11)
                                //+ v.substring(8, 11)
                                + ":"
                                + v.substring(11, 13)
                                + ":"
                                + v.substring(13, 20);
                        stringValue = t;
                    }
                    else if (typeName.equals("org:w3c:date")) {
                        String v = (String) value;
                        String t =
                            v.substring(0, 4)
                                + "-"
                                + v.substring(4, 6)
                                + "-"
                                + v.substring(6, 8);
                        stringValue = t;
                    }
                    else if (typeName.equals("org:w3c:long")
                        || typeName.equals("org:w3c:integer")
                        || typeName.equals("org:w3c:short")
                    ) {
                        // BigDecimal may return strange formatted string, 
                        // thus convert first to Long
                        value = new Long(((Number) value).longValue());
                        stringValue = value.toString();
                    }
                    else if("org:w3c:binary".equals(typeName)) {
                        if(value instanceof byte[]) {
                            stringValue = Base64.encode((byte[])value);
                        }
                        else {
                            stringValue = value.toString();
                        }
                    }
                    else if(value instanceof Path){
                        stringValue = ((Path)value).toXri();
                    }
                    else {                       
                        stringValue = value.toString();
                    }
                    
                    XmlContentHandler.Attributes atts = new XmlContentHandler.Attributes();
                    if (needsPosition) {
                        atts.addCDATA(
                            "_position", 
                            String.valueOf(valueIndex)
                        );
                    }
                    if (isMultiValued) {
                        this.contentHandler.startElement(
                            "",
                            "",
                            "_item",
                            atts
                        );
                    }                    
                    this.contentHandler.characters(
                        stringValue.toCharArray(),
                        0,
                        stringValue.length()
                    );                        
                    if (isMultiValued) {
                        this.contentHandler.endElement("", "", "_item");
                    }
                }
                this.contentHandler.endElement("", "", elementTag);

                // generate attribute tag as comment
                if((tags != null) && (tags.get(attributeName) != null)) {
                    this.contentHandler.comment((String)tags.get(attributeName));
                }                
            }
        }
        this.contentHandler.endElement("", "", "_object");        
        this.contentHandler.startElement(
            "",
            "",
            "_content",
            new XmlContentHandler.Attributes()
        );        
        return true;
    } 
    
    /**
     * No action required.
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#contentComplete(org.openmdx.base.naming.Path, java.lang.String, java.util.List)
     */
    public void contentComplete(
        Path objectPath, 
        String objectClassName, 
        List containedReferences
    ) throws ServiceException { // not interested in, got all the single elements already.
        // no need for deleting anything
    }
    
    /**
     * No action required. 
     * 
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#referenceComplete(org.openmdx.base.naming.Path, java.util.Collection)
     */
    public void referenceComplete(
        Path reference, 
        Collection objectIds
    ) throws ServiceException { // not interested in, got all the single references already.
        // no need for deleting anything
    }

    public void startTraversal(
        List startPaths
    ) throws ServiceException {
        StringBuilder sb = new StringBuilder();
        for (Iterator i = startPaths.iterator(); i.hasNext();) {
            sb.append(((Path) i.next()).toString());
            if (i.hasNext()) {
                sb.append(";");
            }
        }

        this.contentHandler.startDocument();
        this.contentHandler.processingInstruction("StartPath", sb.toString());
    }

    public void endTraversal() throws ServiceException {
        this.contentHandler.endDocument();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#getTransactionBehavior()
     */
    public short getTransactionBehavior() {
        return 0;
    } 
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.exporter.TraversalHandler#setTransactionBehavior(short)
     */
    public void setTransactionBehavior(
        short transactionBehavior
    ) throws ServiceException { 
        //
    }

    private String toSimpleQualifiedName(
        String qualifiedName
    ) {

        return qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1);
    }
    
    private String toXML(
        Object elementName
    ) {
        return ((String) elementName).replace(':', '.');
    } 
    
    //-----------------------------------------------------------------------
    // Members    
    //-----------------------------------------------------------------------
    protected Model_1_0 model = null;
    protected String schemaInstance = null;
    protected String schemaLocation = null;
    protected XmlContentHandler contentHandler = null;
}

//--- End of File -----------------------------------------------------------


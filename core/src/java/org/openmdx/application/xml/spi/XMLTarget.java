/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: XMLTarget.java,v 1.8 2010/03/31 14:34:35 hburger Exp $
 * Description: XML Target 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/31 14:34:35 $
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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.jdo.JDOHelper;
import javax.jmi.reflect.RefObject;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.XMLEncoder;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.format.DateTimeFormat;

/**
 * XML Target
 */
public class XMLTarget implements ExportTarget {

    /**
     * Constructor 
     *
     * @param out
     * 
     * @throws ServiceException
     */
	public XMLTarget(
	    OutputStream out
	) throws ServiceException {
		this.xmlWriter = new XMLWriter(out);
		this.model = Model_1Factory.getModel();
	}

	/**
	 * The delegate
	 */
    private final XMLWriter xmlWriter;

    /**
     * 
     */
    public static final String FILE_EXT_XML = ".xml";
    
    /**
     * The model accessor
     */
    private final Model_1_0 model;
    
    /**
     * Convert a MOF name to its XML format
     * 
     * @param elementName
     * 
     * @return the corresponding XML name
     */
	protected static String toXML(
	    String elementName
	) {
		return elementName.replace(':', '.');
	}

	/**
	 * Extract the simple name from a qualified name
	 * 
	 * @param qualifiedName
	 * 
	 * @return the corresponding simple name
	 */
	private static String getSimpleName(String qualifiedName) {
		return qualifiedName.substring(qualifiedName.lastIndexOf(':') + 1);
	}

	/**
	 * Determine an object's qualifier name
	 * 
	 * @param object
	 * 
	 * @return the qualifier name
	 * 
	 * @throws ServiceException
	 */
    private String getQualifierName(
        RefObject object
    ) throws ServiceException {
        ModelElement_1_0 objectClass = this.model.getElement(object.refClass().refMofId());
        if(!objectClass.objGetList("compositeReference").isEmpty()) {
            ModelElement_1_0 compReference = this.model.getElement(((Path) objectClass.objGetValue("compositeReference")).getBase());
            ModelElement_1_0 associationEnd = this.model.getElement(((Path) compReference.objGetValue("referencedEnd")).getBase());
            return (String) associationEnd.objGetValue("qualifierName");
        }
        if("org:openmdx:base:Authority".equals(objectClass.objGetValue("qualifiedName"))) {
            return "name";
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.ASSERTION_FAILURE, 
            "no composite reference found for class.", 
            new BasicException.Parameter("class", objectClass)
        );
    }
	
    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#exportProlog()
     */
    public void exportProlog(
        boolean empty
    ) throws ServiceException {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#exportProlog(org.openmdx.base.accessor.jmi.cci.RefObject_1_0)
     */
    public void startAuthority(
        String authority
    ) throws ServiceException {
        this.xmlWriter.startDocument(authority);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#startObject(org.openmdx.application.xml.Exporter.TraversedObject, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void startObject(
        RefObject refObject, 
        boolean noOperation
    ) throws ServiceException {
        String qualifierName = getQualifierName(refObject);
        String qualifiedTypeName = refObject.refClass().refMofId();
        Path objectId = (Path) JDOHelper.getObjectId(refObject);
        Map<String, String> atts = new LinkedHashMap<String,String>();
        String qualifierValue = objectId.getBase();
        atts.put(qualifierName, qualifierValue);
        if(noOperation) {
            atts.put("_operation", "null");
        }
        if(qualifiedTypeName.equals("org:openmdx:base:Authority")) {
            String namespace = qualifierValue.substring(0, qualifierValue.lastIndexOf(":"));
            String modelName = qualifierValue.substring(qualifierValue.lastIndexOf(":") + 1);
            atts.put(
                "xmlns:xsi", 
                "http://www.w3.org/2001/XMLSchema-instance"
            );
            atts.put(
                "xsi:noNamespaceSchemaLocation", 
                "xri://+resource/" + namespace.replace(':', '/') + "/" + modelName + "/xmi1/" + modelName + ".xsd"
            );
        }
        this.xmlWriter.startElement("", "", toXML(qualifiedTypeName), atts, false);
	}

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#startAttributes()
     */
    public void startAttributes(boolean empty) throws ServiceException {
        this.xmlWriter.startElement("", "", "_object", null, empty);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#startAttribute(java.lang.String, java.lang.String, java.lang.String, java.lang.Object, boolean)
     */
    public void startAttribute(
        String qualifiedName,
        String typeName,
        String multiplicity,
        Object values,
        boolean empty
    ) throws ServiceException {
        this.xmlWriter.startElement("", "", getSimpleName(qualifiedName), null, empty);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#write(java.lang.Object)
     */
    public void write(
        String typeName, 
        String multiplicity, 
        int position, 
        Object value
    ) throws ServiceException {
        boolean needsPosition = multiplicity.equals(Multiplicities.SPARSEARRAY);
        boolean multiValued = 
            multiplicity.equals(Multiplicities.MULTI_VALUE) || 
            multiplicity.equals(Multiplicities.SET) || 
            multiplicity.equals(Multiplicities.LIST) || 
            multiplicity.equals(Multiplicities.SPARSEARRAY);                
        String stringValue;
        if(PrimitiveTypes.DATETIME.equals(typeName)) {
            stringValue = DateTimeFormat.EXTENDED_UTC_FORMAT.format((Date) value);
        } else if(PrimitiveTypes.DATE.equals(typeName)) {
            stringValue = ((XMLGregorianCalendar) value).toXMLFormat();
        } else if(PrimitiveTypes.LONG.equals(typeName) || PrimitiveTypes.INTEGER.equals(typeName) || PrimitiveTypes.SHORT.equals(typeName)) {
            stringValue = String.valueOf(((Number) value).longValue());
        } else if(PrimitiveTypes.BINARY.equals(typeName)) {
            stringValue = value instanceof byte[] ? Base64.encode((byte[]) value) : value.toString();
        } else if(value instanceof Path) {
            stringValue = ((Path)value).toXRI();
        } else {
            stringValue = value.toString();
        }
        if(multiValued) {
            Map<String, String> atts = new LinkedHashMap<String,String>();
            if(needsPosition) {
                atts.put("_position", String.valueOf(position));
            }
            this.xmlWriter.startElement("", "", "_item", atts, false);
            this.xmlWriter.write(stringValue);
            this.xmlWriter.endElement("", "", "_item", false);
        } else {
            this.xmlWriter.write(stringValue);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#endAttribute(java.lang.String, java.lang.String, java.lang.String, java.lang.Object, boolean)
     */
    public void endAttribute(
        String qualifiedName,
        String typeName,
        String multiplicity,
        Object values,
        boolean empty
    ) throws ServiceException {
        if(!empty) {
            boolean multiValued = 
                multiplicity.equals(Multiplicities.MULTI_VALUE) || 
                multiplicity.equals(Multiplicities.SET) || 
                multiplicity.equals(Multiplicities.LIST) || 
                multiplicity.equals(Multiplicities.SPARSEARRAY);                
            this.xmlWriter.endElement("", "", getSimpleName(qualifiedName), multiValued);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#endAttributes(boolean)
     */
    public void endAttributes(
        boolean empty
    ) throws ServiceException {
        if(!empty) {
            this.xmlWriter.endElement("", "", "_object", true);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#beginReferences()
     */
    public void startChildren(
        boolean empty
    ) throws ServiceException {
        this.xmlWriter.startElement("", "", "_content", null, empty);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#startReference(java.lang.String)
     */
    public void startReference(
        String name, 
        boolean empty
    ) throws ServiceException {
		this.xmlWriter.startElement("", "", getSimpleName(name), null, empty);
	}

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#endReference(java.lang.String)
     */
    public void endReference(
        String reference, 
        boolean empty
    ) throws ServiceException {
        if(!empty){
            this.xmlWriter.endElement("", "", getSimpleName(reference), true);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#endReferences()
     */
    public void endChildren(
        boolean empty
    ) throws ServiceException {
        if(!empty) {
            this.xmlWriter.endElement("", "", "_content", true);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#endObject(org.openmdx.application.xml.Exporter.TraversedObject, org.openmdx.base.accessor.jmi.cci.RefObject_1_0, java.lang.String)
     */
    public void endObject(
        RefObject refObject
    ) throws ServiceException {
        String qualifiedName = refObject.refClass().refMofId();
        this.xmlWriter.endElement("", "", toXML(qualifiedName), true);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#exportEpilog(org.openmdx.base.accessor.jmi.cci.RefObject_1_0, org.openmdx.application.xml.Exporter.TraversedObject)
     */
    public void endAuthority(
        String authority
    ) throws ServiceException {
		this.xmlWriter.endDocument();
	}

    /* (non-Javadoc)
     * @see org.openmdx.application.xml.spi.ExportTarget#exportEpilog()
     */
    public void exportEpilog(
        boolean empty
    ) throws ServiceException {
        // nothing to do
    }

	
    // -----------------------------------------------------------------------
	// Class XMLWriter
    // -----------------------------------------------------------------------
	
	/**
	 * XML Writer
	 */
    static class XMLWriter {

        /**
         * Constructor 
         *
         * @param out
         * 
         * @throws ServiceException
         */
        protected XMLWriter(
            OutputStream out
        ) throws ServiceException {
            this.outputStream = out;
            this.qNameStack = new Stack<String>();
        }

        /**
         * The character encoding to be used
         */
        private static final String ENCODING = "UTF-8";

        /**
         * The binary stream
         */
        private final OutputStream outputStream;

        /**
         * The text stream
         */
        private PrintStream printStream;

        /**
         * Indentation by TAB
         */
        private final static String INDENT = "\t";

        /**
         * 
         */
        private final Stack<String> qNameStack;

        /**
         * Tells whether the documents are added to an archive
         * 
         * @return <code>true</code> if the documents are added to an archive
         */
        private boolean isMultiFileExport(){
            return this.outputStream instanceof ZipOutputStream;
        }
        
        /**
         * Start Document
         * 
         * @throws ServiceException
         */
        public void startDocument(
            String authority
        ) throws ServiceException {
            if(isMultiFileExport()) try {
                ((ZipOutputStream) this.outputStream).putNextEntry(
                    new ZipEntry(toXML(authority) + FILE_EXT_XML)
                );
            } catch (IOException exception) {
                throw new ServiceException(exception);
            }
            try {
                this.printStream = new PrintStream(this.outputStream, false, ENCODING);
            } catch (UnsupportedEncodingException exception) {
                throw new ServiceException(exception);
            }
            this.printStream.print("<?xml version=\"1.0\" encoding=\"");
            this.printStream.print(ENCODING);
            this.printStream.print("\"?>");
        }

        /**
         * Start Element
         * 
         * @param namespaceURI
         * @param localName
         * @param qName
         * @param attributes
         * @param empty
         * 
         * @throws ServiceException
         */
        public void startElement(
            String namespaceURI, 
            String localName, 
            String qName, 
            Map<String, String> attributes, 
            boolean empty
        ) throws ServiceException {
            this.printStream.println();
            for (int i = 0; i < this.qNameStack.size(); i++) {
                this.printStream.print(INDENT);
            }
            this.printStream.print("<");
            this.printStream.print(qName);
            if(attributes != null) {
                for(Map.Entry<String, String> attribute : attributes.entrySet()) {
                    this.printStream.print(" ");
                    this.printStream.print(XMLEncoder.encode(attribute.getKey()));
                    this.printStream.print("=\"");
                    this.printStream.print(XMLEncoder.encode(attribute.getValue()));
                    this.printStream.print("\"");
                }
            }
            if(empty) {
                this.printStream.print("/>");
            } else {
                this.printStream.print(">");
                this.qNameStack.push(qName);
            }
        }

        /**
         * Print XML Encoded Value
         * 
         * @param value the value to be written
         * 
         * @throws ServiceException
         */
        public void write(
            String value
        ) throws ServiceException {
            this.printStream.print(XMLEncoder.encode(value));
        }

        /**
         * End Element
         * 
         * @param namespaceURI
         * @param localName
         * @param qName
         * @param newLine
         * 
         * @throws ServiceException
         */
        public void endElement(
            String namespaceURI, 
            String localName, 
            String qName, 
            boolean newLine
        ) throws ServiceException {
            String expectedQName = this.qNameStack.pop();
            if(!expectedQName.equals(qName)) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Non matching qName for XML tag.", 
                    new BasicException.Parameter("qName", qName),
                    new BasicException.Parameter("expected qName", expectedQName)
                );
            }
            if(newLine) {
                this.printStream.println();
                for (int i = this.qNameStack.size(); i > 0; i--) {
                    this.printStream.print(INDENT);
                }
            }
            this.printStream.print("</");
            this.printStream.print(XMLEncoder.encode(qName));
            this.printStream.print(">");
        }

        /**
         * Comment
         * 
         * @param comment
         * 
         * @throws ServiceException
         */
        public void comment(String comment) throws ServiceException {
            this.printStream.print("<!-- " + comment + " -->");
        }

        /**
         * Processing Instruction
         * 
         * @param target
         * @param data
         * 
         * @throws ServiceException
         */
        public void processingInstruction(String target, String data) throws ServiceException {
            this.printStream.println();
            this.printStream.print("<?");
            this.printStream.print(target);
            this.printStream.print(" ");
            this.printStream.print(data);
            this.printStream.print("?>");
        }
        /**
         * End Document
         * 
         * @throws ServiceException
         */
        public void endDocument(
        ) throws ServiceException {
            this.printStream.close();
            if(!this.qNameStack.isEmpty()) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.ASSERTION_FAILURE, 
                    "Open elements while endDocument().", 
                    new BasicException.Parameter("elements", this.qNameStack)
                );
            }
        }

    }

}
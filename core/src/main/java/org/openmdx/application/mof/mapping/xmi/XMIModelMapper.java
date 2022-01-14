/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: XMI Model Mapper
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.application.mof.mapping.xmi;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.wbxml.cci.StringTable;
import org.openmdx.base.xml.stream.XMLOutputFactories;
import org.w3c.format.DateTimeFormat;

/**
 * XMI Model Mapper
 */
public class XMIModelMapper implements StringTable {

    /**
     * Constructor 
     *
     * @param os
     * @param mimeType
     * @param allFeatures 
     * 
     * @throws ServiceException
     */
    public XMIModelMapper(
        OutputStream os,
        String mimeType, 
        boolean allFeatures
    ) throws ServiceException {
        try {
            this.pw = XMLOutputFactories.newInstance(mimeType).createXMLStreamWriter(os, "UTF-8");
            this.derivedFeatures = allFeatures;
            this.emptyFeatures = allFeatures;
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }
    
    private final XMLStreamWriter pw;
    private final boolean derivedFeatures;
    private final boolean emptyFeatures;

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.cci.StringTable#isStringTablePopulatedExplicitely()
     */
    @Override
    public boolean isStringTablePopulatedExplicitely() {
        if(this.pw instanceof StringTable) {
            return ((StringTable)this.pw).isStringTablePopulatedExplicitely();
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.wbxml.cci.StringTable#addString(java.lang.String)
     */
    @Override
    public void addString(String string) {
        if(this.pw instanceof StringTable) {
            ((StringTable)this.pw).addString(string);
        }
    }

    /**
     * In case of a multi-valued element write it in the form 
     * <elementName>{<_item >value</_item>}</elementName>.
     * 
     * @param elementName
     * @param elementValues
     */
    void writeElement(
        String elementName,
        Collection<Object> elementValues
    ) throws XMLStreamException {
        if(elementValues.isEmpty()) {
            if(this.emptyFeatures) {
                this.pw.writeEmptyElement(elementName);
            }
        } else {
            this.pw.writeStartElement(elementName);
            for(Object elementValue: elementValues) {
                this.pw.writeStartElement("_item");
                writeValue(elementValue);
                this.pw.writeEndElement();
            }
            this.pw.writeEndElement();
        }
    }

    /**
     * In case of a map element write it in the form 
     * <elementName>{<_item key="key">value</_item>}</elementName>.
     * 
     * @param elementName
     * @param elementValues
     */
    void writeElement(
        String elementName,
        Map<?,?> elementValues
    ) throws XMLStreamException {
        if(elementValues.isEmpty()) {
            if(this.emptyFeatures) {
                this.pw.writeEmptyElement(elementName);
            }
        } else {
            this.pw.writeStartElement(elementName);
            for(Map.Entry<?, ?> elementValue: elementValues.entrySet()) {
                this.pw.writeStartElement("_item");
                this.pw.writeAttribute("_key", elementValue.getKey().toString());
                writeValue(elementValue.getValue());
                this.pw.writeEndElement();
            }
            this.pw.writeEndElement();
        }
    }

    
    /**
     * Otherwise write it in the form <elementName>value</elementName>
     *
     * @param elementName
     * @param elementValue
     */
    void writeElement(
        String elementName,
        java.lang.Object elementValue
    ) throws XMLStreamException {
        this.writeElement(
            44,
            elementName,
            elementValue instanceof ModelElement_1_0 ? ((ModelElement_1_0)elementValue).jdoGetObjectId() : elementValue
        );
    } 

    /**
     * Externalize an element value
     * 
     * @param elementValue
     * @throws XMLStreamException
     */
    void writeValue(
        Object elementValue
    ) throws XMLStreamException{
        if(elementValue instanceof Path) {
            writeValue((Path)elementValue);
        } else if (elementValue instanceof DataObject_1_0) {
            writeValue(((DataObject_1_0)elementValue).jdoGetObjectId());
        } else if (elementValue instanceof ModelElement_1_0) {
            writeValue(((ModelElement_1_0)elementValue).jdoGetObjectId());
        } else {
            this.pw.writeCharacters(elementValue.toString());
        }
    }

    /**
     * @param xri
     * @throws XMLStreamException
     */
    private void writeValue(
        final Path xri
    ) throws XMLStreamException {
        this.pw.writeCharacters(xri.toXRI());
    }
    
   /**
     * @param pos
     * @param elementName
     * @param elementValue
     */
    void writeElement(
        int pos,
        String elementName,
        java.lang.Object elementValue
    ) throws XMLStreamException {
        if(elementValue == null) {
            if(this.emptyFeatures) {
                this.pw.writeEmptyElement(elementName);
            }
        } else {
            this.pw.writeStartElement(elementName);
            writeValue(elementValue);
            this.pw.writeEndElement();
        }
    } 

    /**
     * Translate a string of the form 20020406T082623.930Z to a string of the
     * form 2002-04-06T08:26:23Z.
     * 
     * @param elementName
     * @param elementValue
     */
    void writeElementAsDateTime(
        String elementName,
        Date elementValue
    ) throws XMLStreamException {
        this.writeElementAsDateTime(
            44,
            elementName,
            elementValue
        );
    }

    /**
     * Write element as date time.
     * 
     * @param pos
     * @param elementName
     * @param elementValue
     */
    void writeElementAsDateTime(
        int pos,
        String elementName,
        Date elementValue
    ) throws XMLStreamException {
        this.pw.writeStartElement(elementName);
        this.pw.writeCharacters(DateTimeFormat.EXTENDED_UTC_FORMAT.format(elementValue));
        this.pw.writeEndElement();
    }  

    /**
     * Write element with XML-encoded element values.
     * 
     * @param elementName
     * @param elementValues
     */
    void writeElementEncoded(
        String elementName,
        List<Object> elementValues
    ) throws XMLStreamException {
        for(Object elementValue: elementValues) {
            this.pw.writeStartElement(elementName);
            this.pw.writeCharacters(elementValue.toString());
            this.pw.writeEndElement();
        }
    }  

    /**
     * Write model package definition.
     * 
     * @param packageDef
     * @throws ServiceException
     */
    public void writePackage(
        ModelElement_1_0 packageDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Package");
            this.pw.writeAttribute("qualifiedName", packageDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, packageDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)packageDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, packageDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)packageDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, packageDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("container", packageDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", packageDef.getName());
                this.writeElement("qualifiedName", packageDef.getQualifiedName());
            }
            this.writeElement("stereotype", packageDef.objGetList("stereotype"));
            this.writeElement("isAbstract", packageDef.isAbstract());
            if(this.derivedFeatures) {
                this.writeElement("feature", packageDef.objGetList("feature"));
                this.writeElement("content", packageDef.objGetList("content"));
                this.writeElement("allSupertype", packageDef.objGetList("allSupertype"));
                this.writeElement("subtype", packageDef.objGetList("subtype"));
                this.writeElement("allSubtype", packageDef.objGetList("allSubtype"));
            }
            this.writeElement("supertype", packageDef.objGetList("supertype"));
            this.writeElement("visibility", packageDef.objGetValue("visibility"));
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write primitive type definition.
     * 
     * @param primitiveTypeDef
     * @throws ServiceException
     */
    public void writePrimitiveType(
        ModelElement_1_0 primitiveTypeDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.PrimitiveType");
            this.pw.writeAttribute("qualifiedName", primitiveTypeDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, primitiveTypeDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)primitiveTypeDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, primitiveTypeDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)primitiveTypeDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, primitiveTypeDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("container", primitiveTypeDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", primitiveTypeDef.getName());
                this.writeElement("qualifiedName", primitiveTypeDef.getQualifiedName());
            }
            this.writeElement("stereotype", primitiveTypeDef.objGetList("stereotype"));
            this.writeElement("isAbstract", primitiveTypeDef.isAbstract());
            if(this.derivedFeatures) {
                this.writeElement("feature", primitiveTypeDef.objGetList("feature"));
                this.writeElement("content", primitiveTypeDef.objGetList("content"));
                this.writeElement("allSupertype", primitiveTypeDef.objGetList("allSupertype"));
                this.writeElement("subtype", primitiveTypeDef.objGetList("subtype"));
                this.writeElement("allSubtype", primitiveTypeDef.objGetList("allSubtype"));
            }
            this.writeElement("supertype", primitiveTypeDef.objGetList("supertype"));
            this.writeElement("visibility", primitiveTypeDef.objGetValue("visibility"));
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write attribute definition.
     * 
     * @param attributeDef
     * @throws ServiceException
     */
    public void writeAttribute(
        ModelElement_1_0 attributeDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Attribute");
            this.pw.writeAttribute("qualifiedName", attributeDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, attributeDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)attributeDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, attributeDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)attributeDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, attributeDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("isDerived", attributeDef.isDerived());
            this.writeElement("maxLength", attributeDef.objGetValue("maxLength"));
            this.writeElement("container", attributeDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", attributeDef.getName());
                this.writeElement("qualifiedName", attributeDef.getQualifiedName());
            }
            this.writeElement("stereotype", attributeDef.objGetList("stereotype"));
            this.writeElement("scope", attributeDef.objGetValue("scope"));
            this.writeElement("visibility", attributeDef.objGetValue("visibility"));
            this.writeElement("isChangeable", attributeDef.isChangeable());
            this.writeElement("multiplicity", attributeDef.getMultiplicity());
            this.writeElement("type", attributeDef.getType());
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write structure field definition.
     * 
     * @param structureFieldDef
     * @throws ServiceException
     */
    public void writeStructureField(
        ModelElement_1_0 structureFieldDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.StructureField");
            this.pw.writeAttribute("qualifiedName", structureFieldDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, structureFieldDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)structureFieldDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, structureFieldDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)structureFieldDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, structureFieldDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("maxLength", structureFieldDef.objGetValue("maxLength"));
            this.writeElement("multiplicity", structureFieldDef.getMultiplicity());
            this.writeElement("container", structureFieldDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", structureFieldDef.getName());
                this.writeElement("qualifiedName", structureFieldDef.getQualifiedName());
            }
            this.writeElement("stereotype", structureFieldDef.objGetList("stereotype"));
            this.writeElement("type", structureFieldDef.getType());
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write operation definition.
     * 
     * @param operationDef
     * @throws ServiceException
     */
    public void writeOperation(
        ModelElement_1_0 operationDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Operation");
            this.pw.writeAttribute("qualifiedName", operationDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, operationDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)operationDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, operationDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)operationDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, operationDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            if(this.derivedFeatures) {
                this.writeElement("parameter", operationDef.objGetList("parameter"));
            }
            this.writeElement("container", operationDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", operationDef.getName());
                this.writeElement("qualifiedName", operationDef.getQualifiedName());
            }
            this.writeElement("stereotype", operationDef.objGetList("stereotype"));
            this.writeElement("scope", operationDef.objGetValue("scope"));
            this.writeElement("visibility", operationDef.objGetValue("visibility"));
            this.writeElement("exception", operationDef.objGetList("exception"));
            this.writeElement("semantics", operationDef.objGetValue("semantics"));
            this.writeElement("isQuery", operationDef.objGetValue("isQuery"));
            if(this.derivedFeatures) {
                this.writeElement("content", operationDef.objGetList("content"));
            }
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write exception definition.
     * 
     * @param exceptionDef
     * @throws ServiceException
     */
    public void writeException(
        ModelElement_1_0 exceptionDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Exception");
            this.pw.writeAttribute("qualifiedName", exceptionDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, exceptionDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)exceptionDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, exceptionDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)exceptionDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, exceptionDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            if(this.derivedFeatures) {
                this.writeElement("parameter", exceptionDef.objGetList("parameter"));
            }
            this.writeElement("container", exceptionDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", exceptionDef.getName());
                this.writeElement("qualifiedName", exceptionDef.getQualifiedName());
            }
            this.writeElement("stereotype", exceptionDef.objGetList("stereotype"));
            this.writeElement("scope", exceptionDef.objGetValue("scope"));
            this.writeElement("visibility", exceptionDef.objGetValue("visibility"));
            if(this.derivedFeatures) {
                this.writeElement("content", exceptionDef.objGetList("content"));
            }
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write parameter definition.
     * 
     * @param parameterDef
     * @throws ServiceException
     */
    public void writeParameter(
        ModelElement_1_0 parameterDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Parameter");
            this.pw.writeAttribute("qualifiedName", parameterDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, parameterDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)parameterDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, parameterDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)parameterDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, parameterDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("container", parameterDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", parameterDef.getName());
                this.writeElement("qualifiedName", parameterDef.getQualifiedName());
            }
            this.writeElement("stereotype", parameterDef.objGetList("stereotype"));
            this.writeElement("direction", parameterDef.objGetValue("direction"));
            this.writeElement("multiplicity", parameterDef.getMultiplicity());
            this.writeElement("type", parameterDef.getType());
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write association definition.
     * 
     * @param associationDef
     * @throws ServiceException
     */
    public void writeAssociation(
        ModelElement_1_0 associationDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Association");
            this.pw.writeAttribute("qualifiedName", associationDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, associationDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)associationDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, associationDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)associationDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, associationDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("container", associationDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", associationDef.getName());
                this.writeElement("qualifiedName", associationDef.getQualifiedName());
            }
            this.writeElement("stereotype", associationDef.objGetList("stereotype"));
            this.writeElement("isAbstract", associationDef.isAbstract());
            if(this.derivedFeatures) {
                this.writeElement("feature", associationDef.objGetList("feature"));
                this.writeElement("content", associationDef.objGetList("content"));
                this.writeElement("allSupertype", associationDef.objGetList("allSupertype"));
                this.writeElement("subtype", associationDef.objGetList("subtype"));
                this.writeElement("allSubtype", associationDef.objGetList("allSubtype"));
            }
            this.writeElement("supertype", associationDef.objGetList("supertype"));
            this.writeElement("visibility", associationDef.objGetValue("visibility"));  
            this.writeElement("isDerived", associationDef.isDerived());
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write association end definition.
     * 
     * @param associationEndDef
     * @throws ServiceException
     */
    public void writeAssociationEnd(
        ModelElement_1_0 associationEndDef
        ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.AssociationEnd");
            this.pw.writeAttribute("qualifiedName", associationEndDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, associationEndDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)associationEndDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, associationEndDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)associationEndDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, associationEndDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("aggregation", associationEndDef.getAggregation());
            this.writeElement("isChangeable", associationEndDef.isChangeable());
            this.writeElement("isNavigable", associationEndDef.objGetValue("isNavigable"));
            this.writeElement("multiplicity", associationEndDef.getMultiplicity());
            this.writeElement("qualifierName", associationEndDef.objGetList("qualifierName"));
            this.writeElement("qualifierType", associationEndDef.objGetList("qualifierType"));
            this.writeElement("container", associationEndDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", associationEndDef.getName());
                this.writeElement("qualifiedName", associationEndDef.getQualifiedName());
            }
            this.writeElement("stereotype", associationEndDef.objGetList("stereotype"));
            this.writeElement("type", associationEndDef.getType());
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }
  
    /**
     * Write reference definition.
     * 
     * @param referenceDef
     * @throws ServiceException
     */
    public void writeReference(
        ModelElement_1_0 referenceDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Reference");
            this.pw.writeAttribute("qualifiedName", referenceDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, referenceDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)referenceDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, referenceDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)referenceDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, referenceDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("container", referenceDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", referenceDef.getName());
                this.writeElement("qualifiedName", referenceDef.getQualifiedName());
            }
            this.writeElement("stereotype", referenceDef.objGetList("stereotype"));
            this.writeElement("scope", referenceDef.objGetValue("scope"));
            this.writeElement("visibility", referenceDef.objGetValue("visibility"));
            this.writeElement("exposedEnd", referenceDef.getExposedEnd());
            this.writeElement("referencedEnd", referenceDef.getReferencedEnd());
            if(this.derivedFeatures) {
                this.writeElement("referencedEndIsNavigable", referenceDef.objGetValue("referencedEndIsNavigable"));
            }
            this.writeElement("isChangeable", referenceDef.isChangeable());
            this.writeElement("multiplicity", referenceDef.getMultiplicity());
            this.writeElement("type", referenceDef.getType());
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write class definition.
     * 
     * @param classDef
     * @throws ServiceException
     */
    public void writeClass(
        ModelElement_1_0 classDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Class");
            this.pw.writeAttribute("qualifiedName", classDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, classDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)classDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, classDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)classDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, classDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            this.writeElement("isSingleton", classDef.objGetValue("isSingleton"));
            if(this.derivedFeatures) {
                this.writeElement("feature", classDef.objGetList("feature"));
                this.writeElement("content", classDef.objGetList("content"));
            }
            this.writeElement("container", classDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", classDef.getName());
                this.writeElement("qualifiedName", classDef.getQualifiedName());
            }
            this.writeElement("stereotype", classDef.objGetList("stereotype"));
            this.writeElement("isAbstract", classDef.isAbstract());
            if(this.derivedFeatures) {
                this.writeElement("allSupertype", classDef.objGetList("allSupertype"));
                this.writeElement("subtype", classDef.objGetList("subtype"));
                this.writeElement("allSubtype", classDef.objGetList("allSubtype"));
            }
            this.writeElement("supertype", classDef.objGetList("supertype"));
            this.writeElement("visibility", classDef.objGetValue("visibility"));  
            if(this.derivedFeatures) {
                writeElement("compositeReference", classDef.objGetValue("compositeReference"));  
                writeElement("attribute", classDef.objGetMap("attribute"));
                writeElement("reference", classDef.objGetMap("reference"));
                writeElement("operation", classDef.objGetMap("operation"));
                writeElement("field", classDef.objGetMap("field"));
                writeElement("allFeature", classDef.objGetMap("allFeature"));
                writeElement("allFeatureWithSubtype", classDef.objGetMap("allFeatureWithSubtype"));
            }
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write structure type definition.
     * 
     * @param structDef
     * @throws ServiceException
     */
    public void writeStructureType(
        ModelElement_1_0 structDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.StructureType");
            this.pw.writeAttribute("qualifiedName", structDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, structDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)structDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, structDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)structDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, structDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            if(this.derivedFeatures) {
                this.writeElement("feature", structDef.objGetList("feature"));
                this.writeElement("content", structDef.objGetList("content"));
            }
            this.writeElement("container", structDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", structDef.getName());
                this.writeElement("qualifiedName", structDef.getQualifiedName());
            }
            this.writeElement("stereotype", structDef.objGetList("stereotype"));
            this.writeElement("isAbstract", structDef.isAbstract());
            if(this.derivedFeatures) {
                this.writeElement("allSupertype", structDef.objGetList("allSupertype"));
                this.writeElement("subtype", structDef.objGetList("subtype"));
                this.writeElement("allSubtype", structDef.objGetList("allSubtype"));
            }
            this.writeElement("supertype", structDef.objGetList("supertype"));
            this.writeElement("visibility", structDef.objGetValue("visibility"));  
            this.writeElement("compositeReference", structDef.objGetValue("compositeReference"));
            if(this.derivedFeatures) {
                writeElement("attribute", structDef.objGetMap("attribute"));
                writeElement("reference", structDef.objGetMap("reference"));
                writeElement("operation", structDef.objGetMap("operation"));
                writeElement("field", structDef.objGetMap("field"));
            }
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write alias type definition.
     * 
     * @param aliasTypeDef
     * @throws ServiceException
     */
    public void writeAliasType(
        ModelElement_1_0 aliasTypeDef
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.AliasType");
            this.pw.writeAttribute("qualifiedName", aliasTypeDef.getQualifiedName());
            this.pw.writeStartElement("_object");
            if(this.derivedFeatures) {
                this.writeElement(SystemAttributes.OBJECT_IDENTITY, aliasTypeDef.jdoGetObjectId().toXRI());
                this.writeElementAsDateTime(SystemAttributes.CREATED_AT, (Date)aliasTypeDef.objGetValue(SystemAttributes.CREATED_AT));
                this.writeElement(SystemAttributes.CREATED_BY, aliasTypeDef.objGetSet(SystemAttributes.CREATED_BY));
                this.writeElementAsDateTime(SystemAttributes.MODIFIED_AT, (Date)aliasTypeDef.objGetValue(SystemAttributes.MODIFIED_AT));
                this.writeElement(SystemAttributes.MODIFIED_BY, aliasTypeDef.objGetSet(SystemAttributes.MODIFIED_BY));
            }
            if(this.derivedFeatures) {
                this.writeElement("feature", aliasTypeDef.objGetList("feature"));
                this.writeElement("content", aliasTypeDef.objGetList("content"));
            }
            writeElement("container", aliasTypeDef.getContainer());
            if(this.derivedFeatures) {
                this.writeElement("name", aliasTypeDef.getName());
                this.writeElement("qualifiedName", aliasTypeDef.getQualifiedName());
            }
            this.writeElement("stereotype", aliasTypeDef.objGetList("stereotype"));
            this.writeElement("isAbstract", aliasTypeDef.isAbstract());
            if(this.derivedFeatures) {
                this.writeElement("allSupertype", aliasTypeDef.objGetList("allSupertype"));
                this.writeElement("subtype", aliasTypeDef.objGetList("subtype"));
                this.writeElement("allSubtype", aliasTypeDef.objGetList("allSubtype"));
            }
            this.writeElement("supertype", aliasTypeDef.objGetList("supertype"));
            this.writeElement("visibility", aliasTypeDef.objGetValue("visibility"));  
            this.writeElement("type", aliasTypeDef.getType());
            this.writeElement("compositeReference", aliasTypeDef.objGetValue("compositeReference"));
            this.pw.writeEndElement();
            this.pw.writeEmptyElement("_content");
            this.pw.writeEndElement();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write model header.
     * 
     * @param providerName
     * @param segmentName
     * @param schemaFileName
     */
    public void writeModelHeader(
        String providerName,
        String schemaFileName
    ) throws ServiceException {
        try {
            this.pw.writeStartDocument("UTF-8", "1.3");
            this.pw.setPrefix("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            this.pw.writeStartElement("org.openmdx.base.Authority");
            this.pw.writeAttribute("name", "org:omg:model1");
            this.pw.writeAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");            
            this.pw.writeAttribute("http://www.w3.org/2001/XMLSchema-instance", "noNamespaceSchemaLocation", schemaFileName);
            this.pw.writeEmptyElement("_object");
            this.pw.writeStartElement("_content");
            this.pw.writeStartElement("provider");
            this.pw.writeStartElement("org.openmdx.base.Provider");
            this.pw.writeAttribute("qualifiedName", providerName);
            this.pw.writeEmptyElement("_object");
            this.pw.writeStartElement("_content");
            this.pw.writeStartElement("segment");
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write model header.
     * 
     * @param segmentName the qualified segment name
     */
    public void writeSegmentHeader(
        String segmentName
    ) throws ServiceException {
        try {
            this.pw.writeStartElement("org.omg.model1.Segment");
            this.pw.writeAttribute("qualifiedName", segmentName);
            this.pw.writeEmptyElement("_object");
            this.pw.writeStartElement("_content");
            this.pw.writeStartElement("element");
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Write model end.
     */
    public void writeSegmentFooter(
    ) throws ServiceException {
        try {
            this.pw.writeEndElement(); // element
            this.pw.writeEndElement(); // _content
            this.pw.writeEndElement(); // org.omg.model1.Segment
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }
    
    /**
     * Write model end.
     */
    public void writeModelFooter(
    ) throws ServiceException {
        try {
            this.pw.writeEndElement(); // segment
            this.pw.writeEndElement(); // _content
            this.pw.writeEndElement(); // org.openmdx.base.Provider
            this.pw.writeEndElement(); // provider
            this.pw.writeEndElement(); // _content
            this.pw.writeEndElement(); // org.openmdx.base.Authority
            this.pw.close();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

}

//---------------------------------------------------------------------------

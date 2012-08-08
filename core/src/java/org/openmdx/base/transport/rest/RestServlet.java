/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RestServlet.java,v 1.27 2008/11/14 12:50:31 hburger Exp $
 * Description: RestServlet 
 * Revision:    $Revision: 1.27 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/11/14 12:50:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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

package org.openmdx.base.transport.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.jmi1.ContextCapable;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.SQLWildcards;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.persistence.cci.ConfigurableProperty;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.accessor.basic.spi.Model_1;
import org.openmdx.model1.code.Multiplicities;
import org.openmdx.model1.code.PrimitiveTypes;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.Datatypes;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * RestServlet
 *
 */
public class RestServlet
    extends HttpServlet {

    //-----------------------------------------------------------------------
    protected PersistenceManager getPersistenceManager(
        HttpServletRequest req 
    ) {
        String key = PersistenceManager.class.getName();
        HttpSession session = req.getSession();
        if(session == null) {
            session = req.getSession(true);
        }
        PersistenceManager pm = (PersistenceManager)req.getSession().getAttribute(key);
        if(pm == null) {
            req.getSession().setAttribute(
                key, 
                pm = this.pmf.getPersistenceManager(
                    req.getRemoteUser(), 
                    session.getId()
                )
            );
        }
        return pm;
    }

    //------------------------------------------------------------------------
    /**
     * Content handler which maps an XML encoded object to a RefObject.
     * A new object is created if no object is supplied, otherwise the values
     * of the supplied object are updated.
     */
    class RefObjectHandler extends DefaultHandler {
        
        public RefObjectHandler(
            RefPackage_1_0 refPackage,
            RefObject_1_0 refObj
        ) {
            this.refPackage = refPackage;
            this.refObj = refObj;
            this.dateFormat = RestServlet.dateFormats.get();
            this.localSecondFormat = RestServlet.localSecondFormats.get();
            this.secondFormat = RestServlet.secondFormats.get();
            this.localMillisecondFormat = RestServlet.localMillisecondFormats.get();
            this.millisecondFormat = RestServlet.millisecondFormats.get();            
        }

        public RefObject_1_0 getObject(
        ) {
            return this.refObj;
        }
        
        @Override
        public void characters(
            char[] ch, 
            int start, 
            int length
        ) throws SAXException {
            this.stringifiedValue.append(
                ch, 
                start, 
                length
            );
        }

        @SuppressWarnings("unchecked")
        @Override
        public void endElement(
            String uri, 
            String localName, 
            String name
        ) throws SAXException {
            try {
                // Object
                if(name.indexOf(".") > 0) {
                    // fully qualified class name
                }
                // Feature
                else {
                    String featureName = name;
                    String qualifiedClassName = this.refObj.refClass().refMofId();
                    ModelElement_1_0 classDef = RestServlet.this.model.getElement(qualifiedClassName);
                    ModelElement_1_0 featureDef = RestServlet.this.model.getFeatureDef(classDef, featureName, false);
                    String featureType = null;
                    if(featureDef == null) {
                        featureType = PrimitiveTypes.STRING;
                    }
                    else {
                        ModelElement_1_0 featureTypeDef = RestServlet.this.model.getElementType(
                            featureDef
                        );
                        featureType = (String)featureTypeDef.values("qualifiedName").get(0);
                    }
                    if(
                        !SystemAttributes.OBJECT_CLASS.equals(featureName) &&
                        !SystemAttributes.CREATED_AT.equals(featureName) &&
                        !SystemAttributes.MODIFIED_AT.equals(featureName) &&
                        !SystemAttributes.CREATED_BY.equals(featureName) &&
                        !SystemAttributes.MODIFIED_BY.equals(featureName)
                    ) {
                        // Map value
                        java.lang.Object newValue = null;
                        if(PrimitiveTypes.STRING.equals(featureType) || "string".equals(featureType)) {
                            newValue = this.stringifiedValue.toString();
                        }
                        else if(PrimitiveTypes.SHORT.equals(featureType) || "short".equals(featureType)) {
                            newValue = new Short(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.LONG.equals(featureType) || "long".equals(featureType)) {
                            newValue = new Long(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.INTEGER.equals(featureType) || "integer".equals(featureType)) {
                            newValue = new Integer(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.DECIMAL.equals(featureType) || "decimal".equals(featureType)) {
                            newValue = new BigDecimal(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.BOOLEAN.equals(featureType) || "boolean".equals(featureType)) {
                            newValue = new Boolean(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.OBJECT_ID.equals(featureType)) {
                            newValue = this.refPackage.refObject(
                                this.stringifiedValue.toString().trim()
                            );
                        }
                        else if(PrimitiveTypes.DATETIME.equals(featureType) || "dateTime".equals(featureType)) {
                            String v = this.stringifiedValue.toString().trim();
                            // Convert time zone from ISO 8601 to SimpleDateFormat 
                            int timePosition = v.indexOf('T');
                            if(v.endsWith("Z")){ 
                                v = v.substring(0, v.length() - 1) + "GMT+00:00";
                            } 
                            else {
                                int timeZonePosition = v.lastIndexOf('-');
                                if(timeZonePosition < timePosition) timeZonePosition = v.lastIndexOf('+');
                                if(
                                    (timeZonePosition > timePosition) &&
                                    !v.regionMatches(true, timeZonePosition-3, "GMT", 0, 3)
                                ) {
                                    v = v.substring(
                                        0, 
                                        timeZonePosition
                                    ) + "GMT" + v.substring(
                                        timeZonePosition
                                    );
                                }
                            }        
                            int timeLength = v.length() - timePosition - 1;
                            newValue = DateFormat.getInstance().format(
                                v.indexOf('.', timePosition) == -1 ? 
                                    (timeLength == 8 ? this.localSecondFormat.parse(v) : this.secondFormat.parse(v)) : 
                                    (timeLength == 12 ? this.localMillisecondFormat.parse(v) : this.millisecondFormat.parse(v))
                                );
                        }
                        else if(PrimitiveTypes.DATE.equals(featureType) || "date".equals(featureType)) {
                            String v = this.stringifiedValue.toString().trim();
                            newValue = DateFormat.getInstance().format(this.dateFormat.parse(v)).substring(0, 8);
                        }
                        else if(PrimitiveTypes.BINARY.equals(featureType)) {
                            newValue = Base64.decode(this.stringifiedValue.toString());
                        }              
                        else {
                            newValue = this.stringifiedValue.toString();
                        }
                        // Modify feature
                        Object value = this.refObj.refGetValue(featureName);
                        if(value instanceof Collection) {
                            Collection<Object> values = (Collection<Object>)value;
                            if(!this.processedFeatures.contains(featureName)) {
                                values.clear();
                            }
                            values.add(newValue);
                        }
                        else {
                            this.refObj.refSetValue(
                                featureName, 
                                newValue
                            );
                        }
                        this.processedFeatures.add(featureName);
                    }
                }
            }
            catch(Exception e) {
                throw new SAXException(e);
            }            
        }

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
                    new BasicException.Parameter("location", this.getLocationString(e))
                ).log()
            );
        }

        @Override
        public void startDocument(
        ) throws SAXException {
            this.stringifiedValue.setLength(0);
            this.processedFeatures.clear();
        }

        @Override
        public void startElement(
            String uri,
            String localName,
            String name,
            Attributes attributes
        ) throws SAXException {
            this.stringifiedValue.setLength(0);
            // Begin object
            if(name.indexOf(".") > 0) {
                this.processedFeatures.clear();
                if(this.refObj == null) {
                    String typeName = name.replace('.', ':');
                    this.refObj = (RefObject_1_0)this.refPackage.refClass(typeName).refCreateInstance(null);
                    this.refObj.refInitialize(false, false);
                }                
            }
        }
        
        private RefObject_1_0 refObj = null;
        private final RefPackage_1_0 refPackage;
        private final StringBuilder stringifiedValue = new StringBuilder();
        private final Set<String> processedFeatures = new HashSet<String>();
        private final DateFormat dateFormat;
        private final SimpleDateFormat localSecondFormat;
        private final DateFormat secondFormat;
        private final SimpleDateFormat localMillisecondFormat;
        private final DateFormat millisecondFormat;
        
    }
    
    //------------------------------------------------------------------------
    /**
     * Content handler which maps an XML encoded struct to a RefStruct.
     */
    class RefStructHandler extends DefaultHandler {

        public RefStructHandler(
            RefPackage_1_0 refPackage
        ) {
            this.refPackage = refPackage;
            this.dateFormat = RestServlet.dateFormats.get();
            this.localSecondFormat = RestServlet.localSecondFormats.get();
            this.secondFormat = RestServlet.secondFormats.get();
            this.localMillisecondFormat = RestServlet.localMillisecondFormats.get();
            this.millisecondFormat = RestServlet.millisecondFormats.get();            
        }

        public RefStruct_1_0 getStruct(
        ) {
            return this.refStruct;
        }
        
        @Override
        public void characters(
            char[] ch, 
            int start, 
            int length
        ) throws SAXException {
            this.stringifiedValue.append(
                ch, 
                start, 
                length
            );
        }

        @SuppressWarnings("unchecked")
        @Override
        public void endElement(
            String uri, 
            String localName, 
            String name
        ) throws SAXException {
            try {
                // Object
                if(name.indexOf(".") > 0) {
                    String structName = this.structName.replace('.', ':');
                    ModelElement_1_0 structDef = RestServlet.this.model.getElement(structName);
                    List<Object> params = new ArrayList<Object>();
                    for(Iterator<Object> i = structDef.values("content").iterator(); i.hasNext();) {
                        ModelElement_1_0 fieldDef = RestServlet.this.model.getElement(i.next());
                        params.add(this.struct.get(fieldDef.values("name").get(0)));
                    }
                    this.refStruct = (RefStruct_1_0)this.refPackage.refCreateStruct(
                        structName, 
                        params
                    );                    
                }
                // Feature
                else {
                    String fieldName = name;
                    ModelElement_1_0 structDef = RestServlet.this.model.getElement(this.structName.replace('.', ':'));
                    ModelElement_1_0 fieldDef = RestServlet.this.model.getFeatureDef(structDef, fieldName, false);
                    String fieldType = null;
                    String multiplicity = null;
                    if(fieldDef == null) {
                        fieldType = PrimitiveTypes.STRING;
                        multiplicity = Multiplicities.SINGLE_VALUE;
                    }
                    else {
                        ModelElement_1_0 fieldTypeDef = RestServlet.this.model.getElementType(
                            fieldDef
                        );
                        fieldType = (String)fieldTypeDef.values("qualifiedName").get(0);
                        multiplicity = (String)fieldDef.values("multiplicity").get(0);
                    }
                    if(
                        !SystemAttributes.OBJECT_CLASS.equals(fieldName) &&
                        !SystemAttributes.CREATED_AT.equals(fieldName) &&
                        !SystemAttributes.MODIFIED_AT.equals(fieldName) &&
                        !SystemAttributes.CREATED_BY.equals(fieldName) &&
                        !SystemAttributes.MODIFIED_BY.equals(fieldName)
                    ) {
                        // Map value
                        java.lang.Object newValue = null;
                        if(PrimitiveTypes.STRING.equals(fieldType) || "string".equals(fieldType)) {
                            newValue = this.stringifiedValue.toString();
                        }
                        else if(PrimitiveTypes.SHORT.equals(fieldType) || "short".equals(fieldType)) {
                            newValue = new Short(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.LONG.equals(fieldType) || "long".equals(fieldType)) {
                            newValue = new Long(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.INTEGER.equals(fieldType) || "integer".equals(fieldType)) {
                            newValue = new Integer(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.DECIMAL.equals(fieldType) || "decimal".equals(fieldType)) {
                            newValue = new BigDecimal(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.BOOLEAN.equals(fieldType) || "boolean".equals(fieldType)) {
                            newValue = new Boolean(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.OBJECT_ID.equals(fieldType)) {
                            newValue = new Path(this.stringifiedValue.toString().trim());
                        }
                        else if(PrimitiveTypes.DATETIME.equals(fieldType) || "dateTime".equals(fieldType)) {
                            String v = this.stringifiedValue.toString().trim();
                            // Convert time zone from ISO 8601 to SimpleDateFormat 
                            int timePosition = v.indexOf('T');
                            if(v.endsWith("Z")){ 
                                v = v.substring(0, v.length() - 1) + "GMT+00:00";
                            } 
                            else {
                                int timeZonePosition = v.lastIndexOf('-');
                                if(timeZonePosition < timePosition) timeZonePosition = v.lastIndexOf('+');
                                if(
                                    (timeZonePosition > timePosition) &&
                                    !v.regionMatches(true, timeZonePosition-3, "GMT", 0, 3)
                                ) {
                                    v = v.substring(
                                        0, 
                                        timeZonePosition
                                    ) + "GMT" + v.substring(
                                        timeZonePosition
                                    );
                                }
                            }        
                            int timeLength = v.length() - timePosition - 1;
                            newValue = DateFormat.getInstance().format(
                                v.indexOf('.', timePosition) == -1 ? 
                                    (timeLength == 8 ? this.localSecondFormat.parse(v) : this.secondFormat.parse(v)) : 
                                    (timeLength == 12 ? this.localMillisecondFormat.parse(v) : this.millisecondFormat.parse(v))
                                );
                        }
                        else if(PrimitiveTypes.DATE.equals(fieldType) || "date".equals(fieldType)) {
                            String v = this.stringifiedValue.toString().trim();
                            newValue = DateFormat.getInstance().format(this.dateFormat.parse(v)).substring(0, 8);
                        }
                        else if(PrimitiveTypes.BINARY.equals(fieldType)) {
                            newValue = Base64.decode(this.stringifiedValue.toString());
                        }              
                        else {
                            newValue = this.stringifiedValue.toString();
                        }
                        // Modify feature
                        if(
                            !this.struct.containsKey(fieldName) && 
                            !(Multiplicities.SINGLE_VALUE.equals(multiplicity) || Multiplicities.OPTIONAL_VALUE.equals(multiplicity))
                        ) {
                            this.struct.put(
                                fieldName, 
                                new ArrayList<Object>()
                            );
                        }
                        Object value = this.struct.get(fieldName);
                        if(value instanceof Collection) {
                            ((Collection)value).add(newValue);
                        }
                        else {
                            this.struct.put(
                                fieldName, 
                                newValue
                            );
                        }
                    }
                }
            }
            catch(Exception e) {
                throw new SAXException(e);
            }            
        }

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
                    new BasicException.Parameter("location", this.getLocationString(e))
                ).log()
            );
        }

        @Override
        public void startDocument(
        ) throws SAXException {
            this.stringifiedValue.setLength(0);
        }

        @Override
        public void startElement(
            String uri,
            String localName,
            String name,
            Attributes attributes
        ) throws SAXException {
            this.stringifiedValue.setLength(0);
            if(name.indexOf(".") > 0) {
                this.structName = name;
                this.struct.clear();
            }
        }
        
        private RefStruct_1_0 refStruct = null;
        private String structName = null;
        private Map<String,Object> struct = new HashMap<String,Object>();
        private final RefPackage_1_0 refPackage;
        private final StringBuilder stringifiedValue = new StringBuilder();
        private final DateFormat dateFormat;
        private final SimpleDateFormat localSecondFormat;
        private final DateFormat secondFormat;
        private final SimpleDateFormat localMillisecondFormat;
        private final DateFormat millisecondFormat;
        
    }
    
    //------------------------------------------------------------------------
    protected XMLReader getXMLReader(
        DefaultHandler handler        
    ) throws ServiceException {        
        XMLReader reader = xmlReaders.get();
        reader.setContentHandler(handler);
        return reader;
    }
    
    //-----------------------------------------------------------------------
    protected String getHRef(
        HttpServletRequest req,
        String ref
    ) {
       StringBuffer requestUrl = req.getRequestURL();
       String path = requestUrl.substring(0, requestUrl.indexOf(req.getServletPath()));
       String href = path + "/" + ref;
       return href;
    }
    
    //-----------------------------------------------------------------------
    protected String getHRef(
        HttpServletRequest req,
        Path identity
    ) {
        return this.getHRef(req, identity.toXri().substring(13));
    }
    
    //-----------------------------------------------------------------------
    protected Path getXri(
        HttpServletRequest req 
    ) {
        String xri = req.getServletPath();
        return new Path(
            "xri://@openmdx*" + xri.substring(1)
        );
    }
    
    //-----------------------------------------------------------------------
    protected void printValue(
        int indent,
        String tag,
        HttpServletRequest req,
        PrintWriter pw,
        Object value
    ) throws IOException {
        if(value == null) {
            // not present --> value == null
        }
        else {
            if(value instanceof ContextCapable) {
                pw.print(INDENTS[indent]);
                pw.print("<");
                pw.print(tag);
                pw.print(" href=\"");
                pw.print(this.getHRef(req, ((ContextCapable)value).refGetPath()));
                pw.println("\">");
                pw.print(((ContextCapable)value).refGetPath().toXRI().toString());
                pw.print("</");                
                pw.print(tag);
                pw.println(">");
            }
            else {
                pw.print(INDENTS[indent]);
                pw.print("<");
                pw.print(tag);
                pw.print(">");
                if(value instanceof String) {
                    pw.print("<![CDATA[");
                    pw.print(value.toString());
                    pw.print("]]>");
                }
                else if(value instanceof Date) {
                    pw.print(DateFormat.getInstance().format((Date)value));                    
                }
                else if(value instanceof byte[]) {
                    pw.print(Base64.encode((byte[])value));
                }
                else if(value instanceof BinaryLargeObject) {
                    BinaryLargeObject lob = (BinaryLargeObject)value;
                    Base64.encode(lob.getContent(), pw);
                }
                else if(value instanceof XMLGregorianCalendar) {
                    XMLGregorianCalendar cal = (XMLGregorianCalendar)value;
                    pw.print(cal.getYear());                    
                    pw.print(cal.getMonth() < 10 ? "0" + cal.getMonth(): cal.getMonth());                    
                    pw.print(cal.getDay() < 10 ? "0" + cal.getDay() : cal.getDay());                    
                }
                else {
                    pw.print(value.toString());
                }
                pw.print("</");                
                pw.print(tag);
                pw.println(">");
            }
        }
    }
    
    //-----------------------------------------------------------------------
    protected void printObject(
        int indent,
        HttpServletRequest req,
        PrintWriter pw,
        RefObject_1_0 object
    ) throws IOException {
        String tag = object.refClass().refMofId().replace(':', '.');
        pw.print(INDENTS[indent]);
        pw.print("<");
        pw.print(tag);
        pw.print(" id=\"");
        pw.print(object.refGetPath().getBase());
        pw.print("\" href=\"");
        pw.print(this.getHRef(req, object.refGetPath()));
        pw.println("\">");
        Set<String> fetchGroup = object.refDefaultFetchGroup();
        for(String feature: fetchGroup) {
            if(
                !"context".equals(feature) && 
                (feature.indexOf(":") == -1)
            ) {
                try {
                    Object value = object.refGetValue(feature);
                    if(value instanceof Collection) {
                        Collection<?> values = (Collection<?>)value;
                        // not present --> collection is empty
                        if(!values.isEmpty()) {
                            pw.print(INDENTS[indent+1]);
                            pw.print("<");
                            pw.print(feature);
                            pw.println(">");
                            for(
                                Iterator<?> i = values.iterator();
                                i.hasNext();
                            ) {
                                try {
                                    Object v = i.next();
                                    this.printValue(
                                        indent+2,
                                        "_item",
                                        req,
                                        pw,
                                        v
                                    );
                                }
                                catch(Exception e) {
                                    @SuppressWarnings("unused")
                                    ServiceException e0 = new ServiceException(
                                        e,
                                        BasicException.Code.DEFAULT_DOMAIN,
                                        BasicException.Code.PROCESSING_FAILURE,
                                        "Unable to retrieve feature value",
                                        new BasicException.Parameter("object.xri", object.refGetPath().toXRI()),
                                        new BasicException.Parameter("feature", feature)
                                    );
//                                    e0.log();                                    
                                }
                            }
                            pw.print(INDENTS[indent+1]);
                            pw.print("</");
                            pw.print(feature);
                            pw.println(">");
                        }
                    }
                    else {
                        this.printValue(
                            indent+1,
                            feature,
                            req,
                            pw,
                            value
                        );
                    }
                }
                catch(Exception e) {
                    @SuppressWarnings("unused")
                    ServiceException e0 = new ServiceException(
                        e,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "Unable to retrieve feature value",
                        new BasicException.Parameter("object.xri", object.refGetPath().toXRI()),
                        new BasicException.Parameter("feature", feature)
                    );
//                    e0.log();
                }
            }
        }
        pw.print(INDENTS[indent]);
        pw.print("</");
        pw.print(tag);
        pw.println(">");
    }
    
    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected void printStruct(
        int indent,
        String ref,
        HttpServletRequest req,
        PrintWriter pw,
        RefStruct_1_0 struct
    ) throws ServiceException {        
        StringBuilder structName = new StringBuilder();
        String separator = "";
        for(String c: (List<String>)struct.refTypeName()) {
            structName.append(separator).append(c);
            separator = ".";
        }
        pw.print(INDENTS[indent]);
        pw.print("<");
        pw.print(structName);
        pw.print(" id=\"result\" href=\"");
        pw.print(this.getHRef(req, ref));
        pw.println("\">");
        ModelElement_1_0 structDef = RestServlet.this.model.getElement(struct.refTypeName());
        Map<String,ModelElement_1_0> fieldDefs = RestServlet.this.model.getAttributeDefs(structDef, false, false);
        for(String field: fieldDefs.keySet()) {
            try {
                Object value = struct.refGetValue(field);
                if(value instanceof Collection) {
                    pw.print(INDENTS[indent+1]);
                    pw.print("<");
                    pw.print(field);
                    pw.print(">");
                    for(Object v: (Collection<?>)value) {
                        this.printValue(
                            indent+2,
                            "item",
                            req,
                            pw,
                            v
                        );
                    }
                    pw.print(INDENTS[indent+1]);
                    pw.print("</");
                    pw.print(field);
                    pw.println(">");
                }
                else {
                    this.printValue(
                        indent+1,
                        field,
                        req,
                        pw,
                        value
                    );
                }
            }
            catch(Exception e) {
                @SuppressWarnings("unused")
                ServiceException e0 = new ServiceException(
                    e,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.PROCESSING_FAILURE,
                    "Unable to retrieve feature value",
                    new BasicException.Parameter("struct", struct),
                    new BasicException.Parameter("feature", field)
                );
//                e0.log();
            }
        }
        pw.print(INDENTS[indent]);
        pw.print("</");
        pw.print(structName);
        pw.println(">");
    }
    
    //-----------------------------------------------------------------------
    protected void updateObject(
        RefPackage_1_0 refPackage,
        HttpServletRequest req,
        RefObject_1_0 object
    ) throws ServiceException {
        DefaultHandler handler = new RefObjectHandler(
            refPackage,
            object
        );
        XMLReader reader = this.getXMLReader(handler);
        try {
            reader.parse(
                new InputSource(req.getInputStream())
            );
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    protected Query getQuery(
        PersistenceManager pm,
        HttpServletRequest req,
        String defaultQueryType
    ) {        
        Query query = null;
        String queryType = req.getParameter("queryType") == null ?
            defaultQueryType :
            req.getParameter("queryType");
        try {
            String packageName = null;
            String className = null;
            if(queryType.indexOf(":") > 0) {
                packageName = queryType.substring(0, queryType.lastIndexOf(":")).replace(':', '.');
                className = queryType.substring(queryType.lastIndexOf(":") + 1).replace(':', '.');
            }
            else {
                packageName = "";
                className = queryType;
            }
            Class<?> queryClass = Classes.getApplicationClass(packageName + ".jmi1." + className);
            query = pm.newQuery(queryClass);
        }
        catch(Exception e) {
            SysLog.warning("Unable to create query for ", queryType);
        }
        if(query != null) {
            SQLWildcards sqlWildcards = new SQLWildcards('\\');            
            String queryString = req.getParameter("query");
            if(queryString != null) {
                int pos = 0;
                while(pos < queryString.length()) {
                    char c = queryString.charAt(pos);
                    if(Character.isWhitespace(c) || (c == ';')) {
                        pos++;
                    }
                    else {
                        int pos0 = queryString.indexOf("().", pos);
                        if(pos0 < 0) break;
                        String predicateName = queryString.substring(pos, pos0);       
                        int pos1 = queryString.indexOf("(", pos0 + 2);
                        if(pos1 < 0) break;
                        String operator = queryString.substring(pos0 + 3, pos1);
                        // Parse operands
                        List<Object> operands = new ArrayList<Object>();
                        int pos2 = pos1 + 1;
                        Class<?> valueClass = null;
                        c = queryString.charAt(pos2);
                        while((c != ')' && (pos2 < queryString.length()))) {
                            if(Character.isWhitespace(c) || (c == ',')) {
                                pos2++;
                            }
                            // String
                            else if(c == '"') {
                                StringBuilder s = new StringBuilder();
                                pos2++;
                                c = queryString.charAt(pos2);
                                while((c != '"') && (pos2 < queryString.length())) {
                                    s.append(c);
                                    pos2++;
                                    c = queryString.charAt(pos2);
                                    if(c == '\\') {
                                        pos2++;
                                        c = queryString.charAt(pos2);                                            
                                    }
                                }
                                if((valueClass == null) || (valueClass == String.class)) {
                                    operands.add(
                                        sqlWildcards.toJDO(s.toString())
                                    );
                                }
                                valueClass = null;
                                pos2++;
                            }
                            // Number, Date
                            else if((c == '-') || Character.isDigit(c)) {
                                StringBuilder s = new StringBuilder();
                                while(!Character.isWhitespace(c) && (c != ',') && (c != ')')&& (pos2 < queryString.length())) {
                                    s.append(c);
                                    pos2++;
                                    c = queryString.charAt(pos2);
                                }
                                if(valueClass != null) {
                                    operands.add(
                                        Datatypes.create(valueClass, s.toString())
                                    );
                                }
                                else {
                                    operands.add(new BigDecimal(s.toString()));
                                }
                                valueClass = null;
                            }
                            // Type spec
                            else if(c == ':') {
                                StringBuilder s = new StringBuilder();
                                pos2++;
                                c = queryString.charAt(pos2);
                                while((c != ':') && (pos2 < queryString.length())) {
                                    s.append(c);
                                    pos2++;
                                    c = queryString.charAt(pos2);
                                }
                                String type = s.toString();
                                if("string".equalsIgnoreCase(type)) {
                                    valueClass = String.class;
                                }
                                else if("date".equals(type)) {
                                    valueClass = XMLGregorianCalendar.class;
                                }
                                else if("datetime".equalsIgnoreCase(type)) {
                                    valueClass = Date.class;
                                }
                                else if("short".equalsIgnoreCase(type)) {
                                    valueClass = Short.class;
                                }
                                else if("int".equalsIgnoreCase(type) || "integer".equalsIgnoreCase(type)) {
                                    valueClass = Integer.class;
                                }
                                else if("long".equalsIgnoreCase(type)) {
                                    valueClass = Long.class;
                                }
                                else if("decimal".equalsIgnoreCase(type)) {
                                    valueClass = BigDecimal.class;
                                }
                                else if("duration".equalsIgnoreCase(type)) {
                                    valueClass = Duration.class;
                                }
                                pos2++;                                    
                            }
                            c = queryString.charAt(pos2);                            
                        }
                        try {
                            Method predicateMethod = query.getClass().getMethod(predicateName);
                            Object predicate = predicateMethod.invoke(query);
                            Class<?> predicateClass = predicateMethod.getReturnType();
                            Method operatorMethod = null;
                            int nArgs = 0;
                            // AnyTypePredicate
                            if("equalTo".equalsIgnoreCase(operator) && operands.size() == 1) {
                                operatorMethod = predicateClass.getMethod("equalTo", Object.class);
                                nArgs = 1;
                            }
                            else if("notEqualTo".equalsIgnoreCase(operator) && operands.size() == 1) {
                                operatorMethod = predicateClass.getMethod("notEqualTo", Object.class);
                                nArgs = 1;
                            }
                            else if("elementOf".equalsIgnoreCase(operator)) {
                                operatorMethod = predicateClass.getMethod("elementOf", Collection.class);
                                nArgs = -1;
                            }
                            else if("notAnElementOf".equalsIgnoreCase(operator)) {
                                operatorMethod = predicateClass.getMethod("notAnElementOf", Collection.class);
                                nArgs = -1;
                            }
                            // OptionalFeaturePredicate
                            else if("isNull".equalsIgnoreCase(operator) && (operands.size() == 0)) {
                                operatorMethod = predicateClass.getMethod(
                                    "isNull" 
                                );
                                nArgs = 0;
                            }                            
                            else if("isNonNull".equalsIgnoreCase(operator) && (operands.size() == 0)) {
                                operatorMethod = predicateClass.getMethod(
                                    "isNonNull" 
                                );
                                nArgs = 0;
                            }            
                            // BooleanTypePredicate
                            else if("isTrue".equalsIgnoreCase(operator) && (operands.size() == 0)) {
                                operatorMethod = predicateClass.getMethod(
                                    "isTrue" 
                                );
                                nArgs = 0;
                            }                            
                            else if("isFalse".equalsIgnoreCase(operator) && (operands.size() == 0)) {
                                operatorMethod = predicateClass.getMethod(
                                    "isFalse" 
                                );
                                nArgs = 0;
                            }            
                            // MatchableTypePredicate
                            else if("like".equalsIgnoreCase(operator)) {
                                operatorMethod = predicateClass.getMethod(
                                    "like", 
                                    Collection.class 
                                );
                                nArgs = -1;
                            }
                            else if("unlike".equalsIgnoreCase(operator)) {
                                operatorMethod = predicateClass.getMethod(
                                    "unlike", 
                                    Collection.class 
                                );
                                nArgs = -1;
                            }
                            else if("startsWith".equalsIgnoreCase(operator)) {
                                operatorMethod = predicateClass.getMethod(
                                    "startsWith", 
                                    Collection.class 
                                );
                                nArgs = -1;
                            }
                            else if("startsNotWith".equalsIgnoreCase(operator)) {
                                operatorMethod = predicateClass.getMethod(
                                    "startsNotWith", 
                                    Collection.class 
                                );
                                nArgs = -1;
                            }
                            else if("endsWith".equalsIgnoreCase(operator)) {
                                operatorMethod = predicateClass.getMethod(
                                    "endsWith", 
                                    Collection.class 
                                );
                                nArgs = -1;
                            }
                            else if("endsNotWith".equalsIgnoreCase(operator)) {
                                operatorMethod = predicateClass.getMethod(
                                    "endsNotWith", 
                                    Collection.class 
                                );
                                nArgs = -1;
                            }
                            // ComparableTypePredicate
                            else if("between".equalsIgnoreCase(operator) && operands.size() == 2) {
                                operatorMethod = predicateClass.getMethod(
                                    "between", 
                                    Comparable.class, 
                                    Comparable.class
                                );
                                nArgs = 2;
                            }
                            else if("outside".equalsIgnoreCase(operator) && operands.size() == 2) {
                                operatorMethod = predicateClass.getMethod(
                                    "outside", 
                                    Comparable.class, 
                                    Comparable.class
                                );
                                nArgs = 2;
                            }
                            else if("lessThan".equalsIgnoreCase(operator) && operands.size() == 1) {
                                operatorMethod = predicateClass.getMethod(
                                    "lessThan", 
                                    Comparable.class 
                                );
                                nArgs = 1;
                            }
                            else if("lessThanOrEqualTo".equalsIgnoreCase(operator) && operands.size() == 1) {
                                operatorMethod = predicateClass.getMethod(
                                    "lessThanOrEqualTo", 
                                    Comparable.class 
                                );
                                nArgs = 1;
                            }
                            else if("greaterThanOrEqualTo".equalsIgnoreCase(operator) && operands.size() == 1) {
                                operatorMethod = predicateClass.getMethod(
                                    "greaterThanOrEqualTo", 
                                    Comparable.class 
                                );
                                nArgs = 1;
                            }
                            else if("greaterThan".equalsIgnoreCase(operator) && operands.size() == 1) {
                                operatorMethod = predicateClass.getMethod(
                                    "greaterThan", 
                                    Comparable.class 
                                );
                                nArgs = 1;
                            }
                            else if("ascending".equalsIgnoreCase(operator) && operands.size() == 0) {
                                operatorMethod = predicateClass.getMethod("ascending");
                                nArgs = 0;
                            }
                            else if("descending".equalsIgnoreCase(operator) && operands.size() == 0) {
                                operatorMethod = predicateClass.getMethod("descending");
                                nArgs = 0;
                            }
                            if(operatorMethod != null) {
                                if(nArgs == 0) {
                                    operatorMethod.invoke(
                                        predicate
                                    );
                                }
                                else if(nArgs == 1) {
                                    operatorMethod.invoke(
                                        predicate, 
                                        operands.get(0)
                                    );                                        
                                }
                                else if(nArgs == 2) {
                                    operatorMethod.invoke(
                                        predicate, 
                                        operands.get(0), 
                                        operands.get(1)
                                    );                                        
                                }
                                else {
                                    operatorMethod.invoke(
                                        predicate, 
                                        operands
                                    );                                        
                                }
                            }
                        }
                        catch(Exception e) {
                            SysLog.warning("Unknown predicate for query", Arrays.asList(queryType, predicateName));
                        }
                        pos = pos2 + 1;
                    }
                }
            }
        }
        return query;
    }
    
    //-----------------------------------------------------------------------
    protected RefObject_1_0 newObject(
        RefPackage_1_0 refPackage,
        HttpServletRequest req 
    ) throws ServiceException {
        RefObjectHandler handler = new RefObjectHandler(
            refPackage,
            null
        );
        XMLReader reader = this.getXMLReader(handler);
        try {
            reader.parse(
                new InputSource(req.getInputStream())
            );
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
        return handler.getObject();
    }

    //-----------------------------------------------------------------------
    protected RefStruct_1_0 newStruct(
        RefPackage_1_0 refPackage,
        HttpServletRequest req 
    ) throws ServiceException {
        RefStructHandler handler = new RefStructHandler(
            refPackage
        );
        XMLReader reader = this.getXMLReader(handler);
        try {
            reader.parse(
                new InputSource(req.getInputStream())
            );
        }
        catch(Exception e) {
            throw new ServiceException(e);
        }
        return handler.getStruct();
    }

    //-----------------------------------------------------------------------
    protected String uuidAsString(
    ) {
        return UUIDConversion.toUID(this.uuidGenerator.next());
    }

    //-----------------------------------------------------------------------
    protected void handleException(
        Exception e,
        PersistenceManager pm,
        HttpServletResponse resp,
        boolean isLocalTransaction
    ) {
        ServiceException e0 = new ServiceException(e);
        if(isLocalTransaction) {
           try {
               pm.currentTransaction().rollback();
           } catch(Exception e1) {}
        }
        if(e0.getExceptionCode() == BasicException.Code.NOT_FOUND) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);                
        }
        else if(e0.getExceptionCode() == BasicException.Code.AUTHORIZATION_FAILURE) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);                
        }
        else {
            e0.log();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }        
    }
    
    //-----------------------------------------------------------------------
    private PersistenceManagerFactory getPersistenceManagerFactory(
    ) throws NamingException, ServiceException {
        Map<String,String> properties = new HashMap<String,String>();
        properties.put(
            ConfigurableProperty.ConnectionFactoryName.qualifiedName(), 
            "java:comp/env/ejb/EntityManagerFactory"
        );
        properties.put(
            ConfigurableProperty.PersistenceManagerFactoryClass.qualifiedName(), 
            "org.openmdx.base.accessor.jmi1.AccessorFactory_2"
        );
        return JDOHelper.getPersistenceManagerFactory(properties);
    }
      
    //-----------------------------------------------------------------------
    @Override
    public void init(
        ServletConfig config
    ) throws ServletException {
        super.init(config);
        try {
            int i = 0;
            List<String> modelPackages = new ArrayList<String>(); 
            while(getInitParameter("modelPackage[" + i + "]") != null) {
                modelPackages.add(
                    getInitParameter("modelPackage[" + i + "]")
                );
                i++;
            }
            try {
                this.model = new Model_1();
                this.model.addModels(modelPackages);
            }
            catch(Exception e) {
                System.out.println("Can not initialize model repository " + e.getMessage());
                System.out.println(new ServiceException(e).getCause());
            }
            this.pmf = this.getPersistenceManagerFactory();
        }
        catch(Exception e) {
            new ServiceException(e).log();
            throw new ServletException(e);            
        }
    }

    //-----------------------------------------------------------------------
    @Override
    protected void doDelete(
        HttpServletRequest req, 
        HttpServletResponse resp
    ) throws ServletException, IOException {
        PersistenceManager pm = this.getPersistenceManager(req);
        boolean isLocalTransaction = true;
        try {
            ;
            isLocalTransaction = !pm.currentTransaction().isActive();
            if(isLocalTransaction) {
                pm.currentTransaction().begin();
            }
            Path xri = this.getXri(req);
            RefObject_1_0 refObj = (RefObject_1_0)pm.getObjectById(xri);
            refObj.refDelete();
            if(isLocalTransaction) {
                pm.currentTransaction().commit();
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        catch(Exception e) {
            this.handleException(
                e,
                pm, 
                resp, 
                isLocalTransaction
            );
        }
    }

    //-----------------------------------------------------------------------
    @Override
    protected void doGet(
        HttpServletRequest req, 
        HttpServletResponse resp
    ) throws ServletException, IOException {
        PersistenceManager pm = this.getPersistenceManager(req);
        // Reads are non-transactional. If a unit of work is active then
        // the read is executed within the current unit of work
        try {
            Path xri = this.getXri(req);
            resp.setContentType("text/xml");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter pw = resp.getWriter();
            pw.println("<?xml version=\"1.0\"?>");
            // Object
            if(xri.size() % 2 == 1) {
                RefObject_1_0 refObj = (RefObject_1_0)pm.getObjectById(xri);
                this.printObject(
                    0,
                    req,
                    pw,
                    refObj
                );
            }
            // Collection
            else {
                RefObject_1_0 parentObj = (RefObject_1_0)pm.getObjectById(xri.getParent());
                String feature = xri.getBase();
                ModelElement_1_0 featureDef = this.model.getFeatureDef(
                    this.model.getElement(parentObj.refClass().refMofId()), 
                    feature, 
                    true
                );
                ModelElement_1_0 referencedType = featureDef == null ?
                    null :
                    this.model.getElement(featureDef.values("type").get(0));
                Query query = this.getQuery(
                    pm, 
                    req,
                    referencedType == null ? 
                        null : 
                        (String)referencedType.values("qualifiedName").get(0)
                );
                if(query != null) {
                    Collection<?> candidates = (Collection<?>)parentObj.refGetValue(feature);
                    query.setCandidates(candidates);
                    List<?> objects = (List<?>)query.execute();                    
                    pw.print("<");
                    pw.print(feature);
                    pw.print(" href=\"");
                    pw.print(this.getHRef(req, xri));
                    pw.println("\">");
                    pw.print(INDENTS[1]);
                    // query
                    pw.print("<query><![CDATA[");
                    pw.println(query.toString());
                    pw.println("]]></query>");
                    int position = req.getParameter("position") == null ?
                        0 :
                        Integer.valueOf(req.getParameter("position"));
                    int size = req.getParameter("size") == null ?
                        25 :
                        Integer.valueOf(req.getParameter("size"));
                    int n = 0;
                    // position
                    pw.print(INDENTS[1]);
                    pw.print("<position>");
                    pw.print(position);
                    pw.println("</position>");
                    // size
                    pw.print(INDENTS[1]);
                    pw.print("<size>");
                    pw.print(size);
                    pw.println("</size>");
                    // resultSet
                    pw.print(INDENTS[1]);                    
                    pw.println("<resultSet>");
                    ListIterator<?> i = null;
                    for(
                        i = objects.listIterator(position); 
                        i.hasNext();
                    ) {
                        Object obj = i.next();
                        if(obj instanceof RefObject_1_0) {
                            this.printObject(
                                2,
                                req,
                                pw, 
                                (RefObject_1_0)obj
                            );
                        }
                        n++;
                        if(n > size) break;
                    }
                    pw.print(INDENTS[1]);
                    pw.println("</resultSet>");
                    pw.print(INDENTS[1]);
                    pw.print("<hasMore>");
                    pw.print(i == null || !i.hasNext() ? "false" : "true");
                    pw.println("</hasMore>");
                    pw.print("</");
                    pw.print(feature);
                    pw.println(">");
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
                else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);                    
                }
            }
        }
        catch(Exception e) {
            this.handleException(
                e, 
                pm, 
                resp, 
                false
            );
            pm.evictAll();
        }
    }

    //-----------------------------------------------------------------------
    @Override
    protected void doPost(
        HttpServletRequest req, 
        HttpServletResponse resp
    ) throws ServletException, IOException {
        PersistenceManager pm = this.getPersistenceManager(req);
        try {
            Path xri = this.getXri(req);
            boolean hasError = false;
            RefObject_1_0 targetObj = null;
            String feature = null;
            String qualifier = null;
            if(xri.size() % 2 == 1) {
                qualifier = xri.getBase();
                feature = xri.getParent().getBase();
                targetObj = (RefObject_1_0)pm.getObjectById(xri.getParent().getParent());
            }
            else {
                qualifier = null;
                feature = xri.getBase();
                targetObj = (RefObject_1_0)pm.getObjectById(xri.getParent());
            }
            ModelElement_1_0 classDef = this.model.getElement(targetObj.refClass().refMofId());
            ModelElement_1_0 featureDef = this.model.getFeatureDef(
                classDef, 
                feature, 
                false
            );
            // Operation
            if((featureDef != null) && this.model.isOperationType(featureDef)) {
                RefStruct_1_0 params = this.newStruct(
                    (RefPackage_1_0)targetObj.refOutermostPackage(),
                    req
                );
                pm.currentTransaction().begin();
                RefStruct_1_0 result = (RefStruct_1_0)targetObj.refInvokeOperation(
                    feature, 
                    Arrays.asList(params)
                );
                pm.currentTransaction().commit();
                PrintWriter pw = resp.getWriter();
                resp.setContentType("text/xml");
                pw.println("<?xml version=\"1.0\"?>");                        
                this.printStruct(
                     0,
                     this.uuidAsString(),
                     req,
                     pw,
                     result
                );
            }
            // Create object
            else if((featureDef != null) && this.model.isReferenceType(featureDef)) {
                RefContainer container = (RefContainer)targetObj.refGetValue(feature);
                RefObject_1_0 newObj = this.newObject(
                    (RefPackage_1_0)targetObj.refOutermostPackage(), 
                    req
                );
                pm.currentTransaction().begin();
                container.refAdd(
                    QualifierType.REASSIGNABLE,
                    qualifier == null ? this.uuidAsString() : qualifier, 
                    newObj
                );
                pm.currentTransaction().commit();
                PrintWriter pw = resp.getWriter();
                resp.setContentType("text/xml");
                pw.println("<?xml version=\"1.0\"?>");                        
                this.printObject(
                    0, 
                    req, 
                    pw, 
                    newObj
                );
            }
            else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                hasError = true;
            }
            if(!hasError) {
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        }
        catch(Exception e) {
            this.handleException(
                e, 
                pm, 
                resp, 
                true
            );
        }
    }

    //-----------------------------------------------------------------------
    @Override
    protected void doPut(
        HttpServletRequest req, 
        HttpServletResponse resp
    ) throws ServletException, IOException {
        PersistenceManager pm = this.getPersistenceManager(req);
        try {
            Path xri = this.getXri(req);
            RefObject_1_0 refObj = (RefObject_1_0)pm.getObjectById(xri);
            pm.currentTransaction().begin();
            this.updateObject(
                (RefPackage_1_0)refObj.refOutermostPackage(),
                req,
                refObj
            );
            pm.currentTransaction().commit();
            PrintWriter pw = resp.getWriter();
            resp.setContentType("text/xml");
            pw.println("<?xml version=\"1.0\"?>");                                    
            this.printObject(
                0, 
                req, 
                pw, 
                refObj
            );
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        catch(Exception e) {
            this.handleException(
                e, 
                pm, 
                resp, 
                true
            );
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -4403464830407956377L;
    
    protected static final String[] INDENTS = {"", "\t", "\t\t", "\t\t\t"};
    
    protected Model_1_0 model = null;
    protected PersistenceManagerFactory pmf = null;
    protected final UUIDGenerator uuidGenerator = UUIDs.getGenerator();
    private static ThreadLocal<XMLReader> xmlReaders = new ThreadLocal<XMLReader>() {
        protected synchronized XMLReader initialValue() {
            try {
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                XMLReader reader = parser.getXMLReader();
                reader.setFeature("http://xml.org/sax/features/namespaces", true);
                reader.setFeature("http://xml.org/sax/features/validation", false);
                return reader;
            }
            catch(Exception e) {
                new ServiceException(e).log();
                return null;
            }
        }
    };
    static ThreadLocal<DateFormat> secondFormats = new ThreadLocal<DateFormat>() {
        protected synchronized DateFormat initialValue() {
            return DateFormat.getInstance(
                "yyyy-MM-dd'T'HH:mm:ssz"
            );
        }
    };
    static ThreadLocal<DateFormat> millisecondFormats = new ThreadLocal<DateFormat>() {
        protected synchronized DateFormat initialValue() {
            return DateFormat.getInstance(
                "yyyy-MM-dd'T'HH:mm:ss.SSSz"
            );
        }
    };
    static ThreadLocal<DateFormat> dateFormats = new ThreadLocal<DateFormat>() {
        protected synchronized DateFormat initialValue() {
            return DateFormat.getInstance(
                "yyyy-MM-dd"
            );
        }
    }; 
    static ThreadLocal<SimpleDateFormat> localSecondFormats = new ThreadLocal<SimpleDateFormat>() {
        protected synchronized SimpleDateFormat initialValue() {
            return new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss"
            );
        }
    }; 
    static ThreadLocal<SimpleDateFormat> localMillisecondFormats = new ThreadLocal<SimpleDateFormat>() {
        protected synchronized SimpleDateFormat initialValue() {
            return new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS"
            );
        }
    };
    
}
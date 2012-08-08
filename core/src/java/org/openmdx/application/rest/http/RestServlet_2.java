/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: RestServlet_2.java,v 1.12 2009/06/07 22:26:59 wfro Exp $
 * Description: REST Servlet 
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/07 22:26:59 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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
package org.openmdx.application.rest.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.LocalTransaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.RecordFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openmdx.application.rest.spi.InboundConnectionFactory_2;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.base.text.format.DateFormat;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.spi2.Datatypes;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * REST Servlet
 */
public class RestServlet_2 extends HttpServlet {

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected IndexedRecord singletonList(
        Object value
    ) throws ResourceException{
        RecordFactory factory = this.connectionFactory.getRecordFactory();
        if(factory instanceof ExtendedRecordFactory) {
            return ((ExtendedRecordFactory)factory).singletonIndexedRecord(
                Multiplicities.LIST,
                null,
                value
            );
        } else {
            IndexedRecord record = factory.createIndexedRecord(
                Multiplicities.LIST
            );
            record.add(value);
            return record;
        }
    }
    
    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected MappedRecord singletonMap(
        String key,
        Object value
    ) throws ResourceException{
        RecordFactory factory = this.connectionFactory.getRecordFactory();
        if(factory instanceof ExtendedRecordFactory) {
            return ((ExtendedRecordFactory)factory).singletonMappedRecord(
                Multiplicities.MAP,
                null,
                key,
                value
            );
        } else {
            MappedRecord record = factory.createMappedRecord(
                Multiplicities.MAP
            );
            record.put(key, value);
            return record;
        }
    }
    
    //-----------------------------------------------------------------------
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
        String featureName
    ) throws ServiceException{
        return this.model.getFeatureDef(
            this.model.getElement(typeName),
            featureName,
            false
        );
    }
    
    //-----------------------------------------------------------------------
    /**
     * Retrieve the feature type
     * 
     * @param featureDef feature definition
     * 
     * @return the feature type
     * 
     * @throws ServiceException
     */
    protected String getFeatureType(
        ModelElement_1_0 featureDef
    ) throws ServiceException{
        return featureDef == null ? PrimitiveTypes.STRING : (String) this.model.getElementType(
            featureDef
        ).objGetValue(
            "qualifiedName"
        );
    }
    
    //-----------------------------------------------------------------------
    protected Interaction getInteraction(
        HttpServletRequest req 
    ) throws ServletException {
        try {
            String key = Connection.class.getName();
            HttpSession session = req.getSession(true);
            Connection connection = (Connection)req.getSession().getAttribute(key);
            if(connection == null) {
                req.getSession().setAttribute(
                    key, 
                    connection = this.connectionFactory.getConnection(
                        InboundConnectionFactory_2.newConnectionSpec(
                            req.getRemoteUser(), 
                            session.getId()
                        )
                    )
                );
            }
            return connection.createInteraction();
        } catch (ResourceException exception) {
            throw new ServletException(
                "Interaction acquisition failure",
                exception
            );
        }
    }

    //-----------------------------------------------------------------------
    protected void closeInteraction(
        Interaction interaction
    ) throws ServletException {
        try {
            interaction.close();
        } catch (ResourceException exception) {
            throw new ServletException(
                "Interaction close failure",
                exception
            );
        }
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
        String ref = identity.toXRI();
        return this.getHRef(req, ref.substring(15));
    }
    
    //-----------------------------------------------------------------------
    protected Path getXri(
        HttpServletRequest req 
    ) {
       String uri = req.getServletPath().substring(1);
        return new Path(
            uri.startsWith("@openmdx") ? uri : "xri:@openmdx*" + uri
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
        else if(value instanceof Path) {
            pw.print(INDENTS[indent]);
            pw.print("<");
            pw.print(tag);
            pw.print(" href=\"");
            pw.print(this.getHRef(req, (Path)value));
            pw.println("\">");
            pw.print(((Path)value).toXRI());
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
    
    //-----------------------------------------------------------------------
    protected void printRecord(
        int indent,
        HttpServletRequest req,
        PrintWriter pw,
        MappedRecord record,
        String id,
        String href
    ) throws IOException {
        String tag = record.getRecordName().replace(':', '.');
        pw.print(INDENTS[indent]);
        pw.print("<");
        pw.print(tag);
        pw.print(" id=\"");
        pw.print(id);
        pw.print("\" href=\"");
        pw.print(href);
        pw.println("\">");
        for(Object e : record.entrySet()) {
            Map.Entry<?,?> entry = (Map.Entry<?,?>) e;
            String feature = (String)entry.getKey();
            Object value = entry.getValue();
            try {
                if(value instanceof Collection<?>) {
                    Collection<?> values = (Collection<?>)value;
                    // not present --> collection is empty
                    if(values.isEmpty()) {
                        pw.print(INDENTS[indent+1]);
                        pw.print("<");
                        pw.print(feature);
                        pw.println(" />");
                    } 
                    else {
                        pw.print(INDENTS[indent+1]);
                        pw.print("<");
                        pw.print(feature);
                        pw.println(">");
                        for(Object v : values) try {
                            this.printValue(
                                indent+2,
                                "_item",
                                req,
                                pw,
                                v
                            );
                        } 
                        catch(Exception exception) {
                            if(SysLog.isTraceOn()) SysLog.trace(
                                "Collection element print failure", 
                                new ServiceException(
                                    exception,
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.PROCESSING_FAILURE,
                                    "Unable to retrieve feature value",
                                    new BasicException.Parameter("href", href),
                                    new BasicException.Parameter("feature", feature)
                                )
                           );
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
            catch(Exception exception) {
                if(SysLog.isTraceOn()) SysLog.trace(
                    "Collection element print failure", 
                    new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PROCESSING_FAILURE,
                        "Unable to retrieve feature value",
                        new BasicException.Parameter("href", href),
                        new BasicException.Parameter("feature", feature)
                    )
               );
            }
        }
        pw.print(INDENTS[indent]);
        pw.print("</");
        pw.print(tag);
        pw.println(">");
    }

    //-----------------------------------------------------------------------
    protected MappedRecord newValueRecord(
        HttpServletRequest req
    ) throws ServiceException {
        RecordHandler handler = new RecordHandler();
        try {
            this.getXMLReader(handler).parse(
                new InputSource(req.getInputStream())
            );
            return handler.getValues();
        } catch(Exception e) {
            throw new ServiceException(e);
        }
    }

    //-----------------------------------------------------------------------
    protected String uuidAsString(
    ) {
        return UUIDConversion.toUID(this.uuidGenerator.next());
    }

    //-----------------------------------------------------------------------
    protected void handleException(
        Exception e,
        Interaction interaction,
        HttpServletResponse resp,
        boolean isLocalTransaction
    ) {
        ServiceException e0 = new ServiceException(e);
        if(isLocalTransaction) {
           try {
               interaction.getConnection().getLocalTransaction().rollback();
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
    @Override
    public void init(
        ServletConfig config
    ) throws ServletException {
        super.init(config);
        try {
            try {
                this.model = Model_1Factory.getModel();
            }
            catch(Exception e) {
                System.out.println("Can not initialize model repository " + e.getMessage());
                System.out.println(new ServiceException(e).getCause());
            }
            this.connectionFactory = InboundConnectionFactory_2.newInstance(
                "java:comp/env/ejb/EntityManagerFactory"
            );
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
        Interaction interaction = this.getInteraction(req);
        InteractionSpecs interactionSpecs = this.interactionSpecs;
        try {
            Path xri = this.getXri(req);
            LocalTransaction transaction = interaction.getConnection().getLocalTransaction();
            transaction.begin();
            interaction.execute(
                interactionSpecs.DELETE,
                singletonList(
                    xri.toXRI()
                )
            );
            transaction.commit();
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        catch(Exception e) {
            this.handleException(
                e,
                interaction, 
                resp, 
                true
            );
        } finally {
            closeInteraction(interaction);
        }
    }

    //-----------------------------------------------------------------------
    @Override
    protected void doGet(
        HttpServletRequest req, 
        HttpServletResponse resp
    ) throws ServletException, IOException {
        Interaction interaction = this.getInteraction(req);
        InteractionSpecs interactionSpecs = this.interactionSpecs;
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
                IndexedRecord input = singletonList(xri.toXRI());
                IndexedRecord output = (IndexedRecord) interaction.execute(
                    interactionSpecs.GET, 
                    input
                );
                if(output == null) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);                    
                } 
                else if(output.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);                    
                } 
                else {
                    this.printRecord(
                        0, 
                        req, 
                        pw, 
                        ObjectHolder_2Facade.newInstance((MappedRecord)output.get(0)).getValue(),
                        xri.getBase(),
                        this.getHRef(req, xri)
                    );
                    resp.setStatus(HttpServletResponse.SC_OK);                    
                }
            }
            // Collection
            else {
                Query_2Facade inputFacade = Query_2Facade.newInstance();
                String queryType = req.getParameter("queryType");
                String query = req.getParameter("query"); 
                inputFacade.setPath(xri);
                inputFacade.setQueryType(
                    queryType == null ? (String)this.model.getTypes(xri.getChild(":*"))[2].objGetValue("qualifiedName") : queryType
                );
                inputFacade.setQuery(query);
                String position = req.getParameter("position");
                inputFacade.setPosition(
                    position == null ? Integer.valueOf(DEFAULT_POSITION) : Integer.valueOf(position)
                );
                String size = req.getParameter("size");
                int limit = size == null ? DEFAULT_SIZE : Integer.parseInt(size);
                inputFacade.setSize(Integer.valueOf(limit + 1));
                IndexedRecord output = (IndexedRecord) interaction.execute(
                    interactionSpecs.GET, 
                    inputFacade.getDelegate()
                );
                if(output == null) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);                    
                } 
                else {
                    String feature = xri.getBase();
                    pw.print("<");
                    pw.print(feature);
                    pw.print(" href=\"");
                    pw.print(this.getHRef(req, xri));
                    pw.println("\">");
                    pw.print(INDENTS[1]);
                    // query
                    pw.print("<query><![CDATA[");
                    pw.println(query);
                    pw.println("]]></query>");
                    // position
                    pw.print(INDENTS[1]);
                    pw.print("<position>");
                    pw.print(inputFacade.getPosition());
                    pw.println("</position>");
                    // size
                    pw.print(INDENTS[1]);
                    pw.print("<size>");
                    pw.print(limit);
                    pw.println("</size>");
                    // resultSet
                    pw.print(INDENTS[1]);                    
                    pw.println("<resultSet>");
                    int count = 0;
                    boolean hasMore = false;
                    ResultSet: for(Object o : output){
                        if(++count > limit){
                            hasMore = true;
                            break ResultSet;
                        }
                        ObjectHolder_2Facade outputFacade = ObjectHolder_2Facade.newInstance((MappedRecord) o); 
                        Path objectId = outputFacade.getPath();
                        this.printRecord(
                            2, 
                            req, 
                            pw, 
                            outputFacade.getValue(), 
                            objectId.getBase(), 
                            getHRef(req, objectId)
                        );
                    }
                    pw.print(INDENTS[1]);
                    pw.println("</resultSet>");
                    pw.print(INDENTS[1]);
                    pw.print("<hasMore>");
                    pw.print(hasMore);
                    pw.println("</hasMore>");
                    pw.print("</");
                    pw.print(feature);
                    pw.println(">");
                    resp.setStatus(HttpServletResponse.SC_OK);
                }
            }
        } catch(Exception e) {
            this.handleException(
                e, 
                interaction, 
                resp, 
                false
            );
            req.getSession().removeAttribute(Connection.class.getName());
        } finally {
            closeInteraction(interaction);
        }
    }

    //-----------------------------------------------------------------------
    @Override
    protected void doPost(
        HttpServletRequest req, 
        HttpServletResponse resp
    ) throws ServletException, IOException {
        Interaction interaction = this.getInteraction(req);
        InteractionSpecs interactionSpecs = this.interactionSpecs;
        try {
            LocalTransaction transaction = interaction.getConnection().getLocalTransaction();
            Path xri = this.getXri(req);
            if(xri.size() % 2 == 0) {
                xri.add(this.uuidAsString());
            }
            transaction.begin();
            MappedRecord valueRecord = newValueRecord(req);
            boolean operation = this.model.isStructureType(valueRecord.getRecordName());
            MappedRecord input = singletonMap(
                xri.toXRI(),
                valueRecord
            );
            IndexedRecord output = (IndexedRecord) interaction.execute(
                operation ? interactionSpecs.INVOKE : interactionSpecs.CREATE, 
                input
            );
            transaction.commit();
            if(output == null || output.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } 
            else {
                resp.setContentType("text/xml");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter pw = resp.getWriter();
                pw.println("<?xml version=\"1.0\"?>");                                    
                for(Object o : output){
                    MappedRecord record = (MappedRecord) o;
                    if(operation) {
                        this.printRecord(
                            0, 
                            req, 
                            pw, 
                            record,
                            "result",
                            this.getHRef(req, uuidAsString())
                        );
                    } 
                    else {
                        this.printRecord(
                            0, 
                            req, 
                            pw, 
                            ObjectHolder_2Facade.newInstance(record).getValue(),
                            xri.getBase(),
                            this.getHRef(req, xri)
                        );
                    }
                }
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } catch(Exception e) {
            this.handleException(
                e, 
                interaction, 
                resp, 
                true
            );
        } finally {
            closeInteraction(interaction);
        }
    }

    //-----------------------------------------------------------------------
    @Override
    protected void doPut(
        HttpServletRequest req, 
        HttpServletResponse resp
    ) throws ServletException, IOException {
        Interaction interaction = this.getInteraction(req);
        InteractionSpecs interactionSpecs = this.interactionSpecs;
        try {
            LocalTransaction transaction = interaction.getConnection().getLocalTransaction();
            transaction.begin();
            MappedRecord valueRecord = this.newValueRecord(req);
            ObjectHolder_2Facade facade = ObjectHolder_2Facade.newInstance();
            facade.setValue(valueRecord);
            facade.setPath(this.getXri(req));
// TODO     facade.setVersion(version);
            IndexedRecord output = (IndexedRecord) interaction.execute(
                interactionSpecs.PUT, 
                facade.getDelegate()
            );
            transaction.commit();
            if(output == null || output.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } 
            else {
                resp.setContentType("text/xml");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter pw = resp.getWriter();
                pw.println("<?xml version=\"1.0\"?>");                                    
                for(Object o : output){
                    ObjectHolder_2Facade objectHolder = ObjectHolder_2Facade.newInstance((MappedRecord) o);
                    Path objectId = objectHolder.getPath();
                    this.printRecord(
                        0, 
                        req, 
                        pw, 
                        objectHolder.getValue(),
                        objectId.getBase(),
                        this.getHRef(req, objectId)
                    );
                }
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } 
        catch(Exception e) {
            this.handleException(
                e, 
                interaction, 
                resp, 
                true
            );
        } 
        finally {
            closeInteraction(interaction);
        }
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private static final long serialVersionUID = -4403464830407956377L;
    protected static final String[] INDENTS = {"", "\t", "\t\t", "\t\t\t"};
    private static final ThreadLocal<XMLReader> xmlReaders = new ThreadLocal<XMLReader>() {
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
    static final DateFormat secondFormat = DateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssz");
    static final DateFormat millisecondFormat = DateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSz");
    static final DateFormat dateFormat = DateFormat.getInstance("yyyy-MM-dd");
    static final DateFormat localSecondFormat = DateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss",null,true);
    static final DateFormat localMillisecondFormat = DateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS",null,true);
    
    private boolean retainValues = true;
    private final int DEFAULT_POSITION = 0;
    private final int DEFAULT_SIZE = 25;
    protected Model_1_0 model = null;
    protected ConnectionFactory connectionFactory = null;
    private final InteractionSpecs interactionSpecs = InteractionSpecs.newRestInteractionSpecs(
        null, // ignorable principal chain
        this.retainValues
    );
    protected final UUIDGenerator uuidGenerator = UUIDs.getGenerator();

    //------------------------------------------------------------------------
    /**
     * Content handler which maps an XML encoded values to JCA records
     */
    class RecordHandler extends DefaultHandler {

        /**
         * Constructor 
         *
         * @param recordFactory
         */
        protected RecordHandler(
        ) {
        }

        /**
         * 
         */
        private MappedRecord values = null;
        
        /**
         * 
         */
        private final StringBuilder stringifiedValue = new StringBuilder();

        /**
         * Retrieve the interaction's input record
         * 
         * @return the interaction's input record
         */
        MappedRecord getValues(
        ){
            return this.values;
        }

        protected ModelElement_1_0 getFeatureDef(
            String featureName
        ) throws ServiceException{
            return RestServlet_2.this.getFeatureDef(
                this.values.getRecordName(),
                featureName
            );
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
                if(name.indexOf('.') > 0) {
                    // fully qualified class name
                }
                // Feature
                else {
                    String featureName = name;
                    ModelElement_1_0 featureDef = getFeatureDef(featureName);
                    String featureType = getFeatureType(featureDef);
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
                            newValue = this.stringifiedValue.toString();
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
                            newValue = v.indexOf('.', timePosition) == -1 ? 
                                (timeLength == 8 ? RestServlet_2.localSecondFormat.parse(v) : RestServlet_2.secondFormat.parse(v)) : 
                                (timeLength == 12 ? RestServlet_2.localMillisecondFormat.parse(v) : RestServlet_2.millisecondFormat.parse(v));
                        }
                        else if(PrimitiveTypes.DATE.equals(featureType) || "date".equals(featureType)) {
                            String v = this.stringifiedValue.toString().trim();
                            newValue = Datatypes.create(
                                XMLGregorianCalendar.class,
                                DateFormat.getInstance().format(RestServlet_2.dateFormat.parse(v)).substring(0, 8)
                            );
                        }
                        else if(PrimitiveTypes.BINARY.equals(featureType)) {
                            newValue = Base64.decode(this.stringifiedValue.toString());
                        }              
                        else if((featureType != null) && RestServlet_2.this.model.isClassType(featureType)) {
                            newValue = new Path(this.stringifiedValue.toString());
                        }                        
                        else {
                            newValue = this.stringifiedValue.toString();
                        }
                        // Modify feature
                        String multiplicity = (String)featureDef.objGetValue("multiplicity");
                        if (
                            Multiplicities.SINGLE_VALUE.equals(multiplicity) ||
                            Multiplicities.OPTIONAL_VALUE.equals(multiplicity)
                        ) {
                            this.values.put(featureName, newValue);
                        } 
                        else if(
                            Multiplicities.LIST.equals(multiplicity) ||
                            Multiplicities.SET.equals(multiplicity)
                        ) {
                            IndexedRecord target = (IndexedRecord)this.values.get(featureName);
                            if(target == null) {
                                this.values.put(
                                    featureName,
                                    target = RestServlet_2.this.connectionFactory.getRecordFactory().createIndexedRecord(multiplicity)
                                );
                            }
                            target.add(newValue);
                        } 
                        else {
                            SysLog.warning("Unsupported multiplicity, feature ignored", multiplicity);
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
            BasicException.log(null);
            throw Throwables.log(
                BasicException.initHolder(
                    new SAXException(
                        "XML parse error",
                        BasicException.newEmbeddedExceptionStack(
                            e, 
                            BasicException.Code.DEFAULT_DOMAIN, 
                            BasicException.Code.PROCESSING_FAILURE,
                            new BasicException.Parameter("message", e.getMessage()),
                            new BasicException.Parameter("location", this.getLocationString(e))
                        )
                    )
                )
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
            if(name.indexOf('.') > 0) try {
                // Begin object or struct
                this.values = RestServlet_2.this.connectionFactory.getRecordFactory().createMappedRecord(
                    name.replace('.', ':')
                );
            } catch (ResourceException exception) {
                throw new SAXException(exception);
            }
        }
        
    }
    
}
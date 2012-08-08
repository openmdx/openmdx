/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.resource.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.uses.org.apache.commons.httpclient.HttpClient;
import org.openmdx.uses.org.apache.commons.httpclient.methods.GetMethod;
import org.openmdx.uses.org.apache.servicemix.http.jetty.JettyServer;
import org.openmdx.uses.org.apache.servicemix.http.jetty.SslParameters;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.openmdx.uses.org.apache.servicemix.jbi.jaxp.StringSource;
import org.w3c.dom.Document;

public class HttpWsdlTest extends TestCase {

    protected JettyServer server;
    protected static Definition wsdlDefintion;
    
    //-----------------------------------------------------------------------
    protected void setUp() throws Exception {
        this.server = new JettyServer();
        this.server.init();
        this.server.start();
    }
    
    //-----------------------------------------------------------------------
    protected void tearDown() throws Exception {
        this.server.stop();
    }

    //-----------------------------------------------------------------------
    protected Definition createDefinition(
        boolean rpc
    ) throws WSDLException {
        Definition def = WSDLFactory.newInstance().newDefinition();
        def.setTargetNamespace("http://porttype.test");
        def.addNamespace("tns", "http://porttype.test");
        def.addNamespace("xsd", "http://www.w3.org/2000/10/XMLSchema");
        def.addNamespace("w", "uri:hello");
        Message inMsg = def.createMessage();
        inMsg.setQName(new QName("http://porttype.test", "InMessage"));
        inMsg.setUndefined(false);
        Part part1 = def.createPart();
        part1.setName("part1");
        if (rpc) {
            part1.setTypeName(new QName("http://www.w3.org/2000/10/XMLSchema", "int"));
        } else {
            part1.setElementName(new QName("uri:hello", "world"));
        }
        inMsg.addPart(part1);
        Part part2 = def.createPart();
        part2.setName("part2");
        part2.setElementName(new QName("uri:hello", "world"));
        inMsg.addPart(part2);
        def.addMessage(inMsg);
        Message outMsg = def.createMessage();
        outMsg.setQName(new QName("http://porttype.test", "OutMessage"));
        outMsg.setUndefined(false);
        Part part3 = def.createPart();
        part3.setName("part3");
        part3.setElementName(new QName("uri:hello", "world"));
        outMsg.addPart(part3);
        def.addMessage(outMsg);
        PortType type = def.createPortType();
        type.setUndefined(false);
        type.setQName(new QName("http://porttype.test", "MyConsumerInterface"));
        Operation op = def.createOperation();
        op.setName("Hello");
        Input in = def.createInput();
        in.setMessage(inMsg);
        op.setInput(in);
        op.setUndefined(false);
        Output out = def.createOutput();
        out.setMessage(outMsg);
        op.setOutput(out);
        type.addOperation(op);
        def.addPortType(type);
        WSDLFactory.newInstance().newWSDLWriter().writeWSDL(def, System.err);
        return def;
    }
    
    //-----------------------------------------------------------------------
    public static class EchoServlet extends HttpServlet {
        
        private static final long serialVersionUID = 3534913260801386509L;

        protected void doGet(
            HttpServletRequest request, 
            HttpServletResponse response
        ) throws ServletException, IOException {
            this.doPost(request, response);
        }
    
        protected void doPost(
            HttpServletRequest request, 
            HttpServletResponse response
        ) throws ServletException, IOException {
            try {
                WSDLFactory.newInstance().newWSDLWriter().writeWSDL(
                    HttpWsdlTest.wsdlDefintion, 
                    response.getOutputStream()
                );                
                response.setContentType("text/xml");                
                response.setStatus(HttpServletResponse.SC_OK);
            }
            catch(Exception e) {
                throw new ServletException(e);
            }
        }
    }
    
    //-----------------------------------------------------------------------
    protected void testWSDL(
        int portNumber
    ) throws Exception {
        String locationURI = "http://localhost:" + portNumber + "/Service";
        Object echoServlet = this.server.createContext(
            locationURI,
            EchoServlet.class.getName(),
            new SslParameters(),
            null,
            null
        );        
        GetMethod get = new GetMethod(locationURI + "/?wsdl");
        int state = new HttpClient().executeMethod(get);
        assertEquals(HttpServletResponse.SC_OK, state);
        Document doc = (Document) new SourceTransformer().toDOMNode(
            new StringSource(get.getResponseBodyAsString())
        );
        get.releaseConnection();
        
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader reader = factory.newWSDLReader();
        Definition definition;
        definition = reader.readWSDL(locationURI + "/?wsdl", doc);
        assertNotNull(definition);
        assertNotNull(definition.getImports());
        assertNotNull(definition.getBindings());
        assertNotNull(definition.getServices());
        assertNotNull(definition.getPortTypes());
        // Some info to output
        System.out.println("\nBindings");
        System.out.println(definition.getBindings());
        System.out.println("\nImports");
        System.out.println(definition.getImports());
        System.out.println("\nServices");
        System.out.println(definition.getServices());
        this.server.remove(
            echoServlet
        );
    }
    
    //-----------------------------------------------------------------------
    public void testWithNonStandaloneWsdlDoc(
    ) throws Exception {
        wsdlDefintion = this.createDefinition(false);
        this.testWSDL(8192);
    }
    
    //-----------------------------------------------------------------------
    public void testWithNonStandaloneWsdlRpc(
    ) throws Exception {
        wsdlDefintion = this.createDefinition(true);
        this.testWSDL(8193);
    }
    
    //-----------------------------------------------------------------------
    public void testWithExistingBinding(
    ) throws Exception {
        String uri = Classes.getSystemResource("bound-wsdl.wsdl").toString();
        wsdlDefintion = WSDLFactory.newInstance().newWSDLReader().readWSDL(uri);
        this.testWSDL(8194);
    }
        
}

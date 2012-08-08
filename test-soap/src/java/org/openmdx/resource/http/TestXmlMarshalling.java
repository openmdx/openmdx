/*
 * ====================================================================
 * Project:     openMDX/Test SOAP, http://www.openmdx.org/
 * Name:        $Id: TestXmlMarshalling.java,v 1.2 2007/03/22 15:33:58 wfro Exp $
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/22 15:33:58 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland; France Telecom, France
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
 * 
 */
package org.openmdx.resource.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;

import junit.framework.TestCase;

import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;
import org.openmdx.base.transport.jca.DeletePersistentInteractionSpec;
import org.openmdx.base.transport.jca.FlushInteractionSpec;
import org.openmdx.base.transport.jca.MakePersistentInteractionSpec;
import org.openmdx.base.transport.jca.OpenMdxInteractionSpec;
import org.openmdx.base.transport.jca.OperationInteractionSpec;
import org.openmdx.base.transport.jca.QueryInteractionSpec;
import org.openmdx.base.transport.jca.RetrieveInteractionSpec;
import org.openmdx.kernel.text.StringBuilders;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class TestXmlMarshalling extends TestCase {

    //-----------------------------------------------------------------------
    public static class Person {
        
      public Person(
          String firstname,
          String lastname
      ) {
          this.firstname = firstname;
          this.lastname = lastname;
      }
      
      public void setPhone(
          PhoneNumber phone
      ) {
          this.phone = phone;
      }

      public void setFax(
          PhoneNumber fax
      ) {
          this.fax = fax;
      }
      
      private String firstname;
      private String lastname;
      private PhoneNumber phone;
      private PhoneNumber fax;
      // ... constructors and methods
    }
    
    public static class PhoneNumber {
        
      public PhoneNumber(
          int code,
          String number
      ) {
          this.code = code;
          this.number = number;
      }
      
      private int code;
      private String number;
      // ... constructors and methods
    }    
    
    //-----------------------------------------------------------------------
    protected void setUp(
    ) throws Exception {
      this.factory = Records.getRecordFactory();
      this.map2 = new TreeMap();
      this.map2.put("A","a");
      this.map2.put("C","c");
      this.map3 = new TreeMap(map2);
      this.map3.put("B","b");
    }
    
    //-----------------------------------------------------------------------
    protected void tearDown() throws Exception {
    }
    
    //-----------------------------------------------------------------------    
    public void testPerson(
    ) throws Exception {
        XStream xstream = new XStream(new DomDriver());
        xstream.alias("person", Person.class);
        xstream.alias("phonenumber", PhoneNumber.class);        
        Person joe = new Person("Joe", "Walnes");
        joe.setPhone(new PhoneNumber(123, "1234-456"));
        joe.setFax(new PhoneNumber(123, "9999-999"));
        String xml = xstream.toXML(joe);
        System.err.println(xml);
    }

    //-----------------------------------------------------------------------    
    public void testMappedRecord(
    ) throws Exception {
        XStream xstream = new XStream(new DomDriver());

        String xmlr2m = null;
        String xmlr2md = null;
        String xmlr23 = null;
        for(int i = 0; i < 1; i++) {
            MappedRecord r2m = factory.createMappedRecord("r2");
            r2m.putAll(map2);
            xmlr2m = xstream.toXML(r2m);
    
            // VariableSizeMappedRecord with description
            MappedRecord r2md = factory.createMappedRecord("r2");
            r2md.setRecordShortDescription("Record 2");
            r2md.putAll(map2);
            xmlr2md = xstream.toXML(r2md);
    
            // Nested VariableSizeMappedRecords
            MappedRecord r23 = factory.createMappedRecord("r23");
            r23.setRecordShortDescription("Nested Utilities");
            r23.put(new Integer(2),r2md);
            r23.put(new Integer(3),factory.createMappedRecord("r3",null,map3));
            xmlr23 = xstream.toXML(r23);
        }
        System.out.println(xmlr2m);
        System.out.println(xmlr2md);
        System.out.println(xmlr23);
        
    }

    //-----------------------------------------------------------------------    
    public void testInteraction(
    ) throws Exception {
        XStream xstream = new XStream(new DomDriver());
        
        // Map all subtypes of OpenMdxInteractionSpec to the alias 
        // OpenMdxInteractionSpec.class.getName. 
        // Unmarshaling <code>OpenMdxInteractionSpec</code>s for all subtypes. The
        // type of interaction can be determined with <code>getFunctionName()</code>
        xstream.alias(OpenMdxInteractionSpec.class.getName(), DeletePersistentInteractionSpec.class);
        xstream.alias(OpenMdxInteractionSpec.class.getName(), FlushInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), MakePersistentInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), OperationInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), QueryInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), RetrieveInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), OpenMdxInteractionSpec.class);        

        DeletePersistentInteractionSpec i0 = new DeletePersistentInteractionSpec();
        i0.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
        i0.setObjectId("aaaa/bbbb/cccc/dddd/");
        System.out.println(xstream.toXML(i0));
        OpenMdxInteractionSpec i = (OpenMdxInteractionSpec)xstream.fromXML(xstream.toXML(i0));
        
        FlushInteractionSpec i1 = new FlushInteractionSpec();
        i1.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
        i1.setObjectId("aaaa/bbbb/cccc/dddd/");
        System.out.println(xstream.toXML(i1));
        i = (OpenMdxInteractionSpec)xstream.fromXML(xstream.toXML(i1));

        MakePersistentInteractionSpec i2 = new MakePersistentInteractionSpec();
        i2.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
        i2.setObjectId("aaaa/bbbb/cccc/dddd/");
        System.out.println(xstream.toXML(i2));
        i = (OpenMdxInteractionSpec)xstream.fromXML(xstream.toXML(i2));

        OperationInteractionSpec i3 = new OperationInteractionSpec("createAddress");
        i3.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
        i3.setObjectId("aaaa/bbbb/cccc/dddd/");
        System.out.println(xstream.toXML(i3));
        i = (OpenMdxInteractionSpec)xstream.fromXML(xstream.toXML(i3));

        QueryInteractionSpec i4 = new QueryInteractionSpec();        
        i4.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);        
        i4.setObjectId("aaaa/bbbb/cccc/dddd/");
        i4.setDeletePersistent(true);
        i4.setFetchSize(new Integer(1000));
        i4.setRangeFrom(10);
        i4.setRangeTo(100);
        System.out.println(xstream.toXML(i4));
        i = (OpenMdxInteractionSpec)xstream.fromXML(xstream.toXML(i4));

        RetrieveInteractionSpec i5 = new RetrieveInteractionSpec();
        i5.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
        i5.setObjectId("aaaa/bbbb/cccc/dddd/");
        System.out.println(xstream.toXML(i5));
        i = (OpenMdxInteractionSpec)xstream.fromXML(xstream.toXML(i5));
        
    }

    //-----------------------------------------------------------------------    
    private static String readLine(
        Reader reader
    ) throws IOException {
        StringBuilder s = StringBuilders.newStringBuilder();
        char c = (char)reader.read();
        while(c != '\n' && (c != '\r')) {
            s.append(c);
            c = (char)reader.read();
        }
        return s.toString();
    }
    
    //-----------------------------------------------------------------------
    /**
     * ObjectOutputStreamWriter does not close writer. It must be closed explicitely.
     */    
    private static class ObjectOutputStreamWriter extends FilterWriter {
        
        public ObjectOutputStreamWriter(
            Writer writer
        ) {
            super(writer);
        }
        
        public void close(
        ) {
        }        
    }
    
    //-----------------------------------------------------------------------
    /**
     * ObjectInputStreamReader does not close writer. It must be closed explicitely.
     */    
    private static class ObjectInputStreamReader extends FilterReader {
        
        public ObjectInputStreamReader(
            Reader reader
        ) {
            super(reader);
            this.reader = reader;
        }

        private void assertBuffer(
        ) throws IOException {
            if(this.eof) return;
            if(
                (this.buffer == null) ||
                (this.pos >= this.buffer.length())
            ) {
                this.buffer = readLine(this.reader);
                this.pos = 0;                
            }
            this.eof = "</object-stream>".equalsIgnoreCase(this.buffer.trim());            
        }
        
        @Override
        public void mark(int readAheadLimit) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean markSupported() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int read() throws IOException {
            this.assertBuffer();
            if(this.eof) {
                return -1;
            }
            else {
                return this.buffer.charAt(this.pos);
            }
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            this.assertBuffer();
            if(this.eof) {
                return -1;
            }
            else {
                len = java.lang.Math.min(this.buffer.length() - this.pos, len);
                System.arraycopy(this.buffer.toCharArray(), this.pos, cbuf, off, len);
                this.pos += len;
                return len;
            }
        }

        @Override
        public boolean ready() throws IOException {
            this.assertBuffer();
            return !this.eof;
        }

        @Override
        public void reset() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public long skip(long n) throws IOException {
            throw new UnsupportedOperationException();
        }

        public void close(
        ) {
        }
        
        private boolean eof = false;
        private String buffer = null;
        private int pos = -1;
        private final Reader reader;
        
    }
    
    //-----------------------------------------------------------------------    
    public void testStreamingInteraction(
    ) throws Exception {
        HierarchicalStreamDriver driver = new com.thoughtworks.xstream.io.xml.StaxDriver();
        XStream xstream = new XStream(driver);
        
        // Map all subtypes of OpenMdxInteractionSpec to the alias 
        // OpenMdxInteractionSpec.class.getName. 
        // Unmarshaling <code>OpenMdxInteractionSpec</code>s for all subtypes. The
        // type of interaction can be determined with <code>getFunctionName()</code>
        xstream.alias(OpenMdxInteractionSpec.class.getName(), DeletePersistentInteractionSpec.class);
        xstream.alias(OpenMdxInteractionSpec.class.getName(), FlushInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), MakePersistentInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), OperationInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), QueryInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), RetrieveInteractionSpec.class);        
        xstream.alias(OpenMdxInteractionSpec.class.getName(), OpenMdxInteractionSpec.class);        

        long start = System.currentTimeMillis();
        for(int i = 0; i < 100; i++) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(out);
            
            // Write SOAP envelope begin
            writer.println("<soapenv:Envelope");
            writer.println("    xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"");
            writer.println("    xmlns:ws=\"http://ws.location.services.cardinal.com/\">");
            writer.println("    <soapenv:Body>");
            writer.flush();
            
            ObjectOutputStream objectOutput = xstream.createObjectOutputStream(
                new ObjectOutputStreamWriter(writer)
            );
            
            DeletePersistentInteractionSpec i0 = new DeletePersistentInteractionSpec();
            i0.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
            i0.setObjectId("aaaa/bbbb/cccc/dddd/");
            objectOutput.writeObject(i0);
            
            FlushInteractionSpec i1 = new FlushInteractionSpec();
            i1.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
            i1.setObjectId("aaaa/bbbb/cccc/dddd/");
            objectOutput.writeObject(i1);
    
            MakePersistentInteractionSpec i2 = new MakePersistentInteractionSpec();
            i2.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
            i2.setObjectId("aaaa/bbbb/cccc/dddd/");
            objectOutput.writeObject(i2);
    
            OperationInteractionSpec i3 = new OperationInteractionSpec("createAddress");
            i3.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
            i3.setObjectId("aaaa/bbbb/cccc/dddd/");
            objectOutput.writeObject(i3);
    
            QueryInteractionSpec i4 = new QueryInteractionSpec();        
            i4.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);        
            i4.setObjectId("aaaa/bbbb/cccc/dddd/");
            i4.setDeletePersistent(true);
            i4.setFetchSize(new Integer(1000));
            i4.setRangeFrom(10);
            i4.setRangeTo(100);
            objectOutput.writeObject(i4);
    
            RetrieveInteractionSpec i5 = new RetrieveInteractionSpec();
            i5.setInteractionVerb(InteractionSpec.SYNC_RECEIVE);
            i5.setObjectId("aaaa/bbbb/cccc/dddd/");
            objectOutput.writeObject(i5);
    
            objectOutput.close();
            
            // Write SOAP envelope end
            writer.println();
            writer.println("    </soapenv:Body>");
            writer.println("</soapenv:Envelope>");
            
            writer.close();
            
            // Read SOAP envelope begin
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            InputStreamReader reader = new InputStreamReader(in);
            String s = readLine(reader);
            while(!"<soapenv:Body>".equalsIgnoreCase(s.trim())) {
                s = readLine(reader);
            }
            ObjectInputStream objectInput = xstream.createObjectInputStream(
                new ObjectInputStreamReader(reader)
            );
            OpenMdxInteractionSpec ispec = null;        
            ispec = (OpenMdxInteractionSpec)objectInput.readObject();
            ispec = (OpenMdxInteractionSpec)objectInput.readObject();
            ispec = (OpenMdxInteractionSpec)objectInput.readObject();
            ispec = (OpenMdxInteractionSpec)objectInput.readObject();
            ispec = (OpenMdxInteractionSpec)objectInput.readObject();
            ispec = (OpenMdxInteractionSpec)objectInput.readObject();      
            objectInput.close();
    
            // Read SOAP envelope end
            s = readLine(reader);
            while(!"</soapenv:Envelope>".equalsIgnoreCase(s.trim())) {
                s = readLine(reader);
            }
            reader.close();
        }
        long end = System.currentTimeMillis();
        System.out.println("testStreamingInteraction time(ms)=" + (end - start));
        
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private Map map2;
    private Map map3;
    private ExtendedRecordFactory factory;
    
}

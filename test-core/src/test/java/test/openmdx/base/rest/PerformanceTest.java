/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Performance Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2021, OMEX AG, Switzerland
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
package test.openmdx.base.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.resource.cci.MappedRecord;
import javax.xml.datatype.DatatypeFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.w3c.cci2.MutableDatatypeFactory;
import org.w3c.format.DateTimeFormat;

/**
 * Performance Test
 */
@SuppressWarnings("unchecked")
public class PerformanceTest {

    Map<Path, ValueObject> valueObjects;

    Map<Path, Map<String, Object>> valueMaps;

    Map<Path, MappedRecord> dataproviderObjects;

    /**
     * A Datatype Factory Instance
     */
    protected DatatypeFactory xmlDatatypeFactory;

    protected File file;

    protected OutputStream channel;

    static final boolean IN_MEMORY = Boolean.TRUE.booleanValue(); // avoids dead code warning

    static final int RUNS = 100;

    static final int OBJECTS = 100;

    static long sendReference = 0L;

    static long receiveReference = 0L;

    static int sizeReference = 0;

    @BeforeEach
    public void setUp(
    ) throws Exception {
        this.xmlDatatypeFactory = MutableDatatypeFactory.xmlDatatypeFactory();
        this.valueObjects = new HashMap<Path, ValueObject>();
        this.valueMaps = new HashMap<Path, Map<String, Object>>();
        this.dataproviderObjects = new HashMap<Path, MappedRecord>();
        for (int i = 0; i < OBJECTS; i++) {
            ValueObject o = new ValueObject();
            Map<String, Object> m = new HashMap<String, Object>();
            Object_2Facade facade = Object_2Facade.newInstance(
                new Path(
                    new String[] {
                        "test.openmdx.base.object.ValueObject",
                        "provider",
                        "JUnit",
                        "segment",
                        "org*openmdx*test",
                        "object",
                        String.valueOf(i)
                    }
                ),
                "test:openmdx:base:object:TestObject"
            );
            o.id = facade.getPath();
            MappedRecord d = facade.getValue();
            m.put("identity", o.identity = facade.getPath().toXRI());
            d.put("identity", o.identity);
            m.put("field1", o.field1 = new Date());
            d.put("field1", o.field1);
            m.put("field2", o.field2 = Collections.singleton("JUnit"));
            d.put("field2", o.field2);
            m.put("field3", o.field3 = o.field1);
            d.put("field3", o.field3);
            m.put("field4", o.field4 = o.field2);
            d.put("field4", o.field4);
            m.put(
                "field5",
                o.field5 = 1000000 +
                    i
            );
            d.put("field5", o.field5);
            m.put(
                "field6",
                o.field6 = "Name " +
                    i
            );
            d.put("field6", o.field6);
            m.put(
                "field7",
                o.field7 = "Value " +
                    i
            );
            d.put("field7", o.field7);
            this.valueObjects.put(facade.getPath(), o);
            this.valueMaps.put(facade.getPath(), m);
            this.dataproviderObjects.put(facade.getPath(), d);
        }
    }

    /**
     * Serialize Value Objects Together
     * 
     * @throws Exception
     */
    @Test
    public void testSerializeValueObjectsTogether(
    ) throws Exception {
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                ObjectOutputStream out = new ObjectOutputStream(storage);
                out.writeObject(this.valueObjects);
                out.flush();
            }
            logSend(begin);
        }
        {
            try (final InputStream storage = newInputStream()) {
                final long begin = System.currentTimeMillis();
                for (int i = 0; i < RUNS; i++) {
                    if (IN_MEMORY)
                        ((ByteArrayInputStream) storage).reset();
                    try (final ObjectInputStream in = new ObjectInputStream(storage)) {
                        this.valueObjects = (Map<Path, ValueObject>) in.readObject();
                    }
                }
                logReceive(begin);
            }
        }
    }

    /**
     * Serialize Value Maps Together
     * 
     * @throws Exception
     */
    @Test
    public void testSerializeValueMapsTogether(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()) {
            final long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                ObjectOutputStream out = new ObjectOutputStream(storage);
                out.writeObject(this.valueMaps);
                out.flush();
            }
            logSend(begin);
        }
    }

    /**
     * Serialize Value Maps Together
     * 
     * @throws Exception
     */
    @Test
    public void testSerializeDataproviderObjectsTogether(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()) {
            final long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                ObjectOutputStream out = new ObjectOutputStream(storage);
                out.writeObject(this.dataproviderObjects);
                out.flush();
            }
            logSend(begin);
        }
    }

    /**
     * Serialize Value Objects Separately
     * 
     * @throws Exception
     */
    @Test
    public void testSerializeValueObjectsSeparately(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()) {
            final long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                ObjectOutputStream out = new ObjectOutputStream(storage);
                for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                    Map.Entry<Path, ValueObject> e = j.next();
                    out.reset();
                    out.writeObject(e.getKey());
                    out.writeObject(e.getValue());
                }
                out.flush();
            }
            logSend(begin);
        }
    }

    /**
     * Serialize Value Objects Separately
     * 
     * @throws Exception
     */
    @Test
    public void testSerializeDataproviderObjectsSeparately(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()) {
            final long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                ObjectOutputStream out = new ObjectOutputStream(storage);
                for (Iterator<Map.Entry<Path, MappedRecord>> j = this.dataproviderObjects.entrySet().iterator(); j.hasNext();) {
                    Map.Entry<Path, MappedRecord> e = j.next();
                    out.reset();
                    out.writeObject(e.getKey());
                    out.writeObject(e.getValue());
                }
                out.flush();
            }
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects Natively
     * 
     * @throws Exception
     */
    @Test
    public void testExternalizeValueObjectsNatively(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()) {
            final long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                    Map.Entry<Path, ValueObject> e = j.next();
                    Path p = e.getKey();
                    ValueObject v = e.getValue();
                    int l = p.size();
                    out.writeInt(l);
                    for (int k = 0; k < l; k++)
                        out.writeUTF(p.getSegment(k).toClassicRepresentation());
                    out.writeUTF("identity");
                    out.writeUTF(v.identity);
                    out.writeUTF("field1");
                    out.writeLong(v.field1.getTime());
                    out.writeUTF("field2");
                    out.writeInt(v.field2.size());
                    for (Iterator<String> k = v.field2.iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeUTF("field3");
                    out.writeLong(v.field3.getTime());
                    out.writeUTF("field4");
                    out.writeInt(v.field4.size());
                    for (Iterator<String> k = v.field4.iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeUTF("field5");
                    out.writeInt(v.field5);
                    out.writeUTF("field6");
                    out.writeUTF(v.field6);
                    out.writeUTF("field7");
                    out.writeUTF(v.field7);
                }
                out.flush();
            }
            logSend(begin);
        }
    }

    /**
     * Externalize Value Maps
     * 
     * @throws Exception
     */
    @Test
    public void testExternalizeValueMaps(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()) {
            final long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                for (Iterator<Map.Entry<Path, Map<String, Object>>> j = this.valueMaps.entrySet().iterator(); j.hasNext();) {
                    Entry<Path, Map<String, Object>> e = j.next();
                    Path p = e.getKey();
                    Map<String, Object> v = (Map<String, Object>) e.getValue();
                    int l = p.size();
                    out.writeInt(l);
                    for (int k = 0; k < l; k++)
                        out.writeUTF(p.getSegment(k).toClassicRepresentation());
                    out.writeUTF("identity");
                    out.writeUTF((String) v.get("identity"));
                    out.writeUTF("field1");
                    out.writeLong(((Date) v.get("field1")).getTime());
                    out.writeUTF("field2");
                    Set<String> f2 = (Set<String>) v.get("field2");
                    out.writeInt(f2.size());
                    for (Iterator<String> k = f2.iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeUTF("field3");
                    out.writeLong(((Date) v.get("field3")).getTime());
                    out.writeUTF("field4");
                    Set<String> f4 = (Set<String>) v.get("field4");
                    out.writeInt(f4.size());
                    for (Iterator<String> k = f4.iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeUTF("field5");
                    out.writeInt((Integer) v.get("field5"));
                    out.writeUTF("field6");
                    out.writeUTF((String) v.get("field6"));
                    out.writeUTF("field7");
                    out.writeUTF((String) v.get("field7"));
                }
                out.flush();
            }
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects Normally
     * 
     * @throws Exception
     */
    @Test
    public void testExternalizeValueObjectsNormally(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()) {
            final long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                    Map.Entry<Path, ValueObject> e = j.next();
                    Path p = e.getKey();
                    ValueObject v = e.getValue();
                    int l = p.size();
                    out.writeInt(l);
                    for (int k = 0; k < l; k++)
                        out.writeUTF(p.getSegment(k).toClassicRepresentation());
                    out.writeUTF("identity");
                    out.writeUTF(v.getIdentity());
                    out.writeUTF("field1");
                    out.writeLong(v.getField1().getTime());
                    out.writeUTF("field2");
                    out.writeInt(v.getField2().size());
                    for (Iterator<String> k = v.getField2().iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeUTF("field3");
                    out.writeLong(v.getField3().getTime());
                    out.writeUTF("field4");
                    out.writeInt(v.getField4().size());
                    for (Iterator<String> k = v.getField4().iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeUTF("field5");
                    out.writeInt(v.getField5());
                    out.writeUTF("field6");
                    out.writeUTF(v.getField6());
                    out.writeUTF("field7");
                    out.writeUTF(v.getField7());
                }
                out.flush();
            }
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects Reflectively
     * 
     * @throws Exception
     */
    @Test
    public void testExternalizeValueObjectsReflectively(
    ) throws Exception {
        Method g0 = ValueObject.class.getMethod("getIdentity", (Class[]) null);
        Method g1 = ValueObject.class.getMethod("getField1", (Class[]) null);
        Method g2 = ValueObject.class.getMethod("getField2", (Class[]) null);
        Method g3 = ValueObject.class.getMethod("getField3", (Class[]) null);
        Method g4 = ValueObject.class.getMethod("getField4", (Class[]) null);
        Method g5 = ValueObject.class.getMethod("getField5", (Class[]) null);
        Method g6 = ValueObject.class.getMethod("getField6", (Class[]) null);
        Method g7 = ValueObject.class.getMethod("getField7", (Class[]) null);
        try (OutputStream storage = newOutputStream()) {
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                    Map.Entry<Path, ValueObject> e = j.next();
                    Path p = e.getKey();
                    ValueObject v = e.getValue();
                    int l = p.size();
                    out.writeInt(l);
                    for (int k = 0; k < l; k++)
                        out.writeUTF(p.getSegment(k).toClassicRepresentation());
                    out.writeUTF("identity");
                    out.writeUTF((String) g0.invoke(v, (Object[]) null));
                    out.writeUTF("field1");
                    out.writeLong(((Date) g1.invoke(v, (Object[]) null)).getTime());
                    out.writeUTF("field2");
                    Set<String> f2 = (Set<String>) g2.invoke(v, (Object[]) null);
                    out.writeInt(f2.size());
                    for (Iterator<String> k = f2.iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeUTF("field3");
                    out.writeLong(((Date) g3.invoke(v, (Object[]) null)).getTime());
                    out.writeUTF("field4");
                    Set<String> f4 = (Set<String>) g4.invoke(v, (Object[]) null);
                    out.writeInt(f4.size());
                    for (Iterator<String> k = f4.iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeUTF("field5");
                    out.writeInt((Integer) g5.invoke(v, (Object[]) null));
                    out.writeUTF("field6");
                    out.writeUTF((String) g6.invoke(v, (Object[]) null));
                    out.writeUTF("field7");
                    out.writeUTF((String) g7.invoke(v, (Object[]) null));
                }
                out.flush();
            }
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects As Text
     * 
     * @throws Exception
     */
    @Test
    public void testFormatValueObjectsNatively(
    ) throws Exception {
        DateTimeFormat dateFormat = DateTimeFormat.BASIC_UTC_FORMAT;
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                try (DataOutputStream out = new DataOutputStream(storage)) {
                    for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                        Map.Entry<Path, ValueObject> e = j.next();
                        out.writeUTF(e.getKey().toXRI());
                        ValueObject v = e.getValue();
                        out.writeUTF("identity");
                        out.writeUTF(v.identity);
                        out.writeUTF("field1");
                        out.writeUTF(dateFormat.format(v.field1));
                        out.writeUTF("field2");
                        out.writeUTF(String.valueOf(v.field2.size()));
                        for (Iterator<String> k = v.field2.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeUTF("field3");
                        out.writeUTF(dateFormat.format(v.field3));
                        out.writeUTF("field4");
                        out.writeUTF(String.valueOf(v.field4.size()));
                        for (Iterator<String> k = v.field4.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeUTF("field5");
                        out.writeUTF(String.valueOf(v.field5));
                        out.writeUTF("field6");
                        out.writeUTF(v.field6);
                        out.writeUTF("field7");
                        out.writeUTF(v.field7);
                    }
                    out.flush();
                }
            }
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects Reflectively
     * 
     * @throws Exception
     */
    @Test
    public void testFormatValueObjectsReflectively(
    ) throws Exception {
        DateTimeFormat dateFormat = DateTimeFormat.BASIC_UTC_FORMAT;
        Method g0 = ValueObject.class.getMethod("getIdentity", (Class[]) null);
        Method g1 = ValueObject.class.getMethod("getField1", (Class[]) null);
        Method g2 = ValueObject.class.getMethod("getField2", (Class[]) null);
        Method g3 = ValueObject.class.getMethod("getField3", (Class[]) null);
        Method g4 = ValueObject.class.getMethod("getField2", (Class[]) null);
        Method g5 = ValueObject.class.getMethod("getField5", (Class[]) null);
        Method g6 = ValueObject.class.getMethod("getField6", (Class[]) null);
        Method g7 = ValueObject.class.getMethod("getField7", (Class[]) null);
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                try (DataOutputStream out = new DataOutputStream(storage)) {
                    for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                        Map.Entry<Path, ValueObject> e = j.next();
                        ValueObject v = e.getValue();
                        out.writeUTF(e.getKey().toXRI());
                        out.writeUTF("identity");
                        out.writeUTF((String) g0.invoke(v, (Object[]) null));
                        out.writeUTF("field1");
                        out.writeUTF(dateFormat.format((Date) g1.invoke(v, (Object[]) null)));
                        out.writeUTF("field2");
                        Set<String> f2 = (Set<String>) g2.invoke(v, (Object[]) null);
                        out.writeUTF(String.valueOf(f2.size()));
                        for (Iterator<String> k = f2.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeUTF("field3");
                        out.writeUTF(dateFormat.format((Date) g3.invoke(v, (Object[]) null)));
                        out.writeUTF("field4");
                        Set<String> f4 = (Set<String>) g4.invoke(v, (Object[]) null);
                        out.writeUTF(String.valueOf(f4.size()));
                        for (Iterator<String> k = f4.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeUTF("field5");
                        out.writeUTF(String.valueOf((Integer) g5.invoke(v, (Object[]) null)));
                        out.writeUTF("field6");
                        out.writeUTF((String) g6.invoke(v, (Object[]) null));
                        out.writeUTF("field7");
                        out.writeUTF((String) g7.invoke(v, (Object[]) null));
                    }
                    out.flush();
                }
            }
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects As Text
     * 
     * @throws Exception
     */
    @Test
    public void testFormatValueObjectsNormally(
    ) throws Exception {
        DateTimeFormat dateFormat = DateTimeFormat.BASIC_UTC_FORMAT;
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                try (DataOutputStream out = new DataOutputStream(storage)) {
                    for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                        Map.Entry<Path, ValueObject> e = j.next();
                        out.writeUTF(e.getKey().toXRI());
                        ValueObject v = e.getValue();
                        out.writeUTF("identity");
                        out.writeUTF(v.getIdentity());
                        out.writeUTF("field1");
                        out.writeUTF(dateFormat.format(v.getField1()));
                        out.writeUTF("field2");
                        Set<String> f2 = v.getField2();
                        out.writeUTF(String.valueOf(f2.size()));
                        for (Iterator<String> k = f2.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeUTF("field3");
                        out.writeUTF(dateFormat.format(v.getField3()));
                        out.writeUTF("field4");
                        Set<String> f4 = v.getField4();
                        out.writeUTF(String.valueOf(f4.size()));
                        for (Iterator<String> k = f4.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeUTF("field5");
                        out.writeUTF(String.valueOf(v.getField5()));
                        out.writeUTF("field6");
                        out.writeUTF(v.getField6());
                        out.writeUTF("field7");
                        out.writeUTF(v.getField7());
                    }
                    out.flush();
                }
            }
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects As Text
     * 
     * @throws Exception
     */
    @Test
    public void testStreamValueObjectsNormally(
    ) throws Exception {
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                try (DataOutputStream out = new DataOutputStream(storage)) {
                    header(out);
                    for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                        Map.Entry<Path, ValueObject> e = j.next();
                        Path p = e.getKey();
                        int l = p.size();
                        out.writeInt(p.size());
                        for (int k = 0; k < l; k++)
                            out.writeUTF(p.getSegment(k).toClassicRepresentation());
                        ValueObject v = e.getValue();
                        out.writeUTF(v.getIdentity());
                        out.writeLong(v.getField1().getTime());
                        Set<String> f2 = v.getField2();
                        out.writeInt(f2.size());
                        for (Iterator<String> k = f2.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeLong(v.getField3().getTime());
                        Set<String> f4 = v.getField4();
                        out.writeInt(f4.size());
                        for (Iterator<String> k = f4.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeInt(v.getField5());
                        out.writeUTF(v.getField6());
                        out.writeUTF(v.getField7());
                    }
                    out.flush();
                }
            }
            logSend(begin);
        }
        try (InputStream storage = newInputStream()) {
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayInputStream) storage).reset();
                try(DataInputStream in = new DataInputStream(storage)){
                    header(in);
                    this.valueObjects = new HashMap<Path, ValueObject>();
                    for (int j = 0; j < OBJECTS; j++) {
                        int s = in.readInt();
                        String[] p = new String[s];
                        for (int k = 0; k < s; k++)
                            p[k] = in.readUTF();
                        ValueObject v = new ValueObject();
                        this.valueObjects.put(new Path(p), v);
                        v.setIdentity(in.readUTF());
                        v.setField1(new Date(in.readLong()));
                        Set<String> f2 = new HashSet<String>();
                        v.setField2(f2);
                        for (int k = 0, l = in.readInt(); k < l; k++)
                            f2.add(in.readUTF());
                        v.setField1(new Date(in.readLong()));
                        Set<String> f4 = new HashSet<String>();
                        v.setField4(f4);
                        for (int k = 0, l = in.readInt(); k < l; k++)
                            f4.add(in.readUTF());
                        v.setField5(in.readInt());
                        v.setField6(in.readUTF());
                        v.setField7(in.readUTF());
                    }
                }
            }
            logReceive(begin);
        }
    }

    /**
     * Externalize Value Objects As Text
     * 
     * @throws Exception
     */
    @Test
    public void testStreamValueObjectsNatively(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()){
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                header(out);
                for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                    Map.Entry<Path, ValueObject> e = j.next();
                    Path p = e.getKey();
                    int l = p.size();
                    out.writeInt(p.size());
                    for (int k = 0; k < l; k++)
                        out.writeUTF(p.getSegment(k).toClassicRepresentation());
                    ValueObject v = e.getValue();
                    out.writeUTF(v.identity);
                    out.writeLong(v.field1.getTime());
                    out.writeInt(v.field2.size());
                    for (Iterator<String> k = v.field2.iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeLong(v.field3.getTime());
                    out.writeInt(v.field4.size());
                    for (Iterator<String> k = v.field4.iterator(); k.hasNext();)
                        out.writeUTF(k.next());
                    out.writeInt(v.field5);
                    out.writeUTF(v.field6);
                    out.writeUTF(v.field7);
                }
                out.flush();
            }
            logSend(begin);
        }
        try (InputStream storage = newInputStream()) {
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayInputStream) storage).reset();
                try(DataInputStream in = new DataInputStream(storage)){
                    header(in);
                    this.valueObjects = new HashMap<Path, ValueObject>();
                    for (int j = 0; j < OBJECTS; j++) {
                        ValueObject v = new ValueObject();
                        int s = in.readInt();
                        String[] p = new String[s];
                        for (int k = 0; k < s; k++)
                            p[k] = in.readUTF();
                        this.valueObjects.put(new Path(p), v);
                        v.identity = in.readUTF();
                        v.field1 = new Date(in.readLong());
                        v.field2 = new HashSet<String>();
                        for (int k = 0, l = in.readInt(); k < l; k++)
                            v.field2.add(in.readUTF());
                        v.field3 = new Date(in.readLong());
                        v.field4 = new HashSet<String>();
                        for (int k = 0, l = in.readInt(); k < l; k++)
                            v.field4.add(in.readUTF());
                        v.field5 = in.readInt();
                        v.field6 = in.readUTF();
                        v.field7 = in.readUTF();
                    }
                }
            }
            logReceive(begin);
        }
    }

    /**
     * Externalize Value Objects Reflectively
     * 
     * @throws Exception
     */
    @Test
    public void testStreamValueObjectsReflectively(
    ) throws Exception {
        Field g0 = ValueObject.class.getField("identity");
        Field g1 = ValueObject.class.getField("field1");
        Field g2 = ValueObject.class.getField("field2");
        Field g3 = ValueObject.class.getField("field3");
        Field g4 = ValueObject.class.getField("field4");
        Field g5 = ValueObject.class.getField("field5");
        Field g6 = ValueObject.class.getField("field6");
        Field g7 = ValueObject.class.getField("field7");
        try (OutputStream storage = newOutputStream()) {
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                try(DataOutputStream out = new DataOutputStream(storage)){
                    header(out);
                    for (Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator(); j.hasNext();) {
                        Map.Entry<Path, ValueObject> e = j.next();
                        Path p = e.getKey();
                        int l = p.size();
                        out.writeInt(p.size());
                        for (int k = 0; k < l; k++)
                            out.writeUTF(p.getSegment(k).toClassicRepresentation());
                        ValueObject v = e.getValue();
                        out.writeUTF((String) g0.get(v));
                        out.writeLong(((Date) g1.get(v)).getTime());
                        Set<String> f2 = (Set<String>) g2.get(v);
                        out.writeInt(f2.size());
                        for (Iterator<String> k = f2.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeLong(((Date) g3.get(v)).getTime());
                        Set<String> f4 = (Set<String>) g4.get(v);
                        out.writeInt(f4.size());
                        for (Iterator<String> k = f4.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeInt((Integer) g5.get(v));
                        out.writeUTF((String) g6.get(v));
                        out.writeUTF((String) g7.get(v));
                    }
                    out.flush();
                }
            }
            logSend(begin);
        }
        try (InputStream storage = newInputStream()) {
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayInputStream) storage).reset();
                try(DataInputStream in = new DataInputStream(storage)){
                    header(in);
                    this.valueObjects = new HashMap<Path, ValueObject>();
                    for (int j = 0; j < OBJECTS; j++) {
                        int s = in.readInt();
                        String[] p = new String[s];
                        for (int k = 0; k < s; k++)
                            p[k] = in.readUTF();
                        ValueObject v = new ValueObject();
                        this.valueObjects.put(new Path(p), v);
                        g0.set(v, in.readUTF());
                        g1.set(v, new Date(in.readLong()));
                        Set<String> f2 = new HashSet<String>();
                        g2.set(v, f2);
                        for (int k = 0, l = in.readInt(); k < l; k++)
                            f2.add(in.readUTF());
                        g3.set(v, new Date(in.readLong()));
                        Set<String> f4 = new HashSet<String>();
                        g4.set(v, f4);
                        for (int k = 0, l = in.readInt(); k < l; k++)
                            f4.add(in.readUTF());
                        g5.set(v, in.readInt());
                        g6.set(v, in.readUTF());
                        g7.set(v, in.readUTF());
                    }
                }
            }
            logReceive(begin);
        }
    }

    /**
     * Externalize Value Objects Reflectively
     * 
     * @throws Exception
     */
    @Test
    public void testStreamValueMaps(
    ) throws Exception {
        try (OutputStream storage = newOutputStream()){
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayOutputStream) storage).reset();
                try(DataOutputStream out = new DataOutputStream(storage)){
                    header(out);
                    for (Iterator<Map.Entry<Path, Map<String, Object>>> j = this.valueMaps.entrySet().iterator(); j.hasNext();) {
                        Map.Entry<Path, Map<String, Object>> e = j.next();
                        Path p = e.getKey();
                        int l = p.size();
                        out.writeInt(p.size());
                        for (int k = 0; k < l; k++)
                            out.writeUTF(p.getSegment(k).toClassicRepresentation());
                        Map<String, Object> v = e.getValue();
                        out.writeUTF((String) v.get("identity"));
                        out.writeLong(((Date) v.get("field1")).getTime());
                        Set<String> f2 = (Set<String>) v.get("field2");
                        out.writeInt(f2.size());
                        for (Iterator<String> k = f2.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeLong(((Date) v.get("field3")).getTime());
                        Set<String> f4 = (Set<String>) v.get("field4");
                        out.writeInt(f4.size());
                        for (Iterator<String> k = f4.iterator(); k.hasNext();)
                            out.writeUTF(k.next());
                        out.writeInt((Integer) v.get("field5"));
                        out.writeUTF((String) v.get("field6"));
                        out.writeUTF((String) v.get("field7"));
                    }
                    out.flush();
                }
            }
            logSend(begin);
        }
        try (InputStream storage = newInputStream()) {
            long begin = System.currentTimeMillis();
            for (int i = 0; i < RUNS; i++) {
                if (IN_MEMORY)
                    ((ByteArrayInputStream) storage).reset();
                try(DataInputStream in = new DataInputStream(storage)){
                    header(in);
                    this.valueMaps = new HashMap<Path, Map<String, Object>>();
                    for (int j = 0; j < OBJECTS; j++) {
                        int s = in.readInt();
                        String[] p = new String[s];
                        for (int k = 0; k < s; k++)
                            p[k] = in.readUTF();
                        Map<String, Object> v = new HashMap<String, Object>();
                        this.valueMaps.put(new Path(p), v);
                        v.put("identity", in.readUTF());
                        v.put("field1", new Date(in.readLong()));
                        Set<String> f2 = new HashSet<String>();
                        v.put("field2", f2);
                        for (int k = 0, l = in.readInt(); k < l; k++)
                            f2.add(in.readUTF());
                        v.put("field3", new Date(in.readLong()));
                        Set<String> f4 = new HashSet<String>();
                        v.put("field4", f4);
                        for (int k = 0, l = in.readInt(); k < l; k++)
                            f4.add(in.readUTF());
                        v.put("field5", in.readInt());
                        v.put("field6", in.readUTF());
                        v.put("field7", in.readUTF());
                    }
                }
            }
            logReceive(begin);
        }
    }

    protected void logSend(
        long begin
    ) {
        long duration = 1000L *
            (System.currentTimeMillis() -
                begin) /
            RUNS /
            OBJECTS;
        if (PerformanceTest.sendReference == 0L)
            PerformanceTest.sendReference = duration;
        System.out.println(
            "Duration: " +
                duration +
                " ns/object (" +
                100 *
                    duration /
                    PerformanceTest.sendReference +
                "%)"
        );
        if (IN_MEMORY) {
            int size = ((ByteArrayOutputStream) this.channel).size() /
                OBJECTS;
            if (PerformanceTest.sizeReference == 0)
                PerformanceTest.sizeReference = size;
            System.out.println(
                "\tsize: " +
                    size +
                    " bytes/object (" +
                    100 *
                        size /
                        PerformanceTest.sizeReference +
                    "%)"
            );
        }
    }

    protected void logReceive(
        long begin
    ) {
        long duration = 1000L *
            (System.currentTimeMillis() -
                begin) /
            RUNS /
            OBJECTS;
        if (PerformanceTest.receiveReference == 0L)
            PerformanceTest.receiveReference = duration;
        System.out.println(
            "\treceive: " +
                duration +
                " ns/object (" +
                100 *
                    duration /
                    PerformanceTest.receiveReference +
                "%)"
        );
    }

    protected final OutputStream newOutputStream(
    ) throws IOException {
        return channel = IN_MEMORY ? new ByteArrayOutputStream() :
            new FileOutputStream(file = File.createTempFile(PerformanceTest.class.getSimpleName(), ".dat"));
    }

    protected final InputStream newInputStream(
    ) throws IOException {
        return IN_MEMORY ? new ByteArrayInputStream(((ByteArrayOutputStream) this.channel).toByteArray()) : new FileInputStream(this.file);
    }

    private final void header(
        DataInputStream in
    ) throws IOException {
        Assertions.assertEquals(ValueObject.class.getName(), in.readUTF());
        Assertions.assertEquals("7", in.readUTF());
        Assertions.assertEquals("identity", in.readUTF());
        Assertions.assertEquals("field1", in.readUTF());
        Assertions.assertEquals("field2", in.readUTF());
        Assertions.assertEquals("field3", in.readUTF());
        Assertions.assertEquals("field4", in.readUTF());
        Assertions.assertEquals("field5", in.readUTF());
        Assertions.assertEquals("field6", in.readUTF());
    }

    private final void header(
        DataOutputStream out
    ) throws IOException {
        out.writeUTF(ValueObject.class.getName());
        out.writeUTF("7");
        out.writeUTF("identity");
        out.writeUTF("field1");
        out.writeUTF("field2");
        out.writeUTF("field3");
        out.writeUTF("field4");
        out.writeUTF("field5");
        out.writeUTF("field6");
    }

}

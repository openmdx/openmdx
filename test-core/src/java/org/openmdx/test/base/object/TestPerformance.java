/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestPerformance.java,v 1.3 2009/06/03 15:48:43 hburger Exp $
 * Description: TestPerformance 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/03 15:48:43 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2006, OMEX AG, Switzerland
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

package org.openmdx.test.base.object;

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

import javax.xml.datatype.DatatypeFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.application.dataprovider.cci.DataproviderObject;
import org.openmdx.base.naming.Path;
import org.openmdx.base.text.format.DateFormat;

/**
 * Test Performance
 */
public class TestPerformance
    extends TestCase
{

    /**
     * Constructor 
     */
    public TestPerformance() {
        super();
    }

    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }

    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite() {
        return new TestSuite(TestPerformance.class);
    }

    Map<Path, ValueObject> valueObjects;
    Map<Path, Map<String, Object>> valueMaps;
    Map<Path, DataproviderObject> dataproviderObjects;
    
    /**
     * A Datatype Factory Instance
     */
    protected DatatypeFactory xmlDatatypeFactory;

    protected File file;
    protected OutputStream channel;
    
    static final boolean IN_MEMORY = true;
    static final int RUNS = 100;
    static final int OBJECTS = 100;

    static long sendReference = 0L;
    static long receiveReference = 0L;
    static int sizeReference = 0;
    
    protected void setUp(
    ) throws Exception {
        this.xmlDatatypeFactory = DatatypeFactory.newInstance();
        this.valueObjects = new HashMap<Path, ValueObject>();
        this.valueMaps = new HashMap<Path, Map<String, Object>>();
        this.dataproviderObjects = new HashMap<Path, DataproviderObject>();
        for(
            int i = 0;
            i < OBJECTS;
            i++
        ){
            ValueObject o = new ValueObject();
            Map<String, Object> m = new HashMap<String, Object>();
            DataproviderObject d = new DataproviderObject(
                new Path(
                    new String[] {
                        "org.openmdx.test.base.object.ValueObject", "provider", "JUnit", "segment", "org*openmdx*test", "object", String.valueOf(i)
                    }
                )
            );
            o.id = d.path();
            m.put(
                "identity",
                o.identity = d.path().toXRI()
            );
            d.values("identity").add(o.identity);
            m.put(
                "field1",
                o.field1 = new Date()
            );
            d.values("field1").add(o.field1);
            m.put(
                "field2",
                o.field2 = Collections.singleton("JUnit")
            );
            d.values("field2").add(o.field2);
            m.put(
                "field3",
                o.field3 = o.field1                
            );
            d.values("field3").add(o.field3);
            m.put(
                "field4",
                o.field4 = o.field2
            );
            d.values("field4").add(o.field4);
            m.put(
                "field5",
                o.field5 = 1000000 + i
            );
            d.values("field5").add(o.field5);
            m.put(
                "field6",
                o.field6 = "Name " + i
            );
            d.values("field6").add(o.field6);
            m.put(
                "field7",
                o.field7 = "Value " + i
            );
            d.values("field7").add(o.field7);
            this.valueObjects.put(d.path(), o);
            this.valueMaps.put(d.path(), m);
            this.dataproviderObjects.put(d.path(), d);
        }
    }

    /**
     * Constructs a test case with the given name.
     */
    public TestPerformance(String name) {
        super(name);
    }

    /**
     * Serialize Value Objects Together
     * 
     * @throws Exception
     */
    public void testSerializeValueObjectsTogether() throws Exception {
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                ObjectOutputStream out = new ObjectOutputStream(storage);
                out.writeObject(this.valueObjects);
                out.flush();
            }
            logSend(begin);
        }
        {
            ObjectInputStream in;
            InputStream storage = newInputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayInputStream)storage).reset();
                in = new ObjectInputStream(storage);
                this.valueObjects = (Map<Path, ValueObject>) in.readObject();
//              in.close();
            }
            storage.close();
            logReceive(begin);
        }
    }

    /**
     * Serialize Value Maps Together 
     * 
     * @throws Exception
     */
    public void testSerializeValueMapsTogether() throws Exception {
        OutputStream storage = newOutputStream();
        long begin = System.currentTimeMillis();
        for(
            int i = 0;
            i < RUNS;
            i++
        ) {
            if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
            ObjectOutputStream out = new ObjectOutputStream(storage);
            out.writeObject(this.valueMaps);
            out.flush();
        }
        storage.close();
        logSend(begin);
    }

    /**
     * Serialize Value Maps Together 
     * 
     * @throws Exception
     */
    public void testSerializeDataproviderObjectsTogether() throws Exception {
        OutputStream storage = newOutputStream();
        long begin = System.currentTimeMillis();
        for(
            int i = 0;
            i < RUNS;
            i++
        ) {
            if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
            ObjectOutputStream out = new ObjectOutputStream(storage);
            out.writeObject(this.dataproviderObjects);
            out.flush();
        }
        storage.close();
        logSend(begin);
    }

    /**
     * Serialize Value Objects Separately 
     * 
     * @throws Exception
     */
    public void testSerializeValueObjectsSeparately() throws Exception {
        OutputStream storage = newOutputStream();
        long begin = System.currentTimeMillis();
        for(
            int i = 0;
            i < RUNS;
            i++
        ) {
            if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
            ObjectOutputStream out = new ObjectOutputStream(storage);
            for (
                 Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                 j.hasNext();
            ){
                Map.Entry<Path, ValueObject> e = j.next();
                out.reset();
                out.writeObject(e.getKey());
                out.writeObject(e.getValue());
            }
            out.flush();
        }
        storage.close();
        logSend(begin);
    }

    /**
     * Serialize Value Objects Separately 
     * 
     * @throws Exception
     */
    public void testSerializeDataproviderObjectsSeparately() throws Exception {
        OutputStream storage = newOutputStream();
        long begin = System.currentTimeMillis();
        for(
            int i = 0;
            i < RUNS;
            i++
        ) {
            if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
            ObjectOutputStream out = new ObjectOutputStream(storage);
            for (
                 Iterator<Map.Entry<Path, DataproviderObject>> j = this.dataproviderObjects.entrySet().iterator();
                 j.hasNext();
            ){
                Map.Entry<Path, DataproviderObject> e = j.next();
                out.reset();
                out.writeObject(e.getKey());
                out.writeObject(e.getValue());
            }
            out.flush();
        }
        storage.close();
        logSend(begin);
    }

    /**
     * Externalize Value Objects Natively 
     * 
     * @throws Exception
     */
    public void testExternalizeValueObjectsNatively() throws Exception {
        OutputStream storage = newOutputStream();
        long begin = System.currentTimeMillis();
        for(
            int i = 0;
            i < RUNS;
            i++
        ) {
            if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
            DataOutputStream out = new DataOutputStream(storage);
            for (
                 Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                 j.hasNext();
            ){
                Map.Entry<Path, ValueObject> e = j.next();                
                Path p = e.getKey();
                ValueObject v = e.getValue();
                int l = p.size();
                out.writeInt(l);
                for(
                    int k = 0;
                    k < l;
                    k++
                ) out.writeUTF(p.get(k));
                out.writeUTF("identity");
                out.writeUTF(v.identity);
                out.writeUTF("field1");
                out.writeLong(v.field1.getTime());
                out.writeUTF("field2");
                out.writeInt(v.field2.size());
                for(
                    Iterator<String> k = v.field2.iterator();
                    k.hasNext();
                ) out.writeUTF(k.next());
                out.writeUTF("field3");
                out.writeLong(v.field3.getTime());
                out.writeUTF("field4");
                out.writeInt(v.field4.size());
                for(
                    Iterator<String> k = v.field4.iterator();
                    k.hasNext();
                ) out.writeUTF(k.next());
                out.writeUTF("field5");
                out.writeInt(v.field5);
                out.writeUTF("field6");
                out.writeUTF(v.field6);
                out.writeUTF("field7");
                out.writeUTF(v.field7);
            }
            out.flush();
        }
        storage.close();
        logSend(begin);
    }

    /**
     * Externalize Value Maps 
     * 
     * @throws Exception
     */
    public void testExternalizeValueMaps() throws Exception {
        OutputStream storage = newOutputStream();
        long begin = System.currentTimeMillis();
        for(
            int i = 0;
            i < RUNS;
            i++
        ) {
            if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
            DataOutputStream out = new DataOutputStream(storage);
            for (
                 Iterator<Map.Entry<Path, Map<String, Object>>> j = this.valueMaps.entrySet().iterator();
                 j.hasNext();
            ){
                Entry<Path, Map<String, Object>> e = j.next();                
                Path p = e.getKey();
                Map<String, Object> v = (Map<String, Object>) e.getValue();
                int l = p.size();
                out.writeInt(l);
                for(
                    int k = 0;
                    k < l;
                    k++
                ) out.writeUTF(p.get(k));
                out.writeUTF("identity");
                out.writeUTF((String) v.get("identity"));
                out.writeUTF("field1");
                out.writeLong(((Date)v.get("field1")).getTime());
                out.writeUTF("field2");
                Set<String> f2 = (Set<String>) v.get("field2");
                out.writeInt(f2.size());
                for(
                    Iterator<String> k = f2.iterator();
                    k.hasNext();
                ) out.writeUTF(k.next());
                out.writeUTF("field3");
                out.writeLong(((Date)v.get("field3")).getTime());
                out.writeUTF("field4");
                Set<String> f4 = (Set<String>) v.get("field4");
                out.writeInt(f4.size());
                for(
                    Iterator<String> k = f4.iterator();
                    k.hasNext();
                ) out.writeUTF(k.next());
                out.writeUTF("field5");
                out.writeInt((Integer) v.get("field5"));
                out.writeUTF("field6");
                out.writeUTF((String) v.get("field6"));
                out.writeUTF("field7");
                out.writeUTF((String) v.get("field7"));
            }
            out.flush();
        }
        storage.close();
        logSend(begin);
    }

    /**
     * Externalize Value Objects Normally 
     * 
     * @throws Exception
     */
    public void testExternalizeValueObjectsNormally() throws Exception {
        OutputStream storage = newOutputStream();
        long begin = System.currentTimeMillis();
        for(
            int i = 0;
            i < RUNS;
            i++
        ) {
            if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
            DataOutputStream out = new DataOutputStream(storage);
            for (
                 Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                 j.hasNext();
            ){
                Map.Entry<Path, ValueObject> e = j.next();                
                Path p = e.getKey();
                ValueObject v = e.getValue();
                int l = p.size();
                out.writeInt(l);
                for(
                    int k = 0;
                    k < l;
                    k++
                ) out.writeUTF(p.get(k));
                out.writeUTF("identity");
                out.writeUTF(v.getIdentity());
                out.writeUTF("field1");
                out.writeLong(v.getField1().getTime());
                out.writeUTF("field2");
                out.writeInt(v.getField2().size());
                for(
                    Iterator<String> k = v.getField2().iterator();
                    k.hasNext();
                ) out.writeUTF(k.next());
                out.writeUTF("field3");
                out.writeLong(v.getField3().getTime());
                out.writeUTF("field4");
                out.writeInt(v.getField4().size());
                for(
                    Iterator<String> k = v.getField4().iterator();
                    k.hasNext();
                ) out.writeUTF(k.next());
                out.writeUTF("field5");
                out.writeInt(v.getField5());
                out.writeUTF("field6");
                out.writeUTF(v.getField6());
                out.writeUTF("field7");
                out.writeUTF(v.getField7());
            }
            out.flush();
        }
        storage.close();
        logSend(begin);
    }

    /**
     * Externalize Value Objects Reflectively 
     * 
     * @throws Exception
     */
    public void testExternalizeValueObjectsReflectively() throws Exception {
        Method g0 = ValueObject.class.getMethod(
            "getIdentity",
            (Class[])null
        );
        Method g1 = ValueObject.class.getMethod(
            "getField1",
            (Class[])null
        );
        Method g2 = ValueObject.class.getMethod(
            "getField2",
            (Class[])null
        );
        Method g3 = ValueObject.class.getMethod(
            "getField3",
            (Class[])null
        );
        Method g4 = ValueObject.class.getMethod(
            "getField4",
            (Class[])null
        );
        Method g5 = ValueObject.class.getMethod(
            "getField5",
            (Class[])null
        );
        Method g6 = ValueObject.class.getMethod(
            "getField6",
            (Class[])null
        );
        Method g7 = ValueObject.class.getMethod(
            "getField7",
            (Class[])null
        );
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                for (
                     Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                     j.hasNext();
                ){
                    Map.Entry<Path, ValueObject> e = j.next();                
                    Path p = e.getKey();
                    ValueObject v = e.getValue();
                    int l = p.size();
                    out.writeInt(l);
                    for(
                        int k = 0;
                        k < l;
                        k++
                    ) out.writeUTF(p.get(k));
                    out.writeUTF("identity");
                    out.writeUTF(
                        (String)g0.invoke(v, (Object[])null)
                    );
                    out.writeUTF("field1");
                    out.writeLong(
                        ((Date)g1.invoke(v, (Object[])null)).getTime()
                    );
                    out.writeUTF("field2");
                    Set<String> f2 = (Set<String>) g2.invoke(v, (Object[])null);
                    out.writeInt(f2.size());
                    for(
                        Iterator<String> k = f2.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeUTF("field3");
                    out.writeLong(
                        ((Date)g3.invoke(v, (Object[])null)).getTime()
                    );
                    out.writeUTF("field4");
                    Set<String> f4 = (Set<String>) g4.invoke(v, (Object[])null);
                    out.writeInt(f4.size());
                    for(
                        Iterator<String> k = f4.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeUTF("field5");
                    out.writeInt(
                        (Integer)g5.invoke(v, (Object[])null)
                    );
                    out.writeUTF("field6");
                    out.writeUTF(
                        (String)g6.invoke(v, (Object[])null)
                    );
                    out.writeUTF("field7");
                    out.writeUTF(
                        (String)g7.invoke(v, (Object[])null)
                    );
                }
                out.flush();
            }
            storage.close();
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects As Text 
     * 
     * @throws Exception
     */
    public void testFormatValueObjectsNatively() throws Exception {
        DateFormat dateFormat = DateFormat.getInstance();
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                for (
                     Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                     j.hasNext();
                ){
                    Map.Entry<Path, ValueObject> e = j.next();                
                    out.writeUTF(e.getKey().toXRI());
                    ValueObject v = e.getValue();
                    out.writeUTF("identity");
                    out.writeUTF(v.identity);
                    out.writeUTF("field1");
                    out.writeUTF(dateFormat.format(v.field1));
                    out.writeUTF("field2");
                    out.writeUTF(String.valueOf(v.field2.size()));
                    for(
                        Iterator<String> k = v.field2.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeUTF("field3");
                    out.writeUTF(dateFormat.format(v.field3));
                    out.writeUTF("field4");
                    out.writeUTF(String.valueOf(v.field4.size()));
                    for(
                        Iterator<String> k = v.field4.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeUTF("field5");
                    out.writeUTF(String.valueOf(v.field5));
                    out.writeUTF("field6");
                    out.writeUTF(v.field6);
                    out.writeUTF("field7");
                    out.writeUTF(v.field7);
                }
                out.flush();
            }
            storage.close();
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects Reflectively 
     * 
     * @throws Exception
     */
    public void testFormatValueObjectsReflectively() throws Exception {
        DateFormat dateFormat = DateFormat.getInstance();
        Method g0 = ValueObject.class.getMethod(
            "getIdentity",
            (Class[])null
        );
        Method g1 = ValueObject.class.getMethod(
            "getField1",
            (Class[])null
        );
        Method g2 = ValueObject.class.getMethod(
            "getField2",
            (Class[])null
        );
        Method g3 = ValueObject.class.getMethod(
            "getField3",
            (Class[])null
        );
        Method g4 = ValueObject.class.getMethod(
            "getField2",
            (Class[])null
        );
        Method g5 = ValueObject.class.getMethod(
            "getField5",
            (Class[])null
        );
        Method g6 = ValueObject.class.getMethod(
            "getField6",
            (Class[])null
        );
        Method g7 = ValueObject.class.getMethod(
            "getField7",
            (Class[])null
        );
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                for (
                     Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                     j.hasNext();
                ){
                    Map.Entry<Path, ValueObject> e = j.next();                
                    ValueObject v = e.getValue();
                    out.writeUTF(e.getKey().toXRI());
                    out.writeUTF("identity");
                    out.writeUTF(
                        (String)g0.invoke(v, (Object[])null)
                    );
                    out.writeUTF("field1");
                    out.writeUTF(
                        dateFormat.format(
                            (Date)g1.invoke(v, (Object[])null)
                        )
                    );
                    out.writeUTF("field2");
                    Set<String> f2 = (Set<String>) g2.invoke(v, (Object[])null);
                    out.writeUTF(String.valueOf(f2.size()));
                    for(
                        Iterator<String> k = f2.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeUTF("field3");
                    out.writeUTF(
                        dateFormat.format(
                            (Date)g3.invoke(v, (Object[])null)
                        )
                    );
                    out.writeUTF("field4");
                    Set<String> f4 = (Set<String>) g4.invoke(v, (Object[])null);
                    out.writeUTF(String.valueOf(f4.size()));
                    for(
                        Iterator<String> k = f4.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeUTF("field5");
                    out.writeUTF(
                        String.valueOf(
                            (Integer)g5.invoke(v, (Object[])null)
                        )
                    );
                    out.writeUTF("field6");
                    out.writeUTF(
                        (String)g6.invoke(v, (Object[])null)
                    );
                    out.writeUTF("field7");
                    out.writeUTF(
                        (String)g7.invoke(v, (Object[])null)
                    );
                }
                out.flush();
            }
            storage.close();
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects As Text 
     * 
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    public void testFormatValueObjectsNormally() throws Exception {
        DateFormat dateFormat = DateFormat.getInstance();
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                for (
                     Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                     j.hasNext();
                ){
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
                    for(
                        Iterator<String> k = f2.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeUTF("field3");
                    out.writeUTF(dateFormat.format(v.getField3()));
                    out.writeUTF("field4");
                    Set<String> f4 = v.getField4();
                    out.writeUTF(String.valueOf(f4.size()));
                    for(
                        Iterator<String> k = f4.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeUTF("field5");
                    out.writeUTF(String.valueOf(v.getField5()));
                    out.writeUTF("field6");
                    out.writeUTF(v.getField6());
                    out.writeUTF("field7");
                    out.writeUTF(v.getField7());
                }
                out.flush();
            }
            storage.close();
            logSend(begin);
        }
    }

    /**
     * Externalize Value Objects As Text 
     * 
     * @throws Exception
     */
    public void testStreamValueObjectsNormally() throws Exception {
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                header(out);
                for (
                     Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                     j.hasNext();
                ){
                    Map.Entry<Path, ValueObject> e = j.next();                
                    Path p = e.getKey();
                    int l = p.size();
                    out.writeInt(p.size());
                    for(
                         int k = 0;
                         k < l;
                         k++
                    ) out.writeUTF(p.get(k));
                    ValueObject v = e.getValue();
                    out.writeUTF(v.getIdentity());
                    out.writeLong(v.getField1().getTime());
                    Set<String> f2 = v.getField2();
                    out.writeInt(f2.size());
                    for(
                        Iterator<String> k = f2.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeLong(v.getField3().getTime());
                    Set<String> f4 = v.getField4();
                    out.writeInt(f4.size());
                    for(
                        Iterator<String> k = f4.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeInt(v.getField5());
                    out.writeUTF(v.getField6());
                    out.writeUTF(v.getField7());
                }
                out.flush();
            }
            storage.close();
            logSend(begin);
        }
        {
            InputStream storage = newInputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ){
                if(IN_MEMORY) ((ByteArrayInputStream)storage).reset();
                DataInputStream in = new DataInputStream(storage);
                header(in);
                this.valueObjects = new HashMap<Path, ValueObject>();
                for(
                     int j = 0;
                     j < OBJECTS;
                     j++
                ){
                    int s = in.readInt();
                    String[] p = new String[s];
                    for(
                       int k = 0;
                       k < s;
                       k++
                    ) p[k] = in.readUTF();
                    ValueObject v = new ValueObject();
                    this.valueObjects.put(new Path(p), v);
                    v.setIdentity(in.readUTF());
                    v.setField1(new Date(in.readLong()));
                    Set<String> f2 = new HashSet<String>();
                    v.setField2(f2);
                    for(
                       int k = 0, l = in.readInt();
                       k < l;
                       k++
                    ) f2.add(in.readUTF());
                    v.setField1(new Date(in.readLong()));
                    Set<String> f4 = new HashSet<String>();
                    v.setField4(f4);
                    for(
                       int k = 0, l = in.readInt();
                       k < l;
                       k++
                    ) f4.add(in.readUTF());
                    v.setField5(in.readInt());
                    v.setField6(in.readUTF());
                    v.setField7(in.readUTF());                
                }
//              in.close();
            }
            storage.close();
            logReceive(begin);
        }
    }

    /**
     * Externalize Value Objects As Text 
     * 
     * @throws Exception
     */
    public void testStreamValueObjectsNatively() throws Exception {
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                header(out);
                for (
                     Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                     j.hasNext();
                ){
                    Map.Entry<Path, ValueObject> e = j.next();
                    Path p = e.getKey();
                    int l = p.size();
                    out.writeInt(p.size());
                    for(
                         int k = 0;
                         k < l;
                         k++
                    ) out.writeUTF(p.get(k));
                    ValueObject v = e.getValue();
                    out.writeUTF(v.identity);
                    out.writeLong(v.field1.getTime());
                    out.writeInt(v.field2.size());
                    for(
                        Iterator<String> k = v.field2.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeLong(v.field3.getTime());
                    out.writeInt(v.field4.size());
                    for(
                        Iterator<String> k = v.field4.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeInt(v.field5);
                    out.writeUTF(v.field6);
                    out.writeUTF(v.field7);
                }
                out.flush();
            }
            storage.close();
            logSend(begin);
        }
        {
            InputStream storage = newInputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ){
                if(IN_MEMORY) ((ByteArrayInputStream)storage).reset();
                DataInputStream in = new DataInputStream(storage);
                header(in);
                this.valueObjects = new HashMap<Path, ValueObject>();
                for(
                     int j = 0;
                     j < OBJECTS;
                     j++
                ){
                    ValueObject v = new ValueObject();
                    int s = in.readInt();
                    String[] p = new String[s];
                    for(
                       int k = 0;
                       k < s;
                       k++
                    ) p[k] = in.readUTF();
                    this.valueObjects.put(new Path(p), v);
                    v.identity = in.readUTF();
                    v.field1 = new Date(in.readLong());
                    v.field2 = new HashSet<String>();
                    for(
                       int k = 0, l = in.readInt();
                       k < l;
                       k++
                    ) v.field2.add(in.readUTF());
                    v.field3 = new Date(in.readLong());
                    v.field4 = new HashSet<String>();
                    for(
                       int k = 0, l = in.readInt();
                       k < l;
                       k++
                    ) v.field4.add(in.readUTF());
                    v.field5 = in.readInt();
                    v.field6 = in.readUTF();
                    v.field7 = in.readUTF();                
                }
//              in.close();
            }
            storage.close();
            logReceive(begin);
        }
    }

    /**
     * Externalize Value Objects Reflectively 
     * 
     * @throws Exception
     */
    public void testStreamValueObjectsReflectively() throws Exception {
        Field g0 = ValueObject.class.getField("identity");
        Field g1 = ValueObject.class.getField("field1");
        Field g2 = ValueObject.class.getField("field2");
        Field g3 = ValueObject.class.getField("field3");
        Field g4 = ValueObject.class.getField("field4");
        Field g5 = ValueObject.class.getField("field5");
        Field g6 = ValueObject.class.getField("field6");
        Field g7 = ValueObject.class.getField("field7");
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                header(out);
                for (
                     Iterator<Map.Entry<Path, ValueObject>> j = this.valueObjects.entrySet().iterator();
                     j.hasNext();
                ){
                    Map.Entry<Path, ValueObject> e = j.next();                
                    Path p = e.getKey();
                    int l = p.size();
                    out.writeInt(p.size());
                    for(
                         int k = 0;
                         k < l;
                         k++
                    ) out.writeUTF(p.get(k));
                    ValueObject v = e.getValue();
                    out.writeUTF(
                        (String)g0.get(v)
                    );
                    out.writeLong(
                        ((Date)g1.get(v)).getTime()
                    );
                    Set<String> f2 = (Set<String>) g2.get(v);
                    out.writeInt(f2.size());
                    for(
                        Iterator<String> k = f2.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeLong(
                        ((Date)g3.get(v)).getTime()
                    );
                    Set<String> f4 = (Set<String>) g4.get(v);
                    out.writeInt(f4.size());
                    for(
                        Iterator<String> k = f4.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeInt(
                        (Integer)g5.get(v)
                    );
                    out.writeUTF(
                        (String)g6.get(v)
                    );
                    out.writeUTF(
                        (String)g7.get(v)
                    );
                }
                out.flush();
            }
            storage.close();
            logSend(begin);
        }
        {
            InputStream storage = newInputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ){
                if(IN_MEMORY) ((ByteArrayInputStream)storage).reset();
                DataInputStream in = new DataInputStream(storage);
                header(in);
                this.valueObjects = new HashMap<Path, ValueObject>();
                for(
                     int j = 0;
                     j < OBJECTS;
                     j++
                ){
                    int s = in.readInt();
                    String[] p = new String[s];
                    for(
                       int k = 0;
                       k < s;
                       k++
                    ) p[k] = in.readUTF();
                    ValueObject v = new ValueObject();
                    this.valueObjects.put(new Path(p), v);
                    g0.set(v, in.readUTF());
                    g1.set(v, new Date(in.readLong()));
                    Set<String> f2 = new HashSet<String>();
                    g2.set(v, f2);
                    for(
                       int k = 0, l = in.readInt();
                       k < l;
                       k++
                    ) f2.add(in.readUTF());
                    g3.set(v, new Date(in.readLong()));
                    Set<String> f4 = new HashSet<String>();
                    g4.set(v, f4);
                    for(
                       int k = 0, l = in.readInt();
                       k < l;
                       k++
                    ) f4.add(in.readUTF());
                    g5.set(v, in.readInt());
                    g6.set(v, in.readUTF());
                    g7.set(v, in.readUTF());                
                }
//              in.close();
            }
            storage.close();
            logReceive(begin);
        }
    }

    /**
     * Externalize Value Objects Reflectively 
     * 
     * @throws Exception
     */
    public void testStreamValueMaps(
    ) throws Exception {
        {
            OutputStream storage = newOutputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ) {
                if(IN_MEMORY) ((ByteArrayOutputStream)storage).reset();
                DataOutputStream out = new DataOutputStream(storage);
                header(out);
                for (
                     Iterator<Map.Entry<Path, Map<String, Object>>> j = this.valueMaps.entrySet().iterator();
                     j.hasNext();
                ){
                    Map.Entry<Path, Map<String, Object>> e = j.next();                
                    Path p = e.getKey();
                    int l = p.size();
                    out.writeInt(p.size());
                    for(
                         int k = 0;
                         k < l;
                         k++
                    ) out.writeUTF(p.get(k));
                    Map<String, Object> v = e.getValue();
                    out.writeUTF(
                        (String)v.get("identity")
                    );
                    out.writeLong(
                        ((Date)v.get("field1")).getTime()
                    );
                    Set<String> f2 = (Set<String>) v.get("field2");
                    out.writeInt(f2.size());
                    for(
                        Iterator<String> k = f2.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeLong(
                        ((Date)v.get("field3")).getTime()
                    );
                    Set<String> f4 = (Set<String>) v.get("field4");
                    out.writeInt(f4.size());
                    for(
                        Iterator<String> k = f4.iterator();
                        k.hasNext();
                    ) out.writeUTF(k.next());
                    out.writeInt(
                        (Integer)v.get("field5")
                    );
                    out.writeUTF(
                        (String)v.get("field6")
                    );
                    out.writeUTF(
                        (String)v.get("field7")
                    );
                }
                out.flush();
            }
            storage.close();
            logSend(begin);
        }
        {            
            InputStream storage = newInputStream();
            long begin = System.currentTimeMillis();
            for(
                int i = 0;
                i < RUNS;
                i++
            ){
                if(IN_MEMORY) ((ByteArrayInputStream)storage).reset();
                DataInputStream in = new DataInputStream(storage);
                header(in);
                this.valueMaps = new HashMap<Path, Map<String,Object>>();
                for(
                     int j = 0;
                     j < OBJECTS;
                     j++
                ){
                    int s = in.readInt();
                    String[] p = new String[s];
                    for(
                       int k = 0;
                       k < s;
                       k++
                    ) p[k] = in.readUTF();
                    Map<String, Object> v = new HashMap<String, Object>();
                    this.valueMaps.put(new Path(p), v);
                    v.put("identity", in.readUTF());
                    v.put("field1", new Date(in.readLong()));
                    Set<String> f2 = new HashSet<String>();
                    v.put("field2", f2);
                    for(
                       int k = 0, l = in.readInt();
                       k < l;
                       k++
                    ) f2.add(in.readUTF());
                    v.put("field3", new Date(in.readLong()));
                    Set<String> f4 = new HashSet<String>();
                    v.put("field4", f4);
                    for(
                       int k = 0, l = in.readInt();
                       k < l;
                       k++
                    ) f4.add(in.readUTF());
                    v.put("field5", in.readInt());
                    v.put("field6", in.readUTF());
                    v.put("field7", in.readUTF());                
                }
//              in.close();
            }
            storage.close();
            logReceive(begin);
        }
    }

    protected void logSend(
        long begin
    ){
        long duration = 1000L * (
            System.currentTimeMillis() - begin
        ) / RUNS / OBJECTS;         
        if(
            TestPerformance.sendReference == 0L
        ) TestPerformance.sendReference = duration;
        System.out.println(
            getName() + ": " + duration + " ns/object (" +
            100 * duration / TestPerformance.sendReference + "%)"            
        );
        if(IN_MEMORY) {
            int size = ((ByteArrayOutputStream)this.channel).size()/ OBJECTS;
            if(
                TestPerformance.sizeReference == 0
            ) TestPerformance.sizeReference = size ;
            System.out.println(
                "\tsize: " + size + " bytes/object (" +
                100 * size / TestPerformance.sizeReference + "%)"
            );
        }
    }

    protected void logReceive(
        long begin
    ){
        long duration = 1000L * (
            System.currentTimeMillis() - begin
        ) / RUNS / OBJECTS;         
        if(
            TestPerformance.receiveReference == 0L
        ) TestPerformance.receiveReference = duration;
        System.out.println(
            "\treceive: " + duration + " ns/object (" +
            100 * duration / TestPerformance.receiveReference + "%)"            
        );
    }

    protected final OutputStream newOutputStream(
    ) throws IOException {
        return channel = IN_MEMORY ? new ByteArrayOutputStream(
        ) : new FileOutputStream(
            file = File.createTempFile(getName(), ".dat")
        );
    }
    
    protected final InputStream newInputStream(
    ) throws IOException {
        return IN_MEMORY ? new ByteArrayInputStream(
            ((ByteArrayOutputStream)this.channel).toByteArray()
        ) : new FileInputStream(
            this.file
        );
    }

    private final void header(
        DataInputStream in
    ) throws IOException {
        assertEquals(ValueObject.class.getName(), in.readUTF());
        assertEquals("7", in.readUTF());
        assertEquals("identity", in.readUTF());
        assertEquals("field1", in.readUTF());
        assertEquals("field2", in.readUTF());
        assertEquals("field3", in.readUTF());
        assertEquals("field4", in.readUTF());
        assertEquals("field5", in.readUTF());
        assertEquals("field6", in.readUTF());
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

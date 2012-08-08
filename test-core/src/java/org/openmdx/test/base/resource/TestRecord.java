/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestRecord.java,v 1.6 2004/06/22 18:27:02 hburger Exp $
 * Description: class TestRecord
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/06/22 18:27:02 $
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
package org.openmdx.test.base.resource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.ExtendedRecordFactory;

public class TestRecord extends TestCase {
    
    /**
     * Constructs a test case with the given name.
     */
    public TestRecord(
    String name
    ) {
        super(name);
    }
    
    /**
     * The batch TestRunner can be given a class to run directly.
     * To start the batch runner from your main you can write: 
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * A test runner either expects a static method suite as the
     * entry point to get a test to run or it will extract the 
     * suite automatically. 
     */
    public static Test suite(
    ) {
    return new TestSuite(TestRecord.class);
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp(
    ) throws Exception {
      this.factory = Records.getRecordFactory();
      this.map2 = new TreeMap();
      this.map2.put("A","a");
      this.map2.put("C","c");
      this.map3 = new TreeMap(map2);
      this.map3.put("B","b");
      this.map23 = new TreeMap();
      this.map23.put(new Integer(2),this.map2);
      this.map23.put(new Integer(3),this.map3);
      this.array2=new String[]{"a","c"};
      this.array3=new String[]{"a","b","c"};
      this.array23=new String[][]{this.array2,this.array3};
      this.list2 = Arrays.asList(this.array2);
      this.list3 = Arrays.asList(this.array3);
      this.list23 = Arrays.asList(new List[]{this.list2,this.list3});
      this.array0 = new int[0];
      this.array1 = new int[]{99};
      this.array01 = new int[][]{this.array0,this.array1};
      this.list0 = Arrays.asList(new Integer[0]);
      this.list1 = Arrays.asList(new Integer[]{new Integer(99)});
      this.list01 = Arrays.asList(new List[]{this.list0,this.list1});
    }
    
    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void tearDown(
    ) throws Exception {
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPreInitializedMappedRecord(
    ) throws Throwable {
        
        // VariableSizeMappedRecord without description
      MappedRecord r2m = factory.createMappedRecord(
        "r2", null, map2
      );
      assertEquals(
        "r2m",
        "org.openmdx.base.resource.spi.VariableSizeMappedRecord",
        r2m.getClass().getName()
      );
      assertEquals(
        "r2m", 
        "r2: {\n" +
        "\tA = \"a\"\n" +
        "\tC = \"c\"\n" +
        "}",
        r2m.toString()
      );
      assertEquals(
        "r2.A",
        "a",
        r2m.get("A")
      );
      assertNull(
        "r2.B",
        r2m.get("B")
      );
      assertEquals(
        "r2.C",
        "c",
        r2m.get("C")
      );
      assertEquals(
        "r2m.size()",
        2,
        r2m.size()
      );
      assertTrue(
        "map2.equals(r2m)",
        map2.equals(r2m)
      );
      assertTrue(
        "r2m.equals(map2)",
        r2m.equals(map2)
      );
      assertEquals(
        "map2.hashCode()==r2m.hashCode()",
        map2.hashCode(),
        r2m.hashCode()
      );

      // VariableSizeMappedRecord with description
      MappedRecord r2md = factory.createMappedRecord(
        "r2", "Record 2", map2
      );
      assertEquals(
        "r2md",
        "org.openmdx.base.resource.spi.VariableSizeMappedRecord",
        r2md.getClass().getName()
      );
      assertEquals(
        "r2md", 
        "r2 (Record 2): {\n" +
        "\tA = \"a\"\n" +
        "\tC = \"c\"\n" +
        "}",
        r2md.toString()
      );
      assertTrue(
        "map2.equals(r2md)",
        map2.equals(r2md)
      );
      assertTrue(
        "r2md.equals(map2)",
        r2md.equals(map2)
      );
      assertEquals(
        "map2.hashCode()==r2md.hashCode()",
        map2.hashCode(),
        r2md.hashCode()
      );
      assertTrue(
        "r2md.equals(r2m)",
        r2md.equals(r2m)
      );
      assertTrue(
        "r2m.equals(r2md)",
        r2m.equals(r2md)
      );
      assertEquals(
        "r2md.hashCode()==r2m.hashCode()",
        r2md.hashCode(),
        r2m.hashCode()
      );

        // Nested VariableSizeMappedRecords
      Map m23 = new TreeMap();
      m23.put(new Integer(2),r2md);
      m23.put(new Integer(3),factory.createMappedRecord("r3",null,map3));
      Record r23 = factory.createMappedRecord(
        "r23", "Nested Utilities", m23
      );
      assertEquals(
        "r23",
        "org.openmdx.base.resource.spi.VariableSizeMappedRecord",
        r23.getClass().getName()
      );
      assertEquals(
        "r23", 
        "r23 (Nested Utilities): {\n" +
        "\t2 = r2 (Record 2): {\n" +
        "\t\tA = \"a\"\n" +
        "\t\tC = \"c\"\n" +
        "\t}\n" +
        "\t3 = r3: {\n" +
        "\t\tA = \"a\"\n" +
        "\t\tB = \"b\"\n" +
        "\t\tC = \"c\"\n" +
        "\t}\n" +
        "}",
        r23.toString()
      );
      assertTrue(
        "map23.equals(r23)",
        map23.equals(r23)
      );
      assertTrue(
        "r23.equals(map23)",
        r23.equals(map23)
      );
      assertEquals(
        "map23.hashCode()==r23.hashCode()",
        map23.hashCode(),
        r23.hashCode()
      );
      assertTrue(
        "m23.equals(r23)",
        m23.equals(r23)
      );
      assertTrue(
        "r23.equals(m23)",
        r23.equals(m23)
      );
      assertEquals(
        "m23.hashCode()==r23.hashCode()",
        m23.hashCode(),
        r23.hashCode()
      );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testVariableSizeMappedRecord(
    ) throws Throwable {

        // VariableSizeMappedRecord without description
      MappedRecord r2m = factory.createMappedRecord("r2");
      r2m.putAll(map2);
      assertEquals(
        "r2m",
        "org.openmdx.base.resource.spi.VariableSizeMappedRecord",
        r2m.getClass().getName()
      );
      assertEquals(
        "r2m", 
        "r2: {\n" +
        "\tA = \"a\"\n" +
        "\tC = \"c\"\n" +
        "}",
        r2m.toString()
      );
      assertEquals(
        "r2.A",
        "a",
        r2m.get("A")
      );
      assertNull(
        "r2.B",
        r2m.get("B")
      );
      assertEquals(
        "r2.C",
        "c",
        r2m.get("C")
      );
    assertEquals(
      "r2m.size()",
      2,
      r2m.size()
    );
      assertTrue(
        "map2.equals(r2m)",
        map2.equals(r2m)
      );
      assertTrue(
        "r2m.equals(map2)",
        r2m.equals(map2)
      );
      assertEquals(
        "map2.hashCode()==r2m.hashCode()",
        map2.hashCode(),
        r2m.hashCode()
      );

        // VariableSizeMappedRecord with description
      MappedRecord r2md = factory.createMappedRecord("r2");
      r2md.setRecordShortDescription("Record 2");
      r2md.putAll(map2);
      assertEquals(
        "r2md",
        "org.openmdx.base.resource.spi.VariableSizeMappedRecord",
        r2md.getClass().getName()
      );
      assertEquals(
        "r2md", 
        "r2 (Record 2): {\n" +
        "\tA = \"a\"\n" +
        "\tC = \"c\"\n" +
        "}",
        r2md.toString()
      );
      assertTrue(
        "map2.equals(r2md)",
        map2.equals(r2md)
      );
      assertTrue(
        "r2md.equals(map2)",
        r2md.equals(map2)
      );
      assertEquals(
        "map2.hashCode()==r2md.hashCode()",
        map2.hashCode(),
        r2md.hashCode()
      );
      assertTrue(
        "r2md.equals(r2m)",
        r2md.equals(r2m)
      );
      assertTrue(
        "r2m.equals(r2md)",
        r2m.equals(r2md)
      );
      assertEquals(
        "r2m.hashCode()==r2md.hashCode()",
        r2m.hashCode(),
        r2md.hashCode()
      );

        // Nested VariableSizeMappedRecords
      MappedRecord r23 = factory.createMappedRecord("r23");
      r23.setRecordShortDescription("Nested Utilities");
      r23.put(new Integer(2),r2md);
      r23.put(new Integer(3),factory.createMappedRecord("r3",null,map3));
      assertEquals(
        "r23",
        "org.openmdx.base.resource.spi.VariableSizeMappedRecord",
        r23.getClass().getName()
      );
      assertEquals(
        "r23", 
        "r23 (Nested Utilities): {\n" +
        "\t2 = r2 (Record 2): {\n" +
        "\t\tA = \"a\"\n" +
        "\t\tC = \"c\"\n" +
        "\t}\n" +
        "\t3 = r3: {\n" +
        "\t\tA = \"a\"\n" +
        "\t\tB = \"b\"\n" +
        "\t\tC = \"c\"\n" +
        "\t}\n" +
        "}",
        r23.toString()
      );
      assertTrue(
        "map23.equals(r23)",
        map23.equals(r23)
      );
      assertTrue(
        "r23.equals(map23)",
        r23.equals(map23)
      );
      assertEquals(
        "map23.hashCode()==r23.hashCode()",
        map23.hashCode(),
        r23.hashCode()
      );
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testPreInitializedIndexedRecord(
    ) throws Throwable {
        
        // VariableSizeIndexedRecord without description
      IndexedRecord r2i = factory.createIndexedRecord(
        "r2", null, list2
      );
      assertEquals(
        "r2i",
        "org.openmdx.base.resource.spi.VariableSizeIndexedRecord",
        r2i.getClass().getName()
      );
      assertEquals(
        "r2i", 
        "r2: [\n" +
        "\t0: \"a\"\n" +
        "\t1: \"c\"\n" +
        "]",
        r2i.toString()
      );
    assertEquals(
      "r2i[0]",
      "a",
      r2i.get(0)
    );
    assertEquals(
      "r2i[1]",
      "c",
      r2i.get(1)
    );
    assertEquals(
      "r2i.size()",
      2,
      r2i.size()
    );
      assertTrue(
        "list2.equals(r2i)",
        list2.equals(r2i)
      );
      assertTrue(
        "r2i.equals(list2)",
        r2i.equals(list2)
      );
      assertEquals(
        "list2.hashCode()==r2i.hashCode()",
        list2.hashCode(),
        r2i.hashCode()
      );

        // VariableSizeIndexedRecord with description
      Record r2id = factory.createIndexedRecord(
        "r2", "Record 2", list2
      );
      assertEquals(
        "r2id",
        "org.openmdx.base.resource.spi.VariableSizeIndexedRecord",
        r2id.getClass().getName()
      );
      assertEquals(
        "r2id", 
        "r2 (Record 2): [\n" +
        "\t0: \"a\"\n" +
        "\t1: \"c\"\n" +
        "]",
        r2id.toString()
      );
      assertTrue(
        "list2.equals(r2id)",
        list2.equals(r2id)
      );
      assertTrue(
        "r2id.equals(list2)",
        r2id.equals(list2)
      );
      assertEquals(
        "list2.hashCode()==r2id.hashCode()",
        list2.hashCode(),
        r2id.hashCode()
      );
      assertTrue(
        "r2id.equals(r2i)",
        r2id.equals(r2i)
      );
      assertTrue(
        "r2i.equals(r2id)",
        r2i.equals(r2id)
      );
      assertEquals(
        "r2id.hashCode()==r2i.hashCode()",
        r2id.hashCode(),
        r2i.hashCode()
      );

        // VariableSizeIndexedRecord with description
      List l23 = Arrays.asList(
        new Record[]{r2id,factory.createIndexedRecord("r3",null,list3)}
      );
      Record r23 = factory.createIndexedRecord(
        "r23", "Nested Utilities", l23
      );
      assertEquals(
        "r23",
        "org.openmdx.base.resource.spi.VariableSizeIndexedRecord",
        r23.getClass().getName()
      );
      assertEquals(
        "r23", 
        "r23 (Nested Utilities): [\n" +
        "\t0: r2 (Record 2): [\n" +
        "\t\t0: \"a\"\n" +
        "\t\t1: \"c\"\n" +
        "\t]\n" +
        "\t1: r3: [\n" +
        "\t\t0: \"a\"\n" +
        "\t\t1: \"b\"\n" +
        "\t\t2: \"c\"\n" +
        "\t]\n" +
        "]",
        r23.toString()
      );
      assertTrue(
        "list23.equals(r23)",
       list23.equals(r23)
      );
      assertTrue(
        "r23.equals(list23)",
        r23.equals(list23)
      );
      assertEquals(
        "list23.hashCode()==r23.hashCode()",
        list23.hashCode(),
        r23.hashCode()
      );
      assertEquals(
        "list23.hashCode()==r23.hashCode()",
        list23.hashCode(),
        r23.hashCode()
      );
      assertTrue(
        "l23.equals(r23)",
        l23.equals(r23)
      );
      assertTrue(
        "r23.equals(l23)",
        r23.equals(l23)
      );
      assertEquals(
        "l23.hashCode()==r23.hashCode()",
        l23.hashCode(),
        r23.hashCode()
      );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testVariableSizeIndexedRecord(
    ) throws Throwable {

        // VariableSizeIndexedRecord without description
      IndexedRecord r2i = factory.createIndexedRecord("r2");
      r2i.addAll(list2);
      assertEquals(
        "r2i",
        "org.openmdx.base.resource.spi.VariableSizeIndexedRecord",
        r2i.getClass().getName()
      );
      assertEquals(
        "r2i", 
        "r2: [\n" +
        "\t0: \"a\"\n" +
        "\t1: \"c\"\n" +
        "]",
        r2i.toString()
      );
    assertEquals(
      "r2i[0]",
      "a",
      r2i.get(0)
    );
    assertEquals(
      "r2i[1]",
      "c",
      r2i.get(1)
    );
    assertEquals(
      "r2i.size()",
      2,
      r2i.size()
    );
      assertTrue(
        "list2.equals(r2i)",
        list2.equals(r2i)
      );
      assertTrue(
        "r2i.equals(list2)",
        r2i.equals(list2)
      );
      assertEquals(
        "list2.hashCode()==r2i.hashCode()",
        list2.hashCode(),
        r2i.hashCode()
      );

        // VariableSizeIndexedRecord with description
      IndexedRecord r2id = factory.createIndexedRecord("r2");
      r2id.setRecordShortDescription("Record 2");
      r2id.addAll(list2);
      assertEquals(
        "r2id",
        "org.openmdx.base.resource.spi.VariableSizeIndexedRecord",
        r2id.getClass().getName()
      );
      assertEquals(
        "r2id", 
        "r2 (Record 2): [\n" +
        "\t0: \"a\"\n" +
        "\t1: \"c\"\n" +
        "]",
        r2id.toString()
      );
      assertTrue(
        "list2.equals(r2id)",
        list2.equals(r2id)
      );
      assertTrue(
        "r2id.equals(list2)",
        r2id.equals(list2)
      );
      assertEquals(
        "list2.hashCode()==r2id.hashCode()",
        list2.hashCode(),
        r2id.hashCode()
      );
      assertTrue(
        "r2id.equals(r2i)",
        r2id.equals(r2i)
      );
      assertTrue(
        "r2i.equals(r2id)",
        r2i.equals(r2id)
      );
      assertEquals(
        "r2id.hashCode()==r2i.hashCode()",
        r2id.hashCode(),
        r2i.hashCode()
      );

        // Nested VariableSizeIndexedRecords
      List l23 = Arrays.asList(
        new Record[]{r2id,factory.createIndexedRecord("r3",null,list3)}
      );
      Record r23 = factory.createIndexedRecord(
        "r23", "Nested Utilities", l23
      );
      assertEquals(
        "r23",
        "org.openmdx.base.resource.spi.VariableSizeIndexedRecord",
        r23.getClass().getName()
      );
      assertEquals(
        "r23", 
        "r23 (Nested Utilities): [\n" +
        "\t0: r2 (Record 2): [\n" +
        "\t\t0: \"a\"\n" +
        "\t\t1: \"c\"\n" +
        "\t]\n" +
        "\t1: r3: [\n" +
        "\t\t0: \"a\"\n" +
        "\t\t1: \"b\"\n" +
        "\t\t2: \"c\"\n" +
        "\t]\n" +
        "]",
        r23.toString()
      );
      assertTrue(
        "list23.equals(r23)",
       list23.equals(r23)
      );
      assertTrue(
        "r23.equals(list23)",
        r23.equals(list23)
      );
      assertEquals(
        "list23.hashCode()==r23.hashCode()",
        list23.hashCode(),
        r23.hashCode()
      );
      assertEquals(
        "list23.hashCode()==r23.hashCode()",
        list23.hashCode(),
        r23.hashCode()
      );
      assertTrue(
        "l23.equals(r23)",
        l23.equals(r23)
      );
      assertTrue(
        "r23.equals(l23)",
        r23.equals(l23)
      );
      assertEquals(
        "l23.hashCode()==r23.hashCode()",
        l23.hashCode(),
        r23.hashCode()
      );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testObjectArrayIndexedRecord(
    ) throws Throwable {

        // FixedSizeIndexedRecord without description
      IndexedRecord r2i = factory.asIndexedRecord(
        "r2", null, array2
      );
      assertEquals(
        "r2i",
        "org.openmdx.base.resource.spi.FixedSizeIndexedRecord",
        r2i.getClass().getName()
      );
      assertEquals(
        "r2i", 
        "r2: [\n" +
        "\t0: \"a\"\n" +
        "\t1: \"c\"\n" +
        "]",
        r2i.toString()
      );
      Object a2i=Records.nDimensionalArray(r2i);
      assertEquals(
        "a2i",
        "[Ljava.lang.String;",
        a2i.getClass().getName()
      );
      assertTrue(
        "Arrays.equals(array2,a2i)",
        Arrays.equals(array2,(Object[])a2i)
      );
    assertEquals(
      "r2i[0]",
      "a",
      r2i.get(0)
    );
    assertEquals(
      "r2i[1]",
      "c",
      r2i.get(1)
    );
    assertEquals(
      "r2i.size()",
      2,
      r2i.size()
    );
      assertTrue(
        "list.equals(r2i)",
        list2.equals(r2i)
      );
      assertTrue(
        "r2i.equals(list2)",
        r2i.equals(list2)
      );
      assertEquals(
        "list2.hashCode()==r2i.hashCode()",
        list2.hashCode(),
        r2i.hashCode()
      );

        // FixedSizeIndexedRecord with description
      IndexedRecord r2id = factory.asIndexedRecord(
        "r2", "Record 2", array2
      );
      assertEquals(
        "r2id",
        "org.openmdx.base.resource.spi.FixedSizeIndexedRecord",
        r2id.getClass().getName()
      );
      Object a2id=Records.nDimensionalArray(r2id);
      assertEquals(
        "a2id",
        "[Ljava.lang.String;",
        a2id.getClass().getName()
      );
      assertTrue(
        "Arrays.equals(array2,a2id)",
        Arrays.equals(array2,(Object[])a2id)
      );
      assertEquals(
        "r2id", 
        "r2 (Record 2): [\n" +
        "\t0: \"a\"\n" +
        "\t1: \"c\"\n" +
        "]",
        r2id.toString()
      );
      assertTrue(
        "list2.equals(r2id)",
        list2.equals(r2id)
      );
      assertTrue(
        "r2id.equals(list2)",
        r2id.equals(list2)
      );
      assertEquals(
        "list2.hashCode()==r2id.hashCode()",
        list2.hashCode(),
        r2id.hashCode()
      );
      assertTrue(
        "r2id.equals(r2i)",
        r2id.equals(r2i)
      );
      assertTrue(
        "r2i.equals(r2id)",
        r2i.equals(r2id)
      );
      assertEquals(
        "r2id.hashCode()==r2i.hashCode()",
        r2id.hashCode(),
        r2i.hashCode()
      );

        // Nested FixedSizeIndexedRecords
      IndexedRecord r23 = factory.asIndexedRecord(
        "r23", 
        "Nested Utilities", 
        new Record[]{
            r2id,factory.asIndexedRecord("r3",null,array3)
          }
      );
      assertEquals(
        "r23",
        "org.openmdx.base.resource.spi.FixedSizeIndexedRecord",
        r23.getClass().getName()
      );
      Object a23=Records.nDimensionalArray(r23);
      assertEquals(
        "a23",
        "[[Ljava.lang.String;",
        a23.getClass().getName()
      );
      assertEquals(
        "array23.length==r23.length",
        array23.length,
        ((Object[])a23).length
      );
      for(
        int i=0;
        i<array23.length;
        i++
      ){
          assertTrue(
            "Arrays.equals(array23["+i+"],a23["+i+"])",
            Arrays.equals(
                array23[i],
                ((Object[][])a23)[i]
              )
          );
        }
      assertEquals(
        "r23", 
        "r23 (Nested Utilities): [\n" +
        "\t0: r2 (Record 2): [\n" +
        "\t\t0: \"a\"\n" +
        "\t\t1: \"c\"\n" +
        "\t]\n" +
        "\t1: r3: [\n" +
        "\t\t0: \"a\"\n" +
        "\t\t1: \"b\"\n" +
        "\t\t2: \"c\"\n" +
        "\t]\n" +
        "]",
        r23.toString()
      );
      assertTrue(
        "list23.equals(r23)",
       list23.equals(r23)
      );
      assertTrue(
        "r23.equals(list23)",
        r23.equals(list23)
      );
      assertEquals(
        "list23.hashCode()==r23.hashCode()",
        list23.hashCode(),
        r23.hashCode()
      );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testIntegerArrayIndexedRecord(
    ) throws Throwable {

        // FixedSizeIndexedRecord without description
      IndexedRecord r0i = factory.asIndexedRecord(
        "r0", null, array0
      );
      assertEquals(
        "r0i",
        "org.openmdx.base.resource.spi.FixedSizeIndexedRecord",
        r0i.getClass().getName()
      );
      assertEquals(
        "r0i", 
        "r0: []",
        r0i.toString()
      );
      Object a0i=Records.nDimensionalArray(r0i);
      assertEquals(
        "a01",
        "[I",
        a0i.getClass().getName()
      );
      assertTrue(
        "Arrays.equals(array0,a0i)",
        Arrays.equals(array0,(int[])a0i)
      );
    assertEquals(
      "r0i.size()",
      0,
      r0i.size()
    );
      assertTrue(
        "list0.equals(r0i)",
        list0.equals(r0i)
      );
      assertTrue(
        "r0i.equals(list0)",
        r0i.equals(list0)
      );
      assertEquals(
        "list0.hashCode()==r0i.hashCode()",
        list0.hashCode(),
        r0i.hashCode()
      );

        // FixedSizeIndexedRecord with description
      IndexedRecord r1id = factory.asIndexedRecord(
        "r1", "Record 1", array1
      );
      assertEquals(
        "r1id",
        "org.openmdx.base.resource.spi.FixedSizeIndexedRecord",
        r1id.getClass().getName()
      );
      Object a1id=Records.nDimensionalArray(r1id);
      assertEquals(
        "a1id",
        "[I",
        a1id.getClass().getName()
      );

for(
    int t=0;
    t<array1.length;
    t++
)System.out.println("array1["+t+"]="+array1[t]);
for(
    int t=0;
    t<((int[])a1id).length;
    t++
)System.out.println("a1id["+t+"]="+((int[])a1id)[t]);

      assertTrue(
        "Arrays.equals(array1,a1id)",
        Arrays.equals(array1,(int[])a1id)
      );
      assertEquals(
        "r1id", 
        "r1 (Record 1): [\n"+
        "\t0: 99\n"+
        "]",
        r1id.toString()
      );
      assertTrue(
        "list1.equals(r1id)",
        list1.equals(r1id)
      );
      assertTrue(
        "r1id.equals(list1)",
        r1id.equals(list1)
      );
      assertEquals(
        "list1.hashCode()==r1id.hashCode()",
        list1.hashCode(),
        r1id.hashCode()
      );

        // Nested FixedSizeIndexedRecords
      IndexedRecord r01 = factory.asIndexedRecord(
        "r01", 
        "Nested Utilities", 
        new Record[]{r0i,r1id}
      );
      assertEquals(
        "r01",
        "org.openmdx.base.resource.spi.FixedSizeIndexedRecord",
        r01.getClass().getName()
      );
      Object a01=Records.nDimensionalArray(r01);
      assertEquals(
        "a01",
        "[[I",
        a01.getClass().getName()
      );
      assertEquals(
        "array01.length==r01.length",
        array01.length,
        ((Object[])a01).length
      );
      for(
        int i=0;
        i<array01.length;
        i++
      ){
          assertTrue(
            "Arrays.equals(array01["+i+"],a01["+i+"])",
            Arrays.equals(
                array01[i],
                ((int[][])a01)[i]
              )
          );
        }
      assertEquals(
        "r01", 
        "r01 (Nested Utilities): [\n" +
        "\t0: r0: []\n" +
        "\t1: r1 (Record 1): [\n" +
        "\t\t0: 99\n" +
        "\t]\n" +
        "]",
        r01.toString()
      );
      assertTrue(
        "list01.equals(r01)",
       list01.equals(r01)
      );
      assertTrue(
        "r01.equals(list01)",
        r01.equals(list01)
      );
      assertEquals(
        "list01.hashCode()==r01.hashCode()",
        list01.hashCode(),
        r01.hashCode()
      );
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     */
    public void testMixedArrayIndexedRecord(
    ) throws Throwable {
        //...
    }

        
  //--------------------------------------------------------------------------
    // Instance members
    //--------------------------------------------------------------------------
    
    /**
   * {A=a, C=c}
   */
    Map map2;

    /**
   * {A=a, B=b, C=c}
   */
    Map map3;

    /**
   * {2={A=a, C=c}, 3={A=a, B=b, C=c}}
   */
    Map map23;
  
    /**
   * [a, c]
   */
    List list2;
    String[] array2;

    /**
   * [a, b, c]
   */
    List list3;
    String[] array3;

    /**
   * [[a, c], [a, b, c]]
   */
    List list23;
    String[][] array23;

    /**
   * []
   */
  List list0;
    int[] array0;

    /**
   * [99]
   */
  List list1;
    int[] array1;

    /**
   * [[], [99]]
   */
  List list01;
    int[][] array01;


  ExtendedRecordFactory factory;
  
}

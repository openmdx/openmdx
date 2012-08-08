/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestConfiguration.java,v 1.8 2006/05/21 21:07:10 hburger Exp $
 * Description: class TestConfiguration
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/05/21 21:07:10 $
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
package org.openmdx.test.base.application.configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.base.application.configuration.AbstractConfiguration;
import org.openmdx.base.application.configuration.ConfigurationEntryDescription;
import org.openmdx.kernel.application.configuration.Report;

public class TestConfiguration extends TestCase {

	/**
	 * The Configuration class makes AbstractConfiguration testable
	 */
	private static class Configuration extends AbstractConfiguration
	{
		public Configuration(
			String  name,
			int     majorVersion,
			int     minorVersion)
		{
			super(name, majorVersion, minorVersion);
		}

		public Report validate()
		{
			return verify();  // just delegate
		}
		
		public void setEntryDescription(
                ConfigurationEntryDescription[] entryDescriptions)
            throws IllegalArgumentException
        {
            addEntryDescription(entryDescriptions);
        }
	}



	/**
	 * Constructs a test case with the given name.
	 */
	public TestConfiguration(String name)
	{
		super(name);
	}

	/**
	 * The batch TestRunner can be given a class to run directly.
	 * To start the batch runner from your main you can write:
	 */
	public static void main (String[] args)
	{
		junit.textui.TestRunner.run (suite());
	}

	/**
	 * A test runner either expects a static method suite as the
	 * entry point to get a test to run or it will extract the
	 * suite automatically.
	 */
	public static Test suite()
	{
		return new TestSuite(TestConfiguration.class);
	}


    public void testVersionInfo()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        assertEquals("Name",   "Test", config.getName());
        assertEquals("Major",       1, config.getMajorVersion());
        assertEquals("Minor",       0, config.getMinorVersion());

        config.setVersion(2,9);

        assertEquals("Major (new)", 2, config.getMajorVersion());
        assertEquals("Minor (new)", 9, config.getMinorVersion());

    }


    public void testVerifyEmptyConfig()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        assertTrue(config.verify().isSuccess());

        ConfigurationEntryDescription arr[];
        arr = new ConfigurationEntryDescription[]{
			new ConfigurationEntryDescription("a", null, String.class)
        };

        config.setEntryDescription(arr);        
    }


    public void testSingleString1()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        config.setValue("a.b.c", "abc");
        config.setValue("x.y.z", "xyz");
        config.setValue("1.2.3", null);

        assertEquals("a.b.c", "abc", config.getValue("a.b.c"));
        assertEquals("x.y.z", "xyz", config.getValue("x.y.z"));
        assertNull("1.2.3", config.getValue("1.2.3"));
    }


    public void testSingleString2()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        config.setValues("a.b.c", new Object[]{"abc"});
        config.setValues("x.y.z", new Object[]{"xyz"});
        config.setValues("1.2.3", null);

        assertEquals("a.b.c", "abc", config.getValue("a.b.c"));
        assertEquals("x.y.z", "xyz", config.getValue("x.y.z"));
        assertNull("1.2.3", config.getValue("1.2.3"));
    }


    public void testSingleString3()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        config.setValue("a.b.c", "abc");
        config.setValue("x.y.z", "xyz");
        config.setValue("1.2.3", null);

        // use the iterator
		Iterator iter = config.getEntryNames().iterator();
		while(iter.hasNext()) {
			String name = (String)iter.next();
			if (name.equals("a.b.c")) {
		        assertEquals("a.b.c", "abc", config.getValue("a.b.c"));
			}else if (name.equals("x.y.z")) {
		        assertEquals("x.y.z", "xyz", config.getValue("x.y.z"));
			}else if (name.equals("1.2.3")) {
		        assertNull("1.2.3", config.getValue("1.2.3"));
			}else{
				fail("Unexpected entry " + name);
			}
		}
    }


    public void testSingleRemove()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        config.setValue("a.b.c", "abc");
        config.setValue("x.y.z", "xyz");
        config.setValue("1.2.3", null);

        assertEquals("a.b.c", "abc", config.getValue("a.b.c"));
        assertEquals("x.y.z", "xyz", config.getValue("x.y.z"));
        assertNull("1.2.3", config.getValue("1.2.3"));

        config.removeEntry("x.y.z");

        assertEquals("a.b.c", "abc", config.getValue("a.b.c"));
        assertNull("x.y.z", config.getValue("x.y.z"));
        assertNull("1.2.3", config.getValue("1.2.3"));
    }


    public void testRemoveAll()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        config.setValue("a.b.c", "abc");
        config.setValue("x.y.z", "xyz");
        config.setValue("1.2.3", null);

        assertEquals("a.b.c", "abc", config.getValue("a.b.c"));
        assertEquals("x.y.z", "xyz", config.getValue("x.y.z"));
        assertNull("1.2.3", config.getValue("1.2.3"));

        config.removeAllEntries();

		// count entries
		int entries = 0;
		Iterator iter = config.getEntryNames().iterator();
		while(iter.hasNext()) {
			iter.next();
			entries++;
		}
        assertEquals("empty", 0, entries);
    }


    public void testVerifyHoleMultiString1()
    {
        ConfigurationEntryDescription arr[];
        arr = new ConfigurationEntryDescription[]{
			new ConfigurationEntryDescription("a", null, String.class, 4, 4)
        };

        Configuration config  = new Configuration("Test", 1, 0);
		config.setEntryDescription(arr);

        config.setValue("a[0]", "a[0]");
        config.setValue("a[1]", "a[1]");
        config.setValue("a[2]", "a[2]");
        config.setValue("a[3]", "a[3]");

        Object obj[] = config.getValues("a").toArray();

        assertNotNull("a", obj);
        assertEquals("a", 4, obj.length);
        assertEquals("a[0]", "a[0]", obj[0]);
        assertEquals("a[1]", "a[1]", obj[1]);
        assertEquals("a[2]", "a[2]", obj[2]);
        assertEquals("a[3]", "a[3]", obj[3]);

        assertTrue(config.verify().isSuccess());

		// produce a hole
        config.setValue("a[2]", null);

        assertFalse(config.verify().isSuccess());
    }


    public void testMultiplicity()
    {
        Configuration config  = new Configuration("Test", 1, 0);
        
        config.setEntryDescription(
            new ConfigurationEntryDescription[]{
                new ConfigurationEntryDescription(
                        "a", null, String.class, 4, 4) } );

        config.setValues("a", new Object[]{"0","1","2","3"});

        Object obj[] = config.getValues("a").toArray();

        assertNotNull("a", obj);
        assertEquals("a", 4, obj.length);
        assertEquals("a[0]", "0", obj[0]);
        assertEquals("a[1]", "1", obj[1]);
        assertEquals("a[2]", "2", obj[2]);
        assertEquals("a[3]", "3", obj[3]);

        assertTrue(config.verify().isSuccess());

        // produce a hole
        config.setValue("a[2]", null);

        assertFalse(config.verify().isSuccess());



        config  = new Configuration("Test", 1, 0);
        
        config.setEntryDescription(
            new ConfigurationEntryDescription[]{
                new ConfigurationEntryDescription(
                        "a", null, String.class, 2, 3) } );

        config.setValues("a", new Object[]{"0"});
        assertFalse(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1","2"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1", null, null, "2"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1","2","3"});
        assertFalse(config.verify().isSuccess());



        config  = new Configuration("Test", 1, 0);
        
        config.setEntryDescription(
            new ConfigurationEntryDescription[]{
                new ConfigurationEntryDescription(
                        "a", null, String.class, 0, 3) } );

        config.setValues("a", new Object[]{"0"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1","2"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1", null, null, "2"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1","2","3"});
        assertFalse(config.verify().isSuccess());





        config  = new Configuration("Test", 1, 0);
        
        config.setEntryDescription(
            new ConfigurationEntryDescription[]{
                new ConfigurationEntryDescription(
                        "a", null, String.class, 2, 0) } );

        config.setValues("a", new Object[]{"0"});
        assertFalse(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1","2"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1", null, null, "2"});
        assertTrue(config.verify().isSuccess());

        config.setValues("a", new Object[]{"0","1","2","3"});
        assertTrue(config.verify().isSuccess());
 
 
 		try {
            config.setEntryDescription(
                new ConfigurationEntryDescription[]{
                    new ConfigurationEntryDescription(
                            "a", null, String.class, 2, 0) } );
 		    
 		    fail("Unexpected IllegalArgumentException");
 		}catch(IllegalArgumentException ex) {
 		}
    }


    public void testEntryExists()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        config.setValue("a[0]", "a");
        config.setValue("a[1]", "a");
        config.setValue("b[0]", null);
        config.setValue("b[1]", null);
        config.setValue("c", "c");
        config.setValue("d", null);

        assertTrue(config.entryExists("a"));
        assertFalse(config.entryExists("b"));
        assertTrue(config.entryExists("c"));
        assertFalse(config.entryExists("d"));

        assertFalse(config.entryExists(""));
        assertFalse(config.entryExists(null));
        assertFalse(config.entryExists("any-unknown-entry"));
    }


    public void testArraySuffix()
    {
        Configuration config  = new Configuration("Test", 1, 0);


		// valid multi-valued entry
        config.setValue("x[0]", "x[0]");
        config.setValue("x[1]", "x[1]");
        Object obj1[] = config.getValues("x").toArray();
        assertNotNull("x", obj1);
        assertEquals("x", 2, obj1.length);
        assertEquals("x", "x[0]", obj1[0]);
        assertEquals("x", "x[1]", obj1[1]);

		// valid indexed multi-valued entry
        config.setValue("y[0][0]", "y[0][0]");
        config.setValue("y[0][1]", "y[0][1]");
        Object obj2[] = config.getValues("y[0]").toArray();
        assertNotNull("y", obj2);
        assertEquals("y", 2, obj2.length);
        assertEquals("y", "y[0][0]", obj2[0]);
        assertEquals("y", "y[0][1]", obj2[1]);


		// invalid array suffix
        try {
	        config.setValue("a.b.c1]",   "abc[1]");
        	fail("IllegalArgumentException expected");
        }catch(IllegalArgumentException ex) {
        	assertTrue("a.b.c1]", true);
        }catch(Throwable th) {
        	fail("No exception expected");
        }

		// bad index (not an integer)
        try {
	        config.setValue("a.b.c[.]",  "abc[2]");
        	fail("IllegalArgumentException expected");
        }catch(IllegalArgumentException ex) {
        	assertTrue("a.b.c[.]", true);
        }catch(Throwable th) {
        	fail("No exception expected");
        }

		// allowed (but probably not desired)
        try {
        	config.setValue("a.b.c[0",   "abc[0]");
        	assertTrue("a.b.c[0", true);
        }catch(IllegalArgumentException ex) {
        	fail("Exception IllegalArgumentException not expected");
        }catch(Throwable th) {
        	fail("No exception expected");
        }

		// allowed
        try {
	        config.setValue("a.b.c[3].d", "abc[4]");
        	assertTrue("a.b.c[3].d", true);
        }catch(IllegalArgumentException ex) {
        	fail("Exception IllegalArgumentException not expected");
        }catch(Throwable th) {
        	fail("No exception expected");
        }

		// index out of bound
        try {
	        config.setValue("a.b.c[-1]", "abc[5]");
        	fail("IndexOutOfBoundsException expected");
        }catch(IndexOutOfBoundsException ex) {
        	assertTrue("a.b.c[-1]", true);
        }catch(Throwable th) {
        	fail("No exception expected");
        }

    }


    public void testInvalidEntryNames()
    {
        Configuration config  = new Configuration("Test", 1, 0);

        config.setValue(""  ,    "abc");
        config.setValue(null,    "xyz");
        config.setValue("a.b.c", "abc");

		// count entries
		int entries = 0;
		Iterator iter = config.getEntryNames().iterator();
		while(iter.hasNext()) {
			iter.next();
			entries++;
		}

        assertEquals("invalid names", 1, entries);
    }


    public void testInheritedTypes()
    {
        ConfigurationEntryDescription arr[];

        arr = new ConfigurationEntryDescription[]{
			new ConfigurationEntryDescription("string" , null, String.class),
			new ConfigurationEntryDescription("bool"   , null, Boolean.class),
			new ConfigurationEntryDescription("byte"   , null, Byte.class),
			new ConfigurationEntryDescription("int"    , null, Number.class),
			new ConfigurationEntryDescription("long"   , null, Number.class),
			new ConfigurationEntryDescription("decimal", null, Number.class),
			new ConfigurationEntryDescription("float"  , null, Number.class),
			new ConfigurationEntryDescription("double" , null, Number.class)
        };

        Configuration config  = new Configuration("Test", 1, 0);
		config.setEntryDescription(arr);

        config.setValue("string" , new String());
        config.setValue("bool"   , new Boolean(true));
        config.setValue("byte"   , new Byte((byte)0));
        config.setValue("int"    , new Integer(0));
        config.setValue("long"   , new Long(0));
        config.setValue("decimal", new java.math.BigDecimal(0));
        config.setValue("float"  , new Float(0.0));
        config.setValue("double" , new Double(0.0));

        assertTrue(config.verify().isSuccess());
    }


    public void testUndescribedEntry()
    {
        ConfigurationEntryDescription arr[];

        arr = new ConfigurationEntryDescription[]{
            new ConfigurationEntryDescription("a", null, String.class)
        };

        Configuration config  = new Configuration("Test", 1, 0);
        config.setEntryDescription(arr);

        config.setValue("a", "abc");
        config.setValue("b", "abc");
        config.setValue("c", new Date());

        assertFalse(config.verify().isSuccess());
    }


    public void testOptionalEntry()
    {
        ConfigurationEntryDescription arr[];

        arr = new ConfigurationEntryDescription[]{
            new ConfigurationEntryDescription(
						"a", null, String.class, "default", null)
        };

        Configuration config  = new Configuration("Test", 1, 0);
        config.setEntryDescription(arr);

        // defaults not yet applied -> error entry "a" is missing
        assertFalse(config.verify().isSuccess());
        
        config.applyDefaults();

        // now it should be ok
        assertTrue(config.verify().isSuccess());
    }


    public void testMandatoryEntry()
    {
        ConfigurationEntryDescription arr[];

        arr = new ConfigurationEntryDescription[]{
            new ConfigurationEntryDescription("a", null, String.class)
        };

        Configuration config  = new Configuration("Test", 1, 0);
        config.setEntryDescription(arr);

        assertFalse(config.verify().isSuccess());
        
        config.setValue("a", "aaa");

        // now it should be ok
        assertTrue(config.verify().isSuccess());
    }


    public void testAnyTypes()
    {
        ConfigurationEntryDescription arr[];

        arr = new ConfigurationEntryDescription[]{
			new ConfigurationEntryDescription("string", null, String.class),
			new ConfigurationEntryDescription("hashmap", null, HashMap.class),
			new ConfigurationEntryDescription("date", null, Date.class)
        };

        Configuration config  = new Configuration("Test", 1, 0);
		config.setEntryDescription(arr);

        config.setValue("string", new String());
        config.setValue("hashmap", new HashMap());
        config.setValue("date", new Date());

        assertTrue(config.verify().isSuccess());
    }
    

    public void testMultiMixed()
    {
        ConfigurationEntryDescription arr[];

        arr = new ConfigurationEntryDescription[]{
			new ConfigurationEntryDescription("a", null, String.class, -1, -1)
        };

        Configuration config  = new Configuration("Test", 1, 0);
		config.setEntryDescription(arr);

        config.setValue("a[0]", "a[0]");
        config.setValue("a[1]", "a[1]");
        config.setValue("a[2]", new Date());
        config.setValue("a[3]", "a[3]");

        assertFalse(config.verify().isSuccess());
    }


    public void testEntryDescriptions()
    {
        ConfigurationEntryDescription arr[];
        ConfigurationEntryDescription  d1, d2;
        
		d1 = new ConfigurationEntryDescription(
					"entry1", "ann1", String.class, "default", null);
		d2 = new ConfigurationEntryDescription(
					"entry2", "ann2", Long.class, new Long(100), null);
		
        arr = new ConfigurationEntryDescription[]{d1, d2};

        Configuration config  = new Configuration("Test", 1, 0);
        config.setEntryDescription(arr);
        
        Map entryMap = config.getEntryDescriptions();
        assertEquals("size", 2, entryMap.size());

        assertTrue("entry1", entryMap.get("entry1") != null);
        assertTrue("entry2", entryMap.get("entry2") != null);
    }
    
    
    public void testAddConfig()
    {
        Configuration config1  = new Configuration("Test", 1, 0);
        Configuration config2  = new Configuration("Test", 1, 0);
        Configuration config3  = new Configuration("TEST", 2, 0);

        config1.setValue("a", "aa");
        config1.setValue("b", "bb");

        config2.setValue("a", "aa");
        config2.setValue("b", "bb");

        config3.setValue("a", "aaaa");
        config3.setValue("c", "cc");

        config1.addAll(config3, true);
        assertEquals("N", "Test", config1.getName());
        assertEquals("V", 1, config1.getMajorVersion());
        assertEquals("V", 0, config1.getMinorVersion());
        assertEquals("a", "aa", config1.getValue("a"));
        assertEquals("b", "bb", config1.getValue("b"));
        assertEquals("c", "cc", config1.getValue("c"));
        
        config2.addAll(config3, false);
        assertEquals("N", "Test", config2.getName());
        assertEquals("V", 1, config2.getMajorVersion());
        assertEquals("V", 0, config2.getMinorVersion());
        assertEquals("a", "aaaa", config2.getValue("a"));
        assertEquals("b", "bb", config2.getValue("b"));
        assertEquals("c", "cc", config2.getValue("c"));
    }


    public void testToString()
    {
        Configuration config = new Configuration("Test", 1, 0);

        config.setValue("a", "aa");
        config.setValue("b", "bb");
        config.setValue("c[0]", "0");
        config.setValue("c[1]", "1");
        
        String representation = config.toString();
        assertTrue(
			"toString", 
			"CfgName: Test\nVersion: 1.0\nb=bb\na=aa\nc[0]=0\nc[1]=1".equals(representation) || // before JRE 1.4
			"CfgName: Test\nVersion: 1.0\na=aa\nc[0]=0\nc[1]=1\nb=bb".equals(representation)    // since JRE 1.4
		);
    }
    

    public void testDefaults()
    {
        ConfigurationEntryDescription arr[];

        arr = new ConfigurationEntryDescription[]{
			new ConfigurationEntryDescription("a1", null, String.class, "a-a", null),
			new ConfigurationEntryDescription("a2", null, String.class),
			new ConfigurationEntryDescription("a3", null, String.class, "a-a", null),
			new ConfigurationEntryDescription("a4", null, String.class),
			new ConfigurationEntryDescription("b1", null, String.class, "b-b", null),
			new ConfigurationEntryDescription("b2", null, String.class),
        };
			
        Configuration config  = new Configuration("Test", 1, 0);
		config.setEntryDescription(arr);
		
        config.setValue("a1", "aaa");
        config.setValue("a2", null);
        config.setValue("a3", null);
        config.setValue("a4", "aaa");

        config.applyDefaults();

        
        assertEquals("a1", "aaa", config.getValue("a1"));
        assertNull("a2", config.getValue("a2"));
        assertEquals("a3", "a-a", config.getValue("a3"));
        assertEquals("a4", "aaa", config.getValue("a4"));
        
        assertEquals("b1", "b-b", config.getValue("b1"));
        assertNull("b2", config.getValue("b2"));

        assertFalse(config.verify().isSuccess());
    }

}

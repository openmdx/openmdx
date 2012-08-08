/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestInternalMethodInvocationArguments.java,v 1.12 2009/01/12 17:51:00 wfro Exp $
 * Description: Test Internal Method Invocation Arguments
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 17:51:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.test.kernel.application.container;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmdx.application.dataprovider.cci.DataproviderReply;
import org.openmdx.application.dataprovider.cci.DataproviderReplyContexts;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.application.container.spi.ejb.InternalMethodInvocationArguments;
import org.w3c.cci2.ByteString;
import org.w3c.cci2.ByteStringInputStream;
import org.w3c.cci2.CharacterString;
import org.w3c.cci2.CharacterStringReader;

/**
 * Test Internal Method Invocation Arguments
 */
public class TestInternalMethodInvocationArguments extends TestCase {
    
    /**
     * Constructor
     */
    public TestInternalMethodInvocationArguments(
        String name
    ) {
        super(name);
    }

    private ByteString blob;
    private CharacterString clob;
    private SortedMap<String,Object> map;
    private DataproviderReply reply;
    private char[] characters = "anArray".toCharArray();
    private byte[] bytes = {10, 1, 1, 25};
    private static Character MARKER = 0x5A29;    

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
      return new TestSuite(TestInternalMethodInvocationArguments.class);
    }

    /**
     * Sets up the fixture, for example, open a network connection.
     * This method is called before a test is executed.
     */
    protected void setUp(
    ) throws Exception {
        this.map = new TreeMap<String,Object>();
        this.map.put("integer", Integer.valueOf(5));
        this.map.put("marker", MARKER);
        this.map.put("character", 'A');
        this.map.put("date", new Date());
        this.map.put("stackTraceElement", new StackTraceElement("MyClass","myMethod","", 4711));
        this.reply = new DataproviderReply();
        this.reply.context(DataproviderReplyContexts.HAS_MORE).set(0, Boolean.FALSE);
        this.reply.context(DataproviderReplyContexts.TOTAL).set(0, Integer.valueOf(1));
        this.clob = new CharacterString(this.characters);
        this.blob = new ByteString(this.bytes);
    }

    protected void tearDown(
    ) throws Exception {
        this.map = null;
    }

    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     * @throws Exception 
     * @throws ServiceException
     */
    public void testMap(
    ) throws Exception{
        InternalMethodInvocationArguments arguments = InternalMethodInvocationArguments.getInstance();
        arguments.put(this.map);
        SortedMap<String,Object> map = (SortedMap<String, Object>) arguments.get();
        assertEquals("equality", this.map, map);
        assertNotSame("cloning", this.map, map);
        assertSame("integer", this.map.get("integer"), map.get("integer"));
        assertEquals("date", this.map.get("date"), map.get("date"));
        assertNotSame("date", this.map.get("date"), map.get("date"));
        assertEquals("marker", this.map.get("marker"), map.get("marker"));
        assertSame("marker", this.map.get("marker"), map.get("marker"));
        assertEquals("character", this.map.get("character"), map.get("character"));
        assertNotSame("character", this.map.get("character"), map.get("character"));
        assertEquals("stackTraceElement", this.map.get("stackTraceElement"), map.get("stackTraceElement"));
        assertNotSame("stackTraceElement", this.map.get("stackTraceElement"), map.get("stackTraceElement"));
    }

    public void testCharacterString(
    ) throws Exception{
        InternalMethodInvocationArguments arguments = InternalMethodInvocationArguments.getInstance();
        arguments.put(this.clob);
        CharacterString clob = (CharacterString) arguments.get();
        assertSame("CLOB", this.clob, clob);
        clob = this.clob.subSequence(2, 7);
        CharSequence charSequence = clob;
        StringBuilder buffer = new StringBuilder("the").append(charSequence);
        assertEquals("toString", "Array", clob.toString());
        assertEquals("CharSequence", "theArray", buffer.toString());
        assertTrue("toArray", Arrays.equals(new char[]{'A', 'r', 'r', 'a', 'y'}, clob.toArray()));
        try {
            clob = this.clob.subSequence(2,8);
            fail("End too big");
        } catch (IndexOutOfBoundsException exception) {
            // Expected
        }
        try {
            clob = this.clob.subSequence(-1,8);
            fail("Start too small");
        } catch (IndexOutOfBoundsException exception) {
            // Expected
        }
    }

    public void testCharacterArray(
    ) throws Exception{
        InternalMethodInvocationArguments arguments = InternalMethodInvocationArguments.getInstance();
        arguments.put(this.characters);
        char[] characters = (char[]) arguments.get();
        assertTrue("char[]", Arrays.equals(this.characters, characters));
        assertNotSame("char[]", this.characters, characters);
        assertSame("CLOB", this.characters, clob.buffer());
    }

    public void testByteString(
    ) throws Exception{
        assertNotNull(this.blob);
        InternalMethodInvocationArguments arguments = InternalMethodInvocationArguments.getInstance();
        arguments.put(this.blob);
        ByteString blob = (ByteString) arguments.get();
        assertSame("BLOB", this.blob, blob);
        assertSame("BLOB", this.bytes, this.blob.buffer());
        blob = this.blob.subSequence(1,3);
        assertTrue("toArray", Arrays.equals(new byte[]{1,1}, blob.toArray()));
        assertEquals("toString", "0A010119", this.blob.toString());
        try {
            blob = this.blob.subSequence(2,8);
            fail("End too big");
        } catch (IndexOutOfBoundsException exception) {
            // Expected
        }
        try {
            blob = this.blob.subSequence(-1,2);
            fail("Start too small");
        } catch (IndexOutOfBoundsException exception) {
            // Expected
        }
    }

    public void testByteArray(
    ) throws Exception{
        InternalMethodInvocationArguments arguments = InternalMethodInvocationArguments.getInstance();
        arguments.put(this.bytes);
        byte[] bytes = (byte[]) arguments.get();
        assertTrue("byte[]", Arrays.equals(this.bytes, bytes));
        assertNotSame("byte[]", this.bytes, bytes);
    }
    
    /**
     * Write the test case method in the fixture class.
     * Be sure to make it public, or it can't be invoked through reflection. 
     * @throws Exception 
     * @throws ServiceException
     */
    public void testReply(
    ) throws Exception{
        InternalMethodInvocationArguments arguments = InternalMethodInvocationArguments.getInstance();
        arguments.put(this.reply);
        DataproviderReply reply = (DataproviderReply) arguments.get();
        assertEquals(
            "contexts", 
            this.reply.contexts(), 
            reply.contexts()
        );
        assertEquals(
            DataproviderReplyContexts.HAS_MORE, 
            this.reply.context(DataproviderReplyContexts.HAS_MORE), 
            reply.context(DataproviderReplyContexts.HAS_MORE)
        );
        assertEquals(
            DataproviderReplyContexts.TOTAL, 
            this.reply.context(DataproviderReplyContexts.TOTAL), 
            reply.context(DataproviderReplyContexts.TOTAL)
        );
        assertEquals(
            DataproviderReplyContexts.HAS_MORE + "[0]", 
            this.reply.context(DataproviderReplyContexts.HAS_MORE).get(0), 
            reply.context(DataproviderReplyContexts.HAS_MORE).get(0)
        );
        assertEquals(
            DataproviderReplyContexts.TOTAL + "[0]", 
            this.reply.context(DataproviderReplyContexts.TOTAL).get(0), 
            reply.context(DataproviderReplyContexts.TOTAL).get(0)
        );
        assertSame(
            DataproviderReplyContexts.TOTAL + "[0]", 
            this.reply.context(DataproviderReplyContexts.TOTAL).get(0), 
            reply.context(DataproviderReplyContexts.TOTAL).get(0)
        );
    }

    public void testByteStringInputStream(
    ) throws IOException{
        InputStream in = new ByteStringInputStream(this.blob);
        for(byte b : this.bytes) {
            assertEquals(b, in.read());
        }
        assertEquals("EOF", -1, in.read());
    }

    public void testCharacterStringReader(
    ) throws IOException {
        Reader in = new CharacterStringReader(this.clob);
        for(char c : this.characters) {
            assertEquals(c, in.read());
        }
        assertEquals("EOF", -1, in.read());
    }
    
}

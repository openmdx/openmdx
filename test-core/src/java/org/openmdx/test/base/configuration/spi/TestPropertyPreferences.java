/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestPropertyPreferences.java,v 1.5 2006/10/09 21:43:11 hburger Exp $
 * Description: Test Property Preferences 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/10/09 21:43:11 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

package org.openmdx.test.base.configuration.spi;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.openmdx.base.configuration.cci.ContextSensitivePreferences;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test Property Preferences
 */
public class TestPropertyPreferences
    extends TestCase
{

    /**
     * Constructor 
     *
     * @param name
     */
    public TestPropertyPreferences(String name) {
        super(name);
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
        return new TestSuite(TestPropertyPreferences.class);
    }
    
    /**
     * 
     */
    protected Preferences preferences;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp(
    ) throws Exception {
        this.preferences = ContextSensitivePreferences.containerNodeForPackage(getClass());
    }

    /**
     * 
     * @param preferences
     * @throws BackingStoreException
     */
    protected void showTree(
        Preferences preferences
    ) throws BackingStoreException {
        System.out.println(preferences.absolutePath());
        String[] keys = preferences.keys();
        for(
            int i = 0;
            i < keys.length;
            i++
        ) System.out.println(
            '\t' + keys[i] + " = " + preferences.get(keys[i], "null")
        );
        String[] children = preferences.childrenNames();
        for(
            int i = 0;
            i < children.length;
            i++
        ) showTree(preferences.node(children[i]));
    }

    /**
     * 
     *
     */
    public void testRead(){
        assertEquals(
            "java.vendor",
            "http://java.sun.com/",
            ContextSensitivePreferences.containerRoot().node("java/vendor").get("url","?")
        );            
        assertEquals(
            "Property file preference org.openmdx.test.base.configuration.spi.seven",
            "up",
            preferences.get("seven","down")
        );            
    }
    
    /**
     * 
     * @throws BackingStoreException
     */
    public void testWrite(
    ) throws BackingStoreException{
        //
        // Default
        // 
        assertEquals(
            "Default preference org.openmdx.test.base.configuration.spi.five",
            0,
            preferences.getInt("five",0)
        );
        assertEquals(
            "Default property org.openmdx.test.base.preferences.five",
            0,
            Integer.getInteger("org.openmdx.test.base.configuration.spi.five", 0).intValue()
        );
        //
        // Transient
        // 
        preferences.putInt("five", 5);
        assertEquals(
            "Transient preference org.openmdx.test.base.configuration.spi.five",
            5,
            preferences.getInt("five",0)
        );
        assertEquals(
            "Transient property org.openmdx.test.base.configuration.spi.five",
            0,
            Integer.getInteger("org.openmdx.test.base.configuration.spi.five", 0).intValue()
        );
        //
        // Persistent
        // 
        ContextSensitivePreferences.containerRoot().node("org/openmdx/test").sync();
        assertEquals(
            "Persistent preference org.openmdx.test.base.configuration.spi.five",
            5,
            preferences.getInt("five",0)
        );
        assertEquals(
            "Persistent property org.openmdx.test.base.configuration.spi.five",
            5,
            Integer.getInteger("org.openmdx.test.base.configuration.spi.five", 0).intValue()
        );
    }

    /**
     * 
     * @throws BackingStoreException
     */
    public void testShow(
    ) throws BackingStoreException{
        if(VERBOSE) showTree(ContextSensitivePreferences.containerRoot());
    }

    /**
     * Tells whether the whole tree should be shwon
     */
    private static final boolean VERBOSE = true;
    
}

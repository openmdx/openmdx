/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Macro Configuration Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2018-2021, OMEX AG, Switzerland
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.macros;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.collection.TreeSparseArray;
import org.w3c.cci2.SparseArray;

/**
 * Macro Configuration Test
 */
public class TestClassicMacroConfiguration {
    
    private SparseArray<String> stringMacroColumn;
    private SparseArray<String> stringMacroName;
    private SparseArray<String> stringMacroValue;
    private SparseArray<String> pathMacroName;
    private SparseArray<String> pathMacroValue;
    private MacroConfiguration testee;
    
    @BeforeEach
    public void setUp() {
        testee = new ClassicMacroConfiguration(
            () -> stringMacroColumn,
            () -> stringMacroName,
            () -> stringMacroValue,
            () -> pathMacroName,
            () -> pathMacroValue
        );
        stringMacroColumn = new TreeSparseArray<String>(
            Collections.singletonMap(Integer.valueOf(0), "object__class")
        );
        stringMacroName = new TreeSparseArray<String>(
            Collections.singletonMap(Integer.valueOf(0), "state2:")
        );
        stringMacroValue = new TreeSparseArray<String>(
            Collections.singletonMap(Integer.valueOf(0), "org:openmdx:state2:")
        );
        pathMacroName = new TreeSparseArray<String>(
            Collections.singletonMap(Integer.valueOf(0), "app1.Sliced.Standard")
        );
        pathMacroValue = new TreeSparseArray<String>(
            Collections.singletonMap(Integer.valueOf(0), "xri://@openmdx*org.openmdx.test.app1/provider/Sliced/segment/Standard")
        );
    }
    
    @Test
    public void whenMatchingStringThenExternalize() {
        // Arrange
        final MacroHandler macroHandler = testee.getMacroHandler();
        // Act
        final String external = macroHandler.externalizeString("object__class", "org:openmdx:state2:Legacy");
        // Assert
        Assertions.assertEquals("state2:Legacy", external);
    }

    @Test
    public void whenMatchingStringThenInternalize() {
        // Arrange
        final MacroHandler macroHandler = testee.getMacroHandler();
        // Act
        final String internal = macroHandler.internalizeString("object__class", "state2:Legacy");
        // Assert
        Assertions.assertEquals("org:openmdx:state2:Legacy", internal);
    }

    @Test
    public void whenMatchingPathThenExternalize() {
        // Arrange
        final MacroHandler macroHandler = testee.getMacroHandler();
        final Path xri = new Path("xri://@openmdx*org.openmdx.test.app1/provider/Sliced/segment/Standard/product/4711");
        // Act
        final String external = macroHandler.externalizePath(xri);
        // Assert
        Assertions.assertEquals("xri:*app1.Sliced.Standard/product/4711", external);
    }

    @Test
    public void whenMatchingPathThenInternalize() {
        // Arrange
        final MacroHandler macroHandler = testee.getMacroHandler();
        final Path xri = new Path("xri://@openmdx*org.openmdx.test.app1/provider/Sliced/segment/Standard/product/4711");
        // Act
        final Object internal = macroHandler.internalizePath("xri:*app1.Sliced.Standard/product/4711");
        Assertions.assertTrue(internal instanceof Path);
        // Assert
        Assertions.assertEquals(xri, internal);
    }

    @Test
    public void whenNoMatchingPathThenFromURI() {
        // Arrange
        final MacroHandler macroHandler = testee.getMacroHandler();
        final Path xri = new Path("xri://@openmdx*com.example.test.app1/provider/Sliced/segment/Standard/product/4711");
        // Act
        final String external = macroHandler.externalizePath(xri);
        // Assert
        Assertions.assertEquals(xri.toURI(), external);
    }

    @Test
    public void whenNoMatchingPathThenToURI() {
        // Arrange
        final MacroHandler macroHandler = testee.getMacroHandler();
        final Path xri = new Path("xri://@openmdx*com.example.test.app1/provider/Sliced/segment/Standard/product/4711");
        final String uri = xri.toURI();
        // Act
        final Object internal = macroHandler.internalizePath(uri);
        Assertions.assertTrue(internal instanceof Path);
        // Assert
        Assertions.assertEquals(xri, internal);
    }
    
    
    @Test
    public void whenDifferentColumnThenNoChangeByExternalization() {
        // Arrange
        final MacroHandler macroHandler = testee.getMacroHandler();
        // Act
        final String external = macroHandler.externalizeString("type", "org:openmdx:state2:Legacy");
        // Assert
        Assertions.assertEquals("org:openmdx:state2:Legacy", external);
    }

    @Test
    public void whenDifferentColumnThenNoChangeByInternalization() {
        // Arrange
        final MacroHandler macroHandler = testee.getMacroHandler();
        // Act
        final String internal = macroHandler.internalizeString("object__class", "org:openmdx:state2:Legacy");
        // Assert
        Assertions.assertEquals("org:openmdx:state2:Legacy", internal);
    }

    @Test
    public void whenStringMacroNameIsMissingThenThrowException() {
        // Arrange
        stringMacroColumn.put(Integer.valueOf(1), "type");
        stringMacroValue.put(Integer.valueOf(1), "org:openmdx:kernel:");
        //
        // Act/Assert
        //
        try {
        	testee.getMacroHandler();
        	Assertions.fail("NullPointerException expected");
        } catch (NullPointerException expected) {
        	// String macro value missing
        }
    }

    @Test
    public void whenStringMacroValueIsMissingThenThrowException() {
        // Arrange
        stringMacroColumn.put(Integer.valueOf(1), "type");
        stringMacroName.put(Integer.valueOf(1), "kernel:");
        //
        // Act/Assert
        //
        try {
        	testee.getMacroHandler();
        	Assertions.fail("NullPointerException expected");
        } catch (NullPointerException expected) {
        	// String macro value missing
        }
    }

    @Test
    public void whenPathMacroValueIsMissingThenThrowException() {
    	//
        // Arrange
    	//
        pathMacroName.put(Integer.valueOf(1), "app1.Sliced.Superior");
        //
        // Act/Assert
        //
        try {
        	testee.getMacroHandler();
        	Assertions.fail("NullPointerException expected");
        } catch (NullPointerException expected) {
        	// Path macro value missing
        }
    }
    
    @Test
    public void handlerIsCached() {
        // Arrange
        final MacroHandler handler1 = testee.getMacroHandler();
        // Act
        final MacroHandler handler2 = testee.getMacroHandler();
        // Assert
        Assertions.assertSame(handler1, handler2);
    }

}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: TinyIndexedRecord Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations 
 * as listed in the NOTICE file.
 */
package org.openmdx.base.resource.spi;

import javax.resource.cci.IndexedRecord;

import org.junit.Test;

import org.junit.Assert;

/**
 * Java Connector Architecture:
 * An IndexedRecord of size 0 or 1 backed-up by an optional value
 */
public final class TinyIndexRecordTest {

    @Test
    public void whenNullValueThenEmptyList(){
        //
        // Arrange
        //
        final IndexedRecord testee = new TinyIndexedRecord("TestRecord", "TestRecord description", null);
        //
        // Act
        //
        final boolean empty = testee.isEmpty();
        //
        // Assert
        //
        Assert.assertTrue(empty);
    }
    
    @Test
    public void whenNonNullValueThenSizeEquals1(){
        //
        // Arrange
        //
        final IndexedRecord testee = new TinyIndexedRecord("TestRecord", "TestRecord description", "a value");
        //
        // Act
        //
        final int size = testee.size();
        //
        // Assert
        //
        Assert.assertEquals(1, size);
    }

    @Test
    public void whenNonNullValueThenValueIsReturned(){
        //
        // Arrange
        //
        final String initialValue = "a value";
        final IndexedRecord testee = new TinyIndexedRecord("TestRecord", "TestRecord description", initialValue);
        //
        // Act
        //
        final Object retrievedValue = testee.get(0);
        //
        // Assert
        //
        Assert.assertSame(initialValue, retrievedValue);
    }

    @Test
    public void whenMutableThenValueMayBeRemoved(){
        //
        // Arrange
        //
        final String initialValue = "a value";
        final IndexedRecord testee = new TinyIndexedRecord("TestRecord", "TestRecord description", initialValue);
        //
        // Act
        //
        testee.clear();
        //
        // Assert
        //
        Assert.assertTrue(testee.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void whenImmutableThenValueCannotBeRemoved(){
        //
        // Arrange
        //
        final String initialValue = "a value";
        final TinyIndexedRecord testee = new TinyIndexedRecord("TestRecord", "TestRecord description", initialValue);
        testee.makeImmutable();
        //
        // Act
        //
        testee.clear();
    }

}

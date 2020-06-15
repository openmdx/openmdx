/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Tiny Indexed Record Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016-2019, OMEX AG, Switzerland
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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.openmdx.base.resource.cci.SetRecord;
import javax.resource.cci.IndexedRecord;

import org.junit.Assert;
import org.junit.Test;

/**
 * Java Connector Architecture:
 * An IndexedRecord of size 0 or 1 backed-up by an optional value
 */
public final class SetRecordFacadeTest {

    private Object value = null;

    private SetRecord createTestee(
    ){
        return new SetRecordFacade(
            new Supplier<Object>() {
                
                /* (non-Javadoc)
                 * @see java.util.function.Supplier#get()
                 */
                @Override
                public Object get(
                    ) {
                    return value;
                }
                
            },
            new Consumer<Object>() {
                
                @Override
                public void accept(
                    Object t
                    ) {
                    value = t;
                }
                
            }
       );
    }
    
    @Test
    public void whenNullValueThenEmptySet(){
        //
        // Arrange
        //
        value = null;
        final IndexedRecord testee = createTestee();
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
        value = "a value";
        final IndexedRecord testee = createTestee();
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
        value = "a value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        final Object retrievedValue = testee.get(0);
        //
        // Assert
        //
        Assert.assertSame(value, retrievedValue);
    }

    @Test
    public void whenNonEmptyThenRecordMayBeCleared(){
        //
        // Arrange
        //
        value = "a value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.clear();
        //
        // Assert
        //
        Assert.assertTrue(testee.isEmpty());
    }

    @Test
    public void whenNonEmptyThenRecordValueMayBeChanged(){
        //
        // Arrange
        //
        value = "a value";
        final String newValue = "another value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.set(0, newValue);
        //
        // Assert
        //
        Assert.assertSame(newValue, testee.get(0));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void whenEmptyThenRecordValueMayBeNotBeSet(){
        //
        // Arrange
        //
        value = null;
        final String newValue = "another value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.set(0, newValue);
    }
    
    @Test(expected=IllegalStateException.class)
    public void whenNonEmptyThenNothingCanBePrepended(){
        //
        // Arrange
        //
        value = "a value";
        final String newValue = "another value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.add(0, newValue);
        //
        // Assert
        //
        Assert.assertTrue(testee.isEmpty());
    }

    @Test(expected=IllegalStateException.class)
    public void whenNonEmptyThenNothingCanBeAppended(){
        //
        // Arrange
        //
        value = "a value";
        final String newValue = "another value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.add(1, newValue);
        //
        // AssertNonEmpty
        //
        Assert.assertTrue(testee.isEmpty());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void whenIndexGreaterSizeThenNothingCanBeAppended(){
        //
        // Arrange
        //
        value = "a value";
        final String newValue = "another value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.add(2, newValue);
        //
        // Assert
        //
        Assert.assertTrue(testee.isEmpty());
    }
    
    @Test
    public void whenEmptyThenSomethingCanBePrepended(){
        //
        // Arrange
        //
        value = null;
        final String newValue = "a value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.add(0, newValue);
        //
        // Assert
        //
        Assert.assertSame(newValue, testee.iterator().next());
    }

    @Test
    public void whenEmptyThenSomethingCanBeAppended(){
        //
        // Arrange
        //
        value = null;
        final String newValue = "a value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.add(newValue);
        //
        // Assert
        //
        Assert.assertSame(newValue, testee.iterator().next());
    }

    @Test
    public void whenNonEmptyThenAppendingTheSameValueIsIdempotent(){
        //
        // Arrange
        //
        value = null;
        final String newValue = "a value";
        final IndexedRecord testee = createTestee();
        //
        // Act
        //
        testee.add(newValue);
        testee.add(newValue);
        //
        // Assert
        //
        Assert.assertSame(newValue, testee.iterator().next());
    }

}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: InternalizedKeys Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2019, OMEX AG, Switzerland
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

package org.openmdx.kernel.collection;

import org.junit.Assert;
import org.junit.Test;

/**
 * InternalizedKeysTest
 */
public class InternalizedKeysTest {

    @Test
    public void short127isInternalizedByJDK() {
        // Arrange
        short value = 127;
        Short key = new Short(value);
        // Act
        Short internalized = InternalizedKeys.internalize(key);
        // Assert
        Assert.assertNotSame(key, internalized);
        Assert.assertSame(Short.valueOf(value), internalized);
    }

    @Test
    public void shortMinus128isInternalizedByJDK() {
        // Arrange
        short value = -128;
        Short key = new Short(value);
        // Act
        Short internalized = InternalizedKeys.internalize(key);
        // Assert
        Assert.assertNotSame(key, internalized);
        Assert.assertSame(Short.valueOf(value), internalized);
    }

    @Test
    public void integer127isInternalizedByJDK() {
        // Arrange
        int value = 127;
        Integer key = new Integer(value);
        // Act
        Integer internalized = InternalizedKeys.internalize(key);
        // Assert
        Assert.assertNotSame(key, internalized);
        Assert.assertSame(Integer.valueOf(value), internalized);
    }

    @Test
    public void integerMinus128isInternalizedByJDK() {
        // Arrange
        int value = -128;
        Integer key = new Integer(value);
        // Act
        Integer internalized = InternalizedKeys.internalize(key);
        // Assert
        Assert.assertNotSame(key, internalized);
        Assert.assertSame(Integer.valueOf(value), internalized);
    }

    @Test
    public void shortMinValueIsInternalizedByInternalizedKeys() {
        // Arrange
        short value = Short.MIN_VALUE;
        Short key1 = new Short(value);
        Short key2 = new Short(value);
        // Act
        Short internalized1 = InternalizedKeys.internalize(key1);
        Short internalized2 = InternalizedKeys.internalize(key2);
        // Assert
        Assert.assertNotSame(key1, key2);
        Assert.assertSame(internalized1, internalized2);
    }
    
    @Test
    public void shortMaxValueIsInternalizedByInternalizedKeys() {
        // Arrange
        short value = Short.MAX_VALUE;
        Short key1 = new Short(value);
        Short key2 = new Short(value);
        // Act
        Short internalized1 = InternalizedKeys.internalize(key1);
        Short internalized2 = InternalizedKeys.internalize(key2);
        // Assert
        Assert.assertNotSame(key1, key2);
        Assert.assertSame(internalized1, internalized2);
    }
    
    @Test
    public void integerMinValueIsInternalizedByInternalizedKeys() {
        // Arrange
        int value = Integer.MIN_VALUE;
        Integer key1 = new Integer(value);
        Integer key2 = new Integer(value);
        // Act
        Integer internalized1 = InternalizedKeys.internalize(key1);
        Integer internalized2 = InternalizedKeys.internalize(key2);
        // Assert
        Assert.assertNotSame(key1, key2);
        Assert.assertSame(internalized1, internalized2);
    }
    
    @Test
    public void integerMaxValueIsInternalizedByInternalizedKeys() {
        // Arrange
        int value = Integer.MAX_VALUE;
        Integer key1 = new Integer(value);
        Integer key2 = new Integer(value);
        // Act
        Integer internalized1 = InternalizedKeys.internalize(key1);
        Integer internalized2 = InternalizedKeys.internalize(key2);
        // Assert
        Assert.assertNotSame(key1, key2);
        Assert.assertSame(internalized1, internalized2);
    }

    @Test
    public void longMinValueIsInternalizedByInternalizedKeys() {
        // Arrange
        long value = Long.MIN_VALUE;
        Long key1 = new Long(value);
        Long key2 = new Long(value);
        // Act
        Long internalized1 = InternalizedKeys.internalize(key1);
        Long internalized2 = InternalizedKeys.internalize(key2);
        // Assert
        Assert.assertNotSame(key1, key2);
        Assert.assertSame(internalized1, internalized2);
    }
    
    @Test
    public void longMaxValueIsInternalizedByInternalizedKeys() {
        // Arrange
        long value = Long.MAX_VALUE;
        Long key1 = new Long(value);
        Long key2 = new Long(value);
        // Act
        Long internalized1 = InternalizedKeys.internalize(key1);
        Long internalized2 = InternalizedKeys.internalize(key2);
        // Assert
        Assert.assertNotSame(key1, key2);
        Assert.assertSame(internalized1, internalized2);
    }

}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Jmi1ClassOrSubClassPredicate Test 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.function.Predicate;

import javax.resource.ResourceException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.accessor.rest.spi.ObjectRecords;
import org.openmdx.base.rest.cci.ObjectRecord;

/**
 * Jmi1ClassOrSubClassPredicate Test
 */
public class Jmi1ClassOrSubClassPredicateTest {

    @Test
    public void sameClass() throws ResourceException {
        //
        // Arrange
        //
        final String modelClass = "org:openmdx:base:Creatable";
        final Predicate<ObjectRecord> testee = Jmi1ClassPredicate.newInstance(true, modelClass);
        final ObjectRecord candidate = ObjectRecords.createObjectRecord(null, modelClass);
        //
        // Act
        //
        final boolean matches = testee.test(candidate);
        //
        // Assert
        //
        Assertions.assertTrue(matches);
    }

    @Test
    public void subClass() throws ResourceException {
        //
        // Arrange
        //
        final String modelClass = "org:openmdx:base:Creatable";
        final Predicate<ObjectRecord> testee = Jmi1ClassPredicate.newInstance(true, modelClass);
        final ObjectRecord candidate = ObjectRecords.createObjectRecord(null, "org:openmdx:base:BasicObject");
        //
        // Act
        //
        final boolean matches = testee.test(candidate);
        //
        // Assert
        //
        Assertions.assertTrue(matches);
    }

    @Test
    public void superClass() throws ResourceException {
        //
        // Arrange
        //
        final String modelClass = "org:omg:model1:Element";
        final Predicate<ObjectRecord> testee = Jmi1ClassPredicate.newInstance(true, modelClass);
        final ObjectRecord candidate = ObjectRecords.createObjectRecord(null, "org:openmdx:base:BasicObject");
        //
        // Act
        //
        final boolean matches = testee.test(candidate);
        //
        // Assert
        //
        Assertions.assertFalse(matches);
    }
    
    @Test
    public void subclassesDiffere() throws ResourceException {
        final Predicate<ObjectRecord> withoutSubclasses = Jmi1ClassPredicate.newInstance(false, "org:openmdx:base:BasicObject");
        final Predicate<ObjectRecord> withSubclasses = Jmi1ClassPredicate.newInstance(true, "org:openmdx:base:BasicObject");
        //Predicatence
        // Actomg:model1:Element
        //
        final boolean with = withoutSubclasses.equals(withSubclasses);
        final boolean without = withSubclasses.equals(withoutSubclasses);
        //
        // Assert
        //
        Assertions.assertFalse(with, "withoutSubclasses predicate equals withSublcasses predicate");
        Assertions.assertFalse(without, "withSubclasses equals withoutSubclasses predicate");
        Assertions.assertNotEquals(withoutSubclasses.hashCode(), withSubclasses.hashCode(), "hashCode");
    }

}

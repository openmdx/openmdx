/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Clock_1 Test
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package test.mock.clock1;

import java.text.ParseException;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jmi.reflect.RefException;
import #if JAVA_8 javax.resource.ResourceException #else jakarta.resource.ResourceException #endif;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;

import test.openmdx.clock1.jmi1.Segment;

/**
 * AOP 2 Test
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
public class Clock_1Test {

	/**
     * Standard Segment Test
     * @throws RefException 
     */
    @Test
    public void normal(
    ) throws ResourceException, RefException{
        // Arrange
        final PersistenceManagerFactory entityManagerFactory = EntityManagerFactories.getNormalEntityManagerFactory();
        final PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        // Act
        final Segment segment = Segments.getNormalSegment(persistenceManager);
        // Assert
        Segments.validateNormalDescription(segment);
        Segments.validateNormalTimePoint(segment);
        Segments.validateNormalTimePointReflectively(segment);
        Segments.validateNormalProvider(segment);
        Segments.validateUnchangedTimePoint(segment);
    }

    /**
     * Mocked Segment Test
     * @throws RefException 
     */
    @Test
    public void mocked(
    ) throws ResourceException, ParseException, RefException {
        // Arrange
        final PersistenceManagerFactory entityManagerFactory = EntityManagerFactories.getMockedEntityManagerFactory();
        final PersistenceManager persistenceManager = entityManagerFactory.getPersistenceManager();
        // Act
        final Segment segment = Segments.getMockedSegment(persistenceManager);
        // Assert
        Segments.validateMockedDescription(segment);
        Segments.validateMockedTimePoint(segment);
        Segments.validateMockedTimePointReflectively(segment);
        Segments.validateMockedProvider(segment);
        Segments.validateChangedTimePoint(segment);
        Segments.validateChangedTimePointReflectively(segment);
    }

}

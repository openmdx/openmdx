/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: SharedAssociations Test
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
package test.openmdx.application.mof.repository.accessor;

import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.application.mof.repository.accessor.SharedAssociations;
import org.openmdx.base.naming.Path;

/**
 * Shared Associations
 */
public class SharedAssociationsTest {

    private static final Predicate<Path> SHARED_ASSOCIATION = new Predicate<Path>() {

        @Override
        public boolean test(Path t) {
            return t.getLastSegment().toXRIRepresentation().startsWith("s");
        }
        
    };
    
    @Test
    public void whenAuthorityReferenceThenFalse() {
        // Arrange
        final SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertFalse(containsSharedAssociation);
    }
    
    @Test
    public void whenAuthorityThenFalse() {
        // Arrange
        final SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*org.openmdx.base");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertFalse(containsSharedAssociation);
    }

    @Test
    public void whenProviderReferenceThenFalse() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*org.omg.model1/provider");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertFalse(containsSharedAssociation);
    }
    
    @Test
    public void whenProviderThenFalse() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*org.omg.model1/provider/Mof");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertFalse(containsSharedAssociation);
    }
    
    @Test
    public void whenSharedUnsupportedReferenceThenTrue() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*com.example/shared");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertTrue(containsSharedAssociation);
    }

    @Test
    public void whenSharedUnsupportedThenTrue() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*com.example/shared/-");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertTrue(containsSharedAssociation);
    }
    
    @Test
    public void whenContainedUnsuportedReferenceThenFalse() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdxcom.example/contained");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertFalse(containsSharedAssociation);
    }

    @Test
    public void whenContainedUnsuportedThenFalse() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdxcom.example/contained/-");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertFalse(containsSharedAssociation);
    }

    @Test
    public void whenSegmentReferenceThenFalse() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("\"xri://@openmdx*org:openmdx:audit2/provider/Audit/segment");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertFalse(containsSharedAssociation);
    }
    
    @Test
    public void whenSegmentThenFalse() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*org:openmdx:audit2/provider/Audit/segment/Standard");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertFalse(containsSharedAssociation);
    }
    
    @Test
    public void whenStrangeReferenceThenTrue() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*org:openmdx:audit2/provider/Audit/segment/Standard/strange/UnitOfWork/involvement");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertTrue(containsSharedAssociation);
    }
    
    @Test
    public void whenStrangeThenTrue() {
        // Arrange
        SharedAssociations testee = new SharedAssociations(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*org:openmdx:audit2/provider/Audit/segment/Standard/strange/UnitOfWork/involvement/d217cfb7-6a17-4624-a2e4-24d79c2a1f57");
        // Act
        final boolean containsSharedAssociation = testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertTrue(containsSharedAssociation);
    }

    @Test
    public void whenAskingInThisOrderThenNormalizationHasAnEffect() {
        // Arrange
        Testee testee = new Testee(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*org:openmdx:audit2/provider/Audit/segment/Standard/strange/UnitOfWork/involvement");
        // Act
        testee.containsSharedAssociation(xri.getParent());
        testee.containsSharedAssociation(xri);
        // Assert
        Assertions.assertEquals(1, testee.replacements);
    }

    @Test
    public void whenAskingInThisOrderThenNormalizationHasNoEffect() {
        // Arrange
        Testee testee = new Testee(SHARED_ASSOCIATION);
        final Path xri = new Path("xri://@openmdx*org:openmdx:audit2/provider/Audit/segment/Standard/strange/UnitOfWork/involvement");
        // Act
        testee.containsSharedAssociation(xri);
        testee.containsSharedAssociation(xri.getParent());
        // Assert
        Assertions.assertEquals(0, testee.replacements);
    }
    
    private static class Testee extends SharedAssociations {

        /**
         * Constructor 
         *
         * @param sharedAssociation
         */
        Testee(Predicate<Path> sharedAssociation) {
            super(sharedAssociation);
        }

        public int replacements = 0;
        
        /* (non-Javadoc)
         * @see org.openmdx.application.mof.repository.accessor.SharedAssociations#replaceGrandparent(org.openmdx.base.naming.Path, org.openmdx.base.naming.Path)
         */
        @Override
        protected Path replaceGrandparent(
            Path grandparent,
            Path pattern
        ) {
            replacements++;
            return super.replaceGrandparent(grandparent, pattern);
        }
        
    }
    
}

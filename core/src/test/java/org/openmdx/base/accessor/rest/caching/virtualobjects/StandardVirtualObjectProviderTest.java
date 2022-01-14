/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Virtual Object Provider Test 
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

package org.openmdx.base.accessor.rest.caching.virtualobjects;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmdx.base.caching.virtualobjects.StandardVirtualObjects;
import org.openmdx.base.caching.virtualobjects.VirtualObjectProvider;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;

import net.rfc.jmi1.RfcPackage;

/**
 * Virtual Object Provider Test
 */
public class StandardVirtualObjectProviderTest {

    @Test
    public void provideTransaction(
    ) {
        //
        // Arrange
        //
        final VirtualObjectProvider testee = new StandardVirtualObjects();
        final Path xri = new Path("xri://@openmdx*org.openmdx.kernel/transaction/LW0QS1385SLPM8SZSQC0GP8MG");
        //
        // Act
        //
        final boolean provided = testee.provides(xri);
        //
        // Assert
        //
        Assertions.assertFalse(provided);
    }
    
    @Test
    public void provideKernel(
    ) {
        //
        // Arrange
        //
        final VirtualObjectProvider testee = new StandardVirtualObjects();
        final Path xri = new Path("xri://@openmdx*org.openmdx.kernel");
        //
        // Act
        //
        final boolean provided = testee.provides(xri);
        //
        // Assert
        //
        Assertions.assertTrue(provided);
    }
    
    @Test
    public void loadAuthority(
    ) {
        //
        // Arrange
        //
        final VirtualObjectProvider testee = new StandardVirtualObjects();
        final Path xri = new Path(RfcPackage.AUTHORITY_XRI);
        
        //
        // Act
        //
        final ObjectRecord object = testee.load(xri);
        //
        // Assert
        //
        Assertions.assertEquals("org:openmdx:base:Authority", object.getValue().getRecordName());
        Assertions.assertEquals(xri, object.getResourceIdentifier());
    }

    @Test
    public void doNotLoadContainer(
    ) {
        //
        // Arrange
        //
        final VirtualObjectProvider testee = new StandardVirtualObjects();
        final Path xri = new Path(RfcPackage.AUTHORITY_XRI + "/provider");
        
        //
        // Act
        //
        final ObjectRecord object = testee.load(xri);
        //
        // Assert
        //
        Assertions.assertNull(object);
    }
    
    @Test
    public void loadProvider(
    ) {
        //
        // Arrange
        //
        final VirtualObjectProvider testee = new StandardVirtualObjects();
        final Path xri = new Path(RfcPackage.AUTHORITY_XRI + "/provider/Test");
        
        //
        // Act
        //
        final ObjectRecord object = testee.load(xri);
        //
        // Assert
        //
        Assertions.assertEquals("org:openmdx:base:Provider", object.getValue().getRecordName());
        Assertions.assertEquals(xri, object.getResourceIdentifier());
    }

    @Test
    public void doNotLoadSegment(
    ) {
        //
        // Arrange
        //
        final VirtualObjectProvider testee = new StandardVirtualObjects();
        final Path xri = new Path(RfcPackage.AUTHORITY_XRI + "/provider/Test/segment/Standard");
        //
        // Act
        //
        final ObjectRecord object = testee.load(xri);
        //
        // Assert
        //
        Assertions.assertNull(object);
    }

    @Test
    public void loadSome() {
        //
        // Arrange
        //
        final VirtualObjectProvider testee = new StandardVirtualObjects();
        final Path authorityXRI = new Path(RfcPackage.AUTHORITY_XRI);
        final Path providerXRI = new Path(RfcPackage.AUTHORITY_XRI + "/provider/Test");
        final Collection<Path> xris = Arrays.asList(
            authorityXRI,
            new Path(RfcPackage.AUTHORITY_XRI + "/provider"),
            providerXRI
        );
        //
        // Act
        //
        final Map<Path, ObjectRecord> objects = testee.loadAll(xris);
        //
        // Assert
        //
        Assertions.assertEquals(2, objects.size());
        Assertions.assertEquals("org:openmdx:base:Authority", objects.get(authorityXRI).getValue().getRecordName());
        Assertions.assertEquals("org:openmdx:base:Provider", objects.get(providerXRI).getValue().getRecordName());
    }

}

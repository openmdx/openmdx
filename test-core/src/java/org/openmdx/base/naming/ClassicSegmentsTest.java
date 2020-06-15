/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: ClassicSegments Test 
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
package org.openmdx.base.naming;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openmdx.base.exception.ServiceException;

/**
 * ClassicSegments Test
 */
public class ClassicSegmentsTest {
    
    @Test
    public void determineCoreComponentFromAspectQualifierPlaceholder(
    ) throws ServiceException {
        //
        // Arrange
        //
        final String coreComponent = "meineNummer";
        final String placeholderComponent = "3c329bdf-7fe4-4bfd-bd54-ea3c5a6336c6";
        final String placehholder = ":" + coreComponent + ":" + placeholderComponent;
        Assert.assertTrue(ClassicSegments.isPlaceholder(placehholder));
        //
        // Act
        //
        final Optional<String> optionalCoreQualifier = ClassicSegments.getCoreComponentFromAspectQualifierPlaceholder(placehholder);
        //
        // Assert
        //
        Assert.assertTrue(optionalCoreQualifier.isPresent());
        Assert.assertEquals(coreComponent, optionalCoreQualifier.get());
    }

    @Test
    public void createPrivateSegment(
    ) throws ServiceException {
        //
        // Arrange
        //
        final String coreComponent = "meineNummer";
        final Integer aspectComponent = Integer.valueOf(0);
        //
        // Act
        //
        final String qualifier = ClassicSegments.createPrivateSegment(coreComponent, aspectComponent);
        //
        // Assert
        //
        Assert.assertEquals("meineNummer:0:", qualifier);
    }
    
    @Test
    public void determineCoreComponentFromAspectQualifier(
    ) throws ServiceException {
        //
        // Arrange
        //
        final String coreComponent = "meineNummer";
        final String qualifier = coreComponent + ":0:";
        //
        // Act
        //
        final Optional<String> optionalCoreQualifier = ClassicSegments.getCoreComponentFromAspectQualifier(qualifier);
        //
        // Assert
        //
        Assert.assertTrue(optionalCoreQualifier.isPresent());
        Assert.assertEquals(coreComponent, optionalCoreQualifier.get());
    }    
    
}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: As Admin 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020-2021, OMEX AG, Switzerland
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

package test.mock.clock1.aop2;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.junit.jupiter.api.Assertions;
import org.openmdx.base.persistence.cci.UserObjects;

import test.openmdx.clock1.jmi1.Segment;

/**
 * As Admin
 */
public class AsAdmin {

    private static final String ADMIN_PRINCIPAL = "admin";
    private static final String ID_SEPARATOR = "-";
    
    static void notifyDateAndTimeChange(
        Segment segmentAsUser,
        Date to
    ) {
        asAdministrator(segmentAsUser).setDescription(
            "Date set to " + to
        );
    }
    
    private static Segment asAdministrator(
        Segment object
    ){
        final PersistenceManager userPersistenceManager = JDOHelper.getPersistenceManager(object);
        validateConfiguration(userPersistenceManager);
        final String segmentName = object.refGetPath().getSegment(4).toClassicRepresentation();
        final List<String> userPrincipalChain = UserObjects.getPrincipalChain(userPersistenceManager);
        final List<String> adminPrincipalChain = Arrays.asList(
            ADMIN_PRINCIPAL + ID_SEPARATOR + segmentName,
            userPrincipalChain.get(0)
        );
        final PersistenceManager adminPersistenceManager = userPersistenceManager.getPersistenceManagerFactory().getPersistenceManager(
            adminPrincipalChain.toString(),
            null
        );
        validatePrincipalChain(adminPersistenceManager);
        validateConfiguration(adminPersistenceManager);
        return adminPersistenceManager.getObjectById(Segment.class, object.refMofId());
    }

    private static void validateConfiguration(
        final PersistenceManager persistenceManager
    ) {
        final MockPlugInConfiguration configuration = (MockPlugInConfiguration)persistenceManager.getUserObject("configuration");
        Assertions.assertNotNull(configuration);
        Assertions.assertEquals("Hello World", configuration.getGreetings());
    }

    private static void validatePrincipalChain(
        PersistenceManager adminPersistenceManager
    ) {
        final List<String> expectedPrincipalChain = Arrays.asList(
            "admin-Mocked",
            System.getProperty("user.name")
        );
        final List<String> actualPrincipalChain = UserObjects.getPrincipalChain(adminPersistenceManager);
        Assertions.assertEquals(expectedPrincipalChain, actualPrincipalChain, "Principal Chain");
    }

}

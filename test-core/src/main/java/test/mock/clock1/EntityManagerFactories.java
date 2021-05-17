/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Entity Manager Factories
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2020, OMEX AG, Switzerland
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

package test.mock.clock1;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * Entity Manager Factories
 */
public class EntityManagerFactories {

    static PersistenceManagerFactory getNormalEntityManagerFactory(
    ) {
        return getEntityManagerFactory(null);
    }

    static PersistenceManagerFactory getMockedEntityManagerFactory(
    ) {
        Map<String,String> overrides = new HashMap<String, String>();
        overrides.put("org.openmdx.jdo.EntityManager.plugIn[0]","mockPlugIn");
        overrides.put("mockPlugIn.modelPackage[0]","test:openmdx:clock1");
        overrides.put("mockPlugIn.packageImpl[0]","test.mock.clock1.aop2");
        PersistenceManagerFactory entityManagerFactory = getEntityManagerFactory(overrides);
        return entityManagerFactory;
    }

    private static PersistenceManagerFactory getEntityManagerFactory(
        Map<?, ?> overrides
    ) {
        return JDOHelper.getPersistenceManagerFactory(overrides, "test-Clock-EntityManagerFactory");
    }
    
}

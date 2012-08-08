/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestAspects.scala,v 1.1 2010/11/26 14:05:38 wfro Exp $
 * Description: Test Aspects 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/11/26 14:05:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package test_scala.openmdx.base;

import javax.jdo._
import javax.naming._

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.resource.ResourceException;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;

import test.openmdx.base.cci2.AnAspectQuery;
import test.openmdx.base.jmi1.AnAspect;
import test.openmdx.base.jmi1.BasePackage;
import test.openmdx.base.jmi1.Core;
import test.openmdx.base.jmi1.Segment;

import org.junit._
import Assert._

/**
 * TestAspects
 */
class TestAspects {

    /**
     * Persistence Manager used by each test
     */
    var persistenceManager: PersistenceManager = null;

    val PROVIDER: String = "Test";
    
    val SEGMENT: String = "Aspect";

    val principalName: String = "me";
    
    @Before
    def acquirePersistenceManager() = {
        this.persistenceManager = if (principalName == null) TestAspects.accessorFactory.getPersistenceManager(
        ) else TestAspects.accessorFactory.getPersistenceManager(
            principalName,
            null
        );
    }

    @After
    def closePersistenceManager() = {
        if(this.persistenceManager != null) {
            this.persistenceManager.close();
            this.persistenceManager = null;
        }
    }

    def replacePersistenceManager(
    ) = {
        try {
            closePersistenceManager();
            acquirePersistenceManager();
        } catch {
        	case exception: ResourceException => throw new AssertionError(exception);
        }
    }

    def newSegment(): Segment = {
        val unitOfWork:  Transaction = this.persistenceManager.currentTransaction();
        val base: Authority = this.persistenceManager.getObjectById(
            classOf[org.openmdx.base.jmi1.Authority], 
            BasePackage.AUTHORITY_XRI
        )
        val provider: Provider = base.getProvider(
            false, // qualifiedNameIsPersistent 
            PROVIDER
        );
        {
            val segment: Segment = provider.getSegment(
                false, // qualifiedNameIsPersistent 
                SEGMENT
            ).asInstanceOf[Segment]
            if(segment != null) {
                unitOfWork.begin();
                System.out.println("Delete " + segment.refMofId());  
                segment.refDelete();
                unitOfWork.commit();
            }
        }
        {
            unitOfWork.begin();
            val segment: Segment = this.persistenceManager.newInstance(
                classOf[Segment]
            ).asInstanceOf[Segment]
            provider.addSegment(
                false, // qualifiedNameIsPersistent 
                SEGMENT,
                segment
            );
            unitOfWork.commit();
            System.out.println(segment.refMofId() + " created");
        }
        return getSegment();
    }

    def getSegment(): Segment = {
        val base: Authority = this.persistenceManager.getObjectById(
            classOf[Authority], 
            BasePackage.AUTHORITY_XRI
        );
        val provider: Provider = base.getProvider(
            false, // qualifiedNameIsPersistent 
            PROVIDER
        );
        val segment: Segment = provider.getSegment(
            false, // qualifiedNameIsPersistent 
            SEGMENT
        ).asInstanceOf[Segment]
        assertNotNull("segment", segment);
        return segment;
    }
    
    @Test  
    def genericAspect() = {
        createAspect("generic", "*anAspect");
        replacePersistenceManager();
        retrieveAspect("generic", "*anAspect");
    }

    @Test  
    def state1Aspect() = {
        createAspect("state1",":1:");
        replacePersistenceManager();
        retrieveAspect("state1",":1:");
    }
    
    def createAspect(
        coreQualifier: String,
        aspectSuffix: String
    ) = {  
        val segment: Segment = newSegment();
        val unitOfWork: Transaction = this.persistenceManager.currentTransaction();
        {
            unitOfWork.begin();
            val core: Core  = this.persistenceManager.newInstance(classOf[Core]);
            segment.addObject(false, coreQualifier, core);
            core.setString("one");
            val aspect: AnAspect = this.persistenceManager.newInstance(classOf[AnAspect]);
            aspect.setCore(core);
            assertEquals("aspect.string", "one", aspect.getString());
            aspect.setString("eins");
            assertEquals("core.string", "eins", core.getString());
            aspect.setPrime(3,5,7);
            aspect.setUri(TestAspects.OPENMDX_URI);
            assertEquals("aspect.integer", Arrays.asList(3,5,7), aspect.getPrime());
            segment.addObject(false, coreQualifier + aspectSuffix, aspect);
            assertEquals("aspect.string", "eins", aspect.getString());
            assertEquals("aspect.uri", TestAspects.OPENMDX_URI, aspect.getUri());
            unitOfWork.commit();
            assertEquals("aspect.string", "eins", aspect.getString());
            assertEquals("aspect.uri", TestAspects.OPENMDX_URI, aspect.getUri());
            assertEquals("aspect.integer", Arrays.asList(3,5,7), aspect.getPrime());
            assertNotNull("core.identity", core.getIdentity()); 
            assertEquals("aspect.identity", core.getIdentity(), aspect.getIdentity()); 
            System.out.println(core.refMofId() + " created with identity " + core.getIdentity());
            System.out.println(aspect.refMofId() + " created with identity " + aspect.getIdentity());
        }
    }
    
    def retrieveAspect(
        coreQualifier: String,
        aspectSuffix: String 
    ) = {
        val segment: Segment = getSegment();
        val basePackage: BasePackage = segment.refImmediatePackage().asInstanceOf[BasePackage]
        val core: Core = segment.getObject(false, coreQualifier);
        val aspectQuery: AnAspectQuery = basePackage.createAnAspectQuery();
        aspectQuery.core().equalTo(core);
        val aspects: java.util.List[AnAspect] = segment.getObject(aspectQuery);
        System.out.println("Created by " + core.getCreatedBy() + " at " + core.getCreatedAt());
        if(principalName == null) {
            assertTrue("core.createdBy", core.getCreatedBy().isEmpty());
        } else {
            assertEquals("core.createdBy", Collections.singleton(principalName), core.getCreatedBy());
        }
        assertEquals("aspects.size", aspects.size(), 1);
        assertEquals("aspect.string", "eins", aspects.get(0).getString());
        assertEquals("aspect.createdBy", core.getCreatedBy(), aspects.get(0).getCreatedBy());
        assertEquals("aspect.integer", Arrays.asList(3,5,7), aspects.get(0).getPrime());
        assertEquals("aspect.uri", TestAspects.OPENMDX_URI, aspects.get(0).getUri());
    }
    
}

object TestAspects {

    var OPENMDX_URI: URI = new URI("http://www.openmdx.org");

    /**
     * Deployment Configuration
     */
    val accessorFactory: PersistenceManagerFactory = null; 
        
    @AfterClass
    def closePersistenceManagerFactory() = {
        accessorFactory.close();
    }
        
}

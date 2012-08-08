/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestAspects.java,v 1.1 2008/09/17 16:04:32 hburger Exp $
 * Description: Test Aspects 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/17 16:04:32 $
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
package test.openmdx.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import javax.resource.ResourceException;
import javax.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.compatibility.base.application.cci.Deployment_1;

import test.openmdx.base.cci2.AnAspectQuery;
import test.openmdx.base.jmi1.AnAspect;
import test.openmdx.base.jmi1.BasePackage;
import test.openmdx.base.jmi1.Core;
import test.openmdx.base.jmi1.Segment;

/**
 * TestAspects
 */
public class TestAspects {

    /**
     * Deployment Configuration
     */
    protected static final Deployment_1 accessorFactory = new Deployment_1(
        true, // IN_PROCESS_LIGHTWEIGHT_CONTAINER
        "file:../test-core/src/connector/openmdx-2/oracle-10g.rar", // CONNECTOR_URL
        "file:../test-core/src/ear/test-aspect.ear", // APPLICATION_URL
        false, // LOG_DEPLOYMENT_DETAILS
        "test/openmdx/aspect/EntityManagerFactory", // ENTITY_MANAGER_FACTORY_JNDI_NAME
        "test/openmdx/aspect/Gateway", // GATEWAY_JNDI_NAME
        "test:openmdx:base" // MODEL
    );
    
    /**
     * Persistence Manager used by each test
     */
    protected PersistenceManager persistenceManager;

    protected final static String PROVIDER = "Test";
    
    protected final static String SEGMENT = "Aspect";

    protected final static String principalName = "me";
    
    private static URI OPENMDX_URI;
    
    @BeforeClass
    public static void initialize() throws URISyntaxException{
        OPENMDX_URI = new URI("http://www.openmdx.org");
    }
    
    @Before
    public void acquirePersistenceManager() throws ResourceException{
        this.persistenceManager = principalName == null ? accessorFactory.createManager(
        ) : accessorFactory.createManager(
            new Subject(
                true,
                Collections.EMPTY_SET,
                Collections.EMPTY_SET,
                Collections.singleton(
                    new PasswordCredential(principalName, new char[0])
                )
            )
        );
    }

    @After
    public void closePersistenceManager() throws ResourceException{
        if(this.persistenceManager != null) {
            this.persistenceManager.close();
            this.persistenceManager = null;
        }
    }

    protected void replacePersistenceManager(
    ) {
        try {
            closePersistenceManager();
            acquirePersistenceManager();
        } catch (ResourceException exception) {
            throw new AssertionError(exception);
        }
    }

    @AfterClass
    public static void closePersistenceManagerFactory() throws IOException{
        accessorFactory.close();
    }
    
    protected Segment newSegment(
    ){
        Transaction unitOfWork = this.persistenceManager.currentTransaction();
        Authority base = (Authority) this.persistenceManager.getObjectById(
            Authority.class, 
            BasePackage.AUTHORITY_XRI
        );
        Provider provider = base.getProvider(
            false, // qualifiedNameIsPersistent 
            PROVIDER
        );
        {
            Segment segment = (Segment) provider.getSegment(
                false, // qualifiedNameIsPersistent 
                SEGMENT
            );
            if(segment != null) {
                unitOfWork.begin();
                System.out.println("Delete " + segment.refMofId());  
                segment.refDelete();
                unitOfWork.commit();
            }
        }
        {
            unitOfWork.begin();
            Segment segment = (Segment) this.persistenceManager.newInstance(
                Segment.class
            );
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

    protected Segment getSegment(
    ){
        Authority base = (Authority) this.persistenceManager.getObjectById(
            Authority.class, 
            BasePackage.AUTHORITY_XRI
        );
        Provider provider = base.getProvider(
            false, // qualifiedNameIsPersistent 
            PROVIDER
        );
        Segment segment = (Segment) provider.getSegment(
            false, // qualifiedNameIsPersistent 
            SEGMENT
        );
        assertNotNull("segment", segment);
        return segment;
    }
    
    @Test  
    public void genericAspect(
    ){
        createAspect("generic", "*anAspect");
        replacePersistenceManager();
        retrieveAspect("generic", "*anAspect");
    }

    @Test  
    public void state1Aspect(
    ){
        createAspect("state1",":1:");
        replacePersistenceManager();
        retrieveAspect("state1",":1:");
    }
    
    protected void createAspect(
        String coreQualifier,
        String aspectSuffix
    ) {  
        Segment segment = newSegment();
        Transaction unitOfWork = this.persistenceManager.currentTransaction();
        {
            unitOfWork.begin();
            Core core = (Core) this.persistenceManager.newInstance(Core.class);
            segment.addObject(false, coreQualifier, core);
            core.setString("one");
            AnAspect aspect = (AnAspect) this.persistenceManager.newInstance(AnAspect.class);
            aspect.setCore(core);
            assertEquals("aspect.string", "one", aspect.getString());
            aspect.setString("eins");
            assertEquals("core.string", "eins", core.getString());
            aspect.setPrime(3,5,7);
            aspect.setUri(OPENMDX_URI);
            assertEquals("aspect.integer", Arrays.asList(3,5,7), aspect.getPrime());
            segment.addObject(false, coreQualifier + aspectSuffix, aspect);
            assertEquals("aspect.string", "eins", aspect.getString());
            assertEquals("aspect.uri", OPENMDX_URI, aspect.getUri());
            unitOfWork.commit();
            assertEquals("aspect.string", "eins", aspect.getString());
            assertEquals("aspect.uri", OPENMDX_URI, aspect.getUri());
            assertEquals("aspect.integer", Arrays.asList(3,5,7), aspect.getPrime());
            assertNotNull("core.identity", core.getIdentity()); 
            assertEquals("aspect.identity", core.getIdentity(), aspect.getIdentity()); 
            System.out.println(core.refMofId() + " created with identity " + core.getIdentity());
            System.out.println(aspect.refMofId() + " created with identity " + aspect.getIdentity());
        }
    }
    
    protected void retrieveAspect(
        String coreQualifier,
        String aspectSuffix
    ){
        Segment segment = getSegment();
        BasePackage basePackage = (BasePackage) segment.refImmediatePackage();
        Core core = segment.getObject(false, coreQualifier);
        AnAspectQuery aspectQuery = basePackage.createAnAspectQuery();
        aspectQuery.core().equalTo(core);
        List<AnAspect> aspects = segment.getObject(aspectQuery);
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
        assertEquals("aspect.uri", OPENMDX_URI, aspects.get(0).getUri());
    }

    /**
     * For backward compatibility
     *  
     * @return a JUnit 3 Test Suite
     */
    public static junit.framework.Test suite() {  
        return new JUnit4TestAdapter(TestAspects.class);  
    }
    
}

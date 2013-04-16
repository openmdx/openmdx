/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Class Loading Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package test.openmdx.preferences2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import javax.resource.ResourceException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.openmdx.preferences2.jmi1.Entry;
import org.openmdx.preferences2.jmi1.Node;
import org.openmdx.preferences2.jmi1.Preferences2Package;
import org.openmdx.preferences2.jmi1.Segment;
import org.openmdx.preferences2.prefs.AutocommittingPreferencesFactory;
import org.openmdx.preferences2.prefs.Retrievable;

import test.openmdx.application.dataprovider.layer.persistence.jdbc.RidOidQueryDatabase_1;

/**
 * Class Loading Test
 */
public class TestPreferences {

    private static final String JMI_SEGMENT_NAME = "JMI";
    private static final String JDK_SEGMENT_NAME = "JDK";
    protected static final String ENTITY_MANAGER_FACTORY_NAME = "test-Preferences-EntityManagerFactory";
    protected static final String PROVIDER_NAME = "Data";
    
    /**
     * The Test 
     */
    private PersistenceManagerFactory entityManagerFactory;
    
    /**
     * Set-up
     */
    @Before
    public void setUp(
    ){
        this.entityManagerFactory = JDOHelper.getPersistenceManagerFactory(ENTITY_MANAGER_FACTORY_NAME);
    }
    
    /**
     * Retrieve a segment
     */
    protected Segment resetSegment(
        String name
    ) throws ResourceException {
        PersistenceManager entity = this.entityManagerFactory.getPersistenceManager();
        Authority authority = entity.getObjectById(Authority.class, Preferences2Package.AUTHORITY_XRI);
        assertNotNull("Authority '" + Preferences2Package.AUTHORITY_XRI + "'", authority);
        Provider provider = authority.getProvider(PROVIDER_NAME);
        assertNotNull("Provider '" + PROVIDER_NAME + "'", provider);
        Segment segment = (Segment) provider.getSegment(name);
        if(segment != null) {
            entity.currentTransaction().begin();
            segment.refDelete();
            entity.currentTransaction().commit();            
        }
        entity.currentTransaction().begin();
        segment = entity.newInstance(Segment.class);
        segment.setDescription(name + " Segment");
        provider.addSegment(name, segment);
        entity.currentTransaction().commit();            
        return segment;
    }

    /**
     * Retrieve a segment
     */
    protected Segment retrieveSegment(
        String name
    ) throws ResourceException {
        PersistenceManager entity = this.entityManagerFactory.getPersistenceManager();
        Authority authority = entity.getObjectById(Authority.class, Preferences2Package.AUTHORITY_XRI);
        assertNotNull("Authority '" + Preferences2Package.AUTHORITY_XRI + "'", authority);
        Provider provider = authority.getProvider(PROVIDER_NAME);
        assertNotNull("Provider '" + PROVIDER_NAME + "'", provider);
        return (Segment) provider.getSegment(name);
    }
    
    /**
     * Create Preferences through the JMI API
     * 
     * @exception ResourceException
     */
    @Test
    public void jmiCreatePreferences(
    ) throws ResourceException{
        Segment segment = resetSegment(JMI_SEGMENT_NAME);
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(segment);
        persistenceManager.currentTransaction().begin();
        org.openmdx.preferences2.jmi1.Preferences preferences = persistenceManager.newInstance(
            org.openmdx.preferences2.jmi1.Preferences.class
        );
        try {
            preferences.setType("any");
            fail("Only 'system' and 'user' are legal types");
        } catch (IllegalArgumentException expected) {
            // Only "system" and "user" are legal types
        }
        preferences.setType("system");
        segment.addPreferences("System", preferences);
        Node root = persistenceManager.newInstance(Node.class);
        root.setName("");
        assertEquals("Root node", "/", root.getAbsolutePath());
        preferences.addNode(root);
        Node first = persistenceManager.newInstance(Node.class);
        first.setParent(root);
        try {
            first.setName("1/2");
            fail("'/' are not allowed in names");
        } catch (IllegalArgumentException expected) {
            // '/' are not allowed in names
        }
        first.setName("1st");
        preferences.addNode(first);
        assertEquals("First node", "/1st", first.getAbsolutePath());
        Entry entry1 = persistenceManager.newInstance(Entry.class);
        entry1.setValue("Zum ersten");
        first.addEntry("I", entry1);
        assertEquals("I", entry1.getName());
        Node second = persistenceManager.newInstance(Node.class);
        second.setParent(first);
        second.setName("2nd");
        assertEquals("Second node", "/1st/2nd", second.getAbsolutePath());
        Entry entry2 = persistenceManager.newInstance(Entry.class);
        entry2.setValue("Zum zweiten");
        second.addEntry("II", entry2);
        assertEquals("II", entry2.getName());
        preferences.addNode(second);
        persistenceManager.currentTransaction().commit();
        int children = 0;
        for(Node child : first.<Node>getChild(first.getRoot())) {
            assertEquals("Child", "/1st/2nd", child.getAbsolutePath());
            children++;
        }
        assertEquals("Number of children", 1, children);
    }

    /**
     * Read Preferences through the JDK API
     * 
     * @throws BackingStoreException 
     */
    @Test
    public void jdkReadPreferences(
    ) throws BackingStoreException{
        PreferencesFactory testee = new EmbeddedPreferencesFactory(JMI_SEGMENT_NAME);
        Preferences systemRoot = testee.systemRoot();
        Preferences first = systemRoot.node("1st");
        assertEquals("First node", "/1st", first.absolutePath());
        assertArrayEquals("The first node's keys", new String[]{"I"}, first.keys());
        String entry1 = first.get("I", "ZUM ERSTEN");
        assertEquals("The first node's entry", "Zum ersten", entry1);
        Preferences second = first.node("2nd");
        assertEquals("Second node", "/1st/2nd", second.absolutePath());
        String entry2 = second.get("II", "ZUM ZWEITEN");
        assertEquals("The second node's entry", "Zum zweiten", entry2);
        int children = 0;
        for(String name : first.childrenNames()) {
            Preferences child = first.node(name);
            assertEquals("Child", "/1st/2nd", child.absolutePath());
            children++;
        }
        assertEquals("Number of children", 1, children);
    }

    /**
     * Create Preferences through the JDK API
     * 
     * @throws BackingStoreException 
     * @throws ResourceException 
     */
    @Test
    public void jdkCreatePreferences(
    ) throws ResourceException, BackingStoreException {
        resetSegment(JDK_SEGMENT_NAME);
        PreferencesFactory testee = new EmbeddedPreferencesFactory(JDK_SEGMENT_NAME);
        {
            Preferences systemRoot = testee.systemRoot();
            Preferences systemNode = systemRoot.node("test.openmdx.preferences2");
            systemNode.put("kind", "unkind");
            systemNode.remove("kind");
            systemNode.put("kind", "system");
        }
        {
            Preferences userRoot = testee.userRoot();
            assertEquals("The userRoot's absolute path", "/", userRoot.absolutePath());
            Preferences packageNode = userRoot.node("test/openmdx/preferences2");
            assertEquals("The packageNode's absolute path", "/test/openmdx/preferences2", packageNode.absolutePath());
            Preferences dummyNode = userRoot.node("/test/openmdx/dummy0");
            assertEquals("The dummyNode's absolute path", "/test/openmdx/dummy0", dummyNode.absolutePath());
            dummyNode.put("dummy","value");
            Preferences testNode = userRoot.node("test");
            assertEquals("The testNode's absolute path", "/test", testNode.absolutePath());
            Preferences parentNode = testNode.node("openmdx");
            assertEquals("/test/openmdx", parentNode.absolutePath());
            assertEquals(
                "The parent node's transient children",
                Sets.asSet(Arrays.asList("preferences2", "dummy0")),
                Sets.asSet(Arrays.asList(parentNode.childrenNames()))
            );
            packageNode.put("kind", "wrong");
            packageNode.putBoolean("flag", false);
            assertEquals("The package node's keys", Sets.asSet(new String[]{"kind","flag"}), Sets.asSet(packageNode.keys()));
            packageNode.remove("kind");
            dummyNode.removeNode();
            assertEquals(
                "The parent node's persistent children",
                Sets.asSet(Arrays.asList("preferences2")),
                Sets.asSet(Arrays.asList(parentNode.childrenNames()))
            );
        }
    }

    private void jmiReadPreferences(Segment segment, String kind) {
        org.openmdx.preferences2.jmi1.Preferences preferences = segment.getPreferences(kind);
        PersistenceHelper.retrieveAllDescendants(preferences);
    }
    
    /**
     * Read Preferences through the JMI API
     * 
     * @throws BackingStoreException 
     * @throws ResourceException 
     */
    @Test
    public void jmiReadPreferences(
    ) throws BackingStoreException, ResourceException{
        Segment segment = retrieveSegment(JDK_SEGMENT_NAME);
        jmiReadPreferences(segment, "System");
        jmiReadPreferences(segment, System.getProperty("user.name"));
    }
    
    /**
     * Reads and writes the preferences through the JDK API
     * 
     * @param node the preferences to be touched
     * @param touch 
     * 
     * @throws BackingStoreException 
     * @throws ResourceException 
     */
    private void jdkTraversePreferences(
        Preferences node, 
        boolean touch
    ) throws ResourceException, BackingStoreException {
        for(String key : node.keys()) {
            String value = node.get(key, null);
            if(touch) {
                node.put(key, value);
            } else {
                System.out.println(node.name() + "." + key + "=" + value);
            }
        }
        for(String childName : node.childrenNames()) {
            jdkTraversePreferences(node.node(childName), touch);
        }
    }
    
    /**
     * @param root
     * @param touch 
     * @throws ResourceException
     * @throws BackingStoreException
     */
    private void jdkLoadAndTraversePreferences(
        Preferences root, 
        boolean touch
    ) throws ResourceException, BackingStoreException {
        ((Retrievable)root).retrieveAll();
        int expectedCount = touch ? RidOidQueryDatabase_1.getUpdateCount() : RidOidQueryDatabase_1.getQueryCount(); 
        jdkTraversePreferences(root, touch);
        int actualCount = touch ? RidOidQueryDatabase_1.getUpdateCount() : RidOidQueryDatabase_1.getQueryCount(); 
        assertEquals("No DB access necessary", expectedCount, actualCount);
    }

    /**
     * Reads and writes the preferences through the JDK API
     * 
     * @throws BackingStoreException 
     * @throws ResourceException 
     */
    @Test
    public void jdkTouchPreferences(
    ) throws ResourceException, BackingStoreException {
        PreferencesFactory testee = new EmbeddedPreferencesFactory(JDK_SEGMENT_NAME);
        jdkLoadAndTraversePreferences(testee.systemRoot(), true);
        jdkLoadAndTraversePreferences(testee.userRoot(), true);
    }

    /**
     * Reads and writes the preferences through the JDK API
     * 
     * @throws BackingStoreException 
     * @throws ResourceException 
     */
    @Test
    public void jdkDumpPreferences(
    ) throws ResourceException, BackingStoreException {
        PreferencesFactory testee = new EmbeddedPreferencesFactory(JDK_SEGMENT_NAME);
        jdkLoadAndTraversePreferences(testee.systemRoot(), false);
        jdkLoadAndTraversePreferences(testee.userRoot(), false);
    }
    
    @BeforeClass
    public static void deploy() throws NamingException{
        if(!NamingManager.hasInitialContextFactoryBuilder()) {
            NonManagedInitialContextFactoryBuilder.install(null);
        }
    }

    /**
     * 
     * Test Preferences Factory
     */
    class EmbeddedPreferencesFactory extends AutocommittingPreferencesFactory {

        /**
         * Constructor 
         *
         * @param persistenceManager
         * @param providerXRI
         * @param segmentName
         * @param userRootName
         * @param systemRootName
         */
        protected EmbeddedPreferencesFactory(
            String segmentName
        ) {
            super(
                entityManagerFactory.getPersistenceManager(),
                "xri://@openmdx*org.openmdx.preferences2/provider/" + PROVIDER_NAME,
                segmentName,
                "System",
                System.getProperty("user.name")
            );
        }
    
    }
    
}

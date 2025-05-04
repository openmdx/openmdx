/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Preferences Test
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
package test.openmdx.preferences2;

import java.util.Arrays;
import java.util.Collections;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openmdx.base.collection.Sets;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.openmdx.preferences2.jmi1.Entry;
import org.openmdx.preferences2.jmi1.Node;
import org.openmdx.preferences2.jmi1.Preferences2Package;
import org.openmdx.preferences2.jmi1.Segment;
import org.openmdx.preferences2.prefs.AutocommittingPreferencesFactory;
import org.openmdx.preferences2.prefs.Retrievable;

import test.openmdx.application.dataprovider.layer.persistence.jdbc.RidOidQueryDatabase_2;

/**
 * PreferencesTest
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
public class PreferencesTest {

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
    @BeforeEach
    public void setUp(
    ){
        this.entityManagerFactory = JDOHelper.getPersistenceManagerFactory(ENTITY_MANAGER_FACTORY_NAME);
    }
    
    /**
     * Retrieve a segment
     */
    protected Segment resetSegment(
        String name
    ) {
        PersistenceManager entity = this.entityManagerFactory.getPersistenceManager();
        Authority authority = entity.getObjectById(Authority.class, Preferences2Package.AUTHORITY_XRI);
        Assertions.assertNotNull(authority, "Authority '" + Preferences2Package.AUTHORITY_XRI + "'");
        Provider provider = authority.getProvider(PROVIDER_NAME);
        Assertions.assertNotNull(provider, "Provider '" + PROVIDER_NAME + "'");
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
    )  {
        PersistenceManager entity = this.entityManagerFactory.getPersistenceManager();
        Authority authority = entity.getObjectById(Authority.class, Preferences2Package.AUTHORITY_XRI);
        Assertions.assertNotNull(authority, "Authority '" + Preferences2Package.AUTHORITY_XRI + "'");
        Provider provider = authority.getProvider(PROVIDER_NAME);
        Assertions.assertNotNull(provider, "Provider '" + PROVIDER_NAME + "'");
        return (Segment) provider.getSegment(name);
    }
    
    /**
     * Create Preferences through the JMI API
     */
//    @Disabled
    @Test
    public void jmiCreatePreferences(
    ) {
        Segment segment = resetSegment(JMI_SEGMENT_NAME);
        PersistenceManager persistenceManager = JDOHelper.getPersistenceManager(segment);
        persistenceManager.currentTransaction().begin();
        org.openmdx.preferences2.jmi1.Preferences preferences = persistenceManager.newInstance(
            org.openmdx.preferences2.jmi1.Preferences.class
        );
        try {
            preferences.setType("any");
            Assertions.fail("Only 'system' and 'user' are legal types");
        } catch (IllegalArgumentException expected) {
            // Only "system" and "user" are legal types
        }
        preferences.setType("system");
        segment.addPreferences("System", preferences);
        Node root = persistenceManager.newInstance(Node.class);
        root.setName("");
        Assertions.assertEquals("/",  root.getAbsolutePath(), "Root node");
        preferences.addNode(root);
        Node first = persistenceManager.newInstance(Node.class);
        first.setParent(root);
        try {
            first.setName("1/2");
            Assertions.fail("'/' are not allowed in names");
        } catch (IllegalArgumentException expected) {
            // '/' are not allowed in names
        }
        first.setName("1st");
        preferences.addNode(first);
        Assertions.assertEquals("/1st",  first.getAbsolutePath(), "First node");
        Entry entry1 = persistenceManager.newInstance(Entry.class);
        entry1.setValue("Zum ersten");
        first.addEntry("I", entry1);
        Assertions.assertEquals("I",  entry1.getName());
        Node second = persistenceManager.newInstance(Node.class);
        second.setParent(first);
        second.setName("2nd");
        Assertions.assertEquals("/1st/2nd",  second.getAbsolutePath(), "Second node");
        Entry entry2 = persistenceManager.newInstance(Entry.class);
        entry2.setValue("Zum zweiten");
        second.addEntry("II", entry2);
        Assertions.assertEquals("II",  entry2.getName());
        preferences.addNode(second);
        persistenceManager.currentTransaction().commit();
        int children = 0;
        for(Node child : first.<Node>getChild(first.getRoot())) {
            Assertions.assertEquals("/1st/2nd",  child.getAbsolutePath(), "Child");
            children++;
        }
        Assertions.assertEquals(1,  children, "Number of children");
    }

    /**
     * Read Preferences through the JDK API
     */
//    @Disabled
    @Test
    public void jdkReadPreferences(
    ) throws BackingStoreException{
        PreferencesFactory testee = new EmbeddedPreferencesFactory(JMI_SEGMENT_NAME);
        Preferences systemRoot = testee.systemRoot();
        Preferences first = systemRoot.node("1st");
        Assertions.assertEquals("/1st",  first.absolutePath(), "First node");
        assertArrayEquals("The first node's keys", new String[]{"I"}, first.keys());
        String entry1 = first.get("I", "ZUM ERSTEN");
        Assertions.assertEquals("Zum ersten",  entry1, "The first node's entry");
        Preferences second = first.node("2nd");
        Assertions.assertEquals("/1st/2nd",  second.absolutePath(), "Second node");
        String entry2 = second.get("II", "ZUM ZWEITEN");
        Assertions.assertEquals("Zum zweiten",  entry2, "The second node's entry");
        int children = 0;
        for(String name : first.childrenNames()) {
            Preferences child = first.node(name);
            Assertions.assertEquals("/1st/2nd",  child.absolutePath(), "Child");
            children++;
        }
        Assertions.assertEquals(1,  children, "Number of children");
    }

    private void assertArrayEquals(String string, String[] strings, String[] keys) {

		
	}

	/**
     * Create Preferences through the JDK API
     */
//    @Disabled
    @Test
    public void jdkCreatePreferences(
    ) throws BackingStoreException {
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
            Assertions.assertEquals("/",  userRoot.absolutePath(), "The userRoot's absolute path");
            Preferences packageNode = userRoot.node("test/openmdx/preferences2");
            Assertions.assertEquals("/test/openmdx/preferences2",  packageNode.absolutePath(), "The packageNode's absolute path");
            Preferences dummyNode = userRoot.node("/test/openmdx/dummy0");
            Assertions.assertEquals("/test/openmdx/dummy0",  dummyNode.absolutePath(), "The dummyNode's absolute path");
            dummyNode.put("dummy","value");
            Preferences testNode = userRoot.node("test");
            Assertions.assertEquals("/test",  testNode.absolutePath(), "The testNode's absolute path");
            Preferences parentNode = testNode.node("openmdx");
            Assertions.assertEquals("/test/openmdx",  parentNode.absolutePath());
            Assertions.assertEquals(Sets.asSet(Arrays.asList("preferences2", "dummy0")),  Sets.asSet(Arrays.asList(parentNode.childrenNames())), "The parent node's transient children");
            packageNode.put("kind", "wrong");
            packageNode.putBoolean("flag", false);
            Assertions.assertEquals(Sets.asSet("kind","flag"),  Sets.asSet(packageNode.keys()), "The package node's keys");
            packageNode.remove("kind");
            dummyNode.removeNode();
            Assertions.assertEquals(Sets.asSet(Collections.singletonList("preferences2")),  Sets.asSet(Arrays.asList(parentNode.childrenNames())), "The parent node's persistent children");
        }
    }

    private void jmiReadPreferences(Segment segment, String kind) {
        org.openmdx.preferences2.jmi1.Preferences preferences = segment.getPreferences(kind);
        PersistenceHelper.retrieveAllDescendants(preferences);
    }
    
    /**
     * Read Preferences through the JMI API
     */
//    @Disabled
    @Test
    public void jmiReadPreferences(
    ){
        Segment segment = retrieveSegment(JDK_SEGMENT_NAME);
        jmiReadPreferences(segment, "System");
        jmiReadPreferences(segment, System.getProperty("user.name"));
    }
    
    /**
     * Reads and writes the preferences through the JDK API
     * 
     * @param node the preferences to be touched
     */
    private void jdkTraversePreferences(
        Preferences node, 
        boolean touch
    ) throws BackingStoreException {
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
    
    private void jdkLoadAndTraversePreferences(
        Preferences root, 
        boolean touch
    ) throws BackingStoreException {
        ((Retrievable)root).retrieveAll();
        int expectedCount = touch ? RidOidQueryDatabase_2.getUpdateCount() : RidOidQueryDatabase_2.getQueryCount(); 
        jdkTraversePreferences(root, touch);
        int actualCount = touch ? RidOidQueryDatabase_2.getUpdateCount() : RidOidQueryDatabase_2.getQueryCount(); 
        Assertions.assertEquals(expectedCount,  actualCount, "No DB access necessary");
    }

    /**
     * Reads and writes the preferences through the JDK API
     */
//    @Disabled
    @Test
    public void jdkTouchPreferences(
    ) throws BackingStoreException {
        PreferencesFactory testee = new EmbeddedPreferencesFactory(JDK_SEGMENT_NAME);
        jdkLoadAndTraversePreferences(testee.systemRoot(), true);
        jdkLoadAndTraversePreferences(testee.userRoot(), true);
    }

    /**
     * Reads and writes the preferences through the JDK API
     */
//    @Disabled
    @Test
    public void jdkDumpPreferences(
    ) throws BackingStoreException {
        PreferencesFactory testee = new EmbeddedPreferencesFactory(JDK_SEGMENT_NAME);
        jdkLoadAndTraversePreferences(testee.systemRoot(), false);
        jdkLoadAndTraversePreferences(testee.userRoot(), false);
    }
    
    /**
     * 
     * Test Preferences Factory
     */
    class EmbeddedPreferencesFactory extends AutocommittingPreferencesFactory {

        /**
         * Constructor 
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

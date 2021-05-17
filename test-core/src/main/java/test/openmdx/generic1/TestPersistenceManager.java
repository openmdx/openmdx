package test.openmdx.generic1;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmdx.generic1.jmi1.Property;
import org.openmdx.generic1.jmi1.StringProperty;
import org.openmdx.kernel.lightweight.naming.NonManagedInitialContextFactoryBuilder;
import org.w3c.cci2.Container;

import test.openmdx.app1.cci2.GenericAddress;

public class TestPersistenceManager {

    protected static final String ENTITY_MANAGER_FACTORY_NAME = "test-Main-EntityManagerFactory";
    protected static PersistenceManagerFactory entityManagerFactory;
    protected PersistenceManager entityManager;
    

    @BeforeClass
    public static void createPersistenceManagerFactory(
    ) throws NamingException{
        entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            ENTITY_MANAGER_FACTORY_NAME
        );
        if(!NamingManager.hasInitialContextFactoryBuilder()) {
            NonManagedInitialContextFactoryBuilder.install(null);
        }
    }
    
    @Before
    public void setUp(){
        this.entityManager = entityManagerFactory.getPersistenceManager();
    }

    @Test
    public void whenOneChildIsAddedToTransientContainerThenItsSizeIs1(){
    	// Arrange
    	final GenericAddress parent = this.entityManager.newInstance(GenericAddress.class);
    	final Container<Property> testee = parent.<Property>getProperty();
    	final StringProperty child = this.entityManager.newInstance(StringProperty.class);
    	// Act
    	testee.add(child);
    	// Assert
    	Assert.assertEquals(1, testee.size());
    }

    @Test
    public void whenOneChildIsAddedToTransientContainerThenItIsReturnedByIterator(){
    	// Arrange
    	final GenericAddress parent = this.entityManager.newInstance(GenericAddress.class);
    	final Container<Property> testee = parent.<Property>getProperty();
    	final StringProperty child = this.entityManager.newInstance(StringProperty.class);
    	testee.add(child);
    	// Act
    	Property found = null;
    	for(Property property : testee) {
    		found = property;
    	}
    	// Assert
    	Assert.assertSame(child,found);
    }
    
}

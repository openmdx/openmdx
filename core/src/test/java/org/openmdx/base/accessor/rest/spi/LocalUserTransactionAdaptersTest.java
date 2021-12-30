package org.openmdx.base.accessor.rest.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.resource.ResourceException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmdx.application.transaction.JTALocalUserTransactionAdapter;
import org.openmdx.base.transaction.LocalUserTransaction;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.platform.Platform;

class LocalUserTransactionAdaptersTest {

    Factory<LocalUserTransaction> jtaUserTransactionFactory;
    Factory<LocalUserTransaction> containerManagedUserTransactionFactory;

    private LocalUserTransactionAdapters underTest;
    private String jtaKey;
    private String containerManagedKey;

    //    PersistenceManagerFactory pmf;
//    private PersistenceManager pm;

    @BeforeEach
    void setUp() {
        jtaKey = "org.openmdx.base.transaction.LocalUserTransaction.jta";
        jtaUserTransactionFactory = new LocalUserTransactionFactory(
            Platform.getProperty(jtaKey)
        );
        containerManagedKey = "org.openmdx.base.transaction.LocalUserTransaction.containerManaged";
        containerManagedUserTransactionFactory = new LocalUserTransactionFactory(
            Platform.getProperty(containerManagedKey)
        );
    }

    @AfterEach
    void tearDown() {
    }

//    @Test
//    @Ignore
//    void getResourceLocalUserTransactionAdapter() throws ResourceException {
//
//
//
//    }

    @Test
    void getJTAUserTransactionAdapter() throws ResourceException {
        final LocalUserTransaction localUserTransaction =
                LocalUserTransactionAdapters.getJTAUserTransactionAdapter();

        assertNotNull(localUserTransaction);
        assertEquals(Platform.getProperty(jtaKey), jtaKey);

    }

    @Test
    void getContainerManagedUserTransactionAdapter() throws ResourceException {

        final LocalUserTransaction localUserTransaction =
                LocalUserTransactionAdapters.getContainerManagedUserTransactionAdapter();

        final String className = containerManagedUserTransactionFactory.getInstanceClass().getName();

        assertNotNull(localUserTransaction);
        assertEquals(Platform.getProperty(containerManagedKey), className);
    }
}
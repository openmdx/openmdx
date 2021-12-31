package org.openmdx.base.accessor.rest.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.resource.ResourceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.openmdx.base.transaction.LocalUserTransaction;
import org.openmdx.junit5.OpenmdxCoreStandardExtension;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.platform.Platform;

@ExtendWith(OpenmdxCoreStandardExtension.class)
class LocalUserTransactionAdaptersTest {

    Factory<LocalUserTransaction> jtaUserTransactionFactory;
    Factory<LocalUserTransaction> containerManagedUserTransactionFactory;

    private static LocalUserTransactionAdapters underTest;
    private String jtaKey;
    private String containerManagedKey;

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

    @Test
    void testGetJTAUserTransactionAdapter() throws ResourceException {
        final String className = jtaUserTransactionFactory.getInstanceClass().getName();
        final LocalUserTransaction localUserTransaction =
                LocalUserTransactionAdapters.getJTAUserTransactionAdapter();

        assertEquals(Platform.getProperty(jtaKey), className);
        assertNotNull(localUserTransaction);
    }

    @Test
    void testGetJTAUserTransactionAdapterPlatformKeyNotExists() {
        jtaUserTransactionFactory = new LocalUserTransactionFactory(Platform.getProperty("non.existent.platform.key"));
        assertThrows(NullPointerException.class, (Executable) underTest);
    }

    @Test
    void testGetContainerManagedUserTransactionAdapter() throws ResourceException {
        final String className = containerManagedUserTransactionFactory.getInstanceClass().getName();
        final LocalUserTransaction localUserTransaction =
                underTest.getContainerManagedUserTransactionAdapter();

        assertEquals(Platform.getProperty(containerManagedKey), className);
        assertNotNull(localUserTransaction);
    }

    @Test
    void testGetContainerManagedUserTransactionAdapterPlatformKeyNotExists() {
        containerManagedUserTransactionFactory = new LocalUserTransactionFactory(Platform.getProperty("non.existent.platform.key"));
        assertThrows(NullPointerException.class, (Executable) underTest);
    }
}
/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: User Transactions 
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.rest.spi;

import javax.jdo.PersistenceManager;
#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransactionException;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.spi.LocalTransactionException;
#endif

import org.openmdx.base.transaction.LocalUserTransaction;
import org.openmdx.kernel.loading.Factory;
import org.openmdx.kernel.platform.Platform;

/**
 * UserTransactions
 */
public class LocalUserTransactionAdapters {

    private static final Factory<LocalUserTransaction> jtaUserTransactionFactory = new LocalUserTransactionFactory(
        Platform.getProperty(
                "org.openmdx.base.transaction.LocalUserTransaction.jta",
                "org.openmdx.application.transaction.JTALocalUserTransactionAdapter"
        )
    );

    private static final Factory<LocalUserTransaction> containerManagedLocalUserTransactionFactory = new LocalUserTransactionFactory(
        Platform.getProperty(
                "org.openmdx.base.transaction.LocalUserTransaction.containerManaged",
                "org.openmdx.application.transaction.ContainerManagedLocalUserTransactionAdapter"
        )
    );

    public static LocalUserTransaction getResourceLocalUserTransactionAdapter(
        PersistenceManager persistenceManager
    ) throws ResourceException {
        return new ResourceLocaUserTransactionAdapter(persistenceManager);
    }

    public static LocalUserTransaction getJTAUserTransactionAdapter(
    ) throws ResourceException {
        try {
            return jtaUserTransactionFactory.instantiate();
        } catch (RuntimeException exception) {
            throw new LocalTransactionException(
            	"JTA " + LocalUserTransaction.class.getName() + " acquisition failure",
                exception
            );
        }
    }

    public static LocalUserTransaction getContainerManagedUserTransactionAdapter(
    ) throws ResourceException {
        try {
            return containerManagedLocalUserTransactionFactory.instantiate();
        } catch (RuntimeException exception) {
            throw new LocalTransactionException(
        		"Container managed " + LocalUserTransaction.class.getName() + " acquisition failure",
                exception
            );
        }
    }

}

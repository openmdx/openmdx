/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Atomikos Initial Context Factory
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
package org.openmdx.kernel.lightweight.naming;

import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import #if JAVA_8 javax.transaction.TransactionSynchronizationRegistry #else jakarta.transaction.TransactionSynchronizationRegistry #endif;
import org.openmdx.kernel.lightweight.naming.jdbc.AbstractDataSourceContext;
import org.openmdx.kernel.lightweight.naming.jdbc.AtomikosDataSourceContext;
import com.atomikos.icatch.jta.TransactionSynchronizationRegistryImp;
import com.atomikos.icatch.jta.UserTransactionManager;

/**
 * Atomikos Initial Context Factory
 */
public class AtomikosInitialContextFactory extends AbstractInitialContextFactory {

	@Override
    protected LightweightInitialContext createInitialContext(
    ) throws NamingException {
        try {
            //
            // Atomikos transaction manager set-up
            //
        	final UserTransactionManager userTransactionManager = new UserTransactionManager();
            final TransactionSynchronizationRegistry transactionSynchronizationRegistry = new TransactionSynchronizationRegistryImp();
            //
            // Initial context setup
            //
            return createInitialContext(
            	userTransactionManager,
                transactionSynchronizationRegistry,
                userTransactionManager
            );
        } catch (NoInitialContextException exception) {
            throw exception;
        } catch (Exception exception) {
            throw (NoInitialContextException) new NoInitialContextException(
                "Unable to set up the openMDX lightweight container with the Atomikos transaction manager"
            ).initCause(
                exception
            );
        }
    }

	@Override
	protected AbstractDataSourceContext createDataSourceContext(){
		return new AtomikosDataSourceContext();
	}

}

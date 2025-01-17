/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Transaction Test
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
package test.openmdx.kernel.lightweight.transaction;

import static org.junit.jupiter.api.Assertions.fail;

import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserCallbackException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.naming.NamingException;
import javax.transaction.Synchronization; // JDO 3 requires JTA 1.3!
#if JAVA_8
	import javax.resource.ResourceException;
	import javax.transaction.SystemException;
	import javax.transaction.UserTransaction;
#else
	import jakarta.resource.ResourceException;
	import jakarta.transaction.SystemException;
	import jakarta.transaction.UserTransaction;
#endif	
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openmdx.application.transaction.UserTransactions;
import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.jmi1.Provider;
import org.openmdx.base.transaction.Status;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

import test.openmdx.app1.jmi1.App1Package;
import test.openmdx.app1.jmi1.MessageTemplate;
import test.openmdx.app1.jmi1.Segment;

/**
 * Test the lightweight container's transaction management
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
public class TransactionTest {

    /**
     * Entity Manager Factory
     */
    protected static PersistenceManagerFactory entityManagerFactory;

    @Test
    public void testSuccessfulCallback() throws ResourceException, NamingException{
        runTest("successfulCallback", true);
    }

    @Test
    public void testFailingCallback() throws ResourceException, NamingException{
        runTest("failingCallback", false);
    }    

    protected Synchronization getSynchronization(
        final boolean successful
    ) throws ResourceException{
        final UserTransaction userTransaction = UserTransactions.getUserTransaction();
        return new Synchronization(){

            public void afterCompletion(int status) {
                SysLog.info("Synchorization.aterCompletion()", Status.valueOf(status));
            }

            public void beforeCompletion() {
                SysLog.info("Synchorization.beforCompletion()", successful ? "successful" : "failing");
                if(!successful) {
                    try {
                        userTransaction.setRollbackOnly();
                    } catch (IllegalStateException exception) {
                        throw new JDOUserCallbackException(
                            "Failing beforeCompletion() failed to set rollback-only",
                            exception
                        );
                    } catch (SystemException exception) {
                        throw new JDOUserCallbackException(
                            "Failing beforeCompletion() failed to set rollback-only",
                            exception
                        );
                    }
                }
            }
            
        };
    }
        
    /* (non-Javadoc)
     * @see junit.framework.TestCase#runTest()
     */
    protected void runTest(
        String name,
        boolean successful
    ) throws ResourceException, NamingException{
        PersistenceManager entityManager = entityManagerFactory.getPersistenceManager();
        Transaction unitOfWork = entityManager.currentTransaction();
        Authority authority = entityManager.getObjectById(Authority.class, App1Package.AUTHORITY_XRI);
        Provider provider = authority.getProvider("Data");
        Segment segment = (Segment) provider.getSegment(name);
        if(segment != null) {            
            unitOfWork.begin();
            segment.refDelete();
            unitOfWork.commit();
        }
        unitOfWork.begin();
        App1Package app1 = (App1Package) provider.refOutermostPackage().refPackage("test:openmdx:app1");
        Synchronization synchronization = getSynchronization(successful);
        segment = app1.getSegment().createSegment();
        provider.addSegment(false, name, segment);
        unitOfWork.commit();
        unitOfWork.setSynchronization(synchronization);
        try {
            unitOfWork.begin();
            MessageTemplate messageTemplate = app1.getMessageTemplate().createMessageTemplate();
            messageTemplate.setText("");
            segment.addMessageTemplate(false, "ko", messageTemplate);
            messageTemplate = app1.getMessageTemplate().createMessageTemplate();
            messageTemplate.setText("Ok");
            segment.addMessageTemplate(false, "ok", messageTemplate);
            unitOfWork.commit();
            Assertions.fail("Empty description should have been rejected");
        } catch (JDOFatalDataStoreException jdoException) {
            BasicException exception = (BasicException) jdoException.getCause();
            Assertions.assertEquals(
                BasicException.Code.ROLLBACK,
                exception.getExceptionCode(),
                "Unit fo work failure"
            );
        }
        unitOfWork.begin();
        MessageTemplate messageTemplate = app1.getMessageTemplate().createMessageTemplate();
        messageTemplate.setText("Ok");
        segment.addMessageTemplate(false, "ok", messageTemplate);
        if(successful) {
            unitOfWork.commit();
        } else try {
            unitOfWork.commit();
            fail("Callback should have set rollback-only");
        } catch (JDOFatalDataStoreException jdoException) {
            BasicException exception = (BasicException) jdoException.getCause();
            Assertions.assertEquals(
                BasicException.Code.ROLLBACK,
                exception.getExceptionCode(),
                "Unit fo work failure"
            );
        }
    }

    @BeforeAll
    public static void initialize(
    ) throws NamingException{
        entityManagerFactory = JDOHelper.getPersistenceManagerFactory(
            "test-Main-EntityManagerFactory"
        );
    }

}

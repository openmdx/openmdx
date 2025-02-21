/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Auto-Closeable Persistence Manager 
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

package org.openmdx.kernel.jdo;

import javax.jdo.PersistenceManager;


/**
 * Auto-Closeable Persistence Manager for JDO 3.1
 */
public interface JDOPersistenceManager extends PersistenceManager, AutoCloseable {

    /** Close this {@code PersistenceManager} so that no further requests 
     * may be made on it.  A {@code PersistenceManager} instance can be 
     * used only until it is closed.
     *
     * <P>Closing a {@code PersistenceManager} might release it to the pool 
     * of available {@code PersistenceManager}s, or might be garbage 
     * collected, at the option of the JDO implementation.  Before being used 
     * again to satisfy a {@code getPersistenceManager()} request, the 
     * default values for options will be restored to their values as specified 
     * in the {@code PersistenceManagerFactory}.
     *
     * <P>This method closes the {@code PersistenceManager}.
     */
    void close ();

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    @Override
    JDOPersistenceManagerFactory getPersistenceManagerFactory();

}

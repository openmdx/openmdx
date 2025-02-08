/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JNID Accessor
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
package org.openmdx.application.naming;

import javax.naming.InitialContext;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Factory;

/**
 * JNDI Acccessor
 * <p>
 * This class is instantiated reflectively, e.g. by
 * <li>AbstractPersistenceManagerFactory.getConnectionFactoryByName()
 * </ul>
 */
public class JNDIAccessor<T> implements Factory<T> {

    /**
     * Constructor
     * <p>
     * This constructor is invoked reflectively by the method
     * AbstractPersistenceManagerFactory.getConnectionFactoryByName()
     *
     * @param jndiName the object's JNDI name
     * @param type the expected class
     */
    public JNDIAccessor(
        String jndiName,
        Class<T> type
    ){
        this.jndiName = jndiName;
        this.type = type;
    }
    
    /**
     * The object's JNDI name
     */
    private final String jndiName;
    
    private final Class<T> type;
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.loading.Factory#instantiate()
     */
    @Override
    public T instantiate() {
        try {
            return type.cast(new InitialContext().lookup(jndiName));
        } catch (Exception exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Connection factory lookup failure",
                new BasicException.Parameter(
                    "jndiName", 
                    jndiName
                ),
                new BasicException.Parameter(
                    "type", 
                    type.getName()
                )
            ); 
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.loading.Factory#getInstanceClass()
     */
    @Override
    public Class<? extends T> getInstanceClass() {
        return type;
    }

}

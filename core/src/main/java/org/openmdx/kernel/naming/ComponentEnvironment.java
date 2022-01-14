/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Component Environment 
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
package org.openmdx.kernel.naming;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOFatalUserException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Factory;

/**
 * Component Environment
 */
public class ComponentEnvironment {

    /**
     * Constructor 
     */
    private ComponentEnvironment() {
        // Avoid instantiation
    }

    /**
     * The factory registry
     */
    private static Map<Class<?>, Factory<?>> registry = new HashMap<Class<?>, Factory<?>>();
    
    /**
     * Retrieve an object registered in the registry or the component's JNDI context
     * 
     * @param name the objects JNDI name
     * 
     * @return the object
     * @throws BasicException 
     * 
     * @exception JDOFatalUserException if the object's acquisition fails
     */
    public static <T> T lookup(
        Class<T> objectClass
    ) throws BasicException{
        Factory<?> factory = registry.get(objectClass);
        if(factory == null) {
            final String jndiName = "java:comp/" + objectClass.getSimpleName();
            try {
            	return objectClass.cast(new InitialContext().lookup(jndiName));
            } catch (NamingException exception) {
                throw BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_RESOURCE,
                    "Unable to retrieve object from JNDI",
                    new BasicException.Parameter("class", objectClass.getName()),
                    new BasicException.Parameter("name", jndiName)
                );
            }
        } else {
            try {
                return objectClass.cast(factory.instantiate());
            } catch (RuntimeException exception) {
                throw BasicException.newStandAloneExceptionStack(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_RESOURCE,
                    "Unable to retrieve object from registered factory",
                    new BasicException.Parameter("class", objectClass.getName()),
                    new BasicException.Parameter("factoryClass", factory.getClass().getName())
                );
            }
        }
    }

    /**
     * Register a component object
     * 
     * @param factory a factory returning the registration <em>Interface</em> as instance class.
     */
    public static synchronized <T> void register (
        Factory<?> factory
    ){
        registry.put(factory.getInstanceClass(), factory);
    }

}

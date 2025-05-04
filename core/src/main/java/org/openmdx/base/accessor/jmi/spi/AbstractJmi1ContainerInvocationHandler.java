/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: ContainerInvocationHandler 
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
package org.openmdx.base.accessor.jmi.spi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
#if !CLASSIC_CHRONO_TYPES
import java.util.ArrayList;
import java.util.List;
#endif

import javax.jmi.reflect.InvalidCallException;

#if !CLASSIC_CHRONO_TYPES
import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefQualifier;
#endif
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * ContainerInvocationHandler
 */
abstract class AbstractJmi1ContainerInvocationHandler
    implements InvocationHandler
{

    /**
     * Constructor 
     * 
     * @param marshaller the marshaller
     */
    protected AbstractJmi1ContainerInvocationHandler(
        Marshaller marshaller
    ) {
        this.marshaller = marshaller;
    }

    /**
     * The marshaller
     */
    protected final Marshaller marshaller;

    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        if(method.getDeclaringClass() == Object.class) {
            final String methodName = method.getName();
            if("hashCode".equals(methodName)) {
                return Integer.valueOf(
                    ReducedJDOHelper.getTransactionalObjectId(proxy).hashCode()
                );
            } else if ("equals".equals(methodName)) {
                return Boolean.valueOf(
                    ReducedJDOHelper.getPersistenceManager(proxy) == ReducedJDOHelper.getPersistenceManager(args[0]) &&
                    ReducedJDOHelper.getTransactionalObjectId(proxy).equals(ReducedJDOHelper.getTransactionalObjectId(args[0]))
                );
            } else if ("toString".equals(methodName)) {
                return proxy.getClass().getInterfaces()[0].getName() + ": " + ReducedJDOHelper.getAnyObjectId(proxy);
            } else throw new JmiServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Unexpected method dispatching",
                new BasicException.Parameter("method-class", Object.class.getName()),
                new BasicException.Parameter("method-name", methodName)
            );
        }
        try {
            return  invokeOnDelegate(proxy, method, args);
        } catch (InvocationTargetException exception) {
            final Throwable throwable = exception.getTargetException();
            throw throwable instanceof RuntimeServiceException ? new JmiServiceException(
                throwable.getCause()
            ) : throwable instanceof UnsupportedOperationException ? Throwables.initCause(
                new InvalidCallException(null, null, throwable.getMessage()), 
                throwable, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED
            ) : throwable;
        }
    }

    protected abstract Object invokeOnDelegate(
        Object proxy,
        Method method,
        Object[] args
    ) throws IllegalAccessException, InvocationTargetException, ServiceException;

    #if !CLASSIC_CHRONO_TYPES
    /**
     * Convert the given arguments to a list of qualifiers
     *
     * @param args the arguments
     * @return the qualifiers
     */
    protected static List<RefQualifier> getQualifierList(Object[] args) {

        if (args == null) {
            throw new IllegalArgumentException("Arguments array cannot be null.");
        }

        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Arguments array must have an even number of elements.");
        }

        List<RefQualifier> qualifiers = new ArrayList<>();

        for (int i = 0; i < args.length; i += 2) {
            qualifiers.add(new RefQualifier((QualifierType) args[i], args[i + 1]));
        }

        return qualifiers;
    }
    #endif
}

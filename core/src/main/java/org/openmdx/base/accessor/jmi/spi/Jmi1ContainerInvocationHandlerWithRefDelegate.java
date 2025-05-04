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

import org.oasisopen.jmi1.RefContainer;
import org.oasisopen.jmi1.RefQualifier;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.collection.MarshallingConsumer;
import org.openmdx.base.collection.MarshallingSpliterator;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.Container;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * ContainerInvocationHandler
 */
public class Jmi1ContainerInvocationHandlerWithRefDelegate extends AbstractJmi1ContainerInvocationHandler {

    /**
     * Constructor
     *
     * @param marshaller the marshaller
     * @param delegate   thedelegate
     */
    public Jmi1ContainerInvocationHandlerWithRefDelegate(
            Marshaller marshaller,
            RefContainer<?> delegate
    ) {
        super(marshaller);
        this.refDelegate = delegate;
    }

    /**
     * The delegate
     */
    private final RefContainer<?> refDelegate;

    @Override
    protected Object invokeOnDelegate(
            Object proxy,
            Method method,
            Object[] args
    ) throws ServiceException, IllegalAccessException, InvocationTargetException {
        final Class<?> declaringClass = method.getDeclaringClass();
        final String methodName = method.getName();
        if (declaringClass == proxy.getClass().getInterfaces()[0]) {
            // 
            // This typed association end interface has been prepended 
            // by the Jmi1ObjectInvocationHandler
            //
            #if CLASSIC_CHRONO_TYPES
            if("add".equals(methodName)) {
                this.refDelegate.refAdd(
                    (Object[]) this.marshaller.unmarshal(args)
                );
                return null;
            } else if("get".equals(methodName)) {
                return this.marshaller.marshal(
                    this.refDelegate.refGet(
                        (Object[]) this.marshaller.unmarshal(args)
                    )
                );
            } else if("remove".equals(methodName)) {
                this.refDelegate.refRemove(
                    (Object[]) this.marshaller.unmarshal(args)
                );
                return null;
            }
            #else
            final Object[] arguments = (Object[]) this.marshaller.unmarshal(args);
            final RefArguments refArguments = RefArguments.newInstance(arguments);
            switch (methodName) {
                case "add":
                    ((RefContainer_1) this.refDelegate).refAdd(
                            refArguments.qualifiers,
                            (RefObject_1_0) refArguments.value
                    );
                    break;
                case "get":
                    return this.marshaller.marshal(
                            this.refDelegate.refGet(refArguments.qualifiers)
                    );
                case "remove":
                    this.refDelegate.refRemove(refArguments.qualifiers);
                    break;
                default:
                    throw new UnsupportedOperationException("Method not supported: " + methodName);
            }
            #endif

        } else if (declaringClass == Container.class) {
            // 
            // This interface is extended by the typed association end
            // interface which has been prepended 
            // by the Jmi1ObjectInvocationHandler
            //
            #if CLASSIC_CHRONO_TYPES
            if ("getAll".equals(methodName) && args.length == 1) {
                return this.marshaller.marshal(
                        this.refDelegate.refGetAll(
                                this.marshaller.unmarshal(args[0])
                        )
                );
            } else if ("removeAll".equals(methodName) && args.length == 1) {
                this.refDelegate.refRemoveAll(
                        this.marshaller.unmarshal(args[0])
                );
                return null;
            } else if ("processAll".equals(methodName) && args.length == 2) {
                @SuppressWarnings("unchecked") final Consumer<RefObject_1_0> action = (Consumer<RefObject_1_0>) args[1];
                this.refDelegate.processAll(
                        (AnyTypePredicate) this.marshaller.unmarshal(args[0]),
                        new MarshallingConsumer<>(RefObject_1_0.class, action, this.marshaller)
                );
                return null;
            }
            #else
            // TODO: kjdd
            // Wird, wie schon letztes mal angemerkt, bei "add" abstürzen, da Anzahl der ARgumente dann ungerade ist!
            if ("getAll".equals(methodName) && args.length == 1) {
                return this.marshaller.marshal(
                        this.refDelegate.refGetAll(
                                this.marshaller.unmarshal(args[0])
                        )
                );
            } else if ("removeAll".equals(methodName) && args.length == 1) {
                this.refDelegate.refRemoveAll(
                        this.marshaller.unmarshal(args[0])
                );
                return null;
            } else if ("processAll".equals(methodName) && args.length == 2) {
                @SuppressWarnings("unchecked") final Consumer<RefObject_1_0> action = (Consumer<RefObject_1_0>) args[1];
                this.refDelegate.processAll(
                        (AnyTypePredicate) this.marshaller.unmarshal(args[0]),
                        new MarshallingConsumer<>(RefObject_1_0.class, action, this.marshaller)
                );
                return null;
            }
            #endif
        } else if (declaringClass == Collection.class) {
            if ("toArray".equals(methodName) && args != null && args.length == 1) {
                Object[] source = ((Collection<?>) this.refDelegate).toArray();
                Object[] target = (Object[]) args[0];
                int size = this.refDelegate.size();
                if (target.length < size) {
                    target = (Object[]) Array.newInstance(target.getClass().getComponentType(), size);
                }
                int i;
                for (i = 0; i < size; i++) {
                    target[i] = this.marshaller.marshal(source[i]);
                }
                if (i < target.length) {
                    target[i] = null;
                }
                return target;
            } else if ("spliterator".equals(methodName) && (args == null || args.length == 0)) {
                return new MarshallingSpliterator<>(
                        RefObject_1_0.class,
                        this.refDelegate.spliterator(),
                        this.marshaller
                );
            } else if ("removeIf".equals(methodName) && args != null && args.length == 1) {
                @SuppressWarnings("unchecked") final Predicate<RefObject_1_0> predicate = (Predicate<RefObject_1_0>) args[0];
                Boolean changed = Boolean.FALSE;
                for (Iterator<?> i = this.refDelegate.iterator(); i.hasNext(); ) {
                    final RefObject_1_0 candidate = (RefObject_1_0) this.marshaller.marshal(i.next());
                    if (predicate.test(candidate)) {
                        i.remove();
                        changed = Boolean.TRUE;
                    }
                }
                return changed;
            } else if ("stream".equals(methodName)) {
                return StreamSupport.stream(
                        new MarshallingSpliterator<>(
                                RefObject_1_0.class,
                                this.refDelegate.spliterator(),
                                this.marshaller
                        ),
                        false
                );
            }
        } else if (declaringClass == Iterable.class) {
            if ("forEach".equals(methodName) && args != null && args.length == 1) {
                @SuppressWarnings("unchecked")
                Consumer<RefObject_1_0> action = (Consumer<RefObject_1_0>) args[0];
                this.refDelegate.forEach(
                        new MarshallingConsumer<>(RefObject_1_0.class, action, marshaller)
                );
            }
        }
        return this.marshaller.marshal(
                method.invoke(this.refDelegate,
                        (Object[]) this.marshaller.unmarshal(args)
                )
        );
    }

}

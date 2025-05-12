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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;

import org.oasisopen.jmi1.RefContainer;
import org.oasisopen.jmi1.RefQualifier;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.collection.MarshallingConsumer;
import org.openmdx.base.collection.MarshallingSpliterator;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.Container;

/**
 * ContainerInvocationHandler
 */
public class Jmi1ContainerInvocationHandlerWithCciDelegate extends AbstractJmi1ContainerInvocationHandler {

    /**
     * Constructor 
     *
     * @param marshaller the marshaller
     * @param delegate the delegate
     */
    public Jmi1ContainerInvocationHandlerWithCciDelegate(
        Marshaller marshaller,
        Container<?> delegate
    ) {
        super(marshaller);
        this.cciDelegate = delegate;
    }
    
    /**
     * The delegate
     */
    private final Container<?> cciDelegate;
    
    @Override
    protected Object invokeOnDelegate(
        Object proxy,
        Method method,
        Object[] args
    ) throws IllegalAccessException, InvocationTargetException, ServiceException {
        final Class<?> declaringClass = method.getDeclaringClass();
        final String methodName = method.getName();
        if(declaringClass == RefBaseObject.class) {
            if(
                "refOutermostPackage".equals(methodName) && 
                this.marshaller instanceof StandardMarshaller
             ) {
                return ((StandardMarshaller)this.marshaller).getOutermostPackage();
            } else if("refMofId".equals(methodName)) {
                if(this.cciDelegate instanceof RefBaseObject) {
                    return ((RefBaseObject)this.cciDelegate).refMofId();
                } else {
                    Object xri = ReducedJDOHelper.getObjectId(this.cciDelegate);
                    return xri instanceof Path ? ((Path)xri).toXRI() : null;
                }
            } else throw new UnsupportedOperationException(
                declaringClass + ": " + methodName
            );
        } else if(declaringClass == RefContainer.class) {
            if("refAdd".equals(methodName)) {
                final Method cciAdd = ReferenceDef.getInstance(proxy.getClass()).add;
                cciAdd.invoke(
                    this.cciDelegate,
                    toCciArguments(args, cciAdd.getParameterCount())
                );
                return null;
            } else if("refGet".equals(methodName)) {
                final Method cciGet = ReferenceDef.getInstance(proxy.getClass()).get;
                return this.marshaller.marshal(
                        cciGet.invoke(
                        this.cciDelegate,
                        toCciArguments(args, cciGet.getParameterCount())
                    )
                );
            } else if("refRemove".equals(methodName)) {
                final Method cciRemove = ReferenceDef.getInstance(proxy.getClass()).remove;
                cciRemove.invoke(
                    this.cciDelegate,
                    toCciArguments(args, cciRemove.getParameterCount())
                );
                return null;
            } else if("refGetAll".equals(methodName)) {
                Object predicate = this.marshaller.unmarshal(args[0]);
                Object value;
                if(predicate instanceof AnyTypePredicate){
                    value = ReferenceDef.getAll.invoke(
                        this.cciDelegate, 
                        predicate
                    ); 
                } else if (this.cciDelegate instanceof RefContainer<?>) {
                    value = ((RefContainer<?>)this.cciDelegate).refGetAll(
                        predicate
                    );
                } else if (predicate instanceof RefQuery_1_0) {
                    value = this.cciDelegate.getAll(
                        ((RefQuery_1_0)predicate).refGetFilter()
                    );
                } else {
                    throw new IllegalArgumentException(
                        "Unsupported container/filter combination"
                    );
                }
                return this.marshaller.marshal(value);
            } else if("refRemoveAll".equals(methodName)) {
                final Object predicate = this.marshaller.unmarshal(args[0]);
                if(predicate instanceof AnyTypePredicate) {
                    ReferenceDef.removeAll.invoke(
                        this.cciDelegate, 
                        predicate
                    );
                } else {
                    ((RefContainer<?>)this.cciDelegate).refRemoveAll(
                        predicate
                    );
                }
                return null;
            } else throw new UnsupportedOperationException(
                declaringClass + ": " + methodName
             );
        } else if (declaringClass == Collection.class) {
            if("toArray".equals(methodName) && args != null && args.length == 1) {
                Object[] source = ((Collection<?>)this.cciDelegate).toArray();
                Object[] target = (Object[]) args[0];
                int size = this.cciDelegate.size();
                if (target.length < size){
                    target = (Object[]) Array.newInstance(target.getClass().getComponentType(), size);
                }
                int i;
                for(i = 0; i < size; i++){
                    target[i] = this.marshaller.marshal(source[i]);
                }
                if(i < target.length) {
                    target[i] = null;
                }
                return target;
            } else if("spliterator".equals(methodName) && (args == null || args.length == 0)) {
                return new MarshallingSpliterator<>(
                    RefObject_1_0.class, 
                    this.cciDelegate.spliterator(), 
                    this.marshaller
                );
            } else if ("removeIf".equals(methodName) && args != null && args.length == 1) {
                @SuppressWarnings("unchecked")
                final Predicate<RefObject_1_0> predicate = (Predicate<RefObject_1_0>) args[0];
                Boolean changed = Boolean.FALSE;
                for(Iterator<?> i = this.cciDelegate.iterator(); i.hasNext(); ) {
                    final RefObject_1_0 candidate = (RefObject_1_0) this.marshaller.marshal(i.next());
                    if(predicate.test(candidate)) {
                        i.remove();
                        changed = Boolean.TRUE;
                    }
                }
                return changed;
            } else if ("stream".equals(methodName)) {
                return StreamSupport.stream(
                    new MarshallingSpliterator<>(
                        RefObject_1_0.class, 
                        this.cciDelegate.spliterator(), 
                        this.marshaller
                    ), 
                    false
                );
            }
        } else if (declaringClass == Container.class) {
            if("processAll".equals(methodName) && args.length == 2) {
                @SuppressWarnings("unchecked")
                final Consumer<RefObject_1_0> action = (Consumer<RefObject_1_0>) args[1];
                this.cciDelegate.processAll(
                    (AnyTypePredicate) this.marshaller.unmarshal(args[0]),
                    new MarshallingConsumer<>(RefObject_1_0.class, action, this.marshaller)    
                );
                return null;
            } else if("getAll".equals(methodName) && args.length == 1) {
                return this.marshaller.marshal(
                    this.cciDelegate.getAll(
                        (AnyTypePredicate)this.marshaller.unmarshal(args[0])
                    )
                );
            } else if("removeAll".equals(methodName) && args.length == 1) {
                this.cciDelegate.removeAll(
                    (AnyTypePredicate)this.marshaller.unmarshal(args[0])
                );
                return null;
            }
        } else if (declaringClass == Iterable.class) {
            if("forEach".equals(methodName) && args != null && args.length == 1) {
                @SuppressWarnings("unchecked")
                Consumer<RefObject_1_0> action = (Consumer<RefObject_1_0>) args[0];
                this.cciDelegate.forEach(
                    new MarshallingConsumer<>(RefObject_1_0.class, action, this.marshaller)
                );
            }
        } else if(declaringClass == PersistenceCapable.class) {
            if("jdoGetPersistenceManager".equals(methodName)) {
                if(this.marshaller instanceof StandardMarshaller) {
                    return ((StandardMarshaller)this.marshaller).getOutermostPackage().refPersistenceManager();
                } else {
                    throw new UnsupportedOperationException(
                        "Don't know how to retrieve the PersistenceManager from the given marshaller: " +
                        this.marshaller.getClass().getName()
                    );
                }
            } 
        } 
        return this.marshaller.marshal(
            method.invoke(
                this.cciDelegate, 
                (Object[]) this.marshaller.unmarshal(args)
            )
        );
    }

    #if CLASSIC_CHRONO_TYPES

    private Object[] toCciArguments(Object[] refArgs, int parameterCount) throws ServiceException {
        return (Object[]) this.marshaller.unmarshal(refArgs[0]);
    }

    #else

    private Object[] toCciArguments(Object[] refArgs, int parameterCount) throws ServiceException {
        final Object[] cciArgs = new Object[parameterCount];
        switch(refArgs.length) {
            case 1:
                toCciQualifiers(cciArgs, (List<RefQualifier>) refArgs[0]);
                break;
            case 2:
                toCciQualifiers(cciArgs, (List<RefQualifier>) refArgs[0], (RefObject) refArgs[1]);
                break;
            default:
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "Unexpected number of arguments",
                    new BasicException.Parameter("refArgs", refArgs)
                );
        }
        return cciArgs;
    }

    private void toCciQualifiers(Object[] target, List<RefQualifier> source) {
        int i = 0;
        for (RefQualifier refQualifier : source) {
            target[i++] = refQualifier.qualifierType;
            target[i++] = refQualifier.qualifierValue;
        }
    }

    private void toCciQualifiers(Object[] target, List<RefQualifier> source, RefObject value)  throws ServiceException {
        toCciQualifiers(target, source);
        target[target.length - 1] = this.marshaller.unmarshal(value);
    }

        #endif


}

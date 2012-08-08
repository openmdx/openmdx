/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Jmi1ContainerInvocationHandler.java,v 1.19 2010/08/06 12:23:42 hburger Exp $
 * Description: ContainerInvocationHandler 
 * Revision:    $Revision: 1.19 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/08/06 12:23:42 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2010, OMEX AG, Switzerland
 * All rights reserved.
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.jmi.reflect.InvalidCallException;
import javax.jmi.reflect.RefBaseObject;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.LoggerFactory;
import org.w3c.cci2.AnyTypePredicate;
import org.w3c.cci2.Container;

/**
 * ContainerInvocationHandler
 */
public class Jmi1ContainerInvocationHandler
    implements InvocationHandler
{

    /**
     * Constructor 
     * 
     * @param marshaller 
     * @param delegate
     */
    public Jmi1ContainerInvocationHandler(
        Marshaller marshaller, 
        RefContainer<?> delegate
    ) {
        this.marshaller = marshaller == null ? IdentityMarshaller.INSTANCE : marshaller;
        this.refDelegate = delegate;
        this.cciDelegate = null;
    }

    /**
     * Constructor 
     *
     * @param delegate
     */
    public Jmi1ContainerInvocationHandler(
        Marshaller marshaller,
        Container<?> delegate
    ) {
        this.marshaller = marshaller == null ? IdentityMarshaller.INSTANCE : marshaller;
        this.cciDelegate = delegate;
        this.refDelegate = null;
    }
    
    /**
     * 
     */
    private final Marshaller marshaller;

    /**
     * 
     */
    private final RefContainer<?> refDelegate;

    /**
     * 
     */
    private final Container<?> cciDelegate;
    
            
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {        
        String methodName = method.getName();
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            if(this.cciDelegate == null) {
                if(declaringClass == proxy.getClass().getInterfaces()[0]) {
                    // 
                    // This typed association end interface has been prepended 
                    // by the Jmi1ObjectInvocationHandler
                    //
                    if("add".equals(methodName)) {
                        this.refDelegate.refAdd(
                            (Object[]) this.marshaller.unmarshal(args)
                        );
                        return null;
                    } 
                    else if("get".equals(methodName)) {
                        return this.marshaller.marshal(
                            this.refDelegate.refGet(
                                (Object[]) this.marshaller.unmarshal(args)
                            )
                        );
                    } 
                    else if("remove".equals(methodName)) {
                        this.refDelegate.refRemove(
                            (Object[]) this.marshaller.unmarshal(args)
                        );
                        return null;
                    }
                } 
                else if (declaringClass == Container.class) {
                    // 
                    // This interfaces is extended by the typed association end 
                    // interface which has been prepended 
                    // by the Jmi1ObjectInvocationHandler
                    //
                    if("getAll".equals(methodName) && args.length == 1) {
                        return this.marshaller.marshal(
                            this.refDelegate.refGetAll(
                                this.marshaller.unmarshal(args[0])
                            )
                        );
                    } 
                    else if("removeAll".equals(methodName) && args.length == 1) {
                        this.refDelegate.refRemoveAll(
                            this.marshaller.unmarshal(args[0])
                        );
                        return null;
                    }
                }
                return this.marshaller.marshal(
                    method.invoke(this.refDelegate, 
                        (Object[]) this.marshaller.unmarshal(args)
                    )
                );
            } 
            else {
                if(declaringClass == RefBaseObject.class) {
                    if(
                        "refOutermostPackage".equals(methodName) && 
                        this.marshaller instanceof Jmi1ObjectInvocationHandler.StandardMarshaller
                     ) {
                        return ((Jmi1ObjectInvocationHandler.StandardMarshaller)this.marshaller).getOutermostPackage();
                    } else if("refMofId".equals(methodName)) {
                        if(this.cciDelegate instanceof RefBaseObject) {
                            return ((RefBaseObject)this.cciDelegate).refMofId();
                        } else {
                            Object xri = PersistenceHelper.getContainerId(this.cciDelegate);
                            return xri == null ? null : xri.toString();
                        }
                    } else throw new UnsupportedOperationException(
                        declaringClass + ": " + methodName
                    );
                } 
                else if(declaringClass == RefContainer.class) {
                    if("refAdd".equals(methodName)) {
                        ReferenceDef.getInstance(proxy.getClass()).add.invoke(
                            this.cciDelegate, 
                            (Object[]) this.marshaller.unmarshal(args[0])
                        );
                        return null;
                    } 
                    else if("refGet".equals(methodName)) {
                        return this.marshaller.marshal(
                            ReferenceDef.getInstance(proxy.getClass()).get.invoke(
                                this.cciDelegate, 
                                (Object[]) this.marshaller.unmarshal(args[0])
                            )
                        );
                    } 
                    else if("refRemove".equals(methodName)) {
                        ReferenceDef.getInstance(proxy.getClass()).remove.invoke(
                            this.cciDelegate, 
                            (Object[]) this.marshaller.unmarshal(args[0])
                        );
                        return null;
                    } 
                    else if("refGetAll".equals(methodName)) {
                        Object predicate = this.marshaller.unmarshal(args[0]);
                        Object value;
                        if(predicate instanceof AnyTypePredicate){
                            value = ReferenceDef.getAll.invoke(
                                this.cciDelegate, 
                                predicate
                            ); 
                        } else if (predicate instanceof RefQuery_1_0) {
                            value = ((RefContainer<?>)this.cciDelegate).refGetAll(
                                ((RefQuery_1_0)predicate).refGetFilter()
                            );
                        } else if (this.cciDelegate instanceof RefContainer<?>) {
                            value = ((RefContainer<?>)this.cciDelegate).refGetAll(
                                predicate
                            );
                        } else {
                            throw new IllegalArgumentException(
                                "Unsupported container/filter combination"
                            );
                        }
                        return this.marshaller.marshal(value);
                    } 
                    else if("refRemoveAll".equals(methodName)) {
                        Object predicate = this.marshaller.unmarshal(args[0]);
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
                } else {
                    return this.marshaller.marshal(
                        method.invoke(
                            this.cciDelegate, 
                            (Object[]) this.marshaller.unmarshal(args)
                        )
                    );
                } 
            }
        } catch (InvocationTargetException exception) {
            Throwable throwable = exception.getTargetException();
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

    /**
     * Provide the container's add() arguments
     * 
     * @param containerClass the RefContainer sub-class
     * 
     * @return the container's add() arguments
     */
    @SuppressWarnings("unchecked")
    public static Class<?>[] getAddArguments(
        Class<? extends RefContainer> containerClass
    ){
        return ReferenceDef.getInstance(containerClass).add.getParameterTypes();
    }
    
    
    //------------------------------------------------------------------------
    // Class ReferenceDef
    //------------------------------------------------------------------------
    
    /**
     * ReferenceDef
     */
    static class ReferenceDef {
    
        /**
         * Constructor 
         *
         * @param nativeInterface
         */
        private ReferenceDef(
            Class<?> nativeInterface
        ){  
            Method add = null;
            Method get = null;
            Method remove = null;
            for(Method method : nativeInterface.getDeclaredMethods()) {
                String methodName = method.getName();
                if("add".equals(methodName)) {
                    add = method;
                } else if ("get".equals(methodName)) {
                    get = method;
                } else if ("remove".equals(methodName)) {
                    remove = method;
                }
            }
            this.add = add;
            this.get = get;
            this.remove = remove;
        }
        
        final Method add;
        final Method get;
        final Method remove;
        final static Method getAll = getContainerMethod("getAll");
        final static Method removeAll = getContainerMethod("removeAll");
        
        private final static ConcurrentMap<Class<?>,ReferenceDef> instances = 
            new ConcurrentHashMap<Class<?>,ReferenceDef>();

        /**
         * Retrieve a proxy class' instance
         * 
         * @param referenceClass
         * 
         * @return the corresponding instance
         */
        static ReferenceDef getInstance(
            Class<?> referenceClass
        ){
            ReferenceDef instance = instances.get(referenceClass);
            return instance == null ? Maps.putUnlessPresent(
                instances,
                referenceClass, 
                new ReferenceDef(referenceClass.getInterfaces()[0])
            ) : instance;
        }
                
        /**
         * Retrieve a bulk method
         * 
         * @param methodName
         * 
         * @return the requested method
         */
        private static Method getContainerMethod(
            String methodName
        ){
            try {
                return Container.class.getMethod(methodName, AnyTypePredicate.class);
            } catch (NoSuchMethodException exception) {
                LoggerFactory.getLogger().log(
                    Level.SEVERE,
                    "Expected getAll() and removeAll() being a member of " + Container.class.getName(),
                    exception
                );
                return null;
            }
        }
        
    }

    
    //------------------------------------------------------------------------
    // Class IdentityMarshaller
    //------------------------------------------------------------------------
    
    /**
     * 
     * IdentityMarshaller
     *
     */
    final static class IdentityMarshaller implements Marshaller {

        /**
         * Constructor 
         */
        private IdentityMarshaller() {
            super();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Marshaller#marshal(java.lang.Object)
         */
        public final Object marshal(Object source) {
            return source;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.persistence.spi.Marshaller#unmarshal(java.lang.Object)
         */
        public final Object unmarshal(Object source) {
            return source;
        } 
        
        /**
         * The singleton
         */
        final static Marshaller INSTANCE = new IdentityMarshaller();
        
    }
    
}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Implementation Mapper
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
#if JAVA_8
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;
#else
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
import jakarta.resource.cci.Record;
#endif

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.w3c.jpa3.AbstractObject;


/**
 * Implementation Mapper
 */
class ImplementationMapping_1 implements Mapping_1_0 {

    /**
     * Constructor
     *
     * @param next the next mapping layer
     * @param aspectImplementationPackageNames a comma separated list of package names
     */
    ImplementationMapping_1(
        Mapping_1_0 next,
        Map<String, String> aspectImplementationPackageNames
    ) {
        this.next = (ImplementationMapping_1) next;
        this.aspectImplementationPackageNames = aspectImplementationPackageNames == null || aspectImplementationPackageNames.isEmpty() ? null : aspectImplementationPackageNames;
        this.specificationMapping = next == null ? new SpecificationMapping_1() : this.next.specificationMapping;
    }

    /**
     * The shared mapping
     */
    private final SpecificationMapping_1 specificationMapping;

    /**
     * The delegate's implementation mapper
     */
    private final ImplementationMapping_1 next;

    /**
     * The implementation descriptors
     */
    private final ConcurrentMap<String, ImplementationDescriptor> implementationDescriptors = new ConcurrentHashMap<>();

    /**
     * The aspect descriptors
     */
    private final ConcurrentMap<String, AspectImplementationDescriptor> aspectDescriptors = new ConcurrentHashMap<>();

    /**
     * Map which contains &lt;&lsaquo;model-package-name&rsaquo;, &lsaquo;java-package-name&rsaquo;&gt;
     * entries, defining where classes implementing modeled features or JDO callback interfaces are
     * located.
     */
    private final Map<String, String> aspectImplementationPackageNames;

    @Override
    public RefPackage newPackage(
        Jmi1Package_1_0 outermostPackage,
        String qualifiedName
    ) throws ServiceException {
        SpecificationMapping_1.PackageDescriptor descriptor = this.specificationMapping.getPackageDescriptor(qualifiedName);
        return Classes.<RefPackage>newProxyInstance(
            new Jmi1PackageInvocationHandler(
                descriptor.qualifiedPackageName,
                outermostPackage,
                outermostPackage
            ),
            descriptor.jmi1Interface,
            Jmi1Package_1_0.class
        );
    }

    /**
     * Re-package the record
     *
     * @param keys the mapped record's keys
     * @param values the record's values in the same order as the keys
     * @return a mapped record
     */
    @SuppressWarnings("unchecked")
    private static MappedRecord asMappedRecord(
        String[] keys,
        IndexedRecord values
    ) {
        return Records.getRecordFactory().asMappedRecord(
            values.getRecordName(),
            null,
            keys,
            values.toArray(new Object[keys.length])
        );
    }

    @Override
    public RefStruct newStruct(
        Jmi1Package_1_0 outermostPackage,
        Record delegate
    ) throws ServiceException {
        SpecificationMapping_1.StructureDescriptor descriptor = this.specificationMapping.getStructureDescriptor(
            delegate.getRecordName()
        );
        if (delegate instanceof MappedRecord) {
            return Classes.<RefStruct>newProxyInstance(
                new Jmi1StructInvocationHandler(
                    outermostPackage,
                    (MappedRecord) delegate
                ),
                descriptor.jmi1Interface
            );
        } else if (delegate instanceof IndexedRecord) {
            return Classes.<RefStruct>newProxyInstance(
                new Jmi1StructInvocationHandler(
                    outermostPackage,
                    ImplementationMapping_1.asMappedRecord(
                        descriptor.members,
                        (IndexedRecord) delegate
                    )
                ),
                descriptor.jmi1Interface
            );
        } else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "Invalid struct record",
            new BasicException.Parameter("supported", MappedRecord.class.getName(), IndexedRecord.class.getName()),
            new BasicException.Parameter("actual", delegate.getClass().getName())
        );
    }

    @Override
    public FeatureMapper getFeatureMapper(
        String qualifiedClassName,
        FeatureMapper.Type type
    ) throws ServiceException {
        return this.specificationMapping.getSpecificationDescriptor(
            qualifiedClassName
        ).getFeatureMapper(
            type
        );
    }

    @Override
    public String getModelClassName(
        Class<?> javaClass
    ) throws ServiceException {
        return this.specificationMapping.getSpecificationDescriptor(javaClass).qualifiedClassName;
    }

    @Override
    public Class<? extends RefObject> getInstanceInterface(
        Class<?> javaClass
    ) throws ServiceException {
        return this.specificationMapping.getSpecificationDescriptor(javaClass).jmi1Interface;
    }

    @Override
    public ImplementationDescriptor getClassMapping(
        String qualifiedClassName
    ) throws ServiceException {
        String key = qualifiedClassName.intern();
        ImplementationDescriptor implementationDescriptor = this.implementationDescriptors.get(key);
        if (implementationDescriptor == null) {
            List<AspectImplementationDescriptor> aspectImplementationClasses;
            List<Class<?>> mixedInInterfaces = this.next == null ? Collections.<Class<?>>emptyList(
            ) : Arrays.asList(
                this.next.getClassMapping(qualifiedClassName).mixedInInterfaces
            );
            if (this.aspectImplementationPackageNames == null) {
                aspectImplementationClasses = Collections.emptyList();
            } else {
                //
                // Aspect Implementation Classes
                //
                aspectImplementationClasses = new ArrayList<>();
                for (Object supertypePath : Model_1Factory.getModel().getElement(qualifiedClassName).objGetList("allSupertype")) {
                    String qualifiedNameOfSupertype = ((Path) supertypePath).getLastSegment().toClassicRepresentation();
                    getAspectDescriptor(qualifiedNameOfSupertype).ifPresent(
                        descriptor -> ImplementationDescriptor.add(descriptor, aspectImplementationClasses)
                    );
                }
                //
                // Mixed-in Interfaces
                //
                mixedInInterfaces = new ArrayList<>(mixedInInterfaces);
                for (AspectImplementationDescriptor aspectImplementationClass : aspectImplementationClasses) {
                    for (Class<?> mixedInInterface : Classes.getInterfaces(aspectImplementationClass.implementationClass)) {
                        ImplementationDescriptor.add(mixedInInterface, mixedInInterfaces);
                    }
                }
            }
            //
            // Combined interfaces
            //
            Set<Class<?>> combinedInterfaces = new LinkedHashSet<>();
            // CCI
            SpecificationMapping_1.SpecificationDescriptor interfaceDescriptor = this.specificationMapping.getSpecificationDescriptor(qualifiedClassName);
            combinedInterfaces.add(interfaceDescriptor.jmi1Interface);
            combinedInterfaces.add(PersistenceCapable.class);
            // SPI
            combinedInterfaces.add(org.openmdx.base.persistence.spi.Cloneable.class);
            if (this.next != null) {
                combinedInterfaces.add(DelegatingRefObject_1_0.class);
            }
            // Mixed-In
            combinedInterfaces.addAll(mixedInInterfaces);
            ImplementationDescriptor concurrent = this.implementationDescriptors.put(
                key,
                implementationDescriptor = new ImplementationDescriptor(
                    interfaceDescriptor,
                    aspectImplementationClasses,
                    mixedInInterfaces,
                    combinedInterfaces
                )
            );
            return concurrent == null ? implementationDescriptor : concurrent;
        } else {
            return implementationDescriptor;
        }
    }

    /**
     * Find a class' aspect descriptor
     *
     * @param qualifiedClassName a fully qualified class name
     * @return the class' aspect descriptor
     */
    private Optional<AspectImplementationDescriptor> getAspectDescriptor(
        String qualifiedClassName
    ) throws ServiceException {
        try {
            final AspectImplementationDescriptor aspectDescriptor = this.aspectDescriptors.computeIfAbsent(qualifiedClassName, this::determineAspectDescriptor);
            return aspectDescriptor == AspectImplementationDescriptor.NULL ? Optional.empty() : Optional.of(aspectDescriptor);
        } catch (RuntimeServiceException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * Provides an {@code AspectImplementationDescriptor.NULL} if an implementation exists
     * and returns {@code AspectImplementationDescriptor.NULL} otherwise.
     *
     * @return an {@code AspectImplementationDescriptor} or else {@code AspectImplementationDescriptor.NULL}
     */
    private AspectImplementationDescriptor determineAspectDescriptor(
        String qualifiedClassName
    ) {
        if (this.aspectImplementationPackageNames == null) {
            return AspectImplementationDescriptor.NULL;
        }
        final String modelPackageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(":"));
        return determineAspectDescriptor(
            this.aspectImplementationPackageNames.get(modelPackageName),
            getSpecificationDescriptor(qualifiedClassName)
        );
    }

    private SpecificationMapping_1.SpecificationDescriptor getSpecificationDescriptor(
        String qualifiedClassName
    ) {
        try {
            return this.specificationMapping.getSpecificationDescriptor(qualifiedClassName);
        } catch (ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    /**
     * Provides an {@code AspectImplementationDescriptor.NULL} if an implementation exists
     * and returns {@code AspectImplementationDescriptor.NULL} otherwise.
     *
     * @param aspectImplementationPackageNames a comma separated list of Java implementation
     *                                         package names for the given model package
     * @return an {@code AspectImplementationDescriptor} or else {@code AspectImplementationDescriptor.NULL}
     */
    private AspectImplementationDescriptor determineAspectDescriptor(
        String aspectImplementationPackageNames,
        SpecificationMapping_1.SpecificationDescriptor interfaceDescriptor
    ) {
        if (interfaceDescriptor == null || aspectImplementationPackageNames == null || aspectImplementationPackageNames.isEmpty()) {
            return AspectImplementationDescriptor.NULL;
        }
        for (String aspectImplementationPackageName : aspectImplementationPackageNames.split(",")) {
            try {
                Class<?> implementationClass = Classes.getApplicationClass(
                    aspectImplementationPackageName + '.' + interfaceDescriptor.simpleClassName + "Impl"
                );
                Constructor<?> aspectImplementationConstructor;
                try {
                    aspectImplementationConstructor = implementationClass.getConstructor(
                        interfaceDescriptor.jmi1Interface,
                        interfaceDescriptor.cci2Interface
                    );
                } catch (NoSuchMethodException ignorable) {
                    try {
                        aspectImplementationConstructor = implementationClass.getConstructor(
                            interfaceDescriptor.cci2Interface,
                            interfaceDescriptor.cci2Interface
                        );
                    } catch (NoSuchMethodException exception) {
                        throw new ServiceException(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.VALIDATION_FAILURE,
                            "Invalid aspect implementation class, missing expected and fallback constructor",
                            new BasicException.Parameter("expected", interfaceDescriptor.simpleClassName + '(' + interfaceDescriptor.jmi1Interface.getName() + ',' + interfaceDescriptor.cci2Interface.getName() + ')'),
                            new BasicException.Parameter("fallback", interfaceDescriptor.simpleClassName + '(' + interfaceDescriptor.cci2Interface.getName() + ',' + interfaceDescriptor.cci2Interface.getName() + ')')
                        ).log();
                    }
                }
                return new AspectImplementationDescriptor(
                    interfaceDescriptor.jmi1Interface,
                    implementationClass,
                    aspectImplementationConstructor
                );
            } catch (Exception ignore) {
                // Safely try next package
            }
        }
        return AspectImplementationDescriptor.NULL;
    }

    @Override
    public Constructor<? extends RefException> getExceptionConstructor(
        String qualifiedExceptionName,
        String qualifiedPackageName
    ) {
        return this.specificationMapping.getExceptionConstructor(
            qualifiedExceptionName,
            qualifiedPackageName
        );
    }


    //------------------------------------------------------------------------
    // Class Descriptor
    //------------------------------------------------------------------------

    /**
     * Descriptor
     */
    private static class ImplementationDescriptor implements ClassMapping_1_0 {

        /**
         * Constructor
         */
        ImplementationDescriptor(
            SpecificationMapping_1.SpecificationDescriptor interfaceDescriptor,
            List<AspectImplementationDescriptor> aspectDescriptors,
            List<Class<?>> mixedInInterfaces,
            Set<Class<?>> combinedInterfaces
        ) {
            this.interfaceDescriptor = interfaceDescriptor;
            this.aspectImplementationDescriptors = aspectDescriptors.toArray(new AspectImplementationDescriptor[aspectDescriptors.size()]);
            this.mixedInInterfaces = mixedInInterfaces.toArray(new Class<?>[mixedInInterfaces.size()]);
            this.combinedInterfaces = combinedInterfaces.toArray(new Class<?>[combinedInterfaces.size()]);
            this.queryInterfaces = new Class[]{RefQuery_1_0.class, interfaceDescriptor.queryInterface};
        }

        /**
         * The qualified class name
         */
        private final SpecificationMapping_1.SpecificationDescriptor interfaceDescriptor;

        /**
         * Aspect implementation descriptors
         */
        private final AspectImplementationDescriptor[] aspectImplementationDescriptors;

        final Class<?>[] mixedInInterfaces;

        final Class<?>[] combinedInterfaces;

        private final Class<?>[] queryInterfaces;

        private Class<?>[] classInterfaces;

        private final ConcurrentMap<Method, InvocationDescriptor> invocationDescriptors = new ConcurrentHashMap<>();

        @Override
        public AspectImplementationDescriptor[] getAspectImplementationDescriptors(
        ) {
            return this.aspectImplementationDescriptors;
        }

        @Override
        public Class<? extends RefObject> getInstanceInterface() {
            return this.interfaceDescriptor.jmi1Interface;
        }

        @Override
        public boolean isMixedInInterfaces(
            Class<?> declaringClass
        ) {
            for (Class<?> mixedInInterface : this.mixedInInterfaces) {
                if (declaringClass.isAssignableFrom(mixedInInterface)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Retrieve the class interfaces lazily
         *
         * @return the class interfaces
         * @throws ServiceException in case of failure
         */
        private Class<?>[] getClassInterfaces(
        ) throws ServiceException {
            if (this.classInterfaces == null) {
                this.classInterfaces = new Class<?>[]{
                    this.interfaceDescriptor.getClassInterface(),
                    Jmi1Class_1_0.class
                };
            }
            return this.classInterfaces;
        }

        @Override
        public Class<? extends AbstractObject> getInstanceClass(
        ) {
            return this.interfaceDescriptor.jpa3Class;
        }

        @Override
        public Jmi1Class_1_0 newClass(
            Jmi1Package_1_0 refPackage
        ) throws ServiceException {
            return Classes.<Jmi1Class_1_0>newProxyInstance(
                new Jmi1ClassInvocationHandler(
                    this.interfaceDescriptor.qualifiedClassName,
                    refPackage
                ),
                this.getClassInterfaces()
            );
        }

        @Override
        public RefObject_1_0 newInstance(
            Jmi1Class_1_0 refClass,
            PersistenceCapable delegate
        ) {
            return Classes.<RefObject_1_0>newProxyInstance(
                new Jmi1ObjectInvocationHandler(refClass, delegate, this),
                this.combinedInterfaces
            );
        }

        @Override
        public RefQuery_1_0 newQuery(
            InvocationHandler invocationHandler
        ) {
            return Classes.<RefQuery_1_0>newProxyInstance(
                invocationHandler,
                this.queryInterfaces
            );
        }

        /**
         * Canonical add operation
         *
         * @param newClass another class
         * @param to the target
         */
        static void add(
            Class<?> newClass,
            List<Class<?>> to
        ) {
            boolean replaced = false;
            for (
                ListIterator<Class<?>> i = to.listIterator();
                i.hasNext();
            ) {
                Class<?> oldClass = i.next();
                if (newClass.isAssignableFrom(oldClass)) {
                    return;
                }
                if (oldClass.isAssignableFrom(newClass)) {
                    if (replaced) {
                        i.remove();
                    } else {
                        replaced = true;
                        i.set(oldClass);
                    }
                }
            }
            if (!replaced) {
                to.add(newClass);
            }
        }

        /**
         * Canonical add operation
         *
         * @param newDescriptor another descriptor
         * @param to the target
         */
        static void add(
            AspectImplementationDescriptor newDescriptor,
            List<AspectImplementationDescriptor> to
        ) {
            boolean replaced = false;
            for (
                ListIterator<AspectImplementationDescriptor> i = to.listIterator();
                i.hasNext();
            ) {
                AspectImplementationDescriptor oldDescriptor = i.next();
                if (newDescriptor.implementationClass.isAssignableFrom(oldDescriptor.implementationClass)) {
                    return;
                }
                if (oldDescriptor.implementationClass.isAssignableFrom(newDescriptor.implementationClass)) {
                    if (replaced) {
                        i.remove();
                    } else {
                        replaced = true;
                        i.set(newDescriptor);
                    }
                }
            }
            if (!replaced) {
                to.add(newDescriptor);
            }
        }

        @Override
        public InvocationDescriptor getInvocationDescriptor(
            Method method
        ) {
            InvocationDescriptor invocationDescriptor = this.invocationDescriptors.get(method);
            if (invocationDescriptor == null) {
                String declaredName = method.getName();
                Class<?>[] declaredParameterTypes = method.getParameterTypes();
                int declaredParameterCount = declaredParameterTypes.length;
                Class<?> declaringClass = method.getDeclaringClass();
                AspectImplementations:
                for (
                    int index = 0;
                    index < this.aspectImplementationDescriptors.length;
                    index++
                ) {
                    AspectImplementationDescriptor descriptor = this.aspectImplementationDescriptors[index];
                    if (
                        declaringClass.isAssignableFrom(descriptor.implementationClass) ||
                            declaringClass.isAssignableFrom(descriptor.declaringClass)
                    ) {
                        ImplementedMethod:
                        for (Method implementedMethod : Classes.getOrderedMethods(descriptor.implementationClass)) {
                            if (declaredName.equals(implementedMethod.getName())) {
                                Class<?>[] implementedParameterTypes = implementedMethod.getParameterTypes();
                                if (declaredParameterCount == implementedParameterTypes.length) {
                                    for (
                                        int i = 0;
                                        i < declaredParameterCount;
                                        i++
                                    ) {
                                        if (!declaredParameterTypes[i].isAssignableFrom(implementedParameterTypes[i])) {
                                            continue ImplementedMethod;
                                        }
                                    }
                                }
                                invocationDescriptor = new InvocationDescriptor(
                                    index,
                                    implementedMethod
                                );
                                break AspectImplementations;
                            }
                        }
                    }
                }
                this.invocationDescriptors.putIfAbsent(
                    method,
                    invocationDescriptor == null ? InvocationDescriptor.NULL : invocationDescriptor
                );
                return invocationDescriptor;
            } else {
                return invocationDescriptor == InvocationDescriptor.NULL ? null : invocationDescriptor;
            }
        }

    }

}

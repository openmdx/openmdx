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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefException;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;

import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.w3c.jpa3.AbstractObject;


/**
 * Implementation Mapper
 */
class SpecificationMapping_1  {

    /**
     * Constructor 
     */
    SpecificationMapping_1(
    ){
        try {
            this.defaultExceptionConstructor = RefException_1.class.getConstructor(ServiceException.class);
        } catch (Exception exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "Default RefException constructor unavailable",
                new BasicException.Parameter("class", RefException_1.class.getName()),
                new BasicException.Parameter("arguments", ServiceException.class.getName())
            );
        }
    }

    /**
     * Maps classes to descriptors
     */
    private final ConcurrentMap<Class<?>,SpecificationDescriptor> descriptorsForClass = new ConcurrentHashMap<>();

    /**
     * Maps class names to descriptors
     */
    private final ConcurrentMap<String,SpecificationDescriptor> descriptorsForName = new ConcurrentHashMap<>();

    /**
     * Maps package names to package descriptors
     */
    private final ConcurrentMap<String, PackageDescriptor> packageDescriptors = new ConcurrentHashMap<>();

    /**
     * Maps package names to package descriptors
     */
    private final ConcurrentMap<String, StructureDescriptor> structureDescriptors = new ConcurrentHashMap<>();

    /**
     * Maps exception names to exception constructors
     */
    private final ConcurrentMap<String, Constructor<? extends RefException>> exceptionConstructors = new ConcurrentHashMap<>();
    
    /**
     * The {@code RefException_1(ServiceException)} constructor
     */
    private final Constructor<? extends RefException> defaultExceptionConstructor;
    
    /**
     * Retrieve a package descriptor
     * 
     * @param qualifiedName
     * 
     * @return the package descriptor
     * 
     * @throws ServiceException 
     */
    PackageDescriptor getPackageDescriptor(
        String qualifiedName
    ) throws ServiceException{
        PackageDescriptor descriptor = this.packageDescriptors.get(qualifiedName);
        if(descriptor == PackageDescriptor.NULL) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_AVAILABLE,
                "Java interface(s) not available for the given model package",
                new BasicException.Parameter("model-package", qualifiedName)
            );
        } else if (descriptor != null) {
            return descriptor;
        } 
        try {            
            descriptor = new PackageDescriptor(qualifiedName);
            return Maps.putUnlessPresent(
                this.packageDescriptors,
                descriptor.nestedPackageName, 
                descriptor
            );
        } catch (ServiceException exception) {
            this.packageDescriptors.put(
                qualifiedName.intern(),
                PackageDescriptor.NULL
            );
            throw exception;
        }
    }

    /**
     * Retrieve a structure descriptor
     * 
     * @param qualifiedName
     * 
     * @return the structure descriptor
     * 
     * @throws ServiceException 
     */
    StructureDescriptor getStructureDescriptor(
        String qualifiedName
    ) throws ServiceException{
        StructureDescriptor descriptor = this.structureDescriptors.get(qualifiedName);
        if(descriptor == StructureDescriptor.NULL) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_AVAILABLE,
                "Java interface(s) not available for the given struct",
                new BasicException.Parameter("struct", qualifiedName)
            );
        } else if (descriptor != null) {
            return descriptor;
        } 
        try {            
            descriptor = new StructureDescriptor(qualifiedName);
            return Maps.putUnlessPresent(
                this.structureDescriptors,
                descriptor.qualifiedName, 
                descriptor
            );
        } catch (ServiceException exception) {
            this.structureDescriptors.put(
                qualifiedName.intern(),
                StructureDescriptor.NULL
            );
            throw exception;
        }
    }
    
    
    /**
     * Retrieve an interface's descriptor
     * 
     * @param javaClass the cci2, jmi1 or jpa3 class
     * 
     * @return the corresponding specification descriptor
     * 
     * @throws JmiServiceException
     */
    SpecificationDescriptor getSpecificationDescriptor(
        Class<?> javaClass
    ) throws ServiceException {
        final SpecificationDescriptor descriptor = this.descriptorsForClass.get(javaClass);
        if(descriptor != null){
            return descriptor;
        }
        final Model_1_0 model = Model_1Factory.getModel();
        for(ModelElement_1_0 element: model.getContent()) {
            if(model.isClassType(element)) {
                final SpecificationDescriptor candidate = getSpecificationDescriptor(
                    element.getQualifiedName(), 
                    javaClass
                );
                if(candidate != SpecificationDescriptor.NULL) {
                    return candidate;
                }
            }
        }
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            "No model class found for the given interface",
            new BasicException.Parameter("interface",javaClass.getName())
        );
    }

    private SpecificationDescriptor getSpecificationDescriptor(
        String qualifiedName,
        Class<?> javaClass
    ) {
        final SpecificationDescriptor candidate = getRegisteredSpecificationDescriptor(qualifiedName);
        return (
            candidate == SpecificationDescriptor.NULL ||
            candidate.cci2Interface == javaClass ||
            candidate.jmi1Interface == javaClass ||
            candidate.jpa3Class == javaClass 
        ) ? candidate : SpecificationDescriptor.NULL;
    }

    private SpecificationDescriptor getRegisteredSpecificationDescriptor(
        String qualifiedName
    ) {
        final SpecificationDescriptor candidate = this.descriptorsForName.get(
            qualifiedName
        );
        if(candidate != null) {
            return candidate;
        }
        try {
            return newSpecificationDescriptor(qualifiedName);
        } catch (ServiceException exception) {
            this.descriptorsForName.putIfAbsent(
                qualifiedName.intern(), 
                SpecificationDescriptor.NULL
            );
            return SpecificationDescriptor.NULL;
        }
    }
    
    /**
     * Retrieve an interface's descriptor
     * 
     * @param qualifiedName qualified model class name
     * 
     * @return the interface's descriptor
     * 
     * @throws ServiceException  
     */
    SpecificationDescriptor getSpecificationDescriptor(
        String qualifiedName
    ) throws ServiceException {
        SpecificationDescriptor descriptor = this.descriptorsForName.get(qualifiedName);
        if(descriptor == SpecificationDescriptor.NULL) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_AVAILABLE,
                "Java interface(s) not available for the given model class",
                new BasicException.Parameter("model-class", qualifiedName)
            );
        } else if(descriptor != null){
            return descriptor;
        } else if(Model_1Factory.getModel().findElement(qualifiedName) == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.BAD_PARAMETER, 
                "Class not found in model repository",
                new BasicException.Parameter("model-class", qualifiedName)
            );
        } else {
            return newSpecificationDescriptor(qualifiedName);
        } 
    }
    
    /**
     * Create a descriptor
     * 
     * @param qualifiedName the model class name
     * 
     * @return a newly created descriptor
     * 
     * @throws JmiServiceException 
     */
    private SpecificationDescriptor newSpecificationDescriptor(
        String qualifiedName
    ) throws ServiceException {
        SpecificationDescriptor descriptor = new SpecificationDescriptor(qualifiedName);
        SpecificationDescriptor concurrent = this.descriptorsForName.putIfAbsent(
            descriptor.qualifiedClassName,
            descriptor
        );
        if(concurrent == null) {
            registerForClass(
                descriptor,
                descriptor.cci2Interface,
                descriptor.jmi1Interface,
                descriptor.jpa3Class
            );
            return descriptor;
        } else {
            return concurrent;
        }
    }

    private void registerForClass(
        SpecificationDescriptor descriptor,
        Class<?>... keys
    ){
        for(Class<?> key : keys) {
            if(key != null) {
                Maps.putUnlessPresent(
                    this.descriptorsForClass,
                    key,
                    descriptor
                );
            }
        }
    }
        
    /**
     * Retrieve the exception constructor
     * 
     * @param qualifiedExceptionName the exception's id
     * @param qualifiedPackageName the exception's name space
     * 
     * @return the exception constructor
     */
    Constructor<? extends RefException> getExceptionConstructor(
        String qualifiedExceptionName,
        String qualifiedPackageName
    ){
        Constructor<? extends RefException> constructor = this.exceptionConstructors.get(qualifiedExceptionName);
        if(constructor == null) {
            String javaClass = Names.toPackageName(
                qualifiedPackageName, 
                Names.JMI1_PACKAGE_SUFFIX
            ) + '.' + Identifier.CLASS_PROXY_NAME.toIdentifier(
                qualifiedExceptionName.substring(qualifiedExceptionName.lastIndexOf(':') + 1)
            );
            try {
                constructor = Classes.<RefException>getApplicationClass(javaClass).getConstructor(ServiceException.class);
            } catch (Exception exception) {
                constructor = this.defaultExceptionConstructor;
            }
            return Maps.putUnlessPresent(
                this.exceptionConstructors,
                qualifiedExceptionName.intern(),
                constructor
            );
        } else {
            return constructor;
        }
    }
    

    //------------------------------------------------------------------------
    // Class StructureDescriptor
    //------------------------------------------------------------------------

    /**
     * Structure Descriptor
     */
    static class StructureDescriptor {

        /**
         * Constructor 
         */
        private StructureDescriptor(
        ){
            this.qualifiedName = null;
            this.jmi1Interface = null;
            this.members = null;
        }
        
        StructureDescriptor(
            String qualifiedName
        ) throws ServiceException {
            this.qualifiedName = qualifiedName.intern();
            String jmi1InterfaceName = Names.toClassName(
                this.qualifiedName,
                Names.JMI1_PACKAGE_SUFFIX
            );
            String cci2MembersName = Names.toClassName(
                this.qualifiedName,
                Names.CCI2_PACKAGE_SUFFIX
            ) + "$Member";
            try {
                this.jmi1Interface = Classes.getApplicationClass(jmi1InterfaceName);
                Enum<?>[] members = Classes.<Enum<?>>getApplicationClass(cci2MembersName).getEnumConstants();
                this.members = new String[members.length];
                for(Enum<?> e : members){
                    this.members[e.ordinal()] = e.name();
                }
            } catch (ClassNotFoundException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_AVAILABLE,
                    "Java class not available for the given struct",
                    new BasicException.Parameter("model-class", qualifiedName),
                    new BasicException.Parameter("java-class", jmi1InterfaceName)
                );
            }
        }

        final static StructureDescriptor NULL = new StructureDescriptor();
        
        final String qualifiedName;

        final Class<? extends RefStruct> jmi1Interface;

        final String[] members;
        
    }
    
    
    //------------------------------------------------------------------------
    // Class PackageDescriptor
    //------------------------------------------------------------------------

    /**
     * Package Descriptor
     */
    static class PackageDescriptor {

        /**
         * Constructor 
         */
        private PackageDescriptor(){
            this.simplePackageName = null;
            this.nestedPackageName = null;
            this.qualifiedPackageName = null;
            this.jmi1Interface = null;
        }

        /**
         * Constructor 
         * 
         * @param nestedPackageName
         * 
         * @throws ServiceException  
         */
        PackageDescriptor(
            String nestedPackageName
        ) throws ServiceException {
            int i = nestedPackageName.lastIndexOf(':');
            this.nestedPackageName = nestedPackageName.intern();
            this.simplePackageName = nestedPackageName.substring(i + 1).intern();
            this.qualifiedPackageName = (this.nestedPackageName + ":" + this.simplePackageName).intern();
            String packageClassName = Names.toClassName(
                this.qualifiedPackageName, 
                Names.JMI1_PACKAGE_SUFFIX
            ) + "Package";
            try {
                this.jmi1Interface = Classes.getApplicationClass(packageClassName);
            } catch (ClassNotFoundException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_AVAILABLE,
                    "Java interface(s) not available for the given model class",
                    new BasicException.Parameter("model-class", nestedPackageName),
                    new BasicException.Parameter("java-class", packageClassName)
                );
            }
        }
        
        final static PackageDescriptor NULL = new PackageDescriptor();
        
        final String simplePackageName;
        
        final String nestedPackageName;

        final String qualifiedPackageName;
        
        final Class<? extends RefPackage> jmi1Interface;

    }
    
    //------------------------------------------------------------------------
    // Class Descriptor
    //------------------------------------------------------------------------

    /**
     * Interface Descriptor
     */
    static class SpecificationDescriptor {

        /**
         * Constructor 
         */
        private SpecificationDescriptor(){
            this.simpleClassName = null;
            this.qualifiedClassName = null;
            this.cci2Interface = null;
            this.jmi1Interface = null;
            this.queryInterface = null;
            this.jpa3Class = null;
        }
        
        /**
         * Constructor 
         *
         * @param qualifiedClassName
         * 
         * @throws JmiServiceException
         */
        SpecificationDescriptor(
            String qualifiedClassName
        ) throws ServiceException {
            this.qualifiedClassName = qualifiedClassName.intern();
            final String cci2InterfaceName = Names.toClassName(this.qualifiedClassName, Names.CCI2_PACKAGE_SUFFIX);
            final String jmi1InterfaceName = Names.toClassName(this.qualifiedClassName, Names.JMI1_PACKAGE_SUFFIX);
            final String queryClassName = cci2InterfaceName + "Query";
            try {
                this.jmi1Interface = Classes.getApplicationClass(jmi1InterfaceName);
                this.cci2Interface = Classes.getApplicationClass(cci2InterfaceName);
				this.queryInterface = Classes.getApplicationClass(queryClassName);
                this.simpleClassName = this.cci2Interface.getSimpleName().intern();
            } catch (ClassNotFoundException exception) {
                throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_AVAILABLE,
                    "Java interface(s) not available for the given model class",
                    new BasicException.Parameter("model-class", qualifiedClassName),
                    new BasicException.Parameter("cci2-class", cci2InterfaceName),
                    new BasicException.Parameter("jmi1-class", jmi1InterfaceName),
                    new BasicException.Parameter("query-class", queryClassName)
                );
            }
            this.jpa3Class = Detaching.isEnabled() ? getDetachedClass(this.qualifiedClassName) : null;
        }

        final static SpecificationDescriptor NULL = new SpecificationDescriptor();
        
        final String simpleClassName;
        
        final String qualifiedClassName;
        
        final Class<?> cci2Interface;
        
        final Class<? extends RefObject> jmi1Interface;

        final Class<?> queryInterface;
        
        final Class<? extends AbstractObject> jpa3Class;

        private FeatureMapper terminalFeatureMapper;
        
        private FeatureMapper nonTerminalFeatureMapper;

        private FeatureMapper queryFeatureMapper;
        
        private Class<? extends RefClass> classInterface;
        
		/**
		 * Determine the JPA3 class
		 * 
         * @param qualifiedClassName the qualified class name
		 * 
		 * @return the JPA3 class or {@code null}
		 */
		private static Class<? extends AbstractObject> getDetachedClass(
			String qualifiedClassName
		){
            final String jpa3ClassName = Names.toClassName(
            	qualifiedClassName, 
            	Names.JPA3_PACKAGE_SUFFIX
            );
            try {
            	return Classes.getApplicationClass(jpa3ClassName);
            } catch (ClassNotFoundException exception) {
            	return null;
            }
		}

        /**
         * Retrieve the class interface lazily
         * 
         * @return the class interface
         * 
         * @throws ServiceException
         */
        Class<? extends RefClass> getClassInterface(
        ) throws ServiceException{
            if(this.classInterface == null) {
                String className = this.jmi1Interface.getName() + "Class"; 
                try {
                    this.classInterface = Classes.getApplicationClass(className);
                } catch (ClassNotFoundException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_AVAILABLE,
                        "Java interface(s) not available for the given model class",
                        new BasicException.Parameter("model-class", this.qualifiedClassName),
                        new BasicException.Parameter("java-class", className)
                    );
                }
            }
            return this.classInterface;
        }

        /**
         * Retrieve the ClassDef
         * 
         * @return the ClassDef
         * 
         * @throws ServiceException 
         */
        ModelElement_1_0 getClassDef(
        ) throws ServiceException{
            return Model_1Factory.getModel().getElement(this.qualifiedClassName);
        }
        
        /**
         * Retrieve the feature mapper lazily
         * 
         * @param type the feature mapper type
         * 
         * @return the requested feature mapper
         * 
         * @throws ServiceException in case of failure
         */
        FeatureMapper getFeatureMapper(
            FeatureMapper.Type type
        ) throws ServiceException {
            switch(type) {
                case TEMRINAL:
                    if(this.terminalFeatureMapper == null) {
                        this.terminalFeatureMapper = new FeatureMapper(
                            getClassDef(),
                            Jmi1Class_1_0.class
                        );
                    }
                    return this.terminalFeatureMapper;
                case NON_TERMINAL:
                    if(this.nonTerminalFeatureMapper == null) {
                        this.nonTerminalFeatureMapper = new FeatureMapper(
                            getClassDef(),
                            this.jmi1Interface
                        );
                    }
                    return this.nonTerminalFeatureMapper;
                case QUERY:
                    if(this.queryFeatureMapper == null) {
                        this.queryFeatureMapper = new FeatureMapper(
                            getClassDef(),
                            this.queryInterface
                        );
                    }
                    return this.queryFeatureMapper;
                default:
                    return null;
            }
            
        }

    }
    
}

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefPackage_1.java,v 1.82 2009/06/09 12:45:17 hburger Exp $
 * Description: RefPackage_1 class
 * Revision:    $Revision: 1.82 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/09 12:45:17 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.spi;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.spi.PersistenceCapable;
import javax.jmi.reflect.RefAssociation;
import javax.jmi.reflect.RefClass;
import javax.jmi.reflect.RefEnum;
import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.oasisopen.jmi1.RefContainer;
import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.PersistenceHelper;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.resource.Records;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.Container;
import org.w3c.cci2.SparseArray;

//---------------------------------------------------------------------------
/**
 * Implementation of RefPackage 1.x. 
 * <p>
 * This implementation supports lightweight serialization. It contains only
 * members to the immediate and outermost package. Other members are static.
 */
public abstract class RefPackage_1
    implements Jmi1Package_1_0, Serializable 
{

    //-------------------------------------------------------------------------
    public RefPackage_1(
        RefPackage outermostPackage,
        RefPackage immediatePackage
    ) {
        this.outermostPackage = (RefRootPackage_1)outermostPackage;
        this.immediatePackage = immediatePackage;
    }


    //-------------------------------------------------------------------------
    // Implements RefPackage_1_0
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_0#refDelegate()
     */
    public PersistenceManager refDelegate() {
        return this.outermostPackage.refDelegate();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_0#getRefPackage(javax.resource.cci.InteractionSpec)
     */
    public RefPackage_1_0 refPackage(InteractionSpec viewContext) {
        return this.outermostPackage.refPackage(viewContext);
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_4#refLegacyDelegate()
     */
    public boolean isTerminal() {
        return this.outermostPackage.isTerminal();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_4#refImplPackageName(java.lang.String)
     */
    public String refImplPackageName(String packageName) {
        return this.outermostPackage.refImplPackageName(packageName);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_4#refCreateImpl(java.lang.String, java.lang.Object, java.lang.Object)
     */
    public Object refCreateImpl(
        String qualifiedClassName,
        Object self,
        Object next
    ) {
        return this.outermostPackage.refCreateImpl(
            qualifiedClassName,
            self,
            next
        );
    }

    
    //-------------------------------------------------------------------------
    // Implements RefPackage_1_3
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_3#close()
     */
    public void close() {
        this.outermostPackage.close();
    }

    //-------------------------------------------------------------------------
    public Object refCreateImpl(
        String qualifiedClassName, 
        RefObject_1_0 refDelegate
    ) {
        return this.outermostPackage.refCreateImpl(
            qualifiedClassName, 
            refDelegate
        );
    }  

    //-------------------------------------------------------------------------
    public String refBindingPackageSuffix(
    ) {
        return this.outermostPackage.refBindingPackageSuffix();
    }

    //-------------------------------------------------------------------------
    /**
     * @return config option userContext
     * 
     * @deprecated
     */
    public Object refUserContext(
    ) {
        return this.refPersistenceManager().getUserObject();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_3#refPersistenceManagerFactory()
     */
    public PersistenceManagerFactory refPersistenceManagerFactory() {
        return this.outermostPackage.refPersistenceManagerFactory();
    }


    //-------------------------------------------------------------------------
    // Implements RefPackage_1_2
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_2#refContainer(org.openmdx.compatibility.base.naming.Path)
     */
    public RefContainer refContainer(
        Path resourceIdentifier,
        Class<Container<RefObject>> containerClass
    ) {
        return this.outermostPackage.refContainer(resourceIdentifier,containerClass);
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_2#refViewContext()
     */
    public InteractionSpec refInteractionSpec() {
        return this.outermostPackage.refInteractionSpec();
    }




    //-------------------------------------------------------------------------
    // Implements RefPackage_1_1
    //-------------------------------------------------------------------------

    /**
     * Retrieves the JDO Persistence Manager delegating to this package.
     * 
     * @return the JDO Persistence Manager delegating to this package.
     */
    public PersistenceManager_1_0 refPersistenceManager(
    ) {
        return this.outermostPackage.refPersistenceManager();
    }

    //-------------------------------------------------------------------------
    // Implements RefPackage_1_0
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public Model_1_0 refModel(
    ) {
        return this.outermostPackage.refModel();
    }

    //-------------------------------------------------------------------------
    public RefObject refObject(
        String refMofId
    ) {
        return this.outermostPackage.refObject(refMofId);
    }

    //-------------------------------------------------------------------------
    public RefObject refObject(
        Path objectId
    ) {
        return this.outermostPackage.refObject(objectId);
    }

    //-------------------------------------------------------------------------
    /**
     * @deprecated
     */
    public Transaction refUnitOfWork(
    ) {
        return this.outermostPackage.refUnitOfWork();
    }

    //-------------------------------------------------------------------------
    /**
     * @deprecated
     */
    public void refBegin(
    ) {
        try {
            this.refUnitOfWork().begin();
        }
        catch(Exception e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * @deprecated
     */
    public void refCommit(
    ) {
        try {
            this.refUnitOfWork().commit();
        }
        catch(Exception e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * @deprecated
     */
    public void refRollback(
    ) {
        try {
            this.refUnitOfWork().rollback();
        }
        catch(Exception e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public RefStruct refCreateStruct(
        String structName,
        List arg
    ) {
        int simpleNamePosition = structName.lastIndexOf(':') + 1;
        String packagePrefix = structName.substring(0, simpleNamePosition).replace(':', '.');
        String className =  Identifier.CLASS_PROXY_NAME.toIdentifier(structName.substring(simpleNamePosition));
        String qualifiedStructClassName = packagePrefix + refOutermostPackage().refBindingPackageSuffix() + '.' + className;                         
        String qualifiedMemberClassName = packagePrefix + Names.CCI2_PACKAGE_SUFFIX + '.' + className + "$Member";                         
        try {
            Enum<?>[] members = Classes.<Enum<?>>getApplicationClass(qualifiedMemberClassName).getEnumConstants();
            String[] keys = new String[members.length];
            for(Enum<?> e : members){
                keys[e.ordinal()] = e.name();
            }
            Object[] values;
            if(arg == null) {
                values = new Object[keys.length];
            }
            else {
                values = new Object[arg.size()];
                for(
                    ListIterator i = arg.listIterator();
                    i.hasNext();
                ){
                    values[i.nextIndex()] = toStructValue(i.next());
                }
            }
            return (RefStruct) Classes.newProxyInstance(
                new Jmi1StructInvocationHandler(
                    refOutermostPackage(),
                    Records.getRecordFactory().asMappedRecord(
                        structName,
                        null, // short description
                        keys,
                        values
                    )
                ),
                Classes.<Object>getApplicationClass(qualifiedStructClassName)
            );
        } catch (ClassNotFoundException exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Struct construction failure",
                    new BasicException.Parameter("structName", structName),
                    new BasicException.Parameter("structClass", qualifiedStructClassName),
                    new BasicException.Parameter("memberClass", qualifiedMemberClassName),
                    new BasicException.Parameter("values", arg)
                )
            );
        } catch (ResourceException exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Struct construction failure",
                    new BasicException.Parameter("structName", structName),
                    new BasicException.Parameter("structClass", qualifiedStructClassName),
                    new BasicException.Parameter("memberClass", qualifiedMemberClassName),
                    new BasicException.Parameter("values", arg)
                )
            );
        }
    }

    @SuppressWarnings("unchecked")
    private static Object toStructValue(
        Object source
    ) throws ResourceException{
        if(source instanceof List<?>) {
            IndexedRecord target = Records.getRecordFactory().createIndexedRecord(Multiplicities.LIST);
            for(Object value : (List<?>)source){
                target.add(toStructValue(value));
            }
            return target;
        } else if (source instanceof Set<?>) {
            IndexedRecord target = Records.getRecordFactory().createIndexedRecord(Multiplicities.SET);
            for(Object value : (Set<?>)source){
                target.add(toStructValue(value));
            }
            return target;
        } else if (source instanceof SparseArray<?>) {
            MappedRecord target = Records.getRecordFactory().createMappedRecord(Multiplicities.SPARSEARRAY);
            for(Object e : target.entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
                target.put(
                    entry.getKey(),
                    toStructValue(entry.getValue())
                );
            }
            return target;
            
        } else if(source instanceof PersistenceCapable) {
            return PersistenceHelper.getCurrentObjectId(source);
        } else {
            return source;
        }
    }
        
    /**
     * Create a structure proxy based on the record name
     * 
     * @param structName
     * @param delegate
     * 
     * @return the structure proxy based on the record name
     */
    public RefStruct refCreateStruct(
        Record record
    ) {
        if(record instanceof MappedRecord) {
            return refCreateStruct(record.getRecordName(), (MappedRecord)record);
        } else if (record instanceof IndexedRecord) {
            return refCreateStruct(record.getRecordName(), (IndexedRecord)record);
        } else throw new JmiServiceException(
            new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Struct construction failure",
                new BasicException.Parameter("recordClass", record == null ? "<null>" : record.getClass().getName())
            )
        );
    }

    /**
     * Create a structure proxy without accessing the delegate
     * 
     * @param structName
     * @param delegate
     * 
     * @return the structure proxy without accessing the delegate
     */
    public RefStruct refCreateStruct(
        String structName,
        MappedRecord delegate
    ) {
        int simpleNamePosition = structName.lastIndexOf(':') + 1;
        String packagePrefix = structName.substring(0, simpleNamePosition).replace(':', '.');
        String className =  Identifier.CLASS_PROXY_NAME.toIdentifier(structName.substring(simpleNamePosition));
        String qualifiedStructClassName = packagePrefix + refOutermostPackage().refBindingPackageSuffix() + '.' + className;                         
        try {
            return (RefStruct) Classes.newProxyInstance(
                new Jmi1StructInvocationHandler(
                    refOutermostPackage(),
                    delegate
                ),
                Classes.<Object>getApplicationClass(qualifiedStructClassName)
            );
        } catch (ClassNotFoundException exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Struct construction failure",
                    new BasicException.Parameter("structName", structName)
                )
            );
        }
    }
    
    //-------------------------------------------------------------------------
    public RefFilter_1_0 refCreateFilter(
        String filterClassName,
        org.openmdx.base.query.Filter filter
    ) {
        List<FilterProperty> filterProperties = new ArrayList<FilterProperty>();
        for(Condition condition: filter.getCondition()) {
            filterProperties.add(
                new FilterProperty(
                    condition.getQuantor(),
                    condition.getFeature(),
                    FilterOperators.fromString(condition.getName()),
                    condition.getValue()
                )
            );
        }        
        List<AttributeSpecifier> attributeSpecifiers = new ArrayList<AttributeSpecifier>();
        for(OrderSpecifier orderSpecifier: filter.getOrderSpecifier()) {
            attributeSpecifiers.add(
                new AttributeSpecifier(
                    orderSpecifier.getFeature(),
                    0,
                    Integer.MAX_VALUE,
                    orderSpecifier.getOrder()
                )
            );
        }
        return refCreateFilter(
            filterClassName,
            filterProperties.toArray(new FilterProperty[filterProperties.size()]),
            attributeSpecifiers.toArray(new AttributeSpecifier[attributeSpecifiers.size()]),
            null, // delegateFilter
            null, // delegateQuantor
            null // delegateName
        );
    }

    //-------------------------------------------------------------------------
    public RefFilter_1_0 refCreateFilter(
        String filterClassName,
        FilterProperty[] filterProperties,
        AttributeSpecifier[] attributeSpecifiers
    ) {
        return refCreateFilter(
            filterClassName,
            filterProperties,
            attributeSpecifiers,
            null, // delegateFilter
            null, // delegateQuantor
            null // delegateName
        );
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.cci.RefPackage_1_1#refCreateFilter(org.openmdx.base.accessor.jmi.cci.RefPackage_1_0, java.lang.String, org.openmdx.base.accessor.jmi.cci.RefFilter_1_0, short, java.lang.String)
     */
    public RefFilter_1_0 refCreateFilter(
        String filterClassName,
        FilterProperty[] filterProperties,
        AttributeSpecifier[] attributeSpecifiers,
        RefFilter_1_0 delegateFilter, 
        Short delegateQuantor, 
        String delegateName
    ) {
        try {
            Constructor<?> filterConstructor = this.filterConstructors.get(filterClassName);
            String packageName = filterClassName.substring(0, filterClassName.lastIndexOf(':'));
            String className = filterClassName.substring(filterClassName.lastIndexOf(':') + 1);
            if(filterConstructor == null) {
                filterConstructor = Jmi1PredicateInvocationHandler.class.getConstructor(
                    RefPackage_1_0.class,
                    String.class,
                    FilterProperty[].class,
                    AttributeSpecifier[].class,
                    RefFilter_1_0.class,
                    Short.class,
                    String.class
                );
                this.filterConstructors.put(
                    filterClassName,
                    filterConstructor
                );
            }
            Object filter = filterConstructor.newInstance(
                filterConstructor.getParameterTypes().length == 6 
                ? new Object[]{
                    this,
                    filterProperties,
                    attributeSpecifiers,
                    delegateFilter, 
                    delegateQuantor, 
                    delegateName
                }
                : new Object[]{
                        this,
                        className.endsWith("Query")
                        ? filterClassName.substring(0, filterClassName.lastIndexOf("Query"))
                            : filterClassName,
                            filterProperties,
                            attributeSpecifiers,
                            delegateFilter, 
                            delegateQuantor, 
                            delegateName                            
                    }
            );
            if(filter instanceof InvocationHandler) {
                String cciClassName = Identifier.CLASS_PROXY_NAME.toIdentifier(className);
                return (RefFilter_1_0) Classes.newProxyInstance(
                    (InvocationHandler)filter,
                    RefFilter_1_0.class,
                    Query.class,
                    Classes.getApplicationClass(packageName.replace(':', '.') + "." + Names.CCI2_PACKAGE_SUFFIX + "." + (cciClassName.endsWith("Query") ? cciClassName : cciClassName + "Query"))
                    
                );
            }
            else {
                return (RefFilter_1_0)filter;
            }
        }
        catch(InvocationTargetException e) {
            if(e.getTargetException() instanceof Exception) {
                throw new JmiServiceException(
                    new ServiceException(
                        (Exception)e.getTargetException()
                    )
                );
            }
            else {
                throw new JmiServiceException(new ServiceException(e));
            }
        }
        catch(ClassNotFoundException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(IllegalAccessException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(InstantiationException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(NoSuchMethodException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
    }

    //-------------------------------------------------------------------------
    // RefPackage
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public RefObject refMetaObject(
    ) {
        try {
            return new RefMetaObject_1(
                this.refModel().getElement(
                    this.refMofId()
                )
            );
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    public RefPackage refPackage(
        RefObject nestedPackage
    ) {
        return this.outermostPackage.refPackage(nestedPackage);
    }

    //-------------------------------------------------------------------------
    public RefPackage refPackage(
        String nestedPackageName
    ) {
        return this.outermostPackage.refPackage(nestedPackageName);
    }

    //-------------------------------------------------------------------------
    public Collection<?> refAllPackages(
    ) {
        return this.outermostPackage.refAllPackages();
    }

    //-------------------------------------------------------------------------
    public RefClass refClass(
        RefObject type
    ) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    public RefClass refClass(
        String qualifiedClassName
    ) {
        return this.refClass(
            qualifiedClassName,
            this
        );
    }

    //-------------------------------------------------------------------------
    /**
     * @param qualifedClassName qualified name of the returned class.
     * @param immediatePackage passed as package when constructing the class, i.e.
     *        refImmediatePackage() of the returned class is equal to immediatePackage.
     */
    public RefClass refClass(
        String qualifiedClassName,
        RefPackage_1_0 immediatePackage
    ) {
        RefClass refClass = null;
        try {
            String loadedClassName = null;            
            if((refClass = this.classes.get(qualifiedClassName)) == null) {
                // Try to load class from alternate implementation path
                String alternateImplementation = this.outermostPackage.refImplPackageName(this.refMofId());
                if(alternateImplementation != null) {
                    String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf(":") + 1);
                    try {
                        String classNameImpl = alternateImplementation + "." + className + "ClassImpl";
                        Class<?> classClass = Classes.getApplicationClass(
                            loadedClassName = classNameImpl
                        );
                        java.lang.reflect.Constructor<?> instanceConstructor = classClass.getConstructor(
                            RefPackage_1_0.class
                        );
                        refClass = (RefClass)instanceConstructor.newInstance(
                            immediatePackage
                        );
                        this.classes.put(
                            qualifiedClassName,
                            refClass
                        );
                    }
                    // fallback to default location
                    catch(java.lang.ClassNotFoundException e) {
                        // ignore
                    }
                }

                // Load class from standard location (= location of package)
                if(refClass == null) {
                    String packageName = qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(':'));
                    String className = qualifiedClassName.substring(qualifiedClassName.lastIndexOf(':') + 1);
                    String bindingPackageSuffix = this.outermostPackage.refBindingPackageSuffix();
                    String classNameIntf =
                        packageName.replace(':', '.') + "." +
                        bindingPackageSuffix + "." +
                        ("cci".equals(bindingPackageSuffix) ? className : Identifier.CLASS_PROXY_NAME.toIdentifier(className)) + 
                        "Class";
                    if(!Names.JMI1_PACKAGE_SUFFIX.equals(bindingPackageSuffix)) {
                        throw new JmiServiceException(
                            new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_FOUND,
                                "Unsupported binding. Supported are " + Arrays.asList(Names.JMI1_PACKAGE_SUFFIX),
                                new BasicException.Parameter("binding.name", bindingPackageSuffix)
                            )
                        );
                    }
                    refClass = (RefClass) Classes.newProxyInstance(
                        new Jmi1ClassInvocationHandler(
                            qualifiedClassName,
                            immediatePackage
                        ),
                        Classes.getApplicationClass(loadedClassName = classNameIntf),
                        Jmi1Class_1_0.class
                    );
                    this.classes.put(
                        qualifiedClassName,
                        refClass
                    );
                }
            }
            if(refClass != null) {
                return refClass;
            }
            throw new JmiServiceException(
                new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "unknown class name. Can not get JMI class",
                    new BasicException.Parameter("className", loadedClassName)
                )
            );
        }
        catch(java.lang.IllegalAccessException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(java.lang.InstantiationException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(java.lang.ClassNotFoundException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(java.lang.NoSuchMethodException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
        catch(java.lang.reflect.InvocationTargetException e) {
            throw new JmiServiceException(new ServiceException(e));
        }
    }

    //-------------------------------------------------------------------------
    public Collection<RefClass> refAllClasses(
    ) {
        return this.classes.values();
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not support association classes.
     */
    public RefAssociation refAssociation(
        RefObject association
    ) {
        throw new UnsupportedOperationException("associations not supported");
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not support association classes.
     */
    public RefAssociation refAssociation(
        String associationName
    ) {
        throw new UnsupportedOperationException("assocations not supported");
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not support association classes.
     */
    public Collection<?> refAllAssociations(
    ) {
        return this.outermostPackage.refAllAssociations();
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public RefStruct refCreateStruct(
        RefObject structType,
        List args
    ) {
        try {
            if(
                    structType instanceof ModelElement_1_0 &&
                    this.refModel().isStructureType(structType)
            ) {
                return this.refCreateStruct(
                    (String)((ModelElement_1_0)structType).objGetValue("qualifiedName"),
                    args
                );
            }
            else {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "unsupported structure type. Must be [StructureType]",
                    new BasicException.Parameter("structure type", structType)
                );
            }
        }
        catch(ServiceException e) {
            throw new JmiServiceException(e);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not supporte enums.
     */
    public RefEnum refGetEnum(
        RefObject enumType,
        String literalName
    ) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    //-------------------------------------------------------------------------
    /**
     * This implementation does not supporte enums.
     */
    public RefEnum refGetEnum(
        String enumName,
        String literalName
    ) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    //-------------------------------------------------------------------------
    public void refDelete(
    ) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    // RefBaseObject
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public RefPackage refImmediatePackage(
    ) {
        return this.immediatePackage;
    }

    //-------------------------------------------------------------------------
    public RefRootPackage_1 refOutermostPackage(
    ) {
        return this.outermostPackage;
    }

    //-------------------------------------------------------------------------
    public Collection<?> refVerifyConstraints(
        boolean deepVerify
    ) {
        throw new UnsupportedOperationException();
    }

    //--------------------------------------------------------------------------
    // Implements Serializable
    //--------------------------------------------------------------------------

    /**
     * Save the data of the <tt>Object_1_0</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The objects data
     */
    private synchronized void writeObject(
        java.io.ObjectOutputStream stream
    ) throws java.io.IOException {
        stream.defaultWriteObject();
    }

    /**
     * Reconstitute the <tt>Object_1_0</tt> instance from a stream (that is,
     * deserialize it).
     */
    private synchronized void readObject(
        java.io.ObjectInputStream stream
    ) throws java.io.IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.classes = new HashMap<String,RefClass>();
        this.filterConstructors = new HashMap<String,Constructor<?>>();
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------

    private static final long serialVersionUID = 350730437983426852L;

    /**
     * These members are serialized when serializing a package. However, this
     * is cheap because 'immediatePackage' only contains the members 
     * 'immediatePackage', 'outmostPackage', 'implementationUri' and
     * the 'outmostPackage' the 'accessor'.
     */
    private final RefPackage immediatePackage;
    private final RefRootPackage_1 outermostPackage;

    /**
     * Map containing <qualifiedName, JMI class> and <qualifiedName, JMI structs> 
     * entries, respectively. This map is shared by all JMI packages, i.e. if 
     * any package loads a class it is available for all other packages and 
     * therefore must be loaded only once per classloader. Moreover, these 
     * members do not have to be serialized. 
     */
    protected transient Map<String,RefClass> classes = new HashMap<String,RefClass>();
    protected transient Map<String,Constructor<?>> filterConstructors = new HashMap<String,Constructor<?>>();

}

//--- End of File -----------------------------------------------------------

/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefObjectFactory_1.java,v 1.57 2008/09/18 12:46:43 hburger Exp $
 * Description: RefObjectFactory_1 class
 * Revision:    $Revision: 1.57 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/18 12:46:43 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.cci;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.generic.cci.LargeObject_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Object_1_1;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.accessor.jmi.spi.DateMarshaller;
import org.openmdx.base.accessor.jmi.spi.DateTimeMarshaller;
import org.openmdx.base.accessor.jmi.spi.DurationMarshaller;
import org.openmdx.base.accessor.jmi.spi.IntegerMarshaller;
import org.openmdx.base.accessor.jmi.spi.LongMarshaller;
import org.openmdx.base.accessor.jmi.spi.RefException_1;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.accessor.jmi.spi.RefRootPackage_1;
import org.openmdx.base.accessor.jmi.spi.ShortMarshaller;
import org.openmdx.base.accessor.jmi.spi.StructMarshaller;
import org.openmdx.base.accessor.jmi.spi.URIMarshaller;
import org.openmdx.base.collection.FetchSize;
import org.openmdx.base.collection.FilterableMap;
import org.openmdx.base.collection.MarshallingFilterableMap;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSequentialList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller;
import org.openmdx.compatibility.base.marshalling.CollectionMarshallerAdapter;
import org.openmdx.compatibility.base.marshalling.Marshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.PrimitiveTypes;
import org.w3c.cci2.BinaryLargeObject;

//---------------------------------------------------------------------------
/**
 * ObjectFactory facade delegating to a JmiAccessor_1. 
 */
public class RefObjectFactory_1
extends CachingMarshaller
implements ObjectFactory_1_0 {

//  -------------------------------------------------------------------------
    public RefObjectFactory_1(
        RefRootPackage_1 refRootPackage,
        Set<Path> directAccessPaths
    ) {
        super();
        this.refRootPackage = refRootPackage;
        this.directAccessPaths = directAccessPaths;
    }

    //-------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public RefObjectFactory_1(
        RefRootPackage_1 refRootPackage
    ) {
        this(
            refRootPackage,
            Collections.EMPTY_SET
        );
    }

    /**
     * @serial
     */
    final RefRootPackage_1 refRootPackage;

    /**
     * @serial
     */
    final Set<Path> directAccessPaths;

    //-------------------------------------------------------------------------
    // CachingMarshaller
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    public Object createMarshalledObject(
        Object source
    ) throws ServiceException {
        return source instanceof RefObject_1_0 ?
            new DelegatingObject(
                (RefObject_1_0)source,
                null
            ) :
                source;
    }

    //-------------------------------------------------------------------------
    public Object unmarshal(
        Object source
    ) {
        if(source instanceof DelegatingObject) {
            try {
                ((DelegatingObject)source).getDelegate();
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }
            return ((DelegatingObject)source).refObject;
        }
        else {
            return source;
        }
    }

    
    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_1
    //------------------------------------------------------------------------

    /**
     * This method is deprecated and will throw a NOT_SUPPORTED exception
     * 
     * @deprecated
     * 
     * @exception   ServiceException    NOT_SUPPORTED
     */
    public Object_1_0 createObject(
      String roleClass,
      String roleId,
      Object_1_0 roleCapable
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            ASPECTS_ONLY
        );
    }

    
    //-------------------------------------------------------------------------
    // ObjectFactory_1_0
    //-------------------------------------------------------------------------

    /**
     * This method is deprecated and will throw a NOT_SUPPORTED exception
     * 
     * @deprecated
     * 
     * @exception   ServiceException    NOT_SUPPORTED
     */
    public Object_1_0 createObject(
        String objectClass, 
        Object_1_0 initialValues
    ) throws ServiceException {
        throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.NOT_SUPPORTED,
            ASPECTS_ONLY
        );
    }

    //-------------------------------------------------------------------------
    public void close(
    ) throws ServiceException {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#isClosed()
     */
    public boolean isClosed(
    ){ 
        return this.refRootPackage.refObjectFactory().isClosed();
    }

//  -------------------------------------------------------------------------
    /**
     * Get an object from the object factory.
     * <p>
     * If an object with the given id is already in the cache it is returned,
     * otherwise a new object is returned.
     *
     * @param       accessPath
     *              Access path of object to be retrieved.
     *
     * @return      A persistent object
     */
    private Object_1_0 getObject(
        Path accessPath
    ) throws ServiceException{
        return new DelegatingObject(accessPath);  
    }

    //-------------------------------------------------------------------------
    public Object_1_0 getObject(
        Object accessPath
    ) throws ServiceException {
        return accessPath == null ?
            null :
                getObject(
                    accessPath instanceof Path ? (Path)accessPath : new Path(accessPath.toString())
                );
    }

    //-------------------------------------------------------------------------
    public UnitOfWork_1_0 getUnitOfWork(
    ){
        return this.refRootPackage.refUnitOfWork();
    }

    //-------------------------------------------------------------------------
    public Object_1_0 createObject(
        String objectClass
    ) throws ServiceException {
        return new DelegatingObject(
            (RefObject_1_0)this.refRootPackage.refClass(objectClass).refCreateInstance(null),
            objectClass
        );
    }

    //-------------------------------------------------------------------------
    public Structure_1_0 createStructure(
        String type,
        List<String> fieldNames,
        List<?> fieldValues
    ) throws ServiceException {
        return this.refRootPackage.refObjectFactory().createStructure(
            type,
            fieldNames,
            fieldValues
        );
    }

    //---------------------------------------------------------------------------
    /**
     * Maps the generic Object_1_0 feature accessors to typed JMI methods.  
     */
    class DelegatingObject
    implements Serializable, Object_1_1 {

        private static final long serialVersionUID = 3691040976072423476L;

        //-------------------------------------------------------------------------
        /**
         * @param identity identity of JMI object.
         */
        public DelegatingObject(
            Path identity
        ) throws ServiceException {
            this.identity = identity;
            this.refObject = null;
            this.qualifiedClassName = null;
        }

        //-------------------------------------------------------------------------
        public DelegatingObject(
            RefObject_1_0 delegation,
            String qualifiedClassName
        ) throws ServiceException {
            this.identity = delegation.refGetPath();
            this.refObject = delegation;
            this.qualifiedClassName = qualifiedClassName;
        }

        //------------------------------------------------------------------------
        public String toString(
        ) {
            if(this.refObject != null) {
                return this.refObject.toString();
            }
            else {
                return null;
            }
        }

        //------------------------------------------------------------------------
        Object_1_0 getDelegate(
        ) throws ServiceException {
            if(this.refObject == null) try {
                this.refObject = (RefObject_1_0)RefObjectFactory_1.this.refRootPackage.refObject(
                    this.identity.toString()
                );
                RefObjectFactory_1.this.cache(this.refObject, this);
            } catch(JmiServiceException e) {
                throw new ServiceException(e);
            }
            return this.refObject.refDelegate();
        }

        //-------------------------------------------------------------------------
        private Model_1_0 getModel(
        ) {
            return RefObjectFactory_1.this.refRootPackage.refModel();
        }

        //-------------------------------------------------------------------------
        private ModelElement_1_0 getType(
            ModelElement_1_0 elementDef
        ) throws ServiceException {
            return this.getModel().getDereferencedType(
                elementDef.values("type").get(0)
            );
        }

        //-------------------------------------------------------------------------
        private String toBeanGetterName(
            ModelElement_1_0 attributeDef
        ) throws ServiceException {

            ModelElement_1_0 attributeType = this.getModel().getDereferencedType(attributeDef);
            String name = (String)attributeDef.values("name").get(0);
            boolean isBoolean = "org:w3c:boolean".equals(attributeType.values("qualifiedName").get(0));
            String beanName = Character.toUpperCase(name.charAt(0)) + name.substring(1);

            if(isBoolean) {
                if(name.startsWith("is")) {
                    beanName = name;
                }
                else {
                    beanName = "is" + beanName;
                }
            }
            else {
                beanName = "get" + beanName;
            }
            return beanName;
        }

        //-------------------------------------------------------------------------
        private String toBeanSetterName(
            ModelElement_1_0 attributeDef
        ) throws ServiceException {

            ModelElement_1_0 attributeType = this.getModel().getDereferencedType(attributeDef);
            String name = (String)attributeDef.values("name").get(0);
            boolean isBoolean = "org:w3c:boolean".equals(attributeType.values("qualifiedName").get(0));
            String beanName = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            if(isBoolean) {
                if(name.startsWith("is")) {
                    beanName = "set" + name.substring(2);
                }
                else {
                    beanName = "set" + beanName;
                }
            }
            else {
                beanName = "set" + beanName;
            }  
            return beanName;
        }


        //-------------------------------------------------------------------------
        private Marshaller toRefStructMarshaller(
            String typeName
        ) {
            return new StructMarshaller(
                typeName, 
                (RefPackage_1_0)this.refObject.refClass().refOutermostPackage(), 
                true
            );
        }

        //-------------------------------------------------------------------------
        private Marshaller fromRefStructMarshaller(
            String typeName
        ) {
            return new StructMarshaller(
                typeName, 
                (RefPackage_1_0)this.refObject.refClass().refOutermostPackage(), 
                false
            );
        }

        //------------------------------------------------------------------------
        public Set<String> objDefaultFetchGroup(
        ) throws ServiceException {
            this.getDelegate();
            try {
                return this.refObject.refDefaultFetchGroup();
            }
            catch(JmiServiceException e) {
                throw new ServiceException(
                    e.getExceptionStack()
                );
            }
        }

        //------------------------------------------------------------------------
        /**
         * Get a stream's content
         */
        long getValue(
            String feature,
            Object stream,
            long position
        ) throws ServiceException {
            this.getDelegate();
            // TODO Maybe some dispatcher code should be added here...
            return this.refObject.refGetValue(feature, stream, position);
        }

        /**
         * Get method get<feature>() and invoke it. This way getValue() 
         * dispatches the calls to the implemented class methods. If a method 
         * get&lt;feature&gt; can not be found then refGetValue() is called and an
         * info message is logged.
         */
        @SuppressWarnings({
            "unchecked", "deprecation"
        })
        Object getValue(
            String feature
        ) throws ServiceException {

            this.getDelegate();

            String beanGetterName = null;
            ModelElement_1_0 featureDef = null;
            Model_1_0 model = this.getModel();

            ModelElement_1_0 classDef = model.getElement(this.refObject.refClass().refMofId());
            if(classDef == null) {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.ASSERTION_FAILURE,
                    "class not found",
                    new BasicException.Parameter("class", this.refObject.refClass().refMofId())
                );
            }
            featureDef = model.getFeatureDef(
                classDef,
                feature,
                false
            );
            if(featureDef == null) {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.ASSERTION_FAILURE,
                    "feature not found",
                    new BasicException.Parameter("class", this.refObject.refClass().refMofId()),
                    new BasicException.Parameter("feature", feature)
                );
            }

            Object values = null;
            ModelElement_1_0 featureType = this.getType(featureDef);
            String qualifiedTypeName = (String)featureType.values("qualifiedName").get(0);

            // invoke JMI method
            // get get<feature>() method and invoke it. Try to find
            // feature on current class and all superclasses
            beanGetterName = this.toBeanGetterName(featureDef);
            try { 
                Method getterMethod = this.refObject.getClass().getMethod(
                    beanGetterName,
                    new Class[]{}
                );
                try {
                    values = getterMethod.invoke(
                        this.refObject,
                        EMPTY_OBJECT_ARRAY
                    );
                }
                catch(InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if(t instanceof ServiceException) {
                        throw (ServiceException)t;
                    }
                    else if(t instanceof RefException_1) {
                        throw ((RefException_1)t).refGetServiceException();
                    }
                    else if(t instanceof JmiServiceException) {
                        throw new ServiceException(
                            ((JmiServiceException)t).getExceptionStack()
                        );
                    }
                    else if(t instanceof RuntimeServiceException) {
                        throw new ServiceException(
                            (RuntimeServiceException)t
                        );
                    }
                    throw new ServiceException(e); 
                }
                catch(IllegalAccessException e) {
                    throw new ServiceException(e);
                }
            }

            // not implemented by class --> fallback to generic objGetValue()
            catch(NoSuchMethodException e) {
                values = this.refObject.refGetValue(
                    new RefMetaObject_1(featureDef),
                    null,
                    true
                );
            }

            // optional value
            if(values == null) {
                return values;
            }

            // Container. objects in CONTAINER_TYPE are marshalled and implement Object_1_0
            else if(values instanceof org.openmdx.compatibility.base.collection.Container || values instanceof RefContainer) {
                return values;
            }

            // List
            else if(values instanceof List) {      
                if(PrimitiveTypes.DATETIME.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingList(
                        DateTimeMarshaller.getInstance(false),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.DATE.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingList(
                        DateMarshaller.getInstance(false),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.ANYURI.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingList(
                        URIMarshaller.getInstance(false),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.DURATION.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingList(
                        DurationMarshaller.getInstance(false),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.SHORT.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingList(
                        ShortMarshaller.getInstance(false),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.INTEGER.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingList(
                        IntegerMarshaller.getInstance(false),
                        (List)values
                    );
                }
                else if(PrimitiveTypes.LONG.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingList(
                        LongMarshaller.getInstance(false),
                        (List)values
                    );
                }
                else if(model.isStructureType(featureType)) {
                    return new MarshallingList(
                        this.fromRefStructMarshaller(qualifiedTypeName),
                        (List)values
                    );
                }
                else if(
                        this.getModel().isClassType(featureType) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingList(
                        RefObjectFactory_1.this,
                        (List)values
                    );
                }          
                else {
                    return values;
                }
            }          

            // Set
            else if(values instanceof Set) {
                if(PrimitiveTypes.DATETIME.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSet(
                        DateTimeMarshaller.getInstance(false),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.DATE.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSet(
                        DateMarshaller.getInstance(false),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.ANYURI.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSet(
                        URIMarshaller.getInstance(false),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.DURATION.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSet(
                        DurationMarshaller.getInstance(false),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.SHORT.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSet(
                        ShortMarshaller.getInstance(false),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.INTEGER.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSet(
                        IntegerMarshaller.getInstance(false),
                        (Set)values
                    );
                }
                else if(PrimitiveTypes.LONG.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSet(
                        LongMarshaller.getInstance(false),
                        (Set)values
                    );
                }
                else if(model.isStructureType(featureType)) {
                    return new MarshallingSet(
                        this.fromRefStructMarshaller(qualifiedTypeName),
                        (Set)values
                    );
                }
                else if(
                        this.getModel().isClassType(featureType) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingSet(
                        RefObjectFactory_1.this,
                        (Set)values
                    );
                }          
                else {
                    return values;
                }
            }

            // SparseArray
            else if(values instanceof SortedMap) {
                if(PrimitiveTypes.DATETIME.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSortedMap(
                        DateTimeMarshaller.getInstance(false),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.DATE.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSortedMap(
                        DateMarshaller.getInstance(false),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.ANYURI.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSortedMap(
                        URIMarshaller.getInstance(false),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.DURATION.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSortedMap(
                        DurationMarshaller.getInstance(false),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.SHORT.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSortedMap(
                        ShortMarshaller.getInstance(false),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.INTEGER.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSortedMap(
                        IntegerMarshaller.getInstance(false),
                        (SortedMap)values
                    );
                }
                else if(PrimitiveTypes.LONG.equals(featureType.values("qualifiedName").get(0))) {
                    return new MarshallingSortedMap(
                        LongMarshaller.getInstance(false),
                        (SortedMap)values
                    );
                }
                else if(model.isStructureType(featureType)) {
                    return new MarshallingSortedMap(
                        this.fromRefStructMarshaller(qualifiedTypeName),
                        (SortedMap)values
                    );
                }
                else if(
                        this.getModel().isClassType(featureType) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new MarshallingSortedMap(
                        RefObjectFactory_1.this,
                        (SortedMap)values
                    );
                }          
                else {
                    return values;
                }
            }

            // single-valued          
            else {
                if(PrimitiveTypes.DATETIME.equals(featureType.values("qualifiedName").get(0))) {
                    return DateTimeMarshaller.getInstance(false).marshal(values);
                }
                else if(PrimitiveTypes.DATE.equals(featureType.values("qualifiedName").get(0))) {
                    return DateMarshaller.getInstance(false).marshal(values);
                }
                else if(PrimitiveTypes.ANYURI.equals(featureType.values("qualifiedName").get(0))) {
                    return URIMarshaller.getInstance(false).marshal(values);
                }                
                else if(PrimitiveTypes.DURATION.equals(featureType.values("qualifiedName").get(0))) {
                    return DurationMarshaller.getInstance(false).marshal(values);
                }
                else if(PrimitiveTypes.SHORT.equals(featureType.values("qualifiedName").get(0))) {
                    return ShortMarshaller.getInstance(false).marshal(values);
                }
                else if(PrimitiveTypes.INTEGER.equals(featureType.values("qualifiedName").get(0))) {
                    return IntegerMarshaller.getInstance(false).marshal(values);
                }
                else if(PrimitiveTypes.LONG.equals(featureType.values("qualifiedName").get(0))) {
                    return LongMarshaller.getInstance(false).marshal(values);
                }
                else if(model.isStructureType(featureType)) {
                    return this.fromRefStructMarshaller(qualifiedTypeName).marshal(values);
                }
                else if(
                        this.getModel().isClassType(featureType) ||
                        PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)
                ) {
                    return new DelegatingObject(
                        (RefObject_1_0)values,
                        null
                    );
                }          
                else {
                    return values;
                }
            }
        }

        //------------------------------------------------------------------------
        private Class<?> getNativeTypeOrClass(
            Object feature,
            ModelElement_1_0 featureDef
        ) {
            String typeName = (String)featureDef.values("qualifiedName").get(0);
            if (PrimitiveTypes.BOOLEAN.equals(typeName)) {
                return Boolean.TYPE;
            } else if(PrimitiveTypes.SHORT.equals(typeName)) {
                return Short.TYPE;
            } else if(PrimitiveTypes.INTEGER.equals(typeName)) {
                return Integer.TYPE;
            } else if(PrimitiveTypes.LONG.equals(typeName)) {
                return Long.TYPE;
            }
            else {
                return feature.getClass();
            }
        }

        //------------------------------------------------------------------------
        void setValue(
            String feature,
            Object to
        ) throws ServiceException {

            this.getDelegate();

            String beanSetterName = null;
            ModelElement_1_0 featureDef = null;
            Model_1_0 model = this.getModel();

            featureDef = model.getFeatureDef(
                model.getElement(this.refObject.refClass().refMofId()),
                feature,
                false
            );

            // set<feature>() method and invoke it.
            beanSetterName = this.toBeanSetterName(featureDef);
            try { 
                Method setterMethod = null;
                if(to != null) {
                    setterMethod = this.refObject.getClass().getMethod(
                        beanSetterName,
                        new Class[]{
                            to instanceof InputStream 
                            ? InputStream.class 
                                : to instanceof Reader 
                                ? Reader.class 
                                    : this.getNativeTypeOrClass(to, featureDef)
                        }
                    );
                }
                else {
                    Method[] setters = this.refObject.getClass().getMethods();
                    for(
                            int i = 0; 
                            i < setters.length;
                            i++
                    ) {
                        if(setters[i].getName().equals(beanSetterName)) {
                            setterMethod = setters[i];
                            break;
                        }
                    }
                    if(setterMethod == null) {
                        throw new NoSuchMethodException(beanSetterName);
                    }
                }
                try {
                    setterMethod.invoke(
                        this.refObject,
                        to
                    );
                }
                catch(InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if(t instanceof ServiceException) {
                        throw (ServiceException)t;
                    }
                    else if(t instanceof RefException_1) {
                        throw ((RefException_1)t).refGetServiceException();
                    }
                    else if(t instanceof JmiServiceException) {
                        throw new ServiceException(
                            ((JmiServiceException)t).getExceptionStack()
                        );
                    }
                    else if(t instanceof RuntimeServiceException) {
                        throw new ServiceException(
                            (RuntimeServiceException)t
                        );
                    }
                    throw new ServiceException(e); 
                }
                catch(IllegalAccessException e) {
                    throw new ServiceException(e);
                }
            }

            // not implemented by class --> fallback to generic objSetValue()
            catch(NoSuchMethodException e) {
                this.refObject.refDelegate().objSetValue(
                    feature, 
                    to
                );
            }
        }

        //------------------------------------------------------------------------
        public Object objGetValue(
            String feature
        ) throws ServiceException {
            return this.getValue(
                feature
            );
        }  

        //------------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public List<Object> objGetList(
            String feature
        ) throws ServiceException {
            return (List<Object>)this.getValue(feature);
        }

        //------------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public Set<Object> objGetSet(
            String feature
        ) throws ServiceException {
            return (Set<Object>)this.getValue(feature);
        }

        //------------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public SortedMap<Integer,Object> objGetSparseArray(
            String feature
        ) throws ServiceException {
            return (SortedMap<Integer,Object>)this.getValue(feature);
        }

        //------------------------------------------------------------------------
        /**
         * Get a large object feature
         * <p> 
         * This method returns a new LargeObject.
         *
         * @param       feature
         *              The feature's name.
         *
         * @return      a large object which may be empty but never is null.
         *
         * @exception   ServiceException ILLEGAL_STATE
         *              if the object is deleted
         * @exception   ClassCastException
         *              if the feature's value is not a large object
         * @exception   ServiceException BAD_MEMBER_NAME
         *              if the object has no such feature
         */
        public LargeObject_1_0 objGetLargeObject(
            String feature
        ) throws ServiceException {
            return new LargeObject_1(feature);
        }

        //------------------------------------------------------------------------
        @SuppressWarnings("unchecked")
        public FilterableMap<String, Object_1_0> objGetContainer(
            String feature
        ) throws ServiceException {

            // check whether path is a direct access path. If yes,
            // the parent object of the container does not have to
            // be loaded and marshalled (minimizes attribute retrieval)
            if(this.identity != null) {
                Path containerIdentity = this.identity.getChild(feature);
                for(
                        Iterator<Path> i = RefObjectFactory_1.this.directAccessPaths.iterator();
                        i.hasNext();
                ) {
                    Path path = i.next();
                    if(path.size() >= containerIdentity.size()) {
                        if(containerIdentity.isLike(path.getPrefix(containerIdentity.size()))) {
                            // get container from unmarshalled Object_1_0. No attributes 
                            // (e.g. object class) need to be loaded from parent object 
                            // which minimizes attribute retrievals
                            return new DirectAccessContainer(this.identity, feature);
                        }
                    }
                }
            }

            // get container from refObject
            Object container = this.getValue(feature);
            return container instanceof LegacyContainer ? new StandardContainer(
                RefObjectFactory_1.this,
                (LegacyContainer)container
            ) : new DirectAccessContainer(
                (FilterableMap<String,Object_1_0>)container
            );        
        }

        //------------------------------------------------------------------------
        public void objMove(
            FilterableMap<String,Object_1_0> there,
            String criteria
        ) throws ServiceException {
            Object_1_0 delegate = this.getDelegate();
            if(there instanceof StandardContainer){
                try {
                    ((StandardContainer)there).getDelegate().refAddValue(
                        criteria,
                        this.refObject
                    );
                } catch(JmiServiceException e) {
                    throw new ServiceException(
                        e.getExceptionStack()
                    );
                }
            } else if (there instanceof DirectAccessContainer) {
                this.refObject.refDelegate().objMove(
                    ((DirectAccessContainer)there).container,
                    criteria
                );
            } else {
                there.put(criteria, delegate);
            }
            this.identity = this.refObject.refGetPath();
        }

        //------------------------------------------------------------------------
        /**
         * Invokes the operation with signature <ResultClass> operation(<ParamsClass> params);
         * where <ResultClass> and <ParamsClass> are the result and parameter classes
         * of the operation refClass().refMofId() + ":" + operation. If the operation
         * is not defined then refInvokeOperation() is invoked instead.
         */  
        public Structure_1_0 objInvokeOperation(
            String operation,
            Structure_1_0 parameter
        ) throws ServiceException {
            this.getDelegate(); 

            ModelElement_1_0 operationDef = this.getModel().getFeatureDef(
                this.getModel().getElement(this.refObject.refClass().refMofId()),
                operation, 
                false
            );
            if(operationDef == null) {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.ASSERTION_FAILURE,
                    "operation not defined for class",
                    new BasicException.Parameter("class", this.refObject.refClass().refMofId()),
                    new BasicException.Parameter("operation", operation)
                );
            }

            // get the type names of 'in' parameter and 'result' 
            String qualifiedNameResultType = null;
            String qualifiedNameInParamType = null;
            String nameInParamType = null;
            for(
                    Iterator<?> i = operationDef.values("content").iterator();
                    i.hasNext();
            ) {
                ModelElement_1_0 paramDef = this.getModel().getElement(i.next());
                ModelElement_1_0 paramDefType = this.getType(paramDef);
                if("in".equals(paramDef.values("name").get(0))) {
                    qualifiedNameInParamType = (String)paramDefType.values("qualifiedName").get(0);
                    nameInParamType = (String)paramDefType.values("name").get(0);
                }
                else if("result".equals(paramDef.values("name").get(0))) {
                    qualifiedNameResultType = (String)paramDefType.values("qualifiedName").get(0);
                }
            }
            if(qualifiedNameInParamType == null) {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.ASSERTION_FAILURE,
                    "no parameter with name \"in\" defined for operation",
                    new BasicException.Parameter("operation", operationDef)
                );
            }
            if(qualifiedNameResultType == null) {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.ASSERTION_FAILURE,
                    "no parameter with name \"result\" defined for operation",
                    new BasicException.Parameter("operation", operationDef)
                );
            }

            // get operation to invoke
            try {
                RefRootPackage_1 rootPackage = (RefRootPackage_1)this.refObject.refOutermostPackage();
                Method method = null;
                boolean hasNoParams = false;
                try { 
                    // Try to find signature matching the binding suffix
                    String bindingPackageSuffix = rootPackage.refBindingPackageSuffix();
                    Class<?> inParamClass = Classes.getApplicationClass(
                        this.getModel().toJavaPackageName(
                            qualifiedNameInParamType, 
                            bindingPackageSuffix
                        ) +  
                        "." + (
                                "cci".equals(bindingPackageSuffix) ? nameInParamType : JavaNames.toClassName(nameInParamType)
                        )
                    );
                    try {
                        method = this.refObject.getClass().getMethod(
                            JavaNames.toMethodName(operation),
                            new Class[]{
                                inParamClass
                            }
                        );
                    }
                    catch(NoSuchMethodException e) {
                        if(!"org:openmdx:base:Void".equals(qualifiedNameInParamType)) {
                            throw e;                    
                        }
                        method = this.refObject.getClass().getMethod(
                            JavaNames.toMethodName(operation),
                            new Class[]{}
                        );   
                        hasNoParams = true;
                    }
                }
                // Fallback to cci2
                catch(NoSuchMethodException e) {
                    Class<?> inParamClass = Classes.getApplicationClass(
                        this.getModel().toJavaPackageName(
                            qualifiedNameInParamType, 
                            "cci2"
                        ) +  
                        "." + 
                        JavaNames.toClassName(nameInParamType)
                    );
                    try {
                        method = this.refObject.getClass().getMethod(
                            JavaNames.toMethodName(operation),
                            new Class[]{
                                inParamClass
                            }
                        );
                    }
                    catch(NoSuchMethodException e0) {
                        if(!"org:openmdx:base:Void".equals(qualifiedNameInParamType)) {
                            throw e;                    
                        }
                        method = this.refObject.getClass().getMethod(
                            JavaNames.toMethodName(operation),
                            new Class[]{}
                        );      
                        hasNoParams = true;
                    }
                }
                // invoke operation
                try {
                    return (Structure_1_0)this.toRefStructMarshaller(
                        qualifiedNameResultType
                    ).unmarshal(
                        hasNoParams ? 
                            method.invoke(this.refObject, EMPTY_OBJECT_ARRAY) : 
                                method.invoke(
                                    this.refObject, 
                                    this.toRefStructMarshaller(qualifiedNameInParamType).marshal(parameter)
                                )
                    );
                } 
                catch(InvocationTargetException e) {
                    Throwable t = e.getTargetException();
                    if(t instanceof ServiceException) {
                        throw (ServiceException)t;
                    }
                    else if(t instanceof RefException_1) {
                        throw ((RefException_1)t).refGetServiceException();
                    }
                    else if(t instanceof JmiServiceException) {
                        throw new ServiceException((JmiServiceException)t);
                    }
                    else if(t instanceof RuntimeServiceException) {
                        throw (RuntimeServiceException)t;
                    }
                    throw new RuntimeServiceException(
                        BasicException.toStackedException(t)
                    );
                }
                catch(IllegalAccessException e) {
                    throw new ServiceException(e);
                }
            }
            catch(ClassNotFoundException e) {
                throw new ServiceException(e);
            }
            catch(NoSuchMethodException e) {
                SysLog.info("method " + operation + " is not defined for class " + this.getClass().getName() + ". Invoking objInvokeOperation()");
                return this.refObject.refDelegate().objInvokeOperation(
                    operation,
                    parameter
                );
            }
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddToUnitOfWork()
         */
        public void objAddToUnitOfWork(
        ) throws ServiceException {
            this.getDelegate().objAddToUnitOfWork();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objCopy(org.openmdx.compatibility.base.collection.Container, java.lang.String)
         */
        public Object_1_0 objCopy(
            FilterableMap<String,Object_1_0> there, 
            String criteria
        ) throws ServiceException {
            return this.getDelegate().objCopy(
                there, 
                criteria
            );
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetClass()
         */
        public String objGetClass(
        ) throws ServiceException {
            // The class name is unmodifiable
            return this.qualifiedClassName == null ?
                this.getDelegate().objGetClass() :
                    this.qualifiedClassName;
        }

        //-------------------------------------------------------------------------
        /** (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetPath()
         */
        public Path objGetPath(
        ){
            return this.identity;
        }

        //-------------------------------------------------------------------------
        /**
         * Returns the object's access path.
         *
         * @return  the object's access path;
         *          or null for transient or new objects
         */
        public Object objGetResourceIdentifier(
        ){
            return this.identity == null ? null : this.identity.toUri();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objInvokeOperationInUnitOfWork(java.lang.String, org.openmdx.base.accessor.generic.cci.Structure_1_0)
         */
        public Structure_1_0 objInvokeOperationInUnitOfWork(
            String operation,
            Structure_1_0 arguments
        ) throws ServiceException {
            return this.getDelegate().objInvokeOperationInUnitOfWork(
                operation, 
                arguments
            );
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsDeleted()
         */
        public boolean objIsDeleted(
        ) throws ServiceException {
            return this.getDelegate().objIsDeleted();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsDirty()
         */
        public boolean objIsDirty(
        ) throws ServiceException {
            return this.getDelegate().objIsDirty();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsNew()
         */
        public boolean objIsNew(
        ) throws ServiceException {
            return this.refObject == null ?
                this.qualifiedClassName != null & this.identity != null :
                    this.getDelegate().objIsNew();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objIsPersistent()
         */
        public boolean objIsPersistent(
        ) throws ServiceException {
            return this.identity != null;
        }

        //-------------------------------------------------------------------------
        /**
         * Tests whether this object belongs to the current unit of work.
         *
         * @return  true if this instance belongs to the current unit of work.
         */
        public boolean objIsInUnitOfWork(
        ) throws ServiceException {
            return this.getDelegate().objIsInUnitOfWork();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRefresh()
         */
        public void objRefresh(
        ) throws ServiceException {
            this.getDelegate().objRefresh();
        }

        //-------------------------------------------------------------------------
        /**
         * Flush the state of the instance to its provider.
         * 
         * @return      true if all attributes could be flushed,
         *              false if some attributes contained placeholders
         *
         * @exception   ServiceException NOT_SUPPORTED
         *              if the unit of work is optimistic
         * @exception   ServiceException ILLEGAL_STATE
         *              if the object is not persistent
         * @exception   ServiceException 
         *              if the object can't be synchronized
         */
        public boolean objFlush(
        ) throws ServiceException {
            return this.getDelegate().objFlush();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objVolatile()
         */
        public void objMakeVolatile() throws ServiceException {
            this.getDelegate().objFlush();
        }  

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objAddEventListener(java.lang.String, java.util.EventListener)
         */
        public void objAddEventListener(
            String feature, 
            EventListener listener
        ) throws ServiceException {
            this.getDelegate().objAddEventListener(feature,listener); //... Marshalling to be added
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveEventListener(java.lang.String, java.util.EventListener)
         */
        public void objRemoveEventListener(String feature, EventListener listener) throws ServiceException {
            this.getDelegate().objRemoveEventListener(feature,listener); //... Marshalling to be added
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetEventListener(java.lang.String, java.lang.Class)
         */
        public EventListener[] objGetEventListeners(
            String feature, 
            Class<? extends EventListener> listenerType
        ) throws ServiceException {
            return this.getDelegate().objGetEventListeners(feature,listenerType); //... Marshalling to be added
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemove()
         */
        public void objRemove(
        ) throws ServiceException {
            this.getDelegate().objRemove();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objRemoveFromUnitOfWork()
         */
        public void objRemoveFromUnitOfWork(
        ) throws ServiceException {
            this.getDelegate().objRemoveFromUnitOfWork();
        }

        //-------------------------------------------------------------------------
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objSetValue(java.lang.String, java.lang.Object)
         */
        public void objSetValue(
            String feature, 
            Object to
        ) throws ServiceException {
            this.setValue(
                feature,
                to
            );
        }

        //-------------------------------------------------------------------------
        class LargeObject_1 implements LargeObject_1_0 {

            private final String feature;

            private long length = -1L;

            /**
             * Constructor
             * 
             * @param feature
             */
            LargeObject_1 (
                String feature
            ){
                this.feature = feature;
            }
            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#length()
             */
            public long length() throws ServiceException {
                return this.length;
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBytes(long, int)
             */
            public byte[] getBytes(long position, int capacity) throws ServiceException {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    "getBytes(long,int) not supported yet"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBinaryStream()
             */
            public InputStream getBinaryStream() throws ServiceException {
                try {
                    Object value = getValue(this.feature);
                    return value instanceof InputStream
                    ? (InputStream)value
                        : ((BinaryLargeObject)value).getContent();
                }
                catch(Exception e) {
                    throw new ServiceException(e);
                }
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getBinaryStream(java.io.OutputStream, long)
             */
            public void getBinaryStream(OutputStream stream, long position) throws ServiceException {
                this.length = getValue(this.feature, stream, position);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacters(long, int)
             */
            public char[] getCharacters(long position, int capacity) throws ServiceException {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    "getCharacters(long,int) not supported yet"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacterStream()
             */
            public Reader getCharacterStream() throws ServiceException {
                return (Reader)getValue(this.feature);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.ReadableLargeObject#getCharacterStream(java.io.Writer, long)
             */
            public void getCharacterStream(Writer writer, long position) throws ServiceException {
                this.length = getValue(this.feature, writer, position);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#truncate(long)
             */
            public void truncate(long length) throws ServiceException {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBytes(long, byte[])
             */
            public void setBytes(long position, byte[] content) throws ServiceException {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBinaryStream(java.io.InputStream, long)
             */
            public void setBinaryStream(InputStream stream, long size) throws ServiceException {
                setValue(this.feature, stream);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setBinaryStream(long)
             */
            public OutputStream setBinaryStream(long position) throws ServiceException {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacters(long, char[])
             */
            public void setCharacters(long position, char[] content) throws ServiceException {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    "Partial update not supported"
                );
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacterStream(java.io.Reader, long)
             */
            public void setCharacterStream(Reader stream, long size) throws ServiceException {
                setValue(this.feature, stream);
            }

            /* (non-Javadoc)
             * @see org.openmdx.compatibility.base.accessor.object.cci.WritableLargeObject#setCharacterStream(long)
             */
            public Writer setCharacterStream(long position) throws ServiceException {
                throw new ServiceException(
                    StackedException.DEFAULT_DOMAIN,
                    StackedException.NOT_SUPPORTED,
                    "setCharacterStream(long) not supported yet"
                );
            }
        }

        //-------------------------------------------------------------------------
        // Variables
        //-------------------------------------------------------------------------

        /**
         * @serial
         */
        private Path identity;

        /**
         * @serial
         */
        private String qualifiedClassName;

        /**
         * 
         */
        transient RefObject_1_0 refObject = null; // lazy init

        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.generic.cci.Object_1_1#objIsHollow()
         */
        public boolean objIsHollow() {
            return this.refObject == null;
        }

    }

    //---------------------------------------------------------------------------
    /**
     * Direct Access Container
     */
    class DirectAccessContainer 
    extends MarshallingFilterableMap<String,Object_1_0,FilterableMap<String,Object_1_0>> 
    implements Serializable 
    {

        /**
         * <code>serialVersionUID</code> to implement<code>Serializable</code>.
         */
        private static final long serialVersionUID = 3905801989500973104L;
        FilterableMap<String,Object_1_0> container;

        DirectAccessContainer(
            FilterableMap<String,Object_1_0> container
        ){
            super(
                RefObjectFactory_1.this, 
                new MarshallingFilterableMap<String,Object_1_0,FilterableMap<String,Object_1_0>>(
                        RefObjectFactory_1.this.refRootPackage,
                        container
                )
            );
            this.container = container;
        }

        /**
         * Constructor 
         *
         * @param parent
         * @param feature
         * @throws ServiceException
         */
        public DirectAccessContainer(
            Path parent,
            String feature
        ) throws ServiceException {
            this(
                RefObjectFactory_1.this.refRootPackage.refObjectFactory().getObject(
                    parent
                ).objGetContainer(feature)
            );
        }



    }

    //---------------------------------------------------------------------------
    /**
     * Container containing RefDelegatingObject_1's  
     */
    static class StandardContainer 
    extends AbstractMap<String,Object_1_0> 
    implements Serializable, FilterableMap<String,Object_1_0>, FetchSize
    {

        /**
         * <code>serialVersionUID</code> to implement<code>Serializable</code>.
         */
        private static final long serialVersionUID = 3257001038556051255L;

        @SuppressWarnings("deprecation")
        protected org.openmdx.compatibility.base.collection.Container<?> refSelection;

        protected LegacyContainer refContainer;

        protected org.openmdx.base.persistence.spi.Marshaller marshaller;

        transient private Set<Map.Entry<String,Object_1_0>> entries = null;

        @SuppressWarnings("deprecation")
        private StandardContainer(
            org.openmdx.base.persistence.spi.Marshaller marshaller,
            LegacyContainer refContainer,
            org.openmdx.compatibility.base.collection.Container<?> refSelection
        ) {
            this.marshaller = marshaller;
            this.refContainer = refContainer;
            this.refSelection = refSelection;
        }

        public StandardContainer(
            org.openmdx.base.persistence.spi.Marshaller marshaller,
            LegacyContainer refContainer
        ) {
            this(marshaller, refContainer, refContainer);
        }

        public StandardContainer(
            Marshaller marshaller,
            LegacyContainer refContainer
        ) {
            this(new CollectionMarshallerAdapter(marshaller), refContainer);
        }

        LegacyContainer getDelegate(
        ){
            return this.refContainer;
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set<Map.Entry<String,Object_1_0>> entrySet(
        ) {
            return this.entries == null ? this.entries = new MarshallingSet<Map.Entry<String,Object_1_0>>(
                    new ContainerMarshaller(this.marshaller),
                    this.refSelection
            ) : this.entries;
        }

        /* (non-Javadoc)
         * @see org.openmdx.provider.object.cci.Container#subSet(java.lang.Object)
         */
        @SuppressWarnings("deprecation")
        public FilterableMap<String,Object_1_0> subMap(Object filter) {
            return new StandardContainer(
                this.marshaller,
                this.refContainer,
                this.refSelection.subSet(this.marshaller.unmarshal(filter))
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.provider.object.cci.Container#toList(java.lang.Object)
         */
        @SuppressWarnings("deprecation")
        public List<Object_1_0> values(Object criteria) {
            return new MarshallingSequentialList<Object_1_0>(
                    this.marshaller,
                    this.refSelection.toList(this.marshaller.unmarshal(criteria))
            );
        }


        /* (non-Javadoc)
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public Object_1_0 put(String key, Object_1_0 value) {
            RefObject_1_0 refObject = (RefObject_1_0) this.marshaller.unmarshal(value);
            this.refContainer.refAddValue(
                key, 
                refObject
            );
            return null;
        }

        /* (non-Javadoc)
         * @see java.util.Map#values()
         */
        public Collection<Object_1_0> values() {
            return new MarshallingSet<Object_1_0>(
                    this.marshaller,
                    this.refSelection, null
            );
        }

        /* (non-Javadoc)
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            return this.refSelection.contains(this.marshaller.unmarshal(value));
        }

        /* (non-Javadoc)
         * @see java.util.Map#get(java.lang.Object)
         */
        @SuppressWarnings("deprecation")
        public Object_1_0 get(Object key) {
            return (Object_1_0) this.marshaller.marshal(this.refSelection.get(key));
        }

        /* (non-Javadoc)
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty() {
            return this.refSelection.isEmpty();
        }

        /* (non-Javadoc)
         * @see java.util.Map#remove(java.lang.Object)
         */
        public Object_1_0 remove(String key) {
            Object_1_0 oldValue = get(key);
            this.refSelection.remove(
                this.marshaller.unmarshal(oldValue)
            );
            return oldValue;
        }

        /* (non-Javadoc)
         * @see java.util.Map#size()
         */
        public int size() {
            return this.refSelection.size();
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.collection.FetchSize#batchSize()
         */
        public int getFetchSize(
        ) {
            if(
                    this.refContainer instanceof FetchSize
            ) this.fetchSize = ((FetchSize)this.refContainer).getFetchSize();
            return this.fetchSize;
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.collection.FetchSize#setFetchSize(int)
         */
        public void setFetchSize(
            int fetchSize
        ){
            this.fetchSize = fetchSize;
            if(
                    this.refContainer instanceof FetchSize
            ) ((FetchSize)this.refContainer).setFetchSize(fetchSize);
        }

        /**
         * The proposed fetch size
         */
        private int fetchSize = DEFAULT_FETCH_SIZE;

    }

    static class ContainerMarshaller
    implements Serializable, Marshaller
    {

        /**
         * <code>serialVersionUID</code> to implement<code>Serializable</code>.
         */
        private static final long serialVersionUID = 3257567287195349299L;

        /**
         * @serial
         */
        org.openmdx.base.persistence.spi.Marshaller marshaller;

        ContainerMarshaller(
            org.openmdx.base.persistence.spi.Marshaller marshaller
        ){
            this.marshaller = marshaller;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
         */
        public Object marshal(Object source) throws ServiceException {
            return source instanceof RefObject_1_0 ? 
                new ContainerEntry<Object>(this.marshaller, (RefObject_1_0) source) :
                    source;
        }

        /* (non-Javadoc)
         * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
         */
        @SuppressWarnings("unchecked")
        public Object unmarshal(Object source) throws ServiceException {
            return source instanceof Map.Entry ? 
                ((Map.Entry) source).getValue() :
                    source;
        }  

    }

    static class ContainerEntry<E> 
    implements Map.Entry<String,E>
    {

        RefObject_1_0 value;

        org.openmdx.base.persistence.spi.Marshaller marshaller;

        ContainerEntry(
            org.openmdx.base.persistence.spi.Marshaller marshaller,
            RefObject_1_0 value
        ){
            this.value = value;
            this.marshaller = marshaller;
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getKey()
         */
        public String getKey() {
            Path path = this.value.refGetPath();
            return path == null ? null : path.getBase();
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#getValue()
         */
        @SuppressWarnings("unchecked")
        public E getValue() {
            return (E) this.marshaller.marshal(this.value);
        }

        /* (non-Javadoc)
         * @see java.util.Map.Entry#setValue(java.lang.Object)
         */
        public E setValue(E value) {
            throw new UnsupportedOperationException();
        }

    }


    //-------------------------------------------------------------------------
    // Interface LegacyContainer
    //-------------------------------------------------------------------------

    /**
     * This interface keeps the legacy code running!
     */
    @SuppressWarnings("deprecation")
    public interface LegacyContainer
    extends org.openmdx.compatibility.base.collection.Container<RefObject_1_0> 
    {

        /**
         * Adds object to the container with qualifier. Adding an object to a container
         * with add(value) is equivalent to refAddValue(null, value).
         * 
         * @throws JmiServiceException in case the object can not be added to the container.
         */
        public void refAddValue(
            String qualifier,
            RefObject_1_0 value
        );

    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3979265850337276212L;
    protected static final Object[] EMPTY_OBJECT_ARRAY = new Object[]{};
    private static final String ASPECTS_ONLY = "Support for " +
        "org::openmdx::compatibility::view1 and " +
        "org::openmdx::compatibility::role1 has been removed"; 

}

//--- End of File -----------------------------------------------------------

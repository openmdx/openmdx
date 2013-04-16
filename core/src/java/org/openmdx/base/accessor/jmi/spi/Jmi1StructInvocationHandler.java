/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Jmi1StructInvocationHandler 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007-2010, OMEX AG, Switzerland
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jmi.reflect.RefStruct;
import javax.resource.cci.MappedRecord;

import org.omg.mof.spi.AbstractNames;
import org.omg.mof.spi.Identifier;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefStruct_1_0;
import org.openmdx.base.collection.Maps;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.TreeSparseArray;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.BinaryLargeObject;
import org.w3c.cci2.BinaryLargeObjects;
import org.w3c.cci2.SparseArray;

/**
 * Jmi1StructInvocationHandler
 */
public class Jmi1StructInvocationHandler implements InvocationHandler, Marshaller {

    //-----------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param refPackage
     * @param delegate
     */
    public Jmi1StructInvocationHandler(
        Jmi1Package_1_0 refPackage,
        MappedRecord delegate
    ) {
        this.refPackage = refPackage;
        this.delegate = delegate;
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected ModelElement_1_0 getFieldDef(
        String methodName
    ) throws ServiceException {
        String structName = this.delegate.getRecordName();
        ConcurrentMap<String,ModelElement_1_0> fields = allFields.get(structName);
        if(fields == null) {
            fields = Maps.putUnlessPresent(
                allFields,
                structName, 
                new ConcurrentHashMap<String,ModelElement_1_0>()
            );
        }
        ModelElement_1_0 fieldDef = fields.get(methodName);
        if(fieldDef == null) {
            ModelElement_1_0 structDef = Model_1Factory.getModel().getElement(structName);
            for(Map.Entry<String,ModelElement_1_0> field: ((Map<String,ModelElement_1_0>)structDef.objGetMap("field")).entrySet()) {
                fields.putIfAbsent(
                    Identifier.OPERATION_NAME.toIdentifier(
                        AbstractNames.openmdx2AccessorName(
                            field.getKey(),
                            true, // forQuery
                            false, // forBoolean
                            true // singleValued
                        )
                    ), 
                    field.getValue()
                );
                fields.putIfAbsent(
                    Identifier.OPERATION_NAME.toIdentifier(
                        AbstractNames.openmdx2AccessorName(
                            field.getKey(),
                            true, // forQuery
                            true, // forBoolean
                            true // singleValued
                        )
                    ), 
                    field.getValue()
                );
            }
            fieldDef = fields.get(methodName);
            if(fieldDef == null) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_FOUND, 
                    "field not found for struct",
                    new BasicException.Parameter("field.name", methodName),
                    new BasicException.Parameter("struct.name", structName)
                );                
            }
        }
        return fieldDef;
    }
            
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    @SuppressWarnings("unchecked")
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        String methodName = method.getName();
        Class<?> methodClass = method.getDeclaringClass();
        if(methodClass == RefStruct.class) {
            if("refFieldNames".equals(methodName)) {
                return new ArrayList<String>(this.delegate.keySet());
            } 
            else if ("refGetValue".equals(methodName)) {
                Model_1_0 model = Model_1Factory.getModel();
                String fieldName = (String)args[0];
                return getValue(                    
                    fieldName.indexOf(":") > 0 ?
                        model.getElement(fieldName) :
                        ((Map<String,ModelElement_1_0>)model.getElement(this.delegate.getRecordName()).objGetMap("field")).get(fieldName)
                );
            } 
            else if ("refTypeName".equals(methodName)) {
                return Arrays.asList(this.delegate.getRecordName().split(":"));
            }
        } else if (methodClass == RefStruct_1_0.class) {
            if("refDelegate".equals(methodName)) {
                return this.delegate;
            }
        } else if (methodClass == Object.class) {
            if("equals".equals(methodName)) {
                if(Proxy.isProxyClass(args[0].getClass())) {
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(args[0]);
                    if(invocationHandler instanceof Jmi1StructInvocationHandler) {
                        Jmi1StructInvocationHandler that = (Jmi1StructInvocationHandler) invocationHandler;
                        return this.delegate.equals(that.delegate);
                    }
                }
                return false;
            } else if("hashCode".equals(methodName)) {
                return this.delegate.hashCode();
            } else if("toString".equals(methodName)) {
                return this.delegate.toString();
            }
        } else if (args == null || args.length == 0){
            return this.getValue(
                this.getFieldDef(methodName)
            );
        } 
        throw new UnsupportedOperationException(method.getName());
    }

    /**
     * Retrieve a structure field value
     * 
     * @param fieldName the structure field name
     * 
     * @return the structure field value
     */
    @SuppressWarnings({
        "unchecked", "rawtypes"
    })
    private Object getValue(
        ModelElement_1_0 fieldDef
    ) throws ServiceException {
        String fieldName = (String)fieldDef.objGetValue("name");
        Object value = this.delegate.get(fieldName);
        switch(ModelHelper.getMultiplicity(fieldDef)){
	        case OPTIONAL: case SINGLE_VALUE:
	            return marshal(
                    value instanceof Collection ? ((Collection<?>)value).iterator().next() : value
                );
	        case LIST:
	            return  new MarshallingList(
                    this, 
                    value instanceof List ? (List)value : Collections.singletonList(value)
                );
	        case SET:
	            return  new MarshallingSet(
                    this, 
                    value instanceof Collection ? (Collection)value : Collections.singleton(value)
                );
	        case SPARSEARRAY:
	            SparseArray target = new TreeSparseArray();
	            for(Object e : ((Map)value).entrySet()) {
	                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
	                target.put(entry.getKey(), marshal(entry.getValue()));
	            }
	            return target;
        	default:
                return value;
        }
    }

    //------------------------------------------------------------------------
    // Implements Marshaller
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(Object source) {
        return 
            source instanceof Path ? refPackage.refObject((Path)source) :
            source instanceof MappedRecord ? refPackage.refCreateStruct((MappedRecord)source) :
            source;    
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(Object source) {
        return source instanceof RefObject_1_0 ?
            ((RefObject_1_0)source).refGetPath() :
            source;
    }
    
    //-----------------------------------------------------------------------
    class Jmi1BinaryLargeObject implements BinaryLargeObject {

        public Jmi1BinaryLargeObject(            
            String fieldName,
            InputStream value
        ) {
            this.fieldName = fieldName;
            this.initialValue = value;
        }

        protected transient InputStream initialValue = null;
        protected final String fieldName;
        protected transient Long length = null;

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            InputStream value = this.initialValue == null
            ? (InputStream)Jmi1StructInvocationHandler.this.delegate.get(this.fieldName)
                : this.initialValue;
            this.initialValue = null;
            return value;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            return this.length;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent(java.io.OutputStream, long)
         */
        public void getContent(
            OutputStream stream, 
            long position
        ) throws IOException {
            this.length = Jmi1StructInvocationHandler.this.delegate instanceof RefObject_1_0 ?
                ((RefObject_1_0)Jmi1StructInvocationHandler.this.delegate).refGetValue(
                    this.fieldName, 
                    stream, 
                    position
                ) :
                    position + BinaryLargeObjects.streamCopy(
                        getContent(), 
                        position,
                        stream
                    );            
        }

    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final static ConcurrentMap<String,ConcurrentMap<String,ModelElement_1_0>> allFields = 
        new ConcurrentHashMap<String,ConcurrentMap<String,ModelElement_1_0>>();
    protected final MappedRecord delegate;
    protected final Jmi1Package_1_0 refPackage;

}

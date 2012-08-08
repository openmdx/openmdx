/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1StructInvocationHandler.java,v 1.12 2008/02/15 14:02:30 wfro Exp $
 * Description: Jmi1StructInvocationHandler 
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/15 14:02:30 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.mapping.Names;
import org.openmdx.model1.mapping.java.Identifier;
import org.w3c.cci2.BinaryLargeObject;

/**
 * Jmi1StructInvocationHandler
 */
public class Jmi1StructInvocationHandler implements InvocationHandler {
    
    //-----------------------------------------------------------------------
    public static class RefStruct_1Proxy extends RefStruct_1 {
        
        public RefStruct_1Proxy(
            String typeName,
            RefPackage_1_0 refPackage,
            Object value
        ) {
            super(
                typeName,
                refPackage,
                value
            );
            this.qualifiedTypeName = typeName;
        }
                
        protected String refQualifiedTypeName(
        ) {
            return this.qualifiedTypeName;
        }

        private static final long serialVersionUID = -6190748767632081058L;
        private final String qualifiedTypeName;
    }
    
    //-----------------------------------------------------------------------
    private class Jmi1BinaryLargeObject implements BinaryLargeObject {

        public Jmi1BinaryLargeObject(            
            String fieldName,
            InputStream value
        ) {
            this.fieldName = fieldName;
            this.initialValue = value;
        }
        
        /* (non-Javadoc)
         * @see org.w3c.cci2.BinaryLargeObject#getContent()
         */
        public InputStream getContent(
        ) throws IOException {
            InputStream value = this.initialValue == null
                ? (InputStream)Jmi1StructInvocationHandler.this.delegation.refGetValue(this.fieldName)
                : this.initialValue;
            this.initialValue = null;
            return value;
        }

        /* (non-Javadoc)
         * @see org.w3c.cci2.LargeObject#getLength()
         */
        public Long getLength(
        ) throws IOException {
            return null;
        }
        
        protected transient InputStream initialValue = null;
        protected final String fieldName;
    }
        
    //-----------------------------------------------------------------------
    public Jmi1StructInvocationHandler(
        String typeName,
        RefPackage_1_0 refPackage,
        Object value
    ) {
        this.delegation = new RefStruct_1Proxy(
            typeName,
            refPackage,
            value
        );
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    protected String getMofName(
        String methodName
    ) throws ServiceException {
        String structName = this.delegation.refQualifiedTypeName();
        Map<String,String> fields = allFields.get(structName);
        if(fields == null) {
            allFields.put(
                structName, 
                fields = new HashMap<String,String>()
            );
        }
        String fieldName = fields.get(methodName);
        if(fieldName == null) {
            Model_1_0 model = this.delegation.getModel();
            ModelElement_1_0 structDef = model.getElement(structName);
            for(
                Iterator<String> i = ((Map)structDef.values("field").get(0)).keySet().iterator(); 
                i.hasNext(); 
            ) {
                String name = i.next();
                fields.put(
                    Identifier.OPERATION_NAME.toIdentifier(
                        Names.openmdx2AccessorName(
                            name,
                            true, // forQuery
                            false, // forBoolean
                            true // singleValued
                        )
                    ), 
                    name
                );
                fields.put(
                    Identifier.OPERATION_NAME.toIdentifier(
                        Names.openmdx2AccessorName(
                            name,
                            true, // forQuery
                            true, // forBoolean
                            true // singleValued
                        )
                    ), 
                    name
                );
            }
            fieldName = fields.get(methodName);
            if(fieldName == null) {
                throw new ServiceException (
                    BasicException.Code.DEFAULT_DOMAIN, 
                    BasicException.Code.NOT_FOUND, 
                    new BasicException.Parameter [] {
                      new BasicException.Parameter("field.name", methodName),
                      new BasicException.Parameter("struct.name", structName)
                    },
                    "field not found for struct"
                );                
            }
        }
        return fieldName;
    }
        
    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        // RefObject
        if(
            method.getName().startsWith("ref") && 
            (method.getName().length() > 3) &&
            Character.isUpperCase(method.getName().charAt(3))
        ) {
            try {
                return method.invoke(
                    this.delegation, 
                    args
                );
            }
            catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
        // Getters
        else if(method.getName().startsWith("get")) {
            String fieldName = method.getName().substring(3);
            fieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(fieldName);
            if((args == null) || (args.length == 0)) {      
                Object value = this.delegation.refGetValue(
                    fieldName
                );
                return value instanceof InputStream
                    ? new Jmi1BinaryLargeObject(fieldName, (InputStream)value)
                    : value;
            }
        }
        // Boolean getters
        else if(method.getName().startsWith("is")) {
            String fieldName = method.getName().substring(2);
            fieldName = Identifier.ATTRIBUTE_NAME.toIdentifier(fieldName);
            if((args == null) || (args.length == 0)) {  
                try {
                    return this.delegation.refGetValue(
                        method.getName()
                    );                    
                }
                catch(JmiServiceException e) {
                    if(e.getExceptionCode() != BasicException.Code.NOT_FOUND) {
                        throw e;
                    }
                    return this.delegation.refGetValue(
                        fieldName
                    );
                }
            }
        }
        // Object
        else if("toString".equals(method.getName())) {
            return this.delegation.toString();
        }
        throw new UnsupportedOperationException(method.getName());
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final RefStruct_1 delegation;
    protected final static Map<String,Map<String,String>> allFields = new HashMap<String,Map<String,String>>();

}

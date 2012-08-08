/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openmdx.uses.org.apache.servicemix.jbi.messaging;

import java.net.URI;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.RobustInOnly;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.openmdx.uses.org.apache.servicemix.JbiConstants;
import org.openmdx.uses.org.apache.servicemix.id.IdGenerator;

/**
 * Resolver for URI patterns
 *
 * @version $Revision: 1.1 $
 */
public class MessageExchangeFactoryImpl implements MessageExchangeFactory {

    private QName interfaceName;
    private QName serviceName;
    private QName operationName;
    private ServiceEndpoint endpoint;
    private IdGenerator idGenerator;
    private ComponentContext context;
    private AtomicBoolean closed;

    /**
     * Constructor for a factory
     * @param idGen
     */
    public MessageExchangeFactoryImpl(IdGenerator idGen, AtomicBoolean closed){
        this.idGenerator = idGen;
        this.closed = closed;
    }
    
    protected void checkNotClosed() throws MessagingException {
        if (closed.get()) {
            throw new MessagingException("DeliveryChannel has been closed.");
        }
    }

    /**
     * Create an exchange from the specified pattern
     *
     * @param pattern
     * @return MessageExchange
     * @throws MessagingException
     */
    public MessageExchange createExchange(URI pattern) throws MessagingException {
        checkNotClosed();
        MessageExchange result = null;
        if (pattern != null) {
            if (pattern.equals(MessageExchangeSupport.IN_ONLY) ||
                pattern.equals(MessageExchangeSupport.WSDL2_IN_ONLY)) {
                result = createInOnlyExchange();
            }
            else if (pattern.equals(MessageExchangeSupport.IN_OUT) ||
                     pattern.equals(MessageExchangeSupport.WSDL2_IN_OUT)) {
                result = createInOutExchange();
            }
            else if (pattern.equals(MessageExchangeSupport.IN_OPTIONAL_OUT) ||
                     pattern.equals(MessageExchangeSupport.WSDL2_IN_OPTIONAL_OUT)) {
                result = createInOptionalOutExchange();
            }
            else if (pattern.equals(MessageExchangeSupport.ROBUST_IN_ONLY) ||
                     pattern.equals(MessageExchangeSupport.WSDL2_ROBUST_IN_ONLY)) {
                result = createRobustInOnlyExchange();
            }
        }
        if (result == null) {
            throw new MessagingException("Do not understand pattern: " + pattern);
        }
        return result;
    }

    /**
     * create InOnly exchange
     *
     * @return InOnly exchange
     * @throws MessagingException
     */
    public InOnly createInOnlyExchange() throws MessagingException {
        checkNotClosed();
        InOnlyImpl result =  new InOnlyImpl(getExchangeId());
        setDefaults(result);
        return result;
    }

    /**
     * create RobustInOnly exchange
     *
     * @return RobsutInOnly exchange
     * @throws MessagingException
     */
    public RobustInOnly createRobustInOnlyExchange() throws MessagingException {
        checkNotClosed();
        RobustInOnlyImpl result =  new RobustInOnlyImpl(getExchangeId());
        setDefaults(result);
        return result;
    }

    /**
     * create InOut Exchange
     *
     * @return InOut exchange
     * @throws MessagingException
     */
    public InOut createInOutExchange() throws MessagingException {
        checkNotClosed();
        InOutImpl result = new InOutImpl(getExchangeId());
        setDefaults(result);
        return result;
    }

    /**
     * create InOptionalOut exchange
     *
     * @return InOptionalOut exchange
     * @throws MessagingException
     */
    public InOptionalOut createInOptionalOutExchange() throws MessagingException {
        checkNotClosed();
        InOptionalOutImpl result =  new InOptionalOutImpl(getExchangeId());
        setDefaults(result);
        return result;
    }

    /**
     * Create an exchange that points at an endpoint that conforms to the declared capabilities, requirements, and
     * policies of both the consumer and the provider.
     *
     * @param serviceName
     * @param operationName the WSDL name of the operation to be performed
     * @return a message exchange that is initialized with given interfaceName, operationName, and the endpoint decided
     *         upon by JBI.
     * @throws MessagingException
     */
    public MessageExchange createExchange(QName serviceName, QName operationName) throws MessagingException {
        // TODO: look for the operation in the wsdl and infer the MEP
        checkNotClosed();
        InOptionalOutImpl me =  new InOptionalOutImpl(getExchangeId());
        setDefaults(me);
        me.setService(serviceName);
        me.setOperation(operationName);
        return me;
    }

    protected String getExchangeId() {
        return idGenerator.generateId();
    }
    
    /**
     * @return endpoint
     */
    public ServiceEndpoint getEndpoint() {
        return endpoint;
    }
    
    /**
     * set endpoint
     * @param endpoint
     */
    public void setEndpoint(ServiceEndpoint endpoint) {
        this.endpoint = endpoint;
    }
    
    /**
     * @return interface name
     */
    public QName getInterfaceName() {
        return interfaceName;
    }
    
    /**
     * set interface name
     * @param interfaceName
     */
    public void setInterfaceName(QName interfaceName) {
        this.interfaceName = interfaceName;
    }
    
    /**
     * @return service name
     */
    public QName getServiceName() {
        return serviceName;
    }
    
    /**
     * set service name
     * @param serviceName
     */
    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }
    
    /**
     * @return Returns the operationName.
     */
    public QName getOperationName() {
        return operationName;
    }


    /**
     * @param operationName The operationName to set.
     */
    public void setOperationName(QName operationName) {
        this.operationName = operationName;
    }

    /**
     * Get the Context 
     * @return the context
     */
    public ComponentContext getContext() {
        return context;
    }

    /**
     * Set the Context
     * @param context
     */
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    protected void setDefaults(MessageExchangeImpl exchange) {
        exchange.setOperation(getOperationName());
        if (endpoint != null) {
            exchange.setEndpoint(getEndpoint());
        } else {
            exchange.setService(serviceName);
            exchange.setInterfaceName(interfaceName);
        }

//        if (getContext() != null) {
//            exchange.setSourceContext(getContext());
//            PojoMarshaler marshaler = getContext().getActivationSpec().getMarshaler();
//            if (marshaler != null) {
//                exchange.setMarshaler(marshaler);
//            }
//        }
        exchange.setProperty(JbiConstants.DATESTAMP_PROPERTY_NAME, Calendar.getInstance());
    }
}
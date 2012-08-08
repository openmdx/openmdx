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
package org.openmdx.uses.org.apache.servicemix.components.util;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmdx.uses.org.apache.servicemix.jbi.FaultException;
import org.openmdx.uses.org.apache.servicemix.jbi.NotInitialisedYetException;
import org.openmdx.uses.org.apache.servicemix.jbi.management.BaseLifeCycle;

/**
 * A useful base class for a POJO based JBI component which contains most of the basic plumbing
 *
 * @version $Revision: 1.1 $
 */
public abstract class PojoSupport extends BaseLifeCycle implements ComponentLifeCycle {

    private ComponentContext context;
    private ObjectName extensionMBeanName;
    private QName service;
    private String endpoint;
    private MessageExchangeFactory exchangeFactory;
    private String description = "POJO Component";
    private ServiceEndpoint serviceEndpoint;
    private DeliveryChannel channel;
    
    protected Log logger = LogFactory.getLog(getClass());
    
    protected PojoSupport() {
    }

    protected PojoSupport(QName service, String endpoint) {
        this.service = service;
        this.endpoint = endpoint;
    }
    
    /**
     * Get the description
     * @return the description
     */
    public String getDescription(){
        return description;
    }


    /**
     * Called when the Component is initialized
     *
     * @param cc
     * @throws JBIException
     */
    public void init(ComponentContext cc) throws JBIException {
        this.context = cc;
        this.channel = this.context.getDeliveryChannel();
        init();
        if (service != null && endpoint != null) {
            serviceEndpoint = context.activateEndpoint(service, endpoint);
        }
    }

    /**
     * Shut down the item. The releases resources, preparing to uninstall
     * 
     * @exception javax.jbi.JBIException if the item fails to shut down.
     */
    public void shutDown() throws javax.jbi.JBIException {
        if (serviceEndpoint != null) {
            context.deactivateEndpoint(serviceEndpoint);
        }
        exchangeFactory = null;
//        super.shutDown();
    }

    // Helper methods
    //-------------------------------------------------------------------------

    /**
     * A helper method to return the body of the message as a POJO which could be a
     * bean or some DOMish model of the body.
     *
     * @param message the message on which to extract the body
     * @return the body of the message as a POJO or DOM object
     * @throws MessagingException
     */
    public Object getBody(NormalizedMessage message) throws MessagingException {
        return MessageHelper.getBody(message);
    }

    /**
     * Sets the body of the message as a POJO
     *
     * @param message the message on which to set the body
     * @param body    the POJO or DOMish model to set
     * @throws MessagingException
     */
    public void setBody(NormalizedMessage message, Object body) throws MessagingException {
        MessageHelper.setBody(message, body);
    }


    // Properties
    //-------------------------------------------------------------------------
    public ObjectName getExtensionMBeanName() {
        return extensionMBeanName;
    }

    public void setExtensionMBeanName(ObjectName extensionMBeanName) {
        this.extensionMBeanName = extensionMBeanName;
    }

    public ComponentContext getContext() {
        return context;
    }

    public QName getService() {
        return service;
    }

    public void setService(QName service) {
        this.service = service;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    /**
     * Provide access to the default message exchange exchangeFactory, lazily creating one.
     */
    public MessageExchangeFactory getExchangeFactory() throws MessagingException {
        if (exchangeFactory == null) {
            if (context != null) {
                exchangeFactory = getDeliveryChannel().createExchangeFactory();
            }
        }
        return exchangeFactory;
    }

    public DeliveryChannel getDeliveryChannel() throws MessagingException {
        if (channel == null) {
            throw new NotInitialisedYetException();
        }
        return channel;
    }

    /**
     * A helper method to allow a component to initialise prior to the endpoint being activated
     * but after the component context has been configured.
     *
     * @throws JBIException
     */
    protected void init() throws JBIException {
        super.init();
    }

    /**
     * A helper method to indicate that the message exchange is complete
     * which will set the status to {@link ExchangeStatus#DONE} and send the message
     * on the delivery channel.
     *
     * @param exchange
     * @throws MessagingException
     */
    public void done(MessageExchange exchange) throws MessagingException {
        exchange.setStatus(ExchangeStatus.DONE);
        getDeliveryChannel().send(exchange);
    }

    public void send(MessageExchange exchange) throws MessagingException {
        getDeliveryChannel().send(exchange);
    }
    
    public boolean sendSync(MessageExchange exchange) throws MessagingException {
        return getDeliveryChannel().sendSync(exchange);
    }

    public boolean sendSync(MessageExchange exchange, long timeMillis) throws MessagingException {
        return getDeliveryChannel().sendSync(exchange, timeMillis);
    }

    /**
     * A helper method to indicate that the message exchange should be
     * continued with the given response and send the message
     * on the delivery channel.
     *
     * @param exchange
     * @throws MessagingException
     */
    public void answer(MessageExchange exchange, NormalizedMessage answer) throws MessagingException {
        exchange.setMessage(answer, "out");
        getDeliveryChannel().send(exchange);
    }

    /**
     * A helper method which fails and completes the given exchange with the specified fault
     */
    public void fail(MessageExchange exchange, Fault fault) throws MessagingException {
        if (exchange instanceof InOnly || fault == null) {
            exchange.setError(new FaultException("Fault occured for in-only exchange", exchange, fault));
        } else {
            exchange.setFault(fault);
        }
        getDeliveryChannel().send(exchange);
    }

    /**
     * A helper method which fails and completes the given exchange with the specified error
     * @throws MessagingException 
     */
    public void fail(MessageExchange exchange, Exception error) throws MessagingException {
        if (exchange instanceof InOnly || error instanceof FaultException == false) {
            exchange.setError(error);
        } else {
            FaultException faultException = (FaultException) error;
            exchange.setFault(faultException.getFault());
        }
        getDeliveryChannel().send(exchange);
    }


    /**
     * A helper method which will return true if the exchange is capable of both In and Out such as InOut,
     * InOptionalOut etc.
     *
     * @param exchange
     * @return true if the exchange can handle both input and output
     */
    protected boolean isInAndOut(MessageExchange exchange) {
        return exchange instanceof InOut || exchange instanceof InOptionalOut;
    }

}

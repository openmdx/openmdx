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

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.openmdx.uses.org.apache.servicemix.JbiConstants;
import org.openmdx.uses.org.apache.servicemix.MessageExchangeListener;

/**
 * A useful base class for a transform component.
 *
 * @version $Revision: 1.1 $
 */
public abstract class TransformComponentSupport extends ComponentSupport implements MessageExchangeListener {
    
    private boolean copyProperties = true;
    private boolean copyAttachments = true;

    protected TransformComponentSupport() {
    }

    protected TransformComponentSupport(QName service, String endpoint) {
        super(service, endpoint);
    }

    public void onMessageExchange(MessageExchange exchange) {
        // Skip done exchanges
        if (exchange.getStatus() == ExchangeStatus.DONE) {
            return;
        // Handle error exchanges
        } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
            return;
        }
        try {
            InOnly outExchange = null;
            NormalizedMessage in = getInMessage(exchange);
            NormalizedMessage out;
            if (isInAndOut(exchange)) {
                out = exchange.createMessage();
            } else {
                outExchange = getExchangeFactory().createInOnlyExchange();
                out = outExchange.createMessage();
            }
            boolean txSync = exchange.isTransacted() && Boolean.TRUE.equals(exchange.getProperty(JbiConstants.SEND_SYNC));
            copyPropertiesAndAttachments(exchange, in, out);
            if (transform(exchange, in, out)) {
                if (isInAndOut(exchange)) {
                    exchange.setMessage(out, "out");
                    if (txSync) {
                        getDeliveryChannel().sendSync(exchange);
                    } else {
                        getDeliveryChannel().send(exchange);
                    }
                }
                else {
                    outExchange.setMessage(out, "in");
                    if (txSync) {
                        getDeliveryChannel().sendSync(outExchange);
                    } else {
                        getDeliveryChannel().send(outExchange);
                    }
                    exchange.setStatus(ExchangeStatus.DONE);
                    getDeliveryChannel().send(exchange);
                }
            } else {
                exchange.setStatus(ExchangeStatus.DONE);
                getDeliveryChannel().send(exchange);
            }
        }
        catch (Exception e) {
            try {
                fail(exchange, e);
            } catch (Exception e2) {
                logger.warn("Unable to handle error: " + e2, e2);
                if (logger.isDebugEnabled()) {
                    logger.debug("Original error: " + e, e);
                }
            }
        }
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Transforms the given out message
     */
    protected abstract boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception;


    public boolean isCopyProperties() {
        return copyProperties;
    }


    public void setCopyProperties(boolean copyProperties) {
        this.copyProperties = copyProperties;
        if (getMessageTransformer() instanceof CopyTransformer) {
            ((CopyTransformer) getMessageTransformer()).setCopyProperties(copyProperties);
        }
    }


    public boolean isCopyAttachments() {
        return copyAttachments;
    }


    public void setCopyAttachments(boolean copyAttachments) {
        this.copyAttachments = copyAttachments;
        if (getMessageTransformer() instanceof CopyTransformer) {
            ((CopyTransformer) getMessageTransformer()).setCopyAttachments(copyAttachments);
        }
    }


    /**
     * If enabled the properties and attachments are copied to the destination message
     */
    protected void copyPropertiesAndAttachments(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        if (isCopyProperties()) {
            CopyTransformer.copyProperties(in, out);
        }
        if (isCopyAttachments()) {
            CopyTransformer.copyAttachments(in, out);
        }
    }
}

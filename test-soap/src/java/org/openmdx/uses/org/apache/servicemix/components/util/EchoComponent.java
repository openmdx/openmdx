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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmdx.uses.org.apache.servicemix.MessageExchangeListener;

/**
 * A simple, yet useful component for testing synchronous flows. Echos back Exchanges
 * 
 * @version $Revision: 1.1 $
 */
public class EchoComponent extends TransformComponentSupport implements MessageExchangeListener {
    private static final Log log = LogFactory.getLog(EchoComponent.class);
    
    public EchoComponent() {
    }
    
    public EchoComponent(QName service, String endpoint) {
        super(service, endpoint);
    }
    
    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        getMessageTransformer().transform(exchange, in, out);
        log.info("Echoed back message: " + out);
        return true;
    }
}

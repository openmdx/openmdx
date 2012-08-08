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

import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * A plugin strategy which marshals an Object into and out of a JBI message.
 * This interface is used by the ServiceMixClient to marshal POJOs into and out of JBI messages.
 *
 * @version $Revision: 1.1 $
 */
public interface PojoMarshaler {

    /**
     * The key on the message to store the message body which cannot be marshaled into or out of XML easily
     * or to provide a cache of the object representation of the object.
     */
    String BODY = "org.apache.servicemix.body";

    /**
     * Marshals the payload into the normalized message, typically as the content
     * property.
     *
     * @param exchange the message exchange in which to marshal
     * @param message the message in which to marshal
     * @param body the body of the message as a POJO
     */
    void marshal(MessageExchange exchange, NormalizedMessage message, Object body) throws MessagingException;

    /**
     * Unmarshals the response out of the normalized message.
     *
     * @param exchange the message exchange, which is an {@link InOut} or {@link InOptionalOut}
     * @param message the output message
     * @return the unmarshaled body object, extracted from the message
     * @throws MessagingException
     */
    Object unmarshal(MessageExchange exchange, NormalizedMessage message) throws MessagingException;
}

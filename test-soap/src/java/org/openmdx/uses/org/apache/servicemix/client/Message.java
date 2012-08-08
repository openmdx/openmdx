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
package org.openmdx.uses.org.apache.servicemix.client;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * An extension of the standard {@link NormalizedMessage} which allows you to
 * work directly with message bodies as POJOs ignoring the XML stuff or passing a binary
 * message around as a ByteBuffer or byte[]
 * 
 * @version $Revision: 1.1 $
 */
public interface Message extends NormalizedMessage {

    /**
     * Returns the body as a POJO. Depending on the implementation this could be
     * a Java POJO, a DOM tree or a byte[]
     */
    public Object getBody() throws MessagingException;

    /**
     * Sets the body as a POJO
     */
    public void setBody(Object body) throws MessagingException;

    /**
     * Returns the message exchange
     */
    public MessageExchange getExchange();

    /**
     * Helper method to create a new fault for this message exchange
     */
    public Fault createFault() throws MessagingException;
}

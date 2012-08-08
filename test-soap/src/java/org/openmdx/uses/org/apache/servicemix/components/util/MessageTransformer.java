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

/**
 * Represents a transformer of an input message into an output message
 *
 * @version $Revision: 1.1 $
 */
public interface MessageTransformer {

    /**
     * Transfers the state in the input message into the output message
     * @param exchange the exchange on which the messages are flowing
     * @param in the input message
     * @param out an empty out message ready to contain the result of the transformation
     */
    boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException;
}

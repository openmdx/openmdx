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

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;

import org.openmdx.uses.org.apache.servicemix.JavaSource;
import org.openmdx.uses.org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.openmdx.uses.org.apache.servicemix.jbi.messaging.PojoMarshaler;

/**
 * Some helper methods for working with messages
 *
 * @version $Revision: 1.1 $
 */
public class MessageHelper {

    /**
     * A helper method to return the body of the message as a POJO which could be a
     * bean or some DOMish model of the body.
     *
     * @param message the message on which to extract the body
     * @return the body of the message as a POJO or DOM object
     * @throws javax.jbi.messaging.MessagingException
     *
     */
    public static Object getBody(NormalizedMessage message) throws MessagingException {
        Source content = message.getContent();
        if (content instanceof JavaSource) {
            JavaSource source = (JavaSource) content;
            return source.getObject();
        }
        if (message instanceof NormalizedMessageImpl) {
            return ((NormalizedMessageImpl) message).getBody();
        }
        return message.getProperty(PojoMarshaler.BODY);
    }

    /**
     * A helper method to return the body of the message as a POJO which could be a
     * bean or some DOMish model of the body.
     *
     * @param message    the message on which to extract the body
     * @param marshaller the marshaller used to map from the XML representation to the POJO
     * @return the body of the message as a POJO or DOM object
     * @throws javax.jbi.messaging.MessagingException
     *
     */
    public static Object getBody(NormalizedMessage message, PojoMarshaler marshaller) throws MessagingException {
        Source content = message.getContent();
        if (content instanceof JavaSource) {
            JavaSource source = (JavaSource) content;
            return source.getObject();
        }
        if (message instanceof NormalizedMessageImpl) {
            return ((NormalizedMessageImpl) message).getBody(marshaller);
        }
        return message.getProperty(PojoMarshaler.BODY);
    }

    /**
     * Sets the body of the message as a POJO
     *
     * @param message the message on which to set the body
     * @param body    the POJO or DOMish model to set
     * @throws MessagingException
     */
    public static void setBody(NormalizedMessage message, Object body) throws MessagingException {
        Source content = message.getContent();
        if (content instanceof JavaSource) {
            JavaSource source = (JavaSource) content;
            source.setObject(body);
        }
        else if (message instanceof NormalizedMessageImpl) {
            ((NormalizedMessageImpl) message).setBody(body);
        }
        else {
            message.setProperty(PojoMarshaler.BODY, body);
        }
    }

}

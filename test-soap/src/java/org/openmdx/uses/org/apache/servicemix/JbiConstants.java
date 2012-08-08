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
package org.openmdx.uses.org.apache.servicemix;


public interface JbiConstants {

    String SEND_SYNC = "javax.jbi.messaging.sendSync";
    
    String PROTOCOL_TYPE = "javax.jbi.messaging.protocol.type";
    
    String PROTOCOL_HEADERS = "javax.jbi.messaging.protocol.headers";
    
    String SECURITY_SUBJECT = "javax.jbi.security.subject";
    
    String SOAP_HEADERS = "org.apache.servicemix.soap.headers";
    
	String PERSISTENT_PROPERTY_NAME = "org.apache.servicemix.persistent";
    
    String DATESTAMP_PROPERTY_NAME = "org.apache.servicemix.datestamp";
    
    String FLOW_PROPERTY_NAME = "org.apache.servicemix.flow";
    
    String STATELESS_CONSUMER = "org.apache.servicemix.consumer.stateless";
    
    String STATELESS_PROVIDER = "org.apache.servicemix.provider.stateless";
    
    String SENDER_ENDPOINT = "org.apache.servicemix.senderEndpoint";

    String HTTP_DESTINATION_URI = "org.apache.servicemix.http.destination.uri";
    
    /**
     * This property should be set when a consumer endpoint creates an exchange
     * related to another provider exchange.  The value of the property should
     * be set to the value of this property in the provider exchange,
     * or to the id of the provider exchange if the property does not exist.
     */
    String CORRELATION_ID = "org.apache.servicemix.correlationId";
    
}

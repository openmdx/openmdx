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
package org.openmdx.uses.org.apache.servicemix.jbi.resolver;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

/**
 * A Strategy pattern which can be used to plug in different {@link ServiceEndpoint} resolving policies from static
 * relationships to dynamic resolution of the endpoint used based on some policy.
 *
 * @version $Revision: 1.1 $
 */
public interface EndpointResolver {

    /**
     * Resolves the endpoint which should be used for the given message exchange
     * using either a hard coded endpoint or some policy which chooses the endpoint
     * dynamically using some algorithm.
     *
     * @param context  is the component context
     * @param exchange the message exchange which the endpoint will be used for which may
     *                 contain some state to help choose the algorithm.
     * @param filter the filter to be applied to the available endpoints
     * @return the chosen endpoint or null if no endpoint could be found.
     */
    ServiceEndpoint resolveEndpoint(ComponentContext context, MessageExchange exchange, EndpointFilter filter) throws JBIException;

    /**
     * Resolves all the available endpoints which may not be applicable to a component.
     */ 
    ServiceEndpoint[] resolveAvailableEndpoints(ComponentContext context, MessageExchange exchange) throws JBIException;
}

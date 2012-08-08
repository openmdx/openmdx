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

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

/**
 * A selection policy which will choose a service endpoint from an array of possible endpoints
 * using some kind of policy.
 *
 * @version $Revision: 1.1 $
 */
public interface EndpointChooser {

    /**
     * Chooses an endpoint from the array of 2 or more endpoints or returns null if none of them are acceptable
     */
    ServiceEndpoint chooseEndpoint(ServiceEndpoint[] endpoints, ComponentContext context, MessageExchange exchange);
}

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
 * A useful base class for {@link EndpointResolver} implementations.
 *
 * @version $Revision: 1.1 $
 */
public abstract class EndpointResolverSupport implements EndpointResolver {
    private EndpointChooser chooser;
    private boolean failIfUnavailable = true;

    public ServiceEndpoint resolveEndpoint(ComponentContext context, MessageExchange exchange, EndpointFilter filter) throws JBIException {
        ServiceEndpoint[] endpoints = resolveAvailableEndpoints(context, exchange);
        if (endpoints.length > 0) {
            endpoints = filterEndpoints(endpoints, exchange, filter);
        }
        if (endpoints.length == 0) {
            if (failIfUnavailable) {
                throw createServiceUnavailableException();
            }
            else {
                return null;
            }
        }
        if (endpoints.length == 1) {
            return endpoints[0];
        }
        return getChooser().chooseEndpoint(endpoints, context, exchange);
    }

    public boolean isFailIfUnavailable() {
        return failIfUnavailable;
    }

    public void setFailIfUnavailable(boolean failIfUnavailable) {
        this.failIfUnavailable = failIfUnavailable;
    }

    public EndpointChooser getChooser() {
        if (chooser == null) {
            chooser = new FirstChoicePolicy();
        }
        return chooser;
    }

    public void setChooser(EndpointChooser chooser) {
        this.chooser = chooser;
    }

    protected abstract JBIException createServiceUnavailableException();

    protected ServiceEndpoint[] filterEndpoints(ServiceEndpoint[] endpoints, MessageExchange exchange, EndpointFilter filter) {
        int matches = 0;
        for (int i = 0; i < endpoints.length; i++) {
            ServiceEndpoint endpoint = endpoints[i];
            if (filter.evaluate(endpoint, exchange)) {
                matches++;
            }
            else {
                endpoints[i] = null;
            }
        }
        if (matches == endpoints.length) {
            return endpoints;
        }
        else {
            ServiceEndpoint[] answer = new ServiceEndpoint[matches];
            for (int i = 0, j = 0; i < endpoints.length; i++) {
                ServiceEndpoint endpoint = endpoints[i];
                if (endpoint != null) {
                    answer[j++] = endpoints[i];
                }
            }
            return answer;
        }
    }
}

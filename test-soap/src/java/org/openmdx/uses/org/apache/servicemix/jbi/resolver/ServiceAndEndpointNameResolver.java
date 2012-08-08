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
import javax.xml.namespace.QName;

import org.openmdx.uses.org.apache.servicemix.jbi.NoServiceAvailableException;

/**
 * Resolves the endpoint using the service name and endpoint name to resolve the {@link ServiceEndpoint}
 *
 * @version $Revision: 1.1 $
 */
public class ServiceAndEndpointNameResolver extends EndpointResolverSupport {
	
    private QName serviceName;
    private String endpointName;

    public ServiceAndEndpointNameResolver() {
    }

    public ServiceAndEndpointNameResolver(QName serviceName, String endpointName) {
        this.serviceName = serviceName;
        this.endpointName = endpointName;
    }

    public ServiceEndpoint[] resolveAvailableEndpoints(ComponentContext context, MessageExchange exchange) throws JBIException {
        ServiceEndpoint endpoint = context.getEndpoint(serviceName, endpointName);
        if (endpoint != null) {
            return new ServiceEndpoint[]{endpoint};
        }
        else {
            return new ServiceEndpoint[0];
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected JBIException createServiceUnavailableException() {
        return new NoServiceAvailableException(serviceName);
    }
}

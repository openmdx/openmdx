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
package org.openmdx.uses.org.apache.servicemix.jbi;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

/**
 * An exception thrown if a component cannot find an instance of a {@link ServiceEndpoint} for a given serviceName.
 *
 * @version $Revision: 1.1 $
 */
public class NoServiceAvailableException extends NoEndpointAvailableException {
    private static final long serialVersionUID = -996143500855531757L;

    private QName serviceName;

    public NoServiceAvailableException(QName serviceName) {
        super("Cannot find an instance of the service: " + serviceName);
        this.serviceName = serviceName;
    }

    /**
     * Returns the service name that could not be found
     */
    public QName getServiceName() {
        return serviceName;
    }
}

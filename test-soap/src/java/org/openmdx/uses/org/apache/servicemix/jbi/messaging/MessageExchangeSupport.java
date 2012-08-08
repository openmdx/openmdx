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

import java.net.URI;

/**
 * Resolver for URI patterns
 *
 * @version $Revision: 1.1 $
 */
public class MessageExchangeSupport {
    /**
     * In Only MEP.
     */
    public static final URI IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/in-only");
    /**
     * In Out MEP.
     */
    public static final URI IN_OUT = URI.create("http://www.w3.org/2004/08/wsdl/in-out");
    /**
     * In Optional Out MEP.
     */
    public static final URI IN_OPTIONAL_OUT = URI.create("http://www.w3.org/2004/08/wsdl/in-opt-out");
    /**
     * Robust In Only MEP.
     */
    public static final URI ROBUST_IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/robust-in-only");
    /**
     * Out Only MEP.
     */
    public static final URI OUT_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/out-only");
    /**
     * Out In MEP.
     */
    public static final URI OUT_IN = URI.create("http://www.w3.org/2004/08/wsdl/out-in");
    /**
     * Out Optional In MEP.
     */
    public static final URI OUT_OPTIONAL_IN = URI.create("http://www.w3.org/2004/08/wsdl/out-opt-in");
    /**
     * Robust Out Only MEP.
     */
    public static final URI ROBUST_OUT_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/robust-out-only");


    /**
     * In Only MEP.
     */
    public static final URI WSDL2_IN_ONLY = URI.create("http://www.w3.org/2006/01/wsdl/in-only");
    /**
     * In Out MEP.
     */
    public static final URI WSDL2_IN_OUT = URI.create("http://www.w3.org/2006/01/wsdl/in-out");
    /**
     * In Optional Out MEP.
     */
    public static final URI WSDL2_IN_OPTIONAL_OUT = URI.create("http://www.w3.org/2006/01/wsdl/in-opt-out");
    /**
     * Robust In Only MEP.
     */
    public static final URI WSDL2_ROBUST_IN_ONLY = URI.create("http://www.w3.org/2006/01/wsdl/robust-in-only");
    /**
     * Out Only MEP.
     */
    public static final URI WSDL2_OUT_ONLY = URI.create("http://www.w3.org/2006/01/wsdl/out-only");
    /**
     * Out In MEP.
     */
    public static final URI WSDL2_OUT_IN = URI.create("http://www.w3.org/2006/01/wsdl/out-in");
    /**
     * Out Optional In MEP.
     */
    public static final URI WSDL2_OUT_OPTIONAL_IN = URI.create("http://www.w3.org/2006/01/wsdl/out-opt-in");
    /**
     * Robust Out Only MEP.
     */
    public static final URI WSDL2_ROBUST_OUT_ONLY = URI.create("http://www.w3.org/2006/01/wsdl/robust-out-only");
}
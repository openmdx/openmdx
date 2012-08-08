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
package org.openmdx.uses.org.apache.servicemix.jbi.security.auth;

import java.security.GeneralSecurityException;

import javax.security.auth.Subject;

/**
 * Interface for the authentication service.
 * 
 */
public interface AuthenticationService {

    /**
     * Authenticate a user given its name and credentials.
     * Upon sucessfull completion, the subject should be populated
     * with the user known principals.
     * 
     * @param subject the subject to populate
     * @param domain the security domain to use
     * @param user the user name
     * @param credentials the user credntials
     * @throws GeneralSecurityException if the user can not be authenticated
     */
    void authenticate(Subject subject, String domain, String user, Object credentials) throws GeneralSecurityException;
    
}

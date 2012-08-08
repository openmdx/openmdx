/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/file/ResourceManagerSystemException.java,v 1.1 2005/03/24 13:42:53 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:42:53 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmdx.uses.org.apache.commons.transaction.file;

/**
 * Signals an internal system error in a {@link ResourceManager}.
 * 
 * @version $Revision: 1.1 $
 *
 */
public class ResourceManagerSystemException extends ResourceManagerException {
    /**
     * 
     */
    private static final long serialVersionUID = 3832903239002634550L;

    public ResourceManagerSystemException(String message, int status, Object txId, Throwable cause) {
        super(message, status, txId, cause);
    }

    public ResourceManagerSystemException(String message, int status, Object txId) {
        super(message, status, txId);
    }

    public ResourceManagerSystemException(int status, Object txId, Throwable cause) {
        super(status, txId, cause);
    }

    public ResourceManagerSystemException(int status, Object txId) {
        super(status, txId);
    }
}

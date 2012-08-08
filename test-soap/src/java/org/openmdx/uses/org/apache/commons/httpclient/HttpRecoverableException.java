/*
 * $Header: /cvsroot/openmdx/test-soap/src/java/org/openmdx/uses/org/apache/commons/httpclient/HttpRecoverableException.java,v 1.2 2007/03/22 15:37:57 wfro Exp $
 * $Revision: 1.2 $
 * $Date: 2007/03/22 15:37:57 $
 *
 * ====================================================================
 *
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.openmdx.uses.org.apache.commons.httpclient;

/**
 * <p>
 * Signals that an HTTP or HttpClient exception has occurred.  This
 * exception may have been caused by a transient error and the request
 * may be retried.  It may be possible to retrieve the underlying transient
 * error via the inherited {@link HttpException#getCause()} method.
 * </p>
 * 
 * @deprecated no longer used
 * 
 * @author Unascribed
 * @version $Revision: 1.2 $ $Date: 2007/03/22 15:37:57 $
 */
public class HttpRecoverableException extends HttpException {

    /**
     * Creates a new HttpRecoverableException with a <tt>null</tt> detail message.
     */
    public HttpRecoverableException() {
        super();
    }

    /**
     * Creates a new HttpRecoverableException with the specified detail message.
     *
     * @param message exception message
     */
    public HttpRecoverableException(String message) {
        super(message);
    }

}

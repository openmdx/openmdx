/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/jca/MapManagedConnectionFactory.java,v 1.2 2008/03/21 18:42:19 hburger Exp $
 * $Revision: 1.2 $
 * $Date: 2008/03/21 18:42:19 $
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

package org.openmdx.uses.org.apache.commons.transaction.memory.jca;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;


/**
 *   
 * @version $Revision: 1.2 $
 * 
 */
@SuppressWarnings("unchecked")
public class MapManagedConnectionFactory implements ManagedConnectionFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 3258408422146847288L;
    private PrintWriter writer;

    /**
     * @see ManagedConnectionFactory#createConnectionFactory(ConnectionManager)
     */
    public Object createConnectionFactory(ConnectionManager cm)
        throws ResourceException {

        return new MapConnectionFactory(this, cm);
    }

    /**
     * @see ManagedConnectionFactory#createConnectionFactory()
     */
    public Object createConnectionFactory() throws ResourceException {

        return new MapConnectionFactory(this, null);
    }

    /**
     * @see ManagedConnectionFactory#createManagedConnection(Subject, ConnectionRequestInfo)
     */
    public ManagedConnection createManagedConnection(
        Subject subject,
        ConnectionRequestInfo cxRequestInfo)
        throws ResourceException {

        return new MapManagedConnection(cxRequestInfo);
    }

    /**
     * @see ManagedConnectionFactory#matchManagedConnections(Set, Subject, ConnectionRequestInfo)
     */
    public ManagedConnection matchManagedConnections(
        Set connectionSet,
        Subject subject,
        ConnectionRequestInfo cxRequestInfo)
        throws ResourceException {

        ManagedConnection match = null;
        Iterator iterator = connectionSet.iterator();
        if (iterator.hasNext()) {
            match = (ManagedConnection) iterator.next();
        }

        return match;
    }

    /**
     * @see ManagedConnectionFactory#setLogWriter(PrintWriter)
     */
    public void setLogWriter(PrintWriter writer) throws ResourceException {

        this.writer = writer;
    }

    /**
     * @see ManagedConnectionFactory#getLogWriter()
     */
    public PrintWriter getLogWriter() throws ResourceException {

        return writer;
    }

    public boolean equals(Object other) {

        if (other instanceof MapManagedConnectionFactory) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        
        return 0;
    }
}

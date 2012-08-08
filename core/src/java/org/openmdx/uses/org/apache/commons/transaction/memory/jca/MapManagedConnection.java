/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/jca/MapManagedConnection.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:43:56 $
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
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.openmdx.uses.org.apache.commons.transaction.memory.TransactionalMapWrapper;

/**
 *   
 * @version $Revision: 1.1 $
 * 
 */
public class MapManagedConnection implements ManagedConnection {

    MapXAResource xares = null;
    MapLocalTransaction tx = null;
    String name = null;
    TransactionalMapWrapper map = null;

    protected MapConnection connection = null;
    protected List listeners = new ArrayList();
    protected PrintWriter out;

    public MapManagedConnection(ConnectionRequestInfo cxRequestInfo) {
        name = ((MapConnectionSpec) cxRequestInfo).getName();

        map = MemoryMapResourceManager.getInstance().lookup(name);
        xares = new MapXAResource(map);
        tx = new MapLocalTransaction(map);

    }

    Map getMap() {
        return map;
    }

    public void close() {
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        for (Iterator it = listeners.iterator(); it.hasNext();) {
            ((ConnectionEventListener) it.next()).connectionClosed(event);
        }
        connection.invalidate();
        connection = null;
    }

    /**
     * @see ManagedConnection#getConnection(Subject, ConnectionRequestInfo)
     */
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {

        if (connection == null) {
            connection = new MapConnection(this);
        }
        return connection;
    }

    /**
     * @see ManagedConnection#destroy()
     */
    public void destroy() throws ResourceException {

        if (connection != null) {
            connection.invalidate();
            connection = null;
        }
    
        listeners = null;
        name = null;
        map = null;
        xares = null;
        tx = null;
    }

    /**
     * @see ManagedConnection#cleanup()
     */
    public void cleanup() throws ResourceException {

        if (connection != null) {
            connection.invalidate();
        }
    }

    /**
     * @see ManagedConnection#associateConnection(Object)
     */
    public void associateConnection(Object connection) throws ResourceException {
        if (!(connection instanceof MapConnection)) {
            throw new ResourceException("Connection is not of type MapConnection");
        }
        
        this.connection = (MapConnection)connection;
        this.connection.mc = this;
    }

    /**
     * @see ManagedConnection#addConnectionEventListener(ConnectionEventListener)
     */
    public void addConnectionEventListener(ConnectionEventListener listener) {

        listeners.add(listener);
    }

    /**
     * @see ManagedConnection#removeConnectionEventListener(ConnectionEventListener)
     */
    public void removeConnectionEventListener(ConnectionEventListener listener) {

        listeners.remove(listener);
    }

    /**
     * @see ManagedConnection#getXAResource()
     */
    public XAResource getXAResource() throws ResourceException {
        return xares;
    }

    /**
     * @see ManagedConnection#getLocalTransaction()
     */
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return tx;
    }

    /**
     * @see ManagedConnection#getMetaData()
     */
    public ManagedConnectionMetaData getMetaData() throws ResourceException {

        return null;
    }

    /**
     * @see ManagedConnection#setLogWriter(PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.out = out;
        xares.setLoggerFacade(out);
    }

    /**
     * @see ManagedConnection#getLogWriter()
     */
    public PrintWriter getLogWriter() throws ResourceException {

        return out;
    }
}

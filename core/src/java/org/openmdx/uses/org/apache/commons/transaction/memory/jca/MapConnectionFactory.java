/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/memory/jca/MapConnectionFactory.java,v 1.1 2005/03/24 13:43:56 hburger Exp $
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

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

/**
 *   
 * @version $Revision: 1.1 $
 * 
 */
public class MapConnectionFactory implements ConnectionFactory {

    /**
     * 
     */
    private static final long serialVersionUID = 3834592088929874744L;
    Reference reference;
    ConnectionManager cm;
    ManagedConnectionFactory mcf;

    String name;

    public MapConnectionFactory(ManagedConnectionFactory mcf, ConnectionManager cm) {
        System.out.println("MCF Init with mcf " + mcf + " cm " + cm);
        this.mcf = mcf;
        this.cm = cm;
    }

    public Connection getConnection() throws ResourceException {
        throw new NotSupportedException("Need name of accessed map. Call getConnection(ConnectionSpec spec) instead");
    }

    public Connection getConnection(ConnectionSpec spec) throws ResourceException {
        if (!(spec instanceof MapConnectionSpec)) {
            throw new NotSupportedException("ConnectionSpec must be instance of MapConnectionSpec");
        }
        System.out.println("Getting connection with spec "+spec);
        return (Connection) cm.allocateConnection(mcf, (MapConnectionSpec)spec);
    }

    public RecordFactory getRecordFactory() throws ResourceException {
        return null;
    }

    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return null;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Reference getReference() throws NamingException {
        return reference;
    }

    public String getName() {
        System.out.println("Getting name " + name);
        return name;
    }

    public void setName(String string) {
        System.out.println("Setting name " + string);
        name = string;
    }

}

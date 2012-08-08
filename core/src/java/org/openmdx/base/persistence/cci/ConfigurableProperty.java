/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ConfigurableProperty.java,v 1.7 2011/04/27 06:04:21 hburger Exp $
 * Description: JDO 2.0 Options
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/04/27 06:04:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2011, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.persistence.cci;

import javax.jdo.Constants;

/**
 * JDO 2.0 Options
 *
 * @since openMDX 2.0
 */
public enum ConfigurableProperty {

    //------------------------------------------------------------------------
    // Persistence Manager Factory Properties
    //------------------------------------------------------------------------

    Optimistic(Constants.PROPERTY_OPTIMISTIC),
    RetainValues(Constants.PROPERTY_RETAIN_VALUES),
    RestoreValues(Constants.PROPERTY_RESTORE_VALUES),
    ReadOnly(Constants.PROPERTY_READONLY),
    IgnoreCache(Constants.PROPERTY_IGNORE_CACHE),
    TransactionIsolationLevel(Constants.PROPERTY_TRANSACTION_ISOLATION_LEVEL),
    NontransactionalRead(Constants.PROPERTY_NONTRANSACTIONAL_READ),
    NontransactionalWrite(Constants.PROPERTY_NONTRANSACTIONAL_WRITE),
    Multithreaded(Constants.PROPERTY_MULTITHREADED),
    DetachAllOnCommit(Constants.PROPERTY_DETACH_ALL_ON_COMMIT),
    ConnectionDriverName(Constants.PROPERTY_CONNECTION_DRIVER_NAME),
    ConnectionUserName(Constants.PROPERTY_CONNECTION_USER_NAME),
    ConnectionPassword(Constants.PROPERTY_CONNECTION_PASSWORD),
    ConnectionURL(Constants.PROPERTY_CONNECTION_URL),
    ConnectionFactoryName(Constants.PROPERTY_CONNECTION_FACTORY_NAME),
    ConnectionFactory2Name(Constants.PROPERTY_CONNECTION_FACTORY2_NAME),
    Mapping(Constants.PROPERTY_MAPPING),
    CopyOnAttach(Constants.PROPERTY_COPY_ON_ATTACH),
    Name(Constants.PROPERTY_NAME),
    PersistenceUnitName(Constants.PROPERTY_NAME),
    ServerTimeZoneID(Constants.PROPERTY_SERVER_TIME_ZONE_ID),
    TransactionType("javax.persistence.TransactionType"),

    //------------------------------------------------------------------------
    // Optional Features
    //------------------------------------------------------------------------

    TransientTransactional(Constants.OPTION_TRANSACTIONAL_TRANSIENT),
    ApplicationIdentity(Constants.OPTION_APPLICATION_IDENTITY),
    DatastoreIdentity(Constants.OPTION_DATASTORE_IDENTITY),
    NonDurableIdentity(Constants.OPTION_NONDURABLE_IDENTITY),
    ArrayList(Constants.OPTION_ARRAYLIST),
    LinkedList(Constants.OPTION_LINKEDLIST),
    TreeMap(Constants.OPTION_TREEMAP),
    TreeSet(Constants.OPTION_TREESET),
    Vector(Constants.OPTION_VECTOR),
    NullCollection(Constants.OPTION_NULL_COLLECTION),
    ChangeApplicationIdentity(Constants.OPTION_CHANGE_APPLICATION_IDENTITY),
    BinaryCompatibility(Constants.OPTION_BINARY_COMPATIBILITY),
    GetDataStoreConnection(Constants.OPTION_GET_DATASTORE_CONNECTION),
    UnconstrainedQueryVariables(Constants.OPTION_UNCONSTRAINED_QUERY_VARIABLES),

    //------------------------------------------------------------------------
    // Standard JDO Properties
    //------------------------------------------------------------------------

    PersistenceManagerFactoryClass(Constants.PROPERTY_PERSISTENCE_MANAGER_FACTORY_CLASS),
    MappingCatalog(Constants.PROPERTY_MAPPING_CATALOG),
    MappingSchema(Constants.PROPERTY_MAPPING_SCHEMA),

    //------------------------------------------------------------------------
    // Vendor Specific Properties
    //------------------------------------------------------------------------

    ConnectionFactory("org.openmdx.jdo.ConnectionFactory"),
    ConnectionFactory2("org.openmdx.jdo.ConnectionFactory2"),
    ContainerManaged("org.openmdx.jdo.option.ContainerManaged");

    /**
     * Constructor
     * 
     * @param qualifiedName the property's qualifiedName
     */
    private ConfigurableProperty(
        String qualifiedName
    ){
        this.qualifiedName = qualifiedName;
    }

    /**
     * The property name
     */
    private final String qualifiedName;

    /**
     * Retrieve the property's qualified name
     * 
     * @return the qualified property name
     */
    public final String qualifiedName(
    ){
        return this.qualifiedName;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return qualifiedName();
    }

}

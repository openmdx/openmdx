/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ConfigurableProperty.java,v 1.6 2009/01/14 14:33:22 wfro Exp $
 * Description: JDO 2.0 Options
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/14 14:33:22 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.persistence.cci;



/**
 * JDO 2.0 Options
 *
 * @since openMDX 2.0
 */
public enum ConfigurableProperty {

    //------------------------------------------------------------------------
    // Persistence Manager Factory Properties
    //------------------------------------------------------------------------

    Optimistic("javax.jdo.option.Optimistic"),
    RetainValues("javax.jdo.option.RetainValues"),
    RestoreValues("javax.jdo.option.RestoreValues"),
    ReadOnly("javax.jdo.option.ReadOnly"),
    IgnoreCache("javax.jdo.option.IgnoreCache"),
    TransactionIsolationLevel("javax.jdo.option.TransactionIsolationLevel"),
    NontransactionalRead("javax.jdo.option.NontransactionalRead"),
    NontransactionalWrite("javax.jdo.option.NontransactionalWrite"),
    Multithreaded("javax.jdo.option.Multithreaded"),
    DetachAllOnCommit("javax.jdo.option.DetachAllOnCommit"),
    ConnectionDriverName("javax.jdo.option.ConnectionDriverName"),
    ConnectionUserName("javax.jdo.option.ConnectionUserName"),
    ConnectionPassword("javax.jdo.option.ConnectionPassword"),
    ConnectionURL("javax.jdo.option.ConnectionURL"),
    ConnectionFactoryName("javax.jdo.option.ConnectionFactoryName"),
    ConnectionFactory2Name("javax.jdo.option.ConnectionFactory2Name"),
    Mapping("javax.jdo.option.Mapping"),
    CopyOnAttach("javax.jdo.option.CopyOnAttach"),
    Name("javax.jdo.option.Name"),
    PersistenceUnitName("javax.jdo.option.PersistenceUnitName"),
    ServerTimeZoneID("javax.jdo.option.ServerTimeZoneID"),
    TransactionType("javax.persistence.TransactionType"),

    //------------------------------------------------------------------------
    // Optional Features
    //------------------------------------------------------------------------

    TransientTransactional("javax.jdo.option.TransientTransactional"),
    ApplicationIdentity("javax.jdo.option.ApplicationIdentity"),
    DatastoreIdentity("javax.jdo.option.DatastoreIdentity"),
    NonDurableIdentity("javax.jdo.option.NonDurableIdentity"),
    ArrayList("javax.jdo.option.ArrayList"),
    LinkedList("javax.jdo.option.LinkedList"),
    TreeMap("javax.jdo.option.TreeMap"),
    TreeSet("javax.jdo.option.TreeSet"),
    Vector("javax.jdo.option.Vector"),
    NullCollection("javax.jdo.option.NullCollection"),
    ChangeApplicationIdentity("javax.jdo.option.ChangeApplicationIdentity"),
    BinaryCompatibility("javax.jdo.option.BinaryCompatibility"),
    GetDataStoreConnection("javax.jdo.option.GetDataStoreConnection"),
    UnconstrainedQueryVariables("javax.jdo.option.UnconstrainedQueryVariables"),

    //------------------------------------------------------------------------
    // Standard JDO Properties
    //------------------------------------------------------------------------

    PersistenceManagerFactoryClass("javax.jdo.PersistenceManagerFactoryClass"),
    MappingCatalog("javax.jdo.mapping.Catalog"),
    MappingSchema("javax.jdo.mapping.Schema"),

    //------------------------------------------------------------------------
    // Vendor Specific Options
    //------------------------------------------------------------------------

    AccessorFactoryClass("javax.jdo.PersistenceManagerFactoryClass"),
    ConnectionFactory("org.openmdx.jdo.ConnectionFactory"),
    ConnectionFactory2("org.openmdx.jdo.ConnectionFactory2"),
    ContainerManaged("org.openmdx.transaction.ContainerManaged"),
    BindingPackageSuffix("org.openmdx.base.accessor.BindingPackageSuffix"),
    ObjectIdBuilder("org.oasisopen.spi2.ObjectIdBuilder");

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

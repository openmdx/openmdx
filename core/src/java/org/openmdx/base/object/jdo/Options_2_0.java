/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Options_2_0.java,v 1.14 2008/05/14 09:46:18 hburger Exp $
 * Description: JDO 2.0 Options
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/14 09:46:18 $
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
package org.openmdx.base.object.jdo;

import org.openmdx.kernel.persistence.cci.ConfigurableProperty;

/**
 * JDO 2.0 Options
 *
 * @deprecated use ConfigurableProperty
 */
public class Options_2_0 {

    //------------------------------------------------------------------------
    // Persistence Manager Factory Properties
    //------------------------------------------------------------------------

    public final static String OPTIMISTIC = ConfigurableProperty.Optimistic.qualifiedName(); // "javax.jdo.option.Optimistic";
    public final static String RETAIN_VALUES = ConfigurableProperty.RetainValues.qualifiedName(); // "javax.jdo.option.RetainValues";
    public final static String RESTORE_VALUES = ConfigurableProperty.RestoreValues.qualifiedName(); // "javax.jdo.option.RestoreValues";
    public final static String IGNORE_CACHE = ConfigurableProperty.IgnoreCache.qualifiedName(); // "javax.jdo.option.IgnoreCache";
    public final static String NONTRANSACTIONAL_READ = ConfigurableProperty.NontransactionalRead.qualifiedName(); // "javax.jdo.option.NontransactionalRead";
    public final static String NONTRANSACTIONAL_WRITE = ConfigurableProperty.NontransactionalWrite.qualifiedName(); // "javax.jdo.option.NontransactionalWrite";
    public final static String MULTITHREADED = ConfigurableProperty.Multithreaded.qualifiedName(); // "javax.jdo.option.Multithreaded";
    public final static String DETACH_ALL_ON_COMMIT = ConfigurableProperty.DetachAllOnCommit.qualifiedName(); // "javax.jdo.option.DetachAllOnCommit";
    public final static String CONNECTION_DRIVER_NAME = ConfigurableProperty.ConnectionDriverName.qualifiedName(); // "javax.jdo.option.ConnectionDriverName";
    public final static String CONNECTION_USER_NAME = ConfigurableProperty.ConnectionUserName.qualifiedName(); // "javax.jdo.option.ConnectionUserName";
    public final static String CONNECTION_PASSWORD = ConfigurableProperty.ConnectionPassword.qualifiedName(); // "javax.jdo.option.ConnectionPassword";
    public final static String CONNECTION_URL = ConfigurableProperty.ConnectionURL.qualifiedName(); // "javax.jdo.option.ConnectionURL";
    public final static String CONNECTION_FACTORY_NAME = ConfigurableProperty.ConnectionFactoryName.qualifiedName(); // "javax.jdo.option.ConnectionFactoryName";
    public final static String CONNECTION_FACTORY2_NAME = ConfigurableProperty.ConnectionFactory2Name.qualifiedName(); // "javax.jdo.option.ConnectionFactory2Name";
    public final static String MAPPING = ConfigurableProperty.Mapping.qualifiedName(); // "javax.jdo.option.Mapping";
    
    //------------------------------------------------------------------------
    // Optional Features
    //------------------------------------------------------------------------

    public final static String TRANSIENT_TRANSACTIONAL = ConfigurableProperty.TransientTransactional.qualifiedName(); // "javax.jdo.option.TransientTransactional";
//  public final static String NONTRANSACTIONAL_READ = ConfigurableProperty.NontransactionalRead.qualifiedName(); // "javax.jdo.option.NontransactionalRead";
//  public final static String NONTRANSACTIONAL_WRITE = ConfigurableProperty.NontransactionalWrite.qualifiedName(); // "javax.jdo.option.NontransactionalWrite";
//  public final static String RETAIN_VALUES = ConfigurableProperty.RetainValues.qualifiedName(); // "javax.jdo.option.RetainValues";
//  public final static String OPTIMISTIC = ConfigurableProperty.Optimistic.qualifiedName(); // "javax.jdo.option.Optimistic";
    public final static String APPLICATION_IDENTITY = ConfigurableProperty.ApplicationIdentity.qualifiedName(); // "javax.jdo.option.ApplicationIdentity";
    public final static String DATASTORE_IDENTITY = ConfigurableProperty.DatastoreIdentity.qualifiedName(); // "javax.jdo.option.DatastoreIdentity";
    public final static String NON_DURABLE_IDENTITY = ConfigurableProperty.NonDurableIdentity.qualifiedName(); // "javax.jdo.option.NonDurableIdentity";
    public final static String ARRAY_LIST = ConfigurableProperty.ArrayList.qualifiedName(); // "javax.jdo.option.ArrayList"; 
    public final static String LINKED_LIST = ConfigurableProperty.LinkedList.qualifiedName(); // "javax.jdo.option.LinkedList"; 
    public final static String TREE_MAP = ConfigurableProperty.TreeMap.qualifiedName(); // "javax.jdo.option.TreeMap"; 
    public final static String TREE_SET = ConfigurableProperty.TreeSet.qualifiedName(); // "javax.jdo.option.TreeSet"; 
    public final static String VECTOR = ConfigurableProperty.Vector.qualifiedName(); // "javax.jdo.option.Vector"; 
    public final static String LIST = "javax.jdo.option.List";
    public final static String ARRAY = "javax.jdo.option.Array";
    public final static String NULL_COLLECTION = ConfigurableProperty.NullCollection.qualifiedName(); // "javax.jdo.option.NullCollection";
    public final static String CHANGE_APPLICATION_IDENTITY = ConfigurableProperty.ChangeApplicationIdentity.qualifiedName(); // "javax.jdo.option.ChangeApplicationIdentity";
    public final static String BINARY_COMPATIBILITY = ConfigurableProperty.BinaryCompatibility.qualifiedName(); // "javax.jdo.option.BinaryCompatibility";
    public final static String GET_DATA_STORE_CONNECTION = ConfigurableProperty.GetDataStoreConnection.qualifiedName(); // "javax.jdo.option.GetDataStoreConnection";
    public final static String UNCONSTRAINED_QUERY_VARIABLES = ConfigurableProperty.UnconstrainedQueryVariables.qualifiedName(); // "javax.jdo.option.UnconstrainedQueryVariables";

    //------------------------------------------------------------------------
    // Vendor Specific Options
    //------------------------------------------------------------------------
    public final static String BINDING_PACKAGE_SUFFIX = ConfigurableProperty.BindingPackageSuffix.qualifiedName(); // "org.openmdx.base.accessor.BindingPackageSuffix";
    public final static String OBJECT_ID_BUILDER = ConfigurableProperty.ObjectIdBuilder.qualifiedName(); // "org.oasisopen.spi2.ObjectIdBuilder";
        
}

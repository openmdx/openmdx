/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Container_1_0.java,v 1.4 2009/06/03 17:36:37 hburger Exp $
 * Description: Container_1_0 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/03 17:36:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.cci;

import org.openmdx.base.collection.FilterableMap;

/**
 * Container_1_0
 */
public interface Container_1_0
    extends FilterableMap<String, DataObject_1_0>
{
    /**
     * Retrieve the super-set
     * 
     * @return the super-set
     */
    Container_1_0 superSet(
    );

    /**
     * Retrieve the containers path
     * 
     * @return path of container
     */
    Object getContainerId();

    /**
     * Load the collection into the cache
     * <p>
     * Retrieve field values of instances from the store.  This tells
     * the <code>PersistenceManager</code> that the application intends to use 
     * the instances, and their field values should be retrieved.  The fields
     * in the current fetch group must be retrieved, and the implementation
     * might retrieve more fields than the current fetch group.
     * <P>
     * If the useFetchPlan parameter is false, this method behaves exactly
     * as the corresponding method without the useFetchPlan parameter. 
     * If the useFetchPlan parameter is true, and the fetch plan has not been
     * modified from its default setting, all fields in the current fetch plan
     * are fetched, and other fields might be fetched lazily by the
     * implementation. If the useFetchPlan parameter is true, and the fetch
     * plan has been changed from its default setting, then the fields
     * specified by the fetch plan are loaded, along with related instances
     * specified by the fetch plan.
     * @param useFetchPlan whether to use the current fetch plan to determine
     * which fields to load and which instances to retrieve.     */
    void retrieveAll(
        boolean useFetchPlan
    );

}

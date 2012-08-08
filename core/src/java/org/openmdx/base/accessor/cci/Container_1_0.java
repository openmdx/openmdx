/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Container_1_0.java,v 1.13 2010/12/18 18:37:38 hburger Exp $
 * Description: Container_1_0 
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/12/18 18:37:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2010, OMEX AG, Switzerland
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

import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;

import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.OrderSpecifier;

/**
 * Container_1_0
 */
public interface Container_1_0 
    extends PersistenceCapableCollection, Map<String,DataObject_1_0> 
{

    /**
     * Retrieve the selection's container
     * 
     * @return the selection's container
     */
    Container_1_0 container();

    /**
     * Selects objects matching the filter.
     *
     * @param       filter
     *              The filter to be applied to objects of this container
     *
     * @return      A subset of this container containing the objects
     *              matching the filter.
     * 
     * @exception   ClassCastException
     *              if the class of the specified filter prevents it from
     *              being applied to this container.
     * @exception   IllegalArgumentException
     *              if some aspect of this filter prevents it from being
     *              applied to this container.
     */
    Container_1_0 subMap(
        Filter filter
    );

    /**
     * Applies given criteria to the elements of the container and returns the
     * result as list.
     * 
     * @param fetchPlan 
     * @param       criteria
     *                The criteria to be applied to objects of this container;
     *                or <code>null</code> for all the container's elements in
     *                      their default order.
     *
     * @return    a list based on the container's elements and the given
     *                      criteria.
     * 
     * @exception   ClassCastException
     *                  if the class of the specified criteria prevents them from
     *                  being applied to this container's elements.
     */
    List<DataObject_1_0> values(
        FetchPlan fetchPlan, 
        OrderSpecifier... criteria
    );

    /**
     * Tells whether the collection has been loaded into the cache.
     * 
     * @return <code>true</code> if the collection has been loaded into the cache.
     */
    boolean isRetrieved();
    
}

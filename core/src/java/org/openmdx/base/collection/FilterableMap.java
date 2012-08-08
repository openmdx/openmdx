/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: FilterableMap.java,v 1.4 2008/02/08 16:51:25 hburger Exp $
 * Description: Filterable Map
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.collection;

import java.util.List;
import java.util.Map;

/**
 * Filterable Map
 */
public interface FilterableMap<K,V>
    extends Map<K,V>
{

    /**
     * Selects objects matching the filter.
     * <p>
     * The semantics of the collection returned by this method become
     * undefined if the backing collection (i.e., this container) is
     * structurally modified in any way other than via the returned collection.
     * (Structural modifications are those that change the size of this list, or
     * otherwise perturb it in such a fashion that iterations in progress may
     * yield incorrect results.) 
     * <p>
     * This method returns a <code>Collection</code> as opposed to a
     * <code>Set</code> because it behaves as set in respect to object id
     * equality, not element equality.
     * <p>
     * The acceptable filter object classes must be specified by the 
     * container implementation.
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
    FilterableMap<K,V> subMap(
        Object filter
    );

    /**
     * Applies given criteria to the elements of the container and returns the
     * result as list.
     * <p>
     * The acceptable criteria classes must be specified by the container 
     * implementation.
     *
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
     * @exception   IllegalArgumentException
     *                  if some aspect of the criteria prevents them from being
     *                  applied to this container's elements. 
     */
    List<V> values(
        Object criteria
    );
        
}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Order Specifier Record
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.base.rest.cci;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.query.SortOrder;

/**
 * <code>org::openmdx::kernel::FeatureOrder</code>
 */
public interface FeatureOrderRecord extends MappedRecord {
    
    /**
     * A feature order record's name
     */
    String NAME = "org:openmdx:kernel:FeatureOrder";
    
    /**
     * Retrieve the sort order.
     *
     * @return Returns the sort order.
     */
    SortOrder getSortOrder();
    
    /**
     * Retrieve the name of the feature, optionally followed by a pointer.
     * 
     * @return the name of the feature or a path consisting of the 
     * name followed by a pointer, e.g. an XPath or a JSONPointer.
     * 
     * @see #hasFeaturePointer()
     * @see #featureName()
     * @see #featurePointer()
     */
    String getFeature();

    /**
     * Deep clone
     * 
     * @return a clone of this record
     */
    FeatureOrderRecord clone();

    /**
     * Tells whether the feature is recognised by a name only or
     * by a name followed by a path.
     * 
     * @return <code>false</code> if the feature is identified by a name only
     */
    boolean hasFeaturePointer();
    
    /**
     * Returns the feature name in case of a feature path.
     * The same as <code>getFeature()</code> if <code>hasFeaturePointer() is <code>false</code>.
     * 
     * @return the name
     */
    String featureName();
    
    /**
     * Returns the feature pointer (starting with a '/') in case of a path,
     * or <code>null</code> if <code>hasFeaturePointer() is <code>false</code>.
     * 
     * @return the feature pointer, e.g. XPath or JSONPointer (RFC 6901)
     */
    String featurePointer();
    
    enum Member {
        feature,
        sortOrder
    }
    
}

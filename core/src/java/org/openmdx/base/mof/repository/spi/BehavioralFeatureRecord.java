/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::BehavioralFeature Record 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2016, OMEX AG, Switzerland
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
package org.openmdx.base.mof.repository.spi;

import java.util.Collection;
import java.util.List;

import javax.resource.cci.IndexedRecord;

import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::BehavioralFeature Record
 */
abstract class BehavioralFeatureRecord<M extends Enum<M>>
    extends NamespaceRecord<M>
    implements org.openmdx.base.mof.repository.cci.BehavioralFeatureRecord {

    private String visibility;
    private String scope;
    private IndexedRecord parameter;

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -1375930371308186046L;
    
    /**
     * Retrieve visibility.
     *
     * @return Returns the visibility.
     */
    @Override
    public  String getVisibility() {
        return this.visibility;
    }

    /**
     * Set visibility.
     * 
     * @param visibility The visibility to set.
     */
    protected void setVisibility(String visibility) {
        assertMutability();
        this.visibility = internalize(visibility);
    }
    
    /**
     * Retrieve scope.
     *
     * @return Returns the scope.
     */
    @Override
    public String getScope() {
        return this.scope;
    }
    
    /**
     * Set scope.
     * 
     * @param scope The scope to set.
     */
    protected void setScope(String scope) {
        assertMutability();
        this.scope = internalize(scope);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Path> getParameter(){
        return parameter();
    }
    
    protected IndexedRecord parameter(){
        if(this.parameter == null) {
            this.parameter = newList();
        }
        return this.parameter;
    }
    
    protected void setParameter(
        Collection<?> parameter
    ){
        replaceValues(parameter(), parameter);
    }
    
}

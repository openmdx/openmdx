/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::ModelElement Record
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
import java.util.Set;

import javax.resource.cci.IndexedRecord;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::GeneralizableElement Record
 */
abstract class GeneralizableElementRecord<M extends Enum<M>>
  extends NamespaceRecord<M>
  implements org.openmdx.base.mof.repository.cci.GeneralizableElementRecord
{

    /**
     * Constructor 
     */
    protected GeneralizableElementRecord() {
        super();
    }

    private IndexedRecord feature;
    private IndexedRecord supertypes;
    private IndexedRecord allSupertypes;
    private IndexedRecord subtypes;
    private IndexedRecord allSubtypes;
    private boolean isAbstract;
    private String visibility;
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7868622340563182666L;

    
    //------------------------------------------------------------------------
    // Implements org::omg::model1::GeneralizableElement
    //------------------------------------------------------------------------
    
    /**
     * Retrieve isAbstract.
     *
     * @return Returns the isAbstract.
     */
    @Override
    public boolean isAbstract() {
        return this.isAbstract;
    }
    
    /**
     * Set isAbstract.
     * 
     * @param isAbstract The isAbstract to set.
     */
    protected void setAbstract(Boolean isAbstract) {
        assertMutability();
        this.isAbstract = Boolean.TRUE.equals(isAbstract);
    }
    
    /**
     * Retrieve visibility.
     *
     * @return Returns the visibility.
     */
    @Override
    public String getVisibility() {
        return this.visibility;
    }
    
    /**
     * Set visibility.
     * 
     * @param visibility The visibility to set.
     */
    protected void setVisibility(String visibility) {
        assertMutability();
        this.visibility = visibility;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Path> getSupertypes(){
        return Sets.asSet(supertypes());
    }
    
    protected IndexedRecord supertypes(){
        if(this.supertypes == null) {
            this.supertypes = newSet();
        }
        return this.supertypes;
    }
    
    protected void setSupertypes(
        Collection<?> supertypes
    ){
        replaceValues(supertypes(), supertypes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Path> getSubtypes(){
        return Sets.asSet(subtypes());
    }
    
    protected IndexedRecord subtypes(){
        if(this.subtypes == null) {
            this.subtypes = newSet();
        }
        return this.subtypes;
    }
    
    protected void setSubtypes(
        Collection<?> subtypes
    ){
        replaceValues(subtypes(), subtypes);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Set<Path> getAllSupertypes(){
        return Sets.asSet(allSupertypes());
    }    
    
    protected IndexedRecord allSupertypes(){
        if(this.allSupertypes == null) {
            this.allSupertypes = newSet();
        }
        return this.allSupertypes;
    }
    
    protected void setAllSupertypes(
        Collection<?> allSupertypes
    ){
        replaceValues(allSupertypes(), allSupertypes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Path> getAllSubtypes(){
        return Sets.asSet(allSubtypes());
    }    
    
    protected IndexedRecord allSubtypes(){
        if(this.allSubtypes == null) {
            this.allSubtypes = newSet();
        }
        return this.allSubtypes;
    }
    
    protected void setAllSubtypes(
        Collection<?> allSubtypes
    ){
        replaceValues(allSubtypes(), allSubtypes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Path> getFeature(){
        return Sets.asSet(feature());
    }    
    
    protected IndexedRecord feature(){
        if(this.feature == null) {
            this.feature = newSet();
        }
        return this.feature;
    }
    
    protected void setFeature(
        Collection<?> feature
    ){
        replaceValues(feature(), feature);
    }

}

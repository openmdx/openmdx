/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::Operation Record
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::Operation Record
 */
public class OperationRecord
  extends BehavioralFeatureRecord<org.openmdx.base.mof.repository.cci.OperationRecord.Member>
  implements org.openmdx.base.mof.repository.cci.OperationRecord
{
    
    private IndexedRecord exceptions;
    private boolean query;
    private String semantics;
    
    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 5182549306411846691L;
    @Override
    public String getRecordName() {
        return NAME;
    }

    /**
     * Retrieve a value by index
     *
     * @param index the index
     * @return the value
     */
     @Override
     protected Object get(
         Member index
){
         switch(index) {
             case annotation: return getAnnotation();
             case identity: return getIdentity();
             case modifiedAt: return getModifiedAt();
             case createdBy: return createdBy();
             case stereotype: return stereotype(); 
             case createdAt: return getCreatedAt(); 
             case modifiedBy: return modifiedBy(); 
             case container: return getContainer(); 
             case name: return getName(); 
             case qualifiedName: return getQualifiedName();
             case scope: return getScope();
             case visibility: return getVisibility();
             case exception: return exceptions;
             case isQuery: return Boolean.valueOf(isQuery());
             case content: return content();
             case parameter: return parameter();
             case semantics: return getSemantics();
             default: return super.get(index);
         }
     }

    /**
     * Retrieve a value by index
     * 
     * @param index the index
     * @param value the new value
     * 
     * @return the old value
     */
     @Override
     protected void put(
         Member index,
         Object value
     ){
         assertMutability();
         switch(index) {
             case annotation:
                 setAnnotation((String)value);
                 break;
             case identity:
                 setIdentity(value);
                 break;
             case modifiedAt:
                 setModifiedAt((#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif)value);
                 break;
             case createdBy:
                 setCreatedBy((Collection<?>)value);
                 break;
             case stereotype:
                 setStereotype((Collection<?>)value);
                 break;
             case createdAt:
                 setCreatedAt((#if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif)value);
                 break;
             case modifiedBy:
                 setModifiedBy((Collection<?>)value);
                 break;
             case container:
                 setContainer((Path)value);
                 break;
             case name:
                 setName((String)value);
                 break;
             case qualifiedName:
                 setQualifiedName((String)value);
                 break;
             case scope:
                 setScope((String) value);
                 break;
             case visibility:
                 setVisibility((String) value);
                 break;
             case exception:
                 setExceptions((Collection<?>) value);
                 break;
             case isQuery:
                 setQuery((Boolean) value);
                 break;
             case content:
                 setContent((Collection<?>) value);
                 break;
             case parameter:
                 setParameter((Collection<?>) value);
                 break;
             case semantics:
                 setSemantics((String) value);
                 break;
            default:
                 super.put(index, value);
         }
    }
     
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractMappedRecord#members()
     */
    @Override
    protected org.openmdx.base.rest.spi.AbstractMappedRecord.Members<Member> members() {
        return MEMBERS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Path> getExceptions(){
        return Sets.asSet(exceptions());
    }
    
    protected IndexedRecord exceptions(){
        if(this.exceptions == null) {
            this.exceptions = newSet();
        }
        return this.exceptions;
    }
    
    protected void setExceptions(
        Collection<?> exceptions
    ){
        replaceValues(exceptions(), exceptions);
    }
    
    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
    @Override
    public boolean isQuery() {
        return this.query;
    }
    
    /**
     * Set query.
     * 
     * @param query The query to set.
     */
    protected void setQuery(Boolean query) {
        assertMutability();
        this.query = Boolean.TRUE.equals(query);
    }
    
    /**
     * Retrieve semantics.
     *
     * @return Returns the semantics.
     */
    @Override
    public String getSemantics() {
        return this.semantics;
    }
    
    /**
     * Set semantics.
     * 
     * @param semantics The semantics to set.
     */
    protected void setSemantics(String semantics) {
        assertMutability();
        this.semantics = internalize(semantics);
    }
    
    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.spi.ElementRecord#clone()
     */
    @Override
    public OperationRecord clone() {
        return prepareClone(new OperationRecord());
    }
    
}

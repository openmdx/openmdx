/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::Exception Record
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

import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::Exception Record
 */
public class ImportRecord
  extends ElementRecord<org.openmdx.base.mof.repository.cci.ImportRecord.Member>
  implements org.openmdx.base.mof.repository.cci.ImportRecord
{
    
    private boolean clustered;
    private String visibility;
    private Path importedNamespace;

    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -4540890453795651971L;
    
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
             case isClustered: return isClustered();
             case visibility: return getVisibility();
             case importedNamespace: return getImportedNamespace();
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
             case isClustered:
                 setClustered((Boolean) value);
                 break;
             case visibility:
                 setVisibility((String) value);
                 break;
             case importedNamespace:
                 setImportedNamespace((Path) value);
                 break;
            default:
                 super.put(index, value);
         }
     }
         

    @Override
    protected Members<Member> members() {
        return MEMBERS;
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.spi.ElementRecord#clone()
     */
    @Override
    public ImportRecord clone() {
        return prepareClone(new ImportRecord());
    }

    
    //------------------------------------------------------------------------
    // Implements ElementRecord
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.ImportRecord#getVisibility()
     */
    @Override
    public String getVisibility() {
        return this.visibility;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.ImportRecord#isClustered()
     */
    @Override
    public boolean isClustered() {
        return this.clustered;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.ImportRecord#getImportedNamespace()
     */
    @Override
    public Path getImportedNamespace() {
        return this.importedNamespace;
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
     * Set importedNamespace.
     * 
     * @param importedNamespace The importedNamespace to set.
     */
    protected void setImportedNamespace(Path importedNamespace) {
        assertMutability();
        this.importedNamespace = importedNamespace;
    }

    
    /**
     * Set clustered.
     * 
     * @param clustered The clustered to set.
     */
    protected void setClustered(Boolean clustered) {
        assertMutability();
        this.clustered = Boolean.TRUE.equals(clustered);
    }
    
}

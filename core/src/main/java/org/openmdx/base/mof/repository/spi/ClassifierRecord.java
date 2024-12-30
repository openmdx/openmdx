/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: org::omg::model1::Classifier Record
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

import java.util.Map;

import #if JAVA_8 javax.resource.cci.MappedRecord #else jakarta.resource.cci.MappedRecord #endif;

import org.openmdx.base.naming.Path;

/**
 * org::omg::model1::Classifier Record
 */
abstract class ClassifierRecord<M extends Enum<M>>
  extends GeneralizableElementRecord<M>
  implements org.openmdx.base.mof.repository.cci.ClassifierRecord
{

    /**
     * Constructor 
     */
    protected ClassifierRecord() {
        super();
    }

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 5268968762243168949L;

    private Path compositeReference;
    private Path type;
    private MappedRecord reference;
    private MappedRecord attribute;
    private MappedRecord field;
    private MappedRecord operation;
    
    /**
     * Retrieve compositeReference.
     *
     * @return Returns the compositeReference.
     */
    @Override
    public Path getCompositeReference() {
        return this.compositeReference;
    }

    /**
     * Set compositeReference.
     * 
     * @param compositeReference The compositeReference to set.
     */
    protected void setCompositeReference(Path compositeReference) {
        assertMutability();
        this.compositeReference = compositeReference;
    }

    
    /**
     * Retrieve type.
     *
     * @return Returns the type.
     */
    @Override
    public Path getType() {
        return this.type;
    }
    
    /**
     * Set type.
     * 
     * @param type The type to set.
     */
    protected void setType(Path type) {
        assertMutability();
        this.type = type;
    }
    
    protected MappedRecord reference(){
        if(this.reference == null){
            this.reference = newMap();
        }
        return this.reference;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String,Path> getReference(){
       return reference(); 
    }

    protected void setReference(MappedRecord value) {
        replaceValues(reference(), value);
    }
    
    protected MappedRecord attribute(){
        if(this.attribute == null){
            this.attribute = newMap();
        }
        return this.attribute;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String,Path> getAttribute(){
       return attribute(); 
    }

    protected void setAttribute(MappedRecord value) {
        replaceValues(attribute(), value);
    }

    protected MappedRecord field(){
        if(this.field == null){
            this.field = newMap();
        }
        return this.field;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String,Path> getField(){
       return field(); 
    }

    protected void setField(MappedRecord value) {
        replaceValues(field(), value);
    }

    protected MappedRecord operation(){
        if(this.operation == null){
            this.operation = newMap();
        }
        return this.operation;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String,Path> getOperation(){
       return operation(); 
    }

    protected void setOperation(MappedRecord value) {
        replaceValues(operation(), value);
    }
    
}

/*
 * ====================================================================
 * Project:     openpackCore, http://www.openmdx.org/
 * Description: org::omg::model1::Element Record
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
import java.util.Date;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;

import org.openmdx.base.collection.Sets;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.cci.SetRecord;
import org.openmdx.base.rest.spi.AbstractMappedRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;

/**
 * org::omg::model1::Element Record
 */
abstract class ElementRecord<M extends Enum<M>>
  extends AbstractMappedRecord<M>
  implements org.openmdx.base.mof.repository.cci.ElementRecord
{

    /**
     * Constructor 
     */
    protected ElementRecord() {
        super();
    }

    private Path objectId;
    private String name;
    private String qualifiedName;
    private String annotation;
    private String createdBy;
    private long createdAt;
    private String modifiedBy;
    private long modifiedAt;
    private IndexedRecord stereotype;
    private Path container;

    private final static long NULL_DATE = Long.MIN_VALUE;
    
    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -3466367625186217353L;

    
    //------------------------------------------------------------------------
    // Implements org::openmdx::base::BasicObject
    //------------------------------------------------------------------------
    
    protected String getIdentity(){
        return this.objectId == null ? null : this.objectId.toXRI();
    }
    
    protected void setIdentity(
        Object newValue
    ){
        assertMutability(); // Derived feature
        if(this.objectId == null) {
            if(newValue instanceof Path) {
                this.objectId = (Path) newValue;
            } else if (newValue instanceof String) {
                this.objectId = new Path((String)newValue);
            } else if (newValue != null){
                throw Throwables.log(
                    BasicException.initHolder(
                        new IllegalArgumentException(
                            "Invalid identity class",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("value", newValue),
                                new BasicException.Parameter("actual class", newValue.getClass().getName()),
                                new BasicException.Parameter("suported class", Path.class.getName(), String.class.getName())
                            )
                        )
                    )
                );
            }
        } else {
            final boolean matches;
            if(newValue instanceof Path) {
                matches = this.objectId.equals(newValue);
            } else if (newValue instanceof String) {
                matches = this.objectId.toXRI().equals(newValue);
            } else {
                matches = false;
            }
            if(!matches) {
                throw Throwables.log(
                    BasicException.initHolder(
                        new IllegalArgumentException(
                            "Unmodifiable identity",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("fixed", this.objectId),
                                new BasicException.Parameter("requested", newValue)
                            )
                        )
                    )
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.ElementRecord#getObjectId()
     */
    @Override
    public Path getObjectId() {
        return this.objectId;
    }

    protected Date getCreatedAt(){
        return this.createdAt == NULL_DATE ? null : new Date(this.createdAt);
    }

    protected void setCreatedAt(Date newValue){
        assertMutability();
        this.createdAt = newValue == null ? NULL_DATE : newValue.getTime();
    }

    protected IndexedRecord createdBy() {
        try {
            return Records.getRecordFactory().indexedRecordFacade(
                SetRecord.class, 
                new Supplier<Object>() {

                    @Override
                    public Object get() {
                        return createdBy;
                    }
                },
                new Consumer<Object>() {

                    @Override
                    public void accept(Object t) {
                        assertMutability();                    
                        createdBy = (String) t;
                    }
                    
                }
            );
        } catch (ResourceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }
    
    protected void setCreatedBy(
        Collection<?> createdBy
    ){
        assertMutability();
        this.createdBy = internalize(firstOfSet(createdBy));
    }
    
    protected Date getModifiedAt(){
        return this.modifiedAt == NULL_DATE ? null : new Date(this.modifiedAt);
    }

    protected void setModifiedAt(Date newValue){
        assertMutability();
        this.modifiedAt = newValue == null ? NULL_DATE : newValue.getTime();
    }

    protected IndexedRecord modifiedBy(){
        try {
            return Records.getRecordFactory().indexedRecordFacade(
                SetRecord.class,
                new Supplier<Object>() {

                    @Override
                    public Object get() {
                        return modifiedBy;
                    }
                },
                new Consumer<Object>() {

                    @Override
                    public void accept(Object t) {
                        assertMutability();                    
                        modifiedBy = (String) t;
                    }
                    
                }
            );
        } catch (ResourceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }
    
    protected void setModifiedBy(
        Collection<?> modifiedBy
    ){
        assertMutability();
        this.modifiedBy = firstOfSet(modifiedBy);
    }

    
    //------------------------------------------------------------------------
    // Implements org::omg::model1::Element
    //------------------------------------------------------------------------
    
    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getStereotype(){
        return Sets.asSet(stereotype());
    }
    
    protected IndexedRecord stereotype(){
        if(this.stereotype == null) {
            this.stereotype = newSet();
        }
        return this.stereotype;
    }
    
    protected void setStereotype(
        Collection<?> stereotype
    ){
        replaceValues(stereotype(), stereotype);
    }

    @Override
    public String getQualifiedName(){
        return this.qualifiedName;
    }
    
    protected void setQualifiedName(String qualifiedName) {
        assertMutability();
        this.qualifiedName = qualifiedName;
    }
    
    @Override
    public Path getContainer(){
        return this.container;
    }
    
    protected void setContainer(Path container) {
        assertMutability();
        this.container = container;
    }
    
    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String newValue) {
        assertMutability();
        this.name = newValue;
    }
    
    @Override
    public String getAnnotation() {
        return this.annotation;
    }

    public void setAnnotation(String newValue) {
        this.annotation = newValue;
    }

    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.rest.spi.AbstractMappedRecord#clone()
     */
    @Override
    public abstract ElementRecord<M> clone();

    protected <T extends ElementRecord<M>> T prepareClone(T that) {
        that.putAll(this);
        return that;
    }
    
}

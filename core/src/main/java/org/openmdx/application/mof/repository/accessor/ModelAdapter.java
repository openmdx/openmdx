/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ModelAdapter 
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

package org.openmdx.application.mof.repository.accessor;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.repository.cci.ClassifierRecord;
import org.openmdx.base.mof.repository.cci.ElementRecord;
import org.openmdx.base.mof.repository.cci.Repository;
import org.openmdx.base.mof.repository.cci.TypedElementRecord;
import org.openmdx.base.naming.Path;


/**
 * ModelAdapter
 *
 */
public class ModelAdapter implements Repository {

    /**
     * Constructor 
     *
     * @param model
     */
    ModelAdapter(final Model_1_0 model) {
        this.model = model;
    }

    private final Model_1_0 model;
    
    private Set<ElementRecord> content;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.Repository#getContent()
     */
    @Override
    public Set<ElementRecord> getContent() {
        if(this.content == null) {
            this.content = new AbstractSet<ElementRecord>() {

                private final Collection<ModelElement_1_0> contentDelegate = ModelAdapter.this.model.getContent();
                
                @Override
                public Iterator<ElementRecord> iterator() {
                    return new Iterator<ElementRecord>() {

                        private final Iterator<ModelElement_1_0> iteratorDelegate = contentDelegate.iterator();
                        
                        @Override
                        public boolean hasNext() {
                            return iteratorDelegate.hasNext();
                        }

                        @Override
                        public ElementRecord next() {
                            return iteratorDelegate.next().getDelegate();
                        }
                        
                    };
                }

                @Override
                public int size() {
                    return this.contentDelegate.size();
                }
                
            };
        }
        return this.content;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.Repository#getElement(org.openmdx.base.naming.Path)
     */
    @Override
    public ElementRecord getElement(Path xri) throws ServiceException {
        return this.model.getElement(xri).getDelegate();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.Repository#getElement(java.lang.String)
     */
    @Override
    public ElementRecord getElement(String qualifiedName) throws ServiceException {
        return this.model.getElement(qualifiedName).getDelegate();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.Repository#getType(org.openmdx.base.mof.repository.cci.TypedElementRecord)
     */
    @Override
    public ClassifierRecord getElementType(TypedElementRecord element)
        throws ServiceException {
        return getDereferencedType(element.getType());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.Repository#getElementType(org.openmdx.base.mof.repository.cci.ClassifierRecord)
     */
    @Override
    public ClassifierRecord getElementType(ClassifierRecord element)
        throws ServiceException {
        return getDereferencedType(element.getType());
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.mof.repository.cci.Repository#getDereferencedType(org.openmdx.base.naming.Path)
     */
    @Override
    public ClassifierRecord getDereferencedType(Path typeId)
        throws ServiceException {
        return (ClassifierRecord) this.model.getDereferencedType(typeId).getDelegate();
    }
    
}

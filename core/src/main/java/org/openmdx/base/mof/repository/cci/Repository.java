/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Repository 
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

package org.openmdx.base.mof.repository.cci;

import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Repository
 */
public interface Repository {

    /**
     * Provide the MOF repository's content
     *  
     * @return the MOF repository's content
     */
    Set<ElementRecord> getContent();
    
    /**
     * Retrieve an element from the MOF repository
     * 
     * @param xri the model element's object id
     * 
     * @return the model element
     * 
     * @throws ServiceException if model element not found.
     */
    ElementRecord getElement(Path xri) throws ServiceException;
    
    /**
     * Retrieve an element from the MOF repository
     * 
     * @param xri the model element's qualified name
     * 
     * @return the model element
     * 
     * @throws ServiceException if model element not found.
     */
    ElementRecord getElement(String qualifiedName) throws ServiceException;

    /**
     * Provides the element's dereferenced type
     * 
     * @param element the element to be inspected
     * 
     * @return the element's dereferenced type
     */
    ClassifierRecord getElementType(TypedElementRecord element) throws ServiceException;

    /**
     * Provides the element's dereferenced type
     * 
     * @param element the element to be inspected
     * 
     * @return the element's dereferenced type
     */
    ClassifierRecord getElementType(ClassifierRecord element) throws ServiceException;
    
    /**
     * Dereferences a type
     * 
     * @param typeId the id of type to be dereferenced
     * 
     * @return the dereferenced type
     */
    ClassifierRecord getDereferencedType(Path typeId) throws ServiceException;

}

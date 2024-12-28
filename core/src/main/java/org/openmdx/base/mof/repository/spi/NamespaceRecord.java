/*
 * ====================================================================
 * Project:     openpackCore, http://www.openmdx.org/
 * Description: org::omg::model1::Namespace Record
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
 * org::omg::model1::Namespace Record
 */
abstract class NamespaceRecord<M extends Enum<M>>
  extends ElementRecord<M>
  implements org.openmdx.base.mof.repository.cci.NamespaceRecord
{

    /**
     * Constructor 
     */
    protected NamespaceRecord() {
        super();
    }

    private IndexedRecord content;

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -5529552168142280814L;
    
    @Override
    @SuppressWarnings("unchecked")
    public Set<Path> getContent(){
        return Sets.asSet(content());
    }
    
    protected IndexedRecord content(){
        if(this.content == null) {
            this.content = newSet();
        }
        return this.content;
    }
    
    protected void setContent(
        Collection<?> content
    ){
        replaceValues(content(), content);
    }

}

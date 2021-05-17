/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Model Element Marshaller 
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
package org.openmdx.application.mof.repository.accessor;

import java.util.Map;

import org.openmdx.base.marshalling.TypeSafeMarshaller;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * Model Element Marshaller
 */
public class ModelElementMarshaller implements TypeSafeMarshaller<Path, ModelElement_1_0> {

    public ModelElementMarshaller(Map<String, ModelElement_1_0> modelElements) {
        this.modelElements = modelElements;
    }

    /**
     * The repository's eagerly populated content
     */
    private final Map<String,ModelElement_1_0> modelElements;
    
    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.TypeSafeMarshaller#marshal(java.lang.Object)
     */
    @Override
    public ModelElement_1_0 marshal(Path source) {
        if(source == null) {
            return null;
        } else {
            final String qualifiedName = toQualifiedName(source);
            final ModelElement_1_0 element = this.modelElements.get(qualifiedName);
            if(element == null) {
                throw BasicException.initHolder(
                    new IllegalArgumentException(
                        "No model element with the XRI " + source,
                        BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        new BasicException.Parameter(BasicException.Parameter.XRI,source),
                        new BasicException.Parameter("qualifiedName",qualifiedName)
                        )
                    )
                );
            }
            return element;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.TypeSafeMarshaller#unmarshal(java.lang.Object)
     */
    @Override
    public Path unmarshal(ModelElement_1_0 source) {
        return source == null ? null : source.jdoGetObjectId();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.TypeSafeMarshaller#asUnmarshalledValue(java.lang.Object)
     */
    @Override
    public Path asUnmarshalledValue(Object value) {
        return value instanceof Path ? (Path) value : null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.TypeSafeMarshaller#asMarshalledValue(java.lang.Object)
     */
    @Override
    public ModelElement_1_0 asMarshalledValue(Object value) {
        return value instanceof ModelElement_1_0 ? (ModelElement_1_0) value : null;
    }

    /**
     * Extract the MOF id from the object id
     */
    static String toQualifiedName(Path xri) {
        return xri.getLastSegment().toClassicRepresentation();
    }

}

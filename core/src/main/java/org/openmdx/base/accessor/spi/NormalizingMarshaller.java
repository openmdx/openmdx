/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: NormalizingMarshaller 
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
package org.openmdx.base.accessor.spi;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.marshalling.ReluctantUnmarshalling;
import org.openmdx.kernel.exception.BasicException;
#if CLASSIC_CHRONO_TYPES
import org.w3c.cci2.ImmutableDatatype;
#endif
import org.w3c.spi2.Datatypes;

/**
 * NormalizingMarshaller
 */
public class NormalizingMarshaller 
    implements Marshaller, ReluctantUnmarshalling 
{

    /**
     * Constructor 
     */
    protected NormalizingMarshaller(
        Class<?> targetClass
    ) {
        this.targetClass = targetClass;
    }

    /**
     * Define the marshaller's target class
     */
    private final Class<?> targetClass;
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source
    ) throws ServiceException {
        return normalize(source);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return normalize(source);
    }

    /**
     * Normalize the source value to the targetClass
     * 
     * @param source
     * 
     * @return the normalized value
     */
    protected Object normalize(
        Object source
    ) throws ServiceException {
        if(source == null) {
            return null;
        } else if(this.targetClass.isInstance(source)) {
            return #if source CLASSIC_CHRONO_TYPES instanceof ImmutableDatatype<?> ? ((ImmutableDatatype<?>)source).clone() : #endif source;
        } else if (source instanceof String) {
            return Datatypes.create(this.targetClass, (String)source);
        } else if (source instanceof Number){
            return normalize((Number)source);
        } else {
            throw newServiceException(null, source);
        }
    }

    /**
     * Normalize numbers not being an instance of the target class
     * 
     * @param source
     * 
     * @return an instance of the target class
     */
    protected Object normalize(
        Number source
    ) throws ServiceException {
        throw newServiceException(null, source);
    }
    
    /**
     * Creates a transformation exception
     * 
     * @param exception
     * @param source
     * 
     * @return a transformation exception
     */
    protected ServiceException newServiceException(
        Exception exception,
        Object source
    ){
        return new ServiceException(
            exception,
            BasicException.Code.DEFAULT_DOMAIN, 
            BasicException.Code.TRANSFORMATION_FAILURE, 
            "Could not normalize " + this.targetClass.getSimpleName(),
            new BasicException.Parameter("target class", this.targetClass.getName()),
            new BasicException.Parameter("source class", source.getClass().getName()),
            new BasicException.Parameter("source value", source)
        );
    }

}

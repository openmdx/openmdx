/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Standard Marshaller Provider 
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.marshalling.MarshallerProvider;
import org.openmdx.base.mof.cci.PrimitiveTypes;


/**
 * StandardMarshallerProvider
 */
public class StandardPrimitiveTypeMarshallerProvider implements MarshallerProvider {

    /**
     * Constructor 
     */
    private StandardPrimitiveTypeMarshallerProvider() {
        super();
    }

    /**
     * An instance
     */
    private static final MarshallerProvider INSTANCE = new StandardPrimitiveTypeMarshallerProvider();
    
    /**
     * Retrieve an instance
     * 
     * @return an instance
     */
    public static MarshallerProvider getInstance(){
        return INSTANCE;
    }
    
    private static final List<String> NO_MARSHALLING_REQUIRED = Arrays.asList(
        PrimitiveTypes.STRING,
        PrimitiveTypes.BOOLEAN,
        PrimitiveTypes.BINARY
    );
    
    /* (non-Javadoc)
     * @see org.openmdx.base.marshalling.MarshallerProvider#getMarshaller(java.lang.String)
     */
    @Override
    public Optional<Marshaller> getMarshaller(String qualifiedTypeName) {
        return Optional.ofNullable(
            NO_MARSHALLING_REQUIRED.contains(qualifiedTypeName) ? IdentityMarshaller.INSTANCE :
            PrimitiveTypes.DATETIME.equals(qualifiedTypeName) ? DateTimeMarshaller.NORMALIZE :
            PrimitiveTypes.DATE.equals(qualifiedTypeName) ? DateMarshaller.NORMALIZE :
            PrimitiveTypes.ANYURI.equals(qualifiedTypeName) ? URIMarshaller.NORMALIZE :
            PrimitiveTypes.DURATION.equals(qualifiedTypeName) ? DurationMarshaller.NORMALIZE :
            PrimitiveTypes.SHORT.equals(qualifiedTypeName) ? ShortMarshaller.NORMALIZE :
            PrimitiveTypes.INTEGER.equals(qualifiedTypeName) ? IntegerMarshaller.NORMALIZE :
            PrimitiveTypes.LONG.equals(qualifiedTypeName) ? LongMarshaller.NORMALIZE :
            PrimitiveTypes.DECIMAL.equals(qualifiedTypeName) ? DecimalMarshaller.NORMALIZING :
            null
        );
    }

}

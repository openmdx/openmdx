/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Immutable Primitive Type Parser 
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
package org.openmdx.base.text.parsing;

import java.util.Arrays;
import java.util.Collection;

import #if CLASSIC_CHRONO_TYPES javax.xml.datatype #else java.time #endif.Duration;

import org.openmdx.base.naming.Path;
import org.openmdx.kernel.text.parsing.AbstractParser;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.spi.DatatypeFactories;

/**
 * Immutable Primitive Type Parser
 */
public class ImmutablePrimitiveTypeParser extends AbstractParser {

    /**
     * Constructor 
     */
    private ImmutablePrimitiveTypeParser() {
        super();
    }

    /**
     * An instance
     */
    private static final Parser INSTANCE = new ImmutablePrimitiveTypeParser();

    /**
     * The supported types
     */
    private static final Collection<Class<?>> SUPPORTED_TYPES = Arrays.asList(
        #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif.class,
        Duration.class,
        #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif.class,
        Path.class
    );
    
    /**
     * Retrieve an instance
     * 
     * @return an instance
     */
    public static Parser getInstance(){
        return INSTANCE;
    }
        
    
    

    /* (non-Javadoc)
     * @see org.openmdx.kernel.text.parsing.AbstractParser#supportedTypes()
     */
    @Override
    protected Collection<Class<?>> supportedTypes() {
        return SUPPORTED_TYPES;
    }




    /* (non-Javadoc)
	 * @see org.openmdx.kernel.text.parse.AbstractParser#parseAs(java.lang.Class, java.lang.String)
	 */
	@Override
	protected Object parseAs(
		String externalRepresentation, 
		Class<?> valueClass
	) throws Exception {
		return 
    		valueClass == #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif.class ? DatatypeFactories.immutableDatatypeFactory().newDateTime(externalRepresentation) :
    		valueClass == Duration.class ? DatatypeFactories.immutableDatatypeFactory().newDuration(externalRepresentation) :
    		valueClass == #if CLASSIC_CHRONO_TYPES javax.xml.datatype.XMLGregorianCalendar #else java.time.LocalDate#endif.class ? DatatypeFactories.immutableDatatypeFactory().newDate(externalRepresentation) :
    		valueClass == Path.class ? new Path(externalRepresentation) :
    		super.parseAs(externalRepresentation, valueClass);
	}
    
}

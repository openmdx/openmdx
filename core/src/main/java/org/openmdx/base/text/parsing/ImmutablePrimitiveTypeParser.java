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

import org.openmdx.base.naming.Path;
import org.openmdx.kernel.text.parsing.AbstractParser;
import org.openmdx.kernel.text.spi.Parser;
import org.w3c.spi2.Datatypes;

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
        Datatypes.DATE_TIME_CLASS,
        Datatypes.DURATION_CLASS,
        Datatypes.DATE_CLASS,
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
    		valueClass == Datatypes.DATE_TIME_CLASS ? Datatypes.DATATYPE_FACTORY.newDateTime(externalRepresentation) :
                valueClass == Datatypes.DURATION_CLASS ? Datatypes.DATATYPE_FACTORY.newDuration(externalRepresentation) :
                    valueClass == Datatypes.DATE_CLASS ? Datatypes.DATATYPE_FACTORY.newDate(externalRepresentation) :
    		            valueClass == Path.class ? new Path(externalRepresentation) :
                            super.parseAs(externalRepresentation, valueClass);
	}
    
}

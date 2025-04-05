/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Datatype Factories
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
package org.w3c.spi;

import javax.xml.datatype.DatatypeFactory;
import org.w3c.cci2.MutableDatatypeFactory;

/**
 * DatatypeFactories
 */
public class DatatypeFactories {

    /**
     * Constructor
     */
    private DatatypeFactories() {
        // Avoid instantiation
    }

    #if CLASSIC_CHRONO_TYPES

    /**
     * The XML Datatype Factory is lazily initialized
     */
    private static ImmutableDatatypeFactory immutableFactory;


    /**
     * Retrieve an Immutable Datatype Factory
     *
     * @return an Immutable Datatype Factory instance
     */
    public static ImmutableDatatypeFactory immutableDatatypeFactory(
    ){
        if(immutableFactory == null) {
            immutableFactory = new AlternativeDatatypeFactory();
        }
        return immutableFactory;
    }

    #endif

    /**
     * Retrieve an XML Datatype Factory
     *
     * @return an XML Datatype Factory instance
     */
    public static DatatypeFactory xmlDatatypeFactory(
    ){
        return MutableDatatypeFactory.xmlDatatypeFactory();
    }

    /**
     * Retrieve a new XML Duration
     *
     * @return a new XML Duration
     */
    public static javax.xml.datatype.Duration toPeriod() {
        return xmlDatatypeFactory().newDuration();
    }

    public static ContemporaryChronoDatatypeFactory contemporaryChronoDatatypeFactory() {
        return new DefaultContemporaryChronoDatatypeFactory();
    }

}

/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: $
 * Description: StandardPrimitiveTypeParser 
 * Revision:    $Revision: $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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

package org.w3c.spi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.exception.BasicException;


/**
 * StandardPrimitiveTypeParser
 *
 */
public class StandardPrimitiveTypeParser implements Parser {

    /**
     * Constructor 
     */
    private StandardPrimitiveTypeParser() {
        super();
    }

    /**
     * An instance
     */
    private static final Parser INSTANCE = new StandardPrimitiveTypeParser();

    /**
     * The supported types
     */
    private static final List<Class<?>> SUPPORTED_TYPES = Arrays.<Class<?>>asList(
        Long.class,
        Integer.class,
        Short.class,
        String.class,
        Boolean.class,
        BigDecimal.class,
        BigInteger.class,
        URI.class,
        Date.class,
        UUID.class,
        Duration.class,
        XMLGregorianCalendar.class,
        Oid.class
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
     * @see org.w3c.spi.Parser#handles(java.lang.Class)
     */
    @Override
    public boolean handles(Class<?> type) {
        return SUPPORTED_TYPES.contains(type);
    }

    /* (non-Javadoc)
     * @see org.w3c.spi.Parser#parse(java.lang.Class, java.lang.String)
     */
    @Override
    public <T> T parse(Class<T> valueClass, String string) {
        if (string == null && handles(valueClass)) return null;
        if (valueClass == Long.class) return valueClass.cast(Long.valueOf(string));
        if (valueClass == Integer.class) return valueClass.cast(Integer.valueOf(string));
        if (valueClass == Short.class) return valueClass.cast(Short.valueOf(string));
        if (valueClass == String.class) return valueClass.cast(string);
        if (valueClass == Boolean.class) return valueClass.cast(Boolean.valueOf(string));
        if (valueClass == BigDecimal.class) return valueClass.cast(new BigDecimal(string));
        if (valueClass == BigInteger.class) return valueClass.cast(new BigInteger(string));
        if (valueClass == URI.class) return valueClass.cast(URI.create(string));
        if (valueClass == Date.class) return valueClass.cast(DatatypeFactories.immutableDatatypeFactory().newDateTime(string));
        if (valueClass == UUID.class) return valueClass.cast(UUIDConversion.fromString(string));
        if (valueClass == Duration.class) return valueClass.cast(DatatypeFactories.immutableDatatypeFactory().newDuration(string));
        if (valueClass == XMLGregorianCalendar.class) return valueClass.cast(DatatypeFactories.immutableDatatypeFactory().newDate(string));
        if (valueClass == Oid.class) return valueClass.cast(parseOid(string));
        throw new IllegalArgumentException(valueClass.getName() + " is not supported by " + getClass().getName());
    }

    private static Oid parseOid(
        String oid
    ) {
        try {
            return new Oid(oid);
        } catch (GSSException exception) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Unable to parse the given OID value",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.PARSE_FAILURE,
                        new BasicException.Parameter("class", Oid.class.getName()),
                        new BasicException.Parameter("value", oid)
                    )
                )
            );
        }
    }
            
}

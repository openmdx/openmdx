/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DatatypeFactories.java,v 1.2 2009/03/31 17:05:17 hburger Exp $
 * Description: DatatypeFactories 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/31 17:05:17 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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


import java.util.logging.Level;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.LoggerFactory;

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

    /**
     * The XML Datatype Factory 
     */
    private static final DatatypeFactory xmlFactory;

    /**
     * The XML Datatype Factory Acquisition Exception
     */
    private static final DatatypeConfigurationException xmlException;
    
    /**
     * The XML Datatype Factory is immediately initialized
     */
    private static final ImmutableDatatypeFactory immutablFactory  = new AlternativeDatatypeFactory();
    
    /**
     * Retrieve an XML Datatype Factory
     * 
     * @return an XML Datatype Factory instance
     */
    public static DatatypeFactory xmlDatatypeFactory(
    ){
        if(xmlException == null) {
            return xmlFactory;
        } else {
            throw new RuntimeServiceException(
                xmlException,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Datatype factory acquisition failure"
            );
        }
    }

    /**
     * Retrieve an Immutable Datatype Factory
     * 
     * @return an Immutable Datatype Factory instance
     */
    public static ImmutableDatatypeFactory immutableDatatypeFactory(
    ){  
        return immutablFactory;
    }

    static {
        DatatypeFactory factory = null;
        DatatypeConfigurationException failure = null;
        try {
            factory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException exception) {
            failure = exception;
            LoggerFactory.getLogger().log(
                Level.SEVERE,
                "XML Datatype Factory Acquisition Failure",
                failure
            );
        }
        xmlFactory = factory;
        xmlException = failure;
    }

}

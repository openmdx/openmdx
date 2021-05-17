/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: class TestBasicException.Code 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package test.openmdx.kernel.exception;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import org.openmdx.application.mof.cci.ModelExceptions;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

public class TestBasicException {

    /**
     * Test the resource bundle 
     */
    @Test
    public void testIntegerConstants() {
        //
        // DefaultDomain.ACTIVATION_FAILURE
        //
        assertEquals(
            "ACTIVATION_FAILURE",
            BasicException.Code.toString(BasicException.Code.DEFAULT_DOMAIN, BasicException.Code.ACTIVATION_FAILURE)
        );
        assertEquals(
            "ACTIVATION_FAILURE",
            BasicException.Code.toString(ModelExceptions.MODEL_DOMAIN, BasicException.Code.ACTIVATION_FAILURE)
        );
        //
        // ModelDomain.INVALID_MULTIPLICITY_FORMAT
        //
        assertEquals(
            "INVALID_MULTIPLICITY_FORMAT",
            BasicException.Code.toString(ModelExceptions.MODEL_DOMAIN, ModelExceptions.INVALID_MULTIPLICITY_FORMAT)
        );
    }
    
    /**
     * Test mapping and logging
     */
    @Test
    public void testMapAndLog() {
        new ServiceException(
            new SQLException(
                "The application requester is unable to establish the connection.",
                "08001",
                -30080
            ),
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.MEDIA_ACCESS_FAILURE,
            "Database access failure"
        ).log();
    }

}

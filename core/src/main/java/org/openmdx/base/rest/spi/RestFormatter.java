/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Formatter
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
package org.openmdx.base.rest.spi;

import java.io.ObjectOutput;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
#endif

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.MessageRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.kernel.exception.BasicException;

/**
 * REST Formatter
 */
public interface RestFormatter {

    /**
     * Provide a {@code format()} target
     * 
     * @param source the {@code ObjectOutput}
     * 
     * @return a {@code Target}
     */
    Target asTarget(
        final ObjectOutput output
    );
    
    /**
     * Format Object. Serialize null values.
     * 
     * @param target
     * @param source
     * 
     * @throws ServiceException
     */
    void format(
        Target target, 
        ObjectRecord source
    ) throws ResourceException;

    /**
     * Format Object
     * 
     * @param target
     * @param source
     * @param serializeNulls
     * 
     * @throws ServiceException
     */
    void format(
        Target target, 
        ObjectRecord source,
        boolean serializeNulls
    ) throws ResourceException;

    /**
     * Format Query
     * 
     * @param target
     * @param source
     * 
     * @throws ServiceException
     */
    void format(
        Target target, 
        QueryRecord source
    ) throws ResourceException;

    /**
     * Format Result Set. Serialize null values.
     * 
     * @param target
     * @param xri
     * @param source
     * 
     * @throws ServiceException
     */
    void format(
        Target target, 
        Path xri, 
        IndexedRecord source
    ) throws ResourceException;

    /**
     * Format Result Set
     * 
     * @param target
     * @param xri
     * @param source
     * @param serializeNulls
     * 
     * @throws ServiceException
     */
    void format(
        Target target, 
        Path xri, 
        IndexedRecord source,
        boolean serializeNulls
    ) throws ResourceException;

    /**
     * Format Operation
     * 
     * @param target
     * @param id the (optional) id
     * @param source
     * @throws ServiceException
     */
    void format(
        Target target, 
        String id, 
        MessageRecord source
    ) throws ResourceException;

    /**
     * Format Operation
     * 
     * @param target
     * @param id the (optional) id
     * @param source
     * @param serializeNulls
     * @throws ServiceException
     */
    void format(
        Target target, 
        String id, 
        MessageRecord source,
        boolean serializeNulls
    ) throws ResourceException;

    /**
     * Print Exception
     * 
     * @param target
     * @param exception
     * @throws ServiceException
     */
    void format(
        Target target, 
        BasicException source
    );

}

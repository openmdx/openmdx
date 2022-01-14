/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Data Types
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

package org.openmdx.base.dataprovider.layer.persistence.jdbc.spi;

import java.sql.Connection;

import org.openmdx.base.exception.ServiceException;

/**
 * Date Types 
 */
public interface DataTypes {

    /**
     * Retrieves the configured date time type
     * 
     * @param connection
     * 
     * @return the date time type to be used
     * 
     * @throws ServiceException if meta data retrieval fails
     */
    String getDateTimeType(
        Connection connection
    ) throws ServiceException;

    /**
     * Retrieves the configured date type
     * 
     * @param connection
     * 
     * @return the date type to be used
     * 
     * @throws ServiceException if meta data retrieval fails
     */
    String getDateType(
        Connection connection
    ) throws ServiceException;
    
    /**
     * Retrieves the configured time type
     * 
     * @param connection
     * 
     * @return the time type to be used
     * 
     * @throws ServiceException if meta data retrieval fails
     */
    String getTimeType(
        Connection connection
    ) throws ServiceException;

    /**
     * Retrieves the configured boolean type
     * 
     * @param connection
     * 
     * @return the boolean type to be used
     * 
     * @throws ServiceException if meta data retrieval fails
     */
    String getBooleanType(
        Connection connection
    ) throws ServiceException;

}

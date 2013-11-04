/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Sliced DB Object 2 Without Index
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011-2013, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * This class implements a non-indexed sliced db object. If indexing
 * is not required then this type may improve performance on queries
 * dramatically. This is often the case with user-defined views.
 */
public class SlicedDbObject2NonIndexed extends SlicedDbObject2 {

    /**
     * Constructor 
     *
     * @param database
     * @param conn
     * @param dbObjectConfiguratioin
     * @param accessPath
     * @param isExtent
     * @param isQuery
     * @throws ServiceException
     */
    public SlicedDbObject2NonIndexed(
        AbstractDatabase_1 database, 
        Connection conn,
        DbObjectConfiguration dbObjectConfiguratioin, 
        Path accessPath,
        boolean isExtent,
        boolean isQuery
        ) throws ServiceException {
        super(
            database, 
            conn, 
            dbObjectConfiguratioin, 
            accessPath, 
            isExtent,
            isQuery
        );
        // no index column for non-indexed sliced DB objects
        this.indexColumn = null;
        this.excludeAttributes.add("objectIdx");
    }

    /**
     * Constructor 
     *
     * @param database
     * @param conn
     * @param dbObjectConfiguration
     * @throws ServiceException
     */
    public SlicedDbObject2NonIndexed(
        AbstractDatabase_1 database, 
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration
    ) throws ServiceException {
        super(
            database,
            conn, 
            dbObjectConfiguration
        );
        // no index column for non-indexed sliced DB objects
        this.indexColumn = null;
        this.excludeAttributes.add("objectIdx");
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3257849870223226167L;

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.SlicedDbObject#createObjectSlice(int, java.lang.String, javax.resource.cci.MappedRecord)
     */
    @Override
    public void createObjectSlice(
        int index,
        String objectClass,
        MappedRecord object
    ) throws ServiceException {
        if(index == 0) {
            super.createObjectSlice(index, objectClass, object);
        } else {
            Model_1Factory.getModel(true);
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CARDINALITY,
                "TRY_TO_FORGET: Indexed row erroneously requested from " + getClass().getName(),
                new BasicException.Parameter("index", index),
                new BasicException.Parameter(SystemAttributes.OBJECT_CLASS, objectClass),
                new BasicException.Parameter("value", Object_2Facade.getValue(object))
            );
        }
    }

    @Override
    public int getIndex(
        FastResultSet frs
    ) throws SQLException {
        return 0;
    }

}

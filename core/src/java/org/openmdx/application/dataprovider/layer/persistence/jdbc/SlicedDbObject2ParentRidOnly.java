/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: SlicedDbObjectParentRidOnly class
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Rows of this type do not contain the column object_rid. Instead, the rows
 * contain the columns parent_object__rid and parent_object__oid. This db 
 * object type is used when the calculation of object_rid is expensive
 * compared to the calculation of the object_parent columns.
 */
public class SlicedDbObject2ParentRidOnly extends SlicedDbObject2 {

    //-------------------------------------------------------------------------
    public SlicedDbObject2ParentRidOnly(
        AbstractDatabase_1 database,
        Connection conn, 
        DbObjectConfiguration typeConfiguration
    ) throws ServiceException {
        super(
            database, 
            conn, 
            typeConfiguration
        );
        String name = this.database.getPrivateAttributesPrefix() + "object_parent_";
        this.columnNameParentRid = this.database.toRid(name);            
        this.columnNameParentOid = this.database.toOid(name);
    }
    
    //-------------------------------------------------------------------------
    public SlicedDbObject2ParentRidOnly(
        AbstractDatabase_1 database,
        Connection conn, 
        DbObjectConfiguration typeConfigurationEntry,
        Path accessPath, 
        boolean isExtent, 
        boolean isQuery
    ) throws ServiceException {
        super(
            database, 
            conn, 
            typeConfigurationEntry, 
            accessPath, 
            isExtent,
            isQuery
        );
        String name = this.database.getPrivateAttributesPrefix() + "object_parent_";
        this.columnNameParentRid = this.database.toRid(name);            
        this.columnNameParentOid = this.database.toOid(name);
        if(
            (
                !isExtent ||
                this.getReference().isLike(INVOLVEMENT_REFERENCE_PATTERN) // TODO make it more general!
                
            ) && ( 
                accessPath.isLike(typeConfigurationEntry.getType()) || 
                (accessPath.size() % 2 == 0 && accessPath.isLike(typeConfigurationEntry.getType().getParent()))
            )
        ) {
            // get rid|oid of parent object and construct reference clause
            this.getReferenceValues().clear();
            // parent object rid
            Path parentResourceIdentifier = this.getReference().getParent();
            this.getReferenceValues().add(
                this.database.getReferenceId(
                    conn, 
                    parentResourceIdentifier.getParent(), 
                    false
                )
            );
            if(":*".equals(parentResourceIdentifier.getBase())) {
                this.referenceClause = 
                    "(v." + columnNameParentRid + " IN (?))";           
            } else {
                // parent object oid
                this.getReferenceValues().add(
                    this.database.getObjectId(
                        parentResourceIdentifier.getBase()
                    )
                );
                this.referenceClause = 
                    "(v." + columnNameParentRid + " IN (?)) AND " +
                    "(v." + columnNameParentOid + " IN (?))";           
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.SlicedDbObject#getObjectReference(org.openmdx.application.dataprovider.layer.persistence.jdbc.FastResultSet)
     */
    @Override
    public Path getObjectReference(FastResultSet frs)
        throws SQLException, ServiceException {
        if(this.database.configuration.normalizeObjectIds()) {
            return this.database.configuration.buildResourceIdentifier(
                frs.getObject(columnNameParentRid).toString(), 
                true // reference
            ).getDescendant(
                frs.getObject(columnNameParentOid).toString(),
                getReference().getBase()
            );
        } else {
            return super.getObjectReference(frs);
        }
    }

    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3257566196189706291L;
    private static final Path INVOLVEMENT_REFERENCE_PATTERN = new Path(
        "xri://@openmdx*org.openmdx.audit2/provider/($..)/segment/($..)/unitOfWork/($..)/involvement"
    );
    private final String columnNameParentRid;
    private final String columnNameParentOid;
    
}

//--- End of File -----------------------------------------------------------

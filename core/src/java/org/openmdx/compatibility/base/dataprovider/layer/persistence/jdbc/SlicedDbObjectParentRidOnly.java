/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SlicedDbObjectParentRidOnly.java,v 1.10 2009/01/06 13:14:45 wfro Exp $
 * Description: SlicedDbObjectParentRidOnly class
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 13:14:45 $
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
package org.openmdx.compatibility.base.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.util.ArrayList;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Rows of this type do not contain the column object_rid. Instead, the rows
 * contain the columns parent_object__rid and parent_object__oid. This db 
 * object type is used when the calculation of object_rid is expensive
 * compared to the calculation of the object_parent columns.
 */
@SuppressWarnings("unchecked")
public class SlicedDbObjectParentRidOnly
  extends SlicedDbObject 
{

    //-------------------------------------------------------------------------
    public SlicedDbObjectParentRidOnly(
        AbstractDatabase_1 database,
        Connection conn, 
        DbObjectConfiguration typeConfiguration
    ) {
        super(
            database, 
            conn, 
            typeConfiguration
        );
    }
    
    //-------------------------------------------------------------------------
    public SlicedDbObjectParentRidOnly(
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
        if(
            !isExtent && ( 
                accessPath.isLike(typeConfigurationEntry.getType()) || 
                (accessPath.size() % 2 == 0 && accessPath.isLike(typeConfigurationEntry.getType().getParent()))
            )
        ) {
            String columnNameParentRid = this.database.toRid(
                this.database.privateAttributesPrefix + "object_parent_"
            );            
            String columnNameParentOid = this.database.toOid(
                this.database.privateAttributesPrefix + "object_parent_"
            );
            // get rid|oid of parent object and construct reference clause
            this.referenceValues = new ArrayList();
            // parent object rid
            Path parentResourceIdentifier = this.getReference().getParent();
            this.referenceValues.add(
                this.database.getReferenceId(
                    conn, 
                    parentResourceIdentifier.getParent(), 
                    false
                )
            );
            // parent object oid
            this.referenceValues.add(
                this.database.getObjectId(
                    parentResourceIdentifier.getBase()
                )
            );
            this.referenceClause = 
                "(v." + columnNameParentRid + " IN (?)) AND " +
                "(v." + columnNameParentOid + " IN (?))";           
        }
    }
    
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3257566196189706291L;

}

//--- End of File -----------------------------------------------------------

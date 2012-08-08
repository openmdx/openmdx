/*
 * ====================================================================
 * Project:     opencrx, http://www.opencrx.org/
 * Name:        $Id: SyncDatabase.java,v 1.8 2007/03/26 01:14:35 wfro Exp $
 * Description: openCRX application plugin
 * Revision:    $Revision: 1.8 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2007/03/26 01:14:35 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, CRIXP Corp., Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of CRIXP Corp. nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
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
 * 
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.openmdx.syncml.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncDatabase {

    //-----------------------------------------------------------------------
    public static class DatabaseObject {
    
        public DatabaseObject(
            String id,
            String[] values,
            Date modifiedAt
        ) {
            this.id = id;
            this.values = values;
            this.modifiedAt = modifiedAt;
            this.externalIds = new HashMap();
        }
        
        public String getId(
        ) {
            return this.id;
        }
        
        public String[] getValues(
        ) {
            return this.values;
        }
        
        public Date getModifiedAt(
        ) {
            return this.modifiedAt;
        }
        
        public String getExternalId(
            String qualifier
        ) {
            return this.externalIds.get(qualifier);
        }
        
        public String setExternalId(
            String qualifier,
            String externalId
        ) {
            return this.externalIds.put(
                qualifier,
                externalId
            );
        }
        
        public void touch(
            long modifiedAt
        ) {
            this.modifiedAt = new Date(modifiedAt);
        }
        
        private final String id;
        private final String[] values;
        private Date modifiedAt;
        private final Map<String, String> externalIds;
        
    }
    
    //-----------------------------------------------------------------------
    public SyncDatabase(
        String name
    ) {
        this.name = name;
        this.objects = new HashMap();
    }
    
    //-----------------------------------------------------------------------
    public String getName(
    ) {
        return this.name;
    }
    
    //-----------------------------------------------------------------------
    public String getDataStoreInfo(
    ) {
        return "";
    }
    
    //-----------------------------------------------------------------------
    /**
     * @return true if object with id exists and was removed
     */
    public boolean deleteObject(
        String id
    ) {
        return this.objects.remove(id) != null;
    }
    
    //-----------------------------------------------------------------------
    public DatabaseObject getObject(
        String id
    ) {
        if(id == null) return null;
        return this.objects.get(id);
    }
    
    //-----------------------------------------------------------------------
    public Collection<DatabaseObject> getModifiedObjects(
        Date startAt,
        Date endAt,
        int position
    ) {
        List<DatabaseObject> modifiedObjects = new ArrayList();
        int currentPosition = 0;
        for(DatabaseObject object : this.objects.values()) {
            if(
                (object.getModifiedAt().compareTo(startAt) > 0) &&
                (object.getModifiedAt().compareTo(endAt) <= 0)
            ) {
                if(currentPosition >= position) {
                    modifiedObjects.add(object);
                }
                currentPosition++;
            }
        }
        return modifiedObjects;
    }

    //-----------------------------------------------------------------------
    public DatabaseObject putObject(
        String id,
        String[] values
    ) {        
        DatabaseObject dbo = null;
        this.objects.put(
            id,
            dbo = new DatabaseObject(
                id,
                values,
                new Date()
            )
        );
        return dbo;
    }

    //-----------------------------------------------------------------------
    public DatabaseObject findObjectByExternalId(
        String qualifier,
        String externalId
    ) {
        for(DatabaseObject dbo : this.objects.values()) {
            if(
                (dbo.getExternalId(qualifier) != null) && 
                dbo.getExternalId(qualifier).equals(externalId)
            ) {
                return dbo;
            }
        }
        return null;
    }
    
    //-----------------------------------------------------------------------
    public Collection<DatabaseObject> getObjects(
    ) {
        return this.objects.values();
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private final Map<String, DatabaseObject> objects;
    private final String name;
    
}

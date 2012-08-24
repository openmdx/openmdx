/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Database Preferences 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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

import java.util.Collection;

import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;


/**
 * Database Preferences
 */
class DatabasePreferences {

    private static final Path PREFERENCES = new Path(
        "xri://@openmdx*org:openmdx:preferences2/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager/preferences"
    ).lock();
    
    private static final Path CONFIGURATION_PATTERN = PREFERENCES.getChild("%").lock();
    
    private static final Path NODES_PATTERN = PREFERENCES.getDescendant(":*", "node").lock();

    private static final Path ENTRIES_PATTERN = NODES_PATTERN.getDescendant(":*", "entry").lock();
    
    @SuppressWarnings("unchecked")
    private static ObjectRecord createNode(
        Path nodes
    ) throws ResourceException {    
        Object_2Facade node = Object_2Facade.newInstance(
            nodes.getChild("-"),
            "org:openmdx:preferences2:Node"
        );
        MappedRecord values = node.getValue();
        values.put("name", "dbObject");
        values.put("absolutePath", "/dbObject");
        return node.getDelegate();
    }
    
    @SuppressWarnings("unchecked")
    private static ObjectRecord createNode(
        ObjectRecord parent,
        DbObjectConfiguration configuration
    ) throws ResourceException {    
        String typeName = configuration.getTypeName();
        Object_2Facade node = Object_2Facade.newInstance(
            parent.getPath().getParent().getChild(typeName),
            "org:openmdx:preferences2:Node"
        );
        MappedRecord values = node.getValue();
        values.put("name", typeName);
        values.put("absolutePath", "/dbObject/" + typeName);
        values.put("parent", parent.getPath());
        return node.getDelegate();
    }
    
    @SuppressWarnings("unchecked")
    private static MappedRecord createEntry(
        Path entries,
        String name,
        String value
    ) throws ResourceException{
        Object_2Facade entry = Object_2Facade.newInstance(
            entries.getChild(name),
            "org:openmdx:preferences2:Entry"
        );
        MappedRecord values = entry.getValue();
        values.put("name", name);
        values.put("value", value);
        return entry.getDelegate();
    }

    /**
     * Tells whether the XRI refers to a configuration request
     * 
     * @param xri
     * 
     * @return <code>true</code> if the XRI refers to a configuration request
     */
    static boolean isConfigurationRequest(
        Path xri
    ){
        return xri.isLike(CONFIGURATION_PATTERN);
    }
    
    /**
     * @param xri
     * @param output
     * @param object
     * @throws ResourceException 
     */
    @SuppressWarnings("unchecked")
    static void discloseConfiguration(
        Path xri,
        IndexedRecord output,
        Collection<DbObjectConfiguration> configurations
    ) throws ServiceException {
        try {
            int count = 0;
            if(xri.isLike(NODES_PATTERN)){
                ObjectRecord parent = createNode(xri);
                output.add(parent);
                count++;
                for(DbObjectConfiguration configuration : configurations) {
                    output.add(createNode(parent, configuration));
                    count++;
                }
            } else if (xri.isLike(ENTRIES_PATTERN)) {
               String typeName = xri.get(8);
               Configurations: for(DbObjectConfiguration configuration : configurations) {
                   if(configuration.getTypeName().equals(typeName)) {
                       output.add(createEntry(xri, "dbObjectType", configuration.getType().toXRI()));
                       output.add(createEntry(xri, "dbObjectForUpdate1", configuration.getDbObjectForUpdate1()));
                       output.add(createEntry(xri, "dbObjectForUpdate2", configuration.getDbObjectForUpdate2()));
                       output.add(createEntry(xri, "dbObjectForQuery1", configuration.getDbObjectForQuery1()));
                       output.add(createEntry(xri, "dbObjectForQuery2", configuration.getDbObjectForQuery2()));
                       count = 5;
                       break Configurations;
                   }
               }
            } else throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "No preferences found",
                new BasicException.Parameter("xri", xri)
            );
            if(output instanceof ResultRecord) {
                ResultRecord resultRecord = (ResultRecord) output;
                resultRecord.setHasMore(false);
                resultRecord.setTotal(count);
            }
        } catch (ResourceException exception) {
            throw new ServiceException(exception);
        }
    }
    
}

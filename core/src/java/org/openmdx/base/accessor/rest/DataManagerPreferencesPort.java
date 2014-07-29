/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Data Manager Preferences Port 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012-2014, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Data Manager Configurations
 */
class DataManagerPreferencesPort implements Port {

    /**
     * Constructor 
     *
     * @param destinations
     * @param raw
     */
    private DataManagerPreferencesPort(
        Map<Path, Port> destinations,
        Map<String, Port> raw
    ) {
        this.destinations = destinations;
        this.raw = raw;
    }

    /**
     * 
     */
    private final Map<Path, Port> destinations;
    
    /**
     * 
     */
    private final Map<String, Port> raw;
    
    /**
     * 
     */
    private Map<Path,Map<String,ObjectRecord>> containers;

    /**
     * openMDX configuration segments
     */
    static final Path SEGMENTS_ID = new Path(
        "xri://@openmdx*org:openmdx:preferences2/provider/(@openmdx!configuration)/segment"
    );
    
    /**
     * xri://@openmdx*org:openmdx:preferences1/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager
     */
    static final Path SEGMENT_ID = SEGMENTS_ID.getChild("org.openmdx.jdo.DataManager");
    
    /**
     * xri://@openmdx*org:openmdx:preferences1/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager/($...)
     */
    static final Path EXPOSED_PATH = SEGMENT_ID.getDescendant("%");

    /**
     * xri://@openmdx*org:openmdx:preferences1/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager/preferences
     */
    static final Path PREFERENCES_ID = SEGMENT_ID.getChild("preferences"); 

    /** 
     * xri://@openmdx*org:openmdx:preferences1/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager/preferences/($..)
     */
    static final Path PREFERENCES_PATTERN = PREFERENCES_ID.getChild(":*");

    /**
     * xri://@openmdx*org:openmdx:preferences1/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager/preferences/($..)/property
     */
    static final Path PROPERTY_ID = PREFERENCES_PATTERN.getChild("property");

    /**
     * xri://@openmdx*org:openmdx:preferences1/provider/(@openmdx!configuration)/segment/org.openmdx.jdo.DataManager/preferences/($..)/property/($..)
     */
    static final Path PROPERTY_PATTERN = PROPERTY_ID.getChild(":*");
    
    /**
     * Build the preferences object lazily
     * 
     * @return the preferences objects
     * 
     * @throws ResourceException
     */
    Map<Path,Map<String,ObjectRecord>> getContainers(
        Connection connection
    ) throws ResourceException {
        if(this.containers == null) {
            this.containers = new HashMap<Path, Map<String,ObjectRecord>>();
            //
            // Segments
            //
            Map<String,ObjectRecord> segmentsContainer = new LinkedHashMap<String,ObjectRecord>();
            this.containers.put(SEGMENTS_ID, segmentsContainer);
            addSegment(segmentsContainer);
            //
            // Preferences
            //                    
            Map<String,ObjectRecord> preferencesContainer = new LinkedHashMap<String,ObjectRecord>();
            this.containers.put(PREFERENCES_ID, preferencesContainer);
            for(Map.Entry<String,Port> portEntry : raw.entrySet()) {
                Port port = portEntry.getValue();
                if(port != DataManagerPreferencesPort.this){
                    String name = portEntry.getKey();
                    Path nodesId = addPreferences(preferencesContainer, name);
                    //
                    // Nodes
                    //
                    for(Map.Entry<Path, Port> destinationEntry : destinations.entrySet()){
                        if(destinationEntry.getValue() == port) {
                            Map<String,ObjectRecord> nodesContainer = new LinkedHashMap<String,ObjectRecord>();
                            this.containers.put(nodesId, nodesContainer);
                            Record nodes = port.getInteraction(connection).execute(
                                InteractionSpecs.getRestInteractionSpecs(false).GET,
                                Query_2Facade.newInstance(nodesId).getDelegate()
                            );
                            for(Object rawNode : (IndexedRecord)nodes){
                                ObjectRecord node = (ObjectRecord) rawNode;
                                Path nodeId = node.getPath();
                                nodesContainer.put(nodeId.getLastSegment().toClassicRepresentation(), node);
                                //
                                // Entries
                                //
                                Path entriesId = nodeId.getChild("entry");
                                Map<String,ObjectRecord> entriesContainer = new LinkedHashMap<String,ObjectRecord>();
                                this.containers.put(entriesId, entriesContainer);
                                Record entries = port.getInteraction(connection).execute(
                                    InteractionSpecs.getRestInteractionSpecs(false).GET,
                                    Query_2Facade.newInstance(entriesId).getDelegate()
                                );
                                for(Object rawEntry : (IndexedRecord)entries){
                                    ObjectRecord entry = (ObjectRecord) rawEntry;
                                    Path entryId = entry.getPath();
                                    entriesContainer.put(entryId.getLastSegment().toClassicRepresentation(), entry);
                                }
                            }
                        }
                    }
                }
            }
            
        }
        return this.containers;
    }
    
    /**
     * Add a new preferences segment
     * 
     * @throws ResourceException
     */
    @SuppressWarnings("unchecked")
    static void addSegment(
        Map<String,ObjectRecord> to
    ) throws ResourceException {
        Object_2Facade object = Object_2Facade.newInstance(
            SEGMENT_ID,
            "org:openmdx:preferences2:Segment"
        );
        MappedRecord values = object.getValue();
        values.put("description", "openMDX Configuration Preferences");
        to.put(object.getPath().getLastSegment().toClassicRepresentation(), object.getDelegate());
    }

    /**
     * Add preferences and return the nodes path
     * 
     * @param to
     * @param name
     * 
     * @return the nodes path
     * 
     * @throws ResourceException
     */
    @SuppressWarnings("unchecked")
    static Path addPreferences(
        Map<String,ObjectRecord> to,
        String name
    ) throws ResourceException {
        Object_2Facade object = Object_2Facade.newInstance(
            PREFERENCES_ID.getChild(name),
            "org:openmdx:preferences2:Preferences"
        );
        MappedRecord values = object.getValue();
        values.put("type", "system");
        to.put(object.getPath().getLastSegment().toClassicRepresentation(), object.getDelegate());
        return object.getPath().getChild("node");
    }
    
    /**
     * Disclose the configuration via preferences
     * 
     * @param destinations
     * @param raw
     */
    static void discloseConfiguration(
        Map<Path, Port> destinations,
        Map<String, Port> raw
    ) {
        destinations.put(EXPOSED_PATH, new DataManagerPreferencesPort(destinations, raw));
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
//  @Override
    public Interaction getInteraction(
        Connection connection
    ) throws ResourceException {
        return new DataManagerConfigurationInteraction(connection);
    }

    
    //------------------------------------------------------------------------
    // Class DataManagerConfigurationInteraction
    //------------------------------------------------------------------------
    
    /**
     * Data Manager Configuration Interaction
     */
    class DataManagerConfigurationInteraction extends AbstractRestInteraction {

        /**
         * Constructor 
         *
         * @param connection
         */
        protected DataManagerConfigurationInteraction(Connection connection) {
            super(connection);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            Path xri = input.getPath();
            try {
                Map<String,ObjectRecord> container = getContainers(getConnection()).get(xri.getParent());
                if(container != null) {
                    ObjectRecord object = container.get(xri.getLastSegment().toClassicRepresentation());
                    if(object != null) {
                        output.add(object);
                        return true;
                    }
                }
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_FOUND,
                "No such object found among the data manager configuration preferences",
                new BasicException.Parameter("xri", input.getPath()),
                new BasicException.Parameter("position", input.getPosition()),
                new BasicException.Parameter("query", input.getQuery())
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            Path xri = input.getPath();
            try {
                if(input.getQuery() != null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_IMPLEMENTED,
                        "Queries are not yet implemented for data manager configuration preferences",
                        new BasicException.Parameter("xri", input.getPath()),
                        new BasicException.Parameter("position", input.getPosition()),
                        new BasicException.Parameter("query", input.getQuery()),
                        new BasicException.Parameter("parameters", input.getParameters())
                    );
                }
                Map<String,ObjectRecord> container = getContainers(getConnection()).get(xri);
                if(container == null){
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        "No such container faund in the data manager configuration preferences",
                        new BasicException.Parameter("xri", input.getPath()),
                        new BasicException.Parameter("position", input.getPosition()),
                        new BasicException.Parameter("query", input.getQuery()),
                        new BasicException.Parameter("parameters", input.getParameters())
                    );
                } else {
                    long skip = input.getPosition() == null ? 0 : input.getPosition().longValue();
                    for(ObjectRecord object : container.values()) {
                        if(skip-- <= 0) {
                            output.add(object);
                        }
                    }
                    if(output instanceof ResultRecord) {
                        ResultRecord resultRecord = (ResultRecord) output;
                        resultRecord.setHasMore(false);
                        resultRecord.setTotal(container.size());
                    }
                } 
            } catch (ResourceException exception) {
                throw new ServiceException(exception);
            }
            return true;
        }
       
    }
 
}
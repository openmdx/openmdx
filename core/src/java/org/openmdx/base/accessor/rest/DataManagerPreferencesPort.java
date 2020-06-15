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

import javax.jdo.FetchPlan;
import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Interaction;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.QueryRecord;
import org.openmdx.base.rest.cci.RestConnection;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Data Manager Configurations
 */
class DataManagerPreferencesPort implements Port<RestConnection> {

    /**
     * Constructor
     *
     * @param destinations
     * @param raw
     */
    private DataManagerPreferencesPort(
        Map<Path, Port<RestConnection>> destinations,
        Map<String, Port<RestConnection>> raw
    ) {
        this.destinations = destinations;
        this.raw = raw;
    }

    /**
     * 
     */
    private final Map<Path, Port<RestConnection>> destinations;

    /**
     * 
     */
    private final Map<String, Port<RestConnection>> raw;

    /**
     * 
     */
    Map<Path, Map<String, ObjectRecord>> containers;

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
     * Add a new preferences segment
     * 
     * @throws ResourceException
     */
    @SuppressWarnings("unchecked")
    static void addSegment(
        Map<String, ObjectRecord> to
    )
        throws ResourceException {
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
        Map<String, ObjectRecord> to,
        String name
    )
        throws ResourceException {
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
        Map<Path, Port<RestConnection>> destinations,
        Map<String, Port<RestConnection>> raw
    ) {
        destinations.put(EXPOSED_PATH, new DataManagerPreferencesPort(destinations, raw));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public Interaction getInteraction(
        RestConnection connection
    )
        throws ResourceException {
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
        protected DataManagerConfigurationInteraction(RestConnection connection) {
            super(connection);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#newQuery(org.openmdx.base.naming.Path)
         */
        @Override
        protected QueryRecord newQuery(Path resourceIdentifier) {
            final QueryRecord query = super.newQuery(resourceIdentifier);
            query.setSize(Long.valueOf(FetchPlan.FETCH_SIZE_GREEDY));
            return query;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        )
            throws ResourceException {
            Path xri = input.getResourceIdentifier();
            Map<String, ObjectRecord> container = getContainers(getConnection()).get(xri.getParent());
            if (container != null) {
                ObjectRecord object = container.get(xri.getLastSegment().toClassicRepresentation());
                if (object != null) {
                    output.add(object);
                    return true;
                }
            }
            throw ResourceExceptions.initHolder(
                new ResourceException(
                    "No such container found in the data manager configuration preferences",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_FOUND,
                        new BasicException.Parameter(BasicException.Parameter.XRI, input.getResourceIdentifier()),
                        new BasicException.Parameter("position", input.getPosition()),
                        new BasicException.Parameter("queryFilter", input.getQueryFilter())
                    )
                )
            );
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.openmdx.base.rest.spi.AbstractFacadeInteraction#find(org.openmdx.base.resource.spi.RestInteractionSpec,
         * org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean find(
            RestInteractionSpec ispec,
            QueryRecord input,
            ResultRecord output
        )
            throws ResourceException {
            Path xri = input.getResourceIdentifier();
            if (input.getQueryFilter() != null) {
                throw ResourceExceptions.initHolder(
                    new NotSupportedException(
                        "Queries are not yet implemented for data manager configuration preferences",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_IMPLEMENTED,
                            new BasicException.Parameter(BasicException.Parameter.XRI, input.getResourceIdentifier()),
                            new BasicException.Parameter("position", input.getPosition()),
                            new BasicException.Parameter("queryFilter", input.getQueryFilter())
                        )
                    )
                );
            }
            Map<String, ObjectRecord> container = getContainers(getConnection()).get(xri);
            if (container == null) {
                throw ResourceExceptions.initHolder(
                    new ResourceException(
                        "No such container found in the data manager configuration preferences",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.NOT_FOUND,
                            new BasicException.Parameter(BasicException.Parameter.XRI, input.getResourceIdentifier()),
                            new BasicException.Parameter("position", input.getPosition()),
                            new BasicException.Parameter("queryFilter", input.getQueryFilter())
                        )
                    )
                );
            } else {
                long skip = input.getPosition() == null ? 0 : input.getPosition().longValue();
                for (ObjectRecord object : container.values()) {
                    if (skip-- <= 0) {
                        output.add(object);
                    }
                }
                ResultRecord resultRecord = output;
                resultRecord.setHasMore(false);
                resultRecord.setTotal(container.size());
            }
            return true;
        }

        /**
         * Build the preferences object lazily
         * 
         * @return the preferences objects
         * 
         * @throws ResourceException
         */
        Map<Path, Map<String, ObjectRecord>> getContainers(
            RestConnection connection
        )
            throws ResourceException {
            if (DataManagerPreferencesPort.this.containers == null) {
                DataManagerPreferencesPort.this.containers = new HashMap<Path, Map<String, ObjectRecord>>();
                //
                // Segments
                //
                Map<String, ObjectRecord> segmentsContainer = new LinkedHashMap<String, ObjectRecord>();
                DataManagerPreferencesPort.this.containers.put(SEGMENTS_ID, segmentsContainer);
                addSegment(segmentsContainer);
                //
                // Preferences
                //                    
                Map<String, ObjectRecord> preferencesContainer = new LinkedHashMap<String, ObjectRecord>();
                DataManagerPreferencesPort.this.containers.put(PREFERENCES_ID, preferencesContainer);
                for (Map.Entry<String, Port<RestConnection>> portEntry : raw.entrySet()) {
                    getNodes(connection, preferencesContainer, portEntry);
                }

            }
            return DataManagerPreferencesPort.this.containers;
        }

        private void getNodes(
            RestConnection connection,
            Map<String, ObjectRecord> preferencesContainer,
            Map.Entry<String, Port<RestConnection>> portEntry
        )
            throws ResourceException {
            final Port<RestConnection> port = portEntry.getValue();
            if (port != DataManagerPreferencesPort.this) {
                String name = portEntry.getKey();
                Path nodesId = addPreferences(preferencesContainer, name);
                //
                // Nodes
                //
                for (Map.Entry<Path, Port<RestConnection>> destinationEntry : destinations.entrySet()) {
                    if (destinationEntry.getValue() == port) {
                        final Map<String, ObjectRecord> nodesContainer = new LinkedHashMap<String, ObjectRecord>();
                        DataManagerPreferencesPort.this.containers.put(nodesId, nodesContainer);
                        final Record nodes;
                        try {
                            nodes = port.getInteraction(connection).execute(
                                InteractionSpecs.getRestInteractionSpecs(false).GET,
                                newQuery(nodesId)
                            );
                        } catch (RuntimeException exception) {
                            exception.printStackTrace();
                            throw exception;
                        } catch (ResourceException exception) {
                            exception.printStackTrace();
                            throw exception;
                        }
                        for (Object rawNode : (IndexedRecord) nodes) {
                            final ObjectRecord node = (ObjectRecord) rawNode;
                            final Path nodeId = node.getResourceIdentifier();
                            nodesContainer.put(nodeId.getLastSegment().toClassicRepresentation(), node);
                            //
                            // Entries
                            //
                            final Path entriesId = nodeId.getChild("entry");
                            final Map<String, ObjectRecord> entriesContainer = new LinkedHashMap<String, ObjectRecord>();
                            DataManagerPreferencesPort.this.containers.put(entriesId, entriesContainer);
                            final Record entries = port.getInteraction(connection).execute(
                                InteractionSpecs.getRestInteractionSpecs(false).GET,
                                newQuery(entriesId)
                            );
                            for (Object rawEntry : (IndexedRecord) entries) {
                                final ObjectRecord entry = (ObjectRecord) rawEntry;
                                final Path entryId = entry.getResourceIdentifier();
                                entriesContainer.put(entryId.getLastSegment().toClassicRepresentation(), entry);
                            }
                        }
                    }
                }
            }
        }

    }

}
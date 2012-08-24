/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Basic Preferences Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and uEJBse in source and binary forms, with or
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
package org.openmdx.preferences2.prefs;

import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

import javax.jdo.PersistenceManager;

import org.openmdx.base.jmi1.Provider;
import org.openmdx.preferences2.cci2.NodeQuery;
import org.openmdx.preferences2.jmi1.Node;
import org.openmdx.preferences2.jmi1.Root;
import org.openmdx.preferences2.jmi1.Segment;

/**
 * JMI Preferences Factory
 */
public class ManagedPreferencesFactory implements PreferencesFactory {
    
    /**
     * Constructor 
     */
    public ManagedPreferencesFactory(
        PersistenceManager persistenceManager,
        String providerXRI,
        String segmentName,
        String systemPreferencesName,
        String userPreferencesName
    ) {
        this.jmiEntityManager = persistenceManager;
        this.providerXRI = providerXRI;
        this.segmentName = segmentName;
        this.systemPreferencesName = systemPreferencesName;    
        this.userPreferencesName = userPreferencesName;
    }

    /**
     * The entity manager factory associated with this preferences factory
     */
    protected final PersistenceManager jmiEntityManager;

    /**
     * The provider XRI, usually matching xri://@openmdx*org.openmdx.preferences2/provider/($..)
     */
    String providerXRI;
    
    /**
     * The preferences segment name
     */
    String segmentName;

    /**
     * The last segment of the user preferences XRI
     */
    String userPreferencesName;
    
    /**
     * The last segment of the system preferences XRI
     */
    String systemPreferencesName;

    /**
     * Retrieve the root object, creating it if necessary
     * 
     * @param type
     * @param name
     * 
     * @return the (maybe newly created) root object
     */
    protected Node getRootNode(
        String type,
        String name
    ){
        Provider provider = this.jmiEntityManager.getObjectById(
            Provider.class,
            this.providerXRI
        );
        Segment segment = (Segment) provider.getSegment(
            this.segmentName
        );
        if(segment == null) {
            throw new RuntimeException(
                "Preferences segment " + this.segmentName + " is not provided by " + provider.refMofId()
            );
        }
        org.openmdx.preferences2.jmi1.Preferences root = segment.getPreferences(name);
        List<Node> nodes;
        if(root == null) {
            nodes = Collections.emptyList();
        } else {
            if(!type.equals(root.getType())) {
                throw new RuntimeException(
                    root.refMofId() + " is a " + root.getType() + " root, " +
                    "not a " + type + " root"
                );
            }
            NodeQuery query = (NodeQuery) this.jmiEntityManager.newQuery(Node.class);
            query.parent().isNull();
            nodes = root.getNode(query);
        }
        switch(nodes.size()) {
            case 0:
                return newRootNode(segment, root, type, name);
            case 1:
                return nodes.get(0);
            default:
                throw new RuntimeException(
                    "The root node for " + root.refMofId() + " is ambiguous"
                );
        }
    }
    
    /**
     * Provide a root node
     * 
     * @param segment
     * @param preferences
     * @param type
     * @param name
     * 
     * @return a newly created root node
     */
    protected Node newRootNode(
        Segment segment,
        Root root,        
        String type,
        String name
    ){
        if(root == null) {
            //
            // Create a Preferences object
            //
            org.openmdx.preferences2.jmi1.Preferences preferences = this.jmiEntityManager.newInstance(
                org.openmdx.preferences2.jmi1.Preferences.class
            );
            preferences.setType(type);
            segment.addPreferences(name, preferences);
            root = preferences;
        }
        //
        // Create a root Node
        //
        Node node = this.jmiEntityManager.newInstance(Node.class);
        node.setName("");
        root.addNode(node);
        return node;
    }
    
    /* (non-Javadoc);
     * @see java.util.prefs.PreferencesFactory#systemRoot()
     */    
//  @Override
    public Preferences systemRoot(
    ) {
        return new ManagedPreferences(
            getRootNode("system", this.systemPreferencesName)
        );
    }

    /* (non-Javadoc)
     * @see java.util.prefs.PreferencesFactory#userRoot()
     */
//  @Override
    public Preferences userRoot(
    ) {
        return new ManagedPreferences(
            getRootNode("user", this.userPreferencesName)
        );
    }

}

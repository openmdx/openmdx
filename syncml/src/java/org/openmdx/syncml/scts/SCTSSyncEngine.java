package org.openmdx.syncml.scts;

import java.util.Map;

import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.engine.SyncDatabase;
import org.openmdx.syncml.engine.SyncEngine;
import org.openmdx.syncml.engine.SyncOptions;

/**
 * SyncML conformance test engine. Handles requests issued by SyncML
 * Conformance Test Suite (http://sourceforge.net/projects/oma-scts/) 
 */
public class SCTSSyncEngine extends SyncEngine {
    
    //-----------------------------------------------------------------------
    public SCTSSyncEngine(
        SyncOptions options,
        String responseURI,
        Map<String, SyncDatabase> databases,
        SyncDatabase anchors
    ) throws SmlException_t {
        super(
            options,
            responseURI,
            databases,
            anchors
        );

    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    
}

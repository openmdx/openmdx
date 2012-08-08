package org.openmdx.syncml.example;

import java.util.Map;

import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.engine.SyncDatabase;
import org.openmdx.syncml.engine.SyncEngine;
import org.openmdx.syncml.engine.SyncOptions;

public class EchoSyncEngine extends SyncEngine {
    
    //-----------------------------------------------------------------------
    public EchoSyncEngine(
        SyncOptions options,
        String respURI,
        Map<String, SyncDatabase> databases,
        SyncDatabase anchors
    ) throws SmlException_t {
        super(
            options,
            respURI,
            databases,
            anchors
        );
    }

}

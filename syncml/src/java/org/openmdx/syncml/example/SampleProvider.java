package org.openmdx.syncml.example;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlProcessMode_t;
import org.openmdx.syncml.engine.MessageParser;
import org.openmdx.syncml.engine.SyncEngine;
import org.openmdx.syncml.engine.SyncOptions;
import org.openmdx.syncml.xlt.SmlEncoding_t;

public class SampleProvider {
    
    public static void process(
        byte[] request,
        ByteArrayOutputStream response
    ) throws SmlException_t {

        // Message parser dispatches message commands to sync servlet
        SyncOptions options = new SyncOptions();
        options.encoding = SmlEncoding_t.SML_XML;   
        SyncEngine syncEngine = new EchoSyncEngine(
            options,
            "http://data.sync.server.url",
            Collections.EMPTY_MAP,
            null
        );        
        MessageParser messageParser = new MessageParser(
            syncEngine,
            options,
            request
        );
        // Example helper for building a message
        boolean hasMore = true;
        while(hasMore) {
            /**
             * Prepare the callback parameter userData here!
             * userData is a void pointer handed over to every callback function
             * as one of the function arguments. The Toolkit doesn't touch the
             * content of this structure. For instance, this mechanism can be used 
             * by the application to pass data to the callback routines. 
             */       
            hasMore = messageParser.parse(
                SmlProcessMode_t.SML_ALL_COMMANDS
            );
        }
        syncEngine.getResponse(response);
    }

}

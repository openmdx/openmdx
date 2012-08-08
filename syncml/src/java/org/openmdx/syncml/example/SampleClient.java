package org.openmdx.syncml.example;

import java.io.ByteArrayOutputStream;

import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.engine.MessageBuilder;
import org.openmdx.syncml.engine.SyncOptions;

public class SampleClient {

    public static void sendMessage0(
        SyncOptions options,
        ByteArrayOutputStream request
    ) throws SmlException_t {
        
        MessageBuilder messageBuilder = new MessageBuilder(
            options,
            request
        );
        SampleHelper smlHelper = new SampleHelper(messageBuilder);
    
        /* Start a new message using the syncHdr proto element */
        smlHelper.startMessage();
    
        /* Continue adding SyncML commands to the workspace. The proto element
           structures holding the parameters associated with each command
           need to be allocated and set to appropriate values before usage. */
    
        /* Start sync cmd */
        smlHelper.startSync();
    
        /* Start add cmd */
        smlHelper.addCmd();
    
        /* End the sync block */
        smlHelper.endSync();
    
        /* --- End message --- */
        smlHelper.endMessage();
    
        smlHelper.terminate();
    
    }

}

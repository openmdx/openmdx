//*************************************************************************/
//* module:          SyncML Command Builder                               */
//*                                                                       */   
//* file:            mgrcmdbuilder.c                                      */
//* target system:   all                                                  */
//* target OS:       all                                                  */   
//*                                                                       */   
//* Description:                                                          */   
//* Core Module for assembling SyncML compliant documents                 */
//*************************************************************************/

package org.openmdx.syncml.engine;

import java.io.ByteArrayOutputStream;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlAtomic_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlExec_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlMap_t;
import org.openmdx.syncml.SmlProtoElement_t;
import org.openmdx.syncml.SmlResults_t;
import org.openmdx.syncml.SmlSearch_t;
import org.openmdx.syncml.SmlSequence_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.SmlVersion_t;
import org.openmdx.syncml.xlt.XltEnc;
import org.openmdx.syncml.xlt.XltEncoder_t;

/*************************************************************************
 *  Exported SyncML API functions
 *************************************************************************/

public class MessageBuilder {
  
    //-----------------------------------------------------------------------
    public MessageBuilder(
        SyncOptions options,
        ByteArrayOutputStream buffer        
    ) throws SmlException_t {
        this.options = options;
        this.buffer = buffer;
    }
    
    //-----------------------------------------------------------------------
    public int getBufferSize(
    ) {
        return this.buffer.size();
    }
    
    //-----------------------------------------------------------------------
    public SyncOptions getOptions(
    ) {
        return this.options;
    }
    
    //-----------------------------------------------------------------------
    /**
     * FUNCTION: smlStartMessageExt
     * (%%% added by luz 2003-08-06 to support SyncML versions other than
     * 1.0 with new vers parameter)
     *
     * Start a SyncML Message 
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *                  SyncML version
     *
     * IN:              SmlSyncHdrPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlStartMessage(
        SmlSyncHdr_t syncHdr, 
        SmlVersion_t vers
    ) throws SmlException_t {
        XltEncoder_t encoderState = null;        
        try {
            encoderState = XltEnc.xltEncInit(
                this.options.encoding,
                syncHdr,
                this.buffer,
                vers
            );
        }
        catch(SmlException_t e) {
            // abort, unlock the buffer again without changing it's current
            // position
            // Reset the encoder module (free the encoding object)
            XltEnc.xltEncReset(encoderState);
            // this encoding job is over! reset instanceInfo pointer            
            throw e;
        }
        this.encoderState = encoderState;        
    }

    /**
     * FUNCTION: smlEndMessage
     *
     * End a SyncML Message
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              Boolean_t
     *                  Final Flag indicates last message within a package
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlEndMessage(
        boolean isFinal
    ) throws SmlException_t {

        if (encoderState == null)
            throw new SmlException_t(Ret_t.SML_ERR_MGR_INVALID_INSTANCE_INFO);
    
        /* -- set Final Flag -- */
        ((XltEncoder_t) (encoderState)).isFinal = isFinal;

        /* --- Call the encoder module --- */
        try {
            XltEnc.xltEncTerminate(
                this.encoderState,
                this.buffer
            );
        }
        catch(SmlException_t e) {
            this.encoderState = null;
            throw e;
        }
        // this encoding job is over! reset instanceInfo pointer
        // (the decoding object itself has been freed by the decoder)
        this.encoderState = null;
    }

    /**
     * FUNCTION: smlStartSync
     *
     * Start synchronizing
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              SyncPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlStartSync(
        SmlSync_t smlSync
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_SYNC_START, 
            smlSync
        );
    }

    /**
     * FUNCTION: smlEndSync
     *
     * End synchronizing
     *
     * IN:              InstanceID_t
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlEndSync(
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_SYNC_END, 
            null
        );
    }


    /**
     * FUNCTION: smlStartAtomic
     *
     * Start an atomic sequence
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              SmlAtomicPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlStartAtomic(
        SmlAtomic_t pContent
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_ATOMIC_START, 
            pContent
        );
    }

    /**
     * FUNCTION: smlEndAtomic
     *
     * End an atomic sequence
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlEndAtomic(
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_ATOMIC_END, 
            null
        );
    }

    /**
     * FUNCTION: smlStartSequence
     *
     * Start a sequence
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              SequencePtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlStartSequence(
        SmlSequence_t pContent
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_SEQUENCE_START, 
            pContent
        );
    }

    /**
     * FUNCTION: smlEndSequence
     *
     * End a sequence
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlEndSequence(
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_SEQUENCE_END, 
            null
        );
    }

    /**
     * FUNCTION: smlAddCmd
     *
     * Create a Add Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              SmlAddPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlAddCmd(
        SmlGenericCmd_t smlAdd
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_ADD, 
            smlAdd
        );
    }

    /**
     * FUNCTION: smlAlertCmd
     *
     * Create a Alert Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              SmlAlertPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlAlertCmd(
        SmlAlert_t smlAlert
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_ALERT, 
            smlAlert
        );
    }

    /**
     * FUNCTION: smlDeleteCmd
     *
     * Create a Start Message Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              DeletePtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlDeleteCmd(
        SmlGenericCmd_t pContent
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_DELETE, 
            pContent
        );
    }

    /**
     * FUNCTION: smlGetCmd
     *
     * Create a Get Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              GetPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlGetCmd(
        SmlGetPut_t smlGet
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_GET, 
            smlGet
        );
    }

    /**
     * FUNCTION: smlPutCmd
     *
     * Create a Put Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              PutPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlPutCmd(
        SmlGetPut_t smlPut
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_PUT, 
            smlPut
        );
    }

    /**
     * FUNCTION: smlMapCmd
     *
     * Create a Map Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              MapPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlMapCmd(
        SmlMap_t smlMap
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_MAP, 
            smlMap
        );
    }

    /**
     * FUNCTION: smlResultsCmd
     *
     * Create a Results  Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              ResultsPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlResultsCmd(
        SmlResults_t smlResults
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_RESULTS, 
            smlResults
        );
    }

    /**
     * FUNCTION: smlStatusCmd
     *
     * Create a Status Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              StatusPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlStatusCmd(
        SmlStatus_t smlStatus
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_STATUS, 
            smlStatus
        );
    }

    /**
     * FUNCTION: smlReplaceCmd
     *
     * Create a Replace Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              SmlReplacePtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlReplaceCmd(
        SmlGenericCmd_t smlReplace
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_REPLACE, 
            smlReplace
        );
    }

    /**
     * FUNCTION: smlCopyCmd
     *
     * Create a Copy Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              CopyPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlCopyCmd(
        SmlGenericCmd_t pContent
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_COPY, 
            pContent
        );
    }

    /**
     * FUNCTION: smlExecCmd
     *
     * Create a Exec Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              ExecPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlExecCmd(
        SmlExec_t pContent
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_EXEC, 
            pContent
        );
    }

    /**
     * FUNCTION: smlSearchCmd
     *
     * Create a Search Command
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN:              SearchPtr_t
     *                  Data to pass along with that SyncML command
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlSearchCmd(
        SmlSearch_t pContent
    ) throws SmlException_t {
        mgrCreateNextCommand(
            SmlProtoElement_t.SML_PE_SEARCH, 
            pContent
        );
    }

    /**
     * FUNCTION: smlStartEvaluation
     *
     * Starts an evaluation run which prevents further API-Calls to write tags - 
     * just the tag-sizes are calculated. Must be sopped via smlEndEvaluation
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlStartEvaluation(
    ) throws SmlException_t {
        XltEnc.xltStartEvaluation(
            this.encoderState
        );
    }

    /**
     * FUNCTION: smlEndEvaluation
     *
     * Stops an evaluation run which prevents further API-Calls to write tags - 
     * the remaining free buffer size after all Tags are written is returned
     *
     * IN:              InstanceID_t
     *                  ID of the used instance
     *
     * IN/OUT:          MemSize_t              
     *					Size of free buffer for data after all tags are written
     *
     * RETURN:          Ret_t
     *                  Return Code
     */
    public void smlEndEvaluation(
        long freemem
    ) throws SmlException_t {
        if (this.encoderState == null)
            throw new SmlException_t(Ret_t.SML_ERR_WRONG_USAGE);

        XltEnc.xltEndEvaluation(
            this.encoderState, 
            freemem
        );
    }

    /**
     * FUNCTION: 
     * Calls the encoding routines of the Encoder Module for a given Command Type
     * and Command Content
     *
     *
     * IN:        InstanceID_t              
     *            ID of the Instance
     *
     * IN:        ProtoElement_t              
     *            Type of the command (defined by the Proto Element Enumeration)
     *
     * IN:        VoidPtr_t              
     *            Content of the command to encode
     *
     * RETURN:    Return value,            
     *            SML_ERR_OK if command has been encoded successfully
     */
    public void mgrCreateNextCommand(
        SmlProtoElement_t cmdType, 
        Object pContent
    ) throws SmlException_t {
        if (this.encoderState == null)
            throw new SmlException_t(Ret_t.SML_ERR_MGR_INVALID_INSTANCE_INFO);

        /* --- Call the encoder module --- */
        XltEnc.xltEncAppend(
            this.encoderState, 
            cmdType, 
            pContent,
            this.buffer
        );
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private final ByteArrayOutputStream buffer;
    private XltEncoder_t encoderState = null;
    private SyncOptions options = null;    
    
}

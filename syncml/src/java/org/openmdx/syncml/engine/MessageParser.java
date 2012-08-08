//*************************************************************************/
//* module:          SyncML Command Dispatcher                            */
//*                                                                       */   
//* file:            mgrcmddispatcher.c                                   */
//* target system:   all                                                  */
//* target OS:       all                                                  */   
//*                                                                       */   
//* Description:                                                          */   
//* Core module for dispatching parsed commands and invoking callback     */
//* functions of the application                                          */
//*************************************************************************/

package org.openmdx.syncml.engine;

import org.openmdx.syncml.Ret_t;
import org.openmdx.syncml.SmlAlert_t;
import org.openmdx.syncml.SmlAtomic_t;
import org.openmdx.syncml.SmlException_t;
import org.openmdx.syncml.SmlExec_t;
import org.openmdx.syncml.SmlGenericCmd_t;
import org.openmdx.syncml.SmlGetPut_t;
import org.openmdx.syncml.SmlMap_t;
import org.openmdx.syncml.SmlProcessMode_t;
import org.openmdx.syncml.SmlProtoElement_t;
import org.openmdx.syncml.SmlResults_t;
import org.openmdx.syncml.SmlSearch_t;
import org.openmdx.syncml.SmlSequence_t;
import org.openmdx.syncml.SmlStatus_t;
import org.openmdx.syncml.SmlSyncHdr_t;
import org.openmdx.syncml.SmlSync_t;
import org.openmdx.syncml.xlt.XltDec;
import org.openmdx.syncml.xlt.XltDecInitResult_t;
import org.openmdx.syncml.xlt.XltDecNextResult_t;
import org.openmdx.syncml.xlt.XltDecoder_t;

public class MessageParser {

    public MessageParser(
        SyncCallbackHandler engine,
        SyncOptions options,        
        byte[] buffer
    ) {
        this.engine = engine;
        this.options = options;
        this.buffer = buffer;        
    }
    
    /**
     * FUNCTION:  smlProcessData
     *
     * Start the parsing of the XML code in the workspace buffer, 
     * dispatches the interpreted command and calls the corresponding callback 
     * functions provided by the application.
     *
     * IN:              InstanceID_t
     *                  The SyncML instance id is used for referencing the 
     *                  workspace buffer from the XML content is parsed          
     *
     * IN:              ProcessMode_t
     *                  Mode of processing, Defines, if only the first or next 
     *                  XML command is parsed or if all commands are processed 
     *                  subsequently until the end of the entire workspace buffer 
     *                  is reached. The NEXT_COMMAND flag defines the blocking mode, 
     *                  the ALL_COMMANDS tag defines the non-blocking mode.  
     *
     * RETURN:          Ret_t
     */
    public boolean parse(
        final SmlProcessMode_t mode
    ) throws SmlException_t {  
      /* --- Is parsing already in progress? --- */
      if (this.decoderState==null) {
        /* No! Parse the Message header section first */    
        this.startMessage();
      }
        
      /* --- Parse now the Message body section! --- */ 
      boolean hasMore = false;
      do {
          hasMore = nextCommand();
      } 
      // keep processing while no error occurs,
      // AND the document end was not reached (decoderState has been invalidated),
      // AND the ALL_COMMAND mode is used
      while (
          hasMore &&
          (this.decoderState != null) &&
          (mode==SmlProcessMode_t.SML_ALL_COMMANDS)
      );
      // abort, unlock the buffer again without changing it's current position
      // Reset the decoder module (free the decoding object)
      XltDec.xltDecReset(this.decoderState);
      // this decoding job is over! reset Instance Info pointer
      this.decoderState=null;     
      // Reset the Workspace (the remaining unparsed document fragment will be lost)
      return hasMore;
    }

    /**
     * FUNCTION: 
     * Parses the header information at the beginning of an SyncML document.
     *
     * IN:              InstanceID
     *                  current InstanceID to pass to callback functions
     *
     * IN/OUT:          InstanceInfo
     *                  state information of the given InstanceID (decoder state will be changed)
     *
     * RETURN:          Return value of the Parser,            
     *                  SML_ERR_OK if next command was handled successfully
     */
    public void startMessage(
    ) throws SmlException_t {
    
      /* --- Definitions --- */ 
      Ret_t               rc = Ret_t.SML_ERR_OK;                   // Temporary return code saver
      SmlSyncHdr_t     pContent=null;               // data of the command to process
        
      /* --- Start new decoding sequence and pass returned decoder status structure to instanceInfo --- */
      XltDecInitResult_t result = XltDec.xltDecInit(
          this.options.encoding,
          this.buffer
      );
      this.decoderState = result.ppDecoder;
      pContent = result.ppSyncHdr;
    
      if (rc!=Ret_t.SML_ERR_OK) {
        // abort, unlock the buffer again without changing it's current position
      	// Reset the decoder module (free the decoding object)
    	  XltDec.xltDecReset(this.decoderState);
        // this decoding job is over! reset Instance Info pointer
        this.decoderState = null;     
        // Reset the Workspace (the remaining unparsed document fragment will be lost)
        throw new SmlException_t(rc);
      }
    
      /* --- Perform callback to handle the beginning of a new message --- */
      this.engine.startMessage(pContent); 
    
      if (rc != Ret_t.SML_ERR_OK)
      {
        // abort, unlock the buffer again without changing it's current position
        // Reset the decoder module (free the decoding object)
        XltDec.xltDecReset(this.decoderState);
        // this decoding job is over! reset Instance Info pointer
        this.decoderState=null;     
     	  // Reset the Workspace (the remaining unparsed document fragment will be lost)
        throw new SmlException_t(rc);
      }
      
    }

    /**
     * FUNCTION: 
     * Parses the next Sync Command in the sync document.
     *
     * IN:              InstanceID
     *                  current InstanceID to pass to callback functions
     *
     * IN:              InstanceInfo
     *                  state information of the given InstanceID
     *
     * RETURN:          Return value of the Parser of the called application callback,            
     *                  SML_ERR_OK if next command was handled successfully
     */
    public boolean nextCommand(
    ) throws SmlException_t {
    
        /* --- Definitions --- */ 
        Ret_t rc = Ret_t.SML_ERR_OK; // Temporary return code saver
        SmlProtoElement_t cmdType; // ID of the command to process
        Object pContent = null; // data of the command to process
        boolean isFinal; // flag indicates last message within a package
          
      /* --- Get Read Access to the workspace --- */
      // Remember the position we have started reading
    
      /* --- Parse next Command --- */ 
      XltDecNextResult_t result = XltDec.xltDecNext(
          this.decoderState 
      );
      pContent = result.ppContent;
      cmdType = result.pe;
      
      if (rc!=Ret_t.SML_ERR_OK) {
      	// Reset the decoder module (free the decoding object)
    	  XltDec.xltDecReset(this.decoderState);
        // this decoding job is over! reset Instance Info pointer
        this.decoderState=null;           
        throw new SmlException_t(rc);
      }
    
      /* --- End Read Access to the workspace --- */
      /* --- Did we reach end of synchronization document? --- */
      if (((XltDecoder_t)(this.decoderState)).isFinished) {
        isFinal = ((XltDecoder_t)(this.decoderState)).isFinal; // flag is returned to appl. with callback
        rc=XltDec.xltDecTerminate(this.decoderState);
    
        if (rc!=Ret_t.SML_ERR_OK) {
          // Reset the decoder module (free the decoding object)
    	  XltDec.xltDecReset(this.decoderState);
          // this decoding job is over! reset Instance Info pointer
          this.decoderState=null;           
          throw new SmlException_t(rc);
    	}
    
        // this decoding job is over! reset Instance Info pointer
    	  // (the decoding object itself has been freed by the decoder)
        this.decoderState=null;
          
        // Call the callback for handling an message ending
        this.engine.endMessage(isFinal);
        return false;
      } 
      else {
          /* --- Dispatch parsed command (and call the applications command handler function)--- */
          switch (cmdType)
          {
            /* Handle ADD Command */
            case SML_PE_ADD:
              this.engine.addCmd ((SmlGenericCmd_t)pContent); 
              break;
            /* Handle ALERT Command */
            case SML_PE_ALERT:
              this.engine.alertCmd ((SmlAlert_t)pContent); 
              break;
            /* Handle DELETE Command */
            case SML_PE_DELETE:
              this.engine.deleteCmd ((SmlGenericCmd_t)pContent);  
              break;
            /* Handle PUT Command */
            case SML_PE_PUT:
              this.engine.putCmd ((SmlGetPut_t)pContent);   
              break;
            /* Handle GET Command */
            case SML_PE_GET:
              this.engine.getCmd ((SmlGetPut_t)pContent);     
              break;
            /* Handle MAP Command */
            case SML_PE_MAP:
              this.engine.mapCmd ((SmlMap_t)pContent);     
              break;
            /* Handle RESULTS Command */
            case SML_PE_RESULTS:
              this.engine.resultsCmd ((SmlResults_t)pContent); 
              break;
            /* Handle STATUS Command */
            case SML_PE_STATUS:
              this.engine.statusCmd ((SmlStatus_t)pContent);  
              break;
            /* Handle START SYNC Command */
            case SML_PE_SYNC_START:
              this.engine.startSync ((SmlSync_t)pContent);  
              break;
            /* Handle END SYNC Command */
            case SML_PE_SYNC_END:
              this.engine.endSync ();              
              break;
            /* Handle REPLACE Command */
            case SML_PE_REPLACE:
              this.engine.replaceCmd((SmlGenericCmd_t)pContent);  
              break;
            /* Handle Final Flag */
            case SML_PE_FINAL:
              // if a FINAL Flag appears do nothing
              break;      
            /* Handle SEARCH Command */
            case SML_PE_SEARCH:
              this.engine.searchCmd ((SmlSearch_t)pContent);  
              break;
            /* Handle START SEQUENCE Command */
            case SML_PE_SEQUENCE_START:
              this.engine.startSequence ((SmlSequence_t)pContent); 
              break;
            /* Handle END SEQUENCE Command */
            case SML_PE_SEQUENCE_END:
              this.engine.endSequence ();             
              break;
            /* Handle START ATOMIC Command */
            case SML_PE_ATOMIC_START:
              this.engine.startAtomic ((SmlAtomic_t)pContent);
              break;
            /* Handle END ATOMIC Command */
            case SML_PE_ATOMIC_END:
              this.engine.endAtomic ();            
              break;
            /* Handle COPY Command */
            case SML_PE_COPY:
              this.engine.copyCmd ((SmlGenericCmd_t)pContent);    
              break;
            /* Handle EXEC Command */
            case SML_PE_EXEC:
              this.engine.execCmd ((SmlExec_t)pContent);    
              break;
            /* Handle ERROR DETECTED  */
            //case SML_PE_ERROR:
            //  if (pInstanceInfo.callbacks.handleErrorFunc==null) return SML_ERR_COMMAND_NOT_HANDLED;
            //  return pInstanceInfo.callbacks.handleErrorFunc (id, pInstanceInfo.userData);        
            //  break;
        
            /* --- Invalid Command Element --- */
            default:
              throw new SmlException_t(Ret_t.SML_ERR_XLT_INVAL_PROTO_ELEM);   
          } // switch 
          return true;
      }
    }
 
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    private final byte[] buffer;
    private XltDecoder_t decoderState;
    private final SyncCallbackHandler engine;
    private final SyncOptions options;
    
}

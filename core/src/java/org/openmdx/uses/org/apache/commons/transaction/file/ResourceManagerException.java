/*
 * $Header: /cvsroot/openmdx/core/src/java/org/openmdx/uses/org/apache/commons/transaction/file/ResourceManagerException.java,v 1.1 2005/03/24 13:42:52 hburger Exp $
 * $Revision: 1.1 $
 * $Date: 2005/03/24 13:42:52 $
 *
 * ====================================================================
 *
 * Copyright 1999-2002 The Apache Software Foundation 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.openmdx.uses.org.apache.commons.transaction.file;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Signals any kind of error or failure state in a {@link ResourceManager}.
 * 
 * @version $Revision: 1.1 $
 *
 */
public class ResourceManagerException extends Exception implements ResourceManagerErrorCodes {

    /**
     * 
     */
    private static final long serialVersionUID = 3257004337124489264L;

    private static final int[] ERROR_CODES =
        {
            ERR_SYSTEM,
            ERR_SYSTEM_INCONSISTENT,
            ERR_NO_TX,
            ERR_TXID_INVALID,
            ERR_TX_INACTIVE,
            ERR_TX_INCONSISTENT,
            ERR_DUP_TX,
            ERR_THREAD_INVALID,
            ERR_ISOLATION_LEVEL_UNSUPPORTED,
            ERR_RESOURCEID_INVALID,
            ERR_RESOURCE_EXISTS,
            ERR_NO_SUCH_RESOURCE,
            ERR_LOCK,
            ERR_NO_LOCK,
            ERR_MARKED_FOR_ROLLBACK,
            };

    private static final String[] ERROR_CODE_STRINGS =
        {
            "ERR_SYSTEM",
            "ERR_SYSTEM_INCONSISTENT",
            "ERR_NO_TX",
            "ERR_TXID_INVALID",
            "ERR_TX_INACTIVE",
            "ERR_TX_INCONSISTENT",
            "ERR_DUP_TX",
            "ERR_THREAD_INVALID",
            "ERR_ISOLATION_LEVEL_UNSUPPORTED",
            "ERR_RESOURCEID_INVALID",
            "ERR_RESOURCE_EXISTS",
            "ERR_NO_SUCH_RESOURCE",
            "ERR_LOCK",
            "ERR_NO_LOCK",
            "ERR_MARKED_FOR_ROLLBACK",
            };

    private static final String[] ERROR_CODE_TEXTS =
        {
            "System error",
            "Inconsistent system data",
            "Unknown transaction",
            "Invalid transaction id",
            "Transaction inactive",
            "Inconsistent transaction data",
            "Duplicate transaction id",
            "Thread of control is the one that not start tx",
            "Isolation level unsupported",
            "Specified resource id is invalid",
            "Resource already exists",
            "No such resource",
            "Locking error",
            "Could not acquire lock",
            "Transaction already marked for rollback" };

    public static final String ERR_UNKNOWN_TEXT = "Unknown error";
    public static final String ERR_UNKNOWN_CODE = "ERR_UNKNOWN";

    protected final int status;
    protected final Object txId;

    protected static final String composeMessage(String msg, int status, Object txId, Throwable cause) {
        String message = composeMessage(msg, status, txId);
        StringBuffer messageBuffer = new StringBuffer(message);
        messageBuffer.append("\nCaused by: ");
        StringWriter sw = new StringWriter();
        cause.printStackTrace(new PrintWriter(sw));
        messageBuffer.append(sw.getBuffer());
        return messageBuffer.toString();
    }
    
    protected static final String composeMessage(String msg, int status, Object txId) {
        StringBuffer composed = new StringBuffer();
        if (txId != null) {
            composed.append(txId).append(": ");
        }
        if (msg != null) {
            composed.append(msg);
            if (status != -1) {
                composed.append(" (").append(statusToCode(status)).append(')');
            }
        } else if (status != -1) {
            composed.append(statusToText(status));
        }

        return composed.toString();
    }

    public static final String statusToText(int status) {
        if (status == ERR_UNKNOWN) {
            return ERR_UNKNOWN_TEXT;
        } else {
            int pos = -1;
            for (int i = 0; i < ERROR_CODES.length; i++) {
                int code = ERROR_CODES[i];
                if (status == code) {
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {
                return ERR_UNKNOWN_TEXT + ", code: " + status;
            } else {
                return ERROR_CODE_TEXTS[pos];
            }
        }
    }

    public static final String statusToCode(int status) {
        if (status == ERR_UNKNOWN) {
            return ERR_UNKNOWN_CODE;
        } else {
            int pos = -1;
            for (int i = 0; i < ERROR_CODES.length; i++) {
                int code = ERROR_CODES[i];
                if (status == code) {
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {
                return ERR_UNKNOWN_CODE + ": " + status;
            } else {
                return ERROR_CODE_STRINGS[pos];
            }
        }
    }

    public ResourceManagerException(String message, int status, Object txId) {
        super(ResourceManagerException.composeMessage(message, status, txId));
        this.status = status;
        this.txId = txId;
    }

    public ResourceManagerException(int status, Object txId) {
        this(null, status, txId);
    }

    public ResourceManagerException(String message) {
        super(message);
        this.status = ERR_UNKNOWN;
        this.txId = null;
    }

    public ResourceManagerException(String message, int status, Object txId, Throwable cause) {
        // XXX can not do this, as 1.3 Throwable does not allow cause in ctor :( 
//        super(ResourceManagerException.composeMessage(message, status, txId), cause);
        // for now format cause by ourselves
        super(ResourceManagerException.composeMessage(message, status, txId, cause));
        this.status = status;
        this.txId = txId;
    }

    public ResourceManagerException(String message, int status, Throwable cause) {
        this(message, status, null, cause);
    }

    public ResourceManagerException(String message, Throwable cause) {
        this(message, ERR_UNKNOWN, cause);
    }

    public ResourceManagerException(int status, Object txId, Throwable cause) {
        this(null, status, txId, cause);
    }

    public String statusToString() {
        return ResourceManagerException.statusToText(status);
    }

    public int getStatus() {
        return status;
    }

}

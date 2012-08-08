/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SystemOperations.java,v 1.3 2007/10/10 16:06:01 hburger Exp $
 * Description: System Operations
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:01 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.spi;


/**
 * System Operations
 */
public class SystemOperations {

    protected SystemOperations() {
        // Avoid instantiation
    }

    /**
     * Retrieve a character stream by providing a <code>Writer</code>.
     */
    public final static String GET_CHARACTER_STREAM = "_getCharacterStream";
    
    /**
     * Retrieve a character stream by providing an <code>OutputStream</code>.
     */
    public final static String GET_BINARY_STREAM = "_getBinaryStream";
    
    /**
     * The attribute name for a get stream operation.
     */
    public final static String GET_STREAM_FEATURE = "feature";
    
    /**
     * The initial position for a get stream operation.
     */
    public final static String GET_STREAM_POSITION = "position";

    /**
     * The value for a get stream operation.
     */
    public final static String GET_STREAM_VALUE = "value";

    /**
     * The get stream reply field containing the large object's size
     */
    public final static String GET_STREAM_LENGTH = "length";

    /**
     * The arguments for a get character stream operation.
     */
    public final static String GET_CHARACTER_STREAM_ARGUMENTS = "org:openmdx:compatibility:document1:GetCharacterStreamArguments";

    /**
     * The arguments for a get binary stream operation.
     */
    public final static String GET_BINARY_STREAM_ARGUMENTS = "org:openmdx:compatibility:document1:GetBinaryStreamArguments";

    /**
     * The reply for a get stream operation.
     */
    public final static String GET_STREAM_RESULT = "org:openmdx:compatibility:document1:GetStreamResult";

}

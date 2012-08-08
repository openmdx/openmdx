/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ClassicFormatter.java,v 1.3 2009/04/01 13:33:33 hburger Exp $
 * Description: Classic Formatter 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/01 13:33:33 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.logging;

import java.util.logging.LogRecord;

/**
 * The <code>ClassicFormatter</code> adds a log records thrown exception.
 */
public class ClassicFormatter
    extends AbstractFormatter
{

    /**
     * Constructor 
     */
    public ClassicFormatter() {
        super();
    }

    /**
     * A class and its sub-classes compose the message
     * 
     * @param record
     * 
     * @return a StringBuilder containing the message
     */
    @Override    
    protected void appendFields(
        LogRecord record
    ){
        append(record.getLoggerName());
        newField(); appendTimestamp(record);
        newField().append(record.getLevel());
        newField(); appendHostName();
        newField().append(record.getThreadID());
        newField().append(record.getSourceClassName());
        newField().append(record.getSourceMethodName());
        newField(); appendMessage(record);
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.logging.AbstractFormatter#appendLogFormat()
     */
    @Override
    protected void appendLogFormat() {
        append("logger|timestamp|level|host|thread|class|method|information");
        newLine();
    }
    
}

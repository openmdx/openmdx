/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ClassicFormatter.java,v 1.1 2008/03/21 18:39:16 hburger Exp $
 * Description: Classic Formatter 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:39:16 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.openmdx.base.text.format.DateFormat;

/**
 * Classic Formatter
 */
public class ClassicFormatter
    extends Formatter
{

    /**
     * Constructor 
     */
    public ClassicFormatter() {
        this.dateFormat = DateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        //
        // Host Name
        //
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException exception) {
            hostName = "localhost";
        }
        this.hostName = hostName;
    }

    /**
     * 
     */
    protected final String hostName;
    
    /**
     * 
     */
    protected final DateFormat dateFormat;
    
    /**
     * Classic formatter
     * <p>
     * content=[${log-source}|]${message}[|${throwable}]
     * ${logger}|${date-time}|${level}|${host}|${thread-id}|${class}|${method}|${content}
     */
    @Override
    public String format(LogRecord record) {
        StringBuilder buffer = new StringBuilder(
            record.getLoggerName()
        ).append(
            '|'
        ).append(
            this.dateFormat.format(new Date(record.getMillis()))
        ).append(
            '|'
        ).append(
            record.getLevel()
        ).append(
            '|'
        ).append(
            this.hostName
        ).append(
            '|'
        ).append(
            record.getThreadID()
        ).append(
            '|'
        ).append(
            record.getSourceClassName()
        ).append(
            '|'
        ).append(
            record.getSourceMethodName()
        ).append(
            '|'
        ).append(
            record.getMessage()
        );
        Throwable throwable = record.getThrown();
        if(throwable != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            buffer.append(
                '|'
            ).append(
                stringWriter                    
            );    
        }
        return buffer.append('\n').toString();
    }

}

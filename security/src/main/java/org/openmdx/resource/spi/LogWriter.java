/*
 * ====================================================================
 * Project:     openMDX/Security, http://www.openmdx.org/
 * Description: Log Writer
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2018, OMEX AG, Switzerland
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
package org.openmdx.resource.spi;

import java.io.PrintWriter;

import javax.resource.ResourceException;

public class LogWriter extends PrintWriter {

    public LogWriter(String loggerName) {
        super(new LogAdapter(loggerName));
    }

    /* (non-Javadoc)
     * @see java.io.PrintWriter#println()
     */
    @Override
    public void println() {
        ((LogAdapter)out).flush();
    }

    /**
     * Logs unless the target is {@code null}. The placeholders {0}, {1} etc. 
     * are replaced by the arguments' string values
     * 
     * @param target the optional target
     * @param pattern the pattern
     * @param arguments the (optional) arguments
     */
    public static void log(
        PrintWriter target,
        String pattern,
        Object... arguments
    ) {
        if(target != null) {
            target.println(
                arguments != null && arguments.length > 0 ? LogFormatter.format(pattern, arguments) : pattern
            );
         }
    }

    public static void log(
        PrintWriter target,
        ResourceException exception,
        boolean withStackTrace
    ) {
        if(target != null && exception != null) {
            if(withStackTrace) {
                exception.printStackTrace(target);
            } else {
                target.append(
                    "Resource exception thrown: "
                ).append(
                    exception.getMessage()
                );
                final Throwable cause = exception.getCause();
                if(cause != null) {
                    target.append(
                        " (Caused by "
                    ).append(
                        cause.toString()
                    ).append(
                        ")"
                    );
                }
                target.println();
            }
        }
    }

}

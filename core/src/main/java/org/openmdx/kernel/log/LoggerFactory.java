/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: openMDX Logger Factory
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

/**
 * openMDX Logger Factory
 */
public class LoggerFactory {

    /**
     * The openMDX logger name
     */
    public static final String STANDARD_LOGGER_NAME = "org.openmdx.kernel.log.LoggerFactory";
    
    /**
     * The openMDX logger singleton
     */
    private static final Map<ClassLoader,Logger> loggers = new WeakHashMap<ClassLoader,Logger>();
    
    /**
     * The version information is logged lazily
     */
    private static volatile boolean logVersion = true;
    
    /**
     * Retrieve the openMDX logger
     * 
     * @return the openMDX logger
     */
    public static final Logger getLogger(
    ){
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Logger logger = loggers.get(cl);
        if(logger == null) {
            synchronized(loggers) {
                loggers.put(
                    cl, 
                    logger = Logger.getLogger(STANDARD_LOGGER_NAME)
                );
            }
            if(logVersion) { 
                // No synchronization necessary as Logging the version twice wouldn't hurt
                logVersion = false;
                SysLog.info("openMDX base implementation version", org.openmdx.base.Version.getImplementationVersion());
            }
        }
        return logger;
    }    
    
    public static Collection<Logger> getLoggers(
    ) {
        return loggers.values();
    }
    
}

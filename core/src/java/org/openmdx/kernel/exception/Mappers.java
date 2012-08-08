/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Mappers.java,v 1.1 2009/04/02 14:56:21 hburger Exp $
 * Description: Mapping 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/02 14:56:21 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.kernel.exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.openmdx.kernel.log.SysLog;

/**
 * Mapping
 */
class Mappers implements BasicException.Mapper {
    
    /**
     * Constructor 
     */
    Mappers(){
        Properties configuration = new Properties();
        ClassLoader classLoader = BasicException.class.getClassLoader();
        try {
            for(
                Enumeration<URL> urls = classLoader.getResources("META-INF/openmdxExceptionMapper.properties");
                urls.hasMoreElements();
            ) {
                URL url = urls.nextElement();
                try {
                    InputStream source = url.openStream();       
                    configuration.load(source);
                    source.close();
                } catch (IOException exception) {
                    SysLog.warning("Exception mapper configuration failure: " + url, exception);
                }
            }
        } catch (IOException exception) {
            SysLog.error("Exception mapper configuration failure", exception);
        }
        for(Map.Entry<?, ?> entries : configuration.entrySet()) {
            try {
                Class<?> exceptionClass = Class.forName(
                    entries.getKey().toString(),
                    false,
                    classLoader
                );
                if(Throwable.class.isAssignableFrom(exceptionClass)) {
                    Class<?> mapperClass = Class.forName(
                        entries.getValue().toString(),
                        false,
                        classLoader
                    );
                    if(BasicException.Mapper.class.isAssignableFrom(mapperClass)) {
                        try {
                            registry.put(
                                exceptionClass,
                                (BasicException.Mapper)mapperClass.newInstance()
                            );
                        } catch (InstantiationException exception) {
                            SysLog.warning("Exception mapper instantiation failure: " + entries, exception);
                        } catch (IllegalAccessException exception) {
                            SysLog.warning("Exception mapper instantiation failure: " + entries, exception);
                        }
                    } else {
                        SysLog.log(
                            Level.WARNING, 
                            "Mapper {1} could not be registered for {0} beacuse it does not implement {2}", 
                            entries.getKey(), entries.getValue(), BasicException.Mapper.class.getName()
                        );
                    }
                } else {
                    SysLog.log(
                        Level.WARNING, 
                        "Mapper {1} could not be registered because {0} is not a subclass of {2}", 
                        entries.getKey(), entries.getValue(), Throwable.class.getName()
                    );
                }
            } catch (NoClassDefFoundError error) {
                SysLog.warning("Exception mapper registration failure: " + entries, error);
            } catch (ClassNotFoundException exception) {
                SysLog.warning("Exception mapper registration failure: " + entries, exception);
            }
        }
       
    }
    
    /**
     * Exception Mapper Registry
     */
    private final Map<Class<?>,BasicException.Mapper> registry = new IdentityHashMap<Class<?>,BasicException.Mapper>();

    /**
     * Maps a throwable to a basic exception
     * 
     * @param throwable
     * 
     * @return
     */
    public BasicException map(
        Throwable throwable
    ){
        try {
            for(
                Class<?> current = throwable.getClass();
                current != Throwable.class;
                current = current.getSuperclass()
            ){
                BasicException.Mapper mapper = registry.get(current);
                if(mapper != null) {
                    return mapper.map(throwable);
                }
            }
        } catch (RuntimeException ignore) {
            // Failing exception mappers are just ignored
        }
        return null;
    }
        
}

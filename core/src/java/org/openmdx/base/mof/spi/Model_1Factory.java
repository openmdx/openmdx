/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Model_1Factory.java,v 1.16 2011/06/21 22:30:37 hburger Exp $
 * Description: Model_1Factory
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/06/21 22:30:37 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2010, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.mof.spi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;

/**
 * Model_1Factory
 */
public class Model_1Factory {

    /**
     * The volatile singleton holder allows lazy synchronization
     */
    private static volatile Model_1_0 model;

    /**
     * Retrieve the MOF repository
     * 
     * @return the singleton
     */
    public static Model_1_0 getModel(
    ) {
        if(Model_1Factory.model == null) {
            synchronized(Model_1Factory.class) {
                if(Model_1Factory.model == null) try {
                    List<String> modelPackages = new ArrayList<String>();
                    for(
                    	Enumeration<URL> resources = Classes.getResources("META-INF/openmdxmof.properties");
                    	resources.hasMoreElements();
                    ) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(resources.nextElement().openStream()));
                        for(
                            String line;
                            (line = in.readLine()) != null;
                        ) {
                        	line = line.trim();
                        	if(
                        	    line.length() > 0 && 
                        	    !line.startsWith("#") && 
                        	    !line.startsWith("!") &&
                        	    !modelPackages.contains(line) 
                        	) {
        	                    modelPackages.add(line);
                        	}
                        }
                        in.close();
                    }
                    Model_1_0 model = (Model_1_0) Classes.getApplicationClass(
                        "org.openmdx.application.mof.repository.accessor.Model_1"
                    ).getMethod(
                        "getInstance"
                    ).invoke(
                        null
                    );
                    model.addModels(modelPackages);
                    Model_1Factory.model = model;
                }  catch(Exception exception) {
                    SysLog.log(
                        Level.SEVERE,
                        "Model acquisition failure",
                        exception
                    );
                    throw new RuntimeServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION,
                        "Model acquisition failure"
                    );
                }
            }
        }
        return Model_1Factory.model;
    }
    
    /**
     * Tells whether the MOF repository is already prepared
     * 
     * @return <code>true</code> if the model is loaded
     */
    public static boolean isLoaded(
    ) {
        return Model_1Factory.model != null;
    }
    
}

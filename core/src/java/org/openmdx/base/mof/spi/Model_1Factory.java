/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Model_1Factory.java,v 1.5 2009/03/03 17:23:08 hburger Exp $
 * Description: Model_1Factory
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/03 17:23:08 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2009, OMEX AG, Switzerland
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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.openmdx.base.mof.cci.Model_1_6;
import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.slf4j.LoggerFactory;

public class Model_1Factory {

    public static synchronized Model_1_6 getModel(
    ) {
        if(model == null) try {
            Class<?> modelClass = Classes.getApplicationClass("org.openmdx.application.mof.repository.accessor.Model_1");
            Method getInstanceMethod = modelClass.getMethod("getInstance");
            model = (Model_1_6)getInstanceMethod.invoke(null);
            Enumeration<URL> resources = Classes.getResources("META-INF/openmdxmof.properties");
            List<String> modelPackages = new ArrayList<String>();
            while(resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                BufferedReader in = new BufferedReader(new InputStreamReader(resource.openStream()));
                while(in.ready()) {
                    modelPackages.add(in.readLine());
                }
                in.close();
            }
            model.addModels(modelPackages);
        } catch(Exception e) {
            LoggerFactory.getLogger(
                Model_1Factory.class
            ).warn(
                "Model acquisition failure",
                e
            );
        }
        return model;
    }
    
    private static Model_1_6 model;
    
}

//--- End of File -----------------------------------------------------------

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Platform.java,v 1.6 2007/10/10 16:06:06 hburger Exp $
 * Description: openMDX Platform Accessor
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:06:06 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
package org.openmdx.kernel.environment;

import org.openmdx.kernel.environment.cci.Platform_1_0;
import org.openmdx.kernel.environment.jre.Platform_1;

/**
 * 
 */
public class Platform {

    private Platform(
    ){
        // Avoid instantiation
    }

    /**
     * Java Runtime Environment Information
     */
    private static Platform_1_0 javaRuntimeEnvironment = null;

    /**
     * Get information about the current java platform
     * 
     * @return information about the current java platform
     */
    public static Platform_1_0 getJavaRuntimeEnvironment(
    ){
        if(Platform.javaRuntimeEnvironment == null) synchronized (Platform.class){
            if(Platform.javaRuntimeEnvironment == null) Platform.javaRuntimeEnvironment = new Platform_1(); 
        }
        return Platform.javaRuntimeEnvironment;
    }
    
    /**
     * Java Runtime Environment Information
     */
    private static Platform_1_0 applicationServerEnvironment = null;

    /**
     * Get information about the current application server
     * 
     * @return information about the current application server
     *         or null
     */
    public static Platform_1_0 getApplicationServerEnvironment(
    ){
        if(Platform.applicationServerEnvironment == null) synchronized(Platform.class){
//.NET            String packagePrefix = Platform.class.getPackage().getName();
            String packagePrefix = "org.openmdx.kernel.environment";
            Platform.applicationServerEnvironment = 
                "Microsoft Corp.".equals(getJavaRuntimeEnvironment().getImplementationVendor()) ? 
                    createPlatform(packagePrefix + ".dotnet.Platform_1") :
                System.getProperty("weblogic.Name") != null ? 
                    createPlatform(packagePrefix + ".weblogic.Platform_1") :
                    null; //... Support other application servers as well!
        }
        return Platform.applicationServerEnvironment;
    }

    /**
     * Load and instatiate a platformdescription class dynamically
     * 
     * @param name
     */
    private static Platform_1_0 createPlatform(
        String name
    ){
        try {
            return (Platform_1_0)Class.forName(name).newInstance();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Display the environments.
     * 
     * @param arguments
     */
    public static void main(
        String[] arguments
    ){
        System.out.println(getJavaRuntimeEnvironment());
        System.out.println(getApplicationServerEnvironment());
    }

}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Launch.java,v 1.12 2008/01/25 00:58:54 hburger Exp $
 * Description: Application Client Launcher
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 00:58:54 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.kernel.application.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.log.SysLog;

/**
 * Application Client Launcher
 */
public class Launch {

    /**
     * Avoid instantiation
     */
    private Launch() {
        super();
    }

    /**
     * Launch an Application Client
     * 
     * @param arguments
     */
    public static void main(
        String[] arguments
    ){
        // 
        // Parse args
        //
        List<String> argumentList = new ArrayList<String>();
        Map<String,String> applicationClientEnvironment = new HashMap<String,String>();
        String ear = null;
        for(
            int i = 0;
            i < arguments.length;
            i++
        ){
            if("-ear".equals(arguments[i])){
                if(++i < arguments.length) ear = arguments[i];
            } else if(arguments[i].startsWith("--")){
                int d = arguments[i].indexOf('=');
                if(d < 0){
                    applicationClientEnvironment.put(arguments[i].substring(2), null);
                } else {
                    applicationClientEnvironment.put(arguments[i].substring(2,d), arguments[i].substring(d+1));
                }
            } else {
                argumentList.add(arguments[i]);
            }
        }
        //
        // Validate, deploy and invoke application client
        // 
        if(ear == null){
            System.err.println(
                "Usage: java [<java option>...] " + Launch.class.getName() + 
                "\n\t-ear <enterprise application URL>" + 
                "\n\t[--<env-entry name>=<env-entry value>...]" +
                "\n\t[<application client argument>...]"
            );
        } else {
            String[] applicationClientArguments = argumentList.toArray(
                new String[argumentList.size()]
            );
	        //
	        // Deploy the Application Client
	        // 
            try {
                Runnable main = LightweightContainer.getInstance(
                    LightweightContainer.Mode.ENTERPRISE_APPLICATION_CLIENT
                ).deployApplicationClient(
                    ear, 
                    applicationClientEnvironment,
                    applicationClientArguments
                );
    	        if(main != null) {
    	            main.run();
    	            System.exit(SUCCESS);
    	        }
            } catch (Exception exception) {
                SysLog.warning(
                    "Exception in application client launch", 
                    exception
                );
                System.err.println(
                    "Exception in application client launch: " + 
                    exception.getMessage() + " (see log for details)"
                );
            }
        }
        System.exit(FAILURE);
    }

    /**
     * Success exit status
     */
    final static int SUCCESS = 0;
    
    /**
     * Failure exit status
     */
    final static int FAILURE = 1;
    
}

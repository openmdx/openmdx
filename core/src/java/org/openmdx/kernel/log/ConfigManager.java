/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ConfigManager.java,v 1.6 2008/02/05 13:41:39 hburger Exp $
 * Description: Logging
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/05 13:41:39 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.log;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;



/**
 * The ConfigManager object manages all Log Config objects available.
 * 
 * Remember that the registered {@link Config} objects are only proxies for
 * the real configuration objects. The proxies hold their delegates using
 * a WeakReference. This guarantees that the ConfigManager works even
 * in a J2EE environment where J2EE applications can be undeployed any time.
 */
public final class ConfigManager {

    public ConfigManager()
    {
        super();
    }


    /**
     * List all registered configuration objects. 
     * 
     * @return an unmodifiable, synchronized collection of {@link Config} 
     * objects
     */
    public Collection<Config> list()
    {
    	return Collections.unmodifiableList(this.configList);
	}


    /**
     * Register a Logger by its configuration object. All AppLog classes
     * and the SysLog class must register their configuration object.
     *
     * This list of configurations may be used to track the available
     * loggers and to implement dynamic logging configurators.
     *
     * @param config  a configuration object that belongs to a static
     *  logger class
     */
    void register(Config config)
    {
    	if (config == null) return;

		if (!config.getLogName().equals("SysLog")) {
	     	SysLog.trace("Registering config object: " +
	     	             "appId=" +  config.getApplicationId() + ", " +
	     	             "logger=" + config.getLogName());
		}
		
		// Register
      	this.configList.add(config);
    }


    /**
     * Unregister a logger by its configuration object.
     *
     * @param config  a configuration object that belongs to a static
     *  logger class
     */
    void unregister(Config config)
    {
    	if (config == null) return;

     	SysLog.trace("Unregistering config object: " +
     	             "appId=" +  config.getApplicationId() + ", " +
     	             "logger=" + config.getLogName());

		this.configList.remove(config);
    }



	/** The registered log configs */
	private   List<Config>   configList = Collections.synchronizedList(new ArrayList<Config>());
}


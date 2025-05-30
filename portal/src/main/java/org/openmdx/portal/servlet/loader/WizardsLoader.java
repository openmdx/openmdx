/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: WizardsLoader
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import #if JAVA_8 javax #else jakarta#endif.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.wizards.WizardDefinition;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;
import org.w3c.time.SystemClock;

/**
 * WizardsLoader
 *
 */
public class WizardsLoader extends Loader {

    /**
     * Constructor.
     * 
     * @param context
     * @param portalExtension
     */
    public WizardsLoader(
        ServletContext context,
        PortalExtension_1_0 portalExtension        
    ) {
        super(
            context,
            portalExtension
        );
    }

    /**
     * Loads wizard definitions.
     * 
     */
    synchronized public WizardDefinitionFactory loadWizardDefinitions(
        ServletContext context,
        String[] locale,
        Model_1_0 model
    ) throws ServiceException {
    	String messagePrefix = SystemClock.getInstance().now() + "  ";
    	System.out.println(messagePrefix + "Loading wizards");
    	SysLog.info("Loading wizards");    	
        Map<String,List<WizardDefinition>> wizardDefinitions = new HashMap<String,List<WizardDefinition>>();
        wizardDefinitions.put(
            locale[0], 
            new ArrayList<WizardDefinition>()
        );
        // Get all wizards in locale[0]
        {
	        Set<String> wizardPaths = context.getResourcePaths("/wizards/" + locale[0]);
	        if(wizardPaths != null) {
	            for(String path0: wizardPaths) {
	                if(!path0.endsWith("/")) {            
	                    WizardDefinition wizardDefinition0 = 
	                        WizardDefinitionFactory.createWizardDefinition(
	                            path0,
	                            locale[0],
	                            (short)0,
	                            context.getResourceAsStream(path0)
	                        );
	                    if(wizardDefinition0 != null) {
	                        wizardDefinitions.get(
	                            locale[0]
	                        ).add(wizardDefinition0);
	                        SysLog.info("Loaded " + path0 + " (forClass=" + wizardDefinition0.getForClass() + ")");
	                        // Load locale-specific wizards
	                        for(int j = 1; j < locale.length; j++) {
	                            if(locale[j] != null) {
	                                if(wizardDefinitions.get(locale[j]) == null) {
	                                    wizardDefinitions.put(
	                                        locale[j], 
	                                        new ArrayList<WizardDefinition>()
	                                    );
	                                }
	                                String path = path0.replace(locale[0], locale[j]);
	                                WizardDefinition wizardDefinition = null;
	                                try {
	                                    wizardDefinition = WizardDefinitionFactory.createWizardDefinition(
	                                        path,
	                                        locale[j],
	                                        (short)j,
	                                        context.getResourceAsStream(path)
	                                    );
	                                } catch(Exception ignore) {
	                        			SysLog.trace("Exception ignored", ignore);
	                                }
	                                if(wizardDefinition != null) {
	                                    wizardDefinitions.get(
	                                        locale[j]
	                                    ).add(wizardDefinition);
	                                    SysLog.info("Loaded " + path + " (forClass=" + wizardDefinition.getForClass() + ")");     
	                                } else {
	                                    int fallbackLocaleIndex = 0;
	                                    for(int k = j-1; k >= 0; k--) {
	                                        if((locale[k] != null) && locale[j].substring(0,2).equals(locale[k].substring(0,2))) {
	                                            fallbackLocaleIndex = k;
	                                            break;
	                                        }
	                                    }
	                                    List<WizardDefinition> fallbackWizardDefinitions = wizardDefinitions.get(locale[fallbackLocaleIndex]);
	                                    wizardDefinitions.get(
	                                        locale[j]
	                                    ).add(fallbackWizardDefinitions.get(fallbackWizardDefinitions.size()-1));
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }    
        }
        // Load app-style wizards
        for(String suffix: Arrays.asList("index.jsp", "index.xhtml")) {
	        Set<String> wizardPaths = context.getResourcePaths("/wizards");
	        if(wizardPaths != null) {
	            for(String path: wizardPaths) {
	            	try {
		            	if(
		            		path.endsWith("/") &&
		            		context.getResource(path + suffix) != null &&
		            		context.getResource(path + "META-INF/wizard.properties") != null
		            	) {
		                    WizardDefinition wizardDefinition = 
		                        WizardDefinitionFactory.createWizardDefinition(
		                        	path + suffix,
		                            "",
		                            (short)0,
		                            context.getResourceAsStream(path + "/META-INF/wizard.properties")
		                        );
		                    // Register for all locales
		                    for(int i = 0; i < locale.length; i++) {
		                    	if(wizardDefinitions.get(locale[i]) == null) {
		                            wizardDefinitions.put(
	                                    locale[i], 
	                                    new ArrayList<WizardDefinition>()
	                                );		                    		
		                    	}
		                        wizardDefinitions.get(
		                        	locale[i]
			                    ).add(wizardDefinition);
		                    }
		            	}
	            	} catch(Exception ignore) {}
	            }
	        }
        }
    	System.out.println(messagePrefix + "Done (" + wizardDefinitions.size() + " wizards)");
    	SysLog.info("Done", wizardDefinitions.size());    	        
        return new WizardDefinitionFactory(
            wizardDefinitions,
            model
        );
    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------

}

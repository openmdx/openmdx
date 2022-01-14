/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: WizardDefinitionFactory 
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 * ---[ SMC (http://www.smc.it) ]---
 *
 * Allowed Jsf, MyFaces
 *
 */
package org.openmdx.portal.servlet.wizards;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;

/**
 * WizardDefinitionFactory
 *
 */
public class WizardDefinitionFactory implements Serializable {

    /**
     * Create wizard definition.
     * 
     * @param path
     * @param locale
     * @param localeIndex
     * @param is
     * @return
     * @throws ServiceException
     */
    public static WizardDefinition createWizardDefinition(
        String path,
        String locale,
        short localeIndex,
        InputStream is
    ) throws ServiceException {
    	if(path.endsWith("/index.jsp") || path.endsWith("/index.xhtml")) {
    		return new AppWizardDefinition(
	    		path,
	    		locale,
	    		localeIndex,
	    		is
	    	);
    	} else if(path.endsWith(".jsp")) {
            return new JspWizardDefinition(
                path,
                locale,
                localeIndex,
                is
            );
        } else if(path.endsWith(".config")) {
            // Ignore configuration files
            return null;
        } else {
        	SysLog.info("Unsupported wizard definition format. Supported formats are [.jsp]", path);
            return null;
        }
    }

    /**
     * Constructor 
     *
     * @param wizardDefinitions
     * @param model
     */
    public WizardDefinitionFactory(
        Map<String,List<WizardDefinition>> wizardDefinitions,
        Model_1_0 model
    ) {
        this.wizardDefinitions = wizardDefinitions;
        this.customizedDefinitions = new HashMap<String,Set<String>>();
        this.model = model;
        SysLog.info("loaded wizards=" + this.wizardDefinitions.keySet());
    }

    /**
     * Get wizard definitions.
     * 
     * @param locale
     * @return
     */
    public List<WizardDefinition> getWizardDefinitions(
        String locale
    ) {
        return this.wizardDefinitions.get(
            locale
        );
    }

    /**
     * Find wizard definition for given class and locale.
     * 
     * @param forClass
     * @param locale
     * @param orderPattern
     * @return
     */
    public WizardDefinition[] findWizardDefinitions(
        String forClass,
        String locale,
        String orderPattern
    ) {
        List<WizardDefinition> wizardDefinitions = this.getWizardDefinitions(locale);
        Map<String,WizardDefinition> matchingWizardDefinitions = new TreeMap<String,WizardDefinition>();
        // Get set of already matched definitions
        String id = forClass + ":" + locale;
        Set<String> customizedDefinitions = this.customizedDefinitions.get(id);
        if(customizedDefinitions == null) {
            this.customizedDefinitions.put(
                id, 
                customizedDefinitions = new HashSet<String>()
            );
        }
        int ii = 0;
        if(wizardDefinitions != null) {
	        for(WizardDefinition wizardDefinition: wizardDefinitions) {
	            try {
	                for(String wizardClass: wizardDefinition.getForClass()) {
	                    /**
	                     * A wizard definition matches if:
	                     * <ul>
	                     *   <li>orderKey == null and order is numeric
	                     *   <li>orderKey != null and is equal to order
	                     * </ul>  
	                     */                  
	                    List<String> order = wizardDefinition.getOrder();
	                    if(
	                        model.isSubtypeOf(forClass, wizardClass) &&
	                        ((orderPattern == null) && !customizedDefinitions.contains(wizardDefinition.getName()) || 
	                                ((orderPattern != null) && (order != null) && order.contains(orderPattern)))
	                    ) {
	                        matchingWizardDefinitions.put(
	                            order + ":" + ii,
	                            wizardDefinition
	                        );
	                    }
	                    // Wizard is customized if multiple orders are customized and order is not numeric, i.e. a qualified element name
	                    boolean isCustomized = 
	                        (order != null) && 
	                        (order.size() >= 1) && 
	                        !Character.isDigit(((String)order.get(0)).charAt(0));
	                    if(isCustomized) {
	                        customizedDefinitions.add(
	                            wizardDefinition.getName()
	                        );
	                    }
	                }
	            } catch(ServiceException ignore) {}
	            ii++;
	        }
        }
        return (WizardDefinition[])matchingWizardDefinitions.values().toArray(new WizardDefinition[matchingWizardDefinitions.size()]);
    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 53793339941479036L;

    private final Map<String,List<WizardDefinition>> wizardDefinitions;
    /**
     * The factory manages a set of customized definitions for each 
     * forClass and locale. This allows to put all non or wrong customized 
     * wizard definitions in the default wizards menu.
     */ 
    private final Map<String,Set<String>> customizedDefinitions;
    private final Model_1_0 model;
}

//--- End of File -----------------------------------------------------------

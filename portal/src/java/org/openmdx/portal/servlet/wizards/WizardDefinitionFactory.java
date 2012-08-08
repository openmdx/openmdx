/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: WizardDefinitionFactory.java,v 1.17 2008/09/19 20:54:23 wfro Exp $
 * Description: WizardDefinitionFactory 
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/19 20:54:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */

package org.openmdx.portal.servlet.wizards;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

public class WizardDefinitionFactory
implements Serializable {

    //-------------------------------------------------------------------------
    public static WizardDefinition createWizardDefinition(
        String path,
        String locale,
        short localeIndex,
        InputStream is
    ) throws ServiceException {
        if(path.endsWith(".jsp")) {
            return new JspWizardDefinition(
                path,
                locale,
                localeIndex,
                is
            );
        }
        else if(path.endsWith(".config")) {
            // Ignore configuration files
            return null;
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("name", path)
                },
                "Unsupported wizard definition format. Supported formats are [.jsp]"
            );
        }
    }

    //-------------------------------------------------------------------------
    public WizardDefinitionFactory(
        Map reports,
        Model_1_0 model
    ) {
        this.allDefinitions = reports;
        this.customizedDefinitions = new HashMap<String,Set<String>>();
        this.model = model;
        AppLog.info("loaded wizards=" + this.allDefinitions.keySet());
    }

    //-------------------------------------------------------------------------
    public List getWizardDefinitions(
        String locale
    ) {
        return (List)this.allDefinitions.get(
            locale
        );
    }

    //-------------------------------------------------------------------------
    public WizardDefinition[] findWizardDefinitions(
        String forClass,
        String locale,
        String orderPattern
    ) {
        List wizardDefinitions = this.getWizardDefinitions(locale);
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
        for(
                Iterator i = wizardDefinitions.iterator();
                i.hasNext();
                ii++
        ) {
            WizardDefinition wizardDefinition = (WizardDefinition)i.next();
            try {
                for(
                        Iterator j = wizardDefinition.getForClass().iterator(); 
                        j.hasNext(); 
                ) {
                    String wizardClass = (String)j.next();
                    /**
                     * A wizard definition matches if:
                     * <ul>
                     *   <li>orderKey == null and order is numeric
                     *   <li>orderKey != null and is equal to order
                     * </ul>  
                     */                  
                    List order = wizardDefinition.getOrder();
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
            }
            catch(ServiceException e) {}
        }
        return (WizardDefinition[])matchingWizardDefinitions.values().toArray(new WizardDefinition[matchingWizardDefinitions.size()]);
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 53793339941479036L;

    private final Map allDefinitions;
    /**
     * The factory manages a set of customized definitions for each 
     * forClass and locale. This allows to put all non or wrong customized 
     * wizard definitions in the default wizards menu.
     */ 
    private final Map<String,Set<String>> customizedDefinitions;
    private final Model_1_0 model;
}

//--- End of File -----------------------------------------------------------

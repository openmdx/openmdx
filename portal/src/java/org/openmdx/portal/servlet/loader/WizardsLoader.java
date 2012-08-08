/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: WizardsLoader.java,v 1.11 2008/08/12 16:38:07 wfro Exp $
 * Description: ReportsLoader
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/12 16:38:07 $
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.portal.servlet.RoleMapper_1_0;
import org.openmdx.portal.servlet.wizards.WizardDefinition;
import org.openmdx.portal.servlet.wizards.WizardDefinitionFactory;

public class WizardsLoader 
    extends Loader {

    //-------------------------------------------------------------------------
    public WizardsLoader(
        ServletContext context,
        RoleMapper_1_0 roleMapper        
    ) {
        super(
            context,
            roleMapper
        );
    }
  
    //-------------------------------------------------------------------------
    /**
     * Loads wizard definitions.
     */
    @SuppressWarnings("unchecked")
    synchronized public WizardDefinitionFactory loadWizardDefinitions(
        ServletContext context,
        String[] locale,
        Model_1_0 model
    ) throws ServiceException {

        Map wizardDefinitions = new HashMap();
        int fallbackLocaleIndex = 0;
        for(int i = 0; i < locale.length; i++) {
            Set wizardPaths = new HashSet();
            if(locale[i] != null) {
                if(wizardDefinitions.get(locale[i]) == null) {
                    wizardDefinitions.put(
                        locale[i],
                        new ArrayList()
                    );
                }
                fallbackLocaleIndex = 0;
                System.out.println("Loading wizards for locale " + locale[i]);     
                wizardPaths = context.getResourcePaths("/wizards/" + locale[i]);
                if(wizardPaths == null) {
                    for(int j = i-1; j >= 0; j--) {
                        if((locale[j] != null) && locale[i].substring(0,2).equals(locale[j].substring(0,2))) {
                            fallbackLocaleIndex = j;
                            break;
                        }
                    }
                    System.out.println(locale[i] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
                    wizardPaths = context.getResourcePaths("/wizards/" + locale[fallbackLocaleIndex]);
                }
                if(wizardPaths != null) {
                    for(Iterator j = wizardPaths.iterator(); j.hasNext(); ) {
                        String path = (String)j.next();
                        if(!path.endsWith("/")) {            
                            WizardDefinition wizardDefinition = 
                                WizardDefinitionFactory.createWizardDefinition(
                                    path,
                                    locale[i],
                                    (short)i,
                                    context.getResourceAsStream(path)
                                );
                            if(wizardDefinition != null) {
                                ((List)wizardDefinitions.get(
                                    locale[i]
                                )).add(wizardDefinition);
                                System.out.println("Loaded " + path + " (forClass=" + wizardDefinition.getForClass() + ")");     
                            }
                        }
                    }
                }
            }
        }
        return new WizardDefinitionFactory(
            wizardDefinitions,
            model
        );
        
    }
  
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
        
}

//--- End of File -----------------------------------------------------------

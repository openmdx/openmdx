/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ReportsLoader.java,v 1.17 2009/10/29 23:37:19 wfro Exp $
 * Description: ReportsLoader
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/29 23:37:19 $
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.RoleMapper_1_0;
import org.openmdx.portal.servlet.reports.ReportDefinition;
import org.openmdx.portal.servlet.reports.ReportDefinitionFactory;

public class ReportsLoader 
    extends Loader {

    //-------------------------------------------------------------------------
    public ReportsLoader(
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
     * Loads report configuration.
     */
    @SuppressWarnings("unchecked")
    synchronized public ReportDefinitionFactory loadReportDefinitions(
        ServletContext context,
        String[] locale,
        Model_1_0 model
    ) throws ServiceException {
        Map reportDefinitions = new HashMap();
        int fallbackLocaleIndex = 0;
        SysLog.info("Loading reports");
        String messagePrefix = new Date() + "  ";
        System.out.println(messagePrefix + "Loading reports");
        for(int i = 0; i < locale.length; i++) {
            Set reportPaths = new HashSet();
            if(locale[i] != null) {
                if(reportDefinitions.get(locale[i]) == null) {
                    reportDefinitions.put(
                        locale[i],
                        new ArrayList()
                    );
                }
                fallbackLocaleIndex = 0;
                SysLog.info("Loading reports from /WEB-INF/config/report/" + locale[i]);
                reportPaths = context.getResourcePaths("/WEB-INF/config/report/" + locale[i]);
                if(reportPaths == null) {
                    for(int j = i-1; j >= 0; j--) {
                        if((locale[j] != null) && locale[i].substring(0,2).equals(locale[j].substring(0,2))) {
                            fallbackLocaleIndex = j;
                            break;
                        }
                    }
                    SysLog.info(locale[i] + " not found. Fallback to " + locale[fallbackLocaleIndex]);
                    reportPaths = context.getResourcePaths("/WEB-INF/config/report/" + locale[fallbackLocaleIndex]);
                }
                if(reportPaths != null) {
                    for(Iterator j = reportPaths.iterator(); j.hasNext(); ) {
                        String path = (String)j.next();
                        if(!path.endsWith("/")) {            
                            ReportDefinition def = null;
                            try {
                                def = ReportDefinitionFactory.createReportDefinition(
                                    path,
                                    locale[i],
                                    (short)i,
                                    context.getResourceAsStream(path)
                                );
                            }
                            catch(Exception e) {
                                System.out.println(messagePrefix + "STATUS: " + e.getMessage() + " (for more info see log)");
                                new ServiceException(e).log();
                            }
                            if(def != null) {
                                SysLog.info("Loaded report " + path + " (forClass=" + def.getForClass() + ")");
                                ((List)reportDefinitions.get(
                                    locale[i]
                                )).add(def);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(messagePrefix + "Done (" + reportDefinitions.size() + " reports)");
        SysLog.info("Done",  reportDefinitions.size());
        return new ReportDefinitionFactory(
            reportDefinitions,
            model
        );        
    }
  
    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
        
}

//--- End of File -----------------------------------------------------------

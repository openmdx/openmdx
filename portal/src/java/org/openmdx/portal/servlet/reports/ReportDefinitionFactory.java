/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ReportDefinitionFactory.java,v 1.22 2009/06/16 17:08:27 wfro Exp $
 * Description: TextsFactory
 * Revision:    $Revision: 1.22 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/16 17:08:27 $
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
package org.openmdx.portal.servlet.reports;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

public class ReportDefinitionFactory
implements Serializable {

    //-------------------------------------------------------------------------
    public static ReportDefinition createReportDefinition(
        String path,
        String locale,
        short localeIndex,
        InputStream is
    ) throws ServiceException {
        if(path.endsWith(".rptdesign")) {
            return new BirtReportDefinition(
                path,
                locale,
                localeIndex,
                is
            );
        }
        else if(path.endsWith(".jsp")) {
            return new JspReportDefinition(
                path,
                locale,
                localeIndex,
                is
            );
        }
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unsupported report definition format. Supported formats are [.rptdesign,.jsp]",
                new BasicException.Parameter("name", path)
            );
        }
    }

    //-------------------------------------------------------------------------
    public ReportDefinitionFactory(
        Map reports,
        Model_1_0 model
    ) {
        this.reportDefinitions = reports;
        this.customizedDefinitions = new HashMap<String,Set<String>>();
        this.model = model;
        SysLog.info("loaded reports=" + this.reportDefinitions.keySet());
    }

    //-------------------------------------------------------------------------
    public List getReportDefinitions(
        String locale
    ) {
        return (List)this.reportDefinitions.get(
            locale
        );
    }

    //-------------------------------------------------------------------------
    public ReportDefinition[] findReportDefinitions(
        String forClass,
        String locale,
        String orderPattern
    ) {
        List reportDefinitions = this.getReportDefinitions(locale);
        // Get set of already matched definitions
        String id = forClass + ":" + locale;
        Set<String> customizedDefinitions = this.customizedDefinitions.get(id);
        if(customizedDefinitions == null) {
            this.customizedDefinitions.put(
                id, 
                customizedDefinitions = new HashSet<String>()
            );
        }
        Map<String,ReportDefinition> matchingReportDefinitions = new TreeMap<String,ReportDefinition>();

        int ii = 0;
        for(
                Iterator i = reportDefinitions.iterator();
                i.hasNext();
                ii++
        ) {
            ReportDefinition reportDefinition = (ReportDefinition)i.next();
            try {
                for(
                        Iterator j = reportDefinition.getForClass().iterator(); 
                        j.hasNext(); 
                ) {
                    String reportClass = (String)j.next();
                    /**
                     * A report definition matches if:
                     * <ul>
                     *   <li>orderKey == null and order is numeric
                     *   <li>orderKey != null and is equal to order
                     * </ul>  
                     */
                    List order = reportDefinition.getOrder();
                    if(
                            model.isSubtypeOf(forClass, reportClass) &&
                            ((orderPattern == null) && !customizedDefinitions.contains(reportDefinition.getName()) || 
                                    ((orderPattern != null) && (order != null) && order.contains(orderPattern)))
                    ) {
                        matchingReportDefinitions.put(
                            order + ":" + ii,
                            reportDefinition
                        );
                    }
                    // Report is customized if multiple orders are customized and order is not numeric, i.e. a qualified element name
                    boolean isCustomized = 
                        (order != null) && 
                        (order.size() >= 1) && 
                        !Character.isDigit(((String)order.get(0)).charAt(0));                    
                    if(isCustomized) {
                        customizedDefinitions.add(
                            reportDefinition.getName()
                        );
                    }
                }
            }
            catch(ServiceException e) {}
        }
        return (ReportDefinition[])matchingReportDefinitions.values().toArray(new ReportDefinition[matchingReportDefinitions.size()]);
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 53793339941479036L;

    private final Map reportDefinitions;

    /**
     * The factory manages a set of customized definitions for each 
     * forClass and locale. This allows to put all non or wrong customized 
     * report definitions in the default reports menu.
     */ 
    private final Map<String,Set<String>> customizedDefinitions;
    private final Model_1_0 model;
}

//--- End of File -----------------------------------------------------------

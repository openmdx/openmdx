/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: FindSearchFieldValuesAction 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.WebKeys;

/**
 * FindSearchFieldValuesAction
 *
 */
public class FindSearchFieldValuesAction extends UnboundAction {
    
    public final static int EVENT_ID = 55;

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.action.UnboundAction#perform(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.openmdx.portal.servlet.ApplicationContext, java.lang.String, java.util.Map)
     */
    @Override
    public ActionPerformResult perform(
        HttpServletRequest request,
        HttpServletResponse response,
        ApplicationContext app,
        String parameter,
        Map<String,String[]> requestParameters
    ) throws IOException {
        String objectXri = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);    	
        String qualifiedReferenceName = Action.getParameter(parameter, Action.PARAMETER_NAME);
        String featureName = Action.getParameter(parameter, Action.PARAMETER_FILTER_BY_FEATURE);
        String[] filterValues = (String[])requestParameters.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
        try(
            PrintWriter pw = this.getWriter(
                request, 
                response
            )
        ){
            PersistenceManager pm = null;
            try {
            	RefObject_1_0 object = null;
            	if(objectXri != null && !objectXri.isEmpty()) {
    	            Path objectIdentity = new Path(objectXri);
    	            pm = app.getNewPmData();
    	            object = (RefObject_1_0)pm.getObjectById(objectIdentity);
            	}
            	if(qualifiedReferenceName != null && featureName != null && filterValues != null && filterValues.length > 0) {
            		PortalExtension_1_0.SearchFieldDef searchFieldDef = app.getPortalExtension().getSearchFieldDef(qualifiedReferenceName, featureName, app);
            		if(searchFieldDef != null) {
            			List<String> values = searchFieldDef.findValues(object, filterValues[0], app);
                        pw.print("<ul class=\"" + CssClass.dropdown_menu.toString() + "\" style=\"display:block;\">\n");
                        for(String value: values) {
                        	pw.write("<li>");
                        	pw.write(app.getHtmlEncoder().encode(value, false));
                        	pw.write("</li>\n");
                        }
                        pw.print("</ul>\n");
            		}
            	}
            } catch(Exception e) {
                Throwables.log(e);
            } finally {
            	if(pm != null) {
            		pm.close();
            	}        	
            }
        }
        return new ActionPerformResult(
            ActionPerformResult.StatusCode.DONE
        );
    }

}

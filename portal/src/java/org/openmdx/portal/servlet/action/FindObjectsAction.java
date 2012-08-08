/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: FindObjectsAction.java,v 1.2 2011/07/07 22:35:36 wfro Exp $
 * Description: FindObjectsAction 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/07/07 22:35:36 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.WebKeys;

public class FindObjectsAction extends UnboundAction {
    
    public final static int EVENT_ID = 40;

	//-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public ActionPerformResult perform(
        HttpServletRequest request,
        HttpServletResponse response,
        ApplicationContext application,
        String parameter,
        Map<String,String[]> requestParameters
    ) throws IOException {
        String objectXri = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
        String referenceName = Action.getParameter(parameter, Action.PARAMETER_REFERENCE_NAME);
        String filterByType = Action.getParameter(parameter, Action.PARAMETER_FILTER_BY_TYPE);
        String filterByFeature = Action.getParameter(parameter, Action.PARAMETER_FILTER_BY_FEATURE);
        String[] filterValues = (String[])requestParameters.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
        ConditionType filterOperator = ConditionType.IS_LIKE;
        if(Action.getParameter(parameter, Action.PARAMETER_FILTER_OPERATOR) != null) {
            filterOperator = ConditionType.valueOf(
                Action.getParameter(parameter, Action.PARAMETER_FILTER_OPERATOR)
            );
        }
        String orderByFeature = Action.getParameter(parameter, Action.PARAMETER_ORDER_BY_FEATURE);
        // TODO: the option format allows to specify the output format 
//        String format = Action.getParameter(parameter, Action.PARAMETER_FORMAT);
        String positionAsString = Action.getParameter(parameter, Action.PARAMETER_POSITION);
        int position = positionAsString == null ? 
        	0 : 
        		new Integer(positionAsString).intValue();         
        String sizeAsString = Action.getParameter(parameter, Action.PARAMETER_SIZE);        
        int size = sizeAsString == null ? 
        	0 : 
        		new Integer(sizeAsString).intValue();

        // Output
        PrintWriter pw = this.getWriter(
            request, 
            response
        );
        PersistenceManager pm = null;
        try {
            Path objectIdentity = new Path(objectXri);
            pm = application.getNewPmData();
            RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(objectIdentity);
            List<Condition> conditions = new ArrayList<Condition>(
                application.getPortalExtension().getFindObjectsBaseFilter(
                    application, 
                    parent,
                    referenceName
                )
            );            
            // filterByType
            if(
                (filterByType != null) &&
                (filterByType.length()  > 0)
            ) {
                conditions.add(
                    new IsInstanceOfCondition(filterByType)
                );                
            }
            // filterByFeature
            if(
                (filterByFeature != null) && (filterByFeature.length() > 0) &&
                (filterValues != null) && (filterValues.length > 0)
            ) {
                Model_1_0 model = application.getModel();
                // Is filter feature numeric?
                ModelElement_1_0 parentDef = ((RefMetaObject_1)parent.refMetaObject()).getElementDef();                
                ModelElement_1_0 referenceDef = 
                    (ModelElement_1_0)((Map)parentDef.objGetValue("reference")).get(referenceName);
                if(referenceDef != null) {
                    ModelElement_1_0 referencedType = model.getElement(referenceDef.objGetValue("type"));
                    ModelElement_1_0 filterFeatureDef = model.getFeatureDef(referencedType, filterByFeature, true);
                    if(filterFeatureDef != null) {
                        boolean filterFeatureIsNumeric = model.isNumericType(filterFeatureDef.objGetValue("type"));
                        String filterValue = filterValues[0];
                        // Remove trailing "[ text ]". This suffix was most probably by the autocompleter.
                        if(filterValue.indexOf(" [") >= 0 && filterValue.endsWith("]")) {
                        	filterValue = filterValue.substring(0, filterValue.indexOf(" ["));
                        }
                        conditions.add(
                            new AnyTypeCondition(
                                Quantifier.THERE_EXISTS,
                                filterByFeature,
                                filterOperator,
                                filterFeatureIsNumeric ? Integer.valueOf(filterValue) :
                                filterOperator == ConditionType.IS_LIKE ? "(?i).*" + filterValue + ".*": filterValue
                            )
                        );
                    }
                }
            }
            // Order
            OrderSpecifier s = null;
            if(
                (orderByFeature != null) && (orderByFeature.length() > 0)
            ) {
                s = new OrderSpecifier(
                    orderByFeature,
                    SortOrder.ASCENDING
                );
            }
            Collection allObjects = (Collection)parent.refGetValue(referenceName);
            List filteredObjects = null;
            try {
                filteredObjects = ((RefContainer)allObjects).refGetAll(
                    new org.openmdx.base.query.Filter(
                        conditions,
                        s == null ? null : Collections.singletonList(s),
                        null // extension
                    )
                );
            }
            catch(UnsupportedOperationException e) {
                filteredObjects = new ArrayList(
                    ((RefContainer)allObjects).refGetAll(null)
                );
            }
            pw.print("<ul class=\"autocomplete\" >\n");
            int ii = 0;
            for(
                ListIterator i = filteredObjects.listIterator(position);
                i.hasNext() && (ii < size);
                ii++
            ) {
                RefObject_1_0 obj = (RefObject_1_0)i.next();
                pw.write("<li>");
                String title = application.getPortalExtension().getTitle(
                    obj, 
                    application.getCurrentLocaleAsIndex(), 
                    application.getCurrentLocaleAsString(), 
                    false, // asShortTitle
                    application
                );
                if(filterByFeature.length() > 0) {
                    try {
                        title += " [" + obj.refGetValue(filterByFeature) + "]";
                    } 
                    catch(Exception e) {}
                }
                pw.write(
                    application.getHtmlEncoder().encode(title, false)
                );
                // Mark non-primary fields as informal 
                pw.write("<span class=\"informal\">");
                pw.write("<div style=\"display:none\">");
                pw.write(response.encodeURL(obj.refMofId()));
                pw.write("</div>");
                pw.write("</span>");
                pw.write("</li>\n");
            }
            pw.print("</ul>\n");
        }
        catch(Exception e) {
            new ServiceException(e).log();
        } finally {
        	if(pm != null) {
        		pm.close();
        	}
        }
        pw.close();  
        return new ActionPerformResult(
            ActionPerformResult.StatusCode.DONE
        );
    }
    
}

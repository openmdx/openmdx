/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: FindObjectsAction 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2020, OMEX AG, Switzerland
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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AnyTypeCondition;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.IsInstanceOfCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.SortOrder;
import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryFilterRecord;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.WebKeys;

/**
 * FindObjectsAction
 *
 */
public class FindObjectsAction extends UnboundAction {
    
    public final static int EVENT_ID = 40;

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.action.UnboundAction#perform(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.openmdx.portal.servlet.ApplicationContext, java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public ActionPerformResult perform(
        HttpServletRequest request,
        HttpServletResponse response,
        ApplicationContext app,
        String parameter,
        Map<String,String[]> requestParameters
    ) throws IOException {
        String objectXri = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
        String referenceName = Action.getParameter(parameter, Action.PARAMETER_REFERENCE_NAME);
        String filterByType = Action.getParameter(parameter, Action.PARAMETER_FILTER_BY_TYPE);
        String filterByFeature = Action.getParameter(parameter, Action.PARAMETER_FILTER_BY_FEATURE);
        String filterByQuery = Action.getParameter(parameter, Action.PARAMETER_FILTER_BY_QUERY);
        String[] filterValues = (String[])requestParameters.get(WebKeys.REQUEST_PARAMETER_FILTER_VALUES);
        ConditionType filterOperator = ConditionType.IS_LIKE;
        if(Action.getParameter(parameter, Action.PARAMETER_FILTER_OPERATOR) != null) {
            filterOperator = ConditionType.valueOf(
                Action.getParameter(parameter, Action.PARAMETER_FILTER_OPERATOR)
            );
        }
        String orderByFeature = Action.getParameter(parameter, Action.PARAMETER_ORDER_BY_FEATURE);
        String positionAsString = Action.getParameter(parameter, Action.PARAMETER_POSITION);
        int position = positionAsString == null ? 0 : Integer.parseInt(positionAsString);         
        String sizeAsString = Action.getParameter(parameter, Action.PARAMETER_SIZE);        
        int size = sizeAsString == null ? 0 : new Integer(sizeAsString).intValue();
        // Output
        try(
            PrintWriter pw = this.getWriter(
                request, 
                response
            )
        ) {
            PersistenceManager pm = null;
            try {
                Path objectIdentity = new Path(objectXri);
                pm = app.getNewPmData();
                RefObject_1_0 parent = (RefObject_1_0)pm.getObjectById(objectIdentity);
                List<ConditionRecord> conditions = new ArrayList<ConditionRecord>(
                    app.getPortalExtension().getFindObjectsBaseFilter(
                        app, 
                        parent,
                        referenceName
                    )
                );
                List<FeatureOrderRecord> orderSpecifiers = new ArrayList<FeatureOrderRecord>();
                // filterByType
                if((filterByType != null) && !filterByType.isEmpty()) {
                    // filterByQuery
                    if((filterByQuery != null) && !filterByQuery.isEmpty()) {
                        Query_2Facade queryFacade = Facades.newQuery(objectIdentity.getDescendant(referenceName));
                        queryFacade.setQueryType(filterByType);
                        queryFacade.setQuery(filterByQuery);
                        queryFacade.setPosition(position);
                        queryFacade.setSize(size);
                        javax.jdo.Query query = pm.newQuery(
                        	org.openmdx.base.persistence.cci.Queries.QUERY_LANGUAGE, 
                        	queryFacade.getDelegate()
                        );
                        if(query instanceof RefQuery_1_0) {
                        	RefQuery_1_0 refQuery = (RefQuery_1_0)query;
                        	QueryFilterRecord queryRecord = refQuery.refGetFilter();
                        	conditions.addAll(queryRecord.getCondition());
                        	orderSpecifiers.addAll(queryRecord.getOrderSpecifier());
                        }
                    } else {
	                    conditions.add(
	                        new IsInstanceOfCondition(filterByType)
	                    );
                    }
                }
                // filterByFeature
                if(
                    (filterByFeature != null) && !filterByFeature.isEmpty() &&
                    (filterValues != null) && (filterValues.length > 0)
                ) {
                    Model_1_0 model = app.getModel();
                    // Is filter feature numeric?
                    ModelElement_1_0 parentDef = ((RefMetaObject_1)parent.refMetaObject()).getElementDef();                
                    ModelElement_1_0 referenceDef = (ModelElement_1_0)parentDef.objGetMap("reference").get(referenceName);
                    if(referenceDef != null) {
                        ModelElement_1_0 referencedType = model.getElement(referenceDef.getType());
                        ModelElement_1_0 filterFeatureDef = model.getFeatureDef(referencedType, filterByFeature, true);
                        if(filterFeatureDef != null) {
                            boolean filterFeatureIsNumeric = model.isNumericType(filterFeatureDef.getType());
                            String filterValue = filterValues[0];
                            // Remove trailing "[ text ]". This suffix was most probably by the autocompleter.
                            if(filterValue.indexOf(" [") > 0) {
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
                if((orderByFeature != null) && !orderByFeature.isEmpty()) {
                    orderSpecifiers.add(
                    	new OrderSpecifier(
	                        orderByFeature,
	                        SortOrder.ASCENDING
	                    )
	               );
                }
                Collection<?> allObjects = (Collection<?>)parent.refGetValue(referenceName);
                List<RefObject_1_0> filteredObjects = null;
                try {
                    filteredObjects = ((RefContainer<RefObject_1_0>)allObjects).refGetAll(
                        new org.openmdx.base.query.Filter(
                            conditions,
                            orderSpecifiers,
                            null // extension
                        )
                    );
                } catch(UnsupportedOperationException e) {
                    filteredObjects = new ArrayList<RefObject_1_0>(
                        ((RefContainer<RefObject_1_0>)allObjects).refGetAll(null)
                    );
                }
                pw.print("<ul class=\"" + CssClass.dropdown_menu.toString() + "\" style=\"display:block;\">\n");
                int ii = 0;
                for(
                    ListIterator<RefObject_1_0> i = filteredObjects.listIterator(position);
                    i.hasNext() && (ii < size);
                    ii++
                ) {
                    RefObject_1_0 obj = (RefObject_1_0)i.next();
                    pw.write("<li>");
                    String title = app.getPortalExtension().getTitle(
                        obj, 
                        app.getCurrentLocaleAsIndex(), 
                        app.getCurrentLocaleAsString(), 
                        false, // asShortTitle
                        app
                    );
                    if(!filterByFeature.isEmpty()) {
                        try {
                            title += " [" + obj.refGetValue(filterByFeature) + "]";
                        } catch(Exception e) {}
                    }
                    pw.write(app.getHtmlEncoder().encode(title, false));
                    // Mark non-primary fields as informal 
                    pw.write("<span>");
                    pw.write("<div style=\"display:none\">");
                    pw.write(response.encodeURL(obj.refMofId()));
                    pw.write("</div>");
                    pw.write("</span>");
                    pw.write("</li>\n");
                }
                pw.print("</ul>\n");
            }
            catch(Exception e) {
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

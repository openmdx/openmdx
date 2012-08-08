/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: FindObjectsEventHandler.java,v 1.16 2008/05/31 23:40:07 wfro Exp $
 * Description: FindObjectsEventHandler 
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/31 23:40:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.eventhandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.spi.RefMetaObject_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.dataprovider.cci.Orders;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.compatibility.base.query.FilterProperty;
import org.openmdx.compatibility.base.query.Quantors;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;

public class FindObjectsEventHandler {
    
    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public static HandleEventResult handleRequest(
        HttpServletRequest request,
        HttpServletResponse response,
        ApplicationContext application,
        String parameter,
        String[] filterValues
    ) throws IOException {
        String objectXri = Action.getParameter(parameter, Action.PARAMETER_OBJECTXRI);
        String referenceName = Action.getParameter(parameter, Action.PARAMETER_REFERENCE_NAME);
        String filterByType = Action.getParameter(parameter, Action.PARAMETER_FILTER_BY_TYPE);
        String filterByFeature = Action.getParameter(parameter, Action.PARAMETER_FILTER_BY_FEATURE);
        short filterOperator = FilterOperators.IS_LIKE;
        if(Action.getParameter(parameter, Action.PARAMETER_FILTER_OPERATOR) != null) {
            filterOperator = (short)FilterOperators.fromString(
                Action.getParameter(parameter, Action.PARAMETER_FILTER_OPERATOR)
            );
        }
        String orderByFeature = Action.getParameter(parameter, Action.PARAMETER_ORDER_BY_FEATURE);
        // TODO: the option format allows to specify the output format 
//        String format = Action.getParameter(parameter, Action.PARAMETER_FORMAT);
        String positionAsString = Action.getParameter(parameter, Action.PARAMETER_POSITION);
        int position = positionAsString == null
            ? 0
            : new Integer(positionAsString).intValue();         
        String sizeAsString = Action.getParameter(parameter, Action.PARAMETER_SIZE);        
        int size = sizeAsString == null
            ? 0
            : new Integer(sizeAsString).intValue();

        // Output
        PrintWriter pw = EventHandlerHelper.getWriter(
            request, 
            response
        );
        try {
            Path objectIdentity = new Path(objectXri);
            RefObject_1_0 parent = (RefObject_1_0)application.getPmData().getObjectById(objectIdentity);
            List filterProperties = new ArrayList();
            filterProperties.addAll(
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
                filterProperties.add(
                    new FilterProperty(
                        Quantors.THERE_EXISTS,
                        SystemAttributes.OBJECT_INSTANCE_OF,
                        FilterOperators.IS_IN,
                        new String[]{filterByType}
                    )
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
                    (ModelElement_1_0)((Map)parentDef.values("reference").get(0)).get(referenceName);
                if(referenceDef != null) {
                    ModelElement_1_0 referencedType = model.getElement(referenceDef.values("type").get(0));
                    ModelElement_1_0 filterFeatureDef = model.getFeatureDef(referencedType, filterByFeature, true);
                    if(filterFeatureDef != null) {
                        boolean filterFeatureIsNumeric = model.isNumericType(filterFeatureDef.values("type").get(0));
                        String filterValue = filterValues[0];
                        filterProperties.add(
                            new FilterProperty(
                                Quantors.THERE_EXISTS,
                                filterByFeature,
                                filterOperator,
                                filterFeatureIsNumeric
                                    ? new Object[]{new Integer(filterValue)}
                                    : new Object[]{(filterOperator == FilterOperators.IS_LIKE ? "(?i)%" + filterValue + "%": filterValue)}
                            )
                        );
                    }
                }
            }
            // Order
            AttributeSpecifier s = null;
            if(
                (orderByFeature != null) && (orderByFeature.length() > 0)
            ) {
                s = new AttributeSpecifier(
                    orderByFeature,
                    0,
                    Orders.ASCENDING
                );
            }
            Collection allObjects = (Collection)parent.refGetValue(referenceName);
            List filteredObjects = null;
            try {
                filteredObjects = ((RefContainer)allObjects).refGetAll(
                    new org.openmdx.base.query.Filter(
                        filterProperties == null
                            ? null
                            : (FilterProperty[])filterProperties.toArray(new FilterProperty[filterProperties.size()]),
                        s == null
                            ? null
                            : new AttributeSpecifier[]{s}
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
                    application
                );
                if(filterByFeature.length() > 0) {
                    try {
                        title += " [" + obj.refGetValue(filterByFeature) + "]";
                    } catch(Exception e) {}
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
        }
        pw.close();  
        return new HandleEventResult(
            HandleEventResult.StatusCode.DONE
        );
    }

    //-------------------------------------------------------------------------
    public static boolean acceptsEvent(
        int event
    ) {
        return
            (event == Action.EVENT_FIND_OBJECTS);
    }
    
}

/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: FindObjectsAutocompleter.java,v 1.24 2008/08/12 16:38:05 wfro Exp $
 * Description: ListAutocompleteControl 
 * Revision:    $Revision: 1.24 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/12 16:38:05 $
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
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.base.query.FilterOperators;
import org.openmdx.portal.servlet.attribute.AttributeValue;
import org.openmdx.portal.servlet.attribute.ObjectReferenceValue;

public class FindObjectsAutocompleter
    implements Autocompleter_1_0, Serializable {
  
    //-------------------------------------------------------------------------
    /**
     * Create an input field control with Ajax.Autocompleter based auto completion.
     * Multiple triples {filterByFeature, filterOperator, orderByFeature} can be specified.
     * This allows to register different auto completion methods for a specific field. The 
     * completer with index 0 is active by default. A specific completer is selected 
     * by the user. The completer uses the EVENT_FIND_OBJECTS of ObjectInspectorServlet
     * in order to get the objects located at the specified target object and
     * reference.
     */
    public FindObjectsAutocompleter(
        Path target,
        String[] referenceName,
        String filterByType,
        String[] filterByFeature,
        String[] filterByLabel,
        int[] filterOperator,
        String[] orderByFeature
    ) {
        this.target = target;
        this.referenceName = referenceName;
        this.filterByType = filterByType;
        this.filterByFeature = filterByFeature;
        this.filterByLabel = filterByLabel;
        this.filterOperator = filterOperator; 
        this.orderByFeature = orderByFeature;
        
        this.findObjectsActions = new Action[filterByFeature.length]; 
        for(
            int i = 0;
            i < this.filterByFeature.length;
            i++
        ) {
            List<Action.Parameter> parameters = new ArrayList<Action.Parameter>();

            // xri
            parameters.add(
                new Action.Parameter(
                    Action.PARAMETER_OBJECTXRI,
                    this.target.toXri()
                )
            );
            // referenceName
            parameters.add(
                new Action.Parameter(
                    Action.PARAMETER_REFERENCE_NAME,
                    this.referenceName[i]
                )
            );
            // filterByType
            if(this.filterByType != null) {            
                parameters.add(
                    new Action.Parameter(
                        Action.PARAMETER_FILTER_BY_TYPE,
                        this.filterByType
                    )
                );
            }            
            // filterByFeature
            if(this.filterByFeature[i] != null) {            
                parameters.add(
                    new Action.Parameter(
                        Action.PARAMETER_FILTER_BY_FEATURE,
                        this.filterByFeature[i]
                    )
                );
            }
            // filterOperator
            parameters.add(
                new Action.Parameter(
                    Action.PARAMETER_FILTER_OPERATOR,
                    FilterOperators.toString(this.filterOperator[i])
                )
            );
            // orderByByFeature
            if(this.orderByFeature[i] != null) {            
                parameters.add(
                    new Action.Parameter(
                        Action.PARAMETER_ORDER_BY_FEATURE,
                        this.orderByFeature[i]
                    )
                );
            }
            // position
            parameters.add(
                new Action.Parameter(
                    Action.PARAMETER_POSITION,
                    "0"
                )
            );
            // size
            parameters.add(
                new Action.Parameter(
                    Action.PARAMETER_SIZE,
                    "20"
                )
            );
            this.findObjectsActions[i] = new Action(
                Action.EVENT_FIND_OBJECTS,
                parameters.toArray(new Action.Parameter[parameters.size()]),
                this.filterByLabel[i],
                true
            );     
        }
    }
    
    //-----------------------------------------------------------------------
    public void paint(
        HtmlPage p,
        String id,
        int index,
        String fieldName,
        AttributeValue currentValue,
        boolean numericCompare,
        CharSequence tdTag,
        CharSequence inputFieldDivClass,
        CharSequence inputFieldClass,        
        CharSequence imgTag
    ) throws ServiceException {
        
        AppLog.detail("> paint");        
        id = (id == null) || (id.length() == 0)
            ? fieldName + (index >= 0 ? "[" + index + "]" : "")
            : id;
        HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();
        
        // Get current value as object reference
        ObjectReference objectReference = null;
        if(
            (currentValue != null) &&
            (currentValue instanceof ObjectReferenceValue)
        ) {
            objectReference = (ObjectReference)currentValue.getValue(false);
        }
        // Autogenerate id if none is specified
        String acName = "ac_" + fieldName.replaceAll(":", "_").replaceAll("!", "_") + Integer.toString(index);
        
        p.write("<div class=\"autocompleterMenu\">");
        p.write("  <ul id=\"nav\" class=\"nav\" onmouseover=\"sfinit(this);\" >");
        p.write("    <li><a href=\"#\">", p.getImg("border=\"0\" alt=\"\" src=\"", p.getResourcePath("images/"), WebKeys.ICON_AUTOCOMPLETE_SELECT, "\""), "</a>");
        p.write("      <ul onclick=\"this.style.left='-999em';\" onmouseout=\"this.style.left='';\">");
        for(int i = 0; i < findObjectsActions.length; i++) {
            p.write("        <li", (i == 0 ? " class=\"selected\"" : ""), "><a href=\"#\"", p.getOnClick("javascript:navSelect(this);", acName, ".url= ", p.getEvalHRef(findObjectsActions[i], false), ";return false;"), "><span>&nbsp;&nbsp;&nbsp;</span>", htmlEncoder.encode(findObjectsActions[i].getTitle(), false), "</a></li>");
        }
        p.write("      </ul>");
        p.write("    </li>");
        p.write("  </ul>");                    
        p.write("</div>");       
        p.write("<div ", (inputFieldDivClass == null ? "" : inputFieldDivClass), "><input type=\"text\" ", (inputFieldClass == null ? "" : inputFieldClass), " id=\"", id, ".Title\" name=\"", id, ".Title\" tabindex=\"", Integer.toString(index), "\" value=\"", (objectReference == null ? "" : objectReference.getTitle()), "\" />", (imgTag == null ? "" : "&nbsp;" + imgTag) ,"</div>");
        p.write("<input type=\"hidden\" class=\"valueLLocked\" id=\"", id, "\" name=\"", id, "\" readonly value=\"", (objectReference == null ? "" : objectReference.refMofId()), "\" />");
        if(tdTag != null) {
            p.write("</td>");
            p.write(tdTag);
        }
        p.write("<div class=\"autocomplete\" id=\"", id, ".Update\" style=\"display:none;z-index:500;\"></div>");
        p.write("<script type=\"text/javascript\" language=\"javascript\" charset=\"utf-8\">");
        p.write("  ", acName, " = new Ajax.Autocompleter(");
        p.write("    '", id, ".Title',");
        p.write("    '", id, ".Update',");
        p.write("    '", p.getEncodedHRef(findObjectsActions[0]), "',");
        p.write("    {");
        p.write("      ", "paramName: '", WebKeys.REQUEST_PARAMETER_FILTER_VALUES, "', ");
        p.write("      ", "minChars: 0,");
        p.write("      ", "afterUpdateElement: updateXriField");
        p.write("    }");
        p.write("  );");
        p.write("</script>");
        
    }

    //-----------------------------------------------------------------------
    public boolean hasFixedSelectableValues(
    ) {
        return false;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -1138020420475572050L;
    
    private final Path target;
    private final String[] referenceName;
    private final String filterByType;
    private final String[] filterByFeature;
    private final String[] filterByLabel;
    private final int[] filterOperator;
    private final String[] orderByFeature;
    private final Action[] findObjectsActions;
    
}

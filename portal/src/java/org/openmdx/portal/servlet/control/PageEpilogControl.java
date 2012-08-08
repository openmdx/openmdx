/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: PageEpilogControl.java,v 1.64 2008/06/01 11:26:18 wfro Exp $
 * Description: PageEpilogControl 
 * Revision:    $Revision: 1.64 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/01 11:26:18 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.ObjectView;
import org.openmdx.portal.servlet.view.ReferencePane;
import org.openmdx.portal.servlet.view.ShowObjectView;
import org.openmdx.portal.servlet.view.View;

public class PageEpilogControl
    extends Control
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public PageEpilogControl(
        String id,
        String locale,
        int localeAsIndex,
        ControlFactory controlFactory
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory
        );
    }
    
    //-----------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        
        AppLog.detail("> paint");        
        
        ObjectView view = (ObjectView)p.getView();
        ApplicationContext app = view.getApplicationContext();
        SimpleDateFormat dateFormatter = DateValue.getLocalizedDateFormatter(
            null, 
            true,
            app
        );
        SimpleDateFormat dateTimeFormatter = DateValue.getLocalizedDateTimeFormatter(
            null, 
            true, 
            app
        );        
        boolean editMode = view instanceof EditObjectView;
        String guiLook = app.getCurrentGuiMode();
        boolean noLayoutManager = 
            guiLook.equals(WebKeys.SETTING_GUI_MODE_BASIC);
        
        int currentChartId = p.getProperty(HtmlPage.PROPERTY_CHART_ID) != null
            ? ((Integer)p.getProperty(HtmlPage.PROPERTY_CHART_ID)).intValue()
            : 0;
        int nActiveTab = p.getProperty(HtmlPage.PROPERTY_N_ACTIVE_TAB) != null
            ? ((Integer)p.getProperty(HtmlPage.PROPERTY_N_ACTIVE_TAB)).intValue()
            : 0;
          
        if(!editMode) {
            if(currentChartId > 0) {
                p.write("<script language=\"javascript\" type=\"text/javascript\" src=\"javascript/diagram.js\"></script>");
                p.write("<script language=\"javascript\" type=\"text/javascript\" src=\"javascript/chart.js\"></script>");
            }
        }
        
        // Init scripts
        p.write("<script language=\"javascript\" type=\"text/javascript\">");
        
        // Popup's for multi-valued attributes
        // multi-valued Strings
        p.write("    var multiValuedHigh = 1; // 0..multiValuedHigh-1");
        p.write("");
        p.write("    function editstrings_onchange_checkbox(checkbox, field) {");
        p.write("      if(!checkbox.checked) {");
        p.write("        field.value = \"*\";");
        p.write("      }");
        p.write("      else {");
        p.write("        field.value = \"\";");
        p.write("      }");
        p.write("    }");
        p.write("");
        p.write("    editstrings_maxLength = 2147483647;");
        p.write("    function editstrings_onchange_field(checkbox, field) {");
        p.write("      if(field.value.length > 0) {");
        p.write("        checkbox.checked = true;");
        p.write("        if(field.value.length > editstrings_maxLength) {");
        p.write("          field.value = field.value.substr(0, editstrings_maxLength)");
        p.write("        }");
        p.write("      }");
        p.write("    }");
        p.write("");
        p.write("    function editstrings_click_OK() {");
        p.write("      var nFields = 0;");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if($('editstringIsSelected_' + i).checked) {");
        p.write("          nFields = i+1;");
        p.write("        }");
        p.write("      }");
        p.write("      values = \"\";");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if(i < nFields) values += (i==0 ? \"\" : \"\\n\") + ($('editstringIsSelected_' + i).checked ? $('editstringField_' + i).value : \"#NULL\");");
        p.write("      }");
        p.write("      POPUP_FIELD.value = values;");
        p.write("      editstrings_click_Cancel();");
        p.write("    }");
        p.write("");
        p.write("    function editstrings_click_Cancel() {");
        p.write("      var IfrRef = document.getElementById('DivShim');");
        p.write("      IfrRef.style.display = 'none';");
        p.write("      shownPopup.style.display =  'none';");
        p.write("    }");
        p.write("");
        p.write("    function editstrings_on_load() {");
        p.write("      var i = 0;");
        p.write("      while ($('editstringrow' + i)) {");
        p.write("        var toBeRemoved = $('editstringrow' + i);");
        p.write("        toBeRemoved.parentNode.removeChild(toBeRemoved);");
        p.write("        i += 1;");
        p.write("      }");
        p.write("      var values = POPUP_FIELD.value.split(\"\\n\");");
        p.write("      var templateRow = $('editstringrow');");
        p.write("      var el_idx = $('editstringIdx_');");
        p.write("      var el_checkbox = $('editstringIsSelected_');");
        p.write("      var el_field = $('editstringField_');");
        p.write("");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        value = RTrim(values.length > i ? values[i] : \"#NULL\");");
        p.write("        el_checkbox.name = 'isSelected' + i;");
        p.write("        el_field.name = 'field' + i;");
        p.write("        el_idx.id = 'editstringIdx_' + i;");
        p.write("        el_checkbox.id = 'editstringIsSelected_' + i;");
        p.write("        el_field.id = 'editstringField_' + i;");
        p.write("        el_idx.value = i;");
        p.write("        var newRow = templateRow.cloneNode(true);");
        p.write("        newRow.id = 'editstringrow' + i;");
        p.write("        el_checkbox.name = 'isSelected';");
        p.write("        el_field.name = 'field';");
        p.write("        el_idx.id = 'editstringIdx_';");
        p.write("        el_checkbox.id = 'editstringIsSelected_';");
        p.write("        el_field.id = 'editstringField_';");
        p.write("        templateRow.parentNode.appendChild(newRow);");
        p.write("        newRow.style.display = 'block';");
        p.write("        $('editstringIdx_' + i).appendChild(document.createTextNode(i + ':'));");
        p.write("        $('editstringField_' + i).value = value != \"#NULL\" ? value : \"\";");
        p.write("        $('editstringIsSelected_' + i).checked = value != \"#NULL\";");
        p.write("      }");
        p.write("      templateRow.style.display = 'none';");
        p.write("    }");
        // multi-valued Numbers
        p.write("    function editnumbers_onchange_checkbox(checkbox, field) {");
        p.write("      if(!checkbox.checked) {");
        p.write("        field.value = \"0.00\";");
        p.write("      }");
        p.write("      else {");
        p.write("        field.value = \"\";");
        p.write("      }");
        p.write("    }");
        p.write("");
        p.write("    function editnumbers_onchange_field(checkbox, field) {");
        p.write("      checkbox.checked = field.value.length > 0;");
        p.write("    }");
        p.write("");
        p.write("    function editnumbers_click_OK() {");
        p.write("      var nFields = 0;");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if($('editnumberIsSelected_' + i).checked) {");
        p.write("          nFields = i+1;");
        p.write("        }");
        p.write("      }");
        p.write("      values = \"\";");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if(i < nFields) values += (i==0 ? \"\" : \"\\n\") + ($('editnumberIsSelected_' + i).checked ? $('editnumberField_' + i).value : \"\");");
        p.write("      }");
        p.write("      POPUP_FIELD.value = values;");
        p.write("      editnumbers_click_Cancel();");
        p.write("    }");
        p.write("");
        p.write("    function editnumbers_click_Cancel() {");
        p.write("      var IfrRef = document.getElementById('DivShim');");
        p.write("      IfrRef.style.display = 'none';");
        p.write("      shownPopup.style.display =  'none';");
        p.write("    }");
        p.write("");
        p.write("    function editnumbers_on_load() {");
        p.write("      var i = 0;");
        p.write("      while ($('editnumberrow' + i)) {");
        p.write("        var toBeRemoved = $('editnumberrow' + i);");
        p.write("        toBeRemoved.parentNode.removeChild(toBeRemoved);");
        p.write("        i += 1;");
        p.write("      }");
        p.write("      var values = POPUP_FIELD.value.split(\"\\n\");");
        p.write("      var templateRow = $('editnumberrow');");
        p.write("      var el_idx = $('editnumberIdx_');");
        p.write("      var el_checkbox = $('editnumberIsSelected_');");
        p.write("      var el_field = $('editnumberField_');");
        p.write("");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        value = RTrim(values.length > i ? values[i] : \"#NULL\");");
        p.write("        el_checkbox.name = 'isSelected' + i;");
        p.write("        el_field.name = 'field' + i;");
        p.write("        el_idx.id = 'editnumberIdx_' + i;");
        p.write("        el_checkbox.id = 'editnumberIsSelected_' + i;");
        p.write("        el_field.id = 'editnumberField_' + i;");
        p.write("        el_idx.value = i;");
        p.write("        var newRow = templateRow.cloneNode(true);");
        p.write("        newRow.id = 'editnumberrow' + i;");
        p.write("        el_checkbox.name = 'isSelected';");
        p.write("        el_field.name = 'field';");
        p.write("        el_idx.id = 'editnumberIdx_';");
        p.write("        el_checkbox.id = 'editnumberIsSelected_';");
        p.write("        el_field.id = 'editnumberField_';");
        p.write("        templateRow.parentNode.appendChild(newRow);");
        p.write("        newRow.style.display = 'block';");
        p.write("        $('editnumberIdx_' + i).appendChild(document.createTextNode(i + ':'));");
        p.write("        $('editnumberField_' + i).value = value != \"#NULL\" ? value : \"\";");
        p.write("        $('editnumberIsSelected_' + i).checked =  (value.length > 0) && (value != \"#NULL\");");
        p.write("      }");
        p.write("      templateRow.style.display = 'none';");
        p.write("    }");        
        // multi-valued Dates
        p.write("    function editdates_onchange_checkbox(checkbox, field) {");
        p.write("      if(checkbox.checked) {");
        p.write("        field.value = \"\";");
        p.write("      }");
        p.write("    }");
        p.write("");
        p.write("    function editdates_onchange_field(checkbox, field) {");
        p.write("      checkbox.checked = field.value.length > 0;");
        p.write("    }");
        p.write("");
        p.write("    function editdates_click_OK() {");
        p.write("      var nFields = 0;");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if($('editdateIsSelected_' + i).checked) {");
        p.write("          nFields = i+1;");
        p.write("        }");
        p.write("      }");
        p.write("      values = \"\";");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if(i < nFields) values += (i==0 ? \"\" : \"\\n\") + ($('editdateIsSelected_' + i).checked ? $('editdateField_' + i).value : \"\");");
        p.write("      }");
        p.write("      POPUP_FIELD.value = values;");
        p.write("      editdates_click_Cancel();");
        p.write("    }");
        p.write("");
        p.write("    function editdates_click_Cancel() {");
        p.write("      var IfrRef = document.getElementById('DivShim');");
        p.write("      IfrRef.style.display = 'none';");
        p.write("      shownPopup.style.display =  'none';");
        p.write("    }");
        p.write("");
        p.write("    function editdates_on_load() {");
        p.write("      var i = 0;");
        p.write("      while ($('editdaterow' + i)) {");
        p.write("        var toBeRemoved = $('editdaterow' + i);");
        p.write("        toBeRemoved.parentNode.removeChild(toBeRemoved);");
        p.write("        i += 1;");
        p.write("      }");
        p.write("      var values = POPUP_FIELD.value.split(\"\\n\");");
        p.write("      var templateRow = $('editdaterow');");
        p.write("      var el_idx = $('editdateIdx_');");
        p.write("      var el_checkbox = $('editdateIsSelected_');");
        p.write("      var el_field = $('editdateField_');");
        p.write("      var el_cal = $('cal_date_trigger_');");
        p.write("");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        value = RTrim(values.length > i ? values[i] : \"#NULL\");");
        p.write("        el_checkbox.name = 'isSelected' + i;");
        p.write("        el_field.name = 'field' + i;");
        p.write("        el_idx.id = 'editdateIdx_' + i;");
        p.write("        el_checkbox.id = 'editdateIsSelected_' + i;");
        p.write("        el_field.id = 'editdateField_' + i;");
        p.write("        el_cal.id = 'cal_date_trigger_' + i;");
        p.write("        el_idx.value = i;");
        p.write("        var newRow = templateRow.cloneNode(true);");
        p.write("        newRow.id = 'editdaterow' + i;");
        p.write("        el_checkbox.name = 'isSelected';");
        p.write("        el_field.name = 'field';");
        p.write("        el_idx.id = 'editdateIdx_';");
        p.write("        el_checkbox.id = 'editdateIsSelected_';");
        p.write("        el_field.id = 'editdateField_';");
        p.write("        el_cal.id = 'cal_date_trigger_';");
        p.write("        templateRow.parentNode.appendChild(newRow);");
        p.write("        newRow.style.display = 'block';");
        p.write("        $('editdateIdx_' + i).appendChild(document.createTextNode(i + ':'));");
        p.write("        $('editdateField_' + i).value = value != \"#NULL\" ? value : \"\";");
        p.write("        $('editdateIsSelected_' + i).checked = (value.length > 0) && (value != \"#NULL\");");
        p.write("        Calendar.setup({");
        p.write("          inputField   : \"editdateField_\" + i,");
        p.write("          ifFormat     : \"", DateValue.getCalendarFormat(dateFormatter), "\",");
        p.write("          firstDay     : ", Integer.toString(dateFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
        p.write("          timeFormat   : \"24\",");
        p.write("          button       : \"cal_date_trigger_\" + i,");
        p.write("          align        : \"Tr\",");
        p.write("          singleClick  : true,");
        p.write("          showsTime    : false");
        p.write("        });");
        p.write("      }");
        p.write("      templateRow.style.display = 'none';");
        p.write("    }");
        // multi-valued dateTime        
        p.write("    function editdatetimes_onchange_checkbox(checkbox, field) {");
        p.write("      if(checkbox.checked) {");
        p.write("        field.value = \"\";");
        p.write("      }");
        p.write("    }");
        p.write("");
        p.write("    function editdatetimes_onchange_field(checkbox, field) {");
        p.write("      checkbox.checked = field.value.length > 0;");
        p.write("    }");
        p.write("");
        p.write("    function editdatetimes_click_OK() {");
        p.write("      var nFields = 0;");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if($('editdatetimeIsSelected_' + i).checked) {");
        p.write("          nFields = i+1;");
        p.write("        }");
        p.write("      }");
        p.write("      values = \"\";");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if(i < nFields) values += (i==0 ? \"\" : \"\\n\") + ($('editdatetimeIsSelected_' + i).checked ? $('editdatetimeField_' + i).value : \"\");");
        p.write("      }");
        p.write("      POPUP_FIELD.value = values;");
        p.write("      editdatetimes_click_Cancel();");
        p.write("    }");
        p.write("");
        p.write("    function editdatetimes_click_Cancel() {");
        p.write("      var IfrRef = document.getElementById('DivShim');");
        p.write("      IfrRef.style.display = 'none';");
        p.write("      shownPopup.style.display =  'none';");
        p.write("    }");
        p.write("");
        p.write("    function editdatetimes_on_load() {");
        p.write("      var i = 0;");
        p.write("      while ($('editdatetimerow' + i)) {");
        p.write("        var toBeRemoved = $('editdatetimerow' + i);");
        p.write("        toBeRemoved.parentNode.removeChild(toBeRemoved);");
        p.write("        i += 1;");
        p.write("      }");
        p.write("      var values = POPUP_FIELD.value.split(\"\\n\");");
        p.write("      var templateRow = $('editdatetimerow');");
        p.write("      var el_idx = $('editdatetimeIdx_');");
        p.write("      var el_checkbox = $('editdatetimeIsSelected_');");
        p.write("      var el_field = $('editdatetimeField_');");
        p.write("      var el_cal = $('cal_datetime_trigger_');");
        p.write("");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        value = RTrim(values.length > i ? values[i] : \"#NULL\");");
        p.write("        el_checkbox.name = 'isSelected' + i;");
        p.write("        el_field.name = 'field' + i;");
        p.write("        el_idx.id = 'editdatetimeIdx_' + i;");
        p.write("        el_checkbox.id = 'editdatetimeIsSelected_' + i;");
        p.write("        el_field.id = 'editdatetimeField_' + i;");
        p.write("        el_cal.id = 'cal_datetime_trigger_' + i;");
        p.write("        el_idx.value = i;");
        p.write("        var newRow = templateRow.cloneNode(true);");
        p.write("        newRow.id = 'editdatetimerow' + i;");
        p.write("        el_checkbox.name = 'isSelected';");
        p.write("        el_field.name = 'field';");
        p.write("        el_idx.id = 'editdatetimeIdx_';");
        p.write("        el_checkbox.id = 'editdatetimeIsSelected_';");
        p.write("        el_field.id = 'editdatetimeField_';");
        p.write("        el_cal.id = 'cal_datetime_trigger_';");
        p.write("        templateRow.parentNode.appendChild(newRow);");
        p.write("        newRow.style.display = 'block';");
        p.write("        $('editdatetimeIdx_' + i).appendChild(document.createTextNode(i + ':'));");
        p.write("        $('editdatetimeField_' + i).value = value != \"#NULL\" ? value : \"\";");
        p.write("        $('editdatetimeIsSelected_' + i).checked = (value.length > 0) && (value != \"#NULL\");");
        p.write("        Calendar.setup({");
        p.write("          inputField   : \"editdatetimeField_\" + i,");
        p.write("          ifFormat     : \"", DateValue.getCalendarFormat(dateTimeFormatter), "\",");
        p.write("          firstDay     : ", Integer.toString(dateTimeFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
        p.write("          timeFormat   : \"24\",");
        p.write("          button       : \"cal_datetime_trigger_\" + i,");
        p.write("          align        : \"Tr\",");
        p.write("          singleClick  : true,");
        p.write("          showsTime    : true");
        p.write("        });");
        p.write("      }");
        p.write("      templateRow.style.display = 'none';");
        p.write("    }");
        // multi-valued Booleans
        p.write("    function editbooleans_onchange_checkbox(checkbox, field) {");
        p.write("      if(!checkbox.checked) {");
        p.write("        field.checked = true;");
        p.write("      }");
        p.write("      else {");
        p.write("        field.checked = false;");
        p.write("      }");
        p.write("    }");
        p.write("");
        p.write("    function editbooleans_onchange_field(checkbox, field) {");
        p.write("      checkbox.checked = true;");
        p.write("    }");
        p.write("");
        p.write("    function editbooleans_click_OK() {");
        p.write("      var nFields = 0;");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if($('editbooleanIsSelected_' + i).checked) {");
        p.write("          nFields = i+1;");
        p.write("        }");
        p.write("      }");
        p.write("      values = \"\";");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if(i < nFields) values += (i==0 ? \"\" : \"\\n\") + ($('editbooleanIsSelected_' + i).checked ? $('editbooleanField_' + i).checked : \"\");");
        p.write("      }");
        p.write("      POPUP_FIELD.value = values;");
        p.write("      editbooleans_click_Cancel();");
        p.write("    }");
        p.write("");
        p.write("    function editbooleans_click_Cancel() {");
        p.write("      var IfrRef = document.getElementById('DivShim');");
        p.write("      IfrRef.style.display = 'none';");
        p.write("      shownPopup.style.display =  'none';");
        p.write("    }");
        p.write("");
        p.write("    function editbooleans_on_load() {");
        p.write("      var i = 0;");
        p.write("      while ($('editbooleanrow' + i)) {");
        p.write("        var toBeRemoved = $('editbooleanrow' + i);");
        p.write("        toBeRemoved.parentNode.removeChild(toBeRemoved);");
        p.write("        i += 1;");
        p.write("      }");
        p.write("      var values = POPUP_FIELD.value.split(\"\\n\");");
        p.write("      var templateRow = $('editbooleanrow');");
        p.write("      var el_idx = $('editbooleanIdx_');");
        p.write("      var el_checkbox = $('editbooleanIsSelected_');");
        p.write("      var el_field = $('editbooleanField_');");
        p.write("");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        value = RTrim(values.length > i ? values[i] : \"#NULL\");");
        p.write("        el_checkbox.name = 'isSelected' + i;");
        p.write("        el_field.name = 'field' + i;");
        p.write("        el_idx.id = 'editbooleanIdx_' + i;");
        p.write("        el_checkbox.id = 'editbooleanIsSelected_' + i;");
        p.write("        el_field.id = 'editbooleanField_' + i;");
        p.write("        el_idx.value = i;");
        p.write("        var newRow = templateRow.cloneNode(true);");
        p.write("        newRow.id = 'editbooleanrow' + i;");
        p.write("        el_checkbox.name = 'isSelected';");
        p.write("        el_field.name = 'field';");
        p.write("        el_idx.id = 'editbooleanIdx_';");
        p.write("        el_checkbox.id = 'editbooleanIsSelected_';");
        p.write("        el_field.id = 'editbooleanField_';");
        p.write("        templateRow.parentNode.appendChild(newRow);");
        p.write("        newRow.style.display = 'block';");
        p.write("        $('editbooleanIdx_' + i).appendChild(document.createTextNode(i + ':'));");
        p.write("        $('editbooleanField_' + i).checked = value != \"#NULL\" ? value == \"true\" : \"\";");
        p.write("        $('editbooleanIsSelected_' + i).checked = (value.length > 0) && (value != \"#NULL\");");
        p.write("      }");
        p.write("      templateRow.style.display = 'none';");
        p.write("    }");
        // multi-valued Codes
        p.write("    function editcodes_onchange_checkbox(checkbox, field) {");
        p.write("      if(checkbox.checked) {");
        p.write("        field.value = POPUP_OPTIONS[1];");
        p.write("      }");
        p.write("      else {");
        p.write("        field.value = POPUP_OPTIONS[0];");
        p.write("      }");
        p.write("    }");
        p.write("");
        p.write("    function editcodes_onchange_field(checkbox, field) {");
        p.write("      checkbox.checked = field.value.length > 0;");
        p.write("    }");
        p.write("");
        p.write("    function editcodes_click_OK() {");
        p.write("      var nFields = 0;");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if($('editcodeIsSelected_' + i).checked) {");
        p.write("          nFields = i+1;");
        p.write("        }");
        p.write("      }");
        p.write("      values = \"\";");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        if(i < nFields) values += (i==0 ? \"\" : \"\\n\") + ($('editcodeIsSelected_' + i).checked ? $('editcodeField_' + i).value : \"\");");
        p.write("      }");
        p.write("      POPUP_FIELD.value = values;");
        p.write("      editcodes_click_Cancel();");
        p.write("    }");
        p.write("");
        p.write("    function editcodes_click_Cancel() {");
        p.write("      var IfrRef = document.getElementById('DivShim');");
        p.write("      IfrRef.style.display = 'none';");
        p.write("      shownPopup.style.display =  'none';");
        p.write("    }");
        p.write("");
        p.write("    function editcodes_on_load() {");
        p.write("      var i = 0;");
        p.write("      while ($('editcoderow' + i)) {");
        p.write("        var toBeRemoved = $('editcoderow' + i);");
        p.write("        toBeRemoved.parentNode.removeChild(toBeRemoved);");
        p.write("        i += 1;");
        p.write("      }");
        p.write("      var values = POPUP_FIELD.value.split(\"\\n\");");
        p.write("      var templateRow = $('editcoderow');");
        p.write("      var el_idx = $('editcodeIdx_');");
        p.write("      var el_checkbox = $('editcodeIsSelected_');");
        p.write("      var el_field = $('editcodeField_');");
        p.write("");
        p.write("      for (i=0;i<multiValuedHigh;i++) {");
        p.write("        value = RTrim(values.length > i ? values[i] : \"#NULL\");");
        p.write("        el_checkbox.name = 'isSelected' + i;");
        p.write("        el_field.name = 'field' + i;");
        p.write("        el_idx.id = 'editcodeIdx_' + i;");
        p.write("        el_checkbox.id = 'editcodeIsSelected_' + i;");
        p.write("        el_field.id = 'editcodeField_' + i;");
        p.write("        el_idx.value = i;");
        p.write("        var newRow = templateRow.cloneNode(true);");
        p.write("        newRow.id = 'editcoderow' + i;");
        p.write("        el_checkbox.name = 'isSelected';");
        p.write("        el_field.name = 'field';");
        p.write("        el_idx.id = 'editcodeIdx_';");
        p.write("        el_checkbox.id = 'editcodeIsSelected_';");
        p.write("        el_field.id = 'editcodeField_';");
        p.write("        templateRow.parentNode.appendChild(newRow);");
        p.write("        newRow.style.display = 'block';");
        p.write("        for(j = 0; j < POPUP_OPTIONS.length; j++) {");
        p.write("          $('editcodeField_' + i).options[j] = new Option(POPUP_OPTIONS[j], POPUP_OPTIONS[j]);");
        p.write("        }");
        p.write("        $('editcodeIdx_' + i).appendChild(document.createTextNode(i + ':'));");
        p.write("        $('editcodeIsSelected_' + i).checked =  (value.length > 0) && (value != \"#NULL\");");
        p.write("        $('editcodeField_' + i).value = value != \"#NULL\" ? value : \"\";");
        p.write("      }");
        p.write("      templateRow.style.display = 'none';");
        p.write("    }");
        
        // Does page contains charts?
        if(!editMode) {
            if(currentChartId > 0) {
                p.write("pageHasCharts = true;");             
            }
            else {
                p.write("pageHasCharts = false;");            
            }
        }

        // window.onresize
        p.write("window.onresize=function() {  // is called from guicontrol.js showPanel()");
        for(int i = 0; i < currentChartId; i++) {
            p.write("    displayChart" + i, "();");
        }
        p.write("}");
        
        // dateSelected
        p.write("function dateSelected(calendar, date) {");
        p.write("  if (calendar.dateClicked) {");
        p.write("  };");
        p.write("}");
        List calendarIds = (List)p.getProperty(HtmlPage.PROPERTY_CALENDAR_IDS);
        for(Iterator i = calendarIds.iterator(); i.hasNext(); ) {
            String calendarId = (String)i.next();
            p.write("Calendar.setup({");
            p.write("  flat         : \"", calendarId, "\",");
            p.write("  onSelect     : dateSelected,");
            p.write("  daFormat     : \"", DateValue.getCalendarFormat(dateFormatter), "\",");
            p.write("  align        : \"Tc\",");
            p.write("  firstDay     : ", Integer.toString(dateFormatter.getCalendar().getFirstDayOfWeek()-1), ",");
            p.write("  singleClick  : true,");
            p.write("  showsTime    : false,");
            p.write("  weekNumbers  : true");
            p.write("});");
        }
        // Layout
        p.write("initLayout = function(){");
        p.write("    return {");
        p.write("      init : function(){");
        // No layout in edit mode
        if(!editMode) {
            // Breadcrums / Select parent actions
            if(noLayoutManager) {
                p.write("      var breadcrum = \"\";");                
            }
            else {
                p.write("      var breadcrum = \"&nbsp;&nbsp;&nbsp;&nbsp;\";");
            }
            Action[] selectParentActions = view.getSelectParentAction();
            for(int i = 0; i < selectParentActions .length; i++) {
                Action selectParentAction = selectParentActions[i];
                if(selectParentAction != null) {
                    String breadcrum = new StringBuilder(
                        i > 0 ? " > " : "" 
                    ).append(
                         "<a href=\"#\""
                    ).append(
                        p.getOnClick("javascript:this.href=", p.getEvalHRef(selectParentAction), ";")
                    ).append(                        
                        ">"
                    ).append(
                        selectParentAction.getTitle()
                    ).append(
                        "</a>"
                    ).toString();
                    p.write("      breadcrum = breadcrum + \"", breadcrum.toString().replaceAll("\"", "\\\\\""), "\";");
                }
            }
            if(noLayoutManager) {
                p.write("      $('inspBreadcrum').innerHTML = breadcrum;");                
            }
            else {
                p.write("        layout = new YAHOO.ext.BorderLayout(document.body, {");
                p.write("                     hideOnLayout: true,");
                p.write("                     north: {");
                p.write("                        split:false,");
                p.write("                        initialSize: ", Integer.toString(this.panelSizeNorth), ",");
                p.write("                        titlebar: true,");
                p.write("                        collapsible: true");
                p.write("                     },");
                p.write("                     west: {");
                p.write("                        split:false,");
                p.write("                        initialSize: ", Integer.toString(this.panelSizeWest), ",");
                p.write("                        titlebar: true,");
                p.write("                        collapsible: true");
                p.write("                     },");
                p.write("                     center: {");
                p.write("                        titlebar: true,");
                p.write("                        autoScroll: true");
                p.write("                     }");
                p.write("        });");
                p.write("      layout.beginUpdate();");
                p.write("      layout.add('north',  new YAHOO.ext.ContentPanel('header',     {title: '', fitToFrame:true,  closable:false}));");
                p.write("      layout.add('west',   new YAHOO.ext.ContentPanel('navigation', {title: '', fitToFrame:false,  closable:false}));");
                p.write("      layout.add('center', new YAHOO.ext.ContentPanel('content',    {title: breadcrum, fitToFrame:true, closable:false}));");
                p.write("      $('header').parentNode.style.overflow = 'visible'; // required to show fly-out menues [IE bug]");
                p.write("      $('navigation').parentNode.style.overflow = 'visible'; // required to show fly-out menues [IE bug]");
                if(view.getPanelState("north") == PanelControl.PANEL_STATE_HIDE) {
                    p.write("      layout.getRegion('north').collapse(false);");
                }
                if(view.getPanelState("west") == PanelControl.PANEL_STATE_HIDE) {
                    p.write("      layout.getRegion('west').collapse(false);");
                }
                p.write("      layout.getRegion('north').addListener('expanded', function(){new Ajax.Request(", p.getEvalHRef(view.getSetPanelStateAction("north", PanelControl.PANEL_STATE_SHOW)), ", {asynchronous:true});});");
                p.write("      layout.getRegion('north').addListener('collapsed', function(){new Ajax.Request(", p.getEvalHRef(view.getSetPanelStateAction("north", PanelControl.PANEL_STATE_HIDE)), ", {asynchronous:true});});");
                p.write("      layout.getRegion('west').addListener('expanded', function(){new Ajax.Request(", p.getEvalHRef(view.getSetPanelStateAction("west", PanelControl.PANEL_STATE_SHOW)), ", {asynchronous:true});});");
                p.write("      layout.getRegion('west').addListener('collapsed', function(){new Ajax.Request(", p.getEvalHRef(view.getSetPanelStateAction("west", PanelControl.PANEL_STATE_HIDE)), ", {asynchronous:true});});");
                p.write("");
            }
        }
        // No grid panels in edit mode
        if(!editMode) {
            ShowObjectView showView = (ShowObjectView)view;
            // Prepare grid panels
            int nReferencePanes = showView.getReferencePane().length;
            for(int i = 0; i < nReferencePanes; i++) {
                ReferencePane referencePane = showView.getReferencePane()[i];                
                int paneIndex = referencePane.getReferencePaneControl().getPaneIndex();
                String paneId = Integer.toString(paneIndex);
                boolean isGroupTabActive = false;
                int lastGroupTabIndex = 0;
                int nGridControl = referencePane.getReferencePaneControl().getGridControl().length;
                for(int j = 0; j < nGridControl; j++) {
                    ReferencePaneControl gridTab = referencePane.getReferencePaneControl();
                    Action selectReferenceTabAction = gridTab.getSelectReferenceAction()[j];     
                    int tabIndex = 100*paneIndex + j;
                    String tabId = Integer.toString(tabIndex);
                    // Tab grouping. Generate hide/show tabs for each group of
                    // tabs having a label starting with >>
                    String tabTitle = selectReferenceTabAction.getTitle();
                    boolean isGroupTab = tabTitle.startsWith(WebKeys.TAB_GROUPING_CHARACTER);
                    if(isGroupTab) {
                        tabTitle = tabTitle.substring(1);
                    }
                    // Prolog hidden tabs
                    if(!isGroupTabActive && isGroupTab) {
                        isGroupTabActive = true;
                        lastGroupTabIndex = j;
                    }
                    // Add tab
                    // Get content for selected grid
                    if(j == referencePane.getSelectedReference()) {
                        p.write("      new Ajax.Updater('gridContent", paneId, "', ", p.getEvalHRef(selectReferenceTabAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){try{makeZebraTable('gridTable", tabId, "',1);}catch(e){};}});");
                    }
                    // Epilog hidden tabs. Special treatment if last tab of grid is a group tab 
                    if(isGroupTabActive && (!isGroupTab || (j == nGridControl-1))) {
                        isGroupTabActive = false;
                    }                    
                }
            }
            p.write("");
            if(!noLayoutManager) {
                p.write("      layout.endUpdate();");
            }
        }
        p.write("    }");
        p.write("  }");
        p.write("}();");
        p.write("YAHOO.ext.SSL_SECURE_URL = '", p.getHttpServletRequest().getContextPath(), "/blank.html';");
        p.write("YAHOO.ext.EventManager.ieDeferSrc = YAHOO.ext.SSL_SECURE_URL;");
        p.write("YAHOO.ext.UpdateManager.defaults.indicatorText = \"<div class='loading-indicator'>&nbsp;</div>\";");
        p.write("YAHOO.ext.UpdateManager.defaults.timeout = 60;");        
        p.write("YAHOO.ext.BasicDialog.prototype.syncHeightBeforeShow = true;");
        p.write("YAHOO.ext.EventManager.onDocumentReady(initLayout.init, initLayout, true);");
        // Declare variables for layout and grid panels. This way they can be accessed controls
        if(!editMode) {
            p.write("var layout = null;");
            ShowObjectView showView = (ShowObjectView)view;
            int nReferencePanes = showView.getReferencePane().length;
            for(int i = 0; i < nReferencePanes; i++) {
                ReferencePane referencePane = showView.getReferencePane()[i];                
                int referencePaneIndex = referencePane.getReferencePaneControl().getPaneIndex();
                String referencePaneId = Integer.toString(referencePaneIndex);
                p.write("var gridPanel", referencePaneId, " = null;");
            }
        }
        p.write("");
        p.write("function initPage() {");
        if(currentChartId > 0) {
            p.write("  window.onresize();");
        }
        if(view.getMacro() != null) {
            Object[] macro = view.getMacro();
            Number actionType = (Number)macro[0];
            String actionName = (String)macro[1];
            if(actionType.intValue() == Action.MACRO_TYPE_JAVASCRIPT) {
                actionName = actionName.replaceAll(View.REQUEST_ID_TEMPLATE, view.getRequestId());
                p.write("  ", actionName);
            }
            view.setMacro(null);
        }
        p.write("}");
        p.write("</script>");
        // Generate div for each popup image
        Map popupImages= (Map)p.getProperty(HtmlPage.PROPERTY_POPUP_IMAGES);
        for(Iterator i = popupImages.keySet().iterator(); i.hasNext(); ) {
            String imageId = (String)i.next();
            String imageSrc = (String)popupImages.get(imageId);
            p.write("  <div class=\"divImgPopUp\" id=\"divImgPopUp", imageId, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\" ondblclick=\"javascript:this.style.display='none'\" >");
            p.write("  ", p.getImg("class=\"popUpImg\" id=\"popUpImg", imageId, "\" src=\"", imageSrc, "\" alt=\"\""), "</div>");
        }

        AppLog.detail("< paint");        
    }

    //-----------------------------------------------------------------------
    public void setPanelSizeNorth(
        int newValue
    ) {
        this.panelSizeNorth = newValue;
        
    }
    
    //-----------------------------------------------------------------------
    public int getPanelSizeNorth(
    ) {
        return this.panelSizeNorth;
    }
    
    //-----------------------------------------------------------------------
    public void setPanelSizeWest(
        int newValue
    ) {
        this.panelSizeWest = newValue;
        
    }
    
    //-----------------------------------------------------------------------
    public int getPanelSizeWest(
    ) {
        return this.panelSizeWest;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -294211239994971237L;
    
    private int panelSizeNorth = 125;
    private int panelSizeWest = 230;
    
}

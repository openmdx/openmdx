/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: PageEpilogControl 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2014, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.component.EditObjectView;
import org.openmdx.portal.servlet.component.Grid;
import org.openmdx.portal.servlet.component.ReferencePane;
import org.openmdx.portal.servlet.component.ShowObjectView;
import org.openmdx.portal.servlet.component.View;

/**
 * PageEpilogControl
 *
 */
public class PageEpilogControl extends Control implements Serializable {
  
    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     */
    public PageEpilogControl(
        String id,
        String locale,
        int localeAsIndex
    ) {
        super(
            id,
            locale,
            localeAsIndex
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.control.Control#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
     */
    @Override
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing
    ) throws ServiceException {
    	this.paint(
    		p, 
    		frame, 
    		forEditing, 
    		true // editPopups
    	);
    }
    
    /**
     * Paint content.
     * 
     * @param p
     * @param frame
     * @param forEditing
     * @param globals
     * @throws ServiceException
     */
    public void paint(
        ViewPort p,
        String frame,
        boolean forEditing,
        boolean globals
    ) throws ServiceException {
    	SysLog.detail("> paint");        
        View view = p.getView();
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
        // Init scripts
        p.write("<script language=\"javascript\" type=\"text/javascript\">");
        // Popup's for multi-valued attributes
        if(globals) {
	        // multi-valued Strings
	        p.write("    var multiValuedHigh = 1; // 0..multiValuedHigh-1");
	        p.write("");
	        p.write("    var popup_", EditInspectorControl.EDIT_STRINGS, " = null;");
	        p.write("    var popup_", EditInspectorControl.EDIT_NUMBERS, " = null;");
	        p.write("    var popup_", EditInspectorControl.EDIT_DATES, " = null;");
	        p.write("    var popup_", EditInspectorControl.EDIT_DATETIMES, " = null;");
	        p.write("    var popup_", EditInspectorControl.EDIT_BOOLEANS, " = null;");
	        p.write("    var popup_", EditInspectorControl.EDIT_CODES, " = null;");
	        p.write("");        
	        p.write("    function ", EditInspectorControl.EDIT_STRINGS, "_onchange_checkbox(checkbox, field) {");
	        p.write("      if(!checkbox.checked) {");
	        p.write("        field.value = \"*\";");
	        p.write("      }");
	        p.write("      else {");
	        p.write("        field.value = \"\";");
	        p.write("      }");
	        p.write("    }");
	        p.write("");
	        p.write("    ", EditInspectorControl.EDIT_STRINGS, "_maxLength = 2147483647;");
	        p.write("    function ", EditInspectorControl.EDIT_STRINGS, "_onchange_field(checkbox, field) {");
	        p.write("      if(field.value.length > 0) {");
	        p.write("        checkbox.checked = true;");
	        p.write("        if(field.value.length > ", EditInspectorControl.EDIT_STRINGS, "_maxLength) {");
	        p.write("          field.value = field.value.substr(0, ", EditInspectorControl.EDIT_STRINGS, "_maxLength)");
	        p.write("        }");
	        p.write("      }");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_STRINGS, "_click_OK() {");
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
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_STRINGS, "_on_load() {");
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
	        p.write("    function ", EditInspectorControl.EDIT_NUMBERS, "_onchange_checkbox(checkbox, field) {");
	        p.write("      if(!checkbox.checked) {");
	        p.write("        field.value = \"0.00\";");
	        p.write("      }");
	        p.write("      else {");
	        p.write("        field.value = \"\";");
	        p.write("      }");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_NUMBERS, "_onchange_field(checkbox, field) {");
	        p.write("      checkbox.checked = field.value.length > 0;");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_NUMBERS, "_click_OK() {");
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
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_NUMBERS, "_on_load() {");
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
	        p.write("    function ", EditInspectorControl.EDIT_DATES, "_onchange_checkbox(checkbox, field) {");
	        p.write("      if(checkbox.checked) {");
	        p.write("        field.value = \"\";");
	        p.write("      }");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_DATES, "_onchange_field(checkbox, field) {");
	        p.write("      checkbox.checked = field.value.length > 0;");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_DATES, "_click_OK() {");
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
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_DATES, "_on_load() {");
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
	        p.write("    function ", EditInspectorControl.EDIT_DATETIMES, "_onchange_checkbox(checkbox, field) {");
	        p.write("      if(checkbox.checked) {");
	        p.write("        field.value = \"\";");
	        p.write("      }");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_DATETIMES, "_onchange_field(checkbox, field) {");
	        p.write("      checkbox.checked = field.value.length > 0;");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_DATETIMES, "_click_OK() {");
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
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_DATETIMES, "_on_load() {");
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
	        p.write("    function ", EditInspectorControl.EDIT_BOOLEANS, "_onchange_checkbox(checkbox, field) {");
	        p.write("      if(!checkbox.checked) {");
	        p.write("        field.checked = true;");
	        p.write("      }");
	        p.write("      else {");
	        p.write("        field.checked = false;");
	        p.write("      }");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_BOOLEANS, "_onchange_field(checkbox, field) {");
	        p.write("      checkbox.checked = true;");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_BOOLEANS, "_click_OK() {");
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
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_BOOLEANS, "_on_load() {");
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
	        p.write("    function ", EditInspectorControl.EDIT_CODES, "_onchange_checkbox(checkbox, field) {");
	        p.write("      if(checkbox.checked) {");
	        p.write("        field.value = POPUP_OPTIONS[1];");
	        p.write("      }");
	        p.write("      else {");
	        p.write("        field.value = POPUP_OPTIONS[0];");
	        p.write("      }");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_CODES, "_onchange_field(checkbox, field) {");
	        p.write("      checkbox.checked = field.value.length > 0;");
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_CODES, "_click_OK() {");
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
	        p.write("    }");
	        p.write("");
	        p.write("    function ", EditInspectorControl.EDIT_CODES, "_on_load() {");
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
	        // dateSelected
	        p.write("    function dateSelected(calendar, date) {");
	        p.write("      if (calendar.dateClicked) {};");
	        p.write("    }");
	        @SuppressWarnings({ "unchecked", "rawtypes" })
			List<String> calendarIds = (List)p.getProperty(ViewPort.PROPERTY_CALENDAR_IDS);
	        if(calendarIds != null) {
	            for( String calendarId: calendarIds) {
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
	        }   
        }
        // No grid panels in edit mode
        if(!editMode && (view instanceof ShowObjectView)) {
            ShowObjectView showView = (ShowObjectView)view;
            // Prepare grid panels
            List<ReferencePane> referencePanes = showView.getChildren(ReferencePane.class);
            int nReferencePanes = referencePanes.size();
            for(int i = 0; i < nReferencePanes; i++) {
                ReferencePane referencePane = referencePanes.get(i);
                int paneIndex = referencePane.getPaneIndex();
                boolean isGroupTabActive = false;
                int nGridControl = referencePane.getChildren(Grid.class).size();
                for(int j = 0; j < nGridControl; j++) {
                    Action selectReferenceTabAction = referencePane.getSelectReferenceAction()[j];
                    UiGridControl gridControl = referencePane.getControl().getChildren(UiGridControl.class).get(j);
	            	boolean isRevokeShow = app.getPortalExtension().hasPermission(
	            		gridControl.getQualifiedReferenceName(), 
	            		showView.getObject(), 
	            		app, 
	            		WebKeys.PERMISSION_REVOKE_SHOW
	            	);
	            	if(!isRevokeShow) {
                        String gridContentId = view.getContainerElementId() == null 
                        	? "G_" + Integer.toString(paneIndex) 
                        	: view.getContainerElementId();
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
	                    }
	                    // Add tab
	                    // Get content for selected grid
	                    if(j == referencePane.getSelectedReference()) {
	                        p.write("    jQuery.ajax({type: 'get', url: ", p.getEvalHRef(selectReferenceTabAction), ", dataType: 'html', success: function(data){$('", gridContentId, "').innerHTML=data;evalScripts(data);}});");
	                    }
	                    // Epilog hidden tabs. Special treatment if last tab of grid is a group tab 
	                    if(isGroupTabActive && (!isGroupTab || (j == nGridControl-1))) {
	                        isGroupTabActive = false;
	                    }
	            	}
                }
            }
            p.write("");
            for(int i = 0; i < nReferencePanes; i++) {
                ReferencePane referencePane = referencePanes.get(i);                
                int referencePaneIndex = referencePane.getPaneIndex();
                String referencePaneId = Integer.toString(referencePaneIndex);
                p.write("    var gridPanel", referencePaneId, " = null;");
            }
        }
        p.write("");
        if(globals) {
	        p.write("    function initPage() {");
	        if(view.getMacro() != null) {
	            Object[] macro = view.getMacro();
	            Number actionType = (Number)macro[0];
	            String actionName = (String)macro[1];
	            if(actionType.intValue() == Action.MACRO_TYPE_JAVASCRIPT) {
	                actionName = actionName.replaceAll("$" + View.REQUEST_ID_TEMPLATE, view.getRequestId());
	                actionName = actionName.replaceAll(View.REQUEST_ID_TEMPLATE, view.getRequestId());
	                if(view instanceof ShowObjectView) {
	                	actionName = actionName.replaceAll(View.CURRENT_OBJECT_XRI_TEMPLATE, ((ShowObjectView)view).getObject().refMofId());
	                }
	                p.write("      ", actionName);
	            }
	            view.setMacro(null);
	        }
	        p.write("      jQuery(window).scroll(function(){");
	        p.write("        var windowHeight = jQuery(window).height();");
	        p.write("        if (windowHeight >= 480) {");
	        p.write("          var scrollTop = jQuery(window).scrollTop();");
	        p.write("          if (scrollTop > 150) {");
	        p.write("            jQuery('#nav').addClass('OperationMenuFloat');");
	        p.write("            if(jQuery(window).height() > jQuery('#OperationDialog').height() + 100) {");
	        p.write("              jQuery('#OperationDialog').addClass('modal-content OperationDialogFloat');");	        
	        p.write("              jQuery('#OperationDialog').css({'width':'80%'});");
	        p.write("            }");
	        p.write("            if(jQuery(window).height() > jQuery('#UserDialog').height() + 100) {");
	        p.write("              jQuery('#UserDialog').addClass('modal-content OperationDialogFloat');");
	        p.write("              jQuery('#UserDialog').css({'width':'80%'});");
	        p.write("            }");
	        p.write("          } else {");
	        p.write("            jQuery('#nav').removeClass('OperationMenuFloat');");
	        p.write("            jQuery('#OperationDialog').removeClass('modal-content OperationDialogFloat');");
	        p.write("            jQuery('#OperationDialog').attr('style','');");
	        p.write("            jQuery('#UserDialog').removeClass('modal-content OperationDialogFloat');");
	        p.write("            jQuery('#UserDialog').attr('style','');");
	        p.write("          }");
	        p.write("        }");
	        p.write("      });");
	        p.write("    }");
        }
        p.write("</script>");
        // Generate div for each popup image
        @SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String,String> popupImages= (Map)p.getProperty(ViewPort.PROPERTY_POPUP_IMAGES);
        if(popupImages != null) {
            for(String imageId: popupImages.keySet()) {
                String imageSrc = popupImages.get(imageId);
                p.write("  <div class=\"", CssClass.divImgPopUp.toString(), "\" id=\"divImgPopUp", imageId, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\" ondblclick=\"javascript:this.style.display='none'\" >");
                p.write("  ", p.getImg("class=\"", CssClass.popUpImg.toString(), "\" id=\"popUpImg", imageId, "\" src=\"", imageSrc, "\" alt=\"\""), "</div>");
            }
        }
        SysLog.detail("< paint");        
    }

    /**
     * Set size of north panel.
     * 
     * @param newValue
     */
    public void setPanelSizeNorth(
        int newValue
    ) {
        this.panelSizeNorth = newValue;
        
    }
    
    /**
     * Get size of north panel.
     * 
     * @return
     */
    public int getPanelSizeNorth(
    ) {
        return this.panelSizeNorth;
    }
    
    /**
     * Set size of west panel.
     * 
     * @param newValue
     */
    public void setPanelSizeWest(
        int newValue
    ) {
        this.panelSizeWest = newValue;
        
    }
    
    /**
     * Get size of west panel.
     * 
     * @return
     */
    public int getPanelSizeWest(
    ) {
        return this.panelSizeWest;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.Control#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

	//-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = -294211239994971237L;
    
    private int panelSizeNorth = 125;
    private int panelSizeWest = 230;
    
}

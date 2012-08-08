/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: EditObjectControl.java,v 1.30 2007/08/10 12:41:52 wfro Exp $
 * Description: EditObjectControl
 * Revision:    $Revision: 1.30 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/08/10 12:41:52 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.attribute.DateValue;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.EditObjectView;

public class EditObjectControl
    extends ContainerControl
    implements Serializable {
  
    //-------------------------------------------------------------------------
    public EditObjectControl(
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

    //-------------------------------------------------------------------------
    public void paint(
        HtmlPage p, 
        String frame,
        boolean forEditing    
    ) throws ServiceException {
        ApplicationContext application = p.getApplicationContext();
        Texts_1_0 texts = application.getTexts();
        if(forEditing) {
            EditObjectView editView =  (EditObjectView)p.getView();
            SimpleDateFormat dateFormatter = DateValue.getLocalizedDateFormatter(
                null, 
                true,
                application 
            );
            SimpleDateFormat dateTimeFormatter = DateValue.getLocalizedDateTimeFormatter(
                null, 
                true, 
                application
            );
            
            // Popup multi-valued strings
            p.write("<div class=\"popUp\" id=\"", POPUP_EDIT_STRINGS, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\">");
            p.write("  <script language=\"javascript\" type=\"text/javascript\">");
            p.write("");
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
            p.write("");
            p.write("  </script>");
            p.write("  <form name=\"editstrings\" method=\"post\" action=\"\">");
            p.write("    <table class=\"popUpTable\">");
            p.write("      <!-- template row -->");
            p.write("      <tr id=\"editstringrow\" style=\"display:block;\">");
            p.write("        <td class=\"fieldindex\" id=\"editstringIdx_\"></td>");
            p.write("        <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editstringIsSelected_\" onmouseup=\"editstrings_onchange_checkbox(this, $('editstringField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
            p.write("        <td class=\"fieldvalue\"><input type=\"text\" class=\"valueL\" name=\"field\" id=\"editstringField_\" onkeyup=\"editstrings_onchange_field($('editstringIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
            p.write("      </tr>");
            p.write("    </table>");
            p.write("    <table class=\"popUpTable\">");
            p.write("      <tr>");
            p.write("        <td></td>");
            p.write("        <td></td>");
            p.write("        <td align=\"right\">");
            p.write("          <span class=\"noIcon\" onclick=\"editstrings_click_OK();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getOkTitle(), "</span>");
            p.write("          <span class=\"noIcon\" onclick=\"editstrings_click_Cancel();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getCancelTitle(), "</span>");
            p.write("        </td>");
            p.write("      </tr>");
            p.write("    </table>");
            p.write("  </form>");
            p.write("</div>");

            // Popup multi-valued numbers
            p.write("<div class=\"popUp\" id=\"", POPUP_EDIT_NUMBERS, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\">");
            p.write("  <script language=\"javascript\" type=\"text/javascript\">");
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
            p.write("  </script>");
            p.write("  <form name=\"editnumbers\" method=\"post\" action=\"\">");
            p.write("    <table class=\"popUpTable\">");
            p.write("        <!-- template row -->");
            p.write("        <tr id=\"editnumberrow\" style=\"display:block;\">");
            p.write("          <td class=\"fieldindex\" id=\"editnumberIdx_\"></td>");
            p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editnumberIsSelected_\" onmouseup=\"editnumbers_onchange_checkbox(this, $('editnumberField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
            p.write("          <td class=\"fieldvalue\"><input type=\"text\" class=\"valueR\" name=\"field\" id=\"editnumberField_\" onkeyup=\"editnumbers_onchange_field($('editnumberIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
            p.write("        </tr>");
            p.write("    </table>");
            p.write("    <table class=\"popUpTable\">");
            p.write("        <tr>");
            p.write("          <td></td>");
            p.write("          <td></td>");
            p.write("          <td align=\"right\">");
            p.write("             <span class=\"noIcon\" onclick=\"editnumbers_click_OK();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getOkTitle(), "</span>");
            p.write("             <span class=\"noIcon\" onclick=\"editnumbers_click_Cancel();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getCancelTitle(), "</span>");
            p.write("          </td>");
            p.write("        </tr>");
            p.write("    </table>");
            p.write("  </form>");
            p.write("</div>");

            // Popup multi-valued dates
            p.write("<div class=\"popUp\" id=\"", POPUP_EDIT_DATES, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\">");
            p.write("  <script language=\"javascript\" type=\"text/javascript\">");
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
            p.write("  </script>");
            p.write("");
            p.write("  <form name=\"editdates\" method=\"post\" action=\"\">");
            p.write("    <table class=\"popUpTable\">");
            p.write("        <!-- template row -->");
            p.write("        <tr id=\"editdaterow\" style=\"display:block;\">");
            p.write("          <td class=\"fieldindex\" id=\"editdateIdx_\"></td>");
            p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editdateIsSelected_\" onmouseup=\"editdates_onchange_checkbox(this, $('editdateField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
            p.write("          <td class=\"fieldvalue\"><input type=\"text\" class=\"valueR\" name=\"field\" id=\"editdateField_\" title=\"wfro: locale dependent\" onkeyup=\"editdates_onchange_field($('editdateIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
            p.write("          <td class=\"addon\"><a><img class=\"popUpButton\" id=\"cal_date_trigger_\" border=\"0\" alt=\"\" src=\"", p.getResourcePath("images/"), "cal.gif\" /></a></td>");
            p.write("        </tr>");
            p.write("    </table>");
            p.write("    <table class=\"popUpTable\">");
            p.write("      <tr>");
            p.write("        <td></td>");
            p.write("        <td></td>");
            p.write("        <td align=\"right\">");
            p.write("          <span class=\"noIcon\" onclick=\"editdates_click_OK();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getOkTitle(), "</span>");
            p.write("          <span class=\"noIcon\" onclick=\"editdates_click_Cancel();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getCancelTitle(), "</span>");
            p.write("        </td>");
            p.write("      </tr>");
            p.write("    </table>");
            p.write("  </form>");
            p.write("</div>");
                        
            // Popup multi-valued datetime
            p.write("<div class=\"popUp\" id=\"", POPUP_EDIT_DATETIMES, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\">");
            p.write("  <script language=\"javascript\" type=\"text/javascript\">");
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
            p.write("  </script>");
            p.write("");
            p.write("  <form name=\"editdatetimes\" method=\"post\" action=\"\">");
            p.write("    <table class=\"popUpTable\">");
            p.write("        <!-- template row -->");
            p.write("        <tr id=\"editdatetimerow\" style=\"display:block;\">");
            p.write("          <td class=\"fieldindex\" id=\"editdatetimeIdx_\"></td>");
            p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editdatetimeIsSelected_\" onmouseup=\"editdatetimes_onchange_checkbox(this, $('editdatetimeField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
            p.write("          <td class=\"fieldvalue\"><input type=\"text\" class=\"valueR\" name=\"field\" id=\"editdatetimeField_\" title=\"wfro: locale dependent\" onkeyup=\"editdatetimes_onchange_field($('editdatetimeIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
            p.write("          <td class=\"addon\"><a><img class=\"popUpButton\" id=\"cal_datetime_trigger_\" border=\"0\" alt=\"\" src=\"", p.getResourcePath("images/"), "cal.gif\" /></a></td>");
            p.write("        </tr>");
            p.write("    </table>");
            p.write("    <table class=\"popUpTable\">");
            p.write("      <tr>");
            p.write("        <td></td>");
            p.write("        <td></td>");
            p.write("        <td align=\"right\">");
            p.write("          <span class=\"noIcon\" onclick=\"editdatetimes_click_OK();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getOkTitle(), "</span>");
            p.write("          <span class=\"noIcon\" onclick=\"editdatetimes_click_Cancel();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getCancelTitle(), "</span>");
            p.write("        </td>");
            p.write("      </tr>");
            p.write("    </table>");
            p.write("  </form>");
            p.write("</div>");
                        
            // Poup multi-valued booleans
            p.write("<div class=\"popUp\" id=\"", POPUP_EDIT_BOOLEANS, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\">");
            p.write("  <script language=\"javascript\" type=\"text/javascript\">");
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
            p.write("  </script>");
            p.write("");
            p.write("  <form name=\"editbooleans\" method=\"post\" action=\"\">");
            p.write("    <table class=\"popUpTable\">");
            p.write("        <!-- template row -->");
            p.write("        <tr id=\"editbooleanrow\" style=\"display:block;\">");
            p.write("          <td class=\"fieldindex\" id=\"editbooleanIdx_\"></td>");
            p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editbooleanIsSelected_\" onmouseup=\"editbooleans_onchange_checkbox(this, $('editbooleanField_' + this.id.split('_')[1]));\"></td>");
            p.write("          <td class=\"fieldvalue\"><input type=\"checkbox\" class=\"valueL\" name=\"field\" id=\"editbooleanField_\" onmouseup=\"editbooleans_onchange_field($('editbooleanIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
            p.write("        </tr>");
            p.write("    </table>");
            p.write("    <table class=\"popUpTable\">");
            p.write("        <tr>");
            p.write("            <td></td>");
            p.write("            <td></td>");
            p.write("            <td align=\"right\">");
            p.write("                <span class=\"noIcon\" onclick=\"editbooleans_click_OK();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getOkTitle(), "</span>");
            p.write("                <span class=\"noIcon\" onclick=\"editbooleans_click_Cancel();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getCancelTitle(), "</span>");
            p.write("            </td>");
            p.write("        </tr>");
            p.write("    </table>");
            p.write("  </form>");
            p.write("</div>");
            
            // Popup multi-valued codes
            p.write("<div class=\"popUp\" id=\"", POPUP_EDIT_CODES, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\">");
            p.write("  <script language=\"javascript\" type=\"text/javascript\">");
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
            p.write("  </script>");
            p.write("");
            p.write("  <form name=\"editcodes\" method=\"post\" action=\"\">");
            p.write("    <table class=\"popUpTable\">");
            p.write("        <!-- template row -->");
            p.write("        <tr id=\"editcoderow\" style=\"display:none;\">");
            p.write("          <td class=\"fieldindex\" id=\"editcodeIdx_\"></td>");
            p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editcodeIsSelected_\" onmouseup=\"editcodes_onchange_checkbox(this, $('editcodeField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
            p.write("          <td class=\"fieldvalue\"><select class=\"valueL\" name=\"field\" id=\"editcodeField_\" onchange=\"editcodes_onchange_field($('editcodeIsSelected_'+ this.id.split('_')[1]), this);\"><option></option></select></td>");
            p.write("        </tr>");
            p.write("    </table>");
            p.write("    <table class=\"popUpTable\">");
            p.write("        <tr>");
            p.write("            <td></td>");
            p.write("            <td></td>");
            p.write("            <td align=\"right\">");
            p.write("                <span class=\"noIcon\" onclick=\"editcodes_click_OK();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getOkTitle(), "</span>");
            p.write("                <span class=\"noIcon\" onclick=\"editcodes_click_Cancel();\" onmouseover=\"javascript:this.className='noIconhover';\" onmouseout=\"javascript:this.className='noIcon';\" >", texts.getCancelTitle(), "</span>");
            p.write("            </td>");
            p.write("        </tr>");
            p.write("    </table>");
            p.write("  </form>");
            p.write("</div>");
            
            String formId = (String)p.getProperty(HtmlPage.PROPERTY_FORM_ID);
            p.write("<form name=\"", formId, "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" action=\"\">");
            p.write("  <table cellspacing=\"8\" class=\"tableLayout\">");
            int ii = 0;
            for(
                Iterator i = this.controls.iterator();
                i.hasNext();
                ii++
            ) {
                Control control = (Control)i.next();
                p.write("    <tr>");
                p.write("      <td><br />");
                control.paint(
                    p, 
                    (String)this.frames.get(ii),
                    forEditing
                );
                p.write("      </td>");
                p.write("    </tr>");
            }
            Action cancelAction = editView.getCancelAction();
            Action saveAction = editView.getSaveAction();
            if(!editView.isEditMode()) {
                boolean showQualifier = 
                    application.getPortalExtension().hasUserDefineableQualifier(
                        editView.getEditInspectorControl().getInspector(), 
                        application
                    );
                p.write("    <tr>");
                p.write("      <td class=\"panel\">");
                if(showQualifier) {
                    p.write("        <span class=\"qualifierText\">", texts.getQualifierText(), "</span><br />");
                }
                p.write("        <input class=\"qualifier\" type=\"", (showQualifier ? "text" : "hidden"), "\" name=\"qualifier\" value=\"", org.openmdx.base.text.conversion.UUIDConversion.toUID(org.openmdx.kernel.id.UUIDs.getGenerator().next()), "\">");
                p.write("      </td>");
                p.write("    </tr>");
            }
            p.write("  <tr>");
            p.write("    <td><br />");
            p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", p.getView().getRequestId(), "\">");
            p.write("      <input type=\"hidden\" name=\"event.submit\" value=\"" + saveAction.getEvent(), "\">");
            p.write("      <a id=\"editObjectSave\" class=\"abutton\" href=\"#\"", p.getOnClick("javascript:try{HTMLArea._currentlyActiveEditor.setMode('textmode');}catch(e){};", p.getButtonEffectHighlight(), "document.forms['editObject'].submit();"), ">", saveAction.getTitle(), "</a>");
            p.write("      <a id=\"editObjectCancel\" class=\"abutton\" href=\"#\"", p.getOnClick("javascript:", p.getButtonEffectHighlight(), "this.href=", p.getEvalHRef(cancelAction), ";"), ">", cancelAction.getTitle(), "</a>");
            p.write("    </td>");
            p.write("  </tr>");       
            p.write("  </table>");
            p.write("</form>");
        }
    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 8920052019948316183L;

    public static final String LAYOUT_HORIZONTAL = "horizontal";
    public static final String LAYOUT_VERTICAL = "vertical";

    public static final String POPUP_EDIT_STRINGS = "popup_editstrings";
    public static final String POPUP_EDIT_CODES = "popup_editcodes";
    public static final String POPUP_EDIT_NUMBERS = "popup_editnumbers";
    public static final String POPUP_EDIT_BOOLEANS = "popup_editbooleans";
    public static final String POPUP_EDIT_DATES = "popup_editdates";
    public static final String POPUP_EDIT_DATETIMES = "popup_editdatetimes";
    
}

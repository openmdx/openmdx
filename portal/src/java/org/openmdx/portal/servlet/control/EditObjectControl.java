/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: EditObjectControl.java,v 1.38 2008/06/01 16:52:33 wfro Exp $
 * Description: EditObjectControl
 * Revision:    $Revision: 1.38 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/01 16:52:33 $
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
import java.util.Iterator;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.texts.Texts_1_0;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.View;
import org.openmdx.portal.servlet.view.ViewMode;

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
    public String getFormName(
        View view
    ) {
        String formName = this.getId();
        return view.getContainerElementId() == null
            ? formName
            : formName + "-" + view.getContainerElementId();        
    }
    
    //-------------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p, 
        String frame,
        boolean forEditing    
    ) throws ServiceException {
        ApplicationContext application = p.getApplicationContext();
        Texts_1_0 texts = application.getTexts();
        if(forEditing) {
            EditObjectView editView =  (EditObjectView)p.getView();
            // Popup multi-valued strings
            p.write("<div class=\"popUp\" id=\"", POPUP_EDIT_STRINGS, "\" style=\"display: none;\" onmousedown=\"dragPopupStart(event, this.id);\">");
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
            
            String formName = this.getFormName(editView);
            p.write("<form name=\"", formName, "\" id=\"", formName, "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" action=\"\">");
            p.write("  <table cellspacing=\"8\" class=\"tableLayout\">");
            int ii = 0;
            for(
                Iterator i = this.controls.iterator();
                i.hasNext();
                ii++
            ) {
                Control control = (Control)i.next();
                p.write("    <tr>");
                p.write("      <td>", (ii > 0 ? "<br />" : ""));
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
            p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", editView.getRequestId(), "\">");
            p.write("      <input type=\"hidden\" name=\"event.submit\" value=\"" + saveAction.getEvent(), "\">");
            // In case of an embedded view submit the form as an Ajax request
            // and put the attributes pane at the element with id aPanel
            String containerElementId = editView.getContainerElementId() == null
                ? "aPanel"
                : editView.getContainerElementId();
            if(editView.getMode() == ViewMode.EMBEDDED) {
                p.write("      <a id=\"editSave-", containerElementId, "\" class=\"abutton\" href=\"#\"", p.getOnClick("javascript:var editForm = document.forms['", formName, "']; var params = Form.serialize(editForm); new Ajax.Updater('", containerElementId, "', ", p.getEvalHRef(saveAction), ", {asynchronous:true, evalScripts: true, parameters: params, onComplete: function(){}});return false;"), ">", saveAction.getTitle(), "</a>");
                p.write("      <a id=\"editCancel-", containerElementId, "\" class=\"abutton\" href=\"#\"", p.getOnClick("javascript:new Ajax.Updater('", containerElementId, "', ", p.getEvalHRef(cancelAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){}});return false;"), ">", cancelAction.getTitle(), "</a>");
            }
            else {
                p.write("      <a id=\"editSave-", containerElementId, "\" class=\"abutton\" href=\"#\"", p.getOnClick("javascript:", p.getButtonEffectHighlight(), "document.forms['", formName, "'].submit();"), ">", saveAction.getTitle(), "</a>");
                p.write("      <a id=\"editCancel-", containerElementId, "\" class=\"abutton\" href=\"#\"", p.getOnClick("javascript:", p.getButtonEffectHighlight(), "this.href=", p.getEvalHRef(cancelAction), ";"), ">", cancelAction.getTitle(), "</a>");                              
            }
            p.write("    </td>");
            p.write("  </tr>");       
            p.write("  </table>");
            p.write("</form>");
            if(editView.getContainerElementId() != null) {
                p.write("<div class=\"gridSpacerBottom\"></div>");                        
            }                                                            
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

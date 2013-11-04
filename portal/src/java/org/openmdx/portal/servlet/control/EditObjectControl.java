/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: EditObjectControl
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2013, OMEX AG, Switzerland
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.Autocompleter_1_0;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.WebKeys;
import org.openmdx.portal.servlet.action.EditAsNewAction;
import org.openmdx.portal.servlet.view.EditObjectView;
import org.openmdx.portal.servlet.view.View;
import org.openmdx.portal.servlet.view.ViewMode;

/**
 * EditObjectControl
 *
 */
public class EditObjectControl extends ContainerControl implements Serializable {
  
    /**
     * Constructor 
     *
     * @param id
     * @param locale
     * @param localeAsIndex
     */
    public EditObjectControl(
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

    /**
     * Get form name.
     * 
     * @param view
     * @return
     */
    public String getFormName(
        View view
    ) {
        String formName = this.getId();
        return view.getContainerElementId() == null
            ? formName
            : formName + "-" + view.getContainerElementId();        
    }
    
    /**
     * Generic scripts for pop-up functions for multi-valued fields. 
     * 
     * @param p
     * @throws ServiceException
     */
    public static void paintEditPopups(
        ViewPort p
    ) throws ServiceException {
        ApplicationContext app = p.getApplicationContext();
        Texts_1_0 texts = app.getTexts();
        // Pop-up for multi-valued strings
        p.write("<div id=\"popup_", EDIT_STRINGS, "\" class=\"multiEditDialog\">");
        p.write("<div class=\"bd\">");
        p.write("  <form name=\"", EDIT_STRINGS, "\" method=\"post\" action=\"\">");
        p.write("    <table class=\"popUpTable\">");
        p.write("      <!-- template row -->");
        p.write("      <tr id=\"editstringrow\" style=\"display:block;\">");
        p.write("        <td class=\"fieldindex\" id=\"editstringIdx_\"></td>");
        p.write("        <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editstringIsSelected_\" onmouseup=\"", EDIT_STRINGS, "_onchange_checkbox(this, $('editstringField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("        <td class=\"fieldvalue\"><input type=\"text\" class=\"valueL\" name=\"field\" id=\"editstringField_\" onkeyup=\"", EDIT_STRINGS, "_onchange_field($('editstringIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("      </tr>");
        p.write("    </table>");
        p.write("    <div style=\"text-align:right;padding-top:8px;\">");
        p.write("      <input type=\"button\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:", EDIT_STRINGS, "_click_OK();\" />");
        p.write("      <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:popup_", EDIT_STRINGS, ".hide();\" />");
        p.write("    </div>");
        p.write("  </form>");
        p.write("</div>");
        p.write("</div>");
        // Pop-up for multi-valued numbers
        p.write("<div id=\"popup_", EDIT_NUMBERS, "\" class=\"multiEditDialog\">");
        p.write("<div class=\"bd\">");
        p.write("  <form name=\"", EDIT_NUMBERS, "\" method=\"post\" action=\"\">");
        p.write("    <table class=\"popUpTable\">");
        p.write("        <!-- template row -->");
        p.write("        <tr id=\"editnumberrow\" style=\"display:block;\">");
        p.write("          <td class=\"fieldindex\" id=\"editnumberIdx_\"></td>");
        p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editnumberIsSelected_\" onmouseup=\"", EDIT_NUMBERS, "_onchange_checkbox(this, $('editnumberField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("          <td class=\"fieldvalue\"><input type=\"text\" class=\"valueR\" name=\"field\" id=\"editnumberField_\" onkeyup=\"", EDIT_NUMBERS, "_onchange_field($('editnumberIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("        </tr>");
        p.write("    </table>");
        p.write("    <div style=\"text-align:right;padding-top:8px;\">");
        p.write("      <input type=\"button\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:", EDIT_NUMBERS, "_click_OK();\" />");
        p.write("      <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:popup_", EDIT_NUMBERS, ".hide();\" />");
        p.write("    </div>");
        p.write("  </form>");
        p.write("</div>");
        p.write("</div>");
        // Pop-up for multi-valued dates
        p.write("<div id=\"popup_", EDIT_DATES, "\" class=\"multiEditDialog\">");
        p.write("<div class=\"bd\">");
        p.write("  <form name=\"", EDIT_DATES, "\" method=\"post\" action=\"\">");
        p.write("    <table class=\"popUpTable\">");
        p.write("        <!-- template row -->");
        p.write("        <tr id=\"editdaterow\" style=\"display:block;\">");
        p.write("          <td class=\"fieldindex\" id=\"editdateIdx_\"></td>");
        p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editdateIsSelected_\" onmouseup=\"", EDIT_DATES, "_onchange_checkbox(this, $('editdateField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("          <td class=\"fieldvalue\"><input type=\"text\" class=\"valueR\" name=\"field\" id=\"editdateField_\" title=\"wfro: locale dependent\" onkeyup=\"", EDIT_DATES, "_onchange_field($('editdateIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("          <td class=\"addon\"><a><img class=\"popUpButton\" id=\"cal_date_trigger_\" border=\"0\" alt=\"\" src=\"", p.getResourcePath("images/"), "cal.gif\" /></a></td>");
        p.write("        </tr>");
        p.write("    </table>");
        p.write("    <div style=\"text-align:right;padding-top:8px;\">");
        p.write("      <input type=\"button\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:", EDIT_DATES, "_click_OK();\" />");
        p.write("      <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:popup_", EDIT_DATES, ".hide();\" />");
        p.write("    </div>");
        p.write("  </form>");
        p.write("</div>");
        p.write("</div>");
        // Pop-up for multi-valued datetime
        p.write("<div id=\"popup_", EDIT_DATETIMES, "\" class=\"multiEditDialog\">");
        p.write("<div class=\"bd\">");
        p.write("  <form name=\"", EDIT_DATETIMES, "\" method=\"post\" action=\"\">");
        p.write("    <table class=\"popUpTable\">");
        p.write("        <!-- template row -->");
        p.write("        <tr id=\"editdatetimerow\" style=\"display:block;\">");
        p.write("          <td class=\"fieldindex\" id=\"editdatetimeIdx_\"></td>");
        p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editdatetimeIsSelected_\" onmouseup=\"", EDIT_DATETIMES, "_onchange_checkbox(this, $('editdatetimeField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("          <td class=\"fieldvalue\"><input type=\"text\" class=\"valueR\" name=\"field\" id=\"editdatetimeField_\" title=\"wfro: locale dependent\" onkeyup=\"", EDIT_DATETIMES, "_onchange_field($('editdatetimeIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("          <td class=\"addon\"><a><img class=\"popUpButton\" id=\"cal_datetime_trigger_\" border=\"0\" alt=\"\" src=\"", p.getResourcePath("images/"), "cal.gif\" /></a></td>");
        p.write("        </tr>");
        p.write("    </table>");
        p.write("    <div style=\"text-align:right;padding-top:8px;\">");
        p.write("      <input type=\"button\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:", EDIT_DATETIMES, "_click_OK();\" />");
        p.write("      <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:popup_", EDIT_DATETIMES, ".hide();\" />");
        p.write("    </div>");
        p.write("  </form>");
        p.write("</div>");
        p.write("</div>");
        // Pop-up for multi-valued booleans
        p.write("<div id=\"popup_", EDIT_BOOLEANS, "\" class=\"multiEditDialog\">");
        p.write("<div class=\"bd\">");
        p.write("  <form name=\"", EDIT_BOOLEANS, "\" method=\"post\" action=\"\">");
        p.write("    <table class=\"popUpTable\">");
        p.write("        <!-- template row -->");
        p.write("        <tr id=\"editbooleanrow\" style=\"display:block;\">");
        p.write("          <td class=\"fieldindex\" id=\"editbooleanIdx_\"></td>");
        p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editbooleanIsSelected_\" onmouseup=\"", EDIT_BOOLEANS, "_onchange_checkbox(this, $('editbooleanField_' + this.id.split('_')[1]));\"></td>");
        p.write("          <td class=\"fieldvalue\"><input type=\"checkbox\" class=\"valueL\" name=\"field\" id=\"editbooleanField_\" onmouseup=\"", EDIT_BOOLEANS, "_onchange_field($('editbooleanIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("        </tr>");
        p.write("    </table>");
        p.write("    <div style=\"text-align:right;padding-top:8px;\">");
        p.write("      <input type=\"button\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:", EDIT_BOOLEANS, "_click_OK();\" />");
        p.write("      <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:popup_", EDIT_BOOLEANS, ".hide();\" />");
        p.write("    </div>");
        p.write("  </form>");
        p.write("</div>");
        p.write("</div>");
        // Pop-up for multi-valued codes
        p.write("<div id=\"popup_", EDIT_CODES, "\" class=\"multiEditDialog\">");
        p.write("<div class=\"bd\">");
        p.write("  <form name=\"", EDIT_CODES, "\" method=\"post\" action=\"\">");
        p.write("    <table class=\"popUpTable\">");
        p.write("        <!-- template row -->");
        p.write("        <tr id=\"editcoderow\" style=\"display:none;\">");
        p.write("          <td class=\"fieldindex\" id=\"editcodeIdx_\"></td>");
        p.write("          <td class=\"fieldselection\"><input type=\"checkbox\" name=\"isSelected\" id=\"editcodeIsSelected_\" onmouseup=\"", EDIT_CODES, "_onchange_checkbox(this, $('editcodeField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("          <td class=\"fieldvalue\"><select class=\"valueL\" name=\"field\" id=\"editcodeField_\" onchange=\"", EDIT_CODES, "_onchange_field($('editcodeIsSelected_'+ this.id.split('_')[1]), this);\"><option></option></select></td>");
        p.write("        </tr>");
        p.write("    </table>");
        p.write("    <div style=\"text-align:right;padding-top:8px;\">");
        p.write("      <input type=\"button\" value=\"", texts.getOkTitle(), "\" onclick=\"javascript:", EDIT_CODES, "_click_OK();\" />");
        p.write("      <input type=\"button\" value=\"", texts.getCancelTitle(), "\" onclick=\"javascript:popup_", EDIT_CODES, ".hide();\" />");
        p.write("    </div>");
        p.write("  </form>");
        p.write("</div>");
        p.write("</div>");        
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
        ApplicationContext app = p.getApplicationContext();
        Texts_1_0 texts = app.getTexts();
        HtmlEncoder_1_0 htmlEncoder = app.getHtmlEncoder();
        if(forEditing) {
            EditObjectView editView =  (EditObjectView)p.getView();
            String formId = this.getFormName(editView);
            Model_1_0 model = app.getModel();
            // Render lookup field for existing objects
            boolean hasLookupField = false;
            if(editView.getMode() == ViewMode.STANDARD) {            	
	            String forReferenceQualifiedName = null;
	            try {
	            	forReferenceQualifiedName = editView.getParent() == null
		            	? null
		            	: (String)model.getFeatureDef(model.getElement(editView.getParent().refClass().refMofId()), editView.getForReference(), false).objGetValue("qualifiedName");
	            } catch(Exception ignore) {}
	            if(forReferenceQualifiedName != null) {
		            Autocompleter_1_0 lookupExistingObjectAutocompleter = app.getPortalExtension().getAutocompleter(
		            	app, 
		            	editView.getParent(), 
		            	forReferenceQualifiedName,
		            	editView.getEditInspectorControl().getInspector().getForClass()
		            );
		            if(lookupExistingObjectAutocompleter != null) {
		            	String fieldId = formId + "LookupExisting";
		            	String formDetailsId = formId + "_details";
		            	String label = app.getTexts().getSelectExistingText();		            	
		            	p.write("<table class=\"tableLayout\" cellspacing=\"8\">");
		            	p.write("  <tr><td>");
		            	p.write("  <div class=\"qualifier\" style=\"font-weight:normal;width:100%;padding:10px;\">");
		            	p.write("    <table class=\"fieldGroup\">");
		            	p.write("      <tr>");
		                p.write("        <td class=\"label\" title=\"", htmlEncoder.encode(label, false), "\"><span class=\"nw\">", htmlEncoder.encode(label, false), ":", "</span></td>");            
		                p.write("        <td>");
		                String onChangeValueScript =
		                	"xri=this.value;" +
		                    "$('" + formDetailsId + "').parentNode.className+=' wait';" +
		                	"$('" + formDetailsId + "').innerHTML='';" +
		                    "new Ajax.Updater('" + formDetailsId + "', './'+getEncodedHRef(['" + WebKeys.SERVLET_NAME + "', '" + Action.PARAMETER_REQUEST_ID + "', '" + editView.getRequestId() + "', 'event', '" + EditAsNewAction.EVENT_ID + "', 'parameter', 'xri*('+xri+')*mode*(" + ViewMode.EMBEDDED + ")']), {asynchronous:true, evalScripts: true, onComplete: function(){$('" + formDetailsId + "').parentNode.className='';}});";
		                lookupExistingObjectAutocompleter.paint(
		                    p,
		                    fieldId,
		                    1, // tabIndex
		                    forReferenceQualifiedName,
		                    null, // currentValue
		                    false,
		                    null,
		                    "class=\"autocompleterInput\"",
		                    "class=\"valueL valueAC\"",
		                    null, // imgTag
		                    onChangeValueScript
		                );
		                p.write("        </td>");
		                p.write("        <td class=\"addon\">");
		                String lookupId = UUIDs.newUUID().toString();
	                    Action findObjectAction = Action.getFindObjectAction(
	                    	forReferenceQualifiedName, 
	                    	lookupId
	                    );
	                    p.write("  ", p.getImg("class=\"popUpButton\" border=\"0\" alt=\"Click to open ObjectFinder\" src=\"", p.getResourcePath("images/"), findObjectAction.getIconKey(), "\" onclick=\"javascript:OF.findObject(", p.getEvalHRef(findObjectAction), ", $('", fieldId, ".Title'), $('", fieldId, "'), '", lookupId, "');\""));                    		                
		                p.write("        </td>");
		                p.write("      </tr>");
		                p.write("    </table>");
		                p.write("  </div>");
		            	p.write("  <div>&nbsp;</div>");
		            	p.write("  </td></tr>");
		            	p.write("</table>");
		            	p.write("<div style=\"min-height:30px;\">");
		            	p.write("  <div id=\"", formDetailsId, "\">");
		            	hasLookupField = true;
		            }
	            }
            }
            p.write("<form name=\"", formId, "\" id=\"", formId, "\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" method=\"post\" action=\"\">");
            p.write("  <table cellspacing=\"8\" class=\"tableLayout\">");
            int ii = 0;
            for(Control control: this.controls) {
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
            if(!editView.isEditMode()) {
                boolean showQualifier = 
                    app.getPortalExtension().hasUserDefineableQualifier(
                        editView.getEditInspectorControl().getInspector(), 
                        app
                    );
                p.write("    <tr>");
                p.write("      <td class=\"panel\">");
                if(showQualifier) {
                    p.write("        <span class=\"qualifierText\">", texts.getQualifierText(), "</span><br />");
                }
                p.write("        <input class=\"qualifier\" type=\"", (showQualifier ? "text" : "hidden"), "\" name=\"qualifier\" value=\"", org.openmdx.base.text.conversion.UUIDConversion.toUID(org.openmdx.kernel.id.UUIDs.newUUID()), "\">");
                p.write("      </td>");
                p.write("    </tr>");
            }
            p.write("  <tr>");
            p.write("    <td><br />");
            p.write("      <input type=\"hidden\" name=\"requestId.submit\" value=\"", editView.getRequestId(), "\">");
            Action cancelAction = editView.getCancelAction();
            Action saveAction = editView.getSaveAction();
            Action createAction = editView.getCreateAction();
            p.write("      <input type=\"hidden\" id=\"event.submit\" name=\"event.submit\" value=\"\" >");
            String containerElementId = editView.getContainerElementId() == null
                ? "aPanel"
                : editView.getContainerElementId();
            if(saveAction != null) {
            	if(editView.isEditMode()) {
            		p.write("      <a id=\"editSave-", containerElementId, "\" class=\"abutton\" href=\"#\" onclick=\"javascript:$('event.submit').value='", Integer.toString(saveAction.getEvent()), "';var editForm=document.forms['", formId, "'];var params=Form.serialize(editForm);new Ajax.Updater('", containerElementId, "', ", p.getEvalHRef(saveAction), ", {asynchronous:true, evalScripts: true, parameters: params, onComplete: function(){}});return false;\">", saveAction.getTitle(), "</a>");
            	} else {
            		p.write("      <a id=\"editSave-", containerElementId, "\" class=\"abutton\" href=\"#\" onclick=\"javascript:", p.getButtonEffectHighlight(), "$('event.submit').value='", Integer.toString(saveAction.getEvent()), "';document.forms['", formId, "'].submit();\">", saveAction.getTitle(), "</a>");
            	}
            }
            if(createAction != null) {
            	if(editView.isEditMode()) {
            		p.write("      <a id=\"editCreate-", containerElementId, "\" class=\"abutton\" href=\"#\" onclick=\"javascript:$('event.submit').value='", Integer.toString(saveAction.getEvent()), "';var editForm=document.forms['", formId, "'];var params=Form.serialize(editForm);new Ajax.Updater('", containerElementId, "', ", p.getEvalHRef(createAction), ", {asynchronous:true, evalScripts: true, parameters: params, onComplete: function(){}});return false;\">", createAction.getTitle(), "</a>");
            	} else {
            		p.write("      <a id=\"editCreate-", containerElementId, "\" class=\"abutton\" href=\"#\" onclick=\"javascript:", p.getButtonEffectHighlight(), "$('event.submit').value='", Integer.toString(createAction.getEvent()), "';document.forms['", formId, "'].submit();\">", createAction.getTitle(), "</a>");
            	}
            }
            if(cancelAction != null) {
            	if(editView.isEditMode()) {
            		p.write("      <a id=\"editCancel-", containerElementId, "\" class=\"abutton\" href=\"#\" onclick=\"javascript:new Ajax.Updater('", containerElementId, "', ", p.getEvalHRef(cancelAction), ", {asynchronous:true, evalScripts: true, onComplete: function(){}});return false;\">", cancelAction.getTitle(), "</a>");            	
            	} else {
            		p.write("      <a id=\"editCancel-", containerElementId, "\" class=\"abutton\" href=\"#\" onclick=\"javascript:", p.getButtonEffectHighlight(), "this.href=", p.getEvalHRef(cancelAction), ";\">", cancelAction.getTitle(), "</a>");                                          	
            	}
            }
            p.write("    </td>");
            p.write("  </tr>");       
            p.write("  </table>");
            p.write("</form>");
            if(editView.getContainerElementId() != null) {
                p.write("<div class=\"gridSpacerBottom\"></div>");                        
            }
            if(hasLookupField) {
            	p.write("  </div>");
            	p.write("</div>");
            }
        }
    }

    //-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 8920052019948316183L;

    public static final String LAYOUT_HORIZONTAL = "horizontal";
    public static final String LAYOUT_VERTICAL = "vertical";

    public static final String EDIT_STRINGS = "editstrings";
    public static final String EDIT_CODES = "editcodes";
    public static final String EDIT_NUMBERS = "editnumbers";
    public static final String EDIT_BOOLEANS = "editbooleans";
    public static final String EDIT_DATES = "editdates";
    public static final String EDIT_DATETIMES = "editdatetimes";
    
}

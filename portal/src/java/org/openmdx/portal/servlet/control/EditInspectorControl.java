/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: EditInspectorControl
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
import java.util.Collections;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.PortalExtension_1_0;
import org.openmdx.portal.servlet.Texts_1_0;
import org.openmdx.portal.servlet.ViewPort;

/**
 * EditInspectorControl
 *
 */
public class EditInspectorControl extends InspectorControl implements Serializable {
  
    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     * @param controlFactory
     * @param inspectorDef
     */
    public EditInspectorControl(
        String id,
        String locale,
        int localeAsIndex,
        PortalExtension_1_0.ControlFactory controlFactory,
        org.openmdx.ui1.jmi1.Inspector inspectorDef
    ) {
        super(
            id,
            locale,
            localeAsIndex,
            controlFactory,
            inspectorDef
        );
    	this.showErrorsControl= new ShowErrorsControl(
    		"errors",
    		locale,
    		localeAsIndex
      	);
    	this.titleControl = new EditObjectTitleControl(
    		"title",
    		locale,
    		localeAsIndex
    	);
    }
  
    /* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.control.InspectorControl#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Control> List<T> getChildren(
		Class<T> type
	) {
		if(type == ShowErrorsControl.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)Collections.singletonList(this.showErrorsControl);
			return children;
		} else if(type == EditObjectTitleControl.class) {
			@SuppressWarnings("unchecked")
			List<T> children = (List<T>)Collections.singletonList(this.titleControl);
			return children;
		} else {
			return super.getChildren(type);
		}
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
        p.write("<div id=\"popup_", EDIT_STRINGS, "\" class=\"modal\" role=\"dialog\" style=\"overflow:hidden;\">");
        p.write("  <div class=\"modal-dialog\">");
        p.write("    <div class=\"modal-content\">");
        p.write("      <div class=\"modal-body\">");
        p.write("        <form name=\"", EDIT_STRINGS, "\" method=\"post\" action=\"\">");
        p.write("          <table class=\"", CssClass.popUpTable.toString(), "\">");
        p.write("            <!-- template row -->");
        p.write("            <tr id=\"editstringrow\" style=\"display:block;\">");
        p.write("              <td class=\"", CssClass.fieldindex.toString(), "\" id=\"editstringIdx_\"></td>");
        p.write("              <td class=\"", CssClass.fieldselection.toString(), "\"><input type=\"checkbox\" name=\"isSelected\" id=\"editstringIsSelected_\" onmouseup=\"", EDIT_STRINGS, "_onchange_checkbox(this, $('editstringField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("              <td class=\"", CssClass.fieldvalue.toString(), "\"><input type=\"text\" class=\"", CssClass.valueL.toString(), "\" name=\"field\" id=\"editstringField_\" onkeyup=\"", EDIT_STRINGS, "_onchange_field($('editstringIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("            </tr>");
        p.write("          </table>");
        p.write("        </form>");
        p.write("      </div>");
        p.write("      <div class=\"modal-footer\">");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getOkTitle(), "\" data-dismiss=\"modal\" onclick=\"javascript:", EDIT_STRINGS, "_click_OK();\" />");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getCancelTitle(), "\" data-dismiss=\"modal\" />");
        p.write("      </div>");
        p.write("    </div>");
        p.write("  </div>");
        p.write("</div>");
        // Pop-up for multi-valued numbers
        p.write("<div id=\"popup_", EDIT_NUMBERS, "\" class=\"modal\" role=\"dialog\" style=\"overflow:hidden;\">");
        p.write("  <div class=\"modal-dialog\">");
        p.write("    <div class=\"modal-content\">");
        p.write("      <div class=\"modal-body\">");
        p.write("        <form name=\"", EDIT_NUMBERS, "\" method=\"post\" action=\"\">");
        p.write("          <table class=\"", CssClass.popUpTable.toString(), "\">");
        p.write("            <!-- template row -->");
        p.write("            <tr id=\"editnumberrow\" style=\"display:block;\">");
        p.write("              <td class=\"", CssClass.fieldindex.toString(), "\" id=\"editnumberIdx_\"></td>");
        p.write("              <td class=\"", CssClass.fieldselection.toString(), "\"><input type=\"checkbox\" name=\"isSelected\" id=\"editnumberIsSelected_\" onmouseup=\"", EDIT_NUMBERS, "_onchange_checkbox(this, $('editnumberField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("              <td class=\"", CssClass.fieldvalue.toString(), "\"><input type=\"text\" class=\"", CssClass.valueR.toString(), "\" name=\"field\" id=\"editnumberField_\" onkeyup=\"", EDIT_NUMBERS, "_onchange_field($('editnumberIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("            </tr>");
        p.write("          </table>");
        p.write("        </form>");
        p.write("      </div>");
        p.write("      <div class=\"modal-footer\">");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getOkTitle(), "\" data-dismiss=\"modal\" onclick=\"javascript:", EDIT_NUMBERS, "_click_OK();\" />");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getCancelTitle(), "\" data-dismiss=\"modal\" />");
        p.write("      </div>");
        p.write("    </div>");
        p.write("  </div>");
        p.write("</div>");
        // Pop-up for multi-valued dates
        p.write("<div id=\"popup_", EDIT_DATES, "\" class=\"modal\" role=\"dialog\" style=\"overflow:hidden;\">");
        p.write("  <div class=\"modal-dialog\">");
        p.write("    <div class=\"modal-content\">");
        p.write("      <div class=\"modal-body\">");
        p.write("        <form name=\"", EDIT_DATES, "\" method=\"post\" action=\"\">");
        p.write("          <table class=\"", CssClass.popUpTable.toString(), "\">");
        p.write("            <!-- template row -->");
        p.write("            <tr id=\"editdaterow\" style=\"display:block;\">");
        p.write("              <td class=\"", CssClass.fieldindex.toString(), "\" id=\"editdateIdx_\"></td>");
        p.write("              <td class=\"", CssClass.fieldselection.toString(), "\"><input type=\"checkbox\" name=\"isSelected\" id=\"editdateIsSelected_\" onmouseup=\"", EDIT_DATES, "_onchange_checkbox(this, $('editdateField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("              <td class=\"", CssClass.fieldvalue.toString(), "\"><input type=\"text\" class=\"", CssClass.valueR.toString(), "\" name=\"field\" id=\"editdateField_\" onkeyup=\"", EDIT_DATES, "_onchange_field($('editdateIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("              <td class=\"", CssClass.addon.toString(), "\"><a><img class=\"", CssClass.popUpButton.toString(), "\" id=\"cal_date_trigger_\" border=\"0\" alt=\"\" src=\"", p.getResourcePath("images/"), "cal.gif\" /></a></td>");
        p.write("            </tr>");
        p.write("          </table>");
        p.write("        </form>");
        p.write("      </div>");
        p.write("      <div class=\"modal-footer\">");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getOkTitle(), "\" data-dismiss=\"modal\" onclick=\"javascript:", EDIT_DATES, "_click_OK();\" />");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getCancelTitle(), "\" data-dismiss=\"modal\" />");
        p.write("      </div>");
        p.write("    </div>");
        p.write("  </div>");
        p.write("</div>");
        // Pop-up for multi-valued datetime
        p.write("<div id=\"popup_", EDIT_DATETIMES, "\" class=\"modal\" role=\"dialog\" style=\"overflow:hidden;\">");
        p.write("  <div class=\"modal-dialog\">");
        p.write("    <div class=\"modal-content\">");
        p.write("      <div class=\"modal-body\">");
        p.write("        <form name=\"", EDIT_DATETIMES, "\" method=\"post\" action=\"\">");
        p.write("          <table class=\"", CssClass.popUpTable.toString(), "\">");
        p.write("            <!-- template row -->");
        p.write("            <tr id=\"editdatetimerow\" style=\"display:block;\">");
        p.write("              <td class=\"", CssClass.fieldindex.toString(), "\" id=\"editdatetimeIdx_\"></td>");
        p.write("              <td class=\"", CssClass.fieldselection.toString(), "\"><input type=\"checkbox\" name=\"isSelected\" id=\"editdatetimeIsSelected_\" onmouseup=\"", EDIT_DATETIMES, "_onchange_checkbox(this, $('editdatetimeField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("              <td class=\"", CssClass.fieldvalue.toString(), "\"><input type=\"text\" class=\"", CssClass.valueR.toString(), "\" name=\"field\" id=\"editdatetimeField_\" onkeyup=\"", EDIT_DATETIMES, "_onchange_field($('editdatetimeIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("              <td class=\"addon\"><a><img class=\"", CssClass.popUpButton.toString(), "\" id=\"cal_datetime_trigger_\" border=\"0\" alt=\"\" src=\"", p.getResourcePath("images/"), "cal.gif\" /></a></td>");
        p.write("            </tr>");
        p.write("          </table>");
        p.write("        </form>");
        p.write("      </div>");
        p.write("      <div class=\"modal-footer\">");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getOkTitle(), "\" data-dismiss=\"modal\" onclick=\"javascript:", EDIT_DATETIMES, "_click_OK();\" />");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getCancelTitle(), "\" data-dismiss=\"modal\" />");
        p.write("      </div>");
        p.write("    </div>");
        p.write("  </div>");
        p.write("</div>");
        // Pop-up for multi-valued booleans
        p.write("<div id=\"popup_", EDIT_BOOLEANS, "\" class=\"modal\" role=\"dialog\" style=\"overflow:hidden;\">");
        p.write("  <div class=\"modal-dialog\">");
        p.write("    <div class=\"modal-content\">");
        p.write("      <div class=\"modal-body\">");
        p.write("        <form name=\"", EDIT_BOOLEANS, "\" method=\"post\" action=\"\">");
        p.write("          <table class=\"", CssClass.popUpTable.toString(), "\">");
        p.write("            <!-- template row -->");
        p.write("            <tr id=\"editbooleanrow\" style=\"display:block;\">");
        p.write("              <td class=\"", CssClass.fieldindex.toString(), "\" id=\"editbooleanIdx_\"></td>");
        p.write("              <td class=\"", CssClass.fieldselection.toString(), "\"><input type=\"checkbox\" name=\"isSelected\" id=\"editbooleanIsSelected_\" onmouseup=\"", EDIT_BOOLEANS, "_onchange_checkbox(this, $('editbooleanField_' + this.id.split('_')[1]));\"></td>");
        p.write("              <td class=\"", CssClass.fieldvalue.toString(), "\"><input type=\"checkbox\" class=\"", CssClass.valueL.toString(), "\" name=\"field\" id=\"editbooleanField_\" onmouseup=\"", EDIT_BOOLEANS, "_onchange_field($('editbooleanIsSelected_'+ this.id.split('_')[1]), this);\"></td>");
        p.write("            </tr>");
        p.write("          </table>");
        p.write("        </form>");
        p.write("      </div>");
        p.write("      <div class=\"modal-footer\">");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getOkTitle(), "\" data-dismiss=\"modal\" onclick=\"javascript:", EDIT_BOOLEANS, "_click_OK();\" />");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getCancelTitle(), "\" data-dismiss=\"modal\" />");
        p.write("      </div>");
        p.write("    </div>");
        p.write("  </div>");
        p.write("</div>");
        // Pop-up for multi-valued codes
        p.write("<div id=\"popup_", EDIT_CODES, "\" class=\"modal\" role=\"dialog\">");
        p.write("  <div class=\"modal-dialog\">");
        p.write("    <div class=\"modal-content\">");
        p.write("      <div class=\"modal-body\">");
        p.write("        <form name=\"", EDIT_CODES, "\" method=\"post\" action=\"\">");
        p.write("          <table class=\"", CssClass.popUpTable.toString(), "\">");
        p.write("            <!-- template row -->");
        p.write("            <tr id=\"editcoderow\" style=\"display:none;\">");
        p.write("              <td class=\"", CssClass.fieldindex.toString(), "\" id=\"editcodeIdx_\"></td>");
        p.write("              <td class=\"", CssClass.fieldselection.toString(), "\"><input type=\"checkbox\" name=\"isSelected\" id=\"editcodeIsSelected_\" onmouseup=\"", EDIT_CODES, "_onchange_checkbox(this, $('editcodeField_' + this.id.split('_')[1]));\" value=\"checkbox\"></td>");
        p.write("              <td class=\"", CssClass.fieldvalue.toString(), "\"><select class=\"", CssClass.valueL.toString(), "\" name=\"field\" id=\"editcodeField_\" onchange=\"", EDIT_CODES, "_onchange_field($('editcodeIsSelected_'+ this.id.split('_')[1]), this);\"><option></option></select></td>");
        p.write("            </tr>");
        p.write("          </table>");
        p.write("        </form>");
        p.write("      </div>");
        p.write("      <div class=\"modal-footer\">");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getOkTitle(), "\" data-dismiss=\"modal\" onclick=\"javascript:", EDIT_CODES, "_click_OK();\" />");
        p.write("        <input type=\"button\" class=\"", CssClass.btn.toString(), " ", CssClass.btnDefault.toString(), "\" value=\"", texts.getCancelTitle(), "\" data-dismiss=\"modal\" />");
        p.write("      </div>");
        p.write("    </div>");        
        p.write("  </div>");
        p.write("</div>");
    }

    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 8141773481046519758L;

    public static final String LAYOUT_HORIZONTAL = "horizontal";
    public static final String LAYOUT_VERTICAL = "vertical";

    public static final String EDIT_STRINGS = "editstrings";
    public static final String EDIT_CODES = "editcodes";
    public static final String EDIT_NUMBERS = "editnumbers";
    public static final String EDIT_BOOLEANS = "editbooleans";
    public static final String EDIT_DATES = "editdates";
    public static final String EDIT_DATETIMES = "editdatetimes";

    private ShowErrorsControl showErrorsControl;
    private EditObjectTitleControl titleControl;
    
}

//--- End of File -----------------------------------------------------------

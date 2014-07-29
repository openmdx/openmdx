/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: FlashBinaryValue 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2008-2014, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.portal.servlet.attribute;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.HtmlEncoder_1_0;
import org.openmdx.portal.servlet.ViewPort;

/**
 * FlashBinaryValue
 *
 */
public class FlashBinaryValue extends BinaryValue {

	/**
     * Constructor 
     *
     * @param object
     * @param fieldDef
     * @param application
     */
    public FlashBinaryValue(
        Object object,
        FieldDef fieldDef,
        ApplicationContext application
    ) throws ServiceException {
        super(
            object, 
            fieldDef, 
            application
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.attribute.BinaryValue#paintInPlace(org.openmdx.portal.servlet.ViewPort, org.openmdx.portal.servlet.Action, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    protected void paintInPlace(
        ViewPort p,
        Action binaryValueAction,
        String label,
        String gapModifier,
        String rowSpanModifier,
        String widthModifier,
        String styleModifier
    ) throws ServiceException {
        HtmlEncoder_1_0 htmlEncoder = p.getApplicationContext().getHtmlEncoder();                
        String imageId = org.openmdx.kernel.id.UUIDs.newUUID().toString();
        CharSequence imageSrc = p.getEncodedHRef(binaryValueAction);
        p.write(gapModifier); 
    	String cssClass = this.app.getPortalExtension().getDefaultCssClassFieldGroup(this, this.app);
    	if(this.getCssClassFieldGroup() != null) {
    		cssClass = this.getCssClassFieldGroup() + " " + cssClass;
    	}
        p.write("<td class=\"", CssClass.fieldLabel.toString(), "\"><span class=\"", CssClass.nw.toString(), "\">", htmlEncoder.encode(label, false), "</span></td>");
        p.write("<td ", rowSpanModifier, " class=\"", cssClass, "\" ", widthModifier, " id=\"tdImage", imageId, "\">");
        p.write("<div class=\"", CssClass.valuePicture.toString(), "\" ", styleModifier, ">");
        p.write("  <object classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0\">");
        p.write("    <param name=\"movie\" value=\"", imageSrc, "\">");
        p.write("    <param name=\"quality\" value=\"high\">");
        p.write("    <embed src=\"", imageSrc, "\" quality=\"high\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" type=\"application/x-shockwave-flash\"></embed>");
        p.write("  </object>");        
        p.write("</div>");
        p.write("</td>");                                                            
    }

    private static final long serialVersionUID = -9190625128882528646L;

}

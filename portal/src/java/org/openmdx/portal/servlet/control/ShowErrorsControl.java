/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: ShowErrorsControl.java,v 1.15 2008/08/27 14:04:50 wfro Exp $
 * Description: ReferencePaneRenderer
 * Revision:    $AttributePaneRenderer: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/08/27 14:04:50 $
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.attribute.DateValue;

public class ShowErrorsControl
    extends Control
    implements Serializable {

    //-------------------------------------------------------------------------
    public ShowErrorsControl(
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
    
    //---------------------------------------------------------------------------------
    @Override
    public void paint(
        HtmlPage p,
        String frame,
        boolean forEditing        
    ) throws ServiceException {
        AppLog.detail("> paint");        
        ApplicationContext app = p.getApplicationContext();
        if(app.getErrorMessages().size() > 0) {
           p.write("<table class=\"tableError\">");
           SimpleDateFormat dateTimeFormat = DateValue.getLocalizedDateTimeFormatter(
               null, 
               true, 
               app
           );
           String formattedDateTime = dateTimeFormat.format(new Date());
           String separator = " | ";
           p.write("  <tr>");
           p.write("    <td class=\"cellErrorLeft\">Error</td>");
           p.write("    <td class=\"cellErrorRight\">");
           p.write(formattedDateTime.replace(" ", separator), separator, dateTimeFormat.getTimeZone().getID());
           p.write("    </td>");
           p.write("  </tr>");
           p.write("  <tr>");
           p.write("    <td class=\"cellErrorLeft\"></td>");
           p.write("    <td class=\"cellErrorRight\">");
           for(int i = 0; i < app.getErrorMessages().size(); i++) {
             p.write("      " + app.getErrorMessages().get(i), "<br />");
           }
           p.write("    </td>");
           p.write("  </tr>");
           p.write("</table>");
        }        
        AppLog.detail("< paint");
    }

    //---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
    private static final long serialVersionUID = -5088760857844228009L;
    
}

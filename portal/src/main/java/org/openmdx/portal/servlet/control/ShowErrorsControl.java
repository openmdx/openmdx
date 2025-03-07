/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ShowErrorsControl
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
import java.util.Collections;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.ApplicationContext;
import org.openmdx.portal.servlet.CssClass;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.attribute.DateValue;

/**
 * ShowErrorsControl
 *
 */
public class ShowErrorsControl extends Control implements Serializable {

    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     */
    public ShowErrorsControl(
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
    	SysLog.detail("> paint");        
        ApplicationContext app = p.getApplicationContext();
        if(!app.getErrorMessages().isEmpty()) {
           p.write("<table style=\"width:100%;\">");
           SimpleDateFormat dateTimeFormat = DateValue.getLocalizedDateTimeFormatter(
               null, 
               true, 
               app
           );
           String formattedDateTime = dateTimeFormat.format(#if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now()#endif);
           String separator = " | ";
           p.write("  <tr>");
           p.write("    <td>");
           p.write("      <div class=\"", CssClass.alert.toString(), " ", CssClass.alert_info.toString(), "\" style=\"margin-bottom:0px;\">");
           p.write(formattedDateTime.replace(" ", separator), separator, dateTimeFormat.getTimeZone().getID());
           p.write("      </div>");
           p.write("    </td>");
           p.write("  </tr>");
           p.write("  <tr>");
           p.write("    <td>");
           for(int i = 0; i < app.getErrorMessages().size(); i++) {
        	   String message = app.getErrorMessages().get(i);
        	   String alertClass = CssClass.alert.toString() + " ";
        	   if(message.startsWith(CssClass.alert.toString() + "-") && message.indexOf(" ") > 0) {
        		   int pos = message.indexOf(" ");
        		   alertClass += message.substring(0, pos).trim();
        		   message = message.substring(pos + 1);
        	   } else {
        		   alertClass += CssClass.alert_danger.toString();
        	   }
        	   p.write("      <div class=\"", alertClass, "\" style=\"margin-bottom:0px;\">");
        	   p.write("      " + message);
        	   p.write("      </div>");
           }
           p.write("    </td>");
           p.write("  </tr>");
           p.write("</table>");
        }
        SysLog.detail("< paint");
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
	
    //---------------------------------------------------------------------------------
    // Members
    //---------------------------------------------------------------------------------
    private static final long serialVersionUID = -5088760857844228009L;

}

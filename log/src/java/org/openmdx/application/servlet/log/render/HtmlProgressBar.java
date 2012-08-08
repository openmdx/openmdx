/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: HtmlProgressBar.java,v 1.1 2008/03/21 18:21:49 hburger Exp $
 * Description: HTML Progress Bar
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:49 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
 */
package org.openmdx.application.servlet.log.render;


/**
 * Renders a HTML progress bar based on HTML tables
 */
public class HtmlProgressBar {


        public HtmlProgressBar(long pos, long active, long width)
        {
                init(pos, active, width, 0);
        }

        public HtmlProgressBar(long pos, long active, long width, long barWidth)
        {
                init(pos, active, width, barWidth);
        }


        public String render()
        {
        	StringBuilder html = new StringBuilder(1024);
                String numericPosition;

                if (this.showNumericPosition) {
                        numericPosition = "["+this.pos+"%]";
                }
        else {
                        numericPosition = "";
                }

                String tableWidthAttr = (this.barWidth > 0) ? "width="+this.barWidth : "";
                html.
        append("<table border=0 cellspacing cellpadding=0>").
        append(" <tbody>").
        append("  <tr>").
        append("   <td valign=middle>").
        append("    <table border=0 cellspacing=1 cellpadding=0 "+tableWidthAttr+" bgcolor="+frameColor+">").
        append("     <tbody>").
        append("      <tr><td valign=middle>").
        append("       <table border=0 cellspacing=0 cellpadding=0 width=100%>").
        append("        <tbody>").
        append("         <tr>");
        if ((this.pos+this.active)==0){
            html.
                append("          <td width=100% colspan=3 bgcolor="+this.bgColor+"><br></td>");
        }
        else if (this.active==100){
            html.
                append("          <td width=100% colspan=3 bgcolor="+this.fgColor+"><br></td>");
        }
        else if (this.pos==0) {
            html.
                append("          <td width="+this.active+"% colspan=2 bgcolor="+this.fgColor+"><br></td>").
                append("          <td width="+(100-this.active)+"% bgcolor="+this.bgColor+"><br></td>");
        }
        else if ((this.pos+this.active)==100){
            html.
                append("          <td width="+pos+"% bgcolor="+this.bgColor+"><br></td>").
                append("          <td width="+active+"% colspan=2 bgcolor="+this.fgColor+"><br></td>");
        }
        else {
            html.
                append("          <td width="+this.pos+"% bgcolor="+this.bgColor+"><br></td>").
                append("          <td width="+this.active+"% bgcolor="+this.fgColor+"><br></td>").
                append("          <td width="+(100-this.pos-this.active)+"% bgcolor="+this.bgColor+"><br></td>");
            }
        html.
        append("         </tr>").
        append("        </tbody>").
        append("       </table>").
        append("      </td></tr>").
        append("     </tbody>").
        append("    </table>").
        append("   </td>").
        append("   <td valign=middle>&nbsp;&nbsp;"+numericPosition+"</td>").
        append("  </tr>").
        append(" </tbody>").
        append("</table>");        

        return html.toString();
        }

        void setFrameColor(String htmlColor)
    {
        this.frameColor = htmlColor;
    }

        void setBgColor(String htmlColor)
    {
        this.bgColor = htmlColor;
    }

        void setFgColor(String htmlColor)
    {
        this.fgColor = htmlColor;
    }

        void showNumericPosition(boolean show)
    {
        this.showNumericPosition = show;
    }


        private void init(long _pos, long _active, long _width, long barWidth)
        {       long pos = _pos;
                long active = _active;
                long width = _width;
                boolean notZero = active > 0;

                // scale
                pos = ((pos<0?0:pos)*100)/width;
                active = ((active<0?0:active)*100)/width;

                pos = pos>100 ? 100 : pos;
                active = active>100 ? 100 : ((active==0 && notZero) ? 1 : active);
                pos = ((pos+active)>100) ? 100-active : pos;

                this.pos = pos;
                this.active = active;
//		this.width    = width;
                this.barWidth = barWidth;
        }


        private String frameColor = "#0000FF";
        private String bgColor = "#FFEEBB";
        private String fgColor = "#FFDD89";
        private boolean showNumericPosition = true;

        private long pos;
        private long active;
//	private long     width;
        private long barWidth;
}

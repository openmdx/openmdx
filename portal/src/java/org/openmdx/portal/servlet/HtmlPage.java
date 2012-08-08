/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: HtmlPage.java,v 1.44 2007/11/23 00:21:03 wfro Exp $
 * Description: HtmlPage class
 * Revision:    $Revision: 1.44 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/23 00:21:03 $
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
package org.openmdx.portal.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.portal.servlet.view.View;

public class HtmlPage {
    
    //-----------------------------------------------------------------------
    public void init(
        View view,
        HttpServletRequest request,
        Writer out
    ) {
        this.view = view;
        this.applicationContext = view.getApplicationContext();
        this.request = request;
        this.out = out;
        this.contentLength = 0;
        this.resourcePathPrefix = "./";
        this.properties.clear();
        this.nextStringBuilder = 0;
    }

    //-----------------------------------------------------------------------
    public HttpServletRequest getHttpServletRequest(
    ) {
        return this.request;
    }
    
    //-----------------------------------------------------------------------
    public void setResourcePathPrefix(
        String prefix
    ) {
        this.resourcePathPrefix = prefix;
    }
    
    //-----------------------------------------------------------------------
    public String getResourcePathPrefix(
    ) {
        return this.resourcePathPrefix;
    }
    
    //-----------------------------------------------------------------------
    public String getResourcePath(
        String relativePath
    ) {
        return this.resourcePathPrefix + relativePath;
    }
    
    //-----------------------------------------------------------------------
    private void ensureCapacity(
        int minimumCapacity
    ) {
        int newCapacity = 2 * (this.contentLength + 1);
        if(newCapacity < 0) {
            newCapacity = Integer.MAX_VALUE;
        }
        else if (minimumCapacity > newCapacity) {
            newCapacity = minimumCapacity;
        }
        char newContent[] = new char[newCapacity];
        System.arraycopy(
            this.content, 
            0, 
            newContent, 
            0, 
            this.contentLength
        );
        this.content = newContent;
        
    }
    //-----------------------------------------------------------------------
    private void append(
        CharSequence s
    ) {
        int count = s.length();
        int newLength = this.contentLength + count;        
        if(newLength > this.content.length) {
            this.ensureCapacity(newLength);
        }
        if(s instanceof String) {
            ((String)s).getChars(
                0, 
                count, 
                this.content, 
                this.contentLength
            );
        }
        else if(s instanceof StringBuilder) {
            ((StringBuilder)s).getChars(
                0, 
                count, 
                this.content, 
                this.contentLength
            );            
        }
        else {
            for(int i = 0; i < count; i++) {
                this.content[this.contentLength + i] = s.charAt(i);
            }
        }
        this.contentLength += count;
    }
    
    //-----------------------------------------------------------------------
    private void append(
        char c
    ) {
        int newLength = this.contentLength + 1;
        if(newLength > this.content.length) {
            this.ensureCapacity(newLength);
        }
        this.content[this.contentLength++] = c;        
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s
    ) throws ServiceException {
        this.append(s);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append(s11);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append('\n');
    }
        
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append(s11);
        this.append(s12);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append(s11);
        this.append(s12);
        this.append(s13);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14        
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append(s11);
        this.append(s12);
        this.append(s13);
        this.append(s14);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append(s11);
        this.append(s12);
        this.append(s13);
        this.append(s14);
        this.append(s15);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append(s11);
        this.append(s12);
        this.append(s13);
        this.append(s14);
        this.append(s15);
        this.append(s16);
        this.append(s17);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append(s11);
        this.append(s12);
        this.append(s13);
        this.append(s14);
        this.append(s15);
        this.append(s16);
        this.append(s17);
        this.append(s18);
        this.append(s19);
        this.append(s20);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void write(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23
    ) throws ServiceException {
        this.append(s0);
        this.append(s1);
        this.append(s2);
        this.append(s3);
        this.append(s4);
        this.append(s5);
        this.append(s6);
        this.append(s7);
        this.append(s8);
        this.append(s9);
        this.append(s10);
        this.append(s11);
        this.append(s12);
        this.append(s13);
        this.append(s14);
        this.append(s15);
        this.append(s16);
        this.append(s17);
        this.append(s18);
        this.append(s19);
        this.append(s20);
        this.append(s21);
        this.append(s22);
        this.append(s23);
        this.append('\n');
    }
    
    //-----------------------------------------------------------------------
    public void debug(
        String s
    ) throws ServiceException {
        if(this.isDebug) {
            this.append(s);
            this.append('\n');
        }
    }
    
    //-------------------------------------------------------------------------
    private StringBuilder getStringBuilder(
    ) {
        while(this.nextStringBuilder >= this.stringBuilders.size()) {
            this.stringBuilders.add(
                StringBuilders.newStringBuilder()
            );
        }
        StringBuilder sb = this.stringBuilders.get(this.nextStringBuilder++);
        sb.setLength(0);
        return sb;
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnMouseOver(
        CharSequence s
    ) {
        return this.getStringBuilder(
        ).append(
        	" onmouseover=\""
        ).append(
        	s
        ).append(
        	"\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnMouseOver(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onmouseover=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnMouseOver(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4
    ) {
        return this.getStringBuilder(
        ).append(
            " onmouseover=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnMouseOut(
        CharSequence s
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onmouseout=\""
    	).append(
    		s
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public String getButtonEffectHighlight(
    ) {
        return "new Effect.Highlight(this,{duration:0.3});";
    }
    
    //-------------------------------------------------------------------------
    public String getButtonEffectPulsate(
    ) {
        return "new Effect.Pulsate(this,{pulses:1,duration:0.5});";        
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnDblClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2
    ) {
        return this.getStringBuilder(
        ).append(
            " ondblclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnDblClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6
    ) {
        return this.getStringBuilder(
        ).append(
            " ondblclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		s3
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		s3
    	).append(
    		s4
    	).append(
    		s5
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		s3
    	).append(
    		s4
    	).append(
    		s5
    	).append(
    		s6
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		s3
    	).append(
    		s4
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		s3
    	).append(
    		s4
    	).append(
    		s5
    	).append(
    		s6
    	).append(
    		s7
    	).append(
    		s8
    	).append(
    		s9
    	).append(
    		s10
    	).append(
    		s11
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15
    ) {
    	return this.getStringBuilder(
    	).append(
    		" onclick=\""
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		s3
    	).append(
    		s4
    	).append(
    		s5
    	).append(
    		s6
    	).append(
    		s7
    	).append(
    		s8
    	).append(
    		s9
    	).append(
    		s10
    	).append(
    		s11
    	).append(
    		s12
    	).append(
    		s13
    	).append(
    		s14
    	).append(
    		s15
    	).append(
    		"\""
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            s23
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23,
        CharSequence s24
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            s23
        ).append(
            s24
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23,
        CharSequence s24,
        CharSequence s25,
        CharSequence s26,
        CharSequence s27,
        CharSequence s28,
        CharSequence s29
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            s23
        ).append(
            s24
        ).append(
            s25
        ).append(
            s26
        ).append(
            s27
        ).append(
            s28
        ).append(
            s29
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23,
        CharSequence s24,
        CharSequence s25,
        CharSequence s26,
        CharSequence s27,
        CharSequence s28,
        CharSequence s29,
        CharSequence s30
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            s23
        ).append(
            s24
        ).append(
            s25
        ).append(
            s26
        ).append(
            s27
        ).append(
            s28
        ).append(
            s29
        ).append(
            s30
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23,
        CharSequence s24,
        CharSequence s25,
        CharSequence s26,
        CharSequence s27,
        CharSequence s28,
        CharSequence s29,
        CharSequence s30,
        CharSequence s31
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            s23
        ).append(
            s24
        ).append(
            s25
        ).append(
            s26
        ).append(
            s27
        ).append(
            s28
        ).append(
            s29
        ).append(
            s30
        ).append(
            s31
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23,
        CharSequence s24,
        CharSequence s25,
        CharSequence s26,
        CharSequence s27,
        CharSequence s28,
        CharSequence s29,
        CharSequence s30,
        CharSequence s31,
        CharSequence s32
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            s23
        ).append(
            s24
        ).append(
            s25
        ).append(
            s26
        ).append(
            s27
        ).append(
            s28
        ).append(
            s29
        ).append(
            s30
        ).append(
            s31
        ).append(
            s32
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23,
        CharSequence s24,
        CharSequence s25,
        CharSequence s26,
        CharSequence s27,
        CharSequence s28,
        CharSequence s29,
        CharSequence s30,
        CharSequence s31,
        CharSequence s32,
        CharSequence s33
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            s23
        ).append(
            s24
        ).append(
            s25
        ).append(
            s26
        ).append(
            s27
        ).append(
            s28
        ).append(
            s29
        ).append(
            s30
        ).append(
            s31
        ).append(
            s32
        ).append(
            s33
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnClick(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13,
        CharSequence s14,
        CharSequence s15,
        CharSequence s16,
        CharSequence s17,
        CharSequence s18,
        CharSequence s19,
        CharSequence s20,
        CharSequence s21,
        CharSequence s22,
        CharSequence s23,
        CharSequence s24,
        CharSequence s25,
        CharSequence s26,
        CharSequence s27,
        CharSequence s28,
        CharSequence s29,
        CharSequence s30,
        CharSequence s31,
        CharSequence s32,
        CharSequence s33,
        CharSequence s34
    ) {
        return this.getStringBuilder(
        ).append(
            " onclick=\""
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            s14
        ).append(
            s15
        ).append(
            s16
        ).append(
            s17
        ).append(
            s18
        ).append(
            s19
        ).append(
            s20
        ).append(
            s21
        ).append(
            s22
        ).append(
            s23
        ).append(
            s24
        ).append(
            s25
        ).append(
            s26
        ).append(
            s27
        ).append(
            s28
        ).append(
            s29
        ).append(
            s30
        ).append(
            s31
        ).append(
            s32
        ).append(
            s33
        ).append(
            s34
        ).append(
            "\""
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getEvalHRef(
        Action action,
        boolean includeRequestId
    ) {
        return 
            "'" + this.getResourcePath("") + "'" +
            "+" +
            this.view.getEvalHRef(action, includeRequestId);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getEvalHRef(
        Action action
    ) {
        return 
            "'" + this.getResourcePath("") + "'" +
            "+" +
            this.view.getEvalHRef(action);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getEncodedHRef(
        Action action
    ) {
        return
            this.getResourcePath("") +
            this.view.getEncodedHRef(action);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getEncodedHRef(
        Action action,
        boolean includeRequestId
    ) {
        return 
            this.getResourcePath("") +
            this.view.getEncodedHRef(action, includeRequestId);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s
    ) {
    	return StringBuilders.newStringBuilder(
    	).append(
    		"<img "
    	).append(
    		s
    	).append(
    		" />"
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            " />"
        );
    }
        
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2
    ) {
    	return this.getStringBuilder(
    	).append(
    		"<img "
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		" />"
    	);
    }
        
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3
    ) {
    	return this.getStringBuilder(
    	).append(
    		"<img "
    	).append(
    		s0
    	).append(
    		s1
    	).append(
    		s2
    	).append(
    		s3
    	).append(
    		" />"
    	);
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4            
        ).append(
            " />"
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            " />"
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            " />"
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            " />"
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            " />"
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            " />"
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            " />"
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            " />"
        );
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getImg(
        CharSequence s0,
        CharSequence s1,
        CharSequence s2,
        CharSequence s3,
        CharSequence s4,
        CharSequence s5,
        CharSequence s6,
        CharSequence s7,
        CharSequence s8,
        CharSequence s9,
        CharSequence s10,
        CharSequence s11,
        CharSequence s12,
        CharSequence s13
    ) {
        return this.getStringBuilder(
        ).append(
            "<img "
        ).append(
            s0
        ).append(
            s1
        ).append(
            s2
        ).append(
            s3
        ).append(
            s4
        ).append(
            s5
        ).append(
            s6
        ).append(
            s7
        ).append(
            s8
        ).append(
            s9
        ).append(
            s10
        ).append(
            s11
        ).append(
            s12
        ).append(
            s13
        ).append(
            " />"
        );
    }
    
    //-----------------------------------------------------------------------
    public void writeEventHandlers(
        String indent,
        List eventHandlers
    ) throws ServiceException {
        for(Iterator i = eventHandlers.iterator(); i.hasNext(); ) {
            String eventHandler = (String)i.next();
            int pos = eventHandler.indexOf(":");
            if(pos > 0) {
                this.write(indent, eventHandler.substring(0, pos), "=\"javascript:", eventHandler.substring(pos+1), "();\"");
            }
        }        
    }
    
    //-----------------------------------------------------------------------
    public void flush(
    ) throws ServiceException {
        try {
            this.out.write(this.content, 0, this.contentLength);
            this.contentLength = 0;
        }
        catch(IOException e) {
            throw new ServiceException(e);
        }
    }
        
    //-----------------------------------------------------------------------
    public void close(
        boolean closeWriter
    ) throws ServiceException {
        try {
            this.flush();
            if(closeWriter) {
                this.out.close();
            }
            HtmlPageFactory.closePage(this);
        }
        catch(IOException e) {
            throw new ServiceException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    public Object getProperty(
        String name
    ) {
        return this.properties.get(name);
    }

    //-----------------------------------------------------------------------
    public View getView(
    ) {
        return this.view;
    }
    
    //-----------------------------------------------------------------------
    public ApplicationContext getApplicationContext(
    ) {
        return this.applicationContext;
    }
    
    //-----------------------------------------------------------------------
    public void setProperty(
        String name,
        Object value
    ) {
        this.properties.put(
            name,
            value
        );
    }

    //-----------------------------------------------------------------------
    public void setIsDebug(
        boolean newValue
    ) {
        this.isDebug = newValue;
    }
    
    //-----------------------------------------------------------------------
    /*
     * TODO should be configurable
     */
    public String getImgType(
    ) {
        return WebKeys.ICON_TYPE;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    public static final String PROPERTY_POPUP_IMAGES = "popupImages";
    public static final String PROPERTY_CALENDAR_IDS = "calendarIds";
    public static final String PROPERTY_GROUP_TAB_ID = "groupTabId";
    public static final String PROPERTY_MODIFIERS = "modifiers";
    public static final String PROPERTY_CHART_ID = "chartId";
    public static final String PROPERTY_N_ACTIVE_TAB = "nActiveTab";
    public static final String PROPERTY_FORM_ID = "formId";
    public static final String PROPERTY_FIELD_GROUP_ID = "fieldGroupId";    

    private View view;
    private ApplicationContext applicationContext;
    private char[] content = new char[1024];
    private int contentLength = 0;
    private HttpServletRequest request;
    private Writer out;
    private boolean isDebug = false;
    private String resourcePathPrefix;
    private final Map properties = new HashMap();
    private final List<StringBuilder> stringBuilders = new ArrayList<StringBuilder>();
    private int nextStringBuilder = 0;
}  

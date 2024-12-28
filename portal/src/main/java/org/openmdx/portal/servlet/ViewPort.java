/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: ViewPort class
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
package org.openmdx.portal.servlet;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import #if JAVA_8 javax.servlet.http.HttpServletRequest #else jakarta.servlet.http.HttpServletRequest #endif;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.component.View;

public class ViewPort {
    
    //-----------------------------------------------------------------------
	public enum Type {
	    STANDARD, // standard HTML devices 
	    EMBEDDED // view is embedded
	}
	
    //-----------------------------------------------------------------------
    public void init(
        View view,
        HttpServletRequest request,
        Writer out
    ) {
        this.view = view;
        this.app = view.getApplicationContext();
        this.request = request;
        this.out = out;
        this.contentLength = 0;
        this.resourcePathPrefix = "./";
        this.properties.clear();
        this.nextStringBuilder = 0;
        this.type = request.getParameter(Action.PARAMETER_VIEW_PORT) == null ?
    		this.app.getCurrentViewPortType() : 
    		ViewPort.Type.valueOf(request.getParameter(Action.PARAMETER_VIEW_PORT));
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
        return this.view.getResourcePathPrefix() == null ?
        	this.resourcePathPrefix :
        		this.view.getResourcePathPrefix();
    }
    
    //-----------------------------------------------------------------------
    public String getResourcePath(
        String relativePath
    ) {
        return this.getResourcePathPrefix() + relativePath;
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
        if(s == null) { 
            return;
        }
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
        CharSequence... strings
    ) throws ServiceException {
        for(CharSequence s: strings) {
            this.append(s);
        }
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
                new StringBuilder()
            );
        }
        StringBuilder sb = this.stringBuilders.get(this.nextStringBuilder++);
        sb.setLength(0);
        return sb;
    }
    
    //-------------------------------------------------------------------------
    public CharSequence getOnMouseOver(
        CharSequence... strings
    ) {
        StringBuilder sb = this.getStringBuilder(
        ).append(
            " onmouseover=\""
        );
        for(CharSequence s: strings) {
            sb.append(s);
        }
        sb.append("\"");
        return sb;
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
    public CharSequence getOnDblClick(
        CharSequence... strings
    ) {
        StringBuilder sb = this.getStringBuilder(
        ).append(
            " ondblclick=\""
        );
        for(CharSequence s: strings) {
            sb.append(s);
        }
        sb.append("\"");
        return sb;
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
        CharSequence... strings
    ) {
    	StringBuilder sb = this.getStringBuilder(
    	).append(
    		"<img "
    	);
    	for(CharSequence s: strings) {
    	    sb.append(s);
    	}
    	sb.append(" />");
    	return sb;
    }
        
    //-----------------------------------------------------------------------
    public void writeEventHandlers(
        String indent,
        List<String> eventHandlers
    ) throws ServiceException {
        for(Iterator<String> i = eventHandlers.iterator(); i.hasNext(); ) {
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
            ViewPortFactory.closePage(this);
            this.view = null;
            this.app = null;
            this.out = null;
            this.request = null;
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
    public Type getViewPortType(
    ) {
    	return this.type;
    }

    //-----------------------------------------------------------------------
    public void setViewPortType(
    	Type type
    ) {
    	this.type = type;
    }
    
    //-----------------------------------------------------------------------
    public ApplicationContext getApplicationContext(
    ) {
        return this.app;
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
    public static final String PROPERTY_N_ACTIVE_TAB = "nActiveTab";
    public static final String PROPERTY_FIELD_GROUP_ID = "fieldGroupId";    

    private View view;
    private ApplicationContext app;
    private char[] content = new char[1024];
    private int contentLength = 0;
    private HttpServletRequest request;
    private Writer out;
    private boolean isDebug = false;
    private String resourcePathPrefix;
    private final Map<String,Object> properties = new HashMap<String,Object>();
    private final List<StringBuilder> stringBuilders = new ArrayList<StringBuilder>();
    private int nextStringBuilder = 0;
    private Type type;
    
}  

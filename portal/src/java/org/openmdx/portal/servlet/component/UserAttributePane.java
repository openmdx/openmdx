/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Description: UserAttributePane
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
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
package org.openmdx.portal.servlet.component;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.portal.servlet.ViewPort;
import org.openmdx.portal.servlet.control.UserAttributePaneControl;

/**
 * UserAttributePane
 *
 */
public class UserAttributePane extends AttributePane implements Serializable {
    
	/**
     * Constructor.
     * 
     * @param control
     * @param view
     * @param object
     */
    public UserAttributePane(
        UserAttributePaneControl control,
        ObjectView view,
        Object object
    ) {
        super(
            control,
            view,
            object
        );
    }

    /**
     * Get control casted to UserAttributePaneControl.
     * 
     * @return
     */
    protected UserAttributePaneControl getUserAttributePaneControl(
    ) {
    	return (UserAttributePaneControl)this.control;
    }
    
	/* (non-Javadoc)
	 * @see org.openmdx.portal.servlet.view.Component#getChildren(java.lang.Class)
	 */
	@Override
	public <T extends Component> List<T> getChildren(
		Class<T> type
	) {
		return Collections.emptyList();
	}

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.view.Canvas#refresh(boolean)
     */
    @Override
    public void refresh(
        boolean refreshData
    ) throws ServiceException {
    }

    /* (non-Javadoc)
     * @see org.openmdx.portal.servlet.component.Component#paint(org.openmdx.portal.servlet.ViewPort, java.lang.String, boolean)
     */
    @Override
    public void paint(
		ViewPort p,
		String frame,
		boolean forEditing
	) throws ServiceException {
    	ObjectView view = (ObjectView)p.getView();
    	UserAttributePaneControl control = this.getUserAttributePaneControl();
    	int paneIndex = this.getUserAttributePaneControl().getPaneIndex();
		String paneId = view.getContainerElementId() == null 
			? Integer.toString(paneIndex)
			: view.getContainerElementId() + "-" + Integer.toString(paneIndex);
    	p.write("<div id=\"userPane", paneId, "\">");
    	p.write("</div>");
    	p.write("<script>");    	
    	try {
    		Path objectIdentity = null;
    		if(view instanceof EditObjectView) {
    			EditObjectView editView = (EditObjectView)view;
    			if(editView.getParentObject() == null) {
    				objectIdentity = editView.getObject().refGetPath();
    			} else {
    				objectIdentity = editView.getParentObject().refGetPath();
    			}
    		} else {
    			objectIdentity = view.getObject().refGetPath();
    		}
    		p.write("  jQuery.ajax({type: 'get', url: '", control.getId(), "?xri=" + URLEncoder.encode(objectIdentity.toXRI(), "UTF-8"), "&requestId=", view.getRequestId(), "&paneId=", paneId, "&forEditing=", Boolean.toString(forEditing), "', dataType: 'html', success: function(data){$('userPane", paneId, "').innerHTML=data;evalScripts(data);}});");
    	} catch(Exception ignore) {
    		new ServiceException(ignore).log();
    	}
		p.write("</script>");
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
	private static final long serialVersionUID = 1635676743958964357L;

}

/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: PagePrologControl
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of CRIXP Corp. nor the names of the contributors
 * to openCRX may be used to endorse or promote products derived
 * from this software without specific prior written permission
 * 
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.portal.servlet.ViewPort;

/**
 * PagePrologControl
 *
 */
public class PagePrologControl extends Control implements Serializable {
  
    /**
     * Constructor.
     * 
     * @param id
     * @param locale
     * @param localeAsIndex
     */
    public PagePrologControl(
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
        if(FRAME_PRE_PROLOG.equals(frame)) {
            p.setProperty(
                ViewPort.PROPERTY_POPUP_IMAGES,
                new HashMap<String,String>()
            );
            p.setProperty(
                ViewPort.PROPERTY_CALENDAR_IDS,
                new ArrayList<String>()
            );        
        } else if(FRAME_POST_PROLOG.equals(frame)) {
        	// no-op
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

	//-------------------------------------------------------------------------
    // Members
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = -1459865229401460381L;
    
    public static final String FRAME_PRE_PROLOG = "preProlog";
    public static final String FRAME_POST_PROLOG = "postProlog";
        
}

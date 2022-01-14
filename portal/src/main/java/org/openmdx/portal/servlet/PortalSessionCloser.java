/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: SessionCloser 
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

package org.openmdx.portal.servlet;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * SessionCloser
 *
 */
public class PortalSessionCloser implements HttpSessionListener {

	/* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionCreated(
    	HttpSessionEvent event
    ) {
    }

	/* (non-Javadoc)
     * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
     */
    @Override
    public void sessionDestroyed(
    	HttpSessionEvent event
    ) {
    	HttpSession session = event.getSession();
    	ApplicationContext application = (ApplicationContext)session.getAttribute(WebKeys.APPLICATION_KEY);
    	if(application != null) {
    		application.close();
    	}
    	session.setAttribute(
    		WebKeys.APPLICATION_KEY,
    		null
    	);
        ViewsCache showViewsCache = (ViewsCache)session.getAttribute(WebKeys.VIEW_CACHE_KEY_SHOW);
        if(showViewsCache != null) {
        	showViewsCache.clearViews(
        		session, 
        		System.currentTimeMillis()
        	);
        	session.setAttribute(
        		WebKeys.VIEW_CACHE_KEY_SHOW,
        		null
        	);
        }
        ViewsCache editViewsCache = (ViewsCache)session.getAttribute(WebKeys.VIEW_CACHE_KEY_EDIT);        
        if(editViewsCache != null) {
        	editViewsCache.clearViews(
        		session, 
        		System.currentTimeMillis()
        	);
        	session.setAttribute(
        		WebKeys.VIEW_CACHE_KEY_EDIT,
        		null
        	);
        }    	
    }

}

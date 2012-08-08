/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.opencrx.org/
 * Name:        $Id: EventHandlerHelper.java,v 1.8 2008/05/05 23:06:13 wfro Exp $
 * Description: EventHandlerHelper
 * Revision:    $Revision: 1.8 $
 * Owner:       CRIXP AG, Switzerland, http://www.crixp.com
 * Date:        $Date: 2008/05/05 23:06:13 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 20042-2008, CRIXP Corp., Switzerland
 * All rights reserved.
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
 * This product includes software developed by contributors to
 * openMDX (http://www.openmdx.org/)
 */
package org.openmdx.portal.servlet.eventhandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jmi.reflect.RefPackage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.portal.servlet.ViewsCache;
import org.openmdx.portal.servlet.view.ObjectView;

public class EventHandlerHelper {
    
    //-----------------------------------------------------------------------    
    private EventHandlerHelper() {}
    
    //-----------------------------------------------------------------------    
    public static PrintWriter getWriter(
        HttpServletRequest request,
        HttpServletResponse response        
    ) throws IOException {
        OutputStream os = response.getOutputStream(); 
        response.setContentType("text/html");
        // Do not cache replies
        response.addDateHeader("Expires", -1);
        return new PrintWriter(os);
    }
    
    //-------------------------------------------------------------------------
    public static void notifyObjectModified(
        ViewsCache notifyViews,
        RefObject_1_0 modifiedObject
    ) {
        // Update current views. (Re)load the created / edited object in 
        // object managers of cached views
        List<RefPackage> processedPackages = new ArrayList<RefPackage>();
        processedPackages.add(
            modifiedObject.refOutermostPackage()
        );
        for(
            Iterator i = notifyViews.getViews().values().iterator(); 
            i.hasNext(); 
        ) {
            ObjectView view = (ObjectView)i.next();
            RefPackage_1_0 cachedViewPkg = (RefPackage_1_0)view.getRefObject().refOutermostPackage();
            if(!processedPackages.contains(cachedViewPkg)) {
                RefObject_1_0 toBeRefreshed = (RefObject_1_0)cachedViewPkg.refObject(modifiedObject.refMofId());
                toBeRefreshed.refRefresh();
                processedPackages.add(cachedViewPkg);
            }
        }
    }
           
}

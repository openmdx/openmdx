/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: HtmlPageFactory.java,v 1.1 2007/11/23 00:21:03 wfro Exp $
 * Description: HtmlPageFactory 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/11/23 00:21:03 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.openmdx.portal.servlet;

import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.openmdx.portal.servlet.view.View;

/**
 * HtmlPageFactory
 *
 */
public class HtmlPageFactory {
    
    public static HtmlPage openPage(
        View view,
        HttpServletRequest request,
        Writer out
    ) {
        synchronized(freePool) {
            HtmlPage p = null;
            if(!freePool.isEmpty()) {
                p = freePool.iterator().next();
                freePool.remove(p);
            }
            else {
                p = new HtmlPage();
            }
            p.init(
                view, 
                request, 
                out
            );
            return p;
        }
    }

    public static void closePage(
        HtmlPage p
    ) {
        synchronized(freePool) {
            // Do not pool more than 100 pages. In case of more pages
            // let the gc do the work
            if(freePool.size() < 100) {
                freePool.add(p);
            }
        }
    }
    static final Set<HtmlPage> freePool = new HashSet<HtmlPage>();
    
}
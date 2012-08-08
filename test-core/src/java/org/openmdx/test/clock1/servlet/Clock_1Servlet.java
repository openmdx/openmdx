/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Clock_1Servlet.java,v 1.5 2009/04/08 14:51:58 hburger Exp $
 * Description: Clock_1Servlet 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/08 14:51:58 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2007, OMEX AG, Switzerland
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
package org.openmdx.test.clock1.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.Context;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.jmi1.Authority;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.test.clock1.jmi1.Segment;
import org.openmdx.test.clock1.jmi1.Time;

/**
 * Clock_1Servlet
 */
public class Clock_1Servlet
    extends HttpServlet
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -8959914415390068536L;

    /**
     * 
     */
    private PersistenceManagerFactory persistenceManagerFactory;

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    public void init(
    ) throws ServletException {
        this.persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(
            "java:comp/env/persistence/clock",
            (Context)null
        );
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#destroy()
     */
    public void destroy() {
        this.persistenceManagerFactory.close();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        PrintWriter out = new PrintWriter(response.getOutputStream());
        try {
            Model_1Factory.getModel();
            PersistenceManager persistenceManager = this.persistenceManagerFactory.getPersistenceManager();
            Authority authority = (Authority) persistenceManager.getObjectById(
                Authority.class,
                "xri://@openmdx*org.openmdx.test.clock1"
            );
            Segment segment = (Segment) authority.getProvider(false, "Java").getSegment(false, "Remote");
            Time time = segment.currentDateAndTime();
            out.print(
                "<html>" +
                    "<head>" +
                        "<title>Clock</title>" +
                    "</head>" +
                    "<body>" +
                        "<h1>openMDX/2 Clock</h1>" +
                        "<b>" + time.getUtc() + "</b>" +
                    "</body>" +
                "</html>"
            );
            persistenceManager.close();
        } catch (Exception e) {
            out.println("<pre>");
            e.printStackTrace(out);
            out.println("</pre>");
        }
        out.flush();
    }



}

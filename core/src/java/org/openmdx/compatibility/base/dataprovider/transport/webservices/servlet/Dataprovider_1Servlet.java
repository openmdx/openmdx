/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Servlet.java,v 1.33 2008/09/10 08:55:24 hburger Exp $
 * Description: Dataprovider_1Servlet
 * Revision:    $Revision: 1.33 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:24 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.compatibility.base.dataprovider.transport.webservices.servlet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkReply;
import org.openmdx.compatibility.base.dataprovider.transport.cci.Dataprovider_1_1Connection;
import org.openmdx.compatibility.base.dataprovider.transport.webservices.ReplyMapper;
import org.openmdx.compatibility.base.dataprovider.transport.webservices.RequestParser;
import org.openmdx.compatibility.base.exception.StackedException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Servlet for SPICE webservices transport. 
 * <p>
 * E.g. the servlet can be installed under Tomcat as webapp as follows:
 * <p>
 * <ul>
 *   <li>create a directory TOMCAT_HOME/webapps/dataproviders.</li>
 *   <li>create a folder ./WEB-INF.</li>
 *   <li>create a web.xml as shown below and store it in ./WEB-INF.</li>
 *   <li>create a folder ./WEB-INF/lib.</li>
 *   <li>copy spice-kernel.jar and all required thirdparty jars in this folder.</li>
 *   <li>copy spice-resource-handler.jar in folder TOMCAT_HOME/common/lib and
 *       add it to the classpath in ./bin/setclasspath.bat.</li>
 * </ul>
 * <p>
 * Below is a sample of a web.xml:
 * <p>
 * <pre>
 * <?xml version="1.0" encoding="ISO-8859-1"?>
 * <!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN" "http://java.sun.com/j2ee/dtds/web-app_2.3.dtd">
 * <web-app>
 *   <servlet>
 *     <servlet-name>Dataproviders</servlet-name>
 *     <servlet-class>org.openmdx.compatibility.dataprovider.transport.webservices.servlet.Dataprovider_1Servlet</servlet-class>
 *       <init-param>
 *           <param-name>registrationId</param-name>
 *           <param-value>ch/omex/test/managing/explorer</param-value>
 *           <description>RegistrationId or JNDI-Name for dataprovider lookup</description>
 *       </init-param>
 *   </servlet>
 *   <servlet-mapping>
 *     <servlet-name>Dataproviders</servlet-name>
 *     <url-pattern>/junits</url-pattern>
 *   </servlet-mapping>
 *   <security-constraint>
 *     <web-resource-collection>
 *       <web-resource-name>dataproviders</web-resource-name>
 *       <url-pattern>/junits</url-pattern>
 *     </web-resource-collection>
 *     <auth-constraint>
 *        <role-name>manager</role-name>
 *     </auth-constraint>
 *   </security-constraint>  
 *   <login-config>
 *     <auth-method>BASIC</auth-method>
 *   </login-config>
 * </web-app>
 * </pre>
 *
 * @author wfro
 */
public class Dataprovider_1Servlet
extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 3256446889107994931L;
//  -------------------------------------------------------------------------
    public void init(
    ) throws ServletException {
        super.init();
        registrationId = getInitParameter("registrationId");
        if(registrationId == null) {
            ServiceException se = new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Servlet-InitParameter registrationId not set",
                new StackedException.Parameter("registrationId", "null")
            );
            SysLog.error(
                se.getMessage(),
                se.getExceptionStack()
            );
            throw new ServletException(se);
        }

        // get connection
        try {
            this.delegation = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                new InitialContext().lookup(registrationId)
            );
        }
        catch(ServiceException e) {
            e.log();
        }
        catch(Exception e) {
            new ServiceException(e).log();
        }
    }

    //-----------------------------------------------------------------------
    public void doPost(
        HttpServletRequest req,
        HttpServletResponse res
    ) throws IOException {
        SysLog.detail("Request size", "" + req.getContentLength());
        StringBuilder result = new StringBuilder(
            2048
        ).append(
            "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>"
        ).append(
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
        ).append(
            "xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" "
        ).append(
            "xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">"
        ).append(
            "<SOAP-ENV:Body>"
        );

        try {
            RequestParser parser = new RequestParser(
                new InputStreamReader(req.getInputStream(), "ISO-8859-1")
            );
            if(delegation != null) {
                UnitOfWorkReply[] replies = delegation.process(
                    parser.getHeader(),
                    parser.getUnitOfWorkRequests()
                );
                res.setContentType("text/xml; charset=ISO-8859-1");
                Writer response = null;
                ServletOutputStream out = res.getOutputStream();
                String accEncHeadField = req.getHeader("Accept-Encoding");
                // check whether the client does accept gzipped reply encoding
                if ((accEncHeadField != null) && (accEncHeadField.indexOf("gzip") != -1)) {
                    res.setHeader("Content-Encoding", "gzip");
                    GZIPOutputStream gzipOut = new GZIPOutputStream(out);
                    response = new OutputStreamWriter(gzipOut,"ISO-8859-1");
                }
                else {
                    response = new BufferedWriter(new OutputStreamWriter(out,"ISO-8859-1"));
                }
                ReplyMapper mapper = new ReplyMapper(response);
                mapper.mapProlog(result.toString());
                mapper.mapUnitOfWorkReplies(replies);
                mapper.mapEpilog("</SOAP-ENV:Body></SOAP-ENV:Envelope>");
                response.flush();
                response.close();
                res.setContentType("text/xml; charset=ISO-8859-1");
            }
        }
        catch(ServiceException e) {
            e.log();
        }
        if (SysLog.isTraceOn()) {
            SysLog.trace("Response", result.toString());
        }
    }

    //-----------------------------------------------------------------------
    // Variables
    //-----------------------------------------------------------------------
    private String registrationId = null;
    private Dataprovider_1_1Connection delegation = null;
}

//--- End of File -----------------------------------------------------------

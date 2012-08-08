/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Dataprovider_1Servlet.java,v 1.11 2007/10/10 16:05:55 hburger Exp $
 * Description: Dataprovider Servlet
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:55 $
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
package org.openmdx.compatibility.application.dataprovider.transport.http.to.ejb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ExecutionContextModifier_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.compatibility.base.dataprovider.spi.ExecutionContextModifier_1;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.StringBuilders;

/**
 * Dataprovider_1 Servlet
 */
public class Dataprovider_1Servlet extends HttpServlet {

    /**
     * <code>serialVersionUID</code> to avoiud Eclipse warning.
     */
    private static final long serialVersionUID = 3834868070660780594L;

    /**
     * The dataprovider to be accessed
     */
    protected Dataprovider_1_0 dataprovider;

    /**
     * The servlet's execution context modifier
     */
    protected ExecutionContextModifier_1_0 executionContextModifier;

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#init()
     */
    public void init(
    ) throws ServletException {
        Context context= null;
        try {
            context = new InitialContext();
            this.dataprovider = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
                    context.lookup("java:comp/env/ejb/dataprovider")
            );
        } catch (Exception exception) {
            throw (UnavailableException) Throwables.initCause(
                new UnavailableException(
                    "Dataprovider acquisition failed"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("info", "$Id: Dataprovider_1Servlet.java,v 1.11 2007/10/10 16:05:55 hburger Exp $"),
                    new BasicException.Parameter("dataprovider", "java:comp/env/ejb/dataprovider")
                },
                null
           );
        } finally {
            if(context != null) try {
                context.close();
            } catch (NamingException ignored) {
                // ignore
            }
        }
        this.executionContextModifier = getExecutionContextModifier();
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(
        javax.servlet.ServletConfig configuration
    ) throws ServletException {
        try {
            super.init(configuration);
        } catch (ServletException exception) {
            CharSequence message = StringBuilders.newStringBuilder("Servlet ");
            if(exception instanceof UnavailableException){
                StringBuilders.asStringBuilder(
                    message
                ).append(
                    ((UnavailableException)exception).isPermanent() ? "permanently" : "temporarely"
                ).append(
                    " unavailable"
                );
            } else {
                StringBuilders.asStringBuilder(
                    message
                ).append(
                    "initialization failed"
                );
            }
            if(exception instanceof BasicException.Wrapper) {
                log(
                    StringBuilders.asStringBuilder(
                        message
                    ).append(
                        ": "
                    ).append(
                        exception
                    ).toString()
                );
            } else {
                log(message.toString(), exception);
            }
            throw exception;
        }
    }

    /**
     * Creates the Servlet's Execution Context Modifier
     * <p>
     * This method may be overridden by a sub-class.
     * 
     * @return the Servlet's Execution Context Modifier
     */
    protected ExecutionContextModifier_1_0 getExecutionContextModifier(
    ) throws ServletException {
        String security = this.getInitParameter("security");
        try {
            return ExecutionContextModifier_1.newInstance(security);
        } catch (ServiceException exception) {
            throw (UnavailableException) Throwables.initCause(
                new UnavailableException(
                        "Execution context modifier acquistion failed"
                ),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                new BasicException.Parameter[]{
                     new BasicException.Parameter("id", "$Id: Dataprovider_1Servlet.java,v 1.11 2007/10/10 16:05:55 hburger Exp $"),
                     new BasicException.Parameter("security", security)
                },
                null
           );
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doHead(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        requestNotSupported("HEAD", request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        requestNotSupported("GET", request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws ServletException, IOException {
        if(PROCESS_PATH.equals(request.getPathInfo())) try {
            ObjectInputStream input = new ObjectInputStream(request.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
            ServiceHeader header = (ServiceHeader) input.readObject();
            if(this.executionContextModifier != null) this.executionContextModifier.apply(
                request,
                header
            );
            output.writeObject(
                this.dataprovider.process(
                    header,
                    (UnitOfWorkRequest[]) input.readObject()
                )
            );
        } catch (Exception exception) {
            SysLog.info(
                "POST /process request failed",
                exception
            );
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "POST /process: " + exception.getMessage()
            );
        } else {
            requestNotSupported("POST", request, response);
        }
    }

    /**
     * Signal unsupported request exception
     * 
     * @param requestType
     * @param request
     * @param response
     * @throws IOException
     */
    private void requestNotSupported(
        String requestType,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException{
        response.sendError(
            HttpServletResponse.SC_METHOD_NOT_ALLOWED,
            requestType + " not supported for " + request.getPathInfo()
        );
    }

    /**
     * The process action path
     */
    private static final String PROCESS_PATH = "/process";

}

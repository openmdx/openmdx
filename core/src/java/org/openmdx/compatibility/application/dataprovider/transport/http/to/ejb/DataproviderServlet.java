/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderServlet.java,v 1.1 2005/02/06 20:15:44 hburger Exp $
 * Description: Dataprovider Servlet
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/06 20:15:44 $
 * ====================================================================
 *
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
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
 */
package org.openmdx.compatibility.application.dataprovider.transport.http.to.ejb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmdx.compatibility.application.dataprovider.transport.ejb.cci.Dataprovider_1ConnectionFactoryImpl;
import org.openmdx.compatibility.base.dataprovider.cci.Dataprovider_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.ServiceHeader;
import org.openmdx.compatibility.base.dataprovider.cci.UnitOfWorkRequest;
import org.openmdx.kernel.log.SysLog;

/**
 * Dataprovider Servlet
 */
public class DataproviderServlet extends HttpServlet {

    /**
     * The dataprovider to be accessed
     */
    Dataprovider_1_0 dataprovider;
    
    /* (non-Javadoc)
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(
        ServletConfig configuration
    ) throws ServletException {
        super.init(configuration);
        try {
            this.dataprovider = Dataprovider_1ConnectionFactoryImpl.createGenericConnection(
	            new InitialContext(
                ).lookup("java:comp/env/ejb/dataprovider")
            );
        } catch (Exception exception) {
            throw new ServletException("Dataprovider acquisition failed", exception);
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
            output.writeObject(
                this.dataprovider.process(
                    (ServiceHeader) input.readObject(),
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

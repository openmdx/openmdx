/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestRunnerServlet.java,v 1.2 2005/02/21 12:18:36 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/21 12:18:36 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
package org.openmdx.application.servlet.junit;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet based tool to run tests.
 *
 * TestRunner expects the name of a TestCase class as a request
 * parameter. If this class defines a static <code>suite</code>
 * method it will be invoked and the returned test is run.
 * Otherwise all the methods starting with "test" having
 * no arguments are run.
 * <p>
 * TestRunner displays a trace of the test results, followed by a
 * summary at the end.
 * <p>
 * @author Michael T. Nygard
 * @author Tracie Karsjens
 * @author Dave Robinson
 */
public class TestRunnerServlet extends HttpServlet {

  /**
     * 
     */
    private static final long serialVersionUID = 3256437019172679729L;
/**
   * The parameter name for the class to test.  The value of
   * this parameter must be a fully-qualified class name.
   */
  public static final String P_CLASS_NAME = "classname";

  /**
   * Invoked when the servlet is called with an HTTP post method.  Only one
   * parameter is required: "classname".  It can occur multiple times, in which case,
   * each class is used one at a time.
   *
   * @param req The HTTP request object.
   * @param resp The HTTP response object.
   * @exception IOException if an IO failure occurs at the lower levels.
   */
  protected void doGet(
    HttpServletRequest req, 
    HttpServletResponse resp
  ) throws IOException {
    String[] classesUnderTest = req.getParameterValues(P_CLASS_NAME);
    String url = getUrl(req);

    resp.setContentType("text/html");

    // turn of caching 
    // (see http://developer.java.sun.com/developer/onlineTraining/Programming/JDCBook/aucserv.html)
    if(req.getProtocol().compareTo ("HTTP/1.0") == 0) {
      resp.setHeader ("Pragma", "no-cache");
    } 
    else if(req.getProtocol().compareTo ("HTTP/1.1") == 0) {
      resp.setHeader ("Cache-Control", "no-cache");
    }
    resp.setDateHeader ("Expires", 0);
    
    PrintWriter out = resp.getWriter();

    if(classesUnderTest == null || classesUnderTest.length == 0) {
      printNoParameterResponse(out);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    TestManager tester = TestManager.getInstance();
    tester.addHtmlResults(classesUnderTest, url, out);
    out.flush();
    out.close();
  }

  /**
   * Adds Response when no parameters are given to the provided PrintWriter.
   *
   * @param out The PrintWriter to add the response to.
   */
  protected void printNoParameterResponse(
    PrintWriter out
  ) throws IOException {
    out.println("<html><head><title>Bad parameter(s)</title></head>");
    out.println("<body>");
    out.println("<h1>Bad parameter(s)</h1>");
    out.println("<hr><h2>Allowed Parameters</h2>");
    out.println("<table><th>Parameter</th><th>Value</th>");
    out.println("<tr><td valign=\"top\"><code>"+P_CLASS_NAME+"</code></td><td>The fully-qualified name of the class to test.");
    out.println("If this class has a method like <code>public static TestSuite suite()</code>, then");
    out.println("the results of that method will be run.  Otherwise, all public void no-argument");
    out.println("methods in the class will be run as tests.</td></tr></table>");
    out.println("</body></html>");
  }

  /**
   * Utility method that reconstructs a URL from an HttpServletRequest.
   * 
   * @deprecated
   */
  private String getUrl(
    HttpServletRequest req
  ) {
    return javax.servlet.http.HttpUtils.getRequestURL(req).toString() + "?" + req.getQueryString();
  }

}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestManager.java,v 1.3 2007/10/10 16:05:49 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:49 $
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
import java.util.HashMap;

import org.openmdx.kernel.text.StringBuilders;

/**
 * A class that runs tests and provides output to an PrintWriter object.
 *
 * Some of this code is duplicated from junit.textui.TestRunner,
 * but this file is thread-safe, whereas the textui TestRunner
 * is not.
 * <p>
 * TODO: Currently, this file never clears entries in it's hashtable. This is OK
 * for now, but needs to be fixed when we start dynamically loading test files.
 * <p>
 * @author Michael T. Nygard
 * @author Tracie Karsjens
 * @author Dave Robinson
 */
public class TestManager {

  /**
   * HashMap containing access to testRunner threads.
   */
  private HashMap tests = new HashMap();
  private static TestManager instance;

  private TestManager(
  ) {
      super();
  }

  public static TestManager getInstance() {
    if (instance == null) {
      instance = new TestManager();
    }
    return instance;
  }

  /**
   * Adds test results to the provided PrintWriter.
   */
  public void addHtmlResults(String[] classesUnderTest, String url, PrintWriter out) throws IOException{
    TestRunner thisThread;
    String key = getKey(classesUnderTest);
    if (tests.containsKey(key)) {
      thisThread = (TestRunner) tests.get(key);
    } else {
      thisThread = new TestRunner(classesUnderTest);
      tests.put(key, thisThread);
      thisThread.start();
    }
    printHeader(out, classesUnderTest, url, thisThread.isAlive());
    out.println(thisThread.getHtmlResults());
    endHtml(out);
  }

	/**
   * The header always prints, regardless if test is in progress or finished.
   */
  protected void printHeader(PrintWriter pw, String[] testClassNames, String url, boolean threadAlive) {
    String header = "";
    String verbForm = "";

    if (threadAlive) {
      pw.println("<meta http-equiv=Refresh content=\"5;url=" + url + "\">");
      header = "Test Results (In Progress)";
      verbForm = "are currently being";
    } else {
      header = "Final Test Results";
      verbForm = "have been";
    }

		pw.println("<html><head>");
 		pw.println("<title> " + header + " </title>");
  	pw.println("</head><body>");

		pw.println("<h1> " + header + " </h1> ");
		pw.println("<p> The following units " + verbForm + " tested:");
		pw.println("</p>");
		pw.println("<ul>");
		for (int i=0; i<testClassNames.length; i++)
			pw.println("  <li> <tt>" + testClassNames[i] + "</tt> </li>");
		pw.println("</ul>");
	}

  protected void endHtml(PrintWriter out) {
    out.println("</body>");
    out.println("</html>");
  }

  private String getKey(String[] classesToTest) {
    CharSequence sb = StringBuilders.newStringBuilder();
    for (int x=0; x< classesToTest.length; x++)
      StringBuilders.asStringBuilder(sb).append(classesToTest[x]);
    return sb.toString();
  }
}

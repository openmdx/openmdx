/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestRunner.java,v 1.3 2007/10/10 16:05:49 hburger Exp $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A class that runs tests and provides output to an HttpServletResponse object.
 *
 * Some of this code is duplicated from junit.textui.TestRunner,
 * but this runner is thread-safe, whereas the other TestRunner
 * is not.
 * <p>
 * @author Michael T. Nygard
 * @author Tracie Karsjens
 * @author Dave Robinson
 */
public class TestRunner extends Thread{
  private String[] classesUnderTest;
  private ByteArrayOutputStream baos = new ByteArrayOutputStream();
  private HTMLWriter htmlScrubber = new HTMLWriter(baos);
  private PrintWriter out = new PrintWriter(htmlScrubber);

  public TestRunner(String[] classesUnderTest) {
    this.classesUnderTest = classesUnderTest;
  }

  public void run() {
    try {
      htmlScrubber.setPassthrough(true);

      for(int i = 0; i < classesUnderTest.length; i++) {
        out.println("<hr>");

        Test suite;
        try {
          suite = createTestSuite(classesUnderTest[i]);
        } catch(Exception ex) {
          htmlScrubber.setPassthrough(false);
          out.println(ex.getMessage());
          htmlScrubber.setPassthrough(true);
          continue;
        }

        out.println("<h1>");
        out.println(classesUnderTest[i]);
        out.println("</h1>");

        doRun(suite, htmlScrubber, out);
      }
      out.flush();
      out.close();
    } catch (IOException e) {
      System.out.println("Exception: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Returns results so far.
   */
  public String getHtmlResults() {
    if (this.isAlive()) {
      out.flush();
    }
    return baos.toString();
  }

  /**
   * Creates the TestResult to be used for the test run.
   */
  protected TestResult createTestResult() {
    return new TestResult();
  }

  /**
   * Given the test class name, create a test suite.  First, see if the
   * class named has a static method called "suite" that takes no arguments
   * and returns junit.framework.Test.  If not, construct a test suite
   * on the named class. (See junit.framework.TestSuite for details.)
   *
   * @param classUnderTest The name of the class to exercise
   * @param Exception numerous causes: ClassNotFound, InvocationTargetException, ClassCastException, etc.
   */
  protected Test createTestSuite(String classUnderTest) throws Exception {
    Class testClass = null;
    Method suiteMethod = null;
    Test suite = null;

    try {
      testClass = Class.forName(classUnderTest);
    } catch (Exception e) {
      System.out.println(e);
      e.printStackTrace();
      throw new Exception("Suite class \"" + classUnderTest + "\" not found");
    }

    try {
      suiteMethod = testClass.getMethod("suite", new Class[0]);
//    suite = (Test) suiteMethod.invoke(null, new Class[0]); // static method
      suite = (Test) suiteMethod.invoke(null, (Object[])null); // static method
    } catch (Exception e) {
      // try to extract a test suite automatically
      suite = new TestSuite(testClass);
    }

    if (suite == null) {
      throw new Exception("Cannot create test suite from class " + classUnderTest + ".  Is there a 'public static Test suite()' method?");
    }

    return suite;
  }

  /**
   * Run the tests in the given suite, sending output to the given PrintWriter.
   *
   * @param suite The test suite to run.
   * @param output The servlet's output destination
   * @exception IOException in case of any IO failures in the PrintWriter.
   */
  protected void doRun(Test suite, final HTMLWriter scrubber, final PrintWriter output) throws IOException {
    TestResult result = createTestResult();
    result.addListener(new TestListener() {
      public void startTest(Test test) {
        output.write(".");
      }

      public void endTest(Test test) {
          //
      }

      public void addError(Test test, Throwable error) {
        output.write("E");
      }

      public void addFailure(Test test, AssertionFailedError failure) {
        output.write("F");
      }
    });

    long startTime = System.currentTimeMillis();
    suite.run(result);
    long endTime = System.currentTimeMillis();
    output.println();
    output.println("<p>Time: " + (endTime - startTime) +" ms</p>");

    print(result, scrubber, output);
  }

  /**
   * Produce the final report.
   *
   * @param result The results of testing
   * @param output The servlet's output destination
   */
  protected synchronized void print(TestResult result, final HTMLWriter scrubber, PrintWriter output) {
    printHeader(result, output);
    printErrors(result, scrubber, output);
    printFailures(result, scrubber, output);
  }

  /**
   * Report on all errors.  An error occurs when a test case throws an exception.
   *
   * @param result The results of testing
   * @param output The servlet's output destination
   */
  protected void printErrors(TestResult result, final HTMLWriter scrubber, final PrintWriter output) {
    if (result.errorCount() != 0) {
      print(output, scrubber, result.errors());
    }
  }

  /** Report on all failures.  A failure occurs when a test case "asserts" a
   *  false statement.
   *
   * @param result The results of testing
   * @param output The servlet's output destination
   */
  protected void printFailures(TestResult result, final HTMLWriter scrubber, final PrintWriter output) {
    if (result.failureCount() != 0) {
      print(output, scrubber, result.failures());
    }
  }

  /**
   * Produce the report header.  Summarize the tests.  No news is good news.
   *
   * @param result The results of testing
   * @param output The servlet's output destination
   */
  protected void printHeader(TestResult result, PrintWriter output) {
    if (result.wasSuccessful()) {
      output.println("<p>OK (" + result.runCount() + " tests)</p>");
    } else {
      output.println("<p>FAILURES!!!</p>");
      output.println("<table border=0>");
      output.println("<tr><td>Run</td><td>"+result.runCount()+"</td></tr>");
      output.println("<tr><td>Failed</td><td>"+result.failureCount()+"</td></tr>");
      output.println("<tr><td>Errors</td><td>"+result.errorCount()+"</td></tr>");
      output.println("</table>");
    }
  }

  /**
   * Report a collection of problems.
   *
   * @param output The servlet's output destination
   * @param problems The collection of problems.  These might be test case errors or failures.
   */
  protected void print(PrintWriter output, final HTMLWriter scrubber, Enumeration problems) {
    while(problems.hasMoreElements()) {
      TestFailure problem = (TestFailure) problems.nextElement();
      output.println("<b>");
      output.println(problem.failedTest());
      output.println("</b>");
      output.println("<pre>");
      scrubber.setPassthrough(false);
      problem.thrownException().printStackTrace(output);
      scrubber.setPassthrough(true);
      output.println("</pre>");
      output.println("<br>");
    }
  }
}

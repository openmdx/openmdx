/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: TestThreadSafety.java,v 1.7 2008/04/04 11:52:35 hburger Exp $
 * Description: TestThreadSafety
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 11:52:35 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.test.portal.servlet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.text.conversion.Base64;
import org.openmdx.portal.servlet.Action;
import org.openmdx.portal.servlet.WebKeys;

//---------------------------------------------------------------------------  
public class TestThreadSafety 
    extends TestCase {
  
    public class ServletTester
        implements Runnable {

        public ServletTester(
            List objectXris
        ) {
          this.actions = new Action[objectXris.size()+1];
          for(int i = 0; i < objectXris.size(); i++) {
              this.actions[i] = new Action(
                  Action.EVENT_SELECT_OBJECT,
                  new Action.Parameter[]{   
                      new Action.Parameter(Action.PARAMETER_OBJECTXRI, (String)objectXris.get(i))                  
                  },
                  null,
                  true
              );
          }
          this.actions[objectXris.size()] = new Action(
              Action.EVENT_LOGOFF,
              new Action.Parameter[]{},
              null,
              false
          );
        }

        private HttpURLConnection getConnection(
            URL url,
            String sessionId
        ) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty(
                "Authorization", 
                "Basic " + Base64.encode((TestThreadSafety.this.userName + ":" + TestThreadSafety.this.password).getBytes("US-ASCII"))
            );
            if(sessionId != null) {
                connection.setRequestProperty(
                    "Cookie",
                    sessionId
                );
            }
            connection.connect();
            return connection;
        }
    
        public void run(
        ) {
            String requestId = null;
            String sessionId = null;
            for(int i = 0; i < this.actions.length; i++) {
                try {
                    String encodedHRef = getEncodedHRef(
                        this.actions[i],
                        requestId
                    );
                    URL url = new URL(
                        TestThreadSafety.this.url,
                        encodedHRef
                    );
                    HttpURLConnection connection = this.getConnection(
                        url,
                        sessionId
                    );
                    int responseCode = connection.getResponseCode(); 
                    if(responseCode != HttpURLConnection.HTTP_OK) {
                        fail("Unexpected response: " + url);
                    }
                    URL responseURL = connection.getURL();
                    if(sessionId == null) {
                        sessionId = connection.getHeaderField("Set-Cookie");
                        sessionId = sessionId.substring(0, sessionId.indexOf(";"));
                    }
                    InputStream is = connection.getInputStream();
                    while(is.read() != -1) {}
                    System.out.println(Thread.currentThread().getName() + ": action[" + i + "]" + " responseURL=" + responseURL + " cookie=" + sessionId);
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                    fail("MalformedURLException");
                }
                catch (IOException e) {
                    e.printStackTrace();
                    fail("IOException");
                }
            }            
        }
        
        private final Action[] actions;
        
    }
    
    //---------------------------------------------------------------------------  
    public TestThreadSafety(
        String name
    ) throws MalformedURLException, FileNotFoundException, IOException {
        super(name);
        InputStream is = new FileInputStream("c:/temp/TestThreadSafety.config");
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(is)
        );
        
        this.url = new URL(reader.readLine());
        this.nThreads = Integer.valueOf(reader.readLine()).intValue();
        this.userName = reader.readLine();
        this.password = reader.readLine();
        this.objectXris = new ArrayList<String>();
        while(reader.ready()) {
            objectXris.add(reader.readLine());
        }
        reader.close();
    }  

    //---------------------------------------------------------------------------     
    public static void main(
        String[] args
    ) throws MalformedURLException, FileNotFoundException, IOException {
        TestRunner.run(suite());
    }
     
    //---------------------------------------------------------------------------    
    public static Test suite(
    ) throws MalformedURLException, FileNotFoundException, IOException {
        TestSuite suite = new TestSuite();
        suite.addTest(
            new TestThreadSafety("TestThreadSafety")
        );
        return suite;
    }
  
    //---------------------------------------------------------------------------  
    protected void setUp(
    ) throws Exception {  
        System.out.println(">>>> **** Begin Test: " + this.getName());
    }

   //---------------------------------------------------------------------------  
    protected void tearDown(
    ) {
        try {
          System.out.println("<<<< **** End Test: " + this.getName());
          AppLog.info("End test",this.getName());
        }
        catch(Exception e) {
          System.out.println("error in tearDown");
        }
    }

    //---------------------------------------------------------------------------
    public void runTest(
    ) throws Throwable {
        testServlet();
    }

    //-------------------------------------------------------------------------
    private static String[] getHRef(
        Action action,
        String requestId
    ) {
        String actionParameter = action.getParameter();

        int n = 3;
        if(requestId != null) n+=2;
        if(actionParameter.length() > 0) n+=2;
        String[] components = new String[n];
        n = 0;
      
        StringBuilder href = new StringBuilder(WebKeys.SERVLET_NAME);      
        if(
            (action.getEvent() == Action.EVENT_DOWNLOAD_FROM_LOCATION) || 
            (action.getEvent() == Action.EVENT_DOWNLOAD_FROM_FEATURE)
        ) {
            href.append(
                "/"
            ).append(
                action.getParameter(Action.PARAMETER_NAME)
            );
        }
        components[n++] = href.toString();
      
        // REQUEST_ID
        if(requestId != null) {
            components[n++] = WebKeys.REQUEST_ID;
            components[n++] = requestId;
        }
      
        // REQUEST_EVENT
        components[n++] = WebKeys.REQUEST_EVENT;
        components[n++] = Integer.toString(action.getEvent());
      
        // Parameter name
        if(actionParameter.length() > 0) {
            components[n++] = WebKeys.REQUEST_PARAMETER;
            components[n++] = actionParameter;
        }
      
        return components;
    }
  
    //-------------------------------------------------------------------------
    private static String getEncodedHRef(
        Action action,
        String requestId
    ) {
        String[] components = getHRef(
            action, 
            requestId
        );
        StringBuilder href = new StringBuilder(components[0]);
        for(int i = 1; i < components.length; i+=2) try {
            href.append(
                i == 1 ? "?" : "&"
            ).append(
                components[i]
            ).append(
                "="
            ).append(
                URLEncoder.encode(components[i+1], "UTF-8")
            );
        } catch(UnsupportedEncodingException e) {}
        return href.toString();
    }
    
    //---------------------------------------------------------------------------
    public void testServlet(
    ) throws UnsupportedEncodingException, InterruptedException {
        Thread t[] = new Thread[this.nThreads];
        for(int i = 0; i < this.nThreads; i++) {
            t[i] = new Thread(new ServletTester(this.objectXris));
            t[i].start();
        }
        for(int i = 0; i < this.nThreads; i++) {
            try { t[i].join(); } catch(Exception e) {}
        }
    }
  
    //---------------------------------------------------------------------------
    // Variables    
    //---------------------------------------------------------------------------
    private final int nThreads;
    private final URL url;
    private final String userName;
    private final String password;
    private final List<String> objectXris;
    
}

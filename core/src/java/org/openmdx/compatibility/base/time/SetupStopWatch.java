/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SetupStopWatch.java,v 1.5 2008/03/21 20:14:51 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 20:14:51 $
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
package org.openmdx.compatibility.base.time;

import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author anyff
 */
@SuppressWarnings("unchecked")
public class SetupStopWatch {
    
    public static String EXECUTION = "execution";
    public static String SETUP = "setup";
        
    private class Run {
        public Run(String testName) {
            _testName = testName;
         }
        
        public String getTestName() {
            return _testName;
        }
        
        public long getExecutionTime(String category) {
            return ((Long) _timesByCategory.get(category)).longValue();
        }
        
        public void setExecutionTime(String category, long time) {
            _timesByCategory.put(category, new Long(time));
        }
        
        public void setExecutionTime(long time) {
            setExecutionTime(SetupStopWatch.EXECUTION, time);
        }
        
        public long getExecutionTime() {
            return getExecutionTime(SetupStopWatch.EXECUTION);
        }

        
        private String _testName;
        private Map _timesByCategory = new HashMap();
    }
    
    public static SetupStopWatch instance() {
        if (_singleton == null) {
            _singleton = new SetupStopWatch();
        }
        return _singleton;
    }
    
    /**
     * start the stopwatch. 
     * 
     * ignores if an timer is running
     */
    public void start() {
        _startTime = new Date();
    }
    
    /**
     * stop the stopwatch 
     * 
     * @param test
     */
    public void stop(String testName, String category)  {
        long millis = 0;
        Date endTime = new Date();
        if (_startTime != null) {
            millis = endTime.getTime() - _startTime.getTime();
        }
        
        Run theRun = (Run)_runs.get(testName);
        if (theRun == null) {
            theRun = new Run(testName);
            _runs.put(testName, theRun);
        }
        
        theRun.setExecutionTime(category, millis);
    }
    
    /**
     * Reset all the data gathered so far.
     *
     */
    public void reset() {
        _runs = new HashMap();
    }

    /**
     * print to std out:
     *
     */
    public void printOut(Class testClass) {
        long exeTimes = 0;  
        long setupTimes = 0;
        int nameLength = 5;
        int numberLength = 10;
        PrintStream out = System.out;
        
        for (Iterator r = _runs.keySet().iterator(); r.hasNext(); ) {
            String testName = (String)r.next();
            if (nameLength < testName.length()) {
                nameLength = testName.length();
            }
        }
        nameLength += 1;
        
        out.print("--" + LINES.substring(0, (nameLength)));
        out.print(LINES.substring(0, (numberLength)));
        out.print(LINES.substring(0, (numberLength)));
        out.println();

        out.println("Times for " + testClass.getName());

        
        out.print("  Test" + SPACES.substring(0, (nameLength - "Test".length())));
        out.print("execution"+ SPACES.substring(0, (numberLength - "execution".length())));
        out.print("setup"+ SPACES.substring(0, (numberLength - "setup".length())));
        out.println();
        out.print("--" + LINES.substring(0, (nameLength)));
        out.print(LINES.substring(0, (numberLength)));
        out.print(LINES.substring(0, (numberLength)));
        out.println();
        
        for (Iterator r = _runs.values().iterator(); r.hasNext(); ) {
            Run run = (Run)r.next();
            
            exeTimes += run.getExecutionTime(SetupStopWatch.EXECUTION);
            setupTimes += run.getExecutionTime(SetupStopWatch.SETUP);
            
            String execution = String.valueOf(run.getExecutionTime(SetupStopWatch.EXECUTION));
            String setup = String.valueOf(run.getExecutionTime(SetupStopWatch.SETUP));
            
            out.print("  " + run.getTestName() + SPACES.substring(0, (nameLength - run.getTestName().length())));
            out.print(execution + SPACES.substring(0, (numberLength - execution.length())));
            
            out.print(setup + SPACES.substring(0, (numberLength - setup.length())));
            
            out.println();
        }
        
        
    
        out.print("--" + LINES.substring(0, nameLength));
        out.print(LINES.substring(0, (numberLength)));
        out.print(LINES.substring(0, (numberLength)));
        out.println();

        String execution = String.valueOf(exeTimes);
        String setup = String.valueOf(setupTimes);
        out.print("  Total" + SPACES.substring(0, (nameLength - "Total".length())));
        out.print(execution + SPACES.substring(0, (numberLength - execution.length())));           
        out.print(setup + SPACES.substring(0, (numberLength - setup.length())));
        out.println();
        
        out.print("==" + TOTALS.substring(0, nameLength));
        out.print(TOTALS.substring(0, (numberLength)));
        out.print(TOTALS.substring(0, (numberLength)));
        out.println();
        
    }
    
    
    private SetupStopWatch() {
     // just to have the constructor private  
    }
    
    private Date _startTime = null;
    private Map _runs = new HashMap();
    private static SetupStopWatch _singleton;
    static String SPACES = "                                        ";
    static String LINES = "------------------------------------------";
    static String TOTALS = "==========================================";

}

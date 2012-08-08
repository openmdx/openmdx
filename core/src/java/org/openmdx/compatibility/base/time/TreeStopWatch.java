/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TreeStopWatch.java,v 1.8 2007/10/10 17:16:07 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 17:16:07 $
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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.StringBuilders;
import org.openmdx.compatibility.base.dataprovider.layer.model.StopWatch_1_0;


/**
 * Third incarnation of StopWatch.
 * 
 * New:
 * -free handling of all timers (start/stop overlapping)
 * 
 * @author anyff
 */
public class TreeStopWatch implements StopWatch_1_0 {
  
    static private class TimerDesc {
        public TimerDesc(String name) {
            _name = name;
            _avgRun = false;
            _avgAppCount = false;
            _printMaxTime = false;
            _total = 0;
            _runs = 0;
            _outOfSequence = 0;
            _printOutOfSequence = false;
            
            _maxTotal = 0;
            _maxOutOfSequence = 0;
            _maxRuns = 0;
            _maxMaxTime = 0;
            _maxAppCount = 0;
        } 
        
        public void setTotalTime(long total) {
            _total += total;
            
            if (total > _maxTotal) {
                _maxTotal = total;
            }
        }
        
        public void setOutOfSequence(long outOfSequence) {
            _printOutOfSequence = _printOutOfSequence || (_outOfSequence > 0);
            _outOfSequence += _outOfSequence;
            
            if (outOfSequence > _maxOutOfSequence) {
                _maxOutOfSequence = outOfSequence;
            }

        }
        
        public void setAvgRun(long runs) {
            _avgRun = _avgRun || (runs > 1);
            _runs += runs;
            
            if (runs > _maxRuns) {
                _maxRuns = runs;
            }
        }
        
        public void setMaxTime(long maxTime) {
            
            if (maxTime > ( (float)_total / (float)_runs) * 2) {
                // print maxTime if it is 100% bigger then the average
                _printMaxTime = true;
            }
            
            if (maxTime > _maxMaxTime) {
                _maxMaxTime = maxTime;
            }
        }
        
        public void setAvgAppCount(long appCount) {
            _avgAppCount = _avgAppCount || (appCount > 1);
            
            if (appCount > _maxAppCount){
                _maxAppCount = appCount;
            }
        }
        
        
        public int columns() {
            return 1 + 
                (_avgRun ? 1 : 0) + 
                (_avgAppCount ? 1 : 0) + 
                (_printMaxTime ? 1 : 0) +
                (_printOutOfSequence ? 1 : 0);
        }
        
        String _name;
        boolean _avgRun;
        boolean _avgAppCount;
        boolean _printMaxTime;
        long _total;
        long _runs;
        long _outOfSequence;
        boolean _printOutOfSequence;
        
        long _maxTotal = 0;
        long _maxOutOfSequence = 0;
        long _maxRuns = 0;
        long _maxMaxTime = 0;
        long _maxAppCount = 0;

    }
    
    static private class TestRun {
        public TestRun(String name) {
            _name = name;
            _root = new Timer("root");
        }
        
        // get the timers ordered according to their names rather then 
        // the containment in other timers. 
        public Map getTimersByName() {
            Map timersByName = new HashMap();
            
            getTimersByName(_root, timersByName);
            
            return timersByName;
        }
        
        
        public Timer getRootTimer() {
            return _root;
        }
        
        
        public String getName() {
            return _name;
        }
        
        
        private void getTimersByName(Timer timer, Map timersByName) {
            if (timer != null && timersByName != null) {
                for (Iterator t = timer.getChildren().values().iterator(); t.hasNext();) {
                    Timer child = (Timer) t.next();
                    if (timersByName.containsKey(child.getName())){
                        ((Timer)timersByName.get(child.getName())).addTimings(child);
                    }
                    else {
                        timersByName.put(child.getName(), new Timer(child));
                    }
                    
                    getTimersByName(child, timersByName);
                }
            }
        }
        
        String _name;
        //private Map _timers;
        private Timer _root;
    }
    
    /**
     * Throw RuntimeExceptions to keep the class easy usable, without adding
     * exceptions to signature.
     */
    static private class Timer {
        public Timer(String name) {
            _name = name;
        }
        
        /** 
         * copies the counts of the given timer; the children are not copied at
         * all.
         * @param timer
         */
        public Timer(Timer timer) {
            _name = timer._name;
            _millis = timer._millis;
            _maxMillis = timer._maxMillis;
            _startCount = timer._startCount;
        }
        
            
      
        public String getName() {
            return _name;
        }
        
        
        /** 
         * add the times of timer b to this one. This adds the counters and 
         * times, but not the children.
         */
        public void addTimings(Timer b) {
            _millis += b._millis;
            _startCount += b._startCount;
            _outOfSequence += b._outOfSequence;
            if (_maxMillis < b._maxMillis) {
                _maxMillis = b._maxMillis;
            }
        }
        
                
        /** 
         * add a child Timer with the name to the Timer and return the timer.
         * If there exists already a child with that name, that is returned.
         *  
         * @param name
         */
        public Timer addChild(String name) { 
          Timer timer = (Timer)_children.get(name);
          if (timer == null) {
            timer = new Timer(name);
            _children.put(name, timer);
          }
          return timer;
        }
        
        public Map getChildren() {
            return _children;
        }
      
        /**
         * start first resets all the counters for this timer; any restart 
         * counts or TotalTimes get lost.
         */
        public Timer start() {
            if (_start != null) {
                _outOfSequence = 1;
                //throw new RuntimeException("timer is already running " + _name);
            }
            _millis = 0;
            _startCount = 0;
            _appCount = 0;
            _children = new HashMap();
            _maxMillis = 0;
            
            _start = new Date();
            return this;
        }
      
        /** 
         * Restart calculates the number of times the timer was started;
         * it not started so far, it gets started 
         */
        public Timer restart() {
            
            if (_startCount == 0) {
                start();
            } 
            else {
              _start = new Date();
            }
            return this;
        }
      
        public long stop(long appCount)  {
            Date now = new Date();
            long delta = -1; 
            if (_start == null) {
                _outOfSequence++;
                delta = 0;
                // throw new RuntimeException("must start() or restart() before stop() " + _name);
            }
            else {
                delta = now.getTime() - _start.getTime();
              
                _millis += delta;
                _startCount++;
                _appCount += appCount;
                _start = null;
                
                if (delta > _maxMillis) {
                    _maxMillis = delta;
                }
            }
            return delta;
        }
      
        public void reset() {
            _start = null;
            _millis = 0;
            _maxMillis = 0;
            _startCount = 0;
            _appCount = 0;
            _children = new HashMap(); 
            _outOfSequence = 0;
        }
      
        public long getTotalTime() {
            return _millis;
        }
      
        public long getRestartCount() {
            return _startCount;
        }
        
        public long getOutOfSequence() {
            return _outOfSequence;
        }
        
        public long getMaxTime() {
            return _maxMillis;
        }
      
        public long getAppCount() {
            return _appCount;
        }
      
        public String getTimePerRestart() {
            if (_startCount > 0) {
                return String.valueOf((double)_millis / (double)_startCount);
            }
            else {
                return "n/a";
            }
        }
      
        public String getTimePerAppCount() {
            if (_appCount > 0) {
                return String.valueOf((double)_millis / (double)_appCount);
            }
            else {
                return "n/a";
            }
        }
      
        long _appCount = 0; // application count; eg. number of objects found
        long _millis = 0; 
        long _maxMillis = 0;
        Date _start;
        long _startCount = 0;
        long _outOfSequence = 0;
        String _name;
        Map _children = new HashMap();
    }
  
    /**
     * Class supplying helper methods for pretty printing text. 
     * 
     * Name is shortend to allow direct usage.
     * 
     * @author anyff
     *
     * To change the template for this generated type comment go to
     * Window>Preferences>Java>Code Generation>Code and Comments
     */
  
    private static class PP { // PP for pretty print
      
        public static String alignLeft(
            Object obj, 
            int wide
        ) {
            return align(obj, ALIGN_LEFT, wide);
        }
      
        public static String alignRight(
            long num,
            int wide
        ) {
            String numString = String.valueOf(num);
            return align(numString, ALIGN_RIGHT, wide);
        }
        
        public static String alignRight(
            Object obj,
            int wide
        ) {
            return align(obj, ALIGN_RIGHT, wide);
        }



      
        /** 
         * Construct a new String align it according and fill with spaces
         * or cut the string. The new string will have wide characters, with 
         * at least a space to the end.
         * 
         * @param obj    object to print
         * @param align  ALIGN_LEFT or ALIGN_RIGHT
         * @param wide   length of field in total
         */
        public static String align(
            Object obj, 
            short alignment, 
            int wide
        ) {
            String aligned = null;
            String string = obj.toString();
  
            if (string.length() >= wide - 1) {
                aligned = string.substring(0,wide-1);
            }
            else if (alignment == ALIGN_LEFT) {
                aligned = new String(string) + SPACES.substring(0, wide - 1 - string.length());
            }
            else if (alignment == ALIGN_RIGHT) {
                aligned = SPACES.substring(0, wide - 1 - string.length()) + new String(string);
            }
            aligned = aligned + " ";
            return aligned;
        }
  
        /**
         * alignment for int values
         * 
         * @param intValue
         * @param alignment
         * @param wide
         */
        public static String align(
            int intValue,
            short alignment,
            int wide
        ) {
            return align(new Integer(intValue), alignment, wide);
        }
      
        /**
         * provide spaces to fill
         * 
         * @param wide
         */
        public static String spaces(
            int wide
        ) {
            return SPACES.substring(0, wide < 0 ? 0 : wide);
        }
      
        /**
         * produce a string of length wide, with all the same symbols.
         * @param symbol
         * @param wide
         */
        public static String symbols(
            String symbol,
            int wide
        ) {
            CharSequence buffer = StringBuilders.newStringBuilder(wide+1);
            for (int i = 0; i < wide; i++) {
                StringBuilders.asStringBuilder(buffer).append(symbol);
            }
            return buffer.toString();
        }
  
        public static short ALIGN_LEFT = 0;
        public static short ALIGN_RIGHT = 1;
  
        private static String SPACES = "                                        ";
    }
  
  
//    public static TreeStopWatch instance() {
//        if (_singleton == null) {
//            _singleton = new TreeStopWatch();
//        }
//        return _singleton;
//    }

    
    public TreeStopWatch() {
        reset();
    }


    public void setStopReports(List streams, boolean statisticsLog) {
        _reportStreams = streams;
        _reportStatistics = statisticsLog;
    }
  
    /**
     * Reset all the data gathered so far.
     *
     */
    public void reset() {
        _tests = new ArrayList();
        _runningTimerStack = new Stack();
    }
   
    
    /**
     * switch to test. All the following timers are for this test until the 
     * test is switched again.
     */
    public void switchTest(String name) {
       TestRun currentTest = null;
       for (Iterator r = _tests.iterator(); r.hasNext() && currentTest == null;) {
            TestRun test = (TestRun)r.next();
            if (test._name.equals(name)) {
                currentTest = test;
            }
        }
        if (currentTest == null) {
            currentTest = new TestRun(name);
            _tests.add(currentTest);
        }
        _currentTestName = name;
        _runningTimerStack.push(currentTest.getRootTimer());
    }
    
    /**
     * if a timer is running when a new timer gets first started, the new timer
     * becomes a child of the running timer.
     * 
     * @param name
     */
    private Timer getStartTimer(String name) {
        Timer timer = null;
        if (!_runningTimerStack.empty()) {
            Timer current = ((Timer)_runningTimerStack.peek());
            timer = current.addChild(name);
        }
        else {
            timer = new Timer(name);
        }
        _runningTimerStack.push(timer);
        
        /*
        Timer timer = (Timer)_timers.get(name);
        if (timer == null) {
            timer = new Timer(name);
            if (!_runningTimerStack.empty()) {
                ((Timer)_runningTimerStack.peek()).addChild(timer);
            }
            _timers.put(name, timer);
        }
        _runningTimerStack.push(timer);
        */
        return timer;
    }      
  
  
    /** 
     * starting a timer adds the new time up to the already 
     * accumulated times. 
     * 
     * @param name 
     * @return total time of the timer so far
     */
    public void startTimer(String name) {
        getStartTimer(name).restart();
    }
  
    /**
     * stop the timer.
     * <p>
     * After a stop, the timer can be restarted in which case the appCount 
     * gets accumulated; and the number of invocations gets calculated.
     * <p>
     * The timer can be started, in which case the counts get lost.
     * <p>
     * The appCount can be an application defined count of 
     * the things achieved during the time. The appCount is accumulated and 
     * the average is shown in the end.
     * 
     * @param timer
     * @return time since last start or restart
     */
    public void stopTimer(String name)  {
        if (_runningTimerStack.size() > 0) {
            Timer timer = (Timer)_runningTimerStack.pop();
        
            long time = timer.stop(0);
            
            if (!timer.getName().equals(name)) {
                // just try stoping all timers up to the next timer with that name
                while (_runningTimerStack.size() > 0
                    && !timer.getName().equals(name)
                ) {
                    timer = ((Timer)_runningTimerStack.pop());
                    timer.stop(0);
                }
                                    
    //            throw new RuntimeException(
    //                "timer to stop " + name + 
    //                " is out of sequence, expected "+ timer.getName());
            }
            
            reportStopTime(name, time);
        }
    }
  
  
    /**
     * reset the intermediate timer.
     * 
     * @param timer
     * /
    public void resetTimer(String name) {
        Timer timer = (Timer)_timers.get(name);
        if (timer == null) {
            throw new RuntimeException("unknown timer " + name);
        }
  
        timer.reset();
    }
    */
  
  
    /**
     * print to out stream. 
     * 
     * print a line for each test; 
     * 
     * print a column for each timer; an average column if the timer was used
     * more then once and avg appCount if appCount is not 0.
     *
     */
    public void printOut(String testName, PrintWriter out) {
        
        // get the names of all timers in all test runs. 
        // Find out if they have averages or not
        Map timerDescs = new HashMap();
        
        for (Iterator r = _tests.iterator(); r.hasNext(); ) {
            TestRun testRun = (TestRun) r.next();
            
            Map timersByName = testRun.getTimersByName();
            for (Iterator t = testRun.getTimersByName().keySet().iterator(); t.hasNext(); ){
                String timerName = (String)t.next();
                Timer timer = (Timer)timersByName.get(timerName);
                
                TimerDesc desc = (TimerDesc)timerDescs.get(timerName);
                if (desc == null) {
                  timerDescs.put(timerName, desc = new TimerDesc(timerName));
                }
                
                desc.setOutOfSequence(timer.getOutOfSequence());
                desc.setTotalTime(timer.getTotalTime());
                desc.setAvgAppCount(timer.getAppCount());
                desc.setAvgRun(timer.getRestartCount());
                desc.setMaxTime(timer.getMaxTime());
            }
        }
        
        // find the number of columns
        int columns = 0;
        for (Iterator d = timerDescs.values().iterator(); d.hasNext();) {
            TimerDesc desc = (TimerDesc) d.next();
            columns += desc.columns();
        }
        int lineLength = NAME_LENGTH + columns*NUMBER_LENGTH;
        
        // header
        out.println();
        out.println("Times for " + testName);
        out.println(PP.symbols("-", lineLength));
        out.print(PP.alignLeft("Test", NAME_LENGTH));
        for (Iterator t = timerDescs.values().iterator(); t.hasNext(); ) {
            TimerDesc desc = (TimerDesc) t.next();         
      
            out.print(PP.alignLeft(desc._name, NUMBER_LENGTH));
            if (desc._avgRun) {
                out.print(PP.alignRight("/#start", NUMBER_LENGTH));
            }
            if (desc._avgAppCount) {
                out.print(PP.alignRight("/#app", NUMBER_LENGTH));
            }
            if (desc._printMaxTime) {
                out.print(PP.alignRight("max", NUMBER_LENGTH));
            }
            if (desc._printOutOfSequence) {
                out.print(PP.alignRight("lostSeq", NUMBER_LENGTH));
            }
        }
        out.println();
        out.println(PP.symbols("-", lineLength));
        
        // data
        for (Iterator r = _tests.iterator(); r.hasNext(); ) {
            TestRun testRun = (TestRun) r.next();
            
            out.print(PP.alignLeft(testRun.getName(), NAME_LENGTH));
            Map timers = testRun.getTimersByName();
            
            for (Iterator t = timerDescs.values().iterator(); t.hasNext(); ) {
                TimerDesc desc = (TimerDesc) t.next();
                
                Timer timer = (Timer) timers.get(desc._name);
                if (timer != null) {
                    out.print(PP.alignRight(timer.getTotalTime(), NUMBER_LENGTH));
                    
                    if (desc._avgRun) {
                        out.print(PP.alignRight(timer.getTimePerRestart(), NUMBER_LENGTH));
                    }
                    if (desc._avgAppCount) {
                        out.print(PP.alignRight(timer.getTimePerAppCount(), NUMBER_LENGTH));
                    }
                    if (desc._printMaxTime) {
                        out.print(PP.alignRight(timer.getMaxTime(), NUMBER_LENGTH));
                    }
                    if (desc._printOutOfSequence) {
                        out.print(PP.alignRight(timer.getOutOfSequence(), NUMBER_LENGTH));
                    }

                }
            }
        }
        out.println();
        out.println(PP.symbols("-", lineLength));
        
        //    footer
        out.print(PP.alignLeft("Total", NAME_LENGTH));
        for (Iterator t = timerDescs.values().iterator(); t.hasNext(); ) {
            TimerDesc desc = (TimerDesc) t.next();         
      
            out.print(PP.alignRight(String.valueOf(desc._total), NUMBER_LENGTH));
            if (desc._avgRun) {
                out.print(PP.alignRight("-", NUMBER_LENGTH));
            }
            if (desc._avgAppCount) {
                out.print(PP.alignRight("-", NUMBER_LENGTH));
            }
            if (desc._printMaxTime) {
                out.print(PP.alignRight("-", NUMBER_LENGTH));
            }
            if (desc._printOutOfSequence) {
                out.print(PP.alignRight("-", NUMBER_LENGTH));
            }

        }
        out.println();
        out.println(PP.symbols("=", lineLength));
        
        // hierarchy
        out.println();
        
        for (Iterator r = _tests.iterator(); r.hasNext(); ) {
            TestRun testRun = (TestRun) r.next();
                        
            if (testRun.getRootTimer().getChildren().size() > 0) {
              out.print(PP.alignLeft(testRun.getName(), NAME_LENGTH));
              out.print(PP.spaces(TREE_TIMES_START - NAME_LENGTH));
              out.print(PP.alignRight("time", NUMBER_LENGTH));
              out.print(PP.alignRight("calls", NUMBER_LENGTH));
              out.print(PP.alignRight("avgCalls", NUMBER_LENGTH));
              out.print(PP.alignRight("max", NUMBER_LENGTH));
              out.print(PP.alignRight("lostSeq", NUMBER_LENGTH));
              out.print(PP.alignRight("num", NUMBER_LENGTH));
              out.print(PP.alignRight("avgNum", NUMBER_LENGTH));
              out.println();
            }
            
            for (Iterator t = testRun.getRootTimer().getChildren().values().iterator(); t.hasNext(); ){
              Timer timer = (Timer) t.next();
              
              printParent(out, timer, 0);
              
            }
        }
    }
    
    private void printParent(PrintWriter out, Timer parent, int indent) {
          
        out.print(PP.spaces(indent));
        out.print(PP.alignLeft(parent.getName(), NAME_LENGTH));
        out.print(PP.symbols(".",TREE_TIMES_START - indent - NAME_LENGTH));
        out.print(PP.alignRight(parent.getTotalTime(), NUMBER_LENGTH));
        out.print(PP.alignRight(parent.getRestartCount(), NUMBER_LENGTH));
        out.print(PP.alignRight(parent.getTimePerRestart(), NUMBER_LENGTH));
        out.print(PP.alignRight(parent.getMaxTime(), NUMBER_LENGTH));
        out.print(PP.alignRight(parent.getOutOfSequence(), NUMBER_LENGTH));
        if (parent.getAppCount() > 0) {
            out.print(PP.alignRight(String.valueOf(parent.getAppCount()), NUMBER_LENGTH));
            out.print(PP.alignRight(parent.getTimePerAppCount(), NUMBER_LENGTH));
        }
        out.println();
        
        Collection myChildren = parent.getChildren().values();
        for (Iterator c = myChildren.iterator(); c.hasNext(); ) {
            printParent(out, (Timer)c.next(), indent + 2);
        }
    }                    
  
    
    private void reportStopTime(String timerName, long time) {
        if (_reportStreams != null) {
            for (Iterator s =_reportStreams.iterator(); s.hasNext();) {
                PrintStream ps = (PrintStream)s.next();
                ps.print(_currentTestName);
                ps.print(" ");
                ps.print(timerName);
                ps.print(" ");
                ps.print(time);
                ps.println();
            }
        }
        if (_reportStatistics) {
            SysLog.statistics(_currentTestName + " " + timerName, String.valueOf(time));
        }
    }
  
    
    // private static TreeStopWatch _singleton;
  
    //private Map _timers = new HashMap();
    private List _tests = new ArrayList();
    
    private String _currentTestName;
    
    /** the timer last started and not stopped */
    private Stack _runningTimerStack = new Stack();
    
    List _reportStreams;
    boolean _reportStatistics;
  
    static final int NAME_LENGTH = 20;
    static final int NUMBER_LENGTH = 10;
    static final int TREE_TIMES_START = 50;
  
    static String SPACES = "                                                                                  ";
    static String LINES  = "----------------------------------------------------------------------------------";
    static String TOTALS = "==================================================================================";

}


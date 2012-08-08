/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Subprocess.java,v 1.8 2008/03/21 18:38:44 hburger Exp $
 * Description: SubProcess
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:38:44 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.kernel.application.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmdx.kernel.log.SysLog;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Subprocess
 */
@SuppressWarnings("unchecked")
public class Subprocess extends Thread {

    /**
     * The exit and run value for success.
     */
    public static final Integer SUCCESS = new Integer(0);
    
    /**
	 * Fork a Java process
	 * @param jre the JRE directory. 
	 *        Optional, defaults to the "java.home" system property. 
	 * @param classpath the class-path. 
	 *        Optional, defaults to the "java.class.path" system property. 
	 * @param options additional Java VM Options.
	 *        Optional.
	 * @param properties the system properties to be set.
	 *        Optional, defaults to "org.openmdx.rmi.naming.service" and 
	 *        "org.openmdx.rmi.registry.port" retrieved from 
	 *        {@link org.openmdx.kernel.naming.Contexts Contexts}.
	 * @param arguments
	 *        Program arguments
	 * @param synchronization
	 *        Fork waits until the subprocess has terminated or the 
	 *        synchronization character has been sent either to the 
	 *        subprocess' error or output stream unless synchronization is 
	 *        <code>null</code>.
	 * @param outputStream 
     *        The stream obtains data piped from the standard output stream 
     *        of the process represented by this <code>Subprocess</code> 
     *        object; or <code>null</code> to discard the data. 
	 * @param exceptionStream 
     *        The stream obtains data piped from the error output stream of 
     *        the process represented by this <code>Subprocess</code> 
     *        object; or <code>null</code> to discard the data.
	 * 
	 * @return the LightweightContainer's Process
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static Subprocess fork(
		String jre,
		String classpath, 
		String[] options,
		Map properties,
		String className,
		String[] arguments,
		Character synchronization, 
        OutputStream outputStream, 
        OutputStream exceptionStream
	) throws IOException, InterruptedException {
		List command = new ArrayList();
		String separator = System.getProperty("file.separator");
		command.add(
			(jre == null ? System.getProperty("java.home") : jre) +
			separator + "bin" + separator + "java"
		);
		command.add("-classpath");
		command.add(
			classpath == null ? System.getProperty("java.class.path") : classpath
		);
		if(options != null) command.addAll(Arrays.asList(options));
		if(properties != null) for (
			Iterator entries = properties.entrySet().iterator();
			entries.hasNext();
		){
			Map.Entry entry = (Entry) entries.next();
			command.add("-D" + entry.getKey() + '=' + entry.getValue());
		}
		command.add(className);
		if(arguments != null) command.addAll(Arrays.asList(arguments));
		SysLog.detail(
			"Forking a Java Process",
			new IndentingFormatter(command)			
		);
		Subprocess subprocess = new Subprocess(
			Runtime.getRuntime().exec(
				(String[]) command.toArray(new String[command.size()])
			),
			synchronization,
            outputStream,
            exceptionStream
		);
		if(subprocess.notification != null) synchronized(subprocess.notification){
			subprocess.notification.wait();
		}
		return subprocess;
	}

    /**
     * Fork a Java process
     * 
     * @deprecated in favour of  the fork method including an explicit
     * output and exception stream.
     * 
     * @param jre the JRE directory. 
     *        Optional, defaults to the "java.home" system property. 
     * @param classpath the class-path. 
     *        Optional, defaults to the "java.class.path" system property. 
     * @param options additional Java VM Options.
     *        Optional.
     * @param properties the system properties to be set.
     *        Optional, defaults to "org.openmdx.rmi.naming.service" and 
     *        "org.openmdx.rmi.registry.port" retrieved from 
     *        {@link org.openmdx.kernel.naming.Contexts Contexts}.
     * @param arguments
     *        Program arguments
     * @param synchronization
     *        Fork waits until the subprocess has terminated or the 
     *        synchronization character has been sent either to the 
     *        subprocess' error or output stream unless synchronization is 
     *        <code>null</code>.
     * 
     * @return the LightweightContainer's Process
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public static Subprocess fork(
        String jre,
        String classpath, 
        String[] options,
        Map properties,
        String className,
        String[] arguments,
        Character synchronization
    ) throws IOException, InterruptedException {
        return fork(
            jre, 
            classpath, 
            options, 
            properties, 
            className, 
            arguments, 
            synchronization,
            System.out,
            System.err
         );
    }

    /**
     * Constructor
     * 
     * @param process
     * @param notification
     *        The signal character or <code>null</code>
     * @param outputStream 
     *        The stream obtains data piped from the standard output stream 
     *        of the process represented by this <code>Subprocess</code> 
     *        object; or <code>null</code> to discard the data. 
     * @param exceptionStream 
     *        The stream obtains data piped from the error output stream of 
     *        the process represented by this <code>Subprocess</code> 
     *        object; or <code>null</code> to discard the data.
     */
    public Subprocess(
        Process process,
        Character notification,
        OutputStream outputStream,
        OutputStream errorStream
    ){
        this.process = process;
        this.notification = notification == null ? null : new Character(notification.charValue());
        new Pipe(process.getInputStream(), outputStream, 0).start();
        new Pipe(process.getErrorStream(), errorStream, 1).start();
        start();
    }

    /**
     * Constructor
     * 
     * @deprecated in favour of the constructor including an explicit
     * output and exception stream.
     * 
     * @param process
     * @param notification
     *        The signal character or <code>null</code>
     */
    public Subprocess(
        Process process,
        Character notification
    ){
        this(
            process, 
            notification, 
            System.out, 
            System.err
        );
    }
            
    /**
	 * The notifcation object is used to synchronize 
	 */
	Character notification;
	
    /**
     * The run status 
     */
    Integer status;

    /**
     * 
	 */
	final Process process;

    /* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			this.process.waitFor();
			if(this.notification != null) synchronized(this.notification){
				this.notification.notify();
			}
		} catch (InterruptedException e) {
			// The subprocess is now detached
		}
	}

	/**
	 * Retrieve the subprocess' exit value.
	 * 
	 * @return the subprocess' exit value; or null if the subprocess has not 
	 *         yet terminated
	 */
	public Integer exitValue(
	){
		if(isAlive()) return null;
		try {
			return new Integer(this.process.exitValue());
		} catch (java.lang.IllegalThreadStateException exception) {
			return null;
		}
	}
	
    /**
     * Tells whether the sub-process is active and has notified the launcher
     * via it's exception stream.
     * 
     * @return <ul>
     * <li>0 if the sub-process is alive and has notified the launcher
     * via it's output stream
     * <li>1 if the sub-process is alive and has notified the launcher
     * via it's exception stream
     * <li>code>null</code> the sub-process is no longer alive or the notification 
     * character is <code>null</code> or the current thread is interrupted
     */
    public Integer runValue(){
        if(this.notification == null || !isAlive()) return null;
        synchronized (this.notification){
            if(this.status == null) try {
                this.notification.wait();
            } catch (InterruptedException exception) {
                return null;
            }
        } 
        return this.status;
    }
    
	/**
	 * Kills the subprocess
	 */
	public void destroy() {
		this.process.destroy();
	}
	
    
	//------------------------------------------------------------------------
	// Pipe
	//------------------------------------------------------------------------
	
	/**
	 * Pipes the forked process' output to its parent
	 */
	class Pipe extends Thread {

		private final InputStream source;
		private final OutputStream sink;
        private final int notificationStatus;
        
		Pipe(
			InputStream source,
			OutputStream sink,
            int notificationStatus
		){
			this.source = source;
			this.sink = sink;
            this.notificationStatus = notificationStatus;
			setDaemon(true);
		}
				
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				for(
					int b = this.source.read();
					b >= 0;
					b = this.source.read()
				) if(
					notification != null &&
                    status == null &&
                    b == notification.charValue()
				) synchronized(notification){
                    status = new Integer(this.notificationStatus);
					notification.notify();
				} else {
					if(this.sink != null) this.sink.write(b);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}

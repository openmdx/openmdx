/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ClassLoaderThread.java,v 1.4 2008/09/11 10:49:10 hburger Exp $
 * Description: Class Loader Thread
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/11 10:49:10 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.test.tomcat;

import java.net.URL;
import java.util.Date;

/**
 * Class Loader Thread
 */
public class ClassLoaderThread extends Thread {

	/**
	 * Constructor
	 * 
	 * @param runnable
	 */
	ClassLoaderThread(
		Runnable runnable
	){
		super(runnable);
	}
	
	private static int THREAD_COUNT = 1000; // 1000;
	private static int APPLICATION_COUNT = 7;
	
	/**
	 * @param args
	 */
	public static void main(
		String[] args
	) {
		launch(
			ClassLoaderThread.class.getClassLoader()
		);
	}

	public static void launch(
		ClassLoader commonClassLoader,
		URL... enterpriseApplicationArchives
	){
		System.out.println(new Date() + ": Preparing EAR class loader...");
		ClassLoader[] classLoaders = new ClassLoader[APPLICATION_COUNT];
		for(
			int i = 0;
			i < APPLICATION_COUNT;
			i++
		){
			classLoaders[i] = ClassLoaderTest.getClassLoader(
				commonClassLoader, 
				enterpriseApplicationArchives
			);
		}
		System.out.println(new Date() + ": Preparing threads...");
		Thread[] threads = new Thread[THREAD_COUNT];
		for(
		    int i = 0;
		    i < THREAD_COUNT;
		    i++
		){
			threads[i] = new ClassLoaderThread(
				new ClassLoaderTest(
					classLoaders[i % APPLICATION_COUNT]
				)
			);
		}
		System.out.println(new Date() + ": Starting threads...");
		for(
		    int i = 0;
		    i < THREAD_COUNT;
		    i++
		){
			threads[i].start();
		}
		System.out.println(new Date() + ": Joining threads...");
		for(
		    int i = 0;
		    i < THREAD_COUNT;
		    i++
		){
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				System.err.println(new Date() + ": Thread " + i + " has been interrupted");
			}
		}
		System.out.println(new Date() + ": Threads terminated");
		ClassLoaderTest.printFailures();
	}

}

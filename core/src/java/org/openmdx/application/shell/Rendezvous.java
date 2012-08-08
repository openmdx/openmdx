/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Rendezvous.java,v 1.1 2009/01/13 23:51:09 wfro Exp $
 * Description: A Thread Rendezvous class
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 23:51:09 $
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
package org.openmdx.application.shell;


/**
 * The rendezvous mechanism is implemented as a single class, and provides a set
 * of functions useful for a wide range of applications. In short, it allows one
 * to synchronize an arbitrary number of concurrent threads very easily. To use
 * the rendezvous class, the methods used are add(), remove(), and rendezvous().
 * First, a new rendezvous object and the new thread objects to be synchronized
 * are created. Then, the threads are placed in the rendezvous, using the add()
 * method, usually before the new threads are started. Within the body of each
 * thread, the rendezvous() method of the rendezvous object is called. This
 * causes that thread to block until all threads in that particular rendezvous
 * have called rendezvous(). When all threads reach their rendezvous points,
 * all the threads in the rendezvous resume execution.
 * 
 * Additionally, at any point during its execution, a thread may elect to remove
 * itself or another thread from the rendezvous. This feature supports the 
 * capability to implement conditional synchronization, which can be useful in 
 * many applications. Also, several rendezvous objects may exist within a 
 * program at once, and a single thread may participate in many rendezvous. 
 * This allows many different "clocks" to coexist within the program, and is 
 * also quite powerful in allowing a wide range of expression of behaviors. 
 * Taken together, the capabilities of the rendezvous class make it ideal for 
 * use in applications where inter-thread interaction is rich and complex, 
 * and where determinate behavior is desired.
 */
public class Rendezvous
{
	public Rendezvous()
	{
		this(2);
	}

	public Rendezvous(int initSize)
	{
		target      = new Thread[initSize];
		targState   = new boolean[initSize];
		targetIndex = 0;
	}

	public Rendezvous(Thread[] initMembers)
	{
		this(initMembers.length);
		
		for (int ii=0; ii<initMembers.length; ii++)  add(initMembers[ii]);
	}



	/**
	 * Add the currently executing thread to the rendezvous.
	 */
	public synchronized void add()
	{
		this.add(Thread.currentThread());
	}



	/**
	 * Remove the currently executing thread from the rendezvous.
	 * Note that threads must remove themselves from the rendezvous;
	 * they may not be removed by other threads.
	 */
	public synchronized void remove()
	{
		this.remove(Thread.currentThread());
	}



	/**
	 * Threads should call this when they've reached their rendezvous points
	 */
	public synchronized void rendezvous()
	{
		// Mark off the appropriate entry in the array
		for (int i = 0; i < targetIndex; i++) {
			if (target[i] == Thread.currentThread()) {
				targState[i] = true;
				if (checkStatus() == false) {
					try { 
					    wait(); 
					} catch (InterruptedException e) {
					    // ignore
					}
				}

				break;
			}
		}
	}



	/**
	 * Check the status of all the threads in the rendezvous and wake them up
	 * when appropriate. Return true if all members have reached the rendezvous,
	 * false otherwise.
	 */
	private boolean checkStatus()
	{
		boolean done = true;

		// See if all the entrys are true now..
		for (int i = 0; i < targetIndex; i++) {
			if (targState[i] == false) {
				done = false;
				break;
			}
		}

		if (done == false) {
			// If not all threads have reached rendezvous point yet.
			return false;
		}else{
			// Reset the targState array.
			for (int i = 0; i < targetIndex; i++) {
				targState[i]=false;
			}

			// Wake everyone up.
			notifyAll();
			return true;
		}
	}



	/**
	 * Add a thread to the rendezvous.
	 * 
	 * @param thread   A new thread to add to the rendezvous list
	 */
	private void add(Thread thread)
	{
		if (isFull()) expandArray();

		target[targetIndex]    = thread;
		targState[targetIndex] = false;
		targetIndex++;
	}



	/**
	 * Remove a thread from the rendezvous.
	 * 
	 * @param thread   A thread to be removed from the rendezvous list
	 */
	private void remove(Thread thread)
	{
		for (int ii=0; ii < targetIndex; ii++) {
			// Find the thread in the array.
			if (target[ii] == thread) {
				// Remove the entrys..
				System.arraycopy(target,ii+1,target,ii,target.length-ii-1);
				System.arraycopy(targState,ii+1,targState,ii,targState.length-ii-1);
				targetIndex--;

				checkStatus();
				break;
			}
		}
	}



	/**
	 * Checks if the array is full
	 * 
	 * @return true if thread array is full
	 */
	private boolean isFull()
	{
		return (target.length == targetIndex);
	}



	/**
	 * Expands the thread array by 10 entries
	 */
	private void expandArray()
	{
		int  len = target.length + 10;

		// Make new arrays with more space.
		Thread[]  newTarget = new Thread[len];
		boolean[] newState  = new boolean[len];

		// Copy contents of old array to new array.
		System.arraycopy(target,0,newTarget,0,targetIndex);
		System.arraycopy(targState,0,newState,0,targetIndex);

		// Replace target w/ the new array.
		target    = newTarget;
		targState = newState;
	}



	// Array to store all the threads involved in the rendezvous.
	private Thread[] target;

	// Keeps track of the number of threads in target
	private int targetIndex;

	// Array to set the state of all the threads.
	private boolean[] targState;
}


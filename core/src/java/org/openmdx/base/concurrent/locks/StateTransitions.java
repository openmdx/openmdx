/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateTransitions.java,v 1.10 2008/03/21 18:28:47 hburger Exp $
 * Description: State Transitions
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:28:47 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
package org.openmdx.base.concurrent.locks;


import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * State Transitions
 */
public class StateTransitions <S extends Enum<S>>{

    /**
     * Constructor
     *
     * @param initialState the initial state
     */
    public StateTransitions(
        S initialState
    ){
        this(initialState, null, false);
    }

    /**
     * Constructor
     * 
     * @param initial
     * @param id the id used for log() and toString()
     * @param debug tells whether logging is enabled
     */
    public StateTransitions(
        S initial,
        String id,
        boolean debug
    ){
        this.state = initial;
        this.id = id == null ? super.toString() : id;
        this.debug = debug;
    }

    /**
     * The current state
     */
    private S state;

    /**
     * The id is used for toString() and log().
     */
    private final String id;

    /**
     * The debug flag switches logging on and off
     */
    private final boolean debug;

    /**
     * Retrieve the state.
     * 
     * @return the <code>status</code>'s value
     */
    public synchronized S getState() {
        return this.state;
    }

    /**
     * Test whether the current state matches a given one.
     * 
     * @return <code>true</code> if the current state queals the given one.
     */
    public boolean stateMatches (
        S state
    ){
        return getState() == state;
    }

    /**
     * Test whether the current state matches a given one.
     * 
     * @return <code>true</code> if the current state queals the given one.
     */
    public boolean stateMatches (
        EnumSet<S> stati
    ){
        return stati.contains(getState());
    }

    /**
     * Set the state
     *
     * @param state The <code>status</code>'s value
     */
    public synchronized void setState(
        S state
    ) {
        if(this.debug) log("transitions to " + state);
        this.state = state;
    }

    /**
     * Set the state
     * 
     * @param event the event leading to thei change in state
     * @param state The <code>status</code>'s value
     */
    public final void setState(
        Condition event,
        S state
    ) {
        setState(state);
        if(event != null) event.signal();
    }

    /**
     * Tests whether the expected <code>Status</code> matches the current one.
     * 
     * @param expected the expected <code>Status</code>
     * 
     * @throws IllegalStateException if the expected <code>Status</code> does 
     * not match the current one
     */
    public void assertState(
        S expected
    ){
        S current = getState();
        if(current != expected) {
            throw new IllegalStateException(
                this.id +
                ": Expected status " + expected +
                " does not match the current one: " + current
            );
        }
    }

    /**
     * Tests whether the current <code>Status</code> value is among the
     * expected ones.
     * 
     * @param expected the expected <code>Status</code> set
     * 
     * @throws IllegalStateException if the current <code>Status</code> is not 
     * among the expected ones
     */
    public void assertState(
        EnumSet<S> expected
    ){
        S current = getState();
        if(!expected.contains(current)) {
            throw new IllegalStateException(
                this.id + ": Current status " + current +
                " is not among the expected ones: " + expected
            );
        }
    }

    /**
     * Change the state
     * 
     * @param from
     * @param event
     * @param to
     */
    public synchronized void transition(
        S from,
        Condition event,
        S to
    ){
        assertState(from);
        setState(event, to);
    }

    /**
     * Wait for one of a given set of states
     * 
     * @param stati the status one is waiting for
     * @param condition the condition leading to the awaited transition
     * @param timeout the timeout in milliseconds
     * 
     * @return <code>true</code> if one of the requested states has been 
     * reached; <code>false</code> if the method timed out. 
     * 
     * @throws InterruptedException if the thread is interrupted
     */
    public boolean awaitState(
        EnumSet<S> stati,
        Condition event,
        long timeout
    ) throws InterruptedException {
        if(this.debug) log("is waiting for " + stati);
        if(timeout <= 0L) {
            awaitState(stati, event);
        } else {
            for(
                long remaining = TimeUnit.MILLISECONDS.toNanos(timeout);
                !stateMatches(stati);
                remaining = event.awaitNanos(remaining)
            ) {
                if (remaining <= 0L) return false;
            }
        }
        if(this.debug) log("resumes");
        return true;
    }

    /**
     * Wait for one of a given set of states
     * 
     * @param stati the status one is waiting for
     * @param condition the condition leading to the awaited transition
     * 
     * @throws InterruptedException if the thread is interrupted
     */
    public void awaitState(
        EnumSet<S> stati,
        Condition event
    ) throws InterruptedException {
        while(!stateMatches(stati)) event.await();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.id + " (" + this.state + ")";
    }

    /**
     * Log a nessage
     * 
     * @param message
     */
    private void log(
        String message
    ){
        System.out.println(
             this.toString() + ' ' + message
        );
    }
}

/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: State.java,v 1.1 2006/01/06 17:44:07 hburger Exp $
 * Description: Object States
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/01/06 17:44:07 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.test.uses.java.lang.jre.before1_5;




import org.openmdx.uses.java.lang.Enum;
import org.openmdx.uses.java.util.EnumSet;


/**
 * Object States
 * 
 * <pre>
 *  public enum State {
 *      TRANSIENT(
 *         EnumSet.noneOf(Interrogation.class)
 *      ),
 *      TRANSIENT_CLEAN(
 *         EnumSet.of(Interrogation.TRANSACTIONAL)
 *      ),
 *      TRANSIENT_DIRTY(
 *         EnumSet.of(Interrogation.TRANSACTIONAL, Interrogation.DIRTY)
 *      ),
 *      PERSISTENT_NEW(
 *         EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL, Interrogation.DIRTY, Interrogation.NEW)
 *      ),
 *      PERSISTENT_NONTRANSACTIONAL(
 *         EnumSet.of(Interrogation.PERSISTENT)
 *      ),
 *      PERSISTENT_CLEAN(
 *         EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL)
 *      ),
 *      PERSISTENT_DIRTY(
 *         EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL, Interrogation.DIRTY)
 *      ),
 *      HOLLOW(
 *         EnumSet.of(Interrogation.PERSISTENT)
 *      ),
 *      PERSISTENT_DELETED(
 *         EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL, Interrogation.DIRTY, Interrogation.DELETED)
 *      ),
 *      PERSISTENT_NEW_DELETED(
 *         EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL, Interrogation.DIRTY, Interrogation.NEW, Interrogation.DELETED)
 *      ),
 *      DETACHED_CLEAN(
 *         EnumSet.of(Interrogation.DETACHED)
 *      ),
 *      DETACHED_DIRTY(
 *         EnumSet.of(Interrogation.DIRTY, Interrogation.DETACHED)
 *      );
 *      public EnumSet<Interrogation> interrogation();
 *  }
 *</pre>
 *
 * @since openMDX 2.0
 */
public final class State extends Enum {

    public static final State TRANSIENT = new State(
        EnumSet.noneOf(Interrogation.class)
    );
    public static final State TRANSIENT_CLEAN = new State(
        EnumSet.of(Interrogation.TRANSACTIONAL)
    );
    public static final State TRANSIENT_DIRTY = new State(
        EnumSet.of(Interrogation.TRANSACTIONAL, Interrogation.DIRTY)
    );
    public static final State PERSISTENT_NEW = new State(
        EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL, Interrogation.DIRTY, Interrogation.NEW)
    );
    public static final State PERSISTENT_NONTRANSACTIONAL = new State(
        EnumSet.of(Interrogation.PERSISTENT)
    );
    public static final State PERSISTENT_CLEAN = new State(
        EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL)
    );
    public static final State PERSISTENT_DIRTY = new State(
        EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL, Interrogation.DIRTY)
    );
    public static final State HOLLOW = new State(
        EnumSet.of(Interrogation.PERSISTENT)
    );
    public static final State PERSISTENT_DELETED = new State(
        EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL, Interrogation.DIRTY, Interrogation.DELETED)
    );
    public static final State PERSISTENT_NEW_DELETED = new State(
        EnumSet.of(Interrogation.PERSISTENT, Interrogation.TRANSACTIONAL, Interrogation.DIRTY, Interrogation.NEW, Interrogation.DELETED)
    );
    public static final State DETACHED_CLEAN = new State(
        EnumSet.of(Interrogation.DETACHED)
    );
    public static final State DETACHED_DIRTY = new State(
        EnumSet.of(Interrogation.DIRTY, Interrogation.DETACHED)
    );

    private static final long serialVersionUID = 3257852064951252787L;

    public final EnumSet interrogation(){
        return this.interrogation;
    }

    public String toString(
    ){
        return this.name() + ' ' + this.interrogation();
    }

    private State(
        EnumSet interrogation
    ){
        this.interrogation = interrogation;
    }

    public static State[] values(
    ){
        return (State[]) getEnumConstants(State.class);
    }

    public static State valueOf(
        String name
    ){
        return (State) valueOf(State.class, name);
    }

    private final EnumSet interrogation;

    /**
     * Status Interrogation
     * 
     * <pre>
     *  public static enum Interrogation {
     *      PERSISTENT,
     *      TRANSACTIONAL,
     *      DIRTY,
     *      NEW,
     *      DELETED,
     *      DETACHED
     *  }   
     * </pre>
     */
    public static final class Interrogation extends Enum {

        public final static Interrogation PERSISTENT = new Interrogation();
        public final static Interrogation TRANSACTIONAL = new Interrogation();
        public final static Interrogation DIRTY = new Interrogation();
        public final static Interrogation NEW = new Interrogation();
        public final static Interrogation DELETED = new Interrogation();
        public final static Interrogation DETACHED = new Interrogation();

        private static final long serialVersionUID = 3258134656586101559L;

        private Interrogation(
        ) {
        }

        public static Interrogation[] values(
        ){
            return (Interrogation[]) getEnumConstants(Interrogation.class);
        }

        public static Interrogation valueOf(
            String name
        ){
            return (Interrogation) valueOf(Interrogation.class, name);
        }

    }
}

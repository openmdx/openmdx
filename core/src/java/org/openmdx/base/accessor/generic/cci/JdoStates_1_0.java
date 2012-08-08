/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: JdoStates_1_0.java,v 1.5 2005/07/20 13:52:27 hburger Exp $
 * Description: JDO states
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/20 13:52:27 $
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
package org.openmdx.base.accessor.generic.cci;


/**
 * JDO States
 */
public class JdoStates_1_0 {    

    //-------------------------------------------------------------------------------------
    // Interrogation
    //-------------------------------------------------------------------------------------

    public final static short PERSISTENT = 1;
    public final static short TRANSACTIONAL = 2;
    public final static short DIRTY = 4;
    public final static short NEW = 8;
    public final static short DELETED = 16;

    //-------------------------------------------------------------------------------------
    // Enumeration
    //-------------------------------------------------------------------------------------

    public final static short TRANSIENT = 0;
    public final static short TRANSIENT_CLEAN = TRANSACTIONAL;
    public final static short TRANSIENT_DIRTY = TRANSACTIONAL|DIRTY;
    public final static short PERSISTENT_NEW = PERSISTENT|TRANSACTIONAL|DIRTY|NEW;
    public final static short PERSISTENT_NONTRANSACTIONAL = PERSISTENT;
    public final static short PERSISTENT_CLEAN = PERSISTENT|TRANSACTIONAL;
    public final static short PERSISTENT_DIRTY = PERSISTENT|TRANSACTIONAL|DIRTY;
    public final static short HOLLOW = PERSISTENT;
    public final static short PERSISTENT_DELETED = PERSISTENT|TRANSACTIONAL|DIRTY|DELETED;
    public final static short PERSISTENT_NEW_DELETED = PERSISTENT|TRANSACTIONAL|DIRTY|NEW|DELETED;
        
}

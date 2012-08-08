/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ObjectId.java,v 1.3 2008/02/08 16:50:51 hburger Exp $
 * Description: ApplicationIdentity 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:50:51 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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
package org.oasisopen.spi2;

import java.util.List;

/**
 * ApplicationIdentity
 */
public interface ObjectId {

    /**
     * Retrieve the parent object's application identity.
     * 
     * @param baseClass the parent object's fully qualified base class name;
     * this argument may be <code>null</code> in case of mix-in
     * @return the parent object's application identity;
     * or <code>null</code><ul>
     * <li>if the object has no parent
     * <li>if the application identity is not hierarchical
     * </ul>
     */
    public ObjectId getParentObjectId(
        List<String> baseClass
    );
    
    /**
     * Retrieve the object's class or base-class.
     * 
     * @return the target class; or <code>null</code> if the target class is
     * unknown 
     * 
     */
    public List<String> getTargetClass(
    );

    /**
     * Tells how many qualifiers this application identity consists of.
     * 
     * @return the number of qualifiers this application identity consists of
     */
    public int getQualifierCount(
    );
        
    /**
     * Tells whether a given qualifier has a persistent sub-segement
     * 
     * @param index the index of the requested sub-segment
     * 
     * @return <code>true</code> if the requested sub-segment is persistent
     */
    public boolean isQualifierPersistent(
        int index
    );

    /**
     * Retrieves a given qualifier value
     * 
     * @param index the index of the requested sub-segment
     * 
     * @return the requested sub-segment value
     */
    public <E> E getQualifier(
        Class<E> qualifierClass,
        int index
    );

}
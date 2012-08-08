/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceHelper.java,v 1.4 2009/02/10 16:51:12 hburger Exp $
 * Description: PersistenceHelper 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/10 16:51:12 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.cci;

import javax.jdo.JDOHelper;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * PersistenceHelper
 */
public class PersistenceHelper {

    /**
     * Constructor 
     */
    private PersistenceHelper() {
        // Avoid instantiation
    }

    /**
     * Return a clone of the object
     * 
     * @param object
     * 
     * @return a clone
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(
        T object
    ) {
        if(object instanceof org.openmdx.base.persistence.spi.Cloneable) {
            return ((org.openmdx.base.persistence.spi.Cloneable<T>)object).openmdxjdoClone();
        }
        if(object instanceof java.lang.Cloneable) try {
            return (T) object.getClass(
            ).getMethod(
                "clone"
            ).invoke(
                object
            );
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "A class declared as Cloneable can't be cloned",
                new BasicException.Parameter("interface", java.lang.Cloneable.class.getName()),
                new BasicException.Parameter("class", object.getClass().getName())

            );
        }
        return null;
    }

    /**
     * Retrieve the object id for persistent objects and the
     * transactional object id for transient objects, respectively.
     * 
     * @param pc
     * 
     * @return an object id, or <code>null</code> if <code>pc</code> is <code>null</code>
     */
    public static Object getCurrentObjectId(
        Object pc
    ){
        return 
            pc == null ? null : 
            JDOHelper.isPersistent(pc) ? JDOHelper.getObjectId(pc) :
            JDOHelper.getTransactionalObjectId(pc);
    }

}


/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ExceptionHelper.java,v 1.2 2010/07/12 13:19:26 hburger Exp $
 * Description: ExceptionHelper 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/07/12 13:19:26 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.spi;

import javax.jdo.JDOHelper;

import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;


/**
 * ExceptionHelper
 */
public class ExceptionHelper {

    /**
     * Constructor 
     */
    private ExceptionHelper() {
        // Avoid instantiation
    }

    /**
     * Provide an object id parameter
     * 
     * @param label
     * @param object
     * 
     * @return an object id parameter with the transient object id and 
     * optionally the object id in case of a persistent object
     */
    public static BasicException.Parameter newObjectIdParameter(
        String label,
        Object object
    ){
        return 
            object == null ? new BasicException.Parameter(
                label
            ) : JDOHelper.isPersistent(object) ? new BasicException.Parameter(
                label,
                JDOHelper.getTransactionalObjectId(object), 
                ((Path)JDOHelper.getObjectId(object)).toXRI() 
            ) : new BasicException.Parameter(
                label,
                JDOHelper.getTransactionalObjectId(object)
            );
    }

}

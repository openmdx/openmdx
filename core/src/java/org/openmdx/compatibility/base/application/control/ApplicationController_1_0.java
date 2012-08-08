/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ApplicationController_1_0.java,v 1.6 2006/01/12 00:05:39 hburger Exp $
 * Description: Base Application
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2006/01/12 00:05:39 $
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
package org.openmdx.compatibility.base.application.control;

import org.openmdx.base.application.control.ApplicationController;
import org.openmdx.kernel.log.SysLog;

/**
 * ApplicationController_1_0
 * 
 * @deprecated use ApplicationController instead
 */
public class ApplicationController_1_0 
    extends ApplicationController 
    implements ExceptionListener
{

    /**
     * @param args
     */
    public ApplicationController_1_0(String[] args) {
        super(args);
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.application.control.ExceptionListener#exception(org.openmdx.compatibility.base.application.control.ExceptionEvent)
     */
    public void exception(ExceptionEvent event) {
        Throwable e = event.getException();
        SysLog.criticalError("", e);
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.application.control.ApplicationController#handleExceptionEvent(java.lang.Exception)
     */
    protected void handleExceptionEvent(Exception exception) {
        try {
            (
                super.application instanceof ExceptionListener ? (ExceptionListener)super.application : this        
            ).exception(
                new ExceptionEvent(this, exception)
            );
        } catch (Exception handlerException){
            try {
                SysLog.error("The exception handler caused an exception", handlerException);
            } catch (Exception logException){
                // This method must never return with an exception
            }
        }
    }

}

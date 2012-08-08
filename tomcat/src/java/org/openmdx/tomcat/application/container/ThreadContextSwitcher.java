/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ThreadContextSwitcher.java,v 1.2 2008/01/25 13:27:53 hburger Exp $
 * Description: Thread Context Switcher
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 13:27:53 $
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
package org.openmdx.tomcat.application.container;

import javax.naming.NamingException;

import org.apache.naming.ContextBindings;
import org.apache.naming.ExtendedContextBindings;
import org.openmdx.kernel.application.container.spi.ejb.ContextSwitcher;

/**
 * Thread Context Switcher
 */
class ThreadContextSwitcher implements ContextSwitcher {

	public Object setBeanContext() {
		if(ContextBindings.isThreadBound()) try {
			Object callerContext = ExtendedContextBindings.getThreadName();
			ContextBindings.unbindThread(callerContext);
			return callerContext;
		} catch (NamingException exception) {
			throw new RuntimeException(
				"Context switch failure: Thread context couldn't be unbound",
				exception
			);
		} else {
			return null;
		}
	}
	
	public void setCallerContext(Object callerContext) {
		if(callerContext != null) try {
			ContextBindings.bindThread(callerContext);
		} catch (NamingException exception) {
			throw new RuntimeException(
				"Context switch failure: Thread context couldn't be re-bound",
				exception
			);
		}
	}

}

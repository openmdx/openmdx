/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ExtendedContext.java,v 1.5 2005/07/22 12:25:59 hburger Exp $
 * Description: ExtendedContext interface
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/07/22 12:25:59 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.naming;

/**
 * @deprecated use org.openmdx.base.naming.Contexts#bind(
 * javax.Naming.Context, javax.naming.Name, java.lang.Object) instead of 
 * org.openmdx.compatibility.base.naming.ExtendedContext#LENIENT.
 */
public interface ExtendedContext extends javax.naming.Context {
	
	/**
	 * If the value of the property is the string "true", it means that
	 * intermediate contexts are created on demand. A NamingException is
	 * thrown if the value is anything else and an intermediate context
	 * is missing for an operation. If unspecified, the value defaults to
	 * "false". 
	 * 
	 * @author hburger
	 */
	//String LENIENT = ExtendedContext.class.getPackage().getName() + ".lenient";

  /**
   * TODO: hard-coded to make it compile under J#. Method getPackage() not available
   * 
   * @deprecated use org.openmdx.base.naming.Contexts#getSubcontext(
   * javax.Naming.Context, javax.naming.Name) instead of 
   * org.openmdx.compatibility.base.naming.ExtendedContext#LENIENT.
   */
  String LENIENT = "org.openmdx.compatibility.base.naming.lenient";

}

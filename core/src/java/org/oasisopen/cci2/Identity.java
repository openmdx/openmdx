/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Identity.java,v 1.5 2007/12/12 18:14:38 hburger Exp $
 * Description: Object Identity 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/12 18:14:38 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.oasisopen.cci2;

import java.io.Serializable;

/**
 * Identity<ul>
 * <li>A <em>persistent object's</em> <code>Identity</code> has one of the following XRI 2 authority forms<ul>
 * <li><b>@openmdx*</b><i>&lsaquo;openmdx-authority&rsaquo;</i></b>
 * <li><b>@openmdx*</b><i>&lsaquo;openmdx-authority&rsaquo;</i><b>*($t*(+class)*</b><i>&lsaquo;model-class&rsaquo;</i><b>)</b>
 * </ul>
 * <p>
 * The <i>openmdx-authority</i> sub-segment is defined as 
 * following:<ul>
 * <li>In general, the <i>openmdx-authority</i> begins with the top level domain name 
 * of the organization and then the organization's domain and then any 
 * subdomains listed in reverse order.
 * <li>Its components are separated by dots (<b>.</b>).
 * <li>They should be all lowercase characters whenever possible.
 * </ul>
 * <p>
 * The <i>model-class</i> sub-segment is defined as following:<ul>
 * <li>The <i>model-class</i> begins with an optional namespace followed by
 * the class name.
 * <li>Its components are separated by two colons (<b>::</b>).
 * <li>The namespace should be omitted if it consists of exactly the same 
 * components as the the <i>openmdx-authority</i>.
 * </ul>
 * <li>A <em>transient object's</em> <code>Identity</code> has the following XRI 2 authority form<ul>
 * <li><b>$t*uuid*</b><i>&lsaquo;uuid&rsaquo;</i>
 * </ul>
 */
public interface Identity extends Serializable {
    // a marker interface
}

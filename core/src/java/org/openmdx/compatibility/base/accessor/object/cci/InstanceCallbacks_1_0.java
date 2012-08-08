/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InstanceCallbacks_1_0.java,v 1.4 2004/04/02 16:59:01 wfro Exp $
 * Description: SPICE Object Layer: Instance callbacks interface
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/02 16:59:01 $
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
package org.openmdx.compatibility.base.accessor.object.cci;

import org.openmdx.base.exception.ServiceException;

/**
 * This interface defines the methods executed by the manager for these life 
 * cycle events. 
 * 
 * @deprecated use org.openmdx.base.event.InstanceCallbackListener
 */
public interface InstanceCallbacks_1_0 {

  /**
   * Called after the values are loaded from the data store into this instance. 
   * Derived fields should be initialized in this method.
   * 
   * @deprecated use org.openmdx.base.event.InstanceCallbackListener#postLoad(InstanceCallbackEvent)
   */
  public void objPostLoad(
  ) throws ServiceException;

  /**
   * Called before the values are stored from this instance. 
   * Fields that might have been affected by modified non-persistent fields should be 
   * updated in this method. 
   * 
   * @deprecated use org.openmdx.base.event.InstanceCallbackListener#preStore(InstanceCallbackEvent)
   */
  public void objPreStore(
  ) throws ServiceException;

  /**
   * Called before the values in the instance are cleared. 
   * Transient fields should be cleared in this method. Associations between this 
   * instance and others in the runtime environment should be cleared. 
   * 
   * @deprecated use org.openmdx.base.event.InstanceCallbackListener#preClear(InstanceCallbackEvent)
   */
  public void objPreClear(
  ) throws ServiceException;

  /**
   * Called before the instance is deleted. This method is called before the state 
   * transition to persistent-deleted or persistent-new-deleted. Access to field 
   * values within this call are valid. Access to field values after this call 
   * are disallowed. 
   * 
   * @deprecated use org.openmdx.base.event.InstanceCallbackListener#preDeleteInstanceCallbackEvent)
   */
  public void objPreDelete(
  ) throws ServiceException;

}


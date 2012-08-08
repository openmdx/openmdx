/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: KeepCauseErrorHandler.java,v 1.2 2005/01/21 23:39:19 wfro Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/01/21 23:39:19 $
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
package org.openmdx.compatibility.base.dataprovider.exporter;

import org.openmdx.base.exception.ServiceException;

/**
 * ErrorHandler which keeps the first excpetion. This is most often more
 * relevant to finding a problem then the subsequent ones.
 */
public class KeepCauseErrorHandler implements ErrorHandler {

   private ServiceException causeException = null;
   private ServiceException causeWarning = null;
   
   /**
    * Report an error, just the first entry is accepted, subsequent ones 
    * get lost.
    */
   public void error(ServiceException error) throws ServiceException {
      setCauseException(error);
   }

   /**
    * Report a fatalError, just the first entry is accepted, subsequent ones
    * get lost.
    */
   public void fatalError(ServiceException fatalError) throws ServiceException {
      setCauseException(fatalError);
   }

   /**
    * Report a warning, just the first warning is accepted, subsequent ones 
    * get lost.
    */
   public void warning(ServiceException warning) throws ServiceException {
       if (this.causeWarning == null) {
           this.causeWarning = warning;
       }
   }
   
   /**
    * CauseException is the first, don't allow to be replaced.
    */
   protected void setCauseException(ServiceException e) {
      if (this.causeException == null) {
         this.causeException = e;
      }
   }
   
   /**
    * Get the cause exception which has been reported. 
    */
   public ServiceException getCauseException() {
      return this.causeException;
   }
   
   /**
    * Get the first reported warning
    */
   public Exception getCauseWarning() {
       return this.causeWarning;
   }

}

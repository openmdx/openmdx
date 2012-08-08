/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefException_1.java,v 1.2 2008/02/08 16:51:25 hburger Exp $
 * Description: RefException_1 class
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/08 16:51:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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

/**
 * @author wfro
 */
package org.openmdx.base.accessor.jmi.spi;

import java.util.List;
import java.io.PrintWriter;
import java.io.PrintStream;

import javax.jmi.reflect.RefException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * RefException extension 
 */
public class RefException_1
  extends RefException
  implements BasicException.Wrapper
{

  /**
     * 
     */
    private static final long serialVersionUID = 1060426658889181727L;

//-------------------------------------------------------------------------
  public RefException_1(
    Exception cause,
    String exceptionDomain,
    int exceptionCode,
    String description,
    BasicException.Parameter[] properties
  ) {
    this.exceptionStack = new BasicException(
      cause,
      exceptionDomain,
      exceptionCode,
      properties,
      description,
      this
    );
  }

  //-------------------------------------------------------------------------
  public RefException_1(
    BasicException.Parameter[] properties
  ) {
    this(
      null,
      BasicException.Code.DEFAULT_DOMAIN,
      BasicException.Code.PROCESSING_FAILURE,
      null,
      properties
    );
  }

  //-------------------------------------------------------------------------
  public RefException_1(
    String domain,
    int errorCode,
    String description,
    BasicException.Parameter[] properties
  ) {
    this(
      null,
      domain,
      errorCode,
      description,
      properties
    );
  }

  //-------------------------------------------------------------------------
  public RefException_1(
    ServiceException e
  ) {
    this(e.getExceptionStack());
  }

  //-------------------------------------------------------------------------
  public RefException_1(
    BasicException e
  ) {
    super(e.getMessage());
    this.exceptionStack = e;
  }

  //-------------------------------------------------------------------------
    public RefException_1 log()
    {
    SysLog.warning(getMessage(), getCause(), 1);
    return this;
  }

  //-------------------------------------------------------------------------
  public java.lang.Object refGetValue(
    String propertyName
  ) {
    if(this.exceptionStack != null) {
      BasicException.Parameter[] parameters = this.exceptionStack.getParameters();
      for(int i = 0; i < parameters.length; i++) {
        if(parameters[i].getName().equals(propertyName)) {
          return parameters[i].getValue(); //Now String, not List!
        }
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  public java.lang.Object refGetValue(
    String propertyName,
    int index
  ) {
    java.lang.Object values = refGetValue(propertyName);
    if(values != null) {
      return ((List<?>)values).get(index);
    }
    return null;
  }

  //-------------------------------------------------------------------------
  public static BasicException.Parameter refNewProperty(
    String propertyName,
    java.lang.Object propertyValue
  ) {
    return new BasicException.Parameter(
      propertyName,
      propertyValue
    );
  }

  //-------------------------------------------------------------------------
  public ServiceException refGetServiceException(
  ) {
    return this.exceptionStack == null ?
      null :
      new ServiceException(this.exceptionStack);
  }

  //------------------------------------------------------------------------
  // Implements BasicException.Wrapper
  //------------------------------------------------------------------------

  /**
   * Return a BasicException, this exception object's cause.
   * 
   * @return the BasicException wrapped by this object.
   */
  public BasicException getExceptionStack (
  ) {
    return this.exceptionStack;
  }

  /**
   * Retrieves the exception domain of this <code>ServiceException</code>.
   *
   * @return the exception domain
   */
  public String getExceptionDomain()
  {
    return this.exceptionStack == null ?
      null :
      this.exceptionStack.getExceptionDomain();
  }

  /**
   * Retrieves the exception code of this <code>ServiceException</code>.
   *
   * @return the exception code
   */
  public int getExceptionCode()
  {
    return this.exceptionStack == null ?
      BasicException.Code.GENERIC :
      this.exceptionStack.getExceptionCode();
  }

  /**
   * Returns the cause belonging to a specific exception domain.
   * 
   * @param   exceptionDomain
   * 		  the desired exception domain,
   *          or <code>null</code> to retrieve the initial cause.
   *
   * @return  Either the cause belonging to a specific exception domain
   *          or the initial cause if <code>exceptionDomain</code> is
   * 		  <code>null</code>.  
   */
  public BasicException getCause(
    String exceptionDomain
  ){
    return this.exceptionStack == null ?
        null :
        this.exceptionStack.getCause(exceptionDomain);
  }

  /**
   * The exception stack
   */
   private BasicException exceptionStack;

  //------------------------------------------------------------------------
  // Extends Throwable
  //------------------------------------------------------------------------

  /**
   * Returns the detail message string of this RuntimeServiceException.  
   */
  public String getMessage(
  ){
    return this.exceptionStack == null ?
      super.getMessage() :
      this.exceptionStack.getMessage() + ": " +
      this.exceptionStack.getDescription();
  }

  /**
   * A String consisting of the class of this exception, the exception 
   * domain, the exception code, the exception description and the exception
   * stack.
   * 
   * @return a multiline representation of this exception.
   */
  public String toString(){
    return
      this.exceptionStack == null ?
      super.toString() :
      super.toString() + '\n' + this.exceptionStack;
  }

  /**
   * Initializes the cause of this throwable to the specified value. 
   * (The cause is the throwable that caused this throwable to get thrown.) 
   * 
   * @param  cause
   *  the cause (which is saved for later retrieval by the
   *  getCause() method). (A null value is permitted, and indicates 
   *  that the cause is nonexistent or unknown.) 
   *
   * @return a reference to this RuntimeServiceException instance. 
   *
   * @exception  IllegalArgumentException
   *  if cause is this throwable.
   *  (A throwable cannot be its own cause.) 
   * @exception   IllegalStateException
   *  if the cause is already set.
   */
  public Throwable initCause(
    Throwable cause
  ){
    throw new IllegalStateException(
      "A RuntimeServiceException's cause can't be changed"
    );
  }

  /**
   * Returns the cause of an exception. The cause actually is the wrapped
   * exception.
   *
   * @return Throwable  The exception cause.
   */
  public Throwable getCause(
  ){
    return this.exceptionStack;
  }

  /* (non-Javadoc)
   * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
   */
  public void printStackTrace(PrintStream s) {
    if(this.exceptionStack == null){
      super.printStackTrace(s);
    } else {
      this.exceptionStack.printStack(this, s, true);
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
   */
  public void printStackTrace(PrintWriter s) {
    if(this.exceptionStack == null){
      super.printStackTrace(s);
    } else {
      this.exceptionStack.printStack(this, s, true);
    }
  }

}
//--- End of File -----------------------------------------------------------

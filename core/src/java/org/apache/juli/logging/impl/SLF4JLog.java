/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: SLF4JLog.java,v 1.1 2007/12/19 15:48:12 hburger Exp $
 * Description: SLF4J Log 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/12/19 15:48:12 $
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
 * 
 * ------------------
 * 
 * The original file has been provided by SLF4J (http://www.slf4j.org)
 */
package org.apache.juli.logging.impl;

import org.apache.juli.logging.Log;
import org.slf4j.Logger;

/**
 * Implementation of {@link Log org.apache.commons.logging.Log} interface which 
 * delegates all processing to a wrapped {@link Logger org.slf4j.Logger} instance.
 * 
 * <p>JCL's FATAL and TRACE levels are mapped to ERROR and DEBUG respectively. All 
 * other levels map one to one.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class SLF4JLog implements Log {

  private Logger logger;

  SLF4JLog(Logger logger) {
    this.logger = logger;
  }

  /**
   * Directly delegates to the wrapped <code>org.slf4j.Logger</code> instance.
   */
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  /**
   * Directly delegates to the wrapped <code>org.slf4j.Logger</code> instance.
   */
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  /**
   * Delegates to the <code>isErrorEnabled<code> method of the wrapped 
   * <code>org.slf4j.Logger</code> instance.
   */
  public boolean isFatalEnabled() {
    return logger.isErrorEnabled();
  }

  /**
   * Directly delegates to the wrapped <code>org.slf4j.Logger</code> instance.
   */
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  /**
   * Delegates to the <code>isDebugEnabled<code> method of the wrapped 
   * <code>org.slf4j.Logger</code> instance.
   */
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  /**
   * Directly delegates to the wrapped <code>org.slf4j.Logger</code> instance.
   */
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  /**
   * Converts the input parameter to String and then delegates to 
   * the debug method of the wrapped <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   */
  public void trace(Object message) {
    logger.trace(String.valueOf(message));
  }

  /**
   * Converts the first input parameter to String and then delegates to 
   * the debug method of the wrapped <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   * @param t the exception to log
   */
  public void trace(Object message, Throwable t) {
    logger.trace(String.valueOf(message), t);
  }

  /**
   * Converts the input parameter to String and then delegates to the wrapped 
   * <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String} 
   */
  public void debug(Object message) {
    logger.debug(String.valueOf(message));
  }

  /**
   * Converts the first input parameter to String and then delegates to 
   * the wrapped <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   * @param t the exception to log
   */
  public void debug(Object message, Throwable t) {
    logger.debug(String.valueOf(message), t);
  }

  /**
   * Converts the input parameter to String and then delegates to the wrapped 
   * <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String} 
   */
  public void info(Object message) {
    logger.info(String.valueOf(message));
  }

  /**
   * Converts the first input parameter to String and then delegates to 
   * the wrapped <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   * @param t the exception to log
   */
  public void info(Object message, Throwable t) {
    logger.info(String.valueOf(message), t);
  }

  /**
   * Converts the input parameter to String and then delegates to the wrapped 
   * <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   */
  public void warn(Object message) {
    logger.warn(String.valueOf(message));
  }

  /**
   * Converts the first input parameter to String and then delegates to 
   * the wrapped <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   * @param t the exception to log
   */
  public void warn(Object message, Throwable t) {
    logger.warn(String.valueOf(message), t);
  }

  /**
   * Converts the input parameter to String and then delegates to the wrapped 
   * <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   */
  public void error(Object message) {
    logger.error(String.valueOf(message));
  }

  /**
   * Converts the first input parameter to String and then delegates to 
   * the wrapped <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   * @param t the exception to log
   */
  public void error(Object message, Throwable t) {
    logger.error(String.valueOf(message), t);
  }


 
  /**
   * Converts the input parameter to String and then delegates to 
   * the error method of the wrapped <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   */
  public void fatal(Object message) {
    logger.error(String.valueOf(message));
  }

  /**
   * Converts the first input parameter to String and then delegates to 
   * the error method of the wrapped <code>org.slf4j.Logger</code> instance.
   * 
   * @param message the message to log. Converted to {@link String}  
   * @param t the exception to log
   */
  public void fatal(Object message, Throwable t) {
    logger.error(String.valueOf(message), t);
  }

}
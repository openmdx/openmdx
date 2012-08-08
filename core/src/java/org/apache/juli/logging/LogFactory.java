/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LogFactory.java,v 1.1 2007/12/19 15:48:12 hburger Exp $
 * Description: SLF4J Log Factory 
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
 * The original file in the package org.apache.commons.logging has been 
 * provided under the following terms:
 * 
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.juli.logging;

import org.apache.juli.logging.impl.SLF4JLogFactory;

/**
 * <p>
 * Factory for creating {@link Log} instances, which always delegates to an instance of
 * {@link SLF4JLogFactory}.
 * 
 * </p>
 * 
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author Richard A. Sitze
 * @author Ceki G&uuml;lc&uuml; 
 */

public abstract class LogFactory {

  static LogFactory logFactory = new SLF4JLogFactory();

  /**
   * The name of the property used to identify the LogFactory implementation
   * class name.
   * <p>
   * This property is not used but preserved here for compatibility.
   */
  public static final String FACTORY_PROPERTY =
      "org.apache.juli.logging.LogFactory";

  /**
   * The fully qualified class name of the fallback <code>LogFactory</code>
   * implementation class to use, if no other can be found. 
   * 
   * <p>This property is not used but preserved here for compatibility.
   */
  public static final String FACTORY_DEFAULT =
      "org.apache.juli.logging.impl.SLF4JLogFactory";

  /**
   * The name of the properties file to search for. 
   * <p>
   * This property is not used but preserved here for compatibility.
   */
  public static final String FACTORY_PROPERTIES =
      "commons-logging.properties";

  
  /**
   * Protected constructor that is not available for public use.
   */
  protected LogFactory() {
  }

  // --------------------------------------------------------- Public Methods

  /**
   * Return the configuration attribute with the specified name (if any), or
   * <code>null</code> if there is no such attribute.
   * 
   * @param name
   *          Name of the attribute to return
   */
  public abstract Object getAttribute(String name);

  /**
   * Return an array containing the names of all currently defined configuration
   * attributes. If there are no such attributes, a zero length array is
   * returned.
   */
  public abstract String[] getAttributeNames();

  /**
   * Convenience method to derive a name from the specified class and call
   * <code>getInstance(String)</code> with it.
   * 
   * @param clazz
   *          Class for which a suitable Log name will be derived
   * 
   * @exception LogConfigurationException
   *              if a suitable <code>Log</code> instance cannot be returned
   */
  public abstract Log getInstance(Class clazz) throws LogConfigurationException;

  /**
   * <p>
   * Construct (if necessary) and return a <code>Log</code> instance, using
   * the factory's current set of configuration attributes.
   * </p>
   * 
   * <p>
   * <strong>NOTE </strong>- Depending upon the implementation of the
   * <code>LogFactory</code> you are using, the <code>Log</code> instance
   * you are returned may or may not be local to the current application, and
   * may or may not be returned again on a subsequent call with the same name
   * argument.
   * </p>
   * 
   * @param name
   *          Logical name of the <code>Log</code> instance to be returned
   *          (the meaning of this name is only known to the underlying logging
   *          implementation that is being wrapped)
   * 
   * @exception LogConfigurationException
   *              if a suitable <code>Log</code> instance cannot be returned
   */
  public abstract Log getInstance(String name) throws LogConfigurationException;

  /**
   * Release any internal references to previously created {@link Log}instances
   * returned by this factory. This is useful in environments like servlet
   * containers, which implement application reloading by throwing away a
   * ClassLoader. Dangling references to objects in that class loader would
   * prevent garbage collection.
   */
  public abstract void release();

  /**
   * Remove any configuration attribute associated with the specified name. If
   * there is no such attribute, no action is taken.
   * 
   * @param name
   *          Name of the attribute to remove
   */
  public abstract void removeAttribute(String name);

  /**
   * Set the configuration attribute with the specified name. Calling this with
   * a <code>null</code> value is equivalent to calling
   * <code>removeAttribute(name)</code>.
   * 
   * @param name
   *          Name of the attribute to set
   * @param value
   *          Value of the attribute to set, or <code>null</code> to remove
   *          any setting for this attribute
   */
  public abstract void setAttribute(String name, Object value);

  // --------------------------------------------------------- Static Methods

  /**
   * <p>
   * Construct (if necessary) and return a <code>LogFactory</code> instance,
   * using the following ordered lookup procedure to determine the name of the
   * implementation class to be loaded.
   * </p>
   * <ul>
   * <li>The <code>org.apache.commons.logging.LogFactory</code> system
   * property.</li>
   * <li>The JDK 1.3 Service Discovery mechanism</li>
   * <li>Use the properties file <code>commons-logging.properties</code>
   * file, if found in the class path of this class. The configuration file is
   * in standard <code>java.util.Properties</code> format and contains the
   * fully qualified name of the implementation class with the key being the
   * system property defined above.</li>
   * <li>Fall back to a default implementation class (
   * <code>org.apache.juli.logging.impl.SLF4FLogFactory</code>).</li>
   * </ul>
   * 
   * <p>
   * <em>NOTE</em>- If the properties file method of identifying the
   * <code>LogFactory</code> implementation class is utilized, all of the
   * properties defined in this file will be set as configuration attributes on
   * the corresponding <code>LogFactory</code> instance.
   * </p>
   * 
   * @exception LogConfigurationException
   *              if the implementation class is not available or cannot be
   *              instantiated.
   */
  public static LogFactory getFactory() throws LogConfigurationException {
    return logFactory;
  }

  /**
   * Convenience method to return a named logger, without the application having
   * to care about factories.
   * 
   * @param clazz
   *          Class from which a log name will be derived
   * 
   * @exception LogConfigurationException
   *              if a suitable <code>Log</code> instance cannot be returned
   */
  public static Log getLog(Class clazz) throws LogConfigurationException {

    return (getFactory().getInstance(clazz));
  }

  /**
   * Convenience method to return a named logger, without the application having
   * to care about factories.
   * 
   * @param name
   *          Logical name of the <code>Log</code> instance to be returned
   *          (the meaning of this name is only known to the underlying logging
   *          implementation that is being wrapped)
   * 
   * @exception LogConfigurationException
   *              if a suitable <code>Log</code> instance cannot be returned
   */
  public static Log getLog(String name) throws LogConfigurationException {

    return (getFactory().getInstance(name));

  }

  /**
   * Release any internal references to previously created {@link LogFactory}
   * instances that have been associated with the specified class loader (if
   * any), after calling the instance method <code>release()</code> on each of
   * them.
   * 
   * @param classLoader
   *          ClassLoader for which to release the LogFactory
   */
  public static void release(ClassLoader classLoader) {
    // since SLF4J based JCL does not make use of classloaders, there is nothing
    // to do here
  }

  /**
   * Release any internal references to previously created {@link LogFactory}
   * instances, after calling the instance method <code>release()</code> on
   * each of them. This is useful in environments like servlet containers, which
   * implement application reloading by throwing away a ClassLoader. Dangling
   * references to objects in that class loader would prevent garbage
   * collection.
   */
  public static void releaseAll() {
    // since SLF4J based JCL does not make use of classloaders, there is nothing
    // to do here
  }

}
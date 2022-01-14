/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: RefObject_1_0 interface
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.cci;

import java.util.Set;

import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.naming.Path;

/**
 * This interface extends the javax.jmi.reflect.RefObject interface 
 * by additional helpers and openMDX-specific methods. These
 * methods must not be used by 100% JMI-compliant applications.
 */
public interface RefObject_1_0 extends RefObject {

  //-------------------------------------------------------------------------
  // Object_1_0 like accessors
  // Most of this methods are delegated to refDelegate() but offer JMI-like
  // signatures.
  //-------------------------------------------------------------------------
  /**
   * Return the object which RefObject_1_0 delegates to. The returned object
   * is managed by a basic accessor.
   * 
   * @return Object_1_0 delegate object.
   */
  public ObjectView_1_0 refDelegate(
  );

  /**
   * Returns the object's access path.
   *
   * @return  the object's access path;
   *           or null for transient objects
   */
  public Path refGetPath(
  );

  /**
   * Returns the refDelegate().objDefaultFetchGroup() plus the set of all
   * non-derived attributes of the object.
   *
   */
  public Set<String> refDefaultFetchGroup(
  );

 /**
   * Initializes the object as follows:
   * <ul>
   *   <li>collections are cleared.</li>
   *   <li>primitive required attributes are set to default values: 
   *       string = ""; number = 0; date = min date. They are set to null if setRequiredToNull==true.</li>
   *   <li>primitive optional attributes are set to null if initializeOptional == true
   *       and are left untouched if initializeOptional == false.</li>
   *   <li>multivalued attributes are emptied if emptyMultivalued == true
   *       and are left untouched if emptyMultivalued == false.</li>
   *   <li>required references can not be initialized and an exception is thrown.</li>
   * </ul>
   * 
   * @param setRequiredToNull if true, required attributes are set to null. Otherwise
   *         they are initialized with a default value.
   * @param setOptionalToNull if true, optional features are set to null, otherwise
   *         they are left untouched.
   * @param emptyMultivalued  if true, multi-valued features are emptied, otherwise
   *         they are left untouched.
   */
  public void refInitialize(
    boolean setRequiredToNull,
    boolean setOptionalToNull, 
    boolean emptyMultivalued
  );

  /**
   * Returns the value of feature. Instead of returning the value as return
   * value it is streamed to value. The value must either be a binary or 
   * character output stream.
   *   
   * @param feature feature to be retrieved.
   * @param value binary or character output stream.
   * @param position stream is returned starting from position.
   * @return length of the stream.
   */
  public long refGetValue(
    String feature,
    Object value,
    long position
  );

  /**
   * Sets the value of feature. The value must be a binary or character
   * input stream of the specified length. The parameter length results 
   * in better performance for stream handling.
   * 
   * @param feature feature to be retrieved.
   * @param newValue binary or character input stream.
   * @param length length of the stream.
   */
  public void refSetValue(
    String feature,
    Object newValue,
    long length
  );

}

//--- End of File -----------------------------------------------------------

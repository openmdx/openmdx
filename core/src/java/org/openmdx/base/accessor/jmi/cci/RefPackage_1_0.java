/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RefPackage_1_0.java,v 1.5 2004/04/07 17:01:18 wfro Exp $
 * Description: RefPackage_1_0 interface
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/04/07 17:01:18 $
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
package org.openmdx.base.accessor.jmi.cci;

import javax.jmi.reflect.RefObject;
import javax.jmi.reflect.RefPackage;
import javax.jmi.reflect.RefStruct;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.AttributeSpecifier;
import org.openmdx.compatibility.base.query.FilterProperty;

/**
 * This interface extends the javax.jmi.reflect.RefPackage interface by
 * openMDX-specific helpers. This methods must not be used
 * by 100% JMI-compliant applications. 
 */
public interface RefPackage_1_0 
  extends RefPackage {

  /**
   * Returns model defined for this package.
   * 
   * @return Model_1_0 model assigned to this package.
   */
  public Model_1_0 refModel(
  );

  /**
   * Returns the object factory from which the package creates and retrieves objects.
   * 
   * @return ObjectFactory_1_0 object factory. 
   */
  public ObjectFactory_1_0 refObjectFactory(
  );
    
  /**
   * Get object with the given id. This operation is equivalent to
   * <pre>
   *   Object_1_0 object = refPackage.refObjectFactory().getObject(
   *     new Path(identity)
   *   );
   *   RefObject refObject = (RefRootPackage_1)refPackage.refOutermostPackage()).marshal(object);
   * </pre>
   * 
   * @param refMofId unique id of RefObject.
   * 
   * @return RefObject
   */
  public RefObject refObject(
    String refMofId
  );
  
  /**
   * Return the current unit of work. Equivalent to refOutermostPackage().refUnitOfWork().
   */
  public UnitOfWork_1_0 refUnitOfWork(
  );
    
  /**
   * Same as
   * <pre>
   * try {
   *   this.refUnitOfWork.begin();
   * }
   * catch(ServiceException e) {
   *   throw new JmiServiceException(e);
   * }
   * </pre>
   * The added value of this method is to map a ServiceException to a JmiServiceException
   * which simplifies exception handling.
   */
  public void refBegin();

  /**
   * Same as
   * <pre>
   * try {
   *   this.refUnitOfWork.commit();
   * }
   * catch(ServiceException e) {
   *   throw new JmiServiceException(e);
   * }
   * </pre>
   * The added value of this method is to map a ServiceException to a JmiServiceException
   * which simplifies exception handling.
   */
  public void refCommit();
  
  /**
   * Same as
   * <pre>
   * try {
   *   this.refUnitOfWork.rollback();
   * }
   * catch(ServiceException e) {
   *   throw new JmiServiceException(e);
   * }
   * </pre>
   * The added value of this method is to map a ServiceException to a JmiServiceException
   * which simplifies exception handling.
   */
  public void refRollback();

  /**
   * Creates an instance of a struct data type defined by the metaobject
   * 'structType' (or 'structName') whose attribute values are specified by arg 
   * which must be instanceof Structure_1_0. The members of the arg correspond 
   * 1-to-1 to the parameters for the specific create operation.
   */
  public RefStruct refCreateStruct(
    String structName, 
    Object arg
  );

  /**
   *  
   */
  public RefFilter_1_0 refCreateFilter(
    String filterForClass,
    FilterProperty[] filterProperties,
    AttributeSpecifier[] attributeSpecifiers
  );
  
}

//--- End of File -----------------------------------------------------------
